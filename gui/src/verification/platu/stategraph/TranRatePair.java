package verification.platu.stategraph;

import lpn.parser.Transition;

public class TranRatePair {
	Transition tran; 
	double rate;
	public TranRatePair(Transition tran, double rate) {
		this.tran = tran;
		this.rate = rate;
	}
	public Transition getTran() {
		return this.tran;
	}

}
