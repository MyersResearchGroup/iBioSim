package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.Port;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class SBML_Example1 extends AbstractVerilogParserTest{
	
	private static Model sbmlModel;
	
	@BeforeClass
	public static void setupTest() {
		
		String[] cmd = {"-v", CompilerTestSuite.verilogInitBlock_file, "-sbml"};
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
	
		WrappedSBML sbmlWrapper = compiledVerilog.getSBMLWrapper("init_block");
		Assert.assertNotNull(sbmlWrapper);		
		
		sbmlModel = sbmlWrapper.getModel();
		Assert.assertNotNull(sbmlModel);
		Assert.assertEquals("init_block", sbmlModel.getId());
	}
	
	@Test
	public void TestSBML_ports() {
		CompModelPlugin compPlugin = (CompModelPlugin) sbmlModel.getPlugin("comp");
		Assert.assertEquals(2, compPlugin.getNumPorts());
		for(Port actual_port : compPlugin.getListOfPorts()) {
    		Assert.assertTrue(actual_port.isSetSBOTerm());
    		int sbo = actual_port.getSBOTerm();
    		String[] type_idref = actual_port.getId().split("__");
    		Assert.assertTrue(type_idref.length == 2);
    		Assert.assertTrue(type_idref[0].startsWith("init_block"));
    		if(sbo == 600) {
    			Assert.assertEquals(type_idref[1], "in0");
    			Assert.assertEquals(actual_port.getIdRef(), "in0");
    		} 
    		else if(sbo == 601) {
    			Assert.assertEquals(type_idref[1], "out0");
    			Assert.assertEquals(actual_port.getIdRef(), "out0");
    		}
    		
    	}
	}
	
	@Test 
	public void TestSBML_parameters() {
		Assert.assertEquals(3, sbmlModel.getNumParameters());
		Set<String> expectedIds = new HashSet<>(Arrays.asList("in0", "out0", "state"));
		
		for(Parameter actualParam : sbmlModel.getListOfParameters()) {
			Assert.assertFalse(actualParam.getConstant());
			Assert.assertEquals(602, actualParam.getSBOTerm());
			Assert.assertTrue(actualParam.getValue() == 0);
			Assert.assertTrue(expectedIds.contains(actualParam.getId()));
			Assert.assertTrue(actualParam.getValue() == 0);
		}
	}
	
	@Test
	public void TestSBML_initialAssignments() {
		Assert.assertEquals(2, sbmlModel.getNumInitialAssignments());
		
		HashMap<String, Integer> expected_results = new HashMap<>();
		expected_results.put("out0", 0);
		expected_results.put("state", 1);
		
		for(InitialAssignment actual_initAssignment : sbmlModel.getListOfInitialAssignments()) {
			Assert.assertTrue(expected_results.containsKey(actual_initAssignment.getVariable()));
			Assert.assertTrue(actual_initAssignment.getMath().isInteger());
			Assert.assertTrue(expected_results.get(actual_initAssignment.getVariable()) == actual_initAssignment.getMath().getInteger());
		}
	}
	
	@Test
	public void TestSBML_eventSize() {
		Assert.assertEquals(0, sbmlModel.getNumEvents());
	}
}