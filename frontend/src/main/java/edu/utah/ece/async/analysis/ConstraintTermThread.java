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
package main.java.edu.utah.ece.async.analysis;

import java.util.ArrayList;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ConstraintTermThread extends Thread {

	private AnalysisView analysisView;

	private ArrayList<AnalysisThread> threads;

	private ArrayList<String> dirs, levelOne;

	private String stem;

	public ConstraintTermThread(AnalysisView analysisView) {
		super(analysisView);
		this.analysisView = analysisView;
	}

	public void start(ArrayList<AnalysisThread> threads, ArrayList<String> dirs, ArrayList<String> levelOne, String stem) {
		this.threads = threads;
		this.dirs = dirs;
		this.levelOne = levelOne;
		this.stem = stem;
		super.start();
	}

	@Override
	public void run() {
		analysisView.run(threads, dirs, levelOne, stem);
	}
}
