package verification;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader; //import java.io.FileWriter;
import java.io.IOException; //import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
import java.util.Properties;

import lhpn2sbml.parser.LHPNFile;

import gcm2sbml.gui.PropertyList; //import gcm2sbml.util.Utility;

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

	public JCheckBox xform0, xform1, xform3, xform4, xform5, xform6, xform7, xform8, xform9, xform10, xform11,
			xform12, xform13, xform14, xform15, xform16;

	public DefaultListModel listModel;

	private JTextField field;

	private String directory, separator, root, absFile, oldBdd;

	private boolean change;

	private PropertyList componentList;

	private Log log;

	private BioSim biosim;

	/**
	 * This is the constructor for the Verification class. It initializes all
	 * the input fields, puts them on panels, adds the panels to the frame, and
	 * then displays the frame.
	 */
	public AbstPane(String directory, Verification verification, Log log, BioSim biosim,
			boolean lema, boolean atacs) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.directory = directory;
		this.log = log;
		this.setLayout(new BorderLayout());
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
		this.add(speciesHolder, "North");

		// Add abstraction option check boxes
		JPanel xformPanel = new JPanel();
		//xformPanel.setLayout(new BorderLayout());
		xform0 = new JCheckBox("Transform 0"); // Merge parallel places
		xform1 = new JCheckBox("Transform 1"); // Remove place in self-loop
		xform3 = new JCheckBox("Transform 3"); // Remove trans with 1 place in preset
		xform4 = new JCheckBox("Transform 4"); // Remove trans with 1 place in postset
		xform5 = new JCheckBox("Transform 5"); // Merge trans with same pre/postset
		xform6 = new JCheckBox("Transform 6"); // Merge trans with same preset
		xform7 = new JCheckBox("Transform 7"); // Merge trans with same postset
		xform8 = new JCheckBox("Transform 8");
		xform9 = new JCheckBox("Transform 9");
		xform10 = new JCheckBox("Transform 10");
		xform11 = new JCheckBox("Transform 11");
		xform12 = new JCheckBox("Transform 12"); // Abstract assignments
		xform13 = new JCheckBox("Transform 13"); // Remove variables
		xform14 = new JCheckBox("Transform 14"); // Remove dead places
		xform15 = new JCheckBox("Transform 15"); // Remove dead transitions
		xform16 = new JCheckBox("Transform 16"); // Simplify enabling conditions
		xform0.setSelected(true);
		xform1.setSelected(true);
		xform0.setSelected(true);
		xform3.setSelected(true);
		xform4.setSelected(true);
		xform5.setSelected(true);
		xform6.setSelected(true);
		xform7.setSelected(true);
		xform8.setSelected(true);
		xform9.setSelected(true);
		xform10.setSelected(true);
		xform11.setSelected(true);
		xform12.setSelected(true);
		xform13.setSelected(true);
		xform14.setSelected(true);
		xform15.setSelected(true);
		xform16.setSelected(true);
		xformPanel.add(xform0);
		xformPanel.add(xform1);
		xformPanel.add(xform3);
		xformPanel.add(xform4);
		xformPanel.add(xform5);
		xformPanel.add(xform6);
		xformPanel.add(xform7);
		xformPanel.add(xform8);
		xformPanel.add(xform9);
		xformPanel.add(xform10);
		xformPanel.add(xform11);
		xformPanel.add(xform12);
		xformPanel.add(xform13);
		xformPanel.add(xform14);
		xformPanel.add(xform15);
		xformPanel.add(xform16);
		this.add(xformPanel, "Center");

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
			// System.out.println(absFile);
			Properties prop = new Properties();
			// FileInputStream in = new FileInputStream(new File(directory +
			// separator + filename));
			// prop.load(in);
			// in.close();
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
			// e1.printStackTrace();
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
					JOptionPane.showMessageDialog(biosim.frame(),
							"Cannot add the selected component.", "Error",
							JOptionPane.ERROR_MESSAGE);
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
