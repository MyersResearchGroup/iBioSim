package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogInitialBlock;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModuleInstance;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParserExample1_Test {
	
	private static VerilogModule verilogModule;
	
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		VerilogParser compiledVerilog = new VerilogParser();
		verilogModule = compiledVerilog.parseVerilogFile(new File(TestingFiles.verilogInitBlock_File));
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