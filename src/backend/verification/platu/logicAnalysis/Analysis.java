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
package backend.verification.platu.logicAnalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.PriorityQueue;
import java.util.Stack;


import backend.verification.platu.MDD.MDT;
import backend.verification.platu.MDD.Mdd;
import backend.verification.platu.MDD.mddNode;
import backend.verification.platu.common.IndexObjMap;
import backend.verification.platu.main.Options;
import backend.verification.platu.markovianAnalysis.ProbGlobalState;
import backend.verification.platu.markovianAnalysis.ProbGlobalStateSet;
import backend.verification.platu.markovianAnalysis.ProbLocalStateGraph;
import backend.verification.platu.partialOrders.DependentSet;
import backend.verification.platu.partialOrders.DependentSetComparator;
import backend.verification.platu.partialOrders.ProbStaticDependencySets;
import backend.verification.platu.partialOrders.StaticDependencySets;
import backend.verification.platu.platuLpn.LPNTranRelation;
import backend.verification.platu.platuLpn.LpnTranList;
import backend.verification.platu.por1.AmpleSet;
import backend.verification.platu.project.PrjState;
import backend.verification.platu.stategraph.State;
import backend.verification.platu.stategraph.StateGraph;
import backend.verification.timed_state_exploration.octagon.Equivalence;
import backend.verification.timed_state_exploration.zoneProject.EventSet;
import backend.verification.timed_state_exploration.zoneProject.TimedPrjState;
import backend.verification.timed_state_exploration.zoneProject.TimedStateSet;
import backend.verification.timed_state_exploration.zoneProject.Zone;
import dataModels.lpn.parser.Abstraction;
import dataModels.lpn.parser.ExprTree;
import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Place;
import dataModels.lpn.parser.Transition;
import dataModels.lpn.parser.LpnDecomposition.LpnProcess;
import dataModels.util.GlobalConstants;
import dataModels.util.Message;

import java.util.Queue;
import java.util.Iterator;
 
