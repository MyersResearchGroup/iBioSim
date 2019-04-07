package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.IOException;
import java.util.HashMap;
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
 * Test multiple submodules referencing the same verilog design specification.
 * @author Tramy Nguyen
 *
 */
public class VerilogParserFilter_Test {
	private static VerilogModule verilog_tb;
	
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		CompilerOptions setupOpt = new CompilerOptions();
		setupOpt.addVerilogFile(TestingFiles.verilogFilter_impFile);
		setupOpt.addVerilogFile(TestingFiles.verilogFilter_tbFile);
	
		VerilogCompiler compiledVerilog = new VerilogCompiler(setupOpt.getVerilogFiles());
		compiledVerilog.parseVerilog();
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(2, moduleList.size());
		
		verilog_tb = moduleList.get("filter_testbench");
		Assert.assertNotNull(verilog_tb);
	}
	
	@Test
	public void Test_tbSubmodelSize() {
		Assert.assertEquals(3, verilog_tb.getNumSubmodules());
	}
	
	@Test
	public void Test_tbSubmodelNames() {
		for(VerilogModuleInstance module : verilog_tb.getSubmodules()){
			Assert.assertTrue(module.getSubmoduleId().equals("cell1") || module.getSubmoduleId().equals("cell2") || module.getSubmoduleId().equals("cell3"));
			Assert.assertEquals("filter_imp", module.getModuleReference());
		}
	}
	
	@Test
	public void Test_tb_subMod1Connections() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(0);

		Map<String, String> expectedConnections = new HashMap<>();
		expectedConnections.put("Start", "Start");
		expectedConnections.put("Sensor", "Sensor");
		expectedConnections.put("QS1", "Actuator");
		
		Assert.assertTrue(expectedConnections.keySet().equals(submodule.getNamedConnections().keySet()));
		Assert.assertTrue(expectedConnections.equals(submodule.getNamedConnections()));
	}
	
	@Test
	public void Test_tb_subMod2Connections() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(1);

		Map<String, String> expectedConnections = new HashMap<>();
		expectedConnections.put("QS1", "Start");
		expectedConnections.put("Sensor", "Sensor");
		expectedConnections.put("QS2", "Actuator");
		
		Assert.assertTrue(expectedConnections.keySet().equals(submodule.getNamedConnections().keySet()));
		Assert.assertTrue(expectedConnections.equals(submodule.getNamedConnections()));
	}
	
	@Test
	public void Test_tb_subMod3Connections() {
		VerilogModuleInstance submodule = verilog_tb.getSubmodule(2);

		Map<String, String> expectedConnections = new HashMap<>();
		expectedConnections.put("QS2", "Start");
		expectedConnections.put("Sensor", "Sensor");
		expectedConnections.put("Actuator", "Actuator");
		
		Assert.assertTrue(expectedConnections.keySet().equals(submodule.getNamedConnections().keySet()));
		Assert.assertTrue(expectedConnections.equals(submodule.getNamedConnections()));
	}
}
