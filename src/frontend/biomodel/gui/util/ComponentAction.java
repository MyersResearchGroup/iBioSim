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
package frontend.biomodel.gui.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import frontend.main.Gui;

public class ComponentAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7810208239930810196L;

	private String id;

	private Gui gui;

	public ComponentAction(String name, String id, Gui gui) {
		super(name);
		this.id = id;
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		gui.openModelEditor(id, false);
	}

}
