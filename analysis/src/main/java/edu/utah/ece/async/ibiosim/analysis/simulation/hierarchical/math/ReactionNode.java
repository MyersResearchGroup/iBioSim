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

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;

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
  private Map<String, HierarchicalNode> localParameters;
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
  public boolean computePropensity(int index, boolean computeSpeciesRate) {

    boolean changed = false;
    if (!isDeleted(index)) {
      double newValue = 0;
      if (forwardRate != null) {
        double forwardRateValue = Evaluator.evaluateExpressionRecursive(forwardRate, index);
        newValue = forwardRateValue;
      }

      if (reverseRate != null) {
        double reverseRateValue = Evaluator.evaluateExpressionRecursive(reverseRate, index);
        newValue = newValue + reverseRateValue;
      }
      changed = setValue(index, newValue);

      if (computeSpeciesRate) {
        updateSpeciesRate(index);
      }
    }

    return changed;
  }

  /**
   *
   * @param newValue
   * @param oldValue
   * @param index
   */
  public void updateSpeciesRate(int index) {
    // TODO: fix
    double value = getValue(index);
    if (reactants != null) {
      for (SpeciesReferenceNode specRef : reactants) {
        SpeciesNode speciesNode = specRef.getSpecies();
        HierarchicalState state = speciesNode.getState().getChild(index);
        if (!state.isBoundaryCondition()) {
          double stoichiometry = specRef.getValue(index);
          double currentRate = speciesNode.getState().getChild(index).getRateValue();
          double rateChange = value * stoichiometry;
          double newRate = currentRate - rateChange;
          state.setRateValue(newRate);
        }
      }
    }

    if (products != null) {
      for (SpeciesReferenceNode specRef : products) {
        SpeciesNode speciesNode = specRef.getSpecies();
        HierarchicalState state = speciesNode.getState().getChild(index);
        if (!state.isBoundaryCondition()) {
          double stoichiometry = specRef.getValue(index);
          double currentRate = speciesNode.getState().getChild(index).getRateValue();
          double rateChange = value * stoichiometry;
          double newRate = currentRate + rateChange;
          state.setRateValue(newRate);
        }
      }
    }
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
          updateSpeciesReference(reactants, index, -1, false);
        }

        if (products != null) {
          updateSpeciesReference(products, index, 1, false);
        }
      }
    } else {
      if (computeNotEnoughEnoughMoleculesRv(index)) {
        if (reactants != null) {
          updateSpeciesReference(reactants, index, 1, false);
        }

        if (products != null) {
          updateSpeciesReference(products, index, -1, false);
        }
      }
    }
  }

  /**
   *
   * @param index
   * @param threshold
   */
  public List<HierarchicalState> fireReactionAndUpdatePropensity(int index, double threshold) {
    boolean isForward = reverseRate == null || Evaluator.evaluateExpressionRecursive(forwardRate, index) > threshold;
    getRootState(index).getValue();
    double newValue = 0;
    List<HierarchicalState> output = new ArrayList<>();
    if (isForward) {
      if (computeNotEnoughEnoughMoleculesFd(index)) {
        if (reactants != null) {
          output.addAll(updateSpeciesReference(reactants, index, -1, true));
        }

        if (products != null) {
          output.addAll(updateSpeciesReference(products, index, 1, true));
        }
      }

      newValue = Evaluator.evaluateExpressionRecursive(forwardRate, index);
    } else {
      if (computeNotEnoughEnoughMoleculesRv(index)) {
        if (reactants != null) {
          output.addAll(updateSpeciesReference(reactants, index, 1, true));
        }

        if (products != null) {
          output.addAll(updateSpeciesReference(products, index, -1, true));
        }
      }

      newValue = Evaluator.evaluateExpressionRecursive(reverseRate, index);
    }
    setValue(index, newValue);

    return output;
  }

  private List<HierarchicalState> updateSpeciesReference(List<SpeciesReferenceNode> specRefs, int index, int multiplier, boolean getUpdates) {
    List<HierarchicalState> updatedStates = null;
    if (getUpdates) {
      updatedStates = new ArrayList<>();
    }
    for (SpeciesReferenceNode specRef : specRefs) {
      for (HierarchicalNode subNode : specRef) {
        HierarchicalState state = specRef.updateSpecies(index, multiplier);
        if (getUpdates) {
          updatedStates.add(state);
        }
      }
    }

    return updatedStates;
  }

  private boolean computeNotEnoughEnoughMoleculesFd(int index) {
    if (reactants != null) {
      for (SpeciesReferenceNode specRef : reactants) {
        for (HierarchicalNode subNode : reactants) {
          if (!specRef.hasEnoughMolecules(index)) { return false; }
        }
      }
    }

    return true;
  }

  private boolean computeNotEnoughEnoughMoleculesRv(int index) {
    if (products != null) {
      for (SpeciesReferenceNode specRef : products) {
        for (HierarchicalNode subNode : products) {
          if (!specRef.hasEnoughMolecules(index)) { return false; }
        }
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
  public Map<String, HierarchicalNode> getLocalParameters() {
    return localParameters;
  }

  @Override
  public ReactionNode clone() {
    return new ReactionNode(this);
  }
}
