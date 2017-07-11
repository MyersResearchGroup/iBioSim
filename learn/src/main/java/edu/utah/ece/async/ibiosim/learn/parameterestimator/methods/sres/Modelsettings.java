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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Modelsettings
{

	public List<Double>	IC;
	public int			ngenes;
	public int			startp;
	public int			endp;
	double[]			lowerBounds;
	double[]			upperBounds;
	boolean				verbose;
	int					nums;

	/**
	 * Creates a model settings object.
	 * 
	 * @param ic - time course.
	 * @param ngenes - number of genes.
	 * @param sp - starting point.
	 * @param ep - ending point.
	 * @param lowerbounds - lower bounds of parameter search space.
	 * @param upperbounds - upper bounds of parameter search space.
	 * @param verbose - verbose option.
	 */
	public Modelsettings(List<Double> ic, int ngenes, int sp, int ep, double[] lowerbounds, double[] upperbounds, boolean verbose)
	{

		this.IC = ic;
		this.ngenes = ngenes;
		this.startp = sp;
		this.endp = ep;
		this.lowerBounds = lowerbounds;
		this.upperBounds = upperbounds;
		this.verbose = verbose;
	}

}
