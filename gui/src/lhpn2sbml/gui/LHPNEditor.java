package lhpn2sbml.gui;

import lhpn2sbml.parser.LHPNFile;

import gcm2sbml.gui.AbstractRunnableNamedButton;
//import gcm2sbml.gui.InfluencePanel;
//import gcm2sbml.gui.ParameterPanel;
//import gcm2sbml.gui.PromoterPanel;
import gcm2sbml.gui.PropertyList;
import gcm2sbml.gui.Runnable;
//import gcm2sbml.gui.SpeciesPanel;
import gcm2sbml.util.Utility;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Properties;
//import java.util.Set;

//import javax.swing.DefaultComboBoxModel;
//import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
//import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
//import javax.swing.JSplitPane;
import javax.swing.JTextField;
//import javax.swing.JButton;

import biomodelsim.BioSim;
import biomodelsim.Log;

/**
 * This is the LHPNEditor class. This allows the user to create and edit a
 * Labeled Hybrid Petri Net, which can later be saved and run through the ATACS
 * tool.
 * 
 * @author Kevin Jones
 */
public class LHPNEditor extends JPanel implements ActionListener, MouseListener {
	// , MouseListener {
	private JTextField lhpnNameTextField;

	private PropertyList variables, places, transitions, controlFlow;

	// private BioSim biosim;
	private Log log;

	// private String directory;

	private String filename = "", directory = "", separator = "";

	private String[] varOptions = new String[] { "Boolean", "Continuous", "Integer" };

	// private String lhpnName = "";

	private LHPNFile lhpnFile = null;

	private JPanel mainPanel;// buttonPanel;

	private boolean flag = true, dirty = false;

	public LHPNEditor() {
		super();
	}

	public LHPNEditor(String directory, String filename, LHPNFile lhpn, BioSim biosim, Log log) {
		super();
		// this.biosim = biosim;
		this.log = log;
		addMouseListener(biosim);

		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}

