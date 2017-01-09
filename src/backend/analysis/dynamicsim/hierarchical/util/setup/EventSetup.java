package backend.analysis.dynamicsim.hierarchical.util.setup;

import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Priority;
import org.sbml.jsbml.Trigger;

import backend.analysis.dynamicsim.hierarchical.math.EventNode;
import backend.analysis.dynamicsim.hierarchical.math.FunctionNode;
import backend.analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import backend.analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;

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

				if (!(priority.isSetMetaId() && modelstate.isDeletedByMetaId(priority.getMetaId())))
				{
					setupSinglePriority(modelstate, node, event.getPriority().getMath(), model, variableToNodeMap);
				}

			}
			if (event.isSetDelay())
			{
				Delay delay = event.getDelay();

				if (!(delay.isSetMetaId() && modelstate.isDeletedByMetaId(delay.getMetaId())))
				{
					setupSingleDelay(modelstate, node, event.getDelay().getMath(), model, variableToNodeMap);
				}
			}

			setupEventAssignments(modelstate, node, event, model, variableToNodeMap);

		}
	}

	public static void setupEventAssignments(HierarchicalModel modelstate, EventNode eventNode, Event event, Model model, Map<String, VariableNode> variableToNodeMap)
	{
		for (EventAssignment eventAssignment : event.getListOfEventAssignments())
		{
			if (eventAssignment.isSetMetaId() && modelstate.isDeletedByMetaId(eventAssignment.getMetaId()))
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
