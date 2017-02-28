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
package backend.verification.platu.project;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import backend.verification.platu.main.Options;
import backend.verification.platu.stategraph.State;
import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Transition;

public class PrjState {
	protected  State[] stateArray;
	private PrjState father;
	private PrjState child;	
	/**
	 * A hash map recording the transition-state flow information at current global state.
	 * Key: outgoing transition from current state.
	 * Value: next global state reached by the corresponding outgoing transition. 
	 */
	protected HashMap<Transition, PrjState> nextGlobalStateMap;

	public PrjState() {
		stateArray = null;
		father = null;
		child = null;
	}
	
	public PrjState(final State[] other) {
		stateArray = other;
		father = null;
		child = null;
		if (Options.getOutputSgFlag() || Options.getMarkovianModelFlag())
			nextGlobalStateMap = new HashMap<Transition, PrjState>();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(stateArray);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		PrjState other = (PrjState) obj;
		if(this.stateArray == other.stateArray)
			return true;
		if (!Arrays.equals(stateArray, other.stateArray))
			return false;
		return true;
	}

	public State get(int index) {
		return stateArray[index];
	}
	
	public void add(int index, State other) {
		stateArray[index] = other;
	}
	
	public State[] toStateArray() {
		return stateArray;
	}
	
	public void setFather(final PrjState state) {
		this.father = state;
	}
	
	public void setChild(final PrjState state) {
		this.child = state; 
	}
	
	public PrjState getFather() {
		return this.father;
	}
	
	public PrjState getChild() {
		return this.child; 
	}
	
	@Override
	public String toString() {
		String line ="";
		for(int i = 0; i < stateArray.length; i++) {
			line += stateArray[i].toString()+"+"+stateArray[i].getLpn().getLabel() + "   ";
		}
		return line;
//            return toList(stateArray).toString();
	}
//	public void print(final LPN[] lpnList) {
//		for(int i = 0; i < stateArray.length; i++) {
//			System.out.print(i +": ");
//			stateArray[i].print(lpnList[i].getVarIndexMap());
//			System.out.println();
//		}
//	}
	
	public void print(final LPN[] lpnList) {
		for(int i = 0; i < stateArray.length; i++) {
			System.out.println(lpnList[i].getLabel() + ".lpn" +": ");
			//stateArray[i].print(lpnList[i].getVarIndexMap());
			stateArray[i].printStateInfo();
			System.out.println();
		}
	}
	
    public static final Collection<Object> toList(Object[] arr) {
        Set<Object> l = new HashSet<Object>(1);
        l.addAll(Arrays.asList(arr));
        return l;
    }

    public static final Object[] toArray(Collection<Object> set) {
        Object[] arr = new Object[set.size()];
        int idx = 0;
        for (Object i : set) {
            arr[idx++] = i;
        }
        return arr;
    }

//	public void setTranOut(Transition tran, PrjState nextState) {
//		nextStateMap.put(tran, nextState);
//	}
	
//	public Transition getOutgoingTranToState(PrjState nextState) {
//		return nextStateMap.getKey(nextState);
//	}

//	public void setTranIn(Transition tran, PrjState prevState) {
//		prevStateMap.put(tran, prevState);
//	}
	
//	public Transition getIncomingTranFromState(PrjState prevState) {
//		return prevStateMap.getKey(prevState);
//	}

//	public void setNextStateMap(HashMap<Transition, PrjState> map) {
//		this.nextStateMap = map;
//	}
//	
//	public HashMap<Transition, PrjState> getNextStateMap() {
//		return nextStateMap;
//	}
 
	public State[] getStateArray() {
		return stateArray;		
	}

	
	public void addNextGlobalState(Transition tran, PrjState nextPrjState) {
		this.nextGlobalStateMap.put(tran, nextPrjState);
	}
	
//	public PrjState getNextGlobalState(Transition outTran) {
//		return nextGlobalStateMap.get(outTran);
//	}
//		
	
//	/**
//	 * This method uses local state-transition information to search for the next global states of 
//	 * this current global state, and returns a set of such next global states. The search
//	 * is performed based on the local states that this current global state is composed of. For example, assume a current
//	 * global state S0 is composed of n (n>=1) local states: s_00,s_10,s_20...,s_n0. Given the outgoing transition, outTran, 
//	 * it finds in each of the local state, namely s_00, s_10, s_20, ..., s_n0, their next local states, and then pieces them
//	 * together to form the next global state. Next, it grabs the equivalent one from the global state set. The obtained is the
//	 * next global state reached from S0 by taking outTran, and it is returned. 
//	 * @param outTran
//	 * @param globalStateSet
//	 * @return
//	 */
//	public PrjState getNextPrjState(Transition outTran, StateSetInterface globalStateSet) {
//		return null;
////		State[] nextStateArray = new State[this.toStateArray().length];
////		for (State curLocalSt : this.toStateArray()) {
////			State nextLocalSt = curLocalSt.getNextLocalState(outTran);
////			if (nextLocalSt != null) { // outTran leads curLocalSt to a next state.
////				nextStateArray[curLocalSt.getLpn().getLpnIndex()] = nextLocalSt;//nextOtherLocalSt.clone();
////			}
////			else { // No nextLocalSt was found. Transition outTran did not change this local state.
////				nextStateArray[curLocalSt.getLpn().getLpnIndex()] = curLocalSt;
////			}
////		}
////		PrjState tmpPrjSt = new PrjState(nextStateArray);
////		if (((HashSetWrapper)globalStateSet).get(tmpPrjSt) == null) 
////			throw new NullPointerException("Next global state was not found.");		
////		else
////			return ((HashSetWrapper) globalStateSet).get(tmpPrjSt);
//		
//	}
	
	public HashMap<Transition, PrjState> getNextGlobalStateMap() {
		return nextGlobalStateMap;
	}

//	/**
//	 * Return a set of outgoing transitions from this PrjState. 
//	 * @return
//	 */
//	public Set<Transition> getOutgoingTrans() {
//		Set<Transition> outgoingTrans = new HashSet<Transition>();
//		for (State curLocalSt : this.toStateArray())
//			outgoingTrans.addAll(curLocalSt.getOutgoingTranSet());
//		return outgoingTrans;
//	}

	/**
	 * Return the prjState label, which is the concatenation of local state labels. 
	 * @return
	 */
	public String getLabel() {
		String prjStateLabel = "";
		for (State localSt : this.toStateArray()) {
			prjStateLabel += localSt.getLabel() + "_";
		}
		return prjStateLabel.substring(0, prjStateLabel.lastIndexOf("_"));		
	}
}            

