package verification.timed_state_exploration.zone;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;
import verification.platu.logicAnalysis.Analysis;
import verification.platu.lpn.LPNTranRelation;
import verification.platu.lpn.LpnTranList;
import verification.platu.project.PrjState;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

public class Analysis_Timed extends Analysis{

	public Analysis_Timed(StateGraph[] lpnList, State[] initStateArray,
			LPNTranRelation lpnTranRelation, String method) {
		super(lpnList, initStateArray, lpnTranRelation, method);
		// TODO Auto-generated constructor stub
	}

	public Analysis_Timed(StateGraph[] lpnList) {
		super(lpnList);
		// TODO Auto-generated constructor stub
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
					curEnabled = (sgList[curIndex].getEnabled((TimedState) curStateArray[curIndex])).clone();
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
				LinkedList<Transition> enabledList = sg.getEnabled((TimedState) curStateArray[i]);
				curEnabledArray[i] = enabledList;
				enabledList = sg.getEnabled((TimedState) nextStateArray[i]);
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

		return sgList;
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

}
