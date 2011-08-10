package lmoore;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.Deflater;
import lmoore.zone.Zone;
import org.apache.hadoop.io.compress.zlib.BuiltInZlibDeflater;
import platu.Options;
import platu.lpn.LPN;
import platu.stategraph.state.State;

/**
 * State
 * @author Administrator
 */
public class TimedState extends State implements Serializable {
    protected Zone zone;

    public TimedState(final LPN thisLpn, int[] new_marking, int[] new_vector) {
        super(thisLpn, new_marking, new_vector);
    }
    
//    public TimedState(Markings newMarking, int[] new_vector, Zone zone) {
//        int[] tmp = newMarking.toArray();
//        this.marking = new int[tmp.length];
//        System.arraycopy(tmp, 0, this.marking, 0, this.marking.length);
//
//        this.vector = new_vector;
//        if (TimedStateGraph.TIMED_ANALYSIS) {
//            if (zone == null) {
//                throw new NullPointerException("zone==null");
//            }
//            this.zone = zone;
//        } else {
//            this.zone = null;
//        }
//
//        if (marking == null || vector == null) {
//            new NullPointerException().printStackTrace();
//        }
//
//        counts[0]++;
//    }

    public TimedState(int[] new_marking, int[] new_vector, Zone zone) {
        this.marking = new_marking;

        this.vector = new_vector;

        if (Options.getTimingAnalysisFlag()) {
            if (zone == null) {
                throw new NullPointerException("zone==null");
            }
            this.zone = zone;
        } else {
            this.zone = null;
        }

        if (marking == null || vector == null) {
            new NullPointerException().printStackTrace();
        }

        counts[0]++;
    }

    public TimedState(TimedState other) {
       super((State)other);
       this.zone = other.zone;
    	
    	if (other == null) {
            new NullPointerException().printStackTrace();
        }

        this.marking = new int[other.marking.length];
        System.arraycopy(other.marking, 0, this.marking, 0, other.marking.length);

        //this.vector = other.getVector().clone();
        this.vector = new int[other.vector.length];
        System.arraycopy(other.vector, 0, this.vector, 0, other.vector.length);

        // System.out.println("ORIG: "+st.getVector("pr "));
        // System.out.println("CLONE: "+vector);

        if (Options.getTimingAnalysisFlag()) {
            this.zone = null;//(Zone) other.getZone().clone();
        } else {
            this.zone = null;
        }
        // System.out.println("ORIG: "+st.getZone());
        // System.out.println("CLONE: "+zone);
        // this.type = st.getType();
        // new Exception().printStackTrace();
        // ID = nextID++;
        counts[0]++;
    }

    public TimedState() {
        this.marking = null;
        // System.out.println("ORIG: "+st.getMarking());
        // System.out.println("CLONE: "+marking);
        this.vector = null;//EMPTY_VECTOR.clone();
        // System.out.println("ORIG: "+st.getVector("pr "));
        // System.out.println("CLONE: "+vector);
        if (Options.getTimingAnalysisFlag()) {
            this.zone = null;
        } else {
            this.zone = null;
        }
        // System.out.println("ORIG: "+st.getZone());
        // System.out.println("CLONE: "+zone);
        // this.type = DEFAULT_TYPE;
        // new Exception().printStackTrace();
        counts[0]++;
        // ID = nextID++;
    }
    static PrintStream out = System.out;

    public String digest() {
        counts[9]++;
        String s = "" // + ID
                // marking.hashCode() //
                + marking.toString() + (zone != null ? zone.digest() : "")// !
                + vector.toString()// + vector.hashCode()
                ;
        s = s.replace(".", "").replace("+", "");
        try {
            // for(Provider pr: Security.getProviders()){
            // System.out.println(pr);
            // }
            byte[] data;
            Deflater def = new BuiltInZlibDeflater(
                    BuiltInZlibDeflater.BEST_COMPRESSION);
            def.setInput(s.getBytes());
            def.finish();
            ByteArrayOutputStream os = new ByteArrayOutputStream(
                    s.getBytes().length);
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
            // for(byte b:message){
            // s2+=Integer.toHexString(b&0xff);
            // }
            // byte[] data=s.getBytes();

            s = "";
            for (byte b : data) {
                s += Integer.toString(b & 0xff, Character.MAX_RADIX);
            }

        } catch (Exception ex) {
        }
        return s.substring(0, 8);
    }

    @Override
    public String toString() {
        return "\n" // + ID
                + "\tMARK: " + Arrays.toString(getMarking())
                + "; \n\tVECT: "
                + Arrays.toString(getVector())
                //+ "; \n\tTYPE: "// + getType()
                + "; ";
    }


