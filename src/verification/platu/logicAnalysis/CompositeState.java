package verification.platu.logicAnalysis;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CompositeState{
	private int[] stateTuple = null;
	private List<CompositeStateTran> incomingStateTranList = new LinkedList<CompositeStateTran>();
	private List<CompositeStateTran> outgoingStateTranList = new LinkedList<CompositeStateTran>();
	private String label = null;
	private int index = 0;
	
	public void addIncomingStateTran(CompositeStateTran incomingTran){
		this.incomingStateTranList.add(incomingTran);
	}
	
	public boolean removeIncomingStateTran(CompositeStateTran incomingTran){
		return this.incomingStateTranList.remove(incomingTran);
	}
	
	public boolean removeOutgoingStateTran(CompositeStateTran outgoingTran){
		return this.outgoingStateTranList.remove(outgoingTran);
	}
	
	public List<CompositeStateTran> getIncomingStateTranList(){
		return this.incomingStateTranList;
	}
	
	public void addOutgoingStateTran(CompositeStateTran outgoingTran){
		this.outgoingStateTranList.add(outgoingTran);
	}
	
	public List<CompositeStateTran> getOutgoingStateTranList(){
		return this.outgoingStateTranList;
	}
	
	public int numIncomingTrans(){
		return this.incomingStateTranList.size();
	}
	
	public int numOutgoingTrans(){
		return this.outgoingStateTranList.size();
	}
	
	public CompositeState(int[] stateArray){
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
	
	public void setLabel(String lbl){
		this.label = lbl;
	}
	
	public String getLabel(){
		if(this.label == null){
			this.label = "";
			
			for(int i = 0; i < this.stateTuple.length; i++){
				label += this.stateTuple[i];
				
				if(i < this.stateTuple.length - 1){
					label += ",";
				}
			}
		}
		
		return this.label;
	}
	
	public void setIndex(int idx){
		this.index = idx;
	}
	
	public int getIndex(){
		return this.index;
	}
	
	public void clear(){
		this.outgoingStateTranList.clear();
		this.incomingStateTranList.clear();
	}
	
	@Override
	public String toString(){
		return "" + getIndex();
	}
	
	public int getSize(){
		return this.stateTuple.length;
	}
	
	public int[] getStateTuple(){
		return this.stateTuple;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(stateTuple);
		return result;
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

	
}
