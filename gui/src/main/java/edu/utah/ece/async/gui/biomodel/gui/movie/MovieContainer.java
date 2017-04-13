/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.gui.biomodel.gui.movie;


import com.google.gson.Gson;
import com.mxgraph.model.mxCell;

import edu.utah.ece.async.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.dataModels.util.GlobalConstants;
import edu.utah.ece.async.dataModels.util.dataparser.DTSDParser;
import edu.utah.ece.async.dataModels.util.dataparser.DataParser;
import edu.utah.ece.async.dataModels.util.dataparser.TSDParser;
import edu.utah.ece.async.gui.ResourceManager;
import edu.utah.ece.async.gui.analysis.AnalysisView;
import edu.utah.ece.async.gui.biomodel.gui.comp.Grid;
import edu.utah.ece.async.gui.biomodel.gui.movie.SerializableScheme;
import edu.utah.ece.async.gui.biomodel.gui.schematic.ListChooser;
import edu.utah.ece.async.gui.biomodel.gui.schematic.ModelEditor;
import edu.utah.ece.async.gui.biomodel.gui.schematic.Schematic;
import edu.utah.ece.async.gui.biomodel.gui.schematic.TreeChooser;
import edu.utah.ece.async.gui.biomodel.gui.schematic.Utils;
import edu.utah.ece.async.gui.main.Gui;
import edu.utah.ece.async.gui.main.util.ExampleFileFilter;
import edu.utah.ece.async.gui.main.util.Utility;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class MovieContainer extends JPanel implements ActionListener {

	public static final String COLOR_PREPEND = "_COLOR";
	public static final String MIN_PREPEND = "_MIN";
	public static final String MAX_PREPEND = "_MAX";
	
	private final String PLAYING = "playing";
	private final String PAUSED = "paused";
	
	private String mode = PLAYING;
	
	public static final int FRAME_DELAY_MILLISECONDS = 20;
	
	private static final long serialVersionUID = 1L;
	
	private Schematic schematic;
	private AnalysisView analysisView;
	private BioModel bioModel;
	private Gui biosim;
	private ModelEditor modelEditor;
	private TSDParser parser;
	private DTSDParser dynamicParser;
	private Timer playTimer;
	private MovieScheme movieScheme;
	
	private boolean isUIInitialized;
	private boolean isDirty = false;
	private String outputFilename = "";
	
	//movie toolbar/UI elements
	private JToolBar movieToolbar;
	private JButton fileButton;
	private JButton playPauseButton;
	private JButton rewindButton;
	private JButton singleStepButton;
	private JButton clearButton;
	private JSlider slider;
	
	private int numTimePoints = 0;
	
	private boolean dynamic = false;
	
	private HashSet<String> previousSpeciesList = new HashSet<String>();
	
	private int originalGridRows = 0;
	private int originalGridCols = 0;
	
	private HashSet<String> totalComponentList = new HashSet<String>();
	
	
	
	/**
	 * constructor
	 * 
	 * @param analysisView
	 * @param bioModel
	 * @param biosim
	 * @param gcm2sbml
	 */
 	public MovieContainer(AnalysisView analysisView, BioModel bioModel, Gui biosim, ModelEditor gcm2sbml, boolean lema) {
		
		super(new BorderLayout());
		
		//JComboBox compartmentList = MySpecies.createCompartmentChoices(gcm);
		schematic = new Schematic(bioModel, biosim, gcm2sbml, false, this, gcm2sbml.getCompartmentPanel(), gcm2sbml.getReactionPanel(), gcm2sbml.getRulePanel(),
		  gcm2sbml.getConstraintPanel(),  gcm2sbml.getEventPanel(), gcm2sbml.getParameterPanel(), lema);
		this.add(schematic, BorderLayout.CENTER);
		
		this.bioModel = bioModel;
		this.biosim = biosim;
		this.analysisView = analysisView;
		this.modelEditor = gcm2sbml;
		this.movieScheme = new MovieScheme();
		this.originalGridRows = bioModel.getGridTable().getNumRows();
		this.originalGridCols = bioModel.getGridTable().getNumCols();
		
		this.playTimer = new Timer(0, playTimerEventHandler);
		mode = PAUSED;
	}
 	
 	public void resetGridToOriginalSize() {
 		
 	  modelEditor.getGrid().resetGrid(originalGridRows, originalGridCols);
 	}
	
	
	//TSD FILE METHODS
	
	/**
	 * returns a vector of strings of TSD filenames within a directory
	 * i don't know why it doesn't return a vector of strings
	 * 
	 * @param directoryName directory for search for files in
	 * @return TSD filenames within the directory
	 */
	private Vector<String> recurseTSDFiles(String directoryName) {
		
		
		Vector<String> filenames = new Vector<String>();
		
		filenames.add(new File(directoryName).getName());
		
		for (String s : new File(directoryName).list()) {
			
			String fullFileName = directoryName + GlobalConstants.separator + s;
			File f = new File(fullFileName);
			
			if (s.endsWith(".tsd") || s.endsWith(".dtsd") && f.isFile()) {
				filenames.add(s);
			}
			else if (f.isDirectory()) {
				filenames.addAll(recurseTSDFiles(fullFileName));
			}
		}
		filenames.sort(new Comparator<String>(){
      @Override
      public int compare(String o1, String o2) {
        return o1.compareTo(o2);
      }});
		return filenames;
	}
	
	/**
	 * opens a treechooser of the TSD files, then loads and parses the selected TSD file
	 * 
	 * @throws ListChooser.EmptyListException
	 */
	private void prepareTSDFile() {
		
		pause();
	
		// if simID is present, go up one directory.
		String simPath = analysisView.getSimPath();
		String simID = analysisView.getSimID();
		
		if (!simID.equals("")) {
			simPath = new File(simPath).getParent();
		}
		
		Vector<String> filenames = recurseTSDFiles(simPath);
		
		String filename;
		
		try {
			filename = TreeChooser.selectFromTree(Gui.frame, filenames, "Choose a simulation file");
		}
		catch (TreeChooser.EmptyTreeException e) {
			
			JOptionPane.showMessageDialog(Gui.frame, "There aren't any simulation files. " +
					"Please simulate then try again.");
			return;
		}
		
		if (filename == null || (filename.contains(".tsd") == false && filename.contains(".dtsd") == false))
			return;
		
		String fullFilePath = analysisView.getRootPath() + filename;
		
		if (fullFilePath.contains(".dtsd")) {
			
			dynamic = true;
			dynamicParser = new DTSDParser(fullFilePath.replace(".tsd", ".dtsd"));
			numTimePoints = dynamicParser.getNumSamples();
		}
		else {
			dynamic = false;
			parser = new TSDParser(fullFilePath, false);		
			numTimePoints = parser.getNumSamples();
		}
		
		slider.setMaximum(numTimePoints - 1);
		slider.setValue(0);
		
//		biosim.log.addText(fullFilePath + " loaded. " + 
//				String.valueOf(parser.getData().size()) +
//				" rows of data loaded.");
		
		loadPreferences();
	}
	
	
	//UI METHODS
	
	/**
	 * displays the schematic and the movie UI
	 */
	public void display() {
		
		schematic.display();

		if (isUIInitialized == false) {
			
			this.addPlayUI();			
			isUIInitialized = true;
		}
	}
	
	private JButton makeToolButton(String iconFilename, String actionCommand, String tooltip, ActionListener listener) {
		URL icon = null;
		URL selectedIcon = null;
		if (!iconFilename.equals("")) {
			icon = ResourceManager.getResource("/icons/modelview/" + iconFilename);
			String selectedPath = iconFilename.replaceAll(".png", "_selected.png");
			selectedIcon = ResourceManager.getResource("/icons/modelview/" + selectedPath);
		}
		return Utils.makeToolButton(icon, selectedIcon, actionCommand, tooltip, listener);
	}
	
	/**
	 * adds the toolbar at the bottom
	 */
	private void addPlayUI() {
		// Add the bottom menu bar
		movieToolbar = new JToolBar();
		
		fileButton = makeToolButton("", "choose_simulation_file", "Choose Simulation", this);
		movieToolbar.add(fileButton);
		
		clearButton = makeToolButton("", "clearAppearances", "Clear Appearances", this);
		movieToolbar.add(clearButton);
		
		movieToolbar.addSeparator();
		
		rewindButton = makeToolButton("movie" + GlobalConstants.separator + "rewind.png", "rewind", "Rewind", this);
		movieToolbar.add(rewindButton);

		singleStepButton = makeToolButton("movie" + GlobalConstants.separator + "single_step.png", "singlestep", "Single Step", this);
		movieToolbar.add(singleStepButton);
		
		playPauseButton = makeToolButton("movie" + GlobalConstants.separator + "play.png", "playpause", "Play", this);
		movieToolbar.add(playPauseButton);
			
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		slider.setSnapToTicks(true);
		movieToolbar.add(slider);
		
		movieToolbar.setFloatable(false);

		this.add(movieToolbar, BorderLayout.SOUTH);
	}
	
	/**
	 * reloads the schematic's grid from file
	 * is called on an analysis view when the normal view/SBML is saved
	 */
	public void reloadGrid() {
		
		schematic.reloadGrid();		
	}
	
	/**
	 * sets up the grid so that it animates dynamically properly
	 */
	public void setupDynamicGrid() {
		
		Point gridSize = new Point(dynamicParser.getNumRows(), dynamicParser.getNumCols());
		modelEditor.getGrid().resetGrid((int) gridSize.getX(), (int) gridSize.getY());
	}
	
	
	//EVENT METHODS
	
	/**
	 * event handler for when UI buttons are pressed.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		if (command.equals("rewind")) {
			
			if (parser == null && dynamicParser == null) {
				JOptionPane.showMessageDialog(Gui.frame, "Must first choose a simulation file.");
			} 
			else {
				slider.setValue(0);
				updateVisuals(true, slider.getValue());
			}
		}
		else if (command.equals("playpause")) {
			if (parser == null && dynamicParser == null) {
				JOptionPane.showMessageDialog(Gui.frame, "Please choose a simulation file.");
			} 
			else {
				playPauseButtonPress();
			}
		}
		else if (command.equals("singlestep")) {
			if (parser == null && dynamicParser == null) {
				JOptionPane.showMessageDialog(Gui.frame, "Please choose a simulation file.");
			} 
			else {
				nextFrame();
			}
		}
		else if (command.equals("choose_simulation_file")) {
			
			prepareTSDFile();
			
			if (dynamic == true)
				setupDynamicGrid();
		}
		else if (command.equals("clearAppearances")) {
			
			movieScheme.clearAppearances();
			
			if (dynamic == true)
				schematic.getGraph().updateGrid();
			else
				schematic.getGraph().buildGraph();
			
			this.setIsDirty(true);
		}
		else{
			throw new Error("Unrecognized command '" + command + "'!");
		}
	}
	
	/**
	 * event handler for when the timer ticks
	 */
	ActionListener playTimerEventHandler = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			nextFrame();
		}
	};
	
	private void setIcon(AbstractButton button, String iconFileName) {
		URL icon = ResourceManager.getResource("/icons/modelview/" + iconFileName);
		Utils.setIcon(button, icon);
	}
	
	//MOVIE CONTROL METHODS
	
	/**
	 * switches between play/pause modes
	 * 
	 * Called whenever the play/pause button is pressed, or when the system needs to 
	 * pause the movie (such as at the end)
	 */
	private void playPauseButtonPress() {
		
		if(mode == PAUSED){
			
			if (slider.getValue() >= slider.getMaximum()-1)
				slider.setValue(0);
			
			playTimer.setDelay(FRAME_DELAY_MILLISECONDS);
			setIcon(playPauseButton, "movie" + GlobalConstants.separator + "pause.png");
			playTimer.start();
			mode = PLAYING;
		}
		else{
			
			setIcon(playPauseButton, "movie" + GlobalConstants.separator + "play.png");
			playTimer.stop();
			mode = PAUSED;
		}		
	}
	
	/**
	 * calls playpausebuttonpress to pause the movie
	 */
	private void pause() {
		
		if (mode == PLAYING)
			playPauseButtonPress();
	}
	
	/**
	 * advances the movie to the next frame
	 */
	private void nextFrame() {
		
		slider.setValue(slider.getValue() + 1);
		
		if (slider.getValue() >= slider.getMaximum())		
			pause();
		
		updateVisuals(true, slider.getValue());
	}
	
	/**
	 * updates the visual appearance of cells on the graph (ie, species, components, etc.)
	 * gets called when the timer ticks
	 */
	private void updateVisuals(boolean refresh, int frameIndex) {
		
		if (parser == null && dynamicParser == null)
			throw new Error("NoSimFileChosen");
		
		if (frameIndex < 0 || frameIndex > numTimePoints - 1)
			throw new Error("Invalid slider value. It's outside the data range.");
		
		HashMap<String, Double> speciesTSData = new HashMap<String, Double>();
		HashSet<String> componentList = new HashSet<String>();
		
		if (dynamic == true) {

			Grid grid = modelEditor.getGrid();
			grid.updateComponentLocations(dynamicParser.getComponentToLocationMap(frameIndex));
			speciesTSData = dynamicParser.getSpeciesToValueMap(frameIndex);
			componentList.addAll(dynamicParser.getComponentToLocationMap(frameIndex).keySet());
			totalComponentList.addAll(dynamicParser.getComponentToLocationMap(frameIndex).keySet());
			
			//update the graph by resetting the grid cells and component cells
			schematic.getGraph().updateGrid();
		}
		//if not dynamic
		else {
		
			speciesTSData = parser.getHashMap(frameIndex);
			
			//find all the components
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getParameterCount(); i++) {
				
				if (bioModel.getSBMLDocument().getModel().getParameter(i).getId().contains("__locations")) {
			
					String[] compIDs = AnnotationUtility.parseArrayAnnotation(bioModel.getSBMLDocument().getModel().getParameter(i));
					
					for (int j = 1; j < compIDs.length; ++j) {
						
						if (compIDs[j].contains("=(")) {							
							componentList.add(compIDs[j].split("=")[0].trim());
						}
					}
				}
				//if there isn't a locations parameter, look at the submodel IDs
				else {
					
					//look through the submodel IDs for component IDs
					for (int j = 0; j < bioModel.getSBMLCompModel().getListOfSubmodels().size(); ++j) {						
						
						String submodelID = bioModel.getSBMLCompModel().getListOfSubmodels().get(j).getId();
						
						if (submodelID.contains("GRID__") == false)
							componentList.add(submodelID);
					}					
				}
			}
			
			//loop through the species and set their appearances
			for (String speciesID : speciesTSData.keySet()) {
				
				if (speciesID.equals("time") || speciesID.contains("__location"))
					continue;
				
				//make sure this species has data in the TSD file
				if (speciesTSData.containsKey(speciesID)) {
					
					MovieAppearance speciesAppearance = null;
					
					//get the species' appearance and send it to the graph for updating
					if (schematic.getGraph().getSpeciesCell(speciesID)!=null &&	speciesTSData.get(speciesID) != null) {
						
						speciesAppearance = 
							movieScheme.createAppearance(speciesID, GlobalConstants.SPECIES, speciesTSData, null);
						
						if (speciesAppearance != null) {
							schematic.getGraph().setSpeciesAnimationValue(speciesID, speciesAppearance);
						}
					} else if (schematic.getGraph().getVariableCell(speciesID)!=null &&	speciesTSData.get(speciesID) != null) {
						mxCell cell = schematic.getGraph().getVariableCell(speciesID);
						if (schematic.getGraph().getCellType(cell).equals(GlobalConstants.PLACE)) {
							speciesAppearance = 
									movieScheme.createAppearance(speciesID, GlobalConstants.PLACE, speciesTSData, null);
							if (speciesAppearance != null) {
								schematic.getGraph().setParameterAnimationValue(speciesID, speciesAppearance);
							}
						} else if (schematic.getGraph().getCellType(cell).equals(GlobalConstants.BOOLEAN)) {
							speciesAppearance = 
									movieScheme.createAppearance(speciesID, GlobalConstants.BOOLEAN, speciesTSData, null);
							if (speciesAppearance != null) {
								schematic.getGraph().setParameterAnimationValue(speciesID, speciesAppearance);
							}
						}
					}
				}
			}
		}
		
		//loop through component IDs and set their appearances
		for (String componentID : componentList) {
		
			//get the component's appearance and send it to the graph for updating
			MovieAppearance compAppearance = 
				movieScheme.createAppearance(componentID, GlobalConstants.COMPONENT, speciesTSData, totalComponentList);
			
			if (compAppearance != null)
				schematic.getGraph().setComponentAnimationValue(componentID, compAppearance);
		}
		
		int numRows = bioModel.getGridTable().getNumRows();
		int numCols = bioModel.getGridTable().getNumCols();
		int minRow = 0;
		int minCol = 0;
		
		if (dynamic == true) {
			
			numRows = dynamicParser.getNumRowsAtFrame(frameIndex);
			numCols = dynamicParser.getNumColsAtFrame(frameIndex);
			minRow = dynamicParser.getMinRowAtFrame(frameIndex);
			minCol = dynamicParser.getMinColAtFrame(frameIndex);
		}
			
		//if there's a grid to set the appearance of
		if (modelEditor.getGrid().isEnabled()) {
			
			//loop through all grid locations and set appearances
			for (int row = minRow; row < numRows + minRow; ++row) {
				for (int col = minCol; col < numCols + minCol ; ++col) {
					
					String gridID = "ROW" + row + "_COL" + col;
					
					//get the component's appearance and send it to the graph for updating
					MovieAppearance gridAppearance = 
						movieScheme.createAppearance(gridID, GlobalConstants.GRID_RECTANGLE, speciesTSData, null);
					
					if (gridAppearance != null)
						schematic.getGraph().setGridRectangleAnimationValue(gridID, gridAppearance);
					else {
						
						if (dynamic == true) {
							for (String species : speciesTSData.keySet()) {
								
								//if this is a new row/col location due to a dynamic model,
								//take its appearance from its neighbor
								if (previousSpeciesList.contains(species) == false && 
										species.contains(gridID + "__")) {
								
									//find a neighbor to take an appearance from
									gridAppearance = 
										movieScheme.getNearestGridAppearance(row, col, species, speciesTSData);
									
									if (gridAppearance != null) {										
										schematic.getGraph().setGridRectangleAnimationValue(gridID, gridAppearance);
									}
								}
							}
						}
					}
				}
			}			
		}
		
		if (refresh)
			schematic.getGraph().refresh();
		
		previousSpeciesList.clear();
		previousSpeciesList.addAll(speciesTSData.keySet());
	}

	/**
	 * creates an Movie using JPG frames of the simulation
	 */
	public void outputMovie(String movieFormat) {
		
		if (parser == null && dynamicParser == null){
			
			JOptionPane.showMessageDialog(Gui.frame, "You must first choose a simulation (tsd) file.");
			return;
		}
		if (movieFormat.equals("mp4"))
			outputFilename = Utility.browse(Gui.frame, null, null, JFileChooser.FILES_ONLY, "Save MP4", -1);
		else if (movieFormat.equals("avi"))
			outputFilename = Utility.browse(Gui.frame, null, null, JFileChooser.FILES_ONLY, "Save AVI", -1);
		
		if (outputFilename == null || outputFilename.length() == 0)
			return;
		
		pause();
		
		int startFrame = 0;
		int endFrame = numTimePoints - 1;
		int skipFrame = 0;
		
		//get the start/end frames from the user
		JPanel tilePanel;
		JTextField startFrameChooser;
		JTextField endFrameChooser;
		JTextField printFrameChooser;
		JCheckBox scaleChooser;
		
		//panel that contains grid size options
		tilePanel = new JPanel(new GridLayout(5, 2));
		this.add(tilePanel, BorderLayout.SOUTH);

		tilePanel.add(new JLabel("Start Frame"));
		startFrameChooser = new JTextField("0");
		tilePanel.add(startFrameChooser);
		
		tilePanel.add(new JLabel("End Frame"));
		endFrameChooser = new JTextField(String.valueOf(numTimePoints - 1));
		tilePanel.add(endFrameChooser);
		
		tilePanel.add(new JLabel("Print Frame"));
		printFrameChooser = new JTextField("0"); 
		tilePanel.add(printFrameChooser);
		
		scaleChooser = new JCheckBox("Scale");
		tilePanel.add(scaleChooser);
		
		String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
		
		boolean error = true;
		
		while (error) {
		
			int okCancel = JOptionPane.showOptionDialog(Gui.frame, tilePanel, "Select Frames",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

			//if the user clicks "ok" on the panel
			if (okCancel == JOptionPane.OK_OPTION) {
				
				startFrame = Integer.valueOf(startFrameChooser.getText());
				endFrame = Integer.valueOf(endFrameChooser.getText());
				skipFrame = Integer.valueOf(printFrameChooser.getText());
				
				if (endFrame < numTimePoints && startFrame >= 0)
					error = false;
			}
		}
		
		//disable all buttons and stuff
		fileButton.setEnabled(false);
		playPauseButton.setEnabled(false);
		rewindButton.setEnabled(false);
		singleStepButton.setEnabled(false);
		clearButton.setEnabled(false);
		slider.setEnabled(false);
		
		//un-zoom so that the frames print properly
		schematic.getGraph().getView().setScale(1.0);
		
		JPanel button = new JPanel();
		JPanel progressPanel = new JPanel(new BorderLayout());
		final JLabel label = new JLabel("Creating movie . . .");
		JPanel frameText = new JPanel();
		JButton cancel = new JButton("Cancel");
		final JFrame progressFrame = new JFrame("Progress");
		
		JProgressBar movieProgress = new JProgressBar(0, 100);
		movieProgress.setStringPainted(true);
		movieProgress.setValue(0);
		frameText.add(label);
		button.add(cancel);
		progressPanel.add(frameText, "North");
		progressPanel.add(movieProgress, "Center");
		progressPanel.add(button, "South");
		
		progressFrame.getContentPane().add(progressPanel);
		progressFrame.setLocationRelativeTo(null);
		progressFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		progressFrame.pack();
		progressFrame.setVisible(true);
		
		MovieProgress printMovieFrames = 
			new MovieProgress(movieProgress, startFrame, endFrame, skipFrame, 
					scaleChooser.isSelected(), movieFormat, progressFrame);
		
		final Thread printThread = new Thread(printMovieFrames);
		
		cancel.addActionListener(new ActionListener() {

			@Override
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent arg0) {
				
				label.setText("Canceling movie");					
				progressFrame.dispose();
				printThread.stop();
				removeJPGs();
				addPlayUI();
			}
		});
		
		printThread.start();
	}
	
	/**
	 * creates a JPG of the current graph frame
	 */
	public void outputJPG(int fileNumber, boolean scale) {
		
		//this prompts to save the current frame somewhere
		if (fileNumber == -1) {
			
			JFileChooser fc = new JFileChooser("Save Schematic");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			ExampleFileFilter jpgFilter = new ExampleFileFilter();
			jpgFilter.addExtension("jpg");
			jpgFilter.setDescription("Image Files");		
			
			fc.addChoosableFileFilter(jpgFilter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(jpgFilter);
			
			int returnVal = fc.showDialog(Gui.frame, "Save Schematic");
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
	            File file = fc.getSelectedFile();
	            schematic.outputFrame(file.getAbsoluteFile().toString(), scale);
	        }
		}
		
		String separator = GlobalConstants.separator;
		
		String filenum = String.format("%09d", fileNumber);			
		schematic.outputFrame(analysisView.getRootPath() + separator + filenum  + ".jpg", scale);
	}
	
	/**
	 * removes all created JPGs
	 */
	private void removeJPGs() {
		
		String separator = GlobalConstants.separator;
		
		//remove all created jpg files
	    for (int jpgNum = 0; jpgNum <= slider.getMaximum(); ++jpgNum) {
	    	
	    	String jpgNumString = String.format("%09d", jpgNum);				    	
	    	String jpgFilename = 
	    		analysisView.getRootPath() + separator + jpgNumString + ".jpg";
		    File jpgFile = new File(jpgFilename);
		    
		    if (jpgFile.exists() && jpgFile.canWrite())
		    	jpgFile.delete();		    	
	    }
	    
	    schematic.setMovieMode(false);
	}
	
	
	//PREFERENCES METHODS
	
	/**
	 * outputs the preferences file
	 */
	public void savePreferences() {

		Gson gson = new Gson();
		String out = gson.toJson(this.movieScheme.getAllSpeciesSchemes());
		
		String fullPath = getPreferencesFullPath();
		
		FileOutputStream fHandle;
		
		try {
			fHandle = new FileOutputStream(fullPath);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "An error occured opening preferences file " + fullPath + "\nmessage: " + e.getMessage());
			return;
		}
		
		try {
			fHandle.write(out.getBytes());
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "An error occured writing the preferences file " + fullPath + "\nmessage: " + e.getMessage());
		}
		
		try {
			fHandle.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "An error occured closing the preferences file " + fullPath + "\nmessage: " + e.getMessage());
			return;
		}
		
		biosim.log.addText("file saved to " + fullPath);
		
		this.modelEditor.saveParams(false, "", true, null);
	}

	/**
	 * loads the preferences file if it exists and stores its values into the movieScheme object.
	 */
	public void loadPreferences() {
		
		// load the prefs file if it exists
		String fullPath = getPreferencesFullPath();
		String json = null;
		
		try {
			json = DataParser.readFileToString(fullPath);
		} 
		catch (IOException e) {
		}
		
		if (json == null) {
			
			if (movieScheme == null || movieScheme.getAllSpeciesSchemes().length == 0)
				movieScheme = new MovieScheme();
		}
		else {
			
			Gson gson = new Gson();
			
			SerializableScheme[] speciesSchemes = null;
			
			try {
				
				speciesSchemes = gson.fromJson(json, SerializableScheme[].class);
				
				//if there's already a scheme, keep it
				if (movieScheme == null || movieScheme.getAllSpeciesSchemes().length == 0) {
					
					movieScheme = new MovieScheme();
					
					if (dynamic == true)
						movieScheme.populate(speciesSchemes, dynamicParser.getSpecies());
					else
						movieScheme.populate(speciesSchemes, parser.getSpecies());
				}
			}
			catch (Exception e) {
				biosim.log.addText("An error occured trying to load the preferences file " + fullPath + " ERROR: " + e.toString());
			}
		}
	}
	

	//GET/SET METHODS
	
	public boolean getIsDirty() {
		return isDirty;
	}
	
	public void setIsDirty(boolean value) {
		isDirty = value;
	}
	
	public boolean getDynamic() {
		return dynamic;
	}
	
	public DTSDParser getDTSDParser() {
		return dynamicParser;
	}
	
	public TSDParser getTSDParser() {
		return parser;
	}

	public ModelEditor getGCM2SBMLEditor() {
		return modelEditor;
	}
	
	private String getPreferencesFullPath() {
		
		String path = analysisView.getSimPath();
		String fullPath = path +  GlobalConstants.separator + "schematic_preferences.json";
		return fullPath;
	}

	public Schematic getSchematic() {
		return schematic;
	}

	public MovieScheme getMovieScheme() {
		return movieScheme;
	}
	
	public BioModel getGCM() {
		return bioModel;
	}

	public int getFrameIndex() {
		return slider.getValue();
	}
	
	
	//MOVIEPROGRESS INNER CLASS
	
	/**
	 * class to allow running the movie file creation in a separate thread
	 */
	private class MovieProgress implements Runnable {
		
		private JProgressBar progressBar;
		private int startFrame, endFrame, printFrame;
		private String movieFormat;
		private JFrame progressFrame;
		private boolean scale;
		
		public MovieProgress(JProgressBar progressBar, 
				int startFrame, int endFrame, int printFrame, boolean scale, 
				String movieFormat, JFrame progressFrame) {
			
			this.progressBar = progressBar;
			this.startFrame = startFrame;
			this.endFrame = endFrame;
			this.printFrame = printFrame;
			this.movieFormat = movieFormat;
			this.progressFrame = progressFrame;
			this.scale = scale;
		}		
		
		@Override
		public void run() {
			
			schematic.setMovieMode(true);
			
			int frameNumber = 0;
			
			//output all frames without updating the schematic's image
			for (int currentFrame = startFrame; currentFrame < endFrame; ++currentFrame) {
				
				if (!(printFrame > 0 && (currentFrame % printFrame == 0)))
					continue;
				
				updateVisuals(false, currentFrame);
				
				if (currentFrame - startFrame == 0)
					frameNumber = currentFrame - startFrame;
				
				++frameNumber;
				
				//frame numbers need to start at 001
				outputJPG(frameNumber, scale);				
				
				progressBar.setValue((100 * (currentFrame - startFrame) / (endFrame - startFrame)));
			}
			
			String separator = GlobalConstants.separator;
			
			String path = "";
			String movieName = "";

			if (outputFilename.contains(separator)) {
				
				path = outputFilename.substring(0, outputFilename.lastIndexOf(separator));
				movieName = outputFilename.substring(outputFilename.lastIndexOf(separator)+1, outputFilename.length());
			}
			
			if (movieName.contains("."))
				movieName = movieName.substring(0, movieName.indexOf("."));
			
			String args = "";
			
			//if we're on windows, add "cmd" to the front of the command line argument
			if (System.getProperty("os.name").contains("Windows"))				
				args += "cmd ";
			
			if (movieFormat.equals("mp4")) {				
				//args for ffmpeg
				args +=
					"ffmpeg " + "-y " +
					"-r " + "5 " +
					"-b " + "5000k " +
					"-i " + analysisView.getRootPath() + separator + "%09d.jpg " +
					path + separator + movieName + ".mp4";
			}		
			else if (movieFormat.equals("avi")) {
				//args for ffmpeg
				args +=
					"ffmpeg " + "-y " +
					"-r " + "5 " +
					"-vcodec " + "copy " +
					"-b " + "5000k " +
					"-i " + analysisView.getRootPath() + separator + "%09d.jpg " +
					path + separator + movieName + ".avi";
			}		
			
			//run ffmpeg to generate the movie file
			try {
				Process p = Runtime.getRuntime().exec(args, null, new File(analysisView.getRootPath()));
				
				String line = "";
				
			    BufferedReader input =
			    	new BufferedReader(new InputStreamReader(p.getErrorStream()));
			    
			    while ((line = input.readLine()) != null) {
			    	biosim.log.addText(line);
			    }				    
			    
			    removeJPGs();    
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			
			//close the frame
			progressFrame.dispose();
			
			//enable the movie controls again
			fileButton.setEnabled(true);
			playPauseButton.setEnabled(true);
			rewindButton.setEnabled(true);
			singleStepButton.setEnabled(true);
			clearButton.setEnabled(true);
			slider.setEnabled(true);
			
			addPlayUI();
			
			schematic.setMovieMode(false);
		}
	}
}