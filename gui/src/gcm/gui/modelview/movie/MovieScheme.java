package gcm.gui.modelview.movie;

import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;
import gcm.gui.modelview.movie.SerializableScheme;

import java.awt.Color;
import java.awt.GradientPaint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * this class contains all of the information related to a movie appearance scheme
 * that affects all cells on the graph (ie, species or components, etc.)
 * 
 * this combines/re-works a lot of the stuff that tyler had spread out among myriad classes
 * 
 * @author jason
 */
public class MovieScheme {

	//CLASS VARIABLES
	
	//contains the appearance data for each species
	//these have the full name, including a component or grid location prefix
	private HashMap<String, Scheme> speciesSchemes;
	
	
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
			int min, int max, String applyTo, GCMFile gcm, String cellType) {
				
		final GradientPaint gradient = colorGradient;
		
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

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
	public void removeSpeciesColorScheme(String speciesID, String cellType, String applyTo, GCMFile gcm) {
		
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

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
			String applyTo, GCMFile gcm, String cellType) {
		
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

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
	public void removeSpeciesOpacityScheme(String speciesID, String cellType, String applyTo, GCMFile gcm) {
				
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

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
			String applyTo, GCMFile gcm, String cellType) {
		
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

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
	public void removeSpeciesSizeScheme(String speciesID, String cellType, String applyTo, GCMFile gcm) {
		
		SchemeApplyFunction schemeApply = new SchemeApplyFunction() {

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
			int min, int max, String applyTo, GCMFile gcm, String cellType, boolean remove) {		
		
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
			String compID = speciesParts[0];
			
			//if the user selected to change components
			if (cellType.equals(GlobalConstants.COMPONENT)) {
			
				for (Map.Entry<String, Properties> component : gcm.getComponents().entrySet()) {
					
					if (component != null) {
						
						//if the component has the same GCM as the component whose appearance
						//was just altered via the scheme chooser panel				
						if (component.getValue().getProperty("gcm")
								.equals(gcm.getComponents().get(compID).getProperty("gcm"))) {
							
							speciesID = new String(component.getKey() + "__" + speciesIDNoPrefix);
							
							if (remove) {
								schemeApply.apply(speciesID);
							}
							else {
								//add a scheme with this other species that's part of the same GCM
								//as the component
								this.createOrUpdateSpeciesScheme(speciesID);
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
				for (int row = 0; row < gcm.getGrid().getNumRows(); ++row) {
					for (int col = 0; col < gcm.getGrid().getNumCols(); ++col) {
						
						String gridPrefix = "ROW" + row + "_COL" + col;
						
						speciesID = new String(gridPrefix + "__" + speciesIDNoPrefix);
							
						if (remove) {							
							schemeApply.apply(speciesID);
						}
						else {
							//add a scheme with this other species at another grid location
							this.createOrUpdateSpeciesScheme(speciesID);
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
				for (String specID : gcm.getSpecies().keySet()) {
					
					if (remove) {
						schemeApply.apply(specID);
					}
					else {
						
						this.createOrUpdateSpeciesScheme(specID);
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
	public void createOrUpdateSpeciesScheme(String speciesID) {
		
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
		
		for (int index = 0; index < schemes.length; ++index) {
			
			Map.Entry<String, Scheme> entry = speciesSchemesIter.next();
				
			schemes[index] = new SerializableScheme();
			
			schemes[index].min = entry.getValue().getMin();
			schemes[index].max = entry.getValue().getMax();
			schemes[index].opacityState = entry.getValue().getOpacityState();
			schemes[index].sizeState = entry.getValue().getSizeState();
			schemes[index].startColor = entry.getValue().getColorGradient().getColor1().getRGB();
			schemes[index].endColor = entry.getValue().getColorGradient().getColor2().getRGB();
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
	public void populate(SerializableScheme[] schemes) {
		
		for (SerializableScheme scheme : schemes) {
			
			Scheme speciesScheme = new Scheme(
					new GradientPaint(0.0f, 0.0f, new Color(scheme.startColor), 0.0f, 50.0f, new Color(scheme.endColor)), 
					scheme.opacityState, scheme.sizeState, scheme.min, scheme.max);
			
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
	public MovieAppearance getAppearance(String cellID, String cellType, 
			int frameIndex, HashMap<String, ArrayList<Double>> speciesTSData) {
		
		HashMap<String, Scheme> cellSchemes = getSchemesWithinCell(cellID, cellType);
		
		if (cellSchemes.size() <= 0) return null;
		else if (cellSchemes.size() == 1) {
			
			//if there's just one scheme, use the frame index and the scheme
			//to create an appearance to send back
			
			Map.Entry<String, Scheme> cellScheme = cellSchemes.entrySet().iterator().next();
			
			String speciesID = cellScheme.getKey();
			int min = cellScheme.getValue().getMin();
			int max = cellScheme.getValue().getMax();
			GradientPaint colorGradient = cellScheme.getValue().getColorGradient();
			boolean opacityState = cellScheme.getValue().getOpacityState();
			boolean sizeState = cellScheme.getValue().getSizeState();	
			
			//number of molecules at this time instance
			double speciesValue = speciesTSData.get(speciesID).get(frameIndex);
			
			//how far along this value is on the gradient spectrum of min to max
			double gradientValue = (double)((speciesValue - min) / (max - min));
			
			//now calculate the correct appearance along the gradient to use			
			return getIntermediateAppearance(colorGradient, gradientValue, opacityState, sizeState, cellType);
		}
		else if (cellSchemes.size() > 1) {
			
			//if there's more than one scheme the colors need to be added together
			//this may change to something else in time to make the colors separable
			
			MovieAppearance cellAppearance = new MovieAppearance();
			
			//loop through every scheme in the cell and add them together
			//to get the final cell appearance
			for (Map.Entry<String, Scheme> cellScheme : cellSchemes.entrySet()) {
				
				String speciesID = cellScheme.getKey();
				int min = cellScheme.getValue().getMin();
				int max = cellScheme.getValue().getMax();
				GradientPaint colorGradient = cellScheme.getValue().getColorGradient();
				boolean opacityState = cellScheme.getValue().getOpacityState();
				boolean sizeState = cellScheme.getValue().getSizeState();
				
				//number of molecules at this time instance
				double speciesValue = speciesTSData.get(speciesID).get(frameIndex);
				
				//how far along this value is on the gradient spectrum of min to max
				double gradientValue = (double)((speciesValue - min) / (max - min));
				
				//now calculate the correct appearance along the gradient to use
				cellAppearance.add(getIntermediateAppearance(
						colorGradient, gradientValue, opacityState, sizeState, cellType));
			}
			
			return cellAppearance;			
		}
		
		return null;
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
	private MovieAppearance getIntermediateAppearance(
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
		
		if (gradientValue <= 0.0)
			newAppearance.color = startColor;
		else if (gradientValue >= 1.0)
			newAppearance.color = endColor;
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
			if(startOpacity != null && endOpacity != null && opacityState == true)
				newAppearance.opacity = startOpacity * oneMinusRatio + endOpacity * gradientValue;
			
			//SIZE
			if(startSize != null && endSize != null && sizeState == true)
				newAppearance.size = startSize * oneMinusRatio + endSize * gradientValue;		
		}
		
		return newAppearance;
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
