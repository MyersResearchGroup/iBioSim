package analysis.dynamicsim.hierarchical.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalObjects.ModelState;
import analysis.dynamicsim.hierarchical.util.arrays.IndexObject;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalEventToFire;
import biomodel.network.GeneticNetwork;
import biomodel.parser.BioModel;
import biomodel.parser.GCMParser;

public class HierarchicalUtilities
{

	public static void alterLocalParameter(ASTNode node, String oldString, String newString)
	{
		// String reactionID = reaction.getId();
		if (node.isName() && node.getName().equals(oldString))
		{
			node.setVariable(null);
			node.setName(newString);
		}
		else
		{
			ASTNode childNode;
			for (int i = 0; i < node.getChildCount(); i++)
			{
				childNode = node.getChild(i);
				alterLocalParameter(childNode, oldString, newString);
			}
		}
	}

	public static boolean checkGrid(Model model)
	{
		if (model.getCompartment("Grid") != null)
		{
			return true;
		}
		return false;
	}

	/**
	 * fires events
	 * 
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 * @param currentTime
	 *            TODO
	 * @param replacements
	 *            TODO
	 */
	public static HashSet<String> fireEvents(ModelState modelstate, Selector selector, final boolean noAssignmentRulesFlag,
			final boolean noConstraintsFlag, double currentTime, Map<String, Double> replacements)
	{

		HashSet<String> untriggeredEvents = new HashSet<String>();
		HashSet<String> variableInFiredEvents = new HashSet<String>();
		HashSet<String> affectedReactionSet = new HashSet<String>();
		HashSet<String> affectedAssignmentRuleSet = new HashSet<String>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		checkTriggeredEvents(modelstate, untriggeredEvents, currentTime, replacements);
		updatePreviousTriggerValue(modelstate, currentTime, replacements);

		while (modelstate.getTriggeredEventQueue().size() > 0 && modelstate.getTriggeredEventQueue().peek().getFireTime() <= currentTime)
		{
			fireSingleEvent(modelstate, affectedAssignmentRuleSet, affectedConstraintSet, affectedReactionSet, variableInFiredEvents,
					untriggeredEvents, noAssignmentRulesFlag, noConstraintsFlag, currentTime, replacements);
			variableInFiredEvents.addAll(performAssignmentRules(modelstate, affectedAssignmentRuleSet, replacements, currentTime));
			handleEvents(modelstate, currentTime, replacements);
		}

		if (selector == Selector.VARIABLE)
		{
			return variableInFiredEvents;
		}

		return affectedReactionSet;
	}

	public static void getAllASTNodeChildren(ASTNode node, ArrayList<ASTNode> nodeChildrenList)
	{

		ASTNode child;
		long size = node.getChildCount();

		for (int i = 0; i < size; i++)
		{
			child = node.getChild(i);
			if (child.getChildCount() == 0)
			{
				nodeChildrenList.add(child);
			}
			else
			{
				nodeChildrenList.add(child);
				getAllASTNodeChildren(child, nodeChildrenList);
			}
		}
	}

	public static String getArrayedID(ModelState modelstate, String id, int[] indices)
	{
		for (int i = indices.length - 1; i >= 0; i--)
		{
			id = id + "[" + indices[i] + "]";
		}
		return id;
	}

	public static boolean getBooleanFromDouble(double value)
	{

		if (value == 0.0)
		{
			return false;
		}
		return true;
	}

	public static double getDoubleFromBoolean(boolean value)
	{

		if (value == true)
		{
			return 1.0;
		}
		return 0.0;
	}

	public static SBMLDocument getFlattenedRegulations(String path, String filename)
	{
		BioModel biomodel = new BioModel(path);
		biomodel.load(filename);
		GCMParser parser = new GCMParser(biomodel);
		GeneticNetwork network = parser.buildNetwork(biomodel.getSBMLDocument());
		SBMLDocument sbml = network.getSBML();
		return network.mergeSBML(filename, sbml);
	}

	public static String getIndexedObject(ModelState modelstate, String id, String variable, String attribute, int[] indices,
			Map<String, Double> replacements)
	{

		Map<String, Integer> dimensionIdMap = new HashMap<String, Integer>();
		IndexObject index = modelstate.getIndexObjects().get(id);
		Map<Integer, ASTNode> indexMap = index.getAttributes().get(attribute);
		int[] newIndices = new int[indices.length];

		for (int i = 0; i < indices.length; i++)
		{
			dimensionIdMap.put("d" + i, indices[i]);
		}

		for (int i = 0; i < newIndices.length; i++)
		{
			newIndices[i] = (int) Evaluator.evaluateExpressionRecursive(modelstate, indexMap.get(i), false, 0, null, dimensionIdMap, replacements);
		}

		return getArrayedID(modelstate, variable, newIndices);
	}

