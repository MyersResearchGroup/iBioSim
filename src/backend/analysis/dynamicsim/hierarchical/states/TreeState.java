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

package backend.analysis.dynamicsim.hierarchical.states;

public abstract class TreeState extends HierarchicalState{
  
  @Override
  public double getStateValue(int index) {
    return getState(index).getStateValue();
  }

  @Override
  public void setStateValue(int index, double value) {
    getState(index).setStateValue(value);
  }
  
  @Override
  public double getRateValue() {
    return 0;
  }

  @Override
  public double getRateValue(int index) {
    return 0;
  }
 
}