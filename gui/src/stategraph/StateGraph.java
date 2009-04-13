package stategraph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import lhpn2sbml.parser.ExprTree;
import lhpn2sbml.parser.LHPNFile;

public class StateGraph {
	private HashMap<String, LinkedList<Marking>> stateGraph;
	private LHPNFile lhpn;

	public StateGraph(LHPNFile lhpn) {
		this.lhpn = lhpn;
		stateGraph = new HashMap<String, LinkedList<Marking>>();
		buildStateGraph();
	}

	private void buildStateGraph() {
		ArrayList<String> variables = new ArrayList<String>();
		for (String var : lhpn.getBooleanVars()) {
			variables.add(var);
		}
		boolean[] variableVector = new boolean[variables.size()];
		for (int i = 0; i < variableVector.length; i++) {
			if (lhpn.getInitialVal(variables.get(i)).equals("true")) {
				variableVector[i] = true;
			}
			else {
				variableVector[i] = false;
			}
		}
		ArrayList<String> markedPlaces = new ArrayList<String>();
		HashMap<String, Boolean> places = lhpn.getPlaces();
		for (String place : places.keySet()) {
			if (places.get(place)) {
				markedPlaces.add(place);
			}
		}
		LinkedList<Marking> markings = new LinkedList<Marking>();
		int counter = 0;
		Marking state = new Marking(markedPlaces.toArray(new String[0]), new Marking[0], "S"
				+ counter);
		markings.add(state);
		counter++;
		stateGraph.put(vectorToString(variableVector), markings);
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
						copyStateVector(variableVector), state));
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
				if (lhpn.getBoolAssignTree(fire.getTransition(), variables.get(i)) != null) {
					if (evaluateExp(lhpn.getBoolAssignTree(fire.getTransition(), variables.get(i)))
							.equals("true")) {
						variableVector[i] = true;
					}
					else {
						variableVector[i] = false;
					}
				}
			}
			if (!stateGraph.containsKey(vectorToString(variableVector))) {
				markings = new LinkedList<Marking>();
				state = new Marking(markedPlaces.toArray(new String[0]), new Marking[0], "S"
						+ counter);
				markings.add(state);
				fire.getParent().addNextState(state);
				counter++;
				stateGraph.put(vectorToString(variableVector), markings);
				for (String transition : lhpn.getTransitionList()) {
					boolean addToStack = true;
					for (String place : lhpn.getPreset(transition)) {
						if (!markedPlaces.contains(place)) {
							addToStack = false;
						}
					}
					if (addToStack) {
						transitionsToFire
								.push(new Transition(transition, copyArrayList(markedPlaces),
										copyStateVector(variableVector), state));
					}
				}
			}
			else {
				markings = stateGraph.get(vectorToString(variableVector));
				boolean add = true;
				boolean same = true;
				for (Marking mark : markings) {
					for (String place : mark.getMarkings()) {
						if (!markedPlaces.contains(place)) {
							same = false;
						}
					}
					for (String place : markedPlaces) {
						boolean contains = false;
						for (String place2 : mark.getMarkings()) {
							if (place2.equals(place)) {
								contains = true;
							}
						}
						if (!contains) {
							same = false;
						}
					}
					if (same) {
						add = false;
						fire.getParent().addNextState(mark);
					}
					same = true;
				}
				if (add) {
					state = new Marking(markedPlaces.toArray(new String[0]), new Marking[0], "S"
							+ counter);
					markings.add(state);
					fire.getParent().addNextState(state);
					counter++;
					stateGraph.put(vectorToString(variableVector), markings);
					for (String transition : lhpn.getTransitionList()) {
						boolean addToStack = true;
						for (String place : lhpn.getPreset(transition)) {
							if (!markedPlaces.contains(place)) {
								addToStack = false;
							}
						}
						if (addToStack) {
							transitionsToFire.push(new Transition(transition,
									copyArrayList(markedPlaces), copyStateVector(variableVector),
									state));
						}
					}
				}
			}
		}
	}

	private String evaluateExp(ExprTree[] exprTrees) {
		return "false";
	}

	private ArrayList<String> copyArrayList(ArrayList<String> original) {
		ArrayList<String> copy = new ArrayList<String>();
		for (String element : original) {
			copy.add(element);
		}
		return copy;
	}

	private boolean[] copyStateVector(boolean[] original) {
		boolean[] copy = new boolean[original.length];
		for (int i = 0; i < original.length; i++) {
			copy[i] = original[i];
		}
		return copy;
	}

	private String vectorToString(boolean[] vector) {
		String string = "";
		for (boolean b : vector) {
			if (b) {
				string += "1";
			}
			else {
				string += "0";
			}
		}
		return string;
	}

	public void outputStateGraph(String file) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write("digraph G {\n");

			for (String state : stateGraph.keySet()) {
				for (Marking m : stateGraph.get(state)) {
					out.write(m.getID() + " [shape=\"ellipse\",label=\"" + state + "\"]\n");
					for (Marking next : m.getNextStates()) {
						out.write(m.getID() + " -> " + next.getID() + "\n");
					}
				}
			}
			out.write("}");
			out.close();
		}
		catch (Exception e) {
			System.err.println("Error outputting state graph as dot file.");
		}
	}

	private class Transition {
		private String transition;
		private boolean[] variableVector;
		private ArrayList<String> markedPlaces;
		private Marking parent;

		private Transition(String transition, ArrayList<String> markedPlaces,
				boolean[] variableVector, Marking parent) {
			this.transition = transition;
			this.markedPlaces = markedPlaces;
			this.variableVector = variableVector;
			this.parent = parent;
		}

		private String getTransition() {
			return transition;
		}

		private ArrayList<String> getMarkedPlaces() {
			return markedPlaces;
		}

		private boolean[] getVariableVector() {
			return variableVector;
		}

		private Marking getParent() {
			return parent;
		}
	}

	private class Marking {
		private String[] markings;
		private Marking[] nextStates;
		private String id;

		private Marking(String[] markings, Marking[] nextStates, String id) {
			this.markings = markings;
			this.nextStates = nextStates;
			this.id = id;
		}

		private String getID() {
			return id;
		}

		private String[] getMarkings() {
			return markings;
		}

		private Marking[] getNextStates() {
			return nextStates;
		}

		private void addNextState(Marking nextState) {
			Marking[] newNextStates = new Marking[nextStates.length + 1];
			for (int i = 0; i < nextStates.length; i++) {
				newNextStates[i] = nextStates[i];
			}
			newNextStates[newNextStates.length - 1] = nextState;
			nextStates = newNextStates;
		}
	}
}
