package verification.platu.markovianAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import lpn.parser.Transition;
import verification.platu.main.Options;
import verification.platu.project.PrjState;
import verification.platu.stategraph.State;

public class ProbGlobalState extends PrjState {	
	
	private int color;
	private double currentProb;
	private double nextProb;
	private double piProb;
	
	/**
	 * Sum of all outgoing transition rates from this state. 
	 */
	private double tranRateSum;
	
	/**
	 * A hash map recording the transition-state flow information at current global state.
	 * Key: outgoing transition from current state.
	 * Value: next global state reached by the corresponding outgoing transition. 
	 */
	private HashMap<Transition, PrjState> nextProbGlobalStateMap;

	/**
	 * This map stores for each state the probability value computed when checking
	 * a nested property. 
	 * key: "Pr" + nestedPropString.hashCode() or "St" + nestedPropString.hashCode()
	 * value: success probability
	 */
	private HashMap<String, String> nestedProbValues;
	
	public Semaphore lock;
	
	
	public ProbGlobalState(State[] other) {
		super(other);
		tranRateSum = 0.0;
//		isExplored = false;
		if (Options.getBuildGlobalStateGraph())
			nextProbGlobalStateMap = new HashMap<Transition, PrjState>();	
		lock = new Semaphore(1);
	}
	
	public HashMap<String, String> getNestedProbValues() {
		return nestedProbValues;
	}

	public void addNestedProb(String id, String value) {
		if (nestedProbValues == null)
			nestedProbValues = new HashMap<String, String>();
		nestedProbValues.put(id, value);
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
	
	public void setCurrentProbToNext() {
		currentProb = nextProb;
	}

	public void addNextGlobalState(Transition firedTran, PrjState nextPrjState) {
		this.nextProbGlobalStateMap.put(firedTran, (ProbGlobalState) nextPrjState);
		
	}

	public double getOutgoingTranRate(Transition tran) {
		int curLocalStIndex = tran.getLpn().getLpnIndex();
		State curLocalState = this.toStateArray()[curLocalStIndex];
		double tranRate = ((ProbLocalStateGraph) curLocalState.getStateGraph()).getTranRate(curLocalState, tran);	
		return tranRate;
	}

	public double getTranProb(Transition tran) {
		double tranProb = 0.0;
		if (tranRateSum != 0)
			tranProb = getOutgoingTranRate(tran)/tranRateSum;
		else
			tranProb = 0.0;
		return tranProb;
	}

	public double getPiProb() {
		return piProb;
	}

	public void setPiProb(double piProb) {
		this.piProb = piProb;
	}
	
	/**
	 * If nextProbGlobalStateMap exists, this method returns the value field of it. 
	 * If not, this method uses local state-transition information to search for the next global states of 
	 * this current global state, and returns a set of such next global states. The search
	 * is performed based on the local states that this current global state is composed of. For example, assume a current
	 * global state S0 is composed of n (n>=1) local states: s_00,s_10,s_20...,s_n0. For each outgoing transition, t_k, of 
	 * s_00, it finds in each of the local state, namely s_00, s_10, s_20, ..., s_n0, their next local states. It then 
	 * pieces them together to form a next global state. Next, it grabs the equivalent one from the global state set. The obtained is a 
	 * next global state reached from S0 by taking t_k. The whole process repeats for s_10, s_20, ..., s_n0. All obtained 
	 * next global states are added to a set and returned by this method.
	 * @param globalStateSet
	 * @return
	 */
	public HashSet<ProbGlobalState> getNextProbGlobalStateSet(ProbGlobalStateSet globalStateSet) {
		HashSet<ProbGlobalState> nextProbGlobalStateSet = new HashSet<ProbGlobalState>();
		if (Options.getBuildGlobalStateGraph()) {			
			for (Transition outTran : nextProbGlobalStateMap.keySet())
				nextProbGlobalStateSet.add((ProbGlobalState) getNextProbGlobalState(outTran, globalStateSet));			
			return nextProbGlobalStateSet;						
		}			
		else {			
			for (State localSt : this.toStateArray()) 
				for (Transition outTran : localSt.getOutgoingTranSet()) 
					nextProbGlobalStateSet.add((ProbGlobalState) getNextProbGlobalState(outTran, globalStateSet));							
			return nextProbGlobalStateSet;
		}			
	}

	/**
	 * If nextProbGlobalStateMap exists, this method returns the value for the given outTran, else it calls 
	 * getNextPrjState(Transition, HashMap<PrjState, PrjState>) in PrjState.java.  
	 * @param outTran
	 * @param globalStateSet
	 * @return
	 */
	public PrjState getNextProbGlobalState(Transition outTran, ProbGlobalStateSet globalStateSet) {
		if (Options.getBuildGlobalStateGraph())
			return nextProbGlobalStateMap.get(outTran);
		else {
			State[] nextStateArray = new State[this.toStateArray().length];
			for (State curLocalSt : this.toStateArray()) {
				State nextLocalSt = curLocalSt.getNextLocalState(outTran);
				if (nextLocalSt != null) { // outTran leads curLocalSt to a next state.
					nextStateArray[curLocalSt.getLpn().getLpnIndex()] = nextLocalSt;//nextOtherLocalSt.clone();
				}
				else { // No nextLocalSt was found. Transition outTran did not change this local state.
					nextStateArray[curLocalSt.getLpn().getLpnIndex()] = curLocalSt;
				}
			}
			PrjState tmpPrjSt = new ProbGlobalState(nextStateArray);			
			if (((ProbGlobalStateSet)globalStateSet).get(tmpPrjSt) == null) 
				throw new NullPointerException("Next global state was not found.");		
			else
				return ((ProbGlobalStateSet) globalStateSet).get(tmpPrjSt);				
		}
	}

	/**
	 * If nextProbGlobalStateMap exists, this method returns the keySet of it. Otherwise, it calls 
	 * getOutgoingTrans() in PrjState.java. 
	 * @return
	 */
	public Set<Transition> getOutgoingTranSetForProbGlobalState() {
		if (Options.getBuildGlobalStateGraph())
			return nextProbGlobalStateMap.keySet();
		else {
			Set<Transition> outgoingTrans = new HashSet<Transition>();
			for (State curLocalSt : this.toStateArray())
				outgoingTrans.addAll(curLocalSt.getOutgoingTranSet());
			return outgoingTrans;
		}
	}

	public void computeTranRateSum() {		
		if (Options.getBuildGlobalStateGraph())
			for (Transition tran : nextProbGlobalStateMap.keySet()) 
				tranRateSum += getOutgoingTranRate(tran);
		else
			for (Transition tran : getOutgoingTranSetForProbGlobalState())
				tranRateSum += getOutgoingTranRate(tran);				
	}

	public double getTranRateSum() {
		return tranRateSum;
	}

//	public boolean isExplored() {		
//		return isExplored;
//	}
//
//	public void markAsExplored() {
//		isExplored = true;	
//	}
//
//	public void resetExplored() {
//		isExplored = false;		
//	}

	public void setCurrentProbToPi() {
		currentProb = piProb;
	}

	public void setTranRateSum(double tranRateSum) {
		this.tranRateSum = tranRateSum;		
	}

	public HashMap<String, String> getVariables() {
		HashMap<String, String> varMap = new HashMap<String, String>();
		for (State localSt : this.toStateArray()) {
			varMap.putAll(localSt.getLpn().getAllVarsWithValuesAsString(localSt.getVariableVector()));			
		}
		if (nestedProbValues != null)
			varMap.putAll(nestedProbValues);
		return varMap;
	}
}
