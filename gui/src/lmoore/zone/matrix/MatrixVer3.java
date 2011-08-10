/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lmoore.zone.matrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import lmoore.zone.Zone;
import lmoore.zone.impl.MatrixZoneVer3Impl;
import platu.Main;
import static lmoore.zone.Zone.INF;
import static com.carrotsearch.hppc.IntIntOpenHashMap.*;

/**
 *
 * @author ldtwo
 */
public class MatrixVer3 extends Zone {

    private static final long serialVersionUID = 1982764398127643982L;
    static protected int INIT_SIZE = 10;
    public int[] matrix;
    transient protected boolean[] isFree;
    transient protected IntIntOpenHashMap keyToIndex;
    transient protected int numFree;
    transient protected float GROW_FACTOR = 1f;
    transient protected boolean AUTO_GROW;
    protected int size = 0;
    public static long sumOfSize = 0;
    public static long clones = 0;
    public static long estMemory = 0;

    /**
     *
     * @param initialCapacity
     */
    public MatrixVer3(final int initialCapacity) {
        INIT_SIZE = initialCapacity;
        matrix = new int[INIT_SIZE * INIT_SIZE];
        numFree = INIT_SIZE;
        isFree = new boolean[INIT_SIZE];
        Arrays.fill(isFree, true);
        keyToIndex = new IntIntOpenHashMap(INIT_SIZE);
        this.AUTO_GROW = true;
        size = INIT_SIZE;
    }

    /**
     *
     */
    public MatrixVer3() {
        this(INIT_SIZE);
    }

    public MatrixVer3(Zone zone) {
        this(zone.size());
        size = zone.size();
    }

    /**
     *
     * @param other
     */
    public MatrixVer3(MatrixVer3 other) {
//        INIT_SIZE = other.size();
//        matrix = other.cloneMatrix();
//        numFree = other.numFree;
//        isFree = other.cloneIsFree();
//        keyToIndex = other.cloneKeyMap();
        this.AUTO_GROW = true;
//        size = isFree.length;
        copyToThis(other);
    }

    public MatrixVer3(MatrixZoneVer3Impl other) {
//        INIT_SIZE = other.size();
//        matrix = other.cloneMatrix();
//        numFree = other.numFree;
//        isFree = other.cloneIsFree();
//        keyToIndex = other.cloneKeyMap();
//        size = isFree.length;
        this.AUTO_GROW = true;
        copyToThis(other);
    }

    /**
     *
     * @param initialCapacity
     * @param AUTO_GROW
     */
    public MatrixVer3(final int initialCapacity, boolean AUTO_GROW) {
        INIT_SIZE = initialCapacity;
        matrix = new int[INIT_SIZE * INIT_SIZE];
        numFree = INIT_SIZE;
        isFree = new boolean[INIT_SIZE];
        Arrays.fill(isFree, true);
        keyToIndex = new IntIntOpenHashMap(INIT_SIZE);
        this.AUTO_GROW = AUTO_GROW;
        size = isFree.length;
    }

    /**
     *
     * @param AUTO_GROW
     */
    public MatrixVer3(boolean AUTO_GROW) {
        matrix = new int[INIT_SIZE * INIT_SIZE];
        numFree = INIT_SIZE;
        isFree = new boolean[INIT_SIZE];
        Arrays.fill(isFree, true);
        keyToIndex = new IntIntOpenHashMap(INIT_SIZE);
        this.AUTO_GROW = AUTO_GROW;
        size = isFree.length;
    }

    /**
     *
     * @param other
     * @param AUTO_GROW
     */
    public MatrixVer3(MatrixVer3 other, boolean AUTO_GROW) {
//        INIT_SIZE = other.size();
//        matrix = other.cloneMatrix();
//        numFree = other.numFree;
//        isFree = other.cloneIsFree();
//        keyToIndex = other.cloneKeyMap();
//        size = isFree.length;
        this.AUTO_GROW = AUTO_GROW;
        copyToThis(other);
    }

    /**
     *
     * @return
     */
    final public int size() {
        return size;
    }

