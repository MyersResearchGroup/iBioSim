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

import java.util.List;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;

/**
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class LocalVariableNode extends VariableNode {

  private HierarchicalNode globalVariable;

  /**
   *
   * @param name
   */
  public LocalVariableNode(String name) {
    super(name);
  }

  @Override
  public double getValue(int modelIndex, List<Integer> listOfIndices) {

    if (this.isDeleted(modelIndex)) {
      if (globalVariable != null) {
        return globalVariable.getValue(modelIndex);
      } else {
        return 0;
      }
    }

    HierarchicalState variableState = state.getChild(modelIndex);
    if (listOfIndices != null) {
      for (int i = listOfIndices.size() - 1; i >= 0; i--) {
        variableState = variableState.getChild(listOfIndices.get(i));
      }
    }

    return variableState.getValue();
  }

  /**
   *
   * @param variable
   */
  public void setGlobalVariable(HierarchicalNode variable) {
    this.globalVariable = variable;
  }
}
