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
 * Test lpn output for lfsr design. 
 * @author Tramy Nguyen
 *
 */
public class LPNExample4_Test {

	private static LPN lpn;

	@BeforeClass
	public static void setupTest() throws XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBMLException, ParseException, SBOLValidationException { 

		CompilerOptions setupOpt = new CompilerOptions();
		setupOpt.addVerilogFile(TestingFiles.verilogLFSR_impFile);
		setupOpt.addVerilogFile(TestingFiles.verilogLFSR_tbFile);

		VerilogCompiler compiledVerilog = VerilogRunner.compile(setupOpt.getVerilogFiles());
		compiledVerilog.compileVerilogOutputData(true);
		compiledVerilog.generateLPN("lfsr_imp", "lfsr_testbench", TestingFiles.writeOutputDir);

		lpn = compiledVerilog.getLPN();
	}

	@Test
	public void Test_inputSize(){
		Assert.assertEquals(1, lpn.getAllInputs().size());
	}
	
	@Test
	public void Test_inputs(){
		Map<String, String> expected_in = new HashMap<String, String>();
		expected_in.put("req", "false");
		
		Assert.assertTrue(expected_in.keySet().equals(lpn.getAllInputs().keySet()));
		Assert.assertTrue(expected_in.equals(lpn.getAllInputs()));
	}
	
	@Test
	public void Test_outputSize(){
		Assert.assertEquals(7, lpn.getAllOutputs().size());
	}
	
	@Test
	public void Test_outputs(){
		Map<String, String> expected_out = new HashMap<String, String>();
		expected_out.put("ack", "false");
		expected_out.put("a0", "false");
		expected_out.put("a1", "false");
		expected_out.put("b0", "false");
		expected_out.put("b1", "false");
		expected_out.put("c0", "false");
		expected_out.put("c1", "false");

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
		expected_bool.put("req", "false");
		expected_bool.put("ack", "false");
		expected_bool.put("a0", "false");
		expected_bool.put("a1", "false");
		expected_bool.put("b0", "false");
		expected_bool.put("b1", "false");
		expected_bool.put("c0", "false");
		expected_bool.put("c1", "false");
		expected_bool.put("lfsr_instance__feedback", "false");
		expected_bool.put("lfsr_instance__state0", "false");
		expected_bool.put("lfsr_instance__state1", "false");
		
		Assert.assertTrue(expected_bool.keySet().equals(lpn.getBooleans().keySet()));
		Assert.assertTrue(expected_bool.equals(lpn.getBooleans()));
	}
}
