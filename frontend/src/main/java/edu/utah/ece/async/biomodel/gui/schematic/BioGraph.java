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
/**
 * 
 */
package main.java.edu.utah.ece.async.biomodel.gui.schematic;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import main.java.edu.utah.ece.async.biomodel.annotation.AnnotationUtility;
import main.java.edu.utah.ece.async.biomodel.gui.comp.Grid;
import main.java.edu.utah.ece.async.biomodel.gui.movie.MovieAppearance;
import main.java.edu.utah.ece.async.biomodel.parser.BioModel;
import main.java.edu.utah.ece.async.biomodel.util.SBMLutilities;
import main.java.edu.utah.ece.async.biomodel.util.Utility;
import main.java.edu.utah.ece.async.main.Gui;
import main.java.edu.utah.ece.async.util.GlobalConstants;
 
/**
 * 
 * @author Tyler tpatterson80@gmail.com
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class BioGraph extends mxGraph {

	private double DIS_BETWEEN_NEIGHBORING_EDGES = 35.0;
	private double SECOND_SELF_INFLUENCE_DISTANCE = 20;
	
	private HashMap<String, mxCell> speciesToMxCellMap;
	private HashMap<String, mxCell> reactionsToMxCellMap;
	private HashMap<String, mxCell> compartmentsToMxCellMap;
	private HashMap<String, mxCell> rulesToMxCellMap;
	private HashMap<String, mxCell> constraintsToMxCellMap;
	private HashMap<String, mxCell> eventsToMxCellMap;
	private HashMap<String, mxCell> influencesToMxCellMap;
	private HashMap<String, mxCell> componentsToMxCellMap;
	private HashMap<String, mxCell> componentsConnectionsToMxCellMap;
	private HashMap<String, mxCell> drawnPromoterToMxCellMap;
	private HashMap<String, mxCell> variableToMxCellMap;
	private HashMap<String, mxCell> gridRectangleToMxCellMap;
	
	mxCell cell = new mxCell();
	
	private BioModel bioModel;
	
	public final String CELL_NOT_FULLY_CONNECTED = "cell not fully connected";
	private final String CELL_VALUE_NOT_FOUND = "cell value not found";
	
	// only bother the user about bad promoters once. 
	//This should be improved to happen once per GCM file if this will be a common error.
	public boolean isBuilding = false;
	public boolean dynamic = false;
	
	// Keep track of how many elements did not have positioning info.
	// This allows us to stack them in the topleft corner until they
	// are positioned by the user or a layout algorithm.
	int unpositionedElementCount = 0;
	
	private boolean lema;
	
	private boolean editable;
	
	private ModelEditor modelEditor;
	
	
	/**
	 * constructor
	 * @param bioModel
	 */
	public BioGraph(BioModel bioModel,boolean lema,boolean editable,ModelEditor modelEditor) {
		
		super();
		
		// Turn editing off to prevent mxGraph from letting the user change the 
		// label on the cell. We want to do this using the property windows.
		this.setCellsEditable(false);
		
		this.bioModel = bioModel;
		
		this.lema = lema;
		
		this.editable = editable;
		
		this.modelEditor = modelEditor;
		
		this.initializeMaps();
	
		createStyleSheets();		
	}	
	
	/**
	 * sets the hash maps to null
	 */
	private void initializeMaps(){
		
		speciesToMxCellMap = new HashMap<String, mxCell>();
		reactionsToMxCellMap = new HashMap<String, mxCell>();
		compartmentsToMxCellMap = new HashMap<String, mxCell>();
		rulesToMxCellMap = new HashMap<String, mxCell>();
		constraintsToMxCellMap = new HashMap<String, mxCell>();
		eventsToMxCellMap = new HashMap<String, mxCell>();
		componentsToMxCellMap = new HashMap<String, mxCell>();
		influencesToMxCellMap = new HashMap<String, mxCell>();
		componentsConnectionsToMxCellMap = new HashMap<String, mxCell>();
		drawnPromoterToMxCellMap = new HashMap<String, mxCell>();
		variableToMxCellMap = new HashMap<String, mxCell>();
		gridRectangleToMxCellMap = new HashMap<String, mxCell>();
	}
	
	
	//GRAPH BUILDING
	
	/**
	 * appplies a layout to the graphComponent
	 * 
	 * @param ident
	 * @param graphComponent
	 */
	public void applyLayout(String ident, mxGraphComponent graphComponent){
		Layouting.applyLayout(ident, this, graphComponent);
	}
	
//	private static SBase getGlyph(Layout layout, String glyphId) {
//		SBase sbase = layout.getCompartmentGlyph(glyphId);
//		if (sbase!=null) return sbase;
//		sbase = layout.getSpeciesGlyph(glyphId);
//		if (sbase!=null) return sbase;
//		sbase = layout.getListOfAdditionalGraphicalObjects().get(glyphId);
//		return sbase;
//	}
	
	private void createLayoutConnection(Layout layout,Reaction r,String reactant,String product,String type) {
		ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+reactant+"__"+type+"__"+product);
		if (reactionGlyph!=null) layout.removeReactionGlyph(reactionGlyph);
		reactionGlyph = layout.createReactionGlyph(GlobalConstants.GLYPH+"__"+reactant+"__"+type+"__"+product);
		reactionGlyph.createBoundingBox();
		Dimensions dimension = reactionGlyph.getBoundingBox().createDimensions();
		dimension.setHeight(0);
		dimension.setWidth(0);
		Point point = reactionGlyph.getBoundingBox().createPosition();
		point.setX(this.getSpeciesOrPromoterCell(product).getGeometry().getCenterX());
		point.setY(this.getSpeciesOrPromoterCell(product).getGeometry().getCenterY());
		//SBMLutilities.copyDimensions(r,reactionGlyph);
		SpeciesReferenceGlyph speciesReferenceGlyph = reactionGlyph.createSpeciesReferenceGlyph(GlobalConstants.REFERENCE_GLYPH+"__"+reactant+"__"+type+"__"+product);
		speciesReferenceGlyph.setSpeciesGlyph(GlobalConstants.GLYPH+"__"+product);
		speciesReferenceGlyph.setRole(SpeciesReferenceRole.PRODUCT);
//		SBMLutilities.copyIndices(getGlyph(layout,GlobalConstants.GLYPH+"__"+product), speciesReferenceGlyph, "layout:speciesGlyph");
//		SBMLutilities.copyIndices(reactionGlyph, speciesReferenceGlyph, "layout:id");
		LineSegment lineSegment = speciesReferenceGlyph.createCurve().createLineSegment();
		lineSegment.setStart(new Point(this.getSpeciesOrPromoterCell(reactant).getGeometry().getCenterX(),
				this.getSpeciesOrPromoterCell(reactant).getGeometry().getCenterY()));
		lineSegment.setEnd(new Point(this.getSpeciesOrPromoterCell(product).getGeometry().getCenterX(),
				this.getSpeciesOrPromoterCell(product).getGeometry().getCenterY()));
	}

	private static void addSpeciesReferenceGlyph(Layout layout,mxCell cell,ReactionGlyph reactionGlyph,String reactionId,String speciesId, String role) {
		if (reactionGlyph.getListOfSpeciesReferenceGlyphs().get(GlobalConstants.GLYPH+"__"+reactionId+"__"+role+"__"+speciesId)==null) {
			SpeciesReferenceGlyph speciesReferenceGlyph = reactionGlyph.createSpeciesReferenceGlyph(GlobalConstants.GLYPH+"__"+reactionId+"__"+role+"__"+speciesId);
			speciesReferenceGlyph.setSpeciesGlyph(GlobalConstants.GLYPH+"__"+speciesId);
			speciesReferenceGlyph.setRole(SpeciesReferenceRole.valueOf(role.toUpperCase()));
//			SBMLutilities.copyIndices(getGlyph(layout,GlobalConstants.GLYPH+"__"+speciesId), speciesReferenceGlyph, "layout:speciesGlyph");
//			SBMLutilities.copyIndices(reactionGlyph, speciesReferenceGlyph, "layout:id");
			LineSegment lineSegment = speciesReferenceGlyph.createCurve().createLineSegment();
			lineSegment.setStart(new Point(cell.getSource().getGeometry().getCenterX(),cell.getSource().getGeometry().getCenterY()));
			lineSegment.setEnd(new Point(cell.getTarget().getGeometry().getCenterX(),cell.getTarget().getGeometry().getCenterY()));
		}
	}

	private static void addReferenceGlyph(Layout layout,mxCell cell,GeneralGlyph generalGlyph,String objectId,String refId, String role) {
		if (generalGlyph.getListOfReferenceGlyphs().get(GlobalConstants.GLYPH+"__"+objectId+"__"+role+"__"+refId)==null) {
			ReferenceGlyph referenceGlyph = generalGlyph.createReferenceGlyph(GlobalConstants.GLYPH+"__"+objectId+"__"+role+"__"+refId);
			referenceGlyph.setGlyph(GlobalConstants.GLYPH+"__"+refId);
			referenceGlyph.setRole(role);
//			SBMLutilities.copyIndices(getGlyph(layout,GlobalConstants.GLYPH+"__"+refId), referenceGlyph, "layout:glyph");
//			SBMLutilities.copyIndices(generalGlyph, referenceGlyph, "layout:id");
			LineSegment lineSegment = referenceGlyph.createCurve().createLineSegment();
			lineSegment.setStart(new Point(cell.getSource().getGeometry().getCenterX(),cell.getSource().getGeometry().getCenterY()));
			lineSegment.setEnd(new Point(cell.getTarget().getGeometry().getCenterX(),cell.getTarget().getGeometry().getCenterY()));
		}
	}

	/**
	 * Builds the graph based on the internal representation
	 * @return
	 */	
	public boolean buildGraph() {
		
		this.isBuilding = true;
		this.allowDanglingEdges = false;

		// remove all the cells from the graph (vertices and edges)
		this.removeCells(this.getChildCells(this.getDefaultParent(), true, true));

		initializeMaps();
		
		assert(this.bioModel != null);
		
		// Start an undo transaction
		this.getModel().beginUpdate();
		
		boolean needsPositioning = false;
		unpositionedElementCount = 0;
		
		//createGraphCompartmentFromModel("default");
		
		//put the grid cells in first so that they're below the other cells
		addGridCells();
		bioModel.removeStaleLayout();
		
		Layout layout = bioModel.getLayout();
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getSpeciesCount(); i++) {
			Species s = bioModel.getSBMLDocument().getModel().getSpecies(i);
			if (layout.getSpeciesGlyph(s.getId()) != null) {
				layout.getSpeciesGlyph(s.getId()).setId(GlobalConstants.GLYPH+"__"+s.getId());
				layout.getTextGlyph(s.getId()).setId(GlobalConstants.TEXT_GLYPH+"__"+s.getId());
			}
		}

		Model m = bioModel.getSBMLDocument().getModel();

		// add compartments
		if (!bioModel.isGridEnabled()) {
			for (int i = 0; i < m.getCompartmentCount(); i++) {
				Compartment c = m.getCompartment(i);
				if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+c.getId()) != null) {
					if(createGraphCompartmentFromModel(c.getId())) needsPositioning = true;			
				} else {
					CompartmentGlyph compartmentGlyph = layout.createCompartmentGlyph(GlobalConstants.GLYPH+"__"+c.getId());
					compartmentGlyph.setCompartment(c.getId());
					compartmentGlyph.createBoundingBox();
					compartmentGlyph.getBoundingBox().createDimensions();
					compartmentGlyph.getBoundingBox().createPosition();
					compartmentGlyph.getBoundingBox().getPosition().setX((i*1070)/m.getCompartmentCount());
					compartmentGlyph.getBoundingBox().getPosition().setY(0);
					compartmentGlyph.getBoundingBox().getDimensions().setWidth(1070/m.getCompartmentCount());
					compartmentGlyph.getBoundingBox().getDimensions().setHeight(425);
					//SBMLutilities.copyDimensionsIndices(c, compartmentGlyph, "layout:compartment");
					TextGlyph textGlyph = null;
					if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+c.getId())!=null) {
						textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+c.getId());
					} else {
						textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+c.getId());
					}
					textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+c.getId());
					textGlyph.createBoundingBox();
					textGlyph.getBoundingBox().createDimensions();
					textGlyph.getBoundingBox().createPosition();
					textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+c.getId());
					textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(),c.getId()));
					textGlyph.setBoundingBox(compartmentGlyph.getBoundingBox().clone());
					//SBMLutilities.copyDimensionsIndices(compartmentGlyph, textGlyph, "layout:graphicalObject");
					if(createGraphCompartmentFromModel(c.getId()))
						needsPositioning = true;			
				}
			}
		}

		// add species
		for(String sp : bioModel.getSpecies()){
			
			if (AnnotationUtility.parseGridAnnotation(bioModel.getSBMLDocument().getModel().getSpecies(sp))!=null) 
				continue;
			
			if(createGraphSpeciesFromModel(sp))
				needsPositioning = true;
		}
		
		// add reactions
		for (int i = 0; i < m.getReactionCount(); i++) {
			
			Reaction r = m.getReaction(i);
			if (BioModel.isDegradationReaction(r)) continue;
			if (BioModel.isDiffusionReaction(r)) continue;
			if (BioModel.isProductionReaction(r)) continue;
			if (BioModel.isComplexReaction(r)) continue;
			if (BioModel.isConstitutiveReaction(r)) continue;
			if (BioModel.isGridReaction(r)) continue;
			
			if (layout.getReactionGlyph(r.getId()) != null) {
				layout.getReactionGlyph(r.getId()).setId(GlobalConstants.GLYPH+"__"+r.getId());
				layout.getTextGlyph(r.getId()).setId(GlobalConstants.TEXT_GLYPH+"__"+r.getId());
			}
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId()) != null) {
				if(createGraphReactionFromModel(r.getId()))	needsPositioning = true;			
			} else {
				if (r.getModifierCount() > 0 || (r.getReactantCount()>1 && r.getProductCount()>1) ||
					r.getReactantCount()==0 || r.getProductCount()==0) {
					if(createGraphReactionFromModel(r.getId()))
						needsPositioning = true;			
				}
			}
		}
		
		// add rules
		for (int i = 0; i < m.getRuleCount(); i++) {
			Rule rule = m.getRule(i);
			if (rule.getMetaId().endsWith("_"+GlobalConstants.RATE)) continue;
			if (SBMLutilities.getVariable(rule) != null &&
					SBMLutilities.getVariable(rule).startsWith(GlobalConstants.TRIGGER + "_")) continue;
			if (layout.getReactionGlyph(rule.getMetaId()) != null) {
				layout.getReactionGlyph(rule.getMetaId()).setId(GlobalConstants.GLYPH+"__"+rule.getMetaId());
				layout.getTextGlyph(rule.getMetaId()).setId(GlobalConstants.TEXT_GLYPH+"__"+rule.getMetaId());
			}
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+rule.getMetaId())!=null) {
				ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+rule.getMetaId());
				layout.getListOfReactionGlyphs().remove(GlobalConstants.GLYPH+"__"+rule.getMetaId());
				GeneralGlyph generalGlyph = layout.createGeneralGlyph(reactionGlyph.getId());
				generalGlyph.createBoundingBox();
				generalGlyph.getBoundingBox().createDimensions();
				generalGlyph.getBoundingBox().createPosition();
				generalGlyph.unsetReference();
				generalGlyph.setMetaidRef(reactionGlyph.getReaction());
				generalGlyph.setBoundingBox(reactionGlyph.getBoundingBox().clone());
			}
			if(createGraphRuleFromModel(rule.getMetaId())) needsPositioning = true;			
		}
		
		// add constraints
		for (int i = 0; i < m.getConstraintCount(); i++) {
			Constraint constraint = m.getConstraint(i);
			if (constraint.getMetaId().equals(GlobalConstants.FAIL_TRANSITION)) continue;
			if (layout.getReactionGlyph(constraint.getMetaId()) != null) {
				layout.getReactionGlyph(constraint.getMetaId()).setId(GlobalConstants.GLYPH+"__"+constraint.getMetaId());
				layout.getTextGlyph(constraint.getMetaId()).setId(GlobalConstants.TEXT_GLYPH+"__"+constraint.getMetaId());
			}
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+constraint.getMetaId())!=null) {
				ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+constraint.getMetaId());
				layout.getListOfReactionGlyphs().remove(GlobalConstants.GLYPH+"__"+constraint.getMetaId());
				GeneralGlyph generalGlyph = layout.createGeneralGlyph(reactionGlyph.getId());
				generalGlyph.createBoundingBox();
				generalGlyph.getBoundingBox().createDimensions();
				generalGlyph.getBoundingBox().createPosition();
				generalGlyph.unsetReference();
				generalGlyph.setMetaidRef(reactionGlyph.getReaction());
				generalGlyph.setBoundingBox(reactionGlyph.getBoundingBox().clone());
			}
			if(createGraphConstraintFromModel(constraint.getMetaId())) needsPositioning = true;			
		}
		
		// add events
		for (int i = 0; i < m.getEventCount(); i++) {
			Event event = m.getEvent(i);
			if (layout.getReactionGlyph(event.getId()) != null) {
				layout.getReactionGlyph(event.getId()).setId(GlobalConstants.GLYPH+"__"+event.getId());
				layout.getTextGlyph(event.getId()).setId(GlobalConstants.TEXT_GLYPH+"__"+event.getId());
			}
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+event.getId())!=null) {
				ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+event.getId());
				layout.getListOfReactionGlyphs().remove(GlobalConstants.GLYPH+"__"+event.getId());
				GeneralGlyph generalGlyph = layout.createGeneralGlyph(reactionGlyph.getId());
				generalGlyph.createBoundingBox();
				generalGlyph.getBoundingBox().createDimensions();
				generalGlyph.getBoundingBox().createPosition();
				generalGlyph.setReference(reactionGlyph.getReaction());
				generalGlyph.setBoundingBox(reactionGlyph.getBoundingBox().clone());
			}
			if (SBMLutilities.isTransition(event)) {
				if(createGraphTransitionFromModel(event.getId())) needsPositioning = true;			
			} else {
				if(createGraphEventFromModel(event.getId())) needsPositioning = true;		
			}
		}

		// add all variables
		if (!bioModel.isGridEnabled()) {
			for (int i = 0; i < m.getParameterCount(); i++) {
				Parameter p = m.getParameter(i);
				if (p.getId().equals(GlobalConstants.FAIL)) continue;
				if (p.getId().endsWith("_" + GlobalConstants.RATE)) continue;
				if (p.getId().startsWith(GlobalConstants.TRIGGER + "_")) continue;
				if (!p.getConstant()) {
					String id = p.getId();
					if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
						SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id);
						layout.getListOfSpeciesGlyphs().remove(GlobalConstants.GLYPH+"__"+id);
						GeneralGlyph generalGlyph = layout.createGeneralGlyph(speciesGlyph.getId());
						generalGlyph.createBoundingBox();
						generalGlyph.getBoundingBox().createDimensions();
						generalGlyph.getBoundingBox().createPosition();
						generalGlyph.setReference(speciesGlyph.getSpecies());
						generalGlyph.setBoundingBox(speciesGlyph.getBoundingBox().clone());
					}
					if (SBMLutilities.isPlace(p)) {
						if(createGraphPlaceFromModel(p.getId(),p.getValue()==1.0))
							needsPositioning = true;
					} else if (SBMLutilities.isBoolean(p)) {
						if(createGraphBooleanFromModel(p.getId(),p.getValue()==1.0))
							needsPositioning = true;
					} else {
						if(createGraphVariableFromModel(p.getId()))
							needsPositioning = true;
					}
				}
			}
		}
		
		// add all the drawn promoters
		for(String prom : bioModel.getPromoters()){
			if (bioModel.isPromoterExplicit(prom)) {
				if(createGraphDrawnPromoterFromModel(prom))
					needsPositioning = true;
			}
		}
		
		// add all components
		if (bioModel.isGridEnabled()) {
			
			for (int i = 0; i < m.getParameterCount(); ++i) {
				
				if (m.getParameter(i).getId().contains("__locations")) {
					
					String[] splitAnnotation = AnnotationUtility.parseArrayAnnotation(m.getParameter(i));
									
					for (int j = 1; j < splitAnnotation.length; ++j) {
						
						splitAnnotation[j] = splitAnnotation[j].trim();						
						String submodelID = splitAnnotation[j].split("=")[0];
						
						if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+submodelID)!=null) {
							CompartmentGlyph compartmentGlyph = 
									layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+submodelID);
							layout.getListOfCompartmentGlyphs().remove(GlobalConstants.GLYPH+"__"+submodelID);
							GeneralGlyph generalGlyph = layout.createGeneralGlyph(compartmentGlyph.getId());
							generalGlyph.createBoundingBox();
							generalGlyph.getBoundingBox().createDimensions();
							generalGlyph.getBoundingBox().createPosition();
							generalGlyph.setReference(compartmentGlyph.getCompartment().replace(GlobalConstants.GLYPH+"__",""));
							generalGlyph.setBoundingBox(compartmentGlyph.getBoundingBox().clone());
						}
						if (createGraphComponentFromModel(submodelID))
							needsPositioning = true;
					}					
				}				
			}
			
