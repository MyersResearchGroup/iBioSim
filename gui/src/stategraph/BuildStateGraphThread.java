package stategraph;

public class BuildStateGraphThread extends Thread {

	private StateGraph sg;

	public BuildStateGraphThread(StateGraph sg) {
		super(sg);
		this.sg = sg;
	}

	public void start() {
		super.start();
	}

	public void run() {
		sg.buildStateGraph();
	}
}
