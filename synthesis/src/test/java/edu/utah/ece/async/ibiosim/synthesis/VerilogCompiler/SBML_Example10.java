package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Arrays;
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
public class SBML_Example10 extends AbstractVerilogParserTest{
	
	private static Model sbmlModel;

	@BeforeClass
	public static void setupTest() {
		String[] cmd = {"-v", reader.getFile("init_block.v"), "-sbml"};
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
		
		Port actual_port = compPlugin.getPort(0);
		Assert.assertEquals("init_block__in0", actual_port.getId());
		Assert.assertTrue(actual_port.getSBOTerm() == 600);
		
		actual_port = compPlugin.getPort(1);
		Assert.assertEquals("init_block__out0", actual_port.getId());
		Assert.assertTrue(actual_port.getSBOTerm() == 601);
	}
	
	@Test 
	public void TestSBML_parameters() {
		Assert.assertEquals(3, sbmlModel.getNumParameters());
		Set<String> expectedIds = new HashSet<>(Arrays.asList("in0", "out0", "state"));
		
		for(Parameter actualParam : sbmlModel.getListOfParameters()) {
			Assert.assertFalse(actualParam.getConstant());
			Assert.assertTrue(expectedIds.contains(actualParam.getId()));
			Assert.assertTrue(actualParam.getSBOTerm() == 602 || actualParam.getSBOTerm() == 593);
			Assert.assertFalse(actualParam.getConstant());
			Assert.assertTrue(actualParam.getValue() == 0);
		}
	}
	
	@Test
	public void TestSBML_initialAssignments() {
		Assert.assertEquals(2, sbmlModel.getNumInitialAssignments());
		InitialAssignment actual_initAssign = sbmlModel.getInitialAssignment(0);
		Assert.assertEquals("out0", actual_initAssign.getVariable());
		Assert.assertTrue(actual_initAssign.getMath().isInteger());
		Assert.assertEquals(0, actual_initAssign.getMath().getInteger());
		
		actual_initAssign = sbmlModel.getInitialAssignment(1);
		Assert.assertEquals("state", actual_initAssign.getVariable());
		Assert.assertTrue(actual_initAssign.getMath().isInteger());
		Assert.assertEquals(1, actual_initAssign.getMath().getInteger());
	}
	
	@Test
	public void TestSBML_eventSize() {
		Assert.assertEquals(0, sbmlModel.getNumEvents());
	}
	
}