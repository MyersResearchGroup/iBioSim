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
 * A node that represents SBML Species.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SpeciesNode extends VariableNode {

  private VariableNode compartment;

  public SpeciesNode(String name) {
    super(name);
    varType = VariableType.SPECIES;
  }

  public SpeciesNode(SpeciesNode copy) {
    super(copy);
    this.compartment = copy.compartment;
  }

  /**
   * Sets the compartment of the species.
   *
   * @param compartment
   *          - the compartment that the species belongs to.
   */
  public void setCompartment(VariableNode compartment) {
    this.compartment = compartment;
  }

  @Override
  public HierarchicalNode getCompartment() {
    return compartment;
  }

  @Override
  public double report(int index, boolean concentration) {
    double value = state.getChild(index).getValue();
    if (concentration) {
      value = value / compartment.getValue(index);
    }
    return value;
  }

  @Override
  public SpeciesNode clone() {
    return new SpeciesNode(this);
  }

  @Override
  public double getValue(int modelIndex, List<Integer> listOfIndices) {
    HierarchicalState variableState = state.getChild(modelIndex);
    if (listOfIndices != null) {
      for (int i = listOfIndices.size() - 1; i >= 0; i--) {
        variableState = variableState.getChild(listOfIndices.get(i));
      }
    }

    double speciesValue = variableState.getValue();
    if (!variableState.hasOnlySubstance()) {
      double compartmentValue = compartment.getValue(modelIndex);
      speciesValue = speciesValue / compartmentValue;
    }
    return speciesValue;
  }

  @Override
  public boolean setValue(int modelIndex, List<Integer> listOfIndices, double value) {
    HierarchicalState variableState = state.getChild(modelIndex);
    if (listOfIndices != null) {
      for (int i = listOfIndices.size() - 1; i >= 0; i--) {
        variableState = variableState.getChild(listOfIndices.get(i));
      }
    }

    if (!variableState.hasOnlySubstance()) {
      double compartmentValue = compartment.getValue(modelIndex);
      value = value * compartmentValue;
    }

    double oldValue = variableState.getValue();
    variableState.setStateValue(value);

    return oldValue != value;
  }

}
