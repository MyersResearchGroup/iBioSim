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
 * The LPNTransitionPair gives a packaged pairing of a transition index together
 * with the index of the LPN that the transition is in. The class simply has the two
 * member variables for the transition index and the LPN index with getters and
 * setters, so it is possible to use it as a general pairing of two integers. However,
 * it is recommended that it is only used where the semantics apply. That is, it
 * is recommended that it is only used in the context of pairing a transition index
 * with an LPN index.
 * 
 * If the index of a Transition is t and the index of the LPN that the Transition
 * occurs in in l, the LPNTransitionPair is thought of as (l,t). In particular, the
 * natural ordering on the LPNTransitionPair is the dictionary ordering on this
 * pairs of this form.
 * 
 * @author Andrew N. Fisher 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class LPNTransitionPair implements Comparable<LPNTransitionPair>{
	
	/*
	 * 
	 * TODO : This will need to be changed to reflect the introduction of the continuous
	 * variables. 
	 * 
	 * Abstraction Function : Given a Transition with index t in an LPN that has
	 * 		index l, the LPNTransitionPair represents the pairing (t, l). The
	 * 		index of the transition (t) is stored in _transitionIndex and the index
	 * 		of the LPN (l) is stored in _lpnIndex.
	 */
	
	/*
	 * Representation Invariant : none.
	 */
	
	// Value for indicating a single LPN is in use.
	public static final int SINGLE_LPN = -3;
	
	// Value for indicating the zero timer.
	public static final int ZERO_TIMER = -1;
	
	//public static final LPNTransitionPair ZERO_TIMER_PAIR = new LPNTransitionPair(ZERO_TIMER,ZERO_TIMER,true);
	public static final LPNTransitionPair ZERO_TIMER_PAIR = new LPNTransitionPair(ZERO_TIMER,ZERO_TIMER);
	
	
	protected int _lpnIndex;
	protected int _transitionIndex;
	//private boolean _isTimer;
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _lpnIndex;
		result = prime * result + _transitionIndex;
		
		//int boolValue = _isTimer ? 1 : 0;
		
		//result += prime *result*boolValue;
		
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof LPNTransitionPair))
			return false;
		LPNTransitionPair other = (LPNTransitionPair) obj;
//		if(_isTimer != other._isTimer)
//			return false;
		if (_lpnIndex != other._lpnIndex)
			return false;
		if (_transitionIndex != other._transitionIndex)
			return false;
		return true;
	}	
	
	/**
	 * Creates a LPNTransitionPair with both indicies set to 0.
	 */
	public LPNTransitionPair(){
		_lpnIndex = 0;
		_transitionIndex = 0;
		//_isTimer = false;
	}
	
	/**
	 * Creates a pairing of an LPN index and is associated transition index.
	 * @param lpnIndex
	 * 			The LPN that the transition is in.
	 * @param transitionIndex
	 * 			The index of the transition.
	 */
//	public LPNTransitionPair(int lpnIndex, int transitionIndex, boolean isTimer){
//		this._lpnIndex = lpnIndex;
//		this._transitionIndex = transitionIndex;
//		this._isTimer = isTimer;
//	}
	
	public LPNTransitionPair(int lpnIndex, int transitionIndex){
		this._lpnIndex = lpnIndex;
		this._transitionIndex = transitionIndex;
	}
	
	/**
	 * Gets the index of the LPN that the transition is in.
	 * @return
	 * 		The index of the LPN that the transition is in.
	 */
	public int get_lpnIndex() {
		return _lpnIndex;
	}
	
	/**
	 * Sets the index of the LPN that the transition is in.
	 * @param lpnIndex
	 * 			The index of the Lpn that the transition is in.
	 */
	public void set_lpnIndex(int lpnIndex) {
		this._lpnIndex = lpnIndex;
	}
	
	/**
	 * Get the index of the transition.
	 * @return
	 * 		The index of the transition.
	 */
	public int get_transitionIndex() {
		return _transitionIndex;
	}
	
	/**
	 * Sets the index of the transition.
	 * @param transitionIndex
	 * 			The index of the transition.
	 */
	public void set_transitionIndex(int transitionIndex) {
		this._transitionIndex = transitionIndex;
	}
	
	/**
	 * Gets a boolean value that indicates whether this pair is storing a timer
	 * or a continuous variable.
	 * @return
	 * 		True if the pair is to be interpreted as a timer, false if it is to be
	 * 		interpreted as a continuous variable.
	 */
//	public boolean get_isTimer(){
//		return _isTimer;
//	}
	
	/**
	 * Sets the boolean that's used to determine if the pairing should be treated
	 * as a continuous variable or a timer.
	 * @param isTimer
	 * 		True indicates a timer, false indicates a continuous variables.
	 */
