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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import edu.utah.ece.async.verification.platu.main.Options;
import edu.utah.ece.async.verification.platu.project.PrjState;

/**
 * A StateSet object operates like a hash set of PrjState objects. It operates in one
 * of two modes. The first mode (called the set mode) is exactly like a HashSet<PrjState>.
 * The second mode is backed by a HashMap<PrjState, LinkedList<TimedPrjState>>. This
 * second mode (called the subset/superset mode) allows TimedPrjStates to be keyed by 
 * their un-timed portions for quicker look-up when determining subsets and supersets.
 * The subset/superset mode is only enacted when the Options.getTimingAnalysisFlag() is
 * true and at least one of the ZoneType.getSubsetFlag() or ZoneType.getSupersetFlag() is
 * true.
 * 
 * Note: this object acts differently
 * depending on whether 
 *		Options.getTimingAnalysisFlag() 
 *						&& (ZoneType.getSubsetFlag() || ZoneType.getSupersetFlag())
 * is true or not. If this value is false, then the StateSet acts like HashSet<PrjState>.
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class StateSet implements Iterable<PrjState>{

	/*
	 * Abstraction Function: The StateSet object follows one of two modes for storing
	 * 		elements called the set mode and the subset/superset mode. In the set mode
	 * 		elements are simply stored in a HashSet<PrjState> _singletonList. In this
	 * 		mode the StateSet simply a wrapper of the HashSet<PrjState> and passes
	 * 		the operations to the _singletonList.
	 * 		
	 * 		In the subset/superset mode, elements are stored in _setList. The idea of is
	 * 		to be able to take the un-timed portion of a TimedPrjState (which can be thought
	 * 		of as a PrjState) use it as a key and have return all TimedPrjStates in the
	 * 		StateSet that share the same un-timed portion. Thus the key value pair
	 * 		(k, v) of the HashMap should be such that v is a list containing only 
	 * 		TimedPrjStates that have the same un-timed portion k.
	 */
	
	/*
	 * Representation Invariant: Only one of _singletonList or _setList should
	 * 		be non-null at a time and should correspond to whether the StateSet is in the
	 * 		subset/superset mode or not. Furthermore, if the StateSet is operating in the
	 * 		subset/superset mode, then it is the _setList that should be non-null.
	 * 		The mode is set in the constructor and should not change as long as the object
	 * 		is in existence.
	 * 		
	 * 		In the set mode, operations should be simply passed to the _singletonList
	 * 		thereby ensuring that the StateSet acts identical to a HashSet in this mode.
	 * 
	 * 		Given a key value pair (k, v) stored in the _setList, the LinkedList v should
	 * 		only contain TimedPrjStates that have an un-timed portion equal to k. Specifically,
	 * 		s.getUntimedPrjState.equals(k) should return true for each TimedState in v.
	 */
	
	protected HashSet<PrjState> _singletonList;
	protected HashMap<PrjState, LinkedList<TimedPrjState>> _setList;
	
	// Caches whether subsets, supersets, and timing is being used.
	private boolean timed, subsets, supersets;
	
	private PrjState _initState;
	
	/*
	 * Get the initial state.
	 */
	public PrjState get_initState() {
		return _initState;
	}

    /* 
     * Set the initial state.
     * @param initState
     * 		The initial state.
     */
	public void set_initState(PrjState initState) {
		this._initState = initState;
	}

	/**
	 * Creates a state set. The StateSet will be initialized into the into the subset/superset
	 * mode if the expression
	 * 		Options.getImingAnalysisFlag() && (ZoneType.getSubsetFlag() || 
	 * 												ZoneType.getSupersetFlag())
	 * is true. Otherwise the StateSet will be initialized into the set mode. Once the
	 * StateSet is initialized in the set mode or the subset/superset mode, it cannot be
	 * changed.
	 */
	public StateSet(){
		
		// Store that status when StateSet is initialized to avoid unexpected behavior.
		timed = Options.getTimingAnalysisFlag();
		subsets = Zone.getSubsetFlag();
		supersets = Zone.getSupersetFlag();
		
		if(timed && (subsets || supersets)){
			_setList = new HashMap<PrjState, LinkedList<TimedPrjState>>();
		}
		else{
			_singletonList = new HashSet<PrjState>();
		}
	}
	