    /**
     *
     * @return
     */
    final public int free() {
        return numFree;
    }

    final public void release() {
//        shrink();
//        keyToIndex.
//        isFree = null;
//        keyToIndex = null;
//isFinal=true;
    }

    /**
     *
     * @return
     */
    final public int[] cloneMatrix() {
//        return matrix;
        return Arrays.copyOf(matrix, matrix.length);
    }

    /**
     *
     * @return
     */
    final public boolean[] cloneIsFree() {
//        return isFree;
        return Arrays.copyOf(isFree, size);
    }

    /**
     *
     * @return
     */
    final public IntIntOpenHashMap cloneKeyMap() {
        IntIntOpenHashMap ret = new IntIntOpenHashMap(keyToIndex);
        return ret;
    }

    private void copyToThis(MatrixVer3 other) {

        HashSet<Integer> keys = new HashSet<Integer>(other.keySet().size() / 2);
        for (IntCursor i : other.keySet()) {
            keys.add(i.value);
        }
        NavigableSet<Integer> treeSet = new TreeSet<Integer>(keys);
        int new_size = treeSet.size();
        IntIntOpenHashMap new_keyToIndex = new IntIntOpenHashMap(new_size);
        int[] dbm = new int[new_size * new_size];
        int x = 0;
        for (int i : treeSet) {
            new_keyToIndex.put(i, x);
            idx1 = other.keyToIndex.get(i);
            int y = 0;
            for (int j : treeSet) {
                idx2 = other.keyToIndex.get(j);
                dbm[x * new_size + y] = other.matrix[idx1 * other.size + idx2];
                y++;
            }
            x++;
        }
        matrix = dbm;
        keyToIndex = new_keyToIndex;
        numFree = 0;

        isFree = new boolean[new_size];
        size = new_size;
        Arrays.fill(isFree, false);
//        System.out.printf("Copy: %s\n", Arrays.toString(isFree));
    }

    /**
     *
     */
    final public void grow() {
        final int newSize = size > 0 ? (int) (isFree.length * GROW_FACTOR + 1) : 1;
        final int oldSize = size;
        numFree = numFree + newSize - size;
        int[] oldMatrix = matrix;
        matrix = new int[newSize * newSize];
        for (int i = 0; i < oldSize; i++) {
            System.arraycopy(oldMatrix, i * oldSize, matrix, i * newSize, oldSize);
        }
//        System.out.printf("GROW: %s\n", Arrays.toString(isFree));
        isFree = Arrays.copyOf(isFree, newSize);
        for (int x = oldSize; x < newSize; x++) {
            setFree(x);
        }
//        System.out.printf("GROW': %s\n", Arrays.toString(isFree));
        size = isFree.length;
    }

    private void setFree(int x) {
        isFree[x] = true;
    }

    private void setNotFree(int x) {
        isFree[x] = false;
    }

    private void setKeyIndexPair(int label, int x) {
        keyToIndex.put(label, x);
    }

