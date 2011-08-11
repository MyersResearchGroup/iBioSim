package gcm.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import main.Gui;

public class DropComponentPanel extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String TOPLEFT = "Top Left";
	private static final String DROP_ON_MOUSE_CLICK = "Drop on Mouse Click";
	private static final String[] ORIENTATIONS = {TOPLEFT, DROP_ON_MOUSE_CLICK};
	
	private JComboBox componentCombo;
	private JRadioButton doTiling;
	private JPanel tilePanel;
	private JTextField rowsChooser;
	private JTextField columnsChooser;
	private JTextField paddingChooser;
	private JComboBox orientationsCombo;
	
	private boolean droppedComponent;
	
	private static DropComponentPanel panel;
	private GCM2SBMLEditor gcm2sbml;
	private GCMFile gcm;

	/**
	 * sets up the component dropping
	 * @param gcm2sbml
	 * @param gcm
	 * @param mouseX where to drop the component(s)
	 * @param mouseY where to drop the component(s)
	 * @param onGrid if we're dropping onto a grid
	 * @return true/false is the dropping occurred or not
	 */
	public static boolean dropComponent(
			GCM2SBMLEditor gcm2sbml, GCMFile gcm, float mouseX, float mouseY, boolean onGrid, boolean gridSpatial){
		
		panel = new DropComponentPanel(gcm2sbml, gcm, onGrid);
		
		//if we're dropping onto a grid
		if (onGrid) {
			
			//if the location isn't occupied, go ahead and show the menu
			if (!gcm.getGrid().getOccupancyFromPoint(new Point((int)mouseX, (int)mouseY)))
				panel.openGridGUI(mouseX, mouseY, gridSpatial, false);
			else 
				return false;
		}
		else
			panel.openGUI(mouseX, mouseY);
		
		return panel.droppedComponent;
	}
	
	/**
	 * for dropping components to selected grid locations
	 * 
	 * @param gcm2sbml
	 * @param gcm
	 * @return whether the user clicked ok/cancel
	 */
	public static boolean dropSelectedComponents(GCM2SBMLEditor gcm2sbml, GCMFile gcm, boolean gridSpatial) {
		
		panel = new DropComponentPanel(gcm2sbml, gcm, true);
		
		panel.openGridGUI(0, 0, gridSpatial, true);
		
		return panel.droppedComponent;
	}
	
	private DropComponentPanel(GCM2SBMLEditor gcm2sbml, GCMFile gcm, boolean onGrid){
		
		super(new BorderLayout());
		
		this.gcm2sbml = gcm2sbml;
		this.gcm = gcm;
		
		//no tiling if we're on a grid
		if (!onGrid) {
		
			JPanel tilingPanel = new JPanel(new GridLayout(2, 1));
		
			// radio button to enable tiling
			doTiling = new JRadioButton("Tile Component", false);
			doTiling.addActionListener(this);				
			JLabel note = new JLabel("Note: tiling does not create a grid and no diffusion will occur.");
			
			tilingPanel.add(doTiling);
			tilingPanel.add(note);
			
			this.add(tilingPanel, BorderLayout.CENTER);
		
			// panel that contains tiling options
			tilePanel = new JPanel(new GridLayout(4, 2));
			this.add(tilePanel, BorderLayout.SOUTH);
			
			JLabel l;
			
			// options that go in the tiling panel		
			l = new JLabel("Columns"); tilePanel.add(l);
			columnsChooser = new JTextField("6");
			tilePanel.add(columnsChooser);
			
			l = new JLabel("Rows"); tilePanel.add(l);
			rowsChooser = new JTextField("6");
			tilePanel.add(rowsChooser);
			
			l = new JLabel("Padding"); tilePanel.add(l);
			paddingChooser = new JTextField("20");
			tilePanel.add(paddingChooser);
			
			l = new JLabel("Orientation"); tilePanel.add(l);
			orientationsCombo = new JComboBox(ORIENTATIONS);
			orientationsCombo.setSelectedIndex(0);
			tilePanel.add(orientationsCombo);
			
			updateTilingEnabled();
		}
	}
	
	/**
	 * prompts about adding a component to a grid location
	 * 
	 * @param mouseX where the user clicked
	 * @param mouseY where the user clicked
	 * @param gridSpatial whether the grid is spatial or cell population
	 */
	private void openGridGUI(float mouseX, float mouseY, boolean gridSpatial, boolean selected) {
		
		//component list is the gcms that can be added to a spatial grid
		//but components that aren't compartments are ineligible for
		//being added to a cell population
		ArrayList<String> componentList = gcm2sbml.getComponentsList();
		
		ArrayList<String> compartmentList = new ArrayList<String>();
		ArrayList<String> gridComponents = new ArrayList<String>();
		
		//find all of the comPARTments, which will be available
		//to add to the cell population
		for(String comp : gcm2sbml.getComponentsList()) {
			
			GCMFile compGCM = new GCMFile(gcm.getPath());
			compGCM.load(gcm.getPath() + File.separator + comp);
			
			if (compGCM.getIsWithinCompartment() && !compGCM.getGrid().isEnabled())
				compartmentList.add(comp);
			
			//don't allow adding a component with a grid
			if (compGCM.getGrid().isEnabled())
				gridComponents.add(comp);
		}
		
		componentList.removeAll(gridComponents);
		
		//tell the user if there aren't any components to use
		if (componentList.size() == 0) {
			
			JOptionPane.showMessageDialog(Gui.frame,
					"There aren't any GCMs to use as components.\n"
							+ "Create a new GCM or import one into the project first.",
					"No GCMs to Add", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//if gridSpatial is false, only compartments can be added
		//so tell the user if there aren't any
		if (gridSpatial == false && compartmentList.size() == 0) {
			
			JOptionPane.showMessageDialog(Gui.frame,
					"There aren't any GCMs that are compartments.\n"
							+ "Create a new GCM that is a compartment or import one into the project first.",
					"No Compartments to Add", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//create a panel for the selection of components to add to the cells
		JPanel compPanel = new JPanel(new GridLayout(2,1));
		compPanel.add(new JLabel("Choose a gcm to add to the grid"));
		
		JComboBox componentChooser = new JComboBox();
		
		//show only compartments if it's not a spatial grid (ie, it's a cell population grid)
		if (gridSpatial)
			componentChooser = new JComboBox(componentList.toArray());
		else
			componentChooser = new JComboBox(compartmentList.toArray());
		
		compPanel.add(componentChooser);
		this.add(compPanel, BorderLayout.NORTH);
		
		String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
		
		int okCancel = JOptionPane.showOptionDialog(Gui.frame, this, "Add GCM",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		//if the user clicks "ok" on the panel
		if (okCancel == JOptionPane.OK_OPTION) {
			
			//name of the component
			String component = (String)componentChooser.getSelectedItem();
			
			if (selected == false) {
				
				int row = gcm.getGrid().getRowFromPoint(new Point((int)mouseX, (int)mouseY));
				int col = gcm.getGrid().getColFromPoint(new Point((int)mouseX, (int)mouseY));
				
				applyGridComponent(row, col, component);
			}
			//if we're adding components to selected location(s)
			else {
				
				ArrayList<Point> selectedNodes = gcm.getGrid().getSelectedUnoccupiedNodes();
				
				//loop through all selected locations; apply the component to that location
				if (selectedNodes.size() > 0) {
					
					for (Point rowCol : selectedNodes)
						applyGridComponent(rowCol.x, rowCol.y, component);				
				}
			}
			
			Grid grid = gcm.getGrid();
			grid.refreshComponents(gcm.getComponents());
			
			droppedComponent = true;
		}
	}
	
	/**
	 * puts a component into the grid and gcm
	 * 
	 * @param mouseX where the user clicked
	 * @param mouseY where the user clicked
	 * @param compGCM name of the component
	 */
	private void applyGridComponent(int row, int col, String component) {
		
		Grid grid = gcm.getGrid();
		
		double padding = 30;
		double width = grid.getComponentGeomWidth();
		double height = grid.getComponentGeomHeight();
		
		//make a new properties field with all of the new component's properties
		Properties properties = new Properties();
		properties.setProperty("gcm", component); //comp is the name of the gcm that the component contains
		properties.setProperty("graphwidth", String.valueOf(width));
		properties.setProperty("graphheight", String.valueOf(height));
		properties.setProperty("graphx", String.valueOf(col * (width + padding) + padding));
		properties.setProperty("graphy", String.valueOf(row * (height + padding) + padding));
		properties.setProperty("row", String.valueOf(row));
		properties.setProperty("col", String.valueOf(col));
		
		GCMFile compGCMFile = new GCMFile(gcm.getPath());
		compGCMFile.load(gcm.getPath() + File.separator + component);
		
		//set the correct compartment status
		if (compGCMFile.getIsWithinCompartment())
			properties.setProperty("compartment","true"); 		
		else 
			properties.setProperty("compartment","false");
		
		gcm.addComponent(null, properties);
	}
	
	/**
	 * this is just the component list and it checks for if the user hits OK
	 * i have no idea why this is a separate method
	 * 
	 * @param mouseX where the user clicked
	 * @param mouseY where the user clicked
	 */
	private void openGUI(float mouseX, float mouseY){
		
		ArrayList<String> componentList = gcm2sbml.getComponentsList();
		ArrayList<String> gridComponents = new ArrayList<String>();
		
		for (String comp : componentList) {
			
			GCMFile compGCM = new GCMFile(gcm.getPath());
			compGCM.load(gcm.getPath() + File.separator + comp);
			
			//don't allow adding a component with a grid
			if (compGCM.getGrid().isEnabled())
				gridComponents.add(comp);
		}
		
		componentList.removeAll(gridComponents);
		
		if(componentList.size() == 0){
			JOptionPane.showMessageDialog(Gui.frame,
					"There aren't any other gcms to use as components."
							+ "\nCreate a new gcm or import a gcm into the project first.",
					"Add Another GCM To The Project", JOptionPane.ERROR_MESSAGE);
			return;
		}
 
		//this is the box where the user selects the component gcm to add
		componentCombo = new JComboBox(componentList.toArray());
		componentCombo.setSelectedItem(componentList.get(0));
		this.add(componentCombo, BorderLayout.NORTH);
		
		String[] options = { GlobalConstants.OK, GlobalConstants.CANCEL };
		
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Add Component(s)",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		//if the user clicks okay
		//add the components to the gcm
		if (value == JOptionPane.OK_OPTION)
			applyComponents(mouseX, mouseY);
		else
			this.droppedComponent = false;
	}
	
	/**
	 * Adds the components to the GCM file. Does -NOT- update the
	 * biograph, make an undo point, or mark anything dirty.
	 */
	private void applyComponents(float mouseX, float mouseY){
		
		int rowCount, colCount, padding;
		String orientation;
		
		if(doTiling.isSelected()) {
			
			orientation = (String)orientationsCombo.getSelectedItem();
			
			//parse the input numbers
			try {
				
				rowCount = Integer.parseInt(rowsChooser.getText());
				colCount = Integer.parseInt(columnsChooser.getText());
				padding =  Integer.parseInt(paddingChooser.getText());
			}
			catch(NumberFormatException e) {
				
				JOptionPane.showMessageDialog(Gui.frame,
						"A number you entered could not be parsed.",
						"Invalid number format",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else {
			
			rowCount = colCount = 1;
			padding = 0;
			orientation = DROP_ON_MOUSE_CLICK;
		}

		String comp = (String)componentCombo.getSelectedItem();
				
		float separationX = GlobalConstants.DEFAULT_COMPONENT_WIDTH + padding;
		float separationY = GlobalConstants.DEFAULT_COMPONENT_HEIGHT + padding;
		float topleftX=0;
		float topleftY=0;
		
		if(orientation.equals(TOPLEFT)) {
			
			topleftX = padding;
			topleftY = padding;
		}
		else if(orientation.equals(DROP_ON_MOUSE_CLICK)) {
			
			topleftX = mouseX; // - separationX * colCount/2 + padding/2;
			topleftY = mouseY; // - separationY * rowCount/2 + padding/2;
		}
		
		//sets location(s) for all of the tiled component(s)
		for(int row=0; row<rowCount; row++) {
			for(int col=0; col<colCount; col++) {
				
				Properties properties = new Properties();
				properties.put("gcm", comp); //comp is the name of the gcm that the component contains
				properties.setProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_WIDTH));
				properties.setProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_HEIGHT));
				properties.setProperty("graphx", String.valueOf(col * separationX + topleftX));
				properties.setProperty("graphy", String.valueOf(row * separationY + topleftY));
				
				//find out if the component is a compartment by looking at the gcm data
				//set the property appropriately
				GCMFile compGCM = new GCMFile(gcm.getPath());
				compGCM.load(gcm.getPath() + File.separator + comp);
				
				if (compGCM.getIsWithinCompartment()) {
					properties.setProperty("compartment","true");
				} 
				else {
					properties.setProperty("compartment","false");
				}
				
				gcm.addComponent(null, properties);
			}
		}
		
		this.droppedComponent = true;
	}
	
	public void actionPerformed(ActionEvent e) {
		updateTilingEnabled();
	}
	
	/**
	 * updates the enabled property of all the components that are enabled and disabled with the tiling options.
	 */
	private void updateTilingEnabled(){
		boolean enabled = doTiling.isSelected();
				
		rowsChooser.setEnabled(enabled);
		columnsChooser.setEnabled(enabled);
		paddingChooser.setEnabled(enabled);
		orientationsCombo.setEnabled(enabled);
	}
	
}
