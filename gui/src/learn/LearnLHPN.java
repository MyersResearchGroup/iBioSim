package learn;

import gcm2sbml.parser.GCMFile;
import lhpn2sbml.parser.LHPNFile;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import org.sbml.libsbml.*;
import biomodelsim.*;

/**
 * This class creates a GUI for the Learn program. It implements the
 * ActionListener class. This allows the GUI to perform actions when menu items
 * and buttons are selected.
 * 
 * @author Curtis Madsen
 */
public class LearnLHPN extends JPanel implements ActionListener, Runnable {

	private static final long serialVersionUID = -5806315070287184299L;

	// private JTextField initNetwork; // text field for initial network

	// private JButton browseInit; // the browse initial network button

	private JButton save, run, viewLhpn, saveLhpn, viewLog; // the run button

	private JComboBox debug; // debug combo box

	private JTextField property, iteration;

	// private JTextField windowRising, windowSize;

	private JComboBox numBins;

	// private JTextField influenceLevel, relaxIPDelta, letNThrough,
	// maxVectorSize;

	// private JCheckBox harshenBoundsOnTie, donotInvertSortOrder, seedParents;

	// private JCheckBox mustNotWinMajority, donotTossSingleRatioParents,
	// donotTossChangedInfluenceSingleParents;

	// private JRadioButton succ, pred, both;

	private JCheckBox basicFBP;

	private ArrayList<ArrayList<Component>> variables;

	private JPanel variablesPanel;

	private JRadioButton user, auto, range, points;

	private JButton suggest;

	private String directory, lrnFile;

	private JLabel numBinsLabel;

	private Log log;

	private String separator;

	private BioSim biosim;

	private String learnFile, binFile, lhpnFile;

	private boolean change;

	private ArrayList<String> variablesList;

	private boolean firstRead;

