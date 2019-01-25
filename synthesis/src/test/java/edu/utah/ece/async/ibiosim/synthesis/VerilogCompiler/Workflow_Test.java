package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.FileNotFoundException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;


/**
 * Test end to end process of running the VerilogCompiler. 
 * This process involves reading in the specified verilog files and compiling the result to the desired output format
 * 
 * @author Tramy Nguyen
 */
public class Workflow_Test {

	@Test 
	public void TestEvenZeroes() {
		String files = String.join(" ", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile);
		String[] cmd = {"-v", files,
						"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench", 
						"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "evenzeroes"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void TestFlatEvenZeroes() {
		String files = String.join(" ", CompilerTestSuite.verilogEvenZero_impFile, CompilerTestSuite.verilogEvenZero_tbFile);
		String[] cmd = {"-v", files,
						"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench", 
						"-sbml", "-flat", "-od", CompilerTestSuite.outputDirectory, "-o", "evenzeroes_imp_evenzeroes_testbench_flattened"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void TestCounter() {
		String files = String.join(" ", CompilerTestSuite.verilogCounter_impFile, CompilerTestSuite.verilogCounter_tbFile);
		String[] cmd = {"-v", files, 
				"-imp", "counter_imp", "-tb", "counter_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "counter"};
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void TestMultThree() {
		String files = String.join(" ", CompilerTestSuite.verilogMultThree_impFile, CompilerTestSuite.verilogMultThree_tbFile);
		String[] cmd = {"-v", files, 
				"-imp", "multthree_imp", "-tb", "multThree_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "multThree"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void TestScanflop() {
		String files = String.join(" ", CompilerTestSuite.verilogScanflop_impFile, CompilerTestSuite.verilogScanflop_tbFile);
		String[] cmd = {"-v", files, 
				"-imp", "scanflop_imp", "-tb", "scanflop_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "scanflop"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void TestLFSR() {
		String files = String.join(" ", CompilerTestSuite.verilogLFSR_impFile, CompilerTestSuite.verilogLFSR_tbFile);
		String[] cmd = {"-v", files, 
				"-imp", "lfsr_imp", "-tb", "lfsr_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "lfsr"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test
	public void TestSRLatch() {
		String files = String.join(" ", CompilerTestSuite.verilogSRLatch_impFile, CompilerTestSuite.verilogSRLatch_tbFile);
		String[] cmd = {"-v", files, 
				"-imp", "srlatch_imp", "-tb", "srlatch_testbench", 
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "srlatch"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test	
	public void Test_FilterSynthesized() throws ParseException, FileNotFoundException {
		String[] cmd = {"-v", CompilerTestSuite.verilogSynthesizedFilter_file,
				"-sbol", "-od", CompilerTestSuite.outputDirectory, "-flat"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test	
	public void Test_Filter() throws ParseException, FileNotFoundException {
		String files = String.join(" ", CompilerTestSuite.verilogFilter_impFile, CompilerTestSuite.verilogFilter_tbFile);
		String[] cmd = {"-v", files, "-imp", "filter_imp", "-tb", "filter_testbench",
				"-lpn", "-od", CompilerTestSuite.outputDirectory, "-o", "filter"};
		
		VerilogRunner.main(cmd);
	}

	@Test	
	public void Test_yosysDesign() throws ParseException, FileNotFoundException {
		String[] cmd = {"-v", CompilerTestSuite.yosys,
				"-sbol", "-od", "-flat", CompilerTestSuite.outputDirectory};
		System.out.println("YOSYS ERROR HERE");
		VerilogRunner.main(cmd);
	}
}
