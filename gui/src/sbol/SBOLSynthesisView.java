package sbol;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.sbml.libsbml.CompModelPlugin;
import org.sbolstandard.core.DnaComponent;

import main.Log;
import main.util.Utility;

import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;

public class SBOLSynthesisView extends JTabbedPane implements ActionListener, Runnable {

	String synthID;
	String separator;
	String rootFilePath;
	Log log;
	JFrame frame;
	Properties synthProps;
	JTextField specText;
	List<String> libFilePaths;
	JList<String> libList;
	JScrollPane libScroll;
	JButton addLibButton;
	JButton removeLibButton;
	JComboBox methodBox;
	JLabel numSolnsLabel;
	JTextField numSolnsText;
	
	public SBOLSynthesisView(String synthID, String separator, String rootFilePath, Log log, JFrame frame) {
		this.synthID = synthID;
		this.separator = separator;
		this.rootFilePath = rootFilePath;
		this.log = log;
		new File(rootFilePath + separator + synthID).mkdir();
		JPanel optionsPanel = constructOptionsPanel();
		addTab("Synthesis Options", optionsPanel);
		getComponentAt(getComponents().length - 1).setName("Synthesis Options");
	}
	
	private JPanel constructOptionsPanel() {
		JPanel topPanel = new JPanel();
		JPanel optionsPanel = new JPanel(new BorderLayout());
		JPanel specLibPanel = constructSpecLibPanel();
		JPanel methodPanel = constructMethodPanel();
		topPanel.add(optionsPanel);
		optionsPanel.add(specLibPanel, BorderLayout.NORTH);
		optionsPanel.add(methodPanel, BorderLayout.CENTER);
		return topPanel;
	}
	
	private JPanel constructSpecLibPanel() {
		JPanel specLibPanel = new JPanel();
		JLabel libLabel = new JLabel("Library Files: ");
		JPanel inputPanel = constructSpecLibInputPanel();
		specLibPanel.add(libLabel);
		specLibPanel.add(inputPanel);
		return specLibPanel;
	}
	
	private JPanel constructSpecLibInputPanel() {
		JPanel inputPanel = new JPanel(new BorderLayout());
		JPanel specPanel = constructSpecPanel();
		libScroll = new JScrollPane();
		libScroll.setPreferredSize(new Dimension(276, 55));
		JPanel buttonPanel = constructLibButtonPanel();
		inputPanel.add(specPanel, BorderLayout.NORTH);
		inputPanel.add(libScroll, BorderLayout.WEST);
		inputPanel.add(buttonPanel, BorderLayout.SOUTH);
		return inputPanel;
	}
	
	private JPanel constructSpecPanel() {
		JPanel specPanel = new JPanel();
		JLabel specLabel = new JLabel("Specification File:");
		specText = new JTextField();
		specText.setEditable(false);
		specPanel.add(specLabel);
		specPanel.add(specText);
		return specPanel;
	}
	
	private JPanel constructLibButtonPanel() {
		JPanel buttonPanel = new JPanel();
		addLibButton = new JButton("Add");
		addLibButton.addActionListener(this);
		removeLibButton = new JButton("Remove");
		removeLibButton.addActionListener(this);
		buttonPanel.add(addLibButton);
		buttonPanel.add(removeLibButton);
		return buttonPanel;
	}
	
	private JPanel constructMethodPanel() {
		JPanel topPanel = new JPanel();
		JPanel methodPanel = new JPanel(new BorderLayout());
		JPanel labelPanel = constructMethodLabelPanel();
		JPanel inputPanel = constructMethodInputPanel();
		topPanel.add(methodPanel);
		methodPanel.add(labelPanel, BorderLayout.WEST);
		methodPanel.add(inputPanel, BorderLayout.CENTER);
		return topPanel;
	}
	
	private JPanel constructMethodLabelPanel() {
		JPanel labelPanel = new JPanel(new GridLayout(2, 1));
		labelPanel.add(new JLabel("Synthesis Method:  "));
		numSolnsLabel = new JLabel("Number of Solutions:  ");
		labelPanel.add(numSolnsLabel);
		return labelPanel;
	}
	
