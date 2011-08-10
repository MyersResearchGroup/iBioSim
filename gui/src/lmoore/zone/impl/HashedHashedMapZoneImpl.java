package lmoore.zone.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.Deflater;

import lmoore.zone.Zone;
import platu.Common;
import platu.Main;
import platu.lpn.LPNTran;
import platu.lpn.LpnTranList;
import org.apache.hadoop.io.compress.zlib.BuiltInZlibDeflater;
import static com.carrotsearch.hppc.IntIntOpenHashMap.*;

/**
 * @author ldtwo
 * //
 * @deprecated
 */
@Deprecated
public class HashedHashedMapZoneImpl extends Zone implements Serializable {
    private static final long serialVersionUID =1298734628974L;

    final static boolean DEBUG_PRINT = false;
    final TreeMap<Integer, HashedHashedMapRow> dbm;
    // HashSet<Integer> infTimers = new HashSet<Integer>(3);
    //public ArrayList<Integer> infTimers = new ArrayList<Integer>(3);
    //Vector ra = new Vector();
    HashedHashedMapRow row0 = new HashedHashedMapRow();

    public HashedHashedMapZoneImpl() {
        dbm=new TreeMap<Integer, HashedHashedMapRow>();
        row0.put(0, 0);
        put(0, row0);
    }

    public HashedHashedMapZoneImpl(int initSize) {
        dbm=new TreeMap<Integer, HashedHashedMapRow>();
        row0.put(0, 0);
        put(0, row0);
    }

//    public HashedHashedMapZoneImpl(HashedHashedMapZoneImpl m) {
//        dbm=new TreeMap<Integer, HashedHashedMapRow>(m);
//        row0 = get(0);
//    }

    @Override
    public void restrict(int transIndex, int low) {
        String s = toString();
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
        pr(this);
        if (DEBUG_PRINT) {
            out.println(Main.mergeColumns(s, toString(), "res        " + transIndex + "  " + low, 30, 30, 20));
        }
    }

//    public void restrict1(int transIndex, int low) {
//        pr("----------------------------------------------------- restrict2(t=" + transIndex + ", LB=" + low + ")");
//        //if( label(0) - label(j) > -low(j) ) then
//        if (get(transIndex, 0) + low > 0) {
//            //other(j0) = -low(j);
//            put(transIndex, 0, -low);
//        }
//    }
//    public final boolean eval(int lpntranlabel, int delay_lb) {
//        return true;
//    }
    @Override
    public void allocate(int t) {
        String s = toString();
        if (containsKey(t)) {
            return;
        }
        if (Main.ZONE_OPTIMIZE_INF) {
//            if (infTimers.contains(t)) {
//                //return;
//            }//infTimers.remove(t);
        }
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- extend(" + t + ")");
        }
        Iterator<Map.Entry<Integer, HashedHashedMapRow>> it_row =dbm.entrySet().iterator();
        //add column transIndex of existing rows and set them to 0
        Map.Entry<Integer, HashedHashedMapRow> rowEntry;
        while (it_row.hasNext()) { //foreach row (map) N, add N[transIndex] = upper bound of N
            rowEntry = it_row.next();
            //rowEntry.getValue().put(t,(int) rowEntry.getValue().get(new Integer(0)));
            rowEntry.getValue().put(t, (int) 0);
        }
        HashedHashedMapRow C = new HashedHashedMapRow();
        it_row = dbm.entrySet().iterator();
        for (; it_row.hasNext();) { //foreach row (map) N, add N[transIndex] = upper bound of N
            rowEntry = it_row.next();
//            C.put(rowEntry.getKey(), (get(0,rowEntry.getKey())));
            C.put(rowEntry.getKey(), 0);
        }
        C.put(t, 0);
        put(t, C);

