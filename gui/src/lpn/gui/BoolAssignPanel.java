package lpn.gui;

import lpn.parser.*;
import main.Gui;
import main.Log;

import gcm.gui.*;
import gcm.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class BoolAssignPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String selected = "", oldName = null, transition, id;

	private PropertyList assignmentList, booleanList;

	private String[] boolList;
	
	private String[] options = { "Ok", "Cancel" };

	private LhpnFile lhpn;
	private Log log;

	private JComboBox varBox;

	private HashMap<String, PropertyField> fields = null;

	public BoolAssignPanel(String transition, String selected, PropertyList assignmentList, PropertyList booleanList,
			LhpnFile lhpn, Log log) {
		super(new GridLayout(6, 1));
		this.selected = selected;
		this.transition = transition;
		this.assignmentList = assignmentList;
		this.booleanList = booleanList;
		this.lhpn = lhpn;
		this.log = log;

		fields = new HashMap<String, PropertyField>();
		
		boolList = lhpn.getBooleanVars();
		
		// Variable field
		JPanel tempPanel = new JPanel();
		JLabel varLabel = new JLabel("Variable");
		varBox = new JComboBox(boolList);
		varBox.setSelectedItem(boolList[0]);
		varBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(varLabel);
		tempPanel.add(varBox);
		add(tempPanel);
		
		PropertyField field = new PropertyField("Assigned Value", lhpn.getContAssign(transition, selected), null, null,
				Utility.NAMEstring);
		fields.put("Assignment value", field);
		add(field);
		
		if (selected != null) {
			oldName = selected;
			PropertyField assignField = fields.get("Assignment value");
			String[] tempArray = oldName.split(":=");
			assignField.setValue(tempArray[1]);
			fields.put("Assignment value", assignField);
		}

		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Boolean Assignment Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			id = varBox.getSelectedItem().toString() + ":=" + fields.get("Assignment value").getValue();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			property.put("Variable", varBox.getSelectedItem().toString());
			property.put("Value", fields.get("Assignment value").getValue());

			assignmentList.removeItem(oldName);
			assignmentList.addItem(id);
			assignmentList.setSelectedValue(id, true);
			booleanList.removeItem(oldName);
			booleanList.addItem(id);
		}
		else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
		}
	}
	
	public void save() {
		Properties property = new Properties();
		property.put("Variable", varBox.getSelectedItem().toString());
		property.put("Value", fields.get("Assignment value").getValue());

		if (selected != null && !oldName.equals(id)) {
			lhpn.changeVariableName(oldName, id);
		}
		else {
			log.addText("here " + property.getProperty("Value"));
			lhpn.addBoolAssign(transition, id, property.getProperty("Value"));
		}
	}
}
