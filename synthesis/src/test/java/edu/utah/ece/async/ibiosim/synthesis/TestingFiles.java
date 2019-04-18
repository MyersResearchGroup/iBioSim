package edu.utah.ece.async.ibiosim.synthesis;

import java.io.File;

/**
 * Files used for testing Technology Mapping package
 * @author Tramy Nguyen
 *
 */
public class TestingFiles {
	private static String resourceDir = String.join(File.separator, "src", "test", "resources");
	public static String synthDir = File.separator + String.join(File.separator, "edu", "utah", "ece", "async", "ibiosim", "synthesis") ; 
	public static String verilogDir = synthDir + File.separator + "verilogFiles";
	public static String sbolSpecDir = synthDir + File.separator + "sbolSpecFiles";
	public static String sbolLibDir = synthDir + File.separator + "sbolLibFiles";
	public static String tuFileDir = synthDir + File.separator + "tuFiles";
	
	//structure of mvn will not recognize resource directory. When writing to an output directory, the full path of output file must be provided.
	public static String writeLibDir = resourceDir + File.separator + synthDir + File.separator + "sbolLibFiles";
	public static String writeSpecDir = resourceDir + File.separator + synthDir + File.separator + "sbolSpecFiles";
	public static String writeOutputDir = resourceDir + File.separator + synthDir + File.separator + "outputFiles";
	public static String readOuputDir = synthDir + File.separator + "outputFiles";
	
	//Files used for test cases are recorded below.
	public static String LPN_counterFile = TestingFiles.class.getResource(readOuputDir + File.separator + "counter.lpn").getFile();
	
	
	public static String notTU1_Size1_File = TestingFiles.class.getResource(tuFileDir + File.separator + "NotTu1_Size1.xml").getFile();
	public static String notTU2_Size1_File = TestingFiles.class.getResource(tuFileDir + File.separator + "NotTu2_Size2.xml").getFile();
	public static String norTU1_Size1_File = TestingFiles.class.getResource(tuFileDir + File.separator + "Nor_LacI_TU.xml").getFile();
	public static String norTU2_Size1_File = TestingFiles.class.getResource(tuFileDir + File.separator + "Nor_LacI_TU_v2.xml").getFile();
	public static String nandTU_size1_File = TestingFiles.class.getResource(tuFileDir + File.separator + "NandTu_Size1.xml").getFile();
	public static String nandTU_size2_File = TestingFiles.class.getResource(tuFileDir + File.separator + "NandTu_Size2.xml").getFile();
	public static String pSrpR_roadblockTU_File = TestingFiles.class.getResource(tuFileDir + File.separator + "pSrpR_roadblock_11CombinatorialDerivations.xml").getFile();
	
	public static String NOT_Spec = TestingFiles.class.getResource(sbolSpecDir + File.separator + "Not_Spec.xml").getFile(); 
	public static String NAND_Spec = TestingFiles.class.getResource(sbolSpecDir + File.separator + "Nand_Spec.xml").getFile(); 
	public static String AND_Spec = TestingFiles.class.getResource(sbolSpecDir + File.separator + "And_Spec.xml").getFile(); 
	public static String OR_Spec = TestingFiles.class.getResource(sbolSpecDir + File.separator + "Or_Spec.xml").getFile(); 

	public static String NOT1_LibSize1 = TestingFiles.class.getResource(sbolLibDir + File.separator + "NOTGates_LibrarySize1.xml").getFile(); 
	public static String OR1_LibSize1 = TestingFiles.class.getResource(sbolLibDir + File.separator + "ORGates_LibrarySize1.xml").getFile(); 
	public static String NAND1_LibSize1 = TestingFiles.class.getResource(sbolLibDir + File.separator + "NANDGates_LibrarySize1.xml").getFile(); 
	public static String AND1_LibSize1 = TestingFiles.class.getResource(sbolLibDir + File.separator + "ANDGates_LibrarySize1.xml").getFile(); 
	
	public static String sbmlEvenZero_impFile  = TestingFiles.class.getResource(readOuputDir + File.separator + "evenzeroes_imp.xml").getFile();
	public static String sbmlEvenZero_tbFile   = TestingFiles.class.getResource(readOuputDir + File.separator + "evenzeroes_testbench.xml").getFile();
	public static String sbmlEvenZero_flatFile = TestingFiles.class.getResource(readOuputDir+ File.separator + "evenzeroes_imp_evenzeroes_testbench_flattened.xml").getFile();
	
	public static String verilogNotSpec_File = TestingFiles.class.getResource(verilogDir + File.separator + "Not_Spec.v").getFile();
	public static String verilogNandSpec_File = TestingFiles.class.getResource(verilogDir + File.separator + "Nand_Spec.v").getFile();
	public static String verilogAndSpec_File = TestingFiles.class.getResource(verilogDir + File.separator + "And_Spec.v").getFile();
	public static String verilogOrSpec_File = TestingFiles.class.getResource(verilogDir + File.separator + "Or_Spec.v").getFile();
	
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

	public static String yosys = TestingFiles.class.getResource(verilogDir + File.separator  + "result.v").getFile();
	
}
