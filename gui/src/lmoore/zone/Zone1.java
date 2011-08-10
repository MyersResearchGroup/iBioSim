package lmoore.zone;

import java.util.*;

import platu.Common;
import platu.lpn.*;

public class Zone1 {
	public static final int INFINITY = platu.Common.INFINITY;
	
	public static HashMap<DualHashMap, DualHashMap> enabledArrayCache = null;
    public static HashMap<DBM, DBM> dbmCache = null;

	DualHashMap<LPNTran, Integer> enabledSet;
	private DBM dbm;

	public Zone1() {
		if(Zone1.enabledArrayCache==null)
			Zone1.enabledArrayCache = new HashMap<DualHashMap, DualHashMap>();
		if(Zone1.dbmCache==null)
			Zone1.dbmCache = new HashMap<DBM, DBM>();
		enabledSet = null;
		dbm = null;
	}
	
	public Zone1(Object other) {
		Zone1 otherZone = (Zone1)other;
		enabledSet = otherZone.enabledSet;
		dbm = otherZone.dbm;
	}

	public void project(LPNTran firedTran) {
	}

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
		this.enabledSet = new DualHashMap<LPNTran, Integer>();
		int arraySize = initEnabledArray.length;
		int lpnTranCnt = 0;
		for(int i = 0; i < arraySize; i++) {
			for(LPNTran tran : initEnabledArray[i]) {
				this.enabledSet.insert(tran, lpnTranCnt+1);
				lpnTranCnt++;
			}
		}
		
		/*
		 * Caching the enabled transition set.
		 */
		DualHashMap<LPNTran, Integer> newEnabledSet = Zone1.enabledArrayCache.get(this.enabledSet);
		if(newEnabledSet==null)
			Zone1.enabledArrayCache.put(this.enabledSet, this.enabledSet);
		else
			this.enabledSet = newEnabledSet;
				
		this.dbm = new DBM(lpnTranCnt+1);
		
		for(int y = 1; y <= lpnTranCnt; y++) {
			LPNTran curTran = this.enabledSet.getKey(y);
			this.dbm.assign(0, y, curTran.getDelayUB());
		}
		
