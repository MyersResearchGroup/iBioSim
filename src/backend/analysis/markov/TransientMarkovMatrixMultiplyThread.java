package backend.analysis.markov;

public class TransientMarkovMatrixMultiplyThread extends Thread {

	private StateGraph sg;

	private double Gamma, timeLimit;

	private int startIndex, endIndex, K;

	public TransientMarkovMatrixMultiplyThread(StateGraph sg) {
		super(sg);
		this.sg = sg;
	}

	public void start(int startIndex, int endIndex, double Gamma, double timeLimit, int K) {
		this.timeLimit = timeLimit;
		this.Gamma = Gamma;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.K = K;
		super.start();
	}

	@Override
	public void run() {
		sg.transientMarkovMatrixMultiplication(startIndex, endIndex, Gamma, timeLimit, K);
	}
}
