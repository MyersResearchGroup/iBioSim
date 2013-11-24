package analysis.main;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import main.*;
import main.util.*;

import verification.AbstPane;

/**
 * This is the Nary_Run class. It creates a GUI for input in the nary
 * abstraction. It implements the ActionListener class. This allows the Nary_Run
 * GUI to perform actions when buttons are pressed.
 * 
 * @author Curtis Madsen
 */
public class Nary_Run implements ActionListener, Runnable {

	private JFrame naryFrame; // Frame for nary abstraction

	private JButton naryRun; // The run button for nary abstraction

	private JButton naryClose; // The close button for nary abstraction

	private JTextField stopRate; // Text field for nary abstraction

	private JList finalState; // List for final state

	private Object[] finalStates = new Object[0]; // List of final states

	private JComboBox stopEnabled; // Combo box for Nary Abstraction

	private ArrayList<JTextField> inhib; // Text fields for inhibition levels

	private ArrayList<JList> consLevel; // Lists for concentration levels

	private ArrayList<String> getSpeciesProps; // Species in properties file

	private JButton finalAdd, finalRemove; // Buttons for altering final state

	/*
	 * Text fields for species properties
	 */
	private ArrayList<JTextField> texts;

	private ArrayList<Object[]> conLevel; // Lists for concentration levels

	private JComboBox highLow, speci; // Combo Boxes for final states

	/*
	 * Radio Buttons for termination conditions
	 */
	private JRadioButton ge, gt, eq, le, lt;

	private JComboBox simulators; // Combo Box for possible simulators

	private String filename; // name of sbml file

	private String[] getFilename; // array of filename

	private JRadioButton fba, sbml, dot, xhtml, lhpn; // Radio Buttons output option

	/*
	 * Radio Buttons that represent the different abstractions
	 */
	private JRadioButton nary, ODE, monteCarlo;

	/*
	 * Data used for monteCarlo abstraction
	 */
	private double timeLimit, printInterval, minTimeStep, timeStep;

	private int run;// Data used for monteCarlo abstraction

	private long rndSeed;// Data used for monteCarlo abstraction

	/*
	 * Data used for monteCarlo abstraction
	 */
	private String outDir, printer_id, printer_track_quantity;

	/*
	 * terminations and interesting species
	 */
	private String[] termCond, intSpecies;

	private double rap1, rap2, qss; // advanced options

	private int con; // advanced options

	private ArrayList<Integer> counts; // counts of con levels

	private Log log; // the log

	private Gui biomodelsim; // tstubd gui

	private JTabbedPane simTab; // the simulation tab

	private String root;

	private String separator;

	private String useInterval;

	private String direct;

	private String modelFile;

	private JRadioButton abstraction;

	private AbstPane abstPane;

	private double absError;

