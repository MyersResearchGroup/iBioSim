package biomodel.network;

import java.util.Properties;

import biomodel.visitor.SpeciesVisitor;



/**
 * This represents a spastic species.
 * @author Nam
 *
 */
public class SpasticSpecies extends AbstractSpecies {
	public SpasticSpecies(String name, String stateName, Properties properties) {
		//this.properties = properties;
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

