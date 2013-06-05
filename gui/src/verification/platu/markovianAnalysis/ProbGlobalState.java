package verification.platu.markovianAnalysis;

import java.util.ArrayList;

import lpn.parser.Transition;
import verification.platu.project.PrjState;
import verification.platu.stategraph.State;

public class ProbGlobalState extends PrjState {	
	
	private int color;
	private double currentProb;
	private double nextProb;
	/**
	 * Index of the local state that where the most recent fired transition originates.
	 */
	private ArrayList<Integer> localStateIndexArray;

	public ProbGlobalState(State[] other, Transition newFiredTran,
			int newTranFiringCnt) {
		super(other, newFiredTran, newTranFiringCnt);
		localStateIndexArray = new ArrayList<Integer>();
	}

	public ProbGlobalState(State[] other) {
		super(other);
		localStateIndexArray = new ArrayList<Integer>();
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
	
	public ArrayList<Integer> getLocalStateIndex() {
		return localStateIndexArray;
	}

//	public void setLocalStateIndex(ArrayList<Integer> localStateIndex) {
//		this.localStateIndexArray = localStateIndex;
//	}
	
	public void addLocalStateIndex(int localStateIndex) {
		this.localStateIndexArray.add(localStateIndex);
	}
	
}
