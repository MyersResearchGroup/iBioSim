package biomodel.gui.gcm;


import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodel.gui.schematic.ModelEditor;
import biomodel.gui.util.PropertyField;
import biomodel.gui.util.PropertyList;
import biomodel.parser.CompatibilityFixer;
import biomodel.util.Utility;

import main.Gui;


public class ParameterPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	public ParameterPanel(String totalSelected, PropertyList parameterList,
			ModelEditor gcmEditor) {
		super(new GridLayout(1, 2));
		this.totalSelected = totalSelected;
		this.parameterList = parameterList;
		this.gcmEditor = gcmEditor;
		changedParam = "";

		fields = new HashMap<String, PropertyField>();
		selected = totalSelected.substring(0, totalSelected.indexOf(" ("));
		selected = CompatibilityFixer.getGCMName(selected);

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

	private String changedParam = "";
	private String totalSelected = "";
	private String selected = "";
	private PropertyList parameterList = null;
	private HashMap<String, PropertyField> fields = null;
	private ModelEditor gcmEditor = null;
}
