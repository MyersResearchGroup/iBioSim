package dataModels.biomodel.network;


import dataModels.biomodel.visitor.SpeciesVisitor;



/**
 * This represents a constant species.
 * @author Nam
 *
 */
public class ConstantSpecies extends AbstractSpecies {
	public ConstantSpecies(String name, String stateName) {
		//this.properties = properties;
		this.id = name;
		this.stateName = stateName;
	}
	
	public ConstantSpecies() {
		super();
	}
	
	
	@Override
	public void accept(SpeciesVisitor visitor) {
		visitor.visitConstantSpecies(this);
	}
}

