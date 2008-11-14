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

import biomodelsim.Log;

public class RateAssignPanel extends JPanel implements ActionListener {

	private String selected = "", transition, id, oldVar = null, variable;

	private PropertyList assignmentList, rateAssignList;

	// private String[] varList, boolList, contList;
	private String[] rateList;

	private String[] options = { "Ok", "Cancel" };

	private LHPNFile lhpn;

	private Log log;

	// private JComboBox typeBox, varBox;
	private JComboBox varBox;

	// private static final String[] types = { "boolean", "continuous", "rate"
	// };

	private HashMap<String, PropertyField> fields = null;

	public RateAssignPanel(String transition, String selected, PropertyList assignmentList, PropertyList rateAssignList,
			LHPNFile lhpn, Log log) {
		super(new GridLayout(6, 1));
		this.selected = selected;
		this.transition = transition;
		this.assignmentList = assignmentList;
		this.rateAssignList = rateAssignList;
		this.lhpn = lhpn;
		this.log = log;

		fields = new HashMap<String, PropertyField>();

		rateList = lhpn.getContVars();
		// for (int i=0; i<rateList.length; i++) {
		// log.addText(rateList[i]);
		// }
		// if (boolList.length > 0 && contList.length > 0) {
		// System.arraycopy(boolList, 0, varList, 0, boolList.length);
		// System.arraycopy(contList, 0, varList, boolList.length,
		// contList.length);
		// }
		// if (boolList.length > 0) {
		// System.arraycopy(boolList, 0, varList, 0, boolList.length);
		// }
		// else if (contList.length > 0) {
		// System.arraycopy(contList, 0, varList, 0, contList.length);
		// }

		// ID field
		// PropertyField field = new PropertyField(GlobalConstants.ID, "", null,
		// null,
		// Utility.NAMEstring);
		// fields.put(GlobalConstants.ID, field);
		// add(field);

		// Type field
		// JPanel tempPanel = new JPanel();
		// JLabel tempLabel = new JLabel("Type");
		// typeBox = new JComboBox(types);
		// typeBox.setSelectedItem(types[0]);
		// typeBox.addActionListener(this);
		// tempPanel.setLayout(new GridLayout(1, 2));
		// tempPanel.add(tempLabel);
		// tempPanel.add(typeBox);
		// add(tempPanel);

		if (selected != null) {
			String[] idArray = selected.split("':=");
			variable = idArray[0];
		}

		// Variable field
		JPanel tempPanel = new JPanel();
		JLabel varLabel = new JLabel("Variable");
		varBox = new JComboBox(rateList);
		if (variable != null) {
			varBox.setSelectedItem(variable);
		}
		else {
			varBox.setSelectedItem(rateList[0]);
		}
		varBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(varLabel);
		tempPanel.add(varBox);
		add(tempPanel);

		// Initial field
		PropertyField field = new PropertyField("Assignment Value", lhpn.getRateAssign(transition,
				variable), null, null, Utility.NUMstring);
		fields.put("Assignment value", field);
		add(field);

		String oldName = null;
		if (selected != null) {
			oldVar = variable;
			oldName = selected;
			// Properties prop = lhpn.getVariables().get(selected);
			// log.addText(fields.get("ID") + selected);
			// fields.get(GlobalConstants.ID).setValue(selected);
			fields.get("Assignment value").setValue(lhpn.getRateAssign(transition, variable));
			// loadProperties(prop);
		}

		// setType(types[0]);
		boolean display = false;
		while (!display) {
			display = openGui(oldName, oldVar);
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

	private boolean openGui(String oldName, String oldVar) {
		int value = JOptionPane.showOptionDialog(new JFrame(), this, "Rate Assignment Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			//if (!checkValues()) {
			//	Utility.createErrorMessage("Error", "Illegal values entered.");
			//	return false;
			//}
			/*
			 * if (oldName == null) { String variable =
			 * varBox.getSelectedItem().toString(); String[] varArray =
			 * lhpn.getRateVars(transition); Boolean flag = false; for (int i =
			 * 0; i < varArray.length; i++) { if (varArray[i].equals(variable)) {
			 * flag = true; break; } } if (flag) {
			 * Utility.createErrorMessage("Error", "Assignment already
			 * exists.[0]"); return false; } } else if
			 * (!oldName.equals(varBox.getSelectedItem().toString() + ":=" +
			 * fields.get("Assignment Value"))) { String variable =
			 * varBox.getSelectedItem().toString(); String[] varArray =
			 * lhpn.getRateVars(transition); Boolean flag = false; for (int i =
			 * 0; i < varArray.length; i++) { if (varArray[i].equals(variable)) {
			 * flag = true; break; } } if (flag) {
			 * Utility.createErrorMessage("Error", "Assignment already
			 * exists.[1]"); return false; } }
			 */
			id = varBox.getSelectedItem().toString() + "':=" + fields.get("Assignment value").getValue();
			variable = varBox.getSelectedItem().toString();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			property.put("Variable", varBox.getSelectedItem().toString());

			// if (selected != null && !oldVar.equals(variable)) {
			// lhpn.changeVariableName(oldVar, variable);
			// }
			// else {
			// lhpn.addRateAssign(transition,
			// varBox.getSelectedItem().toString(),
			// property.getProperty("Assignment Value"));
			// }
			assignmentList.removeItem(oldName);
			assignmentList.addItem(id);
			assignmentList.setSelectedValue(id, true);
			rateAssignList.removeItem(oldName);
			rateAssignList.addItem(id);

		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			setID(varBox.getSelectedItem().toString() + "':=" + fields.get("Assignment Value"));
			// setType(typeBox.getSelectedItem().toString());
		}
	}

	public void save() {
		Properties property = new Properties();
		for (PropertyField f : fields.values()) {
			if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
				property.put(f.getKey(), f.getValue());
			}
		}
		property.put("Variable", varBox.getSelectedItem().toString());

		if (selected != null && !oldVar.equals(variable)) {
			lhpn.changeVariableName(oldVar, variable);
		}
		else {
			lhpn.addRateAssign(transition, varBox.getSelectedItem().toString(), property
					.getProperty("Assignment Value"));
		}
	}

	private void setID(String var) {
		fields.get(GlobalConstants.ID).setValue(var);
	}

	// private void setType(String type) {
	/*
	 * if (type.equals(types[0])) { //
	 * fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
	 * fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
	 * fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true); } else if
	 * (type.equals(types[1])) { //
	 * fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
	 * fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
	 * fields.get(GlobalConstants.KDECAY_STRING).setEnabled(false); } else { //
	 * fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
	 * fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
	 * fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true); }
	 */
	// }
	private void loadProperties(Properties property) {
		for (Object o : property.keySet()) {
			if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			}
		}
	}

}
