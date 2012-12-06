package sbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SequenceTypeValidator {
	private DFA dfa;
	private int stateIndex;
	
	public SequenceTypeValidator(String regex) {
		Set<NFAState> nfaStartStates = constructNFA(regex);
		this.dfa = new DFA(nfaStartStates);
//		dfa.setStartState(constructDFA(nfaStartStates));
//		dfa.print();
	}
	
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
	
	private Set<NFAState> constructNFAHelper(Set<NFAState> startStates, String regex, String quantifier, Set<String> localIDs) { 
		Set<NFAState> acceptStates = new HashSet<NFAState>();
		if (!regex.contains("(") && !regex.contains(")") && !regex.contains("+") && !regex.contains("*")
				&& !regex.contains("|") && !regex.contains("?") && !regex.contains(",")) {
			NFAState nextState = new NFAState("S" + stateIndex);
			localIDs.add("S" + stateIndex);
			stateIndex++;
			for (NFAState startState : startStates)
				startState.addTransition(regex.trim(), nextState);
			acceptStates.add(nextState);
		} else {
			int i = 0;
			acceptStates.addAll(startStates);
			do {
				int j;
				String subRegex;
				String subQuantifier = "";
				boolean or = false;
				while (regex.substring(i, i + 1).equals("|") || regex.substring(i, i + 1).equals(",")) {
					if (regex.substring(i, i + 1).equals("|"))
						or = true;
					i++;
				}
				if (regex.substring(i, i + 1).equals("(")) {
					j = findClosingParen(regex, i);
					subRegex = regex.substring(i + 1, j);
				} else {
					j = findClosingLetter(regex, i);
					subRegex = regex.substring(i, j + 1);
				}
				if (j + 1 < regex.length())
					subQuantifier = regex.substring(j + 1, j + 2);
				Set<String> subIDs = new HashSet<String>();
				if (or)
					acceptStates.addAll(constructNFAHelper(startStates, subRegex, subQuantifier, subIDs));
				else
					acceptStates = constructNFAHelper(acceptStates, subRegex, subQuantifier, subIDs);
				localIDs.addAll(subIDs);
				if (subQuantifier.equals("+") || subQuantifier.equals("*") || subQuantifier.equals("?")) {
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
		if (quantifier.equals("*") || quantifier.equals("?"))
			acceptStates.addAll(startStates);
		return acceptStates;
	}
	
	private int findClosingParen(String regex, int j) {
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
	
	private int findClosingLetter(String regex, int j) {
		while (true) {
			if (j + 1 < regex.length()) {
				String token = regex.substring(j + 1, j + 2);
				if (token.equals("(") || token.equals(")") || token.equals("+") || token.equals("*")
						|| token.equals("|") || token.equals("?") || token.equals(","))
					return j;
				else
					j++;
			} else 
				return j;
		}
	}
	
	public boolean validate(List<String> types) {
		return dfa.run(types);
	}
	
	public boolean isValid() {
		return dfa.isAccepting();
	}
	
	public void reset() {
		dfa.reset();
	}
	
	public Set<String> getStartTypes() {
		Set<String> startTypes = new HashSet<String>();
		startTypes.addAll(dfa.getStartState().getTransitions().keySet());
		return startTypes;
	}
	
	public Set<String> getTerminalTypes() {
		Set<String> terminalTypes = new HashSet<String>();
		for (DFAState state : dfa.getStates().values()) {
			HashMap<String, DFAState> transitions = state.getTransitions();
			for (String type : transitions.keySet())
				if (transitions.get(type).isAccepting())
					terminalTypes.add(type);
		}
		return terminalTypes;
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
		
		public void setID(String id) {
			this.id = id;
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
		private HashMap<String, DFAState> states = new HashMap<String, DFAState>();
		private DFAState startState;
		private DFAState currentState;
		
		public DFA(Set<NFAState> nfaStates) {
			startState = constructDFA(nfaStates);
			currentState = startState;
		}
		
		public DFAState constructDFA(Set<NFAState> nfaStates) {
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
			addState(dfaState);
			for (String input : combinedTransitions.keySet()) {
				Set<NFAState> nfaDestinations = combinedTransitions.get(input);
				dfaStateID = "";
				for (NFAState nfaDestination : nfaDestinations)
					dfaStateID = dfaStateID + nfaDestination.getID();
				DFAState dfaDestination;
				if (hasState(dfaStateID))
					dfaDestination = getState(dfaStateID);
				else
					dfaDestination = constructDFA(combinedTransitions.get(input));
				dfaState.addTransition(input, dfaDestination);
			}
			dfaState.setAccepting(accepting);
			return dfaState;
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
		
		// Prints out DFA in dot format
		public void print() {
			int reindex = 0;
			for (DFAState state : states.values()) {
				state.setID("S" + reindex);
				reindex++;
			}
			printHelper(startState, new HashSet<String>());
			System.out.println();
		}
		
		private void printHelper(DFAState currentState, Set<String> visitedIds) {
			visitedIds.add(currentState.getID());
			if (currentState.isAccepting())
				System.out.println(currentState.getID() + " [peripheries=2]");
			for (String input : currentState.getTransitions().keySet()) {
				DFAState destination = currentState.transition(input);
				System.out.println(currentState.getID() + " -> " + destination.getID() + " [label=\" " 
						+ input + "\"]");
				if (!visitedIds.contains(destination.getID()))
					printHelper(destination, visitedIds);
			}
			
		}
		
		public boolean run(List<String> inputs) {
			DFAState nextState = currentState;
			for (int i = 0; i < inputs.size(); i++) {
				nextState = nextState.transition(inputs.get(i));
				if (nextState == null)
					return false;
				else if (i == inputs.size() - 1)
					currentState = nextState;
			}
			return currentState.isAccepting();
		}
		
		public boolean isAccepting() {
			return currentState.isAccepting();
		}
		
		public void reset() {
			currentState = startState;
		}
	}
	
}
