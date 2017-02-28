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
package backend.analysis.dynamicsim.hierarchical.math;

import java.util.ArrayList;
import java.util.List;

public class EventNode extends HierarchicalNode
{
	private HierarchicalNode	delayValue;
	private HierarchicalNode	priorityValue;

	private boolean				isPersistent;
	private boolean				useTriggerValue;

	private boolean				isEnabled;
	private double				fireTime;
	private double				maxDisabledTime;
	private double				minEnabledTime;
	private double				priority;

	private double[]			assignmentValues;
	List<FunctionNode>	eventAssignments;

	public EventNode(Type type)
	{
		super(type);
		maxDisabledTime = Double.NEGATIVE_INFINITY;
		minEnabledTime = Double.POSITIVE_INFINITY;
		fireTime = Double.POSITIVE_INFINITY;
	}

	public EventNode(HierarchicalNode trigger)
	{
		super(Type.PLUS);
		this.addChild(trigger);

		maxDisabledTime = Double.NEGATIVE_INFINITY;
		minEnabledTime = Double.POSITIVE_INFINITY;
		fireTime = Double.POSITIVE_INFINITY;
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

	public boolean isEnabled()
	{
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled)
	{
		this.isEnabled = isEnabled;
	}

	public double getFireTime()
	{
		return fireTime;
	}

	public void setFireTime(double fireTime)
	{
		this.fireTime = fireTime;
	}

	public double getMaxDisabledTime()
	{
		return maxDisabledTime;
	}

	public void setMaxDisabledTime(double maxDisabledTime)
	{
		this.maxDisabledTime = maxDisabledTime;
	}

	public double getMinEnabledTime()
	{
		return minEnabledTime;
	}

	public void setMinEnabledTime(double minEnabledTime)
	{
		this.minEnabledTime = minEnabledTime;
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

		if (maxDisabledTime >= 0 && maxDisabledTime <= minEnabledTime && minEnabledTime <= time)
		{
			isEnabled = true;
			if (priorityValue != null)
			{
				priority = Evaluator.evaluateExpressionRecursive(priorityValue, 0);
			}
			if (delayValue != null)
			{
				fireTime = time + Evaluator.evaluateExpressionRecursive(delayValue, 0);
			}
			else
			{
				fireTime = time;
			}
			if (useTriggerValue)
			{
				computeEventAssignmentValues(0, time);
			}
		}

		return isEnabled;
	}

	public void fireEvent(int index, double time)
	{
		if (isEnabled && fireTime <= time)
		{
			isEnabled = false;

			maxDisabledTime = Double.NEGATIVE_INFINITY;
			minEnabledTime = Double.POSITIVE_INFINITY;

			if (!isPersistent)
			{
				if (!computeTrigger(index))
				{
					maxDisabledTime = time;
					return;
				}
			}

			if (!useTriggerValue)
			{
				computeEventAssignmentValues(index, time);
			}
			for (int i = 0; i < eventAssignments.size(); i++)
			{
				FunctionNode eventAssignmentNode = eventAssignments.get(i);
				VariableNode variable = eventAssignmentNode.getVariable();
				variable.setValue(index, assignmentValues[i]);
			}

			isTriggeredAtTime(time, index);
		}

	}

	private void computeEventAssignmentValues(int index, double time)
	{
		assignmentValues = new double[eventAssignments.size()];
		for (int i = 0; i < eventAssignments.size(); i++)
		{
			FunctionNode eventAssignmentNode = eventAssignments.get(i);
			VariableNode variable = eventAssignmentNode.getVariable();
			double value = Evaluator.evaluateExpressionRecursive(eventAssignmentNode, false, 0);
			if (variable.isSpecies())
			{
				SpeciesNode species = (SpeciesNode) variable;
				if (!species.hasOnlySubstance(index))
				{
					assignmentValues[i] = value * species.getCompartment().getValue(0);
					continue;
				}
			}
			assignmentValues[i] = value;
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
		if (trigger)
		{
			if (maxDisabledTime >= 0 && time > maxDisabledTime && time < minEnabledTime)
			{
				minEnabledTime = time;
			}
			return maxDisabledTime >= 0 && minEnabledTime <= time;
		}
		else
		{
			if (time > maxDisabledTime)
			{
				maxDisabledTime = time;
			}

			return false;
		}
	}

	public double getPriority()
	{
		if (isEnabled)
		{
			return priority;
		}
		return 0;
	}

	public HierarchicalNode getPriorityValue()
	{
		return priorityValue;
	}

	public void setPriorityValue(HierarchicalNode priorityValue)
	{
		this.priorityValue = priorityValue;
	}

	public List<FunctionNode> getEventAssignments()
	{
		return eventAssignments;
	}

	public void setEventAssignments(List<FunctionNode> eventAssignments)
	{
		this.eventAssignments = eventAssignments;
	}

	@Override
	public EventNode clone()
	{
		// TODO
		return null;
	}
}
