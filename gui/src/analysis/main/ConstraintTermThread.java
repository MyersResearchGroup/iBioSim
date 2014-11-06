package analysis.main;

import java.util.ArrayList;

public class ConstraintTermThread extends Thread {

	private AnalysisView analysisView;

	private ArrayList<AnalysisThread> threads;

	private ArrayList<String> dirs, levelOne;

	private String stem;

	public ConstraintTermThread(AnalysisView analysisView) {
		super(analysisView);
		this.analysisView = analysisView;
	}

	public void start(ArrayList<AnalysisThread> threads, ArrayList<String> dirs, ArrayList<String> levelOne, String stem) {
		this.threads = threads;
		this.dirs = dirs;
		this.levelOne = levelOne;
		this.stem = stem;
		super.start();
	}

	@Override
	public void run() {
		analysisView.run(threads, dirs, levelOne, stem);
	}
}
