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
  private final List<HierarchicalNode> reactants;
  private final List<HierarchicalNode> products;

  public SpeciesNode(String name) {
    super(name);
    reactants = new ArrayList<>();
    products = new ArrayList<>();
    varType = VariableType.SPECIES;
  }

  public SpeciesNode(SpeciesNode copy) {
    super(copy);
    this.compartment = copy.compartment;
    this.reactants = copy.reactants;
    this.products = copy.products;
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

  /**
   * Gets the compartment of the species.
   *
   * @return the compartment.
   */
  public VariableNode getCompartment() {
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

  /**
   * Updates the ODE for this species. This function inserts a given reaction rate to the ODE of the species as an
   * addition. When a species is a product in a reaction, the species increases when the reaction fires.
   *
   * @param reactionNode
   *          - the reaction that affects the species.
   * @param specRefNode
   *          - the species reference node that indicates the stoichiometry.
   */
  public void addODERate(ReactionNode reactionNode, SpeciesReferenceNode specRefNode) {
    HierarchicalNode reactionRate = new HierarchicalNode(Type.TIMES);
    reactionRate.addChild(reactionNode);
    reactionRate.addChild(specRefNode);
    products.add(reactionRate);
  }

  /**
   * Updates the ODE for this species. This function inserts a given reaction rate to the ODE of the species as a
   * subtraction. When a species is a reactant in a reaction, the species decreases when the reaction fires.
   *
   * @param reactionNode
   *          - the reaction that affects the species.
   * @param specRefNode
   *          - the species reference node that indicates the stoichiometry.
   */
  public void subtractODERate(ReactionNode reactionNode, SpeciesReferenceNode specRefNode) {
    HierarchicalNode reactionRate = new HierarchicalNode(Type.TIMES);
    reactionRate.addChild(reactionNode);
    reactionRate.addChild(specRefNode);
    reactants.add(reactionRate);
  }

  @Override
  public void computeRate(int index) {
    double rate = 0;
    if (rateRule != null) {
      rate = Evaluator.evaluateExpressionRecursive(rateRule, index);
      if (!state.getChild(index).hasOnlySubstance()) {
        double c = compartment.getValue(index);
        rate = rate * c;
        compartment.computeRate(index);
        double compartmentChange = compartment.getState().getChild(index).getRateValue();
        if (compartmentChange != 0) {
          rate = rate + getValue(index) * compartmentChange / c;
        }
      }
    }

    if (!state.getChild(index).isBoundaryCondition()) {
      for (HierarchicalNode consume : reactants) {
        rate -= Evaluator.evaluateExpressionRecursive(consume, index);
      }
      for (HierarchicalNode produce : products) {
        rate += Evaluator.evaluateExpressionRecursive(produce, index);
      }
    }

    HierarchicalState state = getState().getChild(index);
    state.setRateValue(rate);
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
