package platu.logicAnalysis;

import java.util.*;
import platu.project.*;
import platu.MDD.MDT;
import platu.MDD.Mdd;
import platu.MDD.mddNode;
import platu.common.Common;
import platu.common.IndexObjMap;
import platu.logicAnalysis.Analysis;
import platu.lpn.LPNTran;
import platu.lpn.LPNTranRelation;
import platu.lpn.LpnTranList;
import platu.main.Options;
import platu.stategraph.*;

import platu.por1.*;

public class Analysis {

	private LinkedList<LPNTran> traceCex;
	protected Mdd mddMgr = null;

	
	static
	{
		System.loadLibrary("ArrayImplementation");
	}

	private static native boolean arrayLookup(int[] array);
	
	
	public Analysis(StateGraph[] lpnList, State[] initStateArray, LPNTranRelation lpnTranRelation, String method) {
		traceCex = new LinkedList<LPNTran>();
		mddMgr = new Mdd(lpnList.length);

		if (method.equals("dfs")) {
			if (Options.getPOR().equals("off")) {
				this.search_dfs(lpnList, initStateArray);
				//this.search_dfs_mdd_1(lpnList, initStateArray);
				//this.search_dfs_mdd_2(lpnList, initStateArray);
			}
			else
				this.search_dfs_por(lpnList, initStateArray, lpnTranRelation, "state");
		}
		else if (method.equals("bfs")==true)
			this.search_bfs(lpnList, initStateArray);
		else if (method == "dfs_noDisabling")
			//this.search_dfs_noDisabling(lpnList, initStateArray);
			this.search_dfs_noDisabling_fireOrder(lpnList, initStateArray);
	}


	/**
	 * Recursively find all reachable project states.
	 */
	int iterations = 0;
	int stack_depth = 0;
	int max_stack_depth = 0;

