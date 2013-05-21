package verification.platu.partialOrders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lpn.parser.ExprTree;
import lpn.parser.Place;
import lpn.parser.Transition;
import lpn.parser.LpnDecomposition.LpnProcess;
import verification.platu.main.Options;

public class StaticSets {
	private Transition curTran;
	private HashSet<Transition> disableSet; 
	private HashSet<Transition> disableByStealingToken;
	//private ArrayList<ExprTree> conjunctsOfEnabling; 
	private ArrayList<HashSet<Transition>> otherTransSetCurTranEnablingTrue;
	private HashSet<Transition> curTranSetOtherTranEnablingFalse; // A set of transitions (with associated LPNs) whose enabling condition can become false due to executing curTran's assignments. 
	private HashSet<Transition> otherTransSetCurNonPersistentCurTranEnablingFalse; // A set of transitions (with associated LPNs) whose enabling condition can become false due to executing another transition's assignments.
	private HashSet<Transition> modifyAssignment;
	private String PORdebugFileName;
	private FileWriter PORdebugFileStream;
	private BufferedWriter PORdebugBufferedWriter;
	private HashMap<Transition, LpnProcess> allTransToLpnProcs; 
	
	public StaticSets(Transition curTran, HashMap<Transition, LpnProcess> allTransToLpnProcs) {
		this.curTran = curTran;
		this.allTransToLpnProcs = allTransToLpnProcs;
		disableSet = new HashSet<Transition>();
		disableByStealingToken = new HashSet<Transition>();
		curTranSetOtherTranEnablingFalse = new HashSet<Transition>();
		otherTransSetCurNonPersistentCurTranEnablingFalse = new HashSet<Transition>();
		otherTransSetCurTranEnablingTrue = new ArrayList<HashSet<Transition>>();
		modifyAssignment = new HashSet<Transition>();
		if (Options.getDebugMode()) {
			PORdebugFileName = Options.getPrjSgPath() + Options.getLogName() + "_" + Options.getPOR() + "_" 
					+ Options.getCycleClosingMthd() + "_" + Options.getCycleClosingAmpleMethd() + ".dbg";
			try {
				PORdebugFileStream = new FileWriter(PORdebugFileName, true);
			} catch (IOException e) {
				e.printStackTrace();
			}						
			PORdebugBufferedWriter = new BufferedWriter(PORdebugFileStream);
		}
	}
		
