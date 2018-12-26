package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VerilogConstructs.AbstractVerilogConstruct;
import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogBlock;
import VerilogConstructs.VerilogInitialBlock;
import VerilogConstructs.VerilogModule;
import VerilogConstructs.VerilogModuleInstance;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParserExample1_Test {
	
	private static VerilogModule verilogModule;
	
	@BeforeClass
	public static void setupTest() {
		
		String[] cmd = {"-v", CompilerTestSuite.verilogInitBlock_file};
		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
		
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());
		verilogModule = moduleList.get("init_block");
		Assert.assertNotNull(verilogModule);
	}
	
	@Test
	public void TestVerilog_inputs() {
		List<String> actual_inputPorts = verilogModule.getInputPorts();
		Assert.assertNotNull(actual_inputPorts);
		Assert.assertTrue(actual_inputPorts.size() == 1);
		Assert.assertEquals("in0", actual_inputPorts.get(0));
	}
	
	@Test
	public void TestVerilog_outputs() {
		List<String> actual_outputPorts = verilogModule.getOutputPorts();
		Assert.assertNotNull(actual_outputPorts);
		Assert.assertTrue(actual_outputPorts.size() == 1);
		Assert.assertEquals("out0", actual_outputPorts.get(0));
	}
	
	@Test
	public void TestVerilog_registers() {
		List<String> actual_registers = verilogModule.getRegisters();
		Assert.assertNotNull(actual_registers);
		Assert.assertTrue(actual_registers.size() == 1);
		Assert.assertEquals("state", actual_registers.get(0));
	}
	
	@Test
	public void TestVerilog_submodules() {
		List<VerilogModuleInstance> actual_submodules = verilogModule.getSubmodules();
		Assert.assertNotNull(actual_submodules);
		Assert.assertTrue(actual_submodules.size() == 0);
	}
	
	@Test
	public void TestVerilog_initialBlocks() {
		List<VerilogInitialBlock> actual_initialBlocks = verilogModule.getInitialBlockList();
		Assert.assertNotNull(actual_initialBlocks);
		Assert.assertTrue(actual_initialBlocks.size() == 1);

		VerilogInitialBlock block = actual_initialBlocks.get(0);
		Assert.assertTrue(block.getBlock() instanceof VerilogBlock);
		VerilogBlock actual_initialBlock = (VerilogBlock) block.getBlock();
		List<AbstractVerilogConstruct> actual_blockConstructs = actual_initialBlock.getBlockConstructs();
		Assert.assertTrue(actual_blockConstructs.size() == 2);

		HashMap<String, String> expected_results = new HashMap<>();
		expected_results.put("out0", "0");
		expected_results.put("state", "1");
		for (AbstractVerilogConstruct current_construct : actual_blockConstructs) {
			Assert.assertTrue(current_construct instanceof VerilogAssignment);
			VerilogAssignment actual_initAssignment = (VerilogAssignment) current_construct;
			Assert.assertTrue(expected_results.containsKey(actual_initAssignment.getVariable()));
			Assert.assertEquals(expected_results.get(actual_initAssignment.getVariable()),
					actual_initAssignment.getExpression());
		}
	}
	
	@Test
	public void TestVerilog_alwaysBlocks() {
		Assert.assertEquals(0, verilogModule.getAlwaysBlockList().size());
	}
	

}