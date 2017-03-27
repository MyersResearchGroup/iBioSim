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
package edu.utah.ece.async.verification.platu.MDD;

import java.util.*;

import edu.utah.ece.async.verification.platu.stategraph.*;

/**
 * This data structure cannot be used as stack as it allows node sharing.
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class MDT {
	static mdtNode terminal = new mdtNode();

	private mdtNode root;
	//private int stateCount;
	//private int peakNodes;
	int height;
	int Size;
		
	/*
	 * Initialize MDD with the number of modules in the design model.
	 */
	public MDT(int levels) {
		root = new mdtNode();
		//stateCount = 0;
		height = levels;
		//peakNodes = 0;
	}
		
	public void push(State[] curIdxArray) {
		root.push(curIdxArray, 0);
		this.Size++;
	}
		
	public State[] pop() {
		this.Size--;
		State[] results = new State[this.height];
		return root.pop(results);
	}
	
	public Stack<State[]> popList() {
		this.Size--;
		State[] results = new State[this.height];
		return root.popList(results);
	}
	
//	public State[] peek() {
//		return root.peek(0);
//	}
	
	public boolean contains(State[] stateArray) {
		return root.contains(stateArray, 0);
	}
	
	public boolean empty() {
		return root.empty();
	}
	
	public int size() {
		return this.Size;
	}
	
	public int nodeCnt() {
		return root.nodeCnt();
	}
}
