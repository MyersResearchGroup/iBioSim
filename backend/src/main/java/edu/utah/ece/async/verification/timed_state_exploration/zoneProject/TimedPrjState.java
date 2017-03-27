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
package main.java.edu.utah.ece.async.verification.timed_state_exploration.zoneProject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import main.java.edu.utah.ece.async.lpn.parser.LPN;
import main.java.edu.utah.ece.async.lpn.parser.Transition;
import main.java.edu.utah.ece.async.verification.platu.main.Options;
import main.java.edu.utah.ece.async.verification.platu.platuLpn.LpnTranList;
import main.java.edu.utah.ece.async.verification.platu.project.PrjState;
import main.java.edu.utah.ece.async.verification.platu.stategraph.State;
import main.java.edu.utah.ece.async.verification.timed_state_exploration.octagon.Equivalence;
import main.java.edu.utah.ece.async.verification.timed_state_exploration.octagon.Octagon;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class TimedPrjState extends PrjState{
	
	/*
	 * For state graph purposes, we need a unique id. Should be kept at
	 * the next available number.
	 */
	public static int TimedStateCount = 0;
	
	public static int getTimeStateCount(){
		return TimedStateCount;
	}
	
	public static void incTSCount(){
		TimedStateCount++;
	}
	
	public static void decTSCount(){
		TimedStateCount--;
	}
	
	/*
	 * The id for this state.
	 */
	public int _tsId;
	
	public int getTSID(){
		return _tsId;
	}
	
	public void setTSID(int newID){
		_tsId = newID;
	}
	
	/**
	 * Sets the tsId to the current TimedStateCount.
	 */
	public void setCurrentId(){
		_tsId = TimedStateCount;
	}
	
	/*
	 * Map states to previous state. This map needs to be maintained when
	 * the state graph option is selected and when supersets is enabled.
	 * The reason it is important for subsets is when a previously found
	 * state is removed due to the current state being a superset, then
	 * each of the states that pointed to the previous state need to
	 * be updated to point to the current state.
	 */
	private HashMap<EventSet, HashSet<TimedPrjState>> _previousProjectState;
	
	/**
	 * Add an element to the preset.
	 * @param e
	 * @param tps
	 */
	public void addPreviousState(EventSet e, TimedPrjState tps){
		if (_previousProjectState.containsKey(e)){
			_previousProjectState.get(e).add(tps);
		}
		else{
			HashSet<TimedPrjState> newSet = new HashSet<TimedPrjState>();
			newSet.add(tps);
			_previousProjectState.put(e, newSet);
		}
	}
	
	public HashMap<EventSet, HashSet<TimedPrjState>> get_previousProjectState(){
		return _previousProjectState;
	}
	
//	protected Zone[] _zones;
	protected Equivalence[] _zones;
	
	//*public TimedPrjState(final State[] other, final Zone[] otherZones){
		//*super(other);
		//*this._zones = otherZones;
	//*}
	public TimedPrjState(final State[] other, final Equivalence[] otherZones){
		super(other);
		_tsId = TimedStateCount;
		this._zones = otherZones;
		if(Zone.getSupersetFlag()&&Options.getOutputSgFlag()){
			// We only need to worry about constructing this map when making the state graph.
			_previousProjectState = new HashMap<EventSet, HashSet<TimedPrjState>>();
		}
	}
	
	
	public TimedPrjState(State[] initStateArray) {
		super(initStateArray);
//		_zones = new Zone[initStateArray.length];
//		for(int i=0; i<_zones.length; i++){
//			_zones[i] = new Zone(initStateArray[i]);
//		}
		
		if(Zone.getSupersetFlag()&&Options.getOutputSgFlag()){
			// We only need to worry about constructing this map when making the state graph.
			_previousProjectState = new HashMap<EventSet, HashSet<TimedPrjState>>();
		}
		
		if(main.java.edu.utah.ece.async.verification.platu.main.Options.getTimingAnalysisType()
				.equals("zone")){
		
			/*  
			 * The verification type is zones, so load the
			 * Equivalence variable with a zone.
			 */
			_zones = new Zone[1];
		
			_zones[0] = new Zone(initStateArray);
		}
		else if (main.java.edu.utah.ece.async.verification.platu.main.Options.getTimingAnalysisType()
		.equals("octagon")){
			
			/*
			 * The verification type is octagons, so load the
			 * Equivalence variable with an octagon.
			 */
			
			_zones = new Octagon[1];
			
			_zones[0] = new Octagon(initStateArray);
		}
	}


//	public Zone[] toZoneArray(){
	public Equivalence[] toZoneArray(){
		return _zones;
	}
	
	@Override
	public boolean equals(Object other){
		if(!super.equals(other)){
			return false;
		}
		
		if(!(other instanceof TimedPrjState)){
			return false;
		}
		
		TimedPrjState otherTimedPrjState = (TimedPrjState) other;
		
		if(this._zones == otherTimedPrjState._zones){
			return true;
		}
		
		if(!Arrays.equals(this._zones, otherTimedPrjState._zones)){
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString(){
		String result = "TS_ID = " + _tsId + '\n';
		
		result += super.toString();
		
		result += "\nZones: \n";
		
//		for(Zone z : _zones){
		for(Equivalence z: _zones){
			result += z.toString() + "\n";
		}
		
		return result;
	}
	
	/**
	 * Gives the currently enabled transitions according to the given zone.
	 * @param zoneNumber
	 * 			The index of the Zone to consider.
	 * @return
	 * 			The Transitions that are enabled when considering the zone at index zoneNumber.
	 */
	public LpnTranList getEnabled(int zoneNumber){
		
		// Zone to consider
//		Zone z = _zones[zoneNumber];
		Equivalence z = _zones[zoneNumber];
		
		return new LpnTranList(z.getEnabledTransitions());
	}
	
	/**
	 * Gives the Transitions belonging to a particular LPN that are enabled according to a given
	 * zone.
	 * @param zoneNumber
	 * 			The index of the zone to consider for determining the enabled transitions.
	 * @param lpnIndex
	 * 			The index of the LPN to which the Transitions belong.
	 * @return
	 * 			The Transitions in the LPN with index lpnIndex that are enabled according to the Zone
	 * 			at index zoneNumber.
	 */
	public LpnTranList getEnabled(int zoneNumber, int lpnIndex){
		
//		Zone z = _zones[zoneNumber];
		Equivalence z = _zones[zoneNumber];
		
		return new LpnTranList(z.getEnabledTransitions(lpnIndex));
	}
	
	/**
	 * Gets a project state containing the un-timed portion of this timed project state.
	 * @return
	 * 		A project state that has the same set of (local) un-timed states.
	 */
	public PrjState getUntimedPrjState(){
		return new PrjState(toStateArray());
	}
	
	/**
	 * Checks if all the zones are subsets (or equal to) the corresponding other zones as well
	 * as if the un-timed portions are equal. Note: it is assumed that both states have the same
	 * number of zones.
	 * @param other
	 * 		The state to compare against.
	 * @return
	 * 		True if zone zi is a subset of zone z'i for all i where zi is the i-th zone
	 * 		of this TimedPrjState and z'i is the i-th zone of the other TimedPrjState and
	 * 		the un-timed portion of the states are equal.
	 */
	public boolean subset(TimedPrjState other){
		// For clarity, extract the un-timed portions. Alternately, super.equals(otherState) would
		// probably work.
		
		PrjState thisUntimed = this.getUntimedPrjState();
		PrjState otherUntimed = other.getUntimedPrjState();
		
		if(!(thisUntimed.equals(otherUntimed))){
			return false;
		}
		
		return subsetZone(other);
	}
	
	/**
	 * Checks if all the zones are supersets (or equal to) the corresponding other zones as well
	 * as if the un-timed portions are equal. Note : it is assumed that both states have the same
	 * number of zones.
	 * @param other
	 * 		The state to compare against.
	 * @return
	 * 		True if zone zi is a superset of zone z'i for all i where zi is the i-th zone
	 * 		of this TimedPrjState and z'i is the i-th zone of the other TimedPrjState and
	 * 		the un-timed portion of the states are equal.
	 */
	public boolean superset(TimedPrjState other){
		// For clarity, extract the un-timed portions. Alternately, super.equals(otherState) would
		// probably work.

		PrjState thisUntimed = this.getUntimedPrjState();
		PrjState otherUntimed = other.getUntimedPrjState();

		if(!(thisUntimed.equals(otherUntimed))){
			return false;
		}

		return supersetZone(other);
	}
	
	/**
	 * Checks if all the zones are subsets (or equal to) the corresponding other zones.
	 * Note: The un-timed portion is not considered. It is assumed that each state has
	 * the same number of zones.
	 * @param other
	 * 		The state to compare against.
	 * @return
	 * 		True if zone zi is a subset of zone z'i for all i where zi is the i-th zone
	 * 		of this TimedPrjState and z'i is the i-th zone of the other TimedPrjState.
	 * 		False otherwise.
	 */
	public boolean subsetZone(TimedPrjState other){
		boolean result = true;
		
		for(int i=0; i<this._zones.length; i++){
			result &= _zones[i].subset(other._zones[i]);
		}
		
		return result;
	}
	
	/**
	 * Checks if all the zones are supersets (or equal to) the corresponding other zones.
	 * Note: The un-timed portion is not considered. It is assumed that both states have the
	 * same number of zones.
	 * @param other
	 * 		The state to compare against.
	 * @return
	 * 		True if zone zi is a superset of zone z'i for all i where zi is the i-th zone
	 * 		of this TimedPrjState and z'i is the i-th zone of the other TimedPrjState.
	 * 		False otherwise.
	 */
	public boolean supersetZone(TimedPrjState other){
		boolean result = true;
		
		for(int i=0; i<this._zones.length; i++){
			result &= _zones[i].superset(other._zones[i]);
		}
		
		return result;
	}
	
	/**
	 * Updates all the IneqaulityVariables according to the current state and Zone.
	 */
	public void updateInequalityVariables(){
		
		// Iterate through the InequalityVariables of each state and update the result.
		
		for(State s : this.stateArray){
			// Extract the LPN.
			LPN lpn = s.getLpn();
			
			// Get the state vector to update.
			int[] vector = s.getVariableVector();
			
			// The variables are not stored in the state, so get them from the LPN.
			String[] variables = lpn.getVariables();
			
			// Find the inequality variables.
			for(int i=0; i<variables.length; i++){
				
				// A name starting with '$' indicates a name of an InequalityVariable.
				if(variables[i].startsWith("$")){
					
					// Get the variable for using its evaluator.
					InequalityVariable var = (InequalityVariable) lpn.getVariable(variables[i]);
					//vector[i] = var.evaluateInequality(s, this._zones[0]);
					
					// Get the new value.
					//vector[i] = var.evaluateInequality(s, _zones[0]).equals("true") ? 1 : 0;
					vector[i] = var.evaluate(s, _zones[0]);
				}
			}
			
		}
	}
	
	/**
	 * Gets the sets of possible events that can occur for a given LPN.
	 * @param zoneNumber
	 * 		The Zone to use to determine what can be fired.
	 * @param lpnIndex
	 * 		The index of the LPN of interest.
	 * @return
	 * 		An LpnTranList populated with EvenSet objects giving the events that can fire.
	 */
	public LpnTranList getPossibleEvents(int zoneNumber, int lpnIndex){
		
//		Zone z = _zones[zoneNumber];
		
		Equivalence z = _zones[zoneNumber];
		
//		return new LpnTranList(z.getPossibleEvents(lpnIndex, this.getStateArray()[lpnIndex]));
		
		LpnTranList newEvents = new LpnTranList(z.getPossibleEvents(lpnIndex,
				this.getStateArray()[lpnIndex]));
		
		/*
		 * If the rateOptimization flag is enabled, bias the search towards firing rate change
		 * events. Specifically, when only a single rate change event is present, remove
		 * the other possible events and just fire the rate change event.
		 */
		if(Options.get_rateOptimization()){
//			// Determine if the number of rate events is one.
//			int rateCount = 0;
//			EventSet rateEvent = null;
//			
//			for(Transition event: newEvents){
//				EventSet es = (EventSet) event;
//				if(es.isRate()){
//					rateCount++;
//					rateEvent = es;
//				}
//			}
//			
//			// If one rate change event, change newEvents to have that single event.
//			if(rateCount == 1){
//				newEvents = new LpnTranList();
//				newEvents.add(rateEvent);
//			}
			
			LpnTranList rateEvents = null;
			
			for(Transition event: newEvents){
				EventSet es = (EventSet) event;
				if(es.isRate()){
					if(rateEvents == null){
						rateEvents = new LpnTranList();
						rateEvents.add(es);
					}
					else{
						rateEvents.add(es);
					}
				}
			}
			
			if(rateEvents != null){
				newEvents = rateEvents;
			}
		}
		
		return newEvents;
		
	}
	
	/**
	 * Get the Zones this (global) state contains.
	 * @return
	 * 		The Zones this (gloabl) state contains.
	 */
//	public Zone[] get_zones(){
	public Equivalence[] get_zones(){

		return _zones;
//		return _zones;
	}
}
