/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lmoore.zone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import lmoore.zone.impl.HashedHashedMapZoneImpl;
import lmoore.zone.impl.MatrixZoneVer1Impl;
import lmoore.zone.impl.MatrixZoneVer3Impl;
import lmoore.zone.impl.SparseHashedZoneImpl;

import org.apache.commons.collections.primitives.ArrayIntList;
import platu.Common;
import platu.Main;
import platu.lpn.LPNTran;
import platu.lpn.LpnTranList;

/**
 *
 * @author ldmtwo
 */
abstract public class Zone implements Serializable {

    private static final long serialVersionUID = 82713648276378L;
    public static PrintStream out = Main.out;
    public static final int MIN_INF = (int) (Integer.MAX_VALUE * 0.45);
    public static final int INF = (Integer.MAX_VALUE);
    public static final int MAX_NEG_INF = (int) (Integer.MIN_VALUE * 0.45);
    public static final int NEG_INF = (Integer.MIN_VALUE);
    public int ID = -1;
    public static int NEXT_ID = 0;
    final public static Class[] types = {
        MatrixZoneVer1Impl.class,
        HashedHashedMapZoneImpl.class,
        MatrixZoneVer3Impl.class,
        SparseHashedZoneImpl.class
    };

    /**
     * This is used by all zone implementations
     * @return
     */
//    final public static int nextID() {
//        return NEXT_ID++;
//    }
    public final static Zone newZone() {
        return new MatrixZoneVer3Impl();
    }

    /**
     * Memory = 4N^2 + 13*N bytes (ignoring constants)
     * Passive (final) memory is 4N^2 bytes (ignoring constants)
     * @return
     */
    public final static MatrixZoneVer1Impl newMatrixZoneVer1() {
        return new MatrixZoneVer1Impl();
    }

    /**
     * Active memory = 4N^2 + 9*N bytes (ignoring constants)
     * Passive (final) memory is 4N^2 bytes (ignoring constants)
     * @return
     */
    public final static MatrixZoneVer3Impl newMatrixZoneVer3() {
        return new MatrixZoneVer3Impl();
    }

    /**
     * Active memory is at most 8N^2 + 4N bytes (ignoring constants)  <br>
     * Passive (final) memory is at most 8N^2 bytes (ignoring constants)
     * @return
     */
    public final static SparseHashedZoneImpl newSparseHashedZone() {
        return new SparseHashedZoneImpl();
    }

    /**
     * Active memory = 9N^2 bytes (ignoring constants)
     * Passive (final) memory is 9N^2 bytes (ignoring constants)
     * @return
     */
    public final static HashedHashedMapZoneImpl newHashedHashedMapZone() {
        return new HashedHashedMapZoneImpl();
    }

    private static void restrict(Zone zone, int fired, int lowerBound) {
        //        Main.ZONE_ENABLE_PRINT=true;
        if (zone.get(fired, 0) > -lowerBound) {
            zone.put(fired, 0, -lowerBound);
        }

    }

    abstract public void restrict(int transIndex, int low);

    /**
     * Create a space in memory for this tmpTimer. This is sometimes the
     * exact same as extend, but usually not.
     * @param timerID
     */
    abstract public void allocate(int timerID);

    /**
     * Low level call to remove something. This is not the same as project() in most cases.
     * @param t
     */
    abstract public void remove(int timeerID);

    /**
     * Remove a row and column from the DBM.
     * @param t
     */
    abstract public void project(int timedID);

    /**
     * Make the DBM canonical to ensure it is not wasting memory.
     */
    abstract public void recanonicalize();

    /**
     * Set a specific row and col to a value.
     * @param row
     * @param col
     * @param val
     * @return
     */
    abstract public boolean put(int row, int col, int val);

    /**
     * Retrieve the value of a row and col.
     * @param row
     * @param col
     * @return
     */
    abstract public int get(int row, int col);

    /**
     * test if (row,col) is near Integer.MAX_VALUE.
     * @param row
     * @param col
     * @return
     */
    abstract public boolean isInf(int row, int col);

    /**
     * test if (row,col) is near Integer.MIN_VALUE.
     * @param row
     * @param col
     * @return
     */
    abstract public boolean isNegInf(int row, int col);

