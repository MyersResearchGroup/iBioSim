package verification.platu.markovianAnalysis;

public class TransientMarkovMatrixMultiplyThread extends Thread {

	private MarkovianAnalysis markovianAnalysis;

	private double Gamma, timeLimit;

	private int startIndex, endIndex, K;

	public TransientMarkovMatrixMultiplyThread(MarkovianAnalysis markovianAnalysis) {
		super(markovianAnalysis);
		this.markovianAnalysis = markovianAnalysis;
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
		markovianAnalysis.transientMarkovMatrixMultiplication(startIndex, endIndex, Gamma, timeLimit, K);
	}
}
