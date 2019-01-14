package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

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
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class SBMLExample6_Test {
	
	private static Model sbmlModel;

	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		CompilerOptions setupOpt = new CompilerOptions();
		setupOpt.addVerilogFile(CompilerTestSuite.verilogCondStmt1_file);
		VerilogCompiler compiledVerilog = VerilogRunner.compile(setupOpt.getVerilogFiles());
		compiledVerilog.compileVerilogOutputData(setupOpt.isOutputFlatModel());
		
		WrappedSBML sbmlWrapper = compiledVerilog.getSBMLWrapper("conditional_stmt1");
		Assert.assertNotNull(sbmlWrapper);
		
		sbmlModel = sbmlWrapper.getModel();
		Assert.assertNotNull(sbmlModel);
		Assert.assertEquals("conditional_stmt1", sbmlModel.getId());
	}
	
	@Test
	public void TestSBML_ports() {
		CompModelPlugin compPlugin = (CompModelPlugin) sbmlModel.getPlugin("comp");
		Assert.assertEquals(2, compPlugin.getNumPorts());
		Set<String> expectedIds = new HashSet<>(Arrays.asList("conditional_stmt1__out", "conditional_stmt1__in"));
		
		for(Port actual_port : compPlugin.getListOfPorts()) {
			Assert.assertTrue(expectedIds.contains(actual_port.getId()));
			Assert.assertTrue(actual_port.getSBOTerm() == 600 || actual_port.getSBOTerm() == 601);
		}
		
	}
	
	@Test 
	public void TestSBML_parameters() {
		Assert.assertEquals(8, sbmlModel.getNumParameters());
		Set<String> expectedIds = new HashSet<>(Arrays.asList("in", "out", "P0", "P1", "P2", "P3", "P4", "P5"));
		
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
		Assert.assertEquals(7, sbmlModel.getNumEvents());
	}
	
	@Test
	public void TestSBML_event1() {
		Event actual_event = sbmlModel.getEvent(0);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("if_0", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("in && (P0 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P0", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P2", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
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
		Assert.assertEquals("true && (P2 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(3, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P2", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P3", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		EventAssignment actual_eventAssign3 = actual_event.getEventAssignment(2);
		Assert.assertEquals("out", actual_eventAssign3.getVariable());
		Assert.assertEquals("0", actual_eventAssign3.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event3() {
		Event actual_event = sbmlModel.getEvent(2);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("T0", actual_event.getId());
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
		Assert.assertEquals("P1", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event4() {
		Event actual_event = sbmlModel.getEvent(3);
		Assert.assertNotNull(actual_event);
		Assert.assertEquals("if_1", actual_event.getId());
		Assert.assertEquals(591, actual_event.getSBOTerm());
		
		Assert.assertTrue(actual_event.isSetTrigger());
		Trigger actual_trigger = actual_event.getTrigger();
		Assert.assertTrue(actual_trigger.isSetMath());
		Assert.assertEquals("(!in) && (P0 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P0", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P4", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
		
		Assert.assertFalse(actual_event.isSetDelay());
	}
	
	@Test
	public void TestSBML_event5() {
		Event actual_event = sbmlModel.getEvent(4);
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
		Assert.assertEquals("out", actual_eventAssign3.getVariable());
		Assert.assertEquals("1", actual_eventAssign3.getMath().toString());
		
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
		Assert.assertEquals("true && (P5 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P5", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P1", actual_eventAssign2.getVariable());
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
		Assert.assertEquals("true && (P1 == 1)", actual_trigger.getMath().toString());
		
		Assert.assertEquals(2, actual_event.getNumEventAssignments());
		
		EventAssignment actual_eventAssign1 = actual_event.getEventAssignment(0);
		Assert.assertEquals("P1", actual_eventAssign1.getVariable());
		Assert.assertEquals("0", actual_eventAssign1.getMath().toString());
		
		EventAssignment actual_eventAssign2 = actual_event.getEventAssignment(1);
		Assert.assertEquals("P0", actual_eventAssign2.getVariable());
		Assert.assertEquals("1", actual_eventAssign2.getMath().toString());
	
		Assert.assertFalse(actual_event.isSetDelay());
	}
}
