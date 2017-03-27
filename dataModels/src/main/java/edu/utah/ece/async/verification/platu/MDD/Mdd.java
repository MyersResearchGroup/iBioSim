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
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Mdd {
	static mddNode terminal = new mddNode();

	//private int stateCount;
	private HashMap<mddNode, mddNode>[] nodeTbl;
	private HashMap<State, HashSet<State>>[] localFirings;
	private int peakNodes;
	int height;
	
	/*
	 * Initialize MDD with the number of modules in the design model.
	 */
	@SuppressWarnings("unchecked")
	public Mdd(int numMods) {
		Mdd.terminal.level = numMods;
		//stateCount = 0;
		height = numMods;
		nodeTbl = new HashMap[height];
		localFirings = new HashMap[height+1];
		for(int i = 0; i < this.height; i++) { 
			nodeTbl[i] = new HashMap<mddNode, mddNode>();
			localFirings[i] = new HashMap<State, HashSet<State>>();
		}
			
		peakNodes = 0;
	}
	
	public static mddNode newNode() {
		return new mddNode(0);
	}
	
	/*
	 * Return a MDD node that is the root of MDD representing the union of states encoded in MDD 'target' and MDD 'source'
	 */
	public mddNode union(mddNode target, mddNode source) {
		HashMap<mddNode, HashMap<mddNode, mddNode>> unionCache = new HashMap<mddNode, HashMap<mddNode, mddNode>>();
		mddNode unionResult = target.union(source, nodeTbl, unionCache);
		return unionResult;
	}
	

	
   	/*
	 * create a MDD for stateArray. The nodes in the created MDD are added into the nodeTbl.
	 */	
	public boolean add(mddNode target, int[] idxArray) {					
		target = target.add(idxArray, nodeTbl, 20);

		int curNodes = 0;
		for(int i = 0; i < this.height; i++) 
			curNodes += nodeTbl[i].size();
		
		if(curNodes > peakNodes)
			peakNodes = curNodes;

		//stateCount++;
		return true;
	}	
	
	public boolean add(mddNode target, int[] idxArray, boolean sharing) {
		if(sharing==true)
			target.add(idxArray, nodeTbl, 20);
		else
			target.add(idxArray);

		return true;
	}
	
	
	public void compress(mddNode target) {
		target.compress(nodeTbl);
	}
	
	
	public mddNode doLocalFirings(StateGraph[] curLpnArray, State[] curStateArray, mddNode reachSet) {
		mddNode result = Mdd.newNode();
		@SuppressWarnings("unchecked")
		LinkedList<State>[] nextSetArray = (new LinkedList[curLpnArray.length]);
		for(int i = 0; i < curLpnArray.length; i++)
			nextSetArray[i] = new LinkedList<State>();
		
		// Do firings of local LPN transition.
		mddNode newResult = result.doLocalFirings(curLpnArray, curStateArray, nextSetArray, reachSet, nodeTbl);
		
		if(newResult == result)
			return result;
		this.remove(result);
		return newResult;
	}
	
	
	public void remove(mddNode target) {
		target.remove(nodeTbl);
		if(target.refCount<=0)
			nodeTbl[0].remove(target);
	}
	
	/*
	 * Check if there is a path in MDD that corresponds to stateArray. Return true if so.
	 */
	public static boolean contains(mddNode target, int[] idxArray) {
		if(target == null)
			return false;
		return target.contains(idxArray)==Mdd.terminal;
	}
	
	public int[] next(mddNode curNode) {
		if(curNode == null)
			return null;
		return curNode.next(height);
	}
	
	public int[] next(mddNode curNode, int[] curIdxArray) {
		if(curIdxArray == null)
			return curNode.next(height);
		return curNode.next(height, curIdxArray);
	}
	
	public static double numberOfStates(mddNode target) {
		HashSet<mddNode> uniqueNodes = new HashSet<mddNode>();
		double paths = target.pathCount(uniqueNodes);
		return paths;
	}
	
	public int nodeCnt() {
		int curNodes = 0;
		for(int i = 0; i < this.height; i++) 
			curNodes += nodeTbl[i].size();
		return curNodes;
	}
	
	public HashMap<State, HashSet<State>>[] getLocalFiringTbl() {
		return localFirings;
	}
	
	/*
	 * Returns the largest number of MDD nodes created when this MDD is live.
	 */
	public int peakNodeCnt() {
		return peakNodes;
	}
	
	public void check()
	{
		for(int i = 0; i < this.height; i++) {
			Set entries = nodeTbl[i].entrySet();
			Iterator it = entries.iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				mddNode tmp = (mddNode)entry.getKey();
				System.out.println("nodeTbl@" + i + " >>> " + tmp + ": level = " + tmp.level + ", refCount = " + tmp.refCount);
	    		  //+ ", " + tmp.nodeMapSize);
			}
		}
	}
}
