package verification.timed_state_exploration.zone;

import java.util.List;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;
import verification.platu.lpn.LPNTran;
import verification.platu.lpn.LpnTranList;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

public class StateGraph_timed extends StateGraph{

	public StateGraph_timed(LhpnFile lpn) {
		super(lpn);
		// TODO Auto-generated constructor stub
	}
	
	
	
//	public LpnTranList getEnabled(TimedState curState)
	public LpnTranList getEnabled(State curState)
	{
		// TODO: Overrides the base state and must add checks with zones.
		// Note : Can also just make a new method signature with the TimedState
		
//		LpnTranList initEnabled = super.getEnabled(curState);
//		
//		List<Transition> enabledByZone = curState.getEnabledTransitionByZone();
//		
//		for(Transition T : enabledByZone)
//		{
//			initEnabled.remove(T);
//		}
//		
//		return initEnabled;
		
		return new LpnTranList(((TimedState) curState).getEnabledTransitionByZone());
	}
	
	 //public TimedState[] fire(final StateGraph_timed[] curSgArray, 
	//		 final State[] curStateArray, Transition firedTran)
	public State[] fire(final StateGraph_timed[] curSgArray,
			final State[] curStateArray, Transition firedTran)
	 {
		 // TODO: Overrides the base and must add the zone factor.
		 
		 State[] newState = super.fire(curSgArray, curStateArray, firedTran);
		 //TimedState[] newTimedStates = new TimedState[newState.length];
		 State[] newTimedStates = new State[newState.length];
		 
		 for(int i=0; i<newState.length; i++)
		 {
			 LpnTranList enabledTransitions = super.getEnabled(newState[i]);
			 int[] tranArray = new int[enabledTransitions.size()];
			 
			 int j=0;
			 
			 // TODO: Might be able to change the signature of the fire method
			 // for zones so that this translation step is not necessary.
			 for(Transition T : enabledTransitions)
			 {
				 tranArray[j] = T.getIndex();
				 j++;
			 }
			 
			 Zone newZone = ((TimedState) curStateArray[i]).getZone()
			 		.fireTransitionbyTransitionIndex(firedTran.getIndex(),
			 				tranArray, newState[i]);
			 
			 newTimedStates[i] = new TimedState(newState[i], newZone);
		 }
		 
		 
		 
		 return newTimedStates;
	 }



	public TimedState getInitStateTimed() {
		
		State initialStateNoTime = getInitState();
		
		// TODO Auto-generated method stub
		// Adds the zone factor.
		
		return new TimedState(initialStateNoTime);
	}
	 
	 
	
//	public TimedState addState(State mState) 
//	{
//		return null;
//	}

}
