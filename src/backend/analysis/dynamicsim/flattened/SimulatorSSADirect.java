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
package backend.analysis.dynamicsim.flattened;

import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import dataModels.util.MutableBoolean;
import odk.lang.FastMath;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SimulatorSSADirect extends Simulator
{

	private static Long	initializationTime	= new Long(0);

	MutableBoolean		eventsFlag			= new MutableBoolean(false);
	MutableBoolean		rulesFlag			= new MutableBoolean(false);
	MutableBoolean		constraintsFlag		= new MutableBoolean(false);

	private double		currentStep;
	private double		numSteps;

	public SimulatorSSADirect(String SBMLFileName, String outputDirectory, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies, String quantityType)
			throws IOException
	{

		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval, initializationTime, stoichAmpValue, running, interestingSpecies, quantityType);

		initialize(randomSeed, 1);
	}

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

		initializationTime += System.nanoTime() - initTime2;

		currentTime = 0.0;
		double printTime = 0;
		double oldTime = 0;
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

		while (currentTime < timeLimit && cancelFlag == false)
		{

			// if a constraint fails
			if (constraintFailureFlag == true)
			{
			  message.setErrorDialog("Simulation Canceled Due To Constraint Failure", "Constraint Failure");
        this.notifyObservers(message);
				return;
			}

			double r1 = randomNumberGenerator.nextDouble();
			double r2 = randomNumberGenerator.nextDouble();

			oldTime = currentTime;

			double delta_t = FastMath.log(1 / r1) / totalPropensity;
			double nextReactionTime = currentTime + delta_t;

			if (noEventsFlag == false)
			{

				handleEvents();
				nextEventTime = Double.POSITIVE_INFINITY;
				if (!triggeredEventQueue.isEmpty() && triggeredEventQueue.peek().fireTime <= nextEventTime)
				{
					nextEventTime = triggeredEventQueue.peek().fireTime;
				}
			}

			if (nextReactionTime < nextEventTime && nextReactionTime < currentTime + maxTimeStep)
			{
				currentTime = nextReactionTime;
			}
			else if (nextEventTime < currentTime + maxTimeStep)
			{
				currentTime = nextEventTime;
			}
			else
			{
				currentTime = currentTime + maxTimeStep;
			}

			if (currentTime > timeLimit)
			{
				currentTime = timeLimit;

				if (noEventsFlag == false)
				{
					fireEvents(noAssignmentRulesFlag, noConstraintsFlag);
				}

				performRateRules(currentTime - oldTime);

				break;
			}

			if (currentTime == nextReactionTime)
			{
				performReaction(r2, noAssignmentRulesFlag, noConstraintsFlag);

				performRateRules(currentTime - oldTime);

				printTime = print(printTime);
			}
			else if (currentTime == nextEventTime)
			{
				HashSet<String> affectedReactionSet = fireEvents(noAssignmentRulesFlag, noConstraintsFlag);

				// recalculate propensties/groups for affected reactions
				if (affectedReactionSet.size() > 0)
				{
					updatePropensities(affectedReactionSet);
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

		if (cancelFlag == false)
		{

			currentTime = timeLimit;

			if (noEventsFlag == false)
			{
				fireEvents(noAssignmentRulesFlag, noConstraintsFlag);
			}

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
	}

	/**
	 * sets up data structures local to the SSA-Direct method
	 * 
	 * @param noEventsFlag
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Override
	public void initialize(long randomSeed, int runNumber) throws IOException
	{

		setupArrays();
		setupSpecies();
		setupParameters();
		setupRules();
		setupInitialAssignments();
		setupConstraints();

		eventsFlag = new MutableBoolean(false);
		rulesFlag = new MutableBoolean(false);
		constraintsFlag = new MutableBoolean(false);

		if (numEvents == 0)
		{
			eventsFlag.setValue(true);
		}
		else
		{
			eventsFlag.setValue(false);
		}

		if (numAssignmentRules == 0)
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

		// STEP 0: calculate initial propensities (including the total)
		setupReactions();
		setupEvents();

		setupForOutput(randomSeed, runNumber);

		if (dynamicBoolean == true)
		{

			setupGrid();
			createModelCopy();
		}

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

	@Override
	protected void eraseComponentFurther(HashSet<String> reactionIDs)
	{
	}

	/**
	 * 
	 */
	@Override
	protected void updateAfterDynamicChanges()
	{

	}

	private void performReaction(double r2, boolean noAssignmentRulesFlag, boolean noConstraintsFlag)
	{
		// long step3Initial = System.nanoTime();
		String selectedReactionID = selectReaction(r2);

		// step3Time += System.nanoTime() - step3Initial;

		// if its length isn't positive then there aren't any reactions
		if (selectedReactionID.isEmpty() == false)
		{

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

			updatePropensities(affectedReactionSet);

			// step5Time += System.nanoTime() - step5Initial;
		}

		// update time for next iteration

		if (variableToIsInAssignmentRuleMap != null && variableToIsInAssignmentRuleMap.containsKey("time"))
		{
			performAssignmentRules(variableToAffectedAssignmentRuleSetMap.get("time"));
		}
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

			if (running != null && progress != null)
			{
				running.setTitle("Progress (" + (int) ((currentTime / timeLimit) * 100.0) + "%)");
				// update progress bar
				progress.setValue((int) ((currentTime / timeLimit) * 100.0));
			}

		}

		return printTime;
	}

	/**
	 * updates the propensities of the reactions affected by the recently
	 * performed reaction
	 * 
	 * @param affectedReactionSet
	 *            the set of reactions affected by the recently performed
	 *            reaction
	 */
	private void updatePropensities(HashSet<String> affectedReactionSet)
	{

		// loop through the affected reactions and update the propensities
		for (String affectedReactionID : affectedReactionSet)
		{

			boolean notEnoughMoleculesFlag = false;

			HashSet<StringDoublePair> reactantStoichiometrySet = reactionToReactantStoichiometrySetMap.get(affectedReactionID);

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
				// newPropensity =
				// CalculatePropensityIterative(affectedReactionID);
			}

			double oldPropensity = reactionToPropensityMap.get(affectedReactionID);

			// add the difference of new v. old propensity to the total
			// propensity
			totalPropensity += newPropensity - oldPropensity;

			reactionToPropensityMap.put(affectedReactionID, newPropensity);
		}
	}

	/**
	 * randomly selects a reaction to perform
	 * 
	 * @param r2
	 *            random number
	 * @return the ID of the selected reaction
	 */
	private String selectReaction(double r2)
	{

		double randomPropensity = r2 * (totalPropensity);
		double runningTotalReactionsPropensity = 0.0;
		String selectedReaction = "";

		// finds the reaction that the random propensity lies in
		// it keeps adding the next reaction's propensity to a running total
		// until the running total is greater than the random propensity
		for (String currentReaction : reactionToPropensityMap.keySet())
		{

			runningTotalReactionsPropensity += reactionToPropensityMap.get(currentReaction);

			if (randomPropensity < runningTotalReactionsPropensity)
			{
				selectedReaction = currentReaction;
				break;
			}
		}

		return selectedReaction;
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

		reactionToFormulaMap.clear();
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

		// get rid of things that were created dynamically last run
		if (dynamicBoolean == true)
		{
			resetModel();
		}
	}

	/**
	 * does minimized initalization process to prepare for a new run
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

		setupInitialAssignments();
		setupParameters();
		setupRules();
		setupConstraints();

		totalPropensity = 0.0;
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

		if (numAssignmentRules == 0)
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
		setupEvents();
		setupForOutput(0, newRun);

		if (dynamicBoolean == true)
		{

			setupGrid();
		}
	}

}
