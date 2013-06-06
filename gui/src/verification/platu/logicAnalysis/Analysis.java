package verification.platu.logicAnalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Stack;

import javax.swing.JOptionPane;

import org.antlr.grammar.v3.ANTLRParser.optionsSpec_return;

import lpn.parser.Abstraction;
import lpn.parser.ExprTree;
import lpn.parser.LhpnFile;
import lpn.parser.Place;
import lpn.parser.Transition;
import lpn.parser.LpnDecomposition.LpnProcess;
import main.Gui;
import verification.platu.MDD.MDT;
import verification.platu.MDD.Mdd;
import verification.platu.MDD.mddNode;
import verification.platu.common.IndexObjMap;
import verification.platu.lpn.LPNTranRelation;
import verification.platu.lpn.LpnTranList;
import verification.platu.main.Options;
import verification.platu.markovianAnalysis.ProbGlobalState;
import verification.platu.markovianAnalysis.ProbGlobalStateSet;
import verification.platu.markovianAnalysis.ProbLocalStateGraph;
import verification.platu.markovianAnalysis.ProbLocalStateTuple;
import verification.platu.partialOrders.DependentSet;
import verification.platu.partialOrders.DependentSetComparator;
import verification.platu.partialOrders.StaticSets;
import verification.platu.project.PrjState;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;
import verification.timed_state_exploration.zoneProject.EventSet;
import verification.timed_state_exploration.zoneProject.StateSet;
import verification.timed_state_exploration.zoneProject.TimedPrjState;
import verification.timed_state_exploration.zoneProject.TimedStateSet;
import verification.timed_state_exploration.zoneProject.Zone;

public class Analysis {

	private LinkedList<Transition> traceCex;
	protected Mdd mddMgr = null;
	private HashMap<Transition, HashSet<Transition>> cachedNecessarySets = new HashMap<Transition, HashSet<Transition>>();
	/*
	 * visitedTrans is used in computeNecessary for a disabled transition of interest, to keep track of all transitions visited during trace-back.
	 */
	private HashSet<Transition> visitedTrans;
	HashMap<Transition, StaticSets> staticDependency = new HashMap<Transition, StaticSets>();
		
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

				if (stateTrace.contains(nextPrjState) == true)
					;// System.out.println("found a cycle");

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
	
