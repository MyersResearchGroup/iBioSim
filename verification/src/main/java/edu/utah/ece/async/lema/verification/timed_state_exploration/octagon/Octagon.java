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
package edu.utah.ece.async.lema.verification.timed_state_exploration.octagon;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.utah.ece.async.lema.verification.lpn.ExprTree;
import edu.utah.ece.async.lema.verification.lpn.LPN;
import edu.utah.ece.async.lema.verification.lpn.Transition;
import edu.utah.ece.async.lema.verification.lpn.Variable;
import edu.utah.ece.async.lema.verification.platu.main.Options;
import edu.utah.ece.async.lema.verification.platu.platuLpn.DualHashMap;
import edu.utah.ece.async.lema.verification.platu.platuLpn.LpnTranList;
import edu.utah.ece.async.lema.verification.platu.stategraph.State;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.ContinuousRecordSet;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.ContinuousUtilities;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.Event;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.EventSet;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.InequalityVariable;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.IntervalPair;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.LPNContAndRate;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.LPNContinuousPair;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.LPNTransitionPair;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.UpdateContinuous;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.VariableRangePair;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.Zone;

/**
 * Octagons ('The Octagon Abstract Domain' by Mine) are the subsets of
 * Euclidean space formed by intersecting hyperplanes associated with
 * equations of the form vi-vj<=a, vi+vj<=a, and -vi-vj<=a. In two
 * dimensions, they correspond to convex polygons with up to eight
 * sides where each side is parallel to an axis, forms a positive
 * 45 degree angle with the axes, or forms a negative 45 degree angle
 * with the the axes.
 * 
 * Octagons, like zones, can be represented by a difference bound
 * matrix (DBM). For an octagon in n-dimensions given by the
 * variables v0, v1, ..., v(n-1), the DBM uses 2n-dimensions given by
 * v0+, v0-, v1+, v1-, ..., v(n-1)+, v(n-1)-. The variables are connected
 * by vi+ = +vi and vi- = -vi. Thus vi+ represents the positive
 * vi and vi- represents the negative vi. Given the matrix
 * 
 *            v0+       v0-         v1+         v1-    ...   v(n-1)+       v(n-1)-
 *    v0+     m00       m01         m02         m03    ...  m(0,2n-2)     m(0,2n-1)
 *    v0-     m10       m11         m12         m13    ...  m(1,2n-2)     m(1,2n-1)
 *    v1+     m20       m21         m22         m23    ...  m(2,2n-2)     m(2,2n-1)
 *    v1-     m30       m31         m32         m33    ...  m(3,2n-2)     m(3,2n-1)
 *    ...     ...       ...         ...         ...    ...     ....         ....
 * v(n-1)+ m(2n-2,0)  m(2n-2,1)  m(2n-2,2)  m(2n-2,3)  ... m(2n-2,2n-2)  m(2n-2,n-1)
 * v(n-1)- m(2n-1,0)  m(2n-1,1)  m(2n-1,2)  m(2n-1,3)  ... m(2n-1,2n-2)  m(2n-1,n-1)
 * 
 * the inequalities are encoded as vi - vj <= mji. So an entry gives
 * the upper bound for the column minus the row. So, for example,
 * v0- - v0+ <= m01. Removing the plus and minus variables, the inequality is
 * actually -v0-v0 <= m01 or -2v0 <= m01. So m01 is the negative of twice the
 * lower bound of v0.
 * 
 * For a more concrete example, let a two dimensional octagon be defined as the
 * intersection of the spaces defined inequalities  mx <= x <= Mx,
 * my <= y <= My, y-x <= b1, y-x <= -b2, y+x <= b3, and -y-x <= -b4
 * then the DBM is given by
 * 
 *     x+   x-  y+ y-
 * x+  0  -2mx  b1 -b4
 * x-  2Mx  0   b3 -b2
 * y+ -b2 -b4   0  -2my
 * y-  b3  b1   2My  0
 * 
 * The b1, b2, b3, and b4 are the y-intercepts of the boundary lines
 * for the cross inequalities, the inequalities involving both x and y.
 * 
 * 
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Octagon implements Equivalence {
	
	/*
	 * The list of LPN for the variables.
	 */
	private LPN[] _lpnList;
	
	/*
	 * The matrix for storing the DBM.
	 */
	private int[][] _matrix;
	
	/*
	 * Stores the timers and the non-rate-zero
	 * continuous variables. An LPNTransitionPair
	 * gives the integer representation of the
	 * variables. The position of the variables in
	 * this list gives their order in the DBM. The
	 * i-th variable vi has vi+ at 2i and vi- at
	 * 2i+1. The index of the variable in this list
	 * is referred to in this class as the base
	 * index.
	 */
	private LPNTransitionPair[] _dbmVarList;
	
	/*
	 * These two array stores the cached min,max values for timers and
	 * the range of rates for non-zero rate continuous variables. I could
	 * add an additional row and column to the DBM the same as in the
	 * case of the Zone, but it is a little tedious to have to keep
	 * adjusting for the difference this makes for the indecies.
	 * The _upperBounds stores twice the maximum and the _lowerBounds
	 * stores twice the negative of the minimum.
	 */
	private int[] _upperBounds;
	private int[] _lowerBounds;
	
	
	/* 
	 * Stores the continuous variables that have rate zero. A zero rate
	 * variable still has a potential range of rates and a range of
	 * values. The LPNConAnd Rate keeps the range of rates information
	 * and the VariableRangePair keeps the range of values. This extra
	 * information about the range of values is not needed for a
	 * variable with a non-zero rate since the DBM stores this
	 * information.
	 */
	DualHashMap<LPNContAndRate, VariableRangePair> _rateZeroContinuous;
	
	/* 
	 * Turns on and off subsets for the zones.
	 * True means subset will be considered.
	 * False means subsets will not be considered.
	 */
	private static boolean _subsetFlag = true;
	
	/* 
	 * Turns on and off supersets for zones.
	 * True means that supersets will be considered.
	 * False means that supersets will not be considered.
	 */
	private static boolean _supersetFlag = true;
	
	
	/* Write a log file */
	private static BufferedWriter _writeLogFile = null;
	
	
	/* The hash code. A -1 value is used to indicate the
	 * hashcode has not been created yet.
	 */
	private int _hashCode;
	
	/**
	 * Returns the write log.
	 * @return
	 * 		The _writeLogfile.
	 */
	public static BufferedWriter get_writeLogFile(){
		return _writeLogFile;
	}
	
	/**
	 * Sets the BufferedWriter.
	 * @param writeLogFile
	 */
	public static void set_writeLogFile(BufferedWriter writeLogFile){
			_writeLogFile = writeLogFile;
	}

	
	/**
	 * Sets the writeLogFile to null.
	 */
	public static void reset_writeLogFile(){
		_writeLogFile = null;
	}
	
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
	 * Zero argument constructor for use in methods that create Octagons where
	 * the members variables will be set by the method.
	 */
	protected Octagon()
	{
		//*_matrix = new int[0][0];
		//*_indexToTimerPair = new LPNTransitionPair[0];
		
		//*_hashCode = -1;
		_hashCode = -1;
		
		//*_lpnList = new LhpnFile[0];
		
		
		//*_rateZeroContinuous = new DualHashMap<LPNContAndRate, VariableRangePair>();
		_rateZeroContinuous = new DualHashMap<LPNContAndRate, VariableRangePair>();
		
	}
	
	/**
	 * Creates an octagon based on the local states.
	 * @param localStates
	 * 			The current state (or initial) of the LPNs.
	 */
	public Octagon (State[] localStates){
		
		// Open the log file.
		if(_writeLogFile == null && Options.get_TimingLogfile() != null){
			try{
				_writeLogFile = 
						new BufferedWriter(
								new FileWriter(Options.get_TimingLogfile()));
			} catch (IOException e) {

				e.printStackTrace();
			}finally{

			}
		}
		
		// Initialize hash code to -1 (indicating nothing cached).
		_hashCode = -1;
		
		// Initialize the LPN list.
		//* Zone.initialize_lpnList(localStates);
		initialize_lpnList(localStates);
		
		// Get the enabled transitions. This initializes the _indexTotimerPair
		// which stores the relevant information.
		// This method will also initialize the _rateZeroContinuous
		//* Zone.initialize_indexToTimerPair(localStates);
		/*
		 *  Get the enabled transitions. This initializes the _dbmVarList
		 *  which stores the relevant information.
		 *  This method will also initialize the _rateZeroContinuous
		 *  variable.
		 */
		initialize_dbmVarList(localStates);
		
		
		/*
		 *  Initialize the matrix.
		 *  Note: in contrast with the Zone, the _matrix in an Octagon
		 *  is only the DBM.
		 */
		//* Zone._matrix = new int[matrixSize()][matrixSize()];
		_matrix = new int[DBMsize()][DBMsize()];
//		_lowerBounds = new int[DBMsize()];
//		_upperBounds = new int[DBMsize()];
		_lowerBounds = new int[_dbmVarList.length];
		_upperBounds = new int[_dbmVarList.length];
		
//		this.toString();
		// Set the lower bound/ upper bounds of the timers and the rates.
		//* Zone.initializeLowerUpperBounds(getAllNames(), localStates);
		initializeLowerUpperBounds(getAllNames(), localStates);
		
		// Initialize the row and column entries for the continuous variables.
		//* zone.initializeRowColumnContVar();
		initializeRowColumnContVar();
		
		
		// Create  a previous zone to the initial zone for the sake of warping.
		//* Zone.Zone tmpZone = beforeInitialZone();
		Octagon tmpOct = beforeInitialOctagon();
		
		//* Zone.dbmWarp(tmpZone);
		dbmWarp(tmpOct);
		
		//* Zone.recononicalize();
		recononicalize();
		
		// Advance Time
		//* Zone.advance(localStates);
		advance(localStates);
		
		// Re-canonicalize
		//* Zone.recononicalize();
		recononicalize();
		
		// Get the amount of time that has
		// advanced. If there is a variable in the octagon.
		if(this._dbmVarList.length != 0){
			
			// Get the max value of the first
			// entry for calculating how
			// much time has advanced.
			int initial = getUpperBound(0);
			
			
			int time = (int)Math.ceil((this.getUpperBound(0) - initial)/2);
				
			adjustB3(this, time);
		}
		
		// Check the size of the DBM.
		//* Zone.checkZoneMaxSize();
	}
	
	/**
	 * Creates an Octagon based on the local states.
	 * @param localStates
	 * 			The current state (or initial) of the LPNs.
	 * @param init
	 * 			Should be true if this is and initial octagon.
	 */
	public Octagon(State[] localStates, boolean init) {
		// Extract the local states.
		//State[] localStates = tps.toStateArray();
				
		// Initialize hash code to -1 (indicating nothing cached).
		_hashCode = -1;
				
		// Initialize the LPN list.
		initialize_lpnList(localStates);
				
		// Get the enabled transitions. This initializes the _indexTotimerPair
		// which stores the relevant information.
		// This method will also initialize the _rateZeroContinuous
		//*initialize_indexToTimerPair(localStates);
		initialize_dbmVarList(localStates);
				
		// Initialize the matrix.
		//*_matrix = new int[matrixSize()][matrixSize()];
		_matrix = new int[DBMsize()][DBMsize()];
		_lowerBounds = new int[_dbmVarList.length];
		_upperBounds = new int[_dbmVarList.length];
		
		// Set the lower bound/ upper bounds of the timers and the rates.
		initializeLowerUpperBounds(getAllNames(), localStates);
			
		// Initialize the row and column entries for the continuous variables.
		initializeRowColumnContVar();
				
		if(init){
			return;
		}
				
		// Advance Time
		//advance();
		advance(localStates);
				
		// Re-canonicalize
		recononicalize();
		
		// Check the size of the DBM.
		//*checkZoneMaxSize();
	}

	/**
	 * This method creates an Octagon identical to the current Octagon
	 * except all the current rates are turned to 1. This is to provide
	 * a previous Octagon to the initial Octagon for warping.
	 * @return
	 * 		A Octagon identical to this Octagon with all rates set to 1.
	 */
	private Octagon beforeInitialOctagon(){
		
		//*Zone z = this.clone();
		Octagon oct = this.clone();
		
		//*for(int i=1; i<z._indexToTimerPair.length; i++){
		for (int i=0; i<oct._dbmVarList.length; i++){
			//*if(!(z._indexToTimerPair[i] instanceof LPNContinuousPair)){
			if (!(oct._dbmVarList[i] instanceof LPNContinuousPair)){
				//*break;
				// Setting the rates to one has nothing to do with the timers,
				// only the continuous variables, so break out. A break is
				// permissible since all the continuous variables are order
				// before the timer, so once a timer is seen, there are no
				// more continuous variables to consider.
				break;
			//*}
			}
			
			//*LPNContinuousPair lcPair = (LPNContinuousPair) z._indexToTimerPair[i];
			LPNContinuousPair lcPair = (LPNContinuousPair) oct._dbmVarList[i];
			
			//*lcPair.setCurrentRate(1);
			lcPair.setCurrentRate(1);
			
		//*}
		}
		
		//*return z;
		return oct;
	}
	
	/**
	 * Initializes the _lpnList using information from the local states.
	 * This method is identical to the Zone version.
	 * @param localStates
	 * 		The local states.
	 * @return
	 * 		The enabled transitions.
	 */
	private void initialize_lpnList(State[] localStates){
		
		// Create the LPN list.
		_lpnList = new LPN[localStates.length];
		
		// Get the LPNs.
		for(int i=0; i<localStates.length; i++){
			_lpnList[i] = localStates[i].getLpn();
		}
	}
	
	/**
	 * Initializes the _dbmVarList from the local states. This includes
	 * adding the zero timer, the continuous variables and the set of 
	 * enabled timers. It also initializes the _dbmVarList.
	 * @param localStates
	 * 		The local states.
	 * @return
	 * 		The names of the transitions stored in the _dbmVarList (in the same order).
	 */
	private void initialize_dbmVarList(State[] localStates){
	//private void initialize_indexToTimerPair(State[] localStates){
		
		/*
		 * The populating of the _dbmVarList is done in two stages.
		 * The first is to add the zero timer which is at the beginning of the zone.
		 * The second is to add the continuous variables. And the third is to add
		 * the other timers. Since the continuous variables are added before the
		 * timers and the variables and timers are added in the order of the LPNs,
		 * the elements in an accumulating list (enabledTransitionsArrayList) are
		 * already in order up to the elements added for a particular LPN. Thus the
		 * only sorting that needs to take place is the sorting for a particular LPN.
		 * Correspondingly, elements are first found for an LPN and sort, then added
		 * to the main list.
		 */
		
		/*
		 * This method is similar to the version for Zones. With Zones, there are three
		 * stages. The first stage for a Zone is to add the zero timer. Octagons do
		 * not need a zero timer, so the Octagon version proceeds with the other
		 * two stages. The first stage is to add the continuous variables and the
		 * second is to add the timers.
		 */
		
		
		// This method will also initialize the _rateZeroContinuous
		//*_rateZeroContinuous = 
		//*		new DualHashMap<LPNContAndRate, VariableRangePair>();
		
		// Initialize the _rateZeroContinuous variables.
		_rateZeroContinuous =
				new DualHashMap<LPNContAndRate, VariableRangePair>();
		
		// This list accumulates the transition pairs (ie timers) and the continuous
		// variables.
		//*ArrayList<LPNTransitionPair> enabledTransitionsArrayList =
		//*		new ArrayList<LPNTransitionPair>();
		
		// This list accumulates the transition pairs, that is the timers, and the
		// continuous variables
		ArrayList<LPNTransitionPair> enabledTransitionsArrayList =
				new ArrayList<LPNTransitionPair>();
				
		// Put in the zero timer.
		//*enabledTransitionsArrayList
		//*.add(new LPNTransitionPair(LPNTransitionPair.ZERO_TIMER,
		//*		LPNTransitionPair.ZERO_TIMER));
		
		
		// We do not need to put in the zero timer for Octagons.
		
		// Get the continuous variables.
		//*for(int i=0; i<localStates.length; i++){
		for ( int i=0; i<localStates.length; i++ ) {
			
			
			// Accumulates the changing continuous variables for a single LPN.
			//*ArrayList<LPNTransitionPair> singleLPN = 
			//*					new ArrayList<LPNTransitionPair>();
		
			// Accumulates the changing continuous variables for a single LPN.
			ArrayList<LPNTransitionPair> singleLPN =
					new ArrayList<LPNTransitionPair>();
			
			// Get the associated LPN.
			//*LhpnFile lpn = localStates[i].getLpn();
			
			// Get the associated LPN.
			LPN lpn = localStates[i].getLpn();
			
			// Get the continuous variables for this LPN.
			//*String[] continuousVariables = lpn.getContVars();
			
			// Get the continuous variables for this LPN.
			String[] continuousVariables = lpn.getContVars();
			
			// Get the variable, index map.
			//*DualHashMap<String, Integer> variableIndex = lpn.getContinuousIndexMap();
			
			// Get the variables, index map.
			DualHashMap<String, Integer> variableIndex = lpn.getContinuousIndexMap();
			
			
			// Find which have a nonzero rate.
			//*for(int j=0; j<continuousVariables.length; j++){
			
			// Find which have a nonzero rate.
			for ( int j=0; j<continuousVariables.length; j++) {
				
				// Get the Variables with this name.
				//*Variable contVar = lpn.getVariable(continuousVariables[j]);
				
				// Get the Variables with this name.
				Variable contVar = lpn.getVariable(continuousVariables[j]);
				
				// Get the rate.
				//*IntervalPair rate = parseRate(contVar.getInitRate());
				
				// Get the rate.
				IntervalPair rate = Zone.parseRate(contVar.getInitRate());
				
				// Get the LPN index for the variable
				//* int lpnIndex = lpn.getLpnIndex();
				
				// Get the LPN index for the variable
				int lpnIndex = lpn.getLpnIndex();
				
				// Get the index as a variable for the LPN.
				//* int contVariableIndex = variableIndex.get(continuousVariables[j]);

				// Get the index as a variable for the LPN.
				int contVariableIndex = variableIndex.get(continuousVariables[j]);
				
				//*LPNContinuousPair newPair = 
				//*		new LPNContinuousPair(lpnIndex, contVariableIndex,
				//*				rate.getSmallestRate());
				
				LPNContinuousPair newPair =
						new LPNContinuousPair(lpnIndex, contVariableIndex,
								rate.getSmallestRate());
				
				// If the rate is non-zero, then the variables needs to be tracked
				// by matrix part of the Zone.
				//* if(newPair.getCurrentRate() != 0){	
					// Temporary exception guaranteeing only unit rates.
			
				// If the rate is non-zero, then the variable needs to be tracked
				// by matrix part of the Zone.
				if (newPair.getCurrentRate() != 0) {	
					//*singleLPN.add(newPair);
					
					singleLPN.add(newPair);
				//*}
				}
				else{
					
				//*else{
					// If the rate is zero, then the Zone keeps track of this variable
					// in a list.				
					//*_rateZeroContinuous.
					//*	insert(new LPNContAndRate(newPair, rate),
					//*		new VariableRangePair(contVar,
					//*				parseRate(contVar.getInitValue())));
					
					// If the rate is zero, then the Zone keeps track of this variable
					// in a list.
					_rateZeroContinuous.
						insert(new LPNContAndRate(newPair, rate),
								new VariableRangePair(contVar,
										Zone.parseRate(contVar.getInitValue())));
						
					
				//*}
				}
			//*}
			}
		
			
			// Sort the list.
			//*Collections.sort(singleLPN);
			
			// Sort the list.
			Collections.sort(singleLPN);
			
			// Add the list to the total accumulating list.
			//* for(int j=0; j<singleLPN.size(); j++){
			//*	 enabledTransitionsArrayList.add(singleLPN.get(j));
			//*}
			
			// Add the list to the total accumulating list.
			for (int j=0; j<singleLPN.size(); j++){
				enabledTransitionsArrayList.add(singleLPN.get(j));
			}
		//*}
		}
			
			
		// Get the transitions.
		//* for(int i=0; i<localStates.length; i++){
			
		// Get the transitions.
		for (int i=0; i<localStates.length; i++) {
		
		
			// Extract the enabled transition vector.
			//* boolean[] enabledTran = localStates[i].getTranVector();
		
			// Extract the enabled transition vector.
			boolean[] enabledTran = localStates[i].getTranVector();
			
			// Accumulates the transition pairs for one LPN.
			//* ArrayList<LPNTransitionPair> singleLPN = new ArrayList<LPNTransitionPair>();
			
			// Accumulates the transition pairs for one LPN.
			ArrayList<LPNTransitionPair> singleLPN = new ArrayList<LPNTransitionPair>();
			
			// The index of the boolean value corresponds to the index of the Transition.
			//* for(int j=0; j<enabledTran.length; j++){
				//* if(enabledTran[j]){
					// Add the transition pair.
					//* singleLPN.add(new LPNTransitionPair(i, j));
				//* }
			//* }
			
			// The index of the boolean value corresponds ot the index of the Transition.
			for (int j=0; j<enabledTran.length; j++){
				if(enabledTran[j]){
					// Add the transition pair.
					singleLPN.add(new LPNTransitionPair(i, j));
				}
			}
			
			// Sort the transitions for the current LPN.
			//* Collections.sort(singleLPN);
			
			// Sort the transitions for the current LPN.
			Collections.sort(singleLPN);
			
			// Add the collection to the enabledTransitionsArrayList
			//* for(int j=0; j<singleLPN.size(); j++){
				//* enabledTransitionsArrayList.add(singleLPN.get(j));
			//* }
			
			// Add the collection t the enabledTransitionsArrayList
			for (int j=0; j<singleLPN.size(); j++){
				enabledTransitionsArrayList.add(singleLPN.get(j));
			}
			
		//* }
		}
		
		// Extract out the array portion of the enabledTransitionsArrayList.
		//* _indexToTimerPair = enabledTransitionsArrayList.toArray(new LPNTransitionPair[0]);
		
		// Extract out the array portion of the enabledTransitionsArrayList.
		_dbmVarList = enabledTransitionsArrayList.toArray(new LPNTransitionPair[0]);
	}
	
	/**
	 * Sets the lower and upper bounds for the transitions and continuous variables.
	 * @param varNames
	 * 			The names of the transitions in _dbmVarList.
	 */
	private void initializeLowerUpperBounds(String[] varNames, State[] localStates){
		
		// Traverse the entire length of the DBM matrix.
		// This is the same length as the _indexToTimerPair.length-1. The DBM is used to
		// match the idea of setting the value for each row.
		//*for(int i=1; i<dbmSize(); i++){
			// Get the current LPN and transition pairing.
		//*	LPNTransitionPair ltPair = _indexToTimerPair[i];
		/*
		 * Traverse the length of the _dbmVarList.
		 */
		for (int i=0; i<_dbmVarList.length; i++){
		
			// Get the current LPN and transition pairing.
			LPNTransitionPair ltPair = _dbmVarList[i];
			
			//*	IntervalPair range;
			IntervalPair range;

			//*if(ltPair instanceof LPNContinuousPair){
			if(ltPair instanceof LPNContinuousPair){
			
				//---I don't this is a correct comment.
				// If the pairing represents a continuous variable, then the 
				// upper and lower bound are the initial value or infinity depending
				// on whether the initial rate is positive or negative.
				
				// If the value is a constant, then assign the upper and lower bounds
				// to be constant. If the value is a range then assign the upper and
				// lower bounds to be a range.
				//*Variable v = _lpnList[ltPair.get_lpnIndex()]
				//*		.getContVar(ltPair.get_transitionIndex());
				Variable v = _lpnList[ltPair.get_lpnIndex()]
						.getContVar(ltPair.get_transitionIndex());
				
				
				//*String rate = v.getInitRate();
				String rate = v.getInitRate();
				
				
				/*
				 *  Parse the rate. Should be in the form of [x,y] where x and
				 *  y are integers.
				 */
				//*range = parseRate(rate);
				range = Zone.parseRate(rate);
			
				/*
				 *  Set the upper and lower bound in the _lowerBounds,_upperBounds
				 *  member variables for the continuous variables.
				 */
				//*String contValue = v.getInitValue();
				//*IntervalPair bound = parseRate(contValue);
				String contValue = v.getInitValue();
				IntervalPair bound = Zone.parseRate(contValue);
				
				
				// Set upper bound (DBM entry (0, x) where x is the index of the variable v).
				//*setDbmEntryByPair(LPNTransitionPair.ZERO_TIMER_PAIR, ltPair, bound.get_UpperBound());
				// Set lower bound (DBM entry (x, 0) where x is the index of the variable v).
				//*setDbmEntryByPair(ltPair, LPNTransitionPair.ZERO_TIMER_PAIR, -1*bound.get_LowerBound());
				
				/*
				 * Set the lower and upper bounds for the continuous variable.
				 */
//				setMin(i, ContinuousUtilities.chkDiv(bound.get_LowerBound(), 2, false));
//				setMax(i, ContinuousUtilities.chkDiv(bound.get_UpperBound(), 2, true));
				
				if(range.get_LowerBound() >= 0){
					setMin(i, ContinuousUtilities.chkDiv(bound.get_LowerBound(), range.get_LowerBound(), false));
					setMax(i, ContinuousUtilities.chkDiv(bound.get_UpperBound(), range.get_LowerBound(), true));
				}
				else{
					setMax(i, ContinuousUtilities.chkDiv(bound.get_LowerBound(), range.get_LowerBound(), true));
					setMin(i, ContinuousUtilities.chkDiv(bound.get_UpperBound(), range.get_LowerBound(), false));
				}
			}
			//*}
			//*else{
			else{
			
				// Get the expression tree.
				//*ExprTree delay = _lpnList[ltPair.get_lpnIndex()].getDelayTree(varNames[i]);
				ExprTree delay = _lpnList[ltPair.get_lpnIndex()].getDelayTree(varNames[i]);
				
				
				// Get the values of the variables for evaluating the ExprTree.
				//*HashMap<String, String> varValues = 
				//*		_lpnList[ltPair.get_lpnIndex()]
				//*				.getAllVarsWithValuesAsString(localStates[ltPair.get_lpnIndex()].getVariableVector());
				HashMap<String, String> varValues =
						_lpnList[ltPair.get_lpnIndex()]
								.getAllVarsWithValuesAsString(
										localStates[ltPair.get_lpnIndex()].getVariableVector());
				
				
				// Set the upper and lower bound.
				// Passing the Octagon as null since it should not be needed.
				//*range = delay.evaluateExprBound(varValues, this, null);
				range = delay.evaluateExprBound(varValues, this, null);
				
			}
			//*}
			
			//*setLowerBoundbydbmIndex(i, range.get_LowerBound());
			//*setUpperBoundbydbmIndex(i, range.get_UpperBound());
			
			/*
			 * Set the lower and upper bounds on the timer delays and
			 * range of rates.
			 */
			_lowerBounds[i] = -1*range.get_LowerBound();
			_upperBounds[i] = range.get_UpperBound();
			
		//*}
		}
	}
	
	/**
	 * Gives the names of all the transitions and continuous variables that 
	 * are represented by the zone.
	 * @return
	 * 		The names of the transitions and continuous variables that are 
	 * 		represented by the zone.
	 */
	public String[] getAllNames(){

		/*
		 * This code can be made more efficient
		 * by combining the getContVarNames and
		 * getTranNames.
		 */ 
		
		
		// Get the continuous variable names.
		//*String[] contVar = getContVarNames();
		String[] contVar = getContVarNames();
		
		
		// Get the transition names.
		//*String[] trans = getTranNames();
		String[] trans = getTranNames();
		
		// Create an array large enough for all the names.
		//*String[] names = new String[contVar.length + trans.length + 1];
		String[] names = new String[contVar.length + trans.length];
		
		// Add the zero timer.
		//*names[0] = "The zero timer.";
		
		// Add the continuous variables.
		//*for(int i=0; i<contVar.length; i++){
		//*	names[i+1] = contVar[i];
		//*}
		for (int i=0; i<contVar.length; i++){
			names[i] = contVar[i];
		}
		
		
		
		// Add the timers.
		//*for(int i=0; i<trans.length; i++){
			// Already the zero timer has been added and the elements of contVar.
			// That's a total of 'contVar.length + 1' elements. The last index was
			// thus 'contVar.length' So the first index to add to is 
			// 'contVar.length +1'.
			//*names[1+contVar.length + i] = trans[i];
		//*}
		for (int i=0; i<trans.length; i++){
			// Already the elements of contVar have been added.
			// That's a total of 'contVar.length' elements. The last index was
			// thus 'contVar.length-1' So the first index to add to is 
			// 'contVar.length'.
			names[contVar.length + i] = trans[i];
		}
		
		
		//*return names;
		return names;
	}
	
	/**
	 * Get the names of the continuous variables that this zone uses.
	 * @return
	 * 		The names of the continuous variables that are part of this zone.
	 */
	public String[] getContVarNames(){
		
		// List for accumulating the names.
		//*ArrayList<String> contNames = new ArrayList<String>();
		ArrayList<String> contNames = new ArrayList<String>();
		
		
		// Find the pairs that represent the continuous variables. Loop starts at
		// i=1 since the i=0 is the zero timer.
		//*for(int i=1; i<_indexToTimerPair.length; i++){
		for (int i=0; i<_dbmVarList.length; i++){
			
			//*LPNTransitionPair ltPair = _indexToTimerPair[i];
			LPNTransitionPair ltPair = _dbmVarList[i];
			
			// If the isTimer value is false, then this pair represents a continuous
			// variable.
			// If pair is LPNContinuousPair.
			//*if(ltPair instanceof LPNContinuousPair){
			if(ltPair instanceof LPNContinuousPair){
				// Get the LPN that this pairing references and find the name of
				// the continuous variable whose index is given by this pairing.
				//*contNames.add(_lpnList[ltPair.get_lpnIndex()]
				//*		.getContVarName(ltPair.get_transitionIndex()));
				contNames.add(_lpnList[ltPair.get_lpnIndex()]
				        .getContVarName(ltPair.get_transitionIndex()));
				
			}
			//*}
		//*}
		}
		
		//*return contNames.toArray(new String[0]);
		return contNames.toArray(new String[0]);
	}
	
	/**
	 * Gets the names of the transitions that are associated with the timers in the
	 * Octagon.
	 * @return
	 * 		The names of the transitions whose timers are in the Octagon.
	 */
	public String[] getTranNames(){
		
		// List for accumulating the names.
		//*ArrayList<String> transitionNames = new ArrayList<String>();
		ArrayList<String> transitionNames = new ArrayList<String>();
		
		// Find the pairs that represent the transition timers.
		//*for(int i=1; i<_indexToTimerPair.length; i++){
		for (int i=0; i<_dbmVarList.length; i++){
			
			//*LPNTransitionPair ltPair = _indexToTimerPair[i];
			LPNTransitionPair ltPair = _dbmVarList[i];
			
			// If this is an LPNTransitionPair and not an LPNContinuousPair
			//*if(!(ltPair instanceof LPNContinuousPair)){
			if(!(ltPair instanceof LPNContinuousPair)){
				
				// Get the LPN that this pairing references and find the name of the
				// transition whose index is given by this pairing.
				//*transitionNames.add(_lpnList[ltPair.get_lpnIndex()]
						//*.getTransition(ltPair.get_transitionIndex()).getLabel());
				transitionNames.add(_lpnList[ltPair.get_lpnIndex()]
						.getTransition(ltPair.get_transitionIndex()).getLabel());
				
			//*}
			}
		//*}
		}
		
		//*return transitionNames.toArray(new String[0]);
		return transitionNames.toArray(new String[0]);
	}
	
	/**
	 * Initialize the rows and columns for the continuous variables.
	 */
	private void initializeRowColumnContVar(){
		
		
		// The only entries that do not need to be checked are the ones where both variables 
		// represent timers.
		
		//*for(int row=2; row<_indexToTimerPair.length; row++){
		for (int rowBase=1; rowBase<_dbmVarList.length; rowBase++){
			// Note: rowBase is indexing the pos and neg variables
			// for the row.
			
			//*LPNTransitionPair ltRowPair = _indexToTimerPair[row];
			LPNTransitionPair ltRowPair = _dbmVarList[rowBase];
			
			//*for(int col=1; col<row; col++){
			for (int colBase=0; colBase<rowBase; colBase++){
				// Note: colBase is indexing the pos and neg variables
				// for the column.
				
				//*LPNTransitionPair ltColPair = _indexToTimerPair[col];
				LPNTransitionPair ltColPair = _dbmVarList[colBase];
				
				
				// If we've reached the part of the zone involving only timers, then break out
				// of this row.
				//*if(!(ltRowPair instanceof LPNContinuousPair) && 
						//*!(ltColPair instanceof LPNContinuousPair)){
					//*break;
				//*}
				if(!(ltRowPair instanceof LPNContinuousPair) &&
						!(ltColPair instanceof LPNContinuousPair)){
					break;
				}
				
				
				// Zone: The new (row, col) entry. The entry is given by col-row<= m_(row,col). Since 
				// col <= m_(0,col) (its upper bound) and -row <= m_(row,0) (the negative of its lower
				// bound), the entry is given by col-row <= m(0,col) + m_(row,0) = m_(row,col);
				//*int rowCol = getDbmEntry(row,0) + getDbmEntry(0, col);
				
				/* 
				 * Octagon: The rowBase and colBase give two variables each: a positive
				 * and a negative variable. Thus the rowBase and colBase define four entries. When
				 * rowBase and colBase are equal, the entries define the max and min and two zeros.
				 * Every entry defined by colBase > rowBase is equal to an entry defined by
				 * colBase > rowBase. The condition is that m_(i,j) = m_(ibar,jbar).
				 */
				
				// The new (col, row) entry.
				//*int colRow = getDbmEntry(col, 0) + getDbmEntry(0, row);
				
//				this.toString();
				
//				int colP_rowP = twiceMax(colBase) - twiceMin(rowBase);
//				int colP_rowN = twiceMax(colBase) + twiceMax(rowBase);
//				int colN_rowP = -1*twiceMin(colBase) - twiceMin(rowBase);
//				int colN_rowN = -1*twiceMin(colBase) + twiceMax(rowBase);
				
				int colM_rowm = ContinuousUtilities.chkDiv(
						twiceMax(colBase) - twiceMin(rowBase), 2, true);
				int colM_rowM = ContinuousUtilities.chkDiv(
						twiceMax(colBase) + twiceMax(rowBase), 2, true);
				int colm_rowm = ContinuousUtilities.chkDiv(
						-1*twiceMin(colBase) - twiceMin(rowBase), 2, true);
				int colm_rowM = ContinuousUtilities.chkDiv(
						-1*twiceMin(colBase) + twiceMax(rowBase), 2, true);
				
				//*setDbmEntry(row, col, rowCol);
				//*setDbmEntry(col, row, colRow);
				
				_matrix[baseToPos(rowBase)][baseToPos(colBase)] = colM_rowm;
				_matrix[baseToNeg(colBase)][baseToNeg(rowBase)] = colM_rowm;
				
				_matrix[baseToNeg(rowBase)][baseToPos(colBase)] = colM_rowM;
				_matrix[baseToNeg(colBase)][baseToPos(rowBase)] = colM_rowM;
				
				_matrix[baseToPos(rowBase)][baseToNeg(colBase)] = colm_rowm;
				_matrix[baseToPos(colBase)][baseToNeg(rowBase)] = colm_rowm;
				
				_matrix[baseToNeg(rowBase)][baseToNeg(colBase)] = colm_rowM;
				_matrix[baseToPos(colBase)][baseToPos(rowBase)] = colm_rowM;
				
			}
			//*}
		//*}
		}
	}
	
	
	/**
	 * Give the upper and lower bounds for a continuous variable.
	 * Value comes back not warped.
	 * @param contVar
	 * 		The variable of interest.
	 * @return
	 * 		The upper and lower bounds according to the octagon. Note: the
	 * value may be an over-approximation since an octagon stores twice
	 * the max and min, so one needs to adjust the division by 2 to an integer.
	 */
	@Override
	public IntervalPair getContinuousBounds(String contVar, LPN lpn){
		
		// Extract the necessary indecies.
		int lpnIndex = lpn.getLpnIndex();
				
		// Get the index of the continuous variable.
		DualHashMap<String, Integer> variableIndecies = lpn.getContinuousIndexMap();
		int contIndex = variableIndecies.get(contVar);
				
		// Package the indecies with false indicating not a timer.
		// Note: setting the current rate is not necessary here since the
		// LPNContinuousPair is only being used as an index.
		LPNContinuousPair index = new LPNContinuousPair(lpnIndex, contIndex);
				
		// Search for the continuous variable in the rate zero variables.
		VariableRangePair pairing = _rateZeroContinuous
				.get(new LPNContAndRate(index, new IntervalPair(0,0)));
				
		// If Pairing is not null, the variable was found and return the result.
		if(pairing != null){
			return pairing.get_range();
		}
				
		// If Pairing was null, the variable was not found. Search for the variable
		// in the zone portion.
		int base = Arrays.binarySearch(_dbmVarList, index);
				
		// If base < 0, the search was unsuccessful, so scream.
		if(base < 0){
			throw new IllegalArgumentException("Atempted to find the bounds for "
					+ "a non-rate zero continuous variable that was not found in the "
					+ "zone.");
		}
				
		// Else find the upper and lower bounds.
		//* int lower = (-1)*getDbmEntry(i, 0);
		//* int upper = getDbmEntry(0, i);
//		double preupper = (double) twiceMax(base)*getCurrentRate(base);
//		double prelower = (double) twiceMin(base)*getCurrentRate(base);
		
		double preupper = (double) twiceMax(base);
		double prelower = (double) twiceMin(base);
//		
		int upper = (int) Math.ceil(preupper/2);
		int lower = (int) Math.floor(prelower/2);
		
		return new IntervalPair(lower, upper);		
	}
	
	/**
	 * Gets twice the max of a variable.
	 * @param base The index of the variable pair in
	 * the Octagon.
	 * @return The maximum for the variable.
	 */
	private int twiceMax(int base){
		
		// The maximum is given by v+ - v- <= value. So this
		// is the column for the associated positive
		// variable and the row of the associated
		// negative variable.
		
		return _matrix[baseToNeg(base)][baseToPos(base)];
	}
	
	/**
	 * Sets the DBM entry for the maximum value.
	 * @param base The index for the variable.
	 * @param vlaue The value for the maximum.
	 */
	private void setMax(int base, int value){
		/*
		 * Twice the maximum value for an
		 * Octagon is put in the column v+ and
		 * row v-, since this entry is
		 * v+ - v- <= 2*max.
		 */
		_matrix[baseToNeg(base)][baseToPos(base)] = 2*value;
	}
	
	/**
	 * Gets the DBM entry for the minimum value.
	 * @param base The base index for the minimum.
	 * @return Twice the minimum value.
	 */
	private int twiceMin(int base){
		// The minimum is given by (-1)*(v- - v+) >= (-1)*value.
		// This is the negative variable column and the positive
		// variable row.
		return -1*_matrix[baseToPos(base)][baseToNeg(base)];
	}
	
	/**
	 * Sets the DBM entry for the minimum value.
	 * @param base The index for the variable.
	 * @param value The value for the minimum.
	 */
	private void setMin(int base, int value){
		/*
		 * Twice the minimum value for an
		 * Octagon is put in the column v- and
		 * the row v+, since this entry is
		 * v- - v+ <= -2*min.
		 */
		_matrix[baseToPos(base)][baseToNeg(base)] = -2*value;
	}
	
	/**
	 * Gets the range for the continuous variable. Values come back not
	 * warped.
	 * @param ltContPair
	 * 		The index of the variable of interest.
	 * @return
	 * 		The range of the continuous variable described by ltContPair.
	 */
	@Override
	public IntervalPair getContinuousBounds(LPNContinuousPair ltContPair){
		
		// First check if the variable is in the DBM portion of the
		// Octagon.
		int variableIndex = Arrays.binarySearch(_dbmVarList, ltContPair);
				
		if(variableIndex < 0){
			// The variable was not found in the zone. Check to see if its
			// in the rate-zero variables. Technically I will return whatever
			// is in the _rateZeroConintuous, null or not.
			// Of course, it will be non-null unless something has gone
			// wrong since every continuous variable either has a zero
			// or non-zero rate so should be either in the 
					
			// First get an object to reference into the _rateZeroContinuous
			LPNContAndRate lcr = new LPNContAndRate(ltContPair,
				new IntervalPair(0,0));
			return _rateZeroContinuous.get(lcr).get_range();
		}
				
		// The variable was found in the Octagon. Yay.
		int lower = (int) Math.floor(twiceMin(variableIndex)
			*getCurrentRate(variableIndex)/2);
		int upper = (int) Math.ceil(twiceMax(variableIndex)
			*getCurrentRate(variableIndex)/2);
		
		if(getCurrentRate(ltContPair)<0){
			int tmp = lower;
			lower = upper;
			upper = tmp;
		}
						
		return new IntervalPair(lower, upper);
	}
	
	/**
	 * Gets the current rate for the baseIndex-th variable. Note: this is
	 * getting the current rate of a non-zero rate continuous variable.
	 * 
	 * @param baseIndex The index of the variable to get the current rate for.
	 * 
	 * @return The rate of the variable that is baseIndex in the list. It is
	 * assumed that index baseIndex is a continuous variable in the Octagon.
	 * If it is not, a cast exception is thrown.
	 */
	public int getCurrentRate(int baseIndex){
		
		// Cast the variable as a continuous variable.
		LPNTransitionPair ltPair = _dbmVarList[baseIndex];
		LPNContinuousPair lcPair = (LPNContinuousPair) ltPair;
		
		return lcPair.getCurrentRate();
		
	}
	/**
	 * Gets the range of the rate associated with a continuous variable.
	 * @param ltContPair
	 * 		The index of the continuous variable.
	 * @return
	 * 		The range of rates associated with the continuous variable indexed
	 * 		by ltContPair.
	 */
	@Override
	public IntervalPair getRateBounds(LPNTransitionPair ltPair){

		int upper;
		int lower;
		
		// Check if the ltContpair is in the zone.
		int i = Arrays.binarySearch(_dbmVarList, ltPair);

		if(i < 0){
			// Then the variable is in the rate zero continuous
			// variables so get the range of rates from there.
			
			// Create an object to reference into the rate zero.
			LPNContAndRate lcr = 
					new LPNContAndRate((LPNContinuousPair) ltPair,
							new IntervalPair(0,0));
			
			// Get the old version of lcr from the rate zero since
			// that contains the rate. This is quite a hack.
			VariableRangePair vrp = _rateZeroContinuous.get(lcr);
			lcr = _rateZeroContinuous.getKey(vrp);
			
			return lcr.get_rateInterval();
		}
		
		
		upper = getUpperBound(i);
		lower = -1*getLowerBound(i);

		
		// The continuous variable is in the Octagon.
		// The upper and lower bounds are stored in the same
		// place as the delays, so the same method of 
		// retrieval will work.
		return new IntervalPair(lower, upper);
	}
	
	/**
	 * Get the value of the lower bound for the delay if the index refers
	 * to a timer, otherwise get the lower bound for the continuous variables
	 * rate.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @return
	 * 			The value of the lower bound.
	 */		
	public int getLowerBound(int index){
		return _lowerBounds[index];
	}
	
	/**
	 * Always the (negative) lower bound in warped space.
	 */
	@Override
	public int getLowerBoundTrue(int index){
		// The matrix stores twice the lower bound.
		return (int)(ContinuousUtilities.chkDiv(
				_matrix[baseToPos(index)][baseToNeg(index)],
				2,
				true));
	}
	
	/**
	 * Get the value of the upper bound for the delay. If the index refers
	 * to a timer, otherwise get the upper bound for the continuous 
	 * variables rate.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @return
	 * 			The upper bound on the transitions delay.
	 */
	public int getUpperBound(int index){
		return _upperBounds[index];
	}
	
	/**
	 * Always gives the upper bound in warped space.
	 */
	@Override
	public int getUpperBoundTrue(int index){
		// The matrix stores twice the upper bound.
		return (int)(ContinuousUtilities.chkDiv(
				_matrix[baseToNeg(index)][baseToPos(index)],
				2,
				true));
	}
	
	/**
	 * Get the upper bound unwarped.
	 */
	public int getUnwarpedUpperBound(LPNContinuousPair lcpair){
		
		// Get the index.
		int index = Arrays.binarySearch(_dbmVarList, lcpair);
		
		int rate = ((LPNContinuousPair) _dbmVarList[index])
				.getCurrentRate();
		
		int warpValue = getUpperBoundTrue(index);
		
		return warpValue * rate;
	}
	
	/**
	 * Gets the rate reset value.
	 * @param ltPair The index for the continuous variable.
	 * @return The value to reset to.
	 */
	@Override
	public int rateResetValue(LPNTransitionPair ltPair){
		
		//*IntervalPair rateBounds = getRateBounds(ltPair);
		IntervalPair rateBounds = getRateBounds(ltPair);
		
		
		//*int upper = rateBounds.get_UpperBound();
		//*int lower = rateBounds.get_LowerBound();
		int lower = rateBounds.get_LowerBound();
		int upper = rateBounds.get_UpperBound();
		
		
		/*
		 * Suppose the range of rates is [a,b]. If a>=0, we set the rate to b.
		 * If b<=0, we set the rate to a. Otherwise, a<0<b and we set the rate
		 * to b. Thus we set the rate to b, unless b<=0.
		 */
		
		//*return upper <= 0 ? lower : upper;
		
		return upper<=0 ? lower : upper;
	}
	
	/**
	 * Retrieves an entry of the DBM using LPNTransitionPair indecies.
	 * @param iPair
	 * 		The LPNTransitionPair for the ith entry.
	 * @param jPair
	 * 		The LPNTransitionPair for the jth entry.
	 * @return
	 * 		The value of the (i,j) element of the DBM where i corresponds to the row
	 * 		for the variable iPair and j corresponds to the row for the variable jPair.
	 */
	@Override
	public int getDbmEntryByPair(LPNTransitionPair iPair, LPNTransitionPair jPair){
		
		
		// Get the indecies.
		int i = Arrays.binarySearch(_dbmVarList, iPair);
		int j = Arrays.binarySearch(_dbmVarList, jPair);
		
		return _matrix[i][j];
	}
	
	/**
	 * Determines if this Octagon is a subset of Octagon otherOctagon.
	 * @param otherOctagon
	 * 		The Octagon to compare against.
	 * @return
	 * 		True if this is a subset of other; false otherwise.
	 */
