package edu.utah.ece.async.ibiosim.synthesis;

import java.io.File;

public class TestingFiles {
	private static String resourceDir = String.join(File.separator, "src", "test", "resources");
	public static String synthDir = File.separator + String.join(File.separator, "edu", "utah", "ece", "async", "ibiosim", "synthesis") ; 
	public static String verilogDir = synthDir + File.separator + "verilogFiles";
	public static String sbolSpecDir = synthDir + File.separator + "sbolSpecFiles";
	public static String sbolLibDir = synthDir + File.separator + "sbolLibFiles";
	public static String tuFileDir = synthDir + File.separator + "tuFiles";
	
	//structure of mvn will not recognize resource directory. When writing to an output directory, the full path of output file must be provided.
	public static String writeLibDir = resourceDir + File.separator + synthDir + File.separator + "sbolLibFiles";
	public static String writeOutputDir = resourceDir + File.separator + synthDir + File.separator + "outputFiles";
	public static String readOuputDir = synthDir + File.separator + "outputFiles";
	
	//Files used for test cases are recorded below.
	public static String notTU1_File = TestingFiles.class.getResource(tuFileDir + File.separator + "not1TU.xml").getFile();
	public static String norTU1_File = TestingFiles.class.getResource(tuFileDir + File.separator + "norTU.xml").getFile();
	
	public static String NOT_Spec = TestingFiles.class.getResource(sbolSpecDir + File.separator + "notDesign.xml").getFile(); 
	public static String NOT2_Spec = TestingFiles.class.getResource(sbolSpecDir + File.separator + "not2Design.xml").getFile(); 
	public static String NOTNOR_Spec = TestingFiles.class.getResource(sbolSpecDir + File.separator + "notNorDesign.xml").getFile(); 
	public static String NORNOT_Spec = TestingFiles.class.getResource(sbolSpecDir + File.separator + "norNotDesign.xml").getFile(); 
	public static String SRLATCH_Spec = TestingFiles.class.getResource(sbolSpecDir + File.separator + "srlatchDesign.xml").getFile(); 

	public static String NOT1_LibSize1 = TestingFiles.class.getResource(sbolLibDir + File.separator + "SynthNOTGate_LibrarySize1.xml").getFile(); 
	public static String NOR1_LibSize1 = TestingFiles.class.getResource(sbolLibDir + File.separator + "SynthNORGate_LibrarySize1.xml").getFile(); 
	public static String NOT2_LibSize2 = TestingFiles.class.getResource(sbolLibDir + File.separator + "SynthNOTGates_LibrarySize2.xml").getFile(); 
	public static String NORNOT_LibSize2 = TestingFiles.class.getResource(sbolLibDir + File.separator + "SynthNORNOTGates_LibrarySize2.xml").getFile(); 
	public static String NORNOT_LibSize3 = TestingFiles.class.getResource(sbolLibDir + File.separator + "SynthNORNOTGates_LibrarySize3.xml").getFile(); 
	public static String NORNOT_LibSize6 = TestingFiles.class.getResource(sbolLibDir + File.separator + "SynthNORNOTGates_LibrarySize6.xml").getFile(); 
	
	public static String sbmlEvenZero_impFile  = TestingFiles.class.getResource(readOuputDir + File.separator + "evenzeroes_imp.xml").getFile();
	public static String sbmlEvenZero_tbFile   = TestingFiles.class.getResource(readOuputDir + File.separator + "evenzeroes_testbench.xml").getFile();
	public static String sbmlEvenZero_flatFile = TestingFiles.class.getResource(readOuputDir+ File.separator + "evenzeroes_imp_evenzeroes_testbench_flattened.xml").getFile();
	
	public static String yosys = TestingFiles.class.getResource(verilogDir + File.separator  + "result.v").getFile();
	
