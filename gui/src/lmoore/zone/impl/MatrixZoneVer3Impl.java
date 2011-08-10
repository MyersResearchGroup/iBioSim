/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lmoore.zone.impl;

import java.util.HashSet;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Collection;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import java.io.*;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.zip.Deflater;

import lmoore.zone.Zone;
import lmoore.zone.impl.HashedHashedMapZoneImpl;
import lmoore.zone.matrix.MatrixVer3;

import org.apache.hadoop.io.compress.zlib.BuiltInZlibDeflater;
import platu.Main;
import static com.carrotsearch.hppc.IntIntOpenHashMap.*;

/**
 *
 * @author ldtwo
 */
public class MatrixZoneVer3Impl extends MatrixVer3 {
//static{System.out.println("USING int PRIMITIVES: size="+int.SIZE/8+" bytes; MAX="+int.MAX_VALUE);}

    private void writeObject(ObjectOutputStream out) throws IOException {
//        out.defaultWriteObject();
        out.writeInt(matrix.length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                out.writeInt(matrix[i * isFree.length + j]);
            }
            out.writeBoolean(isFree[i]);
        }
        out.writeInt(numFree);
        out.writeFloat(GROW_FACTOR);
        out.writeBoolean(AUTO_GROW);
        out.writeInt(keyToIndex.size());
        Iterator<IntIntCursor> iteri = keyToIndex.iterator();
        for (IntIntCursor i = iteri.next();
                iteri.hasNext();
                i = iteri.next()) {
            out.writeInt(i.key);
            out.writeInt(i.value);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//        in.defaultReadObject();
        int length = in.readInt();
        matrix = new int[length * length];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                matrix[i * isFree.length + j] = in.readInt();
            }
            isFree[i] = in.readBoolean();
        }
        numFree = in.readInt();
        GROW_FACTOR = in.readFloat();
        AUTO_GROW = in.readBoolean();
        length = in.readInt();
        keyToIndex = new IntIntOpenHashMap(length);
        for (int i = 0; i < length; i++) {
            keyToIndex.put(in.readInt(), in.readInt());
        }
    }

    public void writeObject(DataOutputStream out) throws IOException {
//        out.defaultWriteObject();
        out.writeInt(matrix.length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                out.writeInt(matrix[i * isFree.length + j]);
            }
            out.writeBoolean(isFree[i]);
        }
        out.writeInt(numFree);
        out.writeFloat(GROW_FACTOR);
        out.writeBoolean(AUTO_GROW);
        out.writeInt(keyToIndex.size());
        Iterator<IntIntCursor> iteri = keyToIndex.iterator();
        for (IntIntCursor i = iteri.next();
                iteri.hasNext();
                i = iteri.next()) {
            out.writeInt(i.key);
            out.writeInt(i.value);
        }
    }

    public void readObject(DataInputStream in) throws IOException, ClassNotFoundException {
//        in.defaultReadObject();
        int length = in.readInt();
        matrix = new int[length * length];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                matrix[i * isFree.length + j] = in.readInt();
            }
            isFree[i] = in.readBoolean();
        }
        numFree = in.readInt();
        GROW_FACTOR = in.readFloat();
        AUTO_GROW = in.readBoolean();
        length = in.readInt();
        keyToIndex = new IntIntOpenHashMap(length);
        for (int i = 0; i < length; i++) {
            keyToIndex.put(in.readInt(), in.readInt());
        }
    }

