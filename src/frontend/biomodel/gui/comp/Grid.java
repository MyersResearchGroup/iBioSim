package frontend.biomodel.gui.comp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.sbml.jsbml.Parameter;

import com.mxgraph.model.mxCell;

import backend.biomodel.annotation.AnnotationUtility;
import backend.biomodel.parser.BioModel;
import backend.biomodel.util.Utility;
import backend.util.GlobalConstants;
import frontend.biomodel.gui.schematic.BioGraph;


/**
 * Contains all of the methods relevant to a grid
 * This is an explict, formal grid which is principally used for tracking diffusible species
 * and the locations of compartments/components/cells in a formal population
 * 
 * @author jason (sarbruis@gmail.com)
 * 
 */
public class Grid {
	
	
	//---------------
	//CLASS VARIABLES
	//---------------
	
	//the actual grid -- a 2d arraylist of gridnodes
	private ArrayList<ArrayList<GridNode>> grid;
	
	//map of gridnodes and their corresponding rectangles
	private HashMap<Rectangle, GridNode> rectToNodeMap;
	
	//components on the grid
	//private HashMap<String, Properties> components;
	private BioModel bioModel;
	
	//map of row, col grid locations to the component at that location
	private HashMap<Point, String> locToComponentMap;
	
	//map of compartment ID strings to the corresponding grid node
	private HashMap<String, GridNode> componentIDToNodeMap;
	
	private int numRows, numCols;
	private int verticalOffset;
	private double padding; //padding for the grid rectangles
	private double gridWidth;
	private double gridHeight;
	private double gridGeomHeight; //to retain geometric shape for each grid rectangle
	private double gridGeomWidth; //to retain geometric shape for each grid rectangle
	private double componentGeomWidth;
	private double componentGeomHeight;
	private double zoomAmount;
	private boolean enabled;
	private boolean mouseClicked;
	private boolean mouseReleased; //used for rubberband release
	private Point scrollOffset; //for drawing when scrolled
	private Rectangle rubberbandBounds;	
	private Rectangle gridBounds;
	private Point mouseClickLocation;
	private Point mouseLocation;
	
	private BioGraph graph;
	
	
	
	//-------------
	//CLASS METHODS
	//-------------
	
	
	//PUBLIC
	
	
	//GRID CREATION METHODS	
	
	/**
	 * default constructor
	 */
	public Grid() {
 
		enabled = false;
		mouseReleased = false;
		verticalOffset = 0;
		numRows = 0;
		numCols = 0;
		padding = 30;
		zoomAmount = 1;
		gridWidth = GlobalConstants.DEFAULT_COMPONENT_WIDTH + padding;
		gridHeight = GlobalConstants.DEFAULT_COMPONENT_HEIGHT + padding;
		gridGeomWidth = GlobalConstants.DEFAULT_COMPONENT_WIDTH + padding;
		gridGeomHeight = GlobalConstants.DEFAULT_COMPONENT_HEIGHT + padding;
		componentGeomWidth = GlobalConstants.DEFAULT_COMPONENT_WIDTH;
		componentGeomHeight = GlobalConstants.DEFAULT_COMPONENT_HEIGHT;
		scrollOffset = new Point(0, 0);
		
		gridBounds = new Rectangle();
		rubberbandBounds = new Rectangle();
		mouseClickLocation = new Point();
		mouseLocation = new Point();
		grid = new ArrayList<ArrayList<GridNode>>();
		rectToNodeMap = new HashMap<Rectangle, GridNode>();
		locToComponentMap = new HashMap<Point, String>();
		componentIDToNodeMap = new HashMap<String, GridNode>();
		graph = null;
	}
	
	/**
	 * creates a 2d arraylist of GridNodes of rows x cols
	 * usually called from GridPanel
	 * 
	 * @param rows number of rows in the grid
	 * @param cols number of columns in the grid
	 * @param components the components that are located on the grid
	 */
	public void createGrid(int rows, int cols, BioModel gcm, String compGCM) {
		
		//if the grid size is 0 by 0, don't make it
		if (rows <= 0 || cols <= 0) {
			
			enabled = false; //should be false already, but whatever
			return;
		}
		
		enabled = true;
		numRows = rows;
		numCols = cols;
		
		//gcm.setIsWithinCompartment(true);
		for(int row = 0; row < numRows; ++row) {
			
			grid.add(new ArrayList<GridNode>(numCols));
			
			for(int col = 0; col < numCols; ++col) {
				
				grid.get(row).add(new GridNode());
				grid.get(row).get(col).setRow(row);
				grid.get(row).get(col).setCol(col);
				
				//null signifies that the components are already in the GCM
				if (compGCM != null)
					addComponentToGCM(row, col, compGCM, gcm);
			}
		}
		
		this.bioModel = gcm;
		
		updateGridRectangles();
		updateRectToNodeMap();
		updateLocToComponentMap();
		updateComponentIDToNodeMap();
		putComponentsOntoGrid();
	}
	
	
	//GRID DRAWING
	
