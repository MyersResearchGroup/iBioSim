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
package edu.utah.ece.async.backend.learn.parameterestimator.methods.sres;

import java.util.Arrays;

/**
 * A class representing a real multivariate objective function and constraints
 * that are subject to optimization. The objective functionmethod is left for
 * the user to specify. Arbitrary value range is allowed for objective function.
 * Constraints are provided as { constraint_value <= 0} inequalities, the
 * computation of array should be implemented by user.
 * 
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class Objective
{
	private final int	numberOfFeatures;
	private final double[]	featureLowerBounds, featureUpperBounds;
	private final double[]	mutationRates;
	private final boolean	maximizationProblem;

	/**
	 * Generates an objective function instance and specifies the feature
	 * (argument) space.
	 * 
	 * @param featureLowerBounds
	 *            lower bounds of feature space
	 * @param featureUpperBounds
	 *            upper bounds of feature space
	 * @param maximizationProblem
	 *            if set to true, the objective function is subject to
	 *            maximization task, minimization will be performed otherwise
	 */
	public Objective(Modelsettings Ms)
	{

		// this.numberOfFeatures = featureLowerBounds.length;
		this.numberOfFeatures = Ms.lowerBounds.length;

		if (Ms.upperBounds.length != numberOfFeatures)
		{
			throw new IllegalArgumentException("Lengths of upper and lower bounds should match.");
		}
		this.featureLowerBounds = Ms.lowerBounds;
		this.featureUpperBounds = Ms.upperBounds;
		for (int i = 0; i < numberOfFeatures; i++)
		{
			if (featureUpperBounds[i] < featureLowerBounds[i])
			{
				throw new IllegalArgumentException("Feature upper bound is smaller than lower bound for index " + i + ".");
			}
		}
		this.maximizationProblem = Ms.verbose;

		this.mutationRates = new double[numberOfFeatures];
		double factor = Math.sqrt(numberOfFeatures);

		for (int i = 0; i < numberOfFeatures; i++)
		{
			mutationRates[i] = (featureUpperBounds[i] - featureLowerBounds[i]) / factor;
		}
	}

	/**
	 * Generate a random feature array based on feature space constraints.
	 * 
	 * @return a random array from feature space
	 */
	double[] generateFeatureVector()
	{
		double[] features = new double[numberOfFeatures];

		for (int i = 0; i < numberOfFeatures; i++)
		{
			double x = SRES.random.nextDouble();
			features[i] = featureLowerBounds[i] + x * (featureUpperBounds[i] - featureLowerBounds[i]);
		}

		return features;
	}

	/**
	 * Gets the mutation rates for { SRES} algorithm.
	 * 
	 * @return an array of feature mutation rates
	 */
	final double[] getMutationRates()
	{
		return mutationRates;
	}

	/**
	 * Check whether a given feature value belongs to feature space.
	 * 
	 * @param feature
	 *            feature value
	 * @param index
	 *            feature index
	 * @return true if feature value is in specified bounds, false otherwise
	 */
	final boolean inBounds(double feature, int index)
	{
		return featureLowerBounds[index] <= feature && feature <= featureUpperBounds[index];
	}

	/**
	 * A user-implemented function that computes the value of objective function
	 * and constraint values using provided features (parameters).
	 * 
	 * @param features
	 *            an array of objective function features
	 * @return objective function evaluation result
	 * @see com.antigenomics.jsres.Objective.Result
	 */
	public abstract Result evaluate(double[] features);

	/**
	 * Gets the dimensionality of feature (parameter) space.
	 * 
	 * @return length of feature arrays
	 */
	public final int getNumberOfFeatures()
	{
		return numberOfFeatures;
	}

	public double[] getfeatureLowerBounds()
	{
		return featureLowerBounds;
	}

	public double[] getfeatureUpperBounds()
	{
		return featureUpperBounds;
	}

	/**
	 * Tells if the problem is maximization or minimization one.
	 * 
	 * @return true for maximization task, false for minimization
	 */
	public boolean isMaximizationProblem()
	{
		return maximizationProblem;
	}

	/**
	 * An object containing results of objective function and constraint
	 * evaluation.
	 */
	public class Result
	{
		private final double	value, penalty;
		private final double[]	constraintValues;

		/**
		 * Creates a new instance of objective function evaluation result.
		 * 
		 * @param value
		 *            objective function value
		 */
		public Result(double value)
		{
			this.value = value;
			this.constraintValues = new double[0];
			this.penalty = 0;
		}

		/**
		 * Creates a new instance of objective function evaluation result.
		 * Constraints should be re-written as {constraint_value <= 0}
		 * inequalities and {@code constraint_value} array should be provided.
		 * 
		 * @param value
		 *            objective function value
		 * @param constraintValues
		 *            constraint values
		 */
		public Result(double value, double[] constraintValues)
		{
			this.value = value;
			this.constraintValues = constraintValues;
			double penalty = 0;
			for (double p : constraintValues)
			{
				double pp = Math.max(0, p);
				penalty += pp * pp;
			}
			this.penalty = penalty;
		}

		/**
		 * Get the array of constraint values.
		 * 
		 * @return constraint value array
		 */
		public double[] getConstraintValues()
		{
			return constraintValues;
		}

		/**
		 * Gets the objective function value.
		 * 
		 * @return objective function value
		 */
		public double getValue()
		{
			return value;
		}

		/**
		 * Gets the { SRES} penalty computed from objective values. Penalty
		 * value is computed as {@code sum(max(0, constraint_value)^2}.
		 * 
		 * @return penalty value
		 */
		double getPenalty()
		{
			return penalty;
		}

		/**
		 * Gets the parent objective function instance.
		 * 
		 * @return parent objective function instance
		 */
		public Objective getObjective()
		{
			return Objective.this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return "RESULT objective function value is " + value + ", constraint values are " + Arrays.toString(constraintValues);
		}
	}
}
