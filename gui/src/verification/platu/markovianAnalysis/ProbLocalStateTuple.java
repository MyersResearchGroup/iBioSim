package verification.platu.markovianAnalysis;

import verification.platu.stategraph.State;

public class ProbLocalStateTuple {
	State probLocalState; 
	double tranRate;
	
	public ProbLocalStateTuple(State nextSt, double tranRate) {
		if (nextSt != null)
			this.probLocalState = nextSt;		
		this.tranRate = tranRate;
	}

	public State getNextProbLocalState() {
		return probLocalState;
	}

	public void addProbLocalState(State nextSt) {
		probLocalState = nextSt;
		
	}

	public double getTranRate() {
		return tranRate;
	}

	@Override
	public String toString() {
		String newLine = System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
		return "ProbLocalStateTuple [probLocalState=" + probLocalState
				+ ", tranRate=" + tranRate + "]" + newLine;
	}
	
	
}
