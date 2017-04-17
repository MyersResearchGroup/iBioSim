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
package edu.utah.ece.async.ibiosim.conversion.gcm2sbml;


import java.util.HashMap;

import junit.framework.TestCase;
import test.java.edu.utah.ece.async.gcm2sbml.util.Utility;

import org.junit.After;
import org.junit.Before;

public class UtilityTest extends TestCase{

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	public void testCalcAverage() {
		HashMap<String, double[]> values = Utility.calculateAverage("/home/shang/namphuon/workspace/BioSim/gcm");
		System.out.println();
	}
	
	public void testReadFile() {
		HashMap<String, double[]> values = Utility.readFile("/home/shang/namphuon/workspace/BioSim/gcm/run-1.tsd");
		System.out.println();		
	}
}
