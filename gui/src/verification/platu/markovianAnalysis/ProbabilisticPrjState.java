package verification.platu.markovianAnalysis;

import lpn.parser.Transition;
import verification.platu.project.PrjState;
import verification.platu.stategraph.State;

public class ProbabilisticPrjState extends PrjState {	
	
	private int color;
	private double currentProb;
	private double nextProb;
	private double transitionSum;
	
	public ProbabilisticPrjState() {
		super();
	}

	public ProbabilisticPrjState(State[] other, Transition newFiredTran,
			int newTranFiringCnt) {
		super(other, newFiredTran, newTranFiringCnt);
	}

	public ProbabilisticPrjState(State[] other) {
		super(other);
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
	
//	private double getTransitionSum(double noRate, State n) {
//		if (transitionSum == -1) {
//			transitionSum = 0;
//			for (StateTransitionPair next : nextStates) {
//				if (next.isEnabled()) {
//					transitionSum += next.getTransition();
//				}
//			}
//		}
//		return transitionSum;
//	}
	
}
