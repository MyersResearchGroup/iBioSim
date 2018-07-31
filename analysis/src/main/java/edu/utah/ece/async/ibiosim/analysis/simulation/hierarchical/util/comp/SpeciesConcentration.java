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

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;

/**
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SpeciesConcentration {

  private final SpeciesNode species;
  private final HierarchicalState speciesState;
  private final double initialConcentration;
  private final int index;

  /**
   *
   * @param species
   * @param concentration
   * @param index
   */
  public SpeciesConcentration(SpeciesNode species, double concentration, int index) {
    this.species = species;
    this.initialConcentration = concentration;
    this.index = index;
    this.speciesState = species.getState().getChild(index);
  }

  public SpeciesConcentration(SpeciesConcentration copy, int index) {
    this.species = copy.species;
    this.initialConcentration = copy.initialConcentration;
    this.index = index;
    this.speciesState = species.getState().getChild(index);
  }

  /**
   *
   * @return
   */
  public boolean computeValue() {
    HierarchicalState speciesState = species.getState().getChild(index);
    if (this.speciesState == speciesState) {
      double currentValue = speciesState.getValue();
      double compartmentValue = species.getCompartment().getValue(index);
      double value = initialConcentration * compartmentValue;
      speciesState.setStateValue(value);
      return value != currentValue;
    }
    return false;
  }

  public boolean hasRule() {
    return species.getState().getChild(index).hasRule() || species.getState().getChild(index).hasInitRule();
  }
}
