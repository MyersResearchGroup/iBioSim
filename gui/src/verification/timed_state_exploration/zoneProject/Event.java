package verification.timed_state_exploration.zoneProject;

import lpn.parser.Transition;

public class Event {

	/**
	 * Determines whether this Event represents a Transition.
	 * @return
	 * 		True if this EventSet represents a Transition; false otherwise.
	 */
	public boolean isTransition() {
		
		return false;
	}

	
	/**
	 * Determines whether the EventSet represents a rate event.
	 * @return
	 * 		True if this EventSet represents a rate event; flase otherwise.
	 */
	public boolean isRate() {
		
		return false;
	}
	
	public boolean isInequality(){
		return false;
	}
	
	
	public InequalityVariable getInequalityVariable(){
		return null;
	}
	
	public Transition getTransition(){
		return null;
	}
}
