package verification;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import gcm2sbml.gui.PropertyList;
import gcm2sbml.util.Utility;

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

	private JButton button;

	private JLabel label;

	private JRadioButton radio;

	private JCheckBox check;

	private JTextField field;

	private ButtonGroup group;

	private String directory, separator, root, verFile, oldBdd, sourceFileNoPath;

	private boolean change, atacs;

	private PropertyList componentList;

	private Log log;

	private BioSim biosim;

	/**
	 * This is the constructor for the Verification class. It initializes all
	 * the input fields, puts them on panels, adds the panels to the frame, and
	 * then displays the frame.
	 */
	public AbstPane(String directory, String verName, String filename, Log log, BioSim biosim,
			boolean lema, boolean atacs) {
//		 Creates the interesting species JList
		JList intSpecies = new JList();
		JList species = new JList();
		JLabel spLabel = new JLabel("Available Species:");
		JLabel speciesLabel = new JLabel("Interesting Species:");
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
		JButton addIntSpecies = new JButton("Add Species");
		JButton removeIntSpecies = new JButton("Remove Species");
		JButton clearIntSpecies = new JButton("Clear Species");
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
		intSpecies.setEnabled(false);
		species.setEnabled(false);
		//intSpecies.addMouseListener(this);
		//species.addMouseListener(this);
		spLabel.setEnabled(false);
		speciesLabel.setEnabled(false);
		addIntSpecies.setEnabled(false);
		removeIntSpecies.setEnabled(false);
		addIntSpecies.addActionListener(this);
		removeIntSpecies.addActionListener(this);
		clearIntSpecies.setEnabled(false);
		clearIntSpecies.addActionListener(this);

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
		//maxCon.setEnabled(false);
		qssaLabel.setEnabled(false);
		//qssa.setEnabled(false);
		rapidLabel1.setEnabled(false);
		//rapid1.setEnabled(false);
		rapidLabel2.setEnabled(false);
		//rapid2.setEnabled(false);
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
		advanced.add(advancedGrid);
		// JPanel space = new JPanel();
		// advanced.add(space);
		advanced.add(speciesHolder);

		// load parameters
//		Properties load = new Properties();
		// log.addText(directory + separator + verFile);
//		try {
//			FileInputStream in = new FileInputStream(new File(directory + separator + verFile));
//			load.load(in);
//			in.close();
//			if (load.containsKey("verification.file")) {
//				verifyFile = load.getProperty("verification.file");
//				// log.addText(verifyFile);
//			}
//			if (load.containsKey("verification.bddSize")) {
//				field.setText(load.getProperty("verification .bddSize"));
//			}
//			Integer i = 0;
//			while (load.containsKey("synthesis.compList" + i.toString())) {
//				componentList.addItem(load.getProperty("synthesis.compList" + i.toString()));
//				i++;
//			}
//			if (load.containsKey("verification.timing.methods")) {
//				if (atacs) {
//					if (load.getProperty("verification.timing.methods").equals("untimed")) {
//						check.setSelected(true);
//					}
//				}
//				else {
//					if (load.getProperty("verification.timing.methods").equals("bdd")) {
//						check.setSelected(true);
//					}
//				}
//			}
//			if (load.containsKey("verification.Abst")) {
//				if (load.getProperty("verification.Abst").equals("true")) {
//					check.setSelected(true);
//				}
//			}
//			String[] tempArray = verifyFile.split(separator);
//			sourceFileNoPath = tempArray[tempArray.length - 1];
//			field = new JTextField(sourceFileNoPath);
//		}
//		catch (Exception e) {
//			JOptionPane.showMessageDialog(biosim.frame(), "Unable to load properties file!",
//					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
//			// e.printStackTrace();
//		}
		// save();

		//this.setLayout(new BorderLayout());
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
		change = true;
		if (e.getSource() == button) {
			save(verFile);
			new Thread(this).start();
		}
		else if (e.getSource() == button) {
			log.addText("Saving:\n" + directory + separator + verFile + "\n");
			save(verFile);
		}
		else if (e.getSource() == button) {
			String[] vhdlFiles = new File(root).list();
			ArrayList<String> tempFiles = new ArrayList<String>();
			for (int i = 0; i < vhdlFiles.length; i++) {
				if (vhdlFiles[i].endsWith(".vhd") && !vhdlFiles[i].equals(sourceFileNoPath)) {
					tempFiles.add(vhdlFiles[i]);
				}
			}
			vhdlFiles = new String[tempFiles.size()];
			for (int i = 0; i < vhdlFiles.length; i++) {
				vhdlFiles[i] = tempFiles.get(i);
			}
			String filename = (String) JOptionPane.showInputDialog(this, "", "Select Component",
					JOptionPane.PLAIN_MESSAGE, null, vhdlFiles, vhdlFiles[0]);
			if (filename != null) {
				String[] comps = componentList.getItems();
				boolean contains = false;
				for (int i = 0; i < comps.length; i++) {
					if (comps[i].equals(filename)) {
						contains = true;
					}
				}
				if (!filename.endsWith(".vhd")) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"You must select a valid VHDL file.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if (new File(directory + separator + filename).exists()
						|| filename.equals(sourceFileNoPath) || contains) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"This component is already contained in this tool.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				componentList.addItem(filename);
				return;
			}
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
		save(verFile);
	}

	public void save(String filename) {
		// JOptionPane.showMessageDialog(this, verifyFile);
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(new File(directory + separator + filename));
			prop.load(in);
			in.close();
			//prop.setProperty("verification.file", verifyFile);
			if (!field.getText().trim().equals("")) {
				prop.setProperty("verification.component", field.getText().trim());
			}
			String[] components = componentList.getItems();
			for (Integer i = 0; i < components.length; i++) {
				prop.setProperty("synthesis.compList" + i.toString(), components[i]);
			}
			if (atacs) {
				if (check.isSelected()) {
					prop.setProperty("verification.timing.methods", "untimed");
				}
				else if (check.isSelected()) {
					prop.setProperty("verification.timing.methods", "geometric");
				}
				else if (check.isSelected()) {
					prop.setProperty("verification.timing.methods", "posets");
				}
				else if (check.isSelected()) {
					prop.setProperty("verification.timing.methods", "bag");
				}
				else if (check.isSelected()) {
					prop.setProperty("verification.timing.methods", "bap");
				}
				else {
					prop.setProperty("verification.timing.methods", "baptdc");
				}
			}
			if (check.isSelected()) {
				prop.setProperty("verification.reduction", "true");
			}
			else {
				prop.setProperty("verification.reduction", "false");
			}
			FileOutputStream out = new FileOutputStream(new File(directory + separator + verFile));
			//prop.store(out, verifyFile);
			out.close();
			log.addText("Saving Parameter File:\n" + directory + separator + verFile + "\n");
			change = false;
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to save parameter file!",
					"Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
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

	public void reload(String newname) {
		field.setText(newname);
	}

	public void viewCircuit() {
		String[] getFilename;
		if (field.getText().trim().equals("")) {
			//getFilename = verifyFile.split("\\.");
		}
		else {
			getFilename = new String[1];
			getFilename[0] = field.getText().trim();
		}
		//String circuitFile = getFilename[0] + ".prs";
		// JOptionPane.showMessageDialog(this, circuitFile);
		// JOptionPane.showMessageDialog(this, directory + separator +
		// circuitFile);
//		try {
//			// JOptionPane.showMessageDialog(this, directory + separator +
//			// "run.log");
//			// String[] getFilename = verifyFile.split(".");
//			// String circuitFile = getFilename[0] + ".ps";
//			// JOptionPane.showMessageDialog(this, directory + separator +
//			// circuitFile);
//			if (new File(circuitFile).exists()) {
//				File log = new File(circuitFile);
//				BufferedReader input = new BufferedReader(new FileReader(log));
//				String line = null;
//				JTextArea messageArea = new JTextArea();
//				while ((line = input.readLine()) != null) {
//					messageArea.append(line);
//					messageArea.append(System.getProperty("line.separator"));
//				}
//				input.close();
//				messageArea.setLineWrap(true);
//				messageArea.setWrapStyleWord(true);
//				messageArea.setEditable(false);
//				JScrollPane scrolls = new JScrollPane();
//				scrolls.setMinimumSize(new Dimension(500, 500));
//				scrolls.setPreferredSize(new Dimension(500, 500));
//				scrolls.setViewportView(messageArea);
//				JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Circuit View",
//						JOptionPane.INFORMATION_MESSAGE);
//			}
//			else {
//				JOptionPane.showMessageDialog(biosim.frame(), "No circuit view exists.", "Error",
//						JOptionPane.ERROR_MESSAGE);
//			}
//		}
//		catch (Exception e1) {
//			JOptionPane.showMessageDialog(biosim.frame(), "Unable to view circuit.", "Error",
//					JOptionPane.ERROR_MESSAGE);
//		}
	}

	public void viewTrace() {
		//String[] getFilename = verifyFile.split("\\.");
		//String traceFilename = getFilename[0] + ".trace";
		// JOptionPane.showMessageDialog(this, circuitFile);
		// JOptionPane.showMessageDialog(this, directory + separator +
		// circuitFile);
//		try {
//			// JOptionPane.showMessageDialog(this, directory + separator +
//			// "run.log");
//			// String[] getFilename = verifyFile.split(".");
//			// String circuitFile = getFilename[0] + ".ps";
//			// JOptionPane.showMessageDialog(this, directory + separator +
//			// circuitFile);
//			if (new File(traceFilename).exists()) {
//				File log = new File(traceFilename);
//				BufferedReader input = new BufferedReader(new FileReader(log));
//				String line = null;
//				JTextArea messageArea = new JTextArea();
//				while ((line = input.readLine()) != null) {
//					messageArea.append(line);
//					messageArea.append(System.getProperty("line.separator"));
//				}
//				input.close();
//				messageArea.setLineWrap(true);
//				messageArea.setWrapStyleWord(true);
//				messageArea.setEditable(false);
//				JScrollPane scrolls = new JScrollPane();
//				scrolls.setMinimumSize(new Dimension(500, 500));
//				scrolls.setPreferredSize(new Dimension(500, 500));
//				scrolls.setViewportView(messageArea);
//				JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Trace View",
//						JOptionPane.INFORMATION_MESSAGE);
//			}
//			else {
//				JOptionPane.showMessageDialog(biosim.frame(), "No trace file exists.", "Error",
//						JOptionPane.ERROR_MESSAGE);
//			}
//		}
//		catch (Exception e1) {
//			JOptionPane.showMessageDialog(biosim.frame(), "Unable to view trace.", "Error",
//					JOptionPane.ERROR_MESSAGE);
//		}
	}

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
