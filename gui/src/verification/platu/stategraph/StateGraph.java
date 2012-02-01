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
    		//printCurState(curState);
            return (LpnTranList)enabledSetTbl.get(curState).clone();
        }
    	
        LpnTranList curEnabled = new LpnTranList();
        //System.out.println("----Enabled transitions----");
        for (Transition tran : this.lpn.getAllTransitions()) {
        	if (isEnabled(tran,curState)) {
        		//System.out.println("Transition " + tran.getName() + " is enabled");
        		if(tran.local()==true)
        			curEnabled.addLast(tran);
                else
                	curEnabled.addFirst(tran);
             } 
        }
        
        //printCurState(curState);
        
        this.enabledSetTbl.put(curState, curEnabled);
        return curEnabled;
    }
    
    /**
     * Return the enabled transitions in the state with index 'stateIdx'.
     * @param stateIdx
     * @param applyPOR
     * @return
     */
    public LpnTranList getEnabledWithPOR(int stateIdx, HashMap<Integer, HashSet<Integer>> disableSet, 
    		HashMap<Integer, HashSet<Integer>> disableByStealingToken, HashMap<Integer, HashSet<Integer>> enableSet) {
    	State curState = this.getState(stateIdx);
        return this.getEnabledWithPOR(curState, disableSet, disableByStealingToken, enableSet);
    }
    
    /**
     * Return the set of all LPN transitions that are enabled in 'state'.
     * @param curState
     * @param enable 
     * @param disableByStealingToken 
     * @param disable 
     * @return
     */
    public LpnTranList getEnabledWithPOR(State curState, HashMap<Integer, HashSet<Integer>> disable, 
    		HashMap<Integer, HashSet<Integer>> disableByStealingToken, HashMap<Integer, HashSet<Integer>> enable) {
    	if (curState == null) {
            throw new NullPointerException();
        }
     	if(enabledSetTbl.containsKey(curState) == true){
    		//printCurState(curState);
            return (LpnTranList)enabledSetTbl.get(curState).clone();
        }
        LpnTranList curEnabled = new LpnTranList();
        HashSet<Integer> curEnabledIndices = new HashSet<Integer>();
        HashSet<Integer> curNonStickyEnabledIndices = new HashSet<Integer>();
        boolean allEnabledAreSticky = true;
        for (int i=0; i < this.lpn.getAllTransitions().length; i++) {
        	Transition tran = this.lpn.getAllTransitions()[i];
        	if (isEnabled(tran,curState)){
        		//System.out.println("Transition " + tran.getName() + " is enabled");
        		curEnabledIndices.add(i);
        		allEnabledAreSticky = allEnabledAreSticky && tran.isSticky();
        		if (!tran.isSticky()) {
        			curNonStickyEnabledIndices.add(i);
        		}
             }
        }
        // Apply POR with traceback here.
        HashSet<Integer> ready = new HashSet<Integer>();
       	System.out.println("********************");
   		System.out.println("Begin POR:");
   		printIntegerSet(curEnabledIndices, "Enabled set");
   		//printIntegerSet(curNonStickyEnabledIndices, "Enabled set (non-sticky)");
   		ready = partialOrderReduction(curState, curEnabledIndices, disable, disableByStealingToken, enable, allEnabledAreSticky);
        printIntegerSet(ready, "Ready set");
        System.out.println("End POR");
  		System.out.println("********************");
        Object[] readyArray = ready.toArray();
        for (int i=0; i < readyArray.length; i++) {      	
        	Transition tran = this.lpn.getAllTransitions()[(Integer) readyArray[i]];
        	if(tran.local()==true)
    			curEnabled.addLast(tran);
            else
            	curEnabled.addFirst(tran);
        }          
        this.enabledSetTbl.put(curState, curEnabled);
        return curEnabled;
    }
 	@SuppressWarnings("unchecked")
	private HashSet<Integer> partialOrderReduction(State curState,
			HashSet<Integer> curEnabled, HashMap<Integer, HashSet<Integer>> disable, 
    		HashMap<Integer, HashSet<Integer>> disableByStealingToken, HashMap<Integer, 
    		HashSet<Integer>> enable, boolean allEnabledAreSticky) {
		HashSet<Integer> ready = (HashSet<Integer>) curEnabled.clone();
		if (!allEnabledAreSticky)
			for (Iterator<Integer> enabledSetIter = curEnabled.iterator(); enabledSetIter.hasNext();) {
				Integer enabledTran = enabledSetIter.next();
				HashSet<Integer> dependent = new HashSet<Integer>();
	//			System.out.println("currently enabled transition: " + this.lpn.getAllTransitions()[enabledTran]);
				Transition enabledTransition = this.lpn.getAllTransitions()[enabledTran];
				if (enabledTransition.isSticky()) {
					dependent = (HashSet<Integer>) ready.clone();
				}
				else {
					dependent = getDependentSet(curState,enabledTran,dependent,curEnabled,disable,disableByStealingToken,enable);
				}
				
				printIntegerSet(dependent, "dependent set for enabled transition " + this.lpn.getAllTransitions()[enabledTran]);
				// TODO: (??) temporarily dealing with dummy transitions (This requires the dummy transitions to have "_dummy" in their names.)				
				boolean dependentOnlyHasDummyTrans = true;
				for (Iterator<Integer> depIter = dependent.iterator(); depIter.hasNext();) {
					Integer curTranIndex = depIter.next();
					Transition curTran = this.lpn.getAllTransitions()[curTranIndex];
					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans && isDummyTran(curTran.getName());
				}			
				if (dependent.size() < ready.size() && !dependentOnlyHasDummyTrans) 
					ready = (HashSet<Integer>) dependent.clone();
				if (ready.size() == 1)
					return ready;
			}
		return ready;
	}

	private boolean isDummyTran(String tranName) {
		if (tranName.contains("_dummy"))
			return true;
		else
			return false;
	}

	private HashSet<Integer> getDependentSet(State curState,
			Integer tran, HashSet<Integer> dependent, HashSet<Integer> curEnabled, 
			HashMap<Integer,HashSet<Integer>> disable, HashMap<Integer,HashSet<Integer>> disableByStealingToken, 
			HashMap<Integer,HashSet<Integer>> enable) {
		dependent.add(tran);
//		printIntegerSet(dependent, "dependent set @ getDepdentSet:");
		HashSet<Integer> canDisable = disable.get(tran); // canDisable is the set of transitions that can be disabled by firing tran.
		HashSet<Integer> transCanLoseToken = disableByStealingToken.get(tran);
		for (Iterator<Integer> canDisableIter = canDisable.iterator(); canDisableIter.hasNext();) {
			Integer tranCanBeDisabled = canDisableIter.next();
			boolean tranCanBeDisabledPersistent = this.lpn.getAllTransitions()[tranCanBeDisabled].isPersistent();
			if (curEnabled.contains(tranCanBeDisabled) && !dependent.contains(tranCanBeDisabled)
					&& (transCanLoseToken.contains(tranCanBeDisabled) || !tranCanBeDisabledPersistent)) {
				dependent.addAll(getDependentSet(curState,tranCanBeDisabled,dependent,curEnabled,disable,
						disableByStealingToken,enable));
			}
			else if (!curEnabled.contains(tranCanBeDisabled)) {
				HashSet<Integer> necessary = //new HashSet<Integer>();
						getNecessarySet(curState,tranCanBeDisabled,dependent,curEnabled,enable);
				printIntegerSet(necessary, "necessary set for transition " + this.lpn.getAllTransitions()[tranCanBeDisabled]);
				for (Iterator<Integer> tranNecessaryIter = necessary.iterator(); tranNecessaryIter.hasNext();) {
					Integer tranNecessary = tranNecessaryIter.next();
					if (!dependent.contains(tranNecessary)) {
						dependent.addAll(getDependentSet(curState,tranNecessary,dependent,curEnabled,disable,
								disableByStealingToken,enable));
					}					
				}				
			}
		}
		return dependent;
	}

	@SuppressWarnings("unchecked")
	private HashSet<Integer> getNecessarySet(State curState,
			Integer tran, HashSet<Integer> dependent,
			HashSet<Integer> curEnabled,
			HashMap<Integer, HashSet<Integer>> enable) {
		HashSet<Integer> nMarking = null;
		Transition transition = this.lpn.getAllTransitions()[tran];
		int[] presetPlaces = this.lpn.getPresetIndex(transition.getName());
//		printIntegerArray(presetPlaces, "presetPlaces @ getNecessarySet");
		for (int i=0; i < presetPlaces.length; i++) {
			int place = presetPlaces[i];
			if (curState.getMarking()[place]==0) {
				HashSet<Integer> nMarkingTemp = new HashSet<Integer>();				
				String placeName = this.lpn.getAllPlaces().get(place);
				int[] presetTrans = this.lpn.getPresetIndex(placeName);
				for (int j=0; j < presetTrans.length; j++) {
					if (curEnabled.contains(presetTrans[j])) {
						nMarkingTemp.add(presetTrans[j]);
					}
					else {
						nMarkingTemp.addAll(getNecessarySet(curState, presetTrans[j], dependent, curEnabled, enable));
					}
				}
				if (nMarking == null || nMarkingTemp.size() < nMarking.size()) {
					nMarking = (HashSet<Integer>) nMarkingTemp.clone();
				}
			}
		}
		//printIntegerSet(nMarking, "nMarking = ");
		HashSet<Integer> nEnable = null;
		int[] varValueVector = curState.getVector();
		HashSet<Integer> canEnable = enable.get(tran);
		if (transition.getEnablingTree() != null
				&& transition.getEnablingTree().evaluateExpr(this.lpn.getAllVarsAndValues(varValueVector)) == 0.0) {
			nEnable = new HashSet<Integer>();
			for (Iterator<Integer> canEnableIter = canEnable.iterator(); canEnableIter.hasNext();) {
				Integer tranCanEnable = canEnableIter.next();
				if (curEnabled.contains(tranCanEnable)) {
					nEnable.add(tranCanEnable);
				}
				else {
					nEnable.addAll(getNecessarySet(curState, tranCanEnable, dependent, curEnabled, enable));
				}
			}
		}
		//printIntegerSet(nMarking, "nEnable = ");
		if (nEnable == null) 
			return nMarking;
		else if (nMarking == null) 
			return nEnable;
		else {
			if ((nMarking.size() - dependent.size()) < (nEnable.size() - dependent.size())) 
				return nMarking;
			else
				return nEnable;
		}
	}

	private void printIntegerArray(int[] intArray, String string) {
		System.out.println(string);
		if (intArray.length==0) {
			System.out.println("empty");
		}
		else {
			for (int i=0; i < intArray.length; i++) {
				System.out.print(intArray[i] + "\t");
			}
		}
		System.out.println("\n");
	}

	private void printIntegerSet(HashSet<Integer> integerSet, String setName) {
		System.out.print(setName + " is: ");
		if (integerSet.isEmpty()) {
			System.out.println("empty");
		}
		else {
			for (Iterator<Integer> curTranDisableIter = integerSet.iterator(); curTranDisableIter.hasNext();) {
				Integer tranInDisable = curTranDisableIter.next();
				System.out.print(this.lpn.getAllTransitions()[tranInDisable] + " ");
			}
			System.out.print("\n");
		}
				
	}
    
    private boolean isEnabled(Transition tran, State curState) {
	   	// TODO: (todo) Need to consider disabling transitions.
			int[] varValuesVector = curState.getVector();
			String tranName = tran.getName();
			int tranIndex = tran.getIndex();
			//System.out.println("Checking " + tran);
			if (this.lpn.getEnablingTree(tranName) != null 
					&& this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsAndValues(varValuesVector)) == 0.0
					&& !(tran.isPersistent() && curState.getTranVector()[tranIndex])) {
				//System.out.println(tran.getName() + " " + "Enabling condition is false");
				return false;
			}
			if (this.lpn.getTransitionRateTree(tranName) != null 
					&& this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsAndValues(varValuesVector)) == 0.0) {
				//System.out.println("Rate is zero");
				return false;
			}
			if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
				int[] curMarking = curState.getMarking();
				for (int place : this.lpn.getPresetIndex(tranName)) {
					if (curMarking[place]==0) {
						//System.out.println(tran.getName() + " " + "Missing a preset token");
						return false;
					}
				}
				// if a transition is enabled and it is not recorded in the enabled transition vector
				curState.getTranVector()[tranIndex] = true;
			}
		return true;
}

    
    // TODO: (done) Change isEnabled to work with our transitions.
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
    	int thisLpnIndex = this.getLpn().getIndex(); 
    	State[] nextStateArray = curStateArray.clone();
    	
    	State curState = curStateArray[thisLpnIndex];
    	State nextState = this.fire(curSgArray[thisLpnIndex], curState, firedTran);   
    	
    	int[] nextVector = nextState.getVector();
    	int[] curVector = curState.getVector();
    	
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
//    	// TODO: (original) currently this function is not implemented correctly
		if(firedTran.local()==true) {
//    		nextStateArray[thisLpnIndex] = curSgArray[thisLpnIndex].addState(nextState);
        	return nextStateArray;
		}

        HashMap<String, Integer> vvSet = new HashMap<String, Integer>();
        for (String key : this.lpn.getAllVarsAndValues(curVector).keySet()) {
        	if (this.lpn.getBoolAssignTree(firedTran.getName(), key) != null) {
        		int newValue = (int)this.lpn.getBoolAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
        		vvSet.put(key, newValue);
        	}
        	// TODO: (temp) type cast continuous variable to int.
        	if (this.lpn.getContAssignTree(firedTran.getName(), key) != null) {
        		int newValue = (int)this.lpn.getContAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
        		vvSet.put(key, newValue);
        	}
        	if (this.lpn.getIntAssignTree(firedTran.getName(), key) != null) {
        		int newValue = (int)this.lpn.getIntAssignTree(firedTran.getName(), key).evaluateExpr(this.lpn.getAllVarsAndValues(curVector));
        		vvSet.put(key, newValue);
        	}
        }       
        // TODO: (check) if the code above is the same as below when constructing vvSet.
        /*
        for (VarExpr s : this.getAssignments()) {
            int newValue = nextVector[s.getVar().getIndex(curVector)];
            vvSet.put(s.getVar().getName(), newValue);   
        }
        */
        
        // Update other local states with the new values generated for the shared variables.
		//nextStateArray[this.lpn.getIndex()] = nextState;
		// TODO: (temp) currently only add one LPN to dstLpnList
//		if (!firedTran.getDstLpnList().contains(this.lpn))
//			firedTran.getDstLpnList().add(this.lpn);
		for(LhpnFile curLPN : firedTran.getDstLpnList()) {
        	int curIdx = curLPN.getIndex();
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
        
        int[] newVector = newState.getVector();
        // TODO: (future) assertions in our LPN?
        /*
		for(Expression e : assertions){
        	if(e.evaluate(newVector) == 0){
        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
        		System.exit(1);
        	}
        }
		*/
		return newState;
    }
	
	public void outputStateGraph(String file) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write("digraph G {\n");
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
					out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + "\"]\n");
				}
			}
			out.write("}");
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error outputting state graph as dot file.");
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
}
