/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.gui.modelEditor.movie;

import java.awt.Color;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
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
		
		MovieAppearance newAppearance;
		
		if(ratio <= 0.0)
			newAppearance = new MovieAppearance(this);
		else if(ratio >= 1.0)
			newAppearance = new MovieAppearance(end);
		else{
			newAppearance = new MovieAppearance();
			float oneMinusRatio = (float)1.0 - ratio;
			
			// color
			if(this.color != null && end.color != null){
				
				newAppearance.color = new Color(
						Math.round(this.color.getRed() * oneMinusRatio + end.color.getRed() * ratio),
						Math.round(this.color.getGreen() * oneMinusRatio + end.color.getGreen() * ratio),
						Math.round(this.color.getBlue() * oneMinusRatio + end.color.getBlue() * ratio));
			}
			
			// size
			if(this.size != null && end.size != null)
				newAppearance.size = value;
			
			// opacity
			if(this.opacity != null && end.opacity != null)
				newAppearance.opacity = this.opacity * oneMinusRatio + end.opacity * ratio;
		}
		
		return newAppearance;
	}
	
	/**
	 * Modifies the MovieAppearance by adding the components of the 
	 * incoming adder movie appearance.
	 * @param adder
	 */
	public void add(MovieAppearance adder){
		
		if(adder == null)
			return;
		
		if(adder.color != null) {
			
			if(this.color == null) {
				this.color = new Color(adder.color.getRGB());
			}
			else{
				
				int red = this.color.getRed() + adder.color.getRed();
				int green = this.color.getGreen() + adder.color.getGreen();
				int blue = this.color.getBlue() + adder.color.getBlue();
				
				red = Math.min(red, 255);
				green = Math.min(green, 255);
				blue = Math.min(blue, 255);
				
				this.color = new Color(red, green, blue);
			}
		}
		
		if(adder.opacity != null) {
			
			if(this.opacity == null)
				this.opacity = 0.025;
			
			this.opacity += adder.opacity;
			this.opacity = Math.min(this.opacity, 0.75); // clamp opacity
		}
		
		if(adder.size != null) {
			
			if(this.size == null)
				this.size = 1.0;
			
			this.size += adder.size;
			
			this.size = Math.min(this.size, 100.0);
		}
	}	
}
