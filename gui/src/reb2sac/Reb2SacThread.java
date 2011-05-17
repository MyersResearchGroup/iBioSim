package reb2sac;

public class Reb2SacThread extends Thread {

	private Reb2Sac reb;

	private String direct;
	
	private boolean refresh;

	public Reb2SacThread(Reb2Sac reb2sac) {
		super(reb2sac);
		reb = reb2sac;
	}

	public void start(String string, boolean refresh) {
		direct = string;
		this.refresh = refresh;
		super.start();
	}

	@Override
	public void run() {
		reb.run(direct, refresh);
	}
}
