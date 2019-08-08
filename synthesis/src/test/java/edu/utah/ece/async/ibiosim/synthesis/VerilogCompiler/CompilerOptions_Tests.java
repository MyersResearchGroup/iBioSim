package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.lema.verification.lpn.LPN;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class CompilerOptions_Tests{

	private void runCompiler(String[] args) throws ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLConversionException, SBOLValidationException, org.sbml.jsbml.text.parser.ParseException {
		CommandLine cmds = VerilogRunner.parseCommandLine(args);
		CompilerOptions compilerOptions = VerilogRunner.createCompilerOptions(cmds);
		VerilogParser verilogParser = new VerilogParser();
		Map<String, VerilogModule> verilogModules = new HashMap<>();
		for(File file : compilerOptions.getVerilogFiles()) {
			VerilogModule verilogModule = verilogParser.parseVerilogFile(file);
			verilogModules.put(verilogModule.getModuleId(), verilogModule);
		}

		String outputDir = compilerOptions.getOutputDirectory();
		if(compilerOptions.isGenerateSBOL()){
			VerilogToSBOL sbolConverter = new VerilogToSBOL(compilerOptions.isOutputFlatModel());
			for (VerilogModule vModule : verilogModules.values()) {
				WrappedSBOL sbolResult = sbolConverter.convertVerilog2SBOL(vModule);
				SBOLWriter.write(sbolResult.getSBOLDocument(), outputDir + File.separator + vModule.getModuleId() + ".xml");
			}
		}
		else {
			if(compilerOptions.isGenerateSBML() && !compilerOptions.isOutputFlatModel()) {
				VerilogToSBML sbmlConverter = new VerilogToSBML(verilogModules);
				for (VerilogModule vModule : verilogModules.values()) {
					WrappedSBML sbmlResult = sbmlConverter.convertVerilogToSBML(vModule);
					SBMLWriter writer = new SBMLWriter();
					writer.writeSBMLToFile(sbmlResult.getSBMLDocument(), outputDir + File.separator + vModule.getModuleId() + ".xml");
				}
			}
			VerilogToLPNCompiler sbmlLPNConverter = new VerilogToLPNCompiler();

			for(File file : compilerOptions.getVerilogFiles()) {
				VerilogModule verilogModule = sbmlLPNConverter.parseVerilogFile(file);
				sbmlLPNConverter.generateSBMLFromVerilog(verilogModule);
			}
			if(compilerOptions.isGenerateSBML() && compilerOptions.isOutputFlatModel()) {
				SBMLDocument flattenDoc = sbmlLPNConverter.flattenSBML(compilerOptions.getTestbenchModuleId(), outputDir);
				SBMLWriter writer = new SBMLWriter();
				writer.writeSBMLToFile(flattenDoc.getSBMLDocument(), 
						outputDir + File.separator + compilerOptions.getImplementationModuleId() + "_" + compilerOptions.getTestbenchModuleId() + "_flattened.xml");

			}
			if(compilerOptions.isGenerateLPN()){
				VerilogModule impVerilog = sbmlLPNConverter.getVerilogModule(compilerOptions.getImplementationModuleId());
				VerilogModule tbVerilog = sbmlLPNConverter.getVerilogModule(compilerOptions.getTestbenchModuleId());
				LPN lpn = sbmlLPNConverter.compileToLPN(impVerilog, tbVerilog, outputDir);
				lpn.save(outputDir + File.separator + compilerOptions.getOutputFileName()  + ".lpn");
			}


		}

	}
	
	
	
	@Test
	public void Test_inputSize() throws ParseException, FileNotFoundException {
		String[] args = {"-v", TestingFiles.verilogInitBlock_File};
		
		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		CompilerOptions compilerOptions = VerilogRunner.createCompilerOptions(cmd);
		Assert.assertEquals(1, compilerOptions.getVerilogFiles().size());
	}
	
	@Test
	public void Test_inputSize2() throws ParseException, FileNotFoundException {
		String files = String.join(" ", TestingFiles.verilogInitBlock_File, TestingFiles.verilogInitBlock_File);
		String[] args = {"-v", files};
		
		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		CompilerOptions compilerOptions = VerilogRunner.createCompilerOptions(cmd);
		Assert.assertEquals(2, compilerOptions.getVerilogFiles().size());
	}
	
	@Test
	public void Test_inputSize3() throws ParseException, FileNotFoundException {
		String files = String.join(" ", TestingFiles.verilogInitBlock_File, TestingFiles.verilogAssign_File, TestingFiles.verilogDelay_File);
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
				"-lpn", "-od", TestingFiles.outputDir, "-o", "evenzeroes"};
		//error because missing module identifier names for imp and tb
		runCompiler(args);
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException2( ) throws org.sbml.jsbml.text.parser.ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, ParseException, SBOLConversionException { 
		String files = String.join(" ", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile);
		
		String[] args = {"-v", files, 
				"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench",  
				"-lpn", "-od", TestingFiles.outputDir};
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
	
	@Test(expected = VerilogCompilerException.class)
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
				"-lpn", "-od", TestingFiles.outputDir};
		//error because missing tb module identifier
		runCompiler(args); 
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException7() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile, 
				"-tb", "evenzeroes_testbench",
				"-lpn", "-od", TestingFiles.outputDir};
		//error because missing imp module identifier
		runCompiler(args); 
	}
	
	@Test
	public void Test_SBML() throws ParseException, FileNotFoundException {
		String[] args = {"-v", TestingFiles.verilogInitBlock_File, "-sbml",
						"-od", TestingFiles.outputDir};
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
				"-lpn", "-od", TestingFiles.outputDir, "-o", "evenzeroes"};
		
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
		String[] args = {"--verilogFiles", TestingFiles.verilogInitBlock_File};
		
		CommandLine cmd = VerilogRunner.parseCommandLine(args);
		CompilerOptions compilerOptions = VerilogRunner.createCompilerOptions(cmd);
		Assert.assertEquals(1, compilerOptions.getVerilogFiles().size());
	}
	
	@Test
	public void Test_verbose2() throws ParseException, FileNotFoundException {
		String[] args = {"-sbml", "--odir", TestingFiles.outputDir, "--verilogFiles", TestingFiles.verilogInitBlock_File};
		
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