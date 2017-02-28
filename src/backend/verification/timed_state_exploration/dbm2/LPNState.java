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
package backend.verification.timed_state_exploration.dbm2;

import java.util.*;

/**
 * This class represents the state of an LhpnFile object with only places,
 * boolean values, and timers on the transitions. It does not contain any
 * associations such as presets and postsets of transitions and place. To obtain,
 * such information, the LhpnFile will need to queried.
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class LPNState {
	
	// Abstraction Function:
	// The state of an LHPN consists of the set of markings, the current value of
	// the Boolean values, and zone to represent the state of the clocks.
	// The places are labeled 0 through n and the Boolean variables are
	// labeled 0 through n. Thus the state will refer to the ith place or
	// jth Boolean variable. The state is given a id of -1 to represent the state has
	// not been given an id yet. The member variable numberOfStates keeps track of the
	// current number of states that have been given an id.
	 
	
	// Representation invariant:
	// The cachedHashCode value should be initialized to a negative value.
	// The id should be negative if the id has not been assigned.
	// numberOfStates should represent the number of states that have been given an 
	// id.
	// The zone array should always be square and not contain null entries.
	// The enabledTimers array should not be null.
	
	private static long numberOfStates = 0;		// Keeps track of the number of states created.
												// for a state to be included, the createId 
												// method must be called.
	
	private long id;			// This state's id. If it has been created.
	
	private boolean[] markedPlaces; // Indicates which places are marked.
	
	private boolean[] booleanValues; // Indicates the values of each of the booleans
	
	private HashSet<LPNState> previousStates;	// The states that immediately precede this state.
	
	private HashSet<LPNState> nextStates;		// The states that immediately follow this state.
	
	private int cachedHashCode;			// Stores the hash code.
	
	private int[][] zone;				// Keeps track of the zone information.
	
	private int[] enabledTimers;		// Keeps track of which clocks the zone represents.
	
	/**
	 * Creates a state with the places marked and the given Boolean values.
	 * @param places An array that is true if the place is marked and false otherwise.
	 * @param values An array that is true if the Boolean variable is true and false otherwise.
	 */
	public LPNState(boolean[] places, boolean[] values)
	{
		this(places, values, new int[0], new int[0][0]);
//		cachedHashCode = -1;			// Initialized to a negative value to indicate no
//										// hash code value calculated yet.
//		markedPlaces = places.clone();
//		booleanValues = values.clone();
//		previousStates = new HashSet<LPNState>();
//		nextStates = new HashSet<LPNState>();
//		id = -1;
//		zone = new int[0][0];	// Zone not being used, initialize to empty.
		
		
	}
	
	public LPNState(boolean[] places, boolean[] values, int[] timers, int[][] zone)
	{
		cachedHashCode = -1; // Initialized to a negative value to indicate no
							 // hash code value calculated yet.
		markedPlaces = places.clone();
		booleanValues = values.clone();
		previousStates = new HashSet<LPNState>();
		nextStates = new HashSet<LPNState>();
		id = -1;
		this.zone = zone.clone();
		enabledTimers = timers.clone();
	}
	
	
	
	/**
	 * Create an LPNState object that represents the initial state of the LhpnFile.
	 * @param lpn
	 */
	public LPNState(LPNTranslator lpn)
	{
		cachedHashCode = -1;			// Initialized to a negative value to indicate
										// no hash code value calculated yet.
		markedPlaces = lpn.getInitialMarkings();
		booleanValues = lpn.getInitialBooleanValues();
		previousStates = new HashSet<LPNState>();
		nextStates = new HashSet<LPNState>();
		id = -1;
		DBMLL zoneInfo = new DBMLL(lpn);
		zone = zoneInfo.getZone();
		enabledTimers = zoneInfo.getTimers();
	}
	
	/**
	 * Overrides the clone method from the object class.
	 */
	@Override
	public LPNState clone()
	{
		LPNState newState = new LPNState(markedPlaces, booleanValues);
		newState.cachedHashCode = this.cachedHashCode;
		for(LPNState s : previousStates)
		{
			newState.previousStates.add(s);
		}
		for(LPNState s : nextStates)
		{
			newState.nextStates.add(s);
		}
		//newState.previousStates.addAll(previousStates);
		//newState.nextStates.addAll(nextStates);
		newState.id = this.id;
		
		newState.zone = this.zone.clone();
		
		return newState;
	}
	
	/**
	 * Initializes the LhpnFile to the state given by this LPNState.
	 * @return
	 */
	public void initializeLhpnFile()
	{
	}
	
	/**
	 * Tests for equality.
	 * @return True if o is equal to this object, false otherwise.
	 */
	@Override
	public boolean equals(Object o)
	{
		// Check if the reference is null.
		if(o == null)
		{
			return false;
		}
		
		// Check that the type is correct.
		if(!(o instanceof LPNState))
		{
			return false;
		}
		
		// Check for equality using the LPNState equality.
		return equals((LPNState) o);
	}
	
	
	/**
	 * Tests for equality.
	 * @param state The state to test for equality.
	 * @return True if the two state are equal, false otherwise. Equality is determined by
	 * 			equality is determined based on the same marked places, the boolean values
	 * 			and the zone.
	 */
	public boolean equals(LPNState state)
	{
		// Check if the reference is null first.
		if(state == null)
		{
			return false;
		}
		
		// Check for reference equality.
		if(this == state)
		{
			return true;
		}
		
		// If the hash codes are different, then the objects are not equal. 
		if(this.hashCode() != state.hashCode())
		{
			return false;
		}
		
		if(markedPlaces.length != state.markedPlaces.length ||
				booleanValues.length != state.booleanValues.length)
		{
			return false;
		}
		
		//return markedPlaces.equals(state.markedPlaces) 
				//&& booleanValues.equals(state.booleanValues);
		for(int i=0; i<markedPlaces.length; i++)
		{
			if(markedPlaces[i] != state.markedPlaces[i])
			{
				return false;
			}
		}
		
		for(int i=0; i<booleanValues.length; i++)
		{
			if(booleanValues[i] != state.booleanValues[i])
			{
				return false;
			}
		}
		
		if(this.enabledTimers.length != state.enabledTimers.length)
		{
			return false;
		}
		

//		for (int i = 0; i < enabledTimers.length; i++) {
//			if (enabledTimers[i] != state.enabledTimers[i]) {
//				return false;
//			}
//		}
		
		if(this.zone.length != state.zone.length)
		{
			return false;
		}
		
		for (int i = 0; i < this.zone.length; i++) {
			if (this.zone[i].length != state.zone[i].length) {
				return false;
			}
		}
		
		HashMap<Integer, Integer> timeMap = new HashMap<Integer, Integer>();
		
		timeMap.put(0, 0);
		timeMap.put(1, 1);
		
		// Find the corresponding timer.
		for(int i=0; i<this.enabledTimers.length; i++)
		{
			int index = -1;
			for(int j=0; j<state.enabledTimers.length; j++)
			{
				if(this.enabledTimers[i]== state.enabledTimers[j])
				{
					index = j;
					break;
				}
			}
			
			// If the index is negative, a corresponding timer was not found.
			if(index >= 0)
			{
				timeMap.put(i+2, index+2);
			}
			else
			{
				return false;
			}
		}
		
		for(int i=2; i<this.zone.length; i++)
		{
			for(int j=2; j<this.zone[i].length; j++)
			{
				if(this.zone[i][j] != state.zone[timeMap.get(i)][timeMap.get(j)])
				{
					return false;
				}
			}
		}
		
//		for(int i=0; i<this.zone.length; i++)
//		{
//			for(int j=0; j<this.zone[i].length; j++)
//			{
//				if(this.zone[i][j] != state.zone[i][j])
//				{
//					return false;
//				}
//			}
//		}
		
		return true;
	}
	
	/**
	 * Adds the state to the list of states directly preceding this state.
	 * @param state The state preceding the this state.
	 */
	public void addPreviousState(LPNState state)
	{
		previousStates.add(state);
	}
	
	/**
	 * Adds the state to the list of states directly following this state.
	 * @param state The state that directly follows this state.
	 */
	public void addNextState(LPNState state)
	{
		nextStates.add(state);
	}
	
	/**
	 * Overrides the hashCode.
	 */
	@Override
	public int hashCode()
	{
		if(cachedHashCode <0)		// Check if the hash code has been calculated
		{							// before.
			//cachedHashCode = markedPlaces.hashCode()^booleanValues.hashCode();
			cachedHashCode = createHashCode();
		}

		return cachedHashCode;
	}
	
	private int createHashCode()
	{
		ArrayList<Boolean> hashPlace = new ArrayList<Boolean>();
		ArrayList<Boolean> hashValues = new ArrayList<Boolean>();
		for(int i=0; i<markedPlaces.length; i++)
		{
			hashPlace.add(markedPlaces[i]);
		}
		
		for(int i=0; i<booleanValues.length; i++)
		{
			hashValues.add(booleanValues[i]);
		}
		
		// TODO alter the hash code to take advantage of the zone. 
		
		return Math.abs(hashPlace.hashCode()^hashValues.hashCode());
	}
	
	/**
	 * Get the current markings.
	 * @return A Boolean array of the current markings.
	 */
	public boolean[] getMarkedPlaces()
	{
		return markedPlaces.clone();
	}
	
	/**
	 * Get the current Boolean values.
	 * @return A Boolean array of the current Boolean markings.
	 */
	public boolean[] getBooleanValues()
	{
		return booleanValues.clone();
	}
	
	/**
	 * Overrides the toString method.
	 */
	@Override
	public String toString()
	{	
		String result = "\n";		// The intended result.
		
		if(id >= 0)
		{
			result = "State " + id + " \n";
		}
		else
		{
			result = "State 'no id' : \n";
		}
		
		// Prints the place markings.
		result += "\tPlaces : \n\t\t" + getStringArray(markedPlaces) + "\n";
		
		// Prints the Boolean values.
		result += "\tBoolean Variables : \n\t\t" + getStringArray(booleanValues) + "\n";
		
		// Print the previous states.
		result += "\tPrevious States : \n\t\t" + getStateString(previousStates) + "\n";
		
		// Print the next states.
		result += "\tNext States : \n\t\t" + getStateString(nextStates) + "\n";
		
		result += "\tTimers: " + getStringArray(enabledTimers) + "\n";
		
		// TODO add zone info.
		result += "\tZone: \n";
		for(int i=0; i<zone.length; i++)
		{
			result += "\t\t" + getStringArray(zone[i]) + "\n";
		}
		
		return result;
	}
	
	/**
	 * Returns a HashSet of the states directly previous to this state.
	 * @return Returns a HashSet of the states directly previous to this state.
	 */
	public HashSet<LPNState> getPreviousStates()
	{
		return (HashSet<LPNState>) previousStates.clone();
	}
	
	/**
	 * Returns a HashSet of the states directly following this state.
	 * @return Returns a HashSet of the states directly following this state.
	 */
	public HashSet<LPNState> getNextStates()
	{
		return (HashSet<LPNState>) nextStates.clone();
	}
	
	/**
	 * Creates an id number for this state. The id numbers are given sequentially.
	 */
	public void createId()
	{
		id = numberOfStates;
		numberOfStates++;
	}
	
	/**
	 * Gives the id of this state. To create an id for this instance, call the 
	 * createId method.
	 * @return The id of this state or a negative value if no id has been created.
	 */
	public long getId()
	{
		return id;
	}
	
	/**
	 * Returns the number of states given an id.
	 * @return The total number of states given an id.
	 */
	public static long getNumberOfStates()
	{
		return numberOfStates;
	}
	
	/**
	 * Creates a string representation of an array.
	 * @param array The array to get the string for.
	 * @return A string representation of the array.
	 */
	public static String getStringArray(boolean[] array)
	{
		String result = "[";
		
		if(array.length > 0)
		{
			result += array[0];
		}
		
		for(int i=1; i<array.length; i++)
		{
			result += ", " + array[i];
		}
		
		result += "]";
		
		return result;
	}
	
	public static String getStringArray(int[] array)
	{
		String result = "[";
		
		if(array.length > 0)
		{
			if(array[0] == Integer.MAX_VALUE)
			{
				result += "inf";
			}
			else
			{
				result += array[0];
			}
		}
		
		for(int i=1; i<array.length; i++)
		{
			if(array[i] == Integer.MAX_VALUE)
			{
				result += ", " + "inf";
			}
			else
			{
				result += ", " + array[i];
			}
		}
		
		result += "]";
		
		return result;
	}
	
	/**
	 * Creates a listing of the states with their ids from the HashSet.
	 * @return The string representation.
	 */
	private static String getStateString(HashSet<LPNState> states)
	{
		String result = "";
		for (LPNState s : states)
		{
			result += "State " + s.getId() + " ";
		}
		
		return result;
	}
	
	/**
	 * Allows access to the elements of the zone.
	 * @param i The row of the desired zone element.
	 * @param j The column of the desired zone element.
	 * @return The value of the zone at the i,j location.
	 */
	public int getZoneValue(int i, int j)
	{
		return zone[i][j];
	}
	
	public int[][] getZone()
	{
		return zone;
	}
	
	public int[] getTimers()
	{
		return enabledTimers;
	}
}
