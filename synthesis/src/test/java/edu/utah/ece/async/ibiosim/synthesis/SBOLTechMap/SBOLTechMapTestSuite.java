package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	LibrarySize1_Test1.class,
	LibrarySize1_Test2.class,
	LibrarySize2_Test1.class,
	LibrarySize2_Test2.class,
	LibrarySize3_Test1.class,
	LibrarySize6_Test1.class
})

/**
 * Test suite to execute all test cases related to SBOL Technology Mapping.
 * @author Tramy Nguyen
 */
public class SBOLTechMapTestSuite {
	
	private static final String resourceDir = "src" + File.separator + "test" + File.separator + "resources";
	protected static String sbolSpecDir = resourceDir + File.separator + "sbolSpecFiles";
	
	protected static String sbolLibDir = resourceDir + File.separator + "sbolLibFiles";
	
	protected static String NOT_Spec = sbolSpecDir + File.separator + "notDesign.xml"; 
	protected static String NOT2_Spec = sbolSpecDir + File.separator + "not2Design.xml"; 
	protected static String NOTNOR_Spec = sbolSpecDir + File.separator + "notNorDesign.xml"; 
	protected static String NORNOT_Spec = sbolSpecDir + File.separator + "norNotDesign.xml"; 
	protected static String SRLATCH_Spec = sbolSpecDir + File.separator + "srlatchDesign.xml"; 

	protected static String NOT1_LibSize1 = sbolLibDir + File.separator + "NOTGate_LibrarySize1.xml"; 
	protected static String NOR1_LibSize1 = sbolLibDir + File.separator + "NORGate_LibrarySize1.xml"; 

	protected static String NOT2_LibSize2 = sbolLibDir + File.separator + "NOTGates_LibrarySize2.xml"; 
	protected static String NORNOT_LibSize2 = sbolLibDir + File.separator + "NORNOTGates_LibrarySize2.xml"; 
	
	protected static String NORNOT_LibSize3 = sbolLibDir + File.separator + "NORNOTGates_LibrarySize3.xml"; 
	protected static String NORNOT_LibSize6 = sbolLibDir + File.separator + "NORNOTGates_LibrarySize6.xml"; 
	
}
