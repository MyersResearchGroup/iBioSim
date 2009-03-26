package stategraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import lhpn2sbml.parser.LHPNFile;

public class StateGraph {
	private HashMap<String[], LinkedList<String[]>> stateGraph;
	private LHPNFile lhpn;

	public StateGraph(LHPNFile lhpn) {
		this.lhpn = lhpn;
		stateGraph = new HashMap<String[], LinkedList<String[]>>();
		buildStateGraph();
	}

	private void buildStateGraph() {
		ArrayList<String> variables = new ArrayList<String>();
		for (String var : lhpn.getBooleanVars()) {
			variables.add(var);
		}
		for (String var : lhpn.getContVars()) {
			variables.add(var);
		}
		for (String var : lhpn.getIntVars()) {
			variables.add(var);
		}
		String[] variableVector = new String[variables.size()];
		for (int i = 0; i < variableVector.length; i++) {
			variableVector[i] = evaluateExp(lhpn.getInitialVal(variables.get(i)));
		}
		ArrayList<String> markedPlaces = new ArrayList<String>();
		HashMap<String, Boolean> places = lhpn.getPlaces();
		for (String place : places.keySet()) {
			if (places.get(place)) {
				markedPlaces.add(place);
			}
		}
		LinkedList<String[]> markings = new LinkedList<String[]>();
		markings.add(markedPlaces.toArray(new String[0]));
		stateGraph.put(variableVector, markings);
		Stack<Transition> transitionsToFire = new Stack<Transition>();
		for (String transition : lhpn.getTransitionList()) {
			boolean addToStack = true;
			for (String place : lhpn.getPreset(transition)) {
				if (!markedPlaces.contains(place)) {
					addToStack = false;
				}
			}
			if (addToStack) {
				transitionsToFire.push(new Transition(transition, copyArrayList(markedPlaces),
						copyArray(variableVector)));
			}
		}
		while (transitionsToFire.size() != 0) {
			Transition fire = transitionsToFire.pop();
			markedPlaces = fire.getMarkedPlaces();
			variableVector = fire.getVariableVector();
			for (String place : lhpn.getPreset(fire.getTransition())) {
				markedPlaces.remove(place);
			}
			for (String place : lhpn.getPostset(fire.getTransition())) {
				markedPlaces.add(place);
			}
			for (int i = 0; i < variables.size(); i++) {
				if (lhpn.getBoolAssign(fire.getTransition(), variables.get(i)) != null) {
					variableVector[i] = evaluateExp(lhpn.getBoolAssign(fire.getTransition(),
							variables.get(i)));
				}
				if (lhpn.getContAssign(fire.getTransition(), variables.get(i)) != null) {
					variableVector[i] = evaluateExp(lhpn.getContAssign(fire.getTransition(),
							variables.get(i)));
				}
				if (lhpn.getIntAssign(fire.getTransition(), variables.get(i)) != null) {
					variableVector[i] = evaluateExp(lhpn.getIntAssign(fire.getTransition(),
							variables.get(i)));
				}
			}
			if (!stateGraph.containsKey(variableVector)) {
				markings = new LinkedList<String[]>();
				markings.add(markedPlaces.toArray(new String[0]));
				stateGraph.put(variableVector, markings);
				for (String transition : lhpn.getTransitionList()) {
					boolean addToStack = true;
					for (String place : lhpn.getPreset(transition)) {
						if (!markedPlaces.contains(place)) {
							addToStack = false;
						}
					}
					if (addToStack) {
						transitionsToFire.push(new Transition(transition,
								copyArrayList(markedPlaces), copyArray(variableVector)));
					}
				}
			}
			else {
				markings = stateGraph.get(variableVector);
				boolean add = false;
				for (String[] mark : markings) {
					for (String place : mark) {
						if (!markedPlaces.contains(place)) {
							add = true;
						}
					}
					for (String place : markedPlaces) {
						boolean contains = false;
						for (String place2 : mark) {
							if (place2.equals(place)) {
								contains = true;
							}
						}
						if (!contains) {
							add = true;
						}
					}
				}
				if (add) {
					markings.add(markedPlaces.toArray(new String[0]));
					stateGraph.put(variableVector, markings);
					for (String transition : lhpn.getTransitionList()) {
						boolean addToStack = true;
						for (String place : lhpn.getPreset(transition)) {
							if (!markedPlaces.contains(place)) {
								addToStack = false;
							}
						}
						if (addToStack) {
							transitionsToFire.push(new Transition(transition,
									copyArrayList(markedPlaces), copyArray(variableVector)));
						}
					}
				}
			}
		}
	}

	private String evaluateExp(String exp) {
		return exp;
	}

	private ArrayList<String> copyArrayList(ArrayList<String> original) {
		ArrayList<String> copy = new ArrayList<String>();
		for (String element : original) {
			copy.add(element);
		}
		return copy;
	}

	private String[] copyArray(String[] original) {
		String[] copy = new String[original.length];
		for (int i = 0; i < original.length; i++) {
			copy[i] = original[i];
		}
		return copy;
	}

	private class Transition {
		private String transition;
		private String[] variableVector;
		private ArrayList<String> markedPlaces;

		private Transition(String transition, ArrayList<String> markedPlaces,
				String[] variableVector) {
			this.transition = transition;
			this.markedPlaces = markedPlaces;
			this.variableVector = variableVector;
		}

		private String getTransition() {
			return transition;
		}

		private ArrayList<String> getMarkedPlaces() {
			return markedPlaces;
		}

		private String[] getVariableVector() {
			return variableVector;
		}
	}
}
