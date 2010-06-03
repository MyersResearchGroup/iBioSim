package gcm2sbml.gui.modelview;

import gcm2sbml.gui.PropertiesLauncher;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Properties;


import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;

public class ModelView extends JPanel implements ActionListener {
		
	private static final long serialVersionUID = 1L;
	BioGraph graph;
	mxGraphComponent graphComponent;
	
	/**
	 * Store a reference to the species, influences, etc that make up the graph.
	 */
	private HashMap<String, HashMap<String, Properties>> internalModel;
	
	public ModelView(HashMap<String, HashMap<String, Properties>> internalModel){
		super(new BorderLayout());
		this.internalModel = internalModel;
	}
	
	/**
	 * Called when the tab is clicked.
	 * @param graph
	 */
	public void display(){

		if(graph == null){
			graph = new BioGraph(internalModel);
			refreshGraph();
		}
		
		boolean needs_layouting = graph.buildGraph();
		
		// Create and plug in the graphComponent
		if(graphComponent == null){
			graphComponent = new mxGraphComponent(graph);
			graphComponent.setGraph(graph);
			this.add(graphComponent, BorderLayout.CENTER);
			this.add(buildToolBar(), BorderLayout.NORTH);
			
			// Add a listener for when cells get clicked on.
			graphComponent.getGraphControl().addMouseListener(new MouseAdapter(){
				public void mouseReleased(MouseEvent e)
				{
					if(e.getClickCount() == 2){
						mxCell cell = (mxCell)(graphComponent.getCellAt(e.getX(), e.getY()));
						
						if (cell != null){
							System.out.println("cell="+graph.getLabel(cell) + " " + e.getClickCount());
							graph.cellClickHandler(cell);
//							graph.updateInternalPosition(cell);
							refreshGraph();
							
						}
					}
				}
			});
			
		}
	
	
		// Do layouting if it hasn't been done yet
		if(needs_layouting){
			graph.applyLayout("circleLayout", graphComponent);
		}
		
	}
	
	
	/**
	 * Refreshes the graph.
	 */
	private void refreshGraph(){
		graph.buildGraph();
	}
	
	/**
	 * create the toolbar.
	 * @return
	 */
	private JToolBar buildToolBar(){

		JToolBar toolBar = new JToolBar();

		toolBar.add(Utils.makeToolButton("", "showLayouts", "Apply Layout", this));
		toolBar.add(Utils.makeToolButton("", "addSpecies", "Add Species", this));
		toolBar.add(Utils.makeToolButton("", "addInfluence", "Add Influence", this));

		return toolBar;
	}

	/**
	 * Called when a toolbar button is clicked.
	 */
	JPopupMenu layoutPopup;
	public void actionPerformed(ActionEvent event){
		String command = event.getActionCommand();
				
		if(command.equals("showLayouts")){
			// Show the layout popup
			layoutPopup = new JPopupMenu();
			
			for(JMenuItem item:Layouting.getMenuItems(this))
				layoutPopup.add(item); 	
			
			JComponent showLayoutsButton = (JComponent)event.getSource();
			
			layoutPopup.show(this, showLayoutsButton.getX(), showLayoutsButton.getY()+showLayoutsButton.getHeight());
			
		}else if(command.indexOf("layout_") == 0){
			// Layout actioncommands are prepended with "_"
			command = command.substring(command.indexOf('_')+1);
			graph.applyLayout(command, this.graphComponent);
		}else if(command == "addSpecies"){
			PropertiesLauncher.getInstance().launchSpeciesEditor(null);
			
			refreshGraph();
		}else if(command == "addInfluence"){
			PropertiesLauncher.getInstance().launchInfluencePanel(null);
			
			refreshGraph();
		}else{
			throw(new Error("Invalid actionCommand: " + command));
		}
	}

}
