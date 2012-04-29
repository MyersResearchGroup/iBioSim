package verification.platu.partialOrders;

import java.util.HashSet;

public class DependentSet {
	HashSet<LpnTransitionPair> dependent;
	LpnTransitionPair seed;
	boolean enabledTranIsDummy;
	
	public DependentSet(HashSet<LpnTransitionPair> dependent, LpnTransitionPair enabledTran2, boolean enabledTranIsDummy) {
		this.dependent = dependent;
		this.seed = enabledTran2;
		this.enabledTranIsDummy = enabledTranIsDummy;
	}

	public HashSet<LpnTransitionPair> getDependent() {
		return dependent;
	}

	public LpnTransitionPair getSeed() {
		return seed;
	}
	
	public boolean isEnabledTranDummy() {
		return enabledTranIsDummy;
	}
}