/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Analysis extends Observable{

	private LinkedList<Transition> traceCex;
	protected Mdd mddMgr = null;
	private HashMap<Transition, HashSet<Transition>> cachedNecessarySets = new HashMap<Transition, HashSet<Transition>>();
	private final Message message = new Message();
	
	/*
	 * visitedTrans is used in computeNecessary for a disabled transition of interest, to keep track of all transitions visited during trace-back.
	 */
	private HashSet<Transition> visitedTrans;
	HashMap<Transition, StaticDependencySets> staticDependency = new HashMap<Transition, StaticDependencySets>();
	private String separator = GlobalConstants.separator;
		
	public Analysis(StateGraph[] lpnList, State[] initStateArray, LPNTranRelation lpnTranRelation, String method) {
		traceCex = new LinkedList<Transition>();
		mddMgr = new Mdd(lpnList.length);

		if (method.equals("dfs")) {
			//if (Options.getPOR().equals("off")) {
				//this.search_dfs(lpnList, initStateArray);
				//this.search_dfs_mdd_1(lpnList, initStateArray);
				//this.search_dfs_mdd_2(lpnList, initStateArray);
			//}
			//else
			//	this.search_dfs_por(lpnList, initStateArray, lpnTranRelation, "state");
		}
		else if (method.equals("bfs")==true)
			this.search_bfs(lpnList, initStateArray);
		else if (method == "dfs_noDisabling")
			//this.search_dfs_noDisabling(lpnList, initStateArray);
			this.search_dfs_noDisabling_fireOrder(lpnList, initStateArray);
	}
	
	/**
	 * This constructor performs dfs.
	 * @param lpnList
	 */
	public Analysis(StateGraph[] lpnList){
		traceCex = new LinkedList<Transition>();
		mddMgr = new Mdd(lpnList.length);
//		if (method.equals("dfs")) {
//			if (Options.getPOR().equals("off")) {
//				//this.search_dfs(lpnList, initStateArray);
//				this.search_dfsNative(lpnList, initStateArray);
//				//this.search_dfs_mdd_1(lpnList, initStateArray);
//				//this.search_dfs_mdd_2(lpnList, initStateArray);
//			}
//			else
//			{
//				//behavior analysis
//				boolean BA = true;
//				if(BA==true)
//				{
//					CompositionalAnalysis.searchCompositional(lpnList);
//					this.search_dfs_por(lpnList, initStateArray, lpnTranRelation, "state");
//				}
//				else
//					this.search_dfs_por(lpnList, initStateArray, lpnTranRelation, "lpn");
//				
//			}
//		}
//		else if (method.equals("bfs")==true)
//			this.search_bfs(lpnList, initStateArray);
//		else if (method == "dfs_noDisabling")
//			//this.search_dfs_noDisabling(lpnList, initStateArray);
//			this.search_dfs_noDisabling_fireOrder(lpnList, initStateArray);
	}

	/**
	 * Recursively find all reachable project states.
	 */
	int iterations = 0;
	int stack_depth = 0;
	int max_stack_depth = 0;

	public void search_recursive(final StateGraph[] lpnList,
			final State[] curPrjState,
			final ArrayList<LinkedList<Transition>> enabledList,
			HashSet<PrjState> stateTrace) {		
		int lpnCnt = lpnList.length;
		HashSet<PrjState> prjStateSet = new HashSet<PrjState>();

		stack_depth++;
		if (stack_depth > max_stack_depth)
			max_stack_depth = stack_depth;

		iterations++;
		if (iterations % 50000 == 0)
			System.out.println("iterations: " + iterations
					+ ", # of prjStates found: " + prjStateSet.size()
					+ ", max_stack_depth: " + max_stack_depth);

		for (int index = 0; index < lpnCnt; index++) {
			LinkedList<Transition> curEnabledSet = enabledList.get(index);

			if (curEnabledSet == null)
				continue;

			for (Transition firedTran : curEnabledSet) {
				// while(curEnabledSet.size() != 0) {
				// LPNTran firedTran = curEnabledSet.removeFirst();
				
				// TODO: (check) Not sure if lpnList[index] is correct
				State[] nextStateArray = lpnList[index].fire(lpnList, curPrjState, firedTran);

				// Add nextPrjState into prjStateSet
				// If nextPrjState has been traversed before, skip to the next
				// enabled transition.
				PrjState nextPrjState = new PrjState(nextStateArray);

				//if (stateTrace.contains(nextPrjState) == true)
				//	System.out.println("found a cycle");

				if (prjStateSet.add(nextPrjState) == false) {
					continue;
				}

				// Get the list of enabled transition sets, and call
				// findsg_recursive.
				ArrayList<LinkedList<Transition>> nextEnabledList = new ArrayList<LinkedList<Transition>>();
				for (int i = 0; i < lpnCnt; i++) {
					if (curPrjState[i] != nextStateArray[i]) {
						StateGraph Lpn_tmp = lpnList[i];
						nextEnabledList.add(i, Lpn_tmp.getEnabled(nextStateArray[i]));// firedTran,
																		// enabledList.get(i),
																		// false));
					} else {
						nextEnabledList.add(i, enabledList.get(i));
					}
				}

				stateTrace.add(nextPrjState);
				search_recursive(lpnList, nextStateArray, nextEnabledList,
						stateTrace);
				stateTrace.remove(nextPrjState);
			}
		}
	}
	

	/**
	 * An iterative implement of findsg_recursive().
	 * 
	 * @param sgList
	 * @param start 
	 * @param curLocalStateArray
	 * @param enabledArray
	 */		
	public StateSetInterface search_dfs(final StateGraph[] sgList, final State[] initStateArray) {
		System.out.println("-------- Reachability Analysis ---------");
		System.out.println("---> calling function search_dfs");
				
		double peakUsedMem = 0;
		double peakTotalMem = 0;
		boolean failure = false;
		int tranFiringCnt = 0;
		int totalStates = 1;
		int numLpns = sgList.length;
		Transition firedFailure = null;
		
		//Stack<State[]> stateStack = new Stack<State[]>();
		HashSet<PrjState> stateStack = new HashSet<PrjState>();
		Stack<LinkedList<Transition>> lpnTranStack = new Stack<LinkedList<Transition>>();		
		Stack<Integer> curIndexStack = new Stack<Integer>();
		//HashSet<PrjState> prjStateSet = new HashSet<PrjState>();
		
		// Set of PrjStates that have been seen before. Set class documentation
		// for how it behaves. Timing Change.
//		HashMap<PrjState, PrjState> prjStateSet = generateStateSet();
		StateSetInterface prjStateSet = generateStateSet();
		
		PrjState initPrjState;		
		// Create the appropriate type for the PrjState depending on whether timing is 
		// being used or not. Timing Change.
		if(!Options.getTimingAnalysisFlag()){
			// If not doing timing.
			if (!Options.getMarkovianModelFlag())				
				initPrjState = new PrjState(initStateArray);
			else
				initPrjState = new ProbGlobalState(initStateArray);
		}
		else{
			// If timing is enabled.
			initPrjState = new TimedPrjState(initStateArray);
			TimedPrjState.incTSCount();
			
			// Set the initial values of the inequality variables.
			//((TimedPrjState) initPrjState).updateInequalityVariables();
		}
		prjStateSet.add(initPrjState);
		if(Options.getMarkovianModelFlag()) {	
			((ProbGlobalStateSet) prjStateSet).setInitState(initPrjState);		
		}
		PrjState stateStackTop;		
		stateStackTop = initPrjState;
		if (Options.getDebugMode()) 
			printStateArray(stateStackTop.toStateArray(), "~~~~ stateStackTop ~~~~");		
		stateStack.add(stateStackTop);		
		constructDstLpnList(sgList);
		if (Options.getDebugMode())
			printDstLpnList(sgList);		
		LpnTranList initEnabled;
		if(!Options.getTimingAnalysisFlag()){ // Timing Change.
			initEnabled = StateGraph.getEnabledFromTranVector(initStateArray[0]);
		}
		else
		{
			// When timing is enabled, it is the project state that will determine
			// what is enabled since it contains the zone. This indicates the zeroth zone
			// contained in the project and the zeroth LPN to get the transitions from.
			initEnabled = ((TimedPrjState) stateStackTop).getPossibleEvents(0, 0);
		}
		//lpnTranStack.push(initEnabled.clone());
		lpnTranStack.push(initEnabled);
		curIndexStack.push(0);		
		if (Options.getDebugMode()) {			
			System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
			printTransList(initEnabled, "initEnabled");
		}		
		
		main_while_loop: while (failure == false && stateStack.size() != 0) {
			if (Options.getDebugMode()) {				
				System.out.println("~~~~~~~~~~~ loop begins ~~~~~~~~~~~");
			}
				
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
			//if (failureTranIsEnabled(curEnabled)) {
			firedFailure = failureTranIsEnabled(curEnabled); // Null means no failures.
			if(firedFailure != null){
				failure = true;
				
				if(Options.getTimingAnalysisFlag()&& Options.getOutputSgFlag()){
					// Add the failure transition to the graph.
					
					TimedPrjState tps = (TimedPrjState) stateStackTop;
					
					// To have a target state, clone the last state.
					TimedPrjState lastState = new TimedPrjState(tps.getStateArray(), tps.get_zones());
					TimedPrjState.incTSCount();
					
					stateStackTop.addNextGlobalState(firedFailure, lastState);
				}
				
				break main_while_loop;
			}
			if (Options.getDebugMode()) {
				printStateArray(curStateArray, "------- curStateArray ----------");
				printTransList(curEnabled, "+++++++ curEnabled trans ++++++++");
			}
			// If all enabled transitions of the current LPN are considered,
			// then consider the next LPN
			// by increasing the curIndex.
			// Otherwise, if all enabled transitions of all LPNs are considered,
			// then pop the stacks.
			if (curEnabled.size() == 0) {
				lpnTranStack.pop();
				if (Options.getDebugMode()) {
					System.out.println("+++++++ Pop trans off lpnTranStack ++++++++");	
					printTranStack(lpnTranStack, "***** lpnTranStack *****");
				}
				curIndexStack.pop();
				curIndex++;
				while (curIndex < numLpns) {
//					System.out.println("call getEnabled on curStateArray at 1: ");
					if(!Options.getTimingAnalysisFlag()){ // Timing Change
						curEnabled = StateGraph.getEnabledFromTranVector(curStateArray[curIndex]).clone();
					}
					else{
						// Get the enabled transitions from the zone that are associated with
						// the current LPN.
						curEnabled = ((TimedPrjState) stateStackTop).getPossibleEvents(0, curIndex);
					}
					if (curEnabled.size() > 0) {
						if (Options.getDebugMode()) {							
							printTransList(curEnabled, "+++++++ Push trans onto lpnTranStack ++++++++");
						}
						lpnTranStack.push(curEnabled);
						curIndexStack.push(curIndex);
						break;
					} 					
					curIndex++;
				}
			}
			if (curIndex == numLpns) {
				if (Options.getDebugMode()) {					
					printStateArray(stateStackTop.toStateArray(), "~~~~ Remove stateStackTop from stateStack ~~~~");
				}
				stateStack.remove(stateStackTop);
				stateStackTop = stateStackTop.getFather();
				continue;
			}
			Transition firedTran = curEnabled.removeLast();	
			if (Options.getDebugMode()) {
				System.out.println("###################");			
				System.out.println("Fired transition: " + firedTran.getFullLabel());
				System.out.println("###################");
			}
			
			State[] nextStateArray;
			PrjState nextPrjState; // Moved this definition up. Timing Change.
			
			// The next state depends on whether timing is in use or not. 
			// Timing Change.
			if(!Options.getTimingAnalysisFlag()){
				nextStateArray = sgList[curIndex].fire(sgList, curStateArray, firedTran);
				if (!Options.getMarkovianModelFlag())
					nextPrjState = new PrjState(nextStateArray);
				else
					nextPrjState = new ProbGlobalState(nextStateArray);
			}
			else{
				// Get the next timed state and extract the next un-timed states.
				nextPrjState = sgList[curIndex].fire(sgList, stateStackTop,
						(EventSet) firedTran);
				nextStateArray = nextPrjState.toStateArray();
			}
			tranFiringCnt++;

			// Check if the firedTran causes disabling error or deadlock.
			//LinkedList<Transition>[] curEnabledArray = new LinkedList[numLpns];
			//LinkedList<Transition>[] nextEnabledArray = new LinkedList[numLpns];
			//LinkedList<Transition>[] curEnabledArray = new LinkedList[numLpns];			
			//LinkedList<Transition>[] nextEnabledArray = new LinkedList[numLpns];
			List<LinkedList<Transition>> curEnabledArray = new ArrayList<LinkedList<Transition>>(); 
			List<LinkedList<Transition>> nextEnabledArray = new ArrayList<LinkedList<Transition>>(); 
			for (int i = 0; i < numLpns; i++) {
				LinkedList<Transition> enabledList;
				if(!Options.getTimingAnalysisFlag()){ // Timing Change.
					enabledList = StateGraph.getEnabledFromTranVector(curStateArray[i]);
				}
				else{
					// Get the enabled transitions from the Zone for the appropriate
					// LPN.
					//enabledList = ((TimedPrjState) stateStackTop).getEnabled(i);
					enabledList = ((TimedPrjState) stateStackTop).getPossibleEvents(0, i);
				}
				curEnabledArray.add(i,enabledList);
				if(!Options.getTimingAnalysisFlag()){ // Timing Change.
					enabledList = StateGraph.getEnabledFromTranVector(nextStateArray[i]);
				}
				else{
					//enabledList = ((TimedPrjState) nextPrjState).getEnabled(i);
					enabledList = ((TimedPrjState) nextPrjState).getPossibleEvents(0, i);
				}
				nextEnabledArray.add(i,enabledList);
				// TODO: (temp) Stochastic model does not need disabling error?
				if (Options.getReportDisablingError() && !Options.getMarkovianModelFlag()) {
					Transition disabledTran = firedTran.disablingError(
							curEnabledArray.get(i), nextEnabledArray.get(i));
					if (disabledTran != null) {
						System.err.println("Disabling Error: "
								+ disabledTran.getFullLabel() + " is disabled by "
								+ firedTran.getFullLabel());
						failure = true;
						break main_while_loop;
					}
				}
			}

			if(!Options.getTimingAnalysisFlag()){
				if (Analysis.deadLock(sgList, nextStateArray) == true) {
					System.out.println("*** Verification failed: deadlock.");
					failure = true;
					break main_while_loop;
				}
			}
			else{
				if (Analysis.deadLock(nextEnabledArray)){
					System.out.println("*** Verification failed: deadlock.");
					failure = true;
					if(Options.get_displayResults()){
					  message.setErrorDialog("Error",   "The system deadlocked.");
					  this.notifyObservers(message);
					}
					break main_while_loop;
				}
			}

			// Build transition rate map on global state.
			if (Options.getMarkovianModelFlag()) {
				for (State localSt : stateStackTop.toStateArray()) {					
					for (Transition t : localSt.getEnabledTransitions()) {
						double tranRate = ((ProbLocalStateGraph) localSt.getLpn().getStateGraph()).getTranRate(localSt, t);
						((ProbGlobalState) stateStackTop).addNextGlobalTranRate(t, tranRate);
					}
				}
			}
			
			//PrjState nextPrjState = new PrjState(nextStateArray); // Moved earlier. Timing Change.
			boolean	existingState;
			existingState = prjStateSet.contains(nextPrjState); //|| stateStack.contains(nextPrjState);
			//existingState = prjStateSet.keySet().contains(nextPrjState); //|| stateStack.contains(nextPrjState);
			if (existingState == false) {
				prjStateSet.add(nextPrjState);
				//prjStateSet.put(nextPrjState,nextPrjState);
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				if (Options.getTimingAnalysisFlag()){
					// Assign a new id.
					TimedPrjState tpState = (TimedPrjState) nextPrjState;
					tpState.setCurrentId();
					TimedPrjState.incTSCount();
					
					if(Options.getOutputSgFlag()){
						// Add the current state as a previous state for the next state.
						if(Zone.getSupersetFlag()){
							tpState.addPreviousState((EventSet) firedTran, (TimedPrjState) stateStackTop);
						}
					}
				}
				
				if (Options.getMarkovianModelFlag() || Options.getOutputSgFlag()) {
					stateStackTop.addNextGlobalState(firedTran, nextPrjState);
				}
//				else {
//					if (Options.getDebugMode()) {
//						System.out.println("******* curStateArray *******");
//						printStateArray(curStateArray);
//						System.out.println("******* nextStateArray *******");
//						printStateArray(nextStateArray);			
//						System.out.println("stateStackTop: ");
//						printStateArray(stateStackTop.toStateArray());
//						System.out.println("firedTran = " + firedTran.getName());
//						System.out.println("***nextStateMap for stateStackTop before firedTran being added: ");
//						printNextStateMap(stateStackTop.getNextStateMap());
//					}
//				}
				stateStackTop = nextPrjState;
				stateStack.add(stateStackTop);
				lpnTranStack.push((LpnTranList) nextEnabledArray.get(0).clone());
				curIndexStack.push(0);
				totalStates++;
				if (Options.getDebugMode()) {										
					printStateArray(stateStackTop.toStateArray(), "~~~~~~~ Add global state to stateStack ~~~~~~~");
					System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
					printTransList(nextEnabledArray.get(0), "");
					printTranStack(lpnTranStack, "******** lpnTranStack ***********");
				}
			}
			else { // existingState == true
				if (Options.getMarkovianModelFlag()) {
					PrjState nextPrjStInStateSet = ((ProbGlobalStateSet) prjStateSet).get(nextPrjState);
					stateStackTop.addNextGlobalState(firedTran, nextPrjStInStateSet);					
				}
				else if (!Options.getMarkovianModelFlag() && Options.getOutputSgFlag()) { // non-stochastic model, but need to draw global state graph.
					for (PrjState prjSt : prjStateSet) {
						if(Options.getTimingAnalysisFlag() && Zone.getSubsetFlag()){
							
							TimedPrjState nextTimed = (TimedPrjState) nextPrjState;
							
							if(nextTimed.subset((TimedPrjState) prjSt)){

								// If the subset flag is in effect, then a set can be considered
								// 'previously seen' if the newly produced state is a subset of
								// of the previous set and not just equal. In addition, we cannot
								// break, since the current state could be a subset of more than
								// one state.
								stateStackTop.addNextGlobalState(firedTran, prjSt);

								if(Zone.getSupersetFlag()){
									// If supersets are in effect, add the previous states.
									TimedPrjState tps = (TimedPrjState) prjSt;
									tps.addPreviousState((EventSet) firedTran, (TimedPrjState) stateStackTop);
								}
							}
						}
						else if (prjSt.equals(nextPrjState)) {
							stateStackTop.addNextGlobalState(firedTran, prjSt);
							break;
						}
					}
				}
				else {
					if (Options.getOutputSgFlag()) {
						if (Options.getDebugMode()) {						
							printStateArray(curStateArray, "******* curStateArray *******");
							printStateArray(nextStateArray, "******* nextStateArray *******");						
							printStateArray(stateStackTop.toStateArray(), "stateStackTop: ");
							System.out.println("firedTran = " + firedTran.getFullLabel());
//							System.out.println("nextStateMap for stateStackTop before firedTran being added: ");
//							printNextGlobalStateMap(stateStackTop.getNextStateMap());
						}
					}
				}			
			}
		}
		double totalStateCnt =0;
		totalStateCnt = prjStateSet.size();
		System.out.println("---> final numbers: # LPN transition firings: "	+ tranFiringCnt 
			+ ", # of prjStates found: " + totalStateCnt			
			+ ", max_stack_depth: " + max_stack_depth 
			+ ", peak total memory: " + peakTotalMem / 1000000 + " MB"
			+ ", peak used memory: " + peakUsedMem / 1000000 + " MB");
		if(Options.getTimingAnalysisFlag()){// && !failure){
			if(!failure){
				if(Options.get_displayResults()){
				  message.setErrorDialog("Success", "Verification was successful.");
				  this.notifyObservers(message);
				}
				System.out.println("Verification was successful");
			}
			else{
				System.out.println("************ System failed. ***********");
				if(firedFailure != null){
					if(Options.get_displayResults()){

	          message.setErrorDialog("Error", "Failure transition " + firedFailure.getLabel() + " is enabled.");
	          this.notifyObservers(message);
					}
					System.out.println("The failure transition" + firedFailure.getLabel() + "fired.");
				}
				else{
					if(Options.get_displayResults()){

            message.setErrorDialog("Error",    "System failed for reason other\nthan a failure transition.");
            this.notifyObservers(message);
					}
				}
			}
			System.out.println(prjStateSet.toString());
			
			
		}
		if (Options.getOutputLogFlag()) 
			writePerformanceResultsToLogFile(false, tranFiringCnt, totalStateCnt, peakTotalMem / 1000000, peakUsedMem / 1000000);
		if (Options.getOutputSgFlag()) {
			System.out.println("outputSGPath = "  + Options.getPrjSgPath());
			
			//drawGlobalStateGraph(sgList, prjStateSet.toHashSet(), true);
			//drawReducedStateGraph(initPrjState);
			drawGlobalStateGraph(initPrjState, prjStateSet);			
		}
//		// ---- TEMP ----
//		try{
//			File stateFile = new File(Options.getPrjSgPath() + Options.getLogName() + ".txt");
//			FileWriter fileWritter = new FileWriter(stateFile,true);
//			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
//			// TODO: Need to merge variable vectors from different local states.
//			ArrayList<Integer> vector;			
//			String curPrjStInfo = "";
//			for (PrjState prjSt : prjStateSet) {
//				curPrjStInfo += "--- Begin ---\n";
//				// marking
//				curPrjStInfo += "marking: ";
//				for (State localSt : prjSt.toStateArray()) 
//					curPrjStInfo += intArrayToString("markings", localSt) + "\n";
//				// variable vector
//				curPrjStInfo += "var values: ";
//				for (State localSt : prjSt.toStateArray()) {
//					localSt.getLpn().getAllVarsWithValuesAsInt(localSt.getVariableVector());
//					//curPrjStInfo += intArrayToString("vars", localSt)+ "\n";
//				}
//					
//				// tranVector
//				curPrjStInfo += "tran vector: ";
//				for (State localSt : prjSt.toStateArray())
//					curPrjStInfo += boolArrayToString("enabled trans", localSt)+ "\n";
//				curPrjStInfo += "--- End ---\n";
//				bufferWritter.write(curPrjStInfo);
//				//bufferWritter.flush();
//			}
//			bufferWritter.close();
//			System.out.println("Done writing state file.");
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//		//---------------		
		return prjStateSet;
	}
	
//	private boolean failureCheck(LinkedList<Transition> curEnabled) {
//		boolean failureTranIsEnabled = false;
//		for (Transition tran : curEnabled) {
//			if (tran.isFail()) {
//				JOptionPane.showMessageDialog(Gui.frame,
//						"Failure transition " + tran.getLabel() + " is enabled.", "Error",
//						JOptionPane.ERROR_MESSAGE);
//				failureTranIsEnabled = true;
//				break;
//			}
//		}
//		return failureTranIsEnabled;
//	}

//	/**
//	 * Generates the appropriate version of a HashSet<PrjState> for storing
//	 * the "already seen" set of project states.
//	 * @return
//	 * 		Returns a HashSet<PrjState>, a StateSet, or a ProbGlobalStateSet
//	 * 				depending on the type.
//	 */
//	private HashSet<PrjState> generateStateSet(){
//		
//		boolean timed = Options.getTimingAnalysisFlag();
//		boolean subsets = Zone.getSubsetFlag();
//		boolean supersets = Zone.getSupersetFlag();
//		
//		if(Options.getMarkovianModelFlag()){
//			return new ProbGlobalStateSet();
//		}
//		else if(timed && (subsets || supersets)){
//			return new TimedStateSet();
//		}
//		
//		return new HashSet<PrjState>();
//	}
	
	/**
	 * Generates the appropriate version of a HashSet<PrjState> for storing
	 * the "already seen" set of project states.
	 * @return
	 * 		Returns a HashSet<PrjState>, a StateSet, or a ProbGlobalStateSet
	 * 				depending on the type.
	 */
	private static StateSetInterface generateStateSet(){
		
		boolean timed = Options.getTimingAnalysisFlag();
		boolean subsets = Zone.getSubsetFlag();
		boolean supersets = Zone.getSupersetFlag();
		
		if(Options.getMarkovianModelFlag()){
			return new ProbGlobalStateSet();
		}
		else if(timed && (subsets || supersets)){
			return new TimedStateSet();
		}
		
		return new HashSetWrapper();
	}
	
	private static Transition failureTranIsEnabled(LinkedList<Transition> enabledTrans) {
		Transition failure = null;
		for (Transition tran : enabledTrans) {
			if (tran.isFail()) {
				
				if(Zone.get_writeLogFile() != null){
					try {
						Zone.get_writeLogFile().write(tran.getLabel());
						Zone.get_writeLogFile().newLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
//				
//				JOptionPane.showMessageDialog(Gui.frame,
//						"Failure transition " + tran.getLabel() + " is enabled.", "Error",
//						JOptionPane.ERROR_MESSAGE);
				return tran;
			}
		}
		return failure;
	}

	public static class ReducedStateGraph {
		
		private PrjState initState;
		private HashSet <PrjState> stateSet; // list of all states of given state graph
		private HashMap <PrjState, HashSet <PrjState>> statePreSet; // input transitions for each state
		private HashMap <PrjState, HashSet <PrjState>> statePostSet; // output transitions for each state
		private HashMap <PrjState, HashMap<String, Integer>> stateVars; // list of care variables for each state
		private HashSet<String> careVars; // list of care variables
		
		ReducedStateGraph (PrjState initGlobalState) {
			try {
				// Initialize class variables
				stateSet = new HashSet<PrjState>();
				statePreSet = new HashMap<PrjState, HashSet<PrjState>>();
				statePostSet = new HashMap<PrjState, HashSet<PrjState>>();
				stateVars = new HashMap<PrjState, HashMap<String,Integer>>();
				careVars = new HashSet<String>();
				
				// Values for c-element example 
				//careVars.add("A");
				//careVars.add("B");
				//careVars.add("C");
				
				careVars.add("gp");
				careVars.add("gn");
				careVars.add("gp_ack");
				careVars.add("gn_ack");
				careVars.add("oc");
				careVars.add("uv");

				
				HashSet <PrjState> reducedStateSet =  new HashSet<PrjState>(); // list of care states
				
				// Add initial state
				stateSet.add(initGlobalState);
				reducedStateSet.add(initGlobalState);
				initState = initGlobalState;
				statePreSet.put(initGlobalState, new HashSet<PrjState> ());
				statePostSet.put(initGlobalState, new HashSet<PrjState> ());
				
				HashSet<PrjState> visited = new HashSet<PrjState> ();
				visited.add(initGlobalState);
			
				Queue<PrjState> stateQueue = new LinkedList<PrjState> ();
				stateQueue.add(initGlobalState);
				
				// Traverse graph via bfs and mark noncare states 
				while (!stateQueue.isEmpty()) {
					PrjState curState = stateQueue.remove();
					HashMap<String, Integer> curStateVars = getCareVars(curState);
					stateVars.put(curState, curStateVars);
					
					for (Transition outTran : curState.getNextGlobalStateMap().keySet()) {
						PrjState nextState = curState.getNextGlobalStateMap().get(outTran);
						HashMap<String, Integer> nextStateVars = getCareVars(nextState);
						stateVars.put(nextState, nextStateVars);
						
						if (!visited.contains(nextState)) {
							visited.add(nextState);
		                    stateQueue.add(nextState);
		    				statePreSet.put(nextState, new HashSet<PrjState> ());
		    				statePostSet.put(nextState, new HashSet<PrjState> ());
						} 
					
						// Check if care variables in the next state are equal to those in the current state
						if (!stateVars.get(curState).equals(stateVars.get(nextState))) 
							reducedStateSet.add(nextState);
						
						statePreSet.get(nextState).add(curState);
						statePostSet.get(curState).add(nextState);
						stateSet.add(nextState);
						
					}
				}
				
				
				Iterator<PrjState> nextStateIter = stateSet.iterator();
				
				// Remove unmarked states and update transitions
				while(nextStateIter.hasNext()) {
					PrjState nextState =  nextStateIter.next();
					
					if (!reducedStateSet.contains(nextState)) {
						//stateSet.remove(nextState);
						
						// Update preset states
						Iterator<PrjState> preSetStateIter = statePreSet.get(nextState).iterator();
						while(preSetStateIter.hasNext()) {
							PrjState preSetState =  preSetStateIter.next();
							statePostSet.get(preSetState).addAll(statePostSet.get(nextState));
						}
						
						// Update post states
						Iterator<PrjState> postSetStateIter = statePostSet.get(nextState).iterator();
						while(postSetStateIter.hasNext()) {
							PrjState postSetState =  postSetStateIter.next();
							statePreSet.get(postSetState).addAll(statePreSet.get(nextState));
						}
						
					}
				}
				
				stateSet = reducedStateSet;
				
				HashMap<PrjState, HashSet<PrjState>> statePreSetTemp = new HashMap<PrjState, HashSet<PrjState>>();
				HashMap<PrjState, HashSet<PrjState>> statePostSetTemp = new HashMap<PrjState, HashSet<PrjState>>();
				
				// Remove transitions to unmarked states 
				nextStateIter = stateSet.iterator();
				while(nextStateIter.hasNext()) {
					PrjState nextState =  nextStateIter.next();
					statePreSetTemp.put(nextState, new HashSet<PrjState>());
					statePostSetTemp.put(nextState, new HashSet<PrjState>());
					
					// Update preset states
					Iterator<PrjState> preSetStateIter = statePreSet.get(nextState).iterator();
					while(preSetStateIter.hasNext()) {
						PrjState preSetState =  preSetStateIter.next();
						if (stateSet.contains(preSetState))
							statePreSetTemp.get(nextState).add(preSetState);
					}
					
					// Update post states
					Iterator<PrjState> postSetStateIter = statePostSet.get(nextState).iterator();
					while(postSetStateIter.hasNext()) {
						PrjState postSetState =  postSetStateIter.next();
						if (stateSet.contains(postSetState))
							statePostSetTemp.get(nextState).add(postSetState);
					}
				}
				
				statePostSet = statePostSetTemp;
				statePreSet = statePreSetTemp;
				
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error creating reduced state graph.");
			}

		}
		
		private void mergeDublicates() {
			try {
				boolean reductionFound = true;
				
				while (reductionFound) {
					reductionFound = false;
					
					Iterator<PrjState> curStateIter = stateSet.iterator();
					PrjState preSetState1 = new PrjState();
					PrjState preSetState2 = new PrjState();
					
					// Iterate over all states to identify states with same coding in the preset
					while(curStateIter.hasNext() && !reductionFound) {
						PrjState nextState =  curStateIter.next();
							
						// TODO: use hash to identify same vector values
						Iterator<PrjState> preSetStateIter1 = statePreSet.get(nextState).iterator();
						while(preSetStateIter1.hasNext() && !reductionFound) {
							preSetState1 =  preSetStateIter1.next();
							
							Iterator<PrjState> preSetStateIter2 = statePreSet.get(nextState).iterator();
							while(preSetStateIter2.hasNext() && !reductionFound) {
								preSetState2 =  preSetStateIter2.next();
								
								// TODO: think on checking similar postsets for states with similar encoding
								// It is possible there different paths in the graph
								if (stateVars.get(preSetState1).equals(stateVars.get(preSetState2)) && !preSetState1.equals(initState) && !preSetState2.equals(initState) 
										&& !preSetState1.equals(preSetState2))
										reductionFound = true;
									
							}
						}
					}
					
					// If reduction found merge two states and start over
					if (reductionFound)
						mergeStates(preSetState1, preSetState2);
					
				}
				
				// Remove self loops
				Iterator<PrjState> curStateIter = stateSet.iterator();
				
				// Iterate over all states to identify states with same coding in the preset
				while(curStateIter.hasNext()) {
					PrjState nextState =  curStateIter.next();
					
					statePreSet.get(nextState).remove(nextState);
					statePostSet.get(nextState).remove(nextState);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error removing dublicates from reduced state graph.");
			}
		}
		
		private void mergeStates(PrjState stateA, PrjState stateB) {
			try {
				
				// Copy preset states
				HashSet<PrjState> statePreSetLocal = statePreSet.get(stateB);
				statePreSetLocal.addAll(statePreSet.get(stateA));
				statePreSet.put(stateA, statePreSetLocal);
				
				// Update postset of all connected states
				Iterator<PrjState> preSetStateIter = statePreSet.get(stateA).iterator();
				while(preSetStateIter.hasNext()) {
					PrjState preSetState = preSetStateIter.next();
					// To avoid selfloops
					if (!preSetState.equals(stateA))
						statePostSet.get(preSetState).add(stateA);
					statePostSet.get(preSetState).remove(stateB);
				}
				
				// Copy post states
				HashSet<PrjState> statePostSetLocal = statePostSet.get(stateB);
				statePostSetLocal.addAll(statePostSet.get(stateA));
				statePostSet.put(stateA, statePostSetLocal);
				
				// Update pretset of all connected states
				Iterator<PrjState> postSetStateIter = statePostSet.get(stateA).iterator();
				while(postSetStateIter.hasNext()) {
					PrjState postSetState = postSetStateIter.next();
					// To avoid selfloops
					if (!postSetState.equals(stateA))
						statePreSet.get(postSetState).add(stateA);
					statePreSet.get(postSetState).remove(stateB);
				}
				
				stateSet.remove(stateB);
				statePreSet.remove(stateB);
				statePostSet.remove(stateB);
				
			}  catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error merging states in reduced state graph.");
			}
			
		}
		
		private HashMap<String, Integer> getCareVars(PrjState globalState)	{
			HashMap<String, Integer> vars = new HashMap<String, Integer>();				
			for (State curLocalState : globalState.toStateArray()) {
				LPN curLpn = curLocalState.getLpn();
				for(int i = 0; i < curLpn.getVarIndexMap().size(); i++) {						
					if (careVars.contains(curLpn.getVarIndexMap().getKey(i))) 
						vars.put(curLpn.getVarIndexMap().getKey(i), curLocalState.getVariableVector()[i]);
				}
			}
			
			return vars;
		}
		
		private String getLabel(PrjState stateA, PrjState stateB) {
			String label = null;
			
			try {
				HashMap<String, Integer> stateAVars = stateVars.get(stateA);
				HashMap<String, Integer> stateBVars = stateVars.get(stateB);
				
				Iterator<String> curVarIter = careVars.iterator();
				while (curVarIter.hasNext()) {
					String varName = curVarIter.next();
					
					if (stateAVars.get(varName) > stateBVars.get(varName)) {
						if (label == null)
							label = varName + "-";
						else {
							label = "Error";
							break;
						}
					}
					
					if (stateAVars.get(varName) < stateBVars.get(varName)) {
						if (label == null)
							label = varName + "+";
						else {
							label = "Error";
							break;
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error getting arc label in reduced state graph.");
			}
			return label;
		}
		
		public void drawPetrifySG(String fileName) {
			try {
				String fileNameLocal = null;
				fileNameLocal = Options.getPrjSgPath() + fileName;
				
				BufferedWriter out = new BufferedWriter(new FileWriter(fileNameLocal));
				out.write("# Reduced state graph, generated by LEMA\n");
				out.write(".inputs" + careVars.toString() + "\n");
				out.write(".state graph\n");
				
				for (PrjState curState : statePostSet.keySet()) {
					Iterator<PrjState> nextStateIter = statePostSet.get(curState).iterator();
					while(nextStateIter.hasNext()) {
						PrjState nextState =  nextStateIter.next();
						String arcLabel = getLabel(curState, nextState);
						out.write("TS_" + ((TimedPrjState) curState).getTSID() + " " + arcLabel + " " + "TS_" + ((TimedPrjState) nextState).getTSID() + "\n");
					}
				}
				
				
				out.write(".marking {" + "TS_" + ((TimedPrjState) initState).getTSID() + "}\n");
				out.write(".end");
				out.close();
				
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error producing petrify state graph file.");
			}
		}
		
		public void drawGraph(String fileName) {
			try {
				String fileNameLocal = null;
				fileNameLocal = Options.getPrjSgPath() + fileName;
				
				BufferedWriter out = new BufferedWriter(new FileWriter(fileNameLocal));
				out.write("digraph G {\n");
				out.write("node [shape=box, style=rounded]" + "\n");
				
				Iterator<PrjState> curStateIter = stateSet.iterator();
				while(curStateIter.hasNext()) {
					PrjState curState = curStateIter.next();
					if (curState.equals(initState))
						out.write("TS_" + ((TimedPrjState) curState).getTSID() + "[label=\"" + "TS_" + ((TimedPrjState) curState).getTSID()
								+ "\\n" + stateVars.get(curState) + "\", style=\"rounded, filled\"]" + "\n");
					else
						out.write("TS_" + ((TimedPrjState) curState).getTSID() + "[label=\"" + "TS_" + ((TimedPrjState) curState).getTSID()
								+ "\\n" + stateVars.get(curState) + "\"]" + "\n");

					Iterator<PrjState> nextStateIter = statePostSet.get(curState).iterator();
					while(nextStateIter.hasNext()) {
						PrjState nextState =  nextStateIter.next();
						String arcLabel = getLabel(curState, nextState);
						out.write("TS_" + ((TimedPrjState) curState).getTSID() + "->" + "TS_" + ((TimedPrjState) nextState).getTSID() +
								"[label=\""+ arcLabel + "\"]" +"\n");
					}
					
				}
				
				out.write("}");
				out.close();
				
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error producing reduced state graph as dot file.");
			}
		}
	}
	
	public static void drawReducedStateGraph(PrjState initGlobalState) {
		ReducedStateGraph reducedStateGraph = new ReducedStateGraph (initGlobalState);
		reducedStateGraph.drawGraph("Reduced.dot");
		reducedStateGraph.drawPetrifySG("Reduced.sg");
		reducedStateGraph.mergeDublicates();
		reducedStateGraph.drawGraph("Reduced_merged.dot");
		reducedStateGraph.drawPetrifySG("Reduced_merged.sg");
	}
	
	
	/**
	 * Produces DOT files for visualizing the global state graph. <p>
	 * This method assumes that the global state graph exists.
	 * @param initGlobalState
	 * @param globalStateSet
	 */
	public static void drawGlobalStateGraph(PrjState initGlobalState, StateSetInterface globalStateSet) {
		try {
			String fileName = null;
			if (Options.getPOR().toLowerCase().equals("off")) {
				fileName = Options.getPrjSgPath() + "full_sg.dot";
			}
			else {
				fileName = Options.getPrjSgPath() + Options.getPOR().toLowerCase() + "_"
						+ Options.getCycleClosingMthd().toLowerCase() + "_"
						+ Options.getCycleClosingStrongStubbornMethd().toLowerCase() + "_sg.dot";
			}			
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			NumberFormat num = NumberFormat.getNumberInstance();//NumberFormat.getInstance();
			num.setMaximumFractionDigits(6);
			num.setGroupingUsed(false);
			out.write("digraph G {\n");
			out.write("node [shape=box, style=rounded]");
			for (PrjState curGlobalState : globalStateSet) {
				// Build composite current global state.
				String curVarNames = "";
				String curVarValues = "";
				String curMarkings = "";
				String curEnabledTrans = "";
				String curGlobalStateLabel = "";
				String curGlobalStateProb = null;
				String DBM = "";
				HashMap<String, Integer> vars = new HashMap<String, Integer>();				
				for (State curLocalState : curGlobalState.toStateArray()) {
					curGlobalStateLabel = curGlobalStateLabel + "_" + "S" + curLocalState.getIndex();										
					LPN curLpn = curLocalState.getLpn();
					for(int i = 0; i < curLpn.getVarIndexMap().size(); i++) {						
						vars.put(curLpn.getVarIndexMap().getKey(i), curLocalState.getVariableVector()[i]);
					}
					curMarkings = curMarkings + "," + intArrayToString("markings", curLocalState);
					if (!boolArrayToString("enabled trans", curLocalState).equals(""))
						curEnabledTrans = curEnabledTrans + "," +  boolArrayToString("enabled trans", curLocalState);					
				}
				for (String vName : vars.keySet()) {
					Integer vValue = vars.get(vName);
					curVarValues = curVarValues + vValue + ", ";
					curVarNames = curVarNames + vName + ", ";
				}
				if (!curVarNames.isEmpty() && !curVarValues.isEmpty()) {
					curVarNames = curVarNames.substring(0, curVarNames.lastIndexOf(","));
					curVarValues = curVarValues.substring(0, curVarValues.lastIndexOf(","));
				}
				curMarkings = curMarkings.substring(curMarkings.indexOf(",")+1, curMarkings.length());
				curEnabledTrans = curEnabledTrans.substring(curEnabledTrans.indexOf(",")+1, curEnabledTrans.length());		
				if (Options.getMarkovianModelFlag()) {
					// State probability after steady state analysis.
					curGlobalStateProb = num.format(((ProbGlobalState) curGlobalState).getCurrentProb());
				}
				if(Options.getTimingAnalysisFlag()){
					TimedPrjState timedState = (TimedPrjState) curGlobalState;
					
					curGlobalStateLabel = "TS_" + timedState.getTSID();
					
					if(Options.get_displayDBM()){
						
						Equivalence[] dbm = timedState.get_zones();
						
						DBM = "\\n" + dbm[0].toString().replace("\n", "\\n") + "\\n";
					}
				}
				else{
					curGlobalStateLabel = curGlobalStateLabel.substring(curGlobalStateLabel.indexOf("_")+1, curGlobalStateLabel.length());
				}

				if (curGlobalState.equals(initGlobalState)) {
					String sgVector = ""; // sgVector is a vector of LPN labels. It shows the order of local state graph composition.
					for (State s : initGlobalState.toStateArray()) {
						sgVector = sgVector + s.getLpn().getLabel() + ", ";
					}					
					out.write("Inits[shape=plaintext, label=\"variable vector:<" + curVarNames + ">\\n" + "LPN vector:<" +sgVector.substring(0, sgVector.lastIndexOf(",")) + ">\"]\n");					
					if (!Options.getMarkovianModelFlag()) {
						out.write(curGlobalStateLabel + "[label=\"" + curGlobalStateLabel + "\\n<"+curVarValues+">" 
								+ "\\n<"+curEnabledTrans+">" + "\\n<"+curMarkings+">" + "\\n" + DBM + "\\n" + "\", style=\"rounded, filled\"]\n");
					}						
					else {
						out.write(curGlobalStateLabel + "[label=\"" + curGlobalStateLabel + "\\n<"+curVarValues+">" 
								+ "\\n<"+curEnabledTrans+">" + "\\n<"+curMarkings+">" + "\\nProb = " + curGlobalStateProb + "\", style=\"rounded, filled\"]\n");
					}											
				}
				else { // non-initial global state(s)
					if (!Options.getMarkovianModelFlag()) {
						out.write(curGlobalStateLabel + "[label=\"" + curGlobalStateLabel + "\\n<"+curVarValues+">" 
								+ "\\n<"+curEnabledTrans+">" + "\\n<"+curMarkings+">" + "\\n" + DBM + "\\n" + "\"]\n");
					}
					else {
						out.write(curGlobalStateLabel + "[label=\"" + curGlobalStateLabel + "\\n<"+curVarValues+">" 
								+ "\\n<"+curEnabledTrans+">" + "\\n<"+curMarkings+">" + "\\nProb = " + curGlobalStateProb + "\"]\n");
					}												
				}
				for (Transition outTran : curGlobalState.getNextGlobalStateMap().keySet()) {					
					PrjState nextGlobalState = curGlobalState.getNextGlobalStateMap().get(outTran);
					String nextGlobalStateLabel = "";
					if(Options.getTimingAnalysisFlag()){
						nextGlobalStateLabel = "TS_" + ((TimedPrjState)nextGlobalState).getTSID();
					}else{
						nextGlobalStateLabel = nextGlobalState.getLabel();
					}
					String outTranName = outTran.getLabel();
					if (!Options.getMarkovianModelFlag()) {
						if (outTran.isFail() && !outTran.isPersistent())
							out.write(curGlobalStateLabel + "->" + nextGlobalStateLabel + "[label=\"" + outTranName + "\", fontcolor=red]\n");
						else if (!outTran.isFail() && outTran.isPersistent()) {
//							out.write(curGlobalStateLabel + "[label=\"" + curGlobalStateLabel + "\\n<"+curVarValues+">" 
//										+ "\\n<"+curEnabledTrans+">" + "\\n<"+curMarkings+">" + "\"]\n");
							out.write(curGlobalStateLabel + "->" + nextGlobalStateLabel + "[label=\"" + outTranName + "\", fontcolor=blue]\n");
						}							
						else if (outTran.isFail() && outTran.isPersistent())
							out.write(curGlobalStateLabel + "->" + nextGlobalStateLabel + "[label=\"" + outTranName + "\", fontcolor=purple]\n");						
						else {
//							out.write(curGlobalStateLabel + "[label=\"" + curGlobalStateLabel + "\\n<"+curVarValues+">" 
//										+ "\\n<"+curEnabledTrans+">" + "\\n<"+curMarkings+">" + "\"]\n");	
							out.write(curGlobalStateLabel + "->" + nextGlobalStateLabel + "[label=\"" + outTranName + "\"]\n");								
						}
					}
					else { // stochastic global state graph
						//State localState = curGlobalState.toStateArray()[outTran.getLpn().getLpnIndex()];						
						//String outTranRate = num.format(((ProbLocalStateGraph) localState.getStateGraph()).getTranRate(localState, outTran));
						String outTranRate = num.format(((ProbGlobalState) curGlobalState).getOutgoingTranRate(outTran));
						
						if (outTran.isFail() && !outTran.isPersistent())
							out.write(curGlobalStateLabel + "->" + nextGlobalStateLabel 
									+ "[label=\"" + outTranName + "\\n" + outTranRate + "\", fontcolor=red]\n");
						else if (!outTran.isFail() && outTran.isPersistent())						
							out.write(curGlobalStateLabel + "->" + nextGlobalStateLabel 
									+ "[label=\"" + outTranName + "\\n" + outTranRate + "\", fontcolor=blue]\n");
						else if (outTran.isFail() && outTran.isPersistent())
							out.write(curGlobalStateLabel + "->" + nextGlobalStateLabel 
									+ "[label=\"" + outTranName + "\\n" + outTranRate + "\", fontcolor=purple]\n");						
						else
							out.write(curGlobalStateLabel + "->" + nextGlobalStateLabel 
									+ "[label=\"" + outTranName + "\\n" + outTranRate + "\"]\n");
					}
				}
			}
			out.write("}");
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error producing global state graph as dot file.");
		}	
	}
	
	private void drawDependencyGraphs(LPN[] lpnList) {
		String fileName = Options.getPrjSgPath() + separator + "dependencyGraph.dot";
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(fileName));
			out.write("digraph G {\n");			
			for (Transition curTran : staticDependency.keySet()) {
				String curTranStr = curTran.getLpn().getLabel() + "_" + curTran.getLabel();
				out.write(curTranStr + "[shape=\"box\"];");
				out.newLine();
			}
			for (Transition curTran : staticDependency.keySet()) {
				StaticDependencySets curStaticSets = staticDependency.get(curTran);
				String curTranStr = curTran.getLpn().getLabel() + "_" + curTran.getLabel();
				// TODO: getOtherTransDisableCurTranSet(false) or getOtherTransDisableCurTranSet(true)
				for (Transition curTranInDisable : curStaticSets.getOtherTransDisableSeedTran(false)) {
					String curTranInDisableStr = curTranInDisable.getLpn().getLabel() + "_" + curTranInDisable.getLabel();							
					out.write(curTranInDisableStr + "->" + curTranStr + "[color=\"chocolate\"];");
					out.newLine();
				}
//				for (Transition curTranInDisable : curStaticSets.getCurTranDisableOtherTransSet()) {
//					String curTranInDisableStr = lpnList[curTranInDisable.getLpnIndex()].getLabel() 
//							+ "_" + lpnList[curTranInDisable.getLpnIndex()].getTransition(curTranInDisable.getTranIndex()).getName();
//					out.write(curTranStr + "->" + curTranInDisableStr + "[color=\"chocolate\"];");	
//					out.newLine();				
//				}
//				for (Transition curTranInDisable : curStaticSets.getDisableSet()) {
//					String curTranInDisableStr = lpnList[curTranInDisable.getLpnIndex()].getLabel() 
//							+ "_" + lpnList[curTranInDisable.getLpnIndex()].getTransition(curTranInDisable.getTranIndex()).getName();
//					out.write(curTranStr + "->" + curTranInDisableStr + "[color=\"blue\"];");
//					out.newLine();
//				}
				HashSet<Transition> enableByBringingToken = new HashSet<Transition>();
				for (Place p : curTran.getPreset()) {
					for (Transition presetTran : p.getPreset()) {
						enableByBringingToken.add(presetTran);
					}
				}
				for (Transition curTranInCanEnable : enableByBringingToken) {
					String curTranInCanEnableStr = curTranInCanEnable.getLpn().getLabel() + "_" + curTranInCanEnable.getLabel(); 
					out.write(curTranStr + "->" + curTranInCanEnableStr + "[color=\"mediumaquamarine\"];");
					out.newLine();
				}
				
				for (HashSet<Transition> canEnableOneConjunctSet : curStaticSets.getOtherTransSetSeedTranEnablingTrue()) {
					for (Transition curTranInCanEnable : canEnableOneConjunctSet) {
						String curTranInCanEnableStr = curTranInCanEnable.getLpn().getLabel() + "_" + curTranInCanEnable.getLabel(); 
						out.write(curTranStr + "->" + curTranInCanEnableStr + "[color=\"deepskyblue\"];");
						out.newLine();
					}
				}
			}
			out.write("}");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}				
	}

	
	private static String intArrayToString(String type, State curState) {
		String arrayStr = "";
		if (type.equals("markings")) {
			for (int i=0; i< curState.getMarking().length; i++) {
				if (curState.getMarking()[i] == 1) {
					arrayStr = arrayStr + curState.getLpn().getPlaceList()[i] + ",";
				}
//				String tranName = curState.getLpn().getAllTransitions()[i].getName();
//				if (curState.getTranVector()[i])
//					System.out.println(tranName + " " + "Enabled");
//				else
//					System.out.println(tranName + " " + "Not Enabled");
			}
			if (arrayStr.contains(","))
				arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}				
		else if (type.equals("vars")) {
			for (int i=0; i< curState.getVariableVector().length; i++) {
				arrayStr = arrayStr + curState.getVariableVector()[i] + ",";
			}
			if (arrayStr.contains(","))
				arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}
		return arrayStr;
	}
	
	private static String boolArrayToString(String type, State curState) {
		String arrayStr = "";
		if (type.equals("enabled trans")) {
			for (int i=0; i< curState.getTranVector().length; i++) {
				if (curState.getTranVector()[i]) {
					//arrayStr = arrayStr + curState.getLpn().getAllTransitions()[i].getFullLabel() + ", ";
					arrayStr = arrayStr + curState.getLpn().getAllTransitions()[i].getLabel() + ", ";
				}
			}
			if (arrayStr != "")
				arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}
		return arrayStr;
	}

	private static void printStateArray(State[] stateArray, String title) {
		if (title != null)
			System.out.println(title);		
		for (int i=0; i<stateArray.length; i++) {
			System.out.println("S" + stateArray[i].getIndex() + "(" + stateArray[i].getLpn().getLabel() +"): ");
			System.out.println("\tmarkings: " + intArrayToString("markings", stateArray[i]));
			System.out.println("\tvar values: " + intArrayToString("vars", stateArray[i]));
			System.out.println("\tenabled trans: " + boolArrayToString("enabled trans", stateArray[i]));
		}
		System.out.println("----------------------------");
	}

	private static void printTransList(LinkedList<Transition> tranList, String title) {
		if (title != null && !title.equals(""))
			System.out.println("+++++++" + title + "+++++++");
		for (int i=0; i< tranList.size(); i++) 
			System.out.println(tranList.get(i).getFullLabel() + ", ");
		System.out.println("+++++++++++++");
	}

//	private void writeIntegerStackToDebugFile(Stack<Integer> curIndexStack, String title) {
//		if (title != null)
//			System.out.println(title);
//		for (int i=0; i < curIndexStack.size(); i++) {			
//			System.out.println(title + "[" + i + "]" + curIndexStack.get(i));
//		}
//		System.out.println("------------------");
//	}

	private static void printDstLpnList(StateGraph[] lpnList) {
		System.out.println("++++++ dstLpnList ++++++");
		for (int i=0; i<lpnList.length; i++) {
			LPN curLPN = lpnList[i].getLpn();
			System.out.println("LPN: " + curLPN.getLabel());
			Transition[] allTrans = curLPN.getAllTransitions(); 
			for (int j=0; j< allTrans.length; j++) {
				System.out.println(allTrans[j].getLabel() + ": ");
				for (int k=0; k< allTrans[j].getDstLpnList().size(); k++) {
					System.out.println(allTrans[j].getDstLpnList().get(k).getLabel() + ",");
				}
				System.out.print("\n");
			}
			System.out.println("----------------");
		}
		System.out.println("++++++++++++++++++++");
	}

	private static void constructDstLpnList(StateGraph[] sgList) {
		for (int i=0; i<sgList.length; i++) {
			LPN curLPN = sgList[i].getLpn();
			Transition[] allTrans = curLPN.getAllTransitions();
			for (int j=0; j<allTrans.length; j++) {
				Transition curTran = allTrans[j];
				for (int k=0; k<sgList.length; k++) {
					curTran.setDstLpnList(sgList[k].getLpn());
				}
								
			}
		}
	}
	
	/**
	 * This method performs first-depth search on an array of LPNs and applies partial order reduction technique with trace-back on LPNs. 
	 * @param sgList
	 * @param initStateArray
	 * @param cycleClosingMthdIndex 
	 * @return
	 */	
	public StateSetInterface searchPOR_taceback(final StateGraph[] sgList, final State[] initStateArray) {
		System.out.println("---> calling function searchPOR_traceback");
		System.out.println("---> " + Options.getPOR());
		System.out.println("---> " + Options.getCycleClosingMthd());
		System.out.println("---> " + Options.getCycleClosingStrongStubbornMethd());
		
		double peakUsedMem = 0;
		double peakTotalMem = 0;
		boolean failure = false;
		int tranFiringCnt = 0;
		int totalStates = 1;
		int numLpns = sgList.length;
		Transition firedFailure = null;
		
		LPN[] lpnList = new LPN[numLpns];
		for (int i=0; i<numLpns; i++) {
			lpnList[i] = sgList[i].getLpn();
		}
				
		HashSet<PrjState> stateStack = new HashSet<PrjState>();
		Stack<LinkedList<Transition>> lpnTranStack = new Stack<LinkedList<Transition>>();
		StateSetInterface prjStateSet = generateStateSet();
		PrjState initPrjState;
		// Create the appropriate type for the PrjState depending on whether timing is 
		// being used or not. 
		if (!Options.getMarkovianModelFlag()) {
			initPrjState = new PrjState(initStateArray);
		}			
		else {
			initPrjState = new ProbGlobalState(initStateArray);
		}			
		prjStateSet.add(initPrjState);
		if(Options.getMarkovianModelFlag()) {
			((ProbGlobalStateSet) prjStateSet).setInitState(initPrjState);
		}
		PrjState stateStackTop = initPrjState;
		if (Options.getDebugMode())			
			printStateArray(stateStackTop.toStateArray(), "%%%%%%% stateStackTop %%%%%%%%");
		stateStack.add(stateStackTop);
		constructDstLpnList(sgList);
		if (Options.getDebugMode())
			printDstLpnList(sgList);
		// Determine statistically the dependency relations between transitions. 
		HashMap<Integer, Transition[]> allTransitions = new HashMap<Integer, Transition[]>(lpnList.length); 
		//HashMap<Transition, StaticSets> staticSetsMap = new HashMap<Transition, StaticSets>();
		HashMap<Transition, Integer> allProcessTransInOneLpn = new HashMap<Transition, Integer>();
		HashMap<Transition, LpnProcess> allTransitionsToLpnProcesses = new HashMap<Transition, LpnProcess>();
		for (int lpnIndex=0; lpnIndex<lpnList.length; lpnIndex++) {
			allTransitions.put(lpnIndex, lpnList[lpnIndex].getAllTransitions());
			Abstraction abs = new Abstraction(lpnList[lpnIndex]);
			abs.decomposeLpnIntoProcesses();				 
			allProcessTransInOneLpn = abs.getTransWithProcIDs();
			HashMap<Integer, LpnProcess> processMapForOneLpn = new HashMap<Integer, LpnProcess>();
			for (Transition curTran: allProcessTransInOneLpn.keySet()) {
				Integer procId = allProcessTransInOneLpn.get(curTran);
				if (!processMapForOneLpn.containsKey(procId)) {
					LpnProcess newProcess = new LpnProcess(procId);
					newProcess.addTranToProcess(curTran);
					if (curTran.getPreset() != null) {
						if (newProcess.getStateMachineFlag() 
								&& ((curTran.getPreset().length > 1)
										|| (curTran.getPostset().length > 1))) {
							newProcess.setStateMachineFlag(false);
						}
						Place[] preset = curTran.getPreset();
						for (Place p : preset) {
							newProcess.addPlaceToProcess(p);
						}
					}
					processMapForOneLpn.put(procId, newProcess);
					allTransitionsToLpnProcesses.put(curTran, newProcess);
				}
				else {
					LpnProcess curProcess = processMapForOneLpn.get(procId);
					curProcess.addTranToProcess(curTran);
					if (curTran.getPreset() != null) {
						if (curProcess.getStateMachineFlag() 
								&& (curTran.getPreset().length > 1
										|| curTran.getPostset().length > 1)) {
							curProcess.setStateMachineFlag(false);
						}
						Place[] preset = curTran.getPreset();
						for (Place p : preset) {
							curProcess.addPlaceToProcess(p);
						}
					}
					allTransitionsToLpnProcesses.put(curTran, curProcess);
				}
			}
		}		
		HashMap<Transition, Integer> tranFiringFreq = null;
//		if (Options.getUseDependentQueue())
		tranFiringFreq = new HashMap<Transition, Integer>(allTransitions.keySet().size());
		// Build conjuncts for each transition's enabling condition first before dealing with dependency and enable sets.
		for (int lpnIndex=0; lpnIndex<lpnList.length; lpnIndex++) {
			for (Transition curTran: allTransitions.get(lpnIndex)) {
				if (curTran.getEnablingTree() != null)
					curTran.buildConjunctsOfEnabling(curTran.getEnablingTree());
			}
		}
		for (int lpnIndex=0; lpnIndex<lpnList.length; lpnIndex++) {
			if (Options.getDebugMode()) {
				System.out.println("=======LPN = " + lpnList[lpnIndex].getLabel() + "=======");
			}
			for (Transition seedTran: allTransitions.get(lpnIndex)) {
				StaticDependencySets seedStatic = null;
				if (!Options.getMarkovianModelFlag())
					seedStatic = new StaticDependencySets(seedTran, allTransitionsToLpnProcesses);
				else 
					seedStatic = new ProbStaticDependencySets(seedTran, allTransitionsToLpnProcesses);
				// Requires buildConjunctsOfEnabling(ExprTree) in Transition class to be called on all transitions here.
				seedStatic.buildOtherTransSetSeedTranEnablingTrue(); 
				seedStatic.buildSeedTranDisableOtherTrans();
				seedStatic.buildOtherTransDisableSeedTran();
				if (Options.getMarkovianModelFlag() && Options.getTranRatePorDef().toLowerCase().equals("full")) {					
					((ProbStaticDependencySets) seedStatic).buildSeedTranModifyOtherTransRatesSet();
					((ProbStaticDependencySets) seedStatic).buildOtherTransModifySeedTranRateSet();
				}
				staticDependency.put(seedTran, seedStatic);
				tranFiringFreq.put(seedTran, 0);
			}			
		}
		if (Options.getDebugMode()) {
			printStaticSetsMap(lpnList);
		}
		LpnTranList initStrongStubbornTrans = buildStrongStubbornSet(initStateArray, null, tranFiringFreq, sgList, lpnList, prjStateSet, stateStackTop, null);
		lpnTranStack.push(initStrongStubbornTrans);
		if (Options.getDebugMode()) {			
			printTransList(initStrongStubbornTrans, "+++++++ Push trans onto lpnTranStack @ 1++++++++");
			drawDependencyGraphs(lpnList);
		}		
		updateLocalStrongStubbornSetTbl(initStrongStubbornTrans, sgList, initStateArray);		
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
			if (Options.getDebugMode()) {
				System.out.println("~~~~~~~~~~~ loop " + iterations + " begins ~~~~~~~~~~~");
			}
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
			LinkedList<Transition> curStrongStubbornTrans = lpnTranStack.peek();
			firedFailure = failureTranIsEnabled(curStrongStubbornTrans); // Null means no failure.			
//			if(firedFailure != null){
//				return null;
//			}
			if(firedFailure != null){
				System.out.println("**** Failure transition " + firedFailure.getFullLabel() + " is enabled. Exit.");
				failure = true;				
				break main_while_loop;
			}
			
			if (curStrongStubbornTrans.size() == 0) {
				lpnTranStack.pop();
				prjStateSet.add(stateStackTop);
				stateStack.remove(stateStackTop);
				stateStackTop = stateStackTop.getFather();
				if (Options.getDebugMode()) {
					System.out.println("+++++++ Pop trans off lpnTranStack ++++++++");					
					printTranStack(lpnTranStack, "####### lpnTranStack #########");
										
//					System.out.println("####### prjStateSet #########");
//					printPrjStateSet(prjStateSet);	
				}
				continue;
			}

			Transition firedTran = curStrongStubbornTrans.removeLast();			
			if (Options.getDebugMode()) {
				System.out.println("#################################");
				System.out.println("Fired Transition: " + firedTran.getFullLabel());
				System.out.println("#################################");
			}
			Integer freq = tranFiringFreq.get(firedTran) + 1;
			tranFiringFreq.put(firedTran, freq);
//			if (Options.getDebugMode()) {
//				System.out.println("~~~~~~tranFiringFreq~~~~~~~");
//				//printHashMap(tranFiringFreq, sgList);
//			}
			State[] nextStateArray = sgList[firedTran.getLpn().getLpnIndex()].fire(sgList, curStateArray, firedTran);
			tranFiringCnt++;
			PrjState nextPrjState;
			if (!Options.getMarkovianModelFlag())
				nextPrjState = new PrjState(nextStateArray);
			else
				nextPrjState = new ProbGlobalState(nextStateArray);
			
			// Check if the firedTran causes disabling error or deadlock.
			// TODO: (temp) Stochastic model does not need disabling error?
			if (Options.getReportDisablingError() && !Options.getMarkovianModelFlag()) {
				for (int i=0; i<numLpns; i++) {
					Transition disabledTran = firedTran.disablingError(curStateArray[i].getEnabledTransitions(), nextStateArray[i].getEnabledTransitions());
					if (disabledTran != null) {
						System.err.println("Disabling Error: "
								+ disabledTran.getFullLabel() + " is disabled by "
								+ firedTran.getFullLabel());
						failure = true;
						break main_while_loop;
					}
				}
			}
			LpnTranList nextStrongStubbornTrans = new LpnTranList();
			nextStrongStubbornTrans = buildStrongStubbornSet(curStateArray, nextStateArray, tranFiringFreq, sgList, lpnList, prjStateSet, stateStackTop, firedTran);
			// check for possible deadlock
			if (nextStrongStubbornTrans.size() == 0) {
				System.out.println("*** Verification failed: deadlock.");
				failure = true;
				break main_while_loop;
			}
			// Build transition rate map on global state.
			if (Options.getMarkovianModelFlag()) {
				for (State localSt : stateStackTop.toStateArray()) {					
					for (Transition t : localSt.getEnabledTransitions()) {
						double tranRate = ((ProbLocalStateGraph) localSt.getLpn().getStateGraph()).getTranRate(localSt, t);
						((ProbGlobalState) stateStackTop).addNextGlobalTranRate(t, tranRate);
					}
				}
			}			
			
			// Moved earlier. 
//			PrjState nextPrjState;
//			if (!Options.getMarkovianModelFlag())
//				nextPrjState = new PrjState(nextStateArray);
//			else
//				nextPrjState = new ProbGlobalState(nextStateArray);
			boolean existingState;
			existingState = prjStateSet.contains(nextPrjState);//|| stateStack.contains(nextPrjState);						
			if (existingState == false) {				
				if (Options.getDebugMode()) {
					System.out.println("%%%%%%% existingSate == false %%%%%%%%");				
					printStateArray(curStateArray, "******* curStateArray *******");
					printStateArray(nextStateArray, "******* nextStateArray *******");
					printStateArray(stateStackTop.toStateArray(), "stateStackTop");
					System.out.println("firedTran = " + firedTran.getFullLabel());
//					System.out.println("***nextStateMap for stateStackTop before firedTran being added: ");
//					printNextGlobalStateMap(stateStackTop.getNextStateMap());
					System.out.println("-----------------------");
				}
				prjStateSet.add(nextPrjState);
				//updateLocalStrongStubbornSetTbl(nextStrongStubbornTrans, sgList, nextStateArray);
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				if (Options.getMarkovianModelFlag() || Options.getOutputSgFlag()) {
					stateStackTop.addNextGlobalState(firedTran, nextPrjState);
//					System.out.println("@1, added (" + firedTran.getFullLabel() + ", " + nextPrjState.getLabel() + ") to curGlobalState " + stateStackTop.getLabel());
				}
				stateStackTop = nextPrjState;
				stateStack.add(stateStackTop);
				lpnTranStack.push(nextStrongStubbornTrans.clone());
				updateLocalStrongStubbornSetTbl(nextStrongStubbornTrans, sgList, nextStateArray);
				totalStates++;
				if (Options.getDebugMode()) {
					printStateArray(stateStackTop.toStateArray(), "%%%%%%% Add global state to stateStack %%%%%%%%");					
					printTransList(nextStrongStubbornTrans, "+++++++ Push trans onto lpnTranStack @ 2++++++++");
				}
			}
			else {  // existingState = true
				PrjState nextPrjStInStateSet = null; 
				if (Options.getMarkovianModelFlag()) {
					nextPrjStInStateSet = ((ProbGlobalStateSet) prjStateSet).get(nextPrjState);
					stateStackTop.addNextGlobalState(firedTran, nextPrjStInStateSet);
//					System.out.println("@2, added (" + firedTran.getFullLabel() + ", " + nextPrjState.getLabel() + ") to curGlobalState " + stateStackTop.getLabel());
				}
				else if (!Options.getMarkovianModelFlag() && Options.getOutputSgFlag()) { // non-stochastic model, but need to draw global state graph.
					for (PrjState prjSt : prjStateSet) {
						if (prjSt.equals(nextPrjState)) {
							nextPrjStInStateSet = prjSt;
							break;
						}
					}
					stateStackTop.addNextGlobalState(firedTran, nextPrjStInStateSet);
//					System.out.println("@3, added (" + firedTran.getFullLabel() + ", " + nextPrjStInStateSet.getLabel() + ") to curGlobalState " + stateStackTop.getLabel());
					
				}
				else { // non-stochastic model, no need to draw global state graph, so no need to add the next global state to stateStackTop
					if (Options.getDebugMode()) {
						printStateArray(curStateArray, "******* curStateArray *******");
						printStateArray(nextStateArray, "******* nextStateArray *******");
						System.out.println("stateStackTop: ");
						printStateArray(stateStackTop.toStateArray(), "stateStackTop: ");
						System.out.println("firedTran = " + firedTran.getFullLabel());
						//						System.out.println("nextStateMap for stateStackTop before firedTran being added: ");
						//						printNextGlobalStateMap(stateStackTop.getNextStateMap());
						System.out.println("-----------------------");
					}											
				}
				if (!Options.getCycleClosingMthd().toLowerCase().equals("no_cycleclosing")) { // Cycle closing check					
					if (prjStateSet.contains(nextPrjState) && stateStack.contains(nextPrjState)) {
						if (Options.getDebugMode()) {
							System.out.println("---------- Cycle Closing -------");
							System.out.println("Global state " + printGlobalStateLabel(nextPrjState) + " has been seen before and is on state stack.");
						}							
						HashSet<Transition> nextStrongStubbornSet = new HashSet<Transition>();
						HashSet<Transition> curStrongStubbornSet = new HashSet<Transition>();
						for (Transition t : nextStrongStubbornTrans) 
							nextStrongStubbornSet.add(t);						
						for (State curSt : curStateArray) {
							if (curSt.getStateGraph().getEnabledSetTbl().get(curSt) != null) {
								curStrongStubbornSet.addAll(curSt.getStateGraph().getEnabledSetTbl().get(curSt));
							}
							
						}
						LpnTranList newCurStateStrongStubborn = new LpnTranList();
						newCurStateStrongStubborn = computeCycleClosingTrans(curStateArray, nextStateArray, 
																	tranFiringFreq, sgList, nextStrongStubbornSet, curStrongStubbornSet, firedTran);
						if (newCurStateStrongStubborn != null && !newCurStateStrongStubborn.isEmpty()) {
							if (Options.getDebugMode()) {
								printStateArray(stateStackTop.toStateArray(), "%%%%%%% Add state to stateStack %%%%%%%%");
								System.out.println("stateStackTop: ");
								printStateArray(stateStackTop.toStateArray(), "stateStackTop");
								System.out.println("nextStateMap for stateStackTop: ");
								//printNextGlobalStateMap(nextPrjState.getNextStateMap());
							}																
							lpnTranStack.peek().addAll(newCurStateStrongStubborn);
//							lpnTranStack.push(newNextPersistent);							
//							stateStackTop.setChild(nextPrjStInStateSet);
//							nextPrjStInStateSet.setFather(stateStackTop);
//							stateStackTop = nextPrjStInStateSet;
//							stateStack.add(stateStackTop);
							updateLocalStrongStubbornSetTbl(newCurStateStrongStubborn, sgList, nextStateArray);
							if (Options.getDebugMode()) {
								printTransList(newCurStateStrongStubborn, "+++++++ Push these trans onto lpnTranStack @ Cycle Closing ++++++++");								
								printTranStack(lpnTranStack, "******* lpnTranStack ***************");					
							}
						}
					}
				}
				else {
					updateLocalStrongStubbornSetTbl(nextStrongStubbornTrans, sgList, nextStateArray);
				}
			}
		}
		double totalStateCnt = prjStateSet.size();		
		System.out.println("---> final numbers: # LPN transition firings: "	+ tranFiringCnt 
				+ ", # of prjStates found: " + totalStateCnt 
				+ ", max_stack_depth: " + max_stack_depth 
				+ ", peak total memory: " + peakTotalMem / 1000000 + " MB"
				+ ", peak used memory: " + peakUsedMem / 1000000 + " MB");
		
		if (Options.getOutputLogFlag()) 
			writePerformanceResultsToLogFile(true, tranFiringCnt, totalStateCnt, peakTotalMem / 1000000, peakUsedMem / 1000000);
		if (Options.getOutputSgFlag()) {
			System.out.println("outputSGPath = "  + Options.getPrjSgPath());			//
			drawGlobalStateGraph(initPrjState, prjStateSet);
		}
		return prjStateSet;
	}

	/**
	 * Print the prjState's label. The label consists of full labels of each local state that composes it.
	 * @param prjState
	 * @return
	 */
	private static String printGlobalStateLabel(PrjState prjState) {
		String prjStateLabel = "";
		for (State local : prjState.toStateArray()) {
			prjStateLabel += local.getFullLabel() + "_";
		}		
		return prjStateLabel.substring(0, prjStateLabel.lastIndexOf("_"));
	}
	
	
	private static String printGlobalStateLabel(State[] nextStateArray) {
		String prjStateLabel = "";
		for (State local : nextStateArray) {
			prjStateLabel += local.getFullLabel() + "_";
		}		
		return prjStateLabel.substring(0, prjStateLabel.lastIndexOf("_"));
	}

	private void writePerformanceResultsToLogFile(boolean isPOR, int tranFiringCnt, double totalStateCnt,
			double peakTotalMem, double peakUsedMem) {
		try {
			String fileName = null;
			if (isPOR) {
				fileName = Options.getPrjSgPath() + separator + Options.getLogName() + "_" + Options.getPOR() + "_" 
					+ Options.getCycleClosingMthd() + "_" + Options.getCycleClosingStrongStubbornMethd() + ".log";
			}				
			else
				fileName = Options.getPrjSgPath() + Options.getLogName() + "_full" + ".log";
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write("tranFiringCnt" + "\t" + "prjStateCnt" + "\t" + "maxStackDepth" + "\t" + "peakTotalMem(MB)" + "\t" + "peakUsedMem(MB)\n");
			out.write(tranFiringCnt + "\t" + totalStateCnt + "\t" + max_stack_depth + "\t" + peakTotalMem + "\t" 
					+ peakUsedMem + "\n");
			out.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error producing performance results.");
		}	
	}

	private static void printTranStack(Stack<LinkedList<Transition>> lpnTranStack, String title) {
		if (title != null)
			System.out.println(title);
		for (int i=0; i<lpnTranStack.size(); i++) {
			LinkedList<Transition> tranList = lpnTranStack.get(i);
			for (int j=0; j<tranList.size(); j++) {
				System.out.println(tranList.get(j).getFullLabel());
			}
			System.out.println("----------------");
		}
	}

//	private void printNextGlobalStateMap(HashMap<Transition, PrjState> nextStateMap) {
//		for (Transition t: nextStateMap.keySet()) {
//			System.out.println(t.getFullLabel() + " -> ");
//			State[] stateArray = nextStateMap.get(t).getStateArray();
//			for (int i=0; i<stateArray.length; i++) {
//				System.out.print("S" + stateArray[i].getIndex() + "(" + stateArray[i].getLpn().getLabel() +")" +", ");
//			}
//			System.out.println("");
//		}
//	}

//	private LpnTranList convertToLpnTranList(HashSet<Transition> newNextPersistent) {
//		LpnTranList newNextPersistentTrans = new LpnTranList();
//		for (Transition lpnTran : newNextPersistent) {
//			newNextPersistentTrans.add(lpnTran);
//		}
//		return newNextPersistentTrans;
//	}

	private LpnTranList computeCycleClosingTrans(State[] curStateArray, State[] nextStateArray, HashMap<Transition, Integer> tranFiringFreq,
			StateGraph[] sgList, HashSet<Transition> nextStateStrongStubbornSet, HashSet<Transition> curStateStrongStubbornSet, Transition firedTran) {		
		for (State s : nextStateArray)
			if (s == null) 
				throw new NullPointerException();
		String cycleClosingMthd = Options.getCycleClosingMthd();
		LpnTranList newCurStateStrongStubbornSet = new LpnTranList();
		HashSet<Transition> nextStateEnabled = new HashSet<Transition>();
		HashSet<Transition> curStateEnabled = new HashSet<Transition>();
		for (int lpnIndex=0; lpnIndex<nextStateArray.length; lpnIndex++) {
   			nextStateEnabled.addAll(StateGraph.getEnabledFromTranVector(nextStateArray[lpnIndex]));
   			curStateEnabled.addAll(StateGraph.getEnabledFromTranVector(curStateArray[lpnIndex]));
   		}		
//		for (int lpnIndex=0; lpnIndex<nextStateArray.length; lpnIndex++) {
//			LhpnFile curLpn = sgList[lpnIndex].getLpn();
//			for (int i=0; i < curLpn.getAllTransitions().length; i++) {
//				Transition tran = curLpn.getAllTransitions()[i];
//				if (nextStateArray[lpnIndex].getTranVector()[i]) 
//					nextStateEnabled.add(tran);
//				if (curStateArray[lpnIndex].getTranVector()[i])
//					curStateEnabled.add(tran);
//			}
//		}
		// Cycle closing on global state graph
		if (cycleClosingMthd.equals("strong")) {
			if (Options.getDebugMode()) {
				System.out.println("****** cycle closing check: Strong Cycle Closing ********");
			}
			// Strong cycle condition: Any cycle contains at least one state where it fully expands.
			//newCurStatePersistent.addAll(setSubstraction(curStateEnabled, nextStatePersistent));
			newCurStateStrongStubbornSet.addAll(curStateEnabled);
			updateLocalStrongStubbornSetTbl(newCurStateStrongStubbornSet, sgList, curStateArray);
		}
		else if (cycleClosingMthd.equals("behavioral")) {
			if (Options.getDebugMode()) {
				System.out.println("****** cycle closing check: Behavioral with Strong Stubborn Set Computation ********");
			}
			HashSet<Transition> curStateReduced = setSubstraction(curStateEnabled, curStateStrongStubbornSet);
			HashSet<Transition> oldNextStateStrongStubbornSet = new HashSet<Transition>();
			DependentSetComparator depComp = new DependentSetComparator(tranFiringFreq, sgList.length-1); 
			PriorityQueue<DependentSet> dependentSetQueue = new PriorityQueue<DependentSet>(nextStateEnabled.size(), depComp);
			if (Options.getDebugMode())
				printStateArray(nextStateArray,"******* nextStateArray *******");    			
			for (int lpnIndex=0; lpnIndex < sgList.length; lpnIndex++) {
				if (sgList[lpnIndex].getEnabledSetTbl().get(nextStateArray[lpnIndex]) != null) {
					LpnTranList oldLocalNextStateStrongStubbornTrans = sgList[lpnIndex].getEnabledSetTbl().get(nextStateArray[lpnIndex]);
					if (Options.getDebugMode()) {
						printTransList(oldLocalNextStateStrongStubbornTrans, "oldLocalNextStateStrongStubbornTrans");
					}    						
					for (Transition oldLocalTran : oldLocalNextStateStrongStubbornTrans)
						oldNextStateStrongStubbornSet.add(oldLocalTran);
				}

			}
			HashSet<Transition> ignored = setSubstraction(curStateReduced, oldNextStateStrongStubbornSet);
			if (Options.getDebugMode()) {
				printIntegerSet(ignored, "------ Ignored transition(s) at global state " + printGlobalStateLabel(nextStateArray));
			}
			boolean isCycleClosingStrongStubbornComputation = true;
			for (Transition seed : ignored) {
				HashSet<Transition> dependent = new HashSet<Transition>();
				dependent = computeDependent(curStateArray,seed,dependent,nextStateEnabled,isCycleClosingStrongStubbornComputation); 				    				
				if (Options.getDebugMode()) {
					printIntegerSet(dependent, "------ dependent set for ignored transition " + seed.getFullLabel() + " ------");
				}
				// TODO: Is this still necessary?
				//     				boolean dependentOnlyHasDummyTrans = true;
				//	  				for (LpnTransitionPair dependentTran : dependent) {
				//	  					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
				//	  								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
				//	  				}         	    							
				//	  				if ((newNextPersistent.size() == 0 || dependent.size() < newNextPersistent.size()) && !dependentOnlyHasDummyTrans) 
				//	  					newNextPersistent = (HashSet<LpnTransitionPair>) dependent.clone();
				//	  				DependentSet dependentSet = new DependentSet(newNextPersistent, seed, 
				//	  						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
				//					_______________________________________________________________________
				DependentSet dependentSet = new DependentSet(dependent, seed, isDummyTran(seed.getLabel())); 	  						
				dependentSetQueue.add(dependentSet);
			}
			cachedNecessarySets.clear();
			if (!dependentSetQueue.isEmpty()) {
				//    				System.out.println("depdentSetQueue is NOT empty.");
				//    				newNextPersistent = dependentSetQueue.poll().getDependent();
				// **************************
				// TODO: Will newNextTmp - oldNextStrongStubborn be safe?
				HashSet<Transition> strongStubbornIgnoredTrans = dependentSetQueue.poll().getDependent();        			
				//newNextPersistent = setSubstraction(newNextPersistentTmp, oldNextPersistent);
				newCurStateStrongStubbornSet.addAll(setSubstraction(strongStubbornIgnoredTrans, oldNextStateStrongStubbornSet));
				// **************************
				updateLocalStrongStubbornSetTbl(newCurStateStrongStubbornSet, sgList, curStateArray);
			}
			if (Options.getDebugMode()) {
				printTransList(newCurStateStrongStubbornSet, "----- Ignored trans needed to add to global state " + printGlobalStateLabel(curStateArray) + " ------");
				System.out.println("******** behavioral: end of cycle closing check *****");
			}
		}
		else if (cycleClosingMthd.equals("state_search")) {
			// TODO: complete cycle closing check for state search.		
		}
		return newCurStateStrongStubbornSet;
	}


	private static void updateLocalStrongStubbornSetTbl(LpnTranList nextStrongStubbornTrans,
			StateGraph[] sgList, State[] curStateArray) {
		// Persistent set at each state is stored in the enabledSetTbl in each state graph.
		for (Transition tran : nextStrongStubbornTrans) {
			int lpnIndex = tran.getLpn().getLpnIndex();
			State nextState = curStateArray[lpnIndex];
			LpnTranList curStrongStubbornTrans = sgList[lpnIndex].getEnabledSetTbl().get(nextState);			
			if (curStrongStubbornTrans != null) {
				if (!curStrongStubbornTrans.contains(tran))
					curStrongStubbornTrans.add(tran);
			}
			else {
				LpnTranList newLpnTranList = new LpnTranList();
				newLpnTranList.add(tran);
				sgList[lpnIndex].getEnabledSetTbl().put(curStateArray[lpnIndex], newLpnTranList);
			}
		}
		if (Options.getDebugMode()) {
			printStrongStubbornSetTbl(sgList);
		}
	}

//	private void printPrjStateSet(StateSetInterface prjStateSet) {
//		for (PrjState curGlobal : prjStateSet) {
//			State[] curStateArray = curGlobal.toStateArray();
//			printStateArray(curStateArray, null);
//			System.out.println("-------------");
//		}	
//	}

//	/**
//	 * This method performs first-depth search on multiple LPNs and applies partial order reduction technique with the trace-back. 
//	 * @param sgList
//	 * @param initStateArray
//	 * @param cycleClosingMthdIndex 
//	 * @return
//	 */
//	public StateGraph[] search_dfsPORrefinedCycleRule(final StateGraph[] sgList, final State[] initStateArray, int cycleClosingMthdIndex) {
//		System.out.println("---> calling function search_dfsPORrefinedCycleRule");
//		if (cycleClosingMthdIndex == 1) 
//			System.out.println("---> POR with behavioral analysis");
//		else if (cycleClosingMthdIndex == 2)
//			System.out.println("---> POR with behavioral analysis and state trace-back");	
//		else if (cycleClosingMthdIndex == 4)
//			System.out.println("---> POR with Peled's cycle condition");
//		double peakUsedMem = 0;
//		double peakTotalMem = 0;
//		boolean failure = false;
//		int tranFiringCnt = 0;
//		int totalStates = 1;
//		int numLpns = sgList.length;
//		
//		HashSet<PrjState> stateStack = new HashSet<PrjState>();
//		Stack<LinkedList<Transition>> lpnTranStack = new Stack<LinkedList<Transition>>();
//		Stack<Integer> curIndexStack = new Stack<Integer>();
//		
//		HashSet<PrjState> prjStateSet = new HashSet<PrjState>();
//		PrjState initPrjState = new PrjState(initStateArray);
//		prjStateSet.add(initPrjState);
//		
//		PrjState stateStackTop = initPrjState;
//		System.out.println("%%%%%%% Add states to stateStack %%%%%%%%");
//		printStateArray(stateStackTop.toStateArray());
//		stateStack.add(stateStackTop);
		
//		// Prepare static pieces for POR 		
//		HashMap<Integer, HashMap<Integer, StaticSets>> staticSetsMap = new HashMap<Integer, HashMap<Integer, StaticSets>>(); 
//		Transition[] allTransitions = sgList[0].getLpn().getAllTransitions();
//		HashMap<Integer, StaticSets> tmpMap = new HashMap<Integer, StaticSets>();
//		HashMap<Integer, Integer> tranFiringFreq = new HashMap<Integer, Integer>(allTransitions.length);
//		for (Transition curTran: allTransitions) {
//			StaticSets curStatic = new StaticSets(sgList[0].getLpn(), curTran, allTransitions);
//			curStatic.buildDisableSet();
//			curStatic.buildEnableSet();
//			curStatic.buildModifyAssignSet();
//			tmpMap.put(curTran.getIndex(), curStatic);
//			tranFiringFreq.put(curTran.getIndex(), 0);
//		}
//		staticSetsMap.put(sgList[0].getLpn().getLpnIndex(), tmpMap);
//		printStaticSetMap(staticSetsMap);

		
//		System.out.println("call getPersistent on initStateArray at 0: ");
//		boolean init = true;
//		PersistentxSet initPersistent = new PersistentSet();
//		initPersistent = sgList[0].getPersistent(initStateArray[0], staticSetsMap, init, tranFiringFreq);
//		HashMap<State, LpnTranList> initEnabledSetTbl = (HashMap<State, LpnTranList>) sgList[0].copyEnabledSetTbl();
//		lpnTranStack.push(initPersistent.getPersistentSet());
//		curIndexStack.push(0);
		
//		System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//		printTransitionSet((LpnTranList) initPersistent.getPersistentSet(), "");
//
//		main_while_loop: while (failure == false && stateStack.size() != 0) {
//			System.out.println("$$$$$$$$$$$ loop begins $$$$$$$$$$");
//			long curTotalMem = Runtime.getRuntime().totalMemory();
//			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//
//			if (curTotalMem > peakTotalMem)
//				peakTotalMem = curTotalMem;
//
//			if (curUsedMem > peakUsedMem)
//				peakUsedMem = curUsedMem;
//
//			if (stateStack.size() > max_stack_depth)
//				max_stack_depth = stateStack.size();
//			
//			iterations++;
//			if (iterations % 100000 == 0) {
//				System.out.println("---> #iteration " + iterations
//						+ "> # LPN transition firings: " + tranFiringCnt
//						+ ", # of prjStates found: " + totalStates
//						+ ", stack_depth: " + stateStack.size()
//						+ " used memory: " + (float) curUsedMem / 1000000
//						+ " free memory: "
//						+ (float) Runtime.getRuntime().freeMemory() / 1000000);
//			}
//
//			State[] curStateArray = stateStackTop.toStateArray(); //stateStack.peek();
//			int curIndex = curIndexStack.peek();
////			System.out.println("curIndex = " + curIndex);
//			//PersistentSet curPersistent = new PersistentSet();
//			//LinkedList<Transition> curPersistentTrans = curPersistent.getPersistentSet();
//			LinkedList<Transition> curPersistentTrans = lpnTranStack.peek();
//						
////			System.out.println("------- curStateArray ----------");
////			printStateArray(curStateArray);
////			System.out.println("+++++++ curPersistent trans ++++++++");
////			printTransLinkedList(curPersistentTrans);
//			
//			// If all enabled transitions of the current LPN are considered,
//			// then consider the next LPN
//			// by increasing the curIndex.
//			// Otherwise, if all enabled transitions of all LPNs are considered,
//			// then pop the stacks.		
//			if (curPersistentTrans.size() == 0) {
//				lpnTranStack.pop();
////				System.out.println("+++++++ Pop trans off lpnTranStack ++++++++");
//				curIndexStack.pop();
////				System.out.println("+++++++ Pop index off curIndexStack ++++++++");
//				curIndex++;
////				System.out.println("curIndex = " + curIndex);
//				while (curIndex < numLpns) {
//					System.out.println("call getEnabled on curStateArray at 1: ");

//					LpnTranList tmpPersistentTrans = (LpnTranList) (sgList[curIndex].getPersistent(curStateArray[curIndex], staticSetsMap, init, tranFiringFreq)).getPersistentSet();
//					curPersistentTrans = tmpPersistentTrans.clone();
//					//printTransitionSet(curEnabled, "curEnabled set");
//					if (curPersistentTrans.size() > 0) {
//						System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//						printTransLinkedList(curPersistentTrans);
//						lpnTranStack.push(curPersistentTrans);
//						curIndexStack.push(curIndex);
//						printIntegerStack("curIndexStack after push 1", curIndexStack);
//						break;
//					} 					
//					curIndex++;
//				}
//			}
//			if (curIndex == numLpns) {
//				prjStateSet.add(stateStackTop);
//				System.out.println("%%%%%%% Remove stateStackTop from stateStack %%%%%%%%");
//				printStateArray(stateStackTop.toStateArray());
//				stateStack.remove(stateStackTop);
//				stateStackTop = stateStackTop.getFather();
//				continue;
//			}
//
//			Transition firedTran = curPersistentTrans.removeLast();	
//			System.out.println("###################");			
//			System.out.println("Fired transition: " + firedTran.getName());
//			System.out.println("###################");

//			Integer freq = tranFiringFreq.get(firedTran.getIndex()) + 1;
//			tranFiringFreq.put(firedTran.getIndex(), freq);
//			System.out.println("~~~~~~tranFiringFreq~~~~~~~");
//			printHashMap(tranFiringFreq, allTransitions);
//			State[] nextStateArray = sgList[curIndex].fire(sgList, curStateArray, firedTran);
//			tranFiringCnt++;
		
			// Check if the firedTran causes disabling error or deadlock.
//			@SuppressWarnings("unchecked")
//			LinkedList<Transition>[] curPersistentArray = new LinkedList[numLpns];
//			@SuppressWarnings("unchecked")
//			LinkedList<Transition>[] nextPersistentArray = new LinkedList[numLpns];
//			boolean updatedPersistentDueToCycleRule = false;
//			for (int i = 0; i < numLpns; i++) {
//				StateGraph sg_tmp = sgList[i];
//				System.out.println("call getPersistent on curStateArray at 2: i = " + i);
//				PersistentSet PersistentList = new PersistentSet();
//				if (init) {
//					PersistentList = initPersistent;
//					sg_tmp.setEnabledSetTbl(initEnabledSetTbl);
//					init = false;
//				}
//				else
//					PersistentList = sg_tmp.getPersistent(curStateArray[i], staticSetsMap, init, tranFiringFreq);
//				curPersistentArray[i] = PersistentList.getPersistentSet();

//				System.out.println("call getPersistentRefinedCycleRule on nextStateArray at 3: i = " + i);

//				PersistentList = sg_tmp.getPersistentRefinedCycleRule(curStateArray[i], nextStateArray[i], staticSetsMap, stateStack, stateStackTop, init, cycleClosingMthdIndex, i, tranFiringFreq);
//				nextPersistentArray[i] = PersistentList.getPersistentSet();
//				if (!updatedPersistentDueToCycleRule && PersistentList.getPersistentChanged()) {
//					updatedPersistentDueToCycleRule = true;

//				System.out.println("-------------- curPersistentArray --------------");
//				for (LinkedList<Transition> tranList : curPersistentArray) {
//					printTransLinkedList(tranList);
//				}
////				System.out.println("-------------- curPersistentArray --------------");
////				for (LinkedList<Transition> tranList : curPersistentArray) {
////					printTransLinkedList(tranList);
////				}
////				System.out.println("-------------- nextPersistentArray --------------");
////				for (LinkedList<Transition> tranList : nextPersistentArray) {
////					printTransLinkedList(tranList);
////				}
//				Transition disabledTran = firedTran.disablingError(
//						curStateArray[i].getEnabledTransitions(), nextStateArray[i].getEnabledTransitions());
//				if (disabledTran != null) {
//					System.err.println("Disabling Error: "
//							+ disabledTran.getFullLabel() + " is disabled by "
//							+ firedTran.getFullLabel());
//					failure = true;
//					break main_while_loop;
//				}

//			}
//
//			if (Analysis.deadLock(sgList, nextStateArray, staticSetsMap, init, tranFiringFreq) == true) {
//				System.out.println("*** Verification failed: deadlock.");
//				failure = true;
//				break main_while_loop;
//			}
//
//			PrjState nextPrjState = new PrjState(nextStateArray);
//			Boolean	existingState = prjStateSet.contains(nextPrjState) || stateStack.contains(nextPrjState);
//			if (existingState == true && updatedPersistentDueToCycleRule) {
//				// cycle closing
//				System.out.println("%%%%%%% existingSate == true %%%%%%%%");
//				stateStackTop.setChild(nextPrjState);
//				nextPrjState.setFather(stateStackTop);
//				stateStackTop = nextPrjState;
//				stateStack.add(stateStackTop);
//				System.out.println("%%%%%%% Add state to stateStack %%%%%%%%");
//				printStateArray(stateStackTop.toStateArray());
//				System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//				printTransitionSet((LpnTranList) nextPersistentArray[0], "");
//				lpnTranStack.push((LpnTranList) nextPersistentArray[0].clone());
//				curIndexStack.push(0);
//			}			
//			if (existingState == false) {
//				System.out.println("%%%%%%% existingSate == false %%%%%%%%");
//				stateStackTop.setChild(nextPrjState);
//				nextPrjState.setFather(stateStackTop);
//				stateStackTop = nextPrjState;
//				stateStack.add(stateStackTop);
//				System.out.println("%%%%%%% Add state to stateStack %%%%%%%%");
//				printStateArray(stateStackTop.toStateArray());
//				System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//				printTransitionSet((LpnTranList) nextPersistentArray[0], "");
//				lpnTranStack.push((LpnTranList) nextPersistentArray[0].clone());
//				curIndexStack.push(0);
//				totalStates++;
//			}
//		}
//		// end of main_while_loop
//		
//		double totalStateCnt = prjStateSet.size();
//		
//		System.out.println("---> final numbers: # LPN transition firings: "	+ tranFiringCnt 
//				+ ", # of prjStates found: " + totalStateCnt 
//				+ ", max_stack_depth: " + max_stack_depth 
//				+ ", peak total memory: " + peakTotalMem / 1000000 + " MB"
//				+ ", peak used memory: " + peakUsedMem / 1000000 + " MB");
//
//		// This currently works for a single LPN.
//		return sgList;
//		return null;
//	}

	private void printStaticSetsMap( LPN[] lpnList) {		
		System.out.println("============ staticSetsMap ============");			
		for (Transition lpnTranPair : staticDependency.keySet()) {
			StaticDependencySets statSets = staticDependency.get(lpnTranPair);			
			printLpnTranPair(statSets.getSeedTran(), statSets.getDisableSet(false), "disableSet");
			for (HashSet<Transition> setOneConjunctTrue : statSets.getOtherTransSetSeedTranEnablingTrue()) {
				printLpnTranPair(statSets.getSeedTran(), setOneConjunctTrue, "enableBySetingEnablingTrue for one conjunct");
			}				
		}
	}
	
	private static void printLpnTranPair(Transition curTran,
			HashSet<Transition> TransitionSet, String setName) {				
		System.out.println(setName + " for transition " + curTran.getFullLabel() + " is: ");
		if (TransitionSet.isEmpty()) {
			System.out.println("empty");				
		}
		else {
			for (Transition lpnTranPair: TransitionSet) 
				System.out.print(lpnTranPair.getFullLabel() + "\n");						
			System.out.println();
		}					
	}	
	
//	private Transition[] assignStickyTransitions(LhpnFile lpn) {
//		// allProcessTrans is a hashmap from a transition to its process color (integer). 
//		HashMap<Transition, Integer> allProcessTrans = new HashMap<Transition, Integer>();
//		// create an Abstraction object to call the divideProcesses method. 
//		Abstraction abs = new Abstraction(lpn);
//		abs.decomposeLpnIntoProcesses();
//		allProcessTrans.putAll(
//				 (HashMap<Transition, Integer>)abs.getTransWithProcIDs().clone());
//		HashMap<Integer, LpnProcess> processMap = new HashMap<Integer, LpnProcess>();
//		for (Iterator<Transition> tranIter = allProcessTrans.keySet().iterator(); tranIter.hasNext();) {
//			Transition curTran = tranIter.next();
//			Integer procId = allProcessTrans.get(curTran);
//			if (!processMap.containsKey(procId)) {
//				LpnProcess newProcess = new LpnProcess(procId);
//				newProcess.addTranToProcess(curTran);
//				if (curTran.getPreset() != null) {
//					Place[] preset = curTran.getPreset();
//					for (Place p : preset) {
//						newProcess.addPlaceToProcess(p);
//					}
//				}
//				processMap.put(procId, newProcess);
//			}
//			else {
//				LpnProcess curProcess = processMap.get(procId);
//				curProcess.addTranToProcess(curTran);
//				if (curTran.getPreset() != null) {
//					Place[] preset = curTran.getPreset();
//					for (Place p : preset) {
//						curProcess.addPlaceToProcess(p);
//					}
//				}
//			}
//		}
//		
//		for(Iterator<Integer> processMapIter = processMap.keySet().iterator(); processMapIter.hasNext();) {
//			LpnProcess curProc = processMap.get(processMapIter.next());
//			curProc.assignStickyTransitions();
//			curProc.printProcWithStickyTrans();
//		}
//		return lpn.getAllTransitions();
//	}

//	private void printLpnTranPair(LhpnFile[] lpnList, Transition curTran,
//			HashSet<Transition> curDisable, String setName) {
//		System.out.print(setName + " for " + curTran.getName() + "(" + curTran.getIndex() + ")" + " is: ");
//		if (curDisable.isEmpty()) {
//			System.out.println("empty");
//		}
//		else {
//			for (Transition lpnTranPair: curDisable) {
//				System.out.print("(" + lpnList[lpnTranPair.getLpnIndex()].getLabel() 
//						+ ", " + lpnList[lpnTranPair.getLpnIndex()].getAllTransitions()[lpnTranPair.getTranIndex()].getName() + ")," + "\t");
//			}
//			System.out.print("\n");
//		}			
//	}
			
    /**
     * Return the set of all LPN transitions that are enabled in 'state'.
     * The enabledSetTbl (for each StateGraph obj) stores the strong stubborn set for each state of each LPN.
     * @param stateArray
     * @param prjStateSet 
     * @param enable 
     * @param disableByStealingToken 
     * @param disable 
     * @param sgList 
     * @return
     */
    private LpnTranList buildStrongStubbornSet(State[] curStateArray, State[] nextStateArray, 
    			HashMap<Transition, Integer> tranFiringFreq, StateGraph[] sgList, LPN[] lpnList,
    			StateSetInterface prjStateSet, PrjState stateStackTop, Transition lastFiredTran) {
    	State[] stateArray = null;
    	if (nextStateArray == null)
    		stateArray = curStateArray;
    	else
    		stateArray = nextStateArray;
    	for (State s : stateArray)
    		if (s == null) 
    			throw new NullPointerException();
    	LpnTranList strongStubbornSet = new LpnTranList();
    	HashSet<Transition> curEnabled = new HashSet<Transition>();
   		for (int lpnIndex=0; lpnIndex<stateArray.length; lpnIndex++) {
   			curEnabled.addAll(StateGraph.getEnabledFromTranVector(stateArray[lpnIndex]));
   		}  		
        if (Options.getDebugMode()) {
	      	System.out.println("******* Partial Order Reduction *******");
	      	String name = null;
	      	String globalStateLabel = "";
	      	if (curStateArray == null)
	      		name = "nextStateArray";
	      	else
	      		name = "curStateArray";
	      	for (State st : stateArray) {
	      		globalStateLabel += st.getFullLabel() + "_";
	      	}
	      	System.out.println(name + ": " +globalStateLabel.substring(0, globalStateLabel.lastIndexOf("_")));	      	
	      	printIntegerSet(curEnabled, "Enabled set");
	      	System.out.println("******* Begin POR *******");
        }
        if (curEnabled.isEmpty()) {
        	System.out.println("curEnabled is empty. Exit");        	
        	return strongStubbornSet;
        }
        //HashSet<Transition> ready = curEnabled;
        HashSet<Transition> ready = computeStrongStubbornSet(stateArray, curEnabled, tranFiringFreq, sgList);
    	if (Options.getDebugMode()) {
	    	System.out.println("******* End POR *******");
	    	printIntegerSet(ready, "Strong Stubborn Set");
	    	System.out.println("********************");
    	}
    	
		// ************* Priority: transition fired less times first *************
    	if (tranFiringFreq != null) {
    		LinkedList<Transition> readyList = new LinkedList<Transition>();
        	for (Transition inReady : ready) {
        		readyList.add(inReady);
        	}
        	mergeSort(readyList, tranFiringFreq);
       		for (Transition inReady : readyList) {
       			strongStubbornSet.addFirst(inReady);
       		}
       	}
    	else {
    		for (Transition tran : ready) {
       			strongStubbornSet.add(tran);
       		}
    	}
    	return strongStubbornSet;
    }

	private LinkedList<Transition> mergeSort(LinkedList<Transition> array, HashMap<Transition, Integer> tranFiringFreq) {
		if (array.size() == 1)
			return array;
		int middle = array.size() / 2;
		LinkedList<Transition> left = new LinkedList<Transition>();
		LinkedList<Transition> right = new LinkedList<Transition>();
		for (int i=0; i<middle; i++) {
			left.add(i, array.get(i));
		}
		for (int i=middle; i<array.size();i++) {
			right.add(i-middle, array.get(i));
		}
		left = mergeSort(left, tranFiringFreq);
		right = mergeSort(right, tranFiringFreq);
		return merge(left, right, tranFiringFreq);
	}

	private static LinkedList<Transition> merge(LinkedList<Transition> left,
			LinkedList<Transition> right, HashMap<Transition, Integer> tranFiringFreq) {
		LinkedList<Transition> result = new LinkedList<Transition>(); 
		while (left.size() > 0 || right.size() > 0) {
			if (left.size() > 0 && right.size() > 0) {
				if (tranFiringFreq.get(left.peek()) <= tranFiringFreq.get(right.peek())) {
					result.addLast(left.poll());
				}
				else {
					result.addLast(right.poll());				
				}
			}
			else if (left.size()>0) {
				result.addLast(left.poll());
			}
			else if (right.size()>0) {
				result.addLast(right.poll());
			}
		}
		return result;
	}

//	/**
//     * Return the set of all LPN transitions that are enabled in 'state'. The "init" flag indicates whether a transition
//     * needs to be evaluated. 
//     * @param nextState
//	 * @param stateStackTop 
//     * @param enable 
//     * @param disableByStealingToken 
//     * @param disable 
//     * @param init 
//	 * @param cycleClosingMthdIndex 
//	 * @param lpnIndex 
//     * @param isNextState
//     * @return
//     */
//	private LpnTranList getPersistentRefinedCycleRule(State[] curStateArray, State[] nextStateArray, HashMap<Transition,StaticDependencySets> staticSetsMap, 
//			boolean init, HashMap<Transition,Integer> tranFiringFreq, StateGraph[] sgList, 
//			HashSet<PrjState> stateStack, PrjState stateStackTop) {
//    	PersistentSet nextPersistent = new PersistentSet();
//    	if (nextState == null) {
//            throw new NullPointerException();
//        }
//    	if(PersistentSetTbl.containsKey(nextState) == true && stateOnStack(nextState, stateStack)) {
//     		System.out.println("~~~~~~~ existing state in enabledSetTbl and on stateStack: S" + nextState.getIndex() + "~~~~~~~~");
//    		printTransitionSet((LpnTranList)PersistentSetTbl.get(nextState), "Old Persistent at this state: ");
// 			// Cycle closing check
//    		LpnTranList nextPersistentTransOld = (LpnTranList) nextPersistent.getPersistentSet();
//    		nextPersistentTransOld = PersistentSetTbl.get(nextState);
//    		LpnTranList curPersistentTrans = PersistentSetTbl.get(curState);
//    		LpnTranList curReduced = new LpnTranList();
//    		LpnTranList curEnabled = curState.getEnabledTransitions(); 		
//    		for (int i=0; i<curEnabled.size(); i++) {
//    			if (!curPersistentTrans.contains(curEnabled.get(i))) {
//    				curReduced.add(curEnabled.get(i));
//    			}
//    		}
//    		if (!nextPersistentTransOld.containsAll(curReduced)) {
//    			System.out.println("((((((((((((((((((((()))))))))))))))))))))");
//    			printTransitionSet(curEnabled, "curEnabled:");
//    			printTransitionSet(curPersistentTrans, "curPersistentTrans:");
//    			printTransitionSet(curReduced, "curReduced:");
//    			printTransitionSet(nextState.getEnabledTransitions(), "nextEnabled:");
//    			printTransitionSet(nextPersistentTransOld, "nextPersistentTransOld:");
//    			nextPersistent.setPersistentChanged();
//    			HashSet<Transition> curEnabledIndicies = new HashSet<Transition>();
//        		for (int i=0; i<curEnabled.size(); i++) {
//        			curEnabledIndicies.add(curEnabled.get(i).getIndex());
//        		} 			
//    			// transToAdd = curReduced - nextPersistentOld
//    			LpnTranList overlyReducedTrans = getSetSubtraction(curReduced, nextPersistentTransOld);
//    			HashSet<Integer> transToAddIndices = new HashSet<Integer>();
//    			for (int i=0; i<overlyReducedTrans.size(); i++) {
//    				transToAddIndices.add(overlyReducedTrans.get(i).getIndex());
//    			}	
//    			HashSet<Integer> nextPersistentNewIndices = (HashSet<Integer>) curEnabledIndicies.clone();
//    			for (Integer tranToAdd : transToAddIndices) {
//    				HashSet<Transition> dependent = new HashSet<Transition>();
//    				//Transition enabledTransition = this.lpn.getAllTransitions()[tranToAdd];
//    				dependent = computeDependent(curState,tranToAdd,dependent,curEnabledIndicies,staticMap);  				
//    				// printIntegerSet(dependent, "dependent set for enabled transition " + this.lpn.getAllTransitions()[enabledTran]);
//    								
//    				boolean dependentOnlyHasDummyTrans = true;
//    				for (Integer curTranIndex : dependent) {
//    					Transition curTran = this.lpn.getAllTransitions()[curTranIndex];
//    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans && isDummyTran(curTran.getName());
//    				}			
//    				if (dependent.size() < nextPersistentNewIndices.size() && !dependentOnlyHasDummyTrans) 
//    					nextPersistentNewIndices = (HashSet<Integer>) dependent.clone();
//    				if (nextPersistentNewIndices.size() == 1)
//    					break;
//    			}
//       			LpnTranList nextPersistentNew = new LpnTranList();
//    			for (Integer nextPersistentNewIndex : nextPersistentNewIndices) {
//    				nextPersistentNew.add(this.getLpn().getTransition(nextPersistentNewIndex));
//    			}
//    			LpnTranList transToAdd = getSetSubtraction(nextPersistentNew, nextPersistentTransOld);
//    			boolean allTransToAddFired = false;
//    			if (cycleClosingMthdIndex == 2) {
//    				// For every transition t in transToAdd, if t was fired in the any state on stateStack, there is no need to put it to the new Persistent of nextState.
//            		if (transToAdd != null) {
//            			LpnTranList transToAddCopy = transToAdd.copy();
//            			HashSet<Integer> stateVisited = new HashSet<Integer>();
//            			stateVisited.add(nextState.getIndex());
//            			allTransToAddFired = allTransToAddFired(transToAddCopy, 
//            									allTransToAddFired, stateVisited, stateStackTop, lpnIndex);
//            		}
//    			}
//        		// Update the old Persistent of the next state 
//        		if (!allTransToAddFired || cycleClosingMthdIndex == 1) {
//        			nextPersistent.getPersistentSet().clear();
//        			nextPersistent.getPersistentSet().addAll(transToAdd);
//        			PersistentSetTbl.get(nextState).addAll(transToAdd);
//        			printTransitionSet(nextPersistentNew, "nextPersistentNew:");
//        			printTransitionSet(transToAdd, "transToAdd:");
//        			System.out.println("((((((((((((((((((((()))))))))))))))))))))");
//       		}
//        		if (cycleClosingMthdIndex == 4) {
//        			nextPersistent.getPersistentSet().clear();
//        			nextPersistent.getPersistentSet().addAll(overlyReducedTrans);
//        			PersistentSetTbl.get(nextState).addAll(overlyReducedTrans);
//        			printTransitionSet(nextPersistentNew, "nextPersistentNew:");
//        			printTransitionSet(transToAdd, "transToAdd:");
//        			System.out.println("((((((((((((((((((((()))))))))))))))))))))");
//        		}
//    		}
//    		// enabledSetTble stores the Persistent set at curState. 
//    		// The fully enabled set at each state is stored in the tranVector in each state. 		
//    		return (PersistentSet) nextPersistent;
//    	}
		
		
//		
//    	for (State s : nextStateArray)
//    		if (s == null) 
//    			throw new NullPointerException();
//		cachedNecessarySets.clear();
//    	String cycleClosingMthd = Options.getCycleClosingMthd();
//    	PersistentSet nextPersistent = new PersistentSet();
//    	HashSet<Transition> nextEnabled = new HashSet<Transition>();
////    	boolean allEnabledAreSticky = false;
//    	for (int lpnIndex=0; lpnIndex<nextStateArray.length; lpnIndex++) {
//   			State nextState = nextStateArray[lpnIndex];
//   			if (init) {
//             	LhpnFile curLpn = sgList[lpnIndex].getLpn();
//              	//allEnabledAreSticky = true;
//             	for (int i=0; i < curLpn.getAllTransitions().length; i++) {
//                   	Transition tran = curLpn.getAllTransitions()[i];
//                   	if (sgList[lpnIndex].isEnabled(tran,nextState)){
//                   		nextEnabled.add(new Transition(curLpn.getLpnIndex(), i));
//                   		//allEnabledAreSticky = allEnabledAreSticky && tran.isSticky();
//                    }
//                }
//             	//sgList[lpnIndex].getEnabledSetTbl().put(nextStateArray[lpnIndex], new LpnTranList());          	
//   			}
//   			else {
//   				LhpnFile curLpn = sgList[lpnIndex].getLpn();
//   				for (int i=0; i < curLpn.getAllTransitions().length; i++) {
//   					Transition tran = curLpn.getAllTransitions()[i];
//   					if (nextStateArray[lpnIndex].getTranVector()[i]) {
//   						nextEnabled.add(new Transition(curLpn.getLpnIndex(), i));
//               		   	//allEnabledAreSticky = allEnabledAreSticky && tran.isSticky();
//   					}
//               	}
//   				//sgList[lpnIndex].getEnabledSetTbl().put(nextStateArray[lpnIndex], new LpnTranList());
//   			}
//    	}
//    	DependentSetComparator depComp = new DependentSetComparator(tranFiringFreq); 
//		PriorityQueue<DependentSet> dependentSetQueue = new PriorityQueue<DependentSet>(nextEnabled.size(), depComp);
//		LhpnFile[] lpnList = new LhpnFile[sgList.length];
//		for (int i=0; i<sgList.length; i++) 
//			lpnList[i] = sgList[i].getLpn();
//		HashMap<Transition, LpnTranList> transToAddMap = new HashMap<Transition, LpnTranList>();
//		Integer cycleClosingLpnIndex = -1;
//		for (int lpnIndex=0; lpnIndex<nextStateArray.length; lpnIndex++) {
//			State curState = curStateArray[lpnIndex];
//   			State nextState = nextStateArray[lpnIndex];
//       		if(sgList[lpnIndex].getEnabledSetTbl().containsKey(nextState) == true 
//       				&& !sgList[lpnIndex].getEnabledSetTbl().get(nextState).isEmpty()
//       				&& stateOnStack(lpnIndex, nextState, stateStack)
//       				&& nextState.getIndex() != curState.getIndex()) {
//       			cycleClosingLpnIndex = lpnIndex;
//           		System.out.println("~~~~~~~ existing state in enabledSetTbl for LPN " + sgList[lpnIndex].getLpn().getLabel() +": S" + nextState.getIndex() + "~~~~~~~~");
//          		printPersistentSet((LpnTranList)sgList[lpnIndex].getEnabledSetTbl().get(nextState), "Old Persistent at this state: ");
//  	    		LpnTranList oldLocalNextPersistentTrans = sgList[lpnIndex].getEnabledSetTbl().get(nextState);
//  	    		LpnTranList curLocalPersistentTrans = sgList[lpnIndex].getEnabledSetTbl().get(curState);
//  	    		LpnTranList reducedLocalTrans = new LpnTranList();
//  	    		LpnTranList curLocalEnabledTrans = curState.getEnabledTransitions(); 	
//          		System.out.println("The firedTran is a cycle closing transition.");
//       			if (cycleClosingMthd.toLowerCase().equals("behavioral") || cycleClosingMthd.toLowerCase().equals("state_search")) {
//       				// firedTran is a cycle closing transition. 
//       	    		for (int i=0; i<curLocalEnabledTrans.size(); i++) {
//       	    			if (!curLocalPersistentTrans.contains(curLocalEnabledTrans.get(i))) {
//       	    				reducedLocalTrans.add(curLocalEnabledTrans.get(i));
//       	    			}
//       	    		}
//       	    		if (!oldLocalNextPersistentTrans.containsAll(reducedLocalTrans)) {
//       	    			System.out.println("****** Transitions are possibly ignored in a cycle.******");
//       	    			printTransitionSet(curLocalEnabledTrans, "curLocalEnabled:");
//       	    			printTransitionSet(curLocalPersistentTrans, "curPersistentTrans:");
//       	    			printTransitionSet(reducedLocalTrans, "reducedLocalTrans:");
//       	    			printTransitionSet(nextState.getEnabledTransitions(), "nextLocalEnabled:");
//       	    			printTransitionSet(oldLocalNextPersistentTrans, "nextPersistentTransOld:");
//       	    			
//       	    			// nextPersistent.setPersistentChanged();
//       	    			// ignoredTrans should not be empty here.
//       	    			LpnTranList ignoredTrans = getSetSubtraction(reducedLocalTrans, oldLocalNextPersistentTrans);
//       	    			HashSet<Transition> ignored = new HashSet<Transition>();
//       	    			for (int i=0; i<ignoredTrans.size(); i++) {
//       	    				ignored.add(new Transition(lpnIndex, ignoredTrans.get(i).getIndex()));
//       	    			}
//       	    			HashSet<Transition> newNextPersistent = (HashSet<Transition>) nextEnabled.clone();
//       	    			if (cycleClosingMthd.toLowerCase().equals("behavioral")) {
//         	    			for (Transition seed : ignored) {
//          	    				HashSet<Transition> dependent = new HashSet<Transition>();
//          	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//          	    				printIntegerSet(dependent, sgList, "dependent set for ignored transition " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//          							+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//          	    				
//          	    				boolean dependentOnlyHasDummyTrans = true;
//          	    				for (Transition dependentTran : dependent) {
//          	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//          	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//          	    				}         	    							
//          	    				if (dependent.size() < newNextPersistent.size() && !dependentOnlyHasDummyTrans) 
//          	    					newNextPersistent = (HashSet<Transition>) dependent.clone();
////          	    				if (nextPersistentNewIndices.size() == 1)
////          	    					break;
//          	    				DependentSet dependentSet = new DependentSet(newNextPersistent, seed, 
//          	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//          	    				dependentSetQueue.add(dependentSet);
//          	    				LpnTranList newLocalNextPersistentTrans = new LpnTranList();
//              	    			for (Transition tran : newNextPersistent) {
//              	    				if (tran.getLpnIndex() == lpnIndex)
//              	    					newLocalNextPersistentTrans.add(sgList[lpnIndex].getLpn().getTransition(tran.getTranIndex()));
//              	    			}
//              	    			LpnTranList transToAdd = getSetSubtraction(newLocalNextPersistentTrans, oldLocalNextPersistentTrans);
//              	    			transToAddMap.put(seed, transToAdd);
//          	    			}
//       	    			}
//       	    			else if (cycleClosingMthd.toLowerCase().equals("state_search")) {
//       	    				// For every transition t in ignored, if t was fired in any state on stateStack, there is no need to put it to the new Persistent of nextState.
//       	    				HashSet<Integer> stateVisited = new HashSet<Integer>();
//      	    				stateVisited.add(nextState.getIndex());
//       	    				LpnTranList trulyIgnoredTrans = ignoredTrans.copy();
//       	    				trulyIgnoredTrans = allIgnoredTransFired(trulyIgnoredTrans, stateVisited, stateStackTop, lpnIndex, sgList[lpnIndex]);
//       	    				if (!trulyIgnoredTrans.isEmpty()) {
//       	    					HashSet<Transition> trulyIgnored = new HashSet<Transition>();
//       	    					for (Transition tran : trulyIgnoredTrans) {
//       	    						trulyIgnored.add(new Transition(tran.getLpn().getLpnIndex(), tran.getIndex()));
//       	    					}
//  	          	    			for (Transition seed : trulyIgnored) {
//  	          	    				HashSet<Transition> dependent = new HashSet<Transition>();
//  	          	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//  	          	    				printIntegerSet(dependent, sgList, "dependent set for trulyIgnored transition " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//  	          							+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//  	          	    				
//   	          	    				boolean dependentOnlyHasDummyTrans = true;
//  	          	    				for (Transition dependentTran : dependent) {
//  	          	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//   	          	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//   	          	    				}         	    							
//   	          	    				if (dependent.size() < newNextPersistent.size() && !dependentOnlyHasDummyTrans) 
//   	          	    					newNextPersistent = (HashSet<Transition>) dependent.clone();
//   	          	    				DependentSet dependentSet = new DependentSet(newNextPersistent, seed, 
//   	          	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//   	          	    				dependentSetQueue.add(dependentSet);
//   	          	    				LpnTranList newLocalNextPersistentTrans = new LpnTranList();
//   	              	    			for (Transition tran : newNextPersistent) {
//   	              	    				if (tran.getLpnIndex() == lpnIndex)
//   	              	    					newLocalNextPersistentTrans.add(sgList[lpnIndex].getLpn().getTransition(tran.getTranIndex()));
//   	              	    			}
//   	              	    			LpnTranList transToAdd = getSetSubtraction(newLocalNextPersistentTrans, oldLocalNextPersistentTrans);
//   	              	    			transToAddMap.put(seed, transToAdd);
//   	          	    			}
//       	    				}
//       	    				else { // All ignored transitions were fired before. It is safe to close the current cycle.
//               	    			HashSet<Transition> oldLocalNextPersistent = new HashSet<Transition>();
//               	    			for (Transition tran : oldLocalNextPersistentTrans)
//               	    				oldLocalNextPersistent.add(new Transition(tran.getLpn().getLpnIndex(), tran.getIndex()));
//      							for (Transition seed : oldLocalNextPersistent) {
//      	          	    			HashSet<Transition> dependent = new HashSet<Transition>();
//      	      	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//      	      	    				printIntegerSet(dependent, sgList, "dependent set for transition in oldNextPersistent is " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//      	      							+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//      	      	    				boolean dependentOnlyHasDummyTrans = true;
//      	      	    				for (Transition dependentTran : dependent) {
//      	      	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//      	      	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//      	      	    				}         	    							
//      	      	    				if (dependent.size() < newNextPersistent.size() && !dependentOnlyHasDummyTrans) 
//      	      	    					newNextPersistent = (HashSet<Transition>) dependent.clone();
//      	          	    			DependentSet dependentSet = new DependentSet(newNextPersistent, seed, 
//      	      	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//      	      	    				dependentSetQueue.add(dependentSet);
//       							}
//       	    				}
//       	    			}
//       	    		}
//       	    		else {
//       	    			// oldNextPersistentTrans.containsAll(curReducedTrans) == true (safe to close the current cycle)
//       	    			HashSet<Transition> newNextPersistent = (HashSet<Transition>) nextEnabled.clone();
//       	    			HashSet<Transition> oldLocalNextPersistent = new HashSet<Transition>();
//      	    			for (Transition tran : oldLocalNextPersistentTrans)
//       	    				oldLocalNextPersistent.add(new Transition(tran.getLpn().getLpnIndex(), tran.getIndex()));
//      	    			for (Transition seed : oldLocalNextPersistent) {
//         	    			HashSet<Transition> dependent = new HashSet<Transition>();
//      	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//      	    				printIntegerSet(dependent, sgList, "dependent set for transition in oldNextPersistent is " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//      							+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//      	    				boolean dependentOnlyHasDummyTrans = true;
//      	    				for (Transition dependentTran : dependent) {
//      	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//      	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//      	    				}         	    							
//      	    				if (dependent.size() < newNextPersistent.size() && !dependentOnlyHasDummyTrans) 
//      	    					newNextPersistent = (HashSet<Transition>) dependent.clone();
//          	    			DependentSet dependentSet = new DependentSet(newNextPersistent, seed, 
//      	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//      	    				dependentSetQueue.add(dependentSet);
//						}
//       	    		}
//       			}
//       			else if (cycleClosingMthd.toLowerCase().equals("strong")) {
//       				LpnTranList ignoredTrans = getSetSubtraction(reducedLocalTrans, oldLocalNextPersistentTrans);
//   	    			HashSet<Transition> ignored = new HashSet<Transition>();
//   	    			for (int i=0; i<ignoredTrans.size(); i++) {
//   	    				ignored.add(new Transition(lpnIndex, ignoredTrans.get(i).getIndex()));
//   	    			}
//       				HashSet<Transition> allNewNextPersistent = new HashSet<Transition>();
//       				for (Transition seed : ignored) {
//       					HashSet<Transition> newNextPersistent = (HashSet<Transition>) nextEnabled.clone();
//       					HashSet<Transition> dependent = new HashSet<Transition>();
//   	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//   	    				printIntegerSet(dependent, sgList, "dependent set for transition in curLocalEnabled is " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//						+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//   	    				boolean dependentOnlyHasDummyTrans = true;
//   	    				for (Transition dependentTran : dependent) {
//   	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//   	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//   	    				}         	    							
//   	    				if (dependent.size() < newNextPersistent.size() && !dependentOnlyHasDummyTrans) 
//   	    					newNextPersistent = (HashSet<Transition>) dependent.clone();
//   	    				allNewNextPersistent.addAll(newNextPersistent);
//       				}
//       				// The strong cycle condition requires all seeds in ignored to be included in the allNewNextPersistent, as well as dependent set for each seed.
//       				// So each seed should have the same new Persistent set.
//       				for (Transition seed : ignored) {
//       					DependentSet dependentSet = new DependentSet(allNewNextPersistent, seed, 
//   	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//  	    				dependentSetQueue.add(dependentSet);
//       				}        	    		
//       			}		      		
//            }
//       		else { // firedTran is not a cycle closing transition. Compute next Persistent. 
////       			if (nextEnabled.size() == 1) 
////       				return nextEnabled;
//       			System.out.println("The firedTran is NOT a cycle closing transition.");
//       			HashSet<Transition> ready = null;
//   				for (Transition seed : nextEnabled) {
//       				System.out.println("@ partialOrderReduction, consider transition " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//  							+ "("+ sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()) + ")");
//   					HashSet<Transition> dependent = new HashSet<Transition>();
//   					Transition enabledTransition = sgList[seed.getLpnIndex()].getLpn().getAllTransitions()[seed.getTranIndex()];
//   					boolean enabledIsDummy = false;
////       					if (enabledTransition.isSticky()) {
////       						dependent = (HashSet<Transition>) nextEnabled.clone();
////       					}
////   					else {
////      						dependent = computeDependent(curStateArray,seed,dependent,nextEnabled,staticMap, lpnList);
////       					}
//   					dependent = computeDependent(curStateArray,seed,dependent,nextEnabled,staticSetsMap,lpnList);
//   					printIntegerSet(dependent, sgList, "dependent set for enabled transition " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//       							+ "(" + enabledTransition.getName() + ")");
//   					if (isDummyTran(enabledTransition.getName()))
//   						enabledIsDummy = true;
//   					for (Transition inDependent : dependent) {
//   						if(inDependent.getLpnIndex() == cycleClosingLpnIndex) {
//   							// check cycle closing condition
//   							break;
//   						}
//   					}
//   					
//   					DependentSet dependentSet = new DependentSet(dependent, seed, enabledIsDummy);
//   					dependentSetQueue.add(dependentSet);
//   				}
//   				ready = dependentSetQueue.poll().getDependent();
//       			
//       		}
////    		// Update the old Persistent of the next state 
//   		}
//
//			
////		boolean allTransToAddFired = false;
////		if (cycleClosingMthdIndex == 2) {
////			// For every transition t in transToAdd, if t was fired in the any state on stateStack, there is no need to put it to the new Persistent of nextState.
////  		if (transToAdd != null) {
////  			LpnTranList transToAddCopy = transToAdd.copy();
////  			HashSet<Integer> stateVisited = new HashSet<Integer>();
////  			stateVisited.add(nextState.getIndex());
////  			allTransToAddFired = allTransToAddFired(transToAddCopy, 
////  									allTransToAddFired, stateVisited, stateStackTop, lpnIndex);
////  		}
////		}
////		// Update the old Persistent of the next state 
////		if (!allTransToAddFired || cycleClosingMthdIndex == 1) {
////			nextPersistent.getPersistentSet().clear();
////			nextPersistent.getPersistentSet().addAll(transToAdd);
////			PersistentSetTbl.get(nextState).addAll(transToAdd);
////			printTransitionSet(nextPersistentNew, "nextPersistentNew:");
////			printTransitionSet(transToAdd, "transToAdd:");
////			System.out.println("((((((((((((((((((((()))))))))))))))))))))");
////		}
////		if (cycleClosingMthdIndex == 4) {
////			nextPersistent.getPersistentSet().clear();
////			nextPersistent.getPersistentSet().addAll(ignoredTrans);
////			PersistentSetTbl.get(nextState).addAll(ignoredTrans);
////			printTransitionSet(nextPersistentNew, "nextPersistentNew:");
////			printTransitionSet(transToAdd, "transToAdd:");
////			System.out.println("((((((((((((((((((((()))))))))))))))))))))");
////		}
////	}
////	// enabledSetTble stores the Persistent set at curState. 
////	// The fully enabled set at each state is stored in the tranVector in each state. 		
////	return (PersistentSet) nextPersistent;
//		return null;
// 	}
			
// 	private LpnTranList allIgnoredTransFired(LpnTranList ignoredTrans, 
// 			HashSet<Integer> stateVisited, PrjState stateStackEntry, int lpnIndex, StateGraph sg) {
// 		State state = stateStackEntry.get(lpnIndex);
// 		System.out.println("state = " + state.getIndex());
// 		State predecessor = stateStackEntry.getFather().get(lpnIndex);
// 		if (predecessor != null)
// 			System.out.println("predecessor = " + predecessor.getIndex());
// 		if (predecessor == null || stateVisited.contains(predecessor.getIndex())) {
// 			return ignoredTrans;
// 		}
// 		else 
// 			stateVisited.add(predecessor.getIndex());
// 		LpnTranList predecessorOldPersistent = sg.getEnabledSetTbl().get(predecessor);//enabledSetTbl.get(predecessor);
// 		for (Transition oldPersistentTran : predecessorOldPersistent) {
// 			State tmpState = sg.getNextStateMap().get(predecessor).get(oldPersistentTran);
// 			if (tmpState.getIndex() == state.getIndex()) {
// 				ignoredTrans.remove(oldPersistentTran);
// 				break;
// 			}
// 		}
// 		if (ignoredTrans.size()==0) {
// 			return ignoredTrans;
// 		}
// 		else {
// 			ignoredTrans = allIgnoredTransFired(ignoredTrans, stateVisited, stateStackEntry.getFather(), lpnIndex, sg);
// 		}
//		return ignoredTrans;
// 	}

	
	/**
	 * An iterative implement of findsg_recursive(). 
	 * @param lpnList
	 * @param curLocalStateArray
	 * @param enabledArray
	 */

	/**
	 * An iterative implement of findsg_recursive(). 
	 * @param lpnList
	 * @param curLocalStateArray
	 * @param enabledArray
	 */
	public Stack<State[]> search_dfs_noDisabling_fireOrder(final StateGraph[] lpnList, final State[] initStateArray) {

		boolean firingOrder = false;
		
		long peakUsedMem = 0;
		long peakTotalMem = 0;
		boolean failure = false;
		int tranFiringCnt = 0;
		int arraySize = lpnList.length;

		HashSet<PrjLpnState> globalStateTbl = new HashSet<PrjLpnState>();
		@SuppressWarnings("unchecked")
		IndexObjMap<LpnState>[]  lpnStateCache = new IndexObjMap[arraySize];
		//HashMap<LpnState, LpnState>[] lpnStateCache1 = new HashMap[arraySize];

		Stack<LpnState[]> stateStack = new Stack<LpnState[]>();
		Stack<LpnTranList[]> lpnTranStack = new Stack<LpnTranList[]>();
		
		//get initial enable transition set
		LpnTranList initEnabled = new LpnTranList();
		LpnTranList initFireFirst = new LpnTranList();
		LpnState[] initLpnStateArray = new LpnState[arraySize];
		for (int i = 0; i < arraySize; i++) 
		{
			lpnStateCache[i] = new IndexObjMap<LpnState>();
			LinkedList<Transition> enabledTrans = lpnList[i].getEnabled(initStateArray[i]);
			HashSet<Transition> enabledSet = new HashSet<Transition>();
			if(!enabledTrans.isEmpty())
			{
				for(Transition tran : enabledTrans) {
					enabledSet.add(tran);
					initEnabled.add(tran);
				}
			}
		    LpnState curLpnState = new LpnState(lpnList[i].getLpn(), initStateArray[i], enabledSet);
			lpnStateCache[i].add(curLpnState);
			initLpnStateArray[i] = curLpnState;
			
		}
		LpnTranList[] initEnabledSet = new LpnTranList[2];
		initEnabledSet[0] = initFireFirst;
		initEnabledSet[1] = initEnabled;
		lpnTranStack.push(initEnabledSet);


		stateStack.push(initLpnStateArray);
		globalStateTbl.add(new PrjLpnState(initLpnStateArray));
		
		main_while_loop: while (failure == false && stateStack.empty() == false) {
			long curTotalMem = Runtime.getRuntime().totalMemory();
			long curUsedMem = Runtime.getRuntime().totalMemory()
					- Runtime.getRuntime().freeMemory();

			if (curTotalMem > peakTotalMem)
				peakTotalMem = curTotalMem;

			if (curUsedMem > peakUsedMem)
				peakUsedMem = curUsedMem;
			
			if (stateStack.size() > max_stack_depth)
				max_stack_depth = stateStack.size();

			iterations++;
			//if(iterations>2)break;
			if (iterations % 100000 == 0)
				System.out.println("---> #iteration " + iterations
						+ "> # LPN transition firings: " + tranFiringCnt
						+ ", # of prjStates found: " + globalStateTbl.size()
						+ ", current_stack_depth: " + stateStack.size()
						+ ", total MDD nodes: " + mddMgr.nodeCnt()
						+ " used memory: " + (float) curUsedMem / 1000000
						+ " free memory: "
						+ (float) Runtime.getRuntime().freeMemory() / 1000000);

			
			LpnTranList[] curEnabled = lpnTranStack.peek();
			LpnState[] curLpnStateArray = stateStack.peek();

			// If all enabled transitions of the current LPN are considered,
			// then consider the next LPN
			// by increasing the curIndex.
			// Otherwise, if all enabled transitions of all LPNs are considered,
			// then pop the stacks.
			
			if(curEnabled[0].size()==0 && curEnabled[1].size()==0){
				lpnTranStack.pop();
				stateStack.pop();
				continue;
			}

			Transition firedTran = null;
			if(curEnabled[0].size() != 0)
				firedTran = curEnabled[0].removeFirst();
			else
				firedTran = curEnabled[1].removeFirst();

			traceCex.addLast(firedTran);
			
			State[] curStateArray = new State[arraySize];
			for( int i = 0; i < arraySize; i++)
				curStateArray[i] = curLpnStateArray[i].getState();
			StateGraph sg = null;
			for (int i=0; i<lpnList.length; i++) {
				if (lpnList[i].getLpn().equals(firedTran.getLpn())) {
					sg = lpnList[i];
				}
			}
			State[] nextStateArray = sg.fire(lpnList, curStateArray, firedTran);
			
			// Check if the firedTran causes disabling error or deadlock.
			@SuppressWarnings("unchecked")
			HashSet<Transition>[] extendedNextEnabledArray = new HashSet[arraySize];
			for (int i = 0; i < arraySize; i++) {				
				HashSet<Transition> curEnabledSet = curLpnStateArray[i].getEnabled();
				LpnTranList nextEnabledList = lpnList[i].getEnabled(nextStateArray[i]);
				HashSet<Transition> nextEnabledSet = new HashSet<Transition>();
				for(Transition tran : nextEnabledList) {
					nextEnabledSet.add(tran);
				}
				
				extendedNextEnabledArray[i] = nextEnabledSet;

				//non_disabling
				for(Transition curTran : curEnabledSet) {
					if(curTran == firedTran)
						continue;
										
					if(nextEnabledSet.contains(curTran) == false) {
						int[] nextMarking = nextStateArray[i].getMarking();
						// Not sure if the code below is correct. 
						int[] preset = lpnList[i].getLpn().getPresetIndex(curTran.getLabel());
						
						boolean included = true;
						if (preset != null && preset.length > 0) {
							for (int pp : preset) {
								boolean temp = false;
								for (int mi = 0; mi < nextMarking.length; mi++) {
									if (nextMarking[mi] == pp) {
										temp = true;
										break;
									}
								}
								if (temp == false)
								{
									included = false;
									break;
								}
							}
						}
						if(preset==null || preset.length==0 || included==true) {
							extendedNextEnabledArray[i].add(curTran);
						}
					}					
				}
			}

			boolean deadlock=true;
			for(int i = 0; i < arraySize; i++) {
				if(extendedNextEnabledArray[i].size() != 0){
					deadlock = false;
					break;
				}
			}
			
			if(deadlock==true) {
				failure = true;
				break main_while_loop;
			}
					
			
			// Add nextPrjState into prjStateSet
			// If nextPrjState has been traversed before, skip to the next
			// enabled transition.
			LpnState[] nextLpnStateArray = new LpnState[arraySize];
			for(int i = 0; i < arraySize; i++) {
				HashSet<Transition> lpnEnabledSet = new HashSet<Transition>();
				for(Transition tran : extendedNextEnabledArray[i]) {
					lpnEnabledSet.add(tran);
				}
				LpnState tmp = new LpnState(lpnList[i].getLpn(), nextStateArray[i], lpnEnabledSet);
				LpnState tmpCached = (lpnStateCache[i].add(tmp));
				nextLpnStateArray[i] = tmpCached; 
				
			}
			
			boolean newState = globalStateTbl.add(new PrjLpnState(nextLpnStateArray));
			
			if(newState == false) {
				traceCex.removeLast();
				continue;
			}
			
			stateStack.push(nextLpnStateArray);
		
			LpnTranList[] nextEnabledSet = new LpnTranList[2];
			LpnTranList fireFirstTrans = new LpnTranList();
			LpnTranList otherTrans = new LpnTranList();
			
			for(int i = 0; i < arraySize; i++)
			{
				for(Transition tran : nextLpnStateArray[i].getEnabled())
				{
					if(firingOrder == true)
						if(curLpnStateArray[i].getEnabled().contains(tran))
							otherTrans.add(tran);
						else
							fireFirstTrans.add(tran);
					else
						fireFirstTrans.add(tran);
				}
			}
			
			nextEnabledSet[0] = fireFirstTrans;
			nextEnabledSet[1] = otherTrans;
			lpnTranStack.push(nextEnabledSet);

		}// END while (stateStack.empty() == false)

		// graph.write(String.format("graph_%s_%s-tran_%s-state.gv",mode,tranFiringCnt,
		// prjStateSet.size()));
		System.out.println("SUMMARY: # LPN transition firings: "
				+ tranFiringCnt + ", # of prjStates found: "
				+ globalStateTbl.size() + ", max_stack_depth: " + max_stack_depth);

		/*
		 * by looking at stateStack, generate the trace showing the counter-exPersistent.
		 */
		if (failure == true) {
			System.out.println("-------------------------------------------");
			System.out.println("the deadlock trace:");
			//update traceCex from stateStack
//			LpnState[] cur = null;
//			LpnState[] next = null;
			for(Transition tran : traceCex)
				System.out.println(tran.getFullLabel());
		}
		
		System.out.println("Modules' local states: ");
		for (int i = 0; i < arraySize; i++) {
			System.out.println("module " + lpnList[i].getLpn().getLabel() + ": "
					+ lpnList[i].reachSize());
			
		}
		
		return null;
	}


	/**
	 * findsg using iterative approach based on DFS search. The states found are
	 * stored in MDD.
	 * 
	 * When a state is considered during DFS, only one enabled transition is
	 * selected to fire in an iteration.
	 * 
	 * @param lpnList
	 * @param curLocalStateArray
	 * @param enabledArray
	 * @return a linked list of a sequence of LPN transitions leading to the
	 *         failure if it is not empty.
	 */
	public Stack<State[]> search_dfs_mdd_1(final StateGraph[] lpnList, final State[] initStateArray) {
		System.out.println("---> calling function search_dfs_mdd_1");
				
		long peakUsedMem = 0;
		long peakTotalMem = 0;
		long peakMddNodeCnt = 0;
		int memUpBound = 500; // Set an upper bound of memory in MB usage to
								// trigger MDD compression.
		boolean compressed = false;
		boolean failure = false;
		int tranFiringCnt = 0;
		int totalStates = 1;
		int arraySize = lpnList.length;
		//int newStateCnt = 0;
		
		Stack<State[]> stateStack = new Stack<State[]>();
		Stack<LinkedList<Transition>> lpnTranStack = new Stack<LinkedList<Transition>>();
		Stack<Integer> curIndexStack = new Stack<Integer>();

		mddNode reachAll = null;
		mddNode reach = Mdd.newNode();
		
		int[] localIdxArray = Analysis.getLocalStateIdxArray(lpnList, initStateArray, true);
		mddMgr.add(reach, localIdxArray, compressed);
		
		stateStack.push(initStateArray);
		LpnTranList initEnabled = lpnList[0].getEnabled(initStateArray[0]);
		lpnTranStack.push(initEnabled.clone());
		curIndexStack.push(0);
		
		int numMddCompression = 0;
		
		main_while_loop: while (failure == false && stateStack.empty() == false) {

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
				long curMddNodeCnt = mddMgr.nodeCnt();
				peakMddNodeCnt = peakMddNodeCnt > curMddNodeCnt ? peakMddNodeCnt : curMddNodeCnt;
				
				System.out.println("---> #iteration " + iterations
						+ "> # LPN transition firings: " + tranFiringCnt
						+ ", # of prjStates found: " + totalStates
						+ ", stack depth: " + stateStack.size()
						+ ", total MDD nodes: " + curMddNodeCnt
						+ " used memory: " + (float) curUsedMem / 1000000
						+ " free memory: "
						+ (float) Runtime.getRuntime().freeMemory() / 1000000);

					if (curUsedMem >= memUpBound * 1000000) {
						mddMgr.compress(reach);
						numMddCompression++;
						if (reachAll == null)
							reachAll = reach;
						else {
							mddNode newReachAll = mddMgr.union(reachAll, reach);
							if (newReachAll != reachAll) {
								mddMgr.remove(reachAll);
								reachAll = newReachAll;
							}
						}
						mddMgr.remove(reach);
						reach = Mdd.newNode();

						if(memUpBound < 1500)
							memUpBound *= numMddCompression;
					}
			}

			State[] curStateArray = stateStack.peek();
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
					LpnTranList enabledCached = (lpnList[curIndex].getEnabled(curStateArray[curIndex]));
					if (enabledCached.size() > 0) {
						curEnabled = enabledCached.clone();
						lpnTranStack.push(curEnabled);
						curIndexStack.push(curIndex);
						break;
					} 
					curIndex++;
				}
			}

			if (curIndex == arraySize) {
				stateStack.pop();
				continue;
			}

			Transition firedTran = curEnabled.removeLast();
			State[] nextStateArray = lpnList[curIndex].fire(lpnList, curStateArray,firedTran);
			tranFiringCnt++;

			// Check if the firedTran causes disabling error or deadlock.
			List<LinkedList<Transition>> curEnabledArray = new ArrayList<LinkedList<Transition>>(); 
			List<LinkedList<Transition>> nextEnabledArray = new ArrayList<LinkedList<Transition>>(); 
			
			//LinkedList<Transition>[] curEnabledArray = new LinkedList[arraySize];
			//LinkedList<Transition>[] nextEnabledArray = new LinkedList[arraySize];
			for (int i = 0; i < arraySize; i++) {
				StateGraph lpn_tmp = lpnList[i];
				LinkedList<Transition> enabledList = lpn_tmp.getEnabled(curStateArray[i]);
				//curEnabledArray[i] = enabledList;
				curEnabledArray.add(i, enabledList);
				enabledList = lpn_tmp.getEnabled(nextStateArray[i]);
				//nextEnabledArray[i] = enabledList;
				nextEnabledArray.add(i, enabledList);
				
				Transition disabledTran = firedTran.disablingError(curEnabledArray.get(i),nextEnabledArray.get(i));
						
				if (disabledTran != null) {
					System.err.println("Disabling Error: "
							+ disabledTran.getFullLabel() + " is disabled by "
							+ firedTran.getFullLabel());
					failure = true;
					break main_while_loop;
				}
			}

			if (Analysis.deadLock(lpnList, nextStateArray) == true) {
				//System.out.println("*** Verification failed: deadlock.");
				failure = true;
				break main_while_loop;
			}

			/*
			 * Check if the local indices of nextStateArray already exist.
			 * if not, add it into reachable set, and push it onto stack.
			 */
			localIdxArray = Analysis.getLocalStateIdxArray(lpnList, nextStateArray, true);

			Boolean existingState = false;
			if (reachAll != null && Mdd.contains(reachAll, localIdxArray) == true)
				existingState = true;
			else if (Mdd.contains(reach, localIdxArray) == true)
				existingState = true;
			
			if (existingState == false) {
				mddMgr.add(reach, localIdxArray, compressed);
				//newStateCnt++;
				stateStack.push(nextStateArray);
				lpnTranStack.push((LpnTranList) nextEnabledArray.get(0).clone());
				curIndexStack.push(0);
				totalStates++;
			}
		}

		double totalStateCnt = Mdd.numberOfStates(reach);
		
		System.out.println("---> run statistics: \n"
				+ "# LPN transition firings: "	+ tranFiringCnt + "\n" 
				+ "# of prjStates found: " + totalStateCnt + "\n" 
				+ "max_stack_depth: " + max_stack_depth + "\n" 
				+ "peak MDD nodes: " + peakMddNodeCnt + "\n"
				+ "peak used memory: " + peakUsedMem / 1000000 + " MB\n"
				+ "peak total memory: " + peakTotalMem / 1000000 + " MB\n");

		return null;
	}

	/**
	 * findsg using iterative approach based on DFS search. The states found are
	 * stored in MDD.
	 * 
	 * It is similar to findsg_dfs_mdd_1 except that when a state is considered
	 * during DFS, all enabled transition are fired, and all its successor
	 * states are found in an iteration.
	 * 
	 * @param lpnList
	 * @param curLocalStateArray
	 * @param enabledArray
	 * @return a linked list of a sequence of LPN transitions leading to the
	 *         failure if it is not empty.
	 */
	public Stack<State[]> search_dfs_mdd_2(final StateGraph[] lpnList, final State[] initStateArray) {
		System.out.println("---> calling function search_dfs_mdd_2");

		int tranFiringCnt = 0;
		int totalStates = 0;
		int arraySize = lpnList.length;
		long peakUsedMem = 0;
		long peakTotalMem = 0;
		long peakMddNodeCnt = 0;
		int memUpBound = 1000; // Set an upper bound of memory in MB usage to
								// trigger MDD compression.
		boolean failure = false;

		MDT state2Explore = new MDT(arraySize);
		state2Explore.push(initStateArray);
		totalStates++;
		long peakState2Explore = 0;

		Stack<Integer> searchDepth = new Stack<Integer>();
		searchDepth.push(1);
		
		boolean compressed = false;
		mddNode reachAll = null;
		mddNode reach = Mdd.newNode();

		main_while_loop: 
		while (failure == false	&& state2Explore.empty() == false) {
			long curTotalMem = Runtime.getRuntime().totalMemory();
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

			if (curTotalMem > peakTotalMem)
				peakTotalMem = curTotalMem;

			if (curUsedMem > peakUsedMem)
				peakUsedMem = curUsedMem;

			iterations++;
			if (iterations % 100000 == 0) {
				int mddNodeCnt = mddMgr.nodeCnt();
				peakMddNodeCnt = peakMddNodeCnt > mddNodeCnt ? peakMddNodeCnt : mddNodeCnt;
				int state2ExploreSize = state2Explore.size();
				peakState2Explore = peakState2Explore > state2ExploreSize ? peakState2Explore : state2ExploreSize;
				
				System.out.println("---> #iteration " + iterations
						+ "> # LPN transition firings: " + tranFiringCnt
						+ ", # of prjStates found: " + totalStates
						+ ", # states to explore: " + state2ExploreSize
						+ ", # MDT nodes: " + state2Explore.nodeCnt()
						+ ", total MDD nodes: " + mddNodeCnt
						+ " used memory: " + (float) curUsedMem / 1000000
						+ " free memory: "
						+ (float) Runtime.getRuntime().freeMemory() / 1000000);

				if (curUsedMem >= memUpBound * 1000000) {
					mddMgr.compress(reach);
					if (reachAll == null)
						reachAll = reach;
					else {
						mddNode newReachAll = mddMgr.union(reachAll, reach);
						if (newReachAll != reachAll) {
							mddMgr.remove(reachAll);
							reachAll = newReachAll;
						}
					}
					mddMgr.remove(reach);
					reach = Mdd.newNode();
				}
			}
			
			State[] curStateArray = state2Explore.pop();
			State[] nextStateArray = null;
			
			int states2ExploreCurLevel = searchDepth.pop();
			if(states2ExploreCurLevel > 1) 
				searchDepth.push(states2ExploreCurLevel-1);
			
			int[] localIdxArray = Analysis.getLocalStateIdxArray(lpnList, curStateArray, false);
			mddMgr.add(reach, localIdxArray, compressed);
					
			int nextStates2Explore = 0;
			for (int index = arraySize - 1; index >= 0; index--) {
				StateGraph curLpn = lpnList[index];
				State curState = curStateArray[index];
				LinkedList<Transition> curEnabledSet = curLpn.getEnabled(curState);

				LpnTranList curEnabled = (LpnTranList) curEnabledSet.clone();
				while (curEnabled.size() > 0) {
					Transition firedTran = curEnabled.removeLast();
					// TODO: (check) Not sure if curLpn.fire is correct.
					nextStateArray = curLpn.fire(lpnList, curStateArray, firedTran);
					tranFiringCnt++;

					for (int i = 0; i < arraySize; i++) {
						StateGraph lpn_tmp = lpnList[i];
						if (curStateArray[i] == nextStateArray[i])
							continue;

						LinkedList<Transition> curEnabled_l = lpn_tmp.getEnabled(curStateArray[i]);
						LinkedList<Transition> nextEnabled = lpn_tmp.getEnabled(nextStateArray[i]);
						Transition disabledTran = firedTran.disablingError(curEnabled_l, nextEnabled);
						if (disabledTran != null) {
							System.err.println("Verification failed: disabling error: "
											+ disabledTran.getFullLabel()
											+ " disabled by "
											+ firedTran.getFullLabel() + "!!!");
							failure = true;
							break main_while_loop;
						}
					}

					if (Analysis.deadLock(lpnList, nextStateArray) == true) {
						System.err.println("Verification failed: deadlock.");
						failure = true;
						break main_while_loop;
					}

					/*
					 * Check if the local indices of nextStateArray already exist.
					 */
					localIdxArray = Analysis.getLocalStateIdxArray(lpnList, nextStateArray, false);

					Boolean existingState = false;
					if (reachAll != null && Mdd.contains(reachAll, localIdxArray) == true)
						existingState = true;
					else if (Mdd.contains(reach, localIdxArray) == true)
						existingState = true;
					else if(state2Explore.contains(nextStateArray)==true)
						existingState = true;

					if (existingState == false) {
						totalStates++;
						//mddMgr.add(reach, localIdxArray, compressed);
						state2Explore.push(nextStateArray);
						nextStates2Explore++;
					}
				}
			}
			if(nextStates2Explore > 0)
				searchDepth.push(nextStates2Explore);
		}

		System.out.println("-------------------------------------\n"
						+ "---> run statistics: \n"
						+ " # Depth of search (Length of Cex): " + searchDepth.size() + "\n"
						+ " # LPN transition firings: " + (double)tranFiringCnt/1000000 + " M\n"
						+ " # of prjStates found: " + (double)totalStates / 1000000 + " M\n"
						+ " peak states to explore : " + (double) peakState2Explore / 1000000 + " M\n"
						+ " peak MDD nodes: " + peakMddNodeCnt + "\n"
						+ " peak used memory: " + peakUsedMem / 1000000 + " MB\n"
						+ " peak total memory: " + peakTotalMem /1000000 + " MB\n"
						+ "_____________________________________");

		return null;
	}


	public LinkedList<Transition> search_bfs(final StateGraph[] sgList, final State[] initStateArray) {
		System.out.println("---> starting search_bfs");
		
		long peakUsedMem = 0;
		long peakTotalMem = 0;
		long peakMddNodeCnt = 0;
		int memUpBound = 1000; 	// Set an upper bound of memory in MB usage to
								// trigger MDD compression.
		
		int arraySize = sgList.length;

		for (int i = 0; i < arraySize; i++)
			sgList[i].addState(initStateArray[i]);
		
		mddNode reachSet = null;
		mddNode reach = Mdd.newNode();
		MDT frontier = new MDT(arraySize);
		MDT image = new MDT(arraySize);

		frontier.push(initStateArray);

		State[] curStateArray = null;
		int tranFiringCnt = 0;
		int totalStates = 0;
		int imageSize = 0;
		
		boolean verifyError = false;

		bfsWhileLoop: while (true) {
			if (verifyError == true)
				break;

			long curTotalMem = Runtime.getRuntime().totalMemory();
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

			if (curTotalMem > peakTotalMem)
				peakTotalMem = curTotalMem;

			if (curUsedMem > peakUsedMem)
				peakUsedMem = curUsedMem;

			long curMddNodeCnt = mddMgr.nodeCnt();
			peakMddNodeCnt = peakMddNodeCnt > curMddNodeCnt ? peakMddNodeCnt : curMddNodeCnt;
			
				iterations++;
				System.out.println("iteration " + iterations
						+ "> # LPN transition firings: " + tranFiringCnt 
						+ ", # of prjStates found: "	+ totalStates
						+ ", total MDD nodes: " + curMddNodeCnt
						+ " used memory: " + (float) curUsedMem / 1000000
						+ " free memory: "
						+ (float) Runtime.getRuntime().freeMemory() / 1000000);
				
				if (curUsedMem >= memUpBound * 1000000) {
					mddMgr.compress(reach);
					if (reachSet == null)
						reachSet = reach;
					else {
						mddNode newReachSet = mddMgr.union(reachSet, reach);
						if (newReachSet != reachSet) {
							mddMgr.remove(reachSet);
							reachSet = newReachSet;
						}
					}
					mddMgr.remove(reach);
					reach = Mdd.newNode();
				}
			
			while(frontier.empty() == false) {
				boolean deadlock = true;
				
//				Stack<State[]> curStateArrayList = frontier.pop();
//				while(curStateArrayList.empty() == false) {
//				curStateArray = curStateArrayList.pop();
				{
				 curStateArray = frontier.pop();
				int[] localIdxArray = Analysis.getLocalStateIdxArray(sgList, curStateArray, false);
				mddMgr.add(reach, localIdxArray, false);
				totalStates++;
				
				for (int i = 0; i < arraySize; i++) {
					LinkedList<Transition> curEnabled = sgList[i].getEnabled(curStateArray[i]);
					if (curEnabled.size() > 0)
						deadlock = false;
					
					for (Transition firedTran : curEnabled) {
						// TODO: (check) Not sure if sgList[i].fire is correct.
						State[] nextStateArray = sgList[i].fire(sgList, curStateArray, firedTran);
						tranFiringCnt++;
				
						/*
						 * Check if any transitions can be disabled by fireTran.
						 */
						LinkedList<Transition> nextEnabled = sgList[i].getEnabled(nextStateArray[i]);
						Transition disabledTran = firedTran.disablingError(curEnabled, nextEnabled);
						if (disabledTran != null) {
							System.err.println("*** Verification failed: disabling error: "
											+ disabledTran.getFullLabel()
											+ " disabled by "
											+ firedTran.getFullLabel() + "!!!");
							verifyError = true;
							break bfsWhileLoop;
						}
						
						localIdxArray = Analysis.getLocalStateIdxArray(sgList, nextStateArray, false);
						if (Mdd.contains(reachSet, localIdxArray) == false && Mdd.contains(reach, localIdxArray) == false && frontier.contains(nextStateArray) == false) {
							if(image.contains(nextStateArray)==false) {
							image.push(nextStateArray);
							imageSize++;
							}
						}
					}
				}
				}

				
				
				/*
				 * If curStateArray deadlocks (no enabled transitions), terminate.
				 */
				if (deadlock == true) {
					System.err.println("*** Verification failed: deadlock.");
					verifyError = true;
					break bfsWhileLoop;
				}
			}
			
			if(image.empty()==true) break;

			System.out.println("---> size of image: " + imageSize);

			frontier = image;		
			image = new MDT(arraySize);
			imageSize = 0;
		}

		System.out.println("---> final numbers: # LPN transition firings: " + tranFiringCnt / 1000000 + "M\n"
				+ "---> # of prjStates found: " + (double) totalStates / 1000000 + "M\n"
				+ "---> peak total memory: " + peakTotalMem / 1000000F + " MB\n" 
				+ "---> peak used memory: " + peakUsedMem / 1000000F + " MB\n" 
				+ "---> peak MDD nodes: " + peakMddNodeCnt);

		return null;
	}

	/**
	 * BFS findsg using iterative approach. THe states found are stored in MDD.
	 * 
	 * @param lpnList
	 * @param curLocalStateArray
	 * @param enabledArray
	 * @return a linked list of a sequence of LPN transitions leading to the
	 *         failure if it is not empty.
	 */
	public LinkedList<Transition> search_bfs_mdd_localFirings(final StateGraph[] lpnList, final State[] initStateArray) {
		System.out.println("---> starting search_bfs");
		
		long peakUsedMem = 0;
		long peakTotalMem = 0;

		int arraySize = lpnList.length;

		for (int i = 0; i < arraySize; i++)
			lpnList[i].addState(initStateArray[i]);
		
		// mddNode reachSet = mddMgr.newNode();
		// mddMgr.add(reachSet, curLocalStateArray);
		mddNode reachSet = null;
		mddNode exploredSet = null;
		List<LinkedList<State>> nextSetArray = new ArrayList<LinkedList<State>>(); 
		//LinkedList<State>[] nextSetArray = new LinkedList[arraySize];
		for (int i = 0; i < arraySize; i++)
			nextSetArray.add(i,new LinkedList<State>());

		mddNode initMdd = mddMgr.doLocalFirings(lpnList, initStateArray, null);
		mddNode curMdd = initMdd;
		reachSet = curMdd;
		mddNode nextMdd = null;

		int[] curStateArray = null;
		int tranFiringCnt = 0;

		boolean verifyError = false;

		bfsWhileLoop: while (true) {
			if (verifyError == true)
				break;

			long curTotalMem = Runtime.getRuntime().totalMemory();
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

			if (curTotalMem > peakTotalMem)
				peakTotalMem = curTotalMem;

			if (curUsedMem > peakUsedMem)
				peakUsedMem = curUsedMem;

			curStateArray = mddMgr.next(curMdd, curStateArray);

			if (curStateArray == null) {
				// Break the loop if no new next states are found.
				// System.out.println("nextSet size " + nextSet.size());
				if (nextMdd == null)
					break bfsWhileLoop;

				if (exploredSet == null)
					exploredSet = curMdd;
				else {
					mddNode newExplored = mddMgr.union(exploredSet, curMdd);
					if (newExplored != exploredSet)
						mddMgr.remove(exploredSet);
					exploredSet = newExplored;
				}

				mddMgr.remove(curMdd);
				curMdd = nextMdd;
				nextMdd = null;

				iterations++;
				System.out.println("iteration " + iterations
						+ "> # LPN transition firings: " + tranFiringCnt
						+ ", # of union calls: " + mddNode.numCalls
						+ ", # of union cache nodes: " + mddNode.cacheNodes
						+ ", total MDD nodes: " + mddMgr.nodeCnt()
						+ " used memory: " + (float) curUsedMem / 1000000
						+ " free memory: "
						+ (float) Runtime.getRuntime().freeMemory() / 1000000);
				System.out.println("---> # of prjStates found: " + Mdd.numberOfStates(reachSet) 
						+ ",  CurSet.Size = " + Mdd.numberOfStates(curMdd));
				continue;
			}

			if (exploredSet != null && Mdd.contains(exploredSet, curStateArray) == true)
				continue;

			// If curStateArray deadlocks (no enabled transitions), terminate.
			if (Analysis.deadLock(lpnList, curStateArray) == true) {
				System.err.println("*** Verification failed: deadlock.");
				verifyError = true;
				break bfsWhileLoop;
			}

			// Do firings of non-local LPN transitions.
			for (int index = arraySize - 1; index >= 0; index--) {
				StateGraph curLpn = lpnList[index];
				LinkedList<Transition> curLocalEnabled = curLpn.getEnabled(curStateArray[index]);

				if (curLocalEnabled.size() == 0 || curLocalEnabled.getFirst().isLocal() == true)
					continue;

				for (Transition firedTran : curLocalEnabled) {

					if (firedTran.isLocal() == true)
						continue;
					// TODO: (check) Not sure if curLpn.fire is correct.
					State[] nextStateArray = curLpn.fire(lpnList, curStateArray, firedTran);
					tranFiringCnt++;

					@SuppressWarnings("unused")
					ArrayList<LinkedList<Transition>> nextEnabledArray = new ArrayList<LinkedList<Transition>>(1);
					for (int i = 0; i < arraySize; i++) {
						if (curStateArray[i] == nextStateArray[i].getIndex())
							continue;

						StateGraph lpn_tmp = lpnList[i];
						LinkedList<Transition> curEnabledList = lpn_tmp.getEnabled(curStateArray[i]);
						LinkedList<Transition> nextEnabledList = lpn_tmp.getEnabled(nextStateArray[i].getIndex());

						Transition disabledTran = firedTran.disablingError(curEnabledList, nextEnabledList);
						if (disabledTran != null) {
							System.err.println("Verification failed: disabling error: "
											+ disabledTran.getFullLabel()
											+ ": is disabled by "
											+ firedTran.getFullLabel() + "!!!");
							verifyError = true;
							break bfsWhileLoop;
						}
					}

					if (Analysis.deadLock(lpnList, nextStateArray) == true) {
						//System.err.println("*** Verification failed: deadlock.");
						verifyError = true;
						break bfsWhileLoop;
					}

					// Add nextPrjState into prjStateSet
					// If nextPrjState has been traversed before, skip to the
					// next
					// enabled transition.
					int[] nextIdxArray = Analysis.getIdxArray(nextStateArray);
					if (reachSet != null && Mdd.contains(reachSet, nextIdxArray) == true)
						continue;

					mddNode newNextMdd = mddMgr.doLocalFirings(lpnList, nextStateArray, reachSet);

					mddNode newReachSet = mddMgr.union(reachSet, newNextMdd);
					if (newReachSet != reachSet)
						mddMgr.remove(reachSet);
					reachSet = newReachSet;

					if (nextMdd == null)
						nextMdd = newNextMdd;
					else {
						mddNode tmpNextMdd = mddMgr.union(nextMdd, newNextMdd);
						if (tmpNextMdd != nextMdd)
							mddMgr.remove(nextMdd);
						nextMdd = tmpNextMdd;
						mddMgr.remove(newNextMdd);
					}
				}
			}
		}

		System.out.println("---> final numbers: # LPN transition firings: "
				+ tranFiringCnt + "\n" + "---> # of prjStates found: "
				+ (Mdd.numberOfStates(reachSet)) + "\n"
				+ "---> peak total memory: " + peakTotalMem / 1000000F
				+ " MB\n" + "---> peak used memory: " + peakUsedMem / 1000000F
				+ " MB\n" + "---> peak MMD nodes: " + mddMgr.peakNodeCnt());

		return null;
	}

	
	/**
	 * partial order reduction (Original version of Hao's POR with behavioral analysis)
	 * This method is not used anywhere. See searchPOR_behavioral for POR with behavioral analysis. 
         * 
         * @param lpnList
         * @param curLocalStateArray
         * @param enabledArray
         */
	public Stack<State[]> search_dfs_por(final StateGraph[] lpnList, final State[] initStateArray, LPNTranRelation lpnTranRelation, String approach) {	
		System.out.println("---> Calling search_dfs with partial order reduction");
		
		long peakUsedMem = 0;
		long peakTotalMem = 0;
		double stateCount = 1;
		int max_stack_depth = 0;
		int iterations = 0;
		boolean useMdd = true;
		mddNode reach = Mdd.newNode();
				
		//init por
		backend.verification.platu.por1.AmpleSet ampleClass = new backend.verification.platu.por1.AmpleSet();
		//AmpleSubset ampleClass = new AmpleSubset();
		HashMap<Transition,HashSet<Transition>> indepTranSet = new HashMap<Transition,HashSet<Transition>>();
		
		if(approach == "state")
			indepTranSet = ampleClass.getIndepTranSet_FromState(lpnList, lpnTranRelation);
		else if (approach == "lpn")
			indepTranSet = ampleClass.getIndepTranSet_FromLPN(lpnList);
			
		System.out.println("finish get independent set!!!!");	
		
		boolean failure = false;
		int tranFiringCnt = 0;
		int arraySize = lpnList.length;

		HashSet<PrjState> stateStack = new HashSet<PrjState>();
		Stack<LpnTranList> lpnTranStack = new Stack<LpnTranList>();
		Stack<HashSet<Transition>> firedTranStack = new Stack<HashSet<Transition>>();  
		
		//get initial enable transition set
		@SuppressWarnings("unchecked")
		LinkedList<Transition>[] initEnabledArray = new LinkedList[arraySize];
		for (int i = 0; i < arraySize; i++) {
			lpnList[i].getLpn().setLpnIndex(i);
			initEnabledArray[i] = lpnList[i].getEnabled(initStateArray[i]);
		}
		
		
		//set initEnableSubset
		//LPNTranSet initEnableSubset = ampleClass.generateAmpleList(false, approach,initEnabledArray, indepTranSet);
		LpnTranList initEnableSubset = ampleClass.generateAmpleTranSet(initEnabledArray, indepTranSet);
		/*
		 * Initialize the reach state set with the initial state.
		 */
		HashSet<PrjState> prjStateSet = new HashSet<PrjState>();
		PrjState initPrjState = new PrjState(initStateArray);

		if (useMdd) {
			int[] initIdxArray = Analysis.getIdxArray(initStateArray);
			mddMgr.add(reach, initIdxArray, true);
		}
		else 
			prjStateSet.add(initPrjState);
		
		stateStack.add(initPrjState);
		PrjState stateStackTop = initPrjState;
		lpnTranStack.push(initEnableSubset);
		firedTranStack.push(new HashSet<Transition>());
				
		/*
		 * Start the main search loop.
		 */
		main_while_loop:
		while(failure == false && stateStack.size() != 0) {
			long curTotalMem = Runtime.getRuntime().totalMemory();
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			if (curTotalMem > peakTotalMem)
				peakTotalMem = curTotalMem;

			if (curUsedMem > peakUsedMem)
				peakUsedMem = curUsedMem;
			
			if (stateStack.size() > max_stack_depth)
				max_stack_depth = stateStack.size();

			iterations++;
			if (iterations % 500000 == 0) {
				if (useMdd==true)
					stateCount = Mdd.numberOfStates(reach);
				else
					stateCount = prjStateSet.size();
			
				System.out.println("---> #iteration " + iterations
						+ "> # LPN transition firings: " + tranFiringCnt
						+ ", # of prjStates found: " + stateCount
						+ ", max_stack_depth: " + max_stack_depth
						+ ", total MDD nodes: " + mddMgr.nodeCnt()
						+ " used memory: " + (float) curUsedMem / 1000000
						+ " free memory: "
						+ (float) Runtime.getRuntime().freeMemory() / 1000000);
			}

			State[] curStateArray = stateStackTop.toStateArray();
			LpnTranList curEnabled = lpnTranStack.peek();
			
//			for (LPNTran tran : curEnabled) 
//				for (int i = 0; i < arraySize; i++) 
//					if (lpnList[i] == tran.getLpn()) 
//						if (tran.isEnabled(curStateArray[i]) == false) {
//							System.out.println("transition " + tran.getFullLabel() + " not enabled in the current state");
//							System.exit(0);
//						}
			
			// If all enabled transitions of the current LPN are considered, then consider the next LPN
			// by increasing the curIndex.
			// Otherwise, if all enabled transitions of all LPNs are considered, then pop the stacks.
			if(curEnabled.size() == 0) {
				lpnTranStack.pop();
				firedTranStack.pop();
				stateStack.remove(stateStackTop);
				stateStackTop = stateStackTop.getFather();
				if (stateStack.size() > 0)
					traceCex.removeLast();
				continue;
			}
		
			Transition firedTran = curEnabled.removeFirst();
			firedTranStack.peek().add(firedTran);
			traceCex.addLast(firedTran);
			
			//System.out.println(tranFiringCnt + ": firedTran: "+ firedTran.getFullLabel());
			// TODO: (??) Not sure if the state graph sg below is correct.
			StateGraph sg = null;
			for (int i=0; i<lpnList.length; i++) {
				if (lpnList[i].getLpn().equals(firedTran.getLpn())) {
					sg = lpnList[i];
				}
			}
			State[] nextStateArray = sg.fire(lpnList, curStateArray, firedTran);
			tranFiringCnt++;
			
			// Check if the firedTran causes disabling error or deadlock.
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] curEnabledArray = new LinkedList[arraySize];
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] nextEnabledArray = new LinkedList[arraySize];
			for (int i = 0; i < arraySize; i++) {
				lpnList[i].getLpn().setLpnIndex(i);
				StateGraph lpn_tmp = lpnList[i];
				LinkedList<Transition> enabledList = lpn_tmp.getEnabled(curStateArray[i]);
				curEnabledArray[i] = enabledList;
				enabledList = lpn_tmp.getEnabled(nextStateArray[i]);
				nextEnabledArray[i] = enabledList;

				Transition disabledTran = firedTran.disablingError(curEnabledArray[i], nextEnabledArray[i]);
				if(disabledTran != null) {
					System.out.println("---> Disabling Error: " + disabledTran.getFullLabel() + " is disabled by " + firedTran.getFullLabel());

					System.out.println("Current state:");
					for(int ii = 0; ii < arraySize; ii++) {
						System.out.println("module " + lpnList[ii].getLpn().getLabel());
						System.out.println(curStateArray[ii]);
						System.out.println("Enabled set: " + curEnabledArray[ii]);
					}
					
					System.out.println("======================\nNext state:");
					for(int ii = 0; ii < arraySize; ii++) {
						System.out.println("module " + lpnList[ii].getLpn().getLabel());
						System.out.println(nextStateArray[ii]);
						System.out.println("Enabled set: " + nextEnabledArray[ii]);
					}
					System.out.println();

					failure = true;	
					break main_while_loop;
				}
			}
			
			if (Analysis.deadLock(lpnList, nextStateArray) == true) {
				System.out.println("---> Deadlock.");
//				System.out.println("Deadlock state:");
//				for(int ii = 0; ii < arraySize; ii++) {
//					System.out.println("module " + lpnList[ii].getLabel());
//					System.out.println(nextStateArray[ii]);
//					System.out.println("Enabled set: " + nextEnabledArray[ii]);
//				}
				
				failure = true;
				break main_while_loop;
			}
			
			/*
			// Add nextPrjState into prjStateSet
			// If nextPrjState has been traversed before, skip to the next
			// enabled transition.
			//exist cycle
			*/
			
			PrjState nextPrjState = new PrjState(nextStateArray);
			boolean isExisting = false;
			int[] nextIdxArray = null;
			if(useMdd==true)
				nextIdxArray = Analysis.getIdxArray(nextStateArray);
			
			if (useMdd == true) 
				isExisting = Mdd.contains(reach, nextIdxArray);
			else
				isExisting = prjStateSet.contains(nextPrjState);
			
			if (isExisting == false) {				
				if (useMdd == true) {
					
					mddMgr.add(reach, nextIdxArray, true);
				}
				else
					prjStateSet.add(nextPrjState);
				
				//get next enable transition set
				//LPNTranSet nextEnableSubset = ampleClass.generateAmpleList(false, approach, nextEnabledArray, indepTranSet);
				LpnTranList nextEnableSubset = ampleClass.generateAmpleTranSet(nextEnabledArray, indepTranSet);
				
//				LPNTranSet nextEnableSubset = new LPNTranSet();
//				for (int i = 0; i < arraySize; i++)
//					for (LPNTran tran : nextEnabledArray[i])
//						nextEnableSubset.addLast(tran);

				stateStack.add(nextPrjState);
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				stateStackTop = nextPrjState;
				lpnTranStack.push(nextEnableSubset);
				firedTranStack.push(new HashSet<Transition>());
					

//				for (int i = 0; i < arraySize; i++)
//					for (LPNTran tran : nextEnabledArray[i])
//						System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n");
//
//				for (LPNTran tran : nextEnableSubset)
//					System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n-----------------------------------------------------\n");
				
//				HashSet<LPNTran> allEnabledSet = new HashSet<LPNTran>();
//				for (int i = 0; i < arraySize; i++) {
//					for (LPNTran tran : nextEnabledArray[i]) {
//						allEnabledSet.add(tran);
//						System.out.print(tran.getFullLabel() + ", ");
//					}
//				}
//				
//				System.out.println("\n");
//				
//				if(nextEnableSubset.size() > 0) {
//				for (LPNTran tran : nextEnableSubset) {
//					if (allEnabledSet.contains(tran) == false) {
//						System.out.println("\n\n" + tran.getFullLabel() + " in reduced set but not enabled\n");
//						System.exit(0);
//					}
//					System.out.print(tran.getFullLabel() + ", ");
//				}
//				
//				System.out.println("\n # of states : " + prjStateSet.size() + "\n-----------------------------------------------------\n");
//				}
				continue;
			}
			
			/*
			 * Remove firedTran from traceCex if its successor state already exists.
			 */
			traceCex.removeLast();
			
			/*
			 * When firedTran forms a cycle in the state graph, consider all enabled transitions except those
			 * 1. already fired in the current state,
			 * 2. in the ample set of the next state.
			 */
			if(stateStack.contains(nextPrjState)==true && curEnabled.size()==0) {
				//System.out.println("formed a cycle......");
				
				LpnTranList original = new LpnTranList();
				LpnTranList reduced = new LpnTranList();
				
				//LPNTranSet nextStateAmpleSet = ampleClass.generateAmpleList(false, approach, nextEnabledArray, indepTranSet);
				LpnTranList nextStateAmpleSet = ampleClass.generateAmpleTranSet(nextEnabledArray, indepTranSet);
				
//				System.out.println("Back state's ample set:");
//				for(LPNTran tran : nextStateAmpleSet)
//					System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n");

				int enabledTranCnt = 0;
				LpnTranList[] tmp = new LpnTranList[arraySize];
				for (int i = 0; i < arraySize; i++) {
					tmp[i] = new LpnTranList();
					for (Transition tran : curEnabledArray[i]) {
						original.addLast(tran);
						if (firedTranStack.peek().contains(tran)==false && nextStateAmpleSet.contains(tran)==false) {
							tmp[i].addLast(tran);
							reduced.addLast(tran);
							enabledTranCnt++;
						}
					}
				}
				
				
				LpnTranList ampleSet = new LpnTranList();
				if(enabledTranCnt > 0)
					//ampleSet = ampleClass.generateAmpleList(false, approach, tmp, indepTranSet);
					 ampleSet = ampleClass.generateAmpleTranSet(tmp, indepTranSet);
				
				LpnTranList sortedAmpleSet = ampleSet;

				/*
				 * Sort transitions in ampleSet for better performance for MDD.
				 * Not needed for hash table.
				 */
				if (useMdd == true) {
					LpnTranList[] newCurEnabledArray = new LpnTranList[arraySize];
					for (int i = 0; i < arraySize; i++)
						newCurEnabledArray[i] = new LpnTranList();

					for (Transition tran : ampleSet)
						newCurEnabledArray[tran.getLpn().getLpnIndex()].addLast(tran);

					sortedAmpleSet = new LpnTranList();
					for (int i = 0; i < arraySize; i++) {
						LpnTranList localEnabledSet = newCurEnabledArray[i];
						for (Transition tran : localEnabledSet)
							sortedAmpleSet.addLast(tran);
					}
				}
				
				
//				for(LPNTran tran : original)
//					System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n");
//				
//				for(LPNTran tran : ampleSet)
//					System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n");
//				System.out.println("\n ---------111111-------------\n");
				
				
//				for(LPNTran tran : firedTranStack.peek())
//					allCurEnabled.remove(tran);
//
				lpnTranStack.pop();
				lpnTranStack.push(sortedAmpleSet);
				
//				for (int i = 0; i < arraySize; i++) {
//					for (LPNTran tran : curEnabledArray[i]) {
//						System.out.print(tran.getFullLabel() + ", ");
//					}
//				}
//				System.out.println("\n");
//				
//				for(LPNTran tran : allCurEnabled)
//					System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n-------------------------\n");
			}
				
			//System.out.println("Backtrack........\n");
		}//END while (stateStack.empty() == false)

		
		if (useMdd==true)
			stateCount = Mdd.numberOfStates(reach);
		else
			stateCount = prjStateSet.size();
		
		//long curTotalMem = Runtime.getRuntime().totalMemory();
		long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("SUMMARY: # LPN transition firings: " + tranFiringCnt
                + ", # of prjStates found: " + stateCount
                + ", max_stack_depth: " + max_stack_depth
                + ", used memory: " + (float) curUsedMem / 1000000
				+ ", free memory: "
				+ (float) Runtime.getRuntime().freeMemory() / 1000000);
        
        return null;
    }
	
	/**
	 * partial order reduction with behavioral analysis. (Adapted from search_dfs_por.)
         * 
         * @param sgList
         * @param curLocalStateArray
         * @param enabledArray
         */
	public StateGraph[] searchPOR_behavioral(final StateGraph[] sgList, final State[] initStateArray, LPNTranRelation lpnTranRelation, String approach) {	
		System.out.println("---> calling function searchPOR_behavioral");
		System.out.println("---> " + Options.getPOR());
		//System.out.println("---> " + Options.getCycleClosingMthd());
		//System.out.println("---> " + Options.getCycleClosingAmpleMethd());
		
		long peakUsedMem = 0;
		long peakTotalMem = 0;
		double stateCount = 1;
		int max_stack_depth = 0;
		int iterations = 0;
		//boolean useMdd = true;
		boolean useMdd = false;
		mddNode reach = Mdd.newNode();
				
		//init por
		AmpleSet ampleClass = new AmpleSet();
		//AmpleSubset ampleClass = new AmpleSubset();
		HashMap<Transition,HashSet<Transition>> indepTranSet = new HashMap<Transition,HashSet<Transition>>();
		
		if(approach == "state")
			indepTranSet = ampleClass.getIndepTranSet_FromState(sgList, lpnTranRelation);
		else if (approach == "lpn")
			indepTranSet = ampleClass.getIndepTranSet_FromLPN(sgList);
			
		System.out.println("finish get independent set!!!!");	
		
		boolean failure = false;
		int tranFiringCnt = 0;
		int arraySize = sgList.length;

		HashSet<PrjState> stateStack = new HashSet<PrjState>();
		Stack<LpnTranList> lpnTranStack = new Stack<LpnTranList>();
		Stack<HashSet<Transition>> firedTranStack = new Stack<HashSet<Transition>>();  
		
		//get initial enable transition set
		@SuppressWarnings("unchecked")
		LinkedList<Transition>[] initEnabledArray = new LinkedList[arraySize];
		for (int i = 0; i < arraySize; i++) {
			sgList[i].getLpn().setLpnIndex(i);
			initEnabledArray[i] = sgList[i].getEnabled(initStateArray[i]);
		}
				
		//set initEnableSubset
		//LPNTranSet initEnableSubset = ampleClass.generateAmpleList(false, approach,initEnabledArray, indepTranSet);
		LpnTranList initEnableSubset = ampleClass.generateAmpleTranSet(initEnabledArray, indepTranSet);
		/*
		 * Initialize the reach state set with the initial state.
		 */
		HashSet<PrjState> prjStateSet = new HashSet<PrjState>();
		PrjState initPrjState = new PrjState(initStateArray);

		if (useMdd) {
			int[] initIdxArray = Analysis.getIdxArray(initStateArray);
			mddMgr.add(reach, initIdxArray, true);
		}
		else 
			prjStateSet.add(initPrjState);
		
		stateStack.add(initPrjState);
		PrjState stateStackTop = initPrjState;
		lpnTranStack.push(initEnableSubset);
		firedTranStack.push(new HashSet<Transition>());
				
		/*
		 * Start the main search loop.
		 */
		main_while_loop:
		while(failure == false && stateStack.size() != 0) {
			long curTotalMem = Runtime.getRuntime().totalMemory();
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			if (curTotalMem > peakTotalMem)
				peakTotalMem = curTotalMem;

			if (curUsedMem > peakUsedMem)
				peakUsedMem = curUsedMem;
			
			if (stateStack.size() > max_stack_depth)
				max_stack_depth = stateStack.size();

			iterations++;
			if (iterations % 500000 == 0) {
				if (useMdd==true)
					stateCount = Mdd.numberOfStates(reach);
				else
					stateCount = prjStateSet.size();
			
				System.out.println("---> #iteration " + iterations
						+ "> # LPN transition firings: " + tranFiringCnt
						+ ", # of prjStates found: " + stateCount
						+ ", max_stack_depth: " + max_stack_depth
						+ ", total MDD nodes: " + mddMgr.nodeCnt()
						+ " used memory: " + (float) curUsedMem / 1000000
						+ " free memory: "
						+ (float) Runtime.getRuntime().freeMemory() / 1000000);
			}

			State[] curStateArray = stateStackTop.toStateArray();
			LpnTranList curEnabled = lpnTranStack.peek();
			
//			for (LPNTran tran : curEnabled) 
//				for (int i = 0; i < arraySize; i++) 
//					if (lpnList[i] == tran.getLpn()) 
//						if (tran.isEnabled(curStateArray[i]) == false) {
//							System.out.println("transition " + tran.getFullLabel() + " not enabled in the current state");
//							System.exit(0);
//						}
			
			// If all enabled transitions of the current LPN are considered, then consider the next LPN
			// by increasing the curIndex.
			// Otherwise, if all enabled transitions of all LPNs are considered, then pop the stacks.
			if(curEnabled.size() == 0) {
				lpnTranStack.pop();
				firedTranStack.pop();
				stateStack.remove(stateStackTop);
				stateStackTop = stateStackTop.getFather();
				if (stateStack.size() > 0)
					traceCex.removeLast();
				continue;
			}
		
			Transition firedTran = curEnabled.removeFirst();
			firedTranStack.peek().add(firedTran);
			traceCex.addLast(firedTran);
			
			StateGraph sg = null;
			for (int i=0; i<sgList.length; i++) {
				if (sgList[i].getLpn().equals(firedTran.getLpn())) {
					sg = sgList[i];
				}
			}
			State[] nextStateArray = sg.fire(sgList, curStateArray, firedTran);
			tranFiringCnt++;
			
			// Check if the firedTran causes disabling error or deadlock.
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] curEnabledArray = new LinkedList[arraySize];
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] nextEnabledArray = new LinkedList[arraySize];
			for (int i = 0; i < arraySize; i++) {
				sgList[i].getLpn().setLpnIndex(i);
				StateGraph lpn_tmp = sgList[i];
				LinkedList<Transition> enabledList = lpn_tmp.getEnabled(curStateArray[i]);
				curEnabledArray[i] = enabledList;
				enabledList = lpn_tmp.getEnabled(nextStateArray[i]);
				nextEnabledArray[i] = enabledList;

				Transition disabledTran = firedTran.disablingError(curEnabledArray[i], nextEnabledArray[i]);
				if(disabledTran != null) {
					System.out.println("---> Disabling Error: " + disabledTran.getFullLabel() + " is disabled by " + firedTran.getFullLabel());

					System.out.println("Current state:");
					for(int ii = 0; ii < arraySize; ii++) {
						System.out.println("module " + sgList[ii].getLpn().getLabel());
						System.out.println(curStateArray[ii]);
						System.out.println("Enabled set: " + curEnabledArray[ii]);
					}
					
					System.out.println("======================\nNext state:");
					for(int ii = 0; ii < arraySize; ii++) {
						System.out.println("module " + sgList[ii].getLpn().getLabel());
						System.out.println(nextStateArray[ii]);
						System.out.println("Enabled set: " + nextEnabledArray[ii]);
					}
					System.out.println();

					failure = true;	
					break main_while_loop;
				}
			}
			
			if (Analysis.deadLock(sgList, nextStateArray) == true) {
				System.out.println("---> Deadlock.");
//				System.out.println("Deadlock state:");
//				for(int ii = 0; ii < arraySize; ii++) {
//					System.out.println("module " + lpnList[ii].getLabel());
//					System.out.println(nextStateArray[ii]);
//					System.out.println("Enabled set: " + nextEnabledArray[ii]);
//				}
				
				failure = true;
				break main_while_loop;
			}
			
			/*
			// Add nextPrjState into prjStateSet
			// If nextPrjState has been traversed before, skip to the next
			// enabled transition.
			//exist cycle
			*/
			
			PrjState nextPrjState = new PrjState(nextStateArray);
			boolean isExisting = false;
			int[] nextIdxArray = null;
			if(useMdd==true)
				nextIdxArray = Analysis.getIdxArray(nextStateArray);
			
			if (useMdd == true) 
				isExisting = Mdd.contains(reach, nextIdxArray);
			else
				isExisting = prjStateSet.contains(nextPrjState);
			
			if (isExisting == false) {			
				if (useMdd == true) {
					
					mddMgr.add(reach, nextIdxArray, true);
				}
				else
					prjStateSet.add(nextPrjState);
				
				//get next enable transition set
				//LPNTranSet nextEnableSubset = ampleClass.generateAmpleList(false, approach, nextEnabledArray, indepTranSet);
				LpnTranList nextEnableSubset = ampleClass.generateAmpleTranSet(nextEnabledArray, indepTranSet);
				
//				LPNTranSet nextEnableSubset = new LPNTranSet();
//				for (int i = 0; i < arraySize; i++)
//					for (LPNTran tran : nextEnabledArray[i])
//						nextEnableSubset.addLast(tran);

				stateStack.add(nextPrjState);
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				stateStackTop = nextPrjState;
				lpnTranStack.push(nextEnableSubset);
				firedTranStack.push(new HashSet<Transition>());
					

//				for (int i = 0; i < arraySize; i++)
//					for (LPNTran tran : nextEnabledArray[i])
//						System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n");
//
//				for (LPNTran tran : nextEnableSubset)
//					System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n-----------------------------------------------------\n");
				
//				HashSet<LPNTran> allEnabledSet = new HashSet<LPNTran>();
//				for (int i = 0; i < arraySize; i++) {
//					for (LPNTran tran : nextEnabledArray[i]) {
//						allEnabledSet.add(tran);
//						System.out.print(tran.getFullLabel() + ", ");
//					}
//				}
//				
//				System.out.println("\n");
//				
//				if(nextEnableSubset.size() > 0) {
//				for (LPNTran tran : nextEnableSubset) {
//					if (allEnabledSet.contains(tran) == false) {
//						System.out.println("\n\n" + tran.getFullLabel() + " in reduced set but not enabled\n");
//						System.exit(0);
//					}
//					System.out.print(tran.getFullLabel() + ", ");
//				}
//				
//				System.out.println("\n # of states : " + prjStateSet.size() + "\n-----------------------------------------------------\n");
//				}
				continue;
			}
			
			/*
			 * Remove firedTran from traceCex if its successor state already exists.
			 */
			traceCex.removeLast();
			
			/*
			 * When firedTran forms a cycle in the state graph, consider all enabled transitions except those
			 * 1. already fired in the current state,
			 * 2. in the ample set of the next state.
			 */
			if(stateStack.contains(nextPrjState)==true && curEnabled.size()==0) {
				//System.out.println("formed a cycle......");
				
				LpnTranList original = new LpnTranList();
				LpnTranList reduced = new LpnTranList();
				
				//LPNTranSet nextStateAmpleSet = ampleClass.generateAmpleList(false, approach, nextEnabledArray, indepTranSet);
				LpnTranList nextStateAmpleSet = ampleClass.generateAmpleTranSet(nextEnabledArray, indepTranSet);
				
//				System.out.println("Back state's ample set:");
//				for(LPNTran tran : nextStateAmpleSet)
//					System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n");

				int enabledTranCnt = 0;
				LpnTranList[] tmp = new LpnTranList[arraySize];
				for (int i = 0; i < arraySize; i++) {
					tmp[i] = new LpnTranList();
					for (Transition tran : curEnabledArray[i]) {
						original.addLast(tran);
						if (firedTranStack.peek().contains(tran)==false && nextStateAmpleSet.contains(tran)==false) {
							tmp[i].addLast(tran);
							reduced.addLast(tran);
							enabledTranCnt++;
						}
					}
				}
				
				
				LpnTranList ampleSet = new LpnTranList();
				if(enabledTranCnt > 0)
					//ampleSet = ampleClass.generateAmpleList(false, approach, tmp, indepTranSet);
					 ampleSet = ampleClass.generateAmpleTranSet(tmp, indepTranSet);
				
				LpnTranList sortedAmpleSet = ampleSet;

				/*
				 * Sort transitions in ampleSet for better performance for MDD.
				 * Not needed for hash table.
				 */
				if (useMdd == true) {
					LpnTranList[] newCurEnabledArray = new LpnTranList[arraySize];
					for (int i = 0; i < arraySize; i++)
						newCurEnabledArray[i] = new LpnTranList();

					for (Transition tran : ampleSet)
						newCurEnabledArray[tran.getLpn().getLpnIndex()].addLast(tran);

					sortedAmpleSet = new LpnTranList();
					for (int i = 0; i < arraySize; i++) {
						LpnTranList localEnabledSet = newCurEnabledArray[i];
						for (Transition tran : localEnabledSet)
							sortedAmpleSet.addLast(tran);
					}
				}
				
				
//				for(LPNTran tran : original)
//					System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n");
//				
//				for(LPNTran tran : ampleSet)
//					System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n");
//				System.out.println("\n ---------111111-------------\n");
				
				
//				for(LPNTran tran : firedTranStack.peek())
//					allCurEnabled.remove(tran);
//
				lpnTranStack.pop();
				lpnTranStack.push(sortedAmpleSet);
				
//				for (int i = 0; i < arraySize; i++) {
//					for (LPNTran tran : curEnabledArray[i]) {
//						System.out.print(tran.getFullLabel() + ", ");
//					}
//				}
//				System.out.println("\n");
//				
//				for(LPNTran tran : allCurEnabled)
//					System.out.print(tran.getFullLabel() + ", ");
//				System.out.println("\n-------------------------\n");
			}
				
			//System.out.println("Backtrack........\n");
		}//END while (stateStack.empty() == false)

		
		if (useMdd==true)
			stateCount = Mdd.numberOfStates(reach);
		else
			stateCount = prjStateSet.size();
		
		//long curTotalMem = Runtime.getRuntime().totalMemory();
		long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("SUMMARY: # LPN transition firings: " + tranFiringCnt
                + ", # of prjStates found: " + stateCount
                + ", max_stack_depth: " + max_stack_depth
                + ", used memory: " + (float) curUsedMem / 1000000
				+ ", free memory: "
				+ (float) Runtime.getRuntime().freeMemory() / 1000000);
        
        return sgList;
    }
	
