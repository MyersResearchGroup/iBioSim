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
package edu.utah.ece.async.backend.analysis.simulation;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public interface ParentSimulator
{
	/**
	 * 
	 */
	public abstract void simulate();

	/**
	 * 
	 */
	public abstract void cancel();

	/**
	 * 
	 */
	public abstract void clear();

	/**
	 * 
	 * @param newRun
	 */
	public abstract void setupForNewRun(int newRun);

	/**
	 * 
	 */
	public abstract void printStatisticsTSD();

	public abstract void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException;
}
