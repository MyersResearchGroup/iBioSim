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
package main.java.edu.utah.ece.async.verification.platu.common;


import java.util.Arrays;

import main.java.edu.utah.ece.async.verification.platu.common.PlatuObj;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class IntArrayObj extends PlatuObj {
	int index;
	int[] IntArray;

	public IntArrayObj(int[] intArray) {
		super();
		index = 0;
		IntArray = intArray;
	}
	
	public int[] toArray() {
		return this.IntArray;
	}
	
	@Override
	public void setIndex(int Idx) {
		this.index = Idx;
	}
	
	@Override
	public int getIndex() {
		return this.index;
	}
	
	@Override
	public void setLabel(String lbl) {}

	@Override
	public String getLabel() { return null; }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(IntArray);
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
		IntArrayObj other = (IntArrayObj) obj;
		if (!Arrays.equals(IntArray, other.IntArray))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if(IntArray == null)
			return "()";
		
		String result = "(";
		for(int i = 0; i < this.IntArray.length; i++)
			result += this.IntArray[i] + ",";
		result += ")";
		return result;
	}

}
