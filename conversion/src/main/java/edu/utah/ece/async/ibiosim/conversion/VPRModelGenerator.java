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
import java.io.IOException;
import java.net.URI;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import uk.ac.ncl.ico2s.VPRException;
import uk.ac.ncl.ico2s.VPRTripleStoreException;
import uk.ac.ncl.ico2s.sbolstack.SBOLInteractionAdder_GeneCentric;

/**
 * Perform VPR Model Generation
 * 
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VPRModelGenerator {

	/**
	 * Generate SBOL model from the given design file using given synbiohub repository
	 * 
	 * @param endpoint - The specified synbiohub repository the user wants VPR model generator to connect to. 
	 * @param generatedModel - The file to generate the model from.
	 * @return
	 * @throws SBOLValidationException
	 * @throws IOException
	 * @throws SBOLConversionException
	 * @throws VPRException
	 * @throws VPRTripleStoreException
	 */
	public static SBOLDocument generateModel(String selectedRepo, SBOLDocument generatedModel) throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException
	{ 
		//"http://synbiohub.org/sparql"
		String endpoint = selectedRepo + "/sparql";
		SBOLInteractionAdder_GeneCentric interactionAdder = new SBOLInteractionAdder_GeneCentric(URI.create(endpoint));
		interactionAdder.addInteractions(generatedModel);
		return generatedModel;
		
	}

}
