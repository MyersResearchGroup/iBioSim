package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.IOException;
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
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogInitialBlock;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModuleInstance;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParserExample2_Test {
	
	private static VerilogModule verilogModule;
	
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		CompilerOptions setupOpt = new CompilerOptions();
		setupOpt.addVerilogFile(TestingFiles.verilogAlwaysBlock_file);
	
		VerilogCompiler compiledVerilog = VerilogRunner.compile(setupOpt.getVerilogFiles());
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());
		
		verilogModule = moduleList.get("always_block");
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
		AbstractVerilogConstruct actual_construct = VerilogTestUtility.getBlockConstruct(block, 0);
		
		Assert.assertNotNull(actual_construct);
		Assert.assertTrue(actual_construct instanceof VerilogAssignment);
		
		VerilogAssignment actual_assignment = (VerilogAssignment) actual_construct;
		Assert.assertEquals("out0", actual_assignment.getVariable());
		Assert.assertEquals("or(and(state,1),and(state,not(out0)))", actual_assignment.getExpression());
	}
	
	
	
}
