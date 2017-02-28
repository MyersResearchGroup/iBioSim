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
package backend.verification.platu.markovianAnalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;

import backend.verification.platu.main.Options;
import backend.verification.platu.stategraph.State;
import backend.verification.platu.stategraph.StateGraph;
import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Place;
import dataModels.lpn.parser.Transition;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ProbLocalStateGraph extends StateGraph {
    	
	/**
	 * This map stores each state's outgoing transition rates.
	 */
	private HashMap<State, HashMap<Transition, Double>> nextTranRateMap;

	public ProbLocalStateGraph(LPN lpn) {
    	super(lpn);
    	nextTranRateMap = new HashMap<State, HashMap<Transition, Double>>();
    }
    
    @Override
	public void drawLocalStateGraph() {    	
		try {			
			String graphFileName = null;
			if (Options.getPOR() == null)
				graphFileName = Options.getPrjSgPath() + getLpn().getLabel() + "_local_sg.dot";
			else
				graphFileName = Options.getPrjSgPath() + getLpn().getLabel() + "POR_"+ Options.getCycleClosingMthd() + "_local_sg.dot";
			int size = this.lpn.getVarIndexMap().size();
			String varNames = "";
			for(int i = 0; i < size; i++) {
				varNames = varNames + ", " + this.lpn.getVarIndexMap().getKey(i);
	    	}
			varNames = varNames.replaceFirst(", ", "");
			BufferedWriter out = new BufferedWriter(new FileWriter(graphFileName));
			out.write("digraph G {\n");
			out.write("Inits [shape=plaintext, label=\"<" + varNames + ">\"]\n");
			for (State curState : nextStateMap.keySet()) {
				String markings = intArrayToString("markings", curState);
				String vars = intArrayToString("vars", curState);
				String enabledTrans = boolArrayToString("enabledTrans", curState);
				String curStateName = "S" + curState.getIndex();
				out.write(curStateName + "[shape=\"ellipse\",label=\"" + curStateName + "\\n<"+vars+">" + "\\n<"+enabledTrans+">" + "\\n<"+markings+">" + "\"]\n");
			}			
			for (State curState : nextStateMap.keySet()) {
				HashMap<Transition, State> stateTransitionPair = nextStateMap.get(curState);
				for (Transition curTran : stateTransitionPair.keySet()) {
					String curStateName = "S" + curState.getIndex();
					String nextStateName = "S" + stateTransitionPair.get(curTran).getIndex();
					String curTranName = curTran.getLabel();
					if (curTran.isFail() && !curTran.isPersistent()) 
						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + "\", fontcolor=red]\n");
					else if (!curTran.isFail() && curTran.isPersistent())						
						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + "\", fontcolor=blue]\n");
					else if (curTran.isFail() && curTran.isPersistent())						
						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + "\", fontcolor=purple]\n");
					else 
						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + "\"]\n");
				}
			}
			out.write("}");
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error producing local ProbabilisticState graph as dot file.");
		}
	}

	/**
	 * This method updates the tranVector for the next state and creates the corresponding tranRateVector. If a transition is not enabled,
	 * its corresponding rate is marked as 0. Note that this method ONLY applies to transitions with non-empty delay expressions.  
	 * @param enabledTransAfterFiring
	 * @param newMarking
	 * @param newVariableVector
	 * @param firedTran
	 * @param tranRateMapForNewState 
	 */
	public boolean[] updateTranVector(State state, int[] newMarking, int[] newVariableVector, Transition firedTran, HashMap<Transition, Double> tranRateMapForNewState) {		
		boolean[] tranVectorAfterFiring = state.getTranVector().clone();
		// Disable the fired transition and all of its conflicting transitions. 
		if (firedTran != null) {
			tranVectorAfterFiring[firedTran.getIndex()] = false;
			for (Integer curConflictingTranIndex : firedTran.getConflictSetTransIndices()) {
				tranVectorAfterFiring[curConflictingTranIndex] = false;
			}
		}
		// find newly enabled transition(s) based on the updated markings and variables
		tran_iteration : for (Transition tran : this.lpn.getAllTransitions()) {			
			String tranName = tran.getLabel();
			int tranIndex = tran.getIndex();
			if (Options.getDebugMode()) {
				//    			System.out.println("Checking " + tranName);
			}
			if (this.lpn.getEnablingTree(tranName) != null 
					&& this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(newVariableVector)) == 0.0) {
				if (Options.getDebugMode()) {
					System.out.println(tran.getFullLabel() + " " + "Enabling condition is false.");    			
				}					
				if (tranVectorAfterFiring[tranIndex] && !tran.isPersistent()) {
					tranVectorAfterFiring[tranIndex] = false;
				}		
				continue;
			}
			if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
				for (int place : this.lpn.getPresetIndex(tranName)) {
					if (newMarking[place]==0) {
						if (Options.getDebugMode()) {
							System.out.println(tran.getFullLabel() + " " + "Missing a preset token.");
						}
						if (tranVectorAfterFiring[tranIndex]) {
							tranVectorAfterFiring[tranIndex] = false;
						}							
						continue tran_iteration;
					}
				}
			}			
			if (this.lpn.getTransitionRateTree(tranName) != null) {
				double tranRate = this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(newVariableVector));										
				if (tranRate == 0.0) {
					if (Options.getDebugMode()) {
						System.out.println(tran.getFullLabel() + " Rate is zero.");
					}
					if (tranVectorAfterFiring[tranIndex]) {
						tranVectorAfterFiring[tranIndex] = false;
					}						
					continue;
				}				
				// if a transition passes all tests above, it needs to be marked as enabled.
				tranVectorAfterFiring[tranIndex] = true;
				tranRateMapForNewState.put(tran, tranRate);
				if (Options.getDebugMode()) {
					System.out.println(tran.getFullLabel() + " is Enabled.");
					if (Options.getMarkovianModelFlag()) {
						System.out.println("Added a new entry below to the inner transition rate map.");
						System.out.println("tran = " + tran.getFullLabel() + ", rate = " + tranRate);
						System.out.println("-------------------------");
					}
				}	
			}
			else {
				System.out.println("Transition " + tran.getFullLabel() + "has empty delay expression.");
				new NullPointerException().printStackTrace();
			}				
		}
		return tranVectorAfterFiring;
	}
    
