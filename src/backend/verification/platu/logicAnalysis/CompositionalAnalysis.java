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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import backend.verification.platu.common.Pair;
import backend.verification.platu.main.Options;
import backend.verification.platu.platuLpn.VarSet;
import backend.verification.platu.stategraph.State;
import backend.verification.platu.stategraph.StateGraph;
import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Transition;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class CompositionalAnalysis {  
	public CompositionalAnalysis(){
	}
	
//	public CompositeStateGraph compose(StateGraph sg1, StateGraph sg2){
//		long start = System.currentTimeMillis(); 
//		
//		// check an output drives an input
//		boolean compatible = false;
//		for(String output : sg1.getOutputs()){
//			VarSet inputs = sg2.getInputs();
//			if(inputs.contains(output)){
//				compatible = true;
//				break;
//			}
//		}
//		
//		if(!compatible){
//			VarSet inputs = sg1.getInputs();
//			for(String output : sg2.getOutputs()){
//				if(inputs.contains(output)){
//					compatible = true;
//					break;
//				}
//			}
//		}
//		
//		if(!compatible){
//			System.out.println("state graphs " + sg1.getLabel() + " and " + sg2.getLabel() + " cannot be composed\n");
//			return null;
//		}
//		
//		// create new node with init states
//		State[] initStates = new State[2];
//		initStates[0] = sg1.getInitialState();
//		initStates[1] = sg2.getInitialState();
//		CompositeState initNode = new CompositeState(initStates);
//		
//		HashSet<LPNTran> synchronousTrans = new HashSet<LPNTran>();
//		synchronousTrans.addAll(sg1.getInputTranSet());
//		synchronousTrans.retainAll(sg2.getOutputTranSet());
//		
//		HashSet<LPNTran> temp = new HashSet<LPNTran>();
//		temp.addAll(sg2.getInputTranSet());
//		temp.retainAll(sg1.getOutputTranSet());
//		synchronousTrans.addAll(temp);
//		
//		List<LPNTran> inputTrans1 = new ArrayList<LPNTran>();
//		inputTrans1.addAll(sg1.getInputTranSet());
//		inputTrans1.removeAll(synchronousTrans);
//		
//		List<LPNTran> inputTrans2 = new ArrayList<LPNTran>();
//		inputTrans2.addAll(sg2.getInputTranSet());
//		inputTrans2.removeAll(synchronousTrans);
//		
//		// create new composite state graph
//		StateGraph[] sgArray = new StateGraph[2];
//		sgArray[0] = sg1;
//		sgArray[1] = sg2;
//		CompositeStateGraph compositeSG = new CompositeStateGraph(initNode, sgArray);
//		
//		// create CompositeState stack
//		Stack<CompositeState> compositeStateStack = new Stack<CompositeState>();
//		
//		// initialize with initial MDD node
//		compositeStateStack.push(initNode);
//		
//		List<LPNTran> tranList1 = new ArrayList<LPNTran>();
//		List<State> stateList1 = new ArrayList<State>();
//		List<LPNTran> tranList2 = new ArrayList<LPNTran>();
//		List<State> stateList2 = new ArrayList<State>();
//		List<State> intersect1 = new ArrayList<State>();
//		List<State> intersect2 = new ArrayList<State>();
//		List<LPNTran> intersectTran = new ArrayList<LPNTran>();
//		
//		long peakUsed = 0;
//		long peakTotal = 0;
//		
//		//while stack is not empty
//		while(!compositeStateStack.isEmpty()){
//			//pop stack
//			CompositeState currentCompositeState = compositeStateStack.pop();
//			
//			State[] stateTuple = currentCompositeState.getStateTuple();
//			State s1 = stateTuple[0];
//			State s2 = stateTuple[1];
//			
//			// find next state transitions for each state
////			LPNTranSet enabled1 = sg1.getEnabled(s1);
////			LPNTranSet enabled2 = sg2.getEnabled(s2);
//			List<LPNTran> enabled1 = sg1.lpnTransitionMap.get(s1);
//			List<LPNTran> enabled2 = sg2.lpnTransitionMap.get(s2);
//			
//			tranList1.clear();
//			stateList1.clear();
//			tranList2.clear();
//			stateList2.clear();
//			intersect1.clear();
//			intersect2.clear();
//			intersectTran.clear();
//			
//			for(LPNTran lpnTran : enabled1){
//				if(lpnTran.local()){
//					tranList1.add(lpnTran);
//					stateList1.add((State) lpnTran.getNextState(s1));
//				}
//				else{
//					if(synchronousTrans.contains(lpnTran)){
////						State st = lpnTran.constraintTranMap.get(s2);
//						State st = lpnTran.getNextState(s2);
//						if(st != null){
//							intersect1.add((State) lpnTran.getNextState(s1));
//							intersect2.add(st);
//							intersectTran.add(lpnTran);
//						}
//					}
//					else{
//						tranList1.add(lpnTran);
//						stateList1.add((State) lpnTran.getNextState(s1));
//					}
//				}
//			}
//			
//			
//			for(LPNTran lpnTran : enabled2){
//				if(lpnTran.local()){
//					tranList2.add(lpnTran);
//					stateList2.add((State) lpnTran.getNextState(s2));
//				}
//				else{
//					if(synchronousTrans.contains(lpnTran)){
////						State st = lpnTran.constraintTranMap.get(s1);
//						State st = lpnTran.getNextState(s1);
//						if(st != null){
//							intersect1.add(st);
//							intersect2.add((State) lpnTran.getNextState(s2));
//							intersectTran.add(lpnTran);
//						}
//					}
//					else{
//						tranList2.add(lpnTran);
//						stateList2.add((State) lpnTran.getNextState(s2));
//					}
//				}
//			}
//			
//			for(LPNTran lpnTran : inputTrans1){
////				State st = lpnTran.constraintTranMap.get(s1);
//				State st = lpnTran.getNextState(s1);
//				if(st != null){
//					tranList1.add(lpnTran);
//					stateList1.add(st);
//				}
//			}
//			
//			for(LPNTran lpnTran : inputTrans2){
////				State st = lpnTran.constraintTranMap.get(s2);
//				State st = lpnTran.getNextState(s2);
//				if(st != null){
//					tranList2.add(lpnTran);
//					stateList2.add(st);
//				}
//			}
//			
////			int size = tranList1.size() + tranList2.size() + intersect1.size();
////			CompositeState[] nextStateArray = new CompositeState[size];
////			LPNTran[] tranArray = new LPNTran[size];
////			size--;
//			
//			// for each transition
//			// create new MDD node and push onto stack
//			for(int i = 0; i < tranList1.size(); i++){
//				LPNTran lpnTran = tranList1.get(i);
//				
//				State nextState = stateList1.get(i);
//				State[] newStateTuple = new State[2];
//				newStateTuple[0] = nextState;
//				newStateTuple[1] = s2;
//				
//				CompositeState newCompositeState = new CompositeState(newStateTuple);
//				CompositeState st = compositeSG.addState(newCompositeState);
//				if(st == null){
//					compositeStateStack.push(newCompositeState);
//					st = newCompositeState;
//				}
//				
//				// create a new CompositeStateTran
////				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, lpnTran);
////				if(compositeSG.addStateTran(newCompositeStateTran)){
//					// add an edge between the current and new state
////					currentCompositeState.addEdge(newCompositeStateTran);
////				}
//				
////				nextStateArray[size] = st;
////				tranArray[size] = lpnTran;
////				size--;
//				
////				currentCompositeState.addNextState(st);
////				currentCompositeState.addTran(lpnTran);
//				
//				currentCompositeState.nextStateList.add(st);
//				currentCompositeState.enabledTranList.add(lpnTran);
//				st.incomingStateList.add(currentCompositeState);
//			}
//			
//			for(int i = 0; i < tranList2.size(); i++){
//				LPNTran lpnTran = tranList2.get(i);
//				
//				State nextState = stateList2.get(i);
//				State[] newStateTuple = new State[2];
//				newStateTuple[0] = s1;
//				newStateTuple[1] = nextState;
//				
//				CompositeState newCompositeState = new CompositeState(newStateTuple);
//				CompositeState st = compositeSG.addState(newCompositeState);
//				if(st == null){
//					compositeStateStack.push(newCompositeState);
//					st = newCompositeState;
//				}
//				
//				// create new CompositeStateTran
////				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, lpnTran);
////				if(compositeSG.addStateTran(newCompositeStateTran)){
//					// add an edge between the current and new state
////					currentCompositeState.addEdge(newCompositeStateTran);
////				}
//				
////				nextStateArray[size] = st;
////				tranArray[size] = lpnTran;
////				size--;
//				
////				currentCompositeState.addNextState(st);
////				currentCompositeState.addTran(lpnTran);
//				
//				currentCompositeState.nextStateList.add(st);
//				currentCompositeState.enabledTranList.add(lpnTran);
//				st.incomingStateList.add(currentCompositeState);
//			}
//			
//			for(int i = 0; i < intersect1.size(); i++){
//				LPNTran lpnTran = intersectTran.get(i);
//				
//				State nextState1 = intersect1.get(i);
//				State nextState2 = intersect2.get(i);
//				
//				State[] newStateTuple = new State[2];
//				newStateTuple[0] = nextState1;
//				newStateTuple[1] = nextState2;
//				
//				CompositeState newCompositeState = new CompositeState(newStateTuple);
//				CompositeState st = compositeSG.addState(newCompositeState);
//				if(st == null){
//					compositeStateStack.push(newCompositeState);
//					st = newCompositeState;
//				}
//				
//				// create a new CompositeStateTran
////				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, lpnTran);
////				if(compositeSG.addStateTran(newCompositeStateTran)){
//					// add an edge between the current and new state
////					currentCompositeState.addEdge(newCompositeStateTran);
////				}
//				
////				nextStateArray[size] = st;
////				tranArray[size] = lpnTran;
////				size--;
//				
////				currentCompositeState.addNextState(st);
////				currentCompositeState.addTran(lpnTran);
//				
//				currentCompositeState.nextStateList.add(st);
//				currentCompositeState.enabledTranList.add(lpnTran);
//				st.incomingStateList.add(currentCompositeState);
//			}
//			
////			currentCompositeState.setNextStateArray(nextStateArray);
////			currentCompositeState.setTranArray(tranArray);
//			
//			long curTotalMem = Runtime.getRuntime().totalMemory();
//			if(curTotalMem > peakTotal)
//				peakTotal = curTotalMem;
//			
//			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//			if(curUsedMem > peakUsed)
//				peakUsed = curUsedMem;
//		}
//		
//		System.out.println("\n   " + compositeSG.getLabel() + ": ");
//		System.out.println("   --> # states: " + compositeSG.numCompositeStates());
////		System.out.println("   --> # transitions: " + compositeSG.numCompositeStateTrans());
//
//		System.out.println("   --> Peak used memory: " + peakUsed/1000000F + " MB");
//		System.out.println("   --> Peak total memory: " + peakTotal/1000000F + " MB");
//		System.out.println("   --> Final used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000F + " MB");
//		
//		long elapsedTimeMillis = System.currentTimeMillis()-start; 
//		float elapsedTimeSec = elapsedTimeMillis/1000F;
//		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
//		
//		if(elapsedTimeSec > 60){
//			float elapsedTime = elapsedTimeSec;
//			elapsedTime = elapsedTimeSec/(float)60;
//			System.out.println("   --> Elapsed time: " + elapsedTime + " min");
//		}
//		
//		System.out.println();
//
////		System.out.println();
////		for(CompositeState cState : compositeSG.compositeStateMap){
////			State[] stateTuple = cState.getStateTuple();
////			State s1 = stateTuple[0];
////			State s2 = stateTuple[1];
////			
////			System.out.println(s1.getLabel() + ", " + s2.getLabel());
////		}
//		
//		return compositeSG;
//	}
//	
//	public CompositeStateGraph compose(CompositeStateGraph csg, StateGraph sg){
//		if(csg == null || sg == null){
//			return csg;
//		}
//		
//		long start = System.currentTimeMillis(); 
//		
//		// check an output drives an input
//		boolean compatible = false;
//		for(String output : sg.getOutputs()){
//			for(StateGraph sg2 : csg.stateGraphArray){
//				VarSet inputs = sg2.getInputs();
//				if(inputs.contains(output)){
//					compatible = true;
//					break;
//				}
//			}
//			
//			if(compatible){
//				break;
//			}
//		}
//		
//		if(!compatible){
//			VarSet inputs = sg.getInputs();
//			for(StateGraph sg2 : csg.stateGraphArray){
//				for(String output : sg2.getOutputs()){
//					if(inputs.contains(output)){
//						compatible = true;
//						break;
//					}
//				}
//				
//				if(compatible){
//					break;
//				}
//			}
//			
//		}
//		
//		if(!compatible){
//			System.out.println("state graphs " + csg.getLabel() + " and " + sg.getLabel() + " cannot be composed");
//			return null;
//		}
//		
//		for(StateGraph sg2 : csg.stateGraphArray){
//			if(sg2 == sg){
//				return csg;
//			}
//		}
//		
//		// create new node with init states
//		int size = csg.getSize() + 1;
//		State[] initStates = new State[size];
//		initStates[0] = sg.getInitialState();
//		for(int i = 1; i < size; i++){
//			initStates[i] = csg.stateGraphArray[i-1].getInitialState();
//		}
//		
//		CompositeState initNode = new CompositeState(initStates);
//		
//		HashSet<LPNTran> synchronousTrans = new HashSet<LPNTran>();
//		
//		for(StateGraph sg2 : csg.stateGraphArray){
//			HashSet<LPNTran> inputTrans = new HashSet<LPNTran>();
//			inputTrans.addAll(sg.getInputTranSet());
//			inputTrans.retainAll(sg2.getOutputTranSet());
//			
//			HashSet<LPNTran> outputTrans = new HashSet<LPNTran>();
//			outputTrans.addAll(sg2.getInputTranSet());
//			outputTrans.retainAll(sg.getOutputTranSet());
//			
//			synchronousTrans.addAll(inputTrans);
//			synchronousTrans.addAll(outputTrans);
//		}
//		
//		List<LPNTran> inputTrans = new ArrayList<LPNTran>();
//		inputTrans.addAll(sg.getInputTranSet());
//		inputTrans.removeAll(synchronousTrans);
//		
//		// create new composite state graph
//		StateGraph[] sgArray = new StateGraph[size];
//		sgArray[0] = sg;
//		for(int i = 1; i < size; i++){
//			sgArray[i] = csg.stateGraphArray[i-1];
//		}
//		
//		CompositeStateGraph compositeSG = new CompositeStateGraph(initNode, sgArray);
//		
//		// create CompositeState stack
//		Stack<State> stateStack = new Stack<State>();
//		Stack<CompositeState> compositeStateStack = new Stack<CompositeState>();
//		Stack<CompositeState> newStateStack = new Stack<CompositeState>();
//		
////		Queue<State> stateQueue = new LinkedList<State>();
////		Queue<CompositeState> compositeStateQueue = new LinkedList<CompositeState>();
////		Queue<CompositeState> newStateQueue = new LinkedList<CompositeState>();
//		
//		// initialize with initial MDD node
//		newStateStack.push(initNode);
//		stateStack.push(sg.getInitialState());
//		compositeStateStack.push(csg.getInitState());
//		
////		stateQueue.offer(sg.init);
////		compositeStateQueue.offer(csg.getInitState());
////		newStateQueue.offer(initNode);
//		
////		HashMap<LPNTran, StateTran> tranMap = new HashMap<LPNTran, StateTran>();
////		List<CompositeStateTran> csgStateTranList = new ArrayList<CompositeStateTran>();
////		List<StateTran> sgIntersect = new ArrayList<StateTran>();
////		List<CompositeStateTran> csgIntersect = new ArrayList<CompositeStateTran>();
//		
//		List<LPNTran> tranList1 = new ArrayList<LPNTran>();
//		List<State> stateList1 = new ArrayList<State>();
//		List<LPNTran> tranList2 = new ArrayList<LPNTran>();
//		List<CompositeState> stateList2 = new ArrayList<CompositeState>();
//		List<State> intersect1 = new ArrayList<State>();
//		List<CompositeState> intersect2 = new ArrayList<CompositeState>();
//		List<LPNTran> intersectTran = new ArrayList<LPNTran>();
//		
//		long peakUsed = 0;
//		long peakTotal = 0;
//		
//		//while stack is not empty
//		while(!newStateStack.isEmpty()){
////		while(!newStateQueue.isEmpty()){
//			long s1 = System.currentTimeMillis(); 
//			
//			//pop stack
//			CompositeState currentCompositeState = newStateStack.pop();
//			State subState = stateStack.pop();
//			CompositeState subCompositeState = compositeStateStack.pop();
//			
////			CompositeState currentCompositeState = newStateQueue.poll();
////			State subState = stateQueue.poll();
////			CompositeState subCompositeState = compositeStateQueue.poll();
//			
//			State[] subCompositeTuple = subCompositeState.getStateTuple();
//			
//			tranList1.clear();
//			stateList1.clear();
//			tranList2.clear();
//			stateList2.clear();
//			intersect1.clear();
//			intersect2.clear();
//			intersectTran.clear();
//			
//			// find next state transitions for each state
//			List<LPNTran> enabled1 = sg.lpnTransitionMap.get(subState);
////			List<LPNTran> enabled2 = subCompositeState.getTranList();
////			List<CompositeState> edgeList = subCompositeState.getNextStateList();
////			LPNTran[] enabled2 = subCompositeState.getTranArray();
////			CompositeState[] edgeList = subCompositeState.getNextStateArray();
//			List<LPNTran> enabled2 = subCompositeState.enabledTranList;
//			List<CompositeState> edgeList = subCompositeState.nextStateList;
//			               
////			System.out.println("    enabled1: " + enabled1.size());
//			for(LPNTran lpnTran : enabled1){
//				if(lpnTran.local()){
//					tranList1.add(lpnTran);
//					stateList1.add((State) lpnTran.getNextState(subState));
//				}
//				else{
//					if(synchronousTrans.contains(lpnTran)){
//						boolean synch = false;
//						CompositeState st = null;
//						for(int i = 0; i < enabled2.size(); i++){
//							if(enabled2.get(i) == lpnTran){
//								synch = true;
//								st = edgeList.get(i);
//								break;
//							}
//						}
//						
//						if(synch){
//							intersect1.add((State) lpnTran.getNextState(subState));
//							intersect2.add(st);
//							intersectTran.add(lpnTran);
//						}
//						else{
//							System.out.println("ST == NULL1\n");
//						}
//					}
//					else{
//						tranList1.add(lpnTran);
//						stateList1.add((State) lpnTran.getNextState(subState));
//					}
//				}
//			}
//			
////			System.out.println("    enabled2: " + enabled2.size());
//			for(int i = 0; i < enabled2.size(); i++){
//				LPNTran lpnTran = enabled2.get(i);
//				
//				if(synchronousTrans.contains(lpnTran)){
////					State st = lpnTran.constraintTranMap.get(subState);
//					State st = lpnTran.getNextState(subState);
//					if(st != null){
//						intersectTran.add(lpnTran);
//						intersect1.add(st);
//						intersect2.add(edgeList.get(i));
//					}
//				}
//				else{
//					tranList2.add(lpnTran);
//					stateList2.add(edgeList.get(i));
//				}
//			}
//			
////			System.out.println("    inputTrans: " + inputTrans.size());
//			for(LPNTran lpnTran : inputTrans){
////				State st = lpnTran.constraintTranMap.get(subState);
//				State st = lpnTran.getNextState(subState);
//				if(st != null){
//					tranList1.add(lpnTran);
//					stateList1.add(st);
//				}
//			}
//			
////			int items = tranList1.size() + tranList2.size() + intersect1.size();
////			CompositeState[] nextStateArray = new CompositeState[items];
////			LPNTran[] tranArray = new LPNTran[items];
////			items--;
//			
//			long s2 = System.currentTimeMillis(); 
//			// for each transition
//			// create new MDD node and push onto stack
//			for(int i = 0; i < tranList1.size(); i++){
//				LPNTran lpnTran = tranList1.get(i);
//				State nextState = stateList1.get(i);
//				State[] newStateTuple = new State[size];
//				newStateTuple[0] = nextState;
//				for(int j = 1; j < size; j++){
//					newStateTuple[j] = subCompositeTuple[j-1];
//				}
//				
//				CompositeState newCompositeState = new CompositeState(newStateTuple);
//				CompositeState st = compositeSG.addState(newCompositeState);
//				if(st == null){
//					newStateStack.push(newCompositeState);
//					stateStack.push(nextState);
//					compositeStateStack.push(subCompositeState);
////					newStateQueue.offer(newCompositeState);
////					stateQueue.offer(nextState);
////					compositeStateQueue.offer(subCompositeState);
//					
//					st = newCompositeState;
//				}
//				
//				// create a new CompositeStateTran
////				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, lpnTran);
////				if(compositeSG.addStateTran(newCompositeStateTran)){
//					// add an edge between the current and new state
////				currentCompositeState.addEdge(newCompositeStateTran);
////				}
//				
////				nextStateArray[items] = st;
////				tranArray[items] = lpnTran;
////				items--;
//				
////				currentCompositeState.addNextState(st);
////				currentCompositeState.addTran(lpnTran);
//				
//				currentCompositeState.nextStateList.add(st);
//				currentCompositeState.enabledTranList.add(lpnTran);
//				st.incomingStateList.add(currentCompositeState);
//			}
//			
////			System.out.println("    transList2: " + tranList2.size());
//			for(int i = 0; i < tranList2.size(); i++){
//				LPNTran lpnTran = tranList2.get(i);
//				CompositeState nextState = stateList2.get(i);
//				State[] nextStateTuple = nextState.getStateTuple();
//				State[] newStateTuple = new State[size];
//				newStateTuple[0] = subState;
//				for(int j = 1; j < size; j++){
//					newStateTuple[j] = nextStateTuple[j-1];
//				}
//				
//				CompositeState newCompositeState = new CompositeState(newStateTuple);
//				CompositeState st = compositeSG.addState(newCompositeState);
//				if(st == null){
//					newStateStack.push(newCompositeState);
//					stateStack.push(subState);
//					compositeStateStack.push(nextState);
////					newStateQueue.offer(newCompositeState);
////					stateQueue.offer(subState);
////					compositeStateQueue.offer(nextState);
//					
//					st = newCompositeState;
//				}
//				
//				// create a new CompositeStateTran
////				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, lpnTran);
////				if(compositeSG.addStateTran(newCompositeStateTran)){
//					// add an edge between the current and new state
////					currentCompositeState.addEdge(newCompositeStateTran);
////				}
//				
////				nextStateArray[items] = st;
////				tranArray[items] = lpnTran;
////				items--;
//				
////				currentCompositeState.addNextState(st);
////				currentCompositeState.addTran(lpnTran);
//				
//				currentCompositeState.nextStateList.add(st);
//				currentCompositeState.enabledTranList.add(lpnTran);
//				st.incomingStateList.add(currentCompositeState);
//			}
//			
////			System.out.println("    intersect: " + intersect1.size());
//			for(int i = 0; i < intersect1.size(); i++){
//				LPNTran lpnTran = intersectTran.get(i);
//				
//				State nextState1 = intersect1.get(i);
//				CompositeState nextState2 = intersect2.get(i);
//				State[] nextStateTuple = nextState2.getStateTuple();
//				
//				State[] newStateTuple = new State[size];
//				newStateTuple[0] = nextState1;
//				for(int j = 1; j < size; j++){
//					newStateTuple[j] = nextStateTuple[j-1];
//				}
//				
//				CompositeState newCompositeState = new CompositeState(newStateTuple);
//				CompositeState st = compositeSG.addState(newCompositeState);
//				if(st == null){
//					newStateStack.push(newCompositeState);
//					compositeStateStack.push(nextState2);
//					stateStack.push(nextState1);
////					newStateQueue.offer(newCompositeState);
////					stateQueue.offer(nextState1);
////					compositeStateQueue.offer(nextState2);
//					
//					st = newCompositeState;
//				}
//				
//				// create a new CompositeStateTran
////				CompositeStateTran newCompositeStateTran = new CompositeStateTran(currentCompositeState, st, stTran1.lpnTran);
////				if(compositeSG.addStateTran(newCompositeStateTran)){
//					// add an edge between the current and new state
////					currentCompositeState.addEdge(newCompositeStateTran);
////				}
//				
////				nextStateArray[items] = st;
////				tranArray[items] = lpnTran;
////				items--;
//				
////				currentCompositeState.addNextState(st);
////				currentCompositeState.addTran(lpnTran);
//				
//				currentCompositeState.nextStateList.add(st);
//				currentCompositeState.enabledTranList.add(lpnTran);
//				st.incomingStateList.add(currentCompositeState);
//			}
//			
////			currentCompositeState.setNextStateArray(nextStateArray);
////			currentCompositeState.setTranArray(tranArray);
//			
//			long curTotalMem = Runtime.getRuntime().totalMemory();
//			if(curTotalMem > peakTotal)
//				peakTotal = curTotalMem;
//			
//			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//			if(curUsedMem > peakUsed)
//				peakUsed = curUsedMem;
//		}
//		
//		System.out.println("\n   " + compositeSG.getLabel() + ": ");
//		System.out.println("   --> # states: " + compositeSG.numCompositeStates());
////		System.out.println("   --> # transitions: " + compositeSG.numCompositeStateTrans());
//		
//		System.out.println("   --> Peak used memory: " + peakUsed/1000000F + " MB");
//		System.out.println("   --> Peak total memory: " + peakTotal/1000000F + " MB");
//		System.out.println("   --> Final used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000F + " MB");
//		
//		long elapsedTimeMillis = System.currentTimeMillis()-start; 
//		float elapsedTimeSec = elapsedTimeMillis/1000F;
//		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
//		
//		if(elapsedTimeSec > 60){
//			float elapsedTime = elapsedTimeSec;
//			elapsedTime = elapsedTimeSec/(float)60;
//			System.out.println("   --> Elapsed time: " + elapsedTime + " min");
//		}
//		
//		System.out.println();
//		
////		System.out.println();
////		for(CompositeState cState : compositeSG.compositeStateMap){
////			State[] stateTuple = cState.getStateTuple();
////			State s1 = stateTuple[0];
////			State s2 = stateTuple[1];
////			
////			System.out.println(s1.getLabel() + ", " + s2.getLabel());
////		}
//		
//		return compositeSG;
//	}
	
	public static CompositeStateGraph compose2(CompositeStateGraph sg1, CompositeStateGraph sg2){
		long start = System.currentTimeMillis(); 
		
		if(sg1 == null || sg2 == null){
			return null;
		}
		
		StateGraph[] stateGraphArray1 = sg1.getStateGraphArray();
		StateGraph[] stateGraphArray2 = sg2.getStateGraphArray();
		
		// check an output drives an input
		boolean compatible = false;
		for(StateGraph g : stateGraphArray1){
			for(String output : g.getLpn().getAllOutputs().keySet()){
				for(StateGraph g2 : stateGraphArray2){
					VarSet inputs = (VarSet) g2.getLpn().getAllInputs().keySet();
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
			for(StateGraph g1 : stateGraphArray1){
				VarSet inputs = (VarSet) g1.getLpn().getAllInputs().keySet();
				for(StateGraph g2 : stateGraphArray2){
					for(String output : g2.getLpn().getAllOutputs().keySet()){
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
		for(StateGraph sg : stateGraphArray1){
			if(sgSet.add(sg) == false){
				System.out.println("state graphs " + sg1.getLabel() + " and " + sg2.getLabel() + " cannot be composed\n");
				return null;
			}
		}
		
		for(StateGraph sg : stateGraphArray2){
			if(sgSet.add(sg) == false){
				System.out.println("state graphs " + sg1.getLabel() + " and " + sg2.getLabel() + " cannot be composed\n");
				return null;
			}
		}
		
		// create new node with init states
		int size = sg1.getSize() + sg2.getSize();
		int sg1Size = sg1.getSize();
		int[] initStates = new int[size];
		for(int i = 0; i < sg1Size; i++){
			initStates[i] = stateGraphArray1[i].getInitialState().getIndex();
		}
		
		for(int i = sg1Size; i < size; i++){
			initStates[i] = stateGraphArray2[i-sg1Size].getInitialState().getIndex();
		}
		
		CompositeState initNode = new CompositeState(initStates);
		
		HashSet<Integer> synchronousTrans = new HashSet<Integer>();
		//for(StateGraph g1 : stateGraphArray1){
			//LhpnFile lpn1 = g1.getLpn();
			//for(StateGraph g2 : stateGraphArray2){
				//LhpnFile lpn2 = g2.getLpn();
				
				// TOOD: Need to use our LPN
				/*
				HashSet<LPNTran> inputTrans = new HashSet<LPNTran>();
				inputTrans.addAll(lpn1.getInputTranSet());
				inputTrans.retainAll(lpn2.getOutputTranSet());
				
				HashSet<LPNTran> outputTrans = new HashSet<LPNTran>();
				outputTrans.addAll(lpn2.getInputTranSet());
				outputTrans.retainAll(lpn1.getOutputTranSet());
				
				for(LPNTran lpnTran : inputTrans){
					synchronousTrans.add(lpnTran.getIndex());
				}
				
				for(LPNTran lpnTran : outputTrans){
					synchronousTrans.add(lpnTran.getIndex());
				}
				*/
			//}
		//}
		
		// create new composite state graph
		StateGraph[] sgArray = new StateGraph[size];
		List<LPN> lpnList = new ArrayList<LPN>(size);
		for(int i = 0; i < sg1Size; i++){
			sgArray[i] = stateGraphArray1[i];
			// TOOD: is this needed???
			//lpnList.add(stateGraphArray1[i].getLpn());
		}
		
		for(int i = sg1Size; i < size; i++){
			sgArray[i] = stateGraphArray2[i-sg1Size];
			// TOOD: is this needed???
			//lpnList.add(stateGraphArray2[i-sg1Size].getLpn());
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
		
		List<CompositeStateTran> intersectingTrans1 = new LinkedList<CompositeStateTran>();
		List<CompositeStateTran> intersectingTrans2 = new LinkedList<CompositeStateTran>();
		List<CompositeStateTran> independentTrans1 = new LinkedList<CompositeStateTran>();
		List<CompositeStateTran> independentTrans2 = new LinkedList<CompositeStateTran>();
		
		long peakUsed = 0;
		long peakTotal = 0;
		
		CompositeState tempState = null;
		while(!newStateStack.isEmpty()){
			CompositeState currentState = newStateStack.pop();
			CompositeState subState1 = subStateStack1.pop();
			CompositeState subState2 = subStateStack2.pop();
			
			int[] subState1Tuple = subState1.getStateTuple();
			int[] subState2Tuple = subState2.getStateTuple();
			
			List<CompositeStateTran> stateTrans1 = subState1.getOutgoingStateTranList();
			List<CompositeStateTran> stateTrans2 = subState2.getOutgoingStateTranList();
			
			// clear reused lists
			intersectingTrans1.clear();
			intersectingTrans2.clear();
			independentTrans1.clear();
			independentTrans2.clear();
			
			for(CompositeStateTran stateTran : stateTrans1){
				dataModels.lpn.parser.Transition lpnTran = stateTran.getLPNTran();

				if(!stateTran.visible()){
					independentTrans1.add(stateTran);
				}
				else{
					if(synchronousTrans.contains(lpnTran.getIndex())){
						for(CompositeStateTran stateTran2 : stateTrans2){
							dataModels.lpn.parser.Transition lpnTran2  = stateTran2.getLPNTran();
							if(lpnTran == lpnTran2){
								intersectingTrans1.add(stateTran);
								intersectingTrans2.add(stateTran2);
							}
						}
					}
					else{
						independentTrans1.add(stateTran);
					}
				}
			}
			
			for(CompositeStateTran stateTran : stateTrans2){
				dataModels.lpn.parser.Transition lpnTran = stateTran.getLPNTran();

				if(!stateTran.visible()){
					independentTrans2.add(stateTran);
				}
				else{
					if(!synchronousTrans.contains(lpnTran.getIndex())){
						independentTrans2.add(stateTran);
					}
				}
			}

			for(CompositeStateTran stateTran : independentTrans1){
				dataModels.lpn.parser.Transition lpnTran = stateTran.getLPNTran();
				CompositeState nextState = sg1.getState(stateTran.getNextState());
				int[] nextStateTuple = nextState.getStateTuple();
				int[] newStateTuple = new int[size];
				
				for(int j = 0; j < sg1Size; j++){
					newStateTuple[j] = nextStateTuple[j];
				}
				
				for(int j = sg1Size; j < size; j++){
					newStateTuple[j] = subState2Tuple[j-sg1Size];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				tempState = compositeSG.addState(newCompositeState);
				if(tempState == newCompositeState){
					newStateStack.push(newCompositeState);
					subStateStack1.push(nextState);
					subStateStack2.push(subState2);
				}
				else{
					newCompositeState = tempState;
				}
				
				CompositeStateTran newStateTran = compositeSG.addStateTran(currentState, newCompositeState, lpnTran);
				if(!lpnTran.isLocal()){
					newStateTran.setVisibility();
//					System.out.println(newStateTran);
				}
			}
			
			for(CompositeStateTran stateTran : independentTrans2){
				dataModels.lpn.parser.Transition lpnTran = stateTran.getLPNTran();
				CompositeState nextState = sg2.getState(stateTran.getNextState());
				int[] nextStateTuple = nextState.getStateTuple();
				int[] newStateTuple = new int[size];
				
				for(int i = 0; i < sg1Size; i++){
					newStateTuple[i] = subState1Tuple[i];
				}
				
				for(int i = sg1Size; i < size; i++){
					newStateTuple[i] = nextStateTuple[i-sg1Size];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				tempState = compositeSG.addState(newCompositeState);
				if(tempState == newCompositeState){
					newStateStack.push(newCompositeState);
					subStateStack1.push(subState1);
					subStateStack2.push(nextState);
				}
				else{
					newCompositeState = tempState;
				}

				CompositeStateTran newStateTran = compositeSG.addStateTran(currentState, newCompositeState, lpnTran);
				if(!lpnTran.isLocal()){
					newStateTran.setVisibility();
//					System.out.println(newStateTran);
				}
			}
			
			Iterator<CompositeStateTran> iter1 = intersectingTrans1.iterator();
			Iterator<CompositeStateTran> iter2 = intersectingTrans2.iterator();
			
			while(iter1.hasNext()){
				CompositeStateTran stateTran1 = iter1.next();
				CompositeStateTran stateTran2 = iter2.next();
				
				dataModels.lpn.parser.Transition lpnTran = stateTran1.getLPNTran();
				CompositeState nextState1 = sg1.getState(stateTran1.getNextState());
				int[] nextState1Tuple = nextState1.getStateTuple();
				CompositeState nextState2 = sg2.getState(stateTran2.getNextState());
				int[] nextState2Tuple = nextState2.getStateTuple();
				int[] newStateTuple = new int[size];
				
				for(int i = 0; i < sg1Size; i++){
					newStateTuple[i] = nextState1Tuple[i];
				}
				
				for(int i = sg1Size; i < size; i++){
					newStateTuple[i] = nextState2Tuple[i-sg1Size];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				tempState = compositeSG.addState(newCompositeState);
				if(tempState == newCompositeState){
					newStateStack.push(newCompositeState);
					subStateStack1.push(nextState1);
					subStateStack2.push(nextState2);
				}
				else{
					newCompositeState = tempState;
				}
				
				CompositeStateTran newStateTran = compositeSG.addStateTran(currentState, newCompositeState, lpnTran);
				if(!lpnList.containsAll(lpnTran.getDstLpnList())){
					newStateTran.setVisibility();
//					System.out.println(newStateTran);
				}
			}
			
			long curTotalMem = Runtime.getRuntime().totalMemory();
			if(curTotalMem > peakTotal)
				peakTotal = curTotalMem;
			
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if(curUsedMem > peakUsed)
				peakUsed = curUsedMem;

		}
		
		System.out.println("\n   " + compositeSG.getLabel() + ": ");
		System.out.println("   --> # states: " + compositeSG.numStates());
		System.out.println("   --> # transitions: " + compositeSG.numStateTrans());

		System.out.println("   --> Peak used memory: " + peakUsed/1000000F + " MB");
		System.out.println("   --> Peak total memory: " + peakTotal/1000000F + " MB");
		System.out.println("   --> Final used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000F + " MB");
		
		long elapsedTimeMillis = System.currentTimeMillis()-start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
		
		if(elapsedTimeSec > 60){
			float elapsedTime = elapsedTimeSec;
			elapsedTime = elapsedTimeSec/60;
			System.out.println("   --> Elapsed time: " + elapsedTime + " min");
		}
		
		System.out.println();
		
		return compositeSG;
	}
	
	public static CompositeStateGraph compose(CompositeStateGraph sg1, CompositeStateGraph sg2){
		long start = System.currentTimeMillis(); 
		
		if(sg1 == null || sg2 == null){
			return null;
		}
		
		StateGraph[] stateGraphArray1 = sg1.getStateGraphArray();
		StateGraph[] stateGraphArray2 = sg2.getStateGraphArray();
		
		// check an output drives an input
		boolean compatible = false;
		for(StateGraph g : stateGraphArray1){
			for(String output : g.getLpn().getAllOutputs().keySet()){
				for(StateGraph g2 : stateGraphArray2){
					VarSet inputs = (VarSet) g2.getLpn().getAllInputs().keySet();
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
			for(StateGraph g1 : stateGraphArray1){
				VarSet inputs = (VarSet) g1.getLpn().getAllInputs().keySet();
				for(StateGraph g2 : stateGraphArray2){
					for(String output : g2.getLpn().getAllOutputs().keySet()){
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
		for(StateGraph sg : stateGraphArray1){
			if(sgSet.add(sg) == false){
				System.out.println("state graphs " + sg1.getLabel() + " and " + sg2.getLabel() + " cannot be composed\n");
				return null;
			}
		}
		
		for(StateGraph sg : stateGraphArray2){
			if(sgSet.add(sg) == false){
				System.out.println("state graphs " + sg1.getLabel() + " and " + sg2.getLabel() + " cannot be composed\n");
				return null;
			}
		}
		
		// create new node with init states
		int size = sg1.getSize() + sg2.getSize();
		int sg1Size = sg1.getSize();
		int[] initStates = new int[size];
		for(int i = 0; i < sg1Size; i++){
			initStates[i] = stateGraphArray1[i].getInitialState().getIndex();
		}
		
		for(int i = sg1Size; i < size; i++){
			initStates[i] = stateGraphArray2[i-sg1Size].getInitialState().getIndex();
		}
		
		CompositeState initNode = new CompositeState(initStates);
		
		HashSet<Transition> synchronousTrans = new HashSet<Transition>();
		//for(StateGraph g1 : stateGraphArray1){
			//LhpnFile lpn1 = g1.getLpn();
			//for(StateGraph g2 : stateGraphArray2){
				//LhpnFile lpn2 = g2.getLpn();
				// TOOD: need to change to use our LPN
				/*
				HashSet<LPNTran> inputTrans = new HashSet<LPNTran>();
				inputTrans.addAll(lpn1.getInputTranSet());
				inputTrans.retainAll(lpn2.getOutputTranSet());
				
				HashSet<LPNTran> outputTrans = new HashSet<LPNTran>();
				outputTrans.addAll(lpn2.getInputTranSet());
				outputTrans.retainAll(lpn1.getOutputTranSet());
				
				synchronousTrans.addAll(inputTrans);
				synchronousTrans.addAll(outputTrans);
				*/
			//}
		//}
		
		// create new composite state graph
		StateGraph[] sgArray = new StateGraph[size];
		List<LPN> lpnList = new ArrayList<LPN>(size);
		for(int i = 0; i < sg1Size; i++){
			sgArray[i] = stateGraphArray1[i];
			// TODO: (future) Is this needed??
			//lpnList.add(stateGraphArray1[i].getLpn());
		}
		
		for(int i = sg1Size; i < size; i++){
			sgArray[i] = stateGraphArray2[i-sg1Size];
			// TODO: (future) Is this needed??
			//lpnList.add(stateGraphArray2[i-sg1Size].getLpn());
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
		
		List<dataModels.lpn.parser.Transition> intersectingTrans = new ArrayList<dataModels.lpn.parser.Transition>();
		List<CompositeState> intStateList1 = new ArrayList<CompositeState>();
		List<CompositeState> intStateList2 = new ArrayList<CompositeState>();
		List<dataModels.lpn.parser.Transition> independentTrans1 = new ArrayList<dataModels.lpn.parser.Transition>();
		List<CompositeState> indStateList1 = new ArrayList<CompositeState>();
		List<dataModels.lpn.parser.Transition> independentTrans2 = new ArrayList<dataModels.lpn.parser.Transition>();
		List<CompositeState> indStateList2 = new ArrayList<CompositeState>();
		
		long peakUsed = 0;
		long peakTotal = 0;
		
		CompositeState tempState = null;
		while(!newStateStack.isEmpty()){
			CompositeState currentState = newStateStack.pop();
			CompositeState subState1 = subStateStack1.pop();
			CompositeState subState2 = subStateStack2.pop();
			
			int[] subState1Tuple = subState1.getStateTuple();
			int[] subState2Tuple = subState2.getStateTuple();
			
			List<CompositeStateTran> stateTrans1 = subState1.getOutgoingStateTranList();
			List<CompositeStateTran> stateTrans2 = subState2.getOutgoingStateTranList();
			
			// clear reused lists
			intersectingTrans.clear();
			intStateList1.clear();
			intStateList2.clear();
			independentTrans1.clear();
			indStateList1.clear();
			independentTrans2.clear();
			indStateList2.clear();
			
			for(CompositeStateTran stateTran : stateTrans1){
				dataModels.lpn.parser.Transition lpnTran = stateTran.getLPNTran();
				CompositeState nextState = sg1.getState(stateTran.getNextState());
				
				if(!stateTran.visible()){
					independentTrans1.add(lpnTran);
					indStateList1.add(nextState);
				}
				else{
					if(synchronousTrans.contains(lpnTran)){
						for(CompositeStateTran stateTran2 : stateTrans2){
							dataModels.lpn.parser.Transition lpnTran2  = stateTran2.getLPNTran();
							CompositeState nextState2 = sg2.getState(stateTran2.getNextState());
							if(lpnTran == lpnTran2){
								intersectingTrans.add(lpnTran);
								intStateList1.add(nextState);
								intStateList2.add(nextState2);
							}
						}
					}
					else{
						independentTrans1.add(lpnTran);
						indStateList1.add(nextState);
					}
				}
			}
			
			for(CompositeStateTran stateTran : stateTrans2){
				dataModels.lpn.parser.Transition lpnTran = stateTran.getLPNTran();
				CompositeState nextState = sg2.getState(stateTran.getNextState());
				
				if(!stateTran.visible()){
					independentTrans2.add(lpnTran);
					indStateList2.add(nextState);
				}
				else{
					if(!synchronousTrans.contains(lpnTran)){
						independentTrans2.add(lpnTran);
						indStateList2.add(nextState);
					}
				}
			}

			for(int i = 0; i < independentTrans1.size(); i++){
				dataModels.lpn.parser.Transition lpnTran = independentTrans1.get(i);
				CompositeState nextState = indStateList1.get(i);
				int[] nextStateTuple = nextState.getStateTuple();
				int[] newStateTuple = new int[size];
				
				for(int j = 0; j < sg1Size; j++){
					newStateTuple[j] = nextStateTuple[j];
				}
				
				for(int j = sg1Size; j < size; j++){
					newStateTuple[j] = subState2Tuple[j-sg1Size];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				tempState = compositeSG.addState(newCompositeState);
				if(tempState == newCompositeState){
					newStateStack.push(newCompositeState);
					subStateStack1.push(nextState);
					subStateStack2.push(subState2);
				}
				else{
					newCompositeState = tempState;
				}
				
				CompositeStateTran newStateTran = compositeSG.addStateTran(currentState, newCompositeState, lpnTran);
				if(!lpnTran.isLocal()){
					newStateTran.setVisibility();
//					System.out.println(newStateTran);
				}
			}
			
			for(int j = 0; j < independentTrans2.size(); j++){
				dataModels.lpn.parser.Transition lpnTran = independentTrans2.get(j);
				CompositeState nextState = indStateList2.get(j);
				int[] nextStateTuple = nextState.getStateTuple();
				int[] newStateTuple = new int[size];
				
				for(int i = 0; i < sg1Size; i++){
					newStateTuple[i] = subState1Tuple[i];
				}
				
				for(int i = sg1Size; i < size; i++){
					newStateTuple[i] = nextStateTuple[i-sg1Size];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				tempState = compositeSG.addState(newCompositeState);
				if(tempState == newCompositeState){
					newStateStack.push(newCompositeState);
					subStateStack1.push(subState1);
					subStateStack2.push(nextState);
				}
				else{
					newCompositeState = tempState;
				}

				CompositeStateTran newStateTran = compositeSG.addStateTran(currentState, newCompositeState, lpnTran);
				if(!lpnTran.isLocal()){
					newStateTran.setVisibility();
//					System.out.println(newStateTran);
				}
			}
			
			for(int j = 0; j < intersectingTrans.size(); j++){
				Transition lpnTran = intersectingTrans.get(j);
				CompositeState nextState1 = intStateList1.get(j);
				int[] nextState1Tuple = nextState1.getStateTuple();
				CompositeState nextState2 = intStateList2.get(j);
				int[] nextState2Tuple = nextState2.getStateTuple();
				int[] newStateTuple = new int[size];
				
				for(int i = 0; i < sg1Size; i++){
					newStateTuple[i] = nextState1Tuple[i];
				}
				
				for(int i = sg1Size; i < size; i++){
					newStateTuple[i] = nextState2Tuple[i-sg1Size];
				}
				
				CompositeState newCompositeState = new CompositeState(newStateTuple);
				tempState = compositeSG.addState(newCompositeState);
				if(tempState == newCompositeState){
					newStateStack.push(newCompositeState);
					subStateStack1.push(nextState1);
					subStateStack2.push(nextState2);
				}
				else{
					newCompositeState = tempState;
				}
				
				CompositeStateTran newStateTran = compositeSG.addStateTran(currentState, newCompositeState, lpnTran);
				if(!lpnList.containsAll(lpnTran.getDstLpnList())){
					newStateTran.setVisibility();
//					System.out.println(newStateTran);
				}
			}
			
			long curTotalMem = Runtime.getRuntime().totalMemory();
			if(curTotalMem > peakTotal)
				peakTotal = curTotalMem;
			
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if(curUsedMem > peakUsed)
				peakUsed = curUsedMem;

		}
		
		System.out.println("\n   " + compositeSG.getLabel() + ": ");
		System.out.println("   --> # states: " + compositeSG.numStates());
		System.out.println("   --> # transitions: " + compositeSG.numStateTrans());

		System.out.println("   --> Peak used memory: " + peakUsed/1000000F + " MB");
		System.out.println("   --> Peak total memory: " + peakTotal/1000000F + " MB");
		System.out.println("   --> Final used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000F + " MB");
		
		long elapsedTimeMillis = System.currentTimeMillis()-start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
		
		if(elapsedTimeSec > 60){
			float elapsedTime = elapsedTimeSec;
			elapsedTime = elapsedTimeSec/60;
			System.out.println("   --> Elapsed time: " + elapsedTime + " min");
		}
		
		System.out.println();
		
		return compositeSG;
	}
	
	public void compositionalAnalsysis(List<StateGraph> designUnitSet) {
//		System.out.println("\n****** Compositional Analysis ******");
//		long start = System.currentTimeMillis(); 
//		
//		if(Options.getParallelFlag()){
//			parallelCompositionalFindSG(designUnitSet);
//		}
//		else{
//			compositionalFindSG(designUnitSet);
//		}
		
		findReducedSG(designUnitSet);
		
//		long totalMillis = System.currentTimeMillis()-start; 
//		float totalSec = totalMillis/1000F;
//		System.out.println("\n***** Total Elapsed Time: " + totalSec + " sec *****");
//		
//		if(totalSec > 60){
//			float totalTime = totalSec/(float)60;
//			System.out.println("***** Total Elapsed Time: " + totalTime + " min *****");
//		}
//		
//		System.out.println();
	}
	
	public void findReducedSG(List<StateGraph> designUnitSet) {
		System.out.println("\n****** Compositional Analysis ******");
		long start = System.currentTimeMillis(); 
		
		StateGraph[] sgArray = (StateGraph[]) designUnitSet.toArray();
		compositionalFindSG(sgArray);
		
		List<CompositeStateGraph> sgList = new ArrayList<CompositeStateGraph>();
		System.out.println();
		
		long peakTotal = 0;
		long peakUsed = 0;
		int largestSG = 0;
		
		for(StateGraph sg : designUnitSet){
			CompositeStateGraph csg = new CompositeStateGraph(sg);
			if(csg.numStates() > largestSG){
				largestSG = csg.numStates();
			}
			
//			csg.draw();
			if(Options.getCompositionalMinimization().equals("reduction")){
//				reduce(csg);
			}
			else if(Options.getCompositionalMinimization().equals("abstraction")){
				System.out.println(csg.getLabel() + ": transitionBasedAbstraction");
				transitionBasedAbstraction(csg);
//				csg.draw();
				System.out.println(csg.getLabel() + ": mergeOutgoing");
				mergeOutgoing(csg);
//				csg.draw();
				System.out.println(csg.getLabel() + ": mergeIncoming");
				mergeIncoming(csg);
				System.out.println();
				
//				csg.draw();
			}
			
			if(csg.numStates() > largestSG){
				largestSG = csg.numStates();
			}
			
			sgList.add(csg);
			
			long curTotalMem = Runtime.getRuntime().totalMemory();
			if(curTotalMem > peakTotal)
				peakTotal = curTotalMem;
			
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if(curUsedMem > peakUsed)
				peakUsed = curUsedMem;
		}
		
		CompositeStateGraph csg = null;
		if(sgList.size() > 0){
			csg = sgList.get(0);
			sgList.remove(0);
		}

		while(csg != null && sgList.size() > 1){
			CompositeStateGraph tmpSG = null;
			
			for(CompositeStateGraph sg2 : sgList){
				tmpSG = compose(csg, sg2);

				if(csg.numStates() > largestSG){
					largestSG = csg.numStates();
				}
				
				if(tmpSG != null){
					sgList.remove(sg2);
//					tmpSG.draw();
					System.out.println();
					if(Options.getCompositionalMinimization().equals("reduction")){
//						reduce(tmpSG);
					}
					else if(Options.getCompositionalMinimization().equals("abstraction")){
						System.out.println(tmpSG.getLabel() + ": transitionBasedAbstraction");
						transitionBasedAbstraction(tmpSG);
//						tmpSG.draw();
						System.out.println(tmpSG.getLabel() + ": mergeOutgoing");
						mergeOutgoing(tmpSG);
//						tmpSG.draw();
						System.out.println(tmpSG.getLabel() + ": mergeIncoming");
						mergeIncoming(tmpSG);
						System.out.println();
						
//						tmpSG.draw();
					}
					
					if(csg.numStates() > largestSG){
						largestSG = csg.numStates();
					}
					
					break;
				}
			}
			
			csg = tmpSG;
			
			long curTotalMem = Runtime.getRuntime().totalMemory();
			if(curTotalMem > peakTotal)
				peakTotal = curTotalMem;
			
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if(curUsedMem > peakUsed)
				peakUsed = curUsedMem;
		}
		
		if(sgList.size() == 1){
			csg = compose(csg, sgList.get(0));
			
			if(csg.numStates() > largestSG){
				largestSG = csg.numStates();
			}
			
			long curTotalMem = Runtime.getRuntime().totalMemory();
			if(curTotalMem > peakTotal)
				peakTotal = curTotalMem;
			
			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if(curUsedMem > peakUsed)
				peakUsed = curUsedMem;
		}
		
//		csg.draw();
		
		long curTotalMem = Runtime.getRuntime().totalMemory();
		if(curTotalMem > peakTotal)
			peakTotal = curTotalMem;
		
		long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		if(curUsedMem > peakUsed)
			peakUsed = curUsedMem;
		
		long totalMillis = System.currentTimeMillis()-start; 
		float totalSec = totalMillis/1000F;
		System.out.println("\n****** Total Elapsed Time: " + totalSec + " sec ******");
		
		if(totalSec > 60){
			float totalTime = totalSec/60;
			System.out.println("****** Total Elapsed Time: " + totalTime + " min ******");
		}
		
		System.out.println("****** Peak Memory Used: " + peakUsed/1000000F + " MB ******");
		System.out.println("****** Peak Memory Total: " + peakTotal/1000000F + " MB ******");
		System.out.println("****** Lastest SG: " + largestSG + " states ******");
		System.out.println();
	}
	
//	public void reduce(CompositeStateGraph sg){
//		long startTime = System.currentTimeMillis();
//		int initNumTrans = sg.numTransitions;
//		int initNumStates = sg.compositeStateMap.size();
//		int totalReducedTrans = sg.numTransitions;
//		
//		int case2StateMin = 0;
//		int case3StateMin = 0;
//		int case2TranMin = 0;
//		int case3TranMin = 0;
//		
//		int iter = 0;
//		while(totalReducedTrans > 0){
//			totalReducedTrans = 0;
//			
//			int numStates = sg.compositeStateMap.size();
//			int numTrans = sg.numTransitions;
//			int tranDiff = 0;
//			
//			case2(sg);
//			
//			case2TranMin += numTrans - sg.numTransitions;
//			tranDiff = numStates - sg.compositeStateMap.size();
//			case2StateMin += tranDiff;
//			totalReducedTrans += tranDiff;
//			
//			numTrans = sg.numTransitions;
//			numStates = sg.compositeStateMap.size();
//			
//			case3(sg);
//			
//			case3TranMin += numTrans - sg.numTransitions;
//			tranDiff = numStates - sg.compositeStateMap.size();
//			case3StateMin += tranDiff;
//			totalReducedTrans += tranDiff;
//			
//			iter++;
//		}
//		
//		System.out.println("   Reduce " + sg.getLabel() + ": ");
//		System.out.println("   --> case2: -" + case2StateMin + " states");
//		System.out.println("   --> case2: -" + case2TranMin + " transitions");
//		System.out.println("   --> case3: -" + case3StateMin + " states");
//		System.out.println("   --> case3: -" + case3TranMin + " transitions");
//		System.out.println("   --> # states: " + sg.compositeStateMap.size());
//		System.out.println("   --> # transitions: " + sg.numTransitions);
//		System.out.println("   --> # iterations: " + iter);
//		long elapsedTimeMillis = System.currentTimeMillis()-startTime; 
//		float elapsedTimeSec = elapsedTimeMillis/1000F;
//		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec\n");
//	}
	
	public static void transitionBasedAbstraction(CompositeStateGraph sg){
		HashSet<Integer> stateSet = new HashSet<Integer>();
		HashSet<CompositeStateTran> tranSet = new HashSet<CompositeStateTran>();
		CompositeState initialState = sg.getInitState();
		
		Stack<CompositeStateTran> stateTranStack = new Stack<CompositeStateTran>();
		HashSet<Integer> loopSet = new HashSet<Integer>();  // set used to detect loops
		HashSet<Integer> traversalSet = new HashSet<Integer>();  // set used to avoid duplicate work
		
//		int count = 0;
		for(CompositeStateTran nonlocalStateTran : sg.getStateTranSet()){
//			count++;
//			System.out.println(count + "/" + sg.getStateTranSet().size());
//			if(!sg.containsStateTran(nonlocalStateTran)) continue;
			
			dataModels.lpn.parser.Transition lpnTran = nonlocalStateTran.getLPNTran();
			if(!nonlocalStateTran.visible()){
				continue;
			}
			else if(nonlocalStateTran.getNextState() == sg.getInitState().getIndex()){
				// add current state transition
				tranSet.add(nonlocalStateTran);
				stateSet.add(nonlocalStateTran.getCurrentState());
				stateSet.add(nonlocalStateTran.getNextState());
				continue;
			}
			
			// state transition stack for dfs traversal
			stateTranStack.clear();
			
			// set used to detect loops
			loopSet.clear();
			loopSet.add(nonlocalStateTran.getNextState());
			
			// set used to avoid duplicate work
			traversalSet.clear();
			traversalSet.add(nonlocalStateTran.getNextState());
			
			boolean flag = false;
			CompositeState nextState = sg.getState(nonlocalStateTran.getNextState());
			for(CompositeStateTran outgoingTran : nextState.getOutgoingStateTranList()){
				if(!outgoingTran.visible()){
					stateTranStack.push(outgoingTran);
					loopSet.add(outgoingTran.getNextState());
//					traversalSet.add(outgoingTran.getNextState());
				}
				else{
					flag = true;
				}
			}
			
			// keep nonlocal state transition if a visible successor state transition exists
			if(flag){
				tranSet.add(nonlocalStateTran);
				stateSet.add(nonlocalStateTran.getCurrentState());
				stateSet.add(nonlocalStateTran.getNextState());
			}
			
//			System.out.println(nonlocalStateTran);
			while(!stateTranStack.empty()){
				CompositeStateTran currentStateTran = stateTranStack.pop();
				CompositeState currentNextState = sg.getState(currentStateTran.getNextState());
				
//				// if state has already been encountered skip
				if(!traversalSet.add(currentStateTran.getNextState())){
//					System.out.println("    " + currentStateTran);
//					System.out.println("skip");
					continue;
				}

				if(currentNextState == initialState){
					CompositeStateTran newStateTran = new CompositeStateTran(nonlocalStateTran.getCurrentState(), 
							currentStateTran.getNextState(), lpnTran);
					
					System.out.println(newStateTran.getCurrentState() + " -> " + newStateTran.getNextState());
					newStateTran.setVisibility();
					tranSet.add(newStateTran);
					stateSet.add(newStateTran.getCurrentState());
					stateSet.add(newStateTran.getCurrentState());
					
					continue;
				}
				
				// if the state transition does not have successor transitions create a state transition to last state in path
				if(currentNextState.numOutgoingTrans() == 0){
					CompositeStateTran newStateTran = new CompositeStateTran(nonlocalStateTran.getCurrentState(), 
							currentStateTran.getNextState(), lpnTran);
					
					newStateTran.setVisibility();
					tranSet.add(newStateTran);
					stateSet.add(newStateTran.getCurrentState());
					stateSet.add(newStateTran.getNextState());
				}
				
				// add local outgoing state trans to stack
				// for each nonlocal state tran create a state transition from nonlocalStateTran.currentState to stateTran.currentState
				for(CompositeStateTran stateTran : currentNextState.getOutgoingStateTranList()){
					if(stateTran.visible()){
						CompositeStateTran newStateTran = new CompositeStateTran(nonlocalStateTran.getCurrentState(), 
								stateTran.getCurrentState(), lpnTran);
						
						newStateTran.setVisibility();
						tranSet.add(newStateTran);
						stateSet.add(nonlocalStateTran.getCurrentState());
						stateSet.add(stateTran.getCurrentState());
					}
					else{
//						if(!loopSet.add(stateTran.getNextState())){
//							// create self loop after visible state transition
//							if(flag){								
//								CompositeStateTran newStateTran = new CompositeStateTran(nonlocalStateTran.getNextState(), 
//										nonlocalStateTran.getNextState(), currentStateTran.getLPNTran());
//
//								tranSet.add(newStateTran);
//							}
//							else{
//								tranSet.add(nonlocalStateTran);
//								stateSet.add(nonlocalStateTran.getCurrentState());
//								stateSet.add(nonlocalStateTran.getNextState());
//								flag = true;
//								
//								CompositeStateTran newStateTran = new CompositeStateTran(nonlocalStateTran.getNextState(), 
//										nonlocalStateTran.getNextState(), currentStateTran.getLPNTran());
//
//								tranSet.add(newStateTran);
//							}
//							
////							traversalSet.add(stateTran.getNextState());
//							continue;
//						}
//						else if(!traversalSet.add(stateTran.getNextState())){
//							continue;
//						}
						
						stateTranStack.push(stateTran);
					}
				}
				
				loopSet.remove(currentNextState.getIndex());
			}
		}

		System.out.println(stateSet.size());
		System.out.println(tranSet.size());
//		System.out.println("INITIAL STATE");
		// handle initial state
		loopSet.clear();
		loopSet.add(initialState.getIndex());
		
		traversalSet.clear();
		traversalSet.add(initialState.getIndex());
		
		for(CompositeStateTran stateTran : initialState.getOutgoingStateTranList()){
			if(!stateTran.visible()){
				stateTranStack.push(stateTran);
				loopSet.add(stateTran.getNextState());
//				traversalSet.add(stateTran.getNextState());
			}
		}
		
		stateSet.add(initialState.getIndex());
		
		while(!stateTranStack.empty()){
			CompositeStateTran stateTran = stateTranStack.pop();
			
//			// if state has already been encountered skip
			if(!traversalSet.add(stateTran.getNextState())){
				continue;
			}
			
			CompositeState nextState = sg.getState(stateTran.getNextState());
			
			// if the state transition does not have successor transitions create a state transition to last state in path
			if(nextState.numOutgoingTrans() == 0){
				CompositeStateTran newStateTran = new CompositeStateTran(initialState, sg.getState(stateTran.getNextState()), 
						stateTran.getLPNTran());

				newStateTran.setVisibility();
				tranSet.add(newStateTran);
				stateSet.add(stateTran.getNextState());
			}
			
			for(CompositeStateTran succStateTran : nextState.getOutgoingStateTranList()){
				dataModels.lpn.parser.Transition lpnTran = succStateTran.getLPNTran();
				
				if(succStateTran.visible()){
					// create a state tran from initial state to succStateTran.currentState
					CompositeStateTran newStateTran = new CompositeStateTran(initialState, sg.getState(succStateTran.getNextState()), lpnTran);
					newStateTran.setVisibility();
					tranSet.add(newStateTran);
					stateSet.add(succStateTran.getNextState());
				}
				else{
//					if(!loopSet.add(succStateTran.getNextState())){
//						CompositeStateTran newStateTran = new CompositeStateTran(initialState, initialState, lpnTran);
//						tranSet.add(newStateTran);
//						
////						traversalSet.add(succStateTran.getNextState());
//						continue;
//					}
//					else if(!traversalSet.add(succStateTran.getNextState())){
//						continue;
//					}
					
					// add to stack
					stateTranStack.push(succStateTran);
				}
			}
			
			loopSet.remove(stateTran.getNextState());
		}

//		System.out.println();
//		for(CompositeStateTran stateTran : tranSet){
//			System.out.println(stateTran);
//		}
//		System.out.println();
//		
//		System.out.println();
//		for(Integer state : stateSet){
//			System.out.println(state);
//		}
//		System.out.println();
		
//		System.out.println("COMPOSITE STATE SET");
		HashMap<CompositeState, CompositeState> stateMap = new HashMap<CompositeState, CompositeState>();
		HashMap<Integer, CompositeState> indexStateMap = new HashMap<Integer, CompositeState>();
		for(Integer stateIndex : stateSet){
			CompositeState currentState = sg.getState(stateIndex);
			currentState.clear();
			
			stateMap.put(currentState, currentState);
			indexStateMap.put(stateIndex, currentState);
		}
		
		sg.indexStateMap = indexStateMap;
		sg.stateMap = stateMap;
		
		sg.stateTranMap = new HashMap<CompositeStateTran, CompositeStateTran>();
		for(CompositeStateTran stateTran : tranSet){
			sg.addStateTran(stateTran);
		}
		
		System.out.println("   --> # states: " + stateSet.size());
		System.out.println("   --> # transitions: " + tranSet.size());
	}

	private void removeUnreachableState(CompositeStateGraph sg, CompositeState currentState){
		if(currentState == sg.getInitState()){
			return;
		}
		else if(currentState.numIncomingTrans() != 0){
			return;
		}
		
		boolean rc = sg.containsState(currentState.getIndex());
		if(rc == false){
			return;
		}
		
		for(CompositeStateTran stateTran : currentState.getOutgoingStateTranList().toArray(new CompositeStateTran[currentState.numOutgoingTrans()])){
			sg.removeStateTran(stateTran);
			
			CompositeState nextState = sg.getState(stateTran.getNextState());
			if(nextState.numIncomingTrans() == 0){
				removeUnreachableState(sg, nextState);
			}
		}
		
		sg.removeState(currentState);
	}
	
	private void removeDanglingState(CompositeStateGraph sg, CompositeState currentState){
		if(currentState == sg.getInitState()){
			return;
		}
		else if(currentState.numOutgoingTrans() != 0){
			return;
		}
		
		boolean rc = sg.containsState(currentState.getIndex());
		if(rc == false){
			return;
		}

		for(CompositeStateTran stateTran : currentState.getIncomingStateTranList().toArray(new CompositeStateTran[currentState.numIncomingTrans()])){
			sg.removeStateTran(stateTran);
			
			CompositeState previousState = sg.getState(stateTran.getCurrentState());
			if(previousState.numOutgoingTrans() == 0){
				removeDanglingState(sg, previousState);
			}
		}
		
		sg.removeState(currentState);
	}
	
	public void redundantStateRemoval(CompositeStateGraph sg){
		this.mergeOutgoing(sg);
		this.mergeIncoming(sg);
	}
	
	public void mergeOutgoing(CompositeStateGraph sg){
//		System.out.println("FIND EQUIVALENT PAIRS");
		HashSet<Pair<Integer, Integer>> equivalentPairSet = findInitialEquivalentPairs(sg);
//		System.out.println("REMOVE STATES");
		// remove states that are not equivalent
		for(Pair<Integer, Integer> eqPair : equivalentPairSet.toArray(new Pair[equivalentPairSet.size()])){
			CompositeState state1 = sg.getState(eqPair.getLeft());
			CompositeState state2 = sg.getState(eqPair.getRight());
			
			List<CompositeStateTran> stateTranList1 = state1.getOutgoingStateTranList();
			List<CompositeStateTran> stateTranList2 = state2.getOutgoingStateTranList();

			boolean eq = true;
			for(CompositeStateTran stateTran1 : stateTranList1){
				boolean succEq = false;
				for(CompositeStateTran stateTran2 : stateTranList2){
					int nextState1 = stateTran1.getNextState();
					int nextState2 = stateTran2.getNextState();
					
					if(nextState1 == nextState2){
						succEq = true;
						continue;
					}
					
					if(nextState2 < nextState1){
						int tmp = nextState2;
						nextState2 = nextState1;
						nextState1 = tmp;
					}
					
					Pair<Integer, Integer> statePair = new Pair<Integer, Integer>(nextState1, nextState2);
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

		for(Pair<Integer, Integer> statePair : equivalentPairSet){
			int stateIndex1 = statePair.getLeft();
			int stateIndex2 = statePair.getRight();

			if(!sg.containsState(stateIndex1) || !sg.containsState(stateIndex2)) 
				continue;
			
			System.out.println(stateIndex1 + " - " + stateIndex2);
			CompositeState state2 = sg.getState(stateIndex2);
			
			// merge
			for(CompositeStateTran incomingStateTran : state2.getIncomingStateTranList().toArray(new CompositeStateTran[state2.numIncomingTrans()])){
				sg.removeStateTran(incomingStateTran);
				
				incomingStateTran.setNextState(stateIndex1);
				sg.addStateTran(incomingStateTran);
			}
			
			this.removeUnreachableState(sg, state2);
		}
		
		System.out.println("   --> # states: " + sg.numStates());
		System.out.println("   --> # transitions: " + sg.numStateTrans());
	}
	
	public void mergeIncoming(CompositeStateGraph sg){
		HashSet<Pair<Integer, Integer>> equivalentPairSet = findInitialEquivalentPairs2(sg);
		
		for(Pair<Integer, Integer> statePair : equivalentPairSet){
			int stateIndex1 = statePair.getLeft();
			int stateIndex2 = statePair.getRight();

			System.out.println(stateIndex1 + " - " + stateIndex2);
			
			if(!sg.containsState(stateIndex1) || !sg.containsState(stateIndex2)) 
				continue;
			
			CompositeState state2 = sg.getState(stateIndex2);
			
			// merge outgoing state transitions
			for(CompositeStateTran outgoingStateTran : state2.getOutgoingStateTranList().toArray(new CompositeStateTran[state2.numOutgoingTrans()])){
				sg.removeStateTran(outgoingStateTran);
				
				outgoingStateTran.setCurrentState(stateIndex1);
				sg.addStateTran(outgoingStateTran);
			}
			
			this.removeDanglingState(sg, state2);
		}
		
		System.out.println("   --> # states: " + sg.numStates());
		System.out.println("   --> # transitions: " + sg.numStateTrans());
	}
	
	private static HashSet<Pair<Integer, Integer>> findInitialEquivalentPairs(CompositeStateGraph sg){
		HashSet<Pair<Integer, Integer>> equivalentSet = new HashSet<Pair<Integer, Integer>>();
		CompositeState[] stateArray = sg.getStateSet().toArray(new CompositeState[sg.numStates()]);
		
		for(int i = 0; i < stateArray.length; i++){
//			System.out.println("  " + i + "/" + stateArray.length);
			CompositeState state1 = stateArray[i];
			List<Transition> enabled1 = CompositeStateGraph.getEnabled(state1);
//			HashSet<LPNTran> enabled1Set = new HashSet<LPNTran>();
//			enabled1Set.addAll(enabled1);
			
			for(int j = i + 1; j < stateArray.length; j++){
//				System.out.println("    " + j + "/" + stateArray.length);
				CompositeState state2 = stateArray[j];
				CompositeState state = state1;
				
				List<Transition> enabled2 = CompositeStateGraph.getEnabled(state2);
				
				if(enabled1.containsAll(enabled2) && enabled2.containsAll(enabled1)){
					if(state2.getIndex() < state.getIndex()){
						CompositeState temp = state;
						state = state2;
						state2 = temp;
					}
					
					equivalentSet.add(new Pair<Integer, Integer>(state.getIndex(), state2.getIndex()));
				}
			}
		}
		
		return equivalentSet;
	}
	
	@SuppressWarnings("unused")
	private static boolean equivalentOutgoing(Set<Transition> enabled1, List<Transition> enabled2){
//		enabled2.containsAll(enabled1) && enabled1.containsAll(enabled2)
		HashSet<Transition> enabled2Set = new HashSet<Transition>();
		enabled2Set.addAll(enabled2);
		
		if(enabled2Set.size() == enabled1.size() && enabled1.containsAll(enabled2Set))
			return true;
		
		return false;
	}
	
	private static HashSet<Pair<Integer, Integer>> findInitialEquivalentPairs2(CompositeStateGraph sg){
		HashSet<Pair<Integer, Integer>> equivalentSet = new HashSet<Pair<Integer, Integer>>();
		
		CompositeState[] stateArray = sg.getStateSet().toArray(new CompositeState[sg.numStates()]);
		for(int i = 0; i < stateArray.length; i++){
			CompositeState state1 = stateArray[i];
			List<Transition> enabled1 = CompositionalAnalysis.getIncomingLpnTrans(state1);
			
			for(int j = i + 1; j < stateArray.length; j++){
				CompositeState state2 = stateArray[j];
				CompositeState state = state1;
				
				List<Transition> enabled2 = CompositionalAnalysis.getIncomingLpnTrans(state2);
				
				if(enabled2.containsAll(enabled1) && enabled1.containsAll(enabled2)){
					if(state2.getIndex() < state.getIndex()){
						CompositeState temp = state;
						state = state2;
						state2 = temp;
					}
					
					equivalentSet.add(new Pair<Integer, Integer>(state.getIndex(), state2.getIndex()));
				}
			}
		}
		
		return equivalentSet;
	}
	
	private static List<Transition> getIncomingLpnTrans(CompositeState currentState){
		Set<Transition> lpnTranSet = new HashSet<Transition>(currentState.numOutgoingTrans());
		List<Transition> enabled = new ArrayList<Transition>(currentState.numOutgoingTrans());
		
		for(CompositeStateTran stTran : currentState.getIncomingStateTranList()){
			Transition lpnTran = stTran.getLPNTran();
			if(lpnTranSet.add(lpnTran))
				enabled.add(lpnTran);
		}
		
		return enabled;
	}
//	public void case2(CompositeStateGraph sg){
//		long start = System.currentTimeMillis();
//		int trans = sg.numTransitions;
//		int states = sg.numCompositeStates();
//		
//		List<LPNTran> localTrans = new ArrayList<LPNTran>();
//		List<CompositeState> localStates = new ArrayList<CompositeState>();
//		List<LPNTran> nonLocalTrans = new ArrayList<LPNTran>();
//		List<CompositeState> nonLocalStates = new ArrayList<CompositeState>();
//		
//		for(Object o : sg.getStateMap().values().toArray()){
//			CompositeState currentState = (CompositeState) o;
//			
//			List<LPNTran> enabledTrans = currentState.enabledTranList;
//			List<CompositeState> nextStates = currentState.nextStateList;
//			
//			localTrans.clear();
//			localStates.clear();
//			nonLocalTrans.clear();
//			nonLocalStates.clear();
//			
//			for(int i = 0; i < enabledTrans.size(); i++){
//				LPNTran lpnTran = enabledTrans.get(i);
//				CompositeState nextState = (nextStates.get(i));
//				if(lpnTran.local()){
//					localTrans.add(lpnTran);
//					localStates.add(nextState);
//				}
////				else{
//					nonLocalTrans.add(lpnTran);
//					nonLocalStates.add(nextState);
////				}
//			}
//			
//			if(nonLocalTrans.isEmpty()){
//				continue;
//			}
//			
//			for(int i = 0; i < nonLocalTrans.size(); i++){
//				LPNTran nonLocalTran = nonLocalTrans.get(i);
//				CompositeState s2 = nonLocalStates.get(i);
//				
//				List<LPNTran> s2Enabled = s2.enabledTranList;
//				if(s2Enabled.size() > 1 || s2Enabled.size() < 1){
//					continue;
//				}
////				else if(s2 == sg.getInitState()){
////					continue;
////				}
//				
//				List<CompositeState> s2NextState = s2.nextStateList;
//				for(int j = 0; j < s2Enabled.size(); j++){
//					LPNTran invTran2 = s2Enabled.get(j);
//					if(invTran2.local()){
//						CompositeState s4 = s2NextState.get(j);
//						
//						for(int k = 0; k < localTrans.size(); k++){
//							LPNTran invTran = localTrans.get(k);
//							if(invTran == nonLocalTran) continue;
//							
//							CompositeState s3 = localStates.get(k);
//							
//							List<LPNTran> s3Enabled = s3.enabledTranList;
//							List<CompositeState> s3NextState = s3.nextStateList;
//							for(int n = 0; n < s3Enabled.size(); n++){
//								LPNTran nonLocalTran2 = s3Enabled.get(n);
//								CompositeState nextState = s3NextState.get(n);
//								
//								if(nonLocalTran2 == nonLocalTran && nextState == s4){
//									currentState.enabledTranList.remove(nonLocalTran);
//									currentState.nextStateList.remove(s2);
//									sg.numTransitions --;
//									
//									List<CompositeState> incomingStates = s2.incomingStateList;
//									for(int m = 0; m < incomingStates.size(); m++){
//										CompositeState curr = incomingStates.get(m);
//										List<CompositeState> incomingNextStateList = curr.nextStateList;
//										
//										for(int idx = 0; idx < incomingNextStateList.size(); idx++){
//											CompositeState tmpState = incomingNextStateList.get(idx);
//											if(tmpState == s2){
//												incomingNextStateList.set(idx, s4);
//												s4.incomingStateList.add(curr);
//												break;
//											}
//										}
//									}
//									
//									s2.nextStateList.clear();
//									s2.incomingStateList.clear();
//									s2.enabledTranList.clear();
//									
//									sg.compositeStateMap.remove(s2);
//									if(sg.getInitState() == s2){
//										sg.setInitState(s4);
//									}
//									sg.numTransitions --;
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//		
////		System.out.println(sg.getLabel() + " case2 transitions: " + trans + " - " + sg.numTransitions);
////		System.out.println(sg.getLabel() + " case2 states: " + states + " - " + sg.numCompositeStates());
////		long elapsedTimeMillis = System.currentTimeMillis()-start; 
////		float elapsedTimeSec = elapsedTimeMillis/1000F;
////		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
//	}
//	
//	public void case3(CompositeStateGraph sg){
//		long start = System.currentTimeMillis();
//		int trans = sg.numTransitions;
//		int states = sg.numCompositeStates();
//		
//		List<LPNTran> localTrans = new ArrayList<LPNTran>();
//		List<CompositeState> localStates = new ArrayList<CompositeState>();
//		List<LPNTran> nonLocalTrans = new ArrayList<LPNTran>();
//		List<CompositeState> nonLocalStates = new ArrayList<CompositeState>();
//		
//		for(Object o : sg.getStateMap().values().toArray()){
//			CompositeState currentState = (CompositeState) o;
//			
//			List<LPNTran> enabledTrans = currentState.enabledTranList;
//			List<CompositeState> nextStates = currentState.nextStateList;
//			
//			localTrans.clear();
//			localStates.clear();
//			nonLocalTrans.clear();
//			nonLocalStates.clear();
//			
//			for(int i = 0; i < enabledTrans.size(); i++){
//				LPNTran lpnTran = enabledTrans.get(i);
//				CompositeState nextState = (nextStates.get(i));
//				if(lpnTran.local()){
//					localTrans.add(lpnTran);
//					localStates.add(nextState);
//				}
////				else{
//					nonLocalTrans.add(lpnTran);
//					nonLocalStates.add(nextState);
////				}
//			}
//			
//			if(nonLocalTrans.isEmpty()){
//				continue;
//			}
//			
//			for(int i = 0; i < localTrans.size(); i++){
//				LPNTran localTran = localTrans.get(i);
//				CompositeState s3 = localStates.get(i);
//				if(s3.incomingStateList.size() != 1){
//					continue;
//				}
////				else if(s3 == sg.getInitState()){
////					continue;
////				}
//				
//				List<LPNTran> s3Enabled = s3.enabledTranList;
//				List<CompositeState> s3NextState = s3.nextStateList;
//				
//				boolean remove = false;
//				List<LPNTran> removeTran = new ArrayList<LPNTran>();
//				
//				for(int j = 0; j < s3Enabled.size(); j++){
//					LPNTran nonLocalTran2 = s3Enabled.get(j);
//					if(!nonLocalTran2.local()){
//						CompositeState s4 = s3NextState.get(j);
//						
//						for(int k = 0; k < nonLocalTrans.size(); k++){
//							LPNTran nonLocalTran = nonLocalTrans.get(k);
//							if(localTran == nonLocalTran) continue;
//							
//							CompositeState s2 = nonLocalStates.get(k);
//							
//							List<LPNTran> s2Enabled = s2.enabledTranList;
//							List<CompositeState> s2NextState = s2.nextStateList;
//							for(int n = 0; n < s2Enabled.size(); n++){
//								LPNTran invTran2 = s2Enabled.get(n);
//								if(invTran2.local()){
//									CompositeState nextState = s2NextState.get(n);
//									
//									if(nonLocalTran2 == nonLocalTran && nextState == s4){
//										removeTran.add(nonLocalTran2);
//										remove = true;
//									}
//								}
//							}
//						}
//					}
//				}
//				
//				if(remove){
//					currentState.enabledTranList.remove(localTran);
//					currentState.nextStateList.remove(s3);
//					sg.numTransitions--;
//					
//					for(int m = 0; m < s3Enabled.size(); m++){
//						CompositeState currState = s3NextState.get(m);
//						LPNTran currTran = s3Enabled.get(m);
//						
//						if(removeTran.contains(currTran)){
//							removeTran.remove(currTran);
//							sg.numTransitions--;
//							continue;
//						}
//						
//						currentState.enabledTranList.add(currTran);
//						currentState.nextStateList.add(currState);
//						
//						List<CompositeState> currIncomingList = currState.incomingStateList;
//						for(int idx = 0; idx < currIncomingList.size(); idx++){
//							if(currIncomingList.get(idx) == s3){
//								currIncomingList.set(idx, currState);
//							}
//						}
//					}
//					
//					sg.compositeStateMap.remove(s3);
//					if(sg.getInitState() == s3){
//						sg.setInitState(currentState);
//					}
//					
//					s3.nextStateList.clear();
//					s3.enabledTranList.clear();
//					s3.incomingStateList.clear();
//				}
//			}
//		}
//		
////		System.out.println(sg.getLabel() + " case3 transitions: " + trans + " - " + sg.numTransitions);
////		System.out.println(sg.getLabel() + " case3 states: " + states + " - " + sg.numCompositeStates());
////		long elapsedTimeMillis = System.currentTimeMillis()-start; 
////		float elapsedTimeSec = elapsedTimeMillis/1000F;
////		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
//	}
	
	/**
     * Constructs the compositional state graphs.
     */
	public static void compositionalFindSG(StateGraph[] sgArray){
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
		for (StateGraph sg : sgArray) {
			LPN lpn = sg.getLpn();
		    // Add initial state to state graph
			State init = sg.genInitialState();
			sg.setInitialState(init);
			sg.addState(init);
			sg.addFrontierState(init);
			Set<String> inputSet = lpn.getAllInputs().keySet();
			Set<String> outputSet = lpn.getAllOutputs().keySet();
			int numSrc = 0;
			// Find lpn interfaces
			for(StateGraph sg2 : sgArray){
				if(sg == sg2) 
					continue;
				LPN lpn2 = sg2.getLpn();
				Set<String> outputs = lpn2.getAllOutputs().keySet();
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
			List<int[]> thisInterfaceList = new ArrayList<int[]>(sgArray.length);
			List<int[]> otherInterfaceList = new ArrayList<int[]>(sgArray.length);
			// TODO: Why designUnitSet.size() + 1 ?
//			for(int i = 0; i < designUnitSet.size() + 1; i++){
			for(int i = 0; i < sgArray.length; i++){
				thisInterfaceList.add(null);
				otherInterfaceList.add(null);
			}
			List<StateGraph> srcArray = new ArrayList<StateGraph>(numSrc);
			if(numSrc > 0){
//				int index = 0;
				for(StateGraph sg2 : sgArray){
					if(sg == sg2) 
						continue;
					LPN lpn2 = sg2.getLpn();					
					int interfaceSize = 0;
					Set<String> outputs = lpn2.getAllOutputs().keySet();
					Set<String> inputs = lpn2.getAllInputs().keySet();
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
						lpn.genIndexLists(thisIndexList, otherIndexList, lpn2);						
						thisInterfaceList.set(lpn2.getLpnIndex(), thisIndexList);
						otherInterfaceList.set(lpn2.getLpnIndex(), otherIndexList);
						// Hao's LPN starting index is 1, whereas ours starts from 0. 
//						thisInterfaceList.set(lpn2.ID-1, thisIndexList);
//						otherInterfaceList.set(lpn2.ID-1, otherIndexList);
						srcArray.add(sg2);
//						index++;
					}
				}
			}
			lpn.setThisIndexList(thisInterfaceList);
			lpn.setOtherIndexList(otherInterfaceList);
			inputSrcMap.put(sg, srcArray);
		}
		
		// TODO: (temp) designUnitSet has been created already at this point. 
//		LhpnFile[] lpnList = new LhpnFile[designUnitSet.size()];
//		int idx = 0;
//		for (StateGraph sg : designUnitSet) {
//			lpnList[idx++] = sg.getLpn();
//		}
		constructDstLpnList(sgArray);
		// Run initial findSG
		for (StateGraph sg : sgArray) {
			int result = 0;			
			if(Options.getStickySemantics()){
//				result = sg.constrStickyFindSG(sg.getInitialState(), emptyTranList);
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
			for(StateGraph sg : sgArray){
				sg.genConstraints();
				sg.genFrontier();
			}
			// Extract and apply constraints generated from srcSG to sg. 
			for(StateGraph sg : sgArray){
				for(StateGraph srcSG : inputSrcMap.get(sg)){
					extractConstraints(sg, srcSG, newConstraintSet, oldConstraintSet);
					newTransitions += applyConstraintSet(sg, srcSG, newConstraintSet, oldConstraintSet);			
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
//		int numTrans = 0;
		int numConstr = 0;
		for (StateGraph sg : sgArray) {
			sg.genConstraints();
			sg.genFrontier();
			System.out.print("   ");
			sg.printStates();
//			sg.clear();
			numStates += sg.reachSize();
//			numTrans += sg.numTransitions();
			numConstr += sg.numConstraints();
		}
		
		System.out.println("\n   --> # states: " + numStates);
//		System.out.println("   --> # transitions: " + numTrans);
		System.out.println("   --> # constraints: " + numConstr);
		System.out.println("   --> # iterations: " + iter);
		System.out.println("\n   --> Peak used memory: " + peakUsed/1000000F + " MB");
		System.out.println("   --> Peak total memory: " + peakTotal/1000000F + " MB");
		System.out.println("   --> Final used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000F + " MB");
		long elapsedTimeMillis = System.currentTimeMillis()-start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("   --> Elapsed time: " + elapsedTimeSec + " sec");
		if(elapsedTimeSec > 60){
			float elapsedTime = elapsedTimeSec/60;
			System.out.println("   --> Elapsed time: " + elapsedTime + " min");
		}
	}
	
	/**
     * Constructs the compositional state graphs.
     */
	public static void parallelCompositionalFindSG(List<StateGraph> designUnitSet){
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
			LPN lpn = sg.getLpn();
			
            // Add initial state to state graph
			State init = sg.genInitialState();
			sg.setInitialState(init);
			sg.addState(init);
			sg.addFrontierState(init);
			
			Set<String> inputSet = lpn.getAllInputs().keySet();
			Set<String> outputSet = lpn.getAllOutputs().keySet();
			int size = 0;
			
			// Find lpn interfaces
			for(StateGraph sg2 : designUnitSet){				
				if(sg == sg2) continue;
				
				Set<String> outputs = sg2.getLpn().getAllOutputs().keySet();
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
					LPN lpn2 = sg2.getLpn();
					if(sg == sg2) continue;
					
					boolean src = false;
					//int interfaceSize = 0;
					Set<String> outputs = lpn2.getAllOutputs().keySet();
					for(String output : outputs){
						if (inputSet.contains(output)){
							//interfaceSize++;
							src = true;
						}
					}

					if(src){
						Set<String> inputs = lpn2.getAllInputs().keySet();
						for(String input : inputs){
							if (outputSet.contains(input)){
								//interfaceSize++;
							}
						}
						
						for(String input : inputs){
							if (inputSet.contains(input)){
								//interfaceSize++;
							}
						}
						
						for(String output : outputs){
							if (outputSet.contains(output)){
								//interfaceSize++;
							}
						}
						
						//int[] thisIndexList = new int[interfaceSize];
						//int[] otherIndexList = new int[interfaceSize];
						// TODO: (future) need to add getThisIndexArray in LhpnFile
						/*
						lpn.genIndexLists(thisIndexList, otherIndexList, lpn2);
						
						thisInterfaceList.set(lpn2.ID, thisIndexList);
						otherInterfaceList.set(lpn2.ID, otherIndexList);
						*/
						srcArray[index] = sg2;
						index++;
					}
				}
			}
			lpn.setThisIndexList(thisInterfaceList);
			lpn.setOtherIndexList(otherInterfaceList);
			inputSrcMap.put(sg, srcArray);
		}
		
		LPN[] lpnList = new LPN[designUnitSet.size()];

		int idx = 0;
		for (StateGraph sg : designUnitSet) {
			lpnList[idx] = sg.getLpn();
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
			numConstr += sg.numConstraints();
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
			float elapsedTime = elapsedTimeSec/60;
			System.out.println("   --> Elapsed time: " + elapsedTime + " min");
		}	
	}
	
	/**
	 * Applies new constraints to the entire state set, and applies old constraints to the frontier state set.
     * @return Number of new transitions.
     */
	private static int applyConstraintSet(StateGraph sg, StateGraph srcSG, List<Constraint> newConstraintSet, List<Constraint> oldConstraintSet){
		int newTransitions = 0;
		LPN srcLpn = srcSG.getLpn();
		LPN lpn = sg.getLpn();
		int[] thisIndexList = lpn.getThisIndexArray(srcLpn.getLpnIndex());
		int[] otherIndexList = lpn.getOtherIndexArray(srcLpn.getLpnIndex());
//		int[] thisIndexList = lpn.getThisIndexArray(srcLpn.ID - 1);
//		int[] otherIndexList = lpn.getOtherIndexArray(srcLpn.ID - 1);		
		if(newConstraintSet.size() > 0){
			for(State currentState : sg.getStateSet()){
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
     * @param sg - The state graph the constraints are to be applied.
     * @param srcSG - The state graph the constraint are extracted from.
     */
	private static void extractConstraints(StateGraph sg, StateGraph srcSG, List<Constraint> newConstraintSet, List<Constraint> oldConstraintSet){
		newConstraintSet.clear();
		oldConstraintSet.clear();
		LPN srcLpn = srcSG.getLpn();
		for(Constraint newConstraint : sg.getNewConstraintSet()){
			if(newConstraint.getLpn() != srcLpn) 
				continue;	    	
			newConstraintSet.add(newConstraint);
		}
		for(Constraint oldConstraint : sg.getOldConstraintSet()){
			if(oldConstraint.getLpn() != srcLpn) 
				continue;
			oldConstraintSet.add(oldConstraint);
		}
	}
	
	/**
     * Determines whether a constraint is compatible with a state.
     * @return True if compatible, otherwise False.
     */
	private static boolean compatible(State currentState, Constraint constr, int[] thisIndexList, int[] otherIndexList){
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
     * Creates a state from a given constraint and compatible state and insert into state graph.  If the state is new, then findSG is called.
     * @return Number of new transitions.
     */
	private static int createNewState(StateGraph sg, State compatibleState, Constraint c){
		int newTransitions = 0;
		State newState = new State(compatibleState);
		int[] newVector = newState.getVariableVector();		
		//List<VarNode> variableList = c.getVariableList();
		List<Integer> variableList = c.getVariableList();
		List<Integer> valueList = c.getValueList();
		
		//int[] compatibleVector = compatibleState.getVector();
		// TODO: Need to update tranVector here? 
		for(int i = 0; i < variableList.size(); i++){
			//int index = variableList.get(i).getIndex(compatibleVector);
			int index = variableList.get(i);
			newVector[index] = valueList.get(i);
		}
		updateTranVectorByConstraint(newState.getLpn(), newState.getTranVector(), newState.getMarking(), newVector);
		State nextState = sg.addState(newState);
		if(nextState == newState){
			int result = 0;
			sg.addFrontierState(nextState);
			// TODO: Need to consider the "Sticky sematics"
//			if(Options.getStickySemantics()){
//				result = sg.constrStickyFindSG(nextState, sg.getEnabled(compatibleState));
//			}
//			else{
				result = sg.constrFindSG(nextState);
//			}
			if(result < 0) 
				return newTransitions;
			newTransitions += result;
		}
		Transition constraintTran = c.getLpnTransition();
		sg.addStateTran(compatibleState, constraintTran, nextState);
    	newTransitions++;
		return newTransitions;
	}
	
	@SuppressWarnings("unused")
	private static String printTranVecotr(boolean[] tranVector) {
		String tranVecStr = "[";
		for (boolean i : tranVector) {
			tranVecStr = tranVecStr + "," + i;
		}
		tranVecStr = tranVecStr + "]";
		return tranVecStr;
	}

	/**
     * Update tranVector due to application of a constraint. Only vector of a state can change due to a constraint, and
     * therefore the tranVector can potentially change.
     * @param lpn
     * @param enabledTranBeforeConstr
     * @param marking
     * @param newVector
     * @return
     */
    public static void updateTranVectorByConstraint(LPN lpn, boolean[] enabledTran,
			int[] marking, int[] newVector) {    	
        // find newly enabled transition(s) based on the updated variables vector.
        for (Transition tran : lpn.getAllTransitions()) {
        	boolean needToUpdate = true;
        	String tranName = tran.getLabel();
    		int tranIndex = tran.getIndex();
    		if (Options.getDebugMode()) {
//    			System.out.println("Checking " + tranName);
    		}
    		if (lpn.getEnablingTree(tranName) != null 
    				&& lpn.getEnablingTree(tranName).evaluateExpr(lpn.getAllVarsWithValuesAsString(newVector)) == 0.0) {
    			if (Options.getDebugMode()) {
    				System.out.println(tran.getLabel() + " " + "Enabling condition is false");    			
    			}					
    			if (enabledTran[tranIndex] && !tran.isPersistent())
    				enabledTran[tranIndex] = false;
    			continue;
    		}
    		if (lpn.getTransitionRateTree(tranName) != null 
    				&& lpn.getTransitionRateTree(tranName).evaluateExpr(lpn.getAllVarsWithValuesAsString(newVector)) == 0.0) {
    			if (Options.getDebugMode()) {
    				System.out.println("Rate is zero");
    			}					
    			continue;
    		}
    		if (lpn.getPreset(tranName) != null && lpn.getPreset(tranName).length != 0) {
    			for (int place : lpn.getPresetIndex(tranName)) {
    				if (marking[place]==0) {
    					if (Options.getDebugMode()) {
    						System.out.println(tran.getLabel() + " " + "Missing a preset token");
    					}
    					needToUpdate = false;
    					break;
    				}
    			}
    		}
			if (needToUpdate) {            	
    			enabledTran[tranIndex] = true;
    			if (Options.getDebugMode()) {
    				System.out.println(tran.getLabel() + " is Enabled.");
    			}					
            }
        }
	}

	private static void constructDstLpnList(StateGraph[] sgArray) {
		for (int i=0; i<sgArray.length; i++) {
			LPN curLPN = sgArray[i].getLpn();
			Transition[] allTrans = curLPN.getAllTransitions();
			for (int j=0; j<allTrans.length; j++) {
				Transition curTran = allTrans[j];
				for (int k=0; k<sgArray.length; k++) {
					curTran.setDstLpnList(sgArray[k].getLpn());
				}
								
			}
		}
	}
}
