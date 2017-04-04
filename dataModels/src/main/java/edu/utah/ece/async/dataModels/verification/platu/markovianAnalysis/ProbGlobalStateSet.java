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
package edu.utah.ece.async.dataModels.verification.platu.markovianAnalysis;

import java.util.HashMap;
import java.util.Iterator;

import edu.utah.ece.async.dataModels.verification.platu.logicAnalysis.StateSetInterface;
import edu.utah.ece.async.dataModels.verification.platu.project.PrjState;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ProbGlobalStateSet extends HashMap<PrjState, PrjState> implements StateSetInterface, Runnable {//extends HashSet<PrjState>{

	private static final long serialVersionUID = 1L;

	private PrjState initState;

	public ProbGlobalStateSet() {
		super();
	}

	/*
	 * Get the initial state.
	 */
	public PrjState getInitialState() {
		return initState;
	}

	/* 
     * Set the initial state.
     * @param initState
     * 		The initial state.
     */
	public void setInitState(PrjState initPrjState) {
		this.initState = initPrjState;	
	}

	@Override
	public boolean contains(PrjState s) {
		return this.keySet().contains(s);
	}

	@Override
	public boolean add(PrjState state) {
		super.put(state, state);
		return false;
	}

	@Override
	public Iterator<PrjState> iterator() {
		return super.keySet().iterator();
	}

	@Override
	public void run() {	
	}
	
	
}
