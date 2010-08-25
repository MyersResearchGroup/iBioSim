package stategraph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import lhpn2sbml.parser.ExprTree;
import lhpn2sbml.parser.LhpnFile;

public class StateGraph implements Runnable {
	private HashMap<String, LinkedList<State>> stateGraph;
	private ArrayList<String> variables;
	private LhpnFile lhpn;
	private boolean stop;
	private String markovResults;

	public StateGraph(LhpnFile lhpn) {
		this.lhpn = lhpn;
		stop = false;
		markovResults = null;
	}

	public void buildStateGraph() {
		stateGraph = new HashMap<String, LinkedList<State>>();
		variables = new ArrayList<String>();
		for (String var : lhpn.getBooleanVars()) {
			variables.add(var);
		}
		for (String var : lhpn.getIntVars()) {
			variables.add(var);
		}
		HashMap<String, String> allVariables = new HashMap<String, String>();
		for (String var : lhpn.getBooleanVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getContVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getIntVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		ArrayList<String> markedPlaces = new ArrayList<String>();
		for (String place : lhpn.getPlaceList()) {
			if (lhpn.getPlace(place).isMarked()) {
				markedPlaces.add(place);
			}
		}
		LinkedList<State> markings = new LinkedList<State>();
		int counter = 0;
		State state = new State(markedPlaces.toArray(new String[0]), new StateTransitionPair[0],
				"S" + counter, createStateVector(variables, allVariables),
				copyAllVariables(allVariables));
		markings.add(state);
		counter++;
		stateGraph.put(createStateVector(variables, allVariables), markings);
		Stack<Transition> transitionsToFire = new Stack<Transition>();
		for (String transition : lhpn.getTransitionList()) {
			boolean addToStack = true;
			if (lhpn.getEnablingTree(transition) != null
					&& lhpn.getEnablingTree(transition).evaluateExp(allVariables) == 0.0) {
				addToStack = false;
			}
			if (lhpn.getTransitionRateTree(transition) != null
					&& lhpn.getTransitionRateTree(transition).evaluateExp(allVariables) == 0.0) {
				addToStack = false;
			}
			if (lhpn.getPreset(transition).length != 0) {
				for (String place : lhpn.getPreset(transition)) {
					if (!markedPlaces.contains(place)) {
						addToStack = false;
					}
				}
			}
			else {
				addToStack = false;
			}
			if (addToStack) {
				transitionsToFire.push(new Transition(transition, copyArrayList(markedPlaces),
						state));
			}
		}
		while (transitionsToFire.size() != 0 && !stop) {
			Transition fire = transitionsToFire.pop();
			markedPlaces = fire.getMarkedPlaces();
			allVariables = copyAllVariables(fire.getParent().getVariables());
			for (String place : lhpn.getPreset(fire.getTransition())) {
				markedPlaces.remove(place);
			}
			for (String place : lhpn.getPostset(fire.getTransition())) {
				markedPlaces.add(place);
			}
			for (String key : allVariables.keySet()) {
				if (lhpn.getBoolAssignTree(fire.getTransition(), key) != null) {
					double eval = lhpn.getBoolAssignTree(fire.getTransition(), key).evaluateExp(
							allVariables);
					if (eval == 0.0) {
						allVariables.put(key, "false");
					}
					else {
						allVariables.put(key, "true");
					}
				}
				if (lhpn.getContAssignTree(fire.getTransition(), key) != null) {
					allVariables.put(key, ""
							+ lhpn.getContAssignTree(fire.getTransition(), key).evaluateExp(
									allVariables));
				}
				if (lhpn.getIntAssignTree(fire.getTransition(), key) != null) {
					allVariables.put(key, ""
							+ ((int) lhpn.getIntAssignTree(fire.getTransition(), key).evaluateExp(
									allVariables)));
				}
			}
			if (!stateGraph.containsKey(createStateVector(variables, allVariables))) {
				markings = new LinkedList<State>();
				state = new State(markedPlaces.toArray(new String[0]), new StateTransitionPair[0],
						"S" + counter, createStateVector(variables, allVariables),
						copyAllVariables(allVariables));
				markings.add(state);
				fire.getParent().addNextState(state, fire.getTransition());
				counter++;
				stateGraph.put(createStateVector(variables, allVariables), markings);
				for (String transition : lhpn.getTransitionList()) {
					boolean addToStack = true;
					if (lhpn.getEnablingTree(transition) != null
							&& lhpn.getEnablingTree(transition).evaluateExp(allVariables) == 0.0) {
						addToStack = false;
					}
					if (lhpn.getTransitionRateTree(transition) != null
							&& lhpn.getTransitionRateTree(transition).evaluateExp(allVariables) == 0.0) {
						addToStack = false;
					}
					if (lhpn.getPreset(transition).length != 0) {
						for (String place : lhpn.getPreset(transition)) {
							if (!markedPlaces.contains(place)) {
								addToStack = false;
							}
						}
					}
					else {
						addToStack = false;
					}
					if (addToStack) {
						transitionsToFire.push(new Transition(transition,
								copyArrayList(markedPlaces), state));
					}
				}
			}
			else {
				markings = stateGraph.get(createStateVector(variables, allVariables));
				boolean add = true;
				boolean same = true;
				for (State mark : markings) {
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
						fire.getParent().addNextState(mark, fire.getTransition());
					}
					same = true;
				}
				if (add) {
					state = new State(markedPlaces.toArray(new String[0]),
							new StateTransitionPair[0], "S" + counter, createStateVector(variables,
									allVariables), copyAllVariables(allVariables));
					markings.add(state);
					fire.getParent().addNextState(state, fire.getTransition());
					counter++;
					stateGraph.put(createStateVector(variables, allVariables), markings);
					for (String transition : lhpn.getTransitionList()) {
						boolean addToStack = true;
						if (lhpn.getEnablingTree(transition) != null
								&& lhpn.getEnablingTree(transition).evaluateExp(allVariables) == 0.0) {
							addToStack = false;
						}
						if (lhpn.getTransitionRateTree(transition) != null
								&& lhpn.getTransitionRateTree(transition).evaluateExp(allVariables) == 0.0) {
							addToStack = false;
						}
						if (lhpn.getPreset(transition).length != 0) {
							for (String place : lhpn.getPreset(transition)) {
								if (!markedPlaces.contains(place)) {
									addToStack = false;
								}
							}
						}
						else {
							addToStack = false;
						}
						if (addToStack) {
							transitionsToFire.push(new Transition(transition,
									copyArrayList(markedPlaces), state));
						}
					}
				}
			}
		}
	}
	
