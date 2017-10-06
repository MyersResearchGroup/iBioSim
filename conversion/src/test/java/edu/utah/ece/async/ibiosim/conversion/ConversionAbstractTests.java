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

import java.io.File;

import org.apache.commons.io.IOUtils;

/**
 * 
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class ConversionAbstractTests {
	
	protected String conversionResourceDir = "src" + File.separator + "test" + File.separator + 
			"resources" + File.separator + "edu" + File.separator + "utah" + File.separator + 
			"ece" + File.separator + "async" + File.separator + "ibiosim" + File.separator + 
			"conversion" + File.separator;
	
	//output dir
	protected String sbol2sbml_outputDir = conversionResourceDir + "SBOL2SBML_out/";
	protected String sbml2sbol_outputDir = conversionResourceDir + "SBML2SBOL_out/";
	protected String genBankDir = conversionResourceDir + "GenBankFiles/";
	
	protected String sbmlDir = conversionResourceDir + "SBML/";
	protected String sbolDir = conversionResourceDir + "SBOL/";

	
}
