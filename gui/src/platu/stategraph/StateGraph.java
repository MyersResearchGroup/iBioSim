package platu.stategraph;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import lpn.parser.LhpnFile;
import platu.common.IndexObjMap;
import platu.expression.Expression;
import platu.expression.VarNode;
import platu.logicAnalysis.Constraint;
import platu.lpn.DualHashMap;
import platu.lpn.LPN;
import platu.lpn.LPNTran;
import platu.lpn.LpnTranList;
import platu.lpn.VarExpr;
import platu.main.Main;
import platu.main.Options;

public class StateGraph {
    protected State init = null;
    protected IndexObjMap<State> stateCache;
    protected IndexObjMap<State> localStateCache;
    protected HashMap<State, State> state2LocalMap;
    protected HashMap<State, LpnTranList> enabledSetTbl;
    protected HashMap<State, HashMap<LPNTran, State>> nextStateMap;
    protected List<State> stateSet = new LinkedList<State>();
    protected List<State> frontierStateSet = new LinkedList<State>();
    protected List<State> entryStateSet = new LinkedList<State>();
    protected List<Constraint> oldConstraintSet = new LinkedList<Constraint>();
    protected List<Constraint> newConstraintSet = new LinkedList<Constraint>();
    protected List<Constraint> frontierConstraintSet = new LinkedList<Constraint>();
    protected Set<Constraint> constraintSet = new HashSet<Constraint>();
    protected LhpnFile lpn;
    
    public LhpnFile getLpn(){
    	return this.lpn;
    }
    
    public StateGraph(LhpnFile lpn) {
    	this.lpn = lpn;
    	
        this.stateCache = new IndexObjMap<State>();
        this.localStateCache = new IndexObjMap<State>();
        this.state2LocalMap = new HashMap<State, State>();
        this.enabledSetTbl = new HashMap<State, LpnTranList>();
        this.nextStateMap = new HashMap<State, HashMap<LPNTran, State>>();
    }
    
    public void printStates(){
    	System.out.println(String.format("%-8s    %5s", this.lpn.getLabel(), "|States| = " + stateCache.size()));
    }

