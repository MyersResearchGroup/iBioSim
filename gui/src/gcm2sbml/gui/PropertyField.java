package gcm2sbml.gui;

import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PropertyField extends JPanel implements ActionListener,
		PropertyProvider {

	public PropertyField(String name, String value, String state,
			String defaultValue, String repExp) {
		super(new GridLayout(1, 3));
		if (state == null) {
			setLayout(new GridLayout(1, 2));
		}
		this.defaultValue = defaultValue;
		init(name, value, state);
		setRegExp(repExp);
	}

	public PropertyField(String name, String value, String state,
			String defaultValue) {
		this(name, value, state, defaultValue, null);
	}

	public void setEnabled(boolean state) {
		// super.setEnabled(state);
		isEnabled = state;
		field.setEnabled(state);
		if (box != null) {
			box.setEnabled(state);
		}
		name.setEnabled(state);
		if (state) {
			if (box.getSelectedItem().equals(states[0])) {
				setDefault();
			} else {
				setCustom();
			}
		}
	}

	private void init(String nameString, String valueString, String stateString) {
		name = new JLabel(nameString);
		name.setName(nameString);
		name.setText(nameString);
		this.add(name);
		if (!(valueString == null) && !(stateString == null)) {
//			name.setText(CompatibilityFixer.getGuiName(nameString) + ", ID: " + CompatibilityFixer.getSBMLName(nameString) + "   ");
//			idLabel = new JLabel("ID");
//			idLabel.setEnabled(false);
//			this.add(idLabel);
//			idField = new JTextField(CompatibilityFixer.getSBMLName(nameString));
//			idField.setEditable(false);
//			this.add(idField);
		}
		field = new JTextField(20);
		field.setText(valueString);
		if (stateString != null) {
			box = new JComboBox(new DefaultComboBoxModel(states));
			box.addActionListener(this);
			this.add(box);
			if (stateString.equals(states[0])) {
				setDefault();
			} else {
				setCustom();
			}
		}
		field.addActionListener(this);
		this.add(field);
	}

	public void actionPerformed(ActionEvent e) {
		// TODO: Need to check source
		if (e.getActionCommand().equals("comboBoxChanged")) {
			if (box.getSelectedItem().equals(states[0])) {
				setDefault();
			} else {
				setCustom();
			}
		} else {
			if (Utility.isValid(e.getActionCommand(), regExp)) {
				// System.out.println();
			} else {
				JOptionPane.showMessageDialog(null, "Illegal value entered.",
						"Illegal value entered", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void setDefault() {
		field.setEnabled(false);
		name.setEnabled(false);
		field.setText(defaultValue);
		box.setSelectedItem(states[0]);
	}

	public void setCustom() {
		if (isEnabled && box != null) {
			field.setEnabled(true);
			name.setEnabled(true);
			box.setSelectedItem(states[1]);
		}
	}

	public String getState() {
		if (box == null) {
			return null;
		}
		return box.getSelectedItem().toString();
	}

	public String getKey() {
		return name.getName();
	}

	public String getValue() {
		return field.getText();
	}

	public void setKey(String key) {
		name.setName(key);
		name.setText(CompatibilityFixer.getGuiName(key));
	}

	public void setValue(String value) {
		field.setText(value);
	}

	public boolean isValid() {
		if (getValue() == null || getValue().equals("")) {
			return false;
		}
		return Utility.isValid(getValue(), regExp);
	}

	public void setRegExp(String repExp) {
		this.regExp = repExp;
	}

	private boolean isEnabled = true;

	private String regExp = null;

	private JLabel name = null;

	private JLabel idLabel = null;

	private JComboBox box = null;

	private JTextField field = null;

	private JTextField idField = null;

	// private JLabel idL

	public static final String[] states = new String[] { "default", "custom" };

	private String defaultValue = null;
}
