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

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;

/**
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class NodeContext {

  private final HierarchicalNode node;
  private final int index;

  /**
   *
   * @param node
   * @param index
   */
  public NodeContext(HierarchicalNode node, int index) {
    this.node = node;
    this.index = index;
  }

  /**
   *
   * @return
   */
  public HierarchicalNode getNode() {
    return node;
  }

  /**
   *
   * @return
   */
  public int getIndex() {
    return index;
  }

}
