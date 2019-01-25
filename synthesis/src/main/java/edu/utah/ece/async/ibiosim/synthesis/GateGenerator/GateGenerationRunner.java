package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

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
import org.virtualparts.VPRException;
import org.virtualparts.VPRTripleStoreException;

/**
 * Class to run Gate Generation.
 * @author Tramy Nguyen
 */
public class GateGenerationRunner {
	
	private static final char separator = ' ';
	public static void main(String[] args) {
		
		try {
			CommandLine cmd = parseCommandLine(args);
			GateGeneratorOptions setupOpt = createGateGenerationOptions(cmd);
			GateGeneration generator = run(setupOpt.getTUSBOLDocumentList(), setupOpt.getSelectedSBHRepo());
			String outDir = setupOpt.getOutputDirectory() + File.separator;
			if(setupOpt.outputNOTLibrary()) {
				SBOLDocument notLib = generator.getNOTLibrary();
				generator.exportLibrary(notLib, outDir + "NOTGates_LibrarySize" + notLib.getRootModuleDefinitions().size() + ".xml");
			}
			if(setupOpt.outputNORLibrary()) {
				SBOLDocument norLib = generator.getNORLibrary();
				generator.exportLibrary(norLib, outDir + "NORGates_LibrarySize" + norLib.getRootModuleDefinitions().size() + ".xml");
			}
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
		catch (VPRException e) {
			e.printStackTrace();
		} 
		catch (VPRTripleStoreException e) {
			e.printStackTrace();
		} 
		catch (GateGenerationExeception e) {
			e.printStackTrace();
		} 
		catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private static void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		String sbolUsage = "-f <arg(s)> -od usr/dir -";
		formatter.printHelp(125, sbolUsage, 
				"Gate Generation Options", 
				getCommandLineOptions(), 
				"Note: <arg> is the user's input value.");
	}

	private static Options getCommandLineOptions() {
		Option tuFiles = new Option("f", "SBOLFiles",  true, "List of SBOL files containing transcription unit designs to generate logic gates from.");
		tuFiles.setValueSeparator(separator);
		
		Options options = new Options();
		options.addOption(tuFiles);
		options.addOption("sbh", "sbhRepository", true, "Name of SynBioHub repository to obtain get interaction data from.");
		options.addOption("h", "help", false, "show the available command line needed to run this application.");
		options.addOption("o", "outFileName", true, "Name of output file");
		options.addOption("od", "odir", true, "Path of output directory where GateGeneration will produce the results to.");
		options.addOption("NOT", false, "Export all the available NOT gates");
		options.addOption("NOR", false, "Export all the available NOR gates");
		options.addOption("m", "merge", false, "Export selected logic gates into one SBOL file. If this flag is not turned on, each library of gates will be exported into separate files."); 
		return options;
	}

	protected static CommandLine parseCommandLine(String[] args) throws org.apache.commons.cli.ParseException {
		Options cmd_options = getCommandLineOptions();
		CommandLineParser cmd_parser = new DefaultParser();
		CommandLine cmd = cmd_parser.parse(cmd_options, args);
		return cmd;
	}

	protected static GateGeneratorOptions createGateGenerationOptions(CommandLine cmd) throws FileNotFoundException {
		GateGeneratorOptions gateGenOptions = new GateGeneratorOptions();
		if(cmd.hasOption("h")) {
			printUsage();
		}
		if(cmd.hasOption("o")) {
			String fileName = cmd.getOptionValue("o");
			gateGenOptions.setOutputFileName(fileName);
		}
		if(cmd.hasOption("od")) {
			String outputDirectory = cmd.getOptionValue("od");
			gateGenOptions.setOutputDirectory(outputDirectory);
		}
		if(cmd.hasOption("f")) {
			String[] files = cmd.getOptionValue("f").split(String.valueOf(separator));
			for(String file : files) {
				gateGenOptions.addTUFile(file);
			}
		}
		if(cmd.hasOption("sbh")) {
			String sbhRepo = cmd.getOptionValue("sbh");
			gateGenOptions.setSelectedSBHRepo(sbhRepo);
		}
		if(cmd.hasOption("m")) {
			gateGenOptions.setMergeOutputLibrary(true);
		}
		if(cmd.hasOption("NOT")) {
			gateGenOptions.setOutputNOTLibrary(true);
		}
		if(cmd.hasOption("NOR")) {
			gateGenOptions.setOutputNORLibrary(true);
		}
		return gateGenOptions;
	}
	
	public static GateGeneration run(List<SBOLDocument> transcriptionUnitList, String synbiohubRepository) throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException, GateGenerationExeception {
		GateGeneration generator = new GateGeneration();
		List<SBOLDocument> enrichedTU_List = generator.enrichedTU(transcriptionUnitList, synbiohubRepository);
		generator.sortEnrichedTUList(enrichedTU_List);
		return generator;
	}
}
