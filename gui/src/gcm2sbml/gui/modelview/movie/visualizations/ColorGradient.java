package gcm2sbml.gui.modelview.movie.visualizations;

import java.awt.Color;

public class ColorGradient {

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
		// TODO Auto-generated constructor stub
		this.startColor = Color.BLACK;
		this.endColor = new Color(0, 255, 50);
		this.label = "GFP";
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

}
