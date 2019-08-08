package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogAssignment;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;


/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParserExample12_Test {

	private static VerilogModule verilogModule;
	
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		VerilogParser compiledVerilog = new VerilogParser();
		verilogModule = compiledVerilog.parseVerilogFile(new File(TestingFiles.verilogCont2_File));
		Assert.assertNotNull(verilogModule);
	}
	
	@Test
	public void Test_contAssign() {
		VerilogAssignment actual_assign = verilogModule.getContinuousAssignment(0);
		Assert.assertEquals("y", actual_assign.getVariable());
		Assert.assertEquals("and(a,b)", actual_assign.getExpression());
	}

}