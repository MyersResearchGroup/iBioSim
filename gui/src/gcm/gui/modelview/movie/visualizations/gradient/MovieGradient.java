package gcm.gui.modelview.movie.visualizations.gradient;

import gcm.gui.modelview.movie.visualizations.cellvisualizations.MovieAppearance;
import gcm.util.GlobalConstants;

import java.awt.Color;

public class MovieGradient {

	
	// These names have to match the GRADIENT_APPEARANCES[] array.
	public static final String GRADIENT_NAMES[] = {
		"GFP", 
		"RFP", 
		"BFP",
		"Size",
		"Transparency"
//		,"White to Green",
//		"White to Red",
//		"White to Blue"
		};
	public static final MovieAppearance GRADIENT_APPEARANCES[] = {
		new MovieAppearance(Color.BLACK), new MovieAppearance(new Color(0, 255, 0)),
		new MovieAppearance(Color.BLACK), new MovieAppearance(new Color(255, 0, 0)),
		new MovieAppearance(Color.BLACK), new MovieAppearance(new Color(0, 0, 255)),
		new MovieAppearance(null, (double)0.0), new MovieAppearance(null, (double)0.0),
		new MovieAppearance(null, null, 0.0), new MovieAppearance(null, null, 1.0)
//		,Color.WHITE, new Color(0, 255, 0),
//		Color.WHITE, new Color(255, 0, 0),
//		Color.WHITE, new Color(0, 0, 255)
	};


	private String label;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	private MovieAppearance startAppearance;
	private MovieAppearance endAppearance;
	public MovieAppearance getStartAppearance() {
		return startAppearance;
	}
	public void setStartAppearance(MovieAppearance startColor) {
		this.startAppearance = startColor;
	}
	public MovieAppearance getEndAppearance() {
		return endAppearance;
	}
	public void setEndAppearance(MovieAppearance endColor) {
		this.endAppearance = endColor;
	}
	
	public MovieGradient() {
		// default values
		this.startAppearance = GRADIENT_APPEARANCES[0];
		this.endAppearance = GRADIENT_APPEARANCES[1];
		this.label = GRADIENT_NAMES[0];
	}
	
	/**
	 * Returns a color on the gradient. The start_color 
	 * is returned if the ratio is <= 0, the end_color 
	 * is returned if the ratio is >= 1, and a blended
	 * version is returned if 0 < ratio < 1.
	 * @param ratio
	 */
	public MovieAppearance getIntermediateAppearance(float ratio, double value){
		return startAppearance.interpolate(endAppearance, ratio, value);
	}
	
	/**
	 * Given a gradient name from the GRADIENT_NAMES list,
	 * return a new gradient that matches it.
	 * @param name
	 * @return
	 */
	public static MovieGradient getGradientFromName(String name){
		MovieGradient out = null;
		for(int i=0; i<GRADIENT_NAMES.length; i++){
			if(name.equals(GRADIENT_NAMES[i])){
				int index = i * 2;
				
				// start color
				MovieAppearance startAppearance = (MovieAppearance)(GRADIENT_APPEARANCES[index]);
				startAppearance = new MovieAppearance(startAppearance); // make a copy of the color
				
				// end color
				MovieAppearance endAppearance = (MovieAppearance)(GRADIENT_APPEARANCES[index+1]);
				endAppearance = new MovieAppearance(endAppearance); // make a copy of the color
				
				out = new MovieGradient();
				out.setStartAppearance(startAppearance);
				out.setEndAppearance(endAppearance);
				out.setLabel(name);
				break;
			}
		}
		return out;
	}

	public void duplicatePreferences(MovieGradient master){
		this.startAppearance = new MovieAppearance(master.startAppearance);
		this.endAppearance = new MovieAppearance(master.endAppearance);
		this.label = new String(master.label);
	}
}
