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
public class VectorState extends HierarchicalState{

  private int vectorIndex;
  private final VectorWrapper vectorState;

  public VectorState(VectorWrapper vectorState)
  {
    this.vectorState = vectorState;
    this.vectorIndex = vectorState.incrementSize();
  }

  @Override
  public double getStateValue() {
    if(vectorState.isSet())
    {
      return vectorState.getValues()[vectorIndex];
    }
    else
    {
      return vectorState.getInitValues().get(vectorIndex);
    }
  }

  @Override
  public void setStateValue(double value) {
    if(vectorState.isSet())
    {
      vectorState.getValues()[vectorIndex] = value;
    }
    else
    {
      vectorState.getInitValues().set(vectorIndex, value);
    }
    
  }

  @Override
  public void addState(int index, HierarchicalState state ) {
  }

  @Override
  public void addState(int index, double value) {
  }

  @Override
  public HierarchicalState getState(int index) {
    return this;
  }

  @Override
  public double getStateValue(int index) {
    return getStateValue();
  }

  @Override
  public void setStateValue(int index, double value) {
    setStateValue(value);
  }
  
  @Override
  public double getRateValue(int index) {
    if(isSetRate(index))
    {
      return vectorState.getRates()[vectorIndex];
    }
    else
    {
      return 0;
    }
  }

  @Override
  public void setRateValue(int index, double value) {
    if(isSetRate(index))
    {
      vectorState.getRates()[vectorIndex] = value;
    }
  }

  @Override
  public boolean isSetRate(int index) {
    return vectorState.isSet() && vectorState.getRates() != null;
  }

  @Override
  public void copyState(int from, int to) {
   
  }
}