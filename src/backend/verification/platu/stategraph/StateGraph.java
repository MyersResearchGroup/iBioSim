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
package backend.verification.platu.stategraph;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import backend.verification.platu.common.IndexObjMap;
import backend.verification.platu.logicAnalysis.Constraint;
import backend.verification.platu.main.Main;
import backend.verification.platu.main.Options;
import backend.verification.platu.platuLpn.DualHashMap;
import backend.verification.platu.platuLpn.LpnTranList;
import backend.verification.platu.project.PrjState;
import backend.verification.timed_state_exploration.octagon.Equivalence;
import backend.verification.timed_state_exploration.zoneProject.ContinuousRecordSet;
import backend.verification.timed_state_exploration.zoneProject.ContinuousUtilities;
import backend.verification.timed_state_exploration.zoneProject.Event;
import backend.verification.timed_state_exploration.zoneProject.EventSet;
import backend.verification.timed_state_exploration.zoneProject.InequalityVariable;
import backend.verification.timed_state_exploration.zoneProject.IntervalPair;
import backend.verification.timed_state_exploration.zoneProject.LPNContAndRate;
import backend.verification.timed_state_exploration.zoneProject.LPNContinuousPair;
import backend.verification.timed_state_exploration.zoneProject.LPNTransitionPair;
import backend.verification.timed_state_exploration.zoneProject.TimedPrjState;
import backend.verification.timed_state_exploration.zoneProject.UpdateContinuous;
import backend.verification.timed_state_exploration.zoneProject.Zone;
import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Place;
import dataModels.lpn.parser.Transition;
import dataModels.lpn.parser.Variable;
import dataModels.util.GlobalConstants;

import java.util.Set;
import java.util.Stack;


public class StateGraph {

	protected State init = null;
    protected IndexObjMap<State> stateCache;
    //protected IndexObjMap<State> localStateCache;
    protected HashMap<State, State> state2LocalMap;
    /**
     * This field variable is inherited from Hao's code. His state does not store enabled transition array, and he uses this map to store
     * enabled transitions in each local state. Since we store enabled transitions in each of our local state, the use of this map for storing
     * enabled transitions is redundant. This field variable is used to store persistent sets for each local state instead. 
     */
    protected HashMap<State, LpnTranList> enabledSetTbl;
    protected HashMap<State, HashMap<Transition, State>> nextStateMap;
    protected List<State> stateSet = new LinkedList<State>();
    protected List<State> frontierStateSet = new LinkedList<State>();
    protected List<State> entryStateSet = new LinkedList<State>();
    protected List<Constraint> oldConstraintSet = new LinkedList<Constraint>();
    protected List<Constraint> newConstraintSet = new LinkedList<Constraint>();
    protected List<Constraint> frontierConstraintSet = new LinkedList<Constraint>();
    protected Set<Constraint> constraintSet = new HashSet<Constraint>();
    protected LPN lpn;
    protected static Set<Entry<Transition, State>> emptySet = new HashSet<Entry<Transition, State>>(0);
    private String separator = GlobalConstants.separator;
    
    public StateGraph(LPN lpn) {
    	this.lpn = lpn;   	
        this.stateCache = new IndexObjMap<State>();        
        //this.localStateCache = new IndexObjMap<State>();
        this.state2LocalMap = new HashMap<State, State>();
        this.enabledSetTbl = new HashMap<State, LpnTranList>();        
       	this.nextStateMap = new HashMap<State, HashMap<Transition, State>>();   
    }
    
    public LPN getLpn(){
    	return this.lpn;
    }
    
    public void printStates(){
    	System.out.println(String.format("%-8s    %5s", this.lpn.getLabel(), "|States| = " + stateCache.size()));
    }

//    public Set<Transition> getTranList(State currentState){
//    	return this.nextStateMap.get(currentState).keySet();
//    }
    
    /**
     * Finds reachable states from the given state.
     * Also generates new constraints from the state transitions.
     * @param baseState - State to start from
     * @return Number of new transitions.
     */
    public int constrFindSG(final State baseState){
    	boolean newStateFlag = false;
    	int ptr = 1;
        int newTransitions = 0;
        Stack<State> stStack = new Stack<State>();
        Stack<LpnTranList> tranStack = new Stack<LpnTranList>();
        // TODO: What if we just read tranVector from baseState?
        LpnTranList currentEnabledTransitions = getEnabled(baseState);
        stStack.push(baseState);
        tranStack.push(currentEnabledTransitions);
        while (true){
            ptr--;
            State currentState = stStack.pop();
            currentEnabledTransitions = tranStack.pop();
            for (Transition firedTran : currentEnabledTransitions) {
            	//System.out.println("firedTran: " + firedTran.getLabel() + "(" + firedTran.getLpn().getLabel() + ")");
              	State newState = constrFire(firedTran,currentState);
                State nextState = addState(newState);
                newStateFlag = false;
            	if(nextState == newState){
            		addFrontierState(nextState);
            		newStateFlag = true;
            	}
//            	StateTran stTran = new StateTran(currentState, firedTran, state);
            	if(nextState != currentState){
//            		this.addStateTran(currentState, nextState, firedTran);
            		this.addStateTran(currentState, firedTran, nextState);
            		newTransitions++;
            		// TODO: (original) check that a variable was changed before creating a constraint
	            	if(!firedTran.isLocal()){
	            		for(LPN lpn : firedTran.getDstLpnList()){
	            			//TODO: No need to generate constraint for the lpn where firedTran lives. 
	            			if (firedTran.getLpn().equals(lpn))
	            				continue;
	                  		Constraint c = new Constraint(currentState, nextState, firedTran, lpn);
	                  		lpn.getStateGraph().addConstraint(c);
	        			}
	            	}
        		}
            	if(!newStateFlag) 
            		continue;            	
            	LpnTranList nextEnabledTransitions = getEnabled(nextState);
                if (nextEnabledTransitions.isEmpty()) 
                	continue;                
                Transition disabledTran = firedTran.disablingError(currentEnabledTransitions, nextEnabledTransitions);
                if(disabledTran != null) {
                    System.out.println("Verification failed: " +disabledTran.getFullLabel() + " is disabled by " + 
                    			firedTran.getFullLabel());
                    currentState.setFailure();
                    return -1;
                }                
                stStack.push(nextState);
                tranStack.push(nextEnabledTransitions);
                ptr++;
            }
            if (ptr == 0) {
                break;
            }
        }
        return newTransitions;
    }
    
    /**
     * Finds reachable states from the given state.
     * Also generates new constraints from the state transitions.
     * Synchronized version of constrFindSG().  Method is not synchronized, but uses synchronized methods
     * @param baseState State to start from
     * @return Number of new transitions.
     */
    public int synchronizedConstrFindSG(final State baseState){
    	boolean newStateFlag = false;
    	int ptr = 1;
        int newTransitions = 0;
        Stack<State> stStack = new Stack<State>();
        Stack<LpnTranList> tranStack = new Stack<LpnTranList>();
        LpnTranList currentEnabledTransitions = getEnabled(baseState);

        stStack.push(baseState);
        tranStack.push(currentEnabledTransitions);
        while (true){
            ptr--;
            State currentState = stStack.pop();
            currentEnabledTransitions = tranStack.pop();
            
            for (Transition firedTran : currentEnabledTransitions) {
                State st = constrFire(firedTran,currentState);
                State nextState = addState(st);

                newStateFlag = false;
            	if(nextState == st){
            		newStateFlag = true;
            		addFrontierState(nextState);
            	}
        		
            	if(nextState != currentState){
	        		newTransitions++;
	        		
	            	if(!firedTran.isLocal()){
	            		// TODO: (original) check that a variable was changed before creating a constraint
	            		for(LPN lpn : firedTran.getDstLpnList()){
	                  		Constraint c = new Constraint(currentState, nextState, firedTran, lpn);
	                  		lpn.getStateGraph().synchronizedAddConstraint(c);
	        			}
	  
	            	}
            	}

            	if(!newStateFlag)
            		continue;
            	
            	LpnTranList nextEnabledTransitions = getEnabled(nextState);
                if (nextEnabledTransitions == null || nextEnabledTransitions.isEmpty()) {
                    continue;
                }
                
//                currentEnabledTransitions = getEnabled(nexState);
//                Transition disabledTran = firedTran.disablingError(currentEnabledTransitions, nextEnabledTransitions);
//                if(disabledTran != null) {
//                    prDbg(10, "Verification failed: " +disabledTran.getFullLabel() + " is disabled by " + 
//                    			firedTran.getFullLabel());
//                   
//                    currentState.setFailure();
//                    return -1;
//                }
                
                stStack.push(nextState);
                tranStack.push(nextEnabledTransitions);
                ptr++;
            }

            if (ptr == 0) {
                break;
            }
        }

        return newTransitions;
    }
    
    public List<State> getFrontierStateSet(){
    	return this.frontierStateSet;
    }
    
    public List<State> getStateSet(){
    	return this.stateSet;
    }
    
    public void addFrontierState(State st){
    	this.entryStateSet.add(st);
    }
    
    public List<Constraint> getOldConstraintSet(){
    	return this.oldConstraintSet;
    }
    
    /**
	 * Adds constraint to the constraintSet.
	 * @param c - Constraint to be added.
	 * @return True if added, otherwise false.
	 */
    public boolean addConstraint(Constraint c){
    	if(this.constraintSet.add(c)){
    		this.frontierConstraintSet.add(c);
    		return true;
    	}
    	
    	return false;
    }
    
    /**
	 * Adds constraint to the constraintSet.  Synchronized version of addConstraint().
	 * @param c - Constraint to be added.
	 * @return True if added, otherwise false.
	 */
    public synchronized boolean synchronizedAddConstraint(Constraint c){
    	if(this.constraintSet.add(c)){
    		this.frontierConstraintSet.add(c);
    		return true;
    	}
    	
    	return false;
    }
    
    public List<Constraint> getNewConstraintSet(){
    	return this.newConstraintSet;
    }
    
