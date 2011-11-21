package analysis;

public class AnalysisThread extends Thread {

	private AnalysisView reb;

	private String direct;

	private boolean refresh;

	public AnalysisThread(AnalysisView reb2sac) {
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
