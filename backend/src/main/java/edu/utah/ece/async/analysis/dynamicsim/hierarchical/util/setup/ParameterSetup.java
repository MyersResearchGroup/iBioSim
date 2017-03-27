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
package edu.utah.ece.async.analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;

import edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.VariableNode;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ParameterSetup
{
  /**
   * puts parameter-related information into data structures
   */
  public static void setupParameters(HierarchicalModel modelstate, StateType type, Model model, VectorWrapper wrapper)
  {
    for (Parameter parameter : model.getListOfParameters())
    {
      if (modelstate.isDeletedBySId(parameter.getId()))
      {
        continue;
      }
      else if (ArraysSetup.checkArray(parameter))
      {
        continue;
      }
      setupSingleParameter(modelstate, parameter, type, wrapper);
    }
  }

  /**
   * sets up a single (non-local) parameter
   * 
   * @param parameter
   */
  private static void setupSingleParameter(HierarchicalModel modelstate, Parameter parameter, StateType type, VectorWrapper wrapper)
  {
  
    VariableNode node = new VariableNode(parameter.getId());
    
    
    if (parameter.isConstant())
    {
      node.createState(StateType.SCALAR, wrapper);
      modelstate.addMappingNode(parameter.getId(), node);
    }
    else
    {
      node.createState(type, wrapper);
      modelstate.addVariable(node);
    }

    node.setValue(modelstate.getIndex(), parameter.getValue());

  }

  /**
   * sets up the local parameters in a single kinetic law
   * 
   * @param kineticLaw
   * @param reactionID
   */
  public static void setupLocalParameters(HierarchicalModel modelstate, KineticLaw kineticLaw, Reaction reaction)
  {

    String reactionID = reaction.getId();

    if (kineticLaw != null)
    {
      for (LocalParameter localParameter : kineticLaw.getListOfLocalParameters())
      {

        String id = localParameter.getId();

        if (modelstate.isDeletedBySId(id))
        {
          continue;
        }
        else if (localParameter.isSetMetaId() && modelstate.isDeletedByMetaId(localParameter.getMetaId()))
        {
          continue;
        }

        String parameterID = reactionID + "_" + id;

        VariableNode node = new VariableNode(parameterID, StateType.SCALAR);
        node.setValue(localParameter.getValue());
        modelstate.addMappingNode(parameterID, node);

        HierarchicalUtilities.alterLocalParameter(kineticLaw.getMath(), id, parameterID);
      }
    }
  }

}
