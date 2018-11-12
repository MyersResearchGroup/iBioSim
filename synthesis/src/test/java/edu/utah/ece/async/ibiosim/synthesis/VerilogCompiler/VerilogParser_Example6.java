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
import VerilogConstructs.VerilogInitialBlock;
import VerilogConstructs.VerilogModule;
import VerilogConstructs.VerilogModuleInstance;


/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParser_Example6 extends AbstractVerilogParserTest{

	private static VerilogModule verilogModule;
	
	@BeforeClass
	public static void setupTest() {
		String[] cmd = {"-v", reader.getFile("conditional_stmt1.v")};
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());
		verilogModule = moduleList.get("conditional_stmt1");
		Assert.assertNotNull(verilogModule);
	}
	
	@Test
	public void TestVerilog_inputs() {
		List<String> actual_inputPorts = verilogModule.getInputPorts();
		Assert.assertNotNull(actual_inputPorts);
		Assert.assertTrue(actual_inputPorts.size() == 1);
		Assert.assertEquals("in", actual_inputPorts.get(0));
	}
	
	@Test
	public void TestVerilog_outputs() {
		List<String> actual_outputPorts = verilogModule.getOutputPorts();
		Assert.assertNotNull(actual_outputPorts);
		Assert.assertTrue(actual_outputPorts.size() == 1);
		Assert.assertEquals("out", actual_outputPorts.get(0));
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
		Assert.assertEquals(1, alwaysConstructs.size());
	}
	
	@Test
	public void TestVerilog_construct1() {
		VerilogBlock block = verilogModule.getAlwaysBlock(0);
		AbstractVerilogConstruct actual_construct = VerilogTestUtility.getAlwaysConstruct(block, 0);
		Assert.assertNotNull(actual_construct);
		Assert.assertTrue(actual_construct instanceof VerilogConditional);
		
		VerilogConditional actual_alwayAssignment = (VerilogConditional) actual_construct;
		Assert.assertEquals("in", actual_alwayAssignment.getIfCondition());
		AbstractVerilogConstruct ifBlock = actual_alwayAssignment.getIfBlock();
		Assert.assertTrue(ifBlock instanceof VerilogAssignment);

		VerilogAssignment assignment = (VerilogAssignment) ifBlock;
		Assert.assertEquals("out", assignment.getVariable());	
		Assert.assertEquals("0", assignment.getExpression());	

		AbstractVerilogConstruct elseBlock = actual_alwayAssignment.getElseBlock();
		assignment = (VerilogAssignment) elseBlock;
		Assert.assertEquals("out", assignment.getVariable());	
		Assert.assertEquals("1", assignment.getExpression());	
	}

	
}
