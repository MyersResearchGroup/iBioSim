package backend.verification.platu.TimingAnalysis;

import java.util.*;

import backend.verification.platu.platuLpn.*;
import dataModels.lpn.parser.Transition;

public class Zone1 {
	public static final int INFINITY = backend.verification.platu.common.Common.INFINITY;
	
	public static HashMap<Zone1, Zone1> uniqueCache = null;
	public static HashMap<DualHashMap, DualHashMap> enabledArrayCache = null;
    //public static HashMap<DBM, DBM> dbmCache = null;
    public static HashMap<LinkedList<Integer>, LinkedList<Integer>> timeSepTbl = null;

	DualHashMap<Transition, Integer> enabledSet;
	private DBM dbm;
	//HashMap<LinkedList<LPNTran>, Integer> timeSep;

	public Zone1() {
		if(Zone1.uniqueCache==null)
			Zone1.uniqueCache = new HashMap<Zone1, Zone1>();
		if(Zone1.enabledArrayCache==null)
			Zone1.enabledArrayCache = new HashMap<DualHashMap, DualHashMap>();
		if(Zone1.timeSepTbl==null)
			Zone1.timeSepTbl = new HashMap<LinkedList<Integer>, LinkedList<Integer>>();
		
		enabledSet = null;
		dbm = null;
		//timeSep = null;
	}
	
	public Zone1(Object other) {
		Zone1 otherZone = (Zone1)other;
		enabledSet = otherZone.enabledSet;
		dbm = otherZone.dbm;
		//timeSep = otherZone.timeSep;
	}
	
	public Zone1(DualHashMap<Transition, Integer> newEnabledSet, DBM newDbm) {
		this.enabledSet = newEnabledSet;
		this.dbm = newDbm;
	}
	
	/*
	 * Generate a new zone from a poset.
	 */
	public Zone1(Poset curPoset, LpnTranList[] nextEnabledArray) {		
		this.enabledSet = new DualHashMap<Transition, Integer>();
		int arraySize = nextEnabledArray.length;
		int lpnTranCnt = 0;
		for(int i = 0; i < arraySize; i++) {
			for(Transition tran : nextEnabledArray[i]) {
				this.enabledSet.insert(tran, lpnTranCnt+1);
				lpnTranCnt++;
			}
		}	
		
		/*
		 * Caching the enabled transition set.
		 */
		DualHashMap<Transition, Integer> newEnabledSet = Zone1.enabledArrayCache.get(this.enabledSet);
		if (newEnabledSet == null)
			Zone1.enabledArrayCache.put(this.enabledSet, this.enabledSet);
		else
			this.enabledSet = newEnabledSet;

		this.dbm = new DBM(lpnTranCnt + 1);

		for (int i = 1; i <= lpnTranCnt; i++) {
			Transition tran_i = this.enabledSet.getKey(i);
			// TODO: Get the upper bound of our LPN delay
			//this.dbm.assign(0, i, tran_i.getDelayUB());
			this.dbm.assign(i, 0, 0);
			for (int j = 1; j <= lpnTranCnt; j++) {
				Transition tran_j = this.enabledSet.getKey(j);
				if(tran_i == tran_j)
					continue;
				if(curPoset != null)
					this.dbm.assign(i, j, curPoset.getTimeSep(tran_i, tran_j));
				else
					this.dbm.assign(i, j, 0);
			}
		}
		this.dbm.canonicalize();
		
		/*
		 * Zone normalization
		 */
		int newLpnTranCnt = this.enabledSet.size();
		for(int i = 1; i <= newLpnTranCnt; i++) {
			//Transition tran = this.enabledSet.getKey(i);
			// TODO: Get the upper/lower bound of our LPN delay
			int premax = 3;//tran.getDelayUB()==INFINITY ? tran.getDelayLB() : tran.getDelayUB();
			int delta = this.dbm.value(i, 0) + premax;
			if(delta >= 0)
				continue;
			for(int j = 0; j <= newLpnTranCnt; j++) {
				if(i == j)
					continue;
				int new_ij = this.dbm.value(i, j) - delta;
				this.dbm.assign(i, j, new_ij);
				int new_ji = this.dbm.value(j, i) + delta;
				this.dbm.assign(j, i, new_ji);
			}
		}
		
		LinkedList<Integer> fixup = new LinkedList<Integer>();
		for(int i = 1; i <= newLpnTranCnt; i++) {
			//Transition tran_i = this.enabledSet.getKey(i);
			// TODO: Get the upper/lower bound of our LPN delay
			int premax_i = 3; //tran_i.getDelayUB()==INFINITY ? tran_i.getDelayLB() : tran_i.getDelayUB();
			if(this.dbm.value(0, i) > premax_i) {
				int t = premax_i;
				for(int j = 1; j <= newLpnTranCnt; j++) {
					//Transition tran_j = this.enabledSet.getKey(j);
					// TODO: Get the upper/lower bound of our LPN delay
					int premax_j = 3; //tran_j.getDelayUB()==INFINITY ? tran_j.getDelayLB() : tran_j.getDelayUB();
					int min = this.dbm.value(0, j) < premax_j ? this.dbm.value(0, j) : premax_j;
					int new_max = min - this.dbm.value(i, j);
					if(new_max > t)
						t = new_max;
				}
				if(t < this.dbm.value(0, i)) {
					fixup.addLast(i);
					this.dbm.assign(0, i, t);
				}
			}
		}
		
		if(fixup.size() > 0) {
			for(int i = 1; i <= newLpnTranCnt; i++) {
				for(Integer j : fixup) {
					if(i == j)
						continue;
					int val_i0 = this.dbm.value(i, 0);
					int val_0j = this.dbm.value(0, j);
					if(val_i0 + val_0j < this.dbm.value(i, j))
						this.dbm.assign(i, j, val_0j + val_0j);
				}
			}
		}
	}


