package verification.platu.markovianAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JProgressBar;

import lpn.parser.ExprTree;
import lpn.parser.Transition;
import main.util.dataparser.DataParser;

import verification.platu.project.PrjState;
import verification.platu.stategraph.StateGraph;

public class MarkovianAnalysis implements Runnable{
	
	private boolean stop;
	private DataParser probData;
	private int waitingThreads, threadCount;
	private boolean phase1, phase2;
	
	//public boolean performSteadyStateMarkovianAnalysis(double tolerance, ArrayList<Property> props, 
	//		ProbGlobalStateSet globalStateSet, StateGraph[] sgList, JProgressBar progress) {
	public boolean performSteadyStateMarkovianAnalysis(double tolerance, ProbGlobalStateSet globalStateSet) {
		// Moved the test for the ability to perform Markovian analysis to the run method in Verification.java, where LPN(s) are loaded
		// or generated from decomposition. 
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
//		enableAllTransitions(globalStateSet);
		// --------------------
//		TODO: Can initialGlobalState ever be null? 
//		if (initialGlobalState == null && !stop) {
//		initialGlobalState = generateInitialGlobalStateFromLocalSG(sgList);//getInitialState();
		
		boolean change = false;
		boolean converged = false;
		int counter = 0;
		computeTransitionRateSum(globalStateSet);
		// Set initial probability and period for all global states.
		ProbGlobalState initGlobalSt = (ProbGlobalState) globalStateSet.getInitState();
		if (!stop) {
			for (PrjState m : globalStateSet.keySet()) {
				((ProbGlobalState) m).setCurrentProb(0.0);
			}
			initGlobalSt.setCurrentProb(1.0);
		}
		resetColorsForMarkovianAnalysis(globalStateSet);
		int period = findPeriod(initGlobalSt, globalStateSet);
		if (period == 0) {
			period = 1;
		}
		
		//int loopCount = 0;
		//LinkedList<ProbGlobalState> unexploredStatesQueue = new LinkedList<ProbGlobalState>();
		
		while (!converged && !stop) {
			counter++;
			counter = counter % period;
			for (PrjState curGlobalSt : globalStateSet.keySet()) {				
				for (Transition outTran : ((ProbGlobalState) curGlobalSt).getOutgoingTranSetForProbGlobalState()) {
					PrjState nextGlobalSt = ((ProbGlobalState) curGlobalSt).getNextProbGlobalState(outTran, globalStateSet);//					
					double curStCurProb = ((ProbGlobalState) curGlobalSt).getCurrentProb();
					if (curStCurProb > 0.0) {
						double nextStNextProb = ((ProbGlobalState) nextGlobalSt).getNextProb();
						double tranProb = ((ProbGlobalState) curGlobalSt).getTranProb(outTran);
						((ProbGlobalState) nextGlobalSt).setNextProb(nextStNextProb + curStCurProb * tranProb);
					}
				}
				if (stop) {
					return false;
				}
			}
			
//			// ------ BFS -------------
//			unexploredStatesQueue.clear();
//			unexploredStatesQueue.add(initGlobalSt);
//			((ProbGlobalState) initGlobalSt).markAsExplored();
//			while (!unexploredStatesQueue.isEmpty()) {					
//				ProbGlobalState curGlobalSt = unexploredStatesQueue.removeFirst();
//				for (Transition outTran : ((ProbGlobalState) curGlobalSt).getOutgoingTranSetForProbGlobalState()) {
//					PrjState nextGlobalSt = ((ProbGlobalState) curGlobalSt).getNextProbGlobalState(outTran, globalStateSet);
//					double curStCurProb = ((ProbGlobalState) curGlobalSt).getCurrentProb();
//					if (curStCurProb > 0.0) {
//						double nextStNextProb = ((ProbGlobalState) nextGlobalSt).getNextProb();					
//						double tranProb = ((ProbGlobalState) curGlobalSt).getTranProb(outTran);
//						((ProbGlobalState) nextGlobalSt).setNextProb(nextStNextProb + curStCurProb * tranProb);
//					}										
////					System.out.println("curGlobalSt = " + curGlobalSt.getLabel() + ", nextGlobalSt = " + nextGlobalSt.getLabel()
////							+ ", curGlobalSt.curProb = " + ((ProbGlobalState) curGlobalSt).getCurrentProb()
////							+ ", nextGlobalSt.nextProb = " + ((ProbGlobalState) nextGlobalSt).getNextProb());					
//					if (!((ProbGlobalState) nextGlobalSt).isExplored()) {												
//						unexploredStatesQueue.add((ProbGlobalState) nextGlobalSt);
//						((ProbGlobalState) nextGlobalSt).markAsExplored();
//					}
//				}
//			}
//			// --------------------
			
			for (PrjState globalSt : globalStateSet.keySet()) {
				if (((ProbGlobalState) globalSt).getColor() == counter) {
					double nextProb = ((ProbGlobalState) globalSt).getNextProb();
					double piProb = ((ProbGlobalState) globalSt).getPiProb();
					if (Math.abs((nextProb - piProb)/nextProb) > tolerance) 
						change = true;
					((ProbGlobalState) globalSt).setPiProb(nextProb);											
				}
				((ProbGlobalState) globalSt).setCurrentProbToNext();
				((ProbGlobalState) globalSt).setNextProb(0.0);
				if (stop) {
					return false;
				}
			}
			if (counter == 0) {
				if (!change)
					converged = true;
				change = false;
			}
//			// -------- BFS ----------
//			for (PrjState curGlobalSt : globalStateSet.keySet())				
//				((ProbGlobalState) curGlobalSt).resetExplored();
//			// -----------------------
			//loopCount ++;
		}
		
		//System.out.println("loopCount = " + loopCount);
		//printStateSetStatus(globalStateSet, "the end of convergence test");		
		for (PrjState curGlobalSt : globalStateSet.keySet()) {
			((ProbGlobalState) curGlobalSt).setPiProb(0.0);
			//((ProbGlobalState) curGlobalSt).resetExplored();
		}
		for (int i=0; i<period; i++) {
			for (PrjState curGlobalSt : globalStateSet.keySet()) {
				for (Transition outTran : ((ProbGlobalState) curGlobalSt).getOutgoingTranSetForProbGlobalState()) {
					PrjState nextGlobalSt = ((ProbGlobalState) curGlobalSt).getNextProbGlobalState(outTran, globalStateSet);						
					double nextStNextProb = ((ProbGlobalState) nextGlobalSt).getNextProb();
					double curStCurProb = ((ProbGlobalState) curGlobalSt).getCurrentProb();
					double tranProb = ((ProbGlobalState) curGlobalSt).getTranProb(outTran);
					((ProbGlobalState) nextGlobalSt).setNextProb(nextStNextProb + curStCurProb * tranProb);
					if (stop) {
						return false;
					}
				}
			}
			
//			// ---------- BFS ----------------
//			for (PrjState curGlobalSt : globalStateSet.keySet())				
//				((ProbGlobalState) curGlobalSt).resetExplored();
//			unexploredStatesQueue.clear();
//			unexploredStatesQueue.add(initGlobalSt);
//			((ProbGlobalState) initGlobalSt).markAsExplored();
//			while (!unexploredStatesQueue.isEmpty()) {									
//				ProbGlobalState curGlobalSt = unexploredStatesQueue.removeFirst();
//				for (Transition outTran : ((ProbGlobalState) curGlobalSt).getOutgoingTranSetForProbGlobalState()) {
//					PrjState nextGlobalSt = ((ProbGlobalState) curGlobalSt).getNextProbGlobalState(outTran, globalStateSet);
//					double nextStNextProb = ((ProbGlobalState) nextGlobalSt).getNextProb();
//					double curStCurProb = ((ProbGlobalState) curGlobalSt).getCurrentProb();						
//					double tranProb = ((ProbGlobalState) curGlobalSt).getTranProb(outTran);
//					((ProbGlobalState) nextGlobalSt).setNextProb(nextStNextProb + curStCurProb * tranProb);
//					if (!((ProbGlobalState) nextGlobalSt).isExplored()) {
//						unexploredStatesQueue.add((ProbGlobalState) nextGlobalSt);
//						((ProbGlobalState) nextGlobalSt).markAsExplored();
//					}
//				}
//			}
			// --------------------------------
			
			for (PrjState globalState : globalStateSet.keySet()) {
				((ProbGlobalState) globalState).setCurrentProbToNext();
				((ProbGlobalState) globalState).setNextProb(0.0);
				double piProb = ((ProbGlobalState) globalState).getPiProb();
				double curProb = ((ProbGlobalState) globalState).getCurrentProb();
				((ProbGlobalState) globalState).setPiProb(piProb + curProb);
				if (stop) {
					return false;
				}
			}
			//printStateSetStatus(globalStateSet, "Iterate through period (period = " + period + "), i = " + i);
		}		
		double totalProb = 0;
		for (PrjState curGlobalSt : globalStateSet.keySet()) {
			double piProb = ((ProbGlobalState) curGlobalSt).getPiProb();
			double tranRateSum = ((ProbGlobalState) curGlobalSt).getTranRateSum();
			//((ProbGlobalState) curGlobalSt).setCurrentProb(piProb/period);
			((ProbGlobalState) curGlobalSt).setCurrentProb(piProb/period/tranRateSum);
			totalProb += ((ProbGlobalState) curGlobalSt).getCurrentProb();
		}
		System.out.println("totalProb = " + totalProb);
		if (stop) {
			return false;
		}
		for (PrjState curGlobalSt : globalStateSet.keySet()) {
			double curProb = ((ProbGlobalState) curGlobalSt).getCurrentProb();
//			double tranRateSum = ((ProbGlobalState) curGlobalSt).getTranRateSum();
//			((ProbGlobalState) curGlobalSt).setCurrentProb((curProb/totalProb) * tranRateSum);
			((ProbGlobalState) curGlobalSt).setCurrentProb(curProb/totalProb);
		}
//		printStateSetStatus(globalStateSet, "end of steady state analysis");
		return true;
	}

