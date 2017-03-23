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
package scripts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import dataModels.biomodel.util.Utility;
import frontend.main.Gui;
import junit.framework.TestCase;

public class ProbabilityScript extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
//		togHigh = new ArrayList<String>();
//		togHigh.add("Y");
//		togLow = new ArrayList<String>();
//		togLow.add("Z");
//
//		siHigh = new ArrayList<String>();
//		siHigh.add("P3");
//		siLow = new ArrayList<String>();
//		siLow.add("P2");
//
//		majHigh = new ArrayList<String>();
//		majHigh.add("C");
//		majLow = new ArrayList<String>();
//		majLow.add("E");
		
		togHigh = new ArrayList<String>();
		togHigh.add("C");
		togLow = new ArrayList<String>();

		siHigh = new ArrayList<String>();
		siHigh.add("C");
		siLow = new ArrayList<String>();

		majHigh = new ArrayList<String>();
		majHigh.add("C");
		majLow = new ArrayList<String>();
				
		specialHigh = new ArrayList<String>();
		specialHigh.add("D");
		specialLow = new ArrayList<String>();
		
		highSpecies = new ArrayList<ArrayList<String>>();
		highSpecies.add(majHigh);
		highSpecies.add(togHigh);
		highSpecies.add(siHigh);
		highSpecies.add(specialHigh);

		lowSpecies = new ArrayList<ArrayList<String>>();
		lowSpecies.add(majLow);
		lowSpecies.add(togLow);
		lowSpecies.add(siLow);
		lowSpecies.add(specialLow);
	}

	public void Promoter() {
		for (int i = 1; i <= 5; i++) {
			String currDir = Utility.directory + Gui.separator + "promoter" + i;
			for (int j = 0; j < 3; j++) {
				SpeciesThresholdTester highTester = new SpeciesThresholdTester(
						currDir, gate[j], highSpecies.get(j), lowSpecies.get(j));
				double[][] results = GCMScript.generateStatistics(currDir
						+ Gui.separator + gate[j] + experiment[1], highTester);
				printResults(currDir + Gui.separator + dataGate[j] + "-"
						+ dataInput[0] + "-" + dataChange[1] + ".dat", results);

				results = GCMScript.generateStatistics(currDir + Gui.separator
						+ gate[j] + experiment[3], highTester);
				printResults(currDir + Gui.separator + dataGate[j] + "-"
						+ dataInput[1] + "-" + dataChange[1] + ".dat", results);

				SpeciesThresholdTester lowTester = new SpeciesThresholdTester(
						currDir, gate[j], lowSpecies.get(j), highSpecies.get(j));
				results = GCMScript.generateStatistics(currDir + Gui.separator
						+ gate[j] + experiment[0], lowTester);
				printResults(currDir + Gui.separator + dataGate[j] + "-"
						+ dataInput[0] + "-" + dataChange[0] + ".dat", results);

				results = GCMScript.generateStatistics(currDir + Gui.separator
						+ gate[j] + experiment[2], highTester);
				printResults(currDir + Gui.separator + dataGate[j] + "-"
						+ dataInput[1] + "-" + dataChange[0] + ".dat", results);
			}
		}
	}

	public void testProbabilities() {
		try {
			generateProbabilities("promoter", 2);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on promoter");
		}
//		try {
//			generateProbabilities("coop", 5);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("Error on coop");
//		}
//		try {
//			generateProbabilities("rep", 6);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("Error on rep");
//		}		
//		try {
//			generateProbabilities("decay", 8);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("Error on decay");
//		}		
//		try {
//			generateProbabilities("ratio", 6);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("Error on ratio");
//		}
	}

	public void generateProbabilities(String files, int cases) {
		for (int i = 1; i <= cases; i++) {
			String currDir = Utility.directory + Gui.separator + files + i;
			for (int j = 0; j < 4; j++) {
				
				SpeciesThresholdTester highTester = new SpeciesThresholdTester(
						currDir, gate[j], highSpecies.get(j), lowSpecies.get(j));
				double[][] results = GCMScript.generateStatistics(currDir
						+ Gui.separator + gate[j] + experiment[1], highTester);
				printResults(currDir + Gui.separator + dataGate[j] + "-"
						+ dataInput[0] + "-" + dataChange[1] + ".dat", results);
				System.out.println("Finished " + 1);
				results = GCMScript.generateStatistics(currDir + Gui.separator
						+ gate[j] + experiment[3], highTester);
				printResults(currDir + Gui.separator + dataGate[j] + "-"
						+ dataInput[1] + "-" + dataChange[1] + ".dat", results);
				System.out.println("Finished " + 2);
				SpeciesThresholdTester lowTester = new SpeciesThresholdTester(
						currDir, gate[j], lowSpecies.get(j), highSpecies.get(j));
				results = GCMScript.generateStatistics(currDir + Gui.separator
						+ gate[j] + experiment[0], lowTester);
				printResults(currDir + Gui.separator + dataGate[j] + "-"
						+ dataInput[0] + "-" + dataChange[0] + ".dat", results);
				System.out.println("Finished " + 3);
				results = GCMScript.generateStatistics(currDir + Gui.separator
						+ gate[j] + experiment[2], lowTester);
				printResults(currDir + Gui.separator + dataGate[j] + "-"
						+ dataInput[1] + "-" + dataChange[0] + ".dat", results);
				System.out.println("Finished " + 4);
			}
		}
	}

	private static void printResults(String file, double[][] results) {
		try {
			// Create file
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);

			for (int i = 0; i < results[0].length; i++) {
				out.write(results[0][i] + " " + results[1][i] + " "
						+ results[2][i] + "\n");
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

	private ArrayList<String> togHigh = null;
	private ArrayList<String> togLow = null;

	private ArrayList<String> majHigh = null;
	private ArrayList<String> majLow = null;

	private ArrayList<String> siHigh = null;
	private ArrayList<String> siLow = null;
	
	private ArrayList<String> specialHigh = null;
	private ArrayList<String> specialLow = null;

	private ArrayList<ArrayList<String>> highSpecies = null;
	private ArrayList<ArrayList<String>> lowSpecies = null;

	private String[] gate = { "maj", "tog", "si" ,"tog"};
	private String[] experiment = { "-h-high", "-h-low", "-l-high", "-l-low" };
	private String[] dataGate = { "majority", "toggle", "si" , "special"};
	private String[] dataInput = { "heat", "light" };
	private String[] dataChange = { "higher", "lower" };

}
