package stategraph;

import java.util.ArrayList;

public class PerfromMarkovAnalysisThread extends Thread {

	private StateGraph sg;
	
	private ArrayList<String> conditions;

	public PerfromMarkovAnalysisThread(StateGraph sg) {
		super(sg);
		this.sg = sg;
	}

	public void start(ArrayList<String> conditions) {
		this.conditions = conditions;
		super.start();
	}

	public void run() {
		sg.performMarkovianAnalysis(conditions);
	}
}
