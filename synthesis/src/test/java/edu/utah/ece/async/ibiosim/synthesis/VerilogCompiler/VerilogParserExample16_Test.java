package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;
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
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModuleInstance;

/**
 * Test Verilog port connections with differt port name when connecting to submodule
 * @author Tramy Nguyen
 */
public class VerilogParserExample16_Test {
	
	private static VerilogModule verilogModule;

	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		VerilogParser compiledVerilog = new VerilogParser();
		verilogModule = compiledVerilog.parseVerilogFile(new File(TestingFiles.verilogPortMapping_File));
		Assert.assertNotNull(verilogModule);
	}

	@Test
	public void TestVerilog_registersSize() {
		List<String> actual_registers = verilogModule.getRegisters();
		Assert.assertNotNull(actual_registers);
		Assert.assertTrue(actual_registers.size() == 2);
	}
	
	@Test
	public void TestVerilog_registers() {
		List<String> actual_registers = verilogModule.getRegisters();
		Assert.assertNotNull(actual_registers);
		Assert.assertEquals("IPTG", actual_registers.get(0));
		Assert.assertEquals("aTc", actual_registers.get(1));
	}
	
	@Test
	public void Test_submodRef() {
		VerilogModuleInstance submodule = verilogModule.getSubmodule(0);
		Assert.assertEquals("srLatch_imp", submodule.getModuleReference());
	}
	
	@Test
	public void Test_submodInstance() {
		VerilogModuleInstance submodule = verilogModule.getSubmodule(0);
		Assert.assertEquals("srLatch_instance", submodule.getSubmoduleId());
	}
	
	@Test
	public void Test_subModConnSize() {
		VerilogModuleInstance submodule = verilogModule.getSubmodule(0);
		Assert.assertTrue(submodule.getNumNamedConnections() == 3);
	}
	
	@Test
	public void Test_subModConnections() {
		VerilogModuleInstance submodule = verilogModule.getSubmodule(0);

		Map<String, String> expectedConnections = new HashMap<>();
		expectedConnections.put("IPTG", "r");
		expectedConnections.put("aTc", "s");
		expectedConnections.put("GFP", "q");

		Map<String, String> portConnections = submodule.getNamedConnections();
		for(Map.Entry<String, String> actualConnection: portConnections.entrySet()) {
			Assert.assertTrue(expectedConnections.containsKey(actualConnection.getKey()));
			Assert.assertEquals(expectedConnections.get(actualConnection.getKey()), actualConnection.getValue());
		}
	}
	
	
}
