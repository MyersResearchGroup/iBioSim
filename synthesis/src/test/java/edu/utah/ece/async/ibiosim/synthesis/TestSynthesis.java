package edu.utah.ece.async.ibiosim.synthesis;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * Test Synthesis 
 * 
 * @author Tramy Nguyen
 */
public class TestSynthesis {

	@Test 
	public void Test_LPNFile1() {
		Synthesis syn = new Synthesis();
		System.out.println(TestingFiles.LPN_counterFile);
		String outputDir = File.separator + String.join(File.separator, "src", "test", "resources", "edu", "utah", "ece", "async", "ibiosim", "synthesis", "outputFiles");
		outputDir =  "Users" + File.separator + "tramyn" + File.separator + "Desktop" + File.separator + "c" ;
		try {
			syn.runSynthesis(TestingFiles.LPN_counterFile, outputDir);
			
		} 
		catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
