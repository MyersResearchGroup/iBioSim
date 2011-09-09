package platu.lpn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import platu.stategraph.State;
import platu.stategraph.StateGraph;

/**
 * The LPNTranRelation class stores information about LPN transitions such  as dependence, 
 * interleaving, and independence.
 */
public class LPNTranRelation {
	private List<StateGraph> designUnitSet = null;
	private Map<LPNTran, Set<LPNTran>> transitiveDependence = new HashMap<LPNTran, Set<LPNTran>>();  // type 1 transitive
	private Map<LPNTran, Set<LPNTran>> interleavingDependence = new HashMap<LPNTran, Set<LPNTran>>();  // type 2 interleaving
//	public Map<LPNTran, Set<LPNTran>> case2 = new HashMap<LPNTran, Set<LPNTran>>();
	
	public LPNTranRelation(List<StateGraph> designUnitSet){
		this.designUnitSet = designUnitSet;
	}
	
	/**
	 * Finds pairs of transitions which are dependent locally and between modules.  Identifies two types of dependencies:
	 * 1) transitive
	 * 2) interleaving
	 * This function assumes findSgCompositional() has been called.
	 * Type 1 pairs are stored in Map transitiveDependence, and type 2 pairs are stored in Map interleavingDependence.
	 */
	public void findCompositionalDependencies(){
		for(StateGraph sg : this.designUnitSet){
			//for(State currentState : sg.reachable()){
			for(State currentState : sg.getStateSet()) {
				Set<Entry<LPNTran, State>> stateTranSet = sg.getOutgoingTrans(currentState);
				if(stateTranSet == null) continue;
				
				for(Entry<LPNTran, State> stateTran: stateTranSet){
					State startState = currentState;
					State endState = stateTran.getValue();
					LPNTran lpnTran = stateTran.getKey();
					
					//TODO: only get enabled trans from lpn or input transitions also?
					LpnTranList currentEnabledTransitions = sg.getEnabled(startState);
		        	LpnTranList nextEnabledTransitions = sg.getEnabled(endState);
		        	
		        	// disabled trans
		        	LpnTranList current_minus_next = currentEnabledTransitions.clone();
		        	current_minus_next.removeAll(nextEnabledTransitions);
		        	current_minus_next.remove(lpnTran);
		        	
		        	// type 2
		        	for(LPNTran disabledTran: current_minus_next){
		        		// t1 -> t2
		        		if(interleavingDependence.containsKey(lpnTran)){
		        			interleavingDependence.get(lpnTran).add(disabledTran);
		        		}
		        		else{
		        			Set<LPNTran> tranSet = new HashSet<LPNTran>();
		        			tranSet.add(disabledTran);
		    				interleavingDependence.put(lpnTran, tranSet);
		        		}
		        		
		        		if(interleavingDependence.containsKey(disabledTran)){
		        			interleavingDependence.get(disabledTran).add(lpnTran);
		        		}
		        		else{
		        			Set<LPNTran> tranSet = new HashSet<LPNTran>();
		        			tranSet.add(lpnTran);
		    				interleavingDependence.put(disabledTran, tranSet);
		        		}
		        	}
		        	
		        	// enabled trans
		        	LpnTranList next_minus_current = nextEnabledTransitions;
		        	next_minus_current.removeAll(currentEnabledTransitions);
		        	
		        	// type 1
		        	for(LPNTran enabledTran: next_minus_current){
	        			// t1 -> t2
		        		if(transitiveDependence.containsKey(lpnTran)){
		        			transitiveDependence.get(lpnTran).add(enabledTran);
		        		}
		        		else{
		        			Set<LPNTran> tranSet = new HashSet<LPNTran>();
		        			tranSet.add(enabledTran);
		    				transitiveDependence.put(lpnTran, tranSet);
		        		}
		        	}
		        	
		        	LpnTranList remainEnabled = currentEnabledTransitions;
		        	remainEnabled.removeAll(current_minus_next);
		        	for(LPNTran remainTran : remainEnabled){
		        		State s1 = sg.getNextState(currentState, remainTran);
		        		if(s1 == null) continue;
		        		
		        		LpnTranList en = sg.getEnabled(s1);
		        		if(!en.contains(lpnTran)) continue;
		        		
		        		State s3 = sg.getNextState(s1, lpnTran);
		        		State s2 = sg.getNextState(endState, remainTran);
		        		if(s2 == null) continue;
		        		if(s3 == null) continue;
		        		
		        		if(s2 != s3){
			        		if(interleavingDependence.containsKey(lpnTran)){
			        			interleavingDependence.get(lpnTran).add(remainTran);
			        		}
			        		else{
			        			Set<LPNTran> tranSet = new HashSet<LPNTran>();
			        			tranSet.add(remainTran);
			        			interleavingDependence.put(lpnTran, tranSet);
			        		}
			        		
			        		if(interleavingDependence.containsKey(remainTran)){
			        			interleavingDependence.get(remainTran).add(lpnTran);
			        		}
			        		else{
			        			Set<LPNTran> tranSet = new HashSet<LPNTran>();
			        			tranSet.add(lpnTran);
			        			interleavingDependence.put(remainTran, tranSet);
			        		}
			        		
	//		        		if(case2.containsKey(lpnTran)){
	//		        			case2.get(lpnTran).add(remainTran);
	//		        		}
	//		        		else{
	//		        			Set<LPNTran> tranSet = new HashSet<LPNTran>();
	//		        			tranSet.add(remainTran);
	//		        			case2.put(lpnTran, tranSet);
	//		        		}
		        		}
		        	}
				}
			}
		}
	}
	
	/**
     * Returns entry set of Map transitiveDependence, where the key value is an LPNTran 
     * and the value is a list of LPNTran which have transitive dependence.
     * @return Map transitiveDependence entry set
     */
	public Set<Entry<LPNTran, Set<LPNTran>>> getDependentTrans(){
		return this.transitiveDependence.entrySet();
	}
	
	/**
     * Returns entry set of Map interleavingDependence, where the key value is an LPNTran 
     * and the value is a list of LPNTran which have interleaving dependence.
     * @return Map interleavingDependence entry set
     */
	public Set<Entry<LPNTran, Set<LPNTran>>> getInterleavingTrans(){
		return this.interleavingDependence.entrySet();
	}
}
