package learn.core.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import biomodelsim.core.gui.*;
import buttons.core.gui.*;

/**
 * This class creates a GUI for the Learn program. It implements the
 * ActionListener class. This allows the GUI to perform actions when menu items
 * and buttons are selected.
 * 
 * @author Curtis Madsen
 */
public class Learn extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5806315070287184299L;

	private JTextField initNetwork; // text field for initial network

	private JButton browseInit; // the browse initial network button

	private JButton run; // the run button

	private JComboBox debug; // debug combo box

	private JTextField activation, repression, parent;

	private JTextField background;

	// private JTextField windowRising, windowSize;

	private JComboBox numBins;

	private JTextField influenceLevel, relaxIPDelta, letNThrough, maxVectorSize;

	// private JCheckBox harshenBoundsOnTie, donotInvertSortOrder, seedParents;

	// private JCheckBox mustNotWinMajority, donotTossSingleRatioParents,
	// donotTossChangedInfluenceSingleParents;

	private JCheckBox noSUCC, PRED, basicFBP;

	private ArrayList<ArrayList<Component>> species;

	private JPanel speciesPanel;

	private JRadioButton user, auto, spacing, data;

	private JButton suggest;

	private String directory;

	private JLabel numBinsLabel;

	private Log log;

	/**
	 * This is the constructor for the Learn class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public Learn(String directory, Log log) {
		this.log = log;
		this.directory = directory;

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
		JPanel initNet = new JPanel();
		JLabel initNetLabel = new JLabel("Background Knowledge Network:");
		browseInit = new JButton("Browse");
		browseInit.addActionListener(this);
		initNetwork = new JTextField(39);
		initNet.add(initNetLabel);
		initNet.add(initNetwork);
		initNet.add(browseInit);

		// Sets up the thresholds area
		JPanel thresholdPanel1 = new JPanel(new GridLayout(3, 2));
		JPanel thresholdPanel2 = new JPanel(new GridLayout(10, 2));
		JLabel activationLabel = new JLabel("For Vote Threshold (Tf):");
		activation = new JTextField("1.33");
		thresholdPanel2.add(activationLabel);
		thresholdPanel2.add(activation);
		JLabel repressionLabel = new JLabel("Against Vote Threshold (Ta):");
		repression = new JTextField("0.75");
		thresholdPanel2.add(repressionLabel);
		thresholdPanel2.add(repression);
		JLabel parentLabel = new JLabel("First Round Influence Vector Threshold (Ti):");
		parent = new JTextField("0.5");
		thresholdPanel2.add(parentLabel);
		thresholdPanel2.add(parent);
		JLabel backgroundLabel = new JLabel(
				"Pre-Competition Influence Vector Filter Threshold (Tp):  ");
		background = new JTextField("0.51");
		thresholdPanel2.add(backgroundLabel);
		thresholdPanel2.add(background);
		JLabel influenceLevelLabel = new JLabel("Merge Influence Vectors Delta (Tm):");
		influenceLevel = new JTextField("0.01");
		thresholdPanel2.add(influenceLevelLabel);
		thresholdPanel2.add(influenceLevel);
		JLabel letNThroughLabel = new JLabel("Minimum Initial Vectors (Tn):");
		letNThrough = new JTextField("1");
		thresholdPanel1.add(letNThroughLabel);
		thresholdPanel1.add(letNThrough);
		JLabel maxVectorSizeLabel = new JLabel("Max Influence Vector Size (Tj):  ");
		maxVectorSize = new JTextField("2");
		thresholdPanel1.add(maxVectorSizeLabel);
		thresholdPanel1.add(maxVectorSize);
		JLabel relaxIPDeltaLabel = new JLabel("Relax Thresholds Delta (Tr):");
		relaxIPDelta = new JTextField("0.025");
		thresholdPanel2.add(relaxIPDeltaLabel);
		thresholdPanel2.add(relaxIPDelta);
		numBinsLabel = new JLabel("Number Of Bins:");
		String[] bins = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		numBins = new JComboBox(bins);
		numBins.setSelectedItem("3");
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
		thresholdPanel2.add(debugLabel);
		thresholdPanel2.add(debug);
		noSUCC = new JCheckBox("Do not use successors when calculating probabilities");
		PRED = new JCheckBox("Use predecessors when calculating probabilities");
		basicFBP = new JCheckBox("Use the basic implementation of the FindBaseProb function");
		thresholdPanel2.add(noSUCC);
		thresholdPanel2.add(new JPanel());
		thresholdPanel2.add(PRED);
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

		// Creates the run button
		run = new JButton("Learn");
		JPanel runHolder = new JPanel();
		runHolder.add(run);
		run.addActionListener(this);
		run.setMnemonic(KeyEvent.VK_L);

		// Creates the main panel
		this.setLayout(new BorderLayout());
		JPanel middlePanel = new JPanel(new BorderLayout());
		JPanel firstTab = new JPanel(new BorderLayout());
		JPanel firstTab1 = new JPanel(new BorderLayout());
		JPanel secondTab = new JPanel(new BorderLayout());
		middlePanel.add(radioPanel, "Center");
		firstTab1.add(initNet, "North");
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
		user.doClick();
		auto.doClick();
	}

	/**
	 * This method performs different functions depending on what menu items or
	 * buttons are selected.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().contains("box")) {
			int num = Integer.parseInt(e.getActionCommand().substring(3)) - 1;
			if (!((JCheckBox) this.species.get(num).get(0)).isSelected()) {
				((JComboBox) this.species.get(num).get(2)).setSelectedItem("0");
				editText(num);
				speciesPanel.revalidate();
				speciesPanel.repaint();
				for (int i = 1; i < this.species.get(num).size(); i++) {
					this.species.get(num).get(i).setEnabled(false);
				}
			} else {
				this.species.get(num).get(1).setEnabled(true);
				if (user.isSelected()) {
					for (int i = 2; i < this.species.get(num).size(); i++) {
						this.species.get(num).get(i).setEnabled(true);
					}
				}
			}
		} else if (e.getActionCommand().contains("text")) {
			int num = Integer.parseInt(e.getActionCommand().substring(4)) - 1;
			editText(num);
			speciesPanel.revalidate();
			speciesPanel.repaint();
		} else if (e.getSource() == user) {
			numBinsLabel.setEnabled(false);
			numBins.setEnabled(false);
			suggest.setEnabled(true);
			levelsBin();
			speciesPanel.revalidate();
			speciesPanel.repaint();
		} else if (e.getSource() == auto) {
			numBinsLabel.setEnabled(true);
			numBins.setEnabled(true);
			suggest.setEnabled(false);
			for (Component c : speciesPanel.getComponents()) {
				for (int i = 2; i < ((JPanel) c).getComponentCount(); i++) {
					((JPanel) c).getComponent(i).setEnabled(false);
				}
			}
		} else if (e.getSource() == suggest) {
			levels();
			speciesPanel.revalidate();
			speciesPanel.repaint();
		}
		// if the browse initial network button is clicked
		else if (e.getSource() == browseInit) {
			Buttons.browse(this, new File(initNetwork.getText().trim()), initNetwork,
					JFileChooser.FILES_ONLY, "Open");
		}
		// if the run button is selected
		else if (e.getSource() == run) {
			try {
				String geneNet = "GeneNet";
				geneNet += " --debug " + debug.getSelectedItem();
				try {
					double activation = Double.parseDouble(this.activation.getText().trim());
					geneNet += " -tf " + activation;
					double repression = Double.parseDouble(this.repression.getText().trim());
					geneNet += " -ta " + repression;
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
					double influenceLevel = Double
							.parseDouble(this.influenceLevel.getText().trim());
					geneNet += " -tm " + influenceLevel;
					double relaxIPDelta = Double.parseDouble(this.relaxIPDelta.getText().trim());
					geneNet += " -tr " + relaxIPDelta;
					int letNThrough = Integer.parseInt(this.letNThrough.getText().trim());
					geneNet += " -tn " + letNThrough;
					int maxVectorSize = Integer.parseInt(this.maxVectorSize.getText().trim());
					geneNet += " -tj " + maxVectorSize;
					double background = Double.parseDouble(this.background.getText().trim());
					geneNet += " -tp " + background;
					if (noSUCC.isSelected()) {
						geneNet += " -noSUCC";
					}
					if (PRED.isSelected()) {
						geneNet += " -PRED";
					}
					if (basicFBP.isSelected()) {
						geneNet += " -basicFBP";
					}
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(this, "Must enter numbers into input fields.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				if (user.isSelected()) {
					FileWriter write = new FileWriter(new File(directory + File.separator
							+ "levels.lvl"));
					write.write("time, 0\n");
					for (int i = 0; i < species.size(); i++) {
						if (((JTextField) species.get(i).get(1)).getText().trim().equals("")) {
							write.write("-1");
						} else {
							write.write(((JTextField) species.get(i).get(1)).getText().trim());
						}
						write.write(", " + ((JComboBox) species.get(i).get(2)).getSelectedItem());
						for (int j = 3; j < species.get(i).size(); j++) {
							if (((JTextField) species.get(i).get(j)).getText().trim().equals("")) {
								write.write(", -1");
							} else {
								write.write(", "
										+ ((JTextField) species.get(i).get(j)).getText().trim());
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
				 * --cpp_harshenBoundsOnTie"; } if
				 * (donotInvertSortOrder.isSelected()) { geneNet += "
				 * --cpp_cmp_output_donotInvertSortOrder"; } if
				 * (seedParents.isSelected()) { geneNet += " --cpp_seedParents"; }
				 * if (mustNotWinMajority.isSelected()) { geneNet += "
				 * --cmp_score_mustNotWinMajority"; } if
				 * (donotTossSingleRatioParents.isSelected()) { geneNet += "
				 * --score_donotTossSingleRatioParents"; } if
				 * (donotTossChangedInfluenceSingleParents.isSelected()) {
				 * geneNet += "
				 * --output_donotTossChangedInfluenceSingleParents"; }
				 */
				if (spacing.isSelected()) {
					geneNet += " -binN";
				}
				Runtime exec = Runtime.getRuntime();
				Process learn = exec.exec(geneNet + " " + directory);
				log.addText("Executing:\n" + geneNet + " " + directory + "\n");
				learn.waitFor();
				String output = "";
				InputStream reb = learn.getInputStream();
				FileWriter out = new FileWriter(new File(directory + File.separator + "run.log"));
				int read = reb.read();
				while (read != -1) {
					output += (char) read;
					out.write((char) read);
					read = reb.read();
				}
				out.close();
				log.addText("Output:\n" + output + "\n");
				if (new File(directory + File.separator + "method.dot").exists()) {
					exec
							.exec("dotty "
									+ new File(directory + File.separator + "method.dot")
											.getAbsolutePath());
				} else {
					JOptionPane.showMessageDialog(this, "A dot file was not generated."
							+ "\nPlease see the run.log file.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this, "Unable to learn from data.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void levels() {
		ArrayList<String> str = null;
		try {
			FileWriter write = new FileWriter(new File(directory + File.separator + "levels.lvl"));
			write.write("time, 0\n");
			for (int i = 0; i < species.size(); i++) {
				if (((JTextField) species.get(i).get(1)).getText().trim().equals("")) {
					write.write("-1");
				} else {
					write.write(((JTextField) species.get(i).get(1)).getText().trim());
				}
				write.write(", " + ((JComboBox) species.get(i).get(2)).getSelectedItem());
				for (int j = 3; j < species.get(i).size(); j++) {
					if (((JTextField) species.get(i).get(j)).getText().trim().equals("")) {
						write.write(", -1");
					} else {
						write.write(", " + ((JTextField) species.get(i).get(j)).getText().trim());
					}
				}
				write.write("\n");
			}
			write.close();
			String geneNet = "";
			if (spacing.isSelected()) {
				geneNet = "GeneNet --readLevels --lvl -binN";
			} else {
				geneNet = "GeneNet --readLevels --lvl";
			}
			log.addText("Executing:\n" + geneNet + " " + directory + "\n");
			Runtime exec = Runtime.getRuntime();
			Process learn = exec.exec(geneNet + " " + directory);
			learn.waitFor();
			String output = "";
			InputStream reb = learn.getInputStream();
			FileWriter out = new FileWriter(new File(directory + File.separator + "run.log"));
			int read = reb.read();
			while (read != -1) {
				output += (char) read;
				out.write((char) read);
				read = reb.read();
			}
			out.close();
			log.addText("Output:\n" + output + "\n");
			Scanner f = new Scanner(new File(directory + File.separator + "levels.lvl"));
			str = new ArrayList<String>();
			while (f.hasNextLine()) {
				str.add(f.nextLine());
			}
		} catch (Exception e1) {
		}
		if (!directory.equals("")) {
			File n = null;
			for (File f : new File(directory).listFiles()) {
				if (f.getAbsolutePath().contains(".tsd")) {
					n = f;
				}
			}
			if (n != null) {
				InputStream input;
				ArrayList<String> species = new ArrayList<String>();
				try {
					input = new FileInputStream(n);
					boolean reading = true;
					char cha;
					int readCount = 0;
					while (reading) {
						String word = "";
						boolean readWord = true;
						while (readWord) {
							int read = input.read();
							readCount++;
							if (read == -1) {
								reading = false;
								readWord = false;
							}
							cha = (char) read;
							if (Character.isWhitespace(cha)) {
								input.mark(3);
								char next = (char) input.read();
								char after = (char) input.read();
								String check = "" + next + after;
								if (word.equals("0") || check.equals("0,")) {
									readWord = false;
								} else {
									word += cha;
								}
								input.reset();
							} else if (cha == ',' || cha == ':' || cha == ';' || cha == '!'
									|| cha == '?' || cha == '\"' || cha == '\'' || cha == '('
									|| cha == ')' || cha == '{' || cha == '}' || cha == '['
									|| cha == ']' || cha == '<' || cha == '>' || cha == '*'
									|| cha == '=' || cha == '#') {
								if (!word.equals("") && !word.equals("time")) {
									try {
										Double.parseDouble(word);
									} catch (Exception e2) {
										species.add(word);
									}
								}
								word = "";
							} else if (read != -1) {
								word += cha;
							}
						}
					}
				} catch (Exception e1) {
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
				label.add(new JLabel("Use"));
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
					JCheckBox check = new JCheckBox();
					check.setSelected(true);
					specs.add(check);
					specs.add(new JTextField(s));
					String[] options = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
					specs.add(new JComboBox(options));
					((JTextField) specs.get(1)).setEditable(false);
					sp.add(specs.get(0));
					((JCheckBox) specs.get(0)).addActionListener(this);
					((JCheckBox) specs.get(0)).setActionCommand("box" + j);
					sp.add(specs.get(1));
					sp.add(specs.get(2));
					((JComboBox) specs.get(2)).addActionListener(this);
					((JComboBox) specs.get(2)).setActionCommand("text" + j);
					this.species.add(specs);
					if (str != null) {
						for (String st : str) {
							String[] getString = st.split(",");
							if (getString[0].trim().equals(s)) {
								if (getString.length >= 2) {
									((JComboBox) specs.get(2)).setSelectedItem(getString[1].trim());
									for (int i = 0; i < Integer
											.parseInt((String) ((JComboBox) specs.get(2))
													.getSelectedItem()) - 1; i++) {
										specs.add(new JTextField(getString[i + 2].trim()));
										sp.add(specs.get(i + 3));
									}
									for (int i = Integer.parseInt((String) ((JComboBox) specs
											.get(2)).getSelectedItem()) - 1; i < max - 3; i++) {
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
				InputStream input;
				ArrayList<String> species = new ArrayList<String>();
				try {
					input = new FileInputStream(n);
					boolean reading = true;
					char cha;
					int readCount = 0;
					while (reading) {
						String word = "";
						boolean readWord = true;
						while (readWord) {
							int read = input.read();
							readCount++;
							if (read == -1) {
								reading = false;
								readWord = false;
							}
							cha = (char) read;
							if (Character.isWhitespace(cha)) {
								input.mark(3);
								char next = (char) input.read();
								char after = (char) input.read();
								String check = "" + next + after;
								if (word.equals("0") || check.equals("0,")) {
									readWord = false;
								} else {
									word += cha;
								}
								input.reset();
							} else if (cha == ',' || cha == ':' || cha == ';' || cha == '!'
									|| cha == '?' || cha == '\"' || cha == '\'' || cha == '('
									|| cha == ')' || cha == '{' || cha == '}' || cha == '['
									|| cha == ']' || cha == '<' || cha == '>' || cha == '*'
									|| cha == '=' || cha == '#') {
								if (!word.equals("") && !word.equals("time")) {
									try {
										Double.parseDouble(word);
									} catch (Exception e2) {
										species.add(word);
									}
								}
								word = "";
							} else if (read != -1) {
								word += cha;
							}
						}
					}
				} catch (Exception e1) {
				}
				speciesPanel.removeAll();
				this.species = new ArrayList<ArrayList<Component>>();
				speciesPanel.setLayout(new GridLayout(species.size() + 1, 1));
				JPanel label = new JPanel(new GridLayout());
				label.add(new JLabel("Use"));
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
					JCheckBox check = new JCheckBox();
					check.setSelected(true);
					specs.add(check);
					specs.add(new JTextField(s));
					String[] options = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
					JComboBox combo = new JComboBox(options);
					specs.add(combo);
					combo.setSelectedItem(numBins.getSelectedItem());
					((JTextField) specs.get(1)).setEditable(false);
					sp.add(specs.get(0));
					((JCheckBox) specs.get(0)).addActionListener(this);
					((JCheckBox) specs.get(0)).setActionCommand("box" + j);
					sp.add(specs.get(1));
					sp.add(specs.get(2));
					((JComboBox) specs.get(2)).addActionListener(this);
					((JComboBox) specs.get(2)).setActionCommand("text" + j);
					this.species.add(specs);
					for (int i = 0; i < Integer.parseInt((String) ((JComboBox) specs.get(2))
							.getSelectedItem()) - 1; i++) {
						specs.add(new JTextField(""));
						sp.add(specs.get(i + 3));
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
			int boxes = Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem());
			if ((specs.size() - 3) < boxes) {
				for (int i = 0; i < boxes - 1; i++) {
					try {
						specs.get(i + 3);
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
							specs.remove(boxes + 2);
							((JPanel) panels[num + 1]).remove(boxes + 2);
						}
					} else if (boxes == 0) {
						while (true) {
							specs.remove(3);
							((JPanel) panels[num + 1]).remove(3);
						}
					}
				} catch (Exception e1) {
				}
			}
			int max = 0;
			for (int i = 0; i < this.species.size(); i++) {
				max = Math.max(max, species.get(i).size());
			}
			if (((JPanel) panels[0]).getComponentCount() < max) {
				for (int i = 0; i < max - 3; i++) {
					try {
						((JPanel) panels[0]).getComponent(i + 3);
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
				for (int j = sp.getComponentCount() - 1; j >= 3; j--) {
					if (sp.getComponent(j) instanceof JLabel) {
						sp.remove(j);
					}
				}
				if (max > sp.getComponentCount()) {
					for (int j = sp.getComponentCount(); j < max; j++) {
						sp.add(new JLabel());
					}
				} else {
					for (int j = sp.getComponentCount() - 2; j >= max; j--) {
						sp.remove(j);
					}
				}
			}
		} catch (Exception e) {
		}
	}

	public void setDirectory(String newDirectory) {
		directory = newDirectory;
	}
}