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
package edu.utah.ece.async.verification.platu.logicAnalysis;

import java.util.HashSet;

import edu.utah.ece.async.verification.platu.project.PrjState;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HashSetWrapper extends HashSet<PrjState> implements StateSetInterface {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean contains(PrjState state) {
		return super.contains(state);
	}

	@Override
	public boolean add(PrjState state) {
		return super.add(state);
	}
	
	/**
	 * This method takes a PrjState instance otherPrjState, iterates through this hash set of PrjState instances, 
	 * and grabs the one from this set that "equals" to otherPrjState. If no match is found, this method returns null.
	 * @param otherPrjState
	 * @return
	 */
	public PrjState get(PrjState otherPrjState) {		
		for (PrjState prjSt : this) {
			if (this.contains(otherPrjState))
				return prjSt;
		}
		return null;
	}
}
