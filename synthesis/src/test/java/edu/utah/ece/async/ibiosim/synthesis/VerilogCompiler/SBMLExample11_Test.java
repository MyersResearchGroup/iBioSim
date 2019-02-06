package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;

/**
 * Test replacement and replacedBy objects for SBML conversion
 * @author Tramy Nguyen
 *
 */
public class SBMLExample11_Test {

	private static Model tbModel, impModel;

	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		CompilerOptions setupOpt = new CompilerOptions();
		setupOpt.addVerilogFile(TestingFiles.verilogFilter_impFile);
		setupOpt.addVerilogFile(TestingFiles.verilogFilter_tbFile);
		VerilogCompiler compiledVerilog = VerilogRunner.compile(setupOpt.getVerilogFiles());
		compiledVerilog.compileVerilogOutputData(setupOpt.isOutputFlatModel()); 
		
		WrappedSBML tbWrapper = compiledVerilog.getSBMLWrapper("filter_testbench");
		Assert.assertNotNull(tbWrapper);
		tbModel = tbWrapper.getModel();
		Assert.assertNotNull(tbModel);
		Assert.assertEquals("filter_testbench", tbModel.getId());
		
		WrappedSBML impWrapper = compiledVerilog.getSBMLWrapper("filter_imp");
		Assert.assertNotNull(impWrapper);
		impModel = impWrapper.getModel();
		Assert.assertNotNull(impModel);
		Assert.assertEquals("filter_imp", impModel.getId());
	}
	
	@Test
	public void TestSBML_tbPorts() {
		CompModelPlugin compPlugin = (CompModelPlugin) tbModel.getPlugin("comp");
		Assert.assertEquals(0, compPlugin.getNumPorts());
	}
	
	@Test
	public void TestSBML_impPortSize() {
		CompModelPlugin compPlugin = (CompModelPlugin) impModel.getPlugin("comp");
		Assert.assertEquals(3, compPlugin.getNumPorts());
	}
	
	@Test
	public void TestSBML_impPorts() {
		CompModelPlugin compPort = (CompModelPlugin) impModel.getPlugin("comp");
		for(Port port : compPort.getListOfPorts()){
			if(port.getId().equals("filter_imp__Start")) {
				Assert.assertEquals("Start", port.getIdRef());
			}
			else if(port.getId().equals("filter_imp__Sensor")) {
				Assert.assertEquals("Sensor", port.getIdRef());
			}
			else if(port.getId().equals("filter_imp__Actuator")) {
				Assert.assertEquals("Actuator", port.getIdRef());
			}
			else {
				Assert.fail("Unexpected port found with the following id: " + port.getId());
			}
		}
	}

	@Test
	public void TestSBML_tbSubmoduleSize() {
		CompModelPlugin compPlugin = (CompModelPlugin) tbModel.getPlugin("comp");
		Assert.assertEquals(3, compPlugin.getNumSubmodels());
	}
	
	@Test
	public void TestSBML_tbSubmodule() {
		CompModelPlugin modelPlugin = (CompModelPlugin) tbModel.getPlugin("comp");
		for(Submodel model : modelPlugin.getListOfSubmodels()){
			Assert.assertTrue(model.getId().equals("cell1") || model.getId().equals("cell2") || model.getId().equals("cell3"));
			Assert.assertEquals("filter_imp", model.getModelRef());
		}
	}
	
	@Test 
	public void TestSBML_impParameterSize() {
		Assert.assertEquals(10, impModel.getNumParameters());
	} 
	
	@Test 
	public void TestSBML_tbParameterSize() {
		Assert.assertEquals(36, tbModel.getNumParameters());
	} 
	
	@Test
	public void Test_tbSensorReplacement() {
		Parameter sensor = tbModel.getParameter("Sensor");
		Assert.assertNotNull(sensor);
		
		Assert.assertTrue(sensor.isSetPlugin("comp"));
		CompSBasePlugin sbasePlugin = (CompSBasePlugin)sensor.getPlugin("comp");
		
		Assert.assertEquals(3, sbasePlugin.getNumReplacedElements());
		for(ReplacedElement sensorReplacement : sbasePlugin.getListOfReplacedElements()) {
			Assert.assertEquals("filter_imp__Sensor", sensorReplacement.getPortRef());
			boolean expectedRef = sensorReplacement.getSubmodelRef().equals("cell1") || sensorReplacement.getSubmodelRef().equals("cell2") || sensorReplacement.getSubmodelRef().equals("cell3");
			Assert.assertTrue(expectedRef);
		}
	}
	
	@Test
	public void Test_tbStartReplacement() {
		Parameter sensor = tbModel.getParameter("Start");
		Assert.assertNotNull(sensor);
		
		Assert.assertTrue(sensor.isSetPlugin("comp"));
		CompSBasePlugin sbasePlugin = (CompSBasePlugin)sensor.getPlugin("comp");
		
		Assert.assertEquals(1, sbasePlugin.getNumReplacedElements());
		ReplacedElement startReplacement = sbasePlugin.getReplacedElement(0);
		Assert.assertNotNull(startReplacement);
		Assert.assertEquals("filter_imp__Start", startReplacement.getPortRef());
		Assert.assertEquals("cell1", startReplacement.getSubmodelRef());
	}
	
	@Test
	public void Test_tbActuatorReplacedBy() {
		Parameter actuator = tbModel.getParameter("Actuator");
		Assert.assertNotNull(actuator);
		
		Assert.assertTrue(actuator.isSetPlugin("comp"));
		CompSBasePlugin sbasePlugin = (CompSBasePlugin) actuator.getPlugin("comp");
		
		ReplacedBy actuatorReplacedBy = sbasePlugin.getReplacedBy();
		Assert.assertEquals("filter_imp__Actuator", actuatorReplacedBy.getPortRef());
		Assert.assertTrue(actuatorReplacedBy.getSubmodelRef().equals("cell3"));
	}
	
	@Test
	public void Test_tbQS1ReplacedBy() {
		Parameter QS1 = tbModel.getParameter("QS1");
		Assert.assertNotNull(QS1);
		
		Assert.assertTrue(QS1.isSetPlugin("comp"));
		CompSBasePlugin sbasePlugin = (CompSBasePlugin) QS1.getPlugin("comp");
		
		ReplacedBy actuatorReplacedBy = sbasePlugin.getReplacedBy();
		Assert.assertEquals("filter_imp__Actuator", actuatorReplacedBy.getPortRef());
		Assert.assertTrue(actuatorReplacedBy.getSubmodelRef().equals("cell1"));
	}
	
	@Test
	public void Test_tbQS2ReplacedBy() {
		Parameter QS2 = tbModel.getParameter("QS2");
		Assert.assertNotNull(QS2);
		
		Assert.assertTrue(QS2.isSetPlugin("comp"));
		CompSBasePlugin sbasePlugin = (CompSBasePlugin) QS2.getPlugin("comp");
		
		ReplacedBy actuatorReplacedBy = sbasePlugin.getReplacedBy();
		Assert.assertEquals("filter_imp__Actuator", actuatorReplacedBy.getPortRef());
		Assert.assertTrue(actuatorReplacedBy.getSubmodelRef().equals("cell2"));
	}
	
	@Test
	public void Test_tbQS1Replacement() {
		Parameter qs1 = tbModel.getParameter("QS1");
		Assert.assertNotNull(qs1);
		
		Assert.assertTrue(qs1.isSetPlugin("comp"));
		CompSBasePlugin sbasePlugin = (CompSBasePlugin)qs1.getPlugin("comp");
		
		Assert.assertEquals(1, sbasePlugin.getNumReplacedElements());
		ReplacedElement startReplacement = sbasePlugin.getReplacedElement(0);
		Assert.assertNotNull(startReplacement);
		Assert.assertEquals("filter_imp__Start", startReplacement.getPortRef());
		Assert.assertEquals("cell2", startReplacement.getSubmodelRef());
	}
	
	@Test
	public void Test_tbQS2Replacement() {
		Parameter qs2 = tbModel.getParameter("QS2");
		Assert.assertNotNull(qs2);
		
		Assert.assertTrue(qs2.isSetPlugin("comp"));
		CompSBasePlugin sbasePlugin = (CompSBasePlugin)qs2.getPlugin("comp");
		
		Assert.assertEquals(1, sbasePlugin.getNumReplacedElements());
		ReplacedElement startReplacement = sbasePlugin.getReplacedElement(0);
		Assert.assertNotNull(startReplacement);
		Assert.assertEquals("filter_imp__Start", startReplacement.getPortRef());
		Assert.assertEquals("cell3", startReplacement.getSubmodelRef());
	}
	
	@Test
	public void TestSBML_impInitialAssignmentSize() {
		Assert.assertEquals(1, impModel.getNumInitialAssignments());
	}
	
	@Test
	public void TestSBML_tbInitialAssignmentSize() {
		Assert.assertEquals(2, tbModel.getNumInitialAssignments());
	}
	
	@Test
	public void TestSBML_impInitialAssignment() {
		InitialAssignment assign = impModel.getInitialAssignment(0);
		Assert.assertEquals("Actuator", assign.getVariable());
		Assert.assertTrue(assign.getMath().isInteger());
		Assert.assertEquals(0, assign.getMath().getInteger());
	}
	
	@Test
	public void TestSBML_tbInitialAssignment() {
		for(InitialAssignment assign : tbModel.getListOfInitialAssignments()){
			Assert.assertTrue(assign.getVariable().equals("Start") || assign.getVariable().equals("Sensor"));
			Assert.assertTrue(assign.getMath().isInteger());
			Assert.assertEquals(0, assign.getMath().getInteger());
		}
	}
}
