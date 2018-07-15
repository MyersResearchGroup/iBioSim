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
 * Base class for the representation of state variables.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class HierarchicalState {
  protected double value;
  protected double initValue;
  protected double initConcentration;
  protected boolean isReplaced;

  private final Attribute attributes = new Attribute();

  /**
   * The representation type of the state variable.
   */
  public enum StateType {
    DENSE, SPARSE, SCALAR, VECTOR
  };

  public boolean isReplaced() {
    return isReplaced;
  }

  public void setReplaced(boolean isReplaced) {
    this.isReplaced = isReplaced;
  }

  /**
   * Gets the state value.
   *
   * @return the value.
   */
  public double getValue() {
    return value;
  }

  /**
   * Sets the state value.
   *
   * @param value
   *          - the new value.
   */
  public void setStateValue(double value) {
    this.value = value;
  }

  /**
   * Gets the rate value.
   *
   * @return the rate value.
   */
  public abstract double getRateValue();

  /**
   * Sets the rate value.
   *
   * @param value
   *          - the new rate value.
   */
  public abstract void setRateValue(double value);

  /**
   * Adds a child state.
   *
   * @param index
   *          - the index to insert the state.
   * @param state
   *          - the child state.
   */
  public abstract void addState(int index, HierarchicalState state);

  /**
   * Replace the child state.
   *
   * @param index
   *          - the index to replace the state.
   * @param state
   *          - the new state.
   */
  public abstract void replaceState(int index, HierarchicalState state);

  /**
   * Gets the state at a given index.
   *
   * @param index
   *          - the index of the child state.
   * @return the child state.
   */
  public abstract HierarchicalState getChild(int index);

  /**
   * Sets the initial value of the state.
   *
   * @param value
   *          - the initial value of the state.
   */
  public void setInitialValue(double value) {
    this.value = value;
    this.initValue = value;
  }

  /**
   * Sets the state value to the initial value.
   */
  public abstract void restoreInitialValue();

  /**
   * Checks if the node is persistent.
   *
   * @return true if the node is persistent
   */
  public boolean isPersistent() {
    if (attributes != null) { return attributes.isPersistent; }
    return false;
  }

  /**
   * Sets the persistent flag.
   *
   * @param isPersistent
   *          - true if the event is persistent.
   */
  public void setPersistent(boolean isPersistent) {
    if (attributes != null) {
      attributes.isPersistent = isPersistent;
    }
  }

  /**
   * Checks if the node is using trigger values.
   *
   * @return true if the node is using trigger values.
   */
  public boolean isUseTriggerValue() {
    if (attributes != null) { return attributes.useTriggerValue; }
    return false;
  }

  /**
   * Sets the use trigger value flag.
   *
   * @param useTriggerValue
   *          - true if using trigger value for event assignments.
   */
  public void setUseTriggerValue(boolean useTriggerValue) {

    if (attributes != null) {
      attributes.useTriggerValue = useTriggerValue;
    }
  }

  /**
   * Checks if the node is boundary condition.
   *
   * @return true if the node is boundary condition
   */
  public boolean isBoundaryCondition() {
    if (attributes != null) { return attributes.isBoundary; }
    return false;
  }

  /**
   * Sets the boundary condition flag.
   *
   * @param isBoundary
   *          - true if species is boundary.
   */
  public void setBoundaryCondition(boolean isBoundary) {

    if (attributes != null) {
      attributes.isBoundary = isBoundary;
    }
  }

  /**
   * Checks if the node has only substance units.
   *
   * @return true if the node has only substance units.
   */
  public boolean hasOnlySubstance() {
    if (attributes != null) { return attributes.hasOnlySubstance; }
    return true;
  }

  /**
   * Sets the has only substance units.
   *
   * @param substance
   *          - if species has only substance units.
   */
  public void setHasOnlySubstance(boolean substance) {
    if (attributes != null) {
      attributes.hasOnlySubstance = substance;
    }
  }

  /**
   * Sets the constant flag.
   *
   * @param isConstant
   *          - true if variable is constant.
   */
  public void setConstant(boolean isConstant) {
    if (attributes != null) {
      attributes.isVariableConstant = isConstant;
    }
  }

  /**
   * Checks if the node is constant.
   *
   * @return true if the node is constant
   */
  public boolean isConstant() {
    if (attributes != null) { return attributes.isVariableConstant; }

    return false;
  }

  /**
   * Sets has rule flag.
   *
   * @param hasRule
   *          - true if variable has a rate rule.
   */
  public void setHasRule(boolean hasRule) {
    if (attributes != null) {
      attributes.hasRule = hasRule;
    }
  }

  /**
   * Checks if the node has rule.
   *
   * @return true if the node has rule
   */
  public boolean hasRule() {
    if (attributes != null) { return attributes.hasRule; }
    return false;
  }

  /**
   * Sets has initial assignment rule flag.
   *
   * @param hasInitRule
   *          - true if variable has initial assignment.
   */
  public void setHasInitRule(boolean hasInitRule) {
    if (attributes != null) {
      attributes.hasInitRule = hasInitRule;
    }
  }

  /**
   * Checks if the node has initial assignment.
   *
   * @return true if the node has initial assignment.
   */
  public boolean hasInitRule() {
    if (attributes != null) { return attributes.hasInitRule; }
    return false;
  }

  /**
   * Checks if the node has a rate rule.
   *
   * @return true if the node has a rate rule.
   */
  public boolean hasRate() {
    if (attributes != null) { return attributes.hasRate; }
    return false;
  }

  /**
   * Sets the has rate rule flag.
   *
   * @param hasRate
   *          - true if the variable has a rate rule.
   */
  public void setHasRate(boolean hasRate) {
    if (attributes != null) {
      attributes.hasRate = hasRate;
    }
  }

  private class Attribute {
    boolean isBoundary = false;
    boolean hasOnlySubstance = true;
    boolean isPersistent = false;
    boolean useTriggerValue = false;
    boolean isVariableConstant = false;
    boolean hasRule = false;
    boolean hasInitRule = false;
    boolean hasRate = false;
  }
}
