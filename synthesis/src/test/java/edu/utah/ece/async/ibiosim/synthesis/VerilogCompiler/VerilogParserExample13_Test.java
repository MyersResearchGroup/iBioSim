package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VerilogConstructs.VerilogModule;

/**
 * Test parsing of urandom_range() verilog function
 * 
 * @author Tramy Nguyen
 */
public class VerilogParserExample13_Test {
	
	private static VerilogModule verilog_imp;

	@BeforeClass
	public static void setupTest() {

		String[] cmd = {"-v", CompilerTestSuite.verilogUniform1_file};

		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());

		verilog_imp = moduleList.get("system_functions");
		Assert.assertNotNull(verilog_imp);
	}

	@Test
	public void Test_contAssign() {
	
	}
}
