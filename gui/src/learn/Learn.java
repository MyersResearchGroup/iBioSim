package learn.core.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import biomodelsim.core.gui.*;

/**
 * This class creates a GUI for the Learn program. It implements the
 * ActionListener class. This allows the GUI to perform actions when menu items
 * and buttons are selected.
 * 
 * @author Curtis Madsen
 */
public class Learn extends JPanel implements ActionListener, Runnable {

	private static final long serialVersionUID = -5806315070287184299L;

	// private JTextField initNetwork; // text field for initial network

	// private JButton browseInit; // the browse initial network button

	private JButton save, run, viewGcm, saveGcm, viewLog; // the run button

	private JComboBox debug; // debug combo box

	private JTextField activation, repression, parent;

	// private JTextField windowRising, windowSize;

	private JComboBox numBins;

	private JTextField influenceLevel, relaxIPDelta, letNThrough, maxVectorSize;

	// private JCheckBox harshenBoundsOnTie, donotInvertSortOrder, seedParents;

	// private JCheckBox mustNotWinMajority, donotTossSingleRatioParents,
	// donotTossChangedInfluenceSingleParents;

	private JRadioButton succ, pred, both;

	private JCheckBox basicFBP;

	private ArrayList<ArrayList<Component>> species;

	private JPanel speciesPanel;

	private JRadioButton user, auto, spacing, data;

	private JButton suggest;

	private String directory, lrnFile;

	private JLabel numBinsLabel;

	private Log log;

	private String separator;

	private BioSim biosim;

	private String learnFile;

	private boolean change;

