package gcm2sbml.gui.modelview.movie;

import gcm2sbml.gui.GCM2SBMLEditor;
import gcm2sbml.gui.modelview.movie.MoviePreferences;
import gcm2sbml.gui.modelview.movie.visualizations.ColorScheme;
import gcm2sbml.gui.modelview.movie.visualizations.component.ComponentScheme;
import gcm2sbml.gui.schematic.ListChooser;
import gcm2sbml.gui.schematic.Schematic;
import gcm2sbml.gui.schematic.SchematicObjectClickEvent;
import gcm2sbml.gui.schematic.SchematicObjectClickEventListener;
import gcm2sbml.gui.schematic.Utils;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.jfree.io.FileUtilities;
import org.jfree.ui.tabbedui.VerticalLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import parser.TSDParser;

import reb2sac.Reb2Sac;

import att.grappa.Parser;
import biomodelsim.BioSim;

public class MovieContainer extends JPanel implements ActionListener {

	public static final String COLOR_PREPEND = "_COLOR";
	public static final String MIN_PREPEND = "_MIN";
	public static final String MAX_PREPEND = "_MAX";
	
	public static final int FRAME_DELAY_MILLISECONDS = 20;
	
	private static final long serialVersionUID = 1L;
	private Schematic schematic;
	private Reb2Sac reb2sac;
	
	private GCMFile gcm;
	private BioSim biosim;
	private boolean isDirty = false;
	public boolean getIsDirty(){return isDirty;} public void setIsDirty(boolean value){isDirty = value;}
	
	TSDParser parser;
	Timer playTimer;
	
	private final String PLAYING = "playing"; 
	private final String PAUSED = "paused"; 
	private String mode = PLAYING;
	private MoviePreferences moviePreferences;
	public MoviePreferences getMoviePreferences(){return moviePreferences;}
	
	public MovieContainer(Reb2Sac reb2sac_, GCMFile gcm, BioSim biosim, GCM2SBMLEditor gcm2sbml){
		super(new BorderLayout());
		schematic = new Schematic(gcm, biosim, gcm2sbml, false, this);
		this.add(schematic, BorderLayout.CENTER);
		
		this.gcm = gcm;
		this.biosim = biosim;
		this.reb2sac = reb2sac_;
		
		loadPreferences();
		
		this.playTimer = new Timer(0, playTimerEventHandler);
		mode = PAUSED;
		
		registerEventListeners();
	}
	
	public TSDParser getTSDParser(){return parser;}
	
	private boolean isUIInitialized;
	public void display(){
		schematic.display();

		if(isUIInitialized == false){
			this.addUI();
			
			isUIInitialized = true;
		}
		/*
		if(this.parser == null)
			prepareTSDFile();
		 */
	}
	
	/**
	 * Allows the user to choose from valid TSD files, then loads and parses the file.
	 * @return
	 * @throws ListChooser.EmptyListException
	 */
	private void prepareTSDFile(){
		pause();
		
		// TODO: also search subdirectories and add everything to a tree, instead of a list
		Vector<String> filenames = new Vector<String>();
		for (String s : new File(reb2sac.getSimPath()).list()) {
			if (s.endsWith(".tsd")) {
				filenames.add(s);
			}
		}
		String filename;
		try{
			filename = ListChooser.selectFromList(BioSim.frame, filenames.toArray(), "Please choose a simulation file");
		}catch(ListChooser.EmptyListException e){
			JOptionPane.showMessageDialog(BioSim.frame, "Sorry, there aren't any simulation files. Please simulate then try again.");
			return;
		}
		if(filename == null)
			return;
		String fullFilePath = reb2sac.getSimPath() + File.separator + filename;
		this.parser = new TSDParser(fullFilePath, false);
		
		slider.setMaximum(parser.getNumSamples()-1);
		slider.setValue(0);
		
		biosim.log.addText(fullFilePath + " loaded. " + 
				String.valueOf(parser.getData().size()) +
				" rows of data loaded.");
	}
	
