package platu.TimingAnalysis;

import java.util.*;

import platu.lpn.LPN;
import platu.stategraph.State;

public class TimingState {
	
	State state;
	Zone1 zone;
	int index;

	public TimingState() {
		state = null;
		zone = null;
		index = -1;
	}
	
	public TimingState(State newState, Zone1 newZone) {
		state = newState;
		this.zone = newZone;
		index = -1;
	}

	public TimingState(TimingState other) {
		this.state = other.state;
		this.zone = other.zone;
		this.index = other.index;
	}

	public void setIndex(int idx) {
		this.index = idx;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	
	@Override
	public String toString() {
		return state.toString() + "\n" + zone.toString() + "\n";
	}

	@Override
	public boolean equals(Object other) {
		TimingState otherState = (TimingState)other;

		if(this.state != otherState.state)
			return false;
				
		if((this.zone==null && otherState.zone!=null) || (this.zone!=null && otherState.zone==null))
			return false;
		
		if(this.zone != null && otherState.zone != null && this.zone.equals(otherState.zone)==false)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		if(this.zone == null)
			return state.hashCode();
		
		return state.hashCode() ^ this.zone.hashCode();
	}
}
