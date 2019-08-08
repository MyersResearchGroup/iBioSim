package edu.utah.ece.async.ibiosim.synthesis;

import java.io.File;

/**
 * Files used for testing Technology Mapping package
 * @author Tramy Nguyen
 *
 */
public class TestingFiles {
	public static String verilogDir = TestingFiles.class.getResource("verilogFiles").getFile();
	public static String sbolSpecDir = TestingFiles.class.getResource("sbolSpecFiles").getFile();
	public static String sbolLibDir = TestingFiles.class.getResource("sbolLibFiles").getFile();
	public static String tuFileDir = TestingFiles.class.getResource("tuFiles").getFile();
	public static String outputDir = TestingFiles.class.getResource("outputFiles").getFile();
	public static String writeOutputDir = String.join(File.separator, "src", "test", "resources", "edu", "utah", "ece", "async", "ibiosim", "synthesis", "outputFiles");
	
	private static String getVerilogFile(String fileName) {
		return verilogDir + File.separator + fileName;
	}
	
	private static String getTuFile(String fileName) {
		return tuFileDir + File.separator + fileName;
	}
	
	private static String getSpecFile(String fileName) {
		return sbolSpecDir + File.separator + fileName;
	}
	
	private static String getLibFile(String fileName) {
		return sbolLibDir + File.separator + fileName;
	}
	
	public static String celloEcoli_tu_File = getTuFile("Cello_ecoli_tu.xml");
	public static String celloEcoli_tupAmeR_File = getTuFile("Cello_ecoli_tupAmeR.xml");
	public static String celloEcoli_tupAmtR_File = getTuFile("Cello_ecoli_tupAmtR.xml");
	public static String celloEcoli_tupBAD_File = getTuFile("Cello_ecoli_tupBAD.xml");
	public static String celloEcoli_tupBetI_File = getTuFile("Cello_ecoli_tupBetI.xml");
	public static String celloEcoli_tupBM3R1_File = getTuFile("Cello_ecoli_tupBM3R1.xml");
	public static String celloEcoli_tupHlyIIR_File = getTuFile("Cello_ecoli_tupHlyIIR.xml");
	public static String celloEcoli_tupIcaRA_File = getTuFile("Cello_ecoli_tupIcaRA.xml");
	public static String celloEcoli_tupLitR_File = getTuFile("Cello_ecoli_tupLitR.xml");
	public static String celloEcoli_tupPhlF_File = getTuFile("Cello_ecoli_tupPhlF.xml");
	public static String celloEcoli_tupPsrA_File = getTuFile("Cello_ecoli_tupPsrA.xml");
	public static String celloEcoli_tupQacR_File = getTuFile("Cello_ecoli_tupQacR.xml");
	public static String celloEcoli_tupSrpR_File = getTuFile("Cello_ecoli_tupSrpR.xml");
	public static String celloEcoli_tuReporter_File = getTuFile("Cello_ecoli_tuReporter.xml");
	public static String celloEcoli_tuSensor_File = getTuFile("Cello_ecoli_tuSensor.xml");
	
	public static String notTU1_Size1_File = getTuFile("NotTu1_Size1.xml");
	public static String notTU2_Size2_File = getTuFile("NotTu2_Size2.xml");
	public static String norTU2_Size1_File = getTuFile("Nor_LacI_TU_v2.xml");
	public static String nandTU_size1_File = getTuFile("NandTu_Size1.xml");
	public static String nandTU_size2_File = getTuFile("NandTu_Size2.xml");
	public static String lacITU_File = getTuFile("Cello_ecoli_tuLacI.xml");
	public static String tetRTU_File = getTuFile("Cello_ecoli_tuTetR.xml");
	
	public static String sbolDecompNOT_File = getSpecFile("not_decomposed.xml"); 
	public static String sbolDecompNAND_File = getSpecFile("Nand_Spec.xml"); 
	public static String sbolDecompAND_File = getSpecFile("and_decomposed.xml"); 
	public static String sbolDecompOR_File = getSpecFile("or_decomposed.xml"); 
	public static String sbolDecompSRLatch_File = getSpecFile("srlatch_decomposed.xml"); 

