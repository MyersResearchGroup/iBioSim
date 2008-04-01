package gcm2sbml.scripts;

import java.util.HashMap;

public interface TesterInterface {
	boolean[] passedTest(HashMap<String, double[]> experiment);
}