    public boolean isFailure() {
        return false;// getType() != getType().NORMAL || getType() !=
        // getType().TERMINAL;
    }

    public static long tSum = 0;

    @Override
    public boolean equals(Object other) {
        TimedState s = (TimedState) other;
        if (s == this) {
            return true;
        }

        int[] thisMarking = this.marking;
        int[] otherMarking = s.marking;
        if (thisMarking.length != otherMarking.length) {
            return false;
        }

        HashSet<Integer> tmp = new HashSet();
        for (Integer this_m : thisMarking) {
            tmp.add(this_m);
        }

        for (Integer other_m : otherMarking) {
            if (tmp.contains(other_m) == false) {
                return false;
            }
        }

        if (!this.vector.equals(s.vector)) {
            return false;
        }
        if (Options.getTimingAnalysisFlag()) {
            if (zone == null) {
                throw new NullPointerException("zone==null");
            }
            if (s.zone == null) {
                throw new NullPointerException("s.zone==null");
            }
            if (!this.zone.equals(s.zone)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public TimedState clone() {
        counts[6]++;
        TimedState s = new TimedState(marking, vector, null
//                (zone != null ? getZone().clone() : null)// , getType()
                );

        return s;
    }

    @Override
    public int hashCode() {
        int markingHash = 19;
        int[] tmp = Arrays.copyOf(marking, marking.length);
        Arrays.sort(tmp);
        markingHash = Arrays.hashCode(tmp);
        // for(Integer m : this.marking) {
        // markingHash ^= m.hashCode();
        // }

        return (markingHash ^ Integer.rotateLeft(vector.hashCode(), 13) ^ (zone != null ? (Integer.rotateLeft(zone.hashCode(), 27)) : 9 / 11));
    }


    public String print() {
        System.out.print("Current makring: [");
        for (Integer i : marking) {
            System.out.print(i + ",");
        }
        System.out.println("]");
        //vector.print();
        return null;
    }

    /**
     * @return the marking
     */
    public int[] getMarking() {
        return marking;
    }

    public void setMarking(int[] newMarking) {
        marking = newMarking;
    }

    /**
     * @return the vector
     */
    public int[] getVector() {
        // new Exception("StateVector getVector(): "+s).printStackTrace();
        return vector;
    }

    /**
     * @return the zone
     */
//    public Zone getZone() {
//        //return zone;
//    	return null;
//    }

    //
    // /**
    // * @param type the type to set
    // */
    // public void setType(StateTypeEnum type) {
    // this.type = type;
    // }
    /**
     * @return the enabledSet
     */
    public int[] getEnabledSet() {
        return null;// enabledSet;
    }

    public String getEnabledSetString() {
        String ret = "";
        // for (int i : enabledSet) {
        // ret += i + ", ";
        // }

        return ret;
    }

    static public void printUsageStats() {
        System.out.printf("%-20s %11s\n", "State", counts[0]);
        System.out.printf("\t%-20s %11s\n", "State", counts[10]);
        // System.out.printf("\t%-20s %11s\n", "State", counts[11]);
        // System.out.printf("\t%-20s %11s\n", "merge", counts[1]);
        System.out.printf("\t%-20s %11s\n", "update", counts[2]);
        // System.out.printf("\t%-20s %11s\n", "compose", counts[3]);
        System.out.printf("\t%-20s %11s\n", "equals", counts[4]);
        // System.out.printf("\t%-20s %11s\n", "conjunction", counts[5]);
        System.out.printf("\t%-20s %11s\n", "clone", counts[6]);
        System.out.printf("\t%-20s %11s\n", "hashCode", counts[7]);
        // System.out.printf("\t%-20s %11s\n", "resembles", counts[8]);
        // System.out.printf("\t%-20s %11s\n", "digest", counts[9]);
    }

    public File serialize(String filename) throws FileNotFoundException,
            IOException {
        File f = new File(filename);
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f));
        os.writeObject(this);

        os.close();
        return f;
    }

    public static TimedState deserialize(String filename)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        File f = new File(filename);
        ObjectInputStream os = new ObjectInputStream(new FileInputStream(f));
        TimedState zone = (TimedState) os.readObject();
        os.close();
        return zone;
    }

    public static TimedState deserialize(File f) throws FileNotFoundException,
            IOException, ClassNotFoundException {
        ObjectInputStream os = new ObjectInputStream(new FileInputStream(f));
        TimedState zone = (TimedState) os.readObject();
        os.close();
        return zone;
    }
}
