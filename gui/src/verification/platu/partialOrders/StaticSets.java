package verification.platu.partialOrders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import lpn.parser.ExprTree;
import lpn.parser.LhpnFile;
import lpn.parser.Place;
import lpn.parser.Transition;

public class StaticSets {
	private Transition curTran;
	private HashMap<Integer, Transition[]> allTransitions;
	private HashSet<LpnTransitionPair> disableSet; 
	private HashSet<LpnTransitionPair> disableByStealingToken;
	private HashSet<LpnTransitionPair> enableSet;
	private HashSet<LpnTransitionPair> disableByFailingEnableCond;
	private HashSet<LpnTransitionPair> modifyAssignment;
	
	public StaticSets(Transition curTran, HashMap<Integer,Transition[]> allTransitions) {
		this.curTran = curTran;
		this.allTransitions = allTransitions;
		disableSet = new HashSet<LpnTransitionPair>();
		disableByStealingToken = new HashSet<LpnTransitionPair>();
		disableByFailingEnableCond = new HashSet<LpnTransitionPair>();
		enableSet = new HashSet<LpnTransitionPair>();
		modifyAssignment = new HashSet<LpnTransitionPair>();
	}
		
	/**
	 * Build a set of transitions that can be disabled by curTran.
	 * @param curLpnIndex 
	 */
	public void buildDisableSet(int curLpnIndex) {
		// Test if curTran can disable other transitions by stealing their tokens.
		if (curTran.hasConflictSet()) {
//			if (curTran.getPreset().length==1 && curTran.getPostset().length==1 && curTran.getPreset()[0] == curTran.getPostset()[0]) {				
//			}
			if (!tranFormsSelfLoop()) {
				Set<Integer> conflictSet = curTran.getConflictSetTransIndices();
				conflictSet.remove(curTran.getIndex());
				for (Integer i : conflictSet) {
					LpnTransitionPair lpnTranPair = new LpnTransitionPair(curLpnIndex, i);
					disableByStealingToken.add(lpnTranPair);
				}
			}
		}
		// Test if curTran can disable other transitions by firing its assignments
		for (Integer lpnIndex : allTransitions.keySet()) {
			Transition[] allTransInOneLpn = allTransitions.get(lpnIndex);
			for (int i = 0; i < allTransInOneLpn.length; i++) {
				if (curTran.equals(allTransInOneLpn[i]))
					continue;
				Transition anotherTran = allTransInOneLpn[i];
				ExprTree anotherTranEnablingTree = anotherTran.getEnablingTree();
				if (anotherTranEnablingTree != null
						&& (anotherTranEnablingTree.getChange(curTran.getAssignments())=='F'
						 || anotherTranEnablingTree.getChange(curTran.getAssignments())=='f'
						 || anotherTranEnablingTree.getChange(curTran.getAssignments())=='X')) {
					disableByFailingEnableCond.add(new LpnTransitionPair(lpnIndex, anotherTran.getIndex()));
				}
			}
			disableSet.addAll(disableByStealingToken);
			disableSet.addAll(disableByFailingEnableCond);
			buildModifyAssignSet();
			disableSet.addAll(modifyAssignment);
//			printIntegerSet(disableByStealingToken, "disableByStealingToken");
//			printIntegerSet(disableByFailingEnableCond, "disableByFailingEnableCond");

		}
	}
	
	private boolean tranFormsSelfLoop() {
		boolean isSelfLoop = false;
		Place[] curPreset = curTran.getPreset();
		Place[] curPostset = curTran.getPostset();
		for (Place preset : curPreset) {
			for (Place postset : curPostset) {
				if (preset == postset) {
					isSelfLoop = true;
					break;
				}	
			}
			if (isSelfLoop)
				break;
		}
		return isSelfLoop;
	}

