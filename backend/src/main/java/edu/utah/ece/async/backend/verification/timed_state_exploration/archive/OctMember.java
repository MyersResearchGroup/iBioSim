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
package edu.utah.ece.async.backend.verification.timed_state_exploration.archive;

import edu.utah.ece.async.dataModels.lpn.parser.LPN;

/**
 * Base class for the member variables of the octagons.
 * 
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class OctMember {
	
	/*
	 * Abstraction Function: Gathers together common elements of the octagon
	 * variables timer and continuous variables.
	 */
	
	/*
	 *  The associated LPN for this variable.
	 */
	protected LPN _lpn;
	
	/*
	 * The variable number taking into account all variables for the
	 * LPN.
	 */
	protected int _absReference;
	
	/*
	 *  An interval reference. Timers can use this for caching the delay.
	 *  Rate zero continuous variable can use this for caching the current
	 *  range of values. Non-rate zero variables can use this for caching
	 *  range of rates.
	 */
	protected Interval _range;

	
	/**
	 * Get the associated LPN.
	 * @return The associated LPN.
	 */
	public LPN get_Lpn() {
		return _lpn;
	}

	/**
	 * Set the associated LPN.
	 * @param _lpn
	 */
	public void set_Lpn(LPN _lpn) {
		this._lpn = _lpn;
	}

	/**
	 * Gets the number of the variable.
	 * @return The number of the variable.
	 */
	public int get_absReference() {
		return _absReference;
	}

	/**
	 * Sets the number of the variable.
	 * @param _absReference The number of the variable.
	 */
	public void set_absReference(int _absReference) {
		this._absReference = _absReference;
	}

	/**
	 * Gets the range.
	 * @return The range.
	 */
	public Interval get_range() {
		return _range;
	}

	/**
	 * Sets the range.
	 * @param _range The range.
	 */
	public void set_range(Interval _range) {
		this._range = _range;
	}
	
	
	
}
