package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;

public interface SpeciesVisitor {
	/**
	 * Visits a specie
	 * @param specie specie to visit
	 */
	public void visitSpecies(SpeciesInterface specie);
	
	/**
	 * Visits a dimer
	 * @param specie specie to visit
	 */
	public void visitDimer(DimerSpecies specie);
	
	/**
	 * Visits a dimer
	 * @param specie specie to visit
	 */
	public void visitBiochemical(BiochemicalSpecies specie);
	
	/**
	 * Visits a base specie
	 * @param specie specie to visit
	 */
	public void visitBaseSpecies(BaseSpecies specie);
	
	/**
	 * Visits a constant specie
	 * @param specie specie to visit
	 */
	public void visitConstantSpecies(ConstantSpecies specie);
	
	/**
	 * Visits a spastic specie
	 * @param specie specie to visit
	 */
	public void visitSpasticSpecies(SpasticSpecies specie);	
}
