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
package edu.utah.ece.async.biosim.synthesis.techMap;

/**
 * 
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class WeightedGraph
{

	private SBOLGraph graph;
	private double weight;

	public WeightedGraph(SBOLGraph g, double weight)
	{
		this.graph = g;
		this.weight = weight;

	}
	public SBOLGraph getSBOLGraph()
	{ 
		return this.graph;
	}
	public double getWeight()
	{
		return this.weight;

	}

}