//	public boolean subset(Octagon otherOctagon){
	@Override
	public boolean subset(Equivalence otherEquiv){
		
		Octagon otherOctagon = (Octagon) otherEquiv;
		
		// Check if the reference is null first.
		//*if(otherZone == null)
		//*{
		//*	return false;
		//*}
		if(otherOctagon == null){
			return false;
		}
		
		
		// Check for reference equality.
		//*if(this == otherZone)
		//*{
		//*	return true;
		//*}
		if(this == otherOctagon){
			return true;
		}
		
		
		// Check if the the same number of timers are present.
		//*if(this._indexToTimerPair.length != otherZone._indexToTimerPair.length){
		//*	return false;
		//*}
		/*
		 *  For a true subset relation, this check is not quite right. For this
		 *  Octagon to be a subset, the number of timers has to be at least as
		 *  many as the number of timers in the other Octagon. It is permissible
		 *  fore there to be more timers. When a timer is missing in an Octagon,
		 *  that means that there are no constraints. Thus having a constraint will
		 *  be a subset. The continuous variables are not as simple. A non-zero
		 *  rate continuous variable can be a subset of a rate zero. For simplicity,
		 *  we will assume that subsetting only occurs when the Octagons have
		 *  the same number of timers and non-zero continuous variable.
		 */
		if(this._dbmVarList.length != otherOctagon._dbmVarList.length){
			return false;
		}
		
		// Check if the transitions are the same.
		//*for(int i=0; i<this._indexToTimerPair.length; i++){
		//*	if(!(this._indexToTimerPair[i].equals(otherZone._indexToTimerPair[i]))){
		//*		return false;
		//*	}
		//*}
		for (int i=0; i<this._dbmVarList.length; i++){
			if (!(this._dbmVarList[i].equals(otherOctagon._dbmVarList[i]))){
				return false;
			}
		}
		
		
		// Check if the entries of this Zone are less than or equal to the entries
		// of the other Zone.
		//*for(int i=0; i<_matrix.length; i++)
		//*{
		//*	for(int j=0; j<_matrix[0].length; j++)
		//*	{
		//*		if(!(this._matrix[i][j] <= otherZone._matrix[i][j])){
		//*			return false;
		//*		}
		//*	}
		//*}
		for (int i=0; i<_matrix.length; i++){
			for (int j=0; j<_matrix[0].length; j++){
				if (!(this._matrix[i][j] <= otherOctagon._matrix[i][j])){
					return false;
				}
			}
		}
		
		//*return true;
		
		return true;
	}
	
	/**
	 * Determines if this Octagon is a superset of Octagon otherOctagon.
	 * @param otherOctagon
	 * 		The Octagon to compare against.
	 * @return
	 * 		True if this is a subset of other; false otherwise. More specifically it
	 *		gives the result of otherOctagon.subset(this). Thus it agrees with the subset method.
	 */
