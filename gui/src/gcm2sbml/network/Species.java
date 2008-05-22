package gcm2sbml.network;

import java.util.Properties;

public class Species {


	/**
	 * Empty constructor.
	 * 
	 */
	public Species() {
		super();
		properties = new Properties();
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
		if (properties == null || !properties.containsKey(key)) {
			return null;
		}
		return properties.get(key).toString();
	}
	
	public boolean containsKey(String key) {
		if (properties == null || !properties.containsKey(key)) {
			return false;
		}
		return true;
	}

	//Properties of the species
	private Properties properties = null;

	// The name of the species
	protected String name = null;

	// The state associated with the species
	protected String stateName = null;
}
