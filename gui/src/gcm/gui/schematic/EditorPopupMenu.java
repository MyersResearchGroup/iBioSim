package gcm.gui.schematic;

import javax.swing.JPopupMenu;
//import com.mxgraph.examples.swing.editor.EditorActions.HistoryAction;
import com.mxgraph.swing.util.mxGraphActions;

public class EditorPopupMenu extends JPopupMenu
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

//	public EditorPopupMenu(BasicGraphEditor editor)
	public EditorPopupMenu(Schematic editor)
	{
		boolean selected = !editor.getGraphComponent().getGraph().isSelectionEmpty();
		add(editor.bind("Delete", mxGraphActions.getDeleteAction())).setEnabled(selected);
		addSeparator();
		add(editor.bind("Select Vertices", mxGraphActions.getSelectVerticesAction()));
		add(editor.bind("Select Edges", mxGraphActions.getSelectEdgesAction()));
		addSeparator();
		add(editor.bind("Select All", mxGraphActions.getSelectAllAction()));
	}
}