//	/**
//	 * Add firedTran, its rate, and nextSt to the next state tuple map. 
//	 * @param curSt
//	 * @param firedTran
//	 * @param firedTranRate
//	 * @param nextSt
//	 */
//	public void addStateTran(State curSt, Transition firedTran, double firedTranRate, State nextSt) {
//		HashMap<Transition, ProbLocalStateTuple> nextStTupleMap = this.nextProbLocalStateTupleMap.get(curSt);
//		if(nextStTupleMap == null)  {    		
//			nextStTupleMap = new HashMap<Transition, ProbLocalStateTuple>();	
//			nextStTupleMap.put(firedTran, new ProbLocalStateTuple(nextSt, firedTranRate));
//			this.nextProbLocalStateTupleMap.put(curSt, nextStTupleMap);
//		}
//		else 
//			nextStTupleMap.put(firedTran, new ProbLocalStateTuple(nextSt, firedTranRate));    	
//		if (Options.getDebugMode()) {
//			System.out.println("**** Added <" + firedTran.getFullLabel() + ", S" + nextSt.getIndex() + "(" + curSt.getLpn().getLabel() 
//					+")> to the next state tuple map of state S" + curSt.getIndex() + "(" + curSt.getLpn().getLabel() +")");    
//		}
//	}
    
