package verification.platu.stategraph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Set;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;
import verification.platu.lpn.LpnTranList;
import verification.platu.main.Options;



public class ProbabilisticStateGraph extends StateGraph {
    
	protected HashMap<State, HashMap<TranRatePair, State>> nextProbStateMap;
	
    public ProbabilisticStateGraph(LhpnFile lpn) {
    	super(lpn);
    	this.nextProbStateMap = new HashMap<State, HashMap<TranRatePair, State>>();
    }

    public Set<TranRatePair> getTranRateList(ProbabilisticState currentState){
    	return this.nextProbStateMap.get(currentState).keySet();
    }
    
    /**
     * Return the enabled transitions in the ProbabilisticState with index 'ProbabilisticStateIdx'.
     * @param ProbabilisticStateIdx
     * @return
     */
    public LpnTranList getEnabled(int ProbabilisticStateIdx) {
    	ProbabilisticState curProbabilisticState = (ProbabilisticState) this.getState(ProbabilisticStateIdx);
        return this.getEnabled(curProbabilisticState);
       
    }
    /**
     * Return the set of all LPN transitions that are enabled in 'ProbabilisticState'.
     * @param curProbabilisticState
     * @return
     */
    public LpnTranList getEnabled(ProbabilisticState curProbabilisticState) {
    	if (curProbabilisticState == null) {
            throw new NullPointerException();
        }
    	
    	if(enabledSetTbl.containsKey(curProbabilisticState) == true){
            return (LpnTranList)enabledSetTbl.get(curProbabilisticState).clone();
        }
    	
        LpnTranList curEnabled = new LpnTranList();
        for (Transition tran : this.lpn.getAllTransitions()) {
        	if (isEnabled(tran,curProbabilisticState)) {
        		if(tran.isLocal()==true)
        			curEnabled.addLast(tran);
                else
                	curEnabled.addFirst(tran);
             } 
        }
        this.enabledSetTbl.put(curProbabilisticState, curEnabled);
        return curEnabled;
    }
    
    /**
     * Return the set of all LPN transitions that are enabled in 'ProbabilisticState'.
     * @param curProbabilisticState
     * @return
     */
    public LpnTranList getEnabled(ProbabilisticState curProbabilisticState, boolean init) {
    	if (curProbabilisticState == null) {
            throw new NullPointerException();
        }   	
    	if(enabledSetTbl.containsKey(curProbabilisticState) == true){
    		if (Options.getDebugMode()) {
//    			System.out.println("~~~~~~~ existing ProbabilisticState in enabledSetTbl for LPN" + curProbabilisticState.getLpn().getLabel() + ": S" + curProbabilisticState.getIndex() + "~~~~~~~~");
//        		printTransitionSet((LpnTranList)enabledSetTbl.get(curProbabilisticState), "enabled trans at this ProbabilisticState ");
    		}
            return (LpnTranList)enabledSetTbl.get(curProbabilisticState).clone();
        }   	
        LpnTranList curEnabled = new LpnTranList();
        //System.out.println("----Enabled transitions----");
        if (init) {
        	for (Transition tran : this.lpn.getAllTransitions()) {
            	if (isEnabled(tran,curProbabilisticState)) {
            		if (Options.getDebugMode()) {
//            			System.out.println("Transition " + tran.getLpn().getLabel() + "(" + tran.getName() + ") is enabled");
            		}            			
            		if(tran.isLocal()==true)
            			curEnabled.addLast(tran);
                    else
                    	curEnabled.addFirst(tran);
                 } 
            }
        }
        else {
        	for (int i=0; i < this.lpn.getAllTransitions().length; i++) {
        		Transition tran = this.lpn.getAllTransitions()[i];
        		if (curProbabilisticState.getTranVector()[i])
        			if(tran.isLocal()==true)
            			curEnabled.addLast(tran);
                    else
                    	curEnabled.addFirst(tran);
        			
        	}
        }
        this.enabledSetTbl.put(curProbabilisticState, curEnabled);
        if (Options.getDebugMode()) {
//        	System.out.println("~~~~~~~~ ProbabilisticState S" + curProbabilisticState.getIndex() + " does not exist in enabledSetTbl for LPN " + curProbabilisticState.getLpn().getLabel() + ". Add to enabledSetTbl.");
//        	printEnabledSetTbl();
        }
        return curEnabled;
    }
    
