package gcm2sbml.network;

import gcm2sbml.visitor.SpeciesVisitor;

import java.util.ArrayList;
import java.util.HashMap;

public class Biochemical {
	
	public Biochemical(ArrayList<Species> inputs) {
		this.inputs = new ArrayList<Species>();
		this.inputs.addAll(inputs);
		makeName();
	}

	/**
	 * Adds a species to the biochemical reaction
	 * 
	 * @param species
	 *            species to add
	 */
	public void addInput(Species species) {
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
		//visitor.visitBiochemical(this);
	}

	/**
	 * @return Returns the rateConstant.
	 */
	public double getRateConstant() {
		return rateConstant;
	}

	public ArrayList<Species> getInputs() {
		return inputs;
	}

	/**
	 * @param rateConstant
	 *            The rateConstant to set.
	 */
	public void setRateConstant(double rateConstant) {
		this.rateConstant = rateConstant;
	}
	
	private String name = "";

	private ArrayList<Species> inputs = null;

	private HashMap<String, Reaction> reactions = null;

	private double rateConstant = -1;

}
