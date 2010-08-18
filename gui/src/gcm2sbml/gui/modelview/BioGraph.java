/**
 * 
 */
package gcm2sbml.gui.modelview;

import gcm2sbml.gui.GCM2SBMLEditor;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

//import javax.xml.bind.JAXBElement.GlobalScope;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

/**
 * @author Tyler tpatterson80@gmail.com
 *
 */
public class BioGraph extends mxGraph {

	private static final int DEFAULT_SPECIES_WIDTH = 100;
	private static final int DEFAULT_SPECIES_HEIGHT = 30;
	private static final int DEFAULT_COMPONENT_WIDTH = 80;
	private static final int DEFAULT_COMPONENT_HEIGHT = 40;
	
	/**
	 * Map species to their graph nodes by id. This map is
	 * (or at least, should always be) kept up-to-date whenever
	 * a node is added or deleted.
	 */
	private HashMap<String, mxCell> speciesToMxCellMap;
	private HashMap<String, mxCell> influencesToMxCellMap;
	private HashMap<String, mxCell> componentsToMxCellMap;
	
	
	private GCMFile gcm;
	private GCM2SBMLEditor gcm2sbml; // needed to pull up editor windows
	
	private void initializeMaps(){
		speciesToMxCellMap = new HashMap<String, mxCell>();
		componentsToMxCellMap = new HashMap<String, mxCell>();
		influencesToMxCellMap = new HashMap<String, mxCell>();
	}
	
	public BioGraph(GCMFile gcm, GCM2SBMLEditor gcm2sbml){
		super();
		
		// Turn editing off to prevent mxGraph from letting the user change the 
		// label on the cell. We want to do this using the property windows.
		this.setCellsEditable(false);
		
		this.gcm = gcm;
		this.gcm2sbml = gcm2sbml;
		
		this.initializeMaps();
	
		createStyleSheets();
	}
	
	public void bringUpEditorForCell(mxCell cell){
		if(getCellType(cell) == GlobalConstants.SPECIES){
			gcm2sbml.launchSpeciesPanel(cell.getId());
		}else if(getCellType(cell) == GlobalConstants.INFLUENCE){
			// if an edge, make sure it isn't connected
			// to a component - which aren't really influences at all.
			if(	getCellType(cell.getSource()) == GlobalConstants.SPECIES &&
				getCellType(cell.getTarget()) == GlobalConstants.SPECIES)
			gcm2sbml.launchInfluencePanel(cell.getId());
		}
		// refresh everything.
		this.buildGraph();
	}
	
