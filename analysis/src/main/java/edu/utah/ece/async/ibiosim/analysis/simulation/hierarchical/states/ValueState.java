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
 * State variable for scalar values.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ValueState extends HierarchicalState {

  public ValueState() {
    this.value = 0;
  }

  public ValueState(double value) {
    this.value = value;
  }

  ValueState(ValueState copy) {
    super(copy);
  }

  @Override
  public HierarchicalState getChild(int index) {
    // can't have children
    return this;
  }

  @Override
  public void addState(int index, HierarchicalState state) {

  }

  @Override
  public String toString() {

    return String.valueOf(value);
  }

  public void update() {}

  @Override
  public void replaceState(int index, HierarchicalState state) {

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

  @Override
  public void restoreInitialValue() {
    this.value = initValue;

  }

  @Override
  public ValueState clone() {
    return new ValueState(this);
  }
}