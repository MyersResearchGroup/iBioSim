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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class is for storing and manipulating timing zones via difference bound matrices.
 * The underlying structure is backed by linked lists which yields the name
 * DBMLL (difference bound matrix linked list). A difference bound matrix has the form
 *     t0  t1  t2  t3
 * t0 m00 m01 m02 m03
 * t1 m10 m11 m12 m13
 * t2 m20 m21 m22 m23
 * t3 m30 m31 m32 m33
 * where  tj - ti<= mij. In particular, m0k is an upper bound for tk and -mk is a lower
 * bound for tk.
 * 
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class DBMLL {
	
	// Abstraction Function : 
	// The difference bound matrix is represented by ArrayList<ArrayList<Integer>> matrix.
	// To match the philosophy of a two dimensional array being stored as rows, 
	// each ArrayList<Integer> represents a row of the matrix.
	// For example:
	// 1 2 3 this row is a ArrayList<Integer> stored at index zero
	// 4 5 6 this row is a ArrayList<Integer> stored at index one
	// 7 8 9 this row is a ArrayList<Integer> stored at index two.
	// In order to keep track of the upper and lower bounds of timers from when they are first
	// enabled, the matrix will be augmented by a row and a column. The first row will contain
	// the upper bounds and the first column will contain the negative of the lower bounds.
	// For one timer t1 that is between 2 and 3, we might have
	//    lb t0 t1
	// ub  x  0  3
	// t0  0  m  m
	// t1  -2 m  m
	// where x is not important (and will be given a zero value), 3 is the upper bound on t1
	// and -2 is the negative of the lower bound. The m values represent the actual difference
	// bound matrix. Also note that the column heading are not part of the stored representation
	// lb stands for lower bound while ub stands for upper bound.
	// This upper and lower bound information is called the Delay for a Transition object.
	// Since a timer is tied directly to a Transition, the timers are index by the corresponding
	// Transition's index in a LPNTranslator.
	
	// Representation invariant :
	// Integer.MAX_VALUE is used to logically represent infinity.
	// The lb and ub values for a timer should be set when the timer is enabled.
	
	private ArrayList<ArrayList<Integer>> matrix;	// The underlying matrix.
	
	private HashMap<Integer, Integer> enabledTimers;	// Maps timer to their index.
	private HashMap<Integer, Integer> getTimer;			// Maps the index to the timer.
	
	public static final int INFINITY = Integer.MAX_VALUE;
	
	
	public DBMLL()
	{
		matrix = new ArrayList<ArrayList<Integer>>();
		enabledTimers = new HashMap<Integer, Integer>();
		getTimer = new HashMap<Integer, Integer>();
		
	}
	
	/**
	 * Creates a difference bound matrix based off the initial markings. 
	 * @param trans The associated LPN.
	 */
	public DBMLL(LPNTranslator lpn)
	{
//		int counter = 0;	// Counter for keeping track of the number of transitions
//							// being included in the zone.
		
		enabledTimers = new HashMap<Integer, Integer>();
		getTimer = new HashMap<Integer, Integer>();
		
		// Get the enabled transitions.
		HashSet<Integer> transitions = lpn.getEnabledTransitions(lpn.getInitialMarkings());
		
		initializeZone(lpn, transitions);
		
		advanceTime();
		
		recanonicalize();
		
//		matrix = new ArrayList<ArrayList<Integer>>();	// Set up the matrix.
//		
//		ArrayList<Integer> zeroRow = new ArrayList<Integer>(); 	// Create the zeroth row.
//		zeroRow.add(0); 	// This will end up being the (0,0) position of the matrix.
//							// which is the 'x' in the second diagram of the 
//							// of the Abstraction Function. It will be given zero value.
//		
//		// Initialize the zero row to have the upper bounds of each timer.
//		for(Integer i : transitions)
//		{
//			enabledTimers.put(i, counter);	// Add an entry tying the timer to 
//											// its index in the DBM part of matrix.
//			
//			getTimer.put(counter, i);		// Add the reverse look up.
//			
//			counter++;
//			
//			zeroRow.add(lpn.getDelayLowerBound(i));	// Initialize the upper bound.
//		}
//		
//		// Add the zero row to the matrix.
//		matrix.add(zeroRow);
//		
//		// Create the other rows of the matrix.
//		for(int i=0; i<transitions.size(); i++)
//		{
//			// Create the row.
//			ArrayList<Integer> row = new ArrayList<Integer>();
//			
//			// Set up the lower bound for the ith timer.
//			row.add(lpn.getDelayLowerBound(getTimer.get(i)));
//			
//			// Fill up the array with zeros.
//			for(int j=0; j<transitions.size(); j++)
//			{
//				row.add(0);
//			}
//			matrix.add(row);
//		}
		
		//setUpperBounds(lpn);
		
		//setLowerBounds(lpn);
		
	}
	
	private void initializeZone(LPNTranslator lpn, Iterable<Integer> timers)
	{
		// check logic for zero timer.
		
		int counter =2;
		matrix = new ArrayList<ArrayList<Integer>>();	// Set up the matrix.
		
		ArrayList<Integer> zeroRow = new ArrayList<Integer>(); 	// Create the zeroth row.
		zeroRow.add(0); 	// This will end up being the (0,0) position of the matrix.
							// which is the 'x' in the second diagram of the 
							// of the Abstraction Function. It will be given zero value.
		zeroRow.add(0);		// upper bound for the t0 timer.
		
		// Initialize the zero row to have the upper bounds of each timer.
		for(Integer i : timers)
		{
			enabledTimers.put(i, counter);	// Add an entry tying the timer to 
											// its index in the DBM part of matrix.
			
			getTimer.put(counter, i);		// Add the reverse look up.
			
			counter++;
			
			// Initialize the upper bound.
			zeroRow.add(lpn.getDelayUpperBound(i, lpn.getInitialBooleanValues()));
		}
		
		// Add the zero row to the matrix.
		matrix.add(zeroRow);
		
		// Create the t0 row.
		ArrayList<Integer> t0Row = new ArrayList<Integer>();
		for(int i=0; i<counter; i++)	// Counter is +2 since there are two additional columns than just
		{								// the enabled timers (the lb column and t0 column).
			t0Row.add(0);
		}
		
		matrix.add(t0Row);
		
		// Create the other rows of the matrix.
		for(int i=2; i<counter; i++)
		{
			// Create the row.
			ArrayList<Integer> row = new ArrayList<Integer>();
			
			// Set up the lower bound for the ith timer.
			row.add(lpn.getDelayLowerBound(getTimer.get(i), lpn.getInitialBooleanValues()));
			
			// Fill up the array with zeros.
			for(int j=0; j<counter-1; j++)	// Counter is plus one since the lb column has been initialized
			{								// but the t0 column has not.
				row.add(0);
			}
			matrix.add(row);
		}
	}
	
//	private void setLowerBounds(LPNTranslator lpn)
//	{
//		for(Integer i : enabledTimers.keySet())
//		{
//			
//		}
//		
//	}
//
//	private void setUpperBounds(LPNTranslator lpn) 
//	{
//		for(Integer i : enabledTimers.keySet())
//		{
//			
//		}
//		
//	}

	/**
	 * Creates a difference bound matrix that represents the given matrix.
	 * @param trans The associated LPN.
	 * @param dbm The matrix to begin with.
	 */
//	public DBMLL(LPNTranslator lpn, int[][] dbm)
//	{
//		
//	}
	
//	public DBMLL(LPNTranslator lpn, LPNState state)
//	{
//		
//	}
	
//	public DBMLL(int[][] zone, int[] timers)
//	{
//		
//	}
	
	public DBMLL(LPNState state)
	{
		matrix = new ArrayList<ArrayList<Integer>>();
		
		int[][] zone = state.getZone();
		
		for(int i=0; i<zone.length; i++)
		{
			ArrayList<Integer> row = new ArrayList<Integer>();
			
			for(int j=0; j<zone[0].length; j++)
			{
				row.add(zone[i][j]);
			}
			
			matrix.add(row);
		}
		
		
		int[] timers = state.getTimers();
		
		enabledTimers = new HashMap<Integer, Integer>();
		getTimer = new HashMap<Integer, Integer>();
		for(int i=0; i<timers.length; i++)
		{
			getTimer.put(i+2, timers[i]);
			enabledTimers.put(timers[i], i+2);
		}
		
	}
	
	/**
	 * Creates the difference bound matrix that represent the initial zone.
	 * @param lpn The associated LPN.
	 * @return The difference bound matrix.
	 */
//	public static int[][] intialZone(LPNTranslator lpn)
//	{
//		return null;
//	}
	
	/**
	 * Determines whether two BMLL objects are equal.
	 * @param otherZone The DMBLL to compare.
	 * @return True if they are equal, and false otherwise.
	 */
//	public boolean equals(DBMLL otherZone)
//	{
//		// Check for same clocks.
//		Set<Integer> thisTimers = enabledTimers.keySet();
//		Set<Integer> otherTimers = otherZone.enabledTimers.keySet();
//		
//		if(thisTimers.size() != otherTimers.size())
//		{
//			return false;
//		}
//		
//		for(Integer i : thisTimers)
//		{
//			if(!otherTimers.contains(i))
//			{
//				return false;
//			}
//		}
//		
//		for(int i=0; i<matrix.size(); i++)
//		{
//			for(int j=0; j<matrix.get(0).size(); j++)
//			{
//				if( get(i,j) != otherZone.get(i,j))
//				{
//					return false;
//				}
//			}
//		}
//		
//		// Check the zero row
//		for(int i=0; i<matrix.size(); i++)
//		{
//			
//		}
//		
//		return true;
//	}
	
	/**
	 * Determines if the transition has met its delay.
	 * @param transition The transition to check.
	 * @param zone The zone to consider.
	 * @return True if the transition has met the delay, false otherwise.
	 */
//	public static boolean hasMetDelay(int transition, int[][] zone)
//	{
//		return get(0, enabledTimers.get(transition) );
//	}
	
	public int[][] getZone()
	{
		int[][] zone = new int[enabledTimers.size()+2][enabledTimers.size()+2];	// Size is the number of 
																				// timers plus lb column
																				// plus t0 timer.
		int rowCounter = 0;
		int columnCounter =0;
		for(int i=0; i<matrix.size(); i++)
		{
			if(i != 0 && i!=1 &&  !getTimer.containsKey(i))	// i-2 since the timers
			{													// are indexed at zero
				continue;
			}
			for(int j=0; j<matrix.size(); j++)
			{
				if( j != 0 && j != 1&& !getTimer.containsKey(j))	// j-2 since the timers
				{													// are indexed at zero
					continue;
				}
				zone[rowCounter][columnCounter] = matrix.get(i).get(j);
				columnCounter++;
			}
			
			rowCounter++;
			columnCounter = 0;
		}
		
		return zone;
	}
	
	public int[] getTimers()
	{
		int[] result = new int[enabledTimers.size()];
		
//		Set<Integer> timers = enabledTimers.keySet(); 
//		
//		int counter =0;
//		for(Integer i : timers)
//		{
//			result[counter++] = i;
//		}
		
		
		int counter =0;
		for(int i=2; i<matrix.size(); i++)
		{
			if(!getTimer.containsKey(i))
			{
				continue;
			}
			result[counter++] = getTimer.get(i);
		}
		
		return result;
	}

	public DBMLL updateZone(int transition, LPNTranslator lpn,
			boolean[] newBooleanValues, HashSet<Integer> enabledTransitions)
	{
		
		
		// Restrict to lower bound.
		restrictToLowerBound(transition);
		
		// Tighten loose bounds.
		recanonicalize();
		
		// Project out timer.
		project(transition);
		
		// Delete any timer that has lost its token.
		ArrayList<Integer> integersToDelete = new ArrayList<Integer>();
		
		for(Integer i : enabledTimers.keySet())
		{
			if(!enabledTransitions.contains(i))
			{
				integersToDelete.add(i);
			}
		}
		
		for(Integer i : integersToDelete)
		{
			project(i);
		}
		
		
		//int currentSize = enabledTimers.size();
		
		HashSet<Integer> newTransitions = new HashSet<Integer>();
		for(Integer i : enabledTransitions)
		{
			if(!enabledTimers.keySet().contains(i))
			{
				newTransitions.add(i);
			}
		}
		
		
		for(Integer i : newTransitions)
		{
			//enabledTimers.put(i, currentSize);
			//getTimer.put(currentSize, i);
			
			enabledTimers.put(i, matrix.size());
			getTimer.put(matrix.size(), i);
			
			matrix.get(0).add(lpn.getDelayUpperBound(i, newBooleanValues));
			matrix.get(1).add(0);				// initialize zero element to zero.
			for(int j=2; j< matrix.size(); j++)
			{
				if(!getTimer.containsKey(j))	// If this timer has been deleted, continue.
				{
					matrix.get(j).add(0);
					continue;
				}
				
				if(newTransitions.contains(getTimer.get(j)))	// If the transition is new
				{
					matrix.get(j).add(0);		// j+1 since timers start at 1 but are zero
												// based indexed.
				}
				else
				{
					matrix.get(j).add(getEntry(j, 1));
				}
			}
			
			ArrayList<Integer> row = new ArrayList<Integer>();
			
			row.add(lpn.getDelayLowerBound(i, newBooleanValues));
			row.add(0);						// Initialize zero element to zero.
			for(int j=2; j<matrix.get(0).size(); j++)
			{
				if(!getTimer.containsKey(j))	// If the timer has been deleted, continue.
				{
					row.add(0);
					continue;
				}
				
				if(newTransitions.contains(getTimer.get(j)))
				{
					row.add(0);
				}
				else
				{
					row.add(getEntry(1,j));
				}
			}
			
			matrix.add(row);
		}
		
		// Advance time.
		for(int i=2; i<matrix.get(0).size(); i++)
		{
			matrix.get(1).set(i, getEntry(0, i));
		}
		
		recanonicalize();
		
		// Remove from enabledTransition any transition that have not
		// reached their lower bounds.
		integersToDelete.clear();
		for(Integer i : enabledTransitions)
		{
			if(!hasMetDelay(i))
			{
				integersToDelete.add(i);
			}
		}
		
		for(Integer i : integersToDelete)
		{
			enabledTransitions.remove(i);
		}
		
		return null;
	}
	
	private void advanceTime()
	{
		for(int i=2; i<matrix.get(0).size(); i++)
		{
			matrix.get(1).set(i, getEntry(0,i));
		}
		
	}
	
	/**
	 * Not indexed by timers.
	 * @param i
	 * @param j
	 */
	private int getEntry(int i, int j)
	{
		return matrix.get(i).get(j);
	}
	
	private void project(int transition)
	{
		getTimer.remove(enabledTimers.get(transition));
		enabledTimers.remove(transition);
	}

	private void recanonicalize()
	{
		for(int k=0; k< matrix.size(); k++)
		{
			if(k !=1 && !getTimer.containsKey(k))
			{
				continue;
			}
			for(int i=0; i<matrix.size(); i++)
			{
				if(i!=1 && !getTimer.containsKey(i))
				{
					continue;
				}
				for(int j=1; j<matrix.size(); j++)
				{
					if(j !=1 && !getTimer.containsKey(j) || getEntry(i,k) == INFINITY||
							getEntry(k,j) == INFINITY)
					{
						continue;
					}
					
					if(getEntry(i,j) > getEntry(i,k) + getEntry(k,j))
					{
						setEntry(i,j, getEntry(i,k) + getEntry(k,j));
						if(i==j && getEntry(i,j) != 0)
						{
							throw new NegativeDiagonalException("A diagonal entry was set" +
									"to a negative value");
						}
					}
				}
			}
		}
	}

	public boolean hasMetDelay(int timer)
	{
		return enabledTimers.keySet().contains(timer) ? 
				getEntry(1, enabledTimers.get(timer))>-1*getLowerBound(timer) : false;
	}
	
	public HashSet<Integer> hasMetDelay()
	{
		HashSet<Integer> timers = new HashSet<Integer>();
		
		for(Integer i : enabledTimers.keySet())
		{
			if(hasMetDelay(i))
			{
				timers.add(i);
			}
		}
		
		return timers;
	}
	
	public int getLowerBound(int timer)
	{
		return matrix.get(enabledTimers.get(timer)).get(0);
	}
	
	private void restrictToLowerBound(int timer)
	{
		matrix.get(enabledTimers.get(timer)).set(1, getLowerBound(timer));
	}
	
//	private void advanceToUpperBound(int timer)
//	{
//		matrix.get(1).set(enabledTimers.get(timer), getUpperBound(timer));
//	}
	
	public int getUpperBound(int timer)
	{
		// fix.
		return matrix.get(0).get(enabledTimers.get(timer));
	}
	
//	public int getMatrixValue(int timer1, int timer2)
//	{
//		return getEntry(enabledTimers.get(timer1), enabledTimers.get(timer2));
//	}
	
//	private void set(int i, int j)
//	{
//		
//	}
	
	@SuppressWarnings("unused")
	private int get(int i, int j)
	{
		return matrix.get(i+1).get(j+1);
	}

	/**
	 * Indexed by timers.
	 * @param i
	 * @param j
	 * @param value
	 */
	@SuppressWarnings("unused")
	private void set(int i, int j, int value)
	{
		matrix.get(i+1).set(j+1, value);
	}
	
	private void setEntry(int i, int j, int value)
	{
		matrix.get(i).set(j, value);
	}
	
	
	@Override
	public DBMLL clone()
	{
		DBMLL newZone = new DBMLL();
		
		//newZone.matrix = (ArrayList<ArrayList<Integer>>) this.matrix.clone();
		//newZone.enabledTimers = (HashMap<Integer, Integer>) this.enabledTimers;
		//newZone.getTimer = (HashMap<Integer,Integer>) this.getTimer;
		
		newZone.matrix = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<this.matrix.size(); i++)
		{
			ArrayList<Integer> row = new ArrayList<Integer>();
			for(int j=0; j<this.matrix.get(i).size(); j++)
			{
				row.add(this.matrix.get(i).get(j));
			}
			newZone.matrix.add(row);
		}
		
		newZone.enabledTimers = new HashMap<Integer,Integer>(this.enabledTimers);
		newZone.getTimer = new HashMap<Integer, Integer>(this.getTimer);
		
		return newZone;
	}
	
	
	public boolean mustFire(int transition)
	{
		int lowerBound = matrix.get(enabledTimers.get(transition)).get(1);
		int upperBound = matrix.get(0).get(enabledTimers.get(transition));
		
		return -1 * lowerBound >= upperBound;
	}
	
	
	public class NegativeDiagonalException extends RuntimeException
	{
		/**
		 * The default serialVersionUID.
		 */
		private static final long serialVersionUID = 1L;

		public NegativeDiagonalException(String message)
		{
			super(message);	// Use the default constructor.
		}
	}
	
	
}
