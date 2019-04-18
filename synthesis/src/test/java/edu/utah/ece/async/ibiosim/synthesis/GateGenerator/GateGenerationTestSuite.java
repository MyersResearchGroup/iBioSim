package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	GateIdentifier_Test.class,
	DecomposedNotGate_Test.class,
	DecomposedNandGate_Test.class,
	DecomposedAndGate_Test.class,
	DecomposedOrGate_Test.class,
	Workflow.class
})

/**
 * Test suite to execute all test cases related to GateGeneration
 * @author Tramy Nguyen
 */
public class GateGenerationTestSuite {
	
}
