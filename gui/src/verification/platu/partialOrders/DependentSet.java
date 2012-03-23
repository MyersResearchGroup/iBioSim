package verification.platu.partialOrders;

import java.util.HashSet;

public class DependentSet {
	HashSet<Integer> dependent;
	Integer enabledTran;
	boolean enabledTranIsDummy;
	
	public DependentSet(HashSet<Integer> dependent, Integer enabledTran, boolean enabledTranIsDummy) {
		this.dependent = dependent;
		this.enabledTran = enabledTran;
		this.enabledTranIsDummy = enabledTranIsDummy;
	}

	public HashSet<Integer> getDependent() {
		return dependent;
	}

	public Integer getEnabledTranIndex() {
		return enabledTran;
	}
	
	public boolean isEnabledTranDummy() {
		return enabledTranIsDummy;
	}
}