    /**
     *
     */
    final private void shrink() {
        if (numFree <= 0) {
            return;
        }
//        int count = 0;
//        for (boolean b : isFree) {
//            if (b) {
//                count++;
//            }
//        }
//        final int newSize = count;
//        final int oldSize = size;
//        numFree = count;
//        boolean[] old_isFree = isFree;
//        isFree = new boolean[newSize];
//        size = isFree.length;
//        Arrays.fill(isFree, true);
////        IntIntOpenHashMap old_keyToIndex = keyToIndex;
//       IntIntOpenHashMap new_keyToIndex = new IntIntOpenHashMap(newSize);
//        int[] oldMatrix = matrix;
//        matrix = new int[newSize * newSize];
//        int row, col;
//        for (int i = 0; i < old_keyToIndex.keys.length; i++) {
//            if (old_keyToIndex.states[i] == ASSIGNED) {
//                row = old_keyToIndex.keys[i];
////                treeSet.add(keyToIndex.keys[i]);
//                for (int j = 0; j < old_keyToIndex.keys.length; j++) {
//                    if (old_keyToIndex.states[j] == ASSIGNED) {
//                        col = old_keyToIndex.keys[j];
//int val=get(row, col, oldMatrix, old_keyToIndex, oldSize);
////                    System.err.printf("(%s,%s)...%s\n",row,col,get(row,col, oldMatrix, old_keyToIndex,old_isFree.length));
////                        put(row, col, get(row, col, oldMatrix, old_keyToIndex, old_isFree.length));
//
//
//        if (!keyToIndex.containsKey(col)) {
//            allocate(col);
//        }
//        if (!keyToIndex.containsKey(row)) {
//            allocate(row);
//        }
//        idx1 = keyToIndex.get(row);
//        idx2 = keyToIndex.get(col);
//        matrix[idx1 * newSize + idx2] = val;
//
//
//                    }
//                }
//            }
//        }

        HashSet<Integer> keys = new HashSet<Integer>(keySet().size() / 2);
        for (IntCursor i : keySet()) {
            keys.add(i.value);
        }
        NavigableSet<Integer> treeSet = new TreeSet<Integer>(keys);
        int new_size = treeSet.size();
        IntIntOpenHashMap new_keyToIndex = new IntIntOpenHashMap(new_size);
        int[] dbm = new int[new_size * new_size];
        int x = 0;
        for (int i : treeSet) {
            new_keyToIndex.put(i, x);
            idx1 = getIndex_unchecked(i);
            int y = 0;
            for (int j : treeSet) {
                idx2 = getIndex_unchecked(j);
                dbm[x * new_size + y] = matrix[idx1 * size + idx2];
                y++;
            }
            x++;
        }
        matrix = dbm;
        keyToIndex = new_keyToIndex;
        numFree = 0;

        isFree = new boolean[new_size];
        size = new_size;
        Arrays.fill(isFree, false);
    }
    static private int idx1, idx2;

    /**
     *
     * @param label
     */
    final public void remove(int label) {//!!
        if (!contains(label)) {
            return;
//              System.out.printf("R;%s",this);
//            throw new RuntimeException("CANNOT REMOVE ENTRY(" + label + "); it does not exist!");
        }
        if (label == 0) {
            throw new RuntimeException("You cannot remove 0 from the DBM");
        }
        idx1 = getIndex_unchecked(label);
        keyToIndex.remove(label);
        setFree(idx1);
        for (int i = 0; i < size; i++) {
            matrix[idx1 * size + i] = 0;
            matrix[i * size + idx1] = 0;
        }
        numFree++;
//        System.out.printf("REMOVE'(%s): Exists? %s\t%s\t%s\n", label,
//                contains(label),
//                Arrays.toString(isFree),
//                keySetToString());
    }

    /**get the value that is virtually located at row, col (although physically at idx1, idx2)
     *
     * @param row
     * @param col
     * @return
     */
    final public int get(int row, int col) {
//        if (row == col) {
//            return 0;
//        }
        idx1 = getIndex_unchecked(row);
        idx2 = getIndex_unchecked(col);
        return matrix[idx1 * size + idx2];
    }

    static final private int get(int row, int col, int[] matrix, IntIntOpenHashMap keyToIndex, int len) {
        if (row == col) {
            return 0;
        }
        idx1 = keyToIndex.get(row);
        idx2 = keyToIndex.get(col);
        return matrix[idx1 * len + idx2];
    }

    /**
     *
     * @param row
     * @param col
     * @param val
     * @return
     */
    @Override
    final public boolean put(int row, int col, int val) {
        if (!keyToIndex.containsKey(col)) {
            allocate(col);
        }
        if (!keyToIndex.containsKey(row)) {
            allocate(row);
        }
//        try {
        idx1 = getIndex_unchecked(row);
        idx2 = getIndex_unchecked(col);
        matrix[idx1 * size + idx2] = val;
        return true;
//        } catch (Exception e) {
//            System.err.println("ERROR: cannot put(" + row + ", " + col + ") <- " + val);
//            System.err.println("       matrix[" + idx1 + ", " + idx2 + "] <- " + val);
//            System.err.println("size=" + size() + ";\t free=" + numFree);
//            System.err.println(this);
//            e.printStackTrace();
//            Common.getChar();
//        }
//        return false;
    }

