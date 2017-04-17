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
package edu.utah.ece.async.gui.util;

import java.awt.*;
import javax.swing.*;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Log extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5114950687877373374L;

	private JTextArea logArea; // log text area

	private JScrollPane scroll;

	public Log() {
		// sets up the log text area and menu
		this.setPreferredSize(new Dimension(1150, 150));
		this.setLayout(new BorderLayout());
		logArea = new JTextArea();
		logArea.setEditable(false);
		scroll = new JScrollPane();
		scroll.setViewportView(logArea);
		this.add(scroll, "Center");
	}

	public void addText(String text) {
		logArea.append(text + "\n");
		logArea.setSelectionStart(logArea.getText().length());
		logArea.setSelectionEnd(logArea.getText().length());
	}
}
