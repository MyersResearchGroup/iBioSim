package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;


/**
 * 
 * @author Tramy Nguyen
 *
 */
public class SBML_Example11 { 
	
	private static Model sbmlModel;

	@BeforeClass
	public static void setupTest() {
		
		String[] cmd = {"-v", CompilerTestSuite.verilogCont_file, "-sbml"};
		
		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
		
		WrappedSBML sbmlWrapper = compiledVerilog.getSBMLWrapper("contAssign");
		Assert.assertNotNull(sbmlWrapper);
		
		sbmlModel = sbmlWrapper.getModel();
		Assert.assertNotNull(sbmlModel);
		Assert.assertEquals("contAssign", sbmlModel.getId());
	}
	
	@Test
	public void TestSBML_assignRuleSize() {
		Assert.assertEquals(1, sbmlModel.getNumRules());
	}
	
	@Test
	public void TestSBML_assignRule() {
		
		AssignmentRule actualRule = sbmlModel.getAssignmentRuleByVariable("t");
		Assert.assertNotNull(actualRule);
		Assert.assertEquals("!parity0", actualRule.getMath().toString());
	}

}