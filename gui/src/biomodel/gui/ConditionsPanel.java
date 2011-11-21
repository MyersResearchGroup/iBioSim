package biomodel.gui;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodel.parser.BioModel;
import biomodel.util.Utility;

import lpn.parser.Parser;
import main.Gui;


public class ConditionsPanel extends JPanel {

	private String selected = "";

	private String[] options = { "Ok", "Cancel" };

	private PropertyField field;

	private BioModel gcm = null;
	private PropertyList conditionList = null;
	
	private ModelEditor gcmEditor = null;

	public ConditionsPanel(String selected, PropertyList conditionList, BioModel gcm,
			boolean paramsOnly,ModelEditor gcmEditor) {
		super(new GridLayout(1, 1));
		this.selected = selected;
		this.conditionList = conditionList;
		this.gcm = gcm;
		this.gcmEditor = gcmEditor;

		// Condition field
		field = new PropertyField("Property", "", null, null, "Property", paramsOnly, "default", true);
		add(field);

		String oldProperty = null;
		if (selected != null) {
			oldProperty = selected;
			// Properties prop = lhpn.getVariables().get(selected);
			field.setValue(selected);
		}

		boolean display = false;
		while (!display) {
			display = openGui(oldProperty);
		}
	}

	private boolean checkValues() {
		boolean goodProperty = false;
		String propertyTemp = field.getValue();
		if (propertyTemp != null && !propertyTemp.equals("")) {
			// check the balance of parentheses and square brackets
			Parser p = new Parser(propertyTemp);
			goodProperty = p.parseProperty();
			if (!goodProperty) {
				JOptionPane.showMessageDialog(Gui.frame,
						"Invalid property. See terminal for detailed information.",
						"Error in Property", JOptionPane.ERROR_MESSAGE);
			}
			return goodProperty;

		}
		else {
			goodProperty = true;
			return goodProperty;
		}

	}

	private boolean openGui(String oldProperty) {
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Property Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			gcmEditor.setDirty(true);
			// TODO: REMOVED BECAUSE CONDITIONS REMOVED
			/*
			if (!checkValues()) {
				return false;
			}
			else if (oldProperty == null) {
				if (gcm.getConditions().contains(field.getValue())) {
					Utility.createErrorMessage("Error", "Property already exists.");
					return false;
				}
			}
			else if (!oldProperty.equals(field.getValue())) {
				if (gcm.getConditions().contains(field.getValue())) {
					Utility.createErrorMessage("Error", "Property already exists.");
					return false;
				}
			}
			String property = field.getValue();

			if (selected != null) {
				gcm.removeCondition(oldProperty);
			}
			property = gcm.addCondition(property);
			if (property != null) {
				conditionList.removeItem(oldProperty);
				conditionList.addItem(property);
				conditionList.setSelectedValue(property, true);
			}
			else {
				return false;
			}
			*/
		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
	}
}
