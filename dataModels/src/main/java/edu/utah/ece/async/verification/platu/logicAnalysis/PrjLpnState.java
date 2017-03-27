/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.verification.platu.logicAnalysis;

import java.util.*;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
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
