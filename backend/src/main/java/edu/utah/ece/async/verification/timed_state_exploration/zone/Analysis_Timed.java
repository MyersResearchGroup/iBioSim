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
package main.java.edu.utah.ece.async.verification.timed_state_exploration.zone;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Transition;
import main.java.edu.utah.ece.async.verification.platu.logicAnalysis.Analysis;
import main.java.edu.utah.ece.async.verification.platu.platuLpn.LPNTranRelation;
import main.java.edu.utah.ece.async.verification.platu.platuLpn.LpnTranList;
import main.java.edu.utah.ece.async.verification.platu.project.PrjState;
import main.java.edu.utah.ece.async.verification.platu.stategraph.State;
import main.java.edu.utah.ece.async.verification.platu.stategraph.StateGraph;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Analysis_Timed extends Analysis{

	public Analysis_Timed(StateGraph[] lpnList, State[] initStateArray,
			LPNTranRelation lpnTranRelation, String method) {
		super(lpnList, initStateArray, lpnTranRelation, method);
	}

	public Analysis_Timed(StateGraph[] lpnList) {
		super(lpnList);
	}
	
	int iterations = 0;
	int stack_depth = 0;
	int max_stack_depth = 0;
	
	//public StateGraph_timed[] search_dfs_timed(final StateGraph_timed[] sgList, 
	//		final TimedState[] initStateArray) {
	public StateGraph_timed[] search_dfs_timed(final StateGraph_timed[] sgList,
			final State[] initStateArray){
		System.out.println("---> calling function search_dfs_timed");
				
		double peakUsedMem = 0;
		double peakTotalMem = 0;
		boolean failure = false;
		int tranFiringCnt = 0;
		int totalStates = 1;
		int arraySize = sgList.length;
		
		//Stack<State[]> stateStack = new Stack<State[]>();
		HashSet<PrjState> stateStack = new HashSet<PrjState>();
		Stack<LinkedList<Transition>> lpnTranStack = new Stack<LinkedList<Transition>>();
		Stack<Integer> curIndexStack = new Stack<Integer>();

		HashSet<PrjState> prjStateSet = new HashSet<PrjState>();
		
		PrjState initPrjState = new PrjState(initStateArray);
		prjStateSet.add(initPrjState);
		//initPrjState.print(getLpnList(sgList));
		
		PrjState stateStackTop = initPrjState;
		stateStack.add(stateStackTop);
		
		constructDstLpnList(sgList);
		printDstLpnList(sgList);

		LpnTranList initEnabled = sgList[0].getEnabled(initStateArray[0]);
		lpnTranStack.push(initEnabled.clone());
		curIndexStack.push(0);

		main_while_loop: while (failure == false && stateStack.size() != 0) {

			long curTotalMem = Runtime.getRuntime().totalMemory();
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

			if (curTotalMem > peakTotalMem)
				peakTotalMem = curTotalMem;

			if (curUsedMem > peakUsedMem)
				peakUsedMem = curUsedMem;

			if (stateStack.size() > max_stack_depth)
				max_stack_depth = stateStack.size();
			
			iterations++;
			if (iterations % 100000 == 0) {
				System.out.println("---> #iteration " + iterations
						+ "> # LPN transition firings: " + tranFiringCnt
						+ ", # of prjStates found: " + totalStates
						+ ", stack_depth: " + stateStack.size()
						+ " used memory: " + (float) curUsedMem / 1000000
						+ " free memory: "
						+ (float) Runtime.getRuntime().freeMemory() / 1000000);
			}

			State[] curStateArray = stateStackTop.toStateArray(); //stateStack.peek();
			int curIndex = curIndexStack.peek();
			LinkedList<Transition> curEnabled = lpnTranStack.peek();

			// If all enabled transitions of the current LPN are considered,
			// then consider the next LPN
			// by increasing the curIndex.
			// Otherwise, if all enabled transitions of all LPNs are considered,
			// then pop the stacks.
			if (curEnabled.size() == 0) {
				lpnTranStack.pop();
				curIndexStack.pop();
				curIndex++;
				while (curIndex < arraySize) {
					curEnabled = (sgList[curIndex].getEnabled(curStateArray[curIndex])).clone();
					if (curEnabled.size() > 0) {
						lpnTranStack.push(curEnabled);
						curIndexStack.push(curIndex);
						break;
					} 					
					curIndex++;
				}
			}
			if (curIndex == arraySize) {
				prjStateSet.add(stateStackTop);
				stateStack.remove(stateStackTop);
				stateStackTop = stateStackTop.getFather();
				continue;
			}

			Transition firedTran = curEnabled.removeLast();	
//			System.out.println("###################");			
//			System.out.println("Fired transition: " + firedTran.getName());
			//TimedState[] nextStateArray =
			State[] nextStateArray =
				//sgList[curIndex].fire(sgList, (TimedState[]) curStateArray, firedTran);
				sgList[curIndex].fire(sgList, curStateArray, firedTran);
			tranFiringCnt++;

			// Check if the firedTran causes disabling error or deadlock.
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] curEnabledArray = new LinkedList[arraySize];
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] nextEnabledArray = new LinkedList[arraySize];
			for (int i = 0; i < arraySize; i++) {
				StateGraph_timed sg = sgList[i];
				LinkedList<Transition> enabledList = sg.getEnabled(curStateArray[i]);
				curEnabledArray[i] = enabledList;
				enabledList = sg.getEnabled(nextStateArray[i]);
				//enabledList = sg.getEnabled(nextStateArray[i]);
				nextEnabledArray[i] = enabledList;
				Transition disabledTran = firedTran.disablingError(
						curEnabledArray[i], nextEnabledArray[i]);
				if (disabledTran != null) {
					System.err.println("Disabling Error: "
							+ disabledTran.getFullLabel() + " is disabled by "
							+ firedTran.getFullLabel());
					failure = true;
					break main_while_loop;
				}
			}

			if (Analysis.deadLock(sgList, nextStateArray) == true) {
				System.out.println("*** Verification failed: deadlock.");
				failure = true;
				break main_while_loop;
			}

			PrjState nextPrjState = new PrjState(nextStateArray);
			//nextPrjState.print(getLpnList(sgList));
			//Boolean	existingState = prjStateSet.contains(nextPrjState) || stateStack.contains(nextPrjState);
			
			Boolean existingState = checkStateSet(prjStateSet, nextPrjState, 
					ZoneType.getSubsetFlag(), ZoneType.getSupersetFlag())
					| checkStack(nextPrjState, stateStackTop, lpnTranStack,
							curIndexStack, stateStack, ZoneType.getSubsetFlag(),
							ZoneType.getSupersetFlag());
			
			
			if (existingState == false) {
				//prjStateSet.add(nextPrjState);
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				stateStackTop = nextPrjState;
				stateStack.add(stateStackTop);
				lpnTranStack.push((LpnTranList) nextEnabledArray[0].clone());
				curIndexStack.push(0);
				totalStates++;
			}
		}

		double totalStateCnt = prjStateSet.size();
		
		System.out.println("---> final numbers: # LPN transition firings: "	+ tranFiringCnt 
				+ ", # of prjStates found: " + totalStateCnt 
				+ ", max_stack_depth: " + max_stack_depth 
				+ ", peak total memory: " + peakTotalMem / 1000000 + " MB"
				+ ", peak used memory: " + peakUsedMem / 1000000 + " MB");

		return sgList;
	}
	
	private static void constructDstLpnList(StateGraph[] lpnList) {
		for (int i=0; i<lpnList.length; i++) {
			LPN curLPN = lpnList[i].getLpn();
			Transition[] allTrans = curLPN.getAllTransitions();
			for (int j=0; j<allTrans.length; j++) {
				Transition curTran = allTrans[j];
				for (int k=0; k<lpnList.length; k++) {
					curTran.setDstLpnList(lpnList[k].getLpn());
				}
								
			}
		}
	}
	
	private static void printDstLpnList(StateGraph[] lpnList) {
		System.out.println("++++++ dstLpnList ++++++");
		for (int i=0; i<lpnList.length; i++) {
			LPN curLPN = lpnList[i].getLpn();
			System.out.println("LPN: " + curLPN.getLabel());
			Transition[] allTrans = curLPN.getAllTransitions(); 
			for (int j=0; j< allTrans.length; j++) {
				System.out.print(allTrans[j].getLabel() + ": ");
				for (int k=0; k< allTrans[j].getDstLpnList().size(); k++) {
					System.out.print(allTrans[j].getDstLpnList().get(k).getLabel() + ",");
				}
				System.out.print("\n");
			}
			System.out.println("----------------");
		}
		System.out.println("++++++++++++++++++++");
	}
	
	private static boolean projectUntimedEquals(PrjState left, PrjState right){
		State[] leftArray = left.toStateArray();
		State[] rightArray = right.toStateArray();
		
		Boolean equals = true;
		
		for(int i=0; i<leftArray.length; i++){	
			equals &= ((TimedState)leftArray[i]).untimedStateEquals(rightArray[i]);
		}
		
		return equals;
	}
	
	private static boolean checkStack(PrjState nextPrjState, PrjState stateStackTop,
			Stack<LinkedList<Transition>> lpnTranStack, Stack<Integer> curIndexStack,
			HashSet<PrjState> stateStack, boolean subsets,
			boolean supersets){
		
		boolean existingState = false;
		
		if(!subsets && !supersets){
			return stateStack.contains(nextPrjState);
		}
		
		ZoneType nextZone = ((TimedState) nextPrjState.get(0)).getZone();
		
		PrjState stackStateIterator = stateStackTop;
		int stackDepth = 1;
		while(stackStateIterator != null){
			
			if(!projectUntimedEquals(nextPrjState, stackStateIterator)){
				stackStateIterator = stackStateIterator.getFather();
				stackDepth++;
				continue;
			}
			
			ZoneType iteratorZone = ((TimedState) stackStateIterator.get(0)).getZone();
			
			// Check for subset.
			if(subsets && nextZone.subset(iteratorZone) 
					|| (nextZone.equals(iteratorZone))){
				
//				if(supersets){
//					existingState |= true;
//				}
//				else{
				existingState = true;
				break;
				//}
			}
			
			if(!supersets){
				stackStateIterator = stackStateIterator.getFather();
				stackDepth++;
				continue;
			}

			// Check for the superset.
			if(iteratorZone.subset(nextZone)){
				PrjState father = stackStateIterator.getFather();
				PrjState child = stackStateIterator.getChild();

				if(child != null){
					child.setFather(father);
				}
				if(father != null){
					father.setChild(child);
				}


				// Remove the corresponding items on the stacks and state set.
				lpnTranStack.remove(lpnTranStack.size() - stackDepth);
				curIndexStack.remove(curIndexStack.size() - stackDepth);
				stateStack.remove(stackStateIterator);
				
				stackStateIterator = stackStateIterator.getFather();
				continue;
			}

			stackStateIterator = stackStateIterator.getFather();
			stackDepth++;
		}
		
		
		return existingState;
	}
	
	private static boolean checkStateSet(HashSet<PrjState> prjStateSet, PrjState nextPrjState,
			boolean subsets, boolean supersets){
		
		if(!subsets && !supersets){
			return prjStateSet.contains(nextPrjState);
		}
		
		// Extract the zone.
		ZoneType nextZone = ((TimedState) nextPrjState.get(0)).getZone();
		
		Iterator<PrjState> stateSetIterator = prjStateSet.iterator();
		while(stateSetIterator.hasNext()){

			PrjState nextSetState = stateSetIterator.next();

			if(!projectUntimedEquals(nextPrjState, nextSetState)){
				continue;
			}
			
			// Check for the subsets.
			ZoneType iteratorZone = ((TimedState) nextSetState.get(0)).getZone();
			
			if(subsets){
				//stateSetIterator.remove();
				//if(supersets && nextZone != iteratorZone){
				if(supersets){
					if(nextZone.subset(iteratorZone)){
						return true;
					}
					else if(iteratorZone.subset(nextZone)){
						stateSetIterator.remove();
					}
				}
				else if (nextZone.subset(iteratorZone)){
					//return nextZone.subset(iteratorZone);
					return true;
				}
			}
			else if (nextZone.equals(iteratorZone)){
				//return nextZone.equals(iteratorZone);
				return true;
			}
		}
		
		return false;
	}

}
