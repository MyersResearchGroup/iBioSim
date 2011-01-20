package gcm2sbml.gui.modelview.movie.visualizations.gradient;

import java.awt.Color;

public class ColorGradient {

	
	// These names have to match the GRADIENT_COLORS[] array.
	public static final String GRADIENT_NAMES[] = {
		"GFP", 
		"RFP", 
		"BFP",
		"White to Green",
		"White to Red",
		"White to Blue"
		};
	public static final Color GRADIENT_COLORS[] = {
		Color.BLACK, new Color(0, 255, 0),
		Color.BLACK, new Color(255, 0, 0),
		Color.BLACK, new Color(0, 0, 255),
		Color.WHITE, new Color(0, 255, 0),
		Color.WHITE, new Color(255, 0, 0),
		Color.WHITE, new Color(0, 0, 255)
	};


	private String label;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	private Color startColor;
	private Color endColor;
	public Color getStartColor() {
		return startColor;
	}
	public void setStartColor(Color startColor) {
		this.startColor = startColor;
	}
	public Color getEndColor() {
		return endColor;
	}
	public void setEndColor(Color endColor) {
		this.endColor = endColor;
	}
	
	public ColorGradient() {
		// default values
		this.startColor = GRADIENT_COLORS[0];
		this.endColor = GRADIENT_COLORS[1];
		this.label = GRADIENT_NAMES[0];
	}
	
	/**
	 * Returns a color on the gradient. The start_color 
	 * is returned if the ratio is <= 0, the end_color 
	 * is returned if the ratio is >= 1, and a blended
	 * version is returned if 0 < ratio < 1.
	 * @param ratio
	 */
	public Color getIntermediateColor(float ratio){
		if(ratio <= 0.0)
			return startColor;
		else if(ratio >= 1.0)
			return endColor;
		else{
			float oneMinusRatio = (float)1.0 - ratio; 
			Color ret = new Color(
					Math.round(startColor.getRed() * oneMinusRatio + endColor.getRed() * ratio),
					Math.round(startColor.getGreen() * oneMinusRatio + endColor.getGreen() * ratio),
					Math.round(startColor.getBlue() * oneMinusRatio + endColor.getBlue() * ratio)
			);
			return ret;
		}
	}
	
	/**
	 * Given a gradient name from the GRADIENT_NAMES list,
	 * return a new gradient that matches it.
	 * @param name
	 * @return
	 */
	public static ColorGradient getGradientFromName(String name){
		ColorGradient out = null;
		for(int i=0; i<GRADIENT_NAMES.length; i++){
			if(name.equals(GRADIENT_NAMES[i])){
				int index = i * 2;
				
				// start color
				Color startColor = (Color)(GRADIENT_COLORS[index]);
				startColor = new Color(startColor.getRGB()); // make a copy of the color
				
				// end color
				Color endColor = (Color)(GRADIENT_COLORS[index+1]);
				endColor = new Color(endColor.getRGB()); // make a copy of the color
				
				out = new ColorGradient();
				out.setStartColor(startColor);
				out.setEndColor(endColor);
				out.setLabel(name);
				break;
			}
		}
		return out;
	}

}
