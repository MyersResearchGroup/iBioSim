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
package gcm2sbml;


import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import gcm2sbml.gui.GCM2SBMLEditor;
import junit.framework.TestCase;

import org.junit.Before;

public class GCM2SBMLEditorTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}
	
	public void testList() {
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		DefaultListModel model = new DefaultListModel();
		model.addElement("foo");
		model.addElement("bar");
		JList list = new JList(model);
		panel.add(list);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		System.out.println();
		model.remove(1);
		System.out.println();
	}
	
	public void testView() {
		System.loadLibrary("sbmlj");
		GCM2SBMLEditor editor = new GCM2SBMLEditor("", "nand.dot", null);
		editor.setVisible(true);
		JFrame frame = new JFrame();
		frame.add(editor);
		frame.pack();
		frame.setVisible(true);
		System.out.println();
		System.out.println();
	}
	
	

}
