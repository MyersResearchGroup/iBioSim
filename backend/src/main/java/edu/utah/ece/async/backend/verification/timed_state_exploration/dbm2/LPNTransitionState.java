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
package edu.utah.ece.async.backend.verification.timed_state_exploration.dbm2;

import java.util.*;

/**
 * LPNTransitionStates adds the ability to store unprocessed enabled transitions.
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class LPNTransitionState{

	//boolean[] enabledTransitions;	// The ith position is true, then the ith transition
									// is enabled.
	
	private LPNState state;			// The underlying state.
	
	private HashSet<Integer> enabledTransitions;	// A subset of the enabled 
													// transitions.
	
	private DBMLL zone;			// A mutable zone.
	
	/**
	 * Create an LPNTransition state that has an underlying states whose
	 * with the given marked places and boolean values.
	 * @param places The marked places.
	 * @param values The truth values of the boolean variables.
	 */
	public LPNTransitionState(boolean[] places, boolean[] values)
	{
		state = new LPNState(places, values);
		enabledTransitions = new HashSet<Integer>();
	}
	
	/**
	 * Create an LPNTransition state that has an underlying states
	 * with the given marked places and boolean values. Also starts with
	 * the given enabled transitions.
	 * @param places The marked places.
	 * @param values The truth values of the boolean variables.
	 * @param transitions The enabled transitions.
	 */
	public LPNTransitionState(boolean[] places, boolean[] values, HashSet<Integer> transitions) {
		state = new LPNState(places, values);
		
		enabledTransitions = new HashSet<Integer>(transitions);
	}
	
	/**
	 * Create an LPNTransitionState that has the baseState as the underlying state.
	 * @param baseState The underlying state. Note: the current enabled
	 * transitions are not set by this constructor.
	 */
	public LPNTransitionState(LPNState baseState)
	{
		this.state = baseState;
		enabledTransitions = new HashSet<Integer>();
		zone = new DBMLL(baseState);
	}
	
	public LPNTransitionState(LPNState baseState, HashSet<Integer> enabledTransitions)
	{
		this.state = baseState;
		this.enabledTransitions = (HashSet<Integer>)enabledTransitions.clone();
		zone = new DBMLL(baseState);
	}
	