//			for (long i = 0; i < layout.getListOfCompartmentGlyphs().size(); i++) {
//				
//				String comp = layout.getCompartmentGlyph(i).getId();
//				
//				if (!comp.startsWith(GlobalConstants.GLYPH+"__")) {
//					layout.getCompartmentGlyph(i).setId(GlobalConstants.GLYPH+"__"+comp);
//				} else {
//					comp = comp.replace(GlobalConstants.GLYPH+"__","");
//				}
//				
//				//these are not meant to be displayed
//				//if (comp.contains("GRID__"))
//				//	continue;
//				
//				if (createGraphComponentFromModel(comp))
//					needsPositioning = true;
//			}
		} 
		else {
			CompModelPlugin sbmlCompModel = bioModel.getSBMLCompModel();
			
			for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++) {
			
				String comp = sbmlCompModel.getListOfSubmodels().get(i).getId();
			
				//String comp = gcm.getSBMLCompModel().getListOfSubmodels().get(i).getId();
			
				//these are not meant to be displayed
				//if (comp.contains("GRID__"))
				//	continue;

				if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+comp)!=null) {
					CompartmentGlyph compartmentGlyph = 
							layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+comp);
					layout.getListOfCompartmentGlyphs().remove(GlobalConstants.GLYPH+"__"+comp);
					GeneralGlyph generalGlyph = layout.createGeneralGlyph(compartmentGlyph.getId());
					generalGlyph.createBoundingBox();
					generalGlyph.getBoundingBox().createDimensions();
					generalGlyph.getBoundingBox().createPosition();
					generalGlyph.setReference(compartmentGlyph.getCompartment());
					generalGlyph.setBoundingBox(compartmentGlyph.getBoundingBox().clone());
				}
				if (createGraphComponentFromModel(comp))
					needsPositioning = true;
			}
		}
		
		boolean needsRedrawn = false;
		
		// add all the edges. 
		for (int i = 0; i < m.getReactionCount(); i++) {
			
			Reaction r = m.getReaction(i);
			if (BioModel.isGridReaction(r)) continue;
			
			if (BioModel.isComplexReaction(r)) {
				for (int j = 0; j < r.getReactantCount(); j++) {
					String reactant = r.getReactant(j).getSpecies();
					String product = r.getProduct(0).getSpecies();
					String id = reactant + "+>" + product;
					this.insertEdge(this.getDefaultParent(), id, "", this.getSpeciesCell(reactant), 
							this.getSpeciesCell(product));
					String style = "COMPLEX";
					mxCell cell = this.getInfluence(id);
					cell.setStyle(style);
					createLayoutConnection(layout,r,reactant,product,GlobalConstants.COMPLEX);
				}
			} else if (BioModel.isProductionReaction(r)) {
				String promoterId = SBMLutilities.getPromoterId(r);
				if (bioModel.isPromoterExplicit(promoterId)) {
					for (int j = 0; j < r.getProductCount(); j++) {
						if (BioModel.isMRNASpecies(bioModel.getSBMLDocument(), 
								r.getProduct(j).getSpeciesInstance())) continue;
						String product = r.getProduct(j).getSpecies();
						String id = promoterId + "->" + product;
						mxCell production = (mxCell)this.insertEdge(this.getDefaultParent(), id, "", 
								this.getDrawnPromoterCell(promoterId),this.getSpeciesCell(product));
						production.setStyle("PRODUCTION");
						createLayoutConnection(layout,r,promoterId,product,GlobalConstants.PRODUCTION);
					}
					for (int j = 0; j < r.getModifierCount(); j++) {
						if (BioModel.isRepressor(r.getModifier(j))) {
							String repressor = r.getModifier(j).getSpecies();
							String id = repressor + "-|" + promoterId;
							this.insertEdge(this.getDefaultParent(), id, "", 
									this.getSpeciesCell(repressor),this.getDrawnPromoterCell(promoterId));
							String style = "REPRESSION";
							mxCell cell = this.getInfluence(id);
							cell.setStyle(style);
							createLayoutConnection(layout,r,repressor,promoterId,GlobalConstants.REPRESSION);
						} else if (BioModel.isActivator(r.getModifier(j))) {
							String activator = r.getModifier(j).getSpecies();
							String id = activator + "->" + promoterId;
							this.insertEdge(this.getDefaultParent(), id, "", 
									this.getSpeciesCell(activator),this.getDrawnPromoterCell(promoterId));
							String style = "ACTIVATION";
							mxCell cell = this.getInfluence(id);
							cell.setStyle(style);
							createLayoutConnection(layout,r,activator,promoterId,GlobalConstants.ACTIVATION);
						} else if (BioModel.isRegulator(r.getModifier(j))) {
							String regulator = r.getModifier(j).getSpecies();
							String id = regulator + "-|" + promoterId;
							this.insertEdge(this.getDefaultParent(), id, "", 
									this.getSpeciesCell(regulator),this.getDrawnPromoterCell(promoterId));
							String style = "REPRESSION";
							mxCell cell = this.getInfluence(id);
							cell.setStyle(style);
							id = regulator + "->" + promoterId;
							this.insertEdge(this.getDefaultParent(), id, "", 
									this.getSpeciesCell(regulator),this.getDrawnPromoterCell(promoterId));
							style = "ACTIVATION";
							cell = this.getInfluence(id);
							cell.setStyle(style);
							createLayoutConnection(layout,r,regulator,promoterId,GlobalConstants.REPRESSION);
							createLayoutConnection(layout,r,regulator,promoterId,GlobalConstants.ACTIVATION);
						} else if (BioModel.isNeutral(r.getModifier(j))) {
							String regulator = r.getModifier(j).getSpecies();
							String id = regulator + "x>" + promoterId;
							this.insertEdge(this.getDefaultParent(), id, "", 
									this.getSpeciesCell(regulator),this.getDrawnPromoterCell(promoterId));
							String style = "NOINFLUENCE";
							mxCell cell = this.getInfluence(id);
							cell.setStyle(style);
							createLayoutConnection(layout,r,regulator,promoterId,GlobalConstants.NOINFLUENCE);						}
					}
				} else {
					for (int j = 0; j < r.getModifierCount(); j++) {
						for (int k = 0; k < r.getProductCount(); k++) {
							if (BioModel.isRepressor(r.getModifier(j))) {
								String repressor = r.getModifier(j).getSpecies();
								String product = r.getProduct(k).getSpecies();
								String id = repressor + "-|" + product + "," + promoterId;
								this.insertEdge(this.getDefaultParent(), id, "", 
										this.getSpeciesCell(repressor),this.getSpeciesCell(product));
								String style = "REPRESSION";
								mxCell cell = this.getInfluence(id);
								cell.setStyle(style);
								cell.setValue(promoterId);
								createLayoutConnection(layout,r,repressor,product,GlobalConstants.REPRESSION);
							} else if (BioModel.isActivator(r.getModifier(j))) {
								String activator = r.getModifier(j).getSpecies();
								String product = r.getProduct(k).getSpecies();
								String id = activator + "->" + product + "," + promoterId;
								this.insertEdge(this.getDefaultParent(), id, "", 
										this.getSpeciesCell(activator),this.getSpeciesCell(product));
								String style = "ACTIVATION";
								mxCell cell = this.getInfluence(id);
								cell.setStyle(style);
								cell.setValue(promoterId);
								createLayoutConnection(layout,r,activator,product,GlobalConstants.REPRESSION);
							} else if (BioModel.isNeutral(r.getModifier(j))) {
								String regulator = r.getModifier(j).getSpecies();
								String product = r.getProduct(k).getSpecies();
								String id = regulator + "x>" + product + "," + promoterId;
								this.insertEdge(this.getDefaultParent(), id, "", 
										this.getSpeciesCell(regulator),this.getSpeciesCell(product));
								String style = "NOINFLUENCE";
								mxCell cell = this.getInfluence(id);
								cell.setStyle(style);
								cell.setValue(promoterId);
								createLayoutConnection(layout,r,regulator,product,GlobalConstants.NOINFLUENCE);
							} else if (BioModel.isRegulator(r.getModifier(j))) {
								String regulator = r.getModifier(j).getSpecies();
								String product = r.getProduct(k).getSpecies();
								String id = regulator + "-|" + product + "," + promoterId;
								this.insertEdge(this.getDefaultParent(), id, "", 
										this.getSpeciesCell(regulator),this.getSpeciesCell(product));
								String style = "REPRESSION";
								mxCell cell = this.getInfluence(id);
								cell.setStyle(style);
								cell.setValue(promoterId);
								id = regulator + "->" + product + "," + promoterId;
								this.insertEdge(this.getDefaultParent(), id, "", 
										this.getSpeciesCell(regulator),this.getSpeciesCell(product));
								style = "ACTIVATION";
								cell = this.getInfluence(id);
								cell.setStyle(style);
								cell.setValue(promoterId);
								createLayoutConnection(layout,r,regulator,product,GlobalConstants.ACTIVATION);
								createLayoutConnection(layout,r,regulator,product,GlobalConstants.REPRESSION);
							}
						}
					}
				}
			}
		}

		//add reactions
		for (int i = 0; i < m.getReactionCount(); i++) {
			
			Reaction r = m.getReaction(i);
			if (BioModel.isDegradationReaction(r)) continue;
			if (BioModel.isDiffusionReaction(r)) continue;
			if (BioModel.isProductionReaction(r)) continue;
			if (BioModel.isComplexReaction(r)) continue;
			if (BioModel.isConstitutiveReaction(r)) continue;
			if (BioModel.isGridReaction(r)) continue;
			ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId());
			if (reactionGlyph != null) {
				while (reactionGlyph.getListOfSpeciesReferenceGlyphs().size() > 0) 
					reactionGlyph.getListOfSpeciesReferenceGlyphs().remove(0);
				for (int j = 0; j < r.getReactantCount(); j++) {
					
					SpeciesReference s = r.getReactant(j);
					mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
							s.getSpecies() + "__r" + j + "__"  + r.getId(), "", 
							this.getSpeciesOrPromoterCell(s.getSpecies()), 
							this.getReactionsCell(r.getId()));

					if (r.getReversible()) {
						
						if (s.getStoichiometry() != 1.0)
							cell.setValue(s.getStoichiometry()+",r");
						else 
							cell.setValue("r");
						
						cell.setStyle("REV_REACTION_EDGE");
					} 
					else {
						if (s.getStoichiometry() != 1.0)
							cell.setValue(s.getStoichiometry());
						
						cell.setStyle("REACTION_EDGE");
					}
					String reactant = s.getSpecies();
					addSpeciesReferenceGlyph(layout,cell,reactionGlyph,r.getId(),reactant,"substrate");
				}
				
				for (int j = 0; j < r.getModifierCount(); j++) {
					
					ModifierSpeciesReference s = r.getModifier(j);
					mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
							s.getSpecies() + "__m" + j + "__" + r.getId(), "", 
							this.getSpeciesOrPromoterCell(s.getSpecies()), 
							this.getReactionsCell(r.getId()));

					if (r.getReversible())
						cell.setValue("m");
					
					cell.setStyle("MODIFIER_REACTION_EDGE");
					String modifier = s.getSpecies();
					addSpeciesReferenceGlyph(layout,cell,reactionGlyph,r.getId(),modifier,"modifier");
				}
				
				for (int k = 0; k < r.getProductCount(); k++) {
					
					SpeciesReference s = r.getProduct(k);
					mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
							r.getId() + "__p" + k + "__" + s.getSpecies(), "", 
							this.getReactionsCell(r.getId()),
							this.getSpeciesOrPromoterCell(s.getSpecies()));
					
					if (r.getReversible()) {
						
						if (s.getStoichiometry() != 1.0)
							cell.setValue(s.getStoichiometry()+",p");
						else 
							cell.setValue("p");
						
						cell.setStyle("REV_REACTION_EDGE");
					} 
					else {
						
						if (s.getStoichiometry() != 1.0)
							cell.setValue(s.getStoichiometry());
						
						cell.setStyle("REACTION_EDGE");
					}
					String product = s.getSpecies();
					addSpeciesReferenceGlyph(layout,cell,reactionGlyph,r.getId(),product,"product");
				}
				
				if (r.isSetKineticLaw()) { 
					String initStr = SBMLutilities.myFormulaToString(r.getKineticLaw().getMath());
					String[] vars = SBMLutilities.getSupport(initStr);
					for (int j = 0; j < vars.length; j++) {
						Parameter parameter = m.getParameter(vars[j]);
						if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									parameter.getId() + "__" + r.getId(), "", 
									this.getVariableCell(parameter.getId()), 
									this.getReactionsCell(r.getId()));
							cell.setStyle("REACTION_EDGE");
							//addSpeciesReferenceGlyph(cell,reactionGlyph,r.getId(),parameter.getId(),"substrate");
							GeneralGlyph generalGlyph = 
									(GeneralGlyph)layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+parameter.getId());
							addReferenceGlyph(layout,cell,generalGlyph,parameter.getId(),r.getId(),"product");
						}
					}
				}
			} 
			else {
				for (int j = 0; j < r.getReactantCount(); j++) {
					SpeciesReference s1 = r.getReactant(j);
					String reactant = s1.getSpecies();
					
					for (int k = 0; k < r.getProductCount(); k++) {
						
						SpeciesReference s2 = r.getProduct(k);
						String product = s2.getSpecies();
						mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
								s1.getSpecies() + "_" + r.getId() + "_" + s2.getSpecies(), "", 
								this.getSpeciesOrPromoterCell(s1.getSpecies()), 
								this.getSpeciesOrPromoterCell(s2.getSpecies()));
						
						cell.setValue(r.getId());
						
						if (r.getReversible()) {
							cell.setStyle("REV_REACTION_EDGE");
						} 
						else {
							cell.setStyle("REACTION_EDGE");
						}
						createLayoutConnection(layout,r,reactant,product,GlobalConstants.REACTION);
					}
				}
			}
		}

		//add rules
		for (int i = 0; i < m.getRuleCount(); i++) {
			Rule r = m.getRule(i);
			if (r.getMetaId().endsWith("_"+GlobalConstants.RATE)) continue;
			if (SBMLutilities.getVariable(r) != null &&
					SBMLutilities.getVariable(r).startsWith(GlobalConstants.TRIGGER+"_")) continue;
			GeneralGlyph generalGlyph = (GeneralGlyph)
					layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+r.getMetaId());
			if (generalGlyph != null) {
				while (generalGlyph.getListOfReferenceGlyphs().size() > 0) 
					generalGlyph.getListOfReferenceGlyphs().remove(0);
				String initStr = SBMLutilities.myFormulaToString(r.getMath());
				String[] vars = SBMLutilities.getSupport(initStr);
				for (int j = 0; j < vars.length; j++) {
					Species species = m.getSpecies(vars[j]);
					if (species != null) {
						mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
								species.getId() + "__" + r.getMetaId(), "", 
								this.getSpeciesOrPromoterCell(species.getId()), 
								this.getRulesCell(r.getMetaId()));
						cell.setStyle("RULE_EDGE");
						addReferenceGlyph(layout,cell,generalGlyph,r.getMetaId(),species.getId(),"substrate");
					} else {
						Parameter parameter = m.getParameter(vars[j]);
						if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									parameter.getId() + "__" + r.getMetaId(), "", 
									this.getVariableCell(parameter.getId()), 
									this.getRulesCell(r.getMetaId()));
							cell.setStyle("RULE_EDGE");
							addReferenceGlyph(layout,cell,generalGlyph,r.getMetaId(),parameter.getId(),"substrate");
						}
					}
				}
				// Add variable
				if (r.isAssignment() || r.isRate()) {
					Species species = m.getSpecies(SBMLutilities.getVariable(r));
					if (species != null) {
						mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
								r.getMetaId() + "__" + species.getId(), "", 
								this.getRulesCell(r.getMetaId()),
								this.getSpeciesOrPromoterCell(species.getId()));
						cell.setStyle("RULE_EDGE");
						addReferenceGlyph(layout,cell,generalGlyph,r.getMetaId(),species.getId(),"product");
					} else {
						Parameter parameter = m.getParameter(SBMLutilities.getVariable(r));
						if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									r.getMetaId() + "__" + parameter.getId(), "", 
									this.getRulesCell(r.getMetaId()),
									this.getVariableCell(parameter.getId()));
							cell.setStyle("RULE_EDGE");
							addReferenceGlyph(layout,cell,generalGlyph,r.getMetaId(),parameter.getId(),"product");
						}
					}
				}
			} 
		}
		
		// add constraints
		for (int i = 0; i < m.getConstraintCount(); i++) {
			Constraint c = m.getConstraint(i);
			if (m.getMetaId().equals(GlobalConstants.FAIL_TRANSITION)) continue;
			GeneralGlyph generalGlyph = (GeneralGlyph)
					layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+c.getMetaId());
			if (generalGlyph != null) {
				while (generalGlyph.getListOfReferenceGlyphs().size() > 0) 
					generalGlyph.getListOfReferenceGlyphs().remove(0);
				String initStr = SBMLutilities.myFormulaToString(c.getMath());
				String[] vars = SBMLutilities.getSupport(initStr);
				HashSet<String> variables = new HashSet<String>();
				for (String var: vars) {
					variables.add(var);
				}
				for (String var : variables) {
					Species species = m.getSpecies(var);
					if (species != null) {
						mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
								species.getId() + "__" + c.getMetaId(), "", 
								this.getSpeciesOrPromoterCell(species.getId()), 
								this.getConstraintsCell(c.getMetaId()));
						cell.setStyle("CONSTRAINT_EDGE");
						addReferenceGlyph(layout,cell,generalGlyph,c.getMetaId(),species.getId(),"substrate");
					} else {
						Parameter parameter = m.getParameter(var);
						if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									parameter.getId() + "__" + c.getMetaId(), "", 
									this.getVariableCell(parameter.getId()), 
									this.getConstraintsCell(c.getMetaId()));
							cell.setStyle("CONSTRAINT_EDGE");
							addReferenceGlyph(layout,cell,generalGlyph,c.getMetaId(),parameter.getId(),"substrate");
						}
					}
				}
			} 
		}
		
		// add event edges
		for (int i = 0; i < m.getEventCount(); i++) {
			Event e = m.getEvent(i);
			boolean isTransition = SBMLutilities.isTransition(e);
			GeneralGlyph generalGlyph = (GeneralGlyph)
					layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+e.getId());
			if (generalGlyph != null) {
				while (generalGlyph.getListOfReferenceGlyphs().size() > 0) 
					generalGlyph.getListOfReferenceGlyphs().remove(0);
				if (!isTransition && e.isSetTrigger()) {
					String initStr = SBMLutilities.myFormulaToString(e.getTrigger().getMath());
					String[] vars = SBMLutilities.getSupport(initStr);
					for (int j = 0; j < vars.length; j++) {
						Species species = m.getSpecies(vars[j]);
						if (species != null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									species.getId() + "__" + e.getId(), "", 
									this.getSpeciesOrPromoterCell(species.getId()), 
									this.getEventsCell(e.getId()));
							cell.setStyle("EVENT_EDGE");
							addReferenceGlyph(layout,cell,generalGlyph,e.getId(),species.getId(),"substrate");
						} else {
							Parameter parameter = m.getParameter(vars[j]);
							if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
								mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
										parameter.getId() + "__" + e.getId(), "", 
										this.getVariableCell(parameter.getId()), 
										this.getEventsCell(e.getId()));
								cell.setStyle("EVENT_EDGE");
								addReferenceGlyph(layout,cell,generalGlyph,e.getId(),parameter.getId(),"substrate");
							}
						}
					}
				}
				if (!isTransition && e.isSetDelay()) {
					String initStr = SBMLutilities.myFormulaToString(e.getDelay().getMath());
					String[] vars = SBMLutilities.getSupport(initStr);
					for (int j = 0; j < vars.length; j++) {
						Species species = m.getSpecies(vars[j]);
						if (species != null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									species.getId() + "__" + e.getId(), "", 
									this.getSpeciesOrPromoterCell(species.getId()), 
									this.getEventsCell(e.getId()));
							cell.setStyle("EVENT_EDGE");
							addReferenceGlyph(layout,cell,generalGlyph,e.getId(),species.getId(),"substrate");
						} else {
							Parameter parameter = m.getParameter(vars[j]);
							if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
								mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
										parameter.getId() + "__" + e.getId(), "", 
										this.getVariableCell(parameter.getId()), 
										this.getEventsCell(e.getId()));
								cell.setStyle("EVENT_EDGE");
								addReferenceGlyph(layout,cell,generalGlyph,e.getId(),parameter.getId(),"substrate");
							}
						}
					}
				}
				if (!isTransition && e.isSetPriority()) {
					String initStr = SBMLutilities.myFormulaToString(e.getPriority().getMath());
					String[] vars = SBMLutilities.getSupport(initStr);
					for (int j = 0; j < vars.length; j++) {
						Species species = m.getSpecies(vars[j]);
						if (species != null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									species.getId() + "__" + e.getId(), "", 
									this.getSpeciesOrPromoterCell(species.getId()), 
									this.getEventsCell(e.getId()));
							cell.setStyle("EVENT_EDGE");
							addReferenceGlyph(layout,cell,generalGlyph,e.getId(),species.getId(),"substrate");
						} else {
							Parameter parameter = m.getParameter(vars[j]);
							if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
								mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
										parameter.getId() + "__" + e.getId(), "", 
										this.getVariableCell(parameter.getId()), 
										this.getEventsCell(e.getId()));
								cell.setStyle("EVENT_EDGE");
								addReferenceGlyph(layout,cell,generalGlyph,e.getId(),parameter.getId(),"substrate");
							}
						}
					}
				}
				// Add variable
				for (int k = 0; k < e.getEventAssignmentCount(); k++) {
					EventAssignment ea = e.getListOfEventAssignments().get(k);
					String initStr = SBMLutilities.myFormulaToString(ea.getMath());
					if (!isTransition) {
						String[] vars = SBMLutilities.getSupport(initStr);
						for (int j = 0; j < vars.length; j++) {
							Species species = m.getSpecies(vars[j]);
							if (species != null) {
								mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
										species.getId() + "__" + e.getId(), "", 
										this.getSpeciesOrPromoterCell(species.getId()), 
										this.getEventsCell(e.getId()));
								cell.setStyle("EVENT_EDGE");
								addReferenceGlyph(layout,cell,generalGlyph,e.getId(),species.getId(),"substrate");
							}else {
								Parameter parameter = m.getParameter(vars[j]);
								if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
									mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
											parameter.getId() + "__" + e.getId(), "", 
											this.getVariableCell(parameter.getId()), 
											this.getEventsCell(e.getId()));
									cell.setStyle("EVENT_EDGE");
									addReferenceGlyph(layout,cell,generalGlyph,e.getId(),parameter.getId(),"substrate");
								}
							}
						}
					}
					Species species = m.getSpecies(ea.getVariable());
					if (!isTransition && species != null) {
						mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
								e.getId() + "__" + species.getId(), "", 
								this.getEventsCell(e.getId()),
								this.getSpeciesOrPromoterCell(species.getId()));
						cell.setStyle("EVENT_EDGE");
						addReferenceGlyph(layout,cell,generalGlyph,e.getId(),species.getId(),"product");
					} else if (species==null){
						Parameter parameter = m.getParameter(ea.getVariable());
						if (parameter != null) {
							boolean isPlace = SBMLutilities.isPlace(parameter);
							if (!isTransition || (isPlace && initStr.equals("1"))) {
								if (this.getVariableCell(parameter.getId())!=null) {
									mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
											e.getId() + "__" + parameter.getId(), "", 
											this.getEventsCell(e.getId()),
											this.getVariableCell(parameter.getId()));
									if (isTransition && isPlace) {
										cell.setStyle("TRANSITION_EDGE");
									} else {
										cell.setStyle("EVENT_EDGE");
									}
									addReferenceGlyph(layout,cell,generalGlyph,e.getId(),parameter.getId(),"product");
								}
							} else if (isPlace && initStr.equals("0")) {
								if (this.getVariableCell(parameter.getId())!=null) {
									mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
											e.getId() + "__" + parameter.getId(), "", 
											this.getVariableCell(parameter.getId()),
											this.getEventsCell(e.getId()));
									cell.setStyle("TRANSITION_EDGE");
									addReferenceGlyph(layout,cell,generalGlyph,e.getId(),parameter.getId(),"substrate");
								}							
							}
						}
					}
				}
			} 
		}
		
		addEdgeOffsets();
		this.getModel().endUpdate();
		
		this.isBuilding = false;
		
		// if we found any incorrectly marked promoters we need to redraw. Do so now.
		// The promoters should all pass the second time.
		if(needsRedrawn){
			return buildGraph();
		}

		return needsPositioning;
	}
	
	/**
	 * Loop through all the edges and add control points to reposition them
	 * if they are laying over the top of any other edges.
	 */
	public void addEdgeOffsets(){
		
		// Make a hash where the key is a string built from the ids of the source and destination
		// of all the edges. The source and destination will be sorted so that the same two
		// source-destination pair will always map to the same key. The value is a list
		// of edges. That way if there are ever more then one edge between pairs, 
		// we can modify the geometry so they don't overlap.
		HashMap<String, Vector<mxCell>> edgeHash = new HashMap<String, Vector<mxCell>>();
		
		// build a temporary structure mapping sets of edge endpoints to edges 
		
		// map influences
		Model m = bioModel.getSBMLDocument().getModel();
		for (int i = 0; i < m.getReactionCount(); i++) {
			Reaction r = m.getReaction(i);
			if (BioModel.isComplexReaction(r)) {
				for (int j = 0; j < r.getReactantCount(); j++) {
					String endA = r.getReactant(j).getSpecies();
					String endB = r.getProduct(0).getSpecies();
					String id = r.getReactant(j).getSpecies() + "+>" + r.getProduct(0).getSpecies();
					if(endA.compareTo(endB) > 0){
						// swap the strings
						String t = endA;
						endA = endB;
						endB = t;
					}
					String key = endA + " " + endB;
					mxCell cell = this.getInfluence(id);
					if(edgeHash.containsKey(key) == false)
						edgeHash.put(key, new Vector<mxCell>());
					edgeHash.get(key).add(cell);
				}
			} else if (BioModel.isProductionReaction(r)) {
				String promoterId = SBMLutilities.getPromoterId(r);
				if (!bioModel.isPromoterExplicit(promoterId)) {
					for (int j = 0; j < r.getModifierCount(); j++) {
						for (int k = 0; k < r.getProductCount(); k++) {
							String endA = r.getModifier(j).getSpecies();
							String endB = r.getProduct(k).getSpecies();
							if (BioModel.isRepressor(r.getModifier(j))) {
								String id = r.getModifier(j).getSpecies() + "-|" + r.getProduct(k).getSpecies() + "," + promoterId;
								if(endA.compareTo(endB) > 0){
									// swap the strings
									String t = endA;
									endA = endB;
									endB = t;
								}
								String key = endA + " " + endB;
								mxCell cell = this.getInfluence(id);
								if(edgeHash.containsKey(key) == false)
									edgeHash.put(key, new Vector<mxCell>());
								edgeHash.get(key).add(cell);
							} else if (BioModel.isActivator(r.getModifier(j))) {
								String id = r.getModifier(j).getSpecies() + "->" + r.getProduct(k).getSpecies() + "," + promoterId;
								if(endA.compareTo(endB) > 0){
									// swap the strings
									String t = endA;
									endA = endB;
									endB = t;
								}
								String key = endA + " " + endB;
								mxCell cell = this.getInfluence(id);
								if(edgeHash.containsKey(key) == false)
									edgeHash.put(key, new Vector<mxCell>());
								edgeHash.get(key).add(cell);
							} else if (BioModel.isNeutral(r.getModifier(j))) {
								String id = r.getModifier(j).getSpecies() + "x>" + r.getProduct(k).getSpecies() + "," + promoterId;
								if(endA.compareTo(endB) > 0){
									// swap the strings
									String t = endA;
									endA = endB;
									endB = t;
								}
								String key = endA + " " + endB;
								mxCell cell = this.getInfluence(id);
								if(edgeHash.containsKey(key) == false)
									edgeHash.put(key, new Vector<mxCell>());
								edgeHash.get(key).add(cell);
							} else if (BioModel.isRegulator(r.getModifier(j))) {
								String id = r.getModifier(j).getSpecies() + "->" + r.getProduct(k).getSpecies() + "," + promoterId;
								if(endA.compareTo(endB) > 0){
									// swap the strings
									String t = endA;
									endA = endB;
									endB = t;
								}
								String key = endA + " " + endB;
								mxCell cell = this.getInfluence(id);
								if(edgeHash.containsKey(key) == false)
									edgeHash.put(key, new Vector<mxCell>());
								edgeHash.get(key).add(cell);
								id = r.getModifier(j).getSpecies() + "-|" + r.getProduct(k).getSpecies() + "," + promoterId;
								if(endA.compareTo(endB) > 0){
									// swap the strings
									String t = endA;
									endA = endB;
									endB = t;
								}
								key = endA + " " + endB;
								cell = this.getInfluence(id);
								if(edgeHash.containsKey(key) == false)
									edgeHash.put(key, new Vector<mxCell>());
								edgeHash.get(key).add(cell);
							}  
						}
					}
				} else {
					for (int j = 0; j < r.getModifierCount(); j++) {
						String endA = r.getModifier(j).getSpecies();
						String endB = promoterId;
						if (BioModel.isRegulator(r.getModifier(j))) {
							String id = r.getModifier(j).getSpecies() + "->" + promoterId;
							if(endA.compareTo(endB) > 0){
								// swap the strings
								String t = endA;
								endA = endB;
								endB = t;
							}
							String key = endA + " " + endB;
							mxCell cell = this.getInfluence(id);
							if(edgeHash.containsKey(key) == false)
								edgeHash.put(key, new Vector<mxCell>());
							edgeHash.get(key).add(cell);
							id = r.getModifier(j).getSpecies() + "-|" + promoterId;
							if(endA.compareTo(endB) > 0){
								// swap the strings
								String t = endA;
								endA = endB;
								endB = t;
							}
							key = endA + " " + endB;
							cell = this.getInfluence(id);
							if(edgeHash.containsKey(key) == false)
								edgeHash.put(key, new Vector<mxCell>());
							edgeHash.get(key).add(cell);
						}
					}
				}
			}
		}
		
		for (int i = 0; i < m.getReactionCount(); i++) {
			
			Reaction r = m.getReaction(i);
			if (BioModel.isDegradationReaction(r)) continue;
			if (BioModel.isDiffusionReaction(r)) continue;
			if (BioModel.isProductionReaction(r)) continue;
			if (BioModel.isComplexReaction(r)) continue;
			if (BioModel.isConstitutiveReaction(r)) continue;
			
			if (bioModel.getLayout().getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId()) != null) {
				
				for (int j = 0; j < r.getReactantCount(); j++) {
					
					SpeciesReference s = r.getReactant(j);
					String endA = s.getSpecies();
					String endB = r.getId();
					
					if(endA.compareTo(endB) > 0){
						// swap the strings
						String t = endA;
						endA = endB;
						endB = t;
					}
					
					String key = endA + " " + endB;
					mxCell cell = this.getInfluence(s.getSpecies() + "__r" + j + "__" + r.getId());
					
					if(edgeHash.containsKey(key) == false)
						edgeHash.put(key, new Vector<mxCell>());
					
					edgeHash.get(key).add(cell);
				}
				
				for (int j = 0; j < r.getModifierCount(); j++) {
					
					ModifierSpeciesReference s = r.getModifier(j);
					String endA = s.getSpecies();
					String endB = r.getId();
					
					if(endA.compareTo(endB) > 0){
						// swap the strings
						String t = endA;
						endA = endB;
						endB = t;
					}
					
					String key = endA + " " + endB;
					mxCell cell = this.getInfluence(s.getSpecies() + "__m" + j + "__" + r.getId());
					
					if(edgeHash.containsKey(key) == false)
						edgeHash.put(key, new Vector<mxCell>());
					
					edgeHash.get(key).add(cell);
				}
				
				for (int k = 0; k < r.getProductCount(); k++) {
					
					SpeciesReference s = r.getProduct(k);
					String endA = r.getId();
					String endB = s.getSpecies();
					
					if(endA.compareTo(endB) > 0){
						// swap the strings
						String t = endA;
						endA = endB;
						endB = t;
					}
					
					String key = endA + " " + endB;
					mxCell cell = this.getInfluence(r.getId() + "__p" + k + "__" + s.getSpecies());
					
					if(edgeHash.containsKey(key) == false)
						edgeHash.put(key, new Vector<mxCell>());
					
					edgeHash.get(key).add(cell);
				}
			} 
			else {
				for (int j = 0; j < r.getReactantCount(); j++) {
					
					SpeciesReference s1 = r.getReactant(j);
					
					for (int k = 0; k < r.getProductCount(); k++) {
						
						SpeciesReference s2 = r.getProduct(k);

						String endA = s1.getSpecies();
						String endB = s2.getSpecies();
						
						// ignore anything connected directly to a drawn promoter
						//if(endA.equals(GlobalConstants.NONE) || endB.equals(GlobalConstants.NONE))
						//	continue;
						if(endA.compareTo(endB) > 0){
							// swap the strings
							String t = endA;
							endA = endB;
							endB = t;
						}
						
						String key = endA + " " + endB;
						mxCell cell = this.getInfluence(s1.getSpecies() + "_" + r.getId() + "_" + s2.getSpecies());
						
						if(edgeHash.containsKey(key) == false)
							edgeHash.put(key, new Vector<mxCell>());
						
						edgeHash.get(key).add(cell);
					}
				}
			}
		}
		
		// map components edges
		for (int i = 0; i < bioModel.getSBMLCompModel().getListOfSubmodels().size(); i++) {
			String id = bioModel.getSBMLCompModel().getListOfSubmodels().get(i).getId();
			BioModel compBioModel = new BioModel(bioModel.getPath());
			String modelFileName = bioModel.getModelFileName(id);
			try {
        compBioModel.load(bioModel.getPath() + GlobalConstants.separator + modelFileName);
      } catch (XMLStreamException e) {
        JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      } catch (IOException e) {
        JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      }
			HashMap<String,String> connections = bioModel.getInputConnections(compBioModel,id);
			for (String propName : connections.keySet()) {
				String targetName = connections.get(propName);
				String type = "Input";
				String key = id + " "+type+" " + targetName;
				mxCell cell = componentsConnectionsToMxCellMap.get(key);
				String simpleKey = id + " " + targetName;
				if(edgeHash.containsKey(simpleKey) == false)
					edgeHash.put(simpleKey, new Vector<mxCell>());
				edgeHash.get(simpleKey).add(cell);
			}
			connections = bioModel.getOutputConnections(compBioModel,id);
			for (String propName : connections.keySet()) {
				String targetName = connections.get(propName);
				String type = "Output";
				String key = id + " "+type+" " + targetName;
				mxCell cell = componentsConnectionsToMxCellMap.get(key);
				String simpleKey = id + " " + targetName;
				if(edgeHash.containsKey(simpleKey) == false)
					edgeHash.put(simpleKey, new Vector<mxCell>());
				edgeHash.get(simpleKey).add(cell);
			}
			connections = bioModel.getVariableInputConnections(compBioModel,id);
			for (String propName : connections.keySet()) {
				String targetName = connections.get(propName);
				String type = "Input";
				String key = id + " "+type+" " + targetName;
				mxCell cell = componentsConnectionsToMxCellMap.get(key);
				String simpleKey = id + " " + targetName;
				if(edgeHash.containsKey(simpleKey) == false)
					edgeHash.put(simpleKey, new Vector<mxCell>());
				edgeHash.get(simpleKey).add(cell);
			}
			connections = bioModel.getVariableOutputConnections(compBioModel,id);
			for (String propName : connections.keySet()) {
				String targetName = connections.get(propName);
				String type = "Output";
				String key = id + " "+type+" " + targetName;
				mxCell cell = componentsConnectionsToMxCellMap.get(key);
				String simpleKey = id + " " + targetName;
				if(edgeHash.containsKey(simpleKey) == false)
					edgeHash.put(simpleKey, new Vector<mxCell>());
				edgeHash.get(simpleKey).add(cell);
			}		
		}
		
		// loop through every set of edge endpoints and then move them if needed.
		for(Vector<mxCell> vec:edgeHash.values()){
			
			if(vec.size() > 1 && vec.get(0) != null){
				
				mxCell source = (mxCell)vec.get(0).getSource();
				mxCell target = (mxCell)vec.get(0).getTarget(); 
				
				// find the end and center points
				mxGeometry t;
				t = source.getGeometry();
				mxPoint sp = new mxPoint(t.getCenterX(), t.getCenterY());
				t = target.getGeometry();
				mxPoint tp = new mxPoint(t.getCenterX(), t.getCenterY());
				mxPoint cp = new mxPoint((tp.getX()+sp.getX())/2.0, (tp.getY()+sp.getY())/2.0);
				
				// check for self-influence
				if(source == target){
					
					mxCell c = vec.get(0);
					mxGeometry geom = c.getGeometry();
					
					// set the self-influence's point to the left of the influence.
					// This causes the graph library to draw it rounded in that direction.
					mxPoint p = new mxPoint(
							cp.getX() - t.getWidth()/2-SECOND_SELF_INFLUENCE_DISTANCE, 
							cp.getY()
							);
					
					Vector<mxPoint> points = new Vector<mxPoint>();
					points.add(p);
					geom.setPoints(points);
					c.setGeometry(geom);
					
					continue;
				}
				
				
				// make a unit vector that points in the direction perpendicular to the
				// direction from one endpoint to the other. 90 degrees rotated means flip
				// the x and y coordinates.
				mxPoint dVec = new mxPoint(-(sp.getY()-tp.getY()), sp.getX()-tp.getX());
				double magnitude = Math.sqrt(dVec.getX()*dVec.getX() + dVec.getY()*dVec.getY());
				
				// avoid divide-by-zero errors
				magnitude = Math.max(magnitude, .1);
				
				// normalize
				dVec.setX(dVec.getX()/magnitude);
				dVec.setY(dVec.getY()/magnitude);
				
				// loop through all the edges, create a new midpoint and apply it. 
				// also move the edge center to the midpoint so that labels won't be
				// on top of each other.
				for(int i=0; i<vec.size(); i++){
					
					double offset = i-(vec.size()-1.0)/2.0;
					mxCell edge = vec.get(i);
					//cell.setGeometry(new mxGeometry(0, 0, 100, 100));
					mxGeometry geom = edge.getGeometry();
					Vector<mxPoint> points = new Vector<mxPoint>();
			
					mxPoint p = new mxPoint(
							cp.getX()+dVec.getX()*offset*DIS_BETWEEN_NEIGHBORING_EDGES, 
							cp.getY()+dVec.getY()*offset*DIS_BETWEEN_NEIGHBORING_EDGES
							);
					points.add(p);
					geom.setPoints(points);
//					geom.setX(p.getX());
//					geom.setY(p.getY());
					edge.setGeometry(geom);
				}
			}
		}		
		
