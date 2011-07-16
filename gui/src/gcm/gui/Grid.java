package gcm.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import gcm.util.GlobalConstants;

/*
 * TODO-jstev:
 *  
 * on mouse hover, outline in that green color (for selection)
 * 
 * put the verticalOffset behind the scenes so that componentx,y - 10 is accurate
 * 
 * put stuff in the listeners to update the grid's components and appearance
 * 		and also disallow moving outside of the grid and > 1 component per location and so on
 * 			create a new rectangle with the bounds and use the contains method
 * 		eg, for deletion, take the component and use a map to get the gridnode and then delete its component
 * 
 * undo stuff (ie, get undo-redo to work)
 * 
 * make sure this won't break if there's an empty spot when loading from a file or otherwise
 * 		(ie, in createGrid)
 * 
 * you shouldn't be able to move cells in the schematic during analysis view
 * 		perhaps a toggle to know that it's analysis view? (paramsOnly is this toggle, i think)
 * 
 */


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
	
	//map of x,y points and the grid they're located in
	private HashMap<Point, GridNode> pointToNodeMap;
	
	//components to go on the grid
	private HashMap<String, Properties> components;
	
	//map of x,y grid coords to the component at that location
	private HashMap<Point, Map.Entry<String, Properties>> locToComponentMap;
	
	private int numRows, numCols;
	private int verticalOffset;
	private boolean enabled;
	private boolean mouseClicked;
	private Rectangle gridBounds;
	private Point mouseClickLocation;
	private Point mouseLocation;
	
	//-------------
	//CLASS METHODS
	//-------------
	
	//PUBLIC
	
	/**
	 * constructor to create a row x col grid of GridNodes
	 */
	public Grid(int rows, int cols, HashMap<String, Properties> components) {
				
		this();
		
		createGrid(rows, cols, components);
	}
	
	/**
	 * default constructor
	 */
	public Grid() {
		
		enabled = false;
		verticalOffset = 0;
		numRows = 0;
		numCols = 0;
		
		gridBounds = new Rectangle();
		mouseClickLocation = new Point();
		mouseLocation = new Point();
		grid = new ArrayList<ArrayList<GridNode>>();
		rectToNodeMap = new HashMap<Rectangle, GridNode>();
		pointToNodeMap = new HashMap<Point, GridNode>();
		locToComponentMap = new HashMap<Point, Map.Entry<String, Properties>>();
	}
	
	/**
	 * creates a 2d arraylist of GridNodes of rows x cols
	 * usually called from GridPanel
	 * 
	 * @param rows number of rows in the grid
	 * @param cols number of columns in the grid
	 * @param components the components that are located on the grid
	 */
	public void createGrid(int rows, int cols, HashMap<String, Properties> components) {
		
		enabled = true;
		numRows = rows;
		numCols = cols;
		
		this.components = components;
		
		for(int i = 0; i < numRows; ++i) {
			
			grid.add(new ArrayList<GridNode>(numCols));
			
			for(int j = 0; j < numCols; ++j) {
				
				grid.get(i).add(new GridNode());
				grid.get(i).get(j).setRow(i);
				grid.get(i).get(j).setCol(j);
				grid.get(i).get(j).setOccupied(true);
			}
		}
		
		updateGridRectangles();
		updateNodeMaps();
		updateLocToComponentMap();
		putComponentsOntoGrid();
	}
	
	/**
	 * assumes that the components have the row and col properties
	 * which are then used to create a grid
	 * 
	 * this is called from GCMFile in loadFromBuffer, which happens
	 * when loading a gcm from a file
	 * 
	 * @param components
	 */
	public void createGrid(HashMap<String, Properties> components) {
		
		//find the max row and col elements, then call createGrid
		
		int maxRow = 0;
		int maxCol = 0;
		
		Iterator<Map.Entry<String, Properties>> iter = components.entrySet().iterator();
				
		//if there aren't any components, don't make a grid
		if (components.size() == 0) {
			
			enabled = false;
			return;
		}
		
		//iterate through the components to get the number of rows and cols
		//this is done by finding the maximum row and col numbers
		while(iter.hasNext()) {
			
			Map.Entry<String, Properties> entry = (Map.Entry<String, Properties>)iter.next();
			
			//find the row and col from the component's properties
			Properties props = entry.getValue();
						
			String rowProp = props.getProperty("row");
			String colProp = props.getProperty("col");
			
			if (rowProp != null && colProp != null) {
			
				if (Integer.valueOf(rowProp) > maxRow)
					maxRow = Integer.valueOf(rowProp);
				
				if (Integer.valueOf(colProp) > maxCol)
					maxCol = Integer.valueOf(colProp);
			
			}
			//if components have no row/col properties, then don't create a grid
			else {
				
				enabled = false;
				return;
			}
		}
		
		createGrid(maxRow, maxCol, components);
	}
	
	/**
	 * draws the grid
	 * usually called from Schematic
	 * 
	 * @param g Graphics object (from the Schematic JPanel)
	 */
	public void drawGrid(Graphics g) {
		
		Graphics2D g2 = (Graphics2D) g;
		
		boolean selectionOff = false;
		
		//if the user's mouse is within the grid bounds
		if (gridBounds.contains(mouseLocation)) {
			
			//draw a hover-rectangle over the grid location
			hoverGridLocation(g);
			
			//if the user has clicked, select/de-select that location
			if (mouseClicked) selectGridLocation(g);
		}
		//if the user clicks out of bounds
		//de-select all grid locations
		else {
			if (mouseClicked) selectionOff = true;
		}
		
		//draw the actual grid and selection boxes for selected nodes
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				
				GridNode node = grid.get(row).get(col);
				Rectangle rect = node.getRectangle();
				
				g2.drawRect(rect.x, rect.y, rect.width, rect.height);
				
				if (selectionOff) node.setSelected(false);
				
				if (node.isSelected())
					drawGridSelectionBox(g, rect);
				
				//g2.drawString(Boolean.toString(node.isSelected()), rect.x, rect.y);
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
		
		g.setColor(Color.blue);
		
		//don't change the original rectangle; make a copy
		Rectangle localRect = new Rectangle(rect);
		
		//have to loop to get a thickness > 1
		for (int i = 0; i < 3; ++i) {
			
			localRect.x += 1; localRect.y += 1; localRect.width -= 2; localRect.height -= 2;
			g.drawRect(localRect.x, localRect.y, localRect.width, localRect.height);
		}
		
		g.setColor(Color.black);
	}	
	
	/**
	 * @return the location-to-component map
	 */
	public HashMap<Point, Map.Entry<String, Properties>> getLocToComponentMap() {
		
		updateLocToComponentMap();
		
		return locToComponentMap;
	}
	
	
	//PRIVATE
	
	/**
	 * sets the rectangles/bounds for each node
	 * also sets the bounds for the entire grid
	 */
	private void updateGridRectangles() {
		
		int padding = 30;
		int start = padding/2;
		int currX = start;
		int currY = start + verticalOffset;
		int gridWidth = GlobalConstants.DEFAULT_COMPONENT_WIDTH + padding;
		int gridHeight = GlobalConstants.DEFAULT_COMPONENT_HEIGHT + padding;
		
		//create 2d arraylist of GridNode objects
		//give them a location and rectangle bounds
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
								
				Rectangle rect = new Rectangle(currX, currY, gridWidth, gridHeight);
				
				grid.get(row).get(col).setRectangle(rect);
				
				currX += gridWidth;
			}
			
			currY += gridHeight;
			currX = start;
		}
		
		//sets the total grid bounds
		int outerX = start + gridWidth * numCols;
		int outerY = start + gridHeight * numRows + verticalOffset;
		
		gridBounds.setBounds(start, start, outerX-start, outerY-start);
	}
	
	/**
	 * takes the component list and uses their row/col properties to
	 * place them onto the grid
	 * 
	 * i think this should only be run once on grid creation
	 */
	private void putComponentsOntoGrid() {

		Iterator<Map.Entry<String, Properties>> iter = components.entrySet().iterator();
		
		//iterate through the components to get the x/y coords of its top left vertex
		//using these coords, map it to the grid
		while(iter.hasNext()) {
			
			Map.Entry<String, Properties> entry = (Map.Entry<String, Properties>)iter.next();
			
			//find the row and col from the component's properties
			//use these to put the component into the correct grid location
			Properties props = entry.getValue();
			
			String gcm = props.getProperty("gcm");
			int row = Integer.valueOf(props.getProperty("row"));
			int col = Integer.valueOf(props.getProperty("col"));
			
			//if the user selects no component, then don't put a component there
			if (gcm.equals("none"))
				grid.get(row-1).get(col-1).setOccupied(false);
			else
				grid.get(row-1).get(col-1).setComponent(entry);
		}
		
	}

	/**
	 * creates a hash map of corresponding rectangles and nodes
	 * this allows easy access of nodes from rectangle coordinates
	 * 
	 * creates a hash map of the corresponding x,y points and nodes
	 * this allows easy access to nodes from the location coordinates
	 */
	private void updateNodeMaps() {
		
		rectToNodeMap.clear();
		pointToNodeMap.clear();
		
		//loop through the rows and columns
		//find the grid node and rectangle then make it a map entry
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
		
				rectToNodeMap.put(grid.get(row).get(col).getRectangle(), grid.get(row).get(col));
			}
		}
		
		int startX = gridBounds.x;
		int startY = gridBounds.y;
		int endX = gridBounds.x + gridBounds.width;
		int endY = gridBounds.y + gridBounds.height;
		
		//loop through every point on the grid
		//find the rectangle then the grid node then make it a map entry
		for (int x = startX; x <= endX; ++x) {
			for (int y = startY; y <= endY; ++y) {
				
				boolean nextPoint = false;
				Point point = new Point(x, y);
				
				//loop through every row and column
				//check this location's rectangle against the x,y point
				//if it contains it, add that to the map
				for (int row = 0; row < numRows; ++row) {
					for (int col = 0; col < numCols; ++col) {
						
						Rectangle rect = grid.get(row).get(col).getRectangle();
						
						if (rect.contains(point)) {
							pointToNodeMap.put(point, rectToNodeMap.get(rect));
							nextPoint = true;
							continue;
						}
					}
					
					//if we've found the correct location, move on
					if (nextPoint == true) continue;
				}
			}
		}
				
