package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;


/**
 * 
 * @author Tramy Nguyen
 *
 */
public class SBMLExample11_Test { 
	
	private static Model sbmlModel;

	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		
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