//		for(Object edgeo:this.getSelectionCell()){
//			mxCell edge = (mxCell)edgeo;
//			int s = edge.getSource().getEdgeCount();
//			int t = edge.getTarget().getEdgeCount()
//			
//			if(edge.getSource().getEdgeCount() > 1 && edge.getTarget().getEdgeCount() > 1){
//				// the source and target have multiple edges, now loop through them all...
//				
//				
//				//cell.setGeometry(new mxGeometry(0, 0, 100, 100));
//				mxGeometry geom = new mxGeometry();
//				Vector<mxPoint> points = new Vector<mxPoint>();
//				mxPoint p = new mxPoint(50.0, 50.0);
//				points.add(p);
//				geom.setPoints(points);
//				edge.setGeometry(geom);
//			}
//		}		
	}
	
	/**
	 * adds grid rectangles via cell vertices
	 */
	public void addGridCells() {
		
		if (modelEditor.getGrid().isEnabled()) {
			
			int gridRows = bioModel.getGridTable().getNumRows();
			int gridCols = bioModel.getGridTable().getNumCols();
			double gridWidth = modelEditor.getGrid().getGridGeomWidth();
			double gridHeight = modelEditor.getGrid().getGridGeomHeight();
			
			//creates an mxCell/vertex for each grid rectangle
			//these are later accessible via ID via the hash map
			for (int row = 0; row < gridRows; ++row) {
				for (int col = 0; col < gridCols; ++col) {
					
					String id = "ROW" + row + "_COL" + col;
					
					double currX = 15 + col*gridWidth;
					double currY = 15 + row*gridHeight;
					
					CellValueObject cvo = new CellValueObject(id, "Rectangle", null);
					
					mxGeometry geometry = new mxGeometry(currX, currY, gridWidth, gridHeight);
					mxCell vertex = new mxCell(cvo, geometry, null);

					vertex.setId(id);
					vertex.setVertex(true);
					vertex.setConnectable(false);
					vertex.setStyle("GRID_RECTANGLE");
					
					addCell(vertex, this.defaultParent);
					
					gridRectangleToMxCellMap.put(id, vertex);
				}
			}
		}
	}
	
	
	//POSITION UPDATING
	
	/**
	 * Called after a layout is chosen and applied.
	 * Updates the gcm's postitioning using the
	 * positioning on the graph.
	 */
	public void updateAllInternalPosition(){

		for(mxCell cell:this.speciesToMxCellMap.values()){
			updateInternalPosition(cell,false);
		}
		
		for(mxCell cell:this.reactionsToMxCellMap.values()){
			updateInternalPosition(cell,false);
		}
		
		for(mxCell cell:this.rulesToMxCellMap.values()){
			updateInternalPosition(cell,false);
		}
		
		for(mxCell cell:this.constraintsToMxCellMap.values()){
			updateInternalPosition(cell,false);
		}
		
		for(mxCell cell:this.eventsToMxCellMap.values()){
			updateInternalPosition(cell,false);
		}
		
		for(mxCell cell:this.componentsToMxCellMap.values()){
			updateInternalPosition(cell,false);
		}
		
		for(mxCell cell:this.drawnPromoterToMxCellMap.values()){
			updateInternalPosition(cell,false);
		}
		
		for(mxCell cell:this.variableToMxCellMap.values()){
			updateInternalPosition(cell,false);
		}
		
		for(mxCell cell:this.compartmentsToMxCellMap.values()){
			updateInternalPosition(cell,false);
		}
	}
	
	/**
	 * Given a cell that must be a species or component,
	 * update the internal model to reflect it's coordinates.
	 * Called when a cell is dragged with the GUI.
	 */
	public void updateInternalPosition(mxCell cell,boolean warn){
		
		Layout layout = bioModel.getLayout();
		mxGeometry geom = cell.getGeometry();
		if (getCellType(cell).equals(GlobalConstants.SPECIES) ||
				getCellType(cell).equals(GlobalConstants.PROMOTER)) {
			SpeciesGlyph speciesGlyph = null;
			if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
			} else {
				speciesGlyph = layout.createSpeciesGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
				speciesGlyph.createBoundingBox();
				speciesGlyph.getBoundingBox().createDimensions();
				speciesGlyph.getBoundingBox().createPosition();
				speciesGlyph.setSpecies(cell.getId());
			}
			String compartment = bioModel.getCompartmentByLocation((float)geom.getX(),(float)geom.getY(),
					(float)geom.getWidth(),(float)geom.getHeight());
			if (compartment.equals("")) {
				geom.setX(speciesGlyph.getBoundingBox().getPosition().getX());
				geom.setY(speciesGlyph.getBoundingBox().getPosition().getY());
				geom.setWidth(speciesGlyph.getBoundingBox().getDimensions().getWidth());
				geom.setHeight(speciesGlyph.getBoundingBox().getDimensions().getHeight());
				if (warn) {
				  JOptionPane.showMessageDialog(Gui.frame, "Species must be placed within a compartment.", "Compartment Required", JOptionPane.ERROR_MESSAGE); 
				}
				return;
			} 
			bioModel.getSBMLDocument().getModel().getSpecies(cell.getId()).setCompartment(compartment);
			speciesGlyph.getBoundingBox().getPosition().setX(geom.getX());
			speciesGlyph.getBoundingBox().getPosition().setY(geom.getY());
			speciesGlyph.getBoundingBox().getDimensions().setWidth(geom.getWidth());
			speciesGlyph.getBoundingBox().getDimensions().setHeight(geom.getHeight());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			} else {
				textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
				textGlyph.createBoundingBox();
				textGlyph.getBoundingBox().createDimensions();
				textGlyph.getBoundingBox().createPosition();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+cell.getId());
			textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(),cell.getId()));
			textGlyph.setBoundingBox(speciesGlyph.getBoundingBox().clone());
		} else if (getCellType(cell).equals(GlobalConstants.VARIABLE) || 
				getCellType(cell).equals(GlobalConstants.PLACE) || 
				getCellType(cell).equals(GlobalConstants.BOOLEAN)) {
			GeneralGlyph generalGlyph = null;
			if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				generalGlyph = (GeneralGlyph)layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
			} else {
				generalGlyph = layout.createGeneralGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
				generalGlyph.createBoundingBox();
				generalGlyph.getBoundingBox().createDimensions();
				generalGlyph.getBoundingBox().createPosition();
				generalGlyph.setReference(cell.getId());
			}
			generalGlyph.getBoundingBox().getPosition().setX(geom.getX());
			generalGlyph.getBoundingBox().getPosition().setY(geom.getY());
			generalGlyph.getBoundingBox().getDimensions().setWidth(geom.getWidth());
			generalGlyph.getBoundingBox().getDimensions().setHeight(geom.getHeight());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			} else {
				textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
				textGlyph.createBoundingBox();
				textGlyph.getBoundingBox().createDimensions();
				textGlyph.getBoundingBox().createPosition();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+cell.getId());
			textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(),cell.getId()));
			textGlyph.setBoundingBox(generalGlyph.getBoundingBox().clone());
		} else if (getCellType(cell).equals(GlobalConstants.REACTION)) {
			ReactionGlyph reactionGlyph = null;
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
			} else {
				reactionGlyph = layout.createReactionGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
				reactionGlyph.createBoundingBox();
				reactionGlyph.getBoundingBox().createDimensions();
				reactionGlyph.getBoundingBox().createPosition();
				reactionGlyph.setReaction(cell.getId());
			}
			if (getCellType(cell).equals(GlobalConstants.REACTION)) {
				String compartment = bioModel.getCompartmentByLocation((float)geom.getX(),(float)geom.getY(),
						(float)geom.getWidth(),(float)geom.getHeight());
				if (compartment.equals("")) {
					geom.setX(reactionGlyph.getBoundingBox().getPosition().getX());
					geom.setY(reactionGlyph.getBoundingBox().getPosition().getY());
					geom.setWidth(reactionGlyph.getBoundingBox().getDimensions().getWidth());
					geom.setHeight(reactionGlyph.getBoundingBox().getDimensions().getHeight());
					if (warn) {
					  JOptionPane.showMessageDialog(Gui.frame,"Reaction must be placed within a compartment.", "Compartment Required", JOptionPane.ERROR_MESSAGE); 
		
					}
					return;
				} 
				bioModel.getSBMLDocument().getModel().getReaction(cell.getId()).setCompartment(compartment);
			}
			reactionGlyph.getBoundingBox().getPosition().setX(geom.getX());
			reactionGlyph.getBoundingBox().getPosition().setY(geom.getY());
			reactionGlyph.getBoundingBox().getDimensions().setWidth(geom.getWidth());
			reactionGlyph.getBoundingBox().getDimensions().setHeight(geom.getHeight());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			} else {
				textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
				textGlyph.createBoundingBox();
				textGlyph.getBoundingBox().createDimensions();
				textGlyph.getBoundingBox().createPosition();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+cell.getId());
			textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(),cell.getId()));
			textGlyph.setBoundingBox(reactionGlyph.getBoundingBox().clone());
		} else if (getCellType(cell).equals(GlobalConstants.RULE)||
				getCellType(cell).equals(GlobalConstants.CONSTRAINT)||
				getCellType(cell).equals(GlobalConstants.EVENT)||
				getCellType(cell).equals(GlobalConstants.TRANSITION)) {
			GeneralGlyph generalGlyph = null;
			if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				generalGlyph = (GeneralGlyph)
						layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
			} else {
				generalGlyph = layout.createGeneralGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
				if (getCellType(cell).equals(GlobalConstants.EVENT) || getCellType(cell).equals(GlobalConstants.TRANSITION)) {
					generalGlyph.setReference(cell.getId());
				} else {
					generalGlyph.unsetReference();
					generalGlyph.setMetaidRef(cell.getId());
				}
			}
			generalGlyph.getBoundingBox().getPosition().setX(geom.getX());
			generalGlyph.getBoundingBox().getPosition().setY(geom.getY());
			generalGlyph.getBoundingBox().getDimensions().setWidth(geom.getWidth());
			generalGlyph.getBoundingBox().getDimensions().setHeight(geom.getHeight());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			} else {
				textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
				textGlyph.createBoundingBox();
				textGlyph.getBoundingBox().createDimensions();
				textGlyph.getBoundingBox().createPosition();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+cell.getId());
			textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(),cell.getId()));
			textGlyph.setBoundingBox(generalGlyph.getBoundingBox().clone());		
		} else if (!bioModel.isGridEnabled() && getCellType(cell).equals(GlobalConstants.COMPARTMENT)) {
			CompartmentGlyph compGlyph = null;
			if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				compGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
			} else {
				compGlyph = layout.createCompartmentGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
				compGlyph.setCompartment(cell.getId());
				compGlyph.createBoundingBox();
				compGlyph.getBoundingBox().createDimensions();
				compGlyph.getBoundingBox().createPosition();
			}
			double x = compGlyph.getBoundingBox().getPosition().getX();
			double y = compGlyph.getBoundingBox().getPosition().getY();
			double width = compGlyph.getBoundingBox().getDimensions().getWidth();
			double height = compGlyph.getBoundingBox().getDimensions().getHeight();
			if (!bioModel.checkCompartmentOverlap(cell.getId(),geom.getX(), geom.getY(), geom.getWidth(), geom.getHeight())) {
				geom.setX(x);
				geom.setY(y);
				geom.setWidth(width);
				geom.setHeight(height);
				if (warn) {
					 JOptionPane.showMessageDialog(Gui.frame, "Compartments must not overlap.", "Compartment Overlap", JOptionPane.ERROR_MESSAGE); 
				}
				return;
			}
			if (!bioModel.checkCompartmentLocation(cell.getId(),geom.getX(), geom.getY(), geom.getWidth(), geom.getHeight())) {
				geom.setX(x);
				geom.setY(y);
				geom.setWidth(width);
				geom.setHeight(height);
				if (warn) {
				  JOptionPane.showMessageDialog(Gui.frame,"Compartments must include their species and reactions.", "Compartment Location", JOptionPane.ERROR_MESSAGE); 
			    
				}
				return;
			}
			compGlyph.getBoundingBox().getPosition().setX(geom.getX());
			compGlyph.getBoundingBox().getPosition().setY(geom.getY());
			compGlyph.getBoundingBox().getDimensions().setWidth(geom.getWidth());
			compGlyph.getBoundingBox().getDimensions().setHeight(geom.getHeight());
			if (bioModel.updateCompartmentsByLocation(true)) {
				bioModel.updateCompartmentsByLocation(false);
			} else {
				compGlyph.getBoundingBox().getPosition().setX(x);
				compGlyph.getBoundingBox().getPosition().setY(y);
				compGlyph.getBoundingBox().getDimensions().setWidth(width);
				compGlyph.getBoundingBox().getDimensions().setHeight(height);
				if (warn) {
				  JOptionPane.showMessageDialog(Gui.frame, "All species and reactions must be within a compartment.", "Missing Compartment", JOptionPane.ERROR_MESSAGE); 
				}
			}
			geom.setX(compGlyph.getBoundingBox().getPosition().getX());
			geom.setY(compGlyph.getBoundingBox().getPosition().getY());
			geom.setWidth(compGlyph.getBoundingBox().getDimensions().getWidth());
			geom.setHeight(compGlyph.getBoundingBox().getDimensions().getHeight());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			} else {
				textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
				textGlyph.createBoundingBox();
				textGlyph.getBoundingBox().createDimensions();
				textGlyph.getBoundingBox().createPosition();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+cell.getId());
			textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(),cell.getId()));
			textGlyph.setBoundingBox(compGlyph.getBoundingBox().clone());
		} else if (getCellType(cell).equals(GlobalConstants.COMPONENT)) {
			GeneralGlyph generalGlyph = null;
			if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				generalGlyph = (GeneralGlyph)
						layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
			} else {
				generalGlyph = layout.createGeneralGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
				generalGlyph.createBoundingBox();
				generalGlyph.getBoundingBox().createDimensions();
				generalGlyph.getBoundingBox().createPosition();
				generalGlyph.setReference(cell.getId());
			}
			generalGlyph.getBoundingBox().getPosition().setX(geom.getX());
			generalGlyph.getBoundingBox().getPosition().setY(geom.getY());
			generalGlyph.getBoundingBox().getDimensions().setWidth(geom.getWidth());
			generalGlyph.getBoundingBox().getDimensions().setHeight(geom.getHeight());
			geom.setX(generalGlyph.getBoundingBox().getPosition().getX());
			geom.setY(generalGlyph.getBoundingBox().getPosition().getY());
			geom.setWidth(generalGlyph.getBoundingBox().getDimensions().getWidth());
			geom.setHeight(generalGlyph.getBoundingBox().getDimensions().getHeight());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			} else {
				textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
				textGlyph.createBoundingBox();
				textGlyph.getBoundingBox().createDimensions();
				textGlyph.getBoundingBox().createPosition();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+cell.getId());
			textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+cell.getId());
			textGlyph.setText(SBMLutilities.getArrayId(bioModel.getSBMLDocument(),cell.getId()));
			textGlyph.setBoundingBox(generalGlyph.getBoundingBox().clone());
		} 
	}
	
	/**
	 * Given a species, component, or drawn promoter cell, position it 
	 * using the properties.
	 */
	private boolean sizeAndPositionFromProperties(mxCell cell,double width,double height){
		
		double x = 0;
		double y = 0;
		boolean needsPositioning = false;	

		Layout layout = bioModel.getLayout();
		if (getCellType(cell).equals(GlobalConstants.SPECIES)||
				getCellType(cell).equals(GlobalConstants.PROMOTER)) {
			if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
				x = speciesGlyph.getBoundingBox().getPosition().getX();
				y = speciesGlyph.getBoundingBox().getPosition().getY();
				width = speciesGlyph.getBoundingBox().getDimensions().getWidth();
				height = speciesGlyph.getBoundingBox().getDimensions().getHeight();
				if (bioModel.getSBMLDocument().getModel().getSpecies(cell.getId())!=null) {
					String compartment = bioModel.getSBMLDocument().getModel().getSpecies(cell.getId()).getCompartment();
					if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
						CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+compartment);
						if (compartmentGlyph!=null) {
							x = x + compartmentGlyph.getBoundingBox().getPosition().getX();
							if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
								x = compartmentGlyph.getBoundingBox().getPosition().getX() + 10;
							}
							bioModel.placeSpecies(cell.getId(), x, y, height, width);
						}
					}
				}
			} else {
				unpositionedElementCount += 1;
				needsPositioning = true;
				x = (unpositionedElementCount%50) * 20;
				y = (unpositionedElementCount%10) * (height + 10);
				if (bioModel.getSBMLDocument().getModel().getSpecies(cell.getId())!=null) {
					String compartment = bioModel.getSBMLDocument().getModel().getSpecies(cell.getId()).getCompartment();
					if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
						CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+compartment);
						if (compartmentGlyph!=null) {
							x = x + compartmentGlyph.getBoundingBox().getPosition().getX();
							if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
								x = compartmentGlyph.getBoundingBox().getPosition().getX() + 10;
							}
						}
					}
				}
				bioModel.placeSpecies(cell.getId(), x, y, height, width);
			}
		} else if (getCellType(cell).equals(GlobalConstants.VARIABLE)||
				getCellType(cell).equals(GlobalConstants.PLACE)||
				getCellType(cell).equals(GlobalConstants.BOOLEAN)) {
			if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				GeneralGlyph generalGlyph = (GeneralGlyph)
						layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
				x = generalGlyph.getBoundingBox().getPosition().getX();
				y = generalGlyph.getBoundingBox().getPosition().getY();
				width = generalGlyph.getBoundingBox().getDimensions().getWidth();
				height = generalGlyph.getBoundingBox().getDimensions().getHeight();
			} else {
				unpositionedElementCount += 1;
				needsPositioning = true;
				x = (unpositionedElementCount%50) * 20;
				y = (unpositionedElementCount%10) * (height + 10);
				bioModel.placeGeneral(cell.getId(), x, y, height, width, null);
			}
		} else if (getCellType(cell).equals(GlobalConstants.REACTION)) {
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
				x = reactionGlyph.getBoundingBox().getPosition().getX();
				y = reactionGlyph.getBoundingBox().getPosition().getY();
				width = reactionGlyph.getBoundingBox().getDimensions().getWidth();
				height = reactionGlyph.getBoundingBox().getDimensions().getHeight();
				if (bioModel.getSBMLDocument().getModel().getReaction(cell.getId())!=null) {
					String compartment = bioModel.getSBMLDocument().getModel().getReaction(cell.getId()).getCompartment();
					if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
						CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+compartment);
						if (compartmentGlyph!=null) {
							x = x + compartmentGlyph.getBoundingBox().getPosition().getX();
							if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
								x = compartmentGlyph.getBoundingBox().getPosition().getX() + 10;
							}
							bioModel.placeReaction(cell.getId(), x, y, height, width);
						}
					}
				}
			} else {
				unpositionedElementCount += 1;
				needsPositioning = true;
				x = (unpositionedElementCount%50) * 20;
				y = (unpositionedElementCount%10) * (height + 10);
				if (bioModel.getSBMLDocument().getModel().getReaction(cell.getId())!=null) {
					String compartment = bioModel.getSBMLDocument().getModel().getReaction(cell.getId()).getCompartment();
					if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
						CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+compartment);
						if (compartmentGlyph!=null) {
							x = x + compartmentGlyph.getBoundingBox().getPosition().getX();
							if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
								x = compartmentGlyph.getBoundingBox().getPosition().getX() + 10;
							}
						}
					}
				}
				bioModel.placeReaction(cell.getId(), x, y, height, width);
			} 
		} else if (getCellType(cell).equals(GlobalConstants.RULE) ||
				getCellType(cell).equals(GlobalConstants.CONSTRAINT) ||
				getCellType(cell).equals(GlobalConstants.EVENT) ||
				getCellType(cell).equals(GlobalConstants.TRANSITION)) {
			if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				GeneralGlyph generalGlyph = (GeneralGlyph)
						layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
				x = generalGlyph.getBoundingBox().getPosition().getX();
				y = generalGlyph.getBoundingBox().getPosition().getY();
				width = generalGlyph.getBoundingBox().getDimensions().getWidth();
				height = generalGlyph.getBoundingBox().getDimensions().getHeight();
			} else {
				unpositionedElementCount += 1;
				needsPositioning = true;
				x = (unpositionedElementCount%50) * 20;
				y = (unpositionedElementCount%10) * (height + 10);
				if ((getCellType(cell).equals(GlobalConstants.RULE) ||
						getCellType(cell).equals(GlobalConstants.CONSTRAINT))) {
					bioModel.placeGeneral(cell.getId(), x, y, height, width, cell.getId());
				} else {
					bioModel.placeGeneral(cell.getId(), x, y, height, width, null);
				}
			} 
		} else if (!bioModel.isGridEnabled() && getCellType(cell).equals(GlobalConstants.COMPARTMENT)) {
			if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				CompartmentGlyph compGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
				x = compGlyph.getBoundingBox().getPosition().getX();
				y = compGlyph.getBoundingBox().getPosition().getY();
				width = compGlyph.getBoundingBox().getDimensions().getWidth();
				height = compGlyph.getBoundingBox().getDimensions().getHeight();
			} else {
				unpositionedElementCount += 1;
				needsPositioning = true;
				x = (unpositionedElementCount%50) * 20;
				y = (unpositionedElementCount%10) * (height + 10);
				bioModel.placeCompartment(cell.getId(), x, y, height, width);
			} 
		} else if (getCellType(cell).equals(GlobalConstants.COMPONENT)) {
			if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
				GeneralGlyph generalGlyph = (GeneralGlyph)
						layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
				x = generalGlyph.getBoundingBox().getPosition().getX();
				y = generalGlyph.getBoundingBox().getPosition().getY();
				width = generalGlyph.getBoundingBox().getDimensions().getWidth();
				height = generalGlyph.getBoundingBox().getDimensions().getHeight();
			} else {
				unpositionedElementCount += 1;
				needsPositioning = true;
				x = (unpositionedElementCount%50) * 20;
				y = (unpositionedElementCount%10) * (height + 10);
				bioModel.placeGeneral(cell.getId(), x, y, height, width, null);
			} 
		} 
		cell.setGeometry(new mxGeometry(x, y, width, height));
		return needsPositioning;
	}
	
	/**
	 * redraws the grid components
	 */
	public void updateGrid() {
		
		dynamic = true;
		
		this.removeCells(this.getChildCells(this.getDefaultParent(), true, true));
		
		gridRectangleToMxCellMap.clear();
		addGridCells();
		
		componentsToMxCellMap.clear();
		
		Grid grid = modelEditor.getGrid();
		double gridWidth = grid.getGridGeomWidth();
		double gridHeight = grid.getGridGeomHeight();
		
		componentsToMxCellMap.clear();
		
		//ADD COMPONENTS
		for (int row = 0; row < bioModel.getGridTable().getNumRows(); ++row) {
			for (int col = 0; col < bioModel.getGridTable().getNumCols(); ++col) {
				
				if (grid.getOccupancyFromLocation(row, col) == true) {
					
					double currX = 15 + col*gridWidth;
					double currY = 15 + row*gridHeight;
					
					String compID = grid.getCompIDFromLocation(row, col);
					
					grid.setNodeRectangle(compID, new Rectangle((int) currX, (int) currY, (int) gridWidth, (int) gridHeight));
					
					Rectangle componentRectangle = grid.getSnapRectangleFromCompID(compID);
					
					String fullCompID = compID;
					
					if (compID.contains("_of_"))
						compID = compID.split("_")[0];
					
					if (compID.length() > 10) {
						compID = compID.substring(0,9) + "...";
					}					
					
					CellValueObject compcvo = new CellValueObject(compID, "Component", null);
					
					mxCell compCell = (mxCell) this.insertVertex(this.getDefaultParent(), fullCompID, compcvo, 
							componentRectangle.getX(), componentRectangle.getY(), 
							componentRectangle.getWidth(), componentRectangle.getHeight());
					compCell.setConnectable(false);
					compCell.setStyle("GRIDCOMPARTMENT");
					
					componentsToMxCellMap.put(fullCompID, compCell);
				}					
			}
		}
	}
	
	
	
	//GET METHODS
	
	/**
	 * returns GlobalConstants.SPECIES, GlobalConstants.COMPONENT, GlobalConstants.INFLUENCE, or GlobalConstants.COMPONENT_CONNECTION.
	 * @param cell
	 */
	public String getCellType(mxCell cell){
		
		if(cell != null && cell.isEdge()){
			String sourceType = getCellType(cell.getSource());
			String targetType = getCellType(cell.getTarget());
			
			if(sourceType == CELL_VALUE_NOT_FOUND || targetType == CELL_VALUE_NOT_FOUND){
				return CELL_NOT_FULLY_CONNECTED;
			}
			else if(sourceType == GlobalConstants.COMPONENT || targetType == GlobalConstants.COMPONENT){
				return GlobalConstants.COMPONENT_CONNECTION;
			}
			else if(sourceType == GlobalConstants.PROMOTER && targetType == GlobalConstants.SPECIES){
				return GlobalConstants.PRODUCTION;
			}
			else if (sourceType == GlobalConstants.SPECIES && targetType == GlobalConstants.SPECIES &&
					(bioModel.getSBMLDocument().getModel().getReactionCount() > 0) && cell.getValue() != null &&
					(bioModel.getSBMLDocument().getModel().getReaction((String)cell.getValue()) != null)) {
				return GlobalConstants.REACTION_EDGE;
			}
			else if (sourceType == GlobalConstants.REACTION || targetType == GlobalConstants.REACTION) {
				if (cell.getStyle().equals("MODIFIER_REACTION_EDGE")) {
					return GlobalConstants.MODIFIER_REACTION_EDGE;
				}
				return GlobalConstants.REACTION_EDGE;
			}
			else if (sourceType == GlobalConstants.RULE || targetType == GlobalConstants.RULE) {
				return GlobalConstants.RULE_EDGE;
			}
			else if (sourceType == GlobalConstants.CONSTRAINT || targetType == GlobalConstants.CONSTRAINT) {
				return GlobalConstants.CONSTRAINT_EDGE;
			}
			else if (sourceType == GlobalConstants.EVENT || targetType == GlobalConstants.EVENT) {
				return GlobalConstants.EVENT_EDGE;
			}
			else if (sourceType == GlobalConstants.TRANSITION || targetType == GlobalConstants.TRANSITION) {
				return GlobalConstants.PETRI_NET_EDGE;			}
			else {
				return GlobalConstants.INFLUENCE;
			}
		}
		//cell is a vertex
		else if (cell != null){
			String type = ((CellValueObject)(cell.getValue())).type;
			
			if(type.equals("Component"))
				return GlobalConstants.COMPONENT;
			else if(type.equals("Species"))
				return GlobalConstants.SPECIES;
			else if(type.equals("Promoter"))
				return GlobalConstants.PROMOTER;
			else if(type.equals("Variable"))
				return GlobalConstants.VARIABLE;
			else if(type.equals("Place"))
				return GlobalConstants.PLACE;
			else if(type.equals("Boolean"))
				return GlobalConstants.BOOLEAN;
			else if(type.equals("Reaction"))
				return GlobalConstants.REACTION;
			else if(type.equals("Rule"))
				return GlobalConstants.RULE;
			else if(type.equals("Constraint"))
				return GlobalConstants.CONSTRAINT;
			else if(type.equals("Event"))
				return GlobalConstants.EVENT;
			else if(type.equals("Transition"))
				return GlobalConstants.TRANSITION;
			else if(type.equals("Compartment"))
				return GlobalConstants.COMPARTMENT;
			else if (type.equals("Rectangle"))
				return GlobalConstants.GRID_RECTANGLE;
			else
				return CELL_VALUE_NOT_FOUND;
		}
		return CELL_VALUE_NOT_FOUND;
	}
	
	/**
	 * 
	 * @param cell
	 * @return
	 */
	public String getCellType(mxICell cell)
	{
		return getCellType((mxCell)cell);
	}
	
	public String getModelFileName(String compId) {
		return bioModel.getModelFileName(compId);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public mxCell getSpeciesCell(String id){
		return speciesToMxCellMap.get(id);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public mxCell getSpeciesOrPromoterCell(String id){
		mxCell cell = speciesToMxCellMap.get(id);
		if (cell==null) {
			cell = drawnPromoterToMxCellMap.get(id);
		}
		return cell;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public mxCell getReactionsCell(String id){
		return reactionsToMxCellMap.get(id);
	}

	public mxCell getCompartmentsCell(String id){
		return compartmentsToMxCellMap.get(id);
	}

	public mxCell getRulesCell(String id){
		return rulesToMxCellMap.get(id);
	}

	public mxCell getConstraintsCell(String id){
		return constraintsToMxCellMap.get(id);
	}

	public mxCell getEventsCell(String id){
		return eventsToMxCellMap.get(id);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public mxCell getComponentCell(String id){
		return componentsToMxCellMap.get(id);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public mxCell getDrawnPromoterCell(String id){
		return drawnPromoterToMxCellMap.get(id);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public mxCell getVariableCell(String id){
		return variableToMxCellMap.get(id);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public mxCell getInfluence(String id){
		return (influencesToMxCellMap.get(id));
	}
	
	/**
	 * returns the mxCell corresponding to the id passed in
	 * this mxCell is a grid rectangle on the grid
	 * 
	 * @param id the id of the grid rectangle's cell
	 * @return the corresponding cell
	 */
	public mxCell getGridRectangleCellFromID(String id) {
		
		return gridRectangleToMxCellMap.get(id);
	}
	
	/**
	 * returns if the cell is selectable or not
	 * 
	 * @param cell the cell that is or isn't selectable
	 */
	@Override
	public boolean isCellSelectable(Object cell) {
			
		mxCell tempCell = (mxCell)cell;
		
		//if it's a grid cell, it's not selectable
		//otherwise, do the default behavior	
		if (tempCell.getStyle().equals("GRID_RECTANGLE"))
			return false;
		
		return isCellsSelectable();
	}
	
	
	//GRAPH PART CREATION
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	private boolean createGraphComponentFromModel(String id){

		boolean needsPositioning = false;
				
		//set the correct compartment status
		BioModel compBioModel = new BioModel(bioModel.getPath());
		boolean compart = false;
		//String modelFileName = gcm.getModelFileName(id).replace(".xml", ".gcm");
		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id);
		String modelFileName = bioModel.getModelFileName(id);
		if (modelFileName.equals("")) {
			return false;
		}
		File compFile = new File(bioModel.getPath() + GlobalConstants.separator + modelFileName);
		
		if (compFile.exists()) {
			try {
        compBioModel.load(bioModel.getPath() + GlobalConstants.separator + modelFileName);
      } catch (XMLStreamException e) {
        JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      } catch (IOException e) {
        JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      }
			compart = compBioModel.IsWithinCompartment();
		} else {
			JOptionPane.showMessageDialog(Gui.frame, 
					"A model definition cannot be found for " + modelFileName + 
					".\nDropping module from the schematic.\n",
					"Warning", JOptionPane.WARNING_MESSAGE);
			bioModel.removeComponent(id);
			return false;
		}
		
		String truncModelId = modelFileName.replace(".xml", "");
		//String truncID = "";
		
		//if the id is too long, truncate it
		if (truncModelId.length() > 10)
			truncModelId = truncModelId.substring(0, 9) + "...";
		/*
		if (id.length() > 10)
			truncID = id.substring(0, 9) + "...";
		else truncID = id;
		*/
		
		label = label + "\n" + truncModelId;
		CellValueObject cvo = new CellValueObject(label, "Component", null);
		mxCell insertedVertex = (mxCell) this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		insertedVertex.setId(id);
		this.componentsToMxCellMap.put(id, insertedVertex);
		
		//pass whether or not the component is a compartment, as the styles are different
		this.setComponentStyles(id, compart);
		
		needsPositioning = sizeAndPositionFromProperties(insertedVertex,
				GlobalConstants.DEFAULT_COMPONENT_WIDTH,GlobalConstants.DEFAULT_COMPONENT_HEIGHT);

		// now draw the edges that connect the component
		HashMap<String,String> connections = bioModel.getInputConnections(compBioModel,id);
		for (String propName : connections.keySet()) {
			// input, the arrow should point in from the species
			String topSpecies = connections.get(propName);
			Object createdEdge = this.insertEdge(this.getDefaultParent(), "", "", 
					this.getSpeciesOrPromoterCell(topSpecies),insertedVertex);
			String key = id + " Input " + topSpecies;
			componentsConnectionsToMxCellMap.put(key, (mxCell)createdEdge);
			this.updateComponentConnectionVisuals((mxCell)createdEdge, propName);
		}

		// now draw the edges that connect the component
		connections = bioModel.getOutputConnections(compBioModel,id);
		for (String propName : connections.keySet()) {
			// output, the arrow should point out to the species
			String topSpecies = connections.get(propName);
			Object createdEdge = this.insertEdge(this.getDefaultParent(), "", "", insertedVertex, 
					this.getSpeciesOrPromoterCell(topSpecies));
			String key = id + " Output " + topSpecies;
			componentsConnectionsToMxCellMap.put(key, (mxCell)createdEdge);
			this.updateComponentConnectionVisuals((mxCell)createdEdge, propName);
		}

		// now draw the edges that connect variables to the component
		connections = bioModel.getVariableInputConnections(compBioModel,id);
		for (String propName : connections.keySet()) {
			String topSpecies = connections.get(propName);
			Object createdEdge = this.insertEdge(this.getDefaultParent(), "", "",
					this.getVariableCell(topSpecies), insertedVertex);
			String key = id + " Input " + topSpecies;
			componentsConnectionsToMxCellMap.put(key, (mxCell)createdEdge);
			this.updateComponentConnectionVisuals((mxCell)createdEdge, propName);
		}

		// now draw the edges that connect variables to the component
		connections = bioModel.getVariableOutputConnections(compBioModel,id);
		for (String propName : connections.keySet()) {
			String topSpecies = connections.get(propName);
			Object createdEdge = this.insertEdge(this.getDefaultParent(), "", "", insertedVertex,
					this.getVariableCell(topSpecies));
			String key = id + " Output " + topSpecies;
			componentsConnectionsToMxCellMap.put(key, (mxCell)createdEdge);
			this.updateComponentConnectionVisuals((mxCell)createdEdge, propName);
		}
				
		return needsPositioning;
	}
	
	/**
	 * creates a vertex on the graph using the internal model.
	 * @param id
	 * 
	 * @return: A bool, true if the species had to be positioned.
	 */
	private boolean createGraphSpeciesFromModel(String sp){
		Species species = bioModel.getSBMLDocument().getModel().getSpecies(sp);
		if (BioModel.isMRNASpecies(bioModel.getSBMLDocument(),species)) return false; 
		
		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), sp);
		
		if (bioModel.getDiffusionReaction(sp) != null) label += " (D)";
		if (bioModel.isSpeciesConstitutive(sp)) label += " (C)";
		if (bioModel.isInput(sp)) {
			label += '\n' + GlobalConstants.INPUT;
		} else if (bioModel.isOutput(sp)) {
			label += '\n' + GlobalConstants.OUTPUT;
		} else {
			//label += '\n' + GlobalConstants.INTERNAL;
		}
		
		CellValueObject cvo = new CellValueObject(label, "Species", null);
		mxCell insertedVertex = (mxCell) this.insertVertex(this.getDefaultParent(), sp, cvo, 1, 1, 1, 1);
		insertedVertex.setId(sp);
		this.speciesToMxCellMap.put(sp, insertedVertex);
		
		this.setSpeciesStyles(sp);
		int width = GlobalConstants.DEFAULT_SPECIES_WIDTH;
		int height = GlobalConstants.DEFAULT_SPECIES_HEIGHT;
		if (species.isSetSBOTerm()) {
			if (species.getSBOTermID().equals(GlobalConstants.SBO_DNA) ||
					species.getSBOTermID().equals(GlobalConstants.SBO_DNA_SEGMENT)) {
				width = GlobalConstants.DEFAULT_DNA_WIDTH;
				height = GlobalConstants.DEFAULT_DNA_HEIGHT;
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_RNA) ||
					species.getSBOTermID().equals(GlobalConstants.SBO_RNA_SEGMENT)) {
				width = GlobalConstants.DEFAULT_RNA_WIDTH;
				height = GlobalConstants.DEFAULT_RNA_HEIGHT;
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_PROTEIN)) {
				width = GlobalConstants.DEFAULT_PROTEIN_WIDTH;
				height = GlobalConstants.DEFAULT_PROTEIN_HEIGHT;
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_NONCOVALENT_COMPLEX) ||
					SBMLutilities.sbo.isDescendantOf(species.getSBOTermID(), GlobalConstants.SBO_NONCOVALENT_COMPLEX)) {
				width = GlobalConstants.DEFAULT_COMPLEX_WIDTH;
				height = GlobalConstants.DEFAULT_COMPLEX_HEIGHT;
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_SIMPLE_CHEMICAL) ||
					SBMLutilities.sbo.isDescendantOf(species.getSBOTermID(), GlobalConstants.SBO_SIMPLE_CHEMICAL)) {
				width = GlobalConstants.DEFAULT_SMALL_MOLECULE_WIDTH;
				height = GlobalConstants.DEFAULT_SMALL_MOLECULE_HEIGHT;
			} else {
				width = GlobalConstants.DEFAULT_SPECIES_WIDTH;
				height = GlobalConstants.DEFAULT_SPECIES_HEIGHT;
			}
		} 
		return sizeAndPositionFromProperties(insertedVertex,width,height);
	}
	
	/**
	 * creates a vertex on the graph using the internal model.
	 * @param id
	 * 
	 * @return: A bool, true if the reaction had to be positioned.
	 */
	private boolean createGraphReactionFromModel(String id){
		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id);
		CellValueObject cvo = new CellValueObject(label, "Reaction", null);
		mxCell insertedVertex = (mxCell) this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		insertedVertex.setId(id);
		this.reactionsToMxCellMap.put(id, insertedVertex);
		
		this.setReactionStyles(id);
		
		return sizeAndPositionFromProperties(insertedVertex,
				GlobalConstants.DEFAULT_REACTION_WIDTH,GlobalConstants.DEFAULT_REACTION_HEIGHT);
	}
	
	private boolean createGraphCompartmentFromModel(String id){
		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id);
		CellValueObject cvo = new CellValueObject(label, "Compartment", null);
		mxCell cell = (mxCell) this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		cell.setId(id);
		cell.setConnectable(false);
		this.compartmentsToMxCellMap.put(id, cell);
		
		if (bioModel.IsWithinCompartment()) {
			this.setCompartmentStyles(id,true);
		} else {
			this.setCompartmentStyles(id,false);
		}
		
		return sizeAndPositionFromProperties(cell,GlobalConstants.DEFAULT_COMPARTMENT_WIDTH,
				GlobalConstants.DEFAULT_COMPARTMENT_HEIGHT);
	}
	
	private boolean createGraphRuleFromModel(String id){
		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id);
		CellValueObject cvo = new CellValueObject(label, "Rule", null);
		mxCell insertedVertex = (mxCell) this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		insertedVertex.setId(id);
		this.rulesToMxCellMap.put(id, insertedVertex);
		
		this.setRuleStyles(id);
		
		return sizeAndPositionFromProperties(insertedVertex,
				GlobalConstants.DEFAULT_RULE_WIDTH,GlobalConstants.DEFAULT_RULE_HEIGHT);
	}
	
	private boolean createGraphConstraintFromModel(String id){
		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id);
		CellValueObject cvo = new CellValueObject(label, "Constraint", null);
		mxCell insertedVertex = (mxCell) this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		insertedVertex.setId(id);
		this.constraintsToMxCellMap.put(id, insertedVertex);
		
		this.setConstraintStyles(id);
		
		return sizeAndPositionFromProperties(insertedVertex,
				GlobalConstants.DEFAULT_CONSTRAINT_WIDTH,GlobalConstants.DEFAULT_CONSTRAINT_HEIGHT);
	}
	
	private boolean createGraphEventFromModel(String id){
		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id);
		CellValueObject cvo = new CellValueObject(label, "Event", null);
		mxCell insertedVertex = (mxCell) this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		insertedVertex.setId(id);
		this.eventsToMxCellMap.put(id, insertedVertex);
		
		this.setEventStyles(id);
		
		return sizeAndPositionFromProperties(insertedVertex,
				GlobalConstants.DEFAULT_EVENT_WIDTH,GlobalConstants.DEFAULT_EVENT_HEIGHT);
	}
	
	private boolean createGraphTransitionFromModel(String id){
		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id);
		CellValueObject cvo = new CellValueObject(label, "Transition", null);
		mxCell insertedVertex = (mxCell) this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		insertedVertex.setId(id);
		this.eventsToMxCellMap.put(id, insertedVertex);
		
		this.setTransitionStyles(id);
		
		return sizeAndPositionFromProperties(insertedVertex,
				GlobalConstants.DEFAULT_TRANSITION_WIDTH,GlobalConstants.DEFAULT_TRANSITION_HEIGHT);
	}
	
	/**
	 * Creates a drawn promoter using the internal model
	 * @param pname
	 * @return
	 */
	private boolean createGraphDrawnPromoterFromModel(String id){
		
		/* String truncID;
		
		if (id.length() > 8)
			truncID = id.substring(0, 7) + "...";
		else truncID = id; */
		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id);

		CellValueObject cvo = new CellValueObject(label, "Promoter", null);
		mxCell insertedVertex = (mxCell) this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		insertedVertex.setId(id);
		this.drawnPromoterToMxCellMap.put(id, insertedVertex);
		
		this.setDrawnPromoterStyles(id);
		
		return sizeAndPositionFromProperties(insertedVertex,
				GlobalConstants.DEFAULT_SPECIES_WIDTH,GlobalConstants.DEFAULT_SPECIES_HEIGHT);
	}
	
	/**
	 * Creates a variable using the internal model
	 * @param pname
	 * @return
	 */
	private boolean createGraphVariableFromModel(String id){
		
		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id);
		if (bioModel.isInput(id)) {
			label += '\n' + GlobalConstants.INPUT;
		} else if (bioModel.isOutput(id)) {
			label += '\n' + GlobalConstants.OUTPUT;
		} else {
			//label += '\n' + GlobalConstants.INTERNAL;
		}
		CellValueObject cvo = new CellValueObject(label, "Variable", null);
		mxCell insertedVertex = (mxCell) this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		insertedVertex.setId(id);
		this.variableToMxCellMap.put(id, insertedVertex);
		
		this.setVariableStyles(id);
		
		return sizeAndPositionFromProperties(insertedVertex,
				GlobalConstants.DEFAULT_VARIABLE_WIDTH,GlobalConstants.DEFAULT_VARIABLE_HEIGHT);
	}
	
	/**
	 * Creates a variable using the internal model
	 * @param pname
	 * @return
	 */
	private boolean createGraphPlaceFromModel(String id,boolean marked){
		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id);
		if (bioModel.isInput(id)) {
			label += '\n' + GlobalConstants.INPUT;
		} else if (bioModel.isOutput(id)) {
			label += '\n' + GlobalConstants.OUTPUT;
		} else {
			//label += '\n' + GlobalConstants.INTERNAL;
		}
		CellValueObject cvo = new CellValueObject(label, "Place", null);
		mxCell insertedVertex = (mxCell) this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		insertedVertex.setId(id);
		this.variableToMxCellMap.put(id, insertedVertex);
		
		if (marked) {
			this.setMarkedPlaceStyles(id);
		} else {
			this.setPlaceStyles(id);
		}
		
		return sizeAndPositionFromProperties(insertedVertex,
				GlobalConstants.DEFAULT_VARIABLE_WIDTH,GlobalConstants.DEFAULT_VARIABLE_HEIGHT);
	}
	
	/**
	 * Creates a variable using the internal model
	 * @param pname
	 * @return
	 */
	private boolean createGraphBooleanFromModel(String id,boolean initial){

		String label = SBMLutilities.getArrayId(bioModel.getSBMLDocument(), id);
		CellValueObject cvo = new CellValueObject(label, "Boolean", null);
		mxCell insertedVertex = (mxCell) this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		insertedVertex.setId(id);
		this.variableToMxCellMap.put(id, insertedVertex);

		if (initial) {
			this.setTrueBooleanStyles(id);
		} else {
			this.setBooleanStyles(id);
		}
		
		return sizeAndPositionFromProperties(insertedVertex,
				GlobalConstants.DEFAULT_VARIABLE_WIDTH,GlobalConstants.DEFAULT_VARIABLE_HEIGHT);
	}
	
	/**
	 * creates an edge between two graph entities
	 */
	@Override
	public Object insertEdge(Object parent, String id, Object value, Object source, Object target, String style){
		
		Object ret = super.insertEdge(parent, id, value, source, target, style);
		this.influencesToMxCellMap.put(id, (mxCell)ret);
		
		return ret;
	}	
	
	
	//VISUALS
	
	/**
	 * Given an id, update the style of the influence based on the internal model.
	 */
	/*
	private void updateInfluenceVisuals(String id){
		
		Properties prop = gcm.getInfluences().get(id);
		
		//gcm.getSBMLDocument().getModel();
		
		if(prop == null)
			throw new Error("Invalid id '"+id+"'. Valid ids were:" + String.valueOf(gcm.getInfluences().keySet()));
		
		// build the edge style
		String style = "defaultEdge;" + mxConstants.STYLE_ENDARROW + "=";
		
		if(prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION))
			style = "ACTIVATION";
		else if(prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.REPRESSION))
			style = "REPRESSION";
		else if(prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.NOINFLUENCE))
			style = "NOINFLUENCE";
		else if(prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.COMPLEX))
			style = "COMPLEX";
		else
			style = "DEFAULT";
		
		// apply the style
		mxCell cell = this.getInfluence(id);
		cell.setStyle(style);

		// apply the promoter name as a label, only if the promoter isn't drawn.
		if(gcm.influenceHasExplicitPromoter(id) == false)
			cell.setValue(prop.getProperty(GlobalConstants.PROMOTER));
	};
	*/
	
	/**
	 * 
	 * @param cell
	 * @param label
	 */
	public void updateComponentConnectionVisuals(mxCell cell, String label){
		
		//cell.setStyle(mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
		cell.setStyle("COMPONENT_EDGE");
		cell.setValue("Port " + label);
		
		// position the label as intelligently as possible
		mxGeometry geom = cell.getGeometry();
		
		if(this.getCellType(cell.getSource()) == GlobalConstants.COMPONENT){
			geom.setX(-.6);
		}
		else{
			geom.setX(.6);
		}
		
		cell.setGeometry(geom);
	}
	
	/**
	 * Builds the style sheets that will be used by the graph.
	 */
	public void createStyleSheets(){
		Preferences biosimrc = Preferences.userRoot();
		mxStylesheet stylesheet = this.getStylesheet();
			
		String prefix;
		if (lema) {
			prefix = "lema";
		} else {
			prefix = "biosim";
		}
		
		//compartment
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Compartment", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Compartment", "true").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Compartment", "#FFFFFF"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Compartment", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Compartment", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Compartment", "50")));
		stylesheet.putCellStyle("SBMLCOMPARTMENT", style);
	
		//compartment
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Component", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Component", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Component", "#FFFFFF"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Component", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Component", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Component", "50")));
		stylesheet.putCellStyle("SBMLCOMPONENT", style);		

		//species / complex
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Species", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Species", "true").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Species", "#5CB4F2"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Species", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Species", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Species", "50")));
		stylesheet.putCellStyle("SPECIES", style);

		//DNA / RNA
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Species", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, false);
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Species", "#5CB4F2"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Species", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Species", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Species", "50")));
		stylesheet.putCellStyle("NUCLEIC_ACID", style);
		
		//Protein / Small Molecule
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		style.put(mxConstants.STYLE_ROUNDED, true);
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Species", "#5CB4F2"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Species", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Species", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Species", "50")));
		stylesheet.putCellStyle("MOLECULE", style);
		
		//reactions
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Reaction", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Reaction", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Reaction", "#C7007B"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Reaction", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Reaction", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Reaction", "50")));
		stylesheet.putCellStyle("REACTION", style);
		
		//rules
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Rule", mxConstants.SHAPE_SWIMLANE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Rule", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Rule", "#FFFF00"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Rule", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Rule", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Rule", "50")));
		stylesheet.putCellStyle("RULE", style);
		
		//rules excluded
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Rule", mxConstants.SHAPE_SWIMLANE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Rule", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Rule", "#FFFF00"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Rule", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Rule", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, 0);
		stylesheet.putCellStyle("RULE_EXCLUDE", style);
		
		//constraints
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Constraint", mxConstants.SHAPE_HEXAGON));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Constraint", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Constraint", "#FF0000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Constraint", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Constraint", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Constraint", "50")));
		stylesheet.putCellStyle("CONSTRAINT", style);
		
		//constraints excluded
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Constraint", mxConstants.SHAPE_HEXAGON));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Constraint", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Constraint", "#FF0000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Constraint", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Constraint", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, 0);
		stylesheet.putCellStyle("CONSTRAINT_EXCLUDE", style);
		
		//events
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Event", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Event", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Event", "#00FF00"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Event", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Event", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Event", "50")));
		stylesheet.putCellStyle("EVENT", style);
		
		//events excluded
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Event", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Event", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Event", "#00FF00"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Event", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Event", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, 0);
		stylesheet.putCellStyle("EVENT_EXCLUDE", style);
		
		//explicit promoter
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Promoter", mxConstants.SHAPE_RHOMBUS));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Promoter", "false").equals("true"));
		//style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
		style.put(mxConstants.STYLE_IMAGE,getClass().getResource("/icons/dna.png"));
		if (biosimrc.get(prefix+".schematic.shape.Promoter", mxConstants.SHAPE_RHOMBUS).equals(mxConstants.SHAPE_IMAGE)) {
			style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_BOTTOM);
		}
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Promoter", "#F00E0E"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Promoter", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Promoter", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Promoter", "50")));
		stylesheet.putCellStyle("EXPLICIT_PROMOTER", style);
		
		//sbol gene
		style = new Hashtable<String, Object>();
		//style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Promoter", mxConstants.SHAPE_RHOMBUS));
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Promoter", "false").equals("true"));
		style.put(mxConstants.STYLE_IMAGE,getClass().getResource("/icons/dna.png"));
		style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_BOTTOM);
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Promoter", "#F00E0E"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Promoter", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Promoter", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Promoter", "50")));
		stylesheet.putCellStyle("SBOL_GENE", style);
		
		//variable
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Variable", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Variable", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Variable", "#0000FF"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Variable", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Variable", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Variable", "50")));
		stylesheet.putCellStyle("VARIABLE", style);

		//boolean (false)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Boolean_False", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Boolean_False", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Boolean_False", "#FFFFFF"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Boolean_False", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Boolean_False", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Boolean_False", "50")));
		stylesheet.putCellStyle("BOOLEAN", style);

		//boolean (true)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Boolean_True", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Boolean_True", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Boolean_True", "#808080"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Boolean_True", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Boolean_True", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Boolean_True", "50")));
		stylesheet.putCellStyle("TRUE_BOOLEAN", style);

		//place (not marked)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Place_NotMarked", mxConstants.SHAPE_ELLIPSE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Place_NotMarked", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Place_NotMarked", "#FFFFFF"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Place_NotMarked", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Place_NotMarked", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Place_NotMarked", "50")));
		stylesheet.putCellStyle("PLACE", style);

		//marked place (marked)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Place_Marked", mxConstants.SHAPE_DOUBLE_ELLIPSE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Place_Marked", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Place_Marked", "#808080"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Place_Marked", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Place_Marked", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Place_Marked", "50")));
		stylesheet.putCellStyle("MARKED_PLACE", style);
		
		//transitions
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Transition", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Transition", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Transition", "#FFFFFF"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Transition", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Transition", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Transition", "50")));
		stylesheet.putCellStyle("TRANSITION", style);
		
		//fail transitions
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Transition", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Transition", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Transition", "#FFFFFF"));
		style.put(mxConstants.STYLE_STROKECOLOR, "#FF0000");
		style.put(mxConstants.STYLE_FONTCOLOR, "#FF0000");
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Transition", "50")));
		stylesheet.putCellStyle("FAILTRANSITION", style);
		
		//persistent transitions
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Transition", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Transition", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Transition", "#FFFFFF"));
		style.put(mxConstants.STYLE_STROKECOLOR, "#0000FF");
		style.put(mxConstants.STYLE_FONTCOLOR, "#0000FF");
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Transition", "50")));
		stylesheet.putCellStyle("PERSTRANSITION", style);
		
		//fail/persistent transitions
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.Transition", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.Transition", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Transition", "#FFFFFF"));
		style.put(mxConstants.STYLE_STROKECOLOR, "#FF00FF");
		style.put(mxConstants.STYLE_FONTCOLOR, "#FF00FF");
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Transition", "50")));
		stylesheet.putCellStyle("FAILPERSTRANSITION", style);
		
		//components
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.subComponent", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.subComponent", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.subComponent", "#87F274"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.subComponent", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.subComponent", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.subComponent", "50")));
		stylesheet.putCellStyle("COMPONENT", style);
		
		//compartments
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.subCompartment", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.subCompartment", "true").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.subCompartment", "#87F274"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.subCompartment", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.subCompartment", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.subCompartment", "50")));
		stylesheet.putCellStyle("COMPARTMENT", style);
		
		//grid components
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.subComponent", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.subComponent", "false").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.subComponent", "#87F274"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.subComponent", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.subComponent", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.subComponent", "50")));
		stylesheet.putCellStyle("GRIDCOMPONENT", style);
		
		//grid compartments
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, biosimrc.get(prefix+".schematic.shape.subCompartment", mxConstants.SHAPE_RECTANGLE));
		style.put(mxConstants.STYLE_ROUNDED, biosimrc.get(prefix+".schematic.rounded.subCompartment", "true").equals("true"));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.subCompartment", "#87F274"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.subCompartment", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.subCompartment", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.subCompartment", "50")));
		stylesheet.putCellStyle("GRIDCOMPARTMENT", style);
		
		//grid rectangle
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_FILLCOLOR, "none");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_ROUNDED, false);
		style.put(mxConstants.STYLE_MOVABLE, false);
		style.put(mxConstants.STYLE_RESIZABLE, false);
		style.put(mxConstants.STYLE_NOLABEL, true);
		stylesheet.putCellStyle("GRID_RECTANGLE", style);

		//production edge (promoter to species)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Production_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Production_Edge", "#34BA04"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Production_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Production_Edge", "100")));
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.Production_Edge", mxConstants.ARROW_OPEN));
		style.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ENTITY_RELATION);
		stylesheet.putCellStyle("PRODUCTION", style);
		
		//activation edge (species to species)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Activation_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Activation_Edge", "#34BA04"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Activation_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Activation_Edge", "100")));
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.Activation_Edge", mxConstants.ARROW_BLOCK));
		stylesheet.putCellStyle("ACTIVATION", style);
		
		//repression edge (species to species)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Repression_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Repression_Edge", "#FA2A2A"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Repression_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Repression_Edge", "100")));
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.Repression_Edge", mxConstants.ARROW_OVAL));
		stylesheet.putCellStyle("REPRESSION", style);
		
		//no influence (species to species)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.NoInfluence_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.NoInfluence_Edge", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.NoInfluence_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.NoInfluence_Edge", "100")));
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.NoInfluence_Edge", mxConstants.ARROW_DIAMOND));
		style.put(mxConstants.STYLE_DASHED, "true");
		stylesheet.putCellStyle("NOINFLUENCE", style);
		
		//complex formation edge (species to species)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Complex_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Complex_Edge", "#4E5D9C"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Complex_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Complex_Edge", "100")));
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.Complex_Edge", mxConstants.ARROW_OPEN));
		style.put(mxConstants.STYLE_DASHED, "true");
		stylesheet.putCellStyle("COMPLEX", style);
		
		//component edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Default_Edge", "100")));
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.Default_Edge", mxConstants.ARROW_OPEN));
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("COMPONENT_EDGE", style);	
	
		//reaction edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Default_Edge", "100")));
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.Default_Edge", mxConstants.ARROW_OPEN));
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("REACTION_EDGE", style);

		//reversible reaction edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Default_Edge", "100")));
		style.put(mxConstants.STYLE_STARTARROW, biosimrc.get(prefix+".schematic.shape.Default_Edge", mxConstants.ARROW_OPEN));
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.Default_Edge", mxConstants.ARROW_OPEN));
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("REV_REACTION_EDGE", style);

		//reversible reaction edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Default_Edge", "100")));
		style.put(mxConstants.STYLE_STARTARROW, mxConstants.NONE);
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.NONE);
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("MODIFIER_REACTION_EDGE", style);
		
		//rule edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.Default_Edge", mxConstants.ARROW_OPEN));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Default_Edge", "100")));
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("RULE_EDGE", style);
		
		//constraint edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.Default_Edge", mxConstants.ARROW_OPEN));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Default_Edge", "100")));
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("CONSTRAINT_EDGE", style);
		
		//event edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.Default_Edge", mxConstants.ARROW_OPEN));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Default_Edge", "100")));
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("EVENT_EDGE", style);

		//default edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_ENDARROW, biosimrc.get(prefix+".schematic.shape.Default_Edge", mxConstants.ARROW_OPEN));
		style.put(mxConstants.STYLE_FILLCOLOR, biosimrc.get(prefix+".schematic.color.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_STROKECOLOR, biosimrc.get(prefix+".schematic.strokeColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_FONTCOLOR, biosimrc.get(prefix+".schematic.fontColor.Default_Edge", "#000000"));
		style.put(mxConstants.STYLE_OPACITY, Integer.parseInt(biosimrc.get(prefix+".schematic.opacity.Default_Edge", "100")));
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("DEFAULT", style);
	}
	
	
	//STYLE SETTING
	
	/**
	 * 
	 * @param id
	 */
	private void setSpeciesStyles(String id){
		String style="SPECIES;";
		Species species = bioModel.getSBMLDocument().getModel().getSpecies(id);
		if (species!=null && species.isSetSBOTerm()) {
			if (species.getSBOTermID().equals(GlobalConstants.SBO_DNA) ||
					species.getSBOTermID().equals(GlobalConstants.SBO_DNA_SEGMENT)) {
				style = "NUCLEIC_ACID;";
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_RNA) ||
					species.getSBOTermID().equals(GlobalConstants.SBO_RNA_SEGMENT)) {
				style = "NUCLEIC_ACID;";
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_PROTEIN)) {
				style = "MOLECULE;";
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_NONCOVALENT_COMPLEX) ||
					SBMLutilities.sbo.isDescendantOf(species.getSBOTermID(), GlobalConstants.SBO_NONCOVALENT_COMPLEX)) {
				style="SPECIES;";
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_SIMPLE_CHEMICAL) ||
					SBMLutilities.sbo.isDescendantOf(species.getSBOTermID(), GlobalConstants.SBO_SIMPLE_CHEMICAL)) {
				style = "MOLECULE;";
			} 
		}
		mxCell cell = this.getSpeciesCell(id);
		cell.setStyle(style);
	}
	
	/**
	 * 
	 * @param id
	 */
	private void setReactionStyles(String id){
		String style="REACTION;";
		
		mxCell cell = this.getReactionsCell(id);
		cell.setStyle(style);
	}

	private void setCompartmentStyles(String id, boolean compart){
		String style;
		if (compart) {
			style="SBMLCOMPARTMENT;";
		} else {
			style="SBMLCOMPONENT;";
		}
		
		mxCell cell = this.getCompartmentsCell(id);
		cell.setStyle(style);
	}

	private void setRuleStyles(String id){
		String style="RULE;";
		if (!editable && modelEditor.getElementChanges().contains(id)) {
			style="RULE_EXCLUDE;";
		}
		
		mxCell cell = this.getRulesCell(id);
		cell.setStyle(style);
	}

	private void setConstraintStyles(String id){
		String style="CONSTRAINT;";
		if (!editable && modelEditor.getElementChanges().contains(id)) {
			style="CONSTRAINT_EXCLUDE;";
		}
		
		mxCell cell = this.getConstraintsCell(id);
		cell.setStyle(style);
	}

	private void setEventStyles(String id){
		String style="EVENT;";
		if (!editable && modelEditor.getElementChanges().contains(id)) {
			style="EVENT_EXCLUDE;";
		}
		
		mxCell cell = this.getEventsCell(id);
		cell.setStyle(style);
	}

	private void setTransitionStyles(String id){
		String style="TRANSITION;";
		if (SBMLutilities.isFailTransition(bioModel.getSBMLDocument().getModel().getEvent(id))) {
			if (SBMLutilities.isPersistentTransition(bioModel.getSBMLDocument(),
					bioModel.getSBMLDocument().getModel().getEvent(id))) {
				style="FAILPERSTRANSITION;";
			} else {
				style="FAILTRANSITION;";
			}
		} else if (SBMLutilities.isPersistentTransition(bioModel.getSBMLDocument(),
				bioModel.getSBMLDocument().getModel().getEvent(id))) {
			style="PERSTRANSITION;";
		}
		
		mxCell cell = this.getEventsCell(id);
		cell.setStyle(style);
	}
	
	/**
	 * 
	 * @param id
	 * @param compart
	 */
	private void setComponentStyles(String id, boolean compart){
		
		String style = "";
		
		if (modelEditor.getGrid().isEnabled()) {
			
			if (compart) style = "GRIDCOMPARTMENT;";
			else style = "GRIDCOMPONENT;";
		}
		else {
			
			if (compart) style = "COMPARTMENT;";
			else style = "COMPONENT;";
		}
		
		mxCell cell = this.getComponentCell(id);
		cell.setStyle(style);	
	}
	
	/**
	 * 
	 * @param id
	 */
	private void setDrawnPromoterStyles(String id){
		String style = "EXPLICIT_PROMOTER";
		if (bioModel.isSBOLGene(id)) {
			style = "SBOL_GENE";
		}
		
		mxCell cell = this.getDrawnPromoterCell(id);
		cell.setStyle(style);
	}
	
	/**
	 * 
	 * @param id
	 */
	private void setVariableStyles(String id){
		String style="VARIABLE";
		
		mxCell cell = this.getVariableCell(id);
		cell.setStyle(style);
	}
	
	/**
	 * 
	 * @param id
	 */
	private void setBooleanStyles(String id){
		String style="BOOLEAN";
		
		mxCell cell = this.getVariableCell(id);
		cell.setStyle(style);
	}
	
	/**
	 * 
	 * @param id
	 */
	private void setTrueBooleanStyles(String id){
		String style="TRUE_BOOLEAN";
		
		mxCell cell = this.getVariableCell(id);
		cell.setStyle(style);
	}
	
	/**
	 * 
	 * @param id
	 */
	private void setPlaceStyles(String id){
		String style="PLACE";
		
		mxCell cell = this.getVariableCell(id);
		cell.setStyle(style);
	}
	
	/**
	 * 
	 * @param id
	 */
	private void setMarkedPlaceStyles(String id){
		String style="MARKED_PLACE";
		
		mxCell cell = this.getVariableCell(id);
		cell.setStyle(style);
	}
	
	
	//ANIMATION
	
	/**
	 * 
	 */
	public void setSpeciesAnimationValue(String species, MovieAppearance appearance) {
		
		mxCell cell = this.speciesToMxCellMap.get(species);
		if (cell != null) {
			setCellAnimationValue(cell, appearance);
		}
	}
	
	/**
	 * 
	 */
	public void setParameterAnimationValue(String parameter, MovieAppearance appearance) {
		
		mxCell cell = this.variableToMxCellMap.get(parameter);
		if (cell != null) {
			setCellAnimationValue(cell, appearance);
		}
	}
	
	/**
	 * 
	 */
	public void setComponentAnimationValue(String component, MovieAppearance appearance) {
		
		mxCell cell = this.componentsToMxCellMap.get(component);
		setCellAnimationValue(cell, appearance);
	}
	
	public void setGridRectangleAnimationValue(String gridLocation, MovieAppearance appearance) {
		
		mxCell cell = this.gridRectangleToMxCellMap.get(gridLocation);
		setCellAnimationValue(cell, appearance);
	}
	
	/**
	 * Applies the MovieAppearance to the cell
	 * @param cell
	 * @param appearance
	 * @param properties
	 */
	private void setCellAnimationValue(mxCell cell, MovieAppearance appearance) {
		
		if (appearance == null)
			return;
		
		// color
		String newStyle = cell.getStyle() + ";";
		
		if (appearance.color != null) {
			
			newStyle += mxConstants.STYLE_FILLCOLOR + "=" + Integer.toHexString(appearance.color.getRGB()) + ";";
			newStyle += mxConstants.STYLE_OPACITY + "=" + 75;
		}
		
		// opacity
		if (appearance.opacity != null) {
			
			newStyle += ";";
			
			double op = (appearance.opacity) * 100.0;
			newStyle += mxConstants.STYLE_OPACITY + "=" + String.valueOf(op);
		}
		
		cell.setStyle(newStyle);
		
		// size
		if (appearance.size != null) {

			double x = 0;
			double y = 0;
			double width = 0;
			double height = 0;

			Layout layout = bioModel.getLayout();
			if (getCellType(cell).equals(GlobalConstants.SPECIES)||
					getCellType(cell).equals(GlobalConstants.PROMOTER)) {
				if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
					SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
					x = speciesGlyph.getBoundingBox().getPosition().getX();
					y = speciesGlyph.getBoundingBox().getPosition().getY();
					width = speciesGlyph.getBoundingBox().getDimensions().getWidth();
					height = speciesGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					x = -9999;
					y = -9999;
					width = GlobalConstants.DEFAULT_SPECIES_WIDTH;
					height = GlobalConstants.DEFAULT_SPECIES_HEIGHT;
				}
			} else if (getCellType(cell).equals(GlobalConstants.VARIABLE) || 
					getCellType(cell).equals(GlobalConstants.PLACE) || 
					getCellType(cell).equals(GlobalConstants.BOOLEAN)) {
				if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
					GeneralGlyph generalGlyph = (GeneralGlyph)
							layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
					x = generalGlyph.getBoundingBox().getPosition().getX();
					y = generalGlyph.getBoundingBox().getPosition().getY();
					width = generalGlyph.getBoundingBox().getDimensions().getWidth();
					height = generalGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					x = -9999;
					y = -9999;
					width = GlobalConstants.DEFAULT_SPECIES_WIDTH;
					height = GlobalConstants.DEFAULT_SPECIES_HEIGHT;
				}
			} else if (getCellType(cell).equals(GlobalConstants.REACTION)) {
				if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
					ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
					x = reactionGlyph.getBoundingBox().getPosition().getX();
					y = reactionGlyph.getBoundingBox().getPosition().getY();
					width = reactionGlyph.getBoundingBox().getDimensions().getWidth();
					height = reactionGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					x = -9999;
					y = -9999;
					width = GlobalConstants.DEFAULT_REACTION_WIDTH;
					height = GlobalConstants.DEFAULT_REACTION_HEIGHT;
				} 
			} else if (getCellType(cell).equals(GlobalConstants.RULE)||
					getCellType(cell).equals(GlobalConstants.CONSTRAINT)||
					getCellType(cell).equals(GlobalConstants.EVENT)||
					getCellType(cell).equals(GlobalConstants.TRANSITION)) {
				if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
					GeneralGlyph generalGlyph = (GeneralGlyph)
							layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
					x = generalGlyph.getBoundingBox().getPosition().getX();
					y = generalGlyph.getBoundingBox().getPosition().getY();
					width = generalGlyph.getBoundingBox().getDimensions().getWidth();
					height = generalGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					x = -9999;
					y = -9999;
					width = GlobalConstants.DEFAULT_REACTION_WIDTH;
					height = GlobalConstants.DEFAULT_REACTION_HEIGHT;
				} 
			} else if (getCellType(cell).equals(GlobalConstants.COMPARTMENT)) {
				if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
					CompartmentGlyph compGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+cell.getId());
					x = compGlyph.getBoundingBox().getPosition().getX();
					y = compGlyph.getBoundingBox().getPosition().getY();
					width = compGlyph.getBoundingBox().getDimensions().getWidth();
					height = compGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					x = -9999;
					y = -9999;
					width = GlobalConstants.DEFAULT_COMPONENT_WIDTH;
					height = GlobalConstants.DEFAULT_COMPONENT_HEIGHT;
				} 
			} else if (getCellType(cell).equals(GlobalConstants.COMPONENT)) {
				if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId())!=null) {
					GeneralGlyph generalGlyph = (GeneralGlyph)
							layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+cell.getId());
					x = generalGlyph.getBoundingBox().getPosition().getX();
					y = generalGlyph.getBoundingBox().getPosition().getY();
					width = generalGlyph.getBoundingBox().getDimensions().getWidth();
					height = generalGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					x = -9999;
					y = -9999;
					width = GlobalConstants.DEFAULT_COMPONENT_WIDTH;
					height = GlobalConstants.DEFAULT_COMPONENT_HEIGHT;
				} 
			} 
			
			double aspect_ratio = height / width;
			
			double centerX = x + width/2.0;
			double centerY = y + height/2.0;
			
			mxGeometry startG = cell.getGeometry();
			startG.setWidth(appearance.size);
			startG.setHeight(appearance.size * aspect_ratio);
			
			startG.setX(centerX - startG.getWidth()/2.0);
			startG.setY(centerY - startG.getHeight()/2.0);
		}
	}
	
	
	//CELL VALUE OBJECT CLASS
	
	/**
	 * The object that gets set as the mxCell value object.
	 * It is basically a way to store a property and label.
	 */
	public class CellValueObject extends Object implements Serializable{
		
		private static final long serialVersionUID = 918273645;
		public Properties prop;
		public String label;
		public String type;
		
		@Override
		/**
		 * 
		 */
		public String toString(){
			return this.label;
		}
		
		/**
		 * 
		 * @param oos
		 * @throws IOException
		 */
		private void writeObject(ObjectOutputStream oos)
			throws IOException {
			
			oos.writeObject(label);
			oos.writeObject(prop);
		}

		/**
		 * 
		 * @param ois
		 * @throws ClassNotFoundException
		 * @throws IOException
		 */
		private void readObject(ObjectInputStream ois)
		    throws ClassNotFoundException, IOException {
			
			label = ois.readObject().toString();
			prop = (Properties)ois.readObject();
		}
		
		/**
		 * 
		 * 
		 * @param label
		 * @param prop
		 */
		public CellValueObject(String label, String type, Properties prop){
			
			if(label == null || type == null)
				throw new Error("Neither label nor type can be null!");
			
			this.label = label;
			this.type = type;
			this.prop = prop;
		}
	}	

}