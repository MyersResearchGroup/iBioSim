/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.lema.verification.platu.platuLpn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import edu.utah.ece.async.lema.verification.platu.TimingAnalysis.Zone1;
import edu.utah.ece.async.lema.verification.platu.expression.Expression;
import edu.utah.ece.async.lema.verification.platu.expression.VarNode;
import edu.utah.ece.async.lema.verification.platu.platuLpn.PlatuLPN;
import edu.utah.ece.async.lema.verification.platu.stategraph.State;
import edu.utah.ece.async.lema.verification.platu.stategraph.StateGraph;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class LPNTran {

    public static final int[] toArray(Collection<Integer> set) {
        int[] arr = new int[set.size()];
        int idx = 0;
        for (int i : set) {
            arr[idx++] = i;
        }
        return arr;
    }
    
    /* LPN where this transition is defined. */
    private PlatuLPN lpn;
    
    /* Transition labels in the same LPN must be unique. */
    private String label;   
    
    /* Index of this transition in the this.lpn. */
    private int index;
    
    private int[] preSet;
    private int[] postSet;
    
    private Expression enablingGuard; // Enabling condition
    
    private VarExprList assignments = new VarExprList();
    
    private int delayLB;  // Lower bound of the delay
    private int delayUB;  // Upper bound of the delay.
    
    /* Variables on LHS of assignments of this transition. */
    private HashSet<VarNode> assignedVarSet;
    
    /* Variables in the enablingGuard and in the expressions on RHS of assignments. */
    private HashSet<String> supportSet;
    
    /* True if all variables in assignedVarSet are in the set 'internals' of this.lpn. */
    private boolean local = true;
    
    /* A set of Boolean formulas that should hold after firing this transition. */
    private ArrayList<Expression> assertions = new ArrayList<Expression>(1);
    
    /* LPNs that share variables in the assignedVarSet of this transition. */
    private List<PlatuLPN> dstLpnList = new ArrayList<PlatuLPN>();
    
    /* true indicating this transition follows non-disabling semantics. */
    private boolean stickyFlag = true;
    
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
        assignedVarSet = new HashSet<VarNode>();
        //nextStateMap = new HashMap<Object, Object>();

        if (preSet == null || postSet == null || enablingGuard == null || assignments == null) {
            new NullPointerException().printStackTrace();
        }
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
        assignedVarSet = new HashSet<VarNode>();
        //nextStateMap = new HashMap<Object, Object>();

        if (preSet == null || postSet == null || enablingGuard == null || assignments == null) {
            new NullPointerException().printStackTrace();
        }
    }
    
    public void initialize(final PlatuLPN lpnModel) {
    	// Computer the variables on the left hand side of the assignments.
    	this.lpn = lpnModel;
    	this.supportSet = new HashSet<String>();
    	
		HashSet<VarNode> guardVarSet = this.enablingGuard.getVariables();
		for(VarNode var : guardVarSet){
			this.supportSet.add(var.getName());
		}
    	
    	// Determine the visibility of this tran.
    	for (VarExpr assign : this.assignments) {
    		assignedVarSet.add(assign.getVar());
    		
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

    // (done) Moved to StateGraph.
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
            		if (cet.label == net.label) {
            			disabled = false;
            			break;
            		}
            	}
            }

            if (disabled == true) 
                if (ths.sharePreSet(cet)==false)
                    return cet;
        }

        return null;
    }
    
    final public boolean isEnabled(final State curState) {
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
        
        int[] curVector = curState.getVariableVector();
        if (curVector.length > 0) {
            if(getEnablingGuard().evaluate(curVector) == 0)
                return false;
        }

        return true;
    }
    
    // Check if this LPN transition satisfies the timing constraint: its lower bound of delay
    // is larger than the maximal value of its timed in curZone.
	public boolean isTimedEnabled(final State curState, final Zone1 curZone) {
    	if(this.isEnabled(curState) == false)
    		return false;
    	
        if (this.delayLB == 0)
            return true;
       
        // Checking timing against the zone
//        Integer timerMaxVal = 0;//curZone.get(0, this.getID());
//        return timerMaxVal == null ? true : this.delayLB <= timerMaxVal;
        return false;
    }

    public static boolean containsAll(Integer[] set1, int[] set2) {
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
        return false;
    }
    
    @Override
    final public String toString() {
    	String ret = lpn.getLabel() + ": transition: " + getLabel() + ": \n" 
    				+ "preset: [\n";
    	
        for(int i = 0; i < preSet.length; i++)
        	ret += preSet[i]+ ",";
        ret += "]\n" 
        	+ "postset: [";
        
        for(int i = 0; i < postSet.length; i++)
        	ret += postSet[i]+ ",";
        ret += "]\n";
       
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
    public HashSet<Integer> getPostSet() {
       	HashSet<Integer> ret = new HashSet<Integer>();
       	for(int i : this.postSet)
       		ret.add(i);
       	return ret;
    }

//    /**
//     * @param postSet the PostSet to set
//     */
//    public void setPostSet(Collection<Integer> PostSet) {
//        this.postSet = toArray(PostSet);
//    }

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
    
    /**
     * Fire a transition on a state array, find new local states, and return the new state array formed by the new local states.
     * @param curLpnArray
     * @param curStateArray
     * @param curLpnIndex
     * @return
     */
    public State[] fire(final StateGraph[] curSgArray, final int[] curStateIdxArray) {
    	State[] stateArray = new State[curSgArray.length];
    	for(int i = 0; i < curSgArray.length; i++)
    		stateArray[i] = curSgArray[i].getState(curStateIdxArray[i]);

    	return this.fire(curSgArray, stateArray);
    }
    
    
    public State[] fire(final StateGraph[] curSgArray, final State[] curStateArray) {
    	int thisLpnIndex = this.getLpn().getIndex(); 
    	State[] nextStateArray = curStateArray.clone();
    	
    	State curState = curStateArray[thisLpnIndex];
    	State nextState = this.fire(curSgArray[thisLpnIndex], curState);   
    	
    	int[] nextVector = nextState.getVariableVector();
    	int[] curVector = curState.getVariableVector();
    	
        for(Expression e : assertions){
        	if(e.evaluate(nextVector) == 0){
        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
        		System.exit(1);
			}
		}

        if(this.local()==true) {
    		nextStateArray[thisLpnIndex] = curSgArray[thisLpnIndex].addState(nextState);
        	return nextStateArray;
		}

        HashMap<String, Integer> vvSet = new HashMap<String, Integer>();
        for (VarExpr s : this.getAssignments()) {
            int newValue = nextVector[s.getVar().getIndex(curVector)];
            vvSet.put(s.getVar().getName(), newValue);   
        }
        
        
        // Update other local states with the new values generated for the shared variables.
		nextStateArray[this.lpn.getIndex()] = nextState;
//        for(LPN curLpn : this.dstLpnList) {
//        	int curIdx = curLpn.getIndex();
        	// (temp) Hack here.  Probably LPNTran.java will go away.
 //   		State newState = null; //curSgArray[curIdx].getNextState(curStateArray[curIdx], this);
//    		if(newState != null) {
        		//nextStateArray[curIdx] = newState;
//   		} else {
        		// (done) may not need to be updated, but could change to use our var index map
        		/*
        		State newOther = curStateArray[curIdx].update(vvSet, curSgArray[curIdx].getLpn().getVarIndexMap());
        		if (newOther == null)
        			nextStateArray[curIdx] = curStateArray[curIdx];
        		else {
        			State cachedOther = curSgArray[curIdx].addState(newOther);
					//nextStateArray[curIdx] = newOther;
            		nextStateArray[curIdx] = cachedOther;
            		curSgArray[curIdx].addStateTran(curStateArray[curIdx], this, cachedOther);
        		}
        		*/
 //       	}
 //       }
        
        return nextStateArray;
    }
    
    // (done) Moved fire to StateGraph    
	public State fire(final StateGraph thisSg, final State curState) {  		
    	// Search for and return cached next state first. 
//    	if(this.nextStateMap.containsKey(curState) == true)
//    		return (State)this.nextStateMap.get(curState);
    	// (temp) Hack here.  Probably LPNTran.java will go away.
    	//State nextState = null; // thisSg.getNextState(curState, this);
    	//if(nextState != null)
    	//	return nextState;
    	
    	// If no cached next state exists, do regular firing. 
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
        int[] newVectorArray = curState.getVariableVector().clone();
        int[] curVector = curState.getVariableVector();
        
        for (VarExpr s : getAssignments()) {
            int newValue = s.getExpr().evaluate(curVector);
            newVectorArray[s.getVar().getIndex(curVector)] = newValue;
        }
                
        State newState = null; //= thisSg.addState(new State(this.lpn, curNewMarking, newVectorArray));
        
        /*
        int[] newVector = newState.getVariableVector();
		for(Expression e : assertions){
        	if(e.evaluate(newVector) == 0){
        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
        		System.exit(1);
        	}
        }
        */
		// (temp) Hack here.  Probably LPNTran.java will go away.
		//thisSg.addStateTran(curState, this, newState);
		return newState;
    }
	
    
    public List<PlatuLPN> getDstLpnList(){
    	return this.dstLpnList;
    }
    
    public void addDstLpn(PlatuLPN lpn){
    	this.dstLpnList.add(lpn);
    }
    
    // Moved to StateGraph.java  
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
        int[] oldVector = curState.getVariableVector();
        int size = oldVector.length;
        int[] newVectorArray = new int[size];
        System.arraycopy(oldVector, 0, newVectorArray, 0, size);
        
        int[] curVector = curState.getVariableVector();
        for (VarExpr s : getAssignments()) {
            int newValue = s.getExpr().evaluate(curVector);
            newVectorArray[s.getVar().getIndex(curVector)] = newValue;
        }
        
        //(temp) Hack here. Probably LPNTran.java will go away.
        State newState = null; //new State(this.lpn, curNewMarking, newVectorArray);
        
        //int[] newVector = newState.getVariableVector();
        /* 
		for(Expression e : assertions){
        	if(e.evaluate(newVector) == 0){
        		System.err.println("Assertion " + e.toString() + " failed in LPN transition " + this.lpn.getLabel() + ":" + this.label);
        		System.exit(1);
        	}
        }
		*/
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
     * @return LPN object containing this LPN transition.
     */
    public PlatuLPN getLpn() {
        return lpn;
    }

    /**
     * @param lpn - Associated LPN containing this LPN transition.
     */
    public void setLpn(PlatuLPN lpn) {
        this.lpn = lpn;
    }

//    static public void printUsageStats() {
//        System.out.printf("%-20s %11s\n", "LPNTran", counts[0]);
////         System.out.printf("\t%-20s %11s\n",   "clone",  counts[1]);
//        System.out.printf("\t%-20s %11s\n", "getEnabledLpnTrans", counts[2]);
////         System.out.printf("\t%-20s %11s\n",   "getMarkedLpnTrans",  counts[3]);
////        System. out.printf("\t%-20s %11s\n",   "equals",  counts[4]);
//        System.out.printf("\t%-20s %11s\n", "isEnabled", counts[5]);
//        System.out.printf("\t%-20s %11s\n", "isTimeEnabled", counts[6]);
////       System.  out.printf("\t%-20s %11s\n",   "isMarked",  counts[7]);
//        System.out.printf("\t%-20s %11s\n", "fire", counts[8]);
////      System.   out.printf("\t%-20s %11s\n",   "sharePreSet",  counts[9]);
//        System.out.printf("\t%-20s %11s\n", "disablingError", counts[10]);
////       System.  out.println("getEnTrans: "+stats.toString());
//    }

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
    public static int getID() {
    	return 0;
    }
    
    public void setSticky(){
    	this.stickyFlag = true;
    }

    public boolean sticky(){
    	return this.stickyFlag;
    }
}
