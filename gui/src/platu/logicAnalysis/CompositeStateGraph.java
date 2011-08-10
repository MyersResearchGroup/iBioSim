package platu.logicAnalysis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import platu.lpn.LPNTran;
import platu.stategraph.StateGraph;
import platu.stategraph.state.State;

public class CompositeStateGraph {
	public Map<CompositeState, CompositeState> compositeStateSet = null;
//	public Set<CompositeStateTran> compositeStateTranSet = new HashSet<CompositeStateTran>();
	public StateGraph[] stateGraphArray = null;
	private CompositeState initState = null;
	private String label = "";
	public int numTransitions = 0;
	
	public CompositeStateGraph(CompositeState initialState, StateGraph[] sgArray){
		this.initState = initialState;
		this.stateGraphArray = sgArray;
		
		int size = 0;
		for(int i = 0; i < sgArray.length; i++){
			label += sgArray[i].getLabel();
			
			if(i < sgArray.length - 1){
				label += "||";
			}
			
			size *= sgArray[i].reachSize();
		}
		
		compositeStateSet = new HashMap<CompositeState, CompositeState>();
		this.compositeStateSet.put(this.initState, this.initState);
	}
	
	public CompositeStateGraph(StateGraph sg){
		State[] initStateArray = new State[1];
		initStateArray[0] = sg.getInitialState();
		CompositeState init = new CompositeState(initStateArray);
		
		StateGraph[] sgArray = new StateGraph[1];
		sgArray[0] = sg;
		
		// initialize attributes
		this.initState = init;
		this.stateGraphArray = sgArray;
		
		int size = 0;
		for(int i = 0; i < sgArray.length; i++){
			label += sgArray[i].getLabel();
			
			if(i < sgArray.length - 1){
				label += "||";
			}
			
			size *= sgArray[i].reachSize();
		}
		
		compositeStateSet = new HashMap<CompositeState, CompositeState>();
		this.compositeStateSet.put(this.initState, this.initState);
		
		
		CompositeState tempState = null;
		//for(State currentState : sg.reachable()) {
		for(int stateIdx = 0; stateIdx < sg.reachSize(); stateIdx++) {
			State currentState = sg.getState(stateIdx);
			State[] currentStateArray = new State[1];
			currentStateArray[0] = currentState;
			
			CompositeState currentCompositeState = new CompositeState(currentStateArray);
			tempState = this.addState(currentCompositeState);
			if(tempState != null){
				currentCompositeState = tempState;
			}

			List<LPNTran> enabledTrans = sg.lpnTransitionMap.get(currentState);
			int numTrans = enabledTrans.size();
			this.numTransitions += numTrans;
			
			for(LPNTran lpnTran : enabledTrans){
				State nextState = lpnTran.getNextState(currentState);
				State[] nextStateArray = new State[1];
				nextStateArray[0] = nextState;
				
				CompositeState nextCompositeState = new CompositeState(nextStateArray);
				tempState = this.addState(nextCompositeState);
				if(tempState != null){
					nextCompositeState = tempState;
				}
				
				currentCompositeState.enabledTranList.add(lpnTran);
				currentCompositeState.nextStateList.add(nextCompositeState);
				nextCompositeState.incomingStateList.add(currentCompositeState);
			}
		}
	}
	
	public Set<CompositeState> getStateSet(){
		return this.compositeStateSet.keySet();
	}
	
	public int getSize(){
		return this.stateGraphArray.length;
	}
	
	public String getLabel(){
		return this.label;
	}
	
	/**
     * Adds a composite state to the composite state graph
     * @param st - CompositeState to be added
     * @return Equivalent CompositeState object, otherwise null.
     */
	public CompositeState addState(CompositeState st){
		if(this.compositeStateSet.containsKey(st)){
			return this.compositeStateSet.get(st);
		}
		
		this.compositeStateSet.put(st, st);
		
		return null;
	}
	
//	public boolean addStateTran(CompositeStateTran stTran){
//		return this.compositeStateTranSet.add(stTran);
//	}
	
	public final CompositeState getInitState(){
		return this.initState;
	}
	
	public void setInitState(CompositeState init){
		this.initState = init;
	}
	
	public int numCompositeStates(){
		return this.compositeStateSet.size();
	}
	
//	public int numCompositeStateTrans(){
//		return this.compositeStateTranSet.size();
//	}
	
	public void draw(){
    	String dotFile = this.label + ".dot";
    	PrintStream graph = null;
    	
		try {
			graph = new PrintStream(new FileOutputStream(dotFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
    	
    	graph.println("digraph SG{");
    	
    	for(CompositeState currentState : this.compositeStateSet.keySet()){
    		String currentLabel = currentState.getLabel();
    		for(int i = 0; i < currentState.nextStateList.size(); i++){
    			CompositeState nextState = currentState.nextStateList.get(i);
    			LPNTran lpnTran = currentState.enabledTranList.get(i);
    			
    			graph.println("  \"" + currentLabel + "\" " + " -> " + "\"" + nextState.getLabel() + "\"" + " [label=\"" + lpnTran.getFullLabel() + "\"]");
    		}
    	}
    	
    	graph.println("}");
	    graph.close();
    }
}
