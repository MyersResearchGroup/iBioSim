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

import java.util.LinkedList;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.TriggeredEvent;

/**
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class EventState {
  private double maxDisabledTime;
  private double minEnabledTime;
  private boolean isInitialTrue;
  private final LinkedList<TriggeredEvent> nonPersistentEvents;

  /**
   *
   */
  public EventState() {
    this.maxDisabledTime = Double.NEGATIVE_INFINITY;
    this.minEnabledTime = Double.POSITIVE_INFINITY;
    this.nonPersistentEvents = new LinkedList<>();
  }

  /**
   *
   * @param copy
   */
  public EventState(EventState copy) {
    this.maxDisabledTime = copy.maxDisabledTime;
    this.minEnabledTime = copy.minEnabledTime;
    this.isInitialTrue = copy.isInitialTrue;
    this.nonPersistentEvents = new LinkedList<>();
  }

  /**
   *
   */
  public void reset() {
    if (isInitialTrue) {
      this.maxDisabledTime = Double.NEGATIVE_INFINITY;
    } else {
      this.maxDisabledTime = 0;
    }
    this.minEnabledTime = Double.POSITIVE_INFINITY;
    this.nonPersistentEvents.clear();
  }

  /**
   *
   * @return
   */
  public double getMaxDisabledTime() {
    return maxDisabledTime;
  }

  /**
   *
   * @param maxDisabledTime
   */
  public void setMaxDisabledTime(double maxDisabledTime) {
    this.maxDisabledTime = maxDisabledTime;
  }

  /**
   *
   * @return
   */
  public double getMinEnabledTime() {
    return minEnabledTime;
  }

  /**
   *
   * @param minEnabledTime
   */
  public void setMinEnabledTime(double minEnabledTime) {
    this.minEnabledTime = minEnabledTime;
  }

  /**
   *
   * @return
   */
  public boolean isInitialTrue() {
    return isInitialTrue;
  }

  /**
   *
   * @param isInitialTrue
   */
  public void setInitialTrue(boolean isInitialTrue) {
    this.isInitialTrue = isInitialTrue;
  }

  /**
   *
   * @param event
   */
  public void addNonPersistentEvent(TriggeredEvent event) {
    this.nonPersistentEvents.add(event);
  }

  /**
   *
   * @return
   */
  public LinkedList<TriggeredEvent> getNonPersistentEvents() {
    return nonPersistentEvents;
  }

  @Override
  public EventState clone() {
    return new EventState(this);
  }
}