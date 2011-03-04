package gcm.gui.modelview.movie.visualizations.cellvisualizations;

import java.awt.Color;

public class MovieAppearance {
	public Color color;
	public Double size;
	public Double opacity;

	/**
	 * No args constructor
	 */
	public MovieAppearance(){
		this.size = null;
		this.color = null;
		this.opacity = null;
	}
	
	public MovieAppearance(Color color){
		this.size = null;
		this.color = color;
		this.opacity = null;
	}
	
	public MovieAppearance(Color color, Double size){
		this.size = size;
		this.color = color;
		this.opacity = null;
	}
	
	public MovieAppearance(Color color, Double size, Double opacity){
		this.size = size;
		this.color = color;
		this.opacity = opacity;
	}
	
	/**
	 * A copy constructor
	 * @param master
	 */
	public MovieAppearance(MovieAppearance master){
		this.size = master.size;
		this.color = master.color;
		this.opacity = master.opacity;
	}
	
	/**
	 * Returns a new MovieAppearance object that is an interpolation
	 * between this one and end. 
	 * @param end: the object returned if ratio == 1.
	 * @param ratio: from 0 to 1. Will be clamped to the range [0,1].
	 * @param value: The actual value. Used for size.
	 * @return
	 */
	public MovieAppearance interpolate(MovieAppearance end, float ratio, double value){
		MovieAppearance ret;
		if(ratio <= 0.0)
			ret = new MovieAppearance(this);
		else if(ratio >= 1.0)
			ret = new MovieAppearance(end);
		else{
			ret = new MovieAppearance();
			float oneMinusRatio = (float)1.0 - ratio;
			
			// color
			if(this.color != null && end.color != null){
				ret.color = new Color(
						Math.round(this.color.getRed() * oneMinusRatio + end.color.getRed() * ratio),
						Math.round(this.color.getGreen() * oneMinusRatio + end.color.getGreen() * ratio),
						Math.round(this.color.getBlue() * oneMinusRatio + end.color.getBlue() * ratio)
				);
			}
			
			// size
			if(this.size != null && end.size != null)
				ret.size = value;
			
			// opacity
			if(this.opacity != null && end.opacity != null)
				ret.opacity = this.opacity * oneMinusRatio + end.opacity * ratio;
		}
		return ret;
	}
	
	/**
	 * Modifies the MovieAppearance by adding the components of the 
	 * incoming adder movie appearance.
	 * @param adder
	 */
	public void add(MovieAppearance adder){
		if(adder == null)
			return;
		if(adder.color != null){
			if(this.color == null){
				this.color = new Color(adder.color.getRGB());
			}else{
				int red = this.color.getRed() + adder.color.getRed();
				int green = this.color.getGreen() + adder.color.getGreen();
				int blue = this.color.getBlue() + adder.color.getBlue();
				red = Math.min(red, 255);
				green = Math.min(green, 255);
				blue = Math.min(blue, 255);
				this.color = new Color(red, green, blue);
			}
		}
		
		if(adder.opacity != null){
			if(this.opacity == null)
				this.opacity = 0.0;
			this.opacity += adder.opacity;
			this.opacity = Math.min(this.opacity, 1.0); // clamp opacity
		}
		
		if(adder.size != null){
			if(this.size == null)
				this.size = 0.0;
			this.size += adder.size;
		}
	}
	
}
