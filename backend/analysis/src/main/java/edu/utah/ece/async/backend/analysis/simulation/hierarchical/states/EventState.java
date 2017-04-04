package edu.utah.ece.async.backend.analysis.simulation.hierarchical.states;

import edu.utah.ece.async.backend.analysis.simulation.hierarchical.math.EventNode;

public class EventState {
  private double        fireTime;
  private double        maxDisabledTime;
  private double        minEnabledTime;
  private double        priority;
  private boolean isEnabled;
  private int index;
  private EventNode parent;
  private double[]      assignmentValues;
  
  public EventState(int index, EventNode parent)
  {
    this.maxDisabledTime = Double.NEGATIVE_INFINITY;
    this.minEnabledTime = Double.POSITIVE_INFINITY;
    this.fireTime = Double.POSITIVE_INFINITY;
    this.isEnabled = false;
    this.parent = parent;
    this.index = index;
  }

  public int getIndex()
  {
    return index;
  }
  
  public double getFireTime() {
    return fireTime;
  }

  
  public void setFireTime(double fireTime) {
    this.fireTime = fireTime;
  }

  
  public double getMaxDisabledTime() {
    return maxDisabledTime;
  }

  
  public void setMaxDisabledTime(double maxDisabledTime) {
    this.maxDisabledTime = maxDisabledTime;
  }

  
  public double getMinEnabledTime() {
    return minEnabledTime;
  }

  
  public void setMinEnabledTime(double minEnabledTime) {
    this.minEnabledTime = minEnabledTime;
  }

  
  public double getPriority() {
    return priority;
  }

  
  public void setPriority(double priority) {
    this.priority = priority;
  }

  
  public boolean isEnabled() {
    return isEnabled;
  }

  
  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  
  public EventNode getParent() {
    return parent;
  }

  
  public void setParent(EventNode parent) {
    this.parent = parent;
  }

  
  public double[] getAssignmentValues() {
    return assignmentValues;
  }

  
  public void setAssignmentValues(double[] assignmentValues) {
    this.assignmentValues = assignmentValues;
  }
}
