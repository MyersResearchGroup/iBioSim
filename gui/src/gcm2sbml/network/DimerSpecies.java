package gcm2sbml.network;

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
		this.dimerizationValue = num;
		this.name = monomer.getName() + "_" + num;
		this.dimerizationConstant = monomer.getDimerizationConstant();
	}

	/**
	 * @return Returns the dimerizationValue. or the number
	 * of monomers necessary to form this dimer.
	 */
	public int getDimerizationValue() {
		return dimerizationValue;
	}

	/**
	 * @param dimerizationValue
	 *            The dimerizationValue to set.
	 */
	public void setDimerizationValue(int dimerizationValue) {
		this.dimerizationValue = dimerizationValue;
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

	// Number of monomers necessary to combine to make this dimer
	private int dimerizationValue = 0;
}