//	public StateSet(boolean map){
//		if(map){
//			_setList = new HashMap<PrjState, LinkedList<TimedPrjState>>();
//		}
//		else{
//			_singletonList = new HashSet<PrjState>();
//		}
//	}
	
	/**
	 * Determines whether the any PrjStates are in the StateSet.
	 * @return
	 * 		True if any PrjStates are in the StateSet, false otherwise.
	 */
	public boolean isEmpty(){
		if(_singletonList != null){
			return _singletonList.isEmpty();
		}
		return _setList.isEmpty();
	}
	
	/**
	 * Determines how many PrjStates are in the StateSet.
	 * @return
	 * 		A non-negative integer that gives the number of PrjStates are in the StateSet.
	 */
	public int size(){
		if(_singletonList != null){
			return _singletonList.size();
		}
		int result = 0;
		
		for(LinkedList<TimedPrjState> l : _setList.values()){
			result += l.size();
		}
		
		return result;
	}
	
	/**
	 * Adds a PrjState to the StateSet.
	 * @param s
	 * 		The PrjState to add to the StateSet.
	 * @return
	 * 		True if the StateSet changes by adding the element s.
	 */
	public boolean add(PrjState s){
		
		if(_singletonList != null){
			return _singletonList.add(s);
		}
		
		if(_setList != null){
			
			if(!(s instanceof TimedPrjState)){
				throw new IllegalArgumentException("Subset/superset mode set, but an un-timed" +
						"state is being added.");
			}
			
			TimedPrjState ts = (TimedPrjState) s;
			
			PrjState untimedState = ts.getUntimedPrjState();
			
			LinkedList<TimedPrjState> list = _setList.get(untimedState);
			
			if(list == null){
				
				// No list is associated with this set of un-timed (local) states.
				// So create a new list with this PrjState.
				LinkedList<TimedPrjState> newList = new LinkedList<TimedPrjState>();
				newList.add(ts);
				_setList.put(untimedState, newList);
				
				// The list changed, so return true;
				return true;
			}
			
			if(list.contains(ts)){
				// The set already contains the timed project state. So nothing changes.
				return false;
			}
			// The set does not already contain the timed project state. So add it.
			list.add(ts);
			
			
			return true;
		}
		
		throw new IllegalStateException("Add was used and StateSet was not initialized.");
	}
	
//	public boolean add(PrjState s){
//		if(s instanceof TimedPrjState){
//			TimedPrjState ts = (TimedPrjState) s;
//			return add(ts);
//		}
//		else if(_singletonList == null){
//			throw new IllegalArgumentException("StateSet initialized in subset/superset mode," +
//					" but only an un-timed state is being added.");
//		}
//		
//		return _singletonList.add(s);
//	}
//	
//	
//	public boolean add(TimedPrjState s){
//		if(_setList == null){
//			throw new IllegalArgumentException("StateSet initialized as un-timed, " +
//					" but a timed project state is attempted to be added.");
//		}
//		
//		PrjState untimedState = s.getUntimedPrjState();
//		
//		LinkedList<TimedPrjState> list = _setList.get(untimedState);
//		
//		if(list == null){
//			
//			// No list is associated with this set of un-timed (local) states.
//			// So create a new list with this PrjState.
//			LinkedList<TimedPrjState> newList = new LinkedList<TimedPrjState>();
//			newList.add(s);
//			_setList.put(untimedState, newList);
//			
//			// The list changed, so return true;
//			return true;
//		}
//		
//		if(list.contains(s)){
//			// The set already contains the timed project state. So nothing changes.
//			return false;
//		}
//		else{
//			// The set does not already contain the timed project state. So add it.
//			list.add(s);
//		}
//		
//		
//		return true;
//	}
	
	/**
	 * Determines whether the StateSet contains the PrjState or not.
	 * @param s
	 * 		The PrjState to determine if the PrjState contains it.
	 * @return
	 * 		True if s is in the PrjState, false otherwise.
	 */
	public boolean contains(PrjState s){
		if(_singletonList != null){
			return _singletonList.contains(s);
		}
		
		if(_setList != null){
			// If _setList != null, then StateSet has been initialized into subset/superset mode.
			// Thus there are three possibilities : subsets has been selected, supersets has been
			// selected, or both subsets and supersets have been selected.
			
			
			if(!(s instanceof TimedPrjState)){
				throw new IllegalArgumentException("Subset/superset mode set, but an un-timed" +
						"state is being added.");
			}
			
			TimedPrjState ts = (TimedPrjState) s;
			
			// Get the un-timed portion for the cache.
			PrjState untimedState = ts.getUntimedPrjState();

			// Get the list keyed to this set of (local) un-timed states.
			LinkedList<TimedPrjState> list = _setList.get(untimedState);

			if(list == null){

				// No list is associated with this set of un-timed (local) states.
				// So the timed state cannot be in the set.

				return false;
			}

			// Get an iterator from the list to allow removal of elements as the list is 
			// traversed.
			Iterator<TimedPrjState> iterate = list.iterator();

			boolean result = false;

			while(iterate.hasNext()){
				TimedPrjState listState = iterate.next();

				// If subsets are selected, then iteration can be exited as soon as a subset is found.
				if(subsets && ts.subset(listState)){
					return true;
				}

				// If supersets are selected, items that are subsets of the new state may be
				// removed.
				if(supersets){
					if(!subsets && ts.equals(listState)){
						// When an equal state is found, the return value must be true and the
						// state should not be removed. When not doing subsets, the superset check
						// cannot end here (since other sets that are supersets may exist further in the
						// list). If control has passed here, it can be deduced that subsets has not
						// been selected even without the subset flag since the 
						// if(subsets && s.subset(listState) would have already been taken.
						// The subset flag is added here to allow a quick out of the extra equality check.
						result |= true;
					}
					else if(ts.superset(listState)){
						// The new state (s) is a strict superset of an existing state.
						// Remove the existing state.
						iterate.remove();
					}
				}

			}

			return result;
		}
		
		throw new IllegalStateException("Contains was called and StateSet was not initialized.");
	}
	
	
