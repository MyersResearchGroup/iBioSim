package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.FileNotFoundException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;


/**
 * 
 * @author Tramy Nguyen
 *
 */
public class Workflow_Test {

	@Test 
	public void TestEvenZeroes() {
		String[] cmd = {"-v", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile,
						"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench", 
						"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "evenzeroes"};
		
		Main.main(cmd);
	}
	
	@Test 
	public void TestCounter() {
		String[] cmd = {"-v", CompilerTestSuite.verilogCounter_impFile, CompilerTestSuite.verilogCounter_tbFile, 
				"-imp", "counter_imp", "-tb", "counter_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "counter"};
		Main.main(cmd);
	}
	
	@Test 
	public void TestMultThree() {
		String[] cmd = {"-v", CompilerTestSuite.verilogMultThree_impFile, CompilerTestSuite.verilogMultThree_tbFile, 
				"-imp", "multthree_imp", "-tb", "multThree_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "multThree"};
		
		Main.main(cmd);
	}
	
	@Test 
	public void TestScanflop() {
		String[] cmd = {"-v", CompilerTestSuite.verilogScanflop_impFile, CompilerTestSuite.verilogScanflop_tbFile, 
				"-imp", "scanflop_imp", "-tb", "scanflop_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "scanflop"};
		
		Main.main(cmd);
	}
	
	@Test 
	public void TestLFSR() {
		String[] cmd = {"-v", CompilerTestSuite.verilogLFSR_impFile, CompilerTestSuite.verilogLFSR_tbFile, 
				"-imp", "lfsr_imp", "-tb", "lfsr_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "lfsr"};
		
		Main.main(cmd);
	}
	
	@Test
	public void TestSRLatch() {
		String[] cmd = {"-v", CompilerTestSuite.verilogSRLatch_impFile, CompilerTestSuite.verilogSRLatch_tbFile, 
				"-imp", "srlatch_imp", "-tb", "srlatch_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "srlatch"};
		
		Main.main(cmd);
	}
	
	@Test	
	public void Test_Filter() throws ParseException, FileNotFoundException {
		String[] cmd = {"-v", CompilerTestSuite.verilogFilter_file,
				"-sbol", "-od", CompilerTestSuite.outputDirectory, "-flat"};
		
		Main.main(cmd);
	}

	@Test	
	public void Test_small() throws ParseException, FileNotFoundException {
		String[] cmd = {"-v", CompilerTestSuite.verilogCont5_file,
				"-sbol", "-od", CompilerTestSuite.outputDirectory };
		
		Main.main(cmd);
	}
}
