package verification.timed_state_exploration.zoneProject;

import java.util.ArrayList;
import java.util.Iterator;

import lpn.parser.Transition;

/**
 * An EventSet represents a transition to fire or a set of inequalities that must
 * fire together. 
 * 
 * @author Andrew N. Fisher
 *
 */
public class EventSet extends Transition implements Iterable<Event>{

	/*
	 * Abstraction Function : 
	 */
	
	
	/*
	 * Representation Invariant :
	 * Exactly one of the fields '_transition' or '_inequalities' should be non-null.
	 * Testing for null is how this class determines whether it represents a 
	 * Transition or a set of inequalities.
	 */
	
	// A variable indicating whether we are a transition or a set of inequalities
	// may not be need since we could test for null or non-null.
	// Indicates whether this EventSet is a transitions or a set of inequalities.
//	boolean _isTransition;
	
	
	// The transition to fire.
	Transition _transition;
	
	// The set of inequalities.
	ArrayList<InequalityVariable> _inequalities;
	
	/**
	 * Determines whether this EventSet represents a Transition.
	 * @return
	 * 		True if this EventSet represents a Transition; false otherwise.
	 */
	public boolean isTransition(){
		return _transition != null;
	}
	
	/**
	 * Determines whether the EventSet represents a rate event.
	 * @return
	 * 		True if this EventSet represents a rate event; flase otherwise.
	 */
	public boolean isRate(){
		return false;
	}
	
	public void insert(Event e){
		
	}
	
	public Iterator<Event> iterator(){
		return new EventSetIterator();
	}
	
	public EventSet clone(){
		return null;
	}
	
	public void remove(Event e){
		
	}
	
	public void add(Event e){
		
	}
	
	public int size(){
		return 0;
	}
	
	public boolean isEmpty(){
		return false;
	}
	
	/*
	 * -------------------------------------------------------------------------------------
	 *                                      Inner Class
	 * -------------------------------------------------------------------------------------
	 */
	
	/**
	 * This is the custom iterator for the EventSet class.
	 * @author Andrew N. Fisher
	 *
	 */
	private class EventSetIterator implements Iterator<Event>{

		/*
		 * Abstraction Function : The Iterator operates in one of two modes: the
		 * Transition mode and the Inequality mode. If the _inequal variable
		 * is null, then the mode is the Transition mode, otherwise it is the
		 * Inequality mode.
		 */
		
		/*
		 * Representation Invariant : If the Iterator is created in a given mode,
		 * then it should stay in that mode. The mode is determined by whether
		 * the _ineq variable is null or not.
		 */
		
		// Stores the single transition if the Iterator is in the Transition mode.
		Transition _tran;
		
		// Stores the ArrayList<Inequality> objects iterator if the Iterator is in the
		// Inequality mode.
		Iterator<InequalityVariable> _inequal;
		
		/**
		 * The constructor initializes an Iterator in one of two modes : the Transition
		 * mode or the the Inequality mode. This mode is set once the Iterator is created.
		 */
		public EventSetIterator(){
			if(_transition == null && _inequalities == null){
				throw new IllegalStateException("The EventSet has both a transition" +
						" and a set of inequalities.");
			}
			
			if(EventSet.this._inequalities != null){
				_inequal = EventSet.this._inequalities.iterator();
			}
			else{
				_tran = EventSet.this._transition;
			}
		}
		
		@Override
		public boolean hasNext() {
			
			if(_inequal != null){
				return _inequal.hasNext();
			}
			
			return _tran != null;
		}

		@Override
		public Event next() {
			if(_inequal != null){
				return new Event(_inequal.next());
			}
			
			if(_tran == null){
				throw new IllegalStateException("No more elements to return.");
			}
			
			return new Event(_tran);
		}

		@Override
		public void remove() {
			
			throw new UnsupportedOperationException("Remove is not supported by" +
					" the EventSet's iterator.");
		}
		
	}
	
}
