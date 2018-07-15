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

import java.util.ArrayList;
import java.util.List;

/**
 * Stores pointers to values and rates arrays used in the {@link VectorState}.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VectorWrapper {

  private final List<VectorState> listOfVectorStates;
  private double[] values;
  private double[] rates;
  private int size;

  public VectorWrapper() {
    this.size = 0;
    this.values = null;
    this.values = null;
    this.listOfVectorStates = new ArrayList<>();
  }

  /**
   * Sets the array of values.
   *
   * @param vector
   *          - the array of values.
   */
  public void setValues(double[] vector) {
    this.values = vector;
  }

  /**
   * Gets the array of values.
   *
   * @return the array of values.
   */
  public double[] getValues() {
    return this.values;
  }

  /**
   * Sets the array of rates.
   *
   * @param vector
   *          - the new array of rates.
   */
  public void setRates(double[] vector) {
    this.rates = vector;
  }

  /**
   * Gets the array of rates.
   *
   * @return the array of rates.
   */
  public double[] getRates() {
    return this.rates;
  }

  /**
   * Gets the index of the last element in the array and increments the size.
   *
   * @return the index of the last element before incrementing size.
   */
  public int incrementSize() {
    int index = size;
    size++;
    return index;
  }

  /**
   * Gets the size of the vector.
   *
   * @return the number of elements.
   */
  public int getSize() {
    return size;
  }

  /**
   * Checks if the values array has been set.
   *
   * @return true if the values array is set.
   */
  public boolean isSet() {
    return values != null;
  }

  /**
   * Creates the array for state values if it hasn't been created.
   */
  public void initStateValues() {
    if (values == null) {
      ArrayList<Double> initialValues = new ArrayList<>();
      for (VectorState state : listOfVectorStates) {
        if (!state.isReplaced) {
          state.initializeVectorIndex();
          initialValues.add(state.initValue);
        }
      }
      this.values = new double[size];
      for (int i = 0; i < size; i++) {
        values[i] = initialValues.get(i);
      }

    }
  }

  /**
   *
   * @param vectorState
   */
  void addVectorState(VectorState vectorState) {
    listOfVectorStates.add(vectorState);
  }

}
