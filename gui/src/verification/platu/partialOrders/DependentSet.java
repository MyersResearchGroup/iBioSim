package verification.platu.partialOrders;

import java.util.HashSet;
import lpn.parser.Transition;

public class DependentSet {
	HashSet<Transition> dependent;
	Transition seed;
	boolean enabledTranIsDummy;
	
	public DependentSet(HashSet<Transition> dependent, Transition enabledTran2, boolean enabledTranIsDummy) {
		this.dependent = dependent;
		this.seed = enabledTran2;
		this.enabledTranIsDummy = enabledTranIsDummy;
	}

	public HashSet<Transition> getDependent() {
		return dependent;
	}

	public Transition getSeed() {
		return seed;
	}
	
	public boolean isEnabledTranDummy() {
		return enabledTranIsDummy;
	}
}
