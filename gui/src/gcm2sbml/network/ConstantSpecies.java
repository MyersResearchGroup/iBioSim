package gcm2sbml.network;

import gcm2sbml.visitor.SpeciesVisitor;

/**
 * This represents a constant species.
 * @author Nam
 *
 */
public class ConstantSpecies extends AbstractSpecies {
	public ConstantSpecies(String name, String stateName) {
		this.name = name;
		this.stateName = stateName;
	}
	
	public ConstantSpecies() {
	}
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitConstantSpecies(this);
	}
}
