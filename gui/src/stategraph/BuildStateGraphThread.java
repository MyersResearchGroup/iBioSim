package stategraph;

public class BuildStateGraphThread extends Thread {

	private StateGraph sg;

	public BuildStateGraphThread(StateGraph sg) {
		super(sg);
		this.sg = sg;
	}

	@Override
	public void start() {
		super.start();
	}

	@Override
	public void run() {
		sg.buildStateGraph();
	}
}
