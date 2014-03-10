package analysis.main;

import javax.xml.stream.XMLStreamException;


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
		try {
			reb.run(direct, refresh);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
