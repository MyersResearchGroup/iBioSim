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
package edu.utah.ece.async.ibiosim.analysis.simulation;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;

/**
 * Base class of a Simulator class.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public abstract class AbstractSimulator extends CoreObservable {
	protected final Message message = new Message();

	/**
	 * Simulates the given model.
	 */
	public abstract void simulate() throws IOException, XMLStreamException, BioSimException;

	/**
	 * Stops simulation.
	 */
	public abstract void cancel();

	/**
	 * Sets up a new run.
	 *
	 * @param newRun
	 *          - the run index.
	 */
	public abstract void setupForNewRun(int newRun) throws IOException;

	/**
	 * Prints statistics from simulation.
	 */
	public abstract void printStatisticsTSD();

}
