package verification.timed_state_exploration.zone;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;
import verification.platu.lpn.LpnTranList;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

public class StateGraph_timed extends StateGraph{

	public StateGraph_timed(LhpnFile lpn) {
		super(lpn);
		// TODO Auto-generated constructor stub
	}
	
	
	
	public LpnTranList getEnabled(State curState)
	{
		// TODO: Overrides the base state and must add checks for with zones.
		// Note : Can also just make a new method signature with the TimedState
		return null;
	}
	
	 public State[] fire(final StateGraph[] curSgArray, final State[] curStateArray,
			 Transition firedTran)
	 {
		 // TODO: Overrides the base and must add the zone factor.
		 return null;
	 }

}
