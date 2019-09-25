package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import org.junit.Test;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapRunner;

public class Workflow_Test {

	@Test 
	public void Test_BranchBound() {
		String files = String.join(" ", TestingFiles.yfp1NOT_LibSize1, TestingFiles.yfp2NOT_LibSize1, TestingFiles.lacINOR_LibSize1, TestingFiles.tetRNOR_LibSize1);
		String[] cmd = {"-l", files, "-s", TestingFiles.sbolDecompSRLatch_File, "-bb",
				"-od", TestingFiles.writeOutputDir, "-o", "tm_solution"};
		
		SBOLTechMapRunner.main(cmd);
	}
	
	@Test 
	public void Test_Exhuastive() {
		String files = String.join(" ", TestingFiles.NOT_LibSize1);
		String[] cmd = {"-l", files, "-s", TestingFiles.sbolDecompNOT_File, "-e",
				"-od", TestingFiles.writeOutputDir, "-o", "tm_solution"};
		
		SBOLTechMapRunner.main(cmd);
	}
	
	@Test 
	public void Test_Greedy() {
		String files = String.join(" ", TestingFiles.yfp1NOT_LibSize1, TestingFiles.yfp2NOT_LibSize1, TestingFiles.lacINOR_LibSize1, TestingFiles.tetRNOR_LibSize1);
		String[] cmd = {"-l", files, "-s", TestingFiles.sbolDecompSRLatch_File, "-g", "-nsol", "3",
				"-od", TestingFiles.writeOutputDir, "-o", "tm_solution"};
		
		SBOLTechMapRunner.main(cmd);
	}
	
	@Test 
	public void Test_Nand() {
		String files = String.join(" ", TestingFiles.srlatchNand_Lib); 
		String[] cmd = {"-l", files, "-s", TestingFiles.sbolNandDecompSRLatch_File, "-e",
				"-od", TestingFiles.writeOutputDir, "-o", "nandTm"};
		
		SBOLTechMapRunner.main(cmd);
	}
	
	@Test 
	public void Test_gc() {
		String files = "/Users/tramyn/Desktop/TechMap/library3";
		String spec = "/Users/tramyn/Desktop/TechMap/xAE.xml";
		String[] preselect = new String[] {
				"http://dummy.org/MD0xAE/FC3_out/1.0",
				"https://synbiohub.programmingbiology.org/public/Cello_Parts/YFP_protein/1",
//				"http://dummy.org/MD0Start_Sensor_Actuator_net/FC0_Sensor/1.0",
//				"https://synbiohub.programmingbiology.org/public/Cello_Parts/LacI_protein/1", 
//				"http://dummy.org/MD0Start_Sensor_Actuator_net/FC1_Start/1.0",
//				"https://synbiohub.programmingbiology.org/public/Cello_Parts/TetR_protein/1",
		};
		String[] cmd = {"-l", files, "-s", spec, "-bb", //"-nsol", "3",
				//"-ps", String.join(" ", preselect),
				"-od", TestingFiles.writeOutputDir, "-o", "xAE_bb"};
		
		SBOLTechMapRunner.main(cmd);
	}
}
