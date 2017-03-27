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
package edu.utah.ece.async.scripts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import dataModels.biomodel.util.ExperimentResult;
import dataModels.biomodel.util.Utility;
import frontend.main.Gui;
import junit.framework.TestCase;

public class TimingScript extends TestCase {

	@Override
	protected void setUp() throws Exception {
		keySpeciesList = new ArrayList<String>();
		keySpeciesList.add(keySpecies);
		//script = new GCMScript();
	}

	public void testTiming() {
		try {
			generateTiming("promoter", 5);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on promoter");
		}
		try {
			generateTiming("coop", 5);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on coop");
		}
		try {
			generateTiming("rep", 7);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on rep");
		}
		try {
			generateTiming("decay", 8);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on decay");
		}
		try {
			generateTiming("ratio", 6);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on ratio");
		}
	}

	public double[] findTime(ExperimentResult results) {
		// First find the midpoint between high and low
		double low = results.getValue(keySpecies, lowTime);
		double high = results.getValue(keySpecies, highTime);
		double mid = (high - low) / 2.;

		// Now calculate switching time for low to high, high to low
		double timeToHigh = results.getTime(keySpecies, mid, switchHighTime);		
		double timeToLow = results.getTime(keySpecies, mid, switchLowTime);

		return new double[] { timeToHigh, timeToLow };
	}

	public void generateTiming(String files, int cases) {
		double[][] timingResults = new double[6][cases];

		for (int i = 1; i <= cases; i++) {
			String currDir = Utility.directory + Gui.separator + files + i;
			for (int j = 0; j < 3; j++) {
				HashMap<String, double[]> results = Utility
						.calculateAverage(currDir + Gui.separator
								+ dataGate[j]);
				ExperimentResult expResults = new ExperimentResult(results);
				double[] times = findTime(expResults);
				timingResults[j*2][i-1] = times[0] - switchHighTime;
				if (timingResults[j*2][i-1] < 0) {
					timingResults[j*2][i-1] = 0;
				}
				timingResults[j*2+1][i-1] = times[1] - switchLowTime;
				if (timingResults[j*2+1][i-1] < 0) {
					timingResults[j*2+1][i-1] = 0;
				}
			}
			System.out.println("Done " + i);
		}
		printResults(Utility.directory + Gui.separator + files + ".dat", timingResults);
	}

	private static void printResults(String file, double[][] results) {
		try {
			// Create file
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("maj-high\tmaj-low\ttog-high\ttog-low\tsi-high\tsi-low\n");
			for (int i = 0; i < results[0].length; i++) {
				for (int j = 0; j < results.length; j++) {
					out.write(results[j][i] + "\t");
				}
				out.write("\n");
			}
			out.flush();
			// Close the output stream
			fstream.flush();
			out.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//private GCMScript script = null;

	//private String[] gate = { "maj", "tog", "si" };
	private String[] dataGate = { "majority", "toggle", "si" };
	ArrayList<String> keySpeciesList = null;
	String keySpecies = "C";

	// Switching time
	private final static double lowTime = 2400;
	private final static double highTime = 7400;

	private final static double switchHighTime = 5000;
	private final static double switchLowTime = 10000;
}
