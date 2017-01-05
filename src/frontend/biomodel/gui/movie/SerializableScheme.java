package frontend.biomodel.gui.movie;

import java.awt.GradientPaint;


/**
 * this class exists to store information in a way that works with JSON
 * 
 * @author jason
 *
 */
public class SerializableScheme {
		
	public GradientPaint colorGradient;
	public boolean opacityState, sizeState;
	public int min, max;
	public String name;
	public int startColor, endColor;
}