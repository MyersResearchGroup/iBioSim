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

import java.io.IOException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;

import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.FunctionNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.SpeciesNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.VariableNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.AbstractHierarchicalNode.Type;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
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
public class SpeciesSetup
{
  /**
   * sets up a single species
   * 
   * @param species
   * @param speciesID
   */
  private static void setupSingleSpecies(HierarchicalModel modelstate, Species species, Model model, StateType type, VectorWrapper wrapper)
  {

    SpeciesNode node = createSpeciesNode(species, type, modelstate.getIndex(), wrapper);
    
    VariableNode compartment = modelstate.getNode(species.getCompartment());
    node.setCompartment(compartment);
    if (species.isSetInitialAmount())
    {
      node.setValue(modelstate.getIndex(), species.getInitialAmount());
    }
    else if (species.isSetInitialConcentration())
    {
      HierarchicalNode initConcentration = new HierarchicalNode(Type.TIMES);
      initConcentration.addChild(new HierarchicalNode(species.getInitialConcentration()));
      initConcentration.addChild(compartment);
      FunctionNode functionNode = new FunctionNode(node, initConcentration);
      modelstate.addInitAssignment(functionNode);
      functionNode.setIsInitAssignment(true);
    }
    if (species.getConstant())
    {
      modelstate.addMappingNode(species.getId(), node);
    }
    else
    {
      modelstate.addVariable(node);
    }

  }

  /**
   * puts species-related information into data structures
   * 
   * @throws IOException
   */
  public static void setupSpecies(HierarchicalModel modelstate,  StateType type, Model model, VectorWrapper wrapper)
  {
    for (Species species : model.getListOfSpecies())
    {
      if (modelstate.isDeletedBySId(species.getId()))
      {
        continue;
      }
      if (ArraysSetup.checkArray(species))
      {
        continue;
      }
      setupSingleSpecies(modelstate, species, model, type, wrapper);
    }
  }


  private static SpeciesNode createSpeciesNode(Species species, StateType type, int index, VectorWrapper wrapper)
  {
    SpeciesNode node = new SpeciesNode(species.getId());
    node.createSpeciesTemplate(index);
    
    if(species.getConstant())
    {
      node.createState(StateType.SCALAR, wrapper);
    }
    else
    {
      node.createState(type, wrapper);
    }
    node.setValue(index, 0);
    node.setBoundaryCondition(species.getBoundaryCondition(), index);
    node.setHasOnlySubstance(species.getHasOnlySubstanceUnits(), index);
    node.setIsVariableConstant(species.getConstant());
  
    return node;
  }

}
