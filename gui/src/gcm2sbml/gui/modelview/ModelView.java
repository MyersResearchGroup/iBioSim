package gcm2sbml.gui.modelview;

import gcm2sbml.gui.GCM2SBMLEditor;
import gcm2sbml.gui.InfluencePanel;
import gcm2sbml.gui.PromoterPanel;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Properties;


import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import biomodelsim.BioSim;

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
	
	private GCMFile gcm;
	private BioSim biosim;
	private GCM2SBMLEditor gcm2sbml;
	private boolean editable;
	
	/**
	 * Constructor
	 * @param internalModel
	 */
	public ModelView(GCMFile gcm, BioSim biosim, GCM2SBMLEditor gcm2sbml, boolean editable){
		super(new BorderLayout());
		
		this.gcm = gcm;
		this.biosim = biosim;
		this.gcm2sbml = gcm2sbml;
		this.editable = editable;
		
		// initialize everything on creation.
		display();
	}
	
	/**
	 * Called when the tab is clicked.
	 * @param graph
	 */
	public void display(){

		if(graph == null){
			graph = new BioGraph(gcm, gcm2sbml);
			
			addGraphListeners();
		}
		
		boolean needs_layouting = graph.buildGraph();
		
		// Create and plug in the graphComponent
		if(graphComponent == null){
			graphComponent = new mxGraphComponent(graph);
			graphComponent.setGraph(graph);
			this.add(graphComponent, BorderLayout.CENTER);
			if(this.editable)
				this.add(buildToolBar(), BorderLayout.NORTH);
			addGraphComponentListeners();
		}
	
	
		// Do layouting if it hasn't been done yet
		if(needs_layouting){
			graph.applyLayout("circleLayout", graphComponent);
		}
		
	}
	
	
	/**
	 * create the toolbar.
	 * @return
	 */
	AbstractButton selectButton;
	AbstractButton addSpeciesButton;
	AbstractButton addComponentButton;
	AbstractButton editPromoterButton;
	AbstractButton selfInfluenceButton;
	
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
		selfInfluenceButton = Utils.makeRadioToolButton("self_influence.png", "", "Create Self Influences", this, modeButtonGroup);
		toolBar.add(selfInfluenceButton);
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
			gcm2sbml.setDirty(true);
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
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger()){
					// rightclick on windows
					if(editable)
						showGraphPopupMenu(e);
				}else if(e.getClickCount() == 1 && editable){
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
							gcm2sbml.refresh();
							gcm2sbml.setDirty(true);
						}else if(addComponentButton.isSelected()){
							// Ask the user which component to add, then plop it down where the click happened
//							String comp = (String) JOptionPane.showInputDialog(biosim.frame(),
//									"Choose a gcm to use as a component:", "Component Editor",
//									JOptionPane.PLAIN_MESSAGE, null, 
//									gcm2sbml.getAllPossibleComponentNames().toArray(), null);
							String createdID = gcm2sbml.displayChooseComponentDialog(false, null, true);
							if(createdID != null){
								graph.centerVertexOverPoint(gcm.getComponents().get(createdID), 
										e.getX(), e.getY());
								gcm2sbml.setDirty(true);
								graph.buildGraph();
								gcm2sbml.refresh();
							}
						}
					}else{
						if(editPromoterButton.isSelected()){
							// If the thing clicked on was an influence, bring up it's promoter window.
							if(cell.isEdge()){
								Properties prop = gcm.getInfluences().get(cell.getId());
								String promoter = prop.getProperty("Promoter");
								//System.out.print(promoter);
								if(promoter != null){
									gcm2sbml.launchPromoterPanel(promoter);
								}else{
									PromoterPanel p = gcm2sbml.launchPromoterPanel(null);
									// set the selected influence to use the given promoter
									prop = gcm.getInfluences().get(cell.getId());
									// now rename the influence to incorporate the new promoter
									gcm.changeInfluencePromoter(cell.getId(), prop.getProperty(GlobalConstants.PROMOTER), p.getLastUsedPromoter());
								}
								graph.buildGraph();
								gcm2sbml.refresh();
							}
						}else if(selfInfluenceButton.isSelected()){
							if(cell.isEdge() == false){
								// the user clicked to add a self-influence to a component.
								//Object parent, String id, Object value, Object source, Object target, String style
								mxCell edge = new mxCell();
								edge.setEdge(true);
								edge.setSource(cell);
								edge.setTarget(cell);
								tryAddAssociationBetweenCells(edge);
								
							}
						}
					}
				}else if(e.getClickCount() == 2){
					// double click
					mxCell cell = (mxCell)(graphComponent.getCellAt(e.getX(), e.getY()));
					
					if (cell != null){
						graph.bringUpEditorForCell(cell);
						graph.buildGraph();
						
					}
				}
			}
		});
		
		// Add a special listener for the MAC. Rightclick only works on mousedown,
		// while on windows it only works on mouseup.
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter(){
			@Override
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
				boolean edgeMoved = false;
				Object cells[] = (Object [])event.getProperties().get("cells");
				for(int i=0; i<cells.length; i++){
					// TODO: Disallow moving edges around.
					mxCell cell = (mxCell)cells[i];
					if(cell.isEdge()){ // If an edge gets moved ignore it then rebuild the graph from the model.
						edgeMoved = true;
						biosim.log.addText("Sorry, edges cann't be moved independently.");
					}else{
						graph.updateInternalPosition(cell);
					}
				}
				if(edgeMoved)
					graph.buildGraph();
				gcm2sbml.setDirty(true);
			}
		});
	
		// Listen for deleted cells
		graph.addListener(null, new mxEventSource.mxIEventListener() {
			
//			@Override
			public void invoke(Object arg0, mxEventObject event) {
				// see what events we have to work with.
				//biosim.log.addText((event.getName()));
			}
		});

		// TODO: Figure out how to tell when the user has moved an edge from one 
		// cell to another. Then either undo the change (using a rebuild?) or 
		// properly detach and re-attach the edge (tricky considering componentConnections)
		graph.addListener(mxEvent.CELL_CONNECTED, new mxEventSource.mxIEventListener() {
			public void invoke(Object arg0, mxEventObject event) {
				// see what events we have to work with.
				//biosim.log.addText((event.getName()));
				if(graph.isBuilding == false && event.getProperties().get("source").equals(false)){
					int a=1+1; // I needed somewhere to stick a breakpoint
					int b=a+a;
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
						
						String type = graph.getCellType(cell);
						if(type == GlobalConstants.INFLUENCE){
							//gcm.getInfluences().remove(cell.getId()); // why am I doing this twice? Was this a CVS glitch, is this really intentional, or was I up too late when I wrote it? TODO: get rid of this or put in a comment explaining whats up.
							//gcm.getInfluences().remove(cell.getId());
							gcm.removeInfluence(cell.getId());
							//graph.influenceRemoved(cell.getId());
						}else if(type == GlobalConstants.SPECIES){
							//gcm.getSpecies().remove(cell.getId());
							//gcm.getSpecies().remove(cell.getId());
							if(gcm.speciesUsedInOtherGCM(cell.getId())){
								JOptionPane.showMessageDialog(biosim.frame(), "Sorry, the species \""+cell.getId()+"\" is used in another component and cannot be removed.");
								continue;
							}
							gcm.removeSpeciesAndAssociations(cell.getId());
							//gcm.removeSpecies(cell.getId());
							//graph.speciesRemoved(cell.getId());
							//graph.buildGraph();
						}else if(type == GlobalConstants.COMPONENT){
							gcm.getComponents().remove(cell.getId());
						}else if(type == GlobalConstants.COMPONENT_CONNECTION){
							removeComponentConnection(cell);
						}else if(type == graph.CELL_NOT_FULLY_CONNECTED){
							// do nothing. This can happen if the user deletes a species that is connected
							// to influences or connections. The influences or connections will be 
							// removed, but then the graph library will still fire an event to remove
							// the now defunct edge. In that case this branch will be called and should
							// do nothing.
						}
					}
					gcm2sbml.setDirty(true);
					gcm2sbml.refresh();
					graph.buildGraph();
				}
			}
		});

