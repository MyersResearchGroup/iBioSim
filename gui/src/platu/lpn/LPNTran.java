package platu.lpn;

import java.io.*;
import platu.expression.Expression;
import platu.expression.VarNode;
import platu.logicAnalysis.Constraint;
import java.util.*;
import java.util.Map.Entry;
import lmoore.zone.Zone;
import platu.Main;
import platu.stategraph.state.State;

public class LPNTran {
    public static final Collection<Integer> toList(int[] arr) {
        TreeSet<Integer> l = new TreeSet<Integer>();
        for (int i : arr) {
            l.add(i);
        }
        return l;
    }

    public static final int[] toArray(Collection<Integer> set) {
        int[] arr = new int[set.size()];
        int idx = 0;
        for (int i : set) {
            arr[idx++] = i;
        }
        return arr;
    }
    
    public static PrintStream out = Main.out;
    public static int[] counts = new int[15];
    public static boolean ENABLE_PRINT = true;
    public static int PRINT_LEVEL = 10;
    
    public LpnTranList mSet = new LpnTranList();
    private LPN lpn; // Pointer to the LPN where this tran is defined.
    private String label;   
    private int index; // Index of this transition in LPN
    private int[] preSet;
    private int[] postSet;
    private Expression enablingGuard; // Enabling condition
    private VarExprList assignments = new VarExprList();
    private int delayLB;  // Lower bound of the delay
    private int delayUB;  // Upper bound of the delay.
    private boolean visible; // if output variables defined.
    private HashSet<VarNode> assignedVarSet;
    private HashSet<String> supportSet;
    private boolean local = true;
    private ArrayList<Expression> assertions = new ArrayList<Expression>(1);
    private HashMap<Object, Object> nextStateMap;  //current state, next state
    private Set<Constraint> constraintSet = new HashSet<Constraint>();
    private List<LPN> lpnList = new ArrayList<LPN>();
    private boolean stickyFlag = true;
    
    public void setSticky(){
    	this.stickyFlag = true;
    }

    public boolean sticky(){
    	return this.stickyFlag;
    }
    
    public LPNTran(String Label, int index, Collection<Integer> preSet, Collection<Integer> postSet,
            Expression enablingGuard, VarExprList assignments, int DelayLB, int DelayUB, boolean local) {
        //this.lpnModel=lpnModel;
        this.label = Label;
        this.index = index;
        this.preSet = toArray(preSet);
        this.postSet = toArray(postSet);
        this.enablingGuard = enablingGuard;
        this.assignments = assignments;
        this.delayLB = DelayLB;
        this.delayUB = DelayUB;
        this.local = local;
        visible = true;
        assignedVarSet = new HashSet<VarNode>();
        nextStateMap = new HashMap<Object, Object>();

        if (preSet == null || postSet == null || enablingGuard == null || assignments == null) {
            new NullPointerException().printStackTrace();
        }

        counts[0]++;
    }
    
    public LPNTran(String Label, int index, int[] preSet, int[] postSet, Expression enablingGuard, 
    		VarExprList assignments, int DelayLB, int DelayUB, boolean local) {
        this.label = Label;
        this.index = index;
        this.preSet = preSet;
        this.postSet = postSet;
        this.enablingGuard = enablingGuard;
        this.assignments = assignments;
        this.delayLB = DelayLB;
        this.delayUB = DelayUB;
        this.local = local;
        visible = true;
        assignedVarSet = new HashSet<VarNode>();
        nextStateMap = new HashMap<Object, Object>();

        if (preSet == null || postSet == null || enablingGuard == null || assignments == null) {
            new NullPointerException().printStackTrace();
        }

        counts[0]++;
    }
    
    public LPNTran(LPNTran lpnTran) {
    	this.label = lpnTran.label;
    	this.preSet = lpnTran.preSet;
    	this.postSet = lpnTran.postSet;
    	this.enablingGuard = lpnTran.enablingGuard;
    	this.assignments = lpnTran.assignments;
    	this.delayLB = lpnTran.delayLB;
    	this.delayUB = lpnTran.delayUB;
    	this.local = lpnTran.local;
    	this.assignedVarSet = new HashSet<VarNode>();
    	nextStateMap = new HashMap<Object, Object>();
    }
    
//    @Override
//	public boolean equals(final Object other) {
//		LPNTran otherTran = (LPNTran)other;
//
//		if(this.lpn != otherTran.lpn)
//			return false;
//
//		if(this.label != otherTran.label)
//			return false;
//
//		return true;
//	}
	
//	@Override
//	public int hashCode() {
//		return Integer.rotateLeft(this.lpn.getLabel().hashCode(), this.getLabel());
//	}
    
