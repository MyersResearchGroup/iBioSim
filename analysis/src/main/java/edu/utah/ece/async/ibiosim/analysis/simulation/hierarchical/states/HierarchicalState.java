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
public abstract class HierarchicalState
{
  protected double  value;
  protected double  initValue;
  private Attribute attributes = new Attribute();
  
  public enum StateType
  {
    DENSE, SPARSE, SCALAR, VECTOR
  };

  public double getStateValue()
  {
    return value;
  }

  public void setStateValue(double value)
  {
    this.value = value;
  }

  public abstract double getRateValue();

  public abstract void setRateValue(double value);

  public abstract void addState(int index, HierarchicalState state );

  public abstract void replaceState(int index, HierarchicalState state);
  
  public abstract HierarchicalState getState(int index);
  
  public abstract void update();
  
  public void setInitialValue(double value)
  {
    this.value = value;
    this.initValue = value;
  }
  
  public abstract void restoreInitialValue();
  
  public boolean isPersistent()
  {
    if(attributes != null)
    {
      return attributes.isPersistent;
    }
    return false;
  }

  public void setPersistent(boolean isPersistent)
  {

    if(attributes != null)
    {
      attributes.isPersistent = isPersistent;
    }
  }

  public boolean isUseTriggerValue()
  {

    if(attributes != null)
    {
      return attributes.useTriggerValue;
    }
    return false;
  }

  public void setUseTriggerValue(boolean useTriggerValue)
  {

    if(attributes != null)
    {
      attributes.useTriggerValue = useTriggerValue;
    }
  }

  public boolean isBoundaryCondition()
  {
    if(attributes != null)
    {
      return attributes.isBoundary;
    }
    return false;
  }

  public void setBoundaryCondition(boolean isBoundary)
  {

    if(attributes != null)
    {
      attributes.isBoundary = isBoundary;
    }
  }

  public boolean hasOnlySubstance()
  {

    if(attributes != null)
    {
      return attributes.hasOnlySubstance;
    }
    return false;
  }

  public void setHasOnlySubstance(boolean substance)
  {

    if(attributes != null)
    {
      attributes.hasOnlySubstance = substance;
    }
  }

  public void setIsVariableConstant(boolean isConstant)
  {

    if(attributes != null)
    {
      attributes.isVariableConstant = isConstant;
    }
  }

  public boolean isVariableConstant()
  {
    if(attributes != null)
    {
      return attributes.isVariableConstant;
    }

    return false;
  }

  public void setHasRule(boolean hasRule)
  {
    if(attributes != null)
    {
    attributes.hasRule = hasRule;
    }
  }

  public boolean hasRule()
  {
    if(attributes != null)
    {
    return attributes.hasRule;
    }
    return false;
  }

  public void setHasInitRule(boolean hasInitRule)
  {
    if(attributes != null)
    {
    attributes.hasInitRule = hasInitRule;
    }
  }

  public boolean hasInitRule()
  {
    if(attributes != null)
    {
    return attributes.hasInitRule;
    }
    return false;
  }

  public boolean hasAmountUnits()
  {
    if(attributes != null)
    {
    return attributes.hasAmountUnits;
    }
    return false;
  }

  public void setHasAmountUnits(boolean hasAmountUnits)
  {
    if(attributes != null)
    {
    attributes.hasAmountUnits = hasAmountUnits;
    }
  }

  public boolean hasRate()
  {
    if(attributes != null)
    {
    return attributes.hasRate;
    }
    return false;
  }
  
  public void setHasRate(boolean hasRate)
  {
    if(attributes != null)
    {
      attributes.hasRate = hasRate;
      update();
    }
  }
  
  private class Attribute 
  {

    boolean isBoundary;
    boolean hasOnlySubstance;
    boolean isPersistent;
    boolean useTriggerValue;
    boolean isVariableConstant;
    boolean hasRule;
    boolean hasInitRule;
    boolean hasAmountUnits;
    boolean hasRate;
  }
}
