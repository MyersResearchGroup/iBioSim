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
 * State variable for states represented as arrays.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VectorState extends HierarchicalState {

  private int vectorIndex;
  private final VectorWrapper wrapper;

  public VectorState(VectorWrapper wrapper) {
    this.wrapper = wrapper;
    this.vectorIndex = -1;
    wrapper.addVectorState(this);
  }

  public VectorState(HierarchicalState copy, VectorWrapper wrapper) {
    super(copy);
    this.vectorIndex = -1;
    this.wrapper = wrapper;
    wrapper.addVectorState(this);
  }

  VectorState(VectorState copy) {
    super(copy);
    this.vectorIndex = -1;
    this.wrapper = copy.wrapper;
    wrapper.addVectorState(this);
  }

  public int getIndex() {
    return vectorIndex;
  }

  void initializeVectorIndex() {
    this.vectorIndex = wrapper.incrementSize();
  }

  @Override
  public double getValue() {
    if (wrapper.isSet()) {
      return wrapper.getValues()[vectorIndex];
    } else {
      return value;
    }
  }

  @Override
  public void setStateValue(double value) {
    if (wrapper.isSet()) {
      wrapper.getValues()[vectorIndex] = value;
    } else {
      this.value = value;
    }
  }

  @Override
  public void addState(int index, HierarchicalState state) {}

  @Override
  public HierarchicalState getChild(int index) {
    return this;
  }

  @Override
  public double getRateValue() {
    return wrapper.getRates()[vectorIndex];

  }

  @Override
  public void replaceState(int index, HierarchicalState state) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setRateValue(double value) {
    wrapper.getRates()[vectorIndex] = value;
  }

  @Override
  public void restoreInitialValue() {
    wrapper.getValues()[vectorIndex] = initValue;
  }

  @Override
  public String toString() {
    if (wrapper.isSet()) { return String.valueOf(wrapper.getValues()[vectorIndex]); }
    return "NaN";
  }

  @Override
  public VectorState clone() {
    return new VectorState(this);
  }
}