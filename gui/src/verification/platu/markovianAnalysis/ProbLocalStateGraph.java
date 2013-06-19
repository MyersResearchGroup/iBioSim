package verification.platu.markovianAnalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;

import lpn.parser.LhpnFile;
import lpn.parser.Place;
import lpn.parser.Transition;
import verification.platu.main.Options;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

public class ProbLocalStateGraph extends StateGraph {
    	
	//protected HashMap<State, HashMap<Transition, ProbLocalStateTuple>> nextProbLocalStateTupleMap;
	
    public ProbLocalStateGraph(LhpnFile lpn) {
    	super(lpn);
    	//this.nextProbLocalStateTupleMap = new HashMap<State, HashMap<Transition, ProbLocalStateTuple>>();
    }
    
    public void drawLocalStateGraph() {
    	// TODO: Need to fix this method call.
//		try {			
//			String graphFileName = null;
//			if (Options.getPOR() == null)
//				graphFileName = Options.getPrjSgPath() + getLpn().getLabel() + "_local_sg.dot";
//			else
//				graphFileName = Options.getPrjSgPath() + getLpn().getLabel() + "POR_"+ Options.getCycleClosingMthd() + "_local_sg.dot";
//			int size = this.lpn.getVarIndexMap().size();
//			String varNames = "";
//			for(int i = 0; i < size; i++) {
//				varNames = varNames + ", " + this.lpn.getVarIndexMap().getKey(i);
//	    	}
//			varNames = varNames.replaceFirst(", ", "");
//			BufferedWriter out = new BufferedWriter(new FileWriter(graphFileName));
//			out.write("digraph G {\n");
//			out.write("Inits [shape=plaintext, label=\"<" + varNames + ">\"]\n");
//			for (State curState : nextProbLocalStateTupleMap.keySet()) {
//				String markings = intArrayToString("markings", curState);
//				String vars = intArrayToString("vars", curState);
//				String enabledTrans = boolArrayToString("enabledTrans", curState);
//				String curStateName = "S" + curState.getIndex();
//				out.write(curStateName + "[shape=\"ellipse\",label=\"" + curStateName + "\\n<"+vars+">" + "\\n<"+enabledTrans+">" + "\\n<"+markings+">" + "\"]\n");
//			}
//			
//			for (State curState : nextProbLocalStateTupleMap.keySet()) {
//				HashMap<Transition, ProbLocalStateTuple> nextStateTupleMap = nextProbLocalStateTupleMap.get(curState);
//				for (Transition curTran : nextStateTupleMap.keySet()) {
//					String curStateName = "S" + curState.getIndex();
//					printPartialNextProbLocalStateTupleMap(curState, "drawLocalStateGraph");
//					printNextProbLocalStateTupleMap("drawLocalStateGraph");
//					String nextStateName = "S" + nextStateTupleMap.get(curTran).getNextProbLocalState().getIndex();					
//					String curTranName = curTran.getLabel();
//					String curTranRate = nextProbLocalStateTupleMap.get(curState).get(curTran).toString().substring(0, 4);
//					if (curTran.isFail() && !curTran.isPersistent()) 
//						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + ", " + curTranRate + "\", fontcolor=red]\n");
//					else if (!curTran.isFail() && curTran.isPersistent())						
//						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + ", " + curTranRate + "\", fontcolor=blue]\n");
//					else if (curTran.isFail() && curTran.isPersistent())						
//						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + ", " + curTranRate + "\", fontcolor=purple]\n");
//					else 
//						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + ", " + curTranRate + "\"]\n");
//				}
//			}
//			out.write("}");
//			out.close();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			System.err.println("Error producing local ProbabilisticState graph as dot file.");
//		}
	}

