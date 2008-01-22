package gcm2sbml.network;

import gcm2sbml.visitor.SpeciesVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class describes a series of species that can bind together to form a
 * complex.
 * 
 * @author Nam
 * 
 */
public class BiochemicalSpecies extends AbstractSpecies {
	public BiochemicalSpecies(ArrayList<SpeciesInterface> inputs) {
		this.inputs = new ArrayList<SpeciesInterface>();
		this.inputs.addAll(inputs);
		makeName();
	}

	/**
	 * Adds a species to the biochemical reaction
	 * 
	 * @param species
	 *            species to add
	 */
	public void addInput(SpeciesInterface species) {
		this.inputs.add(species);
		makeName();
	}

	/**
	 * Creates the name of the biochemical species
	 * 
	 */
	private void makeName() {
		name = "Biochemical_";
		for (int i = 0; i < inputs.size(); i++) {
			name = name + inputs.get(i);
			if (i != inputs.size() - 1) {
				name = name + "_";
			}
		}
		return;
	}

	/**
	 * Returns the reactions
	 * 
	 * @return the reactions
	 */
	public HashMap<String, Reaction> getReactions() {
		return reactions;
	}

	/**
	 * Sets the reactions
	 * 
	 * @param reactions
	 *            the reactions to set
	 */
	public void setReactions(HashMap<String, Reaction> reactions) {
		this.reactions = reactions;
	}

	/**
	 * Adds reactions
	 * 
	 * @param react
	 *            the reaction to add
	 */
	public void addReaction(Reaction react) {
		this.reactions.put(react.getName(), react);
	}

	public void accept(SpeciesVisitor visitor) {
		visitor.visitBiochemical(this);
	}

	/**
	 * @return Returns the rateConstant.
	 */
	public double getRateConstant() {
		return rateConstant;
	}

	public ArrayList<SpeciesInterface> getInputs() {
		return inputs;
	}

	/**
	 * @param rateConstant
	 *            The rateConstant to set.
	 */
	public void setRateConstant(double rateConstant) {
		this.rateConstant = rateConstant;
	}

	private ArrayList<SpeciesInterface> inputs = null;

	private HashMap<String, Reaction> reactions = null;

	private double rateConstant = -1;
}
