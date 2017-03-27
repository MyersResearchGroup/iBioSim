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


import biomodelsim.BioSim;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Properties;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.libsbml;

import junit.framework.TestCase;
import test.java.edu.utah.ece.async.gcm2sbml.network.GeneticNetwork;
import test.java.edu.utah.ece.async.gcm2sbml.parser.GCMFile;
import test.java.edu.utah.ece.async.gcm2sbml.parser.GCMParser;

/**
 * This file tests the GCMParser
 * 
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class GCMParserTest extends TestCase {
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
	}
	
	public void testKinetic() {
		System.loadLibrary("sbmlj");
		System.out.println(libsbml.parseFormula("k^2"));
	}

	public void testLoad() {
		try {
			System.out.println(System.getenv("CLASSPATH"));
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[]) field.get(null);
			for (int i = 0; i < paths.length; i++) {
				System.out.println(paths[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.loadLibrary("sbmlj");
	}

	// Tests that parser can be constructed
	public void testConstructor() {
		GCMParser parser = new GCMParser("nand.dot", true);
		GeneticNetwork network = parser.buildNetwork();
		network.loadProperties(new GCMFile());
		SBMLDocument doc = network.outputSBML("nand.sbml");
//		if (doc.checkConsistency() > 0) {
//			for (int i = 0; i < doc.getNumErrors(); i++) {
//				System.out.println(doc.getError(i).getMessage());
//			}			
//		}
	}
	
	// Tests that parser can be constructed
	public void testGCM() {
		GCMFile file = new GCMFile();
		file.load("nand.dot");
		file.save("nand3.dot");
		
		GCMParser parser = new GCMParser("nand3.dot", true);
		GeneticNetwork network = parser.buildNetwork();
		network.loadProperties(new GCMFile());
		SBMLDocument doc = network.outputSBML("nand3.sbml");
//		if (doc.checkConsistency() > 0) {
//			for (int i = 0; i < doc.getNumErrors(); i++) {
//				System.out.println(doc.getError(i).getMessage());
//			}			
//		}
	}	
	
	public void testRead() {
		SBMLDocument doc = BioSim.readSBML("nand.sbml");
		System.out.println(doc.getModel().getName());
		System.out.println(doc.getModel().getId());
		
	}

	public void testCreateProperties() {
		Properties properties = new Properties();
		properties.put("promoters", "2");
		properties.put("RNAP", "30");
		properties.put("deg", ".0075");
		properties.put("kdimer", ".05");
		try {
			properties.store(new FileOutputStream("celem.param"),
					"My Properties");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private GCMParser parser = null;
}