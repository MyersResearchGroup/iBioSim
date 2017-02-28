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
package backend.verification.platu.project;

import java.util.Arrays;

import backend.verification.platu.TimingAnalysis.Zone1;
import backend.verification.platu.stategraph.State;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class prjStateTimed extends PrjState {

    private Object zone;

    public prjStateTimed(final State[] other, Object zone) {
    	this.stateArray = other;
        this.zone = zone;
    }

    public prjStateTimed(final State[] other) {
        stateArray = other;
    }
    
    @Override
    public boolean equals(final Object other) {
        return super.equals(other) && zone.equals(((prjStateTimed) other).zone);
    }

    @Override
    public int hashCode() {
        return super.hashCode()*31+zone.hashCode();
    }

     /**
     * @return the zone
     */
    public Zone1 getZone() {
        return (Zone1) zone;
    }

    /**
     * @param zone the zone to set
     */
    public void setZone(Object zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return Arrays.toString(stateArray)+zone.hashCode();
    }

}