	public boolean canPerformMarkovianAnalysis() {
		for(String trans : lhpn.getTransitionList()) {
			if (!lhpn.isExpTransitionRateTree(trans)) {
				return false;
			}
		}
		if (lhpn.getContVars().length > 0) {
			return false;
		}
		return true;
	}

	public boolean performMarkovianAnalysis(ArrayList<String> conditions) {
		if (!canPerformMarkovianAnalysis()) {
			return false;
		}
		else {
			State initial = getInitialState();
			if (initial != null && !stop) {
				resetColorsForMarkovianAnalysis();
				int period = findPeriod(initial);
				if (period == 0) {
					period = 1;
				}
				int step = 0;
				initial.setCurrentProb(1.0);
				double tolerance = 0.01;
				boolean done = false;
				if (!stop) {
					do {
						step++;
						step = step % period;
						for (String state : stateGraph.keySet()) {
							for (State m : stateGraph.get(state)) {
								if (m.getColor() % period == step) {
									double nextProb = 0.0;
									for (StateTransitionPair prev : m.getPrevStatesWithTrans()) {
										double transProb = 0.0;
										if (lhpn.getTransitionRateTree(prev.getTransition()) != null) {
											transProb = lhpn.getTransitionRateTree(
													prev.getTransition()).evaluateExp(
													prev.getState().getVariables());
										}
										else {
											transProb = 1.0;
										}
										double transitionSum = prev.getState().getTransitionSum();
										if (transitionSum != 0) {
											transProb = (transProb / transitionSum);
										}
										nextProb += (prev.getState().getCurrentProb() * transProb);
										if (stop) {
											return false;
										}
									}
									if (nextProb != 0.0) {
										m.setNextProb(nextProb);
									}
								}
								if (stop) {
									return false;
								}
							}
							if (stop) {
								return false;
							}
						}
						boolean change = false;
						for (String state : stateGraph.keySet()) {
							for (State m : stateGraph.get(state)) {
								if (m.getColor() % period == step) {
									if ((m.getCurrentProb() != 0)
											&& (Math.abs(((m.getCurrentProb() - m.getNextProb()))
													/ m.getCurrentProb()) > tolerance)) {
										change = true;
									}
									else if (m.getCurrentProb() == 0 && m.getNextProb() > tolerance) {
										change = true;
									}
									m.setCurrentProbToNext();
								}
								if (stop) {
									return false;
								}
							}
							if (stop) {
								return false;
							}
						}
						if (!change) {
							done = true;
						}
					}
					while (!done && !stop);
				}
				if (!stop) {
					double totalProb = 0.0;
					for (String state : stateGraph.keySet()) {
						for (State m : stateGraph.get(state)) {
							double transitionSum = m.getTransitionSum();
							if (transitionSum != 0.0) {
								m.setCurrentProb((m.getCurrentProb() / period) / transitionSum);
							}
							totalProb += m.getCurrentProb();
							if (stop) {
								return false;
							}
						}
						if (stop) {
							return false;
						}
					}
					for (String state : stateGraph.keySet()) {
						for (State m : stateGraph.get(state)) {
							if (totalProb != 0.0) {
								m.setCurrentProb(m.getCurrentProb() / totalProb);
							}
							if (stop) {
								return false;
							}
						}
						if (stop) {
							return false;
						}
					}
					resetColors();
					HashMap<String, Double> output = new HashMap<String, Double>();
					if (conditions != null && !stop) {
						for (String s : conditions) {
							double prob = 0;
							for (String ss : s.split("&&")) {
								if (ss.split("->").length == 2) {
									String[] states = ss.split("->");
									for (String state : stateGraph.keySet()) {
										for (State m : stateGraph.get(state)) {
											ExprTree expr = new ExprTree(lhpn);
											expr.token = expr.intexpr_gettok(states[0]);
											expr.intexpr_L(states[0]);
											if (expr.evaluateExp(m.getVariables()) == 1.0) {
												for (StateTransitionPair nextState : m
														.getNextStatesWithTrans()) {
													ExprTree nextExpr = new ExprTree(lhpn);
													nextExpr.token = nextExpr
															.intexpr_gettok(states[1]);
													nextExpr.intexpr_L(states[1]);
													if (nextExpr.evaluateExp(nextState.getState()
															.getVariables()) == 1.0) {
														prob += (m.getCurrentProb() * (lhpn
																.getTransitionRateTree(
																		nextState.getTransition())
																.evaluateExp(m.getVariables()) / m
																.getTransitionSum()));
													}
												}
												if (stop) {
													return false;
												}
											}
											if (stop) {
												return false;
											}
										}
										if (stop) {
											return false;
										}
									}
								}
								else {
									for (String state : stateGraph.keySet()) {
										for (State m : stateGraph.get(state)) {
											ExprTree expr = new ExprTree(lhpn);
											expr.token = expr.intexpr_gettok(ss);
											expr.intexpr_L(ss);
											if (expr.evaluateExp(m.getVariables()) == 1.0) {
												prob += m.getCurrentProb();
											}
											if (stop) {
												return false;
											}
										}
										if (stop) {
											return false;
										}
									}
								}
								if (stop) {
									return false;
								}
							}
							output.put(s, prob);
							if (stop) {
								return false;
							}
						}
						String result1 = "#total";
						String result2 = "1.0";
						for (String s : output.keySet()) {
							result1 += " " + s;
							result2 += " " + output.get(s);
						}
						markovResults = result1 + "\n" + result2 + "\n";
					}
				}
			}
			return true;
		}
	}

