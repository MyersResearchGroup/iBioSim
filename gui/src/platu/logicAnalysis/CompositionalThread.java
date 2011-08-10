package platu.logicAnalysis;

import java.util.ArrayList;
import java.util.List;

import platu.expression.VarNode;
import platu.lpn.LPN;
import platu.lpn.LPNTran;
import platu.stategraph.StateGraph;
import platu.stategraph.state.State;

public class CompositionalThread extends Thread{
	
	private StateGraph[] srcArray = null;
	private StateGraph sg = null;
	private int iter = 0;
	private int newTransitions = 0;
	
	public CompositionalThread(StateGraph sg, StateGraph[] srcArray, int iter){
		this.srcArray = srcArray;
		this.sg = sg;
		this.iter = iter;
	}
	
	public int getNewTransitions(){
		return this.newTransitions;
	}
	
	public StateGraph getStateGraph(){
		return this.sg;
	}
	
	public void run(){
		List<Constraint> newConstraintSet = new ArrayList<Constraint>();
		List<Constraint> oldConstraintSet = new ArrayList<Constraint>();
		
		for(StateGraph srcSG : this.srcArray){
			extractConstraints(sg, srcSG, newConstraintSet, oldConstraintSet);
			newTransitions += applyConstraintSet(sg, srcSG, iter, newConstraintSet, oldConstraintSet);
		}
	}
	
	/**
	 * Applies new constraints to the entire state set, and applies old constraints to the frontier state set.
     * @return Number of new transitions.
     */
	private int applyConstraintSet(StateGraph sg, StateGraph srcSG, int iter, List<Constraint> newConstraintSet, List<Constraint> oldConstraintSet){
		int newTransitions = 0;
//		int[] thisIndexList = null;
//		int[] otherIndexList = null;
//		
//		String label = srcSG.getLabel();
//		String[] indexArray = sg.getKeyArray();
//		
//		for(int i = 0; i < indexArray.length; i++){
//			if(label == indexArray[i]){
//				thisIndexList = sg.getThisIndexArray(i);
//				otherIndexList = sg.getOtherIndexArray(i);
//				break;
//			}
//		}
		
//		int index = sg.getInterfaceIndex(srcSG.getLabel());
//		int[] thisIndexList = sg.getThisIndexArray(index);
//		int[] otherIndexList = sg.getOtherIndexArray(index);
		int[] thisIndexList = sg.getThisIndexArray(srcSG.ID);
		int[] otherIndexList = sg.getOtherIndexArray(srcSG.ID);
		
		if(newConstraintSet.size() > 0){
			for(Object obj : sg.getStateSet().toArray()){
				platu.stategraph.state.State currentState = (platu.stategraph.state.State) obj;

				for(Constraint c : newConstraintSet){
					if(compatible(currentState, c, thisIndexList, otherIndexList)){
						newTransitions += createNewState(sg, currentState, c);
					}
				}
			}

			for(platu.stategraph.state.State currentState : sg.getFrontierStateSet()){
				for(Constraint c : newConstraintSet){
					if(compatible(currentState, c, thisIndexList, otherIndexList)){						
						newTransitions += createNewState(sg, currentState, c);
					}
				}
			}
		}

		if(oldConstraintSet.size() > 0){
			for(platu.stategraph.state.State currentState : sg.getFrontierStateSet()){
				for(Constraint c : oldConstraintSet){
					if(compatible(currentState, c, thisIndexList, otherIndexList)){						
						newTransitions += createNewState(sg, currentState, c);
					}
				}
			}
		}
		
		return newTransitions;
	}
	
	/**
     * Extracts applicable constraints from a StateGraph.
     * @param sg The state graph the constraints are to be applied.
     * @param srcSG The state graph the constraint are extracted from.
     */
	private void extractConstraints(StateGraph sg, StateGraph srcSG, List<Constraint> newConstraintSet, List<Constraint> oldConstraintSet){
		newConstraintSet.clear();
		oldConstraintSet.clear();
		
		LPN srcLpn = (LPN) srcSG;
		for(Constraint newConstraint : sg.getNewConstraintSet()){
			if(newConstraint.getLpn() != srcLpn) continue;
	    	
			newConstraintSet.add(newConstraint);
		}
		
		for(Constraint oldConstraint : sg.getOldConstraintSet()){
			if(oldConstraint.getLpn() != srcLpn) continue;
			
			oldConstraintSet.add(oldConstraint);
		}
	}
	
	/**
     * Determines whether a constraint is compatible with a state.
     * @return True if compatible, otherwise False.
     */
	private boolean compatible(platu.stategraph.state.State currentState, Constraint constr, int[] thisIndexList, int[] otherIndexList){
		int[] constraintVector = constr.getVector();
		int[] currentVector = currentState.getVector();
		
		for(int i = 0; i < thisIndexList.length; i++){
			int thisIndex = thisIndexList[i];
			int otherIndex = otherIndexList[i];

			if(currentVector[thisIndex] != constraintVector[otherIndex]){
				return false;
			}
		}
		
		return true;
	}
	
	/**
     * Creates a state from a given constraint and compatible state.  If the state is new, then findSG is called.
     * @return Number of new transitions.
     */
	private int createNewState(StateGraph sg, platu.stategraph.state.State compatibleState, Constraint c){
		int newTransitions = 0;

		// Create new state and insert into state graph
		platu.stategraph.state.State newState = new platu.stategraph.state.State(compatibleState);
		int[] newVector = newState.getVector();
		
		List<VarNode> variableList = c.getVariableList();
		List<Integer> valueList = c.getValueList();
		
		for(int i = 0; i < variableList.size(); i++){
			int index = variableList.get(i).getIndex(compatibleState);
			newVector[index] = valueList.get(i);
		}
		
		platu.stategraph.state.State nextState = sg.addReachable(newState);
		if(nextState == null){
			nextState = newState;
			int result = sg.synchronizedConstrFindSG(nextState);
			if(result < 0) return newTransitions;
			
			newTransitions += result;
		}

//    	StateTran stTran = new StateTran(compatibleState, constraintTran, state);

		LPNTran constraintTran = c.getLpnTransition();
		constraintTran.synchronizedAddStateTran(compatibleState, nextState);
    	sg.lpnTransitionMap.get(compatibleState).add(constraintTran);
    	newTransitions++;
		
		return newTransitions;
	}
}
