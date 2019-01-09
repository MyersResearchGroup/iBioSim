package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import static org.junit.Assert.assertEquals;

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

import VerilogConstructs.AbstractVerilogConstruct;
import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogBlock;
import VerilogConstructs.VerilogInitialBlock;
import VerilogConstructs.VerilogModule;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * Test parsing of urandom_range() verilog function for varying parameters
 * @author Tramy Nguyen
 */
public class VerilogParserExample15_Test {
	
	private static VerilogModule verilogModule;

	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {

		String[] cmd = {"-v", CompilerTestSuite.verilogSystemFunc3_file};

		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());

		verilogModule = moduleList.get("system_func3");
		Assert.assertNotNull(verilogModule);
	}

	@Test
	public void TestVerilog_registers() {
		List<String> actual_registers = verilogModule.getRegisters();
		Assert.assertNotNull(actual_registers);
		Assert.assertTrue(actual_registers.size() == 4);
		Assert.assertEquals("delay1", actual_registers.get(0));
		Assert.assertEquals("delay2", actual_registers.get(1));
		Assert.assertEquals("delay3", actual_registers.get(2));
		Assert.assertEquals("delay4", actual_registers.get(3));
	}

	@Test
	public void TestVerilog_alwaysBlocks() {
		Assert.assertEquals(0, verilogModule.getAlwaysBlockList().size());
	}
	
	@Test
	public void TestVerilog_initialBlocksSize() {
		List<VerilogInitialBlock> actual_initialBlocks = verilogModule.getInitialBlockList();
		Assert.assertNotNull(actual_initialBlocks);
		Assert.assertTrue(actual_initialBlocks.size() == 1);
	}
	
	@Test
	public void TestVerilog_initialCstSize() {
		VerilogBlock actual_initialBlock = verilogModule.getInitialBlock(0);
		assertEquals(4, actual_initialBlock.getNumConstructSize());
	}
	
	@Test
	public void TestVerilog_initialConstruct1() {
		VerilogBlock actual_initialBlock = VerilogTestUtility.getVerilogBlock(verilogModule.getInitialBlock(0));
		AbstractVerilogConstruct current_construct = VerilogTestUtility.getBlockConstruct(actual_initialBlock, 0);
		
		VerilogAssignment actual_initAssignment = VerilogTestUtility.getVerilogAssignment(current_construct);
		Assert.assertEquals("delay1", actual_initAssignment.getVariable());
		Assert.assertEquals("uniform(20,30)", actual_initAssignment.getExpression());
	}
	
	@Test
	public void TestVerilog_initialConstruct2() {
		VerilogBlock actual_initialBlock = VerilogTestUtility.getVerilogBlock(verilogModule.getInitialBlock(0));
		AbstractVerilogConstruct current_construct = VerilogTestUtility.getBlockConstruct(actual_initialBlock, 1);
		
		VerilogAssignment actual_initAssignment = VerilogTestUtility.getVerilogAssignment(current_construct);
		Assert.assertEquals("delay2", actual_initAssignment.getVariable());
		Assert.assertEquals("uniform(0,20)", actual_initAssignment.getExpression());
	}
	
	@Test
	public void TestVerilog_initialConstruct3() {
		VerilogBlock actual_initialBlock = VerilogTestUtility.getVerilogBlock(verilogModule.getInitialBlock(0));
		AbstractVerilogConstruct current_construct = VerilogTestUtility.getBlockConstruct(actual_initialBlock, 2);
		
		VerilogAssignment actual_initAssignment = VerilogTestUtility.getVerilogAssignment(current_construct);
		Assert.assertEquals("delay3", actual_initAssignment.getVariable());
		Assert.assertEquals("uniform(20,30)", actual_initAssignment.getExpression());
	}
	
	@Test
	public void TestVerilog_initialConstruct4() {
		VerilogBlock actual_initialBlock = VerilogTestUtility.getVerilogBlock(verilogModule.getInitialBlock(0));
		AbstractVerilogConstruct current_construct = VerilogTestUtility.getBlockConstruct(actual_initialBlock, 3);
		
		VerilogAssignment actual_initAssignment = VerilogTestUtility.getVerilogAssignment(current_construct);
		Assert.assertEquals("delay4", actual_initAssignment.getVariable());
		Assert.assertEquals("uniform(20,20)", actual_initAssignment.getExpression());
	}
}
