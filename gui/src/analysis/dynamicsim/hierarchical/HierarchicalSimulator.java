package analysis.dynamicsim.hierarchical;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Rule;

import analysis.dynamicsim.XORShiftRandom;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringPair;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;

public abstract class HierarchicalSimulator extends HierarchicalSBaseSetup{

	public HierarchicalSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, 
			JFrame running, String[] interestingSpecies, String quantityType) throws IOException, XMLStreamException 
	{
		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep, progress,
				printInterval, stoichAmpValue, running, interestingSpecies, quantityType);

	}
	
	protected void printAllToTSD(double printTime) throws IOException 
	{
		String commaSpace = "";

		getBufferedTSDWriter().write("(");

		commaSpace = "";

		//print the current time
		getBufferedTSDWriter().write(printTime + ",");

		//loop through the speciesIDs and print their current value to the file
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
				if(!models.getIsHierarchical().contains(speciesID))
				{
					getBufferedTSDWriter().write(commaSpace + models.getVariableToValue(getReplacements(), speciesID));
					commaSpace = ",";
				}
			}

			for (String noConstantParam : models.getVariablesToPrint())
			{	
				if(!models.getIsHierarchical().contains(noConstantParam))
				{
					getBufferedTSDWriter().write(commaSpace + models.getVariableToValue(getReplacements(), noConstantParam));
					commaSpace = ",";
				}
			}
		}

		getBufferedTSDWriter().write(")");
		getBufferedTSDWriter().flush();
	}

	/**
	 * opens output file and seeds rng for new run
	 * 
	 * @param randomSeed
	 * @param currentRun
	 * @throws IOException
	 */
	protected void setupForOutput(long randomSeed, int currentRun) {

		setCurrentRun(currentRun);

		setRandomNumberGenerator(new XORShiftRandom(randomSeed));

		try {

			String extension = ".tsd";

			setTSDWriter(new FileWriter(getOutputDirectory() + "run-" + currentRun + extension));
			setBufferedTSDWriter(new BufferedWriter(getTSDWriter()));
			getBufferedTSDWriter().write('(');

			if (currentRun > 1) {

				getBufferedTSDWriter().write("(" + "\"" + "time" + "\"");

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void printInterestingToTSD(double printTime) throws IOException 
	{


		String commaSpace = "";

		getBufferedTSDWriter().write("(");

		commaSpace = "";

		//print the current time
		getBufferedTSDWriter().write(printTime + ",");

		double temp;
		//loop through the speciesIDs and print their current value to the file

		for(String s : getInterestingSpecies())
		{
			String id = s.replaceAll("__[\\w]+", "");
			String element = s.replace(id+"__", "");
			ModelState ms = (id.equals(s))?getTopmodel():getSubmodels().get(id);
			if(getPrintConcentrationSpecies().contains(s))
			{
				temp = ms.getVariableToValue(getReplacements(), ms.getSpeciesToCompartmentNameMap().get(element));
				getBufferedTSDWriter().write(commaSpace + ms.getVariableToValue(getReplacements(), element)/temp);
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
	 * Returns the total propensity of all model states.
	 */
	protected double getTotalPropensity()
	{
		double totalPropensity = 0;
		totalPropensity += getTopmodel().getPropensity();

		for(ModelState model : getSubmodels().values())
		{
			totalPropensity += model.getPropensity();
		}

		return totalPropensity;
	}

	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException 
	 */
	protected void printToTSD(double printTime) throws IOException 
	{
		if(getInterestingSpecies().length == 0)
			printAllToTSD(printTime);
		else
			printInterestingToTSD(printTime);


	}
	
	protected void performReaction(ModelState modelstate, String selectedReactionID, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {

		//these are sets of things that need to be re-evaluated or tested due to the reaction firing
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		//loop through the reaction's reactants and products and update their amounts
		for (HierarchicalStringDoublePair speciesAndStoichiometry : modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(selectedReactionID)) 
		{

			double stoichiometry = speciesAndStoichiometry.doub;
			String speciesID = speciesAndStoichiometry.string;

			//this means the stoichiometry isn't constant, so look to the variableToValue map
			if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(selectedReactionID)) {

				for (HierarchicalStringPair doubleID : modelstate.getReactionToNonconstantStoichiometriesSetMap().get(selectedReactionID)) {

					//string1 is the species ID; string2 is the speciesReference ID
					if (doubleID.string1.equals(speciesID)) {

						stoichiometry = modelstate.getVariableToValue(getReplacements(), doubleID.string2);

						//this is to get the plus/minus correct, as the variableToValueMap has
						//a stoichiometry without the reactant/product plus/minus data
						stoichiometry *= (int)(speciesAndStoichiometry.doub/Math.abs(speciesAndStoichiometry.doub));
						break;
					}
				}
			}

			//update the species count if the species isn't a boundary condition or constant
			//note that the stoichiometries are earlier modified with the correct +/- sign
			boolean cond1 = modelstate.getSpeciesToIsBoundaryConditionMap().get(speciesID);
			boolean cond2 = modelstate.getVariableToIsConstantMap().get(speciesID);
			if (!cond1 && !cond2) {

				double val = modelstate.getVariableToValue(getReplacements(), speciesID) + stoichiometry;
				if(val >= 0)
					modelstate.setvariableToValueMap(getReplacements(), speciesID, val);

			}

			//if this variable that was just updated is part of an assignment rule (RHS)
			//then re-evaluate that assignment rule
			if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(speciesID) == true)
				affectedAssignmentRuleSet.addAll(modelstate.getVariableToAffectedAssignmentRuleSetMap().get(speciesID));

			if (noConstraintsFlag == false && modelstate.getVariableToIsInConstraintMap().get(speciesID) == true)
				affectedConstraintSet.addAll(modelstate.getVariableToAffectedConstraintSetMap().get(speciesID));
		}

		if (affectedAssignmentRuleSet.size() > 0)
		{
			performAssignmentRules(modelstate, affectedAssignmentRuleSet);
		}

		if (affectedConstraintSet.size() > 0) 
			//if (testConstraints(modelstate, affectedConstraintSet) == false)
			//	constraintFailureFlag = true;
			//else
			setConstraintFlag(testConstraints(modelstate, affectedConstraintSet));

	}
	protected static HashSet<String> getAffectedReactionSet(ModelState modelstate, String selectedReactionID, boolean noAssignmentRulesFlag) {

		HashSet<String> affectedReactionSet = new HashSet<String>(20);
		affectedReactionSet.add(selectedReactionID);


		//loop through the reaction's reactants and products
		for (HierarchicalStringDoublePair speciesAndStoichiometry : modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(selectedReactionID)) {

			String speciesID = speciesAndStoichiometry.string;
			affectedReactionSet.addAll(modelstate.getSpeciesToAffectedReactionSetMap().get(speciesID));

			//if the species is involved in an assignment rule then it its changing may affect a reaction's propensity
			if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(speciesID)) {

				//this assignment rule is going to be evaluated, so the rule's variable's value will change
				for (AssignmentRule assignmentRule : modelstate.getVariableToAffectedAssignmentRuleSetMap().get(speciesID)) {
					if (modelstate.getSpeciesToAffectedReactionSetMap().get(assignmentRule.getVariable())!=null) {
						affectedReactionSet.addAll(modelstate.getSpeciesToAffectedReactionSetMap()
								.get(assignmentRule.getVariable()));
					}
				}
			}
		}

		return affectedReactionSet;
	}

	protected void updateRules()
	{
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		for(ModelState model : getSubmodels().values())
			for(String element : model.getIsHierarchical())
			{
				if (model.isNoRuleFlag() == false && model.getVariableToIsInAssignmentRuleMap().get(element) == true)
					affectedAssignmentRuleSet.addAll(model.getVariableToAffectedAssignmentRuleSetMap().get(element));
				if (affectedAssignmentRuleSet.size() > 0)
					performAssignmentRules(model, affectedAssignmentRuleSet);
				if (model.isNoConstraintsFlag() == false && model.getVariableToIsInConstraintMap().get(element) == true)
					affectedConstraintSet.addAll(model.getVariableToAffectedConstraintSetMap().get(element));
				if (affectedConstraintSet.size() > 0) 
					setConstraintFlag(testConstraints(model, affectedConstraintSet));
			}
	}

	protected boolean testConstraints(ModelState modelstate, HashSet<ASTNode> affectedConstraintSet) {

		//check all of the affected constraints
		//if one evaluates to true, then the simulation halts
		for (ASTNode constraint : affectedConstraintSet) {
			//System.out.println("Node: " + libsbml.formulaToString(constraint));

			if (HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, constraint)))
				return true;
		}

		return false;
	}

	protected void handleEvents(ModelState modelstate) {

		HashSet<String> triggeredEvents = new HashSet<String>();

		//loop through all untriggered events
		//if any trigger, evaluate the fire time(s) and add them to the queue
		for (String untriggeredEventID : modelstate.getUntriggeredEventSet()) {
			//if the trigger evaluates to true
			if (HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(untriggeredEventID)))) {

				//skip the event if it's initially true and this is time == 0
				if (getCurrentTime() == 0.0 && modelstate.getEventToTriggerInitiallyTrueMap().get(untriggeredEventID) == true)
					continue;

				//switch from false to true must happen
				if (modelstate.getEventToPreviousTriggerValueMap().get(untriggeredEventID) == true)
					continue;

				triggeredEvents.add(untriggeredEventID);

				//if assignment is to be evaluated at trigger time, evaluate it and replace the ASTNode assignment
				if (modelstate.getEventToUseValuesFromTriggerTimeMap().get(untriggeredEventID) == true)	{

					//temporary hashset of evaluated assignments
					HashSet<Object> evaluatedAssignments = new HashSet<Object>();

					for (Object evAssignment : modelstate.getEventToAssignmentSetMap().get(untriggeredEventID)) {

						EventAssignment eventAssignment = (EventAssignment) evAssignment;
						evaluatedAssignments.add(new HierarchicalStringDoublePair(
								eventAssignment.getVariable(), evaluateExpressionRecursive(modelstate, eventAssignment.getMath())));
					}

					double fireTime = getCurrentTime();

					if (modelstate.getEventToHasDelayMap().get(untriggeredEventID) == true)
						fireTime += evaluateExpressionRecursive(modelstate, modelstate.getEventToDelayMap().get(untriggeredEventID));

					modelstate.getTriggeredEventQueue().add(new HierarchicalEventToFire(modelstate.getID(),
							untriggeredEventID, evaluatedAssignments, fireTime));
				}
				else {

					double fireTime =  getCurrentTime();
					
					if (modelstate.getEventToHasDelayMap().get(untriggeredEventID) == true)
						fireTime += evaluateExpressionRecursive(modelstate, modelstate.getEventToDelayMap().get(untriggeredEventID));
				
					modelstate.getTriggeredEventQueue().add(new HierarchicalEventToFire(modelstate.getID(),
							untriggeredEventID, modelstate.getEventToAssignmentSetMap().get(untriggeredEventID), fireTime));
				}			
			}
			else {

				modelstate.getEventToPreviousTriggerValueMap().put(untriggeredEventID, false);
			}
		}

		//remove recently triggered events from the untriggered set
		//when they're fired, they get put back into the untriggered set
		modelstate.getUntriggeredEventSet().removeAll(triggeredEvents);
	}



	protected boolean checkModelTriggerEvent(ModelState modelstate, double t, double[] y, HashMap<String, Integer> variableToIndexMap) {

		if(modelstate.isNoEventsFlag() == true)
			return false;

		for (String untriggeredEventID : modelstate.getUntriggeredEventSet()) 
		{
			//if the trigger evaluates to true
			if (HierarchicalUtilities.getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(untriggeredEventID), t, y, variableToIndexMap)) == true) 
			{

				//skip the event if it's initially true and this is time == 0
				if (getCurrentTime() == 0.0 && modelstate.getEventToTriggerInitiallyTrueMap().get(untriggeredEventID) == true)
					continue;

				//switch from false to true must happen
				if (modelstate.getEventToPreviousTriggerValueMap().get(untriggeredEventID) == true)
					continue;

				return true;


			}
		}

		if(modelstate.getTriggeredEventQueue().peek() != null
				&& modelstate.getTriggeredEventQueue().peek().fireTime <= t)

			return true;

		return false;
	}

	/**
	 * fires events
	 * 
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 */
	protected HashSet<String> fireEvents(ModelState modelstate, String selector, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {

		//temporary set of events to remove from the triggeredEventQueue
		HashSet<String> untriggeredEvents = new HashSet<String>();
		HashSet<String> variableInFiredEvents = new HashSet<String>();
		//loop through all triggered events
		//if the trigger is no longer true
		//remove from triggered queue and put into untriggered set
		for (HierarchicalEventToFire triggeredEvent : modelstate.getTriggeredEventQueue())
		{
			String triggeredEventID = triggeredEvent.eventID;

			//if the trigger evaluates to false
			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == false && 
					HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(triggeredEventID))) == false) {

				untriggeredEvents.add(triggeredEventID);
				modelstate.getEventToPreviousTriggerValueMap().put(triggeredEventID, false);
			}

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == true &&
					HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(triggeredEventID))) == false) {
				modelstate.getUntriggeredEventSet().add(triggeredEventID);
			}
		}

		//copy the triggered event queue -- except the events that are now untriggered
		//this is done because the remove function can't work with just a string; it needs to match events
		//this also re-evaluates the priorities in case they have changed
		//LinkedList<EventToFire> newTriggeredEventQueue = new LinkedList<EventToFire>();

		PriorityQueue<HierarchicalEventToFire> newTriggeredEventQueue = new PriorityQueue<HierarchicalEventToFire>((int)modelstate.getNumEvents(), getEventComparator());


		while (modelstate.getTriggeredEventQueue().size() > 0) {

			HierarchicalEventToFire event = modelstate.getTriggeredEventQueue().poll();

			@SuppressWarnings("unchecked")
			HierarchicalEventToFire eventToAdd = new HierarchicalEventToFire(modelstate.getID(), event.getEventID(), (HashSet<Object>) event.eventAssignmentSet.clone(), event.fireTime);

			if (untriggeredEvents.contains(event.eventID) == false)
				newTriggeredEventQueue.add(eventToAdd);
			else
				modelstate.getUntriggeredEventSet().add(event.eventID);
		}

		modelstate.setTriggeredEventQueue(newTriggeredEventQueue);

		//loop through untriggered events
		//if the trigger is no longer true
		//set the previous trigger value to false
		for (String untriggeredEventID : modelstate.getUntriggeredEventSet()) {

			if (modelstate.getEventToTriggerPersistenceMap().get(untriggeredEventID) == false && 
					HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(untriggeredEventID))) == false)
				modelstate.getEventToPreviousTriggerValueMap().put(untriggeredEventID, false);
		}

		//these are sets of things that need to be re-evaluated or tested due to the event firing
		HashSet<String> affectedReactionSet = new HashSet<String>();
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		//set of fired events to add to the untriggered set
		HashSet<String> firedEvents = new HashSet<String>();


		//fire all events whose fire time is less than the current time	
		while (modelstate.getTriggeredEventQueue().size() > 0 &&
				modelstate.getTriggeredEventQueue().peek().fireTime <=  getCurrentTime()) {

			HierarchicalEventToFire eventToFire = modelstate.getTriggeredEventQueue().poll();
			String eventToFireID = eventToFire.eventID;

			//System.err.println("firing " + eventToFireID);

			if (modelstate.getEventToAffectedReactionSetMap().get(eventToFireID) != null)
				affectedReactionSet.addAll(modelstate.getEventToAffectedReactionSetMap().get(eventToFireID));

			firedEvents.add(eventToFireID);
			//modelstate.eventToPreviousTriggerValueMap.put(eventToFireID, true);
			modelstate.getEventToPreviousTriggerValueMap().put(eventToFireID, HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(eventToFireID))) == false);


			//execute all assignments for this event
			for (Object eventAssignment : eventToFire.eventAssignmentSet) {

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
					assignmentValue = evaluateExpressionRecursive(modelstate, ((EventAssignment) eventAssignment).getMath());
				}

				variableInFiredEvents.add(variable);



				//update the species, but only if it's not a constant (bound. cond. is fine)
				if (modelstate.getVariableToIsConstantMap().get(variable) == false) {

					if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable) && 
							modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false)
						modelstate.setvariableToValueMap(getReplacements(), variable, 
								assignmentValue); //needs to fix this
					else		
						modelstate.setvariableToValueMap(getReplacements(),variable, assignmentValue);
				}

				if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(variable) == true) 
					affectedAssignmentRuleSet.addAll(modelstate.getVariableToAffectedAssignmentRuleSetMap().get(variable));
				if (noConstraintsFlag == false && modelstate.getVariableToIsInConstraintMap().get(variable) == true)
					affectedConstraintSet.addAll(modelstate.getVariableToAffectedConstraintSetMap().get(variable));

			} //end loop through assignments

			//after an event fires, need to make sure the queue is updated
			untriggeredEvents.clear();

			//loop through all triggered events
			//if they aren't persistent and the trigger is no longer true
			//remove from triggered queue and put into untriggered set
			for (HierarchicalEventToFire triggeredEvent : modelstate.getTriggeredEventQueue()) {

				String triggeredEventID = triggeredEvent.eventID;

				//if the trigger evaluates to false and the trigger isn't persistent
				if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == false  &&
						HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(triggeredEventID))) == false) {

					untriggeredEvents.add(triggeredEventID);
					modelstate.getEventToPreviousTriggerValueMap().put(triggeredEventID, false);
				}

				if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID) == true && 
						HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(triggeredEventID))) == false)
					modelstate.getUntriggeredEventSet().add(triggeredEventID);
			}

			//copy the triggered event queue -- except the events that are now untriggered
			//this is done because the remove function can't work with just a string; it needs to match events
			//this also re-evaluates the priorities in case they have changed

			newTriggeredEventQueue = new PriorityQueue<HierarchicalEventToFire>((int)modelstate.getNumEvents(), getEventComparator());

			while (modelstate.getTriggeredEventQueue().size() > 0) {

				HierarchicalEventToFire event = modelstate.getTriggeredEventQueue().poll();

				@SuppressWarnings("unchecked")
				HierarchicalEventToFire eventToAdd = 
				new HierarchicalEventToFire(modelstate.getID(), event.eventID, (HashSet<Object>) event.eventAssignmentSet.clone(), event.fireTime);

				if (untriggeredEvents.contains(event.eventID) == false)
					newTriggeredEventQueue.add(eventToAdd);
				else
					modelstate.getUntriggeredEventSet().add(event.eventID);
			}

			modelstate.setTriggeredEventQueue(newTriggeredEventQueue);

			//some events might trigger after this
			handleEvents(modelstate);
		}//end loop through event queue

		//add the fired events back into the untriggered set
		//this allows them to trigger/fire again later


		modelstate.getUntriggeredEventSet().addAll(firedEvents);
		if(selector.equals("variable"))
			return variableInFiredEvents;
		return affectedReactionSet;
	}




}
