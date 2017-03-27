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
package edu.utah.ece.async.gcm2sbml;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;
import test.java.edu.utah.ece.async.gcm2sbml.scripts.GCMScript;
import test.java.edu.utah.ece.async.gcm2sbml.scripts.SpeciesThresholdTester;

import org.junit.After;
import org.junit.Before;

public class ScriptTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public void ThresholdCoop() {
		System.out.println("\nCoop:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + Gui.separator + "coop", "C",
				3500, "coop", 5);
	}

	public void ThresholdRatio() {
		System.out.println("\nRatio:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + Gui.separator + "ratio", "C",
				3500, "ratio", 5);
	}

	public void testThresholdPromoter() {
		System.out.println("\nPromoter:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + Gui.separator + "promoter", "C",
				3500, "promoter", 5);
	}

	public void ThresholdDecay() {
		System.out.println("\nDecay:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + Gui.separator + "decay", "C",
				3500, "decay", 5);
	}

	public void ThresholdRep() {
		System.out.println("\nRep:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + Gui.separator + "rep", "C",
				3500, "rep", 6);
	}

	private static final String directory = "/home/shang/namphuon/muller";
}
