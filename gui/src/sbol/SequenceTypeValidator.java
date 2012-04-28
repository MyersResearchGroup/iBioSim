package sbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class SequenceTypeValidator {
	
	private NFAState nfaStart;
	private DFAState dfaStart;
	private String regex;
	private int regIndex;
	
	public SequenceTypeValidator (String regex, Set<String> terminals) {
		this.regex = regex;
		regIndex = 0;
		nfaStart = new NFAState("S" + regIndex);
		constructNFA(nfaStart);
		int i = 0;
		do {
			String input = regex.substring(i, i + 1);
			if (input.equals("(")) {
				int closeParen = 0;
				int openParen = 1;
				int j = i + 1;
				do {
					String lookAhead = regex.substring(j, j + 1);
					if (lookAhead.equals("("))
						openParen++;
					else if (lookAhead.equals(")"))
						closeParen++;
					j++;
				} while (openParen != closeParen && j < regex.length());
				if (j < regex.length()) {
					String modifier = regex.substring(j, j + 1);
					if (modifier.equals("+"))
						;
					else if (modifier.equals("*"))
						;
				}
			}
		} while (i < regex.length());
	}
	
	public NFAState constructNFA(NFAState startState) { 
		NFAState currentState = startState;
		NFAState flaggedState = null;
		String flaggedInput = null;
		boolean flagged = false;
		do {
			String input = regex.substring(regIndex, regIndex + 1);
			String lookAhead1 = regex.substring(regIndex + 1, regIndex + 2);
			if (input.equals("("))
				constructNFA(currentState);
			else if (lookAhead1.equals(")")) {
				String lookAhead2 = regex.substring(regIndex + 2, regIndex + 3);
				if (lookAhead2.equals("+"))
					currentState.addTransition(flaggedInput, flaggedState);
//				else if (lookAhead2.equals("*"))
//					currentState.addTransition(, startState);
			}
			else {
				NFAState nextState = new NFAState("S");
				currentState.addTransition(input, nextState);
				currentState = nextState;
				if (!flagged) {
					flaggedState = nextState;
					flaggedInput = input;
					flagged = true;
				}
			}
		} while (regIndex < regex.length());
		return currentState;
	}
	
	private class NFAState {
		
		private HashMap<String, Set<NFAState>> transitions;
		private String stateID;
		
		private NFAState (String stateID) {
			this.stateID = stateID;
			transitions = new HashMap<String, Set<NFAState>>();
		}
		
		private void addTransition(String input, NFAState destination) {
			if (transitions.containsKey(input))
				transitions.get(input).add(destination);
			else {
				transitions.put(input, new HashSet<NFAState>());
				transitions.get(input).add(destination);
			}
		}
		
		private Set<NFAState> transition(String input) {
			return transitions.get(input);
		}
		
	}
	
	private class DFAState {
		
		private HashMap<String, DFAState> transitions;
		
		private DFAState() {
			transitions = new HashMap<String, DFAState>();
		}
		
		private void addTransition(String input, DFAState destination) {
			transitions.put(input, destination);
		}
		
		private DFAState transition(String input) {
			return transitions.get(input);
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
