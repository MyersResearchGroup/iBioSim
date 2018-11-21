package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.utah.ece.async.lema.verification.lpn.LPN;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class LPN_Example1 extends AbstractVerilogParserTest{
	
	private static LPN lpn;
	
	@BeforeClass
	public static void setupTest() {
		String files = String.join(" ", reader.getFile("evenZeroes_imp.v"), reader.getFile("evenZeroes_testbench.v"));	
		
		String[] cmd = {"-v", files, "-lpn",
				"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench",
				"-od", CompilerTestSuite.outputDirectory, "-o", "tempLPN"};
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
	
		lpn = compiledVerilog.getLPN();
	}
	
	@Test
	public void Test_inputSize(){
		Assert.assertEquals(2, lpn.getAllInputs().size());
	}
	
	@Test
	public void Test_input(){
		
		lpn.getAllInputs().values();
	}
}
