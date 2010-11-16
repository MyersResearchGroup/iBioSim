package gcm2sbml.gui;

import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodelsim.BioSim;

public class ParameterPanel extends JPanel {
	public ParameterPanel(String totalSelected, PropertyList parameterList,
			GCMFile gcm, boolean paramsOnly, GCMFile refGCM) {
		super(new GridLayout(1, 2));
		this.totalSelected = totalSelected;
		this.parameterList = parameterList;
		this.gcm = gcm;
		this.refGCM = refGCM;
		this.paramsOnly = paramsOnly;
		changedParam = "";

		fields = new HashMap<String, PropertyField>();
		selected = totalSelected.substring(0, totalSelected.indexOf(" ("));
		selected = CompatibilityFixer.getGCMName(selected);

		// Initial field
		PropertyField field;
		String origString = "default";
		if (paramsOnly) {
			if (refGCM.getGlobalParameters().containsKey(selected)) {
				origString = "custom";
				field = new PropertyField(selected, gcm.getParameter(selected), origString, refGCM
						.getParameter(selected), Utility.SWEEPstring, paramsOnly, origString);
			}
			else {
				field = new PropertyField(selected, gcm.getParameter(selected), origString, refGCM
						.getParameter(selected), Utility.SWEEPstring, paramsOnly, origString);
			}
		}
		else {
			field = new PropertyField(selected, gcm.getParameter(selected), origString, gcm
					.getDefaultParameters().get(selected), Utility.NUMstring, paramsOnly,
					origString);
		}
		fields.put(selected, field);
		if (gcm.getGlobalParameters().containsKey(selected)) {
			field.setValue(gcm.getGlobalParameters().get(selected));
			field.setCustom();
		} else {			
			field.setDefault();
		}
		add(field);

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
		int value = JOptionPane.showOptionDialog(BioSim.frame, this,
				"Parameter Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			String newItem = CompatibilityFixer.getGuiName(selected);
			if (fields.get(selected).getState().equals(fields.get(selected).getStates()[1])) {
				gcm.setParameter(selected, fields.get(selected).getValue());
				newItem = newItem + " (" + CompatibilityFixer.getSBMLName(selected) + "), ";
				if (paramsOnly && fields.get(selected).getValue().trim().startsWith("(")) {
					newItem = newItem + "Sweep, " + fields.get(selected).getValue();
					changedParam += CompatibilityFixer.getSBMLName(selected) + " "
							+ fields.get(selected).getValue();
				}
				else {
					if (paramsOnly) {
						newItem = newItem + "Modified, " + fields.get(selected).getValue();
					}
					else {
						newItem = newItem + "Custom, " + fields.get(selected).getValue();
					}
					changedParam += CompatibilityFixer.getSBMLName(selected) + " "
							+ fields.get(selected).getValue();
				}
			}
			else if (fields.get(selected).getState().equals(fields.get(selected).getStates()[0])) {
				gcm.removeParameter(selected);
				if (paramsOnly) {
					if (refGCM.getGlobalParameters().containsKey(selected)) {
						newItem = newItem + " (" + CompatibilityFixer.getSBMLName(selected)
								+ "), Custom, " + refGCM.getParameter(selected);
					}
					else {
						newItem = newItem + " (" + CompatibilityFixer.getSBMLName(selected)
								+ "), Default, " + refGCM.getParameter(selected);
					}
					changedParam += CompatibilityFixer.getSBMLName(selected);
				}
				else {
					newItem = newItem + " (" + CompatibilityFixer.getSBMLName(selected)
							+ "), Default, " + gcm.getParameter(selected);
				}
			}
			parameterList.removeItem(totalSelected);
			parameterList.addItem(newItem);
			parameterList.setSelectedValue(newItem, true);
		} else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}
	
	public String updates() {
			return changedParam;
	}

	private String[] options = { "Ok", "Cancel" };

	private boolean paramsOnly;
	private String changedParam = "";
	private String totalSelected = "";
	private String selected = "";
	private GCMFile gcm = null;
	private GCMFile refGCM = null;
	private PropertyList parameterList = null;
	private HashMap<String, PropertyField> fields = null;
}
