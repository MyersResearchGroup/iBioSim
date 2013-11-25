package verification.platu.MDD;

import java.util.*;

import lpn.parser.Transition;

import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

public class mddNode {
	static int blockSize = 8;
	static int numBlocks = 16;
	static int blockIdxMask = 15; 
	static int arrayIdxoffset = 4;
	
	int level;
	private mddNode[][] nodeMap;
	private int[] blkHashVal;
	int refCount;
	int hashVal;
	int nodeMapSize;
	int maxArrayBound;
	
	public mddNode() {
		this.reset();
	}
	
	public mddNode(int thisLevel) {
		this.reset();
		level = thisLevel;
	}
	
	private void reset() {
		level = -1;
		nodeMap = new mddNode[numBlocks][blockSize];
		this.blkHashVal = new int[numBlocks];
		for(int i = 0; i < numBlocks; i++){
			this.nodeMap[i] = null;
			this.blkHashVal[i] = 0;
		}
		refCount = 0;
		hashVal = 0;
		nodeMapSize = 0;
		maxArrayBound = -1;
	}

	
	private mddNode split() {
		mddNode copy = new mddNode();
		copy.level = this.level;
		for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
			if(this.nodeMap[blkIter] == null) 
				continue;
			for(int arrayIter = 0; arrayIter < this.nodeMap[blkIter].length; arrayIter++) {
				mddNode succ = this.getSucc(blkIter, arrayIter);
				if(succ != null)
					copy.addSucc(blkIter, arrayIter, succ);
			}
			copy.blkHashVal[blkIter] = this.blkHashVal[blkIter];
		}
		
