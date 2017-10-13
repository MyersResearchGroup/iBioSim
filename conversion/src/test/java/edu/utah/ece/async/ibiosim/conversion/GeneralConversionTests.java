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

import java.io.IOException;
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
	
	private String uriPrefix = "http://www.async.ece.utah.edu/";
	
	@Test
	public void test_validation(){
		/* test validation on a valid sbol file. */
		String inputfile = sbolDir + "CRISPR_example.xml";
		
		//Options
		String selectedMod = "http://sbols.org/CRISPR_Example/CRPb_characterization_Circuit/1.0.0";
		
		String[] converter_cmdArgs = {"-no", "-p", uriPrefix, inputfile, "-s", selectedMod};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	
	@Test
	public void test_cmd_e(){
		/* compare SBOL files with same content but with different file name*/
		String inputfile = sbolDir + "CRISPR_example.xml";
		String inputfile2 = sbolDir + "meherGolden_RepressionModel.xml";
		
		String[] converter_cmdArgs = {"-no", "-p", uriPrefix, inputfile, "-e", inputfile2, "-mf", "mainfileRes" , "-cf", "secondFileRes"};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	

	
	@Test
	public void test_cmd_l_sbml() throws IOException{
		/* convert sbol2sbml with exporting to single SBML file*/
		String inputfile = sbolDir + "CRISPR_example.xml";
		String outFile =  sbmlDir + "CRISPR_example_out2";
		
		//Options
		String outputLang = "SBML";	
		
		String[] converter_cmdArgs = {"-l", outputLang, "-esf", "-p", uriPrefix, inputfile, "-oDir", sbmlDir, "-o", outFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbml2() throws IOException{
		/* convert sbol2sbml* with reference sbol files*/
		
		String inputfile = sbolDir + "CRISPR_example.xml";
		String outFile =  sbmlDir + "CRISPR_example_out3.xml";
		
		//Options
		String outputLang = "SBML";	
		
		String[] converter_cmdArgs = {"-l", outputLang, "-rsbol", sbolDir, "-p", uriPrefix, inputfile, "-o", outFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbml3() throws IOException{
		/* convert sbol2sbml and print to console*/
		String inputfile = sbolDir + "INV0_output.xml";
		
		//Options
		String outputLang = "SBML";	
		
		String[] converter_cmdArgs = {"-no", "-l", outputLang, "-p", uriPrefix, inputfile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_genbank(){
		/* convert sbol2genbank*/
		String inputfile = sbolDir + "acoA_full.xml";
		String outFile = genBankDir + "acoA_full_out.gb";
		
		//Options
		String outputLang = "GenBank";
		
		String[] converter_cmdArgs = {"-l", outputLang, inputfile, "-o", outFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_genbank2(){
		/* convert genbank2sbol*/
		String inputfile = genBankDir + "sequence1.gb";
		String outFile = sbolDir + "sequence1_out.xml";
		
		//Options
		String outputLang = "SBOL2";
		
		String[] converter_cmdArgs = {"-l", outputLang, inputfile, "-o", outFile, "-p", uriPrefix};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbol()
	{
		/* convert sbml2sbol*/
		String inputfile = sbmlDir + "INV0.xml";
		
		//Options
		String outputLang = "SBOL2";
		String outputFile = sbolDir +"INV0_output";
		
		String[] converter_cmdArgs = {"-l", outputLang, "-p", uriPrefix, inputfile, "-o", outputFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbol2()
	{
		/* convert sbml2sbol and print result to console*/
		String inputfile = sbmlDir + "INV0.xml";
		
		//Options
		String outputLang = "SBOL2";
		
		String[] converter_cmdArgs = {"-no", "-l", outputLang, "-p", uriPrefix, inputfile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbol3()
	{
		/* convert sbml2sbol with external SBML files using -rsbml*/
		String inputfile = sbmlDir + "CRPb_characterization_Circuit.xml";
		
		//Options
		String outputLang = "SBOL2";
		String outputFile = sbolDir + "CRPb_characterization_Circuit_output.xml";
		
		String[] converter_cmdArgs = {"-l", outputLang, "-rsbml", sbmlDir, "-p", uriPrefix, inputfile, "-o", outputFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	@Test
	public void test_cmd_l_sbml4() throws IOException{
		/* convert sbol2sbml and store result in output directory without an output file provided*/
		String inputfile = sbolDir + "repressibleTU_Connected.xml";
		
		//Options
		String outputLang = "SBML";	
		
		String[] converter_cmdArgs = {inputfile, "-l", outputLang,"-oDir", sbmlDir};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
	
	
	@Test
	public void test_cmd_l_sbml5() throws IOException{
		/* convert sbol2sbml and output file without full path.*/
		String inputfile = sbolDir + "INV0_output.xml";
		
		//Options
		String outputLang = "SBML";	
		String outputFile = sbmlDir + "INV0_sbmlOut";
		String[] converter_cmdArgs = {inputfile, "-l", outputLang,"-o", outputFile};
		edu.utah.ece.async.ibiosim.conversion.Converter.main(converter_cmdArgs);
	}
}