	private void printStateSetStatus(ProbGlobalStateSet globalStateSet,
			String location) {
		System.out.println("------- Prob Global States status @ " + location + "----------------------");
		for (PrjState curGlobalSt : globalStateSet.keySet()) {
			if (((ProbGlobalState) curGlobalSt).getCurrentProb() > 1) {
				System.out.println(curGlobalSt.getLabel() + ", curProb = " 
						+ ((ProbGlobalState) curGlobalSt).getCurrentProb() + ", exceeds 1.");
				System.exit(1);
				return;
			}
			if (((ProbGlobalState) curGlobalSt).getNextProb() > 1) {
				System.out.println(curGlobalSt.getLabel() + ", nextProb = " 
						+ ((ProbGlobalState) curGlobalSt).getNextProb() + ", exceeds 1.");
				System.exit(1);
				return;
			}
			if (((ProbGlobalState) curGlobalSt).getPiProb() > 1) {
				System.out.println(curGlobalSt.getLabel() + ", piProb = " 
						+ ((ProbGlobalState) curGlobalSt).getPiProb() + ", exceeds 1.");
				System.exit(1);
				return;
			}
			System.out.println(curGlobalSt.getLabel() 
					+ ",\t curProb = " + ((ProbGlobalState) curGlobalSt).getCurrentProb()
					+ ",\t nextProb = " + ((ProbGlobalState) curGlobalSt).getNextProb() 
					+ ",\t piProb = " + ((ProbGlobalState) curGlobalSt).getPiProb());
		}
		System.out.println("------------------------------------------------------------------------");
	}

