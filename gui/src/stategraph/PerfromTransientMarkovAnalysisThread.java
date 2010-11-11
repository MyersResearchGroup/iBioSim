package stategraph;

public class PerfromTransientMarkovAnalysisThread extends Thread {

	private StateGraph sg;

	private double timeLimit, error;

	private String condition;

	public PerfromTransientMarkovAnalysisThread(StateGraph sg) {
		super(sg);
		this.sg = sg;
	}

	public void start(double timeLimit, double error, String condition) {
		this.timeLimit = timeLimit;
		this.error = error;
		this.condition = condition;
		super.start();
	}

	@Override
	public void run() {
		sg.performTransientMarkovianAnalysis(timeLimit, error, condition);
	}
}
