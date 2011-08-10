/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lmoore.zone;

import bak.pcj.set.IntOpenHashSet;
import com.carrotsearch.hppc.LongOpenHashSet;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import lmoore.zone.impl.MatrixZoneVer3Impl;
import lmoore.zone.map.HashedMap;

import org.apache.commons.collections.primitives.ArrayIntList;
import platu.project.IDGenerator;
import platu.stategraph.UpdateTimer;

/**
 *This class reduces the number of actual objects in memory.
 * If instance of some object A and B are equal such that
 * A.equals(B) <==> B.equals(A) and
 * A.hashcode() == B.hashcode(),
 * then only 1 of either A or B needs to be kept in memory
 * and the other is referenced.
 */
abstract public class Cache {

    final int INIT_SIZE;

    public static final Cache getNewCache(int initSize) {
        return new HashedCache(initSize);
    }

    public static final Cache getNewDCache(int initSize, int chunkSize) {
        return new DCache(initSize, chunkSize);
    }

    public static final Cache getNewIdentityCache(int initSize) {
        return new IdentityCache(initSize);
    }

    private Cache(int initSize) {
        INIT_SIZE = initSize;
    }

    /**
     * Insert the value if it does not exist. If it does exist, 
     * return the old object, else return the new object.
     * @param obj
     * @return a non-redundant object that is equal to the input
     */
    abstract public Object tryInsert(Object obj);

    abstract public int tryInsert(int obj);

    abstract public long tryInsert(long obj);

    public boolean add(Object obj) {
        return tryInsert(obj) == obj;
    }

    final public boolean add(int obj) {
        return tryInsert(obj) == obj;
    }

    final public boolean add(long obj) {
        return tryInsert(obj) == obj;
    }

    abstract public int size();

    final private static byte[] int2byte(int[] src) {
        final int srcLength = src.length;
        byte[] dst = new byte[srcLength << 2];

        for (int i = 0; i < srcLength; i++) {
            int x = src[i];
            int j = i << 2;
            dst[j++] = (byte) ((x) & 0xff);
            dst[j++] = (byte) ((x >>> 8) & 0xff);
            dst[j++] = (byte) ((x >>> 16) & 0xff);
            dst[j++] = (byte) ((x >>> 24) & 0xff);
        }
        return dst;
    }

    /**Decomposed cache --> break elements into smaller pieces for storage
     */
    public static final class DCache extends Cache {

        final HashedCache hcache = new HashedCache(10);
        final HashedMap cache;
        final int chunkSize;

        public DCache(int initSize, int chunkSize) {
            super(initSize);
            this.chunkSize = chunkSize;
            cache = new HashedMap();
        }

        final public Object tryInsert(Object obj, final int chunkSize) {
            if (obj instanceof MatrixZoneVer3Impl) {
                MatrixZoneVer3Impl z = (MatrixZoneVer3Impl) obj;
//                obj=new BigInteger(int2byte(z.canonicalDBM()));
                obj = new ArrayIntList(z.canonicalDBM());
                return get(obj);
            } else if (obj instanceof int[]) {
                int[] data = (int[]) obj;
//                chunkSize=(int) Math.sqrt(data.length);
                ArrayList list = new ArrayList(1);
                for (int i = 0; i < data.length; i += chunkSize) {
                    list.add(get(Arrays.copyOfRange(data, i, i + chunkSize)));
                }
                return get(list);
            } else {
                return get(obj);
            }
        }

        @Override
        final public Object tryInsert(Object obj) {
            if (obj instanceof MatrixZoneVer3Impl) {
                MatrixZoneVer3Impl z = (MatrixZoneVer3Impl) obj;
//                obj=new BigInteger(int2byte(z.canonicalDBM()));
                obj = new ArrayIntList(z.canonicalDBM());
                return get(obj);
            } else if (obj instanceof int[]) {
                int[] data = (int[]) obj;
//                chunkSize=(int) Math.sqrt(data.length);
                ArrayList list = new ArrayList(1);
                for (int i = 0; i < data.length; i += chunkSize) {
                    list.add(get(Arrays.copyOfRange(data, i, i + chunkSize)));
                }
                return get(list);
            } else {
                return get(obj);
            }
        }

