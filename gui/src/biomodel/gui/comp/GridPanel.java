package biomodel.gui.comp;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import biomodel.gui.schematic.ModelEditor;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;


import main.Gui;

/**
 * @author jason
 *
 */
public class GridPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private BioModel gcm;
	private JComboBox componentChooser;
	private ArrayList<String> componentList;	
	
	private static boolean built;
	
	/**
	 * constructor that sets the gcm2sbmleditor and gcmfile
	 * calls the buildPanel method
	 * builds component and compartment lists
	 * 
	 * @param gcm2sbml the gui/editor
	 * @param gcm the gcm file to work with
	 */
	private GridPanel(ModelEditor gcm2sbml, BioModel gcm, boolean editMode) {
		
		//call the JPanel constructor to make this a border layout panel
		super(new BorderLayout());
		
		if (gcm2sbml == null && gcm == null && editMode == false) {
			built = buildPromptPanel() == true ? true : false;
			return;
		}

		this.gcm = gcm;
		
		//component list is the gcms that can be added to a spatial grid
		//but components that aren't compartments are ineligible for
		//being added to a cell population
		componentList = gcm2sbml.getComponentsList();
		componentList.add("none");
		
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
	public static boolean showGridPanel(ModelEditor gcm2sbml, BioModel gcm, boolean editMode) {
		
		new GridPanel(gcm2sbml, gcm, editMode);
		
		return built;
	}
	
	/**
	 * static method to create a prompting panel to delete graph before creating a grid
	 * 
	 * @return ok/cancel
	 */
	public static boolean showGridPromptPanel() {
		
		new GridPanel(null, null, false);
		
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

		tilePanel.add(new JLabel("Rows"));
		rowsChooser = new JTextField("3");
		tilePanel.add(rowsChooser);
		
		tilePanel.add(new JLabel("Columns"));
		columnsChooser = new JTextField("3");
		tilePanel.add(columnsChooser);
		
		//create a panel for the selection of components to add to the cells
		compPanel = new JPanel(new GridLayout(3, 1));
		compPanel.add(new JLabel("Choose a model to add to the grid"));
		componentChooser = new JComboBox(componentList.toArray());
		
		//don't allow dropping of a model within itself
		String[] splitFilename = gcm.getFilename().split("/");
		String fname = splitFilename[splitFilename.length - 1].replace(".gcm",".xml");
		componentChooser.removeItem(fname);
		
		compPanel.add(componentChooser);
		this.add(compPanel, BorderLayout.NORTH);
		
		String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
		
		boolean error = true;
		
		while (error) {
		
			int okCancel = JOptionPane.showOptionDialog(Gui.frame, this, "Create a Grid",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	
			//if the user clicks "ok" on the panel
			if (okCancel == JOptionPane.OK_OPTION) {			
				
				//name of the component
				String component = (String)componentChooser.getSelectedItem();
				
				BioModel compGCM = new BioModel(gcm.getPath());
				
				//don't allow dropping a grid component
				if (component != "none" && compGCM.getGridEnabledFromFile(gcm.getPath() + 
						File.separator + component.replace(".gcm",".xml"))) {
					
					JOptionPane.showMessageDialog(Gui.frame,
							"Dropping grid components is disallowed.\n" +
							"Please choose a different component.",
							"Cannot drop a grid component", JOptionPane.ERROR_MESSAGE);
					
					continue;
				}
				else {			
				
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
					
					if (rowCount < 1 || colCount < 1) {
						
						JOptionPane.showMessageDialog(Gui.frame,
								"The size must be positive",
								"Invalid size",
								JOptionPane.ERROR_MESSAGE);
						
						return false;
					}
					
					//filename of the component
					String compGCMName = (String)componentChooser.getSelectedItem();
					
					//create the grid with these components
					//these will be added to the GCM as well
					Grid grid = gcm.getGrid();
					grid.setEnabled(true);
					grid.createGrid(rowCount, colCount, gcm, compGCMName);
					
					return true;
				}
			}
			else {
				
				Grid grid = gcm.getGrid();
				grid.setEnabled(true);
				return true;
			}
		}
		
		return false;
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
		
		Grid grid = gcm.getGrid();
		
		infoPanel = new JPanel(new GridLayout(3,1));
		infoPanel.add(new JLabel("Choose a new grid size"));
		infoPanel.add(new JLabel("Note: A smaller size will result in grid truncation."));
		this.add(infoPanel, BorderLayout.NORTH);
		
		//panel that contains grid size options
		tilePanel = new JPanel(new GridLayout(3, 2));
		this.add(tilePanel, BorderLayout.CENTER);

		tilePanel.add(new JLabel("Rows"));
		rowsChooser = new JTextField(Integer.toString(grid.getNumRows()));
		tilePanel.add(rowsChooser);
		
		tilePanel.add(new JLabel("Columns"));
		columnsChooser = new JTextField(Integer.toString(grid.getNumCols()));
		tilePanel.add(columnsChooser);
		
		//create a panel for the selection of components to add to the cells
		compPanel = new JPanel(new GridLayout(2,1));
		compPanel.add(new JLabel("Populate new grid spaces with"));
		
		componentChooser = new JComboBox(componentList.toArray());
		
		//don't allow dropping of a model within itself
		String[] splitFilename = gcm.getFilename().split("/");
		String fname = splitFilename[splitFilename.length - 1].replace(".gcm",".xml");
		componentChooser.removeItem(fname);
		
		compPanel.add(componentChooser);
		this.add(compPanel, BorderLayout.SOUTH);
		
		String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
		
		boolean error = true;
		
		while (error) {
		
			int okCancel = JOptionPane.showOptionDialog(Gui.frame, this, "Edit the Grid Size",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	
			//if the user clicks "ok" on the panel
			if (okCancel == JOptionPane.OK_OPTION) {
				
				//name of the component
				String component = (String)componentChooser.getSelectedItem();
				BioModel compGCM = new BioModel(gcm.getPath());
				
				//don't allow dropping a grid component
				if (!component.equals("none") && compGCM.getGridEnabledFromFile(gcm.getPath() + 
							File.separator + component.replace(".gcm",".xml"))) {
					
					JOptionPane.showMessageDialog(Gui.frame,
						"Dropping grid components is disallowed.\n" +
						"Please choose a different component.",
						"Cannot drop a grid component", JOptionPane.ERROR_MESSAGE);
					continue;
				}
				else {		
				
					int rowCount = 0, colCount = 0;
					
					//try to get the number of rows and columns from the user
					try{
						
						rowCount = Integer.parseInt(rowsChooser.getText());
						colCount = Integer.parseInt(columnsChooser.getText());
					}
					catch (NumberFormatException e) {
						
						JOptionPane.showMessageDialog(Gui.frame,
								"A number you entered could not be parsed.",
								"Invalid number format",
								JOptionPane.ERROR_MESSAGE);
					}
					
					if ((rowCount < 1 || colCount < 1) || (rowCount < 2 && colCount < 2)) {
						
						JOptionPane.showMessageDialog(Gui.frame,
								"The size must be at least 1x2 or 2x1",
								"Invalid size",
								JOptionPane.ERROR_MESSAGE);
						
						continue;
					}
					
					//filename of the component
					String compGCMName = (String)componentChooser.getSelectedItem();
					
					//if the grid size increases, then add the new components to the GCM
					//if it decreases, delete components from the GCM (getComponents.remove(id))
					grid.changeGridSize(rowCount, colCount, compGCMName, gcm);
		
					return true;
				}
			}
			else return false;
		}
		
		return false;
	}
	
	/**
	 * builds the grid prompt panel
	 * (this isn't used anymore, but i'll leave it here for potential future use)
	 * @return ok/cancel
	 */
	private boolean buildPromptPanel() {
		
		//create a panel for the selection of components to add to the cells
		JPanel gridPanel = new JPanel(new GridLayout(1,1));
		
		JLabel prompt = new JLabel("To create a grid, the model must be empty.  Press OK to clear the model first.");
		
		gridPanel.add(prompt);
		this.add(gridPanel, BorderLayout.NORTH);
		
		String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
		
		int okCancel = JOptionPane.showOptionDialog(Gui.frame, this, "Model is not empty",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		//if the user clicks "ok" on the panel
		if (okCancel == JOptionPane.OK_OPTION) {
			
			return true;
		}
		else return false;
	}
	
	/**
	 * called when the user clicks on something
	 * in this case i only care about the spatial and cell pop radio buttons
	 */
	public void actionPerformed(ActionEvent event) {
		
	}
}
