package biomodel.gui.gcm;


import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodel.gui.schematic.ModelEditor;
import biomodel.gui.util.PropertyField;
import biomodel.gui.util.PropertyList;
import biomodel.parser.BioModel;
import biomodel.parser.CompatibilityFixer;
import biomodel.util.Utility;

import main.Gui;


public class ParameterPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	public ParameterPanel(String totalSelected, PropertyList parameterList,
			BioModel gcm, boolean paramsOnly, BioModel refGCM, ModelEditor gcmEditor) {
		super(new GridLayout(1, 2));
		this.totalSelected = totalSelected;
		this.parameterList = parameterList;
		//this.gcm = gcm;
		//this.refGCM = refGCM;
		//this.paramsOnly = paramsOnly;
		this.gcmEditor = gcmEditor;
		changedParam = "";

		fields = new HashMap<String, PropertyField>();
		selected = totalSelected.substring(0, totalSelected.indexOf(" ("));
		selected = CompatibilityFixer.getGCMName(selected);

		// Initial field
		// TODO: REMOVED NOT VALID ANYMORE
		/*
		PropertyField field;
		String origString = "default";
				if (paramsOnly) {
			if (refGCM.getGlobalParameters().containsKey(selected)) {
				origString = "custom";
				field = new PropertyField(selected, gcm.getParameter(selected), origString, refGCM
						.getParameter(selected), Utility.SWEEPstring, paramsOnly, origString, false);
			}
			else {
				field = new PropertyField(selected, gcm.getParameter(selected), origString, refGCM
						.getParameter(selected), Utility.SWEEPstring, paramsOnly, origString, false);
			}
		}
		else {
			field = new PropertyField(selected, gcm.getParameter(selected), origString, gcm
					.getDefaultParameters().get(selected), Utility.NUMstring, paramsOnly,
					origString, false);
		}
		fields.put(selected, field);
		if (gcm.getGlobalParameters().containsKey(selected)) {
			field.setValue(gcm.getGlobalParameters().get(selected));
			field.setCustom();
		} else {			
			field.setDefault();
		}
		add(field);
		*/

		boolean display = false;
		while (!display) {
			display = openGui(selected);
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

	private boolean openGui(String selected) {
		int value = JOptionPane.showOptionDialog(Gui.frame, this,
				"Parameter Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			String newItem = CompatibilityFixer.getGuiName(selected);
			// TODO: REMOVED NOT VALID ANYMORE
			/*
			if (fields.get(selected).getState().equals(fields.get(selected).getStates()[1])) {
				gcm.setParameter(selected, fields.get(selected).getValue());
				newItem = newItem + " (" + selected + "), ";
				if (paramsOnly && fields.get(selected).getValue().trim().startsWith("(")) {
					newItem = newItem + "Sweep, " + fields.get(selected).getValue();
					changedParam += selected + " "
							+ fields.get(selected).getValue();
				}
				else {
					if (paramsOnly) {
						newItem = newItem + "Modified, " + fields.get(selected).getValue();
					}
					else {
						newItem = newItem + "Custom, " + fields.get(selected).getValue();
					}
					changedParam += selected + " "
							+ fields.get(selected).getValue();
				}
			}
			else if (fields.get(selected).getState().equals(fields.get(selected).getStates()[0])) {
				gcm.removeParameter(selected);
				if (paramsOnly) {
					if (refGCM.getGlobalParameters().containsKey(selected)) {
						newItem = newItem + " (" + selected
								+ "), Custom, " + refGCM.getParameter(selected);
					}
					else {
						newItem = newItem + " (" + selected
								+ "), Default, " + refGCM.getParameter(selected);
					}
					changedParam += selected;
				}
				else {
					newItem = newItem + " (" + selected
							+ "), Default, " + gcm.getParameter(selected);
				}
			}
			*/
			
			parameterList.removeItem(totalSelected);
			parameterList.addItem(newItem);
			parameterList.setSelectedValue(newItem, true);
			gcmEditor.setDirty(true);
		} else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}
	
	public String updates() {
			return changedParam;
	}

	private String[] options = { "Ok", "Cancel" };

	//private boolean paramsOnly;
	private String changedParam = "";
	private String totalSelected = "";
	private String selected = "";
	//private BioModel gcm = null;
	//private BioModel refGCM = null;
	private PropertyList parameterList = null;
	private HashMap<String, PropertyField> fields = null;
	private ModelEditor gcmEditor = null;
}
