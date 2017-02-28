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
package backend.verification.platu.markovianAnalysis;
import javax.swing.JProgressBar;

import dataModels.util.exceptions.BioSimException;


/**
 * This class is used to construct threads for transient or nested Markovian analysis. 
 * @author Zhen Zhang (adapted from Curtis Madsen's PerformTransientMarkovAnalysisThread class)
 *
 */

public class PerfromTransientMarkovAnalysisThread extends Thread {

	//private ProbGlobalStateSet globalStateSet;
	private MarkovianAnalysis markovianAnalysis;

	private JProgressBar progress;

	private double timeLimit, timeStep, printInterval, error;

	private String[] condition;

	private boolean globallyTrue;

	public PerfromTransientMarkovAnalysisThread(MarkovianAnalysis markovianAnalysis, JProgressBar progress) {
		super(markovianAnalysis);
		this.markovianAnalysis = markovianAnalysis;		
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
      markovianAnalysis.performTransientMarkovianAnalysis(timeLimit, timeStep, printInterval, error, condition, progress, globallyTrue);
    } catch (BioSimException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
	}
}
