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
package edu.utah.ece.async.ibiosim.gui.modelEditor.comp;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.utah.ece.async.ibiosim.gui.modelEditor.schematic.ModelEditor;
import edu.utah.ece.async.ibiosim.gui.modelEditor.schematic.Schematic;


/**
 * grid actions
 * these come from the right-click menu
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class GridAction extends AbstractAction {
	
	private static final long serialVersionUID = 1L;
	
	Schematic schematic; 
	Grid grid;

	public GridAction(String name, Schematic schematic) {
		
		super(name);
		
		//we need the gcm to do deletion/addition of nodes through these actions
		this.schematic = schematic;
		this.grid = schematic.getGrid();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getActionCommand().equals("Clear Selected Location(s)")) {
			
			grid.eraseSelectedNodes(schematic.getGCM());
			schematic.getGCM2SBML().getSpeciesPanel().refreshSpeciesPanel(schematic.getGCM());
			schematic.getGCM2SBML().makeUndoPoint();
		}
		else if (event.getActionCommand().equals("Add Module(s) to (Non-Occupied) Selected Location(s)")) {
			
			//bring up a panel so the component/gcm can be chosen to add to the selected locations
			boolean added = DropComponentPanel.dropSelectedComponents(
					schematic.getGCM2SBML(), schematic.getGCM());
			
			if (added) {
				
				ModelEditor gcm2sbml = schematic.getGCM2SBML();
				gcm2sbml.setDirty(true);
				gcm2sbml.refresh();
				schematic.getGraph().buildGraph();
				schematic.repaint();
				schematic.getGCM2SBML().getSpeciesPanel().refreshSpeciesPanel(schematic.getGCM());
				schematic.getGCM2SBML().makeUndoPoint();
				
				return;
			}
		}
		else if (event.getActionCommand().equals("Select All Locations"))
			grid.selectAllNodes();
		else if (event.getActionCommand().equals("De-select All Locations"))
			grid.deselectAllNodes();
		
		schematic.getGraph().buildGraph();
		schematic.repaint();
	}
}