    /**
     * may throw NumberFormatException, ArrayIndexOutOfBoundsException
     * @param row
     * @param col
     * @param val
     * @return
     */
    final public boolean put_unchecked(int row, int col, int val) {
        idx1 = getIndex_unchecked(row);
        idx2 = getIndex_unchecked(col);
        matrix[idx1 * size + idx2] = val;
        return true;
    }

    /**
     *    matrix[row][col] = val;
     * @param idx1
     * @param idx2
     * @param val
     */
    final public void put_direct_unchecked(int idx1, int idx2, int val) {
        matrix[idx1 * size + idx2] = val;
    }

    /**get the value that is physically at idx1, idx2
     *
     * @param idx1
     * @param idx2
     * @return
     */
    final public int get_direct_unchecked(int idx1, int idx2) {
        return matrix[idx1 * size + idx2];
    }

    /**get the physical index for a key
     *
     * @param rowOrCol
     * @return keyToIndex.get(label);
     */
    final public int getIndex_unchecked(int rowOrCol) {
        return keyToIndex.get(rowOrCol);
    }

    /**
     *
     * @param o
     */
    final protected static void pr(Object o) {
        //if (Main.ZONE_ENABLE_PRINT)
        {
            System.out.println(o);
        }
    }

    /**
     *  if (numFree lessThan 1) {
    grow();
    }
    idx1 = -1;
    if (keyToIndex.containsKey(label)) {
    idx1 = keyToIndex.get(label);
    } else {
    for (int x = 0; x < matrix.length; x++) {
    if (isFree[x]) {
    isFree[x] = false;
    keyToIndex.put(label, x);
    numFree--;
    }
    }
    }
     * @param label
     */
    final public void allocate(int label) {
//        System.out.printf("Alloc: %s\n", Arrays.toString(isFree));
        if (numFree < 1) {
            if (Main.ZONE_ENABLE_PRINT) {
                String fr = "";
                for (boolean b : isFree) {
                    fr += b ? "1 " : "0 ";
                }
                System.out.printf("GROWING: free=%s;\tsize=%s;\tkeytoindex=%s\n\tFREE: %s\n",
                        numFree, size(), keyToIndex.toString(), fr);
            }
            grow();
        }
        /**
         * Since we have already confirmed that numFree >= 1,
         * we can just loop without checking bounds until we find the first spot open
         */
        for (int x = 0;; x++) {//inf loop is needed. If an error occurs, it is not here
            if (isFree[x]) {
                setNotFree(x);
                setKeyIndexPair(label, x);
                numFree--;
                return;
            }
        }

    }

    /**
     *
     * @param label
     * @return
     */
    final public boolean contains(int label) {
        return keyToIndex.containsKey(label);
    }

    /**
     *
     * @return
     */
    final public IntIntOpenHashMap.KeySet keySet() {
        return keyToIndex.keySet();
    }

    final public String keySetToString() {
        String s = "";
        HashSet<Integer> keys = new HashSet<Integer>(keySet().size() / 2);
        for (IntCursor i : keySet()) {
            keys.add(i.value);
        }
        NavigableSet<Integer> treeSet = new TreeSet<Integer>(keys);
        Iterator<Integer> rowIter = treeSet.iterator();
        int row;
        while (rowIter.hasNext()) {
            row = rowIter.next();
            idx1 = getIndex_unchecked(row);
            s += String.format("(%s-->%s),", row, idx1);
        }
        return s;
    }

    final public int[] keys() {
        return keyToIndex.keys;
    }

    final public int key(int i) {
        return keyToIndex.keys[i];
    }

    final public int[] values() {
        return keyToIndex.values;
    }

    final public byte[] states() {
        return keyToIndex.states;
    }

