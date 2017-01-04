package biomodel.network;


import biomodel.visitor.SpeciesVisitor;



/**
 * This is the most basic implementation of the species class.  
 * @author Nam
 *
 */
public class BaseSpecies extends AbstractSpecies {
	/**
	 * Constructor with parameters
	 * 
	 * @param name
	 *            the name of the species.
	 * @param stateName
	 *            the state name of the species.
	 * @param dimerizationConstant
	 *            the dimerization constants associated with the species.
	 * @param decayRate
	 *            the decay rates associated with the species.
	 * @param maxDimer
	 * 			  the maximum monomers can combine to form dimer
	 */
	public BaseSpecies(String name, String stateName) {
		this.id = name;
		this.stateName = stateName;
	}
	
	/**
	 * Empty constructor
	 *
	 */
	public BaseSpecies() {
		super();
	}
	
	
	@Override
	public void accept(SpeciesVisitor visitor) {
		visitor.visitBaseSpecies(this);
	}
}

