package analysis.markov;

import java.util.ArrayList;

public class PerfromSteadyStateMarkovAnalysisThread extends Thread {

	private StateGraph sg;

	private double tolerance;

	private ArrayList<String> conditions;

	public PerfromSteadyStateMarkovAnalysisThread(StateGraph sg) {
		super(sg);
		this.sg = sg;
	}

	public void start(double tolerance, ArrayList<String> conditions) {
		this.tolerance = tolerance;
		this.conditions = conditions;
		super.start();
	}

	@Override
	public void run() {
		sg.performSteadyStateMarkovianAnalysis(tolerance, conditions, true);
	}
}