    final public boolean notAssigned(int i) {
        return keyToIndex.states[i] != ASSIGNED;
    }

//    /**
//     *
//     * @return
//     */
//    final public TreeSet<Integer> treeKeySet() {
//        return new TreeSet<Integer>(keyToIndex.keySet());
//    }
//
//    /**
//     *
//     * @return
//     */
//    final public Iterator<Integer> keyIterator() {
//        return keyToIndex.keySet().iterator();
//    }
    /**
     *
     * @param s
     * @return
     */
    final private String format1(String s) {
        return String.format("%5s ", s);
    }

    /**
     *
     * @param s
     * @return
     */
    final private String format2(String s) {
        return String.format("%3s ", s);
    }

    public final int[] canonicalDBM() {

        HashSet<Integer> keys = new HashSet<Integer>(keySet().size() / 2);
        for (IntCursor i : keySet()) {
            keys.add(i.value);
        }
        NavigableSet<Integer> treeSet = new TreeSet<Integer>(keys);
        int new_size = treeSet.size();
        int[] dbm = new int[new_size * new_size];
        int x = 0;
        for (int i : treeSet) {
            idx1 = getIndex_unchecked(i);
            int y = 0;
            for (int j : treeSet) {
                idx2 = getIndex_unchecked(j);
                dbm[x * new_size + y] = matrix[idx1 * size + idx2];
                y++;
            }
            x++;
        }



        return dbm;
    }

    /**
     *
     * @return
     */
    @Override
    final public String toString() {

        NavigableSet<Integer> treeSet = new TreeSet<Integer>();
        for (IntCursor i : keySet()) {
            treeSet.add(i.value);
        }
        Iterator<Integer> rowIter = treeSet.iterator();
        String ret = format1("|");
        String s2 = "";
        for (int i : treeSet) {
            s2 += format2(i + "");
        }
        ret = ret + s2.replace(" ", "_") + "__\n";
        int row, col;
        while (rowIter.hasNext()) {
            row = rowIter.next();
            idx1 = getIndex_unchecked(row);
            Iterator<Integer> colIter = treeSet.iterator();
            ret += format1(row + "|");
            while (colIter.hasNext()) {
                col = colIter.next();
                idx2 = getIndex_unchecked(col);
                try {
//                    System.out.printf("POS[%s\t%s]\tRC(%s\t%s)\tVal[%s]=%s\t\n",
//                            idx1, idx2, row, col, idx1 * size + idx2, matrix[idx1 * size + idx2]);
//                    if (idx1 < 0 || idx2 < 0) {
//                    }
                    ret += format2(matrix[idx1 * size + idx2] > 0.9 * INF
                            ? "INF" : matrix[idx1 * size + idx2] + "");
                } catch (Exception e) {
                    String fr = "";
                    for (boolean b : isFree) {
                        fr += b ? "1  " : "0  ";
                    }
//                    System.err.printf("ERROR:%s\t%s\n\t row,col=(%s,%s);\tidx=(%s,%s);"
//                            + "\tnum free=%s;\tsize=%s;\tlen=%s,%s\n",
//                            e.getLocalizedMessage(), e.getCause(), row, col, idx1, idx2,
//                            numFree, size(), matrix.length, matrix[idx1].length);
                    System.err.println("keyToIndex:\t" + keyToIndex.toString());
                    System.err.println("Free      :\t" + fr);
                    e.printStackTrace();
                    System.err.println("...\n\n");
                }
            }
            ret += " |\n";
        }
        return ret;
        // return super.toString().replace("\r", "  |").replace(",", "|\n").replace("{", "  |").replace("}", "");
    }

    /**
     *
     * @return
     */
    public long getLongHash() {
        return 1;
    }

