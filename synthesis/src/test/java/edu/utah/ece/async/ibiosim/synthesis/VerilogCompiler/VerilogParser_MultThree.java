package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Arrays;
import java.util.HashMap;
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
import VerilogConstructs.VerilogModule;
import VerilogConstructs.VerilogModuleInstance;
import VerilogConstructs.VerilogWait;


/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParser_MultThree extends AbstractVerilogParserTest{
	
	private static VerilogModule verilog_imp, verilog_tb;
	private static VerilogBlock imp_alwaysblk, tb_alwaysblk;
	
	@BeforeClass
	public static void setupTest() {
		String files = String.join(" ", CompilerTestSuite.verilogMultThree_impFile, CompilerTestSuite.verilogMultThree_tbFile);	
		String[] cmd = {"-v", files};
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(2, moduleList.size());
		
		verilog_imp = moduleList.get("multthree_imp");
		Assert.assertNotNull(verilog_imp);
		imp_alwaysblk = verilog_imp.getAlwaysBlock(0);
		
		verilog_tb = moduleList.get("multThree_testbench");
		Assert.assertNotNull(verilog_tb);
		tb_alwaysblk = verilog_tb.getAlwaysBlock(0);
	}
	
	@Test
	public void Test_imp_inputSize() {
		Assert.assertEquals(2, verilog_imp.getNumInputs());
	}
	
	@Test
	public void Test_imp_inputs() {
		List<String> actual_inputPorts = verilog_imp.getInputPorts();
		List<String> expected_inputs = Arrays.asList("in0", "in1");
		Assert.assertEquals(expected_inputs, actual_inputPorts);
	}
	
	@Test
	public void Test_tb_inputSize() {
		Assert.assertEquals(0, verilog_tb.getNumInputs());
	}
	
	@Test
	public void Test_imp_outputSize() {
		Assert.assertEquals(2, verilog_imp.getNumOutputs());
	}
	
	@Test
	public void Test_imp_outputs() {
		List<String> actual_outputPorts = verilog_imp.getOutputPorts();
		List<String> expected_outputs = Arrays.asList("parity0", "parity1");
		Assert.assertEquals(expected_outputs, actual_outputPorts);
	}
	
	@Test
	public void Test_tb_outputSize() {
		Assert.assertEquals(0, verilog_tb.getNumOutputs());
	}
	
	@Test
	public void Test_imp_regSize() {
		Assert.assertEquals(3, verilog_imp.getNumRegisters());
	}
	
	@Test
	public void Test_imp_reg() {
		List<String> actual_registers = verilog_imp.getRegisters();
		List<String> expected_reg = Arrays.asList("state0", "state1", "temp");
		Assert.assertEquals(expected_reg, actual_registers);
	}

	@Test
	public void Test_tb_regSize() {
		Assert.assertEquals(3, verilog_tb.getNumRegisters());
	}
	
	@Test
	public void Test_tb_registers() {
		List<String> actual_registers = verilog_tb.getRegisters();
		List<String> expected_reg = Arrays.asList("bit0", "bit1", "next");
		Assert.assertEquals(expected_reg, actual_registers);
	}
	
	@Test 
	public void Test_imp_wireSize() {
		Assert.assertEquals(0, verilog_imp.getNumWires());
	}
	
	@Test 
	public void Test_tb_wireSize() {
		Assert.assertEquals(2, verilog_tb.getNumWires());
	}
	
	@Test 
	public void Test_tb_wires() {
		List<String> actual_wires = verilog_tb.getWirePorts();
		List<String> expected_wires = Arrays.asList("parity0", "parity1");
		Assert.assertEquals(expected_wires, actual_wires);
	}
	
	@Test
	public void Test_imp_submoduleSize() {
		Assert.assertEquals(0, verilog_imp.getNumSubmodules());
	}
	
	@Test
	public void Test_tb_submoduleSize() {
		Assert.assertEquals(1, verilog_tb.getNumSubmodules());
	}
	
	@Test
	public void Test_tb_submodRef() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);
		Assert.assertEquals("multthree_imp", submodule.getModuleReference());
	}
	
	@Test
	public void Test_tb_submodInstance() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);
		Assert.assertEquals("mt_instance", submodule.getSubmoduleId());
	}
	
	@Test
	public void Test_tb_subModConnSize() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);
		Assert.assertTrue(submodule.getNumNamedConnections() == 4);
	}
	
	@Test
	public void Test_tb_subModConnections() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);

		Map<String, String> expectedConnections = new HashMap<>();
		expectedConnections.put("bit0", "in0");
		expectedConnections.put("bit1", "in1");
		expectedConnections.put("parity0", "parity0");
		expectedConnections.put("parity1", "parity1");

		Map<String, String> portConnections = submodule.getNamedConnections();
		for(Map.Entry<String, String> actualConnection: portConnections.entrySet()) {
			Assert.assertTrue(expectedConnections.containsKey(actualConnection.getKey()));
			Assert.assertEquals(expectedConnections.get(actualConnection.getKey()), actualConnection.getValue());
		}
	}

	@Test
	public void Test_imp_alwaysblkSize() {
		Assert.assertEquals(1, verilog_imp.getNumAlwaysBlock());
	}
	
	@Test
	public void Test_imp_alwaysCstSize() {
		Assert.assertEquals(6, imp_alwaysblk.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_alwaysblkSize() {
		Assert.assertEquals(1, verilog_tb.getNumAlwaysBlock());
	}
	
	@Test
	public void Test_tb_alwaysCstSize() {
		Assert.assertEquals(6, tb_alwaysblk.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const1_wait() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 0);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		Assert.assertEquals("or(eq(in0,1),eq(in1,1))", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_imp_const1_delay() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 0);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const2_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("or(and(eq(in0,1),and(neq(state1,1),neq(state0,1))),and(eq(in1,1),and(neq(state1,1),eq(state0,1))))", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const2_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("parity1", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const2_elseCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(3, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_else_nestedIfCond() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		Assert.assertEquals("eq(state1,1)", nestedIf.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_else_nestedIfCondAssign() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		VerilogBlock nested_block = VerilogTestUtility.getVerilogBlock(nestedIf);
		AbstractVerilogConstruct nest_cst = VerilogTestUtility.getBlockConstruct(nested_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(nest_cst);
		Assert.assertEquals("temp", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const2_else_nestedElseCondAssign() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		VerilogBlock nested_block = VerilogTestUtility.getVerilogBlock(nestedIf.getElseBlock());
		AbstractVerilogConstruct nest_cst = VerilogTestUtility.getBlockConstruct(nested_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(nest_cst);
		Assert.assertEquals("temp", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const2_else_nestedDelay() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());	
	}
	
	@Test
	public void Test_imp_const2_else_nestedAssign() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, 2);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("parity0", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const3_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(parity0,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_if_nestedIfCond() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond.getIfBlock());
		AbstractVerilogConstruct nested_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(nested_cst);
		Assert.assertEquals("and(neq(state1,1),neq(state0,1))", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_if_nestedIfAssign() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond.getIfBlock());
		AbstractVerilogConstruct nested_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(nested_cst);
		VerilogBlock nestedIf_block = VerilogTestUtility.getVerilogBlock(nestedIf.getIfBlock());
		
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(nestedIf_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("state0", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const3_if_nestedElseIf() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond.getIfBlock());
		AbstractVerilogConstruct nested_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(nested_cst);
		VerilogConditional nestedElseIf = VerilogTestUtility.getConditionalConstruct(nestedIf.getElseBlock());
		Assert.assertEquals("and(neq(state1,1),eq(state0,1))", nestedElseIf.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_if_nestedElseIfAssign() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond.getIfBlock());
		AbstractVerilogConstruct nested_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(nested_cst);
		VerilogConditional nestedElseIf = VerilogTestUtility.getConditionalConstruct(nestedIf.getElseBlock());
		VerilogBlock nestedElseIf_block = VerilogTestUtility.getVerilogBlock(nestedElseIf.getIfBlock());
		
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(nestedElseIf_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("state1", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const3_if_nestedElseIf2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond.getIfBlock());
		AbstractVerilogConstruct nested_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(nested_cst);
		VerilogConditional nestedElseIf = VerilogTestUtility.getConditionalConstruct(nestedIf.getElseBlock());
		VerilogConditional nestedElseIf2 = VerilogTestUtility.getConditionalConstruct(nestedElseIf.getElseBlock());
		Assert.assertEquals("and(eq(state1,1),and(eq(state0,1),eq(in0,1)))", nestedElseIf2.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_if_nestedElseIf2Assign() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond.getIfBlock());
		AbstractVerilogConstruct nested_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(nested_cst);
		VerilogConditional nestedElseIf = VerilogTestUtility.getConditionalConstruct(nestedIf.getElseBlock());
		VerilogConditional nestedElseIf2 = VerilogTestUtility.getConditionalConstruct(nestedElseIf.getElseBlock());
		VerilogBlock nestedElseIf_block = VerilogTestUtility.getVerilogBlock(nestedElseIf2.getIfBlock());
		
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(nestedElseIf_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("state1", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const3_elseIf() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseIf_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		Assert.assertEquals("and(eq(parity1,1),and(neq(state1,1),eq(state0,1)))", elseIf_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_elseIfAssign() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseIf_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogBlock elseIf_block = VerilogTestUtility.getVerilogBlock(elseIf_cond.getIfBlock());
		
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(elseIf_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("state0", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const4_wait() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 3);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(alwaysConstruct);
		Assert.assertEquals("or(and(eq(parity1,1),and(neq(state0,1),neq(state1,1))),"
				+ "or(and(eq(parity0,1),and(neq(state1,1),eq(state0,1))),and(eq(parity0,1),and(eq(state1,1),eq(state0,1)))))", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_imp_const4_delay() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 3);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(alwaysConstruct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const5_wait() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(alwaysConstruct);
		Assert.assertEquals("and(neq(in0,1),neq(in1,1))", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_imp_const5_delay() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(alwaysConstruct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const6_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(parity0,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const6_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const6_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const6_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("parity0", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const6_elseCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(2, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const6_elseCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(else_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const6_elseCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(else_cst);
		Assert.assertEquals("parity1", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 0);
		Assert.assertNotNull(alwaysConstruct);
	
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(alwaysConstruct);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(alwaysConstruct);
		Assert.assertEquals("next", actual_assign.getVariable());
		Assert.assertEquals("piecewise(1, uniform(0,1)  < 0.5, 0)", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const3() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("not(next)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_tb_const3_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const3_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	
	@Test
	public void Test_tb_const3_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("bit0", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const3_elseCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(2, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const3_elseCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(else_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const3_elseCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(else_cst);
		Assert.assertEquals("bit1", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const4_wait() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 3);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(alwaysConstruct);
		Assert.assertEquals("or(eq(parity0,1),eq(parity1,1))", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_tb_const4_delay() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 3);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(alwaysConstruct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const5_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(bit0,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_tb_const5_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const5_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	
	@Test
	public void Test_tb_const5_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("bit0", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const5_elseIf() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseIf_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		Assert.assertEquals("eq(bit1,1)", elseIf_cond.getIfCondition());
	}
	
	@Test
	public void Test_tb_const5_elseIfCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseIf_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseIf_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const5_elseICst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseIf_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseIf_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
	
	
	@Test
	public void Test_tb_const5_elseIfCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseIf_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseIf_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("bit1", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const6_wait() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(alwaysConstruct);
		Assert.assertEquals("and(neq(parity0,1),neq(parity1,1))", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_tb_const6_delay() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(alwaysConstruct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals(5, actual_delay.getDelayValue());
	}
}