		copy.refCount = 0;
		copy.hashVal = this.hashVal;
		return copy;
	}
	
	/**
	 * Add an integer tuple into MDD root at this node. 
	 * Isomorphic nodes are not shared.
	 * 
	 * @param idxArray
	 * @return null if input idxArray is added into the MDD, root node otherwise.
	 */
	public mddNode add(int[] idxArray) {	
		if(this.level == -1) {
			System.out.println("level is not right 1");
			System.exit(0);
		}
		
		int curIdx = idxArray[this.level];
		int stateIdx = curIdx;
		int blockIdx = stateIdx & mddNode.blockIdxMask;
		int arrayIdx = stateIdx >> mddNode.arrayIdxoffset;
		
		mddNode nextNode = this.getSucc(blockIdx, arrayIdx);

		if(nextNode == null) {
			if(this.level == idxArray.length-1) {
				this.addSucc(blockIdx, arrayIdx, Mdd.terminal);				
				this.blkHashVal[blockIdx] = Integer.rotateLeft(Arrays.hashCode(this.nodeMap[blockIdx]), this.level);
				this.hashVal = 0;		
				return null;
			}
			nextNode = new mddNode();
			nextNode.level = this.level + 1;
			nextNode.add(idxArray);
			this.addSucc(blockIdx, arrayIdx, nextNode);
			this.blkHashVal[blockIdx] = Integer.rotateLeft(Arrays.hashCode(this.nodeMap[blockIdx]), this.level);
			this.hashVal = 0;
			
			return null;			
		}
		if(nextNode == Mdd.terminal)
			return this;
		else if(nextNode.add(idxArray)==null)
			return null;
		return this;
	}
	
	public mddNode add(int[] idxArray, HashMap<mddNode, mddNode>[] nodeTbl, int shrLevel) {	
		if(this.level == -1) {
			System.out.println("level is not right 1");
			System.exit(0);
		}
		
		int curIdx = idxArray[this.level];
		int stateIdx = curIdx;
		int blockIdx = stateIdx & mddNode.blockIdxMask;
		int arrayIdx = stateIdx >> mddNode.arrayIdxoffset;
		
		mddNode nextNode = this.getSucc(blockIdx, arrayIdx);

		if(nextNode == null) {
			if(this.level == idxArray.length-1) {
				nodeTbl[this.level].remove(this);
				this.addSucc(blockIdx, arrayIdx, Mdd.terminal);				
				this.blkHashVal[blockIdx] = Integer.rotateLeft(Arrays.hashCode(this.nodeMap[blockIdx]), this.level);
				this.hashVal = 0;
								
				mddNode newThisNode = nodeTbl[this.level].get(this);  
				if(newThisNode != null) {
					return newThisNode;
				}
				
				nodeTbl[this.level].put(this, this);
				return this;
			}
			nextNode = new mddNode();
			nextNode.level = this.level + 1;
			
			mddNode newNextNode = nextNode.add(idxArray, nodeTbl, shrLevel);				
			if(newNextNode != nextNode) 
				nextNode = newNextNode;
			nextNode.refCount++;	
			
			if(level < shrLevel) 
				nodeTbl[this.level].remove(this);
			
			this.addSucc(blockIdx, arrayIdx, nextNode);
			this.blkHashVal[blockIdx] = Integer.rotateLeft(Arrays.hashCode(this.nodeMap[blockIdx]), this.level);
			this.hashVal = 0;
				
			if(level < shrLevel) {
			mddNode newThis = nodeTbl[this.level].get(this);
			if (newThis != null) {		
				for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
					if(this.nodeMap[blkIter] == null)
						continue;
					for(int arrayIter = 0; arrayIter < this.nodeMap[blkIter].length; arrayIter++)
						if(this.nodeMap[blkIter][arrayIter] != null)
							this.nodeMap[blkIter][arrayIter].refCount--;
				} 
			    
				return newThis;
			}
			
			nodeTbl[this.level].put(this, this);
			}
			return this;			
		}
		else if(nextNode == Mdd.terminal) {
			//System.out.println("mddNode: should not reach here. Abort!");
			//System.exit(0);
			return Mdd.terminal;
		}
		else {
			mddNode newNextNode = nextNode;
			if(nextNode.refCount > 1) {
				newNextNode = nextNode.split();//new mddNode(nextNode);
				nextNode.refCount--;
//				for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
//					if(nextNode.nodeMap[blkIter] == null)
//						continue;
//				
//					for(int arrayIter = 0; arrayIter < nextNode.nodeMap[blkIter].length; arrayIter++)
//						if(nextNode.nodeMap[blkIter][arrayIter] != null)
//							nextNode.nodeMap[blkIter][arrayIter].refCount++;
//				}
						
			    mddNode newNextNode_1 = newNextNode.add(idxArray, nodeTbl, shrLevel);
			
			    if (newNextNode_1 != newNextNode) 
			    	newNextNode = newNextNode_1;
			}
			else {
				newNextNode = nextNode.add(idxArray, nodeTbl, shrLevel);

				// if no next node is splitted and the next node does not have equivalent, 
				// do nothing further.
				if(newNextNode == nextNode)
					return this;
				
				nextNode.refCount--;
				for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
					if(nextNode.nodeMap[blkIter] == null)
						continue;
					
					for(int arrayIter = 0; arrayIter < nextNode.nodeMap[blkIter].length; arrayIter++)
						if(nextNode.nodeMap[blkIter][arrayIter] != null)
							nextNode.nodeMap[blkIter][arrayIter].refCount++;
				}
			}
			    
			    if(level < shrLevel)
			    	nodeTbl[this.level].remove(this);
			    
			    newNextNode.refCount++;
				this.addSucc(blockIdx, arrayIdx, newNextNode);
				this.blkHashVal[blockIdx] = Integer.rotateLeft(Arrays.hashCode(this.nodeMap[blockIdx]), this.level);
				this.hashVal = 0;
					
				if(level < shrLevel) {
				mddNode newThis = nodeTbl[this.level].get(this);
				if (newThis != null) {	
					for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
						if(this.nodeMap[blkIter] == null)
							continue;
					
						for(int arrayIter = 0; arrayIter < this.nodeMap[blkIter].length; arrayIter++)
							if(this.nodeMap[blkIter][arrayIter] != null)
								this.nodeMap[blkIter][arrayIter].refCount--;
					}
					    
					return newThis;
				}				
			
				nodeTbl[this.level].put(this, this);
				}
				return this;
			}
	}	
	
	public static int numCalls = 0;
	public static int cacheNodes = 0;
	public static int splits_level1 = 0;
	public static int splits_level2 = 0;
	public static int splits_level3 = 0;

	public mddNode union(final mddNode other, HashMap<mddNode, mddNode>[] nodeTbl, HashMap<mddNode, HashMap<mddNode, mddNode>> unionCache) {
		numCalls++;
		
		mddNode thisCopy = this.split();
		
		for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
			if(other.nodeMap[blkIter] == null)
				continue;
			
			for(int arrayIter = 0; arrayIter < other.nodeMap[blkIter].length; arrayIter++) {
				mddNode thisSucc = this.getSucc(blkIter, arrayIter);
				mddNode otherSucc = other.getSucc(blkIter, arrayIter);
				
				if(otherSucc == null)
					continue;
				
				if(thisSucc == null) {
					thisCopy.addSucc(blkIter, arrayIter, otherSucc);
					continue;
				}
				
				// When successors are terminals, return. 
				if(thisSucc == Mdd.terminal || otherSucc == Mdd.terminal)
					continue;
				
				if(thisSucc==otherSucc || otherSucc.subSet(thisSucc) == true)
					continue;
				
				mddNode succCached = null;
				HashMap<mddNode, mddNode> second = unionCache.get(thisSucc);
				if(second != null) {
					succCached = second.get(otherSucc);
				}
			
				if(succCached == null) {
					mddNode succUnion = thisSucc.union(otherSucc, nodeTbl, unionCache);
					
					// Add newSucc into the cache to avoid call union(thisSuccOriginal, otherSucc) again.		
					HashMap<mddNode, mddNode> secondCache = unionCache.get(thisSucc);
					cacheNodes++;
					if(secondCache == null) {
						secondCache = new HashMap<mddNode, mddNode>();
						secondCache.put(otherSucc, succUnion);
						unionCache.put(thisSucc, secondCache);
					}
					else
						secondCache.put(otherSucc, succUnion);
					
					succCached = succUnion;
				}
				thisSucc.remove(nodeTbl);
				thisCopy.addSucc(blkIter, arrayIter, succCached);
			}
		}
		
		mddNode result = nodeTbl[thisCopy.level].get(thisCopy);
		if(result == null) {
			nodeTbl[thisCopy.level].put(thisCopy, thisCopy);
			return thisCopy;
		}
		thisCopy.remove(nodeTbl);
		return result;
	}
	
	
	public mddNode compress(HashMap<mddNode, mddNode>[] nodeTbl) {
		int mddHeight = nodeTbl.length;
				
		if(this.level < mddHeight-1) {	
			for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
				if(this.nodeMap[blkIter] == null)
					continue;
				for(int arrayIter = 0; arrayIter < this.nodeMap[blkIter].length; arrayIter++) {
					mddNode nextNode = this.nodeMap[blkIter][arrayIter];
					if(nextNode == null)
						continue;

					mddNode newNextNode = nextNode.compress(nodeTbl);
					if(newNextNode != nextNode) {
						this.addSucc(blkIter, arrayIter, newNextNode);
					}
				}
			}
		}
		mddNode newThis = nodeTbl[this.level].get(this);
		if(newThis == null) {
			nodeTbl[this.level].put(this, this);
			return this;
		}
		else if(newThis != this) 
			return newThis;

		return this;
	}

	/*
	 * Recursively remove this nodes and its successor nodes from nodeTbl if their reference
	 * count is 0.
	 */
	public void remove(HashMap<mddNode, mddNode>[] nodeTbl) {
		int mddHeight = nodeTbl.length;
		
		this.refCount--;

		if(this.refCount > 0)
			return;
		
		if(this.level < mddHeight) {
			if(this.level < mddHeight - 1) {
				for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
					if(this.nodeMap[blkIter] == null)
						continue;
					for(int arrayIter = 0; arrayIter < this.nodeMap[blkIter].length; arrayIter++) {
						mddNode thisSucc = this.getSucc(blkIter, arrayIter);
						if(thisSucc == null)
							continue;
						thisSucc.remove(nodeTbl);
					}
				}
			}
			nodeTbl[this.level].remove(this);
		}
	}
	

	
	/**
	 * Check if stateArray already exists in MDD.
	 * @param stateArray
	 * @param index
	 * @param terminal
	 * @return true if stateArray exists in MDD, false otherwise.
	 */
	public mddNode contains(int[] idxArray) {	
		if(this.level == -1) {
			System.out.println("level is not right 2");
			System.exit(0);
		}
		
		int curIdx = idxArray[this.level];
		int blockIdx = curIdx & mddNode.blockIdxMask;
		int arrayIdx = curIdx >> mddNode.arrayIdxoffset;

		mddNode nextNode = this.getSucc(blockIdx, arrayIdx);
		
		if (nextNode == null)
			return null;
		
		if (nextNode == Mdd.terminal)
			return Mdd.terminal;

		return nextNode.contains(idxArray);
	}	
	
	/*
	 * Exhaustively fire all local LPN transitions from each local state in curStateArray 
	 */
	public mddNode doLocalFirings(StateGraph[] curLpnArray, State[] curStateArray,
			LinkedList<State>[] nextSetArray,
			mddNode reachSet,
			HashMap<mddNode, mddNode>[] nodeTbl) {
		int curBlkIdx = curStateArray[this.level].getIndex() & mddNode.blockIdxMask;
		int curArrayIdx = curStateArray[this.level].getIndex() >> mddNode.arrayIdxoffset;
							
		mddNode succ = Mdd.terminal;
		if(this.level == curLpnArray.length - 1) {
			this.addSucc(curBlkIdx, curArrayIdx, succ);
		}
		else if(this.level < curLpnArray.length) {
			succ = new mddNode();
			succ.level = this.level + 1;
			mddNode newSucc = succ.doLocalFirings(curLpnArray, curStateArray, nextSetArray, reachSet, nodeTbl);
			this.addSucc(curBlkIdx, curArrayIdx, newSucc);
			succ = newSucc;
		}
		
		/*
		 * Do exhaustive local firings at this level.
		 */
		StateGraph curLpn = curLpnArray[this.level];
		State curState = curStateArray[this.level];
//		HashMap<State, State> curLocalStateSet = localStateSets[this.level];
		nextSetArray[this.level].addLast(curState); 

		LinkedList<Transition> curEnabled = curLpn.getEnabled(curState);
		LinkedList<Transition> localTranSet = new LinkedList<Transition>();
		if(curEnabled != null) {
			for(Transition firedTran : curEnabled) 
				if(firedTran.isLocal()==true) 
					localTranSet.addLast(firedTran);
		}
		
		if(localTranSet.size() == 0) {
			mddNode newThis = nodeTbl[this.level].get(this);
			if(newThis == null) {
				nodeTbl[this.level].put(this, this);
				return this;
			}
			this.remove(nodeTbl);
			return newThis;
		}
		
		HashSet<State> curLocalNewStates = new HashSet<State>();
		Stack<State> stateStack = new Stack<State>();
		Stack<LinkedList<Transition>> enabledStack = new Stack<LinkedList<Transition>>();
		
		stateStack.push(curState);
		enabledStack.push(localTranSet);
		//LinkedList<State[]> nextSet_tmp = new LinkedList<State[]>();

		while(stateStack.size() != 0) {
			curState = stateStack.pop();
			LinkedList<Transition> curLocalEnabled = enabledStack.pop();
			for(Transition tran2fire : curLocalEnabled) {
				System.out.println("tran2fire = " + tran2fire.getLabel() + " in  curlocalState = " + curState.getLabel());
				// TODO: Need to fix this.
				State nextState = null; //tran2fire.fire(curLpnArray[tran2fire.getLpn().getIndex()], curState); 
				
//				System.out.println("1 nextLocalState = " + nextState.getLabel());
				if(curLocalNewStates.contains(nextState) == true)
					continue;

//				System.out.println("2 nextLocalState = " + nextState.getLabel());

				curEnabled = curLpn.getEnabled(curState);
				LinkedList<Transition> nextEnabled = curLpn.getEnabled(nextState);
				
				// TODO: Need to fix this.
				//Transition disabledTran = null; //tran2fire.disablingError(curEnabled, nextEnabled);
				/*
				if(disabledTran != null) {
					System.err.println("Verification failed: disabling error: " 
							+ disabledTran.getFullLabel()  + " is disabled by "
									+ tran2fire.getFullLabel() + "!");
					System.exit(0);
				} 
				*/
				
				//System.out.println("addlocal nextLocalState = " + nextState.getLabel());
				curLocalNewStates.add(nextState);
				
				// TODO: had to remove because nextState is null
				//int nextBlkIdx = nextState.getIndex() & mddNode.blockIdxMask;
				//int nextArrayIdx = nextState.getIndex() >> mddNode.arrayIdxoffset;				
				//this.addSucc(nextBlkIdx, nextArrayIdx, succ);

				LinkedList<Transition> nextLocalEnabled = new LinkedList<Transition>();
				//boolean nonLocalNext = false;
				for(Transition tran : nextEnabled) {
					if(tran.isLocal()==true)
						nextLocalEnabled.addLast(tran);
					//else
						//nonLocalNext = true;
				}
				
				// TODO: had to remove because nextState is null
				//nextState.hasNonLocalEnabled(nonLocalNext);
				//nextSetArray[this.level].addLast(nextState);
				
				if(nextLocalEnabled.size() == 0)
					continue;				
				
				stateStack.push(nextState);
				enabledStack.push(nextLocalEnabled);
				//System.out.println("added state " + nextState.getLabel() + " into localNewStateSet");
				
//				for(int i = 0; i < curStateArray.length; i++)
//					System.out.print(newNextStateArray[i].getLabel()+", ");
//				System.out.println("\n#####################");
			}
		}

		mddNode newThis = nodeTbl[this.level].get(this);
		if(newThis == null) {
			nodeTbl[this.level].put(this, this);
			return this;
		}
		this.remove(nodeTbl);
		return newThis;
	}

	/*
	 * Return the successor node with index exists in nodeMap.
	 */
	private mddNode getSucc(int blockIdx, int arrayIdx) {
		if(this.nodeMap[blockIdx]==null || arrayIdx >= this.nodeMap[blockIdx].length)
			return null;
		
		return this.nodeMap[blockIdx][arrayIdx];
	}
	
	/*
	 * Insert a succNode with index into the nodeMap, whose size is automatically adjusted.
	 */
	private boolean addSucc(int blockIdx, int arrayIdx, mddNode succNode) {
		//int oldsize = nodeMap.length;
		
		//boolean newState = false;
		if(this.nodeMap[blockIdx]==null || arrayIdx >= this.nodeMap[blockIdx].length) {
			this.resizeNodeMap(blockIdx, arrayIdx);
			//newState = true;
			//System.out.println(this + " >>> node level = " + level + "  state label = " + index + "  " + oldsize + "  " + this.nodeMap.length);
		}
		
		this.nodeMap[blockIdx][arrayIdx] = succNode;
		succNode.refCount++;
		this.blkHashVal[blockIdx] = Integer.rotateLeft(Arrays.hashCode(this.nodeMap[blockIdx]), this.level);
		this.hashVal = 0;
		nodeMapSize++;
		return true;
	}
	
	/*
	 * Resize the nodeMap so that an element with 'index' can be inserted into the nodeMap.
	 */
	private void resizeNodeMap(int blockIdx, int arrayIdx) {
		if(this.nodeMap[blockIdx] == null) {
                    int newBlockSize = (arrayIdx / mddNode.blockSize + 1) * mddNode.blockSize;
                try {
                    this.nodeMap[blockIdx] = new mddNode[newBlockSize];
                    for (int i = 0; i < newBlockSize; i++) {
                        this.nodeMap[blockIdx][i] = null;
                    }
                } catch (Exception e) {
                    String errorMessage = String.format(
                            "blockIdx=%s, arrayIdx=%s, newBlockSize=%s\n",
                           blockIdx, arrayIdx,newBlockSize);
                    throw new RuntimeException(errorMessage, e);
                }
            }
		else if(arrayIdx >= this.nodeMap[blockIdx].length) {
			int newBlockSize = (arrayIdx / mddNode.blockSize + 1) * mddNode.blockSize;
			mddNode[] newBlock = new mddNode[newBlockSize];
			for(int i = 0; i < newBlock.length; i++) {
				if(i < this.nodeMap[blockIdx].length)
					newBlock[i] = this.nodeMap[blockIdx][i];
				else
					newBlock[i] = null;
			}
		
			this.nodeMap[blockIdx] = newBlock;
		}
	}
	
	@Override
	public boolean equals(Object other) {
		mddNode otherNode = (mddNode)other;
		if(level != otherNode.level)
			return false;
		
		for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
			if(this.nodeMap[blkIter] == null && otherNode.nodeMap[blkIter] == null)
				continue;
			
			if(this.nodeMap[blkIter] == null && otherNode.nodeMap[blkIter] != null)
				return false;
			
			if(this.nodeMap[blkIter] != null && otherNode.nodeMap[blkIter] == null)
				return false;
		
			if(this.nodeMap[blkIter].length != otherNode.nodeMap[blkIter].length)
				return false;
			
			for(int arrayIter = 0; arrayIter < this.nodeMap[blkIter].length; arrayIter++)
				if(this.nodeMap[blkIter][arrayIter] != otherNode.nodeMap[blkIter][arrayIter])
					return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		if(hashVal == 0)
			hashVal = Arrays.hashCode(this.blkHashVal);

		
		return hashVal;
	}
	
	public boolean subSet(mddNode other) {
		for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
			if(other.nodeMap[blkIter] == null && this.nodeMap[blkIter] != null)
				return false;
			if(this.nodeMap[blkIter] == null)
				continue;
			if(this.nodeMap[blkIter].length > other.nodeMap[blkIter].length)
				return false;
			for(int arrayIter = 0; arrayIter < this.nodeMap[blkIter].length; arrayIter++) {
				mddNode thisSucc = this.getSucc(blkIter, arrayIter);
				mddNode otherSucc = other.getSucc(blkIter, arrayIter);
				if(thisSucc != null && otherSucc == null)
					return false;
				if(thisSucc != otherSucc)
					return false;
			}
		}
		return true;
	}
	
	public int[] next(int mddHeight) {
		if(maxArrayBound == -1) { 
			for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++)
				if(this.nodeMap[blkIter] != null && this.nodeMap[blkIter].length > maxArrayBound)
					maxArrayBound = this.nodeMap[blkIter].length;
		}
		
		for(int arrayIter = 0; arrayIter < maxArrayBound; arrayIter++) {
			for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
				mddNode thisSucc = this.getSucc(blkIter, arrayIter);
				if(thisSucc == null) 
					continue;
				
				if(this.level == mddHeight-1) {
					int stateIdx = (arrayIter << mddNode.arrayIdxoffset) | blkIter;
					int[] result = new int[mddHeight];
					result[this.level] = stateIdx;
					return result;
				}
							
				int[] tmp = thisSucc.next(mddHeight);
				if(tmp == null)
					continue;
							
				int stateIdx = (arrayIter << mddNode.arrayIdxoffset) | blkIter;
				tmp[this.level] = stateIdx;
				return tmp;
			}
		}
		return null;
	}
	
	public int[] next(int mddHeight, int[] curIdxArray) {
		int curIdx = curIdxArray[this.level];
			
		// Find the largest array bound.
		if(maxArrayBound == -1) { 	
			for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++)
				if(this.nodeMap[blkIter] != null && this.nodeMap[blkIter].length > maxArrayBound)
					maxArrayBound = this.nodeMap[blkIter].length;
		}
		
		if(this.level == mddHeight-1) {
			int newIdx = curIdx + 1;
			int newBlkIdx = newIdx & mddNode.blockIdxMask;
			int newArrayIdx = newIdx >> mddNode.arrayIdxoffset;
			
			for(int arrayIter = newArrayIdx; arrayIter < maxArrayBound; arrayIter++) {
				int startingBlkIdx = (arrayIter > newArrayIdx) ? 0 : newBlkIdx; 
				for(int blkIter = startingBlkIdx; blkIter < mddNode.numBlocks; blkIter++) {
//					if(this.nodeMap[blkIter] == null)
//						continue;
					mddNode thisSucc = this.getSucc(blkIter, arrayIter);
					if(thisSucc == null) 
						continue;
					
					int stateIdx = (arrayIter << mddNode.arrayIdxoffset) | blkIter;
					int[] result = new int[mddHeight];
					result[this.level] = stateIdx;
					return result;
				}
			}
			return null;
		}
		int curBlkIdx = curIdx & mddNode.blockIdxMask;
		int curArrayIdx = curIdx >> mddNode.arrayIdxoffset;
		mddNode thisSucc = this.getSucc(curBlkIdx, curArrayIdx);
		int[] tmp = thisSucc.next(mddHeight, curIdxArray);
		if(tmp != null) {
			tmp[this.level] = curIdx;
			return tmp;
		}
		
		int newIdx = curIdx + 1;
		int newBlkIdx = newIdx & mddNode.blockIdxMask;
		int newArrayIdx = newIdx >> mddNode.arrayIdxoffset;
		
		for(int arrayIter = newArrayIdx; arrayIter < maxArrayBound; arrayIter++) {
			int startingBlkIdx = (arrayIter > newArrayIdx) ? 0 : newBlkIdx; 
			for(int blkIter = startingBlkIdx; blkIter < mddNode.numBlocks; blkIter++) {
//						if(this.nodeMap[blkIter] == null)
//							continue;
				thisSucc = this.getSucc(blkIter, arrayIter);
				if(thisSucc == null) 
					continue;
					
				int stateIdx = (arrayIter << mddNode.arrayIdxoffset) | blkIter;
				int[] result = thisSucc.next(mddHeight);
				result[this.level] = stateIdx;
				return result;
			}
		}
		return null;
	}
	
	
	public void increaseRefCnt() {
		refCount++;
	}
	
	public void decreaseRefCnt() {
		if(refCount == 1) {
			System.out.println("Cannot decrease the ref count of 1");
			System.exit(0);
		}
		refCount--;
	}
	
	public int getRefCount() {
		return refCount;
	}
	
	public int getSuccSize() {
		return nodeMap.length;
	}
	
	public double pathCount(HashSet<mddNode> uniqueNodes) {
		uniqueNodes.add(this);
		double paths = 0.0;
		
		if(this == Mdd.terminal)
			return paths;
		
		for(int blkIter = 0; blkIter != mddNode.numBlocks; blkIter++) {
			if(this.nodeMap[blkIter] == null)
				continue;
		
			for(int arrayIter = 0; arrayIter < this.nodeMap[blkIter].length; arrayIter++) {
				if(this.nodeMap[blkIter][arrayIter] == null)
					continue;
				
				if(this.nodeMap[blkIter][arrayIter] == Mdd.terminal)
					paths += 1;
				else
					paths += nodeMap[blkIter][arrayIter].pathCount(uniqueNodes);
			}
		}
		return paths;
	}
	
	public void print() {
		if(nodeMap.length == 0)
			return;
		
		for(int blkIter = 0; blkIter < mddNode.numBlocks; blkIter++) {
			if(this.nodeMap[blkIter] == null)
				continue;

			for(int arrayIter = 0; arrayIter < nodeMap[blkIter].length; arrayIter++) {
				if(this.nodeMap[blkIter][arrayIter] == null)
					continue;
				System.out.println(this + " level = " + level + ",  " + (blkIter + (arrayIter << mddNode.arrayIdxoffset)) + " -> " + nodeMap[blkIter][arrayIter] + "  refCount = " + nodeMap[blkIter][arrayIter].refCount);
				nodeMap[blkIter][arrayIter].print();
			}
		}
	}
}