	private void computeTransitionRateSum(ProbGlobalStateSet globalStateSet) {
		//System.out.println("--------------- Transition Rate Sum -------------");
		for (PrjState curGlobalSt : globalStateSet.keySet()) {
			((ProbGlobalState) curGlobalSt).computeTranRateSum();
			//System.out.println(curGlobalSt.getLabel() + ", tranRateSum = " + ((ProbGlobalState) curGlobalSt).getTranRateSum());
		}		
	}

	private void resetColorsForMarkovianAnalysis(ProbGlobalStateSet globalStateSet) {
		for (PrjState m : globalStateSet.keySet()) {
			((ProbGlobalState) m).setColor(-1);
			if (stop) {
				return;
			}
		}
	}
	
	private int findPeriod(ProbGlobalState curSt, ProbGlobalStateSet globalStateSet) {
		if (stop) {
			return 0;
		}
		int period = 0;
		int color = 0;
		curSt.setColor(color);
		color = curSt.getColor() + 1;
		Queue<ProbGlobalState> unVisitedStates = new LinkedList<ProbGlobalState>();
		for (PrjState nextSt : ((ProbGlobalState) curSt).getNextProbGlobalStateSet(globalStateSet)) {
			if (((ProbGlobalState) nextSt).getColor() == -1) {
				((ProbGlobalState) nextSt).setColor(color);
				unVisitedStates.add((ProbGlobalState) nextSt);
			}
			else {
				if (period == 0) {
					period = curSt.getColor() - ((ProbGlobalState) nextSt).getColor() + 1;
				}
				else {
					period = gcd(curSt.getColor() - ((ProbGlobalState) nextSt).getColor() + 1, period);
				}
			}
			if (stop) {
				return 0;
			}
		}
		while (!unVisitedStates.isEmpty() && !stop) {
			curSt = (ProbGlobalState) unVisitedStates.poll();
			color = curSt.getColor() + 1;
			for (PrjState nextSt : ((ProbGlobalState)curSt).getNextProbGlobalStateSet(globalStateSet)) {
				if (((ProbGlobalState) nextSt).getColor() == -1) {
					((ProbGlobalState) nextSt).setColor(color);
					unVisitedStates.add((ProbGlobalState) nextSt);
				}
				else {
					if (period == 0) {
						period = (curSt.getColor() - ((ProbGlobalState) nextSt).getColor() + 1);
					}
					else {
						period = gcd(curSt.getColor() - ((ProbGlobalState) nextSt).getColor() + 1, period);
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
	
////	public boolean performTransientMarkovianAnalysis(double timeLimit, double timeStep, double printInterval,
////			double error, String[] condition, JProgressBar progress, boolean globallyTrue) {
//	public boolean performTransientMarkovianAnalysis(double timeLimit, double timeStep, double printInterval,
//			double error, String[] condition, JProgressBar progress, boolean globallyTrue, ProbGlobalStateSet globalStateSet ) {	
//		// Moved the test for the ability to perform Markovian analysis to the run method in Verification.java, where LPN(s) are loaded
//		// or generated from decomposition. 
//		if (condition != null) {
//			condition[0] = removeNesting(error, timeStep, condition[0], progress);
//			condition[1] = removeNesting(error, timeStep, condition[1], progress);
//			progress.setString(null);
//			double nextPrint = printInterval;
//			if (condition[3].equals("inf")) {
//				progress.setIndeterminate(true);
//			}
//			else {
//				// TODO: Do we need to pass an LPN to this ExprTree constructor?
//				//ExprTree expr = new ExprTree(lhpn);
//				ExprTree expr = new ExprTree();
//				expr.token = expr.intexpr_gettok(condition[3]);
//				expr.intexpr_L(condition[3]);
//				progress.setMaximum((int) expr.evaluateExpr(null));
//			}
//			enableAllTransitions();
//			ProbGlobalState initial = (ProbGlobalState) globalStateSet.getInitState();			
//			if (initial != null) {
//				for (PrjState m : globalStateSet.keySet()) {
//					((ProbGlobalState) m).setCurrentProb(0.0);
//					((ProbGlobalState) m).setPiProb(0.0);
//				}
//				initial.setCurrentProb(1.0);
//				initial.setPiProb(1.0);				
//			}
//			double lowerbound = 0;
//			if (!condition[2].equals("")) {
//				//ExprTree expr = new ExprTree(lhpn);
//				ExprTree expr = new ExprTree();
//				expr.token = expr.intexpr_gettok(condition[2]);
//				expr.intexpr_L(condition[2]);
//				// lowerbound = Math.min(expr.evaluateExpr(null), timeLimit);
//				// TODO: Is lowerbound Double.NaN here?
//				lowerbound = expr.evaluateExpr(null);
//			}
//			double Gamma;
//			ArrayList<String> dataLabels = new ArrayList<String>();
//			dataLabels.add("time");
//			// dataLabels.add("~(" + condition[0] + ")&~(" + condition[1] +
//			// ")");
//			dataLabels.add("Failure");
//			// dataLabels.add(condition[1]);
//			dataLabels.add("Success");
//			ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
//			ArrayList<Double> temp = new ArrayList<Double>();
//			temp.add(0.0);
//			data.add(temp);
//			temp = new ArrayList<Double>();
//			double failureProb = 0;			
//			for (PrjState m : globalStateSet.keySet()) {
//				//ExprTree failureExpr = new ExprTree(lhpn);
//				ExprTree failureExpr = new ExprTree();
//				if (lowerbound == 0) {
//					if (!condition[0].equals("true")) {
//						failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~(" + condition[1]
//								+ ")");
//						failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
//					}
//				}
//				else if (!condition[0].equals("true")) {
//					failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")");
//					failureExpr.intexpr_L("~(" + condition[0] + ")");
//				}
//				// TODO: What does getVariables() return? Should it be added to GlobalProbState?
//				if (failureExpr.evaluateExpr(m.getVariables()) == 1.0) {
//					failureProb += ((ProbGlobalState) m).getCurrentProb();
//				}
//			}
//			temp.add(failureProb * 100);
//			data.add(temp);
//			// if (globallyTrue) {
//			double successProb = 0;
//			if (lowerbound == 0) {
//				for (PrjState m : globalStateSet.keySet()) {				
//					//ExprTree successExpr = new ExprTree(lhpn);
//					ExprTree successExpr = new ExprTree();
//					successExpr.token = successExpr.intexpr_gettok(condition[1]);
//					successExpr.intexpr_L(condition[1]);
//					if (successExpr.evaluateExpr(m.getVariables()) == 1.0) {
//						successProb += ((ProbGlobalState) m).getCurrentProb();
//					}
//				}
//			}
//			temp = new ArrayList<Double>();
//			temp.add(successProb * 100);
//			data.add(temp);
//			probData = new DataParser(dataLabels, data);
//			if (initial != null) {
//				if (!condition[2].equals("")) {
//					if (!condition[0].equals("true")) {
//						pruneStateGraph("~(" + condition[0] + ")");
//					}
//					// Compute Gamma
//					Gamma = 0;
//					for (PrjState m : globalStateSet.keySet()) 
//						Gamma = Math.max(((ProbGlobalState) m).getTranRateSum(), Gamma);					
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
//					double step = Math.min(Math.min(timeStep, lowerbound), nextPrint);
//					for (double i = 0; i < lowerbound; i += step) {
//						step = Math.min(Math.min(timeStep, lowerbound - i), nextPrint - i);
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
//						if (!performTransientMarkovianAnalysis(step, Gamma, K, progress)) {
//							return false;
//						}
//						double prob = 0;
//						for (PrjState m : globalStateSet.keySet()) {
//							//ExprTree expr = new ExprTree(lhpn);
//							ExprTree expr = new ExprTree();
//							expr.token = expr.intexpr_gettok("~(" + condition[0] + ")");
//							expr.intexpr_L("~(" + condition[0] + ")");
//							if (expr.evaluateExpr(m.getVariables()) == 1.0) {
//								prob += ((ProbGlobalState) m).getCurrentProb();
//							}
//							// }
//						}
//						if (i + step == nextPrint) {
//							probData.getData().get(0).add(nextPrint);
//							probData.getData().get(1).add(prob * 100);
//							probData.getData().get(2).add(0.0);
//							nextPrint += printInterval;
//						}
//					}
//				}
//				else {
//					if (!condition[0].equals("true")) {
//						pruneStateGraph("~(" + condition[0] + ")");
//					}
//				}
//				double upperbound;
//				if (condition[3].equals("inf")) {
//					upperbound = -1;
//				}
//				else {
//					//ExprTree expr = new ExprTree(lhpn);
//					ExprTree expr = new ExprTree();
//					expr.token = expr.intexpr_gettok(condition[3]);
//					expr.intexpr_L(condition[3]);
//					// upperbound = Math.min(expr.evaluateExpr(null) -
//					// lowerbound, timeLimit - lowerbound);
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
//					return performSteadyStateMarkovianAnalysis(error, conditions, initial, progress);					
//				}
//				// Compute Gamma
//				Gamma = 0;
//				for (PrjState m : globalStateSet.keySet())
//					Gamma = Math.max(((ProbGlobalState) m).getTranRateSum(), Gamma);
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
//				double step = Math.min(Math.min(timeStep, upperbound - lowerbound), nextPrint - lowerbound);
//				for (double i = 0; i < upperbound; i += step) {
//					step = Math.min(Math.min(timeStep, upperbound - i), nextPrint - lowerbound - i);
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
//					if (!performTransientMarkovianAnalysis(step, Gamma, K, progress)) {
//						return false;
//					}
//					failureProb = 0;
//					successProb = 0;
//					// for (String state : stateGraph.keySet()) {
//					// for (State m : stateGraph.get(state)) {
//					//for (State m : stateGraph) {
//					for (PrjState m : globalStateSet.keySet()) {						
//						//ExprTree failureExpr = new ExprTree(lhpn);
//						ExprTree failureExpr = new ExprTree();
//						failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~(" + condition[1]
//								+ ")");
//						failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
//						//ExprTree successExpr = new ExprTree(lhpn);
//						ExprTree successExpr = new ExprTree();
//						if (globallyTrue) {
//							successExpr.token = successExpr.intexpr_gettok("~(" + condition[1] + ")");
//							successExpr.intexpr_L("~(" + condition[1] + ")");
//						}
//						else {
//							successExpr.token = successExpr.intexpr_gettok(condition[1]);
//							successExpr.intexpr_L(condition[1]);
//						}
//						if (failureExpr.evaluateExpr(m.getVariables()) == 1.0) {
//							failureProb += ((ProbGlobalState) m).getCurrentProb();
//						}
//						else if (successExpr.evaluateExpr(m.getVariables()) == 1.0) {
//							successProb += ((ProbGlobalState) m).getCurrentProb();
//						}
//						// }
//					}
//					if (globallyTrue) {
//						successProb = 1 - successProb;
//					}
//					if (lowerbound + i + step == nextPrint) {
//						probData.getData().get(0).add(nextPrint);
//						probData.getData().get(1).add(failureProb * 100);
//						probData.getData().get(2).add(successProb * 100);
//						nextPrint += printInterval;
//					}
//				}
//				HashMap<String, Double> output = new HashMap<String, Double>();
//				failureProb = 0;
//				successProb = 0;
//				double timelimitProb = 0;
//				for (PrjState m : globalStateSet.keySet()) {
//					//ExprTree failureExpr = new ExprTree(lhpn);
//					ExprTree failureExpr = new ExprTree();
//					failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~(" + condition[1] + ")");
//					failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
//					//ExprTree successExpr = new ExprTree(lhpn);
//					ExprTree successExpr = new ExprTree();
//					if (globallyTrue) {
//						successExpr.token = successExpr.intexpr_gettok("~(" + condition[1] + ")");
//						successExpr.intexpr_L("~(" + condition[1] + ")");
//					}
//					else {
//						successExpr.token = successExpr.intexpr_gettok(condition[1]);
//						successExpr.intexpr_L(condition[1]);
//					}
//					if (failureExpr.evaluateExpr(m.getVariables()) == 1.0) {
//						failureProb += ((ProbGlobalState) m).getCurrentProb();
//					}
//					else if (successExpr.evaluateExpr(m.getVariables()) == 1.0) {
//						successProb += ((ProbGlobalState) m).getCurrentProb();
//					}
//					else {
//						if (!globallyTrue) {
//							timelimitProb += ((ProbGlobalState) m).getCurrentProb();
//						}
//					}
//					// }
//				}
//				if (globallyTrue) {
//					successProb = 1 - successProb;
//					timelimitProb = 1 - (failureProb + successProb);
//				}
//				output.put("Failure", failureProb);
//				output.put("Success", successProb);
//				output.put("Timelimit", timelimitProb);
//				String result1 = "#total";
//				String result2 = "1.0";
//				for (String s : output.keySet()) {
//					result1 += " " + s;
//					result2 += " " + output.get(s);
//				}
//				markovResults = result1 + "\n" + result2 + "\n";
//				return true;
//			}
//			else {
//				return false;
//			}
//		}
//		else { // condition == null
//			progress.setMaximum((int) timeLimit);
//			ProbGlobalState initial = (ProbGlobalState) globalStateSet.getInitState();			
//			if (initial != null) {
//				initial.setCurrentProb(1.0);
//				initial.setPiProb(1.0);
//				boolean stop = false;
//				// Compute Gamma
//				double Gamma = 0;
//				for (PrjState m : globalStateSet.keySet())
//					Gamma = Math.max(((ProbGlobalState) m).getTranRateSum(), Gamma);				
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
//				for (double i = 0; i < timeLimit; i += timeStep) {
//					double step = Math.min(timeStep, timeLimit - i);
//					if (step == timeLimit - i) {
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
//					stop = !performTransientMarkovianAnalysis(step, Gamma, K, progress);
//					progress.setValue((int) i);
//					if (stop) {
//						return false;
//					}
//				}
//				return !stop;
//			}
//			else {
//				return false;
//			}
//		}
//	}
//	
////	private synchronized boolean performTransientMarkovianAnalysis(double timeLimit, double Gamma, int K,
////			JProgressBar progress) {
//	private synchronized boolean performTransientMarkovianAnalysis(double timeLimit, double Gamma, int K,
//			JProgressBar progress, ProbGlobalStateSet globalStateSet) {
//		if (timeLimit == 0.0) {
//			return true;
//		}
//		int progressValue = 0;
//		if (progress != null) {
//			progressValue = progress.getValue();
//		}
//		// Approximate pi(t)
//		threadCount = 4;
//		waitingThreads = threadCount;
//		phase1 = true;
//		phase2 = false;
//		ArrayList<TransientMarkovMatrixMultiplyThread> threads = new ArrayList<TransientMarkovMatrixMultiplyThread>();
//		for (int i = 0; i < threadCount; i++) {
//			TransientMarkovMatrixMultiplyThread thread = new TransientMarkovMatrixMultiplyThread(this);
//			thread.start((i * globalStateSet.keySet().size()) / threadCount, ((i + 1) * globalStateSet.keySet().size()) / threadCount, Gamma,
//					timeLimit, K);
//			threads.add(thread);
//		}
//		for (int k = 1; k <= K && !stop; k++) {
//			while (waitingThreads != 0) {
//				try {
//					notifyAll();
//					wait();
//				}
//				catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			if (stop) {
//				for (TransientMarkovMatrixMultiplyThread thread : threads) {
//					try {
//						thread.join();
//					}
//					catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//				return false;
//			}
//			phase1 = false;
//			phase2 = true;
//			while (waitingThreads != threadCount) {
//				try {
//					notifyAll();
//					wait();
//				}
//				catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			if (stop) {
//				for (TransientMarkovMatrixMultiplyThread thread : threads) {
//					try {
//						thread.join();
//					}
//					catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//				return false;
//			}
//			phase1 = true;
//			phase2 = false;
//			if (progress != null) {
//				progress.setValue(progressValue + ((int) ((timeLimit * k) / K)));
//			}
//			/*
//			 * for (State m : stateGraph) { //for (String state :
//			 * stateGraph.keySet()) { // for (State m : stateGraph.get(state)) {
//			 * double nextProb = m.getCurrentProb() (1 -
//			 * (m.getTransitionSum(0.0, null) / Gamma)); for
//			 * (StateTransitionPair prev : m.getPrevStatesWithTrans()) { // if
//			 * (lhpn.getTransitionRateTree(prev.getTransition()) // != null) {
//			 * // prob = //
//			 * lhpn.getTransitionRateTree(prev.getTransition()).evaluateExpr
//			 * (prev.getState().getVariables()); // } nextProb +=
//			 * (prev.getState().getCurrentProb() * (prev.getTransition() /
//			 * Gamma)); } m.setNextProb(nextProb * ((Gamma * timeLimit) / k));
//			 * m.setPiProb(m.getPiProb() + m.getNextProb()); //} } if (stop) {
//			 * return false; } for (State m : stateGraph) { //for (String state
//			 * : stateGraph.keySet()) { //for (State m : stateGraph.get(state))
//			 * { m.setCurrentProbToNext(); //} } progress.setValue(progressValue
//			 * + ((int) ((timeLimit * k) / K)));
//			 */
//		}
//		try {
//			while (waitingThreads != 0) {
//				try {
//					notifyAll();
//					wait(10);
//				}
//				catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			for (TransientMarkovMatrixMultiplyThread thread : threads) {
//				thread.join();
//			}
//		}
//		catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		if (!stop) {
//			for (PrjState m : globalStateSet.keySet()) {
//				((ProbGlobalState) m).setPiProb(((ProbGlobalState) m).getPiProb() * (Math.pow(Math.E, ((0 - Gamma) * timeLimit))));
//				((ProbGlobalState) m).setCurrentProbToPi();
//				// }
//			}
//		}
//		if (stop) {
//			return false;
//		}
//		return true;
//	}
//
////	public synchronized void transientMarkovMatrixMultiplication(int startIndex, int endIndex, double Gamma,
////			double timeLimit, int K) {
//	public synchronized void transientMarkovMatrixMultiplication(int startIndex, int endIndex, double Gamma,
//			double timeLimit, int K, ProbGlobalStateSet globalStateSet) {
//		for (int k = 1; k <= K && !stop; k++) {
//			for (int i = startIndex; i < endIndex; i++) {
//				// TODO: Do we need the global state graph to be ordered?
//				State m = stateGraph.get(i);
//				double nextProb = m.getCurrentProb() * (1 - (m.getTranRateSum() / Gamma));
//				// TODO: getPrevStatesWithTrans ?
//				for (StateTransitionPair prev : m.getPrevStatesWithTrans()) {
//					if (prev.isEnabled()) {
//						nextProb += (prev.getState().getCurrentProb() * (prev.getTransition() / Gamma));
//					}
//				}
//				m.setNextProb(nextProb * ((Gamma * timeLimit) / k));
//				m.setPiProb(m.getPiProb() + m.getNextProb());
//				if (stop) {
//					waitingThreads--;
//					notifyAll();
//					return;
//				}
//			}
//			waitingThreads--;
//			if (waitingThreads == 0) {
//				notifyAll();
//			}
//			try {
//				while (!phase2 && !stop) {
//					wait();
//				}
//			}
//			catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			for (int i = startIndex; i < endIndex; i++) {
//				ProbGlobalState m = (ProbGlobalState) globalStateSet.keySet();
//				m.setCurrentProbToNext();
//				if (stop) {
//					waitingThreads++;
//					notifyAll();
//					return;
//				}
//			}
//			waitingThreads++;
//			if (waitingThreads == threadCount) {
//				notifyAll();
//			}
//			try {
//				while (!phase1 && !stop) {
//					wait();
//				}
//			}
//			catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			if (stop) {
//				return;
//			}
//		}
//		waitingThreads--;
//	}
//	
//	public void pruneStateGraph(String condition, ProbGlobalStateSet globalStateSet) {
//		//for (State m : stateGraph) {
//		for (PrjState m : globalStateSet.keySet()) {
//			ExprTree expr = new ExprTree();
//			expr.token = expr.intexpr_gettok(condition);
//			expr.intexpr_L(condition);
//			if (expr.evaluateExpr(m.getVariables()) == 1.0) {
//				//for (State s : m.getNextStates()) {
//				for (PrjState s : ((ProbGlobalState) m).getNextProbGlobalStateSet(globalStateSet)) {
//					// TODO: getPrevStatesWithTrans ?
//					for (StateTransitionPair trans : s.getPrevStatesWithTrans()) {
//						if (trans.getState() == m) {
//							trans.setEnabled(false);
//						}
//					}
//				}
//				// TODO: getPrevStatesWithTrans ?
//				for (StateTransitionPair trans : m.getNextStatesWithTrans()) {
//					trans.setEnabled(false);
//				}				
//			}
//			((ProbGlobalState) m).setTranRateSum(-1);
//		}
//	}
//	
//	public static void enableAllTransitions(ProbGlobalStateSet globalStateSet) {
//		//for (State m : stateGraph) {
//		for (PrjState m : globalStateSet.keySet()) {
//			for (StateTransitionPair trans : m.getNextStatesWithTrans()) {
//				trans.setEnabled(true);
//			}
//			for (StateTransitionPair trans : m.getPrevStatesWithTrans()) {
//				trans.setEnabled(true);
//			}
//			m.setTranRateSum(-1);
//		}
//	}

	@Override
	public void run() {		
		
	}
		
}