//	/**
//	 * Check if this project deadlocks in the current state 'stateArray'.	
//	 * @param sgList
//	 * @param stateArray
//	 * @param staticSetsMap 
//	 * @param enableSet 
//	 * @param disableByStealingToken 
//	 * @param disableSet 
//	 * @param init 
//	 * @return
//	 */
//	// Called by search search_dfsPOR
//	public boolean deadLock(StateGraph[] sgList, State[] stateArray, HashMap<Transition,StaticSets> staticSetsMap, 
//							boolean init, HashMap<Transition, Integer> tranFiringFreq, HashSet<PrjState> stateStack, PrjState stateStackTop, int cycleClosingMethdIndex) {
//		boolean deadlock = true;
//		System.out.println("@ deadlock:");
////		for (int i = 0; i < stateArray.length; i++) {
////			LinkedList<Transition> tmp = getAmple(stateArray, null, staticSetsMap, init, tranFiringFreq, sgList, stateStack, stateStackTop, cycleClosingMethdIndex).getAmpleSet();
////			if (tmp.size() > 0) {
////				deadlock = false;
////				break;
////			}
////		}
//		
//		LinkedList<Transition> tmp = getAmple(stateArray, null, staticSetsMap, init, tranFiringFreq, sgList, stateStack, stateStackTop, cycleClosingMethdIndex).getAmpleSet();
//		if (tmp.size() > 0) {
//			deadlock = false;
//		}
//		System.out.println("@ end of deadlock");
//		return deadlock;
//	}

	public static boolean deadLock(StateGraph[] lpnArray, State[] stateArray) {
		boolean deadlock = true;
		for (int i = 0; i < stateArray.length; i++) {
			LinkedList<Transition> tmp = lpnArray[i].getEnabled(stateArray[i]);
			if (tmp.size() > 0) {
				deadlock = false;
				break;
			}
		}
		return deadlock;
	}
	
	public static boolean deadLock(StateGraph[] lpnArray, int[] stateIdxArray) {
		boolean deadlock = true;
		for (int i = 0; i < stateIdxArray.length; i++) {
			LinkedList<Transition> tmp = lpnArray[i].getEnabled(stateIdxArray[i]);
			if (tmp.size() > 0) {
				deadlock = false;
				break;
			}
		}

		return deadlock;
	}
	
	public static boolean deadLock(List<LinkedList<Transition>> lpnList) {
		boolean deadlock = true;
		for (int i = 0; i < lpnList.size(); i++) {
			LinkedList<Transition> tmp = lpnList.get(i);
			if (tmp.size() > 0) {
				deadlock = false;
				break;
			}
		}
		return deadlock;
	}

