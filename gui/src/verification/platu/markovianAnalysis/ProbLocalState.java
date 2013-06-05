package verification.platu.markovianAnalysis;

import java.util.HashMap;
import java.util.Set;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;
import verification.platu.lpn.DualHashMap;
import verification.platu.main.Options;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

public class ProbLocalState extends State{
		
	public ProbLocalState(LhpnFile lpn, int[] marking, int[] vector,
			boolean[] tranVector) {
		super(lpn, marking, vector, tranVector);
	}
	
    /**
     * Return a new state if the newVector leads to a new state from this state; otherwise return null.
     * @param newVector
     * @param VarIndexMap
     * @return
     */
    public State update(StateGraph thisSg, HashMap<String, Integer> newVector, DualHashMap<String, Integer> VarIndexMap) {
    	int[] newStateVector = new int[this.vector.length];   	
    	boolean newStateExists = false;
    	for(int index = 0; index < vector.length; index++) {
    		String var = VarIndexMap.getKey(index);
    		int this_val = this.vector[index];    		
			Integer newVal = newVector.get(var);
    		if(newVal != null) {
    			if(this_val != newVal) {
    				newStateExists = true;
    				newStateVector[index] = newVal;
    			}
    			else
    				newStateVector[index] = this.vector[index]; 
    		}
    		else
    			newStateVector[index] = this.vector[index];    		
    	}
    	if(newStateExists == true) {    
    		boolean[] newEnabledTranVector = this.getTranVector().clone();    		
        	HashMap<Transition, ProbLocalStateTuple> nextStateMap = new HashMap<Transition, ProbLocalStateTuple>();
    		((ProbLocalStateGraph)thisSg).updateTranVectorBuildNextStateSubMap(newEnabledTranVector, this.marking, newStateVector, null, nextStateMap);
        	State newState = new ProbLocalState(this.lpn, this.marking, newStateVector, newEnabledTranVector);
        	HashMap<Transition, ProbLocalStateTuple> existingNextStateMap = ((ProbLocalStateGraph) thisSg).getNextProbLocalStateMap().get(newState); 
        	if (existingNextStateMap == null)	
        	   	((ProbLocalStateGraph) thisSg).getNextProbLocalStateMap().put(newState, nextStateMap);
        	else
        		existingNextStateMap.putAll(nextStateMap);
        	if (Options.getDebugMode()) {
        		String newLine = System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
        		System.out.println("----------------Next State Map @ ProbLocalState -> update()----------------");        		
        		for (State st : ((ProbLocalStateGraph) thisSg).getNextProbLocalStateMap().keySet()) {
        			HashMap<Transition, ProbLocalStateTuple> nextStMap = ((ProbLocalStateGraph) thisSg).getNextProbLocalStateMap().get(st);
        			System.out.println("S" + st.getIndex() + "("+ st.getLpn().getLabel() + "):");
        			String message = "";
        			for (Transition t : nextStMap.keySet()) {
        				message += "\t" + t.getLabel() + "(" + t.getLpn().getLabel() +") ==> "; 
        				ProbLocalStateTuple nextStTuple = nextStMap.get(t);
        				if (nextStTuple.getNextProbLocalState() == null)
        					message += "null" + newLine;
        				else
        					message += "S" + nextStTuple.getNextProbLocalState().getLabel() + "(" 
        								+ nextStTuple.getNextProbLocalState().getLpn().getLabel() +"), rate="
        								+ nextStTuple.getTranRate() + newLine;
        			}            		
        			System.out.print(message);            		
        		}
        		System.out.println("---------------End Of Next State Map--------------------");
        	}
        	return newState;
    	}
    	return null;
    }
    
    public ProbLocalState getLocalProbState() {
    	//VarSet lpnOutputs = this.lpnModel.getOutputs();
    	//VarSet lpnInternals = this.lpnModel.getInternals();
    	Set<String> lpnOutputs = this.lpn.getAllOutputs().keySet();
    	Set<String> lpnInternals = this.lpn.getAllInternals().keySet();
    	DualHashMap<String,Integer> varIndexMap = this.lpn.getVarIndexMap();
    	 
    	int[] outVec = new int[this.vector.length];
    	
    	/*
    	 * Create a copy of the vector of mState such that the values of inputs are set to 0
    	 * and the values for outputs/internal variables remain the same.
    	 */
    	for(int i = 0; i < this.vector.length; i++) {
    		String curVar = varIndexMap.getKey(i);
    		if(lpnOutputs.contains(curVar) ==true || lpnInternals.contains(curVar)==true)
    			outVec[i] = this.vector[i];
    		else
    			outVec[i] = 0;
    	}    	
    	return new ProbLocalState(this.lpn, this.marking, outVec, this.tranVector);
    }
}