//	public LPNTransitionState(LPNTranslator trans, LPNState baseState)
//	{
//		this.state = baseState;
//		zone = new DBMLL(baseState);
//		enabledTransitions = new HashSet<Integer>();
//	}
	
	/**
	 * Create an LPNTransitionState that has the initial markings, boolean values
	 * from the LPN associated with the LPNTranslator. Note: the current enabled
	 * transitions are not set by this constructor.
	 * @param lpn Translates between the LPN and an indexed representation.
	 */
	public LPNTransitionState(LPNTranslator lpn)
	{
		state = new LPNState(lpn);
		zone = new DBMLL(state);
		enabledTransitions = new HashSet<Integer>();
	}
	
	/**
	 * Remove a transition from the enabled list.
	 * @param i The transition to remove.
	 */
	public void removeTransition(int i)
	{
		enabledTransitions.remove(i);
	}
	
	/**
	 * Determines whether there are any more transitions that are enabled.
	 * @return True if there are more transitions that are enabled, false
	 * otherwise.
	 */
	public boolean remainingTransitions()
	{
		return !enabledTransitions.isEmpty();
	}

	/**
	 * Selects one of the remaining enabled transitions. The selected transition
	 * is then removed from the list of remaining transitions.
	 * @return One of the remaining enabled transitions. 
	 */
	public int getATransition() {
		
		HashSet<Integer> mustFireTransition = new HashSet<Integer>();
		
		// Determine if any transition must fire.
		for(Integer i : enabledTransitions)
		{
			if(zone.mustFire(i))
			{
//				enabledTransitions.remove(i);
//				return i;
				mustFireTransition.add(i);
			}
		}
		
		if(mustFireTransition.size() != 0)
		{
			enabledTransitions = mustFireTransition;
		}
		
		Iterator<Integer> nextElement = enabledTransitions.iterator();
		int transition = nextElement.next();
		enabledTransitions.remove(transition);
		
		return transition;
	}

	/**
	 * Returns a new instance identical to this instance.
	 * @return The new instance.
	 */
	@Override
	public LPNTransitionState clone()
	{
		LPNTransitionState newState = new LPNTransitionState(state);
		newState.enabledTransitions.addAll(this.enabledTransitions);
		
		return newState;
	}


	/**
	 * Returns an LPNstate reflecting the change due to the given transition firing.
	 * @param transition The transition to fire. The base state does not change.
	 * @param LPNTrans An LPNTranslator that translates between the LPNState representation
	 * 				   and the LPNFile object representation.
	 * @return The state without the transition set.
	 * @throws IllegalTransitionFireException If the is is illegal for the transition to fire.
	 */
	public LPNState getUpdatedState(int transition, LPNTranslator LPNTrans,
			HashSet<Integer> newTransitions)
	{
		if(!LPNTrans.isEnabled(transition, state.getMarkedPlaces()))
		{
			throw new IllegalTransitionFireException("Transition " + transition + 
					" has fired illegally.");
		}
		
		// Get the Boolean values that result from firing the transition.
		boolean[] newBooleanValues = LPNTrans.getNewBooleanValues(transition, 
											state.getBooleanValues());
		
		// Get the marked places that results from firing the transition.
		boolean[] newMarkedPlaces = LPNTrans.getNewMarking(transition, state.getMarkedPlaces());
		
		//DBMLL newZone = zone.updateZone(transition, LPNTrans, newMarkedPlaces);
		
		//return new LPNState(newMarkedPlaces, newBooleanValues, 
		//		newZone.getTimers(), newZone.getZone());
		
		// TODO Pass the newTransitions as parameter to the initializeEnabledTransitions.
		initializeEnabledTransitions(LPNTrans, newTransitions, newMarkedPlaces, newBooleanValues);
		
		
		
		DBMLL newZone = zone.clone();
		
		newZone.updateZone(transition, LPNTrans, newBooleanValues, 
				newTransitions);
		
		// Set the enabled transitions.
		//enabledTransitions = newTransitions;
		
		return new LPNState(newMarkedPlaces, newBooleanValues,
				newZone.getTimers(), newZone.getZone());
		
	}
	
	/**
	 * Returns the underlying LPNState.
	 * @return The underlying LPNState.
	 */
	public LPNState getState()
	{	
		return state;
	}

	/**
	 * Sets the list of enabled transitions to all the currently enabled
	 * transitions.
	 * @param transLPN A translator built on the associated LPN.
	 */
	public void initializeEnabledTransitions(LPNTranslator transLPN)
	{
		//enabledTransitions = transLPN.getEnabledTransitions(state.getMarkedPlaces());
		enabledTransitions = transLPN.getEnabledTransitions(this);
	}
	
	public static void initializeEnabledTransitions(LPNTranslator transLPN, 
			HashSet<Integer> enabledTransitions, boolean[] newMarkedPlaces,
			boolean[] newBooleanValues)
	{
		
		LPNTransitionState state = new LPNTransitionState( new LPNState(newMarkedPlaces, newBooleanValues));
		
		// Get transitions that are enabled by markings.
		transLPN.getEnabledTransitions(state, enabledTransitions);
		//TODO remove transitions that are not enabled by reaching their lower bound.
		
//		for(Integer i : zone.hasMetDelay())
//		{
//			enabledTransitions.remove(i);
//		}
	}
	
	
	/**
	 * Checks if a transition is enabled.
	 * @param transition The transition to check.
	 * @return True if the transition is enabled, false otherwise.
	 */
	//public boolean checkTransitionEnabled(int transition)
	//{
	//	return false;
	//}
	
	/**
	 * Defines an exception thrown for a transition firing illegally.
	 */
	public class IllegalTransitionFireException extends RuntimeException
	{
		/**
		 * The default serialVersionUID.
		 */
		private static final long serialVersionUID = 1L;

		public IllegalTransitionFireException(String message)
		{
			super(message);	// Use the default constructor.
		}
	}
	
	/**
	 * Gets a set of the enabled transitions that have not yet fired.
	 * @return The set of the remaining transitions.
	 */
	public HashSet<Integer> getRemainingTransitions()
	{
		return (HashSet<Integer>) enabledTransitions.clone();
	}
	
	/**
	 * Creates an id for the base state.
	 */
	public void createStateId()
	{
		state.createId();
	}
	
	/**
	 * Overrides the toString method.
	 */
	@Override
	public String toString()
	{
		String result = state.toString(); 	// Variable for building the string.
		
		// Get the remaining transitions.
		result += "\n\t\tRemaining Transitions : " + enabledTransitions + "\n";
		
		return result;
	}
	
	public boolean hasMetDelay(int transition)
	{
		return zone.hasMetDelay(transition);
	}
}
