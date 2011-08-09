/**
 * 
 */
package gcm.gui.schematic;

import gcm.gui.modelview.movie.MovieAppearance;
import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SpeciesReference;

import main.Gui;

//import javax.xml.bind.JAXBElement.GlobalScope;


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
	private HashMap<String, mxCell> influencesToMxCellMap;
	private HashMap<String, mxCell> componentsToMxCellMap;
	private HashMap<String, mxCell> componentsConnectionsToMxCellMap;
	private HashMap<String, mxCell> drawnPromoterToMxCellMap;
	private HashMap<String, mxCell> gridRectangleToMxCellMap;
	
	mxCell cell = new mxCell();
	
	private GCMFile gcm;
	
	public final String CELL_NOT_FULLY_CONNECTED = "cell not fully connected";
	private final String CELL_VALUE_NOT_FOUND = "cell value not found";
	
	// only bother the user about bad promoters once. 
	//This should be improved to happen once per GCM file if this will be a common error.
	private int _badPromoters = 0;
	public boolean isBuilding = false;
	
	// Keep track of how many elements did not have positioning info.
	// This allows us to stack them in the topleft corner until they
	// are positioned by the user or a layout algorithm.
	int unpositionedSpeciesComponentCount = 0;
	
	
	/**
	 * constructor
	 * @param gcm
	 */
	public BioGraph(GCMFile gcm) {
		
		super();
		
		// Turn editing off to prevent mxGraph from letting the user change the 
		// label on the cell. We want to do this using the property windows.
		this.setCellsEditable(false);
		
		this.gcm = gcm;
		
		this.initializeMaps();
	
		createStyleSheets();		
	}	
	
	/**
	 * sets the hash maps to null
	 */
	private void initializeMaps(){
		
		speciesToMxCellMap = new HashMap<String, mxCell>();
		reactionsToMxCellMap = new HashMap<String, mxCell>();
		componentsToMxCellMap = new HashMap<String, mxCell>();
		influencesToMxCellMap = new HashMap<String, mxCell>();
		componentsConnectionsToMxCellMap = new HashMap<String, mxCell>();
		drawnPromoterToMxCellMap = new HashMap<String, mxCell>();
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
	
	/**
	 * Builds the graph based on the internal representation
	 * @return
	 */	
	public boolean buildGraph(){
		
		this.isBuilding = true;

		// remove all the cells from the graph (vertices and edges)
		this.removeCells(this.getChildCells(this.getDefaultParent(), true, true));

		initializeMaps();
		
		assert(this.gcm != null);
		
		// Start an undo transaction
		this.getModel().beginUpdate();
		
		boolean needsPositioning = false;
		unpositionedSpeciesComponentCount = 0;
		
		//createGraphCompartmentFromModel("default");
		
		//put the grid cells in first so that they're below the other cells
		addGridCells();
		
		// add species
		for(String sp : gcm.getSpecies().keySet()){
			
			if(createGraphSpeciesFromModel(sp))
				needsPositioning = true;
		}

		Model m = gcm.getSBMLDocument().getModel();
		
		for (int i = 0; i < m.getNumReactions(); i++) {
			
			Reaction r = m.getReaction(i);
			
			if (gcm.getReactions().get(r.getId()) == null) {
				
				if (r.getNumModifiers() > 0 || (r.getNumReactants()>1 && r.getNumProducts()>1) ||
					r.getNumReactants()==0 || r.getNumProducts()==0) {
					
					Properties prop = new Properties();
					prop.setProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_REACTION_WIDTH));
					prop.setProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_REACTION_HEIGHT));
					gcm.centerVertexOverPoint(prop, 50, 50);
					gcm.getReactions().put(r.getId(), prop);
				}
			}
		}
		
		// add reactions
		for(String sp : gcm.getReactions().keySet()){ 
			
			if(createGraphReactionFromModel(sp))
				needsPositioning = true;
		}

		// add all components
		for(String comp : gcm.getComponents().keySet()){
			
			if(createGraphComponentFromModel(comp))
				needsPositioning = true;
		}
		
		// add all the drawn promoters
		for(String prom : gcm.getPromoters().keySet()){
			
			Properties pprop = gcm.getPromoters().get(prom);
			
			if(pprop.get(GlobalConstants.EXPLICIT_PROMOTER) != null && 
					pprop.get(GlobalConstants.EXPLICIT_PROMOTER).equals(GlobalConstants.TRUE)){
				//System.out.printf("Please draw this promoter!");
				
				if(createGraphDrawnPromoterFromModel(prom))
					needsPositioning = true;
			}
		}
		
		boolean needsRedrawn = false;
		
		// add all the edges. 
		for(String inf : gcm.getInfluences().keySet()){
			
			String ins = GCMFile.getInput(inf);
			String outs = GCMFile.getOutput(inf);
		
			if(ins.equals(GlobalConstants.NONE) || outs.equals(GlobalConstants.NONE)){
				
				// a drawn-promoter edge
				Properties influence = gcm.getInfluences().get(inf);
				String promName = influence.getProperty(GlobalConstants.PROMOTER, "");
				
				if(promName.equals("")){
					
					if(_badPromoters++ == 0)
						JOptionPane.showMessageDialog(Gui.frame, "You have a default promoter that is connected to None. Because of this some edges are not being drawn in schematic. Please fix.\n");
					
					continue;
				}
				
				Properties promProp = gcm.getPromoters().get(promName);
				
				// If an edge is found with a drawn promoter, 
				// this is a recoverable error case. Flag the promoter as drawn, re-call this 
				// function and bail. This will ensure that legacy gcm files and files
				// created with the other interface get built correctly.
				// make sure the promoter is set to be drawn. If not, set it and try this function again.
				if(!promProp.getProperty(GlobalConstants.EXPLICIT_PROMOTER, "").equals(GlobalConstants.TRUE)){
					
					promProp.setProperty(GlobalConstants.EXPLICIT_PROMOTER, GlobalConstants.TRUE);
					needsRedrawn = true;
					// we can't draw this edge because the promoter isn't drawn. 
					// It will be drawn in the next pass.
					continue;
				}
				
				// now draw the edge to the promoter
				if(ins.equals(GlobalConstants.NONE)){
					// the output must be the species
					mxCell production = (mxCell)this.insertEdge(this.getDefaultParent(), inf, "", 
							this.getDrawnPromoterCell(promName), 
							this.getSpeciesCell(outs));
					//updateProductionVisuals(inf);
					production.setStyle("PRODUCTION");
				}
				else{
					// the input must be the species
					this.insertEdge(this.getDefaultParent(), inf, "", 
							this.getSpeciesCell(ins), 
							this.getDrawnPromoterCell(promName));
					
					updateInfluenceVisuals(inf);
				}
			}
			else{
				// A normal, non-drawn promoter edge
				this.insertEdge(this.getDefaultParent(), inf, "", 
						this.getSpeciesCell(ins), 
						this.getSpeciesCell(outs)
						);
				
				updateInfluenceVisuals(inf);
			}
		}
		
		//add reactions
		for (int i = 0; i < m.getNumReactions(); i++) {
			
			Reaction r = m.getReaction(i);
			
			if (gcm.getReactions().get(r.getId()) != null) {
				
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
		for(String inf:gcm.getInfluences().keySet()){
			
			String endA = GCMFile.getInput(inf);
			String endB = GCMFile.getOutput(inf);
			
			// ignore anything connected directly to a drawn promoter
			if(endA.equals(GlobalConstants.NONE) || endB.equals(GlobalConstants.NONE))
				continue;
			
			if(endA.compareTo(endB) > 0){
				// swap the strings
				String t = endA;
				endA = endB;
				endB = t;
			}
			
			String key = endA + " " + endB;
			mxCell cell = this.getInfluence(inf);
			
			if(edgeHash.containsKey(key) == false)
				edgeHash.put(key, new Vector<mxCell>());
			
			edgeHash.get(key).add(cell);
		}
		
		Model m = gcm.getSBMLDocument().getModel();
		
		for (int i = 0; i < m.getNumReactions(); i++) {
			
			Reaction r = m.getReaction(i);
			
			if (gcm.getReactions().get(r.getId()) != null) {
				
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
		Pattern getPropNamePattern = Pattern.compile("^type_([\\w\\_\\d]+)");
		
		for(String compName:gcm.getComponents().keySet()){

			Properties comp = gcm.getComponents().get(compName);
			
			for(Object propNameO:comp.keySet()){
				
				String propName = propNameO.toString();
				
				if(propName.startsWith("type_")){
					
					Matcher matcher = getPropNamePattern.matcher(propName);
					matcher.find();
					String compInternalName = matcher.group(1);
					String targetName = comp.get(compInternalName).toString();
					String type = comp.get(propName).toString(); // Input or Output
					String key = compName + " "+type+" " + targetName;
					mxCell cell = componentsConnectionsToMxCellMap.get(key);
					String simpleKey = compName + " " + targetName;
					
					if(edgeHash.containsKey(simpleKey) == false)
						edgeHash.put(simpleKey, new Vector<mxCell>());
					
					edgeHash.get(simpleKey).add(cell);
				}
			}
		}
		
		// loop through every set of edge endpoints and then move them if needed.
		for(Vector<mxCell> vec:edgeHash.values()){
			
			if(vec.size() > 1){
				
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
		
		if (gcm.getGrid().isEnabled()) {
			
			int gridRows = gcm.getGrid().getNumRows();
			int gridCols = gcm.getGrid().getNumCols();
			double gridWidth = gcm.getGrid().getGridGeomWidth();
			double gridHeight = gcm.getGrid().getGridGeomHeight();
			
			//creates an mxCell/vertex for each grid rectangle
			//these are later accessible via ID via the hash map
			for (int row = 0; row < gridRows; ++row) {
				for (int col = 0; col < gridCols; ++col) {
					
					String id = "ROW" + row + "_COL" + col;
					
					Properties prop = new Properties();
					prop.setProperty("Type", GlobalConstants.GRID_RECTANGLE);
					
					double currX = 15 + col*gridWidth;
					double currY = 15 + row*gridHeight;
					
					CellValueObject cvo = new CellValueObject(id, prop);
					
					Object vertex = this.insertVertex(this.getDefaultParent(), id, cvo, currX, currY, gridWidth, gridHeight);
					
					mxCell cell = (mxCell)vertex;
					cell.setConnectable(false);
					cell.setStyle("GRID_RECTANGLE");
					
					gridRectangleToMxCellMap.put(id, cell);
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
	public void updateAllSpeciesPosition(){
		
		for(mxCell cell:this.speciesToMxCellMap.values()){
			updateInternalPosition(cell);
		}
		
		for(mxCell cell:this.reactionsToMxCellMap.values()){
			updateInternalPosition(cell);
		}
		
		for(mxCell cell:this.componentsToMxCellMap.values()){
			updateInternalPosition(cell);
		}
		
		for(mxCell cell:this.drawnPromoterToMxCellMap.values()){
			updateInternalPosition(cell);
		}
	}
	
	/**
	 * Given a cell that must be a species or component,
	 * update the internal model to reflect it's coordinates.
	 * Called when a cell is dragged with the GUI.
	 */
	public void updateInternalPosition(mxCell cell){
		
		Properties prop = ((CellValueObject)cell.getValue()).prop;
		mxGeometry geom = cell.getGeometry();
		prop.setProperty("graphx", String.valueOf(geom.getX()));
		prop.setProperty("graphy", String.valueOf(geom.getY()));
		prop.setProperty("graphwidth", String.valueOf(geom.getWidth()));
		prop.setProperty("graphheight", String.valueOf(geom.getHeight()));
	}
	
	/**
	 * Given a species, component, or drawn promoter cell, position it 
	 * using the properties.
	 */
	private boolean sizeAndPositionFromProperties(mxCell cell, Properties prop){
		
		double x = Double.parseDouble(prop.getProperty("graphx", "-9999"));
		double y = Double.parseDouble(prop.getProperty("graphy", "-9999"));;
		double width = Double.parseDouble(prop.getProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_SPECIES_WIDTH)));;
		double height = Double.parseDouble(prop.getProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_SPECIES_HEIGHT)));

		/*
		String id = !(prop.getProperty(GlobalConstants.NAME, "").equals("")) ? 
						prop.getProperty(GlobalConstants.NAME) : 
							!prop.getProperty("ID", "").equals("") ? prop.getProperty("ID", ""):
								prop.getProperty("label");
								*/
		boolean needsPositioning = false;	
		
		if(x < -9998 || y < -9998){
			
			unpositionedSpeciesComponentCount += 1;
			needsPositioning = true;
			
			// Line the unpositioned species up nicely. The mod is there as a rough
			// and dirty way to prevent
			// them going off the bottom or right hand side of the screen.
			x = (unpositionedSpeciesComponentCount%50) * 20;
			y = (unpositionedSpeciesComponentCount%10) * (GlobalConstants.DEFAULT_SPECIES_HEIGHT + 10);
		}
		
		cell.setGeometry(new mxGeometry(x, y, width, height));
		
		return needsPositioning;
	}
	
	
	//GET METHODS
	
	/**
	 * returns GlobalConstants.SPECIES, GlobalConstants.COMPONENT, GlobalConstants.INFLUENCE, or GlobalConstants.COMPONENT_CONNECTION.
	 * @param cell
	 */
	public String getCellType(mxCell cell){
		
		if(cell.isEdge()){
			
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
					(gcm.getSBMLDocument().getModel().getNumReactions() > 0) && cell.getValue() != null &&
					(gcm.getSBMLDocument().getModel().getReaction((String)cell.getValue()) != null)) {
				return GlobalConstants.REACTION_EDGE;
			}
			else if (sourceType == GlobalConstants.REACTION || targetType == GlobalConstants.REACTION) {
				return GlobalConstants.REACTION_EDGE;
			}
			else {
				return GlobalConstants.INFLUENCE;
			}
		}
		//cell is a vertex
		else{
			Properties prop = ((CellValueObject)(cell.getValue())).prop;
			
			if(gcm.getSpecies().containsValue(prop))
				return GlobalConstants.SPECIES;
			else if(gcm.getComponents().containsValue(prop))
				return GlobalConstants.COMPONENT;
			else if(gcm.getPromoters().containsValue(prop))
				return GlobalConstants.PROMOTER;
			else if(gcm.getReactions().containsValue(prop)) 
				return GlobalConstants.REACTION;
			else if (prop.getProperty("Type").equals(GlobalConstants.GRID_RECTANGLE))
				return GlobalConstants.GRID_RECTANGLE;
			else
				return CELL_VALUE_NOT_FOUND;
		}
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
	
	/**
	 * Given a cell, return the list that it belongs to.
	 */
	public HashMap<String, Properties> getPropertiesList(mxCell cell){
		
		String type = this.getCellType(cell);
		
		if(type == GlobalConstants.SPECIES)
			return gcm.getSpecies();
		else if(type == GlobalConstants.COMPONENT)
			return gcm.getComponents();
		else if(type == GlobalConstants.INFLUENCE)
			return gcm.getInfluences();
		else if(type == GlobalConstants.COMPONENT_CONNECTION)
			return null; // Component connection don't have properties
		else if(type == GlobalConstants.PRODUCTION)
			return null; // Production connection don't have properties
		else if(type == GlobalConstants.PROMOTER)
			return gcm.getPromoters();
		else if(type == GlobalConstants.REACTION) 
			return gcm.getReactions();
		else if(type == GlobalConstants.REACTION_EDGE) 
			return null;
		else if(type == GlobalConstants.GRID_RECTANGLE)
			return null;
		else
			throw new Error("Invalid type: " + type);
	}

	/**
	 * A convenience function
	 * @param cell
	 * @return
	 */
	public Properties getCellProperties(mxCell cell){
		if(cell.getId() == null || getPropertiesList(cell) == null)
			return null;
		
		return getPropertiesList(cell).get(cell.getId());
	}
	
	/**
	 * 
	 * @param cell
	 * @return
	 */
	public Properties getCellProperties(mxICell cell){
		return getCellProperties((mxCell)cell);
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
	public mxCell getReactionsCell(String id){
		return reactionsToMxCellMap.get(id);
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
		//{invb={ID=invb, gcm=inv.gcm}
		//{invb={ID=invb, gcm=inv.gcm}, i1={b=S3, ID=i1, a=S1, gcm=inv.gcm, type_b=Output, type_a=Input}}

		boolean needsPositioning = false;
		
		Properties prop = gcm.getComponents().get(id);

		double x = Double.parseDouble(prop.getProperty("graphx", "-9999"));
		double y = Double.parseDouble(prop.getProperty("graphy", "-9999"));;
		double width = Double.parseDouble(prop.getProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_WIDTH)));;
		double height = Double.parseDouble(prop.getProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_HEIGHT)));
				
		//set the correct compartment status
		GCMFile compGCMFile = new GCMFile(gcm.getPath());
		boolean compart = false;
		
		if (compGCMFile != null) {
			compGCMFile.load(gcm.getPath() + File.separator + prop.getProperty("gcm"));
			compart = compGCMFile.getIsWithinCompartment();
		}
		
		prop.setProperty("compartment", Boolean.toString(compart));
		
		
		if(x < -9998 || y < -9998){
			
			unpositionedSpeciesComponentCount += 1;
			needsPositioning = true;
			
			// Line the unpositioned species up nicely. The mod is there as a rough
			// and dirty way to prevent
			// them going off the bottom or right hand side of the screen.
			x = (unpositionedSpeciesComponentCount%50) * 20;
			y = (unpositionedSpeciesComponentCount%10) * (GlobalConstants.DEFAULT_SPECIES_HEIGHT + 10);
		}
		
		String label = id + "\n" + prop.getProperty("gcm").replace(".gcm", "");
		CellValueObject cvo = new CellValueObject(label, prop);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, x, y, width, height);
		this.componentsToMxCellMap.put(id, (mxCell)insertedVertex);
		
		//pass whether or not the component is a compartment, as the styles are different
		this.setComponentStyles(id, compart);

		// now draw the edges that connect the component
		for (Object propName : prop.keySet()) {
			
			if (!propName.toString().equals("gcm")
					&& !propName.toString().equals(GlobalConstants.ID)
					&& prop.keySet().contains("type_" + propName)) {
				
				Object createdEdge;
				
				if (prop.getProperty("type_" + propName).equals("Output")) {
					
					// output, the arrow should point out to the species
					createdEdge = this.insertEdge(this.getDefaultParent(), "", "", insertedVertex, 
							this.getSpeciesCell(prop.getProperty(propName.toString()).toString()));

					String key = id + " Output " + prop.getProperty(propName.toString()).toString();
					componentsConnectionsToMxCellMap.put(key, (mxCell)createdEdge);
				}
				else {
					// input, the arrow should point in from the species
					createdEdge = this.insertEdge(this.getDefaultParent(), "", "", 
							this.getSpeciesCell(prop.getProperty(propName.toString()).toString()),
							insertedVertex);

					String key = id + " Input " + prop.getProperty(propName.toString()).toString();
					componentsConnectionsToMxCellMap.put(key, (mxCell)createdEdge);
				}
				
				this.updateComponentConnectionVisuals((mxCell)createdEdge, propName.toString());
			}
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
		
		Properties prop = gcm.getSpecies().get(sp);
		String id = prop.getProperty("ID", "");
		String type = prop.getProperty("Type", "");
		
		if (id==null) {
			id = prop.getProperty("label", "");
		}		
		
		String label = id + '\n' + type;

		CellValueObject cvo = new CellValueObject(label, prop);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.speciesToMxCellMap.put(id, (mxCell)insertedVertex);
		
		this.setSpeciesStyles(sp);
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex, prop);
	}
	
	/**
	 * creates a vertex on the graph using the internal model.
	 * @param id
	 * 
	 * @return: A bool, true if the reaction had to be positioned.
	 */
	private boolean createGraphReactionFromModel(String id){
		
		Properties prop = gcm.getReactions().get(id);

		CellValueObject cvo = new CellValueObject(id, prop);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.reactionsToMxCellMap.put(id, (mxCell)insertedVertex);
		
		this.setReactionStyles(id);
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex, prop);
	}
	
	/**
	 * Creates a drawn promoter using the internal model
	 * @param pname
	 * @return
	 */
	private boolean createGraphDrawnPromoterFromModel(String pname){
		
		Properties prop = gcm.getPromoters().get(pname);
		
		if(prop == null){
			throw new Error("No promoter could be found that matches the name " + pname);
		}
		
		String id = prop.getProperty("ID", "");
		
		if (id==null) {
			id = prop.getProperty("label", "");
		}

		CellValueObject cvo = new CellValueObject(id, prop);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
		this.drawnPromoterToMxCellMap.put(id, (mxCell)insertedVertex);
		
		this.setDrawnPromoterStyles(pname);
		
		return sizeAndPositionFromProperties((mxCell)insertedVertex, prop);
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
		
		//species
		Hashtable<String, Object> style = new Hashtable<String, Object>();
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
		
		//complex formation edge (species to species)
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#4E5D9C");
		style.put(mxConstants.STYLE_STROKECOLOR, "#4E5D9C");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_DIAMOND);
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
	
	/**
	 * 
	 * @param id
	 * @param compart
	 */
	private void setComponentStyles(String id, boolean compart){
		
		String style = "";
		
		if (gcm.getGrid().isEnabled()) {
			
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
	
	
	//ANIMATION
	
	/**
	 * 
	 */
	public void setSpeciesAnimationValue(String species, MovieAppearance appearance){
		
		mxCell cell = this.speciesToMxCellMap.get(species);
		Properties prop = gcm.getSpecies().get(species);
		assert(prop != null);
		setCellAnimationValue(cell, appearance, prop);
	}
	
	/**
	 * 
	 */
	public void setComponentAnimationValue(String component, MovieAppearance appearance){
		
		mxCell cell = this.componentsToMxCellMap.get(component);
		Properties prop = gcm.getComponents().get(component);
		assert(prop != null);
		setCellAnimationValue(cell, appearance, prop);
	}
	
	public void setGridRectangleAnimationValue(String gridLocation, MovieAppearance appearance) {
		
		mxCell cell = this.gridRectangleToMxCellMap.get(gridLocation);
		setCellAnimationValue(cell, appearance, null);
	}
	
	/**
	 * Applies the MovieAppearance to the cell
	 * @param cell
	 * @param appearance
	 * @param properties
	 */
	private void setCellAnimationValue(mxCell cell, MovieAppearance appearance, Properties prop){
		
		if(appearance == null)
			return;
		
		// color
		String newStyle = cell.getStyle() + ";";
		
		if(appearance.color != null){
			
			newStyle += mxConstants.STYLE_FILLCOLOR + "=" + Integer.toHexString(appearance.color.getRGB()) + ";";
			newStyle += mxConstants.STYLE_OPACITY + "=" + 75;
		}
		
		// opacity
		if(appearance.opacity != null){
			
			if(newStyle == null)
				newStyle = "";
			else
				newStyle += ";";
			
			double op = (appearance.opacity) * 100.0;
			newStyle += mxConstants.STYLE_OPACITY + "=" + String.valueOf(op);
		}
		
		if(newStyle != null)
			cell.setStyle(newStyle);
		
		// size
		if(appearance.size != null){
			
			double gcmX = Double.parseDouble(
					prop.getProperty("graphx", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_WIDTH)));
			double gcmY = Double.parseDouble(
					prop.getProperty("graphy", String.valueOf(GlobalConstants.DEFAULT_SPECIES_HEIGHT)));
			double gcmWidth = Double.parseDouble(
					prop.getProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_WIDTH)));
			double gcmHeight = Double.parseDouble(
					prop.getProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_SPECIES_HEIGHT)));
			
			double aspect_ratio = gcmHeight	/ gcmWidth;
			
			double centerX = gcmX + gcmWidth/2.0;
			double centerY = gcmY + gcmHeight/2.0;
			
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
		public CellValueObject(String label, Properties prop){
			
			if(prop == null || label == null)
				throw new Error("Neither Properties nor label can not be null!");
			
			this.label = label;
			this.prop = prop;
		}
	}	

	
	
	
	
	
	//FUNCTIONS THAT TYLER COMMENTED OUT THAT WILL PROBABLY NEVER BE USED
	
	
