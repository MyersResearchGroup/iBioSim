package backend.verification.timed_state_exploration.zone;

import java.util.Arrays;

import backend.verification.platu.platuLpn.LpnTranList;
import backend.verification.platu.project.PrjState;
import backend.verification.platu.stategraph.State;

public class TimedPrjState extends PrjState{
	protected ZoneType[] _zones;
	
	public TimedPrjState(final State[] other, final ZoneType[] otherZones){
		super(other);
		this._zones = otherZones;
	}
	
	public TimedPrjState(State[] initStateArray) {
		super(initStateArray);
		_zones = new Zone[initStateArray.length];
		for(int i=0; i<_zones.length; i++){
			_zones[i] = new Zone(initStateArray[i]);
		}
	}


	public ZoneType[] toZoneArray(){
		return _zones;
	}
	
	@Override
	public boolean equals(Object other){
		if(!super.equals(other)){
			return false;
		}
		
		if(!(other instanceof TimedPrjState)){
			return false;
		}
		
		TimedPrjState otherTimedPrjState = (TimedPrjState) other;
		
		if(this._zones == otherTimedPrjState._zones){
			return true;
		}
		
		if(!Arrays.equals(this._zones, otherTimedPrjState._zones)){
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString(){
		String result = super.toString();
		
		result += "\nZones: \n";
		
		for(ZoneType z : _zones){
			result += z.toString() + "\n";
		}
		
		return result;
	}
	
	public LpnTranList getEnabled(int zoneNumber){
		
		// ZoneType to consider
		ZoneType z = _zones[zoneNumber];
		
		return new LpnTranList(z.getEnabledTransitions());
	}
	
	/**
	 * Gets a project state containing the un-timed portion of this timed project state.
	 * @return
	 * 		A project state that has the same set of (local) un-timed states.
	 */
	public PrjState getUntimedPrjState(){
		return new PrjState(toStateArray());
	}
	
	/**
	 * Checks if all the zones are subsets (or equal to) the corresponding other zones as well
	 * as if the un-timed portions are equal. Note: it is assumed that both states have the same
	 * number of zones.
	 * @param other
	 * 		The state to compare against.
	 * @return
	 * 		True if zone zi is a subset of zone z'i for all i where zi is the i-th zone
	 * 		of this TimedPrjState and z'i is the i-th zone of the other TimedPrjState and
	 * 		the un-timed portion of the states are equal.
	 */
	public boolean subset(TimedPrjState other){
		// For clarity, extract the un-timed portions. Alternately, super.equals(otherState) would
		// probably work.
		
		PrjState thisUntimed = this.getUntimedPrjState();
		PrjState otherUntimed = other.getUntimedPrjState();
		
		if(!(thisUntimed.equals(otherUntimed))){
			return false;
		}
		
		return subsetZone(other);
	}
	
	/**
	 * Checks if all the zones are supersets (or equal to) the corresponding other zones as well
	 * as if the un-timed portions are equal. Note : it is assumed that both states have the same
	 * number of zones.
	 * @param other
	 * 		The state to compare against.
	 * @return
	 * 		True if zone zi is a superset of zone z'i for all i where zi is the i-th zone
	 * 		of this TimedPrjState and z'i is the i-th zone of the other TimedPrjState and
	 * 		the un-timed portion of the states are equal.
	 */
	public boolean superset(TimedPrjState other){
		// For clarity, extract the un-timed portions. Alternately, super.equals(otherState) would
		// probably work.

		PrjState thisUntimed = this.getUntimedPrjState();
		PrjState otherUntimed = other.getUntimedPrjState();

		if(!(thisUntimed.equals(otherUntimed))){
			return false;
		}

		return supersetZone(other);
	}
	
	/**
	 * Checks if all the zones are subsets (or equal to) the corresponding other zones.
	 * Note: The un-timed portion is not considered. It is assumed that each state has
	 * the same number of zones.
	 * @param other
	 * 		The state to compare against.
	 * @return
	 * 		True if zone zi is a subset of zone z'i for all i where zi is the i-th zone
	 * 		of this TimedPrjState and z'i is the i-th zone of the other TimedPrjState.
	 * 		False otherwise.
	 */
	public boolean subsetZone(TimedPrjState other){
		boolean result = true;
		
		for(int i=0; i<this._zones.length; i++){
			result &= _zones[i].subset(other._zones[i]);
		}
		
		return result;
	}
	
	/**
	 * Checks if all the zones are supersets (or equal to) the corresponding other zones.
	 * Note: The un-timed portion is not considered. It is assumed that both states have the
	 * same number of zones.
	 * @param other
	 * 		The state to compare against.
	 * @return
	 * 		True if zone zi is a superset of zone z'i for all i where zi is the i-th zone
	 * 		of this TimedPrjState and z'i is the i-th zone of the other TimedPrjState.
	 * 		False otherwise.
	 */
	public boolean supersetZone(TimedPrjState other){
		boolean result = true;
		
		for(int i=0; i<this._zones.length; i++){
			result &= _zones[i].superset(other._zones[i]);
		}
		
		return result;
	}
}
