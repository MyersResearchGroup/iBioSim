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
	Decomposition_Test.class,
	FlatteningSBML_Test.class,
	LPNExample1_Test.class,
	LPNExample2_Test.class,
	LPNExample3_Test.class,
	LPNExample4_Test.class,
	LPNExample5_Test.class,
	LPNExample6_Test.class,
	LPNExample7_Test.class,
	VerilogParserExample1_Test.class, 
	VerilogParserExample2_Test.class, 
	VerilogParserExample3_Test.class, 
	VerilogParserExample4_Test.class,
	VerilogParserExample5_Test.class,
	VerilogParserExample6_Test.class,
	VerilogParserExample7_Test.class,
	VerilogParserExample8_Test.class,
	VerilogParserExample9_Test.class,
	VerilogParserExample10_Test.class,
	VerilogParserExample11_Test.class,
	VerilogParserExample12_Test.class,
	VerilogParserExample13_Test.class,
	VerilogParserExample14_Test.class,
	VerilogParserExample15_Test.class,
	VerilogParserExample16_Test.class,
	VerilogParserEvenZeroes_Test.class,
	VerilogParserMultThree_Test.class, 
	VerilogParserCounter_Test.class,
	VerilogParserFilter_Test.class,
	VerilogParserLFSR_Test.class,
	VerilogParserSRLatch_Test.class,
	VerilogParserScanflop_Test.class,
	SBMLExample1_Test.class,
	SBMLExample2_Test.class,
	SBMLExample3_Test.class,
	SBMLExample4_Test.class,
	SBMLExample5_Test.class,
	SBMLExample6_Test.class,
	SBMLExample7_Test.class,
	SBMLExample8_Test.class,
	SBMLExample9_Test.class,
	SBMLExample10_Test.class,
	SBMLExample11_Test.class,
	SBOLExample1_Test.class,
	SBOLExample2_Test.class,
	SBOLExample3_Test.class,
	SBOLExample4_Test.class,
	SBOLExample5_Test.class,
	SBOLExample6_Test.class,
	SBOLExample7_Test.class,
	SBOLExample8_Test.class,
	Workflow_Test.class}) 

public class CompilerTestSuite {
	
	protected static String outputDirectory = "src" + File.separator + "test" + File.separator + 
			"resources" + File.separator + "outputFiles";
	
	//Files used for test cases are recorded below.
	protected static String sbmlEvenZero_impFile  = outputDirectory + File.separator + "evenzeroes_imp.xml";
	protected static String sbmlEvenZero_tbFile   = outputDirectory + File.separator + "evenzeroes_testbench.xml";
	protected static String sbmlEvenZero_flatFile = outputDirectory + File.separator + "evenzeroes_imp_evenzeroes_testbench_flattened.xml";
	
	protected static String vSpec = CompilerTestSuite.class.getResource(File.separator + "NOT_Spec.v").getFile();
	
	protected static String verilogAlwaysBlock_file = CompilerTestSuite.class.getResource(File.separator + "always_block.v").getFile();
	protected static String verilogAssign_file = CompilerTestSuite.class.getResource(File.separator + "assign.v").getFile();
	protected static String verilogCondStmt1_file = CompilerTestSuite.class.getResource(File.separator + "conditional_stmt1.v").getFile();
	protected static String verilogCondStmt2_file = CompilerTestSuite.class.getResource(File.separator + "conditional_stmt2.v").getFile();
	protected static String verilogCondStmt3_file = CompilerTestSuite.class.getResource(File.separator + "conditional_stmt3.v").getFile();
	protected static String verilogCont_file = CompilerTestSuite.class.getResource(File.separator + "contAssign.v").getFile();
	protected static String verilogCont2_file = CompilerTestSuite.class.getResource(File.separator + "contAssign2.v").getFile();
	protected static String verilogCont3_file = CompilerTestSuite.class.getResource(File.separator + "contAssign3.v").getFile();
	protected static String verilogCont4_file = CompilerTestSuite.class.getResource(File.separator + "contAssign4.v").getFile();
	protected static String verilogCont5_file = CompilerTestSuite.class.getResource(File.separator + "contAssign5.v").getFile();
	protected static String verilogCont6_file = CompilerTestSuite.class.getResource(File.separator + "contAssign6.v").getFile();
	protected static String verilogCont7_file = CompilerTestSuite.class.getResource(File.separator + "contAssign7.v").getFile();
	protected static String verilogDelay_file = CompilerTestSuite.class.getResource(File.separator + "delay.v").getFile();
	protected static String verilogInitBlock_file = CompilerTestSuite.class.getResource(File.separator + "init_block.v").getFile();
	protected static String verilogPortMapping_file = CompilerTestSuite.class.getResource(File.separator + "portMapping.v").getFile();
	protected static String verilogReg_file = CompilerTestSuite.class.getResource(File.separator + "register.v").getFile();
	protected static String verilogRegAssign_file = CompilerTestSuite.class.getResource(File.separator + "reg_assignments.v").getFile();
	protected static String verilogSystemFunc1_file = CompilerTestSuite.class.getResource(File.separator + "system_func1.v").getFile();
	protected static String verilogSystemFunc2_file = CompilerTestSuite.class.getResource(File.separator + "system_func2.v").getFile();
	protected static String verilogSystemFunc3_file = CompilerTestSuite.class.getResource(File.separator + "system_func3.v").getFile();
	protected static String verilogWaitStmt_file = CompilerTestSuite.class.getResource(File.separator + "wait_stmt.v").getFile();
	protected static String verilogWaitStmt2_file = CompilerTestSuite.class.getResource(File.separator + "wait_stmt2.v").getFile();

	protected static String verilogCounter_impFile = CompilerTestSuite.class.getResource(File.separator + "counter_imp.v").getFile();
	protected static String verilogCounter_tbFile = CompilerTestSuite.class.getResource(File.separator + "counter_testbench.v").getFile();
	protected static String verilogEvenZero_impFile = CompilerTestSuite.class.getResource(File.separator + "evenZeroes_imp.v").getFile();
	protected static String verilogEvenZero_tbFile = CompilerTestSuite.class.getResource(File.separator + "evenZeroes_testbench.v").getFile();
	protected static String verilogFilter_impFile = CompilerTestSuite.class.getResource(File.separator + "filter_imp.v").getFile();
	protected static String verilogFilter_tbFile = CompilerTestSuite.class.getResource(File.separator + "filter_testbench.v").getFile();
	protected static String verilogLFSR_impFile = CompilerTestSuite.class.getResource(File.separator + "lfsr_imp.v").getFile();
	protected static String verilogLFSR_tbFile = CompilerTestSuite.class.getResource(File.separator + "lfsr_testbench.v").getFile();
	protected static String verilogMultThree_impFile = CompilerTestSuite.class.getResource(File.separator + "multThree_imp.v").getFile();
	protected static String verilogMultThree_tbFile = CompilerTestSuite.class.getResource(File.separator + "multThree_testbench.v").getFile();
	protected static String verilogScanflop_impFile = CompilerTestSuite.class.getResource(File.separator + "scanflop_imp.v").getFile();
	protected static String verilogScanflop_tbFile = CompilerTestSuite.class.getResource(File.separator + "scanflop_testbench.v").getFile();
	protected static String verilogSRLatch_impFile = CompilerTestSuite.class.getResource(File.separator + "srlatch_imp.v").getFile();
	protected static String verilogSRLatch_tbFile = CompilerTestSuite.class.getResource(File.separator + "srlatch_testbench.v").getFile();

	protected static String verilogSynthesizedFilter_file = CompilerTestSuite.class.getResource(File.separator + "filter.v").getFile();

}