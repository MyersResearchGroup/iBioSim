package learn.core.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

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

	private JTextArea encodings; // encodings text area

	private JComboBox debug; // debug combo box

	private JTextField activation, repression, parent;

	private JTextField windowRising, windowSize, numBins;

	private JTextField influenceLevel, relaxIPDelta, letNThrough;

	private JCheckBox harshenBoundsOnTie, donotInvertSortOrder, seedParents;

	private JCheckBox mustNotWinMajority, donotTossSingleRatioParents,
			donotTossChangedInfluenceSingleParents;

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
		JLabel letNThroughLabel = new JLabel("Minimum Number Of SelectInitalParents:");
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
		donotTossSingleRatioParents = new JCheckBox("Single Ratio Parents Should Be Kept");
		donotTossChangedInfluenceSingleParents = new JCheckBox(
				"Parents That Change Influence Should Not Be Tossed");
		thresholdPanel.add(harshenBoundsOnTie);
		thresholdPanel.add(donotInvertSortOrder);
		thresholdPanel.add(seedParents);
		thresholdPanel.add(mustNotWinMajority);
		thresholdPanel.add(donotTossSingleRatioParents);
		thresholdPanel.add(donotTossChangedInfluenceSingleParents);

		// Sets up the encodings area
		JPanel encodingPanel = new JPanel(new BorderLayout());
		encodings = new JTextArea();
		JLabel encodingsLabel = new JLabel("Encodings:");
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setMinimumSize(new Dimension(260, 200));
		scroll2.setPreferredSize(new Dimension(276, 132));
		scroll2.setViewportView(encodings);
		encodingPanel.add(encodingsLabel, "North");
		encodingPanel.add(scroll2, "Center");

		// Creates the run button
		run = new JButton("Run");
		JPanel runHolder = new JPanel();
		runHolder.add(run);
		run.addActionListener(this);
		run.setMnemonic(KeyEvent.VK_R);

		// Creates a menu for the gui
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		menuBar.add(file);
		close = new JMenuItem("Close");
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
		close.setMnemonic(KeyEvent.VK_C);
		close.addActionListener(this);
		file.add(close);

		// Creates the main panel
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel middlePanel = new JPanel(new BorderLayout());
		middlePanel.add(thresholdPanel, "North");
		middlePanel.add(encodingPanel, "South");
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
		// if the close menu item is selected
		if (e.getSource() == close) {
			frame.dispose();
		}
		// if the browse initial network button is clicked
		else if (e.getSource() == browseInit) {
			Buttons.browse(frame, new File(initNetwork.getText().trim()), initNetwork,
					JFileChooser.FILES_ONLY, "Open");
		}
		// if the browse experiments directory button is clicked
		else if (e.getSource() == browseExper) {
			File dir;
			if (experiments.getText().trim().equals("")) {
				dir = null;
			} else {
				dir = new File(experiments.getText().trim() + File.separator + ".");
			}
			Buttons.browse(frame, dir, experiments, JFileChooser.DIRECTORIES_ONLY, "Open");
		}
		// if the run button is selected
		else if (e.getSource() == run) {
			try {
				String geneNet = "GeneNet";
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
				Process learn = exec.exec(geneNet + " " + experiments.getText().trim()
						+ " > run.log");
				learn.waitFor();
				exec.exec("dotty "
						+ new File(experiments.getText().trim() + File.separator + "method.dot")
								.getAbsolutePath());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Unable to learn from data.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static void main(String args[]) {
		new Learn(null);
	}
}