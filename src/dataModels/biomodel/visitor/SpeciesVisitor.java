package dataModels.biomodel.visitor;

import dataModels.biomodel.network.BaseSpecies;
import dataModels.biomodel.network.ComplexSpecies;
import dataModels.biomodel.network.ConstantSpecies;
import dataModels.biomodel.network.DiffusibleConstitutiveSpecies;
import dataModels.biomodel.network.DiffusibleSpecies;
import dataModels.biomodel.network.NullSpecies;
import dataModels.biomodel.network.SpasticSpecies;
import dataModels.biomodel.network.SpeciesInterface;

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
	public void visitComplex(ComplexSpecies specie);
	
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
	
	/**
	 * Visits a null species
	 * @param specie specie to visit
	 * @param specie
	 */
	public void visitNullSpecies(NullSpecies specie);
	
	/**
	 * 
	 * @param species
	 */
	public void visitDiffusibleConstitutiveSpecies(DiffusibleConstitutiveSpecies species);
	
	/**
	 * Visits a diffusible species
	 * @param species diffusible species to visit
	 */
	public void visitDiffusibleSpecies(DiffusibleSpecies species);
}

