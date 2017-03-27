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
package main.java.edu.utah.ece.async.verification.platu.partialOrders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import main.java.edu.utah.ece.async.lpn.parser.ExprTree;
import main.java.edu.utah.ece.async.lpn.parser.Place;
import main.java.edu.utah.ece.async.lpn.parser.Transition;
import main.java.edu.utah.ece.async.lpn.parser.LpnDecomposition.LpnProcess;
import main.java.edu.utah.ece.async.verification.platu.main.Options;

/**
 * This class is used for constructing static dependent transition set for each seed transition.
 * @author Zhen Zhang
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class StaticDependencySets {
	protected Transition seedTran;
	//protected HashSet<Transition> disableSet; 
	protected HashSet<Transition> disableByStealingToken;
	
	/**
	 * All transitions whose enabling condition can be evaluated to <code>true</code> if seedTran is executed.
	 */
	protected ArrayList<HashSet<Transition>> otherTransSetSeedTranEnablingTrue;
	/**
	 * All transitions whose enabling condition can be evaluated to <code>false</code> if seedTran is executed.
	 */
	protected HashSet<Transition> seedTranSetOtherTranEnablingFalse;
	
	/**
 	 * All transitions that can potentially set seedTran's enabling condition to <code>false</code>. 
	 */
	protected HashSet<Transition> otherTransSetSeedTranEnablingFalse;
	
	protected HashSet<Transition> seedTranModifyOtherTranAssign;
	
	protected HashSet<Transition> otherTransModifySeedTranAssign;
	
	protected HashSet<Transition> seedTranOtherTransModifySameVars;
	
	
	private String PORdebugFileName;
	private FileWriter PORdebugFileStream;
	private BufferedWriter PORdebugBufferedWriter;
	protected HashMap<Transition, LpnProcess> allTransToLpnProcs; 
	// ---- Specific field variables for probabilistic models ----
	//private HashSet<Transition> seedTranModifyOtherTranRate; 
	//private ArrayList<HashSet<Transition>> otherTransModifySeedTranRate;
	// --------------------------------------------------
	
	public StaticDependencySets(Transition seedTran, HashMap<Transition, LpnProcess> allTransToLpnProcs) {
		this.seedTran = seedTran;
		this.allTransToLpnProcs = allTransToLpnProcs;
		//disableSet = new HashSet<Transition>();
		disableByStealingToken = new HashSet<Transition>();
		otherTransSetSeedTranEnablingTrue = new ArrayList<HashSet<Transition>>();		
		seedTranSetOtherTranEnablingFalse = new HashSet<Transition>();
		otherTransSetSeedTranEnablingFalse = new HashSet<Transition>();
		seedTranModifyOtherTranAssign = new HashSet<Transition>();
		otherTransModifySeedTranAssign = new HashSet<Transition>();
		seedTranOtherTransModifySameVars = new HashSet<Transition>();
		
		if (Options.getDebugMode()) {
			PORdebugFileName = Options.getPrjSgPath() + Options.getLogName() + "_" + Options.getPOR() + "_" 
					+ Options.getCycleClosingMthd() + "_" + Options.getCycleClosingStrongStubbornMethd() + ".dbg";
			try {
				PORdebugFileStream = new FileWriter(PORdebugFileName, true);
			} catch (IOException e) {
				e.printStackTrace();
			}						
			PORdebugBufferedWriter = new BufferedWriter(PORdebugFileStream);
		}
	}
	
	/**
	 * Build the set of transitions whose enabling condition can be set to <code>false</code> by executing <code>seedTran</code>.
	 */
	public void buildSeedTranSetOtherTranEnablingFalse() {
		// Test if seedTran can disable other transitions by executing its assignments. 
		// This excludes any transitions that are in the same process as seedTran, and the process is a state machine.
		if (!seedTran.getAssignments().isEmpty()) {
			ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
			if (allTransToLpnProcs.get(seedTran).getStateMachineFlag()) { 
				transInOneStateMachine = allTransToLpnProcs.get(seedTran).getProcessTransitions();
			}
			for (Transition anotherTran : allTransToLpnProcs.keySet()) {
				if (seedTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
					continue;	
				ExprTree anotherTranEnablingTree = anotherTran.getEnablingTree();
				if (anotherTranEnablingTree != null
						&& (anotherTranEnablingTree.getChange(seedTran.getAssignments())=='F'
						|| anotherTranEnablingTree.getChange(seedTran.getAssignments())=='f'
						|| anotherTranEnablingTree.getChange(seedTran.getAssignments())=='X')) {
					if (Options.getDebugMode()) {
						writeStringWithEndOfLineToPORDebugFile(seedTran.getLpn().getLabel() + "(" + seedTran.getLabel() + ") can disable " + anotherTran.getLpn().getLabel() + "(" + anotherTran.getLabel() + "). " 
								+ anotherTran.getLabel() + "'s enabling condition (" + anotherTranEnablingTree + "), may become false due to firing of " 
								+ seedTran.getLabel() + ".");
						if (anotherTranEnablingTree.getChange(seedTran.getAssignments())=='F') 
							writeStringWithEndOfLineToPORDebugFile("Reason is " + anotherTran.getLabel() + "_enablingTree.getChange(" + seedTran.getLabel() + ".getAssignments()) = F.");
						if (anotherTranEnablingTree.getChange(seedTran.getAssignments())=='f') 
							writeStringWithEndOfLineToPORDebugFile("Reason is " + anotherTran.getLabel() + "_enablingTree.getChange(" + seedTran.getLabel() + ".getAssignments()) = f.");
						if (anotherTranEnablingTree.getChange(seedTran.getAssignments())=='X') 
							writeStringWithEndOfLineToPORDebugFile("Reason is " + anotherTran.getLabel() + "_enablingTree.getChange(" + seedTran.getLabel() + ".getAssignments()) = X.");
					}
					seedTranSetOtherTranEnablingFalse.add(anotherTran);
				}
			}
		}
	}

	/**
	 * Build a set of transitions that may disable <code>seedTran</code>.
	 */
	public void buildOtherTransDisableSeedTran() {
		buildDisableByStealingToken();
		buildOtherTransModifySeedTranAssign();
		buildSeedTranOtherTransModifySameVars();
		buildOtherTransSetSeedTranEnablingFalse();		
	}
	
	/**
	 * Build a set of transitions that <code>seedTran</code> can disable.	
	 */
	public void buildSeedTranDisableOtherTrans() {
		buildDisableByStealingToken();
		buildSeedTranModifyOtherTransAssign();
		buildSeedTranOtherTransModifySameVars();
		buildSeedTranSetOtherTranEnablingFalse();		
	}
	
	public void buildOtherTransSetSeedTranEnablingFalse() {
		// Test if other transitions can disable seedTran by executing their assignments.
		// If seedTran's process is a state machine, this set excludes any transitions that are in the same process as seedTran.
		ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
		if (allTransToLpnProcs.get(seedTran).getStateMachineFlag()) { 
			transInOneStateMachine = allTransToLpnProcs.get(seedTran).getProcessTransitions();
		}
		for (Transition anotherTran : allTransToLpnProcs.keySet()) {
			if (seedTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
				continue;	
			if (!anotherTran.getAssignments().isEmpty()) {
				ExprTree seedTranEnablingTree = seedTran.getEnablingTree();
				if (seedTranEnablingTree != null
						&& (seedTranEnablingTree.getChange(anotherTran.getAssignments())=='F'
						|| seedTranEnablingTree.getChange(anotherTran.getAssignments())=='f'
						|| seedTranEnablingTree.getChange(anotherTran.getAssignments())=='X')) {
					if (Options.getDebugMode()) {
						writeStringWithEndOfLineToPORDebugFile(seedTran.getLpn().getLabel() + "(" + seedTran.getLabel() + ") can be disabled by " + anotherTran.getLpn().getLabel() + "(" + anotherTran.getLabel() + "). " 
								+ seedTran.getLabel() + "'s enabling condition (" + seedTranEnablingTree +  "), may become false due to firing of " 
								+ anotherTran.getLabel() + ".");
						if (seedTranEnablingTree.getChange(anotherTran.getAssignments())=='F') 
							writeStringWithEndOfLineToPORDebugFile("Reason is " + seedTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = F.");
						if (seedTranEnablingTree.getChange(anotherTran.getAssignments())=='f') 
							writeStringWithEndOfLineToPORDebugFile("Reason is " + seedTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = f.");
						if (seedTranEnablingTree.getChange(anotherTran.getAssignments())=='X') 
							writeStringWithEndOfLineToPORDebugFile("Reason is " + seedTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = X.");					
					}	
					otherTransSetSeedTranEnablingFalse.add(anotherTran);
				}
			}
		}
	}
	
	/**
	 * Construct the set of transitions that may disable <code>seedTran</code> by stealing its preset token.
	 */
	public void buildDisableByStealingToken() {
		// To avoid duplicated calls for this method, i.e.buildOtherTransDisableSeedTran and buildSeedTranDisableOtherTrans,
		// only proceed to build the disableByStealingToken set if it is not empty. 
		if (!disableByStealingToken.isEmpty()) {
			if (seedTran.hasConflict()) {
				if (!isSelfLoop()) {
					if (allTransToLpnProcs.get(seedTran).getStateMachineFlag()) {
						outerloop:for (Transition tranInConflict : seedTran.getConflictSet()) {
							// In a state machine, if tranInConflict and seedTran have disjoint enabling conditions, 
							// they are not dependent on each other. 
							// Temporary solution: compare each conjunct to decide if they are disjoint. 
							// Each conjunct in comparison only allows single literal or the negation of a literal. 
							// TODO: Need a (DPLL) SAT solver to decide if they are disjoint.
							if (seedTran.getConjunctsOfEnabling() != null && !seedTran.getConjunctsOfEnabling().isEmpty()) {							
								for (ExprTree conjunctOfSeedTran : seedTran.getConjunctsOfEnabling()) {	
									for (ExprTree conjunctOfTranInConflict : tranInConflict.getConjunctsOfEnabling()) {
										if (conjunctOfSeedTran.getVars().size()==1 && conjunctOfTranInConflict.getVars().size()==1
												&& conjunctOfSeedTran.getVars().equals(conjunctOfTranInConflict.getVars())) {
											ExprTree clause = new ExprTree(conjunctOfSeedTran, conjunctOfTranInConflict, "&&", 'l');
											HashMap<String, String> assign = new HashMap<String, String>();
											assign.put(conjunctOfSeedTran.getVars().get(0), "0");
											if (clause.evaluateExpr(assign) == 0.0) {
												assign.put(conjunctOfSeedTran.getVars().get(0), "1");
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
						for (Transition tranInConflict : seedTran.getConflictSet()) {
							disableByStealingToken.add(tranInConflict);
						}
					}	
				}
				if (Options.getDebugMode()) {
					writeStringWithEndOfLineToPORDebugFile(seedTran.getLpn().getLabel() + "(" + seedTran.getLabel() + ") can cause dependency relation by stealing tokens from these transitions:");
					for (Transition t : disableByStealingToken) {
						writeStringToPORDebugFile(t.getLpn().getLabel() + "(" + t.getLabel() + "), ");
					}
					writeStringWithEndOfLineToPORDebugFile("");
				}
			}
		}
	}

	private boolean isSelfLoop() {
		boolean isSelfLoop = false;
		Place[] seedPreset = seedTran.getPreset();
		Place[] seedPostset = seedTran.getPostset();
		for (Place preset : seedPreset) {
			for (Place postset : seedPostset) {
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
//	 * Construct a set of transitions that can make the enabling condition of seedTran true, by executing their assignments.
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
	public void buildOtherTransSetSeedTranEnablingTrue() {
		ExprTree seedTranEnablingTree = seedTran.getEnablingTree();
		if (seedTranEnablingTree != null) {// || curTranEnablingTree.toString().equals("TRUE") || curTranEnablingTree.toString().equals("FALSE"))) {
			if (Options.getDebugMode()) { 
				writeStringWithEndOfLineToPORDebugFile("Transition " + seedTran.getLpn().getLabel() + "(" + seedTran.getLabel() + ")'s enabling tree is " + seedTranEnablingTree.toString() + " and getOp() returns " + seedTranEnablingTree.getOp());
				writeStringWithEndOfLineToPORDebugFile(seedTran.getLpn().getLabel() + "(" + seedTran.getLabel() + ")'s enabling transition has the following terms: ");
				if (seedTran.getConjunctsOfEnabling() != null){
					for (int i1=0; i1<seedTran.getConjunctsOfEnabling().size(); i1++) {
						writeStringWithEndOfLineToPORDebugFile(seedTran.getConjunctsOfEnabling().get(i1).toString());
					}
				}	
			}
			if (!seedTran.getConjunctsOfEnabling().isEmpty()) {
				for (int index=0; index<seedTran.getConjunctsOfEnabling().size(); index++) {
					ExprTree conjunct = seedTran.getConjunctsOfEnabling().get(index);
					HashSet<Transition> transCanEnableConjunct = new HashSet<Transition>();
					for (Transition anotherTran : allTransToLpnProcs.keySet()) {
						if (seedTran.equals(anotherTran))
							continue;
						if (conjunct.getChange(anotherTran.getAssignments())=='T'
								|| conjunct.getChange(anotherTran.getAssignments())=='t'
								|| conjunct.getChange(anotherTran.getAssignments())=='X') {
							transCanEnableConjunct.add(anotherTran);
							if (Options.getDebugMode()) {
								writeStringWithEndOfLineToPORDebugFile(seedTran.getLpn().getLabel() + "(" + seedTran.getLabel() + ")'s conjunct (" + conjunct.toString() + ") in its enabling condition (" +  seedTranEnablingTree 
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
					otherTransSetSeedTranEnablingTrue.add(index, transCanEnableConjunct);
				}
			}
		}
	}
	
	public HashSet<Transition> getOtherTransDisableSeedTran(boolean persistentTranEnabled) {
		HashSet<Transition> otherTransDisableSeedTran = new HashSet<Transition>();
		if (!persistentTranEnabled) {
			otherTransDisableSeedTran.addAll(otherTransSetSeedTranEnablingFalse);
		}		
		otherTransDisableSeedTran.addAll(disableByStealingToken);
		otherTransDisableSeedTran.addAll(otherTransModifySeedTranAssign);
		otherTransDisableSeedTran.addAll(seedTranOtherTransModifySameVars);
		return otherTransDisableSeedTran;
	}

	public HashSet<Transition> getSeedTranDisableOtherTrans() {
		HashSet<Transition> seedTranDisableOtherTransSet = new HashSet<Transition>();
		seedTranDisableOtherTransSet.addAll(disableByStealingToken);
		seedTranDisableOtherTransSet.addAll(seedTranSetOtherTranEnablingFalse);
		seedTranDisableOtherTransSet.addAll(seedTranModifyOtherTranAssign);
		seedTranDisableOtherTransSet.addAll(seedTranOtherTransModifySameVars);
		return seedTranDisableOtherTransSet;
	}
	
	public HashSet<Transition> getDisableSet(boolean seedTranIsPersistent) {
		HashSet<Transition> disableSet = new HashSet<Transition>();
		disableSet.addAll(getSeedTranDisableOtherTrans());
		disableSet.addAll(getOtherTransDisableSeedTran(seedTranIsPersistent));
		return disableSet;
	}
	
	public ArrayList<HashSet<Transition>> getOtherTransSetSeedTranEnablingTrue() {
		return otherTransSetSeedTranEnablingTrue;
	}
	
	public Transition getSeedTran() {
		return seedTran;
	}
	
	/**
	 * For every transition t other than the seedTran, add it to <code>seedTranModifyOtherTranAssign if</code><br><br>
	 * 		
		intersection(VA(t), assignExp(seedTran)) != empty, <br> <br>

		where VA(t0) is the set of variables being assigned to (left hand side of the assignment) in transition t0, and <br>
		assignExp(t0) is the set of variables appearing in t0's every assignment expression (right hand side of an assignment).		
    */	
	public void buildSeedTranModifyOtherTransAssign() {
		ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
		if (allTransToLpnProcs.get(seedTran).getStateMachineFlag()) { 
			transInOneStateMachine = allTransToLpnProcs.get(seedTran).getProcessTransitions();
		}
		for (Transition anotherTran : allTransToLpnProcs.keySet()) {
			if (seedTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
				// seedTran does not have "modify assignments" problem with all transitions in the same process, if the process is a state machine.
				continue;
			for (String v : seedTran.getAssignTrees().keySet()) {
				for (ExprTree anotherTranAssignTree : anotherTran.getAssignTrees().values()) {
					if (anotherTranAssignTree != null && anotherTranAssignTree.containsVar(v)) {
						seedTranModifyOtherTranAssign.add(anotherTran);
						if (Options.getDebugMode()) {
							writeStringWithEndOfLineToPORDebugFile("Variable " + v + " in " + seedTran.getLpn().getLabel() + "(" + seedTran.getLabel() + ")"
									+ " may change the right hand side of assignment " + anotherTranAssignTree + " in " + anotherTran.getLabel());
						}
					}					
				}
			}
		}
	}

	/**
	 * For every transition t other than the seedTran, add it to <code>otherTransModifySeedTranAssign</code> if<br><br>
		intersection(VA(seedTran), assignExp(t)) != empty <br><br>			

		where VA(t0) is the set of variables being assigned to (left hand side of the assignment) in transition t0, and <br>
		assignExp(t0) is the set of variables appearing in t0's every assignment expression (right hand side of an assignment).		
	 */	
	public void buildOtherTransModifySeedTranAssign() {		
		ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
		if (allTransToLpnProcs.get(seedTran).getStateMachineFlag()) { 
			transInOneStateMachine = allTransToLpnProcs.get(seedTran).getProcessTransitions();
		}
		for (Transition anotherTran : allTransToLpnProcs.keySet()) {
			if (seedTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
				// seedTran does not have "modify assignments" problem with all transitions in the same process, if the process is a state machine.
				continue;
			for (ExprTree seedTranAssignTree : seedTran.getAssignTrees().values()) {
				for (String v : anotherTran.getAssignTrees().keySet()) {
					if (seedTranAssignTree != null && seedTranAssignTree.containsVar(v)) {
						otherTransModifySeedTranAssign.add(anotherTran);
						if (Options.getDebugMode()) {
							writeStringWithEndOfLineToPORDebugFile("Variable " + v + " in " + anotherTran.getLpn().getLabel() + "(" + anotherTran.getLabel() + ")"
									+ " may change the right hand side of assignment " + seedTranAssignTree +  " in " + anotherTran.getLabel());
						}
					}
				}
			}
		}
	}
	
	/**
	 * For every transition t other than the seedTran, add it to <code>seedTranOtherTransModifySameVars</code> if <br><br>			
		intersection(VA(t), VA(seedTran) != empty <br><br>

		where VA(t0) is the set of variables being assigned to (left hand side of the assignment) in transition t0, and <br>
		assignExp(t0) is the set of variables appearing in t0's every assignment expression (right hand side of an assignment).		
	 */	
	public void buildSeedTranOtherTransModifySameVars() {
		// To avoid duplicated calls for this method, i.e.buildOtherTransDisableSeedTran and buildSeedTranDisableOtherTrans,
		// only proceed to build the seedTranOtherTransModifySameVars set if it is not empty. 
		if (seedTranOtherTransModifySameVars.isEmpty()) {
			ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
			if (allTransToLpnProcs.get(seedTran).getStateMachineFlag()) { 
				transInOneStateMachine = allTransToLpnProcs.get(seedTran).getProcessTransitions();
			}
			for (Transition anotherTran : allTransToLpnProcs.keySet()) {
				if (seedTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
					// seedTran does not have "modify assignments" problem with all transitions in the same process, if the process is a state machine.
					continue;
				for (String v1 : seedTran.getAssignTrees().keySet()) {
					for (String v2 : anotherTran.getAssignTrees().keySet()) {
						if (v1.equals(v2) && !seedTran.getAssignTree(v1).equals(anotherTran.getAssignTree(v2))) {
							seedTranOtherTransModifySameVars.add(anotherTran);
							if (Options.getDebugMode()) {
								writeStringWithEndOfLineToPORDebugFile("Variable " + v1 + " are assigned in " + seedTran.getLpn().getLabel() + "(" + seedTran.getLabel() + ") and " + anotherTran.getLpn().getLabel() + "(" + anotherTran.getLabel() + ")"); 
							}
						}					
					}
				}
			}
		}		
	}
	
	protected void writeStringWithEndOfLineToPORDebugFile(String string) {		
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
//			for (Iterator<Integer> seedTranDisableIter = integerSet.iterator(); seedTranDisableIter.hasNext();) {
//				Integer tranInDisable = seedTranDisableIter.next();
//				System.out.print(lpn.getAllTransitions()[tranInDisable] + " ");
//			}
//			System.out.print("\n");
//		}				
//	}
 }
