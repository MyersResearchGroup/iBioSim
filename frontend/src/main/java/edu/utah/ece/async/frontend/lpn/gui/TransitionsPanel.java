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

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;
//import java.lang.Runnable;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.utah.ece.async.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.dataModels.lpn.parser.*;
import edu.utah.ece.async.dataModels.util.GlobalConstants;
import edu.utah.ece.async.frontend.biomodel.gui.util.AbstractRunnableNamedButton;
import edu.utah.ece.async.frontend.biomodel.gui.util.PropertyList;
import edu.utah.ece.async.frontend.biomodel.gui.util.Runnable;
import edu.utah.ece.async.frontend.main.Gui;

import javax.swing.JCheckBox;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

public class TransitionsPanel extends JPanel implements ActionListener, MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String selected = "";

	private PropertyList transitionsList, controlList, boolAssignments, varAssignments,
			intAssignments, rateAssignments, assignments;

	private JPanel fieldPanel;

	private String[] options = { "Ok", "Cancel" };

	private String[] allVariables;

	private JCheckBox fail, persistent;

	private LPN lhpn;

	private HashMap<String, PropertyField> fields = null;
	
	private boolean flag = false;

	public TransitionsPanel(String selected, PropertyList transitionsList,
			PropertyList controlList, LPN lhpn) {
		super(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		this.selected = selected;
		this.transitionsList = transitionsList;
		this.controlList = controlList;
		this.lhpn = lhpn;

		fields = new HashMap<String, PropertyField>();
		fieldPanel = new JPanel(new GridLayout(4, 2));

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.ATACSIDstring);
		fields.put(GlobalConstants.ID, field);
		fieldPanel.add(field);

		// Enabling Field
		field = new PropertyField("Enabling Condition", "", null, null, Utility.NAMEstring);
		fields.put("Enabling Condition", field);
		fieldPanel.add(field);

		// Delay field
		field = new PropertyField("Delay Assignment", "", null, null, Utility.NAMEstring);
		fields.put("delay", field);
		fieldPanel.add(field);

		// Priority field
		field = new PropertyField("Priority Expression", "", null, null, Utility.NAMEstring);
		fields.put("priority", field);
		fieldPanel.add(field);

		constraints.gridx = 0;
		constraints.gridy = 0;
		add(fieldPanel, constraints);

		// Fail Transition check box
		JPanel failPanel = new JPanel(new GridLayout(1, 2));
		JLabel failLabel = new JLabel("Fail Transition");
		JLabel blankLabel2 = new JLabel("                                          ");
		fail = new JCheckBox();
		failPanel.add(failLabel);
		failPanel.add(fail);
		failPanel.add(blankLabel2);
		constraints.gridx = 0;
		constraints.gridy = 1;
		add(failPanel, constraints);
		
		// Persistent Transition check box
		JPanel persistentPanel = new JPanel(new GridLayout(1, 2));
		JLabel persistentLabel = new JLabel("Persistent Transition");
		JLabel blankLabel3 = new JLabel("                                          ");
		persistent = new JCheckBox();
		persistentPanel.add(persistentLabel);
		persistentPanel.add(persistent);
		persistentPanel.add(blankLabel3);
		constraints.gridx = 0;
		constraints.gridy = 2;
		add(persistentPanel, constraints);

		// Assignment panel
		assignments = new PropertyList("Assignment List");
		boolAssignments = new PropertyList("Boolean Assignment List");
		varAssignments = new PropertyList("Continuous Assignment List");
		intAssignments = new PropertyList("Integer Assignment List");
		rateAssignments = new PropertyList("Rate Assignment List");
		EditButton addAssign = new EditButton("Add Assignment", assignments);
		RemoveButton removeAssign = new RemoveButton("Remove Assignment", assignments);
		EditButton editAssign = new EditButton("Edit Assignment", assignments);
		assignments.addMouseListener(this);
		if (selected != null) {
			if (lhpn.getBooleanVars(selected) != null) {
				for (String s : lhpn.getBooleanVars(selected)) {
					if (s != null && lhpn.getBoolAssign(selected, s) != null) {
						boolAssignments.addItem(s + ":="
								+ lhpn.getBoolAssign(selected, s).toString());
						assignments.addItem(s + ":=" + lhpn.getBoolAssign(selected, s).toString());
					}
				}
			}
			if (lhpn.getContVars(selected) != null) {
				for (String s : lhpn.getContVars(selected)) {
					if (s != null) {
						if (!s.equals(null)) {
							varAssignments.addItem(s + ":=" + lhpn.getContAssign(selected, s));
							assignments.addItem(s + ":=" + lhpn.getContAssign(selected, s));
						}
					}
				}
			}
			if (lhpn.getIntVars(selected) != null) {
				for (String s : lhpn.getIntVars(selected)) {
					if (!s.equals(null)) {
						intAssignments.addItem(s + ":=" + lhpn.getIntAssign(selected, s));
						assignments.addItem(s + ":=" + lhpn.getIntAssign(selected, s));
					}
				}
			}
			if (lhpn.getRateVars(selected) != null) {
				for (String s : lhpn.getRateVars(selected)) {
					if (s != null) {
						if (!s.equals(null) && !lhpn.getRateAssign(selected, s).equals("true")
								&& !lhpn.getRateAssign(selected, s).equals("false")) {
							rateAssignments.addItem(s + "':=" + lhpn.getRateAssign(selected, s));
							assignments.addItem(s + "':=" + lhpn.getRateAssign(selected, s));
						}
					}
				}
			}
		}

		JPanel assignPanel = Utility.createPanel(this, "Assignments", assignments, addAssign,
				removeAssign, editAssign);
		constraints.gridx = 0;
		constraints.gridy = 3;
		add(assignPanel, constraints);

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			fields.get(GlobalConstants.ID).setValue(selected);
			if (lhpn.getTransition(oldName).isFail()) {
				fail.setSelected(true);
			}
			if (lhpn.getTransition(oldName).isPersistent()) {
				persistent.setSelected(true);
			}
			String delay = lhpn.getTransition(selected).getDelay();
			if (delay != null) {
				fields.get("delay").setValue(delay);
			}
			String enabling = lhpn.getTransition(selected).getEnabling();
			if (enabling != null) {
				fields.get("Enabling Condition").setValue(enabling);
			}
			String priority = lhpn.getTransition(selected).getPriority();
			if (priority != null) {
				fields.get("priority").setValue(priority);
			}
		}

		boolean display = false;
		allVariables = lhpn.getAllIDs();
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
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Transition Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				 JOptionPane.showMessageDialog(Gui.frame, "Illegal values entered.", "Error", JOptionPane.ERROR_MESSAGE); 
				return false;
			}
			if (oldName == null && allVariables != null) {
				for (int i = 0; i < allVariables.length; i++) {
					if (allVariables[i] != null) {
						if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
							 JOptionPane.showMessageDialog(Gui.frame, "Transition id already exists.", "Error", JOptionPane.ERROR_MESSAGE); 
							return false;
						}
					}
				}
			}
			else if (oldName != null && !oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				for (int i = 0; i < allVariables.length; i++) {
					if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
					  JOptionPane.showMessageDialog(Gui.frame, "Transition id already exists.", "Error", JOptionPane.ERROR_MESSAGE); 
						return false;
					}
				}
			}
			else if (fields.get(GlobalConstants.ID).getValue() == null) {
				 JOptionPane.showMessageDialog(Gui.frame, "Enter transition ID.", "Error", JOptionPane.ERROR_MESSAGE); 
				return false;
			}
			String id = fields.get(GlobalConstants.ID).getValue();
			if (!save(id)) {
			  JOptionPane.showMessageDialog(Gui.frame, "Illegal values entered.", "Error", JOptionPane.ERROR_MESSAGE); 
				return false;
			}

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}

			if (selected != null && oldName != null && !oldName.equals(id)) {
				lhpn.changeTransitionName(oldName, id);
			}
			transitionsList.removeItem(oldName);
			transitionsList.addItem(id);
			transitionsList.setSelectedValue(id, true);
			for (String s : controlList.getItems()) {
				if (oldName != null) {
					String[] array = s.split("\\s");
					for (String t : array) {
						if (t.equals(oldName)) {
							controlList.removeItem(s);
							s = s.replace(oldName, id);
							controlList.addItem(s);
						}
					}
				}
			}
		}
		else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}

	public boolean save(String transition) {
		if (!lhpn.containsTransition(transition)) {
			lhpn.addTransition(transition);
		}
		lhpn.getTransition(transition).removeAllAssign();
		if (boolAssignments.getItems() != null) {
			for (String s : boolAssignments.getItems()) {
				String[] tempArray = s.split(":=");
				if (!lhpn.getTransition(transition).addBoolAssign(tempArray[0], tempArray[1].replaceAll("\\s+", "")))
					return false;
			}
		}
		if (varAssignments.getItems() != null) {
			for (String s : varAssignments.getItems()) {
				String[] tempArray = s.split(":=");
				lhpn.getTransition(transition).addContAssign(tempArray[0], tempArray[1].replaceAll("\\s+", ""));
			}
		}
		if (intAssignments.getItems() != null) {
			for (String s : intAssignments.getItems()) {
				String[] tempArray = s.split(":=");
				if (!lhpn.getTransition(transition).addIntAssign(tempArray[0], tempArray[1].replaceAll("\\s+", "")))
					return false;
			}
		}
		if (rateAssignments.getItems() != null) {
			for (String s : rateAssignments.getItems()) {
				String[] tempArray = s.split("':=");
				if (!lhpn.getTransition(transition).addRateAssign(tempArray[0], tempArray[1].replaceAll("\\s+", "")))
					return false;
			}
		}
		// String delay;
		if (!lhpn.getTransition(transition).addDelay(fields.get("delay").getValue().replaceAll("\\s+", "")))
			return false;
		if (!lhpn.getTransition(transition).addPriority(fields.get("priority").getValue().replaceAll("\\s+", "")))
			return false;
		if (fail.isSelected()) {
			lhpn.getTransition(transition).setFail(true);
		}
		else {
			lhpn.getTransition(transition).setFail(false);
		}
		lhpn.getTransition(transition).setPersistent(persistent.isSelected());
		if (fields.get("Enabling Condition") != null) {
			if (!lhpn.getTransition(transition).addEnabling(fields.get("Enabling Condition").getValue().replaceAll("\\s+", "")))
				return false;
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {

		}
		Object o = e.getSource();
		if (o instanceof Runnable) {
			((Runnable) o).run();
		}
	}

	public class RemoveButton extends AbstractRunnableNamedButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RemoveButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		@Override
		public void run() {
			if (list.getSelectedValue() != null) {
				String assignment = list.getSelectedValue().toString();
				if (isBoolean(assignment)) {
					list.removeItem(assignment);
					boolAssignments.removeItem(assignment);
					lhpn.getTransition(selected).removeBoolAssign(assignment);
				}
				else if (isContinuous(assignment)) {
					list.removeItem(assignment);
					varAssignments.removeItem(assignment);
					lhpn.getTransition(selected).removeContAssign(assignment);
				}
				else if (isRate(assignment)) {
					list.removeItem(assignment);
					rateAssignments.removeItem(assignment);
					lhpn.getTransition(selected).removeRateAssign(assignment);
				}
				else if (isInteger(assignment)) {
					list.removeItem(assignment);
					intAssignments.removeItem(assignment);
					lhpn.getTransition(selected).removeIntAssign(assignment);
				}
			}
		}

		private PropertyList list = null;
	}

	public class EditButton extends AbstractRunnableNamedButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EditButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		@Override
		public void run() {
			new EditCommand(getName(), list).run();
		}

		private PropertyList list = null;
	}

	private class EditCommand implements Runnable {
		public EditCommand(String name, PropertyList list) {
			this.name = name;
			this.list = list;
		}

		@Override
		public void run() {
			if (name == null || name.equals("")) {
				 JOptionPane.showMessageDialog(Gui.frame, "Nothing selected to edit", "Error", JOptionPane.ERROR_MESSAGE); 
				return;
			}
			if (list.getSelectedValue() == null && getName().contains("Edit")) {
				 JOptionPane.showMessageDialog(Gui.frame, "Nothing selected to edit", "Error", JOptionPane.ERROR_MESSAGE); 
				return;
			}
			String variable = null;
			if (list.getSelectedValue() != null && getName().contains("Edit")) {
				variable = list.getSelectedValue().toString();
			}
			if ((lhpn.getContVars().length == 0) && (lhpn.getBooleanVars().length == 0)
					&& (lhpn.getIntVars().length == 0)) {
			  JOptionPane.showMessageDialog(Gui.frame, "Add variables first", "Error", JOptionPane.ERROR_MESSAGE); 
			}
			else if (getName().contains("Add")
					&& (list.getItems().length == lhpn.getContVars().length
							+ lhpn.getBooleanVars().length + lhpn.getIntVars().length)) {
				 JOptionPane.showMessageDialog(Gui.frame, "All variables have already been assigned in this transition", "Error", JOptionPane.ERROR_MESSAGE); 
			}
			else {
				AssignmentPanel assignmentPanel = new AssignmentPanel(variable, list, varAssignments,
						rateAssignments, boolAssignments, intAssignments, lhpn);
				assignmentPanel.displayTransitionGui();
			}
		}

		public String getName() {
			return name;
		}

		private String name = null;

		private PropertyList list = null;
	}

	private boolean isBoolean(String assignment) {
		for (String s : boolAssignments.getItems()) {
			if (s.equals(assignment)) {
				return true;
			}
		}
		return false;
	}

	private boolean isContinuous(String assignment) {
		for (String s : varAssignments.getItems()) {
			if (s.equals(assignment)) {
				return true;
			}
		}
		return false;
	}

	private boolean isInteger(String assignment) {
		for (String s : intAssignments.getItems()) {
			if (s.equals(assignment)) {
				return true;
			}
		}
		return false;
	}

	private boolean isRate(String assignment) {
		for (String s : rateAssignments.getItems()) {
			if (s.equals(assignment)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	
	@Override
	public void mouseExited(MouseEvent e) {	
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			flag = !flag;
			if (flag) {
				AssignmentPanel assignmentPanel = new AssignmentPanel(assignments.getSelectedValue().toString(), assignments, varAssignments,
					rateAssignments, boolAssignments, intAssignments, lhpn);
				assignmentPanel.displayTransitionGui();
			}
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