	public void search_recursive(final StateGraph[] lpnList,
			final State[] curPrjState,
			final ArrayList<LinkedList<LPNTran>> enabledList,
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
			LinkedList<LPNTran> curEnabledSet = enabledList.get(index);

			if (curEnabledSet == null)
				continue;

			for (LPNTran firedTran : curEnabledSet) {
				// while(curEnabledSet.size() != 0) {
				// LPNTran firedTran = curEnabledSet.removeFirst();

				State[] nextStateArray = firedTran.fire(lpnList, curPrjState);

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
				ArrayList<LinkedList<LPNTran>> nextEnabledList = new ArrayList<LinkedList<LPNTran>>();
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
	 * @param lpnList
	 * @param curLocalStateArray
	 * @param enabledArray
	 */
	
	public Stack<State[]> search_dfs(final StateGraph[] lpnList, final State[] initStateArray) {
		System.out.println("---> calling function search_dfs");
				
		double peakUsedMem = 0;
		double peakTotalMem = 0;
		boolean failure = false;
		int tranFiringCnt = 0;
		int totalStates = 1;
		int arraySize = lpnList.length;
		
		//Stack<State[]> stateStack = new Stack<State[]>();
		HashSet<PrjState> stateStack = new HashSet<PrjState>();
		Stack<LinkedList<LPNTran>> lpnTranStack = new Stack<LinkedList<LPNTran>>();
		Stack<Integer> curIndexStack = new Stack<Integer>();

		HashSet<PrjState> prjStateSet = new HashSet<PrjState>();
		
		PrjState initPrjState = new PrjState(initStateArray);
		prjStateSet.add(initPrjState);
		
		PrjState stateStackTop = initPrjState;
		stateStack.add(stateStackTop);
		LpnTranList initEnabled = lpnList[0].getEnabled(initStateArray[0]);
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
			LinkedList<LPNTran> curEnabled = lpnTranStack.peek();

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
					curEnabled = (lpnList[curIndex].getEnabled(curStateArray[curIndex])).clone();
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

			LPNTran firedTran = curEnabled.removeLast();			
			State[] nextStateArray = firedTran.fire(lpnList, curStateArray);
			tranFiringCnt++;

			// Check if the firedTran causes disabling error or deadlock.
			LinkedList<LPNTran>[] curEnabledArray = new LinkedList[arraySize];
			LinkedList<LPNTran>[] nextEnabledArray = new LinkedList[arraySize];
			for (int i = 0; i < arraySize; i++) {
				StateGraph lpn_tmp = lpnList[i];
				LinkedList<LPNTran> enabledList = lpn_tmp.getEnabled(curStateArray[i]);
				curEnabledArray[i] = enabledList;
				enabledList = lpn_tmp.getEnabled(nextStateArray[i]);
				nextEnabledArray[i] = enabledList;

				LPNTran disabledTran = firedTran.disablingError(
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
				System.out.println("*** Verification failed: deadlock.");
				failure = true;
				break main_while_loop;
			}

			PrjState nextPrjState = new PrjState(nextStateArray);
			Boolean	existingState = prjStateSet.contains(nextPrjState) || stateStack.contains(nextPrjState);
			
			
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

		return null;
	}

	
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
			LinkedList<LPNTran> enabledTrans = lpnList[i].getEnabled(initStateArray[i]);
			HashSet<LPNTran> enabledSet = new HashSet<LPNTran>();
			if(!enabledTrans.isEmpty())
			{
				for(LPNTran tran : enabledTrans) {
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

			LPNTran firedTran = null;
			if(curEnabled[0].size() != 0)
				firedTran = curEnabled[0].removeFirst();
			else
				firedTran = curEnabled[1].removeFirst();

			traceCex.addLast(firedTran);
			
			State[] curStateArray = new State[arraySize];
			for( int i = 0; i < arraySize; i++)
				curStateArray[i] = curLpnStateArray[i].getState();
			
			State[] nextStateArray = firedTran.fire(lpnList, curStateArray);			

			// Check if the firedTran causes disabling error or deadlock.
			HashSet<LPNTran>[] extendedNextEnabledArray = new HashSet[arraySize];
			for (int i = 0; i < arraySize; i++) {				
				HashSet<LPNTran> curEnabledSet = curLpnStateArray[i].getEnabled();
				LpnTranList nextEnabledList = lpnList[i].getEnabled(nextStateArray[i]);
				HashSet<LPNTran> nextEnabledSet = new HashSet<LPNTran>();
				for(LPNTran tran : nextEnabledList) {
					nextEnabledSet.add(tran);
				}
				
				extendedNextEnabledArray[i] = nextEnabledSet;

				//non_disabling
				for(LPNTran curTran : curEnabledSet) {
					if(curTran == firedTran)
						continue;
										
					if(nextEnabledSet.contains(curTran) == false) {
						int[] nextMarking = nextStateArray[i].getMarking();
						int[] preset = curTran.getPreSet();
						
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
				HashSet<LPNTran> lpnEnabledSet = new HashSet<LPNTran>();
				for(LPNTran tran : extendedNextEnabledArray[i]) {
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
				for(LPNTran tran : nextLpnStateArray[i].getEnabled())
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
			LpnState[] cur = null;
			LpnState[] next = null;
			for(LPNTran tran : traceCex)
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
		Stack<LinkedList<LPNTran>> lpnTranStack = new Stack<LinkedList<LPNTran>>();
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
			LinkedList<LPNTran> curEnabled = lpnTranStack.peek();

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

			LPNTran firedTran = curEnabled.removeLast();
			State[] nextStateArray = firedTran.fire(lpnList, curStateArray);
			tranFiringCnt++;

			// Check if the firedTran causes disabling error or deadlock.
			LinkedList<LPNTran>[] curEnabledArray = new LinkedList[arraySize];
			LinkedList<LPNTran>[] nextEnabledArray = new LinkedList[arraySize];
			for (int i = 0; i < arraySize; i++) {
				StateGraph lpn_tmp = lpnList[i];
				LinkedList<LPNTran> enabledList = lpn_tmp.getEnabled(curStateArray[i]);
				curEnabledArray[i] = enabledList;
				enabledList = lpn_tmp.getEnabled(nextStateArray[i]);
				nextEnabledArray[i] = enabledList;

				LPNTran disabledTran = firedTran.disablingError(
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
				System.out.println("*** Verification failed: deadlock.");
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
				LinkedList<LPNTran> curEnabledSet = curLpn.getEnabled(curState);

				LpnTranList curEnabled = (LpnTranList) curEnabledSet.clone();
				while (curEnabled.size() > 0) {
					LPNTran firedTran = curEnabled.removeLast();
					nextStateArray = firedTran.fire(lpnList, curStateArray);
					tranFiringCnt++;

					for (int i = 0; i < arraySize; i++) {
						StateGraph lpn_tmp = lpnList[i];
						if (curStateArray[i] == nextStateArray[i])
							continue;

						LinkedList<LPNTran> curEnabled_l = lpn_tmp.getEnabled(curStateArray[i]);
						LinkedList<LPNTran> nextEnabled = lpn_tmp.getEnabled(nextStateArray[i]);
						LPNTran disabledTran = firedTran.disablingError(curEnabled_l, nextEnabled);
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


	public LinkedList<LPNTran> search_bfs(final StateGraph[] sgList, final State[] initStateArray) {
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
				
				curStateArray = frontier.pop();
				int[] localIdxArray = Analysis.getLocalStateIdxArray(sgList, curStateArray, false);
				mddMgr.add(reach, localIdxArray, false);
				totalStates++;
				
				for (int i = 0; i < arraySize; i++) {
					LinkedList<LPNTran> curEnabled = sgList[i].getEnabled(curStateArray[i]);
					if (curEnabled.size() > 0)
						deadlock = false;
					
					for (LPNTran firedTran : curEnabled) {
						State[] nextStateArray = firedTran.fire(sgList, curStateArray);
						tranFiringCnt++;
				
						/*
						 * Check if any transitions can be disabled by fireTran.
						 */
						LinkedList<LPNTran> nextEnabled = sgList[i].getEnabled(nextStateArray[i]);
						LPNTran disabledTran = firedTran.disablingError(curEnabled, nextEnabled);
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
	public LinkedList<LPNTran> search_bfs_mdd_localFirings(final StateGraph[] lpnList, final State[] initStateArray) {
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
		int totalStates = 1;

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
				LinkedList<LPNTran> curLocalEnabled = curLpn.getEnabled(curStateArray[index]);

				if (curLocalEnabled.size() == 0 || curLocalEnabled.getFirst().local() == true)
					continue;

				for (LPNTran firedTran : curLocalEnabled) {

					if (firedTran.local() == true)
						continue;

					State[] nextStateArray = firedTran.fire(lpnList, curStateArray);
					tranFiringCnt++;

					ArrayList<LinkedList<LPNTran>> nextEnabledArray = new ArrayList<LinkedList<LPNTran>>(1);
					for (int i = 0; i < arraySize; i++) {
						if (curStateArray[i] == nextStateArray[i].getIndex())
							continue;

						StateGraph lpn_tmp = lpnList[i];
						LinkedList<LPNTran> curEnabledList = lpn_tmp.getEnabled(curStateArray[i]);
						LinkedList<LPNTran> nextEnabledList = lpn_tmp.getEnabled(nextStateArray[i].getIndex());

						LPNTran disabledTran = firedTran.disablingError(curEnabledList, nextEnabledList);
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
						System.err.println("*** Verification failed: deadlock.");
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
		AmpleSet ampleClass = new AmpleSet();
		//AmpleSubset ampleClass = new AmpleSubset();
		HashMap<LPNTran,HashSet<LPNTran>> indepTranSet = new HashMap<LPNTran,HashSet<LPNTran>>();
		
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
		Stack<HashSet<LPNTran>> firedTranStack = new Stack<HashSet<LPNTran>>();  
		
		//get initial enable transition set
		LinkedList<LPNTran>[] initEnabledArray = new LinkedList[arraySize];
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
		firedTranStack.push(new HashSet<LPNTran>());
				
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
		
			LPNTran firedTran = curEnabled.removeFirst();
			firedTranStack.peek().add(firedTran);
			traceCex.addLast(firedTran);
			
			//System.out.println(tranFiringCnt + ": firedTran: "+ firedTran.getFullLabel());
			
			State[] nextStateArray = firedTran.fire(lpnList, curStateArray);
			tranFiringCnt++;
			
			// Check if the firedTran causes disabling error or deadlock.
			LinkedList<LPNTran>[] curEnabledArray = new LinkedList[arraySize];
			LinkedList<LPNTran>[] nextEnabledArray = new LinkedList[arraySize];
			for (int i = 0; i < arraySize; i++) {
				lpnList[i].getLpn().setIndex(i);
				StateGraph lpn_tmp = lpnList[i];
				LinkedList<LPNTran> enabledList = lpn_tmp.getEnabled(curStateArray[i]);
				curEnabledArray[i] = enabledList;
				enabledList = lpn_tmp.getEnabled(nextStateArray[i]);
				nextEnabledArray[i] = enabledList;

				LPNTran disabledTran = firedTran.disablingError(curEnabledArray[i], nextEnabledArray[i]);
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
				firedTranStack.push(new HashSet<LPNTran>());
					

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
					for (LPNTran tran : curEnabledArray[i]) {
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

					for (LPNTran tran : ampleSet)
						newCurEnabledArray[tran.getLpn().getIndex()].addLast(tran);

					sortedAmpleSet = new LpnTranList();
					for (int i = 0; i < arraySize; i++) {
						LpnTranList localEnabledSet = newCurEnabledArray[i];
						for (LPNTran tran : localEnabledSet)
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
		
		long curTotalMem = Runtime.getRuntime().totalMemory();
		long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("SUMMARY: # LPN transition firings: " + tranFiringCnt
                + ", # of prjStates found: " + stateCount
                + ", max_stack_depth: " + max_stack_depth
                + ", used memory: " + (float) curUsedMem / 1000000
				+ ", free memory: "
				+ (float) Runtime.getRuntime().freeMemory() / 1000000);
        
        return null;
    }
	
	/*
	 * Check if this project deadlocks in the current state 'stateArray'.
	 */
	public static boolean deadLock(StateGraph[] lpnArray, State[] stateArray) {
		boolean deadlock = true;
		for (int i = 0; i < stateArray.length; i++) {
			LinkedList<LPNTran> tmp = lpnArray[i].getEnabled(stateArray[i]);
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
			LinkedList<LPNTran> tmp = lpnArray[i].getEnabled(stateIdxArray[i]);
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
	
	public static LpnTranList[] getStickyTrans(LpnTranList[] enabledArray, LPNTran firedTran) {
		int arraySize = enabledArray.length;
		LpnTranList[] stickyTranArray = new LpnTranList[arraySize];
		for (int i = 0; i < arraySize; i++) {
			stickyTranArray[i] = new LpnTranList();
			for (LPNTran tran : enabledArray[i]) {
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
			LpnTranList[] nextStickyTransArray, State nextState) {
		int arraySize = curStickyTransArray.length;
		LpnTranList[] stickyTransArray = new LpnTranList[arraySize];
		boolean[] hasStickyTrans = new boolean[arraySize];
		
		for (int i = 0; i < arraySize; i++) {
			HashSet<LPNTran> tmp = new HashSet<LPNTran>();
			if(nextStickyTransArray[i] != null)
				for(LPNTran tran : nextStickyTransArray[i])
					tmp.add(tran);
			
			
			stickyTransArray[i] = new LpnTranList();
			hasStickyTrans[i] = false;
			for (LPNTran tran : curStickyTransArray[i]) {
				if (tran.sticky() == true && tmp.contains(tran)==false) {
					int[] nextMarking = nextState.getMarking();
					int[] preset = tran.getPreSet();
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
