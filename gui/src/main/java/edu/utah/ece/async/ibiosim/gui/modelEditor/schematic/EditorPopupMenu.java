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
package edu.utah.ece.async.ibiosim.gui.modelEditor.schematic;


import javax.swing.JPopupMenu;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.util.mxGraphActions;

import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.modelEditor.comp.Grid;
import edu.utah.ece.async.ibiosim.gui.modelEditor.comp.GridAction;
import edu.utah.ece.async.ibiosim.gui.modelEditor.util.ComponentAction;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class EditorPopupMenu extends JPopupMenu
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

	/**
	 * constructor
	 * @param editor the schematic creating the popup menu
	 * @param cell 
	 * @param biosim 
	 */
	public EditorPopupMenu(Schematic editor, mxCell cell, Gui biosim) {
		
		Grid grid = editor.getGrid();

		boolean selected = !editor.getGraphComponent().getGraph().isSelectionEmpty();
		add(editor.bind("Delete", mxGraphActions.getDeleteAction())).setEnabled(selected);
		addSeparator();
		
		if (cell != null && editor.getGraph().getCellType(cell).equals(GlobalConstants.COMPONENT)) {
			add(new ComponentAction("Open Module in New Tab", editor.getGraph().getModelFileName(cell.getId()), biosim));
			addSeparator();
		}

		if (grid.isEnabled()) {
			
			add(new GridAction("Select All Locations", editor));
			add(new GridAction("De-select All Locations", editor))
				.setEnabled(editor.getGrid().isALocationSelected());
			add(new GridAction("Clear Selected Location(s)", editor))
				.setEnabled(editor.getGrid().isALocationSelected());
			add(new GridAction("Add Module(s) to (Non-Occupied) Selected Location(s)", 
				editor)).setEnabled(editor.getGrid().isALocationSelected());
		}
		else {	
			
			add(editor.bind("Select Vertices", mxGraphActions.getSelectVerticesAction()));
			add(editor.bind("Select Edges", mxGraphActions.getSelectEdgesAction()));
			addSeparator();
			add(editor.bind("Select All", mxGraphActions.getSelectAllAction()));
		}
	}
}