//    public State getNextState(State curSt, Transition firedTran) {
//    	HashMap<Transition, ProbLocalStateTuple> nextMap = this.nextProbLocalStateTupleMap.get(curSt);    	
//    	if(nextMap == null || nextMap.get(firedTran) == null || nextMap.get(firedTran).getNextProbLocalState() == null)
//    		return null; 
//    	return nextMap.get(firedTran).getNextProbLocalState();
//    }
    
//    public ProbLocalStateTuple getNextStateTuple(State curState,
//    		Transition firedTran) {
//    	HashMap<Transition, ProbLocalStateTuple> nextMap = this.nextProbLocalStateTupleMap.get(curState);    	
//    	if(nextMap == null || nextMap.get(firedTran) == null)
//    		return null; 
//    	return nextMap.get(firedTran);    	
//    }
    
//    public HashMap<State, HashMap<Transition, ProbLocalStateTuple>> getNextProbLocalStateTupleMap() {
//    	return this.nextProbLocalStateTupleMap;
//    }
    
	@Override
	public State genInitialState() {	
		// create initial vector
		int size = this.lpn.getVarIndexMap().size();
		int[] initVariableVector = new int[size];
		for(int i = 0; i < size; i++) {
			String var = this.lpn.getVarIndexMap().getKey(i);
			int val = this.lpn.getInitVariableVector(var);
			initVariableVector[i] = val;
		}
		HashMap<Transition, Double> initTranRateMap = new HashMap<Transition, Double>();
		boolean[] initTranVector = genInitTranVector(initVariableVector, initTranRateMap);
		this.init = new ProbLocalState(this.lpn, this.lpn.getInitialMarkingsArray(), initVariableVector, initTranVector);
		this.addTranRate(this.init, initTranRateMap);		
		if (Options.getDebugMode())
			printNextProbLocalTranRateMapForGivenState(this.init, "ProbLocalStateGraph.java -> genInitialState()");
		return this.init;
	}
    
    /* (non-Javadoc)
     * @see verification.platu.stategraph.StateGraph#fire(verification.platu.stategraph.StateGraph, verification.platu.stategraph.State, lpn.parser.Transition)
     * Fire a transition, generate the local next state (if it is not created yet) from the current local state. Also, update the enabled transition vector
     * and its corresponding tranRate vector.
     */
    @Override
	public State fire(final StateGraph thisSg, final State curState, Transition firedTran){
    	State nextState = thisSg.getNextState(curState, firedTran);
    	if(nextState != null) {
    		if (Options.getDebugMode()) {
    			System.out.println("@ ProbLocalStateGraph.java -> fire(StateGraph, State, Transition), nextState is not null. nextState = S" 
    					+ nextState.getIndex() + "("+ nextState.getLpn().getLabel() + ")");
    		}			
    		return nextState;
    	}
    	// If no cached next state exists, do regular firing. 
    	// Marking update
    	int[] curOldMarking = curState.getMarking();
    	int[] curNewMarking = null;
    	if(firedTran.getPreset().length==0 && firedTran.getPostset().length==0)
    		curNewMarking = curOldMarking;
    	else {
    		curNewMarking = new int[curOldMarking.length];	
    		curNewMarking = curOldMarking.clone();
    		for (int prep : this.lpn.getPresetIndex(firedTran.getLabel())) {
    			curNewMarking[prep]=0;
    		}
    		for (int postp : this.lpn.getPostsetIndex(firedTran.getLabel())) {
    			curNewMarking[postp]=1;
    		}
    	}

    	//  Variable vector update
    	int[] newVariableVector = curState.getVariableVector().clone();
    	int[] curVector = curState.getVariableVector();
    	HashMap<String, String> currentValuesAsString = this.lpn.getAllVarsWithValuesAsString(curVector);
    	for (String key : currentValuesAsString.keySet()) {
    		if (this.lpn.getBoolAssignTree(firedTran.getLabel(), key) != null) {
    			int newValue = (int)this.lpn.getBoolAssignTree(firedTran.getLabel(), key).evaluateExpr(currentValuesAsString);
    			newVariableVector[this.lpn.getVarIndexMap().get(key)] = newValue;
    		}
    		if (this.lpn.getIntAssignTree(firedTran.getLabel(), key) != null) {
    			int newValue = (int)this.lpn.getIntAssignTree(firedTran.getLabel(), key).evaluateExpr(currentValuesAsString);
    			newVariableVector[this.lpn.getVarIndexMap().get(key)] = newValue;
    		}
    	} 
    	// Enabled transition vector update
    	HashMap<Transition, Double> tranRateMapForNewState = new HashMap<Transition, Double>();
   		boolean[] newTranVector = updateTranVector(curState, curNewMarking, newVariableVector, firedTran, tranRateMapForNewState);
    	State newState = thisSg.addState(new ProbLocalState(this.lpn, curNewMarking, newVariableVector, newTranVector));
    	// TODO: (future) assertions in our LPN?
    	/*
    	int[] newVector = newState.getVector();
		for(Expression e : assertions){
	    	if(e.evaluate(newVector) == 0){
	    		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
	    		System.exit(1);
	    	}
    	}
    	*/    	
    	thisSg.addStateTran(curState, firedTran, newState);
    	((ProbLocalStateGraph) thisSg).addTranRate(newState, tranRateMapForNewState);  	
    	if (Options.getDebugMode()) {
    		String location = "ProbLocalStateGraph.java, overriden fire(ProbLocalStateGraph, State, Transition)";
    		thisSg.printNextStateForGivenState(curState, location);
    		((ProbLocalStateGraph) thisSg).printNextProbLocalTranRateMapForGivenState(newState, location);    		
    	}
    	return newState;
    }
    

    /* (non-Javadoc)
     * @see verification.platu.stategraph.StateGraph#genInitTranVector(int[])
     * Find enabled transitions in the initial state, and construct the tranVector in this state.
     */
    public boolean[] genInitTranVector(int[] initVariableVector, HashMap<Transition,Double> initTranRateMap) {
    	boolean[] initTranVector = new boolean[this.lpn.getAllTransitions().length];    	
    	tran_outter_loop: for (int i=0; i< this.lpn.getAllTransitions().length; i++) {
    		Transition tran = this.lpn.getAllTransitions()[i];
    		Place[] tranPreset = this.lpn.getTransition(tran.getLabel()).getPreset(); 
    		String tranName = tran.getLabel();
    		if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
    			for (int j=0; j<tranPreset.length; j++) {
    				if (!tranPreset[j].isMarked()) {
    					initTranVector[i] = false;	    					
    					continue tran_outter_loop;
    				}
    			}
    		}
    		if (this.lpn.getEnablingTree(tranName) != null && this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(initVariableVector)) == 0.0) {
    			initTranVector[i] = false;    			
    			continue;
    		}
    		if (this.lpn.getTransitionRateTree(tranName) != null) {
//    			// ------- TEMP ---------
//				System.out.println("---- @ ProbLocalStateGraph.java -> getInitTranVector -----");
//				System.out.println("Printout results of getAllVarsWithVarValuesAsString(int[]) call in LhpnFile.");
//				for (String varName : this.lpn.getAllVarsWithValuesAsString(initVariableVector).keySet()) {
//					String varValue = this.lpn.getAllVarsWithValuesAsString(initVariableVector).get(varName);
//					System.out.println(varName + " = " + varValue);
//				}
//				System.out.println("-------------------------------------------------");
//				// ----------------------
    			double tranRate = this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(initVariableVector));		
    			if (tranRate == 0.0) {
    				if (Options.getDebugMode()) {
    					//						System.out.println("Rate is zero");
    				}
    				initTranVector[i] = false;    				
    				continue;
    			}
    			initTranRateMap.put(tran, tranRate);
    			initTranVector[i] = true;    			
    		}
    		else {
    			System.out.println("Transition " + tran.getFullLabel() + "has empty delay expression.");
    			new NullPointerException().printStackTrace();
    			System.exit(1);
    		}
    	}
    	return initTranVector;
    }
    

    /* (non-Javadoc)
     * @see verification.platu.stategraph.StateGraph#fire(verification.platu.stategraph.StateGraph[], verification.platu.stategraph.State[], lpn.parser.Transition)
     * This method executes firedTran first by calling fire(StateGraph, State, Transition), and then it updates the affected state graphs.
     */
    @Override
	public State[] fire(final StateGraph[] curSgArray, final State[] curStateArray, Transition firedTran){
    	int thisLpnIndex = this.getLpn().getLpnIndex(); 
    	State[] nextStateArray = curStateArray.clone();    	
    	State curState = curStateArray[thisLpnIndex];
    	State nextState = this.fire(curSgArray[thisLpnIndex], curState, firedTran);    	
    	// TODO: (future) assertions in our LPN?
    	/*
        for(Expression e : assertions){
        	if(e.evaluate(nextVector) == 0){
        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
        		System.exit(1);
			}
		}
    	 */
    	nextStateArray[thisLpnIndex] = nextState;
    	if(firedTran.isLocal()) {
    		return nextStateArray;
    	}	
    	HashMap<String, Integer> vvSet = new HashMap<String, Integer>();
    	vvSet = this.lpn.getAllVarsWithValuesAsInt(nextState.getVariableVector());
    	// update state graphs that are affected by firedTran.
    	// TODO: Need to deal with immediate transitions. 
    	for(LPN curLPN : firedTran.getDstLpnList()) {
    		int curIdx = curLPN.getLpnIndex();
    		State newState = curSgArray[curIdx].getNextState(curStateArray[curIdx], firedTran);
    		if(newState != null) {
    			nextStateArray[curIdx] = newState;
    		}     		
    		else {    			
    			State newOther = ((ProbLocalState) curStateArray[curIdx]).update(curSgArray[curIdx], vvSet, curSgArray[curIdx].getLpn().getVarIndexMap());
    			if (newOther == null)
    				nextStateArray[curIdx] = curStateArray[curIdx];
    			else {
    				State cachedOther = curSgArray[curIdx].addState(newOther);
    				nextStateArray[curIdx] = cachedOther;    				
    				curSgArray[curIdx].addStateTran(curStateArray[curIdx], firedTran, cachedOther);
    				double firedTranRate = ((ProbLocalStateGraph) curSgArray[thisLpnIndex]).getTranRate(curState, firedTran);
					((ProbLocalStateGraph) curSgArray[curIdx]).addTranRate(curStateArray[curIdx], firedTran, firedTranRate);
					if (Options.getDebugMode()) {
						String location = "ProbLocalStateGraph.java, overriden fire(StateGraph[], State[], Transition)";
						curSgArray[curIdx].printNextStateForGivenState(curStateArray[curIdx], location);
						((ProbLocalStateGraph) curSgArray[curIdx]).printNextProbLocalTranRateMapForGivenState(curStateArray[curIdx], location);    		
					}
    			}   
    		}
    	}	
    	return nextStateArray;
    }

   	/**
	 * Add all transitions and their corresponding rates for the nextState to the nextTranRateMap.  
	 * @param nextState
	 * @param nextStateTranRateMap
	 */
	public void addTranRate(State nextState, HashMap<Transition, Double> nextStateTranRateMap) {
		HashMap<Transition, Double> innerMap = this.nextTranRateMap.get(nextState);
		if (innerMap == null) {
//			innerMap = new HashMap<Transition, Double>();
//			innerMap.putAll(nextStateTranRateMap);
//			this.nextTranRateMap.put(nextState, innerMap);
			this.nextTranRateMap.put(nextState, nextStateTranRateMap);
		}
		else {
			// TODO: Need to remove this step.
			innerMap.putAll(nextStateTranRateMap);			
		}		
		if (Options.getDebugMode())
			printNextProbLocalTranRateMapForGivenState(nextState, "ProbLocalStateGraph.java -> (public) addTranRate(). Adding state " + nextState.getFullLabel() + " to the map.");
	}
	
	/**
	 * Add a single entry to the nextTranRateMap. 
	 * @param curState
	 * @param firedTran
	 * @param tranRate
	 */
	private void addTranRate(State curState, Transition firedTran,
			double tranRate) {
		HashMap<Transition, Double> innerMap = nextTranRateMap.get(curState);
		if (innerMap != null) {
			innerMap.put(firedTran, tranRate);
		}
		else {
			innerMap = new HashMap<Transition, Double>();
			innerMap.put(firedTran, tranRate);
			nextTranRateMap.put(curState, innerMap);
		}
//		if (Options.getDebugMode())
//			printNextProbLocalTranRateMapForGivenState(curState, "ProbLocalStateGraph.java -> (private) addTranRate(). Adding state " + curState.getFullLabel() + " to the map.");
	}

	public double getTranRate(State curLocalState, Transition tran) {
		if (nextTranRateMap.get(curLocalState) == null) {
			System.out.println("No entry for " + curLocalState.getFullLabel() + " in nextTranRateMap");
			printNextProbLocalTranRateMapForGivenState(curLocalState, "ProbLocalStateGraph.java -> getTranRate()_0");
			NullPointerException npe = new NullPointerException("Next probabilistic local state is null.");
			npe.printStackTrace();  
		}
		else if (nextTranRateMap.get(curLocalState).get(tran) == null) {			
			System.out.println("No entry for " + curLocalState.getFullLabel() + " and transition " + tran.getFullLabel() + " in nextTranRateMap");
			curLocalState.printStateInfo();			
			printNextProbLocalTranRateMapForGivenState(curLocalState, "ProbLocalStateGraph.java -> getTranRate()_1");
			NullPointerException npe1 = new NullPointerException("Next transition rate is null.");
    		npe1.printStackTrace();	 
		}
		return nextTranRateMap.get(curLocalState).get(tran);
	}
	