	@Override
	public Zone1 clone() {
		return null;
	}

	/**
	 * Size of key set (width of DBM including t0).
	 * 
	 * @return
	 */
	public int size() {
		return this.enabledSet.size();
	}

	/**
	 * Initialize zone. Input is a list of lists of LPNTran
	 * 
	 * @param z
	 * @param initEnSet
	 */
	final public void initialize(LpnTranList[] initEnabledArray) {
		this.enabledSet = new DualHashMap<Transition, Integer>();
		int arraySize = initEnabledArray.length;
		int lpnTranCnt = 0;
		for(int i = 0; i < arraySize; i++) {
			for(Transition tran : initEnabledArray[i]) {
				this.enabledSet.insert(tran, lpnTranCnt+1);
				lpnTranCnt++;
			}
		}
		
		/*
		 * Caching the enabled transition set.
		 */
		DualHashMap<Transition, Integer> newEnabledSet = Zone1.enabledArrayCache.get(this.enabledSet);
		if(newEnabledSet==null)
			Zone1.enabledArrayCache.put(this.enabledSet, this.enabledSet);
		else
			this.enabledSet = newEnabledSet;
				
		this.dbm = new DBM(lpnTranCnt+1);
		
		for(int y = 1; y <= lpnTranCnt; y++) {
			//Transition curTran = this.enabledSet.getKey(y);
			// TODO: Get the upper/lower bound of our LPN delay
			//this.dbm.assign(0, y, curTran.getDelayUB());
		}
	}

