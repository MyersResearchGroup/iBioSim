package learn.core.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import buttons.core.gui.*;

/**
 * This class creates a GUI for the Learn program. It implements the
 * ActionListener class. This allows the GUI to perform actions when menu items
 * and buttons are selected.
 * 
 * @author Curtis Madsen
 */
public class Learn extends JPanel implements ActionListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5806315070287184299L;

	// private JFrame frame; // Frame where components of the GUI are displayed

	private JTextField initNetwork; // text field for initial network

	private JButton browseInit; // the browse initial network button

	// private JTextField experiments; // text field for experiments directory

	// private JButton browseExper; // the browse experiments directory button

	private JButton run; // the run button

	// private JMenuItem close; // the close menu item

	private JComboBox debug; // debug combo box

	private JTextField activation, repression, parent;

	private JTextField background;

	private JTextField windowRising, windowSize, numBins;

	private JTextField influenceLevel, relaxIPDelta, letNThrough;

	private JCheckBox harshenBoundsOnTie, donotInvertSortOrder, seedParents;

	private JCheckBox mustNotWinMajority, donotTossSingleRatioParents,
			donotTossChangedInfluenceSingleParents;

	private ArrayList<ArrayList<Component>> species;

	// private ArrayList<JComboBox> numberOfLevels;

	private JPanel speciesPanel;

	private JRadioButton user, auto;

	private JButton suggest;

	private String directory;

	private JLabel numBinsLabel;

	/**
	 * This is the constructor for the Learn class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public Learn(String directory) {
		this.directory = directory;
		/*
		 * // Creates a new frame frame = new JFrame("Learn"); // Makes it so
		 * that clicking the x in the corner closes the program WindowListener w =
		 * new WindowListener() { public void windowClosing(WindowEvent arg0) {
		 * frame.dispose(); }
		 * 
		 * public void windowOpened(WindowEvent arg0) { }
		 * 
		 * public void windowClosed(WindowEvent arg0) { }
		 * 
		 * public void windowIconified(WindowEvent arg0) { }
		 * 
		 * public void windowDeiconified(WindowEvent arg0) { }
		 * 
		 * public void windowActivated(WindowEvent arg0) { }
		 * 
		 * public void windowDeactivated(WindowEvent arg0) { } };
		 * frame.addWindowListener(w);
		 */

		// Sets up the encodings area
		JPanel radioPanel = new JPanel(new BorderLayout());
		JPanel selection = new JPanel();
		user = new JRadioButton("Use User Generated Levels");
		auto = new JRadioButton("Use Auto Generated Levels");
		suggest = new JButton("Suggest Levels");
		ButtonGroup select = new ButtonGroup();
		select.add(auto);
		select.add(user);
		auto.setSelected(true);
		user.addActionListener(this);
		auto.addActionListener(this);
		suggest.addActionListener(this);
		selection.add(auto);
		selection.add(user);
		selection.add(suggest);
		JPanel encodingPanel = new JPanel(new BorderLayout());
		speciesPanel = new JPanel();
		JLabel encodingsLabel = new JLabel("Species Levels:");
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setMinimumSize(new Dimension(260, 200));
		scroll2.setPreferredSize(new Dimension(276, 132));
		scroll2.setViewportView(speciesPanel);
		radioPanel.add(selection, "North");
		radioPanel.add(encodingPanel, "Center");
		encodingPanel.add(encodingsLabel, "North");
		encodingPanel.add(scroll2, "Center");

		// Sets up initial network and experiments text fields
		JPanel initNet = new JPanel();
		// JPanel initNet2 = new JPanel();
		// JLabel expLabel = new JLabel("Experiments: ");
		JLabel initNetLabel = new JLabel("Initial Network:");
		browseInit = new JButton("Browse");
		// browseExper = new JButton("Browse");
		browseInit.addActionListener(this);
		// browseExper.addActionListener(this);
		initNetwork = new JTextField(39);
		// experiments = new JTextField(39);
		// experiments.setText(directory);
		initNet.add(initNetLabel);
		initNet.add(initNetwork);
		initNet.add(browseInit);
		// initNet2.add(expLabel);
		// initNet2.add(experiments);
		// initNet2.add(browseExper);
		// initNet.add(initNet2);

		// Sets up the thresholds area
		JPanel thresholdPanel = new JPanel(new GridLayout(14, 2));
		JLabel debugLabel = new JLabel("Debug Level:");
		String[] options = new String[4];
		options[0] = "0";
		options[1] = "1";
		options[2] = "2";
		options[3] = "3";
		debug = new JComboBox(options);
		thresholdPanel.add(debugLabel);
		thresholdPanel.add(debug);
		JLabel activationLabel = new JLabel("Activation Threshold:");
		activation = new JTextField("1.15");
		thresholdPanel.add(activationLabel);
		thresholdPanel.add(activation);
		JLabel repressionLabel = new JLabel("Repression Threshold:");
		repression = new JTextField("0.75");
		thresholdPanel.add(repressionLabel);
		thresholdPanel.add(repression);
		JLabel parentLabel = new JLabel("Score To Be A Parent:");
		parent = new JTextField("0.5");
		thresholdPanel.add(parentLabel);
		thresholdPanel.add(parent);
		JLabel windowRisingLabel = new JLabel("Window Rising Amount:");
		windowRising = new JTextField("1");
		thresholdPanel.add(windowRisingLabel);
		thresholdPanel.add(windowRising);
		JLabel windowSizeLabel = new JLabel("Window Size:");
		windowSize = new JTextField("1");
		thresholdPanel.add(windowSizeLabel);
		thresholdPanel.add(windowSize);
		JLabel influenceLevelLabel = new JLabel("Influence Level Delta:");
		influenceLevel = new JTextField("0.01");
		thresholdPanel.add(influenceLevelLabel);
		thresholdPanel.add(influenceLevel);
		JLabel relaxIPDeltaLabel = new JLabel("Relax InitialParents Delta:");
		relaxIPDelta = new JTextField("0.025");
		thresholdPanel.add(relaxIPDeltaLabel);
		thresholdPanel.add(relaxIPDelta);
		JLabel letNThroughLabel = new JLabel("Minimum Number Of SelectInitalParents:");
		letNThrough = new JTextField("1");
		thresholdPanel.add(letNThroughLabel);
		thresholdPanel.add(letNThrough);
		JLabel backgroundLabel = new JLabel("Background Knowledge Filter:");
		background = new JTextField("0.51");
		thresholdPanel.add(backgroundLabel);
		thresholdPanel.add(background);
		numBinsLabel = new JLabel("Number Of Bins:");
		numBins = new JTextField("3");
		thresholdPanel.add(numBinsLabel);
		thresholdPanel.add(numBins);
		harshenBoundsOnTie = new JCheckBox("Harshen Bounds On Tie");
		harshenBoundsOnTie.setSelected(true);
		donotInvertSortOrder = new JCheckBox("Do Not Invert Sort Order");
		donotInvertSortOrder.setSelected(true);
		seedParents = new JCheckBox("Parents Should Be Ranked By Score");
		seedParents.setSelected(true);
		mustNotWinMajority = new JCheckBox("Must Not Win Majority");
		mustNotWinMajority.setSelected(true);
		donotTossSingleRatioParents = new JCheckBox("Single Ratio Parents Should Be Kept");
		donotTossChangedInfluenceSingleParents = new JCheckBox(
				"Parents That Change Influence Should Not Be Tossed");
		thresholdPanel.add(harshenBoundsOnTie);
		thresholdPanel.add(donotInvertSortOrder);
		thresholdPanel.add(seedParents);
		thresholdPanel.add(mustNotWinMajority);
		thresholdPanel.add(donotTossSingleRatioParents);
		thresholdPanel.add(donotTossChangedInfluenceSingleParents);

		// Creates the run button
		run = new JButton("Learn");
		JPanel runHolder = new JPanel();
		runHolder.add(run);
		run.addActionListener(this);
		run.setMnemonic(KeyEvent.VK_L);

		/*
		 * // Creates a menu for the gui JMenuBar menuBar = new JMenuBar();
		 * JMenu file = new JMenu("File"); file.setMnemonic(KeyEvent.VK_F);
		 * menuBar.add(file); close = new JMenuItem("Close");
		 * close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
		 * ActionEvent.ALT_MASK)); close.setMnemonic(KeyEvent.VK_C);
		 * close.addActionListener(this); file.add(close);
		 */

		// Creates the main panel
		this.setLayout(new BorderLayout());
		JPanel middlePanel = new JPanel(new BorderLayout());
		middlePanel.add(radioPanel, "Center");
		middlePanel.add(runHolder, "South");
		this.add(initNet, "North");
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, middlePanel, null);
		splitPane.setDividerSize(0);
		this.add(thresholdPanel, "Center");
		this.add(splitPane, "South");

		/*
		 * // Packs the frame and displays it frame.setContentPane(mainPanel);
		 * frame.setJMenuBar(menuBar); frame.pack(); Dimension screenSize; try {
		 * Toolkit tk = Toolkit.getDefaultToolkit(); screenSize =
		 * tk.getScreenSize(); } catch (AWTError awe) { screenSize = new
		 * Dimension(640, 480); } Dimension frameSize = frame.getSize();
		 * 
		 * if (frameSize.height > screenSize.height) { frameSize.height =
		 * screenSize.height; } if (frameSize.width > screenSize.width) {
		 * frameSize.width = screenSize.width; } int x = screenSize.width / 2 -
		 * frameSize.width / 2; int y = screenSize.height / 2 - frameSize.height /
		 * 2; frame.setLocation(x, y); frame.setVisible(true);
		 */
	}

	/**
	 * This method performs different functions depending on what menu items or
	 * buttons are selected.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().contains("box")) {
			int num = Integer.parseInt(e.getActionCommand().substring(3)) - 1;
			if (((JCheckBox) this.species.get(num).get(0)).isSelected()) {
				((JTextField) this.species.get(num).get(2)).setText("0");
				editText(num);
				for (int i = 1; i < this.species.get(num).size(); i++) {
					this.species.get(num).get(i).setEnabled(false);
				}
			} else {
				for (int i = 1; i < this.species.get(num).size(); i++) {
					this.species.get(num).get(i).setEnabled(true);
				}
			}
		} else if (e.getSource() == user) {
			numBinsLabel.setEnabled(false);
			numBins.setEnabled(false);
			levelsBin();
		} else if (e.getSource() == auto) {
			numBinsLabel.setEnabled(true);
			numBins.setEnabled(true);
			for (Component c : speciesPanel.getComponents()) {
				for (Component d : ((JPanel) c).getComponents()) {
					d.setEnabled(false);
				}
			}
		} else if (e.getSource() == suggest) {
			levels();
		}
		// if the browse initial network button is clicked
		else if (e.getSource() == browseInit) {
			Buttons.browse(this, new File(initNetwork.getText().trim()), initNetwork,
					JFileChooser.FILES_ONLY, "Open");
		}
		/*
		 * // if the browse experiments directory button is clicked else if
		 * (e.getSource() == browseExper) { File dir; if
		 * (experiments.getText().trim().equals("")) { dir = null; } else { dir =
		 * new File(experiments.getText().trim() + File.separator + "."); }
		 * Buttons.browse(this, dir, experiments, JFileChooser.DIRECTORIES_ONLY,
		 * "Open"); levels(); }
		 */
		// if the run button is selected
		else if (e.getSource() == run) {
			try {
				String geneNet = "GeneNet";
				geneNet += " --debug " + debug.getSelectedItem();
				try {
					double activation = Double.parseDouble(this.activation.getText().trim());
					geneNet += " -a " + activation;
					double repression = Double.parseDouble(this.repression.getText().trim());
					geneNet += " -r " + repression;
					double parent = Double.parseDouble(this.parent.getText().trim());
					geneNet += " -v " + parent;
					int windowRising = Integer.parseInt(this.windowRising.getText().trim());
					geneNet += " --windowRisingAmount " + windowRising;
					int windowSize = Integer.parseInt(this.windowSize.getText().trim());
					geneNet += " --windowSize " + windowSize;
					int numBins = Integer.parseInt(this.numBins.getText().trim());
					geneNet += " --numBins " + numBins;
					double influenceLevel = Double
							.parseDouble(this.influenceLevel.getText().trim());
					geneNet += " -id " + influenceLevel;
					double relaxIPDelta = Double.parseDouble(this.relaxIPDelta.getText().trim());
					geneNet += " --relaxIPDelta " + relaxIPDelta;
					int letNThrough = Integer.parseInt(this.letNThrough.getText().trim());
					geneNet += " --sip_letNThrough " + letNThrough;
					double background = Double.parseDouble(this.background.getText().trim());
					geneNet += " -bkf " + background;
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
						for (int j = 2; j < species.get(i).size(); j++) {
							if (((JTextField) species.get(i).get(j)).getText().trim().equals("")) {
								write.write("-1");
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
				if (harshenBoundsOnTie.isSelected()) {
					geneNet += " --cpp_harshenBoundsOnTie";
				}
				if (donotInvertSortOrder.isSelected()) {
					geneNet += " --cpp_cmp_output_donotInvertSortOrder";
				}
				if (seedParents.isSelected()) {
					geneNet += " --cpp_seedParents";
				}
				if (mustNotWinMajority.isSelected()) {
					geneNet += " --cmp_score_mustNotWinMajority";
				}
				if (donotTossSingleRatioParents.isSelected()) {
					geneNet += " --score_donotTossSingleRatioParents";
				}
				if (donotTossChangedInfluenceSingleParents.isSelected()) {
					geneNet += " --output_donotTossChangedInfluenceSingleParents";
				}
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
				for (int j = 2; j < species.get(i).size(); j++) {
					if (((JTextField) species.get(i).get(j)).getText().trim().equals("")) {
						write.write("-1");
					} else {
						write.write(", " + ((JTextField) species.get(i).get(j)).getText().trim());
					}
				}
				write.write("\n");
			}
			write.close();
			Runtime exec = Runtime.getRuntime();
			Process learn = exec.exec("GeneNet --readLevels --lvl " + directory);
			learn.waitFor();
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
										Integer.parseInt(word);
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
				label.add(new JLabel("Do Not Use"));
				label.add(new JLabel("Species"));
				label.add(new JLabel("Number Of Levels"));
				for (int i = 0; i < max - 3; i++) {
					label.add(new JLabel("cutoff " + (i + 1)));
				}
				speciesPanel.add(label);
				int j = 0;
				for (String s : species) {
					j++;
					JPanel sp = new JPanel(new GridLayout());
					ArrayList<Component> specs = new ArrayList<Component>();
					specs.add(new JCheckBox());
					specs.add(new JTextField(s));
					specs.add(new JTextField());
					((JTextField) specs.get(1)).setEditable(false);
					sp.add(specs.get(0));
					((JCheckBox) specs.get(0)).addActionListener(this);
					((JCheckBox) specs.get(0)).setActionCommand("box" + j);
					sp.add(specs.get(1));
					sp.add(specs.get(2));
					((JTextField) specs.get(2)).addKeyListener(this);
					((JTextField) specs.get(2)).setName("text" + j);
					this.species.add(specs);
					if (str != null) {
						for (String st : str) {
							String[] getString = st.split(",");
							if (getString[0].trim().equals(s)) {
								if (getString.length >= 2) {
									((JTextField) specs.get(2)).setText(getString[1].trim());
									for (int i = 0; i < Integer
											.parseInt(((JTextField) specs.get(2)).getText()) - 1; i++) {
										specs.add(new JTextField(getString[i + 2].trim()));
										sp.add(specs.get(i + 3));
									}
									for (int i = Integer.parseInt(((JTextField) specs.get(2))
											.getText()) - 1; i < max - 3; i++) {
										sp.add(new JLabel());
									}
								}
							}
						}
					}
					speciesPanel.add(sp);
				}
				speciesPanel.validate();
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
										Integer.parseInt(word);
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
				label.add(new JLabel("Do Not Use"));
				label.add(new JLabel("Species"));
				label.add(new JLabel("Number Of Levels"));
				for (int i = 0; i < Integer.parseInt(numBins.getText().trim()) - 1; i++) {
					label.add(new JLabel("cutoff " + (i + 1)));
				}
				speciesPanel.add(label);
				int j = 0;
				for (String s : species) {
					j++;
					JPanel sp = new JPanel(new GridLayout());
					ArrayList<Component> specs = new ArrayList<Component>();
					specs.add(new JCheckBox());
					specs.add(new JTextField(s));
					specs.add(new JTextField(numBins.getText().trim()));
					((JTextField) specs.get(1)).setEditable(false);
					sp.add(specs.get(0));
					((JCheckBox) specs.get(0)).addActionListener(this);
					((JCheckBox) specs.get(0)).setActionCommand("box" + j);
					sp.add(specs.get(1));
					sp.add(specs.get(2));
					((JTextField) specs.get(2)).addKeyListener(this);
					((JTextField) specs.get(2)).setName("text" + j);
					this.species.add(specs);
					for (int i = 0; i < Integer.parseInt(((JTextField) specs.get(2)).getText()) - 1; i++) {
						specs.add(new JTextField(""));
						sp.add(specs.get(i + 3));
					}
					speciesPanel.add(sp);
				}
				speciesPanel.validate();
			}
		}
	}

	public void keyTyped(KeyEvent e) {
		if (((JTextField) e.getSource()).getName().contains("text")) {
			if (!((JTextField) e.getSource()).getText().trim().equals("")) {
				int num = Integer.parseInt(((JTextField) e.getSource()).getName().substring(4)) - 1;
				editText(num);
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		if (((JTextField) e.getSource()).getName().contains("text")) {
			if (!((JTextField) e.getSource()).getText().trim().equals("")) {
				int num = Integer.parseInt(((JTextField) e.getSource()).getName().substring(4)) - 1;
				editText(num);
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		if (((JTextField) e.getSource()).getName().contains("text")) {
			if (!((JTextField) e.getSource()).getText().trim().equals("")) {
				int num = Integer.parseInt(((JTextField) e.getSource()).getName().substring(4)) - 1;
				editText(num);
			}
		}
	}

	private void editText(int num) {
		ArrayList<Component> specs = species.get(num);
		try {
			Component[] panels = speciesPanel.getComponents();
			int boxes = Integer.parseInt(((JTextField) specs.get(2)).getText().trim());
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
						((JPanel) panels[0]).add(new JLabel("cutoff " + (i + 1)));
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
			speciesPanel.validate();
		} catch (Exception e) {
		}
	}
}