package learn;

//import gcm2sbml.parser.GCMFile;
import lhpn2sbml.parser.LHPNFile;
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

	// private JTextField windowRising, windowSize;

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

	// SB

	private ArrayList<Variable> reqdVarsL;

	private ArrayList<Integer> reqdVarIndices;

	private ArrayList<ArrayList<Double>> data;

	private ArrayList<String> varNames;

	private int[][] bins;

	private ArrayList<ArrayList<Double>> divisionsL;
	
	private HashMap<String, ArrayList<Double>> thresholds;

	private Double[][] rates;

	private Double[] duration;

	private int dmvcCnt = 0;

	private int pathLength ; //= 7 ;// intFixed 25 pd 7 integrator 15;

	private int rateSampling ; //= -1 ; //intFixed 250; 20; //-1;

	private boolean placeRates = true;

	private LHPNFile g;

	private Integer numPlaces = 0;

	private Integer numTransitions = 0;

	HashMap<String, Properties> placeInfo;

	HashMap<String, Properties> transitionInfo;
	
	HashMap<String, Properties> cvgInfo;

	/*
	 * public enum PType { RATE, DMVC, PROP, ASGN, TRACE }
	 */
	private Double minDelayVal = 10.0;

	private Double minRateVal = 10.0;

	private Double minDivisionVal = 10.0;

	// private String decPercent;

	// private boolean limitExists;

	private Double delayScaleFactor = 1.0;

	private Double varScaleFactor = 1.0;

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
	
	private JTextField pathLengthG;
	
	private JTextField rateSamplingG;
	
	private JTextField runTimeG;
	
	private JTextField runLengthG;
	
	private boolean suggestIsSource = false;
	
	private Double[] lowerLimit;
	
	private Double[] upperLimit; 
	
	private String[] transEnablingsVHDL;
	
	private String[][] transIntAssignVHDL;
	
	private String[] transDelayAssignVHDL;
	
	private String[] transEnablingsVAMS;
	
	private Double[][] transIntAssignVAMS;
	
	private Integer[] transDelayAssignVAMS;
	
	private String failPropVHDL;

	private HashMap<String, Properties> transientNetPlaces;

	private HashMap<String, Properties> transientNetTransitions;

	private ArrayList<String> ratePlaces;

	private ArrayList<String> dmvcInputPlaces;

	private ArrayList<String> propPlaces;
	
	// private int[] numValuesL;// the number of constant values for each variable...-1 indicates that the variable isn't considered a DMVC variable

	// private double vaRateUpdateInterval = 1e-6;// how often the rate is added
	// to the continuous variable in the Verilog-A model output

	// Pattern lParenR = Pattern.compile("\\(+"); //SB
	
	//Pattern floatingPointNum = Pattern.compile(">=(-*[0-9]+\\.*[0-9]*)"); //SB

	// Pattern absoluteTimeR = Pattern.compile(".absoluteTime"); //SB

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
		lrnFile = getFilename[getFilename.length - 1] + ".lrn";
