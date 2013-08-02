package verification.platu.partialOrders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import verification.platu.main.Options;

import lpn.parser.ExprTree;
import lpn.parser.Transition;
import lpn.parser.LpnDecomposition.LpnProcess;

public class ProbStaticDependencySets extends StaticDependencySets {
	
	/**
	 * The set of transitions whose transition rates can be changed by executing curTran.
	 */
	private HashSet<Transition> curTranModifyOtherTranRate;
	
	/**
	 * The set of transitions that can potentially change curTran's rate.   
	 */
	private HashSet<Transition> otherTransModifyCurTranRate;

	public ProbStaticDependencySets(Transition curTran,
			HashMap<Transition, LpnProcess> allTransToLpnProcs) {
		super(curTran, allTransToLpnProcs);
		curTranModifyOtherTranRate = new HashSet<Transition>();
		otherTransModifyCurTranRate = new HashSet<Transition>();
	}
	
	/**
	 * Construct a set of transitions. The transition rate of each transition in this set can potentially be modified by curTran's assignment(s). 
	 */
	public void buildCurTranModifyOtherTransRatesSet() {
		// This excludes any transitions that are in the same process as curTran, and the process is a state machine.
		if (!curTran.getAssignments().isEmpty()) {
			Set<String> assignedVarNames = curTran.getAssignments().keySet();
			ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
			if (allTransToLpnProcs.get(curTran).getStateMachineFlag()) { 
				transInOneStateMachine = allTransToLpnProcs.get(curTran).getProcessTransitions();
			}
			for (Transition anotherTran : allTransToLpnProcs.keySet()) {
				if (curTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
					continue;	
				ExprTree anotherTranDelayTree = anotherTran.getDelayTree();
				if (anotherTranDelayTree != null) {
					for (String var : assignedVarNames) {
						if (anotherTranDelayTree.containsVar(var)) {
							curTranModifyOtherTranRate.add(anotherTran);
							break;
						}							
					}
				}
			}
		}
		disableSet.addAll(curTranModifyOtherTranRate);		
	}

	/**
	 * Construct a set of transitions. Each transition in this set can potentially modify curTran's transition rate.
	 */
	public void buildOtherTransModifyCurTranRateSet() {		
		// This excludes any transitions that are in the same process as curTran, and the process is a state machine.
		ArrayList<Transition> transInOneStateMachine = new ArrayList<Transition>();
		if (allTransToLpnProcs.get(curTran).getStateMachineFlag()) { 
			transInOneStateMachine = allTransToLpnProcs.get(curTran).getProcessTransitions();
		}
		for (Transition anotherTran : allTransToLpnProcs.keySet()) {
			if (curTran.equals(anotherTran) || transInOneStateMachine.contains(anotherTran))
				continue;	
			if (!anotherTran.getAssignments().isEmpty()) {
				ExprTree curTranDelayTree = curTran.getDelayTree();
				if (curTranDelayTree != null) {
					Set<String> assignedVarNames = anotherTran.getAssignments().keySet();
					for (String var : assignedVarNames) {
						if (curTranDelayTree.containsVar(var)) {
							otherTransModifyCurTranRate.add(anotherTran);
							break;
						}							
					}				
					if (Options.getDebugMode()) {
//						writeStringWithEndOfLineToPORDebugFile(curTran.getLpn().getLabel() + "(" + curTran.getLabel() + ") can be disabled by " + anotherTran.getLpn().getLabel() + "(" + anotherTran.getLabel() + "). " 
//								+ curTran.getLabel() + "'s enabling condition (" + curTranDelayTree +  "), may become false due to firing of " 
//								+ anotherTran.getLabel() + ".");
//						if (curTranDelayTree.getChange(anotherTran.getAssignments())=='F') 
//							writeStringWithEndOfLineToPORDebugFile("Reason is " + curTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = F.");
//						if (curTranDelayTree.getChange(anotherTran.getAssignments())=='f') 
//							writeStringWithEndOfLineToPORDebugFile("Reason is " + curTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = f.");
//						if (curTranDelayTree.getChange(anotherTran.getAssignments())=='X') 
//							writeStringWithEndOfLineToPORDebugFile("Reason is " + curTran.getLabel() + "_enablingTree.getChange(" + anotherTran.getLabel() + ".getAssignments()) = X.");					
					}	
					otherTransModifyCurTranRate.add(anotherTran);
				}
			}
		}
		disableSet.addAll(otherTransModifyCurTranRate);
	}
	
}
