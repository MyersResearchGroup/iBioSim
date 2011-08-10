package platu.project;

import java.util.*;
import platu.lpn.*;
import platu.stategraph.state.*;

public class prjState {
	protected  State[] stateArray;
	private prjState father;
	private prjState child;
	
	public prjState() {
		stateArray = null;
		father = null;
		child = null;
	}
	
	public prjState(final State[] other) {
		stateArray = other;
		father = null;
		child = null;
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
	
	@Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(final Object other) {
		prjState otherState = (prjState)other;

		if(this.stateArray == otherState.stateArray)
			return true;

		if(this.stateArray.length != otherState.stateArray.length)
			return false;

		for(int i = 0; i < this.stateArray.length; i++)
			if(this.stateArray[i] != otherState.stateArray[i])
				return false;

		return true;
            //fixes wrong equivelence
       // return Arrays.equals(stateArray, ((prjState)other).stateArray);
	}
	
	@Override
	public int hashCode() {
		// This hash funciton is much faster, but uses a lot more memory
		return Arrays.hashCode(stateArray);
				
		// THe following hashing approach uses less memory, but becomes slower 
		// as it causes a lot of hash collisions when the number of states grows. 
//		int hashVal = 0;
//		for(int i = 0; i < stateArray.length; i++) {
//			if(i == 0)
//				hashVal = Integer.rotateLeft(stateArray[i].hashCode(), stateArray.length - i);
//			else
//				hashVal = hashVal ^ Integer.rotateLeft(stateArray[i].hashCode(), stateArray.length - i);
//		}
//		return hashVal;
	}
	
	public void setFather(final prjState state) {
		this.father = state;
	}
	
	public void setChild(final prjState state) {
		this.child = state; 
	}
	
	public prjState getFather() {
		return this.father;
	}
	
	public prjState getChild() {
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
	public void print(final LPN[] lpnList) {
		for(int i = 0; i < stateArray.length; i++) {
			System.out.print(i +": ");
			stateArray[i].print(lpnList[i].getVarIndexMap());
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
}            

