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
 * A node that represents SBML Species References.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SpeciesReferenceNode extends VariableNode {
  private final SpeciesNode species;
  private List<HierarchicalNode> speciesIndices;

  public SpeciesReferenceNode(SpeciesNode species) {
    super(species.name);
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

}
