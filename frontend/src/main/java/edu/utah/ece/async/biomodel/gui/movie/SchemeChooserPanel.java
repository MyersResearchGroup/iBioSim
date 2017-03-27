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
package edu.utah.ece.async.biomodel.gui.movie;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.utah.ece.async.biomodel.gui.movie.MovieScheme.Scheme;
import edu.utah.ece.async.main.Gui;
import edu.utah.ece.async.util.GlobalConstants;

/**
 * this is the panel that pops up in the movie schematic
 * it allows you to change color gradients for things
 * 
 * @author jason (sarbruis@gmail.com)
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SchemeChooserPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	//CLASS VARIABLES
	
	public static boolean changed;
	
	private JPanel optionsPanel;	
	private JTextField minChooser;
	private JTextField maxChooser;
	private JComboBox speciesChooser;
	private JComboBox colorChooser;
	private JComboBox sizeChooser;
	private JComboBox opacityChooser;
	private JComboBox applyToChooser;
	
	//these color arrays need to remain synchronized
	//because you can't get the color name from the RGB data	
	private Color[] colorsArray;
	private String[] colorNamesArray;
	private String[] sizeOptions = {"none", "enabled"};
	private String[] opacityOptions = {"none", "enabled"};
	private String[] applyToComponentOptions = {"this component only", "all components with this model"};
	private String[] applyToGridOptions = {"this location only", "all grid locations"};
	private String[] applyToSpeciesOptions = {"this species only", "all species"};
	private String cellType;
	private Color startColor; //the end of the gradient opposite the actual color
	
	private MovieContainer movieContainer;
	private MovieScheme movieScheme;
	
	//has every species within the TSD
	private ArrayList<String> allSpecies;
	
	//has every species within the particular cell (eg, component or grid location)
	private ArrayList<String> cellSpecies;
	private HashMap<String, String> cellSpeciesAppearanceIndicators;
	
	//ID of the cell that was clicked on
	private String cellID;
	
	private boolean inTab;

	
	//CLASS METHODS

	/**
	 * constructor
	 */
	public SchemeChooserPanel(String cellID, MovieContainer movieContainer, boolean inTab) {
		
		//call the JPanel constructor to make this a border layout panel
		super(new BorderLayout());
		
		cellSpecies = new ArrayList<String>();
		cellSpeciesAppearanceIndicators = new HashMap<String, String>();
		
		this.inTab = inTab;
		this.cellType = GlobalConstants.COMPONENT;
		this.cellID = cellID;
		this.movieContainer = movieContainer;
		this.movieScheme = movieContainer.getMovieScheme();
		this.startColor = Color.black;
		this.allSpecies = new ArrayList<String>();
		
		if (movieContainer.getDynamic() == true)
			this.allSpecies.addAll(movieContainer.getDTSDParser()
					.getSpeciesToValueMap(movieContainer.getFrameIndex()).keySet());
		else
			this.allSpecies = movieContainer.getTSDParser().getSpecies();
		
		this.colorsArray = new Color[] {this.getBackground(), Color.green, Color.red, Color.blue, Color.cyan, 
			Color.magenta, Color.pink, Color.yellow, Color.orange};
		
		this.colorNamesArray = new String[] {"none", "green", "red", "blue", "cyan", 
			"magenta", "pink", "yellow", "orange"};
		
		determineCellType(cellID);
		
		//if this is a species, open the panel for species
		if (cellType.equals(GlobalConstants.SPECIES)) {
			
			cellSpecies.add(cellID);
				
			changed = buildPanel() == true ? true : false;	
		}
		//this is a component or grid location
		else {
		
			//this strips off species prefixes (ie, component or grid location)
			//for species within the cellID cell
			//(eg, C4__S1 will become S1 if cellID is C4)
			for (String species : allSpecies) {
				
				//these shouldn't be included
				if (species.contains("__location") || species.equals("time"))
					continue;
				
				String[] speciesParts = species.split("__");
				
				//if there's a component or grid location prefix, strip it
				if (speciesParts != null && speciesParts.length > 1 && speciesParts[0].equals(cellID)) {
					
					String speciesIDNoPrefix = species.replace(new String(speciesParts[0] + "__"), "");
					
					if (speciesParts[0].contains(cellID)) {
						
						cellSpecies.add(speciesIDNoPrefix);
						Scheme speciesScheme = movieScheme.getSpeciesScheme(species);
						
						//finds the species appearance information and adds text indicators
						//for easily finding which species do what
						if (speciesScheme != null) {
						
							//get the scheme data for the species that was selected
							GradientPaint colorGradient = speciesScheme.getColorGradient();
							boolean opacityOption = speciesScheme.getOpacityState();
							boolean sizeOption = speciesScheme.getSizeState();
							
							String appearanceIndicators = "";
							
							if (colorGradient != null)
								appearanceIndicators += "C";
							if (opacityOption == true) {
								if (appearanceIndicators.length() > 0) 
									appearanceIndicators += ", O";
								else
									appearanceIndicators += "O";
							}
							if (sizeOption == true) {
								if (appearanceIndicators.length() > 0) 
									appearanceIndicators += ", S";
								else
									appearanceIndicators += "S";
							}
							
							if (appearanceIndicators.length() > 0)
								appearanceIndicators = "(" + appearanceIndicators + ")";
							
							cellSpeciesAppearanceIndicators.put(speciesIDNoPrefix, appearanceIndicators);
						}
					}
				}
			}
			
			//if there aren't any species to change, tell the user
			//and don't build the panel
			if (cellSpecies.size() == 0) {
				
				JOptionPane.showMessageDialog(Gui.frame,
						"There aren't any species in this " + 
							cellType.toLowerCase() + " to adjust the appearance of.\n",
						"No Species Present", JOptionPane.ERROR_MESSAGE);
				
				changed = false;
			}
			else
				changed = buildPanel() == true ? true : false;
		}
	}
	
	/**
	 * access point and static call to create a scheme chooser panel
	 * 
	 * @param cellID the cell that was clicked on
	 * @param movieContainer
	 * @return
	 */
	public static boolean showSchemeChooserPanel(String cellID, MovieContainer movieContainer) {

		new SchemeChooserPanel(cellID, movieContainer, false);
		
		return changed;
	}
	
	/**
	 * builds the color scheme chooser panel
	 * 
	 * @return whether the user clicked ok or cancel
	 */
	private boolean buildPanel() {
		
		JPanel infoPanel = new JPanel(new GridLayout(1, 1));
		infoPanel.add(new JLabel("<html>Selecting a scheme for a species will associate a color, opacity, or size gradient<br>" +
				"in proportion to the number of molecules present with time.<br><br></html>"));
		this.add(infoPanel, BorderLayout.NORTH);
		
		optionsPanel = new JPanel(new GridLayout(7, 2));
		this.add(optionsPanel, BorderLayout.CENTER);
		
		//add indicators to the species names' text
		String[] newSpeciesNames = new String[cellSpecies.size()];
		
		for (int i = 0; i < newSpeciesNames.length; ++i) {
			
			newSpeciesNames[i] = new String(cellSpecies.get(i));
			
			if (cellSpeciesAppearanceIndicators.containsKey(newSpeciesNames[i]))
				newSpeciesNames[i] += " " + cellSpeciesAppearanceIndicators.get(newSpeciesNames[i]);
		}
		
		optionsPanel.add(new JLabel("Species"));
		speciesChooser = new JComboBox(newSpeciesNames);
		speciesChooser.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				updatePanelValues();
			}
		});
		optionsPanel.add(speciesChooser);
		
		optionsPanel.add(new JLabel("Color Gradient"));
		colorChooser = new JComboBox(colorsArray);
		ComboBoxRenderer renderer = new ComboBoxRenderer();
		colorChooser.setRenderer(renderer);
		optionsPanel.add(colorChooser);
		
		optionsPanel.add(new JLabel("Size Gradient"));
		sizeChooser = new JComboBox(sizeOptions);
		
		if (cellType.equals(GlobalConstants.GRID_RECTANGLE))
			sizeChooser.setEnabled(false);
		
		optionsPanel.add(sizeChooser);
		
		optionsPanel.add(new JLabel("Opacity Gradient"));
		opacityChooser = new JComboBox(opacityOptions);
		optionsPanel.add(opacityChooser);
		
		optionsPanel.add(new JLabel("Min. number of molecules visible"));
		minChooser = new JTextField("0");
		optionsPanel.add(minChooser);
		
		optionsPanel.add(new JLabel("Saturating number of molecules"));
		maxChooser = new JTextField("20");
		optionsPanel.add(maxChooser);
		
		if (cellType.equals(GlobalConstants.GRID_RECTANGLE)) {
		
			optionsPanel.add(new JLabel("Apply to"));
			applyToChooser = new JComboBox(applyToGridOptions);
			optionsPanel.add(applyToChooser);
		}
		else if (cellType.equals(GlobalConstants.COMPONENT)) {
		
			optionsPanel.add(new JLabel("Apply to"));
			applyToChooser = new JComboBox(applyToComponentOptions);
			optionsPanel.add(applyToChooser);
		}
		else if (cellType.equals(GlobalConstants.SPECIES)) {
			
			optionsPanel.add(new JLabel("Apply to"));
			applyToChooser = new JComboBox(applyToSpeciesOptions);
			optionsPanel.add(applyToChooser);
		}
		
		//populate the panel with stored values if they exist
		updatePanelValues();
		
		if (!inTab) {
		
			String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
			
			int okCancel = JOptionPane.showOptionDialog(Gui.frame, this, "Select inner species appearance scheme",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	
			//if the user clicks "save changes" on the panel
			if (okCancel == JOptionPane.OK_OPTION) {
				
				updateMovieScheme();
				movieContainer.setIsDirty(true);
			}
		}
		
		return false;
	}
	
	/**
	 * updates the movie scheme with the selected information for this cell
	 * this happens when the user hits "okay"
	 */
	public void updateMovieScheme() {
		
		//get the data that the user selected/input
		Color selectedColor = (Color) colorChooser.getSelectedItem();
		String opacityState = opacityChooser.getSelectedItem().toString();
		String sizeState = sizeChooser.getSelectedItem().toString();
		String speciesID = cellID;
		
		if (cellType != GlobalConstants.SPECIES)
			speciesID += "__" + cellSpecies.get(speciesChooser.getSelectedIndex());
		
		String applyTo = applyToChooser.getSelectedItem().toString();
		int min = Integer.parseInt(minChooser.getText());
		int max = Integer.parseInt(maxChooser.getText());
		
    	GradientPaint selectedGradient = 
    		new GradientPaint(min, 0, startColor, max, 0, selectedColor, false);
    	
    	//if the user selected something, make a new species scheme
    	//to populate with the data
    	//note: the schemes are in a hashmap, so this will also update automatically
    	if (!selectedColor.equals(colorsArray[0]) 
    			|| !opacityState.equals(opacityOptions[0]) 
    			|| !sizeState.equals(sizeOptions[0])) {
    		
    		movieContainer.getMovieScheme().createOrUpdateSpeciesScheme(speciesID, allSpecies);
    	}
    	
    	//if a color gradient was selected
    	if (!selectedColor.equals(colorsArray[0])) {
    	
    		//the false indicates that this is an addition to the scheme map
			movieContainer.getMovieScheme().addSpeciesColorScheme(
					speciesID, selectedGradient, min, max, applyTo, movieContainer.getGCM(), cellType);
    	}
    	//if a color gradient wasn't selected (ie, "none" was selected)
    	else {
    		
    		movieContainer.getMovieScheme()
    			.removeSpeciesColorScheme(speciesID, cellType, applyTo, movieContainer.getGCM());
    	}
    	
    	//if opacity is enabled
    	if (!opacityState.equals(opacityOptions[0])) {
        	
    		//the false indicates that this is an addition to the scheme map
			movieContainer.getMovieScheme().addSpeciesOpacityScheme(
					speciesID, min, max, applyTo, movieContainer.getGCM(), cellType);
    	}
    	//if an opacity state wasn't selected (ie, "none" was selected)
    	else {
    		
    		movieContainer.getMovieScheme()
    			.removeSpeciesOpacityScheme(speciesID, cellType, applyTo, movieContainer.getGCM());
    	}
    	
    	//if size is enabled
    	if (!sizeState.equals(sizeOptions[0])) {
        	
    		//the false indicates that this is an addition to the scheme map
			movieContainer.getMovieScheme().addSpeciesSizeScheme(
					speciesID, min, max, applyTo, movieContainer.getGCM(), cellType);
    	}
    	//if a size state wasn't selected (ie, "none" was selected)
    	else {
    		
    		movieContainer.getMovieScheme()
    			.removeSpeciesSizeScheme(speciesID, cellType, applyTo, movieContainer.getGCM());
    	} 
	}
	
	/**
	 * determines whether the selected cell is a grid location, component, or species
	 * @param cellID
	 */
	private void determineCellType(String cellID) {
		
		if (allSpecies.contains(cellID))
			cellType = GlobalConstants.SPECIES;
		else if (cellID.contains("ROW"))
			cellType = GlobalConstants.GRID_RECTANGLE;
		else 
			cellType = GlobalConstants.COMPONENT;
	}
	
	/**
	 * updates the gradients, min, max, and apply to values
	 * based on which species is currently selected
	 * and which values have been saved
	 */
	private void updatePanelValues() {
		
		String speciesID = cellID;
		
		if (cellType != GlobalConstants.SPECIES)
			speciesID += "__" + cellSpecies.get(speciesChooser.getSelectedIndex());
		
		Scheme speciesScheme = movieScheme.getSpeciesScheme(speciesID);
		
		if (speciesScheme != null) {
		
			//get the scheme data for the species that was selected
			GradientPaint colorGradient = speciesScheme.getColorGradient();
			int min = speciesScheme.getMin();
			int max = speciesScheme.getMax();
			String opacityOption = speciesScheme.getOpacityState() ? opacityOptions[1] : opacityOptions[0];
			String sizeOption = speciesScheme.getSizeState() ? sizeOptions[1] : sizeOptions[0];
			
			if (colorGradient != null)
				colorChooser.setSelectedItem(colorGradient.getColor2());
			else
				colorChooser.setSelectedItem(colorsArray[0]);
			
			minChooser.setText(Integer.toString(min));
			maxChooser.setText(Integer.toString(max));
			opacityChooser.setSelectedItem(opacityOption);
			sizeChooser.setSelectedItem(sizeOption);
		}
		else {
			colorChooser.setSelectedItem(colorsArray[0]);
			opacityChooser.setSelectedItem(opacityOptions[0]);
			sizeChooser.setSelectedItem(sizeOptions[0]);
		}
		
		//re-render the color combobox so that it shows the stored item
		colorChooser.setRenderer(new ComboBoxRenderer());
	}
	
	/**
	 * not currently used
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
	}
	
	
	//COMBOBOXRENDERER CLASS
	
	/**
	 * this is used to override the default cell renderer for
	 * comboboxes so that colors can be shown in the drop-down list
	 * 
	 * this is for the gradient combobox only at the moment
	 * 
	 * @author jason
	 */
	class ComboBoxRenderer extends DefaultListCellRenderer {
	
		private static final long serialVersionUID = 1L;
		
		Color gradientColor, selectedGradientColor;
		String name, selectedName;

		/**
		 * constructor
		 */
		public ComboBoxRenderer() {
			super();
			
			gradientColor = (Color)colorChooser.getSelectedItem();
			selectedGradientColor = (Color)colorChooser.getSelectedItem();
			
			name = colorNamesArray[colorChooser.getSelectedIndex()];
			selectedName = colorNamesArray[colorChooser.getSelectedIndex()];
		}
	
		/**
		 * used to assign a gradient color based on which component
		 * the user has hovered over or clicked on
		 * this is then painted (using the paintComponent function below) when it's passed back
		 */
		@Override
		public Component getListCellRendererComponent(
		                    JList list,
		                    Object value,
		                    int index,
		                    boolean isSelected,
		                    boolean cellHasFocus) {
		    
		    //create the colors for the drop-down list
		    if (index <= colorsArray.length && index >= 0) {
		    	
		    	gradientColor = colorsArray[index];
		    	name = colorNamesArray[index];
		    }
		    else {
		    	gradientColor = selectedGradientColor;
		    	name = selectedName;
		    }
		    
		    //make sure the selected color only changes on a mouse click
		    if (isSelected && index <= colorsArray.length && index >= 0) {
		    	
		    	selectedGradientColor = colorsArray[index];
		    	selectedName = colorNamesArray[index];
		    }
		    
			setText(name);
			
		    return this;
		}
		
		/**
		 * draw the gradient for whichever item in the list
		 */
		@Override
		public void paintComponent(Graphics g) {
			
			Color endColor;
			
			if (name.equals("none"))
				endColor = colorsArray[0];
			else 
				endColor = startColor;
			
			setOpaque(false);
	        Graphics2D g2d = (Graphics2D) g;
	        Paint oldPaint = g2d.getPaint();
	        Paint newPaint = new GradientPaint(0, 0, gradientColor, getWidth(), 0, endColor, false);
	        g2d.setPaint(newPaint);
	        g2d.fillRect(0,0,getWidth(),getHeight());
	        g2d.setPaint(oldPaint);
	        super.paintComponent(g);
		}
	}
}
