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
package backend.verification.platu.common;


import backend.verification.platu.BinaryTree.*;


public class BinTreeTable extends SetIntTuple {
	
	BinaryTree StateTable = null;
	int Size = 0;

	public BinTreeTable() {
		this.StateTable = new BinaryTree();
		this.Size = 0;
	}
	
	@Override
	public int add(int[] IntArray) {
		int newEle = StateTable.add(IntArray);
		if(newEle != 0)
			this.Size++;
		return newEle==-1 ? 0 : 1;
	}
	
	@Override
	public boolean contains(int[] IntArray) {
		return this.StateTable.contains(IntArray);
	}
	
	@Override
	public int size() {
		return this.Size;
	}
	
	@Override
	public String stats() {
		return "Element count = "+ this.StateTable.elementCount() + ",  Tree node count = " + this.StateTable.nodeCount();
	}
}
