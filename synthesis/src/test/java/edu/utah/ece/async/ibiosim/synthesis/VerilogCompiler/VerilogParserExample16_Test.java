package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VerilogConstructs.VerilogModule;
import VerilogConstructs.VerilogModuleInstance;

/**
 * Test Verilog port connections with differt port name when connecting to submodule
 * @author Tramy Nguyen
 */
public class VerilogParserExample16_Test {
	
	private static VerilogModule verilogModule;

	@BeforeClass
	public static void setupTest() {

		String[] cmd = {"-v", CompilerTestSuite.verilogPortMapping_file};

		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());

		verilogModule = moduleList.get("portMapping");
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
