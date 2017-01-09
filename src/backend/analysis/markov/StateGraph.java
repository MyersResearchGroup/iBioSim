package backend.analysis.markov;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import backend.lpn.parser.ExprTree;
import backend.lpn.parser.LPN;
import backend.lpn.parser.Translator;
import backend.util.dataparser.DataParser;
import frontend.main.Gui;

public class StateGraph implements Runnable {
	// private HashMap<String, LinkedList<State>> stateGraph;

	private ArrayList<State> stateGraph;

	private ArrayList<String> variables;

	private LPN lhpn;

	private boolean stop;

	private String markovResults;

	private DataParser probData;

	private int waitingThreads, threadCount;

	private boolean phase1, phase2;
	
	private double totalUsedMemory, usedMemory;

	public StateGraph(LPN lhpn) {
		this.lhpn = lhpn;
		stop = false;
		markovResults = null;
	}
	
	public Property createProperty(String label, String property) {
		return new Property(label, property);
	}

	public void buildStateGraph(JProgressBar progress) {
		System.gc();
		long initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		stateGraph = new ArrayList<State>();// HashMap<String,
		// LinkedList<State>>();
		HashMap<String, LinkedList<Integer>> stateLocations = new HashMap<String, LinkedList<Integer>>();
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
		// LinkedList<State> markings = new LinkedList<State>();
		LinkedList<Integer> markings = new LinkedList<Integer>();
		int counter = 0;
		State state = new State(markedPlaces.toArray(new String[0]), new StateTransitionPair[0], "S" + counter,
				createStateVector(variables, allVariables), copyAllVariables(allVariables));
		// markings.add(state);
		counter++;
		progress.setString("States found: " + counter);
		stateGraph.add(state);// .put(createStateVector(variables,
		// allVariables), markings);
		markings.add(stateGraph.size() - 1);
		stateLocations.put(createStateVector(variables, allVariables), markings);
		Stack<Transition> transitionsToFire = new Stack<Transition>();
		for (String transition : lhpn.getTransitionList()) {
			boolean addToStack = true;
			if (lhpn.getEnablingTree(transition) != null
					&& lhpn.getEnablingTree(transition).evaluateExpr(allVariables) == 0.0
					&& !state.containsPersistentTransition(transition)) {
				addToStack = false;
			}
			else if (lhpn.getTransition(transition).isPersistent() && !state.containsPersistentTransition(transition)) {
				state.addPersistentTransition(transition);
			}
			if (lhpn.getTransitionRateTree(transition) != null
					&& lhpn.getTransitionRateTree(transition).evaluateExpr(allVariables) == 0.0) {
				addToStack = false;
				state.removePersistentTransition(transition);
			}
			if (lhpn.getPreset(transition).length != 0) {
				for (String place : lhpn.getPreset(transition)) {
					if (!markedPlaces.contains(place)) {
						addToStack = false;
						state.removePersistentTransition(transition);
					}
				}
			}
			else {
				addToStack = false;
				state.removePersistentTransition(transition);
			}

			if (addToStack) {
				transitionsToFire.push(new Transition(transition, copyArrayList(markedPlaces), state));
			}
		}
		while (transitionsToFire.size() != 0 && !stop) {
			Transition fire = transitionsToFire.pop();
			markedPlaces = fire.getMarkedPlaces();
			allVariables = copyAllVariables(fire.getState().getVariables());
			for (String place : lhpn.getPreset(fire.getTransition())) {
				markedPlaces.remove(place);
			}
			for (String place : lhpn.getPostset(fire.getTransition())) {
				markedPlaces.add(place);
			}
			for (String key : allVariables.keySet()) {
				if (lhpn.getBoolAssignTree(fire.getTransition(), key) != null) {
					double eval = lhpn.getBoolAssignTree(fire.getTransition(), key).evaluateExpr(allVariables);
					if (eval == 0.0) {
						allVariables.put(key, "false");
					}
					else {
						allVariables.put(key, "true");
					}
				}
				if (lhpn.getContAssignTree(fire.getTransition(), key) != null) {
					allVariables.put(key,
							"" + lhpn.getContAssignTree(fire.getTransition(), key).evaluateExpr(allVariables));
				}
				if (lhpn.getIntAssignTree(fire.getTransition(), key) != null) {
					allVariables.put(key,
							"" + ((int) lhpn.getIntAssignTree(fire.getTransition(), key).evaluateExpr(allVariables)));
				}
			}
			if (!stateLocations.containsKey(createStateVector(variables, allVariables))) {
				markings = new LinkedList<Integer>();
			}
			else {
				markings = stateLocations.get(createStateVector(variables, allVariables));
			}
			ArrayList<String> transitions = new ArrayList<String>();
			for (String trans : fire.getState().getPersistentTransitions()) {
				if (!fire.getTransition().equals(trans)) {
					transitions.add(trans);
				}
			}
			for (String transition : lhpn.getTransitionList()) {
				if (lhpn.getEnablingTree(transition) != null
						&& lhpn.getEnablingTree(transition).evaluateExpr(allVariables) != 0.0
						&& lhpn.getTransition(transition).isPersistent() && !transitions.contains(transition)) {
					if (lhpn.getPreset(transition).length != 0) {
						boolean add = true;
						for (String place : lhpn.getPreset(transition)) {
							if (!markedPlaces.contains(place)) {
								add = false;
							}
						}
						if (add) {
							transitions.add(transition);
						}
					}
				}
			}
			boolean add = true;
			boolean same = true;
			for (Integer index : markings) {// State mark : stateGraph) {
				State mark = stateGraph.get(index);
				for (String place : mark.getMarkings()) {
					if (!markedPlaces.contains(place)) {
						same = false;
						break;
					}
				}
				for (String place : markedPlaces) {
					boolean contains = false;
					for (String place2 : mark.getMarkings()) {
						if (place2.equals(place)) {
							contains = true;
							break;
						}
					}
					if (!contains) {
						same = false;
						break;
					}
				}
				for (String trans : mark.getPersistentTransitions()) {
					if (!transitions.contains(trans)) {
						same = false;
						break;
					}
				}
				for (String trans : transitions) {
					boolean contains = false;
					for (String trans2 : mark.getPersistentTransitions()) {
						if (trans2.equals(trans)) {
							contains = true;
							break;
						}
					}
					if (!contains) {
						same = false;
						break;
					}
				}
				if (same) {
					add = false;
					fire.getState().addNextState(
							mark,
							lhpn.getTransitionRateTree(fire.getTransition()).evaluateExpr(
									fire.getState().getVariables()), fire.getTransition());
				}
				same = true;
			}
			if (add) {
				state = new State(markedPlaces.toArray(new String[0]), new StateTransitionPair[0], "S" + counter,
						createStateVector(variables, allVariables), copyAllVariables(allVariables));
				// markings.add(state);
				fire.getState().addNextState(state,
						lhpn.getTransitionRateTree(fire.getTransition()).evaluateExpr(fire.getState().getVariables()),
						fire.getTransition());
				ArrayList<String> persistentTrans = new ArrayList<String>();
				for (String trans : fire.getState().getPersistentTransitions()) {
					if (!fire.getTransition().equals(trans)) {
						persistentTrans.add(trans);
					}
				}
				state.setPersistentTransitions(persistentTrans.toArray(new String[0]));
				counter++;
				progress.setString("States found: " + counter);
				stateGraph.add(state);// .put(createStateVector(variables,
				// allVariables), markings);
				markings.add(stateGraph.size() - 1);
				stateLocations.put(createStateVector(variables, allVariables), markings);
				for (String transition : lhpn.getTransitionList()) {
					boolean addToStack = true;
					if (lhpn.getEnablingTree(transition) != null
							&& lhpn.getEnablingTree(transition).evaluateExpr(allVariables) == 0.0
							&& !state.containsPersistentTransition(transition)) {
						addToStack = false;
					}
					else if (lhpn.getTransition(transition).isPersistent()
							&& !state.containsPersistentTransition(transition)) {
						state.addPersistentTransition(transition);
					}
					if (lhpn.getTransitionRateTree(transition) != null
							&& lhpn.getTransitionRateTree(transition).evaluateExpr(allVariables) == 0.0) {
						addToStack = false;
						state.removePersistentTransition(transition);
					}
					if (lhpn.getPreset(transition).length != 0) {
						for (String place : lhpn.getPreset(transition)) {
							if (!markedPlaces.contains(place)) {
								addToStack = false;
								state.removePersistentTransition(transition);
							}
						}
					}
					else {
						addToStack = false;
						state.removePersistentTransition(transition);
					}
					if (addToStack) {
						transitionsToFire.push(new Transition(transition, copyArrayList(markedPlaces), state));
					}
				}
			}
		}
		totalUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		usedMemory = (totalUsedMemory - initialMemory) / 1000000;
		totalUsedMemory = totalUsedMemory /1000000;
	}

