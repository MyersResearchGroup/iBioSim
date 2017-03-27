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
package edu.utah.ece.async.verification.timed_state_exploration.zoneProject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import edu.utah.ece.async.lpn.parser.Transition;

/**
 * An EventSet represents a transition to fire, a set of inequalities that must
 * fire together, or a rate change. When the EventSet represents a single transition,
 * it is said to be in Transition mode. When the EventSet represents a list of 
 * inequalities, it is said to be in Inequality mode. When it represents a rate
 * rate change it is said to be in Rate mode.
 * 
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class EventSet extends Transition implements Iterable<Event>{

	/*
	 * Abstraction Function : An EventSet is a singleton set containing a Transition,
	 * a set of IneqaulityVariables, or a singleton set representing a rate change.
	 * Accordingly, an EventSet is said to operate in one
	 * of three modes: a Transition mode, an Inequality mode, or a Rate mode. When the
	 * EventSet contains no elements it is said to have No Mode. When the EventSet
	 * contains a single Transition it is stored as the _transition variable. When
	 * the EventSet contains a set InequalityVariables, they are stored in
	 * _inequalities. When the EventSet contains a rate change, the variable and new
	 * rate are stored as an LPNContinuousPair in the _rate variable.
	 */
	
	
	/*
	 * Representation Invariant :
	 * Exactly one of the fields '_transition', '_inequalities', '_rate' should be
	 * non-null. Testing for null is how this class determines whether it represents
	 * a Transition, a set of inequalities, or a rate change. All three variables can
	 * be null in which case the EventSet can be changed to either mode by storing
	 * something in the _transition, _inequalities or rate variables.
	 */
	
	// A variable indicating whether we are a transition or a set of inequalities
	// may not be need since we could test for null or non-null.
	// Indicates whether this EventSet is a transitions or a set of inequalities.
//	boolean _isTransition;
	
	
	// The transition to fire.
	Transition _transition;
	
	// The set of inequalities.
	ArrayList<InequalityVariable> _inequalities;
	
	// The rate to change.
	LPNContinuousPair  _rate;
	
	// Cached hash code value.
//	int _hashCode;
	
	/**
	 * Overrides the hashCode.
	 */
//	@Override
//	public int hashCode()
//	{
//		// Check if the hash code has been set.
//		if(_hashCode <0)
//		{
//			_hashCode = createHashCode();
//		}
//
//		return _hashCode;
//	}
	
	/**
	 * Creates a hash code for an EventSet object.
	 * @return
	 * 		The hash code.
	 */
