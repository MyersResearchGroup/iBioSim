package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.lema.verification.lpn.LPN;

/**
 * Test lpn output for filter design.  
 * @author Tramy Nguyen
 *
 */
public class LPNExample3_Test {

	private static LPN lpn;

	@BeforeClass
	public static void setupTest() throws XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBMLException, ParseException, SBOLValidationException { 

		CompilerOptions setupOpt = new CompilerOptions();
		setupOpt.addVerilogFile(TestingFiles.verilogFilter_impFile);
		setupOpt.addVerilogFile(TestingFiles.verilogFilter_tbFile);

		VerilogCompiler compiledVerilog = VerilogRunner.compile(setupOpt.getVerilogFiles());
		compiledVerilog.compileVerilogOutputData(true);
		compiledVerilog.generateLPN("filter_imp", "filter_testbench", TestingFiles.writeOutputDir);

		lpn = compiledVerilog.getLPN();
	}

	@Test
	public void Test_inputSize(){
		Assert.assertEquals(2, lpn.getAllInputs().size());
	}
	
	@Test
	public void Test_inputs(){
		Map<String, String> expected_in = new HashMap<String, String>();
		expected_in.put("Start", "false");
		expected_in.put("Sensor", "false");
		
		Assert.assertTrue(expected_in.keySet().equals(lpn.getAllInputs().keySet()));
		Assert.assertTrue(expected_in.equals(lpn.getAllInputs()));
	}
	
	@Test
	public void Test_outputSize(){
		Assert.assertEquals(3, lpn.getAllOutputs().size());
	}
	
	@Test
	public void Test_outputs(){
		Map<String, String> expected_out = new HashMap<String, String>();
		expected_out.put("Actuator", "false");
		expected_out.put("QS1", "false");
		expected_out.put("QS2", "false");

		Assert.assertTrue(expected_out.keySet().equals(lpn.getAllOutputs().keySet()));
		Assert.assertTrue(expected_out.equals(lpn.getAllOutputs()));
	}
	
	@Test
	public void Test_booleanSize(){
		Assert.assertEquals(5, lpn.getBooleans().size());
		
	}
	
	@Test
	public void Test_booleans(){
		Map<String, String> expected_bool = new HashMap<String, String>();
		expected_bool.put("Start", "false");
		expected_bool.put("Sensor", "false");
		expected_bool.put("QS1", "false");
		expected_bool.put("QS2", "false");
		expected_bool.put("Actuator", "false");
		
		Assert.assertTrue(expected_bool.keySet().equals(lpn.getBooleans().keySet()));
		Assert.assertTrue(expected_bool.equals(lpn.getBooleans()));
	}
}
