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
package edu.utah.ece.async;

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
	
	protected String workingDirectory = System.getProperty("user.dir");
	protected String resourceDir = workingDirectory + File.separator + "src" + File.separator + "test" + File.separator + "conversion" + File.separator + "resources" + File.separator ;
	protected String sbml2sbol_outputDir = resourceDir + "SBML2SBOL_Output" + File.separator;
	protected String sbol2sbml_outputDir = resourceDir + "SBOL2SBML_Output" + File.separator;
	protected String genBank_outputDir = resourceDir + "GenBank_Output" + File.separator;
	protected String fasta_outputDir = resourceDir + "FASTA_Output" + File.separator;
	protected String sbmlDir = resourceDir + "SBML" + File.separator;
	protected String sbolDir = resourceDir + "SBOL" + File.separator;
	
	
	
}