	/**
	 * This is the constructor for the Learn class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public LearnLHPN(String directory, Log log, BioSim biosim) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}

		this.biosim = biosim;
		this.log = log;
		this.directory = directory;
		String[] getFilename = directory.split(separator);
		lrnFile = getFilename[getFilename.length - 1] + ".lrn";
		binFile = getFilename[getFilename.length - 1] + ".bins";
		lhpnFile = getFilename[getFilename.length - 1] + ".g";
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
		JPanel thresholdPanel1 = new JPanel(new GridLayout(3, 2));
		JLabel propertyLabel = new JLabel("Property to Verify:");
		property = new JTextField("");
		thresholdPanel1.add(propertyLabel);
		thresholdPanel1.add(property);

		JLabel iterationLabel = new JLabel("Iterations of Optimization Algorithm");
		iteration = new JTextField("10000");
		thresholdPanel1.add(iterationLabel);
		thresholdPanel1.add(iteration);

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
		Properties load = new Properties();
		learnFile = "";
		try {
			FileInputStream in = new FileInputStream(new File(directory + separator + lrnFile));
			load.load(in);
			in.close();
			if (load.containsKey("learn.file")) {
				String[] getProp = load.getProperty("learn.file").split(separator);
				learnFile = directory.substring(0, directory.length()
						- getFilename[getFilename.length - 1].length())
						+ separator + getProp[getProp.length - 1];
			}
			if (load.containsKey("learn.iter")) {
				iteration.setText(load.getProperty("learn.iter"));
			}
			if (load.containsKey("learn.prop")) {
				property.setText(load.getProperty("learn.prop"));
			}
			if (load.containsKey("learn.bins")) {
				numBins.setSelectedItem(load.getProperty("learn.bins"));
			}
			if (load.containsKey("learn.equal")) {
				if (load.getProperty("learn.equal").equals("range")) {
					range.setSelected(true);
				}
				else {
					points.setSelected(true);
				}
			}
			if (load.containsKey("learn.use")) {
				if (load.getProperty("learn.use").equals("auto")) {
					auto.setSelected(true);
				}
				else if (load.getProperty("learn.use").equals("user")) {
					user.setSelected(true);
				}
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}

		variablesList = new ArrayList<String>();
		LHPNFile lhpn = new LHPNFile(log);
		lhpn.load(learnFile);
		HashMap<String, Properties> variablesMap = lhpn.getVariables();
		for (String s : variablesMap.keySet()) {
			variablesList.add(s);
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
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to create background file!",
					"Error Writing Background", JOptionPane.ERROR_MESSAGE);
		}

		// sortSpecies();
		JPanel runHolder = new JPanel();
		levels(false);
		if (auto.isSelected()) {
			auto.doClick();
		}
		else {
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
		if (!(new File(directory + separator + "run.log").exists())) {
			viewLog.setEnabled(false);
		}

		// Creates the main panel
		this.setLayout(new BorderLayout());
		JPanel middlePanel = new JPanel(new BorderLayout());
		JPanel firstTab = new JPanel(new BorderLayout());
		JPanel firstTab1 = new JPanel(new BorderLayout());
		// JPanel secondTab = new JPanel(new BorderLayout());
		middlePanel.add(radioPanel, "Center");
		// firstTab1.add(initNet, "North");
		firstTab1.add(thresholdPanelHold1, "Center");
		firstTab.add(firstTab1, "North");
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, middlePanel, null);
		splitPane.setDividerSize(0);
		// secondTab.add(thresholdPanelHold2, "North");
		firstTab.add(splitPane, "Center");
		// JTabbedPane tab = new JTabbedPane();
		// tab.addTab("Basic Options", firstTab);
		// tab.addTab("Advanced Options", secondTab);
		// this.add(tab, "Center");
		this.add(firstTab, "Center");
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
			if (variables != null && user.isSelected()) {
				for (int i = 0; i < variables.size(); i++) {
					editText(i);
				}
			}
			variablesPanel.revalidate();
			variablesPanel.repaint();
			biosim.setGlassPane(true);
		}
		else if (e.getSource() == numBins || e.getSource() == debug) {
			biosim.setGlassPane(true);
		}
		else if (e.getSource() == user) {
			if (!firstRead) {
				try {
					FileWriter write = new FileWriter(new File(directory + separator + binFile));
					// write.write("time 0\n");
					for (int i = 0; i < variables.size(); i++) {
						if (((JTextField) variables.get(i).get(0)).getText().trim().equals("")) {
							write.write("?");
						}
						else {
							write.write(((JTextField) variables.get(i).get(0)).getText().trim());
						}
						// write.write(" " + ((JComboBox)
						// variables.get(i).get(1)).getSelectedItem());
						for (int j = 2; j < variables.get(i).size(); j++) {
							if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
								write.write(" ?");
							}
							else {
								write.write(" "
										+ ((JTextField) variables.get(i).get(j)).getText().trim());
							}
						}
						write.write("\n");
					}
					write.close();
				}
				catch (Exception e1) {
				}
			}
			numBinsLabel.setEnabled(false);
			numBins.setEnabled(false);
			suggest.setEnabled(true);
			// levelsBin();
			variablesPanel.revalidate();
			variablesPanel.repaint();
			levels(true);
		}
		else if (e.getSource() == auto) {
			numBinsLabel.setEnabled(true);
			numBins.setEnabled(true);
			suggest.setEnabled(false);
			for (Component c : variablesPanel.getComponents()) {
				for (int i = 1; i < ((JPanel) c).getComponentCount(); i++) {
					((JPanel) c).getComponent(i).setEnabled(false);
				}
			}
		}
		else if (e.getSource() == suggest) {
			levels(false);
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
			save();
			new Thread(this).start();
		}
		else if (e.getSource() == save) {
			save();
		}
		else if (e.getSource() == viewLhpn) {
			viewLhpn();
		}
		else if (e.getSource() == viewLog) {
			viewLog();
		}
		else if (e.getSource() == saveLhpn) {
			saveLhpn();
		}
	}

	private void levels(boolean readfile) {
		ArrayList<String> str = null;
		try {
			if (!readfile) {
				FileWriter write = new FileWriter(new File(directory + separator + binFile));
				// write.write("time 0\n");
				for (int i = 0; i < variables.size(); i++) {
					if (((JTextField) variables.get(i).get(0)).getText().trim().equals("")) {
						write.write("?");
					}
					else {
						write.write(((JTextField) variables.get(i).get(0)).getText().trim());
					}
					// write.write(" " + ((JComboBox)
					// variables.get(i).get(1)).getSelectedItem());
					for (int j = 2; j < variables.get(i).size(); j++) {
						if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
							write.write(" ?");
						}
						else {
							write.write(" "
									+ ((JTextField) variables.get(i).get(j)).getText().trim());
						}
					}
					write.write("\n");
				}
				write.close();
				// Integer numThresh =
				// Integer.parseInt(numBins.getSelectedItem().toString()) - 1;
				String command = "autogenT.py -b" + binFile + " -i" + iteration.getText();
				if (range.isSelected()) {
					command = command + " -cr";
				}
				log.addText("Executing:\n" + command + " " + directory + "\n");
				File work = new File(directory);
				Runtime exec = Runtime.getRuntime();
				Process learn = exec.exec(command, null, work);
				try {
					String output = "";
					InputStream reb = learn.getInputStream();
					InputStreamReader isr = new InputStreamReader(reb);
					BufferedReader br = new BufferedReader(isr);
					FileWriter out = new FileWriter(new File(directory + separator + "run.log"));
					while ((output = br.readLine()) != null) {
						out.write(output);
						out.write("\n");
					}
					out.close();
					br.close();
					isr.close();
					reb.close();
					viewLog.setEnabled(true);
				}
				catch (Exception e) {
				}
				learn.waitFor();
			}
			Scanner f = new Scanner(new File(directory + separator + binFile));
			str = new ArrayList<String>();
			while (f.hasNextLine()) {
				str.add(f.nextLine());
			}
		}
		catch (Exception e1) {
		}
		if (!directory.equals("")) {
			if (true) {
				variablesPanel.removeAll();
				this.variables = new ArrayList<ArrayList<Component>>();
				variablesPanel.setLayout(new GridLayout(variablesList.size() + 1, 1));
				int max = 0;
				if (str != null) {
					for (String st : str) {
						String[] getString = st.split("\\s");
						max = Math.max(max, getString.length + 1);
					}
				}
				JPanel label = new JPanel(new GridLayout());
				// label.add(new JLabel("Use"));
				label.add(new JLabel("Species"));
				label.add(new JLabel("Number Of Bins"));
				for (int i = 0; i < max - 3; i++) {
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
					JComboBox combo = new JComboBox(options);
					combo.setSelectedItem(numBins.getSelectedItem());
					specs.add(combo);
					((JTextField) specs.get(0)).setEditable(false);
					// sp.add(specs.get(0));
					// ((JCheckBox) specs.get(0)).addActionListener(this);
					// ((JCheckBox) specs.get(0)).setActionCommand("box" + j);
					sp.add(specs.get(0));
					sp.add(specs.get(1));
					((JComboBox) specs.get(1)).addActionListener(this);
					((JComboBox) specs.get(1)).setActionCommand("text" + j);
					this.variables.add(specs);
					if (str != null) {
						boolean found = false;
						for (String st : str) {
							String[] getString = st.split(" ");
							if (getString[0].trim().equals(s)) {
								found = true;
								if (getString.length >= 1) {
									((JComboBox) specs.get(1))
											.setSelectedItem(getString.length - 1);
									for (int i = 0; i < Integer
											.parseInt((String) ((JComboBox) specs.get(1))
													.getSelectedItem()) - 1; i++) {
										if (getString[i + 1].trim().equals("?")) {
											specs.add(new JTextField(""));
										}
										else {
											specs.add(new JTextField(getString[i + 1].trim()));
										}
										sp.add(specs.get(i + 2));
									}
									for (int i = Integer.parseInt((String) ((JComboBox) specs
											.get(1)).getSelectedItem()) - 1; i < max - 3; i++) {
										sp.add(new JLabel());
									}
								}
							}
						}
						if (!found) {
							for (int i = 0; i < Integer
									.parseInt((String) ((JComboBox) specs.get(1)).getSelectedItem()) - 1; i++) {
								specs.add(new JTextField(""));
								sp.add(specs.get(i + 2));
							}
							for (int i = Integer.parseInt((String) ((JComboBox) specs.get(1))
									.getSelectedItem()) - 1; i < max - 3; i++) {
								sp.add(new JLabel());
							}
						}
					}
					else {
						for (int i = 0; i < Integer.parseInt((String) ((JComboBox) specs.get(1))
								.getSelectedItem()) - 1; i++) {
							specs.add(new JTextField(""));
							sp.add(specs.get(i + 2));
						}
					}
					variablesPanel.add(sp);
				}
			}
		}
		editText(0);
	}

	/*
	 * private void levelsBin() { if (!directory.equals("")) { // File n = null; //
	 * for (File f : new File(directory).listFiles()) { // if
	 * (f.getAbsolutePath().contains(".tsd")) { // n = f; // } // } if (true) { //
	 * if (n != null) { // ArrayList<String> species = new ArrayList<String>(); //
	 * try { // InputStream input = new FileInputStream(n); // boolean reading =
	 * true; // char cha; // while (reading) { // String word = ""; // boolean
	 * readWord = true; // while (readWord) { // int read = input.read(); // if
	 * (read == -1) { // reading = false; // readWord = false; // } // cha =
	 * (char) read; // if (Character.isWhitespace(cha)) { // word += cha; // } //
	 * else if (cha == ',' || cha == ':' || cha == ';' || cha == '\"' || cha // ==
	 * '\'' // || cha == '(' || cha == ')' || cha == '[' || cha == ']') { // if
	 * (!word.equals("") && !word.equals("time")) { // try { //
	 * Double.parseDouble(word); // } // catch (Exception e2) { //
	 * species.add(word); // } // } // word = ""; // } // else if (read != -1) { //
	 * word += cha; // } // } // } // input.close(); // } // catch (Exception
	 * e1) { // } speciesPanel.removeAll(); this.species = new ArrayList<ArrayList<Component>>();
	 * speciesPanel.setLayout(new GridLayout(speciesList.size() + 1, 1)); JPanel
	 * label = new JPanel(new GridLayout()); // label.add(new JLabel("Use"));
	 * label.add(new JLabel("Species")); label.add(new JLabel("Number Of
	 * Bins")); for (int i = 0; i < Integer.parseInt((String)
	 * numBins.getSelectedItem()) - 1; i++) { label.add(new JLabel("Level " + (i +
	 * 1))); } speciesPanel.add(label); int j = 0; for (String s : speciesList) {
	 * j++; JPanel sp = new JPanel(new GridLayout()); ArrayList<Component>
	 * specs = new ArrayList<Component>(); // JCheckBox check = new
	 * JCheckBox(); // check.setSelected(true); // specs.add(check);
	 * specs.add(new JTextField(s)); String[] options = { "0", "1", "2", "3",
	 * "4", "5", "6", "7", "8", "9" }; JComboBox combo = new JComboBox(options);
	 * combo.setSelectedItem(numBins.getSelectedItem()); specs.add(combo);
	 * ((JTextField) specs.get(0)).setEditable(false); // sp.add(specs.get(0)); //
	 * ((JCheckBox) specs.get(0)).addActionListener(this); // ((JCheckBox)
	 * specs.get(0)).setActionCommand("box" + j); sp.add(specs.get(0));
	 * sp.add(specs.get(1)); ((JComboBox) specs.get(1)).addActionListener(this);
	 * ((JComboBox) specs.get(1)).setActionCommand("text" + j);
	 * this.species.add(specs); for (int i = 0; i < Integer.parseInt((String)
	 * ((JComboBox) specs.get(1)) .getSelectedItem()) - 1; i++) { specs.add(new
	 * JTextField("")); sp.add(specs.get(i + 2)); } speciesPanel.add(sp); } } } }
	 */

