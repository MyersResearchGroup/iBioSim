/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lmoore;

import lmoore.zone.matrix.MatrixVer1;

/**
 *
 * @author ldtwo
 */
public class POSETMatrix extends MatrixVer1 {


    public void project(int t) {
        remove(t);
    }

    public void recanonicalize() {
        int x;
        for (int i : keySet()) {
            for (int j : keySet()) {
                if (i == j) {
                    continue;
                }
                for (int k : keySet()) {
                    if (i == k) {
                        continue;
                    }
                    if (j == k) {
                        continue;
                    }
                    if (isInf(i, k)) {
                        x = INF;
                    } else if (isInf(k, j)) {
                        x = INF;
                    } else if (isNegInf(i, k)) {
                        x = Integer.MIN_VALUE;
                    } else if (isNegInf(k, j)) {
                        x = Integer.MIN_VALUE;
                    } else {
                        x = get(i, k) + get(k, j);
                    }
                    if (isInf(i, j)) {
                        put(i, j, x);
                    } else {
                        if (get(i, j) > x) {
                            put(i, j, x);
                        }
                    }
                }
            }
        }




    }

   @Override
    public POSETMatrix clone()  {
        return new POSETMatrix(this);
    }
/**
 *
 * @param other
 */
    public POSETMatrix(POSETMatrix other) {
        INIT_SIZE = other.size();
        matrix = other.cloneMatrix();
        numFree = other.numFree;
        isFree = other.cloneIsFree();
        keyToIndex = other.cloneKeyMap();
       AUTO_GROW = true;
    }

    public POSETMatrix() {
    }

}
