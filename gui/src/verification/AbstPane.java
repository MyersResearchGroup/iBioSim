package verification;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
import java.util.Properties;

import lhpn2sbml.parser.LHPNFile;

import gcm2sbml.gui.PropertyList;
//import gcm2sbml.util.Utility;

import biomodelsim.*;

/**
 * This class creates a GUI front end for the Verification tool. It provides the
 * necessary options to run an atacs simulation of the circuit and manage the
 * results from the BioSim GUI.
 * 
 * @author Kevin Jones
 */

public class AbstPane extends JPanel implements ActionListener, Runnable {

	private static final long serialVersionUID = -5806315070287184299L;

	private JButton addIntSpecies, removeIntSpecies, clearIntSpecies;

	private JList species, intSpecies;

	public DefaultListModel listModel;

	// private JLabel label;

	// private JRadioButton radio;

	// private JCheckBox check;

	private JTextField field;

	// private ButtonGroup group;

	private String directory, separator, root, absFile, oldBdd;

	// sourceFileNoPath;

	private boolean change, atacs;

	private PropertyList componentList;

	private Log log;

	private BioSim biosim;

	/**
	 * This is the constructor for the Verification class. It initializes all
	 * the input fields, puts them on panels, adds the panels to the frame, and
	 * then displays the frame.
	 */
	public AbstPane(String directory, Verification verification, Log log,
			BioSim biosim, boolean lema, boolean atacs) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.directory = directory;
		this.log = log;
		absFile = verification.getVerName() + ".abs";
		verification.copyFile();
		LHPNFile lhpn = new LHPNFile();
		lhpn.load(directory + separator + verification.verifyFile);
		// Creates the interesting species JList
		listModel = new DefaultListModel();
		intSpecies = new JList(lhpn.getAllVariables());
		species = new JList(listModel);
		JLabel spLabel = new JLabel("Available Variables:");
		JLabel speciesLabel = new JLabel("Interesting Variables:");
		JPanel speciesHolder = new JPanel(new BorderLayout());
		JPanel listOfSpeciesLabelHolder = new JPanel(new GridLayout(1, 2));
		JPanel listOfSpeciesHolder = new JPanel(new GridLayout(1, 2));
		JScrollPane scroll = new JScrollPane();
		JScrollPane scroll1 = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 200));
		scroll.setPreferredSize(new Dimension(276, 132));
		scroll.setViewportView(species);
		scroll1.setMinimumSize(new Dimension(260, 200));
		scroll1.setPreferredSize(new Dimension(276, 132));
		scroll1.setViewportView(intSpecies);
		addIntSpecies = new JButton("Add Variable");
		removeIntSpecies = new JButton("Remove Variable");
		clearIntSpecies = new JButton("Clear Variable");
		addIntSpecies.addActionListener(this);
		removeIntSpecies.addActionListener(this);
		clearIntSpecies.addActionListener(this);
		listOfSpeciesLabelHolder.add(spLabel);
		listOfSpeciesHolder.add(scroll1);
		listOfSpeciesLabelHolder.add(speciesLabel);
		listOfSpeciesHolder.add(scroll);
		speciesHolder.add(listOfSpeciesLabelHolder, "North");
		speciesHolder.add(listOfSpeciesHolder, "Center");
		JPanel buttonHolder = new JPanel();
		buttonHolder.add(addIntSpecies);
		buttonHolder.add(removeIntSpecies);
		buttonHolder.add(clearIntSpecies);
		speciesHolder.add(buttonHolder, "South");
		// intSpecies.setEnabled(false);
		// species.setEnabled(false);
		// intSpecies.addMouseListener(this);
		// species.addMouseListener(this);
		// spLabel.setEnabled(false);
		// speciesLabel.setEnabled(false);
		// addIntSpecies.setEnabled(false);
		// removeIntSpecies.setEnabled(false);
		// addIntSpecies.addActionListener(this);
		// removeIntSpecies.addActionListener(this);
		// clearIntSpecies.setEnabled(false);
		// clearIntSpecies.addActionListener(this);

		// Creates some abstraction options
		JPanel advancedGrid = new JPanel(new GridLayout(8, 2));
		JPanel advanced = new JPanel(new GridLayout(2, 1));
		JPanel rapidSpace1 = new JPanel();
		JPanel rapidSpace2 = new JPanel();
		JPanel rapidSpace3 = new JPanel();
		JPanel rapidSpace4 = new JPanel();
		JPanel qssaSpace1 = new JPanel();
		JPanel qssaSpace2 = new JPanel();
		JPanel maxConSpace1 = new JPanel();
		JPanel maxConSpace2 = new JPanel();
		JLabel rapidLabel1 = new JLabel("Rapid Equilibrium Condition 1:");
		JTextField rapid1 = new JTextField("", 15);
		JLabel rapidLabel2 = new JLabel("Rapid Equilibrium Condition 2:");
		JTextField rapid2 = new JTextField("", 15);
		JLabel qssaLabel = new JLabel("QSSA Condition:");
		JTextField qssa = new JTextField("", 15);
		JLabel maxConLabel = new JLabel("Max Concentration Threshold:");
		JTextField maxCon = new JTextField("", 15);
		maxConLabel.setEnabled(false);
		// maxCon.setEnabled(false);
		qssaLabel.setEnabled(false);
		// qssa.setEnabled(false);
		rapidLabel1.setEnabled(false);
		// rapid1.setEnabled(false);
		rapidLabel2.setEnabled(false);
		// rapid2.setEnabled(false);
		advancedGrid.add(rapidLabel1);
		advancedGrid.add(rapid1);
		advancedGrid.add(rapidSpace1);
		advancedGrid.add(rapidSpace2);
		advancedGrid.add(rapidLabel2);
		advancedGrid.add(rapid2);
		advancedGrid.add(rapidSpace3);
		advancedGrid.add(rapidSpace4);
		advancedGrid.add(qssaLabel);
		advancedGrid.add(qssa);
		advancedGrid.add(qssaSpace1);
		advancedGrid.add(qssaSpace2);
		advancedGrid.add(maxConLabel);
		advancedGrid.add(maxCon);
		advancedGrid.add(maxConSpace1);
		advancedGrid.add(maxConSpace2);
		// advanced.add(advancedGrid);
		// JPanel space = new JPanel();
		// advanced.add(space);
		advanced.add(speciesHolder);

		// load parameters
		Properties load = new Properties();
		// log.addText(directory + separator + verFile);
