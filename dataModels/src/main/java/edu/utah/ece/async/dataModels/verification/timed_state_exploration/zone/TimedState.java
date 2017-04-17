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
package edu.utah.ece.async.dataModels.verification.timed_state_exploration.zone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.utah.ece.async.dataModels.lpn.LPN;
import edu.utah.ece.async.dataModels.lpn.Transition;
import edu.utah.ece.async.dataModels.verification.platu.platuLpn.DualHashMap;
import edu.utah.ece.async.dataModels.verification.platu.platuLpn.VarSet;
import edu.utah.ece.async.dataModels.verification.platu.stategraph.State;
import edu.utah.ece.async.dataModels.verification.platu.stategraph.StateGraph;
import edu.utah.ece.async.dataModels.verification.timed_state_exploration.zone.TimedState;
import edu.utah.ece.async.dataModels.verification.timed_state_exploration.zone.Zone;

/**
 * Adds timing to a State.
 * 
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class TimedState extends State{
	
	// Abstraction Function:
	// This class follows the extension pattern for extending a base class. A TimedState
	// adds a Zone for keeping track of timing relations.
	
	// A Zone for keeping track timing information.
	private ZoneType _zone;
	
	// A ZoneGraph for storing a zone.
	private ZoneGraph _graph;
	
	// Variable that determines whether zones or graph are being used.
	private boolean _useGraph;

	// The state that this TimingState extends.
	private State _state;
	
	@Override
	public void setLpn(LPN thisLpn) {
		_state.setLpn(thisLpn);
	}

	@Override
	public LPN getLpn() {
		return _state.getLpn();
	}

	@Override
	public void setLabel(String lbl) {
		_state.setLabel(lbl);
	}

	@Override
	public String getLabel() {
		return _state.getLabel();
	}

	@Override
	public boolean[] getTranVector() {
		return _state.getTranVector();
	}

	@Override
	public void setIndex(int newIndex) {
		_state.setIndex(newIndex);
	}

	@Override
	public int getIndex() {
		return _state.getIndex();
	}

	@Override
	public boolean hasNonLocalEnabled() {
		return _state.hasNonLocalEnabled();
	}

	@Override
	public void hasNonLocalEnabled(boolean nonLocalEnabled) {
		_state.hasNonLocalEnabled(nonLocalEnabled);
	}

	@Override
	public boolean isFailure() {
		return _state.isFailure();
	}

	@Override
	public TimedState clone() {
		// TODO: Ensure that the new TimedState contains its own copy of the zone.
		if(_useGraph){
			return new TimedState(_state, _zone, true);
		}
		return new TimedState(_state, _zone, false);
	}

	@Override
	public String print() {
		return _state.print();
	}

	@Override
	public int hashCode() {
		return _state.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Check for completion.
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		TimedState other = (TimedState) obj;
		
		if(!_state.equals(other._state))
			return false;
		if(_useGraph){
			return _graph.equals(other._graph);
		}
		return _zone.equals(other._zone);
	}

	@Override
	public void print(DualHashMap<String, Integer> VarIndexMap) {
		_state.print(VarIndexMap);
	}

	@Override
	public int[] getMarking() {
		return _state.getMarking();
	}

	@Override
	public void setMarking(int[] newMarking) {
		_state.setMarking(newMarking);
	}

	@Override
	public int[] getVariableVector() {
		return _state.getVariableVector();
	}

	@Override
	public HashMap<String, Integer> getOutVector(VarSet outputs,
			DualHashMap<String, Integer> VarIndexMap) {
		return _state.getOutVector(outputs, VarIndexMap);
	}

	@Override
	public State getLocalState() {
		return _state.getLocalState();
	}


	@Override
	public String getEnabledSetString() {
		return _state.getEnabledSetString();
	}

	@Override
	public State update(StateGraph SG, HashMap<String, Integer> newVector,
			DualHashMap<String, Integer> VarIndexMap) {
		return _state.update(SG, newVector, VarIndexMap);
	}

	@Override
	public State update(HashMap<String, Integer> newVector,
			DualHashMap<String, Integer> VarIndexMap, boolean[] newTranVector) {
		return _state.update(newVector, VarIndexMap, newTranVector);
	}

	@Override
	public File serialize(String filename) throws FileNotFoundException,
			IOException {
		return _state.serialize(filename);
	}

	@Override
	public boolean failure() {
		return _state.failure();
	}

	@Override
	public void setFailure() {
		_state.setFailure();
	}

	@Override
	public void printStateInfo() {
		_state.printStateInfo();
	}

	@Override
	public ArrayList<TimedState> getTimeExtension() {
		//return super.getTimeExtension();
		return _state.getTimeExtension();
	}

	@Override
	public void setTimeExtension(ArrayList<TimedState> s) {
		//super.setTimeExtension(s);
		_state.setTimeExtension(s);
	}
	
	@Override
	public void addTimeExtension(TimedState s){
		//super.addTimeExtension(s);
		_state.addTimeExtension(s);
	}

	public TimedState(LPN lpn, int[] new_marking, int[] new_vector,
			boolean[] new_isTranEnabled, boolean usegraph) {
		super(lpn, new_marking, new_vector, new_isTranEnabled);
		// TODO Find a way to remove the super call. 
		
		_state = new State(lpn, new_marking, new_vector, new_isTranEnabled);
		
		_useGraph = usegraph;
		
		//_zone = new Zone(new State(lpn, new_marking, new_vector, new_isTranEnabled));
		
		Zone newZone = new Zone(new State(lpn, new_marking, new_vector, new_isTranEnabled));
		
		if(usegraph){
			_graph = ZoneGraph.extractZoneGraph(newZone);
		}
		else{
			_zone = newZone;
		}
		
		//_state.setTimeExtension(this);
		_state.addTimeExtension(this);
	}

	/**
	 * Creates a timed state by adding an initial zone.
	 * @param other
	 * 			The current state.
	 */
	public TimedState(State other, boolean usegraph) {
		super(other);
		// TODO Find a way to remove the super call.
		
		_state = other;
		
		_useGraph = usegraph;

		//_zone = new Zone(other);
		
		Zone newZone = new Zone(other);
		
		if(usegraph){
			_graph = ZoneGraph.extractZoneGraph(newZone);
		}
		else{
			_zone = newZone;
		}
		
		//_state.setTimeExtension(this);
		_state.addTimeExtension(this);
	}
	
	public TimedState(State s, ZoneType z, boolean usegraph)
	{
		super(s);
		// TODO: Find a way to remove the super call.
		_state = s;
		
		_useGraph = usegraph;
		
		if(usegraph && (z instanceof Zone)){
			_graph = ZoneGraph.extractZoneGraph((Zone) z);
		}
		else{
			_zone = z.clone();
		}
		//_state.setTimeExtension(this);
		_state.addTimeExtension(this);
	}
	
	@Override
	public String toString()
	{
		if(_useGraph){
			return _state.toString() + "\n" + _graph;
		}
		return _state.toString() + "\n" + _zone;
	}
	
	public List<Transition> getEnabledTransitionByZone()
	{
		if(_useGraph){
			return _graph.extractZone().getEnabledTransitions();
		}
		return _zone.getEnabledTransitions();
	}
	
	public ZoneType getZone()
	{
		if(_useGraph){
			return _graph.extractZone();
		}
		return _zone;
	}
	
	public ZoneGraph getZoneGraph(){
		return _graph;
	}

	public State getState()
	{
		return _state;
	}
	
	
	public boolean usingGraphs(){
		return _useGraph;
	}
	
	public boolean untimedStateEquals(TimedState s){
		return this._state.equals(s._state);
	}
	
	public boolean untimedStateEquals(State s){
		if(s instanceof TimedState){
			TimedState t = (TimedState) s;
			return untimedStateEquals(t);
		}
		return this._state.equals(s);
	}
}