    /**
     * Test if a tmpTimer is enabled
     * @param delayLB
     * @param label
     * @return
     */
    abstract public boolean isTimeEnabled(int delayLB, int label);

    /**
     * Deep copy of a zone.
     * @return
     */
    abstract public Zone clone();

    /**
     * Check if the diagonal of the DBM is all 0's.
     * @return
     */
    abstract public boolean isValid();

    /**
     * Make a printable string.
     * @return
     */
    @Override
    abstract public String toString();

    /**
     * Hashcode
     * @return
     */
    @Override
    abstract public int hashCode();

    /**
     * Equals
     * @param o
     * @return
     */
    @Override
    abstract public boolean equals(Object o);

    /**
     * Unique cryptographic hash string.
     * @return
     */
    abstract public String digest();

    /**
     * Same as toString, but using HTML tags as a table.
     * @return
     */
    abstract public String toHTMLTable();

    /**
     * Size of key set (width of DBM including t0).
     * @return
     */
    abstract public int size();

    /**
     * Force the diagonal to be 0's. This should only be used if you are
     * iterating and modifying all values of the DBM. However, you should not
     * modify the diagonal if you can help it.
     */
    abstract public void validate();

    /**
     * Returns true if the the tmpTimer is found in the key set. Same as contains().
     * It is duplicate in order to override some interfaces that have this.
     * @param t
     * @return
     */
    abstract public boolean containsKey(int t);

    /**
     * Returns true if the the tmpTimer is found in the key set. Same as containsKey().
     * It is duplicate in order to override some interfaces that have this.
     * @param t
     * @return
     */
    abstract public boolean contains(int t);

    /**
     * Write the DBM to a file to be later be read back in.
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    abstract public File serialize(String filename)
            throws FileNotFoundException, IOException;

    /**
     * Set of timers currently in the DBM.
     * @return
     */
    abstract public Object keySet();

    /**
     *Unique ID to distinguish two Zones.
     * @return
     */
    abstract public int getID();

    public static Zone[] split2(MatrixZoneVer3Impl initialZone, LinkedList[] nextEnabledList) {
        MatrixZoneVer3Impl[] ret = new MatrixZoneVer3Impl[nextEnabledList.length];
        int i = 0;
        String  out=""+initialZone;
        for (LinkedList list1 : nextEnabledList) {
            ret[i] = (MatrixZoneVer3Impl) initialZone.clone();
            for (LinkedList list2 : nextEnabledList) {
                if (list1 != list2) {
                    for (Object o : list2) {
                        LPNTran tran = (LPNTran) o;
                        ret[i].project(tran.getID());
                    }
                }
            }
       out= Main.mergeColumns(out, ret[i]+"", 30, 30);
            i++;
            
        }
        System.out.printf("%s\n",out);
        return ret;
    }

    
    public static Zone[] split(MatrixZoneVer3Impl initialZone, LinkedList[] enabledArray) {
    	int arraySize = enabledArray.length;
        MatrixZoneVer3Impl[] ret = new MatrixZoneVer3Impl[enabledArray.length];
        
        String  out=""+initialZone;
        for(int thisOne = 0; thisOne < arraySize; thisOne++) {
    		ret[thisOne]=(MatrixZoneVer3Impl) initialZone.clone();
        	for(int otherOne = 0; otherOne < arraySize; otherOne++) {
        		if(thisOne == otherOne)
        			continue;
        		for(Object obj : enabledArray[otherOne]) {
        			LPNTran tran = (LPNTran) obj;
        			ret[thisOne].project(tran.getID());
        		}
        	}
            out= Main.mergeColumns(out, ret[thisOne]+"", 30, 30);
        }
        //System.out.printf("%s\n",out);
        return ret;
    }

