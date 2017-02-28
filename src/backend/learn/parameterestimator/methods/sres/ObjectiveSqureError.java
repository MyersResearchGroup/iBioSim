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
package backend.learn.parameterestimator.methods.sres;

import static java.lang.Math.abs;

import java.util.List;

import backend.analysis.dynamicsim.hierarchical.HierarchicalSimulation;
import backend.analysis.dynamicsim.hierarchical.methods.HierarchicalODERKSimulator;
import backend.learn.genenet.Experiments;
import backend.learn.genenet.SpeciesCollection;

public class ObjectiveSqureError extends Objective
{
	private final double			valueAtSolution;
	private final double			absolutePrecision, relativePrecision;
	private final int				allowedViolatedConstraintsCount;
	private final Modelsettings		Models;

	private Experiments				experiment;
	private List<String>			listOfParams;
	private String[]				speciesCollection;
	private HierarchicalSimulation	sim;

	/*
	 * public TestProblem(double[] featureUpperBounds, double[]
	 * featureLowerBounds, boolean maximizationProblem, double valueAtSolution)
	 * { this(featureUpperBounds, featureLowerBounds, maximizationProblem,
	 * valueAtSolution, 0); }
	 * 
	 * public TestProblem(double[] featureUpperBounds, double[]
	 * featureLowerBounds, boolean maximizationProblem, double valueAtSolution,
	 * int allowedViolatedConstraintsCount) { this(featureUpperBounds,
	 * featureLowerBounds, maximizationProblem, valueAtSolution,
	 * allowedViolatedConstraintsCount, 1e-6, 1e-3); }
	 */

	public ObjectiveSqureError(HierarchicalSimulation sim, Experiments experiments, List<String> parameterList, SpeciesCollection speciesCollection, Modelsettings Ms, double valueAtSolution)
	{
		super(Ms);
		this.valueAtSolution = valueAtSolution;
		this.relativePrecision = 0;
		this.allowedViolatedConstraintsCount = 0;
		this.absolutePrecision = 0;
		this.Models = Ms;
		this.experiment = experiments;
		this.listOfParams = parameterList;
		this.speciesCollection = new String[speciesCollection.size() + 1];
		for (String species : speciesCollection.getInterestingSpecies())
		{
			this.speciesCollection[speciesCollection.getColumn(species)] = species;
		}
		this.sim = sim;
	}

	public ObjectiveSqureError(Modelsettings Ms, double valueAtSolution, int allowedViolatedConstraintsCount, double absolutePrecision, double relativePrecision)
	{
		super(Ms);
		this.valueAtSolution = valueAtSolution;
		this.absolutePrecision = absolutePrecision;
		this.relativePrecision = relativePrecision;
		this.allowedViolatedConstraintsCount = allowedViolatedConstraintsCount;
		this.Models = Ms;
	}

	@Override
	public Result evaluate(double[] features)
	{
		double sum = 0;
		List<List<Double>> experiment = this.experiment.getExperiments().get(0);

		HierarchicalODERKSimulator odeSim = (HierarchicalODERKSimulator) sim;

		for (int i = 1; i < speciesCollection.length; i++)
		{
			odeSim.setTopLevelValue(speciesCollection[i], experiment.get(0).get(i));
		}

		for (int i = 0; i < listOfParams.size(); i++)
		{
			odeSim.setTopLevelValue(listOfParams.get(i), features[i]);
		}
		for (int i = 0; i < experiment.size() - 1; i++)
		{

			odeSim.setTimeLimit(experiment.get(i + 1).get(0));
			odeSim.simulate();
			for (int j = 1; j < speciesCollection.length; j++)
			{
				double tmp = odeSim.getTopLevelValue(speciesCollection[j]) - experiment.get(i + 1).get(j);
				tmp = tmp * tmp;
				sum = sum + tmp;
			}

		}

		odeSim.setCurrentTime(0);

		odeSim.setTimeLimit(0);

		odeSim.setupForNewRun(0);

		return new Result(sum);
	}

	public boolean isSolved(SRES.Solution solution)
	{
		Objective.Result result = solution.getObjectiveResult();

		int violatedConstraints = 0;

		for (int i = 0; i < result.getConstraintValues().length; i++)
		{
			if (result.getConstraintValues()[i] > 0)
			{
				violatedConstraints++;
			}
		}

		double absoluteError = abs(valueAtSolution - result.getValue()), relativeError = absoluteError / abs(valueAtSolution);
		System.out.print(allowedViolatedConstraintsCount);
		System.out.print(absolutePrecision);
		System.out.print(valueAtSolution);
		System.out.print(violatedConstraints <= allowedViolatedConstraintsCount && absoluteError <= absolutePrecision && (abs(valueAtSolution) < relativePrecision || relativeError <= relativePrecision));
		return violatedConstraints <= allowedViolatedConstraintsCount && absoluteError <= absolutePrecision && (abs(valueAtSolution) < relativePrecision || relativeError <= relativePrecision);

	}
}
