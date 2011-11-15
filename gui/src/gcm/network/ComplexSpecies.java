package gcm.network;


import gcm.visitor.SpeciesVisitor;

public class ComplexSpecies extends AbstractSpecies {
	

	public ComplexSpecies(SpeciesInterface s) {
		id = s.getId();
		//properties = s.getProperties();
	}
	
	/**
	 * Empty constructor
	 *
	 */
	public ComplexSpecies() {
		super();
	}
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitComplex(this);
	}
	
}