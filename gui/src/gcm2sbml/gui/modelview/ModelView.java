package gcm2sbml.gui.modelview;

import gcm2sbml.gui.InfluencePanel;
import gcm2sbml.gui.PromoterPanel;
import gcm2sbml.gui.PropertiesLauncher;
import gcm2sbml.parser.GCMFile;
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
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
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
	
	
	private GCMFile gcm;
	
	/**
	 * Constructor
	 * @param internalModel
	 */
	public ModelView(HashMap<String, HashMap<String, Properties>> internalModel, GCMFile gcm){
		super(new BorderLayout());
		this.internalModel = internalModel;
		
		this.gcm = gcm;
	}
	
	/**
	 * Called when the tab is clicked.
	 * @param graph
	 */
	public void display(){

		if(graph == null){
			graph = new BioGraph(internalModel);
			
			addGraphListeners();
			
			refreshGraph();
		}
		
		boolean needs_layouting = graph.buildGraph();
		
		// Create and plug in the graphComponent
		if(graphComponent == null){
			graphComponent = new mxGraphComponent(graph);
			graphComponent.setGraph(graph);
			this.add(graphComponent, BorderLayout.CENTER);
			this.add(buildToolBar(), BorderLayout.NORTH);
			addGraphComponentListeners();
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
	AbstractButton selectButton;
	AbstractButton addSpeciesButton;
	AbstractButton addComponentButton;
	AbstractButton editPromoterButton;
	
	AbstractButton activationButton;
	AbstractButton inhibitionButton;
	AbstractButton bioActivationButton;
	AbstractButton bioInhibitionButton;
	AbstractButton noInfluenceButton;
	private JToolBar buildToolBar(){

		JToolBar toolBar = new JToolBar();
		
		ButtonGroup modeButtonGroup = new ButtonGroup();
		selectButton =Utils.makeRadioToolButton("select_mode.png", "", "Select Mode", this, modeButtonGroup); 
		toolBar.add(selectButton);
		selectButton.setSelected(true);
		addSpeciesButton = Utils.makeRadioToolButton("add_species.png", "", "Add Species Mode", this, modeButtonGroup);
		toolBar.add(addSpeciesButton);
		addComponentButton = Utils.makeRadioToolButton("add_component.png", "", "Add Component Mode", this, modeButtonGroup);
		toolBar.add(addComponentButton);
		editPromoterButton = Utils.makeRadioToolButton("promoter_mode.png", "", "P", this, modeButtonGroup);
		toolBar.add(editPromoterButton);
		//toolBar.add(Utils.makeToolButton("", "addInfluence", "Add Influence", this));

		toolBar.addSeparator();
		ButtonGroup influenceButtonGroup = new ButtonGroup();
		
		activationButton = Utils.makeRadioToolButton("activation.png", "", "Create Activation Influences", this, influenceButtonGroup);
		activationButton.setSelected(true);
		toolBar.add(activationButton);
		inhibitionButton = Utils.makeRadioToolButton("inhibition.png", "", "Create Repression Influences", this, influenceButtonGroup);
		toolBar.add(inhibitionButton);
		bioActivationButton = Utils.makeRadioToolButton("bio_activation.png", "", "Create Biological Activation Influences", this, influenceButtonGroup);
		toolBar.add(bioActivationButton);
		bioInhibitionButton = Utils.makeRadioToolButton("bio_inhibition.png", "", "Create Biological Repression Influences", this, influenceButtonGroup);
		toolBar.add(bioInhibitionButton);
		noInfluenceButton = Utils.makeRadioToolButton("no_influence.png", "", "Explicitly Set No Influences", this, influenceButtonGroup);
		toolBar.add(noInfluenceButton);
		
		toolBar.addSeparator();
		
		toolBar.add(Utils.makeToolButton("choose_layout.png", "showLayouts", "Apply Layout", this));
		
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
			graph.buildGraph(); // rebuild, quick way to clear out any edge midpoints.
			graph.applyLayout(command, this.graphComponent);
//		}else if(command == "addSpecies"){
//			PropertiesLauncher.getInstance().launchSpeciesEditor(null);
//			
//			refreshGraph();
//		}else if(command == "addInfluence"){
//			PropertiesLauncher.getInstance().launchInfluencePanel(null);
//			
//			refreshGraph();
		}else if(command == ""){
			// radio buttons don't have to do anything and have an action command of "".
		}else{
			throw(new Error("Invalid actionCommand: " + command));
		}
	}
	
	/**
	 * Add listeners for the graph component
	 */
	private void addGraphComponentListeners(){

		
		// Add a listener for when cells get clicked on.
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger()){
					// rightclick on windows
					showGraphPopupMenu(e);
				}else if(e.getClickCount() == 1){
					// single click.
					// First check and if the user clicked on a component, let the graph lib take care of it.
					mxCell cell = (mxCell)(graphComponent.getCellAt(e.getX(), e.getY()));
					if(cell == null){
						// If control gets here, the user clicked once, and not on any component.
						if(selectButton.isSelected()){
							// do the default graph lib behavior
						}else if(addSpeciesButton.isSelected()){
							// plop a species down with good default info at the mouse coordinates
							graph.createSpecies(null, e.getX(), e.getY());
						}else if(addComponentButton.isSelected()){
							// Ask the user which component to add, then plop it down where the click happened
							
						}
					}else{
						if(editPromoterButton.isSelected()){
							// If the thing clicked on was an influence, bring up it's promotor window.
							if(cell.isEdge()){
								Properties prop = internalModel.get("influences").get(cell.getId());
								String promoter = prop.getProperty("Promoter");
								//System.out.print(promoter);
								if(promoter != null){
									PropertiesLauncher.getInstance().launchPromoterEditor(promoter);
									graph.buildGraph();
								}else{
									PromoterPanel p = PropertiesLauncher.getInstance().launchPromoterEditor(null);
									// set the selected influence to use the given promoter
									prop = internalModel.get("influences").get(cell.getId());
									// now rename the influence to incorporate the new promoter
									gcm.changeInfluencePromoter(cell.getId(), prop.getProperty(GlobalConstants.PROMOTER), p.getLastUsedPromoter());
									graph.buildGraph();
								}
							}
						}
					}
				}else if(e.getClickCount() == 2){
					// double click
					mxCell cell = (mxCell)(graphComponent.getCellAt(e.getX(), e.getY()));
					
					if (cell != null){
						System.out.println("cell="+graph.getLabel(cell) + " " + e.getClickCount());
						graph.bringUpEditorForCell(cell);
						refreshGraph();
						
					}
				}
			}
		});
		
		// Add a special listener for the MAC. Rightclick only works on mousedown,
		// while on windows it only works on mouseup.
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger()){
					// rightclick on mac
					showGraphPopupMenu(e);
				}
			}
		});
	}
	
	/**
	 * Adds listeners to the graph that watch for changes we are interested in.
	 */
	private void addGraphListeners(){
		// Listen for moved cells
		graph.addListener(mxEvent.CELLS_MOVED, new mxEventSource.mxIEventListener() {
			
//			@Override
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
			
//			@Override
			public void invoke(Object arg0, mxEventObject event) {

				// if the graph isn't being built and this event
				// comes through, remove all the cells from the 
				// internal model that were specified.
				if(graph.isBuilding == false){
					Object cells[] = (Object [])event.getProperties().get("cells");
					for(Object ocell:cells){
						mxCell cell = (mxCell)ocell;
						System.out.print(cell.getId() + " Deleting.\n");
						if(cell.isEdge()){
							internalModel.get("influences").remove(cell.getId());
							PropertiesLauncher.getInstance().removeInfluenceFromList(cell.getId());
							graph.influenceRemoved(cell.getId());
						}else if(cell.isVertex()){
							internalModel.get("species").remove(cell.getId());
							PropertiesLauncher.getInstance().removeSpeciesFromList(cell.getId());
							graph.speciesRemoved(cell.getId());
						}
					}
				}
			}
		});

//		// listener for added influences
		graph.addListener(mxEvent.CELLS_ADDED, new mxEventSource.mxIEventListener() {
//			@Override
			public void invoke(Object arg0, mxEventObject event) {
				
				// if the graph is building, ignore the creation of edges.
				if(graph.isBuilding == false){
				
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
						if(activationButton.isSelected()){
							isBio = "no"; type = "activation"; constType = GlobalConstants.ACTIVATION;
						}else if(inhibitionButton.isSelected()){
							isBio = "no"; type = "inhibition"; constType = GlobalConstants.REPRESSION;
						}else if(bioActivationButton.isSelected()){
							isBio = "yes"; type = "activation"; constType = GlobalConstants.ACTIVATION;
						}else if(bioInhibitionButton.isSelected()){
							isBio = "yes"; type = "inhibition"; constType = GlobalConstants.REPRESSION;
						}else if(noInfluenceButton.isSelected()){
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
