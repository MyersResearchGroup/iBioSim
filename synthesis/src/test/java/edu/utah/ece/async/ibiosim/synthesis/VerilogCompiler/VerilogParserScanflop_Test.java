package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

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
public class VerilogParserScanflop_Test {
	
	private static VerilogModule verilog_imp, verilog_tb;
	private static VerilogBlock imp_alwaysblk, tb_alwaysblk;
	
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		CompilerOptions setupOpt = new CompilerOptions();
		setupOpt.addVerilogFile(TestingFiles.verilogScanflop_impFile);
		setupOpt.addVerilogFile(TestingFiles.verilogScanflop_tbFile);
	
		VerilogCompiler compiledVerilog = new VerilogCompiler(setupOpt.getVerilogFiles());
		compiledVerilog.parseVerilog();
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(2, moduleList.size());
		
		verilog_imp = moduleList.get("scanflop_imp");
		Assert.assertNotNull(verilog_imp);
		imp_alwaysblk = verilog_imp.getAlwaysBlock(0);
		
		verilog_tb = moduleList.get("scanflop_testbench");
		Assert.assertNotNull(verilog_tb);
		tb_alwaysblk = verilog_tb.getAlwaysBlock(0);
	}
	
	@Test
	public void Test_imp_inputSize() {
		Assert.assertEquals(7, verilog_imp.getNumInputs());
	}
	
	@Test
	public void Test_imp_inputs() {
		List<String> actual_inputPorts = verilog_imp.getInputPorts();
		List<String> expected_inputs = Arrays.asList("in1_0", "in1_1", "in2_0", "in2_1", "sel0", "sel1", "req");
		Assert.assertEquals(expected_inputs, actual_inputPorts);
	}
	
	@Test
	public void Test_tb_inputSize() {
		Assert.assertEquals(0, verilog_tb.getNumInputs());
	}
	
	@Test
	public void Test_imp_outputSize() {
		Assert.assertEquals(3, verilog_imp.getNumOutputs());
	}
	
	@Test
	public void Test_imp_outputs() {
		List<String> actual_outputPorts = verilog_imp.getOutputPorts();
		List<String> expected_outputs = Arrays.asList("q1", "q0", "ack");
		Assert.assertEquals(expected_outputs, actual_outputPorts);
	}
	
	@Test
	public void Test_tb_outputSize() {
		Assert.assertEquals(0, verilog_tb.getNumOutputs());
	}
	
	@Test
	public void Test_imp_regSize() {
		Assert.assertEquals(1, verilog_imp.getNumRegisters());
	}
	
	@Test
	public void Test_imp_reg() {
		List<String> actual_registers = verilog_imp.getRegisters();
		List<String> expected_reg = Arrays.asList("state");
		Assert.assertEquals(expected_reg, actual_registers);
	}

	@Test
	public void Test_tb_regSize() {
		Assert.assertEquals(10, verilog_tb.getNumRegisters());
	}
	
	@Test
	public void Test_tb_registers() {
		List<String> actual_registers = verilog_tb.getRegisters();
		List<String> expected_reg = Arrays.asList("in1_0", "in1_1", "in2_0", "in2_1", "sel0", "sel1", "next1", "next2", "next3", "req");
		Assert.assertEquals(expected_reg, actual_registers);
	}
	
	@Test 
	public void Test_imp_wireSize() {
		Assert.assertEquals(0, verilog_imp.getNumWires());
	}
	
	@Test 
	public void Test_tb_wireSize() {
		Assert.assertEquals(3, verilog_tb.getNumWires());
	}
	
	@Test 
	public void Test_tb_wires() {
		List<String> actual_wires = verilog_tb.getWirePorts();
		List<String> expected_wires = Arrays.asList("q0", "q1", "ack");
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
		Assert.assertEquals("scanflop_imp", submodule.getModuleReference());
	}
	
	@Test
	public void Test_tb_submodInstance() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);
		Assert.assertEquals("sf_instance", submodule.getSubmoduleId());
	}
	
	@Test
	public void Test_tb_subModConnSize() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);
		Assert.assertTrue(submodule.getNumNamedConnections() == 10);
	}
	
	@Test
	public void Test_tb_subModConnections() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);

		Map<String, String> expectedConnections = new HashMap<>();
		expectedConnections.put("in1_0", "in1_0");
		expectedConnections.put("in1_1", "in1_1");
		expectedConnections.put("in2_0", "in2_0");
		expectedConnections.put("in2_1", "in2_1");
		expectedConnections.put("sel0", "sel0");
		expectedConnections.put("sel1", "sel1");
		expectedConnections.put("q0", "q0");
		expectedConnections.put("q1", "q1");
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
		Assert.assertEquals(9, imp_alwaysblk.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_alwaysblkSize() {
		Assert.assertEquals(1, verilog_tb.getNumAlwaysBlock());
	}
	
	@Test
	public void Test_tb_alwaysCstSize() {
		Assert.assertEquals(18, tb_alwaysblk.getNumConstructSize());
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
	public void Test_imp_const2_cond() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("and(eq(sel0,1),eq(in1_0,1))", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_condCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(4, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_ifCstDelays() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		int[] delay_index = {0, 2};
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
		expectedAssign.put("q0", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3};
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
		Assert.assertEquals("and(eq(sel0,1),eq(in1_1,1))", elseif_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_elseifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(4, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_elseifCstDelays() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock elseif_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		int[] delay_index = {0, 2};
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
		expectedAssign.put("q1", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3};
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
		Assert.assertEquals("and(eq(sel0,1),and(eq(in1_0,0),and(eq(in1_1,0),eq(state,0))))", elseif2_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_elseif2CstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif2_cond.getIfBlock());
		Assert.assertEquals(4, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_elseif2CstDelays() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif2_cond.getIfBlock());
		int[] delay_index = {0, 2};
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
		expectedAssign.put("q0", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3};
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
		Assert.assertEquals("and(eq(sel0,1),and(eq(in1_0,0),and(eq(in1_1,0),eq(state,1))))", elseif3_cond.getIfCondition());
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
		Assert.assertEquals(4, else_block.getNumConstructSize());
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
		int[] delay_index = {0, 2};
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
		expectedAssign.put("q1", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3};
		int i = 0;
		while(i < assign_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, assign_index[i++]);
			VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
			Assert.assertTrue(expectedAssign.containsKey(actual_assign.getVariable()));
			Assert.assertEquals(expectedAssign.get(actual_assign.getVariable()), actual_assign.getExpression());
		}
	}
	
	@Test
	public void Test_imp_const2_elseif4() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		Assert.assertEquals("and(eq(sel1,1),eq(in2_0,1))", elseif4_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_elseif4CstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif4_cond.getIfBlock());
		Assert.assertEquals(4, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_elseif4CstDelays() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif4_cond.getIfBlock());
		int[] delay_index = {0, 2};
		int i = 0;
		while(i < delay_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, delay_index[i++]);
			VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
			Assert.assertNotNull(actual_delay);
			Assert.assertEquals("5", actual_delay.getDelayValue());
		}
		
	}
	
	@Test
	public void Test_imp_const2_elseif4CstAssigns() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif4_cond.getIfBlock());
		
		Map<String, String> expectedAssign = new HashMap<>();
		expectedAssign.put("q0", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3};
		int i = 0;
		while(i < assign_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, assign_index[i++]);
			VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
			Assert.assertTrue(expectedAssign.containsKey(actual_assign.getVariable()));
			Assert.assertEquals(expectedAssign.get(actual_assign.getVariable()), actual_assign.getExpression());
		}
	}
	
	@Test
	public void Test_imp_const2_elseif5() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		Assert.assertEquals("and(eq(sel1,1),eq(in2_1,1))", elseif5_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_elseif5CstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif5_cond.getIfBlock());
		Assert.assertEquals(4, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_elseif5CstDelays() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif5_cond.getIfBlock());
		int[] delay_index = {0, 2};
		int i = 0;
		while(i < delay_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, delay_index[i++]);
			VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
			Assert.assertNotNull(actual_delay);
			Assert.assertEquals("5", actual_delay.getDelayValue());
		}
		
	}
	
	@Test
	public void Test_imp_const2_elseif5CstAssigns() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif5_cond.getIfBlock());
		
		Map<String, String> expectedAssign = new HashMap<>();
		expectedAssign.put("q1", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3};
		int i = 0;
		while(i < assign_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, assign_index[i++]);
			VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
			Assert.assertTrue(expectedAssign.containsKey(actual_assign.getVariable()));
			Assert.assertEquals(expectedAssign.get(actual_assign.getVariable()), actual_assign.getExpression());
		}
	}
	
	@Test
	public void Test_imp_const2_elseif6() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		VerilogConditional elseif6_cond = VerilogTestUtility.getConditionalConstruct(elseif5_cond.getElseBlock());
		Assert.assertEquals("and(eq(sel1,1),and(eq(in2_0,0),and(eq(in2_1,0),eq(state,0))))", elseif6_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_elseif6CstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		VerilogConditional elseif6_cond = VerilogTestUtility.getConditionalConstruct(elseif5_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif6_cond.getIfBlock());
		Assert.assertEquals(4, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_elseif6CstDelays() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		VerilogConditional elseif6_cond = VerilogTestUtility.getConditionalConstruct(elseif5_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif6_cond.getIfBlock());
		int[] delay_index = {0, 2};
		int i = 0;
		while(i < delay_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, delay_index[i++]);
			VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
			Assert.assertNotNull(actual_delay);
			Assert.assertEquals("5", actual_delay.getDelayValue());
		}
		
	}
	
	@Test
	public void Test_imp_const2_elseif6CstAssigns() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		VerilogConditional elseif6_cond = VerilogTestUtility.getConditionalConstruct(elseif5_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif6_cond.getIfBlock());
		
		Map<String, String> expectedAssign = new HashMap<>();
		expectedAssign.put("q0", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3};
		int i = 0;
		while(i < assign_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, assign_index[i++]);
			VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
			Assert.assertTrue(expectedAssign.containsKey(actual_assign.getVariable()));
			Assert.assertEquals(expectedAssign.get(actual_assign.getVariable()), actual_assign.getExpression());
		}
	}
	
	@Test
	public void Test_imp_const2_elseif7() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		VerilogConditional elseif6_cond = VerilogTestUtility.getConditionalConstruct(elseif5_cond.getElseBlock());
		VerilogConditional elseif7_cond = VerilogTestUtility.getConditionalConstruct(elseif6_cond.getElseBlock());
		Assert.assertEquals("and(eq(sel1,1),and(eq(in2_0,0),and(eq(in2_1,0),eq(state,1))))", elseif7_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const2_elseif7CstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		VerilogConditional elseif6_cond = VerilogTestUtility.getConditionalConstruct(elseif5_cond.getElseBlock());
		VerilogConditional elseif7_cond = VerilogTestUtility.getConditionalConstruct(elseif6_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif7_cond.getIfBlock());
		Assert.assertEquals(4, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_elseif7CstDelays() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		VerilogConditional elseif6_cond = VerilogTestUtility.getConditionalConstruct(elseif5_cond.getElseBlock());
		VerilogConditional elseif7_cond = VerilogTestUtility.getConditionalConstruct(elseif6_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif7_cond.getIfBlock());
		int[] delay_index = {0, 2};
		int i = 0;
		while(i < delay_index.length){
			AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(else_block, delay_index[i++]);
			VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
			Assert.assertNotNull(actual_delay);
			Assert.assertEquals("5", actual_delay.getDelayValue());
		}
		
	}
	
	@Test
	public void Test_imp_const2_elseif7CstAssigns() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogConditional elseif2_cond = VerilogTestUtility.getConditionalConstruct(elseif_cond.getElseBlock());
		VerilogConditional elseif3_cond = VerilogTestUtility.getConditionalConstruct(elseif2_cond.getElseBlock());
		VerilogConditional elseif4_cond = VerilogTestUtility.getConditionalConstruct(elseif3_cond.getElseBlock());
		VerilogConditional elseif5_cond = VerilogTestUtility.getConditionalConstruct(elseif4_cond.getElseBlock());
		VerilogConditional elseif6_cond = VerilogTestUtility.getConditionalConstruct(elseif5_cond.getElseBlock());
		VerilogConditional elseif7_cond = VerilogTestUtility.getConditionalConstruct(elseif6_cond.getElseBlock());
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(elseif7_cond.getIfBlock());
		
		Map<String, String> expectedAssign = new HashMap<>();
		expectedAssign.put("q1", "1");
		expectedAssign.put("ack", "1");

		int[] assign_index = {1, 3};
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
		Assert.assertEquals("and(eq(q0,1),eq(state,1))", actual_cond.getIfCondition());
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
	public void Test_imp_const3_ifCst() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		
		Map<String, String> expectedAssign = new HashMap<>();
		expectedAssign.put("state", "0");

		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertTrue(expectedAssign.containsKey(actual_assign.getVariable()));
		Assert.assertEquals(expectedAssign.get(actual_assign.getVariable()), actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const3_elseif() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		Assert.assertEquals("and(eq(q1,1),eq(state,0))", elseif_cond.getIfCondition());
	}
	
	@Test
	public void Test_imp_const3_elseifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseif_cond);
		Assert.assertEquals(1, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const3_elseifCst() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogConditional elseif_cond = VerilogTestUtility.getConditionalConstruct(if_cond.getElseBlock());
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(elseif_cond);
		
		Map<String, String> expectedAssign = new HashMap<>();
		expectedAssign.put("state", "1");

		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertTrue(expectedAssign.containsKey(actual_assign.getVariable()));
		Assert.assertEquals(expectedAssign.get(actual_assign.getVariable()), actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const4_wait() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 3);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		Assert.assertEquals("or(and(eq(q0,1),neq(state,1)),and(eq(q1,1),eq(state,1)))", actual_wait.getWaitExpression());
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
		Assert.assertEquals("eq(q0,1)", actual_cond.getIfCondition());
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
		Assert.assertEquals("q0", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const7_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(q1,1)", actual_cond.getIfCondition());
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
		Assert.assertEquals("q1", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const8() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 7);
		Assert.assertNotNull(construct);
		
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(construct);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const9() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 8);
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
		Assert.assertEquals("next1", actual_assign.getVariable());
		Assert.assertEquals("piecewise(1, uniform(0,1)  < 0.5, 0)", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const3() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
	
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(alwaysConstruct);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const4() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 3);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(alwaysConstruct);
		Assert.assertEquals("next2", actual_assign.getVariable());
		Assert.assertEquals("piecewise(1, uniform(0,1)  < 0.5, 0)", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const5() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
	
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(alwaysConstruct);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const6() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 5);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(alwaysConstruct);
		Assert.assertEquals("next3", actual_assign.getVariable());
		Assert.assertEquals("piecewise(1, uniform(0,1)  < 0.5, 0)", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const7_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("neq(next1,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_tb_const7_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const7_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const7_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("in1_0", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const7_elseCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(2, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const7_elseCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(else_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const7_elseCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 6);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(else_cst);
		Assert.assertEquals("in1_1", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const8_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 7);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("neq(next2,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_tb_const8_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 7);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const8_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 7);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const8_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 7);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("in2_0", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const8_elseCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 7);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(2, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const8_elseCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 7);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(else_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const8_elseCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 7);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(else_cst);
		Assert.assertEquals("in2_1", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const9_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 8);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("neq(next3,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_tb_const9_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 8);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const9_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 8);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const9_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 8);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("sel0", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const9_elseCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 8);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(2, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const9_elseCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 8);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(else_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const9_elseCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 8);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(else_cst);
		Assert.assertEquals("sel1", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const10_delay() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 9);
		Assert.assertNotNull(construct);
		
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(construct);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const11() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 10);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(alwaysConstruct);
		Assert.assertEquals("req", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const12_wait() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 11);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		Assert.assertEquals("eq(ack,1)", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_tb_const12_delay() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 11);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const13_delay() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 12);
		Assert.assertNotNull(construct);
		
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(construct);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const14() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 13);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(alwaysConstruct);
		Assert.assertEquals("req", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const15_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 14);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(in1_0,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_tb_const15_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 14);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const15_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 14);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const15_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 14);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("in1_0", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const15_elseCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 14);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(2, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const15_elseCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 14);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(else_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const15_elseCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 14);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(else_cst);
		Assert.assertEquals("in1_1", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const16_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 15);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(in2_0,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_tb_const16_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 15);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const16_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 15);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const16_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 15);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("in2_0", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const16_elseCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 15);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(2, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const16_elseCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 15);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(else_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const16_elseCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 15);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(else_cst);
		Assert.assertEquals("in2_1", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const17_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 16);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(sel0,1)", actual_cond.getIfCondition());
	}
	
	@Test
	public void Test_tb_const17_ifCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 16);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		Assert.assertEquals(2, if_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const17_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 16);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(if_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const17_ifCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 16);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("sel0", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const17_elseCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 16);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(2, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const17_elseCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 16);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(else_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const17_elseCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 16);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(else_cst);
		Assert.assertEquals("sel1", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const18_wait() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 17);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		Assert.assertEquals("neq(ack,1)", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_tb_const18_delay() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 17);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
}