//		// listener for added influences
		graph.addListener(mxEvent.CELLS_ADDED, new mxEventSource.mxIEventListener() {
//			@Override
			public void invoke(Object arg0, mxEventObject event) {
				
				// if the graph is building, ignore the creation of edges.
				if(graph.isBuilding == false){
					
					// if the user tries to add anything in simulation mode, stop them.
					if(editable == false){
						graph.buildGraph();
						return;
					}
				
					Object cells[] = (Object [])event.getProperties().get("cells");
					
					if(cells.length == 1 && ((mxCell)(cells[0])).isEdge()){
	
						mxCell edge = (mxCell)(cells[0]);
						
						// make sure there is a target cell. If there isn't it is because
						// the user dragged an edge and let it go when it wasn't connected
						// to anything. Remove it and return if this is the case.
						if(edge.getTarget() == null){
							//graph.removeCells(cells);
							graph.buildGraph();
							return;
						}
						
						tryAddAssociationBetweenCells(edge);
					
					}
				}

			}
		});
	}
	
	/**
	 * Tries to properly connect an edge that is connected in the graph. Called 
	 * right after the graph library adds an edge, this function will remove
	 * that edge if it cannot be connected properly (for instance, if it is
	 * connected between two components).
	 * @param edge
	 */
	private void tryAddAssociationBetweenCells(mxCell edge){
		// figure out if we need to connect to a component
		mxCell source = (mxCell)edge.getSource();
		mxCell target = (mxCell)edge.getTarget();
		//string source = edge.getSource().getValue();
		int numComponents = 0;
		if(graph.getCellType(source)==GlobalConstants.COMPONENT)
			numComponents++;
		if(graph.getCellType(target)==GlobalConstants.COMPONENT)
			numComponents++;
		// bail out if the user tries to connect two components.
		if(numComponents == 2){
			JOptionPane.showMessageDialog(biosim.frame(), "Sorry, you can't connect a component directly to another component. Please go through a species.");
			//graph.removeCells(cells);
			graph.buildGraph();
			return;
		}

		
		String sourceID = source.getId();
		String targetID = target.getId();
		
		if(numComponents == 1){
			Properties sourceProp = graph.getCellProperties(source);
			Properties targetProp = graph.getCellProperties(target);
			String port = null;
			if(graph.getCellType(source) == GlobalConstants.COMPONENT){
				// source is a component
				try{
					port = connectComponentToSpecies(sourceProp, targetID);
				}catch(ListChooser.EmptyListException e){
					JOptionPane.showMessageDialog(biosim.frame(), "Sorry, this component has no output ports.");
					graph.buildGraph();
					return;
				}
			}else{
				// target is a component
				try{
					port = connectSpeciesToComponent(sourceID, targetProp);
				}catch(ListChooser.EmptyListException e){
					JOptionPane.showMessageDialog(biosim.frame(), "Sorry, this component has no input ports.");	
					//graph.removeCells(cells);
					// rebuild the graph to get rid of the edge that was created.
					// Better then explicitly removing the edge because that will
					// call an event to disconnect the edge and throw an exception 
					// due to the edge not really being connected.
					graph.buildGraph();
					return;
				}
			}
			if(port == null){
				graph.buildGraph();
				return;
			}
			
			gcm2sbml.refresh();
			gcm2sbml.setDirty(true);
			graph.updateComponentConnectionVisuals(edge, port);

			graph.buildGraph();
			return;
		}
		
		// if flow gets here then we are connecting a species to another
		// species.

		
		String isBio;
		String type;
		String constType;
		if(activationButton.isSelected()){
			isBio = "no"; type = InfluencePanel.types[1]; constType = GlobalConstants.ACTIVATION;
		}else if(inhibitionButton.isSelected()){
			isBio = "no"; type = InfluencePanel.types[0]; constType = GlobalConstants.REPRESSION;
		}else if(bioActivationButton.isSelected()){
			isBio = "yes"; type = InfluencePanel.types[1]; constType = GlobalConstants.ACTIVATION;
		}else if(bioInhibitionButton.isSelected()){
			isBio = "yes"; type = InfluencePanel.types[0]; constType = GlobalConstants.REPRESSION;
		}else if(noInfluenceButton.isSelected()){
			isBio = ""; type = InfluencePanel.types[2]; constType = GlobalConstants.NOINFLUENCE;
		}else{
			throw(new Error("No influence button was pressed!"));
		}
								
		String promoter = "default";
		String name = InfluencePanel.buildName(sourceID, targetID, type, isBio, promoter);
		
		graph.addInfluence(edge, name, constType);
		graph.buildGraph();
		gcm2sbml.refresh();
		gcm2sbml.setDirty(true);
	}
	
	/**
	 * connects an output in a component to a species.
	 * @param comp_id
	 * @param spec_id
	 * @return: A boolean representing success or failure. True means it worked, false, means there was no output in the component.
	 */
	public String connectComponentToSpecies(Properties comp, String specID) throws ListChooser.EmptyListException{

		Object[] portNames = getGCMPorts(comp, GlobalConstants.OUTPUT);
		
		String port = ListChooser.selectFromList(biosim.frame(), portNames, 
				"Please Choose an Output Port");
		
		if(port == null)
			return null;
		gcm.connectComponentAndSpecies(comp, port, specID, "Output");
		return port;
	}
	
	/**
	 * connects a species to the input of a component.
	 * @param spec_id
	 * @param comp_id
	 * @return a boolean representing success or failure.
	 */
	public String connectSpeciesToComponent(String specID, Properties comp) throws ListChooser.EmptyListException{

		Object[] portNames = getGCMPorts(comp, GlobalConstants.INPUT);
		
		String port = ListChooser.selectFromList(biosim.frame(), portNames, 
				"Please Choose an Input Port");
		if(port == null)
			return null;
		gcm.connectComponentAndSpecies(comp, port, specID, "Input");
		return port;
	}

	// TODO: This should probably be moved to GCMFile.java, maybe as a static method?
	private Object[] getGCMPorts(Properties comp, String type){
		String fullPath = gcm.getPath() + File.separator + comp.getProperty("gcm");
		GCMFile compGCM = new GCMFile(gcm.getPath());
		compGCM.load(fullPath);
		HashMap<String, Properties> ports = compGCM.getPorts(type);
		return ports.keySet().toArray();
	}
	
	/**
	 * given an mxCell where either the source or target is a component, 
	 * remove the connection.
	 */
	private void removeComponentConnection(mxCell cell){
		Properties comp;
		String speciesId;
		if(graph.getCellType(cell.getTarget()) == GlobalConstants.COMPONENT){
			comp = graph.getCellProperties(cell.getTarget());
			speciesId = cell.getSource().getId();
		}else if(graph.getCellType(cell.getSource()) == GlobalConstants.COMPONENT){
			comp = graph.getCellProperties(cell.getSource());
			speciesId = cell.getTarget().getId();
		}else
			throw new Error("removeComponentConnection was called with a cell in which neither the source nor target was a component!");
		
		if(gcm.checkDisconnectComponentAndSpecies(comp, speciesId, true) == false)
			throw new Error("Species was not connected to the given component!");
	}
	
	////// Copied from mxGraph example file BasicGraphEditor.java
	
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
	
	
	//////////////////////////////////////// ANIMATION TYPE STUFF ////////////////////////////////
	
	public void beginFrame(){
	//	graph.getModel().beginUpdate(); // doesn't seem needed.
	}
	
	public void setSpeciesAnimationValue(String s, double value){
		graph.setSpeciesAnimationValue(s, value);
	}
	
	public void endFrame(){
	//	graph.getModel().endUpdate();
		graph.refresh();
	}
	

}
