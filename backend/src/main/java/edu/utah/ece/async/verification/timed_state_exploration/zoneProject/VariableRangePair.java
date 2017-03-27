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
package main.java.edu.utah.ece.async.verification.timed_state_exploration.zoneProject;

import dataModels.lpn.parser.Variable;

/**
 * A Variable and Range class packages an lpn.parser.Variable with an
 * IntervalPair. This allows one to store the Variable along with its range.
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VariableRangePair {

	// The variable to store.
	private Variable _variable;
	
	
	// The range of the variable.
	private IntervalPair _range;

	
	
	public VariableRangePair(Variable _variable, IntervalPair _range) {
		super();
		this._variable = _variable;
		this._range = _range;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_variable == null) ? 0 : _variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VariableRangePair))
			return false;
		VariableRangePair other = (VariableRangePair) obj;
		if (_variable == null) {
			if (other._variable != null)
				return false;
		} else if (!_variable.equals(other._variable))
			return false;
		return true;
	}



	public Variable get_variable() {
		return _variable;
	}

	public void set_variable(Variable _variable) {
		this._variable = _variable;
	}

	public IntervalPair get_range() {
		return _range;
	}

	public void set_range(IntervalPair _range) {
		this._range = _range;
	}
	
	@Override
	public String toString(){
		return "" + _variable + " = " + _range ;
	}
}