//	public boolean contains(PrjState s){
//		if(s instanceof TimedPrjState){
//			TimedPrjState ts = (TimedPrjState) s;
//			return contains(ts);
//		}
//		else if(_singletonList == null){
//			throw new IllegalArgumentException("StateSet initialized as timed," +
//					" but only an untimed state is being added.");
//		}
//		
//		return _singletonList.contains(s);
//	}
//	
//	public boolean contains(TimedPrjState s){
//		if(_setList == null){
//			throw new IllegalArgumentException("StateSet initialized as untimed, " +
//					" but a timed project state is attempted to be added.");
//		}
//		
//		// If _setList == null, then StateSet has been initialized into subset/superset mode.
//		// Thus there are three possibilities : subsets has been selected, supersets has been
//		// selected, or both subsets and supersets have been selected.
//		
//		// Get the un-timed portion for the cache.
//		PrjState untimedState = s.getUntimedPrjState();
//		
//		// Get the list keyed to this set of (local) untimed states.
//		LinkedList<TimedPrjState> list = _setList.get(untimedState);
//		
//		if(list == null){
//			
//			// No list is associated with this set of un-timed (local) states.
//			// So the timed state cannot be in the set.
//			
//			return false;
//		}
//		
//		//return list.contains(s);
//		
//		// Get an iterator from the list to allow removal of elements as the list is 
//		// traversed.
//		Iterator<TimedPrjState> iterate = list.iterator();
//		 
//		boolean result = false;
//		
//		while(iterate.hasNext()){
//			TimedPrjState listState = iterate.next();
//			
//			// If subsets are selected, then iteration can be exited as soon as a subset is found.
//			if(subsets && s.subset(listState)){
//				return true;
//			}
//			
//			// If supersets are selected, items that are subsets of the new state may be
//			// removed.
//			if(supersets){
//				if(!subsets && s.equals(listState)){
//					// When an equal state is found, the return value must be true and the
//					// state should not be removed. When not doing subsets, the superset check
//					// cannot end here (since other sets that are supersets may exist further in the
//					// list). If control has passed here, it can be deduced that subsets has not
//					// been selected even without the subset flag since the 
//					// if(subsets && s.subset(listState) would have already been taken.
//					// The subset flag is added here to allow a quick out of the extra equality check.
//					result |= true;
//				}
//				else if(s.superset(listState)){
//					// The new state (s) is a strict superset of an existing state.
//					// Remove the existing state.
//					iterate.remove();
//				}
//			}
//			
//		}
//		
//		return result;
//	}
	
	/**
	 * Converts the StateSet into a HashSet<PrjState>.
	 * @return
	 * 		A HashSet<PrjState> containing the same PrjStates as the StateSet.
	 */
	public HashSet<PrjState> toHashSet(){
		if(_singletonList != null){
			return _singletonList;
		}
		HashSet<PrjState> result = new HashSet<PrjState>();
		//throw new IllegalStateException("Array initialized in subset/superset mode.");
		for(LinkedList<TimedPrjState> list : _setList.values()){
			result.addAll(list);
		}
		return result;
	}
	
	public String stateString(){
		
		String result = "";
		
		if(_singletonList != null){
			result += "# of prjStates found: " + size();
		}
		
		if(_setList != null){
			// Report the total number of project states found.
			result += "# of timedPrjStates found: " + size();
			
			// Report the number of un-timed states.
			result += ", # of untimed states found: " + _setList.size();
			
			// Report the largest Zone used.
			result += ", Largest zone: " + Zone.ZoneSize;
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<PrjState> iterator(){
		Iterator<PrjState> hashIterator = null;
		Iterator<LinkedList<TimedPrjState>> listIterator = null;
		
		if(_singletonList != null){
			hashIterator = _singletonList.iterator();
		}
		
		if(_setList != null){
			listIterator = _setList.values().iterator();
		}
		
		
		return new StateSetIterator(hashIterator, listIterator);
	}
	
	/**
	 * This is the particular version of the iterator that the StateSet uses.
	 * @author Andrew N. Fisher
	 *
	 */
	private class StateSetIterator implements Iterator<PrjState>{
		
		/*
		 * Abstraction Function: 
		 * 		A StateSetIterator is the implementation for the Iterator<PrjState>
		 * 		required by StateSet being iterable. The StateSet has two modes,
		 * 		one where a HashSet<PrjState> is used and one where a
		 * 		HashMap<PrjState, LinkedList<TimedPrjState>> is used. Correspondingly,
		 * 		This iterator has two modes. One that is meant iterate through the 
		 * 		HashSet and the other that is meant to iterate through the HashMap.
		 * 		The member variable _hashIterator is simply the HashSet's own iterator.
		 * 		The member variables _hashListIterator and _listIterator together
		 * 		iterate through all the TimedPrjStates that are stored in the LinkedLists
		 * 		of a HashMap<PrjState, <LinkedList<TimedPrjState>>. The _hashListIterator
		 * 		iterator goes through each of the LinkedList and the _listIterator goes
		 * 		through a single list. Thus the idea is to get the first LinkedList,
		 * 		traverse its elements, get the second LinkedList, traverse its elements
		 * 		and so on until all elements of the LinkedLists have been traversed.
		 */
		
		
		/*
		 * Representation Invariant : Only one of _hashIterator or _hashListIterator
		 * 		should be non-null at one time. The iterator should be iterating through
		 * 		one mode at a time, either the HashSet mode or the LinkedList mode.
		 * 
		 * 		If _hashListIterator is not null, then _listIterator should either 
		 * 		be the iterator for the last LinkedList returned by _hashListIterator
		 * 		or an iterator that has not exhausted all its elements. The idea is
		 * 		the _listIterator should be able to give the next element that is to
		 * 		be return if there are still elements that can be returned.
		 */
		
		Iterator<PrjState> _hashIterator;
		Iterator<LinkedList<TimedPrjState>> _hashListIterator;
		Iterator<TimedPrjState> _listIterator;
		
		/**
		 * Initializes the iterator. Only one parameter should be non-null when this
		 * object is created matching one of the two modes of the StateSet object,
		 * it throws an IllegalStateExceptoin otherwise.
		 * @param hashIterator
		 * 			Iterator for a HashSet<PrjState.
		 * @param listIterator
		 * 			Iterator for a HashMap<PrjState, LinkedList<TimedPrjState>>.
		 */
		private StateSetIterator(Iterator<PrjState> hashIterator,
				Iterator<LinkedList<TimedPrjState>> listIterator){
			
			// This method initializes the iterator for the StateSet. It initializes
			// the iterator member variables. The member variables that are initialized
			// should match the mode that the StateSet is in. This method may be made
			// to check the member variables directly, but currently determines things
			// by the variables passed. The HashIterator should be an iterator for the
			// __singletonList member variable of the StateSet and the listIterator
			// should be an iterator for the _setList member variable of the StateSet.
			// In keeping with only one mode being initialized, only one of the member 
			// variables can be non-null.
			if(hashIterator != null && listIterator !=null){
				throw new IllegalStateException("Only one iterator should be non-null.");
			}
			
			_hashIterator = hashIterator;
			_hashListIterator = listIterator;
			
			
			if(_hashListIterator != null && _hashListIterator.hasNext()){
				_listIterator = _hashListIterator.next().iterator();
				
				// Find the first list with an element or end with the last list.
				while( !_listIterator.hasNext() && _hashListIterator.hasNext()){
					_listIterator = _hashListIterator.next().iterator();
				}
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			
			if(_hashIterator !=null){
				return _hashIterator.hasNext();
			}
			
			if(_listIterator == null){
				return false;
			}
			
			return _listIterator.hasNext();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public PrjState next() {
			
			if(_hashIterator != null){
				return _hashIterator.next();
			}
			
			PrjState nextState = _listIterator.next();
			
			// If this list is exhausted, find the next list with elements or 
			// get the last list.
			while( !_listIterator.hasNext() && _hashListIterator.hasNext()){
				_listIterator = _hashListIterator.next().iterator();
			}
			
			return nextState;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			if(_hashIterator != null){
				_hashIterator.remove();
				return;
			}
			
			// TODO: This has the following flaw currently. Suppose list1 and lists2 are two
			// nonempty consecutive lists. Once next is called on the last element in list1,
			// list2 becomes the new _listIterator. If removed is called, it will be called on list2
			// instead of list1 causing an error.
			if(_hashListIterator != null){
				_listIterator.remove();
			}
			
			throw new UnsupportedOperationException("The iterator was not initialized.");
		}
	}
}
