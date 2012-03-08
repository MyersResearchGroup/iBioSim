package verification.platu.logicAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import lpn.parser.Abstraction;
import lpn.parser.LhpnFile;
import lpn.parser.LpnProcess;
import lpn.parser.Place;
import lpn.parser.Transition;
import verification.platu.MDD.MDT;
import verification.platu.MDD.Mdd;
import verification.platu.MDD.mddNode;
import verification.platu.common.IndexObjMap;
import verification.platu.lpn.LPNTranRelation;
import verification.platu.lpn.LpnTranList;
import verification.platu.partialOrders.*;
import verification.platu.partialOrders.AmpleSet;
import verification.platu.por1.*;
import verification.platu.project.PrjState;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

public class Analysis {

	private LinkedList<Transition> traceCex;
	protected Mdd mddMgr = null;


	
	public Analysis(StateGraph[] lpnList, State[] initStateArray, LPNTranRelation lpnTranRelation, String method) {
		traceCex = new LinkedList<Transition>();
		mddMgr = new Mdd(lpnList.length);

		if (method.equals("dfs")) {
			//if (Options.getPOR().equals("off")) {
				this.search_dfs(lpnList, initStateArray);
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
	 * This constructor performs dfs with partial order reduction.
	 * @param lpnList
	 * @param applyPOR
	 */
	//public Analysis(StateGraph[] lpnList, State[] initStateArray, LPNTranRelation lpnTranRelation, String method) {
	public Analysis(StateGraph[] lpnList){
		traceCex = new LinkedList<Transition>();
		mddMgr = new Mdd(lpnList.length);

//		if (method.equals("dfs")) {
//			//if (Options.getPOR().equals("off")) {
//				this.search_dfs(lpnList, initStateArray, applyPOR);
//				//this.search_dfs_mdd_1(lpnList, initStateArray);
//				//this.search_dfs_mdd_2(lpnList, initStateArray);
//			//}
//			//else
//			//	this.search_dfs_por(lpnList, initStateArray, lpnTranRelation, "state");
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
	 * @param curLocalStateArray
	 * @param enabledArray
	 */
	
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

		HashSet<PrjState> prjStateSet = new HashSet<PrjState>();
		
		PrjState initPrjState = new PrjState(initStateArray);
		prjStateSet.add(initPrjState);
		//initPrjState.print(getLpnList(sgList));
		
		PrjState stateStackTop = initPrjState;
//		System.out.println("%%%%%%% Add states to stateStack%%%%%%%%");
//		printStateArray(stateStackTop.toStateArray());
		stateStack.add(stateStackTop);
		
		constructDstLpnList(sgList);
//		printDstLpnList(sgList);
		
		boolean init = true;
		LpnTranList initEnabled = sgList[0].getEnabled(initStateArray[0], init);
		lpnTranStack.push(initEnabled.clone());
		curIndexStack.push(0);
		init = false;
//		System.out.println("call getEnabled on initStateArray at 0: ");
//		System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//		printTransitionSet(initEnabled, "");

		main_while_loop: while (failure == false && stateStack.size() != 0) {
//			System.out.println("$$$$$$$$$$$ loop begins $$$$$$$$$$");
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
//			System.out.println("curIndex = " + curIndex);
			LinkedList<Transition> curEnabled = lpnTranStack.peek();
//			System.out.println("------- curStateArray ----------");
//			printStateArray(curStateArray);
//			System.out.println("+++++++ curEnabled trans ++++++++");
//			printTransLinkedList(curEnabled);
			// If all enabled transitions of the current LPN are considered,
			// then consider the next LPN
			// by increasing the curIndex.
			// Otherwise, if all enabled transitions of all LPNs are considered,
			// then pop the stacks.
			if (curEnabled.size() == 0) {
				lpnTranStack.pop();
//				System.out.println("+++++++ Pop trans off lpnTranStack ++++++++");
				curIndexStack.pop();
//				System.out.println("+++++++ Pop index off curIndexStack ++++++++");
				curIndex++;
//				System.out.println("curIndex = " + curIndex);
				while (curIndex < numLpns) {
//					System.out.println("call getEnabled on curStateArray at 1: ");
					curEnabled = (sgList[curIndex].getEnabled(curStateArray[curIndex], init)).clone();
					if (curEnabled.size() > 0) {
//						System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//						printTransLinkedList(curEnabled);
						lpnTranStack.push(curEnabled);
						curIndexStack.push(curIndex);
//						printIntegerStack("curIndexStack after push 1", curIndexStack);
						break;
					} 					
					curIndex++;
				}
			}
			if (curIndex == numLpns) {
				prjStateSet.add(stateStackTop);
//				System.out.println("%%%%%%% Remove stateStackTop from stateStack %%%%%%%%");
//				printStateArray(stateStackTop.toStateArray());
				stateStack.remove(stateStackTop);
				stateStackTop = stateStackTop.getFather();
				continue;
			}

			Transition firedTran = curEnabled.removeLast();	
//			System.out.println("###################");			
//			System.out.println("Fired transition: " + firedTran.getName());
//			System.out.println("###################");
			State[] nextStateArray = sgList[curIndex].fire(sgList, curStateArray, firedTran);
			tranFiringCnt++;

			// Check if the firedTran causes disabling error or deadlock.
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] curEnabledArray = new LinkedList[numLpns];
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] nextEnabledArray = new LinkedList[numLpns];
			for (int i = 0; i < numLpns; i++) {
				StateGraph sg = sgList[i];
//				System.out.println("call getEnabled on curStateArray at 2: ");
				LinkedList<Transition> enabledList = sg.getEnabled(curStateArray[i], init);
				curEnabledArray[i] = enabledList;
//				System.out.println("call getEnabled on nextStateArray at 3: ");
				enabledList = sg.getEnabled(nextStateArray[i], init);
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
			Boolean	existingState = prjStateSet.contains(nextPrjState) || stateStack.contains(nextPrjState);		
			if (existingState == false) {
				//prjStateSet.add(nextPrjState);
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				stateStackTop = nextPrjState;
//				System.out.println("%%%%%%% Add state to stateStack %%%%%%%%");
//				printStateArray(stateStackTop.toStateArray());
				stateStack.add(stateStackTop);
//				System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//				printTransitionSet((LpnTranList) nextEnabledArray[0], "");
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

	private void printStateArray(State[] stateArray) {
		for (int i=0; i<stateArray.length; i++) {
			System.out.print("S" + stateArray[i].getIndex() + " ");
		}
		System.out.println("");
	}

	private void printTransLinkedList(LinkedList<Transition> curPop) {
		for (int i=0; i< curPop.size(); i++) {
			System.out.print(curPop.get(i).getName() + " ");
		}
		System.out.println("");
	}

	private void printIntegerStack(String stackName, Stack<Integer> curIndexStack) {
		System.out.println("+++++++++" + stackName + "+++++++++");
		for (int i=0; i < curIndexStack.size(); i++) {
			System.out.println(stackName + "[" + i + "]" + curIndexStack.get(i));
		}
		System.out.println("------------------");
	}

	private void printDstLpnList(StateGraph[] lpnList) {
		System.out.println("++++++ dstLpnList ++++++");
		for (int i=0; i<lpnList.length; i++) {
			LhpnFile curLPN = lpnList[i].getLpn();
			System.out.println("LPN: " + curLPN.getLabel());
			Transition[] allTrans = curLPN.getAllTransitions(); 
			for (int j=0; j< allTrans.length; j++) {
				System.out.print(allTrans[j].getName() + ": ");
				for (int k=0; k< allTrans[j].getDstLpnList().size(); k++) {
					System.out.print(allTrans[j].getDstLpnList().get(k).getLabel() + ",");
				}
				System.out.print("\n");
			}
			System.out.println("----------------");
		}
		System.out.println("++++++++++++++++++++");
	}

	private void constructDstLpnList(StateGraph[] lpnList) {
		for (int i=0; i<lpnList.length; i++) {
			LhpnFile curLPN = lpnList[i].getLpn();
			Transition[] allTrans = curLPN.getAllTransitions();
			for (int j=0; j<allTrans.length; j++) {
				Transition curTran = allTrans[j];
				for (int k=0; k<lpnList.length; k++) {
					curTran.setDstLpnList(lpnList[k].getLpn());
				}
								
			}
		}
	}
	
	/**
	 * This method performs first-depth search on a single LPN and applies partial order reduction technique with the traceback. 
	 * @param sgList
	 * @param initStateArray
	 * @param cycleClosingMthdIndex 
	 * @return
	 */
	public StateGraph search_dfsPOR(final StateGraph[] sgList, final State[] initStateArray, int cycleClosingMthdIndex) {
		System.out.println("---> calling function search_dfsPORSingleLpn");
		if (cycleClosingMthdIndex == 0) 
			System.out.println("---> POR with sticky transitions");
		else if (cycleClosingMthdIndex == 3)
			System.out.println("---> POR without cycle closing check");
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

		HashSet<PrjState> prjStateSet = new HashSet<PrjState>();
		
		PrjState initPrjState = new PrjState(initStateArray);
		prjStateSet.add(initPrjState);
		
		PrjState stateStackTop = initPrjState;
		stateStack.add(stateStackTop);
		
		// Find static pieces for POR. 
		Transition[] allTransitions = null; 
		HashMap<Integer, HashMap<Integer, StaticSets>> staticSetsMap = new HashMap<Integer, HashMap<Integer, StaticSets>>();
		if (cycleClosingMthdIndex == 0) { // Sticky transitions
			allTransitions = assignStickyTransitions(sgList[0].getLpn());
		}
		if (cycleClosingMthdIndex == 3) { // No cycle closing
			allTransitions = sgList[0].getLpn().getAllTransitions();
		}	
		HashMap<Integer, StaticSets> tmpMap = new HashMap<Integer, StaticSets>();
		for (Transition curTran: allTransitions) {
			StaticSets curStatic = new StaticSets(sgList[0].getLpn(), curTran, allTransitions);
			curStatic.buildDisableSet();
			curStatic.buildEnableSet();
			tmpMap.put(curTran.getIndex(), curStatic);
		}
		staticSetsMap.put(sgList[0].getLpn().getIndex(), tmpMap);
//		printStaticSetMap(staticSetsMap);
		boolean init = true;
		AmpleSet initAmple = new AmpleSet();
		initAmple = sgList[0].getAmple(initStateArray[0], staticSetsMap, stateStack, init);
		lpnTranStack.push(initAmple.getAmpleSet());
		curIndexStack.push(0);
		init = false;
//		System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//		printTransitionSet((LpnTranList) initAmple.getAmpleSet(), "");

		main_while_loop: while (failure == false && stateStack.size() != 0) {
//			System.out.println("$$$$$$$$$$$ loop begins $$$$$$$$$$");
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
//			System.out.println("curIndex = " + curIndex);
			LinkedList<Transition> curAmpleTrans = lpnTranStack.peek();
			curAmpleTrans = lpnTranStack.peek();

			// If all enabled transitions of the current LPN are considered,
			// then consider the next LPN
			// by increasing the curIndex.
			// Otherwise, if all enabled transitions of all LPNs are considered,
			// then pop the stacks.
			if (curAmpleTrans.size() == 0) {
				lpnTranStack.pop();
//				System.out.println("+++++++ Pop trans off lpnTranStack ++++++++");
				curIndexStack.pop();
//				System.out.println("+++++++ Pop index off curIndexStack ++++++++");
				curIndex++;
				while (curIndex < numLpns) {
//					System.out.println("call getEnabled on curStateArray at 1: ");
					LpnTranList tmpAmpleTrans = (LpnTranList) (sgList[curIndex].getAmple(curStateArray[curIndex], staticSetsMap, stateStack, init)).getAmpleSet();
					curAmpleTrans = tmpAmpleTrans.clone();
					//printTransitionSet(curEnabled, "curEnabled set");
					if (curAmpleTrans.size() > 0) {
//						System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//						printTransLinkedList(curAmpleTrans);
						lpnTranStack.push(curAmpleTrans);
						curIndexStack.push(curIndex);
//						printIntegerStack("curIndexStack after push 1", curIndexStack);
						break;
					} 					
					curIndex++;
				}
			}
			if (curIndex == numLpns) {
				prjStateSet.add(stateStackTop);
//				System.out.println("%%%%%%% Remove stateStackTop from stateStack %%%%%%%%");
				stateStack.remove(stateStackTop);
				stateStackTop = stateStackTop.getFather();
				continue;
			}

			Transition firedTran = curAmpleTrans.removeLast();	
//			System.out.println("###################");
//			System.out.println("Fired Transition: " + firedTran.getName());
//			System.out.println("###################");
			State[] nextStateArray = sgList[curIndex].fire(sgList, curStateArray, firedTran);
			tranFiringCnt++;

			// Check if the firedTran causes disabling error or deadlock.
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] curAmpleArray = new LinkedList[numLpns];
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] nextAmpleArray = new LinkedList[numLpns];
			for (int i = 0; i < numLpns; i++) {
				StateGraph sg_tmp = sgList[i];				
//				System.out.println("call getAmple on curStateArray at 2: i = " + i);
				AmpleSet ampleList = sg_tmp.getAmple(curStateArray[i], staticSetsMap, stateStack, init);
				curAmpleArray[i] = ampleList.getAmpleSet();
//				System.out.println("call getAmpleRefinedCycleRule on nextStateArray at 3: i = " + i);
				ampleList = sg_tmp.getAmple(nextStateArray[i], staticSetsMap, stateStack, init);
				nextAmpleArray[i] = ampleList.getAmpleSet();				
				Transition disabledTran = firedTran.disablingError(
				curStateArray[i].getEnabledTransitions(), nextStateArray[i].getEnabledTransitions());
				if (disabledTran != null) {
					System.err.println("Disabling Error: "
							+ disabledTran.getFullLabel() + " is disabled by "
							+ firedTran.getFullLabel());
					failure = true;
					break main_while_loop;
				}
			}

			if (Analysis.deadLock(sgList, nextStateArray, staticSetsMap, stateStack, init) == true) {
				System.out.println("*** Verification failed: deadlock.");
				failure = true;
				break main_while_loop;
			}

			PrjState nextPrjState = new PrjState(nextStateArray);
			Boolean	existingState = prjStateSet.contains(nextPrjState) || stateStack.contains(nextPrjState);
						
			if (existingState == false) {
//				System.out.println("%%%%%%% existingSate == false %%%%%%%%");
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				stateStackTop = nextPrjState;
				stateStack.add(stateStackTop);
//				System.out.println("%%%%%%% Add state to stateStack %%%%%%%%");
//				printStateArray(stateStackTop.toStateArray());
//				System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//				printTransitionSet((LpnTranList) nextAmpleArray[0], "");
				lpnTranStack.push((LpnTranList) nextAmpleArray[0].clone());
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

		// This only works for a single LPN.
		return sgList[0];
	}
	
	/**
	 * This method performs first-depth search on multiple LPNs and applies partial order reduction technique with the traceback. 
	 * @param sgList
	 * @param initStateArray
	 * @param cycleClosingMthdIndex 
	 * @return
	 */
	public StateGraph search_dfsPORrefinedCycleRule(final StateGraph[] sgList, final State[] initStateArray, int cycleClosingMthdIndex) {
		System.out.println("---> calling function search_dfsPORrefinedCycleRule");
		if (cycleClosingMthdIndex == 1) 
			System.out.println("---> POR with behavioral analysis");
		else if (cycleClosingMthdIndex == 2)
			System.out.println("---> POR with behavioral analysis and state trace-back");	
		double peakUsedMem = 0;
		double peakTotalMem = 0;
		boolean failure = false;
		int tranFiringCnt = 0;
		int totalStates = 1;
		int numLpns = sgList.length;
		
		HashSet<PrjState> stateStack = new HashSet<PrjState>();
		Stack<LinkedList<Transition>> lpnTranStack = new Stack<LinkedList<Transition>>();
		Stack<Integer> curIndexStack = new Stack<Integer>();

		HashSet<PrjState> prjStateSet = new HashSet<PrjState>();
		
		PrjState initPrjState = new PrjState(initStateArray);
		prjStateSet.add(initPrjState);
		
		PrjState stateStackTop = initPrjState;
//		System.out.println("%%%%%%% Add states to stateStack %%%%%%%%");
//		printStateArray(stateStackTop.toStateArray());
		stateStack.add(stateStackTop);
		
		// Prepare static pieces for POR 		
		HashMap<Integer, HashMap<Integer, StaticSets>> staticSetsMap = new HashMap<Integer, HashMap<Integer, StaticSets>>(); 
		Transition[] allTransitions = sgList[0].getLpn().getAllTransitions();
		HashMap<Integer, StaticSets> tmpMap = new HashMap<Integer, StaticSets>();
		for (Transition curTran: allTransitions) {
			StaticSets curStatic = new StaticSets(sgList[0].getLpn(), curTran, allTransitions);
			curStatic.buildDisableSet();
			curStatic.buildEnableSet();
			tmpMap.put(curTran.getIndex(), curStatic);
		}
		staticSetsMap.put(sgList[0].getLpn().getIndex(), tmpMap);
		//printStaticSetMap(staticSetsMap);
		
//		System.out.println("call getAmple on initStateArray at 0: ");
		boolean init = true;
		AmpleSet initAmple = new AmpleSet();
		initAmple = sgList[0].getAmple(initStateArray[0], staticSetsMap, stateStack, init);
		lpnTranStack.push(initAmple.getAmpleSet());
		curIndexStack.push(0);
		init = false;
//		System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//		printTransitionSet((LpnTranList) initAmple.getAmpleSet(), "");

		main_while_loop: while (failure == false && stateStack.size() != 0) {
//			System.out.println("$$$$$$$$$$$ loop begins $$$$$$$$$$");
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
//			System.out.println("curIndex = " + curIndex);
			AmpleSet curAmple = new AmpleSet();
			LinkedList<Transition> curAmpleTrans = curAmple.getAmpleSet();
			curAmpleTrans = lpnTranStack.peek();
						
//			System.out.println("------- curStateArray ----------");
//			printStateArray(curStateArray);
//			System.out.println("+++++++ curAmple trans ++++++++");
//			printTransLinkedList(curAmpleTrans);
			
			// If all enabled transitions of the current LPN are considered,
			// then consider the next LPN
			// by increasing the curIndex.
			// Otherwise, if all enabled transitions of all LPNs are considered,
			// then pop the stacks.		
			if (curAmpleTrans.size() == 0) {
				lpnTranStack.pop();
//				System.out.println("+++++++ Pop trans off lpnTranStack ++++++++");
				curIndexStack.pop();
//				System.out.println("+++++++ Pop index off curIndexStack ++++++++");
				curIndex++;
//				System.out.println("curIndex = " + curIndex);
				while (curIndex < numLpns) {
//					System.out.println("call getEnabled on curStateArray at 1: ");
					LpnTranList tmpAmpleTrans = (LpnTranList) (sgList[curIndex].getAmple(curStateArray[curIndex], staticSetsMap, stateStack, init)).getAmpleSet();
					curAmpleTrans = tmpAmpleTrans.clone();
					//printTransitionSet(curEnabled, "curEnabled set");
					if (curAmpleTrans.size() > 0) {
//						System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//						printTransLinkedList(curAmpleTrans);
						lpnTranStack.push(curAmpleTrans);
						curIndexStack.push(curIndex);
//						printIntegerStack("curIndexStack after push 1", curIndexStack);
						break;
					} 					
					curIndex++;
				}
			}
			if (curIndex == numLpns) {
				prjStateSet.add(stateStackTop);
//				System.out.println("%%%%%%% Remove stateStackTop from stateStack %%%%%%%%");
//				printStateArray(stateStackTop.toStateArray());
				stateStack.remove(stateStackTop);
				stateStackTop = stateStackTop.getFather();
				continue;
			}

			Transition firedTran = curAmpleTrans.removeLast();	
//			System.out.println("###################");			
//			System.out.println("Fired transition: " + firedTran.getName());
//			System.out.println("###################");
			State[] nextStateArray = sgList[curIndex].fire(sgList, curStateArray, firedTran);
			tranFiringCnt++;

			// Check if the firedTran causes disabling error or deadlock.
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] curAmpleArray = new LinkedList[numLpns];
			@SuppressWarnings("unchecked")
			LinkedList<Transition>[] nextAmpleArray = new LinkedList[numLpns];
			boolean updatedAmpleDueToCycleRule = false;
			for (int i = 0; i < numLpns; i++) {
				StateGraph sg_tmp = sgList[i];
//				System.out.println("call getAmple on curStateArray at 2: i = " + i);
				AmpleSet ampleList = sg_tmp.getAmple(curStateArray[i], staticSetsMap, stateStack, init);
				curAmpleArray[i] = ampleList.getAmpleSet();
//				System.out.println("call getAmpleRefinedCycleRule on nextStateArray at 3: i = " + i);
				ampleList = sg_tmp.getAmpleRefinedCycleRule(curStateArray[i], nextStateArray[i], staticSetsMap, stateStack, init, cycleClosingMthdIndex);
				nextAmpleArray[i] = ampleList.getAmpleSet();
				if (ampleList.getAmpleChanged() && !updatedAmpleDueToCycleRule) {
					updatedAmpleDueToCycleRule = true;
				}
//				System.out.println("-------------- curAmpleArray --------------");
//				for (LinkedList<Transition> tranList : curAmpleArray) {
//					printTransLinkedList(tranList);
//				}
//				System.out.println("-------------- nextAmpleArray --------------");
//				for (LinkedList<Transition> tranList : nextAmpleArray) {
//					printTransLinkedList(tranList);
//				}
				Transition disabledTran = firedTran.disablingError(
						curStateArray[i].getEnabledTransitions(), nextStateArray[i].getEnabledTransitions());
				if (disabledTran != null) {
					System.err.println("Disabling Error: "
							+ disabledTran.getFullLabel() + " is disabled by "
							+ firedTran.getFullLabel());
					failure = true;
					break main_while_loop;
				}
			}

			if (Analysis.deadLock(sgList, nextStateArray, staticSetsMap, stateStack, init) == true) {
				System.out.println("*** Verification failed: deadlock.");
				failure = true;
				break main_while_loop;
			}

			PrjState nextPrjState = new PrjState(nextStateArray);
			Boolean	existingState = prjStateSet.contains(nextPrjState) || stateStack.contains(nextPrjState);
			if (existingState == true && updatedAmpleDueToCycleRule) {
				// cycle closing
//				System.out.println("%%%%%%% existingSate == true %%%%%%%%");
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				stateStackTop = nextPrjState;
				stateStack.add(stateStackTop);
//				System.out.println("%%%%%%% Add state to stateStack %%%%%%%%");
//				printStateArray(stateStackTop.toStateArray());
//				System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//				printTransitionSet((LpnTranList) nextAmpleArray[0], "");
				lpnTranStack.push((LpnTranList) nextAmpleArray[0].clone());
				curIndexStack.push(0);
			}			
			if (existingState == false) {
//				System.out.println("%%%%%%% existingSate == false %%%%%%%%");
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				stateStackTop = nextPrjState;
				stateStack.add(stateStackTop);
//				System.out.println("%%%%%%% Add state to stateStack %%%%%%%%");
//				printStateArray(stateStackTop.toStateArray());
//				System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
//				printTransitionSet((LpnTranList) nextAmpleArray[0], "");
				lpnTranStack.push((LpnTranList) nextAmpleArray[0].clone());
				curIndexStack.push(0);
				totalStates++;
			}
		}
		// end of main_while_loop
		
		double totalStateCnt = prjStateSet.size();
		
		System.out.println("---> final numbers: # LPN transition firings: "	+ tranFiringCnt 
				+ ", # of prjStates found: " + totalStateCnt 
				+ ", max_stack_depth: " + max_stack_depth 
				+ ", peak total memory: " + peakTotalMem / 1000000 + " MB"
				+ ", peak used memory: " + peakUsedMem / 1000000 + " MB");

		// This currently works for a single LPN.
		return sgList[0];
	}
	
	private void printStaticSetMap(
			HashMap<Integer, HashMap<Integer, StaticSets>> staticSetsMap) {
		System.out.println("^^^^^^^^^^^^ staticSetsMap ^^^^^^^^^^^^");
		for (Integer i : staticSetsMap.keySet()) {
			HashMap<Integer, StaticSets> tmp = staticSetsMap.get(i);
			for (Integer j : tmp.keySet()) {
				StaticSets statSets = tmp.get(j);
				printDisableOREnableSet(statSets.getLpn(), statSets.getTran(), statSets.getDisabled(), "disableSet");
				printDisableOREnableSet(statSets.getLpn(), statSets.getTran(), statSets.getEnable(), "enableSet");
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	private Transition[] assignStickyTransitions(LhpnFile lpn) {
		// allProcessTrans is a hashmap from a transition to its process color (integer). 
		HashMap<Transition, Integer> allProcessTrans = new HashMap<Transition, Integer>();
		// create an Abstraction object to call the divideProcesses method. 
		Abstraction abs = new Abstraction(lpn);
		abs.decomposeLpnIntoProcesses();
		allProcessTrans.putAll(
				 (HashMap<Transition, Integer>)abs.getTransWithProcIDs().clone());
		HashMap<Integer, LpnProcess> processMap = new HashMap<Integer, LpnProcess>();
		for (Iterator<Transition> tranIter = allProcessTrans.keySet().iterator(); tranIter.hasNext();) {
			Transition curTran = tranIter.next();
			Integer procId = allProcessTrans.get(curTran);
			if (!processMap.containsKey(procId)) {
				LpnProcess newProcess = new LpnProcess(procId);
				newProcess.addTranToProcess(curTran);
				if (curTran.getPreset() != null) {
					Place[] preset = curTran.getPreset();
					for (Place p : preset) {
						newProcess.addPlaceToProcess(p);
					}
				}
				processMap.put(procId, newProcess);
			}
			else {
				LpnProcess curProcess = processMap.get(procId);
				curProcess.addTranToProcess(curTran);
				if (curTran.getPreset() != null) {
					Place[] preset = curTran.getPreset();
					for (Place p : preset) {
						curProcess.addPlaceToProcess(p);
					}
				}
			}
		}
		
		for(Iterator<Integer> processMapIter = processMap.keySet().iterator(); processMapIter.hasNext();) {
			LpnProcess curProc = processMap.get(processMapIter.next());
			curProc.assignStickyTransitions();
			curProc.printProcWithStickyTrans();
		}
		return lpn.getAllTransitions();
	}

//	private void printPlacesIndices(ArrayList<String> allPlaces) {
//		System.out.println("Indices of all places: ");
//		for (int i=0; i < allPlaces.size(); i++) {
//			System.out.println(allPlaces.get(i) + "\t" + i);
//		}
//		
//	}
//
//	private void printTransIndices(Transition[] allTransitions) {
//		System.out.println("Indices of all transitions: ");
//		for (int i=0; i < allTransitions.length; i++) {
//			System.out.println(allTransitions[i].getName() + "\t" + allTransitions[i].getIndex());
//		}
//	}
//
	private void printDisableOREnableSet(LhpnFile lpn, Transition curTran,
			HashSet<Integer> curTranDisable, String setName) {
		System.out.print(setName + " for " + curTran.getName() + "(" + curTran.getIndex() + ")" + " is: ");
		if (curTranDisable.isEmpty()) {
			System.out.println("empty");
		}
		else {
			for (Iterator<Integer> curTranDisableIter = curTranDisable.iterator(); curTranDisableIter.hasNext();) {
				Integer tranInDisable = curTranDisableIter.next();
				System.out.print(curTran.getLpn().getAllTransitions()[tranInDisable] + "\t");
			}
			System.out.print("\n");
		}			
	}
	
	private void printTransitionSet(LpnTranList transitionSet, String setName) {
		if (!setName.isEmpty())
			System.out.print(setName + " is: ");
		if (transitionSet.isEmpty()) {
			System.out.println("empty");
		}
		else {
			for (Iterator<Transition> curTranIter = transitionSet.iterator(); curTranIter.hasNext();) {
				Transition tranInDisable = curTranIter.next();
				System.out.print(tranInDisable.getName() + " ");
			}
			System.out.print("\n");
		}
	}
//	
//	private void printTransitionSet(LinkedList<Transition> transitionSet, String setName) {
//		System.out.print(setName + " is: ");
//		if (transitionSet.isEmpty()) {
//			System.out.println("empty");
//		}
//		else {
//			for (Iterator<Transition> curTranIter = transitionSet.iterator(); curTranIter.hasNext();) {
//				Transition tranInDisable = curTranIter.next();
//				System.out.print(tranInDisable.getIndex() + " ");
//			}
//			System.out.print("\n");
//		}			
//	}




	
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
			// TODO: (??) Not sure about getting the state graph corresponding to firedTran. 
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
						int[] preset = lpnList[i].getLpn().getPresetIndex(curTran.getName());
						
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

		endoffunction: System.out.println("-------------------------------------\n"
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

				if (curLocalEnabled.size() == 0 || curLocalEnabled.getFirst().local() == true)
					continue;

				for (Transition firedTran : curLocalEnabled) {

					if (firedTran.local() == true)
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
	 * partial order reduction
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
			lpnList[i].getLpn().setIndex(i);
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
				lpnList[i].getLpn().setIndex(i);
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
						newCurEnabledArray[tran.getLpn().getIndex()].addLast(tran);

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
	 * Check if this project deadlocks in the current state 'stateArray'.	
	 * @param lpnArray
	 * @param stateArray
	 * @param staticSetsMap 
	 * @param enableSet 
	 * @param disableByStealingToken 
	 * @param disableSet 
	 * @param init 
	 * @return
	 */
	// Called by search search_dfsPORrefinedCycleRule
	public static boolean deadLock(StateGraph[] lpnArray, State[] stateArray, HashMap<Integer, HashMap<Integer, StaticSets>> staticSetsMap, HashSet<PrjState> stateStack, boolean init) {
		boolean deadlock = true;
		//System.out.println("@deadlock:");
		for (int i = 0; i < stateArray.length; i++) {
			LinkedList<Transition> tmp = lpnArray[i].getAmple(stateArray[i], staticSetsMap, stateStack,init).getAmpleSet();
			if (tmp.size() > 0) {
				deadlock = false;
				break;
			}
		}
		return deadlock;
	}

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

	/*
	 * Scan enabledArray, identify all sticky transitions other the firedTran, and return them.
	 * 
	 * Arguments remain constant.
	 */
	
	public static LpnTranList[] getStickyTrans(LpnTranList[] enabledArray, Transition firedTran) {
		int arraySize = enabledArray.length;
		LpnTranList[] stickyTranArray = new LpnTranList[arraySize];
		for (int i = 0; i < arraySize; i++) {
			stickyTranArray[i] = new LpnTranList();
			for (Transition tran : enabledArray[i]) {
				if (tran != firedTran) 
					stickyTranArray[i].add(tran);
			}
			
			if(stickyTranArray[i].size()==0)
				stickyTranArray[i] = null;
		}
		return stickyTranArray;
	}
	
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
					int[] preset = LPN.getPresetIndex(tran.getName());//tran.getPreSet();
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
}
