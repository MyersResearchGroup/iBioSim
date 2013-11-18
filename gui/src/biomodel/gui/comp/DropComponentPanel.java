package biomodel.gui.comp;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;

import biomodel.util.GlobalConstants;
import biomodel.util.Utility;
import biomodel.gui.schematic.ModelEditor;
import biomodel.parser.BioModel;
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
	private JCheckBox doTiling;
	private JPanel tilePanel;
	private JTextField rowsChooser;
	private JTextField columnsChooser;
	private JTextField paddingChooser;
	private JComboBox orientationsCombo;
	
	private boolean droppedComponent;
	
	private static DropComponentPanel panel;
	private ModelEditor gcm2sbml;
	private BioModel gcm;

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
			ModelEditor gcm2sbml, BioModel gcm, float mouseX, float mouseY, boolean onGrid){
		
		panel = new DropComponentPanel(gcm2sbml, gcm, onGrid);
		
		//if we're dropping onto a grid
		if (onGrid) {
			
			//if the location isn't occupied, go ahead and show the menu
			if (!gcm.getGrid().getOccupancyFromPoint(new Point((int)mouseX, (int)mouseY)))
				panel.openGridGUI(mouseX, mouseY, false);
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
	public static boolean dropSelectedComponents(ModelEditor gcm2sbml, BioModel gcm) {
		
		panel = new DropComponentPanel(gcm2sbml, gcm, true);
		
		panel.openGridGUI(0, 0, true);
		
		return panel.droppedComponent;
	}
	
	private DropComponentPanel(ModelEditor gcm2sbml, BioModel gcm, boolean onGrid){
		
		super(new BorderLayout());
		
		this.gcm2sbml = gcm2sbml;
		this.gcm = gcm;
		
		//no tiling if we're on a grid
		if (!onGrid) {
		
			JPanel tilingPanel = new JPanel(new GridLayout(1, 1));
		
			// radio button to enable tiling
			doTiling = new JCheckBox("Tile Component", false);
			doTiling.addActionListener(this);	
			tilingPanel.add(doTiling);
			
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
	 */
	private void openGridGUI(float mouseX, float mouseY, boolean selected) {
		
		//list of all available components to drop
		ArrayList<String> componentList = gcm2sbml.getComponentsList();
		
		//tell the user if there aren't any components to use
		if (componentList.size() == 0) {
			
			JOptionPane.showMessageDialog(Gui.frame,
					"There aren't any models to use as components.\n"
							+ "Create a new model or import one into the project first.",
					"No Models to Add", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//create a panel for the selection of components to add to the cells
		JPanel compPanel = new JPanel(new GridLayout(2,1));
		compPanel.add(new JLabel("Choose a model to add to the grid"));
		
		JComboBox componentChooser = new JComboBox();
		
		componentChooser = new JComboBox(componentList.toArray());
		
		//don't allow dropping of a model within itself
		String[] splitFilename = gcm.getFilename().split("/");
		String fname = splitFilename[splitFilename.length - 1].replace(".gcm",".xml");
		componentChooser.removeItem(fname);
		
		compPanel.add(componentChooser);
		this.add(compPanel, BorderLayout.NORTH);
		
		String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
		
		//String newComponentID = "";
		
		boolean error = true;
		
		while (error) {
			
			int value = JOptionPane.showOptionDialog(Gui.frame, this, "Add Component(s)",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	
			//if the user clicks okay
			//add the components to the gcm
			if (value == JOptionPane.OK_OPTION) {			
			
				//name of the component
				String component = (String)componentChooser.getSelectedItem();
				
				BioModel compGCM = new BioModel(gcm.getPath());
				
				//don't allow dropping a grid component
				if (compGCM.getGridEnabledFromFile(gcm.getPath() + File.separator + component.replace(".gcm",".xml"))) {
					
					JOptionPane.showMessageDialog(Gui.frame,
							"Dropping grid components is disallowed.\n" +
							"Please choose a different component.",
							"Cannot drop a grid component", JOptionPane.ERROR_MESSAGE);
				}
				else {
					
					error = false;
				
					//if we're adding a single component
					if (selected == false) {
						
						int row = gcm.getGrid().getRowFromPoint(new Point((int)mouseX, (int)mouseY));
						int col = gcm.getGrid().getColFromPoint(new Point((int)mouseX, (int)mouseY));
						
						//newComponentID = 
						applyGridComponent(row, col, component);
					}
					//if we're adding components to selected location(s)
					else {
						
						ArrayList<Point> selectedNodes = gcm.getGrid().getSelectedUnoccupiedNodes();
						
						//loop through all selected locations; apply the component to that location
						if (selectedNodes.size() > 0) {
							
							for (Point rowCol : selectedNodes)
								//newComponentID = 
								applyGridComponent(rowCol.x, rowCol.y, component);				
						}
					}
					
					Grid grid = gcm.getGrid();
					grid.refreshComponents();
					gcm2sbml.getSpeciesPanel().refreshSpeciesPanel(gcm);
					
					droppedComponent = true;
				}
			}
			else {
				
				this.droppedComponent = false;
				error = false;
			}
		}
	}
	
	/**
	 * puts a component into the grid and gcm
	 * 
	 * @param mouseX where the user clicked
	 * @param mouseY where the user clicked
	 * @param compGCM name of the component
	 */
	private String applyGridComponent(int row, int col, String component) {
		
		Grid grid = gcm.getGrid();
		
		double padding = 30;
		double width = grid.getComponentGeomWidth();
		double height = grid.getComponentGeomHeight();
		
		BioModel compGCMFile = new BioModel(gcm.getPath());
		compGCMFile.load(gcm.getPath() + File.separator + component);
		SBMLWriter writer = new SBMLWriter();
		String SBMLstr = null;
		try {
			SBMLstr = writer.writeSBMLToString(compGCMFile.getSBMLDocument());
		}
		catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String md5 = Utility.MD5(SBMLstr);
		return gcm.addComponent(null, component, compGCMFile.IsWithinCompartment(), compGCMFile.getCompartmentPorts(), row, col, 
				col * (width + padding) + padding, row * (height + padding) + padding,md5);
	}
	
	/**
	 * this is just the component list and it checks for if the user hits OK
	 * i have no idea why this is a separate method
	 * 
	 * @param mouseX where the user clicked
	 * @param mouseY where the user clicked
	 */
	private void openGUI(float mouseX, float mouseY){
		
		//list of all available components to drop
		ArrayList<String> componentList = gcm2sbml.getComponentsList();
		
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
		
		boolean error = true;
		
		while (error) {
		
			int value = JOptionPane.showOptionDialog(Gui.frame, this, "Add Component(s)",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	
			//if the user clicks okay
			//add the components to the gcm
			if (value == JOptionPane.OK_OPTION) {		
			
				//name of the component
				String component = (String)componentCombo.getSelectedItem();
				
				BioModel compGCM = new BioModel(gcm.getPath());
				
				//don't allow grids within a grid
				if (compGCM.getGridEnabledFromFile(gcm.getPath() + File.separator + component.replace(".gcm",".xml"))) {
					JOptionPane.showMessageDialog(Gui.frame,
							"Dropping grid components is disallowed.\n" +
							"Please choose a different component.",
							"Cannot drop a grid component", JOptionPane.ERROR_MESSAGE);
				}
				else if (!gcm2sbml.checkNoComponentLoop(gcm2sbml.getFilename(), component)) {
					JOptionPane.showMessageDialog(Gui.frame,
							"Dropping this component creates a cycle of instantiations.\n" +
							"Please choose a different component.",
							"Cannot drop a component", JOptionPane.ERROR_MESSAGE);
				} else {
					applyComponents(mouseX, mouseY);
					error = false;
					gcm.makeUndoPoint();
				}
			}
			else {
				this.droppedComponent = false;
				error = false;
			}
		}
	}
	
	/**
	 * Adds the components to the GCM file. Does -NOT- update the
	 * biograph, make an undo point, or mark anything dirty.
	 * 
	 * this is for components that aren't part of a grid
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
				
				//properties.setProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_WIDTH));
				//properties.setProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_HEIGHT));
				//properties.setProperty("graphx", String.valueOf(col * separationX + topleftX));
				//properties.setProperty("graphy", String.valueOf(row * separationY + topleftY));
				
				//find out if the component is a compartment by looking at the gcm data
				//set the property appropriately
				BioModel compGCM = new BioModel(gcm.getPath());
				compGCM.load(gcm.getPath() + File.separator + comp);
				
				if (compGCM.IsWithinCompartment()) {
					properties.setProperty("compartment","true");
				} 
				else {
					properties.setProperty("compartment","false");
				}
				SBMLWriter writer = new SBMLWriter();
				String SBMLstr = null;
				try {
					SBMLstr = writer.writeSBMLToString(compGCM.getSBMLDocument());
				}
				catch (SBMLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (XMLStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String md5 = Utility.MD5(SBMLstr);
				//String id = 
				gcm.addComponent(null, comp, compGCM.IsWithinCompartment(), compGCM.getCompartmentPorts(), -1, -1, 
						col * separationX + topleftX, row * separationY + topleftY,md5);
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
