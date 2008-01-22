package gcm2sbml.network;

import gcm2sbml.util.Utility;
import gcm2sbml.visitor.SpeciesVisitor;

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
	public BaseSpecies(String name, String stateName,
			double dimerizationConstant, double decayRate, int maxDimer) {
		super();
		this.name = name;
		this.stateName = stateName;
		this.dimerizationConstant = dimerizationConstant;
		this.decayRate = decayRate;
		this.maxDimer = maxDimer;
	}
	
	/**
	 * Empty constructor
	 *
	 */
	public BaseSpecies() {
		super();
	}
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitBaseSpecies(this);
	}
}