    public void genConstraints(){
    	oldConstraintSet.addAll(newConstraintSet);
    	newConstraintSet.clear();
    	newConstraintSet.addAll(frontierConstraintSet);
    	frontierConstraintSet.clear();
    }
    
    
    public void genFrontier(){
    	this.stateSet.addAll(this.frontierStateSet);
    	this.frontierStateSet.clear();
    	this.frontierStateSet.addAll(this.entryStateSet);
    	this.entryStateSet.clear();
    }
    
    public void setInitialState(State init){
    	this.init = init;
    }
    
    public State getInitialState(){
    	return this.init;
    }
    
    // Hao's method
    public void draw(){
    	String dotFile = Options.getDotPath();
		if(!dotFile.endsWith("/") && !dotFile.endsWith("\\")){
			String dirSlash = "/";
			if(Main.isWindows) dirSlash = "\\";
			
			dotFile = dotFile += dirSlash;
		}
		
		dotFile += this.lpn.getLabel() + ".dot";
    	PrintStream graph = null;
    	
		try {
			graph = new PrintStream(new FileOutputStream(dotFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}
    	
    	graph.println("digraph SG{");
    	//graph.println("  fixedsize=true");
    	
    	int size = this.lpn.getAllOutputs().size() + this.lpn.getAllInputs().size() + this.lpn.getAllInternals().size();
    	String[] variables = new String[size];
    	
    	DualHashMap<String, Integer> varIndexMap = this.lpn.getVarIndexMap(); 
    	
    	int i;
    	for(i = 0; i < size; i++){
    		variables[i] = varIndexMap.getKey(i);
    	}
    	
    	//for(State state : this.reachableSet.keySet()){
    	for(int stateIdx = 0; stateIdx < this.reachSize(); stateIdx++) {
    		State state = this.getState(stateIdx);
    		String dotLabel = state.getIndex() + ": ";
    		int[] vector = state.getVariableVector();

    		for(i = 0; i < size; i++){
    			dotLabel += variables[i];

        		if(vector[i] == 0) dotLabel += "'";
        		
        		if(i < size-1) dotLabel += " ";
    		}
    		
    		int[] mark = state.getMarking();
    		
    		dotLabel += "\\n";
    		for(i = 0; i < mark.length; i++){
    			if(i == 0) dotLabel += "[";
    			
    			dotLabel += mark[i];
    			
    			if(i < mark.length - 1)
    				dotLabel += ", ";
    			else
    				dotLabel += "]";
    		}

    		String attributes = "";
    		if(state == this.init) attributes += " peripheries=2";
    		if(state.failure()) attributes += " style=filled fillcolor=\"red\"";
    		
    		graph.println("  " + state.getIndex() + "[shape=ellipse width=.3 height=.3 " +
					"label=\"" + dotLabel + "\"" + attributes + "]");
    		
    		
    		for(Entry<Transition, State> stateTran : this.nextStateMap.get(state).entrySet()){
    			State tailState = state;
    			State headState = stateTran.getValue();
    			Transition lpnTran = stateTran.getKey();
    			
    			String edgeLabel = lpnTran.getLabel() + ": ";
        		int[] headVector = headState.getVariableVector();
        		int[] tailVector = tailState.getVariableVector();
        		
        		for(i = 0; i < size; i++){
            		if(headVector[i] != tailVector[i]){
            			if(headVector[i] == 0){
            				edgeLabel += variables[i];
            				edgeLabel += "-";
            			}
            			else{
            				edgeLabel += variables[i];
            				edgeLabel += "+";
            			}
            		}
        		}
        		
        		graph.println("  " + tailState.getIndex() + " -> " + headState.getIndex() + "[label=\"" + edgeLabel + "\"]");
    		}
    	}
    	
    	graph.println("}");
	    graph.close();
    }
    
    /**
     * Return the enabled transitions in the state with index 'stateIdx'.
     * @param stateIdx
     * @return
     */
    public LpnTranList getEnabled(int stateIdx) {
    	State curState = this.getState(stateIdx);
        return this.getEnabled(curState);
    }
    /**
     * Return the set of all LPN transitions that are enabled in the given state.
     * @param curState
     * @return
     */
    public LpnTranList getEnabled(State curState) {
    	if (curState == null) {
            throw new NullPointerException();
        }
    	
    	if(enabledSetTbl.containsKey(curState) == true){
    		// TODO: need to return a clone?
    		return enabledSetTbl.get(curState).clone();
    	}
    	
        LpnTranList curEnabled = new LpnTranList();
        for (Transition tran : this.lpn.getAllTransitions()) {
        	if (isEnabled(tran,curState)) {
        		if(tran.isLocal()==true)
        			curEnabled.addLast(tran);
                else
                	curEnabled.addFirst(tran);
             } 
        }
        this.enabledSetTbl.put(curState, curEnabled);
        return curEnabled;
    }

    /**
     * Return the set of all enabled local transitions in the current local state. The enabled transitions 
     * are read from the tranVector in current state. 
     * @param curState
     * @return
     */
    public static LpnTranList getEnabledFromTranVector(State curState) {
    	if (curState == null) {
    		throw new NullPointerException();
    	}   	
    	LpnTranList curEnabled = curState.getEnabledTransitions();
    	return curEnabled;
    }
    
//    protected void printEnabledSetTblToDebugFile() {
//    	System.out.println("******* enabledSetTbl**********");
//    	for (State s : enabledSetTbl.keySet()) {
//    		System.out.print("S" + s.getIndex() + " -> ");
//    		printTransitionSet(enabledSetTbl.get(s), "");
//    	}	
//	}

    protected static void printTransitionSet(LpnTranList transitionSet, String setName) {
		if (!setName.isEmpty()) 
			System.out.println(setName + " : ");
		if (transitionSet.isEmpty()) {
			System.out.println("empty");
		}
		else {
			for (Transition tran : transitionSet) {
				System.out.print(tran.getFullLabel() + ", ");
			}
			System.out.println("");
		}
	}
	

	private boolean isEnabled(Transition tran, State curState) {	   	
			int[] varValuesVector = curState.getVariableVector();
			String tranName = tran.getLabel();
			int tranIndex = tran.getIndex();
			if (Options.getDebugMode()) {
//				System.out.println("Checking " + tran);
			}				
			if (this.lpn.getEnablingTree(tranName) != null 
					&& this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(varValuesVector)) == 0.0
					&& !(tran.isPersistent() && curState.getTranVector()[tranIndex])) {
				if (Options.getDebugMode()) {
//					System.out.println(tran.getName() + " " + "Enabling condition is false");
				}	
				return false;
			}
			if (this.lpn.getTransitionRateTree(tranName) != null 
					&& this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(varValuesVector)) == 0.0) {
				if (Options.getDebugMode()) {
//					System.out.println("Rate is zero");
				}					
				return false;
			}
			if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
				int[] curMarking = curState.getMarking();
				for (int place : this.lpn.getPresetIndex(tranName)) {
					if (curMarking[place]==0) {
						if (Options.getDebugMode()) {
//							System.out.println(tran.getName() + " " + "Missing a preset token");
						}							
						return false;
					}
				}
				// if a transition is enabled and it is not recorded in the enabled transition vector
				curState.getTranVector()[tranIndex] = true;
			}
		return true;
    }
    
	public int reachSize() {
    	if(this.stateCache == null){
    		return this.stateSet.size();
    	}
    	
		return this.stateCache.size();
    }
    
	public static boolean stateOnStack(State curState, HashSet<PrjState> stateStack) {
		boolean isStateOnStack = false;
		for (PrjState prjState : stateStack) {
			State[] stateArray = prjState.toStateArray();
			for (State s : stateArray) {
				if (s == curState) {
					isStateOnStack = true;
					break;
				}
			}
			if (isStateOnStack) 
				break;
		}
		return isStateOnStack;
	}
	
    /**
     * Add the module state mState to the local cache, and also add its local portion to
     * the local portion cache, and build the mapping between the mState and lState for fast lookup
     * in the future.
     * @param mState
     * @return State
     */
    public State addState(State mState) {
    	State cachedState = this.stateCache.add(mState);
    	State lState = this.state2LocalMap.get(cachedState);
    	if(lState == null) {
    		lState = cachedState.getLocalState();
    		//lState = this.localStateCache.add(lState);
    		this.state2LocalMap.put(cachedState, lState);
    	}
    	return cachedState;
    }

    /*
     * Get the local portion of mState from the cache..
     */
    public State getLocalState(State mState) {
    	return this.state2LocalMap.get(mState);
    }

    public State getState(int stateIdx) {
    	return this.stateCache.get(stateIdx);
    }
    
    public void addStateTran(State curSt, Transition firedTran, State nextSt) {
    	HashMap<Transition, State> nextMap = this.nextStateMap.get(curSt);
    	if(nextMap == null)  {
    		nextMap = new HashMap<Transition,State>();
    		nextMap.put(firedTran, nextSt);
    		this.nextStateMap.put(curSt, nextMap);
    	}
    	else {
    		nextMap.put(firedTran, nextSt);
    	}
//    	if (Options.getDebugMode()) 
//    		printNextStateForGivenState(curSt, "StateGraph.java -> addStateTran(State, Transition, State)");    	
    }
    
    public State getNextState(State curSt, Transition firedTran) {
    	HashMap<Transition, State> nextMap = this.nextStateMap.get(curSt);
    	if(nextMap == null)
    		return null;   	
    	return nextMap.get(firedTran);
    }
    
    // Hao's method
    public Set<Entry<Transition, State>> getOutgoingTrans(State currentState){
    	HashMap<Transition, State> tranMap = this.nextStateMap.get(currentState);
    	if(tranMap == null){
    		return emptySet;
    	}    	    	
    	return tranMap.entrySet();
    }
    
    public int numConstraints(){
    	if(this.constraintSet == null){
    		return this.oldConstraintSet.size();
    	}
    	
    	return this.constraintSet.size();
    }
    
    public void clear(){
    	this.constraintSet.clear();
    	this.frontierConstraintSet.clear();
    	this.newConstraintSet.clear();
    	this.frontierStateSet.clear();
    	this.entryStateSet.clear();
    	
    	this.constraintSet = null;
    	this.frontierConstraintSet = null;
    	this.newConstraintSet = null;
    	this.frontierStateSet = null;
    	this.entryStateSet = null;
    	this.stateCache = null;
    }
    
    public State genInitialState() {	
    	// create initial vector: vector for variable initial values. 
		int size = this.lpn.getVarIndexMap().size();
    	int[] initVariableVector = new int[size];
    	for(int i = 0; i < size; i++) {
    		String var = this.lpn.getVarIndexMap().getKey(i);
    		int val = this.lpn.getInitVariableVector(var);
    		initVariableVector[i] = val;
    	}
    	// Initial state can contain immediate transitions.
    	this.init = new State(this.lpn, this.lpn.getInitialMarkingsArray(), initVariableVector, genInitTranVector(initVariableVector));
    	return this.init;
    }

	/**
	 * If an immediate transition is enabled in the current state, 
	 * returns the first enabled immediate transition of curState, otherwise return null. 
	 * @param curState
	 * @return
	 */
	public static Transition getEnabledImmediateTran(State curState) {
		Transition enabledImmTran = null;
		for (Transition enabledTran : curState.getEnabledTransitions()) {
			if (enabledTran.getDelay() == null) {
				enabledImmTran = enabledTran;
				break;
			}
		}		
		return enabledImmTran;
	}

	/**
     * Fire a transition on a state array, find new local states, and return the new state array formed by the new local states.
     * @param firedTran 
     * @param curLpnArray
     * @param curStateArray
     * @param curLpnIndex
     * @return
     */
    
    public State[] fire(final StateGraph[] curSgArray, final int[] curStateIdxArray, Transition firedTran) {
    	State[] stateArray = new State[curSgArray.length];
    	for(int i = 0; i < curSgArray.length; i++)
    		stateArray[i] = curSgArray[i].getState(curStateIdxArray[i]);

    	return this.fire(curSgArray, stateArray, firedTran);
    }

     /**
     * This method is called by untimed search_dfs(StateGraph[], State[]).
     * @param curSgArray
     * @param curStateArray
     * @param firedTran
     * @return
     */
    public State[] fire(final StateGraph[] curSgArray, final State[] curStateArray, Transition firedTran){
//    		HashMap<LPNContinuousPair, IntervalPair> continuousValues, Zone z) {
//    public State[] fire(final StateGraph[] curSgArray, final State[] curStateArray, Transition firedTran,
//    		ArrayList<HashMap<LPNContinuousPair, IntervalPair>> newAssignValues, Zone z) {
//    public State[] fire(final StateGraph[] curSgArray, final State[] curStateArray, Transition firedTran,
//    		ArrayList<HashMap<LPNContAndRate, IntervalPair>> newAssignValues, Zone z) {
    	
    	int thisLpnIndex = this.getLpn().getLpnIndex(); 
    	State[] nextStateArray = curStateArray.clone();
    	
    	State curState = curStateArray[thisLpnIndex];
//    	State nextState = this.fire(curSgArray[thisLpnIndex], curState, firedTran, continuousValues, z);   
    	
//    	State nextState = this.fire(curSgArray[thisLpnIndex], curState, firedTran, newAssignValues, z);   
    	State nextState = this.fire(curSgArray[thisLpnIndex], curState, firedTran);
    	
    	//int[] nextVector = nextState.getVector();
    	//int[] curVector = curState.getVector();
    	
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
		if(firedTran.isLocal()==true) {
//    		nextStateArray[thisLpnIndex] = curSgArray[thisLpnIndex].addState(nextState);
        	return nextStateArray;
		}	
        HashMap<String, Integer> vvSet = new HashMap<String, Integer>();
        vvSet = this.lpn.getAllVarsWithValuesAsInt(nextState.getVariableVector());
        // Update other local states with the new values generated for the shared variables.
		//nextStateArray[this.lpn.getIndex()] = nextState;      
//		if (!firedTran.getDstLpnList().contains(this.lpn))
//			firedTran.getDstLpnList().add(this.lpn);
		for(LPN curLPN : firedTran.getDstLpnList()) {
        	int curIdx = curLPN.getLpnIndex();
    		State newState = curSgArray[curIdx].getNextState(curStateArray[curIdx], firedTran);
    		if(newState != null) {
    			nextStateArray[curIdx] = newState;
    		}     		
        	else {
        		State newOther = curStateArray[curIdx].update(curSgArray[curIdx], vvSet, curSgArray[curIdx].getLpn().getVarIndexMap());
        		if (newOther == null)
        			nextStateArray[curIdx] = curStateArray[curIdx];
        		else {
        			State cachedOther = curSgArray[curIdx].addState(newOther);
					//nextStateArray[curIdx] = newOther;
            		nextStateArray[curIdx] = cachedOther;
//        			System.out.println("ADDING TO " + curIdx + ":\n" + curStateArray[curIdx].getIndex() + ":\n" +
//        					curStateArray[curIdx].print() + firedTran.getName() + "\n" + 
//        					cachedOther.getIndex() + ":\n" + cachedOther.print());
            		curSgArray[curIdx].addStateTran(curStateArray[curIdx], firedTran, cachedOther);
					if (Options.getDebugMode()) {
						String location = "StateGraph.java, fire(StateGraph[], State[], Transition)";
						printNextStateForGivenState(curStateArray[curIdx], location);						
					}
        		}   		
        	}
        }	
        return nextStateArray;
    }
    
    
    /**
     * This method is used by untimed search_dfs(StateGraph[], State[]).
     * @param thisSg
     * @param curState
     * @param firedTran
     * @return
     */
    public State fire(final StateGraph thisSg, final State curState, Transition firedTran){ 
//    		HashMap<LPNContinuousPair, IntervalPair> continuousValues, Zone z) {
//    public State fire(final StateGraph thisSg, final State curState, Transition firedTran, 
//    		ArrayList<HashMap<LPNContinuousPair, IntervalPair>> newAssignValues, Zone z) {
//    public State fire(final StateGraph thisSg, final State curState, Transition firedTran, 
//    		ArrayList<HashMap<LPNContAndRate, IntervalPair>> newAssignValues, Zone z) {
    
    	// Search for and return cached next state first. 
//    	if(this.nextStateMap.containsKey(curState) == true)
//    		return (State)this.nextStateMap.get(curState);
    	
    	State nextState = thisSg.getNextState(curState, firedTran);
    	
    	if(Options.getTimingAnalysisFlag()){
    		// This effectively turns off the caching done by
    		// thisSg.getNextState. The caching takes the current state and transition and
    		// outputs the next state if it has been seen before. The problem with this
    		// is that it is agnostic to timing information while the state itself is not
    		// due to having the boolean variables for the inequalities. Thus, it is possible
    		// to have the same current state and transition result in different next states
    		// based on how the continuous portion of the state effects the boolean inequality
    		// variables.
    		nextState = null;
    	}
    	
    	if(nextState != null)
    		return nextState;  	
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

        //  State vector update
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
        
//        // Update rates      
//        final int OLD_ZERO = 0; 	// Case 0 in description.
////		final int NEW_NON_ZERO = 1; // Case 1 in description.
////		final int NEW_ZERO = 2;		// Case 2 in description.
////		final int OLD_NON_ZERO = 3;	// Case 3 in description.
////		newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
////		newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
////		newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
////		newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
//
//        final int NEW_NON_ZERO = 1; // Case 1 in description.
//        final int NEW_ZERO = 2;		// Case 2 in description.
//        final int OLD_NON_ZERO = 3;	// Cade 3 in description.
//        if(Options.getTimingAnalysisFlag()){
//        	newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
//        	newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
//        	newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
//        	newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
//        }
//
//        for(String key : this.lpn.getContVars()){
//        	
//    		// Get the pairing.
//    		int lpnIndex = this.lpn.getLpnIndex();
//    		int contVarIndex = this.lpn.getContVarIndex(key);
//
//    		// Package up the indecies.
//    		LPNContinuousPair contVar = new LPNContinuousPair(lpnIndex, contVarIndex);
//
//    		//Integer newRate = null;
//    		IntervalPair newRate = null;
//    		IntervalPair newValue= null;
//        	
//        	// Check if there is a new rate assignment.
//        	if(this.lpn.getRateAssignTree(firedTran.getName(), key) != null){
//        		// Get the new value.
//        		//IntervalPair newIntervalRate = this.lpn.getRateAssignTree(firedTran.getName(), key)
//        				//.evaluateExprBound(this.lpn.getAllVarsWithValuesAsString(curVector), z, null);
//        		newRate = this.lpn.getRateAssignTree(firedTran.getName(), key)
//        				.evaluateExprBound(currentValuesAsString, z, null);
//        				
////        		// Get the pairing.
////        		int lpnIndex = this.lpn.getLpnIndex();
////        		int contVarIndex = this.lpn.getContVarIndex(key);
////
////        		// Package up the indecies.
////        		LPNContinuousPair contVar = new LPNContinuousPair(lpnIndex, contVarIndex);
//
//        		// Keep the current rate.
//        		//newRate = newIntervalRate.get_LowerBound();
//        		
//        		//contVar.setCurrentRate(newIntervalRate.get_LowerBound());
//        		contVar.setCurrentRate(newRate.get_LowerBound());
//        		
////        		continuousValues.put(contVar, new IntervalPair(z.getDbmEntryByPair(contVar, LPNTransitionPair.ZERO_TIMER_PAIR),
////        				z.getDbmEntryByPair(LPNTransitionPair.ZERO_TIMER_PAIR, contVar)));
//        		
////        		// Check if the new assignment gives rate zero.
////        		boolean newRateZero = newRate == 0;
////        		// Check if the variable was already rate zero.
////        		boolean oldRateZero = z.getCurrentRate(contVar) == 0;
////        		
////        		// Put the new value in the appropriate set.
////        		if(oldRateZero){
////        			if(newRateZero){
////        				// Old rate is zero and the new rate is zero.
////        				newAssignValues.get(OLD_ZERO).put(contVar, newValue);
////        			}
////        			else{
////        				// Old rate is zero and the new rate is non-zero.
////        				newAssignValues.get(NEW_NON_ZERO).put(contVar, newValue);
////        			}
////        		}
////        		else{
////        			if(newRateZero){
////        				// Old rate is non-zero and the new rate is zero.
////        				newAssignValues.get(NEW_ZERO).put(contVar, newValue);
////        			}
////        			else{
////        				// Old rate is non-zero and the new rate is non-zero.
////        				newAssignValues.get(OLD_NON_ZERO).put(contVar, newValue);
////        			}
////        		}
//        	}
//        //}
//        
//        // Update continuous variables.
//        //for(String key : this.lpn.getContVars()){
//        	// Get the new assignments on the continuous variables and update inequalities.
//        	if (this.lpn.getContAssignTree(firedTran.getName(), key) != null) {
////        		int newValue = (int)this.lpn.getContAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(curVector));
////        		newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
//        		
//        		// Get the new value.
//        		newValue = this.lpn.getContAssignTree(firedTran.getName(), key)
//        				//.evaluateExprBound(this.lpn.getAllVarsWithValuesAsString(curVector), z, null);
//        				.evaluateExprBound(currentValuesAsString, z, null);
//        		
////        		// Get the pairing.
////        		int lpnIndex = this.lpn.getLpnIndex();
////        		int contVarIndex = this.lpn.getContVarIndex(key);
////        		
////        		// Package up the indecies.
////        		LPNContinuousPair contVar = new LPNContinuousPair(lpnIndex, contVarIndex);
//        		
//        		if(newRate == null){
//        			// Keep the current rate.
//        			contVar.setCurrentRate(z.getCurrentRate(contVar));
//        		}
//        		
//        		
////        		continuousValues.put(contVar, newValue);
//        		
//        		// Get each inequality that involves the continuous variable.
//        		ArrayList<InequalityVariable> inequalities = this.lpn.getContVar(contVarIndex).getInequalities();
//        		
//        		// Update the inequalities.
//        		for(InequalityVariable ineq : inequalities){
//        			int ineqIndex = this.lpn.getVarIndexMap().get(ineq.getName());
//        			
//        			
//        			HashMap<LPNContAndRate, IntervalPair> continuousValues = new HashMap<LPNContAndRate, IntervalPair>();
//        			continuousValues.putAll(newAssignValues.get(OLD_ZERO));
//        			continuousValues.putAll(newAssignValues.get(NEW_NON_ZERO));
//        			continuousValues.putAll(newAssignValues.get(NEW_ZERO));
//        			continuousValues.putAll(newAssignValues.get(OLD_NON_ZERO));
//        			
//        			
//        			newVectorArray[ineqIndex] = ineq.evaluate(newVectorArray, z, continuousValues);
//        		}
//        	}
//        	
//
//        	// If the value did not get assigned, put in the old value.
//        	if(newValue == null){
//        		newValue = z.getContinuousBounds(contVar);
//        	}
//    		
//    		// Check if the new assignment gives rate zero.
//    		//boolean newRateZero = newRate == 0;
//        	boolean newRateZero = newRate.singleValue() ? newRate.get_LowerBound() == 0 : false;
//    		// Check if the variable was already rate zero.
//    		boolean oldRateZero = z.getCurrentRate(contVar) == 0;
//    		
//    		// Put the new value in the appropriate set.
//    		if(oldRateZero){
//    			if(newRateZero){
//    				// Old rate is zero and the new rate is zero.
//    				newAssignValues.get(OLD_ZERO).put(new LPNContAndRate(contVar, newRate), newValue);
//    			}
//    			else{
//    				// Old rate is zero and the new rate is non-zero.
//    				newAssignValues.get(NEW_NON_ZERO).put(new LPNContAndRate(contVar, newRate), newValue);
//    			}
//    		}
//    		else{
//    			if(newRateZero){
//    				// Old rate is non-zero and the new rate is zero.
//    				newAssignValues.get(NEW_ZERO).put(new LPNContAndRate(contVar, newRate), newValue);
//    			}
//    			else{
//    				// Old rate is non-zero and the new rate is non-zero.
//    				newAssignValues.get(OLD_NON_ZERO).put(new LPNContAndRate(contVar, newRate), newValue);
//    			}
//    		}
//        	
//        }
        
        /*
        for (VarExpr s : firedTran.getAssignments()) {
            int newValue = (int) s.getExpr().evaluate(curVector);
            newVectorArray[s.getVar().getIndex(curVector)] = newValue;
        }
        */
        // Enabled transition vector update
	   	boolean[] newTranVector = updateTranVector(curState, curNewMarking, newVariableVector, firedTran);
        State newState = thisSg.addState(new State(this.lpn, curNewMarking, newVariableVector, newTranVector));
        
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
    	if (Options.getDebugMode()) {
    		String location = "StateGraph.java, fire(StateGraph, State, Transition)";
    		thisSg.printNextStateForGivenState(curState, location);    		
    	}
		return newState;
    }
    
    public boolean[] updateTranVector(State state,
			int[] newMarking, int[] newVariableVector, Transition firedTran) {
    	boolean[] tranVectorAfterFiring = state.getTranVector().clone();
		// Disable the fired transition and all of its conflicting transitions. 
    	if (firedTran != null) {
    		tranVectorAfterFiring[firedTran.getIndex()] = false;
    		for (Integer curConflictingTranIndex : firedTran.getConflictSetTransIndices()) {
    			tranVectorAfterFiring[curConflictingTranIndex] = false;
    		}
    	}
        // find newly enabled transition(s) based on the updated markings and variables
        for (Transition tran : this.lpn.getAllTransitions()) {
        	boolean needToUpdate = true;
        	String tranName = tran.getLabel();
    		int tranIndex = tran.getIndex();
    		if (Options.getDebugMode()) {
//    			System.out.println("Checking " + tranName);
    		}
    		if (this.lpn.getEnablingTree(tranName) != null 
    				&& this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(newVariableVector)) == 0.0) {
    			if (Options.getDebugMode()) {
//    				System.out.println(tran.getName() + " " + "Enabling condition is false");    			
    			}					
    			if (tranVectorAfterFiring[tranIndex] && !tran.isPersistent())
    				tranVectorAfterFiring[tranIndex] = false;
    			continue;
    		}
    		if (this.lpn.getTransitionRateTree(tranName) != null 
    				&& this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(newVariableVector)) == 0.0) {
    			if (Options.getDebugMode()) {
//    				System.out.println("Rate is zero");
    			}
    			if (tranVectorAfterFiring[tranIndex])
    				tranVectorAfterFiring[tranIndex] = false;
    			continue;
    		}
    		if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
    			for (int place : this.lpn.getPresetIndex(tranName)) {
    				if (newMarking[place]==0) {
    					if (Options.getDebugMode()) {
//    						System.out.println(tran.getName() + " " + "Missing a preset token");
    					}
    					needToUpdate = false;
    					if (tranVectorAfterFiring[tranIndex])
    						tranVectorAfterFiring[tranIndex] = false;
    					break;
    				}
    			}
    		}
			if (needToUpdate) {            	
    			tranVectorAfterFiring[tranIndex] = true;
    			if (Options.getDebugMode()) {
//    				System.out.println(tran.getName() + " is Enabled.");
    			}					
            }
        }
    	return tranVectorAfterFiring;
	}
    
    /**
     * Updates the transition vector.
     * @param enabledTranBeforeFiring
     * 			The enabling before the transition firing.
     * @param newMarking
     * 			The new marking to check for transitions with.
     * @param newVectorArray
     * 			The new values of the boolean variables.
     * @param firedTran
     * 			The transition that fire.
     * @param newlyEnabled
     * 			A list to capture the newly enabled transitions.
     * @return
     * 			The newly enabled transitions.
     */
    public boolean[] updateTranVector(boolean[] enabledTranBeforeFiring,
			int[] newMarking, int[] newVectorArray, Transition firedTran, HashSet<LPNTransitionPair> newlyEnabled) {
    	boolean[] enabledTranAfterFiring = enabledTranBeforeFiring.clone();
		// Disable fired transition
    	if (firedTran != null) {
    		enabledTranAfterFiring[firedTran.getIndex()] = false;
    		for (Integer curConflictingTranIndex : firedTran.getConflictSetTransIndices()) {
    			enabledTranAfterFiring[curConflictingTranIndex] = false;
    		}
    	}
        // find newly enabled transition(s) based on the updated markings and variables
    	if (Options.getDebugMode())
			System.out.println("Finding newly enabled transitions at updateEnabledTranVector.");
        for (Transition tran : this.lpn.getAllTransitions()) {
        	boolean needToUpdate = true;
        	String tranName = tran.getLabel();
    		int tranIndex = tran.getIndex();
    		if (Options.getDebugMode())
				System.out.println("Checking " + tranName);
    		if (this.lpn.getEnablingTree(tranName) != null 
    				&& this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(newVectorArray)) == 0.0) {
    			if (Options.getDebugMode())
					System.out.println(tran.getLabel() + " " + "Enabling condition is false");    			
    			if (enabledTranAfterFiring[tranIndex] && !tran.isPersistent())
    				enabledTranAfterFiring[tranIndex] = false;
    			continue;
    		}
    		if (this.lpn.getTransitionRateTree(tranName) != null 
    				&& this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(newVectorArray)) == 0.0) {
    			if (Options.getDebugMode())
					System.out.println("Rate is zero");
    			continue;
    		}
    		if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
    			for (int place : this.lpn.getPresetIndex(tranName)) {
    				if (newMarking[place]==0) {
    					if (Options.getDebugMode())
							System.out.println(tran.getLabel() + " " + "Missing a preset token");
    					needToUpdate = false;
    					break;
    				}
    			}
    		}
			if (needToUpdate) {
            	// if a transition is enabled and it is not recorded in the enabled transition vector
    			enabledTranAfterFiring[tranIndex] = true;
    			if (Options.getDebugMode())
					System.out.println(tran.getLabel() + " is Enabled.");
            }
			
			if(newlyEnabled != null && enabledTranAfterFiring[tranIndex] && !enabledTranBeforeFiring[tranIndex]){
				newlyEnabled.add(new LPNTransitionPair(tran.getLpn().getLpnIndex(), tranIndex));
			}
        }
    	return enabledTranAfterFiring;
	}
    
	public State constrFire(Transition firedTran, final State curState) {
		// Hao's original marking update.
//    	// Marking update
//        int[] curOldMarking = curState.getMarking();
//        int[] curNewMarking = null;
//        if(firedTran.getPreset().length==0 && firedTran.getPostset().length==0){
//        	curNewMarking = curOldMarking;
//        }
//		else {
//			curNewMarking = new int[curOldMarking.length - firedTran.getPreset().length + firedTran.getPostset().length];
//			int index = 0;			
//			for (int i : curOldMarking) {
//				boolean existed = false;
//				for (int prep : this.lpn.getPresetIndex(firedTran.getName())) {
//					if (i == prep) {
//						existed = true;
//						break;
//					}
//					else if(prep > i){
//						break;
//					}
//				}
//				
//				if (existed == false) {
//					curNewMarking[index] = i;
//					index++;
//				}
//			}
//			
//			for (int postp : this.lpn.getPostsetIndex(firedTran.getName())) {
//				curNewMarking[index] = postp;
//				index++;
//			}
//        }
        
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

        //  State vector update
        int[] oldVector = curState.getVariableVector();
        int size = oldVector.length;
        int[] newVectorArray = new int[size];
        System.arraycopy(oldVector, 0, newVectorArray, 0, size);
        
        int[] curVector = curState.getVariableVector();
        for (String key : this.lpn.getAllVarsWithValuesAsString(curVector).keySet()) {
        	if (this.lpn.getBoolAssignTree(firedTran.getLabel(), key) != null) {
        		int newValue = (int)this.lpn.getBoolAssignTree(firedTran.getLabel(), key).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(curVector));
        		newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
        	}
        	if (this.lpn.getIntAssignTree(firedTran.getLabel(), key) != null) {
        		int newValue = (int)this.lpn.getIntAssignTree(firedTran.getLabel(), key).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(curVector));
        		newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
        	}
        }       
        // Enabled transition vector update
        /* Hao's code
        //boolean[] newEnabledTranArray = curState.getTranVector();
        //newEnabledTranArray[firedTran.getIndex()] = false;
        //State newState = new State(this.lpn, curNewMarking, newVectorArray, newEnabledTranArray);
         */
        boolean[] newEnabledTranVector = updateTranVector(curState, curNewMarking, newVectorArray, firedTran);
        State newState = new State(this.lpn, curNewMarking, newVectorArray, newEnabledTranVector);
 
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
		return newState;
    }
	
	public void drawLocalStateGraph() {
		try {
			String graphFileName = null;
			if ( Options.getTimingAnalysisType() == "off") {
				if (Options.getPOR().toLowerCase().equals("off")) {
					graphFileName = Options.getPrjSgPath() + separator + getLpn().getLabel() + "_local_full_sg.dot";
				}					
				else {
					graphFileName = Options.getPrjSgPath() + separator + getLpn().getLabel() + "_POR_"+ Options.getCycleClosingMthd() + "_local_sg.dot";
				}					
			} else {
				// TODO: Need to add separator here?
				graphFileName = Options.getPrjSgPath() + getLpn().getLabel() + "_sg.dot";
			}
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
			System.err.println("Error producing local state graph as dot file.");
		}
	}

	protected static String intArrayToString(String type, State curState) {
		String arrayStr = "";
		if (type.equals("markings")) {
			for (int i=0; i< curState.getMarking().length; i++) {
				if (curState.getMarking()[i] == 1) {
					arrayStr = arrayStr + curState.getLpn().getPlaceList()[i] + ",";
				}
//				String tranName = curState.getLpn().getAllTransitions()[i].getName();
//				if (curState.getTranVector()[i])
//					System.out.println(tranName + " " + "Enabled");
//				else
//					System.out.println(tranName + " " + "Not Enabled");
			}
			arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}				
		else if (type.equals("vars")) {
			for (int i=0; i< curState.getVariableVector().length; i++) {
				arrayStr = arrayStr + curState.getVariableVector()[i] + ",";
			}
			if (arrayStr.contains(","))
				arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}
		return arrayStr;
	}
	
	protected static String boolArrayToString(String type, State curState) {
		String arrayStr = "";
		if (type.equals("enabledTrans")) {
			for (int i=0; i< curState.getTranVector().length; i++) {
				if (curState.getTranVector()[i]) {
					arrayStr = arrayStr + curState.getLpn().getAllTransitions()[i].getLabel() + ",";
				}
			}
			if (arrayStr != "")
				arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}
		return arrayStr;
	}
	
