package biomodel.scripts;


import java.util.ArrayList;

import biomodel.util.ExperimentResult;


abstract public class AbstractTester implements TesterInterface {

	public AbstractTester(ArrayList<String> highSpecies,
			ArrayList<String> lowSpecies, double[] highThreshold,
			double[] lowThreshold, double timeStart, double timeSpan, double timeEnd) {
		this.highSpecies = highSpecies;
		this.lowSpecies = lowSpecies;
		this.highThreshold = highThreshold;
		this.lowThreshold = lowThreshold;
		this.timeSpan = timeSpan;
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
	}

	@Override
	public boolean[] passedTest(ExperimentResult experiment) {
		boolean[] results = new boolean[(int)((timeEnd-timeStart)/timeSpan+1)];
		for (int i = 0; i < results.length; i++) {
			results[i] = true;
		}
		//int totalValues = highSpecies.size() + lowSpecies.size();
		int index = 0;
		for (double i = timeStart; i <= timeEnd; i=i+timeSpan) {
			int numPassed = 0;
			for(int j = 0; j < highSpecies.size(); j++) {
				if (experiment.getValue(highSpecies.get(j), i) >= highThreshold[j]) {
					numPassed++;
				}
			}
			for(int j = 0; j < lowSpecies.size(); j++) {
				if (experiment.getValue(lowSpecies.get(j), i) <= lowThreshold[j]) {
					numPassed++;
				}
			}
			if (numPassed == 0) {
//				if (i == timeStart) {
//					System.out.println();
//				}
				for (int k = index; k < results.length; k++) {
					results[k] = false;					
				}
				break;
			}
			index++;
		}
		return results;
	}
	
	@Override
	public double[] getTimes() {
		double[] times = new double[(int)((timeEnd-timeStart)/timeSpan+1)];
		for (double i = timeStart; i <= timeEnd; i=i+timeSpan) {
			times[(int)((i-timeStart)/timeSpan)] = i-timeStart;
		}
		return times;
	}

	protected double timeSpan = -1;
	protected double timeStart = -1;
	protected double timeEnd = -1;
	protected ArrayList<String> highSpecies = null;
	protected ArrayList<String> lowSpecies = null;
	protected double[] highThreshold = null;
	protected double[] lowThreshold = null;	
}

