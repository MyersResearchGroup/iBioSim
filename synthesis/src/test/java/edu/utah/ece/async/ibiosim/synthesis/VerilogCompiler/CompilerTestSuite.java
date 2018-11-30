package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * A test suite to execute all tests created for VerilogCompiler.
 * 
 * @author Tramy Nguyen
 */
@RunWith(Suite.class)
@SuiteClasses({
	CompilerOptions_Tests.class,
	VerilogParser_Example1.class, 
	VerilogParser_Example2.class, 
	VerilogParser_Example3.class, 
	VerilogParser_Example4.class,
	VerilogParser_Example5.class,
	VerilogParser_Example6.class,
	VerilogParser_Example7.class,
	VerilogParser_Example8.class,
	VerilogParser_Example9.class,
	VerilogParser_Example10.class,
	VerilogParser_Example11.class,
	VerilogParser_Example12.class,
	VerilogParser_Example13.class,
	SBML_Example1.class,
	SBML_Example2.class,
	SBML_Example3.class,
	SBML_Example4.class,
	SBML_Example5.class,
	SBML_Example6.class,
	SBML_Example7.class,
	SBML_Example8.class,
	SBML_Example9.class,
	SBML_Example10.class,
	SBML_Example11.class,
	SBML_Example12.class,
	SBOL_Example1.class,
	SBOL_Example2.class,
	SBOL_Example3.class,
	SBOL_Example4.class,
	SBOL_Example5.class,
	SBOL_Example6.class,
	VerilogParser_EvenZeroes.class,
	VerilogParser_MultThree.class, 
	VerilogParser_Counter.class,
	VerilogParser_LFSR.class,
	VerilogParser_SRLatch.class,
	Workflow.class, 
	Decomposition.class,
	SBOL_EvenZeroes.class})

public class CompilerTestSuite {
	
	protected static String outputDirectory = "src" + File.separator + "test" + File.separator + 
			"resources" + File.separator + "outputFiles";
	
}