//	public void setTranRate(State curLocalState, Transition tran, double tranRate) {
//		nextTranRateMap.get(curLocalState).put(tran, tranRate);
//	}
	
//	public HashMap<State, HashMap<Transition, Double>> getNextTranRateMap() {
//		return nextTranRateMap;
//	}
  
	public void printNextProbLocalTranRateMapForGivenState(State givenState, String location) {    	
    	System.out.println("----------------Next Tran Rate Map @ " + location + "----------------");
    	System.out.println("state = " + givenState.getFullLabel());
    	HashMap<Transition, State> nextStateMapForGivenState = nextStateMap.get(givenState);
    	HashMap<Transition, Double> nextTranRateMapForGivenState = nextTranRateMap.get(givenState);
    	if (nextStateMapForGivenState == null) {    		
    		System.out.println("No entry for " + givenState.getFullLabel() + " in nextStateMap");
    	}
    	else {
    		for (Transition t: nextStateMapForGivenState.keySet()) {
    			State nextState = nextStateMapForGivenState.get(t);    			
   				System.out.println(t.getFullLabel() + " -> S" + nextState.getIndex() + "(" + nextState.getLpn().getLabel() + ")");
    		}
    	}	
    	if (nextTranRateMapForGivenState == null) {    		
    		System.out.println("No entry for " + givenState.getFullLabel() + " in nextTranRateMap");
    	}    		
    	else {
    		for (Transition t: nextTranRateMapForGivenState.keySet()) {
    			Double tranRate = nextTranRateMapForGivenState.get(t);
   				System.out.println("tran = " + t.getFullLabel() + ", rate = " + tranRate);
    		}
    	}
    	System.out.println("--------------End Of Next Tran Rate Map----------------------");
    }
}
