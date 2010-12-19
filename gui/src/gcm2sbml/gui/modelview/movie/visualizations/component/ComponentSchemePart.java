package gcm2sbml.gui.modelview.movie.visualizations.component;

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
}
