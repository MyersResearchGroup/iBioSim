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

	private JTextArea thresholds; // threshold text area

	private JTextArea encodings; // encodings text area

	/**
	 * This is the constructor for the Learn class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public Learn() {
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
		initNet1.add(initNetLabel);
		initNet1.add(initNetwork);
		initNet1.add(browseInit);
		initNet2.add(expLabel);
		initNet2.add(experiments);
		initNet2.add(browseExper);
		initNet.add(initNet1);
		initNet.add(initNet2);

		// Sets up the thresholds area
		JPanel thresholdPanel = new JPanel(new BorderLayout());
		thresholds = new JTextArea();
		JLabel thresholdsLabel = new JLabel("Thresholds:");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 200));
		scroll.setPreferredSize(new Dimension(276, 132));
		scroll.setViewportView(thresholds);
		thresholdPanel.add(thresholdsLabel, "North");
		thresholdPanel.add(scroll, "Center");

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
	}
	
	public static void main(String args[]) {
		new Learn();
	}
}