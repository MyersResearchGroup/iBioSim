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
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;

public class TriggeredEventNode 
{
  private double priority;
  private double fireTime;
  private double[] assignmentValues;
  private int index;
  private EventNode parent;
  private boolean hasFlipped;
  
  public TriggeredEventNode(int index, EventNode parent)
  {
    this.fireTime = Double.POSITIVE_INFINITY;
    this.parent = parent;
    this.index = index;
    this.hasFlipped = false;
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

  public double getPriority() {
    return priority;
  }

  
  public void setPriority(double priority) {
    this.priority = priority;
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

  public void setFlipped()
  {
      hasFlipped = true;
  }
  
  public boolean getFlipped()
  {
    return hasFlipped;
  }
  
  public void setAssignmentValues(double[] assignmentValues) {
    this.assignmentValues = assignmentValues;
  }
  
  public void fireEvent(int index, double time)
  {
    
    if (fireTime <= time)
    {

      if (!parent.getState().getState(index).isPersistent())
      {
        if (hasFlipped)
        {
          return;
        }
      }

      if (!parent.getState().getState(index).isUseTriggerValue())
      {
        double[] eventAssignments = parent.computeEventAssignmentValues(index, time);
        
        if(eventAssignments != null)
        {
          setAssignmentValues(eventAssignments);  
        }
      }
      
      if(assignmentValues != null)
      {
        for (int i = 0; i < parent.getEventAssignments().size(); i++)
        {
          FunctionNode eventAssignmentNode = parent.getEventAssignments().get(i);
          VariableNode variable = eventAssignmentNode.getVariable();
          variable.setValue(index, assignmentValues[i]);
        }
      }
    }

  }
}