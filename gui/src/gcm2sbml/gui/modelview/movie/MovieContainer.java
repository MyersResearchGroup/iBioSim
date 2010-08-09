package gcm2sbml.gui.modelview.movie;

import gcm2sbml.gui.GCM2SBMLEditor;
import gcm2sbml.gui.modelview.ListChooser;
import gcm2sbml.gui.modelview.ModelView;
import gcm2sbml.gui.modelview.Utils;
import gcm2sbml.parser.GCMFile;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import parser.TSDParser;

import reb2sac.Reb2Sac;

import biomodelsim.BioSim;

public class MovieContainer extends JPanel implements ActionListener {

	
	public static final int FRAME_DELAY_MILLISECONDS = 20;
	
	private static final long serialVersionUID = 1L;
	private ModelView modelView;
	private Reb2Sac reb2sac;
	
	private GCMFile gcm;
	private BioSim biosim;
	
	TSDParser parser;
	Timer playTimer;
	
	public MovieContainer(Reb2Sac reb2sac_, GCMFile gcm, BioSim biosim, GCM2SBMLEditor gcm2sbml){
		super(new BorderLayout());
		modelView = new ModelView(gcm, biosim, gcm2sbml, false);
		this.add(modelView, BorderLayout.CENTER);
		
		this.gcm = gcm;
		this.biosim = biosim;
		this.reb2sac = reb2sac_;
		
		this.playTimer = new Timer(0, playTimerEventHandler);
	}
	
	private boolean isUIInitialized;
	public void display(){
		modelView.display();

		if(isUIInitialized == false){

			
			this.addUI();
			
			isUIInitialized = true;
		}
		
		
		try{
			this.parser = prepareTSDFile();
		}catch(ListChooser.EmptyListException e){
			JOptionPane.showMessageDialog(biosim.frame(), "Sorry, there aren't any simulation files. Please simulate then try again.");
		}
	}
	
	/**
	 * Allows the user to choose from valid TSD files, then loads and parses the file.
	 * @return
	 * @throws ListChooser.EmptyListException
	 */
	private TSDParser prepareTSDFile() throws ListChooser.EmptyListException{
		Vector<String> filenames = new Vector<String>();
		for (String s : new File(reb2sac.getSimPath()).list()) {
			if (s.endsWith(".tsd")) {
				filenames.add(s);
			}
		}
		String filename = ListChooser.selectFromList(biosim.frame(), filenames.toArray(), "Please choose a simulation file");
		if(filename == null)
			return null;
		String fullFilePath = reb2sac.getSimPath() + File.separator + filename;
		TSDParser parser = new TSDParser(fullFilePath, biosim, false);
		
		slider.setMaximum(parser.getNumSamples()-1);
		
		biosim.log.addText(fullFilePath + " loaded. " + 
				String.valueOf(parser.getData().size()) +
				" rows of data loaded.");
		
		return parser;
	}
	
	JButton playPauseButton;
	JButton rewindButton;
	JSlider slider;
	private void addUI(){
		JToolBar mt = new JToolBar();
		
		rewindButton = Utils.makeToolButton("", "rewind", "Rewind", this);
		mt.add(rewindButton);

		playPauseButton = Utils.makeToolButton("", "playpause", "Play", this);
		mt.add(playPauseButton);
		
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		mt.add(slider);

		this.add(mt, BorderLayout.SOUTH);
	}
	
	/**
	 * Called whenever the play/pause button is pressed, or when the system needs to 
	 * pause the movie (such as at the end)
	 */
	private void playPauseButtonPress(){
		if(playPauseButton.getText() == "Play"){
			if(slider.getValue() >= slider.getMaximum()-1)
				slider.setValue(0);
			playTimer.setDelay(FRAME_DELAY_MILLISECONDS);
			playPauseButton.setText("Pause");
			playTimer.start();
		}else{
			playPauseButton.setText("Play");
			playTimer.stop();
		}		
	}
	
	/**
	 * event handler for when UI buttons are pressed.
	 */
	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		String command = event.getActionCommand();
		
		if(command.equals("rewind")){
			slider.setValue(0);
		}else if(command.equals("playpause")){
			playPauseButtonPress();
		}
	}
	
	/**
	 * event handler for when the timer ticks
	 */
	ActionListener playTimerEventHandler = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			slider.setValue(slider.getValue()+1);
			if(slider.getValue() >= slider.getMaximum()){
				playPauseButtonPress();
			}
			updateVisuals();
		}
	};
	
	
	/**
	 * Called when the timer ticks and we need to update the colors/sizes/etc.
	 */
	private void updateVisuals(){
		int pos = slider.getValue();
		if(pos < 0 || pos > parser.getNumSamples()-1){
			throw new Error("Invalid slider value! It is outside the data range!");
		}
		
		HashMap<String, ArrayList<Double>> dataHash = parser.getHashMap();
		
		modelView.beginFrame();
		for(String s:gcm.getSpecies().keySet()){
			if(dataHash.containsKey(s)){
				double value = dataHash.get(s).get(pos);
				modelView.setSpeciesAnimationValue(s, value);
			}
		}
		modelView.endFrame();
		
	}

}
