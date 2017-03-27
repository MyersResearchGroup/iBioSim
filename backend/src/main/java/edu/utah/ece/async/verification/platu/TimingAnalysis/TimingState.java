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
package main.java.edu.utah.ece.async.verification.platu.TimingAnalysis;

import main.java.edu.utah.ece.async.verification.platu.stategraph.State;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
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
