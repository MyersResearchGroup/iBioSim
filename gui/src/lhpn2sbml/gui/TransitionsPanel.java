package lhpn2sbml.gui;

import lhpn2sbml.gui.LHPNEditor.EditButton;
import lhpn2sbml.gui.LHPNEditor.RemoveButton;
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

public class TransitionsPanel extends JPanel implements ActionListener {

	private String selected = "";

	private PropertyList transitionsList, boolAssignments, varAssignments, rateAssignments;

	private String[] options = { "Ok", "Cancel" };

	private LHPNFile lhpn;

	private HashMap<String, PropertyField> fields = null;

	public TransitionsPanel(String selected, PropertyList transitionsList,
			LHPNFile lhpn) {
		super(new GridLayout(6, 1));
		this.selected = selected;
		this.transitionsList = transitionsList;
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

		// Delay field
		field = new PropertyField("Delay", "", null, null, "\\[\\d+,\\d+\\]");
		fields.put("Delay", field);
		add(field);
		
		// Boolean Assignment panel
		LHPNEditor editor = new LHPNEditor();
		boolAssignments = new PropertyList("Boolean Assignment List");
		EditButton addBoolAssign = editor.new EditButton("Add Assignment", boolAssignments);
		RemoveButton removeBoolAssign = editor.new RemoveButton("Remove Assignment", boolAssignments);
		EditButton editBoolAssign = editor.new EditButton("Edit Assignment", boolAssignments);
		boolAssignments.addAllItem(lhpn.getBooleanVars(selected));
		
		JPanel boolAssignPanel = Utility.createPanel(this, "Boolean Assignments", boolAssignments, addBoolAssign,
				removeBoolAssign, editBoolAssign);
		add(boolAssignPanel);
		
		// Variable Assignment panel
		varAssignments = new PropertyList("Variable Assignment List");
		EditButton addVarAssign = editor.new EditButton("Add Assignment", varAssignments);
		RemoveButton removeVarAssign = editor.new RemoveButton("Remove Assignment", varAssignments);
		EditButton editVarAssign = editor.new EditButton("Edit Assignment", varAssignments);
		//boolAssignments.addAllItem(lhpn.getVariables(selected));
		
		JPanel varAssignPanel = Utility.createPanel(this, "Variable Assignments", varAssignments, addVarAssign,
				removeVarAssign, editVarAssign);
		add(varAssignPanel);
		
		
		//assignments.addAllItem(lhpn.getVariableVars(selected));
		//assignments.addAllItem(lhpn.getRateVars(selected));
		
		//JPanel assignPanel = Utility.createPanel(this, "Assignments", assignments, addAssign,
		//		removeAssign, editAssign);
		//add(assignPanel);

		// Boolean Assignment field
		field = new PropertyField("Boolean Assignments", "", null, null,
				Utility.NAMEstring);
		fields.put("Boolean Assignments", field);
		add(field);
		
		// Continuous Assignment field
		field = new PropertyField("Continuous Assignments", "", null, null,
				Utility.NAMEstring);
		fields.put("Continuous Assignments", field);
		add(field);
		
		// Rate Assignment field
		field = new PropertyField("Rate Assignments", "", null, null,
				Utility.NAMEstring);
		fields.put("Rate Assignments", field);
		add(field);

		// Enabling condition field
		field = new PropertyField("Enabling Condition", "", null, null,
				Utility.NAMEstring);
		fields.put("Enabling Condition", field);
		add(field);

		String oldName = null;
		/*if (selected != null) {
			oldName = selected;
			Properties prop = lhpn.getVariables().get(selected);
			fields.get(GlobalConstants.ID).setValue(selected);
			fields.get("Delay").setValue(lhpn.getDelay(selected));
			fields.get("Assignments").setValue(lhpn.getRateAssignments(selected));
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
				modeBox.setSelectedItem(types[0]);
			}
			else {
				modeBox.setSelectedItem(types[1]);
			}
			fields.get("Initial rate").setValue(lhpn.getInitialRate(selected));
			loadProperties(prop);
		}
*/
	//	setType(types[0]);
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

			if (selected != null && !oldName.equals(id)) {
				lhpn.changeVariableName(oldName, id);
			}
			lhpn.addTransition(id);
			transitionsList.removeItem(oldName);
			transitionsList.addItem(id);
			transitionsList.setSelectedValue(id, true);

		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			
		}
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