//    @Override
//    public int hashCode() {
//        return 1;
//    }
//
//    @Override
//    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
//    public boolean equals(Object o) {
////        if (!(o instanceof HashedHashedMapZoneImpl)) {
////            return false;
////        }
//        Zone z = (Zone) o;
//        if (this == z) {
//            return true;
//        }
//        if (size() != z.size()) {
//            return false;
//        }
////         if(z.digest().compareToIgnoreCase(digest())==0)return true;
//
////        Map.Entry<Integer, Row3> thisEntry;
////        Iterator<Map.Entry<Integer, Row3>> it = this.entrySet().iterator();
////        while (it.hasNext()) {
////            thisEntry = it.next();
////            //if(thisEntry!=null)
////            if (!z.get((int) thisEntry.getKey()).equals(thisEntry.getValue())) {
////                return false;
////            }
////        }
//        Iterator<IntIntCursor> iteri =keyToIndex.iterator();
//        for (IntIntCursor i = iteri.next();
//                iteri.hasNext();
//                i = iteri.next()) {
//        Iterator<IntIntCursor> iterj =keyToIndex.iterator();
//        for (IntIntCursor j = iterj.next();
//                iterj.hasNext();
//               j = iterj.next()) {
//                if (z.get(i.key, j.key) != get(i.key, j.key)) {//!!
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//    static final Integer s = 0;
    /**
     *
     */
    public MatrixZoneVer3Impl() {
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- new Zone()");
        }
        put((int) 0, (int) 0, 0);

    }

    /**
     *
     * @param z
     */
    public MatrixZoneVer3Impl(MatrixZoneVer3Impl z) {
        super(z);
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- new Zone()");
        }
        put((int) 0, (int) 0, 0);
    }

    public MatrixZoneVer3Impl(Zone z) {
        super(z);
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- new Zone()");
        }
        put((int) 0, (int) 0, 0);
    }

    /**
     *
     * @return
     */
    @Override
    final public Zone clone() {
//        release();
//          shrink();
        sumOfSize += (size + 1);
        clones++;
        estMemory += ((size + 1) * (size + 1)) * 4 + (size + 1) * (1 + 2 * 4) + 38;
        return new MatrixZoneVer3Impl(this);
//        return new FinalizedCanonicalZone(this);
//return  (Zone) super.clone();
    }

    final public Zone cloneFinal() {
        release();
        sumOfSize += (size + 1);
        clones++;
        estMemory += ((size + 1) * (size + 1)) * 4 + (size + 1) * (1 + 2 * 4) + 38;
//        return new MatrixZoneVer3Impl(this);
        return new FinalizedCanonicalZone(this);
//return  (Zone) super.clone();
    }

//    @Override
//    public int hashCode() {
//        return 1;
//    }
//    @Override
//    public boolean equals(Object o) {
//        return HashedHashedMapZoneImpl.equals(this,(Zone) o);
//    }
    /**
     *
     * @param transIndex
     * @param low
     */
    public void restrict(int transIndex, int low) {
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- restrict2(t=" + transIndex + ", LB=" + low + ")");
        }
        //if( label(0) - label(j) > -low(j) ) then
        if (isInf(transIndex, 0)) {
            put(transIndex, 0, -low);
        }
        if (get(transIndex, 0) + low > 0) {
            //other(j0) = -low(j);
            put(transIndex, 0, -low);
        }
    }

    /**
     *
     * @param t
     */
    public void project(int t) {
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- project(" + t + ")");
        }
