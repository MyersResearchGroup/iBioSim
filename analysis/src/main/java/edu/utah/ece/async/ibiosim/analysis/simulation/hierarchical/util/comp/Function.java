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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp;

import java.util.List;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.Evaluator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;

/**
 * A node that represents SBML Initial assignments and SBML Assignment Rules.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Function {

  private final VariableNode variable;
  private final HierarchicalNode functionMath;
  private boolean isInitialAssignment;
  private List<HierarchicalNode> variableIndices;

  public Function(VariableNode variable, HierarchicalNode math) {
    this.functionMath = math;
    this.variable = variable;
  }

  public Function(Function math) {
    this.variable = math.variable;
    this.functionMath = math.functionMath;
  }

  /**
   * Gets the variable node associated with this function.
   *
   * @return the variable.
   */
  public VariableNode getVariable() {
    return variable;
  }

  /**
   * Sets a flag to indicate whether this function is an initial assignment.
   *
   * @param initAssign
   *          - whether this is an initial assignment.
   */
  public void setIsInitAssignment(boolean initAssign) {
    this.isInitialAssignment = initAssign;
  }

  /**
   * Checks if this function is an initial assignment.
   *
   * @return true if this function is an initial assignment and false otherwise.
   */
  public boolean isInitAssignment() {
    return this.isInitialAssignment;
  }

  /**
   * Evaluates the node and updates the corresponding variable.
   *
   * @param index
   *          - the model index.
   * @return true if the value has changed. False otherwise.
   */
  public boolean computeFunction(int index) {
    boolean changed = false;

    if (!(this.isInitialAssignment && variable.getState().getChild(index).hasRule())) {
      double newValue = Evaluator.evaluateExpressionRecursive(functionMath, index);
      changed = variable.setValue(index, newValue);
    }

    return changed;
  }

  /**
   * Gets the math associated with this function.
   *
   * @return the right-hand side of this function.
   */
  public HierarchicalNode getMath() {
    return functionMath;
  }

}
