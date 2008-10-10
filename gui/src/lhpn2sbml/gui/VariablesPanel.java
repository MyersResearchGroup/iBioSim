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

	private LHPNFile lhpn;

	private JComboBox typeBox, modeBox;

	private static final String[] types = new String[] { "boolean", "continuous" };

	private static final String[] modes = new String[] { "input", "output" };

	private HashMap<String, PropertyField> fields = null;

	public VariablesPanel(String selected, PropertyList variablesList,
			LHPNFile lhpn) {
		super(new GridLayout(6, 1));
		this.selected = selected;
		this.variablesList = variablesList;
		this.lhpn = lhpn;

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.NAMEstring);
		fields.put(GlobalConstants.ID, field);
		add(field);

		// Name field
		field = new PropertyField(GlobalConstants.NAME, "", null, null, Utility.NAMEstring);
		fields.put(GlobalConstants.NAME, field);
		add(field);

		// Type field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel("Type");
		typeBox = new JComboBox(types);
		typeBox.setSelectedItem(types[0]);
		typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeBox);
		add(tempPanel);

		// Initial field
		field = new PropertyField("Initial Value", lhpn.getInitialVal(selected), null, null,
				Utility.NUMstring);
		fields.put("Initial value", field);
		add(field);

		// Mode field
		tempPanel = new JPanel();
		JLabel modeLabel  = new JLabel("Mode");
		modeBox = new JComboBox(modes);
		modeBox.setSelectedItem(modes[0]);
		modeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(modeLabel);
		tempPanel.add(modeBox);
		add(tempPanel);

		// Initial rate field
		if (lhpn.isContinuous(selected)) {
			field = new PropertyField("Initial Rate", lhpn.getInitialRate(selected), null, null,
					Utility.NUMstring);
			fields.put("Initial rate", field);
			add(field);
		}

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			Properties prop = lhpn.getVariables().get(selected);
			fields.get(GlobalConstants.ID).setValue(selected);
			if (lhpn.isContinuous(selected)) {
				typeBox.setSelectedItem(types[1]);
				setType(types[1]);
			}
			else {
				typeBox.setSelectedItem(types[0]);
				setType(types[0]);
			}
			fields.get("Initial value").setValue(lhpn.getInitialVal(selected));
			if (lhpn.isInput(selected)) {
				modeBox.setSelectedItem(modes[0]);
			}
			else {
				modeBox.setSelectedItem(modes[1]);
			}
			fields.get("Initial rate").setValue(lhpn.getInitialRate(selected));
			loadProperties(prop);
		}

		setType(types[0]);
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
					Utility.createErrorMessage("Error", "Species id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (lhpn.getVariables().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Species id already exists.");
					return false;
				}
			}
			String id = fields.get(GlobalConstants.ID).getValue();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			property.put(GlobalConstants.TYPE, typeBox.getSelectedItem().toString());

			if (selected != null && !oldName.equals(id)) {
				lhpn.changeVariableName(oldName, id);
			}
			if (lhpn.isContinuous(id)) {
				lhpn.addVar(id, property);
			}
			else if (lhpn.isInput(id)) {
				Boolean temp = false;
				if (property.get("Initial Value").equals("true")) {
					temp = true;
				}
				lhpn.addInput(id, temp);
			}
			else {
				Boolean temp = false;
				if (property.get("Initial Value").equals("true")) {
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
			setType(typeBox.getSelectedItem().toString());
		}
	}

	private void setType(String type) {
		/*if (type.equals(types[0])) {
			// fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
			fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
		}
		else if (type.equals(types[1])) {
			// fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
			fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(false);
		}
		else {
			// fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
			fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
		}
		*/
	}

	private void loadProperties(Properties property) {
		for (Object o : property.keySet()) {
			if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			}
		}
	}

}
