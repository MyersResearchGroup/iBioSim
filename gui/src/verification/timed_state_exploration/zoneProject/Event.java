package verification.timed_state_exploration.zoneProject;

import lpn.parser.Transition;

/**
 * An Event is an action that is pending. This can be a transition to fire or an inequality.
 * @author Andrew N. Fisher
 *
 */
public class Event {

	/*
	 * Abstraction Function : An event simply represents a transition or an Inequality. Whichever the 
	 * event is representing is stored in the corresponding member variables _transition or _inequality.
	 */
	
	/*
	 * Representation Invariant : Exactly one of _transition or _inequality should be non-null at a given
	 * time.
	 */
	
	Transition _transition;
	
	InequalityVariable _inequality;
	
	/**
	 * Initializes the Event as a transition event.
	 * @param t
	 * 		The transition the event represents.
	 */
	public Event(Transition t){
		_transition = t;
	}
	
	/**
	 * Initializes the Event as an inequality event.
	 * @param v
	 * 		The inequality this event represents.
	 */
	public Event(InequalityVariable v){
		_inequality = v;
	}
	
	/**
	 * Determines whether this Event represents a Transition.
	 * @return
	 * 		True if this EventSet represents a Transition; false otherwise.
	 */
	public boolean isTransition() {
		
		return _transition != null;
	}

	
	/**
	 * Determines whether this Event represents a rate event.
	 * @return
	 * 		True if this EventSet represents a rate event; false otherwise.
	 */
	public boolean isRate() {
		
		return false;
	}
	
	/**
	 * Determines whether this Event represents an inequality.
	 * @return
	 * 		True if this Event is an inequality, false otherwise.	
	 */
	public boolean isInequality(){
		return _inequality != null;
	}
	
	/**
	 * Gets the inequality variable that this Event represents.
	 * @return
	 * 		The inequality variable if this Event represents an inequality, null otherwise.
	 */
	public InequalityVariable getInequalityVariable(){
		return _inequality;
	}
	
	/**
	 * Gets the transition that this Event represents.
	 * @return
	 * 		The transition if this Event represents a transition, null otherwise.
	 */
	public Transition getTransition(){
		return _transition;
	}
}
