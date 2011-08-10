/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lmoore.zone.map;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

/**
 *
 * @author ldmtwo
 */
public class IntHashMap {

    private static int h;
    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;
    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    int threshold;
    public int capacity,//num bins
            size = 0;//num keys
//LinkedList<int[]>[] values;
    transient Array[] V;
    transient Array[] K;

    public IntHashMap() {
        this(1024 * 1024);
    }

    public IntHashMap(int cap) {
        capacity = cap;
        threshold = (int) (capacity * DEFAULT_LOAD_FACTOR);
        V = new Array[capacity];
        K = new Array[capacity];
        for (int i = 0; i < capacity; i++) {
            V[i] = new Array();
            K[i] = new Array();
        }
    }

    void grow() {
        capacity *= 2;
        threshold = (int) (capacity * DEFAULT_LOAD_FACTOR);
        Array[] Vo = V;
        Array[] Ko = K;

        V = new Array[capacity];
        K = new Array[capacity];

        for (int i = 0; i < V.length; i++) {
            V[i] = new Array();
            K[i] = new Array();
        }
        for (int i = 0; i < Vo.length; i++) {
            for (int j = 0; j < Vo[i].a_size(); j++) {
                put(Ko[i].a_get(j), Vo[i].a_get(j));
            }
        }
//        System.out.printf("GROW: %s\n", threshold);
    }
    final static int NEG = ~(1 << 31);

