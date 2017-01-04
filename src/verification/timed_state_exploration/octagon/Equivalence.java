package verification.timed_state_exploration.octagon;

import java.util.Collection;
import java.util.HashSet;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;
import verification.platu.lpn.LpnTranList;
import verification.platu.stategraph.State;
import verification.timed_state_exploration.zoneProject.ContinuousRecordSet;
import verification.timed_state_exploration.zoneProject.EventSet;
import verification.timed_state_exploration.zoneProject.IntervalPair;
import verification.timed_state_exploration.zoneProject.LPNContinuousPair;
import verification.timed_state_exploration.zoneProject.LPNTransitionPair;

/**
 * The purpose of this interface is to define the methods of Zones
 * Octagons that other classes use.
 * 
 * @author Andrew N. Fisher
 *
 */
public interface Equivalence {

	public IntervalPair getContinuousBounds(String variable, LhpnFile lhpn);

	public int getCurrentRate(LPNTransitionPair index);

	public int timerIndexToDBMIndex(LPNTransitionPair index);

	public int getUpperBoundTrue(int index);
	
	public int getUnwarpedUpperBound(LPNContinuousPair lcpair);
	
	public int getLowerBoundTrue(int index);

	public LhpnFile[] get_lpnList();
	
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
