package verification.timed_state_exploration.archive;


public class Timer extends OctMember {

	/*
	 * Abstraction Function: Represents a timer associated with a clock.
	 * In this representation, super._range caches the delay.
	 */
	
	/*
	 * Representation Invariant:
	 * super._range -> delay.
	 */
	
	/**
	 * Get the cached delay.
	 * @return The current delay.
	 */
	public Interval getDelay(){
		return super.get_range();
	}
	
	/**
	 * Sets the current delay.
	 * @param delay The current delay.
	 */
	public void setDelay(Interval delay){
		super.set_range(delay);
	}
}
