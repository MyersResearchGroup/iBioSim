/**
 * 
 */
package gcm2sbml.gui.modelview;

import gcm2sbml.gui.InfluencePanel;
import gcm2sbml.gui.PropertiesLauncher;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.bind.JAXBElement.GlobalScope;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.view.mxGraph;

/**
 * @author tyler
 *
 */
public class BioGraph extends mxGraph {

	private static final int DEFAULT_SPECIES_WIDTH = 100;
	private static final int DEFAULT_SPECIES_HEIGHT = 20;
	
	/**
	 * Map species to their graph nodes by id. This map is
	 * (or at least, should always be) kept up-to-date whenever
	 * a node is added or deleted.
	 */
	private HashMap<String, mxCell> speciesToMxCellMap;
	private HashMap<String, mxCell> influencesToMxCellMap;
	
	/**
	 * The definitive internal model. We store a reference to it here
	 * so that we can update it any time the model is updated.
	 */
	private HashMap<String, HashMap<String, Properties>> internalModel;
	
	private void initializeMaps(){
		speciesToMxCellMap = new HashMap<String, mxCell>();
		influencesToMxCellMap = new HashMap<String, mxCell>();
	}
	
	public BioGraph(HashMap<String, HashMap<String, Properties>> internalModel){
		super();
		
		// Turn editing off to prevent mxGraph from letting the user change the 
		// label on the cell. We want to do this using the property windows.
		this.setCellsEditable(false);
		
		
		this.initializeMaps();
		this.internalModel = internalModel;
	
	}
	
	public void cellClickHandler(mxCell cell){
		if(cell.isVertex()){
			PropertiesLauncher.getInstance().launchSpeciesEditor(cell.getId());
		}else{
			PropertiesLauncher.getInstance().launchInfluencePanel(cell.getId());
		}
	}
	
	public void updateAllSpeciesPosition(){
		
		for(mxCell cell:this.speciesToMxCellMap.values()){
			updateInternalPosition(cell);
		}
	}

	/**
	 * Given a cell that must be a species, update the internal model to reflect it's coordinates.
	 * 
	 */
	public void updateInternalPosition(mxCell cell){
		Properties prop = this.internalModel.get("species").get(cell.getId());
		mxGeometry geom = cell.getGeometry();
		prop.setProperty("graphx", String.valueOf(geom.getX()));
		prop.setProperty("graphy", String.valueOf(geom.getY()));
		prop.setProperty("graphwidth", String.valueOf(geom.getWidth()));
		prop.setProperty("graphheight", String.valueOf(geom.getHeight()));
	}

	
	
	/**
	 * Overwrite the parent insertVertex and additionally put the vertex into our hashmap.
	 * @return
	 */
	public Object insertVertex(Object parent, String id, Object value, double x, double y, double width, double height){
		Object ret = super.insertVertex(parent, id, value, x, y, width, height);
		this.speciesToMxCellMap.put(id, (mxCell)ret);
		return ret;
	}
	public Object getSpeciesCell(String id){
		return speciesToMxCellMap.get(id);
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
	public Object get_influence(String id){
		return influencesToMxCellMap.get(id);
	}
	
	/**
	 * Builds the graph based on the internal representation
	 * @return
	 */
	public boolean buildGraph(){
		BioGraph graph = this;
		
		// remove all the components from the graph if there are any
		graph.selectAll();
		graph.removeCells();
		initializeMaps();
		
		assert(this.internalModel != null);
		
		Object parent = graph.getDefaultParent();
		
		// Start an undo transaction
		graph.getModel().beginUpdate();
		
		// Keep track of how many elements did not have positioning info.
		// This allows us to stack them in the topleft corner until they
		// are positioned by the user or a layout algorithm.
		int unpositioned_species_count = 0;
		int species_count = 0;
		for(String sp:internalModel.get("species").keySet()){ // SPECIES
			Properties prop = internalModel.get("species").get(sp);
			
			double x = Double.parseDouble(prop.getProperty("graphx", "-9999"));
			double y = Double.parseDouble(prop.getProperty("graphy", "-9999"));;
			double width = Double.parseDouble(prop.getProperty("graphwidth", String.valueOf(DEFAULT_SPECIES_WIDTH)));;
			double height = Double.parseDouble(prop.getProperty("graphheight", String.valueOf(DEFAULT_SPECIES_HEIGHT)));
			String id = !(prop.getProperty(GlobalConstants.NAME, "").equals("")) ? 
							prop.getProperty(GlobalConstants.NAME) : 
								!prop.getProperty("ID", "").equals("") ? prop.getProperty("ID", ""):
									prop.getProperty("label");
						
			if(x < -9998 || y < -9998){
				unpositioned_species_count += 1;
				// Line the unpositioned species up nicely. The mod is there as a rough
				// and dirty way to prevent
				// them going off the bottom or right hand side of the screen.
				x = (unpositioned_species_count%50) * 20;
				y = (unpositioned_species_count%10) * (DEFAULT_SPECIES_HEIGHT + 10);
			}
						
			species_count += 1;
			
			graph.insertVertex(parent, id, id, x, y, width, height);
		}

		// add all the edges
		for(String inf:internalModel.get("influences").keySet()){
			Properties prop = internalModel.get("influences").get(inf);
			
			String id = prop.getProperty(GlobalConstants.NAME) != null ? 
					prop.getProperty(GlobalConstants.NAME) : prop.getProperty("label");
			
			// build the edge style
			// Look in mxConstants to see all the pre-built styles.
			String style = "defaultEdge;" + mxConstants.STYLE_ENDARROW + "=";
			if(prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION))
				style += mxConstants.ARROW_CLASSIC;
			else if(prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.REPRESSION))
				style += mxConstants.ARROW_OVAL;
			else
				style += mxConstants.ARROW_OPEN; // This should never happen.
//			=[';p']
			String label = prop.getProperty(GlobalConstants.PROMOTER, "");
					
			Object edge = graph.insertEdge(parent, id, label, 
					graph.getSpeciesCell(GCMFile.getInput(inf)), 
					graph.getSpeciesCell(GCMFile.getOutput(inf)), 
					style
					);
			
		}
		
		graph.getModel().endUpdate();
		
		return unpositioned_species_count == species_count;
	}
	
	public void applyLayout(String ident, mxGraphComponent graphComponent){
		Layouting.applyLayout(ident, this, graphComponent);
	}
	
}
