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
package main.java.edu.utah.ece.async.verification.timed_state_exploration.zone;

import java.util.HashMap;
import java.util.List;

import dataModels.lpn.parser.Transition;
import main.java.edu.utah.ece.async.verification.platu.stategraph.State;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class ZoneType {

	/* Infinity is represented by the maximum integer value. */
	public static final int INFINITY = Integer.MAX_VALUE;

	/* 
	 * Turns on and off subsets for the zones.
	 * True means subset will be considered.
	 * False means subsets will not be considered.
	 */
	private static boolean _subsetFlag = true;
	
	/**
	 * Gets the value of the subset flag.
	 * @return
	 * 		True if subsets are requested, false otherwise.
	 */
	public static boolean getSubsetFlag(){
		return _subsetFlag;
	}
	
	/**
	 * Sets the value of the subset flag.
	 * @param useSubsets
	 * 			The value for the subset flag. Set to true if
	 * 			supersets are to be considered, false otherwise.
	 */
	public static void setSubsetFlag(boolean useSubsets){
		_subsetFlag = useSubsets;
	}
	
	/* 
	 * Turns on and off supersets for zones.
	 * True means that supersets will be considered.
	 * False means that supersets will not be considered.
	 */
	private static boolean _supersetFlag = true;
	
	/**
	 * Gets the value of the superset flag.
	 * @return
	 * 		True if supersets are to be considered, false otherwise.
	 */
	public static boolean getSupersetFlag(){
		return _supersetFlag;
	}
	
	/**
	 * Sets the superset flag.
	 * @param useSupersets
	 * 		The value of the superset flag. Set to true if
	 * 		supersets are to be considered, false otherwise.
	 */
	public static void setSupersetFlag(boolean useSupersets){
		_supersetFlag = useSupersets;
	}
	
	/**
	 * Get the value of the upper bound for the delay.
	 * @param timer
	 * 			The timer's index.
	 * @return
	 * 			The upper bound on the transitions delay.
	 */
	public abstract int getUpperBoundbyTransitionIndex(int timer);

	/**
	 * Get the value of the upper bound for the delay.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @return
	 * 			The upper bound on the transitions delay.
	 */
	public abstract int getUpperBoundbydbmIndex(int index);

	/**
	 * Get the value of the lower bound for the delay.
	 * @param timer
	 * 			The timer's index.
	 * @return
	 * 			The value of the lower bound.
	 */
	public abstract int getLowerBoundbyTransitionIndex(int timer);

	/**
	 * Get the value of the lower bound for the delay.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @return
	 * 			The value of the lower bound.
	 */
	public abstract int getLowerBoundbydbmIndex(int index);

	/**
	 * Retrieves an entry of the DBM using the DBM's addressing.
	 * @param i
	 * 			The row of the DBM.
	 * @param j
	 * 			The column of the DBM.
	 * @return
	 * 			The value of the (i, j) element of the DBM.
	 */
	public abstract int getDbmEntry(int i, int j);

	/**
	 * Determines if a timer has reached its lower bound.
	 * @param timer
	 * 			The timer's index.
	 * @return
	 * 			True if the timer has reached its lower bound, false otherwise.
	 */
	public abstract boolean exceedsLowerBoundbyTransitionIndex(int timer);

	/**
	 * Determines if a timer has reached its lower bound.
	 * @param index
	 * 			The timer's row/column of the DBM.
	 * @return
	 * 			True if the timer has reached its lower bound, false otherwise.
	 */
	public abstract boolean exceedsLowerBoundbydbmIndex(int index);

	/**
	 * Updates the Zone according to a transition firing.
	 * @param timer
	 * 			The index of the transition that fired.
	 * @return
	 * 			The updated Zone.
	 */
	public abstract ZoneType fireTransitionbyTransitionIndex(int timer,
			int[] enabledTimers, State state);

	/**
	 * Updates the Zone according to the transition firing.
	 * @param index
	 * 			The index of the timer.
	 * @return
	 * 			The updated Zone.
	 */
	public abstract ZoneType fireTransitionbydbmIndex(int index,
			int[] enabledTimers, State state);

	/**
	 * Overrides the clone method from Object.
	 */
	@Override
	public abstract ZoneType clone();

	/**
	 * The list of enabled timers.
	 * @return
	 * 		The list of all timers that have reached their lower bounds.
	 */
	public abstract List<Transition> getEnabledTransitions();
	
	/**
	 * Retrieve a lexicon that maps the internal numbers to the transition.
	 * @return
	 * 		The lexicon.
	 */
	public abstract HashMap<Integer, Transition> getLexicon();
	
	/**
	 * Determines if this zone is a subset of Zone otherZone.
	 * @param otherZone 
	 * 		The zone to compare against.
	 * @return
	 * 		True if this is a subset of other; false otherwise.
	 */
	public abstract boolean subset(ZoneType otherZone);
	
	/**
	 * Determines if this zone is a superset of Zone otherZone.
	 * @param otherZone 
	 * 		The zone to compare against.
	 * @return
	 * 		True if this is a subset of other; false otherwise. More specifically it
	 *		gives the result of otherZone.subset(this). Thus it agrees with the subset method.
	 */
	public boolean superset(ZoneType otherZone){
		return otherZone.subset(this);
	}

}