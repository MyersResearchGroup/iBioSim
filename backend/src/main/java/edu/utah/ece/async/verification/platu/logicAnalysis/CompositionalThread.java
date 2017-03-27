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
package main.java.edu.utah.ece.async.verification.platu.logicAnalysis;

import java.util.ArrayList;
import java.util.List;

import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Transition;
import main.java.edu.utah.ece.async.verification.platu.stategraph.StateGraph;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class CompositionalThread extends Thread{
	
	private StateGraph[] srcArray = null;
	private StateGraph sg = null;
	private int iter = 0;
	private int newTransitions = 0;
	
	public CompositionalThread(StateGraph sg, StateGraph[] srcArray, int iter){
		this.srcArray = srcArray;
		this.sg = sg;
		this.iter = iter;
	}
	
	public int getNewTransitions(){
		return this.newTransitions;
	}
	
	public StateGraph getStateGraph(){
		return this.sg;
	}
	
	@Override
	public void run(){
		List<Constraint> newConstraintSet = new ArrayList<Constraint>();
		List<Constraint> oldConstraintSet = new ArrayList<Constraint>();
		
		for(StateGraph srcSG : this.srcArray){
			extractConstraints(sg, srcSG, newConstraintSet, oldConstraintSet);
			newTransitions += applyConstraintSet(sg, srcSG, iter, newConstraintSet, oldConstraintSet);
		}
	}
	
	/**
	 * Applies new constraints to the entire state set, and applies old constraints to the frontier state set.
     * @return Number of new transitions.
     */
	private static int applyConstraintSet(StateGraph sg, StateGraph srcSG, int iter, List<Constraint> newConstraintSet, List<Constraint> oldConstraintSet){
		int newTransitions = 0;
//		int[] thisIndexList = null;
//		int[] otherIndexList = null;
//		
//		String label = srcSG.getLabel();
//		String[] indexArray = sg.getKeyArray();
//		
//		for(int i = 0; i < indexArray.length; i++){
//			if(label == indexArray[i]){
//				thisIndexList = sg.getThisIndexArray(i);
//				otherIndexList = sg.getOtherIndexArray(i);
//				break;
//			}
//		}
		
//		int index = sg.getInterfaceIndex(srcSG.getLabel());
//		int[] thisIndexList = sg.getThisIndexArray(index);
//		int[] otherIndexList = sg.getOtherIndexArray(index);
		//LhpnFile srcLpn = srcSG.getLpn();
		//LhpnFile lpn = sg.getLpn();
		
		// TODO: (future)need to add getThisIndexArray in LhpnFile. 
		/*
		int[] thisIndexList = lpn.getThisIndexArray(srcLpn.ID);
		int[] otherIndexList = lpn.getOtherIndexArray(srcLpn.ID);
		
		if(newConstraintSet.size() > 0){
			for(Object obj : sg.getStateSet().toArray()){
				platu.stategraph.State currentState = (platu.stategraph.State) obj;

				for(Constraint c : newConstraintSet){
					if(compatible(currentState, c, thisIndexList, otherIndexList)){
						newTransitions += createNewState(sg, currentState, c);
					}
				}
			}

			for(platu.stategraph.State currentState : sg.getFrontierStateSet()){
				for(Constraint c : newConstraintSet){
					if(compatible(currentState, c, thisIndexList, otherIndexList)){						
						newTransitions += createNewState(sg, currentState, c);
					}
				}
			}
		}

		if(oldConstraintSet.size() > 0){
			for(platu.stategraph.State currentState : sg.getFrontierStateSet()){
				for(Constraint c : oldConstraintSet){
					if(compatible(currentState, c, thisIndexList, otherIndexList)){						
						newTransitions += createNewState(sg, currentState, c);
					}
				}
			}
		}
		*/
		return newTransitions;
	}
	
	/**
     * Extracts applicable constraints from a StateGraph.
     * @param sg The state graph the constraints are to be applied.
     * @param srcSG The state graph the constraint are extracted from.
     */
	private static void extractConstraints(StateGraph sg, StateGraph srcSG, List<Constraint> newConstraintSet, List<Constraint> oldConstraintSet){
		newConstraintSet.clear();
		oldConstraintSet.clear();
		LPN srcLpn = srcSG.getLpn();
		for(Constraint newConstraint : sg.getNewConstraintSet()){
			if(newConstraint.getLpn() != srcLpn) continue;
			newConstraintSet.add(newConstraint);
		}
		for(Constraint oldConstraint : sg.getOldConstraintSet()){
			if(oldConstraint.getLpn() != srcLpn) continue;
			oldConstraintSet.add(oldConstraint);
		}
	}
	
	/**
     * Determines whether a constraint is compatible with a state.
     * @return True if compatible, otherwise False.
     */
	@SuppressWarnings("unused")
	private static boolean compatible(main.java.edu.utah.ece.async.verification.platu.stategraph.State currentState, Constraint constr, int[] thisIndexList, int[] otherIndexList){
		int[] constraintVector = constr.getVector();
		int[] currentVector = currentState.getVariableVector();
		
		for(int i = 0; i < thisIndexList.length; i++){
			int thisIndex = thisIndexList[i];
			int otherIndex = otherIndexList[i];

			if(currentVector[thisIndex] != constraintVector[otherIndex]){
				return false;
			}
		}
		
		return true;
	}
	
	/**
     * Creates a state from a given constraint and compatible state.  If the state is new, then findSG is called.
     * @return Number of new transitions.
     */
	@SuppressWarnings("unused")
	private static int createNewState(StateGraph sg, main.java.edu.utah.ece.async.verification.platu.stategraph.State compatibleState, Constraint c){
		int newTransitions = 0;

		// Create new state and insert into state graph
		main.java.edu.utah.ece.async.verification.platu.stategraph.State newState = new main.java.edu.utah.ece.async.verification.platu.stategraph.State(compatibleState);
		int[] newVector = newState.getVariableVector();
		
		//List<VarNode> variableList = c.getVariableList();
		List<Integer> variableList = c.getVariableList();
		List<Integer> valueList = c.getValueList();
		
		//int[] compatibleVector = compatibleState.getVector();
		for(int i = 0; i < variableList.size(); i++){
			//int index = variableList.get(i).getIndex(compatibleVector);
			int index = variableList.get(i);
			newVector[index] = valueList.get(i);
		}
		
		main.java.edu.utah.ece.async.verification.platu.stategraph.State nextState = sg.addState(newState);
		if(nextState == newState){
			int result = sg.synchronizedConstrFindSG(nextState);
			if(result < 0) return newTransitions;
			
//			sg.initializeTranList(nextState);
			newTransitions += result;
		}

//    	StateTran stTran = new StateTran(compatibleState, constraintTran, state);

		Transition constraintTran = c.getLpnTransition();
//		constraintTran.synchronizedAddStateTran(compatibleState, nextState);
//    	sg.addEnabledTran(compatibleState, constraintTran);
    	newTransitions++;
		
		return newTransitions;
	}
}
