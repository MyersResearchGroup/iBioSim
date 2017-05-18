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
package edu.utah.ece.async.ibiosim.gui.modelEditor.sbol;

import java.util.List;

import javax.swing.JCheckBox;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.TopLevel;

import edu.utah.ece.async.sboldesigner.swing.AbstractListTableModel;

/**
 * 
 * 
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
class TopLevelTableModel extends AbstractListTableModel<TopLevel> {
	private static final String[] COLUMNS = {"Type", "Display Id", "Name", "Version", "Description" };
	private static final double[] WIDTHS = { 0.1, 0.2, 0.2, 0.1, 0.4 };

	public TopLevelTableModel(List<TopLevel> topLevelObjects) {
		super(topLevelObjects, COLUMNS, WIDTHS);
	}

	public Object getField(TopLevel topLevelObject, int col) {
		switch (col) {
		case 0:
			return "Part";
		case 1:
			return topLevelObject.getDisplayId();
		case 2:
			return topLevelObject.getName();
		case 3:
			return topLevelObject.getVersion();
		case 4:
			return topLevelObject.getDescription();
		default:
			throw new IndexOutOfBoundsException();
		}
	}
}