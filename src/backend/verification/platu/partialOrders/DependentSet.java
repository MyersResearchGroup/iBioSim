package backend.verification.platu.partialOrders;

import java.util.HashSet;

import backend.lpn.parser.Transition;

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

	/**
	 * For each transition in <code>dependent</code>, check its LPN index. 
	 * @return the lowest LPN index found in the <code>dependent</code> set.
	 */
	public int getLowestLpnNumber(int highestLpnIndex) {
		int lowestLpnIndex = highestLpnIndex;
		for (Transition t: dependent) {
			if (t.getLpn().getLpnIndex() < lowestLpnIndex)
				lowestLpnIndex = t.getLpn().getLpnIndex();
		}
		return lowestLpnIndex;
	}
}
