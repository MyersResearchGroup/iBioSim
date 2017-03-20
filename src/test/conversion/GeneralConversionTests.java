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
		/* test a valid sbol file. */
		// [options] <inputFile> -ft <fileType> [-o <outputFile> -e <compareFile>]
		String fileName = "CRISPR_example"; 
		String inputfile = sbolDir + fileName + ".xml";
		String fileType = "SBOL";
		String outputFile = ""; 
		String compareFile = "";
		
		//Options
		String uriPrefix = "http://www.async.ece.utah.edu";
		
		String[] converter_cmdArgs = {"-no", "-p", uriPrefix, inputfile, "-ft", fileType,"-o", outputFile, "-e", compareFile};
		conversion.Converter.main(converter_cmdArgs);
	}
	
}