		// this.directory = directory;
		lhpnFile = new LHPNFile(log);
		this.directory = directory;
		if (filename != null) {
			File f = new File(directory + File.separator + filename);
			if (!(f.length() == 0)) {
				lhpnFile.load(directory + File.separator + filename);
			}
			this.filename = filename;
			// this.lhpnName = filename.replace(".g", "");
		}
		else {
			this.filename = "";
		}
		// log.addText("build Gui");
		buildGui(this.filename);
		// log.addText("Gui built");
	}

	private void buildGui(String filename) {
		JPanel mainPanelNorth = new JPanel();
		JPanel mainPanelCenter = new JPanel(new BorderLayout());
		JPanel mainPanelCenterUp = new JPanel();
		JPanel mainPanelCenterCenter = new JPanel(new GridLayout(2, 2));
		JPanel mainPanelCenterDown = new JPanel(new BorderLayout());
		mainPanelCenter.add(mainPanelCenterUp, BorderLayout.NORTH);
		mainPanelCenter.add(mainPanelCenterCenter, BorderLayout.CENTER);
		mainPanelCenter.add(mainPanelCenterDown, BorderLayout.SOUTH);
		lhpnNameTextField = new JTextField(filename, 15);
		lhpnNameTextField.setEditable(false);
		lhpnNameTextField.addActionListener(this);
		JLabel lhpnNameLabel = new JLabel("LHPN Id:");
		mainPanelNorth.add(lhpnNameLabel);
		mainPanelNorth.add(lhpnNameTextField);

		// buttonPanel = new JPanel();
		// JButton save = new JButton("Save");
		// save.addActionListener(this);
		// buttonPanel.add(save);
		// add(buttonPanel, BorderLayout.SOUTH);

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainPanelNorth, "North");
		mainPanel.add(mainPanelCenter, "Center");
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);

		JPanel buttons = new JPanel();
		SaveButton saveButton = new SaveButton("Save LHPN", lhpnNameTextField);
		buttons.add(saveButton);
		saveButton.addActionListener(this);
		saveButton = new SaveButton("Save LHPN as", lhpnNameTextField);
		buttons.add(saveButton);
		saveButton.addActionListener(this);

		// log.addText("build panels");
		variables = new PropertyList("Variable List");
		variables.addMouseListener(this);
		EditButton addVar = new EditButton("Add Variable", variables);
		RemoveButton removeVar = new RemoveButton("Remove Variable", variables);
		EditButton editVar = new EditButton("Edit Variable", variables);
		if (!lhpnFile.getVariables().equals(null)) {
			variables.addAllItem(lhpnFile.getVariables().keySet());
		}
		if (!lhpnFile.getVariables().equals(null)) {
			variables.addAllItem(lhpnFile.getIntegers().keySet());
		}
		if (!lhpnFile.getInputs().equals(null)) {
			variables.addAllItem(lhpnFile.getInputs().keySet());
		}
		if (!lhpnFile.getOutputs().equals(null)) {
			// log.addText(lhpnFile.getOutputs().toString());
			variables.addAllItem(lhpnFile.getOutputs().keySet());
		}

		JPanel varPanel = Utility.createPanel(this, "Variables", variables, addVar, removeVar,
				editVar);
		mainPanelCenterCenter.add(varPanel);

		// log.addText("build place panel");
		places = new PropertyList("Place List");
		places.addMouseListener(this);
		EditButton addPlace = new EditButton("Add Place", places);
		RemoveButton removePlace = new RemoveButton("Remove Place", places);
		EditButton editPlace = new EditButton("Edit Place", places);
		places.addAllItem(lhpnFile.getPlaces().keySet());

		JPanel placePanel = Utility.createPanel(this, "Places", places, addPlace, removePlace,
				editPlace);
		mainPanelCenterCenter.add(placePanel);

		// log.addText("build transition panel");
		transitions = new PropertyList("Transition List");
		transitions.addMouseListener(this);
		EditButton addTrans = new EditButton("Add Transition", transitions);
		RemoveButton removeTrans = new RemoveButton("Remove Transition", transitions);
		EditButton editTrans = new EditButton("Edit Transition", transitions);
		transitions.addAllItem(lhpnFile.getTransitionList());

		JPanel transPanel = Utility.createPanel(this, "Transitions", transitions, addTrans,
				removeTrans, editTrans);
		mainPanelCenterCenter.add(transPanel);

		// log.addText("build control panel");
		controlFlow = new PropertyList("Control Flow");
		controlFlow.addMouseListener(this);
		EditButton addFlow = new EditButton("Add Movement", controlFlow);
		RemoveButton removeFlow = new RemoveButton("Remove Movement", controlFlow);
		EditButton editFlow = new EditButton("Edit Movement", controlFlow);
		// log.addText("get control panel");
		controlFlow.addAllItem(lhpnFile.getControlFlow());
		// log.addText("got control panel");

		JPanel flowPanel = Utility.createPanel(this, "Control Flow", controlFlow, addFlow,
				removeFlow, editFlow);
		mainPanelCenterCenter.add(flowPanel);

	}
	
	public boolean isDirty() {
		return dirty;
	}

	public void save() {
		dirty = false;
		log.addText("check LHPN");
		lhpnFile.save(directory + File.separator + filename);
	}

	public void saveAs(String newName) {
		dirty = false;
		lhpnFile.save(directory + File.separator + newName);
	}

	public void viewLhpn() {
		try {
			File work = new File(directory);
			if (new File(directory + separator + filename).exists()) {
				//String dotFile = filename.replace(".lhpn", ".dot");
				// String command = "open " + dotFile;
				Runtime exec = Runtime.getRuntime();
				log.addText("Executing:\n" + "atacs -lloddl " + filename + "\n");
				Process load = exec.exec("atacs -lloddl " + filename, null, work);
				load.waitFor();
				// exec.exec(command, null, work);
			}
			else {
				JOptionPane.showMessageDialog(this, "No circuit has been generated yet.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(this, "Unable to view circuit.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public class SaveButton extends AbstractRunnableNamedButton {
		public SaveButton(String name, JTextField fieldNameField) {
			super(name);
			this.fieldNameField = fieldNameField;
		}

		public void run() {
			lhpnFile.save(getName());
		}

		private JTextField fieldNameField = null;
	}

	public class EditButton extends AbstractRunnableNamedButton {
		public EditButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		public void run() {
			new EditCommand(getName(), list).run();
		}

		private PropertyList list = null;
	}

	public class RemoveButton extends AbstractRunnableNamedButton {
		public RemoveButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		public void run() {
			if (getName().contains("Variable")) {
				String name = list.getSelectedValue().toString();
				if (lhpnFile.removeVar(name) != 0) {
					JOptionPane.showMessageDialog(this, "Must delete assignments to variable "
							+ name, "Cannot remove variable" + name, JOptionPane.ERROR_MESSAGE);
				}
				variables.removeItem(name);
			}
			else if (getName().contains("Place")) {
				lhpnFile.removePlace(list.getSelectedValue().toString());
				places.removeItem(list.getSelectedValue().toString());
			}
			else if (getName().contains("Transition")) {
				lhpnFile.removeTransition(list.getSelectedValue().toString());
				transitions.removeItem(list.getSelectedValue().toString());
			}
			else if (getName().contains("Movement")) {
				String tempString = list.getSelectedValue().toString();
				String[] tempArray = tempString.split("\\s");
				lhpnFile.removeControlFlow(tempArray[0], tempArray[1]);
				controlFlow.removeItem(tempString);
			}
		}

		private PropertyList list = null;
	}

	public void actionPerformed(ActionEvent e) {
		dirty = true;
		Object o = e.getSource();
		if (o instanceof Runnable) {
			((Runnable) o).run();
		}
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			if (flag) {
				if (e.getSource() == variables) {
					new EditCommand("Edit Variable", variables).run();
				}
				else if (e.getSource() == places) {
					new EditCommand("Edit Place", places).run();
				}
				else if (e.getSource() == transitions) {
					new EditCommand("Edit Transition", transitions).run();
				}
				else if (e.getSource() == controlFlow) {
					new EditCommand("Edit Movement", controlFlow).run();
				}
				flag = false;
			}
			else {
				flag = true;
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	private class EditCommand implements Runnable {
		public EditCommand(String name, PropertyList list) {
			this.name = name;
			this.list = list;
		}

		public void run() {
			if (name == null || name.equals("")) {
				Utility.createErrorMessage("Error", "Nothing selected to edit");
				return;
			}
			if (list.getSelectedValue() == null && getName().contains("Edit")) {
				Utility.createErrorMessage("Error", "Nothing selected to edit");
				return;
			}
			if (getName().contains("Variable")) {
				String selected = null;
				Boolean continuous = false;
				Boolean integer = false;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
					if (lhpnFile.isContinuous(selected)) {
						continuous = true;
					}
					else if (lhpnFile.isInteger(selected)) {
						integer = true;
					}
				}
				else {
					String temp = (String) JOptionPane.showInputDialog(mainPanel, "",
							"Variable Type Selection", JOptionPane.PLAIN_MESSAGE, null, varOptions,
							varOptions[0]);
					if (temp != null) {
						if (temp.equals(varOptions[1])) {
							continuous = true;
						}
						else if (temp.equals(varOptions[2])) {
							integer = true;
						}
					}
				}
				// log.addText(selected);
				VariablesPanel panel = new VariablesPanel(selected, list, continuous, integer,
						lhpnFile);
			}
			else if (getName().contains("Place")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				PlacePanel panel = new PlacePanel(selected, list, controlFlow, lhpnFile);
			}
			else if (getName().contains("Transition")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				TransitionsPanel panel = new TransitionsPanel(selected, list, controlFlow,
						lhpnFile, log);
			}
			else if (getName().contains("Movement")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				ControlFlowPanel panel = new ControlFlowPanel(selected, list, lhpnFile);
			}
		}

		public String getName() {
			return name;
		}

		private String name = null;

		private PropertyList list = null;
	}

}