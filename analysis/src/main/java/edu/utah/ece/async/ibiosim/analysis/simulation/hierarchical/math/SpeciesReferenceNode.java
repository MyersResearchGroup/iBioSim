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
 * A node that represents SBML Species References.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SpeciesReferenceNode extends VariableNode {
  private final SpeciesNode species;

  public SpeciesReferenceNode(SpeciesNode species) {
    super("");
    this.species = species;
  }

  public SpeciesReferenceNode(SpeciesReferenceNode copy) {
    super(copy);
    this.species = copy.species.clone();
  }

  /**
   * Get the species that this species reference is associated to.
   *
   * @return the associates species.
   */
  public SpeciesNode getSpecies() {
    return species;
  }

  @Override
  public SpeciesReferenceNode clone() {
    return new SpeciesReferenceNode(this);
  }

  @Override
  public boolean isLocalVariable() {
    return true;
  }

  public void updateSpecies(int index, int multiplier) {
    double stoichiometry = getValue(index);
    HierarchicalState speciesState = species.getState().getChild(index);

    if (indexMap != null && indexMap.containsKey(IndexType.SPECIESREFERENCE)) {
      List<HierarchicalNode> indexMath = indexMap.get(IndexType.SPECIESREFERENCE);

      for (int i = indexMath.size() - 1; i >= 0; i--) {
        int speciesIndex = (int) Evaluator.evaluateExpressionRecursive(indexMath.get(index), index);
        speciesState = speciesState.getChild(speciesIndex);
      }
    }
    if (!speciesState.isBoundaryCondition()) {
      speciesState.setStateValue(speciesState.getValue() + multiplier * stoichiometry);
    }
  }

}