    public void initialize(final LPN lpnModel, final VarSet outputs) {
    	// Computer the variables on the left hand side of the assignments.
    	this.lpn = lpnModel;
    	this.supportSet = new HashSet<String>();
    	
		HashSet<VarNode> guardVarSet = this.enablingGuard.getVariables();
		for(VarNode var : guardVarSet){
			this.supportSet.add(var.getName());
		}
    	
    	// Determine the visibility of this tran.
    	this.visible = false;
    	for (VarExpr assign : this.assignments) {
    		assignedVarSet.add(assign.getVar());
    		
    		if(outputs.contains(assign.getVar().getName()) == true) {
    			this.visible = true;
         	}
    		
    		HashSet<VarNode> tmp = assign.getExpr().getVariables();
    		for(VarNode var : tmp){
    			this.supportSet.add(var.getName());
    		}
        }	
    }
    
    public HashSet<VarNode> getAssignedVar() {
    	return this.assignedVarSet;
    }
    
    public HashSet<String> getSupportVar() {
    	return this.supportSet;
    }

	/**
	 * Check if firing 'fired_transition' causes a disabling error.
	 * @param current_enabled_transitions
	 * @param next_enabled_transitions
	 * @return
	 */
	public LPNTran disablingError(
			final LinkedList<LPNTran> current_enabled_transitions,
			final LinkedList<LPNTran> next_enabled_transitions) {
		if (current_enabled_transitions == null || current_enabled_transitions.size()==0)
			return null;
		
		for(LPNTran curTran : current_enabled_transitions) {
			boolean disabled = true;
			if (next_enabled_transitions != null && next_enabled_transitions.size()!=0) {
				for(LPNTran nextTran : next_enabled_transitions) {
					if(curTran == nextTran) {
						disabled = false;
						break;
					}
				}
			}

			if (disabled == true) {
				if(this.sharePreSet(curTran) == false)
					return curTran;
			}
		}

		return null;
	}
    
   static public LPNTran disablingError(final LinkedList<LPNTran> current_enabled_transitions, final LinkedList<LPNTran> next_enabled_transitions, LPNTran ths) {
       return ths.disablingError(current_enabled_transitions, next_enabled_transitions);
   }
   
   static public LPNTran disablingError(final LPNTran[] current_enabled_transitions, final LPNTran[] next_enabled_transitions,LPNTran ths) {
        counts[10]++;
        //  current_enabled_transitions.remove(fired_transition);
        // return current_enabled_transitions.containsAll(next_enabled_transitions);

        if (current_enabled_transitions == null) {
            return null;
        }

        for (LPNTran cet:current_enabled_transitions) {
            if(cet == ths)
            	continue;

            boolean disabled = true;
            if(next_enabled_transitions != null) {
            	for (LPNTran net:next_enabled_transitions) {

//            		System.out.println("checking curTran " + cet.getLpn().getLabel()+":"+cet.getLabel()
//            				+ " with " + net.getLpn().getLabel()+":"+net.getLabel());

            		if (cet.label == net.label) {
            			disabled = false;
            			break;
            		}
            	}
            }

            if (disabled == true) {
                if (ths.sharePreSet(cet)==false) {
                    //disabling not due to choice places
                    if (ENABLE_PRINT) {
                        prDbg(9, "Verification failed: LPN transition "
                                + cet.getLabel() + " disabled by " + ths.getFullLabel());
                    }
                    return cet;
                }
            }
        }

        return null;
    }

//	public boolean isInput() {
//        for (VarExpr ve : getAssignments()) {
//            for (String s : lpn.getInputs()) {
//                if (s.equals(ve.getVar())) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
    
    public boolean isVisible() {
    	return visible;
    }
    
