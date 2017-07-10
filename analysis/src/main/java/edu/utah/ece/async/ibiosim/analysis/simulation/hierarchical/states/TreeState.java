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

package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class TreeState extends HierarchicalState{
  
  @Override
  public double getStateValue(int index) {
    if(!containsChild(index))
    {
      return 0;
    }
    return getState(index).getStateValue();
  }

  @Override
  public void setStateValue(int index, double value) {
    if(!containsChild(index))
    {
      addState(index);
    }
    getState(index).setStateValue(value);
  }
  
  @Override
  public double getRateValue() {
    return 0;
  }

  @Override
  public void setRateValue(double value) {
  }
  
  @Override
  public boolean isSetRate()
  {
    return false;
  }
  
  protected abstract boolean containsChild(int index);
 
}