//	public void set_isTimer(boolean isTimer){
//		this._isTimer = isTimer;
//	}
	
	/**
	 * Creates a string representation of the LPNTransitionsPair object. If
	 * the pairing is representing a timer then the format is in the form
	 * "(LPN Index, Transition Index) = (x, y)" where x and y are the LPNIndex
	 * and Transition Index, respectively. If the pairing is representing a
	 * continuous variable the format is in the form
	 * "(LPN Index, Continuous Variable) = (x, y)" where x and y are the LPNIndex
	 * and the index of the continuous variable, respectively.
	 */
	@Override
	public String toString(){
		String result = "";
		
//		if(_isTimer){
//			
			if(this.equals(ZERO_TIMER_PAIR)){
				result += "Zero timer";
			}
			else{
				result += "(LPN Index, Transition Index) = (";

				result += _lpnIndex + ", " + _transitionIndex + ")";
			}
//		}
//		else{
//			result = "(LPN Index, Continuous Variable) = (";
//			
//			result += _lpnIndex + ", " + _transitionIndex + ")";
//		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public LPNTransitionPair clone(){
		
		LPNTransitionPair newPair = new LPNTransitionPair();
		
		newPair._lpnIndex = this._lpnIndex;
		newPair._transitionIndex = this._transitionIndex;
		//newPair._isTimer = this._isTimer;
		
		return newPair;
	}

	/**
	 * Determines whether this LPNTransitionPair is less than the otherPair
	 * LPNTransitionPair. The ordering is the dictionary ordering on pairs
	 * (l,t) where t is the index of the Transition and l is the index of the
	 * LPN that the Transition is in. All continuous variables come before all
	 * timers.
	 * @param otherValue
	 * 		The value to determine whether this LPNTransitionPair is less than.
	 * @return
	 * 		A negative integer, zero, or positive integer depending on if this
	 * 		LPNTransitionPair is less than, equal, or greater than the otherPair
	 * 		LPNTransitionPair.
	 */
	@Override
	public int compareTo(LPNTransitionPair otherPair) {

		/*
		 * If it is known that the terms will not be too large, than a clever
		 * implementation would be
		 * 		return dlpnIndex * constant + dtransitionIndex
		 * where dlpnIndex is the difference in the _lpnIndex member variables and
		 * dtransitionIndex is the difference in the _transitionIndex member variables.
		 * 
		 * For this to work the constant has to be chosen such that 
		 * 		|dtransitionIndex| < constant
		 * so that the return value has the proper sign when dlpnIndex and
		 * dtransitionIndex have opposite signs. Since the number of transition
		 * is not bounded, this is not practical.
		 * 
		 * The roles of dlpnIndex and dtransitionIndex could be reversed (that is
		 * we could do the dictionary ordering of the reversed pair). But the problem
		 * still remains and this ordering would be much less intuitive.
		 */
		
		/*
		 * This block ensures that all continuous variables (ie _isTimer is false)
		 * come before the timers. However, if one of the timers is the zero timer,
		 * then we want o skip this block. The the reason is that the zero timer
		 * comes before any continuous variable or timer and this will already be 
		 * enforced if this block is skipped due to the _lpnIndex field of the zero
		 * timer is smaller than the possible indicies of any other variable.
		 */
//		if(this._isTimer != otherPair._isTimer &&
//				!(this._lpnIndex == ZERO_TIMER || otherPair._lpnIndex == ZERO_TIMER)){
//			// If one pair represents a continuous variable and the other represents a timer
//			// then already one is less than the other. If this._isTimer is false, then 
//			// otherPair._isTimer is true and this comes before otherPair. Else this._isTimer
//			// is true and otherPair._isTimer is false. So this comes after otherPair.
//			return this._isTimer ? 1 : -1;
//		}
		
		
		if((this instanceof LPNContinuousPair != otherPair instanceof LPNContinuousPair)
				&& !(this.equals(ZERO_TIMER_PAIR) || otherPair.equals(ZERO_TIMER_PAIR))){
			// Continuous variables come before all timers except the zero timer. In this block
			// exactly one of this pair and other pair is a continuous variable index. If this
			// is the continuous variable index, then this is smaller. If this is not the
			// continuous variable, then the other is the continuous variable, so this pair is
			// larger.
			return (this instanceof LPNContinuousPair) ? -1 : 1;
		}
		
		int dlpnIndex = this._lpnIndex - otherPair._lpnIndex;
		
		if(dlpnIndex != 0){
			return dlpnIndex;
		}
		// Reaching here means that the first values are equal.
		// So the sign is determined by the second pair.
		return this._transitionIndex - otherPair._transitionIndex;
	}
	
	
//	public LPNTransitionPair (int a, int b, boolean c){
//		//  remove when Zones dependency have been addressed.
//	}
	
//	public boolean get_isTimer(){
//		// remove when Zones dependency have been addressed.
//		return false;
//	}
}
