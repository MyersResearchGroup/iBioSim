package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VerilogConstructs.VerilogAlwaysBlock;
import VerilogConstructs.VerilogBlock;
import VerilogConstructs.VerilogConditional;
import VerilogConstructs.VerilogInitialBlock;
import VerilogConstructs.VerilogModule;
import VerilogConstructs.VerilogModuleInstance;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParser_Example10 extends AbstractVerilogParserTest{

	private static VerilogModule verilogModule;
	
	@BeforeClass
	public static void setupTest() {
		String[] cmd = {"-v", CompilerTestSuite.verilogCondStmt3_file};
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());
		
		verilogModule = moduleList.get("if_stmnt3");
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
