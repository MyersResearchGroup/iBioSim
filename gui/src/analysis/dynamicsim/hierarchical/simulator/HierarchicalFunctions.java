package analysis.dynamicsim.hierarchical.simulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.EventAssignment;

import analysis.dynamicsim.hierarchical.util.Evaluator;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalEventToFire;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringDoublePair;

public abstract class HierarchicalFunctions extends HierarchicalSetup
{

	public HierarchicalFunctions(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep,
			double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies,
			String quantityType, String abstraction) throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue,
				running, interestingSpecies, quantityType, abstraction);

	}

	/**
	 * fires events
	 * 
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 */
	protected HashSet<String> fireEvents(ModelState modelstate, String selector, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag)
	{

		HashSet<String> untriggeredEvents = new HashSet<String>();
		HashSet<String> variableInFiredEvents = new HashSet<String>();
		HashSet<String> affectedReactionSet = new HashSet<String>();
		HashSet<String> affectedAssignmentRuleSet = new HashSet<String>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		checkTriggeredEvents(modelstate, untriggeredEvents);

		PriorityQueue<HierarchicalEventToFire> newTriggeredEventQueue = constructPriorityQueue(modelstate, untriggeredEvents);

		modelstate.setTriggeredEventQueue(newTriggeredEventQueue);

		updatePreviousTriggerValue(modelstate);

		while (modelstate.getTriggeredEventQueue().size() > 0 && modelstate.getTriggeredEventQueue().peek().getFireTime() <= getCurrentTime())
		{

			fireSingleEvent(modelstate, affectedAssignmentRuleSet, affectedConstraintSet, affectedReactionSet, variableInFiredEvents,
					untriggeredEvents, noAssignmentRulesFlag, noConstraintsFlag);
			handleEvents(modelstate);
		}

		if (selector.equals("variable"))
		{
			return variableInFiredEvents;
		}

		return affectedReactionSet;
	}

	protected double handleEvents()
	{
		double nextEventTime = Double.POSITIVE_INFINITY;
		if (!getTopmodel().isNoEventsFlag())
		{
			handleEvents(getTopmodel());
			if (!getTopmodel().getTriggeredEventQueue().isEmpty() && getTopmodel().getTriggeredEventQueue().peek().getFireTime() <= nextEventTime)
			{
				if (getTopmodel().getTriggeredEventQueue().peek().getFireTime() < nextEventTime)
				{
					nextEventTime = getTopmodel().getTriggeredEventQueue().peek().getFireTime();
				}
			}
		}

		for (ModelState models : getSubmodels().values())
		{
			if (!models.isNoEventsFlag())
			{
				handleEvents(models);
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

	protected void handleEvents(ModelState modelstate)
	{
		for (String untriggeredEventID : modelstate.getUntriggeredEventSet())
		{

			boolean eventTrigger = HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate
					.getEventToTriggerMap().get(untriggeredEventID), false, getCurrentTime(), null, null, getReplacements()));

			if (eventTrigger)
			{

				if (getCurrentTime() == 0.0 && modelstate.getEventToTriggerInitiallyTrueMap().get(untriggeredEventID))
				{
					continue;
				}

				if (modelstate.getEventToPreviousTriggerValueMap().get(untriggeredEventID))
				{
					continue;
				}

				if (modelstate.getEventToUseValuesFromTriggerTimeMap().get(untriggeredEventID))
				{
					handleEventsValueAtTrigger(modelstate, untriggeredEventID);
				}
				else
				{
					handleEventsValueAtFire(modelstate, untriggeredEventID);
				}
			}
			modelstate.getEventToPreviousTriggerValueMap().put(untriggeredEventID, eventTrigger);
		}

	}

	protected void printAllToTSD(double printTime) throws IOException
	{
		String commaSpace = "";

		getBufferedTSDWriter().write("(");

		commaSpace = "";

		// print the current time
		getBufferedTSDWriter().write(printTime + ",");

		// loop through the speciesIDs and print their current value to the file
		for (String speciesID : getTopmodel().getSpeciesIDSet())
		{

			getBufferedTSDWriter().write(commaSpace + getTopmodel().getVariableToValue(getReplacements(), speciesID));
			commaSpace = ",";

		}

		for (String noConstantParam : getTopmodel().getVariablesToPrint())
		{

			getBufferedTSDWriter().write(commaSpace + getTopmodel().getVariableToValue(getReplacements(), noConstantParam));
			commaSpace = ",";

		}

		for (ModelState models : getSubmodels().values())
		{
			for (String speciesID : models.getSpeciesIDSet())
			{
				if (!models.getIsHierarchical().contains(speciesID))
				{
					getBufferedTSDWriter().write(commaSpace + models.getVariableToValue(getReplacements(), speciesID));
					commaSpace = ",";
				}
			}

			for (String noConstantParam : models.getVariablesToPrint())
			{
				if (!models.getIsHierarchical().contains(noConstantParam))
				{
					getBufferedTSDWriter().write(commaSpace + models.getVariableToValue(getReplacements(), noConstantParam));
					commaSpace = ",";
				}
			}
		}

		getBufferedTSDWriter().write(")");
		getBufferedTSDWriter().flush();
	}

	protected void printInterestingToTSD(double printTime) throws IOException
	{

		String commaSpace = "";

		getBufferedTSDWriter().write("(");

		commaSpace = "";

		// print the current time
		getBufferedTSDWriter().write(printTime + ",");

		double temp;
		// loop through the speciesIDs and print their current value to the file

		for (String s : getInterestingSpecies())
		{
			String element = s.replaceAll("(.+)__", "");
			String id = s.replace("__" + element, "");
			ModelState ms = (id.equals(s)) ? getTopmodel() : getSubmodels().get(id);
			if (getPrintConcentrationSpecies().contains(s))
			{
				temp = ms.getVariableToValue(getReplacements(), ms.getSpeciesToCompartmentNameMap().get(element));
				getBufferedTSDWriter().write(commaSpace + ms.getVariableToValue(getReplacements(), element) / temp);
				commaSpace = ",";
			}
			else
			{
				getBufferedTSDWriter().write(commaSpace + ms.getVariableToValue(getReplacements(), element));
				commaSpace = ",";
			}
		}

		getBufferedTSDWriter().write(")");
		getBufferedTSDWriter().flush();
	}

	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException
	 */
	protected void printToTSD(double printTime) throws IOException
	{
		if (getInterestingSpecies().length == 0)
		{
			printAllToTSD(printTime);
		}
		else
		{
			printInterestingToTSD(printTime);
		}

	}

	/**
	 * opens output file and seeds rng for new run
	 * 
	 * @param randomSeed
	 * @param currentRun
	 * @throws IOException
	 */
	protected void setupForOutput(int currentRun)
	{

		setCurrentRun(currentRun);

		// setRandomNumberGenerator(new XORShiftRandom(randomSeed));

		try
		{

			String extension = ".tsd";

			setTSDWriter(new FileWriter(getOutputDirectory() + "run-" + currentRun + extension));
			setBufferedTSDWriter(new BufferedWriter(getTSDWriter()));
			getBufferedTSDWriter().write('(');

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void setupVariableFromTSD() throws IOException
	{
		getBufferedTSDWriter().write("(" + "\"" + "time" + "\"");

		if (getInterestingSpecies().length > 0)
		{
			for (String s : getInterestingSpecies())
			{

				getBufferedTSDWriter().write(",\"" + s + "\"");

			}

			getBufferedTSDWriter().write("),\n");

			return;
		}

		for (String speciesID : getTopmodel().getSpeciesIDSet())
		{
			getBufferedTSDWriter().write(",\"" + speciesID + "\"");
		}

		for (String noConstantParam : getTopmodel().getVariablesToPrint())
		{
			getBufferedTSDWriter().write(",\"" + noConstantParam + "\"");
		}
		/*
		 * for (String compartment : getTopmodel().compartmentIDSet) {
		 * bufferedTSDWriter.write(", \"" + compartment + "\""); }
		 */
		for (ModelState model : getSubmodels().values())
		{
			for (String speciesID : model.getSpeciesIDSet())
			{
				if (!model.getIsHierarchical().contains(speciesID))
				{
					getBufferedTSDWriter().write(",\"" + model.getID() + "__" + speciesID + "\"");
				}
			}

			for (String noConstantParam : model.getVariablesToPrint())
			{
				if (!model.getIsHierarchical().contains(noConstantParam))
				{
					getBufferedTSDWriter().write(",\"" + model.getID() + "__" + noConstantParam + "\"");
				}
			}
		}

		getBufferedTSDWriter().write("),\n");

	}

	protected boolean testConstraints(ModelState modelstate, HashSet<ASTNode> affectedConstraintSet)
	{
		for (ASTNode constraint : affectedConstraintSet)
		{
			if (HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, constraint, false, getCurrentTime(),
					null, null, getReplacements())))
			{
				return true;
			}
		}

		return false;
	}

	private void checkTriggeredEvents(ModelState modelstate, Set<String> untriggeredEvents)
	{
		for (HierarchicalEventToFire triggeredEvent : modelstate.getTriggeredEventQueue())
		{
			String triggeredEventID = triggeredEvent.getEventID();

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == false
					&& HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap()
							.get(triggeredEventID), false, getCurrentTime(), null, null, getReplacements())) == false)
			{

				untriggeredEvents.add(triggeredEventID);
				modelstate.getEventToPreviousTriggerValueMap().put(triggeredEventID, false);
			}

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == true
					&& HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap()
							.get(triggeredEventID), false, getCurrentTime(), null, null, getReplacements())) == false)
			{
				modelstate.getUntriggeredEventSet().add(triggeredEventID);
			}
		}
	}

	private PriorityQueue<HierarchicalEventToFire> constructPriorityQueue(ModelState modelstate, Set<String> untriggeredEvents)
	{
		PriorityQueue<HierarchicalEventToFire> newTriggeredEventQueue = new PriorityQueue<HierarchicalEventToFire>((int) modelstate.getNumEvents(),
				modelstate.getEventComparator());

		while (modelstate.getTriggeredEventQueue().size() > 0)
		{

			HierarchicalEventToFire event = modelstate.getTriggeredEventQueue().poll();

			HierarchicalEventToFire eventToAdd = new HierarchicalEventToFire(modelstate.getID(), event.getEventID(), (HashSet<Object>) event
					.getEventAssignmentSet().clone(), event.getFireTime());

			if (!untriggeredEvents.contains(event.getEventID()))
			{
				newTriggeredEventQueue.add(eventToAdd);
			}
			else
			{
				modelstate.getUntriggeredEventSet().add(event.getEventID());
			}
		}

		return newTriggeredEventQueue;

	}

	private void fireSingleEvent(ModelState modelstate, Set<String> affectedAssignmentRuleSet, Set<ASTNode> affectedConstraintSet,
			Set<String> affectedReactionSet, Set<String> variableInFiredEvents, Set<String> untriggeredEvents, final boolean noAssignmentRulesFlag,
			final boolean noConstraintsFlag)
	{

		HierarchicalEventToFire eventToFire = modelstate.getTriggeredEventQueue().poll();
		String eventToFireID = eventToFire.getEventID();

		if (modelstate.getEventToAffectedReactionSetMap().get(eventToFireID) != null)
		{
			affectedReactionSet.addAll(modelstate.getEventToAffectedReactionSetMap().get(eventToFireID));
		}

		modelstate.getUntriggeredEventSet().add(eventToFireID);
		modelstate.addEventToPreviousTriggerValueMap(
				eventToFireID,
				HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate,
						modelstate.getEventToTriggerMap().get(eventToFireID), false, getCurrentTime(), null, null, getReplacements())));

		Map<String, Double> assignments = new HashMap<String, Double>();

		for (Object eventAssignment : eventToFire.getEventAssignmentSet())
		{

			String variable;
			double assignmentValue;

			if (modelstate.getEventToUseValuesFromTriggerTimeMap().get(eventToFireID) == true)
			{
				variable = ((HierarchicalStringDoublePair) eventAssignment).string;
				assignmentValue = ((HierarchicalStringDoublePair) eventAssignment).doub;
			}
			else
			{
				variable = ((EventAssignment) eventAssignment).getVariable();
				assignmentValue = Evaluator.evaluateExpressionRecursive(modelstate, ((EventAssignment) eventAssignment).getMath(), false,
						getCurrentTime(), null, null, getReplacements());
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

		// Perform event assignments
		for (String id : assignments.keySet())
		{
			modelstate.setVariableToValue(getReplacements(), id, assignments.get(id));
		}

		untriggeredEvents.clear();

		for (HierarchicalEventToFire triggeredEvent : modelstate.getTriggeredEventQueue())
		{

			String triggeredEventID = triggeredEvent.getEventID();

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == false
					&& HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap()
							.get(triggeredEventID), false, getCurrentTime(), null, null, getReplacements())) == false)
			{

				untriggeredEvents.add(triggeredEventID);
				modelstate.getEventToPreviousTriggerValueMap().put(triggeredEventID, false);
			}

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == true
					&& HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap()
							.get(triggeredEventID), false, getCurrentTime(), null, null, getReplacements())) == false)
			{
				modelstate.getUntriggeredEventSet().add(triggeredEventID);
			}
		}

		PriorityQueue<HierarchicalEventToFire> newTriggeredEventQueue = new PriorityQueue<HierarchicalEventToFire>((int) modelstate.getNumEvents(),
				modelstate.getEventComparator());

		while (modelstate.getTriggeredEventQueue().size() > 0)
		{

			HierarchicalEventToFire event = modelstate.getTriggeredEventQueue().poll();

			HierarchicalEventToFire eventToAdd = new HierarchicalEventToFire(modelstate.getID(), event.getEventID(), (HashSet<Object>) event
					.getEventAssignmentSet().clone(), event.getFireTime());

			if (!untriggeredEvents.contains(event.getEventID()))
			{
				newTriggeredEventQueue.add(eventToAdd);
			}
			else
			{
				modelstate.getUntriggeredEventSet().add(event.getEventID());
			}
		}

		modelstate.setTriggeredEventQueue(newTriggeredEventQueue);
	}

	private void handleEventsValueAtFire(ModelState modelstate, String untriggeredEventID)
	{
		double fireTime = getCurrentTime();

		if (modelstate.getEventToHasDelayMap().get(untriggeredEventID) == true)
		{
			fireTime += Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToDelayMap().get(untriggeredEventID), false,
					getCurrentTime(), null, null, getReplacements());
		}

		modelstate.getTriggeredEventQueue().add(
				new HierarchicalEventToFire(modelstate.getID(), untriggeredEventID, modelstate.getEventToAssignmentSetMap().get(untriggeredEventID),
						fireTime));
	}

	private void handleEventsValueAtTrigger(ModelState modelstate, String untriggeredEventID)
	{
		HashSet<Object> evaluatedAssignments = new HashSet<Object>();

		for (Object evAssignment : modelstate.getEventToAssignmentSetMap().get(untriggeredEventID))
		{

			EventAssignment eventAssignment = (EventAssignment) evAssignment;
			evaluatedAssignments.add(new HierarchicalStringDoublePair(eventAssignment.getVariable(), Evaluator.evaluateExpressionRecursive(
					modelstate, eventAssignment.getMath(), false, getCurrentTime(), null, null, getReplacements())));
		}

		double fireTime = getCurrentTime();

		if (modelstate.getEventToHasDelayMap().get(untriggeredEventID))
		{
			fireTime += Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToDelayMap().get(untriggeredEventID), false,
					getCurrentTime(), null, null, getReplacements());
		}

		modelstate.getTriggeredEventQueue().add(new HierarchicalEventToFire(modelstate.getID(), untriggeredEventID, evaluatedAssignments, fireTime));
	}

	private void updatePreviousTriggerValue(ModelState modelstate)
	{
		for (String untriggeredEventID : modelstate.getUntriggeredEventSet())
		{

			if (modelstate.getEventToTriggerPersistenceMap().get(untriggeredEventID) == false
					&& HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap()
							.get(untriggeredEventID), false, getCurrentTime(), null, null, getReplacements())) == false)
			{
				modelstate.getEventToPreviousTriggerValueMap().put(untriggeredEventID, false);
			}
		}
	}

}
