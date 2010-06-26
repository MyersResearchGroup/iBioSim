package gcm2sbml.gui.modelview;

import gcm2sbml.gui.InfluencePanel;
import gcm2sbml.gui.PropertiesLauncher;
import gcm2sbml.util.GlobalConstants;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Properties;


import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;

public class ModelView extends JPanel implements ActionListener {
		
	private static final long serialVersionUID = 1L;
	BioGraph graph;
	private mxGraphComponent graphComponent;
	public mxGraphComponent getGraphComponent(){return graphComponent;};
	
	/**
	 * Store a reference to the species, influences, etc that make up the graph.
	 */
	private HashMap<String, HashMap<String, Properties>> internalModel;
	
	/**
	 * Constructor
	 * @param internalModel
	 */
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
			
			// Listen for moved cells
			graph.addListener(mxEvent.CELLS_MOVED, new mxEventSource.mxIEventListener() {
				
				@Override
				public void invoke(Object arg0, mxEventObject event) {

					Object cells[] = (Object [])event.getProperties().get("cells");

					for(int i=0; i<cells.length; i++){
						mxCell cell = (mxCell)cells[i];
						graph.updateInternalPosition(cell);
					}
					
				}
			});
			
			// Listen for deleted cells
			graph.addListener(mxEvent.CELLS_REMOVED, new mxEventSource.mxIEventListener() {
				
				@Override
				public void invoke(Object arg0, mxEventObject event) {

					// if the graph isn't being built and this event
					// comes through, remove all the cells from the 
					// internal model that were specified.
					if(graph.is_building == false){
						Object cells[] = (Object [])event.getProperties().get("cells");
						for(Object ocell:cells){
							mxCell cell = (mxCell)ocell;
							System.out.print(cell.getId() + " Deleting.\n");
							if(cell.isEdge()){
								internalModel.get("influences").remove(cell.getId());
								PropertiesLauncher.getInstance().removeInfluenceFromList(cell.getId());
							}else if(cell.isVertex()){
								internalModel.get("species").remove(cell.getId());
								PropertiesLauncher.getInstance().removeSpeciesFromList(cell.getId());
							}
						}
					}
				}
			});

//			// listener for added verticies
			graph.addListener(mxEvent.CELLS_ADDED, new mxEventSource.mxIEventListener() {
				@Override
				public void invoke(Object arg0, mxEventObject event) {
					
					// if the graph is building, ignore the creation of edges.
					if(graph.is_building == false){
					
  						Object cells[] = (Object [])event.getProperties().get("cells");
						
						if(cells.length == 1 && ((mxCell)(cells[0])).isEdge()){
		
							mxCell edge = (mxCell)(cells[0]);
							
							// make sure there is a target cell. If there isn't it is because
							// the user dragged an edge and let it go when it wasn't connected
							// to anything. Remove it and return if this is the case.
							if(edge.getTarget() == null){
								graph.removeCells(cells);
								return;
							}
							
							String sourceId = edge.getSource().getId();
							String targetId = edge.getTarget().getId();
							
							String isBio;
							String type;
							String constType;
							if(activation_button.isSelected()){
								isBio = "no"; type = "activation"; constType = GlobalConstants.ACTIVATION;
							}else if(inhibition_button.isSelected()){
								isBio = "no"; type = "inhibition"; constType = GlobalConstants.REPRESSION;
							}else if(bio_activation_button.isSelected()){
								isBio = "yes"; type = "activation"; constType = GlobalConstants.ACTIVATION;
							}else if(bio_inhibition_button.isSelected()){
								isBio = "yes"; type = "inhibition"; constType = GlobalConstants.REPRESSION;
							}else if(no_influence_button.isSelected()){
								isBio = ""; type = "no influence"; constType = GlobalConstants.NOINFLUENCE;
							}else{
								throw(new Error("No influence button was pressed!"));
							}
							
							// TODO: If the user drags an influence onto nothing, it is intended that a new 
							// species will be created and the influence attached to it. We need to handle 
							// that eventuality.
							
							String promoter = "default";
							String name = InfluencePanel.buildName(sourceId, targetId, type, isBio, promoter);
							
							graph.addInfluence(edge, name, constType);
							PropertiesLauncher.getInstance().addInfluenceToList(name);
						}
					}

				}
			});
			
			
			
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
							refreshGraph();
							
						}
					}else if (e.isPopupTrigger()){
						showGraphPopupMenu(e);
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
	AbstractButton activation_button;
	AbstractButton inhibition_button;
	AbstractButton bio_activation_button;
	AbstractButton bio_inhibition_button;
	AbstractButton no_influence_button;
	private JToolBar buildToolBar(){

		JToolBar toolBar = new JToolBar();

		toolBar.add(Utils.makeToolButton("", "showLayouts", "Apply Layout", this));
		toolBar.add(Utils.makeToolButton("add_species.png", "addSpecies", "Add Species", this));
		toolBar.add(Utils.makeToolButton("", "addInfluence", "Add Influence", this));

		ButtonGroup influenceButtonGroup = new ButtonGroup();
		
		activation_button = Utils.makeRadioToolButton("activation.png", "", "Create Activation Influences", this, influenceButtonGroup);
		activation_button.setSelected(true);
		toolBar.add(activation_button);
		inhibition_button = Utils.makeRadioToolButton("inhibition.png", "", "Create Repression Influences", this, influenceButtonGroup);
		toolBar.add(inhibition_button);
		bio_activation_button = Utils.makeRadioToolButton("bio_activation.png", "", "Create Biological Activation Influences", this, influenceButtonGroup);
		toolBar.add(bio_activation_button);
		bio_inhibition_button = Utils.makeRadioToolButton("bio_inhibition.png", "", "Create Biological Repression Influences", this, influenceButtonGroup);
		toolBar.add(bio_inhibition_button);
		no_influence_button = Utils.makeRadioToolButton("no_influence.png", "", "Explicitly Set No Influences", this, influenceButtonGroup);
		toolBar.add(no_influence_button);
		
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
		}else if(command == ""){
			// radio buttons don't have to do anything and have an action command of "".
		}else{
			throw(new Error("Invalid actionCommand: " + command));
		}
	}
	
	
	////// Coppied from mxGraph example file BasicGraphEditor.java
	
	/**
	 * Displays the right-click menu
	 * @param e
	 */
	protected void showGraphPopupMenu(MouseEvent e)
	{
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),
				graphComponent);
		EditorPopupMenu menu = new EditorPopupMenu(ModelView.this);
		menu.show(graphComponent, pt.x, pt.y);

		e.consume();
	}
	@SuppressWarnings("serial")
	public Action bind(String name, final Action action)
	{
		return new AbstractAction(name, null)
		{
			public void actionPerformed(ActionEvent e)
			{
				action.actionPerformed(new ActionEvent(getGraphComponent(), e
						.getID(), e.getActionCommand()));
			}
		};
	}
	
	
	

}
