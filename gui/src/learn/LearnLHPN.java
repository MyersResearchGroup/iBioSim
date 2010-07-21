package learn;

//import gcm2sbml.parser.GCMFile;
import lhpn2sbml.parser.LhpnFile;
import lhpn2sbml.parser.ExprTree;
import lhpn2sbml.parser.Lpn2verilog;
import parser.*;
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.*;
//import java.util.regex.Pattern;

import javax.swing.*;

//import org.sbml.libsbml.*;
import biomodelsim.*;

import org.jdesktop.layout.*;

/**
 * This class creates a GUI for the Learn program. It implements the
 * ActionListener class. This allows the GUI to perform actions when menu items
 * and buttons are selected.
 * 
 * @author Curtis Madsen
 */
public class LearnLHPN extends JPanel implements ActionListener, Runnable, ItemListener { // added ItemListener SB

	private static final long serialVersionUID = -5806315070287184299L;

	private JButton save, run, viewLhpn, saveLhpn, viewLog; // the run button

	private JButton viewCoverage; // SB
	
	private JButton viewVHDL,viewVerilog; // SB
	
	private JComboBox debug; // debug combo box

	private JTextField iteration, backgroundField, propertyG;

	private JComboBox numBins;

	private JCheckBox basicFBP;

	private ArrayList<ArrayList<Component>> variables;

	private JPanel variablesPanel; //, basicOpt, advancedOpt;

	private JRadioButton user, auto, range, points;

	private JButton suggest;

	private String directory, lrnFile;

	private JLabel numBinsLabel;

	private Log log;

	private String separator;

	private BioSim biosim;

	private String learnFile, lhpnFile;

	private boolean change, fail;

	private ArrayList<String> variablesList;

	private boolean firstRead, generate, execute;

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

	private boolean placeRates = true;

	private LhpnFile g;

	private Integer numPlaces = 0;

	private Integer numTransitions = 0;

	private HashMap<String, Properties> placeInfo;

	private HashMap<String, Properties> transitionInfo;
	
	private HashMap<String, Properties> cvgInfo;

	private Double minDelayVal = 10.0;

	private Double minRateVal = 10.0;

	private Double minDivisionVal = 10.0;

	private Double delayScaleFactor = 1.0;

	private Double valScaleFactor = 1.0;

	BufferedWriter out;

	File logFile;

	// Threshold parameters
	private double epsilon ;//= 0.1; // What is the +/- epsilon where signals are considered to be equivalent

	private int runLength ; //= 15; // the number of time points that a value must persist to be considered constant

	private double runTime ; // = 5e-12; // 10e-6 for intFixed; 5e-6 for integrator. 5e-12 for pd;// the amount of time that must pass to be considered constant when using absoluteTime

	private boolean absoluteTime ; // = true; // true for intfixed //false; true for pd; false for integrator// when False time points are used to determine DMVC and when true absolutime time is used to determine DMVC

	private double percent ; // = 0.8; // a decimal value representing the percent of the total trace that must be constant to qualify to become a DMVC var

	private JTextField epsilonG;
	
	private JTextField percentG;
	
	private JCheckBox absTimeG;
	
	private JTextField pathLengthBinG;
	
	private JTextField pathLengthVarG;
	
	private JTextField rateSamplingG;
	
	private JTextField runTimeG;
	
	private JTextField runLengthG;
	
	private JTextField globalDelayScaling;
	
	private JTextField globalValueScaling;
	
	private JCheckBox defaultEnvG;
	
	private boolean suggestIsSource = false;
	
	private Double[] lowerLimit;
	
	private Double[] upperLimit; 
	
	private String[] transEnablingsVHDL;
	
	private String[][] transIntAssignVHDL;
	
	private String[] transDelayAssignVHDL;
	
	private String[] transEnablingsVAMS;
	
	private String[] transConditionalsVAMS;
		
	private String[][] transIntAssignVAMS;
	
	private String[] transDelayAssignVAMS;
	
	private String failPropVHDL;

	private HashMap<String, Properties> transientNetPlaces;

	private HashMap<String, Properties> transientNetTransitions;

	private ArrayList<String> ratePlaces;

	private ArrayList<String> dmvcInputPlaces;

	private ArrayList<String> propPlaces;
	
	private boolean vamsRandom = false;
	
	private ArrayList<String> allVars;
	
	private Thread LearnThread;
	
	private HashMap<String,Properties> dmvcValuesUnique;
	
	private Double dsFactor, vsFactor;
	
	private String currentPlace;
	
	private String[] currPlaceBin;
	
	private ArrayList<Integer> inputs;
	
	private LhpnFile lpnWithPseudo;
	
	private int pseudoTransNum = 0;
	
	private HashMap<String,Boolean> pseudoVars;
	
	private ArrayList<HashMap<String, String>> constVal;
	
	private boolean dmvDetectDone = false;

	private boolean dmvStatusLoaded = false;
	
	private int pathLengthVar = 40;
	
	// Pattern lParenR = Pattern.compile("\\(+"); //SB
	
	//Pattern floatingPointNum = Pattern.compile(">=(-*[0-9]+\\.*[0-9]*)"); //SB

	// Pattern falseR = Pattern.compile("false",Pattern.CASE_INSENSITIVE); //pass the I flag to be case insensitive

	/**
	 * This is the constructor for the Learn class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public LearnLHPN(String directory, Log log, BioSim biosim) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		} else {
			separator = File.separator;
		}

		this.biosim = biosim;
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

		/*
		 * spacing = new JRadioButton("Equal Spacing Of Bins"); data = new
		 * JRadioButton("Equal Data Per Bins");
		 */

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
		// if (biosimrc.get("biosim.learn.autolevels", "").equals("Auto")) {
		// auto.setSelected(true);
		// }
		// else {
		// user.setSelected(true);
		// }
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

		// Sets up initial network and experiments text fields
		// JPanel initNet = new JPanel();
		// JLabel initNetLabel = new JLabel("Background Knowledge Network:");
		// browseInit = new JButton("Browse");
		// browseInit.addActionListener(this);
		// initNetwork = new JTextField(39);
		// initNet.add(initNetLabel);
		// initNet.add(initNetwork);
		// initNet.add(browseInit);

		// Sets up the thresholds area
		JPanel thresholdPanel2 = new JPanel(new GridLayout(8, 2));
		JPanel thresholdPanel1 = new JPanel(new GridLayout(5, 2));
		
		JLabel backgroundLabel = new JLabel("Model File:");
		backgroundField = new JTextField(lhpnFile);
		backgroundField.setEditable(false);
		thresholdPanel1.add(backgroundLabel);
		thresholdPanel1.add(backgroundField);
		//JLabel iterationLabel = new JLabel("Iterations of Optimization Algorithm");
		//iteration = new JTextField("10");
		//thresholdPanel1.add(iterationLabel);
		//thresholdPanel1.add(iteration);
		
	//	JLabel rateLabel = new JLabel("Rate calculation/DMV parameters");
		JLabel epsilonLabel = new JLabel("Epsilon");
		epsilonG = new JTextField("0.1");
		JLabel pathLengthBinLabel = new JLabel("Minimum bin pathLength");//("pathLengthBin");
		pathLengthBinG = new JTextField("0");
		JLabel pathLengthVarLabel = new JLabel("Minimum variable pathLength");//("pathLengthBin");
		pathLengthVarG = new JTextField("0");
		JLabel rateSamplingLabel = new JLabel("Window size");//("Rate Sampling");
		rateSamplingG = new JTextField("-1");
	//	JLabel dmvcLabel = new JLabel("DMV determination parameters");
		JLabel absTimeLabel = new JLabel("Absolute time");
		absTimeG = new JCheckBox();
		absTimeG.setSelected(false);
		absTimeG.addItemListener(this); 
		JLabel percentLabel = new JLabel("Fraction");
		percentG = new JTextField("0.8");
		JLabel runTimeLabel = new JLabel("DMV run time");
		runTimeG = new JTextField("5e-6");
		runTimeG.setEnabled(false);
		JLabel runLengthLabel = new JLabel("DMV run length");
		runLengthG = new JTextField("2");
		runLengthG.setEnabled(true);
		JLabel defaultEnvLabel = new JLabel("Default environment");
		defaultEnvG = new JCheckBox();
		defaultEnvG.setSelected(true);
		defaultEnvG.addItemListener(this); 
		
		epsilonG.addActionListener(this); //SB
		pathLengthBinG.addActionListener(this); //SB
		pathLengthVarG.addActionListener(this); //SB
		rateSamplingG.addActionListener(this); //SB
		percentG.addActionListener(this); //SB
		runTimeG.addActionListener(this); //SB
		runLengthG.addActionListener(this); //SB
		
		JPanel newPanel = new JPanel();
		JPanel jPanel1 = new JPanel();
		JPanel jPanel2 = new JPanel();
		
		JPanel panel4 = new JPanel();
		((FlowLayout) panel4.getLayout()).setAlignment(FlowLayout.CENTER);
		JPanel panel3 = new JPanel(new GridLayout(9, 2));
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
/*
        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(epsilonLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 104, Short.MAX_VALUE)
                                .add(epsilonG, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(rateSamplingLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(129, 129, 129)
                                .add(rateSamplingG, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(pathLengthBinLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                                .add(68, 68, 68)
                                .add(pathLengthBinG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(60, 60, 60)
                        .add(rateLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 186, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {epsilonG, pathLengthBinG, rateSamplingG}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(rateLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(51, 51, 51)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(epsilonLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(epsilonG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rateSamplingLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rateSamplingG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pathLengthBinLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pathLengthBinG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(35, 35, 35))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {epsilonG, epsilonLabel, pathLengthBinG, pathLengthBinLabel, rateSamplingG, rateSamplingLabel}, org.jdesktop.layout.GroupLayout.VERTICAL);
       
        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                    .addContainerGap(140, Short.MAX_VALUE)
                    .add(dmvcLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 206, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(85, 85, 85))
                .add(jPanel2Layout.createSequentialGroup()
                    .add(83, 83, 83)
                    .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel2Layout.createSequentialGroup()
                            .add(percentLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 112, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(percentG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .add(jPanel2Layout.createSequentialGroup()
                            .add(absTimeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(31, 31, 31)
                            .add(absTimeG)
                            .add(30, 30, 30))))
                .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                    .add(73, 73, 73)
                    .add(runTimeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(runTimeG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
                .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                	.addContainerGap(73, Short.MAX_VALUE)
                    .add(runLengthLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(runLengthG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
        );

        jPanel2Layout.linkSize(new java.awt.Component[] {percentG, runLengthG, runTimeG}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel2Layout.linkSize(new java.awt.Component[] {absTimeLabel, percentLabel, runLengthLabel, runTimeLabel}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(53, 53, 53)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                .add(jPanel2Layout.createSequentialGroup()
                .add(dmvcLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(35, 35, 35)
                    .add(absTimeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(absTimeG))
                .add(18, 18, 18)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, percentLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, percentG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, runTimeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, runTimeG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, runLengthLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, runLengthG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(63, 63, 63))
        );
        jPanel2Layout.linkSize(new java.awt.Component[] {absTimeLabel, percentG, percentLabel, runLengthG, runLengthLabel, runTimeG, runTimeLabel}, org.jdesktop.layout.GroupLayout.VERTICAL);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(newPanel);
        newPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(72, 72, 72)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 256, Short.MAX_VALUE)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(157, 157, 157))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(56, 56, 56)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
*/
		// divisionsL = new ArrayList<ArrayList<Double>>(); // SB
		thresholds = new HashMap<String, ArrayList<Double>>();
		reqdVarsL = new ArrayList<Variable>();
		/*
		 * JLabel activationLabel = new JLabel("Ratio For Activation (Ta):");
		 * thresholdPanel2.add(activationLabel); activation = new
		 * JTextField(biosimrc.get("biosim.learn.ta", "")); //
		 * activation.addActionListener(this); thresholdPanel2.add(activation);
		 * JLabel repressionLabel = new JLabel("Ratio For Repression (Tr):");
		 * thresholdPanel2.add(repressionLabel); repression = new
		 * JTextField(biosimrc.get("biosim.learn.tr", "")); //
		 * repression.addActionListener(this); thresholdPanel2.add(repression);
		 * JLabel influenceLevelLabel = new JLabel("Merge Influence Vectors
		 * Delta (Tm):"); thresholdPanel2.add(influenceLevelLabel);
		 * influenceLevel = new JTextField(biosimrc.get("biosim.learn.tm", "")); //
		 * influenceLevel.addActionListener(this);
		 * thresholdPanel2.add(influenceLevel); JLabel letNThroughLabel = new
		 * JLabel("Minimum Number Of Initial Vectors (Tn): ");
		 * thresholdPanel1.add(letNThroughLabel); letNThrough = new
		 * JTextField(biosimrc.get("biosim.learn.tn", "")); //
		 * letNThrough.addActionListener(this);
		 * thresholdPanel1.add(letNThrough); JLabel maxVectorSizeLabel = new
		 * JLabel("Maximum Influence Vector Size (Tj):");
		 * thresholdPanel1.add(maxVectorSizeLabel); maxVectorSize = new
		 * JTextField(biosimrc.get("biosim.learn.tj", "")); //
		 * maxVectorSize.addActionListener(this);
		 * thresholdPanel1.add(maxVectorSize); JLabel parentLabel = new
		 * JLabel("Score For Empty Influence Vector (Ti):");
		 * thresholdPanel1.add(parentLabel); parent = new
		 * JTextField(biosimrc.get("biosim.learn.ti", ""));
		 * parent.addActionListener(this); thresholdPanel1.add(parent); JLabel
		 * relaxIPDeltaLabel = new JLabel("Relax Thresholds Delta (Tt):");
		 * thresholdPanel2.add(relaxIPDeltaLabel); relaxIPDelta = new
		 * JTextField(biosimrc.get("biosim.learn.tt", "")); //
		 * relaxIPDelta.addActionListener(this);
		 * thresholdPanel2.add(relaxIPDelta);
		 */

		numBinsLabel = new JLabel("Number of Bins:");
		String[] bins = { "Auto", "2", "3", "4", "5", "6", "7", "8", "16", "32"};//, "10", "11", "12", "13", "14", "15", "16", "17", "33", "65", "129", "257" };
		numBins = new JComboBox(bins);
		numBins.setSelectedItem(biosimrc.get("biosim.learn.bins", ""));
		numBins.addActionListener(this);
		numBins.setActionCommand("text");
		thresholdPanel1.add(numBinsLabel);
		thresholdPanel1.add(numBins);
		JLabel propertyLabel = new JLabel("Assertion to be Verified");
		propertyG = new JTextField("");
		thresholdPanel1.add(propertyLabel);
		thresholdPanel1.add(propertyG);
		JLabel valueScaleLabel = new JLabel("Scale Factor for Values");
		globalValueScaling = new JTextField("");
		thresholdPanel1.add(valueScaleLabel);
		thresholdPanel1.add(globalValueScaling);
		JLabel delayScaleLabel = new JLabel("Scale Factor for Time");
		globalDelayScaling = new JTextField("");
		thresholdPanel1.add(delayScaleLabel);
		thresholdPanel1.add(globalDelayScaling);
		JPanel thresholdPanelHold1 = new JPanel();
		thresholdPanelHold1.add(thresholdPanel1);
		JLabel debugLabel = new JLabel("Debug Level:");
		String[] options = new String[4];
		options[0] = "0";
		options[1] = "1";
		options[2] = "2";
		options[3] = "3";
		debug = new JComboBox(options);
		// debug.setSelectedItem(biosimrc.get("biosim.learn.debug", ""));
		debug.addActionListener(this);
		thresholdPanel2.add(debugLabel);
		thresholdPanel2.add(debug);
		// succ = new JRadioButton("Successors");
		// pred = new JRadioButton("Predecessors");
		// both = new JRadioButton("Both");
		// if (biosimrc.get("biosim.learn.succpred", "").equals("Successors")) {
		// succ.setSelected(true);
		// }
		// else if (biosimrc.get("biosim.learn.succpred",
		// "").equals("Predecessors")) {
		// pred.setSelected(true);
		// }
		// else {
		// both.setSelected(true);
		// }
		// succ.addActionListener(this);
		// pred.addActionListener(this);
		// both.addActionListener(this);
		basicFBP = new JCheckBox("Basic FindBaseProb");
		// if (biosimrc.get("biosim.learn.findbaseprob", "").equals("True")) {
		// basicFBP.setSelected(true);
		// }
		// else {
		basicFBP.setSelected(false);
		// }
		basicFBP.addActionListener(this);
		// ButtonGroup succOrPred = new ButtonGroup();
		// succOrPred.add(succ);
		// succOrPred.add(pred);
		// succOrPred.add(both);
		JPanel three = new JPanel();
		// three.add(succ);
		// three.add(pred);
		// three.add(both);
		((FlowLayout) three.getLayout()).setAlignment(FlowLayout.LEFT);
		thresholdPanel2.add(three);
		thresholdPanel2.add(new JPanel());
		thresholdPanel2.add(basicFBP);
		thresholdPanel2.add(new JPanel());
		// JPanel thresholdPanelHold2 = new JPanel();
		// thresholdPanelHold2.add(thresholdPanel2);

		/*
		 * JLabel windowRisingLabel = new JLabel("Window Rising Amount:");
		 * windowRising = new JTextField("1");
		 * thresholdPanel2.add(windowRisingLabel);
		 * thresholdPanel2.add(windowRising); JLabel windowSizeLabel = new
		 * JLabel("Window Size:"); windowSize = new JTextField("1");
		 * thresholdPanel2.add(windowSizeLabel);
		 * thresholdPanel2.add(windowSize); harshenBoundsOnTie = new
		 * JCheckBox("Harshen Bounds On Tie");
		 * harshenBoundsOnTie.setSelected(true); donotInvertSortOrder = new
		 * JCheckBox("Do Not Invert Sort Order");
		 * donotInvertSortOrder.setSelected(true); seedParents = new
		 * JCheckBox("Parents Should Be Ranked By Score");
		 * seedParents.setSelected(true); mustNotWinMajority = new
		 * JCheckBox("Must Not Win Majority");
		 * mustNotWinMajority.setSelected(true); donotTossSingleRatioParents =
		 * new JCheckBox("Single Ratio Parents Should Be Kept");
		 * donotTossChangedInfluenceSingleParents = new JCheckBox( "Parents That
		 * Change Influence Should Not Be Tossed");
		 * thresholdPanel2.add(harshenBoundsOnTie);
		 * thresholdPanel2.add(donotInvertSortOrder);
		 * thresholdPanel2.add(seedParents);
		 * thresholdPanel2.add(mustNotWinMajority);
		 * thresholdPanel2.add(donotTossSingleRatioParents);
		 * thresholdPanel2.add(donotTossChangedInfluenceSingleParents);
		 */

