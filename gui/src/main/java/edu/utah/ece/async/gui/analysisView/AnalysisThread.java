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
package edu.utah.ece.async.gui.analysisView;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class AnalysisThread extends Thread {

	private AnalysisView analysisView;

	private String directory;

	private boolean refresh;

	public AnalysisThread(AnalysisView analysisView) {
		super(analysisView);
		this.analysisView = analysisView;
	}

	public void start(String directory, boolean refresh) {
		this.directory = directory;
		this.refresh = refresh;
		super.start();
	}

	@Override
	public void run() {
		analysisView.run(directory, refresh);
	}
}
