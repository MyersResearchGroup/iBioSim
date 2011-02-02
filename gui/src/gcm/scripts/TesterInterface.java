package gcm.scripts;

import gcm.util.ExperimentResult;

public interface TesterInterface {
	boolean[] passedTest(ExperimentResult results);
	double[] getTimes();
}
