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
package test.conversion;

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
	/*
	 * Note for all converter test cases that call src/conversion/Converter.java, flags in the command line can have empty strings for the flag value.
	 * 
	 */
	
	@Test
	public void test_validation(){
		/* test validation on a valid sbol file. */
		String fileName = "CRISPR_example"; 
		String inputfile = sbolDir + fileName + ".xml";
		String outputFile = ""; 
		String compareFile = "";
		
		//Options
		String uriPrefix = "http://www.async.ece.utah.edu";
		
		String[] converter_cmdArgs = {"-no", "-p", uriPrefix, inputfile, "-o", outputFile, "-e", compareFile};
		conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_e(){
		/* compare SBOL files with same content but with different file name through required compareFile flag*/
		String fileName = "CRISPR_example"; 
		String fileName2 = "meherGolden_RepressionModel";
		
		String inputfile = sbolDir + fileName + ".xml";
		String inputfile2 = sbolDir + fileName2 + ".xml";
		
		String outputFile = ""; 
		String compareFile = inputfile2;
		
		String[] converter_cmdArgs = {"-no", inputfile, "-o", outputFile, "-e", compareFile};
		conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_cf(){
		/* compare SBOL files with same content but with different file name through -cf flag*/
		String fileName = "CRISPR_example"; 
		String fileName2 = "meherGolden_RepressionModel";
		
		String inputfile = sbolDir + fileName + ".xml";
		String inputfile2 = sbolDir + fileName2 + ".xml";
		
		String outputFile = ""; 
		String compareFile = "";
		
		String[] converter_cmdArgs = {"-no", "-cf", inputfile2, inputfile, "-o", outputFile, "-e", compareFile};
		conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_mf(){
		/* compare SBOL files with same content but with different file name through required mf and cf flag*/
		String fileName = sbolDir + "CRISPR_example" + ".xml"; 
		String fileName2 = "meherGolden_RepressionModel";
		
		String mainfile = "";
		String inputfile2 = sbolDir + fileName2 + ".xml";
		
		String outputFile = ""; 
		String compareFile = "";
		//TODO: validation fail because fileName is empty 
		String[] converter_cmdArgs = {"-no", "-mf", mainfile, "-cf", inputfile2, fileName, "-o", outputFile, "-e", compareFile};
		conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbml(){
		/* convert sbol2sbml*/
		String fileName = "CRISPR_example"; 
		String inputfile = sbolDir + fileName + ".xml";
		
		String outputFile = sbol2sbml_outputDir + fileName + ".xml";
		String compareFile = "";
		
		//Options
		String uriPrefix = "http://sbols.org/CRISPR_Example/CRPb_characterization_Circuit/1.0.0";
		String outputLang = "SBML";
		
		String[] converter_cmdArgs = {"-l", outputLang, "-esf", "-p", uriPrefix, inputfile, "-o", outputFile, "-e", compareFile};
		conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_genbank(){
		/* convert sbol2genbank*/
		String fileName = "CRISPR_example"; 
		String inputfile = sbolDir + fileName + ".xml";
		
		String outputFile = sbol2GenBank_outputDir + fileName + "_output" + ".xml";
		String compareFile = "";
		
		//Options
		String outputLang = "GenBank";
		
		String[] converter_cmdArgs = {"-l", outputLang, inputfile, "-o", outputFile, "-e", compareFile};
		conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbol(){
		/* convert sbml2sbol*/
		String fileName = "repressibleTU_Connected"; 
		String inputfile = sbmlDir + fileName + ".xml";
		
		String outputFile = sbml2sbol_outputDir + fileName + ".xml";
		String compareFile = "";
		
		//Options
		String outputLang = "SBOL2";
		
		String[] converter_cmdArgs = {"-l", outputLang, inputfile, "-o", outputFile, "-e", compareFile};
		conversion.Converter.main(converter_cmdArgs);
	}
}
