package gcm2sbml.network;

import java.util.Properties;

import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
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
		if (getProperty(GlobalConstants.INITIAL_STRING) != null) {
			return Double.parseDouble(getProperty(GlobalConstants.INITIAL_STRING));
		}
		return initial;
	}

	/**
	 * @param initial the initial to set
	 */
	public void setInitial(double initial) {
		this.initial = initial;
	}
	
	/**
	 * Returns the decay rates of the species
	 * 
	 * @return the decay rates of the species
	 */
	public double getDecayRate() {
		if (getProperty(GlobalConstants.KDECAY_STRING) != null) {
			return Double.parseDouble(getProperty(GlobalConstants.KDECAY_STRING));
		}
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
		if (getProperty(GlobalConstants.KASSOCIATION_STRING) != null) {
			return Double.parseDouble(getProperty(GlobalConstants.KASSOCIATION_STRING));
		}
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
		if (getProperty(GlobalConstants.MAX_DIMER_STRING) != null) {
			return (int)Double.parseDouble(getProperty(GlobalConstants.MAX_DIMER_STRING));
		}
		return maxDimer;
	}
	
	/**
	 * @param maxDimer
	 *            The maxDimer to set.
	 */
	public void setMaxDimer(int maxDimer) {
		this.maxDimer = maxDimer;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new Properties();			
		}
		properties.put(key, value);
	}
	
	public String getProperty(String key) {
		if (properties == null || !properties.contains(key)) {
			return null;
		}
		return properties.get(key).toString();
	}
	
	public boolean containsKey(String key) {
		if (properties == null || !properties.contains(key)) {
			return false;
		}
		return true;
	}
		
	protected Properties properties = null;
	
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
}