//	/*
//	 * Scan enabledArray, identify all sticky transitions other the firedTran, and return them.
//	 * 
//	 * Arguments remain constant.
//	 */
//	
//	public static LpnTranList[] getStickyTrans(LpnTranList[] enabledArray, Transition firedTran) {
//		int arraySize = enabledArray.length;
//		LpnTranList[] stickyTranArray = new LpnTranList[arraySize];
//		for (int i = 0; i < arraySize; i++) {
//			stickyTranArray[i] = new LpnTranList();
//			for (Transition tran : enabledArray[i]) {
//				if (tran != firedTran) 
//					stickyTranArray[i].add(tran);
//			}
//			
//			if(stickyTranArray[i].size()==0)
//				stickyTranArray[i] = null;
//		}
//		return stickyTranArray;
//	}
	
	/**
	 * Identify if any sticky transitions in currentStickyTransArray can  existing in the nextState. If so, add them to 
	 * nextStickyTransArray.
	 * 
	 * Arguments:  curStickyTransArray and nextState are constant, nextStickyTransArray may be added with sticky transitions
	 * from curStickyTransArray.
	 * 
	 *  Return: sticky transitions from curStickyTransArray that are not marking disabled in nextState.
	 */
	public static LpnTranList[] checkStickyTrans(
			LpnTranList[] curStickyTransArray, LpnTranList[] nextEnabledArray, 
			LpnTranList[] nextStickyTransArray, State nextState, LPN LPN) {
		int arraySize = curStickyTransArray.length;
		LpnTranList[] stickyTransArray = new LpnTranList[arraySize];
		boolean[] hasStickyTrans = new boolean[arraySize];
		
		for (int i = 0; i < arraySize; i++) {
			HashSet<Transition> tmp = new HashSet<Transition>();
			if(nextStickyTransArray[i] != null)
				for(Transition tran : nextStickyTransArray[i])
					tmp.add(tran);
			
			
			stickyTransArray[i] = new LpnTranList();
			hasStickyTrans[i] = false;
			for (Transition tran : curStickyTransArray[i]) {
				if (tran.isPersistent() == true && tmp.contains(tran)==false) {
					int[] nextMarking = nextState.getMarking();
					int[] preset = LPN.getPresetIndex(tran.getLabel());//tran.getPreSet();
					boolean included = false;
					if (preset != null && preset.length > 0) {
						for (int pp : preset) {
							for (int mi = 0; i < nextMarking.length; i++) {
								if (nextMarking[mi] == pp) {
									included = true;
									break;
								}
							}
							if (included == false)
								break;
						}
					}
					if(preset==null || preset.length==0 || included==true) {
						stickyTransArray[i].add(tran);
						hasStickyTrans[i] = true;
					}
				}
			}
			
			if(stickyTransArray[i].size()==0)
				stickyTransArray[i] = null;
		}
		
		return stickyTransArray;
	}
	
	/*
	 * Return an array of indices for the given stateArray.
	 */
	private static int[] getIdxArray(State[] stateArray) {
		int[] idxArray = new int[stateArray.length];
		
		for(int i = 0; i < stateArray.length; i++) {
			idxArray[i] = stateArray[i].getIndex();
		}
		return idxArray;
	}
	
	private static int[] getLocalStateIdxArray(StateGraph[] sgList, State[] stateArray, boolean reverse) {
		int arraySize = sgList.length;
		int[] localIdxArray = new int[arraySize];
		
		for(int i = 0; i < arraySize; i++) {
			if(reverse == false)
				localIdxArray[i] = sgList[i].getLocalState(stateArray[i]).getIndex();
			else
				localIdxArray[arraySize - i - 1] = sgList[i].getLocalState(stateArray[i]).getIndex();

			//System.out.print(localIdxArray[i] + " ");
		}
		//System.out.println();
		return localIdxArray;
	}
	
    private static void printStrongStubbornSetTbl(StateGraph[] sgList) {
    	for (int i=0; i<sgList.length; i++) {
    		System.out.println("******* Stored strong stubborn sets for state graph " + sgList[i].getLpn().getLabel() + " *******");
    		for (State s : sgList[i].getEnabledSetTbl().keySet()) {
        		System.out.print(s.getFullLabel() + " -> ");
        		printTransList(sgList[i].getEnabledSetTbl().get(s), "");
        	}
    	}		
	}
    
    private HashSet<Transition> computeStrongStubbornSet(State[] curStateArray,
    		HashSet<Transition> curEnabled, HashMap<Transition,Integer> tranFiringFreq, StateGraph[] sgList) {
    	if (curEnabled.size() == 1) 
    		return curEnabled;
    	HashSet<Transition> ready = new HashSet<Transition>();
//    	for (Transition enabledTran : curEnabled) {
//    		if (staticMap.get(enabledTran).getOtherTransDisableCurTranSet().isEmpty()) {
//    			ready.add(enabledTran);
//    			return ready;
//    		}				
//    	}
    	DependentSetComparator depComp = new DependentSetComparator(tranFiringFreq, sgList.length-1); 
    	PriorityQueue<DependentSet> dependentSetQueue = new PriorityQueue<DependentSet>(curEnabled.size(), depComp);
    	for (Transition enabledTran : curEnabled) {
    		if (Options.getDebugMode())
    			System.out.println("@ beginning of partialOrderReduction, consider seed transition " + enabledTran.getFullLabel());
    		HashSet<Transition> dependent = new HashSet<Transition>();
    		boolean enabledIsDummy = false;
    		boolean isCycleClosingStrongStubbornComputation = false;
    		dependent = computeDependent(curStateArray,enabledTran,dependent,curEnabled,isCycleClosingStrongStubbornComputation);
    		if (Options.getDebugMode())
    			printIntegerSet(dependent, "@ end of partialOrderReduction, dependent set for enabled transition "
    					+ enabledTran.getFullLabel());
    		// TODO: Immediate transition should have 0 delay, not "null" delay.
//    		// Check if the computed dependent set contains both immediate and non-immediate transitions. 
//    		boolean dependentOnlyHasImmediateTrans = false;
//    		boolean dependentOnlyHasNonImmediateTrans = false;
//    		for (Transition tr: dependent) {
//    			if (!dependentOnlyHasImmediateTrans && tr.getDelayTree() == null) {
//    				dependentOnlyHasImmediateTrans = true;
//    			}
//    			if (!dependentOnlyHasNonImmediateTrans && tr.getDelayTree() != null) {
//    				dependentOnlyHasNonImmediateTrans = true;
//    			}    			
//    		}
//    		if (dependentOnlyHasNonImmediateTrans && dependentOnlyHasImmediateTrans) {
//    			System.err.println("*** Error: Non-immediate transitions are dependent on immediate ones. ***");
//    			System.out.println("dependent set: ");
//    			for (Transition tr : dependent) {
//    				System.out.println(tr.getFullLabel());
//    			}
//    			System.out.println();
//    		}
    		// TODO: temporarily dealing with dummy transitions (This requires the dummy transitions to have "_dummy" in their names.)				
    		if(isDummyTran(enabledTran.getLabel()))
    			enabledIsDummy = true;
    		DependentSet dependentSet = new DependentSet(dependent, enabledTran, enabledIsDummy);
    		dependentSetQueue.add(dependentSet);
    	}    	
    	ready = dependentSetQueue.poll().getDependent();

//    		for (Transition enabledTran : curEnabled) {
//    			if (Options.getDebugMode())
//    				System.out.print("@ beginning of partialOrderReduction, consider seed transition " + getNamesOfLPNandTrans(enabledTran));    			
//    			HashSet<Transition> dependent = new HashSet<Transition>();
//    			boolean isCycleClosingPersistentComputation = false;
//    			dependent = computeDependent(curStateArray,enabledTran,dependent,curEnabled,isCycleClosingPersistentComputation);
//    			if (Options.getDebugMode()) {
//    				printIntegerSet(dependent, "@ end of partialOrderReduction, dependent set for enabled transition "
//    						+ getNamesOfLPNandTrans(enabledTran));						
//    			}
//    			if (ready.isEmpty() || dependent.size() < ready.size())
//    				ready = dependent;
//    			if (ready.size() == 1) {
//    				cachedNecessarySets.clear();
//    				return ready;
//    			}	
//    		}
    	cachedNecessarySets.clear();
    	return ready;
    }

