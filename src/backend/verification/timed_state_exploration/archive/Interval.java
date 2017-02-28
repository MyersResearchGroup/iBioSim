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
package backend.verification.timed_state_exploration.archive;


/**
 * Represents an interval of real numbers [min,max].
 * 
 * @author Andrew N. Fisher
 *
 */
public class Interval {

	/*
	 * Abstraction: represents the set of numbers [min,max].
	 */
	
	
	/*
	 * Representation Invariant: The behavior is undefined for
	 * min > max.
	 */
	
	private int _min,_max;
	
	/**
	 * Creates a degenerate interval representing {0}.
	 */
	public Interval(){
		this._min = this._max = 0;
	}
	
	/**
	 * Creates a degenerate interval representing {value}.
	 * @param value
	 */
	public Interval(int value){
		this._min = this._max = value;
	}
	
	/**
	 * Creates an interval the interval [min,max].
	 * @param min The minimum value.
	 * @param max The maximum value.
	 */
	public Interval(int min, int max){
		this._min = min;
		this._max = max;
	}

	/**
	 * Creates a new instance of Interval.
	 * 
	 * @return Creates an Interval [a,b] where
	 * a is the min value of this instance and b
	 * is the max value of this instance.
	 */
	@Override
	protected Interval clone() {
		
		return new Interval(this._min, this._max);
	}

	/**
	 * Determines if the obj is equal to this Interval.
	 * 
	 * @return True if obj is an Interval instance
	 * that satisfies obj.min = this.min and
	 * obj.max = obj.max.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if(!(obj instanceof Interval)){
			return false;
		}
		
		Interval that = (Interval) obj;
		
		return this.equals(that);
	}

	/**
	 * Determines if two Intervals are equal.
	 * @param that The interval to compare this
	 * Interval to.
	 * @return True if and only if
	 * that.min == this.min and that.max == this.max.
	 */
	public boolean equals(Interval that){
		return that._min == this._min && that._max == this._max;
	}
	
	/**
	 * @return Returns a hashcode.
	 */
	@Override
	public int hashCode() {
		
		return 31*this._min + this._max;
	}

	/**
	 * @return Returns a string representation of this Interval.
	 */
	@Override
	public String toString() {
		
		if( this._min > this._max){
			return "{}";
		}
		
		if( this._min == this._max){
			return "{" + this._min + "}";
		}
		
		return "[" + this._min + "," + this._max + "]";
	}
	
	/**
	 * Determines if the Interval has at least one value.
	 * @return True if and only this this.min <= this.max.
	 */
	public boolean nonDegenerate(){
		return this._min <= this._max;
	}
	
	/**
	 * Determines if the Interval represents a single value.
	 * @return True if and only if this.min == this.max.
	 */
	public boolean signalValue(){
		return this._min == this._max;
	}
	
	/**
	 * @return Returns the minimum value. Assumes non-empty.
	 */
	public int get_Min(){
		return this._min;
	}
	
	/**
	 * Sets the minimum value.
	 * @param min
	 */
	public void set_Min(int min){
		this._min = min;
	}
	
	/**
	 * 
	 * @return Returns the maximum value. Assumes non-empty.
	 */
	public int get_Max(){
		return this._max;
	}
	
	/**
	 * Sets the maximum value.
	 * @param max
	 */
	public void set_Max(int max){
		this._max = max;
	}
	
	/**
	 * Determines if the Interval is just the zero value.
	 * @return True if the Interval is {0}.
	 */
	public boolean isZero(){
		return signalValue() && this._min == 0;
	}
}
