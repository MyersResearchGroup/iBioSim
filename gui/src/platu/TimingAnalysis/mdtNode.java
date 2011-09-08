package platu.TimingAnalysis;

import java.util.*;

import platu.stategraph.*;

public class mdtNode {
	static public int totalStates = 0;
	private HashMap<State, mdtNode> nodeMap;
	private LinkedList<DBM> dbmSet;
	
	public mdtNode() {
		this.nodeMap = null;
		this.dbmSet = null;
	}

	public boolean add(State[] stateArray, DBM dbm, int level) {
		boolean isNew = false;
		
		if(level == stateArray.length) {
			if(this.dbmSet == null) {
				this.dbmSet = new LinkedList<DBM>();
				this.dbmSet.addLast(dbm);
				isNew = true;
			}
			else {
				boolean isSubDbm = false;
				LinkedList<DBM> subDbms = new LinkedList<DBM>();
				for(DBM oldDbm : this.dbmSet) {
					if(isSubDbm==false && dbm.subset(oldDbm)==true)
						isSubDbm = true;
					else if (oldDbm.subset(dbm)==true)
						subDbms.addLast(oldDbm);
				}
				
				for (DBM subDbm : subDbms) 
					this.dbmSet.remove(subDbm);
				
				if (isSubDbm==false) {
					this.dbmSet.addLast(dbm);
					isNew = true;
				}
				else 
					isNew = false;
			}
			return isNew;
		}
		
		State curState = stateArray[level];
		
		if(this.nodeMap == null)
			this.nodeMap = new HashMap<State, mdtNode>();
			
		mdtNode nextNode = this.nodeMap.get(curState);
		if(nextNode == null) {
			nextNode = new mdtNode();
			nextNode.add(stateArray, dbm, level + 1);
			this.nodeMap.put(curState, nextNode);
			isNew = true;
		}
		else {
			isNew = nextNode.add(stateArray, dbm, level+1);
		}
		
		return isNew;
	}
	
	public DBM merge(State[] stateArray, DBM dbm, int level) {		
		if(level == stateArray.length) {
			if(this.dbmSet == null) {
				this.dbmSet = new LinkedList<DBM>();
				this.dbmSet.addLast(dbm);
				totalStates++;
				return dbm;
			}
			else {
				DBM existingDbm = this.dbmSet.removeFirst();
				DBM newDbm = dbm.merge(existingDbm);
				this.dbmSet.addLast(newDbm);
				return newDbm;
			}
		}
		
		State curState = stateArray[level];
		
		if(this.nodeMap == null)
			this.nodeMap = new HashMap<State, mdtNode>();

		int nextLevel = level+1;
		mdtNode nextNode = this.nodeMap.get(curState);
		if(nextNode == null) {
			nextNode = new mdtNode();
			DBM newDbm = nextNode.merge(stateArray, dbm, nextLevel);
			this.nodeMap.put(curState, nextNode);
			return newDbm;
		}
		else {
			return nextNode.merge(stateArray, dbm, nextLevel);
		}		
	}
	
	public DBM getDbm(State[] stateArray, int level) {
		if (level == stateArray.length)
			return this.dbmSet.getFirst();
		
		State curSt = stateArray[level];
		mdtNode nextNode = this.nodeMap.get(curSt);
		if (nextNode != null)
			return nextNode.getDbm(stateArray, level+1);
		return null;
	}
	
	public boolean contains(State[] stateArray, DBM dbm, int level) {
		if (level == stateArray.length) {
			for(DBM existingDbm : this.dbmSet) {
				System.out.println(dbm + "\n\n" + existingDbm);
				if(dbm.subset(existingDbm)==true)
					return true;
			}
			return false;
		}
		else {
			mdtNode nextNode = this.nodeMap.get(stateArray[level]);
			if (nextNode == null)
				return false;
			System.out.println("level = " + level + ": " + stateArray[level] + " ---> "  + nextNode);
			return nextNode.contains(stateArray, dbm, level+1);
		}			 
	}
	
	public int pathCount() {
		if (this.nodeMap != null) {
			int count = 0;
			Set<State> stateSet = this.nodeMap.keySet();
			for (State curState : stateSet) {
				mdtNode nextNode = this.nodeMap.get(curState);
				count += nextNode.pathCount();
			}
			return count;
		}
		
		return this.dbmSet.size();
	}
}