	/**
	 * This method updates the tranVector for the next state and creates the corresponding tranRateVector. If a transition is not enabled,
	 * its corresponding rate is marked as 0. Note that this method ONLY applies to transitions with non-empty delay expressions.  
	 * @param enabledTransAfterFiring
	 * @param newMarking
	 * @param newVectorArray
	 * @param firedTran
	 * @return
	 */
	public double[] updateTranAndRateVectors(boolean[] enabledTransAfterFiring, int[] newMarking, int[] newVectorArray, Transition firedTran) {
		double[] enabledTransRatesAfterFiring = new double[enabledTransAfterFiring.length];
		// Disable the fired transition and all of its conflicting transitions. 
		if (firedTran != null) {
			enabledTransAfterFiring[firedTran.getIndex()] = false;
			enabledTransRatesAfterFiring[firedTran.getIndex()] = 0.0;
			for (Integer curConflictingTranIndex : firedTran.getConflictSetTransIndices()) {
				enabledTransAfterFiring[curConflictingTranIndex] = false;
				enabledTransRatesAfterFiring[firedTran.getIndex()] = 0.0;
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
					&& this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(newVectorArray)) == 0.0) {
				if (Options.getDebugMode()) {
					//    				System.out.println(tran.getName() + " " + "Enabling condition is false");    			
				}					
				if (enabledTransAfterFiring[tranIndex] && !tran.isPersistent()) {
					enabledTransAfterFiring[tranIndex] = false;
					enabledTransRatesAfterFiring[tranIndex] = 0.0;
				}		
				continue;
			}
			if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
				for (int place : this.lpn.getPresetIndex(tranName)) {
					if (newMarking[place]==0) {
						if (Options.getDebugMode()) {
							//    						System.out.println(tran.getName() + " " + "Missing a preset token");
						}
						if (enabledTransAfterFiring[tranIndex]) {
							enabledTransAfterFiring[tranIndex] = false;
							enabledTransRatesAfterFiring[tranIndex] = 0.0;
						}							
						continue tran_iteration;
					}
				}
			}			
			if (this.lpn.getTransitionRateTree(tranName) != null) {		
				double tranRate = this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(newVectorArray));										
				if (tranRate == 0.0) {
					if (Options.getDebugMode()) {
						//						System.out.println("Rate is zero");
					}
					if (enabledTransAfterFiring[tranIndex]) {
						enabledTransAfterFiring[tranIndex] = false;
						enabledTransRatesAfterFiring[tranIndex] = 0.0;
					}						
					continue;
				}
				// if a transition passes all tests above, it needs to be marked as enabled.
				enabledTransAfterFiring[tranIndex] = true;
				enabledTransRatesAfterFiring[tranIndex] = tranRate;
				if (Options.getDebugMode()) {
					//    				System.out.println(tran.getName() + " is Enabled.");
				}	
			}
			else {
				System.out.println("Transition " + tran.getFullLabel() + "has empty delay expression.");
				new NullPointerException().printStackTrace();
				System.exit(1);
			}				
		}
		return enabledTransRatesAfterFiring;
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
    
	public State genInitialState() {	
		// create initial vector
		int size = this.lpn.getVarIndexMap().size();
		int[] initVariableVector = new int[size];
		for(int i = 0; i < size; i++) {
			String var = this.lpn.getVarIndexMap().getKey(i);
			int val = this.lpn.getInitVariableVector(var);
			initVariableVector[i] = val;
		}		
		double[] initTranRateVector = new double[this.lpn.getAllTransitions().length];
		boolean[] initTranVector = genInitTranVector(initVariableVector, initTranRateVector);
		this.init = new ProbLocalState(this.lpn, this.lpn.getInitialMarkingsArray(), initVariableVector, initTranVector, initTranRateVector);
		return this.init;
	}
    
    /* (non-Javadoc)
     * @see verification.platu.stategraph.StateGraph#fire(verification.platu.stategraph.StateGraph, verification.platu.stategraph.State, lpn.parser.Transition)
     * Fire a transition, generate the local next state (if it is not created yet) from the current local state. Also, update the enabled transition vector
     * and its corresponding tranRate vector.
     */
    public State fire(final StateGraph thisSg, final State curState, Transition firedTran){
    	State nextState = thisSg.getNextState(curState, firedTran);
    	if(nextState != null) {
    		if (Options.getDebugMode()) {
    			System.out.println("@ ProbLocalStateGraph.java -> fire(StateGraph, State, Transition), nextState is not null. nextState = S" 
    					+ nextState.getIndex() + "("+ nextState.getLpn().getLabel() + ")");
    			//printPartialNextProbLocalStateTupleMap(curState, "ProbLocalStateGraph.java -> fire(StateGraph, State, Transition)");
    			
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
    	boolean[] newTranVector = curState.getTranVector().clone();    	
    	double[] newTranRateVector = updateTranAndRateVectors(newTranVector, curNewMarking, newVariableVector, firedTran); 
    	State newState = thisSg.addState(new ProbLocalState(this.lpn, curNewMarking, newVariableVector, newTranVector, newTranRateVector));
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
//    	double firedTranRate = ((ProbLocalState) curState).getTranRate(firedTran.getIndex());
//    	((ProbLocalStateGraph)thisSg).addStateTran(curState, firedTran, firedTranRate, newState);
    	thisSg.addStateTran(curState, firedTran, newState);
    	return newState;
    }
    

    /* (non-Javadoc)
     * @see verification.platu.stategraph.StateGraph#genInitTranVector(int[])
     * Find enabled transitions in the initial state, and construct the tranVector in this state.
     */
    public boolean[] genInitTranVector(int[] initVariableVector, double[] initTranRateVector) {
    	boolean[] initTranVector = new boolean[this.lpn.getAllTransitions().length];    	
    	tran_outter_loop: for (int i=0; i< this.lpn.getAllTransitions().length; i++) {
    		Transition transition = this.lpn.getAllTransitions()[i];
    		Place[] tranPreset = this.lpn.getTransition(transition.getLabel()).getPreset(); 
    		String tranName = transition.getLabel();
    		if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
    			for (int j=0; j<tranPreset.length; j++) {
    				if (!tranPreset[j].isMarked()) {
    					initTranVector[i] = false;	
    					initTranRateVector[i] = 0.0;
    					continue tran_outter_loop;
    				}
    			}
    		}
    		if (this.lpn.getEnablingTree(tranName) != null && this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(initVariableVector)) == 0.0) {
    			initTranVector[i] = false;
    			initTranRateVector[i] = 0.0;
    			continue;
    		}
    		if (this.lpn.getTransitionRateTree(tranName) != null) {
    			double tranRate = this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(initVariableVector));		
    			if (tranRate == 0.0) {
    				if (Options.getDebugMode()) {
    					//						System.out.println("Rate is zero");
    				}
    				initTranVector[i] = false;
    				initTranRateVector[i] = 0.0;
    				continue;
    			}
    			initTranVector[i] = true;
    			initTranRateVector[i] = tranRate;
    			//nextStateSubMap.put(transition, new ProbLocalStateTuple(null, tranRate));
    		}
    	}
    	return initTranVector;
    }
    

    /* (non-Javadoc)
     * @see verification.platu.stategraph.StateGraph#fire(verification.platu.stategraph.StateGraph[], verification.platu.stategraph.State[], lpn.parser.Transition)
     * This method executes firedTran first by calling fire(StateGraph, State, Transition), and then it updates the affected state graphs.
     */
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
    	for(LhpnFile curLPN : firedTran.getDstLpnList()) {
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
    				// Find the fired transition rate from the local state (in another state graph) where firedTran originates.
    				//int firedTranLPNindex = firedTran.getLpn().getLpnIndex();
    				//State firedTranCurState = curStateArray[firedTranLPNindex];
    				//double firedTranRate = ((ProbLocalState) firedTranCurState).getTranRate(firedTran.getIndex());
    				//((ProbLocalStateGraph)curSgArray[curIdx]).addStateTran(curStateArray[curIdx], firedTran, firedTranRate, cachedOther);
    				curSgArray[curIdx].addStateTran(curStateArray[curIdx], firedTran, cachedOther);
    			}   		
    		}
    	}	
    	return nextStateArray;
    }

