package verification.platu.project;

import java.util.*;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;

import verification.platu.main.Options;
import verification.platu.stategraph.*;

public class PrjState {
	protected  State[] stateArray;
	private PrjState father;
	private PrjState child;
	private HashMap<Transition, PrjState> nextStateMap;

	public PrjState() {
		stateArray = null;
		father = null;
		child = null;
	}
	
	public PrjState(final State[] other) {
		stateArray = other;
		father = null;
		child = null;
		if (Options.getOutputSgFlag()) {
			nextStateMap = new HashMap<Transition, PrjState>();
		}
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

	public void setTranOut(Transition tran, PrjState nextState) {
		nextStateMap.put(tran, nextState);
	}
	
//	public Transition getOutgoingTranToState(PrjState nextState) {
//		return nextStateMap.getKey(nextState);
//	}

//	public void setTranIn(Transition tran, PrjState prevState) {
//		prevStateMap.put(tran, prevState);
//	}
	
//	public Transition getIncomingTranFromState(PrjState prevState) {
//		return prevStateMap.getKey(prevState);
//	}

	public void setNextStateMap(HashMap<Transition, PrjState> map) {
		this.nextStateMap = map;
	}
	
	public HashMap<Transition, PrjState> getNextStateMap() {
		return nextStateMap;
	}
 
	public State[] getStateArray() {
		return stateArray;		
	}
}            