	public void addProbabilisticStateTran(ProbabilisticState curSt, Transition firedTran, ProbabilisticState nextSt) {
    	HashMap<TranRatePair, State> nextMap = this.nextProbStateMap.get(curSt);
    	if(nextMap == null)  {
    		nextMap = new HashMap<TranRatePair, State>();
    		nextMap.put(new TranRatePair(firedTran,-1), nextSt);
    		this.nextProbStateMap.put(curSt, nextMap);
    	}
    	else
    		nextMap.put(new TranRatePair(firedTran,-1), nextSt);
    }
    
    public State getNextProbabilisticState(ProbabilisticState curSt, TranRatePair firedTranRatePair) {
    	HashMap<TranRatePair, State> nextMap = this.nextProbStateMap.get(curSt);
    	if(nextMap == null)
    		return null;   	
    	return nextMap.get(firedTranRatePair);
    }
    
// TODO: Need special treatment on initial probabilistic state?    
//    public ProbabilisticState getInitProbabilisticState() {	
//    	// create initial vector
//		int size = this.lpn.getVarIndexMap().size();
//    	int[] initialVector = new int[size];
//    	for(int i = 0; i < size; i++) {
//    		String var = this.lpn.getVarIndexMap().getKey(i);
//    		int val = this.lpn.getInitVector(var);
//    		initialVector[i] = val;
//    	}
//		return new ProbabilisticState(this.lpn, this.lpn.getInitialMarkingsArray(), initialVector, this.lpn.getInitEnabledTranArray(initialVector));
//    }

//    // This method is called by search_dfs(ProbabilisticStateGraph[], ProbabilisticState[]).
//    public ProbabilisticState[] fire(final ProbabilisticStateGraph[] curSgArray, final ProbabilisticState[] curProbabilisticStateArray, Transition firedTran){
//    	int thisLpnIndex = this.getLpn().getLpnIndex(); 
//    	ProbabilisticState[] nextProbabilisticStateArray = curProbabilisticStateArray.clone();
//    	ProbabilisticState curProbabilisticState = curProbabilisticStateArray[thisLpnIndex];
//    	ProbabilisticState nextProbabilisticState = this.fire(curSgArray[thisLpnIndex], curProbabilisticState, firedTran);
//    	// TODO: (future) assertions in our LPN?
//    	/*
//        for(Expression e : assertions){
//        	if(e.evaluate(nextVector) == 0){
//        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
//        		System.exit(1);
//			}
//		}
//		*/
//		nextProbabilisticStateArray[thisLpnIndex] = nextProbabilisticState;
//		if(firedTran.isLocal()==true) {
////    		nextProbabilisticStateArray[thisLpnIndex] = curSgArray[thisLpnIndex].addProbabilisticState(nextProbabilisticState);
//        	return nextProbabilisticStateArray;
//		}		
//        HashMap<String, Integer> vvSet = new HashMap<String, Integer>();
//        vvSet = this.lpn.getAllVarsWithValuesAsInt(nextProbabilisticState.getVector());     
//        // Update other local ProbabilisticStates with the new values generated for the shared variables.
//		for(LhpnFile curLPN : firedTran.getDstLpnList()) {
//        	int curIdx = curLPN.getLpnIndex();
//    		ProbabilisticState newProbabilisticState = curSgArray[curIdx].getNextProbabilisticState(curProbabilisticStateArray[curIdx], firedTran);
//    		if(newProbabilisticState != null) {
//    			nextProbabilisticStateArray[curIdx] = newProbabilisticState;
//    		}     		
//        	else {
//        		ProbabilisticState newOther = curProbabilisticStateArray[curIdx].update(curSgArray[curIdx], vvSet, curSgArray[curIdx].getLpn().getVarIndexMap());
//        		if (newOther == null)
//        			nextProbabilisticStateArray[curIdx] = curProbabilisticStateArray[curIdx];
//        		else {
//        			ProbabilisticState cachedOther = curSgArray[curIdx].addProbabilisticState(newOther);
//            		nextProbabilisticStateArray[curIdx] = cachedOther;
//            		curSgArray[curIdx].addProbabilisticStateTran(curProbabilisticStateArray[curIdx], firedTran, cachedOther);
//        		}   		
//        	}
//        }
//        return nextProbabilisticStateArray;
//    }
//        
//    public ProbabilisticState fire(final ProbabilisticStateGraph thisSg, final ProbabilisticState curProbabilisticState, Transition firedTran){     	
//    	ProbabilisticState nextProbabilisticState = thisSg.getNextProbabilisticState(curProbabilisticState, firedTran);
//    	if(nextProbabilisticState != null)
//    		return nextProbabilisticState;
//    	
//    	// If no cached next ProbabilisticState exists, do regular firing. 
//    	// Marking update
//        int[] curOldMarking = curProbabilisticState.getMarking();
//        int[] curNewMarking = null;
//        if(firedTran.getPreset().length==0 && firedTran.getPostset().length==0)
//        	curNewMarking = curOldMarking;
//		else {
//			curNewMarking = new int[curOldMarking.length];	
//			curNewMarking = curOldMarking.clone();
//			for (int prep : this.lpn.getPresetIndex(firedTran.getName())) {
//				curNewMarking[prep]=0;
//			}
//			for (int postp : this.lpn.getPostsetIndex(firedTran.getName())) {
//				curNewMarking[postp]=1;
//			}
//        }
//
//        //  ProbabilisticState vector update
//        int[] newVectorArray = curProbabilisticState.getVector().clone();
//        int[] curVector = curProbabilisticState.getVector();
//        HashMap<String, String> currentValuesAsString = this.lpn.getAllVarsWithValuesAsString(curVector);
//        for (String key : currentValuesAsString.keySet()) {
//        	if (this.lpn.getBoolAssignTree(firedTran.getName(), key) != null) {
//        		int newValue = (int)this.lpn.getBoolAssignTree(firedTran.getName(), key).evaluateExpr(currentValuesAsString);
//        		newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
//        	}
//        	
//        	if (this.lpn.getIntAssignTree(firedTran.getName(), key) != null) {
//        		int newValue = (int)this.lpn.getIntAssignTree(firedTran.getName(), key).evaluateExpr(currentValuesAsString);
//        		newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
//        	}
//        } 
//
//        // Enabled transition vector update
//        boolean[] newEnabledTranVector = updateEnabledTranVector(curProbabilisticState.getTranVector(), curNewMarking, newVectorArray, firedTran);
//        ProbabilisticState newProbabilisticState = thisSg.addProbabilisticState(new ProbabilisticState(this.lpn, curNewMarking, newVectorArray, newEnabledTranVector));
//        // TODO: (future) assertions in our LPN?
//        /*
//        int[] newVector = newProbabilisticState.getVector();
//		for(Expression e : assertions){
//        	if(e.evaluate(newVector) == 0){
//        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
//        		System.exit(1);
//        	}
//        }
//		*/
//		thisSg.addProbabilisticStateTran(curProbabilisticState, firedTran, newProbabilisticState);
//		return newProbabilisticState;
//    }
    
