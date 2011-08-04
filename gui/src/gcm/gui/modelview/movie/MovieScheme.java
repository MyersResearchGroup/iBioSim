package gcm.gui.modelview.movie;

import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;

import java.awt.Color;
import java.awt.GradientPaint;
import java.util.ArrayList;
import java.util.HashMap;
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
	 * this adds or removes a scheme or schemes to the hashmap of schemes
	 * this is also used for updating because the hashmap will overwrite
	 * an entry if one with the same key is added
	 * 
	 * @param speciesID ID of the species to assign the scheme to
	 * @param colorGradient
	 * @param min minimum number of molecules visible wrt the gradient
	 * @param max number of molecules to color saturate wrt the gradient
	 */
	public void applySpeciesScheme(String speciesID, GradientPaint colorGradient, 
			int min, int max, String applyTo, GCMFile gcm, String cellType, boolean remove) {
		
		if (applyTo.equals("this component only")) {
			
			if (remove)
				speciesSchemes.remove(speciesID);
			else
				speciesSchemes.put(speciesID, new Scheme(colorGradient, min, max));
		}
		//if applyTo is "all components with this model"
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
							
							if (remove) {
								speciesSchemes.remove(new String(component.getKey() + "__" + speciesIDNoPrefix));
							}
							else {
								//add a scheme with this other species that's part of the same GCM
								//as the component
								speciesSchemes.put(new String(component.getKey() + "__" + speciesIDNoPrefix), 
										new Scheme(colorGradient, min, max));
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
							
						if (remove) {
							speciesSchemes.remove(new String(gridPrefix + "__" + speciesIDNoPrefix));
						}
						else {
							//add a scheme with this other species at another grid location
							speciesSchemes.put(new String(gridPrefix + "__" + speciesIDNoPrefix), 
									new Scheme(colorGradient, min, max));
						}
					}
				}
			}
		}
	}
	
	/**
	 * removes a scheme for the given species
	 * it's a wrapper for the applySpeciesScheme function
	 * 
	 * @param speciesID
	 */
	public void removeSpeciesScheme(String speciesID, String cellType, String applyTo) {
		
		applySpeciesScheme(speciesID, null, 0, 0, applyTo, null, cellType, true);
	}	
	
	/**
	 * adds a species scheme
	 * it's a wrapper for the applySpeciesScheme function
	 * 
	 * @param speciesID
	 * @param colorGradient
	 * @param min
	 * @param max
	 * @param applyTo
	 * @param gcm
	 * @param cellType
	 */
	public void addSpeciesScheme(String speciesID, GradientPaint colorGradient, 
			int min, int max, String applyTo, GCMFile gcm, String cellType) {
		
		applySpeciesScheme(speciesID, colorGradient, min, max, applyTo, gcm, cellType, false);
	}
	
	/**
	 * returns the scheme corresponding to speciesID
	 * @param speciesID
	 * @return
	 */
	public Scheme getSpeciesScheme(String speciesID) {
		
		return speciesSchemes.get(speciesID);
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
			
			//number of molecules at this time instance
			double speciesValue = speciesTSData.get(speciesID).get(frameIndex);
			
			//how far along this value is on the gradient spectrum of min to max
			double gradientValue = (double)((speciesValue - min) / (max - min));
			
			//now calculate the correct color along the gradient to use
			Color cellColor = calculateIntermediateColor(colorGradient, gradientValue);
			
			return new MovieAppearance(cellColor);
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
				
				//number of molecules at this time instance
				double speciesValue = speciesTSData.get(speciesID).get(frameIndex);
				
				//how far along this value is on the gradient spectrum of min to max
				double gradientValue = (double)((speciesValue - min) / (max - min));
				
				//now calculate the correct color along the gradient to use
				Color cellColor = calculateIntermediateColor(colorGradient, gradientValue);
				
				//add this species' appearance to the total cell appearance
				//this adds the colors together, mainly
				cellAppearance.add(new MovieAppearance(cellColor));
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
			
			//take off the speciesID's component prefix
			//this is done so that other component or grid location prefixes can be added
			String[] speciesParts = speciesScheme.getKey().split("__");
			
			//continue if there's no component prefix
			//or if the prefix is inconsistent with the cell type
			if (speciesParts.length < 2
					|| (cellType.equals(GlobalConstants.COMPONENT) && speciesParts[0].contains("ROW"))) 
				continue;
			
			String compID = speciesParts[0];
			
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
	private Color calculateIntermediateColor(GradientPaint colorGradient, double gradientValue) {
		
		Color startColor = colorGradient.getColor1();
		Color endColor = colorGradient.getColor2();
		
		if(gradientValue <= 0.0)
			return startColor;
		else if(gradientValue >= 1.0)
			return endColor;
		else{
			
			if(startColor != null && endColor != null) {
				
				int newRed = (int)Math.round(startColor.getRed() * (1.0 - gradientValue) + 
						endColor.getRed() * gradientValue);
				if (newRed > 255) newRed = 255;
				
				int newGreen = (int)Math.round(startColor.getGreen() * (1.0 - gradientValue) + 
						endColor.getGreen() * gradientValue);
				if (newGreen > 255) newGreen = 255;
				
				int newBlue = (int)Math.round(startColor.getBlue() * (1.0 - gradientValue) + 
						endColor.getBlue() * gradientValue);
				if (newBlue > 255) newBlue = 255;
				
				return new Color(newRed, newGreen, newBlue);
			}
			else return startColor;
		}
	}
	
	
	
	
	
	
	//SCHEME CLASS
	
	public class Scheme {
	
		GradientPaint colorGradient;
		int min, max;
		
		/**
		 * constructor
		 */
		public Scheme() {
			
			min = 0;
			max = 20;
		}
		
		/**
		 * constructor
		 * @param colorGradient
		 * @param min
		 * @param max
		 */
		public Scheme(GradientPaint colorGradient, int min, int max) {
			
			this.colorGradient = colorGradient;
			this.min = min;
			this.max = max;
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


}
