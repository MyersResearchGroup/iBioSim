package reb2sac;

import java.util.ArrayList;

public class ConstraintTermThread extends Thread {

	private Reb2Sac reb;

	private ArrayList<Reb2SacThread> threads;

	private ArrayList<String> dirs, levelOne;

	private String stem;

	public ConstraintTermThread(Reb2Sac reb2sac) {
		super(reb2sac);
		reb = reb2sac;
	}

	public void start(ArrayList<Reb2SacThread> threads, ArrayList<String> dirs, ArrayList<String> levelOne, String stem) {
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
