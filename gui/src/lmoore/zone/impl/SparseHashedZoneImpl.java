/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lmoore.zone.impl;

import static java.util.Arrays.sort;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import lmoore.zone.Zone;

/**
 *
 * @author ldmtwo
 */
@Deprecated public class SparseHashedZoneImpl extends Zone {
    private static final long serialVersionUID = 137096491764170936L;
    IntIntOpenHashMap dbm = new IntIntOpenHashMap(4);
    int size = 1;
    int[] keys = new int[4];

    public int[] keys() {
        return keys;
    }

    @Override
    public void restrict(int row, int lowerBound) {
        int b=get(row, 0);
          if (b >= MIN_INF) {
            put(row, 0, -lowerBound);
        }else        if (b + lowerBound > 0) {
            //other(j0) = -low(j);
            put(row, 0, -lowerBound);
        }
    }

    @Override
    public void allocate(int t) {
        keys = (cern.colt.Arrays.ensureCapacity(keys, size + 1));
              keys[size] = t;  sort(keys);
        size++;
    }

    @Override
    public void project(int t) {
        for (int row : keys) {
            dbm.remove(row << 16 + t);
        }
        for (int col : keys) {
            dbm.remove(t << 16 + col);
        }
        int i=Arrays.binarySearch(keys, t);
        System.arraycopy(keys, i, keys, i+1, keys.length-i);
        size--;
    }

    @Override
    public void recanonicalize() {
        int x, mij;
        for (int ki = 0; ki < keys().length; ki++) {

            for (int kj = 0; kj < keys().length; kj++) {

//                if (ki == kj) {
//                    continue;
//                }
                mij = get(ki, kj);
                for (int kk = 0; kk < keys().length; kk++) {

//                    if (ki == kk) {
//                        continue;
//                    }
//                    if (kj == kk) {
//                        continue;
//                    }
                    x = get(ki, kk) + get((kk), (kj));
                    if (x < 0) {
                        x = INF;
                    }
                    if (mij > x) {
                        put((ki), (kj), x);
                    }

                }
            }
        }
    }

    @Override
    public boolean put(int row, int col, int val) {
        if(row!=col)
        dbm.put(row << 16 + col, val);
        return true;
    }

    @Override
    public int get(int row, int col) {
               return row==col?0: dbm.get(row << 16 + col);
    }

    @Override
    public boolean isInf(int row, int col) {
        if (get(row, col) >= MIN_INF) {
            put(row, col, INF);
            return true;
        }
        return false;
    }

    @Override
    public boolean isNegInf(int row, int col) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isTimeEnabled(int delayLB, int col) {
        if (delayLB <= 0) {
            return true;
        }
        return delayLB <= get(0, col);
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
       return Arrays.binarySearch(keys, t)>=0;
    }

    @Override
    public boolean contains(int t) {
        return Arrays.binarySearch(keys, t)>=0;
    }

    @Override
    public void remove(int t) {
       
    }

    @Override
    public File serialize(String filename) throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object keySet() {
        return keys;
    }

    public int getID(){
       return this.ID;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