    public Set<LPNTran> getTranList(State currentState){
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

            for (LPNTran firedTran : currentEnabledTransitions) {
                State newState = firedTran.constrFire(currentState);
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
            		// TODO: check that a variable was changed before creating a constraint
	            	if(!firedTran.local()){
	            		for(LPN lpn : firedTran.getDstLpnList()){
	                  		Constraint c = new Constraint(currentState, nextState, firedTran, lpn);
	                  		lpn.getStateGraph().addConstraint(c);
	        			}
	            	}
        		}

            	if(!newStateFlag) continue;
            	
            	LpnTranList nextEnabledTransitions = getEnabled(nextState);
                if (nextEnabledTransitions.isEmpty()) continue;
                
//                currentEnabledTransitions = getEnabled(nexState);
//                LPNTran disabledTran = firedTran.disablingError(currentEnabledTransitions, nextEnabledTransitions);
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
            
            for (LPNTran firedTran : currentEnabledTransitions) {
                State st = firedTran.constrFire(currentState);
                State nextState = addState(st);

                newStateFlag = false;
            	if(nextState == st){
            		newStateFlag = true;
            		addFrontierState(nextState);
            	}
        		
            	if(nextState != currentState){
	        		newTransitions++;
	        		
	            	if(!firedTran.local()){
	            		// TODO: check that a variable was changed before creating a constraint
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
//                LPNTran disabledTran = firedTran.disablingError(currentEnabledTransitions, nextEnabledTransitions);
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
    	
    	// TODO: (Done) Create a variable index map 
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
    		
    		
    		for(Entry<LPNTran, State> stateTran : this.nextStateMap.get(state).entrySet()){
    			State tailState = state;
    			State headState = stateTran.getValue();
    			LPNTran lpnTran = stateTran.getKey();
    			
    			String edgeLabel = lpnTran.getLabel() + ": ";
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
    
    /*
     * Return the enabled transitions in the state with index 'stateIdx'.
     */
    public LpnTranList getEnabled(int stateIdx) {
    	State curState = this.getState(stateIdx);
        return this.getEnabled(curState);
    }

    // Return the set of all LPN transitions that are enabled in 'state'.
    public LpnTranList getEnabled(State curState) {
    	if (curState == null) {
            throw new NullPointerException();
        }
    	
    	if(enabledSetTbl.containsKey(curState) == true){
            return (LpnTranList)enabledSetTbl.get(curState).clone();
        }
    	
        LpnTranList curEnabled = new LpnTranList();
        // TODO: (!) Change this to get transitions from the LPN, but compute the enabling in a function here in the StateGraph class
        for (String tran: this.lpn.getTransitionList()) {
        	
        }
        /*
        for (LPNTran tran : this.lpn.getTransitions()) {
        	if (tran.isEnabled(curState)) {
        		if(tran.local()==true)
        			curEnabled.addLast(tran);
                else
                	curEnabled.addFirst(tran);
             } 
        }
        */
        
        this.enabledSetTbl.put(curState, curEnabled);
        return curEnabled;
    }
    
    // TODO: (!) Change isEnabled to work with our transitions.
    /*
    final public boolean isEnabled(final State curState) {
        if (curState == null) {
            throw new NullPointerException();
        }

		if (this.preSet != null && this.preSet.length > 0) {
			for (int pp : this.preSet) {
				int[] curMarking = curState.getMarking();
				boolean included = false;
				
				for (int i = 0; i < curMarking.length; i++) {
					if (curMarking[i] == pp) {
						included = true;
						break;
					}
				}
				
				if (included == false)
					return false;
            }
		}
        
        int[] curVector = curState.getVector();
        if (curVector.length > 0) {
            if(getEnablingGuard().evaluate(curVector) == 0)
                return false;
        }

        return true;
    }
	*/
	
    public int reachSize() {
    	if(this.stateCache == null){
    		return this.stateSet.size();
    	}
    	
		return this.stateCache.size();
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
    
    public void addStateTran(State curSt, LPNTran firedTran, State nextSt) {
    	HashMap<LPNTran, State> nextMap = this.nextStateMap.get(curSt);
    	if(nextMap == null)  {
    		nextMap = new HashMap<LPNTran,State>();
    		nextMap.put(firedTran, nextSt);
    		this.nextStateMap.put(curSt, nextMap);
    	}
    	else
    		nextMap.put(firedTran, nextSt);
    }
    
    public State getNextState(State curSt, LPNTran firedTran) {
    	HashMap<LPNTran, State> nextMap = this.nextStateMap.get(curSt);
    	if(nextMap == null)
    		return null;
    	
    	return nextMap.get(firedTran);
    }
    
    private static Set<Entry<LPNTran, State>> emptySet = new HashSet<Entry<LPNTran, State>>(0);
    public Set<Entry<LPNTran, State>> getOutgoingTrans(State currentState){
    	HashMap<LPNTran, State> tranMap = this.nextStateMap.get(currentState);
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
    		// TODO: (?) Should we convert all boolean, continuous and integer values to int? 
    		int val = this.lpn.getInitVector(var);// this.initVector.get(var);
    		initialVector[i] = val;
    	}
		return new State(this.lpn, this.lpn.getInitalMarkingsArray(), initialVector);
    }
    
    /**
     * Fire a transition on a state array, find new local states, and return the new state array formed by the new local states.
     * @param curLpnArray
     * @param curStateArray
     * @param curLpnIndex
     * @return
     */
    /*
    public State[] fire(final StateGraph[] curSgArray, final int[] curStateIdxArray) {
    	State[] stateArray = new State[curSgArray.length];
    	for(int i = 0; i < curSgArray.length; i++)
    		stateArray[i] = curSgArray[i].getState(curStateIdxArray[i]);

    	return this.fire(curSgArray, stateArray);
    }

    public State[] fire(final StateGraph[] curSgArray, final State[] curStateArray) {
    	int thisLpnIndex = this.getLpn().getIndex(); 
    	State[] nextStateArray = curStateArray.clone();
    	
    	State curState = curStateArray[thisLpnIndex];
    	State nextState = this.fire(curSgArray[thisLpnIndex], curState);   
    	
    	int[] nextVector = nextState.getVector();
    	int[] curVector = curState.getVector();
    	
        for(Expression e : assertions){
        	if(e.evaluate(nextVector) == 0){
        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
        		System.exit(1);
			}
		}

        if(this.local()==true) {
    		nextStateArray[thisLpnIndex] = curSgArray[thisLpnIndex].addState(nextState);
        	return nextStateArray;
		}

        HashMap<String, Integer> vvSet = new HashMap<String, Integer>();
        for (VarExpr s : this.getAssignments()) {
            int newValue = nextVector[s.getVar().getIndex(curVector)];
            vvSet.put(s.getVar().getName(), newValue);   
        }
        
        
        // Update other local states with the new values generated for the shared variables.
		nextStateArray[this.lpn.getIndex()] = nextState;
        for(LPN curLpn : this.dstLpnList) {
        	int curIdx = curLpn.getIndex();
    		State newState = curSgArray[curIdx].getNextState(curStateArray[curIdx], this);
    		if(newState != null) 
        		nextStateArray[curIdx] = newState;
        	else {
        		// TODO: may not need to be updated, but could change to use our var index map
        		
        		State newOther = curStateArray[curIdx].update(vvSet, curSgArray[curIdx].getLpn().getVarIndexMap());
        		if (newOther == null)
        			nextStateArray[curIdx] = curStateArray[curIdx];
        		else {
        			State cachedOther = curSgArray[curIdx].addState(newOther);
					//nextStateArray[curIdx] = newOther;
            		nextStateArray[curIdx] = cachedOther;
            		curSgArray[curIdx].addStateTran(curStateArray[curIdx], this, cachedOther);
        		}
        		
        	}
        }
        
        return nextStateArray;
    }
    
    
    public State fire(final StateGraph thisSg, final State curState) {  		
    	// Search for and return cached next state first. 
//    	if(this.nextStateMap.containsKey(curState) == true)
//    		return (State)this.nextStateMap.get(curState);
    	
    	State nextState = thisSg.getNextState(curState, this);
    	if(nextState != null)
    		return nextState;
    	
    	// If no cached next state exists, do regular firing. 
    	// Marking update
        int[] curOldMarking = curState.getMarking();
        int[] curNewMarking = null;
        if(preSet.length==0 && postSet.length==0)
        	curNewMarking = curOldMarking;
		else {
			curNewMarking = new int[curOldMarking.length - preSet.length + postSet.length];
			int index = 0;
			for (int i : curOldMarking) {
				boolean existed = false;
				for (int prep : preSet) {
					if (i == prep) {
						existed = true;
						break;
					}
				}
				if (existed == false) {
					curNewMarking[index] = i;
					index++;
				}
			}
			for (int postp : postSet) {
				curNewMarking[index] = postp;
				index++;
			}
        }

        //  State vector update
        int[] newVectorArray = curState.getVector().clone();
        int[] curVector = curState.getVector();
        
        for (VarExpr s : getAssignments()) {
            int newValue = (int) s.getExpr().evaluate(curVector);
            newVectorArray[s.getVar().getIndex(curVector)] = newValue;
        }
        
        State newState = thisSg.addState(new State(this.lpn, curNewMarking, newVectorArray));
        
        int[] newVector = newState.getVector();
		for(Expression e : assertions){
        	if(e.evaluate(newVector) == 0){
        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
        		System.exit(1);
        	}
        }
		
		thisSg.addStateTran(curState, this, newState);
		return newState;
    }
    
    public State constrFire(final State curState) {
    	// Marking update
        int[] curOldMarking = curState.getMarking();
        int[] curNewMarking = null;
        if(this.preSet.length==0 && this.postSet.length==0){
        	curNewMarking = curOldMarking;
        }
		else {
			curNewMarking = new int[curOldMarking.length - this.preSet.length + this.postSet.length];
			int index = 0;			
			for (int i : curOldMarking) {
				boolean existed = false;
				for (int prep : this.preSet) {
					if (i == prep) {
						existed = true;
						break;
					}
					else if(prep > i){
						break;
					}
				}
				
				if (existed == false) {
					curNewMarking[index] = i;
					index++;
				}
			}
			
			for (int postp : postSet) {
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
        for (VarExpr s : getAssignments()) {
            int newValue = s.getExpr().evaluate(curVector);
            newVectorArray[s.getVar().getIndex(curVector)] = newValue;
        }
        
        State newState = new State(this.lpn, curNewMarking, newVectorArray);
        
        int[] newVector = newState.getVector();
		for(Expression e : assertions){
        	if(e.evaluate(newVector) == 0){
        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
        		System.exit(1);
        	}
        }
		
		return newState;
    }
    */
    
}
