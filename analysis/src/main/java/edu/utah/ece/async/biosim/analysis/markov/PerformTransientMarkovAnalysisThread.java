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

import javax.swing.JProgressBar;

import edu.utah.ece.async.biosim.dataModels.util.exceptions.BioSimException;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class PerformTransientMarkovAnalysisThread extends Thread {

	private StateGraph sg;

	private JProgressBar progress;

	private double timeLimit, timeStep, printInterval, error;

	private String[] condition;

	private boolean globallyTrue;

	public PerformTransientMarkovAnalysisThread(StateGraph sg, JProgressBar progress) {
		super(sg);
		this.sg = sg;
		this.progress = progress;
	}

	public void start(double timeLimit, double timeStep, double printInterval, double error, String[] condition, boolean globallyTrue) {
		this.timeLimit = timeLimit;
		this.timeStep = timeStep;
		this.printInterval = printInterval;
		this.error = error;
		this.condition = condition;
		this.globallyTrue = globallyTrue;
		progress.setIndeterminate(false);
		progress.setString(null);
		super.start();
	}

	@Override
	public void run() {
		try {
      sg.performTransientMarkovianAnalysis(timeLimit, timeStep, printInterval, error, condition, progress, globallyTrue);
    } catch (BioSimException e) {
      e.printStackTrace();
    }
	}
}
