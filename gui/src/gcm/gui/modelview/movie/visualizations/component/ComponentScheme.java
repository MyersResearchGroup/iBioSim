package gcm.gui.modelview.movie.visualizations.component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import parser.TSDParser;

/**
 * Components can be colored based on multiple color schemes relating to multiple of their sub-species.
 * This class represents all of the different color schemes and relationships to sub-species for a single component.
 * @author Tyler
 *
 */
public class ComponentScheme {

	public final int NUM_PARTS = 3;
	
	private LinkedList<ComponentSchemePart> schemes;
	public LinkedList<ComponentSchemePart> getSchemes(){return schemes;}

	
	public ComponentScheme() {
		init(null);
	}
	
	public ComponentScheme(TSDParser tsdParser){
		init(tsdParser);
	}
	
	private void init(TSDParser tsdParser){
		// TODO Auto-generated constructor stub
		schemes = new LinkedList<ComponentSchemePart>();
		
		for(int i=0; i<NUM_PARTS; i++){
			ComponentSchemePart csp = new ComponentSchemePart();
			schemes.add(csp);
		}
	}
	
	public Color getColor(HashMap<String, ArrayList<Double>> dataHash, int frameIndex){

		ListIterator<ComponentSchemePart> iter = schemes.listIterator();
		
		int red, green, blue;
		red = green = blue = 0;
		boolean isValid = false;
		
		while(iter.hasNext()){
			Color nextCol = iter.next().getColor(dataHash, frameIndex);
			if(nextCol != null){
				red += nextCol.getRed();
				green += nextCol.getGreen();
				blue += nextCol.getBlue();
				isValid = true;
			}
		}
		if(isValid){
			if(red > 255)
				red = 255;
			if(green > 255)
				green = 255;
			if(blue > 255)
				blue = 255;
			return new Color(red, green, blue);
		}else
			return null;
	}
	
	/**
	 * A sort of deep-copy routine
	 * @param masterScheme
	 */
	public void duplicatePreferences(ComponentScheme masterScheme, String compName){
		// wipe out the old parts
		this.schemes = new LinkedList<ComponentSchemePart>();
		
		ListIterator<ComponentSchemePart> iter = masterScheme.schemes.listIterator();
		while(iter.hasNext()){
			ComponentSchemePart part = new ComponentSchemePart();
			this.schemes.add(part);
			part.duplicatePreferences(iter.next(), compName);
		}
	}

}
