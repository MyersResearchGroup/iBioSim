package verification.platu.partialOrders;

import java.util.LinkedList;

import lpn.parser.Transition;
import verification.platu.lpn.LpnTranList;

public class AmpleSet {
 	private LpnTranList ampleSet;
 	private boolean ampleChanged;
	
//	public AmpleSet(State initState, HashMap<Integer,HashMap<Integer,StaticSets>> staticSetsMap) {
//		this.initState = initState;
//		this.staticArray = staticSetsMap;
//	}
	public AmpleSet() {
		ampleChanged = false;
		ampleSet = new LpnTranList();
	}

	public LinkedList<Transition> getAmpleSet() {
		return ampleSet;
	}
	
	public void setAmpleChanged() {
		ampleChanged = true;
	}
	
	public boolean getAmpleChanged() {
		return ampleChanged;
	}

	public void addAmpleSet(Object object) {
		this.ampleSet = (LpnTranList) object;
		
	}

	
}