	@SuppressWarnings("unchecked")
	public StateGraph[] search_dfs(final StateGraph[] sgList, final State[] initStateArray) {
		System.out.println("---> calling function search_dfs");
				
		double peakUsedMem = 0;
		double peakTotalMem = 0;
		boolean failure = false;
		int tranFiringCnt = 0;
		int totalStates = 1;
		int numLpns = sgList.length;
		
		//Stack<State[]> stateStack = new Stack<State[]>();
		HashSet<PrjState> stateStack = new HashSet<PrjState>();
		Stack<LinkedList<Transition>> lpnTranStack = new Stack<LinkedList<Transition>>();
		Stack<Integer> curIndexStack = new Stack<Integer>();
		//HashSet<PrjState> prjStateSet = new HashSet<PrjState>();
		
		// Set of PrjStates that have been seen before. Set class documentation
		// for how it behaves. Timing Change.
		// TODO: Temporarily call a ProbGlobalStateSet constructor.
		//StateSet prjStateSet = new StateSet();
		//StateSet prjStateSet = new ProbGlobalStateSet();//new StateSet();		
		HashSet<PrjState> prjStateSet = generateStateSet();
		
		PrjState initPrjState;
		// Create the appropriate type for the PrjState depending on whether timing is 
		// being used or not. Timing Change.
		if(!Options.getTimingAnalysisFlag()){
			// If not doing timing.
			if (!Options.getProbabilisticModelFlag())				
				initPrjState = new PrjState(initStateArray);
			else
				initPrjState = new ProbGlobalState(initStateArray);
		}
		else{
			// If timing is enabled.
			initPrjState = new TimedPrjState(initStateArray);
			
			// Set the initial values of the inequality variables.
			//((TimedPrjState) initPrjState).updateInequalityVariables();
		}
		prjStateSet.add(initPrjState);
		
		if(Options.getProbabilisticModelFlag()){
			
			((ProbGlobalStateSet) prjStateSet).set_initState(initPrjState);
		}

		PrjState stateStackTop;		
		stateStackTop = initPrjState;
		if (Options.getDebugMode()) {
			printStateArray(stateStackTop.toStateArray(), "~~~~ stateStackTop ~~~~");
		}
		stateStack.add(stateStackTop);		
		constructDstLpnList(sgList);
		if (Options.getDebugMode()) {
			printDstLpnList(sgList);
		}		
		boolean init = true;
		LpnTranList initEnabled;
		if(!Options.getTimingAnalysisFlag()){ // Timing Change.
			initEnabled = sgList[0].getEnabled(initStateArray[0], init);
		}
		else
		{
			// When timing is enabled, it is the project state that will determine
			// what is enabled since it contains the zone. This indicates the zeroth zone
			// contained in the project and the zeroth LPN to get the transitions from.
			initEnabled = ((TimedPrjState) stateStackTop).getPossibleEvents(0, 0);
		}
		lpnTranStack.push(initEnabled.clone());
		curIndexStack.push(0);
		init = false;
		if (Options.getDebugMode()) {			
			System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
			printTransList(initEnabled, "initEnabled");
		}
		boolean memExceedsLimit = false;
		
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
			
			if (!memExceedsLimit && Options.getMemUpperBoundFlag() && iterations % 100 == 0) {
				if (curUsedMem > Options.getMemUpperBound()) {
					System.out.println("******* Used memory exceeds memory upper bound (" + (float)Options.getMemUpperBound()/1000000 + "MB) *******");
					System.out.println("******* Used memory = " + (float)curUsedMem/1000000 + "MB *******");
					memExceedsLimit = true;
				}
			}
			State[] curStateArray = stateStackTop.toStateArray(); //stateStack.peek();
			int curIndex = curIndexStack.peek();
			LinkedList<Transition> curEnabled = lpnTranStack.peek();
			if (failureTranIsEnabled(curEnabled)) {
				return null;
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
						curEnabled = sgList[curIndex].getEnabled(curStateArray[curIndex], init).clone();
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
//						if (Options.getDebugMode()) {
//							writeIntegerStackToDebugFile(curIndexStack, "curIndexStack after push 1");
//						}
						break;
					} 					
					curIndex++;
				}
			}
			if (curIndex == numLpns) {
//				prjStateSet.add(stateStackTop);
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
				nextPrjState = new PrjState(nextStateArray);
			}
			else{
				// Get the next timed state and extract the next un-timed states.
				nextPrjState = sgList[curIndex].fire(sgList, stateStackTop,
						(EventSet) firedTran);
				nextStateArray = nextPrjState.toStateArray();
			}
			tranFiringCnt++;

			// Check if the firedTran causes disabling error or deadlock.
			LinkedList<Transition>[] curEnabledArray = new LinkedList[numLpns];
			LinkedList<Transition>[] nextEnabledArray = new LinkedList[numLpns];
			for (int i = 0; i < numLpns; i++) {
				LinkedList<Transition> enabledList;
				if(!Options.getTimingAnalysisFlag()){ // Timing Change.
					enabledList = sgList[i].getEnabled(curStateArray[i], init);
				}
				else{
					// Get the enabled transitions from the Zone for the appropriate
					// LPN.
					//enabledList = ((TimedPrjState) stateStackTop).getEnabled(i);
					enabledList = ((TimedPrjState) stateStackTop).getPossibleEvents(0, i);
				}
				curEnabledArray[i] = enabledList;
				if(!Options.getTimingAnalysisFlag()){ // Timing Change.
					enabledList = sgList[i].getEnabled(nextStateArray[i], init);
				}
				else{
					//enabledList = ((TimedPrjState) nextPrjState).getEnabled(i);
					enabledList = ((TimedPrjState) nextPrjState).getPossibleEvents(0, i);
				}
				nextEnabledArray[i] = enabledList;
				if (Options.getReportDisablingError()) {
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
			}

			if(!Options.getTimingAnalysisFlag()){
				if (Analysis.deadLock(sgList, nextStateArray) == true) {
					System.out.println("*** Verification failed: deadlock.");
					failure = true;
					break main_while_loop;
				}
			}
			else{
				if (Analysis.deadLock(sgList, nextStateArray) == true){
					System.out.println("*** Verification failed: deadlock.");
					failure = true;
					JOptionPane.showMessageDialog(Gui.frame,
							"The system deadlocked.", "Error",
							JOptionPane.ERROR_MESSAGE);
				
					break main_while_loop;
				}
			}
			
			//PrjState nextPrjState = new PrjState(nextStateArray); // Moved earlier. Timing Change.
			Boolean	existingState;
			existingState = prjStateSet.contains(nextPrjState); //|| stateStack.contains(nextPrjState);
//			if (!Options.getProbabilisticModelFlag())
//				existingState = prjStateSet.contains(nextPrjState); //|| stateStack.contains(nextPrjState);
//			else
//				existingState = probGlobalStateSet.contains(nextPrjState);
			if (existingState == false) {
				prjStateSet.add(nextPrjState);
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				if (Options.getProbabilisticModelFlag()) {
//					for (int i=0; i<stateStackTop.getStateArray().length; i++) {						
//						State curState = stateStackTop.getStateArray()[i];
//						//						if (curState.getLpn().equals(firedTran.getLpn())) { // Locate the local state from which firedTran was fired in this iteration.
//						//							HashMap<State, HashMap<Transition, ProbLocalStateTuple>> nextLocalStateTupleMap = ((ProbLocalStateGraph) sgList[i]).getNextProbLocalStateMap();
//						//							double firedTranRate = nextLocalStateTupleMap.get(curState).get(firedTran).getTranRate(); 
//						//							// Search for transition rate stored locally, and add global state transition relation. 
//						//							// In this case, next global state (nextPrjState) does not exist. 
//						//							if (nextLocalStateTupleMap.get(curState).get(firedTran).getNextProbLocalState() != null 
//						//									&& firedTranRate != 0.0) {								
//						//								((ProbGlobalStateSet) prjStateSet).addGlobalStateTran(stateStackTop, firedTran, firedTranRate, nextPrjState);
//						//								break;
//						//							}
//						//						}
//						if (curState.getLpn().equals(firedTran.getLpn())) { // Locate the local state from which firedTran was fired in this iteration.
//							HashMap<State, HashMap<Transition, ProbLocalStateTuple>> nextLocalStateTupleMap = ((ProbLocalStateGraph) sgList[i]).getNextProbLocalStateMap();
//							ProbLocalStateTuple nextLocalStateTuple = nextLocalStateTupleMap.get(curState).get(firedTran);
//							State nextLocalState = nextLocalStateTuple.getNextProbLocalState();
//							if (nextLocalState!= null) {
//								nextLocalStateTuple.getNextLocalToGlobalMap().put(firedTran, nextPrjState);								
//								break;
//							}
//						}
//					}
					int curStateIndex = firedTran.getLpn().getLpnIndex();
					// Mark in the global state which local state has the outgoing transition which was fired in this iteration. 
					if (stateStackTop instanceof ProbGlobalState) 
						((ProbGlobalState) stateStackTop).addLocalStateIndex(curStateIndex);
					State curState = stateStackTop.getStateArray()[curStateIndex];
					HashMap<State, HashMap<Transition, ProbLocalStateTuple>> nextLocalStateTupleMap = ((ProbLocalStateGraph) sgList[curStateIndex]).getNextProbLocalStateMap();
					ProbLocalStateTuple nextLocalStateTuple = nextLocalStateTupleMap.get(curState).get(firedTran);
					State nextLocalState = nextLocalStateTuple.getNextProbLocalState();
					if (nextLocalState!= null) 
						nextLocalStateTuple.getNextLocalToGlobalMap().put(firedTran, nextPrjState);																				
				}
				if (Options.getOutputSgFlag()) {
					if (Options.getDebugMode()) {
						//						System.out.println("******* curStateArray *******");
						//						printStateArray(curStateArray);
						//						System.out.println("******* nextStateArray *******");
						//						printStateArray(nextStateArray);			
						//						System.out.println("stateStackTop: ");
						//						printStateArray(stateStackTop.toStateArray());
						//						System.out.println("firedTran = " + firedTran.getName());
						//						System.out.println("***nextStateMap for stateStackTop before firedTran being added: ");
						//						printNextStateMap(stateStackTop.getNextStateMap());
					}
					stateStackTop.setTranOut(firedTran, nextPrjState);
					if (Options.getDebugMode()) {
						//						System.out.println("***nextStateMap for stateStackTop after firedTran being added: ");
						//						printNextStateMap(stateStackTop.getNextStateMap());
					}
					for (PrjState prjState : prjStateSet) {
						if (prjState.equals(stateStackTop)) {
							prjState.setTranOut(firedTran, nextPrjState);
							if (Options.getDebugMode()) {
								//								System.out.println("***nextStateMap for prjState: ");
								//								printNextStateMap(prjState.getNextStateMap());
							}
						}
					}
					//					System.out.println("-----------------------");
				}
				stateStackTop = nextPrjState;
				stateStack.add(stateStackTop);
				lpnTranStack.push((LpnTranList) nextEnabledArray[0].clone());
				curIndexStack.push(0);
				totalStates++;
				if (Options.getDebugMode()) {										
					printStateArray(stateStackTop.toStateArray(), "~~~~~~~ Add global state to stateStack ~~~~~~~");
					System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
					printTransList(nextEnabledArray[0], "");
					printTranStack(lpnTranStack, "******** lpnTranStack ***********");
				}
			}
			else {
				if (Options.getProbabilisticModelFlag()) {
					//					for (int i=0; i<stateStackTop.getStateArray().length; i++) {						
					//						State curState = stateStackTop.getStateArray()[i];
					//						if (curState.getLpn().equals(firedTran.getLpn())) { // Locate the local state from which firedTran was fired in this iteration.
					//							HashMap<State, HashMap<Transition, ProbLocalStateTuple>> nextLocalStateTupleMap = ((ProbLocalStateGraph) sgList[i]).getNextProbLocalStateMap();
					//							double firedTranRate = nextLocalStateTupleMap.get(curState).get(firedTran).getTranRate(); 
					//							// Search for transition rate stored locally, and add global state transition relation. 
					//							// In this case, next global state (nextPrjState) does not exist. 
					//							if (nextLocalStateTupleMap.get(curState).get(firedTran).getNextProbLocalState() != null 
					//									&& firedTranRate != 0.0) {								
					//								((ProbGlobalStateSet) prjStateSet).addGlobalStateTran(stateStackTop, firedTran, firedTranRate, nextPrjState);
					//								break;
					//							}
					//						}
					//					}
//					for (int i=0; i<stateStackTop.getStateArray().length; i++) {						
//						State curState = stateStackTop.getStateArray()[i];
//						if (curState.getLpn().equals(firedTran.getLpn())) { // Locate the local state from which firedTran was fired in this iteration.
//							HashMap<State, HashMap<Transition, ProbLocalStateTuple>> nextLocalStateTupleMap = ((ProbLocalStateGraph) sgList[i]).getNextProbLocalStateMap();
//							ProbLocalStateTuple nextLocalStateTuple = nextLocalStateTupleMap.get(curState).get(firedTran);
//							State nextLocalState = nextLocalStateTuple.getNextProbLocalState();
//							if (nextLocalState!= null) {
//								nextLocalStateTuple.getNextLocalToGlobalMap().put(firedTran, nextPrjState);								
//								break;
//							}
//						}
//					}
					int curStateIndex = firedTran.getLpn().getLpnIndex();					
					// Mark in the global state which local state has the outgoing transition which was fired in this iteration.
					if (stateStackTop instanceof ProbGlobalState)
						((ProbGlobalState)stateStackTop).addLocalStateIndex(curStateIndex);
					State curState = stateStackTop.getStateArray()[curStateIndex];
					HashMap<State, HashMap<Transition, ProbLocalStateTuple>> nextLocalStateTupleMap = ((ProbLocalStateGraph) sgList[curStateIndex]).getNextProbLocalStateMap();
					ProbLocalStateTuple nextLocalStateTuple = nextLocalStateTupleMap.get(curState).get(firedTran);
					State nextLocalState = nextLocalStateTuple.getNextProbLocalState();
					if (nextLocalState!= null) 
						nextLocalStateTuple.getNextLocalToGlobalMap().put(firedTran, nextPrjState);
				}
				if (Options.getOutputSgFlag()) {
					if (Options.getDebugMode()) {						
						printStateArray(curStateArray, "******* curStateArray *******");
						printStateArray(nextStateArray, "******* nextStateArray *******");
					}
					for (PrjState prjState : prjStateSet) {
						if (prjState.equals(nextPrjState)) {
							nextPrjState.setNextStateMap((HashMap<Transition, PrjState>) prjState.getNextStateMap().clone()); 
						}							
					}
					if (Options.getDebugMode()) {						
						printStateArray(stateStackTop.toStateArray(), "stateStackTop: ");
						System.out.println("firedTran = " + firedTran.getFullLabel());
						System.out.println("nextStateMap for stateStackTop before firedTran being added: ");
						printNextGlobalStateMap(stateStackTop.getNextStateMap());
					}
					stateStackTop.setTranOut(firedTran, nextPrjState);
					if (Options.getDebugMode()) {
						System.out.println("*** nextStateMap for stateStackTop after firedTran being added ***");
						printNextGlobalStateMap(stateStackTop.getNextStateMap());
					}
					for (PrjState prjState : prjStateSet) {
						if (prjState == stateStackTop) { 
							prjState.setNextStateMap((HashMap<Transition, PrjState>) stateStackTop.getNextStateMap().clone());
							if (Options.getDebugMode()) {
								System.out.println("*** nextStateMap for prjState ***");
								printNextGlobalStateMap(prjState.getNextStateMap());
							}						
						}
					}
					System.out.println("-----------------------");
				}			
			}
		}
		double totalStateCnt =0;
		totalStateCnt = prjStateSet.size();
		System.out.println("---> final numbers: # LPN transition firings: "	+ tranFiringCnt 
			//+ ", # of prjStates found: " + totalStateCnt 
			+ ", " + prjStateSet.toString()
			+ ", max_stack_depth: " + max_stack_depth 
			+ ", peak total memory: " + peakTotalMem / 1000000 + " MB"
			+ ", peak used memory: " + peakUsedMem / 1000000 + " MB");
		if(Options.getTimingAnalysisFlag() && !failure){
			JOptionPane.showMessageDialog(Gui.frame,
					"Verification was successful.", "Success",
					JOptionPane.INFORMATION_MESSAGE);
		}
		if (Options.getOutputLogFlag()) 
			writePerformanceResultsToLogFile(false, tranFiringCnt, totalStateCnt, peakTotalMem / 1000000, peakUsedMem / 1000000);
		if (Options.getOutputSgFlag()) {
			System.out.println("outputSGPath = "  + Options.getPrjSgPath());
			
			// TODO: Andrew: I don't think you need the toHashSet() now.
			//drawGlobalStateGraph(sgList, prjStateSet.toHashSet(), true);
			drawGlobalStateGraph(sgList, prjStateSet, true);
		}		
		return sgList;
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

	/**
	 * Generates the appropriate version of a HashSet<PrjState> for storing
	 * the "already seen" set of project states.
	 * @return
	 * 		Returns a HashSet<PrjState>, a StateSet, or a ProbGlobalStateSet
	 * 				depending on the type.
	 */
	private HashSet<PrjState> generateStateSet(){
		
		boolean timed = Options.getTimingAnalysisFlag();
		boolean subsets = Zone.getSubsetFlag();
		boolean supersets = Zone.getSupersetFlag();
		
		if(Options.getProbabilisticModelFlag()){
			return new ProbGlobalStateSet();
		}
		else if(timed && (subsets || supersets)){
			return new TimedStateSet();
		}
		
		return new HashSet<PrjState>();
	}
	
	private boolean failureTranIsEnabled(LinkedList<Transition> curAmpleTrans) {
		boolean failureTranIsEnabled = false;
		for (Transition tran : curAmpleTrans) {
			if (tran.isFail()) {
				
				if(Zone.get_writeLogFile() != null){
					try {
						Zone.get_writeLogFile().write(tran.getLabel());
						Zone.get_writeLogFile().newLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				JOptionPane.showMessageDialog(Gui.frame,
						"Failure transition " + tran.getLabel() + " is enabled.", "Error",
						JOptionPane.ERROR_MESSAGE);
				failureTranIsEnabled = true;
				break;
			}
		}
		return failureTranIsEnabled;
	}

	private void drawGlobalStateGraph(StateGraph[] sgList, HashSet<PrjState> prjStateSet, boolean fullSG) {
		try {
			String fileName = null;
			if (fullSG) {
				fileName = Options.getPrjSgPath() + "full_sg.dot";
			}
			else {
				fileName = Options.getPrjSgPath() + Options.getPOR().toLowerCase() + "_"
						+ Options.getCycleClosingMthd().toLowerCase() + "_" 
						+ Options.getCycleClosingAmpleMethd().toLowerCase() + "_sg.dot";
			}
				
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write("digraph G {\n");
			for (PrjState curGlobalState : prjStateSet) {	
				// Build composite current global state.
				String curVarNames = "";
				String curVarValues = "";
				String curMarkings = "";
				String curEnabledTrans = "";
				String curGlobalStateIndex = "";
				HashMap<String, Integer> vars = new HashMap<String, Integer>();
				
				for (State curLocalState : curGlobalState.toStateArray()) {
					LhpnFile curLpn = curLocalState.getLpn();
					for(int i = 0; i < curLpn.getVarIndexMap().size(); i++) {
						//System.out.println(curLpn.getVarIndexMap().getKey(i) + " = " + curLocalState.getVector()[i]);				
						vars.put(curLpn.getVarIndexMap().getKey(i), curLocalState.getVector()[i]);
					}
					curMarkings = curMarkings + "," + intArrayToString("markings", curLocalState);
					if (!boolArrayToString("enabled trans", curLocalState).equals(""))
						curEnabledTrans = curEnabledTrans + "," +  boolArrayToString("enabled trans", curLocalState);
					curGlobalStateIndex = curGlobalStateIndex + "_" + "S" + curLocalState.getIndex();
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
				curGlobalStateIndex = curGlobalStateIndex.substring(curGlobalStateIndex.indexOf("_")+1, curGlobalStateIndex.length());
				out.write("Inits[shape=plaintext, label=\"<" + curVarNames + ">\"]\n");
				out.write(curGlobalStateIndex + "[shape=\"ellipse\",label=\"" + curGlobalStateIndex + "\\n<"+curVarValues+">" + "\\n<"+curEnabledTrans+">" + "\\n<"+curMarkings+">" + "\"]\n");
				
//				// Build composite next global states.
				HashMap<Transition, PrjState> nextStateMap = curGlobalState.getNextStateMap();				
				for (Transition outTran : nextStateMap.keySet()) {
					PrjState nextGlobalState = nextStateMap.get(outTran);
					String nextVarNames = "";
					String nextVarValues = "";
					String nextMarkings = "";
					String nextEnabledTrans = "";
					String nextGlobalStateIndex = "";
					for (State nextLocalState : nextGlobalState.toStateArray()) {
						LhpnFile nextLpn = nextLocalState.getLpn();
						for(int i = 0; i < nextLpn.getVarIndexMap().size(); i++) {
							vars.put(nextLpn.getVarIndexMap().getKey(i), nextLocalState.getVector()[i]);
						}
						nextMarkings = nextMarkings + "," + intArrayToString("markings", nextLocalState);
						if (!boolArrayToString("enabled trans", nextLocalState).equals(""))
							nextEnabledTrans = nextEnabledTrans + "," +  boolArrayToString("enabled trans", nextLocalState);
						nextGlobalStateIndex = nextGlobalStateIndex + "_" + "S" + nextLocalState.getIndex();
					}
					for (String vName : vars.keySet()) {
						Integer vValue = vars.get(vName);
						nextVarValues = nextVarValues + vValue + ", ";
						nextVarNames = nextVarNames + vName + ", ";
					}
					if (!nextVarNames.isEmpty() && !nextVarValues.isEmpty()) {
						nextVarNames = nextVarNames.substring(0, nextVarNames.lastIndexOf(","));
						nextVarValues = nextVarValues.substring(0, nextVarValues.lastIndexOf(","));
					}
					nextMarkings = nextMarkings.substring(nextMarkings.indexOf(",")+1, nextMarkings.length());
					nextEnabledTrans = nextEnabledTrans.substring(nextEnabledTrans.indexOf(",")+1, nextEnabledTrans.length());
					nextGlobalStateIndex = nextGlobalStateIndex.substring(nextGlobalStateIndex.indexOf("_")+1, nextGlobalStateIndex.length());
					out.write("Inits[shape=plaintext, label=\"<" + nextVarNames + ">\"]\n");
					out.write(nextGlobalStateIndex + "[shape=\"ellipse\",label=\"" + nextGlobalStateIndex + "\\n<"+nextVarValues+">" + "\\n<"+nextEnabledTrans+">" + "\\n<"+nextMarkings+">" + "\"]\n");
					
					String outTranName = outTran.getLabel();
					if (outTran.isFail() && !outTran.isPersistent())
						out.write(curGlobalStateIndex + "->" + nextGlobalStateIndex + "[label=\"" + outTranName + "\", fontcolor=red]\n");
					else if (!outTran.isFail() && outTran.isPersistent())						
						out.write(curGlobalStateIndex + "->" + nextGlobalStateIndex + "[label=\"" + outTranName + "\", fontcolor=blue]\n");
					else if (outTran.isFail() && outTran.isPersistent())
						out.write(curGlobalStateIndex + "->" + nextGlobalStateIndex + "[label=\"" + outTranName + "\", fontcolor=purple]\n");
					else
						out.write(curGlobalStateIndex + "->" + nextGlobalStateIndex + "[label=\"" + outTranName + "\"]\n");
				}
			}
			out.write("}");
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error producing local state graph as dot file.");
		}	
	}
	
	private void drawDependencyGraphs(LhpnFile[] lpnList) {
		String fileName = Options.getPrjSgPath() + "dependencyGraph.dot";
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
				StaticSets curStaticSets = staticDependency.get(curTran);
				String curTranStr = curTran.getLpn().getLabel() + "_" + curTran.getLabel();
				for (Transition curTranInDisable : curStaticSets.getOtherTransDisableCurTranSet()) {
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
				
				for (HashSet<Transition> canEnableOneConjunctSet : curStaticSets.getOtherTransSetCurTranEnablingTrue()) {
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

	
	private String intArrayToString(String type, State curState) {
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
			arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}				
		else if (type.equals("vars")) {
			for (int i=0; i< curState.getVector().length; i++) {
				arrayStr = arrayStr + curState.getVector()[i] + ",";
			}
			if (arrayStr.contains(","))
				arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}
		return arrayStr;
	}
	
	private String boolArrayToString(String type, State curState) {
		String arrayStr = "";
		if (type.equals("enabled trans")) {
			for (int i=0; i< curState.getTranVector().length; i++) {
				if (curState.getTranVector()[i]) {
					arrayStr = arrayStr + curState.getLpn().getAllTransitions()[i].getLabel() + ",";
				}
			}
			if (arrayStr != "")
				arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}
		return arrayStr;
	}

	private void printStateArray(State[] stateArray, String title) {
		if (title != null)
			System.out.println(title);		
		for (int i=0; i<stateArray.length; i++) {
			System.out.print("S" + stateArray[i].getIndex() + "(" + stateArray[i].getLpn().getLabel() +") -> ");
			System.out.print("markings: " + intArrayToString("markings", stateArray[i]) + " / ");
			System.out.print("var values: " + intArrayToString("vars", stateArray[i]) + " / ");
			System.out.print("enabled trans: " + boolArrayToString("enabled trans", stateArray[i]) + " / ");	
		}
		
	}

	private void printTransList(LinkedList<Transition> tranList, String title) {
		if (title != null)
			System.out.println(title);
		for (int i=0; i< tranList.size(); i++) 
			System.out.println(tranList.get(i).getFullLabel() + ", ");
		System.out.println("");
	}

//	private void writeIntegerStackToDebugFile(Stack<Integer> curIndexStack, String title) {
//		if (title != null)
//			System.out.println(title);
//		for (int i=0; i < curIndexStack.size(); i++) {			
//			System.out.println(title + "[" + i + "]" + curIndexStack.get(i));
//		}
//		System.out.println("------------------");
//	}

	private void printDstLpnList(StateGraph[] lpnList) {
		System.out.println("++++++ dstLpnList ++++++");
		for (int i=0; i<lpnList.length; i++) {
			LhpnFile curLPN = lpnList[i].getLpn();
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

	private void constructDstLpnList(StateGraph[] sgList) {
		for (int i=0; i<sgList.length; i++) {
			LhpnFile curLPN = sgList[i].getLpn();
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
	@SuppressWarnings("unchecked")
	public StateGraph[] searchPOR_taceback(final StateGraph[] sgList, final State[] initStateArray) {
		System.out.println("---> calling function searchPOR_traceback");
		System.out.println("---> " + Options.getPOR());
		System.out.println("---> " + Options.getCycleClosingMthd());
		System.out.println("---> " + Options.getCycleClosingAmpleMethd());
		
		double peakUsedMem = 0;
		double peakTotalMem = 0;
		boolean failure = false;
		int tranFiringCnt = 0;
		int totalStates = 1;
		int numLpns = sgList.length;
		
		LhpnFile[] lpnList = new LhpnFile[numLpns];
		for (int i=0; i<numLpns; i++) {
			lpnList[i] = sgList[i].getLpn();
		}
				
		HashSet<PrjState> stateStack = new HashSet<PrjState>();
		Stack<LinkedList<Transition>> lpnTranStack = new Stack<LinkedList<Transition>>();
		HashSet<PrjState> prjStateSet = new HashSet<PrjState>();		
		PrjState initPrjState = new PrjState(initStateArray);
		prjStateSet.add(initPrjState);
		
		PrjState stateStackTop = initPrjState;
		if (Options.getDebugMode())			
			printStateArray(stateStackTop.toStateArray(), "%%%%%%% stateStackTop %%%%%%%%");
		stateStack.add(stateStackTop);
		constructDstLpnList(sgList);
		if (Options.getDebugMode())
			printDstLpnList(sgList);
		// Find static pieces for POR. 
		HashMap<Integer, Transition[]> allTransitions = new HashMap<Integer, Transition[]>(lpnList.length); 
		//HashMap<Transition, StaticSets> staticSetsMap = new HashMap<Transition, StaticSets>();
		HashMap<Transition, Integer> allProcessTransInOneLpn = new HashMap<Transition, Integer>();
		HashMap<Transition, LpnProcess> allTransitionsToLpnProcesses = new HashMap<Transition, LpnProcess>();
		for (int lpnIndex=0; lpnIndex<lpnList.length; lpnIndex++) {
			allTransitions.put(lpnIndex, lpnList[lpnIndex].getAllTransitions());
			Abstraction abs = new Abstraction(lpnList[lpnIndex]);
			abs.decomposeLpnIntoProcesses();				 
			allProcessTransInOneLpn = (HashMap<Transition, Integer>)abs.getTransWithProcIDs();
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
		if (Options.getUseDependentQueue())
				tranFiringFreq = new HashMap<Transition, Integer>(allTransitions.keySet().size());
		// Need to build conjuncts for each transition's enabling condition first before dealing with dependency and enable sets.
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
			for (Transition curTran: allTransitions.get(lpnIndex)) {
				StaticSets curStatic = new StaticSets(curTran, allTransitionsToLpnProcesses);
				curStatic.buildOtherTransSetCurTranEnablingTrue(); 
				curStatic.buildCurTranDisableOtherTransSet();
				if (Options.getPORdeadlockPreserve())
					curStatic.buildOtherTransDisableCurTranSet();
				else 
					curStatic.buildModifyAssignSet();
				staticDependency.put(curTran, curStatic);
				if (Options.getUseDependentQueue())
					tranFiringFreq.put(curTran, 0);
			}			
		}
		if (Options.getDebugMode()) {
			printStaticSetsMap(lpnList);
		}			
		boolean init = true;
		//LpnTranList initAmpleTrans = new LpnTranList();
		LpnTranList initAmpleTrans = getAmple(initStateArray, null, init, tranFiringFreq, sgList, lpnList, stateStack, stateStackTop);
		lpnTranStack.push(initAmpleTrans);
		init = false;
		if (Options.getDebugMode()) {			
			printTransList(initAmpleTrans, "+++++++ Push trans onto lpnTranStack @ 1++++++++");
			drawDependencyGraphs(lpnList);
		}		
//		HashSet<Transition> initAmple = new HashSet<Transition>();
//		for (Transition t: initAmpleTrans) {
//			initAmple.add(t);
//		}
		updateLocalAmpleTbl(initAmpleTrans, sgList, initStateArray);		
		boolean memExceedsLimit = false;
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
			if (!memExceedsLimit && Options.getMemUpperBoundFlag() && iterations % 100 == 0) {
				if (curUsedMem > Options.getMemUpperBound()) {
					System.out.println("******* Used memory exceeds memory upper bound (" + (float)Options.getMemUpperBound()/1000000 + "MB) *******");
					System.out.println("******* Used memory = " + (float)curUsedMem/1000000 + "MB *******");
					memExceedsLimit = true;
				}
			}
			State[] curStateArray = stateStackTop.toStateArray(); //stateStack.peek();
			LinkedList<Transition> curAmpleTrans = lpnTranStack.peek();
			if (failureTranIsEnabled(curAmpleTrans)) {
				return null;
			}
			if (curAmpleTrans.size() == 0) {
				lpnTranStack.pop();
				prjStateSet.add(stateStackTop);
				if (Options.getDebugMode()) {
					System.out.println("+++++++ Pop trans off lpnTranStack ++++++++");					
					printTranStack(lpnTranStack, "####### lpnTranStack #########");
					System.out.println("####### prjStateSet #########");
					printPrjStateSet(prjStateSet);
					System.out.println("%%%%%%% Remove stateStackTop from stateStack %%%%%%%%");
				}
				stateStack.remove(stateStackTop);
				stateStackTop = stateStackTop.getFather();
				continue;
			}

			Transition firedTran = curAmpleTrans.removeLast();
			//Transition firedTran = curAmpleTrans.removelast();	
			if (Options.getDebugMode()) {
				System.out.println("#################################");
				System.out.println("Fired Transition: " + firedTran.getLpn().getLabel() + "(" + firedTran.getLabel() + ")");
				System.out.println("#################################");
			}
			if (Options.getUseDependentQueue()) {
				Integer freq = tranFiringFreq.get(firedTran) + 1;
				tranFiringFreq.put(firedTran, freq);
//				if (Options.getDebugMode()) {
//					System.out.println("~~~~~~tranFiringFreq~~~~~~~");
//					printHashMap(tranFiringFreq, sgList);
//				}
			}
			State[] nextStateArray = sgList[firedTran.getLpn().getLpnIndex()].fire(sgList, curStateArray, firedTran);
			tranFiringCnt++;

			// Check if the firedTran causes disabling error or deadlock.
			if (Options.getReportDisablingError()) {
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
			LpnTranList nextAmpleTrans = new LpnTranList();
			nextAmpleTrans = getAmple(curStateArray, nextStateArray, init, tranFiringFreq, sgList, lpnList, prjStateSet, stateStackTop);
			// check for possible deadlock
			if (nextAmpleTrans.size() == 0) {
				System.out.println("*** Verification failed: deadlock.");
				failure = true;
				break main_while_loop;
			}
			PrjState nextPrjState = new PrjState(nextStateArray);
			Boolean existingState = prjStateSet.contains(nextPrjState) || stateStack.contains(nextPrjState);						
			if (existingState == false) {
				if (Options.getDebugMode()) {
					System.out.println("%%%%%%% existingSate == false %%%%%%%%");				
				}
				if (Options.getOutputSgFlag()) {
					if (Options.getDebugMode()) {
						printStateArray(curStateArray, "******* curStateArray *******");
						printStateArray(nextStateArray, "******* nextStateArray *******");
						printStateArray(stateStackTop.toStateArray(), "stateStackTop");
						System.out.println("firedTran = " + firedTran.getFullLabel());
						System.out.println("***nextStateMap for stateStackTop before firedTran being added: ");
						printNextGlobalStateMap(stateStackTop.getNextStateMap());
					}
					stateStackTop.setTranOut(firedTran, nextPrjState);
					if (Options.getDebugMode()) {
						System.out.println("***nextStateMap for stateStackTop after firedTran being added: ");
						printNextGlobalStateMap(stateStackTop.getNextStateMap());
					}
					for (PrjState prjState : prjStateSet) {
						if (prjState.equals(stateStackTop)) {
							prjState.setTranOut(firedTran, nextPrjState);
							if (Options.getDebugMode()) {
								System.out.println("***nextStateMap for prjState: ");
								printNextGlobalStateMap(prjState.getNextStateMap());
							}
						}
					}
					if (Options.getDebugMode())
						System.out.println("-----------------------");
				}
				updateLocalAmpleTbl(nextAmpleTrans, sgList, nextStateArray);
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				stateStackTop = nextPrjState;
				stateStack.add(stateStackTop);
				if (Options.getDebugMode()) {
					printStateArray(stateStackTop.toStateArray(), "%%%%%%% Add global state to stateStack %%%%%%%%");					
					printTransList(nextAmpleTrans, "+++++++ Push trans onto lpnTranStack @ 2++++++++");
				}
				lpnTranStack.push((LinkedList<Transition>) nextAmpleTrans.clone());
				totalStates++;
			}
			else {
				if (Options.getOutputSgFlag()) {
					if (Options.getDebugMode()) {
						printStateArray(curStateArray, "******* curStateArray *******");
						printStateArray(nextStateArray, "******* nextStateArray *******");
					}
					for (PrjState prjState : prjStateSet) {
						if (prjState.equals(nextPrjState)) {
							nextPrjState.setNextStateMap((HashMap<Transition, PrjState>) prjState.getNextStateMap().clone()); 
						}							
					}
					if (Options.getDebugMode()) {
						System.out.println("stateStackTop: ");
						printStateArray(stateStackTop.toStateArray(), "stateStackTop: ");
						System.out.println("firedTran = " + firedTran.getFullLabel());
						System.out.println("nextStateMap for stateStackTop before firedTran being added: ");
						printNextGlobalStateMap(stateStackTop.getNextStateMap());
					}
					stateStackTop.setTranOut(firedTran, nextPrjState);
					if (Options.getDebugMode()) {
						System.out.println("***nextStateMap for stateStackTop after firedTran being added: ");
						printNextGlobalStateMap(stateStackTop.getNextStateMap());
					}
					for (PrjState prjState : prjStateSet) {
						if (prjState == stateStackTop) { 
							prjState.setNextStateMap((HashMap<Transition, PrjState>) stateStackTop.getNextStateMap().clone());
							if (Options.getDebugMode()) {
								System.out.println("***nextStateMap for prjState: ");
								printNextGlobalStateMap(prjState.getNextStateMap());
							}
						}
					}
					if (Options.getDebugMode())
						System.out.println("-----------------------");
				}
				if (!Options.getCycleClosingMthd().toLowerCase().equals("no_cycleclosing")) {
					// Cycle closing check
					if (prjStateSet.contains(nextPrjState) && stateStack.contains(nextPrjState)) {
						if (Options.getDebugMode()) {
							System.out.println("%%%%%%% Cycle Closing %%%%%%%%");
						}							
						HashSet<Transition> nextAmpleSet = new HashSet<Transition>();
						HashSet<Transition> curAmpleSet = new HashSet<Transition>();
						for (Transition t : nextAmpleTrans) 
							nextAmpleSet.add(t);
						for (Transition t: curAmpleTrans) 
							curAmpleSet.add(t);						
						curAmpleSet.add(firedTran);
						HashSet<Transition> newNextAmple = new HashSet<Transition>();
						newNextAmple = computeCycleClosingTrans(curStateArray, nextStateArray, staticDependency, 
																	tranFiringFreq, sgList, prjStateSet, nextPrjState, nextAmpleSet, curAmpleSet, stateStack);

						if (newNextAmple != null && !newNextAmple.isEmpty()) {
							//LpnTranList newNextAmpleTrans = getLpnTranList(newNextAmple, sgList);
							stateStackTop.setChild(nextPrjState);
							nextPrjState.setFather(stateStackTop);	
							if (Options.getDebugMode()) {
								printStateArray(nextPrjState.toStateArray(), "nextPrjState");
								System.out.println( "nextStateMap for nextPrjState");
								printNextGlobalStateMap(nextPrjState.getNextStateMap());
							}
							stateStackTop = nextPrjState;
							stateStack.add(stateStackTop);
							if (Options.getDebugMode()) {
								printStateArray(stateStackTop.toStateArray(), "%%%%%%% Add state to stateStack %%%%%%%%");
								System.out.println("stateStackTop: ");
								printStateArray(stateStackTop.toStateArray(), "stateStackTop");
								System.out.println("nextStateMap for stateStackTop: ");
								printNextGlobalStateMap(nextPrjState.getNextStateMap());
							}
							
							lpnTranStack.push((LinkedList<Transition>) newNextAmple.clone());
							if (Options.getDebugMode()) {
								printTransList((LinkedList<Transition>) newNextAmple.clone(), "+++++++ Push these trans onto lpnTranStack @ Cycle Closing ++++++++");								
								printTranStack(lpnTranStack, "******* lpnTranStack ***************");
								
							}
						}
					}
				}
				updateLocalAmpleTbl(nextAmpleTrans, sgList, nextStateArray);
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
			drawGlobalStateGraph(sgList, prjStateSet, false);
		}
		return sgList;
	}
	
	private void writePerformanceResultsToLogFile(boolean isPOR, int tranFiringCnt, double totalStateCnt,
			double peakTotalMem, double peakUsedMem) {
		try {
			String fileName = null;
			if (isPOR) {
				fileName = Options.getPrjSgPath() + Options.getLogName() + "_" + Options.getPOR() + "_" 
					+ Options.getCycleClosingMthd() + "_" + Options.getCycleClosingAmpleMethd() + ".log";
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
			System.err.println("Error producing local state graph as dot file.");
		}	
	}

	private void printTranStack(Stack<LinkedList<Transition>> lpnTranStack, String title) {
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

	private void printNextGlobalStateMap(HashMap<Transition, PrjState> nextStateMap) {
		for (Transition t: nextStateMap.keySet()) {
			System.out.println(t.getFullLabel() + " -> ");
			State[] stateArray = nextStateMap.get(t).getStateArray();
			for (int i=0; i<stateArray.length; i++) {
				System.out.print("S" + stateArray[i].getIndex() + "(" + stateArray[i].getLpn().getLabel() +")" +", ");
			}
			System.out.println("");
		}
	}

	private LpnTranList convertToLpnTranList(HashSet<Transition> newNextAmple) {
		LpnTranList newNextAmpleTrans = new LpnTranList();
		for (Transition lpnTran : newNextAmple) {
			newNextAmpleTrans.add(lpnTran);
		}
		return newNextAmpleTrans;
	}

	private HashSet<Transition> computeCycleClosingTrans(State[] curStateArray,
			State[] nextStateArray,
			HashMap<Transition, StaticSets> staticSetsMap,
			HashMap<Transition, Integer> tranFiringFreq,
			StateGraph[] sgList, HashSet<PrjState> prjStateSet,
			PrjState nextPrjState, HashSet<Transition> nextAmple, 
			HashSet<Transition> curAmple, HashSet<PrjState> stateStack) {		
    	for (State s : nextStateArray)
    		if (s == null) 
    			throw new NullPointerException();
    	String cycleClosingMthd = Options.getCycleClosingMthd();
    	HashSet<Transition> newNextAmple = new HashSet<Transition>();
    	HashSet<Transition> nextEnabled = new HashSet<Transition>();
    	HashSet<Transition> curEnabled = new HashSet<Transition>();
    	for (int lpnIndex=0; lpnIndex<nextStateArray.length; lpnIndex++) {
			LhpnFile curLpn = sgList[lpnIndex].getLpn();
			for (int i=0; i < curLpn.getAllTransitions().length; i++) {
				Transition tran = curLpn.getAllTransitions()[i];
				if (nextStateArray[lpnIndex].getTranVector()[i]) 
					nextEnabled.add(tran);
				if (curStateArray[lpnIndex].getTranVector()[i])
					curEnabled.add(tran);
            }
    	}
    		// Cycle closing on global state graph
    		if (Options.getDebugMode()) {
    			//System.out.println("~~~~~~~ existing global state ~~~~~~~~");
    		}
    		if (cycleClosingMthd.equals("strong")) {
    			newNextAmple.addAll(setSubstraction(curEnabled, nextAmple));			
    			updateLocalAmpleTbl(convertToLpnTranList(newNextAmple), sgList, nextStateArray);
    		}
    		else if (cycleClosingMthd.equals("behavioral")) {
    			if (Options.getDebugMode()) {
//    				System.out.println("****** behavioral: cycle closing check ********");
    			}
    			HashSet<Transition> curReduced = setSubstraction(curEnabled, curAmple);
    			HashSet<Transition> oldNextAmple = new HashSet<Transition>();
    			DependentSetComparator depComp = new DependentSetComparator(tranFiringFreq); 
    			PriorityQueue<DependentSet> dependentSetQueue = new PriorityQueue<DependentSet>(nextEnabled.size(), depComp);
    			// TODO: Is oldNextAmple correctly obtained below?
    			if (Options.getDebugMode())
    				printStateArray(nextStateArray,"******* nextStateArray *******");    			
    			for (int lpnIndex=0; lpnIndex < sgList.length; lpnIndex++) {
    				if (sgList[lpnIndex].getEnabledSetTbl().get(nextStateArray[lpnIndex]) != null) {
    					LpnTranList oldLocalNextAmpleTrans = sgList[lpnIndex].getEnabledSetTbl().get(nextStateArray[lpnIndex]);
    					if (Options.getDebugMode()) {
//    						printTransitionSet(oldLocalNextAmpleTrans, "oldLocalNextAmpleTrans");
    					}    						
        				for (Transition oldLocalTran : oldLocalNextAmpleTrans)
        					oldNextAmple.add(oldLocalTran);
    				}
    					
    			}
    			HashSet<Transition> ignored = setSubstraction(curReduced, oldNextAmple);
    			boolean isCycleClosingAmpleComputation = true;
    			for (Transition seed : ignored) {
    				HashSet<Transition> dependent = new HashSet<Transition>();
    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,isCycleClosingAmpleComputation); 				    				
    				if (Options.getDebugMode()) {
//    					printIntegerSet(dependent, sgList, "dependent set for ignored transition " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//						+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
    				}
    				// TODO: Is this still necessary?
//     				boolean dependentOnlyHasDummyTrans = true;
//	  				for (LpnTransitionPair dependentTran : dependent) {
//	  					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//	  								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//	  				}         	    							
//	  				if ((newNextAmple.size() == 0 || dependent.size() < newNextAmple.size()) && !dependentOnlyHasDummyTrans) 
//	  					newNextAmple = (HashSet<LpnTransitionPair>) dependent.clone();
//	  				DependentSet dependentSet = new DependentSet(newNextAmple, seed, 
//	  						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//					_______________________________________________________________________
    				DependentSet dependentSet = new DependentSet(dependent, seed,isDummyTran(seed.getLabel())); 	  						
	  				dependentSetQueue.add(dependentSet);
    			}
    			cachedNecessarySets.clear();
    			if (!dependentSetQueue.isEmpty()) {
//    				System.out.println("depdentSetQueue is NOT empty.");
//    				newNextAmple = dependentSetQueue.poll().getDependent();
    				// **************************
        			// TODO: Will newNextAmpleTmp - oldNextAmple be safe?
        			HashSet<Transition> newNextAmpleTmp = dependentSetQueue.poll().getDependent();        			
        			newNextAmple = setSubstraction(newNextAmpleTmp, oldNextAmple);
        			// **************************
        			updateLocalAmpleTbl(convertToLpnTranList(newNextAmple), sgList, nextStateArray);
    			}
    			if (Options.getDebugMode()) {
//    				printIntegerSet(newNextAmple, sgList, "newNextAmple");
//    				System.out.println("******** behavioral: end of cycle closing check *****");
    			}
    		}
    		else if (cycleClosingMthd.equals("state_search")) {
    			// TODO: complete cycle closing check for state search.		
    		}
  		return newNextAmple;
	}

	private void updateLocalAmpleTbl(LpnTranList nextAmpleTrans,
			StateGraph[] sgList, State[] nextStateArray) {
		// Ample set at each state is stored in the enabledSetTbl in each state graph.
		for (Transition lpnTran : nextAmpleTrans) {
			State nextState = nextStateArray[lpnTran.getLpn().getLpnIndex()];
			LpnTranList a = sgList[lpnTran.getLpn().getLpnIndex()].getEnabledSetTbl().get(nextState);
			if (a != null) {
				if (!a.contains(lpnTran))
					a.add(lpnTran);
			}
			else {
				LpnTranList newLpnTranList = new LpnTranList();
				newLpnTranList.add(lpnTran);
				sgList[lpnTran.getLpn().getLpnIndex()].getEnabledSetTbl().put(nextStateArray[lpnTran.getLpn().getLpnIndex()], newLpnTranList);
				if (Options.getDebugMode()) {
//					System.out.println("@ updateLocalAmpleTbl: ");
//					System.out.println("Add S" + nextStateArray[lpnTran.getLpnIndex()].getIndex() + " to the enabledSetTbl of " 
//							+ sgList[lpnTran.getLpnIndex()].getLpn().getLabel() + ".");
				}
			}
		}
		if (Options.getDebugMode()) {
//			printEnabledSetTbl(sgList);
		}
	}

	private void printPrjStateSet(HashSet<PrjState> prjStateSet) {
		for (PrjState curGlobal : prjStateSet) {
			State[] curStateArray = curGlobal.toStateArray();
			printStateArray(curStateArray, null);
			System.out.println("-------------");
		}
		
	}

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

		
//		System.out.println("call getAmple on initStateArray at 0: ");
//		boolean init = true;
//		AmpleSet initAmple = new AmpleSet();
//		initAmple = sgList[0].getAmple(initStateArray[0], staticSetsMap, init, tranFiringFreq);
//		HashMap<State, LpnTranList> initEnabledSetTbl = (HashMap<State, LpnTranList>) sgList[0].copyEnabledSetTbl();
//		lpnTranStack.push(initAmple.getAmpleSet());
//		curIndexStack.push(0);
		
//		System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//		printTransitionSet((LpnTranList) initAmple.getAmpleSet(), "");
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
//			//AmpleSet curAmple = new AmpleSet();
//			//LinkedList<Transition> curAmpleTrans = curAmple.getAmpleSet();
//			LinkedList<Transition> curAmpleTrans = lpnTranStack.peek();
//						
////			System.out.println("------- curStateArray ----------");
////			printStateArray(curStateArray);
////			System.out.println("+++++++ curAmple trans ++++++++");
////			printTransLinkedList(curAmpleTrans);
//			
//			// If all enabled transitions of the current LPN are considered,
//			// then consider the next LPN
//			// by increasing the curIndex.
//			// Otherwise, if all enabled transitions of all LPNs are considered,
//			// then pop the stacks.		
//			if (curAmpleTrans.size() == 0) {
//				lpnTranStack.pop();
////				System.out.println("+++++++ Pop trans off lpnTranStack ++++++++");
//				curIndexStack.pop();
////				System.out.println("+++++++ Pop index off curIndexStack ++++++++");
//				curIndex++;
////				System.out.println("curIndex = " + curIndex);
//				while (curIndex < numLpns) {
//					System.out.println("call getEnabled on curStateArray at 1: ");

//					LpnTranList tmpAmpleTrans = (LpnTranList) (sgList[curIndex].getAmple(curStateArray[curIndex], staticSetsMap, init, tranFiringFreq)).getAmpleSet();
//					curAmpleTrans = tmpAmpleTrans.clone();
//					//printTransitionSet(curEnabled, "curEnabled set");
//					if (curAmpleTrans.size() > 0) {
//						System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//						printTransLinkedList(curAmpleTrans);
//						lpnTranStack.push(curAmpleTrans);
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
//			Transition firedTran = curAmpleTrans.removeLast();	
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
//			LinkedList<Transition>[] curAmpleArray = new LinkedList[numLpns];
//			@SuppressWarnings("unchecked")
//			LinkedList<Transition>[] nextAmpleArray = new LinkedList[numLpns];
//			boolean updatedAmpleDueToCycleRule = false;
//			for (int i = 0; i < numLpns; i++) {
//				StateGraph sg_tmp = sgList[i];
//				System.out.println("call getAmple on curStateArray at 2: i = " + i);
//				AmpleSet ampleList = new AmpleSet();
//				if (init) {
//					ampleList = initAmple;
//					sg_tmp.setEnabledSetTbl(initEnabledSetTbl);
//					init = false;
//				}
//				else
//					ampleList = sg_tmp.getAmple(curStateArray[i], staticSetsMap, init, tranFiringFreq);
//				curAmpleArray[i] = ampleList.getAmpleSet();

//				System.out.println("call getAmpleRefinedCycleRule on nextStateArray at 3: i = " + i);

//				ampleList = sg_tmp.getAmpleRefinedCycleRule(curStateArray[i], nextStateArray[i], staticSetsMap, stateStack, stateStackTop, init, cycleClosingMthdIndex, i, tranFiringFreq);
//				nextAmpleArray[i] = ampleList.getAmpleSet();
//				if (!updatedAmpleDueToCycleRule && ampleList.getAmpleChanged()) {
//					updatedAmpleDueToCycleRule = true;

//				System.out.println("-------------- curAmpleArray --------------");
//				for (LinkedList<Transition> tranList : curAmpleArray) {
//					printTransLinkedList(tranList);
//				}
////				System.out.println("-------------- curAmpleArray --------------");
////				for (LinkedList<Transition> tranList : curAmpleArray) {
////					printTransLinkedList(tranList);
////				}
////				System.out.println("-------------- nextAmpleArray --------------");
////				for (LinkedList<Transition> tranList : nextAmpleArray) {
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
//			if (existingState == true && updatedAmpleDueToCycleRule) {
//				// cycle closing
//				System.out.println("%%%%%%% existingSate == true %%%%%%%%");
//				stateStackTop.setChild(nextPrjState);
//				nextPrjState.setFather(stateStackTop);
//				stateStackTop = nextPrjState;
//				stateStack.add(stateStackTop);
//				System.out.println("%%%%%%% Add state to stateStack %%%%%%%%");
//				printStateArray(stateStackTop.toStateArray());
//				System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//				printTransitionSet((LpnTranList) nextAmpleArray[0], "");
//				lpnTranStack.push((LpnTranList) nextAmpleArray[0].clone());
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
//				printTransitionSet((LpnTranList) nextAmpleArray[0], "");
//				lpnTranStack.push((LpnTranList) nextAmpleArray[0].clone());
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

	private void printStaticSetsMap( LhpnFile[] lpnList) {		
		System.out.println("---------- staticSetsMap -----------");			
		for (Transition lpnTranPair : staticDependency.keySet()) {
			StaticSets statSets = staticDependency.get(lpnTranPair);
			printLpnTranPair(statSets.getTran(), statSets.getDisableSet(), "disableSet");
			for (HashSet<Transition> setOneConjunctTrue : statSets.getOtherTransSetCurTranEnablingTrue()) {
				printLpnTranPair(statSets.getTran(), setOneConjunctTrue, "enableBySetingEnablingTrue for one conjunct");
			}				
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
	
	private void printLpnTranPair(Transition curTran,
			HashSet<Transition> TransitionSet, String setName) {				
		System.out.println(setName + " for (" + curTran.getLpn().getLabel() + " , " + curTran.getLabel() + ") is: ");
		if (TransitionSet.isEmpty()) {
			System.out.println("empty");				
		}
		else {
			for (Transition lpnTranPair: TransitionSet) 
				System.out.print("(" + lpnTranPair.getLpn().getLabel() + ", " + lpnTranPair.getLabel() + ")," + " ");						
			System.out.println();
		}					
	}		
	
    /**
     * Return the set of all LPN transitions that are enabled in 'state'. The "init" flag indicates whether a transition
     * needs to be evaluated. The enabledSetTbl (for each StateGraph obj) stores the AMPLE set for each state of each LPN.
     * @param stateArray
     * @param stateStack 
     * @param enable 
     * @param disableByStealingToken 
     * @param disable 
     * @param init 
     * @param sgList 
     * @return
     */
    public LpnTranList getAmple(State[] curStateArray, State[] nextStateArray, 
    			boolean init, HashMap<Transition, Integer> tranFiringFreq, StateGraph[] sgList, LhpnFile[] lpnList,
    			HashSet<PrjState> stateStack, PrjState stateStackTop) {
    	State[] stateArray = null;
    	if (nextStateArray == null)
    		stateArray = curStateArray;
    	else
    		stateArray = nextStateArray;
    	for (State s : stateArray)
    		if (s == null) 
    			throw new NullPointerException();
    	LpnTranList ample = new LpnTranList();
    	HashSet<Transition> curEnabled = new HashSet<Transition>();
   		for (int lpnIndex=0; lpnIndex<stateArray.length; lpnIndex++) {
   			State state = stateArray[lpnIndex];
   			if (init) {
             	LhpnFile curLpn = sgList[lpnIndex].getLpn();
             	for (int i=0; i < curLpn.getAllTransitions().length; i++) {
                   	if (sgList[lpnIndex].isEnabled(curLpn.getAllTransitions()[i], state)){
                   		curEnabled.add(curLpn.getAllTransitions()[i]);
                    }
                }
   			}
   			else {
   				LhpnFile curLpn = sgList[lpnIndex].getLpn();
   				for (int i=0; i < curLpn.getAllTransitions().length; i++) {
   					if (stateArray[lpnIndex].getTranVector()[i]) {
   						curEnabled.add(curLpn.getAllTransitions()[i]);
   					}
               	}
   			}
   		}  		
        if (Options.getDebugMode()) {
	      	System.out.println("******* Partial Order Reduction *******");
	      	printIntegerSet(curEnabled, "Enabled set");
	      	System.out.println("******* Begin POR *******");
        }
        if (curEnabled.isEmpty()) {
        	return ample;
        }
        
        HashSet<Transition> ready = partialOrderReduction(stateArray, curEnabled, tranFiringFreq, sgList);
    	if (Options.getDebugMode()) {
	    	System.out.println("******* End POR *******");
	    	printIntegerSet(ready, "Ample set");
	    	System.out.println("********************");
    	}
		// ************* Priority: tran fired less times first *************
    	if (tranFiringFreq != null) {
    		LinkedList<Transition> readyList = new LinkedList<Transition>();
        	for (Transition inReady : ready) {
        		readyList.add(inReady);
        	}
        	mergeSort(readyList, tranFiringFreq);
       		for (Transition inReady : readyList) {
       			ample.addFirst(inReady);
       		}
       	}
    	else {
    		for (Transition tran : ready) {
       			ample.add(tran);
       		}
    	}
    	return ample;
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

	private LinkedList<Transition> merge(LinkedList<Transition> left,
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

	/**
     * Return the set of all LPN transitions that are enabled in 'state'. The "init" flag indicates whether a transition
     * needs to be evaluated. 
     * @param nextState
	 * @param stateStackTop 
     * @param enable 
     * @param disableByStealingToken 
     * @param disable 
     * @param init 
	 * @param cycleClosingMthdIndex 
	 * @param lpnIndex 
     * @param isNextState
     * @return
     */
	public LpnTranList getAmpleRefinedCycleRule(State[] curStateArray, State[] nextStateArray, HashMap<Transition,StaticSets> staticSetsMap, 
			boolean init, HashMap<Transition,Integer> tranFiringFreq, StateGraph[] sgList, 
			HashSet<PrjState> stateStack, PrjState stateStackTop) {
//    	AmpleSet nextAmple = new AmpleSet();
//    	if (nextState == null) {
//            throw new NullPointerException();
//        }
//    	if(ampleSetTbl.containsKey(nextState) == true && stateOnStack(nextState, stateStack)) {
//     		System.out.println("~~~~~~~ existing state in enabledSetTbl and on stateStack: S" + nextState.getIndex() + "~~~~~~~~");
//    		printTransitionSet((LpnTranList)ampleSetTbl.get(nextState), "Old ample at this state: ");
// 			// Cycle closing check
//    		LpnTranList nextAmpleTransOld = (LpnTranList) nextAmple.getAmpleSet();
//    		nextAmpleTransOld = ampleSetTbl.get(nextState);
//    		LpnTranList curAmpleTrans = ampleSetTbl.get(curState);
//    		LpnTranList curReduced = new LpnTranList();
//    		LpnTranList curEnabled = curState.getEnabledTransitions(); 		
//    		for (int i=0; i<curEnabled.size(); i++) {
//    			if (!curAmpleTrans.contains(curEnabled.get(i))) {
//    				curReduced.add(curEnabled.get(i));
//    			}
//    		}
//    		if (!nextAmpleTransOld.containsAll(curReduced)) {
//    			System.out.println("((((((((((((((((((((()))))))))))))))))))))");
//    			printTransitionSet(curEnabled, "curEnabled:");
//    			printTransitionSet(curAmpleTrans, "curAmpleTrans:");
//    			printTransitionSet(curReduced, "curReduced:");
//    			printTransitionSet(nextState.getEnabledTransitions(), "nextEnabled:");
//    			printTransitionSet(nextAmpleTransOld, "nextAmpleTransOld:");
//    			nextAmple.setAmpleChanged();
//    			HashSet<Transition> curEnabledIndicies = new HashSet<Transition>();
//        		for (int i=0; i<curEnabled.size(); i++) {
//        			curEnabledIndicies.add(curEnabled.get(i).getIndex());
//        		} 			
//    			// transToAdd = curReduced - nextAmpleOld
//    			LpnTranList overlyReducedTrans = getSetSubtraction(curReduced, nextAmpleTransOld);
//    			HashSet<Integer> transToAddIndices = new HashSet<Integer>();
//    			for (int i=0; i<overlyReducedTrans.size(); i++) {
//    				transToAddIndices.add(overlyReducedTrans.get(i).getIndex());
//    			}	
//    			HashSet<Integer> nextAmpleNewIndices = (HashSet<Integer>) curEnabledIndicies.clone();
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
//    				if (dependent.size() < nextAmpleNewIndices.size() && !dependentOnlyHasDummyTrans) 
//    					nextAmpleNewIndices = (HashSet<Integer>) dependent.clone();
//    				if (nextAmpleNewIndices.size() == 1)
//    					break;
//    			}
//       			LpnTranList nextAmpleNew = new LpnTranList();
//    			for (Integer nextAmpleNewIndex : nextAmpleNewIndices) {
//    				nextAmpleNew.add(this.getLpn().getTransition(nextAmpleNewIndex));
//    			}
//    			LpnTranList transToAdd = getSetSubtraction(nextAmpleNew, nextAmpleTransOld);
//    			boolean allTransToAddFired = false;
//    			if (cycleClosingMthdIndex == 2) {
//    				// For every transition t in transToAdd, if t was fired in the any state on stateStack, there is no need to put it to the new ample of nextState.
//            		if (transToAdd != null) {
//            			LpnTranList transToAddCopy = transToAdd.copy();
//            			HashSet<Integer> stateVisited = new HashSet<Integer>();
//            			stateVisited.add(nextState.getIndex());
//            			allTransToAddFired = allTransToAddFired(transToAddCopy, 
//            									allTransToAddFired, stateVisited, stateStackTop, lpnIndex);
//            		}
//    			}
//        		// Update the old ample of the next state 
//        		if (!allTransToAddFired || cycleClosingMthdIndex == 1) {
//        			nextAmple.getAmpleSet().clear();
//        			nextAmple.getAmpleSet().addAll(transToAdd);
//        			ampleSetTbl.get(nextState).addAll(transToAdd);
//        			printTransitionSet(nextAmpleNew, "nextAmpleNew:");
//        			printTransitionSet(transToAdd, "transToAdd:");
//        			System.out.println("((((((((((((((((((((()))))))))))))))))))))");
//       		}
//        		if (cycleClosingMthdIndex == 4) {
//        			nextAmple.getAmpleSet().clear();
//        			nextAmple.getAmpleSet().addAll(overlyReducedTrans);
//        			ampleSetTbl.get(nextState).addAll(overlyReducedTrans);
//        			printTransitionSet(nextAmpleNew, "nextAmpleNew:");
//        			printTransitionSet(transToAdd, "transToAdd:");
//        			System.out.println("((((((((((((((((((((()))))))))))))))))))))");
//        		}
//    		}
//    		// enabledSetTble stores the ample set at curState. 
//    		// The fully enabled set at each state is stored in the tranVector in each state. 		
//    		return (AmpleSet) nextAmple;
//    	}
		
		
//		
//    	for (State s : nextStateArray)
//    		if (s == null) 
//    			throw new NullPointerException();
//		cachedNecessarySets.clear();
//    	String cycleClosingMthd = Options.getCycleClosingMthd();
//    	AmpleSet nextAmple = new AmpleSet();
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
//          		printAmpleSet((LpnTranList)sgList[lpnIndex].getEnabledSetTbl().get(nextState), "Old ample at this state: ");
//  	    		LpnTranList oldLocalNextAmpleTrans = sgList[lpnIndex].getEnabledSetTbl().get(nextState);
//  	    		LpnTranList curLocalAmpleTrans = sgList[lpnIndex].getEnabledSetTbl().get(curState);
//  	    		LpnTranList reducedLocalTrans = new LpnTranList();
//  	    		LpnTranList curLocalEnabledTrans = curState.getEnabledTransitions(); 	
//          		System.out.println("The firedTran is a cycle closing transition.");
//       			if (cycleClosingMthd.toLowerCase().equals("behavioral") || cycleClosingMthd.toLowerCase().equals("state_search")) {
//       				// firedTran is a cycle closing transition. 
//       	    		for (int i=0; i<curLocalEnabledTrans.size(); i++) {
//       	    			if (!curLocalAmpleTrans.contains(curLocalEnabledTrans.get(i))) {
//       	    				reducedLocalTrans.add(curLocalEnabledTrans.get(i));
//       	    			}
//       	    		}
//       	    		if (!oldLocalNextAmpleTrans.containsAll(reducedLocalTrans)) {
//       	    			System.out.println("****** Transitions are possibly ignored in a cycle.******");
//       	    			printTransitionSet(curLocalEnabledTrans, "curLocalEnabled:");
//       	    			printTransitionSet(curLocalAmpleTrans, "curAmpleTrans:");
//       	    			printTransitionSet(reducedLocalTrans, "reducedLocalTrans:");
//       	    			printTransitionSet(nextState.getEnabledTransitions(), "nextLocalEnabled:");
//       	    			printTransitionSet(oldLocalNextAmpleTrans, "nextAmpleTransOld:");
//       	    			
//       	    			// nextAmple.setAmpleChanged();
//       	    			// ignoredTrans should not be empty here.
//       	    			LpnTranList ignoredTrans = getSetSubtraction(reducedLocalTrans, oldLocalNextAmpleTrans);
//       	    			HashSet<Transition> ignored = new HashSet<Transition>();
//       	    			for (int i=0; i<ignoredTrans.size(); i++) {
//       	    				ignored.add(new Transition(lpnIndex, ignoredTrans.get(i).getIndex()));
//       	    			}
//       	    			HashSet<Transition> newNextAmple = (HashSet<Transition>) nextEnabled.clone();
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
//          	    				if (dependent.size() < newNextAmple.size() && !dependentOnlyHasDummyTrans) 
//          	    					newNextAmple = (HashSet<Transition>) dependent.clone();
////          	    				if (nextAmpleNewIndices.size() == 1)
////          	    					break;
//          	    				DependentSet dependentSet = new DependentSet(newNextAmple, seed, 
//          	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//          	    				dependentSetQueue.add(dependentSet);
//          	    				LpnTranList newLocalNextAmpleTrans = new LpnTranList();
//              	    			for (Transition tran : newNextAmple) {
//              	    				if (tran.getLpnIndex() == lpnIndex)
//              	    					newLocalNextAmpleTrans.add(sgList[lpnIndex].getLpn().getTransition(tran.getTranIndex()));
//              	    			}
//              	    			LpnTranList transToAdd = getSetSubtraction(newLocalNextAmpleTrans, oldLocalNextAmpleTrans);
//              	    			transToAddMap.put(seed, transToAdd);
//          	    			}
//       	    			}
//       	    			else if (cycleClosingMthd.toLowerCase().equals("state_search")) {
//       	    				// For every transition t in ignored, if t was fired in any state on stateStack, there is no need to put it to the new ample of nextState.
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
//   	          	    				if (dependent.size() < newNextAmple.size() && !dependentOnlyHasDummyTrans) 
//   	          	    					newNextAmple = (HashSet<Transition>) dependent.clone();
//   	          	    				DependentSet dependentSet = new DependentSet(newNextAmple, seed, 
//   	          	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//   	          	    				dependentSetQueue.add(dependentSet);
//   	          	    				LpnTranList newLocalNextAmpleTrans = new LpnTranList();
//   	              	    			for (Transition tran : newNextAmple) {
//   	              	    				if (tran.getLpnIndex() == lpnIndex)
//   	              	    					newLocalNextAmpleTrans.add(sgList[lpnIndex].getLpn().getTransition(tran.getTranIndex()));
//   	              	    			}
//   	              	    			LpnTranList transToAdd = getSetSubtraction(newLocalNextAmpleTrans, oldLocalNextAmpleTrans);
//   	              	    			transToAddMap.put(seed, transToAdd);
//   	          	    			}
//       	    				}
//       	    				else { // All ignored transitions were fired before. It is safe to close the current cycle.
//               	    			HashSet<Transition> oldLocalNextAmple = new HashSet<Transition>();
//               	    			for (Transition tran : oldLocalNextAmpleTrans)
//               	    				oldLocalNextAmple.add(new Transition(tran.getLpn().getLpnIndex(), tran.getIndex()));
//      							for (Transition seed : oldLocalNextAmple) {
//      	          	    			HashSet<Transition> dependent = new HashSet<Transition>();
//      	      	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//      	      	    				printIntegerSet(dependent, sgList, "dependent set for transition in oldNextAmple is " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//      	      							+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//      	      	    				boolean dependentOnlyHasDummyTrans = true;
//      	      	    				for (Transition dependentTran : dependent) {
//      	      	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//      	      	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//      	      	    				}         	    							
//      	      	    				if (dependent.size() < newNextAmple.size() && !dependentOnlyHasDummyTrans) 
//      	      	    					newNextAmple = (HashSet<Transition>) dependent.clone();
//      	          	    			DependentSet dependentSet = new DependentSet(newNextAmple, seed, 
//      	      	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//      	      	    				dependentSetQueue.add(dependentSet);
//       							}
//       	    				}
//       	    			}
//       	    		}
//       	    		else {
//       	    			// oldNextAmpleTrans.containsAll(curReducedTrans) == true (safe to close the current cycle)
//       	    			HashSet<Transition> newNextAmple = (HashSet<Transition>) nextEnabled.clone();
//       	    			HashSet<Transition> oldLocalNextAmple = new HashSet<Transition>();
//      	    			for (Transition tran : oldLocalNextAmpleTrans)
//       	    				oldLocalNextAmple.add(new Transition(tran.getLpn().getLpnIndex(), tran.getIndex()));
//      	    			for (Transition seed : oldLocalNextAmple) {
//         	    			HashSet<Transition> dependent = new HashSet<Transition>();
//      	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//      	    				printIntegerSet(dependent, sgList, "dependent set for transition in oldNextAmple is " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//      							+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//      	    				boolean dependentOnlyHasDummyTrans = true;
//      	    				for (Transition dependentTran : dependent) {
//      	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//      	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//      	    				}         	    							
//      	    				if (dependent.size() < newNextAmple.size() && !dependentOnlyHasDummyTrans) 
//      	    					newNextAmple = (HashSet<Transition>) dependent.clone();
//          	    			DependentSet dependentSet = new DependentSet(newNextAmple, seed, 
//      	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//      	    				dependentSetQueue.add(dependentSet);
//						}
//       	    		}
//       			}
//       			else if (cycleClosingMthd.toLowerCase().equals("strong")) {
//       				LpnTranList ignoredTrans = getSetSubtraction(reducedLocalTrans, oldLocalNextAmpleTrans);
//   	    			HashSet<Transition> ignored = new HashSet<Transition>();
//   	    			for (int i=0; i<ignoredTrans.size(); i++) {
//   	    				ignored.add(new Transition(lpnIndex, ignoredTrans.get(i).getIndex()));
//   	    			}
//       				HashSet<Transition> allNewNextAmple = new HashSet<Transition>();
//       				for (Transition seed : ignored) {
//       					HashSet<Transition> newNextAmple = (HashSet<Transition>) nextEnabled.clone();
//       					HashSet<Transition> dependent = new HashSet<Transition>();
//   	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//   	    				printIntegerSet(dependent, sgList, "dependent set for transition in curLocalEnabled is " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//						+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//   	    				boolean dependentOnlyHasDummyTrans = true;
//   	    				for (Transition dependentTran : dependent) {
//   	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//   	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//   	    				}         	    							
//   	    				if (dependent.size() < newNextAmple.size() && !dependentOnlyHasDummyTrans) 
//   	    					newNextAmple = (HashSet<Transition>) dependent.clone();
//   	    				allNewNextAmple.addAll(newNextAmple);
//       				}
//       				// The strong cycle condition requires all seeds in ignored to be included in the allNewNextAmple, as well as dependent set for each seed.
//       				// So each seed should have the same new ample set.
//       				for (Transition seed : ignored) {
//       					DependentSet dependentSet = new DependentSet(allNewNextAmple, seed, 
//   	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//  	    				dependentSetQueue.add(dependentSet);
//       				}        	    		
//       			}		      		
//            }
//       		else { // firedTran is not a cycle closing transition. Compute next ample. 
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
////    		// Update the old ample of the next state 
//   		}
//
//			
////		boolean allTransToAddFired = false;
////		if (cycleClosingMthdIndex == 2) {
////			// For every transition t in transToAdd, if t was fired in the any state on stateStack, there is no need to put it to the new ample of nextState.
////  		if (transToAdd != null) {
////  			LpnTranList transToAddCopy = transToAdd.copy();
////  			HashSet<Integer> stateVisited = new HashSet<Integer>();
////  			stateVisited.add(nextState.getIndex());
////  			allTransToAddFired = allTransToAddFired(transToAddCopy, 
////  									allTransToAddFired, stateVisited, stateStackTop, lpnIndex);
////  		}
////		}
////		// Update the old ample of the next state 
////		if (!allTransToAddFired || cycleClosingMthdIndex == 1) {
////			nextAmple.getAmpleSet().clear();
////			nextAmple.getAmpleSet().addAll(transToAdd);
////			ampleSetTbl.get(nextState).addAll(transToAdd);
////			printTransitionSet(nextAmpleNew, "nextAmpleNew:");
////			printTransitionSet(transToAdd, "transToAdd:");
////			System.out.println("((((((((((((((((((((()))))))))))))))))))))");
////		}
////		if (cycleClosingMthdIndex == 4) {
////			nextAmple.getAmpleSet().clear();
////			nextAmple.getAmpleSet().addAll(ignoredTrans);
////			ampleSetTbl.get(nextState).addAll(ignoredTrans);
////			printTransitionSet(nextAmpleNew, "nextAmpleNew:");
////			printTransitionSet(transToAdd, "transToAdd:");
////			System.out.println("((((((((((((((((((((()))))))))))))))))))))");
////		}
////	}
////	// enabledSetTble stores the ample set at curState. 
////	// The fully enabled set at each state is stored in the tranVector in each state. 		
////	return (AmpleSet) nextAmple;
		return null;
 	}
			
 	private LpnTranList allIgnoredTransFired(LpnTranList ignoredTrans, 
 			HashSet<Integer> stateVisited, PrjState stateStackEntry, int lpnIndex, StateGraph sg) {
 		State state = stateStackEntry.get(lpnIndex);
 		System.out.println("state = " + state.getIndex());
 		State predecessor = stateStackEntry.getFather().get(lpnIndex);
 		if (predecessor != null)
 			System.out.println("predecessor = " + predecessor.getIndex());
 		if (predecessor == null || stateVisited.contains(predecessor.getIndex())) {
 			return ignoredTrans;
 		}
 		else 
 			stateVisited.add(predecessor.getIndex());
 		LpnTranList predecessorOldAmple = sg.getEnabledSetTbl().get(predecessor);//enabledSetTbl.get(predecessor);
 		for (Transition oldAmpleTran : predecessorOldAmple) {
 			State tmpState = sg.getNextStateMap().get(predecessor).get(oldAmpleTran);
 			if (tmpState.getIndex() == state.getIndex()) {
 				ignoredTrans.remove(oldAmpleTran);
 				break;
 			}
 		}
 		if (ignoredTrans.size()==0) {
 			return ignoredTrans;
 		}
 		else {
 			ignoredTrans = allIgnoredTransFired(ignoredTrans, stateVisited, stateStackEntry.getFather(), lpnIndex, sg);
 		}
		return ignoredTrans;
 	}

// 		for (Transition oldAmpleTran : oldAmple) {
//			State successor = nextStateMap.get(nextState).get(oldAmpleTran);
//			if (stateVisited.contains(successor.getIndex())) {
//				break;
//			}
//			else
//				stateVisited.add(successor.getIndex());
//			LpnTranList successorOldAmple = enabledSetTbl.get(successor);
//			// Either successor or sucessorOldAmple should not be null for a nonterminal state graph.
//			HashSet<Transition> transToAddFired = new HashSet<Transition>();
//			for (Transition tran : transToAddCopy) {
//				if (successorOldAmple.contains(tran))
//					transToAddFired.add(tran);
//			}
//			transToAddCopy.removeAll(transToAddFired);
//			if (transToAddCopy.size() == 0) {
//				allTransFired = true;
//				break;
//			}				
//			else {
//				allTransFired = allTransToAddFired(successorOldAmple, nextState, successor, 
//						transToAddCopy, allTransFired, stateVisited, stateStackEntry, lpnIndex);
//			}
//		}
//		return allTransFired;

	
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
				LpnState tmpCached = (LpnState)(lpnStateCache[i].add(tmp));
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
		 * by looking at stateStack, generate the trace showing the counter-example.
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
		int newStateCnt = 0;
		
		Stack<State[]> stateStack = new Stack<State[]>();
		Stack<LinkedList<Transition>> lpnTranStack = new Stack<LinkedList<Transition>>();
		Stack<Integer> curIndexStack = new Stack<Integer>();

		mddNode reachAll = null;
		mddNode reach = mddMgr.newNode();
		
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
						reach = mddMgr.newNode();

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
					} else
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
			LinkedList<Transition>[] curEnabledArray = new LinkedList[arraySize];
			LinkedList<Transition>[] nextEnabledArray = new LinkedList[arraySize];
			for (int i = 0; i < arraySize; i++) {
				StateGraph lpn_tmp = lpnList[i];
				LinkedList<Transition> enabledList = lpn_tmp.getEnabled(curStateArray[i]);
				curEnabledArray[i] = enabledList;
				enabledList = lpn_tmp.getEnabled(nextStateArray[i]);
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
			if (reachAll != null && mddMgr.contains(reachAll, localIdxArray) == true)
				existingState = true;
			else if (mddMgr.contains(reach, localIdxArray) == true)
				existingState = true;
			
			if (existingState == false) {
				mddMgr.add(reach, localIdxArray, compressed);
				newStateCnt++;
				stateStack.push(nextStateArray);
				lpnTranStack.push((LpnTranList) nextEnabledArray[0].clone());
				curIndexStack.push(0);
				totalStates++;
			}
		}

		double totalStateCnt = mddMgr.numberOfStates(reach);
		
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
		mddNode reach = mddMgr.newNode();

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
					reach = mddMgr.newNode();
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
					if (reachAll != null && mddMgr.contains(reachAll, localIdxArray) == true)
						existingState = true;
					else if (mddMgr.contains(reach, localIdxArray) == true)
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
		mddNode reach = mddMgr.newNode();
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
					reach = mddMgr.newNode();
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
						if (mddMgr.contains(reachSet, localIdxArray) == false && mddMgr.contains(reach, localIdxArray) == false && frontier.contains(nextStateArray) == false) {
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
		LinkedList<State>[] nextSetArray = (LinkedList<State>[]) (new LinkedList[arraySize]);
		for (int i = 0; i < arraySize; i++)
			nextSetArray[i] = new LinkedList<State>();

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
				System.out.println("---> # of prjStates found: " + mddMgr.numberOfStates(reachSet) 
						+ ",  CurSet.Size = " + mddMgr.numberOfStates(curMdd));
				continue;
			}

			if (exploredSet != null && mddMgr.contains(exploredSet, curStateArray) == true)
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
					if (reachSet != null && mddMgr.contains(reachSet, nextIdxArray) == true)
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
				+ (mddMgr.numberOfStates(reachSet)) + "\n"
				+ "---> peak total memory: " + peakTotalMem / 1000000F
				+ " MB\n" + "---> peak used memory: " + peakUsedMem / 1000000F
				+ " MB\n" + "---> peak MMD nodes: " + mddMgr.peakNodeCnt());

		return null;
	}

	
	/**
	 * partial order reduction (Original version of Hao's POR with behavioral analysis)
	 * This method is not used anywhere. See search_dfs_por_behavioral for POR with behavioral analysis. 
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
		mddNode reach = mddMgr.newNode();
				
		//init por
		verification.platu.por1.AmpleSet ampleClass = new verification.platu.por1.AmpleSet();
		//AmpleSubset ampleClass = new AmpleSubset();
		HashMap<Transition,HashSet<Transition>> indepTranSet = new HashMap<Transition,HashSet<Transition>>();
		
		if(approach == "state")
			indepTranSet = ampleClass.getIndepTranSet_FromState(lpnList, lpnTranRelation);
		else if (approach == "lpn")
			indepTranSet = ampleClass.getIndepTranSet_FromLPN(lpnList);
			
		System.out.println("finish get independent set!!!!");	
		
		boolean failure = false;
		int tranFiringCnt = 0;
		int arraySize = lpnList.length;;

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
					stateCount = mddMgr.numberOfStates(reach);
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
				isExisting = mddMgr.contains(reach, nextIdxArray);
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
			stateCount = mddMgr.numberOfStates(reach);
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
		mddNode reach = mddMgr.newNode();
				
		//init por
		verification.platu.por1.AmpleSet ampleClass = new verification.platu.por1.AmpleSet();
		//AmpleSubset ampleClass = new AmpleSubset();
		HashMap<Transition,HashSet<Transition>> indepTranSet = new HashMap<Transition,HashSet<Transition>>();
		
		if(approach == "state")
			indepTranSet = ampleClass.getIndepTranSet_FromState(sgList, lpnTranRelation);
		else if (approach == "lpn")
			indepTranSet = ampleClass.getIndepTranSet_FromLPN(sgList);
			
		System.out.println("finish get independent set!!!!");	
		
		boolean failure = false;
		int tranFiringCnt = 0;
		int arraySize = sgList.length;;

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
					stateCount = mddMgr.numberOfStates(reach);
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
				isExisting = mddMgr.contains(reach, nextIdxArray);
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
			stateCount = mddMgr.numberOfStates(reach);
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
			LpnTranList[] nextStickyTransArray, State nextState, LhpnFile LPN) {
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
	
//    private void printEnabledSetTbl(StateGraph[] sgList) {
//    	for (int i=0; i<sgList.length; i++) {
//    		System.out.println("******* enabledSetTbl for " + sgList[i].getLpn().getLabel() + " **********");
//    		for (State s : sgList[i].getEnabledSetTbl().keySet()) {
//        		System.out.print("S" + s.getIndex() + " -> ");
//        		printTransList(sgList[i].getEnabledSetTbl().get(s), "");
//        	}
//    	}		
//	}
    
    private HashSet<Transition> partialOrderReduction(State[] curStateArray,
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
    	if (Options.getUseDependentQueue()) {
    		DependentSetComparator depComp = new DependentSetComparator(tranFiringFreq); 
    		PriorityQueue<DependentSet> dependentSetQueue = new PriorityQueue<DependentSet>(curEnabled.size(), depComp);
    		for (Transition enabledTran : curEnabled) {
    			if (Options.getDebugMode())
    				System.out.println("@ beginning of partialOrderReduction, consider seed transition " + getNamesOfLPNandTrans(enabledTran));
    			HashSet<Transition> dependent = new HashSet<Transition>();
    			boolean enabledIsDummy = false;
    			boolean isCycleClosingAmpleComputation = false;
    			dependent = computeDependent(curStateArray,enabledTran,dependent,curEnabled,isCycleClosingAmpleComputation);
    			if (Options.getDebugMode())
    				printIntegerSet(dependent, "@ end of partialOrderReduction, dependent set for enabled transition "
    						+ getNamesOfLPNandTrans(enabledTran));						
    			
    			// TODO: temporarily dealing with dummy transitions (This requires the dummy transitions to have "_dummy" in their names.)				
    			if(isDummyTran(enabledTran.getLabel()))
    				enabledIsDummy = true;
    			DependentSet dependentSet = new DependentSet(dependent, enabledTran, enabledIsDummy);
    			dependentSetQueue.add(dependentSet);
    		}
    		//cachedNecessarySets.clear();
    		ready = dependentSetQueue.poll().getDependent();
    		//return ready;
    	}
    	else {
    		for (Transition enabledTran : curEnabled) {
    			if (Options.getDebugMode())
    				System.out.print("@ beginning of partialOrderReduction, consider seed transition " + getNamesOfLPNandTrans(enabledTran));    			
    			HashSet<Transition> dependent = new HashSet<Transition>();
    			boolean isCycleClosingAmpleComputation = false;
    			dependent = computeDependent(curStateArray,enabledTran,dependent,curEnabled,isCycleClosingAmpleComputation);
    			if (Options.getDebugMode()) {
    				printIntegerSet(dependent, "@ end of partialOrderReduction, dependent set for enabled transition "
    						+ getNamesOfLPNandTrans(enabledTran));						
    			}
    			if (ready.isEmpty() || dependent.size() < ready.size())
    				ready = dependent;
    			if (ready.size() == 1) {
    				cachedNecessarySets.clear();
    				return ready;
    			}	
    		}
    	}
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
//				boolean isCycleClosingAmpleComputation = false;
//				dependent = computeDependent(curStateArray,enabledTran,dependent,curEnabled,staticMap,isCycleClosingAmpleComputation);
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
//				boolean isCycleClosingAmpleComputation = false;
//				dependent = computeDependent(curStateArray,enabledTran,dependent,curEnabled,staticMap,isCycleClosingAmpleComputation);
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

	private boolean isDummyTran(String tranName) {
		if (tranName.contains("_dummy"))
			return true;
		else
			return false;
	}    
    
    private HashSet<Transition> computeDependent(State[] curStateArray,
			Transition seedTran, HashSet<Transition> dependent, HashSet<Transition> curEnabled, boolean isCycleClosingAmpleComputation) { 	
		// disableSet is the set of transitions that can be disabled by firing enabledLpnTran, or can disable enabledLpnTran.
		HashSet<Transition> disableSet = staticDependency.get(seedTran).getDisableSet(); 
		HashSet<Transition> otherTransDisableEnabledTran = staticDependency.get(seedTran).getOtherTransDisableCurTranSet();
		if (Options.getDebugMode()) {
			System.out.println("@ beginning of computeDependent, consider transition " + getNamesOfLPNandTrans(seedTran));
			printIntegerSet(disableSet, "Disable set for " + getNamesOfLPNandTrans(seedTran));
		}
		dependent.add(seedTran);
//		for (Transition lpnTranPair : canModifyAssign) {
//			if (curEnabled.contains(lpnTranPair))
//				dependent.add(lpnTranPair);
//		}
		if (Options.getDebugMode()) 
			printIntegerSet(dependent, "@ computeDependent at 0, dependent set for " + getNamesOfLPNandTrans(seedTran));		
		// dependent is equal to enabled. Terminate.
		if (dependent.size() == curEnabled.size()) {
			if (Options.getDebugMode()) {
				System.out.println("Check 0: Size of dependent is equal to enabled. Return dependent.");
			}
			return dependent;
		}
		for (Transition tranInDisableSet : disableSet) {
			if (Options.getDebugMode()) 
				System.out.println("Consider transition in the disable set of "  
						+ getNamesOfLPNandTrans(seedTran) + ": "
						+ getNamesOfLPNandTrans(tranInDisableSet));			
			if (curEnabled.contains(tranInDisableSet) && !dependent.contains(tranInDisableSet)
					&& (!tranInDisableSet.isPersistent() || otherTransDisableEnabledTran.contains(tranInDisableSet))) {
				dependent.addAll(computeDependent(curStateArray,tranInDisableSet,dependent,curEnabled,isCycleClosingAmpleComputation));
				if (Options.getDebugMode()) {
					printIntegerSet(dependent, "@ computeDependent at 1 for transition " + getNamesOfLPNandTrans(seedTran));				
				}
			}
			else if (!curEnabled.contains(tranInDisableSet)) {
				if(Options.getPOR().toLowerCase().equals("tboff")  // no trace-back
						|| (Options.getCycleClosingAmpleMethd().toLowerCase().equals("cctboff") && isCycleClosingAmpleComputation)) {
					dependent.addAll(curEnabled);
					break;
				}
				HashSet<Transition> necessary = null;
				if (Options.getDebugMode()) {
					printCachedNecessarySets();
				}				
				if (cachedNecessarySets.containsKey(tranInDisableSet)) {
					if (Options.getDebugMode()) {
						printCachedNecessarySets();
						System.out.println("@ computeDependent: Found transition " + getNamesOfLPNandTrans(tranInDisableSet) + "in the cached necessary sets.");
					}
					necessary = cachedNecessarySets.get(tranInDisableSet);
				}
				else {
					if (Options.getDebugMode())
						System.out.println("==== Compute necessary using DFS ====");					
					if (visitedTrans == null)
						visitedTrans = new HashSet<Transition>();
					else
						visitedTrans.clear();
					necessary = computeNecessary(curStateArray,tranInDisableSet,dependent,curEnabled);//, tranInDisableSet.getFullLabel());
				}	
				if (necessary != null && !necessary.isEmpty()) {
					cachedNecessarySets.put(tranInDisableSet, necessary);
					if (Options.getDebugMode()) 
						printIntegerSet(necessary, "@ computeDependent, necessary set for transition " + getNamesOfLPNandTrans(tranInDisableSet));					
					for (Transition tranNecessary : necessary) {
						if (!dependent.contains(tranNecessary)) {
							if (Options.getDebugMode()) {
								printIntegerSet(dependent,"Check if the newly found necessary transition is in the dependent set of " + getNamesOfLPNandTrans(seedTran));
								System.out.println("It does not contain this transition found by computeNecessary: " 
								+ getNamesOfLPNandTrans(tranNecessary) + ". Compute its dependent set."); 
							}
							dependent.addAll(computeDependent(curStateArray,tranNecessary,dependent,curEnabled,isCycleClosingAmpleComputation));
						}
						else {
							if (Options.getDebugMode()) {
								printIntegerSet(dependent, "Check if the newly found necessary transition is in the dependent set. Dependent set for " + getNamesOfLPNandTrans(seedTran));
								System.out.println("It already contains this transition found by computeNecessary: " 
										 + getNamesOfLPNandTrans(tranNecessary) + ".");
							}
						}
					}
				}
				else {
					if (Options.getDebugMode()) {
						if (necessary == null) 
							System.out.println("necessary set for transition " + getNamesOfLPNandTrans(seedTran) + " is null.");						
						else 
							System.out.println("necessary set for transition " + getNamesOfLPNandTrans(seedTran) + " is empty.");						
					}			
					//dependent.addAll(curEnabled);
					return curEnabled;
				}
				if (Options.getDebugMode()) {
					printIntegerSet(dependent,"@ computeDependent at 2, dependent set for transition " + getNamesOfLPNandTrans(seedTran)); 
				}					
			}
			else if (dependent.contains(tranInDisableSet)) {
				if (Options.getDebugMode()) {
					printIntegerSet(dependent,"@ computeDependent at 3 for transition " + getNamesOfLPNandTrans(seedTran));
					System.out.println("Transition " + getNamesOfLPNandTrans(tranInDisableSet) + " is already in the dependent set of "
							+ getNamesOfLPNandTrans(seedTran) + ".");
				}
			}
		}
		return dependent;
	}

	private String getNamesOfLPNandTrans(Transition tran) {
		return tran.getLpn().getLabel() + "(" + tran.getLabel() + ")";
	}

	private HashSet<Transition> computeNecessary(State[] curStateArray,
			Transition tran, HashSet<Transition> dependent,
			HashSet<Transition> curEnabled) {
		if (Options.getDebugMode()) {
			System.out.println("@ computeNecessary, consider transition: " + getNamesOfLPNandTrans(tran));
		}			
		if (cachedNecessarySets.containsKey(tran)) {
			if (Options.getDebugMode()) {
				printCachedNecessarySets();
				System.out.println("@ computeNecessary: Found transition " + getNamesOfLPNandTrans(tran)
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
					System.out.println("####### compute nMarking for transition " + getNamesOfLPNandTrans(tran) + "########");
				}				
				HashSet<Transition> nMarkingTemp = new HashSet<Transition>();				
				String placeName = tran.getLpn().getPlaceList()[placeIndex]; 	
				Transition[] presetTrans = tran.getLpn().getPlace(placeName).getPreset();
				if (Options.getDebugMode()) {
					System.out.println("@ nMarking: Consider preset place of " + getNamesOfLPNandTrans(tran) + ": " + placeName);										
				}	
				for (int j=0; j < presetTrans.length; j++) {
					Transition presetTran = presetTrans[j];
					if (Options.getDebugMode()) {
						System.out.println("@ nMarking: Preset of place " + placeName + " has transition(s): ");
						for (int k=0; k<presetTrans.length; k++) {
							System.out.print(getNamesOfLPNandTrans(presetTrans[k]) + ", ");
						}
						System.out.println("");
						System.out.println("@ nMarking: Consider transition of " + getNamesOfLPNandTrans(presetTran));
					}
					if (curEnabled.contains(presetTran)) {
						if (Options.getDebugMode()) {
							System.out.println("@ nMarking: curEnabled contains transition " 
									+ getNamesOfLPNandTrans(presetTran) + "). Add to nMarkingTmp.");
						}	
						nMarkingTemp.add(presetTran);
					}
					else {
						if (visitedTrans.contains(presetTran)) {//seedTranInDisableSet.getVisitedTrans().contains(presetTran)) {
							if (Options.getDebugMode()) {
								System.out.println("@ nMarking: Transition " + getNamesOfLPNandTrans(presetTran) + " was visted before"); 

							}
							if (cachedNecessarySets.containsKey(presetTran)) {
								if (Options.getDebugMode()) {
									printCachedNecessarySets();
									System.out.println("@nMarking: Found transition " + getNamesOfLPNandTrans(presetTran)
											+ "'s necessary set in the cached necessary sets. Add it to nMarkingTemp.");
								}
								nMarkingTemp = cachedNecessarySets.get(presetTran);
							}		
							continue;
						}
						else
							visitedTrans.add(presetTran);
						if (Options.getDebugMode()) {
							System.out.println("~~~~~~~~~ transVisited ~~~~~~~~~");
							for (Transition visitedTran :visitedTrans) {
								System.out.println(getNamesOfLPNandTrans(visitedTran));
							}
							System.out.println("@ nMarking, before call computeNecessary: consider transition: " 
									+ getNamesOfLPNandTrans(presetTran));
							System.out.println("@ nMarking: transition " + getNamesOfLPNandTrans(presetTran) + " is not enabled. Compute its necessary set.");
						}
						HashSet<Transition> tmp = computeNecessary(curStateArray, presetTran, dependent, curEnabled);
						if (tmp != null) {
							nMarkingTemp.addAll(tmp);
							if (Options.getDebugMode()) {
								System.out.println("@ nMarking: tmp returned from computeNecessary for " + getNamesOfLPNandTrans(presetTran) + " is not null.");
								printIntegerSet(nMarkingTemp, getNamesOfLPNandTrans(presetTran) + "'s nMarkingTemp");
							}
						}	
						else  
							if (Options.getDebugMode()) {
								System.out.println("@ nMarking: necessary set for transition " 
										+ getNamesOfLPNandTrans(presetTran) + " is null.");
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
		int[] varValueVector = curStateArray[tran.getLpn().getLpnIndex()].getVector();
		//HashSet<Transition> canEnable = staticMap.get(tran).getEnableBySettingEnablingTrue();
		ArrayList<HashSet<Transition>> canEnable = staticDependency.get(tran).getOtherTransSetCurTranEnablingTrue(); 
		if (Options.getDebugMode()) {
			System.out.println("####### compute nEnable for transition " + getNamesOfLPNandTrans(tran) + "########");
			printIntegerSet(canEnable, "@ nEnable: " + getNamesOfLPNandTrans(tran) + " can be enabled by");
		}
		if (tran.getEnablingTree() != null
				&& tran.getEnablingTree().evaluateExpr(tran.getLpn().getAllVarsWithValuesAsString(varValueVector)) == 0.0
				&& !canEnable.isEmpty()) {
			for(int index=0; index < tran.getConjunctsOfEnabling().size(); index++) {
				ExprTree conjunctExprTree = tran.getConjunctsOfEnabling().get(index);
				HashSet<Transition> nEnableForOneConjunct = null;
				if (Options.getDebugMode()) {
					printIntegerSet(canEnable, "@ nEnable: " + getNamesOfLPNandTrans(tran) + " can be enabled by");
					System.out.println("@ nEnable: Consider conjunct for transition " + getNamesOfLPNandTrans(tran) + ": " 
							+ conjunctExprTree.toString());	
				}
				if (conjunctExprTree.evaluateExpr(tran.getLpn().getAllVarsWithValuesAsString(varValueVector)) == 0.0) {
					HashSet<Transition> canEnableOneConjunctSet = staticDependency.get(tran).getOtherTransSetCurTranEnablingTrue().get(index);
					nEnableForOneConjunct = new HashSet<Transition>();
					if (Options.getDebugMode()) {
						System.out.println("@ nEnable: Conjunct for transition " + getNamesOfLPNandTrans(tran) + " " 
								+ conjunctExprTree.toString() + " is evaluated to FALSE.");
						printIntegerSet(canEnableOneConjunctSet, "@ nEnable: Transitions that can enable this conjunct are");														
					}
					for (Transition tranCanEnable : canEnableOneConjunctSet) {
						if (curEnabled.contains(tranCanEnable)) {
							nEnableForOneConjunct.add(tranCanEnable);
							if (Options.getDebugMode())	{
								System.out.println("@ nEnable: curEnabled contains transition " + getNamesOfLPNandTrans(tranCanEnable) + ". Add to nEnableOfOneConjunct.");
							}						
						}
						else {
							if (visitedTrans.contains(tranCanEnable)) {
								if (Options.getDebugMode()) {
									System.out.println("@ nEnable: Transition " + getNamesOfLPNandTrans(tranCanEnable) + " was visted before."); 

								}									
								if (cachedNecessarySets.containsKey(tranCanEnable)) {
									if (Options.getDebugMode()) {
										printCachedNecessarySets();
										System.out.println("@ nEnable: Found transition " + getNamesOfLPNandTrans(tranCanEnable)
												+ "'s necessary set in the cached necessary sets. Add it to nEnableOfOneConjunct.");
									}
									nEnableForOneConjunct.addAll(cachedNecessarySets.get(tranCanEnable));
								}				
								continue;
							}
							else 
								visitedTrans.add(tranCanEnable);
							if (Options.getDebugMode()) {
								System.out.println("@ nEnable: Transition " + getNamesOfLPNandTrans(tranCanEnable) 
										+ " is not enabled. Compute its necessary set.");
							}
							HashSet<Transition> tmp = computeNecessary(curStateArray, tranCanEnable, dependent, curEnabled);
							if (tmp != null) {
								nEnableForOneConjunct.addAll(tmp);
								if (Options.getDebugMode()) {
									System.out.println("@ nEnable: tmp returned from computeNecessary for " + getNamesOfLPNandTrans(tranCanEnable) + ": ");
									printIntegerSet(tmp, "");
									printIntegerSet(nEnableForOneConjunct, getNamesOfLPNandTrans(tranCanEnable) + "'s nEnableOfOneConjunct");
								}
							}					
							else
								if (Options.getDebugMode()) {
									System.out.println("@ nEnable: necessary set for transition " 
											+ getNamesOfLPNandTrans(tranCanEnable) + " is null.");
								}				
						}
					}
					if (!nEnableForOneConjunct.isEmpty()) {
						if (nEnable == null 
								|| setSubstraction(nEnableForOneConjunct, dependent).size() < setSubstraction(nEnable, dependent).size()) {
							//&& !nEnableForOneConjunct.isEmpty())) {
							if (Options.getDebugMode()) {
								System.out.println("@ nEnable: nEnable for transition " + getNamesOfLPNandTrans(tran) +" is replaced by nEnableForOneConjunct.");
								printIntegerSet(nEnable, "nEnable");
								printIntegerSet(nEnableForOneConjunct, "nEnableForOneConjunct");
							}
							nEnable = nEnableForOneConjunct;
						}
						else {
							if (Options.getDebugMode()) {
								System.out.println("@ nEnable: nEnable for transition " + getNamesOfLPNandTrans(tran) +" remains unchanged.");
								printIntegerSet(nEnable, "nEnable");
							}
						}				
					}
				}
				else {
					if (Options.getDebugMode()) {
						System.out.println("@ nEnable: Conjunct for transition " + getNamesOfLPNandTrans(tran) + " " 
								+ conjunctExprTree.toString() + " is evaluated to TRUE. No need to trace back on it.");
					}
				}			
			}
		}
		else {
			if (Options.getDebugMode()) {
				if (tran.getEnablingTree() == null) {
					System.out.println("@ nEnable: transition " + getNamesOfLPNandTrans(tran) + " has no enabling condition.");
				}
				else if (tran.getEnablingTree().evaluateExpr(tran.getLpn().getAllVarsWithValuesAsString(varValueVector)) !=0.0) {
					System.out.println("@ nEnable: transition " + getNamesOfLPNandTrans(tran) + "'s enabling condition is true.");
				}
				else if (tran.getEnablingTree() != null
						&& tran.getEnablingTree().evaluateExpr(tran.getLpn().getAllVarsWithValuesAsString(varValueVector)) == 0.0
						&& canEnable.isEmpty()) {
					System.out.println("@ nEnable: transition " + getNamesOfLPNandTrans(tran)
							+ "'s enabling condition is false, but no other transitions that can help to enable it were found .");
				}
				printIntegerSet(nMarking, "=== nMarking for transition " + getNamesOfLPNandTrans(tran));
				printIntegerSet(nEnable, "=== nEnable for transition " + getNamesOfLPNandTrans(tran));
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
		else if (nMarking == null && nEnable == null) {
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
				else {
					cachedNecessarySets.put(tran, nEnable);
					if (Options.getDebugMode()) {
						printCachedNecessarySets();
					}
					return nEnable;
				}
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
	
	private HashSet<Transition> setSubstraction(
			HashSet<Transition> left, HashSet<Transition> right) {
		HashSet<Transition> sub = new HashSet<Transition>();
		for (Transition lpnTranPair : left) {
			if (!right.contains(lpnTranPair))
				sub.add(lpnTranPair);
		}
		return sub;
	}
	
	public boolean stateOnStack(int lpnIndex, State curState, HashSet<PrjState> stateStack) {
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
	
	private void printIntegerSet(HashSet<Transition> Trans, String setName) {
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
	
	private void printIntegerSet(ArrayList<HashSet<Transition>> transSet, String setName) {
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
