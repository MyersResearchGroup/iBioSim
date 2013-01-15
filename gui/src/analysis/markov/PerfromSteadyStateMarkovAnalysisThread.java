package analysis.markov;

import java.util.ArrayList;
import analysis.markov.StateGraph.Property;

import main.Gui;

public class PerfromSteadyStateMarkovAnalysisThread extends Thread {

	private StateGraph sg;

	private double tolerance;

	private ArrayList<Property> conditions;

	public PerfromSteadyStateMarkovAnalysisThread(StateGraph sg) {
		super(sg);
		Thread.setDefaultUncaughtExceptionHandler(new Gui.UncaughtExceptionHandler());
		this.sg = sg;
	}

	public void start(double tolerance, ArrayList<Property> conditions) {
		this.tolerance = tolerance;
		this.conditions = conditions;
		super.start();
	}

	@Override
	public void run() {
		sg.performSteadyStateMarkovianAnalysis(tolerance, conditions, null);
	}
}
