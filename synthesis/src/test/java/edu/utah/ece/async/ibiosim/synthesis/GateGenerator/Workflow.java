package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import org.junit.Test;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;


/**
 *  
 * Test end to end process for running GateGeneration. 
 * This process involves reading in a list of SBOL files built from SBOLDesigner and calling VPR model generation.
 * Types of enriched data are formed into different types of logic gates. 
 * 
 * @author Tramy Nguyen
 */
public class Workflow {
	
	@Test 
	public void Test_NOTGate() {
		String files = String.join(" ", TestingFiles.notTU1_Size1_File);
		String[] cmd = {"-f", files, "-sbh", "https://synbiohub.programmingbiology.org/", "-NOT", "-od", TestingFiles.writeLibDir};
		
		GateGenerationRunner.main(cmd);
	}
	
	@Test 
	public void Test_NOT2Gate() {
		String files = String.join(" ", TestingFiles.notTU2_Size1_File);
		String[] cmd = {"-f", files, "-sbh", "https://synbiohub.programmingbiology.org/", "-NOT", "-od", TestingFiles.writeLibDir};
		
		GateGenerationRunner.main(cmd);
	}
	
	@Test 
	public void Test_NORGate() {
		String files = String.join(" ", TestingFiles.norTU2_Size1_File);
		String[] cmd = {"-f", files, "-sbh", "https://synbiohub.programmingbiology.org/", "-NOR", "-od", TestingFiles.writeLibDir};
		
		GateGenerationRunner.main(cmd);
	}

	@Test 
	public void Test_NANDGate() {
		String files = String.join(" ", TestingFiles.nandTU_size2_File);
		String[] cmd = {"-f", files, "-sbh", "https://synbiohub.programmingbiology.org/", "-NAND", "-od", TestingFiles.writeLibDir};
		
		GateGenerationRunner.main(cmd);
	}

	@Test 
	public void Test_Gate() {
		String files = String.join(" ", TestingFiles.yfp2TU_File);
		String[] cmd = {"-f", files, "-sbh", "https://synbiohub.programmingbiology.org/", "-NOT", "-od", TestingFiles.writeLibDir};
		
		GateGenerationRunner.main(cmd);
	}
}
