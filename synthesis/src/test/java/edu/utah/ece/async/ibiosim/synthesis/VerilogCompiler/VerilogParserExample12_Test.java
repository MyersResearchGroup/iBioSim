package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogModule;


/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParserExample12_Test {

	private static VerilogModule verilog_imp;
	
	@BeforeClass
	public static void setupTest() {
		
		String[] cmd = {"-v", CompilerTestSuite.verilogCont2_file};
		
		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());
		
		verilog_imp = moduleList.get("contAssign2");
		Assert.assertNotNull(verilog_imp);
	}
	
	@Test
	public void Test_contAssign() {
		VerilogAssignment actual_assign = verilog_imp.getContinuousAssignment(0);
		Assert.assertEquals("y", actual_assign.getVariable());
		Assert.assertEquals("and(a,b)", actual_assign.getExpression());
	}

}