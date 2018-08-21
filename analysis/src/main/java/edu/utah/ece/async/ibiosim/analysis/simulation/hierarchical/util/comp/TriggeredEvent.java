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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.FunctionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;

/**
 * Container for events that have been triggered.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class TriggeredEvent implements Comparable<TriggeredEvent> {
  private double priority;
  private double[] assignmentValues;
  private final int index;
  private final double fireTime;
  private final EventNode parent;
  private boolean hasFlipped;

  public TriggeredEvent(int index, double fireTime, EventNode parent) {
    this.parent = parent;
    this.index = index;
    this.hasFlipped = false;
    this.fireTime = fireTime;
  }

  /**
   * Gets the index of the model that had the event triggered.
   *
   * @return the model index.
   */
  public int getIndex() {
    return index;
  }

  /**
   * Gets the firing time of the triggered event.
   *
   * @return the fire time.
   */
  public double getFireTime() {
    return fireTime;
  }

  /**
   * Gets the triggered event priority.
   *
   * @return the priority.
   */
  public double getPriority() {
    return priority;
  }

  /**
   * Sets the priority of the triggered event.
   *
   * @param priority
   *          - the new priority.
   */
  public void setPriority(double priority) {
    this.priority = priority;
  }

  /**
   * Gets the reference to the triggered event node.
   *
   * @return the event node.
   */
  public EventNode getParent() {
    return parent;
  }

  /**
   * Gets the assignment values.
   *
   * @return the assignment values.
   */
  public double[] getAssignmentValues() {
    return assignmentValues;
  }

  /**
   * Checks if the event has its triggering condition changing to false before firing.
   */
  public void setFlipped() {
    hasFlipped = true;
  }

  /**
   * Sets the event assignment values.
   *
   * @param assignmentValues
   *          - the evaluated event assignments.
   */
  public void setAssignmentValues(double[] assignmentValues) {
    this.assignmentValues = assignmentValues;
  }

  /**
   * Fire the event and perform the event assignments.
   *
   * @param time
   *          - current time.
   */
  public void fireEvent(double time) {

    if (fireTime <= time) {

      if (!parent.getState().getChild(index).isPersistent()) {
        if (hasFlipped) { return; }
      }

      if (!parent.getState().getChild(index).isUseTriggerValue()) {
        double[] eventAssignments = parent.computeEventAssignmentValues(index, time);

        if (eventAssignments != null) {
          setAssignmentValues(eventAssignments);
        }
      }

      if (assignmentValues != null) {
        for (int i = 0; i < parent.getEventAssignments().size(); i++) {
          FunctionNode eventAssignmentNode = parent.getEventAssignments().get(i);
          if (!eventAssignmentNode.isDeleted(index)) {
            HierarchicalNode variable = eventAssignmentNode.getVariable();
            variable.setValue(index, assignmentValues[i]);
          }
        }
      }
    }

  }

  @Override
  public int compareTo(TriggeredEvent event2) {
    if (this.getFireTime() > event2.getFireTime()) {
      return 1;
    } else if (this.getFireTime() < event2.getFireTime()) {
      return -1;
    } else {
      if (this.getPriority() > event2.getPriority()) {
        return -1;
      } else if (this.getPriority() < event2.getPriority()) {
        return 1;
      } else {
        return Math.random() >= 0.5 ? 1 : -1;
      }

    }
  }
}