	private void editText(int num) {
		try {
			ArrayList<Component> specs = variables.get(num);
			Component[] panels = variablesPanel.getComponents();
			int boxes = Integer.parseInt((String) ((JComboBox) specs.get(1)).getSelectedItem());
			if ((specs.size() - 2) < boxes) {
				for (int i = 0; i < boxes - 1; i++) {
					try {
						specs.get(i + 2);
					}
					catch (Exception e1) {
						JTextField temp = new JTextField("");
						((JPanel) panels[num + 1]).add(temp);
						specs.add(temp);
					}
				}
			}
			else {
				try {
					if (boxes > 0) {
						while (true) {
							specs.remove(boxes + 1);
							((JPanel) panels[num + 1]).remove(boxes + 1);
						}
					}
					else if (boxes == 0) {
						while (true) {
							specs.remove(2);
							((JPanel) panels[num + 1]).remove(2);
						}
					}
				}
				catch (Exception e1) {
				}
			}
			int max = 0;
			for (int i = 0; i < this.variables.size(); i++) {
				max = Math.max(max, variables.get(i).size());
			}
			if (((JPanel) panels[0]).getComponentCount() < max) {
				for (int i = 0; i < max - 2; i++) {
					try {
						((JPanel) panels[0]).getComponent(i + 2);
					}
					catch (Exception e) {
						((JPanel) panels[0]).add(new JLabel("Level " + (i + 1)));
					}
				}
			}
			else {
				try {
					while (true) {
						((JPanel) panels[0]).remove(max);
					}
				}
				catch (Exception e) {
				}
			}
			for (int i = 1; i < panels.length; i++) {
				JPanel sp = (JPanel) panels[i];
				for (int j = sp.getComponentCount() - 1; j >= 2; j--) {
					if (sp.getComponent(j) instanceof JLabel) {
						sp.remove(j);
					}
				}
				if (max > sp.getComponentCount()) {
					for (int j = sp.getComponentCount(); j < max; j++) {
						sp.add(new JLabel());
					}
				}
				else {
					for (int j = sp.getComponentCount() - 2; j >= max; j--) {
						sp.remove(j);
					}
				}
			}
		}
		catch (Exception e) {
		}
	}

