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
package backend.verification.platu.platuLpn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import backend.verification.platu.stategraph.State;
import backend.verification.platu.stategraph.StateGraph;
import dataModels.lpn.parser.Transition;

import java.util.Set;

/**
 * The LPNTranRelation class stores information about LPN transitions such  as dependence, 
 * interleaving, and independence.
 * 
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
 public class LPNTranRelation {
	private List<StateGraph> designUnitSet = null;
	private Map<Transition, Set<Transition>> transitiveDependence = new HashMap<Transition, Set<Transition>>();  // type 1 transitive
	private Map<Transition, Set<Transition>> interleavingDependence = new HashMap<Transition, Set<Transition>>();  // type 2 interleaving
//	public Map<LPNTran, Set<LPNTran>> case2 = new HashMap<LPNTran, Set<LPNTran>>();
	
	public LPNTranRelation(List<StateGraph> designUnitSet){
		this.designUnitSet = designUnitSet;
	}
	
	/**
	 * Finds pairs of transitions which are dependent locally and between modules.  Identifies two types of dependencies:
	 * 1) transitive
	 * 2) interleaving
	 * This function assumes findSgCompositional() has been called.
	 * Type 1 pairs are stored in Map transitiveDependence, and type 2 pairs are stored in Map interleavingDependence.
	 */
	public void findCompositionalDependencies(){
		for(StateGraph sg : this.designUnitSet){
			//for(State currentState : sg.reachable()){
			for(State currentState : sg.getStateSet()) {
				Set<Entry<Transition, State>> stateTranSet = sg.getOutgoingTrans(currentState);
				if(stateTranSet == null) continue;
				
				for(Entry<Transition, State> stateTran: stateTranSet){
					State startState = currentState;
					State endState = stateTran.getValue();
					Transition lpnTran = stateTran.getKey();
					
					//TODO: (original) only get enabled trans from lpn or input transitions also?
					LpnTranList currentEnabledTransitions = sg.getEnabled(startState);
		        	LpnTranList nextEnabledTransitions = sg.getEnabled(endState);
		        	
		        	// disabled trans
		        	LpnTranList current_minus_next = currentEnabledTransitions;//currentEnabledTransitions.clone();
		        	current_minus_next.removeAll(nextEnabledTransitions);
		        	current_minus_next.remove(lpnTran);
		        	
		        	// type 2
		        	for(Transition disabledTran: current_minus_next){
		        		// t1 -> t2
		        		if(interleavingDependence.containsKey(lpnTran)){
		        			interleavingDependence.get(lpnTran).add(disabledTran);
		        		}
		        		else{
		        			Set<Transition> tranSet = new HashSet<Transition>();
		        			tranSet.add(disabledTran);
		    				interleavingDependence.put(lpnTran, tranSet);
		        		}
		        		
		        		if(interleavingDependence.containsKey(disabledTran)){
		        			interleavingDependence.get(disabledTran).add(lpnTran);
		        		}
		        		else{
		        			Set<Transition> tranSet = new HashSet<Transition>();
		        			tranSet.add(lpnTran);
		    				interleavingDependence.put(disabledTran, tranSet);
		        		}
		        	}
		        	
		        	// enabled trans
		        	LpnTranList next_minus_current = nextEnabledTransitions;
		        	next_minus_current.removeAll(currentEnabledTransitions);
		        	
		        	// type 1
		        	for(Transition enabledTran: next_minus_current){
	        			// t1 -> t2
		        		if(transitiveDependence.containsKey(lpnTran)){
		        			transitiveDependence.get(lpnTran).add(enabledTran);
		        		}
		        		else{
		        			Set<Transition> tranSet = new HashSet<Transition>();
		        			tranSet.add(enabledTran);
		    				transitiveDependence.put(lpnTran, tranSet);
		        		}
		        	}
		        	
		        	LpnTranList remainEnabled = currentEnabledTransitions;
		        	remainEnabled.removeAll(current_minus_next);
		        	for(Transition remainTran : remainEnabled){
		        		State s1 = sg.getNextState(currentState, remainTran);
		        		if(s1 == null) continue;
		        		
		        		LpnTranList en = sg.getEnabled(s1);
		        		if(!en.contains(lpnTran)) continue;
		        		
		        		State s3 = sg.getNextState(s1, lpnTran);
		        		State s2 = sg.getNextState(endState, remainTran);
		        		if(s2 == null) continue;
		        		if(s3 == null) continue;
		        		
		        		if(s2 != s3){
			        		if(interleavingDependence.containsKey(lpnTran)){
			        			interleavingDependence.get(lpnTran).add(remainTran);
			        		}
			        		else{
			        			Set<Transition> tranSet = new HashSet<Transition>();
			        			tranSet.add(remainTran);
			        			interleavingDependence.put(lpnTran, tranSet);
			        		}
			        		
			        		if(interleavingDependence.containsKey(remainTran)){
			        			interleavingDependence.get(remainTran).add(lpnTran);
			        		}
			        		else{
			        			Set<Transition> tranSet = new HashSet<Transition>();
			        			tranSet.add(lpnTran);
			        			interleavingDependence.put(remainTran, tranSet);
			        		}
			        		
	//		        		if(case2.containsKey(lpnTran)){
	//		        			case2.get(lpnTran).add(remainTran);
	//		        		}
	//		        		else{
	//		        			Set<LPNTran> tranSet = new HashSet<LPNTran>();
	//		        			tranSet.add(remainTran);
	//		        			case2.put(lpnTran, tranSet);
	//		        		}
		        		}
		        	}
				}
			}
		}
	}
	
	/**
     * Returns entry set of Map transitiveDependence, where the key value is an LPNTran 
     * and the value is a list of LPNTran which have transitive dependence.
     * @return Map transitiveDependence entry set
     */
	public Set<Entry<Transition, Set<Transition>>> getDependentTrans(){
		return this.transitiveDependence.entrySet();
	}
	
	/**
     * Returns entry set of Map interleavingDependence, where the key value is an LPNTran 
     * and the value is a list of LPNTran which have interleaving dependence.
     * @return Map interleavingDependence entry set
     */
	public Set<Entry<Transition, Set<Transition>>> getInterleavingTrans(){
		return this.interleavingDependence.entrySet();
	}

	public void printCompositionalDependencies() {
		System.out.println("------- Compositional Dependencies -----------");
		if (interleavingDependence.isEmpty()) {
			System.out.println("empty interleavingDependence.");
		}
		else {
			for (Transition tran : interleavingDependence.keySet()) {
				System.out.println("interleaving dependency for tran = " + tran.getFullLabel());
				for (Transition depTran : interleavingDependence.get(tran)) {
					System.out.println("\t" + depTran.getFullLabel());
				}
			}
		}
		if (transitiveDependence.isEmpty()) {
			System.out.println("empty transitiveDependence.");
						for (Transition tran : transitiveDependence.keySet()) {
				System.out.println("transitive dependency for tran = " + tran.getFullLabel());
				for (Transition depTran : transitiveDependence.get(tran)) {
					System.out.println("\t" + depTran.getFullLabel());
				}
			}
		}
		
	}
}
