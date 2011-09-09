package platu.stategraph;

import java.io.*;
import java.util.*;

import platu.common.PlatuObj;
import platu.lpn.DualHashMap;
import platu.lpn.LPN;
import platu.lpn.VarSet;

/**
 * State
 * @author Administrator
 */
public class State extends PlatuObj {

    public static int[] counts = new int[15];
    
    protected int[] marking;
    protected int[] vector;
    private int hashVal = 0;
    private LPN lpnModel = null;
    private int index;
    private boolean localEnabledOnly;
    protected boolean failure = false;

    @Override
    public String toString() {
//        String ret=Arrays.toString(marking)+""+
//               Arrays.toString(vector);
//        return "["+ret.replace("[", "{").replace("]", "}")+"]";
    	return this.print();
    }

    public State(final LPN thisLpn, int[] new_marking, int[] new_vector) {
    	this.lpnModel = thisLpn;
        this.marking = new_marking;
        this.vector = new_vector;

        if (marking == null || vector == null) {
            new NullPointerException().printStackTrace();
        }
        
    	Arrays.sort(this.marking);
    	this.index = 0;
        localEnabledOnly = false;
        counts[0]++;
    }

    public State(State other) {
        if (other == null) {
            new NullPointerException().printStackTrace();
        }
        
        this.lpnModel = other.lpnModel;        	
        this.marking = new int[other.marking.length];
        System.arraycopy(other.marking, 0, this.marking, 0, other.marking.length);

        this.vector = new int[other.vector.length];
        System.arraycopy(other.vector, 0, this.vector, 0, other.vector.length);

//        this.hashVal = other.hashVal;
        this.hashVal = 0;
        this.index = other.index;
        this.localEnabledOnly = other.localEnabledOnly;
        counts[0]++;
    }

    public State() {
        this.marking = new int[0];
        this.vector = new int[0];//EMPTY_VECTOR.clone();
        this.hashVal = 0;
        this.index = 0;
        localEnabledOnly = false;
        counts[0]++;
    }
    //static PrintStream out = System.out;

    public State(Object otherState) {
        State other = (State) otherState;
        if (other == null) {
            new NullPointerException().printStackTrace();
        }
        
        this.lpnModel = other.lpnModel;  	        
        this.marking = new int[other.marking.length];
        System.arraycopy(other.marking, 0, this.marking, 0, other.marking.length);

       // this.vector = other.getVector().clone();
        this.vector = new int[other.vector.length];
        System.arraycopy(other.vector, 0, this.vector, 0, other.vector.length);
        
        this.hashVal = other.hashVal;
        this.index = other.index;
        this.localEnabledOnly = other.localEnabledOnly;
        counts[0]++;
    }
    
    public void setLpn(final LPN thisLpn) {
    	this.lpnModel = thisLpn;
    }
    
    public LPN getLpn() {
    	return this.lpnModel;
    }
    
    public void setLabel(String lbl) {
    	
    }
    
    public String getLabel() {
    	return null;
    }
    
    public void setIndex(int newIndex) {
    	this.index = newIndex;
    }
    
    public int getIndex() {
    	return this.index;
    }
    
    public boolean hasNonLocalEnabled() {
    	return this.localEnabledOnly;
    }
    
    public void hasNonLocalEnabled(boolean nonLocalEnabled) {
    	this.localEnabledOnly = nonLocalEnabled;
    }

    public boolean isFailure() {
        return false;// getType() != getType().NORMAL || getType() !=
        // getType().TERMINAL;
    }

    public static long tSum = 0;

    @Override
    public State clone() {
        counts[6]++;
        State s = new State(this);
        return s;
    }

    public String print() {
    	DualHashMap<String, Integer> VarIndexMap = this.lpnModel.getVarIndexMap();
    	String message = "Marking: [";
        for (int i : marking) {
            message += i + ",";
        }
        message += "]\n" + "Vector: [";
        for (int i = 0; i < vector.length; i++) {
            message += VarIndexMap.getKey(i) + "=>" + vector[i]+", ";
        }
        message += "]\n";
    
        return message;
    }

