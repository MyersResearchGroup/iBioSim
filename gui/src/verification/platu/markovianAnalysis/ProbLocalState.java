package verification.platu.markovianAnalysis;

import java.util.HashMap;

import lpn.parser.LhpnFile;
import verification.platu.lpn.DualHashMap;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

public class ProbLocalState extends State{
	
	public ProbLocalState(LhpnFile lpn, int[] marking, int[] vector,
			boolean[] tranVector) {
		super(lpn, marking, vector, tranVector);		
	}

    /* (non-Javadoc)
     * @see verification.platu.stategraph.State#update(verification.platu.stategraph.StateGraph, java.util.HashMap, verification.platu.lpn.DualHashMap)
     * Return a new state if the newVector leads to a new state from this state; otherwise return null. Also, adjust the tranRateVector.
     */
    public State update(StateGraph thisSg, HashMap<String, Integer> newVector, DualHashMap<String, Integer> VarIndexMap) {
    	int[] newVariableVector = new int[this.vector.length];   	
    	boolean newStateExists = false;
    	for(int index = 0; index < vector.length; index++) {
    		String var = VarIndexMap.getKey(index);
    		int this_val = this.vector[index];    		
			Integer newVal = newVector.get(var);
    		if(newVal != null) {
    			if(this_val != newVal) {
    				newStateExists = true;
    				newVariableVector[index] = newVal;
    			}
    			else
    				newVariableVector[index] = this.vector[index]; 
    		}
    		else
    			newVariableVector[index] = this.vector[index];    		
    	}
    	if(newStateExists) {    		
    		boolean[] newTranVector= ((ProbLocalStateGraph)thisSg).updateTranVector(this, this.marking, newVariableVector, null); 
    		State newState = thisSg.addState(new ProbLocalState(this.lpn, this.marking, newVariableVector, newTranVector));
        	return newState;
    	}
    	return null;
    }
    
}


