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
package edu.utah.ece.async.verification.timed_state_exploration.dbm2;
import java.util.HashMap;
import java.util.HashSet;

import edu.utah.ece.async.dataModels.lpn.*;

/**
 * This class mainly adds the ability to address the places and Boolean variables via
 * integers instead of names, that is, an LPNBool imposes an ordering on the places and
 * Boolean variables. This enables methods that ask whether the ith place is marked or
 * the ith Boolean variable is true.
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class LPNTranslator{
	
	private HashMap<Integer, String> placeNames;	// A dictionary to convert to the place names.
	private HashMap<String, Integer> placeIndex;	// A dictionary to convert the name to the 
													// index in MarkedPlaces.
	
	private HashMap<Integer, String> booleanNames;	// A dictionary to convert to the boolean name.
	private HashMap<String, Integer> booleanIndex;  // A dictionary to convert the name to the
													// index in booleanValue.
	
	private HashMap<Integer, String> transitionNames; // A diction to convert to the transition 
													  //name.
	private HashMap<String, Integer> transitionIndex; // A dictionary to convert the name 
													  // of the transition to its index.
	

	private LPN lpn;		// The LPN to translate between.
	
	/**
	 * Creates a translator between the names of the associated Lhpnfile and
	 * the indices.
	 * @param lpn
	 */
	public LPNTranslator(LPN lpn)
	{
		this.lpn = lpn;
		createDictionaries();	// Create the dictionaries that translate between
								// the names of places, transitions, etc... and
								// their indexed value.
	}

	/**
	 * Create the dictionaries to translate from the place names to their
	 * internal representation.
	 */
	private void createPlaceDictionaries()
	{
		// Create the dictionaries.
		placeIndex = new HashMap<String, Integer>();
		placeNames = new HashMap<Integer, String>();
		
		int i = 0;
		for(String s : lpn.getPlaceList())
		{
			placeIndex.put(s, i);
			placeNames.put(i, s);
			i++;
		}
	}
	
	/**
	 * Create the dictionaries to translate from the Boolean names to their
	 * internal representation.
	 */
	private void createBooleanDictionaries()
	{
		// Create the dictionaries.
		booleanIndex = new HashMap<String, Integer>();
		booleanNames = new HashMap<Integer, String>();
		
		int i=0;
		for(String s : lpn.getBooleans().keySet())
		{
			booleanIndex.put(s, i);
			booleanNames.put(i, s);
			i++;
		}
	}
	
	/**
	 * Create the dictionaries to translate from the transition names to their
	 * internal representation.
	 */
	private void createTransitionDictionaries()
	{
		// Create the dictionaries.
		transitionIndex = new HashMap<String, Integer>();
		transitionNames = new HashMap<Integer, String>();
		
		int i=0;
		for(String s: lpn.getTransitionList())
		{
			transitionIndex.put(s, i);
			transitionNames.put(i, s);
			i++;
		}
	}
	
	/**
	 * Convenience method to get the place, Boolean and transition dictionaries.
	 */
	private void createDictionaries()
	{
		createPlaceDictionaries();
		createBooleanDictionaries();
		createTransitionDictionaries();
	}
	
	/**
	 * Get the initial markings for the places.
	 * @return A Boolean array where the ith value is true if the ith place
	 * is marked, false otherwise.
	 */
	public boolean[] getInitialMarkings()
	{
		       
		boolean[] boolValues = new boolean[placeNames.size()];
		
		for(int i=0; i<boolValues.length; i++)
		{
			if(lpn.getInitialMarking(placeNames.get(i)))
			{
				boolValues[i] = true;
			}
			else
			{
				boolValues[i] = false;
			}
		}
		
		return boolValues;
	}
	
	/**
	 * Gets the initial markings for the Boolean variables.
	 * @return A Boolean array where the ith value is true if the ith Boolean
	 * variable is true, false otherwise.
	 */
	public boolean[] getInitialBooleanValues()
	{
		boolean[] boolValues =  new boolean[booleanNames.size()];
		
		for(int i=0; i<boolValues.length; i++)
		{
			if(lpn.getInitialVal(booleanNames.get(i)).equals("true"))
			{
				boolValues[i] = true;
			}
			else
			{
				boolValues[i] = false;
			}
		}
		
		return boolValues;
	}
	
	/**
	 * Gets the name of the transition from the associated number.
	 * @param transition The number of the transition.
	 * @return	The name of the transition, or null if there is no transition
	 * associated with the given integer.
	 */
	public String getTransitionName(int transition)
	{
		return transitionNames.get(transition);
	}
	
	/**
	 * Gets the index associated with the given transition name.
	 * @param transition The name of the transition.
	 * @return The index of the transition, or null if there is no transition
	 * associated with the given string.
	 */
	public int getTransitionIndex(String transition)
	{
		return transitionIndex.get(transition);
	}
	
	/**
	 * Gets the name of the place from the associated number.
	 * @param place The number of the place.
	 * @return	The name of the place, or null if there is no place associated with
	 * the given integer.
	 */
	public String getPlaceName(int place)
	{
		return placeNames.get(place);
	}
	
	/**
	 * Gets the index of the place from the associated name.
	 * @param place The name of the place.
	 * @return The index of the place, or null if there is no place associted with
	 * the given string.
	 */
	public int getPlaceIndex(String place)
	{
		return placeIndex.get(place);
	}
	
	/**
	 * Gets the name of the Boolean variable from the associated number.
	 * @param bool The number of the Boolean variable.
	 * @return The name of the Boolean variable, or null if there is not a place
	 * associated with the given integer.
	 */
	public String getBooleanName(int bool)
	{
		return booleanNames.get(bool);
	}
	
	/**
	 * Gets the index of the Boolean variable from the associated name.
	 * @param bool The name of the Boolean variable.
	 * @return The index of the Boolean variable, or null if there is not a place
	 * associated with the given name.
	 */
	public int getBooleanIndex(String bool)
	{
		return booleanIndex.get(bool);
	}
	
	/**
	 * Modifies the current Boolean values to reflect the new values when a given 
	 * transition fires. Thus, if any of the Boolean variables change, the 
	 * Boolean array that is passed will be changed to reflect it.
	 * @param transition The transition firing.
	 * @param currentBooleanValues The current values of the Boolean variables.
	 */
	public boolean[] getNewBooleanValues(int transition, boolean[] currentBooleanValues)
	{	
		// Get the name of the transition for communicating with the LhpnFile object.
		String currentTransition = transitionNames.get(transition);
		
		// A HashMap of the names of the Boolean variables with string values
		// ("true" for true and "false" for false) is required for evaluating the
		// new expression.
		HashMap<String, String> currentStringBooleanValues = new HashMap<String, String>();
		
		for(int i=0; i<currentBooleanValues.length; i++)	// Translate the truth values
		{													// of the Boolean variables
			if(currentBooleanValues[i])						// into a string form.
			{
				currentStringBooleanValues.put(booleanNames.get(i), "true");
			}
			else
			{
				currentStringBooleanValues.put(booleanNames.get(i), "false");
			}
		}
	
		// Get a copy of the current Boolean values.
		boolean[] newValues = currentBooleanValues.clone();
		
		for(int i = 0; i<currentBooleanValues.length; i++) // Get the expression tree for each
		{											   // Boolean variable to determine its
													   // new value.
			ExprTree boolTree = lpn.getBoolAssignTree(currentTransition, booleanNames.get(i));
			
			if(boolTree != null)	// If the expression tree exist, evaluate it.
			{
				newValues[i] = boolTree.evaluateExpr(currentStringBooleanValues) > 0;
			}
		}
		
		return newValues;
	}
	
	/**
	 * Modifies the current markings to reflect the situation when the given transition
	 * fires. More specifically, this method sets the truth value to false if 
	 * a given making is in the preset of the transition and sets the truth value to 
	 * true if the transition is in the postset of the transition.
	 * @param transition The transition to fire.
	 * @param currentMarkings The current places that are marked.
	 */
	public boolean[] getNewMarking(int transition, boolean[] currentMarkings)
	{	
		// Create a copy of the currentMarkings to return.
		boolean[] newMarkings = currentMarkings.clone();
		
		// Get the name of the transition that is firing.
		String transitionToFire = transitionNames.get(transition);
		
		// If the transition does not exist, then return the given markings.
		if(transitionToFire == null)
		{
			return newMarkings;
		}
		
		// Get the names of the places in the preset and postset
		String[] presetPlaces = lpn.getPreset(transitionToFire);
		String[] postsetPlaces = lpn.getPostset(transitionToFire);
		
		// Remove the preset places from the marked places.
		for(int i = 0; i<presetPlaces.length; i++)
		{
			// Get the index of the place whose name is given in presetPlaces[i], then
			// change the corresponding boolean value to false.
			newMarkings[placeIndex.get(presetPlaces[i])] = false;
		}
		
		// Add the postset places to the marked places.
		for(int i=0; i< postsetPlaces.length; i++)
		{
			// Get the index of the place whose name is given in postsetPlaces[i], then
			// change the corresponding boolean value to true.
			int index = placeIndex.get(postsetPlaces[i]);
			if(newMarkings[index])
			{
				throw new UnsafeLPNException("\nThe LPN is not safe.\n");
			}
			newMarkings[index] = true;
		}
		
		return newMarkings;
	}
	
	/**
	 * Checks if the given transition is enabled. (Note: This check is performed using only
	 * markings.)
	 * @param transition The transition under consideration.
	 * @param currentMarkings A Boolean array representing the current marked states.
	 * @return True if the transition is enabled, false otherwise.
	 */
	public boolean isEnabled(int transition, boolean[] currentMarkings)
	{	
		// Get the name of the transition.
		String transitionName = transitionNames.get(transition);
		
		// If the transition does not exit, return false.
		if(transitionName == null)
		{
			return false;
		}
		
		// TODO Add zone check.
		
		return isEnabled(transitionName, currentMarkings);
	}
	
	/**
	 * Gets the transitions that are enabled given the current marking.
	 * @param currentMarkings Boolean array that represents the current marking.
	 * @return A HashSet<Integer> that contains the indices of the enabled transitions.
	 */
	public HashSet<Integer> getEnabledTransitions(boolean[] currentMarkings)
	{
		// Get a hash set to store the enabled transitions as they are found.
		HashSet<Integer> enabledTransitions = new HashSet<Integer>();
		
		for(String s : lpn.getTransitionList())
		{
			if(isEnabled(s, currentMarkings))
			{
				enabledTransitions.add(transitionIndex.get(s));
			}
		}
		
		return enabledTransitions;
	}
	
	
	public HashSet<Integer> getEnabledTransitions(LPNTransitionState state)
	{
		// Get a hash set to store the enabled transitions as they are found.
		HashSet<Integer> enabledTransitions = new HashSet<Integer>();
		
		for(String s : lpn.getTransitionList())
		{
			if(isEnabled(s, state) && hasMetDelay(s, state))
			{
				enabledTransitions.add(transitionIndex.get(s));
			}
		}
		
		return enabledTransitions;
	}
	
	public void getEnabledTransitions(LPNTransitionState state, HashSet<Integer> enabledTransitions)
	{
		for(String s : lpn.getTransitionList())
		{
			//if(isEnabled(s, state) && hasMetDelay(s, state))
			if(isEnabled(s, state))
			{
				enabledTransitions.add(transitionIndex.get(s));
			}
		}
	}
	
	/**
	 * Private method that checks if a transition is enabled using the transitions
	 * name.
	 * @param transition The name of the transition to check.
	 * @param currentMarkings The current markings.
	 * @return True if the transition is enabled, false otherwise.
	 */
	private boolean isEnabled(String transition, boolean[] currentMarkings)
	{
		for(String s : lpn.getPreset(transition))
		{
			// Check if the place is marked in the current marking.
			if(currentMarkings[placeIndex.get(s)] != true)
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Private method that checks if a transition is enabled using the transitions
	 * name.
	 * @param transition The name of the transition to check.
	 * @param currentMarkings The current markings.
	 * @return True if the transition is enabled, false otherwise.
	 */
	private boolean isEnabled(String transition, LPNTransitionState state)
	{
		for(String s : lpn.getPreset(transition))
		{
			// Check if the place is marked in the current marking.
			if(state.getState().getMarkedPlaces()[placeIndex.get(s)] != true)
			{
				return false;
			}
		}
		
		//return hasMetDelay(transition, state);
		return true;
	}
	
	/**
	 * Overrides the toString method. It prints the lexicon.
	 */
	@Override
	public String toString()
	{
		String result = "";
		
		// Add the places
		result += placeIndex + "\n";
		
		// Add the Boolean values.
		result += booleanIndex + "\n";
		
		// Add the transitions.
		result += transitionIndex + "\n";
		
		return result;
	}
	
	/**
	 * Defines an exception thrown when 
	 */
	public class UnsafeLPNException extends RuntimeException
	{
		/**
		 * The default serialVersionUID.
		 */
		private static final long serialVersionUID = 1L;

		public UnsafeLPNException(String message)
		{
			super(message);	// Use the default constructor.
		}
	}
	
	private boolean hasMetDelay(String transition, LPNTransitionState state)
	{
		return state.hasMetDelay(transitionIndex.get(transition));
	}
	
	public int getDelayLowerBound(int transition, boolean[] booleanMarkings)
	{
		HashMap<String, String> values = createBooleanHashMap(booleanMarkings);
		
		ExprTree exp = lpn.getDelayTree(transitionNames.get(transition));
		
		if(exp.getOp() !=null && exp.getOp().equals("uniform"))
		{
			exp = exp.getLeftChild();
			return -1*(int) exp.evaluateExpr(values);
		}
		return -1 *(int) exp.evaluateExpr(values);
	}
	
	private HashMap<String, String> createBooleanHashMap(boolean[] booleanMarkings)
	{
		HashMap<String, String> values = new HashMap<String, String>();
		
		for(int i=0; i<booleanMarkings.length; i++)
		{
			if(booleanMarkings[i] == true)
			{
				values.put(booleanNames.get(i), "true");
			}
			else
			{
				values.put(booleanNames.get(i), "false");
			}
		}
		
		return values;
	}
	
	public int getDelayUpperBound(int transition, boolean[] booleanMarkings)
	{
		HashMap<String, String> values = createBooleanHashMap(booleanMarkings);
		
		ExprTree exp = lpn.getDelayTree(transitionNames.get(transition));
		
		if(exp.getOp() !=null && exp.getOp().equals("uniform"))
		{
			exp = exp.getRightChild();
			return (int) exp.evaluateExpr(values);
		}
		return (int) exp.evaluateExpr(values);
	}
}
