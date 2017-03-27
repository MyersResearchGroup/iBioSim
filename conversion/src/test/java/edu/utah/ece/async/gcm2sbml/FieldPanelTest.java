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
package test.java.edu.utah.ece.async.gcm2sbml;

import java.awt.GridLayout;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import junit.framework.TestCase;
import test.java.edu.utah.ece.async.gcm2sbml.gui.FieldPanel;
import test.java.edu.utah.ece.async.gcm2sbml.gui.PropertyField;
import test.java.edu.utah.ece.async.gcm2sbml.util.Utility;

public class FieldPanelTest extends TestCase {

	//@Before
	public void setUp() throws Exception {
	}

	public void Panel() {
		JFrame frame = new JFrame();
		JPanel panel = new JPanel(new GridLayout(1, 3));
		panel.add(new FieldPanel("ID", Utility.IDpat, true));
		panel.add(new FieldPanel("NUM", Utility.NUMpat, true));
		panel.add(new JButton("Button"));
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);

		System.out.println();
	}

	public void testPropertyField() {
		JFrame frame = new JFrame();
		Properties p = new Properties();
		p.setProperty("decay", ".0075");
		PropertyField field = new PropertyField("decay", ".0075", PropertyField.states[0], ".0075");
		field.setRegExp(Utility.NUMstring);
		frame.add(field);
		frame.pack();
		frame.setVisible(true);

		System.out.println();
		field.setEnabled(false);
		field.setEnabled(true);
	}

}