    static protected int hash(Object key) {
        // same as JDK 1.4
        int h =
                (key instanceof int[])
                ? Arrays.hashCode((int[]) key)
                : key.hashCode();
        h += ~(h << 9);
        h ^= (h >>> 14);
        h += (h << 4);
        h ^= (h >>> 10);
        return h;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 216613626;
        int j = 0;
        for (int i : canonicalDBM()) {
            int h = 0x7fffffff + ~i;
            h += ~(h << 15);
            h ^= (h >>> 14);
            h += (h << 6);
            h ^= (h >>> 10);
            hash ^= h;
            hash = Integer.rotateLeft(hash, j % 31);
            j++;
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        Zone otherMatrix = (Zone) o;
        int row, col;
        for (int i = 0; i < keyToIndex.keys.length; i++) {
            if (keyToIndex.states[i] == ASSIGNED) {
                for (int j = i + 1; j < keyToIndex.keys.length; j++) {
                    if (keyToIndex.states[j] == ASSIGNED) {
                        row = keyToIndex.keys[i];
                        col = keyToIndex.keys[j];
                        if (get(row, col) != otherMatrix.get(row, col)) {
                            return false;
                        }
                        if (get(col, row) != otherMatrix.get(col, row)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     *
     * @param label
     * @return
     */
    public boolean containsKey(int label) {
        return keyToIndex.containsKey(label);
    }

    /**
     *
     * @return
     */
    public String toHTMLTable() {

        String ret = "<TR><TD></TD>";
//        NavigableSet<Integer> treeSet = treeKeySet();
//        for (Integer i : treeSet) {
//            ret += "<TD>" + i + "</TD>";
//        }
//        ret += "</TR>";
//
//        Iterator<Integer> rowIter = treeSet.iterator();
//
//        int row, col;
//        while (rowIter.hasNext()) {
//            row = rowIter.next();
//            idx1 = keyToIndex.get(row);
//            Iterator<Integer> colIter = treeSet.iterator();
//            ret += "<TR><TD>" + row + "</TD>";
//
//            while (colIter.hasNext()) {
//                col = colIter.next();
//                idx2 = keyToIndex.get(col);
////                ret += format(matrix[idx1][idx2] + "");
//                ret += "<TD >" + (matrix[idx1*isFree.length+idx2] >= INF * 0.9 ? "INF" ://
//                        (matrix[idx1*isFree.length+idx2] < 0.9 * -INF ? "-INF" : matrix[idx1*isFree.length+idx2])//
//                        ) + "</TD>";
//            }
//            ret += "</TR>";
//        }
        return ret;
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    final public boolean isInf(int row, int col) {
        if (get(row, col) >= INF * 0.9) {
            put(row, col, INF);
            return true;
        }
        return false;
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    final public boolean isNegInf(int row, int col) {
        if (get(row, col) <= -INF * 0.9) {
            put(row, col, Integer.MIN_VALUE);
            return true;
        }
        return false;
    }

    @Override
    public void restrict(int transIndex, int low) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void project(int timedID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void recanonicalize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isTimeEnabled(int delayLB, int label) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Zone clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String digest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void validate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File serialize(String filename) throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getID() {
        return ID;
    }
//    private void writeObject(ObjectOutputStream out) throws IOException {
////    out.defaultWriteObject();
//        out.writeInt(matrix.length);
//        for (int i = 0; i < matrix.length; i++) {
//            for (int j = 0; j < matrix.length; j++) {
//                out.writeInt(matrix[i][j]);
//            }
//            out.writeBoolean(isFree[i]);
//        }
//        out.writeInt(numFree);
//        out.writeFloat(GROW_FACTOR);
//        out.writeBoolean(AUTO_GROW);
//        for (Entry<Integer, Integer> e : keyToIndex.entrySet()) {
//            out.writeInt(e.getKey());
//            out.writeInt(e.getValue());
//        }
//    }
//
//    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
////    in.defaultReadObject();
//        int length = in.readInt();
//        matrix = new int[length][length];
//        for (int i = 0; i < length; i++) {
//            for (int j = 0; j < length; j++) {
//                matrix[i][j] = in.readInt();
//            }
//            isFree[i] = in.readBoolean();
//        }
//        numFree = in.readInt();
//        GROW_FACTOR = in.readFloat();
//        AUTO_GROW = in.readBoolean();
//        length = in.readInt();
//        keyToIndex=new IntIntOpenHashMap(length);
//        for (int i = 0; i < length; i++) {
//         keyToIndex.put(in.readInt(), in.readInt());
//        }
//    }
}
