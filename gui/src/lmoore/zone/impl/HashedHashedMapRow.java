package lmoore.zone.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.RandomAccess;
import java.util.TreeMap;

import static lmoore.zone.Zone.*;

/**
 *Uses bak.pcj.map.IntKeyIntChainedHashMap<br>
 * used with Zone3
 * @author ldtwo
 * @deprecated
 */
@Deprecated public class HashedHashedMapRow extends TreeMap<Integer, Integer> implements RandomAccess {

    private static final long serialVersionUID = 21934812093L;

    public Integer get(int key) {
        return super.get(key);
    }

    @Override
    public String toString() {
        Iterator<Map.Entry<Integer, Integer>> it = entrySet().iterator();
        String ret = "";
        Map.Entry<Integer, Integer> en;
        while (it.hasNext()) {
            //en=it.next();

            int t = it.next().getValue();
            String s;
            s = "" + (t >= MIN_INF ? "INF" ://
                    (t < MAX_NEG_INF ? "-INF" : t)//
                    );

            while (s.length() < 3) {
                s = " " + s;
            }
            ret += s + "  ";
        }
        return ret;
    }

    public String headRow() {
        Iterator<Map.Entry<Integer, Integer>> it = entrySet().iterator();
        String ret = "";
        Map.Entry<Integer, Integer> en;
        while (it.hasNext()) {
            //en=it.next();
            String s = it.next().getKey() + "";
            while (s.length() < 3) {
                s = " " + s;
            }
            ret += s + "  ";
        }
        String s = "";
        while (s.length() < ret.length() + 2) {
            s = "_" + s;
        }


        return "           " + ret + "\n             " + s;
        //return super.toString();
    }

    char[] digest2() {
        char[] b = new char[size() * 2];
        Map.Entry<Integer, Integer> en;
        Iterator<Map.Entry<Integer, Integer>> it = entrySet().iterator();
        for (int i = 0; i < b.length; i++) {
            en = it.next();
            b[i] = (char) (int) en.getKey();
            i++;
            b[i] = (char) (int) en.getValue();
        }
        return b;
    }

    @Override
    public int hashCode() {
        int i = Integer.MIN_VALUE;
        for (Map.Entry e : entrySet()) {
            i ^= (Integer.rotateRight(e.hashCode(), e.hashCode() % 31));
        }
        return i;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        HashedHashedMapRow other = (HashedHashedMapRow) o;
        if (this == other) {
            return true;
        }
        if (size() != other.size()) {
            return false;
        }
        Map.Entry<Integer, Integer> thisEntry;
        Iterator<Map.Entry<Integer, Integer>> it = this.entrySet().iterator();
        while (it.hasNext()) {
            thisEntry = it.next();
            if (other.get((int) thisEntry.getKey()) != ((int) thisEntry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public String digest() {
        String s = "." + size();
        String[] rows = new String[size()];
        int i = 0;
        for (Map.Entry e : entrySet()) {
            rows[i++] = e.getKey() + " " + e.getValue();
        }
        Arrays.sort(rows);
        for (String r : rows) {
            s += "" + r;
        }
        return s.replace(" ", "").replace(".", "").replace("+", ".")//
                .replace("\n", "").replace("\t", "").replace("_", "")//
                .replace("|", "").replace("\\", "").replace("-", "")//
                .replace(",", "").replace("[", "").replace("]", "");
    }
}
