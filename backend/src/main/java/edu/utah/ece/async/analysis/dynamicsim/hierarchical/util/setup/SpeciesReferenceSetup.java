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
package main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.SpeciesReference;

import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.ReactionNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.SpeciesNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.SpeciesReferenceNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.states.VectorWrapper;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SpeciesReferenceSetup
{

	public static void setupSingleProduct(HierarchicalModel modelstate, ReactionNode reaction, String productID, SpeciesReference product, StateType type, VectorWrapper wrapper)
	{

		if (product.isSetId() && modelstate.isDeletedBySId(product.getId()))
		{
			return;
		}
		if (product.isSetMetaId() && modelstate.isDeletedByMetaId(product.getMetaId()))
		{
			return;
		}

		double stoichiometryValue = Double.isNaN(product.getStoichiometry()) ? 1 : product.getStoichiometry();

		SpeciesReferenceNode speciesReferenceNode = new SpeciesReferenceNode();
    if(!product.isConstant())
    {
      speciesReferenceNode.createState(type, wrapper);
    }
    else
    {
      speciesReferenceNode.createState(StateType.SCALAR, wrapper);
    }
		speciesReferenceNode.setValue(modelstate.getIndex(), stoichiometryValue);
		
		SpeciesNode species = (SpeciesNode) modelstate.getNode(productID);
		speciesReferenceNode.setSpecies(species);
		reaction.addProduct(speciesReferenceNode);

		if (product.isSetId() && product.getId().length() > 0)
		{
			speciesReferenceNode.setName(product.getId());

			if (!product.getConstant())
			{
				modelstate.addVariable(speciesReferenceNode);
				speciesReferenceNode.setIsVariableConstant(false);
			}
			else
			{
				modelstate.addMappingNode(product.getId(), speciesReferenceNode);
			}
		}
		species.addODERate(reaction, speciesReferenceNode);

	}

	public static void setupSingleReactant(HierarchicalModel modelstate, ReactionNode reaction, String reactantID, SpeciesReference reactant, StateType type, VectorWrapper wrapper)
	{

		if (reactant.isSetId() && modelstate.isDeletedBySId(reactant.getId()))
		{
			return;
		}
		if (reactant.isSetMetaId() && modelstate.isDeletedByMetaId(reactant.getMetaId()))
		{
			return;
		}

		double stoichiometryValue = Double.isNaN(reactant.getStoichiometry()) ? 1 : reactant.getStoichiometry();
		SpeciesReferenceNode speciesReferenceNode = new SpeciesReferenceNode();
		if(!reactant.isConstant())
		{
	    speciesReferenceNode.createState(type, wrapper);
		}
		else
		{

	    speciesReferenceNode.createState(StateType.SCALAR, wrapper);
		}
    speciesReferenceNode.setValue(modelstate.getIndex(), stoichiometryValue);
		SpeciesNode species = (SpeciesNode) modelstate.getNode(reactantID);
		speciesReferenceNode.setSpecies(species);
		reaction.addReactant(speciesReferenceNode);

		if (reactant.isSetId() && reactant.getId().length() > 0)
		{
			speciesReferenceNode.setName(reactant.getId());

			if (!reactant.getConstant())
			{
				modelstate.addVariable(speciesReferenceNode);
				speciesReferenceNode.setIsVariableConstant(false);
			}
			else
			{
				modelstate.addMappingNode(reactant.getId(), speciesReferenceNode);
			}
		}
		species.subtractODERate(reaction, speciesReferenceNode);

	}

}
