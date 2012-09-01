/**
 * 
 */
package biomodel.gui.schematic;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.sbml.libsbml.BoundingBox;
import org.sbml.libsbml.CompModelPlugin;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentGlyph;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.GeneralGlyph;
import org.sbml.libsbml.Layout;
import org.sbml.libsbml.LineSegment;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.ReactionGlyph;
import org.sbml.libsbml.ReferenceGlyph;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesGlyph;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.SpeciesReferenceGlyph;
import org.sbml.libsbml.TextGlyph;

import main.Gui;

//import javax.xml.bind.JAXBElement.GlobalScope;


import biomodel.gui.Grid;
import biomodel.gui.movie.MovieAppearance;
import biomodel.gui.textualeditor.SBMLutilities;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

/**
 * @author Tyler tpatterson80@gmail.com
 *
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
	
	
	/**
	 * constructor
	 * @param gcm
	 */
	public BioGraph(BioModel gcm) {
		
		super();
		
		// Turn editing off to prevent mxGraph from letting the user change the 
		// label on the cell. We want to do this using the property windows.
		this.setCellsEditable(false);
		
		this.bioModel = gcm;
		
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
	
	private void createLayoutConnection(Layout layout,String reactant,String product,String type) {
		ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+reactant+"__"+type+"__"+product);
		if (reactionGlyph!=null) reactionGlyph.removeFromParentAndDelete();
		reactionGlyph = layout.createReactionGlyph();
		reactionGlyph.setId(GlobalConstants.GLYPH+"__"+reactant+"__"+type+"__"+product);
		SpeciesReferenceGlyph speciesReferenceGlyph = reactionGlyph.createSpeciesReferenceGlyph();
		speciesReferenceGlyph.setId(GlobalConstants.GLYPH+"__"+reactant+"__product__"+product);
		speciesReferenceGlyph.setSpeciesGlyphId(GlobalConstants.GLYPH+"__"+product);
		speciesReferenceGlyph.setRole("product");
		LineSegment lineSegment = speciesReferenceGlyph.createLineSegment();
		lineSegment.setStart(this.getSpeciesOrPromoterCell(reactant).getGeometry().getCenterX(),
				this.getSpeciesOrPromoterCell(reactant).getGeometry().getCenterY());
		lineSegment.setEnd(this.getSpeciesOrPromoterCell(product).getGeometry().getCenterX(),
				this.getSpeciesOrPromoterCell(product).getGeometry().getCenterY());
	}

	private void addSpeciesReferenceGlyph(mxCell cell,ReactionGlyph reactionGlyph,String reactionId,String speciesId, String role) {
		SpeciesReferenceGlyph speciesReferenceGlyph = reactionGlyph.createSpeciesReferenceGlyph();
		speciesReferenceGlyph.setId(GlobalConstants.GLYPH+"__"+reactionId+"__"+role+"__"+speciesId);
		speciesReferenceGlyph.setSpeciesGlyphId(GlobalConstants.GLYPH+"__"+speciesId);
		speciesReferenceGlyph.setRole(role);
		LineSegment lineSegment = speciesReferenceGlyph.createLineSegment();
		lineSegment.setStart(cell.getSource().getGeometry().getCenterX(),cell.getSource().getGeometry().getCenterY());
		lineSegment.setEnd(cell.getTarget().getGeometry().getCenterX(),cell.getTarget().getGeometry().getCenterY());
	}

	private void addReferenceGlyph(mxCell cell,GeneralGlyph generalGlyph,String objectId,String refId, String role) {
		ReferenceGlyph referenceGlyph = generalGlyph.createReferenceGlyph();
		referenceGlyph.setId(GlobalConstants.GLYPH+"__"+objectId+"__"+role+"__"+refId);
		referenceGlyph.setGlyphId(GlobalConstants.GLYPH+"__"+refId);
		referenceGlyph.setRole(role);
		LineSegment lineSegment = referenceGlyph.createLineSegment();
		lineSegment.setStart(cell.getSource().getGeometry().getCenterX(),cell.getSource().getGeometry().getCenterY());
		lineSegment.setEnd(cell.getTarget().getGeometry().getCenterX(),cell.getTarget().getGeometry().getCenterY());
	}

	/**
	 * Builds the graph based on the internal representation
	 * @return
	 */	
	public boolean buildGraph() {
		
		this.isBuilding = true;

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
		
		Layout layout = bioModel.createLayout();
		for (long i = 0; i < bioModel.getSBMLDocument().getModel().getNumSpecies(); i++) {
			Species s = bioModel.getSBMLDocument().getModel().getSpecies(i);
			if (layout.getSpeciesGlyph(s.getId()) != null) {
				layout.getSpeciesGlyph(s.getId()).setId(GlobalConstants.GLYPH+"__"+s.getId());
				layout.getTextGlyph(s.getId()).setId(GlobalConstants.TEXT_GLYPH+"__"+s.getId());
			}
		}

		Model m = bioModel.getSBMLDocument().getModel();

		// add compartments
		if (!bioModel.isGridEnabled()) {
			for (long i = 0; i < m.getNumCompartments(); i++) {
				Compartment c = m.getCompartment(i);
				if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+c.getId()) != null) {
					if(createGraphCompartmentFromModel(c.getId())) needsPositioning = true;			
				} else {
					CompartmentGlyph compartmentGlyph = layout.createCompartmentGlyph();
					compartmentGlyph.setId(GlobalConstants.GLYPH+"__"+c.getId());
					compartmentGlyph.setCompartmentId(c.getId());
					compartmentGlyph.getBoundingBox().setX((i*1070)/m.getNumCompartments());
					compartmentGlyph.getBoundingBox().setY(0);
					compartmentGlyph.getBoundingBox().setWidth(1070/m.getNumCompartments());
					compartmentGlyph.getBoundingBox().setHeight(425);
					TextGlyph textGlyph = null;
					if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+c.getId())!=null) {
						textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+c.getId());
					} else {
						textGlyph = layout.createTextGlyph();
					}
					textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+c.getId());
					textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+c.getId());
					textGlyph.setText(c.getId());
					textGlyph.setBoundingBox(compartmentGlyph.getBoundingBox());
					if(createGraphCompartmentFromModel(c.getId()))
						needsPositioning = true;			
				}
			}
		}

		// add species
		for(String sp : bioModel.getSpecies()){
			
			if (bioModel.getSBMLDocument().getModel().getSpecies(sp).getAnnotationString().contains("type=\"grid\""))
				continue;
			
			if(createGraphSpeciesFromModel(sp))
				needsPositioning = true;
		}
		
		int x = 225;
		int y = 50;
		
		// add reactions
		for (int i = 0; i < m.getNumReactions(); i++) {
			
			Reaction r = m.getReaction(i);
			if (BioModel.isDegradationReaction(r)) continue;
			if (BioModel.isDiffusionReaction(r)) continue;
			if (BioModel.isProductionReaction(r)) continue;
			if (BioModel.isComplexReaction(r)) continue;
			if (BioModel.isConstitutiveReaction(r)) continue;
			if (r.getAnnotationString().contains("grid")) continue;
			
			if (layout.getReactionGlyph(r.getId()) != null) {
				layout.getReactionGlyph(r.getId()).setId(GlobalConstants.GLYPH+"__"+r.getId());
				layout.getTextGlyph(r.getId()).setId(GlobalConstants.TEXT_GLYPH+"__"+r.getId());
			}
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId()) != null) {
				if(createGraphReactionFromModel(r.getId()))	needsPositioning = true;			
			} else {
				if (r.getNumModifiers() > 0 || (r.getNumReactants()>1 && r.getNumProducts()>1) ||
					r.getNumReactants()==0 || r.getNumProducts()==0) {
					if(createGraphReactionFromModel(r.getId()))
						needsPositioning = true;			
				}
			}
		}
		
		// add rules
		for (long i = 0; i < m.getNumRules(); i++) {
			Rule rule = m.getRule(i);
			if (layout.getReactionGlyph(rule.getMetaId()) != null) {
				layout.getReactionGlyph(rule.getMetaId()).setId(GlobalConstants.GLYPH+"__"+rule.getMetaId());
				layout.getTextGlyph(rule.getMetaId()).setId(GlobalConstants.TEXT_GLYPH+"__"+rule.getMetaId());
			}
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+rule.getMetaId())!=null) {
				ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+rule.getMetaId());
				GeneralGlyph generalGlyph = layout.createGeneralGlyph();
				generalGlyph.setId(reactionGlyph.getId());
				generalGlyph.unsetMetaIdRef();
				generalGlyph.setReferenceId(reactionGlyph.getReactionId());
				generalGlyph.setBoundingBox(reactionGlyph.getBoundingBox());
				layout.removeReactionGlyph(GlobalConstants.GLYPH+"__"+rule.getMetaId());
			}
			if(createGraphRuleFromModel(rule.getMetaId())) needsPositioning = true;			
		}
		
		// add constraints
		for (long i = 0; i < m.getNumConstraints(); i++) {
			Constraint constraint = m.getConstraint(i);
			if (layout.getReactionGlyph(constraint.getMetaId()) != null) {
				layout.getReactionGlyph(constraint.getMetaId()).setId(GlobalConstants.GLYPH+"__"+constraint.getMetaId());
				layout.getTextGlyph(constraint.getMetaId()).setId(GlobalConstants.TEXT_GLYPH+"__"+constraint.getMetaId());
			}
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+constraint.getMetaId())!=null) {
				ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+constraint.getMetaId());
				GeneralGlyph generalGlyph = layout.createGeneralGlyph();
				generalGlyph.setId(reactionGlyph.getId());
				generalGlyph.unsetMetaIdRef();
				generalGlyph.setReferenceId(reactionGlyph.getReactionId());
				generalGlyph.setBoundingBox(reactionGlyph.getBoundingBox());
				layout.removeReactionGlyph(GlobalConstants.GLYPH+"__"+constraint.getMetaId());
			}
			if(createGraphConstraintFromModel(constraint.getMetaId())) needsPositioning = true;			
		}
		
		// add events
		for (long i = 0; i < m.getNumEvents(); i++) {
			Event event = m.getEvent(i);
			if (layout.getReactionGlyph(event.getId()) != null) {
				layout.getReactionGlyph(event.getId()).setId(GlobalConstants.GLYPH+"__"+event.getId());
				layout.getTextGlyph(event.getId()).setId(GlobalConstants.TEXT_GLYPH+"__"+event.getId());
			}
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+event.getId())!=null) {
				ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+event.getId());
				GeneralGlyph generalGlyph = layout.createGeneralGlyph();
				generalGlyph.setId(reactionGlyph.getId());
				generalGlyph.setReferenceId(reactionGlyph.getReactionId());
				generalGlyph.setBoundingBox(reactionGlyph.getBoundingBox());
				layout.removeReactionGlyph(GlobalConstants.GLYPH+"__"+event.getId());
			}
			if (SBMLutilities.isTransition(event)) {
				if(createGraphTransitionFromModel(event.getId())) needsPositioning = true;			
			} else {
				if(createGraphEventFromModel(event.getId())) needsPositioning = true;		
			}
		}

		// add all variables
		if (!bioModel.isGridEnabled()) {
			for (long i = 0; i < m.getNumParameters(); i++) {
				if (!m.getParameter(i).getConstant()) {
					String id = m.getParameter(i).getId();
					if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
						SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id);
						GeneralGlyph generalGlyph = layout.createGeneralGlyph();
						generalGlyph.setId(speciesGlyph.getId());
						generalGlyph.setReferenceId(speciesGlyph.getSpeciesId());
						generalGlyph.setBoundingBox(speciesGlyph.getBoundingBox());
						layout.removeSpeciesGlyph(GlobalConstants.GLYPH+"__"+id);
					}
					if (SBMLutilities.isPlace(m.getParameter(i))) {
						if(createGraphPlaceFromModel(m.getParameter(i).getId(),m.getParameter(i).getValue()==1.0))
							needsPositioning = true;
					} else {
						if(createGraphVariableFromModel(m.getParameter(i).getId()))
							needsPositioning = true;
					}
				}
			}
		}
		
		// add all components
		if (bioModel.isGridEnabled()) {
			
			for (long i = 0; i < m.getNumParameters(); ++i) {
				
				if (m.getParameter(i).getId().contains("__locations")) {
					
					String[] splitAnnotation = m.getParameter(i).getAnnotationString().replace("<annotation>","")
					.replace("</annotation>","").replace("\"","").replace("http://www.fakeuri.com","").replace("/>","").split("array:");
				
					for (int j = 2; j < splitAnnotation.length; ++j) {
						
						splitAnnotation[j] = splitAnnotation[j].trim();						
						String submodelID = splitAnnotation[j].split("=")[0];
						
						if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+submodelID)!=null) {
							CompartmentGlyph compartmentGlyph = 
									layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+submodelID);
							GeneralGlyph generalGlyph = layout.createGeneralGlyph();
							generalGlyph.setId(compartmentGlyph.getId());
							generalGlyph.setReferenceId(compartmentGlyph.getCompartmentId().replace(GlobalConstants.GLYPH+"__",""));
							generalGlyph.setBoundingBox(compartmentGlyph.getBoundingBox());
							layout.removeReactionGlyph(GlobalConstants.GLYPH+"__"+submodelID);
						}
						if (createGraphComponentFromModel(submodelID))
							needsPositioning = true;
					}					
				}				
			}
			