//        shrink();
        remove(t);
    }

    /**
     *
     */
    public void recanonicalize() {
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- recanonicalization()");
        }
        //in the book, M (for matrix) is referred as the DBM of the zone
        //key_xy is the virtual row or column
        int key_i, key_j, key_k, M_ij, M_ik, M_kj;
        int x;
        for (int i = 0; i < keys().length; i++) {
            if (notAssigned(i)) {//This hashmap (keysToIndex) requires these checks
                continue;
            }
            key_i = key(i);
            for (int j = 0; j < keys().length; j++) {
                if (notAssigned(j)) {
                    continue;
                }
                if (i == j) {
                    continue;
                }
                key_j = key(j);
                M_ij = get(key_i, key_j);
                for (int k = 0; k < keys().length; k++) {
                    if (notAssigned(k)) {
                        continue;
                    }
                    if (i == k) {
                        continue;
                    }
                    if (j == k) {
                        continue;
                    }
                    key_k = key(k);
                    M_ik = get(key_i, key_k);
                    M_kj = get(key_k, key_j);
                    /**
                     * if the distance from i to j is more than
                     * the distance from i to k plus k to j,
                     * then set the distance of i to j to the other
                     * distance amount.
                     */
                    x = 0;
                    if (M_ik > MIN_INF) {
                        x = INF;

                    } else if (M_kj > MIN_INF) {
                        x = INF;
                    } else if (M_ik < MAX_NEG_INF) {
                        x = Integer.MIN_VALUE;
                    } else if (M_kj < MAX_NEG_INF) {
                        x = Integer.MIN_VALUE;
                    } else {
//                        if(x<0)x=INF;
                        x = M_ik + M_kj;
                    }
                    if (M_ij > MIN_INF) {
                        put(key_i, key_j, (int) x);
                    } else if (M_ij > x) {
                        put(key_i, key_j, (int) x);
                    }
                    //!!
//                    if (isInf(key(i), key(k))) {
//                        x = INF;
//                    } else if (isInf(key(k), key(j))) {
//                        x = INF;
//                    } else if (isNegInf(key(i), key(k))) {
//                        x = Integer.MIN_VALUE;
//                    } else if (isNegInf(key(k), key(j))) {
//                        x = Integer.MIN_VALUE;
//                    } else
//                    {
//                        x = get(key(i), key(k)) + get(key(k), key(j));
//                    }
//                    if (isInf(key(i), key(j))) {
//                        put(key(i), key(j), x);
//                    } else
//                    {
//                        if (get(key(i), key(j)) > x) {
//                            put(key(i), key(j), x);
//                        }
//                    }
                }
            }
        }
    }

    /**
     * Force the diagonal to be zeros.
     */
    public void validate() {
        Iterator<IntCursor> iter = keySet().iterator();
        for (IntCursor j = iter.next();
                iter.hasNext();
                j = iter.next()) {
            put(j.index, j.index, 0);
        }
    }

    /**
     *
     * @return
     */
    final public boolean isValid() {
//        Iterator<IntCursor> iter = keySet().iterator();
//        for (IntCursor j = iter.next();
//                iter.hasNext();
//                j = iter.next()) {
//            if (get(j.index, j.index) != 0) {
//                return false;
//            }
//        }
        HashSet<Integer> keys = new HashSet<Integer>(keySet().size() / 2);
        for (IntCursor i : keySet()) {
            keys.add(i.value);
        }
        NavigableSet<Integer> treeSet = new TreeSet<Integer>(keys);
        int x = 0;
        for (int i : treeSet) {
            if (get(i, i) != 0) {
                throw new RuntimeException("ZONE IS NOT VALID: bad operations");
//                return false;
            }
            x++;
        }
        return true;
    }
//    /**
//     *
//     * @param row
//     * @param col
//     * @return
//     */
//    public boolean isInf(int row, int col) {
//        try {
//            if (get(row, col) >= INF * 0.9) {
//                put(row, col, INF);
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    /**
//     *
//     * @param row
//     * @param col
//     * @return
//     */
//    public boolean isNegInf(int row, int col) {
//        try {
//            if (get(row, col) <= -INF * 0.9) {
//                put(row, col, Integer.MIN_VALUE);
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
    Integer tmpTimer;
    int tmp;

    /**
     * @return the ID
     */
    public int getID() {
        return ID;
    }

    static enum RANGE {

        PASS, FAIL
    }

    @Override
    public boolean isTimeEnabled(int delayLB, int col) {
        return delayLB <= get(0, col);
//        if (delayLB <= 0) {
//            return true;
//        } else if (rangeCheck(col) == false) {
//            return true;
//        } else if (delayLB <= (tmp = get(0, col))) {
//            if (tmp == INF) {
//                project(col);
//            }
//            return true;
//        } else {
//            return false;
//        }
    }

    boolean rangeCheck(int row) {
        return getIndex_unchecked(row) >= 0 ? true : false;
    }

    /**
     *
     * @return
     */
    @Override
    public String digest() {
        String s = "+Z." + "_" + getLongHash();
        s = s.replace(" ", "").replace(".", "").replace("+", ".")//
                .replace("\n", "").replace("        ", "").replace("_", "")//
                .replace("|", "").replace("\\", "").replace("-", "")//
                .replace(",", "").replace("[", "").replace("]", "");
        try {
//         for(Provider pr:   Security.getProviders()){
//            System.out.println(pr);
//         }
            byte[] data;
            Deflater def = new BuiltInZlibDeflater(BuiltInZlibDeflater.BEST_COMPRESSION);
            def.setInput(s.getBytes());
            def.finish();
            ByteArrayOutputStream os = new ByteArrayOutputStream(s.getBytes().length);
            byte[] buf = new byte[8];
            while (!def.finished()) {
                int count = def.deflate(buf);
                os.write(buf, 0, count);
            }
            os.close();
            data = os.toByteArray();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            data = md.digest();
//            for(byte b:message){
//                s2+=Integer.toHexString(b&0xff);
//            }
//            byte[] data=s.getBytes();

            s = "";
            for (byte b : data) {
                s += Integer.toString(b & 0xff, Character.MAX_RADIX);
            }

        } catch (Exception ex) {
        }
        return s;
    }

    public File serialize2(String filename)
            throws FileNotFoundException, IOException {
        File f = new File(filename);
        DataOutputStream os = new DataOutputStream(new FileOutputStream(f));
        writeObject(os);
        os.close();
        return f;
    }

    public static Zone deserialize2(String filename)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        File f = new File(filename);
        return deserialize2(f);
    }

    public static Zone deserialize2(File f)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        DataInputStream is = new DataInputStream(new FileInputStream(f));
