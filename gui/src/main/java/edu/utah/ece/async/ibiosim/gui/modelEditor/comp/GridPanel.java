/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.gui.modelEditor.comp;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamException;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.modelEditor.schematic.ModelEditor;

/**
 * 
 * @author jason
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class GridPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private BioModel gcm;
	private JComboBox componentChooser;
	private ArrayList<String> componentList;	
	private ModelEditor modelEditor;
	private static boolean built;
	
	/**
	 * constructor that sets the gcm2sbmleditor and gcmfile
	 * calls the buildPanel method
	 * builds component and compartment lists
	 * 
	 * @param modelEditor the gui/editor
	 * @param bioModel the gcm file to work with
	 */
	public GridPanel(ModelEditor modelEditor, BioModel bioModel) {
		
		//call the JPanel constructor to make this a border layout panel
		super(new BorderLayout());
		
		if (modelEditor == null) {
			built = buildPromptPanel() == true ? true : false;
			return;
		}

		this.gcm = bioModel;
		this.modelEditor = modelEditor;
		//component list is the gcms that can be added to a spatial grid
		//but components that aren't compartments are ineligible for
		//being added to a cell population
		componentList = modelEditor.getComponentsList();
		componentList.add("none");
	}
	
	/**
	 * static method to create a cell population panel
	 * 
	 * @return if a population is being built or not
	 */
	public boolean showGridPanel(boolean editMode) {
		
		//editMode is false means creating a grid
		if (!editMode)
			built = buildPanel() == true ? true : false;
		//editMode is true means editing a grid
		else
			built = buildEditPanel() == true ? true : false;		
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
				try {
          if (component != "none" && compGCM.getGridEnabledFromFile(gcm.getPath() + 
          		File.separator + component.replace(".gcm",".xml"))) {
          	
          	JOptionPane.showMessageDialog(Gui.frame,
          			"Dropping grid modules is disallowed.\n" +
          			"Please choose a different module.",
          			"Cannot drop a grid module", JOptionPane.ERROR_MESSAGE);
          	
          	continue;
          }
        } catch (HeadlessException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        } catch (XMLStreamException e) {
          JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        } catch (IOException e) {
          JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }
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
				Grid grid = modelEditor.getGrid();
				gcm.getGridTable().setNumRows(rowCount);
				gcm.getGridTable().setNumCols(colCount);
				grid.createGrid(compGCMName);
				
				return true;
			} 
			return false;
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
		
		Grid grid = modelEditor.getGrid();
		
		infoPanel = new JPanel(new GridLayout(3,1));
		infoPanel.add(new JLabel("Choose a new grid size"));
		infoPanel.add(new JLabel("Note: A smaller size will result in grid truncation."));
		this.add(infoPanel, BorderLayout.NORTH);
		
		//panel that contains grid size options
		tilePanel = new JPanel(new GridLayout(3, 2));
		this.add(tilePanel, BorderLayout.CENTER);

		tilePanel.add(new JLabel("Rows"));
		rowsChooser = new JTextField(Integer.toString(gcm.getGridTable().getNumRows()));
		tilePanel.add(rowsChooser);
		
		tilePanel.add(new JLabel("Columns"));
		columnsChooser = new JTextField(Integer.toString(gcm.getGridTable().getNumCols()));
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
				try {
          if (!component.equals("none") && compGCM.getGridEnabledFromFile(gcm.getPath() + 
          			File.separator + component.replace(".gcm",".xml"))) {
          	
          	JOptionPane.showMessageDialog(Gui.frame,
          		"Dropping grid modules is disallowed.\n" +
          		"Please choose a different module.",
          		"Cannot drop a grid module", JOptionPane.ERROR_MESSAGE);
          	continue;
          }
        } catch (HeadlessException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        } catch (XMLStreamException e) {
          JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        } catch (IOException e) {
          JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }
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
			return false;
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
		return false;
	}
	
	/**
	 * called when the user clicks on something
	 * in this case i only care about the spatial and cell pop radio buttons
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		
	}
}