	public static double handleEvents(double currentTime, Map<String, Double> replacements, ModelState topmodel, Map<String, ModelState> submodels)
	{
		double nextEventTime = Double.POSITIVE_INFINITY;
		if (!topmodel.isNoEventsFlag())
		{
			handleEvents(topmodel, currentTime, replacements);
			if (!topmodel.getTriggeredEventQueue().isEmpty() && topmodel.getTriggeredEventQueue().peek().getFireTime() <= nextEventTime)
			{
				if (topmodel.getTriggeredEventQueue().peek().getFireTime() < nextEventTime)
				{
					nextEventTime = topmodel.getTriggeredEventQueue().peek().getFireTime();
				}
			}
		}

		for (ModelState models : submodels.values())
		{
			if (!models.isNoEventsFlag())
			{
				handleEvents(models, currentTime, replacements);
				if (!models.getTriggeredEventQueue().isEmpty() && models.getTriggeredEventQueue().peek().getFireTime() <= nextEventTime)
				{
					if (models.getTriggeredEventQueue().peek().getFireTime() < nextEventTime)
					{
						nextEventTime = models.getTriggeredEventQueue().peek().getFireTime();
					}
				}
			}
		}
		return nextEventTime;
	}

	/**
	 * inlines a formula with function definitions
	 * 
	 * @param formula
	 * @return
	 */
	public static ASTNode inlineFormula(ModelState modelstate, ASTNode formula, Map<String, Model> models, Set<String> ibiosimFunctionDefinitions)
	{
		// TODO: Avoid calling this method
		if (formula.isFunction() == false || formula.isLeaf() == false)
		{

			for (int i = 0; i < formula.getChildCount(); ++i)
			{
				formula.replaceChild(i, inlineFormula(modelstate, formula.getChild(i), models, ibiosimFunctionDefinitions));// .clone()));
			}
		}

		if (formula.isFunction() && models.get(modelstate.getModel()).getFunctionDefinition(formula.getName()) != null)
		{

			if (ibiosimFunctionDefinitions.contains(formula.getName()))
			{
				return formula;
			}

			ASTNode inlinedFormula = models.get(modelstate.getModel()).getFunctionDefinition(formula.getName()).getBody().clone();

			ASTNode oldFormula = formula.clone();

			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			HierarchicalUtilities.getAllASTNodeChildren(inlinedFormula, inlinedChildren);

			if (inlinedChildren.size() == 0)
			{
				inlinedChildren.add(inlinedFormula);
			}

			Map<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();

			for (int i = 0; i < models.get(modelstate.getModel()).getFunctionDefinition(formula.getName()).getArgumentCount(); ++i)
			{
				inlinedChildToOldIndexMap.put(models.get(modelstate.getModel()).getFunctionDefinition(formula.getName()).getArgument(i).getName(), i);
			}

			for (int i = 0; i < inlinedChildren.size(); ++i)
			{

				ASTNode child = inlinedChildren.get(i);
				if ((child.getChildCount() == 0) && child.isName())
				{

					int index = inlinedChildToOldIndexMap.get(child.getName());
					HierarchicalUtilities.replaceArgument(inlinedFormula, child.toFormula(), oldFormula.getChild(index));

					if (inlinedFormula.getChildCount() == 0)
					{
						inlinedFormula = oldFormula.getChild(index);
					}
				}
			}

			return inlinedFormula;
		}
		return formula;
	}

	public static HashSet<String> performAssignmentRules(ModelState modelstate, Set<String> affectedAssignmentRuleSet,
			Map<String, Double> replacements, double currentTime)
	{

		HashSet<String> affectedVariables = new HashSet<String>();

		for (String variable : affectedAssignmentRuleSet)
		{

			if (!modelstate.isConstant(variable))
			{

				updateVariableValue(modelstate, variable, modelstate.getAssignmentRulesList().get(variable), replacements, currentTime);

				affectedVariables.add(variable);
			}
		}

		return affectedVariables;
	}

