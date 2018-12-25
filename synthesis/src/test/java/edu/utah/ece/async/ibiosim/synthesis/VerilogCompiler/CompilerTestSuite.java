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
	Decomposition.class,
	FlatteningSBML.class,
	LPN_Example1.class,
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
	VerilogParser_EvenZeroes.class,
	VerilogParser_MultThree.class, 
	VerilogParser_Counter.class,
	VerilogParser_LFSR.class,
	VerilogParser_SRLatch.class,
	VerilogParser_Scanflop.class,
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
	SBOL_Example1.class,
	SBOL_Example2.class,
	SBOL_Example3.class,
	SBOL_Example4.class,
	SBOL_Example5.class,
	SBOL_Example6.class,
	SBOL_Example7.class,
	Workflow.class}) 

public class CompilerTestSuite {
	
	protected static String outputDirectory = "src" + File.separator + "test" + File.separator + 
			"resources" + File.separator + "outputFiles";
	
	//Files used for test cases are recorded below.
	protected static String sbmlEvenZero_impFile  = outputDirectory + File.separator + "evenzeroes_imp.xml";
	protected static String sbmlEvenZero_tbFile   = outputDirectory + File.separator + "evenzeroes_testbench.xml";
	protected static String sbmlEvenZero_flatFile = outputDirectory + File.separator + "evenzeroes_imp_evenzeroes_testbench_flattened.xml";
	
	
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
	protected static String verilogReg_file = CompilerTestSuite.class.getResource(File.separator + "register.v").getFile();
	protected static String verilogRegAssign_file = CompilerTestSuite.class.getResource(File.separator + "reg_assignments.v").getFile();
	protected static String verilogUniform1_file = CompilerTestSuite.class.getResource(File.separator + "uniformFunc1.v").getFile();
	protected static String verilogWaitStmt_file = CompilerTestSuite.class.getResource(File.separator + "wait_stmt.v").getFile();
	protected static String verilogWaitStmt2_file = CompilerTestSuite.class.getResource(File.separator + "wait_stmt2.v").getFile();

	protected static String verilogCounter_impFile = CompilerTestSuite.class.getResource(File.separator + "counter_imp.v").getFile();
	protected static String verilogCounter_tbFile = CompilerTestSuite.class.getResource(File.separator + "counter_testbench.v").getFile();
	protected static String verilogEvenZero_impFile = CompilerTestSuite.class.getResource(File.separator + "evenZeroes_imp.v").getFile();
	protected static String verilogEvenZero_tbFile = CompilerTestSuite.class.getResource(File.separator + "evenZeroes_testbench.v").getFile();
	protected static String verilogFilter_file = CompilerTestSuite.class.getResource(File.separator + "filter.v").getFile();
	protected static String verilogLFSR_impFile = CompilerTestSuite.class.getResource(File.separator + "lfsr_imp.v").getFile();
	protected static String verilogLFSR_tbFile = CompilerTestSuite.class.getResource(File.separator + "lfsr_testbench.v").getFile();
	protected static String verilogMultThree_impFile = CompilerTestSuite.class.getResource(File.separator + "multThree_imp.v").getFile();
	protected static String verilogMultThree_tbFile = CompilerTestSuite.class.getResource(File.separator + "multThree_testbench.v").getFile();
	protected static String verilogScanflop_impFile = CompilerTestSuite.class.getResource(File.separator + "scanflop_imp.v").getFile();
	protected static String verilogScanflop_tbFile = CompilerTestSuite.class.getResource(File.separator + "scanflop_testbench.v").getFile();
	protected static String verilogSRLatch_impFile = CompilerTestSuite.class.getResource(File.separator + "srlatch_imp.v").getFile();
	protected static String verilogSRLatch_tbFile = CompilerTestSuite.class.getResource(File.separator + "srlatch_testbench.v").getFile();




}