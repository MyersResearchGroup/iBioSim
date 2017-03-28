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
package edu.utah.ece.async.analysis.dynamicsim.hierarchical.util.setup;

import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Priority;
import org.sbml.jsbml.Trigger;

import edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.EventNode;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.FunctionNode;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.VariableNode;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class EventSetup
{
	/**
	 * puts event-related information into data structures
	 */
	public static void setupEvents(HierarchicalModel modelstate, Model model)
	{
		Map<String, VariableNode> variableToNodeMap = modelstate.getVariableToNodeMap();
		for (Event event : model.getListOfEvents())
		{
			if (modelstate.isDeletedBySId(event.getId()))
			{
				continue;
			}

			if(event.isSetTrigger() && event.getTrigger().isSetMath())
			{

	      Trigger trigger = event.getTrigger();
			  ASTNode triggerMath = HierarchicalUtilities.inlineFormula(modelstate, trigger.getMath(), model);
			  HierarchicalNode triggerNode = MathInterpreter.parseASTNode(triggerMath, variableToNodeMap);

			  EventNode node = modelstate.addEvent(triggerNode);

			  boolean useValuesFromTrigger = event.getUseValuesFromTriggerTime();
			  boolean isPersistent = trigger.isSetPersistent() ? trigger.getPersistent() : false;
			  boolean initValue = trigger.isSetInitialValue() ? trigger.getInitialValue() : false;
			  setupSingleEvent(modelstate, node, event.getTrigger().getMath(), useValuesFromTrigger, initValue, isPersistent, model, variableToNodeMap);

			  if (event.isSetPriority())
			  {
			    Priority priority = event.getPriority();

			    if (!(priority.isSetMetaId() && modelstate.isDeletedByMetaId(priority.getMetaId())) && priority.isSetMath())
			    {
			      setupSinglePriority(modelstate, node, event.getPriority().getMath(), model, variableToNodeMap);
			    }

			  }
			  if (event.isSetDelay())
			  {
			    Delay delay = event.getDelay();

			    if (!(delay.isSetMetaId() && modelstate.isDeletedByMetaId(delay.getMetaId())) && delay.isSetMath())
			    {
			      setupSingleDelay(modelstate, node, event.getDelay().getMath(), model, variableToNodeMap);
			    }
			  }

			  setupEventAssignments(modelstate, node, event, model, variableToNodeMap);

			}
		}
	}

	public static void setupEventAssignments(HierarchicalModel modelstate, EventNode eventNode, Event event, Model model, Map<String, VariableNode> variableToNodeMap)
	{
		for (EventAssignment eventAssignment : event.getListOfEventAssignments())
		{
			if (eventAssignment.isSetMetaId() && modelstate.isDeletedByMetaId(eventAssignment.getMetaId()) || !eventAssignment.isSetMath())
			{
				continue;
			}

			ASTNode math = HierarchicalUtilities.inlineFormula(modelstate, eventAssignment.getMath(), model);
			HierarchicalNode assignmentNode = MathInterpreter.parseASTNode(math, variableToNodeMap);
			VariableNode variable = variableToNodeMap.get(eventAssignment.getVariable());
			FunctionNode eventAssignmentNode = new FunctionNode(variable, assignmentNode);
			eventNode.addEventAssignment(eventAssignmentNode);
		}
	}

	public static void setupSinglePriority(HierarchicalModel modelstate, EventNode node, ASTNode math, Model model, Map<String, VariableNode> variableToNodeMap)
	{
		math = HierarchicalUtilities.inlineFormula(modelstate, math, model);
		HierarchicalNode priorityNode = MathInterpreter.parseASTNode(math, variableToNodeMap);
		node.setPriorityValue(priorityNode);
	}

	public static void setupSingleDelay(HierarchicalModel modelstate, EventNode node, ASTNode math, Model model, Map<String, VariableNode> variableToNodeMap)
	{
		math = HierarchicalUtilities.inlineFormula(modelstate, math, model);
		HierarchicalNode delayNode = MathInterpreter.parseASTNode(math, variableToNodeMap);
		node.setDelayValue(delayNode);

	}

	/**
	 * sets up a single event
	 * 
	 * @param event
	 */
	public static void setupSingleEvent(HierarchicalModel modelstate, EventNode node, ASTNode trigger, boolean useFromTrigger, boolean initValue, boolean persistent, Model model, Map<String, VariableNode> variableToNodeMap)
	{
		node.setUseTriggerValue(useFromTrigger);
		node.setPersistent(persistent);

		if (!initValue)
		{
			node.setMaxDisabledTime(0);
			if (node.computeTrigger(modelstate.getIndex()))
			{
				node.setMinEnabledTime(0);
			}
		}
	}

}