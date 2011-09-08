package platu.MDD;

import java.util.*;

import platu.stategraph.*;

public class mdtNode {
	//private Stack<State> localStateSet;
	private HashMap<State, mdtNode> nodeMap;
	
	mdtNode() {
		//localStateSet = new Stack<State>();
		this.nodeMap = new HashMap<State, mdtNode>();
	}

	public boolean push(State[] curIdxArray, int level) {
		State curIdx = curIdxArray[level];
		mdtNode nextNode = this.nodeMap.get(curIdx);
		if(nextNode != null) {
			if(level == curIdxArray.length-1) {
				return false;
			}		
			nextNode.push(curIdxArray, level+1);
			return true;
		}
		else {
			if(level == curIdxArray.length-1) 
				this.nodeMap.put(curIdx, MDT.terminal);
			else {
				nextNode = new mdtNode();
				nextNode.push(curIdxArray, level+1);
				this.nodeMap.put(curIdx, nextNode);
			}
			//this.localStateSet.push(curIdx);
			return true;
		}
	}
	
	public State[] pop(int level) {
		State[] prjState = null;
		//State curLocalState = localStateSet.peek();
		State curLocalState = null;
		Set<State> keySet = this.nodeMap.keySet();
		for(State st : keySet) {
			curLocalState = st;
			break;
		}
		mdtNode nextNode = this.nodeMap.get(curLocalState);
		if(nextNode == MDT.terminal) {
			prjState = new State[level+1];
			prjState[level] = curLocalState;
			//this.localStateSet.pop();
			this.nodeMap.remove(curLocalState);
		}
		else {
			prjState = nextNode.pop(level+1);
			prjState[level] = curLocalState;
			if(nextNode.empty()==true) {
				//this.localStateSet.pop();
				this.nodeMap.remove(curLocalState);
			}
		}
		
		return prjState;
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
		else {
			mdtNode nextNode = this.nodeMap.get(curIdxArray[level]);
			return nextNode.contains(curIdxArray, level+1);
		}
	}
	
	public boolean empty() {
		return (this.nodeMap.size() == 0);
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
