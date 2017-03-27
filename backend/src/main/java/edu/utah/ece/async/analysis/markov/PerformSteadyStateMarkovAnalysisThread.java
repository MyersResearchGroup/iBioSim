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
package main.java.edu.utah.ece.async.analysis.markov;

import java.util.ArrayList;

import javax.swing.JProgressBar;

import dataModels.util.exceptions.BioSimException;
import main.java.edu.utah.ece.async.analysis.markov.StateGraph.Property;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class PerformSteadyStateMarkovAnalysisThread extends Thread {

	private StateGraph sg;

	private JProgressBar progress;
	
	private double tolerance;

	private ArrayList<Property> conditions;

	public PerformSteadyStateMarkovAnalysisThread(StateGraph sg, JProgressBar progress) {
		super(sg);
		//TODO: is this critical for the code?
		//Thread.setDefaultUncaughtExceptionHandler(new Utility.UncaughtExceptionHandler());
		this.sg = sg;
		this.progress = progress;
	}

	public void start(double tolerance, ArrayList<Property> conditions) {
		this.tolerance = tolerance;
		this.conditions = conditions;
		super.start();
	}

	@Override
	public void run() {
		try {
      sg.performSteadyStateMarkovianAnalysis(tolerance, conditions, null, progress);
    } catch (BioSimException e) {
      e.printStackTrace();
    }
	}
}
