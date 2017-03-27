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
package main.java.edu.utah.ece.async.verification.platu.markovianAnalysis;

import java.util.HashMap;

import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Transition;
import main.java.edu.utah.ece.async.verification.platu.main.Options;
import main.java.edu.utah.ece.async.verification.platu.platuLpn.DualHashMap;
import main.java.edu.utah.ece.async.verification.platu.stategraph.State;
import main.java.edu.utah.ece.async.verification.platu.stategraph.StateGraph;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ProbLocalState extends State{
	
	public ProbLocalState(LPN lpn, int[] marking, int[] vector,
			boolean[] tranVector) {
		super(lpn, marking, vector, tranVector);		
	}

    /* (non-Javadoc)
     * @see verification.platu.stategraph.State#update(verification.platu.stategraph.StateGraph, java.util.HashMap, verification.platu.lpn.DualHashMap)
     * Return a new state if the newVector leads to a new state from this state; otherwise return null. Also, adjust the tranRateVector.
     */
    @Override
	public State update(StateGraph thisSg, HashMap<String, Integer> newVector, DualHashMap<String, Integer> VarIndexMap) {
    	int[] newVariableVector = new int[this.vector.length];   	
    	boolean nextStateExists = false;
    	for(int index = 0; index < vector.length; index++) {
    		String var = VarIndexMap.getKey(index);
    		int this_val = this.vector[index];    		
			Integer newVal = newVector.get(var);
    		if(newVal != null) {
    			if(this_val != newVal) {
    				nextStateExists = true;
    				newVariableVector[index] = newVal;
    			}
    			else
    				newVariableVector[index] = this.vector[index]; 
    		}
    		else
    			newVariableVector[index] = this.vector[index];    		
    	}
    	if(nextStateExists) {
    		HashMap<Transition, Double> tranRateMapForNextState = new HashMap<Transition, Double>();
    		boolean[] newTranVector= ((ProbLocalStateGraph)thisSg).updateTranVector(this, this.marking, newVariableVector, null, tranRateMapForNextState); 
    		State nextState = thisSg.addState(new ProbLocalState(this.lpn, this.marking, newVariableVector, newTranVector));
    		((ProbLocalStateGraph) thisSg).addTranRate(nextState, tranRateMapForNextState);
    		if (Options.getDebugMode()) {
    			String location = "ProbLocalState.java, update()";    			
    			((ProbLocalStateGraph) thisSg).printNextProbLocalTranRateMapForGivenState(nextState, location);
    		}
        	return nextState;
    	}
    	return null;
    }
    
}


