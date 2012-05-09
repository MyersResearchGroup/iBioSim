package sbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class SequenceTypeValidator {
	private DFA dfa;
	private int stateIndex;
	
	public SequenceTypeValidator(String regex) {
//		Set<NFAState> nfaStartStates = constructNFA(regex);
//		this.dfa = new DFA();
//		dfa.setStartState(constructDFA(nfaStartStates));
//		dfa.print();
		
		Set<NFAState> nfaStartStates = constructNFA(regex);
		this.dfa = new DFA();
		dfa.setStartState(constructDFA(nfaStartStates));
//		dfa.print();
	}
	
//	private Set<NFAState> constructNFA(String regex) {
//		stateIndex = 0;
//		Set<NFAState> nfaStartStates = new HashSet<NFAState>();
//		nfaStartStates.add(new NFAState("S" + stateIndex));
//		stateIndex++;
//		Set<NFAState> acceptStates = constructNFAHelper(nfaStartStates, regex, "", new HashSet<String>());
//		for (NFAState nfaState : acceptStates)
//			nfaState.setAccepting(true);
//		return nfaStartStates;
//	}
	
	private Set<NFAState> constructNFA(String regex) {
		stateIndex = 0;
		Set<NFAState> nfaStartStates = new HashSet<NFAState>();
		nfaStartStates.add(new NFAState("S" + stateIndex));
		stateIndex++;
		Set<NFAState> acceptStates = constructNFAHelper(nfaStartStates, regex, "", new HashSet<String>());
		for (NFAState nfaState : acceptStates)
			nfaState.setAccepting(true);
		return nfaStartStates;
	}
	
//	private Set<NFAState> constructNFAHelper(Set<NFAState> startStates, String regex, String quantifier, Set<String> localIds) { 
//		int i = 0;
//		Set<NFAState> currentStates = new HashSet<NFAState>();
//		currentStates.addAll(startStates);
//		do {
//			String input = regex.substring(i, i + 1);
//			if (input.equals("(")) {
//				int j = findClosing(regex, i);
//				String subRegex = regex.substring(i + 1, j);
//				String subQuantifier = "";
//				if (j + 1 < regex.length()) {
//					subQuantifier = regex.substring(j + 1, j + 2);
//					i = j + 2;
//				} else
//					i = j + 1;
//				currentStates = constructNFAHelper(currentStates, subRegex, subQuantifier, localIds);
//			}  else {
//				String subQuantifier = "";
//				if (i + 1 < regex.length())
//					subQuantifier = regex.substring(i + 1, i + 2);
//				NFAState nextState = new NFAState("S" + stateIndex);
//				localIds.add("S" + stateIndex);
//				stateIndex++;
//				for (NFAState currentState : currentStates)
//					currentState.addTransition(input, nextState);
//				if (subQuantifier.equals("+") || subQuantifier.equals("*")) {
//					nextState.addTransition(input, nextState);
//					i = i + 2;
//				} else
//					i++;
//				if (!subQuantifier.equals("*"))
//					currentStates.clear();
//				currentStates.add(nextState);
//			}
//		} while (i < regex.length());
//		if (quantifier.equals("+") || quantifier.equals("*"))
//			for (NFAState startState : startStates) 
//				for (String input : startState.getTransitions().keySet()) 
//					for (NFAState destination : startState.transition(input)) 
//						if (localIds.contains(destination.getID()))
//							for (NFAState currentState : currentStates)
//								currentState.addTransition(input, destination);
//		if (quantifier.equals("*"))
//			currentStates.addAll(startStates);
//		return currentStates;
//	}
	
	private Set<NFAState> constructNFAHelper(Set<NFAState> startStates, String regex, String quantifier, Set<String> localIDs) { 
		Set<NFAState> acceptStates = new HashSet<NFAState>();
		if (regex.length() == 1) {
			NFAState nextState = new NFAState("S" + stateIndex);
			localIDs.add("S" + stateIndex);
			stateIndex++;
			for (NFAState startState : startStates)
				startState.addTransition(regex, nextState);
			acceptStates.add(nextState);
		} else {
			int i = 0;
			acceptStates.addAll(startStates);
			do {
				int j;
				String subRegex;
				String subQuantifier;
				if (regex.substring(i, i + 1).equals("(")) {
					j = findClosing(regex, i);
					subRegex = regex.substring(i + 1, j);
					subQuantifier = regex.substring(j + 1, j + 2);
				} else {
					j = i;
					subRegex = regex.substring(i, i + 1);
					if (i + 1 < regex.length())
						subQuantifier = regex.substring(i + 1, i + 2);
					else
						subQuantifier = "";
				}
				Set<String> subIDs = new HashSet<String>();
				acceptStates = constructNFAHelper(acceptStates, subRegex, subQuantifier, subIDs);
				localIDs.addAll(subIDs);
				if (subQuantifier.equals("+") || subQuantifier.equals("*")) {
					i = j + 2;
				} else
					i = j + 1;
			} while (i < regex.length());
		}
		if (quantifier.equals("+") || quantifier.equals("*"))
			for (NFAState startState : startStates) 
				for (String label : startState.getTransitions().keySet()) 
					for (NFAState destination : startState.transition(label)) 
						if (localIDs.contains(destination.getID()))
							for (NFAState acceptState : acceptStates)
								acceptState.addTransition(label, destination);
		if (quantifier.equals("*"))
			acceptStates.addAll(startStates);
		return acceptStates;
	}
	
	private int findClosing(String regex, int j) {
		int openParen = 1;
		int closeParen = 0;
		do {
			j++;
			String token = regex.substring(j, j + 1);
			if (token.equals("("))
				openParen++;
			else if (token.equals(")"))
				closeParen++;
		} while (openParen != closeParen && j < regex.length());
		return j;
	}
	
	private DFAState constructDFA(Set<NFAState> nfaStates) {
		String dfaStateID = "";
		HashMap<String, Set<NFAState>> combinedTransitions = new HashMap<String, Set<NFAState>>();
		boolean accepting = false;
		for (NFAState nfaState : nfaStates) {
			dfaStateID = dfaStateID + nfaState.getID();
			HashMap<String, Set<NFAState>> transitions = nfaState.getTransitions();
			for (String input : transitions.keySet())
				if (combinedTransitions.containsKey(input))
					combinedTransitions.get(input).addAll(transitions.get(input));
				else
					combinedTransitions.put(input, transitions.get(input));
			if (nfaState.isAccepting())
				accepting = true;
		}
		DFAState dfaState = new DFAState(dfaStateID);
		dfa.addState(dfaState);
		for (String input : combinedTransitions.keySet()) {
			Set<NFAState> nfaDestinations = combinedTransitions.get(input);
			dfaStateID = "";
			for (NFAState nfaDestination : nfaDestinations)
				dfaStateID = dfaStateID + nfaDestination.getID();
			DFAState dfaDestination;
			if (dfa.hasState(dfaStateID))
				dfaDestination = dfa.getState(dfaStateID);
			else
				dfaDestination = constructDFA(combinedTransitions.get(input));
			dfaState.addTransition(input, dfaDestination);
		}
		dfaState.setAccepting(accepting);
		return dfaState;
	}
	
	public boolean validateSequenceTypes(LinkedList<String> types) {
		LinkedList<String> terminals = new LinkedList<String>();
		for (String type : types)
			terminals.add(SBOLUtility.soTypeToGrammarTerminal(type));
		return dfa.run(terminals);
	}
	
	private class NFAState {
		
		private HashMap<String, Set<NFAState>> transitions;
		private String id;
		private boolean accepting;
		
		public NFAState (String id) {
			this.id = id;
			transitions = new HashMap<String, Set<NFAState>>();
		}
		
		public String getID() {
			return id;
		}
		
		public String toString() {
			return id;
		}
		
		public void setAccepting(boolean accepting) {
			this.accepting = accepting;
		}
		
		public boolean isAccepting() {
			return accepting;
		}
		
		public void addTransition(String input, NFAState destination) {
			if (transitions.containsKey(input))
				transitions.get(input).add(destination);
			else {
				transitions.put(input, new HashSet<NFAState>());
				transitions.get(input).add(destination);
			}
		}
		
		public Set<NFAState> transition(String input) {
			return transitions.get(input);
		}
		
		public HashMap<String, Set<NFAState>> getTransitions() {
			return transitions;
		}
		
	}
	
	private class DFAState {
		
		private HashMap<String, DFAState> transitions;
		private String id;
		private boolean accepting;
		
		public DFAState(String id) {
			this.id = id;
			transitions = new HashMap<String, DFAState>();
		}
		
		public String getID() {
			return id;
		}
		
		public String toString() {
			return id;
		}
		
		public void setAccepting(boolean accepting) {
			this.accepting = accepting;
		}
		
		public boolean isAccepting() {
			return accepting;
		}
		
		public void addTransition(String input, DFAState destination) {
			transitions.put(input, destination);
		}
		
		public DFAState transition(String input) {
			return transitions.get(input);
		}
		
		public HashMap<String, DFAState> getTransitions() {
			return transitions;
		}
	}
	
	private class DFA {
		private HashMap<String, DFAState> states;
		private DFAState startState;
		private Set<DFAState> acceptStates;
		
		public DFA() {
			this.states = new HashMap<String, DFAState>();
			this.acceptStates = new HashSet<DFAState>();
		}
		
		public void setStartState(DFAState startState) {
			this.startState = startState;
		}
		
		public DFAState getStartState() {
			return startState;
		}
		
		public DFAState getState(String id) {
			return states.get(id);
		}
		
		public void addState(DFAState state) {
			states.put(state.getID(), state);
		}
		
		public boolean hasState(String id) {
			return states.keySet().contains(id);
		}
		
		public HashMap<String, DFAState> getStates() {
			return states;
		}
		
		public void print() {
			printHelper(startState, new HashSet<String>());
			System.out.println();
		}
		
		private void printHelper(DFAState currentState, Set<String> visitedIds) {
			visitedIds.add(currentState.getID());
			if (currentState.isAccepting())
				System.out.println(currentState.getID() + " is accepting.");
			for (String input : currentState.getTransitions().keySet()) {
				DFAState destination = currentState.transition(input);
				System.out.println(currentState.getID() + "-" + input + "->" + destination.getID());
				if (!visitedIds.contains(destination.getID()))
					printHelper(destination, visitedIds);
			}
			
		}
		
		public boolean run(LinkedList<String> inputs) {
			DFAState currentState = startState;
			for (String input : inputs) {
				if (currentState != null)
					currentState = currentState.transition(input);
				else
					return false;
			}
			if (currentState.isAccepting())
				return true;
			else
				return false;
		}
	}
	
}
