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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.utah.ece.async.biomodel.parser.BioModel;
import edu.utah.ece.async.biomodel.util.SBMLutilities;
import edu.utah.ece.async.util.GlobalConstants;

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
		System.err.println("Required:");
		System.err.println("<inputFile> name of input file");
		System.err.println("\t-e  specifies a file to compare if equal to");
		System.err.println("\t-o  <outputFile> specifies the output file produced from the converter");
		System.err.println();
		System.err.println("Options:");
		System.err.println("\t-b  check best practices");
		System.err.println("\t-cf  second SBOL file if file diff. option is selected");
		System.err.println("\t-d  display detailed error trace");
		System.err.println("\t-f  continue after first error");
		System.err.println("\t-i  allow SBOL document to be incomplete");
		System.err.println("\t-l  <language> specifies language (SBOL1/SBOL2/GenBank/FASTA/SBML) for output (default=SBOL2)");
		System.err.println("\t-mf  main SBOL file if file diff. option is selected.");
		System.err.println("\t-n  allow non-compliant URIs");
		System.err.println("\t-no  indicate no output file to be generated from validation");
		System.err.println("\t-oDir  output directory of resulting conversion and validation file");
		System.err.println("\t-p  <URIprefix> used for converted objects");
		System.err.println("\t-rsbml  The full path of external SBML files to be referenced in the SBML2SBOL conversion");
		System.err.println("\t-rsbol  The full path of external SBOL files to be referenced in the SBML2SBOL conversion");
		System.err.println("\t-s  <topLevelURI> select only this object and those it references");
		System.err.println("\t-t  uses types in URIs");
		System.err.println("\t-v  <version> used for converted objects");
		System.exit(1);
	}

	/**
	 * Command line method for reading an input file and producing an output file. 
	 * --jar libSBOLj.jar [options] <inputFile> [-o <outputFile> -e <compareFile>]
	 * <p>
	 * By default, validations on compliance and completeness are performed, and types
	 * for top-level objects are not used in URIs.
	 * <p>
	 * Requirements:
	 * <p>
	 * inputfile
	 * <p>
	 * "-e" specifies a file to compare if equal to
	 * <p>
	 * "-o" specifies an output filename
	 * <p>
	 * 
	 * Options:
	 * <p>
	 * "-b" check best practices
	 * <p>
	 * "-cf" second SBOL file if file diff. option is selected
	 * <p>
	 * "-d" display detailed error trace
	 * <p>
	 * "-esf" export single SBML file
	 * <p>
	 * "-f" fail on first error
	 * <p>
	 * "-i" allow SBOL document to be incomplete
	 * <p>
	 * "-l" indicates the language for output (default=SBOL2, other options SBOL1, GenBank, FASTA, SBML)
	 * <p>
	 * "-mf" main SBOL file if file diff. option is selected
	 * <p>
	 * "-n" allow non-compliant URIs
	 * <p>
	 * "-no" indicate no output file to be generated from validation
	 * <p>
	 * "-oDir" output directory of resulting conversion and validation file
	 * <p>
	 * "-p" specifies the default URI prefix for converted objects
	 * <p>
	 * "-rsbml" The full path of external SBML files to be referenced in the SBML2SBOL conversion
	 * <p>
	 * "-rsbol" The full path of external SBOL files to be referenced in the SBML2SBOL conversion
	 * <p>
	 * "-s" select only this topLevel object and those it references
	 * <p>
	 * "-t" uses types in URIs
	 * <p>
	 * "-v" specifies version to use for converted objects
	 * <p>
	 *
	 * @param args arguments supplied at command line
	 */
	public static void main(String[] args) {

		//-----REQUIRED FIELD-----
		String fileName = ""; //input file name
		String compareFile = ""; //-e
		String outputFileName = ""; //-o

		//-----OPTIONAL FIELD-----
		boolean bestPractice = false; //-b
		String compareFileName = ""; //-cf
		boolean showDetail = false; //-d
		boolean singleSBMLOutput = false; //-esf;
		boolean keepGoing = true; //-f
		boolean complete = true; //-i
		boolean genBankOut = false; //-l
		boolean fastaOut = false; //-l
		boolean sbolV1out = false; //-l
		boolean sbmlOut = false; //-l
		String mainFileName = ""; //-mf
		boolean compliant = true; //-n
		boolean noOutput = false; //-no
		String outputDir = ""; //-oDir
		String URIPrefix = ""; //-p
		String externalSBMLPath = ""; //-rsbml
		String externalSBOLPath = ""; //-rsbol
		HashSet<String> ref_sbolInputFilePath = new HashSet<String>(); //rsbol
		String topLevelURIStr = ""; //-s
		boolean typesInURI = false; //-t
		String version = null; //-v

		int index = 0;

		for(; index< args.length; index++){
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
				if(index+1 >= args.length || args[index+1].equals("-")){
					usage();
				}
				topLevelURIStr = args[++index];
				break;
			case "-l":
				if(index+1 >= args.length || args[index+1].equals("-")){
					usage();
				}
				if (args[index+1].equals("SBOL1")) {
					sbolV1out = true;
					++index;
				} 
				else if (args[index+1].equals("GenBank")) {
					genBankOut = true;
					++index;
				} 
				else if (args[index+1].equals("FASTA")) {
					fastaOut = true;
					++index;
				} 
				else if (args[index+1].equals("SBML")) {
					sbmlOut = true;
					++index;
				}
				else if (args[index+1].equals("SBOL2")) {
					++index;
				} 
				else {
					usage();
				}
				break;
			case "-e":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-')){
					usage();
				}
				compareFile = args[++index];
				break;
			case "-cf":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-')){
					usage();
				}
				compareFileName = args[++index];
				break;
			case "-mf":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-')){
					usage();
				}
				mainFileName = args[++index];
				break;
			case "-o":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-')){
					usage();
				}
				outputFileName = args[++index];
				break;
			case "-oDir":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-')){
					usage();
				}
				outputDir = args[++index];
				break;
			case "-p":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-')){
					usage();
				}
				URIPrefix = args[++index];
				break;
			case "-rsbml":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-')){
					usage();
				}
				externalSBMLPath = args[++index];
				break;
			case "-rsbol":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-')){
					usage();
				}
				externalSBOLPath = args[++index];
				break;
			case "-v":
				if(index+1 >= args.length || (!args[index+1].isEmpty() && args[index+1].charAt(0)=='-')){
					usage();
				}
				version = args[++index];
				break;
			default:
				fileName = args[index];
			}
		}

		/* Note: Check all required field has been set. If not, stop user from continuing.*/
		boolean inputIsSBOL = false; 
		boolean inputIsSBML = false;
		if(!fileName.isEmpty()){
			//find out what input file format is
			String inputFileType = getXMLFileType(fileName);
			if(inputFileType.equals("sbml")){
				inputIsSBML = true;
			}
			else if(inputFileType.equals("rdf:RDF")){
				inputIsSBOL = true;
			}
		}
		else{
			usage();
		}

		/* Note: Initialize any optional variables that are required for conversion, validation, or file diff.*/
		if(externalSBOLPath != null && !externalSBOLPath.isEmpty()){
			//Note: this is an optional field. User provided sbol path to read in
			File fileDir = new File(externalSBOLPath);
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
			 * SBML2SBOL: 
			 * inputFileType is SBML
			 * Call converter
			 * Write file to disk
			 * Validate file of file created from the converter
			 */
			if(inputIsSBML){
				
				SBOLDocument outSBOLDoc = new SBOLDocument();
				SBMLDocument inputSBMLDoc;
				try {
					//We must guarantee user must provide SBOL default URI or else tell user to provide one.
					if(!URIPrefix.isEmpty()){
						if (externalSBMLPath.isEmpty()) {
							//SBML file is relative. No external path was given for the input SBML file. 
							inputSBMLDoc = SBMLutilities.readSBML(fileName);
						} 
						else {
							inputSBMLDoc = SBMLutilities.readSBML(externalSBMLPath + GlobalConstants.separator + fileName);
						}
						
						SBML2SBOL.convert_SBML2SBOL(outSBOLDoc, externalSBMLPath, inputSBMLDoc, fileName, ref_sbolInputFilePath, URIPrefix); 
						
						if(!noOutput){
							if(outputFileName.isEmpty()){
								System.err.println("You must provide an output file name to convert SBML to SBOL.");
								usage();
							}
							if(outputDir.isEmpty()){
								System.err.println("You must provide an output directory to store the SBOL file after SBML to SBOL conversion is performed.");
								usage();
							}
							outSBOLDoc.write(outputDir + outputFileName, SBOLDocument.RDF);
						}
						else{
							outSBOLDoc.write(new ByteArrayOutputStream());
						}

					} 
					else{
						System.err.println("You must provide an SBOL URI prefix in order to convert SBML to SBOL.");
						usage();
					}
					org.sbolstandard.core2.SBOLValidate.validate(outputFileName, URIPrefix, complete, compliant, bestPractice, typesInURI, 
							version, keepGoing, compareFile, compareFileName, mainFileName, 
							topLevelURIStr, genBankOut, sbolV1out, fastaOut, outputFileName + "_validated", 
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
			else if(inputIsSBOL){

				//Perform validation on the input SBOL file if no SBML output file is expected
				org.sbolstandard.core2.SBOLValidate.validate(fileName, URIPrefix, complete, compliant, bestPractice, typesInURI, 
						version, keepGoing, compareFile, compareFileName, mainFileName, 
						topLevelURIStr, genBankOut, sbolV1out, fastaOut, outputFileName, 
						showDetail, noOutput);
				
				if(sbmlOut){

					SBOLDocument sbolDoc;
					try {
						sbolDoc = SBOLReader.read(new FileInputStream(fileName));

						if(!URIPrefix.isEmpty()){
							ModuleDefinition topModuleDef= sbolDoc.getModuleDefinition(URI.create(URIPrefix));
							List<BioModel> models = SBOL2SBML.generateModel(outputDir, topModuleDef, sbolDoc);
							
							if(singleSBMLOutput)
							{
								// Note: last one is always the top model base on SBOL2SBML converter
								BioModel target = models.get(models.size() - 1);
//								SBMLDocument doc = target.getSBMLDocument();
//								ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(doc);
//								target.getSBMLComp().unsetListOfExternalModelDefinitions();
//								for(SBase element : elements)
//								{
//									element.unsetMetaId();
//								}
//								for (int i = 0; i < models.size()-1; ++i)
//								{
//									BioModel bioModel = models.get(i);
//									doc = bioModel.getSBMLDocument();
//									elements = SBMLutilities.getListOfAllElements(doc);
//									for(SBase element : elements)
//									{
//										element.unsetMetaId();
//									}
//									ModelDefinition md = new ModelDefinition(doc.getModel());
//									target.getSBMLComp().addModelDefinition(md);
//									
//								}
//								SBMLWriter.write(target.getSBMLDocument(), new File(outputDir + File.separator + target.getSBMLDocument().getModel().getId() + ".xml"), ' ', (short) 4);
								//target.save(outputDir + File.separator + target.getSBMLDocument().getModel().getId() + ".xml");
								target.exportSingleFile(outputDir + outputFileName);
							}
							else if(noOutput){
								//TODO: print result of SBOL2SBML to OutputStream
							}
							else
							{ 
								//Multiple SBML output
								for (BioModel model : models)
								{
									model.save(outputDir + File.separator + model.getSBMLDocument().getModel().getId() + ".xml");
								}
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
				else{
					System.err.println("You must provide an SBOL URI prefix in order to convert SBOL to SBML.");
					usage();
				}
			}

		} else {
			for (File eachFile : file.listFiles()) {
				// TODO: should allow compare to a directory of same named files
				System.out.println(eachFile.getAbsolutePath());
				org.sbolstandard.core2.SBOLValidate.validate(eachFile.getAbsolutePath(), URIPrefix, complete, compliant, bestPractice, typesInURI, 
						version, keepGoing, compareFile, compareFileName, mainFileName, 
						topLevelURIStr, genBankOut, sbolV1out, fastaOut, outputFileName, 
						showDetail, noOutput);
			}
		}
	}

	/**
	 * Determine what file format the given xml file is. (i.e. sbml or rdf:RDF)
	 * @param file - The given xml file.
	 * @return The type of the given xml file.
	 */
	private static String getXMLFileType(String file){
		String fileType = "";
		File xmlFile = new File(file);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.parse(xmlFile); 
			fileType = doc.getDocumentElement().getNodeName();
			return fileType;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileType;

	}
	
	private boolean isSBMLFile(String file){
		SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
			SAXParser saxParser = factory.newSAXParser();
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return false;
	}

}