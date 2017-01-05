package backend.verification.platu.logicAnalysis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import backend.lpn.parser.Transition;
import backend.verification.platu.main.Main;
import backend.verification.platu.main.Options;
import backend.verification.platu.stategraph.State;
import backend.verification.platu.stategraph.StateGraph;

import java.util.Set;

public class CompositeStateGraph {
	public Map<Integer, CompositeState> indexStateMap = new HashMap<Integer, CompositeState>();
	public Map<CompositeState, CompositeState> stateMap = new HashMap<CompositeState, CompositeState>();
	public Map<CompositeStateTran, CompositeStateTran> stateTranMap = new HashMap<CompositeStateTran, CompositeStateTran>();
	private StateGraph[] stateGraphArray = null;
	private CompositeState initState = null;
	private String label = "";
	
	public StateGraph[] getStateGraphArray(){
		return this.stateGraphArray;
	}
	
	public void setReachableStates(Map<Integer, CompositeState> indexMap, Map<CompositeState, CompositeState> stateMap){
		this.indexStateMap = indexMap;
		this.stateMap = stateMap;
	}
	
	public static List<Transition> getEnabled(CompositeState currentState){
		//Set<Transition> lpnTranSet = new HashSet<Transition>(currentState.numOutgoingTrans());
		List<Transition> enabled = new ArrayList<Transition>(currentState.numOutgoingTrans());
		
		/*
		for(CompositeStateTran stTran : currentState.getOutgoingStateTranList()){
			// TODO: (future) Fix stTran.getLPNTran().
			Transition lpnTran = null; //stTran.getLPNTran();
			if(lpnTranSet.add(lpnTran))
				enabled.add(lpnTran);
		}
		*/
		
		return enabled;
	}
	
	public CompositeState getState(int index){
		return this.indexStateMap.get(index);
	}
	
	public CompositeStateTran addStateTran(CompositeState currentState, CompositeState nextState, backend.lpn.parser.Transition lpnTran){
		CompositeStateTran stateTran = new CompositeStateTran(currentState, nextState, lpnTran);
		
		CompositeStateTran tmpTran = this.stateTranMap.get(stateTran);
		if(tmpTran != null){
			return tmpTran;
		}
		
		this.stateTranMap.put(stateTran, stateTran);
		currentState.addOutgoingStateTran(stateTran);
		nextState.addIncomingStateTran(stateTran);

		return stateTran;
	}
	
	public CompositeStateTran addStateTran(int currentStateIndex, int nextStateIndex, Transition lpnTran){
		CompositeState currentState = this.indexStateMap.get(currentStateIndex);
		CompositeState nextState = this.indexStateMap.get(nextStateIndex);
		CompositeStateTran stateTran = new CompositeStateTran(currentState, nextState, lpnTran);
		
		CompositeStateTran tmpTran = this.stateTranMap.get(stateTran);
		if(tmpTran != null){
			return tmpTran;
		}
		
		this.stateTranMap.put(stateTran, stateTran);
		currentState.addOutgoingStateTran(stateTran);
		nextState.addIncomingStateTran(stateTran);

		return stateTran;
	}
	
	public CompositeStateTran addStateTran(CompositeStateTran stateTran){
		CompositeStateTran tmpTran = this.stateTranMap.get(stateTran);
		if(tmpTran != null){
			return tmpTran;
		}

		this.stateTranMap.put(stateTran, stateTran);
		CompositeState currentState = this.getState(stateTran.getCurrentState());
		CompositeState nextState = this.getState(stateTran.getNextState());
		
		currentState.addOutgoingStateTran(stateTran);
		nextState.addIncomingStateTran(stateTran);

		return stateTran;
	}
	
	public void removeStateTran(CompositeStateTran stateTran){
		if(this.stateTranMap.remove(stateTran) == null){
			return;
		}
		
		CompositeState currentState = this.getState(stateTran.getCurrentState());
		CompositeState nextState = this.getState(stateTran.getNextState());

		currentState.removeOutgoingStateTran(stateTran);
		nextState.removeIncomingStateTran(stateTran);
	}
	
	public boolean removeState(CompositeState st){
		CompositeState retState = this.stateMap.remove(st);
		if(retState == null){
			return false;
		}
		
		this.indexStateMap.remove(st.getIndex());
		
		return true;
	}
	
	public boolean removeState(int stateIndex){
		CompositeState retState = this.indexStateMap.remove(stateIndex);
		if(retState == null){
			return false;
		}
		
		this.stateMap.remove(retState);
		
		return true;
	}
	
