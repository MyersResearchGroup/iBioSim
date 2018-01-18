package edu.utah.ece.async.ibiosim.synthesis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.xml.stream.XMLStreamException;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLFileManager;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.SBMLTechMapping.SynthesisGraph;
import edu.utah.ece.async.ibiosim.synthesis.SBMLTechMapping.Synthesizer;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMap;

public class TechMapping {

	public static void main(String[] args) 
	{
		String sbolLibDir = "";
		String sbmlLibDir = "";
		String libDirectory = "";
		String specFile = "";
		String outFileName = "";
		String defaultPrefix = "";

		boolean sbml = false;
		boolean sbol = false;
		boolean outSBML = false;
		boolean outSBOL = false;


		boolean greedySol = false;
		boolean exactSol = false;
		boolean createDotFile = false;

		int index = 0;

		for(; index< args.length; index++)
		{
			String flag = args[index];
			switch(flag)
			{
			case "-sbolLD":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				sbolLibDir = args[++index];
				break;
			case "-sbmlLD":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				sbmlLibDir = args[++index];
				break;
			case "-ld":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				libDirectory = args[++index];
				break;
			case "-sf":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				specFile = args[++index];
				break;
			case "-o":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				outFileName = args[++index];
				break;
			case "-sbml":
				sbml = true;
				break;
			case "-sbol":
				sbml = true;
				break;
			case "-osbml":
				sbml = true;
				break;
			case "-osbol":
				sbml = true;
				break;
			case "-p":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				defaultPrefix = args[++index];
				break;
			case "-dot":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				createDotFile = true;
				break;
			default:
				usage();
			}
		}


		try 
		{
			if(sbol)
			{
				SBOLDocument specDoc = SBOLUtility.loadSBOLFile(specFile, defaultPrefix);
				SBOLDocument libDoc = SBOLUtility.loadSBOLFile(sbolLibDir, defaultPrefix);
				
				SBOLDocument sbolDoc_sol = SBOLTechMap.runSBOLTechMap(specDoc, libDoc);
				sbolDoc_sol.write(new File(outFileName));
			}
			else if (sbml)
			{
				Set<String> sbolLibFiles = new HashSet<String>();
				for(String sbolGate : new File(sbolLibDir).list())
				{
					sbolLibFiles.add(sbolGate);
				}
				SBOLFileManager fileManager = new SBOLFileManager(sbolLibFiles, defaultPrefix);
				
				// Create a gate graph for each SBML library file that was read in
				Set<SynthesisGraph> graphlibrary = new HashSet<SynthesisGraph>();

				for (String gateFileID : new File(sbmlLibDir).list()) 
				{
					BioModel gateModel = new BioModel(libDirectory);
					try 
					{
						gateModel.load(gateFileID);
					} 
					catch (XMLStreamException e) 
					{
						System.err.println("ERROR: Unable to load SBML file(s) needed for library gates.");
						e.printStackTrace();
					} 
					catch (BioSimException e) 
					{
						System.err.println("ERROR: " + e.getMessage());
						e.printStackTrace();
					}
					graphlibrary.add(new SynthesisGraph(gateModel, fileManager));
				}

				String specFileName = new File(specFile).getName();
				BioModel specModel = new BioModel(specFile); 
				SynthesisGraph spec = new SynthesisGraph(specModel, fileManager);
				
				Properties synthProps = createDefaultSynthesisProperties(specFileName);
				Synthesizer synthesizer = new Synthesizer(graphlibrary, synthProps);
				List<List<SynthesisGraph>> solutions = synthesizer.mapSpecification(spec);
				List<String> solutionFileIDs;
			}
		} 
		catch (FileNotFoundException e) 
		{
			System.err.println("ERROR: Unable to locate input file(s).");
			e.printStackTrace();
		} 
		catch (SBOLException e) 
		{
			System.err.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		} 
		catch (SBOLValidationException e) 
		{
			System.err.println("ERROR: Failed SBOL Validation when converting SBOL Tech. Map solution into SBOL data model.");
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			System.err.println("ERROR: Unable to read or write SBOL file");
			e.printStackTrace();
		} 
		catch (SBOLConversionException e) 
		{
			System.err.println("ERROR: Unable to convert input file to SBOL data format.");
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Get default fields to set the Synthesis View panel by storing the fields in a property file.
	 * @param specFileID - ID of specification file
	 * @return 
	 */
	public static Properties createDefaultSynthesisProperties(String specFileID) 
	{
		Properties synthProps = new Properties();
		Preferences prefs = Preferences.userRoot();
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY, specFileID);
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY, 
				prefs.get(GlobalConstants.SBOL_SYNTH_LIBS_PREFERENCE, ""));
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY,
				prefs.get(GlobalConstants.SBOL_SYNTH_METHOD_PREFERENCE, 
						GlobalConstants.SBOL_SYNTH_EXHAUST_BB));
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY, 
				prefs.get(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PREFERENCE, "1"));
		return synthProps;
	}
	
	public static void exportSBMLTechMap(List<List<SynthesisGraph>> solutions, SynthesisGraph spec, 
			SBOLFileManager fileManager, String synthFilePath)
	{
		
	}

	private static void usage() 
	{
		System.err.println("Required:");
		System.err.println("-lf full path to the library file containing set of genetic gates needed for technology mapping.");
		System.err.println("-sf full path to the specification file to performing technology mapping.");
		System.err.println("-sbml perform SBML Technology Mapping");
		System.err.println("-sbol perform SBOL Technology Mapping");
		System.err.println("-o name of output file along with full path of where output file will be written to.");
		System.err.println("-osbml produce solution for technology mapping in SBML format");
		System.err.println("-osbol produce solution for technology mapping in SBOL format");

		System.err.println("Options:");
		System.err.println("-p SBOL URI prefix needed to set the SBOLDocument when converting the technology mapping solution to the desired SBOL or SBML data format.");
		System.err.println("-ld: directory to multiple SBOL or SBML library files");
		System.err.println("-dot produced SBOL technology mapping solution in dot format.");
	}
	
	

}
