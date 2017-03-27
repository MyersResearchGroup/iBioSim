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
package test.java.edu.utah.ece.async.gcm2sbml;


import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;
import test.java.edu.utah.ece.async.gcm2sbml.network.GeneticNetwork;
import test.java.edu.utah.ece.async.gcm2sbml.parser.GCMFile;
import test.java.edu.utah.ece.async.gcm2sbml.parser.GCMParser;
import test.java.edu.utah.ece.async.gcm2sbml.util.GlobalConstants;

import org.junit.Before;

public class GCMFileTest extends TestCase{

	@Before
	public void setUp() throws Exception {
		System.loadLibrary("sbmlj");
	}
	
	public void testLoadandSave() { 
//		GCMFile file = new GCMFile();		
//		file.load("gcm"+ Gui.separator +"bar.gcm");
//		file.setSBMLFile("foo.sbml");
//		file.save("gcm"+ Gui.separator +"baz.gcm");
	}
	
	public void testMerge() {
		GCMParser parser = new GCMParser("gcm"+ Gui.separator +"baz.gcm");
		GeneticNetwork network = parser.buildNetwork();				
		network.mergeSBML("gcm"+ Gui.separator +"baz.sbml");
	}
	
	public void testAddProperty() {
//		GCMFile file = new GCMFile();
//		file.load(filename);
//		Properties property = new Properties();
//		property.put("hi", "fun");
//		file.addInfluences("g -> a", property);
//		file.save("nand3.dot");
	}
	
	public void testParseInfluence() {
		GCMFile file = new GCMFile();
		String name = "input -> output, Promoter promo";
		assertEquals("Should get correct input", "input", file.getInput(name));
		assertEquals("Should get correct output", "output", file.getOutput(name));
		assertEquals("Should get correct promoter", "promo", file.getPromoter(name));
	}
	
	public void testProperties() {
		Properties prop = new Properties();
		prop.put(GlobalConstants.MAX_DIMER_STRING, GlobalConstants.MAX_DIMER_VALUE);
		assertTrue("Couldn't find value", prop.containsKey(GlobalConstants.MAX_DIMER_STRING));
	}
	
	String filename = "nand.dot";

}
