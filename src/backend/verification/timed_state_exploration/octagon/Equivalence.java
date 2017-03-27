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
package backend.verification.timed_state_exploration.octagon;

import java.util.Collection;
import java.util.HashSet;

import backend.verification.platu.platuLpn.LpnTranList;
import backend.verification.platu.stategraph.State;
import backend.verification.timed_state_exploration.zoneProject.ContinuousRecordSet;
import backend.verification.timed_state_exploration.zoneProject.EventSet;
import backend.verification.timed_state_exploration.zoneProject.IntervalPair;
import backend.verification.timed_state_exploration.zoneProject.LPNContinuousPair;
import backend.verification.timed_state_exploration.zoneProject.LPNTransitionPair;
import main.java.edu.utah.ece.async.lpn.parser.LPN;
import main.java.edu.utah.ece.async.lpn.parser.Transition;

/**
 * The purpose of this interface is to define the methods of Zones
 * Octagons that other classes use.
 * 
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public interface Equivalence {

	public IntervalPair getContinuousBounds(String variable, LPN lhpn);

	public int getCurrentRate(LPNTransitionPair index);

	public int timerIndexToDBMIndex(LPNTransitionPair index);

	public int getUpperBoundTrue(int index);
	
	public int getUnwarpedUpperBound(LPNContinuousPair lcpair);
	
	public int getLowerBoundTrue(int index);

	public LPN[] get_lpnList();
	
	public int getDbmEntry(int i, int j);
	
	public int getIndexByTransitionPair(LPNTransitionPair ltPair);
	
	public int getVarIndex(int lpnIndex, String name);

	public int getLowerBoundbyTransition(Transition t);

	public Collection<? extends Transition> getEnabledTransitions();

	public Collection<? extends Transition> getEnabledTransitions(int lpnIndex);

	public boolean subset(Equivalence equivalence);

	public boolean superset(Equivalence equivalence);

	public Collection<? extends Transition> getPossibleEvents(int lpnIndex,
			State state);

	public Equivalence fire(Transition firedTran,
			LpnTranList enabledTransitions,
			ContinuousRecordSet newAssignValues, State[] newStates);

	public Equivalence getContinuousRestrictedZone(EventSet eventSet,
			State[] states);

	public void recononicalize();

	public void advance(State[] states);

	public Equivalence resetRates();

	public Equivalence addTransition(HashSet<LPNTransitionPair> newlyEnabled,
			State[] states);

	public Equivalence fire(LPNTransitionPair firedRate, int currentRate);

	public Equivalence moveOldRateZero(LPNContinuousPair firedRate);

	public Equivalence saveOutZeroRate(LPNContinuousPair firedRate);

	public Equivalence clone();

	public void setCurrentRate(LPNTransitionPair firedRate, int currentRate);

	public void dbmWarp(Equivalence equivalence);

	public int getDbmEntryByPair(LPNTransitionPair zeroTimerPair,
			LPNTransitionPair index);

	public IntervalPair getRateBounds(LPNTransitionPair contVar);

	public IntervalPair getContinuousBounds(LPNContinuousPair contVar);

	public int rateResetValue(LPNTransitionPair contVar);
}