    final public boolean isEnabled(final State curState) {
        counts[5]++;
        if (curState == null) {
            throw new NullPointerException();
        }

		if (this.preSet != null && this.preSet.length > 0) {
			for (int pp : this.preSet) {
				int[] curMarking = curState.getMarking();
				boolean included = false;
				
				for (int i = 0; i < curMarking.length; i++) {
					if (curMarking[i] == pp) {
						included = true;
						break;
					}
				}
				
				if (included == false)
					return false;
            }
		}
        
        int[] curVector = curState.getVector();
        if (curVector.length > 0) {
            if(getEnablingGuard().evaluate(curState) == 0)
                return false;
        }

        return true;
    }
    
    // Check if this LPN transition satisfies the timing constraint: its lower bound of delay
    // is larger than the maximal value of its timed in curZone.
    public boolean isTimedEnabled(final State curState, final Zone curZone) {
    	if(this.isEnabled(curState) == false)
    		return false;
    	
        if (this.delayLB == 0)
            return true;
       
        // Checking timing against the zone
//        Integer timerMaxVal = 0;//curZone.get(0, this.getID());
//        return timerMaxVal == null ? true : this.delayLB <= timerMaxVal;
        return false;
    }

    public boolean containsAll(Integer[] set1, int[] set2) {
        int curMarkingSize = set1.length;
        for (int prep : set2) {
            boolean existed = false;
            for (int index = 0; index < curMarkingSize; index++) {
                if (set1[index] == prep) {
                    existed = true;
                    break;
                }
            }
            if (existed == false) {
                return false;
            }
        }
        return true;
    }
 
    static final public boolean notContainsAll(final int[] set1,final int[] set2) {
     //return false if set2 is a subset of set1
     int index;
         for (int i = 0; i < set2.length; i++) {
            for ( index = 0; index < set1.length; index++) {
                if (set1[index] == set2[i]) {
                    break;
                }
            }
            if (index >= set1.length) {
                return true;
            }
        }
        return false;
    }
    
    static final public boolean containsAll(int[] set1, int[] set2) {
      int index;  for (int i = 0; i < set2.length; i++) {
            boolean exists = false;
            for ( index = 0; index < set1.length; index++) {
                if (set1[index] == set2[i]) {
                    exists = true;
                    break;
                }
            }
            if (exists == false) {
                return false;
            }
        }
        return true;
    }



    final public boolean sharePreSet(final LPNTran other) {
        for (int i : preSet) {
            for (int j : other.preSet) {
                if (i == j) {
                    return true;
                }
            }
        }
        counts[9]++;
        return false;
    }

    @Override
    public final LPNTran clone(){
    	return new LPNTran(this);
    }
    
    @Override
    final public String toString() {
        String ret = ""
                + label + ":{\n\t{"
                + toList(preSet).toString().replace("[", "").replace("]", "") + "};\n\t{"
                + toList(postSet).toString().replace("[", "").replace("]", "") + "};\n\t"
                + getEnablingGuard() + ";\n\t"
                + getAssignments() + ";\n\t["
                + (delayLB == Integer.MAX_VALUE ? "inf" : delayLB) + ", "
                + (delayUB == Integer.MAX_VALUE ? "inf" : delayUB) + "];\n}\n";
        return ret;
    }
    
    public LPNTran copy(HashMap<String, VarNode> variables){
    	return new LPNTran(this.label, this.index, this.preSet, this.postSet, this.enablingGuard.copy(variables), 
    			this.assignments.copy(variables), this.delayLB, this.delayUB, this.local);
    }

 
    public void setLabel(String lbl) {
    	this.label = lbl;
    }
    
    public String getLabel() {
        return this.label;
    }

    public String getFullLabel() {
    	return this.lpn.getLabel() + ":" + this.label;
    }

    /**
     * @return LPN transition's preset.
     */
    public int[] getPreSet() {
        return this.preSet;
    }

    /**
     * @param preSet the PreSet to set
     */
    public void setPreSet(Collection<Integer> PreSet) {
        this.preSet = toArray(PreSet);
    }

    /**
     * @return the PostSet
     */
    public Collection<Integer> getPostSet() {
        return toList(postSet);
    }

    /**
     * @param postSet the PostSet to set
     */
    public void setPostSet(Collection<Integer> PostSet) {
        this.postSet = toArray(PostSet);
    }

    /**
     * @return the EnablingGuard
     */
    public Expression getEnablingGuard() {
        return enablingGuard;
    }

    /**
     * @param enablingGuard the EnablingGuard to set
     */
    public void setEnablingGuard(Expression EnablingGuard) {
        this.enablingGuard = EnablingGuard;
    }

