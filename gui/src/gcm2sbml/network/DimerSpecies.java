package gcm2sbml.network;

import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;
import gcm2sbml.visitor.SpeciesVisitor;

/**
 * This class represents a dimer species.
 * 
 * @author Nam
 * 
 */
public class DimerSpecies extends AbstractSpecies {
	/**
	 * Constructor from a monomer
	 * 
	 * @param monomer
	 *            the monomer that the dimer is derived from
	 * @param num
	 *            the number of monomers necessary to make dimer
	 */
	public DimerSpecies(SpeciesInterface monomer, int num) {
		super();
		this.monomer = monomer;
		this.name = monomer.getName() + "_" + num;
		properties.setProperty(GlobalConstants.MAX_DIMER_STRING, ""+num);
		properties.setProperty(GlobalConstants.ID, name);
	}

	/**
	 * @return Returns the monomer.
	 */
	public SpeciesInterface getMonomer() {
		return monomer;
	}

	/**
	 * @param monomer
	 *            The monomer to set.
	 */
	public void setMonomer(SpeciesInterface monomer) {
		this.monomer = monomer;
	}

	public void accept(SpeciesVisitor visitor) {
		visitor.visitDimer(this);
	}

	// The monomer that makes this species
	private SpeciesInterface monomer = null;
}
