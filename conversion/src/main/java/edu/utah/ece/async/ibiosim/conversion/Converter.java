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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.ode.ODEIntegrator;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;
import org.synbiohub.frontend.SynBioHubException;
import org.virtualparts.VPRException;
import org.virtualparts.VPRTripleStoreException;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;

import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.Submodel;


//import edu.utah.ece.async.ibiosim.gui.Gui;

/**
 * Command line for:
 * - running conversion for SBML to/from SBOL, GenBank, Fasta, SBOL1 
 * - validating SBOL files.
 * 
 * @author Tramy Nguyen
 * @author Zhen Zhang
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Converter {

	/**
	 * the current SBOL version
	 */
	private static final String SBOLVersion = "2.0";
	private static final String libSBOLj_Version = "2.1";

	private static void usage() 
	{
		System.err.println("libSBOLj version " + libSBOLj_Version);
		System.err.println("Description: validates the contents of an SBOL " + SBOLVersion + " document, can compare two documents,\n"
				+ "and can convert to/from SBOL 1.1, to/from SBML, GenBank, and FASTA formats.");
		System.err.println();
		System.err.println("Usage:");
		//TODO PEDRO shouldnt this be conersion 3.0.0-SNAPSHOT?
		System.err.println("\tjava -jar iBioSim-conversion-0.0.1-SNAPSHOT-jar-with-dependencies.jar [options] inputFile ");
		System.err.println();
		System.err.println("Convert SBOL to SBML Example:");
		System.err.println("\tjava -jar iBioSim-conversion-0.0.1-SNAPSHOT-jar-with-dependencies.jar -l SBML -p <SBOL default URI prefix> <inputFile> -o <outputFileName> ");
		System.err.println();
		System.err.println("Required:");
		System.err.println("<inputFile> full path of input file");
		System.err.println();
		System.err.println("Options:");
		System.err.println("\t-b  check best practices");
		System.err.println("\t-cf The name of the file that will be produced to hold the result of the second SBOL file, if SBOL file diff was selected.");
		System.err.println("\t-d  display detailed error trace");
		System.err.println("\t-e  The second SBOL file to compare to the main SBOL file.");
		System.err.println("\t-esf  Export SBML hierarchical models in a single output file.");
		System.err.println("\t-f  continue after first error");
		System.err.println("\t-i  allow SBOL document to be incomplete");
		System.err.println("\t-l  <language> specifies language (SBOL1/SBOL2/GenBank/FASTA/SBML) for output (default=SBOL2). To output FASTA or GenBank, no SBOL default URI prefix is needed.");
		System.err.println("\t-mf The name of the file that will be produced to hold the result of the main SBOL file, if SBOL file diff was selected.");
		System.err.println("\t-n  allow non-compliant URIs");
		System.err.println("\t-o  <outputFile> specifies the full path of the output file produced from the converter");
		System.err.println("\t-no  indicate no output file to be generated from validation. Instead, print result to console/command line.");
		System.err.println("\t-oDir  output directory when SBOL to SBML conversion is performed and multiple SBML files are produced for individual submodels.");
		System.err.println("\t-p  <URIprefix> used for converted objects");
		System.err.println("\t-rsbml  The full path of external SBML files to be referenced in the SBML2SBOL conversion");
		System.err.println("\t-rsbol  The full path of external SBOL files to be referenced in the SBML2SBOL conversion");
		System.err.println("\t-s  <topLevelURI> select only this object and those it references");
		System.err.println("\t-t  uses types in URIs");
		System.err.println("\t-v  <version> used for converted objects");
		System.err.println("\t-r  <url> The specified synbiohub repository the user wants VPR model generator to connect to");
		System.err.println("\t - env <SBML environment file> is the complete directory path of the environmental file to instantiate to your model. This only works when VPR model generator is used");
		System.exit(1);
	}
	

	
