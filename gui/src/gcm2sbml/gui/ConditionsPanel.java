package gcm2sbml.gui;

import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodelsim.BioSim;

public class ConditionsPanel extends JPanel {
	public ConditionsPanel(String selected, PropertyList conditionList, GCMFile gcm,
			boolean paramsOnly) {
		super(new GridLayout(1, 1));
		this.selected = selected;
		this.conditionList = conditionList;
		this.gcm = gcm;

		fields = new HashMap<String, PropertyField>();

		// Condition field
		PropertyField field = new PropertyField("Property", "", null, null, "Property",
				paramsOnly, "default");
		fields.put("Property", field);
		add(field);

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			fields.get("Property").setValue(selected);
		}

		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(BioSim.frame, this, "Property Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (oldName == null) {
				if (gcm.getConditions().contains(fields.get("Property").getValue())) {
					Utility.createErrorMessage("Error", "Property already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get("Property").getValue())) {
				if (gcm.getConditions().contains(fields.get("Property").getValue())) {
					Utility.createErrorMessage("Error", "Property already exists.");
					return false;
				}
			}
			String id = fields.get("Property").getValue();

			if (selected != null) {
				gcm.removeCondition(oldName);
			}
			id = gcm.addCondition(id);
			if (id != null) {
				conditionList.removeItem(oldName);
				conditionList.addItem(id);
				conditionList.setSelectedValue(id, true);
			}
			else {
				return false;
			}
		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
	}

	private String[] options = { "Ok", "Cancel" };
	private HashMap<String, PropertyField> fields = null;
	private String selected = "";
	private GCMFile gcm = null;
	private PropertyList conditionList = null;
}
