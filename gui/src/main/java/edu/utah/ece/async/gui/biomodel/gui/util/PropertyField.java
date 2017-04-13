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
package edu.utah.ece.async.gui.biomodel.gui.util;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import edu.utah.ece.async.dataModels.biomodel.parser.CompatibilityFixer;
import edu.utah.ece.async.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.gui.main.Gui;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class PropertyField extends JPanel implements ActionListener,
		PropertyProvider {

	private static final long serialVersionUID = 1L;

	public PropertyField(String name, String value, String state,
			String defaultValue, String repExp, boolean paramsOnly, String origString, boolean textArea) {
		super(new GridLayout(1, 3));
		this.textArea = textArea;
		paramStates = new String[] { origString, "modified" };
		this.paramsOnly = paramsOnly;
		if (state == null && !textArea) {
			setLayout(new GridLayout(1, 2));
		} else if (textArea) {
			setLayout(new BorderLayout());
		}
		this.defaultValue = defaultValue;
		init(name, value, state);
		setRegExp(repExp);
	}

	public PropertyField(String name, String value, String state,
			String defaultValue) {
		this(name, value, state, defaultValue, null, false, "default", false);
	}
	
	public String[] getStates() {
		if (paramsOnly) {
			return paramStates;
		}
		return states;
	}

	@Override
	public void setEnabled(boolean state) {
		// super.setEnabled(state);
		isEnabled = state;
		field.setEnabled(state);
		if (box != null) {
			box.setEnabled(state);
		}
		name.setEnabled(state);
		// if (state) {
		if (paramsOnly && box != null) {
			if (box.getSelectedItem().equals(paramStates[0])
					|| box.getSelectedItem().equals(states[0])) {
				setDefault();
			}
			else {
				setCustom();
			}
		}
		else {
			if (box != null) {
				if (box.getSelectedItem().equals(states[0])) {
					setDefault();
				}
				else {
					setCustom();
				}
			}
		}
		// }
	}

	private void init(String nameString, String valueString, String stateString) {
		name = new JLabel(nameString);
		name.setName(nameString);
		name.setText(nameString);
		if (textArea) {
			this.add(name,"North");
		} else {
			this.add(name);
		}
//		if (!(valueString == null) && !(stateString == null)) {
		if (nameString != null && stateString != null) {
			name.setText(CompatibilityFixer.getGuiName(nameString) + " (" + nameString + ") ");
//			idLabel = new JLabel("ID");
//			idLabel.setEnabled(false);
//			this.add(idLabel);
//			idField = new JTextField(CompatibilityFixer.getSBMLName(nameString));
//			idField.setEditable(false);
//			this.add(idField);
		}
		if (textArea) {
			field = new JTextArea(3,20);
			((JTextArea) field).setLineWrap(true);
			((JTextArea) field).setWrapStyleWord(true);
			scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(100, 100));
			scroll.setPreferredSize(new Dimension(100, 100));
			scroll.setViewportView(field);
		} else {
			field = new JTextField(20);
		}
		field.setText(valueString);
		if (stateString != null) {
			if (paramsOnly) {
				box = new JComboBox(new DefaultComboBoxModel(paramStates));
			}
			else {
				box = new JComboBox(new DefaultComboBoxModel(states));
			}
			box.addActionListener(this);
			this.add(box);
			if (paramsOnly) {
				if (stateString.equals(paramStates[0]) || stateString.equals(states[0])) {
					setDefault();
				} else {
					setCustom();
				}
			}
			else {
				if (stateString.equals(states[0])) {
					setDefault();
				} else {
					setCustom();
				}
			}
		}
		if (!textArea) {
			((JTextField)field).addActionListener(this);
		}
		if (textArea) {
			this.add(scroll,"Center");
		} else {
			this.add(field);
		}
		if (paramsOnly && box != null) {
			sweep = new JButton("Sweep");
			sweep.addActionListener(this);
			this.add(sweep);
			if (stateString != null && stateString.equals(paramStates[0])) {
				sweep.setEnabled(false);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			if (paramsOnly) {
				if (box.getSelectedItem().equals(paramStates[0]) || box.getSelectedItem().equals(states[0])) {
					setDefault();
				} else {
					setCustom();
				} 
			}
			else {
				if (box.getSelectedItem().equals(states[0])) {
					setDefault();
				} else {
					setCustom();
				}
			}
		} else if (e.getSource() == sweep) {
			Object[] options = { "Ok", "Close" };
			JPanel p = new JPanel(new GridLayout(4, 2));
			JLabel startLabel = new JLabel("Start:");
			JLabel stopLabel = new JLabel("Stop:");
			JLabel stepLabel = new JLabel("Step:");
			JLabel levelLabel = new JLabel("Level:");
			String[] list1 = { "1", "2" };
			String text = field.getText().trim();
			String startText = "";
			String stopText = "";
			String stepText = "";
			String levelText = "";
			if (!text.equals("") && text.startsWith("(")) {
				try {
					text = text.substring(1, text.length() - 1);
					String[] split = text.split(",");
					startText = split[0].trim();
					stopText = split[1].trim();
					stepText = split[2].trim();
					levelText = split[3].trim();
				}
				catch (Exception e1) {
				}
			}
			final JTextField start = new JTextField(startText);
			final JTextField stop = new JTextField(stopText);
			final JTextField step = new JTextField(stepText);
			final JComboBox level = new JComboBox(list1);
			level.setSelectedItem(levelText);
			p.add(startLabel);
			p.add(start);
			p.add(stopLabel);
			p.add(stop);
			p.add(stepLabel);
			p.add(step);
			p.add(levelLabel);
			p.add(level);
			int i = JOptionPane.showOptionDialog(this, p, "Sweep", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (i == JOptionPane.YES_OPTION) {
				double[] startVal = {0, 0};
				double[] stopVal = {0, 0};  
				double[] stepVal = {1, 0};  
				String sweeper = "(";
				try {
					String[] starter = start.getText().trim().split("/");
					startVal[0] = Double.parseDouble(starter[0]);
					if (starter.length == 2)
						startVal[1] = Double.parseDouble(starter[1]);
				}
				catch (Exception e1) {
				}
				sweeper = sweeper + startVal[0];
				if (startVal[1] != 0)
					sweeper = sweeper + "/" + startVal[1];
				sweeper = sweeper + ",";
				try {
					String[] stopper = stop.getText().trim().split("/");
					stopVal[0] = Double.parseDouble(stopper[0]);
					if (stopper.length == 2)
						stopVal[1] = Double.parseDouble(stopper[1]);
				}
				catch (Exception e1) {
				}
				sweeper = sweeper + stopVal[0];
				if (stopVal[1] != 0)
					sweeper = sweeper + "/" + stopVal[1];
				sweeper = sweeper + ",";
				try {
					String[] stepper = step.getText().trim().split("/");
					stepVal[0] = Double.parseDouble(stepper[0]);
					if (stepper.length == 2)
						stepVal[1] = Double.parseDouble(stepper[1]);
				}
				catch (Exception e1) {
				}
				sweeper = sweeper + stepVal[0];
				if (stepVal[1] != 0)
					sweeper = sweeper + "/" + stepVal[1];
				sweeper = sweeper + "," + level.getSelectedItem() + ")";
				setValue(sweeper);
			}
		} else {
			if (Utility.isValid(e.getActionCommand(), regExp)) {
			} else {
				JOptionPane.showMessageDialog(Gui.frame, "Illegal value entered.",
						"Illegal value entered", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void setDefault() {
		field.setEnabled(false);
		//name.setEnabled(false);
		field.setText(defaultValue);
		if (paramsOnly) {
			box.setSelectedItem(paramStates[0]);
			if (sweep != null) {
				sweep.setEnabled(false);
			}
		}
		else {
			box.setSelectedItem(states[0]);
		}
	}

	public void setCustom() {
		// if (isEnabled && box != null) {
		if (box != null) {
			if (isEnabled) {
				field.setEnabled(true);
				//name.setEnabled(true);
			}
			if (paramsOnly) {
				box.setSelectedItem(paramStates[1]);
				if (sweep != null) {
					sweep.setEnabled(true);
				}
			}
			else {
				box.setSelectedItem(states[1]);
			}
		}
	}

	public String getState() {
		if (box == null) {
			return null;
		}
		return box.getSelectedItem().toString();
	}

	@Override
	public String getKey() {
		return name.getName();
	}

	@Override
	public String getValue() {
		return field.getText();
	}

	@Override
	public void setKey(String key) {
		name.setName(key);
		name.setText(CompatibilityFixer.getGuiName(key));
	}

	@Override
	public void setValue(String value) {
		field.setText(value);
	}

	public boolean isValidValue() {
		if (getValue() == null) {
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

	private JComboBox box = null;

	private JTextComponent field = null;
	
	private JScrollPane scroll = null;
	
	private JButton sweep = null;

	// private JLabel idL

	private String[] states = new String[] { "default", "custom" };
	
	private String[] paramStates;

	private String defaultValue = null;
	
	private boolean paramsOnly;
	
	private boolean textArea;
}
