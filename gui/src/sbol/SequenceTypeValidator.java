package sbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SequenceTypeValidator {
	private DFA constructDFA;
	private DFA fragmentDFA;
	private int stateIndex;
	
	public SequenceTypeValidator(String regex) {
		String altRegex = altFragmentRegex(regex);
		String exAltRegex = exFragmentRegex(altRegex);
//		System.out.println(altRegex);
//		System.out.println(exAltRegex);
		Set<NFAState> nfaStartStates = constructNFA(regex);
		this.constructDFA = new DFA(nfaStartStates);
		nfaStartStates = constructNFA(exAltRegex);
		this.fragmentDFA = new DFA(nfaStartStates);
//		fragmentDFA.print();
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
		acceptStates.addAll(startStates);
		int i = 0;
		do {
			int j;
			String subRegex;
			String subQuantifier = "";
			if (regex.substring(i, i + 1).equals(","))
				i++;
			if (regex.substring(i, i + 1).equals("|")) {
				j = locateClosingOr(regex, i);
				subRegex = regex.substring(i + 1, j);
			} else if (regex.substring(i, i + 1).equals("(")) {
				j = locateClosingParen(regex, i);
				subRegex = regex.substring(i + 1, j);
			} else {
				j = locateClosingLetter(regex, i);
				subRegex = regex.substring(i, j + 1);
			}
			if (j + 1 < regex.length())
				subQuantifier = regex.substring(j + 1, j + 2);
			Set<String> subIDs = new HashSet<String>();
			if (regex.substring(i, i + 1).equals("|"))
				acceptStates.addAll(constructNFAHelper(startStates, subRegex, subQuantifier, subIDs));
			else if (regex.substring(i, i + 1).equals("("))
				acceptStates = constructNFAHelper(acceptStates, subRegex, subQuantifier, subIDs);
			else 
				acceptStates = addNFAState(acceptStates, subRegex, subQuantifier, subIDs);
			localIDs.addAll(subIDs);
			if (regex.substring(i, i + 1).equals("|"))
				i = j;
			else if (subQuantifier.equals("+") || subQuantifier.equals("*") || subQuantifier.equals("?")) 
				i = j + 2;
			else
				i = j + 1;
		} while (i < regex.length());
		quantifyNFAStates(startStates, acceptStates, quantifier, localIDs);
		return acceptStates;
	}
	
	private Set<NFAState> addNFAState(Set<NFAState> startStates, String regex, String quantifier, Set<String> localIDs) { 
		Set<NFAState> acceptStates = new HashSet<NFAState>();
		NFAState nextState = new NFAState("S" + stateIndex);
		localIDs.add("S" + stateIndex);
		stateIndex++;
		for (NFAState startState : startStates)
			startState.addTransition(regex.trim(), nextState);
		acceptStates.add(nextState);
		quantifyNFAStates(startStates, acceptStates, quantifier, localIDs);
		return acceptStates;
	}
	
	private void quantifyNFAStates(Set<NFAState> startStates, Set<NFAState> acceptStates, String quantifier, Set<String> localIDs) {
		if (quantifier.equals("+") || quantifier.equals("*"))
			for (NFAState startState : startStates) 
				for (String label : startState.getTransitions().keySet()) 
					for (NFAState destination : startState.transition(label)) 
						if (localIDs.contains(destination.getID()))
							for (NFAState acceptState : acceptStates)
								acceptState.addTransition(label, destination);
		if (quantifier.equals("*") || quantifier.equals("?"))
			acceptStates.addAll(startStates);
	}
	
	private String altFragmentRegex(String regex) {
		List<String> orFragments = findOrFragments(regex);
		if (orFragments.size() > 1) {
			String result = altFragmentRegex(orFragments.get(0));
			for (int i = 1; i < orFragments.size(); i++)
				result = result + "|" + altFragmentRegex(orFragments.get(i));
			return result;
		} else if (regex.startsWith("(")) {
			int i = locateClosingParen(regex, 0);
			String xFragment = regex.substring(1, i);
			if (i + 1 < regex.length()) {
				String quantifier = regex.substring(i + 1, i + 2);
				if (quantifier.equals("*") || quantifier.equals("+"))
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return "(" + altFragmentRegex(xFragment) + ")" + "(" + xFragment + ")*" + yFragment + "|"
								+ altFragmentRegex(yFragment);
					} else
						return "(" + altFragmentRegex(xFragment) + ")" + "(" + xFragment + ")*";
				else if (quantifier.equals("?") || quantifier.equals(","))
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return altFragmentRegex("(" + xFragment + ")" + yFragment);
					} else
						return altFragmentRegex(xFragment);
				else {
					String yFragment = getSubRegex(regex, i + 1);
					orFragments = findOrFragments(xFragment);
					if (orFragments.size() > 1) {
						String result = altFragmentRegex(orFragments.get(0));
						for (int j = 1; j < orFragments.size(); j++)
							result = result + "|" + altFragmentRegex(orFragments.get(j));
						return ("(" + result + ")" + yFragment + "|" + altFragmentRegex(yFragment));
					} else
						return altFragmentRegex(xFragment + "," + yFragment);
				}
			} else 
				return altFragmentRegex(xFragment);
		} else {
			int i = locateClosingLetter(regex, 0);
			String terminal = regex.substring(0, i + 1);
			if (i + 1 < regex.length()) {
				String quantifier = regex.substring(i + 1, i + 2);
				if (quantifier.equals("*") || quantifier.equals("+"))
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "," + terminal + "*" + xFragment + "|" + altFragmentRegex(xFragment);
					} else
						return terminal + "," + terminal + "*";
				else if (quantifier.equals("?") || quantifier.equals(","))
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "," + xFragment + "|" + altFragmentRegex(xFragment);
					} else
						return terminal;
				else {
					String xFragment = getSubRegex(regex, i + 1);
					return terminal + "," + xFragment + "|" + altFragmentRegex(xFragment);
				}
			} else
				return terminal;
		}
	}
	
	private String exFragmentRegex(String regex) {
		List<String> orFragments = findOrFragments(regex);
		if (orFragments.size() > 1) {
			regex = exFragmentRegex(orFragments.get(0));
			for (int i = 1; i < orFragments.size(); i++)
				regex = regex + "|" + exFragmentRegex(orFragments.get(i));
			return regex;
		} else if (regex.startsWith("(")) {
			int i = locateClosingParen(regex, 0);
			String xFragment = regex.substring(1, i);
			if (i + 1 < regex.length()) {
				String quantifier = regex.substring(i + 1, i + 2);
				if (quantifier.equals("*"))
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return "(" + xFragment + ")*(" + exFragmentRegex(xFragment) + "|" + exFragmentRegex(yFragment) + ")";
					} else
						return "(" + xFragment + ")*(" + exFragmentRegex(xFragment) + ")?";
				else if (quantifier.equals("+")) 
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return "(" + xFragment + ")*" + exFragmentRegex(xFragment + "," + yFragment);
					} else
						return "(" + xFragment + ")*" + exFragmentRegex(xFragment);
				else if (quantifier.equals("?"))
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return "(" + exFragmentRegex(xFragment) + ")?" + exFragmentRegex(yFragment);
					} else
						return "(" + exFragmentRegex(xFragment) + ")?";
				else if (quantifier.equals(","))
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return exFragmentRegex("(" + xFragment + ")" + yFragment);
					} else
						return exFragmentRegex(xFragment);
				else {
					String yFragment = getSubRegex(regex, i + 1);
					orFragments = findOrFragments(xFragment);
					if (orFragments.size() > 1) {
						String result = exFragmentRegex(orFragments.get(0) + "," + yFragment);
						for (int j = 1; j < orFragments.size(); j++)
							result = result + "|" + exFragmentRegex(orFragments.get(j) + "," + yFragment);
						return result;
					} else
						return exFragmentRegex(xFragment + "," + yFragment);
				}
			} else 
				return exFragmentRegex(xFragment);
		} else {
			int i = locateClosingLetter(regex, 0);
			String terminal = regex.substring(0, i + 1);
			if (i + 1 < regex.length()) {
				String quantifier = regex.substring(i + 1, i + 2);
				if (quantifier.equals("*"))
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "*(" + terminal + "|" + exFragmentRegex(xFragment) + ")";
					} else
						return terminal + "*";
				else if (quantifier.equals("+"))
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "*" + exFragmentRegex(terminal + "," + xFragment);
					} else
						return terminal + "*" + terminal;
				else if (quantifier.equals("?"))
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "?" + exFragmentRegex(xFragment);
					} else
						return terminal + "?";
				else if (quantifier.equals(","))
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "(" + exFragmentRegex(xFragment) + ")?";
					} else
						return terminal;
				else {
					String xFragment = getSubRegex(regex, i + 1);
					return terminal + "(" + exFragmentRegex(xFragment) + ")?";
				}
			} else
				return terminal;
		}	
	}
	
	private int locateClosingParen(String regex, int i) {
		int openParen = 0;
		int closeParen = 0;
		for (int j = i; j < regex.length(); j++) {
			if (regex.substring(j, j + 1).equals("("))
				openParen++;
			else if (regex.substring(j, j + 1).equals(")"))
				closeParen++;
			if (openParen == closeParen)
				return j;
		}
		return regex.length();
	}
	
	private int locateClosingLetter(String regex, int i) {
		for (int j = i; j < regex.length(); j++) {
			String token = regex.substring(j, j + 1);
			if ((token.equals("(") || token.equals(")") || token.equals("+") || token.equals("*")
					|| token.equals("|") || token.equals("?") || token.equals(",")) && j > i)
				return j - 1;
		}
		return regex.length() - 1;
	}
	
	private List<String> findOrFragments(String regex) {
		List<String> orFragments = new LinkedList<String>();
		int j = locateClosingOr(regex, 0);
		orFragments.add(regex.substring(0, j));
		while (j < regex.length()) {
			int i = j;
			j = locateClosingOr(regex, i);
			orFragments.add(regex.substring(i + 1, j));
		}
		return orFragments;
	}
	
	private int locateClosingOr(String regex, int i) {
		int openParen = 0;
		int closedParen = 0;
		for (int j = i; j < regex.length(); j++)
			if (regex.substring(j, j + 1).equals("("))
				openParen++;
			else if (regex.substring(j, j + 1).equals(")"))
				closedParen++;
			else if (regex.substring(j, j + 1).equals("|") && j > i && openParen == closedParen)
				return j;
		return regex.length();
	}
	
	private String getSubRegex(String regex, int index) {
		String subRegex = regex.substring(index);
		if (subRegex.startsWith(","))
			return subRegex.substring(1);
		else
			return subRegex;
	}
	
	public boolean validateConstruct(List<String> types) {
		return constructDFA.run(types);
	}
	
	public boolean validateFragment(List<String> types) {
		return fragmentDFA.run(types);
	}
	
	public boolean isConstructValid() {
		return constructDFA.isAccepting();
	}
	
	public boolean isFragmentValid() {
		return fragmentDFA.isAccepting();
	}
	
	public void resetFragmentValidator() {
		fragmentDFA.reset();
	}
	
	public Set<String> getStartTypes() {
		Set<String> startTypes = new HashSet<String>();
		startTypes.addAll(constructDFA.getStartState().getTransitions().keySet());
		return startTypes;
	}
	
	public Set<String> getTerminalTypes() {
		Set<String> terminalTypes = new HashSet<String>();
		for (DFAState state : constructDFA.getStates().values()) {
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
