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
package edu.utah.ece.async.biosim.analysis.simulation.flattened;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import edu.utah.ece.async.biosim.dataModels.util.MutableBoolean;
import odk.lang.FastMath;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SimulatorSSACR extends Simulator
{

	// allows for access to a group number from a reaction ID
	private TObjectIntHashMap<String>	reactionToGroupMap				= null;

	// allows for access to a group's min/max propensity from a group ID
	private TIntDoubleHashMap			groupToMaxValueMap				= null;

	// allows for access to the minimum/maximum possible propensity in the group
	// from a group ID
	private TIntDoubleHashMap			groupToPropensityFloorMap		= null;
	private TIntDoubleHashMap			groupToPropensityCeilingMap		= null;

	// allows for access to the reactionIDs in a group from a group ID
	private ArrayList<HashSet<String>>	groupToReactionSetList			= null;

	// allows for access to the group's total propensity from a group ID
	private TIntDoubleHashMap			groupToTotalGroupPropensityMap	= null;

	// stores group numbers that are nonempty
	private TIntHashSet					nonemptyGroupSet				= null;

	// number of groups including the empty groups and zero-propensity group
	private int							numGroups						= 0;

	private static Long					initializationTime				= new Long(0);

	MutableBoolean						eventsFlag						= new MutableBoolean(false);
	MutableBoolean						rulesFlag						= new MutableBoolean(false);
	MutableBoolean						constraintsFlag					= new MutableBoolean(false);

	private double						currentStep;
	private double						numSteps;

	public SimulatorSSACR(String SBMLFileName, String outputDirectory, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies, String quantityType) throws IOException
	{

		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval, initializationTime, stoichAmpValue, running, interestingSpecies, quantityType);

		initialize(randomSeed, 1);
//
//		SBMLDocument doc = new SBMLDocument();
//		Model model = doc.createModel("foo");
//		FunctionDefinition function = model.createFunctionDefinition("uniform");
//		Constraint c = model.createConstraint();
//		try
//		{
//			function.setMath(ASTNode.parseFormula("lambda(a,b,(a+b)/2)"));
//			c.setMath(ASTNode.parseFormula("uniform(0,1)"));
//		}
//		catch (ParseException e1)
//		{
//			e1.printStackTrace();
//		}
//
//		System.out.println(c.getMath().getType().toString());
//		doc = ArraysFlattening.convert(model.getSBMLDocument());
//		System.out.println(doc.getModel().getConstraint(0).getMath().getType().toString());
	}

	/**
	 * runs the composition and rejection simulation
	 */
	@Override
	public void simulate()
	{

		if (sbmlHasErrorsFlag == true)
		{
			return;
		}

		long initTime2 = System.nanoTime();

		final boolean noEventsFlag = (Boolean) eventsFlag.getValue();
		final boolean noAssignmentRulesFlag = (Boolean) rulesFlag.getValue();
		final boolean noConstraintsFlag = (Boolean) constraintsFlag.getValue();

		double oldTime = 0;

		initializationTime += System.nanoTime() - initTime2;
		// long initTime3 = System.nanoTime() - initTime2;

		// System.err.println("initialization time: " + initializationTime /
		// 1e9f);

		// SIMULATION LOOP
		// simulate until the time limit is reached

		// long step1Time = 0;
		// long step2Time = 0;
		// long step3aTime = 0;
		// long step3bTime = 0;
		// long step4Time = 0;
		// long step5Time = 0;
		// long step6Time = 0;

		// TObjectIntHashMap<String> reactionToTimesFired = new
		// TObjectIntHashMap<String>();

		currentTime = 0.0;
		double printTime = 0;
		double nextEventTime = Double.POSITIVE_INFINITY;
		numSteps = (int) (timeLimit / printInterval);
		currentStep = 0;

		// add events to queue if they trigger
		if (noEventsFlag == false)
		{
			handleEvents();

			HashSet<String> affectedReactionSet = fireEvents(noAssignmentRulesFlag, noConstraintsFlag);

			// recalculate propensties/groups for affected reactions
			if (affectedReactionSet.size() > 0)
			{
				updatePropensities(affectedReactionSet);
			}
		}

		printTime = print(printTime);

		// System.err.println(reactionToPropensityMap.size());

		while (currentTime < timeLimit && cancelFlag == false)
		{

			// if a constraint fails
			if (constraintFailureFlag == true)
			{

			  message.setErrorDialog("Simulation Canceled Due To Constraint Failure", "Constraint Failure");
        this.notifyObservers(message);
        return;
			}

			// prints the initial (time == 0) data
			// if (currentTime >= printTime) {
			//
			// if (printTime < 0)
			// printTime = 0.0;
			//
			// try {
			// printToTSD(printTime);
			// bufferedTSDWriter.write(",\n");
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			//
			// printTime += printInterval;
			// }
			//
			// STEP 1: generate random numbers

			// long step1Initial = System.nanoTime();

			double r1 = randomNumberGenerator.nextDouble();
			double r2 = randomNumberGenerator.nextDouble();
			double r3 = randomNumberGenerator.nextDouble();
			double r4 = randomNumberGenerator.nextDouble();

			// step1Time += System.nanoTime() - step1Initial;

			// STEP 2A: calculate delta_t, the time till the next reaction
			// execution

			// long step2Initial = System.nanoTime();
			oldTime = currentTime;
			double delta_t = FastMath.log(1 / r1) / totalPropensity;
			double nextReactionTime = currentTime + delta_t;

			// add events to queue if they trigger
			if (noEventsFlag == false)
			{

				handleEvents();
				nextEventTime = Double.POSITIVE_INFINITY;
				// step to the next event fire time if it comes before the next
				// time step
				if (!triggeredEventQueue.isEmpty() && triggeredEventQueue.peek().fireTime <= nextEventTime)
				{
					nextEventTime = triggeredEventQueue.peek().fireTime;
				}
			}

			if (nextReactionTime < nextEventTime && nextReactionTime < currentTime + maxTimeStep)
			{
				currentTime = nextReactionTime;
				// perform reaction
			}
			else if (nextEventTime < currentTime + maxTimeStep)
			{
				currentTime = nextEventTime;
				// print
			}
			else
			{
				currentTime = currentTime + maxTimeStep;
				// print
			}

			if (currentTime > timeLimit)
			{
				currentTime = timeLimit;

				fireEvents(noAssignmentRulesFlag, noConstraintsFlag);

				performRateRules(currentTime - oldTime);

				break;
			}

			if (currentTime == nextReactionTime)
			{

				// STEP 2B: calculate rate rules using this time step
				HashSet<String> affectedVariables = performRateRules(delta_t);

				// update stuff based on the rate rules altering values
				for (String affectedVariable : affectedVariables)
				{

					if (speciesToAffectedReactionSetMap != null && speciesToAffectedReactionSetMap.containsKey(affectedVariable))
					{
						updatePropensities(speciesToAffectedReactionSetMap.get(affectedVariable));
					}

					if (variableToAffectedAssignmentRuleSetMap != null && variableToAffectedAssignmentRuleSetMap.containsKey(affectedVariable))
					{
						performAssignmentRules(variableToAffectedAssignmentRuleSetMap.get(affectedVariable));
					}

					if (variableToAffectedConstraintSetMap != null && variableToAffectedConstraintSetMap.containsKey(affectedVariable))
					{
						testConstraints(variableToAffectedConstraintSetMap.get(affectedVariable));
					}
				}

				// STEP 3A: select a group

				// long step3aInitial = System.nanoTime();

				int selectedGroup = selectGroup(r2);

				// step3aTime += System.nanoTime() - step3aInitial;

				// if it's zero that means there aren't any reactions to fire
				if (selectedGroup != 0)
				{

					// STEP 3B: select a reaction within the group

					// long step3bInitial = System.nanoTime();

					String selectedReactionID = selectReaction(selectedGroup, r3, r4);

					// step3bTime += System.nanoTime() - step3bInitial;

					// System.err.println(selectedReactionID + "  " +
					// reactionToPropensityMap.get(selectedReactionID));

					// STEP 4: perform selected reaction and update species
					// counts

					// long step4Initial = System.nanoTime();

					performReaction(selectedReactionID, noAssignmentRulesFlag, noConstraintsFlag);

					// step4Time += System.nanoTime() - step4Initial;

					// STEP 5: compute affected reactions' new propensities and
					// update total propensity

					// long step5Initial = System.nanoTime();

					// create a set (precludes duplicates) of reactions that the
					// selected reaction's species affect
					HashSet<String> affectedReactionSet = getAffectedReactionSet(selectedReactionID, noAssignmentRulesFlag);

					boolean newMinPropensityFlag = updatePropensities(affectedReactionSet);

					// step5Time += System.nanoTime() - step5Initial;

					// STEP 6: re-assign affected reactions to appropriate
					// groups

					// long step6Initial = System.nanoTime();

					// if there's a new minPropensity, then the group boundaries
					// change
					// so re-calculate all groups
					if (newMinPropensityFlag == true)
					{
						reassignAllReactionsToGroups();
					}
					else
					{
						updateGroups(affectedReactionSet);
					}

					// step6Time += System.nanoTime() - step6Initial;
				}

				if (variableToIsInAssignmentRuleMap != null && variableToIsInAssignmentRuleMap.containsKey("time"))
				{
					performAssignmentRules(variableToAffectedAssignmentRuleSetMap.get("time"));
				}

				performRateRules(currentTime - oldTime);

				printTime = print(printTime);

			}

			else if (currentTime == nextEventTime)
			{
				HashSet<String> affectedReactionSet = fireEvents(noAssignmentRulesFlag, noConstraintsFlag);

				// recalculate propensties/groups for affected reactions
				if (affectedReactionSet.size() > 0)
				{

					boolean newMinPropensityFlag = updatePropensities(affectedReactionSet);

					if (newMinPropensityFlag == true)
					{
						reassignAllReactionsToGroups();
					}
					else
					{
						updateGroups(affectedReactionSet);
					}
				}

				performRateRules(currentTime - oldTime);

				printTime = print(printTime);
			}
			else
			{
				performAssignmentRules();

				performRateRules(currentTime - oldTime);

				printTime = print(printTime);
			}

		} // end simulation loop

		// System.err.println("total time: " +
		// String.valueOf((initializationTime + System.nanoTime() -
		// initTime2 - initTime3) / 1e9f));
		// System.err.println("total step 1 time: " + String.valueOf(step1Time /
		// 1e9f));
		// System.err.println("total step 2 time: " + String.valueOf(step2Time /
		// 1e9f));
		// System.err.println("total step 3a time: " + String.valueOf(step3aTime
		// / 1e9f));
		// System.err.println("total step 3b time: " + String.valueOf(step3bTime
		// / 1e9f));
		// System.err.println("total step 4 time: " + String.valueOf(step4Time /
		// 1e9f));
		// System.err.println("total step 5 time: " + String.valueOf(step5Time /
		// 1e9f));
		// System.err.println("total step 6 time: " + String.valueOf(step6Time /
		// 1e9f));

		if (cancelFlag == false)
		{

			currentTime = timeLimit;

			fireEvents(noAssignmentRulesFlag, noConstraintsFlag);

			performRateRules(currentTime - oldTime);

			// print the final species counts
			try
			{
				printToTSD(printTime);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				bufferedTSDWriter.write(')');
				bufferedTSDWriter.flush();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}

		// System.err.println("grid " + diffCount);
		// System.err.println((double) diffCount / (double) totalCount);
		// System.err.println("membrane " + memCount);
		// System.err.println((double) memCount / (double) totalCount);
		// System.err.println("total " + totalCount);
	}

	/**
	 * initializes data structures local to the SSA-CR method calculates initial
	 * propensities and assigns reactions to groups
	 * 
	 * @param noEventsFlag
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 * 
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Override
	public void initialize(long randomSeed, int runNumber) throws IOException
	{

		reactionToGroupMap = new TObjectIntHashMap<String>((int) (numReactions * 1.5));
		groupToMaxValueMap = new TIntDoubleHashMap();
		groupToPropensityFloorMap = new TIntDoubleHashMap();
		groupToPropensityCeilingMap = new TIntDoubleHashMap();
		groupToReactionSetList = new ArrayList<HashSet<String>>();
		groupToTotalGroupPropensityMap = new TIntDoubleHashMap();
		nonemptyGroupSet = new TIntHashSet();

		eventsFlag = new MutableBoolean(false);
		rulesFlag = new MutableBoolean(false);
		constraintsFlag = new MutableBoolean(false);

		setupArrays();
		setupSpecies();
		setupParameters();
		setupRules();
		setupInitialAssignments();
		setupConstraints();

		if (numEvents == 0)
		{
			eventsFlag.setValue(true);
		}
		else
		{
			eventsFlag.setValue(false);
		}

		if (numAssignmentRules == 0 && numRateRules == 0)
		{
			rulesFlag.setValue(true);
		}
		else
		{
			rulesFlag.setValue(false);
		}

		if (numConstraints == 0)
		{
			constraintsFlag.setValue(true);
		}
		else
		{
			constraintsFlag.setValue(false);
		}

		// STEP 0A: calculate initial propensities (including the total)
		setupReactions();

		// STEP OB: create and populate initial groups
		createAndPopulateInitialGroups();

		setupEvents();
		setupForOutput(randomSeed, runNumber);

		if (dynamicBoolean == true)
		{

			setupGrid();
			createModelCopy();
		}

		HashSet<String> comps = new HashSet<String>();
		comps.addAll(componentToLocationMap.keySet());

		if (dynamicBoolean == false)
		{

			bufferedTSDWriter.write("(" + "\"" + "time" + "\"");

			// if there's an interesting species, only those get printed
			if (interestingSpecies.size() > 0)
			{

				for (String speciesID : interestingSpecies)
				{
					bufferedTSDWriter.write(", \"" + speciesID + "\"");
				}
			}
			else
			{

				for (String speciesID : speciesIDSet)
				{
					bufferedTSDWriter.write(", \"" + speciesID + "\"");
				}

				// print compartment IDs (for sizes)
				for (String componentID : compartmentIDSet)
				{

					bufferedTSDWriter.write(", \"" + componentID + "\"");
				}

				// print nonconstant parameter IDs
				for (String parameterID : nonconstantParameterIDSet)
				{

					try
					{
						bufferedTSDWriter.write(", \"" + parameterID + "\"");
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}

			bufferedTSDWriter.write("),\n");

		}
	}

	/**
	 * creates the appropriate number of groups and associates reactions with
	 * groups
	 */
	private void createAndPopulateInitialGroups()
	{

		// create groups
		int currentGroup = 1;
		double groupPropensityCeiling = 2 * minPropensity;

		groupToPropensityFloorMap.put(1, minPropensity);

		while (groupPropensityCeiling < maxPropensity)
		{

			groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
			groupToPropensityFloorMap.put(currentGroup + 1, groupPropensityCeiling);
			groupToMaxValueMap.put(currentGroup, 0.0);

			groupPropensityCeiling *= 2;
			++currentGroup;
		}

		// if there are no non-zero groups
		if (minPropensity == 0)
		{

			numGroups = 1;
			groupToReactionSetList.add(new HashSet<String>(500));
		}
		else
		{

			numGroups = currentGroup + 1;

			groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
			groupToMaxValueMap.put(currentGroup, 0.0);

			// start at 0 to make a group for zero propensities
			for (int groupNum = 0; groupNum < numGroups; ++groupNum)
			{

				groupToReactionSetList.add(new HashSet<String>(500));
				groupToTotalGroupPropensityMap.put(groupNum, 0.0);
			}
		}

		// assign reactions to groups
		for (String reaction : reactionToPropensityMap.keySet())
		{

			double propensity = reactionToPropensityMap.get(reaction);
			org.openmali.FastMath.FRExpResultf frexpResult = org.openmali.FastMath.frexp((float) (propensity / minPropensity));
			int group = frexpResult.exponent;

			groupToTotalGroupPropensityMap.adjustValue(group, propensity);
			groupToReactionSetList.get(group).add(reaction);
			reactionToGroupMap.put(reaction, group);

			if (propensity > groupToMaxValueMap.get(group))
			{
				groupToMaxValueMap.put(group, propensity);
			}
		}

		// find out which (if any) groups are empty
		// this is done so that empty groups are never chosen during simulation
		for (int groupNum = 1; groupNum < numGroups; ++groupNum)
		{

			if (groupToReactionSetList.get(groupNum).isEmpty())
			{
				continue;
			}

			nonemptyGroupSet.add(groupNum);
		}
	}

	/**
	 * cancels the current run
	 */
	@Override
	public void cancel()
	{

		cancelFlag = true;
	}

	/**
	 * clears data structures for new run
	 */
	@Override
	public void clear()
	{

		variableToValueMap.clear();
		reactionToPropensityMap.clear();

		if (numEvents > 0)
		{

			triggeredEventQueue.clear();
			untriggeredEventSet.clear();
			eventToPriorityMap.clear();
			eventToDelayMap.clear();
		}

		reactionToGroupMap.clear();
		reactionToFormulaMap.clear();
		groupToMaxValueMap.clear();
		groupToPropensityFloorMap.clear();
		groupToPropensityCeilingMap.clear();
		groupToReactionSetList.clear();
		groupToTotalGroupPropensityMap.clear();
		nonemptyGroupSet.clear();
		speciesIDSet.clear();
		componentToLocationMap.clear();
		componentToReactionSetMap.clear();
		componentToVariableSetMap.clear();
		componentToEventSetMap.clear();
		compartmentIDSet.clear();
		nonconstantParameterIDSet.clear();

		minRow = Integer.MAX_VALUE;
		minCol = Integer.MAX_VALUE;
		maxRow = Integer.MIN_VALUE;
		maxCol = Integer.MIN_VALUE;

		// get rid of things that were created dynamically this run
		// or else dynamically created stuff will still be in the model next run
		if (dynamicBoolean == true)
		{
			resetModel();
		}
	}

	/**
	 * removes a component's reactions from reactionToGroupMap and
	 * groupToReactionSetList
	 */
	@Override
	protected void eraseComponentFurther(HashSet<String> reactionIDs)
	{

		for (String reactionID : reactionIDs)
		{

			int group = reactionToGroupMap.get(reactionID);
			reactionToGroupMap.remove(reactionID);
			groupToReactionSetList.get(group).remove(reactionID);
		}
	}

	/**
	 * assigns all reactions to (possibly new) groups this is called when the
	 * minPropensity changes, which changes the groups' floor/ceiling propensity
	 * values
	 */
	private void reassignAllReactionsToGroups()
	{

		int currentGroup = 1;
		double groupPropensityCeiling = 2 * minPropensity;

		// re-calulate and store group propensity floors/ceilings
		groupToPropensityCeilingMap.clear();
		groupToPropensityFloorMap.clear();
		groupToPropensityFloorMap.put(1, minPropensity);

		while (groupPropensityCeiling <= maxPropensity)
		{

			groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
			groupToPropensityFloorMap.put(currentGroup + 1, groupPropensityCeiling);

			groupPropensityCeiling *= 2;
			++currentGroup;
		}

		groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
		int newNumGroups = currentGroup + 1;

		// allocate memory if the number of groups expands
		if (newNumGroups > numGroups)
		{

			for (int groupNum = numGroups; groupNum < newNumGroups; ++groupNum)
			{
				groupToReactionSetList.add(new HashSet<String>(500));
			}
		}

		// clear the reaction set for each group
		// start at 1, as the zero propensity group isn't going to change
		for (int groupNum = 1; groupNum < newNumGroups; ++groupNum)
		{

			groupToReactionSetList.get(groupNum).clear();
			groupToMaxValueMap.put(groupNum, 0.0);
			groupToTotalGroupPropensityMap.put(groupNum, 0.0);
		}

		numGroups = newNumGroups;
		totalPropensity = 0;

		// assign reactions to groups
		for (String reaction : reactionToPropensityMap.keySet())
		{

			double propensity = reactionToPropensityMap.get(reaction);

			totalPropensity += propensity;

			// the zero-propensity group doesn't need altering
			if (propensity == 0.0)
			{

				reactionToGroupMap.put(reaction, 0);
				groupToReactionSetList.get(0).add(reaction);
			}

			org.openmali.FastMath.FRExpResultf frexpResult = org.openmali.FastMath.frexp((float) (propensity / minPropensity));
			int group = frexpResult.exponent;

			groupToReactionSetList.get(group).add(reaction);
			reactionToGroupMap.put(reaction, group);
			groupToTotalGroupPropensityMap.adjustValue(group, propensity);

			if (propensity > groupToMaxValueMap.get(group))
			{
				groupToMaxValueMap.put(group, propensity);
			}
		}

		// find out which (if any) groups are empty
		// this is done so that empty groups are never chosen during simulation

		nonemptyGroupSet.clear();

		for (int groupNum = 1; groupNum < numGroups; ++groupNum)
		{

			if (groupToReactionSetList.get(groupNum).isEmpty())
			{
				continue;
			}

			nonemptyGroupSet.add(groupNum);
		}
	}

	/**
	 * chooses a random number between 0 and the total propensity then it finds
	 * which nonempty group this number belongs to
	 * 
	 * @param r2
	 *            random number
	 * @return the group selected
	 */
	private int selectGroup(double r2)
	{

		if (nonemptyGroupSet.size() == 0)
		{
			return 0;
		}

		double randomPropensity = r2 * (totalPropensity);
		double runningTotalGroupsPropensity = 0.0;
		int selectedGroup = 1;

		// System.err.println(reactionToGroupMap);
		//
		// System.err.println(randomPropensity);
		// System.err.println(totalPropensity);
		// System.err.println();

		// finds the group that the random propensity lies in
		// it keeps adding the next group's total propensity to a running total
		// until the running total is greater than the random propensity
		for (; selectedGroup < numGroups; ++selectedGroup)
		{

			runningTotalGroupsPropensity += groupToTotalGroupPropensityMap.get(selectedGroup);

			// System.err.println(selectedGroup + "  " +
			// nonemptyGroupSet.contains(selectedGroup)
			// + "   " + groupToTotalGroupPropensityMap.get(selectedGroup));
			// System.err.println(runningTotalGroupsPropensity);

			if (randomPropensity < runningTotalGroupsPropensity && nonemptyGroupSet.contains(selectedGroup))
			{
				break;
			}
		}

		// System.err.println(selectedGroup);
		// System.err.println();

		// if (selectedGroup == 17)
		// System.err.println(nonemptyGroupSet);

		return selectedGroup;
	}

	/**
	 * from the selected group, a reaction is chosen randomly/uniformly a random
	 * number between 0 and the group's max propensity is then chosen if this
	 * number is not less than the chosen reaction's propensity, the reaction is
	 * rejected and the process is repeated until success occurs
	 * 
	 * @param selectedGroup
	 *            the group to choose a reaction from
	 * @param r3
	 * @param r4
	 * @return the chosen reaction's ID
	 */
	private String selectReaction(int selectedGroup, double r3, double r4)
	{

		HashSet<String> reactionSet = groupToReactionSetList.get(selectedGroup);

		double randomIndex = FastMath.floor(r3 * reactionSet.size());
		int indexIter = 0;
		Iterator<String> reactionSetIterator = reactionSet.iterator();

		while (reactionSetIterator.hasNext() && indexIter < randomIndex)
		{

			reactionSetIterator.next();
			++indexIter;
		}

		String selectedReactionID = reactionSetIterator.next();
		double reactionPropensity = reactionToPropensityMap.get(selectedReactionID);

		// this is choosing a value between 0 and the max propensity in the
		// group
		double randomPropensity = r4 * groupToMaxValueMap.get(selectedGroup);

		// loop until there's no reaction rejection
		// if the random propensity is higher than the selected reaction's
		// propensity, another random reaction is chosen
		while (randomPropensity > reactionPropensity)
		{

			r3 = randomNumberGenerator.nextDouble();
			r4 = randomNumberGenerator.nextDouble();

			randomIndex = (int) FastMath.floor(r3 * reactionSet.size());
			indexIter = 0;
			reactionSetIterator = reactionSet.iterator();

			while (reactionSetIterator.hasNext() && (indexIter < randomIndex))
			{

				reactionSetIterator.next();
				++indexIter;
			}

			selectedReactionID = reactionSetIterator.next();
			reactionPropensity = reactionToPropensityMap.get(selectedReactionID);
			randomPropensity = r4 * groupToMaxValueMap.get(selectedGroup);
		}

		return selectedReactionID;
	}

	/**
	 * does a minimized initialization process to prepare for a new run
	 */
	@Override
	public void setupForNewRun(int newRun)
	{

		try
		{
			setupSpecies();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		setupParameters();
		setupInitialAssignments();
		setupRules();
		setupConstraints();

		totalPropensity = 0.0;
		numGroups = 0;
		minPropensity = Double.MAX_VALUE;
		maxPropensity = Double.MIN_VALUE;

		if (numEvents == 0)
		{
			eventsFlag.setValue(true);
		}
		else
		{
			eventsFlag.setValue(false);
		}

		if (numAssignmentRules == 0 && numRateRules == 0)
		{
			rulesFlag.setValue(true);
		}
		else
		{
			rulesFlag.setValue(false);
		}

		if (numConstraints == 0)
		{
			constraintsFlag.setValue(true);
		}
		else
		{
			constraintsFlag.setValue(false);
		}

		// STEP 0A: calculate initial propensities (including the total)
		setupReactions();

		// STEP OB: create and populate initial groups
		createAndPopulateInitialGroups();

		setupEvents();
		setupForOutput(0, newRun);

		if (dynamicBoolean == true)
		{

			setupGrid();
		}
	}

	/**
	 * updates the groups
	 */
	@Override
	protected void updateAfterDynamicChanges()
	{

		reassignAllReactionsToGroups();
	}

	/**
	 * updates the groups of the reactions affected by the recently performed
	 * reaction ReassignAllReactionsToGroups() is called instead when all
	 * reactions need changing
	 * 
	 * @param affectedReactionSet
	 *            the set of reactions affected by the recently performed
	 *            reaction
	 */
	private void updateGroups(HashSet<String> affectedReactionSet)
	{

		// update the groups for all of the affected reactions
		// their propensities have changed and they may need to go into a
		// different group
		for (String affectedReactionID : affectedReactionSet)
		{

			double newPropensity = reactionToPropensityMap.get(affectedReactionID);
			int oldGroup = reactionToGroupMap.get(affectedReactionID);

			if (newPropensity == 0.0)
			{

				HashSet<String> oldReactionSet = groupToReactionSetList.get(oldGroup);

				// update group collections
				// zero propensities go into group 0
				oldReactionSet.remove(affectedReactionID);
				reactionToGroupMap.put(affectedReactionID, 0);
				groupToReactionSetList.get(0).add(affectedReactionID);

				if (oldReactionSet.size() == 0)
				{
					nonemptyGroupSet.remove(oldGroup);
				}
			}
			// if the new propensity != 0.0 (ie, new group != 0)
			else
			{
				// if it's outside of the old group's boundaries
				if (newPropensity > groupToPropensityCeilingMap.get(oldGroup) || newPropensity < groupToPropensityFloorMap.get(oldGroup))
				{

					org.openmali.FastMath.FRExpResultf frexpResult = org.openmali.FastMath.frexp((float) (newPropensity / minPropensity));
					int group = frexpResult.exponent;

					// if the group is one that currently exists
					if (group < numGroups)
					{

						HashSet<String> newGroupReactionSet = groupToReactionSetList.get(group);
						HashSet<String> oldGroupReactionSet = groupToReactionSetList.get(oldGroup);

						// update group collections
						oldGroupReactionSet.remove(affectedReactionID);
						reactionToGroupMap.put(affectedReactionID, group);
						newGroupReactionSet.add(affectedReactionID);
						groupToTotalGroupPropensityMap.adjustValue(group, newPropensity);

						// if the group that the reaction was just added to is
						// now nonempty
						if (newGroupReactionSet.size() == 1)
						{
							nonemptyGroupSet.add(group);
						}

						if (oldGroupReactionSet.size() == 0)
						{
							nonemptyGroupSet.remove(oldGroup);
						}

						if (newPropensity > groupToMaxValueMap.get(group))
						{
							groupToMaxValueMap.put(group, newPropensity);
						}
					}
					// this means the propensity goes into a group that doesn't
					// currently exist
					else
					{

						// groupToReactionSetList is a list, so the group needs
						// to be the index
						for (int iter = numGroups; iter <= group; ++iter)
						{

							if (iter >= groupToReactionSetList.size())
							{
								groupToReactionSetList.add(new HashSet<String>(500));
							}

							groupToTotalGroupPropensityMap.put(iter, 0.0);
						}

						numGroups = group + 1;

						HashSet<String> oldReactionSet = groupToReactionSetList.get(oldGroup);

						// update group collections
						groupToTotalGroupPropensityMap.adjustValue(group, newPropensity);
						groupToReactionSetList.get(oldGroup).remove(affectedReactionID);
						reactionToGroupMap.put(affectedReactionID, group);
						groupToReactionSetList.get(group).add(affectedReactionID);
						nonemptyGroupSet.add(group);
						groupToMaxValueMap.put(group, newPropensity);

						if (oldReactionSet.size() == 0)
						{
							nonemptyGroupSet.remove(oldGroup);
						}
					}
				}
				// if it's within the old group's boundaries (ie, group isn't
				// changing)
				else
				{

					// maintain current group

					if (newPropensity > groupToMaxValueMap.get(oldGroup))
					{
						groupToMaxValueMap.put(oldGroup, newPropensity);
					}

					groupToTotalGroupPropensityMap.adjustValue(oldGroup, newPropensity);
				}
			}
		}
	}

	/**
	 * updates the propensities of the reactions affected by the recently
	 * performed reaction
	 * 
	 * @param affectedReactionSet
	 *            the set of reactions affected by the recently performed
	 *            reaction
	 * @return whether or not there's a new minPropensity (if there is, all
	 *         reaction's groups need to change)
	 */
	private boolean updatePropensities(HashSet<String> affectedReactionSet)
	{

		boolean newMinPropensityFlag = false;

		// loop through the affected reactions and update the propensities
		for (String affectedReactionID : affectedReactionSet)
		{

			boolean notEnoughMoleculesFlag = false;

			HashSet<StringDoublePair> reactantStoichiometrySet = reactionToReactantStoichiometrySetMap.get(affectedReactionID);

			if (reactantStoichiometrySet == null)
			{
				continue;
			}

			// check for enough molecules for the reaction to occur
			for (StringDoublePair speciesAndStoichiometry : reactantStoichiometrySet)
			{

				String speciesID = speciesAndStoichiometry.string;
				double stoichiometry = speciesAndStoichiometry.doub;

				// if there aren't enough molecules to satisfy the stoichiometry
				if (variableToValueMap.get(speciesID) < stoichiometry)
				{
					notEnoughMoleculesFlag = true;
					break;
				}
			}

			double newPropensity = 0.0;

			if (notEnoughMoleculesFlag == false)
			{
				newPropensity = evaluateExpressionRecursive(reactionToFormulaMap.get(affectedReactionID));
			}

			// stoichiometry amplification -- alter the propensity
			if (affectedReactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
			{
				newPropensity *= (1.0 / stoichAmpGridValue);
			}

			if (newPropensity > 0.0 && newPropensity < minPropensity)
			{

				minPropensity = newPropensity;
				newMinPropensityFlag = true;
			}

			if (newPropensity > maxPropensity)
			{
				maxPropensity = newPropensity;
			}

			double oldPropensity = reactionToPropensityMap.get(affectedReactionID);
			int oldGroup = reactionToGroupMap.get(affectedReactionID);

			// if (oldGroup == 15) {
			//
			// if (newPropensity == 0)
			// System.err.println(affectedReactionID + "  NOW IS ZERO");
			//
			// if (oldPropensity == 0)
			// System.err.println(affectedReactionID +
			// "  still has propensity zero");
			//
			// System.err.println("oldgroup: " + oldGroup);
			// System.err.println(groupToTotalGroupPropensityMap.get(oldGroup) +
			// " - " + oldPropensity);
			// }

			// remove the old propensity from the group's total
			// later on, the new propensity is added to the (possibly new)
			// group's total
			groupToTotalGroupPropensityMap.adjustValue(oldGroup, -oldPropensity);

			// add the difference of new v. old propensity to the total
			// propensity
			totalPropensity += newPropensity - oldPropensity;

			reactionToPropensityMap.put(affectedReactionID, newPropensity);
		}

		return newMinPropensityFlag;
	}

	private double print(double printTime)
	{
		while (currentTime >= printTime && printTime < timeLimit)
		{

			try
			{
				printToTSD(printTime);
				bufferedTSDWriter.write(",\n");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			currentStep++;
			printTime = (currentStep * timeLimit / numSteps);

			running.setTitle("Progress (" + (int) ((currentTime / timeLimit) * 100.0) + "%)");
			// update progress bar
			progress.setValue((int) ((currentTime / timeLimit) * 100.0));

		}

		return printTime;
	}
}