	private JPanel constructMethodInputPanel() {
		JPanel inputPanel = new JPanel(new GridLayout(2, 1));
		methodBox = new JComboBox(GlobalConstants.SBOL_SYNTH_STRUCTURAL_METHODS.split(","));
		methodBox.addActionListener(this);
		numSolnsText = new JTextField(39);
		numSolnsText.addActionListener(this);
		numSolnsText.addActionListener(this);
		inputPanel.add(methodBox);
		inputPanel.add(numSolnsText);
		return inputPanel;
	}
	
	public void loadDefaultSynthesisProperties(String specFileID) {
		synthProps = createDefaultSynthesisProperties(specFileID);
		saveSynthesisProperties();
		loadSynthesisOptions();
	}
	
	private Properties createDefaultSynthesisProperties(String specFileID) {
		Properties synthProps = new Properties();
		Preferences prefs = Preferences.userRoot();
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY, specFileID);
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY, 
				prefs.get(GlobalConstants.SBOL_SYNTH_LIBS_PREFERENCE, ""));
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY,
				prefs.get(GlobalConstants.SBOL_SYNTH_METHOD_PREFERENCE, 
						GlobalConstants.SBOL_SYNTH_EXHAUST_BB));
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY, 
				prefs.get(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PREFERENCE, "1"));
		return synthProps;
	}
	
	private void saveSynthesisProperties() {
		String propFilePath = rootFilePath + separator + synthID + separator + synthID 
				+ GlobalConstants.SBOL_PROPERTIES_FILE_EXTENSION;
		log.addText("Creating properties file:\n" + propFilePath + "\n");
		try {
			FileOutputStream propStreamOut = new FileOutputStream(new File(propFilePath));
			synthProps.store(propStreamOut, synthID + " SBOL Synthesis Properties");
			propStreamOut.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadSynthesisProperties(Properties synthProps) {
		this.synthProps = synthProps;
		loadSynthesisOptions();
	}
	
	private void loadSynthesisOptions() {
		specText.setText(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY));
		libFilePaths = new LinkedList<String>();
		for (String libFilePath : synthProps.getProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY).split(","))
			if (libFilePath.length() > 0)
				libFilePaths.add(libFilePath);
		libList = new JList<String>();
//		String[] libListData = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY).split(",");
//		libFilePaths = new LinkedList<String>();
//		for (int i = 0; i < libListData.length; i++) {
//			libFilePaths.add(libListData[i]);
//			String[] splitData = libListData[i].split(separator);
//			libListData[i] = splitData[splitData.length - 1];
//		}
//		libList = new JList<String>(libListData);
		updateLibraryFiles();
		libScroll.setViewportView(libList);
		methodBox.setSelectedItem(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY));
		numSolnsText.setText(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY));
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addLibButton)
			addLibraryFile(libList.getSelectedIndex());
		else if (e.getSource() == removeLibButton)
			removeLibraryFile(libList.getSelectedIndex());
		else if (e.getSource() == methodBox)
			toggleMethodSettings();
	}
	
	private void toggleMethodSettings() {
		if (methodBox.getSelectedItem().toString().equals(GlobalConstants.SBOL_SYNTH_EXHAUST_BB)) {
			numSolnsText.setText("1");
			numSolnsText.setEnabled(false);
			numSolnsLabel.setEnabled(false);
		} else {
			numSolnsText.setEnabled(true);
			numSolnsLabel.setEnabled(true);
		}
	}
	
	private void addLibraryFile(int addIndex) {
		File startDirectory = new File(Preferences.userRoot().get("biosim.general.project_dir", ""));
		String libFilePath = Utility.browse(frame, startDirectory, null, 
				JFileChooser.DIRECTORIES_ONLY, "Open", -1);
		if (!libFilePaths.contains(libFilePath)) {
			if (addIndex >= 0)
				libFilePaths.add(addIndex, libFilePath);
			else
				libFilePaths.add(libFilePath);
			updateLibraryFiles();
		}
	}
	
	private void removeLibraryFile(int removeIndex) {
		if (removeIndex >= 0)
			libFilePaths.remove(removeIndex);
		else
			libFilePaths.remove(libFilePaths.size() - 1);
		updateLibraryFiles();
	}
	
	private void updateLibraryFiles() {
		String[] libListData = new String[libFilePaths.size()];
		for (int i = 0; i < libListData.length; i++) {
			String[] splitPath = libFilePaths.get(i).split(separator);
			libListData[i] = splitPath[splitPath.length - 1];
		}
		libList.setListData(libListData);
	}
	
	public void run() { 
		
	}
	
	public Set<String> run(String synthesisFilePath, String outputFileID) {
		Set<String> sbolFilePaths = new HashSet<String>();
		for (String libFilePath : libFilePaths) 
			for (String fileID : new File(libFilePath).list())
				if (fileID.endsWith(".sbol"))
					sbolFilePaths.add(libFilePath + separator + fileID);
		SBOLFileManager fileManager = new SBOLFileManager(sbolFilePaths);
		
		Set<SBOLSynthesisGraph> graphlibrary = new HashSet<SBOLSynthesisGraph>();
		for (String libFilePath : libFilePaths) 
			for (String gateFileID : new File(libFilePath).list()) 
				if (gateFileID.endsWith(".xml")) {
					BioModel gateModel = new BioModel(libFilePath);
					gateModel.load(gateFileID);
					graphlibrary.add(new SBOLSynthesisGraph(gateModel, fileManager));
				}
		
		BioModel specModel = new BioModel(rootFilePath);
		specModel.load(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY));
		SBOLSynthesisGraph spec = new SBOLSynthesisGraph(specModel, fileManager);
		
		SBOLSynthesizer synthesizer = new SBOLSynthesizer(graphlibrary, synthProps);
		List<Integer> solution = synthesizer.mapSpecification(spec);
		
		BioModel outputModel = new BioModel(synthesisFilePath);
		outputModel.createSBMLDocument(outputFileID.replace(".xml", ""), false, false);	
		Set<SBOLSynthesisGraph> coverGraphs = synthesizer.extractSolution(solution, spec, outputModel);
		Set<String> compFileIDs = importCoverComponents(synthesisFilePath, coverGraphs, fileManager);
		outputModel.save(synthesisFilePath + separator + outputFileID);
		return compFileIDs;
	}
	
	private Set<String> importCoverComponents(String synthesisFilePath, Set<SBOLSynthesisGraph> coverGraphs, 
			SBOLFileManager fileManager) {
		Set<String> compFileIDs = new HashSet<String>();
		List<URI> compURIs = new LinkedList<URI>();
		for (SBOLSynthesisGraph coverGraph : coverGraphs) {
			compFileIDs.add(coverGraph.getImportFileID());
			compURIs.addAll(coverGraph.getCompURIs());
			BioModel compModel = new BioModel(coverGraph.getSBMLFilePath());
			compModel.load(coverGraph.getSBMLFileID());
			compModel.save(synthesisFilePath + separator + coverGraph.getImportFileID());
		}
		String sbolFileID = getSpecFileID().replace(".xml", GlobalConstants.SBOL_FILE_EXTENSION);
		fileManager.saveDNAComponents(fileManager.resolveURIs(compURIs), synthesisFilePath + separator + sbolFileID);
		compFileIDs.add(sbolFileID);
		return compFileIDs;
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
				synthProps.setProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY, 
						numSolnsText.getText());
			}
	}
	
	public boolean tabChanged(int tabIndex) {
		if (tabIndex == 0) {
			return (libsChanged() || methodChanged() || numSolnsChanged());
		} else
			return false;
	}
	
	private boolean libsChanged() {
		List<String> prevLibFilePaths = new LinkedList<String>();
		for (String prevLibFilePath : synthProps.getProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY).split(","))
				if (prevLibFilePath.length() > 0)
					prevLibFilePaths.add(prevLibFilePath);
		if (prevLibFilePaths.size() != libFilePaths.size())
			return true;
		else {
			for (String prevLibFilePath : prevLibFilePaths)
				if (!libFilePaths.contains(prevLibFilePath))
					return true;
		}
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

}
