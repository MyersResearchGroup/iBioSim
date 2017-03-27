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
package main.java.edu.utah.ece.async.verification.platu.BinaryTree;

import java.util.*;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class BinaryTree {

	/* Tables for storing nodes that are not root. */
	HashMap<Node, Node> uniqueNodeTbl;
	Vector<Node> NodeIdxTbl;
	
	/* Tables for storing root nodes and their indices. */
	HashMap<Node, Node> uniqueRootNodeTbl;
	Vector<Node> rootNodeIdxTbl;
	
	/* Size restricted fast lookup */
	HashMap<Integer, int[]> lastAccessedTbl;
	LinkedList<Integer> lastAccessedIdxList;
	
	
	public BinaryTree() {
		this.uniqueNodeTbl = new HashMap<Node, Node>();
		this.NodeIdxTbl = new Vector<Node>(100);
		
		this.uniqueRootNodeTbl = new HashMap<Node, Node>();
		this.rootNodeIdxTbl = new Vector<Node>(100);
		
		lastAccessedTbl = new HashMap<Integer, int[]>();
		lastAccessedIdxList = new LinkedList<Integer>();
	}
	
	/* 
	 * Build a binary tree for IdxArray.
	 * Return the the literal of the root node. 
	 */
	public int add(int[] IdxArray) {
		if(IdxArray == null)
			return -1;
		
		//System.out.println("add " + Arrays.toString(IdxArray));
		int[] nodeLitArray = IdxArray;
		int[] attributeArray = new int[IdxArray.length];
		
		for (int i = 0; i < IdxArray.length; i++) {
			attributeArray[i] = 1;
		}
		
		while (nodeLitArray.length > 1) {
			int lastSlot = nodeLitArray[nodeLitArray.length - 1];
			int lastAttribute = attributeArray[attributeArray.length-1];
			
			boolean arrayLenOdd = true;
			if ((nodeLitArray.length & 1) == 0)
				arrayLenOdd = false;
			
			int pairCnt = nodeLitArray.length >> 1;
			
			int[] newnodeLitArray = null;
			int[] newattributeArray = null;
			
			if (arrayLenOdd == true) {
				newnodeLitArray = new int[pairCnt + 1];
				newattributeArray = new int[pairCnt + 1];
			} else {
				newnodeLitArray = new int[pairCnt];
				newattributeArray = new int[pairCnt];
			}
			
			for (int i = 0; i < pairCnt; i++) {
				Node newNode = new Node();
				newNode.setLeft(nodeLitArray[i*2]);
				newNode.setRight(nodeLitArray[i*2 + 1]);
				char terminal = 0x00;
				if(attributeArray[i*2]==1) {
					terminal = (char)(terminal | 0xF0);
				}
				if(attributeArray[i*2+1]==1) {
					terminal = (char)(terminal | 0x0F);
				}
				newNode.setTerminal(terminal);
				
				/* If nodeLitArray has more than 2 literals, newNode, which is not a root node, is added by calling addNode().
				 * Otherwise, newNode is a root node, and added by calling addRootNode(). */
				int newNodeLit = nodeLitArray.length > 2 ? this.addNode(newNode) : this.addRootNode(newNode);
				newnodeLitArray[i] = newNodeLit;
				newattributeArray[i] = 0;
			}
			
			if (arrayLenOdd == true) {
				newnodeLitArray[pairCnt] = lastSlot;
				newattributeArray[pairCnt] = lastAttribute;
			}
			
			nodeLitArray = newnodeLitArray;
			attributeArray = newattributeArray;
		}
		//System.out.println("add node lit = " + nodeLitArray[0] + "\n");
		//System.out.println("added vec = " + Arrays.toString(this.toIntArray(nodeLitArray[0])) + "\n");
		int rootLit = nodeLitArray[0];
		
		if(this.lastAccessedTbl.containsKey(rootLit) == false) {
			if(this.lastAccessedIdxList.size() > 1000000) {
				int oldestIdx = this.lastAccessedIdxList.removeFirst();
				this.lastAccessedTbl.remove(oldestIdx);
			}
			this.lastAccessedIdxList.addLast(rootLit);
			this.lastAccessedTbl.put(rootLit, IdxArray);
		}
		
		return rootLit;
	}
	
	public boolean contains(int[] IdxArray) {
		//System.out.println("check " + Arrays.toString(IdxArray));
		int[] nodeLitArray = IdxArray;
		int[] attributeArray = new int[IdxArray.length];
		
		for (int i = 0; i < IdxArray.length; i++) {
			attributeArray[i] = 1;
		}
		
		while (nodeLitArray.length > 1) {
			int lastSlot = nodeLitArray[nodeLitArray.length - 1];
			int lastAttribute = attributeArray[attributeArray.length-1];
			
			boolean arrayLenOdd = true;
			if ((nodeLitArray.length & 1) == 0)
				arrayLenOdd = false;
			
			int pairCnt = nodeLitArray.length >> 1;
			
			int[] newnodeLitArray = null;
			int[] newattributeArray = null;
			
			if (arrayLenOdd == true) {
				newnodeLitArray = new int[pairCnt + 1];
				newattributeArray = new int[pairCnt + 1];
			} else {
				newnodeLitArray = new int[pairCnt];
				newattributeArray = new int[pairCnt];
			}
			
			for (int i = 0; i < pairCnt; i++) {
				Node newNode = new Node();
				newNode.setLeft(nodeLitArray[i*2]);
				newNode.setRight(nodeLitArray[i*2 + 1]);
				char terminal = 0x00;
				if(attributeArray[i*2]==1)
					terminal = (char)(terminal | 0xF0);
				if(attributeArray[i*2+1]==1)
					terminal = (char)(terminal | 0x0F);
				newNode.setTerminal(terminal);
				int newNodeLit = pairCnt > 1 ? this.getNodeLit(newNode) : this.getRootNodeLit(newNode);
				if(newNodeLit == -1) {
					return false;
				}
				newnodeLitArray[i] = newNodeLit;
				newattributeArray[i] = 0;
			}
			
			if (arrayLenOdd == true) {
				newnodeLitArray[pairCnt] = lastSlot;
				newattributeArray[pairCnt] = lastAttribute;
			}
			
			nodeLitArray = newnodeLitArray;
			attributeArray = newattributeArray;
		}
		return nodeLitArray[0] == -1 ? false : true;
		
	}
	
	public int[] toIntArray(int nodeLit) {
		
		int[] tmp = this.lastAccessedTbl.get(nodeLit);
		if(tmp != null) 
			return tmp;
		
		boolean flipped = ((nodeLit & 1) == 1);
		
		Node rootNode = this.getRootNode(nodeLit);
		if(rootNode==null)
			return null;
		
		LinkedList<Integer> IntList = rootNode.toIntArray(this, flipped);
		
		int[] result = new int[IntList.size()];
		int pos = 0;
		while(IntList.size() > 0) {
			result[pos] = IntList.removeFirst();
			pos++;
		}
		return result;
	}
	
	private int addNode(Node node) {
		int nodeLit = this.getNodeLit(node);
		
		if (nodeLit != -1)
			return nodeLit;
		
		int idx = this.uniqueNodeTbl.size()+1;
		node.setIndex(idx);
		this.uniqueNodeTbl.put(node, node);

		if(idx >= this.NodeIdxTbl.size()) {
			this.NodeIdxTbl.setSize(this.NodeIdxTbl.size() + 100);
		}
		this.NodeIdxTbl.setElementAt(node, idx);		
		return (idx << 1);
	}
	
	private int addRootNode(Node node) {
		int nodeLit = this.getRootNodeLit(node);
		
		if (nodeLit != -1)
			return nodeLit;
		
		int idx = this.uniqueRootNodeTbl.size()+1;
		node.setIndex(idx);
		this.uniqueRootNodeTbl.put(node, node);

		if(idx >= this.rootNodeIdxTbl.size()) {
			this.rootNodeIdxTbl.setSize(this.rootNodeIdxTbl.size() + 100);
		}
		this.rootNodeIdxTbl.setElementAt(node, idx);		
		return (idx << 1);
	}
	
	public Node getNode(int nodeLit) {
		int nodeIdx = nodeLit >> 1;
		return this.NodeIdxTbl.get(nodeIdx);
	}
	
	public Node getRootNode(int nodeLit) {
		int nodeIdx = nodeLit >> 1;
		return this.rootNodeIdxTbl.get(nodeIdx);
	}
	
	/*
	 * Check if 'node' exists in the node table. If yes, return the node
	 * literal. If the node complement exists, return its literal as well.
	 * Otherwise, return -1.
	 */
	public int getNodeLit(Node node) {
		Node cachedNode = this.uniqueNodeTbl.get(node);
		
		if (cachedNode != null) {
			int idx = cachedNode.getIndex();
			return idx << 1;
		}
		
		Node flipNode = node.flip();
		Node cachedFlipNode = this.uniqueNodeTbl.get(flipNode);
		if (cachedFlipNode != null) {
			int idx = cachedFlipNode.getIndex();
			return (idx << 1) + 1;
		}
		
		return -1;
	}
	
	public int getRootNodeLit(Node node) {
		Node cachedNode = this.uniqueRootNodeTbl.get(node);
		
		if (cachedNode != null) {
			int idx = cachedNode.getIndex();
			return idx << 1;
		}
		
		Node flipNode = node.flip();
		Node cachedFlipNode = this.uniqueRootNodeTbl.get(flipNode);
		if (cachedFlipNode != null) {
			int idx = cachedFlipNode.getIndex();
			return (idx << 1) + 1;
		}
		
		return -1;
	}
	
	public int nodeCount() {
		return this.uniqueNodeTbl.size();
	}
	
	public int elementCount() {
		return this.uniqueRootNodeTbl.size();
	}
	
	@Override
	public String toString() {
		return "BinaryTree [uniqueNodeTbl=" + this.NodeIdxTbl + "]";
	}
	
}
