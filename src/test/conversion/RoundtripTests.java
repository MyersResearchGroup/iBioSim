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

import java.io.File;

import org.junit.Test;
import conversion.SBML2SBOL;
import conversion.SBOL2SBML;

/**
 * Class to test roundtripping for SBML and SBOL files in SBML2SBOL and SBOL2SBML conversion.
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class RoundtripTests extends ConversionAbstractTests{
	
	public void roundtripSBOLFile(final String fileName){
		String inputFile = sbolDir + fileName + ".xml";
		String outputFile = sbol2sbml_outputDir + fileName + ".xml";
		
		convertSBOL(inputFile, outputFile);
		
		inputFile = outputFile;
		outputFile = sbml2sbol_outputDir + fileName + ".xml";
		
		convertSBML(inputFile, outputFile);
	}
	
	public void roundtripSBMLFile(final String fileName){
		String inputFile = sbmlDir + fileName + ".xml";
		String outputFile = sbml2sbol_outputDir + fileName + ".rdf";
		
		convertSBML(inputFile, outputFile);
		
		inputFile = outputFile;
		outputFile = sbol2sbml_outputDir + fileName + ".xml";
		
		convertSBOL(inputFile, outputFile);
		
		inputFile = outputFile;
		outputFile = sbml2sbol_outputDir + fileName + ".xml";
		
		//convertSBML(inputFile, outputFile);
	}
	
	
	public void convertSBML(String inputFullPath, String outputFullPath){
		String[] sbml2sbol_cmdArgs = {inputFullPath,"-o", outputFullPath};
		SBML2SBOL.main(sbml2sbol_cmdArgs);
	}
	
	public void convertSBOL(String inputFullPath, String outputFullPath){
		String[] sbol2sbml_cmdArgs = {inputFullPath,"-o", outputFullPath};
		SBOL2SBML.main(sbol2sbml_cmdArgs);
	}
	
	
	@Test
	public void run_repressibleTU_Connected(){
		
		String fileName = "repressibleTU_Connected";
		roundtripSBMLFile(fileName);   
	}
	
	@Test
	public void run_CRISPR_example(){
		
		String fileName = "CRISPR_example";
		roundtripSBOLFile(fileName);   
	}
}
