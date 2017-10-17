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

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.TriggeredEventNode;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class EventNode extends HierarchicalNode
{
  private HierarchicalNode	delayValue;
  private HierarchicalNode	priorityValue;

  private boolean				isPersistent;
  private boolean				useTriggerValue;

  private List<FunctionNode>	eventAssignments;
  private Map<Integer, EventState> eventState;

  public EventNode(HierarchicalNode trigger)
  {
    super(Type.PLUS);
    this.addChild(trigger);

    eventState = new HashMap<Integer, EventState>();
  }

  public void addEventState(int index)
  {
    EventState state = new EventState();
    eventState.put(index, state);
  }

  public EventState getEventState(int index)
  {
    return eventState.get(index);
  }

  public HierarchicalNode getDelayValue()
  {
    return delayValue;
  }

  public void setDelayValue(HierarchicalNode delayValue)
  {
    this.delayValue = delayValue;
  }

  public boolean isPersistent()
  {
    return isPersistent;
  }

  public void setPersistent(boolean isPersistent)
  {
    this.isPersistent = isPersistent;
  }

  public boolean isUseTriggerValue()
  {
    return useTriggerValue;
  }

  public void setUseTriggerValue(boolean useTriggerValue)
  {
    this.useTriggerValue = useTriggerValue;
  }

  public double getMaxDisabledTime(int index)
  {
    return eventState.get(index).maxDisabledTime;
  }

  public void setMaxDisabledTime(int index, double maxDisabledTime)
  {
    eventState.get(index).maxDisabledTime = maxDisabledTime;
  }

  public double getMinEnabledTime(int index)
  {
    return eventState.get(index).minEnabledTime;
  }

  public void setMinEnabledTime(int index, double minEnabledTime)
  {
    eventState.get(index).minEnabledTime = minEnabledTime;
  }

  public void addTriggeredEvent(int index, TriggeredEventNode event)
  {
    eventState.get(index).nonPersistentEvents.add(event);
  }
  
  private void untriggerNonPersistent(int index)
  {
    LinkedList<TriggeredEventNode> nonPersistent = eventState.get(index).nonPersistentEvents;
    
    while(!nonPersistent.isEmpty())
    {
      TriggeredEventNode node = nonPersistent.removeFirst();
      node.setFlipped();
    }
  }
  
  public void addEventAssignment(FunctionNode eventAssignmentNode)
  {
    if (eventAssignments == null)
    {
      eventAssignments = new ArrayList<FunctionNode>();
    }

    eventAssignments.add(eventAssignmentNode);
  }

  public double evaluateFireTime(int index)
  {
    double fireTime = 0;
    if (delayValue != null)
    {
      fireTime = Evaluator.evaluateExpressionRecursive(delayValue, index);
    }
    return  fireTime;
  }

  public double evaluatePriority(int index)
  {
    double priority = 0;
    if(priorityValue != null)
    {
      priority =  Evaluator.evaluateExpressionRecursive(priorityValue, index);
    }
    return  priority;
  }
  
  public double[] computeEventAssignmentValues(int index, double time)
  {

    double[] assignmentValues = null;
    if(eventAssignments != null)
    {
      assignmentValues = new double[eventAssignments.size()];
      for (int i = 0; i < eventAssignments.size(); i++)
      {
        FunctionNode eventAssignmentNode = eventAssignments.get(i);
        VariableNode variable = eventAssignmentNode.getVariable();
        double value = Evaluator.evaluateExpressionRecursive(eventAssignmentNode, false, index);
        if (variable.isSpecies())
        {
          SpeciesNode species = (SpeciesNode) variable;
          if (!species.hasOnlySubstance())
          {
            assignmentValues[i] = value * species.getCompartment().getValue(index);
            continue;
          }
        }
        assignmentValues[i] = value;
      }
    }
    return assignmentValues;
  }

  public boolean computeTrigger(int index)
  {

    double triggerResult = Evaluator.evaluateExpressionRecursive(this, index);
    return triggerResult != 0;
  }

  public boolean isTriggeredAtTime(double time, int index)
  {
    boolean trigger = computeTrigger(index);
    EventState state = eventState.get(index);

    if (trigger)
    {
      if(state.maxDisabledTime >= 0 && time >= state.maxDisabledTime && time <= state.minEnabledTime)
      {
        state.minEnabledTime = time;
      }
      return state.maxDisabledTime >= 0 && state.minEnabledTime <= time;
    }
    else
    {
      untriggerNonPersistent(index);
      
      if (time > state.maxDisabledTime)
      {
        state.maxDisabledTime = time;
      }

      return false;
    }
  }

  public void setPriorityValue(HierarchicalNode priorityValue)
  {
    this.priorityValue = priorityValue;
  }

  public List<FunctionNode> getEventAssignments()
  {
    return eventAssignments;
  }
  
  private class EventState {
    private double        maxDisabledTime;
    private double        minEnabledTime;
    private LinkedList<TriggeredEventNode> nonPersistentEvents;
    
    public EventState()
    {
      this.maxDisabledTime = Double.NEGATIVE_INFINITY;
      this.minEnabledTime = Double.POSITIVE_INFINITY;
      this.nonPersistentEvents = new LinkedList<TriggeredEventNode>();
    }

  }
}
