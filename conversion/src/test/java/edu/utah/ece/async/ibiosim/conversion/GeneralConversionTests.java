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
package edu.utah.ece.async.ibiosim.conversion;

import org.junit.Test;

/**
 * General test cases for all conversion supported by iBioSim
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class GeneralConversionTests extends ConversionAbstractTests {
	
	
	@Test
	public void test_validation(){
		/* test validation on a valid sbol file. */
		String fileName = "CRISPR_example"; 
		String inputfile = sbolDir + fileName + ".xml";
		System.out.println(resourceDir);
		//Options
		String uriPrefix = "http://www.async.ece.utah.edu";
		String selectedMod = "http://sbols.org/CRISPR_Example/CRPb_characterization_Circuit/1.0.0";
		
		String[] converter_cmdArgs = {"-no", "-p", uriPrefix, inputfile, "-s", selectedMod};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_e(){
		/* compare SBOL files with same content but with different file name*/
		String fileName = "CRISPR_example"; 
		String fileName2 = "meherGolden_RepressionModel";
		
		String inputfile = sbolDir + fileName + ".xml";
		String inputfile2 = sbolDir + fileName2 + ".xml";
		
		
		String[] converter_cmdArgs = {"-no", inputfile, "-e", inputfile2, "-mf", "mainfileRes" , "-cf", "secondFileRes"};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}

	
	@Test
	public void test_cmd_l_sbml(){
		/* convert sbol2sbml*/
		String fileName = "CRISPR_example"; 
		String inputfile = sbolDir + fileName + ".xml";
		
		String outDir = "/Users/tramyn/Desktop/tempOutFiles/";
		String outFile = fileName + "_out2.xml";
		
		//Options
		String uriPrefix = "http://www.async.ece.utah.edu";
		String outputLang = "SBML";
		
		
		String[] converter_cmdArgs = {"-l", outputLang, "-esf", "-p", uriPrefix, inputfile, "-oDir", outDir, "-o", outFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_genbank(){
		/* convert sbol2genbank*/
		String fileName = "acoA_full"; 
		String inputfile = sbolDir + fileName + ".xml";
		
		String outputDir = "/Users/tramyn/Desktop/tempOutFiles/";
		String outFile = fileName + "_out.xml";
		//Options
		String outputLang = "GenBank";
		
		String[] converter_cmdArgs = {"-l", outputLang, inputfile, "-oDir", outputDir, "-o", outFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbol(){
		/* convert sbml2sbol*/
		String fileName = "repressibleTU_Connected"; 
		String inputfile = sbmlDir + fileName + ".xml";
		String uriPrefix = "http://www.async.ece.utah.edu";
		
		String outputFile = fileName + "_out.xml";
		String outDir = "/Users/tramyn/Desktop/tempOutFiles/";
		
		//Options
		String outputLang = "SBOL2";
		
		String[] converter_cmdArgs = {"-l", outputLang, "-p", uriPrefix, inputfile, "-oDir", outDir, "-o", outputFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
}
