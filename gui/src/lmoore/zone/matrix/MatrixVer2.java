/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lmoore.zone.matrix;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import platu.Main;
import platu.Common;
import static lmoore.zone.Zone.INF;

/**
 *
 * @author ldtwo
 */
public class MatrixVer2 {

    static protected int INIT_SIZE = 2;
    public int[][] matrix;
    protected boolean[] isFree;
//    private Deque<Integer> freeList=new ArrayDeque<Integer>();
    protected IntIntOpenHashMap keyToIndex;
    protected int numFree;
    protected float GROW_FACTOR = 1f;
    protected boolean AUTO_GROW;

    /**
     *
     * @param initialCapacity
     */
    public MatrixVer2(final int initialCapacity) {
        INIT_SIZE = initialCapacity;
        matrix = new int[INIT_SIZE][INIT_SIZE];
        numFree = INIT_SIZE;
        isFree = new boolean[INIT_SIZE];
        Arrays.fill(isFree, true);
        keyToIndex = new IntIntOpenHashMap(INIT_SIZE);
//        for(int i=0;i<INIT_SIZE;i++)freeList.push(i);
        this.AUTO_GROW = true;
    }

    /**
     *
     */
    public MatrixVer2() {
        matrix = new int[INIT_SIZE][INIT_SIZE];
        numFree = INIT_SIZE;
        isFree = new boolean[INIT_SIZE];
        Arrays.fill(isFree, true);
        keyToIndex = new IntIntOpenHashMap(INIT_SIZE);
        this.AUTO_GROW = true;
    }

