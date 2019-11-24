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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.util.Log;
import edu.utah.ece.async.ibiosim.gui.util.Utility;
import edu.utah.ece.async.ibiosim.synthesis.Synthesis;
import edu.utah.ece.async.ibiosim.synthesis.YosysScriptGenerator;
import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Cover;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Match;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.PreSelectedMatch;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLNetList;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapSolution;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapUtility;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.CompilerOptions;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogCompilerException;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogParser;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogToLPNCompiler;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogToSBOL;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.WrappedSBOL;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.lema.verification.lpn.LPN;

/**
 * This class is reserved for performing technology mapping on the GUI front end for Verilog designs.
 * 
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VerilogSynthesisView extends JTabbedPane implements ActionListener, Runnable {

	private static final long serialVersionUID = 1L;
	private String synthID; 	 // ID of synthesis file
	private String rootFilePath; // Path to the iBioSim project

	private Log log; 
	private JFrame frame;
	private Gui gui;
	private Properties synthProps; //Store fields needed for technology mapping in a property file
	private String verilogSpecPath, synthDirPath;

	private JTextField specTextBox, testEnvTextBox;
	private List<String> libFilePaths;
	private JList<String> libList; 
	private JScrollPane libScroll;
	private JButton addLibButton, removeLibButton, testEnvButton;
	private JComboBox<String> coverAlgOptBox, atacsAlgBox;
	private JTextField numSolnsText;
	private JRadioButton yosysNandDecomp_button, yosysNorDecomp_button;
	private JCheckBox atacs_checkbox;
	
	private DecomposedGraph specGraph;

	/**
	 * Constructor to create the technology mapping for the UI.
	 * 
	 * @param synthID - ID of synthesis file
	 * @param separator - Separator used for specifying path
	 * @param rootFilePath - Path to the iBioSim project
	 * @param log - Log file used in each iBioSim project
	 */
	public VerilogSynthesisView(Gui ibioSimGUI, String synthID, String rootFilePath, Log log, String verilogSpecPath) 
	{
		this.gui = ibioSimGUI;
		this.synthID = synthID;
		this.rootFilePath = rootFilePath;
		this.log = log;
		this.verilogSpecPath = verilogSpecPath;
		new File(rootFilePath + File.separator + synthID).mkdir(); // Create the synthesis directory
		this.synthDirPath = rootFilePath + File.separator + synthID;

		int textBoxLength = 20;
		JLabel specFileLabel 			= new JLabel("Specification File:");
		JLabel specDecompTypeLabel 	= new JLabel("Specification Decomposition Type:");
		JLabel testEnvFileLabel 		= new JLabel("Testing Environment File:");
		JLabel libLabel 			= new JLabel("Library Gate Files:");
		JLabel coverMethodLabel 	= new JLabel("Covering Method:");
		JLabel numOfSolLabel 		= new JLabel("# of Solutions:");
//		JLabel preselectNode = new JLabel("Node Assignment:");

		JPanel verilogSynthPanel = new JPanel();

		//----- Test Env Area -----
		testEnvTextBox = new JTextField(textBoxLength);
		testEnvTextBox.setEnabled(true);
		testEnvButton = new JButton("Browse...");
		testEnvButton.addActionListener(this);

		//----- Specification Area -----
		specTextBox = new JTextField(synthID, textBoxLength);
		specTextBox.setEnabled(false);

		JPanel yosysOptPanel = new JPanel(new GridLayout(1, 2)); 

		yosysNandDecomp_button = new JRadioButton("NAND Logic", true);
		yosysNorDecomp_button = new JRadioButton("NOR Logic", false);
		yosysNandDecomp_button.addActionListener(this);
		yosysNorDecomp_button.addActionListener(this);
		yosysOptPanel.add(yosysNandDecomp_button);
		yosysOptPanel.add(yosysNorDecomp_button);

		atacs_checkbox = new JCheckBox("ATACS Synthesis", false);
		atacs_checkbox.addActionListener(this);
		atacsAlgBox = new JComboBox<String>(new String[] {GlobalConstants.SBOL_SYNTH_ATACS_ATOMIC_GATES, GlobalConstants.SBOL_SYNTH_ATACS_GC_GATES});
		atacsAlgBox.setEnabled(false);
		atacsAlgBox.addActionListener(this);

		//----- Covering Method Area -----
		coverAlgOptBox = new JComboBox<String>(new String[] {GlobalConstants.SBOL_SYNTH_GREEDY, GlobalConstants.SBOL_SYNTH_EXHAUSTIVE, GlobalConstants.SBOL_SYNTH_EXHAUST_BB});
		coverAlgOptBox.addActionListener(this);
		numSolnsText = new JTextField(5); 
		numSolnsText.addActionListener(this);

		//----- Library  Area -----
		libScroll = new JScrollPane();
		libScroll.setPreferredSize(new Dimension(500, 70));
		addLibButton = new JButton("Add");
		addLibButton.addActionListener(this);
		removeLibButton = new JButton("Remove");
		removeLibButton.addActionListener(this);

		//----- Preselection  Area -----
//		String[] columnNames = {"Node ID", "Assigned CD"};
//
//		String[][] data = new String[1][1];
//		
//		JTable table = new JTable(data, columnNames);
//		table.setPreferredScrollableViewportSize(new Dimension(200, 150));
//		table.setFillsViewportHeight(true);
//		JScrollPane scrollPane = new JScrollPane(table);

		//----- Layout Area -----
		JPanel p1 = new JPanel(); 
		p1.setLayout(new GridBagLayout()); 
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		p1.add(testEnvFileLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		p1.add(testEnvTextBox, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 2;
		c.gridy = 0;
		p1.add(testEnvButton, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		p1.add(specFileLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 1;
		p1.add(specTextBox, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 2;
		p1.add(specDecompTypeLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 2;
		p1.add(yosysOptPanel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 2;
		c.gridy = 2;
		p1.add(atacs_checkbox, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 3;
		c.gridy = 2;
		p1.add(atacsAlgBox, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 3;
		p1.add(coverMethodLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 3;
		p1.add(coverAlgOptBox, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 2;
		c.gridy = 3;
		numOfSolLabel.setHorizontalAlignment(CENTER);
		p1.add(numOfSolLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 3;
		c.gridy = 3;
		p1.add(numSolnsText, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 4;
		c.insets = new Insets(20,0,0,0);
		p1.add(libLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 2;
		c.gridy = 4;
		c.insets = new Insets(20,0,0,0);
		p1.add(addLibButton, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 3;
		c.gridy = 4;
		c.insets = new Insets(20,0,0,0);
		p1.add(removeLibButton, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.2;
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 4;
		p1.add(libScroll, c);

//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.weightx = 0.2;
//		c.gridx = 0;
//		c.gridy = 6;
//		p1.add(preselectNode, c);

//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.weightx = 0.2;
//		c.gridx = 0;
//		c.gridy = 7;
//		c.gridwidth = 4;
//		p1.add(scrollPane, c);

		verilogSynthPanel.add("North", p1);

		addTab("Verilog Synthesis Options", verilogSynthPanel); 
		getComponentAt(getComponents().length - 1).setName("Verilog Synthesis Options"); 
	}


	/**
	 * Load default fields for Synthesis View Window
	 * @param specFileId - ID of specification file.
	 */
	public void loadDefaultSynthesisProperties(String specFileId) {
		Preferences prefs = Preferences.userRoot();

		synthProps = new Properties();
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY, specFileId);
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_SPEC_PATH_PROPERTY, this.verilogSpecPath);
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY, prefs.get(GlobalConstants.SBOL_SYNTH_LIBS_PREFERENCE, ""));
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY, prefs.get(GlobalConstants.SBOL_SYNTH_METHOD_PREFERENCE, GlobalConstants.SBOL_SYNTH_EXHAUST_BB));
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY, prefs.get(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PREFERENCE, "1"));

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
					"Unable to locate the following SBOL Technology Mapping property file:" + propFilePath + ".",
					"File Not Found",
					JOptionPane.ERROR_MESSAGE);
		} 
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(Gui.frame, 
					"Unable to write to SBOL Technology Mapping property file.",
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
		if(synthProps.containsKey(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY)) {
			specTextBox.setText(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY));
		}
		if(synthProps.containsKey(GlobalConstants.SBOL_SYNTH_TESTBENCH_PROPERTY)) {
			testEnvTextBox.setEnabled(true);
			testEnvTextBox.setText(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_TESTBENCH_PROPERTY));
		}
		if(synthProps.containsKey(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY)) {
			libFilePaths = new LinkedList<String>();
			for (String libFilePath : synthProps.getProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY).split(",")) {
				if (libFilePath.length() > 0) {
					libFilePaths.add(libFilePath);
				}
			}
			libList = new JList<String>();
			updateLibraryFiles();
			libScroll.setViewportView(libList);
		}
		if(synthProps.containsKey(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY)) {
			coverAlgOptBox.setSelectedItem(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY));
		}
		if(synthProps.containsKey(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY)) {
			numSolnsText.setText(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY));
		}
		if(synthProps.containsKey(GlobalConstants.SBOL_SYNTH_ATACS_PROPERTY)) {
			atacs_checkbox.setSelected(true);
			atacsAlgBox.setEnabled(true);
			atacsAlgBox.setSelectedItem(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_ATACS_PROPERTY));
		}
		if(synthProps.containsKey(GlobalConstants.SBOL_SYNTH_YOSYS_PROPERTY)) {
			switch(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_YOSYS_PROPERTY)) {
			case GlobalConstants.SBOL_SYNTH_YOSYS_NAND:
				yosysNandDecomp_button.setSelected(true);
				yosysNorDecomp_button.setSelected(false);
				break;
			case GlobalConstants.SBOL_SYNTH_YOSYS_NOR:
				yosysNorDecomp_button.setSelected(true);
				yosysNandDecomp_button.setSelected(false);
				break;
			default:
				yosysNandDecomp_button.setSelected(false);
				yosysNorDecomp_button.setSelected(false);
				break;
			}
		}


	}

	private boolean isValidSynthesisFile(String fileFullPath) {
		return fileFullPath.endsWith(GlobalConstants.VERILOG_FILE_EXTENTION);	
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
		else if(e.getSource() == testEnvButton) {
			String selectedFilePath = openFileBrowser(JFileChooser.FILES_ONLY);
			if(!isValidSynthesisFile(selectedFilePath)) {
				JOptionPane.showMessageDialog(frame, "You can only select Verilog files.", "Invalid File", JOptionPane.WARNING_MESSAGE);
				return;
			}
			testEnvTextBox.setText(selectedFilePath);
		}
		else if (e.getSource() == coverAlgOptBox)
		{
			toggleMethodSettings();
		}
		else if(e.getSource() == yosysNandDecomp_button)
		{
			if(yosysNandDecomp_button.isSelected()) {
				yosysNorDecomp_button.setSelected(false);
			}
			else {
				yosysNorDecomp_button.setSelected(true);
			}
		}
		else if(e.getSource() == yosysNorDecomp_button)
		{
			if(yosysNorDecomp_button.isSelected()) {
				yosysNandDecomp_button.setSelected(false);
			}
			else {
				yosysNandDecomp_button.setSelected(true);
			}
		}
		else if(e.getSource() == atacs_checkbox)
		{
			if(atacs_checkbox.isSelected()) {
				atacsAlgBox.setEnabled(true);
			}
			else {
				atacsAlgBox.setEnabled(false);
			}
		}
	}

	private void toggleMethodSettings() {
		if (coverAlgOptBox.getSelectedItem().toString().equals(GlobalConstants.SBOL_SYNTH_EXHAUST_BB)) 
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


	public List<String> run(String synthFilePath) 
	{
		List<String> techmapSolPath = new ArrayList<>();
		Synthesis synthesizer = new Synthesis();
		VerilogParser verilogParser = new VerilogParser();
		CompilerOptions compilerOptions = new CompilerOptions();
		try {
			File specFile = compilerOptions.addVerilogFile(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PATH_PROPERTY));
			VerilogModule specVerilogModule = verilogParser.parseVerilogFile(specFile);
			VerilogModule testEnvVerilogModule = null;
			if(synthProps.containsKey(GlobalConstants.SBOL_SYNTH_TESTBENCH_PROPERTY)) {
				File testEnvFile = compilerOptions.addVerilogFile(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_TESTBENCH_PROPERTY));
				testEnvVerilogModule = verilogParser.parseVerilogFile(testEnvFile);
				compilerOptions.setTestbenchModuleId(testEnvVerilogModule.getModuleId());

			}
			compilerOptions.setImplementationModuleId(specVerilogModule.getModuleId());
			compilerOptions.setOutputDirectory(synthDirPath);
			String outputFileName = synthID.endsWith(".v")? synthID.replace(".v", "") : synthID;
			compilerOptions.setOutputFileName(outputFileName);

			String yosysInputPath = specFile.getAbsolutePath();
			if(atacs_checkbox.isSelected()) {
				VerilogToLPNCompiler sbmlLPNConverter = new VerilogToLPNCompiler();
				sbmlLPNConverter.addVerilog(specVerilogModule);
				sbmlLPNConverter.addVerilog(testEnvVerilogModule);
				LPN lpn = sbmlLPNConverter.compileToLPN(specVerilogModule, testEnvVerilogModule, compilerOptions.getOutputDirectory());
				String lpnPath = compilerOptions.getOutputDirectory() + File.separator + compilerOptions.getOutputFileName()  + "_synthesized.lpn";
				lpn.save(lpnPath);
				if(atacsAlgBox.getSelectedItem().toString().equals(GlobalConstants.SBOL_SYNTH_ATACS_ATOMIC_GATES))
				{
					synthesizer.runAtomicGateSynthesis(lpnPath, compilerOptions.getOutputDirectory());
				}
				else if(atacsAlgBox.getSelectedItem().toString().equals(GlobalConstants.SBOL_SYNTH_ATACS_GC_GATES)){
					synthesizer.runGeneralizedCGateSynthesis(lpnPath, compilerOptions.getOutputDirectory());
				}
				yosysInputPath = compilerOptions.getOutputDirectory() + File.separator + compilerOptions.getOutputFileName()  + "_synthesized.v";
			}


			YosysScriptGenerator yosysScript = new YosysScriptGenerator(compilerOptions.getOutputDirectory(), compilerOptions.getOutputFileName() + "_decomposed");
			yosysScript.read_verilog(yosysInputPath);

			if(yosysNandDecomp_button.isSelected()) {
				yosysScript.setAbc_cmd("g", "NAND");
			}
			else if(yosysNorDecomp_button.isSelected()) {
				yosysScript.setAbc_cmd("g", "NOR");
			}

			synthesizer.runSynthesis(compilerOptions.getOutputDirectory(), new String[] {"yosys", "-p", yosysScript.generateScript()});
			File decomposedFile = compilerOptions.addVerilogFile(compilerOptions.getOutputDirectory() + File.separator + compilerOptions.getOutputFileName() + "_decomposed.v");
			VerilogModule decompVerilog = verilogParser.parseVerilogFile(decomposedFile);
			VerilogToSBOL sbolConverter = new VerilogToSBOL(true);
			WrappedSBOL decomposedSbol = sbolConverter.convertVerilog2SBOL(decompVerilog);
			String decomposedSbolPath = compilerOptions.getOutputDirectory() + File.separator + compilerOptions.getOutputFileName() + "_decomposed.xml";
			SBOLWriter.write(decomposedSbol.getSBOLDocument(), decomposedSbolPath);

			SBOLTechMapOptions techMapOptions = setVerilogTechMapProperties(decomposedSbolPath);
			techMapOptions.setOutputDirectory(synthFilePath);
			techMapOptions.setOutputFileName(outputFileName);

			List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(techMapOptions.getLibrary());
			this.specGraph = TechMapUtility.createSpecificationGraphFromSBOL(techMapOptions.getSpefication());

			Match m = new PreSelectedMatch(specGraph, libGraph);
			Cover c = new Cover(m);
			List<TechMapSolution> coverSols = new ArrayList<>();


			if(techMapOptions.isBranchBound()) {
				coverSols.add(c.branchAndBoundCover());
			}
			else if(techMapOptions.isExhaustive()) {
				coverSols = c.exhaustiveCover();
			}
			else if(techMapOptions.isGreedy()) {
				coverSols = c.greedyCover(techMapOptions.getNumOfSolutions());
			}

			int count = 1;
			for(TechMapSolution sol : coverSols) {
				if(sol.getScore() != 0.0 && sol.getScore() != Double.POSITIVE_INFINITY) {
					SBOLNetList sbolSol = new SBOLNetList(specGraph, sol);
					SBOLDocument result = sbolSol.generateSbol();
					String solFilePath = techMapOptions.getOutputDir() + File.separator + techMapOptions.getOuputFileName() + "_sol" + count++ + ".xml";
					SBOLWriter.write(result, solFilePath);	
					techmapSolPath.add(solFilePath);
				}
			}

		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(Gui.frame, 
					"Unable to locate specification file:" + synthFilePath + ".",
					"File Not Found",
					JOptionPane.WARNING_MESSAGE);
			e1.printStackTrace();
			return null;
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (BioSimException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (VerilogCompilerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SBOLConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SBOLTechMapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GeneticGatesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return techmapSolPath;
	}

	private SBOLTechMapOptions setVerilogTechMapProperties(String decomposedSbolPath) {
		SBOLTechMapOptions techMapOptions = new SBOLTechMapOptions();
		techMapOptions.setSpecificationFile(decomposedSbolPath);

		for (String libFilePath : libFilePaths) {
			try {
				techMapOptions.addLibraryFile(libFilePath);

			} 
			catch (SBOLValidationException e) {

			} 
			catch (IOException e) {

			} 
			catch (SBOLConversionException e) {

			}
		}

		techMapOptions.setNumOfSolutions(Integer.parseInt(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY)));

		switch(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY)){
		case GlobalConstants.SBOL_SYNTH_EXHAUST_BB:
			techMapOptions.setBranchBound(true);
		case GlobalConstants.SBOL_SYNTH_EXHAUSTIVE:
			techMapOptions.setExhaustive(true);
		case GlobalConstants.SBOL_SYNTH_GREEDY:
			techMapOptions.setGreedy(true);
		default:
			techMapOptions.setBranchBound(true);
		}
		return techMapOptions;
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
				String testEnvPath = testEnvTextBox.getText();
				if(!testEnvPath.isEmpty()) {
					synthProps.setProperty(GlobalConstants.SBOL_SYNTH_TESTBENCH_PROPERTY, testEnvPath);
				}
				String specId = specTextBox.getText();
				if(!specId.isEmpty()) {
					synthProps.setProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY, specId);
					synthProps.setProperty(GlobalConstants.SBOL_SYNTH_SPEC_PATH_PROPERTY, this.verilogSpecPath);
				}
				String libFileProp = "";
				for (String libFilePath : libFilePaths) {
					libFileProp = libFileProp + libFilePath + ",";
				}
				if (libFileProp.length() > 0) {
					libFileProp = libFileProp.substring(0, libFileProp.length() - 1);
				}
				synthProps.setProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY, libFileProp);
				synthProps.setProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY, coverAlgOptBox.getSelectedItem().toString());

				if (Integer.parseInt(numSolnsText.getText()) <= 0) {
					numSolnsText.setText("0");
				}
				synthProps.setProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY, numSolnsText.getText());
				if(atacs_checkbox.isSelected()) {
					String atacsAlg = atacsAlgBox.getSelectedItem().toString();
					synthProps.setProperty(GlobalConstants.SBOL_SYNTH_ATACS_PROPERTY, atacsAlg);

				}
				if(yosysNandDecomp_button.isSelected()) {
					synthProps.setProperty(GlobalConstants.SBOL_SYNTH_YOSYS_PROPERTY, GlobalConstants.SBOL_SYNTH_YOSYS_NAND);
				}
				else if(yosysNorDecomp_button.isSelected()) {
					synthProps.setProperty(GlobalConstants.SBOL_SYNTH_YOSYS_PROPERTY, GlobalConstants.SBOL_SYNTH_YOSYS_NOR);	
				}
			}
	}

	public boolean tabChanged(int tabIndex) {
		if (tabIndex == 0) {
			if(atacs_checkbox.isSelected() && !synthProps.containsKey(GlobalConstants.SBOL_SYNTH_ATACS_PROPERTY)) {
				return true;
			}
			else if(!atacs_checkbox.isSelected() && synthProps.containsKey(GlobalConstants.SBOL_SYNTH_ATACS_PROPERTY)) {
				String prevAtacsProperty = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_ATACS_PROPERTY);
				if(prevAtacsProperty.equals(GlobalConstants.SBOL_SYNTH_ATACS_ATOMIC_GATES)) {
					if(atacs_checkbox.isSelected() && atacsAlgBox.getSelectedItem().toString().equals(GlobalConstants.SBOL_SYNTH_ATACS_GC_GATES)) {
						return true;
					}
				}
				else if(prevAtacsProperty.equals(GlobalConstants.SBOL_SYNTH_ATACS_GC_GATES)) {
					if(atacs_checkbox.isSelected() && atacsAlgBox.getSelectedItem().toString().equals(GlobalConstants.SBOL_SYNTH_ATACS_ATOMIC_GATES)) {
						return true;
					}
				}
			}

			if(synthProps.containsKey(GlobalConstants.SBOL_SYNTH_YOSYS_PROPERTY)) {
				String prevYosysProperty = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_YOSYS_PROPERTY);
				if(prevYosysProperty.equals(GlobalConstants.SBOL_SYNTH_YOSYS_NAND)) {
					if(yosysNorDecomp_button.isSelected()) {
						return true;
					}
				}
				else if(prevYosysProperty.equals(GlobalConstants.SBOL_SYNTH_YOSYS_NOR)) {
					if(yosysNandDecomp_button.isSelected()) {
						return true;
					}
				}
			}
			else {
				if(yosysNorDecomp_button.isSelected() || yosysNandDecomp_button.isSelected()) {
					return true;
				}	
			}

			if(synthProps.containsKey(GlobalConstants.SBOL_SYNTH_TESTBENCH_PROPERTY)) {
				String savedTb = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_TESTBENCH_PROPERTY);
				String currTb = testEnvTextBox.getText();
				if(!savedTb.equals(currTb)){
					return true;
				}
			}
			if(libsChanged() || methodChanged() || numSolnsChanged()) {
				return true;
			}

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
		String currentMethod = coverAlgOptBox.getSelectedItem().toString();
		String previousMethod = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY);
		return !currentMethod.equals(previousMethod);
	}

	private boolean numSolnsChanged() {
		int currentNumSolns = Integer.parseInt(numSolnsText.getText());
		int previousNumSolns = Integer.parseInt(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY));
		return currentNumSolns != previousNumSolns;
	}

	public String getSpecFileID() {
		return specTextBox.getText();
	}

	public String getRootDirectory() {
		return rootFilePath;
	}

	public void renameView(String synthID) {
		this.synthID = synthID;
	}

	class MyTableModel extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5005751891198182584L;

		String[] columnNames = {"Node ID",
		"Assigned CD"};

		String[][] data; 

		@Override
		public int getRowCount() {
			return data == null? 0 : data[0].length;
		}

		@Override
		public int getColumnCount() {
			return data == null? 0 : data.length;
		}

		@Override
		public String getValueAt(int rowIndex, int columnIndex) {
			if(data == null) {
				return "";
			}
			return data[columnIndex][rowIndex];
		}
		

		public void setValueAt(String value, int row, int col) {
			data[row][col] = value;
		}
	}
}