	public CompositeStateGraph(CompositeState initialState, StateGraph[] sgArray){
		this.initState = initialState;
		this.stateGraphArray = sgArray;
		
		//int size = 0;
		for(int i = 0; i < sgArray.length; i++){
			label += sgArray[i].getLpn().getLabel();
			
			if(i < sgArray.length - 1){
				label += "||";
			}
			
			//size *= sgArray[i].reachSize();
		}
		
		this.addState(this.initState);
	}
	
	public CompositeStateGraph(StateGraph sg){
		int[] initStateArray = new int[1];
		initStateArray[0] = sg.getInitialState().getIndex();
		CompositeState init = new CompositeState(initStateArray);
		
		StateGraph[] sgArray = new StateGraph[1];
		sgArray[0] = sg;
		
		// initialize attributes
		this.initState = init;
		this.stateGraphArray = sgArray;
		
		// construct label
		//int size = 0;
		for(int i = 0; i < sgArray.length; i++){
			label += sgArray[i].getLpn().getLabel();
			
			if(i < sgArray.length - 1){
				label += "||";
			}
			
			//size *= sgArray[i].reachSize();
		}
		
		this.addState(this.initState);
		
		CompositeState tempState = null;
		for(int i = 0; i < sg.reachSize(); i++){
			State currentState = sg.getState(i);
//		for(State currentState : sg.getStateSet()){
			int[] currentStateArray = new int[1];
			currentStateArray[0] = currentState.getIndex();

			CompositeState currentCompositeState = new CompositeState(currentStateArray);
			tempState = this.addState(currentCompositeState);
			if(tempState != currentCompositeState){
				currentCompositeState = tempState;
			}
			
			Set<Entry<Transition, State>> stateSet = sg.getOutgoingTrans(currentState);
			for(Entry<Transition, State> stateTran : stateSet){
				State nextState = stateTran.getValue();
				Transition lpnTran = stateTran.getKey();
				int[] nextStateArray = new int[1];
				nextStateArray[0] = nextState.getIndex();
				
				CompositeState nextCompositeState = new CompositeState(nextStateArray);
				tempState = this.addState(nextCompositeState);
				if(tempState != nextCompositeState){
					nextCompositeState = tempState;
				}
				
				CompositeStateTran newStateTran = new CompositeStateTran(currentCompositeState, nextCompositeState, lpnTran);
				this.addStateTran(newStateTran);
				if(!lpnTran.isLocal()){
					newStateTran.setVisibility();
				}
			}
		}
	}
	
	public Set<CompositeState> getStateSet(){
		return this.stateMap.keySet();
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
     * @return Equivalent CompositeState object it exists, otherwise CompositeState st.
     */
	public CompositeState addState(CompositeState st){
		CompositeState retState = this.stateMap.get(st);
		if(retState == null){
			int index = this.indexStateMap.size();
			st.setIndex(index);
			this.indexStateMap.put(index, st);
			this.stateMap.put(st, st);
			
			return st;
		}
		
		return retState;
	}
	
	public final CompositeState getInitState(){
		return this.initState;
	}
	
	public void setInitState(CompositeState init){
		this.initState = init;
	}
	
	public int numStates(){
		return this.stateMap.size();
	}
	
	public int numStateTrans(){
		return this.stateTranMap.size();
	}
	
	public Set<CompositeStateTran> getStateTranSet(){
		return this.stateTranMap.keySet();
	}
	
	public boolean containsState(int stateIndex){
		return this.indexStateMap.containsKey(stateIndex);
	}
	
	public boolean containsStateTran(CompositeStateTran stateTran){
		return this.stateTranMap.containsKey(stateTran);
	}
	
	public void draw(){
		String dotFile = Options.getDotPath();
		if(!dotFile.endsWith("/") && !dotFile.endsWith("\\")){
			String dirSlash = "/";
			if(Main.isWindows) dirSlash = "\\";
			
			dotFile = dotFile += dirSlash;
		}
		
		dotFile += this.label + ".dot";
    	PrintStream graph = null;
    	
		try {
			graph = new PrintStream(new FileOutputStream(dotFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}
    	
    	graph.println("digraph SG{");
    	
    	for(CompositeState currentState : this.stateMap.keySet()){
    		for(CompositeStateTran stateTran : currentState.getOutgoingStateTranList()){
    			CompositeState nextState = this.indexStateMap.get(stateTran.getNextState());
    			Transition lpnTran = stateTran.getLPNTran();
//    			System.out.println("  " + nextState.getIndex());
    			graph.println("  \"" + currentState.getIndex() + "\" " + " -> " + "\"" + nextState.getIndex() + "\"" + " [label=\"" + lpnTran.getFullLabel() + "\"]");
    		}
    	}
    	
    	graph.println("}");
	    graph.close();
    }
}
