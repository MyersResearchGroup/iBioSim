package reb2sac;

import java.util.ArrayList;

public class ConstraintTermThread extends Thread {

	private Reb2Sac reb;

	private ArrayList<Reb2SacThread> threads;

	private ArrayList<String> dirs;

	public ConstraintTermThread(Reb2Sac reb2sac) {
		super(reb2sac);
		reb = reb2sac;
	}

	public void start(ArrayList<Reb2SacThread> threads, ArrayList<String> dirs) {
		this.threads = threads;
		this.dirs = dirs;
		super.start();
	}

	@Override
	public void run() {
		reb.run(threads, dirs);
	}
}
