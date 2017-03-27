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
/**
 * 
 */
package test.java.edu.utah.ece.async.gcm2sbml;


import java.io.File;

import junit.framework.TestCase;
import test.java.edu.utah.ece.async.gcm2sbml.network.GeneticNetwork;
import test.java.edu.utah.ece.async.gcm2sbml.parser.GCMFile;
import test.java.edu.utah.ece.async.gcm2sbml.parser.GCMParser;
import test.java.edu.utah.ece.async.gcm2sbml.util.GlobalConstants;
import test.java.edu.utah.ece.async.gcm2sbml.util.Utility;

import org.junit.Before;

/**
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class GCMScriptTest extends TestCase{

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}
	
	/**
	 * Tests the effects of varying promoter count
	 */
	public void testCreateDecay() {
		System.loadLibrary("sbmlj");
		GCMFile gcm = null;
		GeneticNetwork network = null;
		GCMParser parser = null;
		for (int i = 1; i <= kdecay.length; i++) {
			int k = 0;
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + toggle);
			gcm.setParameter(GlobalConstants.KDECAY_STRING, ""+kdecay[i-1]);
			gcm.save(saveDirectory + fileSerparator + decay + i + fileSerparator + "toggle.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + decay + i + fileSerparator + "toggle.gcm");			
			network = parser.buildNetwork();			
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + decay + i + fileSerparator + "toggle.sbml");
			network.outputSBML(saveDirectory + fileSerparator + decay + i + fileSerparator +"toggle"+ fileSerparator  + "toggle.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + decay + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "toggle.sbml");
			}
			k++;

			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + si);
			gcm.setParameter(GlobalConstants.KDECAY_STRING, ""+kdecay[i-1]);
			gcm.save(saveDirectory + fileSerparator + decay + i + fileSerparator + "si.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + decay + i + fileSerparator + "si.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + decay + i + fileSerparator + "si.sbml");
			network.outputSBML(saveDirectory + fileSerparator + decay + i + fileSerparator + "si"+ fileSerparator  + "si.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + decay + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "si.sbml");
			}
			k++;
			
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + majority);
			gcm.setParameter(GlobalConstants.KDECAY_STRING, ""+kdecay[i-1]);
			gcm.save(saveDirectory + fileSerparator + decay + i + fileSerparator + "majority.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + decay + i + fileSerparator + "majority.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + decay + i + fileSerparator + "majority.sbml");
			network.outputSBML(saveDirectory + fileSerparator + decay + i + fileSerparator + "majority"+fileSerparator + "majority.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + decay + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "majority.sbml");
			}			
		}
	}
		
	
	/**
	 * Tests the effects of varying promoter count
	 */
	public void testCreatePromoter() {
		System.loadLibrary("sbmlj");
		GCMFile gcm = null;
		GeneticNetwork network = null;
		GCMParser parser = null;
		for (int i = 1; i <= numPromoters; i++) {
			gcm = new GCMFile();
			int k = 0;
			gcm.load(directory + fileSerparator + toggle);
			gcm.setParameter(GlobalConstants.PROMOTER_COUNT_STRING, ""+i);
			gcm.save(saveDirectory + fileSerparator + promoter + i + fileSerparator + "toggle.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + promoter + i + fileSerparator + "toggle.gcm");			
			network = parser.buildNetwork();			
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + "toggle.sbml");
			network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + "toggle"+ fileSerparator + "toggle.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "toggle.sbml");
			}
			k++;
			
						
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + si);
			gcm.setParameter(GlobalConstants.PROMOTER_COUNT_STRING, ""+i);
			gcm.save(saveDirectory + fileSerparator + promoter + i + fileSerparator + "si.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + promoter + i + fileSerparator + "si.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + "si.sbml");
			network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + "si"+ fileSerparator + "si.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "si.sbml");
			}
			k++;

			
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + majority);
			gcm.setParameter(GlobalConstants.PROMOTER_COUNT_STRING, ""+i);
			gcm.save(saveDirectory + fileSerparator + promoter + i + fileSerparator + "majority.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + promoter + i + fileSerparator + "majority.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + "majority.sbml");
			network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + "majority"+ fileSerparator + "majority.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "majority.sbml");
			}
			k++;
		}
	}
	
	/**
	 * Tests the effects of varying promoter count
	 */
	public void testCreateCoop() {
		System.loadLibrary("sbmlj");
		GCMFile gcm = null;
		GeneticNetwork network = null;
		GCMParser parser = null;
		for (int i = 1; i <= kcoop.length; i++) {
			int k = 0;
			gcm = new GCMFile();			
			gcm.load(directory + fileSerparator + toggle);
			gcm.setParameter(GlobalConstants.COOPERATIVITY_STRING, ""+kcoop[i-1]);
			gcm.save(saveDirectory + fileSerparator + coop + i + fileSerparator + "toggle.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + coop + i + fileSerparator + "toggle.gcm");			
			network = parser.buildNetwork();			
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + coop + i + fileSerparator + "toggle.sbml");
			network.outputSBML(saveDirectory + fileSerparator + coop + i + fileSerparator + "toggle"+ fileSerparator + "toggle.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + coop + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "toggle.sbml");
			}
			k++;
						
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + si);
			gcm.setParameter(GlobalConstants.COOPERATIVITY_STRING, ""+kcoop[i-1]);
			gcm.save(saveDirectory + fileSerparator + coop + i + fileSerparator + "si.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + coop + i + fileSerparator + "si.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + coop + i + fileSerparator + "si.sbml");
			network.outputSBML(saveDirectory + fileSerparator + coop + i + fileSerparator + "si"+ fileSerparator + "si.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + coop + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "si.sbml");
			}
			k++;

			
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + majority);
			gcm.setParameter(GlobalConstants.COOPERATIVITY_STRING, ""+kcoop[i-1]);
			gcm.save(saveDirectory + fileSerparator + coop + i + fileSerparator + "majority.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + coop + i + fileSerparator + "majority.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + coop + i + fileSerparator + "majority.sbml");
			network.outputSBML(saveDirectory + fileSerparator + coop + i + fileSerparator + "majority"+ fileSerparator + "majority.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + coop + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "majority.sbml");
			}
			k++;
		}
	}
	
	/**
	 * Tests the effects of varying promoter count
	 */
	public void testRatio() {
		System.loadLibrary("sbmlj");
		GCMFile gcm = null;
		GeneticNetwork network = null;
		GCMParser parser = null;
		for (int i = 1; i <= kratio.length; i++) {
			double newDecay = kratio[i-1]*Double.parseDouble(GlobalConstants.KDECAY_VALUE);
			double newKoc = kratio[i-1]*Double.parseDouble(GlobalConstants.OCR_VALUE);
			int k = 0;
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + toggle);
			gcm.setParameter(GlobalConstants.KDECAY_STRING, ""+newDecay);
			gcm.setParameter(GlobalConstants.OCR_STRING, ""+newKoc);
			gcm.save(saveDirectory + fileSerparator + ratio + i + fileSerparator + "toggle.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + ratio + i + fileSerparator + "toggle.gcm");			
			network = parser.buildNetwork();			
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + ratio + i + fileSerparator + "toggle.sbml");
			network.outputSBML(saveDirectory + fileSerparator + ratio + i + fileSerparator +"toggle"+ fileSerparator  + "toggle.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + ratio + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "toggle.sbml");
			}
			k++;

			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + si);
			gcm.setParameter(GlobalConstants.KDECAY_STRING, ""+newDecay);
			gcm.setParameter(GlobalConstants.OCR_STRING, ""+newKoc);
			gcm.save(saveDirectory + fileSerparator + ratio + i + fileSerparator + "si.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + ratio + i + fileSerparator + "si.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + ratio + i + fileSerparator + "si.sbml");
			network.outputSBML(saveDirectory + fileSerparator + ratio + i + fileSerparator + "si"+ fileSerparator  + "si.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + ratio + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "si.sbml");
			}
			k++;
			
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + majority);
			gcm.setParameter(GlobalConstants.KDECAY_STRING, ""+newDecay);
			gcm.setParameter(GlobalConstants.OCR_STRING, ""+newKoc);
			gcm.save(saveDirectory + fileSerparator + ratio + i + fileSerparator + "majority.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + ratio + i + fileSerparator + "majority.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + ratio + i + fileSerparator + "majority.sbml");
			network.outputSBML(saveDirectory + fileSerparator + ratio + i + fileSerparator + "majority"+fileSerparator + "majority.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + ratio + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "majority.sbml");
			}			
		}
	}
	
	
	/**
	 * Tests the effects of varying promoter count
	 */
	public void testCreateRep() {
		System.loadLibrary("sbmlj");
		GCMFile gcm = null;
		GeneticNetwork network = null;
		GCMParser parser = null;
		for (int i = 1; i <= krep.length; i++) {
			int k = 0;
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + toggle);
			gcm.setParameter(GlobalConstants.KREP_STRING, ""+krep[i-1]);
			gcm.save(saveDirectory + fileSerparator + rep + i + fileSerparator + "toggle.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + rep + i + fileSerparator + "toggle.gcm");			
			network = parser.buildNetwork();			
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator + "toggle.sbml");
			network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator +"toggle"+ fileSerparator  + "toggle.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "toggle.sbml");
			}
			k++;

			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + si);
			gcm.setParameter(GlobalConstants.KREP_STRING, ""+krep[i-1]);
			gcm.save(saveDirectory + fileSerparator + rep + i + fileSerparator + "si.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + rep + i + fileSerparator + "si.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator + "si.sbml");
			network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator + "si"+ fileSerparator  + "si.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "si.sbml");
			}
			k++;
			
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + majority);
			gcm.setParameter(GlobalConstants.KREP_STRING, ""+krep[i-1]);
			gcm.save(saveDirectory + fileSerparator + rep + i + fileSerparator + "majority.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + rep + i + fileSerparator + "majority.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator + "majority.sbml");
			network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator + "majority"+fileSerparator + "majority.sbml");
			for (int j = 0; j < 4; j++) {
				network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator + type[k] + experiment[j]+ fileSerparator  + "majority.sbml");
			}			
		}
	}
	
	
	private double[] kratio = new double[] {.25/2, .25, .5, 1, 2, 4};
	private double[] kcoop = new double[] {1, 2, 3, 4, 5, 6};
	private double[] krep = new double[] {.1, .15, .2, .4, .5, .8,  1};
	private double[] kdecay = new double[] {.0075/2., .0075/1.5, .0075/1.25, .0075, .0075*1.125, .0075*1.2, .0075*1.5, .0075*2};
	private int numPromoters = 5;
	private String directory = "gcm";
	private char fileSerparator = File.separatorChar;
	private String saveDirectory = Utility.directory;
	private String toggle = "toggle.gcm";
	private String si = "si.gcm";
	private String majority = "majority.gcm";
	private String promoter = "promoter";	
	private String rep = "rep";
	private String coop = "coop";
	private String decay = "decay";
	private String ratio = "ratio";
	private String[] type = {"tog", "si", "maj"};
	private String[] experiment = {"-h-high", "-h-low", "-l-high", "-l-low"};
	
}