	public String getMarkovResults() {
		return markovResults;
	}

	private int findPeriod(State state) {
		if (stop) {
			return 0;
		}
		int period = 0;
		int color = 0;
		state.setColor(color);
		color = state.getColor() + 1;
		Queue<State> unVisitedStates = new LinkedList<State>();
		for (State s : state.getNextStates()) {
			if (s.getColor() == -1) {
				s.setColor(color);
				unVisitedStates.add(s);
			}
			else {
				if (period == 0) {
					period = (state.getColor() - s.getColor() + 1);
				}
				else {
					period = gcd(state.getColor() - s.getColor() + 1, period);
				}
			}
			if (stop) {
				return 0;
			}
		}
		while (!unVisitedStates.isEmpty() && !stop) {
			state = unVisitedStates.poll();
			color = state.getColor() + 1;
			for (State s : state.getNextStates()) {
				if (s.getColor() == -1) {
					s.setColor(color);
					unVisitedStates.add(s);
				}
				else {
					if (period == 0) {
						period = (state.getColor() - s.getColor() + 1);
					}
					else {
						period = gcd(state.getColor() - s.getColor() + 1, period);
					}
				}
				if (stop) {
					return 0;
				}
			}
		}
		return period;
	}

	private int gcd(int a, int b) {
		if (b == 0)
			return a;
		return gcd(b, a % b);
	}

