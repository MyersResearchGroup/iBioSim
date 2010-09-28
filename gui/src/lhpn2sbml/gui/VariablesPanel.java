package lhpn2sbml.gui;

import lhpn2sbml.parser.*;

import gcm2sbml.gui.*;
//import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

//import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodelsim.BioSim;

public class VariablesPanel extends JPanel implements ActionListener {

	private String name = "", type = "", selList;

	private PropertyList variablesList;

	private String[] options = { "Ok", "Cancel" };

	private Boolean continuous, integer;

	private LhpnFile lhpn;

	private JPanel initPanel = null;

	private PropertyField initValue = null, initRate;

	private JComboBox modeBox, initBox;

	// private JComboBox typeBox, modeBox, initBox;

	private static final String[] types = new String[] { "boolean", "continuous", "discrete" };

	private static final String[] modes = new String[] { "input", "output" };

	private static final String[] booleans = new String[] { "true", "false", "unknown" };

	private HashMap<String, PropertyField> fields = null;
	
	private BioSim biosim;

	public VariablesPanel(String selected, PropertyList variablesList, Boolean boolCont,
			Boolean integer, LhpnFile lhpn, boolean atacs, BioSim biosim) {
		super(new GridLayout(4, 1));
		if (selected != null) {
			String[] array = selected.split(" ");
			this.name = array[0];
		}
		else {
			this.name = selected;
		}
		this.selList = selected;
		this.variablesList = variablesList;
		this.lhpn = lhpn;
		this.continuous = boolCont;
		this.integer = integer;
		this.biosim = biosim;

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.ATACSIDstring);
		fields.put(GlobalConstants.ID, field);
		add(field);

		// Type label
		JPanel tempPanel = new JPanel();
		if (continuous) {
			type = types[1];
		}
		else if (integer) {
			type = types[2];
		}
		else {
			type = types[0];
		}
		JLabel tempLabel = new JLabel("Type");
		JLabel typeLabel = new JLabel(type);
		// typeBox = new JComboBox(types);
		// typeBox.setSelectedItem(types[0]);
		// typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeLabel);
		add(tempPanel);

		// Initial field
		if (continuous || integer) {
			// JOptionPane.showMessageDialog(this, lhpn.isContinuous(selected));
			if (name != null) {
				String initVal = lhpn.getInitialVal(name);
					initValue = new PropertyField("Initial Value", initVal, null, null,
							Utility.NAMEstring);
			}
			else {
				initValue = new PropertyField("Initial Value", "0", null, null,
						Utility.NAMEstring);
			}
			fields.put("Initial value", initValue);
			add(initValue);
		}
		else {
			// JOptionPane.showMessageDialog(this, lhpn.isContinuous(selected));
			initPanel = new JPanel();
			JLabel initLabel = new JLabel("Initial Value");
			initBox = new JComboBox(booleans);
			initBox.setSelectedItem(booleans[1]);
			// initBox.addActionListener(this);
			initPanel.setLayout(new GridLayout(1, 2));
			initPanel.add(initLabel);
			initPanel.add(initBox);
			add(initPanel);
		}

		// Mode field
		if (!continuous && !integer) {
			tempPanel = new JPanel();
			JLabel modeLabel = new JLabel("Mode");
			modeBox = new JComboBox(modes);
			modeBox.setSelectedItem(modes[0]);
			modeBox.addActionListener(this);
			tempPanel.setLayout(new GridLayout(1, 2));
			if (atacs) {
				tempPanel.add(modeLabel);
				tempPanel.add(modeBox);
			}
			add(tempPanel);
		}

		// Initial rate field
		if (continuous) {
			if (name != null) {
				String rate = lhpn.getInitialRate(name);
					initRate = new PropertyField("Initial Rate", rate, null, null,
							Utility.NAMEstring);
			}
			else {
				initRate = new PropertyField("Initial Rate", "0", null, null, Utility.NAMEstring);
			}
			fields.put("Initial rate", initRate);
			add(initRate);
		}

