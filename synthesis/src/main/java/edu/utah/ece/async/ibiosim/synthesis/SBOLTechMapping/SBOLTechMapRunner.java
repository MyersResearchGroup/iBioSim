package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;

/**
 * Main class for running technology mapping. 
 * @author Tramy Nguyen
 *
 */
public class SBOLTechMapRunner {

	private static final char separator = ' ';
	
	public static void main(String[] args) 
	{
		CommandLine cmd;
		try {
			cmd = parseCommandLine(args);
			SBOLTechMapOptions techMapOptions = createTechMapOptions(cmd);
			
			List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(techMapOptions.getLibrary());
			System.out.println("Library Size: " + libGraph.size());
			DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(techMapOptions.getSpefication());
			
			if(techMapOptions.listSpecNodeIds()) {
				int counter = 0;
				for(DecomposedGraphNode node : specGraph.getAllNodes()) {
					String nType = "Node";
					if(specGraph.isLeaf(node)) {
						nType = "Leaf";
					}
					else if(specGraph.isRoot(node)) {
						nType = "Root";
					}
					
					System.out.println(nType + ++counter + ": " + node.toString());
				}
			}
			if(techMapOptions.hasPreselection()) {
				TechMapUtility.addPreAssignComponentDefinition(specGraph, techMapOptions.getPreselection());
			}
			
			System.out.println("Tech. Map Started");
			System.out.println("Performing Matching Step");
			Match m = new PreSelectedMatch(specGraph, libGraph);
			
			System.out.println("Performing Covering Step");
			Cover c = new Cover(m);
			
			String outDir = techMapOptions.getOutputDir() + File.separator;
			if(techMapOptions.isExhaustive()) {
				List<TechMapSolution> coverSols = c.exhaustiveCover();
				System.out.println(m.getGateList(specGraph.getRootNode()).size() + " mSize");
				System.out.println(coverSols.size() + " Solutions found for Exhaustive");
				int count = 1;
				for(TechMapSolution eSol : coverSols) {
					if(eSol.getScore() != 0.0) {
						SBOLNetList sbolSol = new SBOLNetList(specGraph, eSol);
						SBOLDocument result = sbolSol.generateSbol();
						SBOLWriter.write(result, outDir + specGraph.getGraphId() + "Sol" + count++ + ".xml" );
					}
				}
			}
			else if(techMapOptions.isGreedy()) {
				if(techMapOptions.getNumOfSolutions() == 0) {
					System.out.println("Warning! The number of solution is 0.");
				}
				List<TechMapSolution> coverSols = c.greedyCover(techMapOptions.getNumOfSolutions());
				System.out.println(coverSols.size() + "/" + techMapOptions.getNumOfSolutions() + " Solutions found for Greedy");
				int count = 1;
				for(TechMapSolution gSol : coverSols) {
					if(gSol.getScore() != 0.0) {
						SBOLNetList sbolSol = new SBOLNetList(specGraph, gSol);
						SBOLDocument result = sbolSol.generateSbol();
						SBOLWriter.write(result, outDir + specGraph.getGraphId() + "Sol" + count++ + ".xml" );
					}
				}
			}
			else if(techMapOptions.isBranchBound()) {
				TechMapSolution bbSol = c.branchAndBoundCover();
				if(Double.isInfinite(bbSol.getScore())) {
					System.out.println("positive infinity detected");
				}
				else {
					System.out.println("Best Solution found!");
					SBOLNetList sbolSol = new SBOLNetList(specGraph, bbSol);
					SBOLDocument result = sbolSol.generateSbol();
					SBOLWriter.write(result, outDir + techMapOptions.getOuputFileName() + ".xml");
				}
			}
			System.out.println("Tech. map complete!");
		} 
		catch (ParseException e) {
			e.printStackTrace();
		} 
		catch (SBOLTechMapException e) {
			e.printStackTrace();
		} 
		catch (SBOLValidationException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (SBOLConversionException e) {
			e.printStackTrace();
		} 
		catch (GeneticGatesException e) {
			e.printStackTrace();
		} 
		catch (GateGenerationExeception e) {
			e.printStackTrace();
		}
	}
	
	private static SBOLTechMapOptions createTechMapOptions(CommandLine cmd) throws SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions techMapOptions = new SBOLTechMapOptions();
		
		if(cmd.hasOption("h")) {
			printUsage();
		}
		if(cmd.hasOption("s")){
			techMapOptions.setSpecificationFile(cmd.getOptionValue("s"));
		}
		if(cmd.hasOption("l")) {
			String[] files = cmd.getOptionValue("l").split(String.valueOf(separator));
			for(String file : files) {
				techMapOptions.addLibraryFile(file);
			}
		}
		if(cmd.hasOption("e")) {
			techMapOptions.setExhaustive(true);
		}
		if(cmd.hasOption("g")) {
			techMapOptions.setGreedy(true);
		}
		if(cmd.hasOption("bb")) {
			techMapOptions.setBranchBound(true);
		}
		if(cmd.hasOption("nsol")) {
			int value = Integer.parseInt(cmd.getOptionValue("nsol"));
			techMapOptions.setNumOfSolutions(value);
		}
		if(cmd.hasOption("id")) {
			techMapOptions.setSpecNodeId();
		}
		if(cmd.hasOption("o")) {
			techMapOptions.setOutputFileName(cmd.getOptionValue("o"));
		}
		if(cmd.hasOption("od")) {
			techMapOptions.setOutputDirectory(cmd.getOptionValue("od"));
		}
		if(cmd.hasOption("sbol")) {
			techMapOptions.setOutputSBOL(true);
		}
		if(cmd.hasOption("ps")) {
			String[] preselection = cmd.getOptionValue("ps").split(String.valueOf(separator));
			int index = 0;
			for( ; index < preselection.length; index++) {
				if(index+1 < preselection.length) {
					techMapOptions.addPreselection(preselection[index], preselection[++index]);
				}
			}
		}
		
		return techMapOptions;
	}
	
