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
package main.java.edu.utah.ece.async.verification.platu.partialOrders;

import java.util.HashSet;

import main.java.edu.utah.ece.async.lpn.parser.Transition;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class DependentSet {
	HashSet<Transition> dependent;
	Transition seed;
	boolean enabledTranIsDummy;
	
	public DependentSet(HashSet<Transition> dependent, Transition enabledTran2, boolean enabledTranIsDummy) {
		this.dependent = dependent;
		this.seed = enabledTran2;
		this.enabledTranIsDummy = enabledTranIsDummy;
	}

	public HashSet<Transition> getDependent() {
		return dependent;
	}

	public Transition getSeed() {
		return seed;
	}
	
	public boolean isEnabledTranDummy() {
		return enabledTranIsDummy;
	}

	/**
	 * For each transition in <code>dependent</code>, check its LPN index. 
	 * @return the lowest LPN index found in the <code>dependent</code> set.
	 */
	public int getLowestLpnNumber(int highestLpnIndex) {
		int lowestLpnIndex = highestLpnIndex;
		for (Transition t: dependent) {
			if (t.getLpn().getLpnIndex() < lowestLpnIndex)
				lowestLpnIndex = t.getLpn().getLpnIndex();
		}
		return lowestLpnIndex;
	}
}