// 	private HashSet<Transition> partialOrderReduction(State[] curStateArray,
//			HashSet<Transition> curEnabled, HashMap<Transition, StaticSets> staticMap, 
//			HashMap<Transition,Integer> tranFiringFreqMap, StateGraph[] sgList, LhpnFile[] lpnList) {
//		if (curEnabled.size() == 1) 
//			return curEnabled;
//		HashSet<Transition> ready = new HashSet<Transition>();
//		for (Transition enabledTran : curEnabled) {
//			if (staticMap.get(enabledTran).getOtherTransDisableCurTranSet().isEmpty()) {
//				ready.add(enabledTran);
//				return ready;
//			}				
//		}		
//		if (Options.getUseDependentQueue()) {
//			DependentSetComparator depComp = new DependentSetComparator(tranFiringFreqMap); 
//			PriorityQueue<DependentSet> dependentSetQueue = new PriorityQueue<DependentSet>(curEnabled.size(), depComp);
//			for (Transition enabledTran : curEnabled) {
//				if (Options.getDebugMode()){
//					writeStringWithEndOfLineToPORDebugFile("@ beginning of partialOrderReduction, consider seed transition " + getNamesOfLPNandTrans(enabledTran));
//				}
//				HashSet<Transition> dependent = new HashSet<Transition>();
//				boolean enabledIsDummy = false;
//				boolean isCycleClosingPersistentComputation = false;
//				dependent = computeDependent(curStateArray,enabledTran,dependent,curEnabled,staticMap,isCycleClosingPersistentComputation);
//				if (Options.getDebugMode()) {
//					writeIntegerSetToPORDebugFile(dependent, "@ end of partialOrderReduction, dependent set for enabled transition "
//							+ getNamesOfLPNandTrans(enabledTran));						
//				}
//				// TODO: temporarily dealing with dummy transitions (This requires the dummy transitions to have "_dummy" in their names.)				
//				if(isDummyTran(enabledTran.getName()))
//					enabledIsDummy = true;
//				DependentSet dependentSet = new DependentSet(dependent, enabledTran, enabledIsDummy);
//				dependentSetQueue.add(dependentSet);
//			}
//			//cachedNecessarySets.clear();
//			ready = dependentSetQueue.poll().getDependent();
//			//return ready;
//		}
//		else {
//			for (Transition enabledTran : curEnabled) {
//				if (Options.getDebugMode()){
//					writeStringWithEndOfLineToPORDebugFile("@ beginning of partialOrderReduction, consider seed transition " + getNamesOfLPNandTrans(enabledTran));
//				}
//				HashSet<Transition> dependent = new HashSet<Transition>();
//				boolean isCycleClosingPersistentComputation = false;
//				dependent = computeDependent(curStateArray,enabledTran,dependent,curEnabled,staticMap,isCycleClosingPersistentComputation);
//				if (Options.getDebugMode()) {
//					writeIntegerSetToPORDebugFile(dependent, "@ end of partialOrderReduction, dependent set for enabled transition "
//							+ getNamesOfLPNandTrans(enabledTran));						
//				}
//				if (ready.isEmpty() || dependent.size() < ready.size())
//					ready = dependent;//(HashSet<Transition>) dependent.clone();
//				if (ready.size() == 1) {
//					cachedNecessarySets.clear();
//					return ready;
//				}	
//				
//			}
//		}
//		cachedNecessarySets.clear();
//		return ready;
//	}

	private static boolean isDummyTran(String tranName) {
		if (tranName.contains("_dummy"))
			return true;
		return false;
	}    
    
    private HashSet<Transition> computeDependent(State[] curStateArray, Transition seed, HashSet<Transition> dependent, 
    		HashSet<Transition> curEnabled, boolean isCycleClosingStrongStubbornComputation) { 	
		// disableSet is the set of transitions that can either be disabled by firing enabledLpnTran, or disable enabledLpnTran.
    	boolean seedTranIsPersistent = false;
    	if (seed.isPersistent()) {
    		seedTranIsPersistent = true;
    	}
		HashSet<Transition> disableSet = staticDependency.get(seed).getDisableSet(seedTranIsPersistent); 
		HashSet<Transition> otherTransDisableEnabledPeristentSeedTran 
			= staticDependency.get(seed).getOtherTransDisableSeedTran(seedTranIsPersistent);
		if (Options.getDebugMode()) {
			System.out.println("@ beginning of computeDependent, consider transition " + seed.getFullLabel());
			printIntegerSet(disableSet, "Disable set for " + seed.getFullLabel());
		}
		dependent.add(seed);
//		for (Transition lpnTranPair : canModifyAssign) {
//			if (curEnabled.contains(lpnTranPair))
//				dependent.add(lpnTranPair);
//		}
		if (Options.getDebugMode()) 
			printIntegerSet(dependent, "@ computeDependent at 0, dependent set for " + seed.getFullLabel());		
		// dependent is equal to enabled. Terminate.
		if (dependent.size() == curEnabled.size()) {
			if (Options.getDebugMode()) {
				System.out.println("Check 0: Size of dependent is equal to enabled. Return dependent.");
			}
			return dependent;
		}
		for (Transition tr : disableSet) {
			if (Options.getDebugMode()) 
				System.out.println("Consider transition in the disable set of "  
						+ seed.getFullLabel() + ": "
						+ tr.getFullLabel());			
			if (curEnabled.contains(tr) && !dependent.contains(tr)
					&& (!tr.isPersistent() || otherTransDisableEnabledPeristentSeedTran.contains(tr))) {
				// (tr is enabled) && (tr is not in seed's dependent set) && (tr is not persistent || ) 
				dependent.addAll(computeDependent(curStateArray,tr,dependent,curEnabled,isCycleClosingStrongStubbornComputation));
				if (Options.getDebugMode()) {
					printIntegerSet(dependent, "@ computeDependent at 1 for transition " + seed.getFullLabel());				
				}
			}
			else if (!curEnabled.contains(tr)) {
				if(Options.getPOR().toLowerCase().equals("tboff")  // no trace-back
						|| (Options.getCycleClosingStrongStubbornMethd().toLowerCase().equals("cctboff") 
								&& isCycleClosingStrongStubbornComputation)) {
					dependent.addAll(curEnabled);
					break;
				}
				HashSet<Transition> necessary = null;
				if (Options.getDebugMode()) {
					printCachedNecessarySets();
				}				
				if (cachedNecessarySets.containsKey(tr)) {
					if (Options.getDebugMode()) {
						printCachedNecessarySets();
						System.out.println("@ computeDependent: Found transition " + tr.getFullLabel() + "in the cached necessary sets.");
					}
					necessary = cachedNecessarySets.get(tr);
				}
				else {
					if (Options.getDebugMode())
						System.out.println("==== Compute necessary set using DFS ====");					
					if (visitedTrans == null)
						visitedTrans = new HashSet<Transition>();
					else
						visitedTrans.clear();
					necessary = computeNecessary(curStateArray,tr,dependent,curEnabled);
				}	
				if (necessary != null && !necessary.isEmpty()) {
					cachedNecessarySets.put(tr, necessary);
					if (Options.getDebugMode()) 
						printIntegerSet(necessary, "@ computeDependent, necessary set for transition " + tr.getFullLabel());					
					for (Transition tranInNecessary : necessary) {
						if (!dependent.contains(tranInNecessary)) {
							if (Options.getDebugMode()) {
								printIntegerSet(dependent,"Check if the newly found necessary transition is in the dependent set of " + seed.getFullLabel());
								System.out.println("It does not contain this transition found by computeNecessary: " 
								+ tranInNecessary.getFullLabel() + ". Compute its dependent set."); 
							}
							dependent.addAll(computeDependent(curStateArray,tranInNecessary,dependent,curEnabled,isCycleClosingStrongStubbornComputation));
						}
						else {
							if (Options.getDebugMode()) {
								printIntegerSet(dependent, "Check if the newly found necessary transition is in the dependent set. Dependent set for " + seed.getFullLabel());
								System.out.println("It already contains this transition found by computeNecessary: " 
										 + tranInNecessary.getFullLabel() + ".");
							}
						}
					}
				}
				else {
					if (Options.getDebugMode()) {
						if (necessary == null) 
							System.out.println("necessary set for transition " + seed.getFullLabel() + " is null.");						
						else 
							System.out.println("necessary set for transition " + seed.getFullLabel() + " is empty.");						
					}			
					//dependent.addAll(curEnabled);
					return curEnabled;
				}
				if (Options.getDebugMode()) {
					printIntegerSet(dependent,"@ computeDependent at 2, dependent set for transition " + seed.getFullLabel()); 
				}					
			}
			else if (dependent.contains(tr)) {
				if (Options.getDebugMode()) {
					printIntegerSet(dependent,"@ computeDependent at 3 for transition " + seed.getFullLabel());
					System.out.println("Transition " + tr.getFullLabel() + " is already in the dependent set of "
							+ seed.getFullLabel() + ".");
				}
			}
		}
		return dependent;
	}