//		try {
//			FileInputStream in = new FileInputStream(new File(directory + separator + absFile));
//			load.load(in);
//			in.close();
//			if (load.containsKey("abstraction.interesting")) {
//				String intVars = load.getProperty("abstraction.interesting");
//				String[] array = intVars.split(" ");
//				for (String s : array) {
//					if (!s.equals("")) {
//						listModel.addElement(s);
//					}
//				}
//			}
//		}
//		catch (Exception e) {
//			JOptionPane.showMessageDialog(biosim.frame(), "Unable to load properties file!",
//					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
//			// e.printStackTrace();
//		}
//		save();

		// this.setLayout(new BorderLayout());
		this.add(advanced);
		// this.add(buttonPanel, BorderLayout.PAGE_END);
		change = false;
	}

	/**
	 * This method performs different functions depending on what menu items or
	 * buttons are selected.
	 * 
	 * @throws
	 * @throws
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addIntSpecies) {
			if (!listModel.contains(intSpecies.getSelectedValue())) {
				listModel.addElement(intSpecies.getSelectedValue());
			}
		}
		if (e.getSource() == removeIntSpecies) {
			listModel.removeElement(species.getSelectedValue());
		}
		if (e.getSource() == clearIntSpecies) {
			listModel.removeAllElements();
		}
	}
	
	public void addIntVar(String variable) {
		if (!listModel.contains(variable)) {
			listModel.addElement(variable);
		}
	}

	public void run() {

	}

	public void saveAs() {
		String newName = JOptionPane.showInputDialog(biosim.frame(), "Enter Verification name:",
				"Verification Name", JOptionPane.PLAIN_MESSAGE);
		if (newName == null) {
			return;
		}
		if (!newName.endsWith(".ver")) {
			newName = newName + ".ver";
		}
		save(newName);
	}

	public void save() {
		save(absFile);
	}

	public void save(String filename) {
		// JOptionPane.showMessageDialog(this, verifyFile);
		try {
			//System.out.println(absFile);
			Properties prop = new Properties();
			//FileInputStream in = new FileInputStream(new File(directory + separator + filename));
			//prop.load(in);
			//in.close();
			// prop.setProperty("verification.file", verifyFile);
			String intVars = "";
			for (int i = 0; i < listModel.getSize(); i++) {
				intVars = listModel.getElementAt(i) + " ";
			}
			if (!intVars.equals("")) {
				prop.setProperty("abstraction.interesting", intVars);
			}
			FileOutputStream out = new FileOutputStream(new File(directory + separator + absFile));
			prop.store(out, absFile);
			out.close();
			log.addText("Saving Parameter File:\n" + directory + separator + absFile + "\n");
			change = false;
		}
		catch (Exception e1) {
			//e1.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to save parameter file!",
					"Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
		if (componentList != null) {
		for (String s : componentList.getItems()) {
			try {
				new File(directory + separator + s).createNewFile();
				FileInputStream in = new FileInputStream(new File(root + separator + s));
				FileOutputStream out = new FileOutputStream(new File(directory + separator + s));
				int read = in.read();
				while (read != -1) {
					out.write(read);
					read = in.read();
				}
				in.close();
				out.close();
			}
			catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(biosim.frame(), "Cannot add the selected component.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		}
	}

	public void reload(String newname) {
		field.setText(newname);
	}

	public String[] getIntVars() {
		String[] intVars = new String[listModel.getSize()];
		for (int i = 0; i < listModel.getSize(); i++) {
			if (listModel.elementAt(i) != null) {
				intVars[i] = listModel.elementAt(i).toString();
			}
		}
		return intVars;
	}

	public void viewCircuit() {
		String[] getFilename;
		if (field.getText().trim().equals("")) {
			// getFilename = verifyFile.split("\\.");
		}
		else {
			getFilename = new String[1];
			getFilename[0] = field.getText().trim();
		}
		// String circuitFile = getFilename[0] + ".prs";
		// JOptionPane.showMessageDialog(this, circuitFile);
		// JOptionPane.showMessageDialog(this, directory + separator +
		// circuitFile);
		// try {
		// // JOptionPane.showMessageDialog(this, directory + separator +
		// // "run.log");
		// // String[] getFilename = verifyFile.split(".");
		// // String circuitFile = getFilename[0] + ".ps";
		// // JOptionPane.showMessageDialog(this, directory + separator +
		// // circuitFile);
		// if (new File(circuitFile).exists()) {
		// File log = new File(circuitFile);
		// BufferedReader input = new BufferedReader(new FileReader(log));
		// String line = null;
		// JTextArea messageArea = new JTextArea();
		// while ((line = input.readLine()) != null) {
		// messageArea.append(line);
		// messageArea.append(System.getProperty("line.separator"));
		// }
		// input.close();
		// messageArea.setLineWrap(true);
		// messageArea.setWrapStyleWord(true);
		// messageArea.setEditable(false);
		// JScrollPane scrolls = new JScrollPane();
		// scrolls.setMinimumSize(new Dimension(500, 500));
		// scrolls.setPreferredSize(new Dimension(500, 500));
		// scrolls.setViewportView(messageArea);
		// JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Circuit
		// View",
		// JOptionPane.INFORMATION_MESSAGE);
		// }
		// else {
		// JOptionPane.showMessageDialog(biosim.frame(), "No circuit view
		// exists.", "Error",
		// JOptionPane.ERROR_MESSAGE);
		// }
		// }
		// catch (Exception e1) {
		// JOptionPane.showMessageDialog(biosim.frame(), "Unable to view
		// circuit.", "Error",
		// JOptionPane.ERROR_MESSAGE);
		// }
	}

	// public void viewTrace() {
	// String[] getFilename = verifyFile.split("\\.");
	// String traceFilename = getFilename[0] + ".trace";
	// JOptionPane.showMessageDialog(this, circuitFile);
	// JOptionPane.showMessageDialog(this, directory + separator +
	// circuitFile);
	// try {
	// // JOptionPane.showMessageDialog(this, directory + separator +
	// // "run.log");
	// // String[] getFilename = verifyFile.split(".");
	// // String circuitFile = getFilename[0] + ".ps";
	// // JOptionPane.showMessageDialog(this, directory + separator +
	// // circuitFile);
	// if (new File(traceFilename).exists()) {
	// File log = new File(traceFilename);
	// BufferedReader input = new BufferedReader(new FileReader(log));
	// String line = null;
	// JTextArea messageArea = new JTextArea();
	// while ((line = input.readLine()) != null) {
	// messageArea.append(line);
	// messageArea.append(System.getProperty("line.separator"));
	// }
	// input.close();
	// messageArea.setLineWrap(true);
	// messageArea.setWrapStyleWord(true);
	// messageArea.setEditable(false);
	// JScrollPane scrolls = new JScrollPane();
	// scrolls.setMinimumSize(new Dimension(500, 500));
	// scrolls.setPreferredSize(new Dimension(500, 500));
	// scrolls.setViewportView(messageArea);
	// JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Trace View",
	// JOptionPane.INFORMATION_MESSAGE);
	// }
	// else {
	// JOptionPane.showMessageDialog(biosim.frame(), "No trace file
	// exists.", "Error",
	// JOptionPane.ERROR_MESSAGE);
	// }
	// }
	// catch (Exception e1) {
	// JOptionPane.showMessageDialog(biosim.frame(), "Unable to view
	// trace.", "Error",
	// JOptionPane.ERROR_MESSAGE);
	// }
	// }

	public void viewLog() {
		try {
			// JOptionPane.showMessageDialog(this, directory + separator +
			// "run.log");
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

	public boolean hasChanged() {
		if (!oldBdd.equals(field.getText())) {
			return true;
		}
		return change;
	}
}
