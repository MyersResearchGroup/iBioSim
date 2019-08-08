package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class SBMLExample3_Test {
	
	private static Model sbmlModel;
	
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		VerilogToLPNCompiler compiler = new VerilogToLPNCompiler();
		VerilogModule vModule = compiler.parseVerilogFile(new File(TestingFiles.verilogReg_File));
		
		WrappedSBML sbmlWrapper = compiler.generateSBMLFromVerilog(vModule);
		Assert.assertNotNull(sbmlWrapper);		
		
		sbmlModel = sbmlWrapper.getModel();
		Assert.assertNotNull(sbmlModel);;
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
