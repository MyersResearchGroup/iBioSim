package gcm.gui.modelview.movie.visualizations;

import gcm.gui.modelview.movie.visualizations.cellvisualizations.MovieAppearance;
import gcm.gui.modelview.movie.visualizations.gradient.MovieGradient;

import java.awt.Color;

import parser.TSDParser;

public class ColorScheme {

	private double min;
	private double max;
	private MovieGradient movieGradient;
	
	public MovieGradient getColorGradient() {
		return movieGradient;
	}

	public void setColorGradient(MovieGradient movieGradient) {
		this.movieGradient = movieGradient;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
		assert(min < max);
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
		assert(min < max);
	}
	
	/**
	 * Set min and max simultaneously to prevent
	 * assertion errors if you set them out of order.
	 * @param min
	 * @param max
	 */
	public void setMinMax(double min, double max){
		this.min = min;
		this.max = max;
		assert(min < max);
	}
	
	public ColorScheme() {
		defaultConstructor();
	}
	
	public ColorScheme(TSDParser tsdParser){
		super();
		defaultConstructor();
		if(tsdParser != null){
			this.min = tsdParser.getMinValue();
			this.max = tsdParser.getMaxValue();
		}
	}

	private void defaultConstructor(){
		this.min=0;
		this.max=100;
		this.movieGradient = new MovieGradient();
	}
	
	/**
	 * Returns a color on the gradient based on the input value.
	 * @param val: The value you want to get a color for.
	 * @return: A color on the gradient. returns null if min >= max.
	 */
	public MovieAppearance getAppearance(double val){
		// an error case that would mess up the math.
		// Note that we could have also set the ratio to 0.0
		// and return a color on the gradient, but I thought that
		// setting it to red would more quickly alert the user
		// that they did something wrong.
		if(min >= max)
			return null;
		
		float ratio = (float)((val - min) / (max - min));
		
		// Note that MovieGradient properly handles the case when the ratio
		// is outside the bounds of [0,1]
		return this.movieGradient.getIntermediateAppearance(ratio, val);
	}
	
	public void duplicatePreferences(ColorScheme master){
		this.min = master.min;
		this.max = master.max;
		this.movieGradient.duplicatePreferences(master.movieGradient);
	}
}