		String oldName = null;
		if (name != null) {
			oldName = name;
			// Properties prop = lhpn.getVariables().get(selected);
			fields.get(GlobalConstants.ID).setValue(name);
			// System.out.print("after " +
			// fields.get(GlobalConstants.ID).getValue());
			// if (lhpn.isContinuous(selected)) {
			// typeBox.setSelectedItem(types[1]);
			// setType(types[1]);
			// }
			// else {
			// typeBox.setSelectedItem(types[0]);
			// setType(types[0]);
			// }
			if (lhpn.isContinuous(name) || lhpn.isInteger(name)) {
				String initVal = lhpn.getInitialVal(name);
				fields.get("Initial value").setValue(initVal);
			}
			else {
				HashMap<String, String> inits;
				if (lhpn.isInput(name)) {
					inits = lhpn.getInputs();
					// JOptionPane.showMessageDialog(this,
					// modeBox.getSelectedItem());
				}
				else {
					inits = lhpn.getOutputs();
					// JOptionPane.showMessageDialog(this, inits.toString());
				}
				// JOptionPane.showMessageDialog(this, inits.toString());
				initBox.setSelectedItem(inits.get(name));
			}
			if (!continuous && !integer) {
				if (lhpn.isInput(name)) {
					modeBox.setSelectedItem(modes[0]);
				}
				else {
					modeBox.setSelectedItem(modes[1]);
				}
			}
			if (lhpn.isContinuous(name)) {
				String initRate = lhpn.getInitialRate(name);
				fields.get("Initial rate").setValue(initRate);
				fields.get(GlobalConstants.ID).setValue(name);
				// System.out.print(" " +
				// fields.get(GlobalConstants.ID).getValue());
				// System.out.print(" " + fields.get("Initial
				// rate").getValue());
			}
			// loadProperties(prop);
		}

		// setType(types[0]);
		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
	}

	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValidValue()) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(biosim.frame(), this, "Variable Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				return false;
			}
			String[] allVariables = lhpn.getAllIDs();
			if (oldName == null) {
				for (int i = 0; i < allVariables.length; i++) {
					if (allVariables[i] != null) {
						if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
							Utility.createErrorMessage("Error", "Variable id already exists.");
							return false;
						}
					}
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				for (int i = 0; i < allVariables.length; i++) {
					if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
						Utility.createErrorMessage("Error", "Variable id already exists.");
						return false;
					}
				}
			}
			if (fields.containsKey("Initial value")) {
			if (!(fields.get("Initial value").getValue().matches("\\d+") || fields.get("Initial value").getValue().matches("\\[\\d+,\\d+\\]"))) {
				Utility.createErrorMessage("Error", "Initial value must be an integer or a range of integers.");
				return false;
			}
			}
			if (fields.containsKey("Initial rate")) {
			if (!(fields.get("Initial rate").getValue().matches("\\d+") || fields.get("Initial rate").getValue().matches("\\[\\d+,\\d+\\]"))) {
				Utility.createErrorMessage("Error", "Initial rate must be an integer or a range of integers.");
				return false;
			}
			}
			// System.out.print("after " +
			// fields.get(GlobalConstants.ID).getValue());
			String id = fields.get(GlobalConstants.ID).getValue();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			String tempVal = "";
			if (property.containsKey("Initial Value")) {
				tempVal = property.getProperty("Initial Value");
				property.setProperty("value", tempVal);
				property.remove("Initial Value");
			}
			if (property.containsKey("Initial Rate")) {
				tempVal = property.getProperty("Initial Rate");
				property.setProperty("rate", tempVal);
				property.remove("Initial Rate");
			}

			// property.put(GlobalConstants.TYPE,
			// typeBox.getSelectedItem().toString());

			if (name != null && !oldName.equals(id)) {
				lhpn.changeVariableName(oldName, id);
			}
			if (id.equals(oldName)) {
				lhpn.removeVar(id);
			}
			if (lhpn.isContinuous(id) || continuous) {
				// System.out.println("add var " + property);
				tempVal = fields.get("Initial value").getValue();
				lhpn.addContinuous(id, property);
			}
			else if (lhpn.isInteger(id) || integer) {
				// System.out.println("add var " + property);
				tempVal = fields.get("Initial value").getValue();
				lhpn.addInteger(id, tempVal);
			}
			else if (lhpn.isInput(id) || (!continuous && modeBox.getSelectedItem().equals("input"))) {
				// Boolean temp = false;
				// if (initBox.getSelectedItem().equals("true")) {
				// temp = true;
				// }
				lhpn.addInput(id, initBox.getSelectedItem().toString());
			}
			else if (lhpn.isOutput(id)
					|| (!continuous && modeBox.getSelectedItem().equals("output"))) {
				// Boolean temp = false;
				// if (initBox.getSelectedItem().equals("true")) {
				// temp = true;
				// }
				lhpn.addOutput(id, initBox.getSelectedItem().toString());
			}
			variablesList.removeItem(selList);
			String list;
			if (continuous || integer) {
				list = id + " - " + type + " - " + tempVal;
			}
			else {
				list = id + " - " + type + " - " + initBox.getSelectedItem().toString();
			}
			if (continuous) {
				tempVal = fields.get("Initial rate").getValue();
				list = list + " - " + tempVal;
			}
			variablesList.removeItem(list);
			variablesList.addItem(list);
			variablesList.setSelectedValue(id, true);

		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			// setType(typeBox.getSelectedItem().toString());
		}
	}

}
