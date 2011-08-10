package platu.logicAnalysis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import platu.Options;
import platu.expression.VarNode;
import platu.lpn.LPN;
import platu.lpn.LPNTran;
import platu.lpn.VarSet;
import platu.stategraph.StateGraph;
import platu.stategraph.state.State;

public class CompositionalAnalysis {  
	public CompositionalAnalysis(List<StateGraph> du){
		//super(du);
	}
	
	public CompositeStateGraph compose(StateGraph sg1, StateGraph sg2){
		long start = System.currentTimeMillis(); 
		
		// check an output drives an input
		boolean compatible = false;
		for(String output : sg1.getOutputs()){
			VarSet inputs = sg2.getInputs();
			if(inputs.contains(output)){
				compatible = true;
				break;
			}
		}
		
		if(!compatible){
			VarSet inputs = sg1.getInputs();
			for(String output : sg2.getOutputs()){
				if(inputs.contains(output)){
					compatible = true;
					break;
				}
			}
		}
		
		if(!compatible){
			System.out.println("state graphs " + sg1.getLabel() + " and " + sg2.getLabel() + " cannot be composed\n");
			return null;
		}
		
		// create new node with init states
		State[] initStates = new State[2];
		initStates[0] = sg1.getInitialState();
		initStates[1] = sg2.getInitialState();
		CompositeState initNode = new CompositeState(initStates);
		
		HashSet<LPNTran> synchronousTrans = new HashSet<LPNTran>();
		synchronousTrans.addAll(sg1.getInputTranSet());
		synchronousTrans.retainAll(sg2.getOutputTranSet());
		
		HashSet<LPNTran> temp = new HashSet<LPNTran>();
		temp.addAll(sg2.getInputTranSet());
		temp.retainAll(sg1.getOutputTranSet());
		synchronousTrans.addAll(temp);
		
		List<LPNTran> inputTrans1 = new ArrayList<LPNTran>();
		inputTrans1.addAll(sg1.getInputTranSet());
		inputTrans1.removeAll(synchronousTrans);
		
		List<LPNTran> inputTrans2 = new ArrayList<LPNTran>();
		inputTrans2.addAll(sg2.getInputTranSet());
		inputTrans2.removeAll(synchronousTrans);
		
		// create new composite state graph
		StateGraph[] sgArray = new StateGraph[2];
		sgArray[0] = sg1;
		sgArray[1] = sg2;
		CompositeStateGraph compositeSG = new CompositeStateGraph(initNode, sgArray);
		
		// create CompositeState stack
		Stack<CompositeState> compositeStateStack = new Stack<CompositeState>();
		
		// initialize with initial MDD node
		compositeStateStack.push(initNode);
		
		List<LPNTran> tranList1 = new ArrayList<LPNTran>();
		List<State> stateList1 = new ArrayList<State>();
		List<LPNTran> tranList2 = new ArrayList<LPNTran>();
		List<State> stateList2 = new ArrayList<State>();
		List<State> intersect1 = new ArrayList<State>();
		List<State> intersect2 = new ArrayList<State>();
		List<LPNTran> intersectTran = new ArrayList<LPNTran>();
		
		long peakUsed = 0;
		long peakTotal = 0;
		
		//while stack is not empty
		while(!compositeStateStack.isEmpty()){
			//pop stack
			CompositeState currentCompositeState = compositeStateStack.pop();
			
			State[] stateTuple = currentCompositeState.getStateTuple();
			State s1 = stateTuple[0];
			State s2 = stateTuple[1];
			
			// find next state transitions for each state
//			LPNTranSet enabled1 = sg1.getEnabled(s1);
//			LPNTranSet enabled2 = sg2.getEnabled(s2);
			List<LPNTran> enabled1 = sg1.lpnTransitionMap.get(s1);
			List<LPNTran> enabled2 = sg2.lpnTransitionMap.get(s2);
			
			tranList1.clear();
			stateList1.clear();
			tranList2.clear();
			stateList2.clear();
			intersect1.clear();
			intersect2.clear();
			intersectTran.clear();
			
			for(LPNTran lpnTran : enabled1){
				if(lpnTran.local()){
					tranList1.add(lpnTran);
					stateList1.add((State) lpnTran.getNextState(s1));
				}
				else{
					if(synchronousTrans.contains(lpnTran)){
//						State st = lpnTran.constraintTranMap.get(s2);
						State st = lpnTran.getNextState(s2);
						if(st != null){
							intersect1.add((State) lpnTran.getNextState(s1));
							intersect2.add(st);
							intersectTran.add(lpnTran);
						}
					}
					else{
						tranList1.add(lpnTran);
						stateList1.add((State) lpnTran.getNextState(s1));
					}
				}
			}
			
			
			for(LPNTran lpnTran : enabled2){
				if(lpnTran.local()){
					tranList2.add(lpnTran);
					stateList2.add((State) lpnTran.getNextState(s2));
				}
				else{
					if(synchronousTrans.contains(lpnTran)){
//						State st = lpnTran.constraintTranMap.get(s1);
						State st = lpnTran.getNextState(s1);
						if(st != null){
							intersect1.add(st);
							intersect2.add((State) lpnTran.getNextState(s2));
							intersectTran.add(lpnTran);
						}
					}
					else{
						tranList2.add(lpnTran);
						stateList2.add((State) lpnTran.getNextState(s2));
					}
				}
			}
			
			for(LPNTran lpnTran : inputTrans1){
//				State st = lpnTran.constraintTranMap.get(s1);
				State st = lpnTran.getNextState(s1);
				if(st != null){
					tranList1.add(lpnTran);
					stateList1.add(st);
				}
			}
			
			for(LPNTran lpnTran : inputTrans2){
//				State st = lpnTran.constraintTranMap.get(s2);
				State st = lpnTran.getNextState(s2);
				if(st != null){
					tranList2.add(lpnTran);
					stateList2.add(st);
				}
			}
			
//			int size = tranList1.size() + tranList2.size() + intersect1.size();
//			CompositeState[] nextStateArray = new CompositeState[size];
//			LPNTran[] tranArray = new LPNTran[size];
//			size--;
			
			// for each transition
			// create new MDD node and push onto stack
			for(int i = 0; i < tranList1.size(); i++){
				LPNTran lpnTran = tranList1.get(i);
				
				State nextState = stateList1.get(i);
				State[] newStateTuple = new State[2];
				newStateTuple[0] = nextState;
				newStateTuple[1] = s2;
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				CompositeState st = compositeSG.addState(newCompositeState);
				if(st == null){
					compositeStateStack.push(newCompositeState);
					st = newCompositeState;
				}
				
				// create a new CompositeStateTran
//				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, lpnTran);
//				if(compositeSG.addStateTran(newCompositeStateTran)){
					// add an edge between the current and new state
//					currentCompositeState.addEdge(newCompositeStateTran);
//				}
				
//				nextStateArray[size] = st;
//				tranArray[size] = lpnTran;
//				size--;
				
//				currentCompositeState.addNextState(st);
//				currentCompositeState.addTran(lpnTran);
				
				currentCompositeState.nextStateList.add(st);
				currentCompositeState.enabledTranList.add(lpnTran);
				st.incomingStateList.add(currentCompositeState);
			}
			
			for(int i = 0; i < tranList2.size(); i++){
				LPNTran lpnTran = tranList2.get(i);
				
				State nextState = stateList2.get(i);
				State[] newStateTuple = new State[2];
				newStateTuple[0] = s1;
				newStateTuple[1] = nextState;
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				CompositeState st = compositeSG.addState(newCompositeState);
				if(st == null){
					compositeStateStack.push(newCompositeState);
					st = newCompositeState;
				}
				
				// create new CompositeStateTran
//				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, lpnTran);
//				if(compositeSG.addStateTran(newCompositeStateTran)){
					// add an edge between the current and new state
//					currentCompositeState.addEdge(newCompositeStateTran);
//				}
				
//				nextStateArray[size] = st;
//				tranArray[size] = lpnTran;
//				size--;
				
//				currentCompositeState.addNextState(st);
//				currentCompositeState.addTran(lpnTran);
				
				currentCompositeState.nextStateList.add(st);
				currentCompositeState.enabledTranList.add(lpnTran);
				st.incomingStateList.add(currentCompositeState);
			}
			
			for(int i = 0; i < intersect1.size(); i++){
				LPNTran lpnTran = intersectTran.get(i);
				
				State nextState1 = intersect1.get(i);
				State nextState2 = intersect2.get(i);
				
				State[] newStateTuple = new State[2];
				newStateTuple[0] = nextState1;
				newStateTuple[1] = nextState2;
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				CompositeState st = compositeSG.addState(newCompositeState);
				if(st == null){
					compositeStateStack.push(newCompositeState);
					st = newCompositeState;
				}
				
				// create a new CompositeStateTran
//				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, lpnTran);
//				if(compositeSG.addStateTran(newCompositeStateTran)){
					// add an edge between the current and new state
//					currentCompositeState.addEdge(newCompositeStateTran);
//				}
				
//				nextStateArray[size] = st;
//				tranArray[size] = lpnTran;
//				size--;
				
//				currentCompositeState.addNextState(st);
//				currentCompositeState.addTran(lpnTran);
				
				currentCompositeState.nextStateList.add(st);
				currentCompositeState.enabledTranList.add(lpnTran);
				st.incomingStateList.add(currentCompositeState);
			}
			
//			currentCompositeState.setNextStateArray(nextStateArray);
//			currentCompositeState.setTranArray(tranArray);
			
			long curTotalMem = Runtime.getRuntime().totalMemory();
			if(curTotalMem > peakTotal)
				peakTotal = curTotalMem;
			
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if(curUsedMem > peakUsed)
				peakUsed = curUsedMem;
		}
		
		System.out.println("\n   " + compositeSG.getLabel() + ": ");
		System.out.println("   --> # states: " + compositeSG.numCompositeStates());
//		System.out.println("   --> # transitions: " + compositeSG.numCompositeStateTrans());

		System.out.println("   --> Peak used memory: " + peakUsed/1000000F + " MB");
		System.out.println("   --> Peak total memory: " + peakTotal/1000000F + " MB");
		System.out.println("   --> Final used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000F + " MB");
		
		long elapsedTimeMillis = System.currentTimeMillis()-start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
		
		if(elapsedTimeSec > 60){
			float elapsedTime = elapsedTimeSec;
			elapsedTime = elapsedTimeSec/(float)60;
			System.out.println("   --> Elapsed time: " + elapsedTime + " min");
		}
		
		System.out.println();

//		System.out.println();
//		for(CompositeState cState : compositeSG.compositeStateSet){
//			State[] stateTuple = cState.getStateTuple();
//			State s1 = stateTuple[0];
//			State s2 = stateTuple[1];
//			
//			System.out.println(s1.getLabel() + ", " + s2.getLabel());
//		}
		
		return compositeSG;
	}
	
	public CompositeStateGraph compose(CompositeStateGraph csg, StateGraph sg){
		if(csg == null || sg == null){
			return csg;
		}
		
		long start = System.currentTimeMillis(); 
		
		// check an output drives an input
		boolean compatible = false;
		for(String output : sg.getOutputs()){
			for(StateGraph sg2 : csg.stateGraphArray){
				VarSet inputs = sg2.getInputs();
				if(inputs.contains(output)){
					compatible = true;
					break;
				}
			}
			
			if(compatible){
				break;
			}
		}
		
		if(!compatible){
			VarSet inputs = sg.getInputs();
			for(StateGraph sg2 : csg.stateGraphArray){
				for(String output : sg2.getOutputs()){
					if(inputs.contains(output)){
						compatible = true;
						break;
					}
				}
				
				if(compatible){
					break;
				}
			}
			
		}
		
		if(!compatible){
			System.out.println("state graphs " + csg.getLabel() + " and " + sg.getLabel() + " cannot be composed");
			return null;
		}
		
		for(StateGraph sg2 : csg.stateGraphArray){
			if(sg2 == sg){
				return csg;
			}
		}
		
		// create new node with init states
		int size = csg.getSize() + 1;
		State[] initStates = new State[size];
		initStates[0] = sg.getInitialState();
		for(int i = 1; i < size; i++){
			initStates[i] = csg.stateGraphArray[i-1].getInitialState();
		}
		
		CompositeState initNode = new CompositeState(initStates);
		
		HashSet<LPNTran> synchronousTrans = new HashSet<LPNTran>();
		
		for(StateGraph sg2 : csg.stateGraphArray){
			HashSet<LPNTran> inputTrans = new HashSet<LPNTran>();
			inputTrans.addAll(sg.getInputTranSet());
			inputTrans.retainAll(sg2.getOutputTranSet());
			
			HashSet<LPNTran> outputTrans = new HashSet<LPNTran>();
			outputTrans.addAll(sg2.getInputTranSet());
			outputTrans.retainAll(sg.getOutputTranSet());
			
			synchronousTrans.addAll(inputTrans);
			synchronousTrans.addAll(outputTrans);
		}
		
		List<LPNTran> inputTrans = new ArrayList<LPNTran>();
		inputTrans.addAll(sg.getInputTranSet());
		inputTrans.removeAll(synchronousTrans);
		
		// create new composite state graph
		StateGraph[] sgArray = new StateGraph[size];
		sgArray[0] = sg;
		for(int i = 1; i < size; i++){
			sgArray[i] = csg.stateGraphArray[i-1];
		}
		
		CompositeStateGraph compositeSG = new CompositeStateGraph(initNode, sgArray);
		
		// create CompositeState stack
		Stack<State> stateStack = new Stack<State>();
		Stack<CompositeState> compositeStateStack = new Stack<CompositeState>();
		Stack<CompositeState> newStateStack = new Stack<CompositeState>();
		
//		Queue<State> stateQueue = new LinkedList<State>();
//		Queue<CompositeState> compositeStateQueue = new LinkedList<CompositeState>();
//		Queue<CompositeState> newStateQueue = new LinkedList<CompositeState>();
		
		// initialize with initial MDD node
		newStateStack.push(initNode);
		stateStack.push(sg.getInitialState());
		compositeStateStack.push(csg.getInitState());
		
//		stateQueue.offer(sg.init);
//		compositeStateQueue.offer(csg.getInitState());
//		newStateQueue.offer(initNode);
		
//		HashMap<LPNTran, StateTran> tranMap = new HashMap<LPNTran, StateTran>();
//		List<CompositeStateTran> csgStateTranList = new ArrayList<CompositeStateTran>();
//		List<StateTran> sgIntersect = new ArrayList<StateTran>();
//		List<CompositeStateTran> csgIntersect = new ArrayList<CompositeStateTran>();
		
		List<LPNTran> tranList1 = new ArrayList<LPNTran>();
		List<State> stateList1 = new ArrayList<State>();
		List<LPNTran> tranList2 = new ArrayList<LPNTran>();
		List<CompositeState> stateList2 = new ArrayList<CompositeState>();
		List<State> intersect1 = new ArrayList<State>();
		List<CompositeState> intersect2 = new ArrayList<CompositeState>();
		List<LPNTran> intersectTran = new ArrayList<LPNTran>();
		
		long peakUsed = 0;
		long peakTotal = 0;
		
		//while stack is not empty
		while(!newStateStack.isEmpty()){
//		while(!newStateQueue.isEmpty()){
			long s1 = System.currentTimeMillis(); 
			
			//pop stack
			CompositeState currentCompositeState = newStateStack.pop();
			State subState = stateStack.pop();
			CompositeState subCompositeState = compositeStateStack.pop();
			
//			CompositeState currentCompositeState = newStateQueue.poll();
//			State subState = stateQueue.poll();
//			CompositeState subCompositeState = compositeStateQueue.poll();
			
			State[] subCompositeTuple = subCompositeState.getStateTuple();
			
			tranList1.clear();
			stateList1.clear();
			tranList2.clear();
			stateList2.clear();
			intersect1.clear();
			intersect2.clear();
			intersectTran.clear();
			
			// find next state transitions for each state
			List<LPNTran> enabled1 = sg.lpnTransitionMap.get(subState);
//			List<LPNTran> enabled2 = subCompositeState.getTranList();
//			List<CompositeState> edgeList = subCompositeState.getNextStateList();
//			LPNTran[] enabled2 = subCompositeState.getTranArray();
//			CompositeState[] edgeList = subCompositeState.getNextStateArray();
			List<LPNTran> enabled2 = subCompositeState.enabledTranList;
			List<CompositeState> edgeList = subCompositeState.nextStateList;
			               
//			System.out.println("    enabled1: " + enabled1.size());
			for(LPNTran lpnTran : enabled1){
				if(lpnTran.local()){
					tranList1.add(lpnTran);
					stateList1.add((State) lpnTran.getNextState(subState));
				}
				else{
					if(synchronousTrans.contains(lpnTran)){
						boolean synch = false;
						CompositeState st = null;
						for(int i = 0; i < enabled2.size(); i++){
							if(enabled2.get(i) == lpnTran){
								synch = true;
								st = edgeList.get(i);
								break;
							}
						}
						
						if(synch){
							intersect1.add((State) lpnTran.getNextState(subState));
							intersect2.add(st);
							intersectTran.add(lpnTran);
						}
						else{
							System.out.println("ST == NULL1\n");
						}
					}
					else{
						tranList1.add(lpnTran);
						stateList1.add((State) lpnTran.getNextState(subState));
					}
				}
			}
			
//			System.out.println("    enabled2: " + enabled2.size());
			for(int i = 0; i < enabled2.size(); i++){
				LPNTran lpnTran = enabled2.get(i);
				
				if(synchronousTrans.contains(lpnTran)){
//					State st = lpnTran.constraintTranMap.get(subState);
					State st = lpnTran.getNextState(subState);
					if(st != null){
						intersectTran.add(lpnTran);
						intersect1.add(st);
						intersect2.add(edgeList.get(i));
					}
				}
				else{
					tranList2.add(lpnTran);
					stateList2.add(edgeList.get(i));
				}
			}
			
//			System.out.println("    inputTrans: " + inputTrans.size());
			for(LPNTran lpnTran : inputTrans){
//				State st = lpnTran.constraintTranMap.get(subState);
				State st = lpnTran.getNextState(subState);
				if(st != null){
					tranList1.add(lpnTran);
					stateList1.add(st);
				}
			}
			
//			int items = tranList1.size() + tranList2.size() + intersect1.size();
//			CompositeState[] nextStateArray = new CompositeState[items];
//			LPNTran[] tranArray = new LPNTran[items];
//			items--;
			
			long s2 = System.currentTimeMillis(); 
			// for each transition
			// create new MDD node and push onto stack
			for(int i = 0; i < tranList1.size(); i++){
				LPNTran lpnTran = tranList1.get(i);
				State nextState = stateList1.get(i);
				State[] newStateTuple = new State[size];
				newStateTuple[0] = nextState;
				for(int j = 1; j < size; j++){
					newStateTuple[j] = subCompositeTuple[j-1];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				CompositeState st = compositeSG.addState(newCompositeState);
				if(st == null){
					newStateStack.push(newCompositeState);
					stateStack.push(nextState);
					compositeStateStack.push(subCompositeState);
//					newStateQueue.offer(newCompositeState);
//					stateQueue.offer(nextState);
//					compositeStateQueue.offer(subCompositeState);
					
					st = newCompositeState;
				}
				
				// create a new CompositeStateTran
//				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, lpnTran);
//				if(compositeSG.addStateTran(newCompositeStateTran)){
					// add an edge between the current and new state
//				currentCompositeState.addEdge(newCompositeStateTran);
//				}
				
//				nextStateArray[items] = st;
//				tranArray[items] = lpnTran;
//				items--;
				
//				currentCompositeState.addNextState(st);
//				currentCompositeState.addTran(lpnTran);
				
				currentCompositeState.nextStateList.add(st);
				currentCompositeState.enabledTranList.add(lpnTran);
				st.incomingStateList.add(currentCompositeState);
			}
			
//			System.out.println("    transList2: " + tranList2.size());
			for(int i = 0; i < tranList2.size(); i++){
				LPNTran lpnTran = tranList2.get(i);
				CompositeState nextState = stateList2.get(i);
				State[] nextStateTuple = nextState.getStateTuple();
				State[] newStateTuple = new State[size];
				newStateTuple[0] = subState;
				for(int j = 1; j < size; j++){
					newStateTuple[j] = nextStateTuple[j-1];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				CompositeState st = compositeSG.addState(newCompositeState);
				if(st == null){
					newStateStack.push(newCompositeState);
					stateStack.push(subState);
					compositeStateStack.push(nextState);
//					newStateQueue.offer(newCompositeState);
//					stateQueue.offer(subState);
//					compositeStateQueue.offer(nextState);
					
					st = newCompositeState;
				}
				
				// create a new CompositeStateTran
//				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, lpnTran);
//				if(compositeSG.addStateTran(newCompositeStateTran)){
					// add an edge between the current and new state
//					currentCompositeState.addEdge(newCompositeStateTran);
//				}
				
//				nextStateArray[items] = st;
//				tranArray[items] = lpnTran;
//				items--;
				
//				currentCompositeState.addNextState(st);
//				currentCompositeState.addTran(lpnTran);
				
				currentCompositeState.nextStateList.add(st);
				currentCompositeState.enabledTranList.add(lpnTran);
				st.incomingStateList.add(currentCompositeState);
			}
			
//			System.out.println("    intersect: " + intersect1.size());
			for(int i = 0; i < intersect1.size(); i++){
				LPNTran lpnTran = intersectTran.get(i);
				
				State nextState1 = intersect1.get(i);
				CompositeState nextState2 = intersect2.get(i);
				State[] nextStateTuple = nextState2.getStateTuple();
				
				State[] newStateTuple = new State[size];
				newStateTuple[0] = nextState1;
				for(int j = 1; j < size; j++){
					newStateTuple[j] = nextStateTuple[j-1];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				CompositeState st = compositeSG.addState(newCompositeState);
				if(st == null){
					newStateStack.push(newCompositeState);
					compositeStateStack.push(nextState2);
					stateStack.push(nextState1);
//					newStateQueue.offer(newCompositeState);
//					stateQueue.offer(nextState1);
//					compositeStateQueue.offer(nextState2);
					
					st = newCompositeState;
				}
				
				// create a new CompositeStateTran
//				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, stTran1.lpnTran);
//				if(compositeSG.addStateTran(newCompositeStateTran)){
					// add an edge between the current and new state
//					currentCompositeState.addEdge(newCompositeStateTran);
//				}
				
//				nextStateArray[items] = st;
//				tranArray[items] = lpnTran;
//				items--;
				
//				currentCompositeState.addNextState(st);
//				currentCompositeState.addTran(lpnTran);
				
				currentCompositeState.nextStateList.add(st);
				currentCompositeState.enabledTranList.add(lpnTran);
				st.incomingStateList.add(currentCompositeState);
			}
			
//			currentCompositeState.setNextStateArray(nextStateArray);
//			currentCompositeState.setTranArray(tranArray);
			
			long curTotalMem = Runtime.getRuntime().totalMemory();
			if(curTotalMem > peakTotal)
				peakTotal = curTotalMem;
			
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if(curUsedMem > peakUsed)
				peakUsed = curUsedMem;
		}
		
		System.out.println("\n   " + compositeSG.getLabel() + ": ");
		System.out.println("   --> # states: " + compositeSG.numCompositeStates());
//		System.out.println("   --> # transitions: " + compositeSG.numCompositeStateTrans());
		
		System.out.println("   --> Peak used memory: " + peakUsed/1000000F + " MB");
		System.out.println("   --> Peak total memory: " + peakTotal/1000000F + " MB");
		System.out.println("   --> Final used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000F + " MB");
		
		long elapsedTimeMillis = System.currentTimeMillis()-start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
		
		if(elapsedTimeSec > 60){
			float elapsedTime = elapsedTimeSec;
			elapsedTime = elapsedTimeSec/(float)60;
			System.out.println("   --> Elapsed time: " + elapsedTime + " min");
		}
		
		System.out.println();
		
//		System.out.println();
//		for(CompositeState cState : compositeSG.compositeStateSet){
//			State[] stateTuple = cState.getStateTuple();
//			State s1 = stateTuple[0];
//			State s2 = stateTuple[1];
//			
//			System.out.println(s1.getLabel() + ", " + s2.getLabel());
//		}
		
		return compositeSG;
	}
	
	public CompositeStateGraph compose(CompositeStateGraph sg1, CompositeStateGraph sg2){
		long start = System.currentTimeMillis(); 
		
		if(sg1 == null || sg2 == null){
			return null;
		}
		
		// check an output drives an input
		boolean compatible = false;
		for(StateGraph g : sg1.stateGraphArray){
			for(String output : g.getOutputs()){
				for(StateGraph g2 : sg2.stateGraphArray){
					VarSet inputs = g2.getInputs();
					if(inputs.contains(output)){
						compatible = true;
						break;
					}
				}
				
				if(compatible){
					break;
				}
			}
			
			if(compatible){
				break;
			}
		}
		
		if(!compatible){
			for(StateGraph g1 : sg1.stateGraphArray){
				VarSet inputs = g1.getInputs();
				for(StateGraph g2 : sg2.stateGraphArray){
					for(String output : g2.getOutputs()){
						if(inputs.contains(output)){
							compatible = true;
							break;
						}
					}
					
					if(compatible){
						break;
					}
				}
				
				if(compatible){
					break;
				}
			}
		}
		
		if(!compatible){
			System.out.println("state graphs " + sg1.getLabel() + " and " + sg2.getLabel() + " cannot be composed\n");
			return null;
		}
		
		HashSet<StateGraph> sgSet = new HashSet<StateGraph>();
		for(StateGraph sg : sg1.stateGraphArray){
			if(sgSet.add(sg) == false){
				System.out.println("state graphs " + sg1.getLabel() + " and " + sg2.getLabel() + " cannot be composed\n");
				return null;
			}
		}
		
		for(StateGraph sg : sg2.stateGraphArray){
			if(sgSet.add(sg) == false){
				System.out.println("state graphs " + sg1.getLabel() + " and " + sg2.getLabel() + " cannot be composed\n");
				return null;
			}
		}
		
		// create new node with init states
		int size = sg1.getSize() + sg2.getSize();
		int sg1Size = sg1.getSize();
		State[] initStates = new State[size];
		for(int i = 0; i < sg1Size; i++){
			initStates[i] = sg1.stateGraphArray[i].getInitialState();
		}
		
		for(int i = sg1Size; i < size; i++){
			initStates[i] = sg2.stateGraphArray[i-sg1Size].getInitialState();
		}
		
		CompositeState initNode = new CompositeState(initStates);
		
		HashSet<LPNTran> synchronousTrans = new HashSet<LPNTran>();
		for(StateGraph g1 :sg1.stateGraphArray){
			for(StateGraph g2 : sg2.stateGraphArray){
				HashSet<LPNTran> inputTrans = new HashSet<LPNTran>();
				inputTrans.addAll(g1.getInputTranSet());
				inputTrans.retainAll(g2.getOutputTranSet());
				
				HashSet<LPNTran> outputTrans = new HashSet<LPNTran>();
				outputTrans.addAll(g2.getInputTranSet());
				outputTrans.retainAll(g1.getOutputTranSet());
				
				synchronousTrans.addAll(inputTrans);
				synchronousTrans.addAll(outputTrans);
			}
		}
		
		// create new composite state graph
		StateGraph[] sgArray = new StateGraph[size];
		for(int i = 0; i < sg1Size; i++){
			sgArray[i] = sg1.stateGraphArray[i];
		}
		
		for(int i = sg1Size; i < size; i++){
			sgArray[i] = sg2.stateGraphArray[i-sg1Size];
		}
		
		CompositeStateGraph compositeSG = new CompositeStateGraph(initNode, sgArray);
	
		// create CompositeState stack
		Stack<CompositeState> newStateStack = new Stack<CompositeState>();
		Stack<CompositeState> subStateStack1 = new Stack<CompositeState>();
		Stack<CompositeState> subStateStack2 = new Stack<CompositeState>();
		
		// initialize with initial CompositionalState
		newStateStack.push(initNode);
		subStateStack1.push(sg1.getInitState());
		subStateStack2.push(sg2.getInitState());
		
		List<LPNTran> intersectingTrans = new ArrayList<LPNTran>();
		List<CompositeState> intStateList1 = new ArrayList<CompositeState>();
		List<CompositeState> intStateList2 = new ArrayList<CompositeState>();
		List<LPNTran> independentTrans1 = new ArrayList<LPNTran>();
		List<CompositeState> indStateList1 = new ArrayList<CompositeState>();
		List<LPNTran> independentTrans2 = new ArrayList<LPNTran>();
		List<CompositeState> indStateList2 = new ArrayList<CompositeState>();
		
		long peakUsed = 0;
		long peakTotal = 0;
		
		CompositeState tempState = null;
		while(!newStateStack.isEmpty()){
			CompositeState currentState = newStateStack.pop();
			CompositeState subState1 = subStateStack1.pop();
			CompositeState subState2 = subStateStack2.pop();
			
			State[] subState1Tuple = subState1.getStateTuple();
			State[] subState2Tuple = subState2.getStateTuple();
			
			List<LPNTran> enabledTrans1 = subState1.enabledTranList;
			List<CompositeState> nextStates1 = subState1.nextStateList;
			List<LPNTran> enabledTrans2 = subState2.enabledTranList;
			List<CompositeState> nextStates2 = subState2.nextStateList;
			
			intersectingTrans.clear();
			intStateList1.clear();
			intStateList2.clear();
			independentTrans1.clear();
			indStateList1.clear();
			independentTrans2.clear();
			indStateList2.clear();
			
			for(int i = 0; i < enabledTrans1.size(); i++){
				LPNTran lpnTran = enabledTrans1.get(i);
				if(lpnTran.local()){
					independentTrans1.add(lpnTran);
					indStateList1.add(nextStates1.get(i));
				}
				else{
					if(synchronousTrans.contains(lpnTran)){
						for(int j = 0; j < enabledTrans2.size(); j++){
							if(lpnTran == enabledTrans2.get(j)){
								if(synchronousTrans.contains(lpnTran)){
									intersectingTrans.add(lpnTran);
									intStateList1.add(nextStates1.get(i));
									intStateList2.add(nextStates2.get(j));
								}
								
								break;
							}
						}
					}
					else{
						independentTrans1.add(lpnTran);
						indStateList1.add(nextStates1.get(i));
					}
				}
			}
			
			for(int i = 0; i < enabledTrans2.size(); i++){
				LPNTran lpnTran = enabledTrans2.get(i);
				if(lpnTran.local()){
					independentTrans2.add(lpnTran);
					indStateList2.add(nextStates2.get(i));
				}
				else{
					if(!synchronousTrans.contains(lpnTran)){
						independentTrans2.add(lpnTran);
						indStateList2.add(nextStates2.get(i));
					}
				}
			}

			for(int i = 0; i < independentTrans1.size(); i++){
				LPNTran lpnTran = independentTrans1.get(i);
				CompositeState nextState = indStateList1.get(i);
				State[] nextStateTuple = nextState.getStateTuple();
				State[] newStateTuple = new State[size];
				
				for(int j = 0; j < sg1Size; j++){
					newStateTuple[j] = nextStateTuple[j];
				}
				
				for(int j = sg1Size; j < size; j++){
					newStateTuple[j] = subState2Tuple[j-sg1Size];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				tempState = compositeSG.addState(newCompositeState);
				if(tempState == null){
					newStateStack.push(newCompositeState);
					subStateStack1.push(nextState);
					subStateStack2.push(subState2);
				}
				else{
					newCompositeState = tempState;
				}
				
				currentState.nextStateList.add(newCompositeState);
				currentState.enabledTranList.add(lpnTran);
				newCompositeState.incomingStateList.add(currentState);
				compositeSG.numTransitions++;
			}
			
			for(int j = 0; j < independentTrans2.size(); j++){
				LPNTran lpnTran = independentTrans2.get(j);
				CompositeState nextState = indStateList2.get(j);
				State[] nextStateTuple = nextState.getStateTuple();
				State[] newStateTuple = new State[size];
				
				for(int i = 0; i < sg1Size; i++){
					newStateTuple[i] = subState1Tuple[i];
				}
				
				for(int i = sg1Size; i < size; i++){
					newStateTuple[i] = nextStateTuple[i-sg1Size];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				tempState = compositeSG.addState(newCompositeState);
				if(tempState == null){
					newStateStack.push(newCompositeState);
					subStateStack1.push(subState1);
					subStateStack2.push(nextState);
				}
				else{
					newCompositeState = tempState;
				}
				
				currentState.nextStateList.add(newCompositeState);
				currentState.enabledTranList.add(lpnTran);
				newCompositeState.incomingStateList.add(currentState);
				compositeSG.numTransitions++;
			}
			
			for(int j = 0; j < intersectingTrans.size(); j++){
				LPNTran lpnTran = intersectingTrans.get(j);
				CompositeState nextState1 = intStateList1.get(j);
				State[] nextState1Tuple = nextState1.getStateTuple();
				CompositeState nextState2 = intStateList2.get(j);
				State[] nextState2Tuple = nextState2.getStateTuple();
				State[] newStateTuple = new State[size];
				
				for(int i = 0; i < sg1Size; i++){
					newStateTuple[i] = nextState1Tuple[i];
				}
				
				for(int i = sg1Size; i < size; i++){
					newStateTuple[i] = nextState2Tuple[i-sg1Size];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				tempState = compositeSG.addState(newCompositeState);
				if(tempState == null){
					newStateStack.push(newCompositeState);
					subStateStack1.push(nextState1);
					subStateStack2.push(nextState2);
				}
				else{
					newCompositeState = tempState;
				}
				
				currentState.nextStateList.add(newCompositeState);
				currentState.enabledTranList.add(lpnTran);
				newCompositeState.incomingStateList.add(currentState);
				compositeSG.numTransitions++;
			}
			
			long curTotalMem = Runtime.getRuntime().totalMemory();
			if(curTotalMem > peakTotal)
				peakTotal = curTotalMem;
			
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if(curUsedMem > peakUsed)
				peakUsed = curUsedMem;

		}
		
		System.out.println("\n   " + compositeSG.getLabel() + ": ");
		System.out.println("   --> # states: " + compositeSG.numCompositeStates());
//		System.out.println("   --> # transitions: " + compositeSG.numCompositeStateTrans());

		System.out.println("   --> Peak used memory: " + peakUsed/1000000F + " MB");
		System.out.println("   --> Peak total memory: " + peakTotal/1000000F + " MB");
		System.out.println("   --> Final used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000F + " MB");
		
		long elapsedTimeMillis = System.currentTimeMillis()-start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
		
		if(elapsedTimeSec > 60){
			float elapsedTime = elapsedTimeSec;
			elapsedTime = elapsedTimeSec/(float)60;
			System.out.println("   --> Elapsed time: " + elapsedTime + " min");
		}
		
		System.out.println();
		
		return compositeSG;
	}
	
	public void compositionalAnalsysis(List<StateGraph> designUnitSet) {
		System.out.println("\n****** Compositional Analysis ******");
		long start = System.currentTimeMillis(); 
		
		if(Options.getParallelFlag()){
			parallelCompositionalFindSG(designUnitSet);
		}
		else{
			compositionalFindSG(designUnitSet);
		}
		
		findReducedSG(designUnitSet);
		
		long totalMillis = System.currentTimeMillis()-start; 
		float totalSec = totalMillis/1000F;
		System.out.println("\n***** Total Elapsed Time: " + totalSec + " sec *****");
		
		if(totalSec > 60){
			float totalTime = totalSec/(float)60;
			System.out.println("***** Total Elapsed Time: " + totalTime + " min *****");
		}
		
		System.out.println();
	}
	
	public void findReducedSG(List<StateGraph> designUnitSet) {
		List<CompositeStateGraph> sgList = new ArrayList<CompositeStateGraph>();
		System.out.println();
		for(StateGraph sg : designUnitSet){
			CompositeStateGraph csg = new CompositeStateGraph(sg);
			
			if(Options.getCompositionalMinimization().equals("reduction")){
				reduce(csg);
			}
			
			if(Options.getCompositionalMinimization().equals("abstraction")){
				System.out.println("transitionBasedAbstraction");
				transitionBasedAbstraction(csg);
				System.out.println("redundantStateRemoval\n");
				redundantStateRemoval(csg);
			}
			
			sgList.add(csg);
		}
		
		CompositeStateGraph sg1 = sgList.get(0);
		sgList.remove(0);
		CompositeStateGraph csg = null;
		for(CompositeStateGraph sg2 : sgList){
			csg = compose(sg1, sg2);
			if(csg != null){
				if(Options.getCompositionalMinimization().equals("reduction")){
					reduce(csg);
				}
				
				if(Options.getCompositionalMinimization().equals("abstraction")){
					System.out.println("transitionBasedAbstraction");
					transitionBasedAbstraction(csg);
					System.out.println("redundantStateRemoval\n");
					redundantStateRemoval(csg);
				}
				
				sgList.remove(sg2);
				break;
			}
		}
		
		while(!sgList.isEmpty()){
			CompositeStateGraph tmpSG = null;
			for(CompositeStateGraph sg2 : sgList){
				tmpSG = compose(csg, sg2);
				if(tmpSG != null){
					sgList.remove(sg2);

					if(Options.getCompositionalMinimization().equals("reduction")){
						reduce(csg);
					}
					
					if(Options.getCompositionalMinimization().equals("abstraction")){
						System.out.println("transitionBasedAbstraction");
						transitionBasedAbstraction(csg);
						System.out.println("redundantStateRemoval\n");
						redundantStateRemoval(csg);
					}
					
					break;
				}
			}
			
			csg = tmpSG;
		}
		
		System.out.println(csg.numCompositeStates());
//		csg.draw();
	}
	
	public void reduce(CompositeStateGraph sg){
		long startTime = System.currentTimeMillis();
		int initNumTrans = sg.numTransitions;
		int initNumStates = sg.compositeStateSet.size();
		int totalReducedTrans = sg.numTransitions;
		
		int case2StateMin = 0;
		int case3StateMin = 0;
		int case2TranMin = 0;
		int case3TranMin = 0;
		
		int iter = 0;
		while(totalReducedTrans > 0){
			totalReducedTrans = 0;
			
			int numStates = sg.compositeStateSet.size();
			int numTrans = sg.numTransitions;
			int tranDiff = 0;
			
			case2(sg);
			
			case2TranMin += numTrans - sg.numTransitions;
			tranDiff = numStates - sg.compositeStateSet.size();
			case2StateMin += tranDiff;
			totalReducedTrans += tranDiff;
			
			numTrans = sg.numTransitions;
			numStates = sg.compositeStateSet.size();
			
			case3(sg);
			
			case3TranMin += numTrans - sg.numTransitions;
			tranDiff = numStates - sg.compositeStateSet.size();
			case3StateMin += tranDiff;
			totalReducedTrans += tranDiff;
			
			iter++;
		}
		
		System.out.println("   Reduce " + sg.getLabel() + ": ");
		System.out.println("   --> case2: -" + case2StateMin + " states");
		System.out.println("   --> case2: -" + case2TranMin + " transitions");
		System.out.println("   --> case3: -" + case3StateMin + " states");
		System.out.println("   --> case3: -" + case3TranMin + " transitions");
		System.out.println("   --> # states: " + sg.compositeStateSet.size());
		System.out.println("   --> # transitions: " + sg.numTransitions);
		System.out.println("   --> # iterations: " + iter);
		long elapsedTimeMillis = System.currentTimeMillis()-startTime; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec\n");
	}
	
	public void transitionBasedAbstraction(CompositeStateGraph sg){
		HashSet<CompositeState> visitedStates = new HashSet<CompositeState>();
		Stack<CompositeState> stateStack = new Stack<CompositeState>();
		CompositeState init = sg.getInitState();
		
		int abstractCount = 1;
		while(abstractCount > 0){
			abstractCount = 0;
			
			visitedStates.clear();
			stateStack.clear();
			stateStack.push(init);
			visitedStates.add(init);
		
			while(!stateStack.isEmpty()){
				CompositeState currentState = stateStack.pop();
				int enabledSize = currentState.enabledTranList.size();
				Object[] enabled = currentState.enabledTranList.toArray();
				Object[] nextStateArray = currentState.nextStateList.toArray();
				
				for(int i = 0; i < enabledSize; i++){
					LPNTran invisibleTran = (LPNTran) enabled[i];
					CompositeState nextState = (CompositeState) nextStateArray[i];
					if(nextState == sg.getInitState()) continue;
					
					if(!invisibleTran.local()){
						if(!visitedStates.contains(nextState)){
							stateStack.push(nextState);
							visitedStates.add(nextState);
						}
						
						continue;
					}
					
					abstractCount++;
					
					// remove invisible transition
					currentState.nextStateList.remove(nextState);
					currentState.enabledTranList.remove(invisibleTran);
					nextState.incomingStateList.remove(currentState);
					sg.numTransitions--;
					
					if(nextState.incomingStateList.isEmpty()){
						sg.compositeStateSet.remove(nextState);
						sg.numTransitions -= nextState.nextStateList.size();
					}
					else if(!visitedStates.contains(nextState)){
						stateStack.push(nextState);
						visitedStates.add(nextState);
					}
					
					
					int nextEnabledSize = nextState.enabledTranList.size();
					Object[] nextEnabled = nextState.enabledTranList.toArray();
					Object[] nextNextStateArray = nextState.nextStateList.toArray();
					
					// add next state transitions to current state
					for(int j = 0; j < nextEnabledSize; j++){
						LPNTran lpnTran = (LPNTran) nextEnabled[j];
						CompositeState st = (CompositeState) nextNextStateArray[j];
						
						boolean add = true;
						for(int k = 0; k < currentState.nextStateList.size(); k++){
							if(currentState.nextStateList.get(k) == st && currentState.enabledTranList.get(k) == lpnTran){
								add = false;
								break;
							}
						}
						
						if(add){
							currentState.nextStateList.add(st);
							currentState.enabledTranList.add(lpnTran);
							st.incomingStateList.add(currentState);
							sg.numTransitions++;
						}
					}
				}
			}
		}
	}
	
	public void redundantStateRemoval(CompositeStateGraph sg){
		HashSet<CompositeStatePair> equivalentPairSet = findInitialEquivalentPairs(sg);
		
		// remove states that are not equivalent
		for(Object o : equivalentPairSet.toArray()){
			CompositeStatePair eqPair = (CompositeStatePair) o;
			CompositeState state1 = eqPair.getState1();
			CompositeState state2 = eqPair.getState2();
			
			List<CompositeState> nextStateList1 = state1.nextStateList;
			List<CompositeState> nextStateList2 = state2.nextStateList;
			
			boolean eq = true;
			for(CompositeState succ1 : nextStateList1){
				boolean succEq = false;
				for(CompositeState succ2 : nextStateList2){
					if(succ1 == succ2){
						succEq = true;
						continue;
					}
					
					CompositeStatePair statePair = new CompositeStatePair(succ1, succ2);
					if(equivalentPairSet.contains(statePair)){
						succEq = true;
						continue;
					}
				}
				
				if(!succEq){
					eq = false;
					break;
				}
			}
			
			if(!eq){
				equivalentPairSet.remove(eqPair);
			}
		}
	}
	
	private HashSet<CompositeStatePair> findInitialEquivalentPairs(CompositeStateGraph sg){
		HashSet<CompositeStatePair> equivalentSet = new HashSet<CompositeStatePair>();
		
		for(CompositeState state1 : sg.getStateSet()){
			for(CompositeState state2 : sg.getStateSet()){
				if(state1 == state2) continue;
				
				List<LPNTran> enabled1 = state1.enabledTranList;
				List<LPNTran> enabled2 = state2.enabledTranList;
				
				if(enabled1.size() == enabled2.size() && enabled1.containsAll(enabled2)){
					equivalentSet.add(new CompositeStatePair(state1, state2));
				}
			}
		}
		
		return equivalentSet;
	}
	
	public void case2(CompositeStateGraph sg){
		long start = System.currentTimeMillis();
		int trans = sg.numTransitions;
		int states = sg.numCompositeStates();
		
		List<LPNTran> localTrans = new ArrayList<LPNTran>();
		List<CompositeState> localStates = new ArrayList<CompositeState>();
		List<LPNTran> nonLocalTrans = new ArrayList<LPNTran>();
		List<CompositeState> nonLocalStates = new ArrayList<CompositeState>();
		
		for(Object o : sg.getStateSet().toArray()){
			CompositeState currentState = (CompositeState) o;
			
			List<LPNTran> enabledTrans = currentState.enabledTranList;
			List<CompositeState> nextStates = currentState.nextStateList;
			
			localTrans.clear();
			localStates.clear();
			nonLocalTrans.clear();
			nonLocalStates.clear();
			
			for(int i = 0; i < enabledTrans.size(); i++){
				LPNTran lpnTran = enabledTrans.get(i);
				CompositeState nextState = (nextStates.get(i));
				if(lpnTran.local()){
					localTrans.add(lpnTran);
					localStates.add(nextState);
				}
//				else{
					nonLocalTrans.add(lpnTran);
					nonLocalStates.add(nextState);
//				}
			}
			
			if(nonLocalTrans.isEmpty()){
				continue;
			}
			
			for(int i = 0; i < nonLocalTrans.size(); i++){
				LPNTran nonLocalTran = nonLocalTrans.get(i);
				CompositeState s2 = nonLocalStates.get(i);
				
				List<LPNTran> s2Enabled = s2.enabledTranList;
				if(s2Enabled.size() > 1 || s2Enabled.size() < 1){
					continue;
				}
//				else if(s2 == sg.getInitState()){
//					continue;
//				}
				
				List<CompositeState> s2NextState = s2.nextStateList;
				for(int j = 0; j < s2Enabled.size(); j++){
					LPNTran invTran2 = s2Enabled.get(j);
					if(invTran2.local()){
						CompositeState s4 = s2NextState.get(j);
						
						for(int k = 0; k < localTrans.size(); k++){
							LPNTran invTran = localTrans.get(k);
							if(invTran == nonLocalTran) continue;
							
							CompositeState s3 = localStates.get(k);
							
							List<LPNTran> s3Enabled = s3.enabledTranList;
							List<CompositeState> s3NextState = s3.nextStateList;
							for(int n = 0; n < s3Enabled.size(); n++){
								LPNTran nonLocalTran2 = s3Enabled.get(n);
								CompositeState nextState = s3NextState.get(n);
								
								if(nonLocalTran2 == nonLocalTran && nextState == s4){
									currentState.enabledTranList.remove(nonLocalTran);
									currentState.nextStateList.remove(s2);
									sg.numTransitions --;
									
									List<CompositeState> incomingStates = s2.incomingStateList;
									for(int m = 0; m < incomingStates.size(); m++){
										CompositeState curr = incomingStates.get(m);
										List<CompositeState> incomingNextStateList = curr.nextStateList;
										
										for(int idx = 0; idx < incomingNextStateList.size(); idx++){
											CompositeState tmpState = incomingNextStateList.get(idx);
											if(tmpState == s2){
												incomingNextStateList.set(idx, s4);
												s4.incomingStateList.add(curr);
												break;
											}
										}
									}
									
									s2.nextStateList.clear();
									s2.incomingStateList.clear();
									s2.enabledTranList.clear();
									
									sg.compositeStateSet.remove(s2);
									if(sg.getInitState() == s2){
										sg.setInitState(s4);
									}
									sg.numTransitions --;
								}
							}
						}
					}
				}
			}
		}
		
//		System.out.println(sg.getLabel() + " case2 transitions: " + trans + " - " + sg.numTransitions);
//		System.out.println(sg.getLabel() + " case2 states: " + states + " - " + sg.numCompositeStates());
//		long elapsedTimeMillis = System.currentTimeMillis()-start; 
//		float elapsedTimeSec = elapsedTimeMillis/1000F;
//		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
	}
	
	public void case3(CompositeStateGraph sg){
		long start = System.currentTimeMillis();
		int trans = sg.numTransitions;
		int states = sg.numCompositeStates();
		
		List<LPNTran> localTrans = new ArrayList<LPNTran>();
		List<CompositeState> localStates = new ArrayList<CompositeState>();
		List<LPNTran> nonLocalTrans = new ArrayList<LPNTran>();
		List<CompositeState> nonLocalStates = new ArrayList<CompositeState>();
		
		for(Object o : sg.getStateSet().toArray()){
			CompositeState currentState = (CompositeState) o;
			
			List<LPNTran> enabledTrans = currentState.enabledTranList;
			List<CompositeState> nextStates = currentState.nextStateList;
			
			localTrans.clear();
			localStates.clear();
			nonLocalTrans.clear();
			nonLocalStates.clear();
			
			for(int i = 0; i < enabledTrans.size(); i++){
				LPNTran lpnTran = enabledTrans.get(i);
				CompositeState nextState = (nextStates.get(i));
				if(lpnTran.local()){
					localTrans.add(lpnTran);
					localStates.add(nextState);
				}
//				else{
					nonLocalTrans.add(lpnTran);
					nonLocalStates.add(nextState);
//				}
			}
			
			if(nonLocalTrans.isEmpty()){
				continue;
			}
			
			for(int i = 0; i < localTrans.size(); i++){
				LPNTran localTran = localTrans.get(i);
				CompositeState s3 = localStates.get(i);
				if(s3.incomingStateList.size() != 1){
					continue;
				}
//				else if(s3 == sg.getInitState()){
//					continue;
//				}
				
				List<LPNTran> s3Enabled = s3.enabledTranList;
				List<CompositeState> s3NextState = s3.nextStateList;
				
				boolean remove = false;
				List<LPNTran> removeTran = new ArrayList<LPNTran>();
				
				for(int j = 0; j < s3Enabled.size(); j++){
					LPNTran nonLocalTran2 = s3Enabled.get(j);
					if(!nonLocalTran2.local()){
						CompositeState s4 = s3NextState.get(j);
						
						for(int k = 0; k < nonLocalTrans.size(); k++){
							LPNTran nonLocalTran = nonLocalTrans.get(k);
							if(localTran == nonLocalTran) continue;
							
							CompositeState s2 = nonLocalStates.get(k);
							
							List<LPNTran> s2Enabled = s2.enabledTranList;
							List<CompositeState> s2NextState = s2.nextStateList;
							for(int n = 0; n < s2Enabled.size(); n++){
								LPNTran invTran2 = s2Enabled.get(n);
								if(invTran2.local()){
									CompositeState nextState = s2NextState.get(n);
									
									if(nonLocalTran2 == nonLocalTran && nextState == s4){
										removeTran.add(nonLocalTran2);
										remove = true;
									}
								}
							}
						}
					}
				}
				
				if(remove){
					currentState.enabledTranList.remove(localTran);
					currentState.nextStateList.remove(s3);
					sg.numTransitions--;
					
					for(int m = 0; m < s3Enabled.size(); m++){
						CompositeState currState = s3NextState.get(m);
						LPNTran currTran = s3Enabled.get(m);
						
						if(removeTran.contains(currTran)){
							removeTran.remove(currTran);
							sg.numTransitions--;
							continue;
						}
						
						currentState.enabledTranList.add(currTran);
						currentState.nextStateList.add(currState);
						
						List<CompositeState> currIncomingList = currState.incomingStateList;
						for(int idx = 0; idx < currIncomingList.size(); idx++){
							if(currIncomingList.get(idx) == s3){
								currIncomingList.set(idx, currState);
							}
						}
					}
					
					sg.compositeStateSet.remove(s3);
					if(sg.getInitState() == s3){
						sg.setInitState(currentState);
					}
					
					s3.nextStateList.clear();
					s3.enabledTranList.clear();
					s3.incomingStateList.clear();
				}
			}
		}
		
//		System.out.println(sg.getLabel() + " case3 transitions: " + trans + " - " + sg.numTransitions);
//		System.out.println(sg.getLabel() + " case3 states: " + states + " - " + sg.numCompositeStates());
//		long elapsedTimeMillis = System.currentTimeMillis()-start; 
//		float elapsedTimeSec = elapsedTimeMillis/1000F;
//		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
	}
	
	/**
     * Constructs the compositional state graphs.
     */
	public void compositionalFindSG(List<StateGraph> designUnitSet){
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//		try {
//			br.readLine();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		int iter = 0;
		int newTransitions = 0;
		long start = System.currentTimeMillis();
		HashMap<StateGraph, List<StateGraph>> inputSrcMap = new HashMap<StateGraph, List<StateGraph>>();
		
		for (StateGraph sg : designUnitSet) {
            // Add initial state to state graph
			State init = sg.getInitStateUntimed();
			sg.setInitialState(init);
			sg.addReachable(init);
            
			VarSet inputSet = sg.getInputs();
			VarSet outputSet = sg.getOutputs();
			int numSrc = 0;

			// Find lpn interfaces
			for(StateGraph sg2 : designUnitSet){				
				if(sg == sg2) continue;
				
				VarSet outputs = sg2.getOutputs();
				for(String output : outputs){
					if (inputSet.contains(output)){
						numSrc++;
						break;
					}
				}

				for(String output : outputs){
					if (outputSet.contains(output)){
						numSrc++;
						break;
					}
				}
			}
			
			List<int[]> thisInterfaceList = new ArrayList<int[]>(designUnitSet.size());
			List<int[]> otherInterfaceList = new ArrayList<int[]>(designUnitSet.size());
			for(int i = 0; i < designUnitSet.size() + 1; i++){
				thisInterfaceList.add(null);
				otherInterfaceList.add(null);
			}
			
			List<StateGraph> srcArray = new ArrayList<StateGraph>(numSrc);
			
			if(numSrc > 0){
				int index = 0;
				for(StateGraph sg2 : designUnitSet){				
					if(sg == sg2) continue;
					
					int interfaceSize = 0;
					VarSet outputs = sg2.getOutputs();
					VarSet inputs = sg2.getInputs();
					
					for(String output : outputs){
						if (inputSet.contains(output)){
							interfaceSize++;
						}
					}
					
					for(String input : inputs){
						if(outputSet.contains(input)){
							interfaceSize++;
						}
					}
					
					for(String output : outputs){
						if (outputSet.contains(output)){
							interfaceSize++;
						}
					}
					
					for(String input : inputs){
						if (inputSet.contains(input)){
							interfaceSize++;
						}
					}

					if(interfaceSize > 0){
						int[] thisIndexList = new int[interfaceSize];
						int[] otherIndexList = new int[interfaceSize];
						sg.genIndexLists(thisIndexList, otherIndexList, sg2);
						
						thisInterfaceList.set(sg2.ID-1, thisIndexList);
						otherInterfaceList.set(sg2.ID-1, otherIndexList);
						srcArray.add(sg2);
						index++;
					}
				}
			}
			
			sg.setThisIndexList(thisInterfaceList);
			sg.setOtherIndexList(otherInterfaceList);
			inputSrcMap.put(sg, srcArray);
		}
		
		LPN[] lpnList = new LPN[designUnitSet.size()];

		int idx = 0;
		for (LPN lpn : designUnitSet) {
			lpnList[idx] = lpn;
			idx++;
		}	
		
		List<LPNTran> emptyTranList = new ArrayList<LPNTran>(0);
		
		// Run initial findSG
		for (StateGraph sg : designUnitSet) {
			int result = 0;
			if(Options.getStickySemantics()){
				result = sg.constrStickyFindSG(sg.getInitialState(), emptyTranList);
			}
			else{
				result = sg.constrFindSG(sg.getInitialState());
			}
			
			newTransitions += result;
		}

		long peakUsed = 0;
		long peakTotal = 0;

		List<Constraint> newConstraintSet = new ArrayList<Constraint>();
		List<Constraint> oldConstraintSet = new ArrayList<Constraint>();
		while(newTransitions > 0){
			iter++;
			newTransitions = 0;
			
			for(StateGraph sg : designUnitSet){
				sg.genConstraints();
				sg.genFrontier();
			}

			for(StateGraph sg : designUnitSet){
				for(StateGraph srcSG : inputSrcMap.get(sg)){
					extractConstraints(sg, srcSG, newConstraintSet, oldConstraintSet);
					newTransitions += applyConstraintSet(sg, srcSG, iter, newConstraintSet, oldConstraintSet);
					
				}
			}

			long curTotalMem = Runtime.getRuntime().totalMemory();
			if(curTotalMem > peakTotal)
				peakTotal = curTotalMem;
			
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if(curUsedMem > peakUsed)
				peakUsed = curUsedMem;
		}
		
		System.out.println();
		
		int numStates = 0;
		int numTrans = 0;
		int numConstr = 0;
		for (StateGraph sg : designUnitSet) {
			System.out.print("   ");
			sg.printStates();
			
			numStates += sg.reachSize();
			//numTrans += sg.numTransitions();
//			numConstr += sg.constraintSetSize();
		}
		
		System.out.println("\n   --> # states: " + numStates);
		System.out.println("   --> # transitions: " + numTrans);
		System.out.println("   --> # constraints: " + numConstr);
		System.out.println("   --> # iterations: " + iter);
		
		System.out.println("\n   --> Peak used memory: " + peakUsed/1000000F + " MB");
		System.out.println("   --> Peak total memory: " + peakTotal/1000000F + " MB");
		System.out.println("   --> Final used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000F + " MB");
		
		long elapsedTimeMillis = System.currentTimeMillis()-start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
		
		if(elapsedTimeSec > 60){
			float elapsedTime = elapsedTimeSec/(float)60;
			System.out.println("   --> Elapsed time: " + elapsedTime + " min");
		}	
	}
	
	/**
     * Constructs the compositional state graphs.
     */
	public void parallelCompositionalFindSG(List<StateGraph> designUnitSet){
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//		try {
//			br.readLine();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		int iter = 0;
		int newTransitions = 0;
		long start = System.currentTimeMillis();
		HashMap<StateGraph, StateGraph[]> inputSrcMap = new HashMap<StateGraph, StateGraph[]>();
		
		for (StateGraph sg : designUnitSet) {
            // Add initial state to state graph
			State init = sg.getInitStateUntimed();
			sg.setInitialState(init);
			sg.addReachable(init);
            
			VarSet inputSet = sg.getInputs();
			VarSet outputSet = sg.getOutputs();
			int size = 0;
			
			// Find lpn interfaces
			for(StateGraph sg2 : designUnitSet){				
				if(sg == sg2) continue;
				
				VarSet outputs = sg2.getOutputs();
				for(String output : outputs){
					if (inputSet.contains(output)){
						size++;
						break;
					}
				}
			}

			List<int[]> thisInterfaceList = new ArrayList<int[]>(designUnitSet.size() + 1);
			List<int[]> otherInterfaceList = new ArrayList<int[]>(designUnitSet.size() + 1);
			for(int i = 0; i < designUnitSet.size() + 1; i++){
				thisInterfaceList.add(null);
				otherInterfaceList.add(null);
			}
			
			StateGraph[] srcArray = new StateGraph[size];
			
			if(size > 0){
				int index = 0;
				for(StateGraph sg2 : designUnitSet){				
					if(sg == sg2) continue;
					
					boolean src = false;
					int interfaceSize = 0;
					VarSet outputs = sg2.getOutputs();
					for(String output : outputs){
						if (inputSet.contains(output)){
							interfaceSize++;
							src = true;
						}
					}

					if(src){
						VarSet inputs = sg2.getInputs();
						for(String input : inputs){
							if (outputSet.contains(input)){
								interfaceSize++;
							}
						}
						
						for(String input : inputs){
							if (inputSet.contains(input)){
								interfaceSize++;
							}
						}
						
						for(String output : outputs){
							if (outputSet.contains(output)){
								interfaceSize++;
							}
						}
						
						int[] thisIndexList = new int[interfaceSize];
						int[] otherIndexList = new int[interfaceSize];
						sg.genIndexLists(thisIndexList, otherIndexList, sg2);
						
						thisInterfaceList.set(sg2.ID, thisIndexList);
						otherInterfaceList.set(sg2.ID, otherIndexList);
						srcArray[index] = sg2;
						index++;
					}
				}
			}
			
			sg.setThisIndexList(thisInterfaceList);
			sg.setOtherIndexList(otherInterfaceList);
			inputSrcMap.put(sg, srcArray);
		}
		
		LPN[] lpnList = new LPN[designUnitSet.size()];

		int idx = 0;
		for (LPN lpn : designUnitSet) {
			lpnList[idx] = lpn;
			idx++;
		}	
		
		// Run initial findSG
		for (StateGraph sg : designUnitSet) {
			int result = sg.constrFindSG(sg.getInitialState());
			newTransitions += result;
		}

		long peakUsed = 0;
		long peakTotal = 0;

		CompositionalThread[] threadArray = new CompositionalThread[designUnitSet.size()];
		while(newTransitions > 0){
			iter++;
			newTransitions = 0;
			
			for(StateGraph sg : designUnitSet){
				sg.genConstraints();
				sg.genFrontier();
			}
			
			int t = 0;
			for(StateGraph sg : designUnitSet){
				CompositionalThread newThread = new CompositionalThread(sg, inputSrcMap.get(sg), iter);
			    newThread.start();
			    threadArray[t++] = newThread;
			}

			for(CompositionalThread p : threadArray){
				try {
					p.join();
					newTransitions += p.getNewTransitions();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			long curTotalMem = Runtime.getRuntime().totalMemory();
			if(curTotalMem > peakTotal)
				peakTotal = curTotalMem;
			
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if(curUsedMem > peakUsed)
				peakUsed = curUsedMem;
		}
		
		System.out.println();
		
		int numStates = 0;
		int numTrans = 0;
		int numConstr = 0;
		for (StateGraph sg : designUnitSet) {
			System.out.print("   ");
			sg.printStates();
			
			numStates += sg.reachSize();
			//numTrans += sg.numTransitions();
//			numConstr += sg.constraintSetSize();
		}
		
		System.out.println("\n   --> # states: " + numStates);
		System.out.println("   --> # transitions: " + numTrans);
		System.out.println("   --> # constraints: " + numConstr);
		System.out.println("   --> # iterations: " + iter);
		
		System.out.println("\n   --> Peak used memory: " + peakUsed/1000000F + " MB");
		System.out.println("   --> Peak total memory: " + peakTotal/1000000F + " MB");
		System.out.println("   --> Final used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000F + " MB");
		
		long elapsedTimeMillis = System.currentTimeMillis()-start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
		
		if(elapsedTimeSec > 60){
			float elapsedTime = elapsedTimeSec/(float)60;
			System.out.println("   --> Elapsed time: " + elapsedTime + " min");
		}	
	}
	
	/**
	 * Applies new constraints to the entire state set, and applies old constraints to the frontier state set.
     * @return Number of new transitions.
     */
	private int applyConstraintSet(StateGraph sg, StateGraph srcSG, int iter, List<Constraint> newConstraintSet, List<Constraint> oldConstraintSet){
		int newTransitions = 0;

		int[] thisIndexList = sg.getThisIndexArray(srcSG.ID - 1);
		int[] otherIndexList = sg.getOtherIndexArray(srcSG.ID - 1);
		
		if(newConstraintSet.size() > 0){
			for(Object obj : sg.getStateSet().toArray()){
				State currentState = (State) obj;

				for(Constraint c : newConstraintSet){
					if(compatible(currentState, c, thisIndexList, otherIndexList)){
						newTransitions += createNewState(sg, currentState, c);
					}
				}
			}

			for(State currentState : sg.getFrontierStateSet()){
				for(Constraint c : newConstraintSet){
					if(compatible(currentState, c, thisIndexList, otherIndexList)){						
						newTransitions += createNewState(sg, currentState, c);
					}
				}
			}
		}
		
		if(oldConstraintSet.size() > 0){
			for(State currentState : sg.getFrontierStateSet()){
				for(Constraint c : oldConstraintSet){
					if(compatible(currentState, c, thisIndexList, otherIndexList)){						
						newTransitions += createNewState(sg, currentState, c);
					}
				}
			}
		}
		
		return newTransitions;
	}
	
	/**
     * Extracts applicable constraints from a StateGraph.
     * @param sg The state graph the constraints are to be applied.
     * @param srcSG The state graph the constraint are extracted from.
     */
	private void extractConstraints(StateGraph sg, StateGraph srcSG, List<Constraint> newConstraintSet, List<Constraint> oldConstraintSet){
		newConstraintSet.clear();
		oldConstraintSet.clear();
		
		LPN srcLpn = (LPN) srcSG;
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
	private boolean compatible(State currentState, Constraint constr, int[] thisIndexList, int[] otherIndexList){
		int[] constraintVector = constr.getVector();
		int[] currentVector = currentState.getVector();
		
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
	private int createNewState(StateGraph sg, State compatibleState, Constraint c){
		int newTransitions = 0;

		// Create new state and insert into state graph
		State newState = new State(compatibleState);
		int[] newVector = newState.getVector();
		
		List<VarNode> variableList = c.getVariableList();
		List<Integer> valueList = c.getValueList();
		
		for(int i = 0; i < variableList.size(); i++){
			int index = variableList.get(i).getIndex(compatibleState);
			newVector[index] = valueList.get(i);
		}
		
		State nextState = sg.addReachable(newState);
		if(nextState == null){
			nextState = newState;
            
			int result = 0;
			
			if(Options.getStickySemantics()){
				result = sg.constrStickyFindSG(nextState, sg.getEnabled(compatibleState));
			}
			else{
				result = sg.constrFindSG(nextState);
			}
			
			if(result < 0) return newTransitions;
			
			newTransitions += result;
		}

//    	StateTran stTran = new StateTran(compatibleState, constraintTran, state);

		LPNTran constraintTran = c.getLpnTransition();
		constraintTran.addStateTran(compatibleState, nextState);
    	sg.lpnTransitionMap.get(compatibleState).add(constraintTran);
    	newTransitions++;
		
		return newTransitions;
	}
}
