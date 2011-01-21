package gcm2sbml.gui.modelview.movie.visualizations.component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import parser.TSDParser;

import gcm2sbml.gui.modelview.movie.visualizations.ColorScheme;

/**
 * Components can be colored based on multiple color schemes relating to multiple of their sub-species.
 * This class represents one of the color schemes for one sub-species.
 * @author Tyler
 *
 */
public class ComponentSchemePart {

	private String tsdKey;
	private ColorScheme colorScheme;
	

	public String getTsdKey() {
		return tsdKey;
	}

	public void setTsdKey(String tsdKey) {
		this.tsdKey = tsdKey;
	}

	public ColorScheme getColorScheme() {
		return colorScheme;
	}

	public void setColorScheme(ColorScheme colorScheme) {
		this.colorScheme = colorScheme;
	}

	public ComponentSchemePart(){
		tsdKey = null;
		colorScheme = new ColorScheme();
	}
	
	public Color getColor(HashMap<String, ArrayList<Double>> dataHash, int frameIndex){
		
		String key = getTsdKey();
		// if the user hasn't chosen a species, don't use this color.
		if(key == null)
			return null;
		double val = dataHash.get(key).get(frameIndex);
		return colorScheme.getColor(val);
	}
	
	public void duplicatePreferences(ComponentSchemePart master, String compName){
		// figure out the new tsd key
		if(master.tsdKey == null){
			this.tsdKey = null;
		}else{
			Pattern pattern = Pattern.compile("^.*?__(.*)");
			Matcher matcher = pattern.matcher(master.tsdKey);
			matcher.find();
			String newTSDKey = compName + "__" + matcher.group(1);
			
			this.tsdKey = newTSDKey;
		}
		this.colorScheme.duplicatePreferences(master.colorScheme);
	}
	
}