        private Object get(Object obj) {
            if (cache.containsKey(obj)) {
                return cache.get(obj);
            }
            cache.put(obj, obj);
            return obj;
        }

//    private Object get(Object[] obj) {
//         ArrayList list = new ArrayList(Arrays.asList(obj));
//            if (cache.containsKey(list)) {
//                return cache.get(list);
//            }
//            cache.put(list, list);
//            return list;
//        }
//    private Object get(int[] obj) {
//         ArrayIntList list = new ArrayIntList(obj);
//            if (cache.containsKey(list)) {
//                return cache.get(list);
//            }
//            cache.put(list, list);
//            return list;
//        }
        @Override
        public int size() {
            return cache.size();
        }

        @Override
        public String toString() {
            return cache.toString();
        }

        @Override
        public int tryInsert(int obj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long tryInsert(long obj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**Standard cache structure
     */
    public static final class HashedCache extends Cache {
  final int bins = 10;
        final int rowLen =155;// (int) Math.ceil(100/4f);
        final int numRows = 1;
        int size = 10;
//        final Cache objCache =                new ClusteredCache(bins, rowLen, numRows);
        HashedMap cache = new HashedMap(1);
        IntOpenHashSet c2 = new IntOpenHashSet(1);
        LongOpenHashSet c3 = new LongOpenHashSet(1);

        private HashedCache(int initSize) {
            super(initSize);
//            cache = new Hashtable();
        }

        @Override
        public int tryInsert(int obj) {
            return get(obj);
        }

        @Override
        public long tryInsert(long obj) {
            return get(obj);
        }

        @Override
        final public Object tryInsert(Object obj) {
//            if (obj instanceof MatrixZoneVer3Impl) {
//                final MatrixZoneVer3Impl z = (MatrixZoneVer3Impl) obj;
//////                obj=new BigInteger(int2byte(z.canonicalDBM()));
//                obj = new ArrayIntList(z.canonicalDBM());
//            } else
            if (obj instanceof int[]) {
//                obj = new ArrayIntList(((int[]) obj));
                obj = new BigInteger(int2byte((int[]) obj));
//                return objCache.add(obj);
//                return get((int[]) obj);
            } else if (obj instanceof Object[]) {
                return get((Object[]) obj);
            }
            return get(obj);
        }

        @Override
        final public int size() {
            return cache.size();
        }

        final private int get(int obj) {
            if (c2.contains(obj)) {
                return (obj);
            }
            c2.add(obj);
            return obj;
        }

        final private long get(long obj) {
            if (c3.contains(obj)) {
                return (obj);
            }
            c3.add(obj);
            return obj;
        }

        final private Object get(Object obj) {
            if (cache.containsKey(obj)) {
                return cache.get(obj);
            }
            cache.put(obj, obj);
            return obj;
        }

        private Object get(Object[] obj) {
            ArrayList list = new ArrayList(Arrays.asList(obj));
            if (cache.containsKey(list)) {
                return cache.get(list);
            }
            cache.put(list, list);
            return list;
        }

        private Object get(int[] obj) {
            ArrayIntList list = new ArrayIntList(obj);
            if (cache.containsKey(list)) {
                return cache.get(list);
            }
            cache.put(list, list);
            return list;
        }

        @Override
        public String toString() {
            return cache.toString();
        }
    }

    /**Associate a unique identifier with HashedCache elements
     */
    public static final class IdentityCache extends Cache {

        IDGenerator<Object> id = new IDGenerator<Object>(1);
        HashedMap cache = new HashedMap(1);
        IntOpenHashSet c2 = new IntOpenHashSet(1);
        LongOpenHashSet c3 = new LongOpenHashSet(1);

        private IdentityCache(int initSize) {
            super(initSize);
//            cache = new Hashtable();
        }

        public int getID(Object obj) {
            return id.test(obj);
        }

        public int addID(Object obj) {
            return id.tryInsert(obj);
        }

        @Override
        public int tryInsert(int obj) {
            return get(obj);
        }

        @Override
        public long tryInsert(long obj) {
            return get(obj);
        }

        @Override
        final public Object tryInsert(Object obj) {
//            if (obj instanceof MatrixZoneVer3Impl) {
//                final MatrixZoneVer3Impl z = (MatrixZoneVer3Impl) obj;
//////                obj=new BigInteger(int2byte(z.canonicalDBM()));
//                obj = new ArrayIntList(z.canonicalDBM());
//            } else
            if (obj instanceof int[]) {
//                obj = new ArrayIntList(((int[]) obj));
                obj = new BigInteger(int2byte((int[]) obj));
//                return get((int[]) obj);
            } else if (obj instanceof Object[]) {
                return get((Object[]) obj);
            }
            return get(obj);
        }

        @Override
        final public int size() {
            return id.size();
        }

        final private int get(int obj) {
            if (c2.contains(obj)) {
                return (obj);
            }
            id.add(obj);
            c2.add(obj);
            return obj;
        }

        final private long get(long obj) {
            if (c3.contains(obj)) {
                return (obj);
            }
            id.add(obj);
            c3.add(obj);
            return obj;
        }

        final private Object get(Object obj) {
            if (cache.containsKey(obj)) {
                return cache.get(obj);
            }
            id.add(obj);
            cache.put(obj, obj);
            return obj;
        }

        private Object get(Object[] obj) {
            ArrayList list = new ArrayList(Arrays.asList(obj));
            if (cache.containsKey(list)) {
                return cache.get(list);
            }
            id.add(list);
            cache.put(list, list);
            return list;
        }

        private Object get(int[] obj) {
            ArrayIntList list = new ArrayIntList(obj);
            if (cache.containsKey(list)) {
                return cache.get(list);
            }
            id.add(list);
            cache.put(list, list);
            return list;
        }

        @Override
        public String toString() {
            return cache.toString();
        }
    }

    public static final class TreeCache extends Cache {

        Object[] tree = new Object[256];
//        TreeSet cache = new TreeSet();
        //TreeSet<>
        //
        int idxa, idxb, idxc, idxd;
        int size = 0;

        public TreeCache() {
            super(1);
        }

        @Override
        public Object tryInsert(Object obj) {
            Object ret = null;
            int h;
            if (obj instanceof int[]) {
                h = Arrays.hashCode((int[]) obj);
            } else if (obj instanceof byte[]) {
                h = Arrays.hashCode((byte[]) obj);
            } else if (obj instanceof long[]) {
                h = Arrays.hashCode((long[]) obj);
            } else if (obj instanceof int[]) {
                h = Arrays.hashCode((Object[]) obj);
            } else {
                h = obj.hashCode();
            }
//            ret = cache.get(h);
            idxa = h % 256;
            if (tree[idxa] == null) {
                tree[idxa] = obj;
                return obj;
            } else if (tree[idxa] instanceof Object[]) {
            } else {
            }

            if (ret == null) {
//                cache.add(h, obj);

                return obj;
            }
            return ret;
        }

        @Override
        public int tryInsert(int obj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long tryInsert(long obj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int size() {
//            return cache.size();
            return size;
        }
    }

    public static final class ClusteredCache extends Cache {

//        IntIntOpenHashMap hashToCluster = new IntIntOpenHashMap();
//        IntIntOpenHashMap hashToSize = new IntIntOpenHashMap();
        final int bins;
        final int rowLen;
        final int numRows;
        final int[][] data;
        final int[] cSize;
        int size = 0;

        public ClusteredCache(int bins, int rowLen, int numRows) {
            super(bins);
            this.bins = bins;
            this.rowLen = rowLen;
            this.numRows = numRows;
            data = new int[bins][rowLen * numRows];
            cSize = new int[bins];
        }

        boolean equals(int offset, int[] cluster, int[] other) {
            for (int i = 0; i < other.length; offset++, i++) {
                if (cluster[offset] != other[i]) {
                    return false;
                }
            }
            return true;
        }

        boolean contains(int size, int[] cluster, int[] other) {
            for (int i = 0; i < size && i * other.length < cluster.length; i++) {
//                try {
                if (equals(i * other.length, cluster, other)) {
                    return true;
                }
//                } catch (Exception e) {
//                    throw new RuntimeException(String.format(
//                            "i=%s\tsize=%s\toth.len=%s\ti*oth.len=%s\tcluster.length=%s\n",
//                            i, size, other.length, i * other.length, cluster.length), e);
//                }
            }
            return false;
        }

        void printCluster(int[] cluster) {
            System.out.printf("-----------------------------CLUSTER");
            for (int i = 0; i < cluster.length; i++) {
                if (i % rowLen == 0) {
                    System.out.printf("\n");
                }
                System.out.printf("%s, ", cluster[i]);
            }
            System.out.printf("\n------------------------------\n");
        }

        @Override
        public boolean add(Object obj) {
            int[] o = (int[]) obj;
            int h = Math.abs(Arrays.hashCode(o));
            int clIdx = (h % bins);//hashToCluster.get(h);
            int Csize = cSize[clIdx];
            if (clIdx < 0) {
                clIdx = 0;
            }
            if (Csize < 0) {
                Csize = 0;
            }
//            System.out.printf("ADD: [%s]\t\th=%s\tCsize=%s\tclIdx=%s\n", Arrays.toString(o), h, Csize, clIdx);
            int[] cluster = data[clIdx];
//            printCluster(cluster);
            boolean cont = contains(Csize, cluster, o);
//            System.out.printf("contains? %s\n", cont);
            if (!cont) {
                System.err.println(String.format(
                        "Csize=%s\trowLen=%s\to.len=%s\tCsize*rowLen=%s\tcluster.len=%s\n",
                        Csize, rowLen, o.length, Csize * rowLen, cluster.length));
                if (Csize * rowLen >= cluster.length) {
                    data[clIdx] = cluster = Arrays.copyOf(cluster, cluster.length * 2);

//                    System.err.println(String.format(
//                            "RESIZED: Csize=%s\trowLen=%s\to.len=%s\tCsize*rowLen=%s\tcluster.len=%s\n",
//                            Csize, rowLen, o.length, Csize * rowLen, cluster.length));
                }
//                try {
                System.arraycopy(o, 0, cluster, Csize * rowLen, o.length);
//                } catch (Exception e) {
//                    throw new RuntimeException(String.format(
//                            "Csize=%s\trowLen=%s\to.len=%s\tCsize*rowLen=%s\tcluster.len=%s\n",
//                            Csize, rowLen, o.length, Csize * rowLen, cluster.length), e);
//                }
                Csize++;
                this.size++;
                cSize[clIdx]++;
//                printCluster(cluster);
            }
//            hashToSize.put(clIdx, Csize);
            return cont;
        }

        @Override
        public Object tryInsert(Object obj) {
            int[] o = (int[]) obj;
            int h = Arrays.hashCode(o);


            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int tryInsert(int obj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long tryInsert(long obj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int size() {
            return size;
        }
    }

    public static void main(String[] arg) {
//        TreeCache cache = new TreeCache();
        final int bins = 1024*1024;
        final int rowLen = (int) Math.ceil(33/4f);
        final int numRows = 1;
        int size = 100000000;
        final Cache cache =
                new ClusteredCache(bins, rowLen, numRows);
//     new HashedCache(bins);
        UpdateTimer timer = new UpdateTimer(10000) {

            @Override
            public int getNumStates() {
                return cache.size();
            }

            @Override
            public int getStackHeight() {
                return 1;
            }

            @Override
            public String getLabel() {
                return "";
            }
        };
        int[] data = new int[rowLen];
        Random r = new Random();
    
        timer.start();
       
        for (int i = 0; i < size; i++) {
            
            for (int j = 0; j < rowLen; j++) {
                data[j] = r.nextInt() & 1023;
            }
            boolean ret = cache.add(data);
//            c2.add(data);
//            if(ret)
//            System.out.printf("Input: %s\t ret: %s\t new size: %s\n", Arrays.toString(o), ret, cache.size());
//            else
//            System.out.printf("\t\tInput: %s\t ret: %s\t new size: %s\n", Arrays.toString(o), ret, cache.size());
        }
        System.out.printf("++++ size: %s\n", cache.size());
        timer.print();
        System.exit(0);
    }
}
