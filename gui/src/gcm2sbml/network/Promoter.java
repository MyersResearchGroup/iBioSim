package gcm2sbml.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
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
		name = "Promoter_" + id;
		id++;
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

	// Name of promoter
	protected String name = "";

	// Activators of promoter
	protected HashSet<SpeciesInterface> activators = null;

	// Repressors of promoter
	protected HashSet<SpeciesInterface> repressors = null;

	// Outputs of promoter
	protected HashSet<SpeciesInterface> outputs = null;

	// List of reactions
	protected ArrayList<Reaction> activatingReactions = null;

	protected ArrayList<Reaction> repressingReactions = null;

	protected static int id = 0;

	protected HashMap<SpeciesInterface, ArrayList<Reaction>> reactionMap = null;
}