//    public void printNextProbLocalStateTupleMap(String location) {
//    	String newLine = System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
//    	System.out.println("----------------Next State Map @ " + location + "----------------");
//    	for (State st : nextProbLocalStateTupleMap.keySet()) {
//    		HashMap<Transition, ProbLocalStateTuple> nextStMap = nextProbLocalStateTupleMap.get(st);
//    		System.out.println("S" + st.getIndex() + "("+ st.getLpn().getLabel() + "):");
//    		String message = "";
//    		for (Transition t : nextStMap.keySet()) {
//    			message += "\t" + t.getLabel() + "(" + t.getLpn().getLabel() +") ==> "; 
//    			ProbLocalStateTuple nextStTuple = nextStMap.get(t);
//    			if (nextStTuple.getNextProbLocalState() == null)
//    				message += "null, rate=" + nextStTuple.getTranRate() + newLine;
//    			else
//    				message += "S" + nextStTuple.getNextProbLocalState().getIndex() + "(" 
//    						+ nextStTuple.getNextProbLocalState().getLpn().getLabel() +"), rate="
//    						+ nextStTuple.getTranRate() + newLine;
//    		}            		
//    		System.out.print(message);            		
//    	}
//    	System.out.println("--------------End Of Next State Map----------------------");
//    }
//    
//    public void printPartialNextProbLocalStateTupleMap(State givenState, String location) {    	
//    	System.out.println("----------------Next State Map @ " + location + "----------------");
//    	System.out.println("state = S" + givenState.getIndex() + "(" + givenState.getLpn().getLabel() + ")");
//    	HashMap<Transition, ProbLocalStateTuple> nextStateMapForGivenState = nextProbLocalStateTupleMap.get(givenState);
//    	if (nextStateMapForGivenState == null)
//    		System.out.println("null");
//    	else {
//    		for (Transition t: nextStateMapForGivenState.keySet()) {
//    			ProbLocalStateTuple nextStateTuple = nextStateMapForGivenState.get(t);
//    			State nextState = nextStateTuple.getNextProbLocalState();
//    			double tranRate = nextStateTuple.getTranRate();
//    			if (nextState == null)
//    				System.out.println(t.getFullLabel() + " -> null, rate = " + tranRate);
//    			else
//    				System.out.println(t.getFullLabel() + " -> S" + nextState.getIndex() + "(" + nextState.getLpn().getLabel() + ")" + tranRate);
//    		}
//    	}
//    	System.out.println("--------------End Of Next State Map----------------------");
//    }

}