//        Zone zone = (Zone) os.readObject();
        MatrixZoneVer3Impl zone = new MatrixZoneVer3Impl();
        zone.readObject(is);
        is.close();
        return zone;
    }

    public File serialize(String filename)
            throws FileNotFoundException, IOException {
        File f = new File(filename);
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f));
        os.writeObject(this);
        os.close();
        return f;
    }

    public static Zone deserialize(String filename)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        File f = new File(filename);
        return deserialize(f);
    }

    public static Zone deserialize(File f)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(f));
//        Zone zone = (Zone) os.readObject();
        Zone zone = (Zone) is.readObject();
        is.close();
        return zone;
    }
//public int[] canonicalDBM(){
//     MatrixZoneVer3Impl a = this;
//              int  size = size();
//           int[]     z = new int[size * size];
//               int[] keys = new int[size];
//                int i = 0, j = 0;
//                for (i = 0; i < a.keyToIndex.keys.length; i++) {
//                    if (a.keyToIndex.states[i] == ASSIGNED) {
//// System.err.printf("(%s, %s) (%s, %s)\n",size,j, a.keyToIndex.keys.length,i);
//                        keys[j++] = a.keyToIndex.keys[i];
//                    }
//                }
//                Arrays.sort(keys);
////                Arrays.sort(z);
////                System.arraycopy(keys, 0, z, 0, keys.length);
//                i = 0;
//                for (int row : keys) {
//                    j = 0;
//                    for (int col : keys) {
////                        System.err.printf("(R=%s,C=%s), [(i=%s+1)*S=%s+j=%s]=%s <-- Z[R,C]=%s\t%s\t%s\n",
////                                row, col,
////                                i, size, j,
////                                (i) * size + j,
////                                zone.get(row, col),
////                                Arrays.toString(keys),
////                                Arrays.toString(z));
//                        put(row, col, this.get(row, col));
//                        j++;
//                    }
//                    i++;
//                }
//            return z;
//}

    public static final class FinalizedCanonicalZone extends Zone {

        public final int ID;
        private static final long serialVersionUID = 192837109273L;
        final int[] z, keys;
        final int size;

        int indexOfKey(int key) {
            return Arrays.binarySearch(keys, key);
        }

        public FinalizedCanonicalZone(Zone zone) {
//            System.out.printf("FZ: %s, %s\n", size, z.length);
            ID = (zone).getID();

            if (zone.keySet() instanceof int[]) {
                size = zone.size();
                z = new int[size * size];
                Zone a = (Zone) zone;
                keys = Arrays.copyOf((int[]) a.keySet(), ((int[]) a.keySet()).length);
                Arrays.sort(keys);
                int i = 0, j;
                System.arraycopy(keys, 0, z, 0, keys.length);
                for (int row : keys) {
                    j = 0;
                    for (int col : keys) {
                        z[(i++) * size + j++] = zone.get(row, col);
                    }
                }
            } else if (zone.keySet() instanceof Collection) {
                size = zone.size();
                z = new int[size * size];
                Zone a = (Zone) zone;
                Integer[] keys_ = new LinkedList<Integer>(
                        (Collection<Integer>) a.keySet()).toArray(new Integer[0]);
                int i = 0, j;
                keys = new int[size];
                for (int k : keys_) {
                    keys[i++] = k;
                }
                Arrays.sort(keys);
                i = 0;
                System.arraycopy(keys, 0, z, 0, keys.length);
                for (int row : (Collection<Integer>) a.keySet()) {
                    j = 0;
                    for (int col : (Collection<Integer>) a.keySet()) {
                        z[(i++) * size + j++] = zone.get(row, col);
                    }
                }
            } else {
                MatrixZoneVer3Impl a = (MatrixZoneVer3Impl) zone;
                size = a.keyToIndex.keys.length - a.numFree;
                z = new int[size * size];
                keys = new int[size];
                int i = 0, j = 0;
                for (i = 0; i < a.keyToIndex.keys.length; i++) {
                    if (a.keyToIndex.states[i] == ASSIGNED) {
// System.err.printf("(%s, %s) (%s, %s)\n",size,j, a.keyToIndex.keys.length,i);
                        keys[j++] = a.keyToIndex.keys[i];
                    }
                }
                Arrays.sort(keys);
//                Arrays.sort(z);
//                System.arraycopy(keys, 0, z, 0, keys.length);
                i = 0;
                for (int row : keys) {
                    j = 0;
                    for (int col : keys) {
//                        System.err.printf("(R=%s,C=%s), [(i=%s+1)*S=%s+j=%s]=%s <-- Z[R,C]=%s\t%s\t%s\n",
//                                row, col,
//                                i, size, j,
//                                (i) * size + j,
//                                zone.get(row, col),
//                                Arrays.toString(keys),
//                                Arrays.toString(z));
                        put(row, col, zone.get(row, col));
                        j++;
                    }
                    i++;
                }
            }
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(z);
        }

        @Override
        public boolean equals(Object obj) {
            return Arrays.equals(z, ((FinalizedCanonicalZone) obj).z);
        }

        @Override
        public void restrict(int transIndex, int low) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void allocate(int t) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void project(int t) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void recanonicalize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean put(int row, int col, int val) {
            z[(indexOfKey(row)) * size + indexOfKey(col)] = val;
            return true;
        }

        @Override
        public int get(int row, int col) {
            return z[(indexOfKey(row)) * size + indexOfKey(col)];
        }

        @Override
        public boolean isInf(int row, int col) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isNegInf(int row, int col) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isTimeEnabled(int delayLB, int label) {
            return Zone.isTimeEnabled(this, delayLB, label);
        }

        @Override
        public Zone clone() {
            return new MatrixZoneVer3Impl(this);
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
        public String toHTMLTable() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public void validate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean containsKey(int t) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean contains(int t) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void remove(int t) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File serialize(String filename) throws FileNotFoundException, IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object keySet() {
            return keys;
        }

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

        final public String toString() {

            NavigableSet<Integer> treeSet = new TreeSet<Integer>();
            for (int i : keys) {
                treeSet.add(i);
            }
            int idx1, idx2;
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
                idx1 = indexOfKey(row);
                Iterator<Integer> colIter = treeSet.iterator();
                ret += format1(row + "|");
                while (colIter.hasNext()) {
                    col = colIter.next();
                    idx2 = indexOfKey(col);
                    try {
                        ret += format2(z[idx1 * size + idx2] > 0.9 * INF
                                ? "INF" : z[idx1 * size + idx2] + "");
                    } catch (Exception e) {
                        String fr = "";
//                    for (boolean b : isFree) {
//                        fr += b ? "1  " : "0  ";
//                    }
//                    System.err.printf("ERROR:%s\t%s\n\t row,col=(%s,%s);\tidx=(%s,%s);"
//                            + "\tnum free=%s;\tsize=%s;\tlen=%s,%s\n",
//                            e.getLocalizedMessage(), e.getCause(), row, col, idx1, idx2,
//                            numFree, size(), matrix.length, matrix[idx1].length);
//                    System.err.println("keyToIndex:\t" + keyToIndex.toString());
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

        @Override
        public int getID() {
            return ID;
        }
    }
}
