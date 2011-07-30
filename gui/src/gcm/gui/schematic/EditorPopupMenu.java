package gcm.gui.schematic;

import gcm.gui.Grid;
import gcm.gui.GridAction;

import javax.swing.JPopupMenu;
import com.mxgraph.swing.util.mxGraphActions;

public class EditorPopupMenu extends JPopupMenu
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

	/**
	 * constructor
	 * @param editor the schematic creating the popup menu
	 */
	public EditorPopupMenu(Schematic editor) {
		
		Grid grid = editor.getGrid();
		
		boolean selected = !editor.getGraphComponent().getGraph().isSelectionEmpty();
		add(editor.bind("Delete", mxGraphActions.getDeleteAction())).setEnabled(selected);
		addSeparator();
		
		if (grid.isEnabled()) {
			
			add(new GridAction("Select All Locations", editor));
			add(new GridAction("De-select All Locations", editor))
				.setEnabled(editor.getGrid().isALocationSelected());
			add(new GridAction("Clear Selected Location(s)", editor))
				.setEnabled(editor.getGrid().isALocationSelected());
			add(new GridAction("Add Component(s) to (Non-Occupied) Selected Location(s)", 
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
