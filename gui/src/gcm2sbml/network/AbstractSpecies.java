package gcm2sbml.network;

import java.util.Properties;

import gcm2sbml.util.Utility;
import gcm2sbml.visitor.SpeciesVisitor;
import gcm2sbml.visitor.VisitableSpecies;

/**
 * This class describes an abstract species class. Species are nodes in the
 * graph. All species are derived from this class.
 * 
 * @author Nam Nguyen
 * 
 */
public abstract class AbstractSpecies implements SpeciesInterface {

	/**
	 * Empty constructor.
	 * 
	 */
	public AbstractSpecies() {
		super();
	}
	
	/**
	 * @return the initial
	 */
	public double getInitial() {
		return initial;
	}

	/**
	 * @param initial the initial to set
	 */
	public void setInitial(double initial) {
		this.initial = initial;
	}
	

	/**
	 * @return the stateProperties
	 */
	public Properties getStateProperties() {
		return stateProperties;
	}

	/**
	 * @param stateProperties
	 *            the stateProperties to set
	 */
	public void setStateProperties(Properties stateProperties) {
		this.stateProperties = stateProperties;
	}

	/**
	 * @return the labelProperties
	 */
	public Properties getLabelProperties() {
		return labelProperties;
	}

	/**
	 * @param labelProperties
	 *            the labelProperties to set
	 */
	public void setLabelProperties(Properties labelProperties) {
		this.labelProperties = labelProperties;
	}

	/**
	 * @return the numberProperties
	 */
	public Properties getNumberProperties() {
		return numberProperties;
	}

	/**
	 * @param numberProperties
	 *            the numberProperties to set
	 */
	public void setNumberProperties(Properties numberProperties) {
		this.numberProperties = numberProperties;
	}

	/**
	 * Returns the decay rates of the species
	 * 
	 * @return the decay rates of the species
	 */
	public double getDecayRate() {
		return decayRate;
	}

	/**
	 * Sets the decay rates of the species
	 * 
	 * @param decayRate
	 *            the decay rate of the species
	 */
	public void setDecayRate(double decayRate) {
		this.decayRate = decayRate;
	}

	/**
	 * Returns the dimerization constant.
	 * 
	 * @return the dimerization constant.
	 */
	public double getDimerizationConstant() {
		return dimerizationConstant;
	}

	/**
	 * Sets the dimerization constant.
	 * 
	 * @param dimerizationConstant
	 *            the dimerization constant
	 */
	public void setDimerizationConstant(double dimerizationConstant) {
		this.dimerizationConstant = dimerizationConstant;
	}

	/**
	 * Returns the name of the species
	 * 
	 * @return the name of the species
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the species
	 * 
	 * @param name
	 *            the name of the species
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the statename of the species
	 * 
	 * @return the statename of the species
	 */
	public String getStateName() {
		return stateName;
	}

	/**
	 * Sets the state name of the species
	 * 
	 * @param stateName
	 *            the state name of the species
	 */
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @return Returns the maxDimer.
	 */
	public int getMaxDimer() {
		return maxDimer;
	}

	/**
	 * @param maxDimer
	 *            The maxDimer to set.
	 */
	public void setMaxDimer(int maxDimer) {
		this.maxDimer = maxDimer;
	}
	
	

	// The name of the species
	protected String name = null;

	// The state associated with the species
	protected String stateName = null;

	// The dimerization rate associated with species
	protected double dimerizationConstant = Double.NaN;

	// The decay rate of the species
	protected double decayRate = Double.NaN;
	
	//Initial concentration
	protected double initial = 0;

	// Number of molecules can come together to form dimer
	protected int maxDimer = 0;

	// State properties, only have a finite set of values
	private Properties stateProperties = null;

	// Arbitrary alphanumeric property
	private Properties labelProperties = null;

	// Number property
	private Properties numberProperties = null;

}
