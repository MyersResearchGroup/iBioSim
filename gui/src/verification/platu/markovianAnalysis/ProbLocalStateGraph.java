package verification.platu.markovianAnalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;

import lpn.parser.LhpnFile;
import lpn.parser.Place;
import lpn.parser.Transition;
import verification.platu.lpn.LpnTranList;
import verification.platu.main.Options;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

public class ProbLocalStateGraph extends StateGraph {
    	
	protected HashMap<State, HashMap<Transition, ProbLocalStateTuple>> nextProbLocalStateTupleMap;
	
    public ProbLocalStateGraph(LhpnFile lpn) {
    	super(lpn);
    	this.nextProbLocalStateTupleMap = new HashMap<State, HashMap<Transition, ProbLocalStateTuple>>();
    }
    
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
			for (State curState : nextProbLocalStateTupleMap.keySet()) {
				String markings = intArrayToString("markings", curState);
				String vars = intArrayToString("vars", curState);
				String enabledTrans = boolArrayToString("enabledTrans", curState);
				String curStateName = "S" + curState.getIndex();
				out.write(curStateName + "[shape=\"ellipse\",label=\"" + curStateName + "\\n<"+vars+">" + "\\n<"+enabledTrans+">" + "\\n<"+markings+">" + "\"]\n");
			}
			
