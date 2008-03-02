package gcm2sbml.network;

import java.util.Properties;

import gcm2sbml.parser.GCMFile;
import gcm2sbml.visitor.VisitableSpecies;


/**
 * This interface describes a species. Species are nodes in the graph
 * 
 * @author Nam Nguyen
 * 
 */
public interface SpeciesInterface extends VisitableSpecies {

	/**
	 * Returns the initial concentration
	 * @return the initial concentration
	 */
	public double getInitial();
	
	/**
	 * Sets the initial concentration
	 * @param initial the initial concentration
	 */
	public void setInitial(double initial);
	
	/**
	 * Returns the decay rate of the species
	 * 
	 * @return the decay rate of the species
	 */
	public double getDecayRate();

	/**
	 * Sets the decay rates of the species
	 * 
	 * @param decayRate
	 *            the decay rates of the species
	 */
	public void setDecayRate(double decayRate);

	/**
	 * Returns the dimerization constants. 
	 * 
	 * @return the dimerization constants.
	 */
	public double getDimerizationConstant();

	/**
	 * Sets the dimerization constants. 
	 * 
	 * @param dimerizationConstant
	 *            the dimerization constants
	 */
	public void setDimerizationConstant(double dimerizationConstant);

	/**
	 * Returns the name of the species
	 * 
	 * @return the name of the species
	 */
	public String getName();
	/**
	 * Sets the name of the species
	 * 
	 * @param name
	 *            the name of the species
	 */
	public void setName(String name);

	/**
	 * Returns the statename of the species
	 * 
	 * @return the statename of the species
	 */
	public String getStateName();

	/**
	 * Sets the state name of the species
	 * 
	 * @param stateName
	 *            the state name of the species
	 */
	public void setStateName(String stateName);
	
	
	/**
	 * @return Returns the maxDimer.
	 */
	public int getMaxDimer();

	/**
	 * @param maxDimer The maxDimer to set.
	 */
	public void setMaxDimer(int maxDimer);

	/**
	 * Sets the properties of the specie
	 * @param properties the property to set
	 */
	public void setProperties(Properties properties);
	
	/**
	 * Returns the property of the specie
	 * @return the property of the specie
	 */
	public Properties getProperties();

	/**
	 * Adds a new value to the specie
	 * @param key the key of the value
	 * @param value the value to add
	 */
	public void addProperty(String key, String value);
	
	/**
	 * Returns the property with the given key
	 * @param key the key of the property
	 * @return
	 */
	public String getProperty(String key);
	
}