	private static Options getCommandLineOptions() {
		Option libFiles = new Option("l", "library", true, "A list of files or a directory of genetic gates used for technology mapping");
		libFiles.setValueSeparator(separator);
		Option preselect = new Option("ps",  "preselect", true, "Select nodes in the specification and assign with a ComponentDefinition URI.");
		preselect.setValueSeparator(separator);
		
		Options techMapOptions = new Options();
		techMapOptions.addOption("h", "help", false, "show the available command line needed to run this application.");
		techMapOptions.addOption("s", "specification", true, "A decomposed SBOL file describing the design specification");
		techMapOptions.addOption(libFiles);
		techMapOptions.addOption(preselect);
		techMapOptions.addOption("bb", "branchbound", false, "Perform branch and bound for covering"); 
		techMapOptions.addOption("e", "exhaustive", false, "Perform exhaustive for covering"); 
		techMapOptions.addOption("g", "greedy", false, "Perform greedy for covering"); 
		techMapOptions.addOption("nsol", "numOfSol", true, "Number of solution"); 
		techMapOptions.addOption("o", "outFileName", true, "Name of output file"); 
		techMapOptions.addOption("od", "odir", true, "Path of output directory where the technology mapper will produce the results to.");
		techMapOptions.addOption("sbol", false, "Export solution into SBOL");
		techMapOptions.addOption("id", "nodeId", false, "List all specification node IDs.");
		return techMapOptions;
	}
	
	private static CommandLine parseCommandLine(String[] args) throws org.apache.commons.cli.ParseException {
		Options cmd_options = getCommandLineOptions();
		CommandLineParser cmd_parser = new DefaultParser();
		CommandLine cmd = cmd_parser.parse(cmd_options, args);
		return cmd;
	}
	
	private static void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		
		String usage = "-sf mySpec.xml -lf myLib.xml -od myDir -o result \n";
		
		formatter.printHelp(125, usage, "SBOL Technology Mapping Commands", 
				getCommandLineOptions(), "");

	}

	public static Synthesis run(SBOLDocument specDoc, SBOLDocument libDoc) throws SBOLValidationException, FileNotFoundException, SBOLException, IOException, SBOLConversionException, SBOLTechMapException
	{
		Synthesis syn = new Synthesis();
		return syn;
	}
	
}
