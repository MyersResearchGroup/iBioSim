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
package edu.utah.ece.async.dataModels.lpn.LpnDecomposition;

import java.util.ArrayList;
import java.util.Iterator;

import edu.utah.ece.async.dataModels.lpn.LPN;
import edu.utah.ece.async.dataModels.lpn.Place;
import edu.utah.ece.async.dataModels.lpn.Transition;
import edu.utah.ece.async.dataModels.lpn.Variable;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class LpnProcess extends LPN {
	
	private ArrayList<Transition> processTrans = new ArrayList<Transition>();
	private ArrayList<Place> processPlaces = new ArrayList<Place>();
	private ArrayList<Variable> processInput = new ArrayList<Variable>();
	private ArrayList<Variable> processOutput = new ArrayList<Variable>();
	private ArrayList<Variable> processInternal = new ArrayList<Variable>();
	private int id;
	private boolean isStateMachine;
	
	public LpnProcess(Integer procId) {
		id = procId;
		isStateMachine = true;
	}
	
	public int getProcessId() {
		return id;
	}

	public void addTranToProcess(Transition curTran) {
		processTrans.add(curTran);
	}
	
	public void addPlaceToProcess(Place curPlace) {
		processPlaces.add(curPlace);
	}
	
//	/**
//	 * This method assigns at least one sticky transition to each process. Transitions whose preset places are marked are considered to be sticky
//	 * for each process. The purpose of assigning sticky transitions is to break all cycles in each process. In general, one sticky transition is
//	 * sufficient to break the cycle. However, Floyd's all-pairs shortest-path algorithm (binary) is used to find additional transition(s) that can
//	 * help to cut the cycle(s).
//	 */
//	@SuppressWarnings("unchecked")
//	public void assignStickyTransitions() {
//		// Assign transition(s) with marked post to be sticky
//		for (Place p : processPlaces) {
//			if (p.isMarked()) {
//				if (p.getPreset() != null && p.getPreset().length > 0) {
//					Transition[] transitions = p.getPreset();
//					for (int i=0; i<transitions.length; i++) {
//						transitions[i].setSticky(true);
//					}
//				}
//				else {
//					Integer selectedTran; 
//					do {
//						Random rand = new Random();
//						selectedTran = rand.nextInt(processTrans.size());						
//					}
//					while (processTrans.get(selectedTran).getName().contains("_dummy"));
//					processTrans.get(selectedTran).setSticky(true);
//				}
//			}
//		}		
//		ArrayList<Transition> processTransCopy = (ArrayList<Transition>) processTrans.clone();
//		ArrayList<Transition> transToRemove = new ArrayList<Transition>();
//		DualHashMap<Transition, Integer> tranIndexInMatrixMap = new DualHashMap<Transition, Integer>();
//		int index = -1;
//		for (Iterator<Transition> processTransCopyIter = processTransCopy.iterator(); processTransCopyIter.hasNext();) {
//			Transition curTran = processTransCopyIter.next();
//			if (curTran.isSticky() || curTran.getName().contains("_dummy")) {
//				transToRemove.add(curTran);
//				continue;
//			}			
//			index++;
//			tranIndexInMatrixMap.put(curTran, index);
//		}
//		processTransCopy.removeAll(transToRemove);
//		// Floyd's all-pairs shortest path 
//		ArrayList<ArrayList<Boolean>> matrix = new ArrayList<ArrayList<Boolean>>();
//		Integer numCycles = 0;
//		do {
//			if (processTransCopy.size() == 0) {
//				continue;
//			}
//			ArrayList<Boolean> row = new ArrayList<Boolean>();
//			for (int i=0; i<processTransCopy.size(); i++) {
//				for (int j=0; j<processTransCopy.size(); j++) {
//					row.add(j, false);
//				}
//				matrix.add(i, (ArrayList<Boolean>) row.clone());
//				row.clear();
//			}
//			// find the next reachable transition(s) from the current transition
//			for (int i=0; i<processTransCopy.size(); i++) {
//				Transition curTran = processTransCopy.get(i);
//				for (Place p : curTran.getPostset()) {
//					for (Transition tran : p.getPostset()) {
//						if (processTransCopy.contains(tran)) {
//							Integer tranIndexInMatrix = tranIndexInMatrixMap.get(tran);
//							Integer curTranIndexInMatrix = tranIndexInMatrixMap.get(curTran);
//							matrix.get(curTranIndexInMatrix).set(tranIndexInMatrix, true);
//						}
//					}
//				}
//			}
//			// update reachability
//			for (int k=0; k<processTransCopy.size(); k++) {
//				for (int i=0; i<processTransCopy.size(); i++) {
//					for (int j=0; j<processTransCopy.size(); j++) {
//						if (matrix.get(i).get(k) && matrix.get(k).get(j)) {
//							matrix.get(i).set(j, true);
//						}
//					}
//				}
//			}
//			// Check to see if there is still any cycles left. If a transition can reach itself, then there is a loop.
//			Transition tranWithBiggestPreset = new Transition();
//			for (int i=0; i<matrix.size(); i++) {
//				if (matrix.get(i).get(i)) {
//					numCycles ++;
//					if (tranIndexInMatrixMap.getKey(i).getPreset().length > tranWithBiggestPreset.getPreset().length) {
//						tranWithBiggestPreset = tranIndexInMatrixMap.getKey(i);
//					}
//				}
//			}
//			tranWithBiggestPreset.setSticky(true);
//			// Remove the sticky transition in the matrix.
//			Integer tranWithBiggestPresetIndex = tranIndexInMatrixMap.getValue(tranWithBiggestPreset);
//			processTransCopy.remove(tranWithBiggestPreset);
//			for (int i=0; i<matrix.size(); i++) {
//				matrix.get(i).remove(tranWithBiggestPresetIndex);
//			}
//			matrix.remove(tranWithBiggestPresetIndex);
//			numCycles --;
//		}
//		while (numCycles > 0 && processTransCopy.size() > 0);
//	}

//	public void printProcWithStickyTrans() {
//		System.out.println("*******Sticky Transitions*******");
//		System.out.println("Process ID:" + this.getProcessId());
//		for (int i=0; i < this.processTrans.size(); i++) {
//			if (processTrans.get(i).isSticky()) {
//				System.out.println(processTrans.get(i).getName() + " " + processTrans.get(i).getIndex());
//			}
//		}
//		System.out.println("********************");
//	}
//	/**
//	 * This method assigns at least one sticky transition to each process. Transitions whose preset places are marked are considered to be sticky
//	 * for each process. The purpose of assigning sticky transitions is to break all cycles in each process. In general, one sticky transition is
//	 * sufficient to break the cycle. However, Floyd�s all-pairs shortest-path algorithm (binary) is used to find additional transition(s) that can
//	 * help to cut the cycle(s).
//	 */
//	@SuppressWarnings("unchecked")
//	public void assignStickyTransitions() {
//		// Assign transition(s) with marked post to be sticky
//		for (Place p : processPlaces) {
//			if (p.isMarked()) {
//				if (p.getPreset() != null && p.getPreset().length > 0) {
//					Transition[] transitions = p.getPreset();
//					for (int i=0; i<transitions.length; i++) {
//						transitions[i].setSticky(true);
//					}
//				}
//				else {
//					Integer selectedTran; 
//					do {
//						Random rand = new Random();
//						selectedTran = rand.nextInt(processTrans.size());						
//					}
//					while (processTrans.get(selectedTran).getName().contains("_dummy"));
//					processTrans.get(selectedTran).setSticky(true);
//				}
//			}
//		}		
//		ArrayList<Transition> processTransCopy = (ArrayList<Transition>) processTrans.clone();
//		ArrayList<Transition> transToRemove = new ArrayList<Transition>();
//		DualHashMap<Transition, Integer> tranIndexInMatrixMap = new DualHashMap<Transition, Integer>();
//		int index = -1;
//		for (Iterator<Transition> processTransCopyIter = processTransCopy.iterator(); processTransCopyIter.hasNext();) {
//			Transition curTran = processTransCopyIter.next();
//			if (curTran.isSticky() || curTran.getName().contains("_dummy")) {
//				transToRemove.add(curTran);
//				continue;
//			}			
//			index++;
//			tranIndexInMatrixMap.put(curTran, index);
//		}
//		processTransCopy.removeAll(transToRemove);
//		// Floyd's all-pairs shortest path 
//		ArrayList<ArrayList<Boolean>> matrix = new ArrayList<ArrayList<Boolean>>();
//		Integer numCycles = 0;
//		do {
//			if (processTransCopy.size() == 0) {
//				continue;
//			}
//			ArrayList<Boolean> row = new ArrayList<Boolean>();
//			for (int i=0; i<processTransCopy.size(); i++) {
//				for (int j=0; j<processTransCopy.size(); j++) {
//					row.add(j, false);
//				}
//				matrix.add(i, (ArrayList<Boolean>) row.clone());
//				row.clear();
//			}
//			// find the next reachable transition(s) from the current transition
//			for (int i=0; i<processTransCopy.size(); i++) {
//				Transition curTran = processTransCopy.get(i);
//				for (Place p : curTran.getPostset()) {
//					for (Transition tran : p.getPostset()) {
//						if (processTransCopy.contains(tran)) {
//							Integer tranIndexInMatrix = tranIndexInMatrixMap.get(tran);
//							Integer curTranIndexInMatrix = tranIndexInMatrixMap.get(curTran);
//							matrix.get(curTranIndexInMatrix).set(tranIndexInMatrix, true);
//						}
//					}
//				}
//			}
//			// update reachability
//			for (int k=0; k<processTransCopy.size(); k++) {
//				for (int i=0; i<processTransCopy.size(); i++) {
//					for (int j=0; j<processTransCopy.size(); j++) {
//						if (matrix.get(i).get(k) && matrix.get(k).get(j)) {
//							matrix.get(i).set(j, true);
//						}
//					}
//				}
//			}
//			// Check to see if there is still any cycles left. If a transition can reach itself, then there is a loop.
//			Transition tranWithBiggestPreset = new Transition();
//			for (int i=0; i<matrix.size(); i++) {
//				if (matrix.get(i).get(i)) {
//					numCycles ++;
//					if (tranIndexInMatrixMap.getKey(i).getPreset().length > tranWithBiggestPreset.getPreset().length) {
//						tranWithBiggestPreset = tranIndexInMatrixMap.getKey(i);
//					}
//				}
//			}
//			tranWithBiggestPreset.setSticky(true);
//			// Remove the sticky transition in the matrix.
//			Integer tranWithBiggestPresetIndex = tranIndexInMatrixMap.getValue(tranWithBiggestPreset);
//			processTransCopy.remove(tranWithBiggestPreset);
//			for (int i=0; i<matrix.size(); i++) {
//				matrix.get(i).remove(tranWithBiggestPresetIndex);
//			}
//			matrix.remove(tranWithBiggestPresetIndex);
//			numCycles --;
//		}
//		while (numCycles > 0 && processTransCopy.size() > 0);
//	}


	public void print() {
		System.out.print("Process ID: " + this.getProcessId() + "\n");
		ArrayList<Transition> trans = this.getProcessTransitions();
		System.out.println("Transitions:");
		for (Iterator<Transition> transIter = trans.iterator(); transIter.hasNext();) {
			System.out.print(transIter.next() + " ");
		}
		System.out.print("\n");
		System.out.println("Places:");
		ArrayList<Place> places = this.getProcessPlaces();
		for (Iterator<Place> placesIter = places.iterator(); placesIter.hasNext();) {
			System.out.print(placesIter.next() + " ");
		}
		System.out.print("\n");
		System.out.println("Inputs:");
		ArrayList<Variable> input = this.getProcessInput();
		for (Iterator<Variable> inputIter = input.iterator(); inputIter.hasNext();) {
			System.out.println(inputIter.next() + " ");
		}
		System.out.print("\n");
		System.out.println("Outputs:");
		ArrayList<Variable> output = this.getProcessOutput();
		for (Iterator<Variable> outputIter = output.iterator(); outputIter.hasNext();) {
			System.out.println(outputIter.next() + " ");
		}
		System.out.print("\n");
		System.out.println("Internals:");
		ArrayList<Variable> internal = this.getProcessInternal();
		for (Iterator<Variable> internalIter = internal.iterator(); internalIter.hasNext();) {
			System.out.println(internalIter.next() + " ");
		}
		System.out.print("\n");
		System.out.println("----------------------------");
	}

	public ArrayList<Place> getProcessPlaces() {
		return processPlaces;
	}

	public ArrayList<Transition> getProcessTransitions() {
		return processTrans;
	}
	
	public ArrayList<Variable> getProcessInput() {
		return processInput;
	}
	
	public ArrayList<Variable> getProcessOutput() {
		return processOutput;
	}
	
	public ArrayList<Variable> getProcessInternal() {
		return processInternal;
	}
	
	public ArrayList<Variable> getProcessVariables() {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		variables.addAll(processInput);
		variables.addAll(processOutput);
		variables.addAll(processInternal);
		return variables;
	}
	
	public int getProcessVarSize() {
		return processInternal.size() + processInput.size() + processOutput.size();
	}

	public LPN buildLPN(LPN lpnProc) {
		// Places
		for (int i=0; i< this.getProcessPlaces().size(); i++) {
			Place p = this.getProcessPlaces().get(i);
			lpnProc.addPlace(p.getName(), p.isMarked());
		}
		// Transitions
		for (int i=0; i< this.getProcessTransitions().size(); i++) {
			Transition t = this.getProcessTransitions().get(i);
			t.setIndex(i);
			lpnProc.addTransition(t);
			
		}
		// Inputs
		for (int i=0; i< this.getProcessInput().size(); i++) {
			Variable var = this.getProcessInput().get(i);
			lpnProc.addInput(var.getName(), var.getType(), var.getInitValue());
		}
		// Outputs
		for (int i=0; i< this.getProcessOutput().size(); i++) {
			Variable var = this.getProcessOutput().get(i);
			lpnProc.addOutput(var.getName(), var.getType(), var.getInitValue());
		}
		// Internal
		for (int i=0; i< this.getProcessInternal().size(); i++) {
			Variable var = this.getProcessInternal().get(i);
			lpnProc.addInternal(var.getName(), var.getType(), var.getInitValue());
		}
		return lpnProc;
	}

	public void setStateMachineFlag(boolean isSM) {
		isStateMachine = isSM;		
	}
	
	public boolean getStateMachineFlag() {
		return isStateMachine;
	}
	
}
