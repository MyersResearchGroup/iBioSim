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
package edu.utah.ece.async.lema.verification.platu.partialOrders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.utah.ece.async.lema.verification.lpn.ExprTree;
import edu.utah.ece.async.lema.verification.lpn.Transition;
import edu.utah.ece.async.lema.verification.lpn.LpnDecomposition.LpnProcess;
import edu.utah.ece.async.lema.verification.platu.main.Options;

/**
 * This class extends the non-probabilistic static dependency transition set. 
 * It adds two more sets for dependency relations when transition rate expression is considered. 
 *  
 * @author Zhen Zhang
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ProbStaticDependencySets extends StaticDependencySets {
	
	/**
	 * The set of transitions whose transition rates can be changed by executing seedTran.
	 */
	private HashSet<Transition> seedTranModifyOtherTranRate;
	
	/**
	 * The set of transitions that can potentially change seedTran's rate.   
	 */
	private HashSet<Transition> otherTransModifySeedTranRate;

	public ProbStaticDependencySets(Transition seedTran,
			HashMap<Transition, LpnProcess> allTransToLpnProcs) {
		super(seedTran, allTransToLpnProcs);
		seedTranModifyOtherTranRate = new HashSet<Transition>();
		otherTransModifySeedTranRate = new HashSet<Transition>();
	}
	
	/**
	 * Construct a set of transitions. The transition rate of each transition in this set can potentially be modified by seedTran's assignment(s). 
	 */
	public void buildSeedTranModifyOtherTransRatesSet() {
		// This excludes any transitions that are in the same process as seedTran, and the process is a state machine.
		if (!seedTran.getAssignments().isEmpty()) {
			Set<String> assignedVarNames = seedTran.getAssignments().keySet();
			ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
			if (allTransToLpnProcs.get(seedTran).getStateMachineFlag()) { 
				transInOneStateMachine = allTransToLpnProcs.get(seedTran).getProcessTransitions();
			}
			for (Transition anotherTran : allTransToLpnProcs.keySet()) {
				if (seedTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
					continue;	
				ExprTree anotherTranDelayTree = anotherTran.getDelayTree();
				if (anotherTranDelayTree != null) {
					for (String var : assignedVarNames) {
						if (anotherTranDelayTree.containsVar(var)) {
							seedTranModifyOtherTranRate.add(anotherTran);
							break;
						}							
					}
				}
			}
		}
		//disableSet.addAll(seedTranModifyOtherTranRate);		
	}

	/**
	 * Construct a set of transitions. Each transition in this set can potentially modify seedTran's transition rate.
	 */
	public void buildOtherTransModifySeedTranRateSet() {		
		// This excludes any transitions that are in the same process as seedTran, and the process is a state machine.
		ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
		if (allTransToLpnProcs.get(seedTran).getStateMachineFlag()) { 
			transInOneStateMachine = allTransToLpnProcs.get(seedTran).getProcessTransitions();
		}
		for (Transition anotherTran : allTransToLpnProcs.keySet()) {
			if (seedTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
				continue;	
			if (!anotherTran.getAssignments().isEmpty()) {
				ExprTree seedTranDelayTree = seedTran.getDelayTree();
				if (seedTranDelayTree != null) {
					Set<String> assignedVarNames = anotherTran.getAssignments().keySet();
					for (String var : assignedVarNames) {
						if (seedTranDelayTree.containsVar(var)) {
							otherTransModifySeedTranRate.add(anotherTran);
							break;
						}							
					}				
					if (Options.getDebugMode()) {
//						writeStringWithEndOfLineToPORDebugFile(seedTran.getLpn().getLabel() + "(" + seedTran.getLabel() + ") can be disabled by " + anotherTran.getLpn().getLabel() + "(" + anotherTran.getLabel() + "). " 
//								+ seedTran.getLabel() + "'s enabling condition (" + seedTranDelayTree +  "), may become false due to firing of " 
//								+ anotherTran.getLabel() + ".");
//						if (seedTranDelayTree.getChange(anotherTran.getAssignments())=='F') 
//							writeStringWithEndOfLineToPORDebugFile("Reason is " + seedTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = F.");
//						if (seedTranDelayTree.getChange(anotherTran.getAssignments())=='f') 
//							writeStringWithEndOfLineToPORDebugFile("Reason is " + seedTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = f.");
//						if (seedTranDelayTree.getChange(anotherTran.getAssignments())=='X') 
//							writeStringWithEndOfLineToPORDebugFile("Reason is " + seedTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = X.");					
					}	
					//otherTransModifySeedTranRate.add(anotherTran);
				}
			}
		}
		//disableSet.addAll(otherTransModifySeedTranRate);
	}
	
	@Override
	public HashSet<Transition> getDisableSet(boolean seedTranIsPersistent) {
		HashSet<Transition> disableSet = new HashSet<Transition>();
		disableSet.addAll(getSeedTranDisableOtherTrans());
		disableSet.addAll(getOtherTransDisableSeedTran(seedTranIsPersistent));
		disableSet.addAll(seedTranModifyOtherTranRate);
		disableSet.addAll(otherTransModifySeedTranRate);
		return disableSet;
	}
	
	@Override
	public HashSet<Transition> getOtherTransDisableSeedTran(boolean persistentTranEnabled) {
		HashSet<Transition> otherTransDisableSeedTran = new HashSet<Transition>();
		if (!persistentTranEnabled) {
			otherTransDisableSeedTran.addAll(otherTransSetSeedTranEnablingFalse);
		}		
		otherTransDisableSeedTran.addAll(disableByStealingToken);
		otherTransDisableSeedTran.addAll(otherTransModifySeedTranAssign);
		otherTransDisableSeedTran.addAll(seedTranOtherTransModifySameVars);
		otherTransDisableSeedTran.addAll(otherTransModifySeedTranRate);
		return otherTransDisableSeedTran;
	}

	@Override
	public HashSet<Transition> getSeedTranDisableOtherTrans() {
		HashSet<Transition> seedTranDisableOtherTransSet = new HashSet<Transition>();
		seedTranDisableOtherTransSet.addAll(disableByStealingToken);
		seedTranDisableOtherTransSet.addAll(seedTranSetOtherTranEnablingFalse);
		seedTranDisableOtherTransSet.addAll(seedTranModifyOtherTranAssign);
		seedTranDisableOtherTransSet.addAll(seedTranOtherTransModifySameVars);
		seedTranDisableOtherTransSet.addAll(seedTranModifyOtherTranRate);
		return seedTranDisableOtherTransSet;
	}
	
	// TEMP//
	public HashSet<Transition> getSeedOtherTokenConflictTrans() {
		HashSet<Transition> seedOtherTokenConflictTransSet = new HashSet<Transition>();
		seedOtherTokenConflictTransSet.addAll(disableByStealingToken);
		return seedOtherTokenConflictTransSet;
	}

	public HashSet<Transition> getSeedOtherSetEnablingFalse(boolean seedTranIsPersistent) {
		HashSet<Transition> seedOtherSetEnablingFalseSet = new HashSet<Transition>();

		seedOtherSetEnablingFalseSet.addAll(otherTransModifySeedTranAssign);
		seedOtherSetEnablingFalseSet.addAll(seedTranModifyOtherTranAssign);
		
		seedOtherSetEnablingFalseSet.addAll(seedTranOtherTransModifySameVars);
		
		seedOtherSetEnablingFalseSet.addAll(seedTranSetOtherTranEnablingFalse);
		if (!seedTranIsPersistent) {
			seedOtherSetEnablingFalseSet.addAll(otherTransSetSeedTranEnablingFalse);
		}
				
		seedOtherSetEnablingFalseSet.addAll(seedTranModifyOtherTranRate);
		seedOtherSetEnablingFalseSet.addAll(otherTransModifySeedTranRate);
		
		return seedOtherSetEnablingFalseSet;
	}
	
}
