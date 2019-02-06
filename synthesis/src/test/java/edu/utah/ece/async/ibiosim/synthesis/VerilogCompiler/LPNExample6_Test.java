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
 * Test lpn output for scanflop	design
 * @author Tramy Nguyen 
 *
 */
public class LPNExample6_Test {
	
	private static LPN lpn;

	@BeforeClass
	public static void setupTest() throws XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBMLException, ParseException, SBOLValidationException { 

		CompilerOptions setupOpt = new CompilerOptions();
		setupOpt.addVerilogFile(TestingFiles.verilogScanflop_impFile);
		setupOpt.addVerilogFile(TestingFiles.verilogScanflop_tbFile);

		VerilogCompiler compiledVerilog = VerilogRunner.compile(setupOpt.getVerilogFiles());
		compiledVerilog.compileVerilogOutputData(true);
		compiledVerilog.generateLPN("scanflop_imp", "scanflop_testbench", TestingFiles.writeOutputDir);

		lpn = compiledVerilog.getLPN();
	}

	@Test
	public void Test_inputSize(){
		Assert.assertEquals(7, lpn.getAllInputs().size());
	}
	
	@Test
	public void Test_inputs(){
		Map<String, String> expected_in = new HashMap<String, String>();
		expected_in.put("in1_0", "false");
		expected_in.put("in1_1", "false");
		expected_in.put("in2_0", "false");
		expected_in.put("in2_1", "false");
		expected_in.put("sel0", "false");
		expected_in.put("sel1", "false");
		expected_in.put("req", "false");
		
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
		expected_out.put("q1", "false");
		expected_out.put("q0", "false");
		expected_out.put("ack", "false");

		Assert.assertTrue(expected_out.keySet().equals(lpn.getAllOutputs().keySet()));
		Assert.assertTrue(expected_out.equals(lpn.getAllOutputs()));
	}
	
	@Test
	public void Test_booleanSize(){
		Assert.assertEquals(11, lpn.getBooleans().size());
		
	}
	
	@Test
	public void Test_booleans(){
		Map<String, String> expected_bool = new HashMap<String, String>();
		expected_bool.put("in1_0", "false");
		expected_bool.put("in1_1", "false");
		expected_bool.put("in2_0", "false");
		expected_bool.put("in2_1", "false");
		expected_bool.put("sel0", "false");
		expected_bool.put("sel1", "false");
		expected_bool.put("req", "false");
		expected_bool.put("q1", "false");
		expected_bool.put("q0", "false");
		expected_bool.put("ack", "false");
		expected_bool.put("sf_instance__state", "false");
		
		Assert.assertTrue(expected_bool.keySet().equals(lpn.getBooleans().keySet()));
		Assert.assertTrue(expected_bool.equals(lpn.getBooleans()));
	}
}
