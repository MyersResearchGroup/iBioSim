package backend.analysis.markov;

import java.util.ArrayList;

import javax.swing.JProgressBar;

import backend.analysis.markov.StateGraph.Property;

public class PerfromSteadyStateMarkovAnalysisThread extends Thread {

	private StateGraph sg;

	private JProgressBar progress;
	
	private double tolerance;

	private ArrayList<Property> conditions;

	public PerfromSteadyStateMarkovAnalysisThread(StateGraph sg, JProgressBar progress) {
		super(sg);
		//Thread.setDefaultUncaughtExceptionHandler(new Utility.UncaughtExceptionHandler());
		this.sg = sg;
		this.progress = progress;
	}

	public void start(double tolerance, ArrayList<Property> conditions) {
		this.tolerance = tolerance;
		this.conditions = conditions;
		super.start();
	}

	@Override
	public void run() {
		sg.performSteadyStateMarkovianAnalysis(tolerance, conditions, null, progress);
	}
}
