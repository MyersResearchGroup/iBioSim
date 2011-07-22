package gcm.gui;

import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import main.Gui;

/**
 * @author jason
 *
 */
public class GridPanel extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	private GCMFile gcm;
	private JRadioButton spatial, cellPop;
	private JComboBox componentChooser;
	private ArrayList<String> componentList, compartmentList;	
	
	private static boolean built;
	private boolean gridSpatial; //true if grid is spatial; false if grid is cell population
	
	/**
	 * constructor that sets the gcm2sbmleditor and gcmfile
	 * calls the buildPanel method
	 * builds component and compartment lists
	 * 
	 * @param gcm2sbml the gui/editor
	 * @param gcm the gcm file to work with
	 */
	private GridPanel(GCM2SBMLEditor gcm2sbml, GCMFile gcm, boolean editMode) {
		
		//call the JPanel constructor to make this a border layout panel
		super(new BorderLayout());

		this.gcm = gcm;
		
		//component list is the gcms that can be added to a spatial grid
		//but components that aren't compartments are ineligible for
		//being added to a cell population
		componentList = gcm2sbml.getComponentsList();
		componentList.add("none");
		
		compartmentList = new ArrayList<String>();
		
		//find all of the comPARTments, which will be available
		//to add to the cell population
		for(String comp : gcm2sbml.getComponentsList()) {
			
			GCMFile compGCM = new GCMFile(gcm.getPath());
			compGCM.load(gcm.getPath() + File.separator + comp);
			
			if (compGCM.getIsWithinCompartment())
				compartmentList.add(comp);
		}
		
		compartmentList.add("none");
		
		//editMode is false means creating a grid
		if (!editMode)
			built = buildPanel() == true ? true : false;
		//editMode is true means editing a grid
		else
			built = buildEditPanel() == true ? true : false;
	}
	
	/**
	 * static method to create a cell population panel
	 * 
	 * @return if a population is being built or not
	 */
	public static boolean showGridPanel(GCM2SBMLEditor gcm2sbml, GCMFile gcm, boolean editMode) {
		
		new GridPanel(gcm2sbml, gcm, editMode);
		
		return built;
	}
	
	/**
	 * builds the grid creation panel
	 * 
	 * @return if the user hit ok or cancel
	 */
	private boolean buildPanel() {
		
		JPanel tilePanel;
		JPanel compPanel;
		JTextField rowsChooser;
		JTextField columnsChooser;
		
		//panel that contains grid size options
		tilePanel = new JPanel(new GridLayout(3, 2));
		this.add(tilePanel, BorderLayout.SOUTH);
		
		spatial = new JRadioButton("Spatial");
		spatial.addActionListener(this);
		spatial.setActionCommand("spatial");
		
		cellPop = new JRadioButton("Cell Pop.");
		cellPop.addActionListener(this);
		cellPop.setActionCommand("cellPop");
		cellPop.setSelected(true);
		
		tilePanel.add(spatial);
		tilePanel.add(cellPop, BorderLayout.LINE_START);

		tilePanel.add(new JLabel("Rows"));
		rowsChooser = new JTextField("3");
		tilePanel.add(rowsChooser);
		
		tilePanel.add(new JLabel("Columns"));
		columnsChooser = new JTextField("3");
		tilePanel.add(columnsChooser);
		
		//create a panel for the selection of components to add to the cells
		compPanel = new JPanel(new GridLayout(2,1));
		compPanel.add(new JLabel("Choose a model to add to the grid"));
		componentChooser = new JComboBox(compartmentList.toArray());
		compPanel.add(componentChooser);
		this.add(compPanel, BorderLayout.NORTH);
		
		String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
		
		int okCancel = JOptionPane.showOptionDialog(Gui.frame, this, "Create a Grid",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		//if the user clicks "ok" on the panel
		if (okCancel == JOptionPane.OK_OPTION) {
			
			int rowCount = 0, colCount = 0;
			
			//try to get the number of rows and columns from the user
			try{
				
				rowCount = Integer.parseInt(rowsChooser.getText());
				colCount = Integer.parseInt(columnsChooser.getText());
			}
			catch(NumberFormatException e){
				
				JOptionPane.showMessageDialog(Gui.frame,
						"A number you entered could not be parsed.",
						"Invalid number format",
						JOptionPane.ERROR_MESSAGE);
			}
			
			//filename of the component
			String compGCM = (String)componentChooser.getSelectedItem();
			
			//add components to the gcm
			setGCMComponents(rowCount, colCount, compGCM);
			
			//create the grid with these components
			Grid grid = gcm.getGrid();
			grid.createGrid(rowCount, colCount, gcm.getComponents(), gridSpatial);
			
			return true;
		}
		else return false;
	}
	
	
	/**
	 * builds the grid edit panel
	 * 
	 * @return if the user hit ok or cancel
	 */
	private boolean buildEditPanel() {
		
		JPanel tilePanel;
		JPanel compPanel;
		JPanel infoPanel;
		JTextField rowsChooser;
		JTextField columnsChooser;
		
		infoPanel = new JPanel(new GridLayout(3,1));
		infoPanel.add(new JLabel("Choose a new grid size"));
		infoPanel.add(new JLabel("Note: A small size will result in grid truncation."));
		this.add(infoPanel, BorderLayout.NORTH);
		
		//panel that contains grid size options
		tilePanel = new JPanel(new GridLayout(3, 2));
		this.add(tilePanel, BorderLayout.CENTER);

		tilePanel.add(new JLabel("Rows"));
		rowsChooser = new JTextField("3");
		tilePanel.add(rowsChooser);
		
		tilePanel.add(new JLabel("Columns"));
		columnsChooser = new JTextField("3");
		tilePanel.add(columnsChooser);
		
		//create a panel for the selection of components to add to the cells
		compPanel = new JPanel(new GridLayout(2,1));
		compPanel.add(new JLabel("Populate new grid spaces with"));
		componentChooser = new JComboBox(compartmentList.toArray());
		compPanel.add(componentChooser);
		this.add(compPanel, BorderLayout.SOUTH);
		
		String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
		
		int okCancel = JOptionPane.showOptionDialog(Gui.frame, this, "Edit the Grid Size",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		//if the user clicks "ok" on the panel
		if (okCancel == JOptionPane.OK_OPTION) {
			
			int rowCount = 0, colCount = 0;
			
			//try to get the number of rows and columns from the user
			try{
				
				rowCount = Integer.parseInt(rowsChooser.getText());
				colCount = Integer.parseInt(columnsChooser.getText());
			}
			catch(NumberFormatException e){
				
				JOptionPane.showMessageDialog(Gui.frame,
						"A number you entered could not be parsed.",
						"Invalid number format",
						JOptionPane.ERROR_MESSAGE);
			}
			
			//filename of the component
			String compGCM = (String)componentChooser.getSelectedItem();
			
			Grid grid = gcm.getGrid();
			
			//if the grid size increases, then add the new components to the GCM
			//if it decreases, delete components from the GCM (getComponents.remove(id))
			//grid.changeGridSize(rowCount, colCount, compGCM);

			
			
			return true;
		}
		else return false;
	}
	
	
	/**
	 * 
	 * 
	 * @param rows number of rows of cells/components
	 * @param cols number of columns of cells/components
	 * @param compGCM name of the component's underlying gcm file
	 */
	private void setGCMComponents(int rows, int cols, String compGCM) {
		
		float padding = gcm.getGrid().getPadding();
		float width = GlobalConstants.DEFAULT_COMPONENT_WIDTH;
		float height = GlobalConstants.DEFAULT_COMPONENT_HEIGHT;
		
		//sets properties for all of the new components
		//then adds a new component to the gcm with these properties
		for (int row=0; row < rows; ++row){
			for (int col=0; col < cols; ++col){
				
				//don't put blank components onto the grid or gcm
				if (!compGCM.equals("none")) {
					
					//make a new properties field with all of the new component's properties
					Properties properties = new Properties();
					properties.put("gcm", compGCM); //comp is the name of the gcm that the component contains
					properties.setProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_WIDTH));
					properties.setProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_HEIGHT));
					properties.setProperty("graphx", String.valueOf(col * (width + padding) + padding));
					properties.setProperty("graphy", String.valueOf(row * (height + padding) + padding));
					properties.setProperty("row", String.valueOf(row+1));
					properties.setProperty("col", String.valueOf(col+1));
					
					GCMFile compGCMFile = new GCMFile(gcm.getPath());
					compGCMFile.load(gcm.getPath() + File.separator + compGCM);
					
					//set the correct compartment status
					if (compGCMFile.getIsWithinCompartment())
						properties.setProperty("compartment","true"); 
					
					else properties.setProperty("compartment","false");
					
					gcm.addComponent(null, properties);
				}
			}
		}
	}

	/**
	 * called when the user clicks on something
	 * in this case i only care about the spatial and cell pop radio buttons
	 */
	public void actionPerformed(ActionEvent event) {
				
		if (event.getActionCommand() == "spatial") {
			
			gridSpatial = true;
			
			//only one or the other can be selected
			spatial.setSelected(true);
			cellPop.setSelected(false);
			
			//change the available components to be all components
			componentChooser.removeAllItems();
			
			for (String comp : componentList)
				componentChooser.addItem(comp);
		}		
		else if (event.getActionCommand() == "cellPop") {
			
			gridSpatial = false;
		
			//only one or the other can be selected
			spatial.setSelected(false);
			cellPop.setSelected(true);
			
			//change the available components to be compartments only
			componentChooser.removeAllItems();
			
			for (String comp : compartmentList)
				componentChooser.addItem(comp);
		}
		
	}
}
