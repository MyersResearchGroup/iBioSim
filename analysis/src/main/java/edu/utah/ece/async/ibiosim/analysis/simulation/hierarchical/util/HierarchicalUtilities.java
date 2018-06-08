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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility functions for the Hierarchical Simulator.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HierarchicalUtilities {

	/**
	 * File separator constant.
	 */
	public static final String separator = (File.separator.equals("\\")) ? "\\\\" : File.separator;

	/**
	 * iBioSim-specific function definitions for distributions.
	 */
	public static final Set<String> ibiosimFunctionDefinitions = new HashSet<>(Arrays.asList("uniform", "exponential", "gamma", "chisq", "lognormal", "laplace", "cauchy", "poisson", "binomial", "bernoulli", "normal"));

	/**
	 * Get progress status as a percentage to completion.
	 *
	 * @param totalRuns
	 *          - the number of runs
	 * @param currentRun
	 *          - the current run index
	 * @param currentTime
	 *          - current simulation time
	 * @param timeLimit
	 *          - the time limit of each run
	 *
	 * @return the completion percentage
	 */
	public static int getPercentage (int totalRuns, int currentRun, double currentTime, double timeLimit) {
		if (totalRuns == 1) {
			double timePerc = currentTime / timeLimit;
			return (int) (timePerc * 100);
		} else {
			double runPerc = 1.0 * currentRun / totalRuns;
			return (int) (runPerc * 100);
		}
	}

}
