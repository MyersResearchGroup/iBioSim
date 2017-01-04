package backend.analysis.markov;

import javax.swing.JProgressBar;

public class BuildStateGraphThread extends Thread {

	private StateGraph sg;

	private JProgressBar progress;

	public BuildStateGraphThread(StateGraph sg, JProgressBar progress) {
		super(sg);
		this.sg = sg;
		this.progress = progress;
	}

	@Override
	public synchronized void start() {
		super.start();
	}

	@Override
	public void run() {
		sg.buildStateGraph(progress);
	}
}