    final static int hash(int h) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
//        h ^= (h >>> 20) ^ (h >>> 12);
//        return (h ^ (h >>> 7) ^ (h >>> 4))&(~(1<<31));
        return (h & 0x7FFFFFFF);
    }

    public boolean containsKey(int key) {
        h = hash(key) % capacity;
        for (int i = 0; i < K[h].a_size(); i++) {
            if (K[h].a_get(i) == key) {
                return true;
            }
        }
        return false;
    }

    public int get(int key) {
        h = hash(key) % capacity;
        for (int i = 0; i < K[h].a_size(); i++) {
            if (K[h].a_get(i) == key) {
                return V[h].a_get(i);
            }
        }
        return -1;
    }
    static int max = 0;

    public int put(int key, int value) {
//        if(size>threshold)grow();
        h = hash(key) % capacity;//.5
        int i = 0;

        int si = K[h].a_size();
        for (; i < si; i++) {
            if (K[h].a_get(i) == key) {
                return V[h].a[i]=value;
            }
        }
        K[h].a_add(key);
        V[h].a_add(value);
        size++;
//        max=max>=K[h].size()?max:K[h].size();
//        System.gc();
        return value;
    }

    public int remove(int key) {
         h = hash(key) % capacity;
        for (int i = 0; i < K[h].a_size(); i++) {
            if (K[h].a_get(i) == key) {
                K[h].a_removeElementAt(i);
                size--;V[h].a_removeElementAt(i);
                return 0;
            }
        }
        return -1;
    }

    public Array[] keySet() {
        return K;
    }

    public Array[] values() {
        return V;
    }

    @Override
    public String toString() {
        String ret = "";
        for (int i = 0; i < capacity; i++) {
            ret += "<" + V[i].a_size() + ">";
            for (int j = 0; j < V[i].a_size(); j++) {
                ret += String.format("(%s,%s),", K[i].a_get(j), V[i].a_get(j));
            }
            ret += "\n";
        }
        return ret;
    }

    static class Array {

        int i = 0;
       public int[] a;

        private Array() {
            a = new int[8];
        }

        private Array(int i) {
            a = new int[i];
        }

        private void a_add(int nextInt) {
            if(i==a.length){
                a=Arrays.copyOf(a, a.length+10);
            }
            a[i++] = nextInt;
        }

        private int a_size() {
return i;
        }

  int m;
        private boolean a_contains(int o) {
            for(m=0;m<i;m++)if(a[m]==o)return true;
           return false;
        }

     
        private boolean a_removeKey(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private int a_get(int index) {
           return a[index];
        }


        private void a_removeElementAt(int idx) {
            i--;
            System.arraycopy(a, idx, a, idx-1, a.length-idx);
        }

        private int a_indexOf(int o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private int a_lastIndexOf(int o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

//        private int set(int i, int value) {
//           return a[i]=value;
//        }

   
    }

    public static void main(String[] a) {
        System.out.printf("%s bytes\n", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
//
        long best = Long.MAX_VALUE, t=0, mem=0;
        long best2 = Long.MAX_VALUE;
        for (int j = 0; j < 10; j++) {
//             {Random r = new Random();System.gc();
////                IntHashMap hm = new IntHashMap(1024);
////              cern.colt.list.IntArrayList hm=new cern.colt.list.IntArrayList(1024);//32mb 102
////              IntArrayList hm=new IntArrayList(1024);//64
////              ArrayIntList hm=new ArrayIntList(1024);//32mb 128
////              ArrayList hm=new ArrayList(1024);//96mb
////              org.antlr.misc.IntArrayList hm=new org.antlr.misc.IntArrayList(1024);//45mb
////              com.carrotsearch.hppc.IntArrayList hm=new com.carrotsearch.hppc.IntArrayList(1024);//32 mb 106
////              int[] hm=new int[5000000];int k=0; hm[k++]=r.nextInt();//19mb 92
//              Array hm=new Array(5000000);
//                long t = System.currentTimeMillis();
//                for (int i = 0; i < 5000000; i++) {
//                    hm.add(r.nextInt());
//
//                }
//                t=System.currentTimeMillis() - t;
//                best=best<t?best:t;
//                long mem=Runtime.getRuntime().totalMemory()
//                        - Runtime.getRuntime().freeMemory();
//                best2=best2<mem?best2:mem;
//                System.out.printf("\n%s\t%s\t%s MB\t",
//                        (Runtime.getRuntime().totalMemory()
//                        - Runtime.getRuntime().freeMemory()) / (float) hm.size(),
//                        hm.size(),
//                        (best2) / 1024 / 1024);
//                System.out.printf("%s msec\n",best );
//            }
//            {
//                Random r = new Random();
//                System.gc();
//                IntHashMap hm = new IntHashMap(1024);
//
//                t = System.currentTimeMillis();
//                for (int i = 0; i < 5000000; i++) {
//                    hm.put(r.nextInt(), r.nextInt());
//                }
//                t = System.currentTimeMillis() - t;
//                best = best < t ? best : t;
//                mem = Runtime.getRuntime().totalMemory()
//                        - Runtime.getRuntime().freeMemory();
//                best2 = best2 < mem ? best2 : mem;
////                System.out.printf("\n%s\t%s\t%s MB\t",
////                        (Runtime.getRuntime().totalMemory()
////                        - Runtime.getRuntime().freeMemory()) / (float) hm.size,
////                        hm.size,
////                        (Runtime.getRuntime().totalMemory()
////                        - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
////                System.out.printf("%s msec\n", System.currentTimeMillis() - t);
//            }
            {Random r = new Random();System.gc();
                IntHashMap2 hm = new IntHashMap2(1024);
                 t = System.currentTimeMillis();
                for (int i = 0; i < 5000000; i++) {
                    hm.put(r.nextInt(), r.nextInt());
                }
                t=System.currentTimeMillis() - t;
                best=best<t?best:t;
                 mem=Runtime.getRuntime().totalMemory()
                        - Runtime.getRuntime().freeMemory();
                best2=best2<mem?best2:mem;
//                System.out.printf("\n%s\t%s\t%s MB\t",
//                        (Runtime.getRuntime().totalMemory()
//                        - Runtime.getRuntime().freeMemory()) / (float) hm.size(),
//                        hm.size(),
//                        (best2) / 1024 / 1024);
//                System.out.printf("%s msec\n",best );
            }
//            {Random r = new Random();System.gc();
//                IntIntOpenHashMap hm = new IntIntOpenHashMap(1024);
//                 t = System.currentTimeMillis();
//                for (int i = 0; i < 5000000; i++) {
//                    hm.put(r.nextInt(), r.nextInt());
////                    l1.add(r.nextInt());
////                    l2.add(r.nextInt());
//                }
//
//                t=System.currentTimeMillis() - t;
//                best=best<t?best:t;
//                 mem=Runtime.getRuntime().totalMemory()
//                        - Runtime.getRuntime().freeMemory();
//                best2=best2<mem?best2:mem;
////                System.out.printf("\n%s\t%s\t%s MB\t",
////                        (Runtime.getRuntime().totalMemory()
////                        - Runtime.getRuntime().freeMemory()) / (float) hm.size(),
////                        hm.size(),
////                        (Runtime.getRuntime().totalMemory()
////                        - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
////                System.out.printf("%s msec\n", System.currentTimeMillis() - t);
//            }
//            {Random r = new Random();System.gc();
//                java.util.HashMap hm = new java.util.HashMap();
//                long t = System.currentTimeMillis();
//                for (int i = 0; i < 1000000; i++) {
//                    hm.put(r.nextInt(), r.nextInt());
//                }
//           System.out.printf("\n%s\t%s\t%s MB\t",
//                        (Runtime.getRuntime().totalMemory()
//                        - Runtime.getRuntime().freeMemory()) / (float) hm.size(),
//                        hm.size(), (Runtime.getRuntime().totalMemory()
//                        - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
//            }   
            System.out.printf("\n%s\t%s MB\t",
                    (mem),
                    (Runtime.getRuntime().totalMemory()
                    - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
            System.out.printf("%s msec\n", t);
        }

        System.out.printf("\n%s\t%s MB\t",
                (Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory()),
                (best2) / 1024 / 1024);
        System.out.printf("%s msec\n", best);
    }
}
