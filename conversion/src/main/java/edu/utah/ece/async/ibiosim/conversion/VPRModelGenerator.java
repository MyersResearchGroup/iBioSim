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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.virtualparts.VPRException;
import org.virtualparts.VPRTripleStoreException;
import org.virtualparts.data.QueryParameters;
import org.virtualparts.data2.SBOLInteractionAdder_GeneCentric;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;

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
	 * Perform VPR model generation. 
	 * @param selectedRepo - The specified synbiohub repository the user wants VPR model generator to connect to. 
	 * @param generatedModel - The file to generate the model from.
	 * @param rootModuleID - Name of the top level genetic circuit to be constructed by VPR.
	 * 
	 * @return The generated model.
	 * @throws SBOLValidationException - Validation error in the SBOLDocument.
	 * @throws IOException - Unable to read or write the given SBOLDocument
	 * @throws SBOLConversionException - Unable to perform conversion for the given SBOLDocument.
	 * @throws VPRException - Unable to perform VPR Model Generation on the given SBOLDocument.
	 * @throws VPRTripleStoreException - Unable to perform VPR Model Generation on the given SBOLDocument.
	 */
	public static SBOLDocument generateModel(String selectedRepo, SBOLDocument generatedModel, String rootModuleID) throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException
	{ 
		// TODO: perhaps should remove selectedRepo, but instead compare URIs to all registries in the 
		// list of registries, then for each repo/collection found in the list of CDs, make a separate call
		// to VPR
		List<URI> collections=new ArrayList<URI>();
		for (ComponentDefinition cd : generatedModel.getComponentDefinitions()) {
			if (cd.getIdentity().toString().startsWith(selectedRepo)) {
				String collectionURI = cd.getPersistentIdentity().toString();
				collectionURI = collectionURI.substring(0, collectionURI.lastIndexOf('/')); 
				String collectionId = collectionURI.substring(collectionURI.lastIndexOf('/')+1);
				collectionURI = collectionURI + '/' + collectionId + "_collection/" + cd.getVersion();
				if (!collections.contains(URI.create(collectionURI))) {
					collections.add(URI.create(collectionURI));
				}
			}
		}

		String endpoint = selectedRepo + "/sparql";
		SBOLInteractionAdder_GeneCentric interactionAdder = null;
		QueryParameters params=new QueryParameters();
		params.setCollectionURIs(collections);
		
		if(!rootModuleID.isEmpty() && rootModuleID != null)
		{
			interactionAdder = new SBOLInteractionAdder_GeneCentric(URI.create(endpoint), rootModuleID, params);
		}
		else
		{
			interactionAdder = new SBOLInteractionAdder_GeneCentric(URI.create(endpoint), "TopModule", params);
		}
		interactionAdder.addInteractions(generatedModel);
		return generatedModel;
	}
	
	public static void main(String[] args) 
	{
		//-----REQUIRED FIELD-----
		String fullInputFileName = ""; //input file name
		String selectedRepo = ""; //-r

		//-----OPTIONAL FIELD-----
		boolean noOutput = false; //-no
		String outputFileName = ""; //-o
		String sbolURIPre = ""; //-u
		
		int index = 0;

		for(; index< args.length; index++)
		{
			String flag = args[index];
			switch(flag)
			{
			case "-no":
				noOutput = true;
				break;
			case "-o":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				outputFileName = args[++index];
				break;
			case "-r":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				selectedRepo = args[++index];
				break;
			case "-u":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				sbolURIPre = args[++index];
				break;
			case "-circuitId":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				sbolURIPre = args[++index];
				break;
			default:
				fullInputFileName = args[index];
			}
		}
		
		if(fullInputFileName.isEmpty())
		{
			System.err.println("You must provide the full input file path as this is a required filed.");
			usage();
			return;
		}
		if(selectedRepo.isEmpty())
		{
			System.err.println("You must provide the synbiohub repository instance to perform model generation on as this is a required filed.");
			usage();
			return;
		}
		
		if(sbolURIPre.isEmpty())
		{
			//Default SBOL uri prefix if user didn't provide one.
			sbolURIPre = "http://www.async.ece.utah.edu/";
		}
		
		try 
		{
			SBOLDocument inputSBOL = SBOLUtility.getSBOLUtility().loadSBOLFile(fullInputFileName, sbolURIPre);
			SBOLDocument outputSBOL = generateModel(selectedRepo, inputSBOL, null);
			if(!noOutput)
			{
				if(outputFileName.isEmpty())
				{
					outputSBOL.write(new ByteArrayOutputStream());
				}
				else
				{
					outputSBOL.write(outputFileName+".xml", SBOLDocument.RDF);
				}
			}
			else
			{
				outputSBOL.write(new ByteArrayOutputStream());
			}
		} 
		catch (SBOLValidationException e) 
		{
			System.err.println("ERROR: Invalid SBOL file.");
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			System.err.println("ERROR: Unable to read file.");
			e.printStackTrace();
		} 
		catch (SBOLConversionException e) 
		{
			System.err.println("ERROR: Unable to perform internal SBOL conversion from libSBOLj library.");
			e.printStackTrace();
		} 
		catch (VPRException e) 
		{
			System.err.println("ERROR: Unable to Perform VPR Model Generation");
			e.printStackTrace();
		} 
		catch (VPRTripleStoreException e) 
		{
			System.err.println("ERROR: This SBOL file has contents that can't perform VPR model generation.");
			e.printStackTrace();
		} 
		catch (SBOLException e) 
		{
			System.err.println(e.getMessage());
		}
		
		
		
	}
	
	private static void usage() 
	{
		System.err.println("VPR Model Generation");
		System.err.println("Required:");
		System.err.println("<inputFile> full path of input file");
		System.err.println("\t-r  The specified synbiohub repository the user wants VPR model generator to connect to.");
		System.err.println("Options:");
		System.err.println("\t-o  Specifies the full path of the output file produced from VPR model generator.");
		System.err.println("\t-no  Indicate no output file to be generated.");
		System.err.println("\t-p  	The SBOL uri prefix to be set when reading in the specified SBOL file.");
		
	}

}
