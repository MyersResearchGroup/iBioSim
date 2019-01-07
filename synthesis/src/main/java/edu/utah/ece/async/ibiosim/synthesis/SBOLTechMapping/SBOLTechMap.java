package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class SBOLTechMap {


	public static void main(String[] args) 
	{

		try {
			CommandLine cmd = parseCommandLine(args);
			SBOLTechMapOptions techMapOptions = createTechMapOptions(cmd);
			Synthesis syn = new Synthesis();
			Map<SynthesisNode, SBOLGraph> solution = SBOLTechMap.runSBOLTechMap(syn, techMapOptions.getSpeficationFile(), techMapOptions.getLibraryFile());
			SBOLDocument sbol_solution = syn.getSBOLfromTechMapping(solution, syn.getSpecification());
			
			String outputPath = techMapOptions.getOutputFileDir() + File.separator + techMapOptions.getOuputFileName();
			//print something to terminal
			if(techMapOptions.printCoveredGates()) {
				syn.printCoveredGates(solution);
			}
			
			//export to local machine
			if(techMapOptions.isOutputDOT()) {
				syn.getSpecification().createDotFile(outputPath);
			}
			else {
				syn.exportAsSBOLFile(outputPath, sbol_solution);
			}
		} 
		catch (ParseException e) {
			System.err.println("Command-line ERROR: " + e.getMessage());
			return;
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found ERROR: " + e.getMessage());
			e.printStackTrace();
		} catch (SBOLException e) {
			System.err.println("SBOL Data Model ERROR: " + e.getMessage());
			e.printStackTrace();
		} catch (SBOLValidationException e) {
			System.err.println("SBOLValidation ERROR: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("SBOLReader ERROR: " + e.getMessage());
			e.printStackTrace();
		} catch (SBOLConversionException e) {
			System.err.println("SBOLConversion ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static SBOLTechMapOptions createTechMapOptions(CommandLine cmd) { 
		SBOLTechMapOptions techMapOptions = new SBOLTechMapOptions();
		
		if(cmd.hasOption("h")) {
			printUsage();
		}
		if(cmd.hasOption("sf")){
			techMapOptions.setSpecificationFile(cmd.getOptionValue("sf"));
		}
		if(cmd.hasOption("lf")) {
			techMapOptions.setLibraryFile(cmd.getOptionValue("lf"));
		}
		if(cmd.hasOption("o")) {
			techMapOptions.setOutputFileName(cmd.getOptionValue("o"));
		}
		if(cmd.hasOption("od")) {
			techMapOptions.setOutputDirectory(cmd.getOptionValue("od"));
		}
		if(cmd.hasOption("dot")) {
			techMapOptions.setOutputDotFile(true);
		}
		if(cmd.hasOption("sbol")) {
			techMapOptions.setOutputSBOL(true);
		}
		if(cmd.hasOption("pg")) {
			techMapOptions.setPrintToTerminalCoveredGates(true);
		}
		
		return techMapOptions;
	}
	
	private static Options getCommandLineOptions() {
		Options techMapOptions = new Options();
		techMapOptions.addOption("h", "help", false, "show the available command line needed to run this application.");
		techMapOptions.addOption("sf", "specification", true, "An SBOL file describing the design specification");
		techMapOptions.addOption("lf", "library", true, "An SBOL file or a directory to the SBOL files describing the library of gates used for technology mapping");
		techMapOptions.addOption("o", "outFileName", true, "Name of output file"); 
		techMapOptions.addOption("od", "odir", true, "Path of output directory where the technology mapper will produce the results to.");
		techMapOptions.addOption("dot", false, "Export solution into a dot file");
		techMapOptions.addOption("sbol", false, "Export solution into SBOL");
		techMapOptions.addOption("pg", "printGatesCovered", false, "Print name of gates that were selected for the technology mapping solution");
		
		return techMapOptions;
	}
	
	public static CommandLine parseCommandLine(String[] args) throws org.apache.commons.cli.ParseException {
		Options cmd_options = getCommandLineOptions();
		CommandLineParser cmd_parser = new DefaultParser();
		CommandLine cmd = cmd_parser.parse(cmd_options, args);
		return cmd;
	}
	
	public static void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		
		String usage = "-sf mySpec.xml -lf myLib.xml -od myDir -o result \n";
		
		formatter.printHelp(125, usage, "SBOL Technology Mapping Commands", 
				getCommandLineOptions(), "");

	}


	public static Map<SynthesisNode, SBOLGraph> runSBOLTechMap(Synthesis syn, SBOLDocument specDoc, SBOLDocument libDoc) throws SBOLValidationException, FileNotFoundException, SBOLException, IOException, SBOLConversionException
	{
		syn.createSBOLGraph(specDoc, false);
		syn.createSBOLGraph(libDoc, true);
		
		List<SBOLGraph> library = syn.getLibrary();
		syn.setLibraryGateScores(library);
		
		Map<SynthesisNode, LinkedList<WeightedGraph>> matches = new HashMap<SynthesisNode, LinkedList<WeightedGraph>>();
		syn.match_topLevel(syn.getSpecification(), matches);
		Map<SynthesisNode, SBOLGraph> solution = syn.cover_topLevel(syn.getSpecification(), matches);
		return solution;
	}

}
