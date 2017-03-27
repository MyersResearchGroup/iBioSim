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
public class Node {

	int LeftLit;

	int RightLit;

	int index;

	char terminal;

	public Node() {
		this.LeftLit = -1;
		this.RightLit = -1;
		this.index = -1;
		this.terminal = 0x11;
	}

	public void setLeft(int lit) {
		this.LeftLit = lit;
	}

	public void setRight(int lit) {
		this.RightLit = lit;
	}

	public int getLeft() {
		return this.LeftLit;
	}

	public int getRight() {
		return this.RightLit;
	}

	public void setIndex(int idx) {
		this.index = idx;
	}

	public int getIndex() {
		return this.index;
	}

	public void setTerminal(char terminal) {
		this.terminal = terminal;
	}

	public Node flip() {
		Node flipNode = new Node();
		flipNode.LeftLit = this.RightLit;
		flipNode.RightLit = this.LeftLit;
		if(this.terminal==0xF0) 
			flipNode.terminal = 0x0F;
		else if(this.terminal==0x0F)
			flipNode.terminal = 0xF0;
		else if(this.terminal!=0xFF && this.terminal!=0x00) {
			System.out.println("*** Wrong value for binary tree Node.terminal " + this.terminal);
			System.exit(0);
		}
		return flipNode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + LeftLit;
		result = prime * result + RightLit;
		result = prime * result + terminal;
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
		Node other = (Node) obj;
		if (LeftLit != other.LeftLit)
			return false;
		if (RightLit != other.RightLit)
			return false;
		if (terminal != other.terminal)
			return false;
		return true;
	}

	public char isTerminal() {
		return this.terminal;
	}

	public LinkedList<Integer> toIntArray(BinaryTree tree, boolean flipped) {
		int lLit = this.LeftLit;
		int rLit = this.RightLit;
		if(flipped==true) {
			lLit = this.RightLit;
			rLit = this.LeftLit;
		}

		LinkedList<Integer> lIntList = null;
		LinkedList<Integer> rIntList = null;
		if(this.terminal != 0xFF) {
			if((this.terminal & 0xF0) == 0x00) { // left is not a terminal
				Node leftNode = tree.getNode(lLit);
				boolean leftFlipped = ((lLit & 1) == 1);
				lIntList = leftNode.toIntArray(tree, leftFlipped);
				if(this.terminal==0x0F) {
					lIntList.addLast(rLit);
					return lIntList;
				}
			}
				
			if((this.terminal & 0x0F) == 0x00) { // right is not a terminal
				Node rightNode = tree.getNode(rLit);
				boolean rightFlipped = ((rLit & 1) == 1);
				rIntList = rightNode.toIntArray(tree, rightFlipped);
				if(this.terminal==0xF0) {
					rIntList.addLast(lLit);
					return rIntList;
				}
			}
			
			// Case: this.terminal==0x00
			while(rIntList!=null && lIntList != null && rIntList.size() > 0) {
				int number = rIntList.removeFirst();
				lIntList.addLast(number);
			}
			return lIntList;
		}
		LinkedList<Integer> result = new LinkedList<Integer>();
		result.addLast(lLit);
		result.addLast(rLit);
		return result;
	}



	@Override
	public String toString() {
		String terminalStr = "";
		if(this.terminal==0xF0) 
			terminalStr = "0xF0";
		else if(this.terminal==0x0F)
			terminalStr = "0x0F";
		else if(this.terminal==0xFF)
			terminalStr = "0xFF";
		else if(this.terminal==0x00)
			terminalStr = "0x00";
		else
			terminalStr = "xxxx";

		return "Node [Left=" + LeftLit + ", Right=" + RightLit + ", index=" + index + ", terminal=" + terminalStr + "]";
	}
}
