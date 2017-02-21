package test.scripts;

import dataModels.biomodel.util.ExperimentResult;

public interface TesterInterface {
	boolean[] passedTest(ExperimentResult results);
	double[] getTimes();
}
