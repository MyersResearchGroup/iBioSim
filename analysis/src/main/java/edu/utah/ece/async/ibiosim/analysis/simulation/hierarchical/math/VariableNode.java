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

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.ValueState;

/**
 * A node that represents any SBML variable, such as Compartments, Parameters,
 * LocalParameters, etc.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VariableNode extends HierarchicalNode {

  public VariableNode(String name) {
    super(Type.NAME);
    this.name = name;
  }

  public VariableNode(String name, StateType type) {
    super(Type.NAME);
    this.name = name;
    this.state = new ValueState();
  }

  public VariableNode(VariableNode copy) {
    super(copy);
    this.name = copy.name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public VariableNode clone() {
    return new VariableNode(this);
  }

  public boolean isSetName() {
    return name != null && name.length() > 0;
  }

}
