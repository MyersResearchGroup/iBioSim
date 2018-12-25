package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Trigger;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.Port;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class SBML_Example2 extends AbstractVerilogParserTest{
	
	private static Model sbmlModel;
	
	@BeforeClass
	public static void setupTest() {
		String[] cmd = {"-v", CompilerTestSuite.verilogAlwaysBlock_file, "-sbml"};
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		
		WrappedSBML sbmlWrapper = compiledVerilog.getSBMLWrapper("always_block");
		Assert.assertNotNull(sbmlWrapper);
		sbmlModel = sbmlWrapper.getModel();
		Assert.assertNotNull(sbmlModel);
		Assert.assertEquals("always_block", sbmlModel.getId());
	}

	@Test
	public void TestSBML_ports() {
		CompModelPlugin compPlugin = (CompModelPlugin) sbmlModel.getPlugin("comp");
		Assert.assertEquals(1, compPlugin.getNumPorts());
		Port actual_port = compPlugin.getPort(0);
		Assert.assertEquals("always_block__out0", actual_port.getId());
		Assert.assertEquals(601, actual_port.getSBOTerm());
	}
	
	@Test 
	public void TestSBML_parameters() {
		Assert.assertEquals(4, sbmlModel.getNumParameters());
		Set<String> expectedIds = new HashSet<>(Arrays.asList("out0", "state", "P0", "P1"));
		
		for(Parameter actualParam : sbmlModel.getListOfParameters()) {
			Assert.assertFalse(actualParam.getConstant());
			Assert.assertTrue(expectedIds.contains(actualParam.getId()));
			Assert.assertTrue(actualParam.getSBOTerm() == 602 || actualParam.getSBOTerm() == 593);
			if(actualParam.getValue() == 1) {
				Assert.assertEquals("P0", actualParam.getId());
			}
			else {
				Assert.assertTrue(actualParam.getValue() == 0);
			}
			Assert.assertFalse(actualParam.getConstant());
		}
	}
	
	@Test
	public void TestSBML_initialAssignments() {
		Assert.assertEquals(0, sbmlModel.getNumInitialAssignments());
	}
	
	@Test
	public void TestSBML_eventSize() {
		Assert.assertEquals(2, sbmlModel.getNumEvents());
	}
	
	@Test
	public void TestSBML_event1() {
		Event actual_event = sbmlModel.getEvent(0);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("assign_0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P0 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(3, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P0", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P1", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		
		EventAssignment actual_eventAssign3 = actual_event.getEventAssignment(2);
		Assert.assertEquals("out0", actual_eventAssign3.getVariable());
		Assert.assertEquals("(state && 1) || (state && (!out0))", actual_eventAssign3.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event2() {
		Event actual_event = sbmlModel.getEvent(1);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("T0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P1 == 1)", actual_trigger.getMath().toString());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P1", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P0", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
}