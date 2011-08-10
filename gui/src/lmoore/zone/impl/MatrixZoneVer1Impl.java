/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lmoore.zone.impl;

import java.io.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.Deflater;

import lmoore.zone.Zone;
import lmoore.zone.matrix.MatrixVer1;

import org.apache.hadoop.io.compress.zlib.BuiltInZlibDeflater;
import platu.Main;

/**
 *
 * @author ldtwo
 */
@Deprecated
public class MatrixZoneVer1Impl extends MatrixVer1 implements Serializable {

    private static final long serialVersionUID = 293487239487243L;

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
        for (Entry<Integer, Integer> e : keyToIndex.entrySet()) {
            out.writeInt(e.getKey());
            out.writeInt(e.getValue());
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
        keyToIndex = new HashMap<Integer, Integer>(length);
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
        for (Entry<Integer, Integer> e : keyToIndex.entrySet()) {
            out.writeInt(e.getKey());
            out.writeInt(e.getValue());
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
        keyToIndex = new HashMap<Integer, Integer>(length);
        for (int i = 0; i < length; i++) {
            keyToIndex.put(in.readInt(), in.readInt());
        }
    }
//    static final Integer s = 0;

    /**
     *
     */
    public MatrixZoneVer1Impl() {
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- new Zone()");
        }
        put((short) 0, (short) 0, 0);

    }

    /**
     *
     * @param z
     */
    public MatrixZoneVer1Impl(MatrixZoneVer1Impl z) {
        super(z);
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- new Zone()");
        }
        put((short) 0, (short) 0, 0);
    }

    /**
     *
     * @return
     */
    @Override
    final public Zone clone() {
        return new MatrixZoneVer1Impl(this);

    }

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
        remove(t);
    }

    /**
     *
     */
    public void recanonicalize() {
        if (Main.ZONE_ENABLE_PRINT) {
            pr("----------------------------------------------------- recanonicalization()");
        }
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

    /**
     * Force the diagonal to be zeros.
     */
    public void validate() {
        for (int j : keySet()) {
            put(j, j, 0);
        }
    }

    /**
     *
     * @return
     */
    public boolean isValid() {
        for (int j : keySet()) {
            if (get(j, j) != 0) {
                return false;
            }
        }
        return true;
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
    Integer timer;
    int tmp;

    /**
     *
     * @param delayLB
     * @param label
     * @return
     */
    public boolean isTimeEnabled(int delayLB, int label) {
        try {
            timer = get(0, label);
        } catch (Exception e) {
            return true;
        }
        if (timer == null) {
            return true;
        }
        tmp = delayLB <= timer ? 1 : 0;
        if (Main.ZONE_OPTIMIZE_INF & tmp == 1) {
            project(label);
        }
        return tmp == 1;
    }

    /**
     *
     * @return
     */
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
        MatrixZoneVer1Impl zone = new MatrixZoneVer1Impl();
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

    public int getID() {
        return this.ID;
    }
}
