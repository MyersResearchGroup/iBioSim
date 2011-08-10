/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lmoore;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author ldtwo
 */
public abstract class POSET {

    public static final int INF = (int) (Integer.MAX_VALUE);

    abstract public void project(int t);

    abstract public void recanonicalization();

    abstract public int size();

    abstract public void remove(int label);

    abstract public int get(int row, int col);

    abstract public boolean put(int row, int col, int val);

    abstract public boolean put_unchecked(int row, int col, int val);

    abstract public void put_direct_unchecked(int row, int col, int val);

    abstract public int get_direct_unchecked(int row, int col);

    abstract public int getIndex_unchecked(int label);

    abstract public void allocate(int label);

    abstract public boolean contains(int label);

    abstract public Set<Integer> keySet();

    abstract public TreeSet<Integer> treeKeySet();

    abstract public Iterator<Integer> keyIterator();

    abstract public String toString();

    abstract long getLongHash();

    abstract public int hashCode();

    abstract public boolean equals(Object o);

    abstract public boolean containsKey(int label);

    abstract public String toHTMLTable();

    abstract public boolean isInf(int row, int col);

    abstract public boolean isNegInf(int row, int col);
}
