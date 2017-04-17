/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.biosim.dataModels.biomodel.visitor;

import edu.utah.ece.async.biosim.dataModels.biomodel.network.BaseSpecies;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.ComplexSpecies;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.ConstantSpecies;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.DiffusibleConstitutiveSpecies;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.DiffusibleSpecies;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.NullSpecies;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.SpasticSpecies;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.SpeciesInterface;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
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

