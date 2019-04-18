package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogInitialBlock;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModuleInstance;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogWait;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParserExample9_Test {

	private static VerilogModule verilogModule;
	
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		VerilogParser compiledVerilog = new VerilogParser();
		verilogModule = compiledVerilog.parseVerilogFile(new File(TestingFiles.verilogDelay_file));
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
		Assert.assertTrue(actual_outputPorts.size() == 2);
		Assert.assertEquals("out0", actual_outputPorts.get(0));
		Assert.assertEquals("out1", actual_outputPorts.get(1));
	}
	
	@Test
	public void TestVerilog_registers() {
		List<String> actual_registers = verilogModule.getRegisters();
		Assert.assertNotNull(actual_registers);
		Assert.assertTrue(actual_registers.size() == 1);
		Assert.assertEquals("next", actual_registers.get(0));
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
		Assert.assertEquals(1, verilogModule.getNumAlwaysBlock());
		VerilogBlock alwaysBlock = verilogModule.getAlwaysBlock(0);
		Assert.assertNotNull(alwaysBlock);
		
		List<AbstractVerilogConstruct> alwaysConstructs = alwaysBlock.getBlockConstructs();
		Assert.assertEquals(3, alwaysConstructs.size());
	}
	
	@Test
	public void TestVerilog_construct1() {
		VerilogBlock block = verilogModule.getAlwaysBlock(0);
		AbstractVerilogConstruct actual_construct = VerilogTestUtility.getBlockConstruct(block, 0);
		Assert.assertNotNull(actual_construct);
		Assert.assertTrue(actual_construct instanceof VerilogDelay);
		
		VerilogDelay actual_delay = (VerilogDelay) actual_construct;
		Assert.assertEquals("5", actual_delay.getDelayValue());
	}
	
	@Test
	public void TestVerilog_construct2() {
		VerilogBlock block = verilogModule.getAlwaysBlock(0);
		AbstractVerilogConstruct actual_construct = VerilogTestUtility.getBlockConstruct(block, 1);
		Assert.assertNotNull(actual_construct);
		Assert.assertTrue(actual_construct instanceof VerilogAssignment);
		
		VerilogAssignment actual_assignment = (VerilogAssignment) actual_construct;
		Assert.assertEquals("piecewise(1, uniform(0,1)  < 0.5, 0)", actual_assignment.getExpression());
		Assert.assertEquals("next", actual_assignment.getVariable());
	}
	
	@Test
	public void TestVerilog_construct3() {
		VerilogBlock block = verilogModule.getAlwaysBlock(0);
		AbstractVerilogConstruct always_cst = VerilogTestUtility.getBlockConstruct(block, 2);
		Assert.assertNotNull(always_cst);
		
		VerilogConditional if_ = VerilogTestUtility.getConditionalConstruct(always_cst);
		Assert.assertEquals("eq(next,0)", if_.getIfCondition());
		
		VerilogBlock if_block = VerilogTestUtility.getVerilogBlock(if_);
		Assert.assertEquals(2, if_block.getNumConstructSize());
		AbstractVerilogConstruct act_cst = VerilogTestUtility.getBlockConstruct(if_block, 0);
		VerilogAssignment actual_assignment = VerilogTestUtility.getVerilogAssignment(act_cst);
		Assert.assertEquals("out0", actual_assignment.getVariable());
		Assert.assertEquals("1", actual_assignment.getExpression());
		
		act_cst = VerilogTestUtility.getBlockConstruct(if_block, 1);
		VerilogWait waitCondition = VerilogTestUtility.getWaitConstruct(act_cst);
		Assert.assertEquals("eq(out0,1)",waitCondition.getWaitExpression());	
		
		AbstractVerilogConstruct waitConstruct = waitCondition.getDelayConstruct();
		Assert.assertNotNull(waitConstruct);
		VerilogDelay delay = (VerilogDelay) waitConstruct;
		Assert.assertEquals("5", delay.getDelayValue());
		
		VerilogBlock else_block = VerilogTestUtility.getVerilogBlock(if_.getElseBlock());
		Assert.assertEquals(2, else_block.getNumConstructSize());
		
		act_cst = VerilogTestUtility.getBlockConstruct(else_block, 0);
		actual_assignment = VerilogTestUtility.getVerilogAssignment(act_cst);
		Assert.assertEquals("out1", actual_assignment.getVariable());
		Assert.assertEquals("1", actual_assignment.getExpression());
		
		act_cst = VerilogTestUtility.getBlockConstruct(else_block, 1);
		waitCondition = VerilogTestUtility.getWaitConstruct(act_cst);
		Assert.assertEquals("eq(out1,1)",waitCondition.getWaitExpression());	
		
		waitConstruct = waitCondition.getDelayConstruct();
		Assert.assertNotNull(waitConstruct);
		delay = (VerilogDelay) waitConstruct;
		Assert.assertEquals("5", delay.getDelayValue());
	}
	
	
}