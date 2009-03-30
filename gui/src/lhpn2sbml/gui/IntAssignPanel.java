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

//import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class IntAssignPanel extends JPanel implements ActionListener {

	private String selected = "", transition, id, oldName = null;

	private PropertyList assignmentList, integerList;

	//private String[] varList, boolList, contList;
	private String[] intList;
	
	private String[] options = { "Ok", "Cancel" };

	private LHPNFile lhpn;

	//private JComboBox typeBox, varBox;
	private JComboBox varBox;

	//private static final String[] types = { "boolean", "continuous", "rate" };

	private HashMap<String, PropertyField> fields = null;

	public IntAssignPanel(String transition, String selected, PropertyList assignmentList, PropertyList integerList,
			LHPNFile lhpn) {
		super(new GridLayout(6, 1));
		this.selected = selected;
		this.transition = transition;
		this.assignmentList = assignmentList;
		this.integerList = integerList;
		this.lhpn = lhpn;

		fields = new HashMap<String, PropertyField>();
		
		intList = lhpn.getIntVars();
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
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.NAMEstring);
		fields.put(GlobalConstants.ID, field);
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
		varBox = new JComboBox(intList);
		varBox.setSelectedItem(intList[0]);
		varBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(varLabel);
		tempPanel.add(varBox);
		add(tempPanel);

		// Initial field
		field = new PropertyField("Assignment Value", lhpn.getContAssign(transition, selected), null, null,
				Utility.NAMEstring);
		fields.put("Assignment value", field);
		add(field);
		
		if (selected != null) {
			oldName = selected;
			Properties prop = lhpn.getVariables().get(selected);
			PropertyField idField = fields.get(GlobalConstants.ID);
			PropertyField assignField = fields.get("Assignment value");
			//System.out.println(selected);
			idField.setValue(selected);
			String[] tempArray = oldName.split(":=");
			assignField.setValue(tempArray[1]);
			fields.put(GlobalConstants.ID, idField);
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
		int value = JOptionPane.showOptionDialog(new JFrame(), this, "Variable Assignment Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				Properties prop = lhpn.getIntVars(selected);
				if (prop != null) {
				if (prop.containsKey((String) varBox.getSelectedItem())) {
					Utility.createErrorMessage("Error", "Assignment id already exists.");
					return false;
				}
				}
			}
			//else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
			//	if (lhpn.getVariables().containsKey(fields.get(GlobalConstants.ID).getValue())) {
			//		Utility.createErrorMessage("Error", "Assignment id already exists.");
			//		return false;
			//	}
			//}
			id = varBox.getSelectedItem().toString() + ":=" + fields.get("Assignment value").getValue();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				//System.out.println(f.getKey());
				if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			property.put("Variable", varBox.getSelectedItem().toString());

			//if (selected != null && !oldName.equals(id)) {
			//	lhpn.removeContAssign(selected, oldName);
			//}
			//else {
			//	System.out.println(transition + " " + id + " " + property.getProperty("Assignment value"));
			//	lhpn.addContAssign(transition, id, property.getProperty("Value"));
			//}
			assignmentList.removeItem(oldName);
			//System.out.println(id);
			assignmentList.addItem(id);
			assignmentList.setSelectedValue(id, true);
			integerList.removeItem(oldName);
			integerList.addItem(id);

		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			setID(varBox.getSelectedItem().toString());
			//setType(typeBox.getSelectedItem().toString());
		}
	}
	
	private void setID(String var) {
		PropertyField idField = fields.get(GlobalConstants.ID);
		idField.setValue(var);
		fields.put(GlobalConstants.ID, idField);
	}
	
	public void save() {
		Properties property = new Properties();
		for (PropertyField f : fields.values()) {
			if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
				property.put(f.getKey(), f.getValue());
			}
		}
		property.put("Variable", varBox.getSelectedItem().toString());

		if (selected != null && !oldName.equals(id)) {
			String[] selectArray = selected.split(":=");
			String[] oldArray = oldName.split(":=");
			lhpn.removeIntAssign(selectArray[0], oldArray[0]);
		}
		else {
			//System.out.println(transition + " " + id + " " + property.getProperty("Assignment value"));
			lhpn.addIntAssign(transition, id, property.getProperty("Value"));
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
