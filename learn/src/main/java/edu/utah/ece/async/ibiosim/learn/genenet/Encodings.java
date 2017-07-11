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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An encodings object is used to represent how a set of data is represented in the
 * learning procedure. The data is discretized to facilitate the identification of 
 * potential influencing connections between species.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Encodings
{

	private int							numBins;
	private Map<Integer, double[]>		discreteSpecies;
	private List<List<List<Integer>>>	levelAssignments;

	/**
	 * Creates an Encodings object with default number of bins.
	 */
	public Encodings()
	{
		numBins = 3;
		discreteSpecies = new HashMap<Integer, double[]>();
		levelAssignments = new ArrayList<List<List<Integer>>>();
	}

	/**
   * Creates an Encodings object with a specified number of bins.
   * 
	 * @param bin - number of bins.
	 */
	public Encodings(int bin)
	{
		numBins = bin;
		discreteSpecies = new HashMap<Integer, double[]>();
		levelAssignments = new ArrayList<List<List<Integer>>>();
	}

	/**
	 * Add the values of a particular species.
	 * 
	 * @param col - integer corresponding to the column entry of a species in the data table.
	 * @param values - the values of the species.
	 */
	public void addDiscreteSpecies(int col, double[] values)
	{
		discreteSpecies.put(col, values);
	}

	/**
	 * Associate a value to a certain entry in the data table from a specified experiment. 
	 * 
	 * @param experiment - experiment index from which the value is coming from
	 * @param row - the row in the data table to insert a value.
	 * @param col - the column in the data table to insert a value.
	 * @param data - the value that is inserted to the data table.
	 */
	public void addLevelAssignment(int experiment, int row, int col, double data)
	{
		while (levelAssignments.size() < experiment + 1)
		{
			levelAssignments.add(new ArrayList<List<Integer>>());
		}
		while (levelAssignments.get(experiment).size() < row + 1)
		{
			levelAssignments.get(experiment).add(new ArrayList<Integer>());
		}
		while (levelAssignments.get(experiment).get(row).size() < col + 1)
		{
			levelAssignments.get(experiment).get(row).add(0);
		}
		levelAssignments.get(experiment).get(row).set(col, getLevelAssignment(col, data));
	}

	/**
	 * The discretized value of a certain value.
	 * 
	 * @param col - the column entry of the data table.
	 * @param data - the value to be transformed.
	 * @return the discretized value of data.
	 */
	public int getLevelAssignment(int col, double data)
	{
		double[] discrete = discreteSpecies.get(col);

		for (int i = 1; i < discrete.length; i++)
		{
			if (data <= discrete[i])
			{
				return i - 1;
			}
		}

		return numBins - 1;
	}

	/**
	 * Get the level assignments.
	 * 
	 * @return the level assignments.
	 */
	public List<List<List<Integer>>> getLevelAssignments()
	{
		return levelAssignments;
	}

	/**
	 * Get the size of the level assignments.
	 * 
	 * @return size of level assignments.
	 */
	public int size()
	{
		return levelAssignments.size();
	}

	/**
	 * Print the level assignments for each experiment.
	 */
	public void print()
	{
		for (int i = 0; i < levelAssignments.size(); i++)
		{
			System.out.println("Experiment :" + i);
			for (int j = 0; j < levelAssignments.get(i).size(); j++)
			{
				for (int k = 1; k < levelAssignments.get(i).get(j).size(); k++)
				{
					System.out.print(levelAssignments.get(i).get(j).get(k) + " ");
				}
				System.out.println("");
			}

		}
	}

}