//	private void printTransitionSet(LpnTranList transitionSet, String setName) {
//		if (!setName.isEmpty())
//			System.out.print(setName + " ");
//		if (transitionSet.isEmpty()) {
//			System.out.println("empty");
//		}
//		else {
//			for (Iterator<Transition> curTranIter = transitionSet.iterator(); curTranIter.hasNext();) {
//				Transition tranInDisable = curTranIter.next();
//				System.out.print(tranInDisable.getName() + " ");
//			}
//			System.out.print("\n");
//		}
//	}

//	private static void printAmpleSet(LpnTranList transitionSet, String setName) {
//		if (!setName.isEmpty())
//			System.out.print(setName + " ");
//		if (transitionSet.isEmpty()) {
//			System.out.println("empty");
//		}
//		else {
//			for (Iterator<Transition> curTranIter = transitionSet.iterator(); curTranIter.hasNext();) {
//				Transition tranInDisable = curTranIter.next();
//				System.out.print(tranInDisable.getName() + " ");
//			}
//			System.out.print("\n");
//		}
//	}
	
//	public HashMap<State,LpnTranList> copyEnabledSetTbl() {
//		HashMap<State,LpnTranList> copyEnabledSetTbl = new HashMap<State,LpnTranList>();
//		for (State s : enabledSetTbl.keySet()) {
//			LpnTranList tranList = enabledSetTbl.get(s).clone();
//			copyEnabledSetTbl.put(s.clone(), tranList);
//		}
//		return copyEnabledSetTbl;
//	}

	public HashMap<State, LpnTranList> getEnabledSetTbl() {
		return this.enabledSetTbl;
	}

	public HashMap<State, HashMap<Transition, State>> getNextStateMap() {
		return this.nextStateMap;
	}
	
	/**
	 * Fires a transition for the timed code.
	 * @param curSgArray
	 * 			The current information on the all local states.
	 * @param currentPrjState
	 * 			The current state.
	 * @param firedTran
	 * 			The transition to fire.
	 * @return
	 * 			The new states.
	 */
	public TimedPrjState fire(final StateGraph[] curSgArray, 
			final PrjState currentPrjState, Transition firedTran){
		 
		TimedPrjState currentTimedPrjState;
		
		// Check that this is a timed state.
		if(currentPrjState instanceof TimedPrjState){
			currentTimedPrjState = (TimedPrjState) currentPrjState;
		}
		else{
			throw new IllegalArgumentException("Attempted to use the " +
					"fire(StateGraph[],PrjState,Transition)" +
					"without a TimedPrjState stored in the PrjState " +
					"variable. This method is meant for TimedPrjStates.");
		}
		
		// Extract relevant information from the current project state.
		State[] curStateArray = currentTimedPrjState.toStateArray();
//		Zone[] currentZones = currentTimedPrjState.toZoneArray();
		Equivalence[] currentZones = currentTimedPrjState.toZoneArray();

		
//		Zone[] newZones = new Zone[1];
		Equivalence[] newZones = new Equivalence[1];
		
		/* 
		 * This ArrayList contains four separate maps that store the 
		 * rate and continuous variables assignments.
		 */

		// Get the new un-timed local states.
		State[] newStates = fire(curSgArray, curStateArray, firedTran); 

		
		
//		ArrayList<HashMap<LPNContAndRate, IntervalPair>> newAssignValues =
//				updateContinuousState(currentTimedPrjState.get_zones()[0], curStateArray, newStates, firedTran);

//		ArrayList<UpdateContinuous> newAssignValues =
//				updateContinuousState(currentTimedPrjState.get_zones()[0], curStateArray, newStates, firedTran);

//		ContinuousRecordSet newAssignValues =
//				updateContinuousState(currentTimedPrjState.get_zones()[0],
//						curStateArray, newStates, firedTran);
		
		ContinuousRecordSet newAssignValues = new ContinuousRecordSet();
		
		//*newStates  =
				//*updateContinuousState(currentTimedPrjState.get_zones()[0],
						//*curStateArray, newStates, firedTran,
						//*newAssignValues);
		
		newStates  =
				updateContinuousState(currentTimedPrjState.get_zones()[0],
						curStateArray, newStates, firedTran,
						newAssignValues);
		
		// Check the cached values. Current this assumes no shared continuous variables.
		State nextState = this.getNextState(newStates[this.getLpn().getLpnIndex()], firedTran);
		if( nextState != null){
			newStates[this.getLpn().getLpnIndex()] = nextState;
		}
		else{
			newStates[this.getLpn().getLpnIndex()] = 
					this.addState(newStates[this.getLpn().getLpnIndex()]);
			this.addStateTran(curStateArray[this.getLpn().getLpnIndex()], firedTran,
					newStates[this.getLpn().getLpnIndex()]);
		}
		
		
		LpnTranList enabledTransitions = new LpnTranList();
		
		for(int i=0; i<newStates.length; i++)
		{
			
			// The StateGraph has to be used to call getEnabled
			// since it is being passed implicitly.
			LpnTranList localEnabledTransitions = curSgArray[i].getEnabled(newStates[i]);
			
			for(int j=0; j<localEnabledTransitions.size(); j++){
				enabledTransitions.add(localEnabledTransitions.get(j));
			}
			
		}
	
		newZones[0] = currentZones[0].fire(firedTran,
				enabledTransitions, newAssignValues, newStates);
		

		return new TimedPrjState(newStates, newZones);
	}
	
	/**
	 * Fires a list of events. This list can either contain a single transition, a rate change, 
	 * or a set of inequalities.
	 * @param curSgArray
	 * 			The current information on the all local states.
	 * @param currentPrjState
	 * 			The current state.
	 * @param eventSets
	 * 			The set of events to fire.
	 * @return
	 * 			The new state.
	 */
	public TimedPrjState fire(final StateGraph[] curSgArray, 
			final PrjState currentPrjState, EventSet eventSet){
		
		TimedPrjState currentTimedPrjState;
		
		// Check that this is a timed state.
		if(currentPrjState instanceof TimedPrjState){
			currentTimedPrjState = (TimedPrjState) currentPrjState;
		}
		else{
			throw new IllegalArgumentException("Attempted to use the " +
					"fire(StateGraph[],PrjState,Transition)" +
					"without a TimedPrjState stored in the PrjState " +
					"variable. This method is meant for TimedPrjStates.");
		}
		
		// Determine if the list of events represents a list of inequalities.
		if(eventSet.isInequalities()){
			
			// Create a copy of the current states.
			State[] oldStates = currentTimedPrjState.getStateArray();
			
			State[] states = new State[oldStates.length];
			for(int i=0; i<oldStates.length; i++){
				states[i] = oldStates[i].clone();
			}
			
			// Get the variable index map for getting the indecies
			// of the variables.
			DualHashMap<String, Integer> map = lpn.getVarIndexMap();
			
			for(Event e : eventSet){
								
				// Extract inequality variable.
				InequalityVariable iv = e.getInequalityVariable();
				
				// Get the index to change the value.
				int variableIndex = map.getValue(iv.getName());
				
				// Flip the value of the inequality.
				int[] vector = states[this.lpn.getLpnIndex()].getVariableVector();
				vector[variableIndex] = 
						vector[variableIndex] == 0 ? 1 : 0;
			}
			
			HashSet<LPNTransitionPair> newlyEnabled = new HashSet<LPNTransitionPair>();
			// Update the enabled transitions according to inequalities that have changed.
			for(int i=0; i<states.length; i++){
				boolean[] newEnabledTranVector = updateTranVector(states[i].getTranVector(),
						states[i].marking, states[i].vector, null, newlyEnabled);
		        State newState = curSgArray[i].addState(new State(this.lpn, states[i].marking, states[i].vector, newEnabledTranVector));
		        states[i] = newState;
			}
			
			// Get a new zone that has been restricted according to the inequalities firing.
			//*Zone z = currentTimedPrjState.get_zones()[0]
					//*.getContinuousRestrictedZone(eventSet, states);
			Equivalence z = currentTimedPrjState.get_zones()[0]
					.getContinuousRestrictedZone(eventSet, states);
			
			
			
			z.recononicalize();
			
			
			// Add any new transitions.
			z = z.addTransition(newlyEnabled, states);
			
			// Reset rates to their lower bounds.
			if(!Options.get_resetOnce()){
				z = z.resetRates();
			}
			
			z.advance(states);
			
			z.recononicalize();
			
//			return new TimedPrjState(states, currentTimedPrjState.get_zones());
						
			//*return new TimedPrjState(states, new Zone[]{z});
			return new TimedPrjState(states, new Equivalence[]{z});
		}
		else if (eventSet.isRate()){
			// The EventSet is a rate change event, so fire the rate change.
			return fireRateChange(curSgArray, currentPrjState,
					eventSet.getRateChange());
		}
		
		return fire(curSgArray, currentPrjState, eventSet.getTransition());
	}
	
	//*private Zone addNewEnabledTransitions(final StateGraph[] curSgArray,
	private Equivalence addNewEnabledTransitions(final StateGraph[] curSgArray,
			State[] states, EventSet eventSet,
			TimedPrjState currentTimedPrjState){
		HashSet<LPNTransitionPair> newlyEnabled = new HashSet<LPNTransitionPair>();
		// Update the enabled transitions according to inequalities that have changed.
		for(int i=0; i<states.length; i++){
			boolean[] newEnabledTranVector = updateTranVector(states[i].getTranVector(),
					states[i].marking, states[i].vector, null, newlyEnabled);
	        State newState = curSgArray[i].addState(new State(this.lpn, states[i].marking, states[i].vector, newEnabledTranVector));
	        states[i] = newState;
		}
		
		// Get a new zone that has been restricted according to the inequalities firing.
		//*Zone z = currentTimedPrjState.get_zones()[0].
				//*getContinuousRestrictedZone(eventSet, states);
		
		Equivalence z = currentTimedPrjState.get_zones()[0].
				getContinuousRestrictedZone(eventSet, states);
		
		
//		z.advance(states);
		z.recononicalize();
		
		// Add any new transitions.
		z = z.addTransition(newlyEnabled, states);
		
//		z.advance(states);
//		
//		z.recononicalize();
		
		return z;
	}
	
	/**
	 * Fires a rate change event.
	 * 
	 * @param curSgArray
	 * 		The current information for the states.
	 * @param currentPrjState
	 * 		The current project state.
	 * @param firedRate
	 * 		The rate to fire. This consists of an LPNcontinuousPair that
	 * 		has the index of the continuous variable to change and what the new 
	 * 		rate should be.
	 * @return
	 */
	public TimedPrjState fire(final StateGraph[] curSgArray, 
			final PrjState currentPrjState, LPNContinuousPair firedRate){
		

		TimedPrjState currentTimedPrjState;
		
		// Check that this is a timed state.
		if(currentPrjState instanceof TimedPrjState){
			currentTimedPrjState = (TimedPrjState) currentPrjState;
		}
		else{
			throw new IllegalArgumentException("Attempted to use the " +
					"fire(StateGraph[],PrjState,Transition)" +
					"without a TimedPrjState stored in the PrjState " +
					"variable. This method is meant for TimedPrjStates.");
		}
		
		//*Zone[] newZones = new Zone[1];
		Equivalence[] newZones = new Equivalence[1];
		
		newZones[0] = currentTimedPrjState.get_zones()[0].
				fire(firedRate, firedRate.getCurrentRate());
		
		// Check if the any inequalities need to change.
		
		addNewEnabledTransitions(curSgArray, currentPrjState.getStateArray(),
				null, currentTimedPrjState);
		
		newZones[0].advance(currentTimedPrjState.getStateArray());
		newZones[0].recononicalize();
		
		return new TimedPrjState(currentTimedPrjState.getStateArray(), newZones);
	}

	public static TimedPrjState fireRateChange(final StateGraph[] curSgArray, 
			final PrjState currentPrjState, LPNContinuousPair firedRate){
		
		TimedPrjState currentTimedPrjState;
		
		// Check that this is a timed state.
		if(currentPrjState instanceof TimedPrjState){
			currentTimedPrjState = (TimedPrjState) currentPrjState;
		}
		else{
			throw new IllegalArgumentException("Attempted to use the " +
					"fire(StateGraph[],PrjState,Transition)" +
					"without a TimedPrjState stored in the PrjState " +
					"variable. This method is meant for TimedPrjStates.");
		}
		
		
		// Create a new copy of the zone.
		//*Zone[] newZones = new Zone[1];
		Equivalence[] newZones = new Equivalence[1];
		
		//*Zone[] oldZones = currentTimedPrjState.get_zones();
		Equivalence[] oldZones = currentTimedPrjState.get_zones();
		
		// Check if this continuous variable is currently in the rate zeros.
		if(currentTimedPrjState.get_zones()[0].getCurrentRate(firedRate) == 0){
			// Need to add the rate zero continuous variables back into the zone.
			newZones[0] = oldZones[0].moveOldRateZero(firedRate);
			newZones[0].advance(currentTimedPrjState.getStateArray());
			newZones[0].recononicalize();

			State[] localStates  = currentTimedPrjState.getStateArray();
			
			return new TimedPrjState(localStates, newZones);
		}
		else if(firedRate.getCurrentRate() == 0){
			/*
			 * If the rate change event is zero, then the variable needs to be
			 * saved off into the rate zero variables. No warping or recanonicalization
			 * is necessary since all the other relationship remain the same.
			 */
			newZones[0] = oldZones[0].saveOutZeroRate(firedRate);
		}
		else{
//			newZones[0] = currentTimedPrjState.get_zones()[0]
//					.clone();
			newZones[0] = oldZones[0].clone();
		}
		// Change the rate.
		newZones[0].setCurrentRate(firedRate, firedRate.getCurrentRate());
		
		// Warp the zone.
		newZones[0].dbmWarp(currentTimedPrjState.get_zones()[0]);
		newZones[0].recononicalize();
		newZones[0].advance(currentTimedPrjState.getStateArray());
		newZones[0].recononicalize();

		State[] localStates  = currentTimedPrjState.getStateArray();
		
		TimedPrjState newTimedPrjState = new TimedPrjState(localStates, newZones);
		
		// Get any inequality events that need to change.
		LpnTranList inequalityList = new LpnTranList();
		
		LPN[] lpnList = newZones[0].get_lpnList();

		
		// Check all the inequalities for inclusion.
		Variable contVar = lpnList[firedRate.get_lpnIndex()]
				.getContVar(firedRate.get_transitionIndex());
		
		if(contVar.getInequalities() != null){
			for(InequalityVariable iv : contVar.getInequalities()){

				// Check if the inequality can change.
				// Only consider inequalities for which the continuous
				// variable is equal to the constant.
				if(ContinuousUtilities.
						inequalityCanChange(newZones[0], iv,
								localStates[firedRate.get_lpnIndex()])){
					
					// Find the index of the continuous variable this inequality refers to.
					// I'm assuming there is a single variable.
					LPN lpn = iv.get_lpn();
					Variable tmpVar = iv.getContVariables().get(0);
					DualHashMap<String, Integer> variableIndecies = lpn.getContinuousIndexMap();
					int contIndex = variableIndecies.getValue(tmpVar.getName());

					// Package up the information into a the index. Note the current rate doesn't matter.
					LPNContinuousPair index = 
							new LPNContinuousPair(lpn.getLpnIndex(), contIndex, 0);

//					int value = newZones[0]
//							.getDbmEntryByPair(LPNTransitionPair.ZERO_TIMER_PAIR,
//									index);
					int value = newZones[0].getUnwarpedUpperBound(index);
					
					if(iv.exceedConstant(value)){
					
//						inequalityList = 
//								newZones[0].addSetItem(inequalityList,
//										new Event(iv),
//										localStates[firedRate.get_lpnIndex()]);
						inequalityList = 
								Zone.addSetItem(newZones[0], inequalityList,
										new Event(iv),
										localStates[firedRate.get_lpnIndex()]);
					}
				}
			}
		}
		
		// Fire the inequalities.
//		if(inequalityList.size() >0){
//			if(inequalityList.size() >1){
//				throw new IllegalStateException("Expected a single set of "
//						+ "inequalities, but got something more.");
//			}
//			return fire(curSgArray, newTimedPrjState, inequalityList.getFirst());
//		}
		
		
		return newTimedPrjState;
	}
	
	/**
	 * This method handles the extracting of the new rate and continuous variable assignments.
	 * @param z
	 * 		The previous zone.
	 * @param currentValuesAsString
	 * 		The current values of the Boolean variables converted to strings.
	 * @param oldStates
	 * 		The current states.
	 * @param firedTran
	 * 		The fired transition.
	 * @return
	 * 		The continuous variable and rate assignments.
	 */
//	private ArrayList<HashMap<LPNContAndRate, IntervalPair>>
//		updateContinuousState(Zone z,
//			State[] oldStates, State[] newStates, Transition firedTran){
//	private ArrayList<UpdateContinuous>
//	updateContinuousState(Zone z,
//		State[] oldStates, State[] newStates, Transition firedTran){
	//*private State[] updateContinuousState(Zone z,
	private State[] updateContinuousState(Equivalence z,
		State[] oldStates, State[] newStates, Transition firedTran, 
		ContinuousRecordSet updateContinuousRecords){
		// Convert the current values of Boolean variables to strings for use in the 
		// Evaluator.
		HashMap<String, String> currentValuesAsString = 
				this.lpn.getAllVarsWithValuesAsString(oldStates[this.lpn.getLpnIndex()].getVariableVector());
		
		
		// Accumulates a set of all transitions that need their enabling conditions
		// re-evaluated.
		HashSet<Transition> needUpdating = new HashSet<Transition>();
		
		
		HashSet<InequalityVariable> ineqNeedUpdating=
				new HashSet<InequalityVariable>();
		
		HashSet<InequalityVariable> rateIneqNeedUpdate =
				new HashSet<InequalityVariable>();
		
		// Accumulates the new assignment information.
//		ArrayList<UpdateContinuous> updateContinuousRecords =
//				new ArrayList<UpdateContinuous>();

//		ContinuousRecordSet updateContinuousRecords =
//				new ContinuousRecordSet();
		
		// Accumulates the new assignment information.
//		ArrayList<HashMap<LPNContAndRate, IntervalPair>> newAssignValues 
//			= new ArrayList<HashMap<LPNContAndRate, IntervalPair>>();
		
		/*
		 * 0. old rate is zero, new rate is zero.
		 * 		Lookup the zero rate in the _rateZeroContinuous and add any new continuous assignments.
		 * 1. old rate is zero, new rate is non-zero.
		 * 		Remove the rate from the _rateZeroContinuous and add the zone.
		 * 2. old rate is non-zero, new rate is zero.
		 * 		Add the variable with its upper and lower bounds to _rateZeroContinuous.
		 * 3. old rate is non-zero, new rate is non-zero.
		 * 		Get the LPNContinuousPair from the _indexToTimerPair and change the value.
		 * 
		 * Note: If an assignment is made to the variable, then it should be considered as a
		 * new variable.
		 */
		
		// Update rates      
//		final int OLD_ZERO = 0; 	// Case 0 in description.
//		final int NEW_NON_ZERO = 1; // Case 1 in description.
//		final int NEW_ZERO = 2;		// Case 2 in description.
//		final int OLD_NON_ZERO = 3;	// Case 3 in description.
//		if(Options.getTimingAnalysisFlag()){
//			newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
//			newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
//			newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
//			newAssignValues.add(new HashMap<LPNContAndRate, IntervalPair>());
//		}

		for(String key : this.lpn.getContVars()){

			UpdateContinuous newRecord = new UpdateContinuous();
			
			// Get the pairing.
			int lpnIndex = this.lpn.getLpnIndex();
			int contVarIndex = this.lpn.getContVarIndex(key);

			// Package up the indecies.
			LPNContinuousPair contVar = new LPNContinuousPair(lpnIndex, contVarIndex);

			//Integer newRate = null;
			IntervalPair newRate = null;
			IntervalPair newValue= null;

			//}

			// Update continuous variables.
			if (this.lpn.getContAssignTree(firedTran.getLabel(), key) != null) {
				
				newRecord.set_newValue(true);
				
				// Get the new value.
				newValue = this.lpn.getContAssignTree(firedTran.getLabel(), key)
						.evaluateExprBound(currentValuesAsString, z, null);
				
				// Get each inequality that involves the continuous variable.
				ArrayList<InequalityVariable> inequalities = 
						this.lpn.getContVar(contVarIndex).getInequalities();

				/* If inequalities is null, create a list */
				if(inequalities == null){
					inequalities = new ArrayList<InequalityVariable>();
				}
				
				ineqNeedUpdating.addAll(inequalities);

			}

			// Check if there is a new rate assignment.
			if(this.lpn.getRateAssignTree(firedTran.getLabel(), key) != null){
				newRate = this.lpn.getRateAssignTree(firedTran.getLabel(), key)
						.evaluateExprBound(currentValuesAsString, z, null);

				contVar.setCurrentRate(newRate.getSmallestRate());

				
				newRecord.set_lcrPair(new LPNContAndRate(contVar, newRate));
				
			}
			
//			if(newValue == null && newRate == null){
//				// If nothing was set, then nothing needs to be done.
//				continue;
//			}
			
			
			if(newValue == null && newRate == null){
				// Need to check if the current rate will be set to non-zero
				// when it is currently zero.
				newRate = z.getRateBounds(contVar);
				
				if(newRate.isZero() || !(newRate.containsZero())){
					// Nothing needs to be done, so continue.
					continue;
				}
				newRecord.set_lcrPair(new LPNContAndRate(contVar, newRate));
				
				// Since we are going to be adding the continuous variable back
				// into the zone, the value should be treated as new.
				newValue = z.getContinuousBounds(contVar);
				newRecord.set_Value(newValue);
				newRecord.set_newValue(true);
				
				// Set the rate to the lower bound or zero.
				contVar.setCurrentRate(z.rateResetValue(contVar));
			}

			// If the value did not get assigned, put in the old value.
			if(newValue == null){
				newValue = z.getContinuousBounds(contVar);
				if (newValue == null) {
					newValue = new IntervalPair(0,0);
				}
				newRecord.set_newValue(false);
				newRecord.set_Value(newValue);
			}
			else{
				// Put in the new value.
				newRecord.set_Value(newValue);
				
				// Declare this record has a new value.
				newRecord.set_newValue(true);
			}

			
			// If a new rate has not been assigned, put in the old rate.
			if(newRate == null){
				newRate = z.getRateBounds(contVar);
				newRecord.set_lcrPair(new LPNContAndRate(contVar, newRate));
				
				// Set the rate to the lower bound or zero.
				contVar.setCurrentRate(z.rateResetValue(contVar));
			}
			
//			newRecord.set_lcrPair(new LPNContAndRate(contVar, newRate));
//			
//			// Set the rate to the lower bound or zero.
//			contVar.setCurrentRate(z.getSmallestRate(contVar));
			
			// Check if the new assignment gives rate zero.
//			boolean newRateZero = newRate.singleValue() ? 
//					newRate.get_LowerBound() == 0 : false;
			
			boolean newRateZero = newRate.isZero();
			
			// Check if the variable was already rate zero. 
			boolean oldRateZero = z.getCurrentRate(contVar) == 0;

			newRecord.set_oldZero(oldRateZero);
			newRecord.set_newZero(newRateZero);
			
			updateContinuousRecords.add(newRecord);
			
			// Put the new value in the appropriate set.
//			if(oldRateZero){
//				if(newRateZero){
//					// Old rate is zero and the new rate is zero.
//					newAssignValues.get(OLD_ZERO).put(new LPNContAndRate(contVar, newRate), newValue);
//				}
//				else{
//					// Old rate is zero and the new rate is non-zero.
//					newAssignValues.get(NEW_NON_ZERO).put(new LPNContAndRate(contVar, newRate), newValue);
//				}
//			}
//			else{
//				if(newRateZero){
//					// Old rate is non-zero and the new rate is zero.
//					newAssignValues.get(NEW_ZERO).put(new LPNContAndRate(contVar, newRate), newValue);
//				}
//				else{
//					// Old rate is non-zero and the new rate is non-zero.
//					newAssignValues.get(OLD_NON_ZERO).put(new LPNContAndRate(contVar, newRate), newValue);
//				}
//			}

			// Check for inequalities that need to be changed based on a
			// rate change.
			
			// Get each inequality that involves the continuous variable.
			ArrayList<InequalityVariable> rateInequalities = 
					this.lpn.getContVar(contVarIndex).getInequalities();

			/* If inequalities is null, create a list */
			if(rateInequalities == null){
				rateInequalities = new ArrayList<InequalityVariable>();
			}
			
			// Rate assignments can change the value of inequalities if
			// the value of the variable is equal to the constant
			// for the inequality variable.
			for(InequalityVariable iv: rateInequalities){
				// Assume that the variable can have a rate change alter
				// the inequality if the upper and lower bound for the 
				// values is within one and the constant is in between.
				
				boolean nearConstant = false;
				
				// Determine in the current value of the variable is 
				// within 1 unit in the warped space.
				if(contVar.getCurrentRate() > 0){
					nearConstant = 
							Math.abs(ContinuousUtilities.chkDiv(newValue.get_UpperBound(),
									contVar.getCurrentRate(), true)
							- ContinuousUtilities.chkDiv(newValue.get_LowerBound(),
									contVar.getCurrentRate(), false))
							<= 1.0;
				}
				else if (contVar.getCurrentRate()<0){
					nearConstant = 
							Math.abs(ContinuousUtilities.chkDiv(newValue.get_LowerBound(),
									contVar.getCurrentRate(), true)
							- ContinuousUtilities.chkDiv(newValue.get_UpperBound(),
									contVar.getCurrentRate(), false))
							<= 1.0;
				}
				else{
					nearConstant=Math.abs(newValue.get_UpperBound()
							- newValue.get_LowerBound()) <= 1.0;
				}
				
				
				if(nearConstant
						&& newValue.get_LowerBound() <= iv.getConstant()
						&& newValue.get_UpperBound() >= iv.getConstant()){
					
					int[] currentarray = newStates[this.lpn.getLpnIndex()].getVariableVector();
					int ineqIndex = this.lpn.getVarIndexMap().get(iv.getName());
					if(contVar.getCurrentRate() >= 0){
						if(iv.isGreaterThan() && currentarray[ineqIndex] != 1){
							// If we are an inequality of the form v>=a, v>a, 
							// a<=v, or a<v, then the inequality is treated as true
							// when the rate is positive. So if the inequality is not true
							// add it to the inequalities to change.
							rateIneqNeedUpdate.add(iv);
							needUpdating.addAll(iv.getTransitions());
						}
						else if(iv.isLessThan() && currentarray[ineqIndex] != 0){
							// If we are an inequality of the form v<=a, v<a, 
							// a>=v, or a>v, then the inequality is treated as false
							// when the rate is positive. So if the inequality is true
							// add it to the inequalities to change.
							rateIneqNeedUpdate.add(iv);
							needUpdating.addAll(iv.getTransitions());
						}
					}
					else{
						// The rate is negative, so reverse the logic in the above.
						if(iv.isGreaterThan() && currentarray[ineqIndex] != 0){
							// If we are an inequality of the form v>=a, v>a, 
							// a<=v, or a<v, then the inequality is treated as false
							// when the rate is negative. So if the inequality is true
							// add it to the inequalities to change.
							rateIneqNeedUpdate.add(iv);
							needUpdating.addAll(iv.getTransitions());
						}
						else if(iv.isLessThan() && currentarray[ineqIndex] != 1){
							// If we are an inequality of the form v<=a, v<a, 
							// a>=v, or a>v, then the inequality is treated as true
							// when the rate is negative. So if the inequality is false
							// add it to the inequalities to change.
							rateIneqNeedUpdate.add(iv);
							needUpdating.addAll(iv.getTransitions());
						}
					}
				}
			}
			
		}
		
		// If inequalities have changed, get a new copy of the current state. This is assuming
		// only one local state will change.
		if(ineqNeedUpdating.size() != 0 || rateIneqNeedUpdate.size() != 0){
			newStates[this.getLpn().getLpnIndex()] = newStates[this.getLpn().getLpnIndex()].clone();
		}

		
		// Get the vector to change.
		int[] newVectorArray = newStates[this.lpn.getLpnIndex()].getVariableVector();
		
		// Update the inequalities.
		for(InequalityVariable ineq : ineqNeedUpdating){
			int ineqIndex = this.lpn.getVarIndexMap().
					get(ineq.getName());

			// Add the transitions that this inequality affects.
			needUpdating.addAll(ineq.getTransitions());
			

			HashMap<LPNContAndRate, IntervalPair> continuousValues
							= new HashMap<LPNContAndRate, IntervalPair>();
			
			for(UpdateContinuous record : updateContinuousRecords.keySet()){
				continuousValues.put(record.get_lcrPair(), record.get_Value());
			}
//			continuousValues.putAll(newAssignValues.get(OLD_ZERO));
//			continuousValues.putAll(newAssignValues.get(NEW_NON_ZERO));
//			continuousValues.putAll(newAssignValues.get(NEW_ZERO));
//			continuousValues.putAll(newAssignValues.get(OLD_NON_ZERO));

			// Adjust state vector values for the inequality variables.
//<<<<<<< StateGraph.java
//			int[] newVectorArray = newStates[this.lpn.getLpnIndex()].getVector();
//=======
//			int[] newVectorArray = oldStates[this.lpn.getLpnIndex()].getVector();

//>>>>>>> 1.54
			
			newVectorArray[ineqIndex] = ineq.evaluate(newVectorArray, z, continuousValues);
		}
		
		
		// Update the inequalities.
		for(InequalityVariable ineq : rateIneqNeedUpdate){
			int ineqIndex = this.lpn.getVarIndexMap().
					get(ineq.getName());
			
			LPN lpn = ineq.get_lpn();
			
			// Build the index object
			LPNContinuousPair lcv =
					new LPNContinuousPair(lpn.getLpnIndex(),
							lpn.getContVarIndex(ineq.getContVariables().get(0).getName()));
			
			// Get the record value.
			UpdateContinuous upc = updateContinuousRecords.get(lcv);
			
			// Get the current rate.
			int rate = upc.get_lcrPair().get_lcPair().getCurrentRate();
			
			// TODO: add logic to correctly evaluate the inequality due to rates.
			
			int newvalue = 0;
			
			if(ineq.isGreaterThan() && rate > 0){
				newvalue = 1;
			}
			else if(ineq.isGreaterThan() && rate < 0){
				newvalue = 0;
			}
			else if(ineq.isLessThan() && rate > 0){
				newvalue = 0;
			}
			else if(ineq.isLessThan() && rate < 0){
				newvalue = 1;
			}
			
			// Flip the value of the inequality variable.
//			newVectorArray[ineqIndex] = newVectorArray[ineqIndex] == 0 ? 1 : 0;
			newVectorArray[ineqIndex] = newvalue;
		}
			
			
		// Do the re-evaluating of the transitions.
		for(Transition t : needUpdating){
			// Get the index of the LPN and the index of the transition.
			int lpnIndex = t.getLpn().getLpnIndex();
			int tranIndex = t.getIndex();
			
			// Get the enabled vector from the appropriate state.
			int[] vector = newStates[lpnIndex].getVariableVector();
			int[] marking = newStates[lpnIndex].getMarking();
			boolean[] previouslyEnabled = newStates[lpnIndex].getTranVector();
			
			// Set the (possibly) new value.
			
			boolean needToUpdate = true;
        	String tranName = t.getLabel();
    		if (this.lpn.getEnablingTree(tranName) != null 
    				&& this.lpn.getEnablingTree(tranName)
    					.evaluateExpr(this.lpn.getAllVarsWithValuesAsString(vector)) == 0.0) {
    		   	if (previouslyEnabled[tranIndex] && !t.isPersistent())
    				previouslyEnabled[tranIndex] = false;
    			continue;
    		}
    		
    		if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
    			for (int place : this.lpn.getPresetIndex(tranName)) {
    				if (marking[place]==0) {
    					needToUpdate = false;
    					break;
    				}
    			}
    		}
			if (needToUpdate) {
            	// if a transition is enabled and it is not recorded in the enabled transition vector
    			previouslyEnabled[tranIndex] = true;
            }
			
		}
		
