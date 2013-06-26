package verification.platu.project;

import java.util.*;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;

import verification.platu.main.Options;
import verification.platu.markovianAnalysis.ProbGlobalState;
import verification.platu.stategraph.*;

public class PrjState {
	protected  State[] stateArray;
	private PrjState father;
	private PrjState child;
	//private HashMap<Transition, PrjState> nextStateMap;

	public PrjState() {
		stateArray = null;
		father = null;
		child = null;
	}
	
	public PrjState(final State[] other) {
		stateArray = other;
		father = null;
		child = null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(stateArray);
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
		
		PrjState other = (PrjState) obj;
		if(this.stateArray == other.stateArray)
			return true;
		if (!Arrays.equals(stateArray, other.stateArray))
			return false;
		return true;
	}

	public State get(int index) {
		return stateArray[index];
	}
	
	public void add(int index, State other) {
		stateArray[index] = other;
	}
	
	public State[] toStateArray() {
		return stateArray;
	}
	
	public void setFather(final PrjState state) {
		this.father = state;
	}
	
	public void setChild(final PrjState state) {
		this.child = state; 
	}
	
	public PrjState getFather() {
		return this.father;
	}
	
	public PrjState getChild() {
		return this.child; 
	}
	
	public String toString() {
		String line ="";
		for(int i = 0; i < stateArray.length; i++) {
			line += stateArray[i].toString()+"+"+stateArray[i].getLpn().getLabel() + "   ";
		}
		return line;
//            return toList(stateArray).toString();
	}
//	public void print(final LPN[] lpnList) {
//		for(int i = 0; i < stateArray.length; i++) {
//			System.out.print(i +": ");
//			stateArray[i].print(lpnList[i].getVarIndexMap());
//			System.out.println();
//		}
//	}
	
	public void print(final LhpnFile[] lpnList) {
		for(int i = 0; i < stateArray.length; i++) {
			System.out.println(lpnList[i].getLabel() + ".lpn" +": ");
			//stateArray[i].print(lpnList[i].getVarIndexMap());
			stateArray[i].printStateInfo();
			System.out.println();
		}
	}
	
    public static final Collection<Object> toList(Object[] arr) {
        Set<Object> l = new HashSet<Object>(1);
        l.addAll(Arrays.asList(arr));
        return l;
    }

    public static final Object[] toArray(Collection<Object> set) {
        Object[] arr = new Object[set.size()];
        int idx = 0;
        for (Object i : set) {
            arr[idx++] = i;
        }
        return arr;
    }

//	public void setTranOut(Transition tran, PrjState nextState) {
//		nextStateMap.put(tran, nextState);
//	}
	
//	public Transition getOutgoingTranToState(PrjState nextState) {
//		return nextStateMap.getKey(nextState);
//	}

//	public void setTranIn(Transition tran, PrjState prevState) {
//		prevStateMap.put(tran, prevState);
//	}
	
//	public Transition getIncomingTranFromState(PrjState prevState) {
//		return prevStateMap.getKey(prevState);
//	}

//	public void setNextStateMap(HashMap<Transition, PrjState> map) {
//		this.nextStateMap = map;
//	}
//	
//	public HashMap<Transition, PrjState> getNextStateMap() {
//		return nextStateMap;
//	}
 
	public State[] getStateArray() {
		return stateArray;		
	}
	
	/**
	 * This method uses local state-transition information to search for the next global states of 
	 * this current global state, and returns a set of such next global states. The search
	 * is performed based on the local states that this current global state is composed of. For example, assume a current
	 * global state S0 is composed of n (n>=1) local states: s_00,s_10,s_20...,s_n0. For each outgoing transition, t_k, of 
	 * s_00, it finds in each of the local state, namely s_00, s_10, s_20, ..., s_n0, their next local states. It then 
	 * pieces them together to form a next global state. Next, it grabs the equivalent one from the global state set. The obtained is a 
	 * next global state reached from S0 by taking t_k. The whole process repeats for s_10, s_20, ..., s_n0. All obtained 
	 * next global states are added to a set and returned by this method. 
	 * @param globalStateSet
	 * @return
	 */
	public HashSet<PrjState> getNextPrjStateSet(HashMap<PrjState, PrjState> globalStateSet) {
		HashSet<PrjState> nextPrjStateSet = new HashSet<PrjState>();
		for (State localSt : this.toStateArray()) {
			for (Transition outTran : localSt.getOutgoingTranSet()) {
				State[] nextStateArray = new State[this.toStateArray().length];
				for (State curLocalSt : this.toStateArray()) {
					State nextLocalSt = curLocalSt.getNextLocalState(outTran);
					if (nextLocalSt != null) { // outTran leads curLocalSt to a next state.
						nextStateArray[curLocalSt.getLpn().getLpnIndex()] = nextLocalSt;
					}
					else { // No nextOtherLocalSt found. Transition outTran does not change this local state.
						nextStateArray[curLocalSt.getLpn().getLpnIndex()] = curLocalSt;
					}
				}
				PrjState tmpProbGlobalSt;
				if (!Options.getMarkovianModelFlag())
					tmpProbGlobalSt = new PrjState(nextStateArray);
				else
					tmpProbGlobalSt = new ProbGlobalState(nextStateArray);
				if (globalStateSet.get(tmpProbGlobalSt) == null) {
					throw new NullPointerException("Next global state was not found.");
				}
				else {
					nextPrjStateSet.add((PrjState) globalStateSet.get(tmpProbGlobalSt));
				}
			}
		}
		return nextPrjStateSet;
	}

	/**
	 * This method uses local state-transition information to search for the next global states of 
	 * this current global state, and returns a set of such next global states. The search
	 * is performed based on the local states that this current global state is composed of. For example, assume a current
	 * global state S0 is composed of n (n>=1) local states: s_00,s_10,s_20...,s_n0. Given the outgoing transition, outTran, 
	 * it finds in each of the local state, namely s_00, s_10, s_20, ..., s_n0, their next local states, and then pieces them
	 * together to form the next global state. Next, it grabs the equivalent one from the global state set. The obtained is the
	 * next global state reached from S0 by taking outTran, and it is returned. 
	 * @param outTran
	 * @param globalStateSet
	 * @return
	 */
	public PrjState getNextPrjState(Transition outTran, HashMap<PrjState, PrjState> globalStateSet) {
		State[] nextStateArray = new State[this.toStateArray().length];
		for (State curLocalSt : this.toStateArray()) {
			State nextLocalSt = curLocalSt.getNextLocalState(outTran);
			if (nextLocalSt != null) { // outTran leads curLocalSt to a next state.
				nextStateArray[curLocalSt.getLpn().getLpnIndex()] = nextLocalSt;//nextOtherLocalSt.clone();
			}
			else { // No nextLocalSt was found. Transition outTran did not change this local state.
				nextStateArray[curLocalSt.getLpn().getLpnIndex()] = curLocalSt;
			}
		}
		PrjState tmpProbGlobalSt;
		if (!Options.getMarkovianModelFlag())
			tmpProbGlobalSt = new PrjState(nextStateArray);
		else
			tmpProbGlobalSt = new ProbGlobalState(nextStateArray);
		if (globalStateSet.get(tmpProbGlobalSt) == null) {
			throw new NullPointerException("Next global state was not found.");
		}
		else {
			return globalStateSet.get(tmpProbGlobalSt);					
		}
	}
	
	/**
	 * Return a set of outgoing transitions from this PrjState. 
	 * @return
	 */
	public Set<Transition> getOutgoingTrans() {
		Set<Transition> outgoingTrans = new HashSet<Transition>();
		for (State curLocalSt : this.toStateArray())
			outgoingTrans.addAll(curLocalSt.getOutgoingTranSet());
		return outgoingTrans;
	}

	/**
	 * Return the prjState label, which is concatenated by its local state labels. 
	 * @return
	 */
	public String getLabel() {
		String prjStateLabel = "";
		for (State localSt : this.toStateArray()) {
			prjStateLabel += localSt.getLabel() + "_";
		}
		return prjStateLabel.substring(0, prjStateLabel.lastIndexOf("_"));		
	}
}            