	public void updateAllSpeciesPosition(){
		
		for(mxCell cell:this.speciesToMxCellMap.values()){
			updateInternalPosition(cell);
		}
		for(mxCell cell:this.componentsToMxCellMap.values()){
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
	 * returns GlobalConstants.SPECIES, GlobalConstants.COMPONENT, GlobalConstants.INFLUENCE, or GlobalConstants.COMPONENT_CONNECTION.
	 * @param cell
	 */
	public String getCellType(mxCell cell){
		if(cell.isEdge())
			if(getCellType(cell.getSource()) == GlobalConstants.COMPONENT || getCellType(cell.getTarget()) == GlobalConstants.COMPONENT){
				return GlobalConstants.COMPONENT_CONNECTION;
			}else{
				return GlobalConstants.INFLUENCE;
			}
		else{
			Properties prop = ((CellValueObject)(cell.getValue())).prop;
			if(gcm.getSpecies().containsValue(prop))
				return GlobalConstants.SPECIES;
			else if(gcm.getComponents().containsValue(prop))
				return GlobalConstants.COMPONENT;
			else
				throw new Error("The type of this cell could not be determined!");
		}
	}
	public String getCellType(mxICell cell){return getCellType((mxCell)cell);}
	
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
			throw new Error("Component Connectiosn don't have properties!");
		else
			throw new Error("Invalid type: " + type);
	}
	
	/**
	 * A convenience function
	 * @param cell
	 * @return
	 */
	public Properties getCellProperties(mxCell cell){
		return getPropertiesList(cell).get(cell.getId());
	}
	public Properties getCellProperties(mxICell cell){
		return getCellProperties((mxCell)cell);
	}
	
//	/**
//	 * Overwrite the parent insertVertex and additionally put the vertex into our hashmap.
//	 * @return
//	 */
//	public Object insertVertex(Object parent, String id, Object value, double x, double y, double width, double height){
//		Object ret = super.insertVertex(parent, id, value, x, y, width, height);
//		this.speciesToMxCellMap.put(id, (mxCell)ret);
//		return ret;
//	}
	public mxCell getSpeciesCell(String id){
		return speciesToMxCellMap.get(id);
	}
	public mxCell getComponentCell(String id){
		return componentsToMxCellMap.get(id);
	}
	
	/**
	 * public Object insertEdge(Object parent,
                         String id,
                         Object value,
                         Object source,
                         Object target,
                         String style)
	 */
	public Object insertEdge(Object parent, String id, Object value, Object source, Object target, String style){
		Object ret = super.insertEdge(parent, id, value, source, target, style);
		this.influencesToMxCellMap.put(id, (mxCell)ret);
		return ret;
	}
	public mxCell getInfluence(String id){
		return (mxCell)(influencesToMxCellMap.get(id));
	}
	
	/**
	 * called after a species is deleted. Make sure to delete it from
	 * the internal model.
	 * @param id
	 */
	public void speciesRemoved(String id){
		this.speciesToMxCellMap.remove(id);
	}
	public void influenceRemoved(String id){
		this.influencesToMxCellMap.remove(id);
	}
	
	/**
	 * Builds the graph based on the internal representation
	 * @return
	 */
	public boolean isBuilding = false;
	public boolean buildGraph(){
		this.isBuilding = true;

		// remove all the components from the graph if there are any
		this.selectAll();
		this.removeCells();
		initializeMaps();
		
		assert(this.gcm != null);
		
		// Start an undo transaction
		this.getModel().beginUpdate();
		
		boolean needsPositioning = false;
		// add species
		for(String sp:gcm.getSpecies().keySet()){ // SPECIES
			if(createGraphSpeciesFromModel(sp))
				needsPositioning = true;
		}

		// add all components
		for(String comp:gcm.getComponents().keySet()){
			if(createGraphComponentFromModel(comp))
				needsPositioning = true;
		}
		
		// add all the edges
		for(String inf:gcm.getInfluences().keySet()){
			this.insertEdge(this.getDefaultParent(), inf, "", 
					this.getSpeciesCell(GCMFile.getInput(inf)), 
					this.getSpeciesCell(GCMFile.getOutput(inf))
					);
			
			updateInfluenceVisuals(inf);
		}
		
		this.getModel().endUpdate();
		
		this.isBuilding = false;
		return needsPositioning;
	}
	
	// Keep track of how many elements did not have positioning info.
	// This allows us to stack them in the topleft corner until they
	// are positioned by the user or a layout algorithm.
	int unpositionedSpeciesComponentCount = 0;
	
	private boolean createGraphComponentFromModel(String id){
		//{invb={ID=invb, gcm=inv.gcm}
		//{invb={ID=invb, gcm=inv.gcm}, i1={b=S3, ID=i1, a=S1, gcm=inv.gcm, type_b=Output, type_a=Input}}

		boolean needsPositioning = false;
		
		Properties prop = gcm.getComponents().get(id);

		double x = Double.parseDouble(prop.getProperty("graphx", "-9999"));
		double y = Double.parseDouble(prop.getProperty("graphy", "-9999"));;
		double width = Double.parseDouble(prop.getProperty("graphwidth", String.valueOf(DEFAULT_COMPONENT_WIDTH)));;
		double height = Double.parseDouble(prop.getProperty("graphheight", String.valueOf(DEFAULT_COMPONENT_HEIGHT)));
				
		if(x < -9998 || y < -9998){
			unpositionedSpeciesComponentCount += 1;
			needsPositioning = true;
			// Line the unpositioned species up nicely. The mod is there as a rough
			// and dirty way to prevent
			// them going off the bottom or right hand side of the screen.
			x = (unpositionedSpeciesComponentCount%50) * 20;
			y = (unpositionedSpeciesComponentCount%10) * (DEFAULT_SPECIES_HEIGHT + 10);
		}
		String label = id + "\n" + prop.getProperty("gcm").replace(".gcm", "");
		CellValueObject cvo = new CellValueObject(label, prop);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, x, y, width, height);
		this.componentsToMxCellMap.put(id, (mxCell)insertedVertex);
		
		this.setComponentStyles(id);

		// now draw the edges that connect the component
		for (Object propName : prop.keySet()) {
			if (!propName.toString().equals("gcm")
					&& !propName.toString().equals(GlobalConstants.ID)
					&& prop.keySet().contains("type_" + propName)) {
				Object createdEdge;
				if (prop.getProperty("type_" + propName).equals("Output")) {
					// output, the arrow should point out to the species
					createdEdge = this.insertEdge(this.getDefaultParent(), "", "", 
							insertedVertex, 
							this.getSpeciesCell(prop.getProperty(propName.toString()).toString())
							);

//					buffer.append(s + " -> " + prop.getProperty(propName.toString()).toString()
//							+ " [port=" + propName.toString() + ", type=Output");
//					buffer.append(", arrowhead=normal]\n");
				}
				else {
					// input, the arrow should point in from the species
					createdEdge = this.insertEdge(this.getDefaultParent(), "", "", 
							this.getSpeciesCell(prop.getProperty(propName.toString()).toString()),
							insertedVertex
							);
//					buffer.append(prop.getProperty(propName.toString()).toString() + " -> " + s
//							+ " [port=" + propName.toString() + ", type=Input");
//					buffer.append(", arrowhead=normal]\n");
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
		
		double x = Double.parseDouble(prop.getProperty("graphx", "-9999"));
		double y = Double.parseDouble(prop.getProperty("graphy", "-9999"));;
		double width = Double.parseDouble(prop.getProperty("graphwidth", String.valueOf(DEFAULT_SPECIES_WIDTH)));;
		double height = Double.parseDouble(prop.getProperty("graphheight", String.valueOf(DEFAULT_SPECIES_HEIGHT)));
		String id = !(prop.getProperty(GlobalConstants.NAME, "").equals("")) ? 
						prop.getProperty(GlobalConstants.NAME) : 
							!prop.getProperty("ID", "").equals("") ? prop.getProperty("ID", ""):
								prop.getProperty("label");
		boolean needsPositioning = false;	
		if(x < -9998 || y < -9998){
			unpositionedSpeciesComponentCount += 1;
			needsPositioning = true;
			// Line the unpositioned species up nicely. The mod is there as a rough
			// and dirty way to prevent
			// them going off the bottom or right hand side of the screen.
			x = (unpositionedSpeciesComponentCount%50) * 20;
			y = (unpositionedSpeciesComponentCount%10) * (DEFAULT_SPECIES_HEIGHT + 10);
		}

		CellValueObject cvo = new CellValueObject(id, prop);
		Object insertedVertex = this.insertVertex(this.getDefaultParent(), id, cvo, x, y, width, height);
		this.speciesToMxCellMap.put(id, (mxCell)insertedVertex);

		
		this.setSpeciesStyles(sp);
		
		return needsPositioning;
	}
	
	/**
	 * Given an id, update the style of the influence based on the internal model.
	 */
	private void updateInfluenceVisuals(String id){
		Properties prop = gcm.getInfluences().get(id);
		
		if(prop == null)
			throw new Error("Invalid id '"+id+"'. Valid ids were:" + String.valueOf(gcm.getInfluences().keySet()));
		
		// build the edge style
		// Look in mxConstants to see all the pre-built styles.
		String style = "defaultEdge;" + mxConstants.STYLE_ENDARROW + "=";
		if(prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION))
			style += mxConstants.ARROW_CLASSIC;
		else if(prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.REPRESSION))
			style += mxConstants.ARROW_OVAL;
		else
			style += mxConstants.ARROW_OPEN; // This should never happen.
//		=[';p']
		
		// apply the style
		mxCell cell = this.getInfluence(id);
		cell.setStyle(style);

		// apply the label
		String label = prop.getProperty(GlobalConstants.PROMOTER, "");
		cell.setValue(label);
	}
	
	public void updateComponentConnectionVisuals(mxCell cell, String label){
		//cell.setStyle(mxConstants.STYLE_ENDARROW + "=" + mxConstants.ARROW_OPEN);
		cell.setStyle("COMPONENT_EDGE");
		cell.setValue("Port " + label);
		
		// position the label as intelligently as possible
		mxGeometry geom = cell.getGeometry();
		
		if(this.getCellType(cell.getSource()) == GlobalConstants.COMPONENT){
			geom.setX(-.6);

		}else{
			geom.setX(.6);

		}
		cell.setGeometry(geom);
	}
	
	/**
	 * Builds the style sheets that will be used by the graph.
	 */
	public void createStyleSheets(){
		mxStylesheet stylesheet = this.getStylesheet();
		
		// Species
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FONTCOLOR, "#774400");
		//style.put(mxConstants.RECTANGLE_ROUNDING_FACTOR, .2);
		style.put(mxConstants.STYLE_ROUNDED, true);
		stylesheet.putCellStyle("SPECIES", style);
		
		// components
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 30);
		style.put(mxConstants.STYLE_FONTCOLOR, "#774400");
		style.put(mxConstants.STYLE_ROUNDED, false);
		style.put(mxConstants.STYLE_FILLCOLOR, "#FFAA00");
		style.put(mxConstants.STYLE_STROKECOLOR, "#AA7700");
		stylesheet.putCellStyle("COMPONENT", style);
		