	public static String NOT_LibSize1 = getLibFile("NOTGates_LibrarySize1.xml"); 
	public static String NOT_LibSize2 = getLibFile("NOTGates_LibrarySize2.xml"); 
	public static String NOR_LibSize1 = getLibFile("NORGates_LibrarySize1.xml"); 
	public static String OR_LibSize1 = getLibFile("ORGates_LibrarySize1.xml"); 
	public static String NAND_LibSize1 = getLibFile("NANDGates_LibrarySize1.xml"); 
	public static String NAND_LibSize2 = getLibFile("NANDGates_LibrarySize2.xml"); 
	public static String AND_LibSize1 = getLibFile("ANDGates_LibrarySize1.xml"); 
	public static String lacINOR_LibSize1 = getLibFile("lacINORGates_LibrarySize1.xml"); 
	public static String tetRNOR_LibSize1 = getLibFile("tetRNORGates_LibrarySize1.xml"); 
	public static String yfp1NOT_LibSize1 = getLibFile("yfp1NOTGates_LibrarySize1.xml"); 
	public static String yfp2NOT_LibSize1 = getLibFile("yfp2NOTGates_LibrarySize1.xml"); 
	
	public static String verilogAlwaysBlock_File = getVerilogFile("always_block.v");
	public static String verilogAssign_File = getVerilogFile("assign.v");
	public static String verilogCondStmt1_File = getVerilogFile("conditional_stmt1.v");
	public static String verilogCondStmt2_File = getVerilogFile("conditional_stmt2.v");
	public static String verilogCondStmt3_File = getVerilogFile("conditional_stmt3.v");
	public static String verilogCont_File = getVerilogFile("contAssign.v");
	public static String verilogCont2_File = getVerilogFile("contAssign2.v");
	public static String verilogCont3_File = getVerilogFile("contAssign3.v");
	public static String verilogCont4_File = getVerilogFile("contAssign4.v");
	public static String verilogCont6_File = getVerilogFile("contAssign6.v");
	public static String verilogCont7_File = getVerilogFile("contAssign7.v");
	public static String verilogDelay_File = getVerilogFile("delay.v");
	public static String verilogInitBlock_File = getVerilogFile("init_block.v");
	public static String verilogPortMapping_File = getVerilogFile("portMapping.v");
	public static String verilogReg_File = getVerilogFile("register.v");
	public static String verilogRegAssign_File = getVerilogFile("reg_assignments.v");
	public static String verilogSystemFunc1_File = getVerilogFile("system_func1.v");
	public static String verilogSystemFunc2_File = getVerilogFile("system_func2.v");
	public static String verilogSystemFunc3_File = getVerilogFile("system_func3.v");
	public static String verilogWaitStmt_File = getVerilogFile("wait_stmt.v");
	public static String verilogWaitStmt2_File = getVerilogFile("wait_stmt2.v");
	
	public static String verilogNotDecomp_File = getVerilogFile("not_decomposed.v");
	public static String verilogAndDecomp_File = getVerilogFile("and_decomposed.v");
	public static String verilogOrDecomp_File = getVerilogFile("or_decomposed.v");
	public static String verilogNorDecomp_File = getVerilogFile("cont6_decomposed.v");
	public static String verilogCont7Decomp_File = getVerilogFile("cont7_decomposed.v");
	public static String verilogSrLatchDecomp_File = getVerilogFile("srlatch_decomposed.v");
	public static String verilogFilterDecomp_File = getVerilogFile("filter_decomposed.v");

	public static String verilogCounter_impFile = getVerilogFile("counter_imp.v");
	public static String verilogCounter_tbFile = getVerilogFile("counter_testbench.v");
	public static String verilogEvenZero_impFile = getVerilogFile("evenZeroes_imp.v");
	public static String verilogEvenZero_tbFile = getVerilogFile("evenZeroes_testbench.v");
	public static String verilogFilter_impFile = getVerilogFile("gC_imp.v");
	public static String verilogFilter_tbFile = getVerilogFile("filter_testbench.v");
	public static String verilogLFSR_impFile = getVerilogFile("lfsr_imp.v");
	public static String verilogLFSR_tbFile = getVerilogFile("lfsr_testbench.v");
	public static String verilogMultThree_impFile = getVerilogFile("multThree_imp.v");
	public static String verilogMultThree_tbFile = getVerilogFile("multThree_testbench.v");
	public static String verilogScanflop_impFile = getVerilogFile("scanflop_imp.v");
	public static String verilogScanflop_tbFile = getVerilogFile("scanflop_testbench.v");
	public static String verilogSRLatch_impFile = getVerilogFile("srlatch_imp.v");
	public static String verilogSRLatch_tbFile = getVerilogFile("srlatch_testbench.v");
	public static String verilog_gts_impFile= getVerilogFile("gts_imp.v");
	public static String verilog_gts_tbFile = getVerilogFile("gts_testbench.v");

}
