package gcm.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import gcm.network.DiffusibleSpecies;
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
 * turn every component into a compartment automatically?
 * 		the grid is only for cellular populations, right?  so everything must be a cell/compartment?
 * 		you'll have to set the gcm that each component contains to be a compartment
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
	
	//components to go on the grid
	private HashMap<String, Properties> components;
	
	private int numRows, numCols;

	private int verticalOffset;
	private boolean enabled;
		
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
		
		setEnabled(false);
		verticalOffset = 0;
		numRows = 0;
		numCols = 0;
		
		grid = new ArrayList<ArrayList<GridNode>>();
	}
	
	/**
	 * creates a 2d arraylist of GridNodes of rows x cols
	 * usually called from applyComponents in DropComponentPanel
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
		createRectangleNodeHashMap();
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
			else return;
		}
		
		createGrid(maxRow, maxCol, components);
	}
	
	/**
	 * draws the grid
	 * usually called from Schematic
	 * 
	 * @param g Graphics object (from the Schematic panel)
	 */
	public void drawGrid(Graphics g) {
		
		updateGridRectangles();
		
		Graphics2D g2 = (Graphics2D) g;
		
		for(int i = 0; i < numRows; ++i) {	
			for(int j = 0; j < numCols; ++j) {
				
				Rectangle rect = grid.get(i).get(j).getRectangle();
				
				g2.drawRect(rect.x, rect.y, rect.width, rect.height);
				
				//System.out.println(grid.get(i).get(j).getComponent());
				
				//for debugging
//				g2.drawString(grid.get(i).get(j).getComponent().getValue().getProperty("graphx"), rect.x, rect.y);	
//				g2.drawString(grid.get(i).get(j).getComponent().getValue().getProperty("graphy"), rect.x+40, rect.y);
//				g2.drawString(grid.get(i).get(j).getComponent().getKey(), rect.x, rect.y+30);
			}
		}
	}
	
	//PRIVATE
	
	/**
	 * sets the rectangles/bounds for each node
	 */
	private void updateGridRectangles() {
		
		int currX = 10;
		int currY = 10 + verticalOffset;
		int padding = 20;
		int gridWidth = GlobalConstants.DEFAULT_COMPONENT_WIDTH + padding;
		int gridHeight = GlobalConstants.DEFAULT_COMPONENT_HEIGHT + padding;
		
		//create 2d arraylist of GridNode objects
		//give them a location and rectangle bounds
		for(int i = 0; i < numRows; ++i) {
			for(int j = 0; j < numCols; ++j) {
								
				Rectangle rect = new Rectangle(currX, currY, gridWidth, gridHeight);
				
				grid.get(i).get(j).setRectangle(rect);
				
				currX += gridWidth;
			}
			
			currY += gridHeight;
			currX = 10;
		}
	}
	
	/**
	 * takes the component list and uses their row/col properties to
	 * place them onto the grid
	 * 
	 * i think this should only be run once on grid creation
	 */
	private void putComponentsOntoGrid() {

		//make sure they're in exactly the right place
		//or, if they're close, "snap" them to the right place on the grid
		//^^the snapping needs to happen elsewhere, probably

		Iterator<Map.Entry<String, Properties>> iter = components.entrySet().iterator();
		
		//iterate through the components to get the x/y coords of its top left vertex
		//using these coords, map it to the grid
		while(iter.hasNext()) {
			
			Map.Entry<String, Properties> entry = (Map.Entry<String, Properties>)iter.next();
			
			//find the row and col from the component's properties
			//use these to put the component into the correct grid location
			Properties props = entry.getValue();
			
			int row = Integer.valueOf(props.getProperty("row"));
			int col = Integer.valueOf(props.getProperty("col"));
			
			//System.out.println((row-1) + " " + (col-1));
			
			grid.get(row-1).get(col-1).setComponent(entry);
		}
		
	}

	/**
	 * creates a hash map of corresponding rectangles and nodes
	 * this allows easy access of nodes from rectangle coordinates
	 */
	private void createRectangleNodeHashMap() {
		
		rectToNodeMap = new HashMap<Rectangle, GridNode>();
		
		for(int i = 0; i < numRows; ++i) {
			for(int j = 0; j < numCols; ++j) {
		
				rectToNodeMap.put(grid.get(i).get(j).getRectangle(), grid.get(i).get(j));
			}
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
			setComponent(null);
			
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
		 * @param component the component to set
		 */
		public void setComponent(Map.Entry<String, Properties> component) {
			this.component = component;
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
	}
}
