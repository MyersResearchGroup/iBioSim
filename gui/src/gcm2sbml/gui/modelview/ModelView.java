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
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger()){
					// rightclick on windows
					if(editable)
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
										(double)e.getX(), (double)e.getY());
								gcm2sbml.setDirty(true);
								graph.buildGraph();
								gcm2sbml.refresh();
							}
						}
					}else{
						if(editPromoterButton.isSelected()){
							// If the thing clicked on was an influence, bring up it's promotor window.
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
						}
					}
				}else if(e.getClickCount() == 2){
					// double click
					mxCell cell = (mxCell)(graphComponent.getCellAt(e.getX(), e.getY()));
					
					if (cell != null){
						System.out.println("cell="+graph.getLabel(cell) + " " + e.getClickCount());
						graph.bringUpEditorForCell(cell);
						graph.buildGraph();
						
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
				gcm2sbml.setDirty(true);
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
							gcm.getInfluences().remove(cell.getId());
							gcm.getInfluences().remove(cell.getId());
							graph.influenceRemoved(cell.getId());
						}else if(type == GlobalConstants.SPECIES){
							gcm.getSpecies().remove(cell.getId());
							gcm.getSpecies().remove(cell.getId());
							graph.speciesRemoved(cell.getId());
						}else if(type == GlobalConstants.COMPONENT){
							gcm.getComponents().remove(cell.getId());
						}
					}
					gcm2sbml.setDirty(true);
					gcm2sbml.refresh();
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
							graph.removeCells(cells);
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
									graph.removeCells(cells);
									return;
								}
							}else{
								// target is a component
								try{
									port = connectSpeciesToComponent(sourceID, targetProp);
								}catch(ListChooser.EmptyListException e){
									JOptionPane.showMessageDialog(biosim.frame(), "Sorry, this component has no input ports.");	
									graph.removeCells(cells);
									return;
								}
							}
							if(port == null){
								graph.removeCells(cells);
								return;
							}
							
							gcm2sbml.refresh();
							gcm2sbml.setDirty(true);
							graph.updateComponentInfluenceVisuals((mxCell)cells[0], port);

							graph.buildGraph();
							return;
						}
						
						// if flow gets here then we are connecting a species to another
						// species.

						
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
												
						String promoter = "default";
						String name = InfluencePanel.buildName(sourceID, targetID, type, isBio, promoter);
						
						graph.addInfluence(edge, name, constType);
						graph.buildGraph();
						gcm2sbml.refresh();
						gcm2sbml.setDirty(true);

					}
				}

			}
		});
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

	private Object[] getGCMPorts(Properties comp, String type){
		String fullPath = gcm.getPath() + File.separator + comp.getProperty("gcm");
		GCMFile compGCM = new GCMFile(gcm.getPath());
		compGCM.load(fullPath);
		HashMap<String, Properties> ports = compGCM.getPorts(type);
		return ports.keySet().toArray();
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
