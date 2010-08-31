package lhpn2sbml.gui;

import lhpn2sbml.parser.*; //import lhpn2sbml.parser.Abstraction;

import gcm2sbml.gui.AbstractRunnableNamedButton; //import gcm2sbml.gui.InfluencePanel;
//import gcm2sbml.gui.ParameterPanel;
//import gcm2sbml.gui.PromoterPanel;
import gcm2sbml.gui.PropertyList;
import gcm2sbml.gui.Runnable; //import gcm2sbml.gui.SpeciesPanel;
import gcm2sbml.util.Utility;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import javax.swing.BoxLayout; //import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Properties;
//import java.util.Set;

//import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox; //import javax.swing.JComboBox;
//import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea; //import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JButton;

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
	private static final long serialVersionUID = 1L;

	// , MouseListener {
	private JTextField lhpnNameTextField; //, propertyField;

	private PropertyList variables, places, transitions, controlFlow, properties;

	// private BioSim biosim;
	private Log log;

	// private String directory;

	private String filename = "", directory = "", separator = "";

	private String[] varOptions = new String[] { "Boolean", "Continuous",
			"Discrete" };

	// private String lhpnName = "";

	private LhpnFile lhpnFile = null;

	private JPanel mainPanel;// buttonPanel;

	private JButton abstButton;

	private boolean flag = true, dirty = false;

	private BioSim biosim;

	public LHPNEditor() {
		super();
	}

	public LHPNEditor(String directory, String filename, LhpnFile lhpn,
			BioSim biosim, Log log) {
		super();
		this.biosim = biosim;
		this.log = log;
		addMouseListener(biosim);

		if (File.separator.equals("\\")) {
			separator = "\\\\";
		} else {
			separator = File.separator;
		}

		// this.directory = directory;
		lhpnFile = lhpn;
		if (lhpnFile == null) {
			lhpnFile = new LhpnFile();
		}
		this.directory = directory;
		if (filename != null) {
			File f = new File(directory + separator + filename);
			if (!(f.length() == 0)) {
				lhpnFile.load(directory + separator + filename);
			}
			this.filename = filename;
			// this.lhpnName = filename.replace(".lpn", "");
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
		//propertyField = new JTextField(lhpnFile.getProperty(), 15);
		//propertyField.setEditable(true);
		//propertyField.addActionListener(this);
		JLabel lhpnNameLabel = new JLabel("LPN Id:");
		//JLabel propertyLabel = new JLabel("Property:");
		abstButton = new JButton("Abstract LPN");
		abstButton.addActionListener(this);
		mainPanelNorth.add(lhpnNameLabel);
		mainPanelNorth.add(lhpnNameTextField);
		//mainPanelNorth.add(propertyLabel);
		//mainPanelNorth.add(propertyField);
		// mainPanelNorth.add(abstButton);

		// buttonPanel = new JPanel();
		// JButton save = new JButton("Save");
		// save.addActionListener(this);
		// buttonPanel.add(save);
		// add(buttonPanel, BorderLayout.SOUTH);
		
		properties = new PropertyList("Property List");
		properties.addMouseListener(this);
		EditButton addProp = new EditButton("Add Property", properties);
		RemoveButton removeProp = new RemoveButton("Remove Property", properties);
		EditButton editProp = new EditButton("Edit Property", properties);
		if (!lhpnFile.getProperties().equals(null)) {
			for (String s : lhpnFile.getProperties()) {
				properties.addItem(s);
			}
		}
		JPanel propPanel = Utility.createPanel(this, "Properties", properties,
				addProp, removeProp, editProp);

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainPanelNorth, "North");
		mainPanel.add(mainPanelCenter, "Center");
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Main Elements", mainPanel);
		tab.addTab("Properties", propPanel);
		setLayout(new BorderLayout());
		add(tab, BorderLayout.CENTER);

		JPanel buttons = new JPanel();
		SaveButton saveButton = new SaveButton("Save LPN", lhpnNameTextField);
		buttons.add(saveButton);
		saveButton.addActionListener(this);
		saveButton = new SaveButton("Save LPN as", lhpnNameTextField);
		buttons.add(saveButton);
		saveButton.addActionListener(this);

		// log.addText("build panels");
		variables = new PropertyList("Variable List");
		variables.addMouseListener(this);
		EditButton addVar = new EditButton("Add Variable", variables);
		RemoveButton removeVar = new RemoveButton("Remove Variable", variables);
		EditButton editVar = new EditButton("Edit Variable", variables);
		if (!lhpnFile.getVariables().equals(null)) {
			// variables.addAllItem(lhpnFile.getVariables().keySet());
			for (String s : lhpnFile.getContinuous().keySet()) {
				variables.addItem(s + " - continuous - "
						+ lhpnFile.getInitialVal(s) + " - "
						+ lhpnFile.getInitialRate(s));
			}
		}
		if (!lhpnFile.getVariables().equals(null)) {
			// variables.addAllItem(lhpnFile.getIntegers().keySet());
			for (String s : lhpnFile.getIntegers().keySet()) {
				variables.addItem(s + " - discrete - "
						+ lhpnFile.getInitialVal(s));
			}
		}
		if (!lhpnFile.getInputs().equals(null)) {
			for (String s : lhpnFile.getInputs().keySet()) {
				variables.addItem(s + " - boolean - "
						+ lhpnFile.getInitialVal(s));
			}
			// variables.addAllItem(lhpnFile.getInputs().keySet());
		}
		if (!lhpnFile.getOutputs().equals(null)) {
			// variables.addAllItem(lhpnFile.getOutputs().keySet());
			for (String s : lhpnFile.getOutputs().keySet()) {
				variables.addItem(s + " - boolean - "
						+ lhpnFile.getInitialVal(s));
			}
		}

		JPanel varPanel = Utility.createPanel(this, "Variables", variables,
				addVar, removeVar, editVar);
		mainPanelCenterCenter.add(varPanel);

		// log.addText("build place panel");
		places = new PropertyList("Place List");
		places.addMouseListener(this);
		EditButton addPlace = new EditButton("Add Place", places);
		RemoveButton removePlace = new RemoveButton("Remove Place", places);
		EditButton editPlace = new EditButton("Edit Place", places);
		for (String s : lhpnFile.getPlaceList()) {
			if (lhpnFile.getPlace(s).isMarked()) {
				places.addItem(s + " - marked");
			} else {
				places.addItem(s + " - unmarked");
			}
		}
		// places.addAllItem(lhpnFile.getPlaces().keySet());

		JPanel placePanel = Utility.createPanel(this, "Places", places,
				addPlace, removePlace, editPlace);
		mainPanelCenterCenter.add(placePanel);

		// log.addText("build transition panel");
		transitions = new PropertyList("Transition List");
		transitions.addMouseListener(this);
		EditButton addTrans = new EditButton("Add Transition", transitions);
		RemoveButton removeTrans = new RemoveButton("Remove Transition",
				transitions);
		EditButton editTrans = new EditButton("Edit Transition", transitions);
		transitions.addAllItem(lhpnFile.getTransitionList());

		JPanel transPanel = Utility.createPanel(this, "Transitions",
				transitions, addTrans, removeTrans, editTrans);
		mainPanelCenterCenter.add(transPanel);

		// log.addText("build control panel");
		controlFlow = new PropertyList("Control Flow");
		controlFlow.addMouseListener(this);
		EditButton addFlow = new EditButton("Add Movement", controlFlow);
		RemoveButton removeFlow = new RemoveButton("Remove Movement",
				controlFlow);
		EditButton editFlow = new EditButton("Edit Movement", controlFlow);
		// log.addText("get control panel");
		controlFlow.addAllItem(lhpnFile.getControlFlow());
		// log.addText("got control panel");

		JPanel flowPanel = Utility.createPanel(this, "Control Flow",
				controlFlow, addFlow, removeFlow, editFlow);
		mainPanelCenterCenter.add(flowPanel);

	}

	public boolean isDirty() {
		return dirty;
	}

	public void save() {
		//lhpnFile.addProperty(propertyField.getText());
		lhpnFile.save(directory + separator + filename);
		dirty = false;
		biosim.updateAsyncViews(filename);
	}

	public void saveAs(String newName) {
		dirty = false;
		//lhpnFile.addProperty(propertyField.getText());
		lhpnFile.save(directory + separator + newName);
		reload(newName);
		biosim.addToTree(newName);
		biosim.updateAsyncViews(newName);
	}

	public void viewLhpn() {
		try {
			File work = new File(directory);
			if (new File(directory + separator + filename).exists()) {
				String dotFile = filename.replace(".lpn", ".dot");
				File dot = new File(directory + separator + dotFile);
				dot.delete();
				String command = "";
				if (System.getProperty("os.name").contentEquals("Linux")) {
					command = "gnome-open " + dotFile;
				} else {
					command = "open " + dotFile;
				}
				Runtime exec = Runtime.getRuntime();
				log.addText("Executing:\natacs -cPllodpl " + filename + "\n");
				Process load = exec.exec("atacs -cPllodpl " + filename, null,
						work);
				load.waitFor();
				if (dot.exists()) {
					exec.exec(command, null, work);
				} else {
					File log = new File(directory + separator + "atacs.log");
					BufferedReader input = new BufferedReader(new FileReader(
							log));
					String line = null;
					JTextArea messageArea = new JTextArea();
					while ((line = input.readLine()) != null) {
						messageArea.append(line);
						messageArea
								.append(System.getProperty("line.separator"));
					}
					input.close();
					messageArea.setLineWrap(true);
					messageArea.setWrapStyleWord(true);
					messageArea.setEditable(false);
					JScrollPane scrolls = new JScrollPane();
					scrolls.setMinimumSize(new Dimension(500, 500));
					scrolls.setPreferredSize(new Dimension(500, 500));
					scrolls.setViewportView(messageArea);
					JOptionPane.showMessageDialog(biosim.frame(), scrolls,
							"Log", JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this,
						"No circuit has been generated yet.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(this, "Unable to view circuit.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void reload(String newName) {
		filename = newName + ".lpn";
		if (new File(directory + separator + newName).exists()) {
			lhpnFile.load(directory + separator + newName);
		}
		lhpnNameTextField.setText(newName);
	}

	public class SaveButton extends AbstractRunnableNamedButton {
		private static final long serialVersionUID = 1L;

		public SaveButton(String name, JTextField fieldNameField) {
			super(name);
			// this.fieldNameField = fieldNameField;
		}

		public void run() {
			lhpnFile.save(getName());
		}

		// private JTextField fieldNameField = null;
	}

	public class EditButton extends AbstractRunnableNamedButton {
		private static final long serialVersionUID = 1L;

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
		private static final long serialVersionUID = 1L;

		public RemoveButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		public void run() {
			if (getName().contains("Variable")) {
				if (!list.isSelectionEmpty()) {
					String name = list.getSelectedValue().toString();
					String var = name.split("\\s")[0];
					if (lhpnFile.removeVar(var) == false) {
						JOptionPane.showMessageDialog(this,
								"Must delete assignments to variable " + name,
								"Cannot remove variable" + name,
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					variables.removeItem(name);
					lhpnFile.removeVar(var);
				}
			} else if (getName().contains("Place")) {
				if (!list.isSelectionEmpty()) {
					String id = list.getSelectedValue().toString();
					String[] array = id.split(" ");
					String name = array[0];
					if (lhpnFile.containsMovement(name)) {
						JOptionPane.showMessageDialog(this, "Must remove "
								+ name + " from control flow",
								"Cannot remove place " + name,
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					lhpnFile.removePlace(name);
					places.removeItem(id);
				}
			} else if (getName().contains("Transition")) {
				if (!list.isSelectionEmpty()) {
					String name = list.getSelectedValue().toString();
					if (lhpnFile.containsMovement(name)) {
						JOptionPane.showMessageDialog(this, "Must remove "
								+ name + " from control flow",
								"Cannot remove transition " + name,
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					lhpnFile.removeTransition(name);
					transitions.removeItem(name);
				}
			} else if (getName().contains("Movement")) {
				if (list.getSelectedValue() != null) {
					String tempString = list.getSelectedValue().toString();
					String[] tempArray = tempString.split("\\s");
					lhpnFile.removeMovement(tempArray[0], tempArray[1]);
					controlFlow.removeItem(tempString);
				}
			} else if (getName().contains("Property")) {
				if (list.getSelectedValue() != null) {
					String tempString = list.getSelectedValue().toString();
					String[] tempArray = tempString.split("\\s");
					lhpnFile.removeProperty(tempString);
					properties.removeItem(tempString);
				}
			}
		}

		private PropertyList list = null;
	}

	public void actionPerformed(ActionEvent e) {
		dirty = true;
		Object o = e.getSource();
		if (o instanceof Runnable) {
			((Runnable) o).run();
		} else if (e.getSource() == abstButton) {
			// Abstraction abst = lhpnFile.abstractLhpn();
			String[] boolVars = lhpnFile.getBooleanVars();
			String[] contVars = lhpnFile.getContVars();
			String[] intVars = lhpnFile.getIntVars();
			String[] variables = new String[boolVars.length + contVars.length
					+ intVars.length];
			int k = 0;
			for (int j = 0; j < contVars.length; j++) {
				variables[k] = contVars[j];
				k++;
			}
			for (int j = 0; j < intVars.length; j++) {
				variables[k] = intVars[j];
				k++;
			}
			for (int j = 0; j < boolVars.length; j++) {
				variables[k] = boolVars[j];
				k++;
			}
			// String[] vars = null;
			String abstFilename = (String) JOptionPane.showInputDialog(this,
					"Please enter the file name for the abstracted LPN.",
					"Enter Filename", JOptionPane.PLAIN_MESSAGE);
			if (abstFilename != null) {
				if (!abstFilename.endsWith(".lpn"))
					abstFilename = abstFilename + ".lpn";
				String[] options = { "Ok", "Cancel" };
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				JCheckBox[] list = new JCheckBox[variables.length];
				ArrayList<String> tempVars = new ArrayList<String>();
				for (int i = 0; i < variables.length; i++) {
					JCheckBox temp = new JCheckBox(variables[i]);
					panel.add(temp);
					list[i] = temp;
				}
				int value = JOptionPane.showOptionDialog(biosim.frame(), panel,
						"Variable Assignment Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options, options[0]);
				if (value == JOptionPane.YES_OPTION) {
					for (int i = 0; i < list.length; i++) {
						if (list[i].isSelected()) {
							tempVars.add(variables[i]);
						}
					}
					String[] vars = new String[tempVars.size()];
					for (int i = 0; i < tempVars.size(); i++) {
						vars[i] = tempVars.get(i);
					}
					// abst.abstractVars(vars);
				}
				// abst.abstractSTG();
				// abst.save(directory + separator + abstFilename);
				// biosim.refreshTree();
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			if (flag) {
				if (e.getSource() == variables) {
					new EditCommand("Edit Variable", variables).run();
				} else if (e.getSource() == places) {
					new EditCommand("Edit Place", places).run();
				} else if (e.getSource() == transitions) {
					new EditCommand("Edit Transition", transitions).run();
				} else if (e.getSource() == controlFlow) {
					new EditCommand("Edit Movement", controlFlow).run();
				} else if (e.getSource() == properties) {
					new EditCommand("Edit Property", properties).run();
				}
				flag = false;
			} else {
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
				// log.addText(list.getSelectedValue().toString());
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
					String[] array = selected.split(" ");
					String VarName = array[0];
					if (lhpnFile.isContinuous(VarName)) {
						continuous = true;
					} else if (lhpnFile.isInteger(VarName)) {
						integer = true;
					}
					new VariablesPanel(selected, list, continuous, integer,
							lhpnFile, biosim.atacs, biosim);
				} else {
					String temp = (String) JOptionPane.showInputDialog(
							mainPanel, "", "Variable Type Selection",
							JOptionPane.PLAIN_MESSAGE, null, varOptions,
							varOptions[0]);
					if (temp != null) {
						if (temp.equals(varOptions[1])) {
							continuous = true;
						} else if (temp.equals(varOptions[2])) {
							integer = true;
						}
						new VariablesPanel(selected, list, continuous, integer,
								lhpnFile, biosim.atacs, biosim);
					}
				}
				// log.addText(selected);
			} else if (getName().contains("Place")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
					String[] array = selected.split(" ");
					selected = array[0];
				}
				new PlacePanel(selected, list, controlFlow, lhpnFile, biosim);
			} else if (getName().contains("Transition")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				new TransitionsPanel(selected, list, controlFlow, lhpnFile,
						log, biosim);
			} else if (getName().contains("Movement")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				new ControlFlowPanel(selected, list, lhpnFile, biosim);
			} else if (getName().contains("Property")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				new PropertyPanel(selected, list, lhpnFile, biosim);
			}
		}

		public String getName() {
			return name;
		}

		private String name = null;

		private PropertyList list = null;
	}

}