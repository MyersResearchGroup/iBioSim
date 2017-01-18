package dataModels.biomodel.network;


import dataModels.biomodel.visitor.SpeciesVisitor;



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
		super();
	}
	
	
	@Override
	public void accept(SpeciesVisitor visitor) {
		visitor.visitSpasticSpecies(this);
	}
}

