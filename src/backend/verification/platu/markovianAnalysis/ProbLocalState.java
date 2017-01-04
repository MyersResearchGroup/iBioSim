package backend.verification.platu.markovianAnalysis;

import java.util.HashMap;

import backend.lpn.parser.LhpnFile;
import backend.lpn.parser.Transition;
import backend.verification.platu.lpn.DualHashMap;
import backend.verification.platu.main.Options;
import backend.verification.platu.stategraph.State;
import backend.verification.platu.stategraph.StateGraph;

public class ProbLocalState extends State{
	
	public ProbLocalState(LhpnFile lpn, int[] marking, int[] vector,
			boolean[] tranVector) {
		super(lpn, marking, vector, tranVector);		
	}

    /* (non-Javadoc)
     * @see verification.platu.stategraph.State#update(verification.platu.stategraph.StateGraph, java.util.HashMap, verification.platu.lpn.DualHashMap)
     * Return a new state if the newVector leads to a new state from this state; otherwise return null. Also, adjust the tranRateVector.
     */
    @Override
	public State update(StateGraph thisSg, HashMap<String, Integer> newVector, DualHashMap<String, Integer> VarIndexMap) {
    	int[] newVariableVector = new int[this.vector.length];   	
    	boolean nextStateExists = false;
    	for(int index = 0; index < vector.length; index++) {
    		String var = VarIndexMap.getKey(index);
    		int this_val = this.vector[index];    		
			Integer newVal = newVector.get(var);
    		if(newVal != null) {
    			if(this_val != newVal) {
    				nextStateExists = true;
    				newVariableVector[index] = newVal;
    			}
    			else
    				newVariableVector[index] = this.vector[index]; 
    		}
    		else
    			newVariableVector[index] = this.vector[index];    		
    	}
    	if(nextStateExists) {
    		HashMap<Transition, Double> tranRateMapForNextState = new HashMap<Transition, Double>();
    		boolean[] newTranVector= ((ProbLocalStateGraph)thisSg).updateTranVector(this, this.marking, newVariableVector, null, tranRateMapForNextState); 
    		State nextState = thisSg.addState(new ProbLocalState(this.lpn, this.marking, newVariableVector, newTranVector));
    		((ProbLocalStateGraph) thisSg).addTranRate(nextState, tranRateMapForNextState);
    		if (Options.getDebugMode()) {
    			String location = "ProbLocalState.java, update()";    			
    			((ProbLocalStateGraph) thisSg).printNextProbLocalTranRateMapForGivenState(nextState, location);
    		}
        	return nextState;
    	}
    	return null;
    }
    
}