			for (State curState : nextProbLocalStateTupleMap.keySet()) {
				HashMap<Transition, ProbLocalStateTuple> nextStMap = nextProbLocalStateTupleMap.get(curState);
				for (Transition curTran : nextStMap.keySet()) {
					String curStateName = "S" + curState.getIndex();
					String nextStateName = "S" + nextStMap.get(curTran).getNextProbLocalState().getIndex();					
					String curTranName = curTran.getLabel();
					String curTranRate = nextProbLocalStateTupleMap.get(curState).get(curTran).toString().substring(0, 4);
					if (curTran.isFail() && !curTran.isPersistent()) 
						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + ", " + curTranRate + "\", fontcolor=red]\n");
					else if (!curTran.isFail() && curTran.isPersistent())						
						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + ", " + curTranRate + "\", fontcolor=blue]\n");
					else if (curTran.isFail() && curTran.isPersistent())						
						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + ", " + curTranRate + "\", fontcolor=purple]\n");
					else 
						out.write(curStateName + " -> " + nextStateName + " [label=\"" + curTranName + ", " + curTranRate + "\"]\n");
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
            		printTransitionSet(enabledSetTbl.get(curState), "Enabled trans at this state: ");
        		}
                return (LpnTranList)enabledSetTbl.get(curState).clone();
            }
    	LpnTranList curEnabled = new LpnTranList();
        if (init) {
        	for (Transition tran : this.lpn.getAllTransitions()) {
            	if (isEnabled(tran,curState)) {
            		if (Options.getDebugMode()) {
            			System.out.println("Transition " + tran.getLpn().getLabel() + "(" + tran.getLabel() + ") is enabled");
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
        		if (curState.getTranVector()[i])
        			if(tran.isLocal()==true)
            			curEnabled.addLast(tran);
                    else
                    	curEnabled.addFirst(tran);
        			
        	}
        }
        this.enabledSetTbl.put(curState, curEnabled);
        if (Options.getDebugMode()) {
        	System.out.println("~~~~~~~~ State S" + curState.getIndex() + " does not exist in enabledSetTbl for LPN " + curState.getLpn().getLabel() + ". Add to enabledSetTbl.");    
        	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        	String newLine = System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
    		System.out.println("----------------Next State Map @ StateGraph.java => getEnabled()----------------");
    		for (State st : ((ProbLocalStateGraph) this).getNextProbLocalStateMap().keySet()) {
    			HashMap<Transition, ProbLocalStateTuple> nextStMap = ((ProbLocalStateGraph) this).getNextProbLocalStateMap().get(st);
    			System.out.println("S" + st.getIndex() + "("+ st.getLpn().getLabel() + "):");
    			String message = "";
    			for (Transition t : nextStMap.keySet()) {
    				message += "\t" + t.getLabel() + "(" + t.getLpn().getLabel() +") ==> "; 
    				ProbLocalStateTuple nextStTuple = nextStMap.get(t);
    				if (nextStTuple.getNextProbLocalState() == null)
    					message += "null, rate=" + nextStTuple.getTranRate() + newLine;
    				else
    					message += "S" + nextStTuple.getNextProbLocalState().getIndex() + "(" 
    								+ nextStTuple.getNextProbLocalState().getLpn().getLabel() +"), rate="
    								+ nextStTuple.getTranRate() + newLine;
    			}            		
    			System.out.print(message);            		
    		}
    		System.out.println("--------------End Of Next State Map----------------------");
        }
        return curEnabled;
    }
	
	public boolean isEnabled(Transition tran, State curState) {	   	
		int[] varValuesVector = curState.getVector();
		String tranName = tran.getLabel();
		int tranIndex = tran.getIndex();
		if (Options.getDebugMode()) {
			//					System.out.println("Checking " + tran);
		}				
		if (this.lpn.getEnablingTree(tranName) != null 
				&& this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(varValuesVector)) == 0.0
				&& !(tran.isPersistent() && curState.getTranVector()[tranIndex])) {
			if (Options.getDebugMode()) {
				//						System.out.println(tran.getName() + " " + "Enabling condition is false");
			}	
			return false;
		}
		if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
			int[] curMarking = curState.getMarking();
			for (int place : this.lpn.getPresetIndex(tranName)) {
				if (curMarking[place]==0) {
					if (Options.getDebugMode()) {
						//								System.out.println(tran.getName() + " " + "Missing a preset token");
					}							
					return false;
				}
			}					
		}
		if (this.lpn.getTransitionRateTree(tranName) != null) {
				//&& this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(varValuesVector)) == 0.0) {
			double tranRate = this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(varValuesVector));		
			if (tranRate == 0.0) {
				if (Options.getDebugMode()) {
					//						System.out.println("Rate is zero");
				}					
				return false;
			}	
			curState.getTranVector()[tranIndex] = true;
			HashMap<Transition, ProbLocalStateTuple> nextStateMap = nextProbLocalStateTupleMap.get(curState);
			if (nextStateMap == null) {
				nextStateMap = new HashMap<Transition, ProbLocalStateTuple>();				
				nextStateMap.put(tran, new ProbLocalStateTuple(null, tranRate));
				nextProbLocalStateTupleMap.put(curState, nextStateMap);
			}
			else {
				// more than one transition coming out of current state.
				nextStateMap.put(tran, new ProbLocalStateTuple(null, tranRate));
			}	
		}
		// Moved up. Since curState is put in nextProbLocalStateTupleMap as a key, it should be modified before it is added to the map. 
		//curState.getTranVector()[tranIndex] = true;
		return true;
	}
	