		/*
		 * Caching the DBM.
		 */
		DBM newDbm = Zone1.dbmCache.get(this.dbm);
		if(newDbm == null)
			Zone1.dbmCache.put(this.dbm, this.dbm);
		else
			this.dbm = newDbm;
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
	public Zone1 update(LPNTran firedTran, LpnTranList[] nextEnabledArray) {
		Zone1 newZone = new Zone1();
		
		newZone.enabledSet = new DualHashMap<LPNTran, Integer>();
		int arraySize = nextEnabledArray.length;
		int nextLpnTranCnt = 0;
		for(int i = 0; i < arraySize; i++) {
			for(LPNTran tran : nextEnabledArray[i]) {
				newZone.enabledSet.insert(tran, nextLpnTranCnt+1);
				nextLpnTranCnt++;
			}	
		}
		
		/*
		 * Caching the enabled transition set.
		 */
		DualHashMap<LPNTran, Integer> newEnabledSet = Zone1.enabledArrayCache.get(newZone.enabledSet);
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
		curDbmCopy.restrict(new_x, firedTran.getDelayLB());
		curDbmCopy.canonicalize();
		
		System.out.println("update 1 \n" + curDbmCopy);
		
		/*
		 * Copy timing information of transitions enabled in both current and new states from tmp_dbm 
		 * to newZone 
		 */
		int curLpnTranCnt = this.enabledSet.size();
		for(int x = 0; x <= curLpnTranCnt; x++) {
			new_x = 0;
			if(x>0) {
				LPNTran tran_x = this.enabledSet.getKey(x);
				if(newZone.enabledSet.containsKey(tran_x) == false || tran_x==firedTran)
					continue;
				new_x = newZone.enabledSet.getValue(tran_x);
			}
			for(int y = 0; y <= curLpnTranCnt; y++) {
				int new_y = 0;
				if(y > 0) {
					LPNTran tran_y = this.enabledSet.getKey(y);
					if(newZone.enabledSet.containsKey(tran_y)==false || tran_y==firedTran)
						continue;
					new_y = newZone.enabledSet.getValue(tran_y);
				}
				//System.out.println("x, y = " +x+", " +y + ";  new_x, new_y = " + new_x +", " + new_y + ", val(x, y) = " + tmp_dbm.value(x, y));	
				newZone.dbm.assign(new_x, new_y, curDbmCopy.value(x, y));
			}
		}
		
		System.out.println("update 2 \n" + newZone.dbm);

		
		/*
		 * For every enabled transitions in the nextState, advance its timer to DelayUB, and canonicalize.
		 */
		LinkedList<LPNTran> newTranList = new LinkedList<LPNTran>();
		int newLpnTranCnt = newZone.enabledSet.size();
		for(int y = 1; y <= newLpnTranCnt; y++) {
			LPNTran tran_y = newZone.enabledSet.getKey(y);

			if(this.enabledSet.containsKey(tran_y)==false || tran_y == firedTran) {
				newTranList.addLast(tran_y);
			}
		}
		
		/*
		 * Adjust timing constraints between the enabled transitions in the current state and
		 * those that are newly enabled in the next state.
		 */
		curLpnTranCnt = this.enabledSet.size();
		for(LPNTran newTran : newTranList) {
			int new_i = newZone.enabledSet.getValue(newTran);
			for(int j = 1; j <= curLpnTranCnt; j++) {
				LPNTran tran_j = this.enabledSet.getKey(j);
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
		HashMap<LPNTran, Integer> newTranSet = new HashMap<LPNTran, Integer>();
		for(int y = 1; y <= newLpnTranCnt; y++) {
			LPNTran tran_y = newZone.enabledSet.getKey(y);
			newZone.dbm.assign(0, y, tran_y.getDelayUB());
			newTranSet.put(tran_y, y);
			if(this.enabledSet.containsKey(tran_y)==false || tran_y == firedTran) {
				newTranList.addLast(tran_y);
			}
		}
		
		System.out.println("update 3 \n" + newZone.dbm);

		newZone.dbm.canonicalize();

		System.out.println("update 4 \n" + newZone.dbm);

		/*
		 * Zone normalization
		 */
		for(int i = 1; i <= newLpnTranCnt; i++) {
			LPNTran tran = newZone.enabledSet.getKey(i);
			int premax = tran.getDelayUB()==INFINITY ? tran.getDelayLB() : tran.getDelayUB();
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
			LPNTran tran_i = newZone.enabledSet.getKey(i);
			int premax_i = tran_i.getDelayUB()==INFINITY ? tran_i.getDelayLB() : tran_i.getDelayUB();
			if(newZone.dbm.value(0, i) > premax_i) {
				int t = premax_i;
				for(int j = 1; j <= newLpnTranCnt; j++) {
					LPNTran tran_j = newZone.enabledSet.getKey(j);
					int premax_j = tran_j.getDelayUB()==INFINITY ? tran_j.getDelayLB() : tran_j.getDelayUB();
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
		
		System.out.println("update 5 \n" + newZone.dbm);

		
		/*
		 * Caching the DBM.
		 */
		DBM newDbm = Zone1.dbmCache.get(newZone.dbm);
		if(newDbm == null)
			Zone1.dbmCache.put(newZone.dbm, newZone.dbm);
		else
			newZone.dbm = newDbm;
		
		return newZone;
	}
	
	
	public Zone1[] split(LpnTranList[] enabledArray) {
		int arraySize = enabledArray.length;
        Zone1[] ret = new Zone1[arraySize];
        
        /*
         * Create the local zones and initialize their local enabledSet.
         */
        String  out="" + this;
        for(int thisOne = 0; thisOne < arraySize; thisOne++) {
        	if(enabledArray[thisOne]==null || enabledArray[thisOne].size()==0) {
        		ret[thisOne] = null;
        		continue;
        	}
        	
    		Zone1 tmp = new Zone1();
    		tmp.enabledSet = new DualHashMap<LPNTran, Integer>();
    		int tranIdx = 1;
    		for(LPNTran curTran : enabledArray[thisOne]) 
    			tmp.enabledSet.insert(curTran, tranIdx++);
    		
    		
    		/*
        	 * Copy the cell values for the local zones from the global zone. 
        	 */
    		int localDim = tmp.enabledSet.size();
    		tmp.dbm = new DBM(localDim + 1);
    		for(int x = 0; x <= localDim; x++) {
    			int g_x = 0;
    			if(x > 0) {
    				LPNTran x_tran = tmp.enabledSet.getKey(x);
    				g_x = this.enabledSet.getValue(x_tran);
    			}
        		for(int y = 0; y <= localDim; y++) {
        			if(x==y)
        				continue;
        			
        			int g_y = 0;
        			if(y > 0) {
        				LPNTran y_tran = tmp.enabledSet.getKey(y);
        				g_y = this.enabledSet.getValue(y_tran);
        			}
        			
        			tmp.dbm.assign(x, y, this.dbm.value(g_x, g_y));
            	}        		
            }
//    		if(tmp != null) 
//    			out= Main.mergeColumns(out, tmp +"", 30, 30);
//    		else
//    			out= Main.mergeColumns(out,  "---", 30, 30);
    		
    		ret[thisOne] = tmp;
        }
       
        //System.out.printf("%s\n",out);
        return ret;
	}

	/**
	 * Set (0,t) to the upper bound of t, for all t.
	 * 
	 * @param initEnSet
	 */
	public boolean checkTiming(LPNTran curTran) {
		int curTranDelayLB = curTran.getDelayLB();
		if (curTranDelayLB == 0) {
            return true;
        }
		
		int x = this.enabledSet.getValue(curTran);
        Integer curTimingUB = this.dbm.value(0, x);
        return curTranDelayLB <= curTimingUB;
	}

	
	
//	@Override
//	public int hashCode() {
//		return this.enabledSet.hashCode() >> 11 ^ this.dbm.hashCode() << 9;
//	}
//
//	@Override
//	public boolean equals(Object other) {
//		Zone1 otherZone = (Zone1)other;
//		//if(this.enabledSet.equals(otherZone.enabledSet)==false)
//		if(this.enabledSet != otherZone.enabledSet)	
//			return false;	
//		
////		System.out.println("this zone : " + this.dbm);
////		System.out.println("\nother zone : " + otherZone.dbm + "\n =================================");
//		
//		//if(this.dbm.equals(otherZone.dbm)==false)
//		if(this.dbm != otherZone.dbm)
//			return false;
//
//		return true;
//	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dbm == null) ? 0 : dbm.hashCode());
		result = prime * result
				+ ((enabledSet == null) ? 0 : enabledSet.hashCode());
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
		Zone1 other = (Zone1) obj;
		if (dbm == null) {
			if (other.dbm != null)
				return false;
		} else if (!dbm.equals(other.dbm))
			return false;
		if (enabledSet == null) {
			if (other.enabledSet != null)
				return false;
		} else if (!enabledSet.equals(other.enabledSet))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String ret = new String();
		ret = "\tt0";
		for(int i = 1; i <= this.enabledSet.size(); i++)
			ret  += "\t" + this.enabledSet.getKey(i).getFullLabel();
		
		return ret + "\n\n" + this.dbm.toString();
	}
}
