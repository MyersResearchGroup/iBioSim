package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
	Match_Test.class,
	MatchingSortAscending_Test.class,
	Exhaustive_Test.class,
	Greedy_Test.class,
	BranchAndBound_Test.class, 
	SBOLSolution_Test.class
})

/**
 * Test suite to execute all test cases related to SBOL Technology Mapping.
 * @author Tramy Nguyen
 */
public class SBOLTechMapTestSuite {

	
}
