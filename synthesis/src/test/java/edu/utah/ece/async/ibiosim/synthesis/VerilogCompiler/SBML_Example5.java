package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.Delay;
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
public class SBML_Example5 extends AbstractVerilogParserTest{
	
	private static Model sbmlModel;

	@BeforeClass
	public static void setupTest() {
		String[] cmd = {"-v", reader.getFile("wait_stmt.v"), "-sbml"};
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		
		WrappedSBML sbmlWrapper = compiledVerilog.getSBMLWrapper("wait_stmt");
		Assert.assertNotNull(sbmlWrapper);
		
		sbmlModel = sbmlWrapper.getModel();
		Assert.assertNotNull(sbmlModel);
		Assert.assertEquals("wait_stmt", sbmlModel.getId());
	}
	
	@Test
	public void TestSBML_ports() {
		CompModelPlugin compPlugin = (CompModelPlugin) sbmlModel.getPlugin("comp");
		Assert.assertEquals(1, compPlugin.getNumPorts());
		Port actual_port = compPlugin.getPort(0);
		Assert.assertEquals("wait_stmt__out0", actual_port.getId());
		Assert.assertEquals(601, actual_port.getSBOTerm());
	}
	
	@Test 
	public void TestSBML_parameters() {
		Assert.assertEquals(7, sbmlModel.getNumParameters());
		Set<String> expectedIds = new HashSet<>(Arrays.asList("out0", "P0", "P1", "P2", "P3", "P4", "P5"));
		
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
		Assert.assertEquals(7, sbmlModel.getNumEvents());
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
		Assert.assertEquals("1", actual_eventAssign3.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event2() {
		Event actual_event = sbmlModel.getEvent(1);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("if_0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("(out0 != 1) && (P1 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P1", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P3", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event3() {
		Event actual_event = sbmlModel.getEvent(2);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("wait_0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("(out0 == 1) && (P3 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P3", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P4", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event4() {
		Event actual_event = sbmlModel.getEvent(3);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("delay_0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P4 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P4", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P5", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertTrue(actual_event.isSetDelay());
		Delay delay = actual_event.getDelay();
		Assert.assertEquals("uniform(0,5.0)", delay.getMath().toString());
	}
	
	@Test
	public void TestSBML_event5() {
		Event actual_event = sbmlModel.getEvent(4);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("T0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P5 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P5", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P2", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event6() {
		Event actual_event = sbmlModel.getEvent(5);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("T1", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("(!(out0 != 1)) && (P1 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P1", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P2", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event7() {
		Event actual_event = sbmlModel.getEvent(6);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("T2", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P2 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P2", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P0", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
}
