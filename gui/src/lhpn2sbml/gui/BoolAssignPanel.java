package lhpn2sbml.gui;

import lhpn2sbml.parser.*;

import gcm2sbml.gui.*;
//import gcm2sbml.parser.GCMFile;
//import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;

//import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodelsim.Log;

public class BoolAssignPanel extends JPanel implements ActionListener {

	private String selected = "", oldName = null, transition, id;

	private PropertyList assignmentList, booleanList;

	//private String[] varList, boolList, contList;
	private String[] boolList;
	
	//private String[] values = {"true", "false"};
	private String[] options = { "Ok", "Cancel" };

	private LHPNFile lhpn;
	private Log log;

	//private JComboBox typeBox, varBox;
	private JComboBox varBox;//, valueBox;

	//private static final String[] types = { "boolean", "continuous", "rate" };

	private HashMap<String, PropertyField> fields = null;

	public BoolAssignPanel(String transition, String selected, PropertyList assignmentList, PropertyList booleanList,
			LHPNFile lhpn, Log log) {
		super(new GridLayout(6, 1));
		this.selected = selected;
		this.transition = transition;
		this.assignmentList = assignmentList;
		this.booleanList = booleanList;
		this.lhpn = lhpn;
		this.log = log;

		fields = new HashMap<String, PropertyField>();
		
		//log.addText(selected);
		boolList = lhpn.getBooleanVars();
		//contList = lhpn.getContVars(selected);
		//if (boolList.length > 0 && contList.length > 0) {
		//	System.arraycopy(boolList, 0, varList, 0, boolList.length);
		//	System.arraycopy(contList, 0, varList, boolList.length,
		//			contList.length);
		//}
		//if (boolList.length > 0) {
		//	System.arraycopy(boolList, 0, varList, 0, boolList.length);
		//}
		//else if (contList.length > 0) {
		//	System.arraycopy(contList, 0, varList, 0, contList.length);
		//}

		// ID field
		//PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
		//		Utility.NAMEstring);
		//fields.put(GlobalConstants.ID, field);
		//add(field);

		// Type field
		//JPanel tempPanel = new JPanel();
		//JLabel tempLabel = new JLabel("Type");
		//typeBox = new JComboBox(types);
		//typeBox.setSelectedItem(types[0]);
		//typeBox.addActionListener(this);
		//tempPanel.setLayout(new GridLayout(1, 2));
		//tempPanel.add(tempLabel);
		//tempPanel.add(typeBox);
		//add(tempPanel);
		
		// Variable field
		JPanel tempPanel = new JPanel();
		JLabel varLabel = new JLabel("Variable");
		varBox = new JComboBox(boolList);
		varBox.setSelectedItem(boolList[0]);
		varBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(varLabel);
		tempPanel.add(varBox);
		add(tempPanel);

		// Value field
		/*tempPanel = new JPanel();
		JLabel valueLabel = new JLabel("Assigned Value");
		valueBox = new JComboBox(values);
		valueBox.setSelectedItem(values[0]);
		valueBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(valueLabel);
		tempPanel.add(valueBox);
		add(tempPanel);*/
		
		PropertyField field = new PropertyField("Assigned Value", lhpn.getContAssign(transition, selected), null, null,
				Utility.NAMEstring);
		fields.put("Assignment value", field);
		add(field);
		
		if (selected != null) {
			oldName = selected;
			//Properties prop = lhpn.getVariables().get(selected);
			//fields.get(GlobalConstants.ID).setValue(selected);
			//if (lhpn.getBoolAssign(transition, selected)) {
				//fields.get("Assignment value").setValue(lhpn.getBoolAssign(transition, selected));
				//valueBox.setSelectedItem(values[0]);
			//}
			//else {
				//valueBox.setSelectedItem(values[1]);
			//}
			PropertyField assignField = fields.get("Assignment value");
			String[] tempArray = oldName.split(":=");
			assignField.setValue(tempArray[1]);
			fields.put("Assignment value", assignField);
			//loadProperties(prop);
		}

		//setType(types[0]);
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
		int value = JOptionPane.showOptionDialog(new JFrame(), this, "Boolean Assignment Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			//if (!checkValues()) {
			//	Utility.createErrorMessage("Error", "Illegal values entered.");
			//	return false;
			//}
			//if (oldName == null) {
			//	if (lhpn.getVariables().containsKey(varBox.getSelectedItem())) {
			//		Utility.createErrorMessage("Error", "Assignment id already exists.");
			//		return false;
			//	}
			//}
			//else if (!oldName.equals(varBox.getSelectedItem())) {
			//	if (lhpn.getVariables().containsKey(varBox.getSelectedItem())) {
			//		Utility.createErrorMessage("Error", "Assignment id already exists.");
			//		return false;
			//	}
			//}
			id = varBox.getSelectedItem().toString() + ":=" + fields.get("Assignment value").getValue();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			//for (PropertyField f : fields.values()) {
			//	if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
			//		property.put(f.getKey(), f.getValue());
			//	}
			//}
			property.put("Variable", varBox.getSelectedItem().toString());
			property.put("Value", fields.get("Assignment value").getValue());

			//if (selected != null && !oldName.equals(id)) {
			//	lhpn.changeVariableName(oldName, id);
			//}
			//else {
			//	lhpn.addBoolAssign(transition, id, property.getProperty("Value"));
			//}
			assignmentList.removeItem(oldName);
			assignmentList.addItem(id);
			assignmentList.setSelectedValue(id, true);
			booleanList.removeItem(oldName);
			booleanList.addItem(id);
		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			//setType(typeBox.getSelectedItem().toString());
		}
	}
	
	public void save() {
		Properties property = new Properties();
		//for (PropertyField f : fields.values()) {
		//	if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
		//		property.put(f.getKey(), f.getValue());
		//	}
		//}
		property.put("Variable", varBox.getSelectedItem().toString());
		property.put("Value", fields.get("Assignment value").getValue());

		if (selected != null && !oldName.equals(id)) {
			lhpn.changeVariableName(oldName, id);
		}
		else {
			log.addText("here " + property.getProperty("Value"));
			lhpn.addBoolAssign(transition, id, property.getProperty("Value"));
		}
	}

	//private void setType(String type) {
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
	//}

	private void loadProperties(Properties property) {
		for (Object o : property.keySet()) {
			if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			}
		}
	}

}
