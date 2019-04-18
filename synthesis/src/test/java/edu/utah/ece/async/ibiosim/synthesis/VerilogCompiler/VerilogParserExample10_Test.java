package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogAlwaysBlock;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogBlock;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogConditional;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogInitialBlock;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModuleInstance;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParserExample10_Test {

	private static VerilogModule verilogModule;
	
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		VerilogParser compiledVerilog = new VerilogParser();
		verilogModule = compiledVerilog.parseVerilogFile(new File(TestingFiles.verilogCondStmt3_file));
		Assert.assertNotNull(verilogModule);
	}
	
	@Test
	public void TestVerilog_inputs() {
		List<String> actual_inputPorts = verilogModule.getInputPorts();
		Assert.assertNotNull(actual_inputPorts);
		Assert.assertTrue(actual_inputPorts.size() == 0);
	}
	
	@Test
	public void TestVerilog_outputs() {
		List<String> actual_outputPorts = verilogModule.getOutputPorts();
		Assert.assertNotNull(actual_outputPorts);
		Assert.assertTrue(actual_outputPorts.size() == 0);
	}
	
	@Test
	public void TestVerilog_registers() {
		List<String> actual_registers = verilogModule.getRegisters();
		Assert.assertNotNull(actual_registers);
		Assert.assertEquals(2, actual_registers.size());
		Assert.assertEquals("a", actual_registers.get(0));
		Assert.assertEquals("b", actual_registers.get(1));
	}
	
	@Test
	public void TestVerilog_submodules() {
		List<VerilogModuleInstance> actual_submodules = verilogModule.getSubmodules();
		Assert.assertNotNull(actual_submodules);
		Assert.assertTrue(actual_submodules.size() == 0);
	}
	
	@Test
	public void TestVerilog_initialBlocks() {
		List<VerilogInitialBlock> actual_initBlocks = verilogModule.getInitialBlockList();
		Assert.assertNotNull(actual_initBlocks);
		Assert.assertTrue(actual_initBlocks.size() == 0);
	}
	
	@Test
	public void TestVerilog_alwaysBlocks() {
		List<VerilogAlwaysBlock> actual_alwayBlocks = verilogModule.getAlwaysBlockList();
		Assert.assertNotNull(actual_alwayBlocks);
		Assert.assertTrue(actual_alwayBlocks.size() == 1);
		VerilogBlock alwaysBlock = (VerilogBlock) actual_alwayBlocks.get(0).getBlock();
		
		Assert.assertEquals(1, alwaysBlock.getBlockConstructs().size());
		Assert.assertTrue(alwaysBlock.getBlockConstructs().get(0) instanceof VerilogConditional);
		VerilogConditional actual_if = (VerilogConditional) alwaysBlock.getBlockConstructs().get(0);
		Assert.assertEquals("a", actual_if.getIfCondition());
		VerilogBlock actual_ifBlock = (VerilogBlock) actual_if.getIfBlock();
		Assert.assertEquals(0, actual_ifBlock.getBlockConstructs().size());
		
		Assert.assertNotNull(actual_if.getElseBlock());
		Assert.assertTrue(actual_if.getElseBlock() instanceof VerilogConditional);
		VerilogConditional actual_else_ifCondition = (VerilogConditional) actual_if.getElseBlock();
		Assert.assertEquals("a", actual_else_ifCondition.getIfCondition());
		VerilogBlock actual_elseIfBlock = (VerilogBlock) actual_else_ifCondition.getIfBlock();
		Assert.assertEquals(0, actual_elseIfBlock.getBlockConstructs().size());
		
		Assert.assertTrue(actual_else_ifCondition.getElseBlock() instanceof VerilogBlock);
		VerilogBlock actual_elseBlock = (VerilogBlock) actual_else_ifCondition.getElseBlock();
		Assert.assertEquals(0, actual_elseBlock.getBlockConstructs().size());
	}
	
	
}
