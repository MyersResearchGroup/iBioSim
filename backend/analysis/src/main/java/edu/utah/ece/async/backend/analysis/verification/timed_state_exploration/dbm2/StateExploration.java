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
package edu.utah.ece.async.backend.analysis.verification.timed_state_exploration.dbm2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import javax.swing.JFileChooser;

import edu.utah.ece.async.backend.analysis.verification.timed_state_exploration.dbm2.LPNTranslator.UnsafeLPNException;
import edu.utah.ece.async.dataModels.lpn.parser.*;
import edu.utah.ece.async.dataModels.util.exceptions.BioSimException;

/**
 * This class finds the state graph for an LPN from an LPNFile object.
 * @author Andrew N. Fisher 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class StateExploration {

	
	HashMap<Integer, String> placeNames;	// A dictionary to convert to the place names.
	HashMap<String, Integer> placeIndex;	// A dictionary to convert the name to the index
											// in MarkedPlaces.
	
	HashMap<Integer, String> booleanNames;	// A dictionary to convert to the boolean name.
	HashMap<String, Integer> booleanIndex; // A dictionary to convert the name to the index
											// in booleanValue.
	
	ArrayList<LPNState> States;				// Holds the states found so far.
	
	public static void main(String[] args) throws FileNotFoundException, BioSimException
	{
		// Set up the path for where to store the resulting files.
		String workingDirectory = System.getProperty("user.dir");
		String testFolder = workingDirectory + 
							"\\gui\\src\\verification\\timed_state_exploration\\test\\";
		
		// Create the printStreams for the dot file and log file.
		PrintStream dotFile = new PrintStream(testFolder + "OutPutTestFiles\\testDot.dot");
		PrintStream logFile = new PrintStream(testFolder + "OutPutTestFiles\\testLog.txt");
		
		// Set up a file chooser to get the the lpn file to explore.
		JFileChooser fc = new JFileChooser();
		
		fc.setCurrentDirectory(new File(testFolder + "TestFiles"));
		
		// Launch the file chooser and get the file.
		int returnval = fc.showOpenDialog(null);
		if(returnval == JFileChooser.APPROVE_OPTION){
			
			// Extract the file chosen.
			File fileName = fc.getSelectedFile();
			
			// Create the LhpnFile object and load it with the file.
			LPN lpn = new LPN();
			lpn.load(fileName.getAbsolutePath());
			
			// Run the state exploration algorithm which will produce a log file.
			//ArrayDeque<LPNState> statesFound = exploreStateGraph(lpn, logFile);
			Collection<LPNState> statesFound = exploreStateGraph(lpn, logFile);
			
			
			printDotFile(statesFound, dotFile);
		}
		else
		{
			
		}
		
	}
	
	
	public static void findStateGraph(LPN lpn, String directory, String fileName)
			throws FileNotFoundException
	{
		// Create the printStreams for the dot file and log file.
		
		String name = fileName.replace("lpn", "txt");
		
		PrintStream logFile = new PrintStream(directory + name);
		
		
		
		// Run the state exploration algorithm which will produce a log file.
		//ArrayDeque<LPNState> statesFound = exploreStateGraph(lpn, logFile);
		Collection<LPNState> statesFound = exploreStateGraph(lpn, logFile);
			
		logFile.close();
		
		//PrintStream dotFile = new PrintStream(directory + fileName.replace("lpn", "dot"));
		PrintStream dotFile = new PrintStream(directory + "Graphof" + name.replace("txt", "dot"));
		printDotFile(statesFound, dotFile);
		dotFile.close();
		
	}
	
	/**
	 * Create the dictionaries to translate from the place names to their
	 * internal representation.
	 * @param lpn The associated LHPN
	 */
	private void createPlaceDictionaries(LPN lpn)
	{
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
	 * @param lpn
	 */
	private void createBooleanDictionaries(LPN lpn)
	{
		int i=0;
		for(String s : lpn.getBooleans().keySet())
		{
			booleanIndex.put(s, i);
			booleanNames.put(i, s);
			i++;
		}
	}
	
	/**
	 * Convenience method to get both the place and Boolean dictionaries.
	 * @param lpn
	 */
	@SuppressWarnings("unused")
	private void createDictionaries(LPN lpn)
	{
		createPlaceDictionaries(lpn);
		createBooleanDictionaries(lpn);
	}
	
//	private static ArrayDeque<LPNState> exploreStateGraph(LhpnFile lpn, PrintStream writer)
	private static Collection<LPNState> exploreStateGraph(LPN lpn, PrintStream writer)
	{
		// Stacks for the states remaining to explore and the states that are found.
		ArrayDeque<LPNTransitionState> statesToExplore = new ArrayDeque<LPNTransitionState>();
		//ArrayDeque<LPNState> statesFound = new ArrayDeque<LPNState>();
		HashMap<LPNState, LPNState> statesFound = new HashMap<LPNState, LPNState>();
		
		LPNTranslator transLPN = new LPNTranslator(lpn);
		
		// Print the lexicon.
		writer.println(transLPN);
		
		// Set the initial place markings.
		// Set the initial boolean markings.
		// Set the initial state space
		LPNTransitionState currentState = new LPNTransitionState(transLPN);
		
		// TODO Change finding enabled transitions to new methods.
		
		// Get the initial set of enabled transitions.
		currentState.initializeEnabledTransitions(transLPN);
		
		// Create an id for the first state.
		currentState.createStateId();
		
		// Print the initial state.
		writer.println("The initial state is: " + currentState);
		
		// Set the done flag
		boolean done = false;
		
		// Add initial state to found states.
		//statesFound.add(currentState.getState());
		statesFound.put(currentState.getState(), currentState.getState());
		
		// Loop until done is true.
		while (!done)
		{
			// Check if there are transitions to fire. If not
			// then this path has hit a dead end.
			if(!currentState.remainingTransitions())
			{
				// check if the stack is empty
				if(!statesToExplore.isEmpty())
				{
					// if not, pop the first record and make that the new state
					// to explore from.
					currentState = statesToExplore.pop();
					writer.println("I have reach a dead end and am going back to State " + 
							currentState.getState().getId());
					continue;
				}
				// if it is empty, break out of the loop.
				break;
			}
			// Get an enabled transition
			int transition = currentState.getATransition();
			
			// if there are still enabled transitions, push
			// them on the stack for later
			if(currentState.remainingTransitions())
			{
				statesToExplore.push(currentState);
			}
			
			writer.println("I am firing transition " + transLPN.getTransitionName(transition));
			
			// Update the markings in a new variable
			// Update the boolean values in a new variable
			LPNState nextState;
			HashSet<Integer> enabledTransitions = new HashSet<Integer>();
			try
			{	
				nextState = currentState.getUpdatedState(transition, transLPN,
						enabledTransitions);
			}
			catch(UnsafeLPNException e)
			{
				writer.println(e.getMessage());
				//return	statesFound;
				return statesFound.keySet();
			}
			
			writer.println("State found\n");
			// If the new state is not one seen before
			//if(!statesFound.contains(nextState))
			if(!statesFound.keySet().contains(nextState))
			{
				writer.println("State not found before. Adding to states.");
				
				// create an id for this state
				nextState.createId();
				
				// add it to the set of states
				//statesFound.add(nextState);
				statesFound.put(nextState, nextState);
				
				// add the appropriate edge
				currentState.getState().addNextState(nextState);
				
				nextState.addPreviousState(currentState.getState());

				// TODO create a constructor that takes the enabled transitions.
				// and use is here. Then eliminate the initializeEnabledTransitions
				// step.
				
				currentState = new LPNTransitionState(nextState, enabledTransitions);
				
				// make this state the new state to explore from.
				//currentState = new LPNTransitionState(nextState);
				
				
				// Get the new set of enabled transitions.
				//currentState.initializeEnabledTransitions(transLPN);
				
				writer.println(currentState);
				
			}
			// If the new state is one seen before
			else{
				writer.println("State found before." + statesFound.get(nextState));
				
				//if(checkBooleanArrays())
				
				// Get the previous found state's object for adding edges.
				LPNState previousFoundState = statesFound.get(nextState);
				
				// Add edges.
				currentState.getState().addNextState(previousFoundState);
				previousFoundState.addPreviousState(currentState.getState());
				
				
				// check if the stack is empty
				if(!statesToExplore.isEmpty())
				{
					// if not, pop the first record and make that the new state
					// to explore from.
					currentState = statesToExplore.pop();
					writer.println("I am going back to state " + currentState.getState().getId());
				}
				// if it is empty, set the done flag to true.
				else
				{
					done = true;
				}
			}
			
		}
		
		// Return the set of states.
		//return statesFound;
		return statesFound.keySet();
	}
	
	/**
	 * Checks if the two arrays are equal.
	 * @param array1
	 * @param array2
	 * @return True if the two arrays are equal, false otherwise.
	 */
	@SuppressWarnings("unused")
	private static boolean checkBooleanArrays(boolean[] array1, boolean[] array2)
	{
		if(array1 == null)
		{
			return array2 == null;
		}
		else if(array1.length  == 0)
		{
			return array2.length == 0;
		}
		
		for(int i=0; i<array1.length; i++)
		{
			if(array1[i] != array2[i])
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Prints a dot file of the states.
	 * @param states The states to make the dot file from.
	 * @param writer The stream for printing the result.
	 */
	//private static void printDotFile(ArrayDeque<LPNState> states, PrintStream writer)
	private static void printDotFile(Collection<LPNState> states, PrintStream writer)
	{
		// Print the opening header for the dot file.
		writer.println("digraph G {");
		
		for(LPNState s : states)
		{
			try{
			System.out.println(s);
			}
			catch(Exception e){
				
			}
			
			for(LPNState nextS : s.getNextStates())
			{
				writer.println("\t\"S " + s.getId() +LPNState.getStringArray(s.getBooleanValues())
						+ "\" -> \"S " + nextS.getId() + 
						LPNState.getStringArray(nextS.getBooleanValues()) + "\"");
			}
		}
		
		writer.print("}");
	}
}