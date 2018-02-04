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

import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SparseState extends TreeState
{
  private Map<Integer, HierarchicalState> mapOfStates;

  public SparseState()
  {
    mapOfStates = new HashMap<Integer, HierarchicalState>();
  }
  

  @Override
  public HierarchicalState getState(int index)
  {
    return mapOfStates.get(index);
  }

  @Override
  public void addState(int index, HierarchicalState state )
  {
    mapOfStates.put(index, state);
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if(mapOfStates != null)
    {
      for(Integer integer : mapOfStates.keySet())
      {
        builder.append("{" + integer + ":" + mapOfStates.get(integer) + "}");
      }
    }
    return builder.toString();
  }

  @Override
  protected boolean containsChild(int index) {
    return mapOfStates.containsKey(index);
  }

  @Override
  public void replaceState(int index, HierarchicalState state) {
   mapOfStates.put(index, state);
    
  }


  @Override
  public double getRateValue() {
    // TODO Auto-generated method stub
    return 0;
  }


  @Override
  public void setRateValue(double value) {
    // TODO Auto-generated method stub
    
  }


}