	/**
	 * This constructs a new Nary_Run object. This object is a GUI that contains
	 * input fields for the nary abstraction. This constructor initializes the
	 * member variables and creates the nary frame.
	 */
	public Nary_Run(Component component, JRadioButton ge, JRadioButton gt, JRadioButton eq, JRadioButton lt, JRadioButton le,
			JComboBox simulators, String[] getFilename, String filename, JRadioButton fba, JRadioButton sbml, JRadioButton dot, JRadioButton xhtml, JRadioButton lhpn,
			JRadioButton nary, JRadioButton ODE, JRadioButton monteCarlo, double timeLimit, String useInterval, double printInterval,
			double minTimeStep, double timeStep, String outDir, long rndSeed, int run, String printer_id, String printer_track_quantity,
			String[] intSpecies, double rap1, double rap2, double qss, int con, Log log, String ssaFile,
			Gui biomodelsim, JTabbedPane simTab, String root, String direct, String modelFile, JRadioButton abstraction, AbstPane abstPane,
			double absError) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}

		// intitializes the member variables
		this.absError = absError;
		this.root = root;
		this.rap1 = rap1;
		this.rap2 = rap2;
		this.qss = qss;
		this.con = con;
		this.intSpecies = intSpecies;
		this.ge = ge;
		this.gt = gt;
		this.eq = eq;
		this.le = le;
		this.lt = lt;
		this.timeLimit = timeLimit;
		this.printInterval = printInterval;
		this.minTimeStep = minTimeStep;
		this.timeStep = timeStep;
		this.outDir = outDir;
		this.rndSeed = rndSeed;
		this.run = run;
		this.printer_id = printer_id;
		this.printer_track_quantity = printer_track_quantity;
		this.simulators = simulators;
		this.getFilename = getFilename;
		this.filename = filename;
		this.dot = dot;
		this.sbml = sbml;
		this.fba = fba;
		this.xhtml = xhtml;
		this.lhpn = lhpn;
		this.nary = nary;
		this.monteCarlo = monteCarlo;
		this.ODE = ODE;
		this.log = log;
		this.biomodelsim = biomodelsim;
		this.simTab = simTab;
		this.useInterval = useInterval;
		this.direct = direct;
		this.modelFile = modelFile;
		this.abstPane = abstPane;
		this.abstraction = abstraction;

		// creates the nary frame and adds a window listener
		naryFrame = new JFrame("Nary Properties");
		WindowListener w = new WindowListener() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				naryFrame.dispose();
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
		naryFrame.addWindowListener(w);

		// creates the input fields for the nary abstraction
		JPanel naryInput = new JPanel(new GridLayout(2, 2));
		JLabel stopEnabledLabel = new JLabel("Analysis Stop Enabled:");
		String choice[] = new String[2];
		choice[0] = "false";
		choice[1] = "true";
		stopEnabled = new JComboBox(choice);
		JLabel stopRateLabel = new JLabel("Analysis Stop Rate:");
		stopRate = new JTextField();
		stopRate.setText("0.0005");

		// creates the final state JList
		JLabel finalStateLabel = new JLabel("Final State:");
		finalState = new JList();
		JScrollPane finalScroll = new JScrollPane();
		finalScroll.setPreferredSize(new Dimension(260, 100));
		finalScroll.setViewportView(finalState);
		JPanel addRemove = new JPanel();
		Object[] high = { "high", "low" };
		highLow = new JComboBox(high);
		finalAdd = new JButton("Add");
		finalRemove = new JButton("Remove");
		finalAdd.addActionListener(this);
		finalRemove.addActionListener(this);

		// adds the nary input fields to a panel
		naryInput.add(stopEnabledLabel);
		naryInput.add(stopEnabled);
		naryInput.add(stopRateLabel);
		naryInput.add(stopRate);
		JPanel finalPanel = new JPanel();
		finalPanel.add(finalStateLabel);
		finalPanel.add(finalScroll);
		JPanel naryInputPanel = new JPanel(new BorderLayout());
		naryInputPanel.add(naryInput, "North");
		naryInputPanel.add(finalPanel, "Center");
		naryInputPanel.add(addRemove, "South");

		// reads in the species properties to determine which species to use
		Properties naryProps = new Properties();
		try {
			FileInputStream load = new FileInputStream(new File(outDir + separator + "species.properties"));
			naryProps.load(load);
			load.close();
			FileOutputStream store = new FileOutputStream(new File(outDir + separator + "species.properties"));
			naryProps.store(store, "");
			store.close();
			naryProps = new Properties();
			new File("species.properties").delete();
			load = new FileInputStream(new File(outDir + separator + "species.properties"));
			naryProps.load(load);
			load.close();
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(component, "Properties File Not Found!", "File Not Found", JOptionPane.ERROR_MESSAGE);
		}
		Iterator<Object> iter = naryProps.keySet().iterator();
		getSpeciesProps = new ArrayList<String>();
		while (iter.hasNext()) {
			String next = (String) iter.next();
			if (next.contains("specification")) {
				String[] get = next.split("e");
				getSpeciesProps.add(get[get.length - 1].substring(1, get[get.length - 1].length()));
			}
		}

		// puts the species into the combo box for the final state
		speci = new JComboBox(getSpeciesProps.toArray());
		addRemove.add(highLow);
		addRemove.add(speci);
		addRemove.add(finalAdd);
		addRemove.add(finalRemove);

		// creates the species properties input fields
		ArrayList<JPanel> specProps = new ArrayList<JPanel>();
		texts = new ArrayList<JTextField>();
		inhib = new ArrayList<JTextField>();
		consLevel = new ArrayList<JList>();
		conLevel = new ArrayList<Object[]>();
		counts = new ArrayList<Integer>();
		for (int i = 0; i < getSpeciesProps.size(); i++) {
			JPanel newPanel1 = new JPanel(new GridLayout(1, 2));
			JPanel newPanel2 = new JPanel(new GridLayout(1, 2));
			JPanel label = new JPanel();
			label.add(new JLabel(getSpeciesProps.get(i) + " Absolute Inhibition Threshold:"));
			newPanel1.add(label);
			JPanel text = new JPanel();
			inhib.add(new JTextField());
			inhib.get(i).setPreferredSize(new Dimension(260, 20));
			inhib.get(i).setText("<<none>>");
			text.add(inhib.get(i));
			newPanel1.add(text);
			JPanel otherLabel = new JPanel();
			otherLabel.add(new JLabel(getSpeciesProps.get(i) + " Concentration Level:"));
			newPanel2.add(otherLabel);
			consLevel.add(new JList());
			conLevel.add(new Object[0]);
			iter = naryProps.keySet().iterator();
			ArrayList<String> get = new ArrayList<String>();
			int count = 0;
			while (iter.hasNext()) {
				String next = (String) iter.next();
				if (next.contains("concentration.level." + getSpeciesProps.get(i) + ".")) {
					get.add(naryProps.getProperty(next));
					count++;
				}
			}
			counts.add(count);
			int in;
			for (int out = 1; out < get.size(); out++) {
				if (!get.get(out).equals("<<none>>")) {
					double temp = Double.parseDouble(get.get(out));
					in = out;
					while (in > 0 && Double.parseDouble(get.get(in - 1)) >= temp) {
						get.set(in, get.get(in - 1));
						--in;
					}
					get.set(in, temp + "");
				}
			}
			consLevel.get(i).setListData(get.toArray());
			conLevel.set(i, get.toArray());
			JScrollPane scroll = new JScrollPane();
			scroll.setPreferredSize(new Dimension(260, 100));
			scroll.setViewportView(consLevel.get(i));
			JPanel area = new JPanel();
			area.add(scroll);
			newPanel2.add(area);
			JPanel addAndRemove = new JPanel();
			JTextField adding = new JTextField(15);
			texts.add(adding);
			JButton Add = new JButton("Add");
			JButton Remove = new JButton("Remove");
			Add.addActionListener(this);
			Add.setActionCommand("Add" + i);
			Remove.addActionListener(this);
			Remove.setActionCommand("Remove" + i);
			addAndRemove.add(adding);
			addAndRemove.add(Add);
			addAndRemove.add(Remove);
			JPanel newnewPanel = new JPanel(new BorderLayout());
			newnewPanel.add(newPanel1, "North");
			newnewPanel.add(newPanel2, "Center");
			newnewPanel.add(addAndRemove, "South");
			specProps.add(newnewPanel);
		}

		// creates the nary run and close buttons
		naryRun = new JButton("Run Nary");
		naryClose = new JButton("Cancel");
		naryRun.addActionListener(this);
		naryClose.addActionListener(this);
		JPanel naryRunPanel = new JPanel();
		naryRunPanel.add(naryRun);
		naryRunPanel.add(naryClose);

		// creates tabs for all the nary options and all the species
		JTabbedPane naryTabs = new JTabbedPane();
		naryTabs.addTab("Nary Properties", naryInputPanel);
		for (int i = 0; i < getSpeciesProps.size(); i++) {
			naryTabs.addTab(getSpeciesProps.get(i) + " Properties", specProps.get(i));
		}

		// adds the tabs and the run button to the main panel
		JPanel naryPanel = new JPanel(new BorderLayout());
		naryPanel.add(naryTabs, "Center");
		naryPanel.add(naryRunPanel, "South");

		// Packs the nary frame and displays it
		naryFrame.setContentPane(naryPanel);
		naryFrame.pack();
		Dimension screenSize;
		try {
			Toolkit tk = Toolkit.getDefaultToolkit();
			screenSize = tk.getScreenSize();
		}
		catch (AWTError awe) {
			screenSize = new Dimension(640, 480);
		}
		Dimension frameSize = naryFrame.getSize();

		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		int x = screenSize.width / 2 - frameSize.width / 2;
		int y = screenSize.height / 2 - frameSize.height / 2;
		naryFrame.setLocation(x, y);
		naryFrame.setResizable(false);
		naryFrame.setVisible(true);
	}

	/**
	 * This method performs different functions depending on what buttons are
	 * pushed and what input fields contain data.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// if the nary run button is clicked
		if (e.getSource() == naryRun) {
			new Thread(this).start();
		}
		// if the nary close button is clicked
		if (e.getSource() == naryClose) {
			naryFrame.dispose();
		}
		// if the add button for the final states is clicked
		else if (e.getSource() == finalAdd) {
			JList add = new JList();
			Object[] adding = { highLow.getSelectedItem() + "." + speci.getSelectedItem() };
			add.setListData(adding);
			add.setSelectedIndex(0);
			finalStates = Utility.add(finalStates, finalState, add, ge, gt, eq, lt, le, naryFrame);
		}
		// if the remove button for the final states is clicked
		else if (e.getSource() == finalRemove) {
			Utility.remove(finalState, finalStates);
		}
		// if the add or remove button for the species properties is clicked
		else {
			// if the add button for the species properties is clicked
			if (e.getActionCommand().contains("Add")) {
				int number = Integer.parseInt(e.getActionCommand().substring(3, e.getActionCommand().length()));
				try {
					double get = Double.parseDouble(texts.get(number).getText().trim());
					if (get < 0) {
						JOptionPane.showMessageDialog(naryFrame, "Concentration Levels Must Be Positive Real Numbers.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
					else {
						JList add = new JList();
						Object[] adding = { "" + get };
						add.setListData(adding);
						add.setSelectedIndex(0);
						Object[] sort = Utility.add(conLevel.get(number), consLevel.get(number), add, ge, gt, eq, lt, le,
								naryFrame);
						int in;
						for (int out = 1; out < sort.length; out++) {
							double temp = Double.parseDouble((String) sort[out]);
							in = out;
							while (in > 0 && Double.parseDouble((String) sort[in - 1]) >= temp) {
								sort[in] = sort[in - 1];
								--in;
							}
							sort[in] = temp + "";
						}
						conLevel.set(number, sort);
					}
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(naryFrame, "Concentration Levels Must Be Positive Real Numbers.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}

			}
			// if the remove button for the species properties is clicked
			else if (e.getActionCommand().contains("Remove")) {
				int number = Integer.parseInt(e.getActionCommand().substring(6, e.getActionCommand().length()));
				Utility.remove(consLevel.get(number), conLevel.get(number));
			}
		}
	}

	/**
	 * If the nary run button is pressed, this method starts a new thread for
	 * the nary abstraction.
	 */
	@Override
	public void run() {
		naryFrame.dispose();
		final JButton naryCancel = new JButton("Cancel Nary");
		final JFrame running = new JFrame("Running...");
		WindowListener w = new WindowListener() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				naryCancel.doClick();
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
		JLabel label = new JLabel("Progress");
		JProgressBar progress = new JProgressBar(0, run);
		progress.setStringPainted(true);
		// progress.setString("");
		// progress.setIndeterminate(true);
		progress.setValue(0);
		text.add(label);
		progBar.add(progress);
		button.add(naryCancel);
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
		String sim = (String) simulators.getSelectedItem();
		String stopE = (String) stopEnabled.getSelectedItem();
		double stopR = 0.0005;
		try {
			stopR = Double.parseDouble(stopRate.getText().trim());
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(naryFrame, "Must Enter A Real Number Into The Analysis Stop Rate Field.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		String[] finalS = Utility.getList(finalStates, finalState);
		Run runProgram = new Run(null);
		naryCancel.addActionListener(runProgram);
		runProgram.createNaryProperties(timeLimit, useInterval, printInterval, minTimeStep, timeStep, outDir, rndSeed, run, 1, printer_id,
				printer_track_quantity, getFilename, naryFrame, filename, monteCarlo, stopE, stopR, finalS, inhib, consLevel, getSpeciesProps,
				conLevel, termCond, intSpecies, rap1, rap2, qss, con, counts, false, false, false, false, false);
		if (monteCarlo.isSelected()) {
			File[] files = new File(outDir).listFiles();
			for (File f : files) {
				if (f.getName().contains("run-")) {
					f.delete();
				}
			}
		}
		runProgram.execute(filename, fba, sbml, dot, xhtml, lhpn, naryFrame, ODE, monteCarlo, sim, printer_id, printer_track_quantity, outDir, nary, 2,
				intSpecies, log, biomodelsim, simTab, root, progress, "", null, direct, timeLimit, timeLimit * run, modelFile,
				abstPane, abstraction, null, absError, timeStep, printInterval, run, rndSeed, true, label, running);
		running.setCursor(null);
		running.dispose();
		naryCancel.removeActionListener(runProgram);
	}
}