	public void saveLhpn() {
		try {
			if (true) {// (new File(directory + separator +
				// "method.gcm").exists()) {
				String copy = JOptionPane.showInputDialog(biosim.frame(), "Enter Circuit Name:",
						"Save Circuit", JOptionPane.PLAIN_MESSAGE);
				if (copy != null) {
					copy = copy.trim();
				}
				else {
					return;
				}
				if (!copy.equals("")) {
					if (copy.length() > 1) {
						if (!copy.substring(copy.length() - 2).equals(".g")) {
							copy += ".g";
						}
					}
					else {
						copy += ".g";
					}
				}
				biosim.saveLhpn(copy, directory + separator + lhpnFile);
			}
			else {
				JOptionPane.showMessageDialog(biosim.frame(), "No circuit has been generated yet.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to save circuit.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewLhpn() {
		try {
			File work = new File(directory);
			if (new File(directory + separator + lhpnFile).exists()) {
				String dotFile = lhpnFile.replace(".lhpn", ".dot");
				String command = "open " + dotFile;
				log.addText("Executing:\n" + "open " + directory + separator + dotFile + "\n");
				Runtime exec = Runtime.getRuntime();
				exec.exec("atacs -lloddl " + lhpnFile + " " + dotFile, null, work);
				exec = Runtime.getRuntime();
				exec.exec(command, null, work);
			}
			else {
				JOptionPane.showMessageDialog(biosim.frame(), "No circuit has been generated yet.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to view circuit.", "Error",
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
				JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Run Log",
						JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(biosim.frame(), "No run log exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to view run log.", "Error",
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
			prop.setProperty("learn.prop", this.property.getText().trim());
			prop.setProperty("learn.bins", (String) this.numBins.getSelectedItem());
			if (range.isSelected()) {
				prop.setProperty("learn.equal", "range");
			}
			else {
				prop.setProperty("learn.equal", "points");
			}
			if (auto.isSelected()) {
				prop.setProperty("learn.use", "auto");
			}
			else {
				prop.setProperty("learn.use", "user");
			}
			log.addText("Saving learn parameters to file:\n" + directory + separator + lrnFile
					+ "\n");
			FileOutputStream out = new FileOutputStream(new File(directory + separator + lrnFile));
			prop.store(out, learnFile);
			out.close();
			String[] tempBin = lrnFile.split("\\.");
			String binFile = tempBin[0] + ".bins";
			FileWriter write = new FileWriter(new File(directory + separator + binFile));
			for (int i = 0; i < variables.size(); i++) {
				if (((JTextField) variables.get(i).get(0)).getText().trim().equals("")) {
					write.write("?");
				}
				else {
					write.write(((JTextField) variables.get(i).get(0)).getText().trim());
				}
				// write.write(", " + ((JComboBox)
				// variables.get(i).get(1)).getSelectedItem());
				for (int j = 2; j < variables.get(i).size(); j++) {
					if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
						write.write(" ?");
					}
					else {
						write.write(" " + ((JTextField) variables.get(i).get(j)).getText().trim());
					}
				}
				write.write("\n");
			}
			write.close();
			log.addText("Creating levels file:\n" + directory + separator + binFile + "\n");
			String command = "autogenT.py -b" + binFile + " -t"
					+ numBins.getSelectedItem().toString() + " -i" + iteration.getText();
			if (range.isSelected()) {
				command = command + " -cr";
			}
			File work = new File(directory);
			Runtime.getRuntime().exec(command, null, work);
			change = false;
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to save parameter file!",
					"Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void run() {
		try {
			File work = new File(directory);
			if (auto.isSelected()) {
				String makeBin = "autogenT.py -b" + binFile + " -t"
						+ numBins.getSelectedItem().toString() + " -i" + iteration.getText();
				if (range.isSelected()) {
					makeBin = makeBin + " -cr";
				}
				log.addText(makeBin);
				log.addText("Creating levels file:\n" + directory + separator + binFile + "\n");
				Process bins = Runtime.getRuntime().exec(makeBin, null, work);
				bins.waitFor();
			}
			String command = "data2lhpn.py -b" + binFile + " -l" + lhpnFile;
			if (property.getText().length() > 0) {
				String propFile = binFile.replace("bins", "prop");
				File prop = new File(directory + separator + propFile);
				prop.createNewFile();
				FileWriter write = new FileWriter(prop);
				write.write(property.getText().trim());
				write.close();
				command = command + " -p " + propFile;
			}
			log.addText("Executing:\n" + command + "\n");
			Process run = Runtime.getRuntime().exec(command, null, work);
			run.waitFor();
			command = "atacs -lloddl " + lhpnFile;
			Runtime.getRuntime().exec(command, null, work);
		}
		catch (Exception e) {
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
			if (((JComboBox) variables.get(i).get(1)).isFocusOwner()) {
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

	public void updateSpecies(String newLearnFile) {
		learnFile = newLearnFile;
		variablesList = new ArrayList<String>();
		if ((learnFile.contains(".sbml")) || (learnFile.contains(".xml"))) {
			SBMLReader reader = new SBMLReader();
			SBMLDocument document = reader.readSBML(learnFile);
			Model model = document.getModel();
			ListOf ids = model.getListOfSpecies();
			try {
				FileWriter write = new FileWriter(
						new File(directory + separator + "background.gcm"));
				write.write("digraph G {\n");
				for (int i = 0; i < model.getNumSpecies(); i++) {
					variablesList.add(((Species) ids.get(i)).getId());
					write.write("s" + i + " [shape=ellipse,color=black,label=\""
							+ ((Species) ids.get(i)).getId() + "\"" + "];\n");
				}
				write.write("}\n");
				write.close();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(biosim.frame(), "Unable to create background file!",
						"Error Writing Background", JOptionPane.ERROR_MESSAGE);
			}
		}
		else {
			GCMFile gcm = new GCMFile();
			gcm.load(learnFile);
			HashMap<String, Properties> speciesMap = gcm.getSpecies();
			for (String s : speciesMap.keySet()) {
				variablesList.add(s);
			}
			try {
				FileWriter write = new FileWriter(
						new File(directory + separator + "background.gcm"));
				BufferedReader input = new BufferedReader(new FileReader(new File(learnFile)));
				String line = null;
				while ((line = input.readLine()) != null) {
					write.write(line + "\n");
				}
				write.close();
				input.close();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(biosim.frame(), "Unable to create background file!",
						"Error Writing Background", JOptionPane.ERROR_MESSAGE);
			}
		}
		sortVariables();
		if (user.isSelected()) {
			auto.doClick();
			user.doClick();
		}
		else {
			user.doClick();
			auto.doClick();
		}
	}

	private void sortVariables() {
		int i, j;
		String index;
		for (i = 1; i < variablesList.size(); i++) {
			index = variablesList.get(i);
			j = i;
			while ((j > 0) && variablesList.get(j - 1).compareToIgnoreCase(index) > 0) {
				variablesList.set(j, variablesList.get(j - 1));
				j = j - 1;
			}
			variablesList.set(j, index);
		}
	}

	public void setDirectory(String directory) {
		this.directory = directory;
		String[] getFilename = directory.split(separator);
		lrnFile = getFilename[getFilename.length - 1] + ".lrn";
	}
}
