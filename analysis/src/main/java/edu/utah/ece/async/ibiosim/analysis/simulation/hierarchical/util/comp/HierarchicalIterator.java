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

import java.util.Iterator;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;

/**
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HierarchicalIterator implements Iterator<HierarchicalNode> {

  private final HierarchicalNode node;
  private int size;
  private int iterator;

  /**
   *
   * @param node
   */
  public HierarchicalIterator(HierarchicalNode node) {
    this.node = node;
  }

  @Override
  public boolean hasNext() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public HierarchicalNode next() {
    // TODO Auto-generated method stub
    return null;
  }
}
