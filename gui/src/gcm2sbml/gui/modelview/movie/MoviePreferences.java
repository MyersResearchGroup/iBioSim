package gcm2sbml.gui.modelview.movie;

import gcm2sbml.gui.modelview.movie.visualizations.ColorScheme;
import gcm2sbml.gui.modelview.movie.visualizations.component.ComponentScheme;

import java.util.HashMap;

public class MoviePreferences {


	private HashMap<String, ColorScheme> speciesColorSchemes;
	private HashMap<String, ComponentScheme> componentSchemes;
	
	public MoviePreferences(){
		speciesColorSchemes = new HashMap<String, ColorScheme>();
		componentSchemes = new HashMap<String, ComponentScheme>();
	}
	
	/**
	 * Returns the ColorScheme for a given species. If such a color scheme doesn't exist
	 * then a new one will be created.
	 * @param species
	 * @return
	 */
	public ColorScheme getOrCreateColorSchemeForSpecies(String species){
		ColorScheme cs = speciesColorSchemes.get(species);
		if(cs == null){
			cs = new ColorScheme();
			speciesColorSchemes.put(species, cs);
		}
		return cs;
	}
	/**
	 * Returns the ComponentScheme for a given component. If such a scheme doesn't exist
	 * then a new one will be created.
	 * @param species
	 * @return
	 */
	public ComponentScheme getOrCreateComponentSchemeForComponent(String comp){
		ComponentScheme cs = componentSchemes.get(comp);
		if(cs == null){
			cs = new ComponentScheme();
			componentSchemes.put(comp, cs);
		}
		return cs;
	}
	
	/**
	 * Returns the component scheme for a component or null if it doesn't exist.
	 * @param comp
	 * @return
	 */
	public ComponentScheme getComponentSchemeForComponent(String comp){
		ComponentScheme cs = componentSchemes.get(comp);
		return cs;
	}
	
}