    /**
     * @param assignments the Assignments to set
     */
    public void setAssignments(VarExprList Assignments) {
        this.assignments = Assignments;
    }

    /**
     * @return the DelayLB
     */
    public int getDelayLB() {
        return delayLB;
    }

    /**
     * @param DelayLB the DelayLB to set
     */
    public void setDelayLB(int DelayLB) {
        this.delayLB = DelayLB;
    }

    /**
     * @return the DelayUB
     */
    public int getDelayUB() {
        return delayUB;
    }

    /**
     * @param DelayUB the DelayUB to set
     */
    public void setDelayUB(int DelayUB) {
        this.delayUB = DelayUB;
    }

    public String digest() {
        String s = "+T." + getLabel();
        return s;
    }

    public static void prDbg(int level, Object o) {
        if (level >= PRINT_LEVEL) {
            out.println(o);
        }
    }
    
    /**
     * Fire a transition on a state array, find new local states, and return the new state array formed by the new local states.
     * @param curLpnArray
     * @param curStateArray
     * @param curLpnIndex
     * @return
     */
    public State[] fire(final LPN[] curLpnArray, final int[] curStateIdxArray, int curLpnIndex) {
    	State[] stateArray = new State[curLpnArray.length];
    	for(int i = 0; i < curLpnArray.length; i++)
    		stateArray[i] = curLpnArray[i].getState(curStateIdxArray[i]);

    	return this.fire(curLpnArray, stateArray, curLpnIndex);
    }

    public State[] fire(final LPN[] curLpnArray, final State[] curStateArray, int curLpnIndex) {
    	int stateLength = curLpnArray.length;
    	State[] nextStateArray = curStateArray.clone();
    	
    	State curState = curStateArray[curLpnIndex];
    	State nextState = this.fire(curState);   
    	
        for(Expression e : assertions){
        	if(e.evaluate(nextState) == 0){
        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
        		System.exit(1);
			}
		}

        if(this.local()==true) {
    		nextStateArray[curLpnIndex] = curLpnArray[curLpnIndex].addState(nextState);
        	return nextStateArray;
		}

        int[] nextVector = nextState.getVector();
        HashMap<String, Integer> vvSet = new HashMap<String, Integer>();
        for (VarExpr s : this.getAssignments()) {
            int newValue = nextVector[s.getVar().getIndex(curState)];
            vvSet.put(s.getVar().getName(), newValue);   
        }
        
        // Update other local states with the new values generated for the shared variables.
        for(int i = 0; i < stateLength; i++) {
        	if(i == curLpnIndex)
        		nextStateArray[i] = nextState;
        	else {
        		boolean toUpdate = false;
        		for(VarNode var : this.assignedVarSet) {
        			if(curLpnArray[i].getInputs().contains(var.getName()) == true) {
        				toUpdate = true;
        				break;
        			}
        		}
        		
        		if(toUpdate == true) {
        			State newOther = curStateArray[i].update(vvSet, curLpnArray[i].getVarIndexMap());
        			if(newOther == null) 
        				nextStateArray[i] = curStateArray[i];
        			else
        				nextStateArray[i] = newOther;
        		}
        		else
        			nextStateArray[i] = curStateArray[i];
        	}
        	
        	// Get the reference to the nextLocalState that is already in localStateSets.
        	// Otherwise, add the new nextLocalState into the set.
        	if (curStateArray[i] != nextStateArray[i]) {
        		nextStateArray[i] = curLpnArray[i].addState(nextStateArray[i]);
        	}
        }

        return nextStateArray;
    }
    
    public State fire(final State curState) {  		
    	if(this.nextStateMap.containsKey(curState) == true)
    		return (State)this.nextStateMap.get(curState);
	
    	// Marking update
        int[] curOldMarking = curState.getMarking();
        int[] curNewMarking = null;
        if(preSet.length==0 && postSet.length==0)
        	curNewMarking = curOldMarking;
		else {
			curNewMarking = new int[curOldMarking.length - preSet.length + postSet.length];
			int index = 0;
			for (int i : curOldMarking) {
				boolean existed = false;
				for (int prep : preSet) {
					if (i == prep) {
						existed = true;
						break;
					}
				}
				if (existed == false) {
					curNewMarking[index] = i;
					index++;
				}
			}
			for (int postp : postSet) {
				curNewMarking[index] = postp;
				index++;
			}
        }

        //  State vector update
        int[] newVectorArray = curState.getVector().clone();
        for (VarExpr s : getAssignments()) {
            int newValue = (int) s.getExpr().evaluate(curState);
            newVectorArray[s.getVar().getIndex(curState)] = newValue;
        }
        
        State newState = this.lpn.addState(new State(this.lpn, curNewMarking, newVectorArray));

		for(Expression e : assertions){
        	if(e.evaluate(newState) == 0){
        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
        		System.exit(1);
        	}
        }
		
		this.nextStateMap.put(curState, newState);
		return newState;
    }
    
