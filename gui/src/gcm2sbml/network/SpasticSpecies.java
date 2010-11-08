package gcm2sbml.network;

import java.util.Properties;
import gcm2sbml.visitor.SpeciesVisitor;

/**
 * This represents a spastic species.
 * @author Nam
 *
 */
public class SpasticSpecies extends AbstractSpecies {
	public SpasticSpecies(String name, String stateName, Properties properties) {
		this.properties = properties;
		this.id = name;
		this.stateName = stateName;
	}
	
	public SpasticSpecies() {
		super();
	}
	
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitSpasticSpecies(this);
	}
}

