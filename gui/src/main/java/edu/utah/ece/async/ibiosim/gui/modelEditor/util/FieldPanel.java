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
package edu.utah.ece.async.ibiosim.gui.modelEditor.util;

import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * Describes a panel that contains a label and field with a value
 * 
 * @author Nam Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class FieldPanel extends JPanel implements NamedObject, FocusListener {

	private static final long serialVersionUID = 1L;
	
	public FieldPanel(String name, Pattern pattern, boolean editable) {
		super(new GridLayout(1, 2));
		super.setName(name);
		//this.editable = editable;
		this.pattern = pattern;
		this.field = new JTextField(40);
		field.addFocusListener(this);		
		this.add(new JLabel(name));
		this.add(field);
		field.setEditable(editable);
		 
	}

	@Override
	public String getName() {
		return super.getName();
	}

	public String getValue() {
		return field.getText();
	}

	@Override
	public void focusGained(FocusEvent e) {
		//Do nothing
	}
	
	@Override
	public void focusLost(FocusEvent e) {
		//Must check to see if input was valid
		Matcher matcher =  pattern.matcher(field.getText());
		if (!matcher.find()) {
			JOptionPane
			.showMessageDialog(
					this,
					"Invalid input.");
		}
		field.requestFocus();
	}

	private Pattern pattern = null;
	//private boolean editable = true;
	private JTextField field = null;	
}
