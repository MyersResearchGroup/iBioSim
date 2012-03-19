package verification.platu.partialOrders;

import java.util.HashSet;

public class DependentSet {
	HashSet<Integer> dependent;
	Integer enabledTran;
	
	public DependentSet(HashSet<Integer> dependent, Integer enabledTran) {
		this.dependent = dependent;
		this.enabledTran = enabledTran;
	}

	public HashSet<Integer> getDependent() {
		return dependent;
	}

	public Integer getEnabledTranIndex() {
		return enabledTran;
	}

}
