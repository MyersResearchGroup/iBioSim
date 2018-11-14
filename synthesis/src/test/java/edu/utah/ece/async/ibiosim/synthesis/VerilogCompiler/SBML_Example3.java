package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.comp.CompModelPlugin;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class SBML_Example3 extends AbstractVerilogParserTest{
	
	private static Model sbmlModel;
	
	@BeforeClass
	public static void setupTest() {
		String[] cmd = {"-v", reader.getFile("register.v"), "-sbml"};
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		
		WrappedSBML sbmlWrapper = compiledVerilog.getSBMLWrapper("registers");
		Assert.assertNotNull(sbmlWrapper);
		sbmlModel = sbmlWrapper.getModel();
		Assert.assertNotNull(sbmlModel);
		Assert.assertEquals("registers", sbmlModel.getId());
	}

	@Test
	public void TestSBML_ports() {
		CompModelPlugin compPlugin = (CompModelPlugin) sbmlModel.getPlugin("comp");
		Assert.assertEquals(0, compPlugin.getNumPorts());
	}
	
	@Test 
	public void TestSBML_parameters() {
		Assert.assertEquals(3, sbmlModel.getNumParameters());
		Set<String> expectedIds = new HashSet<>(Arrays.asList("r1", "r2", "r3"));
		
		for(Parameter actualParam : sbmlModel.getListOfParameters()) {
			Assert.assertFalse(actualParam.getConstant());
			Assert.assertTrue(expectedIds.contains(actualParam.getId()));
			Assert.assertEquals(602, actualParam.getSBOTerm());
			Assert.assertFalse(actualParam.getConstant());
			Assert.assertTrue(actualParam.getValue() == 0);
			
		}
	}
	
	@Test
	public void TestSBML_initialAssignments() {
		Assert.assertEquals(0, sbmlModel.getNumInitialAssignments());
	}
	
	@Test
	public void TestSBML_eventSize() {
		Assert.assertEquals(0, sbmlModel.getNumEvents());
	}
}
