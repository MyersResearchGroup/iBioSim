package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.FileNotFoundException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;


/**
 * Test end to end process of running the VerilogCompiler. 
 * This process involves reading in the specified verilog files and compiling the result to the desired output format
 * 
 * @author Tramy Nguyen
 */
public class Workflow_Test {

	@Test 
	public void TestEvenZeroes() {
		String files = String.join(" ", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile);
		String[] cmd = {"-v", files,
						"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench", 
						"-lpn", "-od", TestingFiles.writeOutputDir, "-o", "evenzeroes"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void TestFlatEvenZeroes() {
		String files = String.join(" ", TestingFiles.verilogEvenZero_impFile, TestingFiles.verilogEvenZero_tbFile);
		String[] cmd = {"-v", files,
						"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench", 
						"-sbml", "-flat", "-od", TestingFiles.writeOutputDir, "-o", "evenzeroes_imp_evenzeroes_testbench_flattened"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void TestCounter() {
		String files = String.join(" ", TestingFiles.verilogCounter_impFile, TestingFiles.verilogCounter_tbFile);
		String[] cmd = {"-v", files, 
				"-imp", "counter_imp", "-tb", "counter_testbench", 
				"-lpn", "-od", TestingFiles.writeOutputDir, "-o", "counter"};
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void TestMultThree() {
		String files = String.join(" ", TestingFiles.verilogMultThree_impFile, TestingFiles.verilogMultThree_tbFile);
		String[] cmd = {"-v", files, 
				"-imp", "multthree_imp", "-tb", "multThree_testbench", 
				"-lpn", "-od", TestingFiles.writeOutputDir, "-o", "multThree"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void TestScanflop() {
		String files = String.join(" ", TestingFiles.verilogScanflop_impFile, TestingFiles.verilogScanflop_tbFile);
		String[] cmd = {"-v", files, 
				"-imp", "scanflop_imp", "-tb", "scanflop_testbench", 
				"-lpn", "-od", TestingFiles.writeOutputDir, "-o", "scanflop"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void TestLFSR() {
		String files = String.join(" ", TestingFiles.verilogLFSR_impFile, TestingFiles.verilogLFSR_tbFile);
		String[] cmd = {"-v", files, 
				"-imp", "lfsr_imp", "-tb", "lfsr_testbench", 
				"-lpn", "-od", TestingFiles.writeOutputDir, "-o", "lfsr"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test
	public void TestSRLatch() {
		String files = String.join(" ", TestingFiles.verilogSRLatch_impFile, TestingFiles.verilogSRLatch_tbFile);
		String[] cmd = {"-v", files, 
				"-imp", "srlatch_imp", "-tb", "srlatch_testbench", 
				"-lpn", "-od", TestingFiles.writeOutputDir, "-o", "srlatch"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test	
	public void Test_FilterSynthesized() throws ParseException, FileNotFoundException {
		String[] cmd = {"-v", TestingFiles.verilogSynthesizedFilter_file,
				"-sbol", "-od", TestingFiles.writeOutputDir, "-flat"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test	
	public void Test_Filter() throws ParseException, FileNotFoundException {
		String files = String.join(" ", TestingFiles.verilogFilter_impFile, TestingFiles.verilogFilter_tbFile);
		String[] cmd = {"-v", files, "-imp", "filter_imp", "-tb", "filter_testbench",
				"-lpn", "-od", TestingFiles.writeOutputDir, "-o", "filter"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test	
	public void Test_feedbackDesign() throws ParseException, FileNotFoundException {
		String[] cmd = {"-v", TestingFiles.verilogCont5_file,
				"-sbol", "-od", TestingFiles.writeOutputDir};
		VerilogRunner.main(cmd);
	}

	@Test	
	public void Test_yosysDesign() throws ParseException, FileNotFoundException {
		String[] cmd = {"-v", TestingFiles.yosys,
				"-sbol", "-od", TestingFiles.writeOutputDir, "-flat"};
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void Test() {
		String[] cmd = {"-v", TestingFiles.verilogOrSpec_File,
				"-sbol", "-od", TestingFiles.writeOutputDir, "-flat"};
		
		VerilogRunner.main(cmd);;
	}
}
