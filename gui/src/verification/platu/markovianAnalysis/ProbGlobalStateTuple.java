package verification.platu.markovianAnalysis;

import verification.platu.project.PrjState;


public class ProbGlobalStateTuple {
	
	PrjState probPrjState; 
	double tranRate;		
	/**
	 * Transition probability for the embedded Markov chain. 
	 */
	double tranProb;
	
	public ProbGlobalStateTuple(PrjState nextPrjState, double tranRate, double tranProb) {
		if (nextPrjState != null)
			this.probPrjState = nextPrjState;
		this.tranRate = tranRate;
		this.tranProb = tranProb;
	}

	public PrjState getNextProbGlobalState() {		
		return probPrjState;
	}

	public void addNextProbGlobalState(ProbGlobalState nextSt) {
		probPrjState = nextSt;	
	}

	@Override
	public String toString() {
		return "ProbGlobalStateTuple [probPrjState=" + probPrjState
				+ ", tranRate=" + tranRate + ", tranProb=" + tranProb + "]";
	}

	
}
