package gcm2sbml.gui.modelview.movie.visualizations.component;

import java.util.LinkedList;
import java.util.Vector;

/**
 * Components can be colored based on multiple color schemes relating to multiple of their sub-species.
 * This class represents all of the different color schemes and relationships to sub-species for a single component.
 * @author Tyler
 *
 */
public class ComponentScheme {

	public final int NUM_PARTS = 2;
	
	private LinkedList<ComponentSchemePart> schemes;
	public LinkedList<ComponentSchemePart> getSchemes(){return schemes;}

	
	public ComponentScheme() {
		// TODO Auto-generated constructor stub
		schemes = new LinkedList<ComponentSchemePart>();
		
		for(int i=0; i<NUM_PARTS; i++){
			ComponentSchemePart csp = new ComponentSchemePart();
			schemes.add(csp);
		}

	}

}
