package gcm.gui.schematic;

import gcm.gui.GridPanel;
import gcm.gui.DropComponentPanel;
import gcm.gui.GCM2SBMLEditor;
import gcm.gui.Grid;
import gcm.gui.InfluencePanel;
import gcm.gui.modelview.movie.MovieContainer;
import gcm.gui.modelview.movie.visualizations.cellvisualizations.MovieAppearance;
import gcm.gui.modelview.movie.visualizations.component.ComponentSchemeChooser;
import gcm.parser.GCMFile;
import gcm.parser.GCMParser;
import gcm.util.GlobalConstants;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Properties;
import java.util.prefs.Preferences;


import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SpeciesReference;

import sbmleditor.Compartments;
import sbmleditor.Reactions;
import sbol.SbolSynthesizer;
import util.Utility;

import main.Gui;


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
	private Gui biosim;
	private GCM2SBMLEditor gcm2sbml;
	private boolean editable;
	public boolean getEditable(){return editable;};
	private MovieContainer movieContainer;
	private Compartments compartments;
	private Reactions reactions;
	private JCheckBox check;
	
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
	public Schematic(GCMFile gcm, Gui biosim, GCM2SBMLEditor gcm2sbml, 
			boolean editable, MovieContainer movieContainer2,Compartments compartments, Reactions reactions){
		super(new BorderLayout());
		
		this.gcm = gcm;
		this.biosim = biosim;
		this.gcm2sbml = gcm2sbml;
		this.editable = editable;
		this.movieContainer = movieContainer2;
		this.compartments = compartments;
		this.reactions = reactions;
		
		// initialize everything on creation.
		display();
	}
	
	/**
	 * Called when the tab is clicked.
	 * @param graph
	 */
	public void display(){

		if(graph == null){
			graph = new BioGraph(gcm);
			
			addGraphListeners();
			gcm.makeUndoPoint();
		}
		
		boolean needs_layouting = graph.buildGraph();
		
		// Create and plug in the graphComponent
		if(graphComponent == null){
			
			graphComponent = new mxGraphComponent(graph);
			graphComponent.setGraph(graph);
			
			//make the mxgraph stuff see-through
			//so we can see the grid underneath the mxgraph stuff
			graphComponent.setOpaque(false);
			graphComponent.getViewport().setOpaque(false);
			
			this.add(graphComponent, BorderLayout.CENTER);
			
			//toolbar is given a name so it can be found later
			//among the components
			JToolBar toolbar = buildToolBar();
			toolbar.setName("toolbar");
						
			if(this.editable)
				this.add(toolbar, BorderLayout.NORTH);
						
			addGraphComponentListeners();
		}
	
	
		// Do layouting if it hasn't been done yet
		if(needs_layouting){
		//	graph.applyLayout("circleLayout", graphComponent);
		}
		
		//this.setBackground(Color.BLACK);
		
		drawGrid();
	}
	
	
	/**
	 * create the toolbar.
	 * @return
	 */
	AbstractButton selectButton;
	AbstractButton addSpeciesButton;
	AbstractButton addReactionButton;
	AbstractButton addComponentButton;
	AbstractButton editPromoterButton;
	AbstractButton selfInfluenceButton;
	AbstractButton gridButton;
	
	AbstractButton activationButton;
	AbstractButton inhibitionButton;
	AbstractButton bioActivationButton;
	AbstractButton reactionButton;
	AbstractButton modifierButton;
	//AbstractButton bioInhibitionButton;
	AbstractButton noInfluenceButton;
	private JToolBar buildToolBar(){

		JToolBar toolBar = new JToolBar();
		
		ButtonGroup modeButtonGroup = new ButtonGroup();
		selectButton =Utils.makeRadioToolButton("select_mode.png", "", "Select", this, modeButtonGroup); 
		toolBar.add(selectButton);
		selectButton.setSelected(true);
		addSpeciesButton = Utils.makeRadioToolButton("add_species.png", "", "Add Species", this, modeButtonGroup);
		toolBar.add(addSpeciesButton);
		addReactionButton = Utils.makeRadioToolButton("add_reaction.png", "", "Add Reactions", this, modeButtonGroup);
		toolBar.add(addReactionButton);
		addComponentButton = Utils.makeRadioToolButton("add_component.png", "", "Add Components", this, modeButtonGroup);
		toolBar.add(addComponentButton);
		editPromoterButton = Utils.makeRadioToolButton("promoter_mode.png", "", "Add Promoters", this, modeButtonGroup);
		toolBar.add(editPromoterButton);
		selfInfluenceButton = Utils.makeRadioToolButton("self_influence.png", "", "Add Self Influences", this, modeButtonGroup);
		toolBar.add(selfInfluenceButton);
		//toolBar.add(Utils.makeToolButton("", "addInfluence", "Add Influence", this));
		gridButton = Utils.makeToolButton("", "grid", "Grid", this);
		toolBar.add(gridButton);

		toolBar.addSeparator();
		ButtonGroup influenceButtonGroup = new ButtonGroup();
		
		activationButton = Utils.makeRadioToolButton("activation.png", "", "Activation", this, influenceButtonGroup);
		activationButton.setSelected(true);
		toolBar.add(activationButton);
		inhibitionButton = Utils.makeRadioToolButton("inhibition.png", "", "Repression", this, influenceButtonGroup);
		toolBar.add(inhibitionButton);
		noInfluenceButton = Utils.makeRadioToolButton("no_influence.png", "", "No Influence", this, influenceButtonGroup);
		toolBar.add(noInfluenceButton);
		bioActivationButton = Utils.makeRadioToolButton("bio_activation.png", "", "Complex Formation", this, influenceButtonGroup);
		toolBar.add(bioActivationButton);
		reactionButton = Utils.makeRadioToolButton("reaction.png", "", "Reaction", this, influenceButtonGroup);
		toolBar.add(reactionButton);
		modifierButton = Utils.makeRadioToolButton("modifier.png", "", "Modifier", this, influenceButtonGroup);
		toolBar.add(modifierButton);
		//bioInhibitionButton = Utils.makeRadioToolButton("bio_inhibition.png", "", "Create Biological Repression Influences", this, influenceButtonGroup);
		//toolBar.add(bioInhibitionButton);
		
		if (gcm.getSBMLDocument().getModel().getNumCompartments()==1) {
			toolBar.addSeparator();
			check = new JCheckBox();
			check.setActionCommand("checkCompartment");
			check.addActionListener(this);
			check.setSelected(gcm.getIsWithinCompartment());
			toolBar.add(check);
			toolBar.add(Utils.makeToolButton("", "compartment", "Compartment", this));
		}
		toolBar.addSeparator();
		toolBar.add(Utils.makeToolButton("choose_layout.png", "showLayouts", "Apply Layout", this));
		toolBar.add(Utils.makeToolButton("", "undo", "Undo", this));
		toolBar.add(Utils.makeToolButton("", "redo", "Redo", this));
		toolBar.add(Utils.makeToolButton("", "exportSBOL", "Export SBOL", this));
		
		toolBar.setFloatable(false);
		
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
			drawGrid();
		}else if(command == "redo"){
			gcm.redo();
			graph.buildGraph();
			gcm2sbml.refresh();
			gcm2sbml.setDirty(true);
			drawGrid();
		}else if(command == "compartment"){
			if (compartments != null) {
				compartments.compartEditor("OK");
			}
		}else if(command == "checkCompartment") {
			gcm.setIsWithinCompartment(check.isSelected());
		} else if (command.equals("exportSBOL")) {
			File lastFilePath;
			Preferences biosimrc = Preferences.userRoot();
			if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
				lastFilePath = null;
			}
			else {
				lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
			}
			String exportPath = Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export " + "SBOL", -1);
			if (!exportPath.equals("")) {
				biosimrc.put("biosim.general.export_dir", exportPath);
				GCMParser parser = new GCMParser(gcm, false);
				SbolSynthesizer synthesizer = parser.buildSbolSynthesizer();
				synthesizer.synthesizeDnaComponent(gcm2sbml.getSbolFiles(), exportPath);
			}
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
							gcm.createSpecies(null, e.getX(), e.getY());
							graph.buildGraph();
							gcm2sbml.refresh();
							gcm2sbml.setDirty(true);
							gcm.makeUndoPoint();
							drawGrid();
						}else if(addReactionButton.isSelected()) {
							gcm.createReaction(null, e.getX(), e.getY());
							graph.buildGraph();
							gcm2sbml.refresh();
							gcm2sbml.setDirty(true);
							gcm.makeUndoPoint();
							drawGrid();
						}else if(addComponentButton.isSelected()){
							boolean dropped = DropComponentPanel.dropComponent(gcm2sbml, gcm, e.getX(), e.getY());
							if(dropped){
								gcm2sbml.setDirty(true);
								graph.buildGraph();
								gcm2sbml.refresh();
								gcm.makeUndoPoint();
								drawGrid();
							}
						}else if(editPromoterButton.isSelected()){
							gcm.createPromoter(null, e.getX(), e.getY(), true);
							gcm2sbml.refresh();
							graph.buildGraph();
							gcm2sbml.setDirty(true);
							gcm.makeUndoPoint();
							drawGrid();
						}
					}else{
						// this would create a new promoter if an influence didn't already have one. And bring up the editor for it.
						/*if(editPromoterButton.isSelected()){
							// If the thing clicked on was an influence, bring up it's promoter window.
							if(cell.isEdge()){
								Properties prop = gcm.getInfluences().get(cell.getId());
								String type = prop.getProperty(GlobalConstants.TYPE);
								if (!type.equals(GlobalConstants.NOINFLUENCE) && !type.equals(GlobalConstants.COMPLEX)) {
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
						}else */if(selfInfluenceButton.isSelected()){
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
	
	/**
	 * Given any type of cell, bring up an editor for it if supported.
	 * @param cell
	 */
	public void bringUpEditorForCell(mxCell cell){
		String cellType = graph.getCellType(cell);
		if(cellType == GlobalConstants.SPECIES){
			gcm2sbml.launchSpeciesPanel(cell.getId(), movieContainer);
		}else if(cellType == GlobalConstants.INFLUENCE){
			gcm2sbml.launchInfluencePanel(cell.getId());
		}else if(cellType == GlobalConstants.PRODUCTION){
			// do nothing
		}else if(cellType == GlobalConstants.REACTION_EDGE){
			mxCell source = (mxCell)cell.getSource();
			mxCell target = (mxCell)cell.getTarget();
			if ((graph.getCellType(source) == GlobalConstants.SPECIES) &&
					(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				reactions.reactionsEditor(gcm.getSBMLDocument(),"OK",(String)cell.getValue(),true);
			} else if ((graph.getCellType(source) == GlobalConstants.SPECIES) &&
				(graph.getCellType(target) == GlobalConstants.REACTION)) {
				SpeciesReference reactant = gcm.getSBMLDocument().getModel().getReaction((String)target.getId()).
					getReactant((String)source.getId());
				if (reactant != null) {
					reactions.reactantsEditor(gcm.getSBMLDocument(),"OK",(String)source.getId(),reactant);
				} 
			} else if ((graph.getCellType(source) == GlobalConstants.REACTION) &&
				(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				SpeciesReference product = gcm.getSBMLDocument().getModel().getReaction((String)source.getId()).
					getProduct((String)target.getId());
				reactions.productsEditor(gcm.getSBMLDocument(),"OK",(String)target.getId(),product);
			}
		}else if(cellType == GlobalConstants.REACTION){
			Reaction r = gcm.getSBMLDocument().getModel().getReaction((String)cell.getId());
			reactions.reactionsEditor(gcm.getSBMLDocument(),"OK",(String)cell.getId(),true);
			if (!cell.getId().equals(r.getId())) {
				gcm.getReactions().put(r.getId(), gcm.getReactions().get(cell.getId()));
				gcm.getReactions().remove(cell.getId());
				cell.setId(r.getId());
			}
		}else if(cellType == GlobalConstants.COMPONENT){
			//gcm2sbml.displayChooseComponentDialog(true, null, false, cell.getId());
			if(movieContainer == null)
				gcm2sbml.launchComponentPanel(cell.getId());
			else{
				if(movieContainer.getTSDParser() == null)
					JOptionPane.showMessageDialog(Gui.frame, "You must choose a simulation file before editing component properties.");
				else
					new ComponentSchemeChooser(cell.getId(), movieContainer);
			}
		}else if(cellType.equals(GlobalConstants.PROMOTER)){
			gcm2sbml.launchPromoterPanel(cell.getId());
		}else{
			// it wasn't a type that has an editor.
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
				drawGrid();
			}
		});
	
		// Listen for everything. Put breakpoints in here when trying to figure out new events.
		graph.addListener(null, new mxEventSource.mxIEventListener() {
			
//			@Override
			public void invoke(Object arg0, mxEventObject event) {
				// see what events we have to work with.
				//biosim.log.addText((event.getName()));
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
						if(type == GlobalConstants.INFLUENCE || type == GlobalConstants.PRODUCTION){
							gcm.removeInfluence(cell.getId());
						}else if(type == GlobalConstants.REACTION_EDGE) {
							mxCell source = (mxCell)cell.getSource();
							mxCell target = (mxCell)cell.getTarget();
							if ((graph.getCellType(source) == GlobalConstants.SPECIES) &&
									(graph.getCellType(target) == GlobalConstants.SPECIES)) {
								Reaction r = gcm.getSBMLDocument().getModel().getReaction((String)cell.getValue());
								if (r.getNumReactants()==1 && r.getNumProducts()==1) {
									reactions.removeTheReaction(gcm.getSBMLDocument(),(String)cell.getValue());
								} else if (r.getNumReactants() > 1) {
									ListOf reactants = r.getListOfReactants();
									for (int i = 0; i < r.getNumReactants(); i++) {
										SpeciesReference s = (SpeciesReference)reactants.get(i);
										if (s.getSpecies().equals(source.getId())) {
											reactants.remove(i);
											break;
										}
									}
								} else if (r.getNumProducts() > 1) {
									ListOf products = r.getListOfProducts();
									for (int i = 0; i < r.getNumProducts(); i++) {
										SpeciesReference s = (SpeciesReference)products.get(i);
										if (s.getSpecies().equals(target.getId())) {
											products.remove(i);
											break;
										}
									}
								}
							} else if ((graph.getCellType(source) == GlobalConstants.SPECIES) &&
								(graph.getCellType(target) == GlobalConstants.REACTION)) {
								Reaction r = gcm.getSBMLDocument().getModel().getReaction(target.getId());
								boolean found = false;
								ListOf reactants = r.getListOfReactants();
								for (int i = 0; i < r.getNumReactants(); i++) {
									SpeciesReference s = (SpeciesReference)reactants.get(i);
									if (s.getSpecies().equals(source.getId())) {
										reactants.remove(i);
										found = true;
										break;
									}
								}
								if (!found) {
									ListOf modifiers = r.getListOfModifiers();
									for (int i = 0; i < r.getNumModifiers(); i++) {
										ModifierSpeciesReference s = (ModifierSpeciesReference)modifiers.get(i);
										if (s.getSpecies().equals(source.getId())) {
											modifiers.remove(i);
											break;
										}
									}
								}
							} else if ((graph.getCellType(source) == GlobalConstants.REACTION) &&
								(graph.getCellType(target) == GlobalConstants.SPECIES)) {
								Reaction r = gcm.getSBMLDocument().getModel().getReaction(source.getId());
								ListOf products = r.getListOfProducts();
								for (int i = 0; i < r.getNumProducts(); i++) {
									SpeciesReference s = (SpeciesReference)products.get(i);
									if (s.getSpecies().equals(target.getId())) {
										products.remove(i);
										break;
									}
								}
							}
						}else if(type == GlobalConstants.REACTION) {
							reactions.removeTheReaction(gcm.getSBMLDocument(),(String)cell.getId());
							gcm.removeReaction(cell.getId());
						}else if(type == GlobalConstants.SPECIES){
							//gcm.getSpecies().remove(cell.getId());
							//gcm.getSpecies().remove(cell.getId());
							if(gcm.speciesUsedInOtherGCM(cell.getId())){
								JOptionPane.showMessageDialog(Gui.frame, "Sorry, the species \""+cell.getId()+"\" is used in another component and cannot be removed.");
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
								JOptionPane.showMessageDialog(Gui.frame, "Sorry, you must remove the influences connected to this promoter first.");
								
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
					drawGrid();
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
			JOptionPane.showMessageDialog(Gui.frame, "You can't connect a component directly to another component. Please go through a species.");
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
			JOptionPane.showMessageDialog(Gui.frame, "You can't connect a promoter directly to another promoter.");
			//graph.removeCells(cells);
			graph.buildGraph();
			return;
		}
		
		// bail out if the user tries to connect a component to a promoter.
		if(numComponents > 0 && numPromoters > 0){
			JOptionPane.showMessageDialog(Gui.frame, "You can't connect a component directly to a promoter.");
			//graph.removeCells(cells);
			graph.buildGraph();
			return;
		}		

		
		String sourceID = source.getId();
		String targetID = target.getId();
		
		// Disallows user from connecting to a species that is an input
		if (graph.getCellType(target).equals(GlobalConstants.SPECIES)) {
			String specType = gcm.getSpecies().get(targetID).getProperty(GlobalConstants.TYPE);
			if (specType.equals(GlobalConstants.INPUT) || specType.equals(GlobalConstants.SPASTIC)) {
				JOptionPane.showMessageDialog(Gui.frame, "You can't connect to a species that is an input or constitutive.");
				graph.buildGraph();
				return;
			}
		}
		
		
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
					JOptionPane.showMessageDialog(Gui.frame, "This component has no output ports.");
					graph.buildGraph();
					return;
				}
			}else{
				// target is a component
				try{
					port = connectSpeciesToComponent(sourceID, targetProp);
				}catch(ListChooser.EmptyListException e){
					JOptionPane.showMessageDialog(Gui.frame, "This component has no input ports.");	
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
		String type;
		if(activationButton.isSelected()){
			type = InfluencePanel.types[1]; 
		}else if(inhibitionButton.isSelected()){
			type = InfluencePanel.types[0];
		}else if(bioActivationButton.isSelected()){
			type = InfluencePanel.types[3]; 
		}		
		else if(reactionButton.isSelected()){
			type = GlobalConstants.REACTION_EDGE;
		}else if(modifierButton.isSelected()){
			type = GlobalConstants.MODIFIER;
		}		
		else if(noInfluenceButton.isSelected()){
			type = InfluencePanel.types[2];
		}else{
			throw(new Error("No influence button was pressed"));
		}
		
		String name; // the species name
		Properties newInfluenceProperties = new Properties(); // the new influence
		
		// see if we need to connect a species to a promoter
		if(numPromoters == 1){
			if(graph.getCellType(source) == GlobalConstants.PROMOTER){
				// source is a promoter
				type = InfluencePanel.types[1];
				name = InfluencePanel.buildName(GlobalConstants.NONE,targetID,type,sourceID);
				newInfluenceProperties.setProperty(GlobalConstants.PROMOTER, sourceID);
			}else{
				// target is a promoter
				name = InfluencePanel.buildName(sourceID,GlobalConstants.NONE,type,targetID);
				newInfluenceProperties.setProperty(GlobalConstants.PROMOTER, targetID);
			}

		}// end connect species to promoter
		else{
			// connect two species to each other, create new implicit promoter if influence is activation or repression
			String newPromoterName = "";
			if (activationButton.isSelected() || inhibitionButton.isSelected()) {
				newPromoterName = gcm.createPromoter(null,0, 0, false);
				newInfluenceProperties.setProperty(GlobalConstants.PROMOTER, newPromoterName);
			}
			else
				newPromoterName = "none";
			name = InfluencePanel.buildName(sourceID, targetID, type, newPromoterName);
		}
		// make sure the species name is valid
		String iia = gcm.isInfluenceAllowed(name);
		if(iia != null){
			JOptionPane.showMessageDialog(Gui.frame, "The influence could not be added because " + iia);
			graph.buildGraph();
			gcm2sbml.refresh();
			return;
		}
		
		// build the influence properties
		newInfluenceProperties.setProperty(GlobalConstants.NAME, name);
		newInfluenceProperties.setProperty(GlobalConstants.TYPE, type);
		if (type == GlobalConstants.REACTION_EDGE) {
			if ((graph.getCellType(source) == GlobalConstants.SPECIES) && 
					(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				gcm.addReaction(sourceID,targetID,false);
			} else if ((graph.getCellType(source) == GlobalConstants.REACTION) && 
					(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				Reaction r = gcm.getSBMLDocument().getModel().getReaction(sourceID);
				SpeciesReference s = r.createProduct();
				s.setSpecies(targetID);
				s.setStoichiometry(1.0);
				s.setConstant(true);
			} else if ((graph.getCellType(source) == GlobalConstants.SPECIES) && 
					(graph.getCellType(target) == GlobalConstants.REACTION)) {
				Reaction r = gcm.getSBMLDocument().getModel().getReaction(targetID);
				SpeciesReference s = r.createReactant();
				s.setSpecies(sourceID);
				s.setStoichiometry(1.0);
				s.setConstant(true);
			} else {
				JOptionPane.showMessageDialog(Gui.frame, "Reaction edge can connect only species and reactions");
				graph.buildGraph();
				gcm2sbml.refresh();
				return;
			}
		} else if (type == GlobalConstants.MODIFIER) {
			if ((graph.getCellType(source) == GlobalConstants.SPECIES) && 
					(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				gcm.addReaction(sourceID,targetID,true);
			} else if ((graph.getCellType(source) == GlobalConstants.REACTION) && 
				(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				Reaction r = gcm.getSBMLDocument().getModel().getReaction(sourceID);
				ModifierSpeciesReference s = r.createModifier();
				s.setSpecies(targetID);
			} else if ((graph.getCellType(source) == GlobalConstants.SPECIES) && 
				(graph.getCellType(target) == GlobalConstants.REACTION)) {
				Reaction r = gcm.getSBMLDocument().getModel().getReaction(targetID);
				ModifierSpeciesReference s = r.createModifier();
				s.setSpecies(sourceID);
			} else {
				JOptionPane.showMessageDialog(Gui.frame, "Modifier must connect only species and reactions");
				graph.buildGraph();
				gcm2sbml.refresh();
				return;
			}
		} else {
			if ((graph.getCellType(source) == GlobalConstants.REACTION) || 
				(graph.getCellType(target) == GlobalConstants.REACTION)) {
				JOptionPane.showMessageDialog(Gui.frame, "Can only connect reactions using reaction or modifier edge");
				graph.buildGraph();
				gcm2sbml.refresh();
				return;
			}
			gcm.getInfluences().put(name, newInfluenceProperties);
		}
		
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
		
		String port = ListChooser.selectFromList(Gui.frame, portNames, 
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
		
		String port = ListChooser.selectFromList(Gui.frame, portNames, 
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
	
	/**
	 * wrapper for the paintComponent() function
	 */
	private void drawGrid() {

		Schematic.this.repaint();
	}
	
	/**
	 * overrides the paintComponent method in JComponent;
	 * is called automatically whenever the component becomes visible
	 * or when repaint is called
	 * 
	 * is used to draw the grid on the schematic
	 * 
	 * @param g Graphics object
	 * 
	 */
	public void paintComponent(Graphics g) {
				
		super.paintComponent(g);
		
		Grid grid = gcm.getGrid();
		
		if (grid.isEnabled()) {
			
			Component c[] = this.getComponents();
			
			//if there's a toolbar, adjust for it when drawing the grid
			for(Component d : c) {
				if (d.getName() == "toolbar")
					grid.setVerticalOffset(37);
			}
			
			grid.drawGrid(g);
		}
	}
	
	//////////////////////////////////////// ANIMATION TYPE STUFF ////////////////////////////////
	
	public void beginFrame(){
	//	graph.getModel().beginUpdate(); // doesn't seem needed.
	}
	
	public void setSpeciesAnimationValue(String s, MovieAppearance appearance){
		graph.setSpeciesAnimationValue(s, appearance);
	}
	public void setComponentAnimationValue(String c, MovieAppearance appearance){
		graph.setComponentAnimationValue(c, appearance);
	}
	
	public void endFrame(){
	//	graph.getModel().endUpdate();
		graph.refresh();
	}
	

}
