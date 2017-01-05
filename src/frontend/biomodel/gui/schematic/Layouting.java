package frontend.biomodel.gui.schematic;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.mxPartitionLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxRectangle;

public class Layouting {

	public static final String[][] LAYOUTS = {
		{"verticalHierarchical", "Vertical Hierarchial (acyclic only)"},
		{"horizontalHierarchical", "Horizontal Hierarchical (acyclic only)"},
		{"verticalTree", "Vertical Tree (acyclic only)"},
		{"horizontalTree", "Horizontal Tree (acyclic only)"},
		//{"parallelEdges", "Parallel Edges"},
		//{"placeEdgeLabels", "Place Edge Labels"},
		{"organicLayout", "Organic Layout"},
		//{"verticalPartition", "Vertical Partition"},
		//{"horizontalPartition", "Horizontal Partition"},
		//{"verticalStack", "Vertical Stack"},
		//{"horizontalStack", "Horizontal Stack"},
		{"circleLayout", "Circle Layout"},
	};
	
//	public static JButton[] getLayoutButtons(ActionListener listener){
//		JButton[] buttons = new JButton[LAYOUTS.length];
//		
//		for(int i=0; i<LAYOUTS.length; i++){
//			buttons[i] = Utils.makeToolButton("", LAYOUTS[i][0], LAYOUTS[i][1], listener);
//		}
//		
//		return buttons;
//	}
	
	
	public static JMenuItem[] getMenuItems(ActionListener listener){
		JMenuItem[] items = new JMenuItem[LAYOUTS.length];
		
		for(int i=0; i<LAYOUTS.length; i++){
			JMenuItem item = new JMenuItem(LAYOUTS[i][1]);
			
			item.addActionListener(listener);
			item.setActionCommand("layout_" + LAYOUTS[i][0]);
			
			items[i] = item;
		}
		
		return items;
	}
	
	/**
	 * 
	 * @param ident: A String representing the type of layout
	 * @return: A mxIGraphLayout
	 */
	public static void applyLayout(String ident, BioGraph graph, final mxGraphComponent graphComponent)
	{
		mxIGraphLayout layout = null;

		if (ident != null)
		{

			if (ident.equals("verticalHierarchical"))
			{
				layout = new mxHierarchicalLayout(graph);
			}
			else if (ident.equals("horizontalHierarchical"))
			{
				layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);
			}
			else if (ident.equals("verticalTree"))
			{
				layout = new mxCompactTreeLayout(graph, false);
			}
			else if (ident.equals("horizontalTree"))
			{
				layout = new mxCompactTreeLayout(graph, true);
			}
			else if (ident.equals("parallelEdges"))
			{
				layout = new mxParallelEdgeLayout(graph);
			}
			else if (ident.equals("placeEdgeLabels"))
			{
				layout = new mxEdgeLabelLayout(graph);
			}
			else if (ident.equals("organicLayout"))
			{
				layout = new mxOrganicLayout(graph);
			}
			if (ident.equals("verticalPartition"))
			{
				layout = new mxPartitionLayout(graph, false)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					@Override
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("horizontalPartition"))
			{
				layout = new mxPartitionLayout(graph, true)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					@Override
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("verticalStack"))
			{
				layout = new mxStackLayout(graph, false)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					@Override
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("horizontalStack"))
			{
				layout = new mxStackLayout(graph, true)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					@Override
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("circleLayout"))
			{
				layout = new mxCircleLayout(graph);
			}
		}
		if (layout != null) {
			Object cell = graph.getDefaultParent();
			layout.execute(cell);
			graph.updateAllInternalPosition();
		}
	}
}
