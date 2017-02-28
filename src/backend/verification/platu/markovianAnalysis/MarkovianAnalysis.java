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
package backend.verification.platu.markovianAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JProgressBar;

import backend.verification.platu.project.PrjState;
import backend.verification.platu.stategraph.State;
import dataModels.lpn.parser.ExprTree;
import dataModels.lpn.parser.Transition;
import dataModels.lpn.parser.Translator;
import dataModels.util.dataparser.DataParser;
import dataModels.util.exceptions.BioSimException;

public class MarkovianAnalysis implements Runnable{
	
	private boolean stop;
	private DataParser probData;
	private int waitingThreads, threadCount;
	private boolean phase1, phase2;	
	/**
	 * This list includes names for all variables created during analysis of nested probability
	 * (name format:  "Pr" + nestedPropString.hashCode() or "St" + nestedPropString.hashCode()
	 */
	private ArrayList<String> nestedProbIDs;	
	/**
	 * This list includes the names of all LPN discrete (integer) variables.
	 * Variables that are created during analysis of nested properties will be added to this list too.
	 */
	private ArrayList<String> varNameList;
	
	private ProbGlobalStateSet globalStateSet;
	
	
	public MarkovianAnalysis(ProbGlobalStateSet globalStateSet) {
		this.globalStateSet = globalStateSet;
		varNameList = new ArrayList<String>();
		for (State initLocal : this.globalStateSet.getInitialState().toStateArray()) {
			for (String intVarName : initLocal.getLpn().getIntVars())
				if (!varNameList.contains(intVarName))
					varNameList.add(intVarName);
		}
	}	
	