//	private String getNamesOfLPNandTrans(Transition tran) {
//		return tran.getLpn().getLabel() + "(" + tran.getLabel() + ")";
//	}

	private HashSet<Transition> computeNecessary(State[] curStateArray,
			Transition tran, HashSet<Transition> dependent,
			HashSet<Transition> curEnabled) {
		if (Options.getDebugMode()) {
			System.out.println("@ computeNecessary, consider transition: " + tran.getFullLabel());
		}			
		if (cachedNecessarySets.containsKey(tran)) {
			if (Options.getDebugMode()) {
				printCachedNecessarySets();
				System.out.println("@ computeNecessary: Found transition " + tran.getFullLabel()
						+ "'s necessary set in the cached necessary sets. Return the cached necessary set.");
			}				
			return cachedNecessarySets.get(tran);
		}
		// Search for transition(s) that can help to bring the marking(s).
		HashSet<Transition> nMarking = null;
		//Transition transition = lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex());
		//int[] presetPlaces = lpnList[tran.getLpnIndex()].getPresetIndex(transition.getName());
		for (int i=0; i < tran.getPreset().length; i++) {
			int placeIndex = tran.getLpn().getPresetIndex(tran.getLabel())[i];
			if (curStateArray[tran.getLpn().getLpnIndex()].getMarking()[placeIndex]==0) {
				if (Options.getDebugMode()) {
					System.out.println("####### compute nMarking for transition " + tran.getFullLabel() + "########");
				}				
				HashSet<Transition> nMarkingTemp = new HashSet<Transition>();				
				String placeName = tran.getLpn().getPlaceList()[placeIndex]; 	
				Transition[] presetTrans = tran.getLpn().getPlace(placeName).getPreset();
				if (Options.getDebugMode()) {
					System.out.println("@ nMarking: Consider preset place of " + tran.getFullLabel() + ": " + placeName);										
				}	
				for (int j=0; j < presetTrans.length; j++) {
					Transition presetTran = presetTrans[j];
					if (Options.getDebugMode()) {
						System.out.println("@ nMarking: Preset of place " + placeName + " has transition(s): ");
						for (int k=0; k<presetTrans.length; k++) {
							System.out.print(presetTrans[k].getFullLabel() + ", ");
						}
						System.out.println("");
						System.out.println("@ nMarking: Consider transition of " + presetTran.getFullLabel());
					}
					if (curEnabled.contains(presetTran)) {
						if (Options.getDebugMode()) {
							System.out.println("@ nMarking: curEnabled contains transition " 
									+ presetTran.getFullLabel() + "). Add to nMarkingTmp.");
						}	
						nMarkingTemp.add(presetTran);
					}
					else {
						if (visitedTrans.contains(presetTran)) {//seedTranInDisableSet.getVisitedTrans().contains(presetTran)) {
							if (Options.getDebugMode()) {
								System.out.println("@ nMarking: Transition " + presetTran.getFullLabel() + " was visted before"); 

							}
							if (cachedNecessarySets.containsKey(presetTran)) {
								if (Options.getDebugMode()) {
									printCachedNecessarySets();
									System.out.println("@nMarking: Found transition " + presetTran.getFullLabel()
											+ "'s necessary set in the cached necessary sets. Add it to nMarkingTemp.");
								}
								nMarkingTemp = cachedNecessarySets.get(presetTran);
							}		
							continue;
						}
						visitedTrans.add(presetTran);
						if (Options.getDebugMode()) {
							System.out.println("~~~~~~~~~ transVisited ~~~~~~~~~");
							for (Transition visitedTran :visitedTrans) {
								System.out.println(visitedTran.getFullLabel());
							}
							System.out.println("@ nMarking, before call computeNecessary: consider transition: " 
									+ presetTran.getFullLabel());
							System.out.println("@ nMarking: transition " + presetTran.getFullLabel() + " is not enabled. Compute its necessary set.");
						}
						HashSet<Transition> tmp = computeNecessary(curStateArray, presetTran, dependent, curEnabled);
						if (tmp != null) {
							nMarkingTemp.addAll(tmp);
							if (Options.getDebugMode()) {
								System.out.println("@ nMarking: tmp returned from computeNecessary for " + presetTran.getFullLabel() + " is not null.");
								printIntegerSet(nMarkingTemp, presetTran.getFullLabel() + "'s nMarkingTemp");
							}
						}	
						else  
							if (Options.getDebugMode()) {
								System.out.println("@ nMarking: necessary set for transition " 
										+ presetTran.getFullLabel() + " is null.");
							}
					}
				}
				if (!nMarkingTemp.isEmpty())
					//if (nMarking == null || nMarkingTemp.size() < nMarking.size())
					if (nMarking == null 
					|| setSubstraction(nMarkingTemp, dependent).size() < setSubstraction(nMarking, dependent).size())
						nMarking = nMarkingTemp;
			}
			else 
				if (Options.getDebugMode()) {
					System.out.println("@ nMarking: Place " + tran.getLpn().getLabel()
							+ "(" + tran.getLpn().getPlaceList()[placeIndex] + ") is marked.");
				}			
		}
		if (nMarking != null && nMarking.size() ==1 && setSubstraction(nMarking, dependent).size() == 0) {
			cachedNecessarySets.put(tran, nMarking);
			if (Options.getDebugMode()) {
				System.out.println("Return nMarking as necessary set.");
				printCachedNecessarySets();
			}
			return nMarking;
		}		

		// Search for transition(s) that can help to enable the current transition. 
		HashSet<Transition> nEnable = null;
		int[] varValueVector = curStateArray[tran.getLpn().getLpnIndex()].getVariableVector();
		//HashSet<Transition> canEnable = staticMap.get(tran).getEnableBySettingEnablingTrue();
		ArrayList<HashSet<Transition>> canEnable = staticDependency.get(tran).getOtherTransSetSeedTranEnablingTrue(); 
		if (Options.getDebugMode()) {
			System.out.println("####### compute nEnable for transition " + tran.getFullLabel() + "########");
			printIntegerSet(canEnable, "@ nEnable: " + tran.getFullLabel() + " can be enabled by");
		}
		if (tran.getEnablingTree() != null
				&& tran.getEnablingTree().evaluateExpr(tran.getLpn().getAllVarsWithValuesAsString(varValueVector)) == 0.0
				&& !canEnable.isEmpty()) {
			for(int index=0; index < tran.getConjunctsOfEnabling().size(); index++) {
				ExprTree conjunctExprTree = tran.getConjunctsOfEnabling().get(index);
				HashSet<Transition> nEnableForOneConjunct = null;
				if (Options.getDebugMode()) {
					printIntegerSet(canEnable, "@ nEnable: " + tran.getFullLabel() + " can be enabled by");
					System.out.println("@ nEnable: Consider conjunct for transition " + tran.getFullLabel() + ": " 
							+ conjunctExprTree.toString());	
				}
				if (conjunctExprTree.evaluateExpr(tran.getLpn().getAllVarsWithValuesAsString(varValueVector)) == 0.0) {
					HashSet<Transition> canEnableOneConjunctSet = staticDependency.get(tran).getOtherTransSetSeedTranEnablingTrue().get(index);
					nEnableForOneConjunct = new HashSet<Transition>();
					if (Options.getDebugMode()) {
						System.out.println("@ nEnable: Conjunct for transition " + tran.getFullLabel() + " " 
								+ conjunctExprTree.toString() + " is evaluated to FALSE.");
						printIntegerSet(canEnableOneConjunctSet, "@ nEnable: Transitions that can enable this conjunct are");														
					}
					for (Transition tranCanEnable : canEnableOneConjunctSet) {
						if (curEnabled.contains(tranCanEnable)) {
							nEnableForOneConjunct.add(tranCanEnable);
							if (Options.getDebugMode())	{
								System.out.println("@ nEnable: curEnabled contains transition " + tranCanEnable.getFullLabel() + ". Add to nEnableOfOneConjunct.");
							}						
						}
						else {
							if (visitedTrans.contains(tranCanEnable)) {
								if (Options.getDebugMode()) {
									System.out.println("@ nEnable: Transition " + tranCanEnable.getFullLabel() + " was visted before."); 

								}									
								if (cachedNecessarySets.containsKey(tranCanEnable)) {
									if (Options.getDebugMode()) {
										printCachedNecessarySets();
										System.out.println("@ nEnable: Found transition " + tranCanEnable.getFullLabel()
												+ "'s necessary set in the cached necessary sets. Add it to nEnableOfOneConjunct.");
									}
									nEnableForOneConjunct.addAll(cachedNecessarySets.get(tranCanEnable));
								}				
								continue;
							}
							visitedTrans.add(tranCanEnable);
							if (Options.getDebugMode()) {
								System.out.println("@ nEnable: Transition " + tranCanEnable.getFullLabel()
										+ " is not enabled. Compute its necessary set.");
							}
							HashSet<Transition> tmp = computeNecessary(curStateArray, tranCanEnable, dependent, curEnabled);
							if (tmp != null) {
								nEnableForOneConjunct.addAll(tmp);
								if (Options.getDebugMode()) {
									System.out.println("@ nEnable: tmp returned from computeNecessary for " + tranCanEnable.getFullLabel() + ": ");
									printIntegerSet(tmp, "");
									printIntegerSet(nEnableForOneConjunct, tranCanEnable.getFullLabel() + "'s nEnableOfOneConjunct");
								}
							}					
							else
								if (Options.getDebugMode()) {
									System.out.println("@ nEnable: necessary set for transition " 
											+ tranCanEnable.getFullLabel() + " is null.");
								}				
						}
					}
					if (!nEnableForOneConjunct.isEmpty()) {
						if (nEnable == null 
								|| setSubstraction(nEnableForOneConjunct, dependent).size() < setSubstraction(nEnable, dependent).size()) {
							//&& !nEnableForOneConjunct.isEmpty())) {
							if (Options.getDebugMode()) {
								System.out.println("@ nEnable: nEnable for transition " + tran.getFullLabel() +" is replaced by nEnableForOneConjunct.");
								printIntegerSet(nEnable, "nEnable");
								printIntegerSet(nEnableForOneConjunct, "nEnableForOneConjunct");
							}
							nEnable = nEnableForOneConjunct;
						}
						else {
							if (Options.getDebugMode()) {
								System.out.println("@ nEnable: nEnable for transition " + tran.getFullLabel() +" remains unchanged.");
								printIntegerSet(nEnable, "nEnable");
							}
						}				
					}
				}
				else {
					if (Options.getDebugMode()) {
						System.out.println("@ nEnable: Conjunct for transition " + tran.getFullLabel() + " " 
								+ conjunctExprTree.toString() + " is evaluated to TRUE. No need to trace back on it.");
					}
				}			
			}
		}
		else {
			if (Options.getDebugMode()) {
				if (tran.getEnablingTree() == null) {
					System.out.println("@ nEnable: transition " + tran.getFullLabel() + " has no enabling condition.");
				}
				else if (tran.getEnablingTree().evaluateExpr(tran.getLpn().getAllVarsWithValuesAsString(varValueVector)) !=0.0) {
					System.out.println("@ nEnable: transition " + tran.getFullLabel() + "'s enabling condition is true.");
				}
				else if (tran.getEnablingTree() != null
						&& tran.getEnablingTree().evaluateExpr(tran.getLpn().getAllVarsWithValuesAsString(varValueVector)) == 0.0
						&& canEnable.isEmpty()) {
					System.out.println("@ nEnable: transition " + tran.getFullLabel()
							+ "'s enabling condition is false, but no other transitions that can help to enable it were found .");
				}
				printIntegerSet(nMarking, "=== nMarking for transition " + tran.getFullLabel());
				printIntegerSet(nEnable, "=== nEnable for transition " + tran.getFullLabel());
			}
		}
		if (nMarking != null && nEnable == null) {
			if (!nMarking.isEmpty()) 
				cachedNecessarySets.put(tran, nMarking);
			if (Options.getDebugMode()) {
				printCachedNecessarySets();
			}
			return nMarking;
		}
		else if (nMarking == null && nEnable != null) {
			if (!nEnable.isEmpty())
				cachedNecessarySets.put(tran, nEnable);
			if (Options.getDebugMode()) {
				printCachedNecessarySets();
			}
			return nEnable;
		}
		else if (nMarking == null || nEnable == null) {
			return null;
		}
		else {
			if (!nMarking.isEmpty() && !nEnable.isEmpty()) {
				if (setSubstraction(nMarking, dependent).size() < setSubstraction(nEnable, dependent).size()) {
					cachedNecessarySets.put(tran, nMarking);
					if (Options.getDebugMode()) {
						printCachedNecessarySets();
					}					
					return nMarking;
				}
				cachedNecessarySets.put(tran, nEnable);
				if (Options.getDebugMode()) {
					printCachedNecessarySets();
				}
				return nEnable;
			}
			else if (nMarking.isEmpty() && !nEnable.isEmpty()) {
				cachedNecessarySets.put(tran, nEnable);
				if (Options.getDebugMode()) {
					printCachedNecessarySets();
				}
				return nEnable;
			}
			else if (!nMarking.isEmpty() && nEnable.isEmpty()) { 
				cachedNecessarySets.put(tran, nMarking);
				if (Options.getDebugMode()) {
					printCachedNecessarySets();
				}	
				return nMarking;
			}
			else {				
				return null;
			}
		}
	}