//		Iterator<Map.Entry<Point, GridNode>> iter = pointToNodeMap.entrySet().iterator();
//		
//		int num = 0;
//		
//		//iterate through the components to get the number of rows and cols
//		//this is done by finding the maximum row and col numbers
//		while(iter.hasNext() && num < 100) {
//			
//			Map.Entry<Point, GridNode> entry = (Map.Entry<Point, GridNode>)iter.next();
//			
//			System.out.println(entry);
//			
//			++num;
//		}
	}
	
	/**
	 * based on the mouse location, draws a box around the grid location
	 * that the user is hovering over
	 * 
	 * @param location the mouse location x,y coordinates
	 */
	private void hoverGridLocation(Graphics g) {
		
		GridNode hoveredNode = pointToNodeMap.get(mouseLocation);
		
		if (hoveredNode != null)
			drawGridSelectionBox(g, hoveredNode.getRectangle());
	}
	
	/**
	 * based on the mouse-click location, selects the grid location
	 * also draws a selection box
	 * 
	 * @param location the mouse-click x,y coordinates
	 */
	private void selectGridLocation(Graphics g) {
		
		GridNode hoveredNode = pointToNodeMap.get(mouseClickLocation);
		
		//select or de-select the grid location
		if (hoveredNode != null)
			hoveredNode.setSelected(hoveredNode.isSelected() == true ? false : true);
		
		//this click's action has been performed, so set it to false until
		//the user clicks again
		mouseClicked = false;
	}
	
	/**
	 * creates a hash map for easy access of the component at a specified grid location
	 * with the component you can get its name and properties
	 * used for diffusion reaction printing in printDiffusion
	 */
	private void updateLocToComponentMap() {
		
		locToComponentMap.clear();
				
		Iterator<Map.Entry<String, Properties>> iter = components.entrySet().iterator();
		
		//iterate through the components to get the number of rows and cols
		while(iter.hasNext()) {
			
			Map.Entry<String, Properties> compo = (Map.Entry<String, Properties>)iter.next();
			
			//find the row and col from the component's properties
			Properties props = compo.getValue();
						
			int row = Integer.parseInt(props.getProperty("row"));
			int col = Integer.parseInt(props.getProperty("col"));
			
			locToComponentMap.put(new Point(row, col), compo);
		}
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
	 * 
	 * @return the components on the grid
	 */
	public HashMap<String, Properties> getComponents() {
		return components;
	}

	/**
	 * @param components the components on the grid
	 */
	public void setComponents(HashMap<String, Properties> components) {
		this.components = components;	
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
		updateNodeMaps();
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
		private Map.Entry<String, Properties> component; //component in node
		private int row, col; //location
		private boolean isCompartment; //is a compartment or not
		private boolean selected; //is the grid location selected or not

		//contains the grid coordinates/size
		private Rectangle gridRectangle;
		
		//-------------
		//CLASS METHODS
		//-------------
		
		/**
		 * default constructor that initializes variables
		 */
		public GridNode() {
			
			occupied = false;
			isCompartment = false;
			component = null;
			
			gridRectangle = new Rectangle();			
			gridRectangle.x = 0;
			gridRectangle.y = 0;
			gridRectangle.height = 40;
			gridRectangle.width = 80;
		}
		

		//BORING GET/SET METHODS
		
		/**
		 * 
		 * @return the occupancy status
		 */
		public boolean getOccupied() {
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
		public void setComponent(Map.Entry<String, Properties> component) {
			this.component = component;
			
			isCompartment = Boolean.getBoolean(component.getValue().getProperty("compartment"));
		}
		
		/**
		 * @return the component
		 */
		public Map.Entry<String, Properties> getComponent() {
			return component;
		}
	
		/**
		 * @param rectangle the rectangle for the node
		 */
		public void setRectangle(Rectangle rectangle) {
			this.gridRectangle = rectangle;
		}
		
		/**
		 * 
		 * @return the grid rectangle for the node
		 */
		public Rectangle getRectangle() {
			return this.gridRectangle;
		}
	
		/**
		 * @return the isCompartment
		 */
		public boolean isCompartment() {
			return isCompartment;
		}

		/**
		 * @param isCompartment the isCompartment to set
		 */
		public void setCompartment(boolean isCompartment) {
			this.isCompartment = isCompartment;
		}

		/**
		 * @param selected the selected to set
		 */
		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		/**
		 * @return the selected
		 */
		public boolean isSelected() {
			return selected;
		}
	}
	
}
