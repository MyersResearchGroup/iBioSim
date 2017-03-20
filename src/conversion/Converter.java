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
package conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.util.SBMLutilities;

/**
 * Provides functionality for validating SBOL data models.
 * 
 * @author Zhen Zhang
 * @author Tramy Nguyen
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

	private static void usage() {
		System.err.println("libSBOLj version " + libSBOLj_Version);
		System.err.println("Description: validates the contents of an SBOL " + SBOLVersion + " document, can compare two documents,\n"
				+ "and can convert to/from SBOL 1.1, GenBank, and FASTA formats.");
		System.err.println();
		System.err.println("Usage:");
		System.err.println("\tjava --jar libSBOLj.jar [options] <inputFile> [-o <outputFile> -e <compareFile>]");
		System.err.println();
		System.err.println("Options:");
		System.err.println("\t-l  <language> specfies language (SBOL1/SBOL2/GenBank/FASTA) for output (default=SBOL2)");
		System.err.println("\t-s  <topLevelURI> select only this object and those it references");
		System.err.println("\t-p  <URIprefix> used for converted objects");
		System.err.println("\t-v  <version> used for converted objects");
		System.err.println("\t-t  uses types in URIs");
		System.err.println("\t-n  allow non-compliant URIs");
		System.err.println("\t-i  allow SBOL document to be incomplete");
		System.err.println("\t-b  check best practices");
		System.err.println("\t-f  continue after first error");
		System.err.println("\t-d  display detailed error trace");
		System.exit(1);
	}

	/**
	 * Command line method for reading an input file and producing an output file.
	 * <p>
	 * By default, validations on compliance and completeness are performed, and types
	 * for top-level objects are not used in URIs.
	 * <p>
	 * Options:
	 * <p>
	 * "-o" specifies an output filename
	 * <p>
	 * "-e" specifies a file to compare if equal to
	 * <p>
	 * "-l" indicates the language for output (default=SBOL2, other options SBOL1, GenBank, FASTA, SBML)
	 * <p>
	 * "-s" select only this topLevel object and those it references
	 * <p>
	 * "-p" specifies the default URI prefix for converted objects
	 * <p>
	 * "-v" specifies version to use for converted objects
	 * <p>
	 * "-t" uses types in URIs
	 * <p>
	 * "-n" allow non-compliant URIs
	 * <p>
	 * "-i" allow SBOL document to be incomplete
	 * <p>
	 * "-b" check best practices
	 * <p>
	 * "-f" fail on first error
	 * <p>
	 * "-d" display detailed error trace
	 * <p>
	 * "-mf" main SBOL file if file diff. option is selected
	 * <p>
	 * "-cf" second SBOL file if file diff. option is selected
	 * <p>
	 * "-ft" Specify the input file type. This should be limited to: SBOL, SBML, and BioPAX
	 * <p>
	 * "-rsbml" The full path of external SBML files to be referenced in the SBML2SBOL conversion
	 * <p>
	 * "-rsbol" The full path of external SBOL files to be referenced in the SBML2SBOL conversion
	 * <p>
	 * "-no" indicate no output file to be generated from validation
	 *
	 * @param args arguments supplied at command line
	 */
	public static void main(String[] args) {
		String fileName = ""; //input SBOL file name
		String outputFile = "";
		String compareFile = "";
		String mainFileName = "";
		String compareFileName = "";
		String topLevelURIStr = "";
		String URIPrefix = "";
		String version = null;

		String inputFileType = ""; //Required field
		String externalSBMLPath = ""; //optional field
		String includeSBOLPath = ""; //optional field
		HashSet<String> ref_sbolInputFilePath = new HashSet<String>();

		boolean complete = true;
		boolean compliant = true;
		boolean typesInURI = false;
		boolean bestPractice = false;
		boolean keepGoing = true;
		boolean showDetail = false;
		boolean genBankOut = false;
		boolean fastaOut = false;
		boolean sbolV1out = false;
		boolean sbmlOut = false;
		boolean noOutput = false;

		int i = 0;

		while (i < args.length) {
			if (args[i].equals("-i")) {
				complete = false;
			} else if (args[i].equals("-t")) {
				typesInURI = true;
			} else if (args[i].equals("-b")) {
				bestPractice = true;
			} else if (args[i].equals("-n")) {
				compliant = false;
			} else if (args[i].equals("-f")) {
				keepGoing = false;
			} else if (args[i].equals("-d")) {
				showDetail = true;
			} else if (args[i].equals("-s")) { 	
				if (i+1 >= args.length) {
					usage();
				}
				topLevelURIStr = args[i+1];
				i++;
			} else if (args[i].equals("-l")) {
				if (i+1 >= args.length) {
					usage();
				}
				if (args[i+1].equals("SBOL1")) {
					sbolV1out = true;
				} else if (args[i+1].equals("GenBank")) {
					genBankOut = true;
				} else if (args[i+1].equals("FASTA")) {
					fastaOut = true;
				} 
				else if (args[i+1].equals("SBML")) {
					sbmlOut = true;
				}
				else if (args[i+1].equals("SBOL2")) {
				} else {
					usage();
				}
				i++;
			} else if (args[i].equals("-o")) {
				if (i+1 >= args.length) {
					usage();
				}
				outputFile = args[i+1];
				i++;
			} else if (args[i].equals("-no")) {
				noOutput = true;
			} else if (args[i].equals("-e")) {
				if (i+1 >= args.length) {
					usage();
				}
				compareFile = args[i+1];
				i++;
			} else if (args[i].equals("-mf")) { 
				if (i+1 >= args.length) {
					usage();
				}
				mainFileName = args[i+1];
				i++;
			} else if (args[i].equals("-cf")) {
				if (i+1 >= args.length) {
					usage();
				}
				compareFileName = args[i+1];
				i++;
			} else if (args[i].equals("-p")) {
				if (i+1 >= args.length) {
					usage();
				}
				URIPrefix = args[i+1];
				i++;
			} else if (args[i].equals("-v")) {
				if (i+1 >= args.length) {
					usage();
				}
				version = args[i+1];
				i++;
			} 
			else if (args[i].equals("-ft")) {
				if (i+1 >= args.length) {
					usage();
				}
				inputFileType = args[i+1];
				i++;
			}
			else if (args[i].equals("-rsbml")) {
				if (i+1 >= args.length) {
					usage();
				}
				externalSBMLPath = args[i+1];
				i++;
			}
			else if (args[i].equals("-rsbol")) {
				if (i+1 >= args.length) {
					usage();
				}
				includeSBOLPath = args[i+1];
				i++;
			}
			else if (fileName.equals("")) {
				fileName = args[i];
			} else {
				usage();
			}
			i++;
		}
		if (fileName.equals("")) usage();
		if (inputFileType.equals("")) usage();

		if(includeSBOLPath != null && !includeSBOLPath.isEmpty()){
			//Note: this is an optional field. User provided sbol path to read in
			File fileDir = new File(includeSBOLPath);
			File[] sbolFiles =  fileDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (name.toLowerCase().endsWith(".rdf") || name.toLowerCase().endsWith(".sbol"));
				}
			});

			for(File f : sbolFiles){
				ref_sbolInputFilePath.add(f.getAbsolutePath());
			}
		}

		File file = new File(fileName);
		boolean isDirectory = file.isDirectory();
		if (!isDirectory) {
			/*
			 * if(inputFileType is SBML){
				Call converter
				Write file to disk
				Validate file of file created from the converter
			 */
			if(inputFileType.equals("SBML")){

				SBOLDocument outSBOLDoc = new SBOLDocument();
				SBMLDocument inputSBMLDoc;
				try {
					inputSBMLDoc = SBMLutilities.readSBML(externalSBMLPath);
					SBML2SBOL.convert_SBML2SBOL(outSBOLDoc, externalSBMLPath,inputSBMLDoc, fileName, ref_sbolInputFilePath, URIPrefix); 
					if(sbolV1out){
						outSBOLDoc.write(outputFile, SBOLDocument.RDFV1);
					}
					else if(fastaOut){
						outSBOLDoc.write(outputFile, SBOLDocument.FASTAformat);
					}
					else if(genBankOut){
						outSBOLDoc.write(outputFile, SBOLDocument.GENBANK);
					}
					else {
						outSBOLDoc.write(outputFile, SBOLDocument.RDF);
					}
					org.sbolstandard.core2.SBOLValidate.validate(outputFile, URIPrefix, complete, compliant, bestPractice, typesInURI, 
							version, keepGoing, compareFile, compareFileName, mainFileName, 
							topLevelURIStr, genBankOut, sbolV1out, fastaOut, outputFile + "_validated", 
							showDetail, noOutput);
					
				} catch (XMLStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SBOLValidationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SBOLConversionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(sbmlOut){
				file = new File(outputFile);
				String absPath = file.getAbsolutePath();
				String outputDir = absPath.substring(0, absPath.lastIndexOf(File.separator)+1);
				String outputName = absPath.substring(absPath.lastIndexOf(File.separator)+1);
				
				org.sbolstandard.core2.SBOLValidate.validate(fileName, URIPrefix, complete, compliant, bestPractice, typesInURI, 
						version, keepGoing, compareFile, compareFileName, mainFileName, 
						topLevelURIStr, genBankOut, sbolV1out, fastaOut, outputFile, 
						showDetail, noOutput);
				
				SBOLDocument sbolDoc;
				try {
					sbolDoc = SBOLReader.read(new FileInputStream(fileName));
				
				if(URIPrefix!=null){
					ModuleDefinition topModuleDef= sbolDoc.getModuleDefinition(URI.create(URIPrefix));
					List<BioModel> models = SBOL2SBML.generateModel(outputDir, topModuleDef, sbolDoc);
					for (BioModel model : models)
					{
						model.save(outputDir + File.separator + model.getSBMLDocument().getModel().getId() + ".xml");
					}
				}
				else{
					//No ModuleDefinition URI provided so loop over all rootModuleDefinition
					for (ModuleDefinition moduleDef : sbolDoc.getRootModuleDefinitions())
					{
						List<BioModel> models = SBOL2SBML.generateModel(outputDir, moduleDef, sbolDoc);
						for (BioModel model : models)
						{
							model.save(outputDir + File.separator + model.getSBMLDocument().getModel().getId() + ".xml");
						}
					}
				}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SBOLValidationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SBOLConversionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XMLStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else {
			for (File eachFile : file.listFiles()) {
				// TODO: should allow compare to a directory of same named files
				System.out.println(eachFile.getAbsolutePath());
				org.sbolstandard.core2.SBOLValidate.validate(eachFile.getAbsolutePath(), URIPrefix, complete, compliant, bestPractice, typesInURI, 
						version, keepGoing, compareFile, compareFileName, mainFileName, 
						topLevelURIStr, genBankOut, sbolV1out, fastaOut, outputFile, 
						showDetail, noOutput);
			}
		}
	}

}