	/**
	 * This is the constructor for the Learn class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public Learn(String directory, Log log, BioSim biosim) {
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

		// Sets up the encodings area
		JPanel radioPanel = new JPanel(new BorderLayout());
		JPanel selection1 = new JPanel();
		JPanel selection2 = new JPanel();
		JPanel selection = new JPanel(new BorderLayout());
		spacing = new JRadioButton("Equal Spacing Of Bins");
		data = new JRadioButton("Equal Data Per Bins");
		user = new JRadioButton("Use User Generated Levels");
		auto = new JRadioButton("Use Auto Generated Levels");
		suggest = new JButton("Suggest Levels");
		ButtonGroup select = new ButtonGroup();
		select.add(auto);
		select.add(user);
		ButtonGroup select2 = new ButtonGroup();
		select2.add(spacing);
		select2.add(data);
		auto.setSelected(true);
		user.addActionListener(this);
		spacing.addActionListener(this);
		auto.addActionListener(this);
		suggest.addActionListener(this);
		data.setSelected(true);
		data.addActionListener(this);
		selection1.add(data);
		selection1.add(spacing);
		selection2.add(auto);
		selection2.add(user);
		selection2.add(suggest);
		selection.add(selection1, "North");
		selection.add(selection2, "Center");
		suggest.setEnabled(false);
		JPanel encodingPanel = new JPanel(new BorderLayout());
		speciesPanel = new JPanel();
		JPanel sP = new JPanel();
		((FlowLayout) sP.getLayout()).setAlignment(FlowLayout.LEFT);
		sP.add(speciesPanel);
		JLabel encodingsLabel = new JLabel("Species Levels:");
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
		JPanel thresholdPanel1 = new JPanel(new GridLayout(4, 2));
		JPanel thresholdPanel2 = new JPanel(new GridLayout(8, 2));
		JLabel activationLabel = new JLabel("Ratio For Activation (Ta):");
		thresholdPanel2.add(activationLabel);
		activation = new JTextField("1.15");
		// activation.addActionListener(this);
		thresholdPanel2.add(activation);
		JLabel repressionLabel = new JLabel("Ratio For Repression (Tr):");
		thresholdPanel2.add(repressionLabel);
		repression = new JTextField("0.75");
		// repression.addActionListener(this);
		thresholdPanel2.add(repression);
		JLabel influenceLevelLabel = new JLabel("Merge Influence Vectors Delta (Tm):");
		thresholdPanel2.add(influenceLevelLabel);
		influenceLevel = new JTextField("0.0");
		// influenceLevel.addActionListener(this);
		thresholdPanel2.add(influenceLevel);
		JLabel letNThroughLabel = new JLabel("Minimum Number of Initial Vectors (Tn):  ");
		thresholdPanel1.add(letNThroughLabel);
		letNThrough = new JTextField("2");
		// letNThrough.addActionListener(this);
		thresholdPanel1.add(letNThrough);
		JLabel maxVectorSizeLabel = new JLabel("Maximum Influence Vector Size (Tj):");
		thresholdPanel1.add(maxVectorSizeLabel);
		maxVectorSize = new JTextField("2");
		// maxVectorSize.addActionListener(this);
		thresholdPanel1.add(maxVectorSize);
		JLabel parentLabel = new JLabel("Score for Empty Influence Vector (Ti):");
		thresholdPanel1.add(parentLabel);
		parent = new JTextField("0.5");
		parent.addActionListener(this);
		thresholdPanel1.add(parent);
		JLabel relaxIPDeltaLabel = new JLabel("Relax Thresholds Delta (Tt):");
		thresholdPanel2.add(relaxIPDeltaLabel);
		relaxIPDelta = new JTextField("0.025");
		// relaxIPDelta.addActionListener(this);
		thresholdPanel2.add(relaxIPDelta);
		numBinsLabel = new JLabel("Number Of Bins:");
		String[] bins = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		numBins = new JComboBox(bins);
		numBins.setSelectedItem("4");
		numBins.addActionListener(this);
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
		debug.addActionListener(this);
		thresholdPanel2.add(debugLabel);
		thresholdPanel2.add(debug);
		succ = new JRadioButton("Successors");
		pred = new JRadioButton("Predecessors");
		both = new JRadioButton("Both");
		succ.setSelected(true);
		succ.addActionListener(this);
		pred.addActionListener(this);
		both.addActionListener(this);
		basicFBP = new JCheckBox("Basic FindBaseProb");
		basicFBP.addActionListener(this);
		ButtonGroup succOrPred = new ButtonGroup();
		succOrPred.add(succ);
		succOrPred.add(pred);
		succOrPred.add(both);
		JPanel three = new JPanel();
		three.add(succ);
		three.add(pred);
		three.add(both);
		((FlowLayout) three.getLayout()).setAlignment(FlowLayout.LEFT);
		thresholdPanel2.add(three);
		thresholdPanel2.add(new JPanel());
		thresholdPanel2.add(basicFBP);
		thresholdPanel2.add(new JPanel());
		JPanel thresholdPanelHold2 = new JPanel();
		thresholdPanelHold2.add(thresholdPanel2);
		/*
		 * JLabel windowRisingLabel = new JLabel("Window Rising Amount:");
		 * windowRising = new JTextField("1");
		 * thresholdPanel2.add(windowRisingLabel);
		 * thresholdPanel2.add(windowRising); JLabel windowSizeLabel = new
		 * JLabel("Window Size:"); windowSize = new JTextField("1");
		 * thresholdPanel2.add(windowSizeLabel); thresholdPanel2.add(windowSize);
		 * harshenBoundsOnTie = new JCheckBox("Harshen Bounds On Tie");
		 * harshenBoundsOnTie.setSelected(true); donotInvertSortOrder = new
		 * JCheckBox("Do Not Invert Sort Order");
		 * donotInvertSortOrder.setSelected(true); seedParents = new
		 * JCheckBox("Parents Should Be Ranked By Score");
		 * seedParents.setSelected(true); mustNotWinMajority = new JCheckBox("Must
		 * Not Win Majority"); mustNotWinMajority.setSelected(true);
		 * donotTossSingleRatioParents = new JCheckBox("Single Ratio Parents Should
		 * Be Kept"); donotTossChangedInfluenceSingleParents = new JCheckBox(
		 * "Parents That Change Influence Should Not Be Tossed");
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
			if (load.containsKey("genenet.file")) {
				learnFile = load.getProperty("genenet.file");
			}
			if (load.containsKey("genenet.Tn")) {
				letNThrough.setText(load.getProperty("genenet.Tn"));
			}
			if (load.containsKey("genenet.Tj")) {
				maxVectorSize.setText(load.getProperty("genenet.Tj"));
			}
			if (load.containsKey("genenet.Ti")) {
				parent.setText(load.getProperty("genenet.Ti"));
			}
			if (load.containsKey("genenet.Ta")) {
				activation.setText(load.getProperty("genenet.Ta"));
			}
			if (load.containsKey("genenet.Tr")) {
				repression.setText(load.getProperty("genenet.Tr"));
			}
			if (load.containsKey("genenet.Tm")) {
				influenceLevel.setText(load.getProperty("genenet.Tm"));
			}
			if (load.containsKey("genenet.Tt")) {
				relaxIPDelta.setText(load.getProperty("genenet.Tt"));
			}
			if (load.containsKey("genenet.bins")) {
				numBins.setSelectedItem(load.getProperty("genenet.bins"));
			}
			if (load.containsKey("genenet.debug")) {
				debug.setSelectedItem(load.getProperty("genenet.debug"));
			}
			if (load.containsKey("genenet.equal")) {
				if (load.getProperty("genenet.equal").equals("data")) {
					data.setSelected(true);
				}
				else {
					spacing.setSelected(true);
				}
			}
			if (load.containsKey("genenet.use")) {
				if (load.getProperty("genenet.use").equals("auto")) {
					auto.setSelected(true);
				}
				else {
					user.setSelected(true);
				}
			}
			if (load.containsKey("genenet.find.base.prob")) {
				if (load.getProperty("genenet.find.base.prob").equals("true")) {
					basicFBP.setSelected(true);
				}
			}
			if (load.containsKey("genenet.data.type")) {
				if (load.getProperty("genenet.data.type").equals("succ")) {
					succ.setSelected(true);
				}
				else if (load.getProperty("genenet.data.type").equals("pred")) {
					pred.setSelected(true);
				}
				else {
					both.setSelected(true);
				}
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}

		// Creates the run button
		save = new JButton("Save Parameters");
		JPanel runHolder = new JPanel();
		runHolder.add(save);
		save.addActionListener(this);
		save.setMnemonic(KeyEvent.VK_S);

		// Creates the run button
		run = new JButton("Save and Learn");
		runHolder.add(run);
		run.addActionListener(this);
		run.setMnemonic(KeyEvent.VK_L);

		// Creates the view circuit button
		viewGcm = new JButton("View Circuit");
		runHolder.add(viewGcm);
		viewGcm.addActionListener(this);
		viewGcm.setMnemonic(KeyEvent.VK_V);

		// Creates the save circuit button
		saveGcm = new JButton("Save Circuit");
		runHolder.add(saveGcm);
		saveGcm.addActionListener(this);
		saveGcm.setMnemonic(KeyEvent.VK_C);

		// Creates the view circuit button
		viewLog = new JButton("View Run Log");
		runHolder.add(viewLog);
		viewLog.addActionListener(this);
		viewLog.setMnemonic(KeyEvent.VK_R);
		if (!(new File(directory + separator + "method.gcm").exists())) {
			viewGcm.setEnabled(false);
			saveGcm.setEnabled(false);
		}
		if (!(new File(directory + separator + "run.log").exists())) {
			viewLog.setEnabled(false);
		}

		// Creates the main panel
		this.setLayout(new BorderLayout());
		JPanel middlePanel = new JPanel(new BorderLayout());
		JPanel firstTab = new JPanel(new BorderLayout());
		JPanel firstTab1 = new JPanel(new BorderLayout());
		JPanel secondTab = new JPanel(new BorderLayout());
		middlePanel.add(radioPanel, "Center");
		// firstTab1.add(initNet, "North");
		firstTab1.add(thresholdPanelHold1, "Center");
		firstTab.add(firstTab1, "North");
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, middlePanel, null);
		splitPane.setDividerSize(0);
		secondTab.add(thresholdPanelHold2, "North");
		firstTab.add(splitPane, "Center");

		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Basic Options", firstTab);
		tab.addTab("Advanced Options", secondTab);
		this.add(tab, "Center");
		this.add(runHolder, "South");
		if (user.isSelected()) {
			auto.doClick();
			user.doClick();
			levels(true);
		}
		else {
			user.doClick();
			auto.doClick();
		}
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
		 * (!((JCheckBox) this.species.get(num).get(0)).isSelected()) { ((JComboBox)
		 * this.species.get(num).get(2)).setSelectedItem("0"); editText(num);
		 * speciesPanel.revalidate(); speciesPanel.repaint(); for (int i = 1; i <
		 * this.species.get(num).size(); i++) {
		 * this.species.get(num).get(i).setEnabled(false); } } else {
		 * this.species.get(num).get(1).setEnabled(true); if (user.isSelected()) {
		 * for (int i = 2; i < this.species.get(num).size(); i++) {
		 * this.species.get(num).get(i).setEnabled(true); } } } } else
		 */
		change = true;
		if (e.getActionCommand().contains("text")) {
			int num = Integer.parseInt(e.getActionCommand().substring(4)) - 1;
			editText(num);
			speciesPanel.revalidate();
			speciesPanel.repaint();
		}
		else if (e.getSource() == user) {
			numBinsLabel.setEnabled(false);
			numBins.setEnabled(false);
			suggest.setEnabled(true);
			levelsBin();
			speciesPanel.revalidate();
			speciesPanel.repaint();
		}
		else if (e.getSource() == auto) {
			numBinsLabel.setEnabled(true);
			numBins.setEnabled(true);
			suggest.setEnabled(false);
			for (Component c : speciesPanel.getComponents()) {
				for (int i = 1; i < ((JPanel) c).getComponentCount(); i++) {
					((JPanel) c).getComponent(i).setEnabled(false);
				}
			}
		}
		else if (e.getSource() == suggest) {
			levels(false);
			speciesPanel.revalidate();
			speciesPanel.repaint();
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
		else if (e.getSource() == viewGcm) {
			viewGcm();
		}
		else if (e.getSource() == viewLog) {
			viewLog();
		}
		else if (e.getSource() == saveGcm) {
			saveGcm();
		}
	}

