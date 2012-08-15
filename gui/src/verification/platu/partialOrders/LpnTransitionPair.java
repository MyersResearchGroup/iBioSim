package verification.platu.partialOrders;

import java.util.HashSet;

public class LpnTransitionPair {
	private Integer lpnIndex;
	private Integer tranIndex;
	private HashSet<LpnTransitionPair> transVisited;
	private int hashVal;
	
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
		if(hashVal == 0){
			final int prime = 31;
			int result = 1;
			result = prime * result + lpnIndex.hashCode();
			result = prime * result + tranIndex.hashCode();
			//result = prime * result + tranSetVisitedByNecessary.hashCode();
			hashVal = result;
		}	
		return hashVal;
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
		if (!lpnIndex.equals(other.lpnIndex)) 
			return false;
		if (!tranIndex.equals(other.tranIndex))
			return false;
		return true;
	}

//	public void resetFiringCount() {
//		firingCount = 0;
//	}
//
//	public void increaseFiringCount() {
//		firingCount++ ;
//		
//	}
//
//	public int getFiringCount() {
//		return firingCount;
//	}
}
