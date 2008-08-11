package gcm2sbml.network;

import java.util.ArrayList;
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
		activators = new HashSet<SpeciesInterface>();
		repressors = new HashSet<SpeciesInterface>();
		outputs = new HashSet<SpeciesInterface>();
		activatingReactions = new ArrayList<Reaction>();
		repressingReactions = new ArrayList<Reaction>();
		reactionMap = new HashMap<SpeciesInterface, ArrayList<Reaction>>();
	}

	/**
	 * Adds reaction to list of reactions
	 * 
	 * @param reaction
	 *            reaction to add
	 */
	public void addReaction(Reaction reaction) {
		if (reaction.getType().equals("tee")) {
			repressingReactions.add(reaction);
		} else if (reaction.getType().equals("vee")) {
			activatingReactions.add(reaction);
		} else {
			throw new IllegalArgumentException(
					"Reaction must be activating or repressing");
		}
	}

	/**
	 * Adds an activator
	 * 
	 * @param species
	 *            the species to add
	 */
	public void addActivator(SpeciesInterface species) {
		System.out.println(species.getId());
		activators.add(species);
	}

	/**
	 * Adds a repressor
	 * 
	 * @param species
	 *            the species to add
	 */
	public void addRepressor(SpeciesInterface species) {
		repressors.add(species);
	}

	/**
	 * @return Returns the activators.
	 */
	public HashSet<SpeciesInterface> getActivators() {
		return activators;
	}

	/**
	 * @param activators
	 *            The activators to set.
	 */
	public void setActivators(HashSet<SpeciesInterface> activators) {
		this.activators = activators;
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
	 * @return Returns the outputs.
	 */
	public HashSet<SpeciesInterface> getOutputs() {
		return outputs;
	}

	/**
	 * @param outputs
	 *            The outputs to set.
	 */
	public void setOutputs(HashSet<SpeciesInterface> outputs) {
		this.outputs = outputs;
	}

	/**
	 * @param specie
	 *            the species to add
	 */
	public void addOutput(SpeciesInterface specie) {
		this.outputs.add(specie);
	}

	/**
	 * @return Returns the repressors.
	 */
	public HashSet<SpeciesInterface> getRepressors() {
		return repressors;
	}

	/**
	 * @param repressors
	 *            The repressors to set.
	 */
	public void setRepressors(HashSet<SpeciesInterface> repressors) {
		this.repressors = repressors;
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
	 * @return Returns the reactions.
	 */
	public ArrayList<Reaction> getActivatingReactions() {
		return activatingReactions;
	}

	/**
	 * @param reactions
	 *            The reactions to set.
	 */
	public void setActivatingReactions(ArrayList<Reaction> reactions) {
		this.activatingReactions = reactions;
	}

	/**
	 * @return Returns the repressingReactions.
	 */
	public ArrayList<Reaction> getRepressingReactions() {
		return repressingReactions;
	}

	/**
	 * @param repressingReactions
	 *            The repressingReactions to set.
	 */
	public void setRepressingReactions(ArrayList<Reaction> repressingReactions) {
		this.repressingReactions = repressingReactions;
	}

	public HashMap<SpeciesInterface, ArrayList<Reaction>> getReactionMap() {
		return reactionMap;
	}

	public void setReactionMap(
			HashMap<SpeciesInterface, ArrayList<Reaction>> reactionMap) {
		this.reactionMap = reactionMap;
	}

	/**
	 * Adds a reaction to the ReactionMap
	 * 
	 * @param species
	 *            the species to add
	 * @param reaction
	 *            the reaction to add
	 */
	public void addToReactionMap(SpeciesInterface species, Reaction reaction) {
		if (reactionMap.containsKey(species)) {
			reactionMap.get(species).add(reaction);
		} else {
			ArrayList<Reaction> list = new ArrayList<Reaction>();
			list.add(reaction);
			reactionMap.put(species, list);
		}
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

	// Activators of promoter
	protected HashSet<SpeciesInterface> activators = null;

	// Repressors of promoter
	protected HashSet<SpeciesInterface> repressors = null;

	// Outputs of promoter
	protected HashSet<SpeciesInterface> outputs = null;

	// List of reactions
	protected ArrayList<Reaction> activatingReactions = null;

	protected ArrayList<Reaction> repressingReactions = null;

	protected static int uniqueID = 0;

	protected HashMap<SpeciesInterface, ArrayList<Reaction>> reactionMap = null;
}
