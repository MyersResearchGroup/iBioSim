package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;

public class SBOLTechMap {

	public static void main(String[] args) 
	{
		
		String libFile = "";
		String multLibFiles = "";
		String specFile = "";
		String outFileName = "";
		String defaultPrefix = "";
		
		boolean greedySol = false;
		boolean exactSol = false;
		boolean createDotFile = false;
		
		int index = 0;

		for(; index< args.length; index++)
		{
			String flag = args[index];
			switch(flag)
			{
			case "-lf":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				libFile = args[++index];
				break;
			case "-mlf":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				multLibFiles = args[++index];
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
			SBOLDocument libDoc = null;
			if(!libFile.isEmpty() && multLibFiles.isEmpty())
			{
				libDoc = SBOLUtility.loadSBOLFile(libFile, defaultPrefix);
			}
			else if(libFile.isEmpty() && !multLibFiles.isEmpty())
			{
				libDoc = SBOLUtility.loadFromDir(multLibFiles, defaultPrefix);
			}
			else if(!libFile.isEmpty() && !multLibFiles.isEmpty())
			{
				System.err.println("ERROR: At least one SBOL library file must provided to perform SBOL Technology Mapping.");
				usage();
			}
			else
			{
				System.err.println("ERROR: Both flags (-lf and -mlf) are not allowed to be turned on at the same time.");
				usage();
			}
			
			SBOLDocument specDoc = SBOLUtility.loadSBOLFile(specFile, defaultPrefix);
			
			SBOLDocument solution = runSBOLTechMap(specDoc, libDoc);
			solution.write(new File(outFileName));
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
		catch (SBOLException e) 
		{
			System.err.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static void usage() 
	{
		System.err.println("Required:");
		System.err.println("-lf full path to the library file containing set of genetic gates needed for technology mapping.");
		System.err.println("-sf full path to the specification file to performing technology mapping.");
		
		System.err.println("Options:");
		System.err.println("-o name of output file along with full path of where output file will be written to.");
		System.err.println("-p SBOL URI prefix needed to set the SBOLDocument when converting the technology mapping solution to SBOL data format.");
		
	}
	
	public static SBOLDocument runSBOLTechMap(SBOLDocument specDoc, SBOLDocument libDoc) throws SBOLValidationException, FileNotFoundException, SBOLException, IOException, SBOLConversionException
	{
		Synthesis syn = new Synthesis();
		syn.createSBOLGraph(specDoc, false);
		syn.createSBOLGraph(libDoc, true);
		
		List<SBOLGraph> library = syn.getLibrary();
		syn.setLibraryGateScores(library);
		
		Map<SynthesisNode, LinkedList<WeightedGraph>> matches = new HashMap<SynthesisNode, LinkedList<WeightedGraph>>();
		syn.match_topLevel(syn.getSpecification(), matches);
		Map<SynthesisNode, SBOLGraph> solution = syn.cover_topLevel(syn.getSpecification(), matches);
		
		return syn.getSBOLfromTechMapping(solution, syn.getSpecification(), GlobalConstants.SBOL_AUTHORITY_DEFAULT);
	}

}
