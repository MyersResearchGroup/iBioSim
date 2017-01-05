package backend.biomodel.scripts;

import backend.biomodel.util.ExperimentResult;

public interface TesterInterface {
	boolean[] passedTest(ExperimentResult results);
	double[] getTimes();
}
