package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VerilogConstructs.AbstractVerilogConstruct;
import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogBlock;
import VerilogConstructs.VerilogDelay;
import VerilogConstructs.VerilogInitialBlock;
import VerilogConstructs.VerilogModule;

/**
 * Test parsing of urandom_range() verilog function for two parameter
 * 
 * @author Tramy Nguyen
 */
public class VerilogParserExample14_Test {
	
	private static VerilogModule verilogModule;

	@BeforeClass
	public static void setupTest() {

		String[] cmd = {"-v", CompilerTestSuite.verilogSystemFunc2_file};

		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());

		verilogModule = moduleList.get("system_func2");
		Assert.assertNotNull(verilogModule);
	}

	@Test
	public void TestVerilog_registers() {
		List<String> actual_registers = verilogModule.getRegisters();
		Assert.assertNotNull(actual_registers);
		Assert.assertTrue(actual_registers.size() == 1);
		Assert.assertEquals("bit0", actual_registers.get(0));
	}
	
	@Test
	public void TestVerilog_initialBlocks() {
		List<VerilogInitialBlock> actual_initialBlocks = verilogModule.getInitialBlockList();
		Assert.assertNotNull(actual_initialBlocks);
		Assert.assertTrue(actual_initialBlocks.size() == 1);

		VerilogInitialBlock block = actual_initialBlocks.get(0);
		Assert.assertTrue(block.getBlock() instanceof VerilogBlock);
		VerilogBlock actual_initialBlock = (VerilogBlock) block.getBlock();
		
		AbstractVerilogConstruct current_construct = VerilogTestUtility.getBlockConstruct(actual_initialBlock, 0);
		VerilogAssignment actual_initAssignment = VerilogTestUtility.getVerilogAssignment(current_construct);
		Assert.assertEquals("bit0", actual_initAssignment.getVariable());
		Assert.assertEquals("0", actual_initAssignment.getExpression());
		
	}
	
	@Test
	public void TestVerilog_alwaysBlocks() {
		Assert.assertEquals(1, verilogModule.getAlwaysBlockList().size());
	}

	@Test
	public void TestVerilog_alwaysCstSize() {
		Assert.assertEquals(2, verilogModule.getAlwaysBlock(0).getNumConstructSize());
	}
	
	@Test
	public void TestVerilog_construct1() {
		VerilogBlock block = verilogModule.getAlwaysBlock(0);
		AbstractVerilogConstruct actual_construct = VerilogTestUtility.getBlockConstruct(block, 0);
		Assert.assertNotNull(actual_construct);
		
		VerilogDelay delay = VerilogTestUtility.getDelayConstruct(actual_construct);
		assertEquals("uniform(5,10)", delay.getDelayValue());
	}
	
	
	@Test
	public void TestVerilog_construct2() {
		VerilogBlock block = verilogModule.getAlwaysBlock(0);
		AbstractVerilogConstruct actual_construct = VerilogTestUtility.getBlockConstruct(block, 1);
		Assert.assertNotNull(actual_construct);
		
		VerilogAssignment assign = VerilogTestUtility.getVerilogAssignment(actual_construct);
		assertEquals("bit0", assign.getVariable());
		assertEquals("1", assign.getExpression());
	}	
	
}
