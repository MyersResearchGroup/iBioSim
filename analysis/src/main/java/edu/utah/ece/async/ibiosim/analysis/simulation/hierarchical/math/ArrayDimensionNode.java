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

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.ValueState;

/**
 *
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ArrayDimensionNode extends HierarchicalNode {

  private final String sizeRef;
  private int size;

  public ArrayDimensionNode(String sizeRef) {
    super(Type.NAME);
    this.sizeRef = sizeRef;
    this.state = new ValueState();
  }

  public String getSizeRef() {
    return sizeRef;
  }

  public void setSize(int value) {
    size = value;
  }

  public int getSize() {
    return size;
  }

}