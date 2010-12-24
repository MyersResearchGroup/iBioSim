package gcm2sbml.gui.schematic;

import gcm2sbml.gui.GCM2SBMLEditor;
import gcm2sbml.gui.InfluencePanel;
import gcm2sbml.gui.PromoterPanel;
import gcm2sbml.gui.modelview.movie.MovieContainer;
import gcm2sbml.gui.modelview.movie.visualizations.ColorScheme;
import gcm2sbml.gui.modelview.movie.visualizations.component.ComponentSchemeChooser;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Event;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
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

public class Schematic extends JPanel implements ActionListener {
		
	private static final long serialVersionUID = 1L;
	private BioGraph graph;
	private mxGraphComponent graphComponent;
	public mxGraphComponent getGraphComponent(){return graphComponent;};	
	
	private GCMFile gcm;
	private BioSim biosim;
	private GCM2SBMLEditor gcm2sbml;
	private MovieContainer movieContainer;
	private boolean editable;
	public boolean getEditable(){return editable;};
	
	/**
	 * listener stuff. Thanks to http://www.exampledepot.com/egs/java.util/custevent.html.
	 * This makes it so that we can dispatch events that the movie container can listen for.
	 */
	protected javax.swing.event.EventListenerList listenerList =
        new javax.swing.event.EventListenerList();
	// This methods allows classes to register for MyEvents
    public void addSchematicObjectClickEventListener(SchematicObjectClickEventListener listener) {
        listenerList.add(SchematicObjectClickEventListener.class, listener);
    }
 // This methods allows classes to unregister for MyEvents
    public void removeSchematicObjectClickEventListener(SchematicObjectClickEventListener listener) {
        listenerList.remove(SchematicObjectClickEventListener.class, listener);
    }
 // This private class is used to fire MyEvents
    void dispatchSchematicObjectClickEvent(SchematicObjectClickEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==SchematicObjectClickEventListener.class) {
                ((SchematicObjectClickEventListener)listeners[i+1]).SchematicObjectClickEventOccurred(evt);
            }
        }
    }
	
	/**
	 * Constructor
	 * @param internalModel
	 */
	public Schematic(GCMFile gcm, BioSim biosim, GCM2SBMLEditor gcm2sbml, 
			boolean editable, MovieContainer movieContainer){
		super(new BorderLayout());
		
		this.gcm = gcm;
		this.biosim = biosim;
		this.gcm2sbml = gcm2sbml;
		this.editable = editable;
		this.movieContainer = movieContainer;
		
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
			gcm.makeUndoPoint();
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
		//	graph.applyLayout("circleLayout", graphComponent);
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
	//AbstractButton bioInhibitionButton;
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
		editPromoterButton = Utils.makeRadioToolButton("promoter_mode.png", "", "Add Promoter Mode", this, modeButtonGroup);
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
		bioActivationButton = Utils.makeRadioToolButton("bio_activation.png", "", "Create Complex Formation Reaction", this, influenceButtonGroup);
		toolBar.add(bioActivationButton);
		//bioInhibitionButton = Utils.makeRadioToolButton("bio_inhibition.png", "", "Create Biological Repression Influences", this, influenceButtonGroup);
		//toolBar.add(bioInhibitionButton);
		noInfluenceButton = Utils.makeRadioToolButton("no_influence.png", "", "Explicitly Set No Influences", this, influenceButtonGroup);
		toolBar.add(noInfluenceButton);
		
		toolBar.addSeparator();
		
		toolBar.add(Utils.makeToolButton("choose_layout.png", "showLayouts", "Apply Layout", this));
		
		toolBar.add(Utils.makeToolButton("", "undo", "Undo", this));
		toolBar.add(Utils.makeToolButton("", "redo", "Redo", this));
		
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
			graph.buildGraph(); // rebuild, quick way to clear out any edge midpoints.
			gcm2sbml.setDirty(true);
			gcm.makeUndoPoint();
		}else if(command == "undo"){
			gcm.undo();
			graph.buildGraph();
			gcm2sbml.refresh();
			gcm2sbml.setDirty(true);
		}else if(command == "redo"){
			gcm.redo();
			graph.buildGraph();
			gcm2sbml.refresh();
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
		
		final Schematic self = this;
		
		// Add a listener for when cells get clicked on.
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter(){
			@Override
			public void mouseReleased(MouseEvent e)
			{
				
				mxCell cell = (mxCell)(graphComponent.getCellAt(e.getX(), e.getY()));
				if(cell != null){
					// dispatch a new event in case the movie container is listening for it
					Event evt = new Event(this, 1000, null);
					Properties prop = graph.getCellProperties(cell);
					if(prop != null){
						SchematicObjectClickEvent soce = new SchematicObjectClickEvent(evt, prop, graph.getCellType(cell));
						self.dispatchSchematicObjectClickEvent(soce);
					}
				}
				
				if (e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON3){
					// rightclick on windows
					if (editable)
						showGraphPopupMenu(e);
				}else if(e.getClickCount() == 1 && editable && e.getButton() == MouseEvent.BUTTON1){
					// single click.
					// First check and if the user clicked on a component, let the graph lib take care of it.
					if(cell == null){
						// If control gets here, the user clicked once, and not on any component.
						if(selectButton.isSelected()){
							// do the default graph lib behavior
						}else if(addSpeciesButton.isSelected()){
							// plop a species down with good default info at the mouse coordinates
							graph.createSpecies(null, e.getX(), e.getY());
							gcm2sbml.refresh();
							gcm2sbml.setDirty(true);
							gcm.makeUndoPoint();
						}else if(addComponentButton.isSelected()){
							// Ask the user which component to add, then plop it down where the click happened
//							String comp = (String) JOptionPane.showInputDialog(BioSim.frame,
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
								gcm.makeUndoPoint();
							}
						}else if(editPromoterButton.isSelected()){
							graph.createPromoter(null, e.getX(), e.getY());
							gcm2sbml.refresh();
							gcm2sbml.setDirty(true);
							gcm.makeUndoPoint();
						}
					}else{
						if(editPromoterButton.isSelected()){
							// If the thing clicked on was an influence, bring up it's promoter window.
							if(cell.isEdge()){
								Properties prop = gcm.getInfluences().get(cell.getId());
								String type = prop.getProperty(GlobalConstants.TYPE);
								if (!type.equals(GlobalConstants.NOINFLUENCE)) {
									String promoter = prop.getProperty(GlobalConstants.PROMOTER);
									//System.out.print(promoter);
									if(promoter != null){
										gcm2sbml.launchPromoterPanel(promoter);
									}else{
										PromoterPanel p = gcm2sbml.launchPromoterPanel(null);
										// set the selected influence to use the given promoter
										prop = gcm.getInfluences().get(cell.getId());
										// now rename the influence to incorporate the new promoter
										if (p.getLastUsedPromoter()!=null) {
											gcm.changeInfluencePromoter(cell.getId(), prop.getProperty(GlobalConstants.PROMOTER), p.getLastUsedPromoter());
										}
									}
									graph.buildGraph();
									gcm2sbml.refresh();
									// no need to set dirty bit because the property window will do it for us.
									gcm.makeUndoPoint();
								}
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
				}else if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1){
					// double click
					if (cell != null){
						bringUpEditorForCell(cell);
						graph.buildGraph();
						gcm.makeUndoPoint();
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
				if (e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON3){
					// rightclick on mac
					if (editable)
						showGraphPopupMenu(e);
					//editable = false;
				}
			}
		});
	}
	
	
	public void bringUpEditorForCell(mxCell cell){
		if(graph.getCellType(cell) == GlobalConstants.SPECIES){
			ColorScheme scheme = null;
			if(movieContainer != null)
				scheme = movieContainer.getMoviePreferences().getColorSchemeForSpecies(cell.getId());
			gcm2sbml.launchSpeciesPanel(cell.getId(), scheme);
		}else if(graph.getCellType(cell) == GlobalConstants.INFLUENCE){
			// if an edge, make sure it isn't connected
			// to a component - which aren't really influences at all.
			if(	graph.getCellType(cell.getSource()) == GlobalConstants.SPECIES &&
					graph.getCellType(cell.getTarget()) == GlobalConstants.SPECIES)
			gcm2sbml.launchInfluencePanel(cell.getId());
		}else if(graph.getCellType(cell) == GlobalConstants.COMPONENT){
			//gcm2sbml.displayChooseComponentDialog(true, null, false, cell.getId());
			if(movieContainer == null)
				gcm2sbml.launchComponentPanel(cell.getId());
			else{
				if(movieContainer.getTSDParser() == null)
					JOptionPane.showMessageDialog(BioSim.frame, "Sorry, you must choose a simulation file before editing component properties.");
				else
					new ComponentSchemeChooser(cell.getId(), movieContainer);
			}
		}
		// refresh everything.
		graph.buildGraph();
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
					// TODO: Disallow moving edges around.
					mxCell cell = (mxCell)cells[i];
					if(cell.isEdge()){ // If an edge gets moved ignore it then rebuild the graph from the model.
						biosim.log.addText("Sorry, edges cann't be moved independently.");
					}else{
						graph.updateInternalPosition(cell);
					}
				}
				graph.buildGraph();
				gcm.makeUndoPoint();
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
					// sort the cells so that edges are first. This makes them
					// get deleted before anything they are connected to.
					Arrays.sort(cells, 0, cells.length, new Comparator<Object>() {
						public int compare(Object a, Object b){
							boolean av = ((mxCell)a).isEdge();
							boolean bv = ((mxCell)b).isEdge();
							if(av && !bv) return -1; // a is edge, b isn't
							if(!av && bv) return 1; // b is edge, a isn't
							return 0; // both are the same
						}
					});
					
					for(Object ocell:cells){
						mxCell cell = (mxCell)ocell;
						//System.out.print(cell.getId() + " Deleting.\n");
						
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
								JOptionPane.showMessageDialog(BioSim.frame, "Sorry, the species \""+cell.getId()+"\" is used in another component and cannot be removed.");
								continue;
							}
							gcm.removeSpeciesAndAssociations(cell.getId());
							//gcm.removeSpecies(cell.getId());
							//graph.speciesRemoved(cell.getId());
							//graph.buildGraph();
						}else if(type == GlobalConstants.COMPONENT){
							gcm.getComponents().remove(cell.getId());
						}else if(type == GlobalConstants.PROMOTER){
							if(gcm.removePromoterCheck(cell.getId()))
								gcm.removePromoter(cell.getId());
							else // this should actually never happen because the edges get deleted before the promoters.
								JOptionPane.showMessageDialog(BioSim.frame, "Sorry, you must remove the influences connected to this promoter first.");
								
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
					gcm.makeUndoPoint();
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
		
		// make sure there is at most 1 component
		int numComponents = 0;
		if(graph.getCellType(source)==GlobalConstants.COMPONENT)
			numComponents++;
		if(graph.getCellType(target)==GlobalConstants.COMPONENT)
			numComponents++;
		// bail out if the user tries to connect two components.
		if(numComponents == 2){
			JOptionPane.showMessageDialog(BioSim.frame, "Sorry, you can't connect a component directly to another component. Please go through a species.");
			//graph.removeCells(cells);
			graph.buildGraph();
			return;
		}

		// make sure there is at most 1 promoter
		int numPromoters = 0;
		if(graph.getCellType(source)==GlobalConstants.PROMOTER)
			numPromoters++;
		if(graph.getCellType(target)==GlobalConstants.PROMOTER)
			numPromoters++;
		// bail out if the user tries to connect two components.
		if(numPromoters == 2){
			JOptionPane.showMessageDialog(BioSim.frame, "Sorry, you can't connect a promoter directly to another promoter.");
			//graph.removeCells(cells);
			graph.buildGraph();
			return;
		}
		
		// bail out if the user tries to connect a component to a promoter.
		if(numComponents > 0 && numPromoters > 0){
			JOptionPane.showMessageDialog(BioSim.frame, "Sorry, you can't connect a component directly to a promoter.");
			//graph.removeCells(cells);
			graph.buildGraph();
			return;
		}		

		
		String sourceID = source.getId();
		String targetID = target.getId();
		
		// see if we are connecting a component to a species
		if(numComponents == 1){
			Properties sourceProp = graph.getCellProperties(source);
			Properties targetProp = graph.getCellProperties(target);
			String port = null;
			if(graph.getCellType(source) == GlobalConstants.COMPONENT){
				// source is a component
				try{
					port = connectComponentToSpecies(sourceProp, targetID);
				}catch(ListChooser.EmptyListException e){
					JOptionPane.showMessageDialog(BioSim.frame, "Sorry, this component has no output ports.");
					graph.buildGraph();
					return;
				}
			}else{
				// target is a component
				try{
					port = connectSpeciesToComponent(sourceID, targetProp);
				}catch(ListChooser.EmptyListException e){
					JOptionPane.showMessageDialog(BioSim.frame, "Sorry, this component has no input ports.");	
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
			gcm.makeUndoPoint();
			return;
		} // end connect species to component
		
		// Calculate some parameters that will be needed to build the
		// influence we will need.
		String isBio;
		String type;
		String constType;
		if(activationButton.isSelected()){
			isBio = InfluencePanel.bio[0]; type = InfluencePanel.types[1]; constType = GlobalConstants.ACTIVATION;
		}else if(inhibitionButton.isSelected()){
			isBio = InfluencePanel.bio[0]; type = InfluencePanel.types[0]; constType = GlobalConstants.REPRESSION;
		}else if(bioActivationButton.isSelected()){
			isBio = "complex"; /*InfluencePanel.bio[1];*/ type = InfluencePanel.types[1]; constType = GlobalConstants.ACTIVATION;
		//}else if(bioInhibitionButton.isSelected()){
		//	isBio = InfluencePanel.bio[1]; type = InfluencePanel.types[0]; constType = GlobalConstants.REPRESSION;
		}else if(noInfluenceButton.isSelected()){
			isBio = ""; type = InfluencePanel.types[2]; constType = GlobalConstants.NOINFLUENCE;
		}else{
			throw(new Error("No influence button was pressed!"));
		}
		
		String name; // the species name
		Properties newInfluenceProperties = new Properties(); // the new influence
		
		// see if  we need to connect a species to a promoter
		if(numPromoters == 1){
			if(graph.getCellType(source) == GlobalConstants.PROMOTER){
				// source is a promoter
				name = InfluencePanel.buildName(
						GlobalConstants.NONE, 
						targetID, 
						type, 
						isBio, 
						sourceID);
				newInfluenceProperties.setProperty(GlobalConstants.PROMOTER, sourceID);
			}else{
				// target is a promoter
				name = InfluencePanel.buildName(
						sourceID, 
						GlobalConstants.NONE, 
						type, 
						isBio, 
						targetID);
				newInfluenceProperties.setProperty(GlobalConstants.PROMOTER, targetID);
			}

		}// end connect species to promoter
		else{
			// connect two species to each other
			name = InfluencePanel.buildName(sourceID, targetID, type, isBio, "default");
		}
		// make sure the species name is valid
		String iia = gcm.isInfluenceAllowed(name);
		if(iia != null){
			JOptionPane.showMessageDialog(BioSim.frame, "Sorry, the influence could not be added because " + iia);
			graph.buildGraph();
			gcm2sbml.refresh();
			return;
		}
		
		// build the influence properties
		newInfluenceProperties.setProperty(GlobalConstants.NAME, name);
		if (isBio == "complex") {
			newInfluenceProperties.setProperty(GlobalConstants.TYPE, GlobalConstants.COMPLEX);
		} else {
			newInfluenceProperties.setProperty(GlobalConstants.TYPE, constType);
		}
		gcm.getInfluences().put(name, newInfluenceProperties);
		
		
		graph.buildGraph();
		gcm2sbml.refresh();
		gcm2sbml.setDirty(true);
		gcm.makeUndoPoint();
	}
	
	/**
	 * connects an output in a component to a species.
	 * @param comp_id
	 * @param spec_id
	 * @return: A boolean representing success or failure. True means it worked, false, means there was no output in the component.
	 */
	public String connectComponentToSpecies(Properties comp, String specID) throws ListChooser.EmptyListException{

		Object[] portNames = getGCMPorts(comp, GlobalConstants.OUTPUT);
		
		String port = ListChooser.selectFromList(BioSim.frame, portNames, 
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
		
		String port = ListChooser.selectFromList(BioSim.frame, portNames, 
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
		EditorPopupMenu menu = new EditorPopupMenu(Schematic.this);
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
	
	public void setSpeciesAnimationValue(String s, Color color){
		graph.setSpeciesAnimationValue(s, color);
	}
	
	public void endFrame(){
	//	graph.getModel().endUpdate();
		graph.refresh();
	}
	

}