		style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#774400");
		style.put(mxConstants.STYLE_FILLCOLOR, "#FFAA00");
		style.put(mxConstants.STYLE_STROKECOLOR, "#AA7700");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
		stylesheet.putCellStyle("COMPONENT_EDGE", style);		
	}
	
	private void setSpeciesStyles(String id){
		String style="SPECIES;";
		
		mxCell cell = this.getSpeciesCell(id);
		cell.setStyle(style);
		
	}
	
	private void setComponentStyles(String id){
		String style="COMPONENT;";
		
		mxCell cell = this.getComponentCell(id);
		cell.setStyle(style);		
	}
	
	/**
	 * Creates an influence based on an already created edge.
	 */
	public void addInfluence(mxCell cell, String id, String constType){
		Properties prop = new Properties();
		prop.setProperty(GlobalConstants.NAME, id);
		prop.setProperty(GlobalConstants.TYPE, constType);
		gcm.getInfluences().put(id, prop);
		this.influencesToMxCellMap.put(id, cell);
		updateInfluenceVisuals(id);
	}
	
	/**
	 * creates and adds a new species.
	 * @param id: the new id. If null the id will be generated
	 * @param x
	 * @param y
	 */
	private int creatingSpeciesID = 0;
	public void createSpecies(String id, float x, float y){
		if(id == null){
			do{
				creatingSpeciesID++;
				id = "S" + String.valueOf(creatingSpeciesID);
			}while(gcm.getSpecies().containsKey(id));
		}
		Properties prop = new Properties();
		prop.setProperty(GlobalConstants.NAME, "");
		prop.setProperty("label", id);
		prop.setProperty("ID", id);
		prop.setProperty("Type", "normal");
		prop.setProperty("graphwidth", String.valueOf(DEFAULT_SPECIES_WIDTH));
		prop.setProperty("graphheight", String.valueOf(DEFAULT_SPECIES_HEIGHT));
		centerVertexOverPoint(prop, x, y);
		gcm.getSpecies().put(id, prop);
		
		this.getModel().beginUpdate();
		this.createGraphSpeciesFromModel(id);
		this.getModel().endUpdate();
		
		gcm2sbml.refresh();
		
	}
	
	/**
	 * Given a properties list (species or components) and some coords, center over that point.
	 */
	public void centerVertexOverPoint(Properties prop, double x, double y){
		x -= Double.parseDouble(prop.getProperty("graphwidth", "60"))/2.0;
		y -= Double.parseDouble(prop.getProperty("graphheight", "20"))/2.0;
		prop.setProperty("graphx", String.valueOf(x));
		prop.setProperty("graphy", String.valueOf(y));		
	}
	
	public void applyLayout(String ident, mxGraphComponent graphComponent){
		Layouting.applyLayout(ident, this, graphComponent);
	}
	
	/**
	 * The object that gets set as the mxCell object.
	 * @author tyler
	 *
	 */
	public class CellValueObject extends Object{
		public Properties prop;
		public String label;
		public String toString(){
			return this.label;
		}
		
		public CellValueObject(String label, Properties prop){
			if(prop == null || label == null)
				throw new Error("Neither Properties nor label can not be null!");
			this.label = label;
			this.prop = prop;
		}
	}
	
	//////////////////////////////////////// ANIMATION TYPE STUFF ////////////////////////////////
	private static final double extraAnimationWidth = 30.0;
	private static final double extraAnimationHeight = 20;
	public void setSpeciesAnimationValue(String s, double value){
		mxCell cell = this.speciesToMxCellMap.get(s);
		
//		String newCol = String.valueOf(value / 100 * 16);
//		
//		cell.setStyle(mxConstants.STYLE_FILLCOLOR, "#FFAA00", newCol);
		mxGeometry priorGeom = cell.getGeometry();
		mxGeometry geom = new mxGeometry();
		
		geom.setWidth(extraAnimationWidth + value*.5);
		geom.setHeight(extraAnimationHeight + value*.5);
		geom.setX(priorGeom.getCenterX() - geom.getWidth()*.5);
		geom.setY(priorGeom.getCenterY() - geom.getHeight()*.5);

		cell.setGeometry(geom);
	}
	
}


