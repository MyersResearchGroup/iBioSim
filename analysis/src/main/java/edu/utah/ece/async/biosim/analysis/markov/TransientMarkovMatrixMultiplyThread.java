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
package edu.utah.ece.async.biosim.analysis.markov;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class TransientMarkovMatrixMultiplyThread extends Thread {

	private StateGraph sg;

	private double Gamma, timeLimit;

	private int startIndex, endIndex, K;

	public TransientMarkovMatrixMultiplyThread(StateGraph sg) {
		super(sg);
		this.sg = sg;
	}

	public void start(int startIndex, int endIndex, double Gamma, double timeLimit, int K) {
		this.timeLimit = timeLimit;
		this.Gamma = Gamma;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.K = K;
		super.start();
	}

	@Override
	public void run() {
		sg.transientMarkovMatrixMultiplication(startIndex, endIndex, Gamma, timeLimit, K);
	}
}