    /**
     *
     * @param other
     */
    public MatrixVer2(MatrixVer2 other) {
        try {
            INIT_SIZE = other.size();
            matrix = other.cloneMatrix();
            numFree = other.numFree;
            isFree = other.cloneIsFree();
            keyToIndex = other.cloneKeyMap();
            this.AUTO_GROW = true;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(MatrixVer2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param initialCapacity
     * @param AUTO_GROW
     */
    public MatrixVer2(final int initialCapacity, boolean AUTO_GROW) {
        INIT_SIZE = initialCapacity;
        matrix = new int[INIT_SIZE][INIT_SIZE];
        numFree = INIT_SIZE;
        isFree = new boolean[INIT_SIZE];
        Arrays.fill(isFree, true);
        keyToIndex = new IntIntOpenHashMap(INIT_SIZE);
        this.AUTO_GROW = AUTO_GROW;
    }

    /**
     *
     * @param AUTO_GROW
     */
    public MatrixVer2(boolean AUTO_GROW) {
        matrix = new int[INIT_SIZE][INIT_SIZE];
        numFree = INIT_SIZE;
        isFree = new boolean[INIT_SIZE];
        Arrays.fill(isFree, true);
        keyToIndex = new IntIntOpenHashMap(INIT_SIZE);
        this.AUTO_GROW = AUTO_GROW;
    }

    /**
     *
     * @param other
     * @param AUTO_GROW
     */
    public MatrixVer2(MatrixVer2 other, boolean AUTO_GROW) {
        try {
            INIT_SIZE = other.size();
            matrix = other.cloneMatrix();
            numFree = other.numFree;
            isFree = other.cloneIsFree();
            keyToIndex = other.cloneKeyMap();
            this.AUTO_GROW = AUTO_GROW;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(MatrixVer2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return
     */
    final public int size() {
        return keyToIndex.size();
    }

    /**
     *
     * @return
     */
    final public int free() {
        return numFree;
    }

    @Override
    public Object clone() {
        return new MatrixVer2(this);
    }

    /**
     *
     * @return
     */
    final public int[][] cloneMatrix() {
        return Arrays.copyOf(matrix, isFree.length);
    }

    /**
     *
     * @return
     */
    final public boolean[] cloneIsFree() {
        return Arrays.copyOf(isFree, isFree.length);
    }

    /**
     *
     * @return
     */
    final public IntIntOpenHashMap cloneKeyMap() throws CloneNotSupportedException {
//        return (IntIntOpenHashMap) keyToIndex.clone();
        return new IntIntOpenHashMap(keyToIndex);
    }

    /**
     *
     */
    final public void grow() {
        final int newSize = isFree.length > 1 ? (int) (isFree.length * GROW_FACTOR+1) : 1;
        final int oldSize = isFree.length;
        numFree = numFree + newSize - isFree.length;
        int[][] oldMatrix = matrix;
        if (true) {//version 3
            matrix = new int[newSize][newSize];
            for (int i = 0; i < oldSize; i++) {
                System.arraycopy(oldMatrix[i], 0, matrix[i], 0, oldSize);
            }
        } else {
            matrix = new int[newSize][];
            for (int i = 0; i < oldSize; i++) {
                matrix[i] = Arrays.copyOf(oldMatrix[i], newSize);
            }
            for (int i = oldSize; i < newSize; i++) {
                matrix[i] = new int[newSize];
            }
        }
        isFree = Arrays.copyOf(isFree, newSize);
        for (int x = oldSize; x < newSize; x++) {
            isFree[x] = true;
        }
    }

    /**
     *
     */
    final public void shrink() {
        final int newSize = isFree.length - numFree;
        numFree = 0;
        matrix = Arrays.copyOf(matrix, newSize);
        isFree = Arrays.copyOf(isFree, newSize);
    }
    private int idx1, idx2;

    /**
     *
     * @param label
     */
    final public void remove(int label) {//!!
        try {
            idx1 = keyToIndex.get(label);
            keyToIndex.remove(label);
            isFree[idx1] = true;
            //Arrays.fill(matrix[idx1], 0);
            for (int i = 0; i < isFree.length; i++) {
                matrix[idx1][i] = 0;
                matrix[i][idx1] = 0;
            }

            numFree++;
        } catch (Exception e) {
//            System.err.printf("ERROR: cannot remove %s; idx=%s; free?=%s; num free=%s\n",
//                    label, idx1, isFree[idx1], numFree);
//            System.err.println("keyToIndex:\t" + keyToIndex.toString());
            // e.printStackTrace();
        }
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    final public int get(int row, int col) {
        idx1 = keyToIndex.get(row);
        idx2 = keyToIndex.get(col);
        if(idx1<0||idx2<0){
            System.out.printf("IDX: 1=%s, 2=%s\n", idx1,idx2);
            System.out.printf("@ r=%s, c=%s\n", row,col);
//            System.out.printf("%s\n",Arrays.toString(((ArrayList)keyToIndex.entrySet()).toArray()));
        }
        return matrix[idx1][idx2];
    }

    /**
     *
     * @param row
     * @param col
     * @param val
     * @return
     */
    final public boolean put(int row, int col, int val) {
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- put(row=" + row + ", col=" + col + ", val=" + val + ")");
        }
        if (!keyToIndex.containsKey(col)) {
            allocate(col);
        }
        if (!keyToIndex.containsKey(row)) {
            allocate(row);
        }
        try {
            idx1 = keyToIndex.get(row);
            idx2 = keyToIndex.get(col);
            matrix[idx1][idx2] = val;
            return true;
        } catch (Exception e) {
            System.err.println("ERROR: cannot put(" + row + ", " + col + ") <- " + val);
            System.err.println("       matrix[" + idx1 + ", " + idx2 + "] <- " + val);
            System.err.println("size=" + size() + ";\t free=" + numFree);
            System.err.println(this);
            e.printStackTrace();
            Common.getChar();
        }
        return false;
    }

    /**
     * may throw NumberFormatException, ArrayIndexOutOfBoundsException
     * @param row
     * @param col
     * @param val
     * @return
     */
    final public boolean put_unchecked(int row, int col, int val) {
        idx1 = keyToIndex.get(row);
        idx2 = keyToIndex.get(col);
        matrix[idx1][idx2] = val;
        return true;
    }

    /**
     *    matrix[row][col] = val;
     * @param row
     * @param col
     * @param val
     */
    final public void put_direct_unchecked(int row, int col, int val) {
        matrix[row][col] = val;
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    final public int get_direct_unchecked(int row, int col) {
        return matrix[row][col];
    }

    /**
     *   return keyToIndex.get(label);
     * @param label
     * @return
     */
    final public int getIndex_unchecked(int label) {
        return keyToIndex.get(label);
    }

    /**
     *
     * @param o
     */
    protected static void pr(Object o) {
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
        //

//        if (Main.ZONE_ENABLE_PRINT) {
//            String fr = "";
//            for (boolean b : isFree) {
//                fr += b ? "1 " : "0 ";
//            }
//            pr("----------------------------------------------------- allocate(" + label + ")");
//            System.out.printf("free=%s;\tsize=%s;\tkeytoindex=%s;\n\tFREE: %s\n",
//                    numFree, size(), keyToIndex.toString(), fr);
//        }
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
//        if (keyToIndex.containsKey(label)) {
//            System.out.println("WARNING:  " + label + " was already allocateed!");
//            return;
//        } else
        {
//            freeList.pop();
//           tmp= freeList.pop();
//           isFree[tmp]=false;
//           keyToIndex.put(label, tmp);
//           numFree--;
            for (int x = 0;; x++) {//inf loop is needed. If an error occurs, it is not here
                if (isFree[x]) {
                    isFree[x] = false;
                    keyToIndex.put(label, x);
                    numFree--;
                    return;
                }
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
    final public Set<Integer> keySet() {
//        return keyToIndex.keySet();
        return new HashSet<Integer>(new LinkedList(Arrays.asList(keyToIndex.keys)));
    }

    /**
     *
     * @return
     */
    final public TreeSet<Integer> treeKeySet() {
        return new TreeSet<Integer>(keySet());
    }

    /**
     *
     * @return
     */
    final public Iterator<Integer> keyIterator() {
        return keySet().iterator();
//        return keyToIndex.keySet().iterator();
    }

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

    /**
     *
     * @return
     */
    @Override
    final public String toString() {
        NavigableSet<Integer> treeSet = treeKeySet();
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
            idx1 = keyToIndex.get(row);
            Iterator<Integer> colIter = treeSet.iterator();
            ret += format1(row + "|");
            while (colIter.hasNext()) {
                col = colIter.next();
                idx2 = keyToIndex.get(col);
                try {
                    ret += format2(matrix[idx1][idx2] > 0.9 * INF ? "INF" : matrix[idx1][idx2] + "");
                } catch (Exception e) {
                    String fr = "";
                    for (boolean b : isFree) {
                        fr += b ? "1  " : "0  ";
                    }
                    System.err.printf("ERROR:%s\t%s\n\t row,col=(%s,%s);\tidx=(%s,%s);"
                            + "\tnum free=%s;\tsize=%s;\tlen=%s,%s\n",
                            e.getLocalizedMessage(), e.getCause(), row, col, idx1, idx2,
                            numFree, size(), matrix.length, matrix[idx1].length);
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
    long hash = -1, hc;

    /**
     *
     * @return
     */
    public long getLongHash() {
        return hash;
    }

    /**
     *
     * @return
     */
    @Override
    final public int hashCode() {
//        if (hash != -1) {
//            return (int) hash;
//        }
        hash = Integer.MIN_VALUE;
        NavigableSet<Integer> treeSet = treeKeySet();
        for (int i : treeSet) {
            hash += Long.rotateLeft(i, i % 31);
        }
//        Iterator<Integer> rowIter = treeSet.iterator();
//        int row, col;
//        while (rowIter.hasNext()) {
//            row = rowIter.next();
//            idx1 = keyToIndex.get(row);
//            Iterator<Integer> colIter = treeSet.iterator();
//            while (colIter.hasNext()) {
//                col = colIter.next();
//                idx2 = keyToIndex.get(col);
//                hash += Integer.rotateLeft(matrix[idx1][idx2], matrix[idx1][idx2]%31);
//            }
//        }
        return (int) hash;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    final public boolean equals(Object o) {
        if (!(o instanceof MatrixVer2)) {
            return false;
        }
        MatrixVer2 otherMatrix = (MatrixVer2) o;
        if (this == otherMatrix) {
            return true;
        }
//        if (size() != otherMatrix.size()) {
//            return false;
//        }

        NavigableSet<Integer> treeSet = treeKeySet();
//        pr("==================================================  +");
//        pr(otherMatrix);
//        pr("\n");
//        pr(this);

//        if (hash == otherMatrix.getLongHash()) {
//            boolean eq = true;
//            int idx1_other, idx2_other;
//            for (int i : treeSet) {
//                idx1 = keyToIndex.get(i);
//                idx1_other = otherMatrix.getIndex_unchecked(i);
//                for (int j : treeSet) {
//                    idx2 = keyToIndex.get(j);
//                    idx2_other = otherMatrix.getIndex_unchecked(j);
//                    if (matrix[idx1][idx2] != otherMatrix.matrix[idx1_other][idx2_other]) {
//                        //pr("false\n==================================================  -");
//                        //Common.getChar();
//                        eq = false;
//                    }
//                }
//            }
//            if (!eq) {
//                pr("==================================================  +");
//                pr(hash + "\t" + otherMatrix.getLongHash());
//                pr(otherMatrix);
//                pr("\n");
//                pr(this);
//                pr("false\n==================================================  -");
//                Common.getChar();
//
//            }
//
//            return true;
//        }
        int idx1_other, idx2_other;
        for (int i : treeSet) {
            idx1 = keyToIndex.get(i);
            idx1_other = otherMatrix.getIndex_unchecked(i);
            for (int j : treeSet) {
                if (i == j) {
                    continue;
                }
                idx2 = keyToIndex.get(j);
                idx2_other = otherMatrix.getIndex_unchecked(j);
                if (matrix[idx1][idx2] != otherMatrix.matrix[idx1_other][idx2_other]) {
                    //pr("false\n==================================================  -");
                    //Common.getChar();
                    return false;
                }
            }
        }
        //pr("true\n==================================================  -");
        //Common.getChar();
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

        NavigableSet<Integer> treeSet = treeKeySet();
        String ret = "<TR><TD></TD>";
        for (Integer i : treeSet) {
            ret += "<TD>" + i + "</TD>";
        }
        ret += "</TR>";

        Iterator<Integer> rowIter = treeSet.iterator();

        int row, col;
        while (rowIter.hasNext()) {
            row = rowIter.next();
            idx1 = keyToIndex.get(row);
            Iterator<Integer> colIter = treeSet.iterator();
            ret += "<TR><TD>" + row + "</TD>";

            while (colIter.hasNext()) {
                col = colIter.next();
                idx2 = keyToIndex.get(col);
//                ret += format(matrix[idx1][idx2] + "");
                ret += "<TD >" + (matrix[idx1][idx2] >= INF * 0.9 ? "INF" ://
                        (matrix[idx1][idx2] < 0.9 * -INF ? "-INF" : matrix[idx1][idx2])//
                        ) + "</TD>";
            }
            ret += "</TR>";
        }
        return ret;
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    public boolean isInf(int row, int col) {
        try {
            if (get(row, col) >= INF * 0.9) {
                put(row, col, INF);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    public boolean isNegInf(int row, int col) {
        try {
            if (get(row, col) <= -INF * 0.9) {
                put(row, col, Integer.MIN_VALUE);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
