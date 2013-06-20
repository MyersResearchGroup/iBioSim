package verification.platu.markovianAnalysis;

import java.util.HashMap;

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
	private double tranProb;
	/**
	 * A hash map recording the transition-state flow information at current global state.
	 * Key: outgoing transition from current state.
	 * Value: next global state reached by the corresponding outgoing transition. 
	 */
	private HashMap<Transition, ProbGlobalState> nextProbGlobalStateMap;
	//	/**
	//	 * Index of the local state where the fired transition(s) originate(s) from.
	//	 */
	//	private ArrayList<Integer> localStateIndexArray;
	

	public ProbGlobalState(State[] other) {
		super(other);
		if (Options.getBuildGlobalStateGraph())
			nextProbGlobalStateMap = new HashMap<Transition, ProbGlobalState>();
		//localStateIndexArray = new ArrayList<Integer>();
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
	
	public HashMap<Transition, ProbGlobalState> getNextProbGlobalStateMap() {
		return nextProbGlobalStateMap;
	}

//	public void setNextGlobalStateMap(
//			HashMap<Transition, ProbGlobalState> nextGlobalStateMap) {
//		this.nextGlobalStateMap = nextGlobalStateMap;
//	}

	public void addNextGlobalState(Transition firedTran, PrjState nextPrjState) {
		this.nextProbGlobalStateMap.put(firedTran, (ProbGlobalState) nextPrjState);
		
	}

	public double computeTransitionSum() {
		double tranRateSum = 0;
		for (Transition tran : nextProbGlobalStateMap.keySet()) {
			tranRateSum += getOutgoingTranRate(tran);
		}
		return tranRateSum;		
	}

	public double getOutgoingTranRate(Transition tran) {
		int curLocalStIndex = tran.getLpn().getLpnIndex();
		State curLocalState = this.toStateArray()[curLocalStIndex];
		double tranRate = ((ProbLocalStateGraph) curLocalState.getLpn().getStateGraph()).getTranRate(curLocalState, tran);	
		return tranRate;
	}

	public double getTranProb() {
		return tranProb;
	}

	public void setTranProb(double tranProb) {
		this.tranProb = tranProb;
	}

	public double getPiProb() {
		return piProb;
	}

	public void setPiProb(double piProb) {
		this.piProb = piProb;
	}

}
