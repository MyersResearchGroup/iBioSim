package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.sbml.jsbml.SBMLDocument;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class CompilerOptions_Tests{

	private VerilogCompiler runCompiler(String[] args) throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException, org.sbml.jsbml.text.parser.ParseException {
		CommandLine cmds = VerilogRunner.parseCommandLine(args);
		CompilerOptions setupOpt = VerilogRunner.createCompilerOptions(cmds);
		VerilogCompiler compiledVerilog = VerilogRunner.compile(setupOpt.getVerilogFiles());
		
		if(setupOpt.isGenerateSBOL() || setupOpt.isGenerateSBML() || setupOpt.isGenerateLPN()) {
			compiledVerilog.compileVerilogOutputData(setupOpt.isOutputFlatModel());
			if(setupOpt.isGenerateLPN()){
				compiledVerilog.generateLPN(setupOpt.getImplementationModuleId(), setupOpt.getTestbenchModuleId(), setupOpt.getOutputDirectory());
			}
		}

		if(setupOpt.isExportOn()) {
			String outputDirectory = setupOpt.getOutputDirectory();
			if(setupOpt.isGenerateSBOL()){
				compiledVerilog.exportSBOL(compiledVerilog.getMappedSBOLWrapper(), outputDirectory);
			}
			
			if(setupOpt.isGenerateSBML()) {
				compiledVerilog.exportSBML(compiledVerilog.getMappedSBMLWrapper(), outputDirectory); 
				if(setupOpt.isOutputFlatModel()) {
					SBMLDocument flattenDoc = compiledVerilog.flattenSBML(outputDirectory, outputDirectory); 
					compiledVerilog.exportSBML(flattenDoc, outputDirectory + setupOpt.getOutputFileName() + "_flattened.xml");
				}
			}
			
			if(setupOpt.isGenerateLPN()){
				compiledVerilog.generateLPN(setupOpt.getImplementationModuleId(), setupOpt.getTestbenchModuleId(), outputDirectory);
				compiledVerilog.exportLPN(compiledVerilog.getLPN(), outputDirectory + File.separator + setupOpt.getOutputFileName()); 

			}
		}
		return compiledVerilog;
	}
	
	@Test
	public void Test_inputSize() throws ParseException, FileNotFoundException {
		String[] args = {"-v", TestingFiles.verilogInitBlock_file};
		
		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		CompilerOptions compilerOptions = VerilogRunner.createCompilerOptions(cmd);
		Assert.assertEquals(1, compilerOptions.getVerilogFiles().size());
	}
	
	@Test
	public void Test_inputSize2() throws ParseException, FileNotFoundException {
		String files = String.join(" ", TestingFiles.verilogInitBlock_file, TestingFiles.verilogInitBlock_file);
		String[] args = {"-v", files};
		
		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		CompilerOptions compilerOptions = VerilogRunner.createCompilerOptions(cmd);
		Assert.assertEquals(2, compilerOptions.getVerilogFiles().size());
	}
	
	@Test
	public void Test_inputSize3() throws ParseException, FileNotFoundException {
		String files = String.join(" ", TestingFiles.verilogInitBlock_file, TestingFiles.verilogAssign_file, TestingFiles.verilogDelay_file);
		String[] args = {"-v", files};
		
		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		CompilerOptions compilerOptions = VerilogRunner.createCompilerOptions(cmd);
		Assert.assertEquals(3, compilerOptions.getVerilogFiles().size());
	}
	
	@Test(expected = org.apache.commons.cli.ParseException.class)
	public void Test_PaserException() throws ParseException {
		String[] args = {"-v"};
		VerilogRunner.parseCommandLine(args);
	}
	
	@Test(expected = FileNotFoundException.class)
	public void Test_FileNotFoundException() throws ParseException, FileNotFoundException {
		String[] args = {"-v", "a.v"};

		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		VerilogRunner.createCompilerOptions(cmd);
		
	}

	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException() throws org.sbml.jsbml.text.parser.ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, ParseException, SBOLConversionException { 
		String[] args = {"-v", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile, 
				"-lpn", "-od", TestingFiles.writeOutputDir, "-o", "evenzeroes"};
		//error because missing module identifier names for imp and tb
		runCompiler(args);
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException2( ) throws org.sbml.jsbml.text.parser.ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, ParseException, SBOLConversionException { 
		String files = String.join(" ", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile);
		
		String[] args = {"-v", files, 
				"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench",  
				"-lpn", "-od", TestingFiles.writeOutputDir};
		//error because missing output file name
		runCompiler(args); 
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException3() throws org.sbml.jsbml.text.parser.ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, ParseException, SBOLConversionException { 
		String files = String.join(" ", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile);
		String[] args = {"-v", files,
				"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench",  
				"-lpn"};
		//no output directory. Generating LPN needs the SBML files exported to a directory before sbml to lpn can be called. 
		runCompiler(args); 
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException4() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile, 
				"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench",  
				"-lpn", "-o", "evenzeroes"};
		//error because missing output directory
		runCompiler(args); 
	}
	
	@Test()
	public void Test_VerilogCompilerException5() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile, 
				"-sbml"};
		//no output directory
		runCompiler(args); 
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException6() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile, 
				"-imp", "evenzeroes_imp",  
				"-lpn", "-od", TestingFiles.writeOutputDir};
		//error because missing tb module identifier
		runCompiler(args); 
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException7() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile, 
				"-tb", "evenzeroes_testbench",
				"-lpn", "-od", TestingFiles.writeOutputDir};
		//error because missing imp module identifier
		runCompiler(args); 
	}
	
	@Test
	public void Test_SBML() throws ParseException, FileNotFoundException {
		String[] args = {"-v", TestingFiles.verilogInitBlock_file, "-sbml",
						"-od", TestingFiles.writeOutputDir};
		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		CompilerOptions compilerOptions = VerilogRunner.createCompilerOptions(cmd);
		Assert.assertTrue(compilerOptions.isExportOn());
		Assert.assertTrue(compilerOptions.isGenerateSBML());
		Assert.assertFalse(compilerOptions.isGenerateLPN());
		Assert.assertTrue(compilerOptions.isOutputDirectorySet());
		Assert.assertFalse(compilerOptions.isOutputFileNameSet());
		Assert.assertFalse(compilerOptions.isImplementatonModuleIdSet());
		Assert.assertFalse(compilerOptions.isTestbenchModuleIdSet());
	}
	
	@Test
	public void Test_LPN() throws ParseException, FileNotFoundException {
		String[] args = {"-v", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile, 
				"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench", 
				"-lpn", "-od", TestingFiles.writeOutputDir, "-o", "evenzeroes"};
		
		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		CompilerOptions compilerOptions = VerilogRunner.createCompilerOptions(cmd);
		Assert.assertTrue(compilerOptions.isExportOn());
		Assert.assertFalse(compilerOptions.isGenerateSBML());
		Assert.assertTrue(compilerOptions.isGenerateLPN());
		Assert.assertTrue(compilerOptions.isOutputDirectorySet());
		Assert.assertTrue(compilerOptions.isOutputFileNameSet());
		Assert.assertTrue(compilerOptions.isImplementatonModuleIdSet());
		Assert.assertTrue(compilerOptions.isTestbenchModuleIdSet());
	}
	
	@Test
	public void Test_verbose() throws ParseException, FileNotFoundException {
		String[] args = {"--verilogFiles", TestingFiles.verilogInitBlock_file};
		
		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		CompilerOptions compilerOptions = VerilogRunner.createCompilerOptions(cmd);
		Assert.assertEquals(1, compilerOptions.getVerilogFiles().size());
	}
	
	@Test
	public void Test_verbose2() throws ParseException, FileNotFoundException {
		String[] args = {"-sbml", "--odir", TestingFiles.writeOutputDir, "--verilogFiles", TestingFiles.verilogInitBlock_file};
		
		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		CompilerOptions compilerOptions = VerilogRunner.createCompilerOptions(cmd);
		Assert.assertEquals(1, compilerOptions.getVerilogFiles().size());
		Assert.assertTrue(compilerOptions.isExportOn());
		Assert.assertTrue(compilerOptions.isGenerateSBML());
		Assert.assertFalse(compilerOptions.isGenerateLPN());
		Assert.assertTrue(compilerOptions.isOutputDirectorySet());
		Assert.assertFalse(compilerOptions.isOutputFileNameSet());
		Assert.assertFalse(compilerOptions.isImplementatonModuleIdSet());
		Assert.assertFalse(compilerOptions.isTestbenchModuleIdSet());
	}

	@Test
	public void Test_help() throws ParseException, FileNotFoundException {
		String[] args = {"-h"};
		
		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		VerilogRunner.createCompilerOptions(cmd);
	}
}