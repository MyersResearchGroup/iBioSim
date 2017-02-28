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

import java.util.HashMap;
import java.util.concurrent.Semaphore;

import backend.verification.platu.project.PrjState;
import backend.verification.platu.stategraph.State;
import dataModels.lpn.parser.Transition;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ProbGlobalState extends PrjState {	
	
	private int color;
	
	private double currentProb;
	
	private double nextProb;
	
	private double piProb;
	
	/**
	 * This maps stores transition rate for each outgoing transition.
	 */
	private HashMap<Transition, Double> nextGlobalTranRateMap; 
	
	/**
	 * Sum of all outgoing transition rates from this state. 
	 */
	private double tranRateSum;
	
	/**
	 * This map stores for each state the probability value computed when checking
	 * a nested property. 
	 * key: "Pr" + nestedPropString.hashCode() or "St" + nestedPropString.hashCode()
	 * value: success probability
	 */
	private HashMap<String, String> nestedProbValues;
	
	public Semaphore lock;
	
	private boolean isAbsorbing;
	
	
	
	public ProbGlobalState(State[] other) {
		super(other);
		tranRateSum = 0.0;
		nextGlobalStateMap = new HashMap<Transition, PrjState>();	
		nextGlobalTranRateMap = new HashMap<Transition, Double>();
		lock = new Semaphore(1);
		isAbsorbing = false;
	}
	
	public boolean isAbsorbing() {
		return isAbsorbing;
	}

	public void setAbsorbing(boolean isAbsorbing) {
		this.isAbsorbing = isAbsorbing;
	}

	public HashMap<String, String> getNestedProbValues() {
		return nestedProbValues;
	}

	public void addNestedProb(String id, String value) {
		if (nestedProbValues == null)
			nestedProbValues = new HashMap<String, String>();
		nestedProbValues.put(id, value);
	}
	
	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public double getCurrentProb() {
		return currentProb;
	}

	public void setCurrentProb(double currentProb) {
		this.currentProb = currentProb;
	}

	public double getNextProb() {
		return nextProb;
	}

	public void setNextProb(double nextProb) {
		this.nextProb = nextProb;
	}
	
	public void setCurrentProbToNext() {
		currentProb = nextProb;
	}

//	public double getOutgoingTranRate(Transition tran) {
//		int curLocalStIndex = tran.getLpn().getLpnIndex();
//		State curLocalState = this.toStateArray()[curLocalStIndex];
//		double tranRate = ((ProbLocalStateGraph) curLocalState.getStateGraph()).getTranRate(curLocalState, tran);	
//		return tranRate;
//	}
	
	public double getOutgoingTranRate(Transition tran) {
		return nextGlobalTranRateMap.get(tran);
	}

	public double getTranProb(Transition tran) {
		double tranProb = 0.0;
		if (tranRateSum != 0)
			tranProb = getOutgoingTranRate(tran)/tranRateSum;
		else
			tranProb = 0.0;
		return tranProb;
	}

	public double getPiProb() {
		return piProb;
	}

	public void setPiProb(double piProb) {
		this.piProb = piProb;
	}
	
//	/**
//	 * If nextProbGlobalStateMap exists, this method returns the value field of it. 
//	 * If not, this method uses local state-transition information to search for the next global states of 
//	 * this current global state, and returns a set of such next global states. The search
//	 * is performed based on the local states that this current global state is composed of. For example, assume a current
//	 * global state S0 is composed of n (n>=1) local states: s_00,s_10,s_20...,s_n0. For each outgoing transition, t_k, of 
//	 * s_00, it finds in each of the local state, namely s_00, s_10, s_20, ..., s_n0, their next local states. It then 
//	 * pieces them together to form a next global state. Next, it grabs the equivalent one from the global state set. The obtained is a 
//	 * next global state reached from S0 by taking t_k. The whole process repeats for s_10, s_20, ..., s_n0. All obtained 
//	 * next global states are added to a set and returned by this method.
//	 * @param globalStateSet
//	 * @return
//	 */
//	public HashSet<ProbGlobalState> getNextProbGlobalStateSet(ProbGlobalStateSet globalStateSet) {
//		HashSet<ProbGlobalState> nextProbGlobalStateSet = new HashSet<ProbGlobalState>();
//		for (Transition outTran : nextGlobalStateMap.keySet())				
//			nextProbGlobalStateSet.add((ProbGlobalState) getNextGlobalState(outTran));
//		return nextProbGlobalStateSet;	
		//if (Options.getBuildGlobalStateGraph()) {			
//			for (Transition outTran : nextGlobalStateMap.keySet())				
//				nextProbGlobalStateSet.add((ProbGlobalState) getNextGlobalState(outTran));
//			return nextProbGlobalStateSet;						
		//}			
//		else {			
//			for (State localSt : this.toStateArray()) 
//				for (Transition outTran : localSt.getOutgoingTranSet()) 
//					nextProbGlobalStateSet.add((ProbGlobalState) getNextGlobalState(outTran, globalStateSet));							
//			return nextProbGlobalStateSet;
//		}			
//	}

//	/**
//	 * If nextProbGlobalStateMap exists, this method returns the value for the given outTran, else it calls 
//	 * getNextPrjState(Transition, HashMap<PrjState, PrjState>) in PrjState.java.  
//	 * @param outTran
//	 * @param globalStateSet
//	 * @return
//	 */
//	public PrjState getNextGlobalState(Transition outTran, ProbGlobalStateSet globalStateSet) {
//		if (Options.getBuildGlobalStateGraph())
//			return nextGlobalStateMap.get(outTran);
//		else {
//			State[] tmpNextStateArray = new State[this.toStateArray().length];
//			for (State curLocalSt : this.toStateArray()) {
//				State nextLocalSt = curLocalSt.getNextLocalState(outTran);
//				if (nextLocalSt != null) { // outTran leads curLocalSt to a next state.
//					tmpNextStateArray[curLocalSt.getLpn().getLpnIndex()] = nextLocalSt;
//				}
//				else { // No nextLocalSt was found. Transition outTran did not change this local state.
//					tmpNextStateArray[curLocalSt.getLpn().getLpnIndex()] = curLocalSt;
//				}
//			}
//			PrjState tmpPrjSt = new ProbGlobalState(tmpNextStateArray);			
//			if (((ProbGlobalStateSet)globalStateSet).get(tmpPrjSt) == null) {
//				String curGlobalStLabel = "";
//				String tmpPrjStLabel = "";
//				for (State s : this.toStateArray()) {
//					curGlobalStLabel  += s.getFullLabel() + "_";
//				}
//				for (State s : tmpPrjSt.toStateArray()) {
//					tmpPrjStLabel += s.getFullLabel() + "_"; 
//				}
//				String message = "Next global state was not found for current global state: " 
//						+ curGlobalStLabel.substring(0, curGlobalStLabel.length()-1) + ","
//						+ "\ngiven transition " + outTran.getFullLabel()
//						+ ".\ntmpPrjSt = " + tmpPrjStLabel.substring(0, tmpPrjStLabel.length()-1) + ".\n"
//						+ "Next local state map:\n";
//				for (State s : this.toStateArray()) {
//					if (s.getNextLocalState(outTran) == null)
//						message += s.getFullLabel() + " -> null\n";
//					else
//						message += s.getFullLabel() + " ->" + s.getNextLocalState(outTran).getFullLabel() + "\n";
//				}
//				NullPointerException npe = new NullPointerException(message);
//				throw npe;
//			}
//			return ((ProbGlobalStateSet) globalStateSet).get(tmpPrjSt);				
//		}
//	}

//	/**
//	 * If nextProbGlobalStateMap exists, this method returns the keySet of it. Otherwise, it calls 
//	 * getOutgoingTrans() in PrjState.java. 
//	 * @return
//	 */
//	public Set<Transition> getOutgoingTranSetForProbGlobalState() {
//		return nextGlobalStateMap.keySet();
////		if (Options.getBuildGlobalStateGraph())
////			return nextGlobalStateMap.keySet();
////		else {
////			Set<Transition> outgoingTrans = new HashSet<Transition>();
////			for (State curLocalSt : this.toStateArray())
////				outgoingTrans.addAll(curLocalSt.getOutgoingTranSet());
////			return outgoingTrans;
////		}
//	}

	public void computeTranRateSum() {		
		for (Transition tran : nextGlobalStateMap.keySet()) {			
			tranRateSum += getOutgoingTranRate(tran);
		}
	}

	public double getTranRateSum() {
		return tranRateSum;
	}

//	public boolean isExplored() {		
//		return isExplored;
//	}
//
//	public void markAsExplored() {
//		isExplored = true;	
//	}
//
//	public void resetExplored() {
//		isExplored = false;		
//	}

	public void setCurrentProbToPi() {
		currentProb = piProb;
	}

	public void setTranRateSum(double tranRateSum) {
		this.tranRateSum = tranRateSum;		
	}

	public HashMap<String, String> getVariables() {
		HashMap<String, String> varMap = new HashMap<String, String>();
		for (State localSt : this.toStateArray()) {
			varMap.putAll(localSt.getLpn().getAllVarsWithValuesAsString(localSt.getVariableVector()));			
		}
		if (nestedProbValues != null)
			varMap.putAll(nestedProbValues);
		return varMap;
	}

	public void addNextGlobalTranRate(Transition t, double tranRate) {
		this.nextGlobalTranRateMap.put(t, tranRate);		
	}
}