	public static String verilogAlwaysBlock_file = TestingFiles.class.getResource(verilogDir + File.separator + "always_block.v").getFile();
	public static String verilogAssign_file = TestingFiles.class.getResource(verilogDir + File.separator + "assign.v").getFile();
	public static String verilogCondStmt1_file = TestingFiles.class.getResource(verilogDir + File.separator + "conditional_stmt1.v").getFile();
	public static String verilogCondStmt2_file = TestingFiles.class.getResource(verilogDir + File.separator + "conditional_stmt2.v").getFile();
	public static String verilogCondStmt3_file = TestingFiles.class.getResource(verilogDir + File.separator + "conditional_stmt3.v").getFile();
	public static String verilogCont_file = TestingFiles.class.getResource(verilogDir + File.separator + "contAssign.v").getFile();
	public static String verilogCont2_file = TestingFiles.class.getResource(verilogDir + File.separator + "contAssign2.v").getFile();
	public static String verilogCont3_file = TestingFiles.class.getResource(verilogDir + File.separator + "contAssign3.v").getFile();
	public static String verilogCont4_file = TestingFiles.class.getResource(verilogDir + File.separator + "contAssign4.v").getFile();
	public static String verilogCont5_file = TestingFiles.class.getResource(verilogDir + File.separator + "contAssign5.v").getFile();
	public static String verilogCont6_file = TestingFiles.class.getResource(verilogDir + File.separator + "contAssign6.v").getFile();
	public static String verilogCont7_file = TestingFiles.class.getResource(verilogDir + File.separator + "contAssign7.v").getFile();
	public static String verilogDelay_file = TestingFiles.class.getResource(verilogDir + File.separator + "delay.v").getFile();
	public static String verilogInitBlock_file = TestingFiles.class.getResource(verilogDir + File.separator + "init_block.v").getFile();
	public static String verilogPortMapping_file = TestingFiles.class.getResource(verilogDir + File.separator + "portMapping.v").getFile();
	public static String verilogReg_file = TestingFiles.class.getResource(verilogDir + File.separator + "register.v").getFile();
	public static String verilogRegAssign_file = TestingFiles.class.getResource(verilogDir + File.separator + "reg_assignments.v").getFile();
	public static String verilogSystemFunc1_file = TestingFiles.class.getResource(verilogDir + File.separator + "system_func1.v").getFile();
	public static String verilogSystemFunc2_file = TestingFiles.class.getResource(verilogDir + File.separator + "system_func2.v").getFile();
	public static String verilogSystemFunc3_file = TestingFiles.class.getResource(verilogDir + File.separator + "system_func3.v").getFile();
	public static String verilogWaitStmt_file = TestingFiles.class.getResource(verilogDir + File.separator + "wait_stmt.v").getFile();
	public static String verilogWaitStmt2_file = TestingFiles.class.getResource(verilogDir + File.separator + "wait_stmt2.v").getFile();

	public static String verilogCounter_impFile = TestingFiles.class.getResource(verilogDir + File.separator + "counter_imp.v").getFile();
	public static String verilogCounter_tbFile = TestingFiles.class.getResource(verilogDir + File.separator + "counter_testbench.v").getFile();
	public static String verilogEvenZero_impFile = TestingFiles.class.getResource(verilogDir + File.separator + "evenZeroes_imp.v").getFile();
	public static String verilogEvenZero_tbFile = TestingFiles.class.getResource(verilogDir + File.separator + "evenZeroes_testbench.v").getFile();
	public static String verilogFilter_impFile = TestingFiles.class.getResource(verilogDir + File.separator + "filter_imp.v").getFile();
	public static String verilogFilter_tbFile = TestingFiles.class.getResource(verilogDir + File.separator + "filter_testbench.v").getFile();
	public static String verilogLFSR_impFile = TestingFiles.class.getResource(verilogDir + File.separator + "lfsr_imp.v").getFile();
	public static String verilogLFSR_tbFile = TestingFiles.class.getResource(verilogDir + File.separator + "lfsr_testbench.v").getFile();
	public static String verilogMultThree_impFile = TestingFiles.class.getResource(verilogDir + File.separator + "multThree_imp.v").getFile();
	public static String verilogMultThree_tbFile = TestingFiles.class.getResource(verilogDir + File.separator + "multThree_testbench.v").getFile();
	public static String verilogScanflop_impFile = TestingFiles.class.getResource(verilogDir + File.separator + "scanflop_imp.v").getFile();
	public static String verilogScanflop_tbFile = TestingFiles.class.getResource(verilogDir + File.separator + "scanflop_testbench.v").getFile();
	public static String verilogSRLatch_impFile = TestingFiles.class.getResource(verilogDir + File.separator + "srlatch_imp.v").getFile();
	public static String verilogSRLatch_tbFile = TestingFiles.class.getResource(verilogDir + File.separator + "srlatch_testbench.v").getFile();

	public static String verilogSynthesizedFilter_file = TestingFiles.class.getResource(verilogDir + File.separator + "filter.v").getFile();
	
}
