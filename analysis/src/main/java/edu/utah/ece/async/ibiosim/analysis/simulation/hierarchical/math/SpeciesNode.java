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
  private HierarchicalNode odeRate;

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

  /**
   * Gets the compartment of the species.
   *
   * @return the compartment.
   */
  public VariableNode getCompartment() {
    return compartment;
  }

  /**
   * Gets the concentration value of the species.
   *
   * @param index
   *          - the model index.
   * @return the concentration value.
   */
  public double getConcentration(int index) {
    return getValue(index) / compartment.getValue(index);
  }

  /**
   * Gets the ODE for the species.
   *
   * @return the rate of change for the species.
   */
  public HierarchicalNode getODERate() {
    return odeRate;
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
    if (odeRate == null) {
      odeRate = new HierarchicalNode(Type.PLUS);
    }

    HierarchicalNode reactionRate = new HierarchicalNode(Type.TIMES);
    reactionRate.addChild(reactionNode);
    reactionRate.addChild(specRefNode);
    odeRate.addChild(reactionRate);
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
    if (odeRate == null) {
      odeRate = new HierarchicalNode(Type.PLUS);
    }

    HierarchicalNode sub = new HierarchicalNode(Type.MINUS);
    HierarchicalNode reactionRate = new HierarchicalNode(Type.TIMES);
    reactionRate.addChild(reactionNode);
    reactionRate.addChild(specRefNode);
    sub.addChild(reactionRate);
    odeRate.addChild(sub);
  }

  @Override
  public double computeRateOfChange(int index) {
    double rate = 0;
    if (rateRule != null) {
      rate = Evaluator.evaluateExpressionRecursive(rateRule, index);
      if (!state.getChild(index).hasOnlySubstance()) {
        double compartmentChange = compartment.computeRateOfChange(index);
        if (compartmentChange != 0) {
          double c = compartment.getValue(index);
          rate = rate + state.getChild(index).getValue() * compartmentChange / c;
        }
      }
    } else if (odeRate != null && !state.getChild(index).isBoundaryCondition()) {
      rate = Evaluator.evaluateExpressionRecursive(odeRate, index);
    }

    return rate;
  }

  @Override
  public SpeciesNode clone() {
    return new SpeciesNode(this);
  }

  /**
   * Get value from arrayed variable.
   *
   * @param modelIndex
   *          - index of submodel.
   * @param listOfIndices
   *          - the array indices.
   * @return the value of selected variable.
   */
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

}