//	private int createHashCode()
//	{
//		int newHashCode = 37;
//		
//		if(!(_transition == null)){
//			newHashCode ^= _transition.hashCode();
//		}
//		
//		if(!(_inequalities == null)){
//			for(InequalityVariable ineq : _inequalities){
//				newHashCode ^= ineq.hashCode();
//			}
//		}
//		
//		if(!(_rate == null)){
//			newHashCode ^= _rate.hashCode();
//		}
//		
//		return Math.abs(newHashCode);
//	}
	
	/**
	 * Creates an uninitialized EventSet. The mode of the EventSet is determined by the first use
	 * of the insert method. If a Transition event is added, then the EventSet will be in the Transition mode.
	 * If an inequality event is added, then the EventSet will be in the Inequality mode.
	 */
	public EventSet(){
		// The mode will be determined by the first element added into the EventSet.
	}
	
	/**
	 * Creates an EventSet in the Transition mode.
	 * @param transition
	 * 		The transition the EventSet should contain.
	 */
	public EventSet(Transition transition){
		_transition = transition;
	}
	
	/**
	 * Creates an EventSet in the Inequality mode.
	 * @param inequalities
	 * 		The list of inequalities that the EventSet should contain.
	 */
	public EventSet(Collection<? extends Event> inequalities){
		_inequalities = new ArrayList<InequalityVariable>();
		_inequalities.addAll(_inequalities);
	}
	
	/**
	 * Creates an EventSet in the Rate mod.
	 * @param rate
	 */
	public EventSet(LPNContinuousPair rate){
		_rate = rate;
	}
	
	/**
	 * Determines whether this EventSet represents a Transition.
	 * @return
	 * 		True if this EventSet represents a Transition; false otherwise.
	 */
	public boolean isTransition(){
		return _transition != null;
	}
	
	/**
	 * Determines whether this EventSet represents a set of Inequalities.
	 * @return
	 * 		True if this EventSet represents a set of inequalities; false otherwise.
	 */
	public boolean isInequalities(){
		return _inequalities != null;
	}
	
	/**
	 * Determines whether the EventSet represents a rate event.
	 * @return
	 * 		True if this EventSet represents a rate event; false otherwise.
	 */
	public boolean isRate(){
		return _rate != null;
	}
	
	/**
	 * Inserts an inequality event into the set of IneqaulityVariables when the EventSet is in
	 * the Inequality mode.
	 * @param e
	 * 
	 * @throws IllegalArgumentException
	 * 		Throws an IllegalArgumentException if in the Inequality mode and e
	 * 		is not an inequality event or if the EventSet is in the Transition mode.
	 */
	public void add(Event e){
		// Determine if the mode is the Inequality mode
		if(_inequalities != null){
			// We are in the inequality mode, now determine if the Event passed
			// is and inequality.
			if(e.isInequality()){
				// It is an inequality, so add it.
				_inequalities.add(e.getInequalityVariable());
			}
			else{
				// Tried to insert something other than an inequality into an
				// inequality list, so complain.
				throw new IllegalArgumentException("Cannot insert a non-inequality" +
						" into an EventSet of inequalities.");
			}
			return;
		}
		
		// We are not in the Inequality mode.
		// If we are also not in the Transition mode or Rate mode, then the new
		//event determines the mode.
		if(_transition == null || _rate == null){
			if(e.isInequality()){
				// The event is an inequality, so add it to the inequalities.
				// This also implies that the mode is the Inequality mode.
				_inequalities = new ArrayList<InequalityVariable>();
				_inequalities.add(e.getInequalityVariable());
			}
			else if(e.isTransition()){
				// The event is a Transition, so store it. This also implies the
				// mode is the Transition mode.
				_transition = e.getTransition();
			}
			else{
				// Since the event is not an inequality or a transition, it must
				// for a rate change.
				_rate = e.getRateChange();
			}
			
			return;
		}
		
		// We are in the Transition mode or Rate mode. Nothing can be added in the
		// these modes so yell.
		throw new IllegalArgumentException("Another event was attempted to be added" +
				"to an EventSet that already had a transition or rate in it.");
	}
	
	/**
	 * Returns an iterator that returns the elements in the set as Event objects.
	 */
	@Override
	public Iterator<Event> iterator(){
		return new EventSetIterator();
	}
	
	/**
	 * Clones the EventSet. Copies the internal objects by copying their reference. It does not make new instances
	 * of the contained objects.
	 */
	@Override
	public EventSet clone(){
		
		// Create a new EventSet instance.
		EventSet newSet = new EventSet();
		
		// Determine whether or not the EventSet is in the Inequality mode.
		if(_inequalities != null){
			// In the Inequality mode, we need to make a new ArrayList and copy the elements references over.
			newSet._inequalities = new ArrayList<InequalityVariable>();
			newSet._inequalities.addAll(this._inequalities);
		}
		
		else if (_transition != null){
			// In this case we are in the Transition mode. Simple copy the transition over.
			newSet._transition = this._transition;
		}
		else{
			// Since we are not in Inequality or Transition mode, we must be in Rate
			// mode (or have no mode). Simply copy the rate.
			newSet._rate = this._rate;
		}
		
		return newSet;
	}
	
	/**
	 * Removes an element from the EventSet.
	 * @param e
	 * 		The event to remove.
	 */
	public void remove(Event e){
		// If the event is a transition and is equal to the stored transition
		// remove the stored transition.
		if(e.isTransition() && e.equals(_transition)){
			_transition = null;
			return;
		}
		
		// If the event is a rate change and is equal to the stored rate change,
		// remove the stored rate.
		if(e.isRate() && e.equals(_rate)){
			_rate = null;
			return;
		}
		
		// If the event is an inequality and the EventSet contains inequalities,
		// attempt to remove the event.
		if(_inequalities != null && e.isInequality()){
			_inequalities.remove(e.getInequalityVariable());
		}
	}
	
	/**
	 * Determines the number of elements in the EventSet.
	 * @return
	 * 		The number of elements in the EventSet.
	 */
	public int size(){
		if(_transition != null || _rate != null){
			// If we are in the Transition mode or Rate mode, the size is 1.
			return 1;
		}
		// If we are in the Inequality mode, the size is the 
		// number of inequalities.
		return _inequalities.size();
	}
	
	/**
	 * Determines whether any elements are in the set.
	 * @return
	 * 		True if there is a least one element, false otherwise.
	 */
	public boolean isEmpty(){
		
		// If one of the member variables is not null (and contains elements),
		// the set is not empty.
		if(_transition != null || _rate != null ||
				(_inequalities != null && _inequalities.size() != 0)){
			return false;
		}
		return true;
	}
	
	/**
	 * Retrieve the transition that this EventSet represents.
	 * @return
	 * 		The transition that this EventSet represents or null if
	 * 		this EventSet does not represent a transition.
	 */
	public Transition getTransition(){
		return _transition;
	}
	
	/**
	 * Retrieve the rate change that this EventSet represents.
	 * @return
	 * 		The rate change that this EventSet represents or null if
	 * 		this EventSet does not represent a rate change.
	 */
	public LPNContinuousPair getRateChange(){
		return _rate;
	}
	
	/*
	 * (non-Javadoc)
	 * @see lpn.parser.Transition#toString()
	 */
	@Override
	public String toString(){
		String result = "";
		
		// Check what type of events are contained in this event set.
		if(_transition != null){
			// This is a set of a singleton transition.
			result += "Transition Event Set = [" + _transition.getLabel();
		}
		else if(_rate !=null){
			// This is a single rate change.
			result += "Rate change Event set = [" + _rate;
		}
		else if (_inequalities != null){
			result += "Inequality Event Set = [" + _inequalities;
		}
		else{
			result += "Event Set = [";
		}
		
		result += "]";
		
		return result;
	}
	
	@Override
	public String getLabel(){
		return toString();
	}
	

	/**
	 * Determines if the EventSet represents a transition that is a failure 
	 * transition.
	 * @return
	 * 		True if this EventSet is storing a failure transition; false otherwise.
	 */
	@Override
	public boolean isFail() {
		
		// If this eventSet represents a transition to fire, then check if the
		// Transition is a failure transition. Otherwise return false.
		
		return _transition != null && _transition.isFail();
	}
	
	/**
	 * This method disables (ha ha) the disablingError. It needs to be finished.
	 */
	@Override
	public Transition disablingError(final LinkedList<Transition> current_enabled_transitions,
			LinkedList<Transition> next_enabled_transitions) {
		return null;
	}
	
	/*
	 * -------------------------------------------------------------------------------------
	 *                                      Inner Class
	 * -------------------------------------------------------------------------------------
	 */
	
	/**
	 * This is the custom iterator for the EventSet class. It operates in one of two modes :
	 * the Transition mode or the Inequality mode depending on whether the EventSet that created
	 * it holds a Transition or a list of Inequalities. In the Transition mode the iterator
	 * will return the single Transition. In the Inequality mode, the Iterator will iterate
	 * through the Inequality variables. All elements are returned packaged as Event objects.
	 * @author Andrew N. Fisher
	 *
	 */
	private class EventSetIterator implements Iterator<Event>{

		/*
		 * Abstraction Function : The Iterator operates in one of three modes: the
		 * Transition mode, the Inequality mode, or the Rate mode. The mode is
		 * determined by which of _tran, _inequal, or _r is non-null.
		 * The modes are then Transition, Inequality, or Rate mode respectively.
		 */
		
		/*
		 * Representation Invariant : If the Iterator is created in a given mode,
		 * then it should stay in that mode. The mode is determined by which field
		 * is non-null. Exactly one of _tran, _inequal, or _r should be non-null.
		 * The _tran or _rate variable will be null in the after returning it.
		 */
		
		// Stores the single transition if the Iterator is in the Transition mode.
		Transition _tran;
		
		// Stores the ArrayList<Inequality> objects iterator if the Iterator is in the
		// Inequality mode.
		Iterator<InequalityVariable> _inequal;
		
		// Stores the single rate change if the Iterator is in the Rate mode.
		LPNContinuousPair _r;
		
		/**
		 * The constructor initializes an Iterator in one of three modes : the 
		 * Transition mode, the Inequality mode, or the Rate mode.
		 * This mode is set once the Iterator is created.
		 */
		public EventSetIterator(){
			
			// Check to see in the EventSet is in a consistent state.
			if((_transition == null && _inequalities == null && _rate == null)
					|| (_transition != null && _inequalities != null)
					|| (_transition != null && _rate != null)
					|| (_inequalities != null && _rate != null)){
				throw new IllegalStateException("The EventSet is not in a correct" +
						" mode.");
			}
			
			if(EventSet.this._inequalities != null){
				// The EventSet contains inequalities. So initialize the
				// EventSetIterator in Inequality mode.
				_inequal = EventSet.this._inequalities.iterator();
			}
			else if (EventSet.this._transition != null){
				// The EventSet contains a transition. So initialize the 
				// EventSetIterator in Transition mode.
				_tran = EventSet.this._transition;
			}
			else{
				// The EventSet is not in Inequality or Transition mode, so
				// it must be in Rate mode. So initialize the EventSetIterator
				// to Rate mode.
				_r = EventSet.this._rate;
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			
			// Determine the mode the EventSetIterator is in. 
			if(_inequal != null){
				//A non-null _inequal variable indicates it is in the
				// Inequality mode, so pass the action to the _inequal iterator.
				return _inequal.hasNext();
			}
			
			// The Iterator is in the Transition or Rate mode.
			// So determine if there is still a transition or rate to return.
			return _tran != null || _r != null;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Event next() {
			
			//Determine the mode the EventSetIterator is in.			
			if(_inequal != null){
				// The Iterator is in the Inequality mode, so pass the action
				// to the _ineqaulities iterator.
				return new Event(_inequal.next());
			}
			
			// The Iterator is in the Transition or rate mode. 
			if(_tran == null && _r == null){
				// The transition or rate has already been returned so complain.
				throw new NoSuchElementException("No more elements to return.");
			}
			
			// The Iterator is in the Transition or Rate mode and the
			// transition or rate has not be removed.
			// Remove the transition or rate and return it.
			
			if(_tran != null){
				Transition tmpTran = _tran;
			
				_tran = null;
			
				return new Event(tmpTran);
			}
			LPNContinuousPair tmpRate = _r;
			_r = null;
			return new Event(tmpRate);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			
			// Determine which mode is being operated in.
			if(_inequal == null){
				// We are in the Transition or Rate mode.
				// This is not supported, so complain.
				throw new UnsupportedOperationException("The remove method is not supported when for the EventSet" +
						" iterator when in the Transition mode.");
			}
			_inequal.remove();
		}
		
	}
	
	/**
	 * Tests for equality. Overrides inherited equals method.
	 * @return True if o is equal to this object, false otherwise.
	 */
	@Override
	public boolean equals(Object o)
	{	
		// Check if the reference is null.
		if(o == null)
		{
			return false;
		}
		
		// Check that the type is correct.
		if(!(o instanceof EventSet))
		{
			return false;
		}
		
		// Check for equality using the Zone equality.
		return equals((EventSet) o);
	}
	
	
	/**
	 * Tests for equality.
	 * @param
	 * 		The EventSet
	 * @return 
	 * 		True if the other EventSet is non-null and is equal.
	 */
	public boolean equals(EventSet otherEventSet)
	{
		// Check if the reference is null first.
		if(otherEventSet == null)
		{
			return false;
		}
		
		// Check for reference equality.
		if(this == otherEventSet)
		{
			return true;
		}
		
		// If the hash codes are different, then the objects are not equal. 
		if(this.hashCode() != otherEventSet.hashCode())
		{
			return false;
		}
		
		if(isTransition()){
			if(otherEventSet.isTransition()){
				// The other is also a transition, so compare the transition.
				return _transition.equals(otherEventSet._transition);
			}
			else{
				// The other is not a transition so they are not equal.
				return false;
			}
		}

		if(isRate()){
			if(otherEventSet.isRate()){
				// The other is also a rate rate transition, so compare.
				return _rate.equals(otherEventSet._rate);
			}
			else{
				// They are not equal.
				return false;
			}
		}
		
		if(isInequalities()){
			if(otherEventSet.isInequalities()){
				// The other is also a set of inequalities.
				
				if(_inequalities.size() != otherEventSet._inequalities.size()){
					return false;
				}
				
				boolean result = true;
				
				for(InequalityVariable ineq: _inequalities){
					result &= otherEventSet._inequalities.contains(ineq);
				}
				
				for(InequalityVariable ineq: otherEventSet._inequalities){
					result &= _inequalities.contains(ineq);
				}
				return result;
			}
			else{
				return false;
			}
		}
		
		System.err.print("Warning: EventSet equality reached end of equality.");
		return false;
	}
}