	/**
	 * Call these functions. restrict(zone, fired, lowerBound);
	 * zone.recanonicalize(); zone.project(fired); allocate(newSet, zone);
	 * extend(newSet, nmnSet, zone); advanceTime(enSet, ubSet, zone);
	 * zone.recanonicalize();
	 * 
	 * @param firedLocalTran
	 * @param NEXT
	 * @param CURRENT
	 * @param nextEnabledList
	 * @param curModuleTranSet
	 */
	public Zone1 update(Transition firedTran, LpnTranList[] nextEnabledArray) {
		Zone1 newZone = new Zone1();
		
		newZone.enabledSet = new DualHashMap<Transition, Integer>();
		int arraySize = nextEnabledArray.length;
		int nextLpnTranCnt = 0;
		for(int i = 0; i < arraySize; i++) {
			for(Transition tran : nextEnabledArray[i]) {
				newZone.enabledSet.insert(tran, nextLpnTranCnt+1);
				nextLpnTranCnt++;
			}	
		}
		
		/*
		 * Caching the enabled transition set.
		 */
		DualHashMap<Transition, Integer> newEnabledSet = Zone1.enabledArrayCache.get(newZone.enabledSet);
		if(newEnabledSet==null)
			Zone1.enabledArrayCache.put(newZone.enabledSet, newZone.enabledSet);
		else
			newZone.enabledSet = newEnabledSet;
		
		/*
		 * Update DBM of this zone.		
		 */
		newZone.dbm = new DBM(nextLpnTranCnt+1);
		
		DBM curDbmCopy = new DBM(this.dbm);
		
		/*
		 * Restrict zone w.r.t firedTran, and canonialize it.
		 */
		int new_x = this.enabledSet.getValue(firedTran);
		// TODO: Get the upper/lower bound of our LPN delay
		//curDbmCopy.restrict(new_x, firedTran.getDelayLB());
		curDbmCopy.canonicalize();
		
		//System.out.println("update 1 \n" + curDbmCopy);
		
		/*
		 * Copy timing information of transitions enabled in both current and new states from tmp_dbm 
		 * to newZone 
		 */
		int curLpnTranCnt = this.enabledSet.size();
		for(int x = 0; x <= curLpnTranCnt; x++) {
			new_x = 0;
			if(x>0) {
				Transition tran_x = this.enabledSet.getKey(x);
				if(newZone.enabledSet.containsKey(tran_x) == false || tran_x==firedTran)
					continue;
				new_x = newZone.enabledSet.getValue(tran_x);
			}
			for(int y = 0; y <= curLpnTranCnt; y++) {
				int new_y = 0;
				if(y > 0) {
					Transition tran_y = this.enabledSet.getKey(y);
					if(newZone.enabledSet.containsKey(tran_y)==false || tran_y==firedTran)
						continue;
					new_y = newZone.enabledSet.getValue(tran_y);
				}
				//System.out.println("x, y = " +x+", " +y + ";  new_x, new_y = " + new_x +", " + new_y + ", val(x, y) = " + tmp_dbm.value(x, y));	
				newZone.dbm.assign(new_x, new_y, curDbmCopy.value(x, y));
			}
		}
		
		//System.out.println("update 2 \n" + newZone.dbm);

		
		/*
		 * For every enabled transitions in the nextState, advance its timer to DelayUB, and canonicalize.
		 */
		LinkedList<Transition> newTranList = new LinkedList<Transition>();
		int newLpnTranCnt = newZone.enabledSet.size();
		for(int y = 1; y <= newLpnTranCnt; y++) {
			Transition tran_y = newZone.enabledSet.getKey(y);

			if(this.enabledSet.containsKey(tran_y)==false || tran_y == firedTran) {
				newTranList.addLast(tran_y);
			}
		}
		
		/*
		 * Adjust timing constraints between the enabled transitions in the current state and
		 * those that are newly enabled in the next state.
		 */
		curLpnTranCnt = this.enabledSet.size();
		for(Transition newTran : newTranList) {
			int new_i = newZone.enabledSet.getValue(newTran);
			for(int j = 1; j <= curLpnTranCnt; j++) {
				Transition tran_j = this.enabledSet.getKey(j);
				if(newZone.enabledSet.containsKey(tran_j) == false)
					continue;
				int new_j = newZone.enabledSet.getValue(tran_j);
				int val_0j = newZone.dbm.value(0, new_j);
				newZone.dbm.assign(new_i, new_j, val_0j);
				int val_j0 = newZone.dbm.value(new_j, 0);
				newZone.dbm.assign(new_j, new_i, val_j0);
			}
		}
		
		/*
		 * For every enabled transitions in the nextState, advance its timer to DelayUB, and canonicalize.
		 */
		HashMap<Transition, Integer> newTranSet = new HashMap<Transition, Integer>();
		for(int y = 1; y <= newLpnTranCnt; y++) {
			Transition tran_y = newZone.enabledSet.getKey(y);
			// TODO: Get the upper/lower bound of our LPN delay
			//newZone.dbm.assign(0, y, tran_y.getDelayUB());
			newTranSet.put(tran_y, y);
			if(this.enabledSet.containsKey(tran_y)==false || tran_y == firedTran) {
				newTranList.addLast(tran_y);
			}
		}
		
		//System.out.println("update 3 \n" + newZone.dbm);

		newZone.dbm.canonicalize();

		//System.out.println("update 4 \n" + newZone.dbm);

		/*
		 * Zone normalization
		 */
		for(int i = 1; i <= newLpnTranCnt; i++) {
			//Transition tran = newZone.enabledSet.getKey(i);
			// TODO: Get the upper/lower bound of our LPN delay
			int premax = 3; //tran.getDelayUB()==INFINITY ? tran.getDelayLB() : tran.getDelayUB();
			int delta = newZone.dbm.value(i, 0) + premax;
			if(delta >= 0)
				continue;
			for(int j = 0; j <= newLpnTranCnt; j++) {
				if(i == j)
					continue;
				int new_ij = newZone.dbm.value(i, j) - delta;
				newZone.dbm.assign(i, j, new_ij);
				int new_ji = newZone.dbm.value(j, i) + delta;
				newZone.dbm.assign(j, i, new_ji);
			}
		}
		
		LinkedList<Integer> fixup = new LinkedList<Integer>();
		for(int i = 1; i <= newLpnTranCnt; i++) {
			//Transition tran_i = newZone.enabledSet.getKey(i);
			// TODO: Get the upper/lower bound of our LPN delay
			int premax_i = 3; //tran_i.getDelayUB()==INFINITY ? tran_i.getDelayLB() : tran_i.getDelayUB();
			if(newZone.dbm.value(0, i) > premax_i) {
				int t = premax_i;
				for(int j = 1; j <= newLpnTranCnt; j++) {
					//Transition tran_j = newZone.enabledSet.getKey(j);
					// TODO: Get the upper/lower bound of our LPN delay
					int premax_j = 3; //tran_j.getDelayUB()==INFINITY ? tran_j.getDelayLB() : tran_j.getDelayUB();
					int min = newZone.dbm.value(0, j) < premax_j ? newZone.dbm.value(0, j) : premax_j;
					int new_max = min - newZone.dbm.value(i, j);
					if(new_max > t)
						t = new_max;
				}
				if(t < newZone.dbm.value(0, i)) {
					fixup.addLast(i);
					newZone.dbm.assign(0, i, t);
				}
			}
		}
		
		if(fixup.size() > 0) {
			for(int i = 1; i <= newLpnTranCnt; i++) {
				for(Integer j : fixup) {
					if(i == j)
						continue;
					int val_i0 = newZone.dbm.value(i, 0);
					int val_0j = newZone.dbm.value(0, j);
					if(val_i0 + val_0j < newZone.dbm.value(i, j))
						newZone.dbm.assign(i, j, val_0j + val_0j);
				}
			}
		}
		
		//System.out.println("update 5 \n" + newZone.dbm);

		return newZone;
	}

