package gcm2sbml.network;

import gcm2sbml.util.GlobalConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

/**
 * This class describes a promoter
 * 
 * @author Nam
 * 
 */
public class Promoter {

	/**
	 * Constructor
	 * 
	 */
	public Promoter() {
		outputs = new HashMap<String, SpeciesInterface>();
		activationMap = new HashMap<String, Reaction>();
		repressionMap = new HashMap<String, Reaction>();
		activators = new HashMap<String, SpeciesInterface>();
		repressors = new HashMap<String, SpeciesInterface>();
	}
	
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Creates a unique name
	 * 
	 */
	public void generatorUID() {
		id = "Promoter_" + uniqueID;
		uniqueID++;
	}
	
	/**
	 * @param specie
	 *            the species to add
	 */
	public void addOutput(String id, SpeciesInterface s) {
		outputs.put(id, s);
	}
	
	/**
	 * @return Returns the outputs.
	 */
	public Collection<SpeciesInterface> getOutputs() {
		return outputs.values();
	}

	/**
	 * @param outputs
	 *            The outputs to set.
	 */
	public void setOutputs(HashMap<String, SpeciesInterface> outputs) {
		this.outputs = outputs;
	}

	/**
	 * Adds reaction to list of reactions
	 * 
	 * @param reaction
	 *            reaction to add
	 */
	public void addToReactionMap(String id, Reaction r) {
		if (r.getType().equals("tee")) {
			repressionMap.put(id, r);
		} else if (r.getType().equals("vee")) {
			activationMap.put(id, r);
		} else {
			//throw new IllegalArgumentException(
			//		"Reaction must be activating or repressing");
		}
	}
	
	public void addActivator(String id, SpeciesInterface s) {
		activators.put(id, s);
	}
	
	public void addRepressor(String id, SpeciesInterface s) {
		repressors.put(id, s);
	}

	/**
	 * @return Returns the activators.
	 */
	public Collection<SpeciesInterface> getActivators() {
		return activators.values();
	}
	
	/**
	 * @return Returns the repressors.
	 */
	public Collection<SpeciesInterface> getRepressors() {
		return repressors.values();
	}
	
	/**
	 * @return Returns the reactions.
	 */
	public Collection<Reaction> getActivatingReactions() {
		return activationMap.values();
	}
	
	/**
	 * @return Returns the repressingReactions.
	 */
	public Collection<Reaction> getRepressingReactions() {
		return repressionMap.values();
	}
	
	public HashMap<String, Reaction> getActivationMap() {
		return activationMap;
	}
	
	public HashMap<String, Reaction> getRepressionMap() {
		return repressionMap;
	}
	
	public double getPcount() {
		return Double.parseDouble(getProperty(GlobalConstants.PROMOTER_COUNT_STRING));
	}
	
	/**
	 * Get the activated, open complex (constitutive), and basal production
	 * rate constants
	 */
	public double getKact() {
		return Double.parseDouble(getProperty(GlobalConstants.ACTIVED_STRING));
	}
	
	public double getKoc() {
		return Double.parseDouble(getProperty(GlobalConstants.OCR_STRING));
	}
	
	public double getKbasal() {
		return Double.parseDouble(getProperty(GlobalConstants.KBASAL_STRING));
	}
	
	/**
	 * Gets the production stoichiometry
	 */
	public double getStoich() {
		return Double.parseDouble(getProperty(GlobalConstants.STOICHIOMETRY_STRING));
	}
	
	/**
	 * Gets the equilibrium constant for RNAP binding to an open promoter
	 */
	public double getKrnap() {
		return Double.parseDouble(getProperty(GlobalConstants.RNAP_BINDING_STRING));
	}
	
	/**
	 * @param reactions
	 *            The reactions to set.
	 */
	public void setActivatingReactions(HashMap<String, Reaction> activationMap) {
		this.activationMap = activationMap;
	}

	/**
	 * @param repressingReactions
	 *            The repressingReactions to set.
	 */
	public void setRepressingReactions(HashMap<String, Reaction> repressionMap) {
		this.repressionMap = repressionMap;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public void addProperties(Properties property) {
		if (this.properties == null) {
			this.properties = new Properties();			
		}
		for (Object s : property.keySet()) {
			this.properties.put(s.toString(), property.get(s.toString()));
		}
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
	
	protected Properties properties = null;

	// id of promoter
	protected String id = "";

	// Outputs of promoter
	protected HashMap<String, SpeciesInterface> outputs;

	// List of reactions
	protected HashMap<String, Reaction> activationMap;

	protected HashMap<String, Reaction> repressionMap;
	
	protected HashMap<String, SpeciesInterface> activators;
	
	protected HashMap<String, SpeciesInterface> repressors;

	protected static int uniqueID = 0;

	
}
