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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.TriggeredEvent;

/**
 * A node that represents Events.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class EventNode extends HierarchicalNode {
  private FunctionNode delay;
  private FunctionNode priority;

  private List<FunctionNode> eventAssignments;
  private final Map<Integer, EventState> eventState;

  public EventNode(HierarchicalNode trigger) {
    super(Type.PLUS);
    this.addChild(trigger);

    eventState = new HashMap<>();
  }

  /**
   * Adds a new event state for a given model.
   *
   * @param index
   *          - the model index.
   */
  public void addEventState(int index) {
    EventState state = new EventState();
    eventState.put(index, state);
  }

  /**
   * Gets the event state for a given model.
   *
   * @param index
   *          - the model index.
   *
   * @return the event state for a given model.
   */
  public EventState getEventState(int index) {
    return eventState.get(index);
  }

  /**
   * Gets the node corresponding to the delay.
   *
   * @return the delay.
   */
  public HierarchicalNode getDelay() {
    return delay;
  }

  /**
   * Sets the node corresponding to the delay.
   *
   * @param delay
   *          - the new value of the delay.
   */
  public void setDelay(FunctionNode delay) {
    this.delay = delay;
  }

  /**
   * Gets the max disabled time.
   *
   * @param index
   *          - the model index.
   *
   * @return the max disabled time.
   */
  public double getMaxDisabledTime(int index) {
    return eventState.get(index).maxDisabledTime;
  }

  /**
   * Sets the max disable time.
   *
   * @param index
   *          - the model index.
   * @param maxDisabledTime
   *          - the time that was last seen disabled.
   */
  public void setMaxDisabledTime(int index, double maxDisabledTime) {
    eventState.get(index).maxDisabledTime = maxDisabledTime;
  }

  public void setInitialTrue(int index, boolean isInitialTrue) {
    eventState.get(index).isInitialTrue = isInitialTrue;
    if (!isInitialTrue) {
      eventState.get(index).maxDisabledTime = 0;
    }
  }

  /**
   * Gets the min disable time.
   *
   * @param index
   *          - the model index.
   *
   * @return the min disabled time.
   */
  public double getMinEnabledTime(int index) {
    return eventState.get(index).minEnabledTime;
  }

  /**
   * Sets the min disable time.
   *
   * @param index
   *          - the model index.
   * @param minDisabledTime
   *          - the time that was first seen enabled.
   */
  public void setMinEnabledTime(int index, double minEnabledTime) {
    eventState.get(index).minEnabledTime = minEnabledTime;
  }

  public void addTriggeredEvent(int index, TriggeredEvent event) {
    eventState.get(index).nonPersistentEvents.add(event);
  }

  private void untriggerNonPersistent(int index) {
    LinkedList<TriggeredEvent> nonPersistent = eventState.get(index).nonPersistentEvents;

    while (!nonPersistent.isEmpty()) {
      TriggeredEvent node = nonPersistent.removeFirst();
      node.setFlipped();
    }
  }

  /**
   * Adds an event assignment node.
   *
   * @param eventAssignmentNode
   *          - the event assignment that will be executed when the event is fired.
   *
   */
  public void addEventAssignment(FunctionNode eventAssignmentNode) {
    if (eventAssignments == null) {
      eventAssignments = new ArrayList<>();
    }

    eventAssignments.add(eventAssignmentNode);
  }

  /**
   * Evaluates the time when the event should be fired.
   *
   * @param index
   *          - the model index.
   * @return the fire time.
   */
  public double evaluateFireTime(int index) {
    double fireTime = 0;
    if (delay != null && !delay.isDeleted(index)) {
      fireTime = Evaluator.evaluateExpressionRecursive(delay.getMath(), index);
    }
    return fireTime;
  }

  /**
   * Evaluates the priority value.
   *
   * @param index
   *          - the model index.
   *
   * @return the priority value.
   */
  public double evaluatePriority(int index) {
    double priorityValue = 0;
    if (priority != null && !priority.isDeleted(index)) {
      priorityValue = Evaluator.evaluateExpressionRecursive(priority.getMath(), index);
    }
    return priorityValue;
  }

  /**
   * Evaluates the event assignments.
   *
   * @param index
   *          - the model index.
   * @param time
   *          - the current simulation time.
   * @return an array with the evaluated event assignments.
   */
  public double[] computeEventAssignmentValues(int index, double time) {

    double[] assignmentValues = null;
    if (eventAssignments != null) {
      assignmentValues = new double[eventAssignments.size()];
      for (int i = 0; i < eventAssignments.size(); i++) {
        FunctionNode eventAssignmentNode = eventAssignments.get(i);
        double value = Evaluator.evaluateExpressionRecursive(eventAssignmentNode.getMath(), index);
        assignmentValues[i] = value;
      }
    }
    return assignmentValues;
  }

  /**
   * Computes the trigger value.
   *
   * @param index
   *          - the model index.
   * @return the trigger value.
   */
  public boolean computeTrigger(int index) {
    double triggerResult = Evaluator.evaluateExpressionRecursive(this, index);
    return triggerResult != 0;
  }

  /**
   * Evaluates whether the event is triggered at a specified time.
   *
   * @param time
   *          - the simulation time.
   * @param index
   *          - the model index.
   * @return true if the event evaluates to true at the given time. False otherwise.
   */
  public boolean isTriggeredAtTime(double time, int index) {
    boolean trigger = computeTrigger(index);
    EventState state = eventState.get(index);

    if (trigger) {
      if (state.maxDisabledTime >= 0 && time >= state.maxDisabledTime && time <= state.minEnabledTime) {
        state.minEnabledTime = time;
      }
      return state.maxDisabledTime >= 0 && state.minEnabledTime <= time;
    } else {
      untriggerNonPersistent(index);

      if (time > state.maxDisabledTime) {
        state.maxDisabledTime = time;
      }

      return false;
    }
  }

  /**
   * Sets the event priority.
   *
   * @param priority
   *          - the event priority node.
   */
  public void setPriority(FunctionNode priority) {
    this.priority = priority;
  }

  /**
   * Gets the event assignment nodes.
   *
   * @return the list of event assignments.
   */
  public List<FunctionNode> getEventAssignments() {
    return eventAssignments;
  }

  /**
   * Resets the state of the event.
   *
   * @param index
   *          - the model index.
   */
  public void resetEvents(int index) {
    eventState.get(index).reset();
  }

  private class EventState {
    private double maxDisabledTime;
    private double minEnabledTime;
    private boolean isInitialTrue;
    private final LinkedList<TriggeredEvent> nonPersistentEvents;

    EventState() {
      this.maxDisabledTime = Double.NEGATIVE_INFINITY;
      this.minEnabledTime = Double.POSITIVE_INFINITY;
      this.nonPersistentEvents = new LinkedList<>();
    }

    void reset() {
      if (isInitialTrue) {
        this.maxDisabledTime = Double.NEGATIVE_INFINITY;
      } else {
        this.maxDisabledTime = 0;
      }
      this.minEnabledTime = Double.POSITIVE_INFINITY;
      this.nonPersistentEvents.clear();
    }

  }
}
