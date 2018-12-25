package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class CompilerOptions_Tests extends AbstractVerilogParserTest{
	
	@Test
	public void Test_inputSize() throws ParseException, FileNotFoundException {
		String[] args = {"-v", CompilerTestSuite.verilogInitBlock_file};
		
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
		Assert.assertEquals(1, compilerOptions.getVerilogFiles().size());
	}
	
	@Test
	public void Test_inputSize2() throws ParseException, FileNotFoundException {
		String files = String.join(" ", CompilerTestSuite.verilogInitBlock_file, CompilerTestSuite.verilogInitBlock_file);
		String[] args = {"-v", files};
		
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
		Assert.assertEquals(2, compilerOptions.getVerilogFiles().size());
	}
	
	@Test
	public void Test_inputSize3() throws ParseException, FileNotFoundException {
		String files = String.join(" ", CompilerTestSuite.verilogInitBlock_file, CompilerTestSuite.verilogAssign_file, CompilerTestSuite.verilogDelay_file);
		String[] args = {"-v", files};
		
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
		Assert.assertEquals(3, compilerOptions.getVerilogFiles().size());
	}
	
	@Test(expected = org.apache.commons.cli.ParseException.class)
	public void Test_PaserException() throws ParseException {
		String[] args = {"-v"};
		
		Main.parseCommandLine(args);
	}
	
	@Test(expected = FileNotFoundException.class)
	public void Test_FileNotFoundException() throws ParseException, FileNotFoundException {
		String[] args = {"-v", "a.v"};

		CommandLine cmd = Main.parseCommandLine(args);
		Main.createCompilerOptions(cmd);
		
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile, 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "evenzeroes"};
		//error because missing module identifier names for imp and tb
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
		compilerOptions.verifyCompilerSetup();
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException2() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile, 
				"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench",  
				"-lpn", "-od", CompilerTestSuite.outputDirectory};
		//error because missing output file name
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
		compilerOptions.verifyCompilerSetup();
	}
	
	@Test()
	public void Test_VerilogCompilerException3() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile, 
				"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench",  
				"-lpn"};
		//no output directory and output file name
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
			compilerOptions.verifyCompilerSetup();
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException4() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile, 
				"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench",  
				"-lpn", "-o", "evenzeroes"};
		//error because missing output directory
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
		compilerOptions.verifyCompilerSetup();
	}
	
	@Test()
	public void Test_VerilogCompilerException5() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile, 
				"-sbml"};
		//no output directory
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
		compilerOptions.verifyCompilerSetup();
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException6() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile, 
				"-imp", "evenzeroes_imp",  
				"-lpn", "-od", CompilerTestSuite.outputDirectory};
		//error because missing tb module identifier
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
		compilerOptions.verifyCompilerSetup();
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test_VerilogCompilerException7() throws ParseException, org.sbml.jsbml.text.parser.ParseException, XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBOLValidationException, SBOLConversionException {
		String[] args = {"-v", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile, 
				"-tb", "evenzeroes_testbench",
				"-lpn", "-od", CompilerTestSuite.outputDirectory};
		//error because missing imp module identifier
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
		compilerOptions.verifyCompilerSetup();
	}
	
	@Test
	public void Test_SBML() throws ParseException, FileNotFoundException {
		String[] args = {"-v", CompilerTestSuite.verilogInitBlock_file, "-sbml",
						"-od", CompilerTestSuite.outputDirectory};
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
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
		String[] args = {"-v", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile, 
				"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "evenzeroes"};
		
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
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
		String[] args = {"--verilogFiles", CompilerTestSuite.verilogInitBlock_file};
		
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
		Assert.assertEquals(1, compilerOptions.getVerilogFiles().size());
	}
	
	@Test
	public void Test_verbose2() throws ParseException, FileNotFoundException {
		String[] args = {"-sbml", "--odir", CompilerTestSuite.outputDirectory, "--verilogFiles", CompilerTestSuite.verilogInitBlock_file};
		
		CommandLine cmd = Main.parseCommandLine(args);
		CompilerOptions compilerOptions = Main.createCompilerOptions(cmd);
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
		
		CommandLine cmd = Main.parseCommandLine(args);
		Main.createCompilerOptions(cmd);
	}
}