	public Zone1[] split(LpnTranList[] enabledArray) {
		int arraySize = enabledArray.length;
        Zone1[] subZoneArray = new Zone1[arraySize];
        
        //HashMap<LinkedList<Transition>, Integer> timeSepSet = new HashMap<LinkedList<Transition>, Integer>();
        
        /*
         * Create the local zones and initialize their local enabledSet.
         */
        for(int curIdx = 0; curIdx < arraySize; curIdx++) {
        	if(enabledArray[curIdx]==null || enabledArray[curIdx].size()==0) {
        		subZoneArray[curIdx] = null;
        		continue;
        	}
        	
    		Zone1 curSubZone = new Zone1();
    		curSubZone.enabledSet = new DualHashMap<Transition, Integer>();
    		int tranIdx = 1;
    		//Transition curFirstTran = enabledArray[curIdx].getFirst();
    		for(Transition curTran : enabledArray[curIdx]) 
    			curSubZone.enabledSet.insert(curTran, tranIdx++);
    		
    		/*
        	 * Copy the cell values for the local zones from the global zone. 
        	 */
    		int localDim = curSubZone.enabledSet.size();
    		curSubZone.dbm = new DBM(localDim + 1);
    		for(int x = 0; x <= localDim; x++) {
    			int g_x = 0;
    			if(x > 0) {
    				Transition x_tran = curSubZone.enabledSet.getKey(x);
    				g_x = this.enabledSet.getValue(x_tran);
    			}
        		for(int y = 0; y <= localDim; y++) {
        			if(x==y)
        				continue;
        			
        			int g_y = 0;
        			if(y > 0) {
        				Transition y_tran = curSubZone.enabledSet.getKey(y);
        				g_y = this.enabledSet.getValue(y_tran);
        			}
        			
        			curSubZone.dbm.assign(x, y, this.dbm.value(g_x, g_y));
            	}        		
            }

    		/*
    		 * Generate the time separations between this and other modules.
    		 */
//            for(int otherIdx = 0; otherIdx < arraySize; otherIdx++) {
//            	if(curIdx == otherIdx || enabledArray[otherIdx] == null || enabledArray[otherIdx].size()==0)
//            		continue;
//            	
//        		LPNTran otherFirstTran = enabledArray[otherIdx].getFirst();
//        		
//        		if(curSubZone.timeSep==null)
//        			curSubZone.timeSep = new HashMap<LinkedList<LPNTran>, Integer>();
//        		
//        		int	curFirstTranIdx = this.enabledSet.getValue(curFirstTran);
//        		int	otherFirstTranIdx = this.enabledSet.getValue(otherFirstTran);
//        		LinkedList<LPNTran> cur2otherPair = new LinkedList<LPNTran>();
//        		cur2otherPair.addFirst(curFirstTran);
//        		cur2otherPair.addLast(otherFirstTran);
//        		curSubZone.timeSep.put(cur2otherPair, this.dbm.value(otherFirstTranIdx, curFirstTranIdx));
//        		LinkedList<LPNTran> other2curPair = new LinkedList<LPNTran>();
//        		other2curPair.addFirst(otherFirstTran);
//        		other2curPair.addLast(curFirstTran);
//        		curSubZone.timeSep.put(other2curPair, this.dbm.value(curFirstTranIdx, otherFirstTranIdx));
//        		
//        		timeSepSet.put(cur2otherPair, this.dbm.value(otherFirstTranIdx, curFirstTranIdx));
//        		timeSepSet.put(other2curPair, this.dbm.value(curFirstTranIdx, otherFirstTranIdx));
//            }
    		
    		subZoneArray[curIdx] = curSubZone;
        }
       
        
        return subZoneArray;
	}
	
