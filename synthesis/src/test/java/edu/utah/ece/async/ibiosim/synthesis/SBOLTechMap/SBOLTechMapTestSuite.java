package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	MatchNotSpecification1_Test.class,
	MatchOrSpecification_Test.class,
	MatchNandSpecification1_Test.class,
	MatchNandSpecification2_Test.class,
	MatchNandSpecification3_Test.class,
	MatchAndSpecification_Test.class
})

/**
 * Test suite to execute all test cases related to SBOL Technology Mapping.
 * @author Tramy Nguyen
 */
public class SBOLTechMapTestSuite {
	
}
