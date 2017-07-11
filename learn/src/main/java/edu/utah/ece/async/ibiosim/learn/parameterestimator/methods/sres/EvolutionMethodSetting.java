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
package edu.utah.ece.async.ibiosim.learn.parameterestimator.methods.sres;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class EvolutionMethodSetting
{

	boolean	verbose;
	int		lambda;
	int		mu;
	// double tau;
	double	expectedConvergenceRate;
	int		numberOfSweeps;
	double	rankingPenalizationFactor;
	double	tauDash;
	int		numberOfgenerations;

	/**
	 * Creates evolution method setting using default values.
	 */
	public EvolutionMethodSetting()
	{
		this(true, 200, 30, 1.0, 200, 1000, 0.45);
	}

	/**
   * Creates evolution method setting.
   * 
	 * @param verbose - if set to true, will print the number of generations
   * passed and other statistics to {@code stderr}
	 */
	public EvolutionMethodSetting(boolean verbose)
	{
		this(verbose, 200, 30, 1.0, 200, 1000, 0.45);
	}

	/**
	 * Creates evolution method setting.
	 * 
	 * @param verbose - if set to true, will print the number of generations
   * passed and other statistics to {@code stderr}
	 * @param lambda - solution set size.
	 */
	public EvolutionMethodSetting(boolean verbose, int lambda)
	{
		this(verbose, lambda, 30, 1.0, 200, 1000, 0.45);
	}

	/**
	 * Creates evolution method setting.
	 * 
	 * @param verbose- if set to true, will print the number of generations
   * passed and other statistics to {@code stderr}
	 * @param lambda - solution set size.
	 * @param mu - number of top-ranking solutions selected to produce new
   * solution set at each generation.
	 */
	public EvolutionMethodSetting(boolean verbose, int lambda, int mu)
	{
		this(verbose, lambda, mu, 1.0, 200, 1000, 0.45);
	}

	/**
   * Creates evolution method setting.
   * 
   * @param verbose- if set to true, will print the number of generations
   * passed and other statistics to {@code stderr}
   * @param lambda - solution set size.
   * @param mu - number of top-ranking solutions selected to produce new
   * solution set at each generation.
   * @param expectedConvergenceRate expected convergence rate
   */
	public EvolutionMethodSetting(boolean verbose, int lambda, int mu, double expectedConvergenceRate)
	{
		this(verbose, lambda, mu, expectedConvergenceRate, 200, 1000, 0.45);
	}

	 /**
   * Creates evolution method setting.
   * 
   * @param verbose- if set to true, will print the number of generations
   * passed and other statistics to {@code stderr}
   * @param lambda - solution set size.
   * @param mu - number of top-ranking solutions selected to produce new
   * solution set at each generation.
   * @param expectedConvergenceRate expected convergence rate
   * @param numberOfSweeps number of times stochastic ranking bubble-sort is
   * applied to solution set
   */
	public EvolutionMethodSetting(boolean verbose, int lambda, int mu, double expectedConvergenceRate, int numberOfSweeps)
	{
		this(verbose, lambda, mu, expectedConvergenceRate, numberOfSweeps, 1000, 0.45);
	}
	
  /**
  * Creates evolution method setting.
  * 
  * @param verbose- if set to true, will print the number of generations
   * passed and other statistics to {@code stderr}
  * @param lambda - solution set size.
  * @param mu - number of top-ranking solutions selected to produce new
  * solution set at each generation.
  * @param expectedConvergenceRate - expected convergence rate
  * @param numberOfSweeps - number of times stochastic ranking bubble-sort is
  * applied to solution set
  * @param numberofgenerations - number of generations
  */
	public EvolutionMethodSetting(boolean verbose, int lambda, int mu, double expectedConvergenceRate, int numberOfSweeps, int numberofgenerations)
	{
		this(verbose, lambda, mu, expectedConvergenceRate, numberOfSweeps, numberofgenerations, 0.45);
	}

  /**
  * Creates evolution method setting.
  * 
  * @param verbose- if set to true, will print the number of generations
   * passed and other statistics to {@code stderr}
  * @param lambda - solution set size.
  * @param mu - number of top-ranking solutions selected to produce new
  * solution set at each generation.
  * @param expectedConvergenceRate - expected convergence rate
  * @param numberOfSweeps - number of times stochastic ranking bubble-sort is
  * applied to solution set
  * @param numberofgenerations - number of generations
  * @param rankingPenalizationFactor - constraint breaking penalization factor,
  * should be in { [0, 1]};
  */
	public EvolutionMethodSetting(boolean verbose, int lambda, int mu, double expectedConvergenceRate, int numberOfSweeps, int numberofgenerations, double rankingPenalizationFactor)
	{
		this.verbose = verbose;
		this.lambda = lambda;
		this.mu = mu;
		// this.tau = expectedConvergenceRate / Math.sqrt(2 *
		// Math.sqrt(numberOfFeatures));
		// this.tauDash = expectedConvergenceRate / Math.sqrt(2 *
		// numberOfFeatures);
		this.numberOfSweeps = numberOfSweeps;
		this.rankingPenalizationFactor = rankingPenalizationFactor;
		this.numberOfgenerations = numberofgenerations;
		this.expectedConvergenceRate = expectedConvergenceRate;
	}

	/**
	 * Returns verbose flag.
	 * 
	 * @return verbose flag.
	 */
	public boolean getverbose()
	{
		return verbose;
	}

	/**
	 * Returns lambda.
	 * 
	 * @return lambda.
	 */
	public int getlamda()
	{
		return lambda;
	}

	/**
	 * Returns mu.
	 * 
	 * @return mu.
	 */
	public int getmu()
	{
		return mu;
	}

	/**
	 * Returns the expected convergence rate.
	 * 
	 * @return the expected convergence rate.
	 */
	public double getexpectedConvergenceRate()
	{
		return expectedConvergenceRate;
	}

	/**
	 * Returns the number of sweeps.
	 * 
	 * @return the number of sweeps.
	 */
	public int getnumberOfSweeps()
	{
		return numberOfSweeps;
	}

	/**
	 * Returns ranking penalization factor.
	 * 
	 * @return - ranking penalization factor.
	 */
	public double getrankingPenalizationFactor()
	{
		return rankingPenalizationFactor;
	}

	/**
	 * Returns the number of generations.
	 * 
	 * @return number of generations.
	 */
	public double getnumberOfgenerations()
	{
		return numberOfgenerations;

	}
}
