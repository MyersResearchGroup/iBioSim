package gcm2sbml.network;

import gcm2sbml.util.GlobalConstants;
import gcm2sbml.visitor.SpeciesVisitor;

/**
 * This represents a spastic species.
 * @author Nam
 *
 */
public class SpasticSpecies extends AbstractSpecies {
	public SpasticSpecies(String name, String stateName) {
		this.id = name;
		this.stateName = stateName;
	}
	
	public SpasticSpecies() {
	}
	
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitSpasticSpecies(this);
	}
}