//		binFile = getFilename[getFilename.length - 1] + ".bins";
//		newBinFile = getFilename[getFilename.length - 1] + "_NEW" + ".bins";
		lhpnFile = getFilename[getFilename.length - 1] + ".lpn";
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
		selection1.add(points);
		selection1.add(range);
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
		JPanel thresholdPanel1 = new JPanel(new GridLayout(4, 2));
		
		JLabel backgroundLabel = new JLabel("Model File:");
		backgroundField = new JTextField(lhpnFile);
		backgroundField.setEditable(false);
		thresholdPanel1.add(backgroundLabel);
		thresholdPanel1.add(backgroundField);
		JLabel iterationLabel = new JLabel("Iterations of Optimization Algorithm");
		iteration = new JTextField("10");
		thresholdPanel1.add(iterationLabel);
		thresholdPanel1.add(iteration);
		
		// SB
		
		JLabel rateLabel = new JLabel("Rate calculation parameters");
		JLabel epsilonLabel = new JLabel("Epsilon");
		epsilonG = new JTextField("0.1");
		JLabel pathLengthLabel = new JLabel("Minimum Pathlength");//("Pathlength");
		pathLengthG = new JTextField("0");
		JLabel rateSamplingLabel = new JLabel("Window Size");//("Rate Sampling");
		rateSamplingG = new JTextField("-1");
		JLabel dmvcLabel = new JLabel("DMVC determination parameters");
		JLabel absTimeLabel = new JLabel("Absolute Time");
		absTimeG = new JCheckBox();
		absTimeG.setSelected(true);
		absTimeG.addItemListener(this); 
		JLabel percentLabel = new JLabel("Fraction");
		percentG = new JTextField("0.8");
		JLabel runTimeLabel = new JLabel("Dmvc Run Time");
		runTimeG = new JTextField("5e-6");
		JLabel runLengthLabel = new JLabel("DMVC Run Length");
		runLengthG = new JTextField("15");
		runLengthG.setEnabled(false);
		
		epsilonG.addActionListener(this); //SB
		pathLengthG.addActionListener(this); //SB
		rateSamplingG.addActionListener(this); //SB
		percentG.addActionListener(this); //SB
		runTimeG.addActionListener(this); //SB
		runLengthG.addActionListener(this); //SB
		
		JPanel newPanel = new JPanel();
		JPanel jPanel1 = new JPanel();
		JPanel jPanel2 = new JPanel();

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
                                .add(pathLengthLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                                .add(68, 68, 68)
                                .add(pathLengthG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(60, 60, 60)
                        .add(rateLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 186, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {epsilonG, pathLengthG, rateSamplingG}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

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
                    .add(pathLengthLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pathLengthG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(35, 35, 35))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {epsilonG, epsilonLabel, pathLengthG, pathLengthLabel, rateSamplingG, rateSamplingLabel}, org.jdesktop.layout.GroupLayout.VERTICAL);
       
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
		divisionsL = new ArrayList<ArrayList<Double>>(); // SB
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
		String[] bins = { "2", "3", "4", "5", "6", "7", "8", "9" };
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
		LHPNFile lhpn = new LHPNFile(log);
		// log.addText(learnFile);
		lhpn.load(learnFile);
		HashMap<String, Properties> variablesMap = lhpn.getVariables();
		for (String s : variablesMap.keySet()) {
			variablesList.add(s);
			reqdVarsL.add(new Variable(s));
			divisionsL.add(new ArrayList<Double>());
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
			if (load.containsKey("learn.pathLength")){
				pathLengthG.setText(load.getProperty("learn.pathLength"));
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
			if (load.containsKey("learn.varsList")){
				String varsListString = load.getProperty("learn.varsList");
				String[] varsList = varsListString.split("\\s");
				j = 0;
				for (String st1 : varsList){
					String s = load.getProperty("learn.bins" + st1);
					if (s != null){
						String[] savedBins = s.split("\\s");
						//divisionsL.add(new ArrayList<Double>());
						//variablesList.add(savedBins[0]);
						//	((JComboBox)(((JPanel)variablesPanel.getComponent(j+1)).getComponent(2))).setSelectedItem(savedBins[1]);
						boolean varFound = false;
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
									divisionsL.get(j).add(Double.parseDouble(savedBins[i]));
									thresholds.get(st1).add(Double.parseDouble(savedBins[i]));
								}
							}
							j++;
						}
					}
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
		} catch (IOException e) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}
		try {
			FileWriter write = new FileWriter(new File(directory + separator + "background.g"));
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
		String vhdFile = lhpnFile.replace(".lpn",".vhd");
		if (!(new File(directory + separator + vhdFile).exists())) {
			viewVHDL.setEnabled(false);
		}
		else{
			viewVHDL.setEnabled(true);
		}
		
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
		secondTab.add(newPanel,"Center");
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
					int combox_selected = Integer.parseInt((String) ((JComboBox) variables.get(i).get(2)).getSelectedItem());
					if (divisionsL.get(i).size() >= combox_selected){
						for (int j = divisionsL.get(i).size() - 1; j >= (combox_selected-1) ; j--){
							divisionsL.get(i).remove(j); //combox_selected);
							thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).remove(j);
						}
					}
				}
			}
			else if ( auto.isSelected()) {  // variables != null // action source is numBins on top
				int combox_selected = Integer.parseInt((String) numBins.getSelectedItem());
				for (int i = 0; i < variablesList.size(); i++) {
					editText(i);  //editText not required??
					// SB
					if (divisionsL.get(i).size() >= combox_selected){
						for (int j = divisionsL.get(i).size() - 1; j >= (combox_selected-1) ; j--){
							divisionsL.get(i).remove(j); //combox_selected);
							thresholds.get(variablesList.get(i)).remove(j);
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
				if (reqdVarsL.get(j-1).isInput()){ 
					((JCheckBox)((JPanel) c).getComponent(1)).setSelected(true); // SB
				}	
				j++;
			}
			biosim.setGlassPane(true);
		} else if (e.getSource() == numBins || e.getSource() == debug) {
			biosim.setGlassPane(true);
		} else if (e.getActionCommand().contains("dmv")) {
			int num = Integer.parseInt(e.getActionCommand().substring(3)) - 1;
			editText(num);
		} else if (e.getActionCommand().contains("input")) {
			//int num = Integer.parseInt(e.getActionCommand().substring(5));  // -1; ??
			//reqdVarsL.get(num).setInput(!reqdVarsL.get(num).isInput());
			String var = e.getActionCommand().substring(5,e.getActionCommand().length());  // -1; ??
			for (int i = 0; i < reqdVarsL.size() ; i++){
				if (var.equalsIgnoreCase(reqdVarsL.get(i).getName())){
					reqdVarsL.get(i).setInput(!reqdVarsL.get(i).isInput());
					break;
				}
			}
		}
		else if (e.getSource() == user) {
			if (!firstRead) {
				try {
					for (int i = 0; i < variables.size(); i++) {
						if (divisionsL.get(i).size() == 0){  // This condition added later.. This ensures that when you switch from auto to user, the options of auto are written to the textboxes. SB.. rechk
							for (int j = 3; j < variables.get(i).size(); j++) { // changed 2 to 3 SB
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
									if (divisionsL.get(i).size() <= (j-3)){
										divisionsL.get(i).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
										thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
									}
									else{
										divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
										thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
									}
								}
							}
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
				for (int i = 2; i < ((JPanel) c).getComponentCount(); i++) { // changed 1 to 2 SB
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
				if (reqdVarsL.get(j-1).isInput()){ //tempPorts.get(j-1)){
					((JCheckBox)((JPanel) c).getComponent(1)).setSelected(true); // SB
				}
				j++;
			}
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
					for (int j = 3; j < variables.get(i).size(); j++) {  // changed 2 to 3 SB
						if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
//							write.write(" ?");
//							writeNew.write(" ?");
					//		divisionsL.get(i).set(j-3,null);
						} else {
//							write.write(" "	+ ((JTextField) variables.get(i).get(j)).getText().trim());
//							writeNew.write(" " + ((JTextField) variables.get(i).get(j)).getText().trim());
					//		divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							if (divisionsL.get(i).size() <= (j-3)){
								divisionsL.get(i).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
								thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
							else{
								divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
								thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
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
				new Thread(this).start();
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
				variablesPanel.setLayout(new GridLayout(
						variablesList.size() + 1, 1));
				int max = 0;
				if (!divisionsL.isEmpty()){
					for (int i = 0; i < divisionsL.size(); i++){
						if (divisionsL.get(i) != null) {
							max = Math.max(max, divisionsL.get(i).size()+2);
						}
					}
				}
				JPanel label = new JPanel(new GridLayout());
				// label.add(new JLabel("Use"));
				label.add(new JLabel("Variables"));
				// label.add(new JLabel("DMV"));
				label.add(new JLabel("Input")); //SB
				label.add(new JLabel("Number Of Bins"));
				for (int i = 0; i < max - 2; i++) {
					label.add(new JLabel("Level " + (i + 1)));
				}
				variablesPanel.add(label);
				int j = 0;
				for (String s : variablesList) {
					j++;
					JPanel sp = new JPanel(new GridLayout());
					ArrayList<Component> specs = new ArrayList<Component>();
					// JCheckBox check = new JCheckBox();
					// check.setSelected(true);
					// specs.add(check);
					specs.add(new JTextField(s));
					String[] options = { "2", "3", "4", "5", "6", "7", "8", "9" };
					// String[] options = { "3", "4", "5", "6", "7", "8", "9" };
					JComboBox combo = new JComboBox(options);
					// String[] dmvOptions = { "", "Yes", "No" };
					// JComboBox dmv = new JComboBox(dmvOptions);
					JCheckBox dmv = new JCheckBox();
					JCheckBox input = new JCheckBox(); //SB
					// dmv.setSelectedIndex(0);
					dmv.addActionListener(this);
					input.addActionListener(this); //SB
					dmv.setActionCommand("dmv" + j);
			//		input.setActionCommand("input" + (j-1)); // SB  j-1 or j ??????
					input.setActionCommand("input" + variablesList.get(j-1)); // SB
					combo.setSelectedItem(numBins.getSelectedItem());
					// specs.add(dmv);
					specs.add(input); //SB
					specs.add(combo);
					((JTextField) specs.get(0)).setEditable(false);
					// sp.add(specs.get(0));
					// ((JCheckBox) specs.get(0)).addActionListener(this);
					// ((JCheckBox) specs.get(0)).setActionCommand("box" + j);
					sp.add(specs.get(0));
					sp.add(specs.get(1));
					sp.add(specs.get(2));  // Uncommented SB
					if ((j-1) < reqdVarsL.size() && reqdVarsL.get(j-1).isInput()){
						((JCheckBox) specs.get(1)).setSelected(true);
					}
					else{
						((JCheckBox) specs.get(1)).setSelected(false);
					}
					((JComboBox) specs.get(2)).addActionListener(this); // changed 1 to 2 SB
					((JComboBox) specs.get(2)).setActionCommand("text" + j);// changed 1 to 2 SB
					this.variables.add(specs);
					if (!divisionsL.isEmpty()) {
						boolean found = false;
				//		if ((j-1) < divisionsL.size()) {		COMMENTED BY SB
				//			divisionsL.add(null);
				//		}
							ArrayList<Double> div =  divisionsL.get(j-1);
							// log.addText(s + " here " + st);
							// String[] getString = st.split(" ");
							// log.addText(getString[0] + s);
							/*if (getString[0].trim().equals(".dmvc")) {
								for (int i = 1; i < getString.length; i++) {
									if (getString[i].equals(s)) {
										// log.addText(s);
										// ((JCheckBox) specs.get(1)).setSelected(true);
									}
								}
							} else if (getString[0].trim().equals(s)) {*/
								found = true;
							//	if (getString.length >= 1) {
								if (div.size() >= 0){
									//((JComboBox) specs.get(2)).setSelectedItem("div.size()+1");// Treats div.size() as string & doesn't work.. changed 1 to 2 SB
									((JComboBox) specs.get(2)).setSelectedItem(String.valueOf(div.size()+1));// changed 1 to 2 SB
									for (int i = 0; i < (Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()) - 1); i++) {// changed 1 to 2 SB
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
										sp.add(specs.get(i + 3)); // changed 2 to 3 SB
									}
									for (int i = Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()) - 1; i < max - 2; i++) {// changed 1 to 2 SB
										sp.add(new JLabel());
									}
								}
						//	}
						//}
						if (!found) {
							for (int i = 0; i < Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()) - 1; i++) {// changed 1 to 2 SB
								specs.add(new JTextField(""));
								sp.add(specs.get(i + 2));// changed 1 to 2 SB
							}
							for (int i = Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()) - 1; i < max - 2; i++) {// changed 1 to 2 SB
								sp.add(new JLabel());
							}
						}
					} else {
						for (int i = 0; i < Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()) - 1; i++) {// changed 1 to 2 SB
							specs.add(new JTextField(""));
							sp.add(specs.get(i + 2));// changed 1 to 2 SB
						}
					}
					variablesPanel.add(sp);
				}
			}
		}
		editText(0);
	}

	private void editText(int num) {
		try {
			ArrayList<Component> specs = variables.get(num);
			Component[] panels = variablesPanel.getComponents();
			int boxes = Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()); //changed 1 to 2 SB
			if ((specs.size() - 3) < boxes) { // changed 2 to 3 SB
				for (int i = 0; i < boxes - 1; i++) {
					try {
						specs.get(i + 3); // changed 2 to 3 SB
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
							specs.remove(boxes + 2); // changed 1 to 2 SB
							((JPanel) panels[num + 1]).remove(boxes + 2); // changed 1 to 2 SB
						}
					} else if (boxes == 0) {
						while (true) {
							specs.remove(3); // changed 2 to 3 SB
							((JPanel) panels[num + 1]).remove(3); // changed 2 to 3 SB
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
				for (int i = 0; i < max - 3; i++) { //changed 2 to 3 SB
					try {
						((JPanel) panels[0]).getComponent(i + 3); //changed 2 to 3 SB
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
				for (int j = sp.getComponentCount() - 1; j >= 3; j--) {//changed 2 to 3 SB
					if (sp.getComponent(j) instanceof JLabel) {
						sp.remove(j);
					}
				}
				if (max > sp.getComponentCount()) {
					for (int j = sp.getComponentCount(); j < max; j++) {
						sp.add(new JLabel());
					}
				} else {
					for (int j = sp.getComponentCount() - 2; j >= max; j--) {//changed 2 to 3 SB .. not sure??
						sp.remove(j);
					}
				}
			}
		} catch (Exception e) {
		}
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
						if (!copy.substring(copy.length() - 4).equals(".lpn")) {
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
					"Unable to view LHPN Model.", "Error",
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
		JLabel label = new JLabel("<html><b>Learning Completed Successfully</b></html>",SwingConstants.CENTER);
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
			String vamsFileName = lhpnFile.replace(".lpn", ".vams");
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
				int inputCount = 0;
				String ip = null;
				int numOfBins = Integer.parseInt(this.numBins.getSelectedItem().toString());
				//int numThresholds = numOfBins -1;
				for (Component c : variablesPanel.getComponents()) {
					if (k == 0){
						k++;
						continue;
					}
					if (k == 1){
						varsList = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
					}
					else{
						varsList += " "+ ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
					}
					String s =  ((JTextField)((JPanel)c).getComponent(0)).getText().trim() + " " + numOfBins;
					if ((divisionsL != null) && (divisionsL.size() != 0)){
						for (int i = 0; i < (numOfBins -1); i++){
							if ((divisionsL.get(k-1)!= null) && (divisionsL.get(k-1).size() > i)){
								s += " ";
								s += divisionsL.get(k-1).get(i);
							}
						}
					}
					prop.setProperty("learn.bins"+ ((JTextField)((JPanel)c).getComponent(0)).getText().trim(), s);
					if (((JCheckBox)((JPanel)c).getComponent(1)).isSelected()){
						if (inputCount == 0){
							inputCount++;
							ip = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
						}
						else{
							ip = ip + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
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
			} else {
				prop.setProperty("learn.use", "user");
				int k = 0;
				int inputCount = 0;
				String ip = null;
				for (Component c : variablesPanel.getComponents()) {
					if (k == 0){
						k++;
						continue;
					}
					if (k == 1){
						varsList = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
					}
					else{
						varsList += " "+ ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
					}
					String s =  ((JTextField)((JPanel)c).getComponent(0)).getText().trim() + " " + (String)((JComboBox)((JPanel)c).getComponent(2)).getSelectedItem();
					int numOfBins = Integer.parseInt((String)((JComboBox)((JPanel)c).getComponent(2)).getSelectedItem())-1;
					for (int i = 0; i < numOfBins; i++){
						s += " ";
						s += ((JTextField)(((JPanel)c).getComponent(i+3))).getText().trim();
					}
					prop.setProperty("learn.bins"+ ((JTextField)((JPanel)c).getComponent(0)).getText().trim(), s);
					if (((JCheckBox)((JPanel)c).getComponent(1)).isSelected()){
						if (inputCount == 0){
							inputCount++;
							ip = ((JTextField)((JPanel)c).getComponent(0)).getText().trim();//((JTextField)((JPanel)c).getComponent(0)).getText().trim();
						}
						else{
							ip = ip + " " + ((JTextField)((JPanel)c).getComponent(0)).getText().trim();
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
			}
			prop.setProperty("learn.epsilon", this.epsilonG.getText().trim());
			prop.setProperty("learn.pathLength", this.pathLengthG.getText().trim());
			prop.setProperty("learn.rateSampling", this.rateSamplingG.getText().trim());
			prop.setProperty("learn.percent", this.percentG.getText().trim());
			prop.setProperty("learn.absTime",String.valueOf(this.absTimeG.isSelected()));
			prop.setProperty("learn.runTime",this.runTimeG.getText().trim());
			prop.setProperty("learn.runLength",this.runLengthG.getText().trim());
			prop.setProperty("learn.varsList",varsList);
			
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
					for (int j = 3; j < variables.get(i).size(); j++) {
						if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
						} else {
							if (divisionsL.get(i).size() <= (j-3)){
								divisionsL.get(i).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
								thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
							else{
								divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
								thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
						}
					}
				}
				generate = true;
			} else {
				for (int i = 0; i < variables.size(); i++) {
					for (int j = 3; j < variables.get(i).size(); j++) {  // changed 2 to 3 SB
						if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {

						} else {
							if (divisionsL.get(i).size() <= (j-3)){
								divisionsL.get(i).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
								thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).add(Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
							else{
								divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
								thresholds.get(((JTextField) variables.get(i).get(0)).getText().trim()).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
						}
					}
				}
				generate = false;
			}
			execute = true;
			new Thread(this).start();
		} 
		catch (NullPointerException e1) {
			System.out.println("Some problem with thresholds hashmap");
		}
		catch (Exception e) {
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
			//final JButton cancel = new JButton("Cancel");
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
		//	button.add(cancel);
			all.add(text, "North");
			all.add(progBar, "Center");
		//	all.add(button, "South");
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
			if (generate) {
		//		log.addText("Running:");
		//		log.addText("autoGenT()");
				out.write("Running autoGenT\n");
				//divisionsL = autoGenT(divisionsL);		
				divisionsL = autoGenT(running);	
				
/*
				String makeBin = "autogenT.py -b" + newBinFile + " -i"
						+ iteration.getText();
				if (range.isSelected()) {
					makeBin = makeBin + " -cr";
				}
				log.addText(makeBin);
				// log.addText("Creating levels file:\n" + directory + separator
				// + binFile + "\n");
				final Process bins = Runtime.getRuntime().exec(makeBin, null,
						work);
				cancel.setActionCommand("Cancel");
				cancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						bins.destroy();
						running.setCursor(null);
						running.dispose();
					}
				});
				biosim.getExitButton().setActionCommand("Exit program");
				biosim.getExitButton().addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						bins.destroy();
						running.setCursor(null);
						running.dispose();
					}
				});
				try {
					String output = "";
					InputStream reb = bins.getInputStream();
					InputStreamReader isr = new InputStreamReader(reb);
					BufferedReader br = new BufferedReader(isr);
					int count = 0;
					while ((output = br.readLine()) != null) {
						if (output.matches("\\d+/\\d+")) {
							// log.addText(output);
							count += 500;
							progress.setValue(count);
						} else if (output.contains("ERROR")) {
							fail = true;
						}
						out.write(output);
						out.write("\n");
					}
					br.close();
					isr.close();
					reb.close();
					if (!execute || fail) {
						out.close();
					}
					viewLog.setEnabled(true);
				} catch (Exception e) {
				}
				int exitValue = bins.waitFor();
				if (exitValue == 143) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"Learning was" + " canceled by the user.",
							"Canceled Learning", JOptionPane.ERROR_MESSAGE);
				}
				FileOutputStream outBins = new FileOutputStream(new File(directory + separator + binFile));
				FileInputStream inBins = new FileInputStream(new File(directory	+ separator + newBinFile));
				int read = inBins.read();
				while (read != -1) {
					outBins.write(read);
					read = inBins.read();
				}
				inBins.close();
				outBins.close();
				if (!execute) {
					levels();
				}
*/
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
				// String command = "data2lhpn.py -b" + binFile + " -l" +
				// lhpnFile;
			//	String command = "dataToLHPN()";
			//	log.addText("Running:\n" + command  + "\n");
				dataToLHPN(running);
				// log.addText("Executing:\n" + command + " " + directory +
				// "\n");
				// File work = new File(directory);
				//				
				// final Process run = Runtime.getRuntime().exec(command,
				// null,work);
				// cancel.setActionCommand("Cancel");
				// cancel.addActionListener(new ActionListener() {
				// public void actionPerformed(ActionEvent e) {
				// run.destroy();
				// running.setCursor(null);
				// running.dispose();
				// }
				// }
				// );
				// biosim.getExitButton().setActionCommand("Exit program");
				// biosim.getExitButton().addActionListener(new ActionListener()
				// {
				// public void actionPerformed(ActionEvent e) {
				// run.destroy();
				// running.setCursor(null);
				// running.dispose();
				// }
				// }
				// );
				// try {
				// String output = "";
				// InputStream reb = run.getInputStream();
				// InputStreamReader isr = new InputStreamReader(reb);
				// BufferedReader br = new BufferedReader(isr);
				// while ((output = br.readLine()) != null) {
				// if (output.contains("ERROR")) {
				// fail = true;
				// }
				// out.write(output);
				// out.write("\n");
				// }
				// br.close();
				// isr.close();
				// reb.close();
				// out.close();
				// viewLog.setEnabled(true);
				// }
				// catch (Exception e) {
				//					  
				// }
				// int exitValue = run.waitFor();
				// if (exitValue == 143) {
				// JOptionPane.showMessageDialog(biosim.frame(), "Learning was"
				// + "canceled by the user.", "Canceled Learning",
				// JOptionPane.ERROR_MESSAGE);
				// }
				viewLog.setEnabled(true);
				if (new File(directory + separator + lhpnFile).exists()) {
					viewVHDL.setEnabled(true); 		// SB
					viewVerilog.setEnabled(true); 	// SB
					viewLhpn.setEnabled(true); 		// SB
					viewCoverage.setEnabled(true); 	// SB
					saveLhpn.setEnabled(true); 		// SB
					//viewLearnComplete();			// SB
					JFrame learnComplete = new JFrame();
					JOptionPane.showMessageDialog(learnComplete,
						    "Learning Completed Successfully.",
						    "LEMA",
						    JOptionPane.PLAIN_MESSAGE);

					//viewLhpn();
					biosim.updateMenu(true,true);
				} else {
					viewVHDL.setEnabled(false); 	// SB
					viewVerilog.setEnabled(false); 	// SB
					viewLhpn.setEnabled(false); 	// SB
					viewCoverage.setEnabled(false); // SB
					saveLhpn.setEnabled(false); 	// SB
					fail = true;
					biosim.updateMenu(true,false);
				}

			}
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
			if (((JComboBox) variables.get(i).get(2)).isFocusOwner()) {  // changed 1 to 2 SB
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
		divisionsL = new ArrayList<ArrayList<Double>>();	// SB
		thresholds = new HashMap<String, ArrayList<Double>>(); // SB
		reqdVarsL = new ArrayList<Variable>();				// SB
		LHPNFile lhpn = new LHPNFile();
		lhpn.load(learnFile);
		HashMap<String, Properties> variablesMap = lhpn.getVariables();
		for (String s : variablesMap.keySet()) {
			variablesList.add(s);
			reqdVarsL.add(new Variable(s));					// SB
			divisionsL.add(new ArrayList<Double>());		// SB
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
					boolean varFound = false;
					int varNum = j;
					for (int i = 0; i < reqdVarsL.size() ; i++){
						if (st1.equalsIgnoreCase(reqdVarsL.get(i).getName())){
							varFound = true;
							varNum = i;
							break;
						}
					}
					if (!varFound){
						continue;
					}
					String s = load.getProperty("learn.bins" + st1);
					String[] savedBins = s.split("\\s");
					//divisionsL.add(new ArrayList<Double>());
					//variablesList.add(savedBins[0]);
					//	((JComboBox)(((JPanel)variablesPanel.getComponent(j+1)).getComponent(2))).setSelectedItem(savedBins[1]);
					for (int i = 2; i < savedBins.length ; i++){
						//		((JTextField)(((JPanel)variablesPanel.getComponent(j+1)).getComponent(i+1))).setText(savedBins[i]);
						if (varNum < variablesMap.size()) {	// chk for varNum or j ????
							divisionsL.get(varNum).add(Double.parseDouble(savedBins[i]));
							thresholds.get(st1).add(Double.parseDouble(savedBins[i]));
						}
					}
					j++;
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
		Collections.sort(divisionsL, new Comparator<ArrayList<Double>>(){
			public int compare(ArrayList<Double> a, ArrayList<Double> b){
				return (reqdVarsL.get(divisionsL.indexOf(a)).compareTo(reqdVarsL.get(divisionsL.indexOf(b))));
			}
		});
		Collections.sort(reqdVarsL);
		/*Collections.sort(divisionsL, new Comparator<ArrayList<Double>>(){
			public int compare(ArrayList<Double> a, ArrayList<Double> b){
				if ()
			}
		});*/
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
	 * Rev. 1 - Scott Little (data2lhpn.py) 
	 * Rev. 2 - Satish Batchu ( dataToLHPN() )
	 */

	public void dataToLHPN(JFrame running) {
		try {
			/* Initializations being done in resetAll method added on Aug 12,2009. These 
			 * initializations ensure that place,transition numbers start from 0 
			 * everytime we click play button on LEMA though compiled only once. 
			 * Init values and rates being cleared for the same reason. 
			 */
			resetAll();
			lowerLimit = new Double[reqdVarsL.size()];
			upperLimit = new Double[reqdVarsL.size()];
			/* end Initializations */
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
		//	divisionsL = parseBinFile();
			lowerLimit[0] = -800.0;  // for integrator
			upperLimit[0] = 800.0;  // for integrator
			
			String failProp = "";
			String enFailAnd = "";
			String enFail = "";
			// Add logic to deal with failprop and related places/transitions
			g = new LHPNFile(); // The generated lhpn is stored in this object
			placeInfo = new HashMap<String, Properties>();
			transitionInfo = new HashMap<String, Properties>();
			cvgInfo = new HashMap<String, Properties>();
			transientNetPlaces = new HashMap<String, Properties>();
			transientNetTransitions = new HashMap<String, Properties>();
			ratePlaces = new ArrayList<String>();
			dmvcInputPlaces = new ArrayList<String>();
			propPlaces = new ArrayList<String>();
			/*if (new File(directory + separator + "learn" + ".prop").exists()){
				BufferedReader prop = new BufferedReader(new FileReader(directory + separator + "learn" + ".prop"));
				failProp = prop.readLine();
				failProp = failProp.replace("\n", "");
			*/	
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
				g.addControlFlow("p" + placeInfo.get("failProp").getProperty("placeNum"), "t" + transitionInfo.get("failProp").getProperty("transitionNum")); 
				numTransitions++;
				enFailAnd = "&~fail";
				enFail = "~fail";
			}
			epsilon = Double.parseDouble(epsilonG.getText().trim());
			rateSampling = Integer.parseInt(rateSamplingG.getText().trim());
			pathLength = Integer.parseInt(pathLengthG.getText().trim());
			percent = Double.parseDouble(percentG.getText().trim());
			runLength = Integer.parseInt(runLengthG.getText().trim());
			runTime = Double.parseDouble(runTimeG.getText().trim());
			absoluteTime = absTimeG.isSelected();
			while (new File(directory + separator + "run-" + i + ".tsd").exists()) {
				Properties cProp = new Properties();
				cvgInfo.put(String.valueOf(i), cProp);
				cProp.setProperty("places", String.valueOf(0));
				cProp.setProperty("transitions", String.valueOf(0));
				cProp.setProperty("rates", String.valueOf(0));
				cProp.setProperty("delays", String.valueOf(0));
				tsd = new TSDParser(directory + separator + "run-" + i + ".tsd", biosim,false);
				data = tsd.getData();
				genBinsRates(divisionsL); // changes made here.. data being used was global before.
				//genBinsRates("run-" + i + ".tsd", divisionsL);
				detectDMV();
				updateGraph(bins, rates, cProp,running);
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
			/*out.write("\nPlaces are : ");
			for (String st3 : g.getPlaceList()) {
				out.write(st3 + " ");
			}
			out.write("\n");
			for (String t : g.getTransitionList()) {
				for (String p : g.getPreset(t)) {
					out.write(p + " " + t + "\n");
				}
				for (String p : g.getPostset(t)) {
					out.write(t + " " + p + "\n");
				}
			}
			*/
			Properties initCond = new Properties();
			for (Variable v : reqdVarsL) {
				if (v.isDmvc()) {
					g.addInteger(v.getName(), v.getInitValue());
				} else {
					initCond.put("value", v.getInitValue());
					initCond.put("rate", v.getInitRate());
					g.addVar(v.getName(), initCond);
				}
			}
			g.addOutput("fail", "false");
			ArrayList<ArrayList<Double>> scaleDiv;
			
			// temporary
		/*	Pattern pat = Pattern.compile("-*[0-9]+\\.*[0-9]*");
	        Matcher m = pat.matcher(failProp);
	        while(m.find()){
	        	failProp = m.replaceFirst(String.valueOf(Double.parseDouble(m.group())*100.0));
	        }
	        System.out.println(failProp);
			for (String t : g.getTransitionList()) {
				if ((g.getPreset(t) != null) && (placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("PROP"))){
					g.addEnabling(t, failProp);	
				}
			} */
			// end temporary
			
			scaleDiv = normalize();
			initCond = new Properties(); 
			for (Variable v : reqdVarsL) {	// Updating with scaled initial values & rates
				if (v.isDmvc()) {
					g.changeIntegerInitCond(v.getName(), v.getInitValue());
				} else {
					initCond.put("value", v.getInitValue());
					initCond.put("rate", v.getInitRate());
					g.changeVarInitCond(v.getName(), initCond);
				}
			}
			String[] transitionList = g.getTransitionList();
			transEnablingsVHDL = new String[transitionList.length];
			transDelayAssignVHDL = new String[transitionList.length];
			transIntAssignVHDL = new String[transitionList.length][reqdVarsL.size()];
			transEnablingsVAMS = new String[transitionList.length];
			transIntAssignVAMS = new Double[transitionList.length][reqdVarsL.size()];
			transDelayAssignVAMS = new Integer[transitionList.length];
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
							transEnablingsVHDL[transNum] = "";
							transEnablingsVAMS[transNum] = "";
							String[] binIncoming = getPlaceInfoIndex(g.getPreset(t)[0]).split("");
							String[] binOutgoing = getPlaceInfoIndex(g.getPostset(t)[0]).split("");
							for (int k : diffL) {
								if (!((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput()))) {
									// the above condition means that if the bin change is on a non-input dmv variable, there won't be any enabling condition
									if (Integer.parseInt(binIncoming[k + 1]) < Integer.parseInt(binOutgoing[k + 1])) {
										//	double val = divisionsL.get(k).get(Integer.parseInt(binIncoming[k + 1])).doubleValue();
										double val = scaleDiv.get(k).get(Integer.parseInt(binIncoming[k + 1])).doubleValue();
										condStr += "(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
										transEnablingsVHDL[transNum] += reqdVarsL.get(k).getName() + "'above(" + (int) Math.floor(val)+".0)";	
										transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/varScaleFactor +"),+1))";	// += temporary
									} else {
										double val = scaleDiv.get(k).get(Integer.parseInt(binOutgoing[k + 1])).doubleValue();
										condStr += "~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.ceil(val) + ")";
										transEnablingsVHDL[transNum] += "not " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.ceil(val)+".0)";
										transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/varScaleFactor +"),-1))";	// +=; temporary
									}
									if (diffL.get(diffL.size() - 1) != k) {
										condStr += "&";
										transEnablingsVHDL[transNum] += " and ";
										//COME BACK???	temporary			transEnablingsVAMS[transNum] += 
									}
								}
								// Enablings Till above.. Below one is dmvc delay,assignment. Whenever a transition's preset and postset places differ in dmvc vars, then this transition gets the assignment of the dmvc value in the postset place and delay assignment of the preset place's duration range. This has to be changed after taking the causal relation input
								if ((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput())) { // require few more changes here.should check for those variables that are constant over these regions and make them as causal????? thesis
									String pPrev = g.getPreset(t)[0];
									String nextPlace = g.getPostset(t)[0];
									//if (!isTransientTransition(t)){
										g.changeDelay(t, "[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMax"))) + "]");
										g.addIntAssign(t,reqdVarsL.get(k).getName(),"[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ","+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + "]");
										int dmvTnum =  Integer.parseInt(t.split("t")[1]);
										transIntAssignVHDL[dmvTnum][k] = reqdVarsL.get(k).getName() +" => span(" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ".0,"+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + ".0)";
										transDelayAssignVHDL[dmvTnum] = "delay(" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMax"))) + ")";
										transIntAssignVAMS[dmvTnum][k] = ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*varScaleFactor);
										transDelayAssignVAMS[dmvTnum] =  (int)(((Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMin"))) + Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMax"))))*Math.pow(10, 12))/(2.0*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
									/*}
									else{
										g.changeDelay(t, "[" + (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))) + "]");
										g.addIntAssign(t,reqdVarsL.get(k).getName(),"[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ","+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + "]");
										int dmvTnum =  Integer.parseInt(t.split("t")[1]);
										transIntAssignVHDL[dmvTnum][k] = reqdVarsL.get(k).getName() +" => span(" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ".0,"+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + ".0)";
										transDelayAssignVHDL[dmvTnum] = "delay(" + (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))) + ")";
										transIntAssignVAMS[dmvTnum][k] = ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*varScaleFactor);
										transDelayAssignVAMS[dmvTnum] =  (int)(((Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))))*Math.pow(10, 12))/(2.0*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
									}*/
								}
							}
							if (diffL.size() > 1){
								transEnablingsVHDL[transNum] = "(" + transEnablingsVHDL[transNum] + ")";
							}
							if (condStr.equalsIgnoreCase("")){
								condStr = enFail;
							}
							else{
								condStr += enFailAnd;
							}
							//transEnablingsVHDL[transNum] += enFailAnd;
							g.addEnabling(t, condStr);
						}
						if ((placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("DMVC"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("DMVC"))) {
							// Dealing with graphs obtained from DMVC INPUT variables
							// NO ENABLINGS for these transitions
							String nextPlace = g.getPostset(t)[0];
							String prevPlace = g.getPreset(t)[0];
							// A transition has delay from it's preset place & 
							// assignment from it's postset place
							g.changeDelay(t, "[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(prevPlace)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(prevPlace)).getProperty("dMax"))) + "]");
							g.addIntAssign(t, placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty("DMVCVariable"), "[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty("DMVCValue")))	+ "," + (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty("DMVCValue"))) + "]");
							g.addEnabling(t, enFail);
						}
					}
					else{
						if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("DMVC"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("DMVC"))){		// transient dmv transition
							String prevPlace = g.getPreset(t)[0];
							String nextPlace = g.getPostset(t)[0];
							// delay from preset; assgnmt from postset
							g.changeDelay(t, "[" + (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(prevPlace)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(prevPlace)).getProperty("dMax"))) + "]");
							g.addIntAssign(t, placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty("DMVCVariable"), "[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty("DMVCValue")))	+ "," + (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty("DMVCValue"))) + "]");
							g.addEnabling(t, enFail);
						}	
						if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
							ArrayList<Integer> diffL = diff(getTransientNetPlaceIndex(g.getPreset(t)[0]), getPlaceInfoIndex(g.getPostset(t)[0]));
							String condStr = "";
							transEnablingsVHDL[transNum] = "";
							transEnablingsVAMS[transNum] = "";
							String[] binIncoming = getTransientNetPlaceIndex(g.getPreset(t)[0]).split("");
							String[] binOutgoing = getPlaceInfoIndex(g.getPostset(t)[0]).split("");
							for (int k : diffL) {
								if (!((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput()))) {
									if (Integer.parseInt(binIncoming[k + 1]) < Integer.parseInt(binOutgoing[k + 1])) {
										//	double val = divisionsL.get(k).get(Integer.parseInt(binIncoming[k + 1])).doubleValue();
										double val = scaleDiv.get(k).get(Integer.parseInt(binIncoming[k + 1])).doubleValue();
										condStr += "(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
										transEnablingsVHDL[transNum] += reqdVarsL.get(k).getName() + "'above(" + (int) Math.floor(val)+".0)";	
										transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/varScaleFactor +"),+1))";	// += temporary
									} else {
										double val = scaleDiv.get(k).get(Integer.parseInt(binOutgoing[k + 1])).doubleValue();
										condStr += "~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.ceil(val) + ")";
										transEnablingsVHDL[transNum] += "not " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.ceil(val)+".0)";
										transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/varScaleFactor +"),-1))";	// +=; temporary
									}
									if (diffL.get(diffL.size() - 1) != k) {
										condStr += "&";
										transEnablingsVHDL[transNum] += " and ";
										//COME BACK???	temporary			transEnablingsVAMS[transNum] += 
									}
								}
								if ((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput())) { // require few more changes here.should check for those variables that are constant over these regions and make them as causal????? thesis
									String pPrev = g.getPreset(t)[0];
									String nextPlace = g.getPostset(t)[0];
										g.changeDelay(t, "[" + (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))) + "]");
										g.addIntAssign(t,reqdVarsL.get(k).getName(),"[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ","+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + "]");
										int dmvTnum =  Integer.parseInt(t.split("t")[1]);
										transIntAssignVHDL[dmvTnum][k] = reqdVarsL.get(k).getName() +" => span(" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ".0,"+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + ".0)";
										transDelayAssignVHDL[dmvTnum] = "delay(" + (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))) + ")";
										transIntAssignVAMS[dmvTnum][k] = ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*varScaleFactor);
										transDelayAssignVAMS[dmvTnum] =  (int)(((Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))))*Math.pow(10, 12))/(2.0*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
								}
							}
							if (diffL.size() > 1){
								transEnablingsVHDL[transNum] = "(" + transEnablingsVHDL[transNum] + ")";
							}
							if (condStr.equalsIgnoreCase("")){
								condStr = enFail;
							}
							else{
								condStr += enFailAnd;
							}
							//transEnablingsVHDL[transNum] += enFailAnd;
							g.addEnabling(t, condStr);
						}
					}
				}   
				if ((g.getPreset(t) != null) && (!isTransientTransition(t)) && (placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("PROP"))){
					g.addEnabling(t, failProp);
					g.addBoolAssign(t, "fail", "true"); // fail would be the variable name
					//g.addProperty(failProp);
				}
				// if ((t.getIncomingP() != null) &&
				// (t.getIncomingP().isPropP())){
				// t.setEnabling(t.getIncomingP().getProperty());
				// }
				// if ((t.getIncomingP() != null) && (t.getOutgoingP() != null)
				// && (t.getIncomingP().isDmvcP()) &&
				// (t.getOutgoingP().isDmvcP())){
				// out.write("\n<t"+t.getTransitionNum() + "= " + "{" +
				// t.getMinDelay() + "," + t.getMaxDelay() + "}" + "["+
				// reqdVarsL.get(t.getOutgoingP().getDmvcVar()).getName()+ ":= "
				// + t.getOutgoingP().getDmvcVal() + "]>");
				// }
			}
			//	out.write("\n#@.rate_assignments {");
			//	if (placeRates) {
			for (String st1 : g.getPlaceList()) {
				if (!isTransientPlace(st1)){
					String p = getPlaceInfoIndex(st1);
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						if (g.getPreset(st1).length != 0){
							for (String t : g.getPreset(st1)) {
								for (int k = 0; k < reqdVarsL.size(); k++) {
									if (!reqdVarsL.get(k).isDmvc()) {
										//	out.write("<" + t	+ "=["	+ reqdVarsL.get(k).getName() + ":=["	+ getMinRate(p, reqdVarsL.get(k).getName())	+ "," + getMaxRate(p, reqdVarsL.get(k).getName()) + "]]>");
										g.addRateAssign(t, reqdVarsL.get(k).getName(), "["	+ getMinRate(p, reqdVarsL.get(k).getName())	+ "," + getMaxRate(p, reqdVarsL.get(k).getName()) + "]");
									}
								}
							}
						}
					}//	out.write("\n");
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
							g.addRateAssign("t" + transientNetTransitions.get(st1).getProperty("transitionNum"), reqdVarsL.get(k).getName(), "["	+ getMinRate(p, reqdVarsL.get(k).getName())	+ "," + getMaxRate(p, reqdVarsL.get(k).getName()) + "]");
						}
					}
				}
			}
		
			/*
			 * if (g.isFailProp()){ out.write("#@.boolean_assignments {\n"); for
			 * (String st:sortedPlaces){ Place p = g.get_valP(st); if
			 * (p.isPropP()){ for (Transition t:p.getOutgoing()){ out.write("<t" +
			 * t.getTransitionNum() + "=[fail:=TRUE]>"); } } } out.write("\n"); }
			 */out.close();
	//		addMetaBins();
	//		addMetaBinTransitions();
			g.save(directory + separator + lhpnFile);
			writeVHDLAMSFile(lhpnFile.replace(".lpn",".vhd"));
			writeVerilogAMSFile(lhpnFile.replace(".lpn",".vams"));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("LPN file couldn't be created/written ");
			JOptionPane.showMessageDialog(biosim.frame(),
					"LPN file couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch (ArrayIndexOutOfBoundsException e1) {	// comes from initMark = -1 of updateGraph()
			e1.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to calculate rates.\nWindow size or pathlength must be reduced.\nLearning unsuccessful.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
			try {
				out.write("ERROR! Unable to calculate rates.\nIf Window size = -1, pathlength must be reduced;\nElse, reduce windowsize\nLearning unsuccessful.");
				out.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			running.setCursor(null);
			running.dispose();
		}
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
		dmvcCnt = 0;
		numPlaces = 0;
		numTransitions = 0;
		delayScaleFactor = 1.0;
		varScaleFactor = 1.0;
		for (Variable v: reqdVarsL){
			v.reset();
		}
	}

	public void genBinsRates(ArrayList<ArrayList<Double>> divisionsL) { // genBins
//	public void genBinsRates(String datFile,ArrayList<ArrayList<Double>> divisionsL) { // genBins
//		TSDParser tsd = new TSDParser(directory + separator + datFile, biosim,false);
		// genBins
//		data = tsd.getData();
		reqdVarIndices = new ArrayList<Integer>();
		bins = new int[reqdVarsL.size()][data.get(0).size()];
		for (int i = 0; i < reqdVarsL.size(); i++) {
			// System.out.println("Divisions " + divisionsL.get(i));
			for (int j = 1; j < varNames.size(); j++) {
				if (reqdVarsL.get(i).getName().equalsIgnoreCase(varNames.get(j))) {
					// System.out.println(reqdVarsL.get(i) + " matched "+
					// varNames.get(j) + " i = " + i + " j = " + j);
					reqdVarIndices.add(j);
					for (int k = 0; k < data.get(j).size(); k++) {
						// System.out.print(data.get(j).get(k) + " ");
						for (int l = 0; l < divisionsL.get(i).size(); l++) {
							if (data.get(j).get(k) <= divisionsL.get(i).get(l)) {
								bins[i][k] = l;
								break;
							} else {
								bins[i][k] = l + 1; // indices of bins not same
								// as that of the variable.
								// i here. not j; if j
								// wanted, then size of bins
								// array should be varNames
								// not reqdVars
							}
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
		duration = new Double[data.get(0).size()];
		int mark, k; // indices of rates not same as that of the variable. if
		// wanted, then size of rates array should be varNames
		// not reqdVars
		if (placeRates) {
			if (rateSampling == -1) { // replacing inf with -1 since int
				mark = 0;
				for (int i = 0; i < data.get(0).size(); i++) {
					if (i < mark) {
						continue;
					}
					while ((mark < data.get(0).size()) && (compareBins(i, mark))) {
						mark++;
					}
					if ((data.get(0).get(mark - 1) != data.get(0).get(i)) && ((mark - i) >=  pathLength)) { 
						for (int j = 0; j < reqdVarsL.size(); j++) {
							k = reqdVarIndices.get(j);
							rates[j][i] = ((data.get(k).get(mark - 1) - data.get(k).get(i)) / (data.get(0).get(mark - 1) - data.get(0).get(i)));
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
				if (!prevFail){ // for the last genuine rate-calculating region of the trace; this may not be required if the trace is incomplete.trace data may not necessarily end at a region endpt
					duration[binStartPoint] = data.get(0).get(data.get(0).size()-1)	- data.get(0).get(binStartPoint);
				}
			}
		} 
		/*
		 * ADD LATER: duration[i] SHOULD BE ADDED TO THE NEXT 2 IF/ELSE
		 * BRANCHES(Transition based rate calc) ALSO
		 */
		else { // Transition based rate calculation
			if (rateSampling == -1) { // replacing inf with -1 since int
				for (int j = 0; j < reqdVarsL.size(); j++) {
					mark = 0;
					k = reqdVarIndices.get(j);
					for (int i = 0; i < data.get(0).size(); i++) {
						if (i < mark) {
							continue;
						}
						while ((mark < data.get(0).size())
								&& (bins[k][i] == bins[k][mark])) {
							mark++;
						}
						if ((data.get(0).get(mark - 1) != data.get(0).get(i))) {
							rates[j][i] = ((data.get(k).get(mark - 1) - data.get(k).get(i)) / (data.get(0).get(mark - 1) - data.get(0).get(i)));
						}
					}
				}
			} else {
				boolean calcRate;
				for (int i = 0; i < (data.get(0).size() - rateSampling); i++) {
					for (int j = 0; j < reqdVarsL.size(); j++) {
						calcRate = true;
						k = reqdVarIndices.get(j);
						for (int l = 0; l < rateSampling; l++) {
							if (bins[k][i] != bins[k][i + l]) {
								calcRate = false;
								break;
							}
						}
						if (calcRate && (data.get(0).get(i + rateSampling) != data.get(0).get(i))) {
							rates[j][i] = ((data.get(k).get(i + rateSampling) - data.get(k).get(i)) / (data.get(0).get(i + rateSampling) - data.get(0).get(i)));
						}
					}
				}
			}
		}
		/*
		try {
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

	public void updateRateInfo(int[][] bins, Double[][] rates, Properties cvgProp) {
		String prevPlaceKey = ""; // "" or " " ; rechk
		String key = "";
		// boolean addNewPlace;
		// ArrayList<String> ratePlaces = new ArrayList<String>(); // ratePlaces can include non-input dmv places.
		// boolean newRate = false;
		Properties p0, p1 = null;
		for (int i = 0; i < (data.get(0).size() - 1); i++) {
			if (rates[0][i] != null) { // check if indices are ok. 0???? or 1???
				prevPlaceKey = key;
				key = "";
				for (int j = 0; j < reqdVarsL.size(); j++) {
					key += bins[j][i];
				}
				if (placeInfo.containsKey(key)) {
					p0 = placeInfo.get(key);
				} 
				else if ((transientNetPlaces.containsKey(key)) && (ratePlaces.size() == 1)){
					p0 = transientNetPlaces.get(key);
				}
				else {
					p0 = new Properties();
					if (ratePlaces.size() == 0){
						transientNetPlaces.put(key, p0);
						g.addPlace("p" + numPlaces, true);
					}
					else{
						placeInfo.put(key, p0);
						g.addPlace("p" + numPlaces, false);
					}
					p0.setProperty("placeNum", numPlaces.toString());
					p0.setProperty("type", "RATE");
					p0.setProperty("initiallyMarked", "false");
					p0.setProperty("metaType","false");  // REMOVE LATER?????
					ratePlaces.add("p" + numPlaces);
					numPlaces++;
					cvgProp.setProperty("places", String.valueOf(Integer.parseInt(cvgProp.getProperty("places"))+1));
				}
				for (int j = 0; j < reqdVarsL.size(); j++) {
					// rechk if (reqdVarsL.get(j).isDmvc() && reqdVarsL.get(j).isInput()) {
					// continue;
					// }
					if (reqdVarsL.get(j).isDmvc()) { // && !reqdVarsL.get(j).isInput()){
						for (int k = 0; k < reqdVarsL.get(j).getRuns().getAvgVals().length; k++) {
							if ((reqdVarsL.get(j).getRuns().getStartPoint(k) <= i) && (reqdVarsL.get(j).getRuns().getEndPoint(k) >= i)) {
								addValue(p0,reqdVarsL.get(j).getName(),reqdVarsL.get(j).getRuns().getAvgVals()[k]);// data.get(reqdVarIndices.get(j)).get(i));
								break;
							}
							if (reqdVarsL.get(j).getRuns().getStartPoint(k) >= i) {
								addValue(p0,reqdVarsL.get(j).getName(),reqdVarsL.get(j).getRuns().getAvgVals()[k]);// data.get(reqdVarIndices.get(j)).get(i));
								break;
							}
							// WRONG addValue(p0, reqdVarsL.get(j).getName(), data.get(reqdVarIndices.get(j)).get(i));
						}
						continue;
					}
					addRate(p0, reqdVarsL.get(j).getName(), rates[j][i]);
					// newR, oldR, dmvc etc. left
				}
				boolean transientNet = false;
				if (!prevPlaceKey.equalsIgnoreCase(key)) {
					if (transitionInfo.containsKey(prevPlaceKey + key)) { // instead of tuple
						p1 = transitionInfo.get(prevPlaceKey + key);
					} else if (prevPlaceKey != "") {
						// transition = new Transition(reqdVarsL.size(),place,prevPlace);
						p1 = new Properties();
						p1.setProperty("transitionNum", numTransitions.toString());
						if (ratePlaces.size() == 2){
							transientNetTransitions.put(prevPlaceKey + key, p1);
							g.addTransition("t" + numTransitions); // prevTranKey+key);
							g.addControlFlow("p" + transientNetPlaces.get(prevPlaceKey).getProperty("placeNum"), "t" + transientNetTransitions.get(prevPlaceKey + key).getProperty("transitionNum")); 
							g.addControlFlow("t" + transientNetTransitions.get(prevPlaceKey + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
							transientNet = true;
						}
						else{
							transitionInfo.put(prevPlaceKey + key, p1);
							g.addTransition("t" + numTransitions); // prevTranKey+key);
							g.addControlFlow("p" + placeInfo.get(prevPlaceKey).getProperty("placeNum"), "t" + transitionInfo.get(prevPlaceKey + key).getProperty("transitionNum")); 
							g.addControlFlow("t" + transitionInfo.get(prevPlaceKey + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
						}
						numTransitions++;
						cvgProp.setProperty("transitions", String.valueOf(Integer.parseInt(cvgProp.getProperty("transitions"))+1));
						// transition.setCore(true);
					}
				}
				if (duration[i] != null){
					addDuration(p0, duration[i]);
				}
				//else if (duration[i] != null && transientNet){
				//	addTransientDuration(p0, duration[i]);
				//}
				if (p1 != null) {
					for (int j = 0; j < reqdVarsL.size(); j++) {
						if (reqdVarsL.get(j).isDmvc() && reqdVarsL.get(j).isInput()) {
							continue;
						}
						if (reqdVarsL.get(j).isDmvc()) {
							continue;
						}
						addRate(p1, reqdVarsL.get(j).getName(), rates[j][i]);
					}
				}
			}
		}
	}

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
										/*	if (j == 0) { // adding the place corresponding to the first dmv run to initial marking. 
											placeInfo.get(k).setProperty("initiallyMarked", "true");
											g.changeInitialMarking("p"	+ placeInfo.get(k).getProperty("placeNum"),true);
										} */
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
						/* TEMPORARY FIX
						Double minTime = getMinDmvcTime(p2);
						if ((d > minTime*Math.pow(10.0, 4.0))){ // && (getMinDmvcTime(p2) == getMaxDmvcTime(p2))){
							deleteInvalidDmvcTime(p2, getMinDmvcTime(p2));	// updates dmin,dmax too
						}
						END TEMPORARY FIX	*/
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
									g.addControlFlow("p" + transientNetPlaces.get(prevPlace).getProperty("placeNum"), "t" + transientNetTransitions.get(prevPlace + currPlace).getProperty("transitionNum")); 
									g.addControlFlow("t" + transientNetTransitions.get(prevPlace + currPlace).getProperty("transitionNum"), "p" + placeInfo.get(currPlace).getProperty("placeNum"));
								//	transientNet = true;
								}
								else{
									transitionInfo.put(prevPlace + currPlace, p3);
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addControlFlow("p"+ placeInfo.get(prevPlace).getProperty("placeNum"), "t"+ transitionInfo.get(prevPlace + currPlace).getProperty("transitionNum")); 
									g.addControlFlow("t"+ transitionInfo.get(prevPlace+ currPlace).getProperty("transitionNum"),"p"+ placeInfo.get(currPlace).getProperty("placeNum"));
								}
								numTransitions++;
								cvgProp.setProperty("transitions", String.valueOf(Integer.parseInt(cvgProp.getProperty("transitions"))+1));
							}
						}
						/*if (!transientNet){	// assuming postset duration
							addDuration(p2, d);
						}
						else {
							addTransientDuration(p2, d);
						}*/
						
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

	public void updateGraph(int[][] bins, Double[][] rates, Properties cvgProp,JFrame running) {
		updateRateInfo(bins, rates, cvgProp);
		updateTimeInfo(bins,cvgProp);
		int initMark = -1;
		int k;
		String key;
		for (int i = 0; i < reqdVarsL.size(); i++) {
			for (int j = 0; j < data.get(0).size(); j++) {
				if (rates[i][j] != null) {
					k = reqdVarIndices.get(i);
					// addInitValues(data.get(k).get(j), i); // k or i think ??
					// addInitRates(rates[i][j], i);// k or i think??
					reqdVarsL.get(i).addInitValues(data.get(k).get(j)); // Do the same for initvals too??
					//reqdVarsL.get(i).addInitRates(rates[i][j]);	// this just adds the first rate; not the rates of entire 1st region.
					initMark = j;
					break;
				}
			}
			key = "";
			for (int l = 0; l < reqdVarsL.size(); l++) {
				key = key + bins[l][initMark];
			}
			if (!reqdVarsL.get(i).isDmvc()){
				reqdVarsL.get(i).addInitRates((double)getMinRate(key, reqdVarsL.get(i).getName()));
				reqdVarsL.get(i).addInitRates((double)getMaxRate(key, reqdVarsL.get(i).getName()));
			}
/*
 			if (placeInfo.get(key).getProperty("initiallyMarked").equalsIgnoreCase("false")) {
 
				placeInfo.get(key).setProperty("initiallyMarked", "true");
				g.changeInitialMarking("p" + placeInfo.get(key).getProperty("placeNum"), true);
			}
*/
		}
	}

	public void detectDMV() {
		int startPoint, endPoint, mark, numPoints;
		double absTime;
		for (int i = 0; i < reqdVarsL.size(); i++) {
			absTime = 0;
			mark = 0;
			DMVCrun runs = reqdVarsL.get(i).getRuns();
			runs.clearAll(); // flush all the runs from previous dat file.
			int lastRunPointsWithoutTransition = 0;
			Double lastRunTimeWithoutTransition = 0.0;
			for (int j = 0; j <= data.get(0).size(); j++) {
				if (j < mark) // not reqd??
					continue;
				if (((j+1) < data.get(reqdVarIndices.get(i)).size()) && 
						Math.abs(data.get(reqdVarIndices.get(i)).get(j) - data.get(reqdVarIndices.get(i)).get(j + 1)) <= epsilon) {
					startPoint = j;
					runs.addValue(data.get(reqdVarIndices.get(i)).get(j)); // chk carefully reqdVarIndices.get(i)
					while (((j + 1) < data.get(0).size()) && (Math.abs(data.get(reqdVarIndices.get(i)).get(startPoint) - data.get(reqdVarIndices.get(i)).get(j + 1)) <= epsilon)) {
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
						} else {
							runs.removeValue();
						}
					}
					mark = endPoint;
				}
			}
			try {
				if (!absoluteTime) {
					numPoints = runs.getNumPoints();
					if (((numPoints + lastRunPointsWithoutTransition)/ (double) data.get(0).size()) < percent) {
						runs.clearAll();
						reqdVarsL.get(i).setDmvc(false);
						out.write(reqdVarsL.get(i).getName()
								+ " is not a dmvc \n");
					} else {
						reqdVarsL.get(i).setDmvc(true);
						out.write(reqdVarsL.get(i).getName() + " is  a dmvc \n");
					}
				} else {
					if (((absTime + lastRunTimeWithoutTransition)/ (data.get(0).get(data.get(0).size() - 1) - data
							.get(0).get(0))) < percent) {
						runs.clearAll();
						reqdVarsL.get(i).setDmvc(false);
						out.write(reqdVarsL.get(i).getName()
								+ " is not a dmvc \n");
					} else {
						reqdVarsL.get(i).setDmvc(true);
						out.write(reqdVarsL.get(i).getName()
										+ " is  a dmvc \n");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(biosim.frame(),
						"Log file couldn't be opened for writing rates and bins.",
						"ERROR!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public double calcDelay(int i, int j) {
		return (data.get(0).get(j) - data.get(0).get(i));
		// should add some next run logic later..?
	}

	public void addValue(Properties p, String name, Double v) { // latest
		// change..
		// above one
		// working fine
		// if this
		// doesn't
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

	public void addRate(Properties p, String name, Double r) { // latest
		// change.. above one working fine if this doesn't
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

	public void addDmvcTime(Properties p, String name, Double t) {
		if (p.getProperty("dmvcTime_" + name) == null) {
			p.setProperty("dmvcTime_" + name, t.toString());
		} else {
			// Double d = Double.parseDouble(p.getProperty("dmvcTime_" + name));
			// d = d + t;
			// p.setProperty("dmvcTime_" + name, d.toString());
			p.setProperty("dmvcTime_" + name, p.getProperty("dmvcTime_" + name)
					+ " " + t.toString());
		}
	}

	public void deleteInvalidDmvcTime(Properties p, Double t) {
		String[] times = null;
		String name = p.getProperty("DMVCVariable");
		String s = p.getProperty("dmvcTime_" + name);
		String newS = null;
		Double dMin = null, dMax = null;
		if (s != null) {
			times = s.split(" ");
			for (int i = 0; i < times.length; i++) {
				if (Double.parseDouble(times[i]) != t) {
					if (newS == null){
						newS = times[i];
						dMin = Double.parseDouble(times[i]);
						dMax = Double.parseDouble(times[i]);
					}
					else{
						newS += " " + times[i];
						dMin = (Double.parseDouble(times[i]) < dMin) ? Double.parseDouble(times[i]) : dMin;
						dMax = (Double.parseDouble(times[i]) > dMax) ? Double.parseDouble(times[i]) : dMax;
					}
				}
			}
			p.setProperty("dmvcTime_" + name, newS);
		}
		if (dMin != null){
			p.setProperty("dMin", dMin.toString());
			p.setProperty("dMax", dMax.toString());
		}
	}
	
	public ArrayList<ArrayList<Double>> normalize() {
		Double minDelay = getMinDelay();
		Double maxDelay = getMaxDelay();
		Double minDivision = null;
		Double maxDivision = null;
		Double scaleFactor = 1.0;
		ArrayList<ArrayList<Double>> scaledDiv = new ArrayList<ArrayList<Double>>();
		// deep copy of divisions
		for (ArrayList<Double> o1 : divisionsL){
			ArrayList<Double> tempDiv = new ArrayList<Double>();
			for (Double o2 : o1){
				tempDiv.add( o2.doubleValue()); // clone() not working here
			}
			scaledDiv.add(tempDiv);
		}
		try {
			out.write("minimum delay is " + minDelay
					+ " before scaling time.\n");
			if ((minDelay != null) && (minDelay != 0)) {
				for (int i = 0; i < 18; i++) {
					if (scaleFactor > (minDelayVal / minDelay)) {
						break;
					}
					scaleFactor *= 10.0;
				}
				if ((maxDelay != null) && ((int) (maxDelay * scaleFactor) > Integer.MAX_VALUE)) {
					System.out.println("Delay Scaling has caused an overflow");
				}
				out.write("minimum delay value is " + scaleFactor * minDelay
						+ "after scaling by " + scaleFactor + "\n");
				delayScaleFactor = scaleFactor;
				scaleDelay();
			}
			scaleFactor = 1.0;
			Double minRate = getMinRate(); // minRate should return minimum by
			// magnitude alone?? or even by sign..
			Double maxRate = getMaxRate();
			out.write("minimum rate is " + minRate + " before scaling the variable.\n");
			if ((minRate != null) && (minRate != 0)) {
				for (int i = 0; i < 14; i++) {
					if (scaleFactor > Math.abs(minRateVal / minRate)) {
						break;
					}
					scaleFactor *= 10.0;
				}
				for (int i = 0; i < 14; i++) {
					if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) < Integer.MAX_VALUE)) {
						break;
					}
					scaleFactor /= 10.0;
				}
				if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) > Integer.MAX_VALUE)) {
					System.out.println("Rate Scaling has caused an overflow");
				}
				out.write("minimum rate is " + minRate * scaleFactor + " after scaling by " + scaleFactor + "\n");
				varScaleFactor = scaleFactor;
				scaledDiv = scaleVariable(scaleFactor,scaledDiv);
		// TEMPORARY
		/*		for (String p : g.getPlaceList()){
					if ((placeInfo.get(getPlaceInfoIndex(p)).getProperty("type")).equals("PROP")){
						String s = g.scaleEnabling(g.getPostset(p)[0],scaleFactor);
						System.out.println(s);
					}
				} */
		// end TEMPORARY
			}
			minDivision = getMinDiv(scaledDiv);
			maxDivision = getMaxDiv(scaledDiv);
			out.write("minimum division is " + minDivision + " before scaling for division.\n");
			scaleFactor = 1.0;
			if ((minDivision != null) && (minDivision != 0)) {
				for (int i = 0; i < 14; i++) {
					if (Math.abs(scaleFactor * minDivision) > minDivisionVal) {
						break;
					}
					scaleFactor *= 10;
				}
				if ((maxDivision != null)
						&& (Math.abs((int) (maxDivision * scaleFactor)) > Integer.MAX_VALUE)) {
					System.out.println("Division Scaling has caused an overflow");
				}
				out.write("minimum division is " + minDivision * scaleFactor
						+ " after scaling by " + scaleFactor + "\n");
				varScaleFactor *= scaleFactor;
				scaledDiv = scaleVariable(scaleFactor,scaledDiv);
	// TEMPORARY
		/*		for (String p : g.getPlaceList()){
					if ((placeInfo.get(getPlaceInfoIndex(p)).getProperty("type")).equals("PROP")){
						String s = g.scaleEnabling(g.getPostset(p)[0],scaleFactor);
						System.out.println(s);
					}
				}*/
	// END TEMPORARY
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"LPN file couldn't be created/written",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		return(scaledDiv);
	}

	public ArrayList<ArrayList<Double>> scaleVariable(Double scaleFactor, ArrayList<ArrayList<Double>> divisions) {
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

					if (p.getProperty("type").equals("DMVC")) {
						p.setProperty("DMVCValue", Double.toString(Double.parseDouble(p.getProperty("DMVCValue"))* scaleFactor));
					} else {
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
					}
				}
			}
			int i = 0;
			for (Variable v : reqdVarsL) {
				v.scaleInitByVar(scaleFactor);
				for (int j = 0; j < divisions.get(i).size(); j++) {
					divisions.get(i).set(j,divisions.get(i).get(j) * scaleFactor);
				}
				i++;
			}
		}
		catch (NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Not all regions have values for all dmv variables",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		return divisions;
	}

	public void scaleDelay() {
		try{
			String place;
			Properties p;
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
					if (p.getProperty("type").equals("DMVC")) {
						String[] times = null;
						String name = p.getProperty("DMVCVariable");
						String s = p.getProperty("dmvcTime_" + name);
						String newS = null;
						if (s != null) {
							times = s.split(" ");
							for (int i = 0; i < times.length; i++) {
								if (newS == null) {
									// newS = Integer.toString((int)(Double.parseDouble(times[i])*delayScaleFactor));
									newS = Double.toString(Double.parseDouble(times[i])
											* delayScaleFactor);
								} else {
									// newS = newS + Integer.toString((int)(Double.parseDouble(times[i])*delayScaleFactor));
									newS = newS + " " + Double.toString(Double
											.parseDouble(times[i]) * delayScaleFactor);
								}
							}
							p.setProperty("dmvcTime_" + name, newS);
						}
						p.setProperty("dMin", Double.toString(Double.parseDouble(p
								.getProperty("dMin")) * delayScaleFactor));
						p.setProperty("dMax", Double.toString(Double.parseDouble(p
								.getProperty("dMax")) * delayScaleFactor));
					} else{
						// p.setProperty("dMin",Integer.toString((int)(Double.parseDouble(p.getProperty("dMin"))*delayScaleFactor)));
						// p.setProperty("dMax",Integer.toString((int)(Double.parseDouble(p.getProperty("dMax"))*delayScaleFactor)));
						p.setProperty("dMin", Double.toString(Double.parseDouble(p
								.getProperty("dMin")) * delayScaleFactor));
						p.setProperty("dMax", Double.toString(Double.parseDouble(p
								.getProperty("dMax")) * delayScaleFactor));
						for (Variable v : reqdVarsL) {
							if (!v.isDmvc()) {
								// p.setProperty(v.getName() +
								// "_rMin",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
								// + "_rMin"))/delayScaleFactor)));
								// p.setProperty(v.getName() +
								// "_rMax",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
								// + "_rMax"))/delayScaleFactor)));
								p.setProperty(v.getName() + "_rMin", Double
										.toString(Double.parseDouble(p.getProperty(v
												.getName() + "_rMin"))	/ delayScaleFactor));
								p.setProperty(v.getName() + "_rMax", Double
										.toString(Double.parseDouble(p.getProperty(v
												.getName() + "_rMax"))	/ delayScaleFactor));
							}
						}
					}
				}
			}
			for (Variable v : reqdVarsL) {
				// if (!v.isDmvc()){ this if maynot be required.. rates do exist for dmvc ones as well.. since calculated before detectDMV
				v.scaleInitByDelay(delayScaleFactor);
				// }
			}
			// SEE IF RATES IN TRANSITIONS HAVE TO BE ADJUSTED HERE
		}
		catch(NullPointerException e){
			System.out.println("Delay scaling error due to null. Check");
		}
	}

	public Double getMinDiv(ArrayList<ArrayList<Double>> divisions) {
		Double minDiv = divisions.get(0).get(0);
		for (int i = 0; i < divisions.size(); i++) {
			for (int j = 0; j < divisions.get(i).size(); j++) {
				if (divisions.get(i).get(j) < minDiv) {
					minDiv = divisions.get(i).get(j);
				}
			}
		}
		return minDiv;
	}

	public Double getMaxDiv(ArrayList<ArrayList<Double>> divisions) {
		Double maxDiv = divisions.get(0).get(0);
		for (int i = 0; i < divisions.size(); i++) {
			for (int j = 0; j < divisions.get(i).size(); j++) {
				if (divisions.get(i).get(j) > maxDiv) {
					maxDiv = divisions.get(i).get(j);
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
		return minRate;
	}

	public Double getMaxRate() {
		Double maxRate = null;
		for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if (p.getProperty("type").equals("RATE")) {
				for (Variable v : reqdVarsL) {
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
		return maxRate;
	}

	public Double getMinDelay() {
		Double minDelay = null;
		for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if (p.getProperty("type").equals("DMVC")) {
				if ((minDelay == null) && (getMinDmvcTime(p) != null)
						&& (getMinDmvcTime(p) != 0)) {
					minDelay = getMinDmvcTime(p);
				} else if ((getMinDmvcTime(p) != null)
						&& (getMinDmvcTime(p) != 0)
						&& (getMinDmvcTime(p) < minDelay)) {
					minDelay = getMinDmvcTime(p);
				}
			} else {
				if ((minDelay == null) && (p.getProperty("dMin") != null)
						&& (Double.parseDouble(p.getProperty("dMin")) != 0)) {
					minDelay = Double.parseDouble(p.getProperty("dMin"));
				} else if ((p.getProperty("dMin") != null)
						&& (Double.parseDouble(p.getProperty("dMin")) != 0)
						&& (Double.parseDouble(p.getProperty("dMin")) < minDelay)) {
					minDelay = Double.parseDouble(p.getProperty("dMin"));
				}
			}
		}
		return minDelay;
	}

	public Double getMaxDelay() {
		Double maxDelay = null;
		for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if (p.getProperty("type").equals("DMVC")) {
				if ((maxDelay == null) && (getMaxDmvcTime(p) != null)
						&& (getMaxDmvcTime(p) != 0)) {
					maxDelay = getMaxDmvcTime(p);
				} else if ((getMaxDmvcTime(p) != null)
						&& (getMaxDmvcTime(p) != 0)
						&& (getMaxDmvcTime(p) > maxDelay)) {
					maxDelay = getMaxDmvcTime(p);
				}
			} else {
				if ((maxDelay == null) && (p.getProperty("dMax") != null)
						&& (Double.parseDouble(p.getProperty("dMax")) != 0)) {
					maxDelay = Double.parseDouble(p.getProperty("dMax"));
				} else if ((p.getProperty("dMax") != null)
						&& (Double.parseDouble(p.getProperty("dMax")) != 0)
						&& (Double.parseDouble(p.getProperty("dMax")) > maxDelay)) {
					maxDelay = Double.parseDouble(p.getProperty("dMax"));
				}
			}
		}
		return maxDelay;
	}

	public Double getMinDmvcTime(Properties p) {
		String[] times = null;
		String name = p.getProperty("DMVCVariable");
		String s = p.getProperty("dmvcTime_" + name);
		if (s != null) {
			times = s.split(" ");
			Double min = Double.parseDouble(times[0]);
			for (int i = 0; i < times.length; i++) {
				if (Double.parseDouble(times[i]) < min) {
					min = Double.parseDouble(times[i]);
				}
			}
			return min;
		} else {
			return null;
		}
	}

	public Double getMaxDmvcTime(Properties p) {
		String[] times = null;
		String name = p.getProperty("DMVCVariable");
		String s = p.getProperty("dmvcTime_" + name);
		if (s != null) {
			times = s.split(" ");
			Double max = Double.parseDouble(times[0]);
			for (int i = 0; i < times.length; i++) {
				if (Double.parseDouble(times[i]) > max) {
					max = Double.parseDouble(times[i]);
				}
			}
			return max;
		} else {
			return null;
		}
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
		// String p_bin[] = p.getBinEncoding();
		String[] preset_encoding = pre_bin.split("");
		String[] postset_encoding = post_bin.split("");
		for (int j = 1; j < preset_encoding.length; j++) { // to account for ""
			// being created in the array
			if (Integer.parseInt(preset_encoding[j]) != Integer
					.parseInt(postset_encoding[j])) {
				diffL.add(j - 1);// to account for "" being created in the
				// array
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
	
	public Double[][] getDataExtrema(ArrayList<ArrayList<Double>> data){
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
		
	//public ArrayList<ArrayList<Double>> initDivisions(Double[][] extrema, ArrayList<ArrayList<Double>>  divisions ){
	public ArrayList<ArrayList<Double>> initDivisions(Double[][] extrema){
		int numThresholds = Integer.parseInt(numBins.getSelectedItem().toString()) - 1;
		double interval;
		ArrayList<ArrayList<Double>> divisions = new ArrayList<ArrayList<Double>>();
		for (int i = 0; i < reqdVarsL.size(); i++){
			divisions.add(new ArrayList<Double>());
			if (!suggestIsSource){ // could use user.isselected instead of this.
				//numThresholds = Integer.parseInt(numBins.getSelectedItem().toString()) - 1;
			}
			else{
				numThresholds = Integer.parseInt((String)((JComboBox)((JPanel)variablesPanel.getComponent(i+1)).getComponent(2)).getSelectedItem())-1;
			}
			interval = (Math.abs(extrema[i][1] - extrema[i][0]))/(numThresholds + 1);
			for (int j = 0; j< numThresholds; j++){
				//if ((divisions.get(i).size() == 0) || (divisions.get(i).get(j) == null)){
				//	divisions.get(i).set(j,extrema[i][0] + interval*j);
				//}
				if (divisions.get(i).size() <= j){
					divisions.get(i).add(extrema[i][0] + interval*(j+1));  // j+1
				}
				else{
					divisions.get(i).set(j,extrema[i][0] + interval*(j+1)); // j+1
				}
			}
		}
		suggestIsSource = false;
		return divisions;
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
	
	
	//public ArrayList<ArrayList<Double>> autoGenT(ArrayList<ArrayList<Double>>  divisions ){
	public ArrayList<ArrayList<Double>> autoGenT(JFrame running){
		//int iterations = Integer.parseInt(iteration.getText());
		ArrayList<ArrayList<Double>> fullData = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> singleFileData = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> divisions = new ArrayList<ArrayList<Double>>();
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
			Double[][] extrema = getDataExtrema(fullData);
			//divisions = initDivisions(extrema,divisions);
			divisions = initDivisions(extrema);
			divisions = greedyOpt(divisions,fullData,extrema);
		}
		catch(NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to calculate rates.\nThresholds could not be generated\nWindow size or pathlength must be reduced.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
			try {
				out.write("ERROR! Unable to calculate rates.\nThresholds could not be generated\nIf Window size = -1, pathlength must be reduced;\nElse, reduce windowsize\nLearning unsuccessful.");
				out.close();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			running.setCursor(null);
			running.dispose();
		}
		return divisions;
	}
	
	
	public ArrayList<ArrayList<Double>> greedyOpt(ArrayList<ArrayList<Double>> divisions,ArrayList<ArrayList<Double>> fullData, Double[][] extrema){
		ArrayList<ArrayList<Double>> newDivs = new ArrayList<ArrayList<Double>>(); // = divisions; // initialization rechk??
		ArrayList<Integer> res = new ArrayList<Integer>();
		int updateVar = 0;
		Double bestCost =0.0,newCost;
		Double distance = 0.0;
		boolean pointsSelected = false;
		int numMoves = 0;
		int iterations =  Integer.parseInt(iteration.getText());
		if (points.isSelected()){
			pointsSelected = true;
			bestCost = pointDistCost(fullData, divisions,res,updateVar);
		}
		else if (range.isSelected()){
			pointsSelected = false;
			bestCost = rateRangeCost(fullData, divisions);
		}
		while (numMoves < iterations){
			for (int i = 0; i < divisions.size(); i++){
				for (int j = 0; j < divisions.get(i).size(); j++){
					if (j == 0){
						if (divisions.get(i).get(j) !=  null){
							distance = Math.abs(divisions.get(i).get(j) - extrema[i][0])/2;
						}
						else{// will else case ever occur???
							distance = Math.abs(divisions.get(i).get(j) - divisions.get(i).get(j-1))/2;
						}
					}
					else{
						distance = Math.abs(divisions.get(i).get(j) - divisions.get(i).get(j-1))/2;
					}
					// deep copy
					//newDivs = divisions; 
					newDivs = new ArrayList<ArrayList<Double>>();
					for (ArrayList<Double> o1 : divisions){
						ArrayList<Double> tempDiv = new ArrayList<Double>();
						for (Double o2 : o1){
							tempDiv.add( o2.doubleValue()); // clone() not working here
						}
						newDivs.add(tempDiv);
					}
					newDivs.get(i).set(j,newDivs.get(i).get(j)-distance);
					if (pointsSelected){
						newCost = pointDistCost(fullData,newDivs,res,i+1);
					}
					else{
						newCost = rateRangeCost(fullData, newDivs);
					}
					numMoves++;
					if (numMoves % 500 == 0){
						System.out.println("Iteration "+ numMoves + "/" + iterations);
					}
					if (newCost < bestCost){
						bestCost = newCost;
						divisions = new ArrayList<ArrayList<Double>>();
						for (ArrayList<Double> o1 : newDivs){
							ArrayList<Double> tempDiv = new ArrayList<Double>();
							for (Double o2 : o1){
								tempDiv.add( o2.doubleValue()); // clone() not working here
							}
							divisions.add(tempDiv);
						}
						// divisions = newDivs; deep copy ?????
					}
					else{
						if (j == (divisions.get(i).size() - 1)){
							distance = Math.abs(extrema[i][1] - divisions.get(i).get(j))/2;
						}
						else{
							distance = Math.abs(divisions.get(i).get(j+1) - divisions.get(i).get(j))/2;
						}
						// deep copy
						//newDivs = divisions;
						newDivs = new ArrayList<ArrayList<Double>>();
						for (ArrayList<Double> o1 : divisions){
							ArrayList<Double> tempDiv = new ArrayList<Double>();
							for (Double o2 : o1){
								tempDiv.add( o2.doubleValue()); // clone() not working here
							}
							newDivs.add(tempDiv);
						}
						newDivs.get(i).set(j,newDivs.get(i).get(j)+distance);
						if (pointsSelected){
							newCost = pointDistCost(fullData,newDivs,res,i+1);
						}
						else{
							newCost = rateRangeCost(fullData, newDivs);
						}
						numMoves++;
						if (numMoves % 500 == 0){
							System.out.println("Iteration "+ numMoves + "/" + iterations);
						}
						if (newCost < bestCost){
							bestCost = newCost;
							divisions = new ArrayList<ArrayList<Double>>();
							for (ArrayList<Double> o1 : newDivs){
								ArrayList<Double> tempDiv = new ArrayList<Double>();
								for (Double o2 : o1){
									tempDiv.add( o2.doubleValue()); // clone() not working here
								}
								divisions.add(tempDiv);
							}
							// divisions = newDivs; deep copy ?????
						}
						if (numMoves > iterations){
							return divisions;
						}
					}
				}
			}
		}
		return divisions;
	}
	
	public Double rateRangeCost(ArrayList<ArrayList<Double>> fullData, ArrayList<ArrayList<Double>> divisions){
		Double total = 0.0;
		Double[] minMaxR = {null,null};
		//genBinsRates(datFile, divisions);
		Double[][] rates = genBinsRatesForAutogen(fullData, divisions);
		for (int i = 0; i < divisions.size(); i++){
			minMaxR = getMinMaxRates(rates[i]);
			total += Math.abs(minMaxR[1] - minMaxR[0]);
		}
		return total;
	}
	
	public Double pointDistCost(ArrayList<ArrayList<Double>> fullData,ArrayList<ArrayList<Double>> divisions, ArrayList<Integer> res, int updateVar ){
		Double total = 0.0;
		int pts = 0;
		if (updateVar == 0){
			for (int i = 0; i < divisions.size() + 1; i++){
				res.add(0);
			}
			for (int i = 0; i < divisions.size(); i++){
				pts = pointDistCostVar(fullData.get(i+1),divisions.get(i));
				total += pts;
				res.set(i,pts);
			}
		}
		else if (updateVar > 0){  // res is kind of being passed by reference. it gets altered outside too. 
			res.set(updateVar-1, pointDistCostVar(fullData.get(updateVar),divisions.get(updateVar-1)));
			for (Integer i : res){
				total += i;
			}
			/*for (int i = 0; i < res.size(); i++){
				if ((updateVar - 1) != i){
					total += res.get(i);
				}
				else{
					total += pointDistCostVar(fullData.get(updateVar),divisions.get(updateVar-1))
				}
			} */
		}
		else{
			for (int i = 0; i < divisions.size(); i++){
				total += pointDistCostVar(fullData.get(i+1),divisions.get(i));
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
	
	public Double[][] genBinsRatesForAutogen(ArrayList<ArrayList<Double>> data,ArrayList<ArrayList<Double>> divisionsL) { // genBins
//		public void genBinsRates(String datFile,ArrayList<ArrayList<Double>> divisionsL) { // genBins
//			TSDParser tsd = new TSDParser(directory + separator + datFile, biosim,false);
			// genBins
//			data = tsd.getData();
			rateSampling = Integer.parseInt(rateSamplingG.getText().trim());
			pathLength = Integer.parseInt(pathLengthG.getText().trim());
		//	ArrayList<Integer> reqdVarIndices = new ArrayList<Integer>();
			int[][] bins = new int[reqdVarsL.size()][data.get(0).size()];
			for (int i = 0; i < reqdVarsL.size(); i++) {
			//	for (int j = 1; j < varNames.size(); j++) {
			//		if (reqdVarsL.get(i).getName().equalsIgnoreCase(varNames.get(j))) {
						// System.out.println(reqdVarsL.get(i) + " matched "+
						// varNames.get(j) + " i = " + i + " j = " + j);
			//			reqdVarIndices.add(j);
						for (int k = 0; k < data.get(i+1).size(); k++) {
							for (int l = 0; l < divisionsL.get(i).size(); l++) {
								if (data.get(i+1).get(k) <= divisionsL.get(i).get(l)) {
									bins[i][k] = l;
									break;
								} else {
									bins[i][k] = l + 1; // indices of bins not same as that of the variable. i here. not j; if j
									// wanted, then size of bins array should be varNames not reqdVars
								}
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
			if (placeRates) {
				if (rateSampling == -1) { // replacing inf with -1 since int
					mark = 0;
					for (int i = 0; i < data.get(0).size(); i++) {
						if (i < mark) {
							continue;
						}
						while ((mark < data.get(0).size()) && (compareBins(i, mark,bins))) {
							mark++;
						}
						if ((data.get(0).get(mark - 1) != data.get(0).get(i)) && ((mark - i) >=  pathLength)) { 
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
			} 
			/*
			 * ADD LATER: duration[i] SHOULD BE ADDED TO THE NEXT 2 IF/ELSE
			 * BRANCHES(Transition based rate calc) ALSO
			 */
			else { // Transition based rate calculation
				if (rateSampling == -1) { // replacing inf with -1 since int
					for (int j = 0; j < reqdVarsL.size(); j++) {
						mark = 0;
						//k = reqdVarIndices.get(j);
						for (int i = 0; i < data.get(0).size(); i++) {
							if (i < mark) {
								continue;
							}
							while ((mark < data.get(0).size())
									&& (bins[j][i] == bins[j][mark])) {
								mark++;
							}
							if ((data.get(0).get(mark - 1) != data.get(0).get(i))) {
								rates[j][i] = ((data.get(j+1).get(mark - 1) - data.get(j+1).get(i)) / (data.get(0).get(mark - 1) - data.get(0).get(i)));
							}
						}
					}
				} else {
					boolean calcRate;
					for (int i = 0; i < (data.get(0).size() - rateSampling); i++) {
						for (int j = 0; j < reqdVarsL.size(); j++) {
							calcRate = true;
							//k = reqdVarIndices.get(j);
							for (int l = 0; l < rateSampling; l++) {
								if (bins[j][i] != bins[j][i + l]) {
									calcRate = false;
									break;
								}
							}
							if (calcRate && (data.get(0).get(i + rateSampling) != data.get(0).get(i))) {
								rates[j][i] = ((data.get(j+1).get(i + rateSampling) - data.get(j+1).get(i)) / (data.get(0).get(i + rateSampling) - data.get(0).get(i)));
							}
						}
					}
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
	
	public void addMetaBins(){
		boolean foundBin = false;
		for (String st1 : g.getPlaceList()){
			String p = getPlaceInfoIndex(st1);
			String[] binEncoding ;
			ArrayList<Integer> syncBinEncoding;
			String o1;
			// st1 w.r.t g
			// p w.r.t placeInfo
			if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
				String [] bE = p.split("");
				binEncoding = new String[bE.length - 1];
				for (int i = 0; i < bE.length - 1; i++){
					binEncoding[i] = bE[i+1];    // since p.split("") gives ,0,1 if p was 01
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
								g.addControlFlow("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + key).getProperty("transitionNum")); 
								g.addControlFlow("t" + transitionInfo.get(p + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
							//	g.addEnabling("t" + numTransitions, "~fail");
								g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")" + "&~fail");
								g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "["	+ getMinRate(key, reqdVarsL.get(i).getName())	+ "," + getMaxRate(key, reqdVarsL.get(i).getName()) + "]");
								numTransitions++;
								Properties p2 = new Properties();
								transitionInfo.put(key + p, p2);
								p2.setProperty("transitionNum", numTransitions.toString());
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addControlFlow("p" + placeInfo.get(key).getProperty("placeNum"), "t" + transitionInfo.get(key + p).getProperty("transitionNum")); 
								g.addControlFlow("t" + transitionInfo.get(key + p).getProperty("transitionNum"), "p" + placeInfo.get(p).getProperty("placeNum"));
								g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")" + "&~fail");
								g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "["	+ getMinRate(p, reqdVarsL.get(i).getName())	+ "," + getMaxRate(p, reqdVarsL.get(i).getName()) + "]");
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
									g.addControlFlow("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + key).getProperty("transitionNum")); 
									g.addControlFlow("t" + transitionInfo.get(p + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
								//	g.addEnabling("t" + numTransitions, "~fail");
									g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")" + "&~fail");
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "["	+ getMinRate(key, reqdVarsL.get(i).getName())	+ "," + getMaxRate(key, reqdVarsL.get(i).getName()) + "]");
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
								g.addControlFlow("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + key).getProperty("transitionNum")); 
								g.addControlFlow("t" + transitionInfo.get(p + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
							//	g.addEnabling("t" + numTransitions, "~fail");
								g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "["	+ getMinRate(key, reqdVarsL.get(i).getName())	+ "," + getMaxRate(key, reqdVarsL.get(i).getName()) + "]");
								g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")" + "&~fail");
								numTransitions++;
								Properties p2 = new Properties();
								transitionInfo.put(key + p, p2);
								p2.setProperty("transitionNum", numTransitions.toString());
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addControlFlow("p" + placeInfo.get(key).getProperty("placeNum"), "t" + transitionInfo.get(key + p).getProperty("transitionNum")); 
								g.addControlFlow("t" + transitionInfo.get(key + p).getProperty("transitionNum"), "p" + placeInfo.get(p).getProperty("placeNum"));
								g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")" + "&~fail");
								g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "["	+ getMinRate(p, reqdVarsL.get(i).getName())	+ "," + getMaxRate(p, reqdVarsL.get(i).getName()) + "]");
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
									g.addControlFlow("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + key).getProperty("transitionNum")); 
									g.addControlFlow("t" + transitionInfo.get(p + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
								//	g.addEnabling("t" + numTransitions, "~fail");
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "["	+ getMinRate(key, reqdVarsL.get(i).getName())	+ "," + getMaxRate(key, reqdVarsL.get(i).getName()) + "]");
									g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")" + "&~fail");
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
				String [] bE = p.split("");
				binEncoding = new String[bE.length - 1];
				for (int i = 0; i < bE.length - 1; i++){
					binEncoding[i] = bE[i+1];    // since p.split("") gives ,0,1 if p was 01
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
									g.addControlFlow("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + key).getProperty("transitionNum")); 
									g.addControlFlow("t" + transitionInfo.get(p + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
								//	g.addEnabling("t" + numTransitions, "~fail");
									g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")" + "&~fail");
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "["	+ getMinRate(key, reqdVarsL.get(i).getName())	+ "," + getMaxRate(key, reqdVarsL.get(i).getName()) + "]");
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
									g.addControlFlow("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + key).getProperty("transitionNum")); 
									g.addControlFlow("t" + transitionInfo.get(p + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
								//	g.addEnabling("t" + numTransitions, "~fail");
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "["	+ getMinRate(key, reqdVarsL.get(i).getName())	+ "," + getMaxRate(key, reqdVarsL.get(i).getName()) + "]");
									g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")" + "&~fail");
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
				vhdlAms.write("\tshared variable fail:boolean:= false;\n");
			}
			for (String st1 : g.getPlaceList()){
				if (!isTransientPlace(st1)){
					String p = getPlaceInfoIndex(st1);
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						ratePlaces.add(st1); // w.r.t g here
					}
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("DMVC")) {
						dmvcPlaces.add(p); // w.r.t placeInfo here
					}
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("PROP")) {

					}
				}
				else{
					String p = getTransientNetPlaceIndex(st1);
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")){
						ratePlaces.add(st1); // w.r.t g here
					}
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("DMVC")){
						dmvcPlaces.add(p); // w.r.t g here
					}
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
				vhdlAms.write("\t\tfail := true;\n");
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
	}
	public void writeVerilogAMSFile(String vamsFileName){
		try{
			ArrayList<String> ratePlaces = new ArrayList<String>();
			ArrayList<String> dmvcPlaces = new ArrayList<String>();
			File vamsFile = new File(directory + separator + vamsFileName);
			vamsFile.createNewFile();
			Double rateFactor = varScaleFactor/delayScaleFactor;
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
						double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*varScaleFactor);
						initBuffer.append("\t\tchange_"+v.getName()+" = "+ spanAvg+";\n");
						vals = v.getInitRate().split("\\,");
						spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*rateFactor);
						initBuffer.append("\t\trate_"+v.getName()+" = "+ (int)spanAvg+";\n");
					}
					else{
						buffer2.append("\treal "+v.getName()+"Val;\n");
						vals = reqdVarsL.get(i).getInitValue().split("\\,");
						double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*varScaleFactor);
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
			vams.write("\tinteger place;\n\n\tinitial\n\tbegin\n");
			vams.write(initBuffer.toString());
			vams.write("\t\tentryTime = 0;\n");
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
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("DMVC")) {
						dmvcPlaces.add(p); // w.r.t placeInfo here
					}
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("PROP")) {

					}
				}
				else{
					String p = getTransientNetPlaceIndex(st1);
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")){
						ratePlaces.add(st1); // w.r.t g here
					}
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("DMVC")){
						dmvcPlaces.add(p); // w.r.t  placeInfo here
					}
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
			for (String t : transitions){
				presetPlace = g.getPreset(t)[0];
				//if (g.getPostset(t) != null){
				//	postsetPlace = g.getPostset(t)[0];
				//}
				transNum = Integer.parseInt(t.split("t")[1]);
				cnt = transNum;
				if (!isTransientTransition(t)){
					if (placeInfo.get(getPlaceInfoIndex(presetPlace)).getProperty("type").equals("RATE")){
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
								transBuffer[cnt].append("\talways@(place)" + "\n\tbegin\n");
							}
							else{
								transBuffer[cnt].append("\t"+transEnablingsVAMS[transNum] + "\n\tbegin\n");	
							}
						}
						else{
							String s = transBuffer[cnt].toString();
							s = s.replaceAll("\t\tend\n\tend\n", "\t\tend\n");
							transBuffer[cnt].delete(0, transBuffer[cnt].length());
							transBuffer[cnt].append(s);
						}
						transBuffer[cnt].append("\t\tif (place == "+ presetPlace.split("p")[1] +")\n\t\tbegin\n");
						if (transDelayAssignVAMS[transNum] != null){
							transBuffer[cnt].append("\t\t\t#"+transDelayAssignVAMS[transNum]+";\n");
							for (int i = 0; i < transIntAssignVAMS[transNum].length; i++){
								if (transIntAssignVAMS[transNum][i] != null){
									transBuffer[cnt].append("\t\t\t"+reqdVarsL.get(i).getName()+"Val = "+ transIntAssignVAMS[transNum][i]+";\n");
								}
							}
						}
						transBuffer[cnt].append("\t\t\tentryTime = $abstime;\n");
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
				else{
					if (transientNetPlaces.get(getTransientNetPlaceIndex(presetPlace)).getProperty("type").equals("RATE")){
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
							transBuffer[cnt].append("\t\t\t#"+transDelayAssignVAMS[transNum]+";\n");
							for (int i = 0; i < transIntAssignVAMS[transNum].length; i++){
								if (transIntAssignVAMS[transNum][i] != null){
									transBuffer[cnt].append("\t\t\t"+reqdVarsL.get(i).getName()+"Val = "+ transIntAssignVAMS[transNum][i]+";\n");
								}
							}
						}
						transBuffer[cnt].append("\t\t\tentryTime = $abstime;\n");
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
					//double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*varScaleFactor);
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
					double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*varScaleFactor);
					initBuffer.append("\n\tinitial\n\tbegin\n"+"\t\t"+ reqdVarsL.get(i).getName() + "Val = "+ spanAvg+";\n");
					//buffer3.append("\talways\n\tbegin\n");
					boolean transientDoneFirst = false;
					for (String p : dmvcVarPlaces.get(i)){
						if (!transientNetPlaces.containsKey(p)){	// since p is w.r.t placeInfo & not w.r.t g
						//	buffer3.append("\t\t#"+  (int)(((Double.parseDouble(placeInfo.get(p).getProperty("dMin"))+ Double.parseDouble(placeInfo.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
							// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
						//	buffer3.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + placeInfo.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/varScaleFactor + ";\n");
							if (transientDoneFirst){
								initBuffer.append("\t\tforever\n\t\tbegin\n");
								transientDoneFirst = false;
							}
							initBuffer.append("\t\t\t#"+  (int)(((Double.parseDouble(placeInfo.get(p).getProperty("dMin"))+ Double.parseDouble(placeInfo.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
							initBuffer.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + placeInfo.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/varScaleFactor + ";\n");
						}
						else{
							/*buffer3.append("\tinitial\n\tbegin\n");
							buffer3.append("\t\t#"+  (int)(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin"))+ Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
							// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
							buffer3.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + transientNetPlaces.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/varScaleFactor + ";\n");
							buffer3.append("\tend\n");*/
							transientDoneFirst = true;
							initBuffer.append("\t\t#"+  (int)(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin"))+ Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
							initBuffer.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + transientNetPlaces.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/varScaleFactor + ";\n" );
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
			JOptionPane.showMessageDialog(biosim.frame(),
					"Verilog-AMS model couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
	//T[] aux = (T[])a.clone();
}



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