//	private HashSet<Transition> computeNecessaryUsingDependencyGraphs(State[] curStateArray,
//			Transition tran, HashSet<Transition> curEnabled,
//			HashMap<Transition, StaticSets> staticMap, 
//			LhpnFile[] lpnList, Transition seedTran) {
//		if (Options.getDebugMode()) {
////			System.out.println("@ computeNecessary, consider transition: " + lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex()).getName() + ")");
//			writeStringWithEndOfLineToPORDebugFile("@ computeNecessary, consider transition: " + lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex()).getName() + ")");
//		}
//		// Use breadth-first search to find the shorted path from the seed transition to an enabled transition.
//		LinkedList<Transition> exploredTransQueue = new LinkedList<Transition>();
//		HashSet<Transition> allExploredTrans = new HashSet<Transition>();
//		exploredTransQueue.add(tran);
//		//boolean foundEnabledTran = false;
//		HashSet<Transition> canEnable = new HashSet<Transition>();
//		while(!exploredTransQueue.isEmpty()){
//			Transition curTran = exploredTransQueue.poll();
//			allExploredTrans.add(curTran);
//			if (cachedNecessarySets.containsKey(curTran)) {
//				if (Options.getDebugMode()) {
//					writeStringWithEndOfLineToPORDebugFile("Found transition " + lpnList[curTran.getLpnIndex()].getLabel() + "(" + lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex()).getName() + ")"
//							+ "'s necessary set in the cached necessary sets. Terminate BFS.");
//				}
//				return cachedNecessarySets.get(curTran);			
//			}
//			canEnable = buildCanBringTokenSet(curTran,lpnList, curStateArray);
//			if (Options.getDebugMode()) {
////				printIntegerSet(canEnable, lpnList, "Neighbors that can help to bring tokens to transition " + lpnList[curTran.getLpnIndex()].getLabel() + "(" + lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex()) + ") ");
//				printIntegerSet(canEnable, lpnList, "Neighbors that can help to bring tokens to transition " + lpnList[curTran.getLpnIndex()].getLabel() + "(" + lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex()) + ") ");
//			}
//			// Decide if canSetEnablingTrue set can help to enable curTran.
//			Transition curTransition = lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex());
//			int[] varValueVector = curStateArray[curTran.getLpnIndex()].getVector();
//			if (curTransition.getEnablingTree() != null
//					&& curTransition.getEnablingTree().evaluateExpr(lpnList[curTran.getLpnIndex()].getAllVarsWithValuesAsString(varValueVector)) == 0.0) {
//				canEnable.addAll(staticMap.get(curTran).getEnableBySettingEnablingTrue());
//				if (Options.getDebugMode()) {
////					printIntegerSet(canEnable, lpnList, "Neighbors that can help to set the enabling of transition " + lpnList[curTran.getLpnIndex()].getLabel() + "(" + lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex()) + ") ");
//					printIntegerSet(staticMap.get(curTran).getEnableBySettingEnablingTrue(), lpnList, "Neighbors that can help to set the enabling transition " + lpnList[curTran.getLpnIndex()].getLabel() + "(" + lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex()) + ") ");
//				}
//			}
//			if (Options.getDebugMode()) {
////				printIntegerSet(canEnable, lpnList, "Neighbors that can help to enable transition " + lpnList[curTran.getLpnIndex()].getLabel() + "(" + lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex()) + ") ");
//				printIntegerSet(canEnable, lpnList, "Neighbors that can help to enable transition " + lpnList[curTran.getLpnIndex()].getLabel() + "(" + lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex()) + ") ");
//			}
//			for (Transition neighborTran : canEnable) {
//				if (curEnabled.contains(neighborTran)) {
//					if (!neighborTran.equals(seedTran)) {
//						HashSet<Transition> necessarySet = new HashSet<Transition>();
//						necessarySet.add(neighborTran);
//						// TODO: Is it true that the necessary set for a disabled transition only contains a single element before the dependent set of the element is computed?
//						cachedNecessarySets.put(tran, necessarySet);
//						if (Options.getDebugMode()) {
////							System.out.println("Enabled neighbor that can help to enable transition " + lpnList[curTran.getLpnIndex()].getLabel() 
////									+ "(" + lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex()) + "): " + lpnList[neighborTran.getLpnIndex()].getLabel() 
////									+ "(" + lpnList[neighborTran.getLpnIndex()].getTransition(neighborTran.getTranIndex()) + ").");
//							writeStringWithEndOfLineToPORDebugFile("Enabled neighbor that can help to enable transition " + lpnList[curTran.getLpnIndex()].getLabel() 
//							+ "(" + lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex()) + "): " + lpnList[neighborTran.getLpnIndex()].getLabel() 
//							+ "(" + lpnList[neighborTran.getLpnIndex()].getTransition(neighborTran.getTranIndex()) + ").");
//						}
//						return necessarySet;
//					}
//					else if (neighborTran.equals(seedTran) && canEnable.size()==1) {
//						if (Options.getDebugMode()) {
////							System.out.println("Enabled neighbor that can help to enable transition " + lpnList[curTran.getLpnIndex()].getLabel() 
////									+ "(" + lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex()) + "): " + lpnList[neighborTran.getLpnIndex()].getLabel() 
////									+ "(" + lpnList[neighborTran.getLpnIndex()].getTransition(neighborTran.getTranIndex()) + "). But " + lpnList[neighborTran.getLpnIndex()].getLabel() 
////									+ "(" + lpnList[neighborTran.getLpnIndex()].getTransition(neighborTran.getTranIndex()) + ") is the seed transition. Return null necessary set.");
//							writeStringWithEndOfLineToPORDebugFile("Enabled neighbor that can help to enable transition " + lpnList[curTran.getLpnIndex()].getLabel() 
//							+ "(" + lpnList[curTran.getLpnIndex()].getTransition(curTran.getTranIndex()) + "): " + lpnList[neighborTran.getLpnIndex()].getLabel() 
//							+ "(" + lpnList[neighborTran.getLpnIndex()].getTransition(neighborTran.getTranIndex()) + "). But " + lpnList[neighborTran.getLpnIndex()].getLabel() 
//							+ "(" + lpnList[neighborTran.getLpnIndex()].getTransition(neighborTran.getTranIndex()) + ") is the seed transition. Return null necessary set.");
//						}
//						return null;
////						if (exploredTransQueue.isEmpty()) {
////							System.out.println("exploredTransQueue is empty. Return null necessary set."); 
////							writeStringWithNewLineToPORDebugFile("exploredTransQueue is empty. Return null necessary set.");							
////							return null;
////						}			
//					}
//				}
//				if (!allExploredTrans.contains(neighborTran)) {
//					allExploredTrans.add(neighborTran);
//					exploredTransQueue.add(neighborTran);
//				}
//			}
//			canEnable.clear();
//		}
//		return null;
//	}
	
	private void printCachedNecessarySets() {
		System.out.println("================ cached necessary sets =================");
		for (Transition key : cachedNecessarySets.keySet()) {
			System.out.print(key.getLpn().getLabel() + "(" + key.getLabel() + ") => ");
			for (Transition necessary : cachedNecessarySets.get(key)) {
				System.out.print(necessary.getLpn().getLabel() + "(" + necessary.getLabel() + ") ");
			}
			System.out.println("");
		}	
	}
	
	private static HashSet<Transition> setSubstraction(
			HashSet<Transition> left, HashSet<Transition> right) {
		HashSet<Transition> sub = new HashSet<Transition>();
		for (Transition lpnTranPair : left) {
			if (!right.contains(lpnTranPair))
				sub.add(lpnTranPair);
		}
		return sub;
	}
	
	public static boolean stateOnStack(int lpnIndex, State curState, HashSet<PrjState> stateStack) {
		boolean isStateOnStack = false;
		for (PrjState prjState : stateStack) {
			State[] stateArray = prjState.toStateArray();
			if(stateArray[lpnIndex].equals(curState)) {// (stateArray[lpnIndex] == curState) {
				isStateOnStack = true;
				break;
			}
		}
		return isStateOnStack;
	}
	
	private static void printIntegerSet(HashSet<Transition> Trans, String setName) {
		if (!setName.isEmpty())
			System.out.print(setName + ": ");
		if (Trans == null) {
			System.out.println("null");
		}
		else if (Trans.isEmpty()) {
			System.out.println("empty");
		}
		else {
			for (Transition lpnTranPair : Trans) {
				System.out.print(lpnTranPair.getLabel() + "(" 
						+ lpnTranPair.getLpn().getLabel() + "),");
			}
			System.out.println();
		}	
	}
	
	private static void printIntegerSet(ArrayList<HashSet<Transition>> transSet, String setName) {
		if (!setName.isEmpty())
			System.out.print(setName + ": ");
		if (transSet == null) {
			System.out.println("null");
		}
		else if (transSet.isEmpty()) {
			System.out.println("empty");
		}
		else {
			for (HashSet<Transition> lpnTranPairSet : transSet) {
				for (Transition lpnTranPair : lpnTranPairSet)
					System.out.print(lpnTranPair.getLpn().getLabel() + "(" 
							+ lpnTranPair.getLabel() + "),");
			}
			System.out.println();
		}
		
	}	
}
