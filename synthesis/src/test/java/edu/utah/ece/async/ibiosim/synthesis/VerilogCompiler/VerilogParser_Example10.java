package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VerilogConstructs.AbstractVerilogConstruct;
import VerilogConstructs.VerilogAlwaysBlock;
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
public class VerilogParser_Example10 extends AbstractVerilogParserTest{

	private static VerilogModule verilogModule;
	
	@BeforeClass
	public static void setupTest() {
		String[] cmd = {"-v", reader.getFile("init_block.v")};
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		
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
		List<VerilogInitialBlock> actual_initBlocks = verilogModule.getInitialBlockList();
		Assert.assertNotNull(actual_initBlocks);
		Assert.assertTrue(actual_initBlocks.size() == 1);
		
		HashMap<String, String> expected_initConstructs = new HashMap<>();
		expected_initConstructs.put("out0", "0");
		expected_initConstructs.put("state", "1");
		
		VerilogBlock init_block = (VerilogBlock) actual_initBlocks.get(0).getBlock();
		for(AbstractVerilogConstruct actual_construct: init_block.getBlockConstructs()) {
			Assert.assertTrue(actual_construct instanceof VerilogAssignment);
			VerilogAssignment actual_assign = (VerilogAssignment) actual_construct;
			Assert.assertTrue(expected_initConstructs.containsKey(actual_assign.getVariable()));
			Assert.assertEquals(expected_initConstructs.get(actual_assign.getVariable()),
					actual_assign.getExpression());
		}
	}
	
	@Test
	public void TestVerilog_alwaysBlocks() {
		List<VerilogAlwaysBlock> actual_alwayBlocks = verilogModule.getAlwaysBlockList();
		Assert.assertNotNull(actual_alwayBlocks);
		Assert.assertTrue(actual_alwayBlocks.size() == 0);
	}
	
	
	
}