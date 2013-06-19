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
		
	/**
	 * An array of enabled transition rates in this state. 
	 * The index in this array corresponds to that in the tranVector in State.
	 * Note that tranRateVector ONLY serves as a place holder for the computed transition rates.
	 * It is NOT part of a state and therefore it should not be used to override 
	 * hashCode and equals methods.
	 */
	private double[] tranRateVector; 
	
	public ProbLocalState(LhpnFile lpn, int[] marking, int[] vector,
			boolean[] tranVector, double[] tranRateVector) {
		super(lpn, marking, vector, tranVector);
		this.tranRateVector = tranRateVector; 
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
    		boolean[] newTranVector = this.getTranVector().clone();		
    		double[] newTranRateVector = ((ProbLocalStateGraph)thisSg).updateTranAndRateVectors(newTranVector, this.marking, newVariableVector, null); 
    		State newState = thisSg.addState(new ProbLocalState(this.lpn, this.marking, newVariableVector, newTranVector, newTranRateVector));
        	return newState;
    	}
    	return null;
    }

    protected double getTranRate(int tranIndex) {
    	return tranRateVector[tranIndex];    	
    }
    
}


