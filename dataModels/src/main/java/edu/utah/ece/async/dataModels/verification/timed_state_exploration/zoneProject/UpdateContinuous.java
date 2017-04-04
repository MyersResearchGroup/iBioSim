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
package edu.utah.ece.async.dataModels.verification.timed_state_exploration.zoneProject;

/**
 * 
 * This class is used to aid in updating continuous variables after a transition has fired.
 * Its purpose is to hold the referencing information and new values for the continuous 
 * variable and to store information of what has changed for the continuous variable that
 * will be important for updating the continuous part of the state. When a transition fires,
 * it can perform a rate assignment or a value assignment to a continuous variable.
 * Furthermore, variables that have a rate of zero are treated different in a Zone than those
 * that have a non-zero rate. Thus it is important to know if a rate assignment results in a
 * variable rate becoming zero or becoming non-zero, whether a rate assignment occurred at
 * all, and whether a value assignment has occurred.
 * 
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class UpdateContinuous {
	
	/*
	 * Abstraction Function: An UpdateContinuous object keeps track of the referencing
	 * information for a variable as well as the rate and values that the variables has.
	 * The member variable lcrPair holds the information for referencing the variable
	 * in a Zone (which is often done using an LPNContinuousPair) and _value stores the
	 * upper and lower bounds for the continuous variable.
	 * 
	 * Since Zones store rate zero variables and non-zero rate variables in different
	 * places, it is important to know when a variable changes from being rate zero
	 * to non-rate zero and vice versa. When a variable does not change between rate
	 * zero and a non-zero rate, then it is important to know that at least the value
	 * of the variable's value has changed. In addition, for variables with a non-zero,
	 * the rate can be change to a different non-zero rate. This leads to five
	 * situations.
	 * 1. The variable was rate zero and remains rate zero, but the value of the
	 * variable has changed. The field _value should contain this new value.
	 * 2. The variable was rate zero and now is a non-zero rate. The field _value
	 * contains the new value if an assignment was made and contains the old value
	 * if no value assignment was made.
	 * 3. The variable had a non-zero rate and now the rate is zero. The field _value
	 * contains the new value if an assignment was made and contains the old value
	 * if no value assignment was made.
	 * 4. The variable had a non-zero rate and has been assigned another non-zero rate.
	 * The field _value should contain the old value.
	 * 5. The variable had a non-zero rate and either no rate assignment has been made
	 * or the rate has been set to another non-zero rate and a new value has been set.
	 * The new value should be placed in the _value field.
	 * 
	 * Note: The handling of a variable that moves between rate zero and a non-zero
	 * rate is not affected by a change in the value of the variable. That is the
	 * situation is handled the same whether the value has changed or not. Thus
	 * there is no need to consider the value changing separately. This not the case
	 * when a variable that has a non-zero rate and continues to have a non-zero rate.
	 * The handling is different depending on whether the value has changed or not.
	 */
	
	/*
	 * Representations invariant: The field _value contains the old value for the 
	 * variable if no value assignment was made and the contains the new value 
	 * otherwise.
	 */

	
	// The rate will be set to zero.
	private boolean _newZero;
	
	
	// The rate was zero.
	private boolean _oldZero;
	
	
	// A new value was assigned.
	private boolean _newValue;
	
	
	// The rate and index for the variable.
	private LPNContAndRate _lcrPair;
	
	
	// The value.
	private IntervalPair _value;
	
	
	/**
	 * Creates an UpdateContinuous object that has default values of false and null.
	 */
	public UpdateContinuous(){
		_oldZero = false;
		_newZero = false;
		_newValue = false;
		_value = null;
		_lcrPair = null;
	}
	
	/**
	 * Creates a new UpdateContinuous object with the given information for a given
	 * continuous variable.
	 * @param oldZero True if the previous rate zero; false otherwise.
	 * @param newZero True if the new rate is zero; false otherwise.
	 * @param newValue True if a value assignment has been made; false otherwise
	 * @param value The range of values for the continuous variable.
	 * @param lcrPair An LPNContAndRate pairing that has the reference information
	 * for the continuous variable as well as the range of rates.
	 */
	public UpdateContinuous(boolean oldZero, boolean newZero, boolean newValue,
			IntervalPair value, LPNContAndRate lcrPair){
		_oldZero = oldZero;
		_newZero = newZero;
		_newValue = newValue;
		_value = value;
		_lcrPair = lcrPair;
	}
	
	public UpdateContinuous(LPNContinuousPair lcpair){
		_oldZero = false;
		_newZero = false;
		_newValue = false;
		_value = null;
		_lcrPair = new LPNContAndRate(lcpair);
	}
	
	// Getters and setters for the member variables.
	
	/**
	 * Gets whether the new rate is zero.
	 * 
	 * @return True if the new rate for the variables is zero; false otherwise.
	 */
	public boolean is_newZero() {
		return _newZero;
	}


	/**
	 * Sets whether the new rate is zero.
	 * 
	 * @param _newZero True if the new rate is zero (regardless of the old rate);
	 * false otherwise.
	 */
	public void set_newZero(boolean _newZero) {
		this._newZero = _newZero;
	}


	/**
	 * Gets whether the old rate is zero.
	 * 
	 * @return True if the previous rate was zero; false otherwise.
	 */
	public boolean is_oldZero() {
		return _oldZero;
	}


	/**
	 * Sets whether the old rate was zero.
	 * 
	 * @param _oldZero True if the previous rate was zero; false otherwise.
	 */
	public void set_oldZero(boolean _oldZero) {
		this._oldZero = _oldZero;
	}


	/**
	 * Gets whether the value is new or not.
	 * 
	 * @return True if a new value has been assigned; false otherwise.
	 */
	public boolean is_newValue() {
		return _newValue;
	}


	/**
	 * Sets whether the value is is new or not.
	 * 
	 * @param _newValue True if a new value has been assigned; false otherwise.
	 */
	public void set_newValue(boolean _newValue) {
		this._newValue = _newValue;
	}


	/**
	 * Gets the reference for the variable.
	 * 
	 * @return The LPNContAndRate that represent the variable.
	 */
	public LPNContAndRate get_lcrPair() {
		return _lcrPair;
	}


	/**
	 * Sets the reference for the variable.
	 * 
	 * @param _lcrPair An LPNContAndRate that gives the reference information for
	 * the variable.
	 */
	public void set_lcrPair(LPNContAndRate _lcrPair) {
		this._lcrPair = _lcrPair;
	}


	/**
	 * Gets the values for the variable.
	 * 
	 * @return An IntervalPair that contains the upper and lower bounds for the value.
	 */
	public IntervalPair get_Value() {
		return _value;
	}


	/**
	 * Sets the values for the variable.
	 * 
	 * @param value An IntervalPair that contains the upper and lower bounds for the
	 * value.
	 */
	public void set_Value(IntervalPair value) {
		this._value = value;
	}

	
	/**
	 * Determines if a variable was non-zero and is now zero.
	 * 
	 * @return True if the previous rate was non-zero and the new rate is zero;
	 * false otherwise.
	 */
	public boolean newlyZero(){
		return !_oldZero && _newZero;
	}

	/**
	 * Determines if a variable was zero and is now non-zero.
	 * 
	 * @return True if the previous rate was zero and the new rate is non-zero;
	 * false otherwise.
	 */
	public boolean newlyNonZero(){
		return _oldZero && !_newZero;
	}
	
	@Override
	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result
