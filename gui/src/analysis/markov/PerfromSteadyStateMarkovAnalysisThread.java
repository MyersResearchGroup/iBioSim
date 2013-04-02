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

	public void start(double tolerance, ArrayList<String> props) {
		this.tolerance = tolerance;
		conditions = new ArrayList<Property>();
		for (String prop : props) {
			conditions.add(sg.createProperty(prop, prop));
		}
		super.start();
	}

	@Override
	public void run() {
		sg.performSteadyStateMarkovianAnalysis(tolerance, conditions, null);
	}
}