	public static void performAssignmentRules(ModelState modelstate, Map<String, Double> replacements, double currentTime)
	{
		boolean changed = true;
		while (changed)
		{
			changed = false;

			for (String variable : modelstate.getAssignmentRulesList().keySet())
			{
				ASTNode assignmentRule = modelstate.getAssignmentRulesList().get(variable);
				if (!modelstate.isConstant(variable))
				{
					changed |= updateVariableValue(modelstate, variable, assignmentRule, replacements, currentTime);
				}
			}
		}
	}

	public static void replaceArgument(ASTNode formula, String bvar, ASTNode arg)
	{
		for (int i = 0; i < formula.getChildCount(); i++)
		{
			ASTNode child = formula.getChild(i);
			if (child.isString() && child.getName().equals(bvar))
			{
				formula.replaceChild(i, arg.clone());
			}
			else if (child.getChildCount() > 0)
			{
				replaceArgument(child, bvar, arg);
			}
		}
	}

	public static void replaceArgument(ASTNode formula, String bvar, int arg)
	{
		if (formula.isString() && formula.getName().equals(bvar))
		{
			formula.setValue(arg);
		}
		for (int i = 0; i < formula.getChildCount(); i++)
		{
			ASTNode child = formula.getChild(i);

			replaceArgument(child, bvar, arg);

		}
	}

	public static void replaceDimensionIds(ASTNode math, String prefix, int[] indices)
	{
		for (int i = 0; i < indices.length; i++)
		{
			HierarchicalUtilities.replaceArgument(math, prefix + i, indices[i]);
		}
	}

	public static void replaceSelector(ModelState modelstate, Map<String, Double> replacements, ASTNode formula)
	{
		if (formula.getType() == ASTNode.Type.FUNCTION_SELECTOR)
		{
			if (formula.getChild(0).isName())
			{
				int[] indices = new int[formula.getChildCount() - 1];
				String id = formula.getChild(0).getName();

				for (int i = 1; i < formula.getChildCount() - 1; i++)
				{
					indices[i - 1] = (int) Evaluator.evaluateExpressionRecursive(modelstate, formula.getChild(i), false, 0, null, null, replacements);
				}

				String newId = getArrayedID(modelstate, id, indices);

				formula.setName(newId);
			}
		}
		else
		{
			for (int i = 0; i < formula.getChildCount(); i++)
			{
				replaceSelector(modelstate, replacements, formula.getChild(i));
			}
		}
	}

