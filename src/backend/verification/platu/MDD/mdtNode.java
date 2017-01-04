package backend.verification.platu.MDD;

import java.util.*;

import backend.verification.platu.stategraph.*;

public class mdtNode {
	//private Stack<State> localStateSet;
	int level;
	private HashMap<State, mdtNode> nodeMap;
	
	mdtNode() {
		//localStateSet = new Stack<State>();
		this.level = 0;
		this.nodeMap = new HashMap<State, mdtNode>();
	}

	public boolean push(State[] curIdxArray, int level) {
		State curIdx = curIdxArray[this.level];
		mdtNode nextNode = this.nodeMap.get(curIdx);
		if(nextNode != null) {
			if(this.level == curIdxArray.length-1) {
				return false;
			}		
			nextNode.push(curIdxArray, level+1);
			return true;
		}
		if(this.level == curIdxArray.length-1) 
			this.nodeMap.put(curIdx, MDT.terminal);
		else {
			nextNode = new mdtNode();
			nextNode.level = this.level + 1;
			nextNode.push(curIdxArray, level+1);
			this.nodeMap.put(curIdx, nextNode);
		}
		//this.localStateSet.push(curIdx);
		return true;
	}
	
	public State[] pop(State[] prjState) {
		//State[] prjState = null;
		//State curLocalState = localStateSet.peek();
		State curLocalState = null;
		Set<State> keySet = this.nodeMap.keySet();
		
		for(State st : keySet) {
			curLocalState = st;
			break;
		}
		prjState[this.level] = curLocalState;
		mdtNode nextNode = this.nodeMap.get(curLocalState);
		if(nextNode == MDT.terminal) {
			prjState[level] = curLocalState;
			this.nodeMap.remove(curLocalState);
		}
		else {
			prjState = nextNode.pop(prjState);
			if(nextNode.empty()==true) {
				this.nodeMap.remove(curLocalState);
			}
		}
		
		return prjState;
	}
	
	public Stack<State[]> popList(State[] prjState) {
		//State[] prjState = null;
		//State curLocalState = localStateSet.peek();
		State curLocalState = null;
		Set<State> keySet = this.nodeMap.keySet();
		Stack<State[]> stateArrayList = null;
		
		if(this.level == prjState.length-1) {
			Stack<State[]> prjStateList = new Stack<State[]>();
			for(State st : keySet) {
				State[] stateArray = prjState.clone();
				stateArray[this.level] = st;
				prjStateList.push(stateArray);
			}
			this.nodeMap = null;
			return prjStateList;
		}
		
		for(State st : keySet) {
			curLocalState = st;
			break;
		}
		prjState[this.level] = curLocalState;
		mdtNode nextNode = this.nodeMap.get(curLocalState);
		if(nextNode == MDT.terminal) {
			//Stack<State[]> results = new Stack<State[]>();
			//prjState = new State[level+1];
			//prjState[level] = curLocalState;
			//this.localStateSet.pop();
			this.nodeMap.remove(curLocalState);
		}
		else {
			stateArrayList = nextNode.popList(prjState);
			//prjState[level] = curLocalState;
			if(nextNode.empty()==true) {
				//this.localStateSet.pop();
				this.nodeMap.remove(curLocalState);
			}
		}
		
		return stateArrayList;
	}
	
//	public State[] peek(int level) {
//		State[] prjState = null;
//		State curLocalState = this.localStateSet.peek();
//		mdtNode nextNode = this.nodeMap.get(curLocalState);
//		if(nextNode == MDT.terminal) {
//			prjState = new State[level+1];
//		}
//		else {
//			prjState = nextNode.peek(level+1);
//		}
//		
//		prjState[level] = curLocalState;
//		return prjState;
//	}
	
	public boolean contains(State[] curIdxArray, int level) {
//		if(this.localStateSet.search(curIdxArray[level])==-1)
//			return false;
		
		if(this.nodeMap.get(curIdxArray[level]) == null)
			return false;
		
		if(level == curIdxArray.length-1)
				return true;
		mdtNode nextNode = this.nodeMap.get(curIdxArray[level]);
		return nextNode.contains(curIdxArray, level+1);
	}
	
	public boolean empty() {
		return (this.nodeMap==null || this.nodeMap.size() == 0);
	}
	
	public int nodeCnt() {
		if(this.nodeMap == null) 
			return 1;
		
		int totalChildren = 0;
		Set<State> keySet = this.nodeMap.keySet();
		for(State st : keySet) 
			totalChildren += this.nodeMap.get(st).nodeCnt();
		
		return totalChildren + 1;
	}
	
}
