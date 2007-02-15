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
public class Learn implements ActionListener {

	private JFrame frame; // Frame where components of the GUI are displayed

	private JTextField initNetwork; // text field for initial network

	private JButton browseInit; // the browse initial network button

	private JTextField experiments; // text field for experiments directory

	private JButton browseExper; // the browse experiments directory button

	private JButton run; // the run button

	private JMenuItem close; // the close menu item

	private JComboBox debug; // debug combo box

	private JTextField activation, repression, parent;

	private JTextField windowRising, windowSize, numBins;

	private JTextField influenceLevel, relaxIPDelta, letNThrough;

	private JCheckBox harshenBoundsOnTie, donotInvertSortOrder, seedParents;

	private JCheckBox mustNotWinMajority, donotTossSingleRatioParents,
			donotTossChangedInfluenceSingleParents;

	private ArrayList<ArrayList<JTextField>> species;

	private ArrayList<JComboBox> numberOfLevels;

	private JPanel speciesPanel;

	private JRadioButton user, auto;

	/**
	 * This is the constructor for the Learn class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public Learn(String directory) {
		// Creates a new frame
		frame = new JFrame("Learn");

		// Makes it so that clicking the x in the corner closes the program
		WindowListener w = new WindowListener() {
			public void windowClosing(WindowEvent arg0) {
				frame.dispose();
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
		frame.addWindowListener(w);

		// Sets up the encodings area
		JPanel radioPanel = new JPanel(new BorderLayout());
		JPanel selection = new JPanel();
		user = new JRadioButton("Use User Generated Levels");
		auto = new JRadioButton("Use Auto Generated Levels");
		ButtonGroup select = new ButtonGroup();
		select.add(user);
		select.add(auto);
		user.setSelected(true);
		user.addActionListener(this);
		auto.addActionListener(this);
		selection.add(user);
		selection.add(auto);
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
		JPanel initNet = new JPanel(new GridLayout(2, 1));
		JPanel initNet1 = new JPanel();
		JPanel initNet2 = new JPanel();
		JLabel expLabel = new JLabel("Experiments:    ");
		JLabel initNetLabel = new JLabel("Initial Network:");
		browseInit = new JButton("Browse");
		browseExper = new JButton("Browse");
		browseInit.addActionListener(this);
		browseExper.addActionListener(this);
		initNetwork = new JTextField(39);
		experiments = new JTextField(39);
		experiments.setText(directory);
		levels();
		initNet1.add(initNetLabel);
		initNet1.add(initNetwork);
		initNet1.add(browseInit);
		initNet2.add(expLabel);
		initNet2.add(experiments);
		initNet2.add(browseExper);
		initNet.add(initNet1);
		initNet.add(initNet2);

		// Sets up the thresholds area
		JPanel thresholdPanel = new JPanel(new GridLayout(13, 2));
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
		JLabel numBinsLabel = new JLabel("Number Of Bins:");
		numBins = new JTextField("3");
		thresholdPanel.add(numBinsLabel);
		thresholdPanel.add(numBins);
		JLabel influenceLevelLabel = new JLabel("Influence Level Delta:");
		influenceLevel = new JTextField("0.01");
		thresholdPanel.add(influenceLevelLabel);
		thresholdPanel.add(influenceLevel);
		JLabel relaxIPDeltaLabel = new JLabel("Relax InitialParents Delta:");
		relaxIPDelta = new JTextField("0.025");
		thresholdPanel.add(relaxIPDeltaLabel);
		thresholdPanel.add(relaxIPDelta);
		JLabel letNThroughLabel = new JLabel(
				"Minimum Number Of SelectInitalParents:");
		letNThrough = new JTextField("1");
		thresholdPanel.add(letNThroughLabel);
		thresholdPanel.add(letNThrough);
		harshenBoundsOnTie = new JCheckBox("Harshen Bounds On Tie");
		harshenBoundsOnTie.setSelected(true);
		donotInvertSortOrder = new JCheckBox("Do Not Invert Sort Order");
		donotInvertSortOrder.setSelected(true);
		seedParents = new JCheckBox("Parents Should Be Ranked By Score");
		seedParents.setSelected(true);
		mustNotWinMajority = new JCheckBox("Must Not Win Majority");
		mustNotWinMajority.setSelected(true);
		donotTossSingleRatioParents = new JCheckBox(
				"Single Ratio Parents Should Be Kept");
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

		// Creates a menu for the gui
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		menuBar.add(file);
		close = new JMenuItem("Close");
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.ALT_MASK));
		close.setMnemonic(KeyEvent.VK_C);
		close.addActionListener(this);
		file.add(close);

		// Creates the main panel
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel middlePanel = new JPanel(new BorderLayout());
		middlePanel.add(thresholdPanel, "North");
		middlePanel.add(radioPanel, "South");
		mainPanel.add(initNet, "North");
		mainPanel.add(middlePanel, "Center");
		mainPanel.add(runHolder, "South");

		// Packs the frame and displays it
		frame.setContentPane(mainPanel);
		frame.setJMenuBar(menuBar);
		frame.pack();
		Dimension screenSize;
		try {
			Toolkit tk = Toolkit.getDefaultToolkit();
			screenSize = tk.getScreenSize();
		} catch (AWTError awe) {
			screenSize = new Dimension(640, 480);
		}
		Dimension frameSize = frame.getSize();

		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		int x = screenSize.width / 2 - frameSize.width / 2;
		int y = screenSize.height / 2 - frameSize.height / 2;
		frame.setLocation(x, y);
		frame.setVisible(true);
	}

	/**
	 * This method performs different functions depending on what menu items or
	 * buttons are selected.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().contains("box")) {
			String s = e.getActionCommand().substring(0,
					e.getActionCommand().length() - 3);
			for (int i = 0; i < species.size(); i++) {
				if (species.get(i).get(0).getText().equals(s)) {
					if (numberOfLevels.get(i).getSelectedItem().equals("3")) {
						species.get(i).get(3).setText("");
						species.get(i).get(3).setEnabled(false);
						species.get(i).get(4).setText("");
						species.get(i).get(4).setEnabled(false);
					} else if (numberOfLevels.get(i).getSelectedItem().equals(
							"4")) {
						species.get(i).get(3).setEnabled(true);
						species.get(i).get(4).setText("");
						species.get(i).get(4).setEnabled(false);
					} else {
						species.get(i).get(3).setEnabled(true);
						species.get(i).get(4).setEnabled(true);
					}
				}
			}
		}
		// if the close menu item is selected
		else if (e.getSource() == close) {
			frame.dispose();
		} else if (e.getSource() == user) {
			for (Component c : speciesPanel.getComponents()) {
				c.setEnabled(true);
			}
			levels();
		} else if (e.getSource() == auto) {
			for (Component c : speciesPanel.getComponents()) {
				c.setEnabled(false);
			}
		}
		// if the browse initial network button is clicked
		else if (e.getSource() == browseInit) {
			Buttons.browse(frame, new File(initNetwork.getText().trim()),
					initNetwork, JFileChooser.FILES_ONLY, "Open");
		}
		// if the browse experiments directory button is clicked
		else if (e.getSource() == browseExper) {
			File dir;
			if (experiments.getText().trim().equals("")) {
				dir = null;
			} else {
				dir = new File(experiments.getText().trim() + File.separator
						+ ".");
			}
			Buttons.browse(frame, dir, experiments,
					JFileChooser.DIRECTORIES_ONLY, "Open");
			levels();
		}
		// if the run button is selected
		else if (e.getSource() == run) {
			try {
				String geneNet = "GeneNet";
				geneNet += " -d " + debug.getSelectedItem();
				try {
					double activation = Double.parseDouble(this.activation
							.getText().trim());
					geneNet += " -a " + activation;
					double repression = Double.parseDouble(this.repression
							.getText().trim());
					geneNet += " -r " + repression;
					double parent = Double.parseDouble(this.parent.getText()
							.trim());
					geneNet += " -v " + parent;
					int windowRising = Integer.parseInt(this.windowRising
							.getText().trim());
					geneNet += " -wr " + windowRising;
					int windowSize = Integer.parseInt(this.windowSize.getText()
							.trim());
					geneNet += " -ws " + windowSize;
					int numBins = Integer.parseInt(this.numBins.getText()
							.trim());
					geneNet += " -nb " + numBins;
					double influenceLevel = Double
							.parseDouble(this.influenceLevel.getText().trim());
					geneNet += " -id " + influenceLevel;
					double relaxIPDelta = Double.parseDouble(this.relaxIPDelta
							.getText().trim());
					geneNet += " -rd " + relaxIPDelta;
					int letNThrough = Integer.parseInt(this.letNThrough
							.getText().trim());
					geneNet += " --sip_letNThrough " + letNThrough;
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(frame,
							"Must enter numbers into input fields.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				if (user.isSelected()) {
					FileWriter write = new FileWriter(new File(experiments
							.getText().trim()
							+ File.separator + "levels.lvl"));
					for (int i = 0; i < species.size(); i++) {
						write.write(species.get(i).get(0).getText() + ", "
								+ species.get(i).get(1).getText() + ", "
								+ species.get(i).get(2).getText());
						if (numberOfLevels.get(i).getSelectedItem().equals("4")) {
							write.write(", " + species.get(i).get(3).getText());
						} else if (numberOfLevels.get(i).getSelectedItem()
								.equals("5")) {
							write.write(", " + species.get(i).get(4).getText());
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
				Process learn = exec.exec(geneNet + " "
						+ experiments.getText().trim() + " > run.log");
				learn.waitFor();
				exec.exec("dotty "
						+ new File(experiments.getText().trim()
								+ File.separator + "method.dot")
								.getAbsolutePath());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame,
						"Unable to learn from data.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void levels() {
		ArrayList<String> str = null;
		try {
			Runtime exec = Runtime.getRuntime();
			Process learn = exec.exec("GeneNet --lvl "
					+ experiments.getText().trim());
			learn.waitFor();
			Scanner f = new Scanner(new File(experiments.getText().trim()
					+ File.separator + "levels.lvl"));
			str = new ArrayList<String>();
			while (f.hasNextLine()) {
				str.add(f.nextLine());
			}
		} catch (Exception e1) {
		}
		if (!experiments.getText().trim().equals("")) {
			File n = null;
			for (File f : new File(experiments.getText().trim()).listFiles()) {
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
							} else if (cha == ',' || cha == ':' || cha == ';'
									|| cha == '!' || cha == '?' || cha == '\"'
									|| cha == '\'' || cha == '(' || cha == ')'
									|| cha == '{' || cha == '}' || cha == '['
									|| cha == ']' || cha == '<' || cha == '>'
									|| cha == '*' || cha == '=' || cha == '#') {
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
				this.species = new ArrayList<ArrayList<JTextField>>();
				numberOfLevels = new ArrayList<JComboBox>();
				speciesPanel.setLayout(new GridLayout(species.size(), 6));
				for (String s : species) {
					ArrayList<JTextField> specs = new ArrayList<JTextField>();
					specs.add(new JTextField(s));
					specs.add(new JTextField());
					specs.add(new JTextField());
					specs.add(new JTextField());
					specs.add(new JTextField());
					String[] options = new String[3];
					options[0] = "3";
					options[1] = "4";
					options[2] = "5";
					JComboBox box = new JComboBox(options);
					box.addActionListener(this);
					box.setActionCommand(s + "box");
					specs.get(0).setEditable(false);
					specs.get(3).setEnabled(false);
					specs.get(4).setEnabled(false);
					speciesPanel.add(specs.get(0));
					speciesPanel.add(box);
					speciesPanel.add(specs.get(1));
					speciesPanel.add(specs.get(2));
					speciesPanel.add(specs.get(3));
					speciesPanel.add(specs.get(4));
					this.species.add(specs);
					numberOfLevels.add(box);
					if (str != null) {
						for (String st : str) {
							String[] getString = st.split(",");
							if (getString[0].trim().equals(s)) {
								if (getString.length > 2) {
									specs.get(1).setText(getString[1].trim());
									specs.get(2).setText(getString[2].trim());
								}
								if (getString.length > 3) {
									specs.get(3).setText(getString[3].trim());
									specs.get(3).setEnabled(true);
									box.setSelectedItem("4");
								}
								if (getString.length > 4) {
									specs.get(4).setText(getString[4].trim());
									specs.get(4).setEnabled(true);
									box.setSelectedItem("5");
								}
							}
						}
					}
				}
				speciesPanel.validate();
			}
		}
	}

	public static void main(String args[]) {
		new Learn(null);
	}
}