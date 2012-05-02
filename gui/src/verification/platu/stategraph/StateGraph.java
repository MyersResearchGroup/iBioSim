package verification.platu.stategraph;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;
import verification.platu.common.IndexObjMap;
import verification.platu.logicAnalysis.Constraint;
import verification.platu.lpn.DualHashMap;
import verification.platu.lpn.LpnTranList;
import verification.platu.main.Main;
import verification.platu.main.Options;
import verification.platu.project.PrjState;

public class StateGraph {
    protected State init = null;
    protected IndexObjMap<State> stateCache;
    protected IndexObjMap<State> localStateCache;
    protected HashMap<State, State> state2LocalMap;
    protected HashMap<State, LpnTranList> enabledSetTbl;
    protected HashMap<State, HashMap<Transition, State>> nextStateMap;
    protected List<State> stateSet = new LinkedList<State>();
    protected List<State> frontierStateSet = new LinkedList<State>();
    protected List<State> entryStateSet = new LinkedList<State>();
    protected List<Constraint> oldConstraintSet = new LinkedList<Constraint>();
    protected List<Constraint> newConstraintSet = new LinkedList<Constraint>();
    protected List<Constraint> frontierConstraintSet = new LinkedList<Constraint>();
    protected Set<Constraint> constraintSet = new HashSet<Constraint>();
    protected LhpnFile lpn;
    
    public StateGraph(LhpnFile lpn) {
    	this.lpn = lpn;   	
        this.stateCache = new IndexObjMap<State>();
        this.localStateCache = new IndexObjMap<State>();
        this.state2LocalMap = new HashMap<State, State>();
        this.enabledSetTbl = new HashMap<State, LpnTranList>();
        this.nextStateMap = new HashMap<State, HashMap<Transition, State>>();
    }
    
    public LhpnFile getLpn(){
    	return this.lpn;
    }
    
    public void printStates(){
    	System.out.println(String.format("%-8s    %5s", this.lpn.getLabel(), "|States| = " + stateCache.size()));
    }

    public Set<Transition> getTranList(State currentState){
    	return this.nextStateMap.get(currentState).keySet();
    }
    
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
        LpnTranList currentEnabledTransitions = getEnabled(baseState);
        
        stStack.push(baseState);
        tranStack.push((LpnTranList) currentEnabledTransitions);
        
        while (true){
            ptr--;
            State currentState = stStack.pop();
            currentEnabledTransitions = tranStack.pop();

            for (Transition firedTran : currentEnabledTransitions) {
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
	            	if(!firedTran.local()){
	            		for(LhpnFile lpn : firedTran.getDstLpnList()){
	            			// TODO: (temp) Hack here.
	                  		Constraint c = null; //new Constraint(currentState, nextState, firedTran, lpn);
	                  		// TODO: (temp) Ignore constraint.
	                  		//lpn.getStateGraph().addConstraint(c);
	        			}
	            	}
        		}

            	if(!newStateFlag) continue;
            	
            	LpnTranList nextEnabledTransitions = getEnabled(nextState);
                if (nextEnabledTransitions.isEmpty()) continue;
                
//                currentEnabledTransitions = getEnabled(nexState);
//                Transition disabledTran = firedTran.disablingError(currentEnabledTransitions, nextEnabledTransitions);
//                if(disabledTran != null) {
//                    System.out.println("Verification failed: " +disabledTran.getFullLabel() + " is disabled by " + 
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
        tranStack.push((LpnTranList) currentEnabledTransitions);
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
	        		
	            	if(!firedTran.local()){
	            		// TODO: (original) check that a variable was changed before creating a constraint
	            		for(LhpnFile lpn : firedTran.getDstLpnList()){
	            			// TODO: (temp) Hack here. 
	                  		Constraint c = null; //new Constraint(currentState, nextState, firedTran, lpn);
	                  	// TODO: (temp) Ignore constraints.
	                  		//lpn.getStateGraph().synchronizedAddConstraint(c);
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
    		int[] vector = state.getVector();

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
    			
    			String edgeLabel = lpnTran.getName() + ": ";
        		int[] headVector = headState.getVector();
        		int[] tailVector = tailState.getVector();
        		
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
     * Return the set of all LPN transitions that are enabled in 'state'.
     * @param curState
     * @return
     */
    public LpnTranList getEnabled(State curState) {
    	if (curState == null) {
            throw new NullPointerException();
        }
    	
    	if(enabledSetTbl.containsKey(curState) == true){
            return (LpnTranList)enabledSetTbl.get(curState).clone();
        }
    	
        LpnTranList curEnabled = new LpnTranList();
        for (Transition tran : this.lpn.getAllTransitions()) {
        	if (isEnabled(tran,curState)) {
        		if(tran.local()==true)
        			curEnabled.addLast(tran);
                else
                	curEnabled.addFirst(tran);
             } 
        }
        this.enabledSetTbl.put(curState, curEnabled);
        return curEnabled;
    }
    
    /**
     * Return the set of all LPN transitions that are enabled in 'state'.
     * @param curState
     * @return
     */
    public LpnTranList getEnabled(State curState, boolean init) {
    	if (curState == null) {
            throw new NullPointerException();
        }   	
    	if(enabledSetTbl.containsKey(curState) == true){
    		if (Options.getDebugMode()) {
    			System.out.println("~~~~~~~ existing state in enabledSetTbl for LPN" + curState.getLpn().getLabel() + ": S" + curState.getIndex() + "~~~~~~~~");
        		printTransitionSet((LpnTranList)enabledSetTbl.get(curState), "enabled trans at this state ");
    		}
            return (LpnTranList)enabledSetTbl.get(curState).clone();
        }   	
        LpnTranList curEnabled = new LpnTranList();
        //System.out.println("----Enabled transitions----");
        if (init) {
        	for (Transition tran : this.lpn.getAllTransitions()) {
            	if (isEnabled(tran,curState)) {
            		if (Options.getDebugMode())
            			System.out.println("Transition " + tran.getLpn().getLabel() + "(" + tran.getName() + ") is enabled");
            		if(tran.local()==true)
            			curEnabled.addLast(tran);
                    else
                    	curEnabled.addFirst(tran);
                 } 
            }
        }
        else {
        	for (int i=0; i < this.lpn.getAllTransitions().length; i++) {
        		Transition tran = this.lpn.getAllTransitions()[i];
        		if (curState.getTranVector()[i])
        			if(tran.local()==true)
            			curEnabled.addLast(tran);
                    else
                    	curEnabled.addFirst(tran);
        			
        	}
        }
        this.enabledSetTbl.put(curState, curEnabled);
        if (Options.getDebugMode()) {
        	System.out.println("~~~~~~~~ State S" + curState.getIndex() + " does not exist in enabledSetTbl for LPN " + curState.getLpn().getLabel() + ". Add to enabledSetTbl.");
        	printEnabledSetTbl();
        }
        return curEnabled;
    }
    
    private void printEnabledSetTbl() {
    	System.out.println("******* enabledSetTbl**********");
    	for (State s : enabledSetTbl.keySet()) {
    		System.out.print("S" + s.getIndex() + " -> ");
    		printTransitionSet(enabledSetTbl.get(s), "");
    	}	
	}
    
    public boolean isEnabled(Transition tran, State curState) {	   	
			int[] varValuesVector = curState.getVector();
			String tranName = tran.getName();
			int tranIndex = tran.getIndex();
			if (Options.getDebugMode())
				System.out.println("Checking " + tran);
			if (this.lpn.getEnablingTree(tranName) != null 
					&& this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsAndValues(varValuesVector)) == 0.0
					&& !(tran.isPersistent() && curState.getTranVector()[tranIndex])) {
				if (Options.getDebugMode())
					System.out.println(tran.getName() + " " + "Enabling condition is false");
				return false;
			}
			if (this.lpn.getTransitionRateTree(tranName) != null 
					&& this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsAndValues(varValuesVector)) == 0.0) {
				if (Options.getDebugMode())
					System.out.println("Rate is zero");
				return false;
			}
			if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
				int[] curMarking = curState.getMarking();
				for (int place : this.lpn.getPresetIndex(tranName)) {
					if (curMarking[place]==0) {
						if (Options.getDebugMode())
							System.out.println(tran.getName() + " " + "Missing a preset token");
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
    
	public boolean stateOnStack(State curState, HashSet<PrjState> stateStack) {
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
	
    /*
     * Add the module state mState into the local cache, and also add its local portion into
     * the local portion cache, and build the mapping between the mState and lState for fast lookup
     * in the future.
     */
    public State addState(State mState) {
    	State cachedState = this.stateCache.add(mState);
    	State lState = this.state2LocalMap.get(cachedState);
    	if(lState == null) {
    		lState = cachedState.getLocalState();
    		lState = this.localStateCache.add(lState);
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
    	else
    		nextMap.put(firedTran, nextSt);
    }
    
    public State getNextState(State curSt, Transition firedTran) {
    	HashMap<Transition, State> nextMap = this.nextStateMap.get(curSt);
    	if(nextMap == null)
    		return null;   	
    	return nextMap.get(firedTran);
    }
    
    private static Set<Entry<Transition, State>> emptySet = new HashSet<Entry<Transition, State>>(0);
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
    
    public State getInitState() {	
    	// create initial vector
		int size = this.lpn.getVarIndexMap().size();
    	int[] initialVector = new int[size];
    	for(int i = 0; i < size; i++) {
    		String var = this.lpn.getVarIndexMap().getKey(i);
    		int val = this.lpn.getInitVector(var);// this.initVector.get(var);
    		initialVector[i] = val;
    	}
		return new State(this.lpn, this.lpn.getInitialMarkingsArray(), initialVector, this.lpn.getInitEnabledTranArray(initialVector));
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

    // This method is called by search_dfs(StateGraph[], State[]).
    public State[] fire(final StateGraph[] curSgArray, final State[] curStateArray, Transition firedTran) {
    	int thisLpnIndex = this.getLpn().getLpnIndex(); 
    	State[] nextStateArray = curStateArray.clone();
    	
    	State curState = curStateArray[thisLpnIndex];
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
		if(firedTran.local()==true) {
//    		nextStateArray[thisLpnIndex] = curSgArray[thisLpnIndex].addState(nextState);
        	return nextStateArray;
		}
		
        HashMap<String, Integer> vvSet = new HashMap<String, Integer>();
        vvSet = this.lpn.getAllVarsWithValues(nextState.getVector());
//        for (String key : this.lpn.getAllVarsAndValues(curVector).keySet()) {
//        	if (this.lpn.getBoolAssignTree(firedTran.getName(), key) != null) {
//        		int newValue = (int)this.lpn.getBoolAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
//        		vvSet.put(key, newValue);
//        	}
//        	// TODO: (temp) type cast continuous variable to int.
//        	if (this.lpn.getContAssignTree(firedTran.getName(), key) != null) {
//        		int newValue = (int)this.lpn.getContAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
//        		vvSet.put(key, newValue);
//        	}
//        	if (this.lpn.getIntAssignTree(firedTran.getName(), key) != null) {
//        		int newValue = (int)this.lpn.getIntAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
//        		vvSet.put(key, newValue);
//        	}
//        }       
        /*
        for (VarExpr s : this.getAssignments()) {
            int newValue = nextVector[s.getVar().getIndex(curVector)];
            vvSet.put(s.getVar().getName(), newValue);   
        }
        */
        
        // Update other local states with the new values generated for the shared variables.
		//nextStateArray[this.lpn.getIndex()] = nextState;      
//		if (!firedTran.getDstLpnList().contains(this.lpn))
//			firedTran.getDstLpnList().add(this.lpn);
		for(LhpnFile curLPN : firedTran.getDstLpnList()) {
        	int curIdx = curLPN.getLpnIndex();
//			System.out.println("Checking " + curLPN.getLabel() + " " + curIdx);
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
        		}   		
        	}
        }
        return nextStateArray;
    }
    
    // TODO: (original) add transition that fires to parameters
    public State fire(final StateGraph thisSg, final State curState, Transition firedTran) {  		
    	// Search for and return cached next state first. 
//    	if(this.nextStateMap.containsKey(curState) == true)
//    		return (State)this.nextStateMap.get(curState);
    	
    	State nextState = thisSg.getNextState(curState, firedTran);
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
			for (int prep : this.lpn.getPresetIndex(firedTran.getName())) {
				curNewMarking[prep]=0;
			}
			for (int postp : this.lpn.getPostsetIndex(firedTran.getName())) {
				curNewMarking[postp]=1;
			}
        }

        //  State vector update
        int[] newVectorArray = curState.getVector().clone();
        int[] curVector = curState.getVector();      
        for (String key : this.lpn.getAllVarsAndValues(curVector).keySet()) {
        	if (this.lpn.getBoolAssignTree(firedTran.getName(), key) != null) {
        		int newValue = (int)this.lpn.getBoolAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
        		newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
        	}
        	// TODO: (temp) type cast continuous variable to int.
        	if (this.lpn.getContAssignTree(firedTran.getName(), key) != null) {
        		int newValue = (int)this.lpn.getContAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
        		newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
        	}
        	if (this.lpn.getIntAssignTree(firedTran.getName(), key) != null) {
        		int newValue = (int)this.lpn.getIntAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
        		newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
        	}
        } 
        /*
        for (VarExpr s : firedTran.getAssignments()) {
            int newValue = (int) s.getExpr().evaluate(curVector);
            newVectorArray[s.getVar().getIndex(curVector)] = newValue;
        }
        */
        // Enabled transition vector update
        boolean[] newEnabledTranVector = updateEnabledTranVector(curState.getTranVector(), curNewMarking, newVectorArray, firedTran);
        State newState = thisSg.addState(new State(this.lpn, curNewMarking, newVectorArray, newEnabledTranVector));
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
		return newState;
    }
    
    public boolean[] updateEnabledTranVector(boolean[] enabledTranBeforeFiring,
			int[] newMarking, int[] newVectorArray, Transition firedTran) {
    	boolean[] enabledTranAfterFiring = enabledTranBeforeFiring.clone();
		// firedTran is disabled
    	if (firedTran != null) {
    		enabledTranAfterFiring[firedTran.getIndex()] = false;
    		for (Iterator<Integer> conflictIter = firedTran.getConflictSetTransIndices().iterator(); conflictIter.hasNext();) {
    			Integer curConflictingTranIndex = conflictIter.next();
    			enabledTranAfterFiring[curConflictingTranIndex] = false;
    		}
    	}
        // find newly enabled transition(s) based on the updated markings and variables
        for (Transition tran : this.lpn.getAllTransitions()) {
        	boolean needToUpdate = true;
        	String tranName = tran.getName();
    		int tranIndex = tran.getIndex();
    		//System.out.println("Checking " + tran);
    		if (this.lpn.getEnablingTree(tranName) != null 
    				&& this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsAndValues(newVectorArray)) == 0.0) {
    			//System.out.println(tran.getName() + " " + "Enabling condition is false");
    			continue;
    		}
    		if (this.lpn.getTransitionRateTree(tranName) != null 
    				&& this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsAndValues(newVectorArray)) == 0.0) {
    			//System.out.println("Rate is zero");
    			continue;
    		}
    		if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
    			for (int place : this.lpn.getPresetIndex(tranName)) {
    				if (newMarking[place]==0) {
    					//System.out.println(tran.getName() + " " + "Missing a preset token");
    					needToUpdate = false;
    					break;
    				}
    			}
    		}
			if (needToUpdate) {
            	// if a transition is enabled and it is not recorded in the enabled transition vector
    			enabledTranAfterFiring[tranIndex] = true;
            }
        }
    	return enabledTranAfterFiring;
	}

	public State constrFire(Transition firedTran, final State curState) {
    	// Marking update
        int[] curOldMarking = curState.getMarking();
        int[] curNewMarking = null;
        if(firedTran.getPreset().length==0 && firedTran.getPostset().length==0){
        	curNewMarking = curOldMarking;
        }
		else {
			curNewMarking = new int[curOldMarking.length - firedTran.getPreset().length + firedTran.getPostset().length];
			int index = 0;			
			for (int i : curOldMarking) {
				boolean existed = false;
				for (int prep : this.lpn.getPresetIndex(firedTran.getName())) {
					if (i == prep) {
						existed = true;
						break;
					}
					// TODO: (??) prep > i
					else if(prep > i){
						break;
					}
				}
				
				if (existed == false) {
					curNewMarking[index] = i;
					index++;
				}
			}
			
			for (int postp : this.lpn.getPostsetIndex(firedTran.getName())) {
				curNewMarking[index] = postp;
				index++;
			}
        }

        //  State vector update
        int[] oldVector = curState.getVector();
        int size = oldVector.length;
        int[] newVectorArray = new int[size];
        System.arraycopy(oldVector, 0, newVectorArray, 0, size);
        
        int[] curVector = curState.getVector();
        for (String key : this.lpn.getAllVarsAndValues(curVector).keySet()) {
        	if (this.lpn.getBoolAssignTree(firedTran.getName(), key) != null) {
        		int newValue = (int)this.lpn.getBoolAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
        		newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
        	}
        	// TODO: (temp) type cast continuous variable to int.
        	if (this.lpn.getContAssignTree(firedTran.getName(), key) != null) {
        		int newValue = (int)this.lpn.getContAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
        		newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
        	}
        	if (this.lpn.getIntAssignTree(firedTran.getName(), key) != null) {
        		int newValue = (int)this.lpn.getIntAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
        		newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
        	}
        }       
        // TODO: (check) Is the update is equivalent to the one below?
        /*
        for (VarExpr s : getAssignments()) {
            int newValue = s.getExpr().evaluate(curVector);
            newVectorArray[s.getVar().getIndex(curVector)] = newValue;
        }
       */
         
        // Enabled transition vector update
        boolean[] newEnabledTranArray = curState.getTranVector();
        newEnabledTranArray[firedTran.getIndex()] = false;
        State newState = new State(this.lpn, curNewMarking, newVectorArray, newEnabledTranArray);
        
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
	
	public void outputLocalStateGraph(String file) {
		try {
			int size = this.lpn.getVarIndexMap().size();
			String varNames = "";
			for(int i = 0; i < size; i++) {
				varNames = varNames + ", " + this.lpn.getVarIndexMap().getKey(i);
	    	}
			varNames = varNames.replaceFirst(", ", "");
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
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
					String curTranName = curTran.getName();
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

	private String intArrayToString(String type, State curState) {
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
			for (int i=0; i< curState.getVector().length; i++) {
				arrayStr = arrayStr + curState.getVector()[i] + ",";
			}
			if (arrayStr.contains(","))
				arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}
		return arrayStr;
	}
	
	private String boolArrayToString(String type, State curState) {
		String arrayStr = "";
		if (type.equals("enabledTrans")) {
			for (int i=0; i< curState.getTranVector().length; i++) {
				if (curState.getTranVector()[i]) {
					arrayStr = arrayStr + curState.getLpn().getAllTransitions()[i].getName() + ",";
				}
			}
			if (arrayStr != "")
				arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}
		return arrayStr;
	}
	
	private void printTransitionSet(LpnTranList transitionSet, String setName) {
		if (!setName.isEmpty())
			System.out.print(setName + " ");
		if (transitionSet.isEmpty()) {
			System.out.println("empty");
		}
		else {
			for (Iterator<Transition> curTranIter = transitionSet.iterator(); curTranIter.hasNext();) {
				Transition tranInDisable = curTranIter.next();
				System.out.print(tranInDisable.getName() + " ");
			}
			System.out.print("\n");
		}
	}

	private static void printAmpleSet(LpnTranList transitionSet, String setName) {
		if (!setName.isEmpty())
			System.out.print(setName + " ");
		if (transitionSet.isEmpty()) {
			System.out.println("empty");
		}
		else {
			for (Iterator<Transition> curTranIter = transitionSet.iterator(); curTranIter.hasNext();) {
				Transition tranInDisable = curTranIter.next();
				System.out.print(tranInDisable.getName() + " ");
			}
			System.out.print("\n");
		}
	}
	
	public HashMap<State,LpnTranList> copyEnabledSetTbl() {
		HashMap<State,LpnTranList> copyEnabledSetTbl = new HashMap<State,LpnTranList>();
		for (State s : enabledSetTbl.keySet()) {
			LpnTranList tranList = enabledSetTbl.get(s).clone();
			copyEnabledSetTbl.put(s.clone(), tranList);
		}
		return copyEnabledSetTbl;
	}

	public void setEnabledSetTbl(HashMap<State, LpnTranList> enabledSetTbl) {
		this.enabledSetTbl = enabledSetTbl;	
	}

	public HashMap<State, LpnTranList> getEnabledSetTbl() {
		return this.enabledSetTbl;
	}

	public HashMap<State, HashMap<Transition, State>> getNextStateMap() {
		return this.nextStateMap;
	}
}
