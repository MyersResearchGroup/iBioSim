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
package edu.utah.ece.async.conversion.scripts;

import java.util.ArrayList;

public class SpeciesThresholdTester extends AbstractTester {
	
	
	public SpeciesThresholdTester(String folder, String type, ArrayList<String> highSpecies,
			ArrayList<String> lowSpecies) {
		super(highSpecies, lowSpecies, null, null, 4000, 100, 6100);
		this.folder = folder;
		this.type = type;
		init();
	}
	
	private void init() {
		GCMScript script = new GCMScript();
		ArrayList<String> species = new ArrayList<String>();
		species.addAll(highSpecies);
		species.addAll(lowSpecies);
		double[][] thresholds = script.generateThreshold(folder, species, type, 3800);
		highThreshold = new double[highSpecies.size()];
		lowThreshold = new double[lowSpecies.size()];
		
		for (int i = 0; i < highSpecies.size(); i++) {
			highThreshold[i] = thresholds[0][i];
		}
		for (int i = 0; i < lowSpecies.size(); i++) {
			lowThreshold[i] = thresholds[1][i+highSpecies.size()];
		}
		System.out.print("");
	}
	
	private String folder = null;
	private String type = null;
}
