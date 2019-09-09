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
	public void Test_VerilogToLPN() {
		String files = String.join(" ", TestingFiles.verilogSRLatch_impFile, TestingFiles.verilogSRLatch_tbFile);
		String[] cmd = {"-v", files,
						"-imp", "srlatch_imp", "-tb", "srlatch_testbench", 
						"-lpn", "-od", TestingFiles.writeOutputDir, "-o", "tm_solution"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void Test_playground() {
		String files = String.join(" ", TestingFiles.verilogCounter_impFile, TestingFiles.verilogCounter_tbFile);
		String[] cmd = {"-v", files,
						"-imp", "counter_imp", "-tb", "counter_testbench", 
						"-lpn", "-od", TestingFiles.writeOutputDir, "-o", "counter"};
		
		VerilogRunner.main(cmd);
	}
	
	@Test 
	public void Test_VerilogToSBML() {
		String files = String.join(" ", TestingFiles.verilogEvenZeroes_impFile, TestingFiles.verilogEvenZeroes_tbFile);
		String[] cmd = {"-v", files,
						"-imp", "evenzeroes_imp", "-tb", "evenzeroes_testbench", 
						"-sbml", "-od", TestingFiles.writeOutputDir, "-o", "evenzeroes"};
		
		VerilogRunner.main(cmd);
	}
	
	
	@Test	
	public void Test_VerilogToSBOL() throws ParseException, FileNotFoundException {
		String[] cmd = {"-v", TestingFiles.verilogSrLatchDecomp_File,
				"-sbol", "-flat", "-od", TestingFiles.writeOutputDir};
		VerilogRunner.main(cmd);
	}

}