/*	*//**
	 * Convert SBOL to SBML.
	 * @param inputSBOLDoc - The SBOLDocument to convert to SBML
	 * @param filePath - The file location where the SBOL document is located.
	 * @return The number of SBML models that was converted from SBOL. 
	 *//*
	public static int generateSBMLFromSBOL(SBOLDocument inputSBOLDoc, String filePath) {
		int numGeneratedSBML = 0;
		try {
			for (ModuleDefinition moduleDef : inputSBOLDoc.getRootModuleDefinitions()) {
				if (moduleDef.getModels().size()==0) {
					HashMap<String,BioModel> models;
					try {
						models = SBOL2SBML.generateModel(filePath, moduleDef, inputSBOLDoc);
						for (BioModel model : models.values()) {
							if (overwrite(filePath + File.separator + model.getSBMLDocument().getModel().getId() + ".xml",
									model.getSBMLDocument().getModel().getId() + ".xml")) {
								model.save(filePath + File.separator + model.getSBMLDocument().getModel().getId() + ".xml",false);
							}
							numGeneratedSBML++;
						}
					} catch (XMLStreamException e) {

						e.printStackTrace();
					}
					catch (BioSimException e) {

						e.printStackTrace();
					}
					catch (SBOLValidationException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {

		} 
		return numGeneratedSBML;
	}*/

	public static void main(String[] args) 
	{
		//-----REQUIRED FIELD-----
		String fullInputFileName = ""; //input file name

		//-----OPTIONAL FIELD-----
		boolean bestPractice = false; //-b
		boolean showDetail = false; //-d
		boolean singleSBMLOutput = false; //-esf;
		boolean keepGoing = true; //-f
		boolean complete = true; //-i
		boolean genBankOut = false; //-l
		boolean fastaOut = false; //-l
		boolean sbolV1out = false; //-l
		boolean sbolV2out = false; //-l
		boolean sbmlOut = false; //-l
		boolean compliant = true; //-n
		boolean noOutput = false; //-no
		boolean typesInURI = false; //-t
		boolean doVPR = false; //-r
		boolean isDiffFile = false; //indicate if diffing of SBOL files are done
		boolean isValidation = false; //indicate if only validate SBOL files
		boolean topEnvir = false; // determines if there is a topEnvironment model to be instantiated 
		
		String compFileResult = ""; //-cf
		String compareFile = ""; //-e
		String mainFileResult = ""; //-mf
		String outputFileName = ""; //-o
		String outputDir = ""; //-oDir
		String URIPrefix = ""; //-p
		String externalSBMLPath = ""; //-rsbml
		String externalSBOLPath = ""; //-rsbol
		String topLevelURIStr = ""; //-s
		String version = null; //-v
		String urlVPR = ""; //The specified synbiohub repository the user wants VPR model generator to connect to.
		String environment ="";
		
		HashSet<String> ref_sbolInputFilePath = new HashSet<String>(); //rsbol

		int index = 0;

		for(; index< args.length; index++)
		{
			String flag = args[index];
			switch(flag)
			{
			case "-b":
				bestPractice = true;
				break;	
			case "-d":
				showDetail = true;
				break;
			case "-esf":
				singleSBMLOutput = true;
				break;
			case "-f":
				keepGoing = false;
				break;
			case "-i":
				complete = false;
				break;
			case "-n":
				compliant = false;
				break;
			case "-no":
				noOutput = true;
				break;
			case "-t":
				typesInURI = true;
				break;
			case "-s":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				topLevelURIStr = args[++index];
				break;
			case "-l":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				if (args[index+1].equals("SBOL1")) 
				{
					sbolV1out = true;
					++index;
				} 
				else if (args[index+1].equals("GenBank")) 
				{
					genBankOut = true;
					++index;
				} 
				else if (args[index+1].equals("FASTA")) 
				{
					fastaOut = true;
					++index;
				} 
				else if (args[index+1].equals("SBML")) 
				{
					sbmlOut = true;
					++index;
				}
				else if (args[index+1].equals("SBOL2")) 
				{
					sbolV2out = true;
					++index;
				} 
				else 
				{
					usage();
				}
				break;
			case "-e":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				compareFile = args[++index];
				isDiffFile = true;
				break;
			case "-cf":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				compFileResult = args[++index];
				break;
			case "-mf":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				mainFileResult = args[++index];
				break;
			case "-o":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				outputFileName = args[++index];
				break;
			case "-oDir":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				outputDir = args[++index];
				break;
			case "-p":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				URIPrefix = args[++index];
				break;
			case "-rsbml":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				externalSBMLPath = args[++index];
				break;
			case "-rsbol":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				externalSBOLPath = args[++index];
				break;
			case "-v":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				version = args[++index];
				break;
			case "-r":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				doVPR = true;
				urlVPR = args[++index];
				break;
			case "-env":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-'))
				{
					usage();
				}
				topEnvir = true;
				environment = args[++index];
				break;
			default:
				fullInputFileName = args[index];
			}
		}

		/* Note: Check all required field has been set. If not, stop user from continuing.*/
		boolean inputIsSBOL = false; 
		boolean inputIsSBML = false;
		if(!fullInputFileName.isEmpty())
		{
			//find out what input file format is
			try 
			{
				if(SBMLutilities.isSBMLFile(fullInputFileName))
				{
					inputIsSBML = true;
				}
				else
				{
					inputIsSBOL = true;
				}
			} 
			catch (IOException e) 
			{
				System.err.println("Invalid input file.");
				usage(); 
			}

		}
		else
		{
			System.err.println("You must provide the full input file path as this is a required field.");
			usage();
			return;
		}

		/* Note: Initialize any optional variables that are required for conversion, validation, or file diff.*/
		if(externalSBOLPath != null && !externalSBOLPath.isEmpty())
		{
			ref_sbolInputFilePath = SBOLUtility.getSBOLFilesFromPath(externalSBOLPath);
		}

		if(genBankOut == false && fastaOut == false && sbolV1out == false && sbolV2out == false && sbmlOut == false)
		{
			isValidation = true;
		}

		//If the output directory is empty, get the path from the output file name that the user has specified
		// since we assume that the user will always provide the full path for the output file name.
		if(!outputFileName.isEmpty())
		{
			String tempFile = outputFileName;
			File outputFilePath = new File(tempFile);

			if(outputDir.isEmpty())
			{
				outputFileName = outputFilePath.getName();
				String tempDir = outputFilePath.getParent();
				if(tempDir == null)
				{
					System.err.println("ERROR: We require that your output file must be provided as a full path (i.e. location/of/outputFile.xml)");
					usage();
				}
				else
				{
					outputDir = outputFilePath.getParent();
				}
			}
			else
			{
				//The user specified both the output directory and an output file so 
				//update the output file name and leave the output directory as is.
				outputFileName = outputFilePath.getName();
			}
		}
		else if(outputFileName.isEmpty() && sbmlOut)
		{
			outputFileName = "default_SBMLOutput";
			noOutput = true;
		}
		else if(outputFileName.isEmpty() && sbolV2out)
		{
			outputFileName = "default_SBOLOutput";
			noOutput = true;
		}
		else if(outputFileName.isEmpty() && !noOutput && !isValidation && !isDiffFile)
		{
			System.err.println("ERROR: Unless result is indicated to print to console, you must provide an output file name to perform any form of conversion.");
			usage();
		}
		String fullPathOutput = outputDir + File.separator + outputFileName;

		File file = new File(fullInputFileName); 
		boolean isDirectory = file.isDirectory();
		if (!isDirectory) 
		{
			if(inputIsSBML)
			{
				SBOLDocument outSBOLDoc = new SBOLDocument();
				SBMLDocument inputSBMLDoc;
				try 
				{
					//We must guarantee user must provide SBOL default URI or else tell user to provide one.
					if(URIPrefix.isEmpty())
					{
						System.err.println("ERROR: You must provide an SBOL URI prefix in order to perform "
								+ "conversion or validation for the inputted file.");
						usage();
					}

					//SBML file is relative. No external path was given for the input SBML file.

					inputSBMLDoc = SBMLutilities.readSBML(fullInputFileName, null, null);

					SBML2SBOL.convert_SBML2SBOL(outSBOLDoc, externalSBMLPath, inputSBMLDoc, fullInputFileName, ref_sbolInputFilePath, URIPrefix); 

					if(noOutput)
					{
						outSBOLDoc.write(System.out);
					}
					else
					{
						outSBOLDoc.write(fullPathOutput, SBOLDocument.RDF);
						String sbolVal_fileName = fullPathOutput;
						String sbolVal_outFileName = fullPathOutput + "_validated";

						// Since the validator requires that the sbolVal_fileName is the full path of the input SBOL file, 
						// this validator will only pass if the user indicated that the the output file name was provided.
						// Else, if the user indicated that they want the result printed to the console, then no output file name was given

						org.sbolstandard.core2.SBOLValidate.validate(System.out,System.err,sbolVal_fileName, URIPrefix, complete, compliant, bestPractice, typesInURI,
								version, keepGoing, compareFile, compFileResult, mainFileResult, 
								topLevelURIStr, genBankOut, sbolV1out, fastaOut, false, sbolVal_outFileName, 
								showDetail, noOutput,true);
					}

				} 
				catch (XMLStreamException e) 
				{
					System.err.println("ERROR: Invalid XML file");
					e.printStackTrace();
				} 
				catch (SBOLValidationException e) 
				{
					System.err.println("ERROR: Invalid SBOL file");
					e.printStackTrace();
				} 
				catch (SBOLConversionException e) 
				{
					System.err.println("ERROR: Unable to perform SBOL conversion");
					e.printStackTrace();
				} 
				catch (IOException e) 
				{
					System.err.println("ERROR: Unable to read or write file");
					e.printStackTrace();
				}
				catch (BioSimException e) {
					System.err.println("ERROR: Invalid SBML file");
					e.printStackTrace();
				}
			} //end of is input is SBML
			else if(inputIsSBOL)
			{
				// If the user want to diff between two SBOL file or only perform validation for a single SBOL file, 
				// call the validation method and then skip the rest. 
				org.sbolstandard.core2.SBOLValidate.validate(System.out,System.err,fullInputFileName, URIPrefix, complete, compliant, bestPractice, typesInURI, 
						version, keepGoing, compareFile, compFileResult, mainFileResult, 
						topLevelURIStr, genBankOut, sbolV1out, fastaOut, false, fullPathOutput, 
						showDetail, noOutput, true);

				//User wants to convert SBOL2SBML, printing to console or saving to sbml file is done in SBMLutilities.
				if(!isDiffFile && !isValidation)
				{
					try 
					{	
						SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(fullInputFileName, URIPrefix);
						String circuit_name = file.getName();
						circuit_name = circuit_name.replace(".xml", "");
						String vpr_output =  circuit_name + "_topModule";
					
						if(!topLevelURIStr.isEmpty())
						{
							//TODO PEDRO calling VPR
							if (doVPR) {
								TopLevel top = sbolDoc.getTopLevel(URI.create(topLevelURIStr));
								SBOLDocument newSbolDoc = sbolDoc.createRecursiveCopy(top);
								
								try {
									newSbolDoc = VPRModelGenerator.generateModel(urlVPR, newSbolDoc, vpr_output);
									//newSbolDoc = VPRModelGenerator.generateModel(urlVPR, newSbolDoc, "topModule");
								} catch (VPRException e) {
									System.err.println("ERROR: VPR generation fails");
									e.printStackTrace();
								} catch (VPRTripleStoreException e) {
									System.err.println("ERROR: VPR generation fails");
									e.printStackTrace();
								}
								//generateSBMLFromSBOL(newSbolDoc, outputDir);
								
								for (ModuleDefinition moduleDef : newSbolDoc.getRootModuleDefinitions())
								{
									HashMap<String,BioModel> models = SBOL2SBML.generateModel(outputDir, moduleDef, newSbolDoc);
									SBMLutilities.exportSBMLModels(models, outputDir, outputFileName, noOutput, sbmlOut, singleSBMLOutput);
								} 
							}
							
							else {
							ModuleDefinition topModuleDef = sbolDoc.getModuleDefinition(URI.create(topLevelURIStr));
							HashMap<String,BioModel> models = SBOL2SBML.generateModel(outputDir, topModuleDef, sbolDoc);
							SBMLutilities.exportSBMLModels(models, outputDir, outputFileName, noOutput, sbmlOut, singleSBMLOutput);
							}
						} 
						else
						{
							//TODO PEDRO calling VPR
							if (doVPR) {
								try {
									sbolDoc = VPRModelGenerator.generateModel(urlVPR, sbolDoc, vpr_output);
									//sbolDoc = VPRModelGenerator.generateModel(urlVPR, sbolDoc, "topModule");
								} catch (VPRException e) {
									System.err.println("ERROR: VPR generation fails");
									e.printStackTrace();
								} catch (VPRTripleStoreException e) {
									System.err.println("ERROR: VPR generation fails");
									e.printStackTrace();
								}
							}
							
							for (ModuleDefinition moduleDef : sbolDoc.getRootModuleDefinitions())
							{
								HashMap<String,BioModel> models = SBOL2SBML.generateModel(outputDir, moduleDef, sbolDoc);
								SBMLutilities.exportSBMLModels(models, outputDir, outputFileName, noOutput, sbmlOut, singleSBMLOutput);
							} 
						}
						if (doVPR) {
							if (topEnvir) {
								SBMLDocument topEnvironment = SBMLutilities.readSBML(environment, null, null);
								
								
								CompSBMLDocumentPlugin docPlugin = (CompSBMLDocumentPlugin) topEnvironment.getPlugin("comp");					
								ExternalModelDefinition exte = docPlugin.getExternalModelDefinition("TopModel");
								exte.setId(vpr_output);
								exte.setSource(vpr_output + ".xml");
								//exte.setId("topModule");
								//exte.setSource("topModule.xml");
														
								CompModelPlugin SBMLplugin = (CompModelPlugin) topEnvironment.getModel().getPlugin("comp");
								Submodel top = SBMLplugin.getSubmodel("C1");
								top.setModelRef(vpr_output);
								//top.setModelRef("topModule");

								SBMLWriter writing = new SBMLWriter();
								
								writing.writeSBMLToFile(topEnvironment, outputDir + File.separator + "Environment.xml");
							}
						}				
					}
					catch (FileNotFoundException e) 
					{
						System.err.println("ERROR:  Unable to locate file");
						e.printStackTrace();
					} 
					catch (SBOLValidationException e) 
					{
						System.err.println("ERROR: Invalid SBOL file");
						e.printStackTrace();
					} 
					catch (IOException e) 
					{
						System.err.println("ERROR: Unable to read or write file");
						e.printStackTrace();
					} 
					catch (SBOLConversionException e) 
					{
						System.err.println("ERROR: Unable to perform SBOL conversion");
						e.printStackTrace();
					} 
					catch (XMLStreamException e) 
					{
						System.err.println("ERROR: Invalid XML file");
						e.printStackTrace();
					} 
					catch (SBOLException e) 
					{
						System.err.println(e.getMessage());
					}
					catch (BioSimException e) 
					{
						System.err.println(e.getMessage());
					} 
				}
			} //end of isSBOL input
		}//end of is not a directory check
		else
		{
			for (File eachFile : file.listFiles()) 
			{
				org.sbolstandard.core2.SBOLValidate.validate(System.out,System.err,eachFile.getAbsolutePath(), URIPrefix, complete, compliant, bestPractice, typesInURI, 
						version, keepGoing, compareFile, compFileResult, mainFileResult, 
						topLevelURIStr, genBankOut, sbolV1out, fastaOut, false, fullPathOutput, 
						showDetail, noOutput,true);
			}
		}
	} // end of method

}
