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

package edu.utah.ece.async.backend.analysis.simulation.hierarchical.states;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class HierarchicalState
{
  protected double  value;
  

  public enum StateType
  {
    DENSE, SPARSE, SCALAR, VECTOR
  };

  public abstract double getRateValue();

  public abstract double getRateValue(int index);
  
  public abstract double getStateValue();

  public abstract double getStateValue(int index);
  
  public abstract void setStateValue(int index, double value);
  
  public abstract void setStateValue(double value);

  public abstract void addState(int index);

  public abstract void addState(int index, double value);

  public abstract HierarchicalState getState(int index);


}