	JButton fileButton;
	JButton playPauseButton;
	JButton rewindButton;
	JButton singleStepButton;
	JSlider slider;
	private void addUI(){

		addPlayUI();
		//addPropertiesWindow();
		
		// add the top menu bar
		/*
		JToolBar sb = new JToolBar();
		JButton b = new JButton();
		b.addActionListener(this);
		b.setText("Choose Sim File");
		b.setActionCommand("choose_simulation_file");
		sb.add(b);
		this.add(sb, BorderLayout.NORTH);
		*/
		
	}
	
	
	private void addPlayUI(){
		// Add the bottom menu bar
		JToolBar mt = new JToolBar();
		
//		JButton loadButton = Utils.makeToolButton("", "load_test", "Test Loading Preferences", this);
//		mt.add(loadButton);
		
		fileButton = Utils.makeToolButton("", "choose_simulation_file", "Choose TSD File", this);
		mt.add(fileButton);
		
		rewindButton = Utils.makeToolButton("movie" + File.separator + "rewind.png", "rewind", "Rewind", this);
		mt.add(rewindButton);

		singleStepButton = Utils.makeToolButton("movie" + File.separator + "single_step.png", "singlestep", "Single Step", this);
		mt.add(singleStepButton);
		
		playPauseButton = Utils.makeToolButton("movie" + File.separator + "play.png", "playpause", "Play", this);
		mt.add(playPauseButton);
		
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		mt.add(slider);

		this.add(mt, BorderLayout.SOUTH);
	}
	
	private void registerEventListeners(){
		
	//	final MovieContainer self = this;
		
		// When the user clicks on an object in the schematic
		this.schematic.addSchematicObjectClickEventListener(new SchematicObjectClickEventListener() {
			
			public void SchematicObjectClickEventOccurred(SchematicObjectClickEvent evt) {
//				
//				
//				if(evt.getType() == GlobalConstants.SPECIES){
//					// A species was clicked on
//				}
//			
//				}else if(evt.getType() == GlobalConstants.COMPONENT){
//					// the clicked object is a component
//				}
			}
		});
	}
	
	/**
	 * Called whenever the play/pause button is pressed, or when the system needs to 
	 * pause the movie (such as at the end)
	 */
	private void playPauseButtonPress(){
		if(mode == PAUSED){
			if(slider.getValue() >= slider.getMaximum()-1)
				slider.setValue(0);
			playTimer.setDelay(FRAME_DELAY_MILLISECONDS);
			//playPauseButton.setText("Pause");
			Utils.setIcon(playPauseButton, "movie" + File.separator + "pause.png");
			playTimer.start();
			mode = PLAYING;
		}else{
			Utils.setIcon(playPauseButton, "movie" + File.separator + "play.png");
			playTimer.stop();
			mode = PAUSED;
		}		
	}
	
	private void pause(){
		if(mode == PLAYING)
			playPauseButtonPress();
	}
	
	/**
	 * event handler for when UI buttons are pressed.
	 */
	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		String command = event.getActionCommand();
		
