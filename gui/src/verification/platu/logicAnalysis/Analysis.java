package verification.platu.logicAnalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Stack;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;
import verification.platu.MDD.MDT;
import verification.platu.MDD.Mdd;
import verification.platu.MDD.mddNode;
import verification.platu.common.IndexObjMap;
import verification.platu.lpn.LPNTranRelation;
import verification.platu.lpn.LpnTranList;
import verification.platu.main.Options;
import verification.platu.partialOrders.DependentSet;
import verification.platu.partialOrders.DependentSetComparator;
import verification.platu.partialOrders.LpnTransitionPair;
import verification.platu.partialOrders.StaticSets;
import verification.platu.project.PrjState;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

public class Analysis {

	private LinkedList<Transition> traceCex;
	protected Mdd mddMgr = null;
	HashMap<LpnTransitionPair, HashSet<LpnTransitionPair>> necessaryMap = new HashMap<LpnTransitionPair, HashSet<LpnTransitionPair>>(); 
		
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
	 * This constructor performs dfs.
	 * @param lpnList
	 */
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
	 * @param start 
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
		
		PrjState stateStackTop = initPrjState;
		if (Options.getDebugMode()) {
			System.out.println("%%%%%%% stateStackTop %%%%%%%%");
			printStateArray(stateStackTop.toStateArray());
		}
		stateStack.add(stateStackTop);
		
		constructDstLpnList(sgList);
		if (Options.getDebugMode())
			printDstLpnList(sgList);
		
		boolean init = true;
		LpnTranList initEnabled = sgList[0].getEnabled(initStateArray[0], init);
		lpnTranStack.push(initEnabled.clone());
		curIndexStack.push(0);
		init = false;
		if (Options.getDebugMode()) {
			System.out.println("call getEnabled on initStateArray at 0: ");
			System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
			printTransitionSet(initEnabled, "");
		}

		main_while_loop: while (failure == false && stateStack.size() != 0) {
			if (Options.getDebugMode())
				System.out.println("$$$$$$$$$$$ loop begins $$$$$$$$$$");
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
			if (Options.getDebugMode()) {
				System.out.println("------- curStateArray ----------");
				printStateArray(curStateArray);
				System.out.println("+++++++ curEnabled trans ++++++++");
				printTransLinkedList(curEnabled);
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
					System.out.println("####### lpnTranStack #########");
					printLpnTranStack(lpnTranStack);
				}
				curIndexStack.pop();
				curIndex++;
				while (curIndex < numLpns) {
//					System.out.println("call getEnabled on curStateArray at 1: ");
					curEnabled = (sgList[curIndex].getEnabled(curStateArray[curIndex], init)).clone();
					if (curEnabled.size() > 0) {
						if (Options.getDebugMode()) {
							System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
							printTransLinkedList(curEnabled);
						}				
						lpnTranStack.push(curEnabled);
						curIndexStack.push(curIndex);
						if (Options.getDebugMode())
							printIntegerStack("curIndexStack after push 1", curIndexStack);
						break;
					} 					
					curIndex++;
				}
			}
			if (curIndex == numLpns) {
				prjStateSet.add(stateStackTop);
				if (Options.getDebugMode()) {
					System.out.println("%%%%%%% Remove stateStackTop from stateStack %%%%%%%%");
					printStateArray(stateStackTop.toStateArray());
				}
				stateStack.remove(stateStackTop);
				stateStackTop = stateStackTop.getFather();
				continue;
			}

