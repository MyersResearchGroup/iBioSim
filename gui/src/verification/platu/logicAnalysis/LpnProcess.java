package verification.platu.logicAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import verification.platu.lpn.DualHashMap;

import lpn.parser.LhpnFile;
import lpn.parser.Place;
import lpn.parser.Transition;

public class LpnProcess extends LhpnFile {
	
	private LinkedList<Transition> processTrans = new LinkedList<Transition>();
	private LinkedList<Place> processPlaces = new LinkedList<Place>();
	private int id;
	
	public LpnProcess(Integer procId) {
		id = procId;
	}
	
	private int getLpnProcessId() {
		return id;
	}

	public void addTranToProcess(Transition curTran) {
		processTrans.add(curTran);
	}
	
	public void addPlaceToProcess(Place curPlace) {
		processPlaces.add(curPlace);
	}
	/**
	 * This method assigns at least one sticky transition to each process. Transitions whose preset places are marked are considered to be sticky
	 * for each process. The purpose of assigning sticky transitions is to break all cycles in each process. In general, one sticky transition is
	 * sufficient to break the cycle. However, FloydÕs all-pairs shortest-path algorithm (binary) is used to find additional transition(s) that can
	 * help to cut the cycle(s).
	 */
	public void assignStickyTransitions() {
		// Assign transition(s) with marked post to be sticky
		for (Place p : processPlaces) {
			if (p.isMarked()) {
				if (p.getPreset() != null && p.getPreset().length > 0) {
					Transition[] transitions = p.getPreset();
					for (int i=0; i<transitions.length; i++) {
						transitions[i].setSticky(true);
					}
				}
				else {
					Integer selectedTran; 
					do {
						Random rand = new Random();
						selectedTran = rand.nextInt(processTrans.size());						
					}
					while (processTrans.get(selectedTran).getName().contains("_dummy"));
					processTrans.get(selectedTran).setSticky(true);
				}
			}
		}		
		@SuppressWarnings("unchecked")
		LinkedList<Transition> processTransCopy = (LinkedList<Transition>) processTrans.clone();
		LinkedList<Transition> transToRemove = new LinkedList<Transition>();
		DualHashMap<Transition, Integer> tranIndexInMatrixMap = new DualHashMap<Transition, Integer>();
		int index = -1;
		for (Iterator<Transition> processTransCopyIter = processTransCopy.iterator(); processTransCopyIter.hasNext();) {
			Transition curTran = processTransCopyIter.next();
			if (curTran.isSticky() || curTran.getName().contains("_dummy")) {
				transToRemove.add(curTran);
				continue;
			}			
			index++;
			tranIndexInMatrixMap.put(curTran, index);
		}
		processTransCopy.removeAll(transToRemove);
		// Floyd's all-pairs shortest path 
		ArrayList<ArrayList<Boolean>> matrix = new ArrayList<ArrayList<Boolean>>();
		Integer numCycles = 0;
		do {
			if (processTransCopy.size() == 0) {
				continue;
			}
			ArrayList<Boolean> row = new ArrayList<Boolean>();
			for (int i=0; i<processTransCopy.size(); i++) {
				for (int j=0; j<processTransCopy.size(); j++) {
					row.add(j, false);
				}
				matrix.add(i, (ArrayList<Boolean>) row.clone());
				row.clear();
			}
			// find the next reachable transition(s) from the current transition
			for (int i=0; i<processTransCopy.size(); i++) {
				Transition curTran = processTransCopy.get(i);
				for (Place p : curTran.getPostset()) {
					for (Transition tran : p.getPostset()) {
						if (processTransCopy.contains(tran)) {
							Integer tranIndexInMatrix = tranIndexInMatrixMap.get(tran);
							Integer curTranIndexInMatrix = tranIndexInMatrixMap.get(curTran);
							matrix.get(curTranIndexInMatrix).set(tranIndexInMatrix, true);
						}
					}
				}
			}
			// update reachability
			for (int k=0; k<processTransCopy.size(); k++) {
				for (int i=0; i<processTransCopy.size(); i++) {
					for (int j=0; j<processTransCopy.size(); j++) {
						if (matrix.get(i).get(k) && matrix.get(k).get(j)) {
							matrix.get(i).set(j, true);
						}
					}
				}
			}
			// Check to see if there is still any cycles left. If a transition can reach itself, then there is a loop.
			Transition tranWithBiggestPreset = new Transition();
			for (int i=0; i<matrix.size(); i++) {
				if (matrix.get(i).get(i)) {
					numCycles ++;
					if (tranIndexInMatrixMap.getKey(i).getPreset().length > tranWithBiggestPreset.getPreset().length) {
						tranWithBiggestPreset = tranIndexInMatrixMap.getKey(i);
					}
				}
			}
			tranWithBiggestPreset.setSticky(true);
			// Remove the sticky transition in the matrix.
			Integer tranWithBiggestPresetIndex = tranIndexInMatrixMap.getValue(tranWithBiggestPreset);
			processTransCopy.remove(tranWithBiggestPreset);
			for (int i=0; i<matrix.size(); i++) {
				matrix.get(i).remove(tranWithBiggestPresetIndex);
			}
			matrix.remove(tranWithBiggestPresetIndex);
			numCycles --;
		}
		while (numCycles > 0 && processTransCopy.size() > 0);
	}

	public void printProcWithStickyTrans() {
		System.out.println("*******Sticky Transitions*******");
		System.out.println("Process ID:" + this.getLpnProcessId());
		for (int i=0; i < this.processTrans.size(); i++) {
			if (processTrans.get(i).isSticky()) {
				System.out.println(processTrans.get(i).getName());
			}
		}
		System.out.println("********************");
	}
}
