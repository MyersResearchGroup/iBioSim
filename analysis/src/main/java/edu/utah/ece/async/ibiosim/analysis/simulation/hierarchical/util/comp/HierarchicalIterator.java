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
import java.util.List;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ArrayDimensionNode;
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
    this.size = 1;
    this.iterator = 0;
    if (node.getListOfDimensions() != null) {
      for (ArrayDimensionNode dimension : node.getListOfDimensions()) {
        size = dimension.getSize() * size;
      }
    }

  }

  @Override
  public boolean hasNext() {
    return iterator < size;
  }

  @Override
  public HierarchicalNode next() {

    if (node.getListOfDimensions() != null) {
      List<ArrayDimensionNode> listOfDimensions = node.getListOfDimensions();
      if (iterator == 0) {
        for (int i = 0; i < listOfDimensions.size(); i++) {
          ArrayDimensionNode dimension = listOfDimensions.get(i);
          dimension.getState().setStateValue(0);
        }
      } else {
        for (int i = 0; i < listOfDimensions.size(); i++) {
          ArrayDimensionNode dimension = listOfDimensions.get(i);
          if (!incrementValue(dimension)) {
            break;
          }
        }
      }

    }
    iterator++;
    return node;
  }

  private boolean incrementValue(ArrayDimensionNode dimension) {
    double value = dimension.getState().getValue() + 1;
    if (value >= dimension.getSize()) {
      dimension.getState().setStateValue(0);
      return true;
    }
    dimension.getState().setStateValue(value);
    return false;
  }
}
