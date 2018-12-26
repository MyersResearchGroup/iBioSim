package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.utah.ece.async.lema.verification.lpn.LPN;
import edu.utah.ece.async.lema.verification.lpn.Transition;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class LPN_Example1_Test {
	
	private static LPN lpn;
	
	@BeforeClass
	public static void setupTest() {
		
		String files = String.join(" ", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile);	
		String[] cmd = {"-v", files, "-lpn",
				"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench", 
				"-od", CompilerTestSuite.outputDirectory};
				
		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
	
		lpn = compiledVerilog.getLPN();
	}
	
	@Test
	public void Test_inputSize(){
		Assert.assertEquals(2, lpn.getAllInputs().size());
	}
	
	@Test
	public void Test_inputs(){
		Map<String, String> expected_in = new HashMap<String, String>();
		expected_in.put("bit0", "false");
		expected_in.put("bit1", "false");
		
		for (Map.Entry<String, String> entry : lpn.getAllInputs().entrySet()) {
		    String actual_key = entry.getKey();
		    String actual_value = entry.getValue();
		    Assert.assertEquals(expected_in.get(actual_key), actual_value);
		}	
	}

	
	@Test
	public void Test_outputSize(){
		Assert.assertEquals(2, lpn.getAllOutputs().size());
	}
	
	
	@Test
	public void Test_outputs(){
		Map<String, String> expected_out = new HashMap<String, String>();
		expected_out.put("parity0", "false");
		expected_out.put("parity1", "false");
		
		for (Map.Entry<String, String> entry : lpn.getAllOutputs().entrySet()) {
		    String actual_key = entry.getKey();
		    String actual_value = entry.getValue();
		    Assert.assertEquals(expected_out.get(actual_key), actual_value);
		}	
	}
	
	@Test
	public void Test_booleanSize(){
		Assert.assertEquals(5, lpn.getBooleans().size());
		
	}
	
	
	@Test
	public void Test_booleans(){
		Map<String, String> expected_bool = new HashMap<String, String>();
		
		expected_bool.put("bit0", "false");
		expected_bool.put("bit1", "false");
		expected_bool.put("parity0", "false");
		expected_bool.put("parity1", "false");
		expected_bool.put("ez_instance__state", "false");
		
		for (Map.Entry<String, String> entry : lpn.getBooleans().entrySet()) {
		    String actual_key = entry.getKey();
		    String actual_value = entry.getValue();
		    Assert.assertEquals(expected_bool.get(actual_key), actual_value);
		}
	}

	@Test
	public void Test_delays() {
		for(Transition t : lpn.getAllTransitions()) {
			//System.out.println(t.getLabel() + " " + t.getDelay());
			//Assert.assertEquals("5.0", t.getDelay());
		}
	}
	
	
	@Test
	public void Test_TransitionSize() {
		Assert.assertEquals(52, lpn.getAllTransitions().length);
	}











}
