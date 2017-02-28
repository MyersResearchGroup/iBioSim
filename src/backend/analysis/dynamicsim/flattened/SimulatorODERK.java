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
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;

import dataModels.util.MutableBoolean;
import flanagan.integration.DerivnFunction;
import flanagan.integration.RungeKutta;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SimulatorODERK extends Simulator
{

	private static Long			initializationTime	= new Long(0);

	MutableBoolean				eventsFlag			= new MutableBoolean(false);
	MutableBoolean				rulesFlag			= new MutableBoolean(false);
	MutableBoolean				constraintsFlag		= new MutableBoolean(false);

	String[]					variables;
	double[]					values;
	ASTNode[]					dvariablesdtime;
	HashMap<String, Integer>	variableToIndexMap;
	HashMap<Integer, String>	indexToVariableMap;

	int							numSteps;
	double						relativeError;
	double						absoluteError;

	public SimulatorODERK(String SBMLFileName, String outputDirectory, double timeLimit, double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies, int numSteps, double relError, double absError,
			String quantityType) throws IOException
	{

		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, 0.0, randomSeed, progress, printInterval, initializationTime, stoichAmpValue, running, interestingSpecies, quantityType);

		this.numSteps = numSteps;
		relativeError = relError;
		absoluteError = absError;

		initialize(randomSeed, 1);
	}

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

		if (dynamicBoolean == true)
		{
			setupGrid();
		}

		setupForOutput(randomSeed, runNumber);

		variables = new String[variableToValueMap.size()];
		values = new double[variableToValueMap.size()];
		dvariablesdtime = new ASTNode[variableToValueMap.size()];
		variableToIndexMap = new HashMap<String, Integer>(variableToValueMap.size());
		indexToVariableMap = new HashMap<Integer, String>(variableToValueMap.size());

		int index = 0;

		// convert variableToValueMap into two arrays
		// and create a hashmap to find indices
		for (String variable : variableToValueMap.keySet())
		{

			variables[index] = variable;
			values[index] = variableToValueMap.get(variable);
			variableToIndexMap.put(variable, index);
			dvariablesdtime[index] = new ASTNode();
			dvariablesdtime[index].setValue(0);
			indexToVariableMap.put(index, variable);
			++index;
		}

		// create system of ODEs for the change in variables
		for (String reaction : reactionToFormulaMap.keySet())
		{

			ASTNode formula = reactionToFormulaMap.get(reaction);
			// System.out.println("HERE: " + formula.toFormula());

			HashSet<StringDoublePair> reactantAndStoichiometrySet = reactionToReactantStoichiometrySetMap.get(reaction);
			HashSet<StringDoublePair> speciesAndStoichiometrySet = reactionToSpeciesAndStoichiometrySetMap.get(reaction);

			// loop through reactants
			for (StringDoublePair reactantAndStoichiometry : reactantAndStoichiometrySet)
			{

				String reactant = reactantAndStoichiometry.string;
				double stoichiometry = reactantAndStoichiometry.doub;
				int varIndex = variableToIndexMap.get(reactant);
				ASTNode stoichNode = new ASTNode();
				stoichNode.setValue(-1 * stoichiometry);

				dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex], ASTNode.times(formula, stoichNode));
			}

			// loop through products
			for (StringDoublePair speciesAndStoichiometry : speciesAndStoichiometrySet)
			{

				String species = speciesAndStoichiometry.string;
				double stoichiometry = speciesAndStoichiometry.doub;

				// if it's a product its stoichiometry will be positive
				// (and if it's a reactant it'll be negative)
				if (stoichiometry > 0)
				{

					int varIndex = variableToIndexMap.get(species);
					ASTNode stoichNode = new ASTNode();
					stoichNode.setValue(stoichiometry);

					dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex], ASTNode.times(formula, stoichNode));
				}
			}
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

				if (dynamicBoolean == true)
				{

					// print compartment location IDs
					for (String componentLocationID : componentToLocationMap.keySet())
					{

						String locationX = componentLocationID + "__locationX";
						String locationY = componentLocationID + "__locationY";

						bufferedTSDWriter.write(", \"" + locationX + "\", \"" + locationY + "\"");
					}
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
	public void cancel()
	{

		cancelFlag = true;
	}

	@Override
	public void clear()
	{

	}

	@Override
	protected void eraseComponentFurther(HashSet<String> reactionIDs)
	{

	}

	@Override
	public void setupForNewRun(int newRun)
	{

	}

	@Override
	public void simulate()
	{
		int currSteps = 0;

		if (sbmlHasErrorsFlag == true)
		{
			return;
		}

		final boolean noEventsFlag = (Boolean) eventsFlag.getValue();
		final boolean noAssignmentRulesFlag = (Boolean) rulesFlag.getValue();
		final boolean noConstraintsFlag = (Boolean) constraintsFlag.getValue();

		double printTime = -0.0001;
		double stepSize = 0.0001;
		double nextEndTime = 0.0;
		currentTime = 0.0;

		if (absoluteError == 0)
		{
			absoluteError = 1e-9;
		}
		if (relativeError == 0)
		{
			relativeError = 1e-6;
		}
		if (stepSize > Double.MAX_VALUE)
		{
			stepSize = 0.01;
		}
		if (numSteps == 0)
		{
			numSteps = 50;
		}

		// create runge-kutta instance
		DerivnFunc derivnFunction = new DerivnFunc();
		RungeKutta rungeKutta = new RungeKutta();
		rungeKutta.setStepSize(stepSize);

		// absolute error
		rungeKutta.setToleranceAdditionFactor(absoluteError);
		// relative error
		rungeKutta.setToleranceScalingFactor(relativeError);
		// rungeKutta.setMaximumIterations(numSteps);

		// add events to queue if they trigger
		if (noEventsFlag == false)
		{
			handleEvents();
		}

		while (printTime <= timeLimit && cancelFlag == false)
		{

			// if a constraint fails
			if (constraintFailureFlag == true)
			{
				message.setErrorDialog("Simulation Canceled Due To Constraint Failure", "Constraint Failure");
	      this.notifyObservers(message);
				return;
			}

			// EVENT HANDLING
			// trigger and/or fire events, etc.
			if (noEventsFlag == false)
			{

				// if events fired, then the affected species counts need to be
				// updated in the values array
				int sizeBefore = triggeredEventQueue.size();
				fireEvents(noAssignmentRulesFlag, noConstraintsFlag);
				int sizeAfter = triggeredEventQueue.size();

				if (sizeAfter != sizeBefore)
				{
					for (int i = 0; i < values.length; ++i)
					{
						values[i] = variableToValueMap.get(indexToVariableMap.get(i));
					}
				}
			}

			// prints the initial (time == 0) data
			if (printTime < 0)
			{

				printTime = 0.0;

				try
				{
					printToTSD(printTime);
					bufferedTSDWriter.write(",\n");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				currSteps++;
				printTime = (currSteps * timeLimit / numSteps);
				// printTime += printInterval;
			}

			nextEndTime = currentTime + maxTimeStep;
			if (nextEndTime > printTime)
			{
				nextEndTime = printTime;
			}

			// set rk values
			rungeKutta.setInitialValueOfX(currentTime);
			rungeKutta.setFinalValueOfX(nextEndTime);
			rungeKutta.setInitialValuesOfY(values);

			currentTime = nextEndTime;

			// STEP 2B: calculate rate rules using this time step
			HashSet<String> affectedVariables = performRateRules(stepSize);

			// update stuff based on the rate rules altering values
			for (String affectedVariable : affectedVariables)
			{

				if (variableToAffectedAssignmentRuleSetMap != null && variableToAffectedAssignmentRuleSetMap.containsKey(affectedVariable))
				{
					performAssignmentRules(variableToAffectedAssignmentRuleSetMap.get(affectedVariable));
				}

				if (variableToAffectedConstraintSetMap != null && variableToAffectedConstraintSetMap.containsKey(affectedVariable))
				{
					testConstraints(variableToAffectedConstraintSetMap.get(affectedVariable));
				}

				for (int i = 0; i < values.length; ++i)
				{

					if (affectedVariable.equals(indexToVariableMap.get(i)))
					{
						values[i] = variableToValueMap.get(indexToVariableMap.get(i));
					}
				}
			}

			if (variableToIsInAssignmentRuleMap != null && variableToIsInAssignmentRuleMap.containsKey("time"))
			{

				performAssignmentRules(variableToAffectedAssignmentRuleSetMap.get("time"));

				for (int i = 0; i < values.length; ++i)
				{
					values[i] = variableToValueMap.get(indexToVariableMap.get(i));
				}
			}

			// System.err.println(variableToValueMap);

			// call the rk algorithm
			values = rungeKutta.fehlberg(derivnFunction);

			// TSD PRINTING
			// this prints the previous timestep's data
			while ((currentTime >= printTime) && (printTime <= timeLimit))
			{

				try
				{
					printToTSD(printTime);

					if (printTime < timeLimit)
					{
						bufferedTSDWriter.write(",\n");
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				currSteps++;
				printTime = (currSteps * timeLimit / numSteps);
				// printTime += printInterval;
				if (running != null)
				{
					running.setTitle("Progress (" + (int) ((currentTime / timeLimit) * 100.0) + "%)");
				}
			}

			if (progress != null)
			{
				progress.setValue((int) ((printTime / timeLimit) * 100.0));
			}

			if (noEventsFlag == false)
			{
				handleEvents();
				if (!triggeredEventQueue.isEmpty() && triggeredEventQueue.peek().fireTime <= currentTime)
				{
					currentTime = triggeredEventQueue.peek().fireTime;
				}
			}
		}

		try
		{
			bufferedTSDWriter.write(')');
			bufferedTSDWriter.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void updateAfterDynamicChanges()
	{

	}

	private class DerivnFunc implements DerivnFunction
	{

		/**
		 * in this context, x is the time and y is the values array this method
		 * is called by the rk algorithm and returns the evaluated derivatives
		 * of the ODE system it needs to return the changes in values for y (ie,
		 * its length is the same)
		 */
		@Override
		public double[] derivn(double x, double[] y)
		{

			double[] currValueChanges = new double[y.length];
			HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();

			for (int i = 0; i < y.length; ++i)
			{
				variableToValueMap.put(indexToVariableMap.get(i), y[i]);
			}

			// calculate the current variable values
			// based on the ODE system
			for (int i = 0; i < currValueChanges.length; ++i)
			{

				String currentVar = indexToVariableMap.get(i);

				if ((speciesIDSet.contains(currentVar) && speciesToIsBoundaryConditionMap.get(currentVar) == false) && (variableToValueMap.contains(currentVar)) && variableToIsConstantMap.get(currentVar) == false)
				{

					currValueChanges[i] = evaluateExpressionRecursive(dvariablesdtime[i]);
					// if (currValueChanges[i]!=0) {
					// System.out.println(indexToVariableMap.get(i) + "= " +
					// dvariablesdtime[i].toFormula() + "=" +
					// currValueChanges[i]);
					// }
				}
				else
				{
					currValueChanges[i] = 0;
				}

				if (variableToIsInAssignmentRuleMap != null && variableToIsInAssignmentRuleMap.containsKey(currentVar) && variableToValueMap.contains(currentVar) && variableToIsInAssignmentRuleMap.get(currentVar) == true)
				{
					affectedAssignmentRuleSet.addAll(variableToAffectedAssignmentRuleSetMap.get(currentVar));
				}

				// if (variableToIsInConstraintMap.get(speciesID) == true)
				// affectedConstraintSet.addAll(variableToAffectedConstraintSetMap.get(speciesID));
			}

			// if assignment rules are performed, these changes need to be
			// reflected in the currValueChanges
			// that get passed back
			if (affectedAssignmentRuleSet.size() > 0)
			{

				HashSet<String> affectedVariables = performAssignmentRules(affectedAssignmentRuleSet);

				for (String affectedVariable : affectedVariables)
				{

					int index = variableToIndexMap.get(affectedVariable);
					currValueChanges[index] = variableToValueMap.get(affectedVariable) - y[index];
				}
			}

			// for (int i = 0; i < currValueChanges.length; ++i)
			// System.err.println(currValueChanges[i]);

			return currValueChanges;
		}
	}
}