	/**
	 * Construct a set of transitions that can make the enabling condition of curTran true, by firing their assignments.
	 * @param lpnIndex 
	 */
	public void buildEnableSet() {
		for (Integer lpnIndex : allTransitions.keySet()) {
			Transition[] allTransInOneLpn = allTransitions.get(lpnIndex);
			for (int i = 0; i < allTransInOneLpn.length; i++) {
				if (curTran.equals(allTransInOneLpn[i]))
					continue;
				Transition anotherTran = allTransInOneLpn[i];
				ExprTree curTranEnablingTree = curTran.getEnablingTree();
				if (curTranEnablingTree != null
						&& (curTranEnablingTree.getChange(anotherTran.getAssignments())=='T'
						|| curTranEnablingTree.getChange(anotherTran.getAssignments())=='t'
						|| curTranEnablingTree.getChange(anotherTran.getAssignments())=='X')) {
					enableSet.add(new LpnTransitionPair(lpnIndex, anotherTran.getIndex()));
				}			
			}
		}
	}
	
	public void buildModifyAssignSet() {
		// modifyAssignment contains transitions (T) that satisfy either (1) or (2): for every t in T, 
		// (1) intersection(VA(curTran), supportA(t)) != empty
		// (2) intersection(VA(t), supportA(curTran)) != empty
		// (3) intersection(VA(t), VA(curTran) != empty
		// VA(t) : set of variables being assigned in transition t.
		// supportA(t): set of variables appearing in the expressions assigned to the variables of t (r.h.s of the assignment).
		for (Integer lpnIndex : allTransitions.keySet()) {
			Transition[] allTransInOneLpn = allTransitions.get(lpnIndex);
			for (int i = 0; i < allTransInOneLpn.length; i++) {
				if (curTran.equals(allTransInOneLpn[i]))
					continue;
				Transition anotherTran = allTransInOneLpn[i];
				for (String v : curTran.getAssignTrees().keySet()) {
					for (ExprTree anotherTranAssignTree : anotherTran.getAssignTrees().values()) {
						if (anotherTranAssignTree != null && anotherTranAssignTree.containsVar(v)) {
							modifyAssignment.add(new LpnTransitionPair(lpnIndex, anotherTran.getIndex()));
						}					
					}
				}
				for (ExprTree exprTree : curTran.getAssignTrees().values()) {
					for (String v : anotherTran.getAssignTrees().keySet()) {
						if (exprTree != null && exprTree.containsVar(v)) {
							modifyAssignment.add(new LpnTransitionPair(lpnIndex, anotherTran.getIndex()));
						}
					}
				}
				for (String v1 : curTran.getAssignTrees().keySet()) {
					for (String v2 : anotherTran.getAssignTrees().keySet()) {
						if (v1.equals(v2)) {
							modifyAssignment.add(new LpnTransitionPair(lpnIndex, anotherTran.getIndex()));
						}					
					}
				}
			}
		}
	}
 
	public HashSet<LpnTransitionPair> getModifyAssignSet() {
		return modifyAssignment;
	}
	
	public HashSet<LpnTransitionPair> getDisabled() {
		return disableSet;
	}

	public HashSet<LpnTransitionPair> getDisableByStealingToken() {
		return disableByStealingToken;
	}

	public HashSet<LpnTransitionPair> getEnable() {
		return enableSet;
	}
	
	public Transition getTran() {
		return curTran;
	}
//	private void printIntegerSet(HashSet<Integer> integerSet, String setName) {
//		if (!setName.isEmpty())
//			System.out.print(setName + ": ");
//		if (integerSet == null) {
//			System.out.println("null");
//		}
//		else if (integerSet.isEmpty()) {
//			System.out.println("empty");
//		}
//		else {
//			for (Iterator<Integer> curTranDisableIter = integerSet.iterator(); curTranDisableIter.hasNext();) {
//				Integer tranInDisable = curTranDisableIter.next();
//				System.out.print(lpn.getAllTransitions()[tranInDisable] + " ");
//			}
//			System.out.print("\n");
//		}				
//	}
 }
