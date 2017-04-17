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
package edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class RangeAndPairing {

	private LPNTransitionPair _indexPair;
	
	private IntervalPair _range;

	public RangeAndPairing(LPNTransitionPair _indexPair, IntervalPair _range) {
//		super();
		this._indexPair = _indexPair;
		this._range = _range;
	}

	public LPNTransitionPair get_indexPair() {
		return _indexPair;
	}

	public void set_indexPair(LPNTransitionPair _indexPair) {
		this._indexPair = _indexPair;
	}

	public IntervalPair get_range() {
		return _range;
	}

	public void set_range(IntervalPair _range) {
		this._range = _range;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_indexPair == null) ? 0 : _indexPair.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RangeAndPairing))
			return false;
		RangeAndPairing other = (RangeAndPairing) obj;
		if (_indexPair == null) {
			if (other._indexPair != null)
				return false;
		} else if (!_indexPair.equals(other._indexPair))
			return false;
		return true;
	}
	
	
	@Override
	public String toString(){
		return "Index Pair : " + _indexPair + " : Range : " + _range;
	}
	
}