    /**
     *Create the NEW and the NEXT_MINUS_NEW set from the NEXT and CURRENT set.
     * @param newTransitionListArray output
     * @param nextMinusNewTransitionListArray output
     * @param nextTransitionListArray input
     * @param currentTransitionListArray input
     */
    static final void calculateNewLists(ArrayList<LinkedList<LPNTran>> newTransitionListArray,
            ArrayList<LinkedList<LPNTran>> nextMinusNewTransitionListArray,
            LinkedList[] nextTransitionListArray,
            LinkedList[] currentTransitionListArray) {
        LinkedList<LPNTran> newEnSet, nextMinusNew;
        int size = currentTransitionListArray.length;
        for (int x = 0; x < size; x++) {
            LinkedList<LPNTran> nextSet = nextTransitionListArray[x];
            LinkedList<LPNTran> currentSet = currentTransitionListArray[x];
            //newEnSet = next - current
            newEnSet = new LinkedList<LPNTran>(nextSet);
            newEnSet.removeAll(currentSet);
            //nextMinusNew = next - new
            nextMinusNew = new LinkedList<LPNTran>(nextSet);
            nextMinusNew.removeAll(newEnSet);
            //save our work for later
            newTransitionListArray.add(new LinkedList<LPNTran>(newEnSet));
            nextMinusNewTransitionListArray.add(new LinkedList<LPNTran>(nextMinusNew));
        }
    }

    /**
     *Create the NEW and the NEXT_MINUS_NEW set from the NEXT and CURRENT set.
     * @param newTransitionListArray output
     * @param nextMinusNewTransitionListArray output
     * @param nextTransitionListArray input
     * @param currentTransitionListArray input
     */
    static final void calculateNewLists(ArrayList<LinkedList<LPNTran>> newTransitionListArray,
            ArrayList<LinkedList<LPNTran>> nextMinusNewTransitionListArray,
            ArrayList<LinkedList<LPNTran>> nextTransitionListArray,
            ArrayList<LinkedList<LPNTran>> currentTransitionListArray) {
        LinkedList<LPNTran> newEnSet, nextMinusNew;
        int size = currentTransitionListArray.size();
        for (int x = 0; x < size; x++) {
            LinkedList<LPNTran> nextSet = nextTransitionListArray.get(x);
            LinkedList<LPNTran> currentSet = currentTransitionListArray.get(x);
            //newEnSet = next - current
            newEnSet = new LinkedList<LPNTran>(nextSet);
            newEnSet.removeAll(currentSet);
            //nextMinusNew = next - new
            nextMinusNew = new LinkedList<LPNTran>(nextSet);
            nextMinusNew.removeAll(newEnSet);
            //save our work for later
            newTransitionListArray.add(new LinkedList<LPNTran>(newEnSet));
            nextMinusNewTransitionListArray.add(new LinkedList<LPNTran>(nextMinusNew));
        }
    }

    /**
     *
     * @param newEnSet
     * @param nextMinusNew
     * @param zone
     */
    static final private void extend(int[] newEnSet, int[] nextMinusNew, Zone zone) {
        for (int i : newEnSet) {
            //Mi0 = M0i = 0;
            zone.put(i, 0, 0);
            zone.put(0, i, 0);
            for (int k : newEnSet) {
                //Mik = Mki = 0;
                zone.put(i, k, 0);
                zone.put(k, i, 0);
            }
            for (int k : nextMinusNew) {
                //M(ik)=M(0k)
                zone.put(i, k, zone.get(0, k));
                zone.put(k, i, zone.get(k, 0));
            }
        }
    }

