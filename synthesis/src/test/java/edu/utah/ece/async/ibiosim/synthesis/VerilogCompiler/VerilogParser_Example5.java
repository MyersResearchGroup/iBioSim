package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VerilogConstructs.AbstractVerilogConstruct;
import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogBlock;
import VerilogConstructs.VerilogConditional;
import VerilogConstructs.VerilogDelay;
import VerilogConstructs.VerilogInitialBlock;
import VerilogConstructs.VerilogModule;
import VerilogConstructs.VerilogModuleInstance;
import VerilogConstructs.VerilogWait;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParser_Example5 extends AbstractVerilogParserTest{

	private static VerilogModule verilogModule;

	@BeforeClass
	public static void setupTest() {
		String[] cmd = {"-v", CompilerTestSuite.verilogWaitStmt_file};
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());
		verilogModule = moduleList.get("wait_stmt");
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
		Assert.assertTrue(actual_outputPorts.size() == 1);
		Assert.assertEquals("out0", actual_outputPorts.get(0));
	}

	@Test
	public void TestVerilog_registers() {
		List<String> actual_registers = verilogModule.getRegisters();
		Assert.assertNotNull(actual_registers);
		Assert.assertTrue(actual_registers.size() == 0);
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
		Assert.assertTrue(actual_initialBlocks.size() == 0);
	}

	@Test
	public void TestVerilog_alwaysBlocks() {
		Assert.assertEquals(1, verilogModule.getNumAlwaysBlock());
		VerilogBlock alwaysBlock = verilogModule.getAlwaysBlock(0);
		Assert.assertNotNull(alwaysBlock);
		
		List<AbstractVerilogConstruct> alwaysConstructs = alwaysBlock.getBlockConstructs();
		Assert.assertEquals(2, alwaysConstructs.size());
	}
	
	@Test
	public void TestVerilog_construct1() {
		VerilogBlock block = verilogModule.getAlwaysBlock(0);
		AbstractVerilogConstruct actual_construct = VerilogTestUtility.getBlockConstruct(block, 0);
		Assert.assertNotNull(actual_construct);
		Assert.assertTrue(actual_construct instanceof VerilogAssignment);
		
		VerilogAssignment actual_assignment = (VerilogAssignment) actual_construct;
		Assert.assertEquals("out0", actual_assignment.getVariable());
		Assert.assertEquals("1", actual_assignment.getExpression());
	}
	
	@Test
	public void TestVerilog_construct2() {
		VerilogBlock block = verilogModule.getAlwaysBlock(0);
		AbstractVerilogConstruct actual_construct = VerilogTestUtility.getBlockConstruct(block, 1);
		Assert.assertNotNull(actual_construct);
		Assert.assertTrue(actual_construct instanceof VerilogConditional);
		
		VerilogConditional actual_condition = (VerilogConditional) actual_construct;
		Assert.assertEquals("neq(out0,1)", actual_condition.getIfCondition());

		Assert.assertTrue(actual_condition.getIfBlock() instanceof VerilogWait);
		VerilogWait waitCondition = (VerilogWait) actual_condition.getIfBlock();
		Assert.assertEquals("eq(out0,1)",waitCondition.getWaitExpression());	
		
		AbstractVerilogConstruct waitConstruct = waitCondition.getDelayConstruct();
		Assert.assertNotNull(waitConstruct);
		VerilogDelay delay = (VerilogDelay) waitConstruct;
		Assert.assertEquals(5, delay.getDelayValue());
	}
	
	
}