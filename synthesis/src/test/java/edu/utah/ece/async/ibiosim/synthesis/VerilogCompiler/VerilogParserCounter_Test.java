package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.AbstractVerilogConstruct;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogAssignment;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogBlock;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogConditional;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogDelay;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModuleInstance;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogWait;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParserCounter_Test {
	
	private static VerilogModule verilog_imp, verilog_tb;
	private static VerilogBlock imp_alwaysblk, tb_alwaysblk;
	
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		VerilogParser compiledVerilog = new VerilogParser();
		
		verilog_imp = compiledVerilog.parseVerilogFile(new File(TestingFiles.verilogCounter_impFile));
		Assert.assertNotNull(verilog_imp);
		imp_alwaysblk = verilog_imp.getAlwaysBlock(0);
		
		verilog_tb = compiledVerilog.parseVerilogFile(new File(TestingFiles.verilogCounter_tbFile));
		Assert.assertNotNull(verilog_tb);
		tb_alwaysblk = verilog_tb.getAlwaysBlock(0);
	}
	
	@Test
	public void Test_imp_inputSize() {
		Assert.assertEquals(1, verilog_imp.getNumInputs());
	}
	
	@Test
	public void Test_imp_inputs() {
		List<String> actual_inputPorts = verilog_imp.getInputPorts();
		List<String> expected_inputs = Arrays.asList("req");
		Assert.assertEquals(expected_inputs, actual_inputPorts);
	}
	
	@Test
	public void Test_tb_inputSize() {
		Assert.assertEquals(0, verilog_tb.getNumInputs());
	}
	
	@Test
	public void Test_imp_outputSize() {
		Assert.assertEquals(5, verilog_imp.getNumOutputs());
	}
	
	@Test
	public void Test_imp_outputs() {
		List<String> actual_outputPorts = verilog_imp.getOutputPorts();
		List<String> expected_outputs = Arrays.asList("a0", "a1", "b0", "b1", "ack");
		Assert.assertEquals(expected_outputs, actual_outputPorts);
	}
	
	@Test
	public void Test_tb_outputSize() {
		Assert.assertEquals(0, verilog_tb.getNumOutputs());
	}
	
	@Test
	public void Test_imp_regSize() {
		Assert.assertEquals(2, verilog_imp.getNumRegisters());
	}
	
	@Test
	public void Test_imp_reg() {
		List<String> actual_registers = verilog_imp.getRegisters();
		List<String> expected_reg = Arrays.asList("state0", "state1");
		Assert.assertEquals(expected_reg, actual_registers);
	}

	@Test
	public void Test_tb_regSize() {
		Assert.assertEquals(1, verilog_tb.getNumRegisters());
	}
	
	@Test
	public void Test_tb_registers() {
		List<String> actual_registers = verilog_tb.getRegisters();
		List<String> expected_reg = Arrays.asList("req");
		Assert.assertEquals(expected_reg, actual_registers);
	}
	
	@Test 
	public void Test_imp_wireSize() {
		Assert.assertEquals(0, verilog_imp.getNumWires());
	}
	
	@Test 
	public void Test_tb_wireSize() {
		Assert.assertEquals(5, verilog_tb.getNumWires());
	}
	
	@Test 
	public void Test_tb_wires() {
		List<String> actual_wires = verilog_tb.getWirePorts();
		List<String> expected_wires = Arrays.asList("a0", "a1", "b0", "b1", "ack");
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
		Assert.assertEquals("counter_imp", submodule.getModuleReference());
	}
	
	@Test
	public void Test_tb_submodInstance() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);
		Assert.assertEquals("counter_instance", submodule.getSubmoduleId());
	}
	
	@Test
	public void Test_tb_subModConnSize() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);
		Assert.assertTrue(submodule.getNumNamedConnections() == 6);
	}
	
	@Test
	public void Test_tb_subModConnections() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);

		Map<String, String> expectedConnections = new HashMap<>();
		expectedConnections.put("a0", "a0");
		expectedConnections.put("a1", "a1");
		expectedConnections.put("b0", "b0");
		expectedConnections.put("b1", "b1");
		expectedConnections.put("req", "req");
		expectedConnections.put("ack", "ack");

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
		Assert.assertEquals(11, imp_alwaysblk.getNumConstructSize());
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
		Assert.assertEquals("eq(req,1)", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_imp_const1_delay() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 0);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const2_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("and(neq(state1,1),neq(state0,1))", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(6, if_block.getNumConstructSize());
	}
		
	@Test
	public void Test_imp_const2_ifCstDelays() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		int[] delay_index = {0, 2, 4};
		int i = 0;
		while(i < delay_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, delay_index[i++]);
			VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
			Assert.assertNotNull(actual_delay);
			Assert.assertEquals("5", actual_delay.getDelayValue());
		}
		
	}
	
	@Test
	public void Test_imp_const2_ifCstAssigns() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		
		Map<String, String> expectedAssign = new HashMap<>();
		expectedAssign.put("a0", "1");
		expectedAssign.put("b0", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3, 5};
		int i = 0;
		while(i < assign_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, assign_index[i++]);
			VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
			Assert.assertTrue(expectedAssign.containsKey(actual_assign.getVariable()));
			Assert.assertEquals(expectedAssign.get(actual_assign.getVariable()), actual_assign.getExpression());
		}
	}
	
	@Test
	public void Test_imp_const2_elseif() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		Assert.assertEquals("and(neq(state1,1),eq(state0,1))", elseif_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_elseifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(6, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_elseifCstDelays() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock elseif_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		int[] delay_index = {0, 2, 4};
		int i = 0;
		while(i < delay_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(elseif_block, delay_index[i++]);
			VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
			Assert.assertNotNull(actual_delay);
			Assert.assertEquals("5", actual_delay.getDelayValue());
		}
		
	}
	
	@Test
	public void Test_imp_const2_elseifCstAssigns() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock elseif_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		
		Map<String, String> expectedAssign = new HashMap<>();
		expectedAssign.put("a0", "1");
		expectedAssign.put("b1", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3, 5};
		int i = 0;
		while(i < assign_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(elseif_block, assign_index[i++]);
			VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
			Assert.assertTrue(expectedAssign.containsKey(actual_assign.getVariable()));
			Assert.assertEquals(expectedAssign.get(actual_assign.getVariable()), actual_assign.getExpression());
		}
	}
	
	@Test
	public void Test_imp_const2_elseif2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		Assert.assertEquals("and(eq(state1,1),eq(state0,1))", elseif2_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_elseif2CstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif2_cond.getIfBlock());
		Assert.assertEquals(6, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_elseif2CstDelays() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif2_cond.getIfBlock());
		int[] delay_index = {0, 2, 4};
		int i = 0;
		while(i < delay_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, delay_index[i++]);
			VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
			Assert.assertNotNull(actual_delay);
			Assert.assertEquals("5", actual_delay.getDelayValue());
		}
		
	}
	
	@Test
	public void Test_imp_const2_elseif2CstAssigns() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif2_cond.getIfBlock());
		
		Map<String, String> expectedAssign = new HashMap<>();
		expectedAssign.put("a1", "1");
		expectedAssign.put("b0", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3, 5};
		int i = 0;
		while(i < assign_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, assign_index[i++]);
			VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
			Assert.assertTrue(expectedAssign.containsKey(actual_assign.getVariable()));
			Assert.assertEquals(expectedAssign.get(actual_assign.getVariable()), actual_assign.getExpression());
		}
	}
	
	@Test
	public void Test_imp_const2_elseif3() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		Assert.assertEquals("and(eq(state1,1),neq(state0,1))", elseif3_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_elseif3CstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif3_cond.getIfBlock());
		Assert.assertEquals(6, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_elseif3CstDelays() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif3_cond.getIfBlock());
		int[] delay_index = {0, 2, 4};
		int i = 0;
		while(i < delay_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, delay_index[i++]);
			VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
			Assert.assertNotNull(actual_delay);
			Assert.assertEquals("5", actual_delay.getDelayValue());
		}
		
	}
	
	@Test
	public void Test_imp_const2_elseif3CstAssigns() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif3_cond.getIfBlock());
		
		Map<String, String> expectedAssign = new HashMap<>();
		expectedAssign.put("a1", "1");
		expectedAssign.put("b1", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3, 5};
		int i = 0;
		while(i < assign_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, assign_index[i++]);
			VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
			Assert.assertTrue(expectedAssign.containsKey(actual_assign.getVariable()));
			Assert.assertEquals(expectedAssign.get(actual_assign.getVariable()), actual_assign.getExpression());
		}
	}
	
	@Test
	public void Test_imp_const3_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("and(eq(a0,1),eq(b0,1))", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(1, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const3_ifCst_nestedCond() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		Assert.assertEquals("and(neq(state1,1),neq(state0,1))", nestedIf.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_ifCst_nestedCondSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		VerilogBlock nested_block = VerilogTestUtility.getVerilogBlock(nestedIf);
		Assert.assertEquals(1, nested_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const3_ifCst_nestedCondAssign() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		VerilogBlock nested_block = VerilogTestUtility.getVerilogBlock(nestedIf);
		AbstractVerilogConstruct nest_cst = VerilogTestUtility.getBlockConstruct(nested_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(nest_cst);
		Assert.assertEquals("state0", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const3_elseif() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		Assert.assertEquals("and(eq(a0,1),eq(b1,1))", elseif_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_elseifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif_cond.getIfBlock());
		Assert.assertEquals(1, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const3_elseif_nestedCond() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseif_cond.getIfBlock());
		
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		Assert.assertEquals("and(neq(state1,1),eq(state0,1))", nestedIf.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_elseif_nestedCondSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseif_cond.getIfBlock());

		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		VerilogBlock nested_block = VerilogTestUtility.getVerilogBlock(nestedIf);
		Assert.assertEquals(1, nested_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const3_elseif_nestedCondAssign() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseif_cond.getIfBlock());
		
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		VerilogBlock nested_block = VerilogTestUtility.getVerilogBlock(nestedIf);
		AbstractVerilogConstruct nest_cst = VerilogTestUtility.getBlockConstruct(nested_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(nest_cst);
		Assert.assertEquals("state1", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const3_elseif2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		Assert.assertEquals("and(eq(a1,1),eq(b0,1))", elseif2_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_elseif2CstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif2_cond.getIfBlock());
		Assert.assertEquals(1, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const3_elseif2_nestedCond() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseif2_cond.getIfBlock());

		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		Assert.assertEquals("and(eq(state1,1),eq(state0,1))", nestedIf.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_elseif2_nestedCondSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseif2_cond.getIfBlock());

		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		VerilogBlock nested_block = VerilogTestUtility.getVerilogBlock(nestedIf);
		Assert.assertEquals(1, nested_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const3_elseif2_nestedCondAssign() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseif2_cond.getIfBlock());

		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		VerilogBlock nested_block = VerilogTestUtility.getVerilogBlock(nestedIf);
		AbstractVerilogConstruct nest_cst = VerilogTestUtility.getBlockConstruct(nested_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(nest_cst);
		Assert.assertEquals("state0", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const3_elseif3() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		Assert.assertEquals("and(eq(a1,1),eq(b1,1))", elseif3_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_elseif3CstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif3_cond.getIfBlock());
		Assert.assertEquals(1, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const3_elseif3_nestedCond() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseif3_cond.getIfBlock());

		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		Assert.assertEquals("and(eq(state1,1),neq(state0,1))", nestedIf.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_elseif3_nestedCondSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseif3_cond.getIfBlock());

		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		VerilogBlock nested_block = VerilogTestUtility.getVerilogBlock(nestedIf);
		Assert.assertEquals(1, nested_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const3_elseif3_nestedCondAssign() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseif3_cond.getIfBlock());

		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogConditional nestedIf = VerilogTestUtility.getConditionalConstruct(if_cst);
		VerilogBlock nested_block = VerilogTestUtility.getVerilogBlock(nestedIf);
		AbstractVerilogConstruct nest_cst = VerilogTestUtility.getBlockConstruct(nested_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(nest_cst);
		Assert.assertEquals("state1", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const4_wait() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 3);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		Assert.assertEquals("or(and(eq(a0,1),and(eq(b0,1),and(neq(state1,1),eq(state0,1)))),"+
			     "or(and(eq(a0,1),and(eq(b1,1),and(eq(state1,1),eq(state0,1)))),"+
					 "or(and(eq(a1,1),and(eq(b0,1),and(eq(state1,1),neq(state0,1)))),"+
					 "and(eq(a1,1),and(eq(b1,1),and(neq(state1,1),neq(state0,1)))))))", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_imp_const4_delay() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 3);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const5_wait() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 4);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		Assert.assertEquals("neq(req,1)", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_imp_const5_delay() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 4);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const6_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(a0,1)", actual_cond.getIfCondition());
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
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const6_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("a0", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const7_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(a1,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const7_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
		
	@Test
	public void Test_imp_const7_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const7_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("a1", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const8_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 7);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(b0,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const8_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 7);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
		
	@Test
	public void Test_imp_const8_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 7);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const8_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 7);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("b0", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const9_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 8);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(b1,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const9_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 8);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
		
	@Test
	public void Test_imp_const9_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 8);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const9_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 8);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("b1", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const10() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 9);
		Assert.assertNotNull(construct);
		
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(construct);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const11() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 10);
		Assert.assertNotNull(construct);
		
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(construct);
		Assert.assertEquals("ack", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 0);
		Assert.assertNotNull(alwaysConstruct);
	
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(alwaysConstruct);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(alwaysConstruct);
		Assert.assertEquals("req", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const3_wait() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 2);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		Assert.assertEquals("eq(ack,1)", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_tb_const3_delay() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 2);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const4() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 3);
		Assert.assertNotNull(alwaysConstruct);
	
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(alwaysConstruct);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const5() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(alwaysConstruct);
		Assert.assertEquals("req", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
		
	@Test
	public void Test_tb_const6_wait() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 5);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		Assert.assertEquals("neq(ack,1)", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_tb_const6_delay() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 5);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
}