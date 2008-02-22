package gcm2sbml.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PropertyField extends JPanel implements ActionListener{

	public PropertyField(String name, String value, String state,
			Properties defaultProperties) {
		super(new GridLayout(1, 3));
		this.defaultProperties = defaultProperties;
		init(name, value, state);
	}

	public void setEnabled(boolean state) {
		// super.setEnabled(state);
		field.setEnabled(state);
		box.setEnabled(state);
		name.setEnabled(state);
	}	

	private void init(String nameString, String valueString, String stateString) {
		name = new JLabel(nameString);
		field = new JTextField(20);
		field.setText(valueString);
		box = new JComboBox(new DefaultComboBoxModel(states));
		box.addActionListener(this);
		this.add(name);
		this.add(box);
		this.add(field);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (box.getSelectedItem().equals(states[0])) {
			field.setEnabled(false);
			name.setEnabled(false);
			field.setText(defaultProperties.getProperty("name").replace("\"",""));
		} else {
			field.setEnabled(true);
			name.setEnabled(true);			
		}
	}

	private JLabel name = null;
	private JComboBox box = null;
	private JTextField field = null;
	public static final String[] states = new String[] { "default", "custom" };

	private Properties defaultProperties = null;
}
