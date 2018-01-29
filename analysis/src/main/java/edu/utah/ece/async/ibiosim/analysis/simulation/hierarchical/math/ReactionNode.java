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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private Map<String, VariableNode> 	localParameters;
  private HierarchicalNode			forwardRate;
  private HierarchicalNode      reverseRate;

  public ReactionNode(String name)
  {
    super(name);
    varType = VariableType.REACTION;
  }

  public ReactionNode(ReactionNode copy)
  {
    super(copy);
    varType = VariableType.REACTION;
    this.reactants = copy.reactants;
    this.products = copy.products;
    this.localParameters = copy.localParameters;
    this.forwardRate = copy.forwardRate;
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

  public void setReverseRate(HierarchicalNode kineticLaw)
  {
    this.reverseRate = kineticLaw;
  }

  public HierarchicalNode getForwardRate()
  {
    return forwardRate;
  }

  public HierarchicalNode getReverseRate()
  {
    return reverseRate;
  }

  public boolean computePropensity(int index)
  {
    double oldValue = getValue(index);
    double newValue = 0;

    if (forwardRate != null)
    {
      double forwardRateValue = Evaluator.evaluateExpressionRecursive(forwardRate, index);
      newValue = forwardRateValue;
    }

    if (reverseRate != null)
    {
      double reverseRateValue = Evaluator.evaluateExpressionRecursive(forwardRate, index);
      newValue = newValue + reverseRateValue;
    }

    setValue(index, newValue);

    return oldValue != newValue;
  }



  public void fireReaction(int index, double threshold)
  {

    boolean isForward = reverseRate == null || Evaluator.evaluateExpressionRecursive(forwardRate, index) > threshold;
    if(isForward)
    {
      if (computeNotEnoughEnoughMoleculesFd(index))
      {
        if (reactants != null)
        {

          updateSpeciesReference(reactants, index, -1);
        }

        if (products != null)
        {
          updateSpeciesReference(products, index, 1);
        }
      }
    }
    else
    {
      if (computeNotEnoughEnoughMoleculesRv(index))
      {
        if (reactants != null)
        {
          updateSpeciesReference(reactants, index, 1);
        }

        if (products != null)
        {
          updateSpeciesReference(products, index, -1);
        }
      }
    }
  }

  private void updateSpeciesReference(List<SpeciesReferenceNode> specRefs, int index, int multiplier)
  {
    for (SpeciesReferenceNode specRef : specRefs)
    {
      double stoichiometry = specRef.getStoichiometry(index);
      SpeciesNode speciesNode = specRef.getSpecies();
      if(!speciesNode.getState().getState(index).isBoundaryCondition())
      {
        speciesNode.setValue(index, speciesNode.getValue(index) + multiplier* stoichiometry);
      }
    }
  }

  private boolean computeNotEnoughEnoughMoleculesFd(int index)
  {
    if (reactants != null)
    {
      for (SpeciesReferenceNode specRef : reactants)
      {
        if (specRef.getSpecies().getValue(index) < specRef.getValue(index))
        {
          return false;
        }
      }
    }

    return true;
  }

  private boolean computeNotEnoughEnoughMoleculesRv(int index)
  {
    if (reactants != null)
    {
      for (SpeciesReferenceNode specRef : reactants)
      {
        if (specRef.getSpecies().getValue(index) < specRef.getValue(index))
        {
          return false;
        }
      }
    }

    return true;
  }

  public void addLocalParameter(String id, VariableNode node)
  {
    if(localParameters == null)
    {
      localParameters = new HashMap<String, VariableNode>();
    }
    localParameters.put(id, node);
  }

  public Map<String, VariableNode> getLocalParameters()
  {
    return localParameters;
  }

  @Override
  public ReactionNode clone()
  {
    return new ReactionNode(this);
  }


}