	public boolean canPerformMarkovianAnalysis() {
		for (String trans : lhpn.getTransitionList()) {
			if (!lhpn.isExpTransitionRateTree(trans)) {
				JOptionPane.showMessageDialog(Gui.frame, "LPN has transitions without exponential delay.",
						"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			for (String var : lhpn.getVariables()) {
				if (lhpn.isRandomBoolAssignTree(trans, var)) {
					JOptionPane.showMessageDialog(Gui.frame, "LPN has assignments containing random functions.",
							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (lhpn.isRandomContAssignTree(trans, var)) {
					JOptionPane.showMessageDialog(Gui.frame, "LPN has assignments containing random functions.",
							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (lhpn.isRandomIntAssignTree(trans, var)) {
					JOptionPane.showMessageDialog(Gui.frame, "LPN has assignments containing random functions.",
							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		if (lhpn.getContVars().length > 0) {
			JOptionPane.showMessageDialog(Gui.frame, "LPN contains continuous variables.",
					"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private String removeNesting(double error, double timeStep, String prop, JProgressBar progress) {
		if (prop.contains("Pr=?{") || prop.contains("St=?{")) {
			if (progress != null) {
				progress.setString("Determining Sat Sets.");
			}
			int transientPropIndex = prop.indexOf("Pr=?{");
			int steadyStatePropIndex = prop.indexOf("St=?{");
			if (transientPropIndex == -1) {
				transientPropIndex = Integer.MAX_VALUE;
			}
			if (steadyStatePropIndex == -1) {
				steadyStatePropIndex = Integer.MAX_VALUE;
			}
			int index = 0;
			if (transientPropIndex < steadyStatePropIndex) {
				index = transientPropIndex;
			}
			else {
				index = steadyStatePropIndex;
			}
			String newProp = prop.substring(0, index);
			String cond = prop.substring(index);
			String nest;
			if (transientPropIndex < steadyStatePropIndex) {
				nest = "Pr=?{";
			}
			else {
				nest = "St=?{";
			}
			int braces = 1;
			for (int i = 5; i < cond.length(); i++) {
				char c = cond.charAt(i);
				if (c == '{') {
					braces++;
				}
				else if (c == '}') {
					braces--;
				}
				nest += c;
				index = i;
				if (braces == 0) {
					break;
				}
			}
			index++;
			cond = cond.substring(index);
			String check = nest.substring(5, nest.length() - 1);
			if (check.contains("Pr=?{") || check.contains("St=?{")) {
				if (transientPropIndex < steadyStatePropIndex) {
					nest = "Pr=?{" + removeNesting(error, timeStep, check, progress) + "}";
				}
				else {
					nest = "St=?{" + removeNesting(error, timeStep, check, progress) + "}";
				}
			}
			newProp += determineNestedProbability(error, timeStep, nest)
					+ removeNesting(error, timeStep, cond, progress);
			return newProp;
		}
		return prop;
	}

	public boolean performTransientMarkovianAnalysis(double timeLimit, double timeStep, double printInterval,
			double error, String[] condition, JProgressBar progress, boolean globallyTrue) {
		if (!canPerformMarkovianAnalysis()) {
			stop = true;
			return false;
		}
		else if (condition != null) {
			condition[0] = removeNesting(error, timeStep, condition[0], progress);
			condition[1] = removeNesting(error, timeStep, condition[1], progress);
			progress.setString(null);
			double nextPrint = printInterval;
			if (condition[3].equals("inf")) {
				progress.setIndeterminate(true);
			}
			else {
				ExprTree expr = new ExprTree(lhpn);
				expr.token = expr.intexpr_gettok(condition[3]);
				expr.intexpr_L(condition[3]);
				progress.setMaximum((int) expr.evaluateExpr(null));
			}
			enableAllTransitions();
			State initial = getInitialState();
			if (initial != null) {
				for (State m : stateGraph) {
					m.setCurrentProb(0.0);
					m.setPiProb(0.0);
				}
				initial.setCurrentProb(1.0);
				initial.setPiProb(1.0);
			}
			double lowerbound = 0;
			if (!condition[2].equals("")) {
				ExprTree expr = new ExprTree(lhpn);
				expr.token = expr.intexpr_gettok(condition[2]);
				expr.intexpr_L(condition[2]);
				// lowerbound = Math.min(expr.evaluateExpr(null), timeLimit);
				lowerbound = expr.evaluateExpr(null);
			}
			double Gamma;
			ArrayList<String> dataLabels = new ArrayList<String>();
			dataLabels.add("time");
			// dataLabels.add("~(" + condition[0] + ")&~(" + condition[1] +
			// ")");
			dataLabels.add("Failure");
			// dataLabels.add(condition[1]);
			dataLabels.add("Success");
			ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
			ArrayList<Double> temp = new ArrayList<Double>();
			temp.add(0.0);
			data.add(temp);
			temp = new ArrayList<Double>();
			double failureProb = 0;
			for (State m : stateGraph) {
				ExprTree failureExpr = new ExprTree(lhpn);
				if (lowerbound == 0) {
					if (!condition[0].equals("true")) {
						failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~(" + condition[1]
								+ ")");
						failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
					}
				}
				else if (!condition[0].equals("true")) {
					failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")");
					failureExpr.intexpr_L("~(" + condition[0] + ")");
				}
				if (failureExpr.evaluateExpr(m.getVariables()) == 1.0) {
					failureProb += m.getCurrentProb();
				}
			}
			temp.add(failureProb * 100);
			data.add(temp);
			// if (globallyTrue) {
			double successProb = 0;
			if (lowerbound == 0) {
				for (State m : stateGraph) {
					ExprTree successExpr = new ExprTree(lhpn);
					successExpr.token = successExpr.intexpr_gettok(condition[1]);
					successExpr.intexpr_L(condition[1]);
					if (successExpr.evaluateExpr(m.getVariables()) == 1.0) {
						successProb += m.getCurrentProb();
					}
				}
			}
			temp = new ArrayList<Double>();
			temp.add(successProb * 100);
			data.add(temp);
			// }
			// else {
			// temp = new ArrayList<Double>();
			// temp.add(0.0);
			// data.add(temp);
			// }
			probData = new DataParser(dataLabels, data);
			if (initial != null) {
				if (!condition[2].equals("")) {
					if (!condition[0].equals("true")) {
						pruneStateGraph("~(" + condition[0] + ")");
					}
					// Compute Gamma
					Gamma = 0;
					for (State m : stateGraph) {
						Gamma = Math.max(m.getTransitionSum(0.0, null), Gamma);
					}
					// Compute K
					int K = 0;
					double xi = 1;
					double delta = 1;
					double eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * timeStep)));
					while (delta < eta) {
						K = K + 1;
						xi = xi * ((Gamma * timeStep) / K);
						delta = delta + xi;
					}
					double step = Math.min(Math.min(timeStep, lowerbound), nextPrint);
					for (double i = 0; i < lowerbound; i += step) {
						step = Math.min(Math.min(timeStep, lowerbound - i), nextPrint - i);
						if (step != timeStep) {
							// Compute K
							K = 0;
							xi = 1;
							delta = 1;
							eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * step)));
							while (delta < eta) {
								K = K + 1;
								xi = xi * ((Gamma * step) / K);
								delta = delta + xi;
							}
						}
						if (!performTransientMarkovianAnalysis(step, Gamma, K, progress)) {
							return false;
						}
						double prob = 0;
						// for (String state : stateGraph.keySet()) {
						for (State m : stateGraph) {
							// for (State m : stateGraph.get(state)) {
							ExprTree expr = new ExprTree(lhpn);
							expr.token = expr.intexpr_gettok("~(" + condition[0] + ")");
							expr.intexpr_L("~(" + condition[0] + ")");
							if (expr.evaluateExpr(m.getVariables()) == 1.0) {
								prob += m.getCurrentProb();
							}
							// }
						}
						if (i + step == nextPrint) {
							probData.getData().get(0).add(nextPrint);
							probData.getData().get(1).add(prob * 100);
							probData.getData().get(2).add(0.0);
							nextPrint += printInterval;
						}
					}
				}
				else {
					if (!condition[0].equals("true")) {
						pruneStateGraph("~(" + condition[0] + ")");
					}
				}
				double upperbound;
				if (condition[3].equals("inf")) {
					upperbound = -1;
				}
				else {
					ExprTree expr = new ExprTree(lhpn);
					expr.token = expr.intexpr_gettok(condition[3]);
					expr.intexpr_L(condition[3]);
					// upperbound = Math.min(expr.evaluateExpr(null) -
					// lowerbound, timeLimit - lowerbound);
					upperbound = expr.evaluateExpr(null) - lowerbound;
				}
				if (globallyTrue) {
					pruneStateGraph("~(" + condition[1] + ")");
				}
				else {
					pruneStateGraph(condition[1]);
				}
				if (upperbound == -1) {
					ArrayList<Property> conditions = new ArrayList<Property>();
					if (!condition[0].equals("true")) {
						conditions.add(new Property("Failure", "~(" + condition[0] + ")&~(" + condition[1] + ")"));
					}
					else {
						conditions.add(new Property("Failure", "~(" + condition[1] + ")"));
					}
					conditions.add(new Property("Success", condition[1]));
					return performSteadyStateMarkovianAnalysis(error, conditions, initial, progress);
				}
				// Compute Gamma
				Gamma = 0;
				for (State m : stateGraph) {
					Gamma = Math.max(m.getTransitionSum(0.0, null), Gamma);
				}
				// Compute K
				int K = 0;
				double xi = 1;
				double delta = 1;
				double eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * timeStep)));
				while (delta < eta) {
					K = K + 1;
					xi = xi * ((Gamma * timeStep) / K);
					delta = delta + xi;
				}
				double step = Math.min(Math.min(timeStep, upperbound - lowerbound), nextPrint - lowerbound);
				for (double i = 0; i < upperbound; i += step) {
					step = Math.min(Math.min(timeStep, upperbound - i), nextPrint - lowerbound - i);
					if (step != timeStep) {
						// Compute K
						K = 0;
						xi = 1;
						delta = 1;
						eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * step)));
						while (delta < eta) {
							K = K + 1;
							xi = xi * ((Gamma * step) / K);
							delta = delta + xi;
						}
					}
					if (!performTransientMarkovianAnalysis(step, Gamma, K, progress)) {
						return false;
					}
					failureProb = 0;
					successProb = 0;
					// for (String state : stateGraph.keySet()) {
					// for (State m : stateGraph.get(state)) {
					for (State m : stateGraph) {
						ExprTree failureExpr = new ExprTree(lhpn);
						failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~(" + condition[1]
								+ ")");
						failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
						ExprTree successExpr = new ExprTree(lhpn);
						if (globallyTrue) {
							successExpr.token = successExpr.intexpr_gettok("~(" + condition[1] + ")");
							successExpr.intexpr_L("~(" + condition[1] + ")");
						}
						else {
							successExpr.token = successExpr.intexpr_gettok(condition[1]);
							successExpr.intexpr_L(condition[1]);
						}
						if (failureExpr.evaluateExpr(m.getVariables()) == 1.0) {
							failureProb += m.getCurrentProb();
						}
						else if (successExpr.evaluateExpr(m.getVariables()) == 1.0) {
							successProb += m.getCurrentProb();
						}
						// }
					}
					if (globallyTrue) {
						successProb = 1 - successProb;
					}
					if (lowerbound + i + step == nextPrint) {
						probData.getData().get(0).add(nextPrint);
						probData.getData().get(1).add(failureProb * 100);
						probData.getData().get(2).add(successProb * 100);
						nextPrint += printInterval;
					}
				}
				HashMap<String, Double> output = new HashMap<String, Double>();
				failureProb = 0;
				successProb = 0;
				double timelimitProb = 0;
				for (State m : stateGraph) {
					// for (String state : stateGraph.keySet()) {
					// for (State m : stateGraph.get(state)) {
					ExprTree failureExpr = new ExprTree(lhpn);
					failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~(" + condition[1] + ")");
					failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
					ExprTree successExpr = new ExprTree(lhpn);
					if (globallyTrue) {
						successExpr.token = successExpr.intexpr_gettok("~(" + condition[1] + ")");
						successExpr.intexpr_L("~(" + condition[1] + ")");
					}
					else {
						successExpr.token = successExpr.intexpr_gettok(condition[1]);
						successExpr.intexpr_L(condition[1]);
					}
					if (failureExpr.evaluateExpr(m.getVariables()) == 1.0) {
						failureProb += m.getCurrentProb();
					}
					else if (successExpr.evaluateExpr(m.getVariables()) == 1.0) {
						successProb += m.getCurrentProb();
					}
					else {
						if (!globallyTrue) {
							timelimitProb += m.getCurrentProb();
						}
					}
					// }
				}
				if (globallyTrue) {
					successProb = 1 - successProb;
					timelimitProb = 1 - (failureProb + successProb);
				}
				output.put("Failure", failureProb);
				output.put("Success", successProb);
				output.put("Timelimit", timelimitProb);
				// --- Temp: Print results -----
				System.out.println("------ Curtis's anlaysis engine ------");
				System.out.println("Failure = " + failureProb);
				System.out.println("Success = " + successProb);
				System.out.println("Timelimit = " + timelimitProb);
				// -----------------------------
				String result1 = "#total";
				String result2 = "1.0";
				for (String s : output.keySet()) {
					result1 += " " + s;
					result2 += " " + output.get(s);
				}
				markovResults = result1 + "\n" + result2 + "\n";
				// State init = initial;
				// ArrayList<State> stats = new ArrayList<State>();
				// stats.add(init);
				// ArrayList<State> stats2 = new ArrayList<State>();
				// for (StateTransitionPair pair :init.nextStates) {
				// stats2.add(pair.getState());
				// }
				// while(!stats2.isEmpty()) {
				// State m = stats2.remove(0);
				// stats.add(m);
				// for (StateTransitionPair pair :m.nextStates) {
				// if (!stats.contains(pair.getState()) &&
				// !stats2.contains(pair.getState())) {
				// stats2.add(pair.getState());
				// }
				// }
				// }
				// System.out.println("State count: " + stats.size());
				return true;
			}
			return false;
		}
		else {
			progress.setMaximum((int) timeLimit);
			State initial = getInitialState();
			if (initial != null) {
				initial.setCurrentProb(1.0);
				initial.setPiProb(1.0);
				boolean stop = false;
				// Compute Gamma
				double Gamma = 0;
				for (State m : stateGraph) {
					Gamma = Math.max(m.getTransitionSum(0.0, null), Gamma);
				}
				// Compute K
				int K = 0;
				double xi = 1;
				double delta = 1;
				double eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * timeStep)));
				while (delta < eta) {
					K = K + 1;
					xi = xi * ((Gamma * timeStep) / K);
					delta = delta + xi;
				}
				for (double i = 0; i < timeLimit; i += timeStep) {
					double step = Math.min(timeStep, timeLimit - i);
					if (step == timeLimit - i) {
						// Compute K
						K = 0;
						xi = 1;
						delta = 1;
						eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * step)));
						while (delta < eta) {
							K = K + 1;
							xi = xi * ((Gamma * step) / K);
							delta = delta + xi;
						}
					}
					stop = !performTransientMarkovianAnalysis(step, Gamma, K, progress);
					progress.setValue((int) i);
					if (stop) {
						return false;
					}
				}
				return !stop;
			}
			return false;
		}
	}

	private String determineNestedProbability(double error, double timeStep, String property) {
		String prop = "";
		int braces = 0;
		for (int i = 0; i < property.length(); i++) {
			char c = property.charAt(i);
			if (c == '{') {
				braces++;
			}
			else if (c == '}') {
				braces--;
			}
			if (braces == 1) {
				if ((c == 'G' || c == 'F' || c == 'U') && property.charAt(i + 1) == '[') {
					prop += "P" + c;
				}
				else {
					prop += c;
				}
			}
			else {
				prop += c;
			}
		}
		String[] condition = Translator.getProbpropParts(prop.substring(5, prop.length() - 1));
		String id = "Pr" + Math.abs(prop.hashCode());
		if (!variables.contains(id)) {
			variables.add(id);
			lhpn.addInteger(id, id);
			boolean globallyTrue = false;
			if (prop.contains("PF")) {
				condition[0] = "true";
			}
			else if (prop.contains("PG")) {
				condition[0] = "true";
				globallyTrue = true;
			}
			for (State initial : stateGraph) {
				if (initial.getVariables().keySet().contains(id)) {
					break;
				}
				enableAllTransitions();
				double Gamma;
				for (State m : stateGraph) {
					m.setCurrentProb(0.0);
					m.setPiProb(0.0);
				}
				initial.setCurrentProb(1.0);
				initial.setPiProb(1.0);
				double lowerbound = 0;
				if (!condition[2].equals("")) {
					ExprTree expr = new ExprTree(lhpn);
					expr.token = expr.intexpr_gettok(condition[2]);
					expr.intexpr_L(condition[2]);
					lowerbound = expr.evaluateExpr(null);
					pruneStateGraph("~(" + condition[0] + ")");
					// Compute Gamma
					Gamma = 0;
					for (State m : stateGraph) {
						Gamma = Math.max(m.getTransitionSum(0.0, null), Gamma);
					}
					// Compute K
					int K = 0;
					double xi = 1;
					double delta = 1;
					double eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * timeStep)));
					while (delta < eta) {
						K = K + 1;
						xi = xi * ((Gamma * timeStep) / K);
						delta = delta + xi;
					}
					double step = Math.min(timeStep, lowerbound);
					for (double i = 0; i < lowerbound; i += step) {
						step = Math.min(timeStep, lowerbound - i);
						if (step != timeStep) {
							// Compute K
							K = 0;
							xi = 1;
							delta = 1;
							eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * step)));
							while (delta < eta) {
								K = K + 1;
								xi = xi * ((Gamma * step) / K);
								delta = delta + xi;
							}
						}
						performTransientMarkovianAnalysis(step, Gamma, K, null);
					}
				}
				else {
					pruneStateGraph("~(" + condition[0] + ")");
				}
				double upperbound;
				if (condition[3].equals("inf")) {
					upperbound = -1;
				}
				else {
					ExprTree expr = new ExprTree(lhpn);
					expr.token = expr.intexpr_gettok(condition[3]);
					expr.intexpr_L(condition[3]);
					upperbound = expr.evaluateExpr(null) - lowerbound;
				}
				if (globallyTrue) {
					pruneStateGraph("~(" + condition[1] + ")");
				}
				else {
					pruneStateGraph(condition[1]);
				}
				if (upperbound == -1) {
					ArrayList<Property> conditions = new ArrayList<Property>();
					if (!condition[0].equals("true")) {
						conditions.add(new Property("Failure", "~(" + condition[0] + ")&~(" + condition[1] + ")"));
					}
					else {
						conditions.add(new Property("Failure", "~(" + condition[1] + ")"));
					}
					conditions.add(new Property("Success", condition[1]));
					performSteadyStateMarkovianAnalysis(error, conditions, initial, null);
				}
				// Compute Gamma
				Gamma = 0;
				for (State m : stateGraph) {
					Gamma = Math.max(m.getTransitionSum(0.0, null), Gamma);
				}
				// Compute K
				int K = 0;
				double xi = 1;
				double delta = 1;
				double eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * timeStep)));
				while (delta < eta) {
					K = K + 1;
					xi = xi * ((Gamma * timeStep) / K);
					delta = delta + xi;
				}
				double step = Math.min(timeStep, upperbound - lowerbound);
				for (double i = 0; i < upperbound; i += step) {
					step = Math.min(timeStep, upperbound - i);
					if (step != timeStep) {
						// Compute K
						K = 0;
						xi = 1;
						delta = 1;
						eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * step)));
						while (delta < eta) {
							K = K + 1;
							xi = xi * ((Gamma * step) / K);
							delta = delta + xi;
						}
					}
					performTransientMarkovianAnalysis(step, Gamma, K, null);
				}
				//double failureProb = 0;
				double successProb = 0;
				//double timelimitProb = 0;
				for (State m : stateGraph) {
					// for (String state : stateGraph.keySet()) {
					// for (State m : stateGraph.get(state)) {
					ExprTree failureExpr = new ExprTree(lhpn);
					failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~(" + condition[1] + ")");
					failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
					ExprTree successExpr = new ExprTree(lhpn);
					if (globallyTrue) {
						successExpr.token = successExpr.intexpr_gettok("~(" + condition[1] + ")");
						successExpr.intexpr_L("~(" + condition[1] + ")");
					}
					else {
						successExpr.token = successExpr.intexpr_gettok(condition[1]);
						successExpr.intexpr_L(condition[1]);
					}
					if (failureExpr.evaluateExpr(m.getVariables()) == 1.0) {
						//failureProb += m.getCurrentProb();
					}
					else if (successExpr.evaluateExpr(m.getVariables()) == 1.0) {
						successProb += m.getCurrentProb();
					}
					else {
						if (!globallyTrue) {
							//timelimitProb += m.getCurrentProb();
						}
					}
					// }
				}
				if (globallyTrue) {
					successProb = 1 - successProb;
					//timelimitProb = 1 - (failureProb + successProb);
				}
				initial.addVariable(id, "" + successProb);
			}
		}
		return id;
	}

	private synchronized boolean performTransientMarkovianAnalysis(double timeLimit, double Gamma, int K,
			JProgressBar progress) {
		if (timeLimit == 0.0) {
			return true;
		}
		int progressValue = 0;
		if (progress != null) {
			progressValue = progress.getValue();
		}
		// Approximate pi(t)
		threadCount = 4;
		waitingThreads = threadCount;
		phase1 = true;
		phase2 = false;
		ArrayList<TransientMarkovMatrixMultiplyThread> threads = new ArrayList<TransientMarkovMatrixMultiplyThread>();
		for (int i = 0; i < threadCount; i++) {
			TransientMarkovMatrixMultiplyThread thread = new TransientMarkovMatrixMultiplyThread(this);
			thread.start((i * stateGraph.size()) / threadCount, ((i + 1) * stateGraph.size()) / threadCount, Gamma,
					timeLimit, K);
			threads.add(thread);
		}
		for (int k = 1; k <= K && !stop; k++) {
			while (waitingThreads != 0) {
				try {
					notifyAll();
					wait();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (stop) {
				for (TransientMarkovMatrixMultiplyThread thread : threads) {
					try {
						thread.join();
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return false;
			}
			phase1 = false;
			phase2 = true;
			while (waitingThreads != threadCount) {
				try {
					notifyAll();
					wait();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (stop) {
				for (TransientMarkovMatrixMultiplyThread thread : threads) {
					try {
						thread.join();
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return false;
			}
			phase1 = true;
			phase2 = false;
			if (progress != null) {
				progress.setValue(progressValue + ((int) ((timeLimit * k) / K)));
			}
			/*
			 * for (State m : stateGraph) { //for (String state :
			 * stateGraph.keySet()) { // for (State m : stateGraph.get(state)) {
			 * double nextProb = m.getCurrentProb() (1 -
			 * (m.getTransitionSum(0.0, null) / Gamma)); for
			 * (StateTransitionPair prev : m.getPrevStatesWithTrans()) { // if
			 * (lhpn.getTransitionRateTree(prev.getTransition()) // != null) {
			 * // prob = //
			 * lhpn.getTransitionRateTree(prev.getTransition()).evaluateExpr
			 * (prev.getState().getVariables()); // } nextProb +=
			 * (prev.getState().getCurrentProb() * (prev.getTransition() /
			 * Gamma)); } m.setNextProb(nextProb * ((Gamma * timeLimit) / k));
			 * m.setPiProb(m.getPiProb() + m.getNextProb()); //} } if (stop) {
			 * return false; } for (State m : stateGraph) { //for (String state
			 * : stateGraph.keySet()) { //for (State m : stateGraph.get(state))
			 * { m.setCurrentProbToNext(); //} } progress.setValue(progressValue
			 * + ((int) ((timeLimit * k) / K)));
			 */
		}
		try {
			while (waitingThreads != 0) {
				try {
					notifyAll();
					wait(10);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (TransientMarkovMatrixMultiplyThread thread : threads) {
				thread.join();
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!stop) {
			for (State m : stateGraph) {
				// for (String state : stateGraph.keySet()) {
				// for (State m : stateGraph.get(state)) {
				m.setPiProb(m.getPiProb() * (Math.pow(Math.E, ((0 - Gamma) * timeLimit))));
				m.setCurrentProbToPi();
				// }
			}
		}
		if (stop) {
			return false;
		}
		return true;
	}

	public synchronized void transientMarkovMatrixMultiplication(int startIndex, int endIndex, double Gamma,
			double timeLimit, int K) {
		for (int k = 1; k <= K && !stop; k++) {
			for (int i = startIndex; i < endIndex; i++) {
				State m = stateGraph.get(i);
				double nextProb = m.getCurrentProb() * (1 - (m.getTransitionSum(0.0, null) / Gamma));
				for (StateTransitionPair prev : m.getPrevStatesWithTrans()) {
					if (prev.isEnabled()) {
						nextProb += (prev.getState().getCurrentProb() * (prev.getTransition() / Gamma));
					}
				}
				m.setNextProb(nextProb * ((Gamma * timeLimit) / k));
				m.setPiProb(m.getPiProb() + m.getNextProb());
				if (stop) {
					waitingThreads--;
					notifyAll();
					return;
				}
			}
			waitingThreads--;
			if (waitingThreads == 0) {
				notifyAll();
			}
			try {
				while (!phase2 && !stop) {
					wait();
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (int i = startIndex; i < endIndex; i++) {
				State m = stateGraph.get(i);
				m.setCurrentProbToNext();
				if (stop) {
					waitingThreads++;
					notifyAll();
					return;
				}
			}
			waitingThreads++;
			if (waitingThreads == threadCount) {
				notifyAll();
			}
			try {
				while (!phase1 && !stop) {
					wait();
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (stop) {
				return;
			}
		}
		waitingThreads--;
	}

	public void pruneStateGraph(String condition) {
		for (State m : stateGraph) {
			// for (String state : stateGraph.keySet()) {
			// for (State m : stateGraph.get(state)) {
			ExprTree expr = new ExprTree(lhpn);
			expr.token = expr.intexpr_gettok(condition);
			expr.intexpr_L(condition);
			if (expr.evaluateExpr(m.getVariables()) == 1.0) {
				for (State s : m.getNextStates()) {
					// ArrayList<StateTransitionPair> newTrans = new
					// ArrayList<StateTransitionPair>();
					for (StateTransitionPair trans : s.getPrevStatesWithTrans()) {
						if (trans.getState() == m) {
							trans.setEnabled(false);
						}
						// if (trans.getState() != m) {
						// newTrans.add(trans);
						// }
					}
					// s.setPrevStatesWithTrans(newTrans.toArray(new
					// StateTransitionPair[0]));
				}
				for (StateTransitionPair trans : m.getNextStatesWithTrans()) {
					trans.setEnabled(false);
				}
				// m.setNextStatesWithTrans(new StateTransitionPair[0]);
			}
			m.setTransitionSum(-1);
			// }
		}
	}

	public void enableAllTransitions() {
		for (State m : stateGraph) {
			for (StateTransitionPair trans : m.getNextStatesWithTrans()) {
				trans.setEnabled(true);
			}
			for (StateTransitionPair trans : m.getPrevStatesWithTrans()) {
				trans.setEnabled(true);
			}
			m.setTransitionSum(-1);
		}
	}

	public boolean performSteadyStateMarkovianAnalysis(double tolerance, ArrayList<Property> props, State initial, JProgressBar progress) {
		if (!canPerformMarkovianAnalysis()) {
			stop = true;
			return false;
		}
		ArrayList<Property> conditions = new ArrayList<Property>();
		if (props != null) {
			for (Property p : props) {
				if (p.getProperty().substring(5, p.getProperty().length() - 1).contains("St=?{")
						|| p.getProperty().substring(5, p.getProperty().length() - 1).contains("Pr=?{")) {
					conditions.add(createProperty(
							p.getLabel(),
							removeNesting(tolerance, 100, p.getProperty()
									.substring(5, p.getProperty().length() - 1), progress)));
				}
				else {
					conditions.add(p);
				}
			}
		}
		if (progress != null) {
			progress.setString(null);
			progress.setIndeterminate(true);
		}
		enableAllTransitions();
		if (initial == null && !stop) {
			initial = getInitialState();
			for (State m : stateGraph) {
				m.setCurrentProb(0.0);
			}
			initial.setCurrentProb(1.0);
		}
		resetColorsForMarkovianAnalysis();
		int period = findPeriod(initial);
		if (period == 0) {
			period = 1;
		}
		int step = 0;
		boolean done = false;
		if (!stop) {
			do {
				step++;
				step = step % period;
				for (State m : stateGraph) {
					// for (String state : stateGraph.keySet()) {
					// for (State m : stateGraph.get(state)) {
					if (m.getColor() != -1 && m.getColor() % period == step) {
						double nextProb = m.getCurrentProb();
						for (StateTransitionPair next : m.getNextStatesWithTrans()) {
							if (next.isEnabled() && next.transition > 0) {
								nextProb = 0.0;
							}
						}
						for (StateTransitionPair prev : m.getPrevStatesWithTrans()) {
							if (prev.isEnabled()) {
								double transProb = 0.0;
								transProb += prev.getTransition();
								// if
								// (lhpn.getTransitionRateTree(prev.getTransition())
								// !=
								// null) {
								// if
								// (lhpn.getTransitionRateTree(prev.getTransition())
								// !=
								// null) {
								// if
								// (!lhpn.isExpTransitionRateTree(prev
								// .getTransition())
								// &&
								// lhpn.getDelayTree(prev.getTransition())
								// .evaluateExpr(null) == 0) {
								// transProb = 1.0;
								// }
								// else {
								// transProb =
								// lhpn.getTransitionRateTree(prev.getTransition()).evaluateExpr(
								// prev.getState().getVariables());
								// }
								// }
								// }
								// else {
								// transProb = 1.0;
								// }
								double transitionSum = prev.getState().getTransitionSum(1.0, m);
								if (transitionSum != 0) {
									transProb = (transProb / transitionSum);
								}
								else {
									transProb = 0.0;
								}
								nextProb += (prev.getState().getCurrentProb() * transProb);
								if (stop) {
									return false;
								}
							}
						}
						m.setNextProb(nextProb);
					}
					if (stop) {
						return false;
					}
				}
				if (stop) {
					return false;
				}
				// }
				boolean change = false;
				for (State m : stateGraph) {
					// for (String state : stateGraph.keySet()) {
					// for (State m : stateGraph.get(state)) {
					if (m.getColor() != -1 && m.getColor() % period == step) {
						if ((m.getCurrentProb() != 0)
								&& (Math.abs(((m.getCurrentProb() - m.getNextProb())) / m.getCurrentProb()) > tolerance)) {
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
				// }
				if (!change) {
					done = true;
				}
			}
			while (!done && !stop);
		}
		if (!stop) {
			double totalProb = 0.0;
			for (State m : stateGraph) {
				// for (String state : stateGraph.keySet()) {
				// for (State m : stateGraph.get(state)) {
				double transitionSum = m.getTransitionSum(1.0, null);
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
			// }
			for (State m : stateGraph) {
				// for (String state : stateGraph.keySet()) {
				// for (State m : stateGraph.get(state)) {
				if (totalProb != 0.0) {
					m.setCurrentProb(m.getCurrentProb() / totalProb);
				}
				if (stop) {
					return false;
				}
			}
			resetColors();
			HashMap<String, Double> output = new HashMap<String, Double>();
			if (!stop) {
				for (Property cond : conditions) {
					String prop = cond.getProperty();
					if (prop.startsWith("St")) {
						prop = prop.substring(5, prop.length()-1);
					}
					double prob = 0;
					// for (String ss : s.split("&&")) {
					// if (ss.split("->").length == 2) {
					// String[] states = ss.split("->");
					// for (String state : stateGraph.keySet()) {
					// for (State m : stateGraph.get(state)) {
					// ExprTree expr = new ExprTree(lhpn);
					// expr.token = expr.intexpr_gettok(states[0]);
					// expr.intexpr_L(states[0]);
					// if (expr.evaluateExpr(m.getVariables()) == 1.0) {
					// for (StateTransitionPair nextState : m
					// .getNextStatesWithTrans()) {
					// ExprTree nextExpr = new ExprTree(lhpn);
					// nextExpr.token = nextExpr
					// .intexpr_gettok(states[1]);
					// nextExpr.intexpr_L(states[1]);
					// if (nextExpr.evaluateExpr(nextState.getState()
					// .getVariables()) == 1.0) {
					// prob += (m.getCurrentProb() * (lhpn
					// .getTransitionRateTree(
					// nextState.getTransition())
					// .evaluateExpr(m.getVariables()) / m
					// .getTransitionSum(1.0, null)));
					// }
					// }
					// if (stop) {
					// return false;
					// }
					// }
					// if (stop) {
					// return false;
					// }
					// }
					// if (stop) {
					// return false;
					// }
					// }
					// }
					// else {
					for (State m : stateGraph) {
						// for (String state : stateGraph.keySet()) {
						// for (State m : stateGraph.get(state)) {
						ExprTree expr = new ExprTree(lhpn);
						expr.token = expr.intexpr_gettok(prop);
						expr.intexpr_L(prop);
						if (expr.evaluateExpr(m.getVariables()) == 1.0) {
							prob += m.getCurrentProb();
						}
						// }
					}
					output.put(cond.getLabel().trim(), prob);
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
		return true;
	}

	public String getMarkovResults() {
		return markovResults;
	}

	public boolean outputTSD(String filename) {
		if (probData != null) {
			probData.outputTSD(filename);
			return true;
		}
		return false;
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
		for (State m : stateGraph) {
			// for (String state : stateGraph.keySet()) {
			// for (State m : stateGraph.get(state)) {
			m.setColor(-1);
			if (stop) {
				return;
			}
		}
		// if (stop) {
		// return;
		// }
		// }
	}

	public void resetColors() {
		for (State m : stateGraph) {
			// for (String state : stateGraph.keySet()) {
			// for (State m : stateGraph.get(state)) {
			m.setColor(0);
			if (stop) {
				return;
			}
		}
		// if (stop) {
		// return;
		// }
		// }
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
		for (State s : stateGraph) {// .get(createStateVector(variables,
			// allVariables))) {
			if (s.getID().equals("S0")) {
				return s;
			}
		}
		return null;
	}

	public ArrayList<State> getStateGraph() {// HashMap<String,
		// LinkedList<State>>
		// getStateGraph() {
		return stateGraph;
	}

	public int getNumberOfStates() {
		return stateGraph.size();
	}
	
	public double getMemoryUsed() {
		return usedMemory;
	}
	
	public double getTotalMemoryUsed() {
		return totalUsedMemory;
	}
	
	public int getNumberOfTransitions() {
		int transitions = 0;
		for (State m : stateGraph) {
			transitions += m.getNumTransitions();
		}
		return transitions;
	}

	private static ArrayList<String> copyArrayList(ArrayList<String> original) {
		ArrayList<String> copy = new ArrayList<String>();
		for (String element : original) {
			copy.add(element);
		}
		return copy;
	}

	private static HashMap<String, String> copyAllVariables(HashMap<String, String> original) {
		HashMap<String, String> copy = new HashMap<String, String>();
		for (String s : original.keySet()) {
			copy.put(s, original.get(s));
		}
		return copy;
	}

	private static String createStateVector(ArrayList<String> variables, HashMap<String, String> allVariables) {
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
			for (State m : stateGraph) {
				// for (String state : stateGraph.keySet()) {
				// for (State m : stateGraph.get(state)) {
				if (withProbs) {
					out.write(m.getID() + " [shape=\"ellipse\",label=\"" + m.getID() + "\\n<" + m.stateVector
							+ ">\\nProb = " + num.format(m.getCurrentProb()) + "\"]\n");
				}
				else {
					out.write(m.getID() + " [shape=\"ellipse\",label=\"" + m.getID() + "\\n<" + m.stateVector
							+ ">\"]\n");
				}
				for (StateTransitionPair next : m.getNextStatesWithTrans()) {
					/*
					 * System.out.println(m.getID() + " -> " +
					 * next.getState().getID() + " [label=\"" +
					 * next.getTransition() + "\\n");
					 * System.out.println(m.getTransitionSum());
					 * System.out.println(lhpn.getTransitionRateTree(
					 * next.getTransition ()).evaluateExpr(m.getVariables()));
					 */
					if (next.isEnabled()) {
						out.write(m.getID() + " -> " + next.getState().getID() + " [label=\""
								+ next.getTransitionName() + "\\n" + num.format(next.getTransition()) + "\"]\n");
					}
					// if (lhpn.getTransitionRateTree(next.getTransition())
					// != null) {
					// out.write(m.getID()
					// + " -> "
					// + next.getState().getID()
					// + " [label=\""
					// + next.getTransition()
					// + "\\n"
					// +
					// num.format((lhpn.getTransitionRateTree(next.getTransition()).evaluateExpr(m
					// .getVariables()) /*
					// * / m . getTransitionSum ( )
					// */)) + "\"]\n");
					// }
					// else {
					// out.write(m.getID() + " -> " +
					// next.getState().getID() +
					// " [label=\"" + next.getTransition() + "\"]\n");
					// }
				}
			}
			// }
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

	public class Property {
		private String label;

		private String property;

		public Property(String label, String property) {
			this.label = label;
			this.property = property;
		}

		private String getLabel() {
			return label;
		}

		private String getProperty() {
			return property;
		}

//		@SuppressWarnings("unused")
//		private void setLabel(String label) {
//			this.label = label;
//		}
//
//		@SuppressWarnings("unused")
//		private void setProperty(String property) {
//			this.property = property;
//		}
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

		private State getState() {
			return parent;
		}
	}

	private class StateTransitionPair {
		private double transition;

		private String transitionName;

		private State state;

		private boolean enabled;

		private StateTransitionPair(State state, double transition, String transitionName) {
			this.state = state;
			this.transition = transition;
			this.transitionName = transitionName;
			enabled = true;
		}

		private State getState() {
			return state;
		}

		private double getTransition() {
			return transition;
		}

		private String getTransitionName() {
			return transitionName;
		}

		private boolean isEnabled() {
			return enabled;
		}

		private void setEnabled(boolean enabled) {
			this.enabled = enabled;
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

		private double piProb;

		private String variablesStr;

		private double transitionSum;

		private String[] persistentTrans;

		public State(String[] markings, StateTransitionPair[] nextStates, String id, String stateVector,
				HashMap<String, String> variables) {
			this.markings = markings;
			this.nextStates = nextStates;
			prevStates = new StateTransitionPair[0];
			this.id = id;
			this.stateVector = stateVector;
			color = 0;
			currentProb = 0.0;
			nextProb = 0.0;
			this.variablesStr = "";
			for (String key : variables.keySet()) {
				String value = variables.get(key);
				if (value.contains(",")) {
					String[] valueSplit = value.split(",");
					value = valueSplit[0].replace("[","");
				}
				if (this.variablesStr.equals("")) {
					this.variablesStr += key + "=" + value;
				}
				else {
					this.variablesStr += "," + key + "=" + value;
				}
			}
			transitionSum = -1;
			persistentTrans = new String[0];
		}

		private void addVariable(String variable, String value) {
			if (value.contains(",")) {
				String[] valueSplit = value.split(",");
				value = valueSplit[0].replace("[","");
			}
			if (this.variablesStr.equals("")) {
				this.variablesStr += variable + "=" + value;
			}
			else {
				this.variablesStr += "," + variable + "=" + value;
			}
		}

		private void addPersistentTransition(String transition) {
			String[] newTrans = new String[persistentTrans.length + 1];
			for (int i = 0; i < persistentTrans.length; i++) {
				newTrans[i] = persistentTrans[i];
			}
			newTrans[newTrans.length - 1] = transition;
			persistentTrans = newTrans;
		}

		private void removePersistentTransition(String transition) {
			ArrayList<String> newTrans = new ArrayList<String>();
			for (String trans : persistentTrans) {
				if (!trans.equals(transition)) {
					newTrans.add(trans);
				}
			}
			persistentTrans = newTrans.toArray(new String[0]);
		}

		private String[] getPersistentTransitions() {
			return persistentTrans;
		}

		private void setPersistentTransitions(String[] persistentTrans) {
			this.persistentTrans = persistentTrans;
		}

		private boolean containsPersistentTransition(String transition) {
			for (String trans : persistentTrans) {
				if (trans.equals(transition)) {
					return true;
				}
			}
			return false;
		}

		private String getID() {
			return id;
		}

		private HashMap<String, String> getVariables() {
			HashMap<String, String> vars = new HashMap<String, String>();
			if (!variablesStr.equals("")) {
				String[] assignments = variablesStr.split(",");
				for (String assignment : assignments) {
					String[] split = assignment.split("=");
					vars.put(split[0], split[1]);
				}
			}
			return vars;
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

		private void setPiProb(double probability) {
			piProb = probability;
		}

		private double getPiProb() {
			return piProb;
		}

		private void setCurrentProbToNext() {
			currentProb = nextProb;
		}

		private void setCurrentProbToPi() {
			currentProb = piProb;
		}

		private void setTransitionSum(double transitionSum) {
			this.transitionSum = transitionSum;
		}

		private double getTransitionSum(double noRate, State n) {
			if (transitionSum == -1) {
				transitionSum = 0;
				for (StateTransitionPair next : nextStates) {
					if (next.isEnabled()) {
						transitionSum += next.getTransition();
					}
					// if (lhpn.getTransitionRateTree(next.getTransition()) !=
					// null) {
					// if
					// (!lhpn.isExpTransitionRateTree(next.getTransition())
					// &&
					// lhpn.getDelayTree(next.getTransition()).evaluateExpr(null)
					// == 0) {
					// if (n == null || next.equals(n)) {
					// return 1.0;
					// }
					// else {
					// return 0.0;
					// }
					// }
					// else {
					// transitionSum +=
					// lhpn.getTransitionRateTree(next.getTransition()).evaluateExpr(variables);
					// }
					// }
					// else {
					// transitionSum += noRate;
					// }
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
		
		public int getNumTransitions() {
			return nextStates.length;
		}

		public State[] getNextStates() {
			ArrayList<State> next = new ArrayList<State>();
			for (StateTransitionPair st : nextStates) {
				if (st.isEnabled()) {
					next.add(st.getState());
				}
			}
			return next.toArray(new State[0]);
		}

		public State[] getPrevStates() {
			ArrayList<State> prev = new ArrayList<State>();
			for (StateTransitionPair st : prevStates) {
				if (st.isEnabled()) {
					prev.add(st.getState());
				}
			}
			return prev.toArray(new State[0]);
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

		private void addNextState(State nextState, double transition, String transitionName) {
			StateTransitionPair[] newNextStates = new StateTransitionPair[nextStates.length + 1];
			for (int i = 0; i < nextStates.length; i++) {
				newNextStates[i] = nextStates[i];
			}
			newNextStates[newNextStates.length - 1] = new StateTransitionPair(nextState, transition, transitionName);
			nextStates = newNextStates;
			nextState.addPreviousState(this, transition, transitionName);
		}

		private void addPreviousState(State prevState, double transition, String transitionName) {
			StateTransitionPair[] newPrevStates = new StateTransitionPair[prevStates.length + 1];
			for (int i = 0; i < prevStates.length; i++) {
				newPrevStates[i] = prevStates[i];
			}
			newPrevStates[newPrevStates.length - 1] = new StateTransitionPair(prevState, transition, transitionName);
			prevStates = newPrevStates;
		}
		
//		@SuppressWarnings("unused")
//		private void setNextStatesWithTrans(StateTransitionPair[] trans) {
//			nextStates = trans;
//		}
//
//		@SuppressWarnings("unused")
//		private void setPrevStatesWithTrans(StateTransitionPair[] trans) {
//			prevStates = trans;
//		}
	}

	@Override
	public void run() {
	}
}