	public LinkedList<Integer> enlarge(LpnTranList[] enabledArray) {
		//System.out.println(this + "\n");

		int arraySize = enabledArray.length;
		DBM dbmCopy = new DBM(this.dbm);
		this.dbm = dbmCopy;
		
        LinkedList<Integer> timeSepSet = new LinkedList<Integer>();

		for(int i = 0; i < arraySize; i++) {
			for(int j = 0; j < arraySize; j++) {
				if(i == j)
					continue;
				
				//boolean first_j = true;
				for(Transition tran_i : enabledArray[i]) {
					int m_i = this.enabledSet.getValue(tran_i);
					for(Transition tran_j : enabledArray[j]) {
						int m_j = this.enabledSet.getValue(tran_j);
//						if(first_j) {
//							first_j = false;
//							timeSepSet.addLast(this.dbm.value(m_i, m_j));
//							continue;
//						}
						
												
						//System.out.println(tran_i.getFullLabel() + "  " + tran_j.getFullLabel());
						this.dbm.assign(m_i, m_j, INFINITY);
						this.dbm.assign(m_j, m_i, INFINITY);
					}
				}
			}
		}

		//System.out.println(this.dbm + "-----------------------");
		//System.out.println(this.dbm + "\n");
		this.dbm.canonicalize();
		//System.out.println(this.dbm + "xxxxxxxxxxxxxxxxxxxxxxx\n");
		
		if(timeSepSet.size() > 0) {
			LinkedList<Integer> newTimeSepSet = Zone1.timeSepTbl.get(timeSepSet); 
			if(newTimeSepSet == null)
				Zone1.timeSepTbl.put(timeSepSet, timeSepSet);
			else
				timeSepSet = newTimeSepSet;
		}
		return timeSepSet;
	}
	
