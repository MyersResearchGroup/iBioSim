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
package edu.utah.ece.async.backend.analysis.verification.timed_state_exploration.archive;


/**
 * Continuous variables whose current rate is not zero.
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class RateNonZero extends PreContinuous {

	/*
	 * Abstraction Function: This class represents a continuous
	 * variable that has a non-zero current rate. In this representation,
	 * super._range stores the cached range of rates.
	 */
	
	/*
	 * Representation Invariant:
	 * super._range -> current range of rates.
	 * 
	 */
	
	/**
	 * 
	 * @return
	 */
	public Interval getRangeOfRates(){
		return super.get_range();
	}
	
}