//	/**
//	 * Overwrite the parent insertVertex and additionally put the vertex into our hashmap.
//	 * @return
//	 */
//	public Object insertVertex(Object parent, String id, Object value, double x, double y, double width, double height){
//		Object ret = super.insertVertex(parent, id, value, x, y, width, height);
//		this.speciesToMxCellMap.put(id, (mxCell)ret);
//		return ret;
//	}
//	
//	/**
//	 * returns the name of the component-to-species connection
//	 * @param compName
//	 * @param speciesName
//	 * @return
//	 */
//	/*
//	private String getComponentConnectionName(String compName, String speciesName){
//		return compName + " (component connection) " + speciesName;
//	}
//	*/
//	
//	/**
//	 * creates a vertex on the graph using the internal model.
//	 * @param id
//	 * 
//	 * @return: A bool, true if the species had to be positioned.
//	 */
//	/*
//	private boolean createGraphCompartmentFromModel(String sp){
//		Properties prop = new Properties();
//		String id = sp;
//		CellValueObject cvo = new CellValueObject(id, prop);
//		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, 1, 1, 1, 1);
//		this.speciesToMxCellMap.put(id, (mxCell)insertedVertex);
//		
//		this.setSpeciesStyles(sp);
//		
//		return sizeAndPositionFromProperties((mxCell)insertedVertex, prop);
//	}
//	 */
//	
//	/**
//	 * called after a species is deleted. Make sure to delete it from
//	 * the internal model.
//	 * @param id
//	 */
//	public void speciesRemoved(String id){
//		this.speciesToMxCellMap.remove(id);
//	}
//	public void influenceRemoved(String id){
//		this.influencesToMxCellMap.remove(id);
//	}


}