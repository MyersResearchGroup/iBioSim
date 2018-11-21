package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.FileNotFoundException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;


/**
 * 
 * @author Tramy Nguyen
 *
 */
public class Workflow extends AbstractVerilogParserTest{

	@Test 
	public void TestEvenZeroes() {
		String files = String.join(" ", reader.getFile("evenZeroes_imp.v"), reader.getFile("evenZeroes_testbench.v"));	
		String[] cmd = {"-v", files,
						"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench", 
						"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "evenzeroes"};
		
		Main.main(cmd);
	}
	
	@Test 
	public void TestCounter() {
		String files = String.join(" ", reader.getFile("counter_imp.v"), reader.getFile("counter_testbench.v"));
		String[] cmd = {"-v", files, 
				"-imp", "counter_imp", "-tb", "counter_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "counter"};
		Main.main(cmd);
	}
	
	@Test 
	public void TestMultThree() {
		String files = String.join(" ", reader.getFile("multThree_imp.v"), reader.getFile("multThree_testbench.v"));
		String[] cmd = {"-v", files, 
				"-imp", "multthree_imp", "-tb", "multThree_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "multThree"};
		
		Main.main(cmd);
	}
	
	@Test 
	public void TestScanflop() {
		String files = String.join(" ", reader.getFile("scanflop_imp.v"), reader.getFile("scanflop_testbench.v"));	
		String[] cmd = {"-v", files, 
				"-imp", "scanflop_imp", "-tb", "scanflop_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "scanflop"};
		
		Main.main(cmd);
	}
	
	@Test 
	public void TestLFSR() {
		String files = String.join(" ", reader.getFile("lfsr_imp.v"), reader.getFile("lfsr_testbench.v"));
		String[] cmd = {"-v", files, 
				"-imp", "lfsr_imp", "-tb", "lfsr_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "lfsr"};
		
		Main.main(cmd);
	}
	
	@Test
	public void TestSRLatch() {
		String files = String.join(" ", reader.getFile("srlatch_imp.v"), reader.getFile("srlatch_testbench.v"));
		String[] cmd = {"-v", files, 
				"-imp", "srlatch_imp", "-tb", "srlatch_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "srlatch"};
		
		Main.main(cmd);
	}
	
	@Test	
	public void Test_EvenZeroesSynth() throws ParseException, FileNotFoundException {
		String[] cmd = {"-v", reader.getFile("evenzeroes_sequential_synth.v"),
				"-sbol", "-od", CompilerTestSuite.outputDirectory};
		
		Main.main(cmd);
	}
	
	@Test	
	public void Test_gts_ack() throws ParseException, FileNotFoundException {
		String[] cmd = {"-v", reader.getFile("sequential_gts_ack.v"),
				"-sbol", "-od", CompilerTestSuite.outputDirectory};
		
		Main.main(cmd);
	}
	

}