	public boolean performSteadyStateMarkovianAnalysis(double tolerance, ArrayList<Property> props,
			PrjState initial, JProgressBar progress) throws BioSimException {
		// Moved the test to perform Markovian analysis to the run method in Verification.java, where LPN(s) are loaded
		// or generated from decomposition. 
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
		//enableAllTransitions();
		setAllGlobalStatesAsNonAbsorbing();
		// --------------------
		boolean change = false;
		boolean converged = false;
		int counter = 0;
		computeTransitionRateSum();
		// Set initial probability and period for all global states.		
		if (!stop) {
			for (PrjState m : globalStateSet.keySet()) {
				((ProbGlobalState) m).setCurrentProb(0.0);
			}
			((ProbGlobalState) initial).setCurrentProb(1.0);
		}
		resetColorsForMarkovianAnalysis();
		int period = findPeriod(initial);
		System.out.println("period = " + period);
		if (period == 0) {
			period = 1;
		}
		while (!converged && !stop) {
			counter++;
			counter = counter % period;
			for (PrjState curGlobalSt : globalStateSet.keySet()) {				
//				for (Transition outTran : ((ProbGlobalState) curGlobalSt).getOutgoingTranSetForProbGlobalState()) {
//					PrjState nextGlobalSt = ((ProbGlobalState) curGlobalSt).getNextGlobalState(outTran);
				for (Transition outTran : curGlobalSt.getNextGlobalStateMap().keySet()) {
					PrjState nextGlobalSt = curGlobalSt.getNextGlobalStateMap().get(outTran);
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
		}
		for (PrjState curGlobalSt : globalStateSet.keySet()) {
			((ProbGlobalState) curGlobalSt).setPiProb(0.0);
		}
		for (int i=0; i<period; i++) {
			for (PrjState curGlobalSt : globalStateSet.keySet()) {
//				for (Transition outTran : ((ProbGlobalState) curGlobalSt).getOutgoingTranSetForProbGlobalState()) {
//					PrjState nextGlobalSt = ((ProbGlobalState) curGlobalSt).getNextGlobalState(outTran);	
				for (Transition outTran : curGlobalSt.getNextGlobalStateMap().keySet()) {
					PrjState nextGlobalSt = curGlobalSt.getNextGlobalStateMap().get(outTran);
					double nextStNextProb = ((ProbGlobalState) nextGlobalSt).getNextProb();
					double curStCurProb = ((ProbGlobalState) curGlobalSt).getCurrentProb();
					double tranProb = ((ProbGlobalState) curGlobalSt).getTranProb(outTran);
					((ProbGlobalState) nextGlobalSt).setNextProb(nextStNextProb + curStCurProb * tranProb);
					if (stop) {
						return false;
					}
				}
			}			
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
		}		
		double normalizationFactor = 0;
		for (PrjState curGlobalSt : globalStateSet.keySet()) {
			double piProb = ((ProbGlobalState) curGlobalSt).getPiProb();
			double tranRateSum = ((ProbGlobalState) curGlobalSt).getTranRateSum();
			//((ProbGlobalState) curGlobalSt).setCurrentProb(piProb/period);
			((ProbGlobalState) curGlobalSt).setCurrentProb(piProb/period/tranRateSum);
			normalizationFactor += ((ProbGlobalState) curGlobalSt).getCurrentProb();
		}
		System.out.println("normalizationFactor = " + normalizationFactor);
		if (stop) {
			return false;
		}		
		for (PrjState curGlobalSt : globalStateSet.keySet()) {
			double curProb = ((ProbGlobalState) curGlobalSt).getCurrentProb();
			((ProbGlobalState) curGlobalSt).setCurrentProb(curProb/normalizationFactor);
		}
		//printStateSetStatus(globalStateSet, "end of steady state analysis");		
		return true;
	}

//	private void printStateSetStatus(ProbGlobalStateSet globalStateSet,
//			String location) {
//		System.out.println("------- Prob Global States status @ " + location + "----------------------");
//		for (PrjState curGlobalSt : globalStateSet.keySet()) {
//			if (((ProbGlobalState) curGlobalSt).getCurrentProb() > 1) {
//				System.out.println(curGlobalSt.getLabel() + ", curProb = " 
//						+ ((ProbGlobalState) curGlobalSt).getCurrentProb() + ", exceeds 1.");
//				//System.exit(1);
//				//return;
//			}
//			if (((ProbGlobalState) curGlobalSt).getNextProb() > 1) {
//				System.out.println(curGlobalSt.getLabel() + ", nextProb = " 
//						+ ((ProbGlobalState) curGlobalSt).getNextProb() + ", exceeds 1.");
//				//System.exit(1);
//				//return;
//			}
//			if (((ProbGlobalState) curGlobalSt).getPiProb() > 1) {
//				System.out.println(curGlobalSt.getLabel() + ", piProb = " 
//						+ ((ProbGlobalState) curGlobalSt).getPiProb() + ", exceeds 1.");
//				//System.exit(1);
//				//return;
//			}
//			System.out.println(curGlobalSt.getLabel() 
//					+ ",\t curProb = " + ((ProbGlobalState) curGlobalSt).getCurrentProb()
//					+ ",\t nextProb = " + ((ProbGlobalState) curGlobalSt).getNextProb() 
//					+ ",\t piProb = " + ((ProbGlobalState) curGlobalSt).getPiProb());
//		}
//		System.out.println("------------------------------------------------------------------------");
//	}

	private void computeTransitionRateSum() {
		//System.out.println("--------------- Transition Rate Sum -------------");
		for (PrjState curGlobalSt : globalStateSet.keySet()) {
			((ProbGlobalState) curGlobalSt).computeTranRateSum();
			//System.out.println(curGlobalSt.getLabel() + ", tranRateSum = " + ((ProbGlobalState) curGlobalSt).getTranRateSum());
		}		
	}

	private void resetColorsForMarkovianAnalysis() {
		for (PrjState m : globalStateSet.keySet()) {
			((ProbGlobalState) m).setColor(-1);
			if (stop) {
				return;
			}
		}
	}
	
	private int findPeriod(PrjState curSt) {
		if (stop) {
			return 0;
		}
		int period = 0;
		int color = 0;
		((ProbGlobalState) curSt).setColor(color);
		color = ((ProbGlobalState) curSt).getColor() + 1;
		Queue<PrjState> unVisitedStates = new LinkedList<PrjState>();
		//for (PrjState nextSt : ((ProbGlobalState) curSt).getNextProbGlobalStateSet(globalStateSet)) {
		for (PrjState nextSt : curSt.getNextGlobalStateMap().values()) {
			if (((ProbGlobalState) nextSt).getColor() == -1) {
				((ProbGlobalState) nextSt).setColor(color);
				unVisitedStates.add(nextSt);
			}
			else {
				if (period == 0) {
					period = ((ProbGlobalState) curSt).getColor() - ((ProbGlobalState) nextSt).getColor() + 1;
				}
				else {
					period = gcd(((ProbGlobalState) curSt).getColor() - ((ProbGlobalState) nextSt).getColor() + 1, period);
				}
			}
			if (stop) {
				return 0;
			}
		}
		while (!unVisitedStates.isEmpty() && !stop) {
			curSt = unVisitedStates.poll();
			color = ((ProbGlobalState) curSt).getColor() + 1;
			//for (PrjState nextSt : ((ProbGlobalState)curSt).getNextProbGlobalStateSet(globalStateSet)) {
			for (PrjState nextSt : curSt.getNextGlobalStateMap().values()) {
				if (((ProbGlobalState) nextSt).getColor() == -1) {
					((ProbGlobalState) nextSt).setColor(color);
					unVisitedStates.add(nextSt);
				}
				else {
					if (period == 0) {
						period = (((ProbGlobalState) curSt).getColor() - ((ProbGlobalState) nextSt).getColor() + 1);
					}
					else {
						period = gcd(((ProbGlobalState) curSt).getColor() - ((ProbGlobalState) nextSt).getColor() + 1, period);
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
	
	//private String removeNesting(double error, double timeStep, String prop, JProgressBar progress) {
	private String removeNesting(double error, double timeStep, String prop, JProgressBar progress) throws BioSimException {
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

	private String determineNestedProbability(double error, double timeStep, String property) throws BioSimException {
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
		if (!this.nestedProbIDs.contains(id)) {
			nestedProbIDs.add(id);			
			boolean globallyTrue = false;
			if (prop.contains("PF")) {
				condition[0] = "true";
			}
			else if (prop.contains("PG")) {
				condition[0] = "true";
				globallyTrue = true;
			}
			for (PrjState initial : globalStateSet.keySet()) {
				// Previously, we checked if (!this.nestedProbIDs.contains(id)) already.
//				if (((ProbGlobalState) initial).getNestedProbValues().keySet().contains(id)) {
//					break;
//				}
				//enableAllTransitions();
				setAllGlobalStatesAsNonAbsorbing();
				double Gamma;
				for (PrjState m : globalStateSet.keySet()) {
					((ProbGlobalState) m).setCurrentProb(0.0);
					((ProbGlobalState) m).setPiProb(0.0);
				}
				((ProbGlobalState) initial).setCurrentProb(1.0);
				((ProbGlobalState) initial).setPiProb(1.0);
				double lowerbound = 0;
				if (!condition[2].equals("")) {					
					ExprTree expr = new ExprTree(varNameList);
					expr.token = expr.intexpr_gettok(condition[2]);
					expr.intexpr_L(condition[2]);
					lowerbound = expr.evaluateExpr(null);
					pruneStateGraph("~(" + condition[0] + ")");
					// Compute Gamma
					Gamma = 0;
					for (PrjState m : globalStateSet) {
						//Gamma = Math.max(m.getTransitionSum(0.0, null), Gamma);
						Gamma = Math.max(((ProbGlobalState) m).getTranRateSum(), Gamma);
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
					ExprTree expr = new ExprTree(varNameList);
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
				for (PrjState m : globalStateSet) {				
					Gamma = Math.max(((ProbGlobalState) m).getTranRateSum(), Gamma);
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
				//double timelimitProb = 0;
				double successProb = 0;
				for (PrjState m : globalStateSet) {				
					// for (String state : stateGraph.keySet()) {
					// for (State m : stateGraph.get(state)) {
					ExprTree failureExpr = new ExprTree(varNameList);
					failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~(" + condition[1] + ")");
					failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
					ExprTree successExpr = new ExprTree(varNameList);
					if (globallyTrue) {
						successExpr.token = successExpr.intexpr_gettok("~(" + condition[1] + ")");
						successExpr.intexpr_L("~(" + condition[1] + ")");
					}
					else {
						successExpr.token = successExpr.intexpr_gettok(condition[1]);
						successExpr.intexpr_L(condition[1]);
					}
					if (successExpr.evaluateExpr(((ProbGlobalState) m).getVariables()) == 1.0) {
						successProb += ((ProbGlobalState) m).getCurrentProb();
					}
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
					// }
				}
				if (globallyTrue) {
					successProb = 1 - successProb;
					//timelimitProb = 1 - (failureProb + successProb);
				}
				((ProbGlobalState) initial).addNestedProb(id, "" + successProb);
			}
		}
		return id;
	}
	
	public static Property createProperty(String label, String property) {
		return new Property(label, property);
	}
	
	public boolean performTransientMarkovianAnalysis(double timeLimit, double timeStep, double printInterval,
			double error, String[] condition, JProgressBar progress, boolean globallyTrue) throws BioSimException {
		// Moved the test for the ability to perform Markovian analysis to the run method in Verification.java, 
		// where LPN(s) are loaded or generated from decomposition.	
		computeTransitionRateSum();
		if (condition != null) {
			condition[0] = removeNesting(error, timeStep, condition[0], progress);
			condition[1] = removeNesting(error, timeStep, condition[1], progress);
			progress.setString(null);
			double nextPrint = printInterval;
			if (condition[3].equals("inf")) {
				progress.setIndeterminate(true);
			}
			else {				
				ExprTree expr = new ExprTree(varNameList);
				expr.token = expr.intexpr_gettok(condition[3]);
				expr.intexpr_L(condition[3]);
				progress.setMaximum((int) expr.evaluateExpr(null));
			}
			//enableAllTransitions();
			setAllGlobalStatesAsNonAbsorbing();
			ProbGlobalState initial = (ProbGlobalState) globalStateSet.getInitialState();			
			if (initial != null) {
				for (PrjState m : globalStateSet.keySet()) {
					((ProbGlobalState) m).setCurrentProb(0.0);
					((ProbGlobalState) m).setPiProb(0.0);
				}
				initial.setCurrentProb(1.0);
				initial.setPiProb(1.0);				
			}
			double lowerbound = 0;
			if (!condition[2].equals("")) {				
				ExprTree expr = new ExprTree(varNameList);
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
			for (PrjState m : globalStateSet.keySet()) {
				ExprTree failureExpr = new ExprTree(varNameList);
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
				if (failureExpr.evaluateExpr(((ProbGlobalState) m).getVariables()) == 1.0) {
					failureProb += ((ProbGlobalState) m).getCurrentProb();
				}
			}
			temp.add(failureProb * 100);
			data.add(temp);
			// if (globallyTrue) {
			double successProb = 0;
			if (lowerbound == 0) {
				for (PrjState m : globalStateSet.keySet()) {									
					ExprTree successExpr = new ExprTree(varNameList);
					successExpr.token = successExpr.intexpr_gettok(condition[1]);
					successExpr.intexpr_L(condition[1]);
					if (successExpr.evaluateExpr(((ProbGlobalState) m).getVariables()) == 1.0) {
						successProb += ((ProbGlobalState) m).getCurrentProb();
					}
				}
			}
			temp = new ArrayList<Double>();
			temp.add(successProb * 100);
			data.add(temp);
			probData = new DataParser(dataLabels, data);
			if (initial != null) {
				if (!condition[2].equals("")) {
					if (!condition[0].equals("true")) {
						pruneStateGraph("~(" + condition[0] + ")");
					}
					// Compute Gamma
					Gamma = 0;
					for (PrjState m : globalStateSet.keySet()) 
						Gamma = Math.max(((ProbGlobalState) m).getTranRateSum(), Gamma);					
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
						for (PrjState m : globalStateSet.keySet()) {							
							ExprTree expr = new ExprTree(varNameList);
							expr.token = expr.intexpr_gettok("~(" + condition[0] + ")");
							expr.intexpr_L("~(" + condition[0] + ")");
							if (expr.evaluateExpr(((ProbGlobalState) m).getVariables()) == 1.0) {
								prob += ((ProbGlobalState) m).getCurrentProb();
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
					ExprTree expr = new ExprTree(varNameList);
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
				for (PrjState m : globalStateSet.keySet())
					Gamma = Math.max(((ProbGlobalState) m).getTranRateSum(), Gamma);
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
					//printStateSetStatus(globalStateSet, "middle of transient analysis");
					failureProb = 0;
					successProb = 0;
					// for (String state : stateGraph.keySet()) {
					// for (State m : stateGraph.get(state)) {
					//for (State m : stateGraph) {
					for (PrjState m : globalStateSet.keySet()) {												
						ExprTree failureExpr = new ExprTree(varNameList);
						failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~(" + condition[1]
								+ ")");
						failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");						
						ExprTree successExpr = new ExprTree(varNameList);
						if (globallyTrue) {
							successExpr.token = successExpr.intexpr_gettok("~(" + condition[1] + ")");
							successExpr.intexpr_L("~(" + condition[1] + ")");
						}
						else {
							successExpr.token = successExpr.intexpr_gettok(condition[1]);
							successExpr.intexpr_L(condition[1]);
						}
						if (failureExpr.evaluateExpr(((ProbGlobalState) m).getVariables()) == 1.0) {
							failureProb += ((ProbGlobalState) m).getCurrentProb();
						}
						else if (successExpr.evaluateExpr(((ProbGlobalState) m).getVariables()) == 1.0) {
							successProb += ((ProbGlobalState) m).getCurrentProb();
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
				for (PrjState m : globalStateSet.keySet()) {
					ExprTree failureExpr = new ExprTree(varNameList);
					failureExpr.token = failureExpr.intexpr_gettok("~(" + condition[0] + ")&~(" + condition[1] + ")");
					failureExpr.intexpr_L("~(" + condition[0] + ")&~(" + condition[1] + ")");
					ExprTree successExpr = new ExprTree(varNameList);
					if (globallyTrue) {
						successExpr.token = successExpr.intexpr_gettok("~(" + condition[1] + ")");
						successExpr.intexpr_L("~(" + condition[1] + ")");
					}
					else {
						successExpr.token = successExpr.intexpr_gettok(condition[1]);
						successExpr.intexpr_L(condition[1]);
					}
					if (failureExpr.evaluateExpr(((ProbGlobalState) m).getVariables()) == 1.0) {
						failureProb += ((ProbGlobalState) m).getCurrentProb();
					}
					else if (successExpr.evaluateExpr(((ProbGlobalState) m).getVariables()) == 1.0) {
						successProb += ((ProbGlobalState) m).getCurrentProb();
					}
					else {
						if (!globallyTrue) {
							timelimitProb += ((ProbGlobalState) m).getCurrentProb();
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
				/*
				String result1 = "#total";
				String result2 = "1.0";
				for (String s : output.keySet()) {
					result1 += " " + s;
					result2 += " " + output.get(s);
				}
				*/
				// --- Temp: Print results -----
				System.out.println("Failure = " + failureProb);
				System.out.println("Success = " + successProb);
				System.out.println("Timelimit = " + timelimitProb);
				// -----------------------------
				return true;
			}
			return false;
		}
		progress.setMaximum((int) timeLimit);
		ProbGlobalState initial = (ProbGlobalState) globalStateSet.getInitialState();			
		if (initial != null) {
			initial.setCurrentProb(1.0);
			initial.setPiProb(1.0);
			boolean stop = false;
			// Compute Gamma
			double Gamma = 0;
			for (PrjState m : globalStateSet.keySet())
				Gamma = Math.max(((ProbGlobalState) m).getTranRateSum(), Gamma);				
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
		Object[] globalStArray = globalStateSet.keySet().toArray();
		for (int i = 0; i < globalStArray.length; i++) {
			ProbGlobalState m = (ProbGlobalState) globalStArray[i];
			if (m.isAbsorbing()) {
				m.setNextProb(m.getCurrentProb());
			}
			else {
				m.setNextProb(m.getCurrentProb() * (1 - (m.getTranRateSum() / Gamma)));
			}
		}
		ArrayList<TransientMarkovMatrixMultiplyThread> threads = new ArrayList<TransientMarkovMatrixMultiplyThread>();
		for (int i = 0; i < threadCount; i++) {
			TransientMarkovMatrixMultiplyThread thread = new TransientMarkovMatrixMultiplyThread(this);
			thread.start((i * globalStateSet.keySet().size()) / threadCount, ((i + 1) * globalStateSet.keySet().size()) / threadCount, Gamma,
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
			for (PrjState m : globalStateSet.keySet()) {
				((ProbGlobalState) m).setPiProb(((ProbGlobalState) m).getPiProb() * (Math.pow(Math.E, ((0 - Gamma) * timeLimit))));
				((ProbGlobalState) m).setCurrentProbToPi();
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
		Object[] globalStateArray = globalStateSet.keySet().toArray();		
		for (int k = 1; k <= K && !stop; k++) {
			for (int i = startIndex; i < endIndex; i++) {
				ProbGlobalState curState = (ProbGlobalState) globalStateArray[i];
				if (!curState.isAbsorbing()) {
//					for (Transition outTran : curState.getOutgoingTranSetForProbGlobalState()) {
//						ProbGlobalState nextState = (ProbGlobalState) curState.getNextGlobalState(outTran);
					for (Transition outTran : curState.getNextGlobalStateMap().keySet()) {
						PrjState nextState = curState.getNextGlobalStateMap().get(outTran);
						try {
							((ProbGlobalState) nextState).lock.acquire();
							((ProbGlobalState) nextState).setNextProb(
									((ProbGlobalState) nextState).getNextProb() 
									+ (curState.getCurrentProb() * (curState.getOutgoingTranRate(outTran) / Gamma)));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						((ProbGlobalState) nextState).lock.release();
					}
				}			
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
				ProbGlobalState m = (ProbGlobalState) globalStateArray[i];
				m.setNextProb(m.getNextProb() * ((Gamma * timeLimit) / k));
				m.setPiProb(m.getPiProb() + m.getNextProb());
				m.setCurrentProbToNext();
				if (m.isAbsorbing()) {
					m.setNextProb(m.getCurrentProb());
				}
				else {
					m.setNextProb(m.getCurrentProb() * (1 - (m.getTranRateSum() / Gamma)));					
				}
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
		for (PrjState m : globalStateSet.keySet()) {
			ExprTree expr = new ExprTree(varNameList);
			expr.token = expr.intexpr_gettok(condition);
			expr.intexpr_L(condition);
			if (expr.evaluateExpr(((ProbGlobalState) m).getVariables()) == 1.0) {
				((ProbGlobalState) m).setAbsorbing(true);				
			}
		}
	}
//	
//	/**
//	 * This method do or undo pruning for a given transition based on the pruneFlag. 
//	 * 
//	 * @param pruneFlag
//	 * @param curProbGlobalSt
//	 * @param outTran
//	 */
//	public void pruneTransition(boolean pruneFlag, PrjState curProbGlobalSt, Transition outTran) {
//		// The local state index (in a global state) is the same as its corresponding lpn index. 
//		int localStateIndex = outTran.getLpn().getLpnIndex();
//		State localState = curProbGlobalSt.toStateArray()[localStateIndex];
//		ProbLocalStateGraph localSg = ((ProbLocalStateGraph) localState.getStateGraph());
//		double tranRate = localSg.getTranRate(localState, outTran);
//		if (pruneFlag && (tranRate > 0.0)) { // Set tranRate to negative since outTran is pruned.
//			localSg.setTranRate(localState, outTran, (-1* tranRate));
//		}
//		else if (!pruneFlag && (tranRate < 0.0)) { // Restore tranRate to positive.
//			localSg.setTranRate(localState, outTran, (-1* tranRate));
//		}
//	}
	
//	public void enableAllTransitions() {
//		// Need to consider the case where next global state map exists.
//		for (State localInitSt : globalStateSet.getInitialState().toStateArray()) {
//			ProbLocalStateGraph localSg = (ProbLocalStateGraph) localInitSt.getStateGraph(); 
//			HashMap<State, HashMap<Transition, Double>> nextTranRateMap = localSg.getNextTranRateMap();
//			for (State localSt : nextTranRateMap.keySet()) {
//				for (Transition outTran : localSt.getOutgoingTranSet()) {
//					if (localSg.getTranRate(localSt, outTran) < 0.0) {
//						double tranRate = localSg.getTranRate(localSt, outTran);
//						// Set tranRate to positive. We use negative rate to indicate a pruned transition.
//						localSg.setTranRate(localSt, outTran, Math.abs(tranRate));
//					}						
//				}
//			}
//		}						
//	}
	
	public void setAllGlobalStatesAsNonAbsorbing() {
		for (PrjState m : globalStateSet.keySet()) {
			((ProbGlobalState) m).setAbsorbing(false);				
		}
	}
	
	@Override
	public void run() {		
		
	}
		
}
