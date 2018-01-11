/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.gui.modelEditor.schematic;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.Trigger;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.util.mxRectangle;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.PanelObservable;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.ResourceManager;
import edu.utah.ece.async.ibiosim.gui.modelEditor.comp.DropComponentPanel;
import edu.utah.ece.async.ibiosim.gui.modelEditor.comp.Grid;
import edu.utah.ece.async.ibiosim.gui.modelEditor.comp.GridPanel;
import edu.utah.ece.async.ibiosim.gui.modelEditor.movie.MovieContainer;
import edu.utah.ece.async.ibiosim.gui.modelEditor.movie.SchemeChooserPanel;
import edu.utah.ece.async.ibiosim.gui.modelEditor.sbmlcore.Compartments;
import edu.utah.ece.async.ibiosim.gui.modelEditor.sbmlcore.Constraints;
import edu.utah.ece.async.ibiosim.gui.modelEditor.sbmlcore.Events;
import edu.utah.ece.async.ibiosim.gui.modelEditor.sbmlcore.ModelPanel;
import edu.utah.ece.async.ibiosim.gui.modelEditor.sbmlcore.Parameters;
import edu.utah.ece.async.ibiosim.gui.modelEditor.sbmlcore.Reactions;
import edu.utah.ece.async.ibiosim.gui.modelEditor.sbmlcore.Rules;
import edu.utah.ece.async.ibiosim.gui.modelEditor.sbmlcore.SpeciesPanel;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Schematic extends PanelObservable implements ActionListener {
		
	//CLASS VARIABLES
	
	private static final long serialVersionUID = 1L;
	private BioGraph graph;
	private mxGraphComponent graphComponent;
	private Rubberband rubberband;
	
	private BioModel bioModel;
	private Gui biosim;
	private ModelEditor modelEditor;
	private boolean editable;
	private MovieContainer movieContainer;
	private Compartments compartments;
	private Reactions reactions;
	private Rules rules;
	private Constraints constraints;
	private Parameters parameters;
	private Events events;
	//private JComboBox compartmentList;
	private Grid grid;
	private JTabbedPane tabbedPane;
	
	//toolbar buttons
	private AbstractButton selectButton;
	private AbstractButton addCompartmentButton;
	private AbstractButton addSpeciesButton;
	private AbstractButton addReactionButton;
	private AbstractButton addComponentButton;
	private AbstractButton addPromoterButton;
	private AbstractButton addBooleanButton;
	private AbstractButton addVariableButton;
	private AbstractButton addPlaceButton;
	private AbstractButton addTransitionButton;
	private AbstractButton addRuleButton;
	private AbstractButton addConstraintButton;
	private AbstractButton addEventButton;
	private AbstractButton selfInfluenceButton;
	private AbstractButton activationButton;
	private AbstractButton inhibitionButton;
	private AbstractButton bioActivationButton;
	private AbstractButton reactionButton;
	private AbstractButton modifierButton;
	private AbstractButton noInfluenceButton;
	private AbstractButton zoomButton;
	private AbstractButton panButton;
	
	private JPopupMenu layoutPopup;
	
	private boolean movieMode = false;
	
	private boolean lema = false;
	
	private ModelPanel modelPanel;
	
	//CLASS METHODS
	
    /**
	 * Constructor
	 * @param internalModel
	 */
	public Schematic(BioModel gcm, Gui biosim, ModelEditor modelEditor, boolean editable, 
			MovieContainer movieContainer2, Compartments compartments, Reactions reactions, Rules rules,
			Constraints constraints, Events events, Parameters parameters, boolean lema) {
		
		super(new BorderLayout());
		
		//sets how much of the cell, when dragged, results in moving vs. edge creation
		mxConstants.DEFAULT_HOTSPOT = 0.5;
		mxGraphTransferable.enableImageSupport = false;
		
		this.bioModel = gcm;
		this.biosim = biosim;
		this.modelEditor = modelEditor;
		this.editable = editable;
		this.movieContainer = movieContainer2;
		this.compartments = compartments;
		this.reactions = reactions;
		this.rules = rules;
		this.constraints = constraints;
		this.parameters = parameters;
		this.events = events;
		this.grid = modelEditor.getGrid();
		this.lema = lema;
		//this.compartmentList = compartmentList;
		this.tabbedPane = biosim.getTab();
		
		// initialize everything on creation.
		display();
		modelEditor.setDirty(false);
	}
	
	/**
	 * displays the graph
	 * called when the user opens the gcm file
	 * or when the user creates a grid
	 */
	public void display() {

		if (graph == null) {
			
			graph = new BioGraph(bioModel,lema,editable,modelEditor);	
			graph.setResetEdgesOnMove(false);
			addGraphListeners();
			modelEditor.makeUndoPoint();
		}
		
		graph.buildGraph();
		
		// Create and plug in the graphComponent
		if (graphComponent == null) {
			
			graphComponent = new mxGraphComponent(graph) {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public void paint(Graphics g) {
					
					if (movieMode == true) 
						return;
					super.paint(g);
				}

				@Override
			   /**
			    * enables panning
			    */
				public boolean isPanningEvent(MouseEvent event) {
				   
					if ((panButton != null && (panButton.isSelected())) || event.isAltDown() || event.isControlDown()) {
						
						rubberband.setEnabled(false);
						graphComponent.getPanningHandler().setEnabled(true);
						return true;
					}
					graphComponent.getPanningHandler().setEnabled(false);
					rubberband.setEnabled(true);
					return false;
				}
			};
			
			graphComponent.setGraph(graph);
			graphComponent.getVerticalScrollBar().setUnitIncrement(50);
			graphComponent.getPanningHandler().setEnabled(false);
			
			//make the mxgraph stuff see-through
			//so we can see the grid underneath the mxgraph stuff
			graphComponent.setOpaque(false);
			graphComponent.getViewport().setOpaque(false);
			
			rubberband = new Rubberband(graphComponent);
			rubberband.setEnabled(true);
			
			this.add(graphComponent, BorderLayout.CENTER);
						
			addGraphComponentListeners();
		}

		JToolBar toolbar = new JToolBar();

		//if the grid is enabled, change the toolbar
		if (bioModel.isGridEnabled()) {
		  grid.createGrid("none");
		  grid.syncGridGraph(graph);

			//remove the previous toolbar (and add back the graph)
			this.removeAll();
			this.add(graphComponent, BorderLayout.CENTER);

			toolbar = buildGridToolbar();

			//if we're in a non-analysis schematic
			if(this.editable) {

				this.add(toolbar, BorderLayout.NORTH);

				toolbar.repaint();
				toolbar.validate();		
				this.validate();
			}

			drawGrid();
			graph.setCellsResizable(false);
		}
		//if there's no grid, draw the usual toolbar
		else {
			if (lema) {
				toolbar = buildLEMAToolBar();
			} else {
				toolbar = buildToolBar();
			}
			
			//if we're in a non-analysis schematic
			if(this.editable)
				this.add(toolbar, BorderLayout.NORTH);
			else {
				graph.setCellsResizable(false);
				graph.setCellsMovable(false);
			}
		}
		//this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S,0),"AddSpecies");
		//this.getActionMap().put("AddSpecies",new AddSpeciesAction());
	}
	
	/*
	class AddSpeciesAction extends AbstractAction {
	    public AddSpeciesAction() {
	        super(null, null);
	        //putValue(SHORT_DESCRIPTION, desc);
	        //putValue(MNEMONIC_KEY, mnemonic);
	    }
	    public void actionPerformed(ActionEvent e) {
	        System.out.println("Action for first button/menu item");
	    }
	}
*/
	
	//TOOLBAR BUILDING METHODS
	
	/**
	 * toolbar for when you're looking at a grid
	 * @return
	 */
	private JToolBar buildGridToolbar() {
		
		JToolBar toolBar = new JToolBar();
		
		ButtonGroup modeButtonGroup = new ButtonGroup();
		selectButton = makeRadioToolButton("select_mode.png", "", "Select (" +
				KeyEvent.getKeyText(KeyEvent.VK_ESCAPE) + ")", this, modeButtonGroup); 
		toolBar.add(selectButton);
		selectButton.setSelected(true);
		addComponentButton = makeRadioToolButton("add_component.png", "", "Add Modules (M)", this, modeButtonGroup);
		toolBar.add(addComponentButton);
		toolBar.add(makeToolButton("", "editGridSize", "Edit Grid Size", this));
		
		toolBar.addSeparator();
		
		zoomButton = new JToggleButton();
		zoomButton.setText("Zoom");
		
		panButton = new JToggleButton();
		panButton.setText("Pan");
		
		toolBar.add(zoomButton);
		toolBar.add(makeToolButton("", "unZoom", "Un-Zoom", this));
		toolBar.add(panButton);
		
		toolBar.addSeparator();

		modelPanel = new ModelPanel(bioModel, modelEditor);
		toolBar.add(modelPanel);
		toolBar.setFloatable(false);
		
		/*
		compartmentList.setSelectedItem(bioModel.getDefaultCompartment());
		compartmentList.addActionListener(this);
		*/
		
		return toolBar;
	}
	
	public void moveCells(int deltaX,int deltaY) {
		Object cells[] = graph.getSelectionCells();
		int numberEdges = 0;
		for(int i=0; i<cells.length; i++){
			
			mxCell cell = (mxCell)cells[i];
			
			// If an edge gets moved ignore it then rebuild the graph from the model
			if(cell.isEdge()){
				numberEdges++;
			}
			else{
				cell.getGeometry().setX(cell.getGeometry().getX()+deltaX);
				cell.getGeometry().setY(cell.getGeometry().getY()+deltaY);
				graph.updateInternalPosition(cell,true);
			}
		}
		if (numberEdges==cells.length) {
		  JOptionPane.showMessageDialog(Gui.frame, "Edges cannot be moved independently.","Error Moving Edges", JOptionPane.ERROR_MESSAGE); 
			graph.buildGraph();
			drawGrid();
		} else {
			graph.buildGraph();
			graph.setSelectionCells(cells);
			drawGrid();
			modelEditor.makeUndoPoint();
			modelEditor.setDirty(true);
		}
	}
	
	public void cut() {
		removeCells();
	}
	
	public int[] getPosition(int x, int y) {
		int[] xy = new int[2];
		if (x < 0 && y < 0) {
			PointerInfo a = MouseInfo.getPointerInfo();
			Point c = graphComponent.getLocationOnScreen();
			Point b = a.getLocation();
			x = (int)(b.getX() - c.getX());
			y = (int)(b.getY() - c.getY());
		}	
		if (x<0) x = 0;
		if (y<0) y = 0;
		xy[0] = x;
		xy[1] = y;
		return xy;
	}
	
	public void addCompartment(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		bioModel.createCompartment(null, x, y);
		graph.buildGraph();
		modelEditor.refresh();
		modelEditor.setDirty(true);
		modelEditor.makeUndoPoint();
	}
	
	public void addSpecies(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		bioModel.createSpecies(null, x, y);
		graph.buildGraph();
		modelEditor.refresh();
		modelEditor.setDirty(true);
		modelEditor.makeUndoPoint();
	}
	
	public void addReaction(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		bioModel.createReaction(null, x, y);
		graph.buildGraph();
		modelEditor.refresh();
		modelEditor.setDirty(true);
		modelEditor.makeUndoPoint();
	}
	
	public void addComponent(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		
		boolean dropped;
		
		//if there's a grid, do a different panel than normal
		if (bioModel.isGridEnabled()) {

			//the true is to indicate the dropping is happening on a grid
			dropped = DropComponentPanel.dropComponent(modelEditor, bioModel, x, y, true);
		}
		else {
			
			//the false is to indicate the dropping isn't happening on a grid
			dropped = DropComponentPanel.dropComponent(modelEditor, bioModel, x, y, false);
		}
		
		//if the components dropped successfully
		if(dropped){
			
			modelEditor.setDirty(true);
			graph.buildGraph();
			modelEditor.refresh();
			modelEditor.makeUndoPoint();
		}
	}
	
	public void addPromoter(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		bioModel.createPromoter(null, x, y, true);
		modelEditor.refresh();
		graph.buildGraph();
		modelEditor.setDirty(true);
		modelEditor.makeUndoPoint();
	}
	
	public void addVariable(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		bioModel.createVariable(null, x, y, false, false);
		modelEditor.refresh();
		graph.buildGraph();
		modelEditor.setDirty(true);
		modelEditor.makeUndoPoint();
	}
	
	public void addBoolean(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		bioModel.createVariable(null, x, y, false, true);
		modelEditor.refresh();
		graph.buildGraph();
		modelEditor.setDirty(true);
		modelEditor.makeUndoPoint();
	}
	
	public void addPlace(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		bioModel.createVariable(null, x, y, true, false);
		modelEditor.refresh();
		graph.buildGraph();
		modelEditor.setDirty(true);
		modelEditor.makeUndoPoint();
	}
	
	public void addTransition(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		String id = events.eventEditor("Add", "",true);
		if (!id.equals("")) {
			bioModel.createEvent(id, x, y, true);
			modelEditor.refresh();
			graph.buildGraph();
			modelEditor.setDirty(true);
			modelEditor.makeUndoPoint();
		}
	}
	
	public void addRule(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		String id = rules.ruleEditor("Add", "");
		if (!id.equals("")) {
			bioModel.createRule(id, x, y);
			modelEditor.refresh();
			graph.buildGraph();
			modelEditor.setDirty(true);
			modelEditor.makeUndoPoint();
		}
	}
	
	public void addConstraint(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		String id = constraints.constraintEditor("Add", "");
		if (!id.equals("")) {
			bioModel.createConstraint(id, x, y);
			modelEditor.refresh();
			graph.buildGraph();
			modelEditor.setDirty(true);
			modelEditor.makeUndoPoint();
		}
	}
	
	public void addEvent(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		String id = events.eventEditor("Add", "",false);
		if (!id.equals("")) {
			bioModel.createEvent(id, x, y, false);
			modelEditor.refresh();
			graph.buildGraph();
			modelEditor.setDirty(true);
			modelEditor.makeUndoPoint();
		}
	}
	
	public void addSelfInfluence(int x, int y) {
		int[] xy = getPosition(x,y);
		x = xy[0];
		y = xy[1];
		mxCell cell = (mxCell)(graphComponent.getCellAt(x,y));
		if(cell.isEdge() == false) {
			// the user clicked to add a self-influence to a component.
			//Object parent, String id, Object value, Object source, Object target, String style
			mxCell edge = new mxCell();
			edge.setEdge(true);
			edge.setSource(cell);
			edge.setTarget(cell);
			tryAddAssociationBetweenCells(edge);								
		}
	}
	
	public void select() {
		selectButton.doClick();
	}
	
	private JRadioButton makeRadioToolButton(String iconFilename, String actionCommand, String tooltip, ActionListener listener, final ButtonGroup buttonGroup){
		URL icon = null;
		URL selectedIcon = null;
		if (!iconFilename.equals("")) {
			icon = ResourceManager.getResource("icons/modelview/" + iconFilename);
			String selectedPath = iconFilename.replaceAll(".png", "_selected.png");
			selectedIcon = ResourceManager.getResource("icons/modelview/" + selectedPath);
		}
		return Utils.makeRadioToolButton(icon,selectedIcon,actionCommand,tooltip,listener,buttonGroup);
	}
	
	private JButton makeToolButton(String iconFilename, String actionCommand, String tooltip, ActionListener listener) {
		URL icon = null;
		URL selectedIcon = null;
		if (!iconFilename.equals("")) {
			icon = ResourceManager.getResource("icons/modelview/" + iconFilename);
			String selectedPath = iconFilename.replaceAll(".png", "_selected.png");
			selectedIcon = ResourceManager.getResource("icons/modelview/" + selectedPath);
		}
		return Utils.makeToolButton(icon, selectedIcon, actionCommand, tooltip, listener);
	}
	/**
	 * create the toolbar.
	 * @return
	 */
	private JToolBar buildToolBar(){

		JToolBar toolBar = new JToolBar();
		
		ButtonGroup modeButtonGroup = new ButtonGroup();
		selectButton = makeRadioToolButton("select_mode.png", "", "Select (" + KeyEvent.getKeyText(KeyEvent.VK_ESCAPE)
				+ ")", this, modeButtonGroup); 
		toolBar.add(selectButton);
		selectButton.setSelected(true);
		addCompartmentButton = makeRadioToolButton("add_compartment.png", "", "Add Compartment (" + 
				KeyEvent.getKeyText(KeyEvent.VK_ALT) + " C)", this, modeButtonGroup);
		toolBar.add(addCompartmentButton);
		addSpeciesButton = makeRadioToolButton("add_species.png", "", "Add Species (S)", this, modeButtonGroup);
		toolBar.add(addSpeciesButton);
		addReactionButton = makeRadioToolButton("add_reaction.png", "", "Add Reactions (R)", this, modeButtonGroup);
		toolBar.add(addReactionButton);
		addComponentButton = makeRadioToolButton("add_component.png", "", "Add Modules (M)", this, modeButtonGroup);
		toolBar.add(addComponentButton);
		addPromoterButton = makeRadioToolButton("promoter_mode.png", "", "Add Promoters (" +
				KeyEvent.getKeyText(KeyEvent.VK_SHIFT) + " P)", this, modeButtonGroup);
		toolBar.add(addPromoterButton);
		addVariableButton = makeRadioToolButton("variable_mode.png", "", "Add Variables (V)", this, modeButtonGroup);
		toolBar.add(addVariableButton);
		addBooleanButton = makeRadioToolButton("boolean_mode.png", "", "Add Booleans (B)", this, modeButtonGroup);
		toolBar.add(addBooleanButton);
		addPlaceButton = makeRadioToolButton("add_place.png", "", "Add Places (P)", this, modeButtonGroup);
		toolBar.add(addPlaceButton);
		addTransitionButton = makeRadioToolButton("add_transition.png", "", "Add Transitions (T)", this, modeButtonGroup);
		toolBar.add(addTransitionButton);
		addRuleButton = makeRadioToolButton("rule_mode.png", "", "Add Rules (" +
				KeyEvent.getKeyText(KeyEvent.VK_SHIFT) + " R)", this, modeButtonGroup);
		toolBar.add(addRuleButton);
		addConstraintButton = makeRadioToolButton("constraint_mode.png", "", "Add Constraints (" +
				KeyEvent.getKeyText(KeyEvent.VK_SHIFT) + " C)", this, modeButtonGroup);
		toolBar.add(addConstraintButton);
		addEventButton = makeRadioToolButton("event_mode.png", "", "Add Events (E)", this, modeButtonGroup);
		toolBar.add(addEventButton);
		selfInfluenceButton = makeRadioToolButton("self_influence.png", "", "Add Self Influences (I)", this, modeButtonGroup);
		toolBar.add(selfInfluenceButton);

		toolBar.addSeparator();
		ButtonGroup influenceButtonGroup = new ButtonGroup();
		
		activationButton = makeRadioToolButton("activation.png", "", "Activation", this, influenceButtonGroup);
		activationButton.setSelected(true);
		toolBar.add(activationButton);
		inhibitionButton = makeRadioToolButton("inhibition.png", "", "Repression", this, influenceButtonGroup);
		toolBar.add(inhibitionButton);
		noInfluenceButton = makeRadioToolButton("no_influence.png", "", "No Influence", this, influenceButtonGroup);
		toolBar.add(noInfluenceButton);
		bioActivationButton = makeRadioToolButton("bio_activation.png", "", "Complex Formation", this, influenceButtonGroup);
		toolBar.add(bioActivationButton);
		reactionButton = makeRadioToolButton("reaction.png", "", "Reaction", this, influenceButtonGroup);
		toolBar.add(reactionButton);
		modifierButton = makeRadioToolButton("modifier.png", "", "Modifier", this, influenceButtonGroup);
		toolBar.add(modifierButton);

		toolBar.addSeparator();
		toolBar.add(makeToolButton("choose_layout.png", "showLayouts", "Apply Layout", this));
		
		toolBar.addSeparator();
		
		zoomButton = new JToggleButton();
		zoomButton.setText("Zoom");
		zoomButton.setToolTipText("Use the mouse wheel to zoom");
		
		panButton = new JToggleButton();
		panButton.setText("Pan");
		panButton.setToolTipText("Use mouse dragging to pan");
		
		toolBar.add(zoomButton);
		toolBar.add(makeToolButton("", "unZoom", "Un-Zoom", this));
		toolBar.add(panButton);
		//toolBar.add(Utils.makeToolButton("", "saveSBOL", "Save SBOL", this));
		//toolBar.add(Utils.makeToolButton("", "exportSBOL", "Export SBOL", this));
		
		toolBar.addSeparator();

		modelPanel = new ModelPanel(bioModel, modelEditor);
		toolBar.add(modelPanel);
		
//		if (bioModel.getElementSBOLCount() == 0 && bioModel.getModelSBOLAnnotationFlag())
//			sbolDescriptorsButton.setEnabled(false);

		toolBar.setFloatable(false);
		
		return toolBar;
	}
	
	/**
	 * create the toolbar.
	 * @return
	 */
	private JToolBar buildLEMAToolBar(){

		JToolBar toolBar = new JToolBar();
		
		ButtonGroup modeButtonGroup = new ButtonGroup();
		selectButton = makeRadioToolButton("select_mode.png", "", "Select (" +
				KeyEvent.getKeyText(KeyEvent.VK_ESCAPE) + ")", this, modeButtonGroup); 
		toolBar.add(selectButton);
		selectButton.setSelected(true);
		addComponentButton = makeRadioToolButton("add_component.png", "", "Add Modules (M)", this, modeButtonGroup);
		toolBar.add(addComponentButton);
		addBooleanButton = makeRadioToolButton("boolean_mode.png", "", "Add Booleans (B)", this, modeButtonGroup);
		toolBar.add(addBooleanButton);
		addVariableButton = makeRadioToolButton("variable_mode.png", "", "Add Variables (V)", this, modeButtonGroup);
		toolBar.add(addVariableButton);
		addPlaceButton = makeRadioToolButton("add_place.png", "", "Add Places (P)", this, modeButtonGroup);
		toolBar.add(addPlaceButton);
		addTransitionButton = makeRadioToolButton("add_transition.png", "", "Add Transitions (T)", this, modeButtonGroup);
		toolBar.add(addTransitionButton);
		addRuleButton = makeRadioToolButton("rule_mode.png", "", "Add Rules (" +
				KeyEvent.getKeyText(KeyEvent.VK_SHIFT) + " R)", this, modeButtonGroup);
		toolBar.add(addRuleButton);
		addConstraintButton = makeRadioToolButton("constraint_mode.png", "", "Add Constraints (" +
				KeyEvent.getKeyText(KeyEvent.VK_SHIFT) + " C)", this, modeButtonGroup);
		toolBar.add(addConstraintButton);
		//addEventButton = Utils.makeRadioToolButton("event_mode.png", "", "Add Events", this, modeButtonGroup);
		//toolBar.add(addEventButton);

		//toolBar.addSeparator();
		ButtonGroup influenceButtonGroup = new ButtonGroup();
		
		activationButton = makeRadioToolButton("activation.png", "", "Activation", this, influenceButtonGroup);
		activationButton.setSelected(true);
		//toolBar.add(activationButton);

		toolBar.addSeparator();
		toolBar.add(makeToolButton("choose_layout.png", "showLayouts", "Apply Layout", this));
		
		toolBar.addSeparator();
		
		zoomButton = new JToggleButton();
		zoomButton.setText("Zoom");
		zoomButton.setToolTipText("Use the mouse wheel to zoom");
		
		panButton = new JToggleButton();
		panButton.setText("Pan");
		panButton.setToolTipText("Use mouse dragging to pan");
		
		toolBar.add(zoomButton);
		toolBar.add(makeToolButton("", "unZoom", "Un-Zoom", this));
		toolBar.add(panButton);
		//toolBar.add(Utils.makeToolButton("", "saveSBOL", "Save SBOL", this));
		//toolBar.add(Utils.makeToolButton("", "exportSBOL", "Export SBOL", this));
		
		//toolBar.addSeparator();

		//ModelPanel modelPanel = new ModelPanel(bioModel, modelEditor);
		//toolBar.add(modelPanel);

		toolBar.setFloatable(false);
		
		return toolBar;
	}

	
	//ACTION AND LISTENER METHODS
	
	/**
	 * Called when a toolbar button is clicked.
	 */
	@Override
	public void actionPerformed(ActionEvent event){
		
		String command = event.getActionCommand();
				
		if(command.equals("showLayouts")){
			// Show the layout popup
			layoutPopup = new JPopupMenu();
			
			for(JMenuItem item:Layouting.getMenuItems(this))
				layoutPopup.add(item); 	
			
			JComponent showLayoutsButton = (JComponent)event.getSource();
			
			layoutPopup.show(this, showLayoutsButton.getX(), showLayoutsButton.getY()+showLayoutsButton.getHeight());
			
		}
		else if(command.indexOf("layout_") == 0){
			// Layout actioncommands are prepended with "_"
			if (bioModel.getSBMLDocument().getModel().getCompartmentCount() > 1) {
			  JOptionPane.showMessageDialog(Gui.frame,  "Automatic layout routines cannot be used with multi-compartment models.", "Error Applying Layout",JOptionPane.ERROR_MESSAGE); 
				return;
			}
			command = command.substring(command.indexOf('_')+1);
			graph.applyLayout(command, this.graphComponent);
			graph.buildGraph(); // rebuild, quick way to clear out any edge midpoints.
			modelEditor.setDirty(true);
			modelEditor.makeUndoPoint();
		}
		/*
		else if(command == "compartment"){
			if (compartments != null) {
				compartments.compartEditor("OK");
			}
		}
		*/
		/*
		else if(command == "checkCompartment") {
			
			boolean disallow = false;
			
			//don't allow the compartment box to be ticked if there
			//are input or output species in the GCM
			for (String species : gcm.getSpecies()) {
				
				if (BioModel.getSpeciesType(gcm.getSBMLDocument(),species).equals(GlobalConstants.INPUT) ||
					BioModel.getSpeciesType(gcm.getSBMLDocument(),species).equals(GlobalConstants.OUTPUT)) {
					
					disallow = true;
					break;
				}
			}
			
			if (!disallow) {
				//gcm.setIsWithinCompartment(check.isSelected());
				gcm.setDefaultCompartment((String)compartmentList.getSelectedItem());
			} else {
				if (!check.isSelected()) {
					//gcm.setIsWithinCompartment(false);
					gcm.setDefaultCompartment((String)compartmentList.getSelectedItem());
				} else { 
					check.setSelected(false);
					JOptionPane.showMessageDialog(Gui.frame, 
						"A GCM cannot become a compartment with input and output species present.\n" +
						"Diffusible species should be used to transport species across a compartment membrane.",
						"Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		} 
		*/
		else if (command.equals("editGridSize")) {
			
			//static method that builds the grid editing panel
			//the true field means to open the grid edit panel
			GridPanel gridPanel = new GridPanel(modelEditor, bioModel);
			boolean changed = gridPanel.showGridPanel(true);
			
			//if the grid size is changed, then draw it and so on
			if (changed) {
				
				modelEditor.setDirty(true);
				modelEditor.refresh();
				graph.buildGraph();
				drawGrid();
				modelEditor.makeUndoPoint();
			}
		}
		else if (command.equals("unZoom")) {
			
			graph.getView().setScale(1.0);
		}
		else if(command == ""){
			// radio buttons don't have to do anything and have an action command of "".
		}
		/*
		else if(command == "comboBoxChanged") {
			if (compartmentList.getSelectedItem()!=null)
				bioModel.setDefaultCompartment((String)compartmentList.getSelectedItem());
		}
		*/
		else{
			throw(new Error("Invalid actionCommand: " + command));
		}
	}
	
	public void refresh() {
		if (bioModel.isGridEnabled()) {
			grid = modelEditor.getGrid();
			
			//the new grid pointer may not have an accurate enabled state
			//so make sure it's set to true
			grid.refreshComponents();
		}
		
		graph.buildGraph();
	}
	
	/**
	 * 
	 * @param name
	 * @param action
	 * @return
	 */
	@SuppressWarnings("serial")
	public Action bind(String name, final Action action) {
		
		return new AbstractAction(name, null)
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				action.actionPerformed(new ActionEvent(getGraphComponent(), 
						e.getID(), e.getActionCommand()));
			}
		};
	}
	
	public void addChangeListener() {
		if (tabbedPane != null) {

			// if the component tab changes, then rebuild the graph
			// this is useful if component/compartment status changes
			tabbedPane.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent event) {

					graph.buildGraph();
				}
			});
		}
	}
	
	/**
	 * Add (mouse) listeners for the graph component
	 * listeners for clicking and mouse movement
	 */
	private void addGraphComponentListeners(){
		
		addChangeListener();
		
		//vertical scrolling listener for grid stuff
		graphComponent.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){

			@Override
			public void adjustmentValueChanged(AdjustmentEvent arg0) {				

				if (bioModel.isGridEnabled()) {
					
					int offset = graphComponent.getVerticalScrollBar().getValue();				
					Point scrollOffset = new Point(grid.getScrollOffset().x, offset);
					
					grid.setScrollOffset(scrollOffset);
					grid.syncGridGraph(graph);
				}
			}
		});
		
		//horizontal scrolling listener for grid stuff
		graphComponent.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener(){

			@Override
			public void adjustmentValueChanged(AdjustmentEvent arg0) {				

				if (bioModel.isGridEnabled()) {
					
					int offset = graphComponent.getHorizontalScrollBar().getValue();		
					Point scrollOffset = new Point(offset, grid.getScrollOffset().y);
					
					grid.setScrollOffset(scrollOffset);
					grid.syncGridGraph(graph);
				}
			}
		});
		
		//mouse clicked listener for grid stuff
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
				
			@Override
			public void mouseReleased(MouseEvent event) {
				
				Point location = event.getPoint();
				
				if (bioModel.isGridEnabled() && SwingUtilities.isLeftMouseButton(event)) {
						
					grid.setMouseClickLocation(location);
					drawGrid();
				}
			}
		});
		
		//mouse moved listener for grid stuff
		graphComponent.getGraphControl().addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent event) {
				
				if (bioModel.isGridEnabled()) {
					
					Point location = event.getPoint();
					
					grid.setMouseLocation(location);			
					drawGrid();
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent event) {
				
				if (rubberband.isEnabled()){
					
					//for rubberband selection of grid rectangles
					grid.setRubberbandBounds(rubberband.getBounds());
				}				
			}
		});
		
		//mouse wheel moved (ie, scrolling) listener for grid stuff
		graphComponent.getGraphControl().addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				
				if ((zoomButton != null && zoomButton.isSelected()) || event.isControlDown() || event.isAltDown()) {
					
					if (event.getWheelRotation() > 0) {
						
						graphComponent.zoomOut();
						
						if (bioModel.isGridEnabled())
							grid.syncGridGraph(graph);
					}
					else {
						
						graphComponent.zoomIn();
						
						if (bioModel.isGridEnabled()) 
							grid.syncGridGraph(graph);
					}
					
				}
				else {
					
					int scrollAmount = event.getScrollAmount();
					
					//make sure the scrolling happens in the right direction
					//scrollAmount is an absolute value figure
					if (event.getWheelRotation() < 0) scrollAmount *= -1;
					
					int newValue = graphComponent.getVerticalScrollBar().getValue() + scrollAmount;
					graphComponent.getVerticalScrollBar().setValue(newValue);
				}				
			}		
		});		
	
		//mouse released listener -- on cells or on the graph
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter(){
			
			@Override
			public void mouseReleased(MouseEvent e) {
				
				grid.setMouseReleased(true);
				
				mxCell cell = (mxCell)(graphComponent.getCellAt(e.getX(), e.getY()));
				
				//single right-click
				if (e.isPopupTrigger() && SwingUtilities.isRightMouseButton(e)) {
					
					if (editable)
						showGraphPopupMenu(e);
				}
				//single left-click
				else if(e.getClickCount() == 1 && editable && SwingUtilities.isLeftMouseButton(e)) {
					
					// First check and if the user clicked on a component, let the graph lib take care of it.
					if(selectButton == null || !selectButton.isSelected()) {
						
						//user clicked with select button
						/*
						if(selectButton != null && selectButton.isSelected()){
						}
						else*/ 
						if(addCompartmentButton != null && addCompartmentButton.isSelected()) {
							addCompartment(e.getX(), e.getY());
						}
						else if(addSpeciesButton != null && addSpeciesButton.isSelected()) {
							addSpecies(e.getX(), e.getY());
						}
						else if(addReactionButton != null && addReactionButton.isSelected()) {
							addReaction(e.getX(), e.getY());
						}
						else if(addComponentButton != null && addComponentButton.isSelected()) {
							addComponent(e.getX(), e.getY());
						}
						else if(addPromoterButton != null && addPromoterButton.isSelected()) {
							addPromoter(e.getX(), e.getY());
						}
						else if(addVariableButton != null && addVariableButton.isSelected()) {
							addVariable(e.getX(), e.getY());
						}
						else if(addBooleanButton != null && addBooleanButton.isSelected()) {
							addBoolean(e.getX(), e.getY());
						}
						else if(addPlaceButton != null && addPlaceButton.isSelected()) {
							addPlace(e.getX(), e.getY());
						}
						else if(addRuleButton != null && addRuleButton.isSelected()) {
							addRule(e.getX(), e.getY());
						}
						else if(addConstraintButton != null && addConstraintButton.isSelected()) {
							addConstraint(e.getX(), e.getY());
						}
						else if(addEventButton != null && addEventButton.isSelected()) {
							addEvent(e.getX(), e.getY());
						}
						else if(addTransitionButton != null && addTransitionButton.isSelected()) {
							addTransition(e.getX(),e.getY());
						}
						else if(selfInfluenceButton != null && selfInfluenceButton.isSelected()) {
							addSelfInfluence(e.getX(),e.getY());
						}
					}
					//if cell != null
				}
				//double-click with left mouse button
				else if(e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
					
					//control + double-click is un-zoom
					if (e.isControlDown()) {
						graph.getView().setScale(1.0);
					}
					
					bringUpEditorForCell(cell);
					
					if (editable)
						graph.buildGraph();
					
					modelEditor.makeUndoPoint();
					
//					if (cell != null) {
//						
//						bringUpEditorForCell(cell);
//						
//						if (editable)
//							graph.buildGraph();
//						
//						bioModel.makeUndoPoint();
//					}			
//					
//					if (grid.isEnabled()) {
//						
//						//if the user clicks on the area outside of the component within the grid location
//						//bring up the appropriate panel
//						if (grid.clickedOnGridPadding(e.getPoint())) {
//							
//							//if we're in analysis view (ie, movie stuff)
//							//prompt the user for coloring species and whatnot for the movie
//							if (!editable) {
//								
//								//if there isn't a TSDParser, the user hasn't selected
//								//a TSD file to simulate
//								if (movieContainer.getTSDParser() == null && movieContainer.getDTSDParser() == null)
//									JOptionPane.showMessageDialog(Gui.frame, "You must choose a simulation file before editing component properties.");
//								else {
//									if (cell != null) {
//										SchemeChooserPanel.showSchemeChooserPanel(cell.getId(), movieContainer);
//									}
//								}
//							}
//						}
//					}
				}
				
				if (bioModel.isGridEnabled())
					drawGrid();
			}
		});
				
		//add a special listener for right-clicking on macs
		//they use mousePressed instead of mouseReleased for a click event
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger() && SwingUtilities.isRightMouseButton(e)){
					// rightclick on mac
					if (editable)
						showGraphPopupMenu(e);
					//editable = false;
				}
			}
		});
	}

	/**
	 * Listeners to take care of CELL events -- cell change/movement on the graph
	 */
	private void addGraphListeners() {
		
		// Listen for moved cells
		graph.addListener(mxEvent.CELLS_MOVED, new mxEventSource.mxIEventListener() {
			
			@Override
			public void invoke(Object arg0, mxEventObject event) {
				
				Object cells[] = (Object [])event.getProperties().get("cells");
				
				int numberEdges = 0;
				for(int i=0; i<cells.length; i++){
					
					mxCell cell = (mxCell)cells[i];
					
					// If an edge gets moved ignore it then rebuild the graph from the model
					if(cell.isEdge()){
						numberEdges++;
					}
					else{
						
						//if there's a grid, only move cell to an open grid location
						//only one cell at a time and you can't be in analysis view
						if (bioModel.isGridEnabled() && cells.length == 1 && editable) {
							
							//see if the component/cell can be moved
							Boolean moved = grid.moveNode(cell.getId(), cell.getGeometry().getCenterX(), 
									cell.getGeometry().getCenterY(), bioModel);
							
							//if it can, update its position on the graph
							//(moveComponent updates its grid position)
							if (moved) {
								
								Rectangle snapRect = grid.getSnapRectangleFromCompID(cell.getId());
								
								//put the moved component/cell in its proper x,y location on the grid
								mxGeometry snapGeom = 
									new mxGeometry(snapRect.x, snapRect.y, snapRect.width, snapRect.height);
								cell.setGeometry(snapGeom);
								graph.updateInternalPosition(cell,true);
							}
						}
						//if there's no grid, move the cell wherever
						else {
							if (!bioModel.isGridEnabled())
								graph.updateInternalPosition(cell,true);
						}
					}
				}
				if (numberEdges==cells.length) {
					 JOptionPane.showMessageDialog(Gui.frame, "Edges cannot be moved independently.", "Error Moving Edges", JOptionPane.ERROR_MESSAGE); 
					graph.buildGraph();
					drawGrid();
				} else {
					graph.buildGraph();
					drawGrid();
					modelEditor.makeUndoPoint();
					modelEditor.setDirty(true);
				}
			}
		});
	
		// Listen for deleted cells
		graph.addListener(mxEvent.CELLS_REMOVED, new mxEventSource.mxIEventListener() {
			
			@Override
			public void invoke(Object arg0, mxEventObject event) {
				removeCells();
			}
	
		});

		graph.addListener(mxEvent.CELLS_RESIZED, new mxEventSource.mxIEventListener() {
			@Override
			public void invoke(Object arg0, mxEventObject event) {
				Object cells[] = (Object [])event.getProperties().get("cells");

				for(Object ocell:cells){
					mxCell cell = (mxCell)ocell;
					graph.updateInternalPosition(cell,true);
				}
				graph.buildGraph();
				drawGrid();
				modelEditor.makeUndoPoint();
				modelEditor.setDirty(true);
			}
		});
		
		// listener for added influences
		graph.addListener(mxEvent.CELLS_ADDED, new mxEventSource.mxIEventListener() {
		
			@Override
			public void invoke(Object arg0, mxEventObject event) {
				
				if (graph.dynamic == true)
					return;
				
				//if the graph is building, ignore the creation of edges.
				//also, if we're on a grid, no influences are allowed
				if (graph.isBuilding == false) {
					
					// if the user tries to add anything in simulation mode, stop them.
					if (editable == false) {
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
		
		//this will print all graph events if uncommented
		graph.addListener(null, new mxEventSource.mxIEventListener() {
			
			//@Override
			@Override
			public void invoke(Object arg0, mxEventObject event) {
			}
		});
		
	}
	
	public void removeCells() {
		
		if (graph.dynamic == true)
			return;
			
		// if the graph isn't being built and this event
		// comes through, remove all the cells from the 
		// internal model that were specified.
		if(graph.isBuilding == false){

			Object cells[] = graph.getSelectionCells(); //(Object [])event.getProperties().get("cells");
			
			// sort the cells so that edges are first. This makes them
			// get deleted before anything they are connected to.
			Arrays.sort(cells, 0, cells.length, new Comparator<Object>() {
				
				@Override
				public int compare(Object a, Object b){
					
					boolean av = ((mxCell)a).isEdge();
					boolean bv = ((mxCell)b).isEdge();
					if(av && !bv) return -1; // a is edge, b isn't
					if(!av && bv) return 1; // b is edge, a isn't
					return 0; // both are the same
				}
			});
			
			boolean doNotRemove = false;
			for(Object ocell:cells){
				
				mxCell cell = (mxCell)ocell;
				String type = graph.getCellType(cell);

				if(type == GlobalConstants.SPECIES){
					if (SBMLutilities.variableInUse(bioModel.getSBMLDocument(), cell.getId(), false, true, this, null)) {
						doNotRemove = true;
					}
				} else if (type == GlobalConstants.PROMOTER){
					if (SBMLutilities.variableInUse(bioModel.getSBMLDocument(), cell.getId(), false, false, this, null)) {
						doNotRemove = true;
					}
				} else if(type == GlobalConstants.VARIABLE || type == GlobalConstants.PLACE || type == GlobalConstants.BOOLEAN) {
					if (SBMLutilities.variableInUse(bioModel.getSBMLDocument(), cell.getId(), false, true, this, null)) {
						doNotRemove = true;
					}
				} else if(type == GlobalConstants.COMPARTMENT) {
					if (Utils.compartmentInUse(bioModel.getSBMLDocument(), cell.getId())) {
						doNotRemove = true; 
					} else if (SBMLutilities.variableInUse(bioModel.getSBMLDocument(), cell.getId(), false, true, this, null)) {
						doNotRemove = true;
					}
				} else if (type == GlobalConstants.REACTION){
					if (SBMLutilities.variableInUse(bioModel.getSBMLDocument(), cell.getId(), false, false, this, null)) {
						doNotRemove = true;
					}
					/*
				} else if (type == GlobalConstants.RULE_EDGE){
					doNotRemove = true;
				} else if (type == GlobalConstants.CONSTRAINT_EDGE){
					doNotRemove = true;
				} else if (type == GlobalConstants.EVENT_EDGE){
					doNotRemove = true;
					*/
				} else if(type == GlobalConstants.REACTION_EDGE) {
					mxCell source = (mxCell)cell.getSource();
					mxCell target = (mxCell)cell.getTarget();
					if ((graph.getCellType(source) == GlobalConstants.SPECIES) &&
						(graph.getCellType(target) == GlobalConstants.SPECIES)) {
						Reaction r = bioModel.getSBMLDocument().getModel().getReaction((String)cell.getValue());
						ListOf<SpeciesReference> reactants = r.getListOfReactants();
						for (int i = 0; i < r.getReactantCount(); i++) {
							SpeciesReference s = reactants.get(i);
							if (s.getSpecies().equals(source.getId())) {
								if (s.isSetId() && 
										SBMLutilities.variableInUse(bioModel.getSBMLDocument(), s.getId(), false, false, this, null)) {
									doNotRemove = true;
								}
								break;
							}
						} 
						ListOf<SpeciesReference> products = r.getListOfProducts();
						for (int i = 0; i < r.getProductCount(); i++) {
							SpeciesReference s = products.get(i);
							if (s.getSpecies().equals(target.getId())) {
								if (s.isSetId() && 
										SBMLutilities.variableInUse(bioModel.getSBMLDocument(), s.getId(), false, false, this, null)) {
									doNotRemove = true;
								}
								break;
							}
						}
					} 
					else if ((graph.getCellType(source) == GlobalConstants.SPECIES) &&
						(graph.getCellType(target) == GlobalConstants.REACTION)) {
						Reaction r = bioModel.getSBMLDocument().getModel().getReaction(target.getId());
						int reactantNum = Integer.parseInt(cell.getId().replace(source.getId()+"__r","").replace("__"+target.getId(),""));
						SpeciesReference s = r.getReactant(reactantNum);
						if (s.isSetId() && 
								SBMLutilities.variableInUse(bioModel.getSBMLDocument(), s.getId(), false, false, this, null)) {
							doNotRemove = true;
						}
						break;
					} 
					else if ((graph.getCellType(source) == GlobalConstants.REACTION) &&
						(graph.getCellType(target) == GlobalConstants.SPECIES)) {
						Reaction r = bioModel.getSBMLDocument().getModel().getReaction(source.getId());
						int productNum = Integer.parseInt(cell.getId().replace(source.getId()+"__p","").replace("__"+target.getId(),""));
						SpeciesReference s = r.getProduct(productNum);
						if (s.isSetId() && 
								SBMLutilities.variableInUse(bioModel.getSBMLDocument(), s.getId(), false, false, this, null)) {
							doNotRemove = true;
						}
						break;
					}
				} else if(type == GlobalConstants.MODIFIER_REACTION_EDGE) {
					mxCell source = (mxCell)cell.getSource();
					mxCell target = (mxCell)cell.getTarget();
					Reaction r = bioModel.getSBMLDocument().getModel().getReaction(target.getId());
					int modifierNum = Integer.parseInt(cell.getId().replace(source.getId()+"__m","").replace("__"+target.getId(),""));
					ModifierSpeciesReference s = r.getModifier(modifierNum);
					if (s.isSetId() && 
							SBMLutilities.variableInUse(bioModel.getSBMLDocument(), s.getId(), false, false, this, null)) {
						doNotRemove = true;
					}
					break;
				}				
				if (doNotRemove) {
					modelEditor.refresh();
					graph.buildGraph();
					drawGrid();
					return;
				}
			}
			
			for(Object ocell:cells){
				
				mxCell cell = (mxCell)ocell;
				//System.out.print(cell.getId() + " Deleting.\n");
				
				String type = graph.getCellType(cell);
				
				if(type == GlobalConstants.INFLUENCE || type == GlobalConstants.PRODUCTION){
					bioModel.removeInfluence(cell.getId());
				}
				else if(type == GlobalConstants.REACTION_EDGE) {
					
					mxCell source = (mxCell)cell.getSource();
					mxCell target = (mxCell)cell.getTarget();
					
					if ((graph.getCellType(source) == GlobalConstants.SPECIES) &&
							(graph.getCellType(target) == GlobalConstants.SPECIES)) {
						
						Reaction r = bioModel.getSBMLDocument().getModel().getReaction((String)cell.getValue());
						
						if (r.getReactantCount()==1 && r.getProductCount()==1) {
							//Reactions.removeTheReaction(bioModel,(String)cell.getValue());
							bioModel.removeReaction((String)cell.getValue());
						} 
						else if (r.getReactantCount() > 1) {
							
							ListOf<SpeciesReference> reactants = r.getListOfReactants();
							
							for (int i = 0; i < r.getReactantCount(); i++) {
								
								SpeciesReference s = reactants.get(i);
								
								if (s.getSpecies().equals(source.getId())) {
									reactants.remove(i);
									break;
								}
							}
						} 
						else if (r.getProductCount() > 1) {
							
							ListOf<SpeciesReference> products = r.getListOfProducts();
							
							for (int i = 0; i < r.getProductCount(); i++) {
								
								SpeciesReference s = products.get(i);
								
								if (s.getSpecies().equals(target.getId())) {
									products.remove(i);
									break;
								}
							}
						}
					} 
					else if ((graph.getCellType(source) == GlobalConstants.SPECIES) &&
						(graph.getCellType(target) == GlobalConstants.REACTION)) {
						
						Reaction r = bioModel.getSBMLDocument().getModel().getReaction(target.getId());
						int reactantNum = Integer.parseInt(cell.getId().replace(source.getId()+"__r","").replace("__"+target.getId(),""));
						ListOf<SpeciesReference> reactants = r.getListOfReactants();
						reactants.remove(reactantNum);
						break;
					} 
					else if ((graph.getCellType(source) == GlobalConstants.REACTION) &&
						(graph.getCellType(target) == GlobalConstants.SPECIES)) {
						
						Reaction r = bioModel.getSBMLDocument().getModel().getReaction(source.getId());
						int productNum = Integer.parseInt(cell.getId().replace(source.getId()+"__p","").replace("__"+target.getId(),""));
						ListOf<SpeciesReference> products = r.getListOfProducts();
						products.remove(productNum);
					}
				}
				else if(type == GlobalConstants.MODIFIER_REACTION_EDGE) {
					
					mxCell source = (mxCell)cell.getSource();
					mxCell target = (mxCell)cell.getTarget();

					Reaction r = bioModel.getSBMLDocument().getModel().getReaction(target.getId());
					int modifierNum = Integer.parseInt(cell.getId().replace(source.getId()+"__m","").replace("__"+target.getId(),""));
					ListOf<ModifierSpeciesReference> modifiers = r.getListOfModifiers();
					modifiers.remove(modifierNum);
				}
				else if(type == GlobalConstants.REACTION) {

					bioModel.removeReaction(cell.getId());
				}
				else if(type == GlobalConstants.RULE) {
					
					bioModel.removeByMetaId(cell.getId());
				}
				else if(type == GlobalConstants.CONSTRAINT) {
					
					bioModel.removeByMetaId(cell.getId());
				}
				else if(type == GlobalConstants.EVENT || type == GlobalConstants.TRANSITION) {
					
					bioModel.removeById(cell.getId());
				}
				else if(type == GlobalConstants.VARIABLE || type == GlobalConstants.PLACE || type == GlobalConstants.BOOLEAN) {
					bioModel.removeById(cell.getId());
				}
				else if(type == GlobalConstants.COMPARTMENT) {
					bioModel.removeById(cell.getId());
				}
				else if(type == GlobalConstants.SPECIES){
					
					/*
					if(gcm.speciesUsedInOtherGCM(cell.getId())){
						JOptionPane.showMessageDialog(Gui.frame, "Sorry, the species \""+cell.getId()+"\" is used in another component and cannot be removed.");
						continue;
					}
					*/
					//gcm.removeSpeciesAndAssociations(cell.getId());
					modelEditor.removeSpecies(cell.getId());
					//graph.speciesRemoved(cell.getId());
				}
				else if(type == GlobalConstants.COMPONENT){
							
					//if there's a grid, remove the component from the grid as well
					if (bioModel.isGridEnabled()) {
						
						grid.eraseNode(cell.getId(), bioModel);
						modelEditor.getSpeciesPanel().refreshSpeciesPanel(bioModel);
					}
					else
						bioModel.removeComponent(cell.getId());
				}
				else if(type == GlobalConstants.PROMOTER){
				  modelEditor.removePromoter(cell.getId());
				}
				else if(type == GlobalConstants.PETRI_NET_EDGE){
					mxCell source = (mxCell)cell.getSource();
					mxCell target = (mxCell)cell.getTarget();
					if (graph.getCellType(source) == GlobalConstants.PLACE) {
						Event e = bioModel.getSBMLDocument().getModel().getEvent(target.getId());
						Trigger t = e.getTrigger();
						String sourceIDdim = SBMLutilities.getIdWithDimension(e, source.getId());
						t.setMath(SBMLutilities.removePreset(t.getMath(),sourceIDdim));
						EventAssignment ea = SBMLutilities.getEventAssignmentByVariable(e, source.getId());

						if (ea!=null) {
							ea.removeFromParent();
						}
					} else if (graph.getCellType(target) == GlobalConstants.PLACE) {
						Event e = bioModel.getSBMLDocument().getModel().getEvent(source.getId());
						EventAssignment ea = SBMLutilities.getEventAssignmentByVariable(e, target.getId());
						if (ea!=null) {
							ea.removeFromParent();
						}
					}
				}
				else if(type == GlobalConstants.COMPONENT_CONNECTION){
					removeComponentConnection(cell);
				}
				else if(type == graph.CELL_NOT_FULLY_CONNECTED){
					// do nothing. This can happen if the user deletes a species that is connected
					// to influences or connections. The influences or connections will be 
					// removed, but then the graph library will still fire an event to remove
					// the now defunct edge. In that case this branch will be called and should
					// do nothing.
				}
			}
			
			modelEditor.setDirty(true);
			modelEditor.refresh();
			graph.buildGraph();
			drawGrid();
			modelEditor.makeUndoPoint();
		}
	}

        
	//INPUT/OUTPUT AND CONNECTION METHODS
	
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
		/*
		if (source.getStyle().contains("COMPARTMENT") ||
 			target.getStyle().contains("COMPARTMENT")) {
			JOptionPane.showMessageDialog(Gui.frame, 
					"You can't connect a compartment to another object.\n" +
					"If you need a species to go across a compartment membrane, " +
					"you can make it diffusible.");
			graph.buildGraph();
			return;
		}
		*/
		// make sure there is at most 1 component
		int numComponents = 0;
		if(graph.getCellType(source)==GlobalConstants.COMPONENT)
			numComponents++;
		if(graph.getCellType(target)==GlobalConstants.COMPONENT)
			numComponents++;
		// bail out if the user tries to connect two components.
		if(numComponents == 2){
			JOptionPane.showMessageDialog(Gui.frame, "You can't connect a module directly to another module. Please go through a species or variable.");
			graph.buildGraph();
			return;
		}
		
		if((numComponents==0) && (graph.getCellType(source)==GlobalConstants.VARIABLE ||
				graph.getCellType(target)==GlobalConstants.VARIABLE)) {
			JOptionPane.showMessageDialog(Gui.frame, "A variable can only be explicitly connected to modules.");
			graph.buildGraph();
			return;
		} else if((numComponents==0) && (graph.getCellType(source)==GlobalConstants.BOOLEAN ||
				graph.getCellType(target)==GlobalConstants.BOOLEAN)) {
			JOptionPane.showMessageDialog(Gui.frame, "A Boolean variable can only be explicitly connected to modules.");
			graph.buildGraph();
			return;
		} /* else if((numComponents==0) && (graph.getCellType(source)==GlobalConstants.PLACE ||
				graph.getCellType(target)==GlobalConstants.PLACE)) {
			JOptionPane.showMessageDialog(Gui.frame, "A place variable can only be explicitly connected to components.");
			graph.buildGraph();
			return;
		} */ else if(graph.getCellType(source)==GlobalConstants.COMPARTMENT ||
				graph.getCellType(target)==GlobalConstants.COMPARTMENT) {
			JOptionPane.showMessageDialog(Gui.frame, "A compartment cannot be connected to other objects.");
			graph.buildGraph();
			return;
		} else if(graph.getCellType(source)==GlobalConstants.RULE ||
				graph.getCellType(target)==GlobalConstants.RULE) {
			JOptionPane.showMessageDialog(Gui.frame, "A rule cannot be explicitly connected to other objects.");
			graph.buildGraph();
			return;
		} else if(graph.getCellType(source)==GlobalConstants.EVENT ||
				graph.getCellType(target)==GlobalConstants.EVENT) {
			JOptionPane.showMessageDialog(Gui.frame, "A event cannot be explicitly connected to other objects.");
			graph.buildGraph();
			return;
		} else if(graph.getCellType(source)==GlobalConstants.CONSTRAINT ||
				graph.getCellType(target)==GlobalConstants.CONSTRAINT) {
			JOptionPane.showMessageDialog(Gui.frame, "A constraint cannot be explicitly connected to other objects.");
			graph.buildGraph();
			return;
		} else if(graph.getCellType(source)==GlobalConstants.PLACE &&
				!(graph.getCellType(target)==GlobalConstants.TRANSITION ||
				  graph.getCellType(target) == GlobalConstants.COMPONENT)) {
			JOptionPane.showMessageDialog(Gui.frame, "A place can only be connected to transitions.");
			graph.buildGraph();
			return;
		} else if(graph.getCellType(target)==GlobalConstants.PLACE &&
				!(graph.getCellType(source)==GlobalConstants.TRANSITION ||
				  graph.getCellType(source) == GlobalConstants.COMPONENT)) {
			JOptionPane.showMessageDialog(Gui.frame, "A place can only be connected to transitions.");
			graph.buildGraph();
			return;
		} else if(graph.getCellType(source)==GlobalConstants.TRANSITION &&
				!(graph.getCellType(target)==GlobalConstants.PLACE)) {
			JOptionPane.showMessageDialog(Gui.frame, "A transition can only be connected to places.");
			graph.buildGraph();
			return;
		} else if(graph.getCellType(target)==GlobalConstants.TRANSITION &&
				!(graph.getCellType(source)==GlobalConstants.PLACE)) {
			JOptionPane.showMessageDialog(Gui.frame, "A transition can only be connected to places.");
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
			JOptionPane.showMessageDialog(Gui.frame, "You can't connect a module directly to a promoter.");
			//graph.removeCells(cells);
			graph.buildGraph();
			return;
		}		

		
		String sourceID = source.getId();
		String targetID = target.getId();
		
		// Disallows user from connecting to a species that is an input
		if (graph.getCellType(target).contains(GlobalConstants.SPECIES)) {
			if (bioModel.isInput(targetID)) {
				// TOOD: REMOVED THIS || specType.contains(GlobalConstants.SPASTIC)) {
				JOptionPane.showMessageDialog(Gui.frame, "You can't connect to a species that is an input or constitutive.");
				graph.buildGraph();
				return;
			}
		}
		
		
		// see if we are connecting a component to a species
		if(numComponents == 1){
			
			String port = null;
			
			if(graph.getCellType(source) == GlobalConstants.COMPONENT){
				// source is a component
				if(graph.getCellType(target) == GlobalConstants.SPECIES){
					try{
						port = connectComponentToSpecies(sourceID, targetID);
					}
					catch(ListChooser.EmptyListException e){
						JOptionPane.showMessageDialog(Gui.frame, "This module has no species output ports.");
						graph.buildGraph();
						return;
					}
				} else if(graph.getCellType(target) == GlobalConstants.VARIABLE){
					try{
						port = connectComponentToVariable(sourceID, targetID);
					}
					catch(ListChooser.EmptyListException e){
						JOptionPane.showMessageDialog(Gui.frame, "This module has no variable output ports.");
						graph.buildGraph();
						return;
					}
				} else if(graph.getCellType(target) == GlobalConstants.PLACE){
					try{
						port = connectComponentToVariable(sourceID, targetID);
					}
					catch(ListChooser.EmptyListException e){
						JOptionPane.showMessageDialog(Gui.frame, "This module has no place output ports.");
						graph.buildGraph();
						return;
					}
				} else if(graph.getCellType(target) == GlobalConstants.BOOLEAN){
					try{
						port = connectComponentToVariable(sourceID, targetID);
					}
					catch(ListChooser.EmptyListException e){
						JOptionPane.showMessageDialog(Gui.frame, "This module has no Boolean output ports.");
						graph.buildGraph();
						return;
					}
				} else {
					graph.buildGraph();
					return;
				}
			}
			else{
				// target is a component
				if(graph.getCellType(source) == GlobalConstants.SPECIES){
					try{
						port = connectSpeciesToComponent(sourceID, targetID);
					}
					catch(ListChooser.EmptyListException e){
						JOptionPane.showMessageDialog(Gui.frame, "This module has no species input ports.");	
						graph.buildGraph();
						return;
					}
				} else if(graph.getCellType(source) == GlobalConstants.VARIABLE){
					try{
						port = connectVariableToComponent(sourceID, targetID);
					}
					catch(ListChooser.EmptyListException e){
						JOptionPane.showMessageDialog(Gui.frame, "This module has no variable input ports.");
						graph.buildGraph();
						return;
					}
				} else if(graph.getCellType(source) == GlobalConstants.PLACE){
					try{
						port = connectVariableToComponent(sourceID, targetID);
					}
					catch(ListChooser.EmptyListException e){
						JOptionPane.showMessageDialog(Gui.frame, "This module has no place input ports.");
						graph.buildGraph();
						return;
					}
				}  else if(graph.getCellType(source) == GlobalConstants.BOOLEAN){
					try{
						port = connectVariableToComponent(sourceID, targetID);
					}
					catch(ListChooser.EmptyListException e){
						JOptionPane.showMessageDialog(Gui.frame, "This module has no Boolean input ports.");
						graph.buildGraph();
						return;
					}
				} else {
					graph.buildGraph();
					return;
				}
			}
			
			if(port == null){
				graph.buildGraph();
				return;
			}
			
			modelEditor.refresh();
			modelEditor.setDirty(true);
			graph.updateComponentConnectionVisuals(edge, port);

			graph.buildGraph();
			modelEditor.makeUndoPoint();
			return;
		} 
		
		if (graph.getCellType(source) == GlobalConstants.PLACE) {
			Event e = bioModel.getSBMLDocument().getModel().getEvent(targetID);
			Trigger trigger = e.getTrigger();
			String sourceIDdim = SBMLutilities.getIdWithDimension(e, sourceID);
			trigger.setMath(SBMLutilities.addPreset(trigger.getMath(),sourceIDdim));
			EventAssignment ea = SBMLutilities.getEventAssignmentByVariable(e, sourceID);
			if (ea == null) {
				ea = e.createEventAssignment();
				ea.setVariable(sourceID);
				SBMLutilities.copyDimensionsToEdgeIndex(e, bioModel.getSBMLDocument().getModel().getParameter(sourceID), 
						ea, "variable");
				ea.setMath(SBMLutilities.myParseFormula("0"));
			} else {
				if (SBMLutilities.myFormulaToString(ea.getMath()).equals("1")) {
					JOptionPane.showMessageDialog(Gui.frame, "Self-loops not currently supported.");
				}
			}
			modelEditor.refresh();
			modelEditor.setDirty(true);
			graph.buildGraph();
			modelEditor.makeUndoPoint();
			return;
		} 
		if (graph.getCellType(target) == GlobalConstants.PLACE) {

			Event e = bioModel.getSBMLDocument().getModel().getEvent(sourceID);
			EventAssignment ea = SBMLutilities.getEventAssignmentByVariable(e, targetID);
			if (ea == null) {
				ea = e.createEventAssignment();
				ea.setVariable(targetID);
				SBMLutilities.copyDimensionsToEdgeIndex(e, bioModel.getSBMLDocument().getModel().getParameter(targetID), 
						ea, "variable");
				ea.setMath(SBMLutilities.myParseFormula("1"));
			} else {
				if (SBMLutilities.myFormulaToString(ea.getMath()).equals("0")) {
					JOptionPane.showMessageDialog(Gui.frame, "Self-loops not currently supported.");
				}
			}
			modelEditor.refresh();
			modelEditor.setDirty(true);
			graph.buildGraph();
			modelEditor.makeUndoPoint();
			return;
		} 
		
		if (reactionButton.isSelected()) {
			
			if (graph.getCellType(source) == GlobalConstants.SPECIES) {
				Species species = bioModel.getSBMLDocument().getModel().getSpecies(sourceID);
				if (!(species.getBoundaryCondition() || (!species.getConstant() && Rules.keepVarRateRule(bioModel, "", sourceID)))) {
					JOptionPane.showMessageDialog(Gui.frame, "Reactant cannot be constant or determined by a rule unless it is a boundary condition");
					graph.buildGraph();
					modelEditor.refresh();
					return;
				}
			}
			if (graph.getCellType(target) == GlobalConstants.SPECIES) {
				Species species = bioModel.getSBMLDocument().getModel().getSpecies(targetID);
				if (!(species.getBoundaryCondition() || (!species.getConstant() && Rules.keepVarRateRule(bioModel, "", targetID)))) {
					JOptionPane.showMessageDialog(Gui.frame, "Product cannot be constant or determined by a rule unless it is a boundary condition");
					graph.buildGraph();
					modelEditor.refresh();
					return;
				}
			}
			if ((graph.getCellType(source) == GlobalConstants.SPECIES) && 
					(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				
				modelEditor.addReaction(sourceID,targetID,false);
			} 
			else if ((graph.getCellType(source) == GlobalConstants.REACTION) && 
					(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				bioModel.addProductToReaction(targetID,sourceID);
			} 
			else if ((graph.getCellType(source) == GlobalConstants.SPECIES) && 
					(graph.getCellType(target) == GlobalConstants.REACTION)) {
				bioModel.addReactantToReaction(sourceID,targetID);
			} 
			else {
				JOptionPane.showMessageDialog(Gui.frame, "Reaction edge can connect only species and reactions");
				graph.buildGraph();
				modelEditor.refresh();
				return;
			}
		} 
		else if (modifierButton.isSelected()) {
			
			if ((graph.getCellType(source) == GlobalConstants.SPECIES) && 
					(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				
				modelEditor.addReaction(sourceID,targetID,true);
			} 
			else if ((graph.getCellType(source) == GlobalConstants.REACTION) && 
				(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				bioModel.addModifierToReaction(targetID,sourceID);
			} 
			else if ((graph.getCellType(source) == GlobalConstants.SPECIES) && 
				(graph.getCellType(target) == GlobalConstants.REACTION)) {
				bioModel.addModifierToReaction(sourceID,targetID);
			} 
			else {
				
				JOptionPane.showMessageDialog(Gui.frame, "Modifier must connect only species and reactions");
				graph.buildGraph();
				modelEditor.refresh();
				return;
			}
		} 
		else {
			if ((graph.getCellType(source) == GlobalConstants.REACTION) || 
				(graph.getCellType(target) == GlobalConstants.REACTION)) {
				
				JOptionPane.showMessageDialog(Gui.frame, "Can only connect reactions using reaction or modifier edge");
				graph.buildGraph();
				modelEditor.refresh();
				return;
			}
			
			// see if we need to connect a species to a promoter
			if(numPromoters == 1){
				if(graph.getCellType(source) == GlobalConstants.PROMOTER){
					// source is a promoter
					/*
					if (bioModel.isArray(targetID)) {	
						JOptionPane.showMessageDialog(Gui.frame, "Promoters cannot currently be connected to species arrays.");
						graph.buildGraph();
						modelEditor.refresh();
						return;
					}
					*/
					bioModel.addActivatorToProductionReaction(sourceID, "none", targetID, null, null, null);
				}
				else{
					// target is a promoter
					/*
					if (bioModel.isArray(sourceID)) {	
						JOptionPane.showMessageDialog(Gui.frame, "Promoters cannot currently be connected to species arrays.");
						graph.buildGraph();
						modelEditor.refresh();
						return;
					}
					*/
					if (activationButton.isSelected()) {
						bioModel.addActivatorToProductionReaction(targetID, sourceID, "none", null, null, null);
					} else if (inhibitionButton.isSelected()) {
						bioModel.addRepressorToProductionReaction(targetID, sourceID, "none", null, null, null);
					} else if (noInfluenceButton.isSelected()) {
						bioModel.addNoInfluenceToProductionReaction(targetID, sourceID, "none");
					}
				}

			}
			else{
				if ((graph.getCellType(source) == GlobalConstants.SPECIES) && 
						(graph.getCellType(target) == GlobalConstants.SPECIES) &&
						(bioModel.getSBMLDocument().getModel().getSpecies(sourceID)!=null) &&
						(bioModel.getSBMLDocument().getModel().getSpecies(targetID)!=null)) {
					// connect two species to each other, create new implicit promoter if influence is activation or repression
					String newPromoterName = "";

					if (activationButton.isSelected() || inhibitionButton.isSelected() || noInfluenceButton.isSelected()) {
						/*
						if (bioModel.isArray(sourceID)) {	
							JOptionPane.showMessageDialog(Gui.frame, "Regulation arcs cannot currently be connected between species arrays.");
							graph.buildGraph();
							modelEditor.refresh();
							return;
						}
						*/
						mxGeometry geom = target.getGeometry();
						newPromoterName = bioModel.createPromoter(null, (float)geom.getX(), (float)geom.getY(), false);
						bioModel.createProductionReaction(newPromoterName,null,null,null,null,null,null,false,null);
						if (activationButton.isSelected()) {
							bioModel.addActivatorToProductionReaction(newPromoterName, sourceID, targetID, null, null, null);
						} else if (inhibitionButton.isSelected()) {
							bioModel.addRepressorToProductionReaction(newPromoterName, sourceID, targetID, null, null, null);
						} else if (noInfluenceButton.isSelected()) {
							bioModel.addNoInfluenceToProductionReaction(newPromoterName, sourceID, targetID);
						}
					}
					else if (bioActivationButton.isSelected()) {
						if (bioModel.getComplexReaction(sourceID)!=null) {
							JOptionPane.showMessageDialog(Gui.frame, "Source of a complex reaction arc cannot be a product of a complex reaction.");
							graph.buildGraph();
							modelEditor.refresh();
							return;
						}

						/*
						if (bioModel.isArray(sourceID)) {	
							JOptionPane.showMessageDialog(Gui.frame, "Complex formation arcs cannot currently be connected between species arrays.");
							graph.buildGraph();
							modelEditor.refresh();
							return;
						}
						*/
						bioModel.addReactantToComplexReaction(sourceID, targetID, null, null);
					}
				}
			}
		}
		
		graph.buildGraph();
		modelEditor.refresh();
		modelEditor.setDirty(true);
		modelEditor.makeUndoPoint();
	}
	
	/**
	 * connects an output in a component to a species.
	 * @param comp_id
	 * @param spec_id
	 * @return: A boolean representing success or failure. True means it worked, false, means there was no output in the module.
	 */
	public String connectComponentToSpecies(String compID, String specID) throws ListChooser.EmptyListException{
		String fullPath = bioModel.getPath() + File.separator + bioModel.getModelFileName(compID);
		BioModel compBioModel = BioModel.createBioModel(bioModel.getPath(), this);
		try {
      compBioModel.load(fullPath);
		ArrayList<String> ports = compBioModel.getOutputPorts(GlobalConstants.SBMLSPECIES);
		String port = ListChooser.selectFromList(Gui.frame, ports.toArray(), "Please Choose an Output Port");
		if(port == null)
			return null;
		bioModel.connectComponentAndSpecies(compID, compBioModel.getPortByIdRef(port).getId(), specID, true);
		return port;
    } catch (XMLStreamException e) {
      JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
		catch (BioSimException e) {
      JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
        JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
		return null;
	}
	
	/**
	 * connects a species to the input of a module.
	 * @param spec_id
	 * @param comp_id
	 * @return a boolean representing success or failure.
	 */
	public String connectSpeciesToComponent(String specID, String compID) throws ListChooser.EmptyListException{
		String fullPath = bioModel.getPath() + File.separator + bioModel.getModelFileName(compID);
		BioModel compBioModel = BioModel.createBioModel(bioModel.getPath(), this);
		try {
      compBioModel.load(fullPath);
      ArrayList<String> ports = compBioModel.getInputPorts(GlobalConstants.SBMLSPECIES);
		String port = ListChooser.selectFromList(Gui.frame, ports.toArray(), "Please Choose an Input Port");
		if(port == null)
			return null;
		bioModel.connectComponentAndSpecies(compID, compBioModel.getPortByIdRef(port).getId(), specID, false);
		return port;
    } catch (XMLStreamException e) {
      JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
		catch (BioSimException e) {
      JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
        JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
		return null;
	}
	
	/**
	 * connects an output in a module to a species.
	 * @param comp_id
	 * @param spec_id
	 * @return: A boolean representing success or failure. True means it worked, false, means there was no output in the module.
	 */
	public String connectComponentToVariable(String compID, String varID) throws ListChooser.EmptyListException{
		Parameter p = bioModel.getSBMLDocument().getModel().getParameter(varID);
		String fullPath = bioModel.getPath() + File.separator + bioModel.getModelFileName(compID);
		BioModel compBioModel = BioModel.createBioModel(bioModel.getPath(), this);
		try {
      compBioModel.load(fullPath);
      ArrayList<String> ports;
      if (SBMLutilities.isBoolean(p)) {
        ports = compBioModel.getOutputPorts(GlobalConstants.BOOLEAN);
      } else if (SBMLutilities.isPlace(p)) {
        ports = compBioModel.getOutputPorts(GlobalConstants.PLACE);
      } else {
        ports = compBioModel.getOutputPorts(GlobalConstants.VARIABLE);
      }
      String port = ListChooser.selectFromList(Gui.frame, ports.toArray(), "Please Choose an Output Port");
      if(port == null)
        return null;
      bioModel.connectComponentAndVariable(compID, compBioModel.getPortByIdRef(port).getId(), varID, true);
      return port;
    } catch (XMLStreamException e) {
      JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
		catch (BioSimException e) {
      JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
        JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
	  return null;
	}
	
	/**
	 * connects a species to the input of a module.
	 * @param spec_id
	 * @param comp_id
	 * @return a boolean representing success or failure.
	 */
	public String connectVariableToComponent(String varID, String compID) throws ListChooser.EmptyListException{
		Parameter p = bioModel.getSBMLDocument().getModel().getParameter(varID);
		String fullPath = bioModel.getPath() + File.separator + bioModel.getModelFileName(compID);
		BioModel compBioModel = BioModel.createBioModel(bioModel.getPath(), this);
		try {
      compBioModel.load(fullPath);
      ArrayList<String> ports;
		if (SBMLutilities.isBoolean(p)) {
			ports = compBioModel.getInputPorts(GlobalConstants.BOOLEAN);
		} else if (SBMLutilities.isPlace(p)) {
			ports = compBioModel.getInputPorts(GlobalConstants.PLACE);
		} else {
			ports = compBioModel.getInputPorts(GlobalConstants.VARIABLE);
		}
		String port = ListChooser.selectFromList(Gui.frame, ports.toArray(), "Please Choose an Input Port");
		if(port == null)
			return null;
		bioModel.connectComponentAndVariable(compID, compBioModel.getPortByIdRef(port).getId(), varID, false);
		return port;
    } catch (XMLStreamException e) {
      JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
		catch (BioSimException e) {
      JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
        JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
		return null;
	}

	/**
	 * given an mxCell where either the source or target is a module,
	 * remove the connection.
	 */
	private void removeComponentConnection(mxCell cell){
		String speciesId;
		String componentId;
		if(graph.getCellType(cell.getTarget()) == GlobalConstants.COMPONENT){
			componentId = cell.getTarget().getId();
			speciesId = cell.getSource().getId();
			bioModel.removeComponentConnection(speciesId, componentId, 
					GlobalConstants.INPUT+"__"+((String)cell.getValue()).replace("Port ",""));
		}else if(graph.getCellType(cell.getSource()) == GlobalConstants.COMPONENT){
			componentId = cell.getSource().getId();
			speciesId = cell.getTarget().getId();
			bioModel.removeComponentConnection(speciesId, componentId, 
					GlobalConstants.OUTPUT+"__"+((String)cell.getValue()).replace("Port ",""));
		}else
			throw new Error("removeComponentConnection was called with a cell in which neither the source nor target was a module!");
	}
	
	/*
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
	*/
	
	//MENUS
	
	/**
	 * Given any type of cell, bring up an editor for it if supported.
	 * @param cell
	 */
	public void bringUpEditorForCell(mxCell cell) {

		String cellType = graph.getCellType(cell);
		
		if(cellType == GlobalConstants.SPECIES){
			
			if (!editable) {
				//if we're in analysis view, we need a tabbed pane
				//to allow for changing species parameters and movie appearance
				JTabbedPane speciesPane = new JTabbedPane();			
				
				SchemeChooserPanel scPanel = null;
				SpeciesPanel speciesPanel = modelEditor.launchSpeciesPanel(cell.getId(), true);
				
				speciesPane.addTab("Parameters", speciesPanel);
				
				//make sure a simulation file is selected before adding the appearance panel
				if(movieContainer != null && movieContainer.getTSDParser() != null) {
					scPanel = ModelEditor.getSchemeChooserPanel(cell.getId(), movieContainer, true);
					speciesPane.addTab("Appearance", scPanel);
				}
				else {
					
					 JPanel panel = new JPanel(false);
				        JLabel text = new JLabel(
				        		"To modify this species' appearance, please select a simulation file");
				        text.setHorizontalAlignment(SwingConstants.LEFT);
				        text.setVerticalAlignment(SwingConstants.TOP);
				        panel.setLayout(new GridLayout(1, 1));
				        panel.add(text);
				        
					speciesPane.addTab("Appearance", panel);
				}
				
				String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
				
				int okCancel = JOptionPane.showOptionDialog(Gui.frame, speciesPane, "Edit Species",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				
				if (okCancel == JOptionPane.OK_OPTION) {
					
					if (scPanel !=null) 
						scPanel.updateMovieScheme();
					
					speciesPanel.handlePanelData(0);
					ArrayList<String> parameterChanges = modelEditor.getParameterChanges();
					String updates = speciesPanel.updates();
					if (!updates.equals("")) {
						for (int i = parameterChanges.size() - 1; i >= 0; i--) {
							if (parameterChanges.get(i).startsWith(updates.split("/")[0])) {
								parameterChanges.remove(i);
							}
						}
						if (updates.contains(" ")) {
							for (String s : updates.split("\n")) {
								parameterChanges.add(s);
							}
						}
					}
					
					movieContainer.setIsDirty(true);
				}
			}
			else
				modelEditor.launchSpeciesPanel(cell.getId(), false);
		}
		else if(cellType == GlobalConstants.INFLUENCE){
			
			modelEditor.launchInfluencePanel(cell.getId());
		}
		else if(cellType == GlobalConstants.PRODUCTION){
			// do nothing
		}
		else if(cellType == GlobalConstants.RULE_EDGE){
			// do nothing
		}
		else if(cellType == GlobalConstants.CONSTRAINT_EDGE){
			// do nothing
		}
		else if(cellType == GlobalConstants.EVENT_EDGE){
			// do nothing
		}
		else if(cellType == GlobalConstants.PETRI_NET_EDGE){
			// do nothing
		}
		else if(cellType == GlobalConstants.REACTION_EDGE){
			
			mxCell source = (mxCell)cell.getSource();
			mxCell target = (mxCell)cell.getTarget();
			
			if ((graph.getCellType(source) == GlobalConstants.SPECIES) &&
					(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				
				reactions.reactionsEditor(bioModel,"OK",(String)cell.getValue(),false);
			} 
			else if ((graph.getCellType(source) == GlobalConstants.SPECIES) &&
				(graph.getCellType(target) == GlobalConstants.REACTION)) {
				
				int reactantNum = Integer.parseInt(cell.getId().replace(source.getId()+"__r","").replace("__"+target.getId(),""));
				SpeciesReference reactant = bioModel.getSBMLDocument().getModel().getReaction(target.getId()).getReactant(reactantNum);
				//getReactantForSpecies(source.getId());
				reactions.reactantsEditor(bioModel,"OK",source.getId(),reactant, true, 
						bioModel.getSBMLDocument().getModel().getReaction(target.getId()));
			} 
			else if ((graph.getCellType(source) == GlobalConstants.REACTION) &&
				(graph.getCellType(target) == GlobalConstants.SPECIES)) {
				
				int productNum = Integer.parseInt(cell.getId().replace(source.getId()+"__p","").replace("__"+target.getId(),""));
				SpeciesReference product = bioModel.getSBMLDocument().getModel().getReaction(source.getId()).getProduct(productNum);
				//getProductForSpecies(target.getId());
				reactions.productsEditor(bioModel,"OK",target.getId(),product, true, 
						bioModel.getSBMLDocument().getModel().getReaction(source.getId()));
			}
		}
		else if(cellType == GlobalConstants.MODIFIER_REACTION_EDGE){
			
			mxCell source = (mxCell)cell.getSource();
			mxCell target = (mxCell)cell.getTarget();

			int modifierNum = Integer.parseInt(cell.getId().replace(source.getId()+"__m","").replace("__"+target.getId(),""));
			ModifierSpeciesReference modifier = bioModel.getSBMLDocument().getModel().getReaction(target.getId()).getModifier(modifierNum);
			//getModifierForSpecies(source.getId());
			reactions.modifiersEditor(bioModel,"OK",source.getId(),modifier, true, 
					bioModel.getSBMLDocument().getModel().getReaction(target.getId()));
		}
		else if(cellType == GlobalConstants.REACTION){
			
			Reaction r = bioModel.getSBMLDocument().getModel().getReaction(cell.getId());
			reactions.reactionsEditor(bioModel,"OK",cell.getId(),true);
			
			Layout layout = bioModel.getLayout();
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
				reactionGlyph.setId(GlobalConstants.GLYPH+"__"+r.getId());
				reactionGlyph.setReaction(r.getId());
//				SBMLutilities.copyDimensionsIndices(bioModel.getSBMLDocument().getModel().getReaction(r.getId()), 
//						reactionGlyph, "layout:reaction");
				if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
					TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
					textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+r.getId());
					textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+r.getId());
					textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(), r.getId()));
					//SBMLutilities.copyDimensionsIndices(reactionGlyph, textGlyph, "layout:graphicalObject");
				}
			}
			cell.setId(r.getId());
		}
		else if(cellType == GlobalConstants.RULE){
			
			if (editable) {
				String id = rules.ruleEditor("OK",cell.getId());
				Layout layout = bioModel.getLayout();
				if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
					GeneralGlyph generalGlyph = (GeneralGlyph)
							layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
					generalGlyph.setId(GlobalConstants.GLYPH+"__"+id);
					generalGlyph.setMetaidRef(id);
					generalGlyph.unsetReference();
//					SBMLutilities.copyDimensionsIndices(SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument().getModel(),id), 
//							generalGlyph, "layout:metaidRef");
					if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
						TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
						textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
						textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
						textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id));
						//SBMLutilities.copyDimensionsIndices(generalGlyph, textGlyph, "layout:graphicalObject");
					}
				}
				cell.setId(id);
			} else {
				if (cell.getStyle().equals("RULE;")) {
					cell.setStyle("RULE_EXCLUDE;");
					graph.refresh();
					modelEditor.getElementChanges().add(cell.getId());
				} else {
					cell.setStyle("RULE;");
					graph.refresh();
					modelEditor.getElementChanges().remove(cell.getId());
				}
			}
		}		
		else if(cellType == GlobalConstants.VARIABLE || cellType == GlobalConstants.PLACE || cellType == GlobalConstants.BOOLEAN){
			
			String id = parameters.parametersEditor("OK",cell.getId(),
					cellType == GlobalConstants.BOOLEAN, cellType == GlobalConstants.PLACE);
			Layout layout = bioModel.getLayout();
			if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				GeneralGlyph generalGlyph = (GeneralGlyph)
						layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
				generalGlyph.setId(GlobalConstants.GLYPH+"__"+id);
				generalGlyph.setReference(id);
//				SBMLutilities.copyDimensionsIndices(bioModel.getSBMLDocument().getModel().getParameter(id), 
//						generalGlyph, "layout:reference");
				if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
					TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
					textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
					textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
					textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(),id));
//					SBMLutilities.copyDimensionsIndices(generalGlyph, textGlyph, "layout:graphicalObject");
				}
			}
			cell.setId(id);
		}
		else if(cellType == GlobalConstants.CONSTRAINT){
			
			if (editable) {
				String id = constraints.constraintEditor("OK",cell.getId());
				Layout layout = bioModel.getLayout();
				if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
					GeneralGlyph generalGlyph = (GeneralGlyph)
							layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
					generalGlyph.setId(GlobalConstants.GLYPH+"__"+id);
					generalGlyph.setMetaidRef(id);
					generalGlyph.unsetReference();
//					SBMLutilities.copyDimensionsIndices(SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument().getModel(),id), 
//							generalGlyph, "layout:metaidRef");
					if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
						TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
						textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
						textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
						textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id));
//						SBMLutilities.copyDimensionsIndices(generalGlyph, textGlyph, "layout:graphicalObject");
					}
				}
				cell.setId(id);
			} else {
				if (cell.getStyle().equals("CONSTRAINT;")) {
					cell.setStyle("CONSTRAINT_EXCLUDE;");
					graph.refresh();
					modelEditor.getElementChanges().add(cell.getId());
				} else {
					cell.setStyle("CONSTRAINT;");
					graph.refresh();
					modelEditor.getElementChanges().remove(cell.getId());
				}
			}
		}
		else if(cellType == GlobalConstants.EVENT || cellType == GlobalConstants.TRANSITION){
			
			if (editable) {
				String id = events.eventEditor("OK",cell.getId(),cellType == GlobalConstants.TRANSITION);
				Layout layout = bioModel.getLayout();
				if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
					GeneralGlyph generalGlyph = (GeneralGlyph)
							layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
					generalGlyph.setId(GlobalConstants.GLYPH+"__"+id);
					generalGlyph.unsetMetaidRef();
					generalGlyph.setReference(id);
//					SBMLutilities.copyDimensionsIndices(bioModel.getSBMLDocument().getModel().getEvent(id), 
//							generalGlyph, "layout:reference");
					if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
						TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
						textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
						textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
						textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(),id));
//						SBMLutilities.copyDimensionsIndices(generalGlyph, textGlyph, "layout:graphicalObject");
					}
				}
				cell.setId(id);
			} else {
				if (cell.getStyle().equals("EVENT;")) {
					cell.setStyle("EVENT_EXCLUDE;");
					graph.refresh();
					modelEditor.getElementChanges().add(cell.getId());
				} else {
					cell.setStyle("EVENT;");
					graph.refresh();
					modelEditor.getElementChanges().remove(cell.getId());
				}
			}
		}
		else if(cellType == GlobalConstants.COMPARTMENT){
			String id = compartments.compartEditor("OK",cell.getId());
			Layout layout = bioModel.getLayout();
			if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
				compartmentGlyph.setId(GlobalConstants.GLYPH+"__"+id);
				compartmentGlyph.setCompartment(id);
//				SBMLutilities.copyDimensionsIndices(bioModel.getSBMLDocument().getModel().getCompartment(id), 
//						compartmentGlyph, "layout:compartment");
				if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
					TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
					textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
					textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
					textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(),id));
