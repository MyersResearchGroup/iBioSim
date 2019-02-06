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
public class VerilogParserEvenZeroes_Test {

	private static VerilogModule verilog_imp, verilog_tb;
	private static VerilogBlock imp_alwaysblk, tb_alwaysblk;

	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		CompilerOptions setupOpt = new CompilerOptions();
		setupOpt.addVerilogFile(TestingFiles.verilogEvenZero_impFile);
		setupOpt.addVerilogFile(TestingFiles.verilogEvenZero_tbFile);
	
		VerilogCompiler compiledVerilog = VerilogRunner.compile(setupOpt.getVerilogFiles());
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(2, moduleList.size());
		
		verilog_imp = moduleList.get("evenzeroes_imp");
		Assert.assertNotNull(verilog_imp);
		imp_alwaysblk = verilog_imp.getAlwaysBlock(0);
		
		verilog_tb = moduleList.get("evenzeroes_testbench");
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
		List<String> expected_inputs = Arrays.asList("bit0", "bit1");
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
		List<String> expected_reg = Arrays.asList("parity0", "parity1");
		Assert.assertEquals(expected_reg, actual_wires);
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
		Assert.assertEquals("evenzeroes_imp", submodule.getModuleReference());
	}
	
	@Test
	public void Test_tb_submodInstance() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);
		Assert.assertEquals("ez_instance", submodule.getSubmoduleId());
	}
	
	@Test
	public void Test_tb_subModConnSize() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);
		Assert.assertTrue(submodule.getNumNamedConnections() == 4);
	}
	
	@Test
	public void Test_tb_subModConnections() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);

		Map<String, String> expectedConnections =  new HashMap<>();
		expectedConnections.put("bit0", "bit0");
		expectedConnections.put("bit1", "bit1");
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
		Assert.assertEquals("or(eq(bit0,1),eq(bit1,1))", actual_wait.getWaitExpression());
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
		Assert.assertEquals("or(and(neq(state,1),eq(bit1,1)),and(eq(state,1),eq(bit0,1)))", actual_cond.getIfCondition());
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
		Assert.assertEquals("5", actual_delay.getDelayValue());
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
		Assert.assertEquals(2, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const2_elseCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(else_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_imp_const2_elseCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 1);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(else_cst);
		Assert.assertEquals("parity0", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const3_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("eq(parity1,1)", actual_cond.getIfCondition());
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
	public void Test_imp_const3_ifCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_cond);
		AbstractVerilogConstruct if_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(if_cst);
		Assert.assertEquals("state", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const3_elseCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(1, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_imp_const3_elseCst() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(else_cst);
		Assert.assertEquals("state", actual_assign.getVariable());
		Assert.assertEquals("1", actual_assign.getExpression());
	}
	
	@Test
	public void Test_imp_const4_wait() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(imp_alwaysblk, 3);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		Assert.assertEquals("or(and(eq(parity0,1),eq(state,1)),and(eq(parity1,1),neq(state,1)))", actual_wait.getWaitExpression());
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
		Assert.assertEquals("and(neq(bit0,1),neq(bit1,1))", actual_wait.getWaitExpression());
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
		Assert.assertEquals("5", actual_delay.getDelayValue());
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
		Assert.assertEquals("5", actual_delay.getDelayValue());
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
	public void Test_tb_const3_if() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 2);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional actual_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		Assert.assertEquals("neq(next,1)", actual_cond.getIfCondition());
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
		Assert.assertEquals("5", actual_delay.getDelayValue());
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
		Assert.assertEquals("5", actual_delay.getDelayValue());
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
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 3);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		Assert.assertEquals("or(eq(parity0,1),eq(parity1,1))", actual_wait.getWaitExpression());
	}
	
	@Test
	public void Test_tb_const4_delay() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 3);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(actual_wait);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
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
		Assert.assertEquals("5", actual_delay.getDelayValue());
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
	public void Test_tb_const5_elseCstSize() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		Assert.assertEquals(2, else_block.getNumConstructSize());
	}
	
	@Test
	public void Test_tb_const5_elseCst1() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		VerilogDelay actual_delay = VerilogTestUtility.getDelayConstruct(else_cst);	
		Assert.assertNotNull(actual_delay);
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void Test_tb_const5_elseCst2() {
		AbstractVerilogConstruct alwaysConstruct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 4);
		Assert.assertNotNull(alwaysConstruct);
		
		VerilogConditional if_cond = VerilogTestUtility.getConditionalConstruct(alwaysConstruct);
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_cond.getElseBlock());
		AbstractVerilogConstruct else_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		VerilogAssignment actual_assign = VerilogTestUtility.getVerilogAssignment(else_cst);
		Assert.assertEquals("bit1", actual_assign.getVariable());
		Assert.assertEquals("0", actual_assign.getExpression());
	}
	
	@Test
	public void Test_tb_const6_wait() {
		AbstractVerilogConstruct construct = VerilogTestUtility.getBlockConstruct(tb_alwaysblk, 5);
		Assert.assertNotNull(construct);
		
		VerilogWait actual_wait = VerilogTestUtility.getWaitConstruct(construct);
		Assert.assertEquals("and(neq(parity0,1),neq(parity1,1))", actual_wait.getWaitExpression());
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