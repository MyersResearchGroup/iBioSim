package frontend.biomodel.gui.comp;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import frontend.biomodel.gui.schematic.ModelEditor;
import frontend.biomodel.gui.schematic.Schematic;


/**
 * grid actions
 * these come from the right-click menu
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
			schematic.getGCM().makeUndoPoint();
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
				schematic.getGCM().makeUndoPoint();
				
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
