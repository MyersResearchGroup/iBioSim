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
package edu.utah.ece.async.backend.analysis.dynamicsim.hierarchical.math;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ReactionNode extends VariableNode
{

	private List<SpeciesReferenceNode>	reactants;
	private List<SpeciesReferenceNode>	products;
	private HierarchicalNode			forwardRate;
	private HierarchicalNode			reverseRate;
	private boolean						hasEnoughMoleculesFd;
	private boolean						hasEnoughMoleculesRv;
	private double						forwardRateValue;
	private double						reverseRateValue;
	private HierarchicalNode			totalPropensityRef;
	private HierarchicalNode			modelPropensityRef;
	private double						initPropensity;
	private double						initForwardPropensity;

	public ReactionNode(String name)
	{
		super(name);
		isReaction = true;
	}

	public ReactionNode(ReactionNode copy)
	{
		super(copy);
		isReaction = true;
	}

	public List<SpeciesReferenceNode> getListOfReactants()
	{
		return reactants;
	}

	public void addReactant(SpeciesReferenceNode speciesRef)
	{
		if (reactants == null)
		{
			reactants = new ArrayList<SpeciesReferenceNode>();
		}
		speciesRef.getSpecies().addReactionDependency(this);
		reactants.add(speciesRef);
	}

	public void addProduct(SpeciesReferenceNode speciesRef)
	{
		if (products == null)
		{
			products = new ArrayList<SpeciesReferenceNode>();
		}

		speciesRef.getSpecies().addReactionDependency(this);
		products.add(speciesRef);
	}

	public void setForwardRate(HierarchicalNode kineticLaw)
	{
		this.forwardRate = kineticLaw;
	}

	public HierarchicalNode getForwardRate()
	{
		return forwardRate;
	}

	public HierarchicalNode getReverseRate()
	{
		return forwardRate;
	}

	public boolean hasEnoughMolecules()
	{
		return hasEnoughMoleculesFd;
	}

	public boolean computePropensity(int index)
	{
		double oldValue = getValue(index);
		if (forwardRate != null)
		{
			forwardRateValue = Evaluator.evaluateExpressionRecursive(forwardRate, index);
			setValue(index, forwardRateValue);
		}
		if (reverseRate != null)
		{
			reverseRateValue = Evaluator.evaluateExpressionRecursive(reverseRate, index);
			setValue(index, forwardRateValue + reverseRateValue);
		}
		if (totalPropensityRef != null)
		{
			totalPropensityRef.setValue(index, totalPropensityRef.getValue(index) + (getValue(index) - oldValue));
		}
		if (modelPropensityRef != null)
		{
			modelPropensityRef.setValue(index, modelPropensityRef.getValue(index) + (getValue(index) - oldValue));
		}
		return oldValue != getValue(index);
	}

	private void fireReaction(int index, boolean hasEnoughMolecules, List<SpeciesReferenceNode> reactants, List<SpeciesReferenceNode> products)
	{
		if (hasEnoughMolecules)
		{
			Set<ReactionNode> dependentReactions = new HashSet<ReactionNode>();

			if (reactants != null)
			{
				for (SpeciesReferenceNode specRef : reactants)
				{
					double stoichiometry = specRef.getStoichiometry(index);
					SpeciesNode speciesNode = specRef.getSpecies();
					speciesNode.setValue(index, speciesNode.getValue(index) - stoichiometry);
					dependentReactions.addAll(speciesNode.getReactionDependents());
				}
			}

			if (products != null)
			{
				for (SpeciesReferenceNode specRef : products)
				{
					double stoichiometry = specRef.getStoichiometry(index);
					SpeciesNode speciesNode = specRef.getSpecies();
					speciesNode.setValue(index, speciesNode.getValue(index) + stoichiometry);
					dependentReactions.addAll(speciesNode.getReactionDependents());
				}
			}
			updateDependentReactions(index, dependentReactions);
		}
	}

	public void fireReaction(int index, double threshold)
	{
		computeNotEnoughEnoughMolecules(index);
		if (forwardRateValue >= threshold)
		{
			fireReaction(index, hasEnoughMoleculesFd, reactants, products);
		}
		else
		{
			fireReaction(index, hasEnoughMoleculesRv, products, reactants);
		}
	}

	private void updateDependentReactions(int index, Set<ReactionNode> dependentReactions)
	{
		for (ReactionNode reaction : dependentReactions)
		{
			reaction.computePropensity(index);
		}
	}

	public void computeNotEnoughEnoughMolecules(int index)
	{
		hasEnoughMoleculesFd = true;
		if (reactants != null)
		{
			for (SpeciesReferenceNode specRef : reactants)
			{
				if (specRef.getSpecies().getValue(index) < specRef.getValue(index))
				{
					hasEnoughMoleculesFd = false;
					break;
				}
			}
		}
		if (reverseRate != null)
		{
			hasEnoughMoleculesRv = true;
			if (products != null)
			{
				for (SpeciesReferenceNode specRef : products)
				{
					if (specRef.getSpecies().getValue(index) < specRef.getValue(index))
					{
						hasEnoughMoleculesRv = false;
						return;
					}
				}
			}
		}
	}

	public void setModelPropensityRef(HierarchicalNode ref)
	{
		this.modelPropensityRef = ref;
	}

	public void setTotalPropensityRef(HierarchicalNode ref)
	{
		this.totalPropensityRef = ref;
	}

	public void setReverseRate(HierarchicalNode rate)
	{
		this.reverseRate = rate;
	}

	public void setInitPropensity(int index)
	{
		int subModel = indexToSubmodel.get(index);
		this.initPropensity = getValue(subModel);
		this.initForwardPropensity = forwardRateValue;
	}

	public void restoreInitPropensity(int index)
	{
		setValue(index, initPropensity);
		this.forwardRateValue = initForwardPropensity;
		this.reverseRateValue = getValue(index) - forwardRateValue;
	}

	@Override
	public ReactionNode clone()
	{
		return new ReactionNode(this);
	}
}
