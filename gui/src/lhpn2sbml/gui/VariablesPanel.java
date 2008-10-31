package lhpn2sbml.gui;

import lhpn2sbml.parser.*;

import gcm2sbml.gui.*;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class VariablesPanel extends JPanel implements ActionListener {

	private String selected = "";

	private PropertyList variablesList;

	private String[] options = { "Ok", "Cancel" };

	private Boolean boolCont;

	private LHPNFile lhpn;

	private JPanel initPanel = null;

	private PropertyField initField = null, rateField;

	private JComboBox modeBox, initBox;

	// private JComboBox typeBox, modeBox, initBox;

	private static final String[] types = new String[] { "boolean", "continuous" };

	private static final String[] modes = new String[] { "input", "output" };

	private static final String[] booleans = new String[] { "true", "false" };

	private HashMap<String, PropertyField> fields = null;

	public VariablesPanel(String selected, PropertyList variablesList, Boolean boolCont,
			LHPNFile lhpn) {
		super(new GridLayout(6, 1));
		this.selected = selected;
		this.variablesList = variablesList;
		this.lhpn = lhpn;
		this.boolCont = boolCont;

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.NAMEstring);
		fields.put(GlobalConstants.ID, field);
		add(field);

		// Type label
		JPanel tempPanel = new JPanel();
		String type = "";
		if (boolCont) {
			type = types[1];
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
		if (boolCont) {
			// JOptionPane.showMessageDialog(this, lhpn.isContinuous(selected));
			if (selected != null) {
				initField = new PropertyField("Initial Value", lhpn.getInitialVal(selected), null,
						null, Utility.NUMstring);
			}
			else {
				initField = new PropertyField("Initial Value", "0", null, null, Utility.NUMstring);
			}
			fields.put("Initial value", initField);
			add(initField);
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
		if (!boolCont) {
			tempPanel = new JPanel();
			JLabel modeLabel = new JLabel("Mode");
			modeBox = new JComboBox(modes);
			modeBox.setSelectedItem(modes[0]);
			modeBox.addActionListener(this);
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(modeLabel);
			tempPanel.add(modeBox);
			add(tempPanel);
		}

		// Initial rate field
		if (boolCont) {
			if (selected != null) {
				rateField = new PropertyField("Initial Rate", lhpn.getInitialRate(selected), null,
						null, Utility.NUMstring);
			}
			else {
				rateField = new PropertyField("Initial Rate", "0", null, null, Utility.NUMstring);
			}
			fields.put("Initial rate", rateField);
			add(rateField);
		}

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			// Properties prop = lhpn.getVariables().get(selected);
			fields.get(GlobalConstants.ID).setValue(selected);
			//System.out.print("after " + fields.get(GlobalConstants.ID).getValue());
			// if (lhpn.isContinuous(selected)) {
			// typeBox.setSelectedItem(types[1]);
			// setType(types[1]);
			// }
			// else {
			// typeBox.setSelectedItem(types[0]);
			// setType(types[0]);
			// }
			if (lhpn.isContinuous(selected)) {
				fields.get("Initial value").setValue(lhpn.getInitialVal(selected));
			}
			else {
				HashMap<String, Boolean> inits;
				if (lhpn.isInput(selected)) {
					inits = lhpn.getInputs();
					// JOptionPane.showMessageDialog(this,
					// modeBox.getSelectedItem());
				}
				else {
					inits = lhpn.getOutputs();
					// JOptionPane.showMessageDialog(this, inits.toString());
				}
				// JOptionPane.showMessageDialog(this, inits.toString());
				if (inits.get(selected)) {
					initBox.setSelectedItem(booleans[0]);
				}
				else {
					initBox.setSelectedItem(booleans[1]);
				}
			}
			if (!boolCont) {
				if (lhpn.isInput(selected)) {
					modeBox.setSelectedItem(modes[0]);
				}
				else {
					modeBox.setSelectedItem(modes[1]);
				}
			}
			if (lhpn.isContinuous(selected)) {
				fields.get("Initial rate").setValue(lhpn.getInitialRate(selected));
				fields.get(GlobalConstants.ID).setValue(selected);
				//System.out.print(" " + fields.get(GlobalConstants.ID).getValue());
				//System.out.print(" " + fields.get("Initial rate").getValue());
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
			if (!f.isValid()) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(new JFrame(), this, "Variable Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				if (lhpn.getVariables().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Variable id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (lhpn.getVariables().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Variable id already exists.");
					return false;
				}
			}
			//System.out.print("after " + fields.get(GlobalConstants.ID).getValue());
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

			if (selected != null && !oldName.equals(id)) {
				lhpn.changeVariableName(oldName, id);
			}
			if (id.equals(oldName)) {
				lhpn.removeVar(id);
			}
			if (lhpn.isContinuous(id) || boolCont) {
				//System.out.println("add var " + property);
				lhpn.addVar(id, property);
			}
			else if (lhpn.isInput(id) || (!boolCont && modeBox.getSelectedItem().equals("input"))) {
				Boolean temp = false;
				if (initBox.getSelectedItem().equals("true")) {
					temp = true;
				}
				lhpn.addInput(id, temp);
			}
			else if (lhpn.isOutput(id) || (!boolCont && modeBox.getSelectedItem().equals("output"))) {
				Boolean temp = false;
				if (initBox.getSelectedItem().equals("true")) {
					temp = true;
				}
				lhpn.addOutput(id, temp);
			}
			variablesList.removeItem(oldName);
			variablesList.addItem(id);
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

	private void setType(String type) {
		// JOptionPane.showMessageDialog(this, type + types[0] +
		// type.equals(types[0]));
		if (type.equals(types[0]) && initField != null) {
			// JOptionPane.showMessageDialog(this, type + types[0] +
			// type.equals(types[0]));
			remove(initField);
			remove(rateField);
			initField = null;
			fields.remove("Initial value");
			initPanel = new JPanel();
			JLabel initLabel = new JLabel("Initial Value");
			initBox = new JComboBox(booleans);
			initBox.setSelectedItem(booleans[0]);
			initBox.addActionListener(this);
			initPanel.setLayout(new GridLayout(1, 2));
			initPanel.add(initLabel);
			initPanel.add(initBox);
			add(initPanel);
			HashMap<String, Boolean> inits;
			if (!boolCont) {
				if (modeBox.getSelectedIndex() == 0) {
					inits = lhpn.getInputs();
				}
				else {
					inits = lhpn.getOutputs();
				}
				if (inits.get(selected)) {
					initBox.setSelectedItem(booleans[1]);
				}
				else {
					initBox.setSelectedItem(booleans[0]);
				}
			}
			lhpn.removeVar(selected);

		}
		else if (type.equals(types[1]) && initPanel != null) {
			// JOptionPane.showMessageDialog(this, "here");
			remove(initPanel);
			initPanel = null;
			initField = new PropertyField("Initial Value", lhpn.getInitialVal(selected), null,
					null, Utility.NUMstring);
			fields.put("Initial value", initField);
			add(initField);
			if (lhpn.isInput(selected)) {
				lhpn.removeInput(selected);
			}
			else if (lhpn.isOutput(selected)) {
				lhpn.removeOutput(selected);
			}
			Properties prop = new Properties();
			prop.setProperty("value", "0");
			prop.setProperty("rate", "0");
			lhpn.addVar(selected, prop);
		}
	}

	//private void loadProperties(Properties property) {
	//	for (Object o : property.keySet()) {
	//		if (fields.containsKey(o.toString())) {
	//			fields.get(o.toString()).setValue(property.getProperty(o.toString()));
	//			fields.get(o.toString()).setCustom();
	//		}
	//	}
	//}

}