//				+ ((_lcrPair == null) ? 0 : _lcrPair.hashCode());
//		result = prime * result + (_newValue ? 1231 : 1237);
//		result = prime * result + (_newZero ? 1231 : 1237);
//		result = prime * result + (_oldZero ? 1231 : 1237);
//		result = prime * result + ((_value == null) ? 0 : _value.hashCode());
//		return result;
		
		// Two UpdateContinuous variables are the same if they share the 
		// same continuous part.
		return _lcrPair.get_lcPair().hashCode();
	}


	@Override
	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		UpdateContinuous other = (UpdateContinuous) obj;
//		if (_lcrPair == null) {
//			if (other._lcrPair != null)
//				return false;
//		} else if (!_lcrPair.equals(other._lcrPair))
//			return false;
//		if (_newValue != other._newValue)
//			return false;
//		if (_newZero != other._newZero)
//			return false;
//		if (_oldZero != other._oldZero)
//			return false;
//		if (_value == null) {
//			if (other._value != null)
//				return false;
//		} else if (!_value.equals(other._value))
//			return false;
//		return true;
		
		if(!(obj instanceof UpdateContinuous)){
			return false;
		}
		
		UpdateContinuous uc = (UpdateContinuous) obj;
		
		// Two UpdateContinuous objects are the same if they share the same
		// continuous part.
		return _lcrPair.equals(uc.get_lcrPair());
	}
	
	
	@Override
	public String toString(){
		
		String s = "Variable: " + _lcrPair + "\n" 
				+ "Was Zero: " + _oldZero + "\n"
				+ "Is Zero: " + _newZero + "\n"
				+ "New value: " + _newValue + "\n";
		
		if(_value != null){
			s += "Value is:" + _value + "\n";
		}
		else{
			s += "Value is null" + "\n";
		}
		
		return s;
	}
}
