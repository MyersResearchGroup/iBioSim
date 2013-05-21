package verification.platu.markovianAnalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Set;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;
import verification.platu.main.Options;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;



public class ProbabilisticStateGraph extends StateGraph {
    
	protected HashMap<State, HashMap<Transition, Double>> tranRateMap;
	
    public ProbabilisticStateGraph(LhpnFile lpn) {
    	super(lpn);
    	this.tranRateMap = new HashMap<State, HashMap<Transition, Double>>();
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
			for (State curState : nextStateMap.keySet()) {
				String markings = intArrayToString("markings", curState);
				String vars = intArrayToString("vars", curState);
				String enabledTrans = boolArrayToString("enabledTrans", curState);
				String curStateName = "S" + curState.getIndex();
				out.write(curStateName + "[shape=\"ellipse\",label=\"" + curStateName + "\\n<"+vars+">" + "\\n<"+enabledTrans+">" + "\\n<"+markings+">" + "\"]\n");
			}
			
			for (State curState : nextStateMap.keySet()) {
				HashMap<Transition, State> nextStMap = nextStateMap.get(curState);
				for (Transition curTran : nextStMap.keySet()) {
					String curStateName = "S" + curState.getIndex();
					String nextStateName = "S" + nextStMap.get(curTran).getIndex();					
					String curTranName = curTran.getLabel();
					String curTranRate = tranRateMap.get(curState).get(curTran).toString().substring(0, 4);
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

//	public HashMap<State, HashMap<TranRatePair, State>> getNextProbabilisticStateMap() {
//		return this.nextProbStateMap;
//	}
	
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
			HashMap<Transition, Double> trRateMap = tranRateMap.get(curState);
			if (trRateMap == null) {
				trRateMap = new HashMap<Transition, Double>();
				trRateMap.put(tran, tranRate);
				tranRateMap.put(curState, trRateMap);
			}
			else 
				trRateMap.put(tran, tranRate);
		}
		// if a transition is enabled and it is not recorded in the enabled transition vector
		curState.getTranVector()[tranIndex] = true;
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
	
    public boolean[] updateEnabledTranVector(State curState, int[] newMarking, int[] newVectorArray, Transition firedTran) {
    	boolean[] enabledTranAfterFiring = curState.getTranVector().clone();
		// Disable the fired transition and all of its conflicting transitions. 
    	if (firedTran != null) {
    		enabledTranAfterFiring[firedTran.getIndex()] = false;
    		for (Integer curConflictingTranIndex : firedTran.getConflictSetTransIndices()) {
    			enabledTranAfterFiring[curConflictingTranIndex] = false;
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
    				&& this.lpn.getEnablingTree(tranName).evaluateExpr(this.lpn.getAllVarsWithValuesAsString(newVectorArray)) == 0.0) {
    			if (Options.getDebugMode()) {
//    				System.out.println(tran.getName() + " " + "Enabling condition is false");    			
    			}					
    			if (enabledTranAfterFiring[tranIndex] && !tran.isPersistent())
    				enabledTranAfterFiring[tranIndex] = false;
    			continue;
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
    			HashMap<Transition, Double> trRateMap = tranRateMap.get(curState);
    			if (trRateMap == null) {
    				trRateMap = new HashMap<Transition, Double>();
    				trRateMap.put(tran, tranRate);
    				tranRateMap.put(curState, trRateMap);
    			}
    			else 
    				trRateMap.put(tran, tranRate);
    		}
    		if (this.lpn.getPreset(tranName) != null && this.lpn.getPreset(tranName).length != 0) {
    			for (int place : this.lpn.getPresetIndex(tranName)) {
    				if (newMarking[place]==0) {
    					if (Options.getDebugMode()) {
//    						System.out.println(tran.getName() + " " + "Missing a preset token");
    					}
    					needToUpdate = false;
    					break;
    				}
    			}
    		}
			if (needToUpdate) {            	
    			enabledTranAfterFiring[tranIndex] = true;
    			if (Options.getDebugMode()) {
//    				System.out.println(tran.getName() + " is Enabled.");
    			}					
            }
        }
    	return enabledTranAfterFiring;
	}
	
}