//		return newAssignValues;
//		return updateContinuousRecords;
		
		return newStates;
	}
	
    @Override
	public String toString() {
		return "StateGraph [lpn=" + lpn + "]";
	}
    
	/**
	 * Find all locally enabled transitions in the initial local state, and construct the corresponding tranVector in this state.
	 * @param initialVector
	 * @return
	 */
	public boolean[] genInitTranVector(int[] initialVector) {
		boolean[] initEnabledTrans = new boolean[this.lpn.getAllTransitions().length];
		for (int i=0; i< this.lpn.getAllTransitions().length; i++) {
			Transition transition = this.lpn.getAllTransitions()[i];
			Place[] tranPreset = this.lpn.getTransition(transition.getLabel()).getPreset(); 
			String tranName = transition.getLabel();
			boolean presetNotMarked = false;
			if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
				for (int j=0; j<tranPreset.length; j++) {
					if (!tranPreset[j].isMarked()) {
						initEnabledTrans[i] = false;
						presetNotMarked = true;
						break;
					}
				}
			}
			if (presetNotMarked) {
				presetNotMarked = false;
				continue;
			}
			if (this.lpn.getEnablingTree(tranName) != null && this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(initialVector)) == 0.0) {
				initEnabledTrans[i] = false;
				continue;
			}
			else if (this.lpn.getTransitionRateTree(tranName) != null && this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(initialVector)) == 0.0) {
				initEnabledTrans[i] = false;
				continue;
			}
			else {
				initEnabledTrans[i] = true;
			}			
		}
		return initEnabledTrans;
	}

	public void printNextStateForGivenState(State givenState, String location) {
		System.out.println("----------------Next State Map @ " + location + "----------------");
		System.out.println("state = " + givenState.getFullLabel());
		HashMap<Transition, State> nextStateMapForGivenState = nextStateMap.get(givenState);		
		if (nextStateMapForGivenState == null) {    		
			System.out.println("No entry for " + givenState.getFullLabel() + " in nextStateMap");
		}
		else {
			for (Transition t: nextStateMapForGivenState.keySet()) {
				State nextState = nextStateMapForGivenState.get(t);    			
				System.out.println(t.getFullLabel() + " -> S" + nextState.getIndex() + "(" + nextState.getLpn().getLabel() + ")");
			}
		}	
		System.out.println("--------------End Of Next State Map----------------------");

	}
}