		if(command.equals("rewind")){
			if(parser == null){
				JOptionPane.showMessageDialog(BioSim.frame, "Must first choose a simulation file.");
			} else {
				slider.setValue(0);
				updateVisuals();
			}
		}else if(command.equals("playpause")){
			if(parser == null){
				JOptionPane.showMessageDialog(BioSim.frame, "Must first choose a simulation file.");
			} else {
				playPauseButtonPress();
			}
		}else if(command.equals("singlestep")){
			if(parser == null){
				JOptionPane.showMessageDialog(BioSim.frame, "Must first choose a simulation file.");
			} else {
				nextFrame();
			}
		}else if(command.equals("choose_simulation_file")){
			prepareTSDFile();
		}else{
			throw new Error("Unrecognized command '" + command + "'!");
		}
	}
	
	/**
	 * event handler for when the timer ticks
	 */
	ActionListener playTimerEventHandler = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			nextFrame();
		}
	};
	
	/**
	 * advances the movie to the next frame
	 */
	private void nextFrame(){
		slider.setValue(slider.getValue()+1);
		if(slider.getValue() >= slider.getMaximum()){
			pause();
		}
		updateVisuals();
	}
	
	/**
	 * Called when the timer ticks and we need to update the colors or species and components.
	 */
	private void updateVisuals(){
		
		if(parser == null){
			throw new Error("NoSimFileChosen");
		}
		
		int frameIndex = slider.getValue();
		if(frameIndex < 0 || frameIndex > parser.getNumSamples()-1){
			throw new Error("Invalid slider value! It is outside the data range!");
		}
		
		HashMap<String, ArrayList<Double>> dataHash = parser.getHashMap();
		
		schematic.beginFrame();
		for(String s:gcm.getSpecies().keySet()){
			if(dataHash.containsKey(s)){
				double value = dataHash.get(s).get(frameIndex);
				Color color = moviePreferences.getOrCreateColorSchemeForSpecies(s).getColor(value);
				schematic.setSpeciesAnimationValue(s, color);
			}
		}
		
		for(String c:gcm.getComponents().keySet()){
			ComponentScheme cs = moviePreferences.getComponentSchemeForComponent(c);
			if(cs != null){
				Color color = cs.getColor(dataHash, frameIndex);
				schematic.setComponentAnimationValue(c, color);
			}
		}
		
		schematic.endFrame();
		
	}
	
	private String getPreferencesFullPath(){
		String path = reb2sac.getSimPath();
		String fullPath = path + File.separator + "schematic_preferences.json";
		return fullPath;
	}

	/**
	 * outputs the preferences file.
	 */
	public void savePreferences(){
		/*
		 * TODO: This should be getting called when the properties get saved.
		 * Save them to a file in path, then remove the test save button.
		 * Then work on loading the properties back.
		 */
		Gson gson = (new GsonMaker()).makeGson();
		String out = gson.toJson(this.getMoviePreferences());
		
		String fullPath = getPreferencesFullPath();
		
		FileOutputStream fHandle;
		try {
			fHandle = new FileOutputStream(fullPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(BioSim.frame, "An error occured opening preferences file " + fullPath + "\nmessage: " + e.getMessage());
			return;
		}
		
		try {
			fHandle.write(out.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(BioSim.frame, "An error occured writing the preferences file " + fullPath + "\nmessage: " + e.getMessage());
		}
		
		try {
			fHandle.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(BioSim.frame, "An error occured closing the preferences file " + fullPath + "\nmessage: " + e.getMessage());
			return;
		}
		
		biosim.log.addText("file saved to " + fullPath);
		
	}

	/**
	 * Loads the preferences file if it exists and stores it's values into the moviePreferences object.
	 * If no preferences file exists, a new moviePreferences file will still be created.
	 */
	public void loadPreferences(){
		
		// load the prefs file if it exists
		String fullPath = getPreferencesFullPath();
		String json = null;
		
		try {
			json = TSDParser.readFileToString(fullPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		if(json == null){
			moviePreferences = new MoviePreferences();			
		}else{
			Gson gson = (new GsonMaker()).makeGson();
			try{
				moviePreferences = gson.fromJson(json, MoviePreferences.class);
			}catch(Exception e){
				biosim.log.addText("An error occured trying to load the preferences file " + fullPath + " ERROR: " + e.toString());
			}
		}
	}
	
	public void copyMoviePreferencesComponent(String compName){
		this.moviePreferences.copyMoviePreferencesComponent(compName, this.gcm);
	}
	
	
	public void copyMoviePreferencesSpecies(String speciesName){
		this.moviePreferences.copyMoviePreferencesSpecies(speciesName, this.gcm);
	}
}
