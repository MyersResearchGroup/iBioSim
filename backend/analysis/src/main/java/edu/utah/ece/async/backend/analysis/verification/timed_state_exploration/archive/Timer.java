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
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Timer extends OctMember {

	/*
	 * Abstraction Function: Represents a timer associated with a clock.
	 * In this representation, super._range caches the delay.
	 */
	
	/*
	 * Representation Invariant:
	 * super._range -> delay.
	 */
	
	/**
	 * Get the cached delay.
	 * @return The current delay.
	 */
	public Interval getDelay(){
		return super.get_range();
	}
	
	/**
	 * Sets the current delay.
	 * @param delay The current delay.
	 */
	public void setDelay(Interval delay){
		super.set_range(delay);
	}
}
