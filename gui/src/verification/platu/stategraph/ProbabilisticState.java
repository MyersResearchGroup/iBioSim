package verification.platu.stategraph;

import java.util.HashMap;
import java.util.Set;

import lpn.parser.LhpnFile;
import verification.platu.lpn.DualHashMap;
import verification.platu.stategraph.State;

public class ProbabilisticState extends State{
	private int color;
	
	private double currentProb;
	
	private double nextProb;
	
	public ProbabilisticState(LhpnFile lpn, int[] new_marking, int[] new_vector,
			boolean[] new_isTranEnabled) {
		super(lpn, new_marking, new_vector, new_isTranEnabled);
		setColor(0);
		setCurrentProb(0.0);
		setNextProb(0.0);
	}
	
	public ProbabilisticState(State other) {
		super(other);
		setColor(0);
		setCurrentProb(0.0);
		setNextProb(0.0);
	}
	
    /**
     * Return a new state if the newVector leads to a new state from this state; otherwise return null.
     * @param newVector
     * @param VarIndexMap
     * @return
     */
    public ProbabilisticState update(ProbabilisticStateGraph SG,HashMap<String, Integer> newVector, DualHashMap<String, Integer> VarIndexMap) {
    	int[] newStateVector = new int[this.vector.length];
    	
    	boolean newState = false;
    	for(int index = 0; index < vector.length; index++) {
    		String var = VarIndexMap.getKey(index);
    		int this_val = this.vector[index];
    		
			Integer newVal = newVector.get(var);
    		if(newVal != null) {
    			if(this_val != newVal) {
    				newState = true;
    				newStateVector[index] = newVal;
    			}
    			else
    				newStateVector[index] = this.vector[index]; 
    		}
    		else
    			newStateVector[index] = this.vector[index];    		
    	}
        boolean[] newEnabledTranVector = SG.updateEnabledTranVector(this.getTranVector(), this.marking, newStateVector, null);
    	if(newState == true)
    		return new ProbabilisticState(this.lpn, this.marking, newStateVector, newEnabledTranVector);
    	
    	return null;
    }
    
    public ProbabilisticState getLocalProbabilisticState() {
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
    	// TODO: (??) Need to create outTranVector as well?
    	return new ProbabilisticState(this.lpn, this.marking, outVec, this.tranVector);
    }

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public double getCurrentProb() {
		return currentProb;
	}

	public void setCurrentProb(double currentProb) {
		this.currentProb = currentProb;
	}

	public double getNextProb() {
		return nextProb;
	}

	public void setNextProb(double nextProb) {
		this.nextProb = nextProb;
	}
	

}