    public void outputLocalProbabilisticStateGraph(String file) {
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
			for (State curProbabilisticState : nextProbStateMap.keySet()) {
				String markings = intArrayToString("markings", curProbabilisticState);
				String vars = intArrayToString("vars", curProbabilisticState);
				String enabledTrans = boolArrayToString("enabledTrans", curProbabilisticState);
				String curProbabilisticStateName = "S" + curProbabilisticState.getIndex();
				out.write(curProbabilisticStateName + "[shape=\"ellipse\",label=\"" + curProbabilisticStateName + "\\n<"+vars+">" + "\\n<"+enabledTrans+">" + "\\n<"+markings+">" + "\"]\n");
			}
			
			for (State curProbabilisticState : nextProbStateMap.keySet()) {
				HashMap<TranRatePair, State> ProbStateTransitionPair = nextProbStateMap.get(curProbabilisticState);
				for (TranRatePair curTranRatePair : ProbStateTransitionPair.keySet()) {
					String curProbabilisticStateName = "S" + curProbabilisticState.getIndex();
					String nextProbabilisticStateName = "S" + ProbStateTransitionPair.get(curTranRatePair).getIndex();
					Transition curTran = curTranRatePair.getTran();
					String curTranName = curTran.getName();
					if (curTran.isFail() && !curTran.isPersistent()) 
						out.write(curProbabilisticStateName + " -> " + nextProbabilisticStateName + " [label=\"" + curTranName + "\", fontcolor=red]\n");
					else if (!curTran.isFail() && curTran.isPersistent())						
						out.write(curProbabilisticStateName + " -> " + nextProbabilisticStateName + " [label=\"" + curTranName + "\", fontcolor=blue]\n");
					else if (curTran.isFail() && curTran.isPersistent())						
						out.write(curProbabilisticStateName + " -> " + nextProbabilisticStateName + " [label=\"" + curTranName + "\", fontcolor=purple]\n");
					else 
						out.write(curProbabilisticStateName + " -> " + nextProbabilisticStateName + " [label=\"" + curTranName + "\"]\n");
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

	public HashMap<State, HashMap<TranRatePair, State>> getNextProbabilisticStateMap() {
		return this.nextProbStateMap;
	}
	
}
