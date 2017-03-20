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

import java.util.List;

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
  private List<Double> initValues;
  
  public VectorWrapper(List<Double> initValues)
  {
    this.size = 0;
    this.values = null;
    this.values = null;
    this.initValues = null;
    this.isSet = false;
    this.initValues = initValues;
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

  public void setSize(int size)
  {
    this.size = size;
  }

  public int incrementSize()
  {
    int index = size;
    
    size++;
    
    while(initValues.size() < size)
    {
      initValues.add(0.0);
    }
    
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
  
  
  public List<Double> getInitValues()
  {
    return initValues;
  }
  
  public void initStateValues()
  {
    this.values = new double[size];
    
    for(int i = 0; i < size; ++i)
    {
      values[i] = initValues.get(i);
    }
    
    isSet = true;
  }

}
