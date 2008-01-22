package gcm2sbml.network;
/**
 * This represents a species
 * @author Nam
 *
 */
//TODO:  Start refactoring out code, but for now, leave this out.
public interface DimerizableSpecies {
	/**
	 * Returns the dimerization constants. If array size is 0, then species
	 * cannot form dimers.
	 * 
	 * @return the dimerization constants.
	 */
	public double[] getDimerizationConstants();
	
	/**
	 * Sets the dimerization constants. If array size is 0, then the species
	 * cannot form dimers.
	 * 
	 * @param dimerizationConstants
	 *            the dimerization constants
	 */
	public void setDimerizationConstants(double[] dimerizationConstants);
	
	/**
	 * Returns the decay rates of the species and its dimers
	 * 
	 * @return the decay rates of the species and its dimers
	 */
	public double[] getDecayRates();

	/**
	 * Sets the decay rates of the species and its dimers
	 * 
	 * @param decayRates
	 *            the decay rates of the species and its dimers
	 */
	public void setDecayRates(double[] decayRates);
	
}
