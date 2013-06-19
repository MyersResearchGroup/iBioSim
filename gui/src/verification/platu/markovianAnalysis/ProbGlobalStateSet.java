package verification.platu.markovianAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lpn.parser.Transition;

import verification.platu.project.PrjState;

public class ProbGlobalStateSet extends HashSet<PrjState>{

	private PrjState initState;

	public ProbGlobalStateSet() {
		super();
	}

	/*
	 * Get the initial state.
	 */
	public PrjState getInitState() {
		return initState;
	}

	/* 
     * Set the initial state.
     * @param initState
     * 		The initial state.
     */
	public void setInitState(PrjState initPrjState) {
		this.initState = initPrjState;	
	}

	public Boolean contains(PrjState s) {
		return this.contains(s);
	}

	public HashSet<PrjState> getProbGlobalStateSet() {		
		return this;
	}

//	public ArrayList<ProbGlobalState> getNextGlobalStates(PrjState curGlobalState, ProbLocalStateGraph[] sgList) {
//		ArrayList<ProbGlobalState> nextGlobalStates = new ArrayList<ProbGlobalState>();
//		for (int indexOfCurStateInStateArray : ((ProbGlobalState)curGlobalState).getLocalStateIndexArray()) {
//			ProbLocalState curState = (ProbLocalState) curGlobalState.getStateArray()[indexOfCurStateInStateArray];
//			HashMap<Transition, ProbLocalStateTuple> nextLocalStateMap = ((ProbLocalStateGraph) sgList[indexOfCurStateInStateArray]).getNextProbLocalStateTupleMap().get(curState);
//			for (Transition tran : nextLocalStateMap.keySet()) {
//				ProbLocalStateTuple nextLocalStateTuple = nextLocalStateMap.get(tran);				
//				ProbGlobalState nextProbGlobalState = (ProbGlobalState) nextLocalStateTuple.getNextLocalToGlobalMap().get(tran);
//				nextGlobalStates.add(nextProbGlobalState);
//			}
//		}
//		return nextGlobalStates;
//	}
    
	
}