	@Override
	public int hashCode() {
		if(hashVal == 0){
			final int prime = 31;
			int result = 1;
			result = prime * result + ((lpnModel == null) ? 0 : lpnModel.getLabel().hashCode());
			result = prime * result + Arrays.hashCode(marking);
			result = prime * result + Arrays.hashCode(vector);
			hashVal = result;
		}
		
		return hashVal;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		State other = (State) obj;
		if (lpnModel == null) {
			if (other.lpnModel != null)
				return false;
		} 
		else if (!lpnModel.equals(other.lpnModel))
			return false;
		
		if (!Arrays.equals(marking, other.marking))
			return false;
		
		if (!Arrays.equals(vector, other.vector))
			return false;
		
		return true;
	}

	public void print(DualHashMap<String, Integer> VarIndexMap) {
        System.out.print("Marking: [");
        for (int i : marking) {
            System.out.print(i + ",");
        }
        System.out.println("]");
        
        System.out.print("Vector: [");
        for (int i = 0; i < vector.length; i++) {
            System.out.print(VarIndexMap.getKey(i) + "=>" + vector[i]+", ");
        }
        System.out.println("]");
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

    public HashMap<String, Integer> getOutVector(VarSet outputs, DualHashMap<String, Integer> VarIndexMap) {
    	HashMap<String, Integer> outVec = new HashMap<String, Integer>();
    	for(int i = 0; i < vector.length; i++) {
    		String var = VarIndexMap.getKey(i);
    		if(outputs.contains(var) == true)
    			outVec.put(var, vector[i]);
    	}
    	
    	return outVec;
    }

    public State getLocalState() {
    	VarSet lpnOutputs = this.lpnModel.getOutputs();
    	VarSet lpnInternals = this.lpnModel.getInternals();
    	DualHashMap<String,Integer> varIndexMap = this.lpnModel.getVarIndexMap();
    	
    	int[] outVec = new int[this.vector.length];
    	
    	/*
    	 * Create a copy of the vector of mState such that the values of inputs are set to 0
    	 * and the values for outputs/internal variables remain the same.
    	 */
    	for(int i = 0; i < this.vector.length; i++) {
    		String curVar = varIndexMap.getKey(i);
    		if(lpnOutputs.contains(curVar) ==true || lpnInternals.contains(curVar)==true)
    			outVec[i] = this.vector[i];
    		else
    			outVec[i] = 0;
    	}
    	
    	return new State(this.lpnModel, this.marking, outVec);
    }
    
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

    /**
     * Return a new state if the newVector leads to a new state from this state; otherwise return null.
     * @param newVector
     * @param VarIndexMap
     * @return
     */
    public State update(HashMap<String, Integer> newVector, DualHashMap<String, Integer> VarIndexMap) {
    	int[] newStateVector = new int[this.vector.length];
    	
    	boolean newState = false;
    	for(int index = 0; index < vector.length; index++) {
    		String var = VarIndexMap.getKey(index);
    		int this_val = this.vector[index];
    		
			Integer newVal = newVector.get(var);
    		if(newVal != null) {
    			if(this_val != newVal) {
    				newState = true;
    				newStateVector[index] = newVal;
    			}
    			else
    				newStateVector[index] = this.vector[index]; 
    		}
    		else
    			newStateVector[index] = this.vector[index];    		
    	}
    
    	if(newState == true)
    		return new State(this.lpnModel, this.marking, newStateVector);
    	
    	return null;
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
//TODO: try database serialization
    public File serialize(String filename) throws FileNotFoundException,
            IOException {
        File f = new File(filename);
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f));
        os.writeObject(this);

        os.close();
        return f;
    }

    public static State deserialize(String filename)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        File f = new File(filename);
        ObjectInputStream os = new ObjectInputStream(new FileInputStream(f));
        State zone = (State) os.readObject();
        os.close();
        return zone;
    }

    public static State deserialize(File f) throws FileNotFoundException,
            IOException, ClassNotFoundException {
        ObjectInputStream os = new ObjectInputStream(new FileInputStream(f));
        State zone = (State) os.readObject();
        os.close();
        return zone;
    }
    
    public boolean failure(){
    	return this.failure;
    }
    
    public void setFailure(){
    	this.failure = true;
    }
}
