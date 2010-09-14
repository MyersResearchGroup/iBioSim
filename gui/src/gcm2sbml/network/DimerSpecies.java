package gcm2sbml.network;

import java.util.Properties;

import gcm2sbml.util.GlobalConstants;
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
	public DimerSpecies(SpeciesInterface monomer, Properties properties) {
		this.monomer = monomer;
		this.properties = properties;
		this.id = monomer.getId() + "_"
				+ properties.getProperty(GlobalConstants.MAX_DIMER_STRING);
		properties.setProperty(GlobalConstants.ID, id);
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
