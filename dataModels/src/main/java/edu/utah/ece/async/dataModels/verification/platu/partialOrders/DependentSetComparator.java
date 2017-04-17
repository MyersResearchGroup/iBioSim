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
package edu.utah.ece.async.dataModels.verification.platu.partialOrders;

import java.util.Comparator;
import java.util.HashMap;

import edu.utah.ece.async.dataModels.lpn.Transition;
import edu.utah.ece.async.dataModels.verification.platu.main.Options;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class DependentSetComparator implements Comparator<DependentSet>{
	private HashMap<Transition, Integer> tranFiringFreqMap;
	int highestLpnIndex;
	
	public DependentSetComparator(HashMap<Transition, Integer> tranFiringFreq, int maxLpnIndex) {
		this.tranFiringFreqMap = tranFiringFreq;
		this.highestLpnIndex = maxLpnIndex;
	}

	@Override
	public int compare(DependentSet dep0, DependentSet dep1) {		
		if (!dep0.isEnabledTranDummy() && dep1.isEnabledTranDummy()) {
			return -1;
		}
		else if ((dep0.isEnabledTranDummy() && !dep1.isEnabledTranDummy()) || (dep0.isEnabledTranDummy() && dep1.isEnabledTranDummy()))
			return 1;
		else { // Neither dep0 nor dep1 contains dummy transitions. 			
			if (!Options.getMarkovianModelFlag()) { // non-stochastic
//				// Compare sets with only immediate transitions and sets with only non-immediate ones. 
//				// Only need to compare seed transitions because a set with mixed immediate 
//				// and non-immediate transitions should not exist.
//				if (dep0.getSeed().getDelayTree() == null && dep1.getSeed().getDelayTree() != null) 
//					return -1;
//				 // TODO: Immediate transition should have 0 delay, not "null" delay.
//				else if (dep1.getSeed().getDelayTree() == null && dep0.getSeed().getDelayTree() != null)
//					return 1;
//				else {
					if (dep0.getDependent().size() < dep1.getDependent().size()) 
						return -1;
					else if (dep0.getDependent().size() > dep1.getDependent().size())
						return 1;
					else {					
						if (tranFiringFreqMap.get(dep0.getSeed()) < tranFiringFreqMap.get(dep1.getSeed()))
							return -1;
						else if (tranFiringFreqMap.get(dep0.getSeed()) > tranFiringFreqMap.get(dep1.getSeed()))
							return 1;
						else {
							if (dep0.getLowestLpnNumber(highestLpnIndex) < dep1.getLowestLpnNumber(highestLpnIndex)) 
								return -1;							
							else if (dep0.getLowestLpnNumber(highestLpnIndex) > dep1.getLowestLpnNumber(highestLpnIndex))
								return 1;
							else
								return 0;
						}
//						if (dep0.getLowestLpnNumber(highestLpnIndex) < dep1.getLowestLpnNumber(highestLpnIndex)) 	// 
//							return -1;							
//						else if (dep0.getLowestLpnNumber(highestLpnIndex) > dep1.getLowestLpnNumber(highestLpnIndex))
//							return 1;
//						else {
//							if (tranFiringFreqMap.get(dep0.getSeed()) < tranFiringFreqMap.get(dep1.getSeed()))
//								return -1;
//							else if (tranFiringFreqMap.get(dep1.getSeed()) < tranFiringFreqMap.get(dep0.getSeed()))
//								return 1;
//							else 
//								return 0;
//						}					
					}
//				}
			}
			// TODO: Add condition to compare the average transition rates.
			return 0;
		}	
	}
}
