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

  public SpeciesNode(String name)
  {
    super(name);

    varType = VariableType.SPECIES;
  }

  public SpeciesNode(SpeciesNode copy)
  {
    super(copy);
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
  public void setValue(int index, double value)
  {
    state.getState(index).setStateValue(value);
  }

  @Override
  public double computeRateOfChange(int index)
  {
    double rate = 0;
    if (rateRule != null)
    {
      rate = Evaluator.evaluateExpressionRecursive(rateRule, index);
      if (!state.getState(index).hasOnlySubstance() )
      {
        double compartmentChange = compartment.computeRateOfChange(index);
        if (compartmentChange != 0)
        {
          double c = compartment.getValue(index);
          rate = rate + getValue(index)  * compartmentChange / c;
        }
      }
    }
    else if (odeRate != null && !state.getState(index).isBoundaryCondition())
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

}