        if (DEBUG_PRINT) {
            out.println(Main.mergeColumns(s, toString(), "ext        " + t, 30, 30, 20));
        }
    }
    /**
     * Low level call to remove something. This is not the same as project() in most cases.
     * @param t
     */
    @Override
    final public void remove(int t) {
        if(!contains(t)){ Common.pErr("Warning: Extra call! @remove("+t+")");return;}
        dbm.remove(t);
    }


    /**
     * Remove a row and column from the DBM.
     * @param t
     */
    @Override
    public void project(int t) {
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- project(" + t + ")");
        }
        Iterator<Map.Entry<Integer, HashedHashedMapRow>> it_row = dbm.entrySet().iterator();
        Map.Entry<Integer, HashedHashedMapRow> row;
        //remove column transIndex from each row
        for (; it_row.hasNext();) {
            row = it_row.next();
            get(row.getKey()).remove(t);
        }
        remove(t);
        if (DEBUG_PRINT) {
            String s = toString();
            out.println(Main.mergeColumns(s, toString(), "proj        " + t, 30, 30, 20));
        }
    }

    @Override
    public void recanonicalize() {
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- recanonicalization()");
        }
        String s = toString();
        int x;
        for (Map.Entry<Integer, HashedHashedMapRow> i : dbm.entrySet()) {
            for (Map.Entry<Integer, HashedHashedMapRow> j : dbm.entrySet()) {
                if ((int) i.getKey() != (int) j.getKey()) {
                    for (Map.Entry<Integer, HashedHashedMapRow> k : dbm.entrySet()) {
                        if ((int) i.getKey() == (int) k.getKey()) {
                            continue;
                        }
                        if ((int) j.getKey() == (int) k.getKey()) {
                            continue;
                        }
                        if (isInf(i.getKey(), k.getKey())) {
                            x = INF;
                        } else if (isInf(k.getKey(), j.getKey())) {
                            x = INF;
                        } else if (isNegInf(i.getKey(), k.getKey())) {
                            x = Integer.MIN_VALUE;
                        } else if (isNegInf(k.getKey(), j.getKey())) {
                            x = Integer.MIN_VALUE;
                        } else {
                            x = get(i.getKey(), k.getKey()) + get(k.getKey(), j.getKey());
                        }
                        if (isInf(i.getKey(), j.getKey())) {
                            put(i.getKey(), j.getKey(), x);
                        } else {
                            if (get(i.getKey(), j.getKey()) > x) {
                                put(i.getKey(), j.getKey(), x);
                            }
                        }
                    }
                }
            }
        }
        if (DEBUG_PRINT) {
            out.println(Main.mergeColumns(s, toString(), "recan        ", 30, 30, 20));
        }
    }

    @Override
    public void validate() {
        for (Map.Entry<Integer, HashedHashedMapRow> j : dbm.entrySet()) {
            put(j.getKey(), j.getKey(), 0);
        }
    }

    @Override
    public boolean isValid() {
        for (Map.Entry<Integer, HashedHashedMapRow> j : dbm.entrySet()) {
            if (get(j.getKey(), j.getKey()) != 0) {
                return false;
            }
        }
        return true;
    }

    static void pr(Object o) {
        if (Main.ZONE_ENABLE_PRINT) {
            out.println(o);
        }
    }

    @Override
    public String toString() {
        //String[] rows=new String[size()];
        String ret = "";
        Iterator<Map.Entry<Integer, HashedHashedMapRow>> itRow1 = dbm.entrySet().iterator();
        Map.Entry<Integer, HashedHashedMapRow> row;
        int r = 0;
        while (itRow1.hasNext()) {
            row = itRow1.next();
            if (r == 0) {
                ret += "   " + row.getValue().headRow() + "\n";
            }
            String t = row.getKey() + "";
            while (t.length() < 3) {
                t += " ";
            }


            //rows[r]=row.getValue().toString();
            if (itRow1.hasNext()) {
                ret += "          " + t + "|" + row.getValue().toString() + "|\n";
            } else {
                ret += "          " + t + "|" + row.getValue().toString() + "|";
            }
            r++;
        }
        return ret;
    }

    @Override
    public boolean put(int row, int col, int val) {
        //String s=toString();

        try {
//            if (OPTIMIZE_INF) {
//                if (infTimers.contains(col) || infTimers.contains(row)) {
//                   // return true;
//                }
//            }
            if (null != get(row)) {
                get(row).put(col, val);

                // out.println(Main.mergeColumns(s, toString(),   "put        "+val, 30,30, 20));
                return true;
            } else {
                System.err.println("ERROR: cannot put(" + row + ", " + col + ") -> " + val);
            }
            throw new NullPointerException("does not exits");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int get(int row, int col) {
//        if (!r.containsKey(col)) {
//            return NEG_INF;
//        }
        try {
            if (row == col) {
                return 0;
            }
            return get(row).get(col);
        } catch (Exception e) {
//            boolean b = containsKey(row);
//            System.err.println("ERROR: get(" + row + ", " + col + "): EXISTS? "
//                    + b + ", " + (b ? get(row).containsKey(col) : "false"));
//            e.printStackTrace();
            return NEG_INF;
        }
    }

    public boolean isInf(int row, int col) {
        try {
            if (Main.ZONE_OPTIMIZE_INF) {
//                if (infTimers.contains(col) || infTimers.contains(row)) {
//                    return true;
//                }
            }
            if (get(row, col) >= MIN_INF) {
                put(row, col, INF);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isNegInf(int row, int col) {
        try {
            if (get(row, col) <= MAX_NEG_INF) {
                put(row, col, Integer.MIN_VALUE);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    Integer timer;
    int tmp;

    @Override
    public boolean isTimeEnabled(int delayLB, int label) {
        if (delayLB == 0) {
            return true;
        }
//        timer = get(0, label);
        timer = row0.get(label);
        if (timer == null) {
            return true;
        }
        tmp = delayLB <= timer ? 1 : 0;
        if (Main.ZONE_OPTIMIZE_INF & tmp == 1) {
//            System.out.println(tmp + " = " + delayLB + " <= " + timer + " === ");
            if (timer > MIN_INF) {
                project(label);
            }
            return true;
        }
        return tmp == 1;
    }
    int hash = -1, hc;

    @Override
    public int hashCode() {
//        if (hash != -1) {
//            return hash;
//        }
        hash = Integer.MIN_VALUE;
        for (Map.Entry e : dbm.entrySet()) {
            hc = e.hashCode();
            hash += (Integer.rotateRight(hc, hc % 31));
        }
        return hash;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
//        if (!(o instanceof HashedHashedMapZoneImpl)) {
//            return false;
//        }
        Zone z = (Zone) o;
        if (this == z) {
            return true;
        }
        if (size() != z.size()) {
            return false;
        }
//         if(z.digest().compareToIgnoreCase(digest())==0)return true;

//        Map.Entry<Integer, HashedHashedMapRow> thisEntry;
//        Iterator<Map.Entry<Integer, HashedHashedMapRow>> it = this.entrySet().iterator();
//        while (it.hasNext()) {
//            thisEntry = it.next();
//            //if(thisEntry!=null)
//            if (!z.get((int) thisEntry.getKey()).equals(thisEntry.getValue())) {
//                return false;
//            }
//        }
        for (int i : dbm.keySet()) {
            for (int j : dbm.keySet()) {
                if (z.get(i, j) != get(i, j)) {//!!
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Zone clone() {
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- clone()");
        }
//        DEBUG_PRINT = false;
        HashedHashedMapZoneImpl z = new HashedHashedMapZoneImpl(size() + 1);
        //if (OPTIMIZE_INF)\
        {
//            z.infTimers = (ArrayList<Integer>) infTimers.clone();
        }
        for (Map.Entry<Integer, HashedHashedMapRow> en : dbm.entrySet()) {
            z.allocate(en.getKey());
        }
        for (Map.Entry<Integer, HashedHashedMapRow> en : dbm.entrySet()) {
            for (Map.Entry<Integer, Integer> en2 : en.getValue().entrySet()) {
                z.put(en.getKey(), en2.getKey(), en2.getValue());
            }
        }
        // out.println(Main.mergeColumns(z.toString(), toString(), "clone        "+z.equals(this), 30,30, 20));
//        DEBUG_PRINT = true;
        return z;
    }

    @Override
    public String digest() {
        System.exit(0);
        String s = "+Z." + size();
        String[] rows = new String[size()];
        int i = 0;
        for (Map.Entry<Integer, HashedHashedMapRow> e : dbm.entrySet()) {
            rows[i++] = e.getKey() + " " + e.getValue().digest();
        }
        Arrays.sort(rows);
        for (String r : rows) {
            s += "" + r;
        }
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

    @Override
    public String toHTMLTable() {
        String ret = "<TR><TD></TD>";
        for (Object i : dbm.keySet()) {
            ret += "<TD>" + i + "</TD>";
        }
        ret += "</TR>";
        for (Map.Entry<Integer, HashedHashedMapRow> row : dbm.entrySet()) {
            ret += "<TR><TD>" + row.getKey() + "</TD>";
            for (Map.Entry<Integer, Integer> col : row.getValue().entrySet()) {
                int i = col.getValue();
                ret += "<TD >"
                        + (i >= MIN_INF ? "INF" ://
                        (i < MAX_NEG_INF ? "-INF" : i)//
                        )
                        + "</TD>";
            }
            ret += "</TR>";
        }
        return ret + "";
    }

    @Override
    public boolean containsKey(int t) {
        return dbm.containsKey(t);
    }

    @Override
    public boolean contains(int t) {
        return dbm.containsKey(t);
    }

    public void put(int t, Object o) {
//        if (OPTIMIZE_INF) {
//            if (infTimers.contains(t)) {
//                return;
//            }
//        }
        if (o == null) {
            throw new NullPointerException("o==null");
        }
        dbm.put(t, (HashedHashedMapRow) o);
    }

    public HashedHashedMapRow get(int t) {
        return dbm.get(t);
    }

    public void normalize(int delayUB, int delayLB, int fired) {
        int premax = delayUB > MIN_INF ? delayUB : delayLB;
        for (int i : dbm.keySet()) {
            if (get(i, 0) > -premax) {
                for (int j : dbm.keySet()) {
                    if (i == j) {
                        continue;
                    }
                    put(i, j, get(i, j) - get(i, 0) + premax);
                    put(j, i, get(j, i) + get(i, 0) + premax);
                }
            }
        }
    }

    public static void main(String[] args) {
        String msg= "This is an example of what Wine.lpn should look like\n"
                + " when running and of how zones should be updated. Use this\n"
                + " code to test and compare two versions of Zone.\n"
                + "-Larry Moore\n"
                + "Spring 2011\n";
        System.out.printf("---------------------\n%s-----------------------\n",msg);
  
        Zone z2;
        MatrixZoneVer3Impl z1;
        boolean PRINT = true;
//        Main.ZONE_ENABLE_PRINT=true;
        z1 = new MatrixZoneVer3Impl();
        z2 = new HashedHashedMapZoneImpl();
//        z1=z2;
        Zone.equals(z1, z2);

//        initialize(z1, new int[]{6}, new int[]{3});
//        {
//            int LB = 2;
//            int fired = 6;
//            int[] newSet = {1, 2};
//            int[] enSet = {1, 2};
//            int[] nmnSet = {};
//            int[] ubSet = {3, 3};
//            System.out.printf("LB=%s\tFIRED=%s\tNEW: %s\tEN:%s\tUB: %s\tNMN: %s\n",
//                    LB, fired,
//                    Arrays.toString(newSet),
//                    Arrays.toString(enSet),
//                    Arrays.toString(ubSet),
//                    Arrays.toString(nmnSet));
////            out.println(z1);
//            update(z1, fired, LB, newSet, nmnSet, ubSet, enSet);
////            out.println("1 *******************************");
//        }
//        {
//            int LB = 2;
//            int fired = 1;
//            int[] newSet = {4};
//            int[] enSet = {2, 4};
//            int[] nmnSet = {2};
//            int[] ubSet = {3, 3};
//            System.out.printf("LB=%s\tFIRED=%s\tNEW: %s\tEN:%s\tUB: %s\tNMN: %s\n",
//                    LB, fired,
//                    Arrays.toString(newSet),
//                    Arrays.toString(enSet),
//                    Arrays.toString(ubSet),
//                    Arrays.toString(nmnSet));
//            update(z1, fired, LB, newSet, nmnSet, ubSet, enSet);
////            out.println("2 *******************************");
//        }
//        {
//            int LB = 2;
//            int fired = 2;
//            int[] newSet = {3};
//            int[] enSet = {3, 4};
//            int[] nmnSet = {4};
//            int[] ubSet = {INF, 3};
//            System.out.printf("LB=%s\tFIRED=%s\tNEW: %s\tEN:%s\tUB: %s\tNMN: %s\n",
//                    LB, fired,
//                    Arrays.toString(newSet),
//                    Arrays.toString(enSet),
//                    Arrays.toString(ubSet),
//                    Arrays.toString(nmnSet));
//            update(z1, fired, LB, newSet, nmnSet, ubSet, enSet);
////            out.println("3 *******************************");
//        }
//        {
//            int LB = 2;
//            int fired = 4;
//            int[] newSet = {};
//            int[] enSet = {3};
//            int[] nmnSet = {3};
//            int[] ubSet = {INF};
//            System.out.printf("LB=%s\tFIRED=%s\tNEW: %s\tEN:%s\tUB: %s\tNMN: %s\n",
//                    LB, fired,
//                    Arrays.toString(newSet),
//                    Arrays.toString(enSet),
//                    Arrays.toString(ubSet),
//                    Arrays.toString(nmnSet));
//            update(z1, fired, LB, newSet, nmnSet, ubSet, enSet);
////            out.println("3 *******************************");
//        }

        z1.allocate(6);
        z2.allocate(6);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "extend        " + Zone.equals(z1, z2), 30, 30, 20));
        }

        z1.put(0, 6, 3);
        z2.put(0, 6, 3);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "adv        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.recanonicalize();
        z2.recanonicalize();
        out.println(Main.mergeColumns(z1.toString(), z2.toString(), "recan        " + Zone.equals(z1, z2), 30, 30, 30));
        out.println("1   *********************************************");
        z1.restrict(6, 2);
        z2.restrict(6, 2);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "res        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.recanonicalize();
        z2.recanonicalize();
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "recan        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.project(6);
        z2.project(6);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "proj 6        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.allocate(1);
        z2.allocate(1);
        z1.allocate(2);
        z2.allocate(2);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "ext 1,2        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.put(0, 1, 3);
        z1.put(0, 2, 3);
        z2.put(0, 1, 3);
        z2.put(0, 2, 3);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "adv        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.recanonicalize();
        z2.recanonicalize();
        out.println(Main.mergeColumns(z1.toString(), z2.toString(), "recan        " + Zone.equals(z1, z2), 30, 30, 20));
        out.println("2   *********************************************");
        z1.restrict(1, 2);
        z2.restrict(1, 2);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "res        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.recanonicalize();
        z2.recanonicalize();
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "recan        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.project(1);
        z2.project(1);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "proj 1        " + Zone.equals(z1, z2), 30, 30, 20));
        }

        z1.allocate(4);
        z2.allocate(4);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "ext 1,2        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.put(0, 4, 3);
        z2.put(0, 4, 3);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "adv        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.recanonicalize();
        z2.recanonicalize();
        out.println(Main.mergeColumns(z1.toString(), z2.toString(), "recan        " + Zone.equals(z1, z2), 30, 30, 20));
        out.println("3   *********************************************");
        z1.restrict(2, 2);
        z2.restrict(2, 2);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "res        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.recanonicalize();
        z2.recanonicalize();
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "recan        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.project(2);
        z2.project(2);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "proj 1        " + Zone.equals(z1, z2), 30, 30, 20));
        }


//        z1 = (HashedHashedMapZoneImpl) z1.clone();
        z2 = (MatrixZoneVer1Impl) z2.clone();
        z1.allocate(3);
        z2.allocate(3);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "ext 1,2        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.put(0, 3, INF);
        z2.put(0, 3, INF);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "adv        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.recanonicalize();
        z2.recanonicalize();
        out.println(Main.mergeColumns(z1.toString(), z2.toString(), "recan        " + Zone.equals(z1, z2), 30, 30, 20));
        z1.restrict(4, 2);
        z2.restrict(4, 2);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "res        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.recanonicalize();
        z2.recanonicalize();
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "recan        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.project(4);
        z2.project(4);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "proj 1        " + Zone.equals(z1, z2), 30, 30, 20));
        }

        z1.put(0, 3, INF);
        z2.put(0, 3, INF);
        if (PRINT) {
            out.println(Main.mergeColumns(z1.toString(), z2.toString(), "adv        " + Zone.equals(z1, z2), 30, 30, 20));
        }
        z1.recanonicalize();
        z2.recanonicalize();
        out.println(Main.mergeColumns(z1.toString(), z2.toString(), "recan        " + Zone.equals(z1, z2), 30, 30, 20));
        z1.restrict(3, 5);
        z2.restrict(3, 5);
        out.println(Main.mergeColumns(z1.toString(), z2.toString(), "res        " + Zone.equals(z1, z2), 30, 30, 20));
        z1.recanonicalize();
        z2.recanonicalize();
        out.println(Main.mergeColumns(z1.toString(), z2.toString(), "recan        " + Zone.equals(z1, z2), 30, 30, 20));
        z1.project(3);
        z2.project(3);
        out.println(Main.mergeColumns(z1.toString(), z2.toString(), "proj 1        " + Zone.equals(z1, z2), 30, 30, 20));

//serialize
//  System.out.println(z2.digest());
//  System.out.println(z2.digest().length());
//        try {
//            File z3Serialized = z1.serialize("zone3.ser");
//            File z2Serialized1 = z2.serialize("zone2.1.ser");
//            File z2Serialized2 = z2.serialize2("zone2.2.ser");
//System.out.println("SERIALIZED HashedHashedMapZoneImpl: "+z3Serialized.getAbsolutePath());
//System.out.println("SERIALIZED HashedHashedMapZoneImpl: "+z2Serialized1.getAbsolutePath());
//
//System.out.println("DESERIALIZED: HashedHashedMapZoneImpl as MatrixZoneVer1Impl\n"+MatrixZoneVer1Impl.deserialize(z3Serialized));
//System.out.println("DESERIALIZED: HashedHashedMapZoneImpl as HashedHashedMapZoneImpl\n"+HashedHashedMapZoneImpl.deserialize(z3Serialized));
//System.out.println("DESERIALIZED: MatrixZoneVer1Impl as MatrixZoneVer1Impl\n"+MatrixZoneVer1Impl.deserialize(z2Serialized1));
//System.out.println("DESERIALIZED: MatrixZoneVer1Impl as HashedHashedMapZoneImpl\n"+HashedHashedMapZoneImpl.deserialize(z2Serialized1));
//            int i = 0;
//            long t1 = System.currentTimeMillis();
//            for (; i < 1000; i++) {
//                MatrixZoneVer1Impl.deserialize(z2Serialized1);
//                MatrixZoneVer1Impl.deserialize(z2Serialized1);
//                MatrixZoneVer1Impl.deserialize(z2Serialized1);
//                MatrixZoneVer1Impl.deserialize(z2Serialized1);
//                MatrixZoneVer1Impl.deserialize(z2Serialized1);
//                MatrixZoneVer1Impl.deserialize(z2Serialized1);
//                MatrixZoneVer1Impl.deserialize(z2Serialized1);
//                MatrixZoneVer1Impl.deserialize(z2Serialized1);
//                MatrixZoneVer1Impl.deserialize(z2Serialized1);
//                MatrixZoneVer1Impl.deserialize(z2Serialized1);
//            }
//            t1 = System.currentTimeMillis() - t1;
//            System.out.println("METHOD 1: " + t1*1000 + " us");
////System.out.println("DESERIALIZED(1): MatrixZoneVer1Impl\n"+MatrixZoneVer1Impl.deserialize(z2Serialized1));
//            long t2 = System.nanoTime();
//            for (; i < 1000; i++) {
//                MatrixZoneVer1Impl.deserialize2(z2Serialized2);
//                MatrixZoneVer1Impl.deserialize2(z2Serialized2);
//                MatrixZoneVer1Impl.deserialize2(z2Serialized2);
//                MatrixZoneVer1Impl.deserialize2(z2Serialized2);
//                MatrixZoneVer1Impl.deserialize2(z2Serialized2);
//                MatrixZoneVer1Impl.deserialize2(z2Serialized2);
//                MatrixZoneVer1Impl.deserialize2(z2Serialized2);
//                MatrixZoneVer1Impl.deserialize2(z2Serialized2);
//                MatrixZoneVer1Impl.deserialize2(z2Serialized2);
//                MatrixZoneVer1Impl.deserialize2(z2Serialized2);
//            }
//            t2 = System.nanoTime() - t2;
//
//            System.out.println("METHOD 2: " + t2/1000f + " us");
////System.out.println("DESERIALIZED(2): MatrixZoneVer1Impl\n"+MatrixZoneVer1Impl.deserialize2(z2Serialized2));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
        ObjectInputStream os = new ObjectInputStream(new FileInputStream(f));
        Zone zone = (Zone) os.readObject();
        os.close();
        return zone;
    }

    public static Zone deserialize(File f)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream os = new ObjectInputStream(new FileInputStream(f));
        Zone zone = (Zone) os.readObject();
        os.close();
        return zone;
    }

    void updateZone_S(Zone zone, LPNTran firedTran,
            LpnTranList nextSet, LpnTranList currentSet) {
        int fired = firedTran.getID();

        //restrict

//            infTimer = !((zone).contains(fired));
//        if (!infTimer) {
//            int val = zone.get(fired, 0);
//            if (val == Integer.MIN_VALUE + 1) {
//                new Exception("zone.get==-1: tran=" + firedTran
//                        + "\n\tzone=" + zone).printStackTrace(platu.Main.out);
//                getChar();
//            }
//            if (val > -firedTran.getDelayLB()) {
//                zone.put(fired, 0, -firedTran.getDelayLB());
//            }
//            zone.restrict(fired, firedTran.getDelayLB());
//            //tighten loose bounds
//            zone.recanonicalize();
//
//            //project out fired rule
//            zone.project(fired);
//        }
        //extend DMB with new timers
        //newEnSet = next - current
        LpnTranList newEnSet = new LpnTranList(nextSet);
        newEnSet.removeAll(currentSet);
        //setB = next - new
        LpnTranList nextMinusNew = new LpnTranList(nextSet);
        nextMinusNew.removeAll(newEnSet);
        zoneAllocate_S(newEnSet, zone);
        int lblI, lblK;
        boolean infTimer;
        for (LPNTran i : newEnSet) {
            lblI = i.getID();
            infTimer = !((zone).contains(lblI));
            //Mi0 = M0i = 0;
            zone.put(lblI, 0, 0);
            zone.put(0, lblI, 0);
            for (LPNTran k : newEnSet) {
                lblK = k.getID();
                //Mik = Mki = 0;
                zone.put(lblI, lblK, 0);
                zone.put(lblK, lblI, 0);
            }
            for (LPNTran k : nextMinusNew) {
                lblK = k.getID();
                //if (Main.ZONE_OPTIMIZE_INF)
                {
                    infTimer = !((zone).contains(lblK));
                    if (infTimer) {
                        continue;
                    }
                }
                //M(ik)=M(0k)
                zone.put(lblI, lblK, zone.get(0, lblK));
                zone.put(lblK, lblI, zone.get(lblK, 0));
            }
        }

        //advance time
        for (LPNTran t : nextSet) {
            boolean b = zone.put(0, t.getID(), t.getDelayUB());
            if (!b) {
                new Exception("zone.put==false: lpnt=" + t + "\n\tzone=" + zone).printStackTrace(platu.Main.out);
                Common.getChar();
            }
        }
        //tighten loose bounds
        zone.recanonicalize();

        if (!zone.isValid()) {
            new Exception("zone.isValid==false: lpnt=" + firedTran + "\n\tzone=" + zone).printStackTrace(platu.Main.out);
            Common.getChar();
        }
        (zone).remove(fired);
    }

    private void zoneAllocate_S(LpnTranList newEnSet, Zone zone) {
        //allocate the space to be modified later
        for (LPNTran t2 : newEnSet) {
            if (!zone.containsKey(t2.getID())) {
                zone.allocate(t2.getID());
            }
        }
    }

    public int getID() {
        return this.ID;
    }

    @Override
    public int size() {
      return dbm.size();
    }

    @Override
    public Object keySet() {
      return dbm.keySet();
    }
}