//	public void addStateTran(State curSt, Transition firedTran, State nextSt) {
//    	HashMap<TranRatePair, State> nextMap = this.nextProbStateMap.get(curSt);
//    	//TranRatePair tranRatePairTmp = null;
//    	for (TranRatePair tranRatePair : nextMap.keySet()) {
//    		if (tranRatePair.getTran().equals(firedTran)) {
//    			//System.out.println("Found transition " + tranRatePair.getTran().getName());
//    			//tranRatePairTmp = new TranRatePair(tranRatePair.getTran(), tranRatePair.getRate());
//    			//break;
//    			nextMap.put(tranRatePair, nextSt);
//    			//System.out.println("Entry in nextMap: S" + nextProbStateMap.get(curSt).get(tranRatePair).getIndex());
//    			break;//return;
//    		}
//    	}
//    	//nextMap.put(tranRatePairTmp, nextSt);
//    	this.nextProbStateMap.put(curSt, nextMap);
//    	
//    }
	
	/**
	 * This method updates the tranVector for the next state and partially builds the nextStateMap. 
	 * It fills the map with enabled transitions in the next state and their rates. 
	 * @param newEnabledTranVector
	 * @param newMarking
	 * @param newVectorArray
	 * @param firedTran
	 * @param nextStateSubMap
	 * @return
	 */
	public boolean[] updateTranVectorBuildNextStateSubMap(boolean[] enabledTransAfterFiring, int[] newMarking, int[] newVectorArray, 
			Transition firedTran, HashMap<Transition, ProbLocalStateTuple> nextStateSubMap) {    	
		// Disable the fired transition and all of its conflicting transitions. 
		if (firedTran != null) {
			enabledTransAfterFiring[firedTran.getIndex()] = false;
			for (Integer curConflictingTranIndex : firedTran.getConflictSetTransIndices()) {
				enabledTransAfterFiring[curConflictingTranIndex] = false;
			}
		}
		// find newly enabled transition(s) based on the updated markings and variables
		tran_iteration : for (Transition tran : this.lpn.getAllTransitions()) {
			//boolean needToUpdate = true;
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
				if (enabledTransAfterFiring[tranIndex] && !tran.isPersistent())
					enabledTransAfterFiring[tranIndex] = false;
				continue;
			}
			if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
				for (int place : this.lpn.getPresetIndex(tranName)) {
					if (newMarking[place]==0) {
						if (Options.getDebugMode()) {
							//    						System.out.println(tran.getName() + " " + "Missing a preset token");
						}    					
						continue tran_iteration;
					}
				}
			}
			if (this.lpn.getTransitionRateTree(tranName) != null) {
				//&& this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(varValuesVector)) == 0.0) {
				double tranRate = this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(newVectorArray));		
				if (tranRate == 0.0) {
					if (Options.getDebugMode()) {
						//						System.out.println("Rate is zero");
					}					
					continue;
				}
				nextStateSubMap.put(tran, new ProbLocalStateTuple(null, tranRate));
			}
			// if a transition passes all tests above, it needs to be marked as enabled.
			enabledTransAfterFiring[tranIndex] = true;
			if (Options.getDebugMode()) {
				//    				System.out.println(tran.getName() + " is Enabled.");
			}					
		}
		return enabledTransAfterFiring;
	}
    
    public void addStateTran(State curSt, Transition firedTran, double firedTranRate, State nextSt) {
    	HashMap<Transition, ProbLocalStateTuple> nextMap = this.nextProbLocalStateTupleMap.get(curSt);//this.nextStateMap.get(curSt); 	
    	if(nextMap == null)  {    		
    		System.out.println("nextMap == null @ S" + curSt.getIndex() + " firedTran=" +firedTran.getLabel());
    		new NullPointerException().printStackTrace();
       	}
    	else if (firedTran.getLpn().equals(curSt.getLpn()) && nextMap.get(firedTran) == null) {
    		System.out.println("nextMap.get(firedTran) == null @ S" + curSt.getIndex() + "(" + curSt.getLpn().getLabel() + ")" + " firedTran=" +firedTran.getFullLabel());
    		new NullPointerException().printStackTrace();
    	}
    	else {
    		if (!firedTran.getLpn().equals(curSt.getLpn())) { // firedTran is from another LPN, but it affects this state graph.
    			// nextMap.get(firedTran) in this case is null.    			
    			nextMap.put(firedTran, new ProbLocalStateTuple(nextSt, firedTranRate));
    			if (Options.getDebugMode()) {
    				System.out.println("**** firedTran is from another LPN. Added <" + firedTran.getLabel() + ", S" + nextSt.getIndex() + "(" + curSt.getLpn().getLabel() 
    						+")> to the next state map of state S" + curSt.getIndex() + "(" + curSt.getLpn().getLabel() +")");    				
    			}
    		}
    		if (nextMap.get(firedTran).getNextProbLocalState() == null) {
    			nextMap.get(firedTran).addProbLocalState(nextSt);
    			if (Options.getDebugMode()) {
    				System.out.println("**** Added <" + firedTran.getLabel() + ", S" + nextSt.getIndex() + "(" + curSt.getLpn().getLabel() 
    						+")> to the next state map of state S" + curSt.getIndex() + "(" + curSt.getLpn().getLabel() +")");    				
    			}
    		}		
    	}
        if (Options.getDebugMode()) {        	
        	String newLine = System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
    		System.out.println("----------------Next State Map @ ProbLocalStateGraph.java => addStateTran()----------------");
    		for (State st : this.getNextProbLocalStateMap().keySet()) {
    			HashMap<Transition, ProbLocalStateTuple> nextStMap = this.getNextProbLocalStateMap().get(st);
    			System.out.println("S" + st.getIndex() + "("+ st.getLpn().getLabel() + "):");
    			String message = "";
    			for (Transition t : nextStMap.keySet()) {
    				message += "\t" + t.getLabel() + "(" + t.getLpn().getLabel() +") ==> "; 
    				ProbLocalStateTuple nextStTuple = nextStMap.get(t);
    				if (nextStTuple.getNextProbLocalState() == null)
    					message += "null, rate=" + nextStTuple.getTranRate() + newLine;
    				else
    					message += "S" + nextStTuple.getNextProbLocalState().getIndex() + "(" 
    								+ nextStTuple.getNextProbLocalState().getLpn().getLabel() +"), rate="
    								+ nextStTuple.getTranRate() + newLine;
    			}            		
    			System.out.print(message);            		
    		}
    		System.out.println("--------------End Of Next State Map----------------------");
        }
    	
    }
    
    public State getNextState(State curSt, Transition firedTran) {
    	HashMap<Transition, ProbLocalStateTuple> nextMap = this.nextProbLocalStateTupleMap.get(curSt);    	
    	if(nextMap == null || nextMap.get(firedTran) == null)
    		return null; 
//    	if (nextMap.get(firedTran) == null) {
//    		String newLine = System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
//    		System.out.println("*** No entry found for state S" + curSt.getIndex() + "(" + curSt.getLpn().getLabel() 
//    				+"), fired tran " + firedTran.getLabel() + "(" + firedTran.getLpn().getLabel() +")");
//    		System.out.println("----------------Next State Map @ ProbLocalStateGraph -> getNextState()----------------");
//    		for (State st : nextProbLocalStateTupleMap.keySet()) {
//    			HashMap<Transition, ProbLocalStateTuple> nextStMap = nextProbLocalStateTupleMap.get(st);
//    			System.out.println("S" + st.getIndex() + "("+ st.getLpn().getLabel() + "):");
//    			String message = "";
//    			for (Transition t : nextStMap.keySet()) {
//    				message += "\t" + t.getLabel() + "(" + t.getLpn().getLabel() +") ==> "; 
//    				ProbLocalStateTuple nextStTuple = nextStMap.get(t);
//    				if (nextStTuple.getNextProbLocalState() == null)
//    					message += "null" + newLine;
//    				else
//    					message += "S" + nextStTuple.getNextProbLocalState().getLabel() + "(" 
//    								+ nextStTuple.getNextProbLocalState().getLpn().getLabel() +"), rate="
//    								+ nextStTuple.getTranRate() + newLine;
//    			}            		
//    			System.out.print(message);            		
//    		}
//    		System.out.println("---------------End Of Next State Map--------------------");	
//    	}
    	return nextMap.get(firedTran).getNextProbLocalState();
    }
    
	public HashMap<State, HashMap<Transition, ProbLocalStateTuple>> getNextProbLocalStateMap() {
		return this.nextProbLocalStateTupleMap;
	}
	
	public State genInitialState() {	
		// create initial vector
		int size = this.lpn.getVarIndexMap().size();
		int[] initialVector = new int[size];
		for(int i = 0; i < size; i++) {
			String var = this.lpn.getVarIndexMap().getKey(i);
			int val = this.lpn.getInitVector(var);
			initialVector[i] = val;
		}
		HashMap<Transition, ProbLocalStateTuple> nextStateSubMap = new HashMap<Transition, ProbLocalStateTuple>();
		boolean[] initTranVector = genInitTranVectorBuildNextStateSubMap(initialVector, nextStateSubMap);
		this.init = new ProbLocalState(this.lpn, this.lpn.getInitialMarkingsArray(), initialVector, initTranVector);
		this.nextProbLocalStateTupleMap.put(this.init, nextStateSubMap);    	
		if (Options.getDebugMode()) {
			String newLine = System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
			System.out.println("----------------Next State Map @ StateGraph.java => genInitialState----------------");
			for (State st : nextProbLocalStateTupleMap.keySet()) {
				HashMap<Transition, ProbLocalStateTuple> nextStMap = nextProbLocalStateTupleMap.get(st);
				System.out.println("S" + st.getIndex() + "("+ st.getLpn().getLabel() + "):");
				String message = "";
				for (Transition t : nextStMap.keySet()) {
					message += "\t" + t.getLabel() + "(" + t.getLpn().getLabel() +") ==> "; 
					ProbLocalStateTuple nextStTuple = nextStMap.get(t);
					if (nextStTuple.getNextProbLocalState() == null)
						message += "null, rate=" + nextStTuple.getTranRate() + newLine;
					else
						message += "S" + nextStTuple.getNextProbLocalState().getLabel() + "(" 
								+ nextStTuple.getNextProbLocalState().getLpn().getLabel() +"), rate="
								+ nextStTuple.getTranRate() + newLine;
				}            		
				System.out.print(message);            		
			}
			System.out.println("--------------End Of Next State Map----------------------");
		}
		return this.init;
	}
    
    public State fire(final StateGraph thisSg, final State curState, Transition firedTran){
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
    		for (int prep : this.lpn.getPresetIndex(firedTran.getLabel())) {
    			curNewMarking[prep]=0;
    		}
    		for (int postp : this.lpn.getPostsetIndex(firedTran.getLabel())) {
    			curNewMarking[postp]=1;
    		}
    	}

    	//  State vector update
    	int[] newVectorArray = curState.getVector().clone();
    	int[] curVector = curState.getVector();
    	HashMap<String, String> currentValuesAsString = this.lpn.getAllVarsWithValuesAsString(curVector);
    	for (String key : currentValuesAsString.keySet()) {
    		if (this.lpn.getBoolAssignTree(firedTran.getLabel(), key) != null) {
    			int newValue = (int)this.lpn.getBoolAssignTree(firedTran.getLabel(), key).evaluateExpr(currentValuesAsString);
    			newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
    		}

    		if (this.lpn.getIntAssignTree(firedTran.getLabel(), key) != null) {
    			int newValue = (int)this.lpn.getIntAssignTree(firedTran.getLabel(), key).evaluateExpr(currentValuesAsString);
    			newVectorArray[this.lpn.getVarIndexMap().get(key)] = newValue;
    		}
    	} 
    	// Enabled transition vector update
    	boolean[] newEnabledTranVector = curState.getTranVector().clone();
    	HashMap<Transition, ProbLocalStateTuple> nextStateSubMap = new HashMap<Transition, ProbLocalStateTuple>();
    	updateTranVectorBuildNextStateSubMap(newEnabledTranVector, curNewMarking, newVectorArray, firedTran, nextStateSubMap);        	
    	State newState = thisSg.addState(new ProbLocalState(this.lpn, curNewMarking, newVectorArray, newEnabledTranVector));
    	HashMap<Transition, ProbLocalStateTuple> existingNextStateSubMap = ((ProbLocalStateGraph) thisSg).getNextProbLocalStateMap().get(newState); 
    	if (existingNextStateSubMap == null) 
    		((ProbLocalStateGraph) thisSg).getNextProbLocalStateMap().put(newState, nextStateSubMap);        		        	        	   	
    	else
    		existingNextStateSubMap.putAll(nextStateSubMap);
    	if (Options.getDebugMode()) {
    		String newLine = System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
    		System.out.println("----------------Next State Map @ StateGraph.java => fire----------------");
    		for (State st : ((ProbLocalStateGraph) thisSg).getNextProbLocalStateMap().keySet()) {
    			HashMap<Transition, ProbLocalStateTuple> nextStMap = ((ProbLocalStateGraph) thisSg).getNextProbLocalStateMap().get(st);
    			System.out.println("S" + st.getIndex() + "("+ st.getLpn().getLabel() + "):");
    			String message = "";
    			for (Transition t : nextStMap.keySet()) {
    				message += "\t" + t.getLabel() + "(" + t.getLpn().getLabel() +") ==> "; 
    				ProbLocalStateTuple nextStTuple = nextStMap.get(t);
    				if (nextStTuple.getNextProbLocalState() == null)
    					message += "null, rate=" + nextStTuple.getTranRate() + newLine;
    				else
    					message += "S" + nextStTuple.getNextProbLocalState().getLabel() + "(" 
    							+ nextStTuple.getNextProbLocalState().getLpn().getLabel() +"), rate="
    							+ nextStTuple.getTranRate() + newLine;
    			}            		
    			System.out.print(message);            		
    		}
    		System.out.println("--------------End Of Next State Map----------------------");
    	}
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
    	double firedTranRate = ((ProbLocalStateGraph) thisSg).getNextProbLocalStateMap().get(curState).get(firedTran).getTranRate();
    	((ProbLocalStateGraph)thisSg).addStateTran(curState, firedTran, firedTranRate, newState);
    	return newState;
    }
    
    public boolean[] genInitTranVectorBuildNextStateSubMap(int[] initialVector,  HashMap<Transition, ProbLocalStateTuple> nextStateSubMap) {
     	boolean[] initEnabledTrans = new boolean[this.lpn.getAllTransitions().length];
    	tran_outter_loop: for (int i=0; i< this.lpn.getAllTransitions().length; i++) {
    		Transition transition = this.lpn.getAllTransitions()[i];
    		Place[] tranPreset = this.lpn.getTransition(transition.getLabel()).getPreset(); 
    		String tranName = transition.getLabel();
    		if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
    			for (int j=0; j<tranPreset.length; j++) {
    				if (!tranPreset[j].isMarked()) {
    					initEnabledTrans[i] = false;						
    					continue tran_outter_loop;
    				}
    			}
    		}
    		if (this.lpn.getEnablingTree(tranName) != null && this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(initialVector)) == 0.0) {
    			initEnabledTrans[i] = false;
    			continue;
    		}
    		if (this.lpn.getTransitionRateTree(tranName) != null) {
    			double tranRate = this.lpn.getTransitionRateTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(initialVector));		
    			if (tranRate == 0.0) {
    				if (Options.getDebugMode()) {
    					//						System.out.println("Rate is zero");
    				}
    				initEnabledTrans[i] = false;
    				continue;
    			}
    			initEnabledTrans[i] = true;
    			nextStateSubMap.put(transition, new ProbLocalStateTuple(null, tranRate));
    		}

    	}
    	return initEnabledTrans;
    }
    
    /**
     * This method is called by search_dfs(StateGraph[], State[]).
     * @param curSgArray
     * @param curStateArray
     * @param firedTran
     * @return
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
        vvSet = this.lpn.getAllVarsWithValuesAsInt(nextState.getVector());
		for(LhpnFile curLPN : firedTran.getDstLpnList()) {
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
            		nextStateArray[curIdx] = cachedOther;
            		//TODO: Need to pass the firedTran rate, so that it can be added to other affected LPNs. 
            		int firedTranLPNindex = firedTran.getLpn().getLpnIndex();
            		State firedTranCurState = curStateArray[firedTranLPNindex];
            		double firedTranRate = nextProbLocalStateTupleMap.get(firedTranCurState).get(firedTran).getTranRate();
            		((ProbLocalStateGraph)curSgArray[curIdx]).addStateTran(curStateArray[curIdx], firedTran, firedTranRate, cachedOther);
        		}   		
        	}
        }	
        return nextStateArray;
    }
	
    
}
