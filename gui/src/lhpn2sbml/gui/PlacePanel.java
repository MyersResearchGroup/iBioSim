package lhpn2sbml.gui;

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

public class PlacePanel extends JPanel implements ActionListener {

	private String selected = "";

	private PropertyList placeList, controlList;

	private JComboBox initBox;

	private String[] initCond = { "true", "false" };

	private String[] options = { "Ok", "Cancel" };

	private LHPNFile lhpn;

	private HashMap<String, PropertyField> fields = null;

	public PlacePanel(String selected, PropertyList placeList, PropertyList controlList,
			LHPNFile lhpn) {
		super(new GridLayout(2, 1));
		this.selected = selected;
		this.placeList = placeList;
		this.controlList = controlList;
		this.lhpn = lhpn;

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.ATACSIDstring);
		fields.put(GlobalConstants.ID, field);
		add(field);

		// Initial field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel("Initially Marked");
		initBox = new JComboBox(initCond);
		initBox.setSelectedItem(initCond[1]);
		initBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(initBox);
		add(tempPanel);
		
		

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			// Properties prop = lhpn.getVariables().get(selected);
			fields.get(GlobalConstants.ID).setValue(selected);
			if (lhpn.getPlaceInitial(selected)) {
				initBox.setSelectedItem(initCond[0]);
			}
			else {
				initBox.setSelectedItem(initCond[1]);
			}
		}

		boolean display = false;
		while (!display) {
			display = openGui(oldName);
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

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(new JFrame(), this, "Place Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			String[] allVariables = lhpn.getAllIDs();
			if (oldName == null) {
				for (int i=0; i<allVariables.length; i++) {
					if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
						Utility.createErrorMessage("Error", "Place id already exists.");
						return false;
					}
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				for (int i=0; i<allVariables.length; i++) {
					if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
						Utility.createErrorMessage("Error", "Place id already exists.");
						return false;
					}
				}
			}
			String id = fields.get(GlobalConstants.ID).getValue();

			// Check to see if we need to add or edit
			Boolean ic;
			if (initBox.getSelectedItem().toString().equals("true")) {
				ic = true;
			}
			else {
				ic = false;
			}

			if (selected != null && !oldName.equals(id)) {
				lhpn.changeVariableName(oldName, id);
			}
			lhpn.removePlace(oldName);
			lhpn.addPlace(id, ic);
			placeList.removeItem(oldName);
			placeList.addItem(id);
			placeList.setSelectedValue(id, true);
			if (oldName != null) {
				for (String s : controlList.getItems()) {
					String[] array = s.split("\\s");
					for (String t : array) {
						if (t.equals(oldName)) {
							lhpn.removeControlFlow(array[0], array[1]);
							controlList.removeItem(s);
							s = s.replace(oldName, id);
							array = s.split("\\s");
							lhpn.addControlFlow(array[0], array[1]);
							controlList.addItem(s);
						}
					}
				}
			}
		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			// setType(initBox.getSelectedItem().toString());
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
