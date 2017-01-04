package backend.biomodel.gui.schematic;


import javax.swing.JPopupMenu;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.util.mxGraphActions;

import backend.biomodel.gui.comp.Grid;
import backend.biomodel.gui.comp.GridAction;
import backend.biomodel.gui.util.ComponentAction;
import backend.biomodel.util.GlobalConstants;
import frontend.main.Gui;

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
