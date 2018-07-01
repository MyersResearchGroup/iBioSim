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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A node that represents SBML Reactions.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ReactionNode extends VariableNode {

  private List<SpeciesReferenceNode> reactants;
  private List<SpeciesReferenceNode> products;
  private Map<String, VariableNode> localParameters;
  private HierarchicalNode forwardRate;
  private HierarchicalNode reverseRate;

  public ReactionNode(String name) {
    super(name);
    varType = VariableType.REACTION;
  }

  public ReactionNode(ReactionNode copy) {
    super(copy);
    varType = VariableType.REACTION;
    this.reactants = copy.reactants;
    this.products = copy.products;
    this.localParameters = copy.localParameters;
    this.forwardRate = copy.forwardRate;
  }

  /**
   * Adds a reaction reactant.
   *
   * @param speciesRef
   *          - the species reference node.
   */
  public void addReactant(SpeciesReferenceNode speciesRef) {
    if (reactants == null) {
      reactants = new ArrayList<>();
    }
    speciesRef.getSpecies().addReactionDependency(this);
    reactants.add(speciesRef);
  }

  /**
   * Adds a reaction product.
   *
   * @param speciesRef
   *          - the species reference node.
   */
  public void addProduct(SpeciesReferenceNode speciesRef) {
    if (products == null) {
      products = new ArrayList<>();
    }

    speciesRef.getSpecies().addReactionDependency(this);
    products.add(speciesRef);
  }

  /**
   * Sets the forward reaction rate.
   *
   * @param kineticLaw
   *          - the rate equation.
   */
  public void setForwardRate(HierarchicalNode kineticLaw) {
    this.forwardRate = kineticLaw;
  }

  /**
   * Sets the reverse reaction rate.
   *
   * @param kineticLaw
   *          - the rate equation.
   */
  public void setReverseRate(HierarchicalNode kineticLaw) {
    this.reverseRate = kineticLaw;
  }

  /**
   * Gets the forward reaction rate.
   *
   * @return the forward reaction rate node.
   */
  public HierarchicalNode getForwardRate() {
    return forwardRate;
  }

  /**
   * Gets the reverse reaction rate.
   *
   * @return the reverse reaction rate node.
   */
  public HierarchicalNode getReverseRate() {
    return reverseRate;
  }

  /**
   * Computes the reaction propensity.
   *
   * @param index
   *          - the model index.
   */
  public boolean computePropensity(int index) {
    double oldValue = state.getChild(index).getValue();
    double newValue = 0;

    if (forwardRate != null) {
      double forwardRateValue = Evaluator.evaluateExpressionRecursive(forwardRate, index);
      newValue = forwardRateValue;
    }

    if (reverseRate != null) {
      double reverseRateValue = Evaluator.evaluateExpressionRecursive(reverseRate, index);
      newValue = newValue + reverseRateValue;
    }

    setValue(index, newValue);

    return oldValue != newValue;
  }

  /**
   * Executes the reaction and updates the species participating in the reaction.
   *
   * @param index
   *          - the model index.
   * @param threshold
   *          - the threshold to select whether to fire the forward or reverse reaction.
   */
  public void fireReaction(int index, double threshold) {
    boolean isForward = reverseRate == null || Evaluator.evaluateExpressionRecursive(forwardRate, index) > threshold;
    if (isForward) {
      if (computeNotEnoughEnoughMoleculesFd(index)) {
        if (reactants != null) {

          updateSpeciesReference(reactants, index, -1);
        }

        if (products != null) {
          updateSpeciesReference(products, index, 1);
        }
      }
    } else {
      if (computeNotEnoughEnoughMoleculesRv(index)) {
        if (reactants != null) {
          updateSpeciesReference(reactants, index, 1);
        }

        if (products != null) {
          updateSpeciesReference(products, index, -1);
        }
      }
    }
  }

  private void updateSpeciesReference(List<SpeciesReferenceNode> specRefs, int index, int multiplier) {
    for (SpeciesReferenceNode specRef : specRefs) {
      double stoichiometry = specRef.getStoichiometry(index);
      SpeciesNode speciesNode = specRef.getSpecies();
      if (!speciesNode.getState().getChild(index).isBoundaryCondition()) {
        speciesNode.setValue(index, speciesNode.getValue(index) + multiplier * stoichiometry);
      }
    }
  }

  private boolean computeNotEnoughEnoughMoleculesFd(int index) {
    if (reactants != null) {
      for (SpeciesReferenceNode specRef : reactants) {
        if (specRef.getSpecies().getValue(index) < specRef.getSpecies().getValue(index)) { return false; }
      }
    }

    return true;
  }

  private boolean computeNotEnoughEnoughMoleculesRv(int index) {
    if (reactants != null) {
      for (SpeciesReferenceNode specRef : reactants) {
        if (specRef.getSpecies().getValue(index) < specRef.getValue(index)) { return false; }
      }
    }

    return true;
  }

  /**
   * Adds a local parameter.
   *
   * @param id
   *          - the id of the local parameter.
   * @param node
   *          - the local parameter node.
   */
  public void addLocalParameter(String id, VariableNode node) {
    if (localParameters == null) {
      localParameters = new HashMap<>();
    }
    localParameters.put(id, node);
  }

  /**
   * Gets the map for local parameters.
   *
   * @return the map for local parameters.
   */
  public Map<String, VariableNode> getLocalParameters() {
    return localParameters;
  }

  @Override
  public ReactionNode clone() {
    return new ReactionNode(this);
  }

}