//			for (long i = 0; i < layout.getNumCompartmentGlyphs(); i++) {
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
			
			for (long i = 0; i < sbmlCompModel.getNumSubmodels(); i++) {
			
				String comp = sbmlCompModel.getSubmodel(i).getId();
			
				//String comp = gcm.getSBMLCompModel().getSubmodel(i).getId();
			
				//these are not meant to be displayed
				//if (comp.contains("GRID__"))
				//	continue;

				if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+comp)!=null) {
					CompartmentGlyph compartmentGlyph = 
							layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+comp);
					GeneralGlyph generalGlyph = layout.createGeneralGlyph();
					generalGlyph.setId(compartmentGlyph.getId());
					generalGlyph.setReferenceId(compartmentGlyph.getCompartmentId());
					generalGlyph.setBoundingBox(compartmentGlyph.getBoundingBox());
					layout.removeCompartmentGlyph(GlobalConstants.GLYPH+"__"+comp);
				}
				if (createGraphComponentFromModel(comp))
					needsPositioning = true;
			}
		}
		
		// add all the drawn promoters
		for(String prom : bioModel.getPromoters()){
			if (bioModel.isPromoterExplicit(prom)) {
				if(createGraphDrawnPromoterFromModel(prom))
					needsPositioning = true;
			}
		}
		
		boolean needsRedrawn = false;
		
		// add all the edges. 
		for (int i = 0; i < m.getNumReactions(); i++) {
			
			Reaction r = m.getReaction(i);
			if (r.getAnnotationString().contains("grid"))
				continue;
			
			if (BioModel.isComplexReaction(r)) {
				for (int j = 0; j < r.getNumReactants(); j++) {
					String reactant = r.getReactant(j).getSpecies();
					String product = r.getProduct(0).getSpecies();
					String id = reactant + "+>" + product;
					this.insertEdge(this.getDefaultParent(), id, "", this.getSpeciesCell(reactant), 
							this.getSpeciesCell(product));
					String style = "COMPLEX";
					mxCell cell = this.getInfluence(id);
					cell.setStyle(style);
					createLayoutConnection(layout,reactant,product,GlobalConstants.COMPLEX);
				}
			} else if (BioModel.isProductionReaction(r)) {
				String promoterId = r.getId().replace("Production_","");
				if (bioModel.isPromoterExplicit(promoterId)) {
					for (int j = 0; j < r.getNumProducts(); j++) {
						if (r.getProduct(j).getSpecies().endsWith("_mRNA")) continue;
						String product = r.getProduct(j).getSpecies();
						String id = promoterId + "->" + product;
						mxCell production = (mxCell)this.insertEdge(this.getDefaultParent(), id, "", 
								this.getDrawnPromoterCell(promoterId),this.getSpeciesCell(product));
						production.setStyle("PRODUCTION");
						createLayoutConnection(layout,promoterId,product,GlobalConstants.PRODUCTION);
					}
					for (int j = 0; j < r.getNumModifiers(); j++) {
						if (BioModel.isRepressor(r.getModifier(j))) {
							String repressor = r.getModifier(j).getSpecies();
							String id = repressor + "-|" + promoterId;
							this.insertEdge(this.getDefaultParent(), id, "", 
									this.getSpeciesCell(repressor),this.getDrawnPromoterCell(promoterId));
							String style = "REPRESSION";
							mxCell cell = this.getInfluence(id);
							cell.setStyle(style);
							createLayoutConnection(layout,repressor,promoterId,GlobalConstants.REPRESSION);
						} else if (BioModel.isActivator(r.getModifier(j))) {
							String activator = r.getModifier(j).getSpecies();
							String id = activator + "->" + promoterId;
							this.insertEdge(this.getDefaultParent(), id, "", 
									this.getSpeciesCell(activator),this.getDrawnPromoterCell(promoterId));
							String style = "ACTIVATION";
							mxCell cell = this.getInfluence(id);
							cell.setStyle(style);
							createLayoutConnection(layout,activator,promoterId,GlobalConstants.ACTIVATION);
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
							createLayoutConnection(layout,regulator,promoterId,GlobalConstants.REPRESSION);
							createLayoutConnection(layout,regulator,promoterId,GlobalConstants.ACTIVATION);
						} else if (r.getModifier(j).getAnnotationString().contains(GlobalConstants.NOINFLUENCE)) {
							String regulator = r.getModifier(j).getSpecies();
							String id = regulator + "x>" + promoterId;
							this.insertEdge(this.getDefaultParent(), id, "", 
									this.getSpeciesCell(regulator),this.getDrawnPromoterCell(promoterId));
							String style = "NOINFLUENCE";
							mxCell cell = this.getInfluence(id);
							cell.setStyle(style);
							createLayoutConnection(layout,regulator,promoterId,GlobalConstants.NOINFLUENCE);						}
					}
				} else {
					for (int j = 0; j < r.getNumModifiers(); j++) {
						for (int k = 0; k < r.getNumProducts(); k++) {
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
								createLayoutConnection(layout,repressor,product,GlobalConstants.REPRESSION);
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
								createLayoutConnection(layout,activator,product,GlobalConstants.REPRESSION);
							} else if (r.getModifier(j).getAnnotationString().contains(GlobalConstants.NOINFLUENCE)) {
								String regulator = r.getModifier(j).getSpecies();
								String product = r.getProduct(k).getSpecies();
								String id = regulator + "x>" + product + "," + promoterId;
								this.insertEdge(this.getDefaultParent(), id, "", 
										this.getSpeciesCell(regulator),this.getSpeciesCell(product));
								String style = "NOINFLUENCE";
								mxCell cell = this.getInfluence(id);
								cell.setStyle(style);
								cell.setValue(promoterId);
								createLayoutConnection(layout,regulator,product,GlobalConstants.NOINFLUENCE);
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
								createLayoutConnection(layout,regulator,product,GlobalConstants.ACTIVATION);
								createLayoutConnection(layout,regulator,product,GlobalConstants.REPRESSION);
							}
						}
					}
				}
			}
		}

		//add reactions
		for (int i = 0; i < m.getNumReactions(); i++) {
			
			Reaction r = m.getReaction(i);
			if (BioModel.isDegradationReaction(r)) continue;
			if (BioModel.isDiffusionReaction(r)) continue;
			if (BioModel.isProductionReaction(r)) continue;
			if (BioModel.isComplexReaction(r)) continue;
			if (BioModel.isConstitutiveReaction(r)) continue;
			if (r.getAnnotationString().contains("grid")) continue;
			
			ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId());
			if (reactionGlyph != null) {
				while (reactionGlyph.getNumSpeciesReferenceGlyphs() > 0) 
					reactionGlyph.removeSpeciesReferenceGlyph(0);
				for (int j = 0; j < r.getNumReactants(); j++) {
					
					SpeciesReference s = r.getReactant(j);
					mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
							s.getSpecies() + "__" + r.getId(), "", 
							this.getSpeciesCell(s.getSpecies()), 
							this.getReactionsCell(r.getId()));

					if (r.getReversible()) {
						
						if (s.getStoichiometry() != 1.0)
							cell.setValue(s.getStoichiometry()+",r");
						else 
							cell.setValue("r");
						
						cell.setStyle("REACTION_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN +
								";" + mxConstants.STYLE_STARTARROW + "=" + mxConstants.ARROW_OPEN);
					} 
					else {
						if (s.getStoichiometry() != 1.0)
							cell.setValue(s.getStoichiometry());
						
						cell.setStyle("REACTION_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
					}
					String reactant = s.getSpecies();
					addSpeciesReferenceGlyph(cell,reactionGlyph,r.getId(),reactant,"substrate");
				}
				
				for (int j = 0; j < r.getNumModifiers(); j++) {
					
					ModifierSpeciesReference s = r.getModifier(j);
					mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
							s.getSpecies() + "__" + r.getId(), "", 
							this.getSpeciesCell(s.getSpecies()), 
							this.getReactionsCell(r.getId()));

					if (r.getReversible())
						cell.setValue("m");
					
					cell.setStyle("REACTION_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.NONE);
					String modifier = s.getSpecies();
					addSpeciesReferenceGlyph(cell,reactionGlyph,r.getId(),modifier,"modifier");
				}
				
				for (int k = 0; k < r.getNumProducts(); k++) {
					
					SpeciesReference s = r.getProduct(k);
					mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
							r.getId() + "__" + s.getSpecies(), "", 
							this.getReactionsCell(r.getId()),
							this.getSpeciesCell(s.getSpecies()));
					
					if (r.getReversible()) {
						
						if (s.getStoichiometry() != 1.0)
							cell.setValue(s.getStoichiometry()+",p");
						else 
							cell.setValue("p");
						
						cell.setStyle("REACTION_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN +
								";" + mxConstants.STYLE_STARTARROW + "=" + mxConstants.ARROW_OPEN);
					} 
					else {
						
						if (s.getStoichiometry() != 1.0)
							cell.setValue(s.getStoichiometry());
						
						cell.setStyle("REACTION_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
					}
					String product = s.getSpecies();
					addSpeciesReferenceGlyph(cell,reactionGlyph,r.getId(),product,"product");
				}
				
				if (r.isSetKineticLaw()) { 
					String initStr = SBMLutilities.myFormulaToString(r.getKineticLaw().getMath());
					String[] vars = initStr.split(" |\\(|\\)|\\,");
					for (int j = 0; j < vars.length; j++) {
						Parameter parameter = m.getParameter(vars[j]);
						if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									parameter.getId() + "__" + r.getId(), "", 
									this.getVariableCell(parameter.getId()), 
									this.getReactionsCell(r.getId()));
							cell.setStyle("REACTION_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
							addSpeciesReferenceGlyph(cell,reactionGlyph,r.getId(),parameter.getId(),"substrate");
						}
					}
				}
			} 
			else {
				for (int j = 0; j < r.getNumReactants(); j++) {
					
					SpeciesReference s1 = r.getReactant(j);
					
					for (int k = 0; k < r.getNumProducts(); k++) {
						
						SpeciesReference s2 = r.getProduct(k);
						mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
								s1.getSpecies() + "_" + r.getId() + "_" + s2.getSpecies(), "", 
								this.getSpeciesCell(s1.getSpecies()), 
								this.getSpeciesCell(s2.getSpecies()));
						
						cell.setValue(r.getId());
						
						if (r.getReversible()) {
							cell.setStyle("REACTION_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN +
									";" + mxConstants.STYLE_STARTARROW + "=" + mxConstants.ARROW_OPEN);
						} 
						else {
							cell.setStyle("REACTION_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
						}
					}
				}
			}
		}

		//add rules
		for (int i = 0; i < m.getNumRules(); i++) {
			Rule r = m.getRule(i);
			GeneralGlyph generalGlyph = (GeneralGlyph)
					layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+r.getMetaId());
			if (generalGlyph != null) {
				while (generalGlyph.getNumReferenceGlyphs() > 0) 
					generalGlyph.removeReferenceGlyph(0);
				String initStr = SBMLutilities.myFormulaToString(r.getMath());
				String[] vars = initStr.split(" |\\(|\\)|\\,");
				for (int j = 0; j < vars.length; j++) {
					Species species = m.getSpecies(vars[j]);
					if (species != null) {
						mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
								species.getId() + "__" + r.getMetaId(), "", 
								this.getSpeciesOrPromoterCell(species.getId()), 
								this.getRulesCell(r.getMetaId()));
						cell.setStyle("RULE_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
						addReferenceGlyph(cell,generalGlyph,r.getMetaId(),species.getId(),"substrate");
					} else {
						Parameter parameter = m.getParameter(vars[j]);
						if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									parameter.getId() + "__" + r.getMetaId(), "", 
									this.getVariableCell(parameter.getId()), 
									this.getRulesCell(r.getMetaId()));
							cell.setStyle("RULE_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
							addReferenceGlyph(cell,generalGlyph,r.getMetaId(),parameter.getId(),"substrate");
						}
					}
				}
				// Add variable
				if (r.isAssignment() || r.isRate()) {
					Species species = m.getSpecies(r.getVariable());
					if (species != null) {
						mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
								r.getMetaId() + "__" + species.getId(), "", 
								this.getRulesCell(r.getMetaId()),
								this.getSpeciesOrPromoterCell(species.getId()));
						cell.setStyle("RULE_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
						addReferenceGlyph(cell,generalGlyph,r.getMetaId(),species.getId(),"product");
					} else {
						Parameter parameter = m.getParameter(r.getVariable());
						if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									r.getMetaId() + "__" + parameter.getId(), "", 
									this.getRulesCell(r.getMetaId()),
									this.getVariableCell(parameter.getId()));
							cell.setStyle("RULE_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
							addReferenceGlyph(cell,generalGlyph,r.getMetaId(),parameter.getId(),"product");
						}
					}
				}
			} 
		}
		
		// add constraints
		for (int i = 0; i < m.getNumConstraints(); i++) {
			Constraint c = m.getConstraint(i);
			GeneralGlyph generalGlyph = (GeneralGlyph)
					layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+c.getMetaId());
			if (generalGlyph != null) {
				while (generalGlyph.getNumReferenceGlyphs() > 0) 
					generalGlyph.removeReferenceGlyph(0);
				String initStr = SBMLutilities.myFormulaToString(c.getMath());
				String[] vars = initStr.split(" |\\(|\\)|\\,");
				for (int j = 0; j < vars.length; j++) {
					Species species = m.getSpecies(vars[j]);
					if (species != null) {
						mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
								species.getId() + "__" + c.getMetaId(), "", 
								this.getSpeciesOrPromoterCell(species.getId()), 
								this.getConstraintsCell(c.getMetaId()));
						cell.setStyle("CONSTRAINT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
						addReferenceGlyph(cell,generalGlyph,c.getMetaId(),species.getId(),"substrate");
					} else {
						Parameter parameter = m.getParameter(vars[j]);
						if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									parameter.getId() + "__" + c.getMetaId(), "", 
									this.getVariableCell(parameter.getId()), 
									this.getConstraintsCell(c.getMetaId()));
							cell.setStyle("CONSTRAINT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
							addReferenceGlyph(cell,generalGlyph,c.getMetaId(),parameter.getId(),"substrate");
						}
					}
				}
			} 
		}
		
		// add event edges
		for (int i = 0; i < m.getNumEvents(); i++) {
			Event e = m.getEvent(i);
			boolean isTransition = SBMLutilities.isTransition(e);
			GeneralGlyph generalGlyph = (GeneralGlyph)
					layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+e.getId());
			if (generalGlyph != null) {
				while (generalGlyph.getNumReferenceGlyphs() > 0) 
					generalGlyph.removeReferenceGlyph(0);
				if (!isTransition && e.isSetTrigger()) {
					String initStr = SBMLutilities.myFormulaToString(e.getTrigger().getMath());
					String[] vars = initStr.split(" |\\(|\\)|\\,");
					for (int j = 0; j < vars.length; j++) {
						Species species = m.getSpecies(vars[j]);
						if (species != null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									species.getId() + "__" + e.getId(), "", 
									this.getSpeciesOrPromoterCell(species.getId()), 
									this.getEventsCell(e.getId()));
							cell.setStyle("EVENT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
							addReferenceGlyph(cell,generalGlyph,e.getId(),species.getId(),"substrate");
						} else {
							Parameter parameter = m.getParameter(vars[j]);
							if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
								mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
										parameter.getId() + "__" + e.getId(), "", 
										this.getVariableCell(parameter.getId()), 
										this.getEventsCell(e.getId()));
								cell.setStyle("EVENT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
								addReferenceGlyph(cell,generalGlyph,e.getId(),parameter.getId(),"substrate");
							}
						}
					}
				}
				if (!isTransition && e.isSetDelay()) {
					String initStr = SBMLutilities.myFormulaToString(e.getDelay().getMath());
					String [] vars = initStr.split(" |\\(|\\)|\\,");
					for (int j = 0; j < vars.length; j++) {
						Species species = m.getSpecies(vars[j]);
						if (species != null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									species.getId() + "__" + e.getId(), "", 
									this.getSpeciesOrPromoterCell(species.getId()), 
									this.getEventsCell(e.getId()));
							cell.setStyle("EVENT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
							addReferenceGlyph(cell,generalGlyph,e.getId(),species.getId(),"substrate");
						} else {
							Parameter parameter = m.getParameter(vars[j]);
							if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
								mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
										parameter.getId() + "__" + e.getId(), "", 
										this.getVariableCell(parameter.getId()), 
										this.getEventsCell(e.getId()));
								cell.setStyle("EVENT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
								addReferenceGlyph(cell,generalGlyph,e.getId(),parameter.getId(),"substrate");
							}
						}
					}
				}
				if (!isTransition && e.isSetPriority()) {
					String initStr = SBMLutilities.myFormulaToString(e.getPriority().getMath());
					String [] vars = initStr.split(" |\\(|\\)|\\,");
					for (int j = 0; j < vars.length; j++) {
						Species species = m.getSpecies(vars[j]);
						if (species != null) {
							mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
									species.getId() + "__" + e.getId(), "", 
									this.getSpeciesOrPromoterCell(species.getId()), 
									this.getEventsCell(e.getId()));
							cell.setStyle("EVENT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
							addReferenceGlyph(cell,generalGlyph,e.getId(),species.getId(),"substrate");
						} else {
							Parameter parameter = m.getParameter(vars[j]);
							if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
								mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
										parameter.getId() + "__" + e.getId(), "", 
										this.getVariableCell(parameter.getId()), 
										this.getEventsCell(e.getId()));
								cell.setStyle("EVENT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
								addReferenceGlyph(cell,generalGlyph,e.getId(),parameter.getId(),"substrate");
							}
						}
					}
				}
				// Add variable
				for (int k = 0; k < e.getNumEventAssignments(); k++) {
					EventAssignment ea = e.getEventAssignment(k);
					String initStr = SBMLutilities.myFormulaToString(ea.getMath());
					if (!isTransition) {
						String [] vars = initStr.split(" |\\(|\\)|\\,");
						for (int j = 0; j < vars.length; j++) {
							Species species = m.getSpecies(vars[j]);
							if (species != null) {
								mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
										species.getId() + "__" + e.getId(), "", 
										this.getSpeciesOrPromoterCell(species.getId()), 
										this.getEventsCell(e.getId()));
								cell.setStyle("EVENT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
								addReferenceGlyph(cell,generalGlyph,e.getId(),species.getId(),"substrate");
							}else {
								Parameter parameter = m.getParameter(vars[j]);
								if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
									mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
											parameter.getId() + "__" + e.getId(), "", 
											this.getVariableCell(parameter.getId()), 
											this.getEventsCell(e.getId()));
									cell.setStyle("EVENT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
									addReferenceGlyph(cell,generalGlyph,e.getId(),parameter.getId(),"substrate");
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
						cell.setStyle("EVENT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
						addReferenceGlyph(cell,generalGlyph,e.getId(),species.getId(),"product");
					} else if (species==null){
						Parameter parameter = m.getParameter(ea.getVariable());
						if (parameter != null) {
							boolean isPlace = SBMLutilities.isPlace(parameter);
							if (!isTransition || (isPlace && initStr.equals("1"))) {
								if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
									mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
											e.getId() + "__" + parameter.getId(), "", 
											this.getEventsCell(e.getId()),
											this.getVariableCell(parameter.getId()));
									if (isTransition && isPlace) {
										cell.setStyle("TRANSITION_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
									} else {
										cell.setStyle("EVENT_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
									}
									addReferenceGlyph(cell,generalGlyph,e.getId(),parameter.getId(),"product");
								}
							} else if (isPlace && initStr.equals("0")) {
								if (parameter != null && this.getVariableCell(parameter.getId())!=null) {
									mxCell cell = (mxCell)this.insertEdge(this.getDefaultParent(), 
											e.getId() + "__" + parameter.getId(), "", 
											this.getVariableCell(parameter.getId()),
											this.getEventsCell(e.getId()));
									cell.setStyle("TRANSITION_EDGE;" + mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
									addReferenceGlyph(cell,generalGlyph,e.getId(),parameter.getId(),"substrate");
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
		for (int i = 0; i < m.getNumReactions(); i++) {
			Reaction r = m.getReaction(i);
			if (BioModel.isComplexReaction(r)) {
				for (int j = 0; j < r.getNumReactants(); j++) {
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
				String promoterId = r.getId().replace("Production_","");
				if (!bioModel.isPromoterExplicit(promoterId)) {
					for (int j = 0; j < r.getNumModifiers(); j++) {
						for (int k = 0; k < r.getNumProducts(); k++) {
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
							} else if (r.getModifier(j).getAnnotationString().contains(GlobalConstants.NOINFLUENCE)) {
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
					for (int j = 0; j < r.getNumModifiers(); j++) {
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
		
		for (int i = 0; i < m.getNumReactions(); i++) {
			
			Reaction r = m.getReaction(i);
			if (BioModel.isDegradationReaction(r)) continue;
			if (BioModel.isDiffusionReaction(r)) continue;
			if (BioModel.isProductionReaction(r)) continue;
			if (BioModel.isComplexReaction(r)) continue;
			if (BioModel.isConstitutiveReaction(r)) continue;
			
			if (bioModel.getSBMLLayout().getLayout("iBioSim").getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId()) != null) {
				
				for (int j = 0; j < r.getNumReactants(); j++) {
					
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
					mxCell cell = this.getInfluence(s.getSpecies() + "__" + r.getId());
					
					if(edgeHash.containsKey(key) == false)
						edgeHash.put(key, new Vector<mxCell>());
					
					edgeHash.get(key).add(cell);
				}
				
				for (int j = 0; j < r.getNumModifiers(); j++) {
					
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
					mxCell cell = this.getInfluence(s.getSpecies() + "__" + r.getId());
					
					if(edgeHash.containsKey(key) == false)
						edgeHash.put(key, new Vector<mxCell>());
					
					edgeHash.get(key).add(cell);
				}
				
				for (int k = 0; k < r.getNumProducts(); k++) {
					
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
					mxCell cell = this.getInfluence(r.getId() + "__" + s.getSpecies());
					
					if(edgeHash.containsKey(key) == false)
						edgeHash.put(key, new Vector<mxCell>());
					
					edgeHash.get(key).add(cell);
				}
			} 
			else {
				for (int j = 0; j < r.getNumReactants(); j++) {
					
					SpeciesReference s1 = r.getReactant(j);
					
					for (int k = 0; k < r.getNumProducts(); k++) {
						
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
		for (long i = 0; i < bioModel.getSBMLCompModel().getNumSubmodels(); i++) {
			String compName = bioModel.getSBMLCompModel().getSubmodel(i).getId();
			for (String propName : bioModel.getInputs(compName).keySet()) {
				String targetName = bioModel.getInputs(compName).get(propName);
				String type = "Input";
				String key = compName + " "+type+" " + targetName;
				mxCell cell = componentsConnectionsToMxCellMap.get(key);
				String simpleKey = compName + " " + targetName;
				if(edgeHash.containsKey(simpleKey) == false)
					edgeHash.put(simpleKey, new Vector<mxCell>());
				edgeHash.get(simpleKey).add(cell);
			}
			for (String propName : bioModel.getOutputs(compName).keySet()) {
				String targetName = bioModel.getOutputs(compName).get(propName);
				String type = "Output";
				String key = compName + " "+type+" " + targetName;
				mxCell cell = componentsConnectionsToMxCellMap.get(key);
				String simpleKey = compName + " " + targetName;
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
		
		if (bioModel.getGrid().isEnabled()) {
			
			int gridRows = bioModel.getGrid().getNumRows();
			int gridCols = bioModel.getGrid().getNumCols();
			double gridWidth = bioModel.getGrid().getGridGeomWidth();
			double gridHeight = bioModel.getGrid().getGridGeomHeight();
			
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

		for(mxCell cell:this.compartmentsToMxCellMap.values()){
			updateInternalPosition(cell);
		}

		for(mxCell cell:this.speciesToMxCellMap.values()){
			updateInternalPosition(cell);
		}
		
		for(mxCell cell:this.reactionsToMxCellMap.values()){
			updateInternalPosition(cell);
		}
		
		for(mxCell cell:this.rulesToMxCellMap.values()){
			updateInternalPosition(cell);
		}
		
		for(mxCell cell:this.constraintsToMxCellMap.values()){
			updateInternalPosition(cell);
		}
		
		for(mxCell cell:this.eventsToMxCellMap.values()){
			updateInternalPosition(cell);
		}
		
		for(mxCell cell:this.componentsToMxCellMap.values()){
			updateInternalPosition(cell);
		}
		
		for(mxCell cell:this.drawnPromoterToMxCellMap.values()){
			updateInternalPosition(cell);
		}
		
		for(mxCell cell:this.variableToMxCellMap.values()){
			updateInternalPosition(cell);
		}
	}
	
	/**
	 * Given a cell that must be a species or component,
	 * update the internal model to reflect it's coordinates.
	 * Called when a cell is dragged with the GUI.
	 */
	public void updateInternalPosition(mxCell cell){
		
		mxGeometry geom = cell.getGeometry();
		if (getCellType(cell).equals(GlobalConstants.SPECIES) ||
				getCellType(cell).equals(GlobalConstants.PROMOTER)) {
			Layout layout = null;
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
			} else {
				layout = bioModel.getSBMLLayout().createLayout();
				layout.setId("iBioSim");
			}
			SpeciesGlyph speciesGlyph = null;
			if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
				speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			} else {
				speciesGlyph = layout.createSpeciesGlyph();
				speciesGlyph.setId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
				speciesGlyph.setSpeciesId((String)cell.getId());
			}
			String compartment = bioModel.getCompartmentByLocation((float)geom.getX(),(float)geom.getY(),
					(float)geom.getWidth(),(float)geom.getHeight());
			if (compartment.equals("")) {
				geom.setX(speciesGlyph.getBoundingBox().x());
				geom.setY(speciesGlyph.getBoundingBox().y());
				geom.setWidth(speciesGlyph.getBoundingBox().width());
				geom.setHeight(speciesGlyph.getBoundingBox().height());
				Utility.createErrorMessage("Compartment Required", "Species must be placed within a compartment.");
				return;
			} 
			bioModel.getSBMLDocument().getModel().getSpecies((String)cell.getId()).setCompartment(compartment);
			speciesGlyph.getBoundingBox().setX(geom.getX());
			speciesGlyph.getBoundingBox().setY(geom.getY());
			speciesGlyph.getBoundingBox().setWidth(geom.getWidth());
			speciesGlyph.getBoundingBox().setHeight(geom.getHeight());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			} else {
				textGlyph = layout.createTextGlyph();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			textGlyph.setText((String)cell.getId());
			textGlyph.setBoundingBox(speciesGlyph.getBoundingBox());
		} else if (getCellType(cell).equals(GlobalConstants.VARIABLE) || 
				getCellType(cell).equals(GlobalConstants.PLACE)) {
			Layout layout = null;
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
			} else {
				layout = bioModel.getSBMLLayout().createLayout();
				layout.setId("iBioSim");
			}
			GeneralGlyph generalGlyph = null;
			if (layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
				generalGlyph = (GeneralGlyph)layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			} else {
				generalGlyph = layout.createGeneralGlyph();
				generalGlyph.setId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
				generalGlyph.setReferenceId((String)cell.getId());
			}
			generalGlyph.getBoundingBox().setX(geom.getX());
			generalGlyph.getBoundingBox().setY(geom.getY());
			generalGlyph.getBoundingBox().setWidth(geom.getWidth());
			generalGlyph.getBoundingBox().setHeight(geom.getHeight());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			} else {
				textGlyph = layout.createTextGlyph();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			textGlyph.setText((String)cell.getId());
			textGlyph.setBoundingBox(generalGlyph.getBoundingBox());
		} else if (getCellType(cell).equals(GlobalConstants.REACTION)) {
			Layout layout = null;
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
			} else {
				layout = bioModel.getSBMLLayout().createLayout();
				layout.setId("iBioSim");
			}
			ReactionGlyph reactionGlyph = null;
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
				reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			} else {
				reactionGlyph = layout.createReactionGlyph();
				reactionGlyph.setId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
				reactionGlyph.setReactionId((String)cell.getId());
			}
			if (getCellType(cell).equals(GlobalConstants.REACTION)) {
				String compartment = bioModel.getCompartmentByLocation((float)geom.getX(),(float)geom.getY(),
						(float)geom.getWidth(),(float)geom.getHeight());
				if (compartment.equals("")) {
					geom.setX(reactionGlyph.getBoundingBox().x());
					geom.setY(reactionGlyph.getBoundingBox().y());
					geom.setWidth(reactionGlyph.getBoundingBox().width());
					geom.setHeight(reactionGlyph.getBoundingBox().height());
					Utility.createErrorMessage("Compartment Required", "Reaction must be placed within a compartment.");
					return;
				} 
				bioModel.getSBMLDocument().getModel().getReaction((String)cell.getId()).setCompartment(compartment);
			}
			reactionGlyph.getBoundingBox().setX(geom.getX());
			reactionGlyph.getBoundingBox().setY(geom.getY());
			reactionGlyph.getBoundingBox().setWidth(geom.getWidth());
			reactionGlyph.getBoundingBox().setHeight(geom.getHeight());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			} else {
				textGlyph = layout.createTextGlyph();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			textGlyph.setText((String)cell.getId());
			textGlyph.setBoundingBox(reactionGlyph.getBoundingBox());
		} else if (getCellType(cell).equals(GlobalConstants.RULE)||
				getCellType(cell).equals(GlobalConstants.CONSTRAINT)||
				getCellType(cell).equals(GlobalConstants.EVENT)||
				getCellType(cell).equals(GlobalConstants.TRANSITION)) {
			Layout layout = null;
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
			} else {
				layout = bioModel.getSBMLLayout().createLayout();
				layout.setId("iBioSim");
			}
			GeneralGlyph generalGlyph = null;
			if (layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
				generalGlyph = (GeneralGlyph)
						layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			} else {
				generalGlyph = layout.createGeneralGlyph();
				generalGlyph.setId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
				if (getCellType(cell).equals(GlobalConstants.EVENT)) {
					generalGlyph.setReferenceId((String)cell.getId());
				} else {
					generalGlyph.unsetMetaIdRef();
					generalGlyph.setReferenceId((String)cell.getId());
				}
			}
			generalGlyph.getBoundingBox().setX(geom.getX());
			generalGlyph.getBoundingBox().setY(geom.getY());
			generalGlyph.getBoundingBox().setWidth(geom.getWidth());
			generalGlyph.getBoundingBox().setHeight(geom.getHeight());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			} else {
				textGlyph = layout.createTextGlyph();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			textGlyph.setText((String)cell.getId());
			textGlyph.setBoundingBox(generalGlyph.getBoundingBox());		
		} else if (!bioModel.isGridEnabled() && getCellType(cell).equals(GlobalConstants.COMPARTMENT)) {
			Layout layout = null;
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
			} else {
				layout = bioModel.getSBMLLayout().createLayout();
				layout.setId("iBioSim");
			}
			CompartmentGlyph compGlyph = null;
			if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
				compGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			} else {
				compGlyph = layout.createCompartmentGlyph();
				compGlyph.setId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
				compGlyph.setCompartmentId((String)cell.getId());
			}
			double x = compGlyph.getBoundingBox().x();
			double y = compGlyph.getBoundingBox().y();
			double width = compGlyph.getBoundingBox().width();
			double height = compGlyph.getBoundingBox().height();
			if (!bioModel.checkCompartmentLocation(cell.getId(),geom.getX(), geom.getY(), geom.getWidth(), geom.getHeight())) {
				geom.setX(x);
				geom.setY(y);
				geom.setWidth(width);
				geom.setHeight(height);
				Utility.createErrorMessage("Compartment Overlap", "Compartments must not overlap.");
				return;
			}
			compGlyph.getBoundingBox().setX(geom.getX());
			compGlyph.getBoundingBox().setY(geom.getY());
			compGlyph.getBoundingBox().setWidth(geom.getWidth());
			compGlyph.getBoundingBox().setHeight(geom.getHeight());
			if (bioModel.updateCompartmentsByLocation(true)) {
				bioModel.updateCompartmentsByLocation(false);
			} else {
				compGlyph.getBoundingBox().setX(x);
				compGlyph.getBoundingBox().setY(y);
				compGlyph.getBoundingBox().setWidth(width);
				compGlyph.getBoundingBox().setHeight(height);
				Utility.createErrorMessage("Missing Compartment", "All species and reactions must be within a compartment.");
			}
			geom.setX(compGlyph.getBoundingBox().x());
			geom.setY(compGlyph.getBoundingBox().y());
			geom.setWidth(compGlyph.getBoundingBox().width());
			geom.setHeight(compGlyph.getBoundingBox().height());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			} else {
				textGlyph = layout.createTextGlyph();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			textGlyph.setText((String)cell.getId());
			textGlyph.setBoundingBox(compGlyph.getBoundingBox());
		} else if (getCellType(cell).equals(GlobalConstants.COMPONENT)) {
			Layout layout = null;
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
			} else {
				layout = bioModel.getSBMLLayout().createLayout();
				layout.setId("iBioSim");
			}
			GeneralGlyph generalGlyph = null;
			if (layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
				generalGlyph = (GeneralGlyph)
						layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			} else {
				generalGlyph = layout.createGeneralGlyph();
				generalGlyph.setId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
				generalGlyph.setReferenceId((String)cell.getId());
			}
			double x = generalGlyph.getBoundingBox().x();
			double y = generalGlyph.getBoundingBox().y();
			double width = generalGlyph.getBoundingBox().width();
			double height = generalGlyph.getBoundingBox().height();
			generalGlyph.getBoundingBox().setX(geom.getX());
			generalGlyph.getBoundingBox().setY(geom.getY());
			generalGlyph.getBoundingBox().setWidth(geom.getWidth());
			generalGlyph.getBoundingBox().setHeight(geom.getHeight());
			geom.setX(generalGlyph.getBoundingBox().x());
			geom.setY(generalGlyph.getBoundingBox().y());
			geom.setWidth(generalGlyph.getBoundingBox().width());
			geom.setHeight(generalGlyph.getBoundingBox().height());
			TextGlyph textGlyph = null;
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId())!=null) {
				textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			} else {
				textGlyph = layout.createTextGlyph();
			}
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+(String)cell.getId());
			textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+(String)cell.getId());
			textGlyph.setText((String)cell.getId());
			textGlyph.setBoundingBox(generalGlyph.getBoundingBox());
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

		if (getCellType(cell).equals(GlobalConstants.SPECIES)||
				getCellType(cell).equals(GlobalConstants.PROMOTER)) {
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
				if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
					SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId());
					x = speciesGlyph.getBoundingBox().getPosition().getXOffset();
					y = speciesGlyph.getBoundingBox().getPosition().getYOffset();
					width = speciesGlyph.getBoundingBox().getDimensions().getWidth();
					height = speciesGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					unpositionedElementCount += 1;
					needsPositioning = true;
					x = (unpositionedElementCount%50) * 20;
					y = (unpositionedElementCount%10) * (height + 10);
					if (bioModel.getSBMLDocument().getModel().getSpecies((String)cell.getId())!=null) {
						String compartment = bioModel.getSBMLDocument().getModel().getSpecies((String)cell.getId()).getCompartment();
						if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
							CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+compartment);
							if (compartmentGlyph!=null) {
								x = x + compartmentGlyph.getBoundingBox().x();
								if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
									x = compartmentGlyph.getBoundingBox().x() + 10;
								}
							}
						}
					}
					bioModel.placeSpecies((String)cell.getId(), x, y, height, width);
				}
			} 
		} else if (getCellType(cell).equals(GlobalConstants.VARIABLE)||
				getCellType(cell).equals(GlobalConstants.PLACE)) {
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
				if (layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
					GeneralGlyph generalGlyph = (GeneralGlyph)
							layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId());
					x = generalGlyph.getBoundingBox().getPosition().getXOffset();
					y = generalGlyph.getBoundingBox().getPosition().getYOffset();
					width = generalGlyph.getBoundingBox().getDimensions().getWidth();
					height = generalGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					unpositionedElementCount += 1;
					needsPositioning = true;
					x = (unpositionedElementCount%50) * 20;
					y = (unpositionedElementCount%10) * (height + 10);
					if (bioModel.getSBMLDocument().getModel().getSpecies((String)cell.getId())!=null) {
						String compartment = bioModel.getSBMLDocument().getModel().getSpecies((String)cell.getId()).getCompartment();
						if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
							CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+compartment);
							if (compartmentGlyph!=null) {
								x = x + compartmentGlyph.getBoundingBox().x();
								if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
									x = compartmentGlyph.getBoundingBox().x() + 10;
								}
							}
						}
					}
					bioModel.placeSpecies((String)cell.getId(), x, y, height, width);
				}
			} 
		} else if (getCellType(cell).equals(GlobalConstants.REACTION)) {
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
				if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
					ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId());
					x = reactionGlyph.getBoundingBox().getPosition().getXOffset();
					y = reactionGlyph.getBoundingBox().getPosition().getYOffset();
					width = reactionGlyph.getBoundingBox().getDimensions().getWidth();
					height = reactionGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					unpositionedElementCount += 1;
					needsPositioning = true;
					x = (unpositionedElementCount%50) * 20;
					y = (unpositionedElementCount%10) * (height + 10);
					if (bioModel.getSBMLDocument().getModel().getReaction((String)cell.getId())!=null) {
						String compartment = bioModel.getSBMLDocument().getModel().getReaction((String)cell.getId()).getCompartment();
						if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
							CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+compartment);
							if (compartmentGlyph!=null) {
								x = x + compartmentGlyph.getBoundingBox().x();
								if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
									x = compartmentGlyph.getBoundingBox().x() + 10;
								}
							}
						}
					}
					bioModel.placeReaction((String)cell.getId(), x, y, height, width);
				} 
			} 
		} else if (getCellType(cell).equals(GlobalConstants.RULE) ||
				getCellType(cell).equals(GlobalConstants.CONSTRAINT) ||
				getCellType(cell).equals(GlobalConstants.EVENT) ||
				getCellType(cell).equals(GlobalConstants.TRANSITION)) {
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
				if (layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
					GeneralGlyph generalGlyph = (GeneralGlyph)
							layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId());
					x = generalGlyph.getBoundingBox().getPosition().getXOffset();
					y = generalGlyph.getBoundingBox().getPosition().getYOffset();
					width = generalGlyph.getBoundingBox().getDimensions().getWidth();
					height = generalGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					unpositionedElementCount += 1;
					needsPositioning = true;
					x = (unpositionedElementCount%50) * 20;
					y = (unpositionedElementCount%10) * (height + 10);
					if (bioModel.getSBMLDocument().getModel().getReaction((String)cell.getId())!=null) {
						String compartment = bioModel.getSBMLDocument().getModel().getReaction((String)cell.getId()).getCompartment();
						if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
							CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+compartment);
							if (compartmentGlyph!=null) {
								x = x + compartmentGlyph.getBoundingBox().x();
								if (!bioModel.getCompartmentByLocation((float)x, (float)y, (float)width, (float)height).equals(compartment)) {
									x = compartmentGlyph.getBoundingBox().x() + 10;
								}
							}
						}
					}
					bioModel.placeReaction((String)cell.getId(), x, y, height, width);
				} 
			} 
		} else if (!bioModel.isGridEnabled() && getCellType(cell).equals(GlobalConstants.COMPARTMENT)) {
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
				if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
					CompartmentGlyph compGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId());
					x = compGlyph.getBoundingBox().getPosition().getXOffset();
					y = compGlyph.getBoundingBox().getPosition().getYOffset();
					width = compGlyph.getBoundingBox().getDimensions().getWidth();
					height = compGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					unpositionedElementCount += 1;
					needsPositioning = true;
					x = (unpositionedElementCount%50) * 20;
					y = (unpositionedElementCount%10) * (height + 10);
					bioModel.placeCompartment((String)cell.getId(), x, y, height, width);
				} 
			} 
		} else if (getCellType(cell).equals(GlobalConstants.COMPONENT)) {
			if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
				Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
				if (layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
					GeneralGlyph generalGlyph = (GeneralGlyph)
							layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId());
					x = generalGlyph.getBoundingBox().getPosition().getXOffset();
					y = generalGlyph.getBoundingBox().getPosition().getYOffset();
					width = generalGlyph.getBoundingBox().getDimensions().getWidth();
					height = generalGlyph.getBoundingBox().getDimensions().getHeight();
				} else {
					unpositionedElementCount += 1;
					needsPositioning = true;
					x = (unpositionedElementCount%50) * 20;
					y = (unpositionedElementCount%10) * (height + 10);
					bioModel.placeCompartment((String)cell.getId(), x, y, height, width);
				} 
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
		
		Grid grid = bioModel.getGrid();
		double gridWidth = grid.getGridGeomWidth();
		double gridHeight = grid.getGridGeomHeight();
		
		componentsToMxCellMap.clear();
		
		//ADD COMPONENTS
		for (int row = 0; row < grid.getNumRows(); ++row) {
			for (int col = 0; col < grid.getNumCols(); ++col) {
				
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
					(bioModel.getSBMLDocument().getModel().getNumReactions() > 0) && cell.getValue() != null &&
					(bioModel.getSBMLDocument().getModel().getReaction((String)cell.getValue()) != null)) {
				return GlobalConstants.REACTION_EDGE;
			}
			else if (sourceType == GlobalConstants.REACTION || targetType == GlobalConstants.REACTION) {
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
		return bioModel.getModelFileName(compId).replace(".xml", ".gcm");
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
		BioModel compGCMFile = new BioModel(bioModel.getPath());
		boolean compart = false;
		//String modelFileName = gcm.getModelFileName(id).replace(".xml", ".gcm");
		String modelFileName = bioModel.getModelFileName(id);
		if (modelFileName.equals("")) {
			return false;
		}
		File compFile = new File(bioModel.getPath() + File.separator + modelFileName);
		
		if (compGCMFile != null && compFile.exists()) {
			compGCMFile.load(bioModel.getPath() + File.separator + modelFileName);
			compart = compGCMFile.IsWithinCompartment();
		} else {
			JOptionPane.showMessageDialog(Gui.frame, 
					"A model definition cannot be found for " + modelFileName + 
					".\nDropping component from the schematic.\n",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		String truncGCM = modelFileName.replace(".xml", "");
		String truncID = "";
		
		//if the id is too long, truncate it
		if (truncGCM.length() > 10)
			truncGCM = truncGCM.substring(0, 9) + "...";
		
		if (id.length() > 10)
			truncID = id.substring(0, 9) + "...";
		else truncID = id;
		
		String label = truncID + "\n" + truncGCM;
		CellValueObject cvo = new CellValueObject(label, "Component", null);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.componentsToMxCellMap.put(id, (mxCell)insertedVertex);
		
		//pass whether or not the component is a compartment, as the styles are different
		this.setComponentStyles(id, compart);
		
		needsPositioning = sizeAndPositionFromProperties((mxCell)insertedVertex,
				GlobalConstants.DEFAULT_COMPONENT_WIDTH,GlobalConstants.DEFAULT_COMPONENT_HEIGHT);

		// now draw the edges that connect the component
		for (String propName : bioModel.getInputs(id).keySet()) {
			// input, the arrow should point in from the species
			String topSpecies = bioModel.getInputs(id).get(propName);
			Object createdEdge = this.insertEdge(this.getDefaultParent(), "", "", 
					this.getSpeciesCell(topSpecies),insertedVertex);
			String key = id + " Input " + topSpecies;
			componentsConnectionsToMxCellMap.put(key, (mxCell)createdEdge);
			this.updateComponentConnectionVisuals((mxCell)createdEdge, propName);
		}

		// now draw the edges that connect the component
		for (String propName : bioModel.getOutputs(id).keySet()) {
			// output, the arrow should point out to the species
			String topSpecies = bioModel.getOutputs(id).get(propName);
			Object createdEdge = this.insertEdge(this.getDefaultParent(), "", "", insertedVertex, 
					this.getSpeciesCell(topSpecies));
			String key = id + " Output " + topSpecies;
			componentsConnectionsToMxCellMap.put(key, (mxCell)createdEdge);
			this.updateComponentConnectionVisuals((mxCell)createdEdge, propName);
		}

		// now draw the edges that connect variables to the component
		HashMap<String,String> variables = bioModel.getVariableInputs(id);
		for (String propName : variables.keySet()) {
			String topSpecies = variables.get(propName);
			Object createdEdge = this.insertEdge(this.getDefaultParent(), "", "",
					this.getVariableCell(topSpecies), insertedVertex);
			String key = id + " Variable " + topSpecies;
			componentsConnectionsToMxCellMap.put(key, (mxCell)createdEdge);
			this.updateComponentConnectionVisuals((mxCell)createdEdge, propName);
		}

		// now draw the edges that connect variables to the component
		variables = bioModel.getVariableOutputs(id);
		for (String propName : variables.keySet()) {
			String topSpecies = variables.get(propName);
			Object createdEdge = this.insertEdge(this.getDefaultParent(), "", "", insertedVertex,
					this.getVariableCell(topSpecies));
			String key = id + " Variable " + topSpecies;
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
		
		String type = BioModel.getSpeciesType(bioModel.getSBMLDocument(),sp);
		if (bioModel.getDiffusionReaction(sp)!=null) type += " (D)";
		if (bioModel.isSpeciesConstitutive(sp)) type += " (C)";
		if (type.equals(GlobalConstants.MRNA)) return false; 
		
		String truncID = "";
		
		if (sp.length() > 12)
			truncID = sp.substring(0, 11) + "...";
		else truncID = sp;
		
		String label = truncID + '\n' + type;

		CellValueObject cvo = new CellValueObject(label, "Species", null);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), sp, cvo, 1, 1, 1, 1);
		this.speciesToMxCellMap.put(sp, (mxCell)insertedVertex);
		
		this.setSpeciesStyles(sp);
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex,
				GlobalConstants.DEFAULT_SPECIES_WIDTH,GlobalConstants.DEFAULT_SPECIES_HEIGHT);
	}
	
	/**
	 * creates a vertex on the graph using the internal model.
	 * @param id
	 * 
	 * @return: A bool, true if the reaction had to be positioned.
	 */
	private boolean createGraphReactionFromModel(String id){
		CellValueObject cvo = new CellValueObject(id, "Reaction", null);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.reactionsToMxCellMap.put(id, (mxCell)insertedVertex);
		
		this.setReactionStyles(id);
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex,
				GlobalConstants.DEFAULT_REACTION_WIDTH,GlobalConstants.DEFAULT_REACTION_HEIGHT);
	}
	
	private boolean createGraphCompartmentFromModel(String id){
		CellValueObject cvo = new CellValueObject(id, "Compartment", null);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.compartmentsToMxCellMap.put(id, (mxCell)insertedVertex);

		if (bioModel.IsWithinCompartment()) {
			this.setCompartmentStyles(id,true);
		} else {
			this.setCompartmentStyles(id,false);
		}
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex,
				GlobalConstants.DEFAULT_COMPARTMENT_WIDTH,GlobalConstants.DEFAULT_COMPARTMENT_HEIGHT);
	}
	
	private boolean createGraphRuleFromModel(String id){
		CellValueObject cvo = new CellValueObject(id, "Rule", null);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.rulesToMxCellMap.put(id, (mxCell)insertedVertex);
		
		this.setRuleStyles(id);
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex,
				GlobalConstants.DEFAULT_RULE_WIDTH,GlobalConstants.DEFAULT_RULE_HEIGHT);
	}
	
	private boolean createGraphConstraintFromModel(String id){
		CellValueObject cvo = new CellValueObject(id, "Constraint", null);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.constraintsToMxCellMap.put(id, (mxCell)insertedVertex);
		
		this.setConstraintStyles(id);
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex,
				GlobalConstants.DEFAULT_CONSTRAINT_WIDTH,GlobalConstants.DEFAULT_CONSTRAINT_HEIGHT);
	}
	
	private boolean createGraphEventFromModel(String id){
		CellValueObject cvo = new CellValueObject(id, "Event", null);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.eventsToMxCellMap.put(id, (mxCell)insertedVertex);
		
		this.setEventStyles(id);
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex,
				GlobalConstants.DEFAULT_EVENT_WIDTH,GlobalConstants.DEFAULT_EVENT_HEIGHT);
	}
	
	private boolean createGraphTransitionFromModel(String id){
		CellValueObject cvo = new CellValueObject(id, "Transition", null);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.eventsToMxCellMap.put(id, (mxCell)insertedVertex);
		
		this.setTransitionStyles(id);
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex,
				GlobalConstants.DEFAULT_TRANSITION_WIDTH,GlobalConstants.DEFAULT_TRANSITION_HEIGHT);
	}
	
	/**
	 * Creates a drawn promoter using the internal model
	 * @param pname
	 * @return
	 */
	private boolean createGraphDrawnPromoterFromModel(String id){
		
		String truncID;
		
		if (id.length() > 8)
			truncID = id.substring(0, 7) + "...";
		else truncID = id;

		CellValueObject cvo = new CellValueObject(truncID, "Promoter", null);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.drawnPromoterToMxCellMap.put(id, (mxCell)insertedVertex);
		
		this.setDrawnPromoterStyles(id);
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex,
				GlobalConstants.DEFAULT_SPECIES_WIDTH,GlobalConstants.DEFAULT_SPECIES_HEIGHT);
	}
	
	/**
	 * Creates a variable using the internal model
	 * @param pname
	 * @return
	 */
	private boolean createGraphVariableFromModel(String id){
		
		String truncID;
		
		if (id.length() > 8)
			truncID = id.substring(0, 7) + "...";
		else truncID = id;

		CellValueObject cvo = new CellValueObject(truncID, "Variable", null);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.variableToMxCellMap.put(id, (mxCell)insertedVertex);
		
		this.setVariableStyles(id);
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex,
				GlobalConstants.DEFAULT_VARIABLE_WIDTH,GlobalConstants.DEFAULT_VARIABLE_HEIGHT);
	}
	
	/**
	 * Creates a variable using the internal model
	 * @param pname
	 * @return
	 */
	private boolean createGraphPlaceFromModel(String id,boolean marked){
		
		String truncID;
		
		if (id.length() > 8)
			truncID = id.substring(0, 7) + "...";
		else truncID = id;

		CellValueObject cvo = new CellValueObject(truncID, "Place", null);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.variableToMxCellMap.put(id, (mxCell)insertedVertex);
		
		if (marked) {
			this.setMarkedPlaceStyles(id);
		} else {
			this.setPlaceStyles(id);
		}
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex,
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
		
		mxStylesheet stylesheet = this.getStylesheet();
		
		//compartment
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 30);
		style.put(mxConstants.STYLE_FILLCOLOR, "#FFFFFF");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_ROUNDED, true);
		stylesheet.putCellStyle("SBMLCOMPARTMENT", style);
	
		//compartment
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 30);
		style.put(mxConstants.STYLE_FILLCOLOR, "#FFFFFF");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_ROUNDED, false);
		stylesheet.putCellStyle("SBMLCOMPONENT", style);		

		//species
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FILLCOLOR, "#5CB4F2");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_ROUNDED, true);
		stylesheet.putCellStyle("SPECIES", style);
		
		//reactions
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FILLCOLOR, "#C7007B");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		stylesheet.putCellStyle("REACTION", style);
		
		//rules
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_SWIMLANE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_ROUNDED, false);
		style.put(mxConstants.STYLE_FILLCOLOR, "#FFFF00");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("RULE", style);
		
		//constraints
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_HEXAGON);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_ROUNDED, false);
		style.put(mxConstants.STYLE_FILLCOLOR, "#FF0000");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("CONSTRAINT", style);
		
		//events
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_ROUNDED, false);
		style.put(mxConstants.STYLE_FILLCOLOR, "#00FF00");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("EVENT", style);
		
		//events
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_ROUNDED, false);
		style.put(mxConstants.STYLE_FILLCOLOR, "#FFFFFF");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("TRANSITION", style);
		
		//components
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_ROUNDED, false);
		style.put(mxConstants.STYLE_FILLCOLOR, "#87F274");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("COMPONENT", style);
		
		//grid components
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_ROUNDED, false);
		style.put(mxConstants.STYLE_FILLCOLOR, "#87F274");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("GRIDCOMPONENT", style);
		
		//compartments
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_ROUNDED, true);
		style.put(mxConstants.STYLE_FILLCOLOR, "#87F274");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("COMPARTMENT", style);
		
		//grid compartments
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_ROUNDED, true);
		style.put(mxConstants.STYLE_FILLCOLOR, "#87F274");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("GRIDCOMPARTMENT", style);
		
		//grid rectangle
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_ROUNDED, false);
		style.put(mxConstants.STYLE_FILLCOLOR, "none");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style.put(mxConstants.STYLE_MOVABLE, false);
		style.put(mxConstants.STYLE_RESIZABLE, false);
		style.put(mxConstants.STYLE_NOLABEL, true);
		stylesheet.putCellStyle("GRID_RECTANGLE", style);
		
		//component edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#FFAA00");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
		stylesheet.putCellStyle("COMPONENT_EDGE", style);	

		//production edge (promoter to species)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#34BA04");
		style.put(mxConstants.STYLE_STROKECOLOR, "#34BA04");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
		style.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ENTITY_RELATION);
		stylesheet.putCellStyle("PRODUCTION", style);
		
		//activation edge (species to species)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#34BA04");
		style.put(mxConstants.STYLE_STROKECOLOR, "#34BA04");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_BLOCK);
		stylesheet.putCellStyle("ACTIVATION", style);
		
		//repression edge (species to species)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#FA2A2A");
		style.put(mxConstants.STYLE_STROKECOLOR, "#FA2A2A");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OVAL);
		stylesheet.putCellStyle("REPRESSION", style);
		
		//no influence (species to species)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#000000");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_DIAMOND);
		style.put(mxConstants.STYLE_DASHED, "true");
		stylesheet.putCellStyle("NOINFLUENCE", style);
		
		//complex formation edge (species to species)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#4E5D9C");
		style.put(mxConstants.STYLE_STROKECOLOR, "#4E5D9C");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
		style.put(mxConstants.STYLE_DASHED, "true");
		stylesheet.putCellStyle("COMPLEX", style);
		
		//reaction edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#F2861B");
		style.put(mxConstants.STYLE_STROKECOLOR, "#F2861B");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("REACTION_EDGE", style);
		
		//rule edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#F2861B");
		style.put(mxConstants.STYLE_STROKECOLOR, "#F2861B");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("RULE_EDGE", style);
		
		//constraint edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#F2861B");
		style.put(mxConstants.STYLE_STROKECOLOR, "#F2861B");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("CONSTRAINT_EDGE", style);
		
		//event edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#F2861B");
		style.put(mxConstants.STYLE_STROKECOLOR, "#F2861B");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("EVENT_EDGE", style);

		//default edge
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#000000");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		style.put(mxConstants.STYLE_DASHED, "false");
		stylesheet.putCellStyle("DEFAULT", style);
		
		//explicit promoter
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#F00E0E");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("EXPLICIT_PROMOTER", style);
		
		//variable
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FILLCOLOR, "#0000FF");
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("VARIABLE", style);

		//place
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FILLCOLOR, "#FFFFFF");
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("PLACE", style);

		//marked place
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FILLCOLOR, "#808080");
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		stylesheet.putCellStyle("MARKED_PLACE", style);
	}
	
	
	//STYLE SETTING
	
	/**
	 * 
	 * @param id
	 */
	private void setSpeciesStyles(String id){
		String style="SPECIES;";
		
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
		
		mxCell cell = this.getRulesCell(id);
		cell.setStyle(style);
	}

	private void setConstraintStyles(String id){
		String style="CONSTRAINT;";
		
		mxCell cell = this.getConstraintsCell(id);
		cell.setStyle(style);
	}

	private void setEventStyles(String id){
		String style="EVENT;";
		
		mxCell cell = this.getEventsCell(id);
		cell.setStyle(style);
	}

	private void setTransitionStyles(String id){
		String style="TRANSITION;";
		
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
		
		if (bioModel.getGrid().isEnabled()) {
			
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
		String style="EXPLICIT_PROMOTER";
		
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
		
		if (newStyle != null)
			cell.setStyle(newStyle);
		
		// size
		if (appearance.size != null) {

			double x = 0;
			double y = 0;
			double width = 0;
			double height = 0;

			if (getCellType(cell).equals(GlobalConstants.SPECIES)||
					getCellType(cell).equals(GlobalConstants.PROMOTER)) {
				if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
					Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
					if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
						SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId());
						x = speciesGlyph.getBoundingBox().getPosition().getXOffset();
						y = speciesGlyph.getBoundingBox().getPosition().getYOffset();
						width = speciesGlyph.getBoundingBox().getDimensions().getWidth();
						height = speciesGlyph.getBoundingBox().getDimensions().getHeight();
					} else {
						x = -9999;
						y = -9999;
						width = GlobalConstants.DEFAULT_SPECIES_WIDTH;
						height = GlobalConstants.DEFAULT_SPECIES_HEIGHT;
					}
				} else {
					x = -9999;
					y = -9999;
					width = GlobalConstants.DEFAULT_SPECIES_WIDTH;
					height = GlobalConstants.DEFAULT_SPECIES_HEIGHT;
				}
			} else if (getCellType(cell).equals(GlobalConstants.VARIABLE) || 
					getCellType(cell).equals(GlobalConstants.PLACE)) {
				if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
					Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
					if (layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
						GeneralGlyph generalGlyph = (GeneralGlyph)
								layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId());
						x = generalGlyph.getBoundingBox().getPosition().getXOffset();
						y = generalGlyph.getBoundingBox().getPosition().getYOffset();
						width = generalGlyph.getBoundingBox().getDimensions().getWidth();
						height = generalGlyph.getBoundingBox().getDimensions().getHeight();
					} else {
						x = -9999;
						y = -9999;
						width = GlobalConstants.DEFAULT_SPECIES_WIDTH;
						height = GlobalConstants.DEFAULT_SPECIES_HEIGHT;
					}
				} else {
					x = -9999;
					y = -9999;
					width = GlobalConstants.DEFAULT_SPECIES_WIDTH;
					height = GlobalConstants.DEFAULT_SPECIES_HEIGHT;
				}
			} else if (getCellType(cell).equals(GlobalConstants.REACTION)) {
				if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
					Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
					if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
						ReactionGlyph reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId());
						x = reactionGlyph.getBoundingBox().getPosition().getXOffset();
						y = reactionGlyph.getBoundingBox().getPosition().getYOffset();
						width = reactionGlyph.getBoundingBox().getDimensions().getWidth();
						height = reactionGlyph.getBoundingBox().getDimensions().getHeight();
					} else {
						x = -9999;
						y = -9999;
						width = GlobalConstants.DEFAULT_REACTION_WIDTH;
						height = GlobalConstants.DEFAULT_REACTION_HEIGHT;
					} 
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
				if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
					Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
					if (layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
						GeneralGlyph generalGlyph = (GeneralGlyph)
								layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId());
						x = generalGlyph.getBoundingBox().getPosition().getXOffset();
						y = generalGlyph.getBoundingBox().getPosition().getYOffset();
						width = generalGlyph.getBoundingBox().getDimensions().getWidth();
						height = generalGlyph.getBoundingBox().getDimensions().getHeight();
					} else {
						x = -9999;
						y = -9999;
						width = GlobalConstants.DEFAULT_REACTION_WIDTH;
						height = GlobalConstants.DEFAULT_REACTION_HEIGHT;
					} 
				} else {
					x = -9999;
					y = -9999;
					width = GlobalConstants.DEFAULT_REACTION_WIDTH;
					height = GlobalConstants.DEFAULT_REACTION_HEIGHT;
				}
			} else if (getCellType(cell).equals(GlobalConstants.COMPARTMENT)) {
				if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
					Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
					if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
						CompartmentGlyph compGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+(String)cell.getId());
						x = compGlyph.getBoundingBox().getPosition().getXOffset();
						y = compGlyph.getBoundingBox().getPosition().getYOffset();
						width = compGlyph.getBoundingBox().getDimensions().getWidth();
						height = compGlyph.getBoundingBox().getDimensions().getHeight();
					} else {
						x = -9999;
						y = -9999;
						width = GlobalConstants.DEFAULT_COMPONENT_WIDTH;
						height = GlobalConstants.DEFAULT_COMPONENT_HEIGHT;
					} 
				} else {
					x = -9999;
					y = -9999;
					width = GlobalConstants.DEFAULT_COMPONENT_WIDTH;
					height = GlobalConstants.DEFAULT_COMPONENT_HEIGHT;
				}
			} else if (getCellType(cell).equals(GlobalConstants.COMPONENT)) {
				if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
					Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
					if (layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId())!=null) {
						GeneralGlyph generalGlyph = (GeneralGlyph)
								layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+(String)cell.getId());
						x = generalGlyph.getBoundingBox().getPosition().getXOffset();
						y = generalGlyph.getBoundingBox().getPosition().getYOffset();
						width = generalGlyph.getBoundingBox().getDimensions().getWidth();
						height = generalGlyph.getBoundingBox().getDimensions().getHeight();
					} else {
						x = -9999;
						y = -9999;
						width = GlobalConstants.DEFAULT_COMPONENT_WIDTH;
						height = GlobalConstants.DEFAULT_COMPONENT_HEIGHT;
					} 
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