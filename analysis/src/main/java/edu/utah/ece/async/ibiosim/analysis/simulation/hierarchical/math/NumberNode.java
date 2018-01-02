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


public class NumberNode extends HierarchicalNode
{

  private double value;
  public NumberNode(double value) {
    super(Type.NUMBER);
    this.value = value;
  }
  
  @Override
  public double getValue()
  {
    return value;
  }
  
  @Override
  public double getValue(int index)
  {
    return value;
  }
  
  @Override
  public String toString()
  {
    return String.valueOf(value);
  }
}
