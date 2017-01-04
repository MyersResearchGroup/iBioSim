package backend.learn.AMSModel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import backend.lpn.parser.LhpnFile;
import frontend.main.*;
import frontend.main.util.dataparser.*;


//import org.sbml.jsbml.*;

/**
 * This class creates a GUI for the Learn program. It implements the
 * ActionListener class. This allows the GUI to perform actions when menu items
 * and buttons are selected.
 * 
 * @author Curtis Madsen
 */
public class LearnLPN extends JPanel implements ActionListener, Runnable, ItemListener { // added ItemListener SB

	private static final long serialVersionUID = -5806315070287184299L;

	private JButton save, run, viewLhpn, saveLhpn, viewLog; 

	private JButton viewCoverage; 

	private JButton viewVHDL,viewVerilog; 

	private JComboBox debug; 

	private JTextField iteration, backgroundField, propertyG;

	private JComboBox numBins;

	private ArrayList<ArrayList<Component>> variables;

	private JPanel variablesPanel; 

	private JRadioButton user, auto, range, points;

	private JButton suggest;

	private String directory, lrnFile;

	private JLabel numBinsLabel;

	private Log log;

	private String separator;

	private Gui gui;

	private String seedLpnFile, lhpnFile;

	private boolean change, fail;

	private ArrayList<String> variablesList;

//	private boolean firstRead;

	private boolean generate, execute;

	private ArrayList<Variable> reqdVarsL;

	private ArrayList<Integer> reqdVarIndices;

	private ArrayList<ArrayList<Double>> data;

	private ArrayList<String> varNames;

	private int[][] bins;

	private ArrayList<ArrayList<Double>> divisionsL;

	private HashMap<String, ArrayList<Double>> thresholds;

	private Double[][] rates;

	private double[][] values;

	private Double[] duration;

	private int pathLengthBin ; //= 7 ;// intFixed 25 pd 7 integrator 15;

	private int rateSampling ; //= -1 ; //intFixed 250; 20; //-1;

	private LhpnFile g;

	private Integer numPlaces = 0;

	private Integer numTransitions = 0;

	private HashMap<String, Properties> placeInfo;

	private HashMap<String, Properties> transitionInfo;

	private Double delayScaleFactor = 1.0;

	private Double valScaleFactor = 1.0;

	BufferedWriter out;

	File logFile;

	// Threshold parameters
	private double epsilon ;//= 0.1; 

	private int runLength ; //= 15; // the number of time points that a value must persist to be considered constant

	private double runTime ; // = 5e-12; // 10e-6 for intFixed; 5e-6 for integrator. 5e-12 for pd;// the amount of time that must pass to be considered constant when using absoluteTime

	private boolean absoluteTime ; // = true; // true for intfixed //false; true for pd; false for integrator// when False time points are used to determine DMVC and when true absolutime time is used to determine DMVC

	private double percent ; // = 0.8; // a decimal value representing the percent of the total trace that must be constant to qualify to become a DMVC var

	//private double unstableTime;
	private double stableTolerance;
	
	//private boolean pseudoEnable;

	private JTextField epsilonG;

	private JTextField percentG;

	private JCheckBox absTimeG;

	private JTextField pathLengthBinG;

	private JTextField pathLengthVarG;

	private JTextField rateSamplingG;

	private JTextField runTimeG;

	private JTextField runLengthG;

//	private JTextField unstableTimeG;

	private JCheckBox pseudoEnableG;
	
	private JTextField stableToleranceG;

	private JTextField globalDelayScaling;

	private JTextField globalValueScaling;

	private JCheckBox defaultEnvG;

	private boolean suggestIsSource = false;

	private Double[] lowerLimit;

	private Double[] upperLimit; 

	//	private String failPropVHDL;

	private HashMap<String, Properties> transientNetPlaces;

	private HashMap<String, Properties> transientNetTransitions;

	//	private boolean vamsRandom = false;

	private ArrayList<String> allVars;

	private Thread LearnThread;

	private HashMap<String,Properties> dmvcValuesUnique;

	private String currentPlace;

	private String[] currPlaceBin;

	private LhpnFile lpnWithPseudo;

	private int pseudoTransNum = 0;

	private HashMap<String,Boolean> pseudoVars;

	private ArrayList<HashMap<String, String>> constVal;

	private boolean dmvDetectDone = false;

	private boolean dmvStatusLoaded = false;

	private int pathLengthVar = 40;

	private JPanel advancedOptionsPanel;
	
	String thresh;
	String[] threshValues;
	 HashMap<String, Double[]> extrema;

	// Pattern lParenR = Pattern.compile("\\(+"); 

	//Pattern floatingPointNum = Pattern.compile(">=(-*[0-9]+\\.*[0-9]*)"); 

	/**
	 * This is the constructor for the Learn class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public LearnLPN(String directory, Log log, Gui biosim) {
		separator = Gui.separator;
		this.gui = biosim;
		this.log = log;
		this.directory = directory;
		String[] getFilename = directory.split(separator);
		lhpnFile = getFilename[getFilename.length - 1] + ".lpn";
		lrnFile = getFilename[getFilename.length - 1] + ".lrn";
		Preferences biosimrc = Preferences.userRoot();

		// Sets up the encodings area
		JPanel radioPanel = new JPanel(new BorderLayout());
		JPanel selection1 = new JPanel();
		JPanel selection2 = new JPanel();
		JPanel selection = new JPanel(new BorderLayout());

		range = new JRadioButton("Minimize Range of Rates");
		points = new JRadioButton("Equalize Points Per Bin");
		user = new JRadioButton("Use User Generated Levels");
		auto = new JRadioButton("Use Auto Generated Levels");
		suggest = new JButton("Suggest Levels");
		ButtonGroup select = new ButtonGroup();
		select.add(auto);
		select.add(user);
		ButtonGroup select2 = new ButtonGroup();						
		select2.add(range);
		select2.add(points);
		user.addActionListener(this);
		range.addActionListener(this);
		auto.addActionListener(this);
		suggest.addActionListener(this);
		// if (biosimrc.get("biosim.learn.equaldata", "").equals("Equal Data Per
		// Bins")) {
		// data.setSelected(true);
		// }
		// else {
		range.setSelected(true);
		// }
		points.addActionListener(this);
		JLabel iterationLabel = new JLabel("Iterations of Optimization Algorithm");
		iteration = new JTextField("10",4);
		selection1.add(points);
		selection1.add(range);
		selection1.add(iterationLabel);
		selection1.add(iteration);
		selection2.add(auto);
		selection2.add(user);
		selection2.add(suggest);
		auto.setSelected(true);
		selection.add(selection1, "North");
		selection.add(selection2, "Center");
		suggest.setEnabled(false);
		JPanel encodingPanel = new JPanel(new BorderLayout());
		variablesPanel = new JPanel();
		JPanel sP = new JPanel();
		((FlowLayout) sP.getLayout()).setAlignment(FlowLayout.LEFT);
		sP.add(variablesPanel);
		JLabel encodingsLabel = new JLabel("Variable Levels:");
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setMinimumSize(new Dimension(260, 200));
		scroll2.setPreferredSize(new Dimension(276, 132));
		scroll2.setViewportView(sP);
		radioPanel.add(selection, "North");
		radioPanel.add(encodingPanel, "Center");
		encodingPanel.add(encodingsLabel, "North");
		encodingPanel.add(scroll2, "Center");

		// Sets up the thresholds area
		//JPanel thresholdPanel2 = new JPanel(new GridLayout(8, 2));
		JPanel thresholdPanel1 = new JPanel(new GridLayout(1, 2));

		JLabel backgroundLabel = new JLabel("Model File:");
		backgroundField = new JTextField(lhpnFile);
		backgroundField.setEditable(false);
		thresholdPanel1.add(backgroundLabel);
		thresholdPanel1.add(backgroundField);
		

		//Model Generation Parameters 
		//Advanced Options Tab
		JLabel epsilonLabel = new JLabel("Epsilon");
		epsilonG = new JTextField("0.1");
		epsilonG.addActionListener(this); 
		JLabel pathLengthBinLabel = new JLabel("Minimum bin pathLength");
		pathLengthBinG = new JTextField("0");
		pathLengthBinG.addActionListener(this); 
		JLabel pathLengthVarLabel = new JLabel("Minimum variable pathLength");
		pathLengthVarG = new JTextField("0");
		pathLengthVarG.addActionListener(this); 
		JLabel rateSamplingLabel = new JLabel("Window size");
		rateSamplingG = new JTextField("-1");
		rateSamplingG.addActionListener(this); 
		JLabel absTimeLabel = new JLabel("Absolute time");
		absTimeG = new JCheckBox();
		absTimeG.setSelected(false);
		absTimeG.addItemListener(this); 
		JLabel percentLabel = new JLabel("Fraction");
		percentG = new JTextField("0.8");
		percentG.addActionListener(this); 
		JLabel runTimeLabel = new JLabel("DMV run time");
		runTimeG = new JTextField("5e-6");
		runTimeG.setEnabled(false);
		runTimeG.addActionListener(this);
		JLabel runLengthLabel = new JLabel("DMV run length");
		runLengthG = new JTextField("2");
		runLengthG.setEnabled(true);
		runLengthG.addActionListener(this);
		JLabel defaultEnvLabel = new JLabel("Default environment");
		defaultEnvG = new JCheckBox();
		defaultEnvG.setSelected(true);
		defaultEnvG.addItemListener(this); 
		//JLabel unstableTimeLabel = new JLabel("Mode unstable time");
		//unstableTimeG = new JTextField("5960.0");
		//unstableTimeG.setEnabled(true);
		//unstableTimeG.addActionListener(this);
		JLabel stableToleranceLabel = new JLabel("Stable tolerance");
		stableToleranceG = new JTextField("0.02");
		stableToleranceG.setEnabled(true);
		stableToleranceG.addActionListener(this);
		JLabel pseudoEnableLabel = new JLabel("Enable pseudo-transitions");
		pseudoEnableG = new JCheckBox();
		pseudoEnableG.setSelected(false);
		pseudoEnableG.addItemListener(this);
		
		JPanel panel4 = new JPanel();
		((FlowLayout) panel4.getLayout()).setAlignment(FlowLayout.CENTER);
		JPanel panel3 = new JPanel(new GridLayout(14, 2));
		panel4.add(panel3, "Center");
		panel3.add(epsilonLabel);
		panel3.add(epsilonG);
		panel3.add(rateSamplingLabel);
		panel3.add(rateSamplingG);
		panel3.add(pathLengthBinLabel);
		panel3.add(pathLengthBinG);
		panel3.add(pathLengthVarLabel);
		panel3.add(pathLengthVarG);
		panel3.add(absTimeLabel);
		panel3.add(absTimeG);
		panel3.add(percentLabel);
		panel3.add(percentG);
		panel3.add(runTimeLabel);
		panel3.add(runTimeG);
		panel3.add(runLengthLabel);
		panel3.add(runLengthG);
		panel3.add(defaultEnvLabel);
		panel3.add(defaultEnvG);
		//panel3.add(unstableTimeLabel);
		//panel3.add(unstableTimeG);
		panel3.add(stableToleranceLabel);
		panel3.add(stableToleranceG);
		panel3.add(pseudoEnableLabel);
		panel3.add(pseudoEnableG);

		thresholds = new HashMap<String, ArrayList<Double>>(); // <Each Variable, List of thresholds for each variable>
		reqdVarsL = new ArrayList<Variable>();  //List of objects for variables that are marked as "ip/op" in variables panel

		//Top panel in learn view
		numBinsLabel = new JLabel("Number of Bins:");
		String[] bins = { "Auto", "2", "3", "4", "5", "6", "7", "8", "16", "32"};//, "10", "11", "12", "13", "14", "15", "16", "17", "33", "65", "129", "257" };
		numBins = new JComboBox(bins);
		numBins.setSelectedItem(biosimrc.get("biosim.learn.bins", ""));
		numBins.addActionListener(this);
		numBins.setActionCommand("text");
		selection2.add(numBinsLabel);
		selection2.add(numBins);
		JLabel propertyLabel = new JLabel("Assertion to be Verified");
		propertyG = new JTextField("");
		propertyG.addActionListener(this);
		panel3.add(propertyLabel);
		panel3.add(propertyG);
		JLabel valueScaleLabel = new JLabel("Scale Factor for Values");
		globalValueScaling = new JTextField("");
		globalValueScaling.addActionListener(this);
		panel3.add(valueScaleLabel);
		panel3.add(globalValueScaling);
		JLabel delayScaleLabel = new JLabel("Scale Factor for Time");
		globalDelayScaling = new JTextField("");
		globalDelayScaling.addActionListener(this);
		panel3.add(delayScaleLabel);
		panel3.add(globalDelayScaling);
		JPanel thresholdPanelHold1 = new JPanel();
		thresholdPanelHold1.add(thresholdPanel1);
//		JLabel debugLabel = new JLabel("Debug Level:");
//		String[] options = new String[4];
//		options[0] = "0";
//		options[1] = "1";
//		options[2] = "2";
//		options[3] = "3";
//		debug = new JComboBox(options);
//		// debug.setSelectedItem(biosimrc.get("biosim.learn.debug", ""));
//		debug.addActionListener(this);
//		thresholdPanel2.add(debugLabel);
//		thresholdPanel2.add(debug);

		// load parameters
		// reading lrnFile twice. On the first read, only seedLpnFile (the seed lpn) is processed.
		// In the gap b/w these reads, reqdVarsL is created based on the seedLpnFile
		Properties load = new Properties();
		seedLpnFile = "";
		TSDParser extractVars;
		ArrayList<String> datFileVars = new ArrayList<String>();
		allVars = new ArrayList<String>();
		Boolean varPresent = false;
		//Finding the intersection of all the variables present in all data files.
		for (int i = 1; (new File(directory + separator + "run-" + i + ".tsd")).exists(); i++) {
			extractVars = new TSDParser(directory + separator + "run-" + i + ".tsd", false);
			datFileVars = extractVars.getSpecies();
			if (i == 1){
				allVars.addAll(datFileVars);
			}
			for (String s : allVars){
				varPresent = false;
				for (String t : datFileVars){
					if (s.equalsIgnoreCase(t)){
						varPresent = true;
						break;
					}
				}
				if (!varPresent){
					allVars.remove(s);
				}
			}
		}
		if (allVars.size() != 0){
			if (allVars.get(0).toLowerCase(Locale.ENGLISH).contains("time")){
				allVars.remove(0);
			}
			else{
				JOptionPane.showMessageDialog(Gui.frame,
						"Error!",
						"AllVars doesnot have time at zeroeth element. Something wrong. Please check time variable", JOptionPane.ERROR_MESSAGE);
			}
		}
		//System.out.println("All variables are : ");
		//for (String s : allVars){
		//	System.out.print(s + " ");
		//}
		try {
			FileInputStream in = new FileInputStream(new File(directory
					+ separator + lrnFile));
			load.load(in);
			in.close();
			if (load.containsKey("learn.file")) {
				String[] getProp = load.getProperty("learn.file").split(
						separator);
				seedLpnFile = directory.substring(0, directory.length()
						- getFilename[getFilename.length - 1].length())
						+ separator + getProp[getProp.length - 1];
				backgroundField.setText(getProp[getProp.length - 1]);

			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}
		variablesList = new ArrayList<String>();
		LhpnFile lhpn = new LhpnFile(log);
		lhpn.load(seedLpnFile);
		HashMap<String, Properties> variablesMap = lhpn.getContinuous(); //System.out.println("Variables MAp :"+variablesMap.keySet());
		for (String s : variablesMap.keySet()) {  //System.out.println("Variables MAp :"+s);
			variablesList.add(s);
			reqdVarsL.add(new Variable(s));
			thresholds.put(s, new ArrayList<Double>());
		}
		
		try {
			FileInputStream in = new FileInputStream(new File(directory
					+ separator + lrnFile));
			load.load(in);
			in.close();
			if (load.containsKey("learn.iter")) {
				iteration.setText(load.getProperty("learn.iter"));
			}
			if (load.containsKey("learn.valueScaling")) {
				globalValueScaling.setText(load.getProperty("learn.valueScaling"));
				if (globalValueScaling.getText().matches("[\\d]+\\.??[\\d]*")){
					valScaleFactor = Double.parseDouble(globalValueScaling.getText().trim());
				} else
					valScaleFactor = -1.0;
			if (load.containsKey("learn.delayScaling")) {
				globalDelayScaling.setText(load.getProperty("learn.delayScaling"));
				if (globalDelayScaling.getText().matches("[\\d]+\\.??[\\d]*")){
					delayScaleFactor = Double.parseDouble(globalDelayScaling.getText().trim());
				} else
					delayScaleFactor = -1.0;			}
			}
			if (load.containsKey("learn.bins")) {
				numBins.setSelectedItem(load.getProperty("learn.bins"));
			}
			if (load.containsKey("learn.prop")) {
				propertyG.setText(load.getProperty("learn.prop"));
			}
			if (load.containsKey("learn.equal")) {
				if (load.getProperty("learn.equal").equals("range")) {
					range.setSelected(true);
				} else {
					points.setSelected(true);
				}
			}
			if (load.containsKey("learn.use")) {
				if (load.getProperty("learn.use").equals("auto")) {
					auto.setSelected(true);
				} else if (load.getProperty("learn.use").equals("user")) {
					user.setSelected(true);
				}
			}
			if (load.containsKey("learn.epsilon")){
				epsilonG.setText(load.getProperty("learn.epsilon"));
			}
			if (load.containsKey("learn.rateSampling")){
				rateSamplingG.setText(load.getProperty("learn.rateSampling"));
			}
			if (load.containsKey("learn.pathLengthBin")){
				pathLengthBinG.setText(load.getProperty("learn.pathLengthBin"));
			}
			if (load.containsKey("learn.pathLengthVar")){
				pathLengthVarG.setText(load.getProperty("learn.pathLengthVar"));
			}
			if (load.containsKey("learn.percent")){
				percentG.setText(load.getProperty("learn.percent"));
			}
			if (load.containsKey("learn.absTime")){
				absTimeG.setSelected(Boolean.parseBoolean(load.getProperty("learn.absTime")));
			}
			if (load.containsKey("learn.runTime")){
				runTimeG.setText(load.getProperty("learn.runTime"));
			}
			if (load.containsKey("learn.runLength")){
				runLengthG.setText(load.getProperty("learn.runLength"));
			}
			if (load.containsKey("learn.defaultEnv")){
				defaultEnvG.setSelected(Boolean.parseBoolean(load.getProperty("learn.defaultEnv")));
			}
			if (load.containsKey("learn.pseudoEnable")){
				pseudoEnableG.setSelected(Boolean.parseBoolean(load.getProperty("learn.pseudoEnable")));
			}
			if (load.containsKey("learn.stableTolerance")){
				stableToleranceG.setText(load.getProperty("learn.stableTolerance"));
			}
			
			//levels();
			
			String[] varsList = null;
			if (load.containsKey("learn.varsList")){
				String varsListString = load.getProperty("learn.varsList");
				varsList = varsListString.split("\\s");
				//System.out.println("VARSLIST  :"+varsList);
				for (String st1 : varsList){ 
					boolean varFound = false;
					String s = load.getProperty("learn.bins" + st1);
					String[] savedBins = null;
					if (s != null){
						savedBins = s.split("\\s");
						//for (int i = 2; i < savedBins.length ; i++){ System.out.println("saved bins[] :"+savedBins[i]);}
					}
					for (String st2 :variablesList){
						if (st1.equalsIgnoreCase(st2)){ 
							varFound = true;
							break;
						}
						
				
					}
					if (varFound){ 
						if (savedBins != null){
							for (int i = 2; i < savedBins.length ; i++){ 
								thresholds.get(st1).add(Double.parseDouble(savedBins[i]));
							}
						}
					}
					else{
						variablesList.add(st1); 
						reqdVarsL.add(new Variable(st1));
						thresholds.put(st1, new ArrayList<Double>());
						if (savedBins != null){
							for (int i = 2; i < savedBins.length ; i++){ 
								thresholds.get(st1).add(Double.parseDouble(savedBins[i])); 
								
							}
						}
					}
					String e = load.getProperty("learn.epsilon" + st1);
					if ((findReqdVarslIndex(st1) != -1) && (e != null)){
						reqdVarsL.get(findReqdVarslIndex(st1)).setEpsilon(Double.valueOf(e));
					}
				}
				ArrayList<Variable> removeVars = new ArrayList<Variable>();
				for (Variable v : reqdVarsL){
					boolean varFound = false;
					String st1 = v.getName();
					for (String st2 : varsList){
						if (st1.equalsIgnoreCase(st2)){
							varFound = true;
						}
					}
					if (!varFound){
						variablesList.remove(st1);
						removeVars.add(v);
						//reqdVarsL.remove(v); not allowed. concurrent modification exception
						thresholds.remove(st1); 
					}
				}
				for (Variable v : removeVars){
					reqdVarsL.remove(v);
				}
			}
			if (load.containsKey("learn.inputs")){
				String s = load.getProperty("learn.inputs");
				String[] savedInputs = s.split("\\s");
				for (String st1 : savedInputs){
					for (int i = 0; i < reqdVarsL.size(); i++){
						if ( reqdVarsL.get(i).getName().equalsIgnoreCase(st1)){
							reqdVarsL.get(i).setInput(true);
						}
					}
				}
			}
			
			if (load.containsKey("learn.destabs")){
				String s = load.getProperty("learn.destabs");
				String[] savedDestabs = s.split("\\s");
				for (String st1 : savedDestabs){
					for (int i = 0; i < reqdVarsL.size(); i++){
						if ( reqdVarsL.get(i).getName().equalsIgnoreCase(st1)){
							reqdVarsL.get(i).setDestab(true);
						}
					}
				}
			}
			if (load.containsKey("learn.dontcares")){
				String s = load.getProperty("learn.dontcares");
				String[] savedDontCares = s.split("\\s");
				for (String st1 : savedDontCares){
					for (int i = 0; i < reqdVarsL.size(); i++){
						if ( reqdVarsL.get(i).getName().equalsIgnoreCase(st1)){
							reqdVarsL.get(i).setCare(false);
						}
					}
				}
			}
			if (load.containsKey("learn.interpolate")){
				String s = load.getProperty("learn.interpolate");
				String[] savedInterpolate = s.split("\\s");
				for (String st1 : savedInterpolate){
					for (int i = 0; i < reqdVarsL.size(); i++){
						String newName = reqdVarsL.get(i).getName();
						if ( newName.equalsIgnoreCase(st1)){
							reqdVarsL.get(i).setInterpolate(true);
						}
					}
				}
			}
			if (load.containsKey("learn.dmv")){
				String s = load.getProperty("learn.dmv");
				String[] savedInputs = s.split("\\s");
				for (String st1 : savedInputs){
					for (int i = 0; i < reqdVarsL.size(); i++){
						if ( reqdVarsL.get(i).getName().equalsIgnoreCase(st1)){
							reqdVarsL.get(i).setDmvc(true);
						}
					}
				}
				dmvStatusLoaded = true;
			}
			if (load.containsKey("learn.cont")){
				String s = load.getProperty("learn.cont");
				String[] savedInputs = s.split("\\s");
				for (String st1 : savedInputs){
					for (int i = 0; i < reqdVarsL.size(); i++){
						if ( reqdVarsL.get(i).getName().equalsIgnoreCase(st1)){
							reqdVarsL.get(i).setDmvc(false);
						}
					}
				}
				dmvStatusLoaded = true;
			}
			if (load.containsKey("learn.autoVar")){
				String s = load.getProperty("learn.autoVar");
				String[] savedInputs = s.split("\\s");
				for (String st1 : savedInputs){
					for (int i = 0; i < reqdVarsL.size(); i++){
						if ( reqdVarsL.get(i).getName().equalsIgnoreCase(st1)){
							reqdVarsL.get(i).setDmvc(false);
						}
					}
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}
		try {
			String extension = ".lpn";
			if (seedLpnFile.endsWith(".xml")) {
				extension = ".xml";
			}
			FileWriter write = new FileWriter(new File(directory + separator + "background"+extension));
			BufferedReader input = new BufferedReader(new FileReader(new File(seedLpnFile)));
			String line = null;
			while ((line = input.readLine()) != null) {
				write.write(line + "\n");
			}
			write.close();
			input.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to create background file!",
					"Error Writing Background", JOptionPane.ERROR_MESSAGE);
		}

		// sortSpecies();
		JPanel runHolder = new JPanel();
		
		levels(); 
		//autogen(false);
		if (auto.isSelected()) {
			auto.doClick();
		} else {
			//user.doClick();
			numBinsLabel.setEnabled(false);
			numBins.setEnabled(false);
			suggest.setEnabled(true);
		}

		// Creates the run button
		run = new JButton("Save and Learn");
		runHolder.add(run);
		run.addActionListener(this);
		run.setMnemonic(KeyEvent.VK_L);

		// Creates the run button
		save = new JButton("Save Parameters");
		runHolder.add(save);
		save.addActionListener(this);
		save.setMnemonic(KeyEvent.VK_S);

		// Creates the view circuit button
		viewLhpn = new JButton("View Circuit");
		runHolder.add(viewLhpn);
		viewLhpn.addActionListener(this);
		viewLhpn.setMnemonic(KeyEvent.VK_V);

		// Creates the save circuit button
		saveLhpn = new JButton("Save Circuit");
		runHolder.add(saveLhpn);
		saveLhpn.addActionListener(this);
		saveLhpn.setMnemonic(KeyEvent.VK_C);

		// Creates the view circuit button
		viewLog = new JButton("View Run Log");
		runHolder.add(viewLog);
		viewLog.addActionListener(this);
		viewLog.setMnemonic(KeyEvent.VK_R);
		if (!(new File(directory + separator + lhpnFile).exists())) {
			viewLhpn.setEnabled(false);
			saveLhpn.setEnabled(false);
		}
		else{
			viewLhpn.setEnabled(true);
			saveLhpn.setEnabled(true);
		}
		if (!(new File(directory + separator + "run.log").exists())) {
			viewLog.setEnabled(false);
		}
		// SB
		viewCoverage = new JButton("View Coverage Report");
		viewVHDL = new JButton("View VHDL-AMS Model");
		viewVerilog = new JButton("View Verilog-AMS Model");
		runHolder.add(viewCoverage);
		runHolder.add(viewVHDL);
		viewCoverage.addActionListener(this);
		viewVHDL.addActionListener(this);
		//	viewCoverage.setMnemonic(KeyEvent.VK_R);
		if (!(new File(directory + separator + "run.cvg").exists())) {
			viewCoverage.setEnabled(false);
		}
		else{
			viewCoverage.setEnabled(true);
		}
		//	String vhdFile = lhpnFile.replace(".lpn",".vhd");
		//	if (!(new File(directory + separator + vhdFile).exists())) {
		viewVHDL.setEnabled(false);
		//	}
		//	else{
		//		viewVHDL.setEnabled(true);
		//	}

		// Creates the main panel
		this.setLayout(new BorderLayout());
		JPanel middlePanel = new JPanel(new BorderLayout());
		JPanel firstTab = new JPanel(new BorderLayout());
		JPanel firstTab1 = new JPanel(new BorderLayout());
		JPanel secondTab = new JPanel(new BorderLayout()); // SB uncommented
		middlePanel.add(radioPanel, "Center");
		// firstTab1.add(initNet, "North");
		firstTab1.add(thresholdPanelHold1, "Center");
		firstTab.add(firstTab1, "North");
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				middlePanel, null);
		splitPane.setDividerSize(0);
		// secondTab.add(thresholdPanelHold2, "North");
		//	JPanel binsFileHoldPanel = new JPanel();
		//	binsFileHoldPanel.add(binsFilePanel);
		//binsFileHoldPanel.setMinimumSize(new Dimension(10000,16000));
		//binsFileHoldPanel.setPreferredSize(getPreferredSize());
		//secondTab.add(binsFileHoldPanel, "Center");
		// secondTab.add(newPanel,"Center");
		secondTab.add(panel4, "Center");
		firstTab.add(splitPane, "Center");
		//JTabbedPane tab = new JTabbedPane();
		//tab.addTab("Basic Options", firstTab);
		//tab.addTab("Advanced Options", secondTab);
		this.add(firstTab, "Center");
		advancedOptionsPanel = secondTab;
		//this.addTab("Basic", (JComponent)firstTab);
		//this.addTab("Advanced", (JComponent)firstTab);
		// this.add(runHolder, "South");
		//firstRead = true;
		/* if (user.isSelected()) {
		 auto.doClick();
	     user.doClick();
		 }
		 else {
		 user.doClick();
		 auto.doClick();
		 } */
		//firstRead = false;
		change = false;
	}

	/*public LearnLHPN() {
		extrema = getExtrema();
		System.out.println("extrema in the constructor :"+extrema);
		
		
		
		
	} */

	public JPanel getAdvancedOptionsPanel() {
		return advancedOptionsPanel;
	}

	/**
	 * This method performs different functions depending on what menu items or
	 * buttons are selected.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		change = true;
		if (e.getActionCommand().contains("text")) {
			if (variables != null && user.isSelected()) { // System.out.println("Var :"+variables);// action source is any of the variables' combobox 
				for (int i = 0; i < variables.size(); i++) {
					String currentVar = ((JTextField) variables.get(i).get(0)).getText().trim();
					if (findReqdVarslIndex(currentVar) != -1){ // condition added after adding allVars
						String selected = (String) (((JComboBox) variables.get(i).get(6)).getSelectedItem()); // changed 2 to 3 after required
						int combox_selected;
						if (!selected.equalsIgnoreCase("Auto"))
							combox_selected = Integer.parseInt(selected); // changed 2 to 3 after required
						else 
							combox_selected = 0;
						ArrayList<Double> iThresholds =  thresholds.get(currentVar);
						if (combox_selected != 0){
							if ((iThresholds != null) && ( iThresholds.size() >= combox_selected)){
								for (int j = iThresholds.size() - 1; j >= (combox_selected-1) ; j--){
									thresholds.get(currentVar).remove(j);
								}
							}
						} else { // if 0 selected, then remove all the stored thresholds
							if ((iThresholds != null) && ( iThresholds.size() >= 1)){
								for (int j = iThresholds.size() - 1; j >= 0 ; j--){
									thresholds.get(currentVar).remove(j);
								}
							}
						}
					}
				}
			}
			else if ( auto.isSelected()) {  // variables != null // action source is numBins on top
				//int combox_selected = Integer.parseInt((String) numBins.getSelectedItem());
				String selected = (String) (numBins.getSelectedItem()); // changed 2 to 3 after required
				int combox_selected;
				if (!selected.equalsIgnoreCase("Auto"))
					combox_selected = Integer.parseInt(selected); // changed 2 to 3 after required
				else 
					combox_selected = 0;
				
				for (int i = 0; i < allVars.size(); i++) { 
					
					String currentVar = allVars.get(i);
				//	if (((String)(((JComboBox) variables.get(i).get(5)).getSelectedItem())).equals("DMV")){
						
				//		combox_selected = 0;
				//	}
					if (findReqdVarslIndex(currentVar) != -1){ // condition added after adding allVars
						
						ArrayList<Double> iThresholds =  thresholds.get(currentVar);
						if (combox_selected != 0){
							if ((iThresholds != null) &&  (iThresholds.size() >= combox_selected)){
								for (int j = iThresholds.size() - 1; j >= (combox_selected-1) ; j--){
									thresholds.get(currentVar).remove(j);
								}
							}
						}
						else{
							if ((iThresholds != null) && ( iThresholds.size() >= 1)){
								for (int j = iThresholds.size() - 1; j >= 0; j--){
									thresholds.get(currentVar).remove(j);
								}
							}
						}
					}//}
				}
			}
			variablesPanel.revalidate();
			variablesPanel.repaint();
			int j = 0;
			for (Component c : variablesPanel.getComponents()) {
				if (j == 0){   // recheck .. SB
					j++;		// SB
					continue;	// SB
				}				//SB
				String s = ((JTextField)((JPanel) c).getComponent(0)).getText().trim();
				if (findReqdVarslIndex(s) != -1) { // condition added after adding allVars
					if (reqdVarsL.get((findReqdVarslIndex(s))).isInput())
						((JComboBox)((JPanel) c).getComponent(1)).setSelectedItem("Input"); // added after adding allVars
					else
						((JComboBox)((JPanel) c).getComponent(1)).setSelectedItem("Output"); // added after adding allVars
					((JCheckBox)((JPanel) c).getComponent(2)).setEnabled(true);// added after adding allVars
					((JCheckBox)((JPanel) c).getComponent(3)).setEnabled(true);
					((JTextField)((JPanel) c).getComponent(4)).setEnabled(true);
					((JComboBox)((JPanel) c).getComponent(5)).setEnabled(true);
					//	for (int i = 3; i < ((JPanel) c).getComponentCount(); i++) { // added after allVars
					//		((JPanel) c).getComponent(i).setEnabled(true); // added after adding allVars
					//	}
					//if (reqdVarsL.get(j-1).isInput()){ changed after adding allVars
					if (reqdVarsL.get(findReqdVarslIndex(s)).isDestab()){
						((JCheckBox)((JPanel) c).getComponent(2)).setSelected(true); // // changed 1 to 2 after required
					} else {
						((JCheckBox)((JPanel) c).getComponent(2)).setSelected(false); // // changed 1 to 2 after required
					}
					if (reqdVarsL.get(findReqdVarslIndex(s)).isCare()){
						((JCheckBox)((JPanel) c).getComponent(3)).setSelected(true); // // changed 1 to 2 after required
					}
					if (reqdVarsL.get(findReqdVarslIndex(s)).isCare()){
						((JCheckBox)((JPanel) c).getComponent(3)).setSelected(true); // // changed 1 to 2 after required
					}else {
						((JCheckBox)((JPanel) c).getComponent(3)).setSelected(false); // // changed 1 to 2 after required
					}
					if (reqdVarsL.get(findReqdVarslIndex(s)).isInterpolate()){
						((JCheckBox)((JPanel) c).getComponent(7)).setSelected(true); // // changed 1 to 2 after required
					}else {
						((JCheckBox)((JPanel) c).getComponent(7)).setSelected(false); // // changed 1 to 2 after required
					}
					//for (Variable v : reqdVarsL){ //SB after required
					//	if ((v.getName()).equalsIgnoreCase(((JTextField)((JPanel) c).getComponent(0)).toString())){
					//		((JCheckBox)((JPanel) c).getComponent(1)).setSelected(true); // for required
					//	}
					//}
				} else {	// added after adding allVars
					((JComboBox)((JPanel) c).getComponent(1)).setSelectedItem("Not used");
					((JCheckBox)((JPanel) c).getComponent(2)).setEnabled(false);
					((JCheckBox)((JPanel) c).getComponent(3)).setEnabled(false);
					((JTextField)((JPanel) c).getComponent(4)).setEnabled(false);
					((JComboBox)((JPanel) c).getComponent(5)).setEnabled(false);
					for (int i = 6; i < ((JPanel) c).getComponentCount(); i++) { // added after allVars
						((JPanel) c).getComponent(i).setEnabled(false); // added after adding allVars
					}
					((JCheckBox)((JPanel) c).getComponent(7)).setEnabled(false);
				}
				j++;
			}
			//biosim.setGlassPane(true);
		} else if (e.getSource() == numBins || e.getSource() == debug) {
			//biosim.setGlassPane(true);
		} //else if (e.getActionCommand().contains("dmv")) {
		//int num = Integer.parseInt(e.getActionCommand().substring(3)) - 1;
		//editText(num);
		//} 
		else if (e.getActionCommand().contains("DMV")) {
			//int num = Integer.parseInt(e.getActionCommand().substring(5));  // -1; ??
			//reqdVarsL.get(num).setInput(!reqdVarsL.get(num).isInput());
			String var = e.getActionCommand().substring(3,e.getActionCommand().length());  // -1; ??
			/*for (int i = 0; i < reqdVarsL.size() ; i++){ // COMMENTED after adding required
				if (var.equalsIgnoreCase(reqdVarsL.get(i).getName())){
					reqdVarsL.get(i).setInput(!reqdVarsL.get(i).isInput());
					break;
				}
			}*/ 
			for (int i = 0 ; i < variables.size(); i++){
				if ((((JTextField) variables.get(i).get(0)).getText().trim()).equalsIgnoreCase(var)){
					if (!(((((JComboBox) variables.get(i).get(1)).getSelectedItem()).toString()).equalsIgnoreCase("Not used"))){
						if (((String)(((JComboBox) variables.get(i).get(5)).getSelectedItem())).equals("DMV")){
							int v = findReqdVarslIndex(((JTextField) variables.get(i).get(0)).getText().trim());
							reqdVarsL.get(v).forceDmvc(true);
							//reqdVarsL.get(v).setDmvc(true);
						} else if (((String)(((JComboBox) variables.get(i).get(5)).getSelectedItem())).equals("Continuous")){
							int v = findReqdVarslIndex(((JTextField) variables.get(i).get(0)).getText().trim());
							reqdVarsL.get(v).forceDmvc(false);
							//reqdVarsL.get(v).setDmvc(false);
						} else {
							int v = findReqdVarslIndex(((JTextField) variables.get(i).get(0)).getText().trim());
							reqdVarsL.get(v).forceDmvc(null);
						}
						break;
					}
				}
			}
		}
		else if (e.getActionCommand().contains("mode")) {
			//int num = Integer.parseInt(e.getActionCommand().substring(5));  // -1; ??
			//reqdVarsL.get(num).setInput(!reqdVarsL.get(num).isInput());
			String var = e.getActionCommand().substring(4,e.getActionCommand().length());  // -1; ??
			/*for (int i = 0; i < reqdVarsL.size() ; i++){ // COMMENTED after adding required
				if (var.equalsIgnoreCase(reqdVarsL.get(i).getName())){
					reqdVarsL.get(i).setInput(!reqdVarsL.get(i).isInput());
					break;
				}
			}*/ 
			for (int i = 0 ; i < variables.size(); i++){
				if ((((JTextField) variables.get(i).get(0)).getText().trim()).equalsIgnoreCase(var)){
					if (!(((((JComboBox) variables.get(i).get(1)).getSelectedItem()).toString()).equalsIgnoreCase("Not used"))){
						reqdVarsL.get(findReqdVarslIndex(((JTextField) variables.get(i).get(0)).getText().trim())).setDestab(((JCheckBox) variables.get(i).get(2)).isSelected());
					}
				}
			}
		}
		else if (e.getActionCommand().contains("care")) {
			String var = e.getActionCommand().substring(4,e.getActionCommand().length());  // -1; ??
			for (int i = 0 ; i < variables.size(); i++){
				if ((((JTextField) variables.get(i).get(0)).getText().trim()).equalsIgnoreCase(var)){
					if (!(((((JComboBox) variables.get(i).get(1)).getSelectedItem()).toString()).equalsIgnoreCase("Not used"))){
						reqdVarsL.get(findReqdVarslIndex(((JTextField) variables.get(i).get(0)).getText().trim())).setCare(((JCheckBox) variables.get(i).get(3)).isSelected());
					}
				}
			}
		}
		else if (e.getActionCommand().contains("interpolate")) {
			String var = e.getActionCommand().substring(11,e.getActionCommand().length());  // -1; ??
			for (int i = 0 ; i < variables.size(); i++){
				if ((((JTextField) variables.get(i).get(0)).getText().trim()).equalsIgnoreCase(var)){
					if (!(((((JComboBox) variables.get(i).get(1)).getSelectedItem()).toString()).equalsIgnoreCase("Not used"))){
						reqdVarsL.get(findReqdVarslIndex(((JTextField) variables.get(i).get(0)).getText().trim())).setInterpolate(((JCheckBox) variables.get(i).get(7)).isSelected());
					}
				}
			}
		}
		else if (e.getActionCommand().contains("port")) {
			//int num = Integer.parseInt(e.getActionCommand().substring(5));  // -1; ??
			//reqdVarsL.get(num).setInput(!reqdVarsL.get(num).isInput());
			String var = e.getActionCommand().substring(4,e.getActionCommand().length());  // -1; ??
			for (int i = 0 ; i < variables.size(); i++){
				String currentVar = ((JTextField) variables.get(i).get(0)).getText().trim();
				if ((currentVar).equalsIgnoreCase(var)){
					if (!(((((JComboBox) variables.get(i).get(1)).getSelectedItem()).toString()).equalsIgnoreCase("Not used"))){
						((JCheckBox) variables.get(i).get(2)).setEnabled(true);
						((JCheckBox) variables.get(i).get(3)).setEnabled(true);
						((JTextField) variables.get(i).get(4)).setEnabled(true);
						((JComboBox) variables.get(i).get(5)).setEnabled(true);
						((JCheckBox) variables.get(i).get(7)).setEnabled(true);
						if ( auto.isSelected()) {
							//TODO: could disable the comboboxes & thresholds though
							// they would already be disabled here.
						}
						else{
							for (int j = 6; j < variables.get(i).size(); j++) { // added after allVars
								variables.get(i).get(j).setEnabled(true); // added after adding allVars
							}
						}
						if (findReqdVarslIndex(var) == -1){
							reqdVarsL.add(new Variable(var));
							//((JCheckBox) variables.get(i).get(3)).setSelected(true);
							if (((JCheckBox) variables.get(i).get(2)).isSelected()){
								if (reqdVarsL.get(reqdVarsL.size() - 1).getName().equalsIgnoreCase(var))
									reqdVarsL.get(reqdVarsL.size() - 1).setDestab(true);
							}
							if (!(((JCheckBox) variables.get(i).get(3)).isSelected())){
								if (reqdVarsL.get(reqdVarsL.size() - 1).getName().equalsIgnoreCase(var))
									reqdVarsL.get(reqdVarsL.size() - 1).setCare(false);
							}
							if ((((JCheckBox) variables.get(i).get(7)).isSelected())){
								if (reqdVarsL.get(reqdVarsL.size() - 1).getName().equalsIgnoreCase(var))
									reqdVarsL.get(reqdVarsL.size() - 1).setInterpolate(true);
							}
							
						} 
						if ((((((JComboBox) variables.get(i).get(1)).getSelectedItem()).toString()).equalsIgnoreCase("Input"))){
							reqdVarsL.get(findReqdVarslIndex(var)).setInput(true);
						} else {
							reqdVarsL.get(findReqdVarslIndex(var)).setInput(false);
						}
					}
					else{
						((JCheckBox) variables.get(i).get(2)).setEnabled(false);
						((JCheckBox) variables.get(i).get(3)).setEnabled(false);
						((JTextField) variables.get(i).get(4)).setEnabled(false);
						((JComboBox) variables.get(i).get(5)).setEnabled(false);
						for (int j = 6; j < variables.get(i).size(); j++) { // added after allVars
							variables.get(i).get(j).setEnabled(false); // added after adding allVars
						}
						((JCheckBox) variables.get(i).get(7)).setEnabled(false);
						int ind = findReqdVarslIndex(var);
						if (ind != -1){
							reqdVarsL.remove(ind);
							//TODO: Should keep updating reqdVarsIndices all the times??						
						}
					}
				}
			}
			//for (int i = 0; i < reqdVarsL.size() ; i++){
			//	if (var.equalsIgnoreCase(reqdVarsL.get(i).getName())){
			//		reqdVarsL.get(i).setInput(!reqdVarsL.get(i).isInput());
			//		break;
			//	}
			//}
		}
		else if (e.getActionCommand().contains("epsilon")) {
			String var = e.getActionCommand().substring(7,e.getActionCommand().length());  // -1; ??
			for (int i = 0 ; i < variables.size(); i++){
				if ((((JTextField) variables.get(i).get(0)).getText().trim()).equalsIgnoreCase(var)){
					if (!(((((JComboBox) variables.get(i).get(1)).getSelectedItem()).toString()).equalsIgnoreCase("Not used"))){
						reqdVarsL.get(findReqdVarslIndex(((JTextField) variables.get(i).get(0)).getText().trim())).setEpsilon(Double.valueOf(((JTextField) variables.get(i).get(4)).getText()));
					}
				}
			}
		}
		else if (e.getSource() == user) { // System.out.println(" ITS USER SELECTED ");
		//   if (!firstRead) {
				try {	
					for (int i = 0; i < variables.size(); i++) { 

					String currentVar = ((JTextField) variables.get(i).get(0)).getText().trim();
					if (findReqdVarslIndex(currentVar) != -1){
						((JCheckBox) variables.get(i).get(2)).setEnabled(true);
						((JCheckBox) variables.get(i).get(3)).setEnabled(true);
						((JTextField) variables.get(i).get(4)).setEnabled(true);
						((JComboBox) variables.get(i).get(5)).setEnabled(true);
						((JComboBox) variables.get(i).get(6)).setEnabled(true);
						ArrayList<Double> iThresholds =  thresholds.get(currentVar);
						//System.out.println("iThresholds :"+iThresholds);
						if ((iThresholds == null) || ( iThresholds.size() == 0)){  	// This condition added later.. This ensures that when you switch from auto to user, the options of auto are written to the textboxes. SB.. rechk

							if (variables.get(i).get(variables.get(i).size()-1) instanceof JTextField
									&& ((JTextField) variables.get(i).get(variables.get(i).size()-1)).getText().trim().equals("")) { 

							} else if (variables.get(i).get(variables.get(i).size()-1) instanceof JTextField) {
								thresholds.put(currentVar,new ArrayList<Double>());
								thresh = ((JTextField) variables.get(i).get(variables.get(i).size()-1)).getText().trim();
								//System.out.println("thresh : "+thresh);
								String[] threshValues = thresh.split(",");
								//System.out.println("threshValues.length is : "+threshValues.length);
								for (int m=0; m<threshValues.length; m++){
									thresholds.get(currentVar).add(Double.parseDouble(threshValues[m]));
									//System.out.println("m is : "+m);
									//System.out.println("thresh : "+variables.get(i).get(m));	
								}
								//System.out.println("Thresholds :"+thresholds);
							}

						}
						((JCheckBox) variables.get(i).get(7)).setEnabled(true);
					}
					else{
						((JCheckBox) variables.get(i).get(2)).setEnabled(false);
						((JCheckBox) variables.get(i).get(3)).setEnabled(false);
						((JTextField) variables.get(i).get(4)).setEnabled(false);
						((JComboBox) variables.get(i).get(5)).setEnabled(false);
						((JComboBox) variables.get(i).get(6)).setEnabled(false);
						((JCheckBox) variables.get(i).get(7)).setEnabled(false);
					}
					//						write.write("\n");
					}
					//					write.close();
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(Gui.frame,
							"Unable to save thresholds!",
							"Error saving thresholds", JOptionPane.ERROR_MESSAGE);
				}
			//}
			numBinsLabel.setEnabled(false);
			numBins.setEnabled(false);
			suggest.setEnabled(true);
			// levelsBin();
			variablesPanel.revalidate();
			variablesPanel.repaint();
			levels(); // To be added later? if the scaled divisions are not supposed to be shown after auto to user switching, then should have something like divsB4scaling which should be passed as a parameter to 
		} else if (e.getSource() == auto) {
			numBinsLabel.setEnabled(true);
			numBins.setEnabled(true);
			suggest.setEnabled(false);
			//int j = 0;  // recheck .. SB
			for (Component c : variablesPanel.getComponents()) {
				for (int i = 6; i < ((JPanel) c).getComponentCount(); i++) { // changed 1 to 2 SB
					((JPanel) c).getComponent(i).setEnabled(false);
				}
				
			}
		} else if (e.getSource() == suggest) {
			suggestIsSource = true;
			autogen(false);
			//levels();
			variablesPanel.revalidate();
			variablesPanel.repaint();
			int j = 0;
			for (Component c : variablesPanel.getComponents()) {
				if (j == 0){   // recheck .. SB
					j++;		// SB
					continue;	// SB
				}				//SB
				String currentVar = ((JTextField)((JPanel) c).getComponent(0)).getText().trim();
				int ind = findReqdVarslIndex(currentVar);
				if (ind != -1){		//this code shouldn't be required ideally. 
					if (reqdVarsL.get(ind).isInput()){ //tempPorts.get(j-1)){
						((JComboBox)((JPanel) c).getComponent(1)).setSelectedItem("Input"); // SB // changed 1 to 2 after required
					} else {
						((JComboBox)((JPanel) c).getComponent(1)).setSelectedItem("Output"); // SB // changed 1 to 2 after required
					}
					if (reqdVarsL.get(ind).isDestab()){ //tempPorts.get(j-1)){
						((JCheckBox)((JPanel) c).getComponent(2)).setSelected(true); // SB // changed 1 to 2 after required
					} else {
						((JCheckBox)((JPanel) c).getComponent(2)).setSelected(false); // SB // changed 1 to 2 after required
					}
					if (reqdVarsL.get(ind).isCare()){ //tempPorts.get(j-1)){
						((JCheckBox)((JPanel) c).getComponent(3)).setSelected(true); // SB // changed 1 to 2 after required
					}
					if (reqdVarsL.get(ind).isInterpolate()){ //tempPorts.get(j-1)){
						((JCheckBox)((JPanel) c).getComponent(7)).setSelected(true); // SB // changed 1 to 2 after required
					} else {
						((JCheckBox)((JPanel) c).getComponent(7)).setSelected(false); // SB // changed 1 to 2 after required
					}
				}
				j++;
			}
			variablesPanel.revalidate();
			variablesPanel.repaint();
		} 
		// if the browse initial network button is clicked
		// else if (e.getSource() == browseInit) {
		// Buttons.browse(this, new File(initNetwork.getText().trim()),
		// initNetwork,
		// JFileChooser.FILES_ONLY, "Open");
		// }
		// if the run button is selected
		else if (e.getSource() == run) {
			if (!auto.isSelected()){  // SB
				save();
				learn();
			}
			else{
				learn();
			}
		} else if (e.getSource() == save) {
			save();
		} else if (e.getSource() == viewLhpn) {
			viewLPN();
		} else if (e.getSource() == viewLog) {
			viewLog();
		} else if (e.getSource() == saveLhpn) {
			saveLPN();
		} else if (e.getSource() == viewCoverage) {  // SB
			viewCoverage();
		} else if (e.getSource() == viewVHDL) {  // SB
			viewVHDL();
		} else if (e.getSource() == viewVerilog) {  // SB
			viewVerilog();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if (source == absTimeG) {
			absoluteTime = !absoluteTime; 
			if (e.getStateChange() == ItemEvent.DESELECTED){
				absTimeG.setSelected(false);
				runTimeG.setEnabled(false);
				runLengthG.setEnabled(true);
			}
			else{
				absTimeG.setSelected(true);
				runTimeG.setEnabled(true);
				runLengthG.setEnabled(false);
			}
		} else if (source == defaultEnvG) {
			if (e.getStateChange() == ItemEvent.DESELECTED){
				defaultEnvG.setSelected(false);
			}
			else{
				defaultEnvG.setSelected(true);
			}
		}
	}


	private void autogen(boolean readfile) {
		try {
			if (!readfile) { 
				if (variables !=null){
					for (int i = 0; i < variables.size(); i++) {	
						//for (int j = 7; j < variables.get(i).size(); j++) {  								// changed 2 to 3 SB 
						// TODO: Needs to be updated to extract thresholds from comma-separated field
						if (variables.get(i).get(variables.get(i).size()-1) instanceof JTextField
								&& ((JTextField) variables.get(i).get(variables.get(i).size()-1)).getText().trim().equals("")) {
						} else if (variables.get(i).get(variables.get(i).size()-1) instanceof JTextField) {
							thresh = ((JTextField) variables.get(i).get(variables.get(i).size()-1)).getText().trim();
							//System.out.println("thresh : "+thresh);
							String[] threshValues = thresh.split(",");
							int size = thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).size();
							if (thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).size() <= threshValues.length-size){ // changed 3 to 4 after required

								for (int m=threshValues.length-size; m<threshValues.length; m++){
									thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).add(Double.parseDouble(threshValues[m]));}
							}
							else{  
								//int size = thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).size();
								for (int m=threshValues.length-size; m<threshValues.length; m++){
									thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).set(m,Double.parseDouble(threshValues[m]));} // changed 3 to 4 after required
							}
						}
					}
				}
				generate = true;
				execute = false;
				LearnThread = new Thread(this);
				LearnThread.start();
			}
		}catch (Exception e1) {
			e1.printStackTrace();
			levels();
		}
	}
	
	private void levels() {  
		if (directory.equals("")) return;
		variablesPanel.removeAll();
		this.variables = new ArrayList<ArrayList<Component>>();
		variablesPanel.setLayout(new GridLayout(allVars.size() + 1, 1));
		int max = 0;
		if (!thresholds.isEmpty()){
			for (String s : thresholds.keySet()){
				if (thresholds.get(s) != null) {
					max = Math.max(max, thresholds.get(s).size()+2);
				}
			}
		}
		JPanel label = new JPanel(new FlowLayout(FlowLayout.LEADING));
		label.add(new JLabel("Variables                   "));
		label.add(new JLabel("Port                  ")); 
		label.add(new JLabel("Ctrl")); 
		label.add(new JLabel("Care")); 
		label.add(new JLabel("Epsilon")); 
		label.add(new JLabel("Type                       ")); 
		label.add(new JLabel("# Bins "));
		label.add(new JLabel("Interpolate"));
		label.add(new JLabel("Levels              "));

		variablesPanel.add(label);
		int j = 0;
		for (String s : allVars) {
			j++;
			JPanel sp = new JPanel(new FlowLayout(FlowLayout.LEADING,0,0));
			ArrayList<Component> specs = new ArrayList<Component>();
			JTextField varsText = new JTextField(10);
			varsText.setText(s);
			specs.add(varsText);
			String[] options = { "Auto", "2", "3", "4", "5", "6", "7", "8", "16", "32"};
			JComboBox combo = new JComboBox(options);
			String[] portOptions = {"Not used", "Input", "Output"};
			JComboBox port = new JComboBox(portOptions);
			JCheckBox mode = new JCheckBox();					
			JCheckBox care = new JCheckBox(); 
			JCheckBox interpolate = new JCheckBox();
			//JCheckBox interpolate = new JCheckBox(); 
			JTextField epsilonTb = new JTextField(3);
			epsilonTb.setText(epsilonG.getText().trim()); 
			String[] dmvOptions = {"DMV", "Continuous", "Auto"};
			JComboBox dmv = new JComboBox(dmvOptions);
			port.addActionListener(this); 
			mode.addActionListener(this); 

			care.addActionListener(this); 
			dmv.addActionListener(this); 
			epsilonTb.addActionListener(this);
			interpolate.addActionListener(this); 
			port.setActionCommand("port" + s);
			mode.setActionCommand("mode" + s);
			care.setActionCommand("care" + s);
			dmv.setActionCommand("DMV" + s);
			interpolate.setActionCommand("Interpolate" + s);
			dmv.setSelectedItem("Auto");
			epsilonTb.setActionCommand("epsilon" + s);
			combo.setSelectedItem(numBins.getSelectedItem());
			specs.add(port);
			specs.add(mode);
			specs.add(care);

			specs.add(epsilonTb); 
			specs.add(dmv);
			specs.add(combo);
			specs.add(interpolate);
			((JComboBox) specs.get(1)).setSelectedItem("Not used");
			((JCheckBox) specs.get(2)).setEnabled(false);
			((JCheckBox) specs.get(3)).setEnabled(false);
			((JTextField) specs.get(4)).setEnabled(false);
			((JComboBox) specs.get(5)).setEnabled(false);
			((JComboBox) specs.get(6)).setEnabled(false);
			((JCheckBox) specs.get(7)).setEnabled(false);

			((JTextField) specs.get(0)).setEditable(false);
			sp.add(specs.get(0));
			sp.add(specs.get(1));
			sp.add(specs.get(2));  
			sp.add(specs.get(3));  
			sp.add(specs.get(4));
			sp.add(specs.get(5));
			sp.add(specs.get(6));
			sp.add(specs.get(7));
			if (findReqdVarslIndex(s) != -1){
				if (reqdVarsL.get((findReqdVarslIndex(s))).isInput())
					((JComboBox) specs.get(1)).setSelectedItem("Input");
				else
					((JComboBox) specs.get(1)).setSelectedItem("Output"); 
				((JCheckBox) specs.get(2)).setEnabled(true); 
				((JCheckBox) specs.get(3)).setEnabled(true); 
				((JTextField) specs.get(4)).setEnabled(true); 
				((JComboBox) specs.get(5)).setEnabled(true);
				((JComboBox) specs.get(6)).setEnabled(true);
				((JCheckBox) specs.get(7)).setEnabled(true);
				if (reqdVarsL.get(findReqdVarslIndex(s)).isDestab()){
					((JCheckBox) specs.get(2)).setSelected(true);
				}
				else{
					((JCheckBox) specs.get(2)).setSelected(false);
				}
				if (reqdVarsL.get(findReqdVarslIndex(s)).isCare()){
					((JCheckBox) specs.get(3)).setSelected(true);
				}
				else{
					((JCheckBox) specs.get(3)).setSelected(false);
				}
				if (reqdVarsL.get(findReqdVarslIndex(s)).getEpsilon()!= null){
					((JTextField) specs.get(4)).setText(String.valueOf(reqdVarsL.get(findReqdVarslIndex(s)).getEpsilon()));
				} else if (epsilonG.getText() != ""){
					((JTextField) specs.get(4)).setText(epsilonG.getText().trim());
				}
				if (!dmvDetectDone && !dmvStatusLoaded)
					((JComboBox) specs.get(5)).setSelectedItem("Auto");
				else{
					if (reqdVarsL.get(findReqdVarslIndex(s)).isDmvc()){
						((JComboBox) specs.get(5)).setSelectedItem("DMV");
					}
					else{
						((JComboBox) specs.get(5)).setSelectedItem("Continuous");
					}
				}
				if (reqdVarsL.get(findReqdVarslIndex(s)).isInterpolate()){
					((JCheckBox) specs.get(7)).setSelected(true);
				}
				else{
					((JCheckBox) specs.get(7)).setSelected(false);
				} 
			}
			((JComboBox) specs.get(6)).addActionListener(this); // changed 1 to 2 SB
			((JComboBox) specs.get(6)).setActionCommand("text" + j);// changed 1 to 2 SB
			this.variables.add(specs);
			/* TODO: fix here for new comma-separated thresholds. */
			if (!thresholds.isEmpty()) {
				if (findReqdVarslIndex(s) != -1){	//This condition added after adding allvarsL
					ArrayList<Double> div =  thresholds.get(s); 
					if ((div != null) && (div.size() > 0)){ //changed >= to >
						((JComboBox) specs.get(6)).setSelectedItem(String.valueOf(div.size()+1));
						String selected = (String) ((JComboBox) specs.get(6)).getSelectedItem();
						int combox_selected;
						if (!selected.equalsIgnoreCase("Auto"))
							combox_selected = Integer.parseInt(selected);
						else
							combox_selected = 0;
						StringBuilder builder = new StringBuilder(div.size()); 
						for (int o=0;o<div.size();o++){
							long app = (long) ((div.get(o))*valScaleFactor);
							builder.append((app/valScaleFactor));
							if (o!=div.size()-1)
								builder.append(",");
						}
						specs.add(new JTextField(builder.toString(),20)); 
						sp.add(specs.get(8)); 
						selected = (String) ((JComboBox) specs.get(6)).getSelectedItem();
						if (!selected.equalsIgnoreCase("Auto"))
							combox_selected = Integer.parseInt(selected);
						else
							combox_selected = 0;
						for (int i = combox_selected - 1; i < max - 2; i++) {// changed 1 to 2 SB
							sp.add(new JLabel());
						}
					}
					else{	

					}
					if (reqdVarsL.get((findReqdVarslIndex(s))).isInput())
						((JComboBox) specs.get(1)).setSelectedItem("Input"); 
					else
						((JComboBox) specs.get(1)).setSelectedItem("Output"); 
					((JCheckBox) specs.get(2)).setEnabled(true);
					((JCheckBox) specs.get(3)).setEnabled(true);
					((JTextField) specs.get(4)).setEnabled(true);
					((JComboBox) specs.get(5)).setEnabled(true);
					((JComboBox) specs.get(6)).setEnabled(true);
					((JCheckBox) specs.get(7)).setEnabled(true);
					if (reqdVarsL.get(findReqdVarslIndex(s)).isDestab()){ 
						((JCheckBox) specs.get(2)).setSelected(true);
					} else {
						((JCheckBox) specs.get(2)).setSelected(false);
					}
					if (reqdVarsL.get(findReqdVarslIndex(s)).isCare()){
						((JCheckBox) specs.get(3)).setSelected(true);
					}
					if (reqdVarsL.get(findReqdVarslIndex(s)).isInterpolate()){
						((JCheckBox) specs.get(7)).setSelected(true);
					}
					if (!dmvDetectDone && !dmvStatusLoaded)
						((JComboBox) specs.get(5)).setSelectedItem("Auto");
					else{
						if (reqdVarsL.get(findReqdVarslIndex(s)).isDmvc()){
							((JComboBox) specs.get(5)).setSelectedItem("DMV");
						}
						else{
							((JComboBox) specs.get(5)).setSelectedItem("Continuous");
						}
					}
				}
			} 

			else {
				if (findReqdVarslIndex(((JTextField) sp.getComponent(0)).getText().trim()) != -1){
					if (reqdVarsL.get((findReqdVarslIndex(s))).isInput())
						((JComboBox) specs.get(1)).setSelectedItem("Input"); 
					else
						((JComboBox) specs.get(1)).setSelectedItem("Output"); 
					((JCheckBox) specs.get(2)).setEnabled(true);
					((JCheckBox) specs.get(3)).setEnabled(true);
					((JTextField) specs.get(4)).setEnabled(true);
					((JComboBox) specs.get(5)).setEnabled(true);
					((JComboBox) specs.get(6)).setEnabled(true);
					((JCheckBox) specs.get(7)).setEnabled(true);
					if (reqdVarsL.get(findReqdVarslIndex(s)).isDestab()){
						((JCheckBox) specs.get(2)).setSelected(true);
					} else {
						((JCheckBox) specs.get(2)).setSelected(false);
					}
					if (reqdVarsL.get(findReqdVarslIndex(s)).isCare()){
						((JCheckBox) specs.get(3)).setSelected(true);
					}
					if (reqdVarsL.get(findReqdVarslIndex(s)).isInterpolate()){
						((JCheckBox) specs.get(7)).setSelected(true);
					}
					if (!dmvDetectDone && !dmvStatusLoaded)
						((JComboBox) specs.get(5)).setSelectedItem("Auto");
					else{
						if (reqdVarsL.get(findReqdVarslIndex(s)).isDmvc()){
							((JComboBox) specs.get(5)).setSelectedItem("DMV");
						}
						else{
							((JComboBox) specs.get(5)).setSelectedItem("Continuous");
						}
					}
				}	
				specs.add(new JTextField("",20));
				sp.add(specs.get(8));
				if (findReqdVarslIndex(((JTextField) sp.getComponent(0)).getText().trim()) != -1){
					((JTextField) specs.get(8)).setEnabled(true);
				}
				else{
					((JTextField) specs.get(8)).setEnabled(false);
				}

			}


			variablesPanel.add(sp);
		}
		variablesPanel.revalidate(); 
		variablesPanel.repaint(); 
	}

	private int findReqdVarslIndex(String s) {
		for (int i = 0; i < reqdVarsL.size() ; i++){
			if (s.equalsIgnoreCase(reqdVarsL.get(i).getName())){
				return i;
			}
		}
		return -1;
	}

	public void saveLPN() {
		try {
			String copy = JOptionPane.showInputDialog(Gui.frame,
					"Enter Circuit Name:", "Save Circuit",
					JOptionPane.PLAIN_MESSAGE);
			if (copy != null) {
				copy = copy.trim();
			} else {
				return;
			}
			if (!copy.equals("")) {
				if (copy.length() > 1) {
					if (copy.length() < 4 || !copy.substring(copy.length() - 4).equals(".lpn")) {
						copy += ".lpn";
					}
				} else {
					copy += ".lpn";
				}
			}
			gui.saveLPN(copy, directory + separator + lhpnFile);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to save model.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewLPN() {
		try {
			File work = new File(directory);
			if (new File(directory + separator + lhpnFile).exists()) {
				String dotFile = lhpnFile.replace(".lpn", ".dot");
				File dot = new File(directory + separator + dotFile);
				dot.delete();
				LhpnFile lhpn = new LhpnFile(log);
				lhpn.load(directory + separator + lhpnFile);
				lhpn.printDot(directory + separator + dotFile);
				//log.addText("Executing:\n" + "atacs -cPllodpl " + lhpnFile);
				Runtime exec = Runtime.getRuntime();
				//Process load = exec.exec("atacs -cPllodpl " + lhpnFile, null,
				//		work);
				//load.waitFor();
				if (dot.exists()) {
					viewLhpn.setEnabled(true);
					Preferences biosimrc = Preferences.userRoot();
					String command = biosimrc.get("biosim.general.graphviz", "");
					command = command + " " + dotFile;
					log.addText(command + " " + directory + separator + dotFile + "\n");
					exec.exec(command, null, work);
				} else {
					File log = new File(directory + separator + "atacs.log");
					BufferedReader input = new BufferedReader(new FileReader(
							log));
					String line = null;
					JTextArea messageArea = new JTextArea();
					while ((line = input.readLine()) != null) {
						messageArea.append(line);
						messageArea
						.append(System.getProperty("line.separator"));
					}
					input.close();
					messageArea.setLineWrap(true);
					messageArea.setWrapStyleWord(true);
					messageArea.setEditable(false);
					JScrollPane scrolls = new JScrollPane();
					scrolls.setMinimumSize(new Dimension(500, 500));
					scrolls.setPreferredSize(new Dimension(500, 500));
					scrolls.setViewportView(messageArea);
					JOptionPane.showMessageDialog(Gui.frame, scrolls,
							"Log", JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"No circuit has been generated yet.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to view LPN Model.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewLog() {
		try {
			if (new File(directory + separator + "run.log").exists()) {
				File log = new File(directory + separator + "run.log");
				BufferedReader input = new BufferedReader(new FileReader(log));
				String line = null;
				JTextArea messageArea = new JTextArea();
				while ((line = input.readLine()) != null) {
					messageArea.append(line);
					messageArea.append(System.getProperty("line.separator"));
				}
				input.close();
				messageArea.setLineWrap(true);
				messageArea.setWrapStyleWord(true);
				messageArea.setEditable(false);
				JScrollPane scrolls = new JScrollPane();
				scrolls.setMinimumSize(new Dimension(500, 500));
				scrolls.setPreferredSize(new Dimension(500, 500));
				scrolls.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scrolls,
						"Run Log", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"No run log exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to view run log.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// SB

	public static void viewLearnComplete() {
		JFrame learnComplete = new JFrame("LEMA");
		learnComplete.setResizable(false);
		JPanel all = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html><b>Learning Completed</b></html>",SwingConstants.CENTER);
		all.add(label, BorderLayout.CENTER);
		Dimension screenSize;
		learnComplete.setContentPane(all);
		learnComplete.setMinimumSize(new Dimension(300,140));
		learnComplete.pack();
		try {
			Toolkit tk = Toolkit.getDefaultToolkit();
			screenSize = tk.getScreenSize();
		} catch (AWTError awe) {
			screenSize = new Dimension(640, 480);
		}
		Dimension frameSize = learnComplete.getSize();

		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		int x = screenSize.width / 2 - frameSize.width / 2;
		int y = screenSize.height / 2 - frameSize.height / 2;
		learnComplete.setLocation(x, y);
		learnComplete.setVisible(true);
		//	learnComplete.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	public void viewCoverage() {
		try {
			if (new File(directory + separator + "run.cvg").exists()) {
				File cvgRpt = new File(directory + separator + "run.cvg");
				BufferedReader input = new BufferedReader(new FileReader(cvgRpt));
				String line = null;
				JTextArea messageArea = new JTextArea();
				while ((line = input.readLine()) != null) {
					messageArea.append(line);
					messageArea.append(System.getProperty("line.separator"));
				}
				input.close();
				messageArea.setLineWrap(true);
				messageArea.setWrapStyleWord(true);
				messageArea.setEditable(false);
				JScrollPane scrolls = new JScrollPane();
				scrolls.setMinimumSize(new Dimension(500, 500));
				scrolls.setPreferredSize(new Dimension(500, 500));
				scrolls.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scrolls,
						"Coverage Report", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"No Coverage Report exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to view Coverage Report.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// SB
	public void viewVHDL() {
		try {
			String vhdFile = lhpnFile.replace(".lpn", ".vhd");
			if (new File(directory + separator + vhdFile).exists()) {
				File vhdlAmsFile = new File(directory + separator + vhdFile);
				BufferedReader input = new BufferedReader(new FileReader(vhdlAmsFile));
				String line = null;
				JTextArea messageArea = new JTextArea();
				while ((line = input.readLine()) != null) {
					messageArea.append(line);
					messageArea.append(System.getProperty("line.separator"));
				}
				input.close();
				messageArea.setLineWrap(true);
				messageArea.setWrapStyleWord(true);
				messageArea.setEditable(false);
				JScrollPane scrolls = new JScrollPane();
				scrolls.setMinimumSize(new Dimension(800, 500));
				scrolls.setPreferredSize(new Dimension(800, 500));
				scrolls.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scrolls,
						"VHDL-AMS Model", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"VHDL-AMS model does not exist.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to view VHDL-AMS model.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// SB
	public void viewVerilog() {
		try {
			String vamsFileName = lhpnFile.replace(".lpn", ".sv");
			if (new File(directory + separator + vamsFileName).exists()) {
				File vamsFile = new File(directory + separator + vamsFileName);
				BufferedReader input = new BufferedReader(new FileReader(vamsFile));
				String line = null;
				JTextArea messageArea = new JTextArea();
				while ((line = input.readLine()) != null) {
					messageArea.append(line);
					messageArea.append(System.getProperty("line.separator"));
				}
				input.close();
				messageArea.setLineWrap(true);
				messageArea.setWrapStyleWord(true);
				messageArea.setEditable(false);
				JScrollPane scrolls = new JScrollPane();
				scrolls.setMinimumSize(new Dimension(800, 500));
				scrolls.setPreferredSize(new Dimension(800, 500));
				scrolls.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scrolls,
						"Verilog-AMS Model", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"Verilog-AMS model does not exist.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to view Verilog-AMS model.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void save() {
		/* TODO: update for new threshold field */
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(new File(directory + separator + lrnFile));
			prop.load(in);
			in.close();
			prop.setProperty("learn.file", seedLpnFile);
			prop.setProperty("learn.iter", this.iteration.getText().trim());
			prop.setProperty("learn.valueScaling", this.globalValueScaling.getText().trim());
			prop.setProperty("learn.delayScaling", this.globalDelayScaling.getText().trim());
			prop.setProperty("learn.bins", (String) this.numBins.getSelectedItem());
			prop.setProperty("learn.prop", this.propertyG.getText().trim());
			String varsList = null;
			if (range.isSelected()) {
				prop.setProperty("learn.equal", "range");
			} else {
				prop.setProperty("learn.equal", "points");
			}
			if (auto.isSelected()) {
				prop.setProperty("learn.use", "auto");
				int k = 0;  // added later .. so that the exact divisions are stored to file when auto is selected. & not the divisions in the textboxes
				int inputCount = 0, dmvCount = 0, contCount = 0, autoVarCount = 0, dontcareCount = 0, destabCount = 0, interpolateCount=0;
				String ip = null, dmv = null, cont = null, autoVar = null, dontcares = null, destab = null, interpolateVars=null;
				String selected = this.numBins.getSelectedItem().toString();
				//int numOfBins = Integer.parseInt(this.numBins.getSelectedItem().toString());
				int numOfBins;
				if (!selected.equalsIgnoreCase("Auto"))
					numOfBins = Integer.parseInt(selected);
				else
					numOfBins = 0;
				//int numThresholds = numOfBins -1;
				for (Component c : variablesPanel.getComponents()) {
					if (k == 0){
						k++;
						continue;
					}
					if (!((((JComboBox)((JPanel)c).getComponent(1)).getSelectedItem().toString()).equalsIgnoreCase("Not used"))){ // added after required
						if (varsList == null){ // k==1
							varsList = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
						}
						else{
							varsList += " "+ ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
						}
						String currentVar = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
						String s =  currentVar + " " + numOfBins;
						if ((thresholds != null) && (thresholds.size() != 0)){ 
							for (int i = 0; i < (numOfBins -1); i++){
								if ((thresholds.get(currentVar)!= null) && (thresholds.get(currentVar).size() > i)){
									s += " ";
									s += thresholds.get(currentVar).get(i);
								}
							}
						}
						prop.setProperty("learn.bins"+ currentVar, s);
						if ((((JComboBox)((JPanel)c).getComponent(1)).getSelectedItem().toString()).equalsIgnoreCase("Input")){  // changed 1 to 2 after required
							if (inputCount == 0){
								inputCount++;
								ip = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								ip = ip + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if (((JCheckBox)((JPanel)c).getComponent(2)).isSelected()){  // changed 1 to 2 after required
							if (destabCount == 0){
								destabCount++;
								destab = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								destab = destab + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if (!(((JCheckBox)((JPanel)c).getComponent(3)).isSelected())){  
							if (dontcareCount == 0){
								dontcareCount++;
								dontcares = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								dontcares = dontcares + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if (!(((JTextField)((JPanel)c).getComponent(4)).getText().trim().equalsIgnoreCase(""))){
							String e = ((JTextField)((JPanel)c).getComponent(4)).getText().trim();
							prop.setProperty("learn.epsilon"+ ((JTextField)((JPanel)c).getComponent(0)).getText().trim(), e);
						}
						if (((String)(((JComboBox)((JPanel)c).getComponent(5)).getSelectedItem())).equals("DMV")){
							if (dmvCount == 0){
								dmvCount++;
								dmv = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								dmv = dmv + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if (((String)(((JComboBox)((JPanel)c).getComponent(5)).getSelectedItem())).equals("Continuous")){ 
							if (contCount == 0){
								contCount++;
								cont = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								cont = cont + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if (((String)(((JComboBox)((JPanel)c).getComponent(5)).getSelectedItem())).equals("Auto")){ 
							if (autoVarCount == 0){
								autoVarCount++;
								autoVar = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								autoVar = autoVar + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if ((((JCheckBox)((JPanel)c).getComponent(7)).isSelected())){  
							if (interpolateCount == 0){
								interpolateCount++;
								interpolateVars = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								interpolateVars = interpolateVars + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
					}
					k++;
				}
				if (inputCount != 0){
					prop.setProperty("learn.inputs", ip);
				}
				else{
					prop.remove("learn.inputs");
				}
				if (destabCount != 0){
					prop.setProperty("learn.destabs", ip);
				}
				else{
					prop.remove("learn.destabs");
				}
				if (dontcareCount != 0){
					prop.setProperty("learn.dontcares", dontcares);
				}
				else{
					prop.remove("learn.dontcares");
				}
				if (interpolateCount != 0){
					prop.setProperty("learn.interpolate", interpolateVars);
				}
				else{
					prop.remove("learn.interpolate");
				}
				if (dmvCount != 0)
					prop.setProperty("learn.dmv", dmv);
				else
					prop.remove("learn.dmv");
				if (contCount != 0)
					prop.setProperty("learn.cont", cont);
				else
					prop.remove("learn.cont");
				if (autoVarCount != 0)
					prop.setProperty("learn.autoVar", autoVar);
				else
					prop.remove("learn.autoVar");
			} else {
				prop.setProperty("learn.use", "user");
				int k = 0;
				int inputCount = 0, dmvCount = 0, contCount = 0, autoVarCount = 0, dontcareCount = 0, destabCount = 0, interpolateCount=0;
				String ip = null, dmv = null, cont = null, autoVar = null, dontcares = null, destab = null, interpolateVars=null;
				for (Component c : variablesPanel.getComponents()) {
					if (k == 0){
						k++;
						continue;
					}
					if (!((((JComboBox)((JPanel)c).getComponent(1)).getSelectedItem().toString()).equalsIgnoreCase("Not used"))){
						if (varsList == null) { //(k == 1){
							varsList = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
						}
						else{
							varsList += " "+ ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
						}
						String selected = (String)((JComboBox)((JPanel)c).getComponent(6)).getSelectedItem();  // changed to 3 after required
						int numOfBins;
						if (!selected.equalsIgnoreCase("Auto"))
							numOfBins = Integer.parseInt(selected)-1; // added -1; chk if issues.
						else
							numOfBins = 0;
						String s =  ((JTextField)((JPanel)c).getComponent(0)).getText().trim() + " " + numOfBins;  // changed to 3 after required
						//int numOfBins = Integer.parseInt((String)((JComboBox)((JPanel)c).getComponent(6)).getSelectedItem())-1; // changed to 3 after required
						if (numOfBins > 0) {
							s += " " + ((JTextField)(((JPanel)c).getComponent(8))).getText().trim().replace(",", " ");
						}
						//for (int i = 0; i < numOfBins; i++){ 
						//	s += " ";
						//	s += ((JTextField)(((JPanel)c).getComponent(i+7))).getText().trim();// changed to 4 after required
						//}
						prop.setProperty("learn.bins"+ ((JTextField)((JPanel)c).getComponent(0)).getText().trim(), s);
						if ((((JComboBox)((JPanel)c).getComponent(1)).getSelectedItem().toString()).equalsIgnoreCase("Input")){  // changed 1 to 2 after required
							if (inputCount == 0){
								inputCount++;
								ip = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								ip = ip + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if (((JCheckBox)((JPanel)c).getComponent(2)).isSelected()){  // changed 1 to 2 after required
							if (destabCount == 0){
								destabCount++;
								destab = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								destab = destab + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if ((((JCheckBox)((JPanel)c).getComponent(7)).isSelected())){  
							
							if (interpolateCount == 0){
								interpolateCount++;
								interpolateVars = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								interpolateVars = interpolateVars + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if (!(((JCheckBox)((JPanel)c).getComponent(3)).isSelected())){
							if (dontcareCount == 0){
								dontcareCount++;
								dontcares = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();//((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								dontcares = dontcares + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if (!(((JTextField)((JPanel)c).getComponent(4)).getText().trim().equalsIgnoreCase(""))){
							String e = ((JTextField)((JPanel)c).getComponent(4)).getText().trim();
							prop.setProperty("learn.epsilon"+ ((JTextField)((JPanel)c).getComponent(0)).getText().trim(), e);
						}
						if (((String)(((JComboBox)((JPanel)c).getComponent(5)).getSelectedItem())).equals("DMV")){  // changed 1 to 2 after required
							if (dmvCount == 0){
								dmvCount++;
								dmv = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								dmv = dmv + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if (((String)(((JComboBox)((JPanel)c).getComponent(5)).getSelectedItem())).equals("Continuous")){  // changed 1 to 2 after required
							if (contCount == 0){
								contCount++;
								cont = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								cont = cont + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
						if (((String)(((JComboBox)((JPanel)c).getComponent(5)).getSelectedItem())).equals("Auto")){ 
							if (autoVarCount == 0){
								autoVarCount++;
								autoVar = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								autoVar = autoVar + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
						}
					}
					k++;
				}
				if (inputCount != 0){
					prop.setProperty("learn.inputs", ip);
				}
				else{
					prop.remove("learn.inputs");
				}
				if (dontcareCount != 0){
					prop.setProperty("learn.dontcares", dontcares);
				}
				else{
					prop.remove("learn.dontcares");
				}
				if (interpolateCount != 0){
					prop.setProperty("learn.interpolate", interpolateVars);
				}
				else{
					prop.remove("learn.interpolate");
				}
				if (dmvCount != 0)
					prop.setProperty("learn.dmv", dmv);
				else
					prop.remove("learn.dmv");
				if (contCount != 0)
					prop.setProperty("learn.cont", cont);
				else
					prop.remove("learn.cont");
				if (autoVarCount != 0)
					prop.setProperty("learn.autoVar", autoVar);
				else
					prop.remove("learn.autoVar");

			}
			prop.setProperty("learn.epsilon", this.epsilonG.getText().trim());
			prop.setProperty("learn.pathLengthBin", this.pathLengthBinG.getText().trim());
			prop.setProperty("learn.pathLengthVar", this.pathLengthVarG.getText().trim());
			prop.setProperty("learn.rateSampling", this.rateSamplingG.getText().trim());
			prop.setProperty("learn.percent", this.percentG.getText().trim());
			prop.setProperty("learn.absTime",String.valueOf(this.absTimeG.isSelected()));
			prop.setProperty("learn.runTime",this.runTimeG.getText().trim());
			prop.setProperty("learn.runLength",this.runLengthG.getText().trim());
			prop.setProperty("learn.defaultEnv",String.valueOf(this.defaultEnvG.isSelected()));
			if (varsList != null){
				prop.setProperty("learn.varsList",varsList);
			}
			else{
				prop.remove("learn.varsList");
			}

			log.addText("Saving learn parameters to file:\n" + directory
					+ separator + lrnFile + "\n");
			FileOutputStream out = new FileOutputStream(new File(directory + separator + lrnFile));
			prop.store(out, seedLpnFile);
			out.close();
			
			change = false;
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to save parameter file!", "Error Saving File",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void reload(String newname) {
		backgroundField.setText(newname);
	}

	
	public void learn() {
		// TODO: needs to be update for new thresholds 
		try {
			if (auto.isSelected()) {
				for (int i = 0; i < variables.size(); i++) {

					if (variables.get(i).get(variables.get(i).size()-1) instanceof JTextField
							&&(((JTextField) variables.get(i).get(variables.get(i).size()-1)).getText().trim().equals(""))) {
					} else if (variables.get(i).get(variables.get(i).size()-1) instanceof JTextField) { 
						
						String currentVar = ((JTextField) variables.get(i).get(0)).getText().trim();
						//System.out.println("Current Var :"+currentVar);
						thresholds.put(currentVar,new ArrayList<Double>());
						thresh = ((JTextField) variables.get(i).get(variables.get(i).size()-1)).getText().trim();
						
						String[] threshValues = thresh.split(",");
						//System.out.println("threshvalues.length : "+threshValues.length);
						for (int m=0; m<threshValues.length; m++){
							thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).add(Double.parseDouble(threshValues[m]));
							//System.out.println("thresh : "+variables.get(i).get(m));	
						}
					}
				}
				generate = true; 
				
			} else {  
				
				for (int i = 0; i < variables.size(); i++) {
					
					if ((variables.get(i).get(variables.get(i).size()-1) instanceof JTextField
							&&((JTextField) variables.get(i).get(8)).getText().trim().equals(""))) {
						
					} else if ((variables.get(i).get(variables.get(i).size()-1) instanceof JTextField)) { 
						
						//int size = thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).size();
						String currentVar = ((JTextField) variables.get(i).get(0)).getText().trim();
						thresh = ((JTextField) variables.get(i).get(variables.get(i).size()-1)).getText().trim();
						
						String[] threshValues = thresh.split(",");
						thresholds.put(currentVar,new ArrayList<Double>());
						for (int m=0; m<threshValues.length; m++){
							thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).add(Double.parseDouble(threshValues[m]));
						}
					}

				}
				generate = false;
				
			}
			execute = true;
			LearnThread = new Thread(this);
			LearnThread.start();
		} 
		catch (NullPointerException e1) {
			e1.printStackTrace();
			System.out.println("Some problem with thresholds hashmap");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Some problem");
		}
	}
	

	@Override
	public void run() {
		/* TODO: need to update for new thresholds */
		if (reqdVarsL.size()<=0) {
			JOptionPane.showMessageDialog(Gui.frame,"No variables selected.",
					"Model Generation Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		new File(directory + separator + lhpnFile).delete();
		fail = false;
		try {
			//File work = new File(directory);
			final JFrame running = new JFrame("Progress");
			//running.setUndecorated(true);
			final JButton cancel = new JButton("Cancel");
			running.setResizable(false);
			WindowListener w = new WindowListener() {
				@Override
				public void windowClosing(WindowEvent arg0) {
					//	cancel.doClick();
					running.dispose();
				}

				@Override
				public void windowOpened(WindowEvent arg0) {
				}

				@Override
				public void windowClosed(WindowEvent arg0) {
				}

				@Override
				public void windowIconified(WindowEvent arg0) {
				}

				@Override
				public void windowDeiconified(WindowEvent arg0) {
				}

				@Override
				public void windowActivated(WindowEvent arg0) {
				}

				@Override
				public void windowDeactivated(WindowEvent arg0) {
				}
			};
			running.addWindowListener(w);
			JPanel text = new JPanel();
			JPanel progBar = new JPanel();
			JPanel button = new JPanel();
			JPanel all = new JPanel(new BorderLayout());
			JLabel label = new JLabel("Running...");
			JProgressBar progress = new JProgressBar();
			progress.setIndeterminate(true);
			// progress.setStringPainted(true);
			// progress.setString("");
			progress.setValue(0);
			text.add(label);
			progBar.add(progress);
			button.add(cancel);
			all.add(text, "North");
			all.add(progBar, "Center");
			all.add(button, "South");
			running.setContentPane(all);
			running.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = running.getSize();
			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			running.setLocation(x, y);
			running.setVisible(true);
			running.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			logFile = new File(directory + separator + "run.log");
			logFile.createNewFile();
			out = new BufferedWriter(new FileWriter(logFile));
			cancel.setActionCommand("Cancel");
			cancel.addActionListener(new ActionListener() {
				@Override
				@SuppressWarnings("deprecation")
				public void actionPerformed(ActionEvent e) {
					running.setCursor(null);
					running.dispose();
					if (LearnThread != null) {
						LearnThread.stop();
					}
					//throw new RuntimeException();
					//TODO: Need to kill thread somehow???
				}
			});
			HashMap<String,Double> tPar = getThreshPar(); //reqdVarsL should be correct b4 this call
			if (generate) {
				out.write("Running autoGenT\n");
				thresholds = autoGenT(running);
				
				if (!execute) {
					levels();
				}
				else{ //added later.. for saving the autogenerated thresholds into learn file after generating thresholds & before running data2lhpn
					save();
				}
			}
			if (execute && !fail) {
				File lhpn = new File(directory + separator + lhpnFile);
				lhpn.delete();
				//	dataToLHPN(running);
				int moduleNumber = 0;
				String failProp = getProp();
				//	for (int k = 0; k < reqdVarsL.size(); k++){
				//		if ((reqdVarsL.get(k).getName().equalsIgnoreCase("muxsel")) || (reqdVarsL.get(k).getName().equalsIgnoreCase("dacsel")) || (reqdVarsL.get(k).getName().equalsIgnoreCase("clk")))
				//			reqdVarsL.get(k).setCare(false);
				//	}
				LearnModel l = new LearnModel();
				out.write("Sending the following thresholds for model generation \n");
				// Warn the user if the internal thresholds don't match those being 
				// displayed in the GUI. Useful for debugging.
				boolean warned = false;
				for (String st1 : thresholds.keySet()){ 
					out.write(st1 + " -> ");
					for (int i = 0; i < variables.size(); i++){
						String cVar = ((JTextField) variables.get(i).get(0)).getText().trim();
						if ((cVar.equalsIgnoreCase(st1)) && (((JComboBox) variables.get(i).get(6)).isEnabled())){ // System.out.println("st1 2:"+variables.get(i).get(0));
							//int combox_selected = Integer.parseInt((String) ((JComboBox) variables.get(i).get(6)).getSelectedItem()); // changed 2 to 3 after required
							String selected = (String) (((JComboBox) variables.get(i).get(6)).getSelectedItem()); 
							
							
							// changed 2 to 3 after required
							int combox_selected;
							if (!selected.equalsIgnoreCase("Auto"))
								combox_selected = Integer.parseInt(selected); // changed 2 to 3 after required
							else 
								combox_selected = 0;
							
							
							if (thresholds.get(st1).size() == (combox_selected -1)){
								thresh = ((JTextField) variables.get(i).get(variables.get(i).size()-1)).getText().trim();
								
							String[] threshValues = thresh.split(",");

							if (thresh!=null)	{
							//int size = thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).size();
							
								for (int j = 0; j < thresholds.get(st1).size(); j++){ 
									
									
									//if (!warned &&(thresholds.get(st1).get(j)!= null)&& (thresholds.get(st1).get(j)!= Double.parseDouble(threshValues[j]))){
								if (!warned &&(thresholds.get(st1).get(j)!= null)&& ((threshValues[j])!= null)){
									if  (thresholds.get(st1).get(j)!= Double.parseDouble(threshValues[j])){ //System.out.println("the error value is :");
										warned = true;
										out.write("WARNING: THRESHOLDS OF " + st1 + " NOT MATCHING THOSE IN THE GUI. WRONG!");
										JOptionPane.showMessageDialog(Gui.frame,
												"Thresholds of " + st1 + " not matching those displayed in the gui.",
												"WARNING!", JOptionPane.WARNING_MESSAGE);
									}
									}
								}}
							} else {
								if (!warned && (thresh!=null)){
									warned = true;
									out.write("WARNING: THRESHOLDS OF " + st1 + " NOT MATCHING THOSE IN THE GUI. WRONG!");
									JOptionPane.showMessageDialog(Gui.frame,
											"Thresholds of " + st1 + " not matching those displayed in the gui.",
											"WARNING!", JOptionPane.WARNING_MESSAGE);
								}
							}
							break;
						}
					}
					for (Double d : thresholds.get(st1)){
						out.write(d + ";"); //System.out.println("d :"+d);
					}
					out.write("\n");
				}
				// Add destabilizing signals
				ArrayList <Variable> varsWithStables = new ArrayList <Variable>();
				for (Variable v : reqdVarsL){
					Variable var = new Variable("");
					var.copy(v);
					
					varsWithStables.add(var);
				}
				HashMap<String, ArrayList<String>> destabMap = new HashMap<String, ArrayList<String>>();
				ArrayList<String> destab_out = new  ArrayList<String>();
				for (Variable v2 : reqdVarsL){
					if ((v2.isInput()) && (v2.isDestab())){
						destab_out.add(v2.getName());
					}
				}
				if (destab_out.size() != 0){ 
					for (Variable v1 : reqdVarsL){
						if (!v1.isInput()){
							destabMap.put(v1.getName(), destab_out);
							Variable vStable = new Variable("stable");
							vStable.setCare(true); 
							vStable.setDmvc(true);
							vStable.setInput(true);
							vStable.setOutput(false);
							vStable.forceDmvc(true);
							vStable.setEpsilon(0.1); // since stable is always 0 or 1 and threshold is 0.5. even epsilon of 0.3 is fine
							varsWithStables.add(vStable);
							ArrayList<Double> tStable = new ArrayList<Double>();
							tStable.add(0.5);
							thresholds.put("stable", tStable);
						}
					}
				}
				LhpnFile g = l.learnModel(directory, log, gui, moduleNumber, thresholds, tPar, varsWithStables, destabMap, false, false, false, valScaleFactor, delayScaleFactor, failProp);
				
				// the false parameter above says that it's not generating a net for stable
				if (new File(seedLpnFile).exists()){ //directory + separator + "complete.lpn").exists()){//
					LhpnFile seedLpn = new LhpnFile();
					seedLpn.load(seedLpnFile);
					g = mergeLhpns(seedLpn,g);
				}
				valScaleFactor = l.getValueScaleFactor();
				delayScaleFactor = l.getDelayScaleFactor();
				globalValueScaling.setText(Double.toString(valScaleFactor));
				globalDelayScaling.setText(Double.toString(delayScaleFactor));
				levels();
				if (auto.isSelected()) {
					auto.doClick();
				}
				change=true;
				
				boolean defaultStim = defaultEnvG.isSelected();
				if (defaultStim){
					int j = 0;
					//stables = new ArrayList<String>();
					destabMap = new HashMap<String, ArrayList<String>>();
					for (Variable v : reqdVarsL){
						if (v.isInput()){
							j++;
							ArrayList <Variable> varsT = new ArrayList <Variable>();
							Variable input = new Variable("");
							input.copy(v);
							input.setInput(false);
							input.setOutput(true);
							input.setCare(true);
							varsT.add(input);
							l = new LearnModel();
							//LhpnFile moduleLPN = l.learnModel(directory, log, biosim, j, thresholds, tPar, varsT, destabMap, false, false, valScaleFactor, delayScaleFactor, null);
							LhpnFile moduleLPN = l.learnModel(directory, log, gui, j, thresholds, tPar, varsT, destabMap, false, false, false, valScaleFactor, delayScaleFactor, null);
							// new Lpn2verilog(directory + separator + lhpnFile); //writeSVFile(directory + separator + lhpnFile);
							g = mergeLhpns(moduleLPN,g);
						}	
					}
				}

				g.save(directory + separator + lhpnFile);
				viewLog.setEnabled(true);
				//System.out.println(directory + separator + lhpnFile);
				if (new File(directory + separator + lhpnFile).exists()) {
					//	System.out.println(" exists \n");
					viewVHDL.setEnabled(true); 		// SB
					viewVerilog.setEnabled(true); 	// SB
					viewLhpn.setEnabled(true); 		// SB
					viewCoverage.setEnabled(true); 	// SB
					saveLhpn.setEnabled(true); 		// SB
					//viewLearnComplete();			// SB
					/*
					JFrame learnComplete = new JFrame();
					JOptionPane.showMessageDialog(learnComplete,
							"Learning Complete.",
							"LEMA",
							JOptionPane.PLAIN_MESSAGE);
			        */
					//viewLhpn();
					gui.updateMenu(true,true);
				} else {
					//	System.out.println(" does not exist \n");
					viewVHDL.setEnabled(false); 	// SB
					viewVerilog.setEnabled(false); 	// SB
					viewLhpn.setEnabled(false); 	// SB
					viewCoverage.setEnabled(false); // SB
					saveLhpn.setEnabled(false); 	// SB
					fail = true;
					gui.updateMenu(true,false);
				}
			}
			out.close();
			running.setCursor(null);
			running.dispose();
			if (fail) {
				viewLog();
			} else {
				if (execute) saveLPN();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to create log file.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);

		} 
		/*catch (RuntimeException e1) {
			JOptionPane.showMessageDialog(BioSim.frame,
					"Learning was" + " canceled by the user.",
					"Canceled Learning", JOptionPane.ERROR_MESSAGE);
		} */
	}
	

	public HashMap<String,Double> getThreshPar(){
		HashMap<String,Double> tPar = new HashMap<String,Double>(); 
		try{
			int k = 0;
			for (Component c : variablesPanel.getComponents()) {
				if (k == 0){
					k++;
					continue;
				}
				String v = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
				if (findReqdVarslIndex(v) != -1){
					if (((JTextField)((JPanel)c).getComponent(4)).getText().trim().matches("[\\d]+\\.?[\\d]+?")){
						reqdVarsL.get(findReqdVarslIndex(v)).setEpsilon(Double.valueOf(((JTextField)((JPanel)c).getComponent(4)).getText().trim()));
						out.write("Epsilon is " + reqdVarsL.get(findReqdVarslIndex(v)).getEpsilon() + " for " + v + "\n");
					}
					else if (epsilonG.getText().matches("[\\d]+\\.?[\\d]+?")){
						reqdVarsL.get(findReqdVarslIndex(v)).setEpsilon(Double.parseDouble(epsilonG.getText().trim()));
						out.write("Can't parse epsilon for " + v + ". Using global one\n");
					}
					else {
						reqdVarsL.get(findReqdVarslIndex(v)).setEpsilon(0.1);
						out.write("Can't parse epsilon. Using default of 0.1\n");
					}
				}
			}
			if (epsilonG.getText().matches("[\\d]+\\.?[\\d]+?"))
				epsilon = Double.parseDouble(epsilonG.getText().trim());
			else {
				epsilon = 0.1;
				System.out.println("Can't parse epsilon. Using default\n");
			}
			if (rateSamplingG.getText().matches("[\\d]+"))
				rateSampling = Integer.parseInt(rateSamplingG.getText().trim());
			else{
				rateSampling = -1;
				out.write("Can't parse rateSampling. Using default\n");
			}
			if (pathLengthBinG.getText().matches("[\\d]+"))
				pathLengthBin = Integer.parseInt(pathLengthBinG.getText().trim());
			else{
				pathLengthBin = 0;
				System.out.println("Can't parse pathLengthBin. Using default\n");
			}
			if (pathLengthVarG.getText().matches("[\\d]+"))
				pathLengthVar = Integer.parseInt(pathLengthVarG.getText().trim());
			else{
				pathLengthVar = 0;
				System.out.println("Can't parse pathLengthVar. Using default\n");
			}
			if (percentG.getText().matches("[\\d]+\\.?[\\d]+?"))
				percent = Double.parseDouble(percentG.getText().trim());
			else{
				percent = 0.2;
				System.out.println("Can't parse percent. Using default\n");
			}
			if (runLengthG.getText().matches("[\\d]+"))
				runLength = Integer.parseInt(runLengthG.getText().trim());
			else{
				runLength = 30;
				System.out.println("Can't parse runLength. Using default\n");
			}
			if ((runTimeG.getText().matches("[\\d]+\\.?[\\d]+?")) || (runTimeG.getText().matches("[\\d]+\\.??[\\d]*?[e]??[-]??[\\d]+"))) 
				runTime = Double.parseDouble(runTimeG.getText().trim());
			else{
				runTime = 5e-6;
				System.out.println("Can't parse runTime. Using default\n");
			}
			absoluteTime = absTimeG.isSelected();
			if (globalValueScaling.getText().matches("[\\d]+\\.??[\\d]*")){
				valScaleFactor = Double.parseDouble(globalValueScaling.getText().trim());
				//System.out.println("valScaleFactor " + valScaleFactor);
			} else
				valScaleFactor = -1.0;
			if (globalDelayScaling.getText().matches("[\\d]+\\.??[\\d]*")){
				delayScaleFactor = Double.parseDouble(globalDelayScaling.getText().trim());
				//System.out.println("delayScaleFactor " + delayScaleFactor);
			} else
				delayScaleFactor = -1.0;
		/*	if ((unstableTimeG.getText().matches("[\\d]+\\.?[\\d]+?")) || (unstableTimeG.getText().matches("[\\d]+\\.??[\\d]*?[e]??[-]??[\\d]+"))) 
				unstableTime = Double.parseDouble(unstableTimeG.getText().trim());
			else{
				unstableTime = 5e-6;
				System.out.println("Can't parse unstableTime. Using default\n");
			}*/
			if ((stableToleranceG.getText().matches("[\\d]+\\.?[\\d]+?")) || (stableToleranceG.getText().matches("[\\d]+\\.??[\\d]*?[e]??[-]??[\\d]+"))) 
				stableTolerance = Double.parseDouble(stableToleranceG.getText().trim());
			else{
				stableTolerance = 0.02;
				System.out.println("Can't parse unstableTime. Using default\n");
			}
			out.write("epsilon = " + epsilon + "; ratesampling = " + rateSampling + "; pathLengthBin = " + pathLengthBin + "; percent = " + percent + "; runlength = " + runLength + "; runtime = " + runTime + "; absoluteTime = " + absoluteTime + "; delayscalefactor = " + delayScaleFactor + "; valuescalefactor = " + valScaleFactor + "; unstableTime = " + stableTolerance + "\n");
			tPar.put("epsilon", epsilon);
			tPar.put("pathLengthBin", Double.valueOf(pathLengthBin));
			tPar.put("pathLengthVar", Double.valueOf(pathLengthVar));
			tPar.put("rateSampling", Double.valueOf(rateSampling));
			tPar.put("percent", percent);
			if (absoluteTime)
				tPar.put("runTime", runTime);
			else
				tPar.put("runLength", Double.valueOf(runLength));
		//	tPar.put("unstableTime", unstableTime);
		tPar.put("stableTolerance", stableTolerance);
		} catch (IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to create log file.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		return tPar;
	}

	public String getProp(){
		String failProp = null;
		if (!(propertyG.getText()).equals("")){
			failProp = propertyG.getText().trim();
			failProp = "~(" + failProp + ")";
			failProp = failProp.replaceAll("\\.[0-9]*","");
		}
		return failProp;
	}
	
	public boolean hasChanged() {
		return change;
	}

	public boolean isComboSelected() {
		if (debug.isFocusOwner() || numBins.isFocusOwner()) {
			return true;
		}
		if (variables == null) {
			return false;
		}
		for (int i = 0; i < variables.size(); i++) {
			if (((JComboBox) variables.get(i).get(4)).isFocusOwner()) {  // changed 1 to 2 SB
				return true;
			}
		}
		return false;
	}

	public boolean getViewLhpnEnabled() {
		return viewLhpn.isEnabled();
	}

	public boolean getSaveLhpnEnabled() {
		return saveLhpn.isEnabled();
	}

	public boolean getViewLogEnabled() {
		return viewLog.isEnabled();
	}

	public boolean getViewCoverageEnabled() {   // SB
		return viewCoverage.isEnabled();
	}

	public boolean getViewVHDLEnabled() {   // SB
		return viewVHDL.isEnabled();
	}

	public boolean getViewVerilogEnabled() {   // SB
		return viewVerilog.isEnabled();
	}

	public void updateSpecies(String newLearnFile) {
		seedLpnFile = newLearnFile;
		variablesList = new ArrayList<String>();
		thresholds = new HashMap<String, ArrayList<Double>>(); // SB
		reqdVarsL = new ArrayList<Variable>();				// SB
		LhpnFile lhpn = new LhpnFile();
		lhpn.load(seedLpnFile);
		HashMap<String, Properties> variablesMap = lhpn.getContinuous();
		for (String s : variablesMap.keySet()) {
			variablesList.add(s);
			reqdVarsL.add(new Variable(s));					
			thresholds.put(s,new ArrayList<Double>());
		}
		Properties load = new Properties();
		try {
			FileInputStream in = new FileInputStream(new File(directory
					+ separator + lrnFile));
			load.load(in);
			in.close();
			//int j = 0;
			if (load.containsKey("learn.varsList")){
				String varsListString = load.getProperty("learn.varsList");
				String[] varsList = varsListString.split("\\s");
				//j = 0;
				for (String st1 : varsList){
					int varNum = findReqdVarslIndex(st1);
					if (varNum == -1){
						continue;
					}
					String s = load.getProperty("learn.bins" + st1);
					String[] savedBins = s.split("\\s");
					//variablesList.add(savedBins[0]);
					//	((JComboBox)(((JPanel)variablesPanel.getComponent(j+1)).getComponent(2))).setSelectedItem(savedBins[1]);
					for (int i = 2; i < savedBins.length ; i++){
						//		((JTextField)(((JPanel)variablesPanel.getComponent(j+1)).getComponent(i+1))).setText(savedBins[i]);
						if (varNum < variablesMap.size()) {	// chk for varNum or j ????
							thresholds.get(st1).add(Double.parseDouble(savedBins[i]));
						}
					}
					//j++;
				}
			}
			if (load.containsKey("learn.inputs")){
				String s = load.getProperty("learn.inputs");
				String[] savedInputs = s.split("\\s");
				for (String st1 : savedInputs){
					int ind = findReqdVarslIndex(st1); //after adding allVars 
					if (ind != -1){
						reqdVarsL.get(ind).setInput(true);
					} 
					/*for (int i = 0; i < reqdVarsL.size(); i++){//commented after adding allVars
						if ( reqdVarsL.get(i).getName().equalsIgnoreCase(st1)){
							reqdVarsL.get(i).setInput(true);
							break;
						}
					}*/
				}
			}
			if (load.containsKey("learn.destabs")){
				String s = load.getProperty("learn.destabs");
				String[] savedDestabs = s.split("\\s");
				for (String st1 : savedDestabs){
					int ind = findReqdVarslIndex(st1); //after adding allVars 
					if (ind != -1){
						reqdVarsL.get(ind).setDestab(true);
					}
					
				}
			}
			
			if (load.containsKey("learn.dontcares")){
				String s = load.getProperty("learn.dontcares");
				String[] savedDontCares = s.split("\\s");
				for (String st1 : savedDontCares){
					int ind = findReqdVarslIndex(st1); //after adding allVars 
					if (ind != -1){
						reqdVarsL.get(ind).setCare(false);
					}
					/*for (int i = 0; i < reqdVarsL.size(); i++){//commented after adding allVars
						if ( reqdVarsL.get(i).getName().equalsIgnoreCase(st1)){
							reqdVarsL.get(i).setInput(true);
							break;
						}
					}*/
				}
			}
			if (load.containsKey("learn.interpolateVars")){
				String s = load.getProperty("learn.interpolateVars");
				String[] savedInterpolateVars = s.split("\\s");
				for (String st1 : savedInterpolateVars){
					int ind = findReqdVarslIndex(st1); //after adding allVars 
					if (ind != -1){
						reqdVarsL.get(ind).setInterpolate(true);
					}
					/*for (int i = 0; i < reqdVarsL.size(); i++){//commented after adding allVars
						if ( reqdVarsL.get(i).getName().equalsIgnoreCase(st1)){
							reqdVarsL.get(i).setInput(true);
							break;
						}
					}*/
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to load properties file!",
				"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}
		/*
		 * try { FileWriter write = new FileWriter( new File(directory +
		 * separator + "background.gcm")); BufferedReader input = new
		 * BufferedReader(new FileReader(new File(seedLpnFile))); String line =
		 * null; while ((line = input.readLine()) != null) { write.write(line +
		 * "\n"); } write.close(); input.close(); } catch (Exception e) {
		 * JOptionPane.showMessageDialog(BioSim.frame, "Unable to create
		 * background file!", "Error Writing Background",
		 * JOptionPane.ERROR_MESSAGE); }
		 */
		sortVariables();
		if (user.isSelected()) {
			//		auto.doClick();			commented SB
			//		user.doClick();			commented SB
			numBinsLabel.setEnabled(false);	// SB
			numBins.setEnabled(false);		// SB
			suggest.setEnabled(true);		// SB
			variablesPanel.revalidate();	// SB	
			variablesPanel.repaint();		// SB
		} else {
			//		user.doClick();			commented SB
			//		auto.doClick();			commented SB
			numBinsLabel.setEnabled(true);	// SB
			numBins.setEnabled(true);		// SB
			suggest.setEnabled(false);		// SB
		}
		levels();
	}

	private void sortVariables() {
		int i, j;
		String index;
		//TODO: AllVars has to be sorted?? Is sorting required at all??
		for (i = 1; i < variablesList.size(); i++) {
			index = variablesList.get(i);
			j = i;
			while ((j > 0)
					&& variablesList.get(j - 1).compareToIgnoreCase(index) > 0) {
				variablesList.set(j, variablesList.get(j - 1));
				j = j - 1;
			}
			variablesList.set(j, index);
		}
		/*	Collections.sort(divisionsL, new Comparator<ArrayList<Double>>(){
			public int compare(ArrayList<Double> a, ArrayList<Double> b){
				int ind1 = divisionsL.indexOf(a);
				int ind2 = divisionsL.indexOf(b);
				String var1 = reqdVarsL.get(ind1).getName();
				String var2 = reqdVarsL.get(ind2).getName();
				return (reqdVarsL.get(divisionsL.indexOf(a)).compareTo(reqdVarsL.get(divisionsL.indexOf(b))));
			}
		});*/
		//TODO: SORTING OF thresholds NOT NECESSARY LIKE ABOVE ???
		Collections.sort(reqdVarsL);
		// sort divisionsL
	}

	public void setDirectory(String directory) {
		this.directory = directory;
		String[] getFilename = directory.split(separator);
		lrnFile = getFilename[getFilename.length - 1] + ".lrn";
	}

	private boolean isTransientPlace(String st1) {
		for (String s : transientNetPlaces.keySet()){
			if (st1.equalsIgnoreCase("p" + transientNetPlaces.get(s).getProperty("placeNum"))){
				return true;
			}
		}
		return false;
	}

	private boolean isTransientTransition(String st1) {
		for (String s : transientNetTransitions.keySet()){
			if (st1.equalsIgnoreCase("t" + transientNetTransitions.get(s).getProperty("transitionNum"))){
				return true;
			}
		}
		return false;
	}

	public void resetAll(){
		numPlaces = 0;
		numTransitions = 0;
		delayScaleFactor = 1.0;
		valScaleFactor = 1.0;
		for (Variable v: reqdVarsL){
			v.reset();
		}
	}

	public void findReqdVarIndices(){
		reqdVarIndices = new ArrayList<Integer>();
		for (int i = 0; i < reqdVarsL.size(); i++) {
			for (int j = 1; j < varNames.size(); j++) {
				if (reqdVarsL.get(i).getName().equalsIgnoreCase(varNames.get(j))) {
					reqdVarIndices.add(j);
				}
			}
		}
	}

	public void addInitPlace(HashMap<String,ArrayList<Double>> scaledThresholds){
		int initPlaceNum = numPlaces;
		g.addPlace("p" + numPlaces, true);
		numPlaces++;
		try{
			for (String st : transientNetPlaces.keySet()){
				g.addTransition("t" + numTransitions); // prevTranKey+key);
				g.addMovement("p" + initPlaceNum, "t" + numTransitions); 
				g.addMovement("t" + numTransitions, "p" + transientNetPlaces.get(st).getProperty("placeNum"));
				g.changeInitialMarking("p" + transientNetPlaces.get(st).getProperty("placeNum"), false);
				out.write("Added transition t" + numTransitions + " b/w initPlace and transient place p" + transientNetPlaces.get(st).getProperty("placeNum") + "\n");
				String[] binOutgoing = st.split(",");
				String condStr = "";
				for (int j = 0; j < reqdVarsL.size(); j++){
					String st2 = reqdVarsL.get(j).getName();
					if (reqdVarsL.get(j).isInput()){
						int bin = Integer.valueOf(binOutgoing[j]);
						if (bin == 0){
							if (!condStr.equalsIgnoreCase(""))
								condStr += "&";
							condStr += "~(" + st2 + ">=" + (int) Math.ceil(scaledThresholds.get(st2).get(bin).doubleValue()) + ")";
						} else if (bin == (scaledThresholds.get(st2).size())){
							if (!condStr.equalsIgnoreCase(""))
								condStr += "&";
							condStr += "(" + st2 + ">="	+ (int) Math.floor(scaledThresholds.get(st2).get(bin-1).doubleValue()) + ")";
						} else{
							if (!condStr.equalsIgnoreCase(""))
								condStr += "&";
							condStr += "(" + st2 + ">=" + (int) Math.floor(scaledThresholds.get(st2).get(bin-1).doubleValue()) + ")&~(" + st2 + ">=" + (int) Math.ceil(scaledThresholds.get(st2).get(bin).doubleValue()) + ")";
						}
					} else {
						if (reqdVarsL.get(j).isDmvc()){
							int minv = (int) Math.floor(Double.parseDouble(transientNetPlaces.get(st).getProperty(st2 + "_vMin")));
							int maxv = (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(st).getProperty(st2 + "_vMax"))); 
							if (minv != maxv)
								g.addIntAssign("t" + numTransitions,st2,"uniform(" + minv  + ","+ maxv + ")");
							else
								g.addIntAssign("t" + numTransitions,st2,String.valueOf(minv));
							out.write("Added assignment to " + st2 + " at transition t" + numTransitions + "\n");
						}
						// deal with rates for continuous here
					}
				}
				out.write("Changed enabling of t" + numTransitions + " to " + condStr + "\n");
				g.addEnabling("t" + numTransitions, condStr);
				numTransitions++;
			}
			for (HashMap<String,String> st1 : constVal){	// for output signals that are constant throughout the trace.
				if (st1.size() != 0){
					g.addTransition("t" + numTransitions); // prevTranKey+key);
					g.addMovement("p" + initPlaceNum, "t" + numTransitions);
					for (String st2: st1.keySet()){
						g.addIntAssign("t" + numTransitions,st2,st1.get(st2));
					}
					numTransitions++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Log file couldn't be opened in addInitPlace.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		} catch (NullPointerException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Null exception in addInitPlace.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void genBinsRates(HashMap<String, ArrayList<Double>> localThresholds) { 
		try{
			// generate bins
			reqdVarIndices = new ArrayList<Integer>();
			bins = new int[reqdVarsL.size()][data.get(0).size()];
			for (int i = 0; i < reqdVarsL.size(); i++) {
				for (int j = 1; j < varNames.size(); j++) {
					String currentVar = reqdVarsL.get(i).getName();
					if (currentVar.equalsIgnoreCase(varNames.get(j))) {
						reqdVarIndices.add(j);
						for (int k = 0; k < data.get(j).size(); k++) {
							ArrayList<Double> thresh = localThresholds.get(currentVar);
							bins[i][k] = getRegion(data.get(j).get(k),thresh);
							if ((k != 0) && (bins[i][k] != bins[i][k-1])){
								int length = 0;
								for (int m = k; m < data.get(j).size(); m++) {
									if (getRegion(data.get(j).get(m),thresh) == bins[i][k])
										length++;
									else 
										break;
								}
								if (length < pathLengthVar){
									out.write("Short bin for variable " + currentVar + " at " + data.get(0).get(k) + " until " + data.get(0).get(k+length-1) + " due to min pathLengthVar. Using " + bins[i][k-1] + " instead of " + bins[i][k] + " \n");
									for (int m = k; m < k+length; m++) {
										bins[i][m] = bins[i][k-1];
									}
								} else {
									for (int m = k; m < k+length; m++) {
										bins[i][m] = bins[i][k];
									}
								}
								k = k+length-1;
							}
						}
					}
				}
			}
			/*
			 * System.out.println("array bins is :"); for (int i = 0; i <
			 * reqdVarsL.size(); i++) { System.out.print(reqdVarsL.get(i).getName() + "
			 * "); for (int k = 0; k < data.get(0).size(); k++) {
			 * System.out.print(bins[i][k] + " "); } System.out.print("\n"); }
			 */
			// generate rates
			rates = new Double[reqdVarsL.size()][data.get(0).size()];
			values = new double[reqdVarsL.size()][data.get(0).size()];
			duration = new Double[data.get(0).size()];
			int mark, k, previous = 0; // indices of rates not same as that of the variable. if
			// wanted, then size of rates array should be varNames
			// not reqdVars
			if (rateSampling == -1) { // replacing inf with -1
				mark = 0;
				for (int i = 0; i < data.get(0).size(); i++) {
					if (i < mark) {
						continue;
					}
					while ((mark < data.get(0).size()) && (compareBins(i, mark))) {
						for (int j = 0; j < reqdVarsL.size(); j++){
							k = reqdVarIndices.get(j);
							values[j][i] = (values[j][i]*(mark-i) + data.get(k).get(mark))/(mark-i+1);
						}
						mark++;
					}
					if ((data.get(0).get(mark - 1) != data.get(0).get(i)) && ((mark - i) >=  pathLengthBin) && (mark != data.get(0).size())) { 	// && (mark != (data.get(0).size() - 1 condition added on nov 23.. to avoid the last region bcoz it's not complete. rechk
						if (!compareBins(previous,i)){
							for (int j = 0; j < reqdVarsL.size(); j++) {
								k = reqdVarIndices.get(j);
								rates[j][i] = ((data.get(k).get(mark - 1) - data.get(k).get(i)) / (data.get(0).get(mark - 1) - data.get(0).get(i)));
							}
							duration[i] = data.get(0).get(mark)	- data.get(0).get(i); // changed (mark - 1) to mark on may 28,2010
							previous = i;
						} else{	// There was a glitch and you returned to the same region
							for (int j = 0; j < reqdVarsL.size(); j++) {
								k = reqdVarIndices.get(j);
								rates[j][previous] = ((data.get(k).get(mark - 1) - data.get(k).get(previous)) / (data.get(0).get(mark - 1) - data.get(0).get(previous)));
							}
							duration[previous] = data.get(0).get(mark)	- data.get(0).get(previous); // changed (mark - 1) to mark on may 28,2010
						}
					} else if ((mark - i) <  pathLengthBin)  { // account for the glitch duration //
						out.write("Short bin at " + data.get(0).get(i) + " until " + data.get(0).get(mark) + " due to min pathLengthBin. This delay being added to " + previous + " \n");
						duration[previous] += data.get(0).get(mark)	- data.get(0).get(i); 
					} else if (data.get(0).get(mark - 1) == data.get(0).get(i)){ // bin with only one point. Added this condition on June 9,2010
						//Rates are meaningless here since the bin has just one point.
						//But calculating the rate because if it is null, then places won't be created in genBinsRates for one point bins.
						//Calculating rate b/w start point of next bin and start point of this bin
						out.write("Bin with one point at time " + data.get(0).get(i) + "\n");
						for (int j = 0; j < reqdVarsL.size(); j++) {
							k = reqdVarIndices.get(j);
							rates[j][i] = ((data.get(k).get(mark) - data.get(k).get(i)) / (data.get(0).get(mark) - data.get(0).get(i)));
						}
						duration[i] = data.get(0).get(mark)	- data.get(0).get(i); // changed (mark - 1) to mark on may 28,2010
						previous = i;
					}
				}
			} else { //TODO: This may have bugs in duration calculation etc.
				boolean calcRate;
				boolean prevFail = true;
				int binStartPoint = 0, binEndPoint = 0;
				for (int i = 0; i < (data.get(0).size() - rateSampling); i++) {
					calcRate = true;
					for (int l = 0; l < rateSampling; l++) {
						if (!compareBins(i, i + l)) {
							if (!prevFail){
								binEndPoint = i -2 + rateSampling;
								duration[binStartPoint] = data.get(0).get(binEndPoint)	- data.get(0).get(binStartPoint);
							}
							calcRate = false;
							prevFail = true;
							break;
						}
					}
					if (calcRate && (data.get(0).get(i + rateSampling) != data.get(0).get(i))) {
						for (int j = 0; j < reqdVarsL.size(); j++) {
							k = reqdVarIndices.get(j);
							rates[j][i] = ((data.get(k).get(i + rateSampling) - data.get(k).get(i)) / (data.get(0).get(i + rateSampling) - data.get(0).get(i)));
						}
						if (prevFail){
							binStartPoint = i;
						}
						prevFail = false;
					}
				}
				// commented on nov 23. don't need this. should avoid rate calculation too for this region. but not avoiding now.
				/*	if (!prevFail){ // for the last genuine rate-calculating region of the trace; this may not be required if the trace is incomplete.trace data may not necessarily end at a region endpt
						duration[binStartPoint] = data.get(0).get(data.get(0).size()-1)	- data.get(0).get(binStartPoint);
					}*/ 
			} //System.out.println("Rates :"+rates);
		} catch (NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Bins/Rates could not be generated. Please check thresholds.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);				
		} catch (IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Log file couldn't be opened for writing genBinsRates messages.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}

		}

	private static int getRegion(double value, ArrayList<Double> varThresholds){
		int bin = 0;
		for (int l = 0; l < varThresholds.size(); l++) {
			if (value <= varThresholds.get(l)) {
				bin = l;
				break;
			}
			bin = l + 1;
		}
		return bin;
	}

	public boolean compareBins(int j, int mark) {
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (bins[i][j] != bins[i][mark]) {
				return false;
			}
			continue;
		}
		return true;
	}

	public HashMap<String, ArrayList<Double>> detectDMV(ArrayList<ArrayList<Double>> data, Boolean callFromAutogen) {
		int startPoint, endPoint, mark, numPoints;
		HashMap<String, ArrayList<Double>> dmvDivisions = new HashMap<String, ArrayList<Double>>();
		double absTime;
		try{
			for (int i = 0; i < reqdVarsL.size(); i++) {
				if (reqdVarsL.get(i).isForcedCont()){
					reqdVarsL.get(i).setDmvc(false);
					out.write(reqdVarsL.get(i).getName() + " is forced to be continuous \n");
				} else {
					absTime = 0;
					mark = 0;
					DMVCrun runs = reqdVarsL.get(i).getRuns();
					runs.clearAll(); // flush all the runs from previous dat file.
					int lastRunPointsWithoutTransition = 0;
					Double lastRunTimeWithoutTransition = 0.0;
					Double lastRunValueWithoutTransition = null;
					if (!callFromAutogen) { // This flag is required because if the call is from autogenT, then data has just the reqdVarsL but otherwise, it has all other vars too. So reqdVarIndices not reqd when called from autogen
						for (int j = 0; j <= data.get(0).size(); j++) {
							if (j < mark) 
								continue;
							if (((j+1) < data.get(reqdVarIndices.get(i)).size()) && 
									Math.abs(data.get(reqdVarIndices.get(i)).get(j) - data.get(reqdVarIndices.get(i)).get(j + 1)) <= reqdVarsL.get(i).getEpsilon()) {
								startPoint = j;
								runs.addValue(data.get(reqdVarIndices.get(i)).get(j)); // chk carefully reqdVarIndices.get(i)
								while (((j + 1) < data.get(0).size()) && (bins[i][startPoint] == bins[i][j+1]) && (Math.abs(data.get(reqdVarIndices.get(i)).get(startPoint) - data.get(reqdVarIndices.get(i)).get(j + 1)) <= reqdVarsL.get(i).getEpsilon())) {       //checking of same bins[] condition added on May 11,2010.
									runs.addValue(data.get(reqdVarIndices.get(i)).get(j + 1)); // chk carefully reqdVarIndices.get(i)
									j++;
								}
								endPoint = j;
								if (!absoluteTime) {
									if ((endPoint < (data.get(0).size() - 1)) && ((endPoint - startPoint) + 1) >= runLength) {
										runs.addStartPoint(startPoint);
										runs.addEndPoint(endPoint);
									} else if (((endPoint - startPoint) + 1) >= runLength) {
										lastRunPointsWithoutTransition = endPoint - startPoint + 1;
										lastRunValueWithoutTransition = runs.getLastValue();
									} else {
										runs.removeValue();
									}
								} else {
									if ((endPoint < (data.get(0).size() - 1)) && (calcDelay(startPoint, endPoint) >= runTime)) {
										runs.addStartPoint(startPoint);
										runs.addEndPoint(endPoint);
										absTime += calcDelay(startPoint, endPoint);
									} else if (((endPoint - startPoint) + 1) >= runLength) {
										lastRunTimeWithoutTransition = calcDelay(startPoint, endPoint);
										lastRunValueWithoutTransition = runs.getLastValue();
									} else {
										runs.removeValue();
									}
								}
								mark = endPoint;
							}
						}
						numPoints = runs.getNumPoints();
						if (!reqdVarsL.get(i).isForcedDmv() && ((absoluteTime && (((absTime + lastRunTimeWithoutTransition)/ (data.get(0).get(data.get(0).size() - 1) - data
								.get(0).get(0))) < percent)) || (!absoluteTime && (((numPoints + lastRunPointsWithoutTransition)/ (double) data.get(0).size()) < percent)))){ // isForced condition added on may 28
							runs.clearAll();
							reqdVarsL.get(i).setDmvc(false);
							out.write(reqdVarsL.get(i).getName() + " is not a dmvc \n");
						} else {
							reqdVarsL.get(i).setDmvc(true);
							Double[] dmvcValues;
							if (lastRunValueWithoutTransition != null ){
								Double[] dVals = reqdVarsL.get(i).getRuns().getAvgVals(); 
								
								dmvcValues = new Double[dVals.length + 1];
								for (int j = 0; j < dVals.length; j++){
									dmvcValues[j] = dVals[j];
								}
								dmvcValues[dVals.length] = lastRunValueWithoutTransition;
							} else
								dmvcValues = reqdVarsL.get(i).getRuns().getAvgVals();
							Arrays.sort(dmvcValues);
							
							if (!dmvcValuesUnique.containsKey(reqdVarsL.get(i).getName()))
								dmvcValuesUnique.put(reqdVarsL.get(i).getName(),new Properties());
							out.write("DMV values of " + reqdVarsL.get(i).getName() + " are ");
							if (dmvcValues.length == 0){// constant value throughout the trace
								dmvcValues = new Double[1];
								dmvcValues[0]= reqdVarsL.get(i).getRuns().getConstVal();
							}
							for (int j = 0; j < dmvcValues.length; j++){
								if (dmvcValues[j] < thresholds.get(reqdVarsL.get(i).getName()).get(0)){
									dmvcValuesUnique.get(reqdVarsL.get(i).getName()).put("0",dmvcValues[j].toString());
									//System.out.println("For variable " + reqdVarsL.get(i).getName() + " value for bin 0 is " + dmvcValues[j] + "\n");
									System.out.print(" dmvc values :"+dmvcValuesUnique+"\n");
								}
								else if (dmvcValues[j] >= thresholds.get(reqdVarsL.get(i).getName()).get(thresholds.get(reqdVarsL.get(i).getName()).size() - 1)){
									dmvcValuesUnique.get(reqdVarsL.get(i).getName()).put(String.valueOf(thresholds.get(reqdVarsL.get(i).getName()).size()),dmvcValues[j].toString());
									//System.out.println("For variable " + reqdVarsL.get(i).getName() + " value for bin " + thresholds.get(reqdVarsL.get(i).getName()).size() + " is " + dmvcValues[j] + "\n");
									System.out.print(" dmvc values2 :"+dmvcValuesUnique+"\n");
								}
								else{
									for (int k = 0; k < thresholds.get(reqdVarsL.get(i).getName()).size()-1; k++){
										if ((dmvcValues[j] >= thresholds.get(reqdVarsL.get(i).getName()).get(k)) && (dmvcValues[j] < thresholds.get(reqdVarsL.get(i).getName()).get(k+1))){
											dmvcValuesUnique.get(reqdVarsL.get(i).getName()).put(String.valueOf(k+1),dmvcValues[j].toString());
											//System.out.println("For variable " + reqdVarsL.get(i).getName() + " value for bin " + String.valueOf(k+1) + " is " + dmvcValues[j] + "\n");
											System.out.print(" dmvc values :"+dmvcValuesUnique+"\n");
											break;
										}
									}
								}
								out.write(dmvcValues[j] + " ");
							}
							out.write(reqdVarsL.get(i).getName() + " is  a dmvc \n");
						}
					} else {
						getThreshPar(); 
						out.write("epsilon = " + epsilon + "; percent = " + percent + "; runlength = " + runLength + "; runtime = " + runTime + "; absoluteTime = " + absoluteTime + "\n");
						for (int j = 0; j <= data.get(0).size(); j++) {
							if (j < mark) // not reqd??
								continue;
							if (((j+1) < data.get(i+1).size()) && 
									Math.abs(data.get(i+1).get(j) - data.get(i+1).get(j + 1)) <= reqdVarsL.get(i).getEpsilon()) { //i+1 and not i bcoz 0th col is time
								startPoint = j;
								runs.addValue(data.get(i+1).get(j)); // chk carefully reqdVarIndices.get(i)
								while (((j + 1) < data.get(0).size()) && (Math.abs(data.get(i+1).get(startPoint) - data.get(i+1).get(j + 1)) <= reqdVarsL.get(i).getEpsilon())) {
									// VERY IMP: add condition data.get(0).get(startPoint) < data.get(0).get(j+1) to make sure that you don't run into the next data file.. 
									runs.addValue(data.get(i+1).get(j + 1)); // chk carefully reqdVarIndices.get(i)
									j++;
								}
								endPoint = j;
								if (!absoluteTime) {
									if ((endPoint < (data.get(0).size() - 1)) && ((endPoint - startPoint) + 1) >= runLength) {
										runs.addStartPoint(startPoint);
										runs.addEndPoint(endPoint);
									} else if (((endPoint - startPoint) + 1) >= runLength) {
										lastRunPointsWithoutTransition = endPoint - startPoint + 1;
										lastRunValueWithoutTransition = runs.getLastValue();
									} else {
										runs.removeValue();
									}
								} else {
									if ((endPoint < (data.get(0).size() - 1)) && (calcDelayWithData(startPoint, endPoint, data) >= runTime)) {
										runs.addStartPoint(startPoint);
										runs.addEndPoint(endPoint);
										absTime += calcDelayWithData(startPoint, endPoint, data);
									} else if (((endPoint - startPoint) + 1) >= runLength) {
										lastRunTimeWithoutTransition = calcDelayWithData(startPoint, endPoint, data);
										lastRunValueWithoutTransition = runs.getLastValue();
									} else {
										runs.removeValue();
									}
								}
								mark = endPoint;
							}
						}
						numPoints = runs.getNumPoints();
						if (!reqdVarsL.get(i).isForcedDmv() && ((absoluteTime && (((absTime + lastRunTimeWithoutTransition)/ (data.get(0).get(data.get(0).size() - 1) - data
								.get(0).get(0))) < percent)) || (!absoluteTime && (((numPoints + lastRunPointsWithoutTransition)/ (double) data.get(0).size()) < percent))))  {// isForced condition added on may 28
							runs.clearAll();
							reqdVarsL.get(i).setDmvc(false);
							out.write(reqdVarsL.get(i).getName() + " is not a dmvc \n");
						} else {
							reqdVarsL.get(i).setDmvc(true);
							Double[] dmvcValues;
							if (lastRunValueWithoutTransition != null ){
								Double[] dVals = reqdVarsL.get(i).getRuns().getAvgVals(); //for (int x=0; x<dVals.length; x++)  System.out.println("dVals :"+dVals[x]);
								dmvcValues = new Double[dVals.length + 1];
								for (int j = 0; j < dVals.length; j++){
									dmvcValues[j] = dVals[j];
								}
								dmvcValues[dVals.length] = lastRunValueWithoutTransition;
							} else
								dmvcValues = reqdVarsL.get(i).getRuns().getAvgVals();
							Arrays.sort(dmvcValues);
						//	System.out.println("Sorted DMV values of " + reqdVarsL.get(i).getName() + " are ");
							
							if (!dmvcValuesUnique.containsKey(reqdVarsL.get(i).getName()))
								dmvcValuesUnique.put(reqdVarsL.get(i).getName(),new Properties());
							ArrayList<Double> dmvSplits = new ArrayList<Double>();
							out.write("Final DMV values of " + reqdVarsL.get(i).getName() + " are ");
							int l = 0;
							for (int j = 0; j < dmvcValues.length; j++){
								dmvcValuesUnique.get(reqdVarsL.get(i).getName()).put(String.valueOf(l),dmvcValues[j].toString());
								out.write(dmvcValues[j] + ", ");
								if (dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() > 1){
									//System.out.print(" dmvc values :"+dmvcValuesUnique+"\n");
									Properties p3 = dmvcValuesUnique.get(reqdVarsL.get(i).getName());
									double d1 = (Double.valueOf(p3.getProperty(String.valueOf(p3.size() - 1))) + Double.valueOf(p3.getProperty(String.valueOf(p3.size() - 2))))/2.0;
									d1 = d1*10000;
									int d2 = (int) d1; 
									dmvSplits.add(d2/10000.0); // truncating to 4 decimal places
								}
								l++;
								for (int k = j+1; k < dmvcValues.length; k++){
									if (Math.abs((dmvcValues[j] - dmvcValues[k])) > reqdVarsL.get(i).getEpsilon()){
										j = k-1;
										break;
									}
									else if (k >= (dmvcValues.length -1)){
										j = k;
									}
								}	
							}
							out.write("\n");
							dmvDivisions.put(reqdVarsL.get(i).getName(), dmvSplits);
							out.write(reqdVarsL.get(i).getName() + " is  a dmvc in detectDMV. Final check will be done using the generated thresholds \n");
						}
					}
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Log file couldn't be opened for writing rates and bins.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		if (callFromAutogen)
			checkthresholds(data,dmvDivisions);
		dmvDetectDone = true;
		return(dmvDivisions);
	}

	public void checkthresholds(ArrayList<ArrayList<Double>> data, HashMap<String, ArrayList<Double>> localThresholds){
		int[][] bins = new int[reqdVarsL.size()][data.get(0).size()];
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (reqdVarsL.get(i).isDmvc()){
				String currentVar = reqdVarsL.get(i).getName();
				for (int k = 0; k < data.get(i+1).size(); k++) {
					for (int l = 0; l < localThresholds.get(currentVar).size(); l++) {
						if (data.get(i+1).get(k) <= localThresholds.get(currentVar).get(l)) {
							bins[i][k] = l;
							break;
						}
						bins[i][k] = l + 1; // indices of bins not same as that of the variable. i here. not j; if j
						// wanted, then size of bins array should be varNames not reqdVars
					}
				}
			}
		}
		int startPoint, endPoint, mark, numPoints;
		double absTime;
		try{
			for (int i = 0; i < reqdVarsL.size(); i++) {
				if (reqdVarsL.get(i).isDmvc()){ // recheck with new thresholds has to be done only for those variables which are detected as dmv already.
					absTime = 0;
					mark = 0;
					DMVCrun runs = reqdVarsL.get(i).getRuns();
					runs.clearAll(); // flush all the runs from previous dat file.
					int lastRunPointsWithoutTransition = 0;
					Double lastRunTimeWithoutTransition = 0.0;
					for (int j = 0; j <= data.get(0).size(); j++) {
						if (j < mark) // not reqd??
							continue;
						if (((j+1) < data.get(i+1).size()) && 
								Math.abs(data.get(i+1).get(j) - data.get(i+1).get(j + 1)) <= reqdVarsL.get(i).getEpsilon()) { //i+1 and not i bcoz 0th col is time
							startPoint = j;
							runs.addValue(data.get(i+1).get(j)); // chk carefully reqdVarIndices.get(i)
							while (((j + 1) < data.get(0).size()) && (bins[i][startPoint] == bins[i][j+1]) && (Math.abs(data.get(i+1).get(startPoint) - data.get(i+1).get(j + 1)) <= reqdVarsL.get(i).getEpsilon())) {
								// VERY IMP: add condition data.get(0).get(startPoint) < data.get(0).get(j+1) to make sure that you don't run into the next data file.. 
								runs.addValue(data.get(i+1).get(j + 1)); // chk carefully
								// reqdVarIndices.get(i)
								j++;
							}
							endPoint = j;
							if (!absoluteTime) {
								if ((endPoint < (data.get(0).size() - 1)) && ((endPoint - startPoint) + 1) >= runLength) {
									runs.addStartPoint(startPoint);
									runs.addEndPoint(endPoint);
								} else if (((endPoint - startPoint) + 1) >= runLength) {
									lastRunPointsWithoutTransition = endPoint - startPoint + 1;
								} else {
									runs.removeValue();
								}
							} else {
								if ((endPoint < (data.get(0).size() - 1)) && (calcDelayWithData(startPoint, endPoint, data) >= runTime)) {
									runs.addStartPoint(startPoint);
									runs.addEndPoint(endPoint);
									absTime += calcDelayWithData(startPoint, endPoint, data);
								} else if (((endPoint - startPoint) + 1) >= runLength) {
									lastRunTimeWithoutTransition = calcDelayWithData(startPoint, endPoint, data);
								} else {
									runs.removeValue();
								}
							}
							mark = endPoint;
						}
					}
					numPoints = runs.getNumPoints();
					if (!reqdVarsL.get(i).isForcedDmv() && ((absoluteTime && (((absTime + lastRunTimeWithoutTransition)/ (data.get(0).get(data.get(0).size() - 1) - data
							.get(0).get(0))) < percent)) || (!absoluteTime && (((numPoints + lastRunPointsWithoutTransition)/ (double) data.get(0).size()) < percent))))  {
						runs.clearAll();
						reqdVarsL.get(i).setDmvc(false);
						out.write("After checking with the generated thresholds " + reqdVarsL.get(i).getName() + " is not a dmv \n");
					} else {
						if (reqdVarsL.get(i).isForcedDmv())
							out.write(reqdVarsL.get(i).getName() + " is a forced dmv. So, generated thresholds not being checked \n");
						else
							out.write("After checking with the generated thresholds " + reqdVarsL.get(i).getName() + " is a dmv \n");
					}
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Log file couldn't be opened for writing rates and bins.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}

	public double calcDelay(int i, int j) {
		return (data.get(0).get(j) - data.get(0).get(i));
	}

	public static double calcDelayWithData(int i, int j, ArrayList<ArrayList<Double>> data) {
		return (data.get(0).get(j) - data.get(0).get(i));
	}

	public static void addValue(Properties p, String name, Double v) { 
		Double vMin;
		Double vMax;
		if ((p.getProperty(name + "_vMin") == null)
				&& (p.getProperty(name + "_vMax") == null)) {
			p.setProperty(name + "_vMin", v.toString());
			p.setProperty(name + "_vMax", v.toString());
			return;
		}
		vMin = Double.parseDouble(p.getProperty(name + "_vMin"));
		vMax = Double.parseDouble(p.getProperty(name + "_vMax"));
		if (v < vMin) {
			vMin = v;
		} else if (v > vMax) {
			vMax = v;
		}
		p.setProperty(name + "_vMin", vMin.toString());
		p.setProperty(name + "_vMax", vMax.toString());
	}

	public static void addRate(Properties p, String name, Double r) { 
		Double rMin;
		Double rMax;
		if ((p.getProperty(name + "_rMin") == null)
				&& (p.getProperty(name + "_rMax") == null)) {
			p.setProperty(name + "_rMin", r.toString());
			p.setProperty(name + "_rMax", r.toString());
			return;
		}
		rMin = Double.parseDouble(p.getProperty(name + "_rMin"));
		rMax = Double.parseDouble(p.getProperty(name + "_rMax"));
		if (r < rMin) {
			rMin = r;
		} else if (r > rMax) {
			rMax = r;
		}
		p.setProperty(name + "_rMin", rMin.toString());
		p.setProperty(name + "_rMax", rMax.toString());
	}

	public HashMap<String,ArrayList<Double>> scaleValue(Double scaleFactor, HashMap<String,ArrayList<Double>> localThresholds) {
		String place;
		Properties p;
		try{
			for (String st1 : g.getPlaceList()) {
				if (!isTransientPlace(st1)){
					place = getPlaceInfoIndex(st1);
					p = placeInfo.get(place);
				}
				else{
					place = getTransientNetPlaceIndex(st1);
					p = transientNetPlaces.get(place);
				}
				if (place != "failProp"){
					for (Variable v : reqdVarsL) {
						if (!v.isDmvc()) {
							p.setProperty(v.getName() + "_rMin", Double
									.toString(Double.parseDouble(p.getProperty(v.getName()
											+ "_rMin"))* scaleFactor));
							p.setProperty(v.getName() + "_rMax", Double
									.toString(Double.parseDouble(p.getProperty(v.getName()
											+ "_rMax"))* scaleFactor));
						} else {
							if (!v.isInput()) {
								p.setProperty(v.getName() + "_vMin", Double
										.toString(Double.parseDouble(p.getProperty(v.getName()
												+ "_vMin"))* scaleFactor));
								p.setProperty(v.getName() + "_vMax", Double
										.toString(Double.parseDouble(p.getProperty(v.getName()
												+ "_vMax")) * scaleFactor));
							}

						}
					}
				}
			}
			for (Variable v : reqdVarsL) {
				v.scaleInitByVar(scaleFactor);
				for (int j = 0; j < localThresholds.get(v.getName()).size(); j++) {
					localThresholds.get(v.getName()).set(j,localThresholds.get(v.getName()).get(j) * scaleFactor);
				}
			}
			for (HashMap<String,String> st1 : constVal){	// for output signals that are constant throughout the trace.
				for (String st2: st1.keySet()){
					st1.put(st2,String.valueOf(Double.valueOf(st1.get(st2))*scaleFactor));
				}
			}
		}
		catch (NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Not all regions have values for all dmv variables",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		return localThresholds;
	}

	public void scaleDelay(Double scaleFactor) {
		try{
			String place;
			Properties p;
			for (String t : g.getTransitionList()) {
				if ((g.getPreset(t) != null) && (g.getPostset(t) != null)){
					if (!isTransientTransition(t)){
						if ((placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
							String pPrev = g.getPreset(t)[0];
							String nextPlace = g.getPostset(t)[0];
							Double mind = Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMin"));
							Double maxd = Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMax"));
							transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).setProperty("dMin",Double.toString(mind*scaleFactor));
							transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).setProperty("dMax",Double.toString(maxd*scaleFactor));
						}
					} else {
						if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
							String pPrev = g.getPreset(t)[0];
							String nextPlace = g.getPostset(t)[0];
							Double mind = Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMin"));
							Double maxd = Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMax"));
							transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).setProperty("dMin",Double.toString(mind*scaleFactor));
							transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).setProperty("dMax",Double.toString(maxd*scaleFactor));
						}
					}
				}
			}
			for (String st1 : g.getPlaceList()) {
				if (!isTransientPlace(st1)){
					place = getPlaceInfoIndex(st1);
					p = placeInfo.get(place);
				}
				else{
					place = getTransientNetPlaceIndex(st1);
					p = transientNetPlaces.get(place);
				}
				if (place != "failProp"){
					for (Variable v : reqdVarsL) {
						if (!v.isDmvc()) {
							p.setProperty(v.getName() + "_rMin", Double
									.toString(Double.parseDouble(p.getProperty(v
											.getName() + "_rMin"))	/ scaleFactor));
							p.setProperty(v.getName() + "_rMax", Double
									.toString(Double.parseDouble(p.getProperty(v
											.getName() + "_rMax"))	/ scaleFactor));
						}
					}
				}
			}
			for (Variable v : reqdVarsL) {
				// if (!v.isDmvc()){ this if maynot be required.. rates do exist for dmvc ones as well.. since calculated before detectDMV
				v.scaleInitByDelay(scaleFactor);
				// }
			}
			// SEE IF RATES IN TRANSITIONS HAVE TO BE ADJUSTED HERE
		}
		catch(NullPointerException e){
			System.out.println("Delay scaling error due to null. Check");
		}
		catch(java.lang.ArrayIndexOutOfBoundsException e){
			System.out.println("Delay scaling error due to Array Index.");
		}
	}

	public static Double getMinDiv(HashMap<String, ArrayList<Double>> divisions) {
		Double minDiv = null;
		for (String s : divisions.keySet()) {
			if (minDiv == null){
				minDiv = divisions.get(s).get(0);
			}
			for (int j = 0; j < divisions.get(s).size(); j++) {
				if (divisions.get(s).get(j) < minDiv) {
					minDiv = divisions.get(s).get(j);
				}
			}
		}
		return minDiv;
	}

	public static Double getMaxDiv(HashMap<String, ArrayList<Double>> divisions) {
		Double maxDiv = null;
		for (String s : divisions.keySet()) {
			if (maxDiv == null){
				maxDiv = divisions.get(s).get(0);
			}
			for (int j = 0; j < divisions.get(s).size(); j++) {
				if (divisions.get(s).get(j) > maxDiv) {
					maxDiv = divisions.get(s).get(j);
				}
			}
		}
		return maxDiv;
	}

	public Double getMinRate() { // minimum of entire lpn
		Double minRate = null;
		for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if (p.getProperty("type").equals("RATE")) {
				for (Variable v : reqdVarsL) {
					if (!v.isDmvc()){
						if (minRate == null) {
							if (p.getProperty(v.getName() + "_rMin") != null) {
								minRate = Double.parseDouble(p.getProperty(v.getName() + "_rMin"));
							}
						} else if ((p.getProperty(v.getName() + "_rMin") != null)
								&& (Double.parseDouble(p.getProperty(v.getName() + "_rMin")) < minRate)
								&& (Double.parseDouble(p.getProperty(v.getName() + "_rMin")) != 0.0)) {
							minRate = Double.parseDouble(p.getProperty(v.getName() + "_rMin"));
						}
					}
				}
			}
		}
		return minRate;
	}

	public Double getMaxRate() {
		Double maxRate = null;
		for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if (p.getProperty("type").equals("RATE")) {
				for (Variable v : reqdVarsL) {
					if (!v.isDmvc()){
						if (maxRate == null) {
							if (p.getProperty(v.getName() + "_rMax") != null) {
								maxRate = Double.parseDouble(p.getProperty(v.getName() + "_rMax"));
							}
						} else if ((p.getProperty(v.getName() + "_rMax") != null)
								&& (Double.parseDouble(p.getProperty(v.getName()
										+ "_rMax")) > maxRate)
										&& (Double.parseDouble(p.getProperty(v.getName()
												+ "_rMax")) != 0.0)) {
							maxRate = Double.parseDouble(p.getProperty(v.getName()
									+ "_rMax"));
						}
					}
				}
			}
		}
		return maxRate;
	}

	public Double getMinDelay() {
		Double minDelay = null;
		Double mind;
		try{
			for (String t : g.getTransitionList()) {
				if ((g.getPreset(t) != null) && (g.getPostset(t) != null)){
					if (!isTransientTransition(t)){
						if ((placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
							String pPrev = g.getPreset(t)[0];
							String nextPlace = g.getPostset(t)[0];
							out.write("Transition " + g.getTransition(t).getLabel() + " b/w " + pPrev + " and " + nextPlace + " : finding delay \n");
							if (transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMin") != null){
								mind = Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMin"));
								if (minDelay == null)
									minDelay = mind;
								else if ((minDelay > mind) && (mind != 0))
									minDelay = mind;
							}
						}
					} else {
						if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
							String pPrev = g.getPreset(t)[0];
							String nextPlace = g.getPostset(t)[0];
							if (transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMin") != null){
								mind = Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMin"));
								if (minDelay == null)
									minDelay = mind;
								else if ((minDelay > mind) && (mind != 0))
									minDelay = mind;
							}
						}
					}
				}
			}
		} catch (IOException e){
			e.printStackTrace();
		}
		return minDelay;
	}

	public Double getMaxDelay() {
		Double maxDelay = null;
		Double maxd;
		for (String t : g.getTransitionList()) {
			if ((g.getPreset(t) != null) && (g.getPostset(t) != null)){
				if (!isTransientTransition(t)){
					if ((placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
							&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
						String pPrev = g.getPreset(t)[0];
						String nextPlace = g.getPostset(t)[0];
						if (transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMax") != null){
							maxd = Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMax"));
							if (maxDelay == null)
								maxDelay = maxd;
							else if ((maxDelay < maxd) && (maxd != 0))
								maxDelay = maxd;
						}
					}
				} else {
					if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
							&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
						String pPrev = g.getPreset(t)[0];
						String nextPlace = g.getPostset(t)[0];
						if (transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dmax") != null){
							maxd = Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dmax"));
							if (maxDelay == null)
								maxDelay = maxd;
							else if ((maxDelay < maxd) && (maxd != 0))
								maxDelay = maxd;
						}
					}
				}
			}
		}
		return maxDelay;
	}

	public static void addDuration(Properties p, Double d) {
		Double dMin;
		Double dMax;
		// d = d*(10^6);
		if ((p.getProperty("dMin") == null) && (p.getProperty("dMax") == null)) {
			// p.setProperty("dMin", Integer.toString((int)(Math.floor(d))));
			// p.setProperty("dMax", Integer.toString((int)(Math.floor(d))));
			p.setProperty("dMin", d.toString());
			p.setProperty("dMax", d.toString());
			return;
		}
		dMin = Double.parseDouble(p.getProperty("dMin"));
		dMax = Double.parseDouble(p.getProperty("dMax"));
		if (d < dMin) {
			dMin = d;
		} else if (d > dMax) {
			dMax = d;
		}
		p.setProperty("dMin", dMin.toString());
		p.setProperty("dMax", dMax.toString());
	}

	public String getPlaceInfoIndex(String s) {
		String index = null;
		for (String st2 : placeInfo.keySet()) {
			if (("p" + placeInfo.get(st2).getProperty("placeNum"))
					.equalsIgnoreCase(s)) {
				index = st2;
				break;
			}
		}
		return index;
	}

	public String getTransientNetPlaceIndex(String s) {
		String index = null;
		for (String st2 : transientNetPlaces.keySet()) {
			if (("p" + transientNetPlaces.get(st2).getProperty("placeNum"))
					.equalsIgnoreCase(s)) {
				index = st2;
				break;
			}
		}
		return index;
	}

	public static ArrayList<Integer> diff(String pre_bin, String post_bin) {
		ArrayList<Integer> diffL = new ArrayList<Integer>();
		String[] preset_encoding = pre_bin.split(",");
		String[] postset_encoding = post_bin.split(",");
		for (int j = 0; j < preset_encoding.length; j++) { // to account for "" being created in the array
			if (Integer.parseInt(preset_encoding[j]) != Integer.parseInt(postset_encoding[j])) {
				diffL.add(j);// to account for "" being created in the array
			}
		}
		return (diffL);
	}

	public int getMinRate(String place, String name) {
		Properties p = placeInfo.get(place);
		return ((int) Math.floor(Double.parseDouble(p.getProperty(name
				+ "_rMin"))));
		// return(rMin[i]);
	}

	public int getMaxRate(String place, String name) {
		Properties p = placeInfo.get(place);
		return ((int) Math.floor(Double.parseDouble(p.getProperty(name
				+ "_rMax"))));
		// return(rMin[i]);
	}

	public HashMap <String, Double[]> getDataExtrema(ArrayList<ArrayList<Double>> data){
		HashMap <String, Double[]> extrema = new HashMap <String, Double[]>();
		for (int i=0; i<reqdVarsL.size(); i++){
			//Object obj = Collections.min(data.get(reqdVarIndices.get(i)));
			Object obj = Collections.min(data.get(i+1));
			//System.out.println("Object :"+obj);
			extrema.put(reqdVarsL.get(i).getName(),new Double[2]);
			extrema.get(reqdVarsL.get(i).getName())[0] = Double.parseDouble(obj.toString());
			//obj = Collections.max(data.get(reqdVarIndices.get(i)));
			obj = Collections.max(data.get(i+1));
			extrema.get(reqdVarsL.get(i).getName())[1] = Double.parseDouble(obj.toString());
		}
		return extrema;
	}

	public HashMap<String, ArrayList<Double>> initDivisions(HashMap<String,Double[]> extrema){
		// this method won't be called in auto case.. so dont worry?
		int numThresholds;
		if (numBins.getSelectedItem().toString().equalsIgnoreCase("Auto"))
			numThresholds = -1;
		else
			numThresholds = Integer.parseInt(numBins.getSelectedItem().toString()) - 1;
		double interval;
		HashMap<String, ArrayList<Double>> localThresholds = new HashMap<String, ArrayList<Double>> ();
		for (int i = 0; i < reqdVarsL.size(); i++){
			localThresholds.put(reqdVarsL.get(i).getName(),new ArrayList<Double>());
			if (!suggestIsSource){ // could use user.isselected instead of this.
				//numThresholds = Integer.parseInt(numBins.getSelectedItem().toString()) - 1;
			}
			else{
				for (int j = 1; j < variablesPanel.getComponentCount(); j++){
					if ((((JTextField)((JPanel)variablesPanel.getComponent(j)).getComponent(0)).getText().trim()).equalsIgnoreCase(reqdVarsL.get(i).getName())){
						if (((String)((JComboBox)((JPanel)variablesPanel.getComponent(j)).getComponent(6)).getSelectedItem()).equalsIgnoreCase("Auto"))
							numThresholds = -1;
						else
							numThresholds = Integer.parseInt((String)((JComboBox)((JPanel)variablesPanel.getComponent(j)).getComponent(6)).getSelectedItem())-1; // changed 2 to 3 after required
						break;
					}
				}
			}
			if (numThresholds != -1){
				interval = (Math.abs(extrema.get(reqdVarsL.get(i).getName())[1] - extrema.get(reqdVarsL.get(i).getName())[0]))/(numThresholds + 1);
				//System.out.println("Interval for variable : "+reqdVarsL.get(i).getName()+" is:"+interval);
				for (int j = 0; j< numThresholds; j++){
					if (localThresholds.get(reqdVarsL.get(i).getName()).size() <= j){
						localThresholds.get(reqdVarsL.get(i).getName()).add(extrema.get(reqdVarsL.get(i).getName())[0] + interval*(j+1));  // j+1
					}
					else{
						localThresholds.get(reqdVarsL.get(i).getName()).set(j,extrema.get(reqdVarsL.get(i).getName())[0] + interval*(j+1)); // j+1
					}
				}
			}
			//System.out.println("thresholds in between :"+localThresholds.get("net_c2"));
		}
		suggestIsSource = false;
		return localThresholds;
	}

	/**
	 * This method generates thresholds for the variables in the learn view. 
	 * If auto generate option is selected, then the number of thresholds for
	 * all the variables is same as the number selected in the number of bins.
	 * If user generation option is selected & suggest button is pressed, then
	 * the number of thresholds for each variable is equal to the number selected
	 * in the combobox against that variable in the variablesPanel.
	 * 
	 * Rev. 1 - Scott Little (autogenT.py) 
	 * Rev. 2 - Satish Batchu ( autogenT() ) -- Aug 12, 2009
	 */

	

	public HashMap<String,ArrayList<Double>> autoGenT(JFrame running){
		//int iterations = Integer.parseInt(iteration.getText());
		ArrayList<ArrayList<Double>> fullData = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> singleFileData = new ArrayList<ArrayList<Double>>();
		HashMap<String, ArrayList<Double>> localThresholds = new HashMap<String, ArrayList<Double>>();
		int i = 1;
		try{
			while (new File(directory + separator + "run-" + i + ".tsd").exists()) {
				TSDParser tsd = new TSDParser(directory + separator + "run-" + i +".tsd", false);
				singleFileData = tsd.getData();
				varNames = tsd.getSpecies();
				if (i == 1){
					fullData.add(new ArrayList<Double>());
				}
				(fullData.get(0)).addAll(singleFileData.get(0));
				for (int k = 0; k < reqdVarsL.size(); k++){
					boolean found = false;
					for (int j = 0; j< singleFileData.size(); j++){
						if (reqdVarsL.get(k).getName().equalsIgnoreCase(varNames.get(j))) {
							if (i == 1){
								fullData.add(new ArrayList<Double>());
							}
							(fullData.get(k+1)).addAll(singleFileData.get(j));
							found = true;
							break;
						}
					}
					if (!found){
						out.write("variable " + reqdVarsL.get(k).getName() + " is required var somehow. But its data is not present. Adding empty array.\n");
						fullData.add(new ArrayList<Double>());
					}
				}
				i++;
			}
			int numThresholds = -1;
			findReqdVarIndices(); // not required in this case
			dmvcValuesUnique = new HashMap<String, Properties>();
			HashMap<String, ArrayList<Double>> dmvDivs = detectDMV(fullData,true); 
			
			HashMap<String, Integer> varThresholds = new HashMap<String, Integer>();
			if (!suggestIsSource){
				
				out.write("suggest is not the source\n");
				String selected = numBins.getSelectedItem().toString();
				
				if (!selected.equalsIgnoreCase("Auto"))
					numThresholds = Integer.parseInt(selected) - 1;
				else 
					numThresholds = -1;
				
				//numThresholds = Integer.parseInt(numBins.getSelectedItem().toString()) - 1;	//after adding 0 to comboboxes
				if (numThresholds == -1){
					for (String k : dmvDivs.keySet()){
						for (int l = 0; l < reqdVarsL.size(); l++){
							if (!reqdVarsL.get(l).isDmvc()){
								JOptionPane.showMessageDialog(Gui.frame,
										"Can't generate the number of thresholds for continuous variables.",
										"ERROR!", JOptionPane.ERROR_MESSAGE);
								out.write(reqdVarsL.get(l).getName() + " is not a dmv. So.. ");
								out.write("ERROR! Can't generate the number of thresholds for continuous variables.");
								out.close();
								running.setCursor(null);
								running.dispose();
								return(localThresholds);
							}
							out.write(reqdVarsL.get(l).getName() + " is a dmv in autogenT.");
							if (k.equalsIgnoreCase(reqdVarsL.get(l).getName())){
								localThresholds.put(k,dmvDivs.get(k));
							}
						}
					}
				} else {
					out.write("auto generate. All variables get " + numThresholds + " threholds \n");
				}
			}
			else{
				out.write("suggest is the source\n");
				for (int k = 0; k < reqdVarsL.size(); k++){
					for (int j = 1; j < variablesPanel.getComponentCount(); j++){
						if ((((JTextField)((JPanel)variablesPanel.getComponent(j)).getComponent(0)).getText().trim()).equalsIgnoreCase(reqdVarsL.get(k).getName())){
							String selected = (String) (((JComboBox) ((JPanel)variablesPanel.getComponent(j)).getComponent(6)).getSelectedItem()); // changed 2 to 3 after required
							if (!selected.equalsIgnoreCase("Auto"))
								numThresholds = Integer.parseInt(selected) - 1; // changed 2 to 3 after required
							else 
								numThresholds = -1;
							varThresholds.put(reqdVarsL.get(k).getName(),Integer.valueOf(numThresholds));
							if (numThresholds == -1){
								if (!reqdVarsL.get(k).isDmvc()){
									JOptionPane.showMessageDialog(Gui.frame,
											"Can't generate the number of thresholds for continuous variables.",
											"ERROR!", JOptionPane.ERROR_MESSAGE);

									out.write(reqdVarsL.get(k).getName() + " is not a dmv. So.. ");
									out.write("ERROR! Can't generate the number of thresholds for continuous variables.");
									out.close();
									running.setCursor(null);
									running.dispose();
									return(localThresholds);
								}
								//else {
									//localThresholds.put(reqdVarsL.get(k).getName(),dmvDivs.get(reqdVarsL.get(k).getName()));
								//}
								out.write("saving auto generated thresholds for " + reqdVarsL.get(k).getName() + " whether or not it is a dmv\n");
								localThresholds.put(reqdVarsL.get(k).getName(),dmvDivs.get(reqdVarsL.get(k).getName()));
							} else {
								out.write(reqdVarsL.get(k).getName() + " is a dmv in autogenT.");
							}
							break;
						}	
					}
				}	
			}
			if ((suggestIsSource && (Collections.max(varThresholds.values()) == -1)) || (!suggestIsSource && (numThresholds == -1))){
				
			}
			else if (suggestIsSource && !(Collections.max(varThresholds.values()) == -1)) {
				extrema = getDataExtrema(fullData); //CHANGE THIS TO HASHMAP for replacing divisionsL by threholds
				setExtrema(extrema);
				//Double[] name = extrema.get("net_c2");
				//for(int m=0; m<name.length;m++){
				//System.out.println("values for extrema for varible net_c2 is :"+name[m]);
				//} 
				
				//Double[] name2 = extrema.get("vout");
				//for(int m=0; m<name2.length;m++){
				//System.out.println("values for extrema for varible vout is :"+name2[m]);
			//	}
				
				//divisions = initDivisions(extrema,divisions);
				localThresholds = initDivisions(extrema);
				System.out.println("local 1 :"+localThresholds);
				//localThresholds = greedyOpt(localThresholds,fullData,extrema);   // commented it on March 26th 2011, not required
				//System.out.println("local 2 :"+localThresholds);
				// Overwriting dmv divisions calculated above with those that come from detectDMV here.
				
				for (int l = 0; l < reqdVarsL.size(); l++){
					for (String k : dmvDivs.keySet()){  
					if ((k.equalsIgnoreCase(reqdVarsL.get(l).getName())) && (varThresholds.get(k) == -1)){ 
					
							localThresholds.get(reqdVarsL.get(l).getName()).clear();
							localThresholds.get(reqdVarsL.get(l).getName()).addAll(dmvDivs.get(k));
							
						}
					
					}
				}
			}
			else{
				HashMap<String, Double[]> extrema = getDataExtrema(fullData); //CHANGE THIS TO HASHMAP for replacing divisionsL by threholds
				
				//divisions = initDivisions(extrema,divisions);
				localThresholds = initDivisions(extrema);
				
				//localThresholds = greedyOpt(localThresholds,fullData,extrema);
				
				// Overwriting dmv divisions calculated above with those that come from detectDMV here.
				
				for (int l = 0; l < reqdVarsL.size(); l++){
					for (String k : dmvDivs.keySet()){ 	
					
					if (k.equalsIgnoreCase(reqdVarsL.get(l).getName())){ 	
							localThresholds.get(reqdVarsL.get(l).getName()).clear();
							localThresholds.get(reqdVarsL.get(l).getName()).addAll(dmvDivs.get(k));
							
						}
					
					}
				}
		
	}//
		}
		catch(NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to calculate rates.\nThresholds could not be generated\nWindow size or pathLengthBin must be reduced.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
			running.setCursor(null);
			running.dispose();
		} catch (IOException e2) {
			e2.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to write into log file in autogenT.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		} catch (NoSuchElementException e3) {
			e3.printStackTrace();
			if (reqdVarsL.size() == 0){
				JOptionPane.showMessageDialog(Gui.frame,
						"No input or output variables.",
						"ERROR!", JOptionPane.ERROR_MESSAGE);
			}

		}
		return localThresholds;
	}
	
	private HashMap<String,ArrayList<Double>> getExtremes(JFrame running){
	//int iterations = Integer.parseInt(iteration.getText());
	ArrayList<ArrayList<Double>> fullData = new ArrayList<ArrayList<Double>>();
	ArrayList<ArrayList<Double>> singleFileData = new ArrayList<ArrayList<Double>>();
	HashMap<String, ArrayList<Double>> localThresholds = new HashMap<String, ArrayList<Double>>();
	int i = 1;
	try{
		while (new File(directory + separator + "run-" + i + ".tsd").exists()) {
			TSDParser tsd = new TSDParser(directory + separator + "run-" + i +".tsd", false);
			singleFileData = tsd.getData();
			varNames = tsd.getSpecies();
			if (i == 1){
				fullData.add(new ArrayList<Double>());
			}
			(fullData.get(0)).addAll(singleFileData.get(0));
			for (int k = 0; k < reqdVarsL.size(); k++){
				boolean found = false;
				for (int j = 0; j< singleFileData.size(); j++){
					if (reqdVarsL.get(k).getName().equalsIgnoreCase(varNames.get(j))) {
						if (i == 1){
							fullData.add(new ArrayList<Double>());
						}
						(fullData.get(k+1)).addAll(singleFileData.get(j));
						found = true;
						break;
					}
				}
				if (!found){
					out.write("variable " + reqdVarsL.get(k).getName() + " is required var somehow. But its data is not present. Adding empty array.\n");
					fullData.add(new ArrayList<Double>());
				}
			}
			i++;
		}
		
		extrema = getDataExtrema(fullData); //CHANGE THIS TO HASHMAP for replacing divisionsL by threholds
		setExtrema(extrema);
		
		
		}
		
		
			
		
	catch(NullPointerException e){
		e.printStackTrace();
		JOptionPane.showMessageDialog(Gui.frame,
				"Unable to calculate rates.\nThresholds could not be generated\nWindow size or pathLengthBin must be reduced.",
				"ERROR!", JOptionPane.ERROR_MESSAGE);
		running.setCursor(null);
		running.dispose();
	} catch (IOException e2) {
		e2.printStackTrace();
		JOptionPane.showMessageDialog(Gui.frame,
				"Unable to write into log file in autogenT.",
				"ERROR!", JOptionPane.ERROR_MESSAGE);
	} catch (NoSuchElementException e3) {
		e3.printStackTrace();
		if (reqdVarsL.size() == 0){
			JOptionPane.showMessageDialog(Gui.frame,
					"No input or output variables.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}

	}
	return localThresholds;
}
	
	
	
	public void setExtrema(HashMap<String, Double[]> extrema) {
		//System.out.println("extrema"+extrema);
		this.extrema = extrema;
	}

	public HashMap<String, Double[]> getExtrema() {
		
		//System.out.println("extrema"+extrema);
		return extrema;
	}


	

	public HashMap<String, ArrayList<Double>> greedyOpt(HashMap<String, ArrayList<Double>> localThresholds,ArrayList<ArrayList<Double>> fullData, HashMap<String,Double[]> extrema){
		HashMap<String, ArrayList<Double>> newThresholds = new HashMap<String, ArrayList<Double>>(); // = divisions; // initialization rechk??
		ArrayList<Integer> res = new ArrayList<Integer>();
		int updateVar = 0;
		Double bestCost =0.0,newCost;
		Double distance = 0.0;
		boolean pointsSelected = false;
		int numMoves = 0;
		int iterations =  Integer.parseInt(iteration.getText());
		if (points.isSelected()){
			pointsSelected = true;
			bestCost = pointDistCost(fullData, localThresholds,res,updateVar);
		}
		else if (range.isSelected()){
			pointsSelected = false;
			bestCost = rateRangeCost(fullData, localThresholds);
		}
		while (numMoves < iterations){ // Infinite loop here if thresholds not present in localThresholds
			for (int i = 0; i < localThresholds.size(); i++){
				for (int j = 0; j < localThresholds.get(reqdVarsL.get(i).getName()).size(); j++){
					if (j == 0){
						if (localThresholds.get(reqdVarsL.get(i).getName()).get(j) !=  null){
							distance = Math.abs(localThresholds.get(reqdVarsL.get(i).getName()).get(j) - extrema.get(reqdVarsL.get(i).getName())[0])/2;
						}
						else{// will else case ever occur???
							distance = Math.abs(localThresholds.get(reqdVarsL.get(i).getName()).get(j) - localThresholds.get(reqdVarsL.get(i).getName()).get(j-1))/2;
						}
					}
					else{
						distance = Math.abs(localThresholds.get(reqdVarsL.get(i).getName()).get(j) - localThresholds.get(reqdVarsL.get(i).getName()).get(j-1))/2;
					}
					// deep copy
					//newDivs = divisions; 
					newThresholds = new HashMap<String, ArrayList<Double>>();
					for (String s : localThresholds.keySet()){
						ArrayList<Double> tempDiv = new ArrayList<Double>();
						for (Double o2 : localThresholds.get(s)){
							tempDiv.add( o2.doubleValue()); // clone() not working here
						}
						newThresholds.put(s, tempDiv);
					}
					newThresholds.get(reqdVarsL.get(i).getName()).set(j,newThresholds.get(reqdVarsL.get(i).getName()).get(j)-distance);
					if (pointsSelected){
						newCost = pointDistCost(fullData,newThresholds,res,i+1);
					}
					else{
						newCost = rateRangeCost(fullData, newThresholds);
					}
					numMoves++;
					if (numMoves % 500 == 0){
						System.out.println("Iteration "+ numMoves + "/" + iterations);
					}
					if (newCost < bestCost){
						bestCost = newCost;
						localThresholds = new HashMap<String, ArrayList<Double>>();
						for (String s : newThresholds.keySet()){
							ArrayList<Double> tempDiv = new ArrayList<Double>();
							for (Double o2 : newThresholds.get(s)){
								tempDiv.add( o2.doubleValue()); // clone() not working here
							}
							localThresholds.put(s, tempDiv);
						}
						// divisions = newDivs; deep copy ?????
					}
					else{
						if (j == (localThresholds.get(reqdVarsL.get(i).getName()).size() - 1)){
							distance = Math.abs(extrema.get(reqdVarsL.get(i).getName())[1] - localThresholds.get(reqdVarsL.get(i).getName()).get(j))/2;
						}
						else{
							distance = Math.abs(localThresholds.get(reqdVarsL.get(i).getName()).get(j+1) - localThresholds.get(reqdVarsL.get(i).getName()).get(j))/2;
						}
						// deep copy
						//newDivs = divisions;
						newThresholds = new HashMap<String, ArrayList<Double>>();
						for (String s : localThresholds.keySet()){
							ArrayList<Double> tempDiv = new ArrayList<Double>();
							for (Double o2 : localThresholds.get(s)){
								tempDiv.add( o2.doubleValue()); // clone() not working here
							}
							newThresholds.put(s, tempDiv);
						}
						newThresholds.get(reqdVarsL.get(i).getName()).set(j,newThresholds.get(reqdVarsL.get(i).getName()).get(j)+distance);
						if (pointsSelected){
							newCost = pointDistCost(fullData,newThresholds,res,i+1);
						}
						else{
							newCost = rateRangeCost(fullData, newThresholds);
						}
						numMoves++;
						if (numMoves % 500 == 0){
							System.out.println("Iteration "+ numMoves + "/" + iterations);
						}
						if (newCost < bestCost){
							bestCost = newCost;
							localThresholds = new HashMap<String, ArrayList<Double>>();
							for (String s : newThresholds.keySet()){
								ArrayList<Double> tempDiv = new ArrayList<Double>();
								for (Double o2 : newThresholds.get(s)){
									tempDiv.add( o2.doubleValue()); // clone() not working here
								}
								localThresholds.put(s, tempDiv);
							}
							// divisions = newDivs; deep copy ?????
						}
						if (numMoves > iterations){
							return localThresholds;
						}
					}
				}
			}
		}
		return localThresholds;
	}

	// CHANGE EXTREMA TO HASHMAP for for replacing divisionsL by threholds in autogenT

	public Double rateRangeCost(ArrayList<ArrayList<Double>> fullData, HashMap<String, ArrayList<Double>> localThresholds){
		Double total = 0.0;
		Double[] minMaxR = {null,null};
		//genBinsRates(datFile, divisions);
		Double[][] rates = genBinsRatesForAutogen(fullData, localThresholds);
		for (int i = 0; i < localThresholds.size(); i++){
			minMaxR = getMinMaxRates(rates[i]);
			total += Math.abs(minMaxR[1] - minMaxR[0]);
		}
		return total;
	}

	public Double pointDistCost(ArrayList<ArrayList<Double>> fullData,HashMap<String, ArrayList<Double>> localThresholds, ArrayList<Integer> res, int updateVar ){
		Double total = 0.0;
		int pts = 0;
		if (updateVar == 0){
			for (int i = 0; i < localThresholds.size() + 1; i++){
				res.add(0);
			}
			for (int i = 0; i < localThresholds.size(); i++){
				pts = pointDistCostVar(fullData.get(i+1),localThresholds.get(reqdVarsL.get(i).getName()));
				total += pts;
				res.set(i,pts);
			}
		}
		else if (updateVar > 0){  // res is kind of being passed by reference. it gets altered outside too. 
			res.set(updateVar-1, pointDistCostVar(fullData.get(updateVar),localThresholds.get(reqdVarsL.get(updateVar-1).getName())));
			for (Integer i : res){
				total += i;
			}
			//for (int i = 0; i < res.size(); i++){
			//	if ((updateVar - 1) != i){
			//		total += res.get(i);
			//	}
			//	else{
			//		total += pointDistCostVar(fullData.get(updateVar),divisions.get(updateVar-1))
			//	}
			//} 
		}
		else{
			for (int i = 0; i < localThresholds.size(); i++){
				total += pointDistCostVar(fullData.get(i+1),localThresholds.get(reqdVarsL.get(i).getName()));
			}
		}
		return total;
	}

	public static int pointDistCostVar(ArrayList<Double> dat,ArrayList<Double> div){
		int optPointsPerBin = dat.size()/(div.size()+1);
		boolean top = false;
		ArrayList<Integer> pointsPerBin = new ArrayList<Integer>();
		for (int i =0; i < (div.size() +1); i++){
			pointsPerBin.add(0);
		}
		for (int i = 0; i <dat.size() ; i++){
			top = true;
			for (int j =0; j < div.size(); j++){
				if (dat.get(i) <= div.get(j)){
					pointsPerBin.set(j, pointsPerBin.get(j)+1);
					top = false;
					break;
				}
			}
			if (top){
				pointsPerBin.set(div.size(), pointsPerBin.get(div.size()) + 1);
			}
		}
		int score = 0;
		for (Integer pts : pointsPerBin){
			score += Math.abs(pts - optPointsPerBin );
		}
		return score;
	}


	public static Double[] getMinMaxRates(Double[] rateList){
		ArrayList<Double> cmpL = new ArrayList<Double>();
		Double[] minMax = {null,null};// new Double[2];
		for (Double r : rateList){
			if (r != null){
				cmpL.add(r);
			}
		}
		if (cmpL.size() > 0){
			Object obj = Collections.min(cmpL);
			minMax[0] = Double.parseDouble(obj.toString());
			obj = Collections.max(cmpL);
			minMax[1] = Double.parseDouble(obj.toString());
		}
		return minMax;
	}

	public Double[][] genBinsRatesForAutogen(ArrayList<ArrayList<Double>> data,HashMap<String, ArrayList<Double>> localThresholds) { // genBins
		rateSampling = Integer.parseInt(rateSamplingG.getText().trim());
		pathLengthBin = Integer.parseInt(pathLengthBinG.getText().trim());
		pathLengthVar = Integer.parseInt(pathLengthVarG.getText().trim());
		int[][] bins = new int[reqdVarsL.size()][data.get(0).size()];
		for (int i = 0; i < reqdVarsL.size(); i++) {
			String currentVar = reqdVarsL.get(i).getName();
			for (int k = 0; k < data.get(i+1).size(); k++) {
				ArrayList<Double> thresh = localThresholds.get(currentVar);
				bins[i][k] = getRegion(data.get(i+1).get(k),thresh);
				if ((k != 0) && (bins[i][k] != bins[i][k-1])){
					int length = 0;
					for (int m = k; m < data.get(i+1).size(); m++) {
						if (getRegion(data.get(i+1).get(m),thresh) == bins[i][k])
							length++;
						else 
							break;
					}
					if (length < pathLengthVar){
						//	out.write("Short bin for variable " + currentVar + " at " + data.get(0).get(k) + " until " + data.get(0).get(k+length-1) + " due to min pathLengthVar. Using " + bins[i][k-1] + " instead of " + bins[i][k] + " \n");
						for (int m = k; m < k+length; m++) {
							bins[i][m] = bins[i][k-1];
						}
					} else {
						for (int m = k; m < k+length; m++) {
							bins[i][m] = bins[i][k];
						}
					}
					k = k+length-1;
				}

			}
		}
		// genRates
		Double[][] rates = new Double[reqdVarsL.size()][data.get(0).size()];
		Double[] duration = new Double[data.get(0).size()];
		int mark ; //, k; // indices of rates not same as that of the variable. if
		// wanted, then size of rates array should be varNames not reqdVars
		if (rateSampling == -1) { // replacing inf with -1 since int
			mark = 0;
			for (int i = 0; i < data.get(0).size(); i++) {
				if (i < mark) {
					continue;
				}
				while ((mark < data.get(0).size()) && (compareBins(i, mark,bins))) {
					mark++;
				}
				if ((data.get(0).get(mark - 1) != data.get(0).get(i)) && ((mark - i) >=  pathLengthBin)) { 
					for (int j = 0; j < reqdVarsL.size(); j++) {
						//k = reqdVarIndices.get(j);
						rates[j][i] = ((data.get(j+1).get(mark - 1) - data.get(j+1).get(i)) / (data.get(0).get(mark - 1) - data.get(0).get(i)));
					}
					duration[i] = data.get(0).get(mark - 1)	- data.get(0).get(i);
				}
			}
		} else {
			boolean calcRate;
			boolean prevFail = true;
			int binStartPoint = 0, binEndPoint = 0;
			for (int i = 0; i < (data.get(0).size() - rateSampling); i++) {
				calcRate = true;
				for (int l = 0; l < rateSampling; l++) {
					if (!compareBins(i, i + l,bins)) {
						if (!prevFail){
							binEndPoint = i -2 + rateSampling;
							duration[binStartPoint] = data.get(0).get(binEndPoint)	- data.get(0).get(binStartPoint);
						}
						calcRate = false;
						prevFail = true;
						break;
					}
				}
				if (calcRate && (data.get(0).get(i + rateSampling) != data.get(0).get(i))) {
					for (int j = 0; j < reqdVarsL.size(); j++) {
						//k = reqdVarIndices.get(j);
						rates[j][i] = ((data.get(j+1).get(i + rateSampling) - data.get(j+1).get(i)) / (data.get(0).get(i + rateSampling) - data.get(0).get(i)));
					}
					if (prevFail){
						binStartPoint = i;
					}
					prevFail = false;
				}
			}
			if (!prevFail){ // for the last genuine rate-calculating region of the trace; this may not be required if the trace is incomplete.trace data may not necessarily end at a region endpt
				duration[binStartPoint] = data.get(0).get(data.get(0).size()-1)	- data.get(0).get(binStartPoint);
			}
		}
		
		return rates;
	}

	public boolean compareBins(int j, int mark,int[][] bins) {
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (bins[i][j] != bins[i][mark]) {
				return false;
			}
			continue;
		}
		return true;
	}

	public void addPseudo(HashMap<String,ArrayList<Double>> scaledThresholds){
		lpnWithPseudo = new LhpnFile();
		lpnWithPseudo = mergeLhpns(lpnWithPseudo,g);
		pseudoVars = new HashMap<String,Boolean>();
		pseudoVars.put("ctl",true);
		for (String st : g.getPlaceList()){
			currentPlace = st;
			//TODO: do this only if not prop type place
			if (getPlaceInfoIndex(st) != null)
				currPlaceBin = getPlaceInfoIndex(st).split(",");
			else 
				currPlaceBin = getTransientNetPlaceIndex(st).split(",");
			traverse(scaledThresholds);
		}
	}

	private void traverse(HashMap<String,ArrayList<Double>> scaledThresholds){
		for (String nextPlace : g.getPlaceList()){
			if ((!nextPlace.equalsIgnoreCase(currentPlace)) && (getPlaceInfoIndex(nextPlace) != null)){
				if ((getPlaceInfoIndex(currentPlace) != null) && (!transitionInfo.containsKey(getPlaceInfoIndex(currentPlace) + getPlaceInfoIndex(nextPlace)))){
					addPseudoTrans(nextPlace, scaledThresholds);
				} else if ((getTransientNetPlaceIndex(currentPlace) != null) && (!transientNetTransitions.containsKey(getTransientNetPlaceIndex(currentPlace) + getPlaceInfoIndex(nextPlace)))){
					addPseudoTrans(nextPlace, scaledThresholds);
				}
			}
		}
	}

	private void addPseudoTrans(String nextPlace, HashMap<String, ArrayList<Double>> scaledThresholds){ // Adds pseudo transition b/w currentPlace and nextPlace
		String[] nextPlaceBin = getPlaceInfoIndex(nextPlace).split(",");
		String enabling = "";
		int bin;
		String st; 
		Boolean pseudo = false;
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (reqdVarsL.get(i).isInput()){
				if (Integer.valueOf(currPlaceBin[i]) == Integer.valueOf(nextPlaceBin[i])){
					continue;
				}
				if (!pseudoVars.containsKey(reqdVarsL.get(i).getName())){
					pseudo = false;
					break;
				}
				if (Math.abs(Integer.valueOf(currPlaceBin[i]) - Integer.valueOf(nextPlaceBin[i])) > 1){
					pseudo = false;
					break;
				}
				pseudo = true;
				bin = Integer.valueOf(nextPlaceBin[i]);
				st = reqdVarsL.get(i).getName();
				if (bin == 0){
					if (!enabling.equalsIgnoreCase(""))
						enabling += "&";
					enabling += "~(" + st + ">=" + (int) Math.ceil(scaledThresholds.get(st).get(bin).doubleValue()) + ")";
				} else if (bin == (scaledThresholds.get(st).size())){
					if (!enabling.equalsIgnoreCase(""))
						enabling += "&";
					enabling += "(" + st + ">="	+ (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")";
				} else{
					if (!enabling.equalsIgnoreCase(""))
						enabling += "&";
					enabling += "(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")&~(" + st + ">=" + (int) Math.ceil(scaledThresholds.get(st).get(bin).doubleValue()) + ")";
				}
			} else {
				if (Integer.valueOf(currPlaceBin[i]) != Integer.valueOf(nextPlaceBin[i])){
					pseudo = false;
					break;
				}
			}
		}
		if (pseudo){
			System.out.println("Adding pseudo-transition pt"  + pseudoTransNum + " between " + currentPlace + " and " + nextPlace + " with enabling " + enabling + "\n");
			lpnWithPseudo.addTransition("pt" + pseudoTransNum);
			lpnWithPseudo.addMovement(currentPlace, "pt" + pseudoTransNum); 
			lpnWithPseudo.addMovement("pt" + pseudoTransNum, nextPlace);
			lpnWithPseudo.addEnabling("pt" + pseudoTransNum, enabling);
			pseudoTransNum++;
		}
	}

	public void addMetaBins(){ // TODO: DIDN'T REPLACE divisionsL by thresholds IN THIS METHOD
		boolean foundBin = false;
		for (String st1 : g.getPlaceList()){
			String p = getPlaceInfoIndex(st1);
			String[] binEncoding ;
			ArrayList<Integer> syncBinEncoding;
			String o1;
			// st1 w.r.t g
			// p w.r.t placeInfo
			if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
				//				String [] bE = p.split("");
				String [] bE = p.split(",");
				binEncoding = new String[bE.length - 1];
				for (int i = 0; i < bE.length; i++){
					binEncoding[i] = bE[i];    // since p.split("") gives ,0,1 if p was 01
				}
				for (int i = 0; i < binEncoding.length ; i++){
					if (!reqdVarsL.get(i).isDmvc()){
						if ((lowerLimit[i] != null) && (getMinRate(p,reqdVarsL.get(i).getName()) < 0)){
							syncBinEncoding = new ArrayList<Integer>();
							// deep copy of bin encoding
							for (int n = 0; n < binEncoding.length; n++){
								o1 = binEncoding[n];
								if (o1 == "A"){
									syncBinEncoding.add(-1);
								}
								else if (o1 == "Z"){
									syncBinEncoding.add(divisionsL.get(n).size());
								}
								else{
									syncBinEncoding.add( Integer.parseInt(o1)); // clone() not working here
								}
							}
							foundBin = false;
							while (!foundBin){
								syncBinEncoding.set(i,syncBinEncoding.get(i) - 1);
								String key = "";
								for (int m = 0; m < syncBinEncoding.size(); m++){
									if (syncBinEncoding.get(m) != -1){
										key += syncBinEncoding.get(m).toString();
									}
									else{
										key += "A"; // ***Encoding -1 as A
									}
								}
								if ((syncBinEncoding.get(i) == -1) && (!placeInfo.containsKey(key))){ 
									foundBin = true;
									Properties p0 = new Properties();
									placeInfo.put(key, p0);
									p0.setProperty("placeNum", numPlaces.toString());
									p0.setProperty("type", "RATE");
									p0.setProperty("initiallyMarked", "false");
									p0.setProperty("metaType","true");
									p0.setProperty("metaVar", String.valueOf(i));
									g.addPlace("p" + numPlaces, false);
									//ratePlaces.add("p"+numPlaces);
									numPlaces++;
									if (getMaxRate(p,reqdVarsL.get(i).getName()) > 0){ // minrate is 0; maxrate remains the same if positive
										addRate(p0, reqdVarsL.get(i).getName(), 0.0);
										addRate(p0, reqdVarsL.get(i).getName(), (double)getMaxRate(p,reqdVarsL.get(i).getName()));
										/*
										 * This transition should be added only in this case but
										 * since dotty cribs if there's no place on a transition's postset,
										 * moving this one down so that this transition is created even if the 
										 * min,max rate is 0 in which case it wouldn't make sense to have this
										 * transition
										 * 		Properties p2 = new Properties();
									transitionInfo.put(key + p, p2);
									p2.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addControlFlow("p" + placeInfo.get(key).getProperty("placeNum"), "t" + transitionInfo.get(key + p).getProperty("transitionNum")); 
									g.addControlFlow("t" + transitionInfo.get(key + p).getProperty("transitionNum"), "p" + placeInfo.get(p).getProperty("placeNum"));
									g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")" + "&~fail");
									numTransitions++; */
									}
									else{
										addRate(p0, reqdVarsL.get(i).getName(), 0.0); // if the maximum rate was negative, then make the min & max rates both as zero
									}
									Properties p1 = new Properties();
									transitionInfo.put(p + key, p1);
									p1.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addMovement("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + key).getProperty("transitionNum")); 
									g.addMovement("t" + transitionInfo.get(p + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
									//	g.addEnabling("t" + numTransitions, "~fail");
									g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")");
									int minr = getMinRate(key, reqdVarsL.get(i).getName());
									int maxr = getMaxRate(key, reqdVarsL.get(i).getName());
									if (minr != maxr)
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr + "," + maxr + ")");
									else
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
									numTransitions++;
									Properties p2 = new Properties();
									transitionInfo.put(key + p, p2);
									p2.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addMovement("p" + placeInfo.get(key).getProperty("placeNum"), "t" + transitionInfo.get(key + p).getProperty("transitionNum")); 
									g.addMovement("t" + transitionInfo.get(key + p).getProperty("transitionNum"), "p" + placeInfo.get(p).getProperty("placeNum"));
									g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")");
									minr = getMinRate(p, reqdVarsL.get(i).getName());
									maxr = getMaxRate(p, reqdVarsL.get(i).getName());
									if (minr != maxr)
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
									else
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
									numTransitions++;
								}
								else if (placeInfo.containsKey(key)){
									foundBin = true;
									//Properties syncP = placeInfo.get(key);
									Properties p1;
									if (!transitionInfo.containsKey(p + key)) { // instead of tuple
										p1 = new Properties();
										transitionInfo.put(p + key, p1);
										p1.setProperty("transitionNum", numTransitions.toString());
										g.addTransition("t" + numTransitions); // prevTranKey+key);
										g.addMovement("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + key).getProperty("transitionNum")); 
										g.addMovement("t" + transitionInfo.get(p + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
										//	g.addEnabling("t" + numTransitions, "~fail");
										g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")");
										int minr = getMinRate(key, reqdVarsL.get(i).getName());
										int maxr = getMaxRate(key, reqdVarsL.get(i).getName());
										if (minr != maxr)
											g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
										else
											g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
										numTransitions++;
									}
								}
							}
						}
						if ((upperLimit[i] != null) && getMaxRate(p,reqdVarsL.get(i).getName()) > 0){
							syncBinEncoding = new ArrayList<Integer>();
							// deep copy of bin encoding
							for (int n = 0; n < binEncoding.length; n++){
								o1 = binEncoding[n];
								if (o1 == "A"){
									syncBinEncoding.add(-1);
								}
								else if (o1 == "Z"){
									syncBinEncoding.add(divisionsL.get(n).size()+1);
								}
								else{
									syncBinEncoding.add( Integer.parseInt(o1)); // clone() not working here
								}
							}
							foundBin = false;
							while (!foundBin){
								syncBinEncoding.set(i,syncBinEncoding.get(i) + 1);
								String key = "";
								for (int m = 0; m < syncBinEncoding.size(); m++){
									if (syncBinEncoding.get(m) < divisionsL.get(i).size()+1){
										key += syncBinEncoding.get(m).toString();
									}
									else{
										key += "Z"; // ***Encoding highest bin +1 as Z
										// encoding not required here.. but may be useful to distinguish the pseudobins from normal bins in future
									}
								}
								if ((syncBinEncoding.get(i) == divisionsL.get(i).size() + 1) && (!placeInfo.containsKey(key))){ 
									// divisionsL.get(i).size() + 1 or  divisionsL.get(i).size()???
									foundBin = true;
									Properties p0 = new Properties();
									placeInfo.put(key, p0);
									p0.setProperty("placeNum", numPlaces.toString());
									p0.setProperty("type", "RATE");
									p0.setProperty("initiallyMarked", "false");
									p0.setProperty("metaType","true");
									p0.setProperty("metaVar", String.valueOf(i));
									//ratePlaces.add("p"+numPlaces);
									g.addPlace("p" + numPlaces, false);
									numPlaces++;
									if (getMinRate(p,reqdVarsL.get(i).getName()) < 0){ // maxrate is 0; minrate remains the same if negative
										addRate(p0, reqdVarsL.get(i).getName(), 0.0);
										addRate(p0, reqdVarsL.get(i).getName(), (double)getMinRate(p,reqdVarsL.get(i).getName()));
										/*
										 * This transition should be added only in this case but
										 * since dotty cribs if there's no place on a transition's postset,
										 * moving this one down so that this transition is created even if the 
										 * min,max rate is 0 in which case it wouldn't make sense to have this
										 * transition
										 * 				
									Properties p2 = new Properties();
									transitionInfo.put(key + p, p2);
									p2.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addControlFlow("p" + placeInfo.get(key).getProperty("placeNum"), "t" + transitionInfo.get(key + p).getProperty("transitionNum")); 
									g.addControlFlow("t" + transitionInfo.get(key + p).getProperty("transitionNum"), "p" + placeInfo.get(p).getProperty("placeNum"));
									g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")" + "&~fail");
									numTransitions++; 
										 */
									}
									else{
										addRate(p0, reqdVarsL.get(i).getName(), 0.0); // if the minimum rate was positive, then make the min & max rates both as zero
									}

									Properties p1 = new Properties();
									transitionInfo.put(p + key, p1);
									p1.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addMovement("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + key).getProperty("transitionNum")); 
									g.addMovement("t" + transitionInfo.get(p + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
									//	g.addEnabling("t" + numTransitions, "~fail");
									int minr = getMinRate(key, reqdVarsL.get(i).getName());
									int maxr = getMaxRate(key, reqdVarsL.get(i).getName());
									if (minr != maxr)
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
									else
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
									g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")");
									numTransitions++;
									Properties p2 = new Properties();
									transitionInfo.put(key + p, p2);
									p2.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addMovement("p" + placeInfo.get(key).getProperty("placeNum"), "t" + transitionInfo.get(key + p).getProperty("transitionNum")); 
									g.addMovement("t" + transitionInfo.get(key + p).getProperty("transitionNum"), "p" + placeInfo.get(p).getProperty("placeNum"));
									g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")");
									minr = getMinRate(p, reqdVarsL.get(i).getName());
									maxr = getMaxRate(p, reqdVarsL.get(i).getName());
									if (minr != maxr)
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
									else
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
									numTransitions++; 
								}
								else if (placeInfo.containsKey(key)){
									foundBin = true;
									//Properties syncP = placeInfo.get(key);
									Properties p1;
									if (!transitionInfo.containsKey(p + key)) { // instead of tuple
										p1 = new Properties();
										transitionInfo.put(p + key, p1);
										p1.setProperty("transitionNum", numTransitions.toString());
										g.addTransition("t" + numTransitions); // prevTranKey+key);
										g.addMovement("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + key).getProperty("transitionNum")); 
										g.addMovement("t" + transitionInfo.get(p + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
										//	g.addEnabling("t" + numTransitions, "~fail");
										int minr = getMinRate(key, reqdVarsL.get(i).getName());
										int maxr = getMaxRate(key, reqdVarsL.get(i).getName());
										if (minr != maxr)
											g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
										else
											g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
										g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")");
										numTransitions++;
									}
								}
							}
						}
					}
				}
			}
		}
	}



	/*	public void writeVHDLAMSFile(String vhdFile){
		try{
			ArrayList<String> ratePlaces = new ArrayList<String>();
			ArrayList<String> dmvcPlaces = new ArrayList<String>();
			File VHDLFile = new File(directory + separator + vhdFile);
			VHDLFile.createNewFile();
			BufferedWriter vhdlAms = new BufferedWriter(new FileWriter(VHDLFile));
			StringBuffer buffer = new StringBuffer();
			String pNum;
			vhdlAms.write("library IEEE;\n");
			vhdlAms.write("use IEEE.std_logic_1164.all;\n");
			vhdlAms.write("use work.handshake.all;\n");
			vhdlAms.write("use work.nondeterminism.all;\n\n");
			vhdlAms.write("entity amsDesign is\n");
			vhdlAms.write("end amsDesign;\n\n");
			vhdlAms.write("architecture "+vhdFile.split("\\.")[0]+" of amsDesign is\n");
			for (Variable v : reqdVarsL){
				vhdlAms.write("\tquantity "+v.getName()+":real;\n");
			}
			for (int i = 0; i < numPlaces; i++){
				if (!isTransientPlace("p"+i)){
					String p = getPlaceInfoIndex("p"+i);
					if (!placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						continue;
					}
				}
				else {
					String p = getTransientNetPlaceIndex("p"+i);
					if (!transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						continue;
					}
				}
				vhdlAms.write("\tshared variable place:integer:= "+i+";\n");
				break;
			}
			if (failPropVHDL != null){
				//TODO: Make this an assertion
				//vhdlAms.write("\tshared variable fail:boolean:= false;\n");
			}
			for (String st1 : g.getPlaceList()){
				if (!isTransientPlace(st1)){
					String p = getPlaceInfoIndex(st1);
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						ratePlaces.add(st1); // w.r.t g here
					}
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("PROP")) {

					}
				}
				else{
					String p = getTransientNetPlaceIndex(st1);
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")){
						ratePlaces.add(st1); // w.r.t g here
					}
				}
			}
			Collections.sort(dmvcPlaces,new Comparator<String>(){
				public int compare(String a, String b){
					if (Integer.parseInt(a.split("_")[1]) < Integer.parseInt(b.split("_")[1])){
						return -1;
					}
					else if (Integer.parseInt(a.split("_")[1]) == Integer.parseInt(b.split("_")[1])){
						return 0;
					}
					else{
						return 1;
					}
				}
			});
			Collections.sort(ratePlaces,new Comparator<String>(){
				String v1,v2;
				public int compare(String a, String b){
					if (!isTransientPlace(a) && !isTransientPlace(b)){
						v1 = placeInfo.get(getPlaceInfoIndex(a)).getProperty("placeNum");
						v2 = placeInfo.get(getPlaceInfoIndex(b)).getProperty("placeNum");
					}
					else if (!isTransientPlace(a) && isTransientPlace(b)){
						v1 = placeInfo.get(getPlaceInfoIndex(a)).getProperty("placeNum");
						v2 = transientNetPlaces.get(getTransientNetPlaceIndex(b)).getProperty("placeNum");
					}
					else if (isTransientPlace(a) && !isTransientPlace(b)){
						v1 = transientNetPlaces.get(getTransientNetPlaceIndex(a)).getProperty("placeNum");
						v2 = placeInfo.get(getPlaceInfoIndex(b)).getProperty("placeNum");
					}
					else {
						v1 = transientNetPlaces.get(getTransientNetPlaceIndex(a)).getProperty("placeNum");
						v2 = transientNetPlaces.get(getTransientNetPlaceIndex(b)).getProperty("placeNum");
					}
					if (Integer.parseInt(v1) < Integer.parseInt(v2)){
						return -1;
					}
					else if (Integer.parseInt(v1) == Integer.parseInt(v2)){
						return 0;
					}
					else{
						return 1;
					}
				}
			});
			// sending the initial place to the end of the list. since if statements begin with preset of each place
			//ratePlaces.add(ratePlaces.get(0)); 
			//ratePlaces.remove(0);

			vhdlAms.write("begin\n");
			//buffer.append("\nbegin\n");
			String[] vals;
			for (Variable v : reqdVarsL){ // taking the lower value from the initial value range. Ok?
				//vhdlAms.write("\tbreak "+v.getName()+" => "+((((v.getInitValue()).split("\\,"))[0]).split("\\["))[1]+".0;\n");
				vals = v.getInitValue().split("\\,");
				vhdlAms.write("\tbreak "+v.getName()+" => span("+((vals[0]).split("\\["))[1]+".0,"+ ((vals[1]).split("\\]"))[0] +".0);\n");
				//buffer.append("\tbreak "+v.getName()+" => span("+((vals[0]).split("\\["))[1]+".0,"+ ((vals[1]).split("\\]"))[0] +".0);\n");
			}
			vhdlAms.write("\n");
			//buffer.append("\n");
			for (Variable v : reqdVarsL){
				if (v.isDmvc()){
					vhdlAms.write("\t"+v.getName()+"'dot == 0.0;\n");
					//buffer.append("\t"+v.getName()+"'dot == 0.0;\n");
				}
			}
			vhdlAms.write("\n");
			//buffer.append("\n");
			ArrayList<ArrayList<String>> dmvcVarPlaces = new ArrayList<ArrayList<String>>();
			boolean contVarExists = false;
			for (Variable v: reqdVarsL){
				dmvcVarPlaces.add(new ArrayList<String>());
				if (v.isDmvc()){
					continue;
				}
				else{
					contVarExists = true;
				}
			}
			for (String st:dmvcPlaces){
				dmvcVarPlaces.get(Integer.parseInt(st.split("_")[1])).add(st);
			}
			if (contVarExists){
				if (ratePlaces.size() != 0){
					//vhdlAms.write("\tcase place use\n");
					buffer.append("\tcase place use\n");
				}
				//vhdlAms.write("\ntype rate_places is (");
				for (String p : ratePlaces){
					pNum = p.split("p")[1];
					//vhdlAms.write("\t\twhen "+p.split("p")[1] +" =>\n");
					buffer.append("\t\twhen "+ pNum +" =>\n");
					if (!isTransientPlace(p)){
						for (int j = 0; j<reqdVarsL.size(); j++){
							if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
								//if (!(reqdVarsL.get(j).isInput() && reqdVarsL.get(j).isDmvc())){
								//vhdlAms.write("\t\t\t" + reqdVarsL.get(j).getName() + "'dot == span(" + getMinRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0,"+getMaxRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0);\n");
								buffer.append("\t\t\t" + reqdVarsL.get(j).getName() + "'dot == span(" + getMinRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0,"+getMaxRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0);\n");
							}
						}
					}
					else{
						for (int j = 0; j<reqdVarsL.size(); j++){
							if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
								//if (!(reqdVarsL.get(j).isInput() && reqdVarsL.get(j).isDmvc())){
								//vhdlAms.write("\t\t\t" + reqdVarsL.get(j).getName() + "'dot == span(" + getMinRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0,"+getMaxRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0);\n");
								buffer.append("\t\t\t" + reqdVarsL.get(j).getName() + "'dot == span(" + getMinRate(getTransientNetPlaceIndex(p), reqdVarsL.get(j).getName())+".0,"+getMaxRate(getTransientNetPlaceIndex(p), reqdVarsL.get(j).getName())+".0);\n");
							}
						}
					}
				}
				vhdlAms.write(buffer.toString());
				vhdlAms.write("\t\twhen others =>\n");
				for (int j = 0; j<reqdVarsL.size(); j++){
					if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
						//if (!(reqdVarsL.get(j).isInput() && reqdVarsL.get(j).isDmvc())){
					vhdlAms.write("\t\t\t" + reqdVarsL.get(j).getName() + "'dot == 0.0;\n");
					}
				}
				vhdlAms.write("\tend case;\n");
			}
			vhdlAms.write("\tprocess\n");
			vhdlAms.write("\tbegin\n");
			vhdlAms.write("\tcase place is\n");
			buffer.delete(0, buffer.length());
			String[] transL;
			int transNum;
			for (String p : ratePlaces){
				pNum = p.split("p")[1];
				vhdlAms.write("\t\twhen "+pNum +" =>\n");
				vhdlAms.write("\t\t\twait until ");
				transL = g.getPostset(p);
				if (transL.length == 1){
					transNum = Integer.parseInt(transL[0].split("t")[1]);
					vhdlAms.write(transEnablingsVHDL[transNum] + ";\n");
					if (transDelayAssignVHDL[transNum] != null){
						vhdlAms.write("\t\t\twait for "+ transDelayAssignVHDL[transNum]+";\n");
						//vhdlAms.write("\t\t\tbreak "+ transIntAssignVHDL[transNum]+";\n");
						for (String s : transIntAssignVHDL[transNum]){
							if (s != null){
								vhdlAms.write("\t\t\tbreak "+ s +";\n");
							}
						}
					}
					if (g.getPostset(transL[0]).length != 0)
						vhdlAms.write("\t\t\tplace := " + g.getPostset(transL[0])[0].split("p")[1] + ";\n");
				}
				else{
					boolean firstTrans = true;
					buffer.delete(0, buffer.length());
					for (String t : transL){
						transNum = Integer.parseInt(t.split("t")[1]);
						if (firstTrans){
							firstTrans = false;
							vhdlAms.write("(" + transEnablingsVHDL[transNum]);
							buffer.append("\t\t\tif " + transEnablingsVHDL[transNum] + " then\n");
							if (transDelayAssignVHDL[transNum] != null){
								buffer.append("\t\t\t\twait for "+ transDelayAssignVHDL[transNum]+";\n");
								for (String s : transIntAssignVHDL[transNum]){
									if (s != null){
										buffer.append("\t\t\t\tbreak "+ s +";\n");
									}
								}
							}
							buffer.append("\t\t\t\tplace := " + g.getPostset(t)[0].split("p")[1] + ";\n"); 
						}
						else{
							vhdlAms.write(" or " +transEnablingsVHDL[transNum] );
							buffer.append("\t\t\telsif " + transEnablingsVHDL[transNum] + " then\n");
							if (transDelayAssignVHDL[transNum] != null){
								buffer.append("\t\t\t\twait for "+ transDelayAssignVHDL[transNum]+";\n");
								//buffer.append("\t\t\t\tbreak "+ transIntAssignVHDL[transNum]+";\n");
								for (String s : transIntAssignVHDL[transNum]){
									if (s != null){
										buffer.append("\t\t\t\tbreak "+ s +";\n");
									}
								}
							}
							buffer.append("\t\t\t\tplace := " + g.getPostset(t)[0].split("p")[1] + ";\n"); 
						}
					}
					vhdlAms.write(");\n");
					buffer.append("\t\t\tend if;\n");
					vhdlAms.write(buffer.toString());
				}
			}
			vhdlAms.write("\t\twhen others =>\n\t\t\twait for 0.0;\n\t\t\tplace := "+ ratePlaces.get(0).split("p")[1] + ";\n\tend case;\n\tend process;\n");
			for (int i = 0; i < dmvcVarPlaces.size(); i++){
				if (dmvcVarPlaces.get(i).size() != 0){
				vhdlAms.write("\tprocess\n");
				vhdlAms.write("\tbegin\n");
				for (String p : dmvcVarPlaces.get(i)){
					if (!transientNetPlaces.containsKey(p)){
						vhdlAms.write("\t\twait for delay("+ (int) Math.floor(Double.parseDouble(placeInfo.get(p).getProperty("dMin")))+","+(int)Math.ceil(Double.parseDouble(placeInfo.get(p).getProperty("dMax"))) +");\n");
						// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
						vhdlAms.write("\t\tbreak "+reqdVarsL.get(i).getName()+ " => "+ (int) Math.floor(Double.parseDouble(placeInfo.get(p).getProperty("DMVCValue"))) + ".0;\n");
					}
					else{
						vhdlAms.write("\t\twait for delay("+ (int) Math.floor(Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin")))+","+(int)Math.ceil(Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax"))) +");\n");
						// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
						vhdlAms.write("\t\tbreak "+reqdVarsL.get(i).getName()+ " => "+ (int) Math.floor(Double.parseDouble(transientNetPlaces.get(p).getProperty("DMVCValue"))) + ".0;\n");
					}
				}
				vhdlAms.write("\tend process;\n\n");
				}
			}
			if (failPropVHDL != null){
				vhdlAms.write("\tprocess\n");
				vhdlAms.write("\tbegin\n");
				vhdlAms.write("\t\twait until " + failPropVHDL + ";\n");
				//TODO: Change this to assertion
				//vhdlAms.write("\t\tfail := true;\n");
				vhdlAms.write("\tend process;\n\n");
			}
		//	vhdlAms.write("\tend process;\n\n");
			vhdlAms.write("end "+vhdFile.split("\\.")[0]+";\n");
			vhdlAms.close();

		}
		catch(IOException e){
			JOptionPane.showMessageDialog(BioSim.frame,
					"VHDL-AMS model couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(BioSim.frame,
					"Error in VHDL-AMS model generation.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
	public void writeVerilogAMSFile(String vamsFileName){
		try{
			ArrayList<String> ratePlaces = new ArrayList<String>();
			ArrayList<String> dmvcPlaces = new ArrayList<String>();
			File vamsFile = new File(directory + separator + vamsFileName);
			vamsFile.createNewFile();
			Double rateFactor = valScaleFactor/delayScaleFactor;
			BufferedWriter vams = new BufferedWriter(new FileWriter(vamsFile));
			StringBuffer buffer = new StringBuffer();
			StringBuffer buffer2 = new StringBuffer();
			StringBuffer buffer3 = new StringBuffer();
			StringBuffer buffer4 = new StringBuffer();
			StringBuffer initBuffer = new StringBuffer();
			vams.write("`include \"constants.vams\"\n");
			vams.write("`include \"disciplines.vams\"\n");
			vams.write("`timescale 1ps/1ps\n\n");
			vams.write("module "+vamsFileName.split("\\.")[0]+" (");
			buffer.append("\tparameter delay = 0, rtime = 1p, ftime = 1p;\n");
			Variable v;
			String[] vals;
			for (int i = 0; i < reqdVarsL.size(); i++){
				v = reqdVarsL.get(i);
				if ( i!= 0){
					vams.write(",");
				}
				vams.write(" "+v.getName());
				if (v.isInput()){
					buffer.append("\tinput "+v.getName()+";\n\telectrical "+v.getName()+";\n");
				} else{
					buffer.append("\tinout "+v.getName()+";\n\telectrical "+v.getName()+";\n");
					if (!v.isDmvc()){
						buffer2.append("\treal change_"+v.getName()+";\n");
						buffer2.append("\treal rate_"+v.getName()+";\n");
						vals = v.getInitValue().split("\\,");
						double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*valScaleFactor);
						initBuffer.append("\t\tchange_"+v.getName()+" = "+ spanAvg+";\n");
						vals = v.getInitRate().split("\\,");
						spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*rateFactor);
						initBuffer.append("\t\trate_"+v.getName()+" = "+ (int)spanAvg+";\n");
					}
					else{
						buffer2.append("\treal "+v.getName()+"Val;\n");	// changed from real to int.. check??
						vals = reqdVarsL.get(i).getInitValue().split("\\,");
						double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*valScaleFactor);
						initBuffer.append("\t\t"+reqdVarsL.get(i).getName()+"Val = "+ spanAvg+";\n");
					}
				}
			}
		//	if (failPropVHDL != null){
		//		buffer.append("\toutput reg fail;\n\tlogic fail;\n");
		//		vams.write(", fail");
		//	}
			vams.write(");\n" + buffer+"\n");
			if (buffer2.length() != 0){
				vams.write(buffer2.toString());
			}
			vams.write("\treal entryTime;\n");
			if (vamsRandom){
				vams.write("\tinteger seed;\n\tinteger del;\n");
			}
			vams.write("\tinteger place;\n\n\tinitial\n\tbegin\n");
			vams.write(initBuffer.toString());
			vams.write("\t\tentryTime = 0;\n");
			if (vamsRandom){
				vams.write("\t\tseed = 0;\n");
			}
			buffer.delete(0, buffer.length());
			buffer2.delete(0, buffer2.length());
			for (int i = 0; i < numPlaces; i++){
				String p;
				if (!isTransientPlace("p"+i)){
					p = getPlaceInfoIndex("p"+i);
					if (!placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						continue;
					}
				}
				else{
					p = getTransientNetPlaceIndex("p"+i);
					if (!transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						continue;
					}
				}
				vams.write("\t\tplace = "+i+";\n");
				break;
			}
			//if (failPropVHDL != null){
			//	vams.write("\t\t\tfail = 1'b0;\n");
			//}
			vams.write("\tend\n\n");
			for (String st1 : g.getPlaceList()){
				if (!isTransientPlace(st1)){
					String p = getPlaceInfoIndex(st1);
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						ratePlaces.add(st1); // w.r.t g here
					}
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("PROP")) {

					}
				}
				else{
					String p = getTransientNetPlaceIndex(st1);
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")){
						ratePlaces.add(st1); // w.r.t g here
					}

				}
			}
			Collections.sort(dmvcPlaces,new Comparator<String>(){
				public int compare(String a, String b){
					if (Integer.parseInt(a.split("_")[1]) < Integer.parseInt(b.split("_")[1])){
						return -1;
					}
					else if (Integer.parseInt(a.split("_")[1]) == Integer.parseInt(b.split("_")[1])){
						if (Integer.parseInt(a.split("_")[2]) < Integer.parseInt(b.split("_")[2])){
							return -1;
						}
						else if (Integer.parseInt(a.split("_")[2]) == Integer.parseInt(b.split("_")[2])){
							return 0;
						}
						else{
							return 1;
						}
					}
					else{
						return 1;
					}
				}
			});
			Collections.sort(ratePlaces,new Comparator<String>(){
				String v1,v2;
				public int compare(String a, String b){
					if (!isTransientPlace(a) && !isTransientPlace(b)){
						v1 = placeInfo.get(getPlaceInfoIndex(a)).getProperty("placeNum");
						v2 = placeInfo.get(getPlaceInfoIndex(b)).getProperty("placeNum");
					}
					else if (!isTransientPlace(a) && isTransientPlace(b)){
						v1 = placeInfo.get(getPlaceInfoIndex(a)).getProperty("placeNum");
						v2 = transientNetPlaces.get(getTransientNetPlaceIndex(b)).getProperty("placeNum");
					}
					else if (isTransientPlace(a) && !isTransientPlace(b)){
						v1 = transientNetPlaces.get(getTransientNetPlaceIndex(a)).getProperty("placeNum");
						v2 = placeInfo.get(getPlaceInfoIndex(b)).getProperty("placeNum");
					}
					else {
						v1 = transientNetPlaces.get(getTransientNetPlaceIndex(a)).getProperty("placeNum");
						v2 = transientNetPlaces.get(getTransientNetPlaceIndex(b)).getProperty("placeNum");
					}
					if (Integer.parseInt(v1) < Integer.parseInt(v2)){
						return -1;
					}
					else if (Integer.parseInt(v1) == Integer.parseInt(v2)){
						return 0;
					}
					else{
						return 1;
					}
				}
			});
			ArrayList<String> transitions = new ArrayList<String>(); 
			for (String t : g.getTransitionList()){
				transitions.add(t);
			}
			Collections.sort(transitions,new Comparator<String>(){
				public int compare(String a, String b){
					String v1 = a.split("t")[1];
					String v2 = b.split("t")[1];
					if (Integer.parseInt(v1) < Integer.parseInt(v2)){
						return -1;
					}
					else if (Integer.parseInt(v1) == Integer.parseInt(v2)){
						return 0;
					}
					else{
						return 1;
					}
				}
			});
			// sending the initial place to the end of the list. since if statements begin with preset of each place
			//ratePlaces.add(ratePlaces.get(0)); 
			//ratePlaces.remove(0);

			ArrayList<ArrayList<String>> dmvcVarPlaces = new ArrayList<ArrayList<String>>();
			boolean contVarExists = false;
			for (Variable var: reqdVarsL){
				dmvcVarPlaces.add(new ArrayList<String>());
				if (var.isDmvc()){
					continue;
				}
				else{
					contVarExists = true;
				}
			}
			for (String st:dmvcPlaces){
				dmvcVarPlaces.get(Integer.parseInt(st.split("_")[1])).add(st);
			}
			int transNum;
			buffer.delete(0, buffer.length());
			initBuffer.delete(0, initBuffer.length());
			String presetPlace = null,postsetPlace = null;
			StringBuffer[] transBuffer = new StringBuffer[transitions.size()];
			int cnt = 0;
			StringBuffer transAlwaysPlaceBuffer = new StringBuffer();
			int placeAlwaysBlockNum = -1;
			for (String t : transitions){
				presetPlace = g.getPreset(t)[0];
				//if (g.getPostset(t) != null){
				//	postsetPlace = g.getPostset(t)[0];
				//}
				transNum = Integer.parseInt(t.split("t")[1]);
				cnt = transNum;
				if (!isTransientTransition(t)){
					if (placeInfo.get(getPlaceInfoIndex(presetPlace)).getProperty("type").equals("RATE")){
						if (g.getPostset(t).length != 0)
							postsetPlace = g.getPostset(t)[0];
						for (int j = 0; j < transNum; j++){
							if ((transEnablingsVAMS[j] != null) && (transEnablingsVAMS[j].equalsIgnoreCase(transEnablingsVAMS[transNum]))){
								cnt = j;
								break;
							}
						}
						if ( cnt == transNum){
							transBuffer[cnt] = new StringBuffer();
							if (transEnablingsVAMS[transNum].equalsIgnoreCase("")){
								//transBuffer[cnt].append("\talways@(place)" + "\n\tbegin\n"); May 14, 2010
								placeAlwaysBlockNum = cnt;
							}
							else{
								transBuffer[cnt].append("\t"+transEnablingsVAMS[transNum] + "\n\tbegin\n");	
							    transAlwaysPlaceBuffer.append("\t\t" + transConditionalsVAMS[transNum] + "\n\t\tbegin\n\t\t\tentryTime = $abstime;\n");
							    if (g.getPostset(t).length != 0)
							    	transAlwaysPlaceBuffer.append("\t\t\tplace = " + postsetPlace.split("p")[1] + ";\n");	
							}
						}
						else{
							String s = transBuffer[cnt].toString();
							s = s.replaceAll("\t\tend\n\tend\n", "\t\tend\n");
							transBuffer[cnt].delete(0, transBuffer[cnt].length());
							transBuffer[cnt].append(s);
							if (!transEnablingsVAMS[transNum].equalsIgnoreCase("")){
								transAlwaysPlaceBuffer.append("\t\t" + transConditionalsVAMS[transNum] + "\n\t\tbegin\n\t\t\tentryTime = $abstime;\n" + "\t\t\tplace = " + postsetPlace.split("p")[1] + ";\n");
							}
						}
						transBuffer[cnt].append("\t\tif (place == "+ presetPlace.split("p")[1] +")\n\t\tbegin\n");
						if (transDelayAssignVAMS[transNum] != null){
							transBuffer[cnt].append("\t\t\t"+transDelayAssignVAMS[transNum]+";\n");
							for (int i = 0; i < transIntAssignVAMS[transNum].length; i++){
								if (transIntAssignVAMS[transNum][i] != null){
									transBuffer[cnt].append("\t\t\t"+ transIntAssignVAMS[transNum][i]);
								}
							}
						}
						transBuffer[cnt].append("\t\t\tentryTime = $abstime;\n");
						transBuffer[cnt].append("\t\t\tplace = " + postsetPlace.split("p")[1] + ";\n");
						for (int j = 0; j<reqdVarsL.size(); j++){
							if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
								transBuffer[cnt].append("\t\t\trate_"+reqdVarsL.get(j).getName()+ " = "+(int)((getMinRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName())+getMaxRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName()))/(2.0*rateFactor)) + ";\n");
								transBuffer[cnt].append("\t\t\tchange_" + reqdVarsL.get(j).getName()+ " = V("+ reqdVarsL.get(j).getName()+ ");\n");
								if (!transEnablingsVAMS[transNum].equalsIgnoreCase("")){
									transAlwaysPlaceBuffer.append("\t\t\trate_"+reqdVarsL.get(j).getName()+ " = "+(int)((getMinRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName())+getMaxRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName()))/(2.0*rateFactor)) + ";\n");
									transAlwaysPlaceBuffer.append("\t\t\tchange_" + reqdVarsL.get(j).getName()+ " = V("+ reqdVarsL.get(j).getName()+ ");\n");
								}
							}
						}
						transBuffer[cnt].append("\t\tend\n");
						//if ( cnt == transNum){
							transBuffer[cnt].append("\tend\n");
						//}
						if (!transEnablingsVAMS[transNum].equalsIgnoreCase("")){
							transAlwaysPlaceBuffer.append("\t\tend\n");
						}
					}
				}
				else{
					if (transientNetPlaces.get(getTransientNetPlaceIndex(presetPlace)).getProperty("type").equals("RATE")){
						if (g.getPostset(t).length != 0)
							postsetPlace = g.getPostset(t)[0];
						for (int j = 0; j < transNum; j++){
							if ((transEnablingsVAMS[j] != null) && (transEnablingsVAMS[j].equalsIgnoreCase(transEnablingsVAMS[transNum]))){
								cnt = j;
								break;
							}
						}
						if ( cnt == transNum){
							transBuffer[cnt] = new StringBuffer();
							transBuffer[cnt].append("\t"+transEnablingsVAMS[transNum] + "\n\tbegin\n");
						}
						else{
							String s = transBuffer[cnt].toString();
							s = s.replaceAll("\t\tend\n\tend\n", "\t\tend\n");
							transBuffer[cnt].delete(0, transBuffer[cnt].length());
							transBuffer[cnt].append(s);
						}
						transBuffer[cnt].append("\t\tif (place == "+ presetPlace.split("p")[1] +")\n\t\tbegin\n");
						if (transDelayAssignVAMS[transNum] != null){
							transBuffer[cnt].append("\t\t\t"+transDelayAssignVAMS[transNum]+";\n");
							for (int i = 0; i < transIntAssignVAMS[transNum].length; i++){
								if (transIntAssignVAMS[transNum][i] != null){
									transBuffer[cnt].append("\t\t\t"+ transIntAssignVAMS[transNum][i]);
								}
							}
						}
						transBuffer[cnt].append("\t\t\tentryTime = $abstime;\n");
						if (g.getPostset(t).length != 0)
							transBuffer[cnt].append("\t\t\tplace = " + postsetPlace.split("p")[1] + ";\n");
						for (int j = 0; j<reqdVarsL.size(); j++){
							if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
								transBuffer[cnt].append("\t\t\trate_"+reqdVarsL.get(j).getName()+ " = "+(int)((getMinRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName())+getMaxRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName()))/(2.0*rateFactor)) + ";\n");
								transBuffer[cnt].append("\t\t\tchange_" + reqdVarsL.get(j).getName()+ " = V("+ reqdVarsL.get(j).getName()+ ");\n");
							}
						}
						transBuffer[cnt].append("\t\tend\n");
						//if ( cnt == transNum){
							transBuffer[cnt].append("\tend\n");
						//}
					}
				}
				//if (transDelayAssignVAMS[transNum] != null){
				//	buffer.append("\t"+transEnablingsVAMS[transNum] + "\n\tbegin\n");
				//  buffer.append("\t\tif (place == "+ presetPlace.split("p")[1] +")\n\t\tbegin\n");
				//	buffer.append("\t\t\t#"+transDelayAssignVAMS[transNum]+";\n");
				//	for (int i = 0; i < transIntAssignVAMS[transNum].length; i++){
				//		if (transIntAssignVAMS[transNum][i] != null){
				//			buffer.append("\t\t\t"+reqdVarsL.get(i).getName()+"Val = "+ transIntAssignVAMS[transNum][i]+";\n");
				//		}
				//	}
				//	buffer.append("\t\tend\n\tend\n");
				//}
			}
			if (placeAlwaysBlockNum == -1){
				vams.write("\talways@(place)" + "\n\tbegin\n");
				vams.write(transAlwaysPlaceBuffer.toString());
				vams.write("\tend\n");
			}
			else{
				String s = transBuffer[placeAlwaysBlockNum].toString();
				s = s.replaceAll("\t\tend\n\tend\n", "\t\tend\n");
				transBuffer[placeAlwaysBlockNum].delete(0, transBuffer[placeAlwaysBlockNum].length());
				transBuffer[placeAlwaysBlockNum].append("\talways@(place)" + "\n\tbegin\n");
				transBuffer[placeAlwaysBlockNum].append(transAlwaysPlaceBuffer);
				transBuffer[placeAlwaysBlockNum].append(s);
				transBuffer[placeAlwaysBlockNum].append("\tend\n");
			}
			for (int j = 0; j < transitions.size(); j++){
				if (transBuffer[j] != null){
					vams.write(transBuffer[j].toString());
				}
			}
			vams.write("\tanalog\n\tbegin\n");
			for (int j = 0; j<reqdVarsL.size(); j++){
				if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){	
					vams.write("\t\tV("+reqdVarsL.get(j).getName()+") <+ transition(change_" + reqdVarsL.get(j).getName() + " + rate_" + reqdVarsL.get(j).getName()+"*($abstime-entryTime));\n");
				}
				if ((!reqdVarsL.get(j).isInput()) && (reqdVarsL.get(j).isDmvc())){
					vams.write("\t\tV("+reqdVarsL.get(j).getName()+") <+ transition(" + reqdVarsL.get(j).getName() + "Val,delay,rtime,ftime);\n");
					//vals = reqdVarsL.get(j).getInitValue().split("\\,");
					//double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*valScaleFactor);
					//initBuffer.append("\t\t"+reqdVarsL.get(j).getName()+"Val = "+ spanAvg+";\n");
				}
			}
			vams.write("\tend\n");
		//	if (initBuffer.length() != 0){
		//		vams.write("\n\tinitial\n\tbegin\n"+initBuffer+"\tend\n");
		//	}
			//if (buffer.length() != 0){
			//	vams.write(buffer.toString());
			//}
			vams.write("endmodule\n\n");
			buffer.delete(0, buffer.length());
			buffer2.delete(0, buffer2.length());
			initBuffer.delete(0, initBuffer.length());
			int count = 0;
			for (int i = 0; i < dmvcVarPlaces.size(); i++){
				if (dmvcVarPlaces.get(i).size() != 0){
					if (count == 0){
						vams.write("module driver ( " + reqdVarsL.get(i).getName() + "drive ");
						count++;
					}
					else{
						vams.write(", " + reqdVarsL.get(i).getName() + "drive ");
						count++;
					}
					buffer.append("\n\toutput "+ reqdVarsL.get(i).getName() + "drive;\n");
					buffer.append("\telectrical "+ reqdVarsL.get(i).getName() + "drive;\n");
					buffer2.append("\treal " + reqdVarsL.get(i).getName() + "Val" + ";\n");
					vals = reqdVarsL.get(i).getInitValue().split("\\,");
					double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*valScaleFactor);
					initBuffer.append("\n\tinitial\n\tbegin\n"+"\t\t"+ reqdVarsL.get(i).getName() + "Val = "+ spanAvg+";\n");
					if ((count == 1) && (vamsRandom) ){
						initBuffer.append("\t\tseed = 0;\n");
					}
					//buffer3.append("\talways\n\tbegin\n");
					boolean transientDoneFirst = false;
					for (String p : dmvcVarPlaces.get(i)){
						if (!transientNetPlaces.containsKey(p)){	// since p is w.r.t placeInfo & not w.r.t g
						//	buffer3.append("\t\t#"+  (int)(((Double.parseDouble(placeInfo.get(p).getProperty("dMin"))+ Double.parseDouble(placeInfo.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
							// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
						//	buffer3.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + placeInfo.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/valScaleFactor + ";\n");
							if (transientDoneFirst){
								initBuffer.append("\t\tforever\n\t\tbegin\n");
								transientDoneFirst = false;
							}
							if (g.getPostset("p" + placeInfo.get(p).getProperty("placeNum")).length != 0){
								if (!vamsRandom){
									//initBuffer.append("\t\t\t#"+  (int)(((Double.parseDouble(placeInfo.get(p).getProperty("dMin"))+ Double.parseDouble(placeInfo.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
									initBuffer.append("\t\t\t#"+  (int)(((Double.parseDouble(placeInfo.get(p).getProperty("dMin"))+ Double.parseDouble(placeInfo.get(p).getProperty("dMax"))))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
								}
								else{
									//initBuffer.append("\t\t\tdel = $dist_uniform(seed," + (int)Math.floor(((Double.parseDouble(placeInfo.get(p).getProperty("dMin")))/delayScaleFactor)*Math.pow(10, 12)) + "," +(int)Math.ceil((Double.parseDouble(placeInfo.get(p).getProperty("dMax"))/delayScaleFactor)*Math.pow(10, 12)) + ");\n\t\t\t#del ");	// converting seconds to ns using math.pow(10,9)
									initBuffer.append("\t\t\tdel = $dist_uniform(seed," + (int)Math.floor(((Double.parseDouble(placeInfo.get(p).getProperty("dMin")))/delayScaleFactor)) + "," +(int)Math.ceil((Double.parseDouble(placeInfo.get(p).getProperty("dMax"))/delayScaleFactor)) + ");\n\t\t\t#del ");	// converting seconds to ns using math.pow(10,9)
								}
								initBuffer.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + placeInfo.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/valScaleFactor + ";\n");
							}
							else{

							}
						}
						else{
							transientDoneFirst = true;
							if (!vamsRandom){
								//initBuffer.append("\t\t#"+  (int)(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin"))+ Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
								initBuffer.append("\t\t#"+  (int)(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin"))+ Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax"))))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
							}
							else{
								//initBuffer.append("\t\tdel = $dist_uniform(seed," + (int)Math.floor(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin")))/delayScaleFactor)*Math.pow(10, 12)) + "," +(int)Math.ceil((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax"))/delayScaleFactor)*Math.pow(10, 12)) + ");\n\t\t#del ");	// converting seconds to ns using math.pow(10,9)
								initBuffer.append("\t\tdel = $dist_uniform(seed," + (int)Math.floor(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin")))/delayScaleFactor)) + "," +(int)Math.ceil((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax"))/delayScaleFactor)) + ");\n\t\t#del ");	// converting seconds to ns using math.pow(10,9)
							}
							initBuffer.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + transientNetPlaces.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/valScaleFactor + ";\n" );
						//	initBuffer.append("\tend\n");
						}
					}
				//	buffer3.append("\tend\n");
					initBuffer.append("\t\tend\n\tend\n");
					buffer4.append("\t\tV("+reqdVarsL.get(i).getName() + "drive) <+ transition("+reqdVarsL.get(i).getName() + "Val,delay,rtime,ftime);\n");
				}
			}
			BufferedWriter topV = new BufferedWriter(new FileWriter(new File(directory + separator + "top.vams")));
			topV.write("`timescale 1ps/1ps\n\nmodule top();\n\n");
			if (count != 0){
				vams.write(");\n");
				vams.write("\tparameter delay = 0, rtime = 1p, ftime = 1p;\n");
				if (vamsRandom){
					vams.write("\tinteger del;\n\tinteger seed;\n");
				}
				vams.write(buffer+"\n"+buffer2+initBuffer+buffer3);
				vams.write("\tanalog\n\tbegin\n"+buffer4+"\tend\nendmodule");
				count = 0;
				for (int i = 0; i < dmvcVarPlaces.size(); i++){
					if (dmvcVarPlaces.get(i).size() != 0){
						if (count == 0){
							topV.write("\tdriver tb(\n\t\t." + reqdVarsL.get(i).getName() + "drive(" + reqdVarsL.get(i).getName() + ")");
							count++;
						}
						else{
							topV.write(",\n\t\t." + reqdVarsL.get(i).getName() + "drive(" + reqdVarsL.get(i).getName() + ")");
							count++;
						}
					}
				}
				topV.write("\n\t);\n\n");
			}
			for (int i = 0; i < reqdVarsL.size(); i++){
				v = reqdVarsL.get(i);
				if ( i== 0){
					topV.write("\t"+vamsFileName.split("\\.")[0]+" dut(\n\t\t."+ v.getName() + "(" + v.getName() + ")");
				}
				else{
					topV.write(",\n\t\t." + v.getName() + "(" + reqdVarsL.get(i).getName() + ")");
					count++;
				}
			}
			topV.write("\n\t);\n\nendmodule");
			topV.close();
			vams.close();
		}
		catch(IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(BioSim.frame,
					"Verilog-AMS model couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(BioSim.frame,
					"Error in Verilog-AMS model generation.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
	//T[] aux = (T[])a.clone();
	 */	
	private LhpnFile mergeLhpns(LhpnFile l1,LhpnFile l2){//(LhpnFile l1, LhpnFile l2){
		String place1 = "p([-\\d]+)", place2 = "P([-\\d]+)";
		String transition1 = "t([-\\d]+)", transition2 = "T([-\\d]+)";
		int placeNum, transitionNum;
		int minPlace=0, maxPlace=0, minTransition = 0, maxTransition = 0;
		Boolean first = true;
		try{
			for (String st1: l1.getPlaceList()){
				if ((st1.matches(place1)) || (st1.matches(place2))){
					st1 = st1.replaceAll("p", "");
					st1 = st1.replaceAll("P", "");
					placeNum = Integer.valueOf(st1);
					if (placeNum > maxPlace){
						maxPlace = placeNum;
						if (first){
							first = false;
							minPlace = placeNum;
						}
					}
					if (placeNum < minPlace){
						minPlace = placeNum;
						if (first){
							first = false;
							maxPlace = placeNum;
						}
					}
				}
			}
			for (String st1: l2.getPlaceList()){
				if ((st1.matches(place1)) || (st1.matches(place2))){
					st1 = st1.replaceAll("p", "");
					st1 = st1.replaceAll("P", "");
					placeNum = Integer.valueOf(st1);
					if (placeNum > maxPlace)
						maxPlace = placeNum;
					if (placeNum < minPlace)
						minPlace = placeNum;
				}
			}
			//System.out.println("min place and max place in both lpns are : " + minPlace + "," + maxPlace);
			for (String st2: l2.getPlaceList()){
				for (String st1: l1.getPlaceList()){
					if (st1.equalsIgnoreCase(st2)){
						maxPlace++;
						l2.renamePlace(st2, "p" + maxPlace);//, l2.getPlace(st2).isMarked());
						break;
					}
				}
			}
			first = true;
			for (String st1: l1.getTransitionList()){
				if ((st1.matches(transition1)) || (st1.matches(transition2))){
					st1 = st1.replaceAll("t", "");
					st1 = st1.replaceAll("T", "");
					transitionNum = Integer.valueOf(st1);
					if (transitionNum > maxTransition){
						maxTransition = transitionNum;
						if (first){
							first = false;
							minTransition = transitionNum;
						}
					}
					if (transitionNum < minTransition){
						minTransition = transitionNum;
						if (first){
							first = false;
							maxTransition = transitionNum;
						}
					}
				}
			}
			for (String st1: l2.getTransitionList()){
				if ((st1.matches(transition1)) || (st1.matches(transition2))){
					st1 = st1.replaceAll("t", "");
					st1 = st1.replaceAll("T", "");
					transitionNum = Integer.valueOf(st1);
					if (transitionNum > maxTransition)
						maxTransition = transitionNum;
					if (transitionNum < minTransition)
						minTransition = transitionNum;
				}
			}
			//System.out.println("min transition and max transition in both lpns are : " + minTransition + "," + maxTransition);
			for (String st2: l2.getTransitionList()){
				for (String st1: l1.getTransitionList()){
					if (st1.equalsIgnoreCase(st2)){
						maxTransition++;
						l2.renameTransition(st2, "t" + maxTransition);
						break;
					}
				}
			}
			l2.save(directory + separator + "tmp.lpn");
			l1.load(directory + separator + "tmp.lpn");
			File tmp = new File(directory + separator + "tmp.lpn");
			tmp.delete();
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Problem while merging lpns",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		return l1;
	}
}