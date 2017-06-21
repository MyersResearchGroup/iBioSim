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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;

/**
 * Command line method for running conversion jar file. 
 * java -jar conversion-0.0.1-SNAPSHOT-jar-with-dependencies.jar [options] inputFile 
 * <p>
 * By default, validations on compliance and completeness are performed, and types
 * for top-level objects are not used in URIs.
 * <p>
 * Requirements:
 * <p>
 * inputfile
 * <p>
 * 
 * Options:
 * <p>
 * "-b" check best practices
 * <p>
 * "-cf" The name of the file that will be produced to hold the result of the second SBOL file, if SBOL file diff was selected.
 * <p>
 * "-d" display detailed error trace
 * <p>
 * "-e" specifies a file to compare if equal to
 * <p>
 * "-esf" export single SBML file
 * <p>
 * "-f" fail on first error
 * <p>
 * "-i" allow SBOL document to be incomplete
 * <p>
 * "-l" indicates the language for output (default=SBOL2, other options SBOL1, GenBank, FASTA, SBML)
 * <p>
 * "-mf" The name of the file that will be produced to hold the result of the main SBOL file, if SBOL file diff was selected.
 * <p>
 * "-n" allow non-compliant URIs
 * <p>
 * "-no" indicate no output file to be generated from validation
 * <p>
 * "-o" specifies an output filename
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
		System.err.println("\tjava -jar iBioSim-conversion-0.0.1-SNAPSHOT-jar-with-dependencies.jar [options] inputFile ");
		System.err.println();
		System.err.println("Convert SBOL to SBML Example:");
		System.err.println("\tjava -jar iBioSim-conversion-0.0.1-SNAPSHOT-jar-with-dependencies.jar -l SBML -p <SBOL default URI prefix> <inputFile> -oDir <outputDirectory> -o <outputFileName> ");
		System.err.println();
		System.err.println("Required:");
		System.err.println("<inputFile> name of input file");
		System.err.println();
		System.err.println("Options:");
		System.err.println("\t-b  check best practices");
		System.err.println("\t-cf The name of the file that will be produced to hold the result of the second SBOL file, if SBOL file diff was selected.");
		System.err.println("\t-d  display detailed error trace");
		System.err.println("\t-e  The second SBOL file to compare to the main SBOL file.");
		System.err.println("\t-f  continue after first error");
		System.err.println("\t-i  allow SBOL document to be incomplete");
		System.err.println("\t-l  <language> specifies language (SBOL1/SBOL2/GenBank/FASTA/SBML) for output (default=SBOL2)");
		System.err.println("\t-mf The name of the file that will be produced to hold the result of the main SBOL file, if SBOL file diff was selected.");
		System.err.println("\t-n  allow non-compliant URIs");
		System.err.println("\t-o  <outputFile> specifies the output file produced from the converter");
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

	public static void main(String[] args) 
	{
		//-----REQUIRED FIELD-----
		String fileName = ""; //input file name

		//-----OPTIONAL FIELD-----
		boolean bestPractice = false; //-b
		String compFileResult = ""; //-cf
		boolean showDetail = false; //-d
		String compareFile = ""; //-e
		boolean singleSBMLOutput = false; //-esf;
		boolean keepGoing = true; //-f
		boolean complete = true; //-i
		boolean genBankOut = false; //-l
		boolean fastaOut = false; //-l
		boolean sbolV1out = false; //-l
		boolean sbmlOut = false; //-l
		String mainFileResult = ""; //-mf
		boolean compliant = true; //-n
		boolean noOutput = false; //-no
		String outputFileName = ""; //-o
		String outputDir = ""; //-oDir
		String URIPrefix = ""; //-p
		String externalSBMLPath = ""; //-rsbml
		String externalSBOLPath = ""; //-rsbol
		HashSet<String> ref_sbolInputFilePath = new HashSet<String>(); //rsbol
		String topLevelURIStr = ""; //-s
		boolean typesInURI = false; //-t
		String version = null; //-v

		boolean isDiffFile = false;

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
			default:
				fileName = args[index];
			}
		}

		/* Note: Check all required field has been set. If not, stop user from continuing.*/
		boolean inputIsSBOL = false; 
		boolean inputIsSBML = false;
		if(!fileName.isEmpty())
		{
			//find out what input file format is
			try {
				if(isSBMLFile(fileName))
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
			System.err.println("You must provide the full input file path as this is a required filed.");
			usage();
			return;
		}

		/* Note: Initialize any optional variables that are required for conversion, validation, or file diff.*/
		if(externalSBOLPath != null && !externalSBOLPath.isEmpty())
		{
			//Note: this is an optional field. User provided sbol path to read in
			File fileDir = new File(externalSBOLPath);
			File[] sbolFiles =  fileDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (name.toLowerCase().endsWith(".rdf") || name.toLowerCase().endsWith(".sbol"));
				}
			});

			for(File f : sbolFiles)
			{
				ref_sbolInputFilePath.add(f.getAbsolutePath());
			}
		}

		File file = new File(fileName); 
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
					if(!URIPrefix.isEmpty())
					{
						if (externalSBMLPath.isEmpty()) 
						{
							//SBML file is relative. No external path was given for the input SBML file. 
							inputSBMLDoc = SBMLutilities.readSBML(fileName);
						} 
						else 
						{
							inputSBMLDoc = SBMLutilities.readSBML(externalSBMLPath + GlobalConstants.separator + fileName);
						}

						SBML2SBOL.convert_SBML2SBOL(outSBOLDoc, externalSBMLPath, inputSBMLDoc, fileName, ref_sbolInputFilePath, URIPrefix); 
					} 
					else
					{
						System.err.println("ERROR: You must provide an SBOL URI prefix in order to convert SBML to SBOL.");
						usage();
					}

					if(!noOutput)
					{
						if(outputFileName.isEmpty())
						{
							System.err.println("ERROR: You must provide an output file name to convert SBML to SBOL.");
							usage();
						}
						if(outputDir.isEmpty())
						{
							System.err.println("ERROR: You must provide an output directory to store the SBOL file after SBML to SBOL conversion is performed.");
							usage();
						}
						outSBOLDoc.write(outputDir + outputFileName, SBOLDocument.RDF);
					}
					else
					{
						outSBOLDoc.write(new ByteArrayOutputStream());
					}
					String sbolVal_fileName = outputDir + outputFileName;
					String sbolVal_outFileName = outputDir + outputFileName + "_validated";
					org.sbolstandard.core2.SBOLValidate.validate(sbolVal_fileName, URIPrefix, complete, compliant, bestPractice, typesInURI, 
							version, keepGoing, compareFile, compFileResult, mainFileResult, 
							topLevelURIStr, genBankOut, sbolV1out, fastaOut, sbolVal_outFileName, 
							showDetail, noOutput);
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
			} //end of is input is SBML
			else if(inputIsSBOL)
			{
				String sbolVal_outFileName = outputDir + outputFileName;
				//Perform validation on the input SBOL file if no SBML output file is expected
				org.sbolstandard.core2.SBOLValidate.validate(fileName, URIPrefix, complete, compliant, bestPractice, typesInURI, 
						version, keepGoing, compareFile, compFileResult, mainFileResult, 
						topLevelURIStr, genBankOut, sbolV1out, fastaOut, sbolVal_outFileName, 
						showDetail, noOutput);
				//If the user only want to diff between two SBOL file, call the validation method and then skip the rest. 
				if(!isDiffFile)
				{
					SBOLDocument sbolDoc;
					try 
					{
						SBOLReader.setURIPrefix(URIPrefix);
						sbolDoc = SBOLReader.read(new FileInputStream(fileName));
						if(!topLevelURIStr.isEmpty())
						{
							ModuleDefinition topModuleDef = sbolDoc.getModuleDefinition(URI.create(topLevelURIStr));
							List<BioModel> models = SBOL2SBML.generateModel(outputDir, topModuleDef, sbolDoc);
							saveSBMLModels(models, outputDir, outputFileName, noOutput, sbmlOut, singleSBMLOutput);
						} 
						else
						{
							//No ModuleDefinition URI provided so loop over all rootModuleDefinition
							for (ModuleDefinition moduleDef : sbolDoc.getRootModuleDefinitions())
							{
								List<BioModel> models = SBOL2SBML.generateModel(outputDir, moduleDef, sbolDoc);
								saveSBMLModels(models, outputDir, outputFileName, noOutput, sbmlOut, singleSBMLOutput);
							} 
						}
					}
					catch (FileNotFoundException e) {
						System.err.println("ERROR:  Unable to located file");
						e.printStackTrace();
					} catch (SBOLValidationException e) {
						System.err.println("ERROR: Invalid SBOL file");
						e.printStackTrace();
					} catch (IOException e) {
						System.err.println("ERROR: Unable to read or write file");
						e.printStackTrace();
					} catch (SBOLConversionException e) {
						System.err.println("ERROR: Unable to perform SBOL conversion");
						e.printStackTrace();
					} catch (XMLStreamException e) {
						System.err.println("ERROR: Invalid XML file");
						e.printStackTrace();
					} //end of last catch
				}
			} //end of isSBOL input
		}//end of is not a directory check
		else
		{
			for (File eachFile : file.listFiles()) {
				// TODO: should allow compare to a directory of same named files
				org.sbolstandard.core2.SBOLValidate.validate(eachFile.getAbsolutePath(), URIPrefix, complete, compliant, bestPractice, typesInURI, 
						version, keepGoing, compareFile, compFileResult, mainFileResult, 
						topLevelURIStr, genBankOut, sbolV1out, fastaOut, outputFileName, 
						showDetail, noOutput);
			}
		}
	} // end of method


	/**
	 * Check if the given file is an SBML file.
	 * @param file - The file to check if it is a valid SBML file.
	 * @return True if it is an SBML file. False otherwise. 
	 * @throws IOException - Unable to read file.
	 */
	private static boolean isSBMLFile(String file) throws IOException
	{
		BufferedReader b = new BufferedReader( new FileReader(file));

		String xmlComment =  "!--";
		String xmlProlog = "?xml";
		String sbmlText = "sbml";

		/* NOTE: Becuse we are reading the xml file as a general file, the BufferedReader will not understand xml syntax.
		 * Therefore, we must account for corner cases:
		 * - there can be new lines in arbitrary places.
		 * - there can be comments before reaching the root node.
		 */
		int c;
		StringBuilder builder = null;
		while((c = b.read()) >= 0)
		{
			if(c == '<')
			{
				builder = new StringBuilder();
			}
			else if(c == '>')
			{
				String currentElement = builder.toString();
				if(currentElement.startsWith(xmlComment)){
					continue;
				}
				else if(currentElement.startsWith(xmlProlog)){
					continue;
				}
				else if(currentElement.startsWith(sbmlText)){
					return true;
				}
				else
				{
					//No need to check for SBOL text. 
					//We will assume user gave SBOL file at this point.
					return false;
				}
			}
			else if(c =='\n')
			{
				continue;
			}
			else
			{
				builder.append((char) c);
			}
		}
		return false;
	}
	
	private static void saveSBMLModels(List<BioModel> models, String outputDir, String outputFileName, 
			boolean noOutput, boolean sbmlOut, boolean singleSBMLOutput)
	{
		// Note: Since SBOL2SBML converter encase the result of SBML model in BioModels, the last biomodel 
		// given from the converter is the top level model. All submodels belonging to the top level models are nested in side this last biomodel
		BioModel target = models.get(models.size() - 1);

		if(noOutput)
		{
			try 
			{
				SBMLWriter.write(target.getSBMLDocument(), System.out, ' ', (short) 4);
			} 
			catch (SBMLException e) 
			{
				System.err.println("ERROR: SBML Exception occurred when exporting BioModels to an SBML file");
				e.printStackTrace();
			} 
			catch (XMLStreamException e) 
			{
				System.err.println("ERROR: Invalid XML file");
				e.printStackTrace();
			}
		}
		else
		{
			if(sbmlOut)
			{
				exportMultSBMLFile(models, outputDir);
				if(singleSBMLOutput)
				{
					exportSingleSBMLFile(target, outputDir, outputFileName);
				}
			}
		}
	}
	
	private static void exportSingleSBMLFile(BioModel target, String outputDir, String outputFileName)
	{
		try 
		{
			target.exportSingleFile(outputDir + File.separator + outputFileName + ".xml"); 
			File fileDir = new File(outputDir);
			File[] files = fileDir.listFiles();
			for(File f : files)
			{
				if(f.isFile())
				{
					String fileName = f.getName();
					if(fileName.endsWith(".xml") && !fileName.equals(outputFileName + ".xml"))
					{
						f.delete();
					}
				}
			}
		} 
		catch (XMLStreamException e) 
		{
			System.err.println("ERROR: Invalid XML file.");
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			System.err.println("ERROR: Unable to write file to SBML.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Export a list of BioModels to the specified output directory.
	 * @param models - The list of BioModels
	 * @param outputDir - The output directory to store the SBML files
	 */
	private static void exportMultSBMLFile(List<BioModel> models, String outputDir)
	{
		//Multiple SBML output
		for (BioModel model : models)
		{
			try {
				model.save(outputDir + File.separator + model.getSBMLDocument().getModel().getId() + ".xml");
			} catch (XMLStreamException e) {
				System.err.println("ERROR: Invalid XML file");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("ERROR: Unable to write file to SBML.");
				e.printStackTrace();
			}
		}
		
		
	}

}
