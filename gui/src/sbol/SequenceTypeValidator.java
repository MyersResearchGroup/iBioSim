package sbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SequenceTypeValidator {
	
	private NFAState nfaStart;
	private DFAState dfaStart;
	
	public SequenceTypeValidator (String regex, Set<String> terminals) {
		int stateIndex = 1;
		nfaStart = new NFAState("S" + stateIndex);
		stateIndex++;
		NFAState currentState = nfaStart;
		for (int i = 0; i < regex.length(); i++) {
			NFAState nextState = new NFAState("S" + stateIndex);
			stateIndex++;
			currentState.addTransition(regex.substring(0,1), nextState);
		}
	}
	
	public void constructNFA() { 
		
	}
	
	private class NFAState {
		
		private HashMap<String, Set<NFAState>> transitions;
		private String stateID;
		
		private NFAState (String stateID) {
			this.stateID = stateID;
			transitions = new HashMap<String, Set<NFAState>>();
		}
		
		private void addTransition(String trigger, NFAState destination) {
			if (transitions.containsKey(trigger))
				transitions.get(trigger).add(destination);
			else {
				transitions.put(trigger, new HashSet<NFAState>());
				transitions.get(trigger).add(destination);
			}
		}
		
		private Set<NFAState> transition(String trigger) {
			return transitions.get(trigger);
		}
		
	}
	
	private class DFAState {
		
		private HashMap<String, DFAState> transitions;
		
		private DFAState() {
			transitions = new HashMap<String, DFAState>();
		}
		
		private void addTransition(String trigger, DFAState destination) {
			transitions.put(trigger, destination);
		}
		
		private DFAState transition(String trigger) {
			return transitions.get(trigger);
		}
	}
	
//	private class Transition {
//		
//		private char trigger;
//		private State destination;
//		
//		private Transition(char trigger, State destination) {
//			this.trigger = trigger;
//			this.destination = destination;
//		}
//		
//		private State getDestination() {
//			return destination;
//		}
//	}
}