//	public boolean superset(Octagon otherOctagon){
	@Override
	public boolean superset(Equivalence otherOctagon){
		return otherOctagon.subset(this);
	}
	
	/**
	 * Performs the tighten algorithm of in Mine's Ocatgon paper.
	 */
	@Override
	public void recononicalize(){
				
		// This is the bagnara2008
		
		// Starts with Floyd-Walsh all pairs.
		for(int k=0; k<2*this._dbmVarList.length; k++){
			for (int i=0; i<2*this._dbmVarList.length; i++){
				for (int j=0; j<2*this._dbmVarList.length; j++){
					
					/*
					 *  Need to check for infinity in the summands.
					 *  If either summand is inifinite, then they
					 *  will not change the current value since we
					 *  only change if the sum is smaller. Logically,
					 *  we consider the largest integer as being infinity.
					 *  However, maybe a warning should be issued for
					 *  overflow not resulting from one of the bounds
					 *  being infinite.
					 */
					
					if(this._matrix[i][k] == Zone.INFINITY ||
							this._matrix[k][j] == Zone.INFINITY){
						continue;
					}
					
					
					int newValue =
							Math.min(this._matrix[i][j],
									this._matrix[i][k] + this._matrix[k][j]);
					this._matrix[i][j] = newValue;
					this._matrix[bar(j)][bar(i)] = newValue;
				}
			}
		}
		
		/*
		 * This to me does not seem to be sound for our
		 * purposes. This can make the upper bound and lower
		 * bound the nearest even number that less than
		 * or equal to the value. So if the upper bound is
		 * 5, then this routine makes it 4.
		 * 
		 * Would also have to account for the logical
		 * infinity.
		 * 
		 * Update: the recanonicalization routine gets upset
		 * when the bounds are not even. However, we
		 * need to over-approximate the state space. So,
		 * we need the ceiling and not the floor.
		 */
		// Need to decide whether this part of the
		// code is necessary.
		// Tightening.
//		for(int i=0; i<2*this._dbmVarList.length; i++){
//			this._matrix[i][bar(i)] = (int) (2*Math.floor(
//					this._matrix[i][bar(i)]/2.0));
//		}
		
		for(int i=0; i<2*this._dbmVarList.length; i++){
		this._matrix[i][bar(i)] = (int) (2*Math.ceil(
				this._matrix[i][bar(i)]/2.0));
	}
		
		
		// Now the coherence portion.
		for(int i=0; i<2*this._dbmVarList.length; i++){
			for(int j=0; j<2*this._dbmVarList.length; j++){
				
				/*
				 * Again care needs to be taken to account for the locigal
				 * infinity. We *should* only be dealing with positive
				 * infinities so the cases are infty+infty, infty+con, and
				 * infty-con. All cases should be taken to be infty, so the
				 * value should not change.
				 */
				
				if(this._matrix[i][bar(i)] == Zone.INFINITY ||
						this._matrix[bar(j)][j] == Zone.INFINITY){
					continue;
				}
				
				int testValue = (int)(Math.floor(this._matrix[i][bar(i)]/2.0)
						+ Math.floor(this._matrix[bar(j)][j]/2.0));
				
				int newValue =
						Math.min(this._matrix[i][j], testValue);
				
				this._matrix[i][j] = newValue;
				this._matrix[bar(j)][bar(i)] = newValue;
			}
			
			if(this._matrix[i][i] != 0 || this._matrix[bar(i)][bar(i)] != 0){
				throw new IllegalStateException("Diagonal non-zero");
			}
		}
	}
	
	/**
	 * Gives the Octagon obtained by firing a given Transitions.
	 * @param t
	 * 		The transitions being fired.
	 * @param enabledTran
	 * 		The list of currently enabled Transitions.
	 * @param localStates
	 * 		The current local states.
	 * @return
	 * 		The Octagon obtained by firing Transition t with enabled Transitions enabled
	 * 		enabledTran when the current state is localStates.
	 */
	@Override
	public Octagon fire(Transition t, LpnTranList enabledTran, 
			ContinuousRecordSet newAssignValues,
			State[] localStates){
	
		
		try {
			if(_writeLogFile != null){
				_writeLogFile.write(t.toString());
				_writeLogFile.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Create the LPNTransitionPair to check if the Transitions is in
		// the Octagon and to find the index.
		//*LhpnFile lpn = t.getLpn();
		//*int lpnIndex = lpn.getLpnIndex();
		//*int transitionIndex = t.getIndex();
		
		//*LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex);
		LPNTransitionPair ltPair = new LPNTransitionPair(t.getLpn().getLpnIndex(), t.getIndex());
		
		//*int dbmIndex = Arrays.binarySearch(_indexToTimerPair, ltPair);
		int baseIndex = Arrays.binarySearch(_dbmVarList, ltPair);
		
		//*if(dbmIndex <= 0){
		//*	return this;
		//*}
		// If the base index does not reference a timer in the DBM, then it is not an
		// active transition. Just return this Octagon.
		if(baseIndex < 0){
			return this;
		}
		
		
		// Get the new zone portion.
		//*Zone newZone = fireTransitionbydbmIndexNew(dbmIndex, enabledTran, localStates,
		//*		newAssignValues);
		// Fire the transition. This method doesn't deal with resetting rates.
		Octagon newOct = fireTransitionbyBaseIndex(baseIndex, enabledTran, localStates,
				newAssignValues);
		
		// Set all the rates to their lower bound.
		//*newZone.setAllToLowerBoundRate();
		newOct.setAllToLowerBoundRate();
		
		// Warp the Zone
		//*newZone.dbmWarp(this);
		// Warp the Octagon.
		newOct.dbmWarp(this);
		
		
		// Warping can wreck the newly assigned values so correct them.
		//*newZone.correctNewAssignemnts(newAssignValues);
		newOct.correctNewAssignments(newAssignValues);
		
		
		//*newZone.recononicalize();
		newOct.recononicalize();
		
		
		//*newZone.advance(localStates);
		newOct.advance(localStates);
		
		// Recanonicalize
		//*newZone.recononicalize();
		newOct.recononicalize();
		
		// Get the amount of time that has
		// advanced. If there is a variable left in the octagon.
		if(newOct._dbmVarList.length != 0){
			
			// Get the max value of the first
			// entry for calculating how
			// much time has advanced.
			int initial = newOct.getUpperBound(0);
			
			int time = (int)Math.ceil((newOct.getUpperBound(0) - initial)/2);
		
			newOct.adjustB3(newOct, time);
		}
		
		//*newZone.checkZoneMaxSize();
		
		//*return newZone;
		return newOct;
	}
	
	/**
	 * Gives the Octagon obtained by firing the given transition.
	 * @param index The index of the transition to fire.
	 * @param enabledTimers The timers that are enabled to fire.
	 * @param localStates The current local states.
	 * @param newAssignValues The set of new assignments made after firing.
	 * @return The new Octagon.
	 */
	public Octagon fireTransitionbyBaseIndex(int index, LpnTranList enabledTimers,
			State[] localStates, 
			ContinuousRecordSet newAssignValues)
	{
		/*
		 * For the purpose of adding the newly enabled transitions and removing
		 * the disable transitions, the continuous variables that still have
		 * a nonzero rate can be treated like still enbaled timers.
		 */
		
		// Initialize the zone.
		//*Zone newZone = new Zone();
		// Initialize an Octagon
		Octagon newOct = new Octagon();
		
		// These sets will differentiate between the new timers and the
		// old timers, that is between the timers that are not already in the
		// zone and those that are already in the zone.
		//*HashSet<LPNTransitionPair> newTimers = new HashSet<LPNTransitionPair>();
		//*HashSet<LPNTransitionPair> oldTimers = new HashSet<LPNTransitionPair>();
		
		/*
		 *  These sets will differentiate between the new timers and the
		 *  old timers, that is bewteen the timers that are not already in the
		 *  Octagon and those that are already in the Octagon.
		 */
		HashSet<LPNTransitionPair> newTimers = new HashSet<LPNTransitionPair>();
		HashSet<LPNTransitionPair> oldTimers = new HashSet<LPNTransitionPair>();
		
		
		// Copy the LPNs over.
		//*newZone._lpnList = new LhpnFile[this._lpnList.length];
		//*for(int i=0; i<this._lpnList.length; i++){
		//*	newZone._lpnList[i] = this._lpnList[i];
		//*}
		// Copy the LPNs over.
		newOct._lpnList = new LPN[this._lpnList.length];
		for (int i=0; i<this._lpnList.length; i++){
			newOct._lpnList[i] = this._lpnList[i];
		}
		
		
		//copyRatesNew(newZone, enabledTimers, newAssignValues);
		copyRatesNew(newOct, enabledTimers, newAssignValues);

		
		// Add the continuous variables to the enabled timers.

		
		//*for(int i=0; i<newZone._indexToTimerPair.length; i++)
		//*{
		for (int i=0; i<newOct._dbmVarList.length; i++){
		
			// Handle the continuous variables portion.
			//*if(newZone._indexToTimerPair[i] instanceof LPNContinuousPair){
			if (newOct._dbmVarList[i] instanceof LPNContinuousPair){
				
				//*LPNContinuousPair lcPair = 
					//*	(LPNContinuousPair) newZone._indexToTimerPair[i];
				
				LPNContinuousPair lcPair =
						(LPNContinuousPair) newOct._dbmVarList[i];
				
				// Get the record
				//*UpdateContinuous continuousState = 
					//*	newAssignValues.get(lcPair);
				UpdateContinuous continuousState =
						newAssignValues.get(lcPair);
				
				//*if(continuousState != null && (continuousState.is_newValue() ||
					//*	continuousState.newlyNonZero())){
				if(continuousState != null && (continuousState.is_newValue() ||
						continuousState.newlyNonZero())){
				
					// In the first case a new value has been assigned, so
					// consider the continuous variable a 'new' variable for
					// the purposes of copying relations from the previous zone.
					//*newTimers.add(newZone._indexToTimerPair[i]);
					newTimers.add(newOct._dbmVarList[i]);
					
					
					//*continue;
					continue;
				}
				//*}
				
				// At this point, either the continuous variable was not present
				// in the newAssignValues or it is in the newAssignValues and
				// satisfies the following: it already had a non-zero rate, is
				// being assigned another non-zero rate, and is not being assigned
				// a new value. This is becuase the field _indexToTimerPair only
				// deals with non-zero rates, so the variable must have a non-zero
				// rate. Furthermore the if statement takes care of the cases
				// when the rate changed from zero to non-zero and/or a new value
				// has been assigned.
				// In either of the cases, we consider the variable an 'old' variable
				// for the purpose of copying the previous zone information.
				
				//*oldTimers.add(newZone._indexToTimerPair[i]);
				oldTimers.add(newOct._dbmVarList[i]);
			}	
			//*}
			
			// At this point, the variable represents a transition (timer).
			// So determine whether this timer is new or old.
			//*else if(Arrays.binarySearch(this._indexToTimerPair,
				//*	newZone._indexToTimerPair[i]) >= 0 )
			//*{
			else if (Arrays.binarySearch(this._dbmVarList,
					newOct._dbmVarList[i]) >=0){
			
				// The timer was already present in the zone.
				//*oldTimers.add(newZone._indexToTimerPair[i]);
				// The timer was already present in the Octagon.
				oldTimers.add(newOct._dbmVarList[i]);
			//*}
			}
			//*else
			//*{
			else {
				// The timer is a new timer.
				//*newTimers.add(newZone._indexToTimerPair[i]);
				newTimers.add(newOct._dbmVarList[i]);
			//*}
			}
		//*}
		}
			
		// Create the new matrix.
		//*newZone._matrix = new int[newZone.matrixSize()][newZone.matrixSize()];
		newOct._matrix = new int[newOct.DBMsize()][newOct.DBMsize()];
		// Create the upper and lower bound 
		newOct._lowerBounds = new int[newOct._dbmVarList.length];
		newOct._upperBounds = new int[newOct._dbmVarList.length];
		
		// Note: For simplicity, make a copy of the current Octagon and perform the
		// restriction and re-canonicalization. Later add a copy re-canonicalization
		// that does the steps together.
		
		//*Zone tempZone = this.clone();
		Octagon tempOct = this.clone();
		
		//*tempZone.restrictTimer(index);
		//*tempZone.recononicalize();
		tempOct.restrictTimer(index);
		tempOct.recononicalize();
		
		// Copy the tempZone to the new zone.
		//*for(int i=0; i<tempZone.dbmSize(); i++)
		//*{
		// Copy the tempOct to the new Octagon.
		for (int i=0; i<tempOct._dbmVarList.length; i++){
		
			//*if(!oldTimers.contains(tempZone._indexToTimerPair[i]))
			//*{
				//*continue;
			//*}
			if (!oldTimers.contains(tempOct._dbmVarList[i])){
				continue;
			}
			
			// Get the new index of for the timer.
			//*int newIndexi = i==0 ? 0 : 
				//*Arrays.binarySearch(newZone._indexToTimerPair,
					//*	tempZone._indexToTimerPair[i]);
			int newIndexi = Arrays.binarySearch(newOct._dbmVarList,
					tempOct._dbmVarList[i]);
			
			//*for(int j=0; j<tempZone.dbmSize(); j++)
			//*{
			for (int j=0; j<tempOct._dbmVarList.length; j++){
			
				//*if(!oldTimers.contains(tempZone._indexToTimerPair[j]))
				//*{
					//*continue;
				//*}
				if (!oldTimers.contains(tempOct._dbmVarList[j])){
					continue;
				}
				
				//*int newIndexj = j==0 ? 0 : 
					//*Arrays.binarySearch(newZone._indexToTimerPair,
						//*	tempZone._indexToTimerPair[j]);
				int newIndexj = Arrays.binarySearch(newOct._dbmVarList,
						tempOct._dbmVarList[j]);
				
				
				//*newZone._matrix[Zone.dbmIndexToMatrixIndex(newIndexi)]
					//*	[Zone.dbmIndexToMatrixIndex(newIndexj)]
						//*		= tempZone.getDbmEntry(i, j);
				
				newOct._matrix[baseToNeg(newIndexi)][baseToNeg(newIndexj)]
						= tempOct._matrix[baseToNeg(i)][baseToNeg(j)];
				newOct._matrix[baseToNeg(newIndexi)][baseToPos(newIndexj)]
						= tempOct._matrix[baseToNeg(i)][baseToPos(j)];
				newOct._matrix[baseToPos(newIndexi)][baseToNeg(newIndexj)]
						= tempOct._matrix[baseToPos(i)][baseToNeg(j)];
				newOct._matrix[baseToPos(newIndexi)][baseToPos(newIndexj)]
						= tempOct._matrix[baseToPos(i)][baseToPos(j)];
				
			}
			//*}
		//*}
		}
		
		// Copy the upper and lower bounds.
		//*for(int i=1; i<tempZone.dbmSize(); i++)
		//*{
		for (int i=0; i<tempOct._dbmVarList.length; i++){
		
			// The block copies the upper and lower bound information from the 
			// old zone. Thus we do not consider anything that is not an old
			// timer.
			//*if(!oldTimers.contains(tempZone._indexToTimerPair[i]))
			//*{
			if (!oldTimers.contains(tempOct._dbmVarList[i])){
				// The block copies the upper and lower bound information from the
				// old Octagon. Thus we do not consider anything that is not an
				// old timer.
				
				// A hack to ensure that the newly zero variables
				// get the new values from the tempZone.
				//*if(tempZone._indexToTimerPair[i] instanceof LPNContinuousPair){
				if(tempOct._dbmVarList[i] instanceof LPNContinuousPair){	
				
					//*LPNContinuousPair lcPair = 
						//*	(LPNContinuousPair) tempZone._indexToTimerPair[i];
					LPNContinuousPair lcPair =
							(LPNContinuousPair) tempOct._dbmVarList[i];
					
					//*VariableRangePair vrp = newZone._rateZeroContinuous
						//*	.get(new LPNContAndRate(lcPair));
					VariableRangePair vrp = newOct._rateZeroContinuous
							.get(new LPNContAndRate(lcPair));
					
					//*if(vrp != null){
					if(vrp != null){
						// This means that the continuous variable was non-zero
						// and is now zero. Fix up the values according to 
						// the temp zone.
						//*IntervalPair newRange = tempZone.getContinuousBounds(lcPair);
						//*vrp.set_range(newRange);
						IntervalPair newRange = tempOct.getContinuousBounds(lcPair);
						vrp.set_range(newRange);
					}
					//*}
				}
				//*}
					
				//*continue;
				continue;
			//*}
			}
			
			//*if(_indexToTimerPair[i] instanceof LPNContinuousPair){
			if (_dbmVarList[i] instanceof LPNContinuousPair){
			
				//*LPNContinuousPair lcPair = (LPNContinuousPair) _indexToTimerPair[i];
				LPNContinuousPair lcPair = (LPNContinuousPair) _dbmVarList[i];
				
				// Check if a rate assignment has occured for any continuous
				// variables.
				//*UpdateContinuous updateRecord = 
					//*	newAssignValues.get(lcPair);
				UpdateContinuous updateRecord =
						newAssignValues.get(lcPair);
				
				
				//*if(updateRecord != null){
				if(updateRecord != null){
				
					// Since the variable is in the oldTimers, it cannot have had
					// a new value assigned to it. It must have had a new rate assignment

					//*IntervalPair rates = updateRecord.get_lcrPair().get_rateInterval();
					IntervalPair rates = updateRecord.get_lcrPair().get_rateInterval();
					
					
					// Copy the new rate information
					//*newZone.setLowerBoundByLPNTransitionPair(_indexToTimerPair[i],
						//*	rates.get_LowerBound());
					//*newZone.setUpperBoundByLPNTransitionPair(_indexToTimerPair[i], 
						//*	rates.get_UpperBound());
					
					// When the variable is a continuous variable, the range of rates
					// are stored in the _lowerBounds and _upperBounds member
					// variables.
					int newIndex = Arrays.binarySearch(newOct._dbmVarList,
							tempOct._dbmVarList[i]);
					newOct._lowerBounds[newIndex] = -1*rates.get_LowerBound();
					newOct._upperBounds[newIndex] = rates.get_UpperBound();
					
					// Copy the smallest and greatest continuous value.
					// The material was commented out in the Zone.
				
					//*continue;
					continue;
				}
				//*}
			//*}
			}
			
			//*newZone.setLowerBoundByLPNTransitionPair(tempZone._indexToTimerPair[i], 
				//*	-1*tempZone.getLowerBoundbydbmIndex(i));
			// The minus sign is because _matrix stores the negative of the lower bound.
			
			//*newZone.setUpperBoundByLPNTransitionPair(tempZone._indexToTimerPair[i],
				//*	tempZone.getUpperBoundbydbmIndex(i));
			
			// The cached lower and upper bound values of the delay are stored in the
			// _upperBounds and _lowerBounds member variables.
//			newOct._lowerBounds[i] = -1*tempOct._lowerBounds[i];
			int newIndex = Arrays.binarySearch(newOct._dbmVarList,
					tempOct._dbmVarList[i]);
			newOct._lowerBounds[newIndex] = tempOct._lowerBounds[i];
			newOct._upperBounds[newIndex] = tempOct._upperBounds[i];
			
		//*}
		}
		
		// Copy in the new relations for the new timers.
		//*for(LPNTransitionPair timerNew : newTimers)
		//*{
		for (LPNTransitionPair timerNew: newTimers){
		
			//*for(LPNTransitionPair timerOld : oldTimers)
			//*{
			for (LPNTransitionPair timerOld : oldTimers){
			
				//*newZone.setDbmEntry(newZone.timerIndexToDBMIndex(timerNew),
					//*	newZone.timerIndexToDBMIndex(timerOld),
						//* tempZone.getDbmEntry(0, tempZone.timerIndexToDBMIndex(timerOld)));

				
				//*newZone.setDbmEntry(newZone.timerIndexToDBMIndex(timerOld),
					//*	newZone.timerIndexToDBMIndex(timerNew),
						//*tempZone.getDbmEntry(tempZone.timerIndexToDBMIndex(timerOld), 0));
				
				int baseTimerNew = newOct.getBaseIndex(timerNew);
				int baseTimerOld = newOct.getBaseIndex(timerOld);
				
//				int upperBound = (int)Math.ceil(
//						tempOct._matrix[baseToPos(baseTimerOld)][baseToPos(baseTimerOld)]/2.0);
				int upperBound = tempOct.getUpperBoundTrue(
						tempOct.getBaseIndex(timerOld));
				
//				int lowerBound = (int)Math.ceil(
//						tempOct._matrix[baseToNeg(baseTimerOld)][baseToNeg(baseTimerOld)]/2.0);
				int lowerBound = tempOct.getLowerBoundTrue(
						tempOct.getBaseIndex(timerOld));
				
				
				// copy in lowerBound.
				newOct._matrix[baseToPos(baseTimerOld)][baseToPos(baseTimerNew)] = lowerBound;
				newOct._matrix[baseToPos(baseTimerOld)][baseToNeg(baseTimerNew)] = lowerBound;
				newOct._matrix[baseToPos(baseTimerNew)][baseToNeg(baseTimerOld)] = lowerBound;
				newOct._matrix[baseToNeg(baseTimerNew)][baseToNeg(baseTimerOld)] = lowerBound;
				
				// copy in upperBound.
				newOct._matrix[baseToNeg(baseTimerOld)][baseToPos(baseTimerNew)] = upperBound;
				newOct._matrix[baseToNeg(baseTimerOld)][baseToNeg(baseTimerNew)] = upperBound;
				newOct._matrix[baseToPos(baseTimerNew)][baseToPos(baseTimerOld)] = upperBound;
				newOct._matrix[baseToNeg(baseTimerNew)][baseToPos(baseTimerOld)] = upperBound;
			}
			//*}
		//*}
		}
		
		// Set the upper and lower bounds for the new timers.
		//*for(LPNTransitionPair pair : newTimers){
		for(LPNTransitionPair pair : newTimers){
		
			// Handle continuous case
			//*if(pair instanceof LPNContinuousPair){
			if (pair instanceof LPNContinuousPair){
				
				//*LPNContinuousPair lcPair = (LPNContinuousPair) pair;
				LPNContinuousPair lcPair = (LPNContinuousPair) pair;
				
				// If a continuous variable is in the newTimers, then an assignment
				// to the variable must have occurred. So get the value.
				//*UpdateContinuous updateRecord = newAssignValues.get(lcPair);
				UpdateContinuous updateRecord = newAssignValues.get(lcPair);
				
				
				//*if(updateRecord == null){
					//*throw new IllegalStateException("The pair " + pair
						//*	+ "was not in the new assigned values but was sorted as "
						//*	+ "a new value.");
				//*}
				if(updateRecord == null){
					throw new IllegalStateException("The pair " + pair
							+ " was not in the new assigned values but was sorted as"
							+ " a new value.");
				}
				
				
				//*IntervalPair rates = updateRecord.get_lcrPair().get_rateInterval();
				IntervalPair rates = updateRecord.get_lcrPair().get_rateInterval();
				
				//*IntervalPair values = updateRecord.get_Value();
				IntervalPair values = updateRecord.get_Value();

				//*newZone.setLowerBoundByLPNTransitionPair(lcPair,
					//*	rates.get_LowerBound());
				//*newZone.setUpperBoundByLPNTransitionPair(lcPair, 
					//*	rates.get_UpperBound());
				int baseIndexPair = newOct.getBaseIndex(lcPair);
				newOct._lowerBounds[baseIndexPair] = -1*rates.get_LowerBound();
				newOct._upperBounds[baseIndexPair] = rates.get_UpperBound();
				
				// Get the current rate.
				//*int currentRate = lcPair.getCurrentRate();
				int currentRate = lcPair.getCurrentRate();
				
				
				//*if(currentRate>= 0){
				if(currentRate >=0){
					
					// Copy the smallest and greatest continuous value.
					//*newZone.setDbmEntryByPair(lcPair,
						//*	LPNTransitionPair.ZERO_TIMER_PAIR, 
						//*	ContinuousUtilities.chkDiv(-1*values.get_LowerBound(),
						//*			currentRate, true));

					//*newZone.setDbmEntryByPair(LPNTransitionPair.ZERO_TIMER_PAIR,
					//*		lcPair,
					//*		ContinuousUtilities.chkDiv(values.get_UpperBound(),
					//*				currentRate, true));
					
					newOct._matrix[baseToPos(baseIndexPair)][baseToNeg(baseIndexPair)]
							= 2*ContinuousUtilities.chkDiv(-1*values.get_LowerBound(),
									currentRate, true);
					
					newOct._matrix[baseToNeg(baseIndexPair)][baseToPos(baseIndexPair)]
							= 2*ContinuousUtilities.chkDiv(values.get_LowerBound(),
									currentRate, true);
				//*}
				}
				//*else{
				else{
					// Copy the smallest and greatest continuous value.
					// For negative rates, the upper and lower bounds need
					// to be switched.
					//*newZone.setDbmEntryByPair(LPNTransitionPair.ZERO_TIMER_PAIR, 
					//*		lcPair,
					//*		ContinuousUtilities.chkDiv(-1*values.get_LowerBound(),
					//*				currentRate, true));

					//*newZone.setDbmEntryByPair(lcPair, 
					//*		LPNTransitionPair.ZERO_TIMER_PAIR,
					//*		ContinuousUtilities.chkDiv(values.get_UpperBound(),
					//*				currentRate, true));
					
					newOct._matrix[baseToNeg(baseIndexPair)][baseToPos(baseIndexPair)]
							= 2*ContinuousUtilities.chkDiv(-1*values.get_LowerBound(),
									currentRate, true);
					
					newOct._matrix[baseToPos(baseIndexPair)][baseToNeg(baseIndexPair)]
							= 2*ContinuousUtilities.chkDiv(values.get_LowerBound(),
									currentRate, true);
					
				//*}
				}
				//*continue;
				continue;
			//*}
			}
			
			
			// Get all the upper and lower bounds for the new timers.
			// Get the name for the timer in the i-th column/row of DBM
			//*String tranName = _lpnList[pair.get_lpnIndex()]
			//*		.getTransition(pair.get_transitionIndex()).getLabel();
			//*ExprTree delay = _lpnList[pair.get_lpnIndex()].getDelayTree(tranName);
			String tranName = _lpnList[pair.get_lpnIndex()]
					.getTransition(pair.get_transitionIndex()).getLabel();
			ExprTree delay = _lpnList[pair.get_lpnIndex()].getDelayTree(tranName);
			
			// Get the values of the variables for evaluating the ExprTree.
			//*HashMap<String, String> varValues = 
			//*	_lpnList[pair.get_lpnIndex()]
			//*			.getAllVarsWithValuesAsString(localStates[pair.get_lpnIndex()]
			//*					.getVariableVector());
			HashMap<String, String> varValues =
					_lpnList[pair.get_lpnIndex()]
							.getAllVarsWithValuesAsString(localStates[pair.get_lpnIndex()]
									.getVariableVector());
			
			if(delay == null){
				_lpnList[pair.get_lpnIndex()].changeDelay(tranName, "0");
				delay = _lpnList[pair.get_lpnIndex()].getDelayTree(tranName);
			}
			
			// Set the upper and lower bound.
			//*int upper, lower;
			//*if(delay.getOp().equals("uniform"))
			//*{
			int upper, lower;
			if(delay.getOp().equals("uniform")){
			
			
			//*	IntervalPair lowerRange = delay.getLeftChild()
			//*			.evaluateExprBound(varValues, null, null);
			//*	IntervalPair upperRange = delay.getRightChild()
			//*			.evaluateExprBound(varValues, null, null);
			
				IntervalPair lowerRange = delay.getLeftChild()
						.evaluateExprBound(varValues, null, null);
				IntervalPair upperRange = delay.getRightChild()
						.evaluateExprBound(varValues, null, null);
				
				// The lower and upper bounds should evaluate to a single
				// value. Yell if they don't.
			//*	if(!lowerRange.singleValue() || !upperRange.singleValue()){
			//*		throw new IllegalStateException("When evaulating the delay, " +
			//*				"the lower or the upper bound evaluated to a range " +
			//*				"instead of a single value.");
			//*	}
				if(!lowerRange.singleValue() || !upperRange.singleValue()){
					throw new IllegalStateException("When evaluating the delay, "
							+ "the lower or the upper bound evaluated to a range "
							+ "instead of a single value.");
				}
				
				//*lower = lowerRange.get_LowerBound();
				//*upper = upperRange.get_UpperBound();
				lower = lowerRange.get_LowerBound();
				upper = upperRange.get_UpperBound();
				
			}
			//*}
			//*else
			//*{
			else{
			
			
			//*	IntervalPair range = delay.evaluateExprBound(varValues, this, null);
				
				//*lower = range.get_LowerBound();
				//*upper = range.get_UpperBound();
				
				IntervalPair range = delay.evaluateExprBound(varValues, this, null);
				
				lower = range.get_LowerBound();
				upper = range.get_UpperBound();
				
			//*}
			}
				
				
			//*newZone.setLowerBoundByLPNTransitionPair(pair, lower);
			//*newZone.setUpperBoundByLPNTransitionPair(pair, upper);

			int baseIndex = Arrays.binarySearch(newOct._dbmVarList, pair);
			newOct._lowerBounds[baseIndex] = -1* lower;
			newOct._upperBounds[baseIndex] = upper;
			
		//*}
		}
		
		
		//Erase relationships for continuous variables that have had new values
		// assigned to them or a new non-rate zero value.
		//*for(int i = 1; i<newZone._indexToTimerPair.length &&
		//*			newZone._indexToTimerPair[i] instanceof LPNContinuousPair; i++){
		for (int i=0; i<newOct._dbmVarList.length
 				&& newOct._dbmVarList[i] instanceof LPNContinuousPair; i++){		
		//*	LPNContinuousPair lcPair = (LPNContinuousPair) newZone._indexToTimerPair[i];
			LPNContinuousPair lcPair = (LPNContinuousPair) newOct._dbmVarList[i];
			
			// Get the update variable.
			//*UpdateContinuous update = newAssignValues.get(lcPair);
			UpdateContinuous update = newAssignValues.get(lcPair);
			
			
			//*if(update != null && (update.is_newValue() || update.newlyNonZero())){
			if(update != null && (update.is_newValue() || update.newlyNonZero())){
			
			//*	for(int j=1; j<newZone._indexToTimerPair.length; j++){
				for (int j=0; j<newOct._dbmVarList.length; j++){
				
			//*		if (j==i){
			//*			continue;
			//*		}
					if(j == i){
						continue;
					}
					
			//*		newZone.setDbmEntry(i, j, Zone.INFINITY);
			//*		newZone.setDbmEntry(j, i, Zone.INFINITY);
					
					newOct._matrix[baseToNeg(i)][baseToNeg(j)] = Zone.INFINITY;
					newOct._matrix[baseToNeg(i)][baseToPos(j)] = Zone.INFINITY;
					newOct._matrix[baseToPos(i)][baseToNeg(j)] = Zone.INFINITY;
					newOct._matrix[baseToPos(i)][baseToPos(j)] = Zone.INFINITY;
					
					newOct._matrix[baseToNeg(j)][baseToNeg(i)] = Zone.INFINITY;
					newOct._matrix[baseToNeg(j)][baseToPos(i)] = Zone.INFINITY;
					newOct._matrix[baseToPos(j)][baseToNeg(i)] = Zone.INFINITY;
					newOct._matrix[baseToPos(j)][baseToPos(i)] = Zone.INFINITY;
				}
			//*	}
			//*}
				
			}
		//*}
		}

		
		//*return newZone;
		return newOct;
	}
	
	private void copyRatesNew(Octagon newOct, LpnTranList enabledTran,
			ContinuousRecordSet newAssignValues){
		
		// Create new rate zero member variable.
		//*newZone._rateZeroContinuous = new DualHashMap<LPNContAndRate,
			//*	VariableRangePair>();
		newOct._rateZeroContinuous = new DualHashMap<LPNContAndRate,
				VariableRangePair>();
		
		// Create new _indexToTimerPair.
		// First get the total number of non-zero rate continuous variables that
		// are present in the old zone.
		//*int totalContinuous = 0;
		//*for(int i=0; i<_lpnList.length; i++){
			//*totalContinuous += _lpnList[i].getTotalNumberOfContVars();
		//*}
		int totalContinuous =0;
		for (int i=0; i<_lpnList.length; i++){
			totalContinuous += _lpnList[i].getTotalNumberOfContVars();
		}

		//*int numberNonZero = totalContinuous - _rateZeroContinuous.size();
		int numberNonZero = totalContinuous - _rateZeroContinuous.size();


		// Note: Create an object that stores the records along with this information.
		//*int newNonZero = 0, newZero = 0;
		//*for(UpdateContinuous record : newAssignValues.keySet()){
			//*if(record.newlyNonZero()){
				//*newNonZero++;
			//*}
			//*if(record.newlyZero()){
				//*newZero++;
			//*}
		//*}
		int newNonZero = 0, newZero = 0;
		for (UpdateContinuous record : newAssignValues.keySet()){
			if(record.newlyNonZero()){
				newNonZero++;
			}
			if(record.newlyZero()){
				newZero++;
			}
		}
		

		//*int newSize = enabledTran.size() + numberNonZero + newNonZero - newZero + 1;
		int newSize = enabledTran.size() + numberNonZero + newNonZero - newZero;
		

		// Create the timer array.
		//*newZone._indexToTimerPair = new LPNTransitionPair[newSize];
		newOct._dbmVarList = new LPNTransitionPair[newSize];
		
		
		// Add in the zero timer.
		//*newZone._indexToTimerPair[0] = LPNTransitionPair.ZERO_TIMER_PAIR;
		// Octagons do not have a zero timer.
		
		
		//*int indexTimerCount = 1;
		int indexTimerCount = 0;
		
		
		// Sort the previous rate zero continuous variables into rate zero or non-zero.
		//*for(LPNContAndRate ltTranPair : _rateZeroContinuous.keySet()){
		for(LPNContAndRate ltTranPair : _rateZeroContinuous.keySet()){
		
			// Cast the index.
			//*LPNContinuousPair ltContPair = ltTranPair.get_lcPair();
			LPNContinuousPair ltContPair = ltTranPair.get_lcPair();

			// Check if the variable is a newly assigned value.
			//*UpdateContinuous assignedLtContPair = newAssignValues.get(ltContPair);
			UpdateContinuous assignedLtContPair = newAssignValues.get(ltContPair);
			
			
			//*if(assignedLtContPair != null){
			if(assignedLtContPair != null){
			
				//*if(assignedLtContPair.newlyNonZero()){
					// Variable was zero and is now non-zero, so add to the the non-zero
					// references.
					//*newZone._indexToTimerPair[indexTimerCount++] = 
							//*assignedLtContPair.get_lcrPair().get_lcPair().clone();
				//*}
				if(assignedLtContPair.newlyNonZero()){
					// Variable was zero and is now non-zero, so add to the non-zero
					// references.
					newOct._dbmVarList[indexTimerCount++] =
							assignedLtContPair.get_lcrPair().get_lcPair().clone();
				}
				//*else{
				else{
					// Variable was zero and is still zero, but an assignment has been
					// made. Simply add in the new assigned value.
					//*VariableRangePair vrp = this._rateZeroContinuous.get(ltTranPair);
					
					//*newZone._rateZeroContinuous.insert(assignedLtContPair.get_lcrPair(),
							//*new VariableRangePair(vrp.get_variable(),
									//*assignedLtContPair.get_Value()));
					VariableRangePair vrp = this._rateZeroContinuous.get(ltTranPair);
					newOct._rateZeroContinuous.insert(assignedLtContPair.get_lcrPair(),
							new VariableRangePair(vrp.get_variable(),
									assignedLtContPair.get_Value()));
				}
					
				//*}
			}
			//*}
			//*else{
				//*newZone._rateZeroContinuous
				//*.insert(ltTranPair, _rateZeroContinuous.get(ltTranPair));
			//*}
			else{
				newOct._rateZeroContinuous
				  .insert(ltTranPair, _rateZeroContinuous.get(ltTranPair));
			}
			
		}
		//*}
		
		
		// Sort the previous non-zero variables into the rate zero and non-zero.
		//*for(int i=1; this._indexToTimerPair[i] instanceof LPNContinuousPair; i++){
		for (int i=0; this._dbmVarList[i] instanceof LPNContinuousPair; i++){	
		
		
			//*LPNContinuousPair lcPair = (LPNContinuousPair) this._indexToTimerPair[i];
			LPNContinuousPair lcPair = (LPNContinuousPair) this._dbmVarList[i];
			
			// Check if an assignment has been made. 
			//*UpdateContinuous updateRecord = newAssignValues.get(lcPair);
			UpdateContinuous updateRecord = newAssignValues.get(lcPair);
			
			//*if(updateRecord != null){
			if(updateRecord != null){
			
				//*if(updateRecord.is_newZero()){
				if(updateRecord.is_newZero()){
					// The continuous variable is now a rate zero variable.
					
					//*LPNContinuousPair ltCar = updateRecord.get_lcrPair().get_lcPair();
					LPNContinuousPair ltCar = updateRecord.get_lcrPair().get_lcPair();
					
					//*Variable v = _lpnList[ltCar.get_lpnIndex()].
							//*getContVar(ltCar.get_ContinuousIndex());
					Variable v = _lpnList[ltCar.get_lpnIndex()]
							.getContVar(ltCar.get_ContinuousIndex());

					// Dewarp the upper and lower bounds.
					//*IntervalPair values = updateRecord.get_Value();
					//*int currentRate = getCurrentRate(ltCar);
					//*values.set_LowerBound(
							//*values.get_LowerBound() * currentRate);
					//*values.set_UpperBound(
							//*values.get_UpperBound() * currentRate);
					IntervalPair values = updateRecord.get_Value();
					int currentRate = getCurrentRate(ltCar);
					values.set_LowerBound(
							values.get_LowerBound()*currentRate);
					values.set_UpperBound(
							values.get_UpperBound()*currentRate);
					
					// Create a VariableRangePair.
					//*VariableRangePair vrp = new VariableRangePair(v,
							//*values);
					VariableRangePair vrp = new VariableRangePair(v,
							values);
					
					// Add the value to the map.
					//*newZone._rateZeroContinuous.insert(updateRecord.get_lcrPair(), vrp);
					newOct._rateZeroContinuous.insert(updateRecord.get_lcrPair(), vrp);
				}
				//*}
				//*else{
				else{
					// This non-zero variable still has rate non-zero, but replace
					// with the newAssignValues since the rate may have changed.
					//*newZone._indexToTimerPair[indexTimerCount++] =
							//*updateRecord.get_lcrPair().get_lcPair();
					newOct._dbmVarList[indexTimerCount++] =
							updateRecord.get_lcrPair().get_lcPair();
				}
				//*}
			}
			//*}
			//*else{
			else{
				// The variable was non-zero and hasn't had an assignment.
				//*newZone._indexToTimerPair[indexTimerCount++] =
						//*this._indexToTimerPair[i].clone();
				newOct._dbmVarList[indexTimerCount++] =
						this._dbmVarList[i].clone();
			}
			//*}
		}
		//*}
		
		
		// Copy over the new transitions.
		//*for(Transition t : enabledTran){
			//*int lpnIndex = t.getLpn().getLpnIndex();
			//*int tranIndex = t.getIndex();
			//*newZone._indexToTimerPair[indexTimerCount++] = 
					//*new LPNTransitionPair (lpnIndex, tranIndex);
		//*}
		for(Transition t : enabledTran){
			int lpnIndex = t.getLpn().getLpnIndex();
			int tranIndex = t.getIndex();
			newOct._dbmVarList[indexTimerCount++] =
					new LPNTransitionPair (lpnIndex, tranIndex);
		}
		
		
		//*Arrays.sort(newZone._indexToTimerPair);
		Arrays.sort(newOct._dbmVarList);
		
	}
	
	/**
	 * Restricts the lower bound of a timer.
	 * 
	 * @param timer
	 * 			The timer to tighten the lower bound.
	 */
	private void restrictTimer(int timer)
	{	
		//*_matrix[dbmIndexToMatrixIndex(timer)][dbmIndexToMatrixIndex(0)]
		//*       = getLowerBoundbydbmIndex(timer);
		
		_matrix[baseToPos(timer)][baseToNeg(timer)] =
				2*_lowerBounds[timer];
	}
	
	/**
	 * This method sets all the rate to their lower bound.
	 * Will not work quite right for continuous variables
	 * with rates that include zero.
	 */
	private void setAllToLowerBoundRate(){
		
		// Loop through the continuous variables.
		//*for(int i=1; i<_indexToTimerPair.length && 
		//*		_indexToTimerPair[i] instanceof LPNContinuousPair; i++){
		//*	LPNContinuousPair ltContPair = (LPNContinuousPair) _indexToTimerPair[i];
		for (int i=0; i<_dbmVarList.length &&
				_dbmVarList[i] instanceof LPNContinuousPair; i++){
			LPNContinuousPair ltContPair = (LPNContinuousPair) _dbmVarList[i];
		
			//* For this, recall that for a continuous variable that the lower bound
			//* rate is stored in the zero column of the matrix.
			/* For this, recall that the lower bound rate of for a continuous
			 * variable is stored in the member variable _lowerBounds.
			 */ 
			
			
			//*int lower = -1*_matrix[dbmIndexToMatrixIndex(i)][0];
			//*int upper = _matrix[0][dbmIndexToMatrixIndex(i)];
			//*int newRate;
			int lower = -1*_lowerBounds[i];
			int upper = _upperBounds[i];
			int newRate;
			
			/*
			 * Suppose the range of rates is [a,b]. If a>=0, then we set the
			 * rate to b. If b<=0, then we set the rate to a. Ohterwise,
			 * a<0<b. In this case we set the rate to b and allow rate change
			 * events to set the rate to a or 0, and in the case the rate is
			 * a, we allow another rate change event to 0.
			 */
			
			//*if(upper <= 0){
			//*	newRate = lower;
			//*}
			//*else{
				// In both cases of lower >=0 or lower < 0 < upper we set the
				// rate to the upper bound.
				//*newRate = upper;
			//*}
			if (upper <= 0){
				newRate = lower;
			}
			else {
				// In both cases of lower >=0 or lower < 0 < upper we set the
				// rate to the upper bound.
				newRate = upper;
			}
			
			
			//*setCurrentRate(ltContPair, newRate);
			setCurrentRate(ltContPair, newRate);
		//*}
		}
	}
	
	public void correctNewAssignments(ContinuousRecordSet newAssignValues){
		//Erase relationships for continuous variables that have had new values
		// assigned to them or a new non-rate zero value.
		//*for(int i = 1; i<this._indexToTimerPair.length &&
		//*		this._indexToTimerPair[i] instanceof LPNContinuousPair; i++){
		//*	LPNContinuousPair lcPair = (LPNContinuousPair) this._indexToTimerPair[i];
		for (int i=0; i<this._dbmVarList.length &&
				this._dbmVarList[i] instanceof LPNContinuousPair; i++){
			LPNContinuousPair lcPair = (LPNContinuousPair) this._dbmVarList[i];
		
			// Get the update variable.
			//*UpdateContinuous update = newAssignValues.get(lcPair);
			//*if(update != null && (update.is_newValue() || update.newlyNonZero())){
			UpdateContinuous update = newAssignValues.get(lcPair);	
			if(update != null && (update.is_newValue() || update.newlyNonZero())){
			
				//*IntervalPair values = update.get_Value();
				//*int currentRate = lcPair.getCurrentRate();
				IntervalPair values = update.get_Value();
				int currentRate = lcPair.getCurrentRate();
				
				// Correct the upper and lower bounds.
				//*if(lcPair.getCurrentRate()>0){
				if(lcPair.getCurrentRate() > 0){
				
				
				//*	setDbmEntry(i, 0, 
				//*			ContinuousUtilities.chkDiv(-1*values.get_LowerBound(),
				//*					currentRate, true));
					/*
					 *  Set the lower bound. Recall in an Octagon with variable v,
					 *  the lower bound is in v- - v+
					 */
//					_matrix[baseToNeg(i)][baseToNeg(i)] = 2*
//							ContinuousUtilities.chkDiv(-1*values.get_LowerBound(),
//									currentRate, true);
					_matrix[baseToPos(i)][baseToNeg(i)] = 2*
							ContinuousUtilities.chkDiv(-1*values.get_LowerBound(),
									currentRate, true);
					
				//*	setDbmEntry(0,i, 
				//*			ContinuousUtilities.chkDiv(values.get_UpperBound(),
				//*					currentRate, true));
					
					/* 
					 * Set the upper bound. Recall in an Octagon with variable v,
					 * the upper bound is in the entry v+ - v-.
					 */
					_matrix[baseToNeg(i)][baseToPos(i)] = 2*
							ContinuousUtilities.chkDiv(values.get_UpperBound(),
									currentRate, true);
				}
				//*}
				//*else{
				else{
				//*	setDbmEntry(i,0, 
				//*			ContinuousUtilities.chkDiv(values.get_UpperBound(),
				//*					currentRate, true));
					
				//*	setDbmEntry(0, i, 
				//*			ContinuousUtilities.chkDiv(-1*values.get_LowerBound(),
				//*					currentRate, true));
					
					_matrix[baseToNeg(i)][baseToNeg(i)] = 2*
							ContinuousUtilities.chkDiv(values.get_UpperBound(),
									currentRate, true);
					_matrix[baseToPos(i)][baseToPos(i)] = 2*
							ContinuousUtilities.chkDiv(-1*values.get_LowerBound(),
									currentRate, true);
					
				}
				//*}
				
				// Erase the relationships.
				//*for(int j=1; j<this._indexToTimerPair.length; j++){
				//*	if (j==i){
						//*continue;
					//*}
					//*this.setDbmEntry(i, j, Zone.INFINITY);
					//*this.setDbmEntry(j, i, Zone.INFINITY);
				//*}
				for (int j=0; j<this._dbmVarList.length; j++){
					if (j == i){
						// When the baseIndex for the column and row are the same
						// the only relationships recorded are the upper and lower
						// bounds on the variable which should not be erased.
						continue;
					}
					this._matrix[baseToNeg(i)][baseToNeg(j)] = Zone.INFINITY;
					this._matrix[baseToNeg(i)][baseToPos(j)] = Zone.INFINITY;
					this._matrix[baseToPos(i)][baseToNeg(j)] = Zone.INFINITY;
					this._matrix[baseToPos(i)][baseToPos(j)] = Zone.INFINITY;
					
					this._matrix[baseToNeg(j)][baseToNeg(i)] = Zone.INFINITY;
					this._matrix[baseToNeg(j)][baseToPos(i)] = Zone.INFINITY;
					this._matrix[baseToPos(j)][baseToNeg(i)] = Zone.INFINITY;
					this._matrix[baseToPos(j)][baseToPos(i)] = Zone.INFINITY;
				}
			}
			//*}
		//*}
		}
	}
	
	/**
	 *  This fire method fires a rate change event.
	 * 
	 * @param ltPair
	 * 		The index of the continuous variable whose rate needs to be changed.
	 * @param rate
	 * 		The new rate.
	 * @return
	 * 		The new zone resulting from the rate change.
	 */
	@Override
	public Octagon fire(LPNTransitionPair ltPair, int rate){
		
		// Make a copy of the Zone.
		//* Zone resultZone = this.clone();
		
		// Make a copy of the Octagon.
		Octagon resultOct = this.clone();	
		
		
		// Change the current rate of the continuous variable.
		//* setCurrentRate(ltPair, rate);
		setCurrentRate(ltPair, rate);
		
				
		// Warp the octagon.
		//* resultZone.dbmWarp(this);
		resultOct.dbmWarp(this);
		
					
		//*return resultZone;
		return resultOct;
	}
	
	/**
	 * Advances time.
	 * @param localStates
	 */
	@Override
	public void advance(State[] localStates){
		
		//*for(LPNTransitionPair ltPair : _indexToTimerPair){
			//*if(ltPair.equals(LPNTransitionPair.ZERO_TIMER_PAIR)){
				//*continue;
			//*}
		
			//*// Get the new value.
			//*int newValue = 0;
		
			//*if(!(ltPair instanceof LPNContinuousPair)){
				//*// If the pair is a timer, then simply get the stored largest value.
				
		
				//*int index = timerIndexToDBMIndex(ltPair);

				//*newValue = getUpperBoundbydbmIndex(index);
			//*}
			//*else{
				//* // If the pair is a continuous variable, then need to find the 
				//* // possible largest bound governed by the inequalities.
				//* newValue = ContinuousUtilities.maxAdvance(this,ltPair, localStates);
				
				
			//*}
			
			//*// In either case (timer or continuous), set the upper bound portion
			//*// of the DBM to the new value.
			//*setDbmEntryByPair(LPNTransitionPair.ZERO_TIMER_PAIR, ltPair, newValue);
		//*}
		
		// Advance the upper bounds.
		for (int i=0; i<_dbmVarList.length; i++){
			// Get the pair.
			LPNTransitionPair ltPair = this._dbmVarList[i];
			
			// Variable to store the new value.
			int newValue = 0;
			
			if ( !(ltPair instanceof LPNContinuousPair)){
				// If the pair is a timer, simply get the largest value.
				newValue = this._upperBounds[i];
			}
			else {
				// The pair is a continuous variable. The largest time
				// advancement is governed by the rate and the inequalities.
				// Use the maxAdvance method to get the new value.
				newValue = ContinuousUtilities.maxAdvance(this, ltPair, localStates);
			}
			
			// In either case (timer or continuous), set the upper bound portion
			// of the DBM to the new value.
			this._matrix[baseToNeg(i)][baseToPos(i)] = 
					newValue == Zone.INFINITY ? Zone.INFINITY : 2*newValue;
		}
		
		// Handle the b3 entry.
		// As a first approach, b3 is set to infinity so it will not restrict the
		// advancement. Later, b3 will be corrected according to how much time
		// elapses. It could just as well be set to M_y-M_x as well (which
		// places the upper boundary line going through the upper right corner
		// of the rectangle with upper bounds My and M_x), but this requires
		// also handling infinite bounds.
		for(int i=0; i<this._dbmVarList.length; i++){
			for(int j=i+1; j<this._dbmVarList.length; j++){
				this._matrix[baseToNeg(i)][baseToPos(j)] = Zone.INFINITY;
				this._matrix[baseToNeg(j)][baseToPos(i)] = Zone.INFINITY;
			}
		}
	}
	
	private void adjustB3(Octagon oct, int timeAdvance){
		//TODO: Finish this method
		
		// Something still has to be done to be more accurate.
	}
	
	/**
	 * Resets the rates of all continuous variables to be their
	 * lower bounds.
	 */
	@Override
	public Octagon resetRates(){
		/*
		 * Create a new zone.
		 */
		/*
		 * Create a new Octagon
		 */
		
		
		//*Zone newZone = new Zone();
		Octagon newOct = new Octagon();
		
		// Copy the LPNs over.
		//*newZone._lpnList = new LhpnFile[this._lpnList.length];
		//*for(int i=0; i<this._lpnList.length; i++){
			//*newZone._lpnList[i] = this._lpnList[i];
		//*}
		newOct._lpnList = new LPN[this._lpnList.length];
		for (int i=0; i<this._lpnList.length; i++){
			newOct._lpnList[i] = this._lpnList[i];
		}
		
		
		/* 
		 * Collect the rate zero variables whose range of rates are not
		 * identically zero. These will be moved out of the zone when
		 * the rate is reset.
		 * 
		 * Copy over the rate zero variables that remain rate zero.
		 */
		
		//*newZone._rateZeroContinuous = new DualHashMap<LPNContAndRate,
			//*	VariableRangePair>();
		newOct._rateZeroContinuous = new DualHashMap<LPNContAndRate,
				VariableRangePair>();
		
		//*HashSet<Map.Entry<LPNContAndRate, VariableRangePair>> newlyNonZero =
				//*new HashSet<Map.Entry<LPNContAndRate, VariableRangePair>>();
		HashSet<Map.Entry<LPNContAndRate, VariableRangePair>> newlyNonZero =
				new HashSet<Map.Entry<LPNContAndRate, VariableRangePair>>();
		
		
		//*for(Map.Entry<LPNContAndRate, VariableRangePair> variable :
			//*_rateZeroContinuous.entrySet()){
		for(Map.Entry<LPNContAndRate, VariableRangePair> variable :
			_rateZeroContinuous.entrySet()){
			
			// Check for a single value which indicates that zero is
			// the only possible rate.
			//*if(variable.getValue().get_range().singleValue()){
//			if(variable.getValue().get_range().singleValue()){
			if(variable.getKey().get_rateInterval().singleValue()){
				// This variable only has zero as a rate so keep it
				// in the rate zero variables.
				//*newZone._rateZeroContinuous.insert(variable.getKey(),
						//*variable.getValue());
				newOct._rateZeroContinuous.insert(variable.getKey(),
						variable.getValue());
			}
			//*}
			//*else{
			else{
				// This variable will need to be added to the zone.
				//*newlyNonZero.add(variable);
				newlyNonZero.add(variable);
			}
			//*}
		}
		//*}
		
		
		/*
		 * Calulate the size of the _indexToTimerPairs array and create
		 * it.
		 */
		/*
		 * Calculate the size of the _dbmVarList array and create it.
		 */
		
		
		//*int oldSize = this._indexToTimerPair.length;
		//*int newSize = oldSize + newlyNonZero.size();
		int oldSize = this._dbmVarList.length;
		int newSize = oldSize + newlyNonZero.size();
		
		
		
		//*newZone._indexToTimerPair = new LPNTransitionPair[newSize];
		newOct._dbmVarList = new LPNTransitionPair[newSize];
		
		/*
		 * Copy over the old pairs and add the new ones.
		 */
		//*for(int i=0; i< this._indexToTimerPair.length; i++){
			//*newZone._indexToTimerPair[i] = this._indexToTimerPair[i].clone();
		//*}
		for (int i=0; i<this._dbmVarList.length; i++){
			newOct._dbmVarList[i] = this._dbmVarList[i].clone();
		}
		
		
		//*for(Map.Entry<LPNContAndRate, VariableRangePair> variable :
			//*newlyNonZero){
			//*newZone._indexToTimerPair[oldSize++] = variable.getKey()
					//*.get_lcPair().clone();
		//*}
		for(Map.Entry<LPNContAndRate, VariableRangePair> variable :
			newlyNonZero){
			newOct._dbmVarList[oldSize++] = variable.getKey()
					.get_lcPair().clone();
		}
		
		
		/*
		 * Sort.
		 */
	    //*Arrays.sort(newZone._indexToTimerPair);
		Arrays.sort(newOct._dbmVarList);
		
		/*
		 * Copy over the old matrix values and new constraints.
		 */
		//*newZone._matrix = new int[newZone.matrixSize()][newZone.matrixSize()];
	    newOct._matrix = new int[newOct.DBMsize()][newOct.DBMsize()];
	    newOct._lowerBounds = new int[newOct._dbmVarList.length];
    	newOct._upperBounds = new int[newOct._dbmVarList.length];
		
		//*for(int i =0; i< this.dbmSize(); i++){
//	    for(int i=0; i<this.DBMsize(); i++){
	    for(int i=0; i<this._dbmVarList.length; i++){
			
			//*int newi = Arrays.binarySearch(newZone._indexToTimerPair,
					//*this._indexToTimerPair[i]);
	    	int newi = Arrays.binarySearch(newOct._dbmVarList,
	    			this._dbmVarList[i]);
	    	
			
			//*if(newi < 0){
				//*System.err.println("In resetRates, old value was not found"+
						//*" in new value.");
				//*continue;
			//*}
	    	if(newi <0){
	    		System.err.println("In resetRates, old values was not found"
	    				+ " in new value.");
	    		continue;
	    	}
	    	
	    	// Create the upper and lower bound arrays.
//	    	newOct._lowerBounds = new int[newOct._dbmVarList.length];
//	    	newOct._upperBounds = new int[newOct._dbmVarList.length];
			
			// Copy upper and lower bounds for the variable.
			//*newZone._matrix[newZone.dbmIndexToMatrixIndex(newi)][0] =
					//*this._matrix[this.dbmIndexToMatrixIndex(i)][0];
			//*newZone._matrix[0][newZone.dbmIndexToMatrixIndex(newi)] =
					//*this._matrix[0][this.dbmIndexToMatrixIndex(i)];
			// Copy the upper and lower bounds for the variable.
	    	// These bounds are containd in the _lowerBounds, _upperBounds
	    	// member variables.
	    	newOct._lowerBounds[newi] = this._lowerBounds[i];
	    	newOct._upperBounds[newi] = this._upperBounds[i];
			
			// Copy the DBM Entry
			//for(int j=0; j< this.dbmSize(); j++){
//	    	for(int j=0; j<this.DBMsize(); j++){
	    	for(int j=0; j<this._dbmVarList.length; j++){
	    		// Make sure that DBMsize is correct instead of _dbmVarList.size. done.
	    		
	    		
				//*int newj = Arrays.binarySearch(newZone._indexToTimerPair,
						//*this._indexToTimerPair[j]);
	    		int newj = Arrays.binarySearch(newOct._dbmVarList,
	    				this._dbmVarList[j]);
				
				//*if(newj < 0){
					//*System.err.println("In resetRates, old value was not"+
							//*" found in new value.");
					//*continue;
				//*}
	    		if(newj <0){
	    			System.err.println("In resetRates, old value was not"
	    					+ " found in new values.");
	    		}
	    		
				
				//*newZone.setDbmEntry(newi, newj, this.getDbmEntry(i, j));
	    		newOct._matrix[baseToNeg(newi)][baseToNeg(newj)] =
	    				this._matrix[baseToNeg(i)][baseToNeg(j)];
	    		newOct._matrix[baseToPos(newi)][baseToNeg(newj)] =
	    				this._matrix[baseToPos(i)][baseToNeg(j)];
	    		newOct._matrix[baseToNeg(newi)][baseToPos(newj)] =
	    				this._matrix[baseToNeg(i)][baseToPos(j)];
	    		newOct._matrix[baseToPos(newi)][baseToPos(newj)] =
	    				this._matrix[baseToPos(i)][baseToPos(j)];
	    	}
			//*}
	    }
		//*}
		
		//*for(Map.Entry<LPNContAndRate, VariableRangePair> variable: newlyNonZero){
	    for(Map.Entry<LPNContAndRate, VariableRangePair> variable: newlyNonZero){
	    
			//*LPNTransitionPair currentVariable = variable.getKey().get_lcPair();
	    	LPNTransitionPair currentVariable = variable.getKey().get_lcPair();
			
			//*int currentIndex = Arrays.binarySearch(newZone._indexToTimerPair,
					//*currentVariable);
	    	int currentIndex = Arrays.binarySearch(newOct._dbmVarList,
	    			currentVariable);
			
			//*IntervalPair rangeOfRates = variable.getKey().get_rateInterval();
			//*IntervalPair rangeOfValues = variable.getValue().get_range();
			IntervalPair rangeOfRates = variable.getKey().get_rateInterval();
			IntervalPair rangeOfValues = variable.getValue().get_range();
	    	
			/*
			 *  First set the range of rates, current rate, and the lower and upper
			 *  bounds for the newly added continuous variables.
			 */
			//*nnewZone.setLowerBoundbydbmIndex(currentIndex, rangeOfRates.get_LowerBound());
			//*newZone.setUpperBoundbydbmIndex(currentIndex, rangeOfRates.get_UpperBound());
			//*newZone.setDbmEntry(currentIndex, 0, -1*rangeOfValues.get_LowerBound());
			//*newZone.setDbmEntry(0, currentIndex, rangeOfValues.get_UpperBound());
			newOct._lowerBounds[currentIndex] = -1*rangeOfRates.get_LowerBound();
			newOct._upperBounds[currentIndex] = rangeOfRates.get_UpperBound();
			// The upper bound is v+ - v- and the lower bound is v- -v+
			// but remember it is column minus row.
			newOct._matrix[baseToPos(currentIndex)][baseToNeg(currentIndex)] =
					-2*rangeOfValues.get_LowerBound();
			newOct._matrix[baseToNeg(currentIndex)][baseToPos(currentIndex)] =
					2*rangeOfValues.get_UpperBound();
			
			
			//*for(int j=1; j<newZone.dbmSize(); j++){
//			for (int j=0; j<newOct.DBMsize(); j++){
			for(int j=0; j<newOct._dbmVarList.length; j++){
				//*if(currentIndex == j){
					//*continue;
				//*}
				if(currentIndex == j){
					continue;
				}
				
				//*newZone.setDbmEntry(currentIndex, j, Zone.INFINITY);
				//*newZone.setDbmEntry(j, currentIndex, Zone.INFINITY);
				newOct._matrix[baseToNeg(currentIndex)][baseToNeg(j)]
						= Zone.INFINITY;
				newOct._matrix[baseToNeg(currentIndex)][baseToPos(j)]
						= Zone.INFINITY;
				newOct._matrix[baseToPos(currentIndex)][baseToNeg(j)]
						= Zone.INFINITY;
				newOct._matrix[baseToPos(currentIndex)][baseToPos(j)]
						= Zone.INFINITY;
				
				newOct._matrix[baseToNeg(j)][baseToNeg(currentIndex)]
						= Zone.INFINITY;
				newOct._matrix[baseToPos(j)][baseToNeg(currentIndex)]
						= Zone.INFINITY;
				newOct._matrix[baseToNeg(j)][baseToPos(currentIndex)]
						= Zone.INFINITY;
				newOct._matrix[baseToPos(j)][baseToPos(currentIndex)]
						= Zone.INFINITY;
			}
			//*}
	    	
	    }
		//*}
		
		/*
		 * Reset all the rates.
		 */
		//*newZone.setAllToLowerBoundRate();
	    newOct.setAllToLowerBoundRate();
		
		/*
		 * recanonicalize, warp, recanonicalize.
		 */
		//*newZone.recononicalize();
		//*newZone.dbmWarp(this);
		//*newZone.recononicalize();
	    newOct.recononicalize();
	    newOct.dbmWarp(this);
	    newOct.recononicalize();
	    
		
		//*return newZone;
	    return newOct;
	//*}
	}
	
	/**
	 * Returns a Octagon that is the result from restricting the
	 * this Octagon according to a list of firing event inequalities.
	 * @param eventSet
	 * 		The list of inequalities that are firing.
	 * @return The new Octagon that is the result of restricting this Octagon
	 *  according to the firing of the inequalities in the eventSet.
	 */
	@Override
	public Octagon getContinuousRestrictedZone(EventSet eventSet, State[] localStates){
		// Make a new copy of the zone.
		//*Zone z = this.clone();
		// Make a new copy of the Octagon.
		Octagon oct = this.clone();
		
		
		//*if(eventSet == null){
			//*return z;
		//*}
		if(eventSet == null){
			return oct;
		}
		
		
		//*z.restrictContinuous(eventSet);
		oct.restrictContinuous(eventSet);
		
		
		//*return z;
		return oct;
	}
	
	/**
	 * Restricts the continuous variables in the Octagon according to the inequalities in a set of events.
	 * @param eventSet
	 * 			A set of inequality events. Does nothing if the event set does not contain inequalities.
	 */
	private void restrictContinuous(EventSet eventSet){
	
		/*
		 * TODO: check logic that bounds need to be adjusted by two,
		 * since the bounds are twice the actual bounds. Also need to
		 * check the logic for adjusting b3. If b3 is not adjusted and
		 * the bound changes, the octagon may become empty.
		 */
				
		// Check that the eventSet is a set of Inequality events.
		//*if(!eventSet.isInequalities()){
			// If the eventSet is not a set of inequalities, do nothing.
			//*return;
		//*}
		if(!eventSet.isInequalities()){
			// If the eventSet is not a set of inequalities, do nothing.
			return;
		}
		
		
		//*HashSet<LPNContinuousPair> adjustedColumns = new HashSet<LPNContinuousPair>();
		HashSet<LPNContinuousPair> adjustedColumns = new HashSet<LPNContinuousPair>();
		
		
		//*boolean needsAdjusting = false;
		boolean needsAdjusting = false;
		
		// Restrict the variables according to each of the inequalities in the eventSet.
		//*for(Event e : eventSet){
		for (Event e: eventSet){
			// Get the inequality.
			//*InequalityVariable iv = e.getInequalityVariable();
			InequalityVariable iv = e.getInequalityVariable();
			
			// Extract the variable. I will assume the inequality only depends on a single 
			// variable.
			//*Variable x = iv.getContVariables().get(0);
			Variable x = iv.getContVariables().get(0);
			
			// Extract the index.
			//*int lpnIndex = iv.get_lpn().getLpnIndex();
			int lpnIndex = iv.get_lpn().getLpnIndex();
			
			// Extract the variable index.
			//*DualHashMap<String, Integer> variableIndexMap = _lpnList[lpnIndex].getContinuousIndexMap();
			//*int  variableIndex = variableIndexMap.getValue(x.getName());
			DualHashMap<String, Integer> variableIndexMap = _lpnList[lpnIndex].getContinuousIndexMap();
			int variableIndex = variableIndexMap.getValue(x.getName());
			
			// Package it up for referencing.
			//*LPNContinuousPair ltContPair = new LPNContinuousPair(lpnIndex, variableIndex);
			LPNContinuousPair ltContPair = new LPNContinuousPair(lpnIndex, variableIndex);

			// Need the current rate for the variable, grab the stored LPNContinuousPair.
			//*int zoneIndex = Arrays.binarySearch(_indexToTimerPair, ltContPair);
			//*if(zoneIndex > 0){
				//*ltContPair = (LPNContinuousPair) _indexToTimerPair[zoneIndex];
			//*}
			int octIndex = Arrays.binarySearch(_dbmVarList, ltContPair);
			if (octIndex >= 0){
				ltContPair = (LPNContinuousPair) _dbmVarList[octIndex];
			}
			
			// Perform the restricting.
			//*needsAdjusting = needsAdjusting | restrictContinuous(ltContPair, iv.getConstant());
			needsAdjusting = needsAdjusting | restrictContinuous(ltContPair,
					iv.getConstant());
			
			
			//*if(needsAdjusting){
				//*adjustedColumns.add(ltContPair);
			//*}
			if(needsAdjusting){
				adjustedColumns.add(ltContPair);
			}
			
		//*}
		}
		
		// If one of the continuous variables has been moved forward, the other colmns
		// need to be adjusted to keep a consistent zone.
		//*if(needsAdjusting){
		if(needsAdjusting){
		
			// At least one of the continuous variables has been moved forward,
			// so we need to ajust the bounds to keep a consistent zone.
			//*for(int i=1; i<_indexToTimerPair.length; i++){
			for (int i=0; i<_dbmVarList.length; i++){
				
				//*LPNTransitionPair ltpair = _indexToTimerPair[i];
				LPNTransitionPair ltpair = _dbmVarList[i];
				
				//*if(adjustedColumns.contains(ltpair)){
				if(adjustedColumns.contains(ltpair)){
					// This continuous variables already had the upper bound
					// adjusted.
					//*continue;
					continue;
				}
				//*}
				// Add one to the upper bounds.
				//*setDbmEntry(0, i, getDbmEntry(0, i)+1);
				// Adjust the upper bound to past the inequality.
				// With a zone we added 1. Should we add 1 or 2 for
				// Octagons? I'll choosing 1 for right now.
				// Upper bound is v+ -v- (remember column minus row).
				_matrix[baseToNeg(i)][baseToPos(i)] += 2;
				
			}
			//*}
			
			// For every continous variable that was adjusted, need
			// to adjust the b3 entry. We will add one to the b3 entry
			// for each continuous variable involved.
			for(int i=0; i<_dbmVarList.length; i++){
				
				LPNTransitionPair ltpair = _dbmVarList[i];
//				
//				if(!adjustedColumns.contains(ltpair)){
//					// If this continuous variable has not
//					// had the upper bound change, then
//					// do nothing.
//					continue;
//				}
				
				for (int j=0; j<_dbmVarList.length; j++){
					
					if(adjustedColumns.contains(_dbmVarList[j]) && j<=i){
						continue;
					}
					
					_matrix[baseToNeg(i)][baseToPos(j)] += 2;
					_matrix[baseToNeg(j)][baseToPos(i)] += 2;
				}
			}
		}
		//*}
	}
	
	/**
	 * Restricts the lower bound of a continuous variable. Also checks fixes
	 * the upper bound to be at least as large if needed. This method
	 * is usually used as a result of an event firing.
	 * @param ltContPair
	 * 		The index of the continuous variable to restrict.
	 * @param constant
	 * 		The constant value of the inequality event that is being used to update
	 * 		the variable indexed by ltContPair.
	 * 
	 */
	private boolean restrictContinuous(LPNContinuousPair ltContPair, int constant){
		
		// It will be quicker to get the DBM index for the ltContPair one time.
		//*int variableIndex = timerIndexToDBMIndex(ltContPair);
		//*int zeroIndex = timerIndexToDBMIndex(LPNTransitionPair.ZERO_TIMER_PAIR);
		int baseIndex = Arrays.binarySearch(_dbmVarList, ltContPair);
		
		// Set the lower bound the variable (which is the DBM[variabl][0] entry.
		// Note : the lower bound in the zone is actually the negative of the lower
		// bound hence the -1 on the warpValue.
		//*setDbmEntry(variableIndex, zeroIndex, ContinuousUtilities.chkDiv(-1*constant, ltContPair.getCurrentRate(), true));
		
		// Set the lower bound for the variable (V- - v+) entry.
		// Note: lower bounds are doubled in the Octagon and are negative.
		int newLower = ContinuousUtilities.chkDiv(-2*constant, ltContPair.getCurrentRate(), true);
		_matrix[baseToPos(baseIndex)][baseToNeg(baseIndex)] = newLower%2 == 0 ? newLower: newLower -1;
		
		
		// Check if the upper bound needs to be advanced and advance it if necessary.
		//*if(getDbmEntry(zeroIndex, variableIndex) < ContinuousUtilities.chkDiv(constant, ltContPair.getCurrentRate(), true)){
		if(_matrix[baseToNeg(baseIndex)][baseToPos(baseIndex)]
				< ContinuousUtilities.chkDiv(2*constant, ltContPair.getCurrentRate(), true)){
			// If the upper bound in the zones is less than the new restricting value, we
			// must advance it for the zone to remain consistent.
			//*setDbmEntry(zeroIndex, variableIndex, ContinuousUtilities.chkDiv(constant, ltContPair.getCurrentRate(), true));
			
			// If the upper bound in the Octagon is less than the new
			// restricting value, we must advance it for the
			// Octagon to remain consistent.
			int newUpper = ContinuousUtilities.chkDiv(2*constant, ltContPair.getCurrentRate(), true);
			_matrix[baseToNeg(baseIndex)][baseToPos(baseIndex)] = newUpper%2 == 0 ? newUpper: newUpper +1;
			
			
			//*return true;
			return true;
			
		//*}
		}
		
		return false;
	}
	
	/**
	 * The list of enabled timers.
	 * @return
	 * 		The list of all timers that have reached their lower bounds.
	 */
	@Override
	public List<Transition> getEnabledTransitions(){
		
		// This method is probably not necessary and does not take
		// into account continuous variables. I think the getPossibleEvents
		// replaces this.
		
		//*ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();
		ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();
		
		
		// Check if the timer exceeds its lower bound staring with the first nonzero
		// timer.
		//*for(int i=1; i<_indexToTimerPair.length; i++)
		//*{
		for (int i=0; i<_dbmVarList.length; i++){
		
			// The upper bound is in the v+ - v- entry. A timer is able to fire
			// if the upper bound is greater than or equal to the lower bound.
			
			//*if(getDbmEntry(0, i) >= -1 * getLowerBoundbydbmIndex(i))
			//*{
			if(_matrix[baseToNeg(i)][baseToPos(i)] >= -2*_lowerBounds[i]){
			
				//*enabledTransitions.add(_lpnList[_indexToTimerPair[i].get_lpnIndex()]
						//*.getTransition(_indexToTimerPair[i].get_transitionIndex()));
				
				enabledTransitions.add(_lpnList[_dbmVarList[i].get_lpnIndex()]
						.getTransition(_dbmVarList[i].get_transitionIndex()));
			//*}
			}
		//*}
		}
		
		//*return enabledTransitions;
		
		
		return enabledTransitions;
	}
		
	/**
	 * Gives the list of enabled transitions associated with a particular LPN.
	 * @param LpnIndex
	 * 			The Index of the LPN the Transitions are a part of.
	 * @return
	 * 			A List of the Transitions that are enabled in the LPN given by the index.
	 */
	@Override
	public List<Transition> getEnabledTransitions(int LpnIndex){
		
		// This method is probably not necessary and does not take
		// into account continuous variables. The get possible events
		// I think replaces this.
		
		//*ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();
		ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();

		// Check if the timer exceeds its lower bound staring with the first nonzero
		// timer.
		//*for(int i=1; i<_indexToTimerPair.length; i++)
		//*{
		for(int i=0; i<_dbmVarList.length; i++){
			
			//*if(getDbmEntry(0, i) >= -1 * getLowerBoundbydbmIndex(i))
			//*{
			if(_matrix[baseToNeg(i)][baseToPos(i)] >= -2*_lowerBounds[i]){
			
				//*LPNTransitionPair ltPair = _indexToTimerPair[i];
				LPNTransitionPair ltPair = _dbmVarList[i];
				
				//*if( ltPair.get_lpnIndex() == LpnIndex){
				if(ltPair.get_lpnIndex() == LpnIndex){

					//*enabledTransitions.add(_lpnList[ltPair.get_lpnIndex()]
							//*.getTransition(ltPair.get_transitionIndex()));
					
					enabledTransitions.add(_lpnList[ltPair.get_lpnIndex()]
							.getTransition(ltPair.get_transitionIndex()));
					
				//*}
				}
			//*}
			}
		//*}
		}
		
		//*return enabledTransitions;
		
		return enabledTransitions;
	}
	
	/**
	 * Find the next possible events.
	 * 
	 * @param LpnIndex
	 * 		The index of the LPN that is of interest.
	 * @param localState
	 * 		The state associated with the LPN indexed by LpnIndex.
	 * @return
	 * 		LpnTranList is populated with a list of 
	 * 		EventSets pertaining to the LPN with index LpnIndex. An EventSet can
	 * 		either contain a transition to
	 * 		fire or set of inequalities to change sign. 
	 */
	@Override
	public LpnTranList getPossibleEvents(int LpnIndex, State localState){
		
		
		//*LpnTranList result = new LpnTranList();
		LpnTranList result = new LpnTranList();
		
		// Look through the timers and continuous variables. For the timers
		// determine if they are ready to fire. For the continuous variables,
		// look up the associated inequalities and see if any of them are ready
		// to fire.

		
		// We do not need to consider the zero timer, so start the 
		// for loop at i=1 and not i=0.
		//*for(int i=1; i<_indexToTimerPair.length; i++){
		
		for(int i=0; i<_dbmVarList.length; i++){
		
			//*LPNTransitionPair ltPair = _indexToTimerPair[i];
			LPNTransitionPair ltPair = _dbmVarList[i];
			
			
			// The enabled events are grouped with the LPN that they affect. So if 
			// this pair does not belong to the current LPN under consideration, skip
			// processing it.
			//*if(ltPair.get_lpnIndex() != LpnIndex){
				//*continue;
			//*}
			if(ltPair.get_lpnIndex() != LpnIndex){
				continue;
			}
			
			
			// If the index refers to a timer (and not a continuous variable)
			// and has exceeded its lower bound, then add the transition.
			//*if(!(ltPair instanceof LPNContinuousPair)){
			if(!(ltPair instanceof LPNContinuousPair)){
			
				
				// The index refers to a timer. Now check if time has advanced
				// far enough for the transition to fire.
				//*if(getDbmEntry(0, i) >= -1 * getLowerBoundbydbmIndex(i)){
				
				// Note the upper bound is the v+-v- entry.
				if(_matrix[baseToNeg(i)][baseToPos(i)]
						>= -2*_lowerBounds[i]){
					//*Event e = new Event(_lpnList[ltPair.get_lpnIndex()].getTransition(ltPair.get_transitionIndex()));
					//*result = addSetItem(result, e, localState);
					
					Event e = new Event(_lpnList[ltPair.get_lpnIndex()]
							.getTransition(ltPair.get_transitionIndex()));
					result = Zone.addSetItem(this, result, e, localState);
				//*}
				}
			//*}
			}
			//*else{
			else{
				
				// The index refers to a continuous variable.
				// First check for a rate change event.
				//*LPNContinuousPair ltContPair = 
						//*((LPNContinuousPair) ltPair).clone();
				LPNContinuousPair ltContPair =
						((LPNContinuousPair) ltPair).clone();
				
				//*IntervalPair ratePair = getRateBounds(ltContPair);
				IntervalPair ratePair = getRateBounds(ltContPair);
				
				//*result = createRateEvents(ltContPair, ratePair, result, localState);
				result = Zone.createRateEvents(this, ltContPair, ratePair, result, localState);
				
				
				// Check all the inequalities for inclusion.
				//*Variable contVar = _lpnList[ltPair.get_lpnIndex()]
						//*.getContVar(ltPair.get_transitionIndex());
				Variable contVar = _lpnList[ltPair.get_lpnIndex()]
						.getContVar(ltPair.get_transitionIndex());
				
				
				//*if(contVar.getInequalities() != null){
				if(contVar.getInequalities() != null){
				
					//*for(InequalityVariable iv : contVar.getInequalities()){
					for(InequalityVariable iv : contVar.getInequalities()){

						// Check if the inequality can change.
						//*if(ContinuousUtilities.inequalityCanChange(this, iv, localState)){
							//*result = addSetItem(result, new Event(iv), localState);
						//*}
						
						
						if(ContinuousUtilities.inequalityCanChange(this, iv, localState)){
							result = Zone.addSetItem(this, result, new Event(iv), localState);
						}
						
					//*}
					}
				//*}
				}
			}
			//*}
		//*}
		}
		
		// Check the rate zero variables for possible rate change events.
		//*for(LPNContAndRate lcrPair : _rateZeroContinuous.keySet()){
		for(LPNContAndRate lcrPair : _rateZeroContinuous.keySet()){
		
			// Get the reference object:
			//*LPNContinuousPair ltContPair = lcrPair.get_lcPair();
			LPNContinuousPair ltContPair = lcrPair.get_lcPair();
			
			// Extract the range of rates.
			//*IntervalPair ratePair = lcrPair.get_rateInterval();
			IntervalPair ratePair = lcrPair.get_rateInterval();

			//*result = createRateEvents(ltContPair, ratePair, result, localState);
			result = Zone.createRateEvents(this, ltContPair, ratePair, result, localState);
			
		//*}
		}
		//*return result;
	//*}
		return result;
	}
	
//	private LpnTranList createRateEvents(LPNContinuousPair ltContPair, IntervalPair ratePair,
//			LpnTranList result, State localState){
		
		// it is probably possible to make this method static as well
		// as the addSetItems method.
//		return null;
//	}
	
	/**
	 * Adds or removes items as appropriate to update the current
	 * list of possible events. Note the type LpnTranList extends
	 * LinkedList<Transition>. The type EventSet extends transition
	 * specifically so that objects of EventSet type can be place in
	 * this list.
	 * @param EventList
	 * 			The list of possible events.
	 */
//	public LpnTranList addSetItem(LpnTranList E, Event e, State s){
		
		
		// It is probably not necessary to repeat this method
		// unless it actually refers to the zone in the zones
		// version. If that is not the case, we can make it
		// a static method and use only the one.
//		return null;
//	}
	
	/**
	 * Warps this Octagon with the aid of rate information from the previous Octagon. 
	 * 
	 * @param oldOctagon
	 * 		The previous Octagon.
	 * @return
	 * 		The warped Octagon.
	 */
	public void dbmWarpOld(Octagon oldOctagon){
		
		// According to atacs comments, this appears to NOT work when
		// INFIN is in the bounds.
		// This portion of the code handles the warping of the relative
		// parts of the octagon.
		//*for(int i=1; i< dbmSize(); i++){
		for(int i=1; i<DBMsize(); i++){
			//*for(int j=i+1; j<dbmSize(); j++){
			for(int j=i+1; j<DBMsize(); j++){
				//*double iVal, jVal, iWarp, jWarp, iXDot, jXDot;
				double iVal, jVal, iWarp, jWarp;// iXDot, jXDot;
				
				// Note : the iVal and the jVal correspond to the 
				// alpha and beta describe in Scott Little's thesis.
				
		
				// Do some warping when dealing with the continuous variables.
				//*if(_indexToTimerPair[i] instanceof LPNContinuousPair){
				if(_dbmVarList[i] instanceof LPNContinuousPair){
				
					// Calcualte the alpha value.
					//*iVal = Math.floor(Math.abs(
							//*(double) oldZone.getCurrentRate(_indexToTimerPair[i]) /
							//*(double) this.getCurrentRate(_indexToTimerPair[i])));
					
					iVal = Math.floor(Math.abs(
							(double) oldOctagon.getCurrentRate(i) /
							(double) this.getCurrentRate(i)));
					
					// The old rate the octagon was warped by.
					//*iWarp = Math.floor(Math.abs(
							//*(double) oldZone.getCurrentRate(_indexToTimerPair[i])));
					iWarp = Math.floor(Math.abs(
							(double) oldOctagon.getCurrentRate(i)));
					
					
					// The current rate rate of this continuous variable.
					//*iXDot = Math.floor(Math.abs(
							//*(double) this.getCurrentRate(_indexToTimerPair[i])));
//					iXDot = Math.floor(Math.abs(
//							(double) this.getCurrentRate(i)));
					
					
					// I'm not going to do any warping when the previous rate
					// is zero. This statement is a break to go to next i value
					// and not the next j.
					//*if(iWarp == 0){
						//*break;
					//*}
					if(iWarp == 0){
						break;
					}
					
				//*}
				}
				
				//*else{
				else{
					// The current variable is a timer, so the new rate and old rate
					// are both 1. Hence we have
					//*iVal = 1.0;
					//*iWarp = 1.0;
					//*iXDot = 1.0;
					iVal = 1.0;
					iWarp = 1.0;
//					iXDot = 1.0;
					
				//*}
				}
				
				// Do some warping of the second variable if it is a continuous variable.
				//*if(_indexToTimerPair[j] instanceof LPNContinuousPair){
				if(_dbmVarList[j] instanceof LPNContinuousPair){
				
					// Calcualte the alpha value.
					//*jVal = Math.floor(Math.abs(
							//*(double) oldZone.getCurrentRate(_indexToTimerPair[j]) /
							//*(double) this.getCurrentRate(_indexToTimerPair[j])));
					jVal = Math.floor(Math.abs(
							(double) oldOctagon.getCurrentRate(j)/
							(double) this.getCurrentRate(j)));
					
					
					// The old rate the zone was warped by.
					//*jWarp = Math.floor(Math.abs(
							//*(double) oldZone.getCurrentRate(_indexToTimerPair[j])));
					jWarp = Math.floor(Math.abs(
							(double) oldOctagon.getCurrentRate(j)));
					
					
					// The current rate of this continuous variable.
					//*jXDot = Math.floor(Math.abs(
							//*(double) this.getCurrentRate(_indexToTimerPair[j])));
//					jXDot = Math.floor(Math.abs(
//							(double) this.getCurrentRate(j)));
							
					// I'm not going to do any warping when the previous rate is
					// zero.
					//*if(jWarp == 0){
						//*continue;
					//*}
					if(jWarp == 0){
						continue;
					}
				}
				//*}
				
				//*else{
				else{
					// The current variable is a timer, so the new rate and old rate
					// are both 1. Hence we have
					//*jVal = 1.0;
					//*jWarp = 1.0;
					//*jXDot = 1.0;
					jVal = 1.0;
					jWarp = 1.0;
//					jXDot = 1.0;
				//*}
				}
				
				// The Octagon is warped differently depending on which of rate is
				// larger. See Andrew Fisher's Thesis for more details.
				//*if(iVal > jVal){
				if(iVal > jVal){
					
				
					//*setDbmEntry(j, i, (int)
							//*Math.ceil(((jWarp*getDbmEntry(j, i))/jXDot) +
							//*((-1*jWarp*getDbmEntry(0, i)/jXDot)) +
							//*((iWarp*getDbmEntry(0, i)/iXDot))));
					
					//*setDbmEntry(i, j, (int)
							//*Math.ceil(((jWarp*getDbmEntry(i, j))/jXDot) +
							//*((-1*jWarp*getDbmEntry(i, 0)/jXDot)) +
							//*((iWarp*getDbmEntry(i, 0)/iXDot))));
					
//					_matrix[i][j] =
//							Math.ceil()
					
					
					
				//*}
				}
				
				//*else{
				else{
					//*setDbmEntry(i, j, (int)
							//*Math.ceil(((iWarp*getDbmEntry(i, j))/iXDot) +
							//*((-1*iWarp*getDbmEntry(0, j)/iXDot)) +
							//*((jWarp*getDbmEntry(0, j)/jXDot))));
					
					//*setDbmEntry(j, i, (int)
							//*Math.ceil(((iWarp*getDbmEntry(j, i))/iXDot) +
							//*((-1*iWarp*getDbmEntry(j, 0)/iXDot)) +
							//*((jWarp*getDbmEntry(j, 0)/jXDot))));
				//*}
				}
			//*}
			}
		//*}
		}
		// Handle the warping of the bounds.
		//*for(int i=1; i<dbmSize(); i++){
			//*if(_indexToTimerPair[i] instanceof LPNContinuousPair){
						
				//*if(Math.abs(getDbmEntry(i, 0)) != INFINITY ){
					
					//*if(oldZone.getCurrentRate(_indexToTimerPair[i]) == 0){
						// If the older rate was zero, then we just need to 
						// divide by the new rate.
						//*setDbmEntry(i, 0, ContinuousUtilities.chkDiv(
								//*getDbmEntry(i,0),
								//*Math.abs(getCurrentRate(_indexToTimerPair[i])),
								//*true));
					//*}
					//*else{
						// Undo the old warping and introduce the new warping.
						// If the bound is infinite, then division does nothing.
						//*setDbmEntry(i, 0, ContinuousUtilities.chkDiv(
								//*Math.abs(oldZone.getCurrentRate(_indexToTimerPair[i]))
								//* * getDbmEntry(i, 0),
								//*Math.abs(getCurrentRate(_indexToTimerPair[i])), 
								//*true));
					//*}
				//*}
								
				//*if(Math.abs(getDbmEntry(0, i)) != INFINITY){
					
					//*if(oldZone.getCurrentRate(_indexToTimerPair[i]) == 0){
						//*setDbmEntry(0, i, ContinuousUtilities.chkDiv(
								//*getDbmEntry(0,i),
								//*Math.abs(getCurrentRate(_indexToTimerPair[i])),
								//*true));
					//*}
					//*else{
						// Undo the old warping and introduce the new warping.
						// If the bound is inifite, then division does nothing.
						//*setDbmEntry(0, i, ContinuousUtilities.chkDiv(
								//*Math.abs(oldZone.getCurrentRate(_indexToTimerPair[i]))
								//* * getDbmEntry(0, i),
								//*Math.abs(getCurrentRate(_indexToTimerPair[i])), 
								//*true));
					//*}
				//*}
			//*}
		//*}
			
					
		//*for(int i=1; i<dbmSize(); i++){
			//*if(_indexToTimerPair[i] instanceof LPNContinuousPair){
		
				// Handle the case when the warping takes us into negative space.
				//*if((double) oldZone.getCurrentRate(_indexToTimerPair[i])/
						//*(double) this.getCurrentRate(_indexToTimerPair[i]) < 0.0){
					/* We are warping into the negative space, so swap the upper and 
					 * lower bounds.
					 */
					//*int temp = getDbmEntry(i, 0);
					//*setDbmEntry(i,0, getDbmEntry(0, i));
					//*setDbmEntry(0, i, temp);


					// Set the relationships to Infinity since nothing else is known.
					//*for(int j=1; j<dbmSize(); j++){
						//*if(i != j){
							//*setDbmEntry(i, j, INFINITY);
							//*setDbmEntry(j, i, INFINITY);
						//*}
					//*}
				//*}
			//*}
		//*}
	}
	
	/**
	 * Handles the modification to the octagon due to the negative rates.
	 * @param oldOctagon
	 */
	private void negativeWarp(Octagon oldOctagon){
		
		for(int i=0; i<this._dbmVarList.length; i++){
			
//			for(int j=i+1; j<this._dbmVarList.length; j++){

				// Let i be 'x' and j be 'y'.
				double yold, xold, signx, signy;
				
				// Define the alpha, ynew, and yold.
				if(_dbmVarList[i] instanceof LPNContinuousPair){
//					(double) oldOctagon.getCurrentRate(this._dbmVarList[i])					
					
					
					xold = Math.abs((double) oldOctagon.getCurrentRate(this._dbmVarList[i]));
					
					signx = (double) oldOctagon.getCurrentRate(this._dbmVarList[i])/
							(double) this.getCurrentRate(i);
				
					// I'm not going to do any warping when the previous rate
					// is zero. This statement is a break to go to next i value
					// and not the next j.
					if(xold == 0){
						break;
					}
				}
				
				else{
					xold = 1.0;
					signx = 1.0;
				}
				
				// Swap the bound if they need to be swapped.
				if(signx < 0){
					int tmp = this._matrix[baseToNeg(i)][baseToPos(i)];
					this._matrix[baseToNeg(i)][baseToPos(i)] =
							this._matrix[baseToPos(i)][baseToNeg(i)];
					this._matrix[baseToPos(i)][baseToNeg(i)] = tmp;
				}
				
				for(int j=i+1; j<this._dbmVarList.length; j++){
				
				// Assign the beta, xnew, and xold.
				if(_dbmVarList[j] instanceof LPNContinuousPair){
				
					yold = Math.floor(Math.abs(
							(double) oldOctagon.getCurrentRate(this._dbmVarList[j])));
					
					signy = (double) oldOctagon.getCurrentRate(this._dbmVarList[j])/
							(double) this.getCurrentRate(j);
					
					// I'm not going to do any warping when the previous rate is
					// zero.
					if(yold == 0){
						continue;
					}
				}
				else{
					yold = 1.0;
					signy = 1.0;
				}
				
				// When the sign of the old rates are different than the sign
				// of the new rates, then the upper and lower bounds swap.
				// In addition, depending on whether both y and x change or
				// just one of them, the internal relationships change.
				if((signx < 0) & (signy > 0)){
					// Perform the swaps of the intercepts.
					// b1 -> b3, b2 -> b4, b3 -> b1, b4 -> b2
					// However, the dbm stores, b1, -b2, b3, and -b4.
					// So the b1 and the b3 entries are swapped directly,
					// Simlarly, the -b2 and the -b4 entries are swapped
					// directly.
					
					// Swap the bounds.
//					int tmp = this._matrix[baseToNeg(i)][baseToPos(i)];
//					this._matrix[baseToNeg(i)][baseToPos(i)] =
//							-1*this._matrix[baseToPos(i)][baseToNeg(i)];
//					this._matrix[baseToPos(i)][baseToNeg(i)] = -1 * tmp;
//					this._matrix[baseToNeg(i)][baseToPos(i)] =
//							this._matrix[baseToPos(i)][baseToNeg(i)];
//					this._matrix[baseToPos(i)][baseToNeg(i)] = tmp;
					
					// Save the b1 entry.
					int tmp = this._matrix[baseToPos(i)][baseToPos(j)];
					
					// Write over the b1 entry with b3.
					this._matrix[baseToPos(i)][baseToPos(j)] =
							this._matrix[baseToNeg(i)][baseToPos(j)];
					this._matrix[baseToNeg(j)][baseToNeg(i)] =
							this._matrix[baseToNeg(i)][baseToPos(j)];
					
					// Write over the b3 entry with b1.
					this._matrix[baseToNeg(i)][baseToPos(j)] = tmp;
					this._matrix[baseToNeg(j)][baseToPos(i)] = tmp;
					
					// Save the -b2 entry.
					tmp = this._matrix[baseToNeg(i)][baseToNeg(j)];
					
					// Write over the -b2 entry with -b4.
					this._matrix[baseToNeg(i)][baseToNeg(j)] =
							this._matrix[baseToPos(i)][baseToNeg(j)];
					this._matrix[baseToPos(j)][baseToPos(i)] =
							this._matrix[baseToPos(i)][baseToNeg(j)];
					
					// Write over the -b4 entry with -b2.
					this._matrix[baseToPos(i)][baseToNeg(j)] = tmp;
					this._matrix[baseToPos(j)][baseToNeg(i)] = tmp;
					
				}
				else if((signx > 0) & (signy < 0)){
					// When y is negative, b1 and b4 are swapped as
					// well as b2 and b3 are swapped (as well as the sign changes).
					// When x is negative, b1 and b3 are swapped as well as b2 and
					// b4 are swapped.
					
					
					// Since x is positive, do not swap the bounds.
//					int tmp = 0;
//					int tmp = this._matrix[baseToNeg(i)][baseToPos(i)];
//					this._matrix[baseToNeg(i)][baseToPos(i)] =
//							-1*this._matrix[baseToPos(i)][baseToNeg(i)];
//					this._matrix[baseToPos(i)][baseToNeg(i)] = -1 * tmp;
					
					// Perform the swaps of the intercepts.
					// b1 -> -b4, b4 -> -b1, b3 -> -b2, b2 -> -b3
					// However, the dbm stores b1, -b2, b3, and -b4.
					// So the b1 and -b4 entries are swapped directly.
					// Simlarly, b3 and the -b2 entry are swapped
					// directly.
					// Save the b1 value.
					int tmp = this._matrix[baseToPos(i)][baseToPos(j)];
					
					// Write over the b1 values with -b4.
					this._matrix[baseToPos(i)][baseToPos(j)] =
							this._matrix[baseToPos(i)][baseToNeg(j)];
					this._matrix[baseToNeg(j)][baseToNeg(i)] =
							this._matrix[baseToPos(j)][baseToNeg(i)];
					
					// Write over the -b4 values with b1.
					this._matrix[baseToPos(i)][baseToNeg(j)] = tmp;
					this._matrix[baseToPos(j)][baseToNeg(i)] = tmp;
					
					// Save the b3 entry.
					tmp = this._matrix[baseToNeg(i)][baseToPos(j)];
					
					// Write over the b3 values with -b2.
					this._matrix[baseToNeg(i)][baseToPos(j)] =
							this._matrix[baseToNeg(i)][baseToNeg(j)];
					this._matrix[baseToNeg(j)][baseToPos(i)] =
							this._matrix[baseToPos(j)][baseToPos(i)];
					
					// Write over the -b2 values with b3.
					this._matrix[baseToNeg(i)][baseToNeg(j)] = tmp;
					this._matrix[baseToPos(j)][baseToPos(i)] = tmp;
				}
				else if((signx < 0) & (signy < 0)){
					// Perform the swaps of the intercepts.
					// b1 -> -b2, b2 -> -b1, b3 -> -b4, b4 -> -b3
					// However, the dbm stores, b1, -b2, b3, and -b4.
					// So the b1 and the -b2 entries are swapped directly,
					// Simlarly, the b3 and the -b4 entries are swapped
					// directly.
					
					// Swap the bounds.
//					int tmp = this._matrix[baseToNeg(i)][baseToPos(i)];
//					this._matrix[baseToNeg(i)][baseToPos(i)] =
//							-1*this._matrix[baseToPos(i)][baseToNeg(i)];
//					this._matrix[baseToPos(i)][baseToNeg(i)] = -1 * tmp;
//					this._matrix[baseToNeg(i)][baseToPos(i)] =
//							this._matrix[baseToPos(i)][baseToNeg(i)];
//					this._matrix[baseToPos(i)][baseToNeg(i)] = tmp;
					
					// Save the b1 entry.
					int tmp = this._matrix[baseToPos(i)][baseToPos(j)];
					
					// Write over the b1 entry with -b2.
					this._matrix[baseToPos(i)][baseToPos(j)] =
							this._matrix[baseToNeg(i)][baseToNeg(j)];
					this._matrix[baseToNeg(j)][baseToNeg(i)] =
							this._matrix[baseToNeg(i)][baseToNeg(j)];
					
					// Write over the -b2 entry with b1.
					this._matrix[baseToNeg(i)][baseToNeg(j)] = tmp;
					this._matrix[baseToPos(j)][baseToPos(i)] = tmp;
					
					// Save the b3 entry.
					tmp = this._matrix[baseToNeg(i)][baseToPos(j)];
					
					// Write over the b3 entry with -b4.
					this._matrix[baseToNeg(i)][baseToPos(j)] =
							this._matrix[baseToPos(i)][baseToNeg(j)];
					this._matrix[baseToNeg(i)][baseToPos(j)] =
							this._matrix[baseToPos(i)][baseToNeg(j)];
					
					// Write over the -b4 entry with b3.
					this._matrix[baseToPos(i)][baseToNeg(j)] = tmp;
					this._matrix[baseToPos(j)][baseToNeg(i)] = tmp;
				}
			}
		}
	}
	
	/**
	 * Warps this Octagon with the aid of rate information from the previous Octagon. 
	 * 
	 * @param oldOctagon
	 * 		The previous Octagon.
	 * @return
	 * 		The warped Octagon.
	 */
	//*public void dbmWarp(Octagon oldOctagon){
	@Override
	public void dbmWarp(Equivalence oldE){
		
		Octagon oldOctagon = (Octagon) oldE;
		
//		oldOctagon.negativeWarp(oldOctagon);
		this.negativeWarp(oldOctagon);
		
//		for(int i=1; i<DBMsize(); i++){
		for(int i=0; i<this._dbmVarList.length; i++){
			
//			for(int j=i+1; j<DBMsize(); j++){
			for(int j=i+1; j<this._dbmVarList.length; j++){

				// Let i be 'x' and j be 'y'.
				double alpha, beta, ynew, yold, xnew, xold;
				
				// Define the alpha, ynew, and yold.
				if(_dbmVarList[i] instanceof LPNContinuousPair){
					
					alpha = Math.floor(Math.abs(
							(double) oldOctagon.getCurrentRate(this._dbmVarList[i]) /
							(double) this.getCurrentRate(i)));
					
					xold = Math.abs((double) oldOctagon.getCurrentRate(this._dbmVarList[i]));
					
					xnew = Math.abs((double) this.getCurrentRate(i));
					
					
//					if (xold == 0){
//						signx = (double) this.getCurrentRate(i) /
//								(double) this.getCurrentRate(i);
//					}
//					else{
//					}
					
					
					// I'm not going to do any warping when the previous rate
					// is zero. This statement is a break to go to next i value
					// and not the next j.
					if(xold == 0){
						break;
					}
				}
				
				else{

					alpha = 1.0;
					xold = 1.0;
					xnew = 1.0;
				}
				
				// Assign the beta, xnew, and xold.
				//*if(_indexToTimerPair[j] instanceof LPNContinuousPair){
				if(_dbmVarList[j] instanceof LPNContinuousPair){
				
					beta = Math.floor(Math.abs(
							(double) oldOctagon.getCurrentRate(this._dbmVarList[j])/
							(double) this.getCurrentRate(j)));
					

					yold = Math.floor(Math.abs(
							(double) oldOctagon.getCurrentRate(this._dbmVarList[j])));
					

					ynew = Math.floor(Math.abs(
							(double) this.getCurrentRate(j)));
					
					
//					if (yold == 0){
//						signy = (double) this.getCurrentRate(i) /
//								(double) this.getCurrentRate(i);
//					}
//					else{
//					}
					
					// I'm not going to do any warping when the previous rate is
					// zero.
					if(yold == 0){
						continue;
					}
				}
				else{
					beta = 1.0;
					yold = 1.0;
					ynew = 1.0;
				}
				
				
				// Next is the warping.
				
				// The warping is different depending on whether
				// beta >= alpha or not.
				//if(beta >= alpha){
				if(yold*xnew >= xold*ynew){
					// This is the warping when the 'y' is greater
					// than the x.
					
					// Get the bounds.
					
					// Get the values out of the matrix.
					int My =(int) Math.ceil(
							this._matrix[baseToNeg(j)][baseToPos(j)]/2);
					int my = (int)((-1)*
							Math.ceil(
							this._matrix[baseToPos(j)][baseToNeg(j)]/2));
					
//					int My =(int) Math.ceil(
//							oldOctagon._matrix[baseToNeg(j)][baseToPos(j)]/2);
//					int my = (int)((-1)*
//							Math.ceil(
//							oldOctagon._matrix[baseToPos(j)][baseToNeg(j)]/2));
					
					int b1 = this._matrix[baseToPos(i)][baseToPos(j)];
					int b2 = (-1)*this._matrix[baseToNeg(i)][baseToNeg(j)];
					int b3 = this._matrix[baseToNeg(i)][baseToPos(j)];
					int b4 = (-1)*this._matrix[baseToPos(i)][baseToNeg(j)];
					
//					int b1new = (int) Math.ceil((beta - alpha)*My + alpha*b1);
//					int negb2new = (int) Math.ceil((-1)*((beta - alpha)*my + alpha*b2));
//					int b3new = (int) Math.ceil((beta-alpha)*My + alpha*b3);
//					int negb4new = (int) Math.ceil((-1)*((beta - alpha)*my + alpha*b4));

					int b1new = (int) Math.ceil((yold/ynew - xold/xnew)*My + xold*b1/xnew);
					
					// The floor since b2 is a lower bound.
					int b2new = (int) Math.floor((yold/ynew - xold/xnew)*my + xold*b2/xnew);
					int b3new = (int) Math.ceil((yold/ynew-xold/xnew)*My + xold*b3/xnew);
					
					// The floor since b4 is a lower bound.
					int b4new = (int) Math.floor((yold/ynew - xold/xnew)*my + xold*b4/xnew);
					
					
					// b1
					this._matrix[baseToPos(i)][baseToPos(j)] = b1new;
					
					this._matrix[baseToNeg(j)][baseToNeg(i)] = b1new;
					
					// -b2
					this._matrix[baseToNeg(i)][baseToNeg(j)] = -1*b2new;
					
					this._matrix[baseToPos(j)][baseToPos(i)] = -1*b2new;
					
					// b3
					this._matrix[baseToNeg(i)][baseToPos(j)] = b3new;
					
					this._matrix[baseToNeg(j)][baseToPos(i)] = b3new;
					
					// -b4
					this._matrix[baseToPos(i)][baseToNeg(j)] = -1*b4new;

					this._matrix[baseToPos(j)][baseToNeg(i)] = -1*b4new;

				}
				else{
					// This is the warping when the 'x' is greater
					// than the y.
					
					// Get the bounds.
					
					int Mx =(int) Math.ceil(
							this._matrix[baseToNeg(i)][baseToPos(i)]/2);
					int mx = (int)((-1)*
							Math.ceil(
							this._matrix[baseToPos(i)][baseToNeg(i)]/2));
					
//					int Mx =(int) Math.ceil(
//							oldOctagon._matrix[baseToNeg(i)][baseToPos(i)]/2);
//					int mx = (int)((-1)*
//							Math.ceil(
//							oldOctagon._matrix[baseToPos(i)][baseToNeg(i)]/2));
					
					
					int b1 = this._matrix[baseToPos(i)][baseToPos(j)];
					int b2 = (-1)*this._matrix[baseToNeg(i)][baseToNeg(j)];
					int b3 = this._matrix[baseToNeg(i)][baseToPos(j)];
					int b4 = (-1)*this._matrix[baseToPos(i)][baseToNeg(j)];
					
//					int b1new = (int) Math.ceil((beta - alpha)*mx + beta*b1);
//					int negb2new = (int) Math.ceil((-1)*((beta - alpha)*Mx + beta*b2));
//					int b3new = (int) Math.ceil((alpha - beta)*Mx + beta*b3);
//					int negb4new = (int) Math.ceil((-1)*((alpha - beta)*mx + beta*b4));
					
					int b1new = (int) Math.ceil((yold/ynew - xold/xnew)*mx + yold/ynew*b1);
					
					// The floor since b2 is a lower bound.
					int b2new = (int) Math.floor((yold/ynew - xold/xnew)*Mx + yold/ynew*b2);
					int b3new = (int) Math.ceil((xold/xnew - yold/ynew)*Mx + yold/ynew*b3);
					
					// The floor since b2 is a lower bound.
					int b4new = (int) Math.floor((xold/xnew - yold/ynew)*mx + yold/ynew*b4);
					
					// b1
					this._matrix[baseToPos(i)][baseToPos(j)] = b1new;
					
					this._matrix[baseToNeg(j)][baseToNeg(i)] = b1new;
					
					// -b2
					this._matrix[baseToNeg(i)][baseToNeg(j)] = -1*b2new;
							
					this._matrix[baseToPos(j)][baseToPos(i)] = -1*b2new;
					
					// b3
					this._matrix[baseToNeg(i)][baseToPos(j)] = b3new;

					this._matrix[baseToNeg(j)][baseToPos(i)] = b3new;

					
					// -b4
					this._matrix[baseToPos(i)][baseToNeg(j)] = -1*b4new;

					this._matrix[baseToPos(j)][baseToNeg(i)] = -1*b4new;
				}
			}
		}
		
		// Warp the upper and lower bounds for x.
		for(int i=0; i<this._dbmVarList.length; i++){
			if(this._dbmVarList[i] instanceof LPNContinuousPair){
				

				if((this._dbmVarList[i] instanceof  LPNContinuousPair)
						&& Math.abs(this.getUpperBound(i)) != Zone.INFINITY){


					if(oldOctagon.getCurrentRate(this._dbmVarList[i]) == 0){
						// If the older rate was zero, then we just need to
						// divide by the new rate. Note: as long as the
						// rate stored in the octagon is already twice the
						// max, there is no need to divide by two before
						// doing the warping.
						this._matrix[baseToNeg(i)][baseToPos(i)] =
								ContinuousUtilities.chkDiv(
										this._matrix[baseToNeg(i)][baseToPos(i)],
										Math.abs(getCurrentRate(i)),
										true);
					}
					else{
						// Undo the old warping and introduce the new warping.
						// If the bound is infinite, then division does nothing.
//						this._matrix[baseToNeg(i)][baseToPos(i)] =
//								2*ContinuousUtilities.chkDiv(
//										Math.abs(oldOctagon.getCurrentRate(i))
//										*this._matrix[baseToNeg(i)][baseToPos(i)],
//										2*Math.abs(getCurrentRate(i)),
//										true);
						this._matrix[baseToNeg(i)][baseToPos(i)] =
								2*ContinuousUtilities.chkDiv(
										Math.abs(oldOctagon.getCurrentRate(this._dbmVarList[i]))
										*this._matrix[baseToNeg(i)][baseToPos(i)],
										2*Math.abs(getCurrentRate(i)),
										true);


					}

				}
				//*if(Math.abs(getDbmEntry(0, i)) != INFINITY){
				if((this._dbmVarList[i] instanceof LPNContinuousPair)
						&& Math.abs(this.getLowerBound(i)) != Zone.INFINITY){

					if(oldOctagon.getCurrentRate(this._dbmVarList[i]) == 0){
						// If the old rate is zero, then only need to warp
						// for the current rate.
						this._matrix[baseToPos(i)][baseToNeg(i)] =
								2*ContinuousUtilities.chkDiv(
										this._matrix[baseToPos(i)][baseToNeg(i)],
										2*Math.abs(this.getCurrentRate(i)),
										true);
					}
					else{
						// Undo the old warping and introduce the new warping.
						// If the bound is infinite, then division does nothing.
						this._matrix[baseToPos(i)][baseToNeg(i)] =
								ContinuousUtilities.chkDiv(
										Math.abs(oldOctagon.getCurrentRate(this._dbmVarList[i]))
										*this._matrix[baseToPos(i)][baseToNeg(i)],
										Math.abs(getCurrentRate(i)),
										true);

					}
				}
			}
		}
	}
	
	/**
	 * Get the list of LhpnFile objects that this Octagon depends on.
	 * @return
	 * 		The list of LhpnFile objects that this Octagon depends on.
	 */
	@Override
	public LPN[] get_lpnList(){
		return _lpnList;
	}
	
	/**
	 * Returns the current rate of the variable.
	 * @param contVar
	 * 		The LPNTransitionPair referring to a continuous variable.
	 * @return
	 * 		The current rate of the continuous variable referenced by the LPNTransitionPair.
	 * @throws ClassCastException
	 * 		If the LPNTransitionPair is not an instance of an LPNContinuousPair.
	 */
	@Override
	public int getCurrentRate(LPNTransitionPair contVar){
		
		// Find  the index in the _dbmVarList.
		int index = Arrays.binarySearch(_dbmVarList, contVar);
		
		if(index <0){
			// The variable is not in the non-zero rate section. So return zero.
			return 0;
		}
		
		LPNContinuousPair lcPair = (LPNContinuousPair) _dbmVarList[index];
		
		
		return lcPair.getCurrentRate();
	}
	
	/**
	 * Sets the current rate for a continuous variable. It sets the rate regardless of 
	 * whether the variable is in the rate zero portion of the Zone or not. But it
	 * does not move variables in and out of the Octagon.
	 * @param contVar
	 * 		The index of the variable whose rate is going to be set.
	 * @param currentRate
	 * 		The value of the rate.
	 */
	@Override
	public void setCurrentRate(LPNTransitionPair contVar, int currentRate){
		
		//*if(!(contVar instanceof LPNContinuousPair)){
			// The LPNTransitionsPair does not refer to a continuous variable, so yell.
			//*throw new IllegalArgumentException("Zone.getCurrentRate was called" +
					//*" on an LPNTransitionPair that was not an LPNContinuousPair.");
		//*}
		if(!(contVar instanceof LPNContinuousPair)){
			//The LPNTransitionPair does not refere to a continuous variable, so yell.
			throw new IllegalArgumentException("Octagon.getCurrentRAte was called"
					+ " on an LPNTransitionPair that was not an LPNContinuousPair.");
		}
		
		//*LPNContinuousPair cV = (LPNContinuousPair) contVar;
		LPNContinuousPair cV = (LPNContinuousPair) contVar;
		
		
		// Check for the current variable in the rate zero variables.
		
		//*VariableRangePair variableRange = _rateZeroContinuous.
				//*getValue(new LPNContAndRate(cV, new IntervalPair(0,0)));
		VariableRangePair variableRange = _rateZeroContinuous
				.getValue(new LPNContAndRate(cV, new IntervalPair(0,0)));
		
		
		//*if(variableRange != null){
			//*LPNContinuousPair lcPair = _rateZeroContinuous.
					//*getKey(variableRange).get_lcPair();
			//*lcPair.setCurrentRate(currentRate);
			//*return;
		//*}
		if(variableRange != null){
			LPNContinuousPair lcPair = _rateZeroContinuous
					.getKey(variableRange).get_lcPair();
			lcPair.setCurrentRate(currentRate);
			return;
		}
		
		
		// Check for the current variable in the Zone variables.
		//*int index = Arrays.binarySearch(_indexToTimerPair, contVar);
		int index = Arrays.binarySearch(_dbmVarList, contVar);
		
		
		//*if(index >= 0){
			// The variable was found, set the rate.
			//*LPNContinuousPair lcPair = (LPNContinuousPair) _indexToTimerPair[index];
			//*lcPair.setCurrentRate(currentRate);
		//*}
		if(index >= 0){
			// The variable was found, set the rate.
			LPNContinuousPair lcPair = (LPNContinuousPair) _dbmVarList[index];
			lcPair.setCurrentRate(currentRate);
		}
		
	}
	
	/**
	 * Adds transitions to the Octagon.
	 * @param newTransitions
	 * 			The newly enabled transitions.
	 * @return
	 * 			The resulting Octagon after adding newTransitions
	 */
	@Override
	public Octagon addTransition(HashSet<LPNTransitionPair> newTransitions, State[] localStates){
		
		/*
		 * The Octagon will remain the same for all the continuous variables.
		 * The only thing that will change is a new transition will be added into the transitions.
		 */
		
		// Create a Zone to alter.
		//* Zone newZone = new Zone();
		// Create an Octagon to alter.
		Octagon newOct = new Octagon();
		
		// Create a copy of the LPN list.
		//*newZone._lpnList = Arrays.copyOf(this._lpnList, this._lpnList.length);
		newOct._lpnList = Arrays.copyOf(this._lpnList, this._lpnList.length);
		
		
		// Copy the rate zero continuous variables.
		//*newZone._rateZeroContinuous = this._rateZeroContinuous.clone();
		newOct._rateZeroContinuous = this._rateZeroContinuous.clone();
		
		// Create a copy of the current indexing pairs.
		//*newZone._indexToTimerPair = new LPNTransitionPair[_indexToTimerPair.length + newTransitions.size()];
		//*for(int i=0; i<_indexToTimerPair.length; i++){
			//*newZone._indexToTimerPair[i] = _indexToTimerPair[i];
		//*}
		newOct._dbmVarList = new LPNTransitionPair[_dbmVarList.length + newTransitions.size()];
		for (int i=0; i<_dbmVarList.length; i++){
			newOct._dbmVarList[i] = _dbmVarList[i];
		}
		
		
		// Add the new transitions to the _indexToTimerPair list.

		
		//*int i = _indexToTimerPair.length;
		//*for(LPNTransitionPair ltPair : newTransitions){
			//*newZone._indexToTimerPair[i++] = ltPair;
		//*}
		int i = _dbmVarList.length;
		for (LPNTransitionPair ltPair : newTransitions){
			newOct._dbmVarList[i++] = ltPair;
		}
		
		// Sort the _indexToTimerPair list.
		//*Arrays.sort(newZone._indexToTimerPair);
		Arrays.sort(newOct._dbmVarList);
		
		// Create matrix.
		//*newZone._matrix = new int[newZone._indexToTimerPair.length+1][newZone._indexToTimerPair.length+1];
		newOct._matrix = new int[newOct.DBMsize()][newOct.DBMsize()];
		
		// Create the upper and lower bound arrays.
		// Add the creation of the upper and lower bound arrays. Done
		newOct._lowerBounds = new int[newOct._dbmVarList.length];
		newOct._upperBounds = new int[newOct._dbmVarList.length];
		
		
		// Convert the current transitions to a collection of transitions.
		//*HashSet<LPNTransitionPair> oldTransitionSet = new HashSet<LPNTransitionPair>();
		//*for(LPNTransitionPair ltPair : _indexToTimerPair){
			//*oldTransitionSet.add(ltPair);
		//*}
		HashSet<LPNTransitionPair> oldTransitionSet = new HashSet<LPNTransitionPair>();
		for(LPNTransitionPair ltPair : _dbmVarList){
			oldTransitionSet.add(ltPair);
		}
		
		// Copy in the new transitions.
		//*newZone.copyTransitions(this, newTransitions, oldTransitionSet, localStates);
		newOct.copyTransitions(this, newTransitions, oldTransitionSet, localStates);
		
		//return newZone;
	//}
		
		return newOct;
	}
	
	/**
	 * Copies in the new values needed to add a set of new times.
	 * @param newZone
	 * 			The zone that the values are going to be copied into.
	 * @param tempZone
	 * 			The zone to look up current values of timers.
	 * @param newTimers
	 * 			A collection of the new timers.
	 * @param oldTimers
	 * 			A collection of the older timers.
	 * @param localStates
	 * 			The current state.
	 */
	private void copyTransitions(Octagon tmpOct, Collection<LPNTransitionPair> newTimers, 
			Collection<LPNTransitionPair> oldTimers, State[] localStates){

		
		// Copy the tempZone to the new zone.
		//*for(int i=0; i<tempZone.dbmSize(); i++)
		//*{
		// Copy the tmpOct to the new octagon.
//		for (int i=0; i<tmpOct.DBMsize(); i++){
		for (int i=0; i<tmpOct._dbmVarList.length; i++){
		
			//*if(!oldTimers.contains(tempZone._indexToTimerPair[i]))
			//*{
				//*continue;
			//*}
			if(!oldTimers.contains(tmpOct._dbmVarList[i])){
				continue;
			}

			// Get the new index of for the timer.
			//*int newIndexi = i==0 ? 0 : 
				//*Arrays.binarySearch(_indexToTimerPair, tempZone._indexToTimerPair[i]);
			int newBasei =
				Arrays.binarySearch(_dbmVarList, tmpOct._dbmVarList[i]);
			
			//*for(int j=0; j<tempZone.dbmSize(); j++)
			//*{
//			for (int j=0; j<tmpOct.DBMsize(); j++){
			for (int j=0; j<tmpOct._dbmVarList.length; j++){
			
				//*if(!oldTimers.contains(tempZone._indexToTimerPair[j]))
				//*{
					//*continue;
				//*}
				if(!oldTimers.contains(tmpOct._dbmVarList[j])){
					continue;
				}
				
				//*int newIndexj = j==0 ? 0 : 
					//*Arrays.binarySearch(_indexToTimerPair, tempZone._indexToTimerPair[j]);
				int newBasej =
					Arrays.binarySearch(_dbmVarList, tmpOct._dbmVarList[j]);
				
				//*_matrix[dbmIndexToMatrixIndex(newIndexi)]
						//*[dbmIndexToMatrixIndex(newIndexj)]
								//*= tempZone.getDbmEntry(i, j);
				_matrix[baseToNeg(newBasei)][baseToNeg(newBasej)] =
						tmpOct._matrix[baseToNeg(i)][baseToNeg(j)];
				_matrix[baseToNeg(newBasei)][baseToPos(newBasej)] =
						tmpOct._matrix[baseToNeg(i)][baseToPos(j)];
				_matrix[baseToPos(newBasei)][baseToNeg(newBasej)] =
						tmpOct._matrix[baseToPos(i)][baseToNeg(j)];
				_matrix[baseToPos(newBasei)][baseToPos(newBasej)] =
						tmpOct._matrix[baseToPos(i)][baseToPos(j)];
			//*}
			}
		//*}
		}

		// Copy the upper and lower bounds.
		//*for(int i=1; i<tempZone.dbmSize(); i++)
		//*{
//		for (int i=0; i<tmpOct.DBMsize(); i++){
		for (int i=0; i<tmpOct._dbmVarList.length; i++){
			//*if(!oldTimers.contains(tempZone._indexToTimerPair[i]))
			//*{
				//*continue;
			//*}
			if(!oldTimers.contains(tmpOct._dbmVarList[i])){
				continue;
			}
			
			//*setLowerBoundByLPNTransitionPair(tempZone._indexToTimerPair[i], 
					//*-1*tempZone.getLowerBoundbydbmIndex(i));
			// The minus sign is because _matrix stores the negative of the lower bound.
			//*setUpperBoundByLPNTransitionPair(tempZone._indexToTimerPair[i],
					//*tempZone.getUpperBoundbydbmIndex(i));
			
			int newBase = Arrays.binarySearch(_dbmVarList, tmpOct._dbmVarList[i]);
			_lowerBounds[newBase] = tmpOct._lowerBounds[i];
			_upperBounds[newBase] = tmpOct._upperBounds[i];
		}
		//*}

		// Copy in the new relations for the new timers.
		//*for(LPNTransitionPair timerNew : newTimers)
		//*{
		for(LPNTransitionPair timerNew : newTimers){
		
			//*for(LPNTransitionPair timerOld : oldTimers)
			//*{
			for(LPNTransitionPair timerOld : oldTimers){
			
				//*setDbmEntry(timerIndexToDBMIndex(timerNew),
						//*timerIndexToDBMIndex(timerOld),
						//*tempZone.getDbmEntry(0, tempZone.timerIndexToDBMIndex(timerOld)));

				//*setDbmEntry(timerIndexToDBMIndex(timerOld),
						//*timerIndexToDBMIndex(timerNew),
						//*tempZone.getDbmEntry(tempZone.timerIndexToDBMIndex(timerOld), 0));
				
				int newTimerIndex = Arrays.binarySearch(_dbmVarList, timerNew);
				int oldTimerIndex = Arrays.binarySearch(_dbmVarList, timerOld);
				
				int upperBound = (int)Math.ceil(
						_matrix[baseToNeg(oldTimerIndex)][baseToPos(oldTimerIndex)]/2.0);
				int lowerBound = (int)Math.ceil(
						_matrix[baseToPos(oldTimerIndex)][baseToNeg(oldTimerIndex)]/2.0);
				
				_matrix[baseToNeg(oldTimerIndex)][baseToNeg(newTimerIndex)] = upperBound;
				_matrix[baseToNeg(oldTimerIndex)][baseToPos(newTimerIndex)] = upperBound;
				_matrix[baseToNeg(newTimerIndex)][baseToPos(oldTimerIndex)] = upperBound;
				_matrix[baseToPos(newTimerIndex)][baseToPos(oldTimerIndex)] = upperBound;
				
				_matrix[baseToPos(oldTimerIndex)][baseToNeg(newTimerIndex)] = lowerBound;
				_matrix[baseToPos(oldTimerIndex)][baseToPos(newTimerIndex)] = lowerBound;
				_matrix[baseToNeg(newTimerIndex)][baseToNeg(oldTimerIndex)] = lowerBound;
				_matrix[baseToPos(newTimerIndex)][baseToNeg(oldTimerIndex)] = lowerBound;
				
			//*}
			}
		//*}
		}

		// Set the upper and lower bounds for the new timers.
		//*for(LPNTransitionPair pair : newTimers){
		for (LPNTransitionPair pair : newTimers){
		
			// Get all the upper and lower bounds for the new timers.
			// Get the name for the timer in the i-th column/row of DBM
			
			//*String tranName = _lpnList[pair.get_lpnIndex()]
					//*.getTransition(pair.get_transitionIndex()).getLabel();
			//*ExprTree delay = _lpnList[pair.get_lpnIndex()].getDelayTree(tranName);
			String tranName = _lpnList[pair.get_lpnIndex()]
					.getTransition(pair.get_transitionIndex()).getLabel();
			ExprTree delay = _lpnList[pair.get_lpnIndex()].getDelayTree(tranName);
			

			// Get the values of the variables for evaluating the ExprTree.
			//*HashMap<String, String> varValues = 
					//*_lpnList[pair.get_lpnIndex()]
							//*.getAllVarsWithValuesAsString(localStates[pair.get_lpnIndex()].getVariableVector());
			HashMap<String, String> varValues =
					_lpnList[pair.get_lpnIndex()]
							.getAllVarsWithValuesAsString(localStates[pair.get_lpnIndex()]
									.getVariableVector());
			
			
			// Set the upper and lower bound.
			//*int upper, lower;
			//*if(delay.getOp().equals("uniform"))
			//*{
			int upper, lower;
			if (delay.getOp().equals("uniform")){
			
				//*IntervalPair lowerRange = delay.getLeftChild()
						//*.evaluateExprBound(varValues, null, null);
				//*IntervalPair upperRange = delay.getRightChild()
						//*.evaluateExprBound(varValues, null, null);
				IntervalPair lowerRange = delay.getLeftChild()
						.evaluateExprBound(varValues, null, null);
				IntervalPair upperRange = delay.getRightChild()
						.evaluateExprBound(varValues, null, null);
				

				// The lower and upper bounds should evaluate to a single
				// value. Yell if they don't.
				//*if(!lowerRange.singleValue() || !upperRange.singleValue()){
					//*throw new IllegalStateException("When evaulating the delay, " +
							//*"the lower or the upper bound evaluated to a range " +
							//*"instead of a single value.");
				//*}
				if(!lowerRange.singleValue() || !upperRange.singleValue()){
					throw new IllegalStateException("When evaluation the delay, "
							+ " the lower or the upper bound evaluated to a range"
							+ " instead of a single value.");
				}
				
				//*lower = lowerRange.get_LowerBound();
				//*upper = upperRange.get_UpperBound();
				lower = lowerRange.get_LowerBound();
				upper = upperRange.get_UpperBound();
			}
			//*}
			//*else
			//*{
			else{
				//*IntervalPair range = delay.evaluateExprBound(varValues, this, null);
				IntervalPair range = delay.evaluateExprBound(varValues, this, null);
				

				//*lower = range.get_LowerBound();
				//*upper = range.get_UpperBound();
				
				lower = range.get_LowerBound();
				upper = range.get_UpperBound();
			//*}
			}

			//*setLowerBoundByLPNTransitionPair(pair, lower);
			//*setUpperBoundByLPNTransitionPair(pair, upper);
			
			int base = Arrays.binarySearch(_dbmVarList, pair);
			_lowerBounds[base] = -1*lower;
			_upperBounds[base] = upper;
		}
		//*}
	}
	
	/**
	 * This method moves a rate-zero variable into the DBM Octagon portion.
	 * The purpose of this method is to handle when a rate-zero variable
	 * is assigned a non-zero rate.
	 * @param ltContPair The continuous variable to move from the rate zero
	 * variables.
	 * @return The resulting Octagon.
	 */
	@Override
	public Octagon moveOldRateZero(LPNContinuousPair ltContPair) {
		
		// Create a Zone to alter.
		//*Zone newZone = new Zone();
		// Create an Octagon to alter.
		Octagon newOct = new Octagon();
		
		
		// Create a copy of the LPN list.
		//*newZone._lpnList = Arrays.copyOf(this._lpnList, this._lpnList.length);
		newOct._lpnList = Arrays.copyOf(this._lpnList, this._lpnList.length);
		
		// Copy the rate zero continuous variables.
		//*newZone._rateZeroContinuous = this._rateZeroContinuous.clone();
		newOct._rateZeroContinuous = this._rateZeroContinuous.clone();
		
		// Extract the continuous variable from the rate zero variables.
		//*LPNContAndRate rateZero = new LPNContAndRate(ltContPair,
				//*new IntervalPair(0,0));
		LPNContAndRate rateZero = new LPNContAndRate(ltContPair,
				new IntervalPair(0,0));
		
		// This gets the values for the continuous variable.
		//*VariableRangePair vrp = newZone._rateZeroContinuous.get(rateZero);
		//*IntervalPair values = vrp.get_range();
		VariableRangePair vrp = newOct._rateZeroContinuous.get(rateZero);
		IntervalPair values = vrp.get_range();
		
		
		// This replaces the rateZero with the one stored in the _rateZeroContinuous.
		// The purpose of this is to obtain the stored range of rates.
		//*rateZero = newZone._rateZeroContinuous.getKey(vrp);
		rateZero = newOct._rateZeroContinuous.getKey(vrp);
		
		
		// Get the range of rates.
		//*IntervalPair rangeOfRates = rateZero.get_rateInterval();
		IntervalPair rangeOfRates = rateZero.get_rateInterval();
		
		
		// Remove the continuous variable.
		//*newZone._rateZeroContinuous.delete(rateZero);
		newOct._rateZeroContinuous.delete(rateZero);
		
		// Create a copy of the current indexing pairs.
		//*newZone._indexToTimerPair = new LPNTransitionPair[_indexToTimerPair.length + 1];
		//*for(int i=0; i<_indexToTimerPair.length; i++){
			//*newZone._indexToTimerPair[i] = _indexToTimerPair[i];
		//*}
		newOct._dbmVarList = new LPNTransitionPair[_dbmVarList.length + 1];
		for(int i=0; i<_dbmVarList.length; i++){
			newOct._dbmVarList[i] = _dbmVarList[i];
		}

		
		// Add the continuous variable to the list of variables/transition in the DBM.
		//*int numOfTransitions = _indexToTimerPair.length;
		//*newZone._indexToTimerPair[numOfTransitions] = ltContPair;
		int numOfTransitions = _dbmVarList.length;
		newOct._dbmVarList[numOfTransitions] = ltContPair;
		
		// Sort the _indexToTimerPair list.
		//*Arrays.sort(newZone._indexToTimerPair);
		Arrays.sort(newOct._dbmVarList);

		// Create matrix.
		//*newZone._matrix = new int[newZone._indexToTimerPair.length+1][newZone._indexToTimerPair.length+1];
//		newOct._matrix = new int[newOct._dbmVarList.length][newOct._dbmVarList.length];
		newOct._matrix = new int[newOct.DBMsize()][newOct.DBMsize()];
		
		// Convert the current transitions to a collection of transitions.
		//*HashSet<LPNTransitionPair> oldTransitionSet = new HashSet<LPNTransitionPair>();
		//*for(LPNTransitionPair ltPair : _indexToTimerPair){
			//*oldTransitionSet.add(ltPair);
		//*}
		HashSet<LPNTransitionPair> oldTransitionSet = new HashSet<LPNTransitionPair>();
		for(LPNTransitionPair ltPair : _dbmVarList){
			oldTransitionSet.add(ltPair);
		}
		

		// Copy in the new transitions.
		//*newZone.copyTransitions(this, new HashSet<LPNTransitionPair>(),
				//*oldTransitionSet, null);
		newOct.copyTransitions(this, new HashSet<LPNTransitionPair>(),
				oldTransitionSet, null);
		
		
		// Get the index for the variable.
		//*int index = Arrays.binarySearch(newZone._indexToTimerPair, ltContPair);
		int index = Arrays.binarySearch(newOct._dbmVarList, ltContPair);
		
		// Copy in the range of rates.
		//*newZone.setLowerBoundbydbmIndex(index, rangeOfRates.get_LowerBound());
		//*newZone.setUpperBoundbydbmIndex(index, rangeOfRates.get_UpperBound());
		newOct._lowerBounds[index] = -1*rangeOfRates.get_LowerBound();
		newOct._upperBounds[index] = rangeOfRates.get_UpperBound();
		
		
		//*if(ltContPair.getCurrentRate()>0){
		if(ltContPair.getCurrentRate()>0){

			// Set the upper and lower bounds.
			//*newZone.setDbmEntry(0, index,
					//*ContinuousUtilities.chkDiv(values.get_UpperBound(),
							//*ltContPair.getCurrentRate(), true));
			//*newZone.setDbmEntry(index, 0,
					//*ContinuousUtilities.chkDiv(-1*values.get_LowerBound(),
							//*ltContPair.getCurrentRate(), true));
			newOct._matrix[baseToNeg(index)][baseToPos(index)] =
					2*ContinuousUtilities.chkDiv(values.get_UpperBound(),
							ltContPair.getCurrentRate(), true);
			newOct._matrix[baseToPos(index)][baseToNeg(index)] =
					2*ContinuousUtilities.chkDiv(-1*values.get_LowerBound(),
							ltContPair.getCurrentRate(), true);
		}
		//*}
		//*else{
		else{
			// Set the upper and lower bounds. Since the rate is zero 
			// We swap the real upper and lower bounds.
			//*newZone.setDbmEntry(0, index,
					//*ContinuousUtilities.chkDiv(values.get_LowerBound(),
							//*ltContPair.getCurrentRate(), true));
			//*newZone.setDbmEntry(index, 0,
					//*ContinuousUtilities.chkDiv(-1*values.get_UpperBound(),
							//*ltContPair.getCurrentRate(), true));
			
			newOct._matrix[baseToNeg(index)][baseToPos(index)] =
					2*ContinuousUtilities.chkDiv(values.get_LowerBound(),
							ltContPair.getCurrentRate(), true);
			newOct._matrix[baseToPos(index)][baseToNeg(index)] =
					2*ContinuousUtilities.chkDiv(-1*values.get_UpperBound(),
							ltContPair.getCurrentRate(), true);
		}
		//*}
		
		// Set the DBM to having no relating information for how this
		// variables relates to the other variables.
		//*for(int i=1; i<newZone._indexToTimerPair.length; i++){
		for (int i=0; i<newOct._dbmVarList.length; i++){
		
			//*if(i == index){
				//*continue;
			//*}
			if(i == index){
				continue;
			}
			
			//*newZone.setDbmEntry(index, i, Zone.INFINITY);
			//*newZone.setDbmEntry(i, index, Zone.INFINITY);
			newOct._matrix[baseToNeg(index)][baseToNeg(i)] =
					Zone.INFINITY;
			newOct._matrix[baseToNeg(index)][baseToPos(i)] =
					Zone.INFINITY;
			newOct._matrix[baseToPos(index)][baseToNeg(i)] =
					Zone.INFINITY;
			newOct._matrix[baseToPos(index)][baseToPos(i)] =
					Zone.INFINITY;
			
			newOct._matrix[baseToNeg(i)][baseToNeg(index)] =
					Zone.INFINITY;
			newOct._matrix[baseToPos(i)][baseToNeg(index)] =
					Zone.INFINITY;
			newOct._matrix[baseToNeg(i)][baseToPos(index)] =
					Zone.INFINITY;
			newOct._matrix[baseToPos(i)][baseToPos(index)] =
					Zone.INFINITY;
			
		}
		//*}
		
		//*newZone.recononicalize();
		newOct.recononicalize();

		//*return newZone;
		
	//*}

		return newOct;
	}
	
	/**
	 * This method removes the variable from the DBM portion of the
	 * octagon and adds the variable to the rate-zero portion of the
	 * octagon. The purpose of this method is to handle when a non-zero
	 * rate variable is assigned a zero rate.
	 * @param firedRate The continuous variable with the new rate.
	 * @return The resulting Octagon.
	 */
	@Override
	public Octagon saveOutZeroRate(LPNContinuousPair firedRate) {
		
		// Check if the variable is in the zone.
		// We assume that the rate is already zero in this case
		// since it must be in the rate zero variables if it exists.
		//*int index = Arrays.binarySearch(this._indexToTimerPair, firedRate);
		
		// Check if the variable is in the octagon.
		// We assume that the rate is already zero in this case
		// since it must be in the rate zero variables if it exists.
		int index = Arrays.binarySearch(this._dbmVarList, firedRate);
		
		//*if(index < 0){
			//*System.err.println("A variable is getting its rate set to zero");
			//*System.err.println("when the rate is already zero.");
			//*return this;
		//*}
		if(index < 0){
			System.err.println("A variable is getting its rate set to zero");
			System.err.println("when the rate is already zero.");
			return this;
		}
		
		// Create new zone
		//*Zone newZone = new Zone();
		
		// Create the new Octagon
		Octagon newOct = new Octagon();
		
		// Copy the LPNs over.
		//*newZone._lpnList = new LhpnFile[this._lpnList.length];
		//*for(int i=0; i<this._lpnList.length; i++){
			//*newZone._lpnList[i] = this._lpnList[i];
		//*}
		newOct._lpnList = new LPN[this._lpnList.length];
		for(int i=0; i<this._lpnList.length; i++){
			newOct._lpnList[i] = this._lpnList[i];
		}
		
		// Copy over the rate zero variables.
		//*newZone._rateZeroContinuous = this._rateZeroContinuous.clone();
		newOct._rateZeroContinuous = this._rateZeroContinuous.clone();
		
		// Get the range of rates and values.
		//*IntervalPair rangeOfRates = this.getRateBounds(firedRate);
		//*IntervalPair rangeOfValues = this.getContinuousBounds(firedRate);
		IntervalPair rangeOfRates = this.getRateBounds(firedRate);
		IntervalPair rangeOfValues = this.getContinuousBounds(firedRate);
		
		
		// Create the key and value pairs for the rate zero continuous variable.
		//*LPNContAndRate lcar = new LPNContAndRate(firedRate.clone(), rangeOfRates);
		LPNContAndRate lcar = new LPNContAndRate(firedRate.clone(), rangeOfRates);
		
		//*VariableRangePair vrPair =
				//*new VariableRangePair(
						//*this._lpnList[firedRate.get_lpnIndex()]
								//*.getContVar(firedRate.get_ContinuousIndex()),
						//*rangeOfValues);
		VariableRangePair vrPair =
				new VariableRangePair(this._lpnList[firedRate.get_lpnIndex()]
						.getContVar(firedRate.get_ContinuousIndex()),
						rangeOfValues);
		
		
		//*newZone._rateZeroContinuous.insert(lcar, vrPair);
		newOct._rateZeroContinuous.insert(lcar, vrPair);
		
		
		// Copy over the pairs, skipping the new rate zero.
		//*newZone._indexToTimerPair = 
				//*new LPNTransitionPair[this._indexToTimerPair.length-1];
		newOct._dbmVarList =
				new LPNTransitionPair[this._dbmVarList.length-1];
		
		
		//*for(int i=0; i<newZone._indexToTimerPair.length; i++){
		
		// Create the upper and lower bound lists.
		newOct._lowerBounds = new int[newOct._dbmVarList.length];
		newOct._upperBounds = new int[newOct._dbmVarList.length];
		
		int offset = 0;
		for(int i=0; i<newOct._dbmVarList.length; i++){
			//*if(i == index){
				//*continue;
			//*}
			if(i == index){
				offset++;
//				continue;
			}
			//*newZone._indexToTimerPair[i] = this._indexToTimerPair[i].clone();
			newOct._dbmVarList[i] = this._dbmVarList[i+offset].clone();
			
			// Copy over the upper and lower bounds.
			newOct._lowerBounds[i] = this._lowerBounds[i+offset];
			newOct._upperBounds[i] = this._upperBounds[i+offset];
		}
		//*}
		
		// Copy over the DBM
		//*newZone._matrix = new int[newZone.matrixSize()][newZone.matrixSize()];
		newOct._matrix = new int[newOct.DBMsize()][newOct.DBMsize()];
		
		//*int offseti = 0;
		//*int offsetj = 0;
		int offseti = 0;
		int offsetj = 0;
		
		
		//*for(int i=0; i<newZone.matrixSize(); i++){
		for(int i=0; i<newOct.DBMsize(); i++){
			//*if(i == index){
				//*offseti++;
			//*}
			if(i == index){
				offseti++;
				offseti++;
			}
				
			//*for(int j=0; j<newZone.matrixSize(); j++){
			offsetj = 0;
			for(int j=0; j<newOct.DBMsize(); j++){
				//*if(j == index){
					//*offsetj++;
				//*}
				if(j == index){
					offsetj++;
					offsetj++;
				}
				//*newZone._matrix[i][j] = this._matrix[i+offseti][j+offsetj];
				newOct._matrix[i][j] = this._matrix[i+offseti][j+offsetj];
			}
			//*}
		}
		//*}
		
		
		//*return newZone;
		return newOct;
	}
	
	
	/**
	 * Gets the index of the variable in the list of variables represented
	 * in the DBM. This index is not the index of the positive variable or
	 * negative variable, but it can be thought of as the index of the
	 * pair associated with a given variables. Note: the indexing is
	 * zero based.
	 * @param ltpair The variable of interest.
	 * @return The index of the ltpair in the list of variables in the DBM.
	 * Returns a negative number if the ltpair is not in the list.
	 */
	public int getBaseIndex(LPNTransitionPair ltpair){
		return Arrays.binarySearch(_dbmVarList, ltpair);
	}
	
	/**
	 * Gives the index in the DBM for the associated positive variable.
	 * @param base The base index.
	 * @return The index in the DBM for the positive variable for the
	 * base variable. The value is, in fact, 2*base.
	 */
	public static int baseToPos(int base){
		return 2*base;
	}
	
	/**
	 * Gives the index in the DBM for the associated negative variable.
	 * @param base The base index.
	 * @return The index in the DBM for the negative variable for the
	 * base variable. The value is, in fact, 2*base +1.
	 */
	public static int baseToNeg(int base){
		return 2*base +1;
	}
	
	/**
	 * Translates the variables octagon index to the base index.
	 * @param varIndex The octagon index of the variable.
	 * @return Given the index of a pos or neg variable, returns
	 * the base index. The base index can be thought of as the
	 * index of the variable pairs starting with zero.
	 */
	public static int varToBase(int varIndex){
		return varIndex/2;
	}
	
	/**
	 * This method is the bar function of Mine. It switches back
	 * and forth between the index for the positive variable and
	 * the index for the negative variable.
	 * @param index The index of the variable.
	 * @return The index of the positive variable if index is
	 * for a negative variable. The index of the negative variable
	 * if index is for a positive variable.
	 */
	public static int bar(int index){
		return index^1;
	}
	
	/**
	 * Returns the size of the DBM. A key feature is that this
	 * method is defined when the private variable _dbmVarList
	 * is defined. Thus the method can be used to size the matrix
	 * when creating the private variable _matrix.
	 * 
	 * @return The size of the DBM.
	 */
	private int DBMsize(){
		return 2*_dbmVarList.length;
	}
	
	/**
	 * Overrides the hashCode.
	 */
	@Override
	public int hashCode()
	{
		// Check if the hash code has been set.
		if(_hashCode <0)
		{
			_hashCode = createHashCode();
		}

		return _hashCode;
	}
	
	/**
	 * Creates a hash code for an Octagon object.
	 * @return
	 * 		The hash code.
	 */
	private int createHashCode()
	{
		int newHashCode = Arrays.hashCode(_dbmVarList);
		
		for(int i=0; i<_matrix.length; i++)
		{
			newHashCode ^= Arrays.hashCode(_matrix[i]);
		}
		
		return Math.abs(newHashCode);
	}
	
	/**
	 * Tests for equality. Overrides inherited equals method.
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
		if(!(o instanceof Octagon))
		{
			return false;
		}
		
		// Check for equality using the Zone equality.
		return equals((Octagon) o);
	}
	
	/**
	 * Tests for equality.
	 * @param
	 * 		The Zone to compare.
	 * @return 
	 * 		True if the zones are non-null and equal, false otherwise.
	 */
	public boolean equals(Octagon otherOctagon)
	{
		// Check if the reference is null first.
		if(otherOctagon == null)
		{
			return false;
		}
		
		// Check for reference equality.
		if(this == otherOctagon)
		{
			return true;
		}
		
		// If the hash codes are different, then the objects are not equal. 
		if(this.hashCode() != otherOctagon.hashCode())
		{
			return false;
		}
		
		// Check if the they have the same number of timers.
		if(this._dbmVarList.length != otherOctagon._dbmVarList.length){
			return false;
		}
		
		// Check if the timers are the same.		
		for(int i=0; i<this._dbmVarList.length; i++){
			if(!(this._dbmVarList[i].equals(otherOctagon._dbmVarList[i]))){
				return false;
			}
		}
		
		// Check if the matrix is the same 
		for(int i=0; i<_matrix.length; i++)
		{
			for(int j=0; j<_matrix[0].length; j++)
			{
				if(!(this._matrix[i][j] == otherOctagon._matrix[i][j]))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Octagon clone()
	{
		// Note: Check if finished.
		
		//*Zone clonedZone = new Zone();
		Octagon clonedOct = new Octagon();
				
		//*clonedZone._matrix = new int[this.matrixSize()][this.matrixSize()];
		clonedOct._matrix = new int[this.DBMsize()][this.DBMsize()];
		
		//*for(int i=0; i<this.matrixSize(); i++)
		//*{
		for (int i = 0; i<this.DBMsize(); i++){
		
			//*for(int j=0; j<this.matrixSize(); j++)
			//*{
			for (int j=0; j<this.DBMsize(); j++){
				//*clonedZone._matrix[i][j] = this._matrix[i][j];
				clonedOct._matrix[i][j] = this._matrix[i][j];
			}
			//*}
		//*}
		}
		
		//*clonedZone._indexToTimerPair = new LPNTransitionPair[this._indexToTimerPair.length];
		clonedOct._dbmVarList = new LPNTransitionPair[this._dbmVarList.length];
		
		
		//*for(int i=0; i<_indexToTimerPair.length; i++){
			//*clonedZone._indexToTimerPair[i] = this._indexToTimerPair[i].clone();
		//*}
		for (int i=0; i<_dbmVarList.length; i++){
			clonedOct._dbmVarList[i] = this._dbmVarList[i].clone();
		}
		
		// Copy over the upper and lower bounds.
		clonedOct._lowerBounds = new int[this._lowerBounds.length];
		clonedOct._upperBounds = new int[this._upperBounds.length];
		
		for (int i=0; i<_lowerBounds.length; i++){
			clonedOct._lowerBounds[i] = this._lowerBounds[i];
			clonedOct._upperBounds[i] = this._upperBounds[i];
		}
		
		
		//*clonedZone._hashCode = this._hashCode;
		clonedOct._hashCode = this._hashCode;
		
		
		//*clonedZone._lpnList = Arrays.copyOf(this._lpnList, this._lpnList.length);
		clonedOct._lpnList = Arrays.copyOf(this._lpnList, this._lpnList.length);
		
		
		//*clonedZone._rateZeroContinuous = this._rateZeroContinuous.clone();
		clonedOct._rateZeroContinuous = this._rateZeroContinuous.clone();
		
		
		//*return clonedZone;
		return clonedOct;
	}


	@Override
	public int timerIndexToDBMIndex(LPNTransitionPair index) {
		
		return Arrays.binarySearch(_dbmVarList, index);
	}

	@Override
	public int getDbmEntry(int i, int j) {
		/*
		 * This method extracts the relational information that
		 * the zone would provide. If x is the i variable and
		 * j is the y variable, the (i,j) entry coprresponds the
		 * relation y-x <= value. This same information is
		 * contained as y+ - x+ <= value or x- - y- <= value
		 * in the octagon. Choosing the former, yeilds the
		 * following code.
		 */
		
		return this._matrix[baseToPos(i)][baseToPos(j)];
	}

	@Override
	public int getIndexByTransitionPair(LPNTransitionPair ltPair) {
		
		return Arrays.binarySearch(_dbmVarList, ltPair);
	}
	
	@Override
	public int getVarIndex(int lpnIndex, String name){
		return _lpnList[lpnIndex].
				getContinuousIndexMap().getValue(name);
	}
	
	
	@Override
	public int getLowerBoundbyTransition(Transition t){
		LPN lpn = t.getLpn();
		
		int lpnIndex = lpn.getLpnIndex();
		
		int transitionIndex = t.getIndex();
		
		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex);
		
		return getLowerBound(getIndexByTransitionPair(ltPair));
	}
	
	@Override
	public String toString(){
		
		String result = "Timer and delay or continuous and ranges.\n";
		
		int count = 0;
		
		// Print the timers.
		for(int i=0; i<_dbmVarList.length; i++, count++)
		{
			if(_lpnList.length == 0)
			{
				// If no LPN's are associated with this Zone, use the index of the timer.
				result += " t" + _dbmVarList[i].get_transitionIndex() + " : ";
			}
			else
			{
				String name;
				
				// If the current LPNTransitionPair is a timer, get the name
				// from the transitions.
//				if(_indexToTimerPair[i].get_isTimer()){
				// If the current timer is an LPNTransitionPair and not an LPNContinuousPair
				if(!(_dbmVarList[i] instanceof LPNContinuousPair)){
				
					// Get the name of the transition.
					Transition tran = _lpnList[_dbmVarList[i].get_lpnIndex()].
							getTransition(_dbmVarList[i].get_transitionIndex());
					
					name = tran.getLabel();
				}
				else{
					// If the current LPNTransitionPair is not a timer, get the
					// name as a continuous variable.
					Variable var = _lpnList[_dbmVarList[i].get_lpnIndex()]
							.getContVar(_dbmVarList[i].get_transitionIndex());
					
					LPNContinuousPair lcPair =
							(LPNContinuousPair) _dbmVarList[i];
					
					name = var.getName() + 
							":[" + -1*getLowerBoundTrue(i)*lcPair.getCurrentRate() + ","
							+ getUpperBoundTrue(i)*lcPair.getCurrentRate() + "]\n" +
							" Current Rate: " + lcPair.getCurrentRate() + " " +
							"rate:";
				}
				
//				result += " " +  tran.getName() + ":";
				
				result += " " + name + ":";
			}
			result += "[ " + -1*getLowerBound(i) + ", " + getUpperBound(i) + " ]";
			
			if(count > 9)
			{
				result += "\n";
				count = 0;
			}
		}
		
		if(!_rateZeroContinuous.isEmpty()){
			result += "\nRate Zero Continuous : \n";
			for (LPNContAndRate lcrPair : _rateZeroContinuous.keySet()){
				result += "" + _rateZeroContinuous.get(lcrPair) 
						+ "Rate: " + lcrPair.get_rateInterval();
			}
		}
		
		result += "\nDBM\n";
		

		// Print the DBM.
		for(int i=0; i<2*_dbmVarList.length; i++)
		{
//			result += "| " + String.format("%3d", getDbmEntry(i, 0));
			result += "| " + String.format("%3d", _matrix[i][0]);
			
			for(int j=1; j<2*_dbmVarList.length; j++)
			{

//				result += ", " + String.format("%3d",getDbmEntry(i, j));
				result += ", " + String.format("%3d",_matrix[i][j]);
			}
			
			result += " |\n";
		}
		

		return result;
	}
}
