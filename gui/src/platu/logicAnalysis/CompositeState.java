package platu.logicAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import platu.lpn.LPNTran;
import platu.stategraph.state.State;

public class CompositeState {
	private int hashCode = 0;
	private State[] stateTuple = null;
//	private LPNTran[] tranArray = null;
//	private CompositeState[] nextStateArray = null;
	public List<LPNTran> enabledTranList = new ArrayList<LPNTran>();
	public List<CompositeState> nextStateList = new ArrayList<CompositeState>();
	public List<CompositeState> incomingStateList = new ArrayList<CompositeState>();
	private String label = null;
//	public Map<LPNTran, CompositeState> nextStateMap = new HashMap<LPNTran, CompositeState>();
//	public Map<LPNTran, CompositeState> previousStateMap = new HashMap<LPNTran, CompositeState>();
	public CompositeState(State[] stateArray){
		this.stateTuple = stateArray;
	}
	
	/**
	 * Adds a state transition from this state.
	 * @param lpnTran - LPN transition causing the state transition
	 * @param cState - Next state
	 * @return The next CompositeState object already associated with the LPNTran, otherwise null.
	 */
//	public CompositeState addNextStateTran(LPNTran lpnTran, CompositeState cState){
//		return this.nextStateMap.put(lpnTran, cState);
//	}
	
	/**
	 * Adds a state transition to this state.
	 * @param lpnTran - LPN transition causing the state transition
	 * @param cState - Previous state
	 * @return The previous CompositeState object already associated with the LPNTran, otherwise null.
	 */
//	public CompositeState addPreviousStateTran(LPNTran lpnTran, CompositeState cState){
//		return this.previousStateMap.put(lpnTran, cState);
//	}
	
//	public void setTranArray(LPNTran[] tranArray){
//		this.tranArray = tranArray;
//	}
//	
//	public LPNTran[] getTranArray(){
//		return this.tranArray;
//	}
//	
//	public void setNextStateArray(CompositeState[] nextStateArray){
//		this.nextStateArray = nextStateArray;
//	}
//	
//	public CompositeState[] getNextStateArray(){
//		return this.nextStateArray;
//	}
	
	public String getLabel(){
		if(this.label == null){
			this.label = "";
			
			for(int i = 0; i < this.stateTuple.length; i++){
				label += this.stateTuple[i].getIndex();
				
				if(i < this.stateTuple.length - 1){
					label += ",";
				}
			}
		}
		
		return this.label;
	}
	
	public int getSize(){
		return this.stateTuple.length;
	}
	
	public State[] getStateTuple(){
		return this.stateTuple;
	}
	
//	public void addEdge(CompositeStateTran edge){
//		this.edgeList.add(edge);
//	}
	
//	public void addTran(LPNTran lpnTran){
//		this.tranList.add(lpnTran);
//	}
//	
//	public void addNextState(CompositeState st){
//		this.nextStateList.add(st);
//	}
	
//	public List<CompositeStateTran> getEdgeList(){
//		return this.edgeList;
//	}
	
//	public List<LPNTran> getTranList(){
//		return this.tranList;
//	}
//	
//	public List<CompositeState> getNextStateList(){
//		return this.nextStateList;
//	}

	@Override
	public int hashCode() {
		if(hashCode == 0){
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(stateTuple);
			hashCode = result;
		}
		
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		CompositeState other = (CompositeState) obj;		
		if (!Arrays.equals(stateTuple, other.stateTuple))
			return false;
		
		return true;
	}
	
	
}
