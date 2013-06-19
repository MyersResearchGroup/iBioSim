package verification.platu.markovianAnalysis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JProgressBar;

import lpn.parser.Transition;

import verification.platu.project.PrjState;
import verification.platu.stategraph.StateGraph;

public class MarkovianAnalysis {
	// TODO: need to deal with the stop variable. Do we need it for non-GUI?	
	private boolean stop;

	public boolean performSteadyStateMarkovianAnalysis(double tolerance, ArrayList<Property> props, 
			ProbGlobalStateSet globalStateSet, StateGraph[] sgList, JProgressBar progress) {
		// Moved the test for the ability to perform Markovian analysis to the run method in Verification.java, where LPN(s) are loaded
		// or generated from decomposition. 
//		// ---- TODO: Are these needed? -----
//		ArrayList<Property> conditions = new ArrayList<Property>();
//		if (props != null) {
//			for (Property p : props) {
//				if (p.getProperty().substring(5, p.getProperty().length() - 1).contains("St=?{")
//						|| p.getProperty().substring(5, p.getProperty().length() - 1).contains("Pr=?{")) {
//					conditions.add(createProperty(
//							p.getLabel(),
//							removeNesting(tolerance, 100, p.getProperty()
//									.substring(5, p.getProperty().length() - 1), progress)));
//				}
//				else {
//					conditions.add(p);
//				}
//			}
//		}
//		if (progress != null) {
//			progress.setString(null);
//			progress.setIndeterminate(true);
//		}
//		enableAllTransitions();
//		// --------------------
		// TODO: Can initialGlobalState ever be null? 
		//if (initialGlobalState == null && !stop) {
		//initialGlobalState = generateInitialGlobalStateFromLocalSG(sgList);//getInitialState();
		
		computeEmbeddedMarkovChain(globalStateSet);
		// Set initial probability and period for all global states.
		ProbGlobalState initialGlobalState = (ProbGlobalState) globalStateSet.getInitState();
		if (!stop) {
			for (PrjState m : globalStateSet) {
				((ProbGlobalState) m).setCurrentProb(0.0);
			}
			initialGlobalState.setCurrentProb(1.0);
		}
		resetColorsForMarkovianAnalysis(globalStateSet);
		int period = findPeriod(initialGlobalState);
		if (period == 0) {
			period = 1;
		}
		boolean change = false;
		boolean converged = false;
		int counter = 0;
		if (!stop) {
			while (!converged) {
				counter++;
				counter = counter % period;
				for (PrjState curGlobalSt : globalStateSet) {
					for (Transition tran : ((ProbGlobalState) curGlobalSt).getNextProbGlobalStateMap().keySet()) {
						PrjState nextGlobalSt = ((ProbGlobalState) curGlobalSt).getNextProbGlobalStateMap().get(tran);
						double nextStNextProb = ((ProbGlobalState) nextGlobalSt).getNextProb();
						double curStCurProb = ((ProbGlobalState) curGlobalSt).getCurrentProb();
						double tranProb = ((ProbGlobalState) curGlobalSt).getTranProb();
						((ProbGlobalState) curGlobalSt).setNextProb(nextStNextProb + curStCurProb * tranProb);
					}
					if (stop) {
						return false;
					}
				}
				for (PrjState curGlobalSt : globalStateSet) {
					if (((ProbGlobalState) curGlobalSt).getColor() == counter) {
						double nextProb = ((ProbGlobalState) curGlobalSt).getNextProb();
						double piProb = ((ProbGlobalState) curGlobalSt).getPiProb();
						if (Math.abs((nextProb - piProb)/nextProb) > tolerance) 
							change = true;
						((ProbGlobalState) curGlobalSt).setPiProb(nextProb);											
					}
					((ProbGlobalState) curGlobalSt).setCurrentProbToNext();
					((ProbGlobalState) curGlobalSt).setNextProb(0);
					if (stop) {
						return false;
					}
				}
				if (counter == 0) {
					if (!change)
						converged = true;
					change = false;
				}				
			}
			for (PrjState curGlobalSt : globalStateSet)
				((ProbGlobalState) curGlobalSt).setPiProb(0);
			for (int i=0; i<=period; i++) {
				for (PrjState curGlobalSt : globalStateSet) {
					for (Transition tran : ((ProbGlobalState) curGlobalSt).getNextProbGlobalStateMap().keySet()) {
						PrjState nextGlobalSt = ((ProbGlobalState) curGlobalSt).getNextProbGlobalStateMap().get(tran);
						double nextStNextProb = ((ProbGlobalState) nextGlobalSt).getNextProb();
						double curStCurProb = ((ProbGlobalState) curGlobalSt).getCurrentProb();
						double tranProb = ((ProbGlobalState) curGlobalSt).getTranProb();
						((ProbGlobalState) curGlobalSt).setNextProb(nextStNextProb + curStCurProb * tranProb);
					}
					if (stop) {
						return false;
					}
				}
				for (PrjState curGlobalSt : globalStateSet) {
					((ProbGlobalState) curGlobalSt).setCurrentProbToNext();
					((ProbGlobalState) curGlobalSt).setNextProb(0);
					double curPiProb = ((ProbGlobalState) curGlobalSt).getPiProb();
					double curCurProb = ((ProbGlobalState) curGlobalSt).getCurrentProb();
					((ProbGlobalState) curGlobalSt).setPiProb(curPiProb + curCurProb);
				}
			}
			if (stop) {
				return false;
			}
			double totalProb = 0;
			for (PrjState curGlobalSt : globalStateSet) {
				double piProb = ((ProbGlobalState) curGlobalSt).getPiProb();
				double tranSum = ((ProbGlobalState) curGlobalSt).computeTransitionSum();
				((ProbGlobalState) curGlobalSt).setCurrentProb(piProb/(period * tranSum));
				totalProb += totalProb + ((ProbGlobalState) curGlobalSt).getCurrentProb();
			}
			if (stop) {
				return false;
			}
			for (PrjState curGlobalSt : globalStateSet) {
				double curProb = ((ProbGlobalState) curGlobalSt).getCurrentProb();
				((ProbGlobalState) curGlobalSt).setCurrentProb(curProb);
			}
			// TODO: need to report totalProb?
		}
		return true;
	}

