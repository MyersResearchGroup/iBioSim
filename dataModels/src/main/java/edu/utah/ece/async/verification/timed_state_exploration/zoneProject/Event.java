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
package edu.utah.ece.async.verification.timed_state_exploration.zoneProject;

import edu.utah.ece.async.lpn.parser.Transition;

/**
 * An Event is an action that is pending. This can be a transition to fire or an inequality.
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Event {

	/*
	 * Abstraction Function : An event simply represents a transition, an Inequality,
	 * or a rate change. Whichever the event is representing is stored in the
	 * corresponding member variables _transition, _inequality, or _rate.
	 */
	
	/*
	 * Representation Invariant : Exactly one of _transition, _inequality, or _rate
	 * should be non-null at a given time.
	 */
	
	private Transition _transition;
	
	private InequalityVariable _inequality;
	
	// The rate stored as the currentRate is the rate the variable is going to be
	// changed to.
	private LPNContinuousPair _rate;
	
	/**
	 * Initializes the Event as a transition event.
	 * @param t
	 * 		The transition the event represents.
	 */
	public Event(Transition t){
		_transition = t;
	}
	
	/**
	 * Initializes the Event as an inequality event.
	 * @param v
	 * 		The inequality this event represents.
	 */
	public Event(InequalityVariable v){
		_inequality = v;
	}
	
	/**
	 * Initializes the Even as a rate change event.
	 * @param r
	 */
	public Event(LPNContinuousPair r){
		_rate = r;
	}
	
	/**
	 * Determines whether this Event represents a Transition.
	 * @return
	 * 		True if this EventSet represents a Transition; false otherwise.
	 */
	public boolean isTransition() {
		
		return _transition != null;
	}

	
	/**
	 * Determines whether this Event represents a rate event.
	 * @return
	 * 		True if this EventSet represents a rate event; false otherwise.
	 */
	public boolean isRate() {
		
		return _rate != null;
	}
	
	/**
	 * Determines whether this Event represents an inequality.
	 * @return
	 * 		True if this Event is an inequality, false otherwise.	
	 */
	public boolean isInequality(){
		return _inequality != null;
	}
	
	/**
	 * Gets the inequality variable that this Event represents.
	 * @return
	 * 		The inequality variable if this Event represents an inequality, null otherwise.
	 */
	public InequalityVariable getInequalityVariable(){
		return _inequality;
	}
	
	/**
	 * Gets the transition that this Event represents.
	 * @return
	 * 		The transition if this Event represents a transition, null otherwise.
	 */
	public Transition getTransition(){
		return _transition;
	}
	
	/**
	 * Gets the rate change for the rate change event this Event represents.
	 * @return
	 */
	public LPNContinuousPair getRateChange(){
		return _rate;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		String result = "";
		
		// Test if this event is a transition or is an inequality.
		if(_transition != null){
			// This event represents a transition.
			result += "Transition Event : " + _transition.getLabel();
		}
		else if(_inequality != null){
			// This event represents an inequality.
			result += "Inequality Event : " + _inequality;
		}
		else if(_rate != null){
			// This event represents a rate change.
			result += "Rate change Event: " + _rate; 
		}
		else{
			// The event is not initialized.
			result += "Event not initialized";
		}
		return result;
	}
}
