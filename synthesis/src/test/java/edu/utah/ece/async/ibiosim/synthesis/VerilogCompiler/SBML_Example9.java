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
public class SBML_Example9 extends AbstractVerilogParserTest{
	
	private static Model sbmlModel;

	@BeforeClass
	public static void setupTest() {
		String[] cmd = {"-v", CompilerTestSuite.verilogDelay_file , "-sbml"};
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		
		WrappedSBML sbmlWrapper = compiledVerilog.getSBMLWrapper("delay");
		Assert.assertNotNull(sbmlWrapper);
		
		sbmlModel = sbmlWrapper.getModel();
		Assert.assertNotNull(sbmlModel);
		Assert.assertEquals("delay", sbmlModel.getId());
	}
	
	@Test
	public void TestSBML_ports() {
		CompModelPlugin compPlugin = (CompModelPlugin) sbmlModel.getPlugin("comp");
		Assert.assertEquals(2, compPlugin.getNumPorts());
		
		Port actual_port = compPlugin.getPort(0);
		Assert.assertEquals("delay__out0", actual_port.getId());
		Assert.assertTrue(actual_port.getSBOTerm() == 601);
		
		actual_port = compPlugin.getPort(1);
		Assert.assertEquals("delay__out1", actual_port.getId());
		Assert.assertTrue(actual_port.getSBOTerm() == 601);
	}
	
	@Test 
	public void TestSBML_parameters() {
		Assert.assertEquals(15, sbmlModel.getNumParameters());
		Set<String> expectedIds = new HashSet<>(Arrays.asList("out0", "out1", "next", "P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8", "P9", "P10", "P11"));
		
		for(Parameter actualParam : sbmlModel.getListOfParameters()) {
			Assert.assertFalse(actualParam.getConstant());
			Assert.assertTrue(expectedIds.contains(actualParam.getId()));
			Assert.assertTrue(actualParam.getSBOTerm() == 602 || actualParam.getSBOTerm() == 593);
			Assert.assertFalse(actualParam.getConstant());
			if(actualParam.getValue() == 1) {
				Assert.assertEquals("P0", actualParam.getId());
			}
			else {
				Assert.assertTrue(actualParam.getValue() == 0);
			}
		}
	}
	
	@Test
	public void TestSBML_initialAssignments() {
		Assert.assertEquals(0, sbmlModel.getNumInitialAssignments());
	}
	
	@Test
	public void TestSBML_eventSize() {
		Assert.assertEquals(13, sbmlModel.getNumEvents());
	}
	
	@Test
	public void TestSBML_event1() {
		Event actual_event = sbmlModel.getEvent(0);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("delay_0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P0 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P0", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P1", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertTrue(actual_event.isSetDelay());
		Delay delay = actual_event.getDelay();
		Assert.assertTrue(5.0 == delay.getMath().getReal());
	}
	
	@Test
	public void TestSBML_event2() {
		Event actual_event = sbmlModel.getEvent(1);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("assign_0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P1 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(3, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P1", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P2", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		EventAssignment actual_eventAssign3 = actual_event.getEventAssignment(2);
		Assert.assertEquals("next", actual_eventAssign3.getVariable());
		Assert.assertEquals("piecewise(1, uniform(0, 1) < 0.5, 0)", actual_eventAssign3.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event3() {
		Event actual_event = sbmlModel.getEvent(2);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("if_0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("(next == 0) && (P2 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P2", actual_eventAssign1.getVariable());
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
		Assert.assertEquals("assign_1", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P4 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(3, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P4", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P5", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		EventAssignment actual_eventAssign3 = actual_event.getEventAssignment(2);
		Assert.assertEquals("out0", actual_eventAssign3.getVariable());
		Assert.assertEquals("1", actual_eventAssign3.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event5() {
		Event actual_event = sbmlModel.getEvent(4);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("wait_0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("(out0 == 1) && (P5 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P5", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P6", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event6() {
		Event actual_event = sbmlModel.getEvent(5);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("delay_1", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P6 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P6", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P7", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertTrue(actual_event.isSetDelay());
		Delay delay = actual_event.getDelay();
		Assert.assertTrue(5.0 == delay.getMath().getReal());
	}
	
	@Test
	public void TestSBML_event7() {
		Event actual_event = sbmlModel.getEvent(6);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("T0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P7 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P7", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P3", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event8() {
		Event actual_event = sbmlModel.getEvent(7);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("if_1", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("(!(next == 0)) && (P2 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P2", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P8", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event9() {
		Event actual_event = sbmlModel.getEvent(8);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("assign_2", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P8 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(3, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P8", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P9", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		EventAssignment actual_eventAssign3 = actual_event.getEventAssignment(2);
		Assert.assertEquals("out1", actual_eventAssign3.getVariable());
		Assert.assertEquals("1", actual_eventAssign3.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event10() {
		Event actual_event = sbmlModel.getEvent(9);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("wait_1", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("(out1 == 1) && (P9 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P9", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P10", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event11() {
		Event actual_event = sbmlModel.getEvent(10);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("delay_2", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P10 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P10", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P11", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertTrue(actual_event.isSetDelay());
		Delay delay = actual_event.getDelay();
		Assert.assertTrue(5.0 == delay.getMath().getReal());
	}
	
	@Test
	public void TestSBML_event12() {
		Event actual_event = sbmlModel.getEvent(11);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("T1", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P11 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P11", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P3", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event13() {
		Event actual_event = sbmlModel.getEvent(12);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("T2", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("true && (P3 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P3", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P0", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
}