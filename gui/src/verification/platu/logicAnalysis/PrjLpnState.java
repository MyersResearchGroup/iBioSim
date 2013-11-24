package verification.platu.logicAnalysis;

import java.util.*;

public class PrjLpnState {

	protected  LpnState[] stateArray;
	
	
	public PrjLpnState() {
		stateArray = null;
	}
	
	public PrjLpnState(final LpnState[] other) {
		stateArray = other;
	}
	
	@Override
	public boolean equals(final Object other) {
		PrjLpnState otherState = (PrjLpnState)other;

		if(this.stateArray == otherState.stateArray)
			return true;

		if(this.stateArray.length != otherState.stateArray.length)
			return false;

		for(int i = 0; i < this.stateArray.length; i++)
			if(this.stateArray[i] != otherState.stateArray[i])
				return false;

		return true;
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
}            
