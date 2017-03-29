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

import java.io.IOException;

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
public class CoreSetup
{

  public static void initializeVariables(HierarchicalModel modelstate, Model model, StateType type, VariableNode time, VectorWrapper wrapper) throws IOException
  {
    modelstate.createVariableToNodeMap();
    modelstate.addMappingNode(time.getName(), time);
    ParameterSetup.setupParameters(modelstate, type, model, wrapper);
    CompartmentSetup.setupCompartments(modelstate, type,  model, wrapper);
    SpeciesSetup.setupSpecies(modelstate, type, model, wrapper);
    ReactionSetup.setupReactions(modelstate, model, type, wrapper);
  }

  //TODO: might be able to merge these
  public static void initializeModel(HierarchicalModel modelstate, Model model, VariableNode time)
  {
    initializeModel(modelstate, model, time, false);
  }

  public static void initializeModel(HierarchicalModel modelstate, Model model, VariableNode time, boolean split)
  {
    ArraysSetup.linkDimensionSize(modelstate);
    ArraysSetup.expandArrays(modelstate);
    EventSetup.setupEvents(modelstate, model);
    ConstraintSetup.setupConstraints(modelstate, model);
    RuleSetup.setupRules(modelstate, model);
    InitAssignmentSetup.setupInitialAssignments(modelstate, model);
  }

}