	private void resetColorsForMarkovianAnalysis() {
		for (String state : stateGraph.keySet()) {
			for (State m : stateGraph.get(state)) {
				m.setColor(-1);
				if (stop) {
					return;
				}
			}
			if (stop) {
				return;
			}
		}
	}

	public void resetColors() {
		for (String state : stateGraph.keySet()) {
			for (State m : stateGraph.get(state)) {
				m.setColor(0);
				if (stop) {
					return;
				}
			}
			if (stop) {
				return;
			}
		}
	}

	public State getInitialState() {
		HashMap<String, String> allVariables = new HashMap<String, String>();
		for (String var : lhpn.getBooleanVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getContVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getIntVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (State s : stateGraph.get(createStateVector(variables, allVariables))) {
			if (s.getID().equals("S0")) {
				return s;
			}
		}
		return null;
	}

	public HashMap<String, LinkedList<State>> getStateGraph() {
		return stateGraph;
	}

	public int getNumberOfStates() {
		int count = 0;
		for (String s : stateGraph.keySet()) {
			for (int i = 0; i < stateGraph.get(s).size(); i++) {
				count++;
			}
		}
		return count;
	}

	private ArrayList<String> copyArrayList(ArrayList<String> original) {
		ArrayList<String> copy = new ArrayList<String>();
		for (String element : original) {
			copy.add(element);
		}
		return copy;
	}

	private HashMap<String, String> copyAllVariables(HashMap<String, String> original) {
		HashMap<String, String> copy = new HashMap<String, String>();
		for (String s : original.keySet()) {
			copy.put(s, original.get(s));
		}
		return copy;
	}

	private String createStateVector(ArrayList<String> variables,
			HashMap<String, String> allVariables) {
		String vector = "";
		for (String s : variables) {
			if (allVariables.get(s).toLowerCase().equals("true")) {
				vector += "1,";
			}
			else if (allVariables.get(s).toLowerCase().equals("false")) {
				vector += "0,";
			}
			else {
				vector += allVariables.get(s) + ",";
			}
		}
		if (vector.length() > 0) {
			vector = vector.substring(0, vector.length() - 1);
		}
		return vector;
	}

	public void outputStateGraph(String file, boolean withProbs) {
		try {
			NumberFormat num = NumberFormat.getInstance();
			num.setMaximumFractionDigits(6);
			num.setGroupingUsed(false);
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write("digraph G {\n");
			for (String state : stateGraph.keySet()) {
				for (State m : stateGraph.get(state)) {
					if (withProbs) {
						out.write(m.getID() + " [shape=\"ellipse\",label=\"" + m.getID() + "\\n<"
								+ state + ">\\nProb = " + num.format(m.getCurrentProb()) + "\"]\n");
					}
					else {
						out.write(m.getID() + " [shape=\"ellipse\",label=\"" + m.getID() + "\\n<"
								+ state + ">\"]\n");
					}
					for (StateTransitionPair next : m.getNextStatesWithTrans()) {
						/*
						 * System.out.println(m.getID() + " -> " +
						 * next.getState().getID() + " [label=\"" +
						 * next.getTransition() + "\\n");
						 * System.out.println(m.getTransitionSum());
						 * System.out.println(lhpn.getTransitionRateTree(
						 * next.getTransition()).evaluateExp(m.getVariables()));
						 */
						if (lhpn.getTransitionRateTree(next.getTransition()) != null) {
							out.write(m.getID()
									+ " -> "
									+ next.getState().getID()
									+ " [label=\""
									+ next.getTransition()
									+ "\\n"
									+ Double.parseDouble(num.format((lhpn
											.getTransitionRateTree(next.getTransition())
											.evaluateExp(m.getVariables()) /*
																			 * /
																			 * m
																			 * .
																			 * getTransitionSum
																			 * (
																			 * )
																			 */))) + "\"]\n");
						}
						else {
							out.write(m.getID() + " -> " + next.getState().getID() + " [label=\""
									+ next.getTransition() + "\"]\n");
						}
					}
				}
			}
			out.write("}");
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error outputting state graph as dot file.");
		}
	}

	public void stop() {
		stop = true;
	}

	public boolean getStop() {
		return stop;
	}

	private class Transition {
		private String transition;
		private ArrayList<String> markedPlaces;
		private State parent;

		private Transition(String transition, ArrayList<String> markedPlaces, State parent) {
			this.transition = transition;
			this.markedPlaces = markedPlaces;
			this.parent = parent;
		}

		private String getTransition() {
			return transition;
		}

		private ArrayList<String> getMarkedPlaces() {
			return markedPlaces;
		}

		private State getParent() {
			return parent;
		}
	}

	private class StateTransitionPair {
		private String transition;
		private State state;

		private StateTransitionPair(State state, String transition) {
			this.state = state;
			this.transition = transition;
		}

		private State getState() {
			return state;
		}

		private String getTransition() {
			return transition;
		}
	}

	public class State {
		private String[] markings;
		private StateTransitionPair[] nextStates;
		private StateTransitionPair[] prevStates;
		private String stateVector;
		private String id;
		private int color;
		private double currentProb;
		private double nextProb;
		private HashMap<String, String> variables;
		private double transitionSum;

		public State(String[] markings, StateTransitionPair[] nextStates, String id,
				String stateVector, HashMap<String, String> variables) {
			this.markings = markings;
			this.nextStates = nextStates;
			prevStates = new StateTransitionPair[0];
			this.id = id;
			this.stateVector = stateVector;
			color = 0;
			currentProb = 0.0;
			nextProb = 0.0;
			this.variables = variables;
			transitionSum = -1;
		}

		private String getID() {
			return id;
		}

		private HashMap<String, String> getVariables() {
			return variables;
		}

		private String[] getMarkings() {
			return markings;
		}

		private void setCurrentProb(double probability) {
			currentProb = probability;
		}

		private double getCurrentProb() {
			return currentProb;
		}

		private void setNextProb(double probability) {
			nextProb = probability;
		}

		private double getNextProb() {
			return nextProb;
		}

		private void setCurrentProbToNext() {
			currentProb = nextProb;
		}

		private double getTransitionSum() {
			if (transitionSum == -1) {
				transitionSum = 0;
				for (StateTransitionPair next : nextStates) {
					if (lhpn.getTransitionRateTree(next.getTransition()) != null) {
						transitionSum += lhpn.getTransitionRateTree(next.getTransition())
								.evaluateExp(variables);
					}
					else {
						transitionSum += 1.0;
					}
				}
			}
			return transitionSum;
		}

		private StateTransitionPair[] getNextStatesWithTrans() {
			return nextStates;
		}

		private StateTransitionPair[] getPrevStatesWithTrans() {
			return prevStates;
		}

		public State[] getNextStates() {
			ArrayList<State> next = new ArrayList<State>();
			for (StateTransitionPair st : nextStates) {
				next.add(st.getState());
			}
			return next.toArray(new State[0]);
		}

		public State[] getPrevStates() {
			ArrayList<State> next = new ArrayList<State>();
			for (StateTransitionPair st : prevStates) {
				next.add(st.getState());
			}
			return next.toArray(new State[0]);
		}

		public int getColor() {
			return color;
		}

		public void setColor(int color) {
			this.color = color;
		}

		public String getStateVector() {
			return stateVector;
		}

		private void addNextState(State nextState, String transition) {
			StateTransitionPair[] newNextStates = new StateTransitionPair[nextStates.length + 1];
			for (int i = 0; i < nextStates.length; i++) {
				newNextStates[i] = nextStates[i];
			}
			newNextStates[newNextStates.length - 1] = new StateTransitionPair(nextState, transition);
			nextStates = newNextStates;
			nextState.addPreviousState(this, transition);
		}

		private void addPreviousState(State prevState, String transition) {
			StateTransitionPair[] newPrevStates = new StateTransitionPair[prevStates.length + 1];
			for (int i = 0; i < prevStates.length; i++) {
				newPrevStates[i] = prevStates[i];
			}
			newPrevStates[newPrevStates.length - 1] = new StateTransitionPair(prevState, transition);
			prevStates = newPrevStates;
		}
	}

	public void run() {
	}
}
