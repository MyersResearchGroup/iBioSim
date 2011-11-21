package analysis;

import java.util.ArrayList;

public class ConstraintTermThread extends Thread {

	private AnalysisView reb;

	private ArrayList<AnalysisThread> threads;

	private ArrayList<String> dirs, levelOne;

	private String stem;

	public ConstraintTermThread(AnalysisView reb2sac) {
		super(reb2sac);
		reb = reb2sac;
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
		reb.run(threads, dirs, levelOne, stem);
	}
}