	private void levels(boolean readfile) {
		ArrayList<String> str = null;
		try {
			if (!readfile) {
				FileWriter write = new FileWriter(new File(directory + separator + "levels.lvl"));
				write.write("time, 0\n");
				for (int i = 0; i < species.size(); i++) {
					if (((JTextField) species.get(i).get(0)).getText().trim().equals("")) {
						write.write("-1");
					}
					else {
						write.write(((JTextField) species.get(i).get(0)).getText().trim());
					}
					write.write(", " + ((JComboBox) species.get(i).get(1)).getSelectedItem());
					for (int j = 2; j < species.get(i).size(); j++) {
						if (((JTextField) species.get(i).get(j)).getText().trim().equals("")) {
							write.write(", -1");
						}
						else {
							write.write(", " + ((JTextField) species.get(i).get(j)).getText().trim());
						}
					}
					write.write("\n");
				}
				write.close();
				String geneNet = "";
				if (spacing.isSelected()) {
					geneNet = "GeneNet --readLevels --lvl -binN .";
				}
				else {
					geneNet = "GeneNet --readLevels --lvl .";
				}
				log.addText("Executing:\n" + geneNet + " " + directory + "\n");
				Runtime exec = Runtime.getRuntime();
				File work = new File(directory);
				Process learn = exec.exec(geneNet, null, work);
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
			Scanner f = new Scanner(new File(directory + separator + "levels.lvl"));
			str = new ArrayList<String>();
			while (f.hasNextLine()) {
				str.add(f.nextLine());
			}
		}
		catch (Exception e1) {
		}
		if (!directory.equals("")) {
			File n = null;
			for (File f : new File(directory).listFiles()) {
				if (f.getAbsolutePath().contains(".tsd")) {
					n = f;
				}
			}
			if (n != null) {
				ArrayList<String> species = new ArrayList<String>();
				try {
					InputStream input = new FileInputStream(n);
					boolean reading = true;
					char cha;
					while (reading) {
						String word = "";
						boolean readWord = true;
						while (readWord) {
							int read = input.read();
							if (read == -1) {
								reading = false;
								readWord = false;
							}
							cha = (char) read;
							if (Character.isWhitespace(cha)) {
								word += cha;
							}
							else if (cha == ',' || cha == ':' || cha == ';' || cha == '\"' || cha == '\''
									|| cha == '(' || cha == ')' || cha == '[' || cha == ']') {
								if (!word.equals("") && !word.equals("time")) {
									try {
										Double.parseDouble(word);
									}
									catch (Exception e2) {
										species.add(word);
									}
								}
								word = "";
							}
							else if (read != -1) {
								word += cha;
							}
						}
					}
					input.close();
				}
				catch (Exception e1) {
				}
				speciesPanel.removeAll();
				this.species = new ArrayList<ArrayList<Component>>();
				speciesPanel.setLayout(new GridLayout(species.size() + 1, 1));
				int max = 0;
				if (str != null) {
					for (String st : str) {
						String[] getString = st.split(",");
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
				speciesPanel.add(label);
				int j = 0;
				for (String s : species) {
					j++;
					JPanel sp = new JPanel(new GridLayout());
					ArrayList<Component> specs = new ArrayList<Component>();
					// JCheckBox check = new JCheckBox();
					// check.setSelected(true);
					// specs.add(check);
					specs.add(new JTextField(s));
					String[] options = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
					specs.add(new JComboBox(options));
					((JTextField) specs.get(0)).setEditable(false);
					// sp.add(specs.get(0));
					// ((JCheckBox) specs.get(0)).addActionListener(this);
					// ((JCheckBox) specs.get(0)).setActionCommand("box" + j);
					sp.add(specs.get(0));
					sp.add(specs.get(1));
					((JComboBox) specs.get(1)).addActionListener(this);
					((JComboBox) specs.get(1)).setActionCommand("text" + j);
					this.species.add(specs);
					if (str != null) {
						for (String st : str) {
							String[] getString = st.split(",");
							if (getString[0].trim().equals(s)) {
								if (getString.length >= 2) {
									((JComboBox) specs.get(1)).setSelectedItem(getString[1].trim());
									for (int i = 0; i < Integer.parseInt((String) ((JComboBox) specs.get(1))
											.getSelectedItem()) - 1; i++) {
										specs.add(new JTextField(getString[i + 2].trim()));
										sp.add(specs.get(i + 2));
									}
									for (int i = Integer.parseInt((String) ((JComboBox) specs.get(1))
											.getSelectedItem()) - 1; i < max - 3; i++) {
										sp.add(new JLabel());
									}
								}
							}
						}
					}
					speciesPanel.add(sp);
				}
			}
		}
		editText(0);
	}

	private void levelsBin() {
		if (!directory.equals("")) {
			File n = null;
			for (File f : new File(directory).listFiles()) {
				if (f.getAbsolutePath().contains(".tsd")) {
					n = f;
				}
			}
			if (n != null) {
				ArrayList<String> species = new ArrayList<String>();
				try {
					InputStream input = new FileInputStream(n);
					boolean reading = true;
					char cha;
					while (reading) {
						String word = "";
						boolean readWord = true;
						while (readWord) {
							int read = input.read();
							if (read == -1) {
								reading = false;
								readWord = false;
							}
							cha = (char) read;
							if (Character.isWhitespace(cha)) {
								word += cha;
							}
							else if (cha == ',' || cha == ':' || cha == ';' || cha == '\"' || cha == '\''
									|| cha == '(' || cha == ')' || cha == '[' || cha == ']') {
								if (!word.equals("") && !word.equals("time")) {
									try {
										Double.parseDouble(word);
									}
									catch (Exception e2) {
										species.add(word);
									}
								}
								word = "";
							}
							else if (read != -1) {
								word += cha;
							}
						}
					}
					input.close();
				}
				catch (Exception e1) {
				}
				speciesPanel.removeAll();
				this.species = new ArrayList<ArrayList<Component>>();
				speciesPanel.setLayout(new GridLayout(species.size() + 1, 1));
				JPanel label = new JPanel(new GridLayout());
				// label.add(new JLabel("Use"));
				label.add(new JLabel("Species"));
				label.add(new JLabel("Number Of Bins"));
				for (int i = 0; i < Integer.parseInt((String) numBins.getSelectedItem()) - 1; i++) {
					label.add(new JLabel("Level " + (i + 1)));
				}
				speciesPanel.add(label);
				int j = 0;
				for (String s : species) {
					j++;
					JPanel sp = new JPanel(new GridLayout());
					ArrayList<Component> specs = new ArrayList<Component>();
					// JCheckBox check = new JCheckBox();
					// check.setSelected(true);
					// specs.add(check);
					specs.add(new JTextField(s));
					String[] options = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
					JComboBox combo = new JComboBox(options);
					specs.add(combo);
					combo.setSelectedItem(numBins.getSelectedItem());
					((JTextField) specs.get(0)).setEditable(false);
					// sp.add(specs.get(0));
					// ((JCheckBox) specs.get(0)).addActionListener(this);
					// ((JCheckBox) specs.get(0)).setActionCommand("box" + j);
					sp.add(specs.get(0));
					sp.add(specs.get(1));
					((JComboBox) specs.get(1)).addActionListener(this);
					((JComboBox) specs.get(1)).setActionCommand("text" + j);
					this.species.add(specs);
					for (int i = 0; i < Integer.parseInt((String) ((JComboBox) specs.get(1))
							.getSelectedItem()) - 1; i++) {
						specs.add(new JTextField(""));
						sp.add(specs.get(i + 2));
					}
					speciesPanel.add(sp);
				}
			}
		}
	}

	private void editText(int num) {
		ArrayList<Component> specs = species.get(num);
		try {
			Component[] panels = speciesPanel.getComponents();
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
			for (int i = 0; i < this.species.size(); i++) {
				max = Math.max(max, species.get(i).size());
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

	public void setDirectory(String newDirectory) {
		directory = newDirectory;
	}

	public void saveGcm() {
		try {
			if (new File(directory + separator + "method.gcm").exists()) {
				String copy = JOptionPane.showInputDialog(biosim.frame(), "Enter Circuit Name:",
						"Save Circuit", JOptionPane.PLAIN_MESSAGE);
				if (copy != null) {
					copy = copy.trim();
				}
				else {
					return;
				}
				if (!copy.equals("")) {
					if (copy.length() > 3) {
						if (!copy.substring(copy.length() - 4).equals(".gcm")) {
							copy += ".gcm";
						}
					}
					else {
						copy += ".gcm";
					}
				}
				biosim.saveGcm(copy, directory + separator + "method.gcm");
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

	public void viewGcm() {
		try {
			File work = new File(directory);
			if (new File(directory + separator + "method.gcm").exists()) {
				String command = "dotty method.gcm";
				log.addText("Executing:\n" + "dotty " + directory + separator + "method.gcm\n");
				Runtime exec = Runtime.getRuntime();
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
			prop.setProperty("genenet.file", learnFile);
			prop.setProperty("genenet.Tn", this.letNThrough.getText().trim());
			prop.setProperty("genenet.Tj", this.maxVectorSize.getText().trim());
			prop.setProperty("genenet.Ti", this.parent.getText().trim());
			prop.setProperty("genenet.Ta", this.activation.getText().trim());
			prop.setProperty("genenet.Tr", this.repression.getText().trim());
			prop.setProperty("genenet.Tm", this.influenceLevel.getText().trim());
			prop.setProperty("genenet.Tt", this.relaxIPDelta.getText().trim());
			prop.setProperty("genenet.bins", (String) this.numBins.getSelectedItem());
			prop.setProperty("genenet.debug", (String) this.debug.getSelectedItem());
			if (spacing.isSelected()) {
				prop.setProperty("genenet.equal", "spacing");
			}
			else {
				prop.setProperty("genenet.equal", "data");
			}
			if (auto.isSelected()) {
				prop.setProperty("genenet.use", "auto");
			}
			else {
				prop.setProperty("genenet.use", "user");
			}
			if (succ.isSelected()) {
				prop.setProperty("genenet.data.type", "succ");
			}
			else if (pred.isSelected()) {
				prop.setProperty("genenet.data.type", "pred");
			}
			else {
				prop.setProperty("genenet.data.type", "both");
			}
			if (basicFBP.isSelected()) {
				prop.setProperty("genenet.find.base.prob", "true");
			}
			else {
				prop.setProperty("genenet.find.base.prob", "false");
			}
			FileOutputStream out = new FileOutputStream(new File(directory + separator + lrnFile));
			prop.store(out, learnFile);
			out.close();
			FileWriter write = new FileWriter(new File(directory + separator + "levels.lvl"));
			write.write("time, 0\n");
			for (int i = 0; i < species.size(); i++) {
				if (((JTextField) species.get(i).get(0)).getText().trim().equals("")) {
					write.write("-1");
				}
				else {
					write.write(((JTextField) species.get(i).get(0)).getText().trim());
				}
				write.write(", " + ((JComboBox) species.get(i).get(1)).getSelectedItem());
				for (int j = 2; j < species.get(i).size(); j++) {
					if (((JTextField) species.get(i).get(j)).getText().trim().equals("")) {
						write.write(", -1");
					}
					else {
						write.write(", " + ((JTextField) species.get(i).get(j)).getText().trim());
					}
				}
				write.write("\n");
			}
			write.close();
			change = false;
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to save parameter file!",
					"Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void run() {
		try {
			String geneNet = "GeneNet";
			geneNet += " --debug " + debug.getSelectedItem();
			try {
				double activation = Double.parseDouble(this.activation.getText().trim());
				geneNet += " -ta " + activation;
				double repression = Double.parseDouble(this.repression.getText().trim());
				geneNet += " -tr " + repression;
				double parent = Double.parseDouble(this.parent.getText().trim());
				geneNet += " -ti " + parent;
				// int windowRising =
				// Integer.parseInt(this.windowRising.getText().trim());
				// geneNet += " --windowRisingAmount " + windowRising;
				// int windowSize =
				// Integer.parseInt(this.windowSize.getText().trim());
				// geneNet += " --windowSize " + windowSize;
				int numBins = Integer.parseInt((String) this.numBins.getSelectedItem());
				geneNet += " --numBins " + numBins;
				double influenceLevel = Double.parseDouble(this.influenceLevel.getText().trim());
				geneNet += " -tm " + influenceLevel;
				double relaxIPDelta = Double.parseDouble(this.relaxIPDelta.getText().trim());
				geneNet += " -tt " + relaxIPDelta;
				int letNThrough = Integer.parseInt(this.letNThrough.getText().trim());
				geneNet += " -tn " + letNThrough;
				int maxVectorSize = Integer.parseInt(this.maxVectorSize.getText().trim());
				geneNet += " -tj " + maxVectorSize;
				if (succ.isSelected()) {
				}
				if (pred.isSelected()) {
					geneNet += " -noSUCC -PRED";
				}
				if (both.isSelected()) {
					geneNet += " -PRED";
				}
				if (basicFBP.isSelected()) {
					geneNet += " -basicFBP";
				}
			}
			catch (Exception e2) {
				JOptionPane.showMessageDialog(this, "Must enter numbers into input fields.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			if (user.isSelected()) {
				FileWriter write = new FileWriter(new File(directory + separator + "levels.lvl"));
				write.write("time, 0\n");
				for (int i = 0; i < species.size(); i++) {
					if (((JTextField) species.get(i).get(0)).getText().trim().equals("")) {
						write.write("-1");
					}
					else {
						write.write(((JTextField) species.get(i).get(0)).getText().trim());
					}
					write.write(", " + ((JComboBox) species.get(i).get(1)).getSelectedItem());
					for (int j = 2; j < species.get(i).size(); j++) {
						if (((JTextField) species.get(i).get(j)).getText().trim().equals("")) {
							write.write(", -1");
						}
						else {
							write.write(", " + ((JTextField) species.get(i).get(j)).getText().trim());
						}
					}
					write.write("\n");
				}
				write.close();
				geneNet += " --readLevels";
			}
			geneNet += " --cpp_harshenBoundsOnTie --cpp_cmp_output_donotInvertSortOrder --cpp_seedParents --cmp_score_mustNotWinMajority";
			/*
			 * if (harshenBoundsOnTie.isSelected()) { geneNet += "
			 * --cpp_harshenBoundsOnTie"; } if (donotInvertSortOrder.isSelected()) {
			 * geneNet += " --cpp_cmp_output_donotInvertSortOrder"; } if
			 * (seedParents.isSelected()) { geneNet += " --cpp_seedParents"; } if
			 * (mustNotWinMajority.isSelected()) { geneNet += "
			 * --cmp_score_mustNotWinMajority"; } if
			 * (donotTossSingleRatioParents.isSelected()) { geneNet += "
			 * --score_donotTossSingleRatioParents"; } if
			 * (donotTossChangedInfluenceSingleParents.isSelected()) { geneNet += "
			 * --output_donotTossChangedInfluenceSingleParents"; }
			 */
			if (spacing.isSelected()) {
				geneNet += " -binN";
			}
			final JButton cancel = new JButton("Cancel");
			final JFrame running = new JFrame("Running...");
			WindowListener w = new WindowListener() {
				public void windowClosing(WindowEvent arg0) {
					cancel.doClick();
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
			JLabel label = new JLabel("Progress");
			JProgressBar progress = new JProgressBar(0, species.size());
			progress.setStringPainted(true);
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
			}
			catch (AWTError awe) {
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
			Runtime exec = Runtime.getRuntime();
			log.addText("Executing:\n" + geneNet + " " + directory + "\n");
			geneNet += " .";
			File work = new File(directory);
			final Process learn = exec.exec(geneNet, null, work);
			cancel.setActionCommand("Cancel");
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					learn.destroy();
					running.setCursor(null);
					running.dispose();
				}
			});
			biosim.getExitButton().setActionCommand("Exit program");
			biosim.getExitButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					learn.destroy();
					running.setCursor(null);
					running.dispose();
				}
			});
			try {
				String output = "";
				InputStream reb = learn.getInputStream();
				InputStreamReader isr = new InputStreamReader(reb);
				BufferedReader br = new BufferedReader(isr);
				FileWriter out = new FileWriter(new File(directory + separator + "run.log"));
				int count = 0;
				while ((output = br.readLine()) != null) {
					if (output.startsWith("Gene = ", 0)) {
						// log.addText(output);
						count++;
						progress.setValue(count);
					}
					out.write(output);
					out.write("\n");
				}
				br.close();
				isr.close();
				reb.close();
				out.close();
				viewLog.setEnabled(true);
			}
			catch (Exception e) {
			}
			int exitValue = learn.waitFor();
			if (exitValue == 143) {
				JOptionPane.showMessageDialog(biosim.frame(), "Learning was" + " canceled by the user.",
						"Canceled Learning", JOptionPane.ERROR_MESSAGE);
			}
			else {
				if (new File(directory + separator + "method.gcm").exists()) {
					String command = "dotty method.gcm";
					log.addText("Executing:\n" + "dotty " + directory + separator + "method.gcm\n");
					exec.exec(command, null, work);
				}
				else {
					JOptionPane.showMessageDialog(biosim.frame(), "A gcm file was not generated."
							+ "\nPlease see the run.log file.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				running.setCursor(null);
				running.dispose();
				if (new File(directory + separator + "method.gcm").exists()) {
					viewGcm.setEnabled(true);
					saveGcm.setEnabled(true);
				}
				if (new File(directory + separator + "run.log").exists()) {
					viewLog.setEnabled(true);
				}
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to learn from data.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean hasChanged() {
		return change;
	}
}