		// load parameters
		// reading lrnFile twice. On the first read, only learnFile (the initial lpn) is processed.
		// In the gap b/w these reads, reqdVarsL is created based on the learnFile
		Properties load = new Properties();
		learnFile = "";
		//int i = 1;
		TSDParser extractVars;
		ArrayList<String> datFileVars = new ArrayList<String>();
		allVars = new ArrayList<String>();
		Boolean varPresent = false;
		//Finding the intersection of all the variables present in all data files.
		for (int i = 1; (new File(directory + separator + "run-" + i + ".tsd")).exists(); i++) {
			Properties cProp = new Properties();
			extractVars = new TSDParser(directory + separator + "run-" + i + ".tsd", biosim,false);
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
				JOptionPane.showMessageDialog(biosim.frame(),
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
				learnFile = directory.substring(0, directory.length()
						- getFilename[getFilename.length - 1].length())
						+ separator + getProp[getProp.length - 1];
				backgroundField.setText(getProp[getProp.length - 1]);
				
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}
		// SB
		variablesList = new ArrayList<String>();
		LhpnFile lhpn = new LhpnFile(log);
		// log.addText(learnFile);
		lhpn.load(learnFile);
		HashMap<String, Properties> variablesMap = lhpn.getContinuous();
		for (String s : variablesMap.keySet()) {
			variablesList.add(s);
			reqdVarsL.add(new Variable(s));
			thresholds.put(s, new ArrayList<Double>());
		}
		// System.out.println(variablesList);
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
			}
			if (load.containsKey("learn.delayScaling")) {
				globalDelayScaling.setText(load.getProperty("learn.delayScaling"));
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
			int j = 0;
			//levels();
			/*while (load.containsKey("learn.bins"+j)){
				String s = load.getProperty("learn.bins" + j);
				String[] savedBins = s.split("\\s");
				//divisionsL.add(new ArrayList<Double>());
				//variablesList.add(savedBins[0]);
			//	((JComboBox)(((JPanel)variablesPanel.getComponent(j+1)).getComponent(2))).setSelectedItem(savedBins[1]);
				for (int i = 2; i < savedBins.length ; i++){
			//		((JTextField)(((JPanel)variablesPanel.getComponent(j+1)).getComponent(i+1))).setText(savedBins[i]);
					if (j < variablesMap.size()) {
						divisionsL.get(j).add(Double.parseDouble(savedBins[i]));
					}
				}
				j++;
			}*/
			String[] varsList = null;
			if (load.containsKey("learn.varsList")){
				String varsListString = load.getProperty("learn.varsList");
				varsList = varsListString.split("\\s");
				for (String st1 : varsList){
					boolean varFound = false;
					String s = load.getProperty("learn.bins" + st1);
					String[] savedBins = null;
					if (s != null){
						savedBins = s.split("\\s");
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
			/*	for (String st1 : varsList){
					String s = load.getProperty("learn.bins" + st1);
					if (s != null){
						String[] savedBins = s.split("\\s");
						//divisionsL.add(new ArrayList<Double>());
						//variablesList.add(savedBins[0]);
						//	((JComboBox)(((JPanel)variablesPanel.getComponent(j+1)).getComponent(2))).setSelectedItem(savedBins[1]);
						boolean varFound = false;
						if (noLpn){
							variablesList.add(st1);
							reqdVarsL.add(new Variable(st1));
							thresholds.put(st1, new ArrayList<Double>());
						}
						for (String st2 :variablesList){
							if (st1.equalsIgnoreCase(st2)){
								varFound = true;
								break;
							}
						}
						if (!varFound){
							continue;
						}
						else{
							for (int i = 2; i < savedBins.length ; i++){
								//		((JTextField)(((JPanel)variablesPanel.getComponent(j+1)).getComponent(i+1))).setText(savedBins[i]);
								if (j < variablesMap.size()) {
								//	divisionsL.get(j).add(Double.parseDouble(savedBins[i]));
									thresholds.get(st1).add(Double.parseDouble(savedBins[i]));
								}
							}
							j++;
						}
					}
				}*/
			}
		//	else{ Doing this will clear the selects even first time when created from lpn
		//		variablesList.clear();
		//		reqdVarsL.clear();
		//		thresholds.clear();
		//	}
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
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}
		try {
			FileWriter write = new FileWriter(new File(directory + separator + "background.lpn"));
			BufferedReader input = new BufferedReader(new FileReader(new File(learnFile)));
			String line = null;
			while ((line = input.readLine()) != null) {
				write.write(line + "\n");
			}
			write.close();
			input.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to create background file!",
					"Error Writing Background", JOptionPane.ERROR_MESSAGE);
		}

		// sortSpecies();
		JPanel runHolder = new JPanel();
		autogen(false);
		if (auto.isSelected()) {
			auto.doClick();
		} else {
			user.doClick();
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
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Basic Options", firstTab);
		tab.addTab("Advanced Options", secondTab);
		this.add(tab, "Center");
		//this.addTab("Basic", (JComponent)firstTab);
		//this.addTab("Advanced", (JComponent)firstTab);
		// this.add(runHolder, "South");
		firstRead = true;
		// if (user.isSelected()) {
		// auto.doClick();
		// user.doClick();
		// }
		// else {
		// user.doClick();
		// auto.doClick();
		// }
		firstRead = false;
		change = false;
	}

	/**
	 * This method performs different functions depending on what menu items or
	 * buttons are selected.
	 */
	public void actionPerformed(ActionEvent e) {
		/*
		 * if (e.getActionCommand().contains("box")) { int num =
		 * Integer.parseInt(e.getActionCommand().substring(3)) - 1; if
		 * (!((JCheckBox) this.species.get(num).get(0)).isSelected()) {
		 * ((JComboBox) this.species.get(num).get(2)).setSelectedItem("0");
		 * editText(num); speciesPanel.revalidate(); speciesPanel.repaint(); for
		 * (int i = 1; i < this.species.get(num).size(); i++) {
		 * this.species.get(num).get(i).setEnabled(false); } } else {
		 * this.species.get(num).get(1).setEnabled(true); if (user.isSelected()) {
		 * for (int i = 2; i < this.species.get(num).size(); i++) {
		 * this.species.get(num).get(i).setEnabled(true); } } } } else
		 */
		change = true;
		if (e.getActionCommand().contains("text")) {
			// int num = Integer.parseInt(e.getActionCommand().substring(4)) -
			// 1;
			if (variables != null && user.isSelected()) {  // action source is any of the variables' combobox
				for (int i = 0; i < variables.size(); i++) {
					editText(i);
					// SB
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
				//for (int i = 0; i < variablesList.size(); i++) {//commented after adding allVars
				for (int i = 0; i < allVars.size(); i++) { 
					editText(i);  //editText not required??
					// SB
					/*if (divisionsL.get(i).size() >= combox_selected){
						for (int j = divisionsL.get(i).size() - 1; j >= (combox_selected-1) ; j--){
							divisionsL.get(i).remove(j); //combox_selected);
							//thresholds.get(variablesList.get(i)).remove(j); TODO:COULD USE THIS BELOW?
						}
					}*/
					//Added for replacing divisionsL by thresholds
					// String currentVar = ((JTextField) variables.get(i).get(0)).getText().trim();
					// int iThresholds =  thresholds.get(currentVar).size(); // GIVING EXCEPTION BCOz variables is null when called first; 
					//String currentVar = variablesList.get(i); //commented after adding allVars
					String currentVar = allVars.get(i);
					if (findReqdVarslIndex(currentVar) != -1){ // condition added after adding allVars
						//int iThresholds =  thresholds.get(variablesList.get(i)).size(); //commented after adding allVars
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
					}
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
					((JCheckBox)((JPanel) c).getComponent(1)).setSelected(true); // added after adding allVars
					((JCheckBox)((JPanel) c).getComponent(2)).setEnabled(true);// added after adding allVars
					((JCheckBox)((JPanel) c).getComponent(3)).setEnabled(true);
					((JTextField)((JPanel) c).getComponent(4)).setEnabled(true);
					((JComboBox)((JPanel) c).getComponent(5)).setEnabled(true);
				//	for (int i = 3; i < ((JPanel) c).getComponentCount(); i++) { // added after allVars
				//		((JPanel) c).getComponent(i).setEnabled(true); // added after adding allVars
				//	}
					//if (reqdVarsL.get(j-1).isInput()){ changed after adding allVars
					if (reqdVarsL.get(findReqdVarslIndex(s)).isInput()){
						((JCheckBox)((JPanel) c).getComponent(2)).setSelected(true); // // changed 1 to 2 after required
					}
					if (reqdVarsL.get(findReqdVarslIndex(s)).isCare()){
						((JCheckBox)((JPanel) c).getComponent(3)).setSelected(true); // // changed 1 to 2 after required
					}
					//for (Variable v : reqdVarsL){ //SB after required
					//	if ((v.getName()).equalsIgnoreCase(((JTextField)((JPanel) c).getComponent(0)).toString())){
					//		((JCheckBox)((JPanel) c).getComponent(1)).setSelected(true); // for required
					//	}
					//}
				} else {	// added after adding allVars
					((JCheckBox)((JPanel) c).getComponent(1)).setSelected(false);
					((JCheckBox)((JPanel) c).getComponent(2)).setEnabled(false);
					((JCheckBox)((JPanel) c).getComponent(3)).setEnabled(false);
					((JTextField)((JPanel) c).getComponent(4)).setEnabled(false);
					((JComboBox)((JPanel) c).getComponent(5)).setEnabled(false);
					for (int i = 6; i < ((JPanel) c).getComponentCount(); i++) { // added after allVars
						((JPanel) c).getComponent(i).setEnabled(false); // added after adding allVars
					}
				}
				j++;
			}
			biosim.setGlassPane(true);
		} else if (e.getSource() == numBins || e.getSource() == debug) {
			biosim.setGlassPane(true);
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
					if (((JCheckBox) variables.get(i).get(1)).isSelected()){
						if (((String)(((JComboBox) variables.get(i).get(5)).getSelectedItem())).equals("DMV")){
							int v = findReqdVarslIndex(((JTextField) variables.get(i).get(0)).getText().trim());
							reqdVarsL.get(v).forceDmvc(true);
							//reqdVarsL.get(v).setDmvc(true);
						} else if (((String)(((JComboBox) variables.get(i).get(5)).getSelectedItem())).equals("Cont")){
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
		else if (e.getActionCommand().contains("input")) {
			//int num = Integer.parseInt(e.getActionCommand().substring(5));  // -1; ??
			//reqdVarsL.get(num).setInput(!reqdVarsL.get(num).isInput());
			String var = e.getActionCommand().substring(5,e.getActionCommand().length());  // -1; ??
			/*for (int i = 0; i < reqdVarsL.size() ; i++){ // COMMENTED after adding required
				if (var.equalsIgnoreCase(reqdVarsL.get(i).getName())){
					reqdVarsL.get(i).setInput(!reqdVarsL.get(i).isInput());
					break;
				}
			}*/ 
			for (int i = 0 ; i < variables.size(); i++){
				if ((((JTextField) variables.get(i).get(0)).getText().trim()).equalsIgnoreCase(var)){
					if (((JCheckBox) variables.get(i).get(1)).isSelected()){
						reqdVarsL.get(findReqdVarslIndex(((JTextField) variables.get(i).get(0)).getText().trim())).setInput(((JCheckBox) variables.get(i).get(2)).isSelected());
					}
				}
			}
		}
		else if (e.getActionCommand().contains("care")) {
			String var = e.getActionCommand().substring(4,e.getActionCommand().length());  // -1; ??
			for (int i = 0 ; i < variables.size(); i++){
				if ((((JTextField) variables.get(i).get(0)).getText().trim()).equalsIgnoreCase(var)){
					if (((JCheckBox) variables.get(i).get(1)).isSelected()){
						reqdVarsL.get(findReqdVarslIndex(((JTextField) variables.get(i).get(0)).getText().trim())).setCare(((JCheckBox) variables.get(i).get(3)).isSelected());
					}
				}
			}
		}
		else if (e.getActionCommand().contains("required")) {
			//int num = Integer.parseInt(e.getActionCommand().substring(5));  // -1; ??
			//reqdVarsL.get(num).setInput(!reqdVarsL.get(num).isInput());
			String var = e.getActionCommand().substring(8,e.getActionCommand().length());  // -1; ??
			for (int i = 0 ; i < variables.size(); i++){
				String currentVar = ((JTextField) variables.get(i).get(0)).getText().trim();
				if ((currentVar).equalsIgnoreCase(var)){
					if (((JCheckBox) variables.get(i).get(1)).isSelected()){
						((JCheckBox) variables.get(i).get(2)).setEnabled(true);
						((JCheckBox) variables.get(i).get(3)).setEnabled(true);
						((JTextField) variables.get(i).get(4)).setEnabled(true);
						((JComboBox) variables.get(i).get(5)).setEnabled(true);
						if ( auto.isSelected()) {
							//TODO:could disable the comboboxes & thresholds though
							// they would already be disabled here.
						}
						else{
							for (int j = 6; j < variables.get(i).size(); j++) { // added after allVars
								variables.get(i).get(j).setEnabled(true); // added after adding allVars
							}
						}
					    if (findReqdVarslIndex(var) == -1){
							reqdVarsL.add(new Variable(var));
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
		else if (e.getSource() == user) {
			if (!firstRead) {
				try {
					for (int i = 0; i < variables.size(); i++) {
						/*if (divisionsL.get(i).size() == 0){  // This condition added later.. This ensures that when you switch from auto to user, the options of auto are written to the textboxes. SB.. rechk
							for (int j = 4; j < variables.get(i).size(); j++) { // changed 2 to 3 SB
								if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
								//divisionsL.get(i).set(j-3,null);
							//	if (divisionsL.get(i).size() < (j-3)){
							//		divisionsL.get(i).set(j-3,null);
							//	}
							//	else{
							//		divisionsL.get(i).add(null);
							//	} 
								} else {
								//divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
									if (divisionsL.get(i).size() <= (j-4)){ // changed 3 to 4 after required
										divisionsL.get(i).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
										//thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
									}
									else{
										divisionsL.get(i).set(j-4,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim())); // changed 3 to 4 after required
										//thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
									}
								}
							}
						}*/
						String currentVar = ((JTextField) variables.get(i).get(0)).getText().trim();
						if (findReqdVarslIndex(currentVar) != -1){
							((JCheckBox) variables.get(i).get(2)).setEnabled(true);
							((JCheckBox) variables.get(i).get(3)).setEnabled(true);
							((JTextField) variables.get(i).get(4)).setEnabled(true);
							((JComboBox) variables.get(i).get(5)).setEnabled(true);
							((JComboBox) variables.get(i).get(6)).setEnabled(true);
							ArrayList<Double> iThresholds =  thresholds.get(currentVar);
							if ((iThresholds == null) || ( iThresholds.size() == 0)){  // This condition added later.. This ensures that when you switch from auto to user, the options of auto are written to the textboxes. SB.. rechk
								for (int j = 7; j < variables.get(i).size(); j++) { // changed 2 to 3 SB
									if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
										//divisionsL.get(i).set(j-3,null);
										/*	if (divisionsL.get(i).size() < (j-3)){
											divisionsL.get(i).set(j-3,null);
										}
										else{
											divisionsL.get(i).add(null);
										} */
									} else {
										//divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
										if ((iThresholds == null) || (iThresholds.size() <= (j-7))){ // changed 3 to 4 after required
											thresholds.get(currentVar).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
										}
										else{
											thresholds.get(currentVar).set(j-7,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim())); // changed 3 to 4 after required
										}
									}
								}
							}
						}
						else{
							((JCheckBox) variables.get(i).get(2)).setEnabled(false);
							((JCheckBox) variables.get(i).get(3)).setEnabled(false);
							((JTextField) variables.get(i).get(4)).setEnabled(false);
							((JComboBox) variables.get(i).get(5)).setEnabled(false);
							((JComboBox) variables.get(i).get(6)).setEnabled(false);
						}
//						write.write("\n");
					}
//					write.close();
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(biosim.frame(),
							"Unable to save thresholds!",
							"Error saving thresholds", JOptionPane.ERROR_MESSAGE);
				}
			}
			numBinsLabel.setEnabled(false);
			numBins.setEnabled(false);
			suggest.setEnabled(true);
			// levelsBin();
			variablesPanel.revalidate();
			variablesPanel.repaint();
			levels(); // To be added later? if the scaled divisions are not supposed to be shown after auto to user switching, then should have something like divsB4scaling which should be passed as a parameter to levels()
		} else if (e.getSource() == auto) {
			numBinsLabel.setEnabled(true);
			numBins.setEnabled(true);
			suggest.setEnabled(false);
			//int j = 0;  // recheck .. SB
			for (Component c : variablesPanel.getComponents()) {
				for (int i = 6; i < ((JPanel) c).getComponentCount(); i++) { // changed 1 to 2 SB
					((JPanel) c).getComponent(i).setEnabled(false);
				}
			/*	if (j == 0){   // recheck .. SB
					j++;		// SB
					continue;	// SB
				}				//SB
				((JCheckBox)((JPanel) c).getComponent(1)).addActionListener(this); // SB
				((JCheckBox)((JPanel) c).getComponent(1)).setActionCommand("input" + j); // SB */
			}
		} else if (e.getSource() == suggest) {
			suggestIsSource = true;
			autogen(false);
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
						((JCheckBox)((JPanel) c).getComponent(2)).setSelected(true); // SB // changed 1 to 2 after required
					}
					if (reqdVarsL.get(ind).isCare()){ //tempPorts.get(j-1)){
						((JCheckBox)((JPanel) c).getComponent(3)).setSelected(true); // SB // changed 1 to 2 after required
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
			viewLhpn();
		} else if (e.getSource() == viewLog) {
			viewLog();
		} else if (e.getSource() == saveLhpn) {
			saveLhpn();
		} else if (e.getSource() == viewCoverage) {  // SB
			viewCoverage();
		} else if (e.getSource() == viewVHDL) {  // SB
			viewVHDL();
		} else if (e.getSource() == viewVerilog) {  // SB
			viewVerilog();
		}
	}

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
//				FileWriter write = new FileWriter(new File(directory + separator + binFile));
//				FileWriter writeNew = new FileWriter(new File(directory	+ separator + newBinFile));
				// write.write("time 0\n");
				// boolean flag = false;
				// for (int i = 0; i < variables.size(); i++) {
				// if (((JCheckBox) variables.get(i).get(1)).isSelected()) {
				// if (!flag) {
				// write.write(".dmvc ");
				// writeNew.write(".dmvc ");
				// flag = true;
				// }
				// write.write(((JTextField)
				// variables.get(i).get(0)).getText().trim() + " ");
				// writeNew.write(((JTextField)
				// variables.get(i).get(0)).getText().trim()
				// + " ");
				// }
				// }
				// if (flag) {
				// write.write("\n");
				// writeNew.write("\n");
				// }
				for (int i = 0; i < variables.size(); i++) {
					// if (!((JCheckBox) variables.get(i).get(1)).isSelected())
					// {
//						if (((JTextField) variables.get(i).get(0)).getText().trim().equals("")) {
//						write.write("?");
//						writeNew.write("?");
//					} else {
//						write.write(((JTextField) variables.get(i).get(0)).getText().trim());
//						writeNew.write(((JTextField) variables.get(i).get(0)).getText().trim());
//					}
					// write.write(" " + ((JComboBox)
					// variables.get(i).get(1)).getSelectedItem());
					for (int j = 7; j < variables.get(i).size(); j++) {  // changed 2 to 3 SB
						if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
//							write.write(" ?");
//							writeNew.write(" ?");
					//		divisionsL.get(i).set(j-3,null);
						} else {
//							write.write(" "	+ ((JTextField) variables.get(i).get(j)).getText().trim());
//							writeNew.write(" " + ((JTextField) variables.get(i).get(j)).getText().trim());
					//		divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
						/*	if (divisionsL.get(i).size() <= (j-4)){ // changed 3 to 4 after required
								divisionsL.get(i).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
							else{
								divisionsL.get(i).set(j-4,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim())); // changed 3 to 4 after required
								//thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}*/
							if (thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).size() <= (j-7)){ // changed 3 to 4 after required
								thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
							else{
								thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).set(j-7,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim())); // changed 3 to 4 after required
							}
						}
					}
//					write.write("\n");
//					writeNew.write("\n");
					// }
				}
//				write.close();
//				writeNew.close();
				// Integer numThresh =
				// Integer.parseInt(numBins.getSelectedItem().toString()) - 1;
				// Thread myThread = new Thread(this);
				generate = true;
				execute = false;
				LearnThread = new Thread(this);
				LearnThread.start();
			}
		} catch (Exception e1) {
			// e1.printStackTrace();
			levels();
		}
	}

	private void levels() {  // based on the current data, create/update the variablesPanel???
	/*	ArrayList<String> str = null;
		try {
			Scanner f = new Scanner(new File(directory + separator + binFile));
			// log.addText(directory + separator + binFile);
			str = new ArrayList<String>();
			str.add(f.nextLine());
			while (f.hasNextLine()) {
				str.add(f.nextLine());
			}
			f.close();
			// System.out.println("here " + str.toString());
		} catch (Exception e1) {
		} */
		if (!directory.equals("")) {
			if (true) {
				// System.out.println(str.toString());
				variablesPanel.removeAll();
				this.variables = new ArrayList<ArrayList<Component>>();
				//variablesPanel.setLayout(new GridLayout(variablesList.size() + 1, 1)); //commented after adding allVars
				variablesPanel.setLayout(new GridLayout(allVars.size() + 1, 1));
				int max = 0;
				if (!thresholds.isEmpty()){
					for (String s : thresholds.keySet()){
						if (thresholds.get(s) != null) {
							max = Math.max(max, thresholds.get(s).size()+2);
						}
					}
				}
				JPanel label = new JPanel(new GridLayout());
				label.add(new JLabel("Variables"));
				// label.add(new JLabel("DMV"));
				label.add(new JLabel("Use")); //SB
				label.add(new JLabel("Input")); //SB
				label.add(new JLabel("Care")); //SB
				label.add(new JLabel("Epsilon")); //SB
				label.add(new JLabel("Type")); //SB
				label.add(new JLabel("Number Of Bins"));
				for (int i = 0; i < max - 2; i++) { 
					label.add(new JLabel("Level " + (i + 1)));
				}
				variablesPanel.add(label);
				int j = 0;
				//for (String s : variablesList) {// commented after adding allVars
				for (String s : allVars) {
					j++;
					JPanel sp = new JPanel(new GridLayout());
					ArrayList<Component> specs = new ArrayList<Component>();
					// JCheckBox check = new JCheckBox();
					// check.setSelected(true);
					// specs.add(check);
					specs.add(new JTextField(s));
					String[] options = { "Auto", "2", "3", "4", "5", "6", "7", "8", "16", "32"};//, "10", "11", "12", "13", "14", "15", "16", "17", "33", "65", "129", "257" };
					JComboBox combo = new JComboBox(options);
					// String[] dmvOptions = { "", "Yes", "No" };
					// JComboBox dmv = new JComboBox(dmvOptions);
					// JCheckBox dmv = new JCheckBox();
					JCheckBox required = new JCheckBox(); //SB
					JCheckBox input = new JCheckBox(); //SB
					JCheckBox care = new JCheckBox(); //SB
					JTextField epsilonTb = new JTextField(epsilonG.getText().trim()); //SB
					String[] dmvOptions = {"DMV", "Cont", "Auto"};
					JComboBox dmv = new JComboBox(dmvOptions);
					// dmv.setSelectedIndex(0);
					// dmv.addActionListener(this);
					input.addActionListener(this); //SB
					required.addActionListener(this); //SB
					care.addActionListener(this); //SB
					dmv.addActionListener(this); //SB
					// dmv.setActionCommand("dmv" + j);
				//	required.setActionCommand("required" + variablesList.get(j-1)); // SB commented after adding allVars
				//	input.setActionCommand("input" + variablesList.get(j-1)); // SB commented after adding allVars
					required.setActionCommand("required" + s);
					input.setActionCommand("input" + s);
					care.setActionCommand("care" + s);
					dmv.setActionCommand("DMV" + s);
					dmv.setSelectedItem("Auto");
					combo.setSelectedItem(numBins.getSelectedItem());
					// specs.add(dmv);
					specs.add(required); //SB
					specs.add(input); //SB
					specs.add(care); //SB
					specs.add(epsilonTb); //SB
					specs.add(dmv);
					specs.add(combo);
					((JTextField) specs.get(0)).setEditable(false);
					// sp.add(specs.get(0));
					// ((JCheckBox) specs.get(0)).addActionListener(this);
					// ((JCheckBox) specs.get(0)).setActionCommand("box" + j);
					sp.add(specs.get(0));
					sp.add(specs.get(1));
					sp.add(specs.get(2));  // Uncommented SB
					sp.add(specs.get(3));  // after required SB
					sp.add(specs.get(4));
					sp.add(specs.get(5));
					sp.add(specs.get(6));
					/*if ((j-1) < reqdVarsL.size() && reqdVarsL.get(j-1).isInput()){ // changed after adding required.
						((JCheckBox) specs.get(2)).setSelected(true); // changed 1 to 2 after required
					}
					else{
						((JCheckBox) specs.get(2)).setSelected(false); // changed 1 to 2 after required
					}*/
					//if ((j-1) < reqdVarsL.size() && reqdVarsL.get(j-1).isInput()){ // changed after adding allVars
					if (findReqdVarslIndex(s) != -1){
						((JCheckBox) specs.get(1)).setSelected(true); // changed 1 to 2 after required
						((JCheckBox) specs.get(2)).setEnabled(true); // added after allVars
						((JCheckBox) specs.get(3)).setEnabled(true); // added after allVars
						((JTextField) specs.get(4)).setEnabled(true); // added after allVars
						((JComboBox) specs.get(5)).setEnabled(true);
						((JComboBox) specs.get(6)).setEnabled(true);
						/*for (int i = 3; i < specs.size(); i++) { // added after allVars
							specs.get(i).setEnabled(true);
						}*/
						if (reqdVarsL.get(findReqdVarslIndex(s)).isInput()){
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
						/*if ((dmvDetectDone || dmvStatusLoaded) || reqdVarsL.get(findReqdVarslIndex(s)).isForcedCont()  || reqdVarsL.get(findReqdVarslIndex(s)).isForcedDmv()){
							if (reqdVarsL.get(findReqdVarslIndex(s)).isDmvc()){
								((JComboBox) specs.get(3)).setSelectedItem("DMV");
							}
							else{
								((JComboBox) specs.get(3)).setSelectedItem("Cont");
							}
						} else {
							((JComboBox) specs.get(3)).setSelectedItem("Auto");
						}*/
						if (!dmvDetectDone && !dmvStatusLoaded)
							((JComboBox) specs.get(5)).setSelectedItem("Auto");
						else{
							if (reqdVarsL.get(findReqdVarslIndex(s)).isDmvc()){
								((JComboBox) specs.get(5)).setSelectedItem("DMV");
							}
							else{
								((JComboBox) specs.get(5)).setSelectedItem("Cont");
							}
						}
					}
					else{	// variable not required
						((JCheckBox) specs.get(1)).setSelected(false); // changed 1 to 2 after required
						((JCheckBox) specs.get(2)).setEnabled(false); // added after allVars
						((JCheckBox) specs.get(3)).setEnabled(false); // added after allVars
						((JTextField) specs.get(4)).setEnabled(false); // added after allVars
						((JComboBox) specs.get(5)).setEnabled(false);
						((JComboBox) specs.get(6)).setEnabled(false);
						/*for (int i = 3; i < specs.size(); i++) { // added after allVars
							specs.get(i).setEnabled(false);
						}*/
					}
					((JComboBox) specs.get(6)).addActionListener(this); // changed 1 to 2 SB
					((JComboBox) specs.get(6)).setActionCommand("text" + j);// changed 1 to 2 SB
//TODO: BETTER BE THIS		((JComboBox) specs.get(3)).setActionCommand("text" + variablesList.get(j-1));// changed 1 to 2 SB
					this.variables.add(specs);
					if (!thresholds.isEmpty()) {
						boolean found = false;
						//		if ((j-1) < divisionsL.size()) {		COMMENTED BY SB
						//			divisionsL.add(null);
						//		}
						// ArrayList<Double> div =  divisionsL.get(j-1);
						//ArrayList<Double> div =  thresholds.get(variablesList.get(j-1)); //Replacing divisionsL by thresholds; commented after adding allVars
						if (findReqdVarslIndex(s) != -1){	//This condition added after adding allvarsL
							ArrayList<Double> div =  thresholds.get(s);
							// log.addText(s + " here " + st); String[] getString = st.split(" "); log.addText(getString[0] + s);
							//found = true; //moved this down after adding allVars
							//	if (getString.length >= 1) {
							if ((div != null) && (div.size() > 0)){ //changed >= to >
								//((JComboBox) specs.get(2)).setSelectedItem("div.size()+1");// Treats div.size() as string & doesn't work.. changed 1 to 2 SB
								found = true;
								((JComboBox) specs.get(6)).setSelectedItem(String.valueOf(div.size()+1));// changed 1 to 2 SB
								String selected = (String) ((JComboBox) specs.get(6)).getSelectedItem();
								int combox_selected;
								if (!selected.equalsIgnoreCase("Auto"))
									combox_selected = Integer.parseInt(selected);
								else
									combox_selected = 0;
								for (int i = 0; i < (combox_selected - 1); i++) {// changed 1 to 2 SB
									if (div.isEmpty() || div.size() < i) {
										specs.add(new JTextField(""));
									} else {
										// log.addText(getString[i+1]);
										specs.add(new JTextField(div.get(i).toString()));
									}
									// if (((JCheckBox) specs.get(1)).isSelected()) {
									// log.addText("here");
									// ((JTextField) specs.get(i + 2)).setEditable(false);
									// }
									sp.add(specs.get(i + 7)); // changed 2 to 3 SB
								}
								selected = (String) ((JComboBox) specs.get(6)).getSelectedItem();
								if (!selected.equalsIgnoreCase("Auto"))
									combox_selected = Integer.parseInt(selected);
								else
									combox_selected = 0;
								for (int i = combox_selected - 1; i < max - 2; i++) {// changed 1 to 2 SB
									sp.add(new JLabel());
								}
							}
							else{	// if (!found){
								String selected = (String) ((JComboBox) specs.get(6)).getSelectedItem();
								int combox_selected;
								if (!selected.equalsIgnoreCase("Auto"))
									combox_selected = Integer.parseInt(selected);
								else
									combox_selected = 0;
								for (int i = 0; i < combox_selected - 1; i++) {// changed 1 to 2 SB
									specs.add(new JTextField(""));
									sp.add(specs.get(i + 7));// changed 1 to 2 SB // changed to 4 expecting a bug
								}
								selected = (String) ((JComboBox) specs.get(6)).getSelectedItem();
								if (!selected.equalsIgnoreCase("Auto"))
									combox_selected = Integer.parseInt(selected);
								else
									combox_selected = 0;
								for (int i = combox_selected - 1; i < max - 2; i++) {// changed 1 to 2 SB
									sp.add(new JLabel());
								}
							}
							String selected = (String) ((JComboBox) specs.get(6)).getSelectedItem();
							int combox_selected;
							if (!selected.equalsIgnoreCase("Auto"))
								combox_selected = Integer.parseInt(selected);
							else
								combox_selected = 0;
							for (int i = 0; i < combox_selected - 1; i++) {
								((JTextField) specs.get(i + 7)).setEnabled(true);
							}
							((JCheckBox) specs.get(1)).setSelected(true);
							((JCheckBox) specs.get(2)).setEnabled(true);
							((JCheckBox) specs.get(3)).setEnabled(true);
							((JTextField) specs.get(4)).setEnabled(true);
							((JComboBox) specs.get(5)).setEnabled(true);
							((JComboBox) specs.get(6)).setEnabled(true);
							if (reqdVarsL.get(findReqdVarslIndex(s)).isInput()){ // This was there before. removed on june 29 thinking redundant
								((JCheckBox) specs.get(2)).setSelected(true);
							}
							if (reqdVarsL.get(findReqdVarslIndex(s)).isCare()){
								((JCheckBox) specs.get(3)).setSelected(true);
							}
							/*if ((dmvDetectDone | dmvStatusLoaded) || reqdVarsL.get(findReqdVarslIndex(s)).isForcedCont()  || reqdVarsL.get(findReqdVarslIndex(s)).isForcedDmv()){
								if (reqdVarsL.get(findReqdVarslIndex(s)).isDmvc()){
									((JComboBox) specs.get(3)).setSelectedItem("DMV");
								}
								else{
									((JComboBox) specs.get(3)).setSelectedItem("Cont");
								}
							} else {
								((JComboBox) specs.get(3)).setSelectedItem("Auto");
							}*/
							if (!dmvDetectDone && !dmvStatusLoaded)
								((JComboBox) specs.get(5)).setSelectedItem("Auto");
							else{
								if (reqdVarsL.get(findReqdVarslIndex(s)).isDmvc()){
									((JComboBox) specs.get(5)).setSelectedItem("DMV");
								}
								else{
									((JComboBox) specs.get(5)).setSelectedItem("Cont");
								}
							}
						}
						else{
							((JCheckBox) specs.get(1)).setSelected(false);
							((JCheckBox) specs.get(2)).setEnabled(false);
							((JCheckBox) specs.get(3)).setEnabled(false);
							((JTextField) specs.get(4)).setEnabled(false);
							((JComboBox) specs.get(5)).setEnabled(false);
							((JComboBox) specs.get(6)).setEnabled(false);
							//for (int k = 1; k < variables.size(); k++){
							//	if ((((JTextField) variables.get(k).get(0)).getText().trim()).equalsIgnoreCase(s)){
							//	}
							//}
							String selected = (String) ((JComboBox) specs.get(6)).getSelectedItem();
							int combox_selected;
							if (!selected.equalsIgnoreCase("Auto"))
								combox_selected = Integer.parseInt(selected);
							else
								combox_selected = 0;
							for (int i = 0; i < combox_selected - 1; i++) {
								selected = (String) ((JComboBox) specs.get(6)).getSelectedItem();
								int combo_selected;
								if (!selected.equalsIgnoreCase("Auto"))
									combo_selected = Integer.parseInt(selected);
								else
									combo_selected = 0;
								if ((specs.size() -7) < combo_selected - 1){
									specs.add(new JTextField(""));
									sp.add(specs.get(i + 7));
									((JTextField) specs.get(i + 7)).setEnabled(false);
								}
								else{
									((JTextField) specs.get(i + 7)).setEnabled(false);
								}
							}
						}
					} else {
						if (findReqdVarslIndex(((JTextField) sp.getComponent(0)).getText().trim()) != -1){
							((JCheckBox) specs.get(1)).setSelected(true);
							((JCheckBox) specs.get(2)).setEnabled(true);
							((JCheckBox) specs.get(3)).setEnabled(true);
							((JTextField) specs.get(4)).setEnabled(true);
							((JComboBox) specs.get(5)).setEnabled(true);
							((JComboBox) specs.get(6)).setEnabled(true);
							if (reqdVarsL.get(findReqdVarslIndex(s)).isInput()){
								((JCheckBox) specs.get(2)).setSelected(true);
							}
							if (reqdVarsL.get(findReqdVarslIndex(s)).isCare()){
								((JCheckBox) specs.get(3)).setSelected(true);
							}
							/*if ((dmvDetectDone | dmvStatusLoaded) || reqdVarsL.get(findReqdVarslIndex(s)).isForcedCont()  || reqdVarsL.get(findReqdVarslIndex(s)).isForcedDmv()){
								if (reqdVarsL.get(findReqdVarslIndex(s)).isDmvc()){
									((JComboBox) specs.get(3)).setSelectedItem("DMV");
								}
								else{
									((JComboBox) specs.get(3)).setSelectedItem("Cont");
								}
							} else {
								((JComboBox) specs.get(3)).setSelectedItem("Auto");
							}*/
							if (!dmvDetectDone && !dmvStatusLoaded)
								((JComboBox) specs.get(5)).setSelectedItem("Auto");
							else{
								if (reqdVarsL.get(findReqdVarslIndex(s)).isDmvc()){
									((JComboBox) specs.get(5)).setSelectedItem("DMV");
								}
								else{
									((JComboBox) specs.get(5)).setSelectedItem("Cont");
								}
							}
						}	
						else{
							((JCheckBox) specs.get(1)).setSelected(false);
							((JCheckBox) specs.get(2)).setEnabled(false);
							((JCheckBox) specs.get(3)).setEnabled(false);
							((JTextField) specs.get(4)).setEnabled(false);
							((JComboBox) specs.get(5)).setEnabled(false);
							((JComboBox) specs.get(6)).setEnabled(false);
						}
						String selected = (String) ((JComboBox) specs.get(6)).getSelectedItem();
						int combox_selected;
						if (!selected.equalsIgnoreCase("Auto"))
							combox_selected = Integer.parseInt(selected);
						else
							combox_selected = 0;
						for (int i = 0; i < combox_selected - 1; i++) {// changed 1 to 2 SB
							specs.add(new JTextField(""));
							sp.add(specs.get(i + 7));// changed 1 to 2 SB //changed to 4 bcoz of a bug
							if (findReqdVarslIndex(((JTextField) sp.getComponent(0)).getText().trim()) != -1){
								((JTextField) specs.get(i + 7)).setEnabled(true);
							}
							else{
								((JTextField) specs.get(i + 7)).setEnabled(false);
							}
						}
					}
					variablesPanel.add(sp);
				}
			}
		}
		editText(0);
	}

	private void editText(int num) { // adjusts number of boxes for thresholds
		try {
			ArrayList<Component> specs = variables.get(num);
			Component[] panels = variablesPanel.getComponents();
			String selected = (String) (((JComboBox) specs.get(6)).getSelectedItem()); // changed 2 to 3 after required
			int boxes;
			if (!selected.equalsIgnoreCase("Auto"))
				boxes = Integer.parseInt(selected); // changed 2 to 3 after required
			else 
				boxes = 0;
			//int boxes = Integer.parseInt((String) ((JComboBox) specs.get(6)).getSelectedItem()); //changed 1 to 2 SB
			if ((specs.size() - 7) < boxes) { // changed 2 to 3 SB
				for (int i = 0; i < boxes - 1; i++) {
					try {
						specs.get(i + 7); // changed 2 to 3 SB
					} catch (Exception e1) {
						JTextField temp = new JTextField("");
						((JPanel) panels[num + 1]).add(temp);
						specs.add(temp);
					}
				}
			} else {
				try {
					if (boxes > 0) {
						while (true) {
							specs.remove(boxes + 6); // changed 1 to 2 SB
							((JPanel) panels[num + 1]).remove(boxes + 6); // changed 1 to 2 SB
						}
					} else if (boxes == 0) {
						while (true) {
							specs.remove(7); // changed 2 to 3 SB
							((JPanel) panels[num + 1]).remove(7); // changed 2 to 3 SB
						}
					}
				} catch (Exception e1) {
				}
			}
			int max = 0;
			for (int i = 0; i < this.variables.size(); i++) {
				max = Math.max(max, variables.get(i).size());
			}
			if (((JPanel) panels[0]).getComponentCount() < max) {
				for (int i = 0; i < max - 7; i++) { //changed 2 to 3 SB
					try {
						((JPanel) panels[0]).getComponent(i + 7); //changed 2 to 3 SB
					} catch (Exception e) {
						((JPanel) panels[0]).add(new JLabel("Level " + (i + 1)));
					}
				}
			} else {
				try {
					while (true) {
						((JPanel) panels[0]).remove(max);
					}
				} catch (Exception e) {
				}
			}
			for (int i = 1; i < panels.length; i++) {
				JPanel sp = (JPanel) panels[i];
				for (int j = sp.getComponentCount() - 1; j >= 7; j--) {//changed 2 to 3 SB
					if (sp.getComponent(j) instanceof JLabel) {
						sp.remove(j);
					}
				}
				if (max > sp.getComponentCount()) {
					for (int j = sp.getComponentCount(); j < max; j++) {
						sp.add(new JLabel());
					}
				} else {
					for (int j = sp.getComponentCount() - 6; j >= max; j--) {//changed 2 to 3 SB .. not sure??
						sp.remove(j);
					}
				}
			}
		} catch (Exception e) {
		}
	}

	private int findReqdVarslIndex(String s) {
		for (int i = 0; i < reqdVarsL.size() ; i++){
			if (s.equalsIgnoreCase(reqdVarsL.get(i).getName())){
				return i;
			}
		}
		return -1;
	}
	
	public void saveLhpn() {
		try {
			if (true) {// (new File(directory + separator +
				// "method.gcm").exists()) {
				String copy = JOptionPane.showInputDialog(biosim.frame(),
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
				biosim.saveLhpn(copy, directory + separator + lhpnFile);
			} else {
				JOptionPane.showMessageDialog(biosim.frame(),
						"No circuit has been generated yet.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to save model.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewLhpn() {
		try {
			File work = new File(directory);
			if (new File(directory + separator + lhpnFile).exists()) {
				String dotFile = lhpnFile.replace(".lpn", ".dot");
				File dot = new File(directory + separator + dotFile);
				dot.delete();
				log.addText("Executing:\n" + "atacs -cPllodpl " + lhpnFile);
				Runtime exec = Runtime.getRuntime();
				Process load = exec.exec("atacs -cPllodpl " + lhpnFile, null,
						work);
				load.waitFor();
				if (dot.exists()) {
					viewLhpn.setEnabled(true);
					String command = "";
					if (System.getProperty("os.name").contentEquals("Linux")) {
						command = "gnome-open " + dotFile;
						log.addText("gnome-open " + directory + separator
								+ dotFile + "\n");
					} else {
						command = "open " + dotFile;
						log.addText("open " + directory + separator + dotFile
								+ "\n");
					}
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
					JOptionPane.showMessageDialog(biosim.frame(), scrolls,
							"Log", JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(biosim.frame(),
						"No circuit has been generated yet.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(),
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
				JOptionPane.showMessageDialog(biosim.frame(), scrolls,
						"Run Log", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(biosim.frame(),
						"No run log exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to view run log.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// SB
	
	public void viewLearnComplete() {
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
				JOptionPane.showMessageDialog(biosim.frame(), scrolls,
						"Coverage Report", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(biosim.frame(),
						"No Coverage Report exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(),
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
				JOptionPane.showMessageDialog(biosim.frame(), scrolls,
						"VHDL-AMS Model", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(biosim.frame(),
						"VHDL-AMS model does not exist.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(),
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
				JOptionPane.showMessageDialog(biosim.frame(), scrolls,
						"Verilog-AMS Model", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Verilog-AMS model does not exist.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to view Verilog-AMS model.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void save() {
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(new File(directory + separator + lrnFile));
			prop.load(in);
			in.close();
			prop.setProperty("learn.file", learnFile);
			prop.setProperty("learn.iter", this.iteration.getText().trim());
			prop.setProperty("learn.valueScaling", this.globalValueScaling.getText().trim());
			prop.setProperty("learn.delayScaling", this.globalDelayScaling.getText().trim());
			prop.setProperty("learn.bins", (String) this.numBins.getSelectedItem());
			prop.setProperty("learn.prop", (String) this.propertyG.getText().trim());
			String varsList = null;
			if (range.isSelected()) {
				prop.setProperty("learn.equal", "range");
			} else {
				prop.setProperty("learn.equal", "points");
			}
			if (auto.isSelected()) {
				prop.setProperty("learn.use", "auto");
				int k = 0;  // added later .. so that the exact divisions are stored to file when auto is selected. & not the divisions in the textboxes
				int inputCount = 0, dmvCount = 0, contCount = 0, autoVarCount = 0, dontcareCount = 0;
				String ip = null, dmv = null, cont = null, autoVar = null, dontcares = null;
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
					if (((JCheckBox)((JPanel)c).getComponent(1)).isSelected()){ // added after required
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
						if (((JCheckBox)((JPanel)c).getComponent(2)).isSelected()){  // changed 1 to 2 after required
							if (inputCount == 0){
								inputCount++;
								ip = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								ip = ip + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
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
						if (((String)(((JComboBox)((JPanel)c).getComponent(5)).getSelectedItem())).equals("Cont")){ 
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
				int inputCount = 0, dmvCount = 0, contCount = 0, autoVarCount = 0, dontcareCount = 0;
				String ip = null, dmv = null, cont = null, autoVar = null, dontcares = null;
				for (Component c : variablesPanel.getComponents()) {
					if (k == 0){
						k++;
						continue;
					}
					if (((JCheckBox)((JPanel)c).getComponent(1)).isSelected()){
						if (varsList == null) { //(k == 1){
							varsList = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
						}
						else{
							varsList += " "+ ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
						}
						String selected = (String)((JComboBox)((JPanel)c).getComponent(6)).getSelectedItem();  // changed to 3 after required
						int numOfBins;
						if (!selected.equalsIgnoreCase("Auto"))
							numOfBins = Integer.parseInt(selected);
						else
							numOfBins = 0;
						String s =  ((JTextField)((JPanel)c).getComponent(0)).getText().trim() + " " + numOfBins;  // changed to 3 after required
						//int numOfBins = Integer.parseInt((String)((JComboBox)((JPanel)c).getComponent(6)).getSelectedItem())-1; // changed to 3 after required
						for (int i = 0; i < numOfBins; i++){
							s += " ";
							s += ((JTextField)(((JPanel)c).getComponent(i+7))).getText().trim();// changed to 4 after required
						}
						prop.setProperty("learn.bins"+ ((JTextField)((JPanel)c).getComponent(0)).getText().trim(), s);
						if (((JCheckBox)((JPanel)c).getComponent(2)).isSelected()){ // changed to 2 after required
							if (inputCount == 0){
								inputCount++;
								ip = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();//((JTextField)((JPanel)c).getComponent(0)).getText().trim();
							}
							else{
								ip = ip + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
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
						if (((String)(((JComboBox)((JPanel)c).getComponent(5)).getSelectedItem())).equals("Cont")){  // changed 1 to 2 after required
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
			prop.store(out, learnFile);
			out.close();
			// log.addText("Creating levels file:\n" + directory + separator +
			// binFile + "\n");
			// String command = "autogenT.py -b" + binFile + " -t"
			// + numBins.getSelectedItem().toString() + " -i" +
			// iteration.getText();
			// if (range.isSelected()) {
			// command = command + " -cr";
			// }
			// File work = new File(directory);
			// Runtime.getRuntime().exec(command, null, work);
			change = false;
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to save parameter file!", "Error Saving File",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void reload(String newname) {
		// try {
		// Properties prop = new Properties();
		// FileInputStream in = new FileInputStream(new File(directory +
		// separator + lrnFile));
		// prop.load(in);
		// in.close();
		// prop.setProperty("learn.file", newname);
		// prop.setProperty("learn.iter", this.iteration.getText().trim());
		// prop.setProperty("learn.bins", (String)
		// this.numBins.getSelectedItem());
		// if (range.isSelected()) {
		// prop.setProperty("learn.equal", "range");
		// }
		// else {
		// prop.setProperty("learn.equal", "points");
		// }
		// if (auto.isSelected()) {
		// prop.setProperty("learn.use", "auto");
		// }
		// else {
		// prop.setProperty("learn.use", "user");
		// }
		// log.addText("Saving learn parameters to file:\n" + directory +
		// separator + lrnFile
		// + "\n");
		// FileOutputStream out = new FileOutputStream(new File(directory +
		// separator + lrnFile));
		// prop.store(out, learnFile);
		// out.close();
		// }
		// catch (Exception e1) {
		// //e1.printStackTrace();
		// JOptionPane.showMessageDialog(biosim.frame(), "Unable to save
		// parameter file!",
		// "Error Saving File", JOptionPane.ERROR_MESSAGE);
		// }
		backgroundField.setText(newname);
	}

	public void learn() {
		try {
			if (auto.isSelected()) {
				for (int i = 0; i < variables.size(); i++) {
					for (int j = 5; j < variables.get(i).size(); j++) { // changed to 4 after required
						if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
						} else {
							/*if (divisionsL.get(i).size() <= (j-4)){ // changed to 4 after required
								divisionsL.get(i).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
							else{ 
								divisionsL.get(i).set(j-4,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim())); // changed to 4 after required
								//thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}*/
							//Added for replacing divisionsL by thresholds
							String currentVar = ((JTextField) variables.get(i).get(0)).getText().trim();
							if (thresholds.get(currentVar) == null){
								thresholds.put(currentVar,new ArrayList<Double>());
								thresholds.get(currentVar).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							} else if (thresholds.get(currentVar).size() <= (j-5)){ // changed to 4 after required
								thresholds.get(currentVar).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
							else{ 
								thresholds.get(currentVar).set(j-5,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim())); // changed to 4 after required
							}
						}
					}
				}
				generate = true;
			} else {
				for (int i = 0; i < variables.size(); i++) {
					for (int j = 7; j < variables.get(i).size(); j++) {  // changed 2 to 3 SB
						if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {

						} else {
							/*if (divisionsL.get(i).size() <= (j-4)){ // changed to 4 after required
								divisionsL.get(i).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
							else{
								divisionsL.get(i).set(j-4,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim())); // changed to 4 after required
							}*/
							//Added for replacing divisionsL by thresholds
							String currentVar = ((JTextField) variables.get(i).get(0)).getText().trim();
							if (thresholds.get(currentVar) == null){
								thresholds.put(currentVar,new ArrayList<Double>());
								thresholds.get(currentVar).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
							else if (thresholds.get(currentVar).size() <= (j-7)){ // changed to 4 after required
								thresholds.get(currentVar).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
							else{ 
								thresholds.get(currentVar).set(j-7,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim())); // changed to 4 after required
							}
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

	public void run() {
		new File(directory + separator + lhpnFile).delete();
		fail = false;
		try {
			//File work = new File(directory);
			final JFrame running = new JFrame("Progress");
			//running.setUndecorated(true);
			final JButton cancel = new JButton("Cancel");
			running.setResizable(false);
			WindowListener w = new WindowListener() {
				public void windowClosing(WindowEvent arg0) {
				//	cancel.doClick();
					running.dispose();
				}
				
				public void windowOpened(WindowEvent arg0) {
				}

				public void windowClosed(WindowEvent arg0) {
				}

				public void windowIconified(WindowEvent arg0) {
				}

				public void windowDeiconified(WindowEvent arg0) {
				}

				public void windowActivated(WindowEvent arg0) {
				}

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
						if (cVar.equalsIgnoreCase(st1)){
							//int combox_selected = Integer.parseInt((String) ((JComboBox) variables.get(i).get(6)).getSelectedItem()); // changed 2 to 3 after required
							String selected = (String) (((JComboBox) variables.get(i).get(6)).getSelectedItem()); // changed 2 to 3 after required
							int combox_selected;
							if (!selected.equalsIgnoreCase("Auto"))
								combox_selected = Integer.parseInt(selected); // changed 2 to 3 after required
							else 
								combox_selected = 0;
							if (thresholds.get(st1).size() == (combox_selected -1)){
								for (int j = 0; j < thresholds.get(st1).size(); j++){
									if (!warned && (thresholds.get(st1).get(j) != Double.parseDouble((String) ((JTextField) variables.get(i).get(7+j)).getText()))){
										warned = true;
										out.write("WARNING: THRESHOLDS OF " + st1 + " NOT MATCHING THOSE IN THE GUI. WRONG!");
										JOptionPane.showMessageDialog(biosim.frame(),
												"Thresholds of " + st1 + " not matching those displayed in the gui.",
												"WARNING!", JOptionPane.WARNING_MESSAGE);
									}
								}
							} else {
								if (!warned){
									warned = true;
									out.write("WARNING: THRESHOLDS OF " + st1 + " NOT MATCHING THOSE IN THE GUI. WRONG!");
									JOptionPane.showMessageDialog(biosim.frame(),
											"Thresholds of " + st1 + " not matching those displayed in the gui.",
											"WARNING!", JOptionPane.WARNING_MESSAGE);
								}
							}
							break;
						}
					}
					for (Double d : thresholds.get(st1)){
						out.write(d + ";");
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
				HashMap<String, ArrayList<String>> stabMap = new HashMap<String, ArrayList<String>>();
				//ArrayList<String> stables = new ArrayList<String>();
		
		//For every op which can have transients, there's a corresponding variable
		//in stables. destabMap's keyset is same as stables.
		//For every variable in stables, destabMap has an entry which maps to 
		//to an arraylist containing ips which can destabilize that op.
		//Add a loop here to do so 	
				
				ArrayList<String> destab_out = new  ArrayList<String>();
				destab_out.add("ctl");
				destabMap.put("out", destab_out);
				//stables.add(new String("stable_out")); 
				Variable vStable = new Variable("stable_out");
				vStable.setCare(true); 
				vStable.setDmvc(true);
				vStable.setInput(true);
				vStable.setOutput(false);
				vStable.forceDmvc(true);
				vStable.setEpsilon(0.1); // since stable is always 0 or 1 and threshold is 0.5. even epsilon of 0.3 is fine
				varsWithStables.add(vStable);
				ArrayList<Double> tStable = new ArrayList<Double>();
				tStable.add(0.5);
				thresholds.put("stable_out", tStable);
				
				LhpnFile g = l.learnModel(directory, log, biosim, moduleNumber, thresholds, tPar, varsWithStables, destabMap, stabMap, valScaleFactor, delayScaleFactor, failProp);
				if (new File(learnFile).exists()){ //directory + separator + "complete.lpn").exists()){//
					LhpnFile seedLpn = new LhpnFile();
					seedLpn.load(learnFile);
					g = mergeLhpns(seedLpn,g);
				}
				valScaleFactor = l.getValueScaleFactor();
				delayScaleFactor = l.getDelayScaleFactor();
				globalValueScaling.setText(Double.toString(valScaleFactor));
				globalDelayScaling.setText(Double.toString(delayScaleFactor));
				
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
							LhpnFile moduleLPN = l.learnModel(directory, log, biosim, j, thresholds, tPar, varsT, destabMap, stabMap, valScaleFactor, delayScaleFactor, null);
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
					JFrame learnComplete = new JFrame();
					JOptionPane.showMessageDialog(learnComplete,
						    "Learning Complete.",
						    "LEMA",
						    JOptionPane.PLAIN_MESSAGE);

					//viewLhpn();
					biosim.updateMenu(true,true);
				} else {
				//	System.out.println(" does not exist \n");
					viewVHDL.setEnabled(false); 	// SB
					viewVerilog.setEnabled(false); 	// SB
					viewLhpn.setEnabled(false); 	// SB
					viewCoverage.setEnabled(false); // SB
					saveLhpn.setEnabled(false); 	// SB
					fail = true;
					biosim.updateMenu(true,false);
				}
			}
			out.close();
			running.setCursor(null);
			running.dispose();
			if (fail) {
				viewLog();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to create log file.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
			
		} 
		/*catch (RuntimeException e1) {
			JOptionPane.showMessageDialog(biosim.frame(),
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
			out.write("epsilon = " + epsilon + "; ratesampling = " + rateSampling + "; pathLengthBin = " + pathLengthBin + "; percent = " + percent + "; runlength = " + runLength + "; runtime = " + runTime + "; absoluteTime = " + absoluteTime + "; delayscalefactor = " + delayScaleFactor + "; valuescalefactor = " + valScaleFactor + "\n");
			tPar.put("epsilon", epsilon);
			tPar.put("pathLengthBin", Double.valueOf((double) pathLengthBin));
			tPar.put("pathLengthVar", Double.valueOf((double) pathLengthVar));
			tPar.put("rateSampling", Double.valueOf((double) rateSampling));
			tPar.put("percent", percent);
			if (absoluteTime)
				tPar.put("runTime", runTime);
			else
				tPar.put("runLength", Double.valueOf((double) runLength));
		} catch (IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to create log file.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		return tPar;
	}
	
	public String getProp(){
		String failProp = null;
		if (!(propertyG.getText()).equals("")){
			//BufferedReader prop = new BufferedReader(new FileReader(directory + separator + "learn" + ".prop"));
			failProp = propertyG.getText().trim();
			failProp = "~(" + failProp + ")";
			//failPropVHDL = failProp.replaceAll("~", "not ");
			//failPropVHDL = failPropVHDL.replaceAll("\\|", " or ");
			//failPropVHDL = failPropVHDL.replaceAll("\\&", " and ");
			//failPropVHDL = failPropVHDL.replaceAll(">=(-*[0-9]+\\.[0-9]*)", "'above($1)");
			//failPropVHDL = failPropVHDL.replaceAll(">=(-*[0-9]+)", "'above($1\\.0)");
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
		learnFile = newLearnFile;
		variablesList = new ArrayList<String>();
		/*
		 * if ((learnFile.contains(".vhd")) || (learnFile.contains(".lpn"))) {
		 * LHPNFile lhpn = new LHPNFile(); lhpn.load(directory + separator +
		 * learnFile); Set<String> ids = lhpn.getVariables().keySet(); /*try {
		 * FileWriter write = new FileWriter( new File(directory + separator +
		 * "background.g")); write.write("digraph G {\n"); for (String s : ids) {
		 * variablesList.add(s); write.write("s" + s + "
		 * [shape=ellipse,color=black,label=\"" + (s) + "\"" + "];\n"); }
		 * write.write("}\n"); write.close(); } catch (Exception e) {
		 * JOptionPane.showMessageDialog(biosim.frame(), "Unable to create
		 * background file!", "Error Writing Background",
		 * JOptionPane.ERROR_MESSAGE); } } else {
		 */
		thresholds = new HashMap<String, ArrayList<Double>>(); // SB
		reqdVarsL = new ArrayList<Variable>();				// SB
		LhpnFile lhpn = new LhpnFile();
		lhpn.load(learnFile);
		HashMap<String, Properties> variablesMap = lhpn.getContinuous();
		for (String s : variablesMap.keySet()) {
			variablesList.add(s);
			reqdVarsL.add(new Variable(s));					
			thresholds.put(s,new ArrayList<Double>());
		}
	// Loading the inputs and bins from the existing .lrn file.	
		Properties load = new Properties();
		try {
			FileInputStream in = new FileInputStream(new File(directory
					+ separator + lrnFile));
			load.load(in);
			in.close();
			int j = 0;
			if (load.containsKey("learn.varsList")){
				String varsListString = load.getProperty("learn.varsList");
				String[] varsList = varsListString.split("\\s");
				j = 0;
				for (String st1 : varsList){
					int varNum = findReqdVarslIndex(st1);
					if (varNum == -1){
						continue;
					}
					else{
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
		} catch (IOException e) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}
		/*
		 * try { FileWriter write = new FileWriter( new File(directory +
		 * separator + "background.gcm")); BufferedReader input = new
		 * BufferedReader(new FileReader(new File(learnFile))); String line =
		 * null; while ((line = input.readLine()) != null) { write.write(line +
		 * "\n"); } write.close(); input.close(); } catch (Exception e) {
		 * JOptionPane.showMessageDialog(biosim.frame(), "Unable to create
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

	/**
	 * This method generates an LHPN model from the simulation traces provided
	 * in the learn view. The generated LHPN is stored in an object of type
	 * lhpn2sbml.parser.LHPNfile . It is then saved in *.lpn file using the
	 * save() method of the above class.
	 * 
	 * Rev. 1 - Kevin Jones
	 * Rev. 2 - Scott Little (data2lhpn.py) 
	 * Rev. 3 - Satish Batchu ( dataToLHPN() )
	 */
/*
	public void dataToLHPN(JFrame running) {
		try {
			// Initializations being done in resetAll method added on Aug 12,2009. These 
			// initializations ensure that place,transition numbers start from 0 
			// everytime we click play button on LEMA though compiled only once. 
			//  Init values and rates being cleared for the same reason. 
			//
			resetAll();
			lowerLimit = new Double[reqdVarsL.size()];
			upperLimit = new Double[reqdVarsL.size()];
			// end Initializations 
			//String[] getRootDir = directory.split(separator);
			//String rootDir = directory;
			//rootDir = rootDir.replace(getRootDir[getRootDir.length - 1], "") ;
			//	logFile = new File(rootDir + "atacs.log");
			
			out.write("Running: dataToLHPN\n");
			TSDParser tsd = new TSDParser(directory + separator + "run-1.tsd",
					biosim, false);
			varNames = tsd.getSpecies();
			//String[] learnDir = lrnFile.split("\\.");
			//File cvgFile = new File(directory + separator + learnDir[0] + ".cvg");
			File cvgFile = new File(directory + separator + "run.cvg");
			cvgFile.createNewFile();
			BufferedWriter coverage = new BufferedWriter(new FileWriter(cvgFile));
			//FileOutputStream coverage = new FileOutputStream(cvgFile);
			// Check whether all the tsd files are following the same variables
			// & order vars = varNames.toArray(new String[varNames.size()]);
			int i = 1;
			lowerLimit[0] = -800.0;  // for integrator
			upperLimit[0] = 800.0;  // for integrator
			
			String failProp = "";
			g = new LhpnFile(); // The generated lhpn is stored in this object
			placeInfo = new HashMap<String, Properties>();
			transitionInfo = new HashMap<String, Properties>();
			cvgInfo = new HashMap<String, Properties>();
			transientNetPlaces = new HashMap<String, Properties>();
			transientNetTransitions = new HashMap<String, Properties>();
		//	ratePlaces = new ArrayList<String>(); moved this to updateRateInfo on jun 22, 2010. See if this causes problems.
			dmvcInputPlaces = new ArrayList<String>();
			propPlaces = new ArrayList<String>();
			if (!(propertyG.getText()).equals("")){
				//BufferedReader prop = new BufferedReader(new FileReader(directory + separator + "learn" + ".prop"));
				failProp = propertyG.getText().trim();
				failProp = "~(" + failProp + ")";
				failPropVHDL = failProp.replaceAll("~", "not ");
				failPropVHDL = failPropVHDL.replaceAll("\\|", " or ");
				failPropVHDL = failPropVHDL.replaceAll("\\&", " and ");
				failPropVHDL = failPropVHDL.replaceAll(">=(-*[0-9]+\\.[0-9]*)", "'above($1)");
				failPropVHDL = failPropVHDL.replaceAll(">=(-*[0-9]+)", "'above($1\\.0)");
				failProp = failProp.replaceAll("\\.[0-9]*","");
				Properties p0 = new Properties();
				placeInfo.put("failProp", p0);
				p0.setProperty("placeNum", numPlaces.toString());
				p0.setProperty("type", "PROP");
				p0.setProperty("initiallyMarked", "true");
				g.addPlace("p" + numPlaces, true);
				propPlaces.add("p" + numPlaces);
				numPlaces++;
				Properties p1 = new Properties();
				transitionInfo.put("failProp", p1);
				p1.setProperty("transitionNum", numTransitions.toString());
				g.addTransition("t" + numTransitions); // prevTranKey+key);
				g.addMovement("p" + placeInfo.get("failProp").getProperty("placeNum"), "t" + transitionInfo.get("failProp").getProperty("transitionNum")); 
				g.getTransition("t" + numTransitions).setFail(true);
				numTransitions++;
				//enFailAnd = "&~fail";
				//enFail = "~fail";
			}
			boolean defaultStim = defaultEnvG.isSelected();
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
			out.write("epsilon = " + epsilon + "; ratesampling = " + rateSampling + "; pathLengthBin = " + pathLengthBin + "; percent = " + percent + "; runlength = " + runLength + "; runtime = " + runTime + "; absoluteTime = " + absoluteTime + "; delayscalefactor = " + delayScaleFactor + "; valuescalefactor = " + valScaleFactor + "\n");
			dsFactor = delayScaleFactor;
			vsFactor = valScaleFactor;
			dmvcValuesUnique = new HashMap<String, Properties>();
			constVal = new ArrayList<HashMap<String, String>>();
			while (new File(directory + separator + "run-" + i + ".tsd").exists()) {
				Properties cProp = new Properties();
				cvgInfo.put(String.valueOf(i), cProp);
				cProp.setProperty("places", String.valueOf(0));
				cProp.setProperty("transitions", String.valueOf(0));
				cProp.setProperty("rates", String.valueOf(0));
				cProp.setProperty("delays", String.valueOf(0));
				tsd = new TSDParser(directory + separator + "run-" + i + ".tsd", biosim,false);
				data = tsd.getData();
//ORDER REVERSED this first then detectDMV				genBinsRates(divisionsL); // changes made here.. data being used was global before.
				//genBinsRates("run-" + i + ".tsd", divisionsL);
				findReqdVarIndices();
				genBinsRates(thresholds); // moved this above detectDMV on May 11,2010 assuming this order reversal won't affect things.
				detectDMV(data,false); // changes made here.. data being used was global before.
			//	genBinsRates(divisionsL); commented after replacing divisionsL with thresholds 
				updateGraph(bins, rates, i, cProp, running);
				//cProp.store(coverage,  "run-" + String.valueOf(i) + ".tsd");
				coverage.write("run-" + String.valueOf(i) + ".tsd\t");
				coverage.write("places : " + cProp.getProperty("places"));
				coverage.write("\ttransitions : " + cProp.getProperty("transitions") + "\n");
				i++;
			}
			coverage.close();
			for (String st1 : g.getTransitionList()) {
				out.write("\nTransition is " + st1);
				if (isTransientTransition(st1)){
					out.write(" Incoming place " + g.getPreset(st1)[0]);
					if (g.getPostset(st1).length != 0){
						out.write(" Outgoing place " + g.getPostset(st1)[0]);
					}
					continue;
				}
				String binEncoding = getPlaceInfoIndex(g.getPreset(st1)[0]);
				out.write(" Incoming place " + g.getPreset(st1)[0]
						+ " Bin encoding is " + binEncoding);
				if (g.getPostset(st1).length != 0){
					binEncoding = getPlaceInfoIndex(g.getPostset(st1)[0]);
					out.write(" Outgoing place " + g.getPostset(st1)[0]
						+ " Bin encoding is " + binEncoding);
				}
			}
			out.write("\nTotal no of transitions : " + numTransitions);
			out.write("\nTotal no of places : " + numPlaces);
			Properties initCond = new Properties();
			for (Variable v : reqdVarsL) {
				if (v.isDmvc()) {
					g.addInteger(v.getName(), v.getInitValue());
				} else {
					initCond.put("value", v.getInitValue());
					initCond.put("rate", v.getInitRate());
					g.addContinuous(v.getName(), initCond);
				}
			}
			//g.addOutput("fail", "false");
			HashMap<String, ArrayList<Double>> scaledThresholds; // scaledThresholds are scaleDiv
			// temporary
			//	Pattern pat = Pattern.compile("-*[0-9]+\\.*[0-9]*");
			//    Matcher m = pat.matcher(failProp);
			//   while(m.find()){
			//  	failProp = m.replaceFirst(String.valueOf(Double.parseDouble(m.group())*100.0));
			// }
			// System.out.println(failProp);
			//	for (String t : g.getTransitionList()) {
			//		if ((g.getPreset(t) != null) && (placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("PROP"))){
			//			g.addEnabling(t, failProp);	
			//		}
			//	} 
			// end temporary

			scaledThresholds = normalize();
			globalValueScaling.setText(Double.toString(valScaleFactor));
			globalDelayScaling.setText(Double.toString(delayScaleFactor));
			initCond = new Properties(); 
			for (Variable v : reqdVarsL) {	// Updating with scaled initial values & rates
				if (v.isDmvc()) {
					g.changeIntegerInitCond(v.getName(), v.getInitValue());
				} else {
					initCond.put("value", v.getInitValue());
					initCond.put("rate", v.getInitRate());
					g.changeContInitCond(v.getName(), initCond);
				}
			}
			String[] transitionList = g.getTransitionList();
		//	transEnablingsVHDL = new String[transitionList.length];
		//	transDelayAssignVHDL = new String[transitionList.length];
		//	transIntAssignVHDL = new String[transitionList.length][reqdVarsL.size()];
		//	transEnablingsVAMS = new String[transitionList.length];
		//	transConditionalsVAMS = new String[transitionList.length];
		//	transIntAssignVAMS = new String[transitionList.length][reqdVarsL.size()];
		//	transDelayAssignVAMS = new String[transitionList.length];
		//
			int transNum;
			for (String t : transitionList) {
				transNum = Integer.parseInt(t.split("t")[1]);
				if ((g.getPreset(t) != null) && (g.getPostset(t) != null)){
					if (!isTransientTransition(t)){
						if ((placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
							// g.getPreset(t).length != 0 && g.getPostset(t).length != 0 ??
							ArrayList<Integer> diffL = diff(getPlaceInfoIndex(g.getPreset(t)[0]), getPlaceInfoIndex(g.getPostset(t)[0]));
							String condStr = "";
						//	transEnablingsVHDL[transNum] = "";
						//	transEnablingsVAMS[transNum] = "";
							String[] binIncoming = getPlaceInfoIndex(g.getPreset(t)[0]).split(",");
							String[] binOutgoing = getPlaceInfoIndex(g.getPostset(t)[0]).split(",");
							Boolean firstInputBinChg = true;
							Boolean firstOutputBinChg = true;
							Boolean opChange = false, ipChange = false;
							for (int k : diffL) {
								if (!((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput()))) {
									// the above condition means that if the bin change is on a non-input dmv variable, there won't be any enabling condition
									ipChange = true;
									if (Integer.parseInt(binIncoming[k]) < Integer.parseInt(binOutgoing[k])) {
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binIncoming[k])).doubleValue();
										if (firstInputBinChg){
											condStr += "(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
									//		transEnablingsVHDL[transNum] += reqdVarsL.get(k).getName() + "'above(" + (int) Math.floor(val)+".0)";	
									//		transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
									//		transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") >= " + ((int)val)/valScaleFactor +"))";
										}
										else{
											condStr += "&(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
									//		transEnablingsVHDL[transNum] += " and " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.floor(val)+".0)";	
									//		transEnablingsVAMS[transNum] += " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
									//		transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") >= " + ((int)val)/valScaleFactor +"))";
										}
									} else {
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binOutgoing[k])).doubleValue();
										if (firstInputBinChg){
											condStr += "~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.ceil(val) + ")";
									//		transEnablingsVHDL[transNum] += "not " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.ceil(val)+".0)";
									//		transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
									//		transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") < " + ((int)val)/valScaleFactor +"))";
										}
										else{
											condStr += "&~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.ceil(val) + ")";
										//	transEnablingsVHDL[transNum] += "and not " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.ceil(val)+".0)";
									//		transEnablingsVAMS[transNum] += " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
									//		transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") < " + ((int)val)/valScaleFactor +"))";
										}
									}
									//if (diffL.get(diffL.size() - 1) != k) {
									//	condStr += "&";
									//	transEnablingsVHDL[transNum] += " and ";
										////COME BACK???	temporary			transEnablingsVAMS[transNum] += 
									//}
									firstInputBinChg = false;
								}
								// Enablings Till above.. Below one is dmvc delay,assignment. Whenever a transition's preset and postset places differ in dmvc vars, then this transition gets the assignment of the dmvc value in the postset place and delay assignment of the preset place's duration range. This has to be changed after taking the causal relation input
								if ((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput())) { // require few more changes here.should check for those variables that are constant over these regions and make them as causal????? thesis
									opChange = true;
									String pPrev = g.getPreset(t)[0];
									String nextPlace = g.getPostset(t)[0];
									//if (!isTransientTransition(t)){
							//		int mind = (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMin")));
							//		int maxd = (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMax")));
									int mind = (int) Math.floor(Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMin")));
									int maxd = (int) Math.ceil(Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMax")));
									if (mind != maxd)
										g.changeDelay(t, "uniform(" + mind + "," + maxd + ")");
									else
										g.changeDelay(t, String.valueOf(mind));
									int minv = (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")));
									int maxv = (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))); 
									if (minv != maxv)
										g.addIntAssign(t,reqdVarsL.get(k).getName(),"uniform(" + minv  + ","+ maxv + ")");
									else
										g.addIntAssign(t,reqdVarsL.get(k).getName(), String.valueOf(minv));
									int dmvTnum =  Integer.parseInt(t.split("t")[1]);
								//	transIntAssignVHDL[dmvTnum][k] = reqdVarsL.get(k).getName() +" => span(" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ".0,"+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + ".0)";
								//	transDelayAssignVHDL[dmvTnum] = "delay(" + mind + "," + maxd + ")";
									if (!vamsRandom){
								//		transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = "+ ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*valScaleFactor)+";\n";
										
								//		transDelayAssignVAMS[dmvTnum] = "#" + (int)((mind + maxd)/(2*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
									}
									else{
								//		transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = $dist_uniform(seed,"+ String.valueOf((int)Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))/valScaleFactor)) + "," + String.valueOf((int)Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))/valScaleFactor))+");\n";
										
								//		transDelayAssignVAMS[dmvTnum] = "del = $dist_uniform(seed," + (int) (mind/delayScaleFactor) + "," +(int) (maxd/delayScaleFactor) + ");\n\t\t\t#del";	// converting seconds to ns using math.pow(10,9)
									}
									//}
									//else{
									//	g.changeDelay(t, "[" + (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))) + "]");
									//	g.addIntAssign(t,reqdVarsL.get(k).getName(),"[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ","+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + "]");
									//	int dmvTnum =  Integer.parseInt(t.split("t")[1]);
									//	transIntAssignVHDL[dmvTnum][k] = reqdVarsL.get(k).getName() +" => span(" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ".0,"+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + ".0)";
									//	transDelayAssignVHDL[dmvTnum] = "delay(" + (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))) + ")";
									//	transIntAssignVAMS[dmvTnum][k] = ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*valScaleFactor);
									//	transDelayAssignVAMS[dmvTnum] =  (int)(((Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))))*Math.pow(10, 12))/(2.0*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
									//}
								}
							}
							if (ipChange & opChange){ // Both ip and op changes on same transition. Then delay should be 0. Not the previous bin duration.
								if ((getPlaceInfoIndex(g.getPreset(t)[0]) != null) && (getPlaceInfoIndex(g.getPostset(t)[0]) != null)) 
									if (!(transitionInfo.get(getPlaceInfoIndex(g.getPreset(t)[0]) + getPlaceInfoIndex(g.getPostset(t)[0])).containsKey("ioChangeDelay")))
										g.changeDelay(t, "0");
							}
							if (diffL.size() > 1){
						//		transEnablingsVHDL[transNum] = "(" + transEnablingsVHDL[transNum] + ")";
							}
						//	if ((transEnablingsVAMS[transNum] != "") && (transEnablingsVAMS[transNum] != null)){ 
						//		transEnablingsVAMS[transNum] += ")";
						//	}
							if (!condStr.equalsIgnoreCase("")){
								//condStr += enFailAnd;
								g.addEnabling(t, condStr);
							}
							else{
								//Nothing added to Enabling condition.
								//condStr = enFail;
								//g.addEnabling(t, condStr);
							}
							//transEnablingsVHDL[transNum] += enFailAnd;
						}
					}
					else{
						if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
							ArrayList<Integer> diffL = diff(getTransientNetPlaceIndex(g.getPreset(t)[0]), getPlaceInfoIndex(g.getPostset(t)[0]));
							String condStr = "";
						//	transEnablingsVHDL[transNum] = "";
						//	transEnablingsVAMS[transNum] = "";
							String[] binIncoming = getTransientNetPlaceIndex(g.getPreset(t)[0]).split(",");
							String[] binOutgoing = getPlaceInfoIndex(g.getPostset(t)[0]).split(",");
							Boolean firstInputBinChg = true;
							Boolean opChange = false, ipChange = false;
							for (int k : diffL) {
								if (!((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput()))) {
									ipChange = true;
									if (Integer.parseInt(binIncoming[k]) < Integer.parseInt(binOutgoing[k])) {
										//	double val = divisionsL.get(k).get(Integer.parseInt(binIncoming[k])).doubleValue();
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binIncoming[k])).doubleValue();
										if (firstInputBinChg){
											condStr += "(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
								//			transEnablingsVHDL[transNum] += reqdVarsL.get(k).getName() + "'above(" + (int) Math.floor(val)+".0)";	
								//			transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
										}
										else{
											condStr += "&(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
								//			transEnablingsVHDL[transNum] += " and " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.floor(val)+".0)";	
								//			transEnablingsVAMS[transNum] += " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
										}
									} else {
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binOutgoing[k])).doubleValue();
										if (firstInputBinChg){
											condStr += "~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.ceil(val) + ")";
									//		transEnablingsVHDL[transNum] += " and not " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.ceil(val)+".0)";
									//		transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
										}
										else{
											condStr += "&~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.ceil(val) + ")";
									//		transEnablingsVHDL[transNum] += "not " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.ceil(val)+".0)";
									//		transEnablingsVAMS[transNum] = " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
										}
									}
									//if (diffL.get(diffL.size() - 1) != k) {
									//	condStr += "&";
									//	transEnablingsVHDL[transNum] += " and ";
									//	//COME BACK???	temporary			transEnablingsVAMS[transNum] += 
									//}
									firstInputBinChg = false;
								}
								if ((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput())) { // require few more changes here.should check for those variables that are constant over these regions and make them as causal????? thesis
									opChange = true;
									String pPrev = g.getPreset(t)[0];
									String nextPlace = g.getPostset(t)[0];
									//int mind = (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin")));
									//int maxd = (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax")));
									int mind = (int) Math.floor(Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMin")));
									int maxd = (int) Math.floor(Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMax")));
									if (mind != maxd)
										g.changeDelay(t, "uniform(" + mind + "," + maxd + ")");
									else
										g.changeDelay(t, String.valueOf(mind));
									int minv = (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")));
									int maxv = (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax")));
									if (minv != maxv)
										g.addIntAssign(t,reqdVarsL.get(k).getName(),"uniform(" + minv + ","+ maxv + ")");
									else
										g.addIntAssign(t,reqdVarsL.get(k).getName(),String.valueOf(minv));
									int dmvTnum =  Integer.parseInt(t.split("t")[1]);
								//	transIntAssignVHDL[dmvTnum][k] = reqdVarsL.get(k).getName() +" => span(" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ".0,"+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + ".0)";
								//	transDelayAssignVHDL[dmvTnum] = "delay(" + mind + "," + maxd + ")";
									if (!vamsRandom){
								//		transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = "+ ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*valScaleFactor)+";\n";;
										//											transDelayAssignVAMS[dmvTnum] =  "#" + (int)(((Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))))*Math.pow(10, 12))/(2.0*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
								//		transDelayAssignVAMS[dmvTnum] =  "#" + (int)((mind + maxd)/(2*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
									}
									else{
								//		transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = $dist_uniform(seed,"+ String.valueOf(Math.floor((Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))/valScaleFactor))) + "," + String.valueOf(Math.ceil((Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))/valScaleFactor)))+");\n";
								
								//		transDelayAssignVAMS[dmvTnum] =  "del = $dist_uniform(seed," + (int) (mind/delayScaleFactor) + "," + (int) (maxd/delayScaleFactor) + ");\n\t\t\t#del";	// converting seconds to ns using math.pow(10,9)
									}
								}
							}
							if (ipChange & opChange){ // Both ip and op changes on same transition. Then delay should be 0. Not the previous bin duration.
								if ((getPlaceInfoIndex(g.getPreset(t)[0]) != null) && (getPlaceInfoIndex(g.getPostset(t)[0]) != null)) 
									if (!(transitionInfo.get(getPlaceInfoIndex(g.getPreset(t)[0]) + getPlaceInfoIndex(g.getPostset(t)[0])).containsKey("ioChangeDelay")))
										g.changeDelay(t, "0");
							}
							if (diffL.size() > 1){
						//		transEnablingsVHDL[transNum] = "(" + transEnablingsVHDL[transNum] + ")";
							}
						//	if ((transEnablingsVAMS[transNum] != "") && (transEnablingsVAMS[transNum] != null)){ 
						//		transEnablingsVAMS[transNum] += ")";
						//	}
							if (!condStr.equalsIgnoreCase("")){
								//condStr += enFailAnd;
								g.addEnabling(t, condStr);
							}
							else{
								//condStr = enFail;
								//g.addEnabling(t, condStr);
							}
							//transEnablingsVHDL[transNum] += enFailAnd;
							
						}
					}
				}   
				if ((g.getPreset(t) != null) && (!isTransientTransition(t)) && (placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("PROP"))){
					g.addEnabling(t, failProp);
					//g.addBoolAssign(t, "fail", "true"); // fail would be the variable name
					//g.addProperty(failProp);
				}
			}
			ArrayList<String> placesWithoutPostsetTrans = new ArrayList<String>();
			for (String st1 : g.getPlaceList()) {
				if (g.getPostset(st1).length == 0){ // a place without a postset transition
					placesWithoutPostsetTrans.add(st1);
				} else if (g.getPostset(st1).length > 1){
					HashMap<String,Boolean> varsInEnabling = new HashMap<String,Boolean>();
					for (String st2 : g.getPostset(st1)){
						if (g.getEnablingTree(st2) != null){
							for (String st3 : g.getEnablingTree(st2).getVars()){
								varsInEnabling.put(st3, true);
							}
						}
					}
					for (String st2 : g.getPostset(st1)){
						if (varsInEnabling.keySet().size() >= 1){ // && (g.getEnablingTree(st2) != null))
							String[] binOutgoing = getPlaceInfoIndex(g.getPostset(st2)[0]).split(",");
							String condStr = "";
							for (String st : varsInEnabling.keySet()){
								int bin = Integer.valueOf(binOutgoing[findReqdVarslIndex(st)]);
								if (bin == 0){
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "~(" + st + ">=" + (int) Math.ceil(scaledThresholds.get(st).get(bin).doubleValue()) + ")";
								} else if (bin == (scaledThresholds.get(st).size())){
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "(" + st + ">="	+ (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")";
								} else{
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")&~(" + st + ">=" + (int) Math.ceil(scaledThresholds.get(st).get(bin).doubleValue()) + ")";
								}
							}
							out.write("Changed enabling of " + st2 + " to " + condStr + "\n");
							g.addEnabling(st2, condStr);
						}
					}
					//for (String st2 : g.getPostset(st1)){
					//	ExprTree enableTree = g.getEnablingTree(st2);
					//	if (enableTree != null){	// If enabling of a transition is null then it's obviously not mutually exclusive of any other parallel transitions from that place
					//		for (String st3 : varsInEnabling.keySet()){
					//			// TODO: CHECK THE BIN CHANGES HERE AND ADD ENABLING CONDITIONS
					//			if (!enableTree.containsVar(st3)){
					//			//	System.out.println("At place " + st1 + " for transition " + st2 + ",Get threshold of " + st3);
					//				visitedPlaces = new HashMap<String,Boolean>();
					//				String completeEn =traceBack(st1,st3);
					//				System.out.println("At place " + st1 + " for transition " + st2 + ",Get threshold of " + st3+ " from " + completeEn);
					//				Pattern enPatternParan = Pattern.compile(".*?(~?\\(" + st3+ ".*?\\)*)[a-zA-Z]*.*");
					//				Pattern enPattern;
					//				Matcher enMatcher;
					//				//Pattern enPatternNoParan = Pattern.compile(".*?(~?\\(?" + st3+ ".*?\\)*)[a-zA-Z]*.*"); 
					//				Matcher enMatcherParan = enPatternParan.matcher(completeEn);
					//				if (enMatcherParan.find()) {
					//					enPattern = Pattern.compile(".*?(~?\\(" + st3+ ".*?\\)).*?");
					//					System.out.println("Matching for pattern " + enPattern.toString());
					//					enMatcher = enPattern.matcher(completeEn);
					//					String enCond = enMatcher.group(1);
					//					System.out.println("Extracted " +enCond);
					//				} else {
					//					enPattern = Pattern.compile(".*?(" + st3+ ".*?)[a-zA-Z]*.*?");
					//					System.out.println("Matching for pattern " + enPattern.toString());
					//					enMatcher = enPattern.matcher(completeEn);
					//					String enCond = enMatcher.group(1);
					//					System.out.println("Extracted " +enCond);
					//				}
					//			}
					//		}
					//	}
					//}
				}
				if (!isTransientPlace(st1)){
					String p = getPlaceInfoIndex(st1);
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						if (g.getPreset(st1).length != 0){
							for (String t : g.getPreset(st1)) {
								for (int k = 0; k < reqdVarsL.size(); k++) {
									if (!reqdVarsL.get(k).isDmvc()) {
										int minr = getMinRate(p, reqdVarsL.get(k).getName());
										int maxr = getMaxRate(p, reqdVarsL.get(k).getName());
										if (minr != maxr)
											g.addRateAssign(t, reqdVarsL.get(k).getName(), "uniform("	+ minr	+ "," + maxr + ")");
										else
											g.addRateAssign(t, reqdVarsL.get(k).getName(), String.valueOf(minr));
									}
								}
							}
						}
					}
				}
			}
			for (String st1 : transientNetTransitions.keySet()){
				String s = g.getPostset("t" + transientNetTransitions.get(st1).getProperty("transitionNum"))[0];
				// check TYPE of preset ????
				String p = getPlaceInfoIndex(s);
				if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
					for (int k = 0; k < reqdVarsL.size(); k++) {
						if (!reqdVarsL.get(k).isDmvc()) {
							//	out.write("<" + t	+ "=["	+ reqdVarsL.get(k).getName() + ":=["	+ getMinRate(p, reqdVarsL.get(k).getName())	+ "," + getMaxRate(p, reqdVarsL.get(k).getName()) + "]]>");
							int minr = getMinRate(p, reqdVarsL.get(k).getName());
							int maxr = getMaxRate(p, reqdVarsL.get(k).getName());
							if (minr != maxr)
								g.addRateAssign("t" + transientNetTransitions.get(st1).getProperty("transitionNum"), reqdVarsL.get(k).getName(), "uniform("	+ minr	+ "," + maxr + ")");
							else
								g.addRateAssign("t" + transientNetTransitions.get(st1).getProperty("transitionNum"), reqdVarsL.get(k).getName(), String.valueOf(minr));
						}
					}
				}
			}
			for (String st1 : placesWithoutPostsetTrans){
				placeInfo.remove(getPlaceInfoIndex(st1));
				g.removePlace(st1);
			}
			addInitPlace(scaledThresholds);
			out.write("learning main process done. Saving stuff and learning other modules.\n");
			out.close();
	//		addMetaBins();
	//		addMetaBinTransitions();
			g.save(directory + separator + lhpnFile);
			new Lpn2verilog(directory + separator + lhpnFile); //writeSVFile(directory + separator + lhpnFile);
			if (new File(learnFile).exists()){ //directory + separator + "complete.lpn").exists()){//
				LhpnFile l1 = new LhpnFile();
				l1.load(learnFile);
				g = mergeLhpns(l1,g);
				//l1.load(directory + separator + "complete.lpn");
				//mergeLhpns(l1,g).save(directory + separator + "complete.lpn");
			} //else {
			//	g.save(directory + separator + "complete.lpn");
			//}
//			addPseudo(scaledThresholds);
			g.save(directory + separator + lhpnFile);
//			lpnWithPseudo.save(directory + separator + "pseudo" + lhpnFile);
//			new Lpn2verilog(directory + separator + lhpnFile); //writeSVFile(directory + separator + lhpnFile);
			//writeVHDLAMSFile(lhpnFile.replace(".lpn",".vhd"));
			//writeVerilogAMSFile(lhpnFile.replace(".lpn",".vams"));
			if (defaultStim){
				int j = 0;
				HashMap<String,Double> tPar = new HashMap<String,Double>(); 
				tPar.put("epsilon", epsilon);
				tPar.put("pathLengthBin", Double.valueOf((double) pathLengthBin));
				tPar.put("rateSampling", Double.valueOf((double) rateSampling));
				tPar.put("percent", percent);
				if (absoluteTime)
					tPar.put("runTime", runTime);
				else
					tPar.put("runLength", Double.valueOf((double) runLength));
				for (Variable v : reqdVarsL){
					if (v.isInput()){
						j++;
						ArrayList <Variable> varsT = new ArrayList <Variable>();
						Variable input = new Variable("");
						input.copy(v);
						input.setInput(false);
						input.setOutput(true);
						varsT.add(input);
						LearnLPN l = new LearnLPN();
						LhpnFile moduleLPN = l.learnLPN(directory, log, biosim, j, thresholds, tPar, varsT ,varNames , vsFactor, dsFactor);
						// new Lpn2verilog(directory + separator + lhpnFile); //writeSVFile(directory + separator + lhpnFile);
						g = mergeLhpns(moduleLPN,g);
					}	
				}
				g.save(directory + separator + lhpnFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"LPN file couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch (NullPointerException e4) {
			e4.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"LPN file couldn't be created/written. Null exception",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch (ArrayIndexOutOfBoundsException e1) {	// comes from initMark = -1 of updateGraph()
			e1.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to calculate rates.\nWindow size or pathLengthBin must be reduced.\nLearning unsuccessful.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
			try {
				out.write("ERROR! Unable to calculate rates.\nIf Window size = -1, pathLengthBin must be reduced;\nElse, reduce windowsize\nLearning unsuccessful.");
				out.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			running.setCursor(null);
			running.dispose();
		}
		catch (java.lang.IllegalStateException e3){
			e3.printStackTrace();
			//System.out.println("LPN file couldn't be created/written ");
			JOptionPane.showMessageDialog(biosim.frame(),
					"LPN File not found for merging.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
	*/
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
		// dmvcCnt = 0;
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
		//Properties initPlace = new Properties();
		//placeInfo.put("initMarked", p0);
		//initPlace.setProperty("placeNum", numPlaces.toString());
		//initPlace.setProperty("type", "INIT");
		//initPlace.setProperty("initiallyMarked", "true");
		int initPlaceNum = numPlaces;
		g.addPlace("p" + numPlaces, true);
		//propPlaces.add("p" + numPlaces);
		numPlaces++;
		try{
			for (String st : transientNetPlaces.keySet()){
				//Properties p1 = new Properties();
				//p1.setProperty("transitionNum", numTransitions.toString());
				//initTransitions.put(key, p1); //from initPlaceNum to key
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
			JOptionPane.showMessageDialog(biosim.frame(),
					"Log file couldn't be opened in addInitPlace.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		} catch (NullPointerException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Null exception in addInitPlace.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void genBinsRates(HashMap<String, ArrayList<Double>> localThresholds) { 
//			TSDParser tsd = new TSDParser(directory + separator + datFile, biosim,false);
			// genBins data = tsd.getData();
		try{
			reqdVarIndices = new ArrayList<Integer>();
			bins = new int[reqdVarsL.size()][data.get(0).size()];
			for (int i = 0; i < reqdVarsL.size(); i++) {
				// System.out.println("Divisions " + divisionsL.get(i));
				for (int j = 1; j < varNames.size(); j++) {
					String currentVar = reqdVarsL.get(i).getName();
					if (currentVar.equalsIgnoreCase(varNames.get(j))) {
						// System.out.println(reqdVarsL.get(i) + " matched "+
						// varNames.get(j) + " i = " + i + " j = " + j);
						reqdVarIndices.add(j);
						for (int k = 0; k < data.get(j).size(); k++) {
							// System.out.print(data.get(j).get(k) + " ");
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
						// System.out.print(" ");
					}
				}
			}
			/*
			 * System.out.println("array bins is :"); for (int i = 0; i <
			 * reqdVarsL.size(); i++) { System.out.print(reqdVarsL.get(i).getName() + "
			 * "); for (int k = 0; k < data.get(0).size(); k++) {
			 * System.out.print(bins[i][k] + " "); } System.out.print("\n"); }
			 */
			// genRates
			rates = new Double[reqdVarsL.size()][data.get(0).size()];
			values = new double[reqdVarsL.size()][data.get(0).size()];
			duration = new Double[data.get(0).size()];
			int mark, k, previous = 0; // indices of rates not same as that of the variable. if
			// wanted, then size of rates array should be varNames
			// not reqdVars
			if (rateSampling == -1) { // replacing inf with -1 since int
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
			}
		} catch (NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Bins/Rates could not be generated. Please check thresholds.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);				
		} catch (IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Log file couldn't be opened for writing genBinsRates messages.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		
			/*try {
				for (int i = 0; i < (data.get(0).size()); i++) {
					for (int j = 0; j < reqdVarsL.size(); j++) {
						k = reqdVarIndices.get(j);
						out.write(data.get(k).get(i) + " ");// + bins[j][i] + " " +
						// rates[j][i] + " ");
					}
					for (int j = 0; j < reqdVarsL.size(); j++) {
						out.write(bins[j][i] + " ");
					}
					for (int j = 0; j < reqdVarsL.size(); j++) {
						out.write(rates[j][i] + " ");
					}
					out.write(duration[i] + " ");
					out.write("\n");
				}
			} catch (IOException e) {
				System.out
						.println("Log file couldn't be opened for writing rates and bins ");
			}*/
	}

	public int getRegion(double value, ArrayList<Double> varThresholds){
		int bin = 0;
		for (int l = 0; l < varThresholds.size(); l++) {
			if (value <= varThresholds.get(l)) {
				bin = l;
				break;
			} else {
				bin = l + 1; 
			}
		}
		return bin;
	}
	
	public boolean compareBins(int j, int mark) {
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (bins[i][j] != bins[i][mark]) {
				return false;
			} else {
				continue;
			}
		}
		return true;
	}
	
	public HashMap<String, ArrayList<Double>> detectDMV(ArrayList<ArrayList<Double>> data, Boolean callFromAutogen) {
		int startPoint, endPoint, mark, numPoints;
		HashMap<String, ArrayList<Double>> dmvDivisions = new HashMap<String, ArrayList<Double>>();
		double absTime;
		//dmvcValuesUnique = new HashMap<String, ArrayList<Double>>();
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
							if (j < mark) // not reqd??
								continue;
							if (((j+1) < data.get(reqdVarIndices.get(i)).size()) && 
									Math.abs(data.get(reqdVarIndices.get(i)).get(j) - data.get(reqdVarIndices.get(i)).get(j + 1)) <= reqdVarsL.get(i).getEpsilon()) {
								startPoint = j;
								runs.addValue(data.get(reqdVarIndices.get(i)).get(j)); // chk carefully reqdVarIndices.get(i)
								while (((j + 1) < data.get(0).size()) && (bins[i][startPoint] == bins[i][j+1]) && (Math.abs(data.get(reqdVarIndices.get(i)).get(startPoint) - data.get(reqdVarIndices.get(i)).get(j + 1)) <= reqdVarsL.get(i).getEpsilon())) {       //checking of same bins[] condition added on May 11,2010.
									runs.addValue(data.get(reqdVarIndices.get(i)).get(j + 1)); // chk carefully
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
							//System.out.println("Sorted DMV values of " + reqdVarsL.get(i).getName() + " are ");
							//for (Double l : dmvcValues){
							//	System.out.print(l + " ");
							//}
							//System.out.println();
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
								}
								else if (dmvcValues[j] >= thresholds.get(reqdVarsL.get(i).getName()).get(thresholds.get(reqdVarsL.get(i).getName()).size() - 1)){
									dmvcValuesUnique.get(reqdVarsL.get(i).getName()).put(String.valueOf(thresholds.get(reqdVarsL.get(i).getName()).size()),dmvcValues[j].toString());
									//System.out.println("For variable " + reqdVarsL.get(i).getName() + " value for bin " + thresholds.get(reqdVarsL.get(i).getName()).size() + " is " + dmvcValues[j] + "\n");
								}
								else{
									for (int k = 0; k < thresholds.get(reqdVarsL.get(i).getName()).size()-1; k++){
										if ((dmvcValues[j] >= thresholds.get(reqdVarsL.get(i).getName()).get(k)) && (dmvcValues[j] < thresholds.get(reqdVarsL.get(i).getName()).get(k+1))){
											dmvcValuesUnique.get(reqdVarsL.get(i).getName()).put(String.valueOf(k+1),dmvcValues[j].toString());
											//System.out.println("For variable " + reqdVarsL.get(i).getName() + " value for bin " + String.valueOf(k+1) + " is " + dmvcValues[j] + "\n");
											break;
										}
									}
								}
								out.write(dmvcValues[j] + " ");
								//following not needed for calls from data2lhpn
								//	dmvSplits.add((dmvcValuesUnique.get(reqdVarsL.get(i).getName()).get(dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() - 1) + dmvcValuesUnique.get(reqdVarsL.get(i).getName()).get(dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() - 2))/2);
								//}
								//for (int k = j+1; k < dmvcValues.length; k++){
								//	if (Math.abs((dmvcValues[j] - dmvcValues[k])) > epsilon){
								//		j = k-1;
								//		break;
								//	}
								//	else if (k >= (dmvcValues.length -1)){
								//		j = k;
								//	}
								//}	
							}
							//dmvDivisions.put(reqdVarsL.get(i).getName(), dmvSplits);
							out.write(reqdVarsL.get(i).getName() + " is  a dmvc \n");
						}
					} else {
					/*	epsilon = Double.parseDouble(epsilonG.getText().trim()); // because these are not extracted b4 data2lhpn()
						percent = Double.parseDouble(percentG.getText().trim());
						runLength = Integer.parseInt(runLengthG.getText().trim());
						runTime = Double.parseDouble(runTimeG.getText().trim());
						absoluteTime = absTimeG.isSelected();
					*/
						getThreshPar(); // Changed from above. CHECK
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
								Double[] dVals = reqdVarsL.get(i).getRuns().getAvgVals();
								dmvcValues = new Double[dVals.length + 1];
								for (int j = 0; j < dVals.length; j++){
									dmvcValues[j] = dVals[j];
								}
								dmvcValues[dVals.length] = lastRunValueWithoutTransition;
							} else
								dmvcValues = reqdVarsL.get(i).getRuns().getAvgVals();
							Arrays.sort(dmvcValues);
							//System.out.println("Sorted DMV values of " + reqdVarsL.get(i).getName() + " are ");
							//for (Double l : dmvcValues){
							//	System.out.print(l + " ");
							//}
							if (!dmvcValuesUnique.containsKey(reqdVarsL.get(i).getName()))
								dmvcValuesUnique.put(reqdVarsL.get(i).getName(),new Properties());
							ArrayList<Double> dmvSplits = new ArrayList<Double>();
							out.write("Final DMV values of " + reqdVarsL.get(i).getName() + " are ");
							int l = 0;
							for (int j = 0; j < dmvcValues.length; j++){
								dmvcValuesUnique.get(reqdVarsL.get(i).getName()).put(String.valueOf(l),dmvcValues[j].toString());
								out.write(dmvcValues[j] + ", ");
								if (dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() > 1){
									Properties p3 = dmvcValuesUnique.get(reqdVarsL.get(i).getName());
									double d1 = (Double.valueOf(p3.getProperty(String.valueOf(p3.size() - 1))) + Double.valueOf(p3.getProperty(String.valueOf(p3.size() - 2))))/2.0;
									d1 = d1*10000;
									int d2 = (int) d1; 
									//System.out.println(d2);
									//System.out.println(((double)d2)/10000.0);
									dmvSplits.add(((double)d2)/10000.0); // truncating to 4 decimal places
									//dmvSplits.add((dmvcValuesUnique.get(reqdVarsL.get(i).getName()).get(dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() - 1) + dmvcValuesUnique.get(reqdVarsL.get(i).getName()).get(dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() - 2))/2);
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
			JOptionPane.showMessageDialog(biosim.frame(),
					"Log file couldn't be opened for writing rates and bins.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		if (callFromAutogen)
			checkthresholds(data,dmvDivisions);
		dmvDetectDone = true;
		return(dmvDivisions);
	}

	public void checkthresholds(ArrayList<ArrayList<Double>> data, HashMap<String, ArrayList<Double>> localThresholds){
		//	ArrayList<Integer> reqdVarIndices = new ArrayList<Integer>();
		int[][] bins = new int[reqdVarsL.size()][data.get(0).size()];
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (reqdVarsL.get(i).isDmvc()){
				String currentVar = reqdVarsL.get(i).getName();
				for (int k = 0; k < data.get(i+1).size(); k++) {
					for (int l = 0; l < localThresholds.get(currentVar).size(); l++) {
						if (data.get(i+1).get(k) <= localThresholds.get(currentVar).get(l)) {
							bins[i][k] = l;
							break;
						} else {
							bins[i][k] = l + 1; // indices of bins not same as that of the variable. i here. not j; if j
							// wanted, then size of bins array should be varNames not reqdVars
						}
					}
				}
			}
		}
		int startPoint, endPoint, mark, numPoints;
		double absTime;
		//dmvcValuesUnique = new HashMap<String, ArrayList<Double>>();
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
			JOptionPane.showMessageDialog(biosim.frame(),
					"Log file couldn't be opened for writing rates and bins.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public double calcDelay(int i, int j) {
		return (data.get(0).get(j) - data.get(0).get(i));
		// should add some next run logic later..?
	}
	
	public double calcDelayWithData(int i, int j, ArrayList<ArrayList<Double>> data) {
		return (data.get(0).get(j) - data.get(0).get(i));
	}

	public void addValue(Properties p, String name, Double v) { 
		Double vMin;
		Double vMax;
		if ((p.getProperty(name + "_vMin") == null)
				&& (p.getProperty(name + "_vMax") == null)) {
			p.setProperty(name + "_vMin", v.toString());
			p.setProperty(name + "_vMax", v.toString());
			return;
		} else {
			vMin = Double.parseDouble(p.getProperty(name + "_vMin"));
			vMax = Double.parseDouble(p.getProperty(name + "_vMax"));
			if (v < vMin) {
				vMin = v;
			} else if (v > vMax) {
				vMax = v;
			}
		}
		p.setProperty(name + "_vMin", vMin.toString());
		p.setProperty(name + "_vMax", vMax.toString());
	}

	public void addRate(Properties p, String name, Double r) { 
		Double rMin;
		Double rMax;
		if ((p.getProperty(name + "_rMin") == null)
				&& (p.getProperty(name + "_rMax") == null)) {
			p.setProperty(name + "_rMin", r.toString());
			p.setProperty(name + "_rMax", r.toString());
			return;
		} else {
			rMin = Double.parseDouble(p.getProperty(name + "_rMin"));
			rMax = Double.parseDouble(p.getProperty(name + "_rMax"));
			if (r < rMin) {
				rMin = r;
			} else if (r > rMax) {
				rMax = r;
			}
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
					/* Related to the separate net for DMV input driver
					if (p.getProperty("type").equals("DMVC")) {
						p.setProperty("DMVCValue", Double.toString(Double.parseDouble(p.getProperty("DMVCValue"))* scaleFactor));
					} else {
					 */
					for (Variable v : reqdVarsL) {
						if (!v.isDmvc()) {
							// p.setProperty(v.getName() +
							// "_rMin",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMin"))/delayScaleFactor)));
							// p.setProperty(v.getName() +
							// "_rMax",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMax"))/delayScaleFactor)));
							p.setProperty(v.getName() + "_rMin", Double
									.toString(Double.parseDouble(p.getProperty(v.getName()
											+ "_rMin"))* scaleFactor));
							p.setProperty(v.getName() + "_rMax", Double
									.toString(Double.parseDouble(p.getProperty(v.getName()
											+ "_rMax"))* scaleFactor));
						} else {
							// p.setProperty(v.getName() +
							// "_rMin",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMin"))/delayScaleFactor)));
							// p.setProperty(v.getName() +
							// "_rMax",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMax"))/delayScaleFactor)));
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
					//}
				}
			}
			/*int i = 0;
			  for (Variable v : reqdVarsL) {
				v.scaleInitByVar(scaleFactor);
				for (int j = 0; j < divisions.get(i).size(); j++) {
					divisions.get(i).set(j,divisions.get(i).get(j) * scaleFactor);
				}
				i++;
			}*/
			//commented above for replacing divisionsL with thresholds
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
			JOptionPane.showMessageDialog(biosim.frame(),
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
					/* Related to the separate net for DMV input driver
					if (p.getProperty("type").equals("DMVC")) {
						String[] times = null;
						String name = p.getProperty("DMVCVariable");
						String s = p.getProperty("dmvcTime_" + name);
						String newS = null;
						if (s != null) {
							times = s.split(" ");
							for (int i = 0; i < times.length; i++) {
								if (newS == null) {
									// newS = Integer.toString((int)(Double.parseDouble(times[i])*scaleFactor));
									newS = Double.toString(Double.parseDouble(times[i])
					 * scaleFactor);
								} else {
									// newS = newS + Integer.toString((int)(Double.parseDouble(times[i])*scaleFactor));
									newS = newS + " " + Double.toString(Double
											.parseDouble(times[i]) * scaleFactor);
								}
							}
							p.setProperty("dmvcTime_" + name, newS);
						}
						p.setProperty("dMin", Double.toString(Double.parseDouble(p
								.getProperty("dMin")) * scaleFactor));
						p.setProperty("dMax", Double.toString(Double.parseDouble(p
								.getProperty("dMax")) * scaleFactor));
					} else{*/
					// p.setProperty("dMin",Integer.toString((int)(Double.parseDouble(p.getProperty("dMin"))*scaleFactor)));
					// p.setProperty("dMax",Integer.toString((int)(Double.parseDouble(p.getProperty("dMax"))*scaleFactor)));
				/* STORING DELAYS AT TRANSITIONS
				  	p.setProperty("dMin", Double.toString(Double.parseDouble(p
							.getProperty("dMin")) * scaleFactor));
					p.setProperty("dMax", Double.toString(Double.parseDouble(p
							.getProperty("dMax")) * scaleFactor));
				*/			
					for (Variable v : reqdVarsL) {
						if (!v.isDmvc()) {
							// p.setProperty(v.getName() +
							// "_rMin",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMin"))/scaleFactor)));
							// p.setProperty(v.getName() +
							// "_rMax",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMax"))/scaleFactor)));
							p.setProperty(v.getName() + "_rMin", Double
									.toString(Double.parseDouble(p.getProperty(v
											.getName() + "_rMin"))	/ scaleFactor));
							p.setProperty(v.getName() + "_rMax", Double
									.toString(Double.parseDouble(p.getProperty(v
											.getName() + "_rMax"))	/ scaleFactor));
						}
					}
					//}
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

	public Double getMinDiv(HashMap<String, ArrayList<Double>> divisions) {
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

	public Double getMaxDiv(HashMap<String, ArrayList<Double>> divisions) {
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
						if ((minRate == null)
								&& (p.getProperty(v.getName() + "_rMin") != null)) {
							minRate = Double.parseDouble(p.getProperty(v.getName()
									+ "_rMin"));
						} else if ((p.getProperty(v.getName() + "_rMin") != null)
								&& (Double.parseDouble(p.getProperty(v.getName()
										+ "_rMin")) < minRate)
										&& (Double.parseDouble(p.getProperty(v.getName()
												+ "_rMin")) != 0.0)) {
							minRate = Double.parseDouble(p.getProperty(v.getName()
									+ "_rMin"));
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
						if ((maxRate == null)
								&& (p.getProperty(v.getName() + "_rMax") != null)) {
							maxRate = Double.parseDouble(p.getProperty(v.getName()
									+ "_rMax"));
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
						out.write("Transition " + g.getTransition(t).getName() + " b/w " + pPrev + " and " + nextPlace + " : finding delay \n");
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
		/*for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if ((minDelay == null) && (p.getProperty("dMin") != null)
					&& (Double.parseDouble(p.getProperty("dMin")) != 0)) {
				minDelay = Double.parseDouble(p.getProperty("dMin"));
			} else if ((p.getProperty("dMin") != null)
					&& (Double.parseDouble(p.getProperty("dMin")) != 0)
					&& (Double.parseDouble(p.getProperty("dMin")) < minDelay)) {
				minDelay = Double.parseDouble(p.getProperty("dMin"));
			}
			//}
		}*/
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
		/*for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if ((maxDelay == null) && (p.getProperty("dMax") != null)
					&& (Double.parseDouble(p.getProperty("dMax")) != 0)) {
				maxDelay = Double.parseDouble(p.getProperty("dMax"));
			} else if ((p.getProperty("dMax") != null)
					&& (Double.parseDouble(p.getProperty("dMax")) != 0)
					&& (Double.parseDouble(p.getProperty("dMax")) > maxDelay)) {
				maxDelay = Double.parseDouble(p.getProperty("dMax"));
			}
			//}
		}*/
		return maxDelay;
	}

	public void addDuration(Properties p, Double d) {
		Double dMin;
		Double dMax;
		// d = d*(10^6);
		if ((p.getProperty("dMin") == null) && (p.getProperty("dMax") == null)) {
			// p.setProperty("dMin", Integer.toString((int)(Math.floor(d))));
			// p.setProperty("dMax", Integer.toString((int)(Math.floor(d))));
			p.setProperty("dMin", d.toString());
			p.setProperty("dMax", d.toString());
			return;
		} else {
			dMin = Double.parseDouble(p.getProperty("dMin"));
			dMax = Double.parseDouble(p.getProperty("dMax"));
			if (d < dMin) {
				dMin = d;
			} else if (d > dMax) {
				dMax = d;
			}
		}
		p.setProperty("dMin", dMin.toString());
		p.setProperty("dMax", dMax.toString());
		// p.setProperty("dMin", Integer.toString((int)(Math.floor(dMin))));
		// p.setProperty("dMax", Integer.toString((int)(Math.ceil(dMax))));
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
	
	public ArrayList<Integer> diff(String pre_bin, String post_bin) {
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
	
/*	public Double[][] getDataExtrema(ArrayList<ArrayList<Double>> data){
		Double[][] extrema = new Double[reqdVarsL.size()][2];
		for (int i=0; i<reqdVarsL.size(); i++){
			//Object obj = Collections.min(data.get(reqdVarIndices.get(i)));
			Object obj = Collections.min(data.get(i+1));
			extrema[i][0] = Double.parseDouble(obj.toString());
			//obj = Collections.max(data.get(reqdVarIndices.get(i)));
			obj = Collections.max(data.get(i+1));
			extrema[i][1] = Double.parseDouble(obj.toString());
		}
	    return extrema;
	}
	*/
	public HashMap <String, Double[]> getDataExtrema(ArrayList<ArrayList<Double>> data){
		HashMap <String, Double[]> extrema = new HashMap <String, Double[]>();
		for (int i=0; i<reqdVarsL.size(); i++){
			//Object obj = Collections.min(data.get(reqdVarIndices.get(i)));
			Object obj = Collections.min(data.get(i+1));
			extrema.put(reqdVarsL.get(i).getName(),new Double[2]);
			extrema.get(reqdVarsL.get(i).getName())[0] = Double.parseDouble(obj.toString());
			//obj = Collections.max(data.get(reqdVarIndices.get(i)));
			obj = Collections.max(data.get(i+1));
			extrema.get(reqdVarsL.get(i).getName())[1] = Double.parseDouble(obj.toString());
		}
	    return extrema;
	}
	
	//public ArrayList<ArrayList<Double>> initDivisions(Double[][] extrema){
	public HashMap<String, ArrayList<Double>> initDivisions(HashMap<String,Double[]> extrema){
		// this method won't be called in auto case.. so dont worry?
		int numThresholds = Integer.parseInt(numBins.getSelectedItem().toString()) - 1;
		double interval;
		//ArrayList<ArrayList<Double>> divisions = new ArrayList<ArrayList<Double>>(); //changed for replacing divisionsL by threholds
		HashMap<String, ArrayList<Double>> localThresholds = new HashMap<String, ArrayList<Double>> ();
		for (int i = 0; i < reqdVarsL.size(); i++){
			//divisions.add(new ArrayList<Double>());//changed for replacing divisionsL by threholds
			localThresholds.put(reqdVarsL.get(i).getName(),new ArrayList<Double>());
			if (!suggestIsSource){ // could use user.isselected instead of this.
				//numThresholds = Integer.parseInt(numBins.getSelectedItem().toString()) - 1;
			}
			else{
				for (int j = 1; j < variablesPanel.getComponentCount(); j++){
					if ((((JTextField)((JPanel)variablesPanel.getComponent(j)).getComponent(0)).getText().trim()).equalsIgnoreCase(reqdVarsL.get(i).getName())){
						numThresholds = Integer.parseInt((String)((JComboBox)((JPanel)variablesPanel.getComponent(j)).getComponent(6)).getSelectedItem())-1; // changed 2 to 3 after required
						break;
					}
				}
			}
			if (numThresholds != -1){
				interval = (Math.abs(extrema.get(reqdVarsL.get(i).getName())[1] - extrema.get(reqdVarsL.get(i).getName())[0]))/(numThresholds + 1);
				for (int j = 0; j< numThresholds; j++){
					//if ((divisions.get(i).size() == 0) || (divisions.get(i).get(j) == null)){
					//	divisions.get(i).set(j,extrema[i][0] + interval*j);
					//}
					/*if (divisions.get(i).size() <= j){
					divisions.get(i).add(extrema[i][0] + interval*(j+1));  // j+1
				}
				else{
					divisions.get(i).set(j,extrema[i][0] + interval*(j+1)); // j+1
				}*///changed for replacing divisionsL by threholds*/
					if (localThresholds.get(reqdVarsL.get(i).getName()).size() <= j){
						localThresholds.get(reqdVarsL.get(i).getName()).add(extrema.get(reqdVarsL.get(i).getName())[0] + interval*(j+1));  // j+1
					}
					else{
						localThresholds.get(reqdVarsL.get(i).getName()).set(j,extrema.get(reqdVarsL.get(i).getName())[0] + interval*(j+1)); // j+1
					}
				}
			}
		}
		suggestIsSource = false;
		//return divisions;
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
	
/*	
	//public ArrayList<ArrayList<Double>> autoGenT(ArrayList<ArrayList<Double>>  divisions ){
	public ArrayList<ArrayList<Double>> autoGenT(JFrame running){//changed for replacing divisionsL by threholds
	//public HashMap<String,ArrayList<Double>> autoGenT(JFrame running){
		//int iterations = Integer.parseInt(iteration.getText());
		ArrayList<ArrayList<Double>> fullData = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> singleFileData = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> divisions = new ArrayList<ArrayList<Double>>(); //changed for replacing divisionsL by threholds
		//HashMap<String, ArrayList<Double>> localThresholds = new HashMap<String, ArrayList<Double>>();
		//ArrayList<String> allVars;
		int i = 1;
		try{
			while (new File(directory + separator + "run-" + i + ".tsd").exists()) {
				TSDParser tsd = new TSDParser(directory + separator + "run-" + i +".tsd",biosim, false);
				singleFileData = tsd.getData();
				varNames = tsd.getSpecies();
				if (i == 1){
					fullData.add(new ArrayList<Double>());
				}
				(fullData.get(0)).addAll(singleFileData.get(0));
				for (int k = 0; k < reqdVarsL.size(); k++){
					for (int j = 0; j< singleFileData.size(); j++){
						if (reqdVarsL.get(k).getName().equalsIgnoreCase(varNames.get(j))) {
							if (i == 1){
								fullData.add(new ArrayList<Double>());
							}
							(fullData.get(k+1)).addAll(singleFileData.get(j));
							break;
						}
					}
				}
				i++;
			}
			Double[][] extrema = getDataExtrema(fullData); //CHANGE THIS TO HASHMAP for replacing divisionsL by threholds
			//divisions = initDivisions(extrema,divisions);
			divisions = initDivisions(extrema); //changed for replacing divisionsL by threholds
			divisions = greedyOpt(divisions,fullData,extrema); //changed for replacing divisionsL by threholds
			//localThresholds = initDivisions(extrema);
			//localThresholds = greedyOpt(localThresholds,fullData,extrema);
			// Overwriting dmv divisions calculated above with those that come from detectDMV here.
			findReqdVarIndices(); // not required in this case
			HashMap<String, ArrayList<Double>> dmvDivs = detectDMV(fullData,true);
			for (String k : dmvDivs.keySet()){
				for (int l = 0; l < reqdVarsL.size(); l++){
					if (k.equalsIgnoreCase(reqdVarsL.get(l).getName())){
						divisions.get(l).clear();
						divisions.get(l).addAll(dmvDivs.get(k));
						//localThresholds.get(reqdVarsL.get(l).getName()).clear();
						//localThresholds.get(reqdVarsL.get(l).getName()).addAll(dmvDivs.get(k));
						// System.out.println("Divisions for " + k + " are ");
						// for (Double d : dmvDivs.get(k)){
						// 		System.out.println(d.toString() + " ");
						// }
						// System.out.print("\n");
					}
				}
			}
		}
		catch(NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to calculate rates.\nThresholds could not be generated\nWindow size or pathLengthBin must be reduced.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
			try {
				out.write("ERROR! Unable to calculate rates.\nThresholds could not be generated\nIf Window size = -1, pathLengthBin must be reduced;\nElse, reduce windowsize\nLearning unsuccessful.");
				out.close();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			running.setCursor(null);
			running.dispose();
		}
		return divisions; //changed for replacing divisionsL by threholds
		//return localThresholds;
	}
*/	
	
	//public ArrayList<ArrayList<Double>> autoGenT(JFrame running){//changed for replacing divisionsL by threholds
	public HashMap<String,ArrayList<Double>> autoGenT(JFrame running){
		//int iterations = Integer.parseInt(iteration.getText());
		ArrayList<ArrayList<Double>> fullData = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> singleFileData = new ArrayList<ArrayList<Double>>();
		HashMap<String, ArrayList<Double>> localThresholds = new HashMap<String, ArrayList<Double>>();
		//ArrayList<String> allVars;
		int i = 1;
		try{
			while (new File(directory + separator + "run-" + i + ".tsd").exists()) {
				TSDParser tsd = new TSDParser(directory + separator + "run-" + i +".tsd",biosim, false);
				singleFileData = tsd.getData();
				varNames = tsd.getSpecies();
				if (i == 1){
					fullData.add(new ArrayList<Double>());
				}
				(fullData.get(0)).addAll(singleFileData.get(0));
				for (int k = 0; k < reqdVarsL.size(); k++){
					for (int j = 0; j< singleFileData.size(); j++){
						if (reqdVarsL.get(k).getName().equalsIgnoreCase(varNames.get(j))) {
							if (i == 1){
								fullData.add(new ArrayList<Double>());
							}
							(fullData.get(k+1)).addAll(singleFileData.get(j));
							break;
						}
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
								JOptionPane.showMessageDialog(biosim.frame(),
										"Can't generate the number of thresholds for continuous variables.",
										"ERROR!", JOptionPane.ERROR_MESSAGE);
								out.write(reqdVarsL.get(l).getName() + " is not a dmv. So.. ");
								out.write("ERROR! Can't generate the number of thresholds for continuous variables.");
								out.close();
								running.setCursor(null);
								running.dispose();
								return(localThresholds);
							} else {
								out.write(reqdVarsL.get(l).getName() + " is a dmv in autogenT.");
							}
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
							//numThresholds = Integer.parseInt((String)((JComboBox)((JPanel)variablesPanel.getComponent(j)).getComponent(6)).getSelectedItem()) -1; // changed 2 to 3 after required
							varThresholds.put(reqdVarsL.get(k).getName(),Integer.valueOf(numThresholds));
							if (numThresholds == -1){
								if (!reqdVarsL.get(k).isDmvc()){
									JOptionPane.showMessageDialog(biosim.frame(),
											"Can't generate the number of thresholds for continuous variables.",
											"ERROR!", JOptionPane.ERROR_MESSAGE);

									out.write(reqdVarsL.get(k).getName() + " is not a dmv. So.. ");
									out.write("ERROR! Can't generate the number of thresholds for continuous variables.");
									out.close();
									running.setCursor(null);
									running.dispose();
									return(localThresholds);
								}
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
			else{
				HashMap<String, Double[]> extrema = getDataExtrema(fullData); //CHANGE THIS TO HASHMAP for replacing divisionsL by threholds
				//divisions = initDivisions(extrema,divisions);
				localThresholds = initDivisions(extrema);
				localThresholds = greedyOpt(localThresholds,fullData,extrema);
				// Overwriting dmv divisions calculated above with those that come from detectDMV here.
				for (int l = 0; l < reqdVarsL.size(); l++){
					for (String k : dmvDivs.keySet()){
						if ((k.equalsIgnoreCase(reqdVarsL.get(l).getName())) && (varThresholds.get(k) == -1)){
							localThresholds.get(reqdVarsL.get(l).getName()).clear();
							localThresholds.get(reqdVarsL.get(l).getName()).addAll(dmvDivs.get(k));
							// System.out.println("Divisions for " + k + " are ");
							// for (Double d : dmvDivs.get(k)){
							// 		System.out.println(d.toString() + " ");
							// }
							// System.out.print("\n");
						}
					}
				}
			}
		}
		catch(NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to calculate rates.\nThresholds could not be generated\nWindow size or pathLengthBin must be reduced.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
			running.setCursor(null);
			running.dispose();
		} catch (IOException e2) {
			e2.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to write into log file in autogenT.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		//return divisions; //changed for replacing divisionsL by threholds
		return localThresholds;
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
	
	public int pointDistCostVar(ArrayList<Double> dat,ArrayList<Double> div){
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
	

	public Double[] getMinMaxRates(Double[] rateList){
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
		//		public void genBinsRates(String datFile,ArrayList<ArrayList<Double>> divisionsL) { // genBins
		//			TSDParser tsd = new TSDParser(directory + separator + datFile, biosim,false);
		// genBins
		//			data = tsd.getData();
		rateSampling = Integer.parseInt(rateSamplingG.getText().trim());
		pathLengthBin = Integer.parseInt(pathLengthBinG.getText().trim());
		pathLengthVar = Integer.parseInt(pathLengthVarG.getText().trim());
		//	ArrayList<Integer> reqdVarIndices = new ArrayList<Integer>();
		int[][] bins = new int[reqdVarsL.size()][data.get(0).size()];
		for (int i = 0; i < reqdVarsL.size(); i++) {
			//	for (int j = 1; j < varNames.size(); j++) {
			//		if (reqdVarsL.get(i).getName().equalsIgnoreCase(varNames.get(j))) {
			// System.out.println(reqdVarsL.get(i) + " matched "+
			// varNames.get(j) + " i = " + i + " j = " + j);
			//			reqdVarIndices.add(j);
			//changes made here for replacing divisionsL with thresholds
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
		//		}
		//	}
		/*
		 * System.out.println("array bins is :"); for (int i = 0; i <
		 * reqdVarsL.size(); i++) { System.out.print(reqdVarsL.get(i).getName() + "
		 * "); for (int k = 0; k < data.get(0).size(); k++) {
		 * System.out.print(bins[i][k] + " "); } System.out.print("\n"); }
		 */
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
		/*
			try {
				logFile = new File(directory + separator + "tmp.log");
				logFile.createNewFile();
				out = new BufferedWriter(new FileWriter(logFile));
				for (int i = 0; i < (data.get(0).size()); i++) {
					for (int j = 0; j < reqdVarsL.size(); j++) {
						//k = reqdVarIndices.get(j);
						out.write(data.get(j+1).get(i) + " ");// + bins[j][i] + " " +
						// rates[j][i] + " ");
					}
					for (int j = 0; j < reqdVarsL.size(); j++) {
						out.write(bins[j][i] + " ");
					}
					for (int j = 0; j < reqdVarsL.size(); j++) {
						out.write(rates[j][i] + " ");
					}
					out.write(duration[i] + " ");
					out.write("\n");
				}
				out.close();
			} catch (IOException e) {
				System.out.println("Log file couldn't be opened for writing rates and bins ");
			}*/
		return rates;
	}

	public boolean compareBins(int j, int mark,int[][] bins) {
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (bins[i][j] != bins[i][mark]) {
				return false;
			} else {
				continue;
			}
		}
		return true;
	}
	
	public void addPseudo(HashMap<String,ArrayList<Double>> scaledThresholds){
		lpnWithPseudo = new LhpnFile();
		lpnWithPseudo = mergeLhpns(lpnWithPseudo,g);
		//inputs = new ArrayList<Integer>();
		pseudoVars = new HashMap<String,Boolean>();
		pseudoVars.put("ctl",true);
		//for (int i = 0; i < reqdVarsL.size(); i++) {
		//	if (reqdVarsL.get(i).isInput()){
		//		inputs.add(i);
		//	}
		//}
		for (String st : g.getPlaceList()){
			currentPlace = st;
			//TODO: do this only if not prop type place
			if (getPlaceInfoIndex(st) != null)
				currPlaceBin = getPlaceInfoIndex(st).split(",");
			else 
				currPlaceBin = getTransientNetPlaceIndex(st).split(",");
//			visitedPlaces = new HashMap<String,Boolean>();
//			visitedPlaces.put(currentPlace, true);
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
				} else {
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
	public void addMetaBinTransitions(){ 
		// Adds transitions b/w existing metaBins. 
		// Doesn't add any new metabins. Doesn't add transitions b/w metabins & normal bins
		boolean foundBin = false;
		for (String st1 : g.getPlaceList()){
			String p = getPlaceInfoIndex(st1);
			Properties placeP = placeInfo.get(p);
			String[] binEncoding ;
			ArrayList<Integer> syncBinEncoding;
			String o1;
			// st1 w.r.t g
			// p w.r.t placeInfo
			if (placeP.getProperty("type").equalsIgnoreCase("RATE") && placeP.getProperty("metaType").equalsIgnoreCase("true")) {
//				String [] bE = p.split("");
				String [] bE = p.split(",");
				binEncoding = new String[bE.length - 1];
				for (int i = 0; i < bE.length; i++){
					binEncoding[i] = bE[i];    // since p.split("") gives ,0,1 if p was 01
				}
				for (int i = 0; i < binEncoding.length ; i++){
					if ((!reqdVarsL.get(i).isDmvc()) && (Integer.parseInt(placeP.getProperty("metaVar")) != i)){
					if ((getMinRate(p,reqdVarsL.get(i).getName()) < 0)){
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
							if (placeInfo.containsKey(key)){
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
					if (getMaxRate(p,reqdVarsL.get(i).getName()) > 0){
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
							if (placeInfo.containsKey(key)){
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

	public void writeVHDLAMSFile(String vhdFile){
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
					/* Related to the separate net for DMV input driver
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("DMVC")) {
						dmvcPlaces.add(p); // w.r.t placeInfo here
					}*/
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("PROP")) {

					}
				}
				else{
					String p = getTransientNetPlaceIndex(st1);
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")){
						ratePlaces.add(st1); // w.r.t g here
					}
					/* Related to the separate net for DMV input driver
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("DMVC")){
						dmvcPlaces.add(p); // w.r.t g here
					}*/
				}
			}
			/*for (String st:dmvcPlaces){
				System.out.println("p" + placeInfo.get(st).getProperty("placeNum") + "," +placeInfo.get(st).getProperty("DMVCVariable"));
			}*/ 
			/*
			Collections.sort(dmvcPlaces,new Comparator<String>(){
				public int compare(String a, String b){
					String v1 = placeInfo.get(a).getProperty("DMVCVariable");
					String v2 = placeInfo.get(b).getProperty("DMVCVariable");
					if (reqdVarsL.indexOf(v1) < reqdVarsL.indexOf(v2)){
						return -1;
					}
					else if (reqdVarsL.indexOf(v1) == reqdVarsL.indexOf(v2)){
						return 0;
					}
					else{
						return 1;
					}
				}
			});*/
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
			
			/*System.out.println("after sorting:");
			for (String st:dmvcPlaces){
				System.out.println("p" + placeInfo.get(st).getProperty("placeNum") + "," +placeInfo.get(st).getProperty("DMVCVariable"));
			}*/
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
			JOptionPane.showMessageDialog(biosim.frame(),
					"VHDL-AMS model couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(biosim.frame(),
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
					/* Related to the separate net for DMV input driver
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("DMVC")) {
						dmvcPlaces.add(p); // w.r.t placeInfo here
					}*/
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("PROP")) {

					}
				}
				else{
					String p = getTransientNetPlaceIndex(st1);
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")){
						ratePlaces.add(st1); // w.r.t g here
					}
					/* Related to the separate net for DMV input driver
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("DMVC")){
						dmvcPlaces.add(p); // w.r.t  placeInfo here
					}*/
				}
			}
			/*for (String st:dmvcPlaces){
				System.out.println("p" + placeInfo.get(st).getProperty("placeNum") + "," +placeInfo.get(st).getProperty("DMVCVariable"));
			}*/ 
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
							/*buffer3.append("\tinitial\n\tbegin\n");
							buffer3.append("\t\t#"+  (int)(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin"))+ Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
							// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
							buffer3.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + transientNetPlaces.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/valScaleFactor + ";\n");
							buffer3.append("\tend\n");*/
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
		/*if (failPropVHDL != null){
				vhdlAms.write("\tprocess\n");
				vhdlAms.write("\tbegin\n");
				vhdlAms.write("\t\twait until " + failPropVHDL + ";\n");
				vhdlAms.write("\t\tfail := true;\n");
				vhdlAms.write("\tend process;\n\n");
			}
		//	vhdlAms.write("\tend process;\n\n");
			vhdlAms.write("end "+vhdFile.split("\\.")[0]+";\n");*/
			
		}
		catch(IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Verilog-AMS model couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(biosim.frame(),
					"Error in Verilog-AMS model generation.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
	//T[] aux = (T[])a.clone();
	
	public LhpnFile mergeLhpns(LhpnFile l1,LhpnFile l2){//(LhpnFile l1, LhpnFile l2){
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
			JOptionPane.showMessageDialog(biosim.frame(),
					"Problem while merging lpns",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		return l1;
	}
}

/* 
	private String traceBack(String place, String var){
		String enabling = null;
		try{
			visitedPlaces.put(place,true);
			for (String presetTrans : g.getPreset(place)){
				ExprTree enableTree = g.getEnablingTree(presetTrans);
				if ((enableTree != null) && (enableTree.containsVar(var))){
					enabling = enableTree.toString();
					return enabling;
				}
			}
			for (String presetTrans : g.getPreset(place)){
				for (String presetPlace : g.getPreset(presetTrans)){
					if (!visitedPlaces.containsKey(presetPlace)){
						enabling = traceBack(presetPlace,var);
						if (enabling != null)
							return enabling;
					}
				}
			}
		} catch (NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Exception while tracing back for making the enabling conditions mutually exclusive.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);

		}
		return enabling;
	}
		
This method is used for creating separate nets for each DMV input variable driver.
public void updateTimeInfo(int[][] bins, Properties cvgProp) {
	String prevPlace = null;
	String currPlace = null;
	Properties p3 = null;
	//ArrayList<String> dmvcPlaceL = new ArrayList<String>(); // only dmvc inputs
	boolean exists;
	// int dmvcCnt = 0; making this global .. rechk
	String[] places;
	try {
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (reqdVarsL.get(i).isDmvc() && reqdVarsL.get(i).isInput()) {
				out.write(reqdVarsL.get(i).getName() + " is a dmvc input variable \n");
				// dmvcCnt = 0; in case of multiple tsd files, this may be a problem. may create a new distinct place with an existing key.??
				prevPlace = null;
				currPlace = null;
				p3 = null;
				Properties p2 = null;
				String k;
				DMVCrun runs = reqdVarsL.get(i).getRuns();
				Double[] avgVals = runs.getAvgVals();
				out.write("variable " + reqdVarsL.get(i).getName() + " Number of runs = " + avgVals.length + "Avg Values are : " + avgVals.toString() + "\n");
				for (int j = 0; j < avgVals.length; j++) { // this gives number of runs/startpoints/endpoints
					exists = false;
					places = g.getPlaceList();
					if (places.length > 1) {
						for (String st : places) {
							k = getPlaceInfoIndex(st);
							if (!isTransientPlace(st) && (placeInfo.get(k).getProperty("type").equalsIgnoreCase("DMVC"))) {
								if ((Math.abs(Double.parseDouble(placeInfo.get(k).getProperty("DMVCValue")) - avgVals[j]) < epsilon)
										&& (placeInfo.get(k).getProperty("DMVCVariable").equalsIgnoreCase(reqdVarsL.get(i).getName()))) {
				//					out.write("Place with key " + k + "already exists. so adding dmvcTime to it\n");
									addDmvcTime(placeInfo.get(k), reqdVarsL.get(i).getName(), calcDelay(runs.getStartPoint(j), runs.getEndPoint(j)));
									addDuration(placeInfo.get(k),calcDelay(runs.getStartPoint(j), runs.getEndPoint(j)));
									exists = true;
									prevPlace = currPlace;
									currPlace = getPlaceInfoIndex(st);// k;
									p2 = placeInfo.get(currPlace);
	//next few lines commented to remove multiple dmv input places of same variable from being marked initially.
									//	if (j == 0) { // adding the place corresponding to the first dmv run to initial marking. 
									//	placeInfo.get(k).setProperty("initiallyMarked", "true");
									//	g.changeInitialMarking("p"	+ placeInfo.get(k).getProperty("placeNum"),true);
									//} 
									// break ; here?
								}
							}
						}
					}
					if (!exists) {
						prevPlace = currPlace;
						currPlace = "d_" + i + "_" + dmvcCnt;
						p2 = new Properties();
						p2.setProperty("placeNum", numPlaces.toString());
						p2.setProperty("type", "DMVC");
						p2.setProperty("DMVCVariable", reqdVarsL.get(i).getName());
						p2.setProperty("DMVCValue", avgVals[j].toString());
						p2.setProperty("initiallyMarked", "false");
						addDmvcTime(p2, reqdVarsL.get(i).getName(),calcDelay(runs.getStartPoint(j), runs.getEndPoint(j)));
						//placeInfo.put("d_" + i + "_" + dmvcCnt, p2);
						if (j == 0) {
							transientNetPlaces.put("d_" + i + "_" + dmvcCnt, p2);
							g.addPlace("p" + numPlaces, true);
							p2.setProperty("initiallyMarked", "true");
						}
						else{
							placeInfo.put("d_" + i + "_" + dmvcCnt, p2);
							g.addPlace("p" + numPlaces, false);
						}
						dmvcInputPlaces.add("p" + numPlaces);
						if (j == 0) { // adding the place corresponding to the first dmv run to initial marking
							p2.setProperty("initiallyMarked", "true");
							g.changeInitialMarking("p" + p2.getProperty("placeNum"), true);
						}
						numPlaces++;
						out.write("Created new place with key " + "d_" + i + "_" + dmvcCnt + "\n");
						dmvcCnt++;
						//dmvcPlaceL.add(currPlace);
						cvgProp.setProperty("places", String.valueOf(Integer.parseInt(cvgProp.getProperty("places"))+1));
					}
					Double d = calcDelay(runs.getStartPoint(j), runs.getEndPoint(j));// data.get(0).get(runs.getEndPoint(j)) - data.get(0).get(runs.getStartPoint(j));
						// data.get(0).get(reqdVarsL.get(prevPlace.getDmvcVar()).getRuns().getEndPoint(j-1));
					// TEMPORARY FIX
					//Double minTime = getMinDmvcTime(p2);
					//if ((d > minTime*Math.pow(10.0, 4.0))){ // && (getMinDmvcTime(p2) == getMaxDmvcTime(p2))){
					//	deleteInvalidDmvcTime(p2, getMinDmvcTime(p2));	// updates dmin,dmax too
					//}
					//END TEMPORARY FIX
		// For dmv input nets, transition's delay assignment is
		// it's preset's duration; value assgnmt is value in postset.
					addDuration(p2,d);	
					//boolean transientNet = false;
					//	out.write("Delay in place p"+ p2.getProperty("placeNum")+ " after updating " + d + " is ["+ p2.getProperty("dMin") + ","+ p2.getProperty("dMax") + "]\n");
					if (prevPlace != null) {
						if (transitionInfo.containsKey(prevPlace + currPlace)) {
							p3 = transitionInfo.get(prevPlace + currPlace);
						} else {
							p3 = new Properties();
							p3.setProperty("transitionNum", numTransitions.toString());
							if (transientNetPlaces.containsKey(prevPlace)){
								transientNetTransitions.put(prevPlace + currPlace, p3);
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addMovement("p" + transientNetPlaces.get(prevPlace).getProperty("placeNum"), "t" + transientNetTransitions.get(prevPlace + currPlace).getProperty("transitionNum")); 
								g.addMovement("t" + transientNetTransitions.get(prevPlace + currPlace).getProperty("transitionNum"), "p" + placeInfo.get(currPlace).getProperty("placeNum"));
							//	transientNet = true;
							}
							else{
								transitionInfo.put(prevPlace + currPlace, p3);
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addMovement("p"+ placeInfo.get(prevPlace).getProperty("placeNum"), "t"+ transitionInfo.get(prevPlace + currPlace).getProperty("transitionNum")); 
								g.addMovement("t"+ transitionInfo.get(prevPlace+ currPlace).getProperty("transitionNum"),"p"+ placeInfo.get(currPlace).getProperty("placeNum"));
							}
							numTransitions++;
							cvgProp.setProperty("transitions", String.valueOf(Integer.parseInt(cvgProp.getProperty("transitions"))+1));
						}
					}
					//if (!transientNet){	// assuming postset duration
					//	addDuration(p2, d);
					//}
					//else {
					//	addTransientDuration(p2, d);
					//}
					
				}
			} else if (reqdVarsL.get(i).isDmvc()) { // non-input dmvc

			}
		}
	} catch (IOException e) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(biosim.frame(),
				"Log file couldn't be opened for writing rates and bins.",
				"ERROR!", JOptionPane.ERROR_MESSAGE);
	}
}
*/

/*
			ArrayList<ArrayList<String>> ifL = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> ifRateL = new ArrayList<ArrayList<String>>();
			for (Variable v: reqdVarsL){
				ifL.add(new ArrayList<String>());
				ifRateL.add(new ArrayList<String>());
			}
			String[] tL;
			for (String p : ratePlaces){
				tL = g.getPreset(p);
				for (String t : tL){
					if ((g.getPreset(t).length != 0) && (g.getPostset(t).length != 0) && (placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
							&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
						ArrayList<Integer> diffL = diff(getPlaceInfoIndex(g.getPreset(t)[0]), getPlaceInfoIndex(g.getPostset(t)[0]));
						String ifStr = "";
						String[] binIncoming = getPlaceInfoIndex(g.getPreset(t)[0]).split("");
						String[] binOutgoing = getPlaceInfoIndex(g.getPostset(t)[0]).split("");
						boolean above;
						double val;
						for (int k : diffL) {
							if (Integer.parseInt(binIncoming[k + 1]) < Integer.parseInt(binOutgoing[k + 1])) {
								val = divisionsL.get(k).get(Integer.parseInt(binIncoming[k + 1])).doubleValue();
								above = true;
							} else {
								val = divisionsL.get(k).get(Integer.parseInt(binOutgoing[k + 1])).doubleValue();
								above = false;
							}
							if (above) {
								ifStr = reqdVarsL.get(k).getName()+"'above("+val+")";
							}
							else{
								ifStr = "not "+ reqdVarsL.get(k).getName()+"'above("+val+")";
							}
							for (int j = 0; j<reqdVarsL.size(); j++){
								String rateStr = "";
								if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
								//if (!(reqdVarsL.get(j).isInput() && reqdVarsL.get(j).isDmvc())){
									rateStr = reqdVarsL.get(j).getName() + "'dot == span(" + getMinRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0,"+getMaxRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0)";
									ifL.get(j).add(ifStr);
									ifRateL.get(j).add(rateStr);
								}
							}
						}
					}
				}
			}
			for (int i = 0; i<reqdVarsL.size(); i++){
				for (int j = 0; j<ifL.get(i).size(); j++){
					if (j==0){
						vhdlAms.write("\tif "+ifL.get(i).get(j)+" use\n");
					}
					else{
						vhdlAms.write("\telsif "+ifL.get(i).get(j)+" use\n");
					}
					vhdlAms.write("\t\t"+ ifRateL.get(i).get(j)+";\n");
				}
				if (ifL.get(i).size() != 0){
					vhdlAms.write("\tend use;\n\n");
				}
			}
		//	vhdlAms.write("\tprocess\n");
		//	vhdlAms.write("\tbegin\n");
			for (int i = 0; i < dmvcVarPlaces.size(); i++){
				if (dmvcVarPlaces.get(i).size() != 0){
				vhdlAms.write("\tprocess\n");
				vhdlAms.write("\tbegin\n");
				for (String p : dmvcVarPlaces.get(i)){
					vhdlAms.write("\t\twait for delay("+placeInfo.get(p).getProperty("dMax")+","+placeInfo.get(p).getProperty("dMin") +");\n");
		// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
					vhdlAms.write("\t\tbreak "+reqdVarsL.get(i).getName()+ " => "+ placeInfo.get(p).getProperty("DMVCValue") + ";\n");
				}
				vhdlAms.write("\tend process;\n\n");
				}
			}
		//	vhdlAms.write("\tend process;\n\n");
			vhdlAms.write("end "+vhdFile.split("\\.")[0]+";\n");
			vhdlAms.close();
			
		}
		catch(IOException e){
			
		}
	}
	//T[] aux = (T[])a.clone();
}
*/
/*  OBSOLETE METHODS
public ArrayList<ArrayList<Double>> parseBinFile() {
	reqdVarsL = new ArrayList<Variable>();
	ArrayList<String> linesBinFileL = null;
	int h = 0;
	//ArrayList<ArrayList<Double>> divisionsL = new ArrayList<ArrayList<Double>>();
	
	try {
		Scanner f1 = new Scanner(new File(directory + separator + binFile));
		// log.addText(directory + separator + binFile);
		linesBinFileL = new ArrayList<String>();
		linesBinFileL.add(f1.nextLine());
		while (f1.hasNextLine()) {
			linesBinFileL.add(f1.nextLine());
		}
		out.write("Required variables and their levels are :");
		for (String st : linesBinFileL) {
			divisionsL.add(new ArrayList<Double>());
			String[] wordsBinFileL = st.split("\\s");
			for (int i = 0; i < wordsBinFileL.length; i++) {
				if (i == 0) {
					reqdVarsL.add(new Variable(wordsBinFileL[i]));
					out.write("\n"	+ reqdVarsL.get(reqdVarsL.size() - 1).getName());
				} else {
					divisionsL.get(h).add(Double.parseDouble(wordsBinFileL[i]));

				}
			}
			out.write(" " + divisionsL.get(h));
			h++;
			// max = Math.max(max, wordsBinFileL.length + 1);
		}
		f1.close();

	} catch (Exception e1) {
	}
	return divisionsL;
}

 public String cleanRow (String row){ 
  	String rowNS,rowTS = null; 
  	try{ 
  		rowNS =lParenR.matcher(row).replaceAll(""); 
  		rowTS = rParenR.matcher(rowNS).replaceAll(""); 
  		return rowTS; 
  	}
  	catch(PatternSyntaxException pse){ 
 	    System.out.format("There is a problem withthe regular expression!%n"); 
	    System.out.format("The pattern in question is:%s%n",pse.getPattern()); 
	    System.out.format("The description is:%s%n",pse.getDescription()); 
  	    System.out.format("The message is:%s%n",pse.getMessage()); 
 	    System.out.format("The index is:%s%n",pse.getIndex()); 
	    System.exit(0); 
	    return rowTS; 
	} 
}
 */