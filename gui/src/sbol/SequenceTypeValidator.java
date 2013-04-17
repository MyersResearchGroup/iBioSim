package sbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import biomodel.util.GlobalConstants;

public class SequenceTypeValidator {
	private DFA completeConstructDFA;
	private DFA terminalConstructDFA;
	private DFA startConstructDFA;
	private DFA partialConstructDFA;
	private int stateIndex;
	
	public SequenceTypeValidator(String regex) {
		String rightAltRegex = altFragmentRegex(regex);
		String revRegex = reverseRegex(regex);
		String leftAltRegex = altFragmentRegex(revRegex);
//		String exAltRegex = exFragmentRegex(rightAltRegex);
//		System.out.println(altRegex);
//		System.out.println(exAltRegex);
		Set<NFAState> nfaStartStates = constructNFA(regex);
		completeConstructDFA = new DFA(nfaStartStates);
		nfaStartStates = constructNFA(rightAltRegex);
		terminalConstructDFA = new DFA(nfaStartStates);
		nfaStartStates = constructNFA(leftAltRegex);
		startConstructDFA = new DFA(nfaStartStates);
//		nfaStartStates = constructNFA(exAltRegex);
//		partialConstructDFA = new DFA(nfaStartStates);
		partialConstructDFA = terminalConstructDFA.clone("?");
//		terminalConstructDFA.print();
//		partialConstructDFA.print();
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
			String result = exFragmentRegex(orFragments.get(0));
			for (int i = 1; i < orFragments.size(); i++)
				result = result + "|" + exFragmentRegex(orFragments.get(i));
			return result;
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
	
	private String reverseRegex(String regex) {
		List<String> orFragments = findOrFragments(regex);
		if (orFragments.size() > 1) {
			String result = reverseRegex(orFragments.get(0));
			for (int i = 1; i < orFragments.size(); i++)
				result = result + "|" + reverseRegex(orFragments.get(i));
			return result;
		} else {
			String result = "";
			int i = 0;
			while (i < regex.length()) {
				String token = regex.substring(i, i + 1);
				if (token.equals("(")) {
					int j = locateClosingParen(regex, i);
					if (j + 1 < regex.length()) {
						String quantifier = regex.substring(j + 1, j + 2);
						if (isQuantifier(quantifier))
							result = "(" + reverseRegex(regex.substring(i + 1, j)) + ")" + quantifier + result;
						else
							result = "(" + reverseRegex(regex.substring(i + 1, j)) + ")" + result;
					} else
						result = "(" + reverseRegex(regex.substring(i + 1, j)) + ")" + result;
					i = j + 1;
				} else if (isLetter(token)) {
					int j = locateClosingLetter(regex, i);
					if (j + 1 < regex.length()) {
						String quantifier = regex.substring(j + 1, j + 2);
						if (isQuantifier(quantifier))
							result = regex.substring(i, j + 1) + quantifier + result;
						else if (i > 0)
							result = regex.substring(i, j + 1) + "," + result;
						else 
							result = regex.substring(i, j + 1) + result;
					} else if (i > 0)
						result = regex.substring(i, j + 1) + "," + result;
					else
						result = regex.substring(i, j + 1) + result;
					i = j + 1;
				} else
					i++;
			}
			return result;
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
			if (!isLetter(token) && j > i)
				return j - 1;
		}
		return regex.length() - 1;
	}
	
	private boolean isLetter(String token) {
		return (!token.equals("(") && !token.equals(")") && !token.equals("+") && !token.equals("*")
				&& !token.equals("|") && !token.equals("?") && !token.equals(","));
	}
	
	private boolean isQuantifier(String token) {
		return (token.equals("+") || token.equals("*") || token.equals("?"));
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
	
	public boolean validateCompleteConstruct(List<String> types, boolean saveState) {
		if (saveState)
			return completeConstructDFA.runAndSave(types);
		else 
			return completeConstructDFA.run(types);
	}
	
	public boolean validatePartialConstruct(List<String> types, boolean saveState) {
		if (saveState)
			return partialConstructDFA.runAndSave(types);
		else 
			return partialConstructDFA.run(types);
	}
	
	public boolean validateTerminalConstruct(List<String> types, boolean saveState) {
		if (saveState)
			return terminalConstructDFA.runAndSave(types);
		else 
			return terminalConstructDFA.run(types);
	}
	
	public boolean validateStartConstruct(List<String> types, boolean saveState) {
		List<String> reverseTypes = new LinkedList<String>();
		for (int i = 0; i < types.size() - 1; i++)
			reverseTypes.add(0, types.get(i));
		if (saveState)
			return startConstructDFA.runAndSave(reverseTypes);
		else 
			return startConstructDFA.run(reverseTypes);
	}

	public boolean isCompleteConstructValid() {
		return completeConstructDFA.isAccepting();
	}
	
	public boolean isPartialConstructValid() {
		return partialConstructDFA.isAccepting();
	}
	
	public boolean isTerminalConstructValid() {
		return terminalConstructDFA.isAccepting();
	}
	
	public boolean isStartConstructValid() {
		return startConstructDFA.isAccepting();
	}
	
	public boolean isCompleteConstructFailed() {
		return completeConstructDFA.inFailState();
	}
	
	public boolean isPartialConstructFailed() {
		return partialConstructDFA.inFailState();
	}
	
	public boolean isTerminalConstructFailed() {
		return terminalConstructDFA.inFailState();
	}
	
	public boolean isStartConstructFailed() {
		return startConstructDFA.inFailState();
	}

	public boolean isTerminalConstructStarted() {
		return !terminalConstructDFA.inStartState();
	}
	
	public boolean isPartialConstructStarted() {
		return !partialConstructDFA.inStartState();
	}
	
	public void resetCompleteConstructValidator() {
		completeConstructDFA.reset();
	}
	
	public void resetPartialConstructValidator() {
		partialConstructDFA.reset();
	}
	
	public void resetTerminalConstructValidator() {
		terminalConstructDFA.reset();
	}
	
	public void resetStartConstructValidator() {
		startConstructDFA.reset();
	}
	
	public void savePartialConstructValidator() {
		partialConstructDFA.save();
	}
	
	public void saveTerminalConstructValidator() {
		terminalConstructDFA.save();
	}
	
	public void loadPartialConstructValidator() {
		partialConstructDFA.load();
	}
	
	public void loadTerminalConstructValidator() {
		terminalConstructDFA.load();
	}
	
	public Set<String> getStartTypes() {
		Set<String> startTypes = new HashSet<String>();
		startTypes.addAll(completeConstructDFA.getStartState().getTransitions().keySet());
		return startTypes;
	}
	
	public Set<String> getTerminalTypes() {
		Set<String> terminalTypes = new HashSet<String>();
		for (DFAState state : completeConstructDFA.getStates().values()) {
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
		
		public void setTransitions(HashMap<String, DFAState> transitions) {
			this.transitions = transitions;
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
		private DFAState currentState;
		private DFAState failState;
		private List<DFAState> saveStates;
		
		public DFA(Set<NFAState> nfaStates) {
			states = new HashMap<String, DFAState>();
			startState = constructDFA(nfaStates);
			currentState = startState;
			failState = new DFAState(GlobalConstants.CONSTRUCT_VALIDATION_FAIL_STATE_ID);
			failState.setAccepting(false);
			saveStates = new LinkedList<DFAState>();
		}
		
		public DFA(HashMap<String, DFAState> states, DFAState startState, DFAState failState) {
			this.states = states;
			this.startState = startState;
			currentState = startState;
			this.failState = failState;
			saveStates = new LinkedList<DFAState>();
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
		
		public DFA clone(String quantifier) {
			HashMap<String, DFAState> clonedStates = new HashMap<String, DFAState>();
			for (String id : states.keySet()) {
				DFAState clonedState = new DFAState(id);
				if (id.equals(GlobalConstants.CONSTRUCT_VALIDATION_FAIL_STATE_ID))
					clonedState.setAccepting(false);
				else if (quantifier.equals("?") && !id.equals(startState.getID()))
					clonedState.setAccepting(true);
				else
					clonedState.setAccepting(states.get(id).isAccepting());
				clonedStates.put(id, clonedState);
			}
			for (String id : states.keySet()) {
				HashMap<String, DFAState> clonedTransitions = new HashMap<String, DFAState>();
				HashMap<String, DFAState> transitions = states.get(id).getTransitions();
				for (String input : transitions.keySet())
					clonedTransitions.put(input, clonedStates.get(transitions.get(input).getID()));
				clonedStates.get(id).setTransitions(clonedTransitions);
			}
			return new DFA(clonedStates, clonedStates.get(startState.getID()), clonedStates.get(failState.getID()));
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
			System.out.println("digraph G {");
			printHelper(startState, new HashSet<String>());
			System.out.println("}");
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
		
		public boolean runAndSave(List<String> inputs) {
			DFAState nextState = currentState;
			for (int i = 0; i < inputs.size(); i++) {
				nextState = nextState.transition(inputs.get(i));
				if (nextState == null) {
					currentState = failState;
					return false;
//					return i;
				} else if (i == inputs.size() - 1)
					currentState = nextState;
			}
//			if (currentState.isAccepting())
//				return inputs.size();
//			else
//				return -1;
			return currentState.isAccepting();
		}
		
		public boolean run(List<String> inputs) {
			DFAState nextState = currentState;
			for (int i = 0; i < inputs.size(); i++) {
				nextState = nextState.transition(inputs.get(i));
				if (nextState == null) {
					return false;
//					return i;
				}
			}
//			if (nextState.isAccepting())
//				return inputs.size();
//			else
//				return -1;
			return nextState.isAccepting();
		}
		
		public boolean isAccepting() {
			return currentState.isAccepting();
		}
		
		public boolean inFailState() {
			return currentState.getID().equals(GlobalConstants.CONSTRUCT_VALIDATION_FAIL_STATE_ID);
		}
		
		public void reset() {
			currentState = startState;
		}
		
		public void save() {
			saveStates.add(0, currentState);
		}
		
		public void load() {
			currentState = saveStates.remove(0);
		}
		
		public boolean inStartState() {
			return currentState == startState;
		}
	}
	
}
