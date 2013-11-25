package verification.platu.partialOrders;

import java.util.Comparator;
import java.util.HashMap;

import lpn.parser.Transition;

public class DependentSetComparator implements Comparator<DependentSet>{
	private HashMap<Transition, Integer> tranFiringFreqMap; 
	
	public DependentSetComparator(HashMap<Transition, Integer> tranFiringFreq) {
		this.tranFiringFreqMap = tranFiringFreq;
	}

	@Override
	public int compare(DependentSet dep0, DependentSet dep1) {
		if (!dep0.isEnabledTranDummy() && dep1.isEnabledTranDummy()) {
			return -1;
		}
		else if ((dep0.isEnabledTranDummy() && !dep1.isEnabledTranDummy()) || (dep0.isEnabledTranDummy() && dep1.isEnabledTranDummy()))
			return 1;
		else {
			if (dep0.getDependent().size() < dep1.getDependent().size()) 
				return -1;
			else if (dep0.getDependent().size() > dep1.getDependent().size())
				return 1;
			else {
				if (tranFiringFreqMap.get(dep0.getSeed()) < tranFiringFreqMap.get(dep1.getSeed())) 
					return -1;
				return 0;
			}
		}	
	}
}
