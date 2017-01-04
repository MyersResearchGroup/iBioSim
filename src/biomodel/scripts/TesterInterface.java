package biomodel.scripts;

import biomodel.util.ExperimentResult;

public interface TesterInterface {
	boolean[] passedTest(ExperimentResult results);
	double[] getTimes();
}
