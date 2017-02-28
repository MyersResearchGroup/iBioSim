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
package backend.verification.platu.logicAnalysis;


import java.util.*;

import backend.verification.platu.common.PlatuObj;
import backend.verification.platu.stategraph.*;
import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Transition;

/**
 * State
 * @author Administrator
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class LpnState extends PlatuObj {
    
    private LPN lpn;
    private State state;
    private HashSet<Transition> enabledSet;
    int index;
    
    

    public LpnState() {
        this.lpn = null;
        this.state = null;
        this.enabledSet = null;
        this.index = -1;
    }

    public LpnState(final LPN lhpnFile, final State newState, final HashSet<Transition> lpnEnabledSet) {
    	this.lpn = lhpnFile;
    	this.state = newState;
    	this.enabledSet = lpnEnabledSet;
    }
    
    public void setLpn(final LPN thisLpn) {
    	this.lpn = thisLpn;
    }
    
    public LPN getLpn() {
    	return this.lpn;
    }
    
    public void setState(State newState) {
    	this.state = newState;
    }
    
    @Override
	public void setLabel(String lbl) {
    	
    }
    
    @Override
	public String getLabel() {
    	return null;
    }
    
    @Override
	public void setIndex(int i) {
    	this.index = i;
    }
    
    @Override
	public int getIndex() {
    	return this.index;
    }
    
    @Override
    public LpnState clone() {
    	LpnState copy = new LpnState();
    	copy.lpn = this.lpn;
    	copy.state = this.state;
    	copy.enabledSet = this.enabledSet;
        return copy;
    }

	@Override
	public int hashCode() {
		final int prime = 13;
		int result = Integer.rotateLeft(this.state.hashCode(), prime) ^ Integer.rotateLeft(this.enabledSet.hashCode(), prime);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		
		LpnState other = (LpnState) obj;
		if(this.lpn != other.lpn)
			return false;
	
		if(this.state != other.state)
			return false;
		
		if(this.enabledSet.equals(other.enabledSet)==false)
			return false;
		
		return true;
	}


    public State getState() {
        return this.state;
    }

    public HashSet<Transition> getEnabled() {
    	return this.enabledSet;
    }
}

