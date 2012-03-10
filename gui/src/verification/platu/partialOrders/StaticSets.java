package verification.platu.partialOrders;

import java.util.HashSet;
import java.util.Set;

import lpn.parser.ExprTree;
import lpn.parser.LhpnFile;
import lpn.parser.Transition;

public class StaticSets {
	private LhpnFile lpn;
	private Transition curTran;
	private Transition[] allTransitions;
	private HashSet<Integer> disableSet; 
	private HashSet<Integer> disableByStealingToken;
	private HashSet<Integer> enableSet;
	private HashSet<Integer> disableByFailingEnableCond;
	private HashSet<Integer> modifyAssignment;
	
	public StaticSets(LhpnFile lpn, Transition curTran, Transition[] allTransitions) {
		this.lpn = lpn;
		this.curTran = curTran;
		this.allTransitions = allTransitions;
		disableSet = new HashSet<Integer>();
		disableByStealingToken = new HashSet<Integer>();
		disableByFailingEnableCond = new HashSet<Integer>();
		enableSet = new HashSet<Integer>();
		modifyAssignment = new HashSet<Integer>();
	}
		
	/**
	 * Build a set of transitions that can be disabled by curTran.
	 */
	public void buildDisableSet() {
		// Test if curTran can disable other transitions by stealing their tokens.
		if (curTran.hasConflictSet()) {
			Set<Integer> conflictSet = curTran.getConflictSetTransIndices();
			conflictSet.remove(curTran);
			disableByStealingToken.addAll(conflictSet);
		}
		// Test if curTran can disable other transitions by firing its assignments
		for (int i = 0; i < allTransitions.length; i++) {
			if (curTran.equals(allTransitions[i]))
				continue;
			Transition anotherTran = allTransitions[i];
			ExprTree anotherTranEnablingTree = anotherTran.getEnablingTree();
			if (anotherTranEnablingTree != null
					&& anotherTranEnablingTree.getChange(curTran.getAssignments())=='F')
				disableByFailingEnableCond.add(anotherTran.getIndex());
		}
		disableSet.addAll(disableByStealingToken);
		disableSet.addAll(disableByFailingEnableCond);
	}
	
	/**
	 * Construct a set of transitions that can make the enabling condition of curTran true, by firing their assignments.
	 */
	public void buildEnableSet() {
		for (int i = 0; i < allTransitions.length; i++) {
			if (curTran.equals(allTransitions[i]))
				continue;
			Transition anotherTran = allTransitions[i];
			ExprTree curTranEnablingTree = curTran.getEnablingTree();
			if (curTranEnablingTree != null
					&& curTranEnablingTree.getChange(anotherTran.getAssignments())=='T')
				enableSet.add(anotherTran.getIndex());
		}
	}
	
	public void buildModifyAssignSet() {
		// modifyAssignment contains transitions (T) that satisfy either (1) or (2): for every t in T, 
		// (1) intersection(VA(curTran), supportA(t)) != empty
		// (2) intersection(VA(t), supportA(curTran)) != empty
		// VA(t) : set of variables being assigned in transition t.
		// supportA(t): set of variables appearing in the expressions assigned to the variables of t (r.h.s of the assignment). 
		for (int i = 0; i < allTransitions.length; i++) {
			if (curTran.equals(allTransitions[i]))
				continue;
			Transition anotherTran = allTransitions[i];
			for (String v : curTran.getAssignTrees().keySet()) {
				for (ExprTree anotherTranAssignTree : anotherTran.getAssignTrees().values()) {
					if (anotherTranAssignTree != null && anotherTranAssignTree.containsVar(v)) {
						modifyAssignment.add(anotherTran.getIndex());
					}					
				}
			}
			for (ExprTree exprTree : curTran.getAssignTrees().values()) {
				for (String v : anotherTran.getAssignTrees().keySet()) {
					if (exprTree != null && exprTree.containsVar(v)) {
						modifyAssignment.add(anotherTran.getIndex());
					}
				}
			}
		}
	}
 
	public HashSet<Integer> getModifyAssignSet() {
		return modifyAssignment;
	}
	
	public HashSet<Integer> getDisabled() {
		return disableSet;
	}

	public HashSet<Integer> getDisableByStealingToken() {
		return disableByStealingToken;
	}

	public HashSet<Integer> getEnable() {
		return enableSet;
	}
	
	public LhpnFile getLpn() {
		return lpn;
	}
	
	public Transition getTran() {
		return curTran;
	}
 }
