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
package edu.utah.ece.async.ibiosim.gui.synthesisView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.conversion.SBML2SBOL;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLFileManager;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.util.Log;
import edu.utah.ece.async.ibiosim.gui.util.Utility;
import edu.utah.ece.async.ibiosim.synthesis.TechMapping;
import edu.utah.ece.async.ibiosim.synthesis.SBMLTechMapping.SynthesisGraph;
import edu.utah.ece.async.ibiosim.synthesis.SBMLTechMapping.Synthesizer;
import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;

/**
 * This class is reserved for performing technology mapping on the GUI front end for SBML models and SBOL designs.
 * 
 * @author Nicholas Roehner 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SynthesisView extends JTabbedPane implements ActionListener, Runnable {

	private static final long serialVersionUID = 1L;
	private String synthID; 	 // ID of synthesis file
	private String rootFilePath; // Path to the iBioSim project

	private Log log; // Log file used in each iBioSim project
	private JFrame frame;
	private Gui gui;
	private Properties synthProps; //Store fields needed for technology mapping in a property file

	private JTextField specText, tbTextBox;

	private List<String> libFilePaths;
	private JList<String> libList; //The path to all the library files.

	private JScrollPane libScroll;
	private JButton addLibButton, removeLibButton, specButton;
	private JComboBox methodBox;
	private JLabel numSolnsLabel;
	private JTextField numSolnsText;

	/**
	 * Constructor to create the technology mapping for the UI.
	 * 
	 * @param synthID - ID of synthesis file
	 * @param separator - Separator used for specifying path
	 * @param rootFilePath - Path to the iBioSim project
	 * @param log - Log file used in each iBioSim project
	 */
	public SynthesisView(Gui ibioSimGUI, String synthID, String rootFilePath, Log log) 
	{
		this.gui = ibioSimGUI;
		this.synthID = synthID;
		this.rootFilePath = rootFilePath;
		this.log = log;

		new File(rootFilePath + File.separator + synthID).mkdir(); // Create the synthesis directory
		JPanel optionsPanel = constructOptionsPanel(); 
		addTab("Synthesis Options", optionsPanel); 
		getComponentAt(getComponents().length - 1).setName("Synthesis Options"); 
	}

	/**
	 * Create all panels to be displayed in the Synthesis View needed for performing technology mapping.
	 * 
	 * @return The complete panel for Synthesis View 
	 */
	private JPanel constructOptionsPanel() 
	{
		JPanel topPanel = new JPanel();
		JPanel optionsPanel = new JPanel(new BorderLayout());
		JPanel specLibPanel = constructSpecLibPanel();
		JPanel methodPanel = constructMethodPanel();
		topPanel.add(optionsPanel);
		optionsPanel.add(specLibPanel, BorderLayout.NORTH);
		optionsPanel.add(methodPanel, BorderLayout.CENTER);
		return topPanel;
	}

	/**
	 * Construct the technology mapping library gate panel for users to upload the library file(s).
	 * 
	 * @return The library file panel that was constructed for technology mapping.
	 */
	private JPanel constructSpecLibPanel() 
	{
		JPanel specLibPanel = new JPanel();
		JLabel libLabel = new JLabel("Library Files: ");
		JPanel inputPanel = constructSpecLibInputPanel();
		specLibPanel.add(libLabel);
		specLibPanel.add(inputPanel);
		return specLibPanel;
	}

	/**
	 * Construct the scrolling text box where the names of the gate library files are loaded in.
	 * 
	 * @return The panel of the scrolling text box to store the gate library files
	 */
	private JPanel constructSpecLibInputPanel() 
	{
		JPanel designPanel = new JPanel(new BorderLayout());
		JPanel inputPanel = new JPanel(new BorderLayout());
		JPanel specPanel = constructSpecPanel();
		designPanel.add(specPanel, BorderLayout.NORTH);

		libScroll = new JScrollPane();
		libScroll.setPreferredSize(new Dimension(276, 55));

		JPanel buttonPanel = constructLibButtonPanel();
		inputPanel.add(designPanel, BorderLayout.NORTH);

		inputPanel.add(libScroll, BorderLayout.WEST);
		inputPanel.add(buttonPanel, BorderLayout.SOUTH);
		return inputPanel;
	}

	/**
	 * Create the specification panel for technology mapping.
	 * @return The specification panel.
	 */
	private JPanel constructSpecPanel() {
		JPanel specPanel = createLabeledPanel("Specification File:");
		specText = new JTextField();
		specText.setEditable(false);
		specPanel.add(specText);

		return specPanel;
	}

	private JPanel createLabeledPanel(String panelName) {
		JPanel panel = new JPanel();
		JLabel panelLabel = new JLabel(panelName);
		panel.add(panelLabel);
		return panel;
	}

	/**
	 * Create buttons to add and remove gate library files.
	 * @return the button panel for the gate library
	 */
	private JPanel constructLibButtonPanel() {
		JPanel buttonContainer = new JPanel();

		addLibButton = new JButton("Add");
		removeLibButton = new JButton("Remove");
		addLibButton.addActionListener(this);
		removeLibButton.addActionListener(this);
		buttonContainer.add(addLibButton);
		buttonContainer.add(removeLibButton);

		return buttonContainer;
	}

	/**
	 * Create the supported technology mapping algorithms panel.
	 * @return The technology mapping algorithms panel
	 */
	private JPanel constructMethodPanel() 
	{
		JPanel topPanel = new JPanel();
		JPanel methodPanel = new JPanel(new BorderLayout());
		JPanel labelPanel = constructMethodLabelPanel();
		JPanel inputPanel = constructMethodInputPanel();
		topPanel.add(methodPanel);
		methodPanel.add(labelPanel, BorderLayout.WEST);
		methodPanel.add(inputPanel, BorderLayout.CENTER);
		return topPanel;
	}



	/**
	 * Set method label in Synthesis View
	 * @return The label panel for the synthesis method.
	 */
	private JPanel constructMethodLabelPanel() {
		JPanel labelPanel = new JPanel(new GridLayout(2, 1));
		labelPanel.add(new JLabel("Synthesis Method:  "));
		numSolnsLabel = new JLabel("Number of Solutions:  ");
		labelPanel.add(numSolnsLabel);
		return labelPanel;
	}

	/**
	 * Set method input that user selected from Synthesis View
	 * @return The method panel
	 */
	private JPanel constructMethodInputPanel() {
		JPanel inputPanel = new JPanel(new GridLayout(2, 1));
		methodBox = new JComboBox(GlobalConstants.SBOL_SYNTH_STRUCTURAL_METHODS.split(","));
		methodBox.addActionListener(this);
		numSolnsText = new JTextField(39); 
		numSolnsText.addActionListener(this);
		inputPanel.add(methodBox);
		inputPanel.add(numSolnsText);
		return inputPanel;
	}

	/**
	 * Load default fields for Synthesis View Window
	 * @param specFileID - ID of specification file.
	 */
	public void loadDefaultSynthesisProperties(String specFileID) {
		synthProps = TechMapping.createDefaultSynthesisProperties(specFileID);
		saveSynthesisProperties();
		loadSynthesisOptions();
	}



	/**
	 * Save the fields needed for SBML Technology mapping in the property file
	 */
	private void saveSynthesisProperties() 
	{
		String propFilePath = rootFilePath + File.separator + synthID + File.separator + synthID 
				+ GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION;
		log.addText("Creating properties file:\n" + propFilePath + "\n");
		try 
		{
			FileOutputStream propStreamOut = new FileOutputStream(new File(propFilePath));
			synthProps.store(propStreamOut, synthID + " SBOL Synthesis Properties");
			propStreamOut.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, 
					"Unable to write SBML Technology Mapping property file to the specified " + propFilePath + ".",
					"File Not Created",
					JOptionPane.ERROR_MESSAGE);
		} 
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(Gui.frame, 
					"Unable to create SBML Technology Mapping property file.",
					"File Not Created",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void loadSynthesisProperties(Properties synthProps) {
		this.synthProps = synthProps;
		loadSynthesisOptions();
	}

	/**
	 * Load fields stored in property file to the Synthesis View panel.
	 */
	private void loadSynthesisOptions() 
	{
		specText.setText(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY));
		libFilePaths = new LinkedList<String>();
		for (String libFilePath : synthProps.getProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY).split(","))
			if (libFilePath.length() > 0)
				libFilePaths.add(libFilePath);
		libList = new JList<String>();
		updateLibraryFiles();
		libScroll.setViewportView(libList);
		methodBox.setSelectedItem(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY));
		numSolnsText.setText(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY));
	}

	private boolean isValidSynthesisFile(String fileFullPath) {
		return fileFullPath.endsWith(GlobalConstants.SBOL_FILE_EXTENSION) ||
				fileFullPath.endsWith(GlobalConstants.XML_FILE_EXTENSION) ||
				fileFullPath.endsWith(GlobalConstants.VERILOG_FILE_EXTENTION);	
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == addLibButton)
		{
			addLibraryFile(libList.getSelectedIndex());
		}
		else if (e.getSource() == removeLibButton)
		{
			removeLibraryFiles(libList.getSelectedIndices());
		}
		else if(e.getSource() == specButton) {
			String selectedFilePath = openFileBrowser(JFileChooser.FILES_ONLY);
			if(!isValidSynthesisFile(selectedFilePath)) {
				JOptionPane.showMessageDialog(frame, "You can only select SBML or SBOL files.", "Invalid File", JOptionPane.WARNING_MESSAGE);
				return;
			}

			specText.setText(selectedFilePath);
		}
		else if (e.getSource() == methodBox)
		{
			toggleMethodSettings();
		}

	}


	private void toggleMethodSettings() {
		if (methodBox.getSelectedItem().toString().equals(GlobalConstants.SBOL_SYNTH_EXHAUST_BB)) 
			numSolnsText.setText("1");
	}

	private String openFileBrowser(int fileChooserType) {
		File startDirectory = new File(Preferences.userRoot().get("biosim.general.project_dir", ""));
		String selectedFilePath = Utility.browse(frame, startDirectory, null, fileChooserType, "Select", -1);
		return selectedFilePath;
	}

	/**
	 * 
	 * @param addIndex - The index location that was returned from file browser dialog to retrieve gate library file.
	 */
	private void addLibraryFile(int addIndex) 
	{
		String libFilePath = openFileBrowser(JFileChooser.FILES_AND_DIRECTORIES);
		if (libFilePath.length() > 0 && !libFilePaths.contains(libFilePath)) 
		{
			if (addIndex >= 0)
			{
				libFilePaths.add(addIndex, libFilePath);
			}
			else
			{
				libFilePaths.add(libFilePath);
			}
			updateLibraryFiles();
		}
	}

	private void removeLibraryFiles(int[] removeIndices) {
		if (removeIndices.length > 0)
			for (int i = removeIndices.length - 1; i >=0; i--)
				libFilePaths.remove(removeIndices[i]);
		else
			libFilePaths.remove(libFilePaths.size() - 1);
		updateLibraryFiles();
	}

	/**
	 * Retrieve the path for all the gate library files and store them in the global variable liblist.
	 */
	private void updateLibraryFiles() 
	{
		//NOTE: go through all file path in libFilePaths and add to libList
		String[] libListData = new String[libFilePaths.size()];
		for (int i = 0; i < libListData.length; i++) {
			String[] splitPath = libFilePaths.get(i).split(File.separator);
			libListData[i] = splitPath[splitPath.length - 1];
		}
		libList.setListData(libListData);
	}

	@Override
	public void run() { 

	}

	/**
	 * Perform SBML Technology Mapping
	 * @param synthFilePath - Path to the synthesis file attached to the specification SBML document to perform technology mapping
	 * @return
	 */
	public List<String> run(String synthFilePath) 
	{
		try
		{
			//Find all .sbol files and store into a collection of files (SBOLFileManager)
			Set<String> sbolFilePaths = new HashSet<String>();
			for (String libFilePath : libFilePaths) 
				for (String fileID : new File(libFilePath).list())
					if (fileID.endsWith(".sbol"))
						sbolFilePaths.add(libFilePath + File.separator + fileID);

			SBOLFileManager fileManager = new SBOLFileManager(sbolFilePaths, SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());


			Set<SynthesisGraph> graphlibrary = new HashSet<SynthesisGraph>();
			//boolean flatImport = libFilePaths.size() > 1;

			/* Find .xml files for the available library of gates and create a bioModel for each file then load 
			 * them to the synthesis graph library.
			 * This is where the loading of the SBML library takes place. 
			 */
			for (String libFilePath : libFilePaths) 
			{
				for (String gateFileID : new File(libFilePath).list()) 
				{
					if (gateFileID.endsWith(".xml")) 
					{
						BioModel gateModel = new BioModel(libFilePath);
						try 
						{
							gateModel.load(gateFileID);
						} 
						catch (XMLStreamException e) 
						{
							JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
						catch (BioSimException e) {
							JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
									JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
						graphlibrary.add(new SynthesisGraph(gateModel, fileManager));
					}
				}
			}
			//Load synthProps that has synthesis.spec as property into the biomodel
			BioModel specModel = new BioModel(rootFilePath); 
			try 
			{
				specModel.load(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY));
			}
			catch (XMLStreamException e) 
			{
				JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} 
			catch (BioSimException e) 
			{
				JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}

			SynthesisGraph spec = new SynthesisGraph(specModel, fileManager); //NOTE: load the SBML library file


			Synthesizer synthesizer = new Synthesizer(graphlibrary, synthProps);
			List<List<SynthesisGraph>> solutions = synthesizer.mapSpecification(spec);
			List<String> solutionFileIDs;

			solutionFileIDs = importSolutions(solutions, spec, fileManager, synthFilePath);
			return solutionFileIDs;
		}
		catch (SBOLValidationException e) 
		{
			JOptionPane.showMessageDialog(Gui.frame, "One or more of the input file(s) are invalid SBOL files.", "Invalid SBOL",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return null;
		} 
		catch (SBOLException e) 
		{
			JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), 
					e.getTitle(), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} 
		catch (FileNotFoundException e) 
		{
			JOptionPane.showMessageDialog(Gui.frame, "Unable to locate input file(s).", "File Not Found",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(Gui.frame, "Unable to read or write SBOL file", "File Not Found",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} 
		catch (SBOLConversionException e) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to convert input file to SBOL data model.", "Failed SBOL Conversion",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		return null;
	}


	private List<String> importSolutions(List<List<SynthesisGraph>> solutions, SynthesisGraph spec, 
			SBOLFileManager fileManager, String synthFilePath) throws SBOLValidationException {
		List<BioModel> solutionModels = new LinkedList<BioModel>();
		Set<String> solutionFileIDs = new HashSet<String>();
		for (List<SynthesisGraph> solutionGraphs : solutions) {
			solutionFileIDs.addAll(importSolutionSubModels(solutionGraphs, synthFilePath));
			solutionFileIDs.add(importSolutionDNAComponents(solutionGraphs, fileManager, synthFilePath));
		}
		int idIndex = 0;
		for (List<SynthesisGraph> solutionGraphs : solutions) {
			System.out.println(solutionGraphs.toString());
			BioModel solutionModel = new BioModel(synthFilePath);
			solutionModel.createSBMLDocument("tempID_" + idIndex, false, false);	
			idIndex++;
			try {
				Synthesizer.composeSolutionModel(solutionGraphs, spec, solutionModel);
			} catch (XMLStreamException e) {
				JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			catch (BioSimException e) {
				JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			solutionModels.add(solutionModel);
		}
		List<String> orderedSolnFileIDs = new LinkedList<String>();
		idIndex = 0;
		for (BioModel solutionModel : solutionModels) {
			String solutionID = spec.getModelFileID().replace(".xml", "");
			do {
				idIndex++;
			} while (solutionFileIDs.contains(solutionID + "_" + idIndex + ".xml"));
			solutionModel.setSBMLFile(solutionID + "_" + idIndex + ".xml");
			solutionModel.getSBMLDocument().getModel().setId(solutionID + "_" + idIndex);
			try {
				solutionModel.save(synthFilePath + File.separator + solutionID + "_" + idIndex + ".xml");
			} catch (XMLStreamException e) {
				JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			catch (BioSimException e) {
				JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			orderedSolnFileIDs.add(solutionID + "_" + idIndex + ".xml");
		}
		orderedSolnFileIDs.addAll(solutionFileIDs);
		return orderedSolnFileIDs;
	}

	private Set<String> importSolutionSubModels(List<SynthesisGraph> solutionGraphs, 
			String synthFilePath) {
		HashMap<String, SynthesisGraph> solutionFileToGraph = new HashMap<String, SynthesisGraph>();
		Set<String> clashingFileIDs = new HashSet<String>();
		for (SynthesisGraph solutionGraph : solutionGraphs) {
			BioModel solutionSubModel = new BioModel(solutionGraph.getProjectPath());
			try {
				solutionSubModel.load(solutionGraph.getModelFileID());
			} catch (XMLStreamException e) {
				JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			catch (BioSimException e) {
				JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			if (solutionFileToGraph.containsKey(solutionGraph.getModelFileID())) {
				SynthesisGraph clashingGraph = solutionFileToGraph.get(solutionGraph.getModelFileID());
				BioModel clashingSubModel = new BioModel(clashingGraph.getProjectPath());
				try {
					clashingSubModel.load(clashingGraph.getModelFileID());
				} catch (XMLStreamException e) {
					JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				catch (BioSimException e) {
					JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				if (!TechMapping.compareModels(solutionSubModel, clashingSubModel)) {
					clashingFileIDs.add(solutionGraph.getModelFileID());
					solutionFileToGraph.remove(solutionGraph.getModelFileID());
					solutionFileToGraph.put(flattenProjectIntoModelFileID(solutionSubModel, solutionFileToGraph.keySet()), 
							solutionGraph);
					solutionFileToGraph.put(flattenProjectIntoModelFileID(clashingSubModel, solutionFileToGraph.keySet()), 
							clashingGraph);
				}
			} else if (clashingFileIDs.contains(solutionGraph.getModelFileID())) 
				solutionFileToGraph.put(flattenProjectIntoModelFileID(solutionSubModel, solutionFileToGraph.keySet()), 
						solutionGraph);
			else
				solutionFileToGraph.put(solutionGraph.getModelFileID(), solutionGraph);
		}
		try {
			for (String subModelFileID : solutionFileToGraph.keySet()) {
				SynthesisGraph solutionGraph = solutionFileToGraph.get(subModelFileID);
				BioModel solutionSubModel = new BioModel(solutionGraph.getProjectPath());
				solutionSubModel.load(solutionGraph.getModelFileID());
				solutionSubModel.getSBMLDocument().getModel().setId(subModelFileID.replace(".xml", ""));
				solutionSubModel.save(synthFilePath + File.separator + subModelFileID);
				solutionGraph.setModelFileID(subModelFileID);
			}
		} catch (XMLStreamException e) {
			JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		catch (BioSimException e) {
			JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		return solutionFileToGraph.keySet();
	}

	private String flattenProjectIntoModelFileID(BioModel biomodel, Set<String> modelFileIDs) {
		String[] splitPath = biomodel.getPath().split(File.separator);
		String flatModelFileID = splitPath[splitPath.length - 1] + "_" + biomodel.getSBMLFile();
		int fileIndex = 0;
		while (modelFileIDs.contains(flatModelFileID)) {
			fileIndex++;
			flatModelFileID = splitPath[splitPath.length - 1] + "_" + biomodel.getSBMLFile().replace(".xml", "") 
					+ "_" + fileIndex + ".xml";
		}
		return splitPath[splitPath.length - 1] + "_" + biomodel.getSBMLFile();
	}

	private String importSolutionDNAComponents(List<SynthesisGraph> solutionGraphs, SBOLFileManager fileManager, 
			String synthFilePath) throws SBOLValidationException {
		Set<URI> compURIs = new HashSet<URI>();
		for (SynthesisGraph solutionGraph : solutionGraphs) {
			for (URI compURI : solutionGraph.getCompURIs())
				if (!compURIs.contains(compURI))
					compURIs.add(compURI);
		}
		String sbolFileID = getSpecFileID().replace(".xml", GlobalConstants.SBOL_FILE_EXTENSION);
		try {
			SBOLFileManager.saveDNAComponents(fileManager.resolveURIs(new LinkedList<URI>(compURIs)), 
					synthFilePath + File.separator + sbolFileID);
			return sbolFileID;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SBOLException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), 
					e.getTitle(), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (SBOLConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void save() {
		Set<Integer> saveIndices = new HashSet<Integer>();
		for (int i = 0; i < getTabCount(); i++)
			saveIndices.add(i);
		saveTabs(saveIndices);
	}

	public void saveTabs(Set<Integer> tabIndices) {
		if (tabIndices.size() > 0) {
			updateSynthesisProperties(tabIndices);
			saveSynthesisProperties();
		}
	}

	public void changeSpecFile(String specFileID) {
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY, specFileID);
		saveSynthesisProperties();
	}

	private void updateSynthesisProperties(Set<Integer> tabIndices) {
		for (int tabIndex : tabIndices)
			if (tabIndex == 0) {
				String libFileProp = "";
				for (String libFilePath : libFilePaths)
					libFileProp = libFileProp + libFilePath + ",";
				if (libFileProp.length() > 0)
					libFileProp = libFileProp.substring(0, libFileProp.length() - 1);
				synthProps.setProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY, 
						libFileProp);
				synthProps.setProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY, 
						methodBox.getSelectedItem().toString());
				if (Integer.parseInt(numSolnsText.getText()) <= 0)
					numSolnsText.setText("0");
				synthProps.setProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY, numSolnsText.getText());
			}
	}

	public boolean tabChanged(int tabIndex) {
		if (tabIndex == 0) {
			return (libsChanged() || methodChanged() || numSolnsChanged());
		}
		return false;
	}

	private boolean libsChanged() {
		List<String> prevLibFilePaths = new LinkedList<String>();
		for (String prevLibFilePath : synthProps.getProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY).split(","))
			if (prevLibFilePath.length() > 0)
				prevLibFilePaths.add(prevLibFilePath);
		if (prevLibFilePaths.size() != libFilePaths.size())
			return true;
		for (String prevLibFilePath : prevLibFilePaths)
			if (!libFilePaths.contains(prevLibFilePath))
				return true;
		return false;
	}

	private boolean methodChanged() {
		String currentMethod = methodBox.getSelectedItem().toString();
		String previousMethod = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY);
		return !currentMethod.equals(previousMethod);
	}

	private boolean numSolnsChanged() {
		int currentNumSolns = Integer.parseInt(numSolnsText.getText());
		int previousNumSolns = Integer.parseInt(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY));
		return currentNumSolns != previousNumSolns;
	}

	public String getSpecFileID() {
		return specText.getText();
	}

	public String getRootDirectory() {
		return rootFilePath;
	}

	public void renameView(String synthID) {
		this.synthID = synthID;
	}

}
