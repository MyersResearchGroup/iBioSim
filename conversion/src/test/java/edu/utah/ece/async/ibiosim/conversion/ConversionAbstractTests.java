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

/**
 * 
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class ConversionAbstractTests {
	
	protected String resourceDir = ConversionAbstractTests.class.getResource(".").getPath() + File.separator ;
	
	//output dir
	protected String sbol2sbml_outputDir = "./src/test/resources/edu/utah/ece/async/ibiosim/conversion/SBOL2SBML_out/";
	protected String sbml2sbol_outputDir = "./src/test/resources/edu/utah/ece/async/ibiosim/conversion/SBML2SBOL_out/";
	protected String genBank_outputDir = "./src/test/resources/edu/utah/ece/async/ibiosim/conversion/GenBank_out/";
	
	protected String sbmlDir = resourceDir + "SBML" + File.separator;
	protected String sbolDir = resourceDir + "SBOL" + File.separator;

	
}
