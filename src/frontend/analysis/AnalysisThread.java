package frontend.analysis;

public class AnalysisThread extends Thread {

	private AnalysisView analysisView;

	private String directory;

	private boolean refresh;

	public AnalysisThread(AnalysisView analysisView) {
		super(analysisView);
		this.analysisView = analysisView;
	}

	public void start(String directory, boolean refresh) {
		this.directory = directory;
		this.refresh = refresh;
		super.start();
	}

	@Override
	public void run() {
		analysisView.run(directory, refresh);
	}
}
