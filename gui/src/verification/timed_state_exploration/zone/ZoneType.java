package verification.timed_state_exploration.zone;

import java.util.HashMap;
import java.util.List;

import lpn.parser.Transition;
import verification.platu.stategraph.State;

public interface ZoneType {

	/* Infinity is represented by the maximum integer value. */
	public static final int INFINITY = Integer.MAX_VALUE;

	/**
	 * Get the value of the upper bound for the delay.
	 * @param timer
	 * 			The timer's index.
	 * @return
	 * 			The upper bound on the transitions delay.
	 */
	public abstract int getUpperBoundbyTransitionIndex(int timer);

	/**
	 * Get the value of the upper bound for the delay.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @return
	 * 			The upper bound on the transitions delay.
	 */
	public abstract int getUpperBoundbydbmIndex(int index);

	/**
	 * Get the value of the lower bound for the delay.
	 * @param timer
	 * 			The timer's index.
	 * @return
	 * 			The value of the lower bound.
	 */
	public abstract int getLowerBoundbyTransitionIndex(int timer);

	/**
	 * Get the value of the lower bound for the delay.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @return
	 * 			The value of the lower bound.
	 */
	public abstract int getLowerBoundbydbmIndex(int index);

	/**
	 * Retrieves an entry of the DBM using the DBM's addressing.
	 * @param i
	 * 			The row of the DBM.
	 * @param j
	 * 			The column of the DBM.
	 * @return
	 * 			The value of the (i, j) element of the DBM.
	 */
	public abstract int getDbmEntry(int i, int j);

	/**
	 * Determines if a timer has reached its lower bound.
	 * @param timer
	 * 			The timer's index.
	 * @return
	 * 			True if the timer has reached its lower bound, false otherwise.
	 */
	public abstract boolean exceedsLowerBoundbyTransitionIndex(int timer);

	/**
	 * Determines if a timer has reached its lower bound.
	 * @param index
	 * 			The timer's row/column of the DBM.
	 * @return
	 * 			True if the timer has reached its lower bound, false otherwise.
	 */
	public abstract boolean exceedsLowerBoundbydbmIndex(int index);

	/**
	 * Updates the Zone according to a transition firing.
	 * @param timer
	 * 			The index of the transition that fired.
	 * @return
	 * 			The updated Zone.
	 */
	public abstract ZoneType fireTransitionbyTransitionIndex(int timer,
			int[] enabledTimers, State state);

	/**
	 * Updates the Zone according to the transition firing.
	 * @param index
	 * 			The index of the timer.
	 * @return
	 * 			The updated Zone.
	 */
	public abstract ZoneType fireTransitionbydbmIndex(int index,
			int[] enabledTimers, State state);

	/**
	 * Overrides the clone method from Object.
	 */
	public abstract ZoneType clone();

	/**
	 * The list of enabled timers.
	 * @return
	 * 		The list of all timers that have reached their lower bounds.
	 */
	public abstract List<Transition> getEnabledTransitions();
	
	/**
	 * Retrieve a lexicon that maps the internal numbers to the transition.
	 * @return
	 * 		The lexicon.
	 */
	public abstract HashMap<Integer, Transition> getLexicon();

}