	public static boolean testConstraints(ModelState modelstate, HashSet<ASTNode> affectedConstraintSet, double currentTime,
			Map<String, Double> replacements)
	{
		for (ASTNode constraint : affectedConstraintSet)
		{
			if (HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, constraint, false, currentTime, null,
					null, replacements)))
			{
				return true;
			}
		}

		return false;
	}

	public static boolean updateVariableValue(ModelState modelstate, String variable, ASTNode math, Map<String, Double> replacements,
			double currentTime)
	{

		boolean changed = false;
		if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable)
				&& !modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable))
		{
			double compartment = modelstate.getVariableToValue(replacements, modelstate.getSpeciesToCompartmentNameMap().get(variable));

			double oldValue = modelstate.getVariableToValue(replacements, variable);
			double newValue = Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements);

			// TODO: is this correct?
			if (oldValue != newValue)
			{
				changed = true;
				modelstate.setVariableToValue(replacements, variable, newValue * compartment);
			}
		}
		else
		{
			double oldValue = modelstate.getVariableToValue(replacements, variable);
			double newValue = Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements);

			if (oldValue != newValue)
			{
				changed = true;
				modelstate.setVariableToValue(replacements, variable,
						Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements));
			}
		}

		return changed;
	}

	private static void checkTriggeredEvents(ModelState modelstate, Set<String> untriggeredEvents, double currentTime,
			Map<String, Double> replacements)
	{
		for (HierarchicalEventToFire triggeredEvent : modelstate.getTriggeredEventQueue())
		{
			String triggeredEventID = triggeredEvent.getEventID();

			if (!modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID)
					&& !HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate
							.getEventToTriggerMap().get(triggeredEventID), false, currentTime, null, null, replacements)))
			{

				untriggeredEvents.add(triggeredEventID);
				modelstate.getEventToPreviousTriggerValueMap().put(triggeredEventID, false);
			}

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID)
					&& !HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate
							.getEventToTriggerMap().get(triggeredEventID), false, currentTime, null, null, replacements)))
			{
				modelstate.getUntriggeredEventSet().add(triggeredEventID);
			}
		}
	}

	public static boolean isEventTriggered(ModelState modelstate, String event, double t, double[] y, boolean state,
			Map<String, Integer> variableToIndexMap, Map<String, Double> replacements)
	{
		double givenState = Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(event), state, t, y,
				variableToIndexMap, replacements);

		if (givenState > 0)
		{
			return true;
		}

		return false;
	}

	private static HierarchicalEventToFire selectRandomEvent(ModelState modelstate, HierarchicalEventToFire eventToFire)
	{

		HierarchicalEventToFire nextToFire = modelstate.getTriggeredEventQueue().peek();

		List<HierarchicalEventToFire> queue = new ArrayList<HierarchicalEventToFire>();

		queue.add(eventToFire);

		while (nextToFire != null)
		{
			if (nextToFire.getFireTime() == eventToFire.getFireTime() && nextToFire.getPriority() == eventToFire.getPriority())
			{
				nextToFire = modelstate.getTriggeredEventQueue().poll();
				queue.add(nextToFire);
				nextToFire = modelstate.getTriggeredEventQueue().peek();
			}
			else
			{
				nextToFire = null;
			}
		}
		double rand = Math.random();
		int select = (int) (rand * queue.size());

		for (int i = 0; i < queue.size(); i++)
		{
			if (select == i)
			{
				nextToFire = queue.get(i);
			}
			else
			{
				modelstate.getTriggeredEventQueue().add(queue.get(i));
			}
		}

		return nextToFire;

	}

	static int	a, b, c;

	private static void fireSingleEvent(ModelState modelstate, Set<String> affectedAssignmentRuleSet, Set<ASTNode> affectedConstraintSet,
			Set<String> affectedReactionSet, Set<String> variableInFiredEvents, Set<String> untriggeredEvents, final boolean noAssignmentRulesFlag,
			final boolean noConstraintsFlag, double currentTime, Map<String, Double> replacements)
	{

		HierarchicalEventToFire eventToFire = modelstate.getTriggeredEventQueue().poll();

		eventToFire = selectRandomEvent(modelstate, eventToFire);

		String eventToFireID = eventToFire.getEventID();

		modelstate.getUntriggeredEventSet().add(eventToFireID);

		modelstate.addEventToPreviousTriggerValueMap(
				eventToFireID,
				HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate,
						modelstate.getEventToTriggerMap().get(eventToFireID), false, currentTime, null, null, replacements)));

		if (!modelstate.getEventToTriggerPersistenceMap().get(eventToFireID))
		{
			boolean state = isEventTriggered(modelstate, eventToFireID, currentTime, null, false, null, replacements);

			if (!state)
			{
				return;
			}

		}

		if (eventToFireID.equals("Rinc"))
		{
			a++;
		}
		else if (eventToFireID.equals("Qinc"))
		{
			b++;
		}
		else if (eventToFireID.equals("Tinc"))
		{
			c++;
		}
		else
		{
			System.out.println("here");
		}
		Map<String, Double> assignments = new HashMap<String, Double>();

		if (modelstate.getEventToAffectedReactionSetMap().get(eventToFireID) != null)
		{
			affectedReactionSet.addAll(modelstate.getEventToAffectedReactionSetMap().get(eventToFireID));
		}

		for (String variable : modelstate.getEventToAssignmentSetMap().get(eventToFireID).keySet())
		{
			double assignmentValue;

			if (modelstate.getEventToUseValuesFromTriggerTimeMap().get(eventToFireID))
			{
				assignmentValue = eventToFire.getEventAssignmentSet().get(variable);
			}
			else
			{
				ASTNode math = modelstate.getEventToAssignmentSetMap().get(eventToFireID).get(variable);
				assignmentValue = Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements);
			}

			variableInFiredEvents.add(variable);

			if (modelstate.isConstant(variable) == false)
			{
				assignments.put(variable, assignmentValue);
			}

			if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(variable) == true)
			{
				affectedAssignmentRuleSet.addAll(modelstate.getVariableToAffectedAssignmentRuleSetMap().get(variable));
			}
			if (noConstraintsFlag == false && modelstate.getVariableToIsInConstraintMap().get(variable) == true)
			{
				affectedConstraintSet.addAll(modelstate.getVariableToAffectedConstraintSetMap().get(variable));
			}

		}

		for (String id : assignments.keySet())
		{
			modelstate.setVariableToValue(replacements, id, assignments.get(id));
		}

		for (HierarchicalEventToFire triggeredEvent : modelstate.getTriggeredEventQueue())
		{

			String triggeredEventID = triggeredEvent.getEventID();

			if (!modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID)
					&& !HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate
							.getEventToTriggerMap().get(triggeredEventID), false, currentTime, null, null, replacements)))
			{

				untriggeredEvents.add(triggeredEventID);
				modelstate.getEventToPreviousTriggerValueMap().put(triggeredEventID, false);
			}

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID)
					&& !HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate
							.getEventToTriggerMap().get(triggeredEventID), false, currentTime, null, null, replacements)))
			{
				modelstate.getUntriggeredEventSet().add(triggeredEventID);
			}
		}
	}

	private static void handleEvents(ModelState modelstate, double currentTime, Map<String, Double> replacements)
	{
		for (String untriggeredEventID : modelstate.getUntriggeredEventSet())
		{

			boolean eventTrigger = HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate
					.getEventToTriggerMap().get(untriggeredEventID), false, currentTime, null, null, replacements));

			if (eventTrigger)
			{

				if (currentTime == 0.0 && modelstate.getEventToTriggerInitiallyTrueMap().get(untriggeredEventID))
				{
					continue;
				}

				if (modelstate.getEventToPreviousTriggerValueMap().get(untriggeredEventID))
				{
					continue;
				}

				if (modelstate.getEventToUseValuesFromTriggerTimeMap().get(untriggeredEventID))
				{
					handleEventsValueAtTrigger(modelstate, untriggeredEventID, currentTime, replacements);
				}
				else
				{
					handleEventsValueAtFire(modelstate, untriggeredEventID, currentTime, replacements);
				}
			}
			modelstate.getEventToPreviousTriggerValueMap().put(untriggeredEventID, eventTrigger);
		}

	}

	private static void handleEventsValueAtFire(ModelState modelstate, String untriggeredEventID, double currentTime, Map<String, Double> replacements)
	{
		double fireTime = currentTime;

		if (modelstate.hasDelay(untriggeredEventID))
		{
			fireTime += Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToDelayMap().get(untriggeredEventID), false,
					currentTime, null, null, replacements);
		}

		double priority;

		if (modelstate.getEventToPriorityMap().containsKey(untriggeredEventID))
		{
			priority = Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToPriorityMap().get(untriggeredEventID), false,
					currentTime, null, null, replacements);
		}
		else
		{
			priority = Double.NEGATIVE_INFINITY;
		}

		modelstate.getTriggeredEventQueue().add(new HierarchicalEventToFire(modelstate.getID(), untriggeredEventID, null, fireTime, priority));
	}

	private static void handleEventsValueAtTrigger(ModelState modelstate, String untriggeredEventID, double currentTime,
			Map<String, Double> replacements)
	{
		Map<String, Double> evaluatedAssignments = new HashMap<String, Double>();

		for (String variable : modelstate.getEventToAssignmentSetMap().get(untriggeredEventID).keySet())
		{

			ASTNode math = modelstate.getEventToAssignmentSetMap().get(untriggeredEventID).get(variable);
			double value = Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements);
			evaluatedAssignments.put(variable, value);
		}

		double fireTime = currentTime;

		if (modelstate.hasDelay(untriggeredEventID))
		{
			fireTime += Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToDelayMap().get(untriggeredEventID), false,
					currentTime, null, null, replacements);
		}

		double priority;

		if (modelstate.getEventToPriorityMap().containsKey(untriggeredEventID))
		{
			priority = Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToPriorityMap().get(untriggeredEventID), false,
					currentTime, null, null, replacements);
		}
		else
		{
			priority = Double.NEGATIVE_INFINITY;
		}

		modelstate.getTriggeredEventQueue().add(
				new HierarchicalEventToFire(modelstate.getID(), untriggeredEventID, evaluatedAssignments, fireTime, priority));
	}

	private static void updatePreviousTriggerValue(ModelState modelstate, double currentTime, Map<String, Double> replacements)
	{
		for (String untriggeredEventID : modelstate.getUntriggeredEventSet())
		{

			if (modelstate.getEventToTriggerPersistenceMap().get(untriggeredEventID) == false
					&& HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap()
							.get(untriggeredEventID), false, currentTime, null, null, replacements)) == false)
			{
				modelstate.getEventToPreviousTriggerValueMap().put(untriggeredEventID, false);
			}
		}
	}

	public static enum Selector
	{
		REACTION, VARIABLE;
	}

}
