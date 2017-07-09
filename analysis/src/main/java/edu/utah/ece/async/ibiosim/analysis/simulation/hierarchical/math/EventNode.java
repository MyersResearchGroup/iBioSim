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
import java.util.List;
import java.util.Map;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.EventState;

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

  List<FunctionNode>	eventAssignments;

  Map<Integer, EventState> eventState;

  public EventNode(HierarchicalNode trigger)
  {
    super(Type.PLUS);
    this.addChild(trigger);

    eventState = new HashMap<Integer, EventState>();
  }

  public EventState addEventState(int index)
  {
    EventState state = new EventState(index, this);
    eventState.put(index, state);
    return state;
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

  public boolean isEnabled(int index)
  {
    return eventState.get(index).isEnabled();
  }

  public void setEnabled(int index, boolean isEnabled)
  {
    eventState.get(index).setEnabled(isEnabled);
  }

  public double getFireTime(int index)
  {
    return eventState.get(index).getFireTime();
  }

  public void setFireTime(int index, double fireTime)
  {
    eventState.get(index).setFireTime(fireTime);
  }

  public double getMaxDisabledTime(int index)
  {
    return eventState.get(index).getMaxDisabledTime();
  }

  public void setMaxDisabledTime(int index, double maxDisabledTime)
  {
    eventState.get(index).setMaxDisabledTime(maxDisabledTime);
  }

  public double getMinEnabledTime(int index)
  {
    return eventState.get(index).getMinEnabledTime();
  }

  public void setMinEnabledTime(int index, double minEnabledTime)
  {
    eventState.get(index).setMinEnabledTime(minEnabledTime);
  }

  public void addEventAssignment(FunctionNode eventAssignmentNode)
  {
    if (eventAssignments == null)
    {
      eventAssignments = new ArrayList<FunctionNode>();
    }

    eventAssignments.add(eventAssignmentNode);
  }

  public boolean computeEnabled(int index, double time)
  {

    EventState state = eventState.get(index);
    if (state.getMaxDisabledTime() >= 0 && state.getMaxDisabledTime() <= state.getMinEnabledTime() && state.getMinEnabledTime() <= time)
    {
      state.setEnabled(true);
      if (priorityValue != null)
      {
        state.setPriority(Evaluator.evaluateExpressionRecursive(priorityValue, index));
      }
      double fireTime = time;
      if (delayValue != null)
      {
        fireTime += Evaluator.evaluateExpressionRecursive(delayValue, index);
      }
      state.setFireTime(fireTime);
      if (useTriggerValue)
      {
        computeEventAssignmentValues(index, time);
      }
    }

    return state.isEnabled();
  }

  public void fireEvent(int index, double time)
  {
    EventState state = eventState.get(index);
    if (state.isEnabled() && state.getFireTime() <= time)
    {
      state.setEnabled(false);
      state.setPriority(0);

      state.setMaxDisabledTime(Double.NEGATIVE_INFINITY);
      state.setMinEnabledTime(Double.POSITIVE_INFINITY);


      if (isPersistent)
      {
        if (!computeTrigger(index))
        {
          state.setMaxDisabledTime(time);
          return;
        }
      }

      if (useTriggerValue)
      {
        if(eventAssignments != null)
        {
          double[] assignmentValues = state.getAssignmentValues();
          for (int i = 0; i < eventAssignments.size(); i++)
          {
            FunctionNode eventAssignmentNode = eventAssignments.get(i);
            VariableNode variable = eventAssignmentNode.getVariable();
            variable.setValue(index, assignmentValues[i]);
          }
        }
      }
      else
      {
        if(eventAssignments != null)
        {
          for(FunctionNode node : eventAssignments)
          {
            node.computeFunction(index);
          } 
        }
      }


      isTriggeredAtTime(time, index);
    }

  }

  private void computeEventAssignmentValues(int index, double time)
  {
    if(eventAssignments != null)
    {
      double[] assignmentValues = new double[eventAssignments.size()];
      eventState.get(index).setAssignmentValues(assignmentValues);
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
      if(state.getMaxDisabledTime() >= 0 && time >= state.getMaxDisabledTime() && time <= state.getMinEnabledTime())
      {
        state.setMinEnabledTime(time);
      }
      return state.getMaxDisabledTime() >= 0 && state.getMinEnabledTime() <= time;
    }
    else
    {
      if (time > state.getMaxDisabledTime())
      {
        state.setMaxDisabledTime(time);
      }

      return false;
    }
  }

  public double getPriority(int index)
  {
    EventState state = eventState.get(index);
    return state.getPriority();
  }


  public void setPriorityValue(HierarchicalNode priorityValue)
  {
    this.priorityValue = priorityValue;
  }

  public List<FunctionNode> getEventAssignments()
  {
    return eventAssignments;
  }
}
