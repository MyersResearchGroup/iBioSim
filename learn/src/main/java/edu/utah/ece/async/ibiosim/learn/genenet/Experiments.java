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
package edu.utah.ece.async.ibiosim.learn.genenet;

import java.util.ArrayList;
import java.util.List;

/**
 * This object holds the experimental data that is used for learning.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Experiments
{

	private List<List<List<Double>>>	experiments;

	/**
	 * Creates an Experiments object.
	 */
	public Experiments()
	{
		experiments = new ArrayList<List<List<Double>>>();
	}

	/**
	 * Adds a value to a certain experiment table.
	 * 
	 * @param experiment - the experiment that the data is coming from.
	 * @param row - the row that data is being inserted to.
	 * @param col - the column that the data is being inserted to.
	 * @param data - the value that is being inserted.
	 */
	public void addExperiment(int experiment, int row, int col, double data)
	{
		while (experiments.size() < experiment + 1)
		{
			experiments.add(new ArrayList<List<Double>>());
		}
		while (experiments.get(experiment).size() < row + 1)
		{
			experiments.get(experiment).add(new ArrayList<Double>());
		}
		while (experiments.get(experiment).get(row).size() < col + 1)
		{
			experiments.get(experiment).get(row).add(0.0);
		}
		experiments.get(experiment).get(row).set(col, data);
	}

	/**
	 * Get a data point from the data table of a given experiment.
	 * 
	 * @param experiment - the experiment you are getting data from.
	 * @param row - the row of the data you want.
	 * @param col - the column of the data you want.
	 * @return the value at the specified index of the given experiment.
	 */
	public double getDataPoint(int experiment, int row, int col)
	{
		return experiments.get(experiment).get(row).get(col);
	}

	/**
	 * Remove mutations. Not supported yet.
	 * 
	 * @param s - a species that you want to remove mutations from.
	 * @return a new Experiments object without mutations.
	 */
	public Experiments removeMutations(String s)
	{
	  //TODO
		return this;
	}

	/**
	 * Get number of experiments.
	 * 
	 * @return the number of experiments.
	 */
	public int getNumOfExperiments()
	{
		return experiments.size();
	}

	/**
	 * Get the experiments.
	 * 
	 * @return a set of data tables, one for each experiment.
	 */
	public List<List<List<Double>>> getExperiments()
	{
		return experiments;
	}
}
