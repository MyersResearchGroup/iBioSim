package verification.platu.partialOrders;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class DependentSetComparator implements Comparator<DependentSet>{
	private HashMap<Integer, Integer> tranFiringFreqMap; 
	
	public DependentSetComparator(HashMap<Integer, Integer> tranFiringFreqMap) {
		this.tranFiringFreqMap = tranFiringFreqMap;
	}

	public int compare(DependentSet dep0, DependentSet dep1) {
		
		if (dep0.getDependent().size() < dep1.getDependent().size()) 
			return -1;
		else if (dep0.getDependent().size() > dep1.getDependent().size())
			return 1;
		else {
			if (tranFiringFreqMap.get(dep0.getEnabledTranIndex()) < tranFiringFreqMap.get(dep1.getEnabledTranIndex())) 
				return -1;
			else 
				return 0;
		}
	}



}
