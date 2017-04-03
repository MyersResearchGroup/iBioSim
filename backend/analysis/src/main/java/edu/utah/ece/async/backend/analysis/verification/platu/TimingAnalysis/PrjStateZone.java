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
package edu.utah.ece.async.backend.analysis.verification.platu.TimingAnalysis;

import java.util.Arrays;

import edu.utah.ece.async.dataModels.verification.platu.TimingAnalysis.Zone1;
import edu.utah.ece.async.dataModels.verification.platu.stategraph.*;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class PrjStateZone {
	State[] stateArray;
	Zone1 zone;
	
	public PrjStateZone(final State[] other, Zone1 zone) {
    	this.stateArray = other;
        this.zone = zone;
    }

    
    @Override
    public boolean equals(final Object other) {
    	PrjStateZone otherSt = (PrjStateZone)other;
    	for(int i = 0; i < this.stateArray.length; i++)
    		if(this.stateArray[i] != otherSt.stateArray[i])
    			return false;
        return this.zone==otherSt.zone;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.stateArray) ^ zone.hashCode();
    }

     /**
     * @return the zone
     */
    public Zone1 getZone() {
        return zone;
    }

    public State[]	getStateArray()	{
    	return stateArray;
    }

    @Override
    public String toString() {
        return Arrays.toString(stateArray)+zone.hashCode();
    }

}