			Transition firedTran = curEnabled.removeLast();	
			if (Options.getDebugMode()) {
				System.out.println("###################");			
				System.out.println("Fired transition: " + firedTran.getLpn().getLabel() + "(" + firedTran.getName() + ")");
				System.out.println("###################");
			}
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
			Boolean	existingState = prjStateSet.contains(nextPrjState) || stateStack.contains(nextPrjState);		
			if (existingState == false) {
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				if (Options.getOutputSgFlag()) {
					if (Options.getDebugMode()) {
						System.out.println("******* curStateArray *******");
						printStateArray(curStateArray);
						System.out.println("******* nextStateArray *******");
						printStateArray(nextStateArray);			
						System.out.println("stateStackTop: ");
						printStateArray(stateStackTop.toStateArray());
						System.out.println("firedTran = " + firedTran.getName());
						System.out.println("***nextStateMap for stateStackTop before firedTran being added: ");
						printNextStateMap(stateStackTop.getNextStateMap());
					}
					stateStackTop.setTranOut(firedTran, nextPrjState);
					if (Options.getDebugMode()) {
						System.out.println("***nextStateMap for stateStackTop after firedTran being added: ");
						printNextStateMap(stateStackTop.getNextStateMap());
					}
					for (PrjState prjState : prjStateSet) {
						if (prjState.equals(stateStackTop)) {
							prjState.setTranOut(firedTran, nextPrjState);
							if (Options.getDebugMode()) {
								System.out.println("***nextStateMap for prjState: ");
								printNextStateMap(prjState.getNextStateMap());
							}
						}
					}
//					System.out.println("-----------------------");
				}
				stateStackTop = nextPrjState;
				if (Options.getDebugMode()) {
					System.out.println("%%%%%%% Add global state to stateStack %%%%%%%%");
					printStateArray(stateStackTop.toStateArray());
				}
				stateStack.add(stateStackTop);
				if (Options.getDebugMode()) {
					System.out.println("+++++++ Push trans onto lpnTranStack ++++++++");
					printTransitionSet((LpnTranList) nextEnabledArray[0], "");
				}				
				lpnTranStack.push((LpnTranList) nextEnabledArray[0].clone());
				if (Options.getDebugMode()) {
					System.out.println("******** lpnTranStack ***********");
					printLpnTranStack(lpnTranStack);
				}
				curIndexStack.push(0);
				totalStates++;
			}
			else {
				if (Options.getOutputSgFlag()) {
					if (Options.getDebugMode()) {
						System.out.println("******* curStateArray *******");
						printStateArray(curStateArray);
						System.out.println("******* nextStateArray *******");
						printStateArray(nextStateArray);
					}		
					for (PrjState prjState : prjStateSet) {
						if (prjState.equals(nextPrjState)) {
							nextPrjState.setNextStateMap((HashMap<Transition, PrjState>) prjState.getNextStateMap().clone()); 
						}							
					}
					if (Options.getDebugMode()) {
						System.out.println("stateStackTop: ");
						printStateArray(stateStackTop.toStateArray());
						System.out.println("firedTran = " + firedTran.getName());
						System.out.println("nextStateMap for stateStackTop before firedTran being added: ");
						printNextStateMap(stateStackTop.getNextStateMap());
					}
					stateStackTop.setTranOut(firedTran, nextPrjState);
					if (Options.getDebugMode()) {
						System.out.println("***nextStateMap for stateStackTop after firedTran being added: ");
						printNextStateMap(stateStackTop.getNextStateMap());
					}
					for (PrjState prjState : prjStateSet) {
						if (prjState == stateStackTop) { 
							prjState.setNextStateMap((HashMap<Transition, PrjState>) stateStackTop.getNextStateMap().clone());
							if (Options.getDebugMode()) {
								System.out.println("***nextStateMap for prjState: ");
								printNextStateMap(prjState.getNextStateMap());
							}						
						}
					}
//					System.out.println("-----------------------");
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
			outputLogFile(false, tranFiringCnt, totalStateCnt, peakTotalMem / 1000000, peakUsedMem / 1000000);
		if (Options.getOutputSgFlag())
			outputGlobalStateGraph(sgList, prjStateSet, true);
		return sgList;
	}

	private void outputGlobalStateGraph(StateGraph[] sgList, HashSet<PrjState> prjStateSet, boolean fullSG) {
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
				curVarNames = curVarNames.substring(0, curVarNames.lastIndexOf(","));
				curVarValues = curVarValues.substring(0, curVarValues.lastIndexOf(","));
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
					nextVarNames = nextVarNames.substring(0, nextVarNames.lastIndexOf(","));
					nextVarValues = nextVarValues.substring(0, nextVarValues.lastIndexOf(","));
					nextMarkings = nextMarkings.substring(nextMarkings.indexOf(",")+1, nextMarkings.length());
					nextEnabledTrans = nextEnabledTrans.substring(nextEnabledTrans.indexOf(",")+1, nextEnabledTrans.length());
					nextGlobalStateIndex = nextGlobalStateIndex.substring(nextGlobalStateIndex.indexOf("_")+1, nextGlobalStateIndex.length());
					out.write("Inits[shape=plaintext, label=\"<" + nextVarNames + ">\"]\n");
					out.write(nextGlobalStateIndex + "[shape=\"ellipse\",label=\"" + nextGlobalStateIndex + "\\n<"+nextVarValues+">" + "\\n<"+nextEnabledTrans+">" + "\\n<"+nextMarkings+">" + "\"]\n");
					
					String outTranName = outTran.getName();
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
					arrayStr = arrayStr + curState.getLpn().getAllTransitions()[i].getName() + ",";
				}
			}
			if (arrayStr != "")
				arrayStr = arrayStr.substring(0, arrayStr.lastIndexOf(","));
		}
		return arrayStr;
	}

	private void printStateArray(State[] stateArray) {
		for (int i=0; i<stateArray.length; i++) {
			System.out.print("S" + stateArray[i].getIndex() + "(" + stateArray[i].getLpn().getLabel() +") -> ");
			System.out.print("markings: " + intArrayToString("markings", stateArray[i]) + " / ");
			System.out.print("enabled trans: " + boolArrayToString("enabled trans", stateArray[i]) + " / ");
			System.out.print("var values: " + intArrayToString("vars", stateArray[i]) + " / ");
			System.out.print("\n");
		}
		
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
	@SuppressWarnings("unchecked")
	public StateGraph[] search_dfsPOR(final StateGraph[] sgList, final State[] initStateArray) {
		System.out.println("---> calling function search_dfsPOR");
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
		if (Options.getDebugMode()) {
			System.out.println("%%%%%%% stateStackTop %%%%%%%%");
			printStateArray(stateStackTop.toStateArray());
		}
		stateStack.add(stateStackTop);

		constructDstLpnList(sgList);
		if (Options.getDebugMode())
			printDstLpnList(sgList);	
		
		// Find static pieces for POR. 
		HashMap<Integer, Transition[]> allTransitions = new HashMap<Integer, Transition[]>(lpnList.length); 
		HashMap<LpnTransitionPair, StaticSets> staticSetsMap = new HashMap<LpnTransitionPair, StaticSets>();
		for (int i=0; i<lpnList.length; i++)
			allTransitions.put(i, lpnList[i].getAllTransitions());
		HashMap<LpnTransitionPair, Integer> tranFiringFreq = new HashMap<LpnTransitionPair, Integer>(allTransitions.keySet().size());
		for (int lpnIndex=0; lpnIndex<lpnList.length; lpnIndex++) {
			if (Options.getDebugMode())
				System.out.println("LPN = " + lpnList[lpnIndex].getLabel());
			for (Transition curTran: allTransitions.get(lpnIndex)) {
				StaticSets curStatic = new StaticSets(curTran, allTransitions);
				curStatic.buildDisableSet(lpnIndex);
				curStatic.buildEnableSet();
				curStatic.buildModifyAssignSet();
				LpnTransitionPair lpnTranPair = new LpnTransitionPair(lpnIndex,curTran.getIndex());
				staticSetsMap.put(lpnTranPair, curStatic);
				tranFiringFreq.put(lpnTranPair, 0);
			}			
		}
		if (Options.getDebugMode())
			printStaticSetMap(lpnList, staticSetsMap);
		boolean init = true;
		LpnTranList initAmpleTrans = new LpnTranList();
		if (Options.getDebugMode())
			System.out.println("call getAmple on curStateArray at 0: ");
		initAmpleTrans = getAmple(initStateArray, null, staticSetsMap, init, tranFiringFreq, sgList, stateStack, stateStackTop);
		lpnTranStack.push(initAmpleTrans);
		init = false;
		if (Options.getDebugMode()) {
			System.out.println("+++++++ Push trans onto lpnTranStack @ 1++++++++");
			printTransitionSet(initAmpleTrans, "");
		}		
		HashSet<LpnTransitionPair> initAmple = new HashSet<LpnTransitionPair>();
		for (Transition t: initAmpleTrans) {
			initAmple.add(new LpnTransitionPair(t.getLpn().getLpnIndex(), t.getIndex()));
		}
		updateLocalAmpleTbl(initAmple, sgList, initStateArray);
		
		main_while_loop: while (failure == false && stateStack.size() != 0) {
			if (Options.getDebugMode()) 
				System.out.println("$$$$$$$$$$$ loop begins $$$$$$$$$$");
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
			LinkedList<Transition> curAmpleTrans = lpnTranStack.peek();
			if (curAmpleTrans.size() == 0) {
				lpnTranStack.pop();
				prjStateSet.add(stateStackTop);
				if (Options.getDebugMode()) {
					System.out.println("+++++++ Pop trans off lpnTranStack ++++++++");
					System.out.println("####### lpnTranStack #########");
					printLpnTranStack(lpnTranStack);
//					System.out.println("####### stateStack #########");
//					printStateStack(stateStack);
					System.out.println("####### prjStateSet #########");
					printPrjStateSet(prjStateSet);
					System.out.println("%%%%%%% Remove stateStackTop from stateStack %%%%%%%%");
				}
				stateStack.remove(stateStackTop);
				stateStackTop = stateStackTop.getFather();
				continue;
			}

			Transition firedTran = curAmpleTrans.removeLast();	
			if (Options.getDebugMode()) {
				System.out.println("###################");
				System.out.println("Fired Transition: " + firedTran.getLpn().getLabel() + "(" + firedTran.getName() + ")");
				System.out.println("###################");
			}
			
			LpnTransitionPair firedLpnTranPair = new LpnTransitionPair(firedTran.getLpn().getLpnIndex(), firedTran.getIndex());
			Integer freq = tranFiringFreq.get(firedLpnTranPair) + 1;
			tranFiringFreq.put(firedLpnTranPair, freq);
			if (Options.getDebugMode()) {
				System.out.println("~~~~~~tranFiringFreq~~~~~~~");
				printHashMap(tranFiringFreq, sgList);
			}
			
			State[] nextStateArray = sgList[firedLpnTranPair.getLpnIndex()].fire(sgList, curStateArray, firedTran);
			tranFiringCnt++;

			// Check if the firedTran causes disabling error or deadlock.
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
//			System.out.println("******* curStateArray *******");
//			printStateArray(curStateArray);
//			System.out.println("******* nextStateArray *******");
//			printStateArray(nextStateArray);			
			LpnTranList nextAmpleTrans = new LpnTranList();
			nextAmpleTrans = getAmple(curStateArray, nextStateArray, staticSetsMap, init, tranFiringFreq, sgList, prjStateSet, stateStackTop);
			HashSet<LpnTransitionPair> nextAmple = getLpnTransitionPair(nextAmpleTrans);
			// check for possible deadlock
			if (nextAmpleTrans.size() == 0) {
				System.out.println("*** Verification failed: deadlock.");
				failure = true;
				break main_while_loop;
			}
			
			PrjState nextPrjState = new PrjState(nextStateArray);
			Boolean existingState = prjStateSet.contains(nextPrjState) || stateStack.contains(nextPrjState);
						
			if (existingState == false) {
				if (Options.getDebugMode()) 
					System.out.println("%%%%%%% existingSate == false %%%%%%%%");				
				if (Options.getOutputSgFlag()) {
					if (Options.getDebugMode()) {
						System.out.println("******* curStateArray *******");
						printStateArray(curStateArray);
						System.out.println("******* nextStateArray *******");
						printStateArray(nextStateArray);			
						System.out.println("stateStackTop: ");
						printStateArray(stateStackTop.toStateArray());
						System.out.println("firedTran = " + firedTran.getName());
						System.out.println("***nextStateMap for stateStackTop before firedTran being added: ");
						printNextStateMap(stateStackTop.getNextStateMap());
					}
					stateStackTop.setTranOut(firedTran, nextPrjState);
					if (Options.getDebugMode()) {
						System.out.println("***nextStateMap for stateStackTop after firedTran being added: ");
						printNextStateMap(stateStackTop.getNextStateMap());
					}
					for (PrjState prjState : prjStateSet) {
						if (prjState.equals(stateStackTop)) {
							prjState.setTranOut(firedTran, nextPrjState);
							if (Options.getDebugMode()) {
								System.out.println("***nextStateMap for prjState: ");
								printNextStateMap(prjState.getNextStateMap());
							}
						}
					}
//					System.out.println("-----------------------");
				}
				updateLocalAmpleTbl(nextAmple, sgList, nextStateArray);
				stateStackTop.setChild(nextPrjState);
				nextPrjState.setFather(stateStackTop);
				stateStackTop = nextPrjState;
				stateStack.add(stateStackTop);
				if (Options.getDebugMode()) {
					System.out.println("%%%%%%% Add global state to stateStack %%%%%%%%");
					printStateArray(stateStackTop.toStateArray());
					System.out.println("+++++++ Push trans onto lpnTranStack @ 2++++++++");
					printTransitionSet((LpnTranList) nextAmpleTrans, "");
				}
				lpnTranStack.push((LpnTranList) nextAmpleTrans.clone());
				totalStates++;
			}
			else {
				if (Options.getOutputSgFlag()) {
					if (Options.getDebugMode()) {
						System.out.println("******* curStateArray *******");
						printStateArray(curStateArray);
						System.out.println("******* nextStateArray *******");
						printStateArray(nextStateArray);
					}
					for (PrjState prjState : prjStateSet) {
						if (prjState.equals(nextPrjState)) {
							nextPrjState.setNextStateMap((HashMap<Transition, PrjState>) prjState.getNextStateMap().clone()); 
						}							
					}
					if (Options.getDebugMode()) {
						System.out.println("stateStackTop: ");
						printStateArray(stateStackTop.toStateArray());
						System.out.println("firedTran = " + firedTran.getName());
						System.out.println("nextStateMap for stateStackTop before firedTran being added: ");
						printNextStateMap(stateStackTop.getNextStateMap());
					}
					stateStackTop.setTranOut(firedTran, nextPrjState);
					if (Options.getDebugMode()) {
						System.out.println("***nextStateMap for stateStackTop after firedTran being added: ");
						printNextStateMap(stateStackTop.getNextStateMap());
					}
					for (PrjState prjState : prjStateSet) {
						if (prjState == stateStackTop) { 
							prjState.setNextStateMap((HashMap<Transition, PrjState>) stateStackTop.getNextStateMap().clone());
							if (Options.getDebugMode()) {
								System.out.println("***nextStateMap for prjState: ");
								printNextStateMap(prjState.getNextStateMap());
							}
						}
					}
//					System.out.println("-----------------------");
				}
				if (!Options.getCycleClosingMthd().toLowerCase().equals("no_cycleclosing")) {
					// Cycle closing check
					if (prjStateSet.contains(nextPrjState) && stateStack.contains(nextPrjState)) {
						if (Options.getDebugMode()) 
							System.out.println("%%%%%%% Cycle Closing %%%%%%%%");
						HashSet<LpnTransitionPair> nextAmpleSet = new HashSet<LpnTransitionPair>();
						HashSet<LpnTransitionPair> curAmpleSet = new HashSet<LpnTransitionPair>();
						for (Transition t : nextAmpleTrans) {
							nextAmpleSet.add(new LpnTransitionPair(t.getLpn().getLpnIndex(), t.getIndex()));
						}
						for (Transition t: curAmpleTrans) {
							curAmpleSet.add(new LpnTransitionPair(t.getLpn().getLpnIndex(), t.getIndex()));
						}
						curAmpleSet.add(new LpnTransitionPair(firedTran.getLpn().getLpnIndex(), firedTran.getIndex()));
						HashSet<LpnTransitionPair> newNextAmple = computeCycleClosingTrans(curStateArray, nextStateArray, staticSetsMap, 
																		tranFiringFreq, sgList, prjStateSet, nextPrjState, nextAmpleSet, curAmpleSet, stateStack);

						if (newNextAmple != null && !newNextAmple.isEmpty()) {
							LpnTranList newNextAmpleTrans = getLpnTranList(newNextAmple, sgList);
							stateStackTop.setChild(nextPrjState);
							nextPrjState.setFather(stateStackTop);	
							if (Options.getDebugMode()) {
								System.out.println("nextPrjState: ");
								printStateArray(nextPrjState.toStateArray());
								System.out.println("nextStateMap for nextPrjState: ");
								printNextStateMap(nextPrjState.getNextStateMap());		
							}
							stateStackTop = nextPrjState;
							stateStack.add(stateStackTop);
							if (Options.getDebugMode()) {
								System.out.println("%%%%%%% Add state to stateStack %%%%%%%%");
								printStateArray(stateStackTop.toStateArray());
								System.out.println("stateStackTop: ");
								printStateArray(stateStackTop.toStateArray());
								System.out.println("nextStateMap for stateStackTop: ");
								printNextStateMap(nextPrjState.getNextStateMap());
							}
							
							lpnTranStack.push(newNextAmpleTrans.clone());
							if (Options.getDebugMode()) {
								System.out.println("+++++++ Push these trans onto lpnTranStack @ Cycle Closing ++++++++");
								printTransitionSet((LpnTranList) newNextAmpleTrans, "");
								System.out.println("******* lpnTranStack ***************");
								printLpnTranStack(lpnTranStack);
								
							}
						}
					}
				}
				updateLocalAmpleTbl(nextAmple, sgList, nextStateArray);
			}
		}		
		double totalStateCnt = prjStateSet.size();		
		System.out.println("---> final numbers: # LPN transition firings: "	+ tranFiringCnt 
				+ ", # of prjStates found: " + totalStateCnt 
				+ ", max_stack_depth: " + max_stack_depth 
				+ ", peak total memory: " + peakTotalMem / 1000000 + " MB"
				+ ", peak used memory: " + peakUsedMem / 1000000 + " MB");
		
		if (Options.getOutputLogFlag()) 
			outputLogFile(true, tranFiringCnt, totalStateCnt, peakTotalMem / 1000000, peakUsedMem / 1000000);
		if (Options.getOutputSgFlag()) {
			outputGlobalStateGraph(sgList, prjStateSet, false);
		}
		return sgList;
	}

	private void outputLogFile(boolean isPOR, int tranFiringCnt, double totalStateCnt,
			double peakTotalMem, double peakUsedMem) {
		try {
			String fileName = null;
			if (isPOR)
				fileName = Options.getPrjSgPath() + Options.getLogName() + "_" + Options.getPOR() + "_" 
						+ Options.getCycleClosingMthd() + "_" + Options.getCycleClosingAmpleMethd() + ".log";
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

	private void printLpnTranStack(Stack<LinkedList<Transition>> lpnTranStack) {
		for (int i=0; i<lpnTranStack.size(); i++) {
			LinkedList<Transition> tranList = lpnTranStack.get(i);
			for (int j=0; j<tranList.size(); j++) {
				System.out.println(tranList.get(j).getName());
			}
			System.out.println("----------------");
		}
		
	}

	private void printNextStateMap(HashMap<Transition, PrjState> nextStateMap) {
		for (Transition t: nextStateMap.keySet()) {
			System.out.print(t.getName() + " -> ");
			State[] stateArray = nextStateMap.get(t).getStateArray();
			for (int i=0; i<stateArray.length; i++) {
				System.out.print("S" + stateArray[i].getIndex() + ", ");
			}
			System.out.print("\n");
		}
	}

	private HashSet<LpnTransitionPair> getLpnTransitionPair(LpnTranList ampleTrans) {
		HashSet<LpnTransitionPair> ample = new HashSet<LpnTransitionPair>();
		for (Transition tran : ampleTrans) {
			ample.add(new LpnTransitionPair(tran.getLpn().getLpnIndex(), tran.getIndex()));
		}
		return ample;
	}

	private LpnTranList getLpnTranList(HashSet<LpnTransitionPair> newNextAmple,
			StateGraph[] sgList) {
		LpnTranList newNextAmpleTrans = new LpnTranList();
		for (LpnTransitionPair lpnTran : newNextAmple) {
			Transition tran = sgList[lpnTran.getLpnIndex()].getLpn().getTransition(lpnTran.getTranIndex());
			newNextAmpleTrans.add(tran);
		}
		return newNextAmpleTrans;
	}

	private HashSet<LpnTransitionPair> computeCycleClosingTrans(State[] curStateArray,
			State[] nextStateArray,
			HashMap<LpnTransitionPair, StaticSets> staticSetsMap,
			HashMap<LpnTransitionPair, Integer> tranFiringFreq, StateGraph[] sgList, HashSet<PrjState> prjStateSet,
			PrjState nextPrjState, HashSet<LpnTransitionPair> nextAmple, 
			HashSet<LpnTransitionPair> curAmple, HashSet<PrjState> stateStack) {		
    	for (State s : nextStateArray)
    		if (s == null) 
    			throw new NullPointerException();
    	String cycleClosingMthd = Options.getCycleClosingMthd();
    	HashSet<LpnTransitionPair> newNextAmple = new HashSet<LpnTransitionPair>();
    	HashSet<LpnTransitionPair> nextEnabled = new HashSet<LpnTransitionPair>();
    	HashSet<LpnTransitionPair> curEnabled = new HashSet<LpnTransitionPair>();
    	LhpnFile[] lpnList = new LhpnFile[sgList.length];
		for (int i=0; i<sgList.length; i++) 
			lpnList[i] = sgList[i].getLpn();
    	for (int lpnIndex=0; lpnIndex<nextStateArray.length; lpnIndex++) {
			LhpnFile curLpn = sgList[lpnIndex].getLpn();
			for (int i=0; i < curLpn.getAllTransitions().length; i++) {
				Transition tran = curLpn.getAllTransitions()[i];
				if (nextStateArray[lpnIndex].getTranVector()[i]) 
					nextEnabled.add(new LpnTransitionPair(curLpn.getLpnIndex(), tran.getIndex()));
				if (curStateArray[lpnIndex].getTranVector()[i])
					curEnabled.add(new LpnTransitionPair(curLpn.getLpnIndex(), tran.getIndex()));
            }
    	}
    		// Cycle closing on global state graph
    		if (Options.getDebugMode()) 
    			System.out.println("~~~~~~~ existing global state ~~~~~~~~");
    		if (cycleClosingMthd.equals("strong")) {
    			newNextAmple.addAll(getIntSetSubstraction(curEnabled, nextAmple));
    			updateLocalAmpleTbl(newNextAmple, sgList, nextStateArray);
    		}
    		else if (cycleClosingMthd.equals("behavioral")) {
    			if (Options.getDebugMode()) 
    				System.out.println("****** behavioral: cycle closing check ********");
    			HashSet<LpnTransitionPair> curReduced = getIntSetSubstraction(curEnabled, curAmple);
    			HashSet<LpnTransitionPair> oldNextAmple = new HashSet<LpnTransitionPair>();
    			DependentSetComparator depComp = new DependentSetComparator(tranFiringFreq); 
    			PriorityQueue<DependentSet> dependentSetQueue = new PriorityQueue<DependentSet>(nextEnabled.size(), depComp);
    			// TODO: Is oldNextAmple correctly obtained below?
    			if (Options.getDebugMode()) {
    				System.out.println("******* nextStateArray *******");
    				printStateArray(nextStateArray);
    			}
    			for (int lpnIndex=0; lpnIndex < sgList.length; lpnIndex++) {
    				if (sgList[lpnIndex].getEnabledSetTbl().get(nextStateArray[lpnIndex]) != null) {
    					LpnTranList oldLocalNextAmpleTrans = sgList[lpnIndex].getEnabledSetTbl().get(nextStateArray[lpnIndex]);
    					if (Options.getDebugMode()) 
    						printTransitionSet(oldLocalNextAmpleTrans, "oldLocalNextAmpleTrans");
        				for (Transition oldLocalTran : oldLocalNextAmpleTrans)
        					oldNextAmple.add(new LpnTransitionPair(lpnIndex, oldLocalTran.getIndex()));
    				}
    					
    			}
    			HashSet<LpnTransitionPair> ignored = getIntSetSubstraction(curReduced, oldNextAmple);
    			boolean isCycleClosingAmpleComputation = true;
    			for (LpnTransitionPair seed : ignored) {
    				HashSet<LpnTransitionPair> dependent = new HashSet<LpnTransitionPair>();
    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList, isCycleClosingAmpleComputation);
    				if (Options.getDebugMode()) {
    					printIntegerSet(dependent, sgList, "dependent set for ignored transition " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
						+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
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
    				DependentSet dependentSet = new DependentSet(dependent, seed, 
	  						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
	  				dependentSetQueue.add(dependentSet);
    			}
    			necessaryMap.clear();
    			if (!dependentSetQueue.isEmpty()) {
//    				System.out.println("depdentSetQueue is NOT empty.");
//    				newNextAmple = dependentSetQueue.poll().getDependent();
    				// **************************
        			// TODO: Will newNextAmpleTmp - oldNextAmple be safe?
        			HashSet<LpnTransitionPair> newNextAmpleTmp = dependentSetQueue.poll().getDependent();        			
        			newNextAmple = getIntSetSubstraction(newNextAmpleTmp, oldNextAmple);
        			// **************************
        			updateLocalAmpleTbl(newNextAmple, sgList, nextStateArray);
    			}
    			if (Options.getDebugMode()) {
    				printIntegerSet(newNextAmple, sgList, "newNextAmple");
    				System.out.println("******** behavioral: end of cycle closing check *****");
    			}
    		}
    		else if (cycleClosingMthd.equals("state_search")) {
    			// TODO: complete cycle closing check for state search.
    			
    		}
  		return newNextAmple;
	}

	private void updateLocalAmpleTbl(HashSet<LpnTransitionPair> newNextAmple,
			StateGraph[] sgList, State[] nextStateArray) {
		// Ample set at each state is stored in the enabledSetTbl in each state graph.
		for (LpnTransitionPair lpnTran : newNextAmple) {
			State nextState = nextStateArray[lpnTran.getLpnIndex()];
			Transition tran = sgList[lpnTran.getLpnIndex()].getLpn().getTransition(lpnTran.getTranIndex());
			if (sgList[lpnTran.getLpnIndex()].getEnabledSetTbl().get(nextState) != null) {
				if (!sgList[lpnTran.getLpnIndex()].getEnabledSetTbl().get(nextState).contains(tran))
					sgList[lpnTran.getLpnIndex()].getEnabledSetTbl().get(nextState).add(tran);
			}
			else {
				LpnTranList newLpnTranList = new LpnTranList();
				newLpnTranList.add(tran);
				sgList[lpnTran.getLpnIndex()].getEnabledSetTbl().put(nextStateArray[lpnTran.getLpnIndex()], newLpnTranList);
				if (Options.getDebugMode()) {
					System.out.println("@ updateLocalAmpleTbl: ");
					System.out.println("Add S" + nextStateArray[lpnTran.getLpnIndex()].getIndex() + " to the enabledSetTbl of " 
							+ sgList[lpnTran.getLpnIndex()].getLpn().getLabel() + ".");
				}			
			}
		}
		if (Options.getDebugMode())
			printEnabledSetTbl(sgList);
	}

	private void printPrjStateSet(HashSet<PrjState> prjStateSet) {
		for (PrjState curGlobal : prjStateSet) {
			State[] curStateArray = curGlobal.toStateArray();
			printStateArray(curStateArray);
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
	
	private void printHashMap(HashMap<LpnTransitionPair, Integer> tranFiringFreq, StateGraph[] sgList) {
		for (LpnTransitionPair curIndex : tranFiringFreq.keySet()) {
			LhpnFile curLpn = sgList[curIndex.getLpnIndex()].getLpn();
			Transition curTran = curLpn.getTransition(curIndex.getTranIndex());
			System.out.println(curLpn.getLabel() + "(" + curTran.getName() + ")" + " -> " + tranFiringFreq.get(curIndex));
		}
		
	}

	private void printStaticSetMap(
			LhpnFile[] lpnList, HashMap<LpnTransitionPair,StaticSets> staticSetsMap) {
		System.out.println("^^^^^^^^^^^^ staticSetsMap ^^^^^^^^^^^^");
		for (LpnTransitionPair lpnTranPair : staticSetsMap.keySet()) {
			StaticSets statSets = staticSetsMap.get(lpnTranPair);
			printLpnTranPair(lpnList, statSets.getTran(), statSets.getDisabled(), "disableSet");
			printLpnTranPair(lpnList, statSets.getTran(), statSets.getEnable(), "enableSet");
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
	private void printLpnTranPair(LhpnFile[] lpnList, Transition curTran,
			HashSet<LpnTransitionPair> curDisable, String setName) {
		System.out.print(setName + " for " + curTran.getName() + "(" + curTran.getIndex() + ")" + " is: ");
		if (curDisable.isEmpty()) {
			System.out.println("empty");
		}
		else {
			for (LpnTransitionPair lpnTranPair: curDisable) {
				System.out.print("(" + lpnList[lpnTranPair.getLpnIndex()].getLabel() 
						+ ", " + lpnList[lpnTranPair.getLpnIndex()].getAllTransitions()[lpnTranPair.getTranIndex()].getName() + ")," + "\t");
			}
			System.out.print("\n");
		}			
	}
	
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
     * Return the set of all LPN transitions that are enabled in 'state'. The "init" flag indicates whether a transition
     * needs to be evaluated. This method does not do cycle closing check. 
     * The enabledSetTbl (for each StateGraph obj) stores the AMPLE set for each state of each LPN.
     * @param stateArray
     * @param stateStack 
     * @param enable 
     * @param disableByStealingToken 
     * @param disable 
     * @param init 
     * @param tranFiringFreq 
     * @param sgList 
     * @return
     */
    public LpnTranList getAmple(State[] curStateArray, State[] nextStateArray, HashMap<LpnTransitionPair,StaticSets> staticSetsMap, 
    			boolean init, HashMap<LpnTransitionPair, Integer> tranFiringFreq, StateGraph[] sgList, 
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
    	HashSet<LpnTransitionPair> curEnabled = new HashSet<LpnTransitionPair>();
   		for (int lpnIndex=0; lpnIndex<stateArray.length; lpnIndex++) {
   			State state = stateArray[lpnIndex];
   			if (init) {
             	LhpnFile curLpn = sgList[lpnIndex].getLpn();
             	for (int i=0; i < curLpn.getAllTransitions().length; i++) {
                   	Transition tran = curLpn.getAllTransitions()[i];
                   	if (sgList[lpnIndex].isEnabled(tran,state)){
                   		curEnabled.add(new LpnTransitionPair(curLpn.getLpnIndex(), i));
                    }
                }
   			}
   			else {
   				LhpnFile curLpn = sgList[lpnIndex].getLpn();
   				for (int i=0; i < curLpn.getAllTransitions().length; i++) {
   					if (stateArray[lpnIndex].getTranVector()[i]) {
   						curEnabled.add(new LpnTransitionPair(curLpn.getLpnIndex(), i));
   					}
               	}
   			}
   		}
   		
        HashSet<LpnTransitionPair> ready = new HashSet<LpnTransitionPair>();
        if (Options.getDebugMode()) {
	        System.out.println("******** partial order reduction ************");
	      	printIntegerSet(curEnabled, sgList, "Enabled set");
	      	System.out.println("******* Begin POR");
        }
    	ready = partialOrderReduction(stateArray, curEnabled, staticSetsMap, tranFiringFreq, sgList);
    	if (Options.getDebugMode()) {
	    	System.out.println("******* End POR");
	    	printIntegerSet(ready, sgList, "Ready set");
	    	System.out.println("********************");
    	}
		// ************* Priority: tran fired less times first *************
    	// TODO: Possibly use a better sorting algorithm here.
    	LinkedList<LpnTransitionPair> readyList = new LinkedList<LpnTransitionPair>();
    	for (LpnTransitionPair inReady : ready) {
    		readyList.add(inReady);
    	}
    	mergeSort(readyList, tranFiringFreq);
   		for (LpnTransitionPair inReady : readyList) {
   			LhpnFile lpn = sgList[inReady.getLpnIndex()].getLpn();
			Transition tran = lpn.getTransition(inReady.getTranIndex());
   			ample.addFirst(tran);
   		}
   		// ************* Priority: local tran first ***********************  
//    	for (LpnTransitionPair inReady : ready) {
//   			LhpnFile lpn = sgList[inReady.getLpnIndex()].getLpn();
//   			Transition tran = lpn.getTransition(inReady.getTranIndex());
//   			if (tran.local()==true) 
//   				ample.addLast(tran);
//   			else
//   				ample.addFirst(tran);
//   		}
        return ample;
    }

	private LinkedList<LpnTransitionPair> mergeSort(LinkedList<LpnTransitionPair> array, HashMap<LpnTransitionPair, Integer> tranFiringFreq) {
		if (array.size() == 1)
			return array;
		int middle = array.size() / 2;
		LinkedList<LpnTransitionPair> left = new LinkedList<LpnTransitionPair>();
		LinkedList<LpnTransitionPair> right = new LinkedList<LpnTransitionPair>();
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

	private LinkedList<LpnTransitionPair> merge(LinkedList<LpnTransitionPair> left,
			LinkedList<LpnTransitionPair> right, HashMap<LpnTransitionPair, Integer> tranFiringFreq) {
		LinkedList<LpnTransitionPair> result = new LinkedList<LpnTransitionPair>(); 
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
	public LpnTranList getAmpleRefinedCycleRule(State[] curStateArray, State[] nextStateArray, HashMap<LpnTransitionPair,StaticSets> staticSetsMap, 
			boolean init, HashMap<LpnTransitionPair,Integer> tranFiringFreq, StateGraph[] sgList, 
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
//    			HashSet<LpnTransitionPair> curEnabledIndicies = new HashSet<LpnTransitionPair>();
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
//    				HashSet<LpnTransitionPair> dependent = new HashSet<LpnTransitionPair>();
//    				//Transition enabledTransition = this.lpn.getAllTransitions()[tranToAdd];
//    				dependent = getDependentSet(curState,tranToAdd,dependent,curEnabledIndicies,staticMap);  				
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
//		necessaryMap.clear();
//    	String cycleClosingMthd = Options.getCycleClosingMthd();
//    	AmpleSet nextAmple = new AmpleSet();
//    	HashSet<LpnTransitionPair> nextEnabled = new HashSet<LpnTransitionPair>();
////    	boolean allEnabledAreSticky = false;
//    	for (int lpnIndex=0; lpnIndex<nextStateArray.length; lpnIndex++) {
//   			State nextState = nextStateArray[lpnIndex];
//   			if (init) {
//             	LhpnFile curLpn = sgList[lpnIndex].getLpn();
//              	//allEnabledAreSticky = true;
//             	for (int i=0; i < curLpn.getAllTransitions().length; i++) {
//                   	Transition tran = curLpn.getAllTransitions()[i];
//                   	if (sgList[lpnIndex].isEnabled(tran,nextState)){
//                   		nextEnabled.add(new LpnTransitionPair(curLpn.getLpnIndex(), i));
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
//   						nextEnabled.add(new LpnTransitionPair(curLpn.getLpnIndex(), i));
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
//		HashMap<LpnTransitionPair, LpnTranList> transToAddMap = new HashMap<LpnTransitionPair, LpnTranList>();
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
//       	    			HashSet<LpnTransitionPair> ignored = new HashSet<LpnTransitionPair>();
//       	    			for (int i=0; i<ignoredTrans.size(); i++) {
//       	    				ignored.add(new LpnTransitionPair(lpnIndex, ignoredTrans.get(i).getIndex()));
//       	    			}
//       	    			HashSet<LpnTransitionPair> newNextAmple = (HashSet<LpnTransitionPair>) nextEnabled.clone();
//       	    			if (cycleClosingMthd.toLowerCase().equals("behavioral")) {
//         	    			for (LpnTransitionPair seed : ignored) {
//          	    				HashSet<LpnTransitionPair> dependent = new HashSet<LpnTransitionPair>();
//          	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//          	    				printIntegerSet(dependent, sgList, "dependent set for ignored transition " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//          							+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//          	    				
//          	    				boolean dependentOnlyHasDummyTrans = true;
//          	    				for (LpnTransitionPair dependentTran : dependent) {
//          	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//          	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//          	    				}         	    							
//          	    				if (dependent.size() < newNextAmple.size() && !dependentOnlyHasDummyTrans) 
//          	    					newNextAmple = (HashSet<LpnTransitionPair>) dependent.clone();
////          	    				if (nextAmpleNewIndices.size() == 1)
////          	    					break;
//          	    				DependentSet dependentSet = new DependentSet(newNextAmple, seed, 
//          	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//          	    				dependentSetQueue.add(dependentSet);
//          	    				LpnTranList newLocalNextAmpleTrans = new LpnTranList();
//              	    			for (LpnTransitionPair tran : newNextAmple) {
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
//       	    					HashSet<LpnTransitionPair> trulyIgnored = new HashSet<LpnTransitionPair>();
//       	    					for (Transition tran : trulyIgnoredTrans) {
//       	    						trulyIgnored.add(new LpnTransitionPair(tran.getLpn().getLpnIndex(), tran.getIndex()));
//       	    					}
//  	          	    			for (LpnTransitionPair seed : trulyIgnored) {
//  	          	    				HashSet<LpnTransitionPair> dependent = new HashSet<LpnTransitionPair>();
//  	          	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//  	          	    				printIntegerSet(dependent, sgList, "dependent set for trulyIgnored transition " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//  	          							+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//  	          	    				
//   	          	    				boolean dependentOnlyHasDummyTrans = true;
//  	          	    				for (LpnTransitionPair dependentTran : dependent) {
//  	          	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//   	          	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//   	          	    				}         	    							
//   	          	    				if (dependent.size() < newNextAmple.size() && !dependentOnlyHasDummyTrans) 
//   	          	    					newNextAmple = (HashSet<LpnTransitionPair>) dependent.clone();
//   	          	    				DependentSet dependentSet = new DependentSet(newNextAmple, seed, 
//   	          	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//   	          	    				dependentSetQueue.add(dependentSet);
//   	          	    				LpnTranList newLocalNextAmpleTrans = new LpnTranList();
//   	              	    			for (LpnTransitionPair tran : newNextAmple) {
//   	              	    				if (tran.getLpnIndex() == lpnIndex)
//   	              	    					newLocalNextAmpleTrans.add(sgList[lpnIndex].getLpn().getTransition(tran.getTranIndex()));
//   	              	    			}
//   	              	    			LpnTranList transToAdd = getSetSubtraction(newLocalNextAmpleTrans, oldLocalNextAmpleTrans);
//   	              	    			transToAddMap.put(seed, transToAdd);
//   	          	    			}
//       	    				}
//       	    				else { // All ignored transitions were fired before. It is safe to close the current cycle.
//               	    			HashSet<LpnTransitionPair> oldLocalNextAmple = new HashSet<LpnTransitionPair>();
//               	    			for (Transition tran : oldLocalNextAmpleTrans)
//               	    				oldLocalNextAmple.add(new LpnTransitionPair(tran.getLpn().getLpnIndex(), tran.getIndex()));
//      							for (LpnTransitionPair seed : oldLocalNextAmple) {
//      	          	    			HashSet<LpnTransitionPair> dependent = new HashSet<LpnTransitionPair>();
//      	      	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//      	      	    				printIntegerSet(dependent, sgList, "dependent set for transition in oldNextAmple is " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//      	      							+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//      	      	    				boolean dependentOnlyHasDummyTrans = true;
//      	      	    				for (LpnTransitionPair dependentTran : dependent) {
//      	      	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//      	      	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//      	      	    				}         	    							
//      	      	    				if (dependent.size() < newNextAmple.size() && !dependentOnlyHasDummyTrans) 
//      	      	    					newNextAmple = (HashSet<LpnTransitionPair>) dependent.clone();
//      	          	    			DependentSet dependentSet = new DependentSet(newNextAmple, seed, 
//      	      	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//      	      	    				dependentSetQueue.add(dependentSet);
//       							}
//       	    				}
//       	    			}
//       	    		}
//       	    		else {
//       	    			// oldNextAmpleTrans.containsAll(curReducedTrans) == true (safe to close the current cycle)
//       	    			HashSet<LpnTransitionPair> newNextAmple = (HashSet<LpnTransitionPair>) nextEnabled.clone();
//       	    			HashSet<LpnTransitionPair> oldLocalNextAmple = new HashSet<LpnTransitionPair>();
//      	    			for (Transition tran : oldLocalNextAmpleTrans)
//       	    				oldLocalNextAmple.add(new LpnTransitionPair(tran.getLpn().getLpnIndex(), tran.getIndex()));
//      	    			for (LpnTransitionPair seed : oldLocalNextAmple) {
//         	    			HashSet<LpnTransitionPair> dependent = new HashSet<LpnTransitionPair>();
//      	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//      	    				printIntegerSet(dependent, sgList, "dependent set for transition in oldNextAmple is " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//      							+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//      	    				boolean dependentOnlyHasDummyTrans = true;
//      	    				for (LpnTransitionPair dependentTran : dependent) {
//      	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//      	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//      	    				}         	    							
//      	    				if (dependent.size() < newNextAmple.size() && !dependentOnlyHasDummyTrans) 
//      	    					newNextAmple = (HashSet<LpnTransitionPair>) dependent.clone();
//          	    			DependentSet dependentSet = new DependentSet(newNextAmple, seed, 
//      	    						isDummyTran(sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName()));
//      	    				dependentSetQueue.add(dependentSet);
//						}
//       	    		}
//       			}
//       			else if (cycleClosingMthd.toLowerCase().equals("strong")) {
//       				LpnTranList ignoredTrans = getSetSubtraction(reducedLocalTrans, oldLocalNextAmpleTrans);
//   	    			HashSet<LpnTransitionPair> ignored = new HashSet<LpnTransitionPair>();
//   	    			for (int i=0; i<ignoredTrans.size(); i++) {
//   	    				ignored.add(new LpnTransitionPair(lpnIndex, ignoredTrans.get(i).getIndex()));
//   	    			}
//       				HashSet<LpnTransitionPair> allNewNextAmple = new HashSet<LpnTransitionPair>();
//       				for (LpnTransitionPair seed : ignored) {
//       					HashSet<LpnTransitionPair> newNextAmple = (HashSet<LpnTransitionPair>) nextEnabled.clone();
//       					HashSet<LpnTransitionPair> dependent = new HashSet<LpnTransitionPair>();
//   	    				dependent = computeDependent(nextStateArray,seed,dependent,nextEnabled,staticSetsMap, lpnList);  				
//   	    				printIntegerSet(dependent, sgList, "dependent set for transition in curLocalEnabled is " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//						+ "(" + sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()).getName() + ")");
//   	    				boolean dependentOnlyHasDummyTrans = true;
//   	    				for (LpnTransitionPair dependentTran : dependent) {
//   	    					dependentOnlyHasDummyTrans = dependentOnlyHasDummyTrans 
//   	    								&& isDummyTran(sgList[dependentTran.getLpnIndex()].getLpn().getTransition(dependentTran.getTranIndex()).getName());
//   	    				}         	    							
//   	    				if (dependent.size() < newNextAmple.size() && !dependentOnlyHasDummyTrans) 
//   	    					newNextAmple = (HashSet<LpnTransitionPair>) dependent.clone();
//   	    				allNewNextAmple.addAll(newNextAmple);
//       				}
//       				// The strong cycle condition requires all seeds in ignored to be included in the allNewNextAmple, as well as dependent set for each seed.
//       				// So each seed should have the same new ample set.
//       				for (LpnTransitionPair seed : ignored) {
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
//       			HashSet<LpnTransitionPair> ready = null;
//   				for (LpnTransitionPair seed : nextEnabled) {
//       				System.out.println("@ partialOrderReduction, consider transition " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//  							+ "("+ sgList[seed.getLpnIndex()].getLpn().getTransition(seed.getTranIndex()) + ")");
//   					HashSet<LpnTransitionPair> dependent = new HashSet<LpnTransitionPair>();
//   					Transition enabledTransition = sgList[seed.getLpnIndex()].getLpn().getAllTransitions()[seed.getTranIndex()];
//   					boolean enabledIsDummy = false;
////       					if (enabledTransition.isSticky()) {
////       						dependent = (HashSet<LpnTransitionPair>) nextEnabled.clone();
////       					}
////   					else {
////      						dependent = computeDependent(curStateArray,seed,dependent,nextEnabled,staticMap, lpnList);
////       					}
//   					dependent = computeDependent(curStateArray,seed,dependent,nextEnabled,staticSetsMap,lpnList);
//   					printIntegerSet(dependent, sgList, "dependent set for enabled transition " + sgList[seed.getLpnIndex()].getLpn().getLabel() 
//       							+ "(" + enabledTransition.getName() + ")");
//   					if (isDummyTran(enabledTransition.getName()))
//   						enabledIsDummy = true;
//   					for (LpnTransitionPair inDependent : dependent) {
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
//	public boolean deadLock(StateGraph[] sgList, State[] stateArray, HashMap<LpnTransitionPair,StaticSets> staticSetsMap, 
//							boolean init, HashMap<LpnTransitionPair, Integer> tranFiringFreq, HashSet<PrjState> stateStack, PrjState stateStackTop, int cycleClosingMethdIndex) {
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
	
    private void printEnabledSetTbl(StateGraph[] sgList) {
    	for (int i=0; i<sgList.length; i++) {
    		System.out.println("******* enabledSetTbl for " + sgList[i].getLpn().getLabel() + " **********");
    		for (State s : sgList[i].getEnabledSetTbl().keySet()) {
        		System.out.print("S" + s.getIndex() + " -> ");
        		printTransitionSet(sgList[i].getEnabledSetTbl().get(s), "");
        	}
    	}		
	}
    

	private HashSet<LpnTransitionPair> partialOrderReduction(State[] curStateArray,
			HashSet<LpnTransitionPair> curEnabled, HashMap<LpnTransitionPair, StaticSets> staticMap, 
			HashMap<LpnTransitionPair,Integer> tranFiringFreqMap, StateGraph[] sgList) {
		if (curEnabled.size() == 1) 
			return curEnabled;
		HashSet<LpnTransitionPair> ready = null;
		LhpnFile[] lpnList = new LhpnFile[sgList.length];
		for (int i=0; i<sgList.length; i++) {
			lpnList[i] = sgList[i].getLpn();
		}
		DependentSetComparator depComp = new DependentSetComparator(tranFiringFreqMap); 
		PriorityQueue<DependentSet> dependentSetQueue = new PriorityQueue<DependentSet>(curEnabled.size(), depComp);
		for (LpnTransitionPair enabledTran : curEnabled) {
//			System.out.println("@ partialOrderReduction, consider transition " + sgList[enabledTran.getLpnIndex()].getLpn().getLabel() 
//					+ "("+ sgList[enabledTran.getLpnIndex()].getLpn().getTransition(enabledTran.getTranIndex()) + ")");
			HashSet<LpnTransitionPair> dependent = new HashSet<LpnTransitionPair>();
			Transition enabledTransition = sgList[enabledTran.getLpnIndex()].getLpn().getAllTransitions()[enabledTran.getTranIndex()];
			boolean enabledIsDummy = false;
			boolean isCycleClosingAmpleComputation = false;
			dependent = computeDependent(curStateArray,enabledTran,dependent,curEnabled,staticMap, lpnList, isCycleClosingAmpleComputation);
//			printIntegerSet(dependent, sgList, "dependent set for enabled transition " + sgList[enabledTran.getLpnIndex()].getLpn().getLabel() 
//					+ "(" + enabledTransition.getName() + ")");
			// TODO: temporarily dealing with dummy transitions (This requires the dummy transitions to have "_dummy" in their names.)				
			if(isDummyTran(enabledTransition.getName()))
				enabledIsDummy = true;
			DependentSet dependentSet = new DependentSet(dependent, enabledTran, enabledIsDummy);
			dependentSetQueue.add(dependentSet);
//				if (dependent.size() < ready.size() && !dependentOnlyHasDummyTrans) 
//					ready = (HashSet<Integer>) dependent.clone();
//				if (ready.size() == 1)
//					return ready;
		}
		// TODO: If a local state did not change, there is no need to clear its necessary mappings.
		necessaryMap.clear();
		ready = dependentSetQueue.poll().getDependent();
		return ready;
	}

	private boolean isDummyTran(String tranName) {
		if (tranName.contains("_dummy"))
			return true;
		else
			return false;
	}
    
    private HashSet<LpnTransitionPair> computeDependent(State[] curStateArray,
			LpnTransitionPair enabledLpnTran, HashSet<LpnTransitionPair> dependent, HashSet<LpnTransitionPair> curEnabled, 
			HashMap<LpnTransitionPair, StaticSets> staticMap, LhpnFile[] lpnList, boolean isCycleClosingAmpleComputation) {
		// canDisable is the set of transitions that can be disabled by firing tranIndex.
		HashSet<LpnTransitionPair> canBeDisabled = staticMap.get(enabledLpnTran).getDisabled(); 
		HashSet<LpnTransitionPair> transCanLoseToken = staticMap.get(enabledLpnTran).getDisableByStealingToken();
		HashSet<LpnTransitionPair> canModifyAssign = staticMap.get(enabledLpnTran).getModifyAssignSet();
		if (Options.getDebugMode()) {
			System.out.println("@ getDependentSet, consider transition " + lpnList[enabledLpnTran.getLpnIndex()].getTransition(enabledLpnTran.getTranIndex()).getName());
			printIntegerSet(canModifyAssign, lpnList, lpnList[enabledLpnTran.getLpnIndex()].getTransition(enabledLpnTran.getTranIndex()) + " can modify assignments");
			printIntegerSet(canBeDisabled, lpnList, lpnList[enabledLpnTran.getLpnIndex()].getTransition(enabledLpnTran.getTranIndex()) + " can disable");
		}
		dependent.add(enabledLpnTran);
		for (LpnTransitionPair lpnTranPair : canModifyAssign) {
			if (curEnabled.contains(lpnTranPair))
				dependent.add(lpnTranPair);
		}
		if (Options.getDebugMode()) 
			printIntegerSet(dependent, lpnList, "@ getDependentSet, before canBeDisabled, dependent " + lpnList[enabledLpnTran.getLpnIndex()].getTransition(enabledLpnTran.getTranIndex()));
		for (LpnTransitionPair tranCanBeDisabled : canBeDisabled) {
			if (Options.getDebugMode()) 
				System.out.println("tranCanBeDisabled = " + lpnList[tranCanBeDisabled.getLpnIndex()].getLabel()  
						+ "(" + lpnList[tranCanBeDisabled.getLpnIndex()].getTransition(tranCanBeDisabled.getTranIndex()) + ")");
			boolean tranCanBeDisabledPersistent = lpnList[tranCanBeDisabled.getLpnIndex()].getTransition(tranCanBeDisabled.getTranIndex()).isPersistent();
			if (curEnabled.contains(tranCanBeDisabled) && !dependent.contains(tranCanBeDisabled)
					&& (transCanLoseToken.contains(tranCanBeDisabled) || !tranCanBeDisabledPersistent)) {
				dependent.addAll(computeDependent(curStateArray,tranCanBeDisabled,dependent,curEnabled,staticMap, lpnList, isCycleClosingAmpleComputation));
				if (Options.getDebugMode()) 
					printIntegerSet(dependent, lpnList, "@ getDependentSet at 0 for transition " + lpnList[enabledLpnTran.getLpnIndex()].getTransition(enabledLpnTran.getTranIndex()));
			}
			else if (!curEnabled.contains(tranCanBeDisabled)) {
				if(Options.getPOR().toLowerCase().equals("tboff")  // no trace-back
						|| (Options.getCycleClosingAmpleMethd().toLowerCase().equals("cctboff") && isCycleClosingAmpleComputation)) {
					dependent.addAll(curEnabled);
					break;
				}
				LpnTransitionPair origTran = new LpnTransitionPair(tranCanBeDisabled.getLpnIndex(), tranCanBeDisabled.getTranIndex());
				HashSet<LpnTransitionPair> necessary = null;
				if (Options.getDebugMode()) 
					printNecessaryMap(lpnList);
				if (necessaryMap.containsKey(tranCanBeDisabled)) {
					if (Options.getDebugMode()) 
						System.out.println("Found transition " + lpnList[tranCanBeDisabled.getLpnIndex()].getLabel() + "("
							+ lpnList[tranCanBeDisabled.getLpnIndex()].getTransition(tranCanBeDisabled.getTranIndex()).getName() + ") in ncessaryMap.");
					necessary = necessaryMap.get(tranCanBeDisabled);
				}
				else
					necessary = computeNecessary(curStateArray,tranCanBeDisabled,dependent,curEnabled, staticMap, lpnList, origTran);
				if (necessary != null && !necessary.isEmpty()) {
					if (Options.getDebugMode()) 
						printIntegerSet(necessary, lpnList, "necessary set for transition " + lpnList[tranCanBeDisabled.getLpnIndex()].getLabel() 
								+ "(" + lpnList[tranCanBeDisabled.getLpnIndex()].getTransition(tranCanBeDisabled.getTranIndex()) + ")");
					for (LpnTransitionPair tranNecessary : necessary) {
						if (!dependent.contains(tranNecessary)) {
							if (Options.getDebugMode()) {
							System.out.println("Dependent set does not contain this transition found by computeNecessary: " + 
									lpnList[tranNecessary.getLpnIndex()].getLabel() + "(" + lpnList[tranNecessary.getLpnIndex()].getTransition(tranNecessary.getTranIndex()) + ")"
									+ ". Compute its dependent set.");
							}
							dependent.addAll(computeDependent(curStateArray,tranNecessary,dependent,curEnabled,staticMap, lpnList, isCycleClosingAmpleComputation));
						}					
					}
				}
				else {
					if (Options.getDebugMode()) 
						System.out.println("necessary set for transition " + lpnList[enabledLpnTran.getLpnIndex()].getTransition(enabledLpnTran.getTranIndex()) + " is empty.");
					dependent.addAll(curEnabled);
				}
				if (Options.getDebugMode()) 
					printIntegerSet(dependent, lpnList, "@ getDependentSet at 1 for transition " + lpnList[enabledLpnTran.getLpnIndex()].getTransition(enabledLpnTran.getTranIndex()));
			}
		}
		return dependent;
	}

	@SuppressWarnings("unchecked")
	private HashSet<LpnTransitionPair> computeNecessary(State[] curStateArray,
			LpnTransitionPair tran, HashSet<LpnTransitionPair> dependent,
			HashSet<LpnTransitionPair> curEnabledIndices, HashMap<LpnTransitionPair, StaticSets> staticMap, 
			LhpnFile[] lpnList, LpnTransitionPair origTran) {
		if (Options.getDebugMode()) 
			System.out.println("@ getNecessary, consider transition: " + lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex()).getName() + ")");
		if (necessaryMap.containsKey(tran)) {
			if (Options.getDebugMode()) 
				System.out.println("Found transition" + lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex()).getName() + ")"
						+ "'s necessary set in ncessaryMap. ");
			return necessaryMap.get(tran);
		}
		// Search for transition(s) that can help to bring the marking(s).
		HashSet<LpnTransitionPair> nMarking = null;
		Transition transition = lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex());
		int[] presetPlaces = lpnList[tran.getLpnIndex()].getPresetIndex(transition.getName());
		for (int i=0; i < presetPlaces.length; i++) {
			int place = presetPlaces[i];
			if (curStateArray[tran.getLpnIndex()].getMarking()[place]==0) {
				if (Options.getDebugMode()) 
					System.out.println("####### compute nMarking for transition " + lpnList[tran.getLpnIndex()].getLabel() 
							+ "(" + lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex()).getName() +  ") " + "########");
				HashSet<LpnTransitionPair> nMarkingTemp = new HashSet<LpnTransitionPair>();				
				String placeName = lpnList[tran.getLpnIndex()].getAllPlaces().get(place);
				if (Options.getDebugMode()) 
					System.out.println("preset place of " + transition.getName() + " is " + placeName);
				int[] presetTrans = lpnList[tran.getLpnIndex()].getPresetIndex(placeName);
				for (int j=0; j < presetTrans.length; j++) {
					LpnTransitionPair presetTran = new LpnTransitionPair(tran.getLpnIndex(), presetTrans[j]);
					if (Options.getDebugMode()) 
						System.out.println("preset transition of " + placeName + " is " + lpnList[presetTran.getLpnIndex()].getLabel() + "(" + lpnList[presetTran.getLpnIndex()].getTransition(presetTran.getTranIndex()) + ")");
					if (origTran.getVisitedTrans().contains(presetTran)) {
						if (Options.getDebugMode()) 
							System.out.println("Transition " + lpnList[presetTran.getLpnIndex()].getLabel() + "(" + lpnList[presetTran.getLpnIndex()].getTransition(presetTran.getTranIndex()) + ") is visted before by " 
								+ lpnList[origTran.getLpnIndex()].getLabel() + "(" + lpnList[origTran.getLpnIndex()].getTransition(origTran.getTranIndex()).getName() + ").");
						if (necessaryMap.containsKey(presetTran)) {
							if (Options.getDebugMode()) 
								System.out.println("Found transition" + lpnList[presetTran.getLpnIndex()].getLabel() + "(" + lpnList[presetTran.getLpnIndex()].getTransition(presetTran.getTranIndex()).getName() + ")"
									+ "'s necessary set in ncessaryMap. Add it to nMarkingTemp.");
							nMarkingTemp = necessaryMap.get(presetTran);
						}
						if (curEnabledIndices.contains(presetTran)) {
							if (Options.getDebugMode())
								System.out.println("@ nMarking: curEnabled contains transition " + lpnList[presetTran.getLpnIndex()].getLabel() 
										+ "(" + lpnList[presetTran.getLpnIndex()].getTransition(presetTran.getTranIndex()).getName() + "). Add to nMarkingTmp.");
							nMarkingTemp = necessaryMap.get(presetTran);
						}					
						continue;
					}
					else
						origTran.addVisitedTran(presetTran);
					if (Options.getDebugMode()) {
						System.out.println("~~~~~~~~~ transVisited for transition " + lpnList[origTran.getLpnIndex()].getLabel() + "(" + lpnList[origTran.getLpnIndex()].getTransition(origTran.getTranIndex()).getName() +  ")~~~~~~~~~"); 
						for (LpnTransitionPair visitedTran : origTran.getVisitedTrans())
							System.out.println(lpnList[visitedTran.getLpnIndex()].getLabel() + "(" + lpnList[visitedTran.getLpnIndex()].getTransition(visitedTran.getTranIndex()).getName() +  ") ");
						System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
						System.out.println("@ getNecessary, consider transition: " + lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getTransition(presetTrans[j]).getName() + ").");
					}

					if (curEnabledIndices.contains(presetTran)) {
						if (Options.getDebugMode())
							System.out.println("@ nMarking: curEnabled contains transition " + lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getTransition(presetTran.getTranIndex()).getName() + "). Add to nMarkingTmp.");
						nMarkingTemp.add(new LpnTransitionPair(tran.getLpnIndex(), presetTrans[j]));
					}
					else {
						if (Options.getDebugMode())
							System.out.println("@ nMarking: transition " + lpnList[tran.getLpnIndex()].getTransition(presetTrans[j]).getName() + " is not enabled. Compute its necessary set.");
						HashSet<LpnTransitionPair> tmp = null;
						tmp = computeNecessary(curStateArray, presetTran, dependent, 
													curEnabledIndices, staticMap, lpnList, origTran);
						if (tmp != null)							
							nMarkingTemp.addAll(tmp);
						else  
							if (Options.getDebugMode())
								System.out.println("@ nMarking: necessary set for transition " + lpnList[tran.getLpnIndex()].getLabel() 
									+ "(" + lpnList[tran.getLpnIndex()].getTransition(presetTrans[j]).getName() + ") is null.");
					}
				}
				if (nMarkingTemp != null)
					if (nMarking == null || nMarkingTemp.size() < nMarking.size()) 
						nMarking = (HashSet<LpnTransitionPair>) nMarkingTemp.clone();
			}
			else 
				if (Options.getDebugMode())
					System.out.println("Place " + lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getAllPlaces().get(place) + ") is marked.");
		}
		// Search for transition(s) that can help to enable the current transition. 
		HashSet<LpnTransitionPair> nEnable = null;
		int[] varValueVector = curStateArray[tran.getLpnIndex()].getVector();//curState.getVector();
		HashSet<LpnTransitionPair> canEnable = staticMap.get(tran).getEnable();
		if (Options.getDebugMode()) {
			System.out.println("####### compute nEnable for transition " + lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex()).getName() +  ") " + "########");
			printIntegerSet(canEnable, lpnList, lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex()) + ") can be enabled by");
		}
		if (transition.getEnablingTree() != null
				&& transition.getEnablingTree().evaluateExpr(lpnList[tran.getLpnIndex()].getAllVarsWithValuesAsString(varValueVector)) == 0.0
				&& !canEnable.isEmpty()) {
			nEnable = new HashSet<LpnTransitionPair>();
			for (LpnTransitionPair tranCanEnable : canEnable) {
				if (curEnabledIndices.contains(tranCanEnable)) {
					nEnable.add(tranCanEnable);
					if (Options.getDebugMode())	
						System.out.println("@ nEnable: curEnabled contains transition " + lpnList[tranCanEnable.getLpnIndex()].getLabel() + "(" + lpnList[tranCanEnable.getLpnIndex()].getTransition(tranCanEnable.getTranIndex()).getName() + "). Add to nEnable.");
				}
				else {
					if (origTran.getVisitedTrans().contains(tranCanEnable)) {
						if (Options.getDebugMode()) 
							System.out.println("Transition " + lpnList[tranCanEnable.getLpnIndex()].getLabel() + "(" + lpnList[tranCanEnable.getLpnIndex()].getTransition(tranCanEnable.getTranIndex()) + ") is visted before by " 
								+ lpnList[origTran.getLpnIndex()].getLabel() + "(" + lpnList[origTran.getLpnIndex()].getTransition(origTran.getTranIndex()).getName() + ").");
						if (necessaryMap.containsKey(tranCanEnable)) {
							if (Options.getDebugMode()) 
								System.out.println("Found transition" + lpnList[tranCanEnable.getLpnIndex()].getLabel() + "(" + lpnList[tranCanEnable.getLpnIndex()].getTransition(tranCanEnable.getTranIndex()).getName() + ")"
									+ "'s necessary set in ncessaryMap. Add it to nMarkingTemp.");
							nEnable.addAll(necessaryMap.get(tranCanEnable));
						}				
						continue;
					}
					else
						origTran.addVisitedTran(tranCanEnable);
					if (Options.getDebugMode())
						System.out.println("@ nEnable: transition " + lpnList[tranCanEnable.getLpnIndex()].getTransition(tranCanEnable.getTranIndex()).getName() + " is not enabled. Compute its necessary set.");
					HashSet<LpnTransitionPair> tmp = null;
					tmp = computeNecessary(curStateArray, tranCanEnable, dependent, 
												curEnabledIndices, staticMap, lpnList, origTran);
					if (tmp != null)
						nEnable.addAll(tmp);
					else
						if (Options.getDebugMode())
							System.out.println("@ nEnable: necessary set for transition " + lpnList[tranCanEnable.getLpnIndex()].getLabel() + "(" + lpnList[tranCanEnable.getLpnIndex()].getTransition(tranCanEnable.getTranIndex()).getName() + ") is null.");
				}
			}
		}
		if (Options.getDebugMode()) {
			if (transition.getEnablingTree() == null)
				System.out.println("@ nEnable: transition " + lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex()).getName() + ") has no enabling condition.");
			else if (transition.getEnablingTree().evaluateExpr(lpnList[tran.getLpnIndex()].getAllVarsWithValuesAsString(varValueVector)) !=0.0)
				System.out.println("@ nEnable: transition " + lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex()).getName() + ")'s enabling condition is true.");
			else if (transition.getEnablingTree() != null
					&& transition.getEnablingTree().evaluateExpr(lpnList[tran.getLpnIndex()].getAllVarsWithValuesAsString(varValueVector)) == 0.0
					&& canEnable.isEmpty()) 
				System.out.println("@ nEnable: transition " + lpnList[tran.getLpnIndex()].getLabel() + "(" + lpnList[tran.getLpnIndex()].getTransition(tran.getTranIndex()).getName() 
						+ ")'s enabling condition is false, but no other transitions that can help to enable it were found .");		
			printIntegerSet(nMarking, lpnList, "nMarking for transition " + transition.getName());
			printIntegerSet(nEnable, lpnList, "nEnable for transition " + transition.getName());
		}

		if (nEnable == null && nMarking != null) {
			if (!nMarking.isEmpty()) 
				necessaryMap.put(tran, nMarking);
			if (Options.getDebugMode())
				printNecessaryMap(lpnList);	
			return nMarking;
		}
		else if (nMarking == null && nEnable != null) {
			if (!nEnable.isEmpty())
				necessaryMap.put(tran, nEnable);
			if (Options.getDebugMode())
				printNecessaryMap(lpnList);
			return nEnable;
		}
		else if (nMarking == null && nEnable == null) {
			return null;
		}
		else {
			if (!nMarking.isEmpty() && !nEnable.isEmpty()) {
				if (getIntSetSubstraction(nMarking, dependent).size() < getIntSetSubstraction(nEnable, dependent).size()) {
					necessaryMap.put(tran, nMarking);
					if (Options.getDebugMode())
						printNecessaryMap(lpnList);
					return nMarking;
				}
				else {
					necessaryMap.put(tran, nEnable);
					if (Options.getDebugMode())
						printNecessaryMap(lpnList);
					return nEnable;
				}
			}
			else if (nMarking.isEmpty() && !nEnable.isEmpty()) {
				necessaryMap.put(tran, nEnable);
				if (Options.getDebugMode())
					printNecessaryMap(lpnList);
				return nEnable;
			}
			else { 
				necessaryMap.put(tran, nMarking);
				if (Options.getDebugMode())
					printNecessaryMap(lpnList);
				return nMarking;
			}
				
		}
	}

	private void printNecessaryMap(LhpnFile[] lpnList) {
		System.out.println("================ necessaryMap =================");
		for (LpnTransitionPair key : necessaryMap.keySet()) {
			System.out.print(lpnList[key.getLpnIndex()].getLabel() + "(" + lpnList[key.getLpnIndex()].getTransition(key.getTranIndex()).getName() + ") => ");
			HashSet<LpnTransitionPair> necessarySet = necessaryMap.get(key);
			for (LpnTransitionPair necessary : necessarySet) {
				System.out.print(lpnList[necessary.getLpnIndex()].getLabel() + "(" + lpnList[necessary.getLpnIndex()].getTransition(necessary.getTranIndex()).getName() + ") ");
			}
			System.out.print("\n");
		}
	}

	private HashSet<LpnTransitionPair> getIntSetSubstraction(
			HashSet<LpnTransitionPair> left, HashSet<LpnTransitionPair> right) {
		HashSet<LpnTransitionPair> sub = new HashSet<LpnTransitionPair>();
		for (LpnTransitionPair lpnTranPair : left) {
			if (!right.contains(lpnTranPair))
				sub.add(lpnTranPair);
		}
		return sub;
	}
	
//	private LpnTranList getSetSubtraction(LpnTranList left, LpnTranList right) {
//		LpnTranList sub = new LpnTranList();
//		for (Transition lpnTran : left) {
//			if (!right.contains(lpnTran))
//				sub.add(lpnTran);
//		}
//		return sub;
//	}
	
	
	public boolean stateOnStack(int lpnIndex, State curState, HashSet<PrjState> stateStack) {
		boolean isStateOnStack = false;
		for (PrjState prjState : stateStack) {
			State[] stateArray = prjState.toStateArray();
			if (stateArray[lpnIndex] == curState) {
				isStateOnStack = true;
				break;
			}
		}
		return isStateOnStack;
	}
	
	private void printIntegerSet(HashSet<LpnTransitionPair> indicies, StateGraph[] sgList, String setName) {
		if (!setName.isEmpty())
			System.out.print(setName + ": ");
		if (indicies == null) {
			System.out.println("null");
		}
		else if (indicies.isEmpty()) {
			System.out.println("empty");
		}
		else {
			for (LpnTransitionPair lpnTranPair : indicies) {
				System.out.print("\t" + sgList[lpnTranPair.getLpnIndex()].getLpn().getLabel() + "(" 
						+ sgList[lpnTranPair.getLpnIndex()].getLpn().getAllTransitions()[lpnTranPair.getTranIndex()].getName() + ") \t");
			}
			System.out.print("\n");
		}				
	}
	
	private void printIntegerSet(HashSet<LpnTransitionPair> indicies,
			LhpnFile[] lpnList, String setName) {
		if (!setName.isEmpty())
			System.out.print(setName + ": ");
		if (indicies == null) {
			System.out.println("null");
		}
		else if (indicies.isEmpty()) {
			System.out.println("empty");
		}
		else {
			for (LpnTransitionPair lpnTranPair : indicies) {
				System.out.print(lpnList[lpnTranPair.getLpnIndex()].getLabel() + "(" 
						+ lpnList[lpnTranPair.getLpnIndex()].getAllTransitions()[lpnTranPair.getTranIndex()].getName() + ") \t");
			}
			System.out.print("\n");
		}			
		
	}
}
