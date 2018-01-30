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
public class VectorWrapper {
  private double[] values;
  private double[] rates;
  private boolean isSet;
  private int size;
  
  public VectorWrapper()
  {
    this.size = 0;
    this.values = null;
    this.values = null;
    this.isSet = false;
  }

  public void setValues(double[] vector)
  {
      this.values = vector;
  }

  public double[] getValues()
  {
    return this.values;
  }

  public void setRates(double[] vector)
  {
    this.rates = vector;
  }

  public double[] getRates()
  {
    return this.rates;
  }

  public int incrementSize()
  {
    int index = size;
    
    size++;
    
    return index;
  }

  public int getSize()
  {
    return size;
  }

  public boolean isSet()
  {
    return isSet;
  }
  

  public void initStateValues()
  {
    this.values = new double[size];
    isSet = true;
  }

}