    public int numConstraints(){
    	return this.constraintSet.size();
    }
    
    public Set<Entry<Object, Object>> getNextStateMapEntrySet(){
    	return this.nextStateMap.entrySet();
    }
    
    public Set<Constraint> getConstraintSet(){
    	return this.constraintSet;
    }
    
    /**
	 * Adds constraint into Set<Constraint> constraintSet.
	 * @param c - constraint to add
	 * @return True if added, otherwise false.
	 */
    public boolean constrainSetContains(Constraint c){
    	return this.constraintSet.contains(c);
    }
    
    /**
	 * Adds constraint into Set<Constraint> constraintSet.
	 * @param c - constraint to add
	 * @return True if added, otherwise false.
	 */
    public boolean addConstraint(Constraint c){
    	return this.constraintSet.add(c);
    }
    
    /**
	 * Find associated state.
	 * @param s - key
	 * @return The state associated with the key s, otherwise null.
	 */
    public State getNextState(State s){
    	return (State) this.nextStateMap.get(s);
    }
    
    /**
	 * Adds a pair of states into HashMap<Object, Object> nextStateMap
	 * @param s1 - key
	 * @param s2 - value
	 * @return The state associated with the key s1, otherwise null.
	 */
    public State addStateTran(State s1, State s2){
    	State s = (State) this.nextStateMap.get(s1);
    	if(s == null){
    		this.nextStateMap.put(s1, s2);
    	}
    	
    	return s;
    }
    
    /**
	 * Adds a pair of states into HashMap<Object, Object> nextStateMap.  Synchronized version of addStateTran().
	 * @param s1 - key
	 * @param s2 - value
	 * @return The state associated with the key s1, otherwise null.
	 */
    public synchronized State synchronizedAddStateTran(State s1, State s2){
    	State s = (State) this.nextStateMap.get(s1);
    	if(s == null){
    		this.nextStateMap.put(s1, s2);
    	}
    	
    	return s;
    }
    
    public List<LPN> getLpnList(){
    	return this.lpnList;
    }
    
    public void addLpnList(LPN lpn){
    	this.lpnList.add(lpn);
    }
    
    public State constrFire(final State curState) {
    	// Marking update
        int[] curOldMarking = curState.getMarking();
        int[] curNewMarking = null;
        if(this.preSet.length==0 && this.postSet.length==0){
        	curNewMarking = curOldMarking;
        }
		else {
			curNewMarking = new int[curOldMarking.length - this.preSet.length + this.postSet.length];
			int index = 0;			
			for (int i : curOldMarking) {
				boolean existed = false;
				for (int prep : this.preSet) {
					if (i == prep) {
						existed = true;
						break;
					}
					else if(prep > i){
						break;
					}
				}
				
				if (existed == false) {
					curNewMarking[index] = i;
					index++;
				}
			}
			
			for (int postp : postSet) {
				curNewMarking[index] = postp;
				index++;
			}
        }

        //  State vector update
        int[] oldVector = curState.getVector();
        int size = oldVector.length;
        int[] newVectorArray = new int[size];
        System.arraycopy(oldVector, 0, newVectorArray, 0, size);
        
        for (VarExpr s : getAssignments()) {
            int newValue = s.getExpr().evaluate(curState);
            newVectorArray[s.getVar().getIndex(curState)] = newValue;
        }
        
        State newState = new State(this.lpn, curNewMarking, newVectorArray);
        
		for(Expression e : assertions){
        	if(e.evaluate(newState) == 0){
        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
        		System.exit(1);
        	}
        }
		
		return newState;
    }
    
