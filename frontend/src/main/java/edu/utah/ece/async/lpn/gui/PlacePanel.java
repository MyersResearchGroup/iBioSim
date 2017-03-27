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
package main.java.edu.utah.ece.async.lpn.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import main.java.edu.utah.ece.async.biomodel.gui.util.PropertyList;
import main.java.edu.utah.ece.async.biomodel.util.Utility;
import main.java.edu.utah.ece.async.lpn.parser.*;
import main.java.edu.utah.ece.async.main.Gui;
import main.java.edu.utah.ece.async.util.GlobalConstants;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class PlacePanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String selected = "";

	private PropertyList placeList, controlList;

	private JComboBox initBox;

	private String[] initCond = { "true", "false" };

	private String[] options = { "Ok", "Cancel" };

	private LPN lhpn;

	private HashMap<String, PropertyField> fields = null;

	public PlacePanel(String selected, PropertyList placeList, PropertyList controlList,
			LPN lhpn) {
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
			if (lhpn.getPlace(selected).isMarked()) {
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
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Place Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				 JOptionPane.showMessageDialog(Gui.frame,"Illegal values entered." , "Error", JOptionPane.ERROR_MESSAGE); 
				return false;
			}
			String[] allVariables = lhpn.getAllIDs();
			if (oldName == null) {
				for (int i=0; i<allVariables.length; i++) {
					if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
						 JOptionPane.showMessageDialog(Gui.frame, "Place id already exists.", "Error", JOptionPane.ERROR_MESSAGE); 
						return false;
					}
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				for (int i=0; i<allVariables.length; i++) {
					if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
					  JOptionPane.showMessageDialog(Gui.frame, "Place id already exists.", "Error", JOptionPane.ERROR_MESSAGE); 
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
			
			if (selected != null) {
				if (oldName != null && !oldName.equals(id)) {
					lhpn.changePlaceName(oldName, id);
				}
				lhpn.changeInitialMarking(id, ic);
			} else {
				lhpn.addPlace(id, ic);
			}
			
			for (String s : placeList.getItems()) {
				if (oldName != null) {
					String p = s.split(" ")[0];
					if (p.equals(oldName)) {
						placeList.removeItem(s);
					}
				}
			}
			String listName;
			if (ic) {
				listName = id + " -  marked";
				placeList.addItem(id + " -  marked");
			}
			else {
				listName = id + " - unmarked";
				placeList.addItem(id + " - unmarked");
			}
			placeList.setSelectedValue(listName, true);
			if (oldName != null  && !id.equals(oldName)) {
				for (String s : controlList.getItems()) {
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
		}
	}

}