//					SBMLutilities.copyDimensionsIndices(compartmentGlyph, textGlyph, "layout:graphicalObject");
				}
			}
			cell.setId(id);
		}
		else if (cellType == GlobalConstants.COMPONENT) {
			
			if(movieContainer == null)
				modelEditor.launchComponentPanel(cell.getId());
			//if in analysis view, bring up the movie options
			else {
				
				//if we're in analysis view, we need a tabbed pane
				JTabbedPane speciesPane = new JTabbedPane();			
				
				SchemeChooserPanel scPanel = null;
				
				String compModelName = 
					graph.getModelFileName(cell.getId()).replace(".gcm","").replace(".xml","");
				
				ArrayList<String> compSpecies = new ArrayList<String>();
				
				for (int i = 0; i < bioModel.getSBMLCompModel().getListOfSubmodels().size(); ++i) {
					
					if (bioModel.getSBMLCompModel().getListOfSubmodels().get(i).getId().replace("GRID__","").equals(
							compModelName)) {
						
						String s = bioModel.getSBMLCompModel().getListOfSubmodels().get(i).getId();
						
						BioModel subModel = BioModel.createBioModel(bioModel.getPath(), this);
						String extModel = bioModel.getSBMLComp().getListOfExternalModelDefinitions().get(bioModel.getSBMLCompModel().getListOfSubmodels().get(s)
									.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
						try {
              subModel.load(bioModel.getPath() + File.separator + extModel);
              Model submodel = subModel.getSBMLDocument().getModel();
              
              for (int j = 0; j < submodel.getSpeciesCount(); ++j)
                compSpecies.add(cell.getId() + "__" + submodel.getSpecies(j).getId());
              
						} catch (XMLStreamException e) {
		          JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
		          e.printStackTrace();
		        } catch (IOException e) {
		          JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
		          e.printStackTrace();
		        }
						catch (BioSimException e) {
			        JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
			          JOptionPane.ERROR_MESSAGE);
			        e.printStackTrace();
			      }
						
						break;						
					}
				}
				
				JPanel speciesPanel = new JPanel(new GridLayout(compSpecies.size() + 3, 2));		
				speciesPane.addTab("Species", speciesPanel);				
				speciesPanel.add(new JLabel("Mark Interesting Species"));
				speciesPanel.add(new JLabel(""));
				HashMap<String, JCheckBox> checkboxes = new HashMap<String, JCheckBox>();
				HashMap<String, JTextField> thresholds = new HashMap<String, JTextField>();
				
				List<String> interestingSpecies = modelEditor.getReb2Sac().getInterestingSpeciesAsArrayList();
				
				for (String compSpec : compSpecies) {
					
					String[] splitID = compSpec.split("__");
					String noCompID = "";
					
					for (int i = 1; i < splitID.length; ++i) {
						
						noCompID += splitID[i];
						
						if (i < splitID.length - 1)
							noCompID += "__";
					}
					
					checkboxes.put(compSpec, new JCheckBox(noCompID));
					thresholds.put(compSpec, new JTextField(""));
					
					//get saved information
					for (String intSpecies : interestingSpecies) {
						
						//get saved information
						if (intSpecies.split(" ")[0].equals(compSpec)) {
							
							checkboxes.get(compSpec).setSelected(true);
							
							if (intSpecies.split(" ").length > 1)
								thresholds.get(compSpec).setText(intSpecies.split(" ")[1]);
						}
					}
					
					speciesPanel.add(checkboxes.get(compSpec));
					speciesPanel.add(thresholds.get(compSpec));
				}
				
				JCheckBox copyIntSpecies = new JCheckBox("Apply to all modules with this model");
				speciesPanel.add(new JLabel(""));
				speciesPanel.add(copyIntSpecies);
				
				//make sure a simulation file is selected before adding the appearance panel
				if (movieContainer != null && (movieContainer.getTSDParser() != null || movieContainer.getDTSDParser() != null)) {
					scPanel = ModelEditor.getSchemeChooserPanel(cell.getId(), movieContainer, true);
					speciesPane.addTab("Appearance", scPanel);
				}
				else {
					
					JPanel panel = new JPanel(false);
			        JLabel text = new JLabel(
			        		"To modify this submodel's appearances, please select a simulation file.");
			        text.setHorizontalAlignment(SwingConstants.LEFT);
			        text.setVerticalAlignment(SwingConstants.TOP);
			        panel.setLayout(new GridLayout(1, 1));
			        panel.add(text);
				        
					speciesPane.addTab("Appearance", panel);
				}
					
				String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
				
				int okCancel = JOptionPane.showOptionDialog(Gui.frame, speciesPane, "Edit Species",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				
				if (okCancel == JOptionPane.OK_OPTION) {
					
					ArrayList<String> submodelComps = new ArrayList<String>();
					
					if (scPanel !=null) 
						scPanel.updateMovieScheme();
					
					if (copyIntSpecies.isSelected()) {
				
						String[] compIDs = AnnotationUtility.parseArrayAnnotation(bioModel.getSBMLDocument().getModel()
							.getParameter(compModelName + "__locations"));
						
						for (int j = 1; j < compIDs.length; ++j) {
							
							if (compIDs[j].contains("=(")) {
								
								compIDs[j] = compIDs[j].split("=")[0].trim();						
								submodelComps.add(compIDs[j]);
							}
						}
					}
					
					//add interesting species to the list
					for (Map.Entry<String, JCheckBox> checkbox : checkboxes.entrySet()) {
						
						//take off the top-level component from the name
						//this will get added on for all of the components with the
						//same model if the checkbox is selected
						String[] splitID = checkbox.getKey().split("__");
						String noCompID = "";
						
						for (int i = 1; i < splitID.length; ++i) {
							
							noCompID += splitID[i];
							
							if (i < splitID.length - 1)
								noCompID += "__";
						}
						
						if (checkbox.getValue().isSelected()) {
							
							String thresholdText = thresholds.get(checkbox.getKey()).getText();
							
							if (thresholdText.isEmpty() == false)
								modelEditor.getReb2Sac().addInterestingSpecies(checkbox.getKey() + " " + thresholdText);
							else							
								modelEditor.getReb2Sac().addInterestingSpecies(checkbox.getKey());
							
							//loop through submodels and add them to the int species list
							if (copyIntSpecies.isSelected()) {
								
								for (String submodelComp : submodelComps) {
									
									String newID = submodelComp + "__" + noCompID;
									
									//checkbox.getkey() was already added, so skip it
									if (newID.equals(checkbox.getKey()) == false) {
										
										if (thresholdText.isEmpty() == false)
											modelEditor.getReb2Sac().addInterestingSpecies(newID + " " + thresholdText);
										else							
											modelEditor.getReb2Sac().addInterestingSpecies(newID);
									}
								}
							}
						}
						else {
							
							for (String intSpecies : modelEditor.getReb2Sac().getInterestingSpecies()) {
								
								if (intSpecies.split(" ")[0].equals(checkbox.getKey())) {
									modelEditor.getReb2Sac().removeInterestingSpecies(intSpecies);
									break;
								}
							}
							
							if (copyIntSpecies.isSelected()) {
								
								for (String submodelComp : submodelComps) {
									
									String newID = submodelComp + "__" + noCompID;
									
									if (newID.equals(checkbox.getKey()) == false) {
										
										for (String intSpecies : modelEditor.getReb2Sac().getInterestingSpecies()) {
											
											if (intSpecies.split(" ")[0].equals(newID)) {
												modelEditor.getReb2Sac().removeInterestingSpecies(intSpecies);
												break;
											}
										}
									}
								}
							}
						}
					}
					
					//for (String intspec : modelEditor.getReb2Sac().getInterestingSpeciesAsArrayList())
					//	System.err.println(intspec);
					
					//System.err.println("");
						
					
					movieContainer.setIsDirty(true);
				}
			}
		}
		else if (cellType.equals(GlobalConstants.PROMOTER)) {
			
			modelEditor.launchPromoterPanel(cell.getId());
		}
		else if (cellType.equals(GlobalConstants.GRID_RECTANGLE)) {
			
			//if we're in analysis view (ie, movie stuff)
			//prompt the user for coloring species and whatnot for the movie
			if (!editable) {
				
				//if there isn't a TSDParser, the user hasn't selected
				//a TSD file to simulate
				if (movieContainer.getTSDParser() == null && movieContainer.getDTSDParser() == null)
					JOptionPane.showMessageDialog(Gui.frame, "You must choose a simulation file " +
							"before editing grid module properties.");
				else {
					if (cell != null) {
						SchemeChooserPanel.showSchemeChooserPanel(cell.getId(), movieContainer);
					}
				}
			}
		}
		else {
			// it wasn't a type that has an editor.
		}
		
		// refresh everything.
		if (editable)
			graph.buildGraph();
		
		drawGrid();
	}
	
	/**
	 * Displays the right-click menu
	 * @param e
	 */
	protected void showGraphPopupMenu(MouseEvent e) {
		
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),
				graphComponent);
		mxCell cell = (mxCell)(graphComponent.getCellAt(e.getX(), e.getY()));
		EditorPopupMenu menu = new EditorPopupMenu(Schematic.this, bioModel, cell, biosim);
		
		menu.show(graphComponent, pt.x, pt.y);

		e.consume();
	}
	
	
	//GRID
	
	/**
	 * reloads the grid from file
	 * is called by MovieContainer when an SBML view is saved and the analysis view needs to update
	 */
	public void reloadGrid() {
		
		//this reload grid call isn't necessary anymore
		//gcm.reloadGrid();
		grid = modelEditor.getGrid();
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
	 * or when repaint/drawGrid is called
	 * 
	 * is used to draw the grid's selection boxes onto the schematic
	 * the actual grid is part of the biograph, so it's drawn when that's drawn
	 * 
	 * @param g Graphics object 
	 */
	@Override
	public void paintComponent(Graphics g) {
				
		if (movieMode == false) {
			
			super.paintComponent(g);
			
			if (bioModel.isGridEnabled()) {
				
				Component[] comps = this.getComponents();
				int height = 0;
				
				//find the height of the toolbar
				for (Component c : comps) {
					
					if (c.getSize() != null && c.getSize().getHeight() < 50)
						height = (int) c.getSize().getHeight();
				}
				
				grid.setVerticalOffset(height+1);			
				grid.drawGrid(g, graph);
			}
		}
	}
	
	
	//GET METHODS
	
	/**
	 * @return the graph component for the schematic
	 */
	public mxGraphComponent getGraphComponent() {
		return graphComponent;
	}
	
	/**
	 * editable == false means we're in analysis view
	 * @return the editable status of the schematic
	 */
	public boolean getEditable() {
		return editable;
	}
	
	/**
	 * returns the grid for the schematic
	 * @return the grid for the schematic
	 */
	public Grid getGrid() {
		
		return grid;
	}
	
	/**
	 * returns the schematic's gcm
	 * @return the gcm
	 */
	public BioModel getGCM() {
		return bioModel;
	}
	
	/**
	 * returns the schematic's biograph
	 * @return the biograph
	 */
	public BioGraph getGraph() {
		return graph;
	}
	
	/**
	 * return's the gcm2sbml/model editor
	 * @return the gcm2sbml editor
	 */
	public ModelEditor getGCM2SBML() {
		
		return modelEditor;
	}
	
	public void setMovieMode(boolean mode) {
		movieMode = mode;
	}
	
	public ModelPanel getModelPanel() {
		return modelPanel;
	}
	//FRAME PRINTING
	
	
	/**
	 * prints the current graph to the filename passed in
	 * 
	 * @param filename the filename to print the frame to
	 */
	public void outputFrame(String filename, boolean scale) {

		FileOutputStream out = null;		
		
		String path = "";
		
		if (filename.contains(File.separator)) {
			path = GlobalConstants.getPath(filename);
			filename = GlobalConstants.getFilename(filename);
		}
		
		if (filename.contains("."))
			filename = filename.substring(0, filename.indexOf("."));		
		
		filename = path + File.separator + filename + ".jpg";
		
		try {
			out = new FileOutputStream(filename);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		//add some padding to the edges of the graph
		mxRectangle paddedGraphBounds = graph.getGraphBounds();
		paddedGraphBounds.setWidth(paddedGraphBounds.getWidth() + 80);
		paddedGraphBounds.setHeight(paddedGraphBounds.getHeight() + 80);
		paddedGraphBounds.setX(paddedGraphBounds.getX() - 40);
		paddedGraphBounds.setY(paddedGraphBounds.getY() - 40);
		
		BufferedImage image = mxCellRenderer.createBufferedImage(graph, null,
			      1, Color.WHITE, true, paddedGraphBounds);
		
		//put these back where they were
		paddedGraphBounds.setWidth(paddedGraphBounds.getWidth() - 80);
		paddedGraphBounds.setHeight(paddedGraphBounds.getHeight() - 80);
		paddedGraphBounds.setX(paddedGraphBounds.getX() + 40);
		paddedGraphBounds.setY(paddedGraphBounds.getY() + 40);
		
		if (scale == true) {
			
			double maxWidth = 4000;
			double maxHeight = 4000;
		
			double newHeight = image.getHeight();
			double newWidth = image.getWidth();
			
			//scale the image if it's too big (ie, > 1024 x 768)
			//be sure to keep the ratio consistent
			if (image.getHeight() > maxHeight) {
				
				newWidth = newWidth * (maxHeight / newHeight);
				newHeight = maxHeight;
			}
			
			if (newWidth > maxWidth) {			
				
				newHeight = newHeight * (maxWidth / newWidth);
				newWidth = maxWidth;
			}
	
			int w = image.getWidth();
			int h = image.getHeight();
			BufferedImage after = new BufferedImage((int) newWidth, (int) newHeight, BufferedImage.TYPE_INT_RGB);
			AffineTransform at = new AffineTransform();
			at.scale(newHeight / h, newWidth / w);
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			after = scaleOp.filter(image, after);
			
			try {
				ImageIO.write(after, "jpg", out);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			
			try {
				ImageIO.write(image, "jpg", out);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			out.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	//RUBBERBAND CLASS	
	
	/**
	 * this class solely exists for the getBounds method
	 * which doesn't exist in the mxRubberband class, unfortunately
	 */
	public class Rubberband extends mxRubberband {

		public Rubberband(mxGraphComponent arg0) {
			super(arg0);
		}
		
		public Rectangle getBounds() {
			return this.bounds;
		}		
	}
	
	
	
	
	
	
//SOME CODE THAT WAS IN THE MOUSE-CLICKED LISTENER
//I DOUBT IT'LL BE USED IN THE FUTURE, BUT JUST IN CASE, IT'S HERE

//	// this would create a new promoter if an influence didn't already have one. And bring up the editor for it.
//	/*if(editPromoterButton.isSelected()){
//		// If the thing clicked on was an influence, bring up it's promoter window.
//		if(cell.isEdge()){
//			Properties prop = gcm.getInfluences().get(cell.getId());
//			String type = prop.getProperty(GlobalConstants.TYPE);
//			if (!type.equals(GlobalConstants.NOINFLUENCE) && !type.equals(GlobalConstants.COMPLEX)) {
//				String promoter = prop.getProperty(GlobalConstants.PROMOTER);
//				//System.out.print(promoter);
//				if(promoter != null){
//					gcm2sbml.launchPromoterPanel(promoter);
//				}else{
//					PromoterPanel p = gcm2sbml.launchPromoterPanel(null);
//					// set the selected influence to use the given promoter
//					prop = gcm.getInfluences().get(cell.getId());
//					// now rename the influence to incorporate the new promoter
//					if (p.getLastUsedPromoter()!=null) {
//						gcm.changeInfluencePromoter(cell.getId(), prop.getProperty(GlobalConstants.PROMOTER), p.getLastUsedPromoter());
//					}
//				}
//				graph.buildGraph();
//				gcm2sbml.refresh();
//				// no need to set dirty bit because the property window will do it for us.
//				gcm.makeUndoPoint();
//			}
//		}
//	}else */


}
