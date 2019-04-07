package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * Main class to call the Verilog compiler and Verilog synthesizer.
 * The compiler will allow a user to produce SBML, SBOL, and LPN model9S) from a list of Verilog files.
 * The synthesizer will allow the user to produce an SBOL data model output.
 * 
 * @author Tramy Nguyen
 */
public class VerilogRunner {

	private static final char separator = ' ';
	
	public static void main(String[] args) {
		
		try {
			CommandLine cmd = parseCommandLine(args);
			CompilerOptions compilerOptions = createCompilerOptions(cmd);
			VerilogCompiler compiledVerilog = new VerilogCompiler(compilerOptions.getVerilogFiles()); 
			compiledVerilog.parseVerilog();
			compiledVerilog.compile(compilerOptions.isOutputFlatModel());
			
			String outputDirectory = compilerOptions.getOutputDirectory();
			
			if(compilerOptions.isGenerateSBOL()){
				compiledVerilog.exportSBOL(compiledVerilog.getMappedSBOLWrapper(), outputDirectory);
			}
			
			if(compilerOptions.isGenerateSBML()) {
				compiledVerilog.exportSBML(compiledVerilog.getMappedSBMLWrapper(), outputDirectory); 
				if(compilerOptions.isOutputFlatModel()) {
					SBMLDocument flattenDoc = compiledVerilog.flattenSBML(compilerOptions.getTestbenchModuleId(), compilerOptions.getOutputDirectory()); 
					compiledVerilog.exportSBML(flattenDoc, outputDirectory + File.separator + compilerOptions.getOutputFileName() + ".xml");
				}
			}
			
			if(compilerOptions.isGenerateLPN()){
				compiledVerilog.generateLPN(compilerOptions.getImplementationModuleId(), compilerOptions.getTestbenchModuleId(), outputDirectory);
				compiledVerilog.exportLPN(compiledVerilog.getLPN(), outputDirectory + File.separator + compilerOptions.getOutputFileName() + ".lpn"); 

			}
			
		} 
		catch (org.apache.commons.cli.ParseException e1) {
			printUsage();
			System.err.println("Command-line ERROR: " + e1.getMessage());
		}
		catch (ParseException e) {
			System.err.println("ParseException ERROR: Invalid format found when converting Verilog data to SBOL or SBML. " + e.getMessage());
			return;
		} 
		catch (XMLStreamException e) {
			System.err.println("XMLException ERROR: Unable to load or export an XML file. " + e.getMessage());
			return;
		} 
		catch (IOException e) {
			System.err.println("IOException ERROR: Unable to load or export a file. " + e.getMessage());
			return;
		} 
		catch (BioSimException e) {
			System.err.println("iBioSim Exception " + e.getMessage());
			return;
		} 
		catch (VerilogCompilerException e) {
			System.err.println("Verilog Compiler ERROR: " + e.getMessage());
			return;
		} 
		catch (SBOLValidationException e) {
			System.err.println("SBOL Validation ERROR: Invalid SBOL data was created when converting from Verilog to SBOL. " + e.getMessage());
			return;
		} catch (SBOLConversionException e) {
			System.err.println("SBOL Conversion ERROR: Unable to export SBOL data. " + e.getMessage());
			return;
		}
	}
	
	private static void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		
		String sbolUsage = "-v <arg(s)> -od usr/dir/example/ -sbol -flat \n";
		String sbmlUsage = "usage: -v <arg(s)> -od usr/dir/example/ -sbml \n";
		String lpnUsage = "usage: -v <arg(s)> -tb my_testbench -imp my_impCircuit -o example -od usr/dir/ -lpn \n";
		
		formatter.printHelp(125, sbolUsage + sbmlUsage + lpnUsage , 
				"VerilogCompiler Options", 
				getCommandLineOptions(), 
				"Note: <arg> is the user's input value.");

	}

	private static Options getCommandLineOptions() {
		Option verilogFiles = new Option("v", "verilogFiles",  true, "compile the given verilog file(s).");
		verilogFiles.setValueSeparator(separator);
		
		Options options = new Options();
		options.addOption(verilogFiles);
		options.addOption("h", "help", false, "show the available command line needed to run this application.");
		options.addOption("lpn", false, "Export result of the compiler to an LPN model."  +
				"Note that the LPN model produced in this compiler is limited to converting one implementation verilog file and its testbench file to LPN. " +
				"If the testbench file has more than one submodule instantiated, the user must specificy the name of the implementation verilog module and the name of the testbench verilog module for the compiler to produce a valid LPN model. " +
				"These fields should be set by specifying the -tb and -imp command.");
		options.addOption("sbml", false, "Export result of the compiler to SBML.");
		options.addOption("o", "outFileName", true, "Name of output file when exporting result of compiler to an LPN model.");
		options.addOption("od", "odir", true, "Path of output directory where the compiler will produce the results to.");
		options.addOption("tb", "tb_modId", true, "Name of the testbench verilog module identifier that simulates the design.");
		options.addOption("imp", "imp_modId", true, "Name of the implemenation verilog module identifier that describes the circuit");
		options.addOption("sbol", false, "Output data into SBOL.");
		options.addOption("flat", false, "Export data model as a flat model.");
		return options;
	}

	protected static CommandLine parseCommandLine(String[] args) throws org.apache.commons.cli.ParseException {
		Options cmd_options = getCommandLineOptions();
		CommandLineParser cmd_parser = new DefaultParser();
		CommandLine cmd = cmd_parser.parse(cmd_options, args);
		return cmd;
	}

	protected static CompilerOptions createCompilerOptions(CommandLine cmd) throws FileNotFoundException {
		CompilerOptions compilerOptions = new CompilerOptions();
		
		if(cmd.hasOption("h")) {
			printUsage();
		}
		if(cmd.hasOption("o")) {
			String fileName = cmd.getOptionValue("o");
			compilerOptions.setOutputFileName(fileName);
			compilerOptions.exportCompiler(true);
		}
		if(cmd.hasOption("od")) {
			String outputDirectory = cmd.getOptionValue("od");
			compilerOptions.setOutputDirectory(outputDirectory);
			compilerOptions.exportCompiler(true);
		}
		if(cmd.hasOption("tb")) {
			String testbenchName = cmd.getOptionValue("tb");
			compilerOptions.setTestbenchModuleId(testbenchName);
		}
		if(cmd.hasOption("imp")) {
			String implementationName = cmd.getOptionValue("imp");
			compilerOptions.setImplementationModuleId(implementationName);
		}
		if(cmd.hasOption("lpn")) {
			compilerOptions.setGenerateLPN(true);
		}
		if(cmd.hasOption("sbml")) {
			compilerOptions.setGenerateSBML(true);
		}
		if(cmd.hasOption("sbol")) {
			compilerOptions.setGenerateSBOL(true); 
		}
		if(cmd.hasOption("v")) {
			String[] files = cmd.getOptionValue("v").split(String.valueOf(separator));
			for(String file : files) {
				compilerOptions.addVerilogFile(file);
			}
		}
		if(cmd.hasOption("flat")) {
			compilerOptions.setFlatModel(true);
		}
		return compilerOptions;
	}


}