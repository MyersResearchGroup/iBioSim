package gcm2sbml.scripts;

import gcm2sbml.util.ExperimentResult;

public interface TesterInterface {
	boolean[] passedTest(ExperimentResult results);
	double[] getTimes();
}