	public int[] signature() {
		return this.dbm.signature();
	}
	
	/**
	 * Set (0,t) to the upper bound of t, for all t.
	 * 
	 * @param initEnSet
	 */
	public boolean checkTiming(Transition curTran) {
		// TODO: Get the upper/lower bound of our LPN delay
		int curTranDelayLB = 3; // curTran.getDelayLB();
		if (curTranDelayLB == 0) {
            return true;
        }
		
		int x = this.enabledSet.getValue(curTran);
        Integer curTimingUB = this.dbm.value(0, x);
        return curTranDelayLB <= curTimingUB;
	}

	/*
	 * Check if the this zone is a subset of the other zone.
	 */
	public boolean subset(Zone1 other) {
		if(this.enabledSet != other.enabledSet)
			return false;
		
		return this.dbm.subset(other.dbm);
	}
	
	public DualHashMap<Transition, Integer> getEnabledSet() {
		return this.enabledSet;
	}
	
	public DBM getDbm() {
		return this.dbm;
	}
	
	@Override
	public int hashCode() {
		//if(this.timeSep==null)
			return Integer.rotateLeft(this.enabledSet.hashCode(), 11) ^ Integer.rotateLeft(this.dbm.hashCode(), 7);
     
		//return Integer.rotateLeft(this.enabledSet.hashCode(), 11) ^ Integer.rotateLeft(this.dbm.hashCode(), 7) ^ Integer.rotateLeft(this.timeSep.hashCode(), 19);
	}

	@Override
	public boolean equals(Object other) {
		Zone1 otherZone = (Zone1)other;
		//if(this.enabledSet.equals(otherZone.enabledSet)==false)
		if(this.enabledSet != otherZone.enabledSet)	
			return false;	
		
//		System.out.println("this zone : " + this.dbm);
//		System.out.println("\nother zone : " + otherZone.dbm + "\n =================================");
		
		//if(this.dbm.equals(otherZone.dbm)==false)
		if(this.dbm != otherZone.dbm)
			return false;

//		if(this.timeSep == null && otherZone.timeSep == null)
//			return true;
//		
//		if((this.timeSep == null && otherZone.timeSep != null) || (this.timeSep != null && otherZone.timeSep == null))
//			return false;
//		
//		if(this.timeSep.equals(otherZone.timeSep) == false)
//			return false;
				
		return true;
	}
	
	@Override
	public String toString() {
		String ret = new String();
		ret = "\tt0";
		for(int i = 1; i <= this.enabledSet.size(); i++)
			ret  += "\t" + this.enabledSet.getKey(i).getFullLabel();
		
		String timeSepConstraints = new String();
		
//		if(this.timeSep != null) {
//			Set<LinkedList<LPNTran>> tranPairs = this.timeSep.keySet();
//			for(LinkedList<LPNTran> tranPair : tranPairs)
//				timeSepConstraints += "(" + tranPair.getFirst().getFullLabel() + ", " + tranPair.getLast().getFullLabel() + ", " + this.timeSep.get(tranPair)+"),  ";
//		}
//		else
//			timeSepConstraints = "timeSep = null";
		
		
		return ret + "\n\n" + this.dbm.toString() + timeSepConstraints +"\n";
	}
}