    /**
     *Create a space in memory for these timers.
     * @param label
     * @param zone
     */
    static final private void allocate(int[] label, Zone zone) {
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- zoneAllocation_A()");
        }
        for (int t : label) {
            if (Main.ZONE_ENABLE_PRINT) {
                System.out.println("Alloc: " + t);
            }
            if (!zone.containsKey(t)) {
                zone.allocate(t);
            }
        }
    }

    /**
     *Initialize zone. Input is a list of lists of LPNTran
     * @param z
     * @param initEnSet
     */
    final public void initialize(ArrayList<LinkedList<LPNTran>> initEnSet) {

        extend(initEnSet);
        advanceTime(initEnSet);
        //tighten loose bounds
        recanonicalize();
        if (!isValid()) {//check for errors
            new Exception("zone.isValid==false: lpnt=INIT_TRAN\n\tzone=" + this).printStackTrace(platu.Main.out);
            Common.getChar();
        }
    }

    /** Call these functions.
     *   restrict(zone, fired, lowerBound);
     *   zone.recanonicalize();
     *   zone.project(fired);
     *   allocate(newSet, zone);
     *   extend(newSet, nmnSet, zone);
     *   advanceTime(enSet, ubSet, zone);
     *   zone.recanonicalize();
     *
     * @param firedLocalTran
     * @param NEXT
     * @param CURRENT
     * @param nextEnabledList
     * @param curModuleTranSet
     */
    public static void update(Zone zone, LPNTran firedTran, LinkedList[] nextEnabledList, LinkedList[] curEnabledList) {
        zone.update(firedTran, nextEnabledList, curEnabledList);
        // project allowable disables
        zone.projectExtraneous(zone, curEnabledList, nextEnabledList, firedTran);
    }

    /** Call these functions.
     *   restrict(zone, fired, lowerBound);
     *   zone.recanonicalize();
     *   zone.project(fired);
     *   allocate(newSet, zone);
     *   extend(newSet, nmnSet, zone);
     *   advanceTime(enSet, ubSet, zone);
     *   zone.recanonicalize();
     *
     * @param firedLocalTran
     * @param NEXT
     * @param CURRENT
     * @param nextEnabledList
     * @param curModuleTranSet
     */
    public void update(LPNTran firedLocalTran,
            LinkedList[] NEXT, LinkedList[] CURRENT,
            LPNTran[][] nextEnabledList, LPNTran[][] curModuleTranSet) {
        update(firedLocalTran, NEXT, CURRENT);
        // project allowable disables
        projectExtraneous(this, firedLocalTran, nextEnabledList, curModuleTranSet);
    }

    /**This version converts from LinkedList< LPNTran >[] to int[] and
     * calls a version of update that uses int[] directly.
     *
     * @param zone
     * @param firedTran
     * @param nextTransitionListArray
     * @param currentTransitionListArray
     */
    final public void update(LPNTran firedTran,
            LinkedList[] nextTransitionListArray,
            LinkedList[] currentTransitionListArray) {
//        Main.ZONE_ENABLE_PRINT=true;
        int size = currentTransitionListArray.length;
        final ArrayList<LinkedList<LPNTran>> newTransitionListArray = new ArrayList<LinkedList<LPNTran>>(size);
        final ArrayList<LinkedList<LPNTran>> nextMinusNewTransitionListArray = new ArrayList<LinkedList<LPNTran>>(size);
        //determine {new} and {next-new} for later use
        calculateNewLists(newTransitionListArray, nextMinusNewTransitionListArray,
                nextTransitionListArray, currentTransitionListArray);
        isValid();
        ArrayIntList newList = new ArrayIntList(1);
        ArrayIntList ubList = new ArrayIntList(1);
        ArrayIntList enList = new ArrayIntList(1);
        ArrayIntList nextMinusNewList = new ArrayIntList(1);
        for (int x = 0; x < currentTransitionListArray.length; x++) {
            for (LPNTran i : newTransitionListArray.get(x)) {
                newList.add(i.getID());
                ubList.add(i.getDelayUB());
                enList.add(i.getID());
            }
            for (LPNTran i : nextMinusNewTransitionListArray.get(x)) {
                nextMinusNewList.add(i.getID());
                ubList.add(i.getDelayUB());
                enList.add(i.getID());
            }
        }

        int LB = firedTran.getDelayLB();
        int fired = firedTran.getID();
        int[] newSet = newList.toArray();
        int[] enSet = enList.toArray();
        int[] nmnSet = nextMinusNewList.toArray();
        int[] ubSet = ubList.toArray();
        if (Main.ZONE_ENABLE_PRINT) {
            System.out.printf("LB=%s\tFIRED=%s\tNEW: %s\tEN:%s\tUB: %s\tNMN: %s\n",
                    LB, fired,
                    Arrays.toString(newSet),
                    Arrays.toString(enSet),
                    Arrays.toString(ubSet),
                    Arrays.toString(nmnSet));
        }
        update(this, fired, LB, newSet, nmnSet, ubSet, enSet);

//        if (!isValid()) {
//            new Exception("zone.isValid==false: lpnt=" + firedTran + "\n\tzone=" + this).printStackTrace(platu.Main.out);
//            Common.getChar();
//        }
    }

    /** Call these functions.
     *   restrict(zone, fired, lowerBound);
     *   zone.recanonicalize();
     *   zone.project(fired);
     *   allocate(newSet, zone);
     *   extend(newSet, nmnSet, zone);
     *   advanceTime(enSet, ubSet, zone);
     *   zone.recanonicalize();
     * @param zone
     * @param fired
     * @param lowerBound
     * @param newSet
     * @param nmnSet
     * @param ubSet
     * @param enSet
     */
    public final static void update(Zone zone, int fired, int lowerBound,
            int[] newSet, int[] nmnSet, int[] ubSet, int[] enSet) {
        restrict(zone, fired, lowerBound);
        zone.recanonicalize();
        zone.project(fired);
        allocate(newSet, zone);
        extend(newSet, nmnSet, zone);
        advanceTime(enSet, ubSet, zone);
        zone.recanonicalize();
    }

    /**
     * Create the initial DBM.
     * @param z
     * @param label
     * @param UB
     */
    static final public void initialize(Zone z, int[] label, int[] UB) {
        //extend DMB with new timers
        for (int t : label) {
            if (!z.containsKey(t)) {
                z.allocate(t);
            }
        }
        for (int i : label) {
            //Mi0 = M0i = 0;
            z.put(i, 0, 0);
            z.put(0, i, 0);
            for (int k : label) {
                //Mik = Mki = 0;
                z.put(i, k, 0);
                z.put(k, i, 0);
            }
        }
        //advance time
        int idx = 0;
        for (int t : label) {
            boolean b = z.put(0, t, UB[idx++]);
            if (!b) {//check for errors
                new Exception("zone.put==false: lpnt=" + t + "\n\tzone=" + z).printStackTrace(platu.Main.out);
                Common.getChar();
            }
        }
        //tighten loose bounds
        z.recanonicalize();
        if (!z.isValid()) {//check for errors
            new Exception("zone.isValid==false: lpnt=INIT_TRAN\n\tzone=" + z).printStackTrace(platu.Main.out);
            Common.getChar();
        }
    }

    /**
     * Allocate and clear the new space for a set of timers.
     * @param initEnSet
     */
    private void extend(ArrayList<LinkedList<LPNTran>> initEnSet) {
        for (LinkedList<LPNTran> list : initEnSet) {
            //extend DMB with new timers
            for (LPNTran t : list) {
                if (!containsKey(t.getID())) {
                    allocate(t.getID());
                }
            }
        }
        for (LinkedList<LPNTran> list : initEnSet) {
            for (LPNTran i : list) {
                //Mi0 = M0i = 0;
                put(i.getID(), 0, 0);
                put(0, i.getID(), 0);
                for (LPNTran k : list) {
                    //Mik = Mki = 0;
                    put(i.getID(), k.getID(), 0);
                    put(k.getID(), i.getID(), 0);
                }
            }
        }
    }

    /**
     * Set (0,t) to the upper bound of t, for all t.
     * @param initEnSet
     */
    private void advanceTime(ArrayList<LinkedList<LPNTran>> initEnSet) {
        //advance time
        for (LinkedList<LPNTran> list2 : initEnSet) {
            for (LPNTran t : list2) {
                boolean inserted = put(0, t.getID(), t.getDelayUB());
                if (!inserted) {
                    //check for errors
                    new Exception("zone.put==false: lpnt=" + t + "\n\tzone=" + this).printStackTrace(platu.Main.out);
                    Common.getChar();
                }
            }
        }
    }

    /**
     * Set (0,t) to the upper bound of t, for all t.
     * @param nextSet
     * @param UB
     * @param zone
     */
    static final private void advanceTime(int[] nextSet, int[] UB, Zone zone) {
        int idx = 0;
        for (int t : nextSet) {
            boolean b = zone.put(0, t, UB[idx++]);
            if (!b) {//check for errors
                new Exception("zone.put==false: lpnt=" + t + "\n\tzone=" + zone).printStackTrace(platu.Main.out);
                Common.getChar();
            }
        }
    }

    /**
     * Form a nice string to show an array of LPNTrans.
     * @param trans
     * @return
     */
    private static String toString(LPNTran[] trans) {
        String ret = "";
        for (LPNTran t : trans) {
            if (t != null) {
                ret += t.getID() + ", ";
            }
        }
        return ret;
    }

    /*
     * Regardless of implementation, a equals b implies b equals a and vice versa.
     * The hash of a and b must also equal.
     */
    static public boolean equals(final Zone a, final Zone b) {
        if (a.keySet() instanceof int[]) {
            for (int i : (int[]) a.keySet()) {
                for (int j : (int[]) a.keySet()) {
                    if (b.get(i, j) != a.get(i, j)) {//!!
                        return false;
                    }
                }
            }
        } else if (a.keySet() instanceof Collection) {
            for (int i : (Collection<Integer>) a.keySet()) {
                for (int j : (Collection<Integer>) a.keySet()) {
                    if (b.get(i, j) != a.get(i, j)) {//!!
                        return false;
                    }
                }
            }
        } else {
            final MatrixZoneVer3Impl za = (MatrixZoneVer3Impl) a;
            return za.equals(b);
        }
        return true;
    }

    /**
     * Determines if a tmpTimer is enabled.
     * @param zone
     * @param delayLB
     * @param label
     * @return
     */
    static public boolean isTimeEnabled(Zone zone, int delayLB, int label) {
        if (delayLB == 0) {
            return true;
        }
        Integer timer = zone.get(0, label);
        return timer == null ? true : delayLB <= timer;
    }

    public static final Collection<Object> toList(Object[] arr) {
        Set<Object> l = new HashSet<Object>(1);
        l.addAll(Arrays.asList(arr));
        return l;
    }

    public static final Collection<Object> toList(int[] arr) {
        Collection<Object> l = new HashSet<Object>(1);
        l.addAll(Arrays.asList(arr));
        return l;
    }

    public static final Object[] toArray(Collection<Object> set) {
        Object[] arr = new Object[set.size()];
        int idx = 0;
        for (Object i : set) {
            arr[idx++] = i;
        }
        return arr;
    }

    static void pr(Object o) {
        if (Main.ZONE_ENABLE_PRINT) {
            out.println(o);
        }
    }

    public void projectExtraneous(
            Zone zone,
            LinkedList[] curEnabledArray,
            LinkedList[] nextEnabledList,
            LPNTran firedTran) {
        //differenceSet = currentEnabledTransitions - nextEnabledTransitions
        //remove un-needed timers from zone
        //essentially: nextZone = currentZone  - (currentEnabledTransitions - nextEnabledTransitions)
        final LpnTranList diffEnabledTransitions = new LpnTranList();
        for (LinkedList<LPNTran> tList : curEnabledArray) {
            diffEnabledTransitions.addAll(tList);
        }
        for (LinkedList<LPNTran> tList : nextEnabledList) {
            diffEnabledTransitions.removeAll(tList);
        }
        for (LPNTran t : diffEnabledTransitions) {
            if (firedTran == t) {
                continue;
            }
            zone.project(t.getID());
        }
    }

    /**
     *
     *   differenceSet = currentEnabledTransitions - nextEnabledTransitions
     *   remove un-needed timers from zone
     *   essentially: nextZone = currentZone  - (currentEnabledTransitions - nextEnabledTransitions)
     * @param zone
     * @param firedLocalTran
     * @param nextEnabledList
     * @param curModuleTranSet
     */
    private static void projectExtraneous(Zone zone, LPNTran firedLocalTran,
            LPNTran[][] nextEnabledList, LPNTran[][] curModuleTranSet) {
        //differenceSet = currentEnabledTransitions - nextEnabledTransitions
        //remove un-needed timers from zone
        //essentially: nextZone = currentZone  - (currentEnabledTransitions - nextEnabledTransitions)
        LpnTranList diffEnabledTransitions = new LpnTranList();
        for (LPNTran[] tList : curModuleTranSet) {
            diffEnabledTransitions.addAll(Arrays.asList(tList));
        }
        for (LPNTran[] tList : nextEnabledList) {
            diffEnabledTransitions.removeAll(Arrays.asList(tList));
        }
        for (LPNTran t : diffEnabledTransitions) {
            if (firedLocalTran == t) {
                continue;
            }
            zone.project(t.getID());
        }
    }

    /**
     * @return the NEXT_ID
     */
    final public static int newID() {
        return NEXT_ID++;
    }
}
