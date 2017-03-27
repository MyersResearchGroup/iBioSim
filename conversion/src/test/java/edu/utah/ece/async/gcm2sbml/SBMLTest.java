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


//import gcm2sbml.util.Utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;

import biomodelsim.BioSim;

public class SBMLTest extends TestCase {
	
	public void testAddDuplicate() {
		try {
			System.loadLibrary("sbmlj");
			String filename = "foo.sbml";
			
			SBMLDocument document = new SBMLDocument(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
			String compartment = "default";
			Model m = document.createModel();
			document.setModel(m);
			document.getModel().addCompartment(new Compartment(compartment));
			document.getModel().getCompartment("default").setSize(1);
			
			m.addSpecies(Utility.makeSpecies("A", compartment, 0));
			m.addSpecies(Utility.makeSpecies("A", compartment, 2));

			PrintStream p = new PrintStream(new FileOutputStream(filename));

			m.setName("foo");
			m.setId("foo");
			
			SBMLWriter writer = new SBMLWriter(); 
			p.print(writer.writeToString(document));

			p.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
