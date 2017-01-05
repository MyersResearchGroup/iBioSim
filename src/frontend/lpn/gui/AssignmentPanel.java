package frontend.lpn.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import backend.biomodel.util.Utility;
import backend.lpn.parser.*;
import frontend.biomodel.gui.util.PropertyList;
import frontend.main.Gui;

import javax.swing.JCheckBox;


public class AssignmentPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String selected = "", id, oldName = null;

	private PropertyList assignmentList, continuousList, rateList, integerList, booleanList;

	private ArrayList<String> boolList, contList, intList;

	private String[] varList;

	private String[] options = { "Ok", "Cancel" };

	private LPN lhpn;

	private JComboBox varBox;

	private JCheckBox rateBox;


	private HashMap<String, PropertyField> fields = null;


	public AssignmentPanel(String selected, PropertyList assignmentList, PropertyList continuousList,
			PropertyList rateList, PropertyList booleanList, PropertyList integerList,
			LPN lhpn) {
		super(new GridLayout(3, 1));
		this.selected = selected;
		this.assignmentList = assignmentList;
		this.continuousList = continuousList;
		this.rateList = rateList;
		this.booleanList = booleanList;
		this.integerList = integerList;

		this.lhpn = lhpn;

		fields = new HashMap<String, PropertyField>();

		contList = new ArrayList<String>();
		boolList = new ArrayList<String>();
		intList = new ArrayList<String>();
		String[] tempArray = lhpn.getContVars();
		String[] tempList = assignmentList.getItems();
		for (int i = 0; i < tempArray.length; i++) {
			boolean contains = false;
			if (selected != null) {
				if (!(selected.startsWith(tempArray[i] + ":=") || selected.startsWith(tempArray[i]
						+ "':="))) {
					for (int j = 0; j < tempList.length; j++) {
						if (tempList[j].startsWith(tempArray[i] + ":=")
								|| tempList[j].startsWith(tempArray[i] + "':=")) {
							contains = true;
							break;
						}
					}
				}
			}
			else {
				for (int j = 0; j < tempList.length; j++) {
					if (tempList[j].startsWith(tempArray[i] + ":=")
							|| tempList[j].startsWith(tempArray[i] + "':=")) {
						contains = true;
						break;
					}
				}
			}
			if (!contains && tempArray[i] != null) {
				contList.add(tempArray[i]);
			}
		}
		tempArray = lhpn.getBooleanVars();
		for (int i = 0; i < tempArray.length; i++) {
			boolean contains = false;
			if (selected != null) {
				if (!selected.startsWith(tempArray[i] + ":=")) {
					for (int j = 0; j < tempList.length; j++) {
						if (tempList[j].startsWith(tempArray[i] + ":=")) {
							contains = true;
							break;
						}
					}
				}
			}
			else {
				for (int j = 0; j < tempList.length; j++) {
					if (tempList[j].startsWith(tempArray[i] + ":=")) {
						contains = true;
						break;
					}
				}
			}
			if (!contains && tempArray[i] != null) {
				boolList.add(tempArray[i]);
			}
		}
		tempArray = lhpn.getIntVars();
		for (int i = 0; i < tempArray.length; i++) {
			boolean contains = false;
			if (selected != null) {
				if (!selected.startsWith(tempArray[i] + ":=")) {
					for (int j = 0; j < tempList.length; j++) {
						if (tempList[j].startsWith(tempArray[i] + ":=")) {
							contains = true;
							break;
						}
					}
				}
			}
			else {
				for (int j = 0; j < tempList.length; j++) {
					if (tempList[j].startsWith(tempArray[i] + ":=")) {
						contains = true;
						break;
					}
				}
			}
			if (!contains && tempArray[i] != null) {
				intList.add(tempArray[i]);
			}
		}
		varList = new String[contList.size() + boolList.size() + intList.size()];
		for (int i = 0; i < boolList.size(); i++) {
			varList[i] = boolList.get(i);
		}
		for (int i = 0; i < contList.size(); i++) {
			varList[i + boolList.size()] = contList.get(i);
		}
		for (int i = 0; i < intList.size(); i++) {
			varList[i + boolList.size() + contList.size()] = intList.get(i);
		}

		// Variable field
		JPanel tempPanel = new JPanel();
		JLabel varLabel = new JLabel("Variable");
		varBox = new JComboBox(varList);
		varBox.setSelectedItem(varList[0]);
		varBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(varLabel);
		tempPanel.add(varBox);
		add(tempPanel);

		// Assignment lower bound
		PropertyField field = new PropertyField("Assignment", "", null, null,
				Utility.VALstring);
		fields.put("Assignment", field);
		add(field);

		// Rate Assignment Box
		rateBox = new JCheckBox("Rate Assignment");
		add(rateBox);
		if (!lhpn.isContinuous(varBox.getSelectedItem().toString())) {
			rateBox.setEnabled(false);
		}

		if (selected != null) {
			oldName = selected;
			tempArray = new String[2];
			if (oldName.matches("[\\S^']+':=[\\S]+")) {
				rateBox.setSelected(true);
				tempArray = oldName.split("':=");
			}
			else {
				tempArray = oldName.split(":=");
			}
			for (int i = 0; i < varList.length; i++) {
				if (varList[i].equals(tempArray[0])) {
					varBox.setSelectedItem(varList[i]);
					break;
				}
			}
			if (lhpn.isContinuous(varBox.getSelectedItem().toString())) {
				rateBox.setEnabled(true);
			}
			else {
				rateBox.setEnabled(false);
			}
			fields.get("Assignment").setValue(tempArray[1]);
		}
		/*
		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
		*/
	}
	
	public void displayTransitionGui() {
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
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Variable Assignment Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				HashMap<String, String> prop;
				if (lhpn.getTransition(selected) == null) {
					prop = null;
				}
				else {
					prop = lhpn.getTransition(selected).getContAssignments();
				}
				if (prop != null) {
					if (prop.containsKey(varBox.getSelectedItem())) {
						Utility.createErrorMessage("Error", "Assignment id already exists.");
						return false;
					}
				}
			}
			if (!save()) {
				return false;
			}
			String assign = "";
			assign = fields.get("Assignment").getValue();
			if (rateBox.isSelected()) {
				id = varBox.getSelectedItem().toString() + "':=" + assign;
			}
			else {
				id = varBox.getSelectedItem().toString() + ":=" + assign;
			}

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			property.put("Variable", varBox.getSelectedItem().toString());

			assignmentList.removeItem(oldName);
			assignmentList.addItem(id);
			assignmentList.setSelectedValue(id, true);
			String var = varBox.getSelectedItem().toString();
			if (rateBox.isSelected()) {
				if (lhpn.isContinuous(var)) {
					rateList.removeItem(oldName);
					rateList.addItem(id);
				}
				else {
					Utility.createErrorMessage("Error",
							"Rate assignments must be for continuous variables.");
					return false;
				}
			}
			else if (lhpn.isBoolean(var)) {
				booleanList.removeItem(oldName);
				booleanList.addItem(id);
			}
			else if (lhpn.isContinuous(var)) {
				continuousList.removeItem(oldName);
				continuousList.addItem(id);
			}
			else if (lhpn.isInteger(var)) {
				integerList.removeItem(oldName);
				integerList.addItem(id);
			}
		}
		else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			if (lhpn.isContinuous(varBox.getSelectedItem().toString())) {
				rateBox.setEnabled(true);
			}
			else {
				rateBox.setEnabled(false);
			}
		}
	}

	public boolean save() {
		String variable = varBox.getSelectedItem().toString();
		String value = "";
		ExprTree[] expr = new ExprTree[2];
		expr[0] = new ExprTree(lhpn);
		expr[1] = new ExprTree(lhpn);
			value = fields.get("Assignment").getValue();
			expr[0].token = expr[0].intexpr_gettok(value);
			if (!value.equals("")) {
				try {
					expr[0].intexpr_L(value);
				} catch (IllegalArgumentException e) {
					JOptionPane.showMessageDialog(Gui.frame, String.format("Error parsing %s\n",value)+e.getMessage(),
							"Parse Error in Property", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		Properties property = new Properties();
		for (PropertyField f : fields.values()) {
			if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
				property.put(f.getKey(), f.getValue());
			}
		}
		property.put("Variable", variable);
		return true;
	}

}
