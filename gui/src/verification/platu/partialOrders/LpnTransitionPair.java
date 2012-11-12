package verification.platu.partialOrders;

import java.util.HashSet;

public class LpnTransitionPair {
	private Integer lpnIndex;
	private Integer tranIndex;
	private HashSet<LpnTransitionPair> transVisited;
	
	public LpnTransitionPair(Integer lpnIndex, Integer tranIndex) {
		this.lpnIndex = lpnIndex;
		this.tranIndex = tranIndex;
		transVisited = new HashSet<LpnTransitionPair>();
		//firingCount = 0;
	}
	
	public Integer getLpnIndex() {
		return lpnIndex;
	}
	
	public Integer getTranIndex() {
		return tranIndex;
	}
	
	public void addVisitedTran(LpnTransitionPair lpnTranPair) {
		transVisited.add(lpnTranPair);
	}
	
	public HashSet<LpnTransitionPair> getVisitedTrans() {
		return transVisited;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((lpnIndex == null) ? 0 : lpnIndex.hashCode());
		result = prime * result
				+ ((tranIndex == null) ? 0 : tranIndex.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LpnTransitionPair other = (LpnTransitionPair) obj;
		if (lpnIndex == null) {
			if (other.lpnIndex != null)
				return false;
		} else if (!lpnIndex.equals(other.lpnIndex))
			return false;
		if (tranIndex == null) {
			if (other.tranIndex != null)
				return false;
		} else if (!tranIndex.equals(other.tranIndex))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LpnTransitionPair [lpnIndex=" + lpnIndex + ", tranIndex="
				+ tranIndex + "]";
	}
}
