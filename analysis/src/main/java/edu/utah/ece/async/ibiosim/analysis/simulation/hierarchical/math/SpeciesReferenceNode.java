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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SpeciesReferenceNode extends VariableNode
{
	private SpeciesNode	species;
	private String		speciesRefId;

	public SpeciesReferenceNode()
	{
		super("none");
	}

	public SpeciesReferenceNode(String id, double value)
	{
		super(id);
	}

	public SpeciesReferenceNode(SpeciesReferenceNode copy)
	{
		super(copy);
		this.species = copy.species.clone();
		this.speciesRefId = copy.speciesRefId;
	}

	public void setSpecies(SpeciesNode species)
	{
		this.species = species;
	}

	public void setSpeciesRefId(String species)
	{
		this.speciesRefId = species;
	}

	public String getSpeciesRefId()
	{
		return speciesRefId;
	}

	public double getStoichiometry(int index)
	{
		return getValue(index);
	}

	public SpeciesNode getSpecies()
	{
		return species;
	}

	@Override
	public SpeciesReferenceNode clone()
	{
		return new SpeciesReferenceNode(this);
	}

}
