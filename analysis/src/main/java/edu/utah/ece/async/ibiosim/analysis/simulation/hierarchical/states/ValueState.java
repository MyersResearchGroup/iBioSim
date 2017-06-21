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
public class ValueState extends HierarchicalState
{

  public ValueState()
  {
    this.value = 0;
  }

  public ValueState(double value)
  {
    this.value = value;
  }

  @Override
  public double getStateValue()
  {
    return value;
  }

  @Override
  public void setStateValue(double value)
  {
    this.value = value;
  }

  @Override
  public void addState(int index, double value)
  {
    // can't have children
  }

  @Override
  public HierarchicalState getState(int index)
  {
    // can't have children
    return this;
  }

  @Override
  public void addState(int index)
  {

  }
  
  @Override
  public String toString() {
    
    return String.valueOf(value);
  }
  @Override
  public double getStateValue(int index) {
    return value;
  }

  @Override
  public void setStateValue(int index, double value) {
    this.value = value;
  }
  
  @Override
  public double getRateValue() {
    return 0;
  }

  @Override
  public double getRateValue(int index) {
    return 0;
  }

  @Override
  public void setRateValue(int index, double value) {
  }

  @Override
  public void setRateValue(double value) {
  }

  
}