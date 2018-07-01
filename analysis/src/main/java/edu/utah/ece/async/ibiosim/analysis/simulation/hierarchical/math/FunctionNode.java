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

/**
 * A node that represents SBML Initial assignments and SBML Assignment Rules.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class FunctionNode extends HierarchicalNode {

  private VariableNode variable;
  private boolean isInitialAssignment;
  private List<HierarchicalNode> variableIndices;

  public FunctionNode(VariableNode variable, HierarchicalNode math) {
    this(math);
    this.variable = variable;
  }

  public FunctionNode(Type type) {
    super(type);
  }

  public FunctionNode(HierarchicalNode math) {
    super(math.getType());
    if (math.getNumOfChild() > 0) {
      for (HierarchicalNode node : math.getChildren()) {
        this.addChild(node);
      }
    }
    if (math.state != null) {
      this.state = math.state;
    }
    this.name = math.name;

  }

  public FunctionNode(FunctionNode math) {
    super(math);
    this.variable = math.variable;
  }

  public VariableNode getVariable() {
    return variable;
  }

  public void setVariable(VariableNode variable) {
    this.variable = variable;
  }

  public void setIsInitAssignment(boolean initAssign) {
    this.isInitialAssignment = initAssign;
  }

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

    if (!(this.isInitialAssignment && variable.state.getChild(index).hasRule())) {
      double oldValue = variable.getState().getChild(index).getValue();
      double newValue = Evaluator.evaluateExpressionRecursive(this, index);
      variable.setValue(index, newValue);
      boolean isNaN = Double.isNaN(oldValue) && Double.isNaN(newValue);
      changed = !isNaN && oldValue != newValue;
    }

    return changed;
  }

}
