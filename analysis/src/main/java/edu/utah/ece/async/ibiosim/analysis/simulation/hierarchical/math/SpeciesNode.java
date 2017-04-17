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
public class SpeciesNode extends VariableNode
{

	private VariableNode		compartment;
	private HierarchicalNode	odeRate;
	private SpeciesTemplate speciesTemplates;
	
	public SpeciesNode(String name)
	{
		super(name);
		this.isSpecies = true;
	}

	public SpeciesNode(SpeciesNode copy)
	{
		this(copy.name);
		this.compartment = copy.compartment;
	}

	public void setCompartment(VariableNode compartment)
	{
		this.compartment = compartment;
	}

	public VariableNode getCompartment()
	{
		return compartment;
	}

	public double getConcentration(int index)
	{
		return this.getValue(index) / compartment.getValue(index);
	}

	public void createSpeciesTemplate()
	{
	    speciesTemplates = new SpeciesTemplate();
	}
	
	public boolean isBoundaryCondition()
	{
		return speciesTemplates.isBoundary;
	}

	public void setBoundaryCondition(boolean isBoundary)
	{
	    speciesTemplates.isBoundary = isBoundary;
	}

	public boolean hasOnlySubstance()
	{
		  return speciesTemplates.hasOnlySubstance;
	}

	public void setHasOnlySubstance(boolean substance)
	{
	  speciesTemplates.hasOnlySubstance = substance;
	}

	public HierarchicalNode getODERate()
	{
		return odeRate;
	}

	public void addODERate(ReactionNode reactionNode, SpeciesReferenceNode specRefNode)
	{
		if (odeRate == null)
		{
			odeRate = new HierarchicalNode(Type.PLUS);
		}

		HierarchicalNode reactionRate = new HierarchicalNode(Type.TIMES);
		reactionRate.addChild(reactionNode);
		reactionRate.addChild(specRefNode);
		odeRate.addChild(reactionRate);
	}

	public void subtractODERate(ReactionNode reactionNode, SpeciesReferenceNode specRefNode)
	{
		if (odeRate == null)
		{
			odeRate = new HierarchicalNode(Type.PLUS);
		}

		HierarchicalNode sub = new HierarchicalNode(Type.MINUS);

		HierarchicalNode reactionRate = new HierarchicalNode(Type.TIMES);

		reactionRate.addChild(reactionNode);

		reactionRate.addChild(specRefNode);

		sub.addChild(reactionRate);

		odeRate.addChild(sub);
	}

	@Override
	public double computeRateOfChange(int index, double time)
	{
		double rate = 0;
		if (rateRule != null)
		{
			rate = Evaluator.evaluateExpressionRecursive(rateRule, false, index);
			
			if (!speciesTemplates.hasOnlySubstance)
      {
        double compartmentChange = compartment.computeRateOfChange(index, time);
        if (compartmentChange != 0)
        {
          rate = rate * compartment.getValue(index);
          rate = rate + getValue(index) / compartment.getValue(index) * compartmentChange;
        }
        else
        {
          rate = rate * compartment.getValue(index);
        }
      }
			else if (odeRate != null && !speciesTemplates.isBoundary)
	    {
	      rate = Evaluator.evaluateExpressionRecursive(odeRate, index);
	    }
			
			return rate;

		}
		else if (odeRate != null && !speciesTemplates.isBoundary)
		{
			rate = Evaluator.evaluateExpressionRecursive(odeRate, index);
		}
		return rate;
	}

	@Override
	public SpeciesNode clone()
	{
		return new SpeciesNode(this);
	}

	public void setODERate(HierarchicalNode odeRate)
	{
		this.odeRate = odeRate;
	}
	
	private class SpeciesTemplate
	{

	  boolean       isBoundary;
	  boolean       hasOnlySubstance;
	  
	}
}
