/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.frontend.lpn.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties; 

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.utah.ece.async.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.dataModels.lpn.parser.*;
import edu.utah.ece.async.dataModels.util.GlobalConstants;
import edu.utah.ece.async.frontend.biomodel.gui.util.PropertyList;
import edu.utah.ece.async.frontend.main.Gui;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VariablesPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name = "", type = "", selList;

	private PropertyList variablesList;

	private String[] options = { "Ok", "Cancel" };

	private Boolean continuous, integer;

	private LPN lhpn;

	private JPanel initPanel = null;

	private PropertyField initValue = null, initRate;

	private JComboBox modeBox, initBox;

	private static final String[] types = new String[] { "boolean",
			"continuous", "discrete" };

	private static final String[] modes = new String[] { "input", "output",
			"internal" };

	private static final String[] booleans = new String[] { "true", "false",
			"unknown" };

	private HashMap<String, PropertyField> fields = null;

	public VariablesPanel(String selected, PropertyList variablesList,
			Boolean boolCont, Boolean integer, LPN lhpn) {
		super(new GridLayout(5, 1));
		if (selected != null) {
			String[] array = selected.split(" ");
			this.name = array[0];
		} else {
			this.name = selected;
		}
		this.selList = selected;
		this.variablesList = variablesList;
		this.lhpn = lhpn;
		this.continuous = boolCont;
		this.integer = integer;

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null,
				null, Utility.ATACSIDstring);
		fields.put(GlobalConstants.ID, field);
		add(field);

		// Type label
		JPanel tempPanel = new JPanel();
		if (continuous) {
			type = types[1];
		} else if (integer) {
			type = types[2];
		} else {
			type = types[0];
		}
		JLabel tempLabel = new JLabel("Type");
		JLabel typeLabel = new JLabel(type);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeLabel);
		add(tempPanel);

		// Initial field
		if (continuous || integer) {
			if (name != null) {
				String initVal = lhpn.getInitialVal(name);
				initValue = new PropertyField("Initial Value", initVal, null,
						null, Utility.NAMEstring);
			} else {
				initValue = new PropertyField("Initial Value", "0", null, null,
						Utility.NAMEstring);
			}
			fields.put("Initial value", initValue);
			add(initValue);
		} else {
			initPanel = new JPanel();
			JLabel initLabel = new JLabel("Initial Value");
			initBox = new JComboBox(booleans);
			initBox.setSelectedItem(booleans[1]);
			initPanel.setLayout(new GridLayout(1, 2));
			initPanel.add(initLabel);
			initPanel.add(initBox);
			add(initPanel);
		}

		// Mode field
		tempPanel = new JPanel();
		JLabel modeLabel = new JLabel("Mode");
		modeBox = new JComboBox(modes);
		modeBox.setSelectedItem(modes[0]);
		modeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(modeLabel);
		tempPanel.add(modeBox);
		add(tempPanel);

		// Initial rate field
		if (continuous) {
			if (name != null) {
				String rate = lhpn.getInitialRate(name);
				initRate = new PropertyField("Initial Rate", rate, null, null,
						Utility.NAMEstring);
			} else {
				initRate = new PropertyField("Initial Rate", "0", null, null,
						Utility.NAMEstring);
			}
			fields.put("Initial rate", initRate);
			add(initRate);
		}

		String oldName = null;
		if (name != null) {
			oldName = name;
			fields.get(GlobalConstants.ID).setValue(name);
			if (lhpn.isContinuous(name) || lhpn.isInteger(name)) {
				String initVal = lhpn.getInitialVal(name);
				fields.get("Initial value").setValue(initVal);
			} else {
				HashMap<String, String> inits;
				if (lhpn.isInput(name)) {
					inits = lhpn.getBoolInputs();
				} else {
					inits = lhpn.getBoolOutputs();
				}
				initBox.setSelectedItem(inits.get(name));
			}
			if (lhpn.isInput(name)) {
				modeBox.setSelectedItem(modes[0]);
			} else if (lhpn.isOutput(name)) {
				modeBox.setSelectedItem(modes[1]);
			} else {
				modeBox.setSelectedItem(modes[2]);
			}
			if (lhpn.isContinuous(name)) {
				String initRate = lhpn.getInitialRate(name);
				fields.get("Initial rate").setValue(initRate);
				fields.get(GlobalConstants.ID).setValue(name);
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
		int value = JOptionPane.showOptionDialog(Gui.frame, this,
				"Variable Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				return false;
			}
			String[] allVariables = lhpn.getAllIDs();
			if (oldName == null) {
				for (int i = 0; i < allVariables.length; i++) {
					if (allVariables[i] != null) {
						if (allVariables[i].equals(fields.get(
								GlobalConstants.ID).getValue())) {
							 JOptionPane.showMessageDialog(Gui.frame, "Variable id already exists.", "Error", JOptionPane.ERROR_MESSAGE); 
							return false;
						}
					}
				}
			} else if (!oldName.equals(fields.get(GlobalConstants.ID)
					.getValue())) {
				for (int i = 0; i < allVariables.length; i++) {
					if (allVariables[i].equals(fields.get(GlobalConstants.ID)
							.getValue())) {
					  JOptionPane.showMessageDialog(Gui.frame, 
              "Variable id already exists.", "Error", JOptionPane.ERROR_MESSAGE); 
						return false;
					}
				}
			}
			if (fields.containsKey("Initial value")) {
				if (!(fields.get("Initial value").getValue().matches("-?\\d+") || fields
						.get("Initial value").getValue().matches("\\[-?\\d+,-?\\d+\\]"))) {
					 JOptionPane.showMessageDialog(Gui.frame, "Initial value must be an integer or a range of integers.", "Error", JOptionPane.ERROR_MESSAGE); 
					return false;
				}
			}
			if (fields.containsKey("Initial rate")) {
				if (!(fields.get("Initial rate").getValue().matches("-?\\d+") || fields
						.get("Initial rate").getValue().matches("\\[-?\\d+,-?\\d+\\]"))) {
				  JOptionPane.showMessageDialog(Gui.frame, "Initial rate must be an integer or a range of integers.", "Error", JOptionPane.ERROR_MESSAGE); 
          
					return false;
				}
			}
			String id = fields.get(GlobalConstants.ID).getValue();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				if (f.getState() == null
						|| f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			String tempVal = "";
			if (property.containsKey("Initial Value")) {
				tempVal = property.getProperty("Initial Value");
				property.setProperty("value", tempVal);
				property.remove("Initial Value");
			}
			if (property.containsKey("Initial Rate")) {
				tempVal = property.getProperty("Initial Rate");
				property.setProperty("rate", tempVal);
				property.remove("Initial Rate");
			}

			if (name != null && oldName != null && !oldName.equals(id)) {
				lhpn.changeVariableName(oldName, id);
			}
			if (id.equals(oldName)) {
				lhpn.removeVar(id);
			}
			if (lhpn.isContinuous(id) || continuous) {
				tempVal = fields.get("Initial value").getValue();
				lhpn.addContinuous(id, property);
				lhpn.getVariable(id).setPort(
						modeBox.getSelectedItem().toString());
			} else if (lhpn.isInteger(id) || integer) {
				tempVal = fields.get("Initial value").getValue();
				lhpn.addInteger(id, tempVal);
				lhpn.getVariable(id).setPort(
						modeBox.getSelectedItem().toString());
			} else {
				lhpn.addBoolean(id, initBox.getSelectedItem().toString());
				lhpn.getVariable(id).setPort(
						modeBox.getSelectedItem().toString());
			}
			variablesList.removeItem(selList);
			String list;
			if (continuous || integer) {
				list = id + " - " + type + " - " + modeBox.getSelectedItem().toString() + " - " + tempVal;
			} else {
				list = id + " - " + type + " - " + modeBox.getSelectedItem().toString() + " - "
						+ initBox.getSelectedItem().toString();
			}
			if (continuous) {
				tempVal = fields.get("Initial rate").getValue();
				list = list + " - " + tempVal;
			}
			variablesList.removeItem(list);
			variablesList.addItem(list);
			variablesList.setSelectedValue(id, true);

		} else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
		}
	}

}
