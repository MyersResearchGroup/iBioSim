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

package edu.utah.ece.async.backend.analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;

import edu.utah.ece.async.backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import edu.utah.ece.async.backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.backend.analysis.dynamicsim.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.backend.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class CompartmentSetup
{
  public static void setupCompartments(HierarchicalModel modelstate,  StateType type, Model model, VectorWrapper wrapper)
  {
    for (Compartment compartment : model.getListOfCompartments())
    {
      if (modelstate.isDeletedBySId(compartment.getId()))
      {
        continue;
      }
      setupSingleCompartment(modelstate, compartment, type, wrapper);
    }
  }

  private static void setupSingleCompartment(HierarchicalModel modelstate, Compartment compartment, StateType type, VectorWrapper wrapper)
  {

    String compartmentID = compartment.getId();
    VariableNode node = new VariableNode(compartmentID);
    

    
    if (compartment.getConstant())
    {
      node.createState(StateType.SCALAR, wrapper);
      modelstate.addMappingNode(compartmentID, node);
    }
    else
    {
      node.createState(type, wrapper);
      modelstate.addVariable(node);
    }
    
    if (Double.isNaN(compartment.getSize()))
    {
      node.setValue(modelstate.getIndex(), 1);
    }
    else
    {
      node.setValue(modelstate.getIndex(), compartment.getSize());
    }
  }

}
