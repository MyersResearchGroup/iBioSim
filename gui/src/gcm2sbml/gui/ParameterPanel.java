package gcm2sbml.gui;

import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ParameterPanel extends JPanel {
	public ParameterPanel(String selected, PropertyList parameterList,
			GCMFile gcm) {
		super(new GridLayout(1, 2));
		this.selected = selected;
		this.parameterList = parameterList;
		this.gcm = gcm;

		fields = new HashMap<String, PropertyField>();

		// Initial field
		PropertyField field = new PropertyField(selected, gcm
				.getParameter(selected), PropertyField.states[0], gcm
				.getDefaultParameters().get(selected), Utility.NUMstring);
		fields.put(selected, field);
		if (gcm.getGlobalParameters().containsKey(selected)) {
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
			if (!f.isValid()) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String selected) {
		int value = JOptionPane.showOptionDialog(new JFrame(), this,
				"Parameter Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (fields.get(selected).getState().equals(PropertyField.states[1])) {
				gcm.setParameter(selected, fields.get(selected).getValue());
			}
		} else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	private String[] options = { "Okay", "Cancel" };

	private String selected = "";
	private GCMFile gcm = null;
	private PropertyList parameterList = null;
	private HashMap<String, PropertyField> fields = null;
}
