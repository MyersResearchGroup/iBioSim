package backend.verification.platu.MDD;

import java.util.*;

import backend.verification.platu.stategraph.*;

/*
 * This data structure cannot be used as stack as it allows node sharing.
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
