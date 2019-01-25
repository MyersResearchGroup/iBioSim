package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	NOTGate_Test.class
})

/**
 * Test suite to execute all test cases related to GateGeneration
 * @author Tramy Nguyen
 */
public class GateGeneratorTestSuite {
	
	protected static String outputDirectory = "src" + File.separator + "test" + File.separator + 
			"resources" + File.separator + "sbolLibFiles";
	
	private static String tuFileDirectory = File.separator + "tuFiles";

	protected static String notTU1_File = GateGeneratorTestSuite.class.getResource(tuFileDirectory + File.separator +  "not1TU.xml").getFile();
	protected static String norTU1_File = GateGeneratorTestSuite.class.getResource(tuFileDirectory + File.separator +  "norTU.xml").getFile();
}
