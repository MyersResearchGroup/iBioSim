package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	Test_LibrarySize1.class
})

/**
 * Test suite to execute all test cases related to SBOL Technology Mapping.
 * @author tramyn
 *
 */
public class SBOLTechMapTestSuite {
	
	protected static SBOLTechMap_TestEnvironment testEnv = new SBOLTechMap_TestEnvironment();
	private static final String resourceDir = "src" + File.separator + "test" + File.separator + "resources";
	protected static String sbolSpecDir = resourceDir + File.separator + "sbolSpecFiles";
	
	protected static String sbolLibDir = resourceDir + File.separator + "sbolLibFiles";
	
	protected static String NOT_Spec = sbolSpecDir + File.separator + "NOT_spec.xml"; 

	protected static String NOT_LibSize1 = sbolLibDir + File.separator + "NOTGate_LibrarySize1.xml"; 
	protected static String NOT2_LibSize1 = sbolLibDir + File.separator + "NOTGate2_LibrarySize1.xml"; 


}
