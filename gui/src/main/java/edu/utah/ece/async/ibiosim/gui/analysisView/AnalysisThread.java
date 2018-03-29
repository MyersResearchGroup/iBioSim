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
package edu.utah.ece.async.ibiosim.gui.analysisView;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class AnalysisThread extends Thread {

	private AnalysisView analysisView;

	private boolean refresh;
	
	private String direct = ".";

	public AnalysisThread(AnalysisView analysisView, String direct) {
		super(analysisView);
		this.analysisView = analysisView;
		this.direct = direct;
	}

	public void start(boolean refresh) {
		this.refresh = refresh;
		super.start();
	}

	@Override
	public void run() {
		analysisView.run(refresh,direct);
	}
}