	/**
	 * Build a set of transitions that curTran can disable.
	 * @param curLpnIndex 
	 */
	public void buildCurTranDisableOtherTransSet() {
		// Test if curTran can disable other transitions by stealing their tokens.
		buildConflictSet();
		// Test if curTran can disable other transitions by executing its assignments. 
		// This excludes any transitions that are in the same process as curTran, and the process is a state machine.
		if (!curTran.getAssignments().isEmpty()) {
			ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
			if (allTransToLpnProcs.get(curTran).getStateMachineFlag()) { 
				transInOneStateMachine = allTransToLpnProcs.get(curTran).getProcessTransitions();
			}
			for (Transition anotherTran : allTransToLpnProcs.keySet()) {
				if (curTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
					continue;	
				ExprTree anotherTranEnablingTree = anotherTran.getEnablingTree();
				if (anotherTranEnablingTree != null
						&& (anotherTranEnablingTree.getChange(curTran.getAssignments())=='F'
						|| anotherTranEnablingTree.getChange(curTran.getAssignments())=='f'
						|| anotherTranEnablingTree.getChange(curTran.getAssignments())=='X')) {
					if (Options.getDebugMode()) {
						writeStringWithEndOfLineToPORDebugFile(curTran.getLpn().getLabel() + "(" + curTran.getLabel() + ") can disable " + anotherTran.getLpn().getLabel() + "(" + anotherTran.getLabel() + "). " 
								+ anotherTran.getLabel() + "'s enabling condition (" + anotherTranEnablingTree + "), may become false due to firing of " 
								+ curTran.getLabel() + ".");
						if (anotherTranEnablingTree.getChange(curTran.getAssignments())=='F') 
							writeStringWithEndOfLineToPORDebugFile("Reason is " + anotherTran.getLabel() + "_enablingTree.getChange(" + curTran.getLabel() + ".getAssignments()) = F.");
						if (anotherTranEnablingTree.getChange(curTran.getAssignments())=='f') 
							writeStringWithEndOfLineToPORDebugFile("Reason is " + anotherTran.getLabel() + "_enablingTree.getChange(" + curTran.getLabel() + ".getAssignments()) = f.");
						if (anotherTranEnablingTree.getChange(curTran.getAssignments())=='X') 
							writeStringWithEndOfLineToPORDebugFile("Reason is " + anotherTran.getLabel() + "_enablingTree.getChange(" + curTran.getLabel() + ".getAssignments()) = X.");
					}
					curTranSetOtherTranEnablingFalse.add(anotherTran);
				}
			}
		}
		disableSet.addAll(disableByStealingToken);
		disableSet.addAll(curTranSetOtherTranEnablingFalse);
	}
	
	/**
	 * Build a set of transitions that disable curTran.
	 * @param allTransitionsToLpnProcesses 
	 */
	public void buildOtherTransDisableCurTranSet() {
		// Test if other transition(s) can disable curTran by stealing their tokens.
		buildConflictSet();
		// Test if other transitions can disable curTran by executing their assignments.
		// This excludes any transitions that are in the same process as curTran, and the process is a state machine.
		if (!curTran.isPersistent()) { 
			// If curTran is persistent, when it becomes enabled, 
			// it can not be disabled if another transition sets its enabling condition to false.
			// Dependent calculation on curTran happens when curTran is enabled.
			ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
			if (allTransToLpnProcs.get(curTran).getStateMachineFlag()) { 
				transInOneStateMachine = allTransToLpnProcs.get(curTran).getProcessTransitions();
			}
			for (Transition anotherTran : allTransToLpnProcs.keySet()) {
				if (curTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
					continue;	
				if (!anotherTran.getAssignments().isEmpty()) {
					ExprTree curTranEnablingTree = curTran.getEnablingTree();
					if (curTranEnablingTree != null
							&& (curTranEnablingTree.getChange(anotherTran.getAssignments())=='F'
							|| curTranEnablingTree.getChange(anotherTran.getAssignments())=='f'
							|| curTranEnablingTree.getChange(anotherTran.getAssignments())=='X')) {
						if (Options.getDebugMode()) {
							writeStringWithEndOfLineToPORDebugFile(curTran.getLpn().getLabel() + "(" + curTran.getLabel() + ") can be disabled by " + anotherTran.getLpn().getLabel() + "(" + anotherTran.getLabel() + "). " 
									+ curTran.getLabel() + "'s enabling condition (" + curTranEnablingTree +  "), may become false due to firing of " 
									+ anotherTran.getLabel() + ".");
							if (curTranEnablingTree.getChange(anotherTran.getAssignments())=='F') 
								writeStringWithEndOfLineToPORDebugFile("Reason is " + curTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = F.");
							if (curTranEnablingTree.getChange(anotherTran.getAssignments())=='f') 
								writeStringWithEndOfLineToPORDebugFile("Reason is " + curTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = f.");
							if (curTranEnablingTree.getChange(anotherTran.getAssignments())=='X') 
								writeStringWithEndOfLineToPORDebugFile("Reason is " + curTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = X.");					
						}	
						otherTransSetCurNonPersistentCurTranEnablingFalse.add(anotherTran);
					}
				}
			}
		}
		disableSet.addAll(disableByStealingToken);
		disableSet.addAll(otherTransSetCurNonPersistentCurTranEnablingFalse);
		buildModifyAssignSet();
		disableSet.addAll(modifyAssignment);
	}
	
	public void buildConflictSet() {
		if (curTran.hasConflictSet()) {
			if (!tranFormsSelfLoop()) {
				if (allTransToLpnProcs.get(curTran).getStateMachineFlag()) {
					outerloop:for (Transition tranInConflict : curTran.getConflictSet()) {
						// In a state machine, if tranInConflict and curTran have disjoint enabling conditions, 
						// they are not dependent on each other. 
						// Temporary solution: compare each conjunct to decide if they are disjoint. 
						// Each conjunct in comparison only allow single literal or the negation of a literal. 
						// TODO: Need a (DPLL) SAT solver to decide if they are disjoint.
						if (!curTran.getConjunctsOfEnabling().isEmpty()) { 
							// Assume buildConjunctsOfEnabling(term) was called previously for curTran and tranInConflict.
							for (ExprTree conjunctOfCurTran : curTran.getConjunctsOfEnabling()) {	
								for (ExprTree conjunctOfTranInConflict : tranInConflict.getConjunctsOfEnabling()) {
									if (conjunctOfCurTran.getVars().size()==1 && conjunctOfTranInConflict.getVars().size()==1
											&& conjunctOfCurTran.getVars().equals(conjunctOfTranInConflict.getVars())) {
										ExprTree clause = new ExprTree(conjunctOfCurTran, conjunctOfTranInConflict, "&&", 'l');
										HashMap<String, String> assign = new HashMap<String, String>();
										assign.put(conjunctOfCurTran.getVars().get(0), "0");
										if (clause.evaluateExpr(assign) == 0.0) {
											assign.put(conjunctOfCurTran.getVars().get(0), "1");
											if (clause.evaluateExpr(assign) == 0.0) {
												continue outerloop;
											}
										}
									}
								}
							}
						}
						disableByStealingToken.add(tranInConflict);
					}

				}
				else {
					for (Transition tranInConflict : curTran.getConflictSet()) {
						disableByStealingToken.add(tranInConflict);
					}
				}
				if (Options.getDebugMode()) {
					writeStringWithEndOfLineToPORDebugFile(curTran.getLpn().getLabel() + "(" + curTran.getLabel() + ") can cause dependency relation by stealing tokens from these transitions:");
					for (Transition t : disableByStealingToken) {
						writeStringToPORDebugFile(t.getLpn().getLabel() + "(" + t.getLabel() + "), ");
					}
					writeStringWithEndOfLineToPORDebugFile("");
				}
			}
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

//	/**
//	 * Construct a set of transitions that can make the enabling condition of curTran true, by executing their assignments.
//	 */
//	public void buildEnableBySettingEnablingTrue() {
//		for (Integer lpnIndex : allTransitions.keySet()) {
//			Transition[] allTransInOneLpn = allTransitions.get(lpnIndex);
//			for (int i = 0; i < allTransInOneLpn.length; i++) {
//				if (curTran.equals(allTransInOneLpn[i]))
//					continue;
//				Transition anotherTran = allTransInOneLpn[i];
//				ExprTree curTranEnablingTree = curTran.getEnablingTree();
//				if (curTranEnablingTree != null
//						&& (curTranEnablingTree.getChange(anotherTran.getAssignments())=='T'
//						|| curTranEnablingTree.getChange(anotherTran.getAssignments())=='t'
//						|| curTranEnablingTree.getChange(anotherTran.getAssignments())=='X')) {
//					enableBySettingEnablingTrue.add(new Transition(lpnIndex, anotherTran.getIndex()));
//					if (Options.getDebugMode()) {
//						writeStringWithEndOfLineToPORDebugFile(curTran.getName() + " can be enabled by " + anotherTran.getName() + ". " 
//			                       + curTran.getName() + "'s enabling condition (" + curTranEnablingTree +  "), may become true due to firing of " 
//						           + anotherTran.getName() + ".");
//						if (curTranEnablingTree.getChange(anotherTran.getAssignments())=='T') 
//							writeStringWithEndOfLineToPORDebugFile("Reason is " + curTran.getName() + "_enablingTree.getChange(" + anotherTran.getName() + ".getAssignments()) = T.");
//						if (curTranEnablingTree.getChange(anotherTran.getAssignments())=='t') 
//							writeStringWithEndOfLineToPORDebugFile("Reason is " + curTran.getName() + "_enablingTree.getChange(" + anotherTran.getName() + ".getAssignments()) = t.");
//						if (curTranEnablingTree.getChange(anotherTran.getAssignments())=='X') 
//							writeStringWithEndOfLineToPORDebugFile("Reason is " + curTran.getName() + "_enablingTree.getChange(" + anotherTran.getName() + ".getAssignments()) = X.");
//					}
//				}			
//			}
//		}
//	}
			
//	public void buildModifyAssignSet() {
////		for every transition curTran in T, where T is the set of all transitions, we check t (t != curTran) in T, 
////		(1) intersection(VA(curTran), supportA(t)) != empty
////		(2) intersection(VA(t), supportA(curTran)) != empty
////		(3) intersection(VA(t), VA(curTran) != empty
////
////		VA(t0) : set of variables being assigned to (left hand side of the assignment) in transition t0.
////		supportA(t0): set of variables appearing in the expressions assigned to the variables of t0 (right hand side of the assignment).		
//		for (Integer lpnIndex : allTransitions.keySet()) {
//			Transition[] allTransInOneLpn = allTransitions.get(lpnIndex);									
//			for (int i = 0; i < allTransInOneLpn.length; i++) {
//				if (curTran.equals(allTransInOneLpn[i]))
//					continue;
//				Transition anotherTran = allTransInOneLpn[i];
//				for (String v : curTran.getAssignTrees().keySet()) {
//					for (ExprTree anotherTranAssignTree : anotherTran.getAssignTrees().values()) {
//						if (anotherTranAssignTree != null && anotherTranAssignTree.containsVar(v)) {
//							modifyAssignment.add(new Transition(lpnIndex, anotherTran.getIndex()));
//							if (Options.getDebugMode()) {
////								System.out.println("Variable " + v + " in " + curTran.getName()
////										+ " may change the right hand side of assignment " + anotherTranAssignTree + " in " + anotherTran.getName());
//							}
//						}					
//					}
//				}
//				for (ExprTree curTranAssignTree : curTran.getAssignTrees().values()) {
//					for (String v : anotherTran.getAssignTrees().keySet()) {
//						if (curTranAssignTree != null && curTranAssignTree.containsVar(v)) {
//							modifyAssignment.add(new Transition(lpnIndex, anotherTran.getIndex()));
//							if (Options.getDebugMode()) {
////								System.out.println("Variable " + v + " in " + anotherTran.getName() 
////										+ " may change the right hand side of assignment " + curTranAssignTree +  " in " + anotherTran.getName());
//							}
//						}
//					}
//				}
//				for (String v1 : curTran.getAssignTrees().keySet()) {
//					for (String v2 : anotherTran.getAssignTrees().keySet()) {
//						if (v1.equals(v2)) {
//							modifyAssignment.add(new Transition(lpnIndex, anotherTran.getIndex()));
//							if (Options.getDebugMode()) {
////								System.out.println("Variable " + v1 + " are assigned in " + curTran.getName() + " and " + anotherTran.getName()); 
//							}
//						}					
//					}
//				}
//			}
//		}
//	}
	/**
	 * Construct a set of transitions that can make the enabling condition of curTran true, by executing their assignments.
	 */
	public void buildOtherTransSetCurTranEnablingTrue() {
		ExprTree curTranEnablingTree = curTran.getEnablingTree();
		if (curTranEnablingTree != null) {// || curTranEnablingTree.toString().equals("TRUE") || curTranEnablingTree.toString().equals("FALSE"))) {
			//buildConjunctsOfEnabling(curTranEnablingTree, conjunctsOfEnabling);
			if (Options.getDebugMode()) { 
				writeStringWithEndOfLineToPORDebugFile("Transition " + curTran.getLpn().getLabel() + "(" + curTran.getLabel() + ")'s enabling tree is " + curTranEnablingTree.toString() + " and getOp() returns " + curTranEnablingTree.getOp());
				writeStringWithEndOfLineToPORDebugFile(curTran.getLpn().getLabel() + "(" + curTran.getLabel() + ")'s enabling transition has the following terms: ");
				for (int i1=0; i1<curTran.getConjunctsOfEnabling().size(); i1++) {
					writeStringWithEndOfLineToPORDebugFile(curTran.getConjunctsOfEnabling().get(i1).toString());
				}
			}
			if (!curTran.getConjunctsOfEnabling().isEmpty()) {
				for (int index=0; index<curTran.getConjunctsOfEnabling().size(); index++) {
					ExprTree conjunct = curTran.getConjunctsOfEnabling().get(index);
					HashSet<Transition> transCanEnableConjunct = new HashSet<Transition>();
					for (Transition anotherTran : allTransToLpnProcs.keySet()) {
						if (curTran.equals(anotherTran))
							continue;
						if (conjunct.getChange(anotherTran.getAssignments())=='T'
								|| conjunct.getChange(anotherTran.getAssignments())=='t'
								|| conjunct.getChange(anotherTran.getAssignments())=='X') {
							transCanEnableConjunct.add(anotherTran);
							if (Options.getDebugMode()) {
								writeStringWithEndOfLineToPORDebugFile(curTran.getLpn().getLabel() + "(" + curTran.getLabel() + ")'s conjunct (" + conjunct.toString() + ") in its enabling condition (" +  curTranEnablingTree 
										+  ") can be set to true by " + anotherTran.getLabel() + ". ");
								if (conjunct.getChange(anotherTran.getAssignments())=='T') 
									writeStringWithEndOfLineToPORDebugFile("The reason is conjunct.getChange(" + anotherTran.getLabel() + ".getAssignments()) = T.");
								if (conjunct.getChange(anotherTran.getAssignments())=='t') 
									writeStringWithEndOfLineToPORDebugFile("The reason is conjunct.getChange(" + anotherTran.getLabel() + ".getAssignments()) = t.");
								if (conjunct.getChange(anotherTran.getAssignments())=='X') 
									writeStringWithEndOfLineToPORDebugFile("The reason is conjunct.getChange(" + anotherTran.getLabel() + ".getAssignments()) = X.");
							}
						}
					}
					otherTransSetCurTranEnablingTrue.add(index, transCanEnableConjunct);
				}
			}
		}
	}
	
	public HashSet<Transition> getModifyAssignSet() {
		return modifyAssignment;
	}
	
	public HashSet<Transition> getDisableSet() {
		return disableSet;
	}
	
	public HashSet<Transition> getOtherTransDisableCurTranSet() {
		HashSet<Transition> otherTransDisableCurTranSet = new HashSet<Transition>();
		otherTransDisableCurTranSet.addAll(otherTransSetCurNonPersistentCurTranEnablingFalse);
		otherTransDisableCurTranSet.addAll(disableByStealingToken);
		otherTransDisableCurTranSet.addAll(modifyAssignment);
		return otherTransDisableCurTranSet;
	}

	public HashSet<Transition> getCurTranDisableOtherTransSet() {
		HashSet<Transition> curTranDisableOtherTransSet = new HashSet<Transition>();
		curTranDisableOtherTransSet.addAll(disableByStealingToken);
		curTranDisableOtherTransSet.addAll(curTranSetOtherTranEnablingFalse);
		return curTranDisableOtherTransSet;
	}
	
	public ArrayList<HashSet<Transition>> getOtherTransSetCurTranEnablingTrue() {
		return otherTransSetCurTranEnablingTrue;
	}
	
	public Transition getTran() {
		return curTran;
	}

	/*	For every transition curTran in T, where T is the set of all transitions, we check t (t != curTran) in T, 
		(1) intersection(VA(curTran), supportA(t)) != empty
		(2) intersection(VA(t), supportA(curTran)) != empty
		(3) intersection(VA(t), VA(curTran) != empty

		VA(t0) : set of variables being assigned to (left hand side of the assignment) in transition t0.
		supportA(t0): set of variables appearing in the expressions assigned to the variables of t0 (right hand side of the assignment).		
	 */
	public void buildModifyAssignSet() {
		ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
		if (allTransToLpnProcs.get(curTran).getStateMachineFlag()) { 
			transInOneStateMachine = allTransToLpnProcs.get(curTran).getProcessTransitions();
		}
		for (Transition anotherTran : allTransToLpnProcs.keySet()) {
			if (curTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
				// curTran does not have "modify assignments" problem with all transitions in the same process, if the process is a state machine.
				continue;
			for (String v : curTran.getAssignTrees().keySet()) {
				for (ExprTree anotherTranAssignTree : anotherTran.getAssignTrees().values()) {
					if (anotherTranAssignTree != null && anotherTranAssignTree.containsVar(v)) {
						modifyAssignment.add(anotherTran);
						if (Options.getDebugMode()) {
							writeStringWithEndOfLineToPORDebugFile("Variable " + v + " in " + curTran.getLpn().getLabel() + "(" + curTran.getLabel() + ")"
									+ " may change the right hand side of assignment " + anotherTranAssignTree + " in " + anotherTran.getLabel());
						}
					}					
				}
			}
			for (ExprTree curTranAssignTree : curTran.getAssignTrees().values()) {
				for (String v : anotherTran.getAssignTrees().keySet()) {
					if (curTranAssignTree != null && curTranAssignTree.containsVar(v)) {
						modifyAssignment.add(anotherTran);
						if (Options.getDebugMode()) {
							writeStringWithEndOfLineToPORDebugFile("Variable " + v + " in " + anotherTran.getLpn().getLabel() + "(" + anotherTran.getLabel() + ")"
									+ " may change the right hand side of assignment " + curTranAssignTree +  " in " + anotherTran.getLabel());
						}
					}
				}
			}
			for (String v1 : curTran.getAssignTrees().keySet()) {
				for (String v2 : anotherTran.getAssignTrees().keySet()) {
					if (v1.equals(v2) && !curTran.getAssignTree(v1).equals(anotherTran.getAssignTree(v2))) {
						modifyAssignment.add(anotherTran);
						if (Options.getDebugMode()) {
							writeStringWithEndOfLineToPORDebugFile("Variable " + v1 + " are assigned in " + curTran.getLpn().getLabel() + "(" + curTran.getLabel() + ") and " + anotherTran.getLpn().getLabel() + "(" + anotherTran.getLabel() + ")"); 
						}
					}					
				}
			}

		}
	}
	
	private void writeStringWithEndOfLineToPORDebugFile(String string) {		
		try {
			PORdebugBufferedWriter.append(string);
			PORdebugBufferedWriter.newLine();
			PORdebugBufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private void writeStringToPORDebugFile(String string) {		
		try {
			PORdebugBufferedWriter.append(string);
			//PORdebugBufferedWriter.newLine();
			PORdebugBufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

//	public HashSet<Transition> getEnableSet() {
//		HashSet<Transition> enableSet = new HashSet<Transition>();
//		enableSet.addAll(enableByBringingToken);
//		enableSet.addAll(enableBySettingEnablingTrue);
//		return enableSet;
//	}
	
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
