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
package edu.utah.ece.async.gui.biomodel.gui.movie;


import java.awt.Color;
import java.awt.GradientPaint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.sbml.jsbml.Parameter;

import edu.utah.ece.async.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.dataModels.util.GlobalConstants;
import edu.utah.ece.async.gui.biomodel.gui.movie.SerializableScheme;


/**
 * this class contains all of the information related to a movie appearance scheme
 * that affects all cells on the graph (ie, species or components, etc.)
 * 
 * this combines/re-works a lot of the stuff that tyler had spread out among myriad classes
 * 
 * @author jason
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class MovieScheme {

	//CLASS VARIABLES
	
	//contains the appearance data for each species
	//these have the full name, including a component or grid location prefix
	private HashMap<String, Scheme> speciesSchemes;
	
	private ArrayList<String> allSpecies;
	
	
	//CLASS METHODS
	
	/**
	 * constructor
	 */
	public MovieScheme() {
		
		speciesSchemes = new HashMap<String, Scheme>();
	}
	
	
	//SCHEME METHODS
	
	/**
	 * adds a species color scheme
	 * it's a wrapper for the applySpeciesColorScheme function
	 * 
	 * @param speciesID
	 * @param colorGradient
	 * @param min
	 * @param max
	 * @param applyTo
	 * @param gcm
	 * @param cellType
	 */
	public void addSpeciesColorScheme(String speciesID, GradientPaint colorGradient, 
			int min, int max, String applyTo, BioModel gcm, String cellType) {
				
		final GradientPaint gradient = colorGradient;
		
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

			@Override
			public void apply(String speciesID) {
				
				speciesSchemes.get(speciesID).setColorGradient(gradient);
			}
		};		
		
		applySpeciesSchemeElement(schemeApply, speciesID, min, max, applyTo, gcm, cellType, false);
	}
	
	/**
	 * removes a color scheme for the given species
	 * it's a wrapper for the applySpeciesColorScheme function
	 * 
	 * @param speciesID
	 */
	public void removeSpeciesColorScheme(String speciesID, String cellType, String applyTo, BioModel gcm) {
		
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

			@Override
			public void apply(String speciesID) {
				
				if (speciesSchemes.get(speciesID) != null)
					speciesSchemes.get(speciesID).setColorGradient(null);
			}
		};		
		
		applySpeciesSchemeElement(schemeApply, speciesID, 0, 0, applyTo, gcm, cellType, true);
	}
	
	/**
	 * adds an opacity scheme to the overall species scheme
	 * 
	 * @param speciesID
	 * @param min
	 * @param max
	 * @param applyTo
	 * @param gcm
	 * @param cellType
	 */
	public void addSpeciesOpacityScheme(String speciesID, int min, int max, 
			String applyTo, BioModel gcm, String cellType) {
		
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

			@Override
			public void apply(String speciesID) {
				
				speciesSchemes.get(speciesID).setOpacityState(true);
			}
		};		
		
		applySpeciesSchemeElement(schemeApply, speciesID, min, max, applyTo, gcm, cellType, false);
	}
	
	/**
	 * removes an opacity scheme from the overall species scheme
	 * @param speciesID
	 * @param cellType
	 * @param applyTo
	 */
	public void removeSpeciesOpacityScheme(String speciesID, String cellType, String applyTo, BioModel gcm) {
				
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

			@Override
			public void apply(String speciesID) {
				
				if (speciesSchemes.get(speciesID) != null)
					speciesSchemes.get(speciesID).setOpacityState(false);
			}
		};
		
		applySpeciesSchemeElement(schemeApply, speciesID, 0, 0, applyTo, gcm, cellType, true);
	}
	
	/**
	 * 
	 * @param speciesID
	 * @param min
	 * @param max
	 * @param applyTo
	 * @param gcm
	 * @param cellType
	 */
	public void addSpeciesSizeScheme(String speciesID, int min, int max, 
			String applyTo, BioModel gcm, String cellType) {
		
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

			@Override
			public void apply(String speciesID) {
				
				speciesSchemes.get(speciesID).setSizeState(true);
			}
		};		
		
		applySpeciesSchemeElement(schemeApply, speciesID, min, max, applyTo, gcm, cellType, false);
	}
	
	/**
	 * 
	 * @param speciesID
	 * @param cellType
	 * @param applyTo
	 * @param gcm
	 */
	public void removeSpeciesSizeScheme(String speciesID, String cellType, String applyTo, BioModel gcm) {
		
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

			@Override
			public void apply(String speciesID) {
				
				if (speciesSchemes.get(speciesID) != null)
					speciesSchemes.get(speciesID).setSizeState(false);
			}
		};
		
		applySpeciesSchemeElement(schemeApply, speciesID, 0, 0, applyTo, gcm, cellType, true);
	}
	
	/**
	 * uses anonymous functions to update the scheme object for the particular species scheme
	 * given the species ID passed in
	 * 
	 * @param schemeApply function that applies ths schem
	 * @param speciesID
	 * @param min
	 * @param max
	 * @param applyTo
	 * @param gcm
	 * @param cellType
	 * @param remove boolean representing addition or removal of the scheme
	 */
	public void applySpeciesSchemeElement(SchemeApplyFunction schemeApply, String speciesID,  
			int min, int max, String applyTo, BioModel gcm, String cellType, boolean remove) {		
		
		if (applyTo.equals("this component only")
				|| applyTo.equals("this location only") 
				|| applyTo.equals("this species only")) {
			
			if (remove) {
				schemeApply.apply(speciesID);
			}
			else {
				schemeApply.apply(speciesID);
				speciesSchemes.get(speciesID).setMin(min);
				speciesSchemes.get(speciesID).setMax(max);
			}
		}
		//if applyTo is for all cells of that type or model or whatever
		else {
			
			//take off the speciesID's component prefix
			//this is done so that other component or grid location prefixes can be added
			String[] speciesParts = speciesID.split("__");
			String speciesIDNoPrefix = speciesID.replace(new String(speciesParts[0] + "__"), "");
			//String compID = speciesParts[0];
			
			//if the user selected to change components
			if (cellType.equals(GlobalConstants.COMPONENT)) {
				
				//look through the location parameter arrays to find the correct model ref
				for (int i = 0; i < gcm.getSBMLDocument().getModel().getParameterCount(); ++i) {
					
					Parameter parameter = gcm.getSBMLDocument().getModel().getParameter(i);
					
					//if it's a location parameter
					if (parameter.getId().contains("__locations")) {
						String[] splitAnnotation = AnnotationUtility.parseArrayAnnotation(parameter);

						//loop through all components in the locations parameter array
						for (int j = 1; j < splitAnnotation.length; ++j) {

							String submodelID = splitAnnotation[j].split("=")[0].trim();

							speciesID = submodelID + "__" + speciesIDNoPrefix;

							if (submodelID.length() == 0)
								continue;

							if (remove) {
								schemeApply.apply(speciesID);
							}
							else {
								//add a scheme with this other species that's part of the same GCM
								//as the component
								this.createOrUpdateSpeciesScheme(speciesID, null);
								schemeApply.apply(speciesID);
								speciesSchemes.get(speciesID).setMin(min);
								speciesSchemes.get(speciesID).setMax(max);
							}
						}
					}				
				}
			}
			//if the user selected to change grid rectangles
			else if (cellType.equals(GlobalConstants.GRID_RECTANGLE)) {
				
				//loop through every grid location
				//add a new scheme for each grid rectangle
				for (int row = 0; row < gcm.getGridTable().getNumRows(); ++row) {
					for (int col = 0; col < gcm.getGridTable().getNumCols(); ++col) {
						
						String gridPrefix = "ROW" + row + "_COL" + col;
						
						speciesID = new String(gridPrefix + "__" + speciesIDNoPrefix);
						
						if (allSpecies != null && !allSpecies.contains(speciesID)) continue;
							
						if (remove) {	
							schemeApply.apply(speciesID);
						}
						else {
							//add a scheme with this other species at another grid location
							this.createOrUpdateSpeciesScheme(speciesID, null);
							schemeApply.apply(speciesID);
							speciesSchemes.get(speciesID).setMin(min);
							speciesSchemes.get(speciesID).setMax(max);
						}
					}
				}
			}
			//if the user selected to change species
			else if (cellType.equals(GlobalConstants.SPECIES)) {
				
				//loop through every species in the gcm
				//add/remove a scheme for that species
				for (String specID : gcm.getSpecies()) {
					
					if (remove) {
						schemeApply.apply(specID);
					}
					else {
						
						this.createOrUpdateSpeciesScheme(specID, null);
						schemeApply.apply(specID);
						speciesSchemes.get(specID).setMin(min);
						speciesSchemes.get(specID).setMax(max);
					}
				}				
			}
		}
	}
	
	/**
	 * returns the scheme corresponding to speciesID
	 * @param speciesID
	 * @return
	 */
	public Scheme getSpeciesScheme(String speciesID) {
		
		return speciesSchemes.get(speciesID);
	}
	
	/**
	 * creates a new scheme if one doesn't exist
	 * 
	 * @param speciesID
	 */
	public void createOrUpdateSpeciesScheme(String speciesID, ArrayList<String> allSpecies) {
		
		if (allSpecies != null)
			this.allSpecies = allSpecies;
		
		if (speciesSchemes.get(speciesID) == null)
			speciesSchemes.put(speciesID, new Scheme());
	}
	
	/**
	 * returns the hash map of species schemes
	 * @return the hashmap of species schemes
	 */
	public SerializableScheme[] getAllSpeciesSchemes() {
		
		SerializableScheme[] schemes = new SerializableScheme[speciesSchemes.size()];
		
		Iterator<Map.Entry<String, Scheme>>  speciesSchemesIter = speciesSchemes.entrySet().iterator();
		
		for (int index = 0; index < speciesSchemes.size(); ++index) {
			
			Map.Entry<String, Scheme> entry = speciesSchemesIter.next();
				
			schemes[index] = new SerializableScheme();
			
			if (entry.getValue().getColorGradient() == null) {
				schemes[index].startColor = 0;
				schemes[index].endColor = 0;
			}
			else {
				schemes[index].startColor = entry.getValue().getColorGradient().getColor1().getRGB();
				schemes[index].endColor = entry.getValue().getColorGradient().getColor2().getRGB();
			}
			
			schemes[index].min = entry.getValue().getMin();
			schemes[index].max = entry.getValue().getMax();
			schemes[index].opacityState = entry.getValue().getOpacityState();
			schemes[index].sizeState = entry.getValue().getSizeState();			
			schemes[index].name = entry.getKey();
		}
		
		return schemes;		
	}
	
	/**
	 * adds schemes to the movie scheme
	 * this data comes from a saved filebut
	 * 
	 * @param schemes
	 */
	public void populate(SerializableScheme[] schemes, ArrayList<String> allSpecies) {
		
		for (SerializableScheme scheme : schemes) {
			
			//make sure not to load schemes for species that no longer exist
			if (allSpecies.contains(scheme.name) == false) continue;
			
			GradientPaint gradient = null;
			
			if (!(scheme.startColor == 0 && scheme.endColor == 0)) {
				
				gradient = new GradientPaint(0.0f, 0.0f, 
						new Color(scheme.startColor), 0.0f, 50.0f, new Color(scheme.endColor));
			}
		
			Scheme speciesScheme = new Scheme(gradient, scheme.opacityState, 
					scheme.sizeState, scheme.min, scheme.max);
			
			speciesSchemes.put(scheme.name, speciesScheme);
		}	
	}
	
	
	//APPEARANCE METHODS
	
	/**
	 * returns a MovieAppearance object which has the data that the graph needs
	 * in order to change the appearance of the cell during the movie
	 * 
	 * @param ID 
	 * @param cellType
	 * @return the appearance at
	 */
	public MovieAppearance createAppearance(String cellID, String cellType, 
			HashMap<String, Double> speciesTSData, HashSet<String> componentList) {
		
		HashMap<String, Scheme> cellSchemes = getSchemesWithinCell(cellID, cellType);
		
		String oldCellID = cellID;
		
		//this denotes a dynamically created compartment
		//so take its appearance from its ancestor if the user hasn't specified the appearance explicitly
		if (cellSchemes.size() <= 0 && cellID.contains("_of_")) {
			
			//get the parent cell ID
			cellID = cellID.substring(cellID.indexOf("_of_") + 4, cellID.length());
			cellSchemes = getSchemesWithinCell(cellID, cellType);
			
			if (componentList != null) {
				
				//find the first ancestor with a scheme and use that as the scheme
				while (cellSchemes.size() <= 0) {
					
					boolean keepLooking = false;
					
					for (String ID : componentList) {
						
						if (ID.split("_of_")[0].equals(cellID)) {
							
							cellID = ID;							
							cellSchemes = getSchemesWithinCell(cellID, cellType);
							
							if (cellID.contains("_of_") && cellSchemes.size() <= 0) {
								
								cellID = cellID.substring(cellID.indexOf("_of_") + 4, cellID.length());
								keepLooking = true;
							}
							else
								keepLooking = false;
							
							break;
						}
					}
					
					if (keepLooking == false)
						break;
				}
			}
		}
		
		String newCellID = cellID;
		
		//don't use the ancestor's appearance, only its scheme
		cellID = oldCellID;
		
		if (cellType.equals(GlobalConstants.PLACE)) {
			if (speciesTSData.containsKey(cellID)) {
				double variableValue = speciesTSData.get(cellID);
				if (variableValue == 0.0) {
					return new MovieAppearance(new Color(255,255,255));
				}
				return new MovieAppearance(new Color(128,128,128));
			}
			//cellAppearance.add(getIntermediateAppearance(
				//	colorGradient, gradientValue, opacityState, sizeState, cellType));
		} else if (cellType.equals(GlobalConstants.BOOLEAN)) {
			if (speciesTSData.containsKey(cellID)) {
				double variableValue = speciesTSData.get(cellID);
				if (variableValue == 0.0) {
					return new MovieAppearance(new Color(255,255,255));
				}
				return new MovieAppearance(new Color(128,128,128));
			}
		}
		
		if (cellSchemes.size() <= 0) 
			return null;
		MovieAppearance cellAppearance = new MovieAppearance();
		
		//loop through every scheme in the cell and add them together
		//to get the final cell appearance
		for (Map.Entry<String, Scheme> cellScheme : cellSchemes.entrySet()) {
			
			String speciesID = cellScheme.getKey();
			
			//if we're using the scheme of an ancestor, use the child's species ID
			speciesID = speciesID.replace(newCellID, cellID);
			
			int min = cellScheme.getValue().getMin();
			int max = cellScheme.getValue().getMax();
			GradientPaint colorGradient = cellScheme.getValue().getColorGradient();
			boolean opacityState = cellScheme.getValue().getOpacityState();
			boolean sizeState = cellScheme.getValue().getSizeState();				
			double speciesValue = 0.0;
			
			if (speciesTSData.containsKey(speciesID))
				speciesValue = speciesTSData.get(speciesID);
			
			//how far along this value is on the gradient spectrum of min to max
			double gradientValue = ((speciesValue - min) / (max - min));
			
			//now calculate the correct appearance along the gradient to use
			cellAppearance.add(getIntermediateAppearance(
					colorGradient, gradientValue, opacityState, sizeState, cellType));
		}
		
		return cellAppearance;
	}
	
	/**
	 * finds a grid species appearance near the location passed in.  this is used for expanding
	 * grid appearances during dynamic resizing/expansion
	 * 
	 * @param row
	 * @param col
	 * @param gridSpeciesID
	 * @param speciesTSData
	 * @return the nearest grid appearance
	 */
	public MovieAppearance getNearestGridAppearance(int row, int col, 
			String gridSpeciesID, HashMap<String, Double> speciesTSData) {
		
		Scheme nearestScheme = null;
		int closestRow = 99999, closestCol = 99999;
		
		//loop through species schemes
		for (String speciesID : speciesSchemes.keySet()) {
			
			if (speciesID.contains("ROW") && speciesID.contains("COL")) {
				
				int nRow = Integer.parseInt(speciesID.split("_")[0].replace("ROW",""));
				int nCol = Integer.parseInt(speciesID.split("_")[1].split("__")[0].replace("COL",""));
				
				if ((Math.abs(nRow - row) + Math.abs(nCol - col)) < 
						Math.abs((closestRow - row) + Math.abs(closestCol - col))) {
					
					closestRow = nRow;
					closestCol = nCol;
					nearestScheme = speciesSchemes.get(speciesID);
				}
			}
		}
		
		//add the scheme
		if (nearestScheme != null) {
			speciesSchemes.put(gridSpeciesID, nearestScheme);
		}
		else
			return null;		
		
		return createAppearance(gridSpeciesID.split("__")[0], GlobalConstants.GRID_RECTANGLE, speciesTSData, null);
	}
	
	/**
	 * returns a hashmap of all of the schemes that apply to the cell passed in
	 * 
	 * @param cellID
	 * @param cellType
	 * @return
	 */
	private HashMap<String, Scheme> getSchemesWithinCell(String cellID, String cellType) {
		
		//this will store all species schemes that exist within this component
		HashMap<String, Scheme> cellSchemes = new HashMap<String, Scheme>();
		
		//loop through the species schemes
		//if any of these apply to the cell with cellID
		//then add that scheme to the map of cell schemes
		for (Map.Entry<String, Scheme> speciesScheme : speciesSchemes.entrySet()) {
			
			String compID = "";
			
			if (cellType.equals(GlobalConstants.SPECIES))
				compID = speciesScheme.getKey();
			else {
			
				//take off the speciesID's component prefix
				//this is done so that other component or grid location prefixes can be added
				String[] speciesParts = speciesScheme.getKey().split("__");
				
				//continue if there's no component prefix
				//or if the prefix is inconsistent with the cell type
				if (speciesParts.length < 2
						|| (cellType.equals(GlobalConstants.COMPONENT) && speciesParts[0].contains("ROW")))
					continue;
				
				compID = speciesParts[0];
			}
			
			//if these are equal then this scheme is for this cell
			if (compID.equals(cellID))
				cellSchemes.put(speciesScheme.getKey(), speciesScheme.getValue());		
		
		}
		
		return cellSchemes;
	}
	
	/**
	 * calculates a color along the gradient
	 * this is essentially a re-located function that tyler wrote
	 * 
	 * @param colorGradient the color gradient
	 * @param gradientValue the location along the gradient (on 0 to 1)
	 * @return the intermediate color
	 */
	private static MovieAppearance getIntermediateAppearance(
			GradientPaint colorGradient, double gradientValue, 
			Boolean opacityState, Boolean sizeState, String cellType) {
		
		Color startColor = null, endColor = null;
		
		if (colorGradient != null) {
			
			startColor = colorGradient.getColor1();
			endColor = colorGradient.getColor2();
		}
			
		Double startOpacity = 0.025;
		Double endOpacity = 0.75;
		Double startSize = 1.0;
		Double endSize = GlobalConstants.DEFAULT_COMPONENT_WIDTH + 20.0;
		
		if (cellType.equals(GlobalConstants.SPECIES)) {
			
			endSize = GlobalConstants.DEFAULT_SPECIES_WIDTH + 20.0;
		}
		
		MovieAppearance newAppearance = new MovieAppearance();
		
		if (gradientValue <= 0.0) {
			newAppearance.color = startColor;
			if (opacityState == true) newAppearance.opacity = startOpacity;
			if (sizeState == true) newAppearance.size = startSize;
		}
		else if (gradientValue >= 1.0) {
			newAppearance.color = endColor;
			if (opacityState == true) newAppearance.opacity = endOpacity;
			if (sizeState == true) newAppearance.size = endSize;
		}
		else{
			
			float oneMinusRatio = (float)1.0 - (float)gradientValue;
			
			//COLOR
			if(startColor != null && endColor != null) {
				
				int newRed = (int)Math.round(startColor.getRed() * oneMinusRatio + 
						endColor.getRed() * gradientValue);
				if (newRed > 255) newRed = 255;
				
				int newGreen = (int)Math.round(startColor.getGreen() * oneMinusRatio + 
						endColor.getGreen() * gradientValue);
				if (newGreen > 255) newGreen = 255;
				
				int newBlue = (int)Math.round(startColor.getBlue() * oneMinusRatio + 
						endColor.getBlue() * gradientValue);
				if (newBlue > 255) newBlue = 255;
				
				newAppearance.color = new Color(newRed, newGreen, newBlue);
			}
			else newAppearance.color = endColor;
			
			//OPACITY
			if(opacityState == true)
				newAppearance.opacity = startOpacity * oneMinusRatio + endOpacity * gradientValue;
			
			//SIZE
			if(sizeState == true)
				newAppearance.size = startSize * oneMinusRatio + endSize * gradientValue;		
		}
		
		return newAppearance;
	}

	/**
	 * empties the scheme hashmap
	 */
	public void clearAppearances() {
		
		speciesSchemes = new HashMap<String, Scheme>();
	}
	
	
	//SCHEME CLASS

	public class Scheme {

		GradientPaint colorGradient;
		boolean opacityState, sizeState;
		int min, max;
		
		/**
		 * constructor
		 */
		public Scheme() {
			
			min = 0;
			max = 20;
			colorGradient = null;
			opacityState = false;
			sizeState = false;
		}
		
		/**
		 * constructor
		 * @param colorGradient
		 * @param min
		 * @param max
		 */
		public Scheme(GradientPaint colorGradient, Boolean opacityState, Boolean sizeState, int min, int max) {
			
			this();
			
			this.min = min;
			this.max = max;
			
			if (colorGradient != null)
				this.colorGradient = colorGradient;
			
			if (opacityState != null)
				this.opacityState = opacityState;
			
			if (sizeState != null)
				this.sizeState = sizeState;
		}		
		
		//BORING GET/SET METHODS
		
		/**
		 * @param gradient color gradient to be set
		 */
		public void setColorGradient(GradientPaint colorGradient) {
			this.colorGradient = colorGradient;
		}
		
		/**
		 * returns the scheme's color gradient
		 * @return the scheme's color gradient
		 */
		public GradientPaint getColorGradient() {
			return colorGradient;
		}
		
		/**
		 * set whether the opacity changes or not
		 * @param opacityState
		 */
		public void setOpacityState(boolean opacityState) {
			this.opacityState = opacityState;
		}
		
		/**
		 * @return whether opacity changes or not
		 */
		public boolean getOpacityState() {
			return opacityState;
		}
		
		/**
		 * set whether the size changes or not
		 * @param sizeState
		 */
		public void setSizeState(boolean sizeState) {
			this.sizeState = sizeState;
		}
		
		/**
		 * @return whether size changes or not
		 */
		public boolean getSizeState() {
			return sizeState;
		}
		
		/**
		 * @param min the minimum visible number of molecules wrt the color gradient
		 */
		public void setMin(int min) {
			this.min = min;
		}
		
		/**
		 * 
		 * @return
		 */
		public int getMin() {
			return min;
		}
		
		/**
		 * @param max the saturating number of molecules wrt the color gradient
		 */
		public void setMax(int max) {
			this.max = max;
		}
		
		/**
		 * 
		 * @return
		 */
		public int getMax() {
			return max;
		}

	}


	
	//SCHEME APPLY FUNCTION INTERFACE
	
	interface SchemeApplyFunction {
		
		void apply(String speciesID);
	}
}