	/**
	 * draws the grid selection boxes and updates the local graph pointer
	 * usually called from Schematic
	 * UPDATE: the grid rectangles are now cells in the graph
	 * so, the drawing of the grid happens automatically when the graph is drawn
	 * but this function still draws the hover/selection boxes
	 * 
	 * @param g Graphics object (from the Schematic JPanel)
	 */
	public void drawGrid(Graphics g, BioGraph graph) {
		
		//Graphics2D g2 = (Graphics2D) g;
		
		this.graph = graph;
		
		boolean selectionOff = false;
		
		//if the user has completed dragging the rubberband
		if (rubberbandBounds != null && rubberbandBounds.height > 5
				&& rubberbandBounds.width > 5 && mouseReleased == true) {
			
			selectGridLocationsWithRubberband(g);
		}
		//if the user's mouse is within the grid bounds
		else {
			
			if (gridBounds.contains(mouseLocation)) {		
			
			//draw a hover-rectangle over the grid location
			hoverGridLocation(g);
			}
			
			if (gridBounds.contains(mouseClickLocation)) {
			
				//if the user has clicked, select/de-select that location
				if (mouseClicked) selectGridLocation();
			}
			//if the user clicks out of bounds
			//de-select all grid locations
			else {
				
				if (mouseClicked) selectionOff = true;
			}
		}
		
		//draw the selection boxes for selected nodes
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				GridNode node = grid.get(row).get(col);
				Rectangle rect = node.getZoomedRectangle();
				
				if (selectionOff) node.setSelected(false);
				
				if (node.isSelected())
					drawGridSelectionBox(g, rect);
				
//				//some debug stuff to draw onto the grid
//				g2.drawString(Boolean.toString(node.isSelected()), rect.x, rect.y);
//				
//				if (node.component == null) {
//					g2.drawString("null", rect.x+40, rect.y);
//				}
//				else g2.drawString(node.getComponent().getKey(), rect.x+40, rect.y);
			}
		}
	}
	
	/**
	 * this draws a box around the grid location when the user hovers over it
	 * with the mouse or clicks it, which is used for grid location selection
	 * 
	 * @param g Graphics object from the Schematic JPanel
	 * @param row the row of the selected location
	 * @param col the column of the selected location
	 */
	public void drawGridSelectionBox(Graphics g, Rectangle rect) {
		
		g.setColor(new Color(0, 0, 255, 30));
		
		//don't change the original rectangle; make a copy
		Rectangle localRect = new Rectangle(rect);
		localRect.x -= scrollOffset.x - 1;
		localRect.y -= scrollOffset.y;

		g.fillRect(localRect.x, localRect.y, localRect.width, localRect.height);
		
		g.setColor(Color.black);
	}	
	
	/**
	 * this draws a box around the grid location when the user hovers over it
	 * with the mouse or clicks it, which is used for grid location selection
	 * 
	 * @param g Graphics object from the Schematic JPanel
	 * @param row the row of the selected location
	 * @param col the column of the selected location
	 */
	public void drawGridHoverBox(Graphics g, Rectangle rect) {
		
		g.setColor(new Color(0, 0, 255, 150));
		
		//don't change the original rectangle; make a copy
		Rectangle localRect = new Rectangle(rect);
		localRect.x -= scrollOffset.x - 1;
		localRect.y -= scrollOffset.y;
		
		//have to loop to get a thickness > 1
		for (int i = 0; i < 3; ++i) {
			
			localRect.x += 1; localRect.y += 1; localRect.width -= 2; localRect.height -= 2;
			g.drawRect(localRect.x, localRect.y, localRect.width, localRect.height);
		}
		
		g.setColor(Color.black);
	}
	
	
	//NODE METHODS
	
	/**
	 * clears a node and then removes it from the gcm
	 * 
	 * @param compID component ID (uniquely identifies the node)
	 * @param gcm gcm file
	 */
	public void eraseNode(String compID, BioModel gcm) {
		
		//clear the grid node data (the actual node stays, though)
		clearNode(compID);		
		
		//remove the component from the gcm
		gcm.removeComponent(compID);
	}
	
	/**
	 * empties a node on the grid based on a component ID
	 * 
	 * @param compID
	 */
	public void clearNode(String compID) {
		
		GridNode node = getNodeFromCompID(compID);
		
		if (node != null && node.getComponent() != null) {
			node.clear();
		}
	}
	
	/**
	 * allocates a new node and sets the grid location
	 * also adds the node to the gcm
	 * 
	 * @param row
	 * @param col
	 * @param compGCM
	 * @param gcm
	 */
	public void addNode(int row, int col, String compGCM, BioModel gcm) {
		
		grid.get(row).add(new GridNode());
		grid.get(row).get(col).setRow(row);
		grid.get(row).get(col).setCol(col);
		
		if (!compGCM.equals("none")) {
			
			grid.get(row).get(col).setOccupied(true);		
			addComponentToGCM(row, col, compGCM, gcm);
		}
	}
	
	/**
	 * tries to move a node given the component ID and the center x,y coords
	 * of where the user is trying to move the component
	 * 
	 * @param compID id of the component
	 * @param centerX center x coord of where the component is trying to be moved to
	 * @param centerY center y coord of where the component is trying to be moved to
	 */
	public boolean moveNode(String compID, double centerX, double centerY, BioModel gcm) {
		
		//adjust the point for zoom
		Point moveToPoint = new Point((int)(centerX*zoomAmount), (int)(centerY*zoomAmount + verticalOffset));
		
		//if the user is trying to move the component to a place within the grid
		//then pay attention
		if (gridBounds.contains(moveToPoint)) {
			
			GridNode node = getNodeFromPoint(moveToPoint);
			
			//this shouldn't be null because we're on the grid, but just in case . . .
			if (node != null) {
				
				//make sure there isn't a component in the location
				if (node.isOccupied() == false) {
										
					//this is a horrible hack to get a Map.Entry<String, Property>
					//i didn't realize it was an interface and not instantiable
					//HashMap<String, Properties> tempMap = new HashMap<String, Properties>();
					//tempMap.put(compID, components.get(compID));
					
					//Map.Entry<String, Properties> compo = tempMap.entrySet().iterator().next();
					
					//clear the old component's spot
					clearNode(compID);
					
					//put the component in its new home
					node.setComponent(compID);
					node.setOccupied(true);
					
					//Properties props = compo.getValue();
					
					gcm.setSubmodelRowCol(compID, node.getRow(), node.getCol());
					
					//update the location to component hash map
					//as the component and their locations have changed
					updateLocToComponentMap();
					
					return true;
				}
				return false;
			}
			return false;
		}
		return false;		
	}
	
	/**
	 * clears all nodes that are selected
	 */
	public void eraseSelectedNodes(BioModel gcm) {
		
		//loop through all nodes
		//if it's selected, clear it
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				if (grid.get(row).get(col).isSelected() && grid.get(row).get(col).isOccupied())
					eraseNode(grid.get(row).get(col).getComponent(), gcm);
			}
		}
	}
	
	/**
	 * sets every node to selected
	 */
	public void selectAllNodes() {
		
		//loop through each grid node to see if one is selected
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				grid.get(row).get(col).setSelected(true);
			}
		}
		
		mouseClicked = false;
	}
	
	/**
	 * sets every node to deselected
	 */
	public void deselectAllNodes() {
		
		//loop through each grid node to see if one is selected
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				grid.get(row).get(col).setSelected(false);
			}
		}
	}
	
	/**
	 * returns the row/col of each selected node in an arraylist
	 * @return the row/col of each selected node
	 */
	public ArrayList<Point> getSelectedUnoccupiedNodes() {
		
		ArrayList<Point> selectedNodes = new ArrayList<Point>();
		
		//loop through each grid node to see if one is selected
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				if (grid.get(row).get(col).isSelected() && !grid.get(row).get(col).isOccupied())
					selectedNodes.add(new Point(grid.get(row).get(col).getRow(), grid.get(row).get(col).getCol()));
			}
		}
		
		return selectedNodes;
	}

	/**
	 * updates and node's rectangle
	 * 
	 * @param compID
	 * @param rectangle
	 */
	public void setNodeRectangle(String compID, Rectangle rectangle) {
		
		getNodeFromCompID(compID).setRectangle(rectangle);
	}
	
	
	//INFORMATION RETRIEVAL METHODS
	
	/**
	 * finds and returns the snap rectangle from the component id passed in
	 * @param compID component id
	 * @return the snap rectangle
	 */
	public Rectangle getSnapRectangleFromCompID(String compID) {
		
		return getNodeFromCompID(compID).getSnapRectangle();
	}
	
	/**
	 * returns where or not the user clicked on an occupied grid location
	 * @param mouseX where the user clicked
	 * @param mouseY where the user clicked
	 * @return true if the grid location is occupied, false if not
	 */
	public boolean getOccupancyFromPoint(Point point) {
		
		point.y += verticalOffset;
		GridNode node = getNodeFromPoint(point);
		
		if (node != null)
			return node.isOccupied();
		return true;
	}
	
	/**
	 * returns the grid row corresponding to a point
	 * 
	 * @param point
	 * @return the grid row corresponding to the point
	 */
	public int getRowFromPoint(Point point) {
		
		point.y += verticalOffset;
		return getNodeFromPoint(point).getRow();
	}
	
	/**
	 * returns the grid column corresponding to a point
	 * 
	 * @param point
	 * @return the grid column corresponding to the point
	 */
	public int getColFromPoint(Point point) {
		
		point.y += verticalOffset;
		return getNodeFromPoint(point).getCol();
	}
	
	/**
	 * returns whether or not the user clicked within a grid but outside of the component
	 * (ie, in the grid's padding area)
	 * 
	 * @param clickPoint where the user clicked
	 * @return whether or not the user clicked within a grid but outside of the component
	 */
	public boolean clickedOnGridPadding(Point clickPoint) {
		
		clickPoint.y += verticalOffset;
		
		//if the user clicked on the grid somewhere
		if (gridBounds.contains(clickPoint)) {
			
			//get the grid node corresponding to the click point
			GridNode node = getNodeFromPoint(clickPoint);
						
			if (node == null) return true;
			
			//set the snap rectangles operate on mxGraph coordinates
			//so we need to erase the vertical offset
			clickPoint.y -= verticalOffset;
		
			//if the user didn't click within the component
			if (!node.getZoomedSnapRectangle().contains(clickPoint) || node.isOccupied() == false) {
				return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * returns whether or not there's something selected
	 * this is used for changing the right-click context menu
	 * 
	 * @return whether or not there's something selected
	 */
	public boolean isALocationSelected() {
		
		//loop through each grid node
		//if one is selected, return true
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				if (grid.get(row).get(col).isSelected())
					return true;
			}
		}
		
		return false;
	}

	/**
	 * returns the component ID that occupies a location
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public String getCompIDFromLocation(int row, int col) {
		
		return grid.get(row).get(col).getComponent();
	}
	
	/**
	 * returns the boolean occupancy state of a grid location
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public boolean getOccupancyFromLocation(int row, int col) {
	
		return grid.get(row).get(col).isOccupied();
	}
	
	
	//GRID UPDATING METHODS
	
	/**
	 * updates the local graph pointer and updates the grid's rectangles and maps
	 * according to the new graph passed in
	 * 
	 * this is used when the grid sizes/positions change, like with scrolling or zooming
	 * 
	 * @param graph
	 */
	public void syncGridGraph(BioGraph graph) {
		
		this.graph = graph;
		updateGridRectangles();
		updateLocToComponentMap();
		updateRectToNodeMap();
	}
	
	/**
	 * takes the gcm's components and resets the grid with those components
	 * this happens after adding or deleting a node or nodes (like in resizing)
	 */
	public void refreshComponents() {
		
		//update the graph
		if (graph != null)
			graph.buildGraph();
		
		putComponentsOntoGrid();
		updateGridRectangles();
		
		//update the location to component hash map
		//and the rectangle to node map
		//as the components and their locations have changed
		updateLocToComponentMap();		
		updateRectToNodeMap();
	}
	
	/**
	 * changes the grid size
	 * also updates the GCM components
	 * 
	 * @param rows number of rows in the new grid size
	 * @param cols number of cols in the new grid size
	 * @param compGCM the name of the gcm of the new components
	 * @param gcm the gcm file that's being altered
	 */
	public void changeGridSize(int rows, int cols, String compGCM, BioModel gcm) {
		
		int currentNumRows = this.numRows;
		int currentNumCols = this.numCols;
		int newNumRows = rows;
		int newNumCols = cols;
		int numRowsToAdd = newNumRows - currentNumRows;
		int numColsToAdd = newNumCols - currentNumCols;
		
		//growing the grid rows
		if (numRowsToAdd > 0) {
			
			//add new rows up to the new number of columns
			for (int row = currentNumRows; row < newNumRows; ++row) {
				
				//allocate grid nodes for the new rows
				//note: this could be small than the old size
				//but that doesn't matter
				grid.add(new ArrayList<GridNode>(newNumCols));
				
				for(int col = 0; col < newNumCols; ++col) {
					
					addNode(row, col, compGCM, gcm);
				}
			}
		}
		//shrinking the grid rows
		else if (numRowsToAdd < 0) {
			
			//go from the new last row to the prior last row
			//erase all of the nodes (which clears them and erases the components from the gcm)
			for (int row = newNumRows; row < currentNumRows; ++row) {				
				for (int col = 0; col < currentNumCols; ++col) {
					
					String compID = locToComponentMap.get(new Point(row, col));
					
					//if it's null, there's nothing to erase
					if (compID != null) {
						
						eraseNode(compID, gcm);
					}
				}				
			}
			
			//change this so that the columns aren't added with length past the proper number of rows
			currentNumRows = newNumRows;
		}
		
		//growing the grid cols
		if (numColsToAdd > 0) {
			
			//add new columns up to the current number of rows
			for (int row = 0; row < currentNumRows; ++row) {
				
				//allocate grid nodes for the new columns
				grid.add(new ArrayList<GridNode>(numColsToAdd));
				
				for(int col = currentNumCols; col < newNumCols; ++col) {
					
					addNode(row, col, compGCM, gcm);
				}				
			}
		}
		//shrinking the grid cols
		else if (numColsToAdd < 0) {
			
			//go from the new last col to the prior last col
			//erase all of the nodes (which clears them and erases the components from the gcm)
			for (int row = 0; row < currentNumRows; ++row) {				
				for (int col = newNumCols; col < currentNumCols; ++col) {
					
					String compID = locToComponentMap.get(new Point(row, col));
					
					//if it's null, there's nothing to erase
					if (compID != null) {
						eraseNode(compID, gcm);
					}
				}				
			}
		}
		
		this.numRows = newNumRows;
		this.numCols = newNumCols;
		
		//put the components onto the grid and update hash maps and update the graph
		refreshComponents();
	}
	
	/**
	 * called when component locations change
	 * puts components in their proper locations
	 * 
	 * @param componentToLocationMap
	 */
	public void updateComponentLocations(HashMap<String, Point> componentToLocationMap) {
		
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				grid.get(row).get(col).setComponent("none");
				grid.get(row).get(col).setOccupied(false);
			}
		}		
		
		//now go through the updated list of components and put them in the proper spot
		for (Map.Entry<String, Point> componentAndLocation : componentToLocationMap.entrySet()) {
			
			GridNode node = grid.get((int) componentAndLocation.getValue().getX())
			.get((int) componentAndLocation.getValue().getY());
			
			node.setComponent(componentAndLocation.getKey());
			node.setOccupied(true);
			node.setRow((int) componentAndLocation.getValue().getX());
			node.setCol((int) componentAndLocation.getValue().getY());
		}
	}
	
	/**
	 * changes the grid size to a new number of rows/cols
	 * and repopulates the grid
	 * this is used during dynamic simulation playback
	 * 
	 * @param rows
	 * @param cols
	 */
	public void resetGrid(int rows, int cols) {
		
		this.numRows = rows;
		this.numCols = cols;
		
		grid = new ArrayList<ArrayList<GridNode> >(numRows);
		
		//clear the grid of its components
		for (int row = 0; row < numRows; ++row) {
			
			grid.add(new ArrayList<GridNode>(numCols));
			
			for (int col = 0; col < numCols; ++col) {
				
				grid.get(row).add(new GridNode());
				
				grid.get(row).get(col).setComponent("none");
				grid.get(row).get(col).setOccupied(false);
			}			
		}
	}
	
	
	
	
	//PRIVATE
	
	
	//HASH MAP UPDATING METHODS
	
	/**
	 * creates a hash map of corresponding rectangles and nodes
	 * this allows easy access of nodes from rectangle coordinates
	 */
	private void updateRectToNodeMap() {
		
		rectToNodeMap.clear();
		
		//loop through the rows and columns
		//find the grid node and rectangle then make it a map entry
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
		
				rectToNodeMap.put(grid.get(row).get(col).getZoomedRectangle(), grid.get(row).get(col));				
			}
		}
	}

	/**
	 * creates a hash map for easy access of the component at a specified grid location
	 * with the component you can get its name and properties
	 * used for diffusion reaction printing in printDiffusion
	 * uses 1-indexed row/col numbers!!!
	 */
	private void updateLocToComponentMap() {
		
		locToComponentMap.clear();	
		
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getParameterCount(); ++i) {
			
			Parameter parameter = bioModel.getSBMLDocument().getModel().getParameter(i);
			
			//if it's a location parameter
			if (parameter.getId().contains("__locations")) {

				String[] splitAnnotation = AnnotationUtility.parseArrayAnnotation(parameter);

				for (int j = 1; j < splitAnnotation.length; ++j) {
					
					splitAnnotation[j] = splitAnnotation[j].trim();
					
					String submodelID = splitAnnotation[j].split("=")[0];
					
					int row = bioModel.getSubmodelRow(submodelID);
					int col = bioModel.getSubmodelCol(submodelID);
					
					locToComponentMap.put(new Point(row, col), submodelID);
				}
			}
		}
	}
	
	/**
	 * creates a hashmap for easy access to node information from a component ID
	 */
	private void updateComponentIDToNodeMap() {
		
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				GridNode node = grid.get(row).get(col);
				
				if (node != null && node.getComponent() != null)
					componentIDToNodeMap.put(node.getComponent(), node);
			}
		}
	}
	
	
	//INFORMATION RETRIEVAL METHODS
	
	/**
	 * returns the corresponding grid node for the given point
	 * 
	 * @param point
	 * @return the corresponding grid node
	 */
	private GridNode getNodeFromPoint(Point point) {
		
		//loop through every row and column
		//check this location's rectangle against the x,y point
		//if it contains it, return that node
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				Rectangle rect = grid.get(row).get(col).getZoomedRectangle();
				
				if (rect.contains(point))					
					return rectToNodeMap.get(rect);
			}
		}
		
		return null;
	}
	
	/**
	 * finds and returns the grid node with the component id passed in
	 * @param compID
	 * @return the grid node with the component id
	 */
	private GridNode getNodeFromCompID(String compID) {
		
		updateComponentIDToNodeMap();
		return componentIDToNodeMap.get(compID);
	}
	
	
	//GRID UPDATING METHODS
	
	/**
	 * sets the rectangles/bounds for each node
	 * also sets the bounds for the entire grid
	 */
	private void updateGridRectangles() {

		//create/set rectangles for each location in grid
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				//find the bounds from the cell at this row/col
				//use that rectangle as the node's rectangle
				String cellID = "ROW" + row + "_COL" + col;
				
				if (graph != null && graph.getGridRectangleCellFromID(cellID) != null) {
					
					//set the component sizes (which may have changed due to zooming)
					String compId = grid.get(row).get(col).getComponent();
					
					if (compId != null) {
						
						mxCell compCell = graph.getComponentCell(compId);
						
						if (compCell != null) {
							
							Rectangle compCellRect = graph.getCellGeometry(compCell).getRectangle();							
							componentGeomHeight = compCellRect.getHeight();
							componentGeomWidth = compCellRect.getWidth();
						}
					}
					
					//set the zoom scale
					zoomAmount = graph.getView().getScale();
					
					//get the bounds information from the graph and set the grid
					//rectangle's data to fit with the graph's data
					mxCell cell = graph.getGridRectangleCellFromID(cellID);					
					Rectangle cellGeom = graph.getCellGeometry(cell).getRectangle();
					
					gridGeomHeight = cellGeom.getHeight();
					gridGeomWidth = cellGeom.getWidth();				
					gridHeight = gridGeomHeight * zoomAmount;
					gridWidth = gridGeomWidth * zoomAmount;
					
					//set the current padding value (which may have changed due to zooming)
					//this is usually going to be 30
					padding = gridGeomWidth - componentGeomWidth;
					
					grid.get(row).get(col).setRectangle(cellGeom);
				}
			}
		}
		
		//set the total grid bounds
		int outerX = (int)gridWidth * numCols;
		int outerY = (int)gridHeight * numRows + verticalOffset;
		
		gridBounds.setBounds(15, 15, outerX, outerY);
	}
	
	/**
	 * takes the component list and uses their row/col properties to
	 * place them onto the grid
	 * 
	 * i think this should only be run once on grid creation
	 */
	private void putComponentsOntoGrid() {
		
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getParameterCount(); ++i) {
			
			Parameter parameter = bioModel.getSBMLDocument().getModel().getParameter(i);
			
			//if it's a location parameter
			if (parameter.getId().contains("__locations")) {
			
				String[] splitAnnotation = AnnotationUtility.parseArrayAnnotation(parameter);
							
				for (int j = 1; j < splitAnnotation.length; ++j) {
					
					splitAnnotation[j] = splitAnnotation[j].trim();
					
					String submodelID = splitAnnotation[j].split("=")[0];
					
					int row = bioModel.getSubmodelRow(submodelID);
					int col = bioModel.getSubmodelCol(submodelID);
					
					String gcmName = parameter.getId().replace("__locations","");
					
					//if adding blank components, don't set the component
					if (gcmName.equals("none"))
						grid.get(row).get(col).setOccupied(false);
					else {
	
						grid.get(row).get(col).setComponent(submodelID);
						grid.get(row).get(col).setOccupied(true);
					}
				}
			}
		}		
	}

	/**
	 * adds a component to the GCM passed in
	 * 
	 * @param row
	 * @param col
	 * @param compGCM the name of the component's model file
	 * @param bioModel the model to put the component in
	 */
	private void addComponentToGCM(int row, int col, String compGCM, BioModel bioModel) {
		
		double padding = 30;
		double width = componentGeomWidth;
		double height = componentGeomHeight;
		
		//don't put blank components onto the grid or model
		if (!compGCM.equals("none")) {
			BioModel compGCMFile = new BioModel(bioModel.getPath());
			compGCMFile.load(bioModel.getPath() + GlobalConstants.separator + compGCM);
			String md5 = Utility.MD5(compGCMFile.getSBMLDocument());
			bioModel.addComponent(null, compGCM, compGCMFile.IsWithinCompartment(), compGCMFile.getCompartmentPorts(), row, col, 
					col * (width + padding) + padding, row * (height + padding) + padding, md5);
		}
	}
	
	
	//HOVER AND SELECTION METHODS
	
	/**
	 * based on the mouse location, draws a box around the grid location
	 * that the user is hovering over
	 * 
	 * @param location the mouse location x,y coordinates
	 */
	private void hoverGridLocation(Graphics g) {
		
		GridNode hoveredNode = getNodeFromPoint(mouseLocation);	
		
		if (hoveredNode != null) {
			drawGridHoverBox(g, hoveredNode.getZoomedRectangle());
		}
	}
	
	/**
	 * based on the mouse-click location, selects the grid location
	 * also draws a selection box
	 * @param location the mouse-click x,y coordinates
	 */
	private void selectGridLocation() {
		
		GridNode hoveredNode = getNodeFromPoint(mouseClickLocation);
		
		//select or de-select the grid location
		if (hoveredNode != null)
			hoveredNode.setSelected(hoveredNode.isSelected() == true ? false : true);
		
		//this click's action has been performed, so set it to false until
		//the user clicks again
		mouseClicked = false;
	}
	
	/**
	 * selects grid locations using the bounds of the graph's rubberband
	 * 
	 * @param g
	 */
	private void selectGridLocationsWithRubberband(Graphics g) {
		
		rubberbandBounds.y += verticalOffset;
		
		//loop through all of the grid locations
		//if its rectangle is contained within the rubberband, select it
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				if (rubberbandBounds.contains(grid.get(row).get(col).getZoomedRectangle()))
					grid.get(row).get(col).setSelected(true);
			}
		}
		
		mouseReleased = false;
		rubberbandBounds = null;
		mouseClicked = false;
	}	
	
	
	
	//BORING GET/SET METHODS
	
	/**
	 * @param enabled whether or not the grid is enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return whether or not the grid is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * used for when there's a toolbar
	 * @return the number of pixels from the top of the jpanel
	 */
	public int getVerticalOffset() {
		return verticalOffset;
	}

	/**
	 * used for when there's a toolbar
	 * @param verticalOffset pixels from the top of the jpanel
	 */
	public void setVerticalOffset(int verticalOffset) {
		
		this.verticalOffset = verticalOffset;
		
		//change the grid's rectangle locations based on this offset
		updateGridRectangles();
		updateRectToNodeMap();
	}

	/**
	 * @return the numRows
	 */
	public int getNumRows() {
		return numRows;
	}

	/**
	 * @return the numCols
	 */
	public int getNumCols() {
		return numCols;
	}
	
	/**
	 * @param mouseClickLocation the mouseClickLocation to set
	 */
	public void setMouseClickLocation(Point mouseClickLocation) {
		
		this.mouseClickLocation = new Point(mouseClickLocation.x, mouseClickLocation.y + verticalOffset);
		mouseClicked = true;
	}

	/**
	 * @return the mouseClickLocation
	 */
	public Point getMouseClickLocation() {
		return mouseClickLocation;
	}

	/**
	 * @param mouseLocation the mouseLocation to set
	 */
	public void setMouseLocation(Point mouseLocation) {
		
		//the mouse location is changed because of the vertical offset
		this.mouseLocation = new Point(mouseLocation.x, mouseLocation.y + verticalOffset);
	}

	/**
	 * @return the mouseLocation
	 */
	public Point getMouseLocation() {
		return mouseLocation;
	}
	
	/**
	 * @return padding pixels between grid components
	 */
	public double getPadding() {
		return padding;
	}

	/**
	 * @param scrollOffset the scrollOffset to set
	 */
	public void setScrollOffset(Point scrollOffset) {
		this.scrollOffset = scrollOffset;
	}
	
	/**
	 * @return the scrollOffset
	 */
	public Point getScrollOffset() {
		return scrollOffset;
	}
	
	/**
	 * @return the gridWidth
	 */
	public double getGridWidth() {
		return gridWidth;
	}
	
	/**
	 * @param gridWidth the gridWidth to set
	 */
	public void setGridWidth(double gridWidth) {
		this.gridWidth = gridWidth;
	}
	
	/**
	 * @param componentWidth the componentWidth to set
	 */
	public void setComponentGeomWidth(double componentGeomWidth) {
		this.componentGeomWidth = componentGeomWidth;
	}
	
	/**
	 * @return the componentGeomWidth
	 */
	public double getComponentGeomWidth() {
		return componentGeomWidth;
	}
	
	/**
	 * @return the ComponentGeomHeight
	 */
	public double getComponentGeomHeight() {
		return componentGeomHeight;
	}
	
	/**
	 * @param ComponentGeomHeight the ComponentGeomHeight to set
	 */
	public void setComponentGeomHeight(double componentGeomHeight) {
		this.componentGeomHeight = componentGeomHeight;
	}
	
	/**
	 * @return the gridGeomHeight
	 */
	public double getGridGeomHeight() {
		return gridGeomHeight;
	}
	
	/**
	 * @param gridGeomHeight the gridGeomHeight to set
	 */
	public void setGridGeomHeight(double gridGeomHeight) {
		this.gridGeomHeight = gridGeomHeight;
	}
	
	/**
	 * @return the gridGeomWidth
	 */
	public double getGridGeomWidth() {
		return gridGeomWidth;
	}
	
	/**
	 * @param gridGeomWidth the gridGeomWidth to set
	 */
	public void setGridGeomWidth(double gridGeomWidth) {
		this.gridGeomWidth = gridGeomWidth;
	}
	
	/**
	 * @return the gridHeight
	 */
	public double getGridHeight() {
		return gridHeight;
	}
	
	/**
	 * @param gridHeight the gridHeight to set
	 */
	public void setGridHeight(double gridHeight) {
		this.gridHeight = gridHeight;
	}
	
	/**
	 * @param rubberbandBounds the bounds of the graph's rubberband
	 */
	public void setRubberbandBounds(Rectangle rubberbandBounds) {
		this.rubberbandBounds = rubberbandBounds;
	}
	
	/**
	 * @return the bounds of the graph's rubberband
	 */
	public Rectangle getRubberbandBounds() {
		return rubberbandBounds;
	}
	
	/**
	 * @param mouseReleased the mouseReleased state of the schematic
	 */
	public void setMouseReleased(boolean mouseReleased) {
		this.mouseReleased = mouseReleased;
	}
	
	/**
	 * @return the mouseReleased state of the schematic
	 */
	public boolean isMouseReleased() {
		return mouseReleased;
	}
	
	/**
	 * @return the location-to-component map
	 */
	public String getComponentFromLocation(Point location) {
		
		updateLocToComponentMap();
		
		return locToComponentMap.get(location);
	}
	
	/**
	 * returns the row and column of a component based on a given ID
	 * 
	 * @param compID the component ID
	 * @return the row and column of that component
	 */
	public Point getLocationFromComponentID(String compID) {
		
		return new Point(getNodeFromCompID(compID).getRow(), getNodeFromCompID(compID).getCol());
	}
	
	public void setGraph(BioGraph graph) {
		this.graph = graph;
	}
	
	
	//--------------
	//GRIDNODE CLASS
	//--------------

	/**
	 * contains all of the information for a grid node
	 * each location on the grid is a grid node
	 */
	private class GridNode {
		
		//---------------
		//CLASS VARIABLES
		//---------------
		
		private boolean occupied; //has a component or not
		//private Map.Entry<String, Properties> component; //component in node
		private String compId;
		private int row, col; //location on the grid (not x,y coords)
		private Rectangle snapRectangle; //x,y coordinate of the top-left of the component (not grid) rectangle
		//private boolean isCompartment; //component is a compartment or not
		private boolean selected; //is the grid location selected or not
		//private boolean hover; //is the grid location being hovered over or not
		private Rectangle gridRectangle; //contains the grid coordinates and size
		
		//-------------
		//CLASS METHODS
		//-------------
		
		/**
		 * default constructor that initializes variables
		 */
		public GridNode() {
			
			occupied = false;
			//isCompartment = false;
			compId = null;
			selected = false;
			//hover = false;
			snapRectangle = new Rectangle(0, 0, 0, 0);
			gridRectangle = new Rectangle(0, 0, 0, 0);
		}
		

		//PUBLIC
		
		/**
		 * deletes the node's component, freeing it to be re-set to another
		 */
		public void clear() {
			
			occupied = false;
			compId = null;
			//isCompartment = false;
		}
		
		
		//BORING GET/SET METHODS
		
		/**
		 * 
		 * @return the occupancy status
		 */
		public boolean isOccupied() {
			return occupied;
		}
		
		/**
		 * 
		 * @param occupancy whether gridNode is occupied or not
		 */
		public void setOccupied(boolean occupancy) {
			occupied = occupancy;
		}

		/**
		 * @param col the col to set
		 */
		public void setCol(int col) {
			this.col = col;
		}

		/**
		 * @return the col
		 */
		public int getCol() {
			return col;
		}

		/**
		 * @param row the row to set
		 */
		public void setRow(int row) {
			this.row = row;
		}

		/**
		 * @return the row
		 */
		public int getRow() {
			return row;
		}

		/**
		 * also sets boolean about whether the component is a compartment
		 * @param component the component to set
		 */
		public void setComponent(String compId) {
			
			this.compId = compId;
//			isCompartment = gcm.getSBMLComp().getListOfExternalModelDefinitions().get(
//					gcm.getSBMLCompModel().getListOfSubmodels().get(compId).getModelRef()).getAnnotation().toXMLString().contains("compartment");
		}
		
		/**
		 * @return the component
		 */
		public String getComponent() {
			return compId;
		}
	
		/**
		 * sets the geometric rectangle of the location
		 * @param rectangle the rectangle for the node
		 */
		public void setRectangle(Rectangle rectangle) {
			
			this.gridRectangle = rectangle;
			
			int xCoord = (int)(col * gridGeomWidth + padding/2);
			int yCoord = (int)(row * gridGeomHeight + padding/2);
			
			snapRectangle = new Rectangle(
					(int)(xCoord + padding/2),
					(int)(yCoord + padding/2),
					(int)componentGeomWidth,
					(int)componentGeomHeight);
		}
		
		/**
		 * returns the geometric rectangle of the location
		 * to get the actual coordinates, you need the zoomed version
		 * 
		 * @return the grid rectangle for the node
		 */
//		public Rectangle getRectangle() {
//			
//			return new Rectangle(gridRectangle.x, gridRectangle.y + verticalOffset, 
//					gridRectangle.width, gridRectangle.height);
//		}
		
		/**
		 * returns rectangle after applying zoom scaling
		 * 
		 * @return zoomed rectangle
		 */
		public Rectangle getZoomedRectangle() {
			
			return new Rectangle(
					(int)(gridRectangle.x * zoomAmount + 0.5),
					(int)(gridRectangle.y * zoomAmount + verticalOffset + 0.5),
					(int)(gridRectangle.width * zoomAmount + 0.5),
					(int)(gridRectangle.height * zoomAmount + 0.5));
			
		}
	
		/**
		 * pair of points specifying which location the component should "snap" to
		 * ie, a component is only allowed to have its top-left point at this snap point
		 * 
		 * used by the schematic to move cells around the graph
		 * 
		 * this is a geometric rectangle; to get actual coordinates, you need the zoomed rectangle
		 * 
		 * @param x
		 * @param y
		 */
//		public void setSnapRectangle(Rectangle r) {
//			
//			snapRectangle = r;
//		}
		
		/**
		 * used by the schematic to move cells around the graph
		 * 
		 * @return the snap rectangle
		 */
		public Rectangle getSnapRectangle() {
			
			return snapRectangle;
		}
		
		/**
		 * returns the snap rectangle after applying a zoom scaling
		 * @return the zoomed snap rectangle
		 */
		public Rectangle getZoomedSnapRectangle() {
			
			return new Rectangle(
					(int)(snapRectangle.x * zoomAmount),
					(int)(snapRectangle.y * zoomAmount),
					(int)(snapRectangle.width * zoomAmount),
					(int)(snapRectangle.height * zoomAmount));
		}
		
		/**
		 * @return the isCompartment
		 */
//		public boolean isCompartment() {
//			return isCompartment;
//		}
//
//		/**
//		 * @param isCompartment the isCompartment to set
//		 */
//		public void setCompartment(boolean isCompartment) {
//			this.isCompartment = isCompartment;
//		}

		/**
		 * @param selected the selected status
		 */
		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		/**
		 * @return the selected status
		 */
		public boolean isSelected() {
			return selected;
		}
		
	}

}