	private void computeEmbeddedMarkovChain(ProbGlobalStateSet globalStateSet) {
		for (PrjState curGlobalSt : globalStateSet) {
			double tranRateSum = ((ProbGlobalState) curGlobalSt).computeTransitionSum();
			for (Transition tran : ((ProbGlobalState) curGlobalSt).getNextProbGlobalStateMap().keySet()) {
				double outTranRate = ((ProbGlobalState) curGlobalSt).getOutgoingTranRate(tran);
				((ProbGlobalState) curGlobalSt).setTranProb(outTranRate/tranRateSum);
			}			
		}		
	}

	private void resetColorsForMarkovianAnalysis(ProbGlobalStateSet globalStateSet) {
		for (PrjState m : globalStateSet) {
			((ProbGlobalState) m).setColor(-1);
			if (stop) {
				return;
			}
		}
	}
	
	private int findPeriod(ProbGlobalState state) {
		if (stop) {
			return 0;
		}
		int period = 0;
		int color = 0;
		state.setColor(color);
		color = state.getColor() + 1;
		Queue<ProbGlobalState> unVisitedStates = new LinkedList<ProbGlobalState>();
		for (ProbGlobalState s : ((ProbGlobalState)state).getNextProbGlobalStateMap().values()) {
			if (((ProbGlobalState) s).getColor() == -1) {
				((ProbGlobalState) s).setColor(color);
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
			state = (ProbGlobalState) unVisitedStates.poll();
			color = state.getColor() + 1;
			for (ProbGlobalState s : ((ProbGlobalState)state).getNextProbGlobalStateMap().values()) {
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
		else {
			return prop;
		}
	}

	// TODO: Do we still need this method?
	private String determineNestedProbability(double error, double timeStep, String property) {
//		String prop = "";
//		int braces = 0;
//		for (int i = 0; i < property.length(); i++) {
//			char c = property.charAt(i);
//			if (c == '{') {
//				braces++;
//			}
//			else if (c == '}') {
//				braces--;
//			}
//			if (braces == 1) {
//				if ((c == 'G' || c == 'F' || c == 'U') && property.charAt(i + 1) == '[') {
//					prop += "P" + c;
//				}
//				else {
//					prop += c;
//				}
//			}
//			else {
//				prop += c;
//			}
//		}
//		String[] condition = Translator.getProbpropParts(prop.substring(5, prop.length() - 1));
//		String id = "Pr" + Math.abs(prop.hashCode());
//		if (!variables.contains(id)) {
//			variables.add(id);
//			lhpn.addInteger(id, id);
//			boolean globallyTrue = false;
//			if (prop.contains("PF")) {
//				condition[0] = "true";
//			}
//			else if (prop.contains("PG")) {
//				condition[0] = "true";
//				globallyTrue = true;
//			}
//			for (State initial : stateGraph) {
//				if (initial.getVariables().keySet().contains(id)) {
//					break;
//				}
//				enableAllTransitions();
//				double Gamma;
//				for (State m : stateGraph) {
//					m.setCurrentProb(0.0);
//					m.setPiProb(0.0);
//				}
//				initial.setCurrentProb(1.0);
//				initial.setPiProb(1.0);
//				double lowerbound = 0;
//				if (!condition[2].equals("")) {
//					ExprTree expr = new ExprTree(lhpn);
//					expr.token = expr.intexpr_gettok(condition[2]);
//					expr.intexpr_L(condition[2]);
//					lowerbound = expr.evaluateExpr(null);
//					pruneStateGraph("~(" + condition[0] + ")");
//					// Compute Gamma
//					Gamma = 0;
//					for (State m : stateGraph) {
//						Gamma = Math.max(m.getTransitionSum(0.0, null), Gamma);
//					}
//					// Compute K
//					int K = 0;
//					double xi = 1;
//					double delta = 1;
//					double eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * timeStep)));
//					while (delta < eta) {
//						K = K + 1;
//						xi = xi * ((Gamma * timeStep) / K);
//						delta = delta + xi;
//					}
//					double step = Math.min(timeStep, lowerbound);
//					for (double i = 0; i < lowerbound; i += step) {
//						step = Math.min(timeStep, lowerbound - i);
//						if (step != timeStep) {
//							// Compute K
//							K = 0;
//							xi = 1;
//							delta = 1;
//							eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * step)));
//							while (delta < eta) {
//								K = K + 1;
//								xi = xi * ((Gamma * step) / K);
//								delta = delta + xi;
//							}
//						}
//						performTransientMarkovianAnalysis(step, Gamma, K, null);
//					}
//				}
//				else {
//					pruneStateGraph("~(" + condition[0] + ")");
//				}
//				double upperbound;
//				if (condition[3].equals("inf")) {
//					upperbound = -1;
//				}
//				else {
//					ExprTree expr = new ExprTree(lhpn);
//					expr.token = expr.intexpr_gettok(condition[3]);
//					expr.intexpr_L(condition[3]);
//					upperbound = expr.evaluateExpr(null) - lowerbound;
//				}
//				if (globallyTrue) {
//					pruneStateGraph("~(" + condition[1] + ")");
//				}
//				else {
//					pruneStateGraph(condition[1]);
//				}
//				if (upperbound == -1) {
//					ArrayList<Property> conditions = new ArrayList<Property>();
//					if (!condition[0].equals("true")) {
//						conditions.add(new Property("Failure", "~(" + condition[0] + ")&~(" + condition[1] + ")"));
//					}
//					else {
//						conditions.add(new Property("Failure", "~(" + condition[1] + ")"));
//					}
//					conditions.add(new Property("Success", condition[1]));
//					performSteadyStateMarkovianAnalysis(error, conditions, initial, null);
//				}
//				// Compute Gamma
//				Gamma = 0;
//				for (State m : stateGraph) {
//					Gamma = Math.max(m.getTransitionSum(0.0, null), Gamma);
//				}
//				// Compute K
//				int K = 0;
//				double xi = 1;
//				double delta = 1;
//				double eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * timeStep)));
//				while (delta < eta) {
//					K = K + 1;
//					xi = xi * ((Gamma * timeStep) / K);
//					delta = delta + xi;
//				}
//				double step = Math.min(timeStep, upperbound - lowerbound);
//				for (double i = 0; i < upperbound; i += step) {
//					step = Math.min(timeStep, upperbound - i);
//					if (step != timeStep) {
//						// Compute K
//						K = 0;
//						xi = 1;
//						delta = 1;
//						eta = (1 - error) / (Math.pow(Math.E, ((0 - Gamma) * step)));
//						while (delta < eta) {
//							K = K + 1;
//							xi = xi * ((Gamma * step) / K);
//							delta = delta + xi;
//						}
//					}
//					performTransientMarkovianAnalysis(step, Gamma, K, null);
//				}
//				double failureProb = 0;
//				double successProb = 0;
//				double timelimitProb = 0;
//				for (State m : stateGraph) {
//					// for (String state : stateGraph.keySet()) {
//					// for (State m : stateGraph.get(state)) {
//					ExprTree failureExpr = new ExprTree(lhpn);
//					failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~(" + condition[1] + ")");
//					failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
//					ExprTree successExpr = new ExprTree(lhpn);
//					if (globallyTrue) {
//						successExpr.token = successExpr.intexpr_gettok("~(" + condition[1] + ")");
//						successExpr.intexpr_L("~(" + condition[1] + ")");
//					}
//					else {
//						successExpr.token = successExpr.intexpr_gettok(condition[1]);
//						successExpr.intexpr_L(condition[1]);
//					}
//					if (failureExpr.evaluateExpr(m.getVariables()) == 1.0) {
//						failureProb += m.getCurrentProb();
//					}
//					else if (successExpr.evaluateExpr(m.getVariables()) == 1.0) {
//						successProb += m.getCurrentProb();
//					}
//					else {
//						if (!globallyTrue) {
//							timelimitProb += m.getCurrentProb();
//						}
//					}
//					// }
//				}
//				if (globallyTrue) {
//					successProb = 1 - successProb;
//					timelimitProb = 1 - (failureProb + successProb);
//				}
//				initial.addVariable(id, "" + successProb);
//			}
//		}
//		return id;
		// ===TEMP===
		return null;
		// ========
	}
	
	public Property createProperty(String label, String property) {
		return new Property(label, property);
	}

	//TODO: Do we still need this method?
//	public static void enableAllTransitions() {
//		for (State m : stateGraph) {
//			for (StateTransitionPair trans : m.getNextStatesWithTrans()) {
//				trans.setEnabled(true);
//			}
//			for (StateTransitionPair trans : m.getPrevStatesWithTrans()) {
//				trans.setEnabled(true);
//			}
//			m.setTransitionSum(-1);
//		}
//	}
}
