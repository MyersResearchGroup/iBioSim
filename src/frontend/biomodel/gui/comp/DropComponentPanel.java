package frontend.biomodel.gui.comp;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamException;

import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.util.Utility;
import dataModels.util.GlobalConstants;
import frontend.biomodel.gui.schematic.ModelEditor;
import frontend.main.Gui;

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
	private BioModel bioModel;

	/**
	 * sets up the component dropping
	 * @param gcm2sbml
	 * @param bioModel
	 * @param mouseX where to drop the component(s)
	 * @param mouseY where to drop the component(s)
	 * @param onGrid if we're dropping onto a grid
	 * @return true/false is the dropping occurred or not
	 */
	public static boolean dropComponent(
			ModelEditor gcm2sbml, BioModel bioModel, float mouseX, float mouseY, boolean onGrid){
		
		panel = new DropComponentPanel(gcm2sbml, bioModel, onGrid);
		
		//if we're dropping onto a grid
		if (onGrid) {
			
			//if the location isn't occupied, go ahead and show the menu
			if (!gcm2sbml.getGrid().getOccupancyFromPoint(new Point((int)mouseX, (int)mouseY)))
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
	 * @param bioModel
	 * @return whether the user clicked ok/cancel
	 */
	public static boolean dropSelectedComponents(ModelEditor gcm2sbml, BioModel bioModel) {
		
		panel = new DropComponentPanel(gcm2sbml, bioModel, true);
		
		panel.openGridGUI(0, 0, true);
		
		return panel.droppedComponent;
	}
	
	private DropComponentPanel(ModelEditor gcm2sbml, BioModel bioModel, boolean onGrid){
		
		super(new BorderLayout());
		
		this.gcm2sbml = gcm2sbml;
		this.bioModel = bioModel;
		
		//no tiling if we're on a grid
		if (!onGrid) {
		
			JPanel tilingPanel = new JPanel(new GridLayout(1, 1));
		
			// radio button to enable tiling
			doTiling = new JCheckBox("Tile Modules", false);
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
					"There aren't any models to use as modules.\n"
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
		String[] splitFilename = bioModel.getFilename().split("/");
		String fname = splitFilename[splitFilename.length - 1].replace(".gcm",".xml");
		componentChooser.removeItem(fname);
		
		compPanel.add(componentChooser);
		this.add(compPanel, BorderLayout.NORTH);
		
		String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
		
		//String newComponentID = "";
		
		boolean error = true;
		
		while (error) {
			
			int value = JOptionPane.showOptionDialog(Gui.frame, this, "Add Module(s)",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	
			//if the user clicks okay
			//add the components to the model
			if (value == JOptionPane.OK_OPTION) {			
			
				//name of the component
				String component = (String)componentChooser.getSelectedItem();
				
				BioModel compGCM = new BioModel(bioModel.getPath());
				
				//don't allow dropping a grid component
				try {
          if (compGCM.getGridEnabledFromFile(bioModel.getPath() + GlobalConstants.separator + component.replace(".gcm",".xml"))) {
          	
          	JOptionPane.showMessageDialog(Gui.frame,
          			"Dropping grid modules is disallowed.\n" +
          			"Please choose a different module.",
          			"Cannot drop a grid module", JOptionPane.ERROR_MESSAGE);
          }
          else {
          	
          	error = false;
          
          	//if we're adding a single component
          	if (selected == false) {
          		
          		int row = gcm2sbml.getGrid().getRowFromPoint(new Point((int)mouseX, (int)mouseY));
          		int col = gcm2sbml.getGrid().getColFromPoint(new Point((int)mouseX, (int)mouseY));
          		
          		//newComponentID = 
          		applyGridComponent(row, col, component);
          	}
          	//if we're adding components to selected location(s)
          	else {
          		
          		ArrayList<Point> selectedNodes = gcm2sbml.getGrid().getSelectedUnoccupiedNodes();
          		
          		//loop through all selected locations; apply the component to that location
          		if (selectedNodes.size() > 0) {
          			
          			for (Point rowCol : selectedNodes)
          				//newComponentID = 
          				applyGridComponent(rowCol.x, rowCol.y, component);				
          		}
          	}
          	
          	Grid grid = gcm2sbml.getGrid();
          	grid.refreshComponents();
          	gcm2sbml.getSpeciesPanel().refreshSpeciesPanel(bioModel);
          	
          	droppedComponent = true;
          }
        } catch (HeadlessException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (XMLStreamException e) {
          JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        } catch (IOException e) {
          JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }
			}
			else {
				
				this.droppedComponent = false;
				error = false;
			}
		}
	}
	
	/**
	 * puts a component into the grid and model
	 * 
	 * @param mouseX where the user clicked
	 * @param mouseY where the user clicked
	 * @param compGCM name of the component
	 */
	private String applyGridComponent(int row, int col, String component) {
		
		Grid grid = gcm2sbml.getGrid();
		
		double padding = 30;
		double width = grid.getComponentGeomWidth();
		double height = grid.getComponentGeomHeight();
		
		BioModel compGCMFile = new BioModel(bioModel.getPath());
		try {
      compGCMFile.load(bioModel.getPath() + GlobalConstants.separator + component);
	  } catch (XMLStreamException e) {
      JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
		String md5 = Utility.MD5(compGCMFile.getSBMLDocument());
		return bioModel.addComponent(null, component, compGCMFile.IsWithinCompartment(), compGCMFile.getCompartmentPorts(), row, col, 
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
					"There aren't any other models to use as modules."
							+ "\nCreate a new model or import a model into the project first.",
					"No Models in the Project", JOptionPane.ERROR_MESSAGE);
			return;
		}
 
		//this is the box where the user selects the component model to add
		componentCombo = new JComboBox(componentList.toArray());
		componentCombo.setSelectedItem(componentList.get(0));
		this.add(componentCombo, BorderLayout.NORTH);
		
		String[] options = { GlobalConstants.OK, GlobalConstants.CANCEL };
		
		boolean error = true;
		
		while (error) {
		
			int value = JOptionPane.showOptionDialog(Gui.frame, this, "Add Module(s)",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	
			//if the user clicks okay
			//add the components to the model
			if (value == JOptionPane.OK_OPTION) {		
			
				//name of the component
				String component = (String)componentCombo.getSelectedItem();
				
				BioModel compGCM = new BioModel(bioModel.getPath());
				
				//don't allow grids within a grid
				try {
          if (compGCM.getGridEnabledFromFile(bioModel.getPath() + GlobalConstants.separator + component.replace(".gcm",".xml"))) {
          	JOptionPane.showMessageDialog(Gui.frame,
          			"Dropping grid modules is disallowed.\n" +
          			"Please choose a different module.",
          			"Cannot drop a grid module", JOptionPane.ERROR_MESSAGE);
          }
          else if (!gcm2sbml.checkNoComponentLoop(gcm2sbml.getFilename(), component)) {
          	JOptionPane.showMessageDialog(Gui.frame,
          			"Dropping this module creates a cycle of instantiations.\n" +
          			"Please choose a different module.",
          			"Cannot drop a module", JOptionPane.ERROR_MESSAGE);
          } else {
          	applyComponents(mouseX, mouseY);
          	error = false;
          	gcm2sbml.makeUndoPoint();
          }
        } catch (HeadlessException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (XMLStreamException e) {
          JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        } catch (IOException e) {
          JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }
			}
			else {
				this.droppedComponent = false;
				error = false;
			}
		}
	}
	
	/**
	 * Adds the components to the Model file. Does -NOT- update the
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
				BioModel compBioModel = new BioModel(bioModel.getPath());
				try {
          compBioModel.load(bioModel.getPath() + GlobalConstants.separator + comp);
        } catch (XMLStreamException e) {
          JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        } catch (IOException e) {
          JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }
				String md5 = Utility.MD5(compBioModel.getSBMLDocument());
				bioModel.addComponent(null, comp, compBioModel.IsWithinCompartment(), compBioModel.getCompartmentPorts(), -1, -1, 
						col * separationX + topleftX, row * separationY + topleftY,md5);
			}
		}
		
		this.droppedComponent = true;
	}
	
	@Override
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