    /**
     * CHeck if enabling of 'other' depends on 'this'.
     * @param other
     * @return boolean
     */
    public boolean dependent(LPNTran other) {
        // Check if 'this' and 'other' have common places in their presets
        // Check if the preset of 'other' includes places in the postset of 'this'.
        // Check if the Boolean guard of 'other' depends on the variables defined by 'this'.
        // If one of the three checks is true, return true; otherwise, return false.
    	HashSet<String> thisVarSet = new HashSet<String>();
        for (VarExpr assgn : this.assignments) {
            thisVarSet.add(assgn.getVar().getName());
        }
                
        //set of variables that the Boolean guard of 'other' depends on.
        for (VarNode otherVar : other.getEnablingGuard().getVariables()) {
            //System.out.println(otherVar);
            if (thisVarSet.contains(otherVar.getName())) {
                return true;
            }
        }
    	
        // if this and other transitions are in different LPNs, they are independent.
        if(this.lpn.getLabel() != other.lpn.getLabel())
        	return false;
        	
    	for (int i : other.preSet) {
            for (int j : this.preSet) {
                if (i == j) {
                    return true;
                }
            }
            for (int j : this.postSet) {
                if (i == j) {
                    return true;
                }
            }
        }
        
        

        return false;
    }

    /**
     * @return the lpn
     */
    public LPN getLpn() {
        return lpn;
    }

    /**
     * @param lpn the lpn to set
     */
    public void setLpn(LPN lpn) {
        this.lpn = lpn;
//    	ID=lpn.ID;
    }

    static public void printUsageStats() {
        System.out.printf("%-20s %11s\n", "LPNTran", counts[0]);
//         System.out.printf("\t%-20s %11s\n",   "clone",  counts[1]);
        System.out.printf("\t%-20s %11s\n", "getEnabledLpnTrans", counts[2]);
//         System.out.printf("\t%-20s %11s\n",   "getMarkedLpnTrans",  counts[3]);
//        System. out.printf("\t%-20s %11s\n",   "equals",  counts[4]);
        System.out.printf("\t%-20s %11s\n", "isEnabled", counts[5]);
        System.out.printf("\t%-20s %11s\n", "isTimeEnabled", counts[6]);
//       System.  out.printf("\t%-20s %11s\n",   "isMarked",  counts[7]);
        System.out.printf("\t%-20s %11s\n", "fire", counts[8]);
//      System.   out.printf("\t%-20s %11s\n",   "sharePreSet",  counts[9]);
        System.out.printf("\t%-20s %11s\n", "disablingError", counts[10]);
//       System.  out.println("getEnTrans: "+stats.toString());
    }

    public void print() {
        System.out.println(lpn.getLabel() + ": transition: " + getLabel() + ": ");

        System.out.print("preset: [");
        for(int i = 0; i < preSet.length; i++)
        	System.out.print(preSet[i]+ ",");
        System.out.println("]");
        
        System.out.print("postset: [");
        for(int i = 0; i < postSet.length; i++)
        	System.out.print(postSet[i]+ ",");
        System.out.println("]");

        //System.out.print("\t Guard: ");
        //enablingGuard.print();
    }
    
    /**
     * @return the assignments
     */
    public VarExprList getAssignments() {
        return assignments;
    }

    
    /**
     * Inserts a Boolean expression into LPNTran's assertion list.
     * @param booleanExpr - Boolean expression
     * @return void
     */
    public void addAssertion(Expression booleanExpr){
    	assertions.add(booleanExpr);
    }
    
    /**
     * Inserts a collection of Boolean expressions into LPNTran's assertion list.
     * @param booleanExprs - collection of Boolean expressions
     * @return void
     */
    public void addAllAssertions(Collection<Expression> booleanExprs){
    	assertions.addAll(booleanExprs);
    }
    
    /**
     * Returns true if LPNTran only modifies non-output variables.
     * @return Boolean
     */
    public boolean local(){
    	return this.local;
    }
    
    public void setLocalFlag(boolean flag){
    	this.local = flag;
    }
    
    public String getAssignmentString(){
        String ret="";
        for(VarExpr ve:getAssignments()){

            ret+=ve.toString()+"; ";
        }
        return ret;
    }
    
    public String getLabelString(){
    	return lpn.getLabel() + ":" + getLabel();
    }

    public void setIndex(int idx) {
    	this.index = idx;
    }
    
    public int getIndex() {
    	return this.index;
    }
    
    /*
     * Needed in Moore's code. Should be removed later.
     */
    public int getID() {
    	return 0;
    }
}
