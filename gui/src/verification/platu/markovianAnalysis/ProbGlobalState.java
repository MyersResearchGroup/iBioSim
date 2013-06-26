package verification.platu.markovianAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
	 * Transition probability for the embedded Markov chain. 
	 */
	//private double tranProb;
	
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
//	/**
//	 * This flag is used in Markovian analysis to indicate whether this state has been explored or not.  
//	 */
//	private boolean isExplored;
	

	public ProbGlobalState(State[] other) {
		super(other);
		tranRateSum = 0.0;
//		isExplored = false;
		if (Options.getBuildGlobalStateGraph())
			nextProbGlobalStateMap = new HashMap<Transition, PrjState>();
		
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
	
//	public ArrayList<Integer> getLocalStateIndexArray() {
//		return localStateIndexArray;
//	}
//	
//	public void addLocalStateIndex(int localStateIndex) {
//		this.localStateIndexArray.add(localStateIndex);
//	}
	
//	public HashMap<Transition, ProbGlobalState> getNextProbGlobalStateMap() {
//		if (Options.getBuildGlobalStateGraph())
//			return nextProbGlobalStateMap;
//		else {
//			
//		}
//	}

//	public void setNextGlobalStateMap(
//			HashMap<Transition, ProbGlobalState> nextGlobalStateMap) {
//		this.nextGlobalStateMap = nextGlobalStateMap;
//	}

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
//		double tranProb = 0.0;
//		try {
//			tranProb = getOutgoingTranRate(tran)/tranRateSum;
//		}
//		catch (ArithmeticException e){
//			System.out.println("tranRateSum is 0. Divided by 0 here.");
//			e.printStackTrace();
//		}
//		return tranProb;
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

//	public Set<Transition> getOutgoingTransitions() {
//		if (Options.getBuildGlobalStateGraph())
//			return nextProbGlobalStateMap.keySet();
//		else {
//			Set<Transition> outgoingTrans = new HashSet<Transition>();
//			for (State curLocalSt : this.toStateArray())
//				outgoingTrans.addAll(curLocalSt.getOutgoingTranSet());
//			return outgoingTrans;
//		}
//	}

//	/**
//	 * If nextProbGlobalStateMap exists, this method returns the value field of it, else it uses local state-transition 
//	 * information to search for the next probabilistic global states of this current probabilistic global state, 
//	 * and returns a set of such next probabilistic global states. The search
//	 * is performed based on the local states that this current global state is composed of. For example, assume a current
//	 * global state S0 is composed of n (n>=1) local states: s_00,s_10,s_20...,s_n0. For each outgoing transition, t_k, of 
//	 * s_00, find in each of the local state, namely s_00, s_10, s_20, ..., s_n0, their next states, and then piece them
//	 * together to form a next global state, then grab the equivalent one from the global state set. The obtained is a 
//	 * next global state reached from S0 by taking t_k. The process repeats for s_10, s_20, ..., s_n0. All obtained 
//	 * next global states are added to a set and returned by this method. 
//	 * @param globalStateSet
//	 * @return
//	 */
//	public HashSet<ProbGlobalState> getNextProbGlobalStateSet(ProbGlobalStateSet globalStateSet) {
//		if (Options.getBuildGlobalStateGraph())
//			return (HashSet<ProbGlobalState>) nextProbGlobalStateMap.values();
//		else {
//			HashSet<ProbGlobalState> nextProbGlobalStateSet = new HashSet<ProbGlobalState>();
//			for (State localSt : this.toStateArray()) {
//				for (Transition outTran : localSt.getOutgoingTranSet()) {
//					State[] nextStateArray = new State[this.toStateArray().length];
//					for (State curLocalSt : this.toStateArray()) {
//						State nextLocalSt = curLocalSt.getNextLocalState(outTran);
//						if (nextLocalSt != null) { // outTran leads curLocalSt to a next state.
//							nextStateArray[curLocalSt.getLpn().getLpnIndex()] = nextLocalSt;
//						}
//						else { // No nextOtherLocalSt found. Transition outTran does not change this local state.
//							nextStateArray[curLocalSt.getLpn().getLpnIndex()] = curLocalSt;
//						}
//					}
//					ProbGlobalState tmpProbGlobalSt = new ProbGlobalState(nextStateArray);
//					if (globalStateSet.get(tmpProbGlobalSt) == null) {
//						throw new NullPointerException("Next global state was not found.");
//					}
//					else {
//						nextProbGlobalStateSet.add((ProbGlobalState) globalStateSet.get(tmpProbGlobalSt));
//					}
//				}
//			}
//			return nextProbGlobalStateSet;
//		}
//	}
//
//	public ProbGlobalState getNextProbGlobalStates(Transition outTran, ProbGlobalStateSet globalStateSet) {
//		if (Options.getBuildGlobalStateGraph())
//			return nextProbGlobalStateMap.get(outTran);
//		else {			
//			State[] nextStateArray = new State[this.toStateArray().length];
//			for (State curLocalSt : this.toStateArray()) {
//				State nextLocalSt = curLocalSt.getNextLocalState(outTran);
//				if (nextLocalSt != null) { // outTran leads curLocalSt to a next state.
//					nextStateArray[curLocalSt.getLpn().getLpnIndex()] = nextLocalSt;//nextOtherLocalSt.clone();
//				}
//				else { // No nextLocalSt was found. Transition outTran does not change this local state.
//					nextStateArray[curLocalSt.getLpn().getLpnIndex()] = curLocalSt;
//				}
//			}
//			ProbGlobalState tmpProbGlobalSt = new ProbGlobalState(nextStateArray);
//			if (globalStateSet.get(tmpProbGlobalSt) == null) {
//				throw new NullPointerException("Next global state was not found.");
//			}
//			else {
//				return (ProbGlobalState) globalStateSet.get(tmpProbGlobalSt);					
//			}
//		}
//	}
	
	/**
	 * If nextProbGlobalStateMap exists, this method returns the value field of it, else it calls 
	 * getNextPrjStateSet(HashMap<PrjState, PrjState>) in PrjState.java. 
	 * @param globalStateSet
	 * @return
	 */
	public HashSet<PrjState> getNextProbGlobalStateSet(ProbGlobalStateSet globalStateSet) {
		if (Options.getBuildGlobalStateGraph())
			return (HashSet<PrjState>) nextProbGlobalStateMap.values();
		else
			return this.getNextPrjStateSet(globalStateSet);
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
		else 
			return this.getNextPrjState(outTran, globalStateSet);			
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
}
