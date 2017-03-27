/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package backend.sbol.assembly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import main.java.edu.utah.ece.async.util.GlobalConstants;
import main.java.edu.utah.ece.async.util.exceptions.SBOLException;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SequenceTypeValidator {
	private DFA completeConstructDFA;
	private DFA terminalConstructDFA;
	private DFA startConstructDFA;
	private DFA partialConstructDFA;
	private int stateIndex;
	
	public SequenceTypeValidator(String regex) throws SBOLException {
		regex = kleeneRegex(regex);
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
//		startConstructDFA.print();
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
	
	private static void quantifyNFAStates(Set<NFAState> startStates, Set<NFAState> acceptStates, String quantifier, Set<String> localIDs) {
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
	
	private static String kleeneRegex(String regex) throws SBOLException {
		if (isBalanced(regex)) {
			regex = trimOuterParentheses(regex);
			if ((isQuantifier(regex.substring(regex.length() - 1, regex.length())) &&  
					(locateClosingLetter(regex, 0) == regex.length() - 2 || locateClosingParen(regex, 0) == regex.length() - 2)) ||
					locateClosingLetter(regex, 0) == regex.length() - 1) { 
				if (regex.endsWith("?"))
					regex = regex.substring(0, regex.length() - 1) + "*";
				else if (!regex.endsWith("*") && !regex.endsWith("+"))
					regex = regex + "+";
			} else 
				regex = "(" + regex + ")+";
		} 
		return regex;
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
				if (quantifier.equals("*") || quantifier.equals("+")) {
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return "(" + altFragmentRegex(xFragment) + ")" + "(" + xFragment + ")*" + yFragment + "|"
								+ altFragmentRegex(yFragment);
					}
					return "(" + altFragmentRegex(xFragment) + ")" + "(" + xFragment + ")*";
				}
				else if (quantifier.equals("?") || quantifier.equals(",")) {
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return altFragmentRegex("(" + xFragment + ")" + yFragment);
					}
					return altFragmentRegex(xFragment);
				}
				else {
					String yFragment = getSubRegex(regex, i + 1);
					orFragments = findOrFragments(xFragment);
					if (orFragments.size() > 1) {
						String result = altFragmentRegex(orFragments.get(0));
						for (int j = 1; j < orFragments.size(); j++)
							result = result + "|" + altFragmentRegex(orFragments.get(j));
						return ("(" + result + ")" + yFragment + "|" + altFragmentRegex(yFragment));
					}
					return altFragmentRegex(xFragment + "," + yFragment);
				}
			}
			return altFragmentRegex(xFragment);
		} else {
			int i = locateClosingLetter(regex, 0);
			String terminal = regex.substring(0, i + 1);
			if (i + 1 < regex.length()) {
				String quantifier = regex.substring(i + 1, i + 2);
				if (quantifier.equals("*") || quantifier.equals("+")) {
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "," + terminal + "*" + xFragment + "|" + altFragmentRegex(xFragment);
					}
					return terminal + "," + terminal + "*";
				}
				else if (quantifier.equals("?") || quantifier.equals(",")) {
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "," + xFragment + "|" + altFragmentRegex(xFragment);
					}
					return terminal;
				}
				else {
					String xFragment = getSubRegex(regex, i + 1);
					return terminal + "," + xFragment + "|" + altFragmentRegex(xFragment);
				}
			}
			return terminal;
		}
	}
	
	@SuppressWarnings("unused")
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
				if (quantifier.equals("*")) {
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return "(" + xFragment + ")*(" + exFragmentRegex(xFragment) + "|" + exFragmentRegex(yFragment) + ")";
					}
					return "(" + xFragment + ")*(" + exFragmentRegex(xFragment) + ")?";
				}
				else if (quantifier.equals("+")) {
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return "(" + xFragment + ")*" + exFragmentRegex(xFragment + "," + yFragment);
					}
					return "(" + xFragment + ")*" + exFragmentRegex(xFragment);
				}
				else if (quantifier.equals("?")) {
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return "(" + exFragmentRegex(xFragment) + ")?" + exFragmentRegex(yFragment);
					}
					return "(" + exFragmentRegex(xFragment) + ")?";
				}
				else if (quantifier.equals(",")) {
					if (i + 2 < regex.length()) {
						String yFragment = getSubRegex(regex, i + 2);
						return exFragmentRegex("(" + xFragment + ")" + yFragment);
					}
					return exFragmentRegex(xFragment);
				}
				else {
					String yFragment = getSubRegex(regex, i + 1);
					orFragments = findOrFragments(xFragment);
					if (orFragments.size() > 1) {
						String result = exFragmentRegex(orFragments.get(0) + "," + yFragment);
						for (int j = 1; j < orFragments.size(); j++)
							result = result + "|" + exFragmentRegex(orFragments.get(j) + "," + yFragment);
						return result;
					}
					return exFragmentRegex(xFragment + "," + yFragment);
				}
			}
			return exFragmentRegex(xFragment);
		} else {
			int i = locateClosingLetter(regex, 0);
			String terminal = regex.substring(0, i + 1);
			if (i + 1 < regex.length()) {
				String quantifier = regex.substring(i + 1, i + 2);
				if (quantifier.equals("*")) {
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "*(" + terminal + "|" + exFragmentRegex(xFragment) + ")";
					}
					return terminal + "*";
				}
				else if (quantifier.equals("+")) {
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "*" + exFragmentRegex(terminal + "," + xFragment);
					}
					return terminal + "*" + terminal;
				}
				else if (quantifier.equals("?")) {
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "?" + exFragmentRegex(xFragment);
					}
					return terminal + "?";
				}
				else if (quantifier.equals(",")) {
					if (i + 2 < regex.length()) {
						String xFragment = getSubRegex(regex, i + 2);
						return terminal + "(" + exFragmentRegex(xFragment) + ")?";
					}
					return terminal;
				}
				else {
					String xFragment = getSubRegex(regex, i + 1);
					return terminal + "(" + exFragmentRegex(xFragment) + ")?";
				}
			}
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
		}
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
	
	private static int locateClosingParen(String regex, int i) {
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
	
	private static int locateClosingLetter(String regex, int i) {
		for (int j = i; j < regex.length(); j++) {
			String token = regex.substring(j, j + 1);
			if (!isLetter(token) && j > i)
				return j - 1;
		}
		return regex.length() - 1;
	}
	
	private static boolean isLetter(String token) {
		return (!token.equals("(") && !token.equals(")") && !token.equals("+") && !token.equals("*")
				&& !token.equals("|") && !token.equals("?") && !token.equals(","));
	}
	
	private static boolean isQuantifier(String token) {
		return (token.equals("+") || token.equals("*") || token.equals("?"));
	}
	
	private static boolean isBalanced(String regex) throws SBOLException {
		int uncapturedCount = 0;
		for (int i = 0; i < regex.length(); i++)
			if (regex.substring(i, i + 1).equals("("))
				uncapturedCount++;
			else if (regex.substring(i, i + 1).equals(")"))
				uncapturedCount--;
		if (uncapturedCount == 0)
			return true;
//		JOptionPane.showMessageDialog(Gui.frame, "Regular expression for SBOL assembly has unbalanced parentheses.", 
//				"Unbalanced Regex", JOptionPane.ERROR_MESSAGE);

		String message = "Regular expression for SBOL assembly has unbalanced parentheses.";
		String messageTitle = "Unbalanced Regex";
		throw new SBOLException(message, messageTitle);
//		return false;
	}
	
	
	private static List<String> findOrFragments(String regex) {
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
	
	private static int locateClosingOr(String regex, int i) {
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
	
	private static String getSubRegex(String regex, int index) {
		String subRegex = regex.substring(index);
		if (subRegex.startsWith(","))
			return subRegex.substring(1);
		return subRegex;
	}
	
	private static String trimOuterParentheses(String regex) {
		int leftParenCount = 0;
		int rightParenCount = 0;
		for (int i = 0; i < regex.length(); i++)
			if (regex.substring(i, i + 1).equals("("))
				leftParenCount++;
			else
				i = regex.length() - 1;
		for (int i = regex.length() - 1; i >= 0; i--)
			if (regex.substring(i, i + 1).equals(")"))
				rightParenCount++;
			else
				i = 0;
		int trimParenCount = Math.min(leftParenCount, rightParenCount);
		if (trimParenCount > 0)
			return regex.substring(trimParenCount, regex.length() - trimParenCount + 1);
		return regex;
	}
	public boolean validateCompleteConstruct(List<String> types, boolean saveState) {
		if (saveState)
			return completeConstructDFA.runAndSave(types);
		return completeConstructDFA.run(types);
	}
	
	public boolean validatePartialConstruct(List<String> types, boolean saveState) {
		if (saveState)
			return partialConstructDFA.runAndSave(types);
		return partialConstructDFA.run(types);
	}
	
	public boolean validateTerminalConstruct(List<String> types, boolean saveState) {
		if (saveState)
			return terminalConstructDFA.runAndSave(types);
		return terminalConstructDFA.run(types);
	}
	
	public boolean validateStartConstruct(List<String> types, boolean saveState) {
		List<String> reverseTypes = new LinkedList<String>();
		for (int i = 0; i < types.size(); i++)
			reverseTypes.add(0, types.get(i));
		if (saveState)
			return startConstructDFA.runAndSave(reverseTypes);
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
		
		@Override
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
		
		@Override
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
		private String strand;
		private String saveStrand;
		
		public DFA(Set<NFAState> nfaStates) {
			states = new HashMap<String, DFAState>();
			startState = constructDFA(nfaStates);
			currentState = startState;
			failState = new DFAState(GlobalConstants.CONSTRUCT_VALIDATION_FAIL_STATE_ID);
			failState.setAccepting(false);
			saveStates = new LinkedList<DFAState>();
			strand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
		}
		
		public DFA(HashMap<String, DFAState> states, DFAState startState, DFAState failState) {
			this.states = states;
			this.startState = startState;
			currentState = startState;
			this.failState = failState;
			saveStates = new LinkedList<DFAState>();
			strand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
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
				else if (quantifier.equals("?"))
//					&& !id.equals(startState.getID()
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
		@SuppressWarnings("unused")
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
			for (int i = 0; i < inputs.size(); i++) {
				if (inputs.get(i).equals(GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND)
						|| inputs.get(i).equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND)) {
					if (!strand.equals(inputs.get(i))) {
						currentState = startState;
						strand = inputs.get(i);
					}
				} else {
					currentState = currentState.transition(inputs.get(i));
					if (currentState == null) {
						currentState = failState;
						return false;
					}	
				}
			}
			return currentState.isAccepting();
		}
		
		public boolean run(List<String> inputs) {
			DFAState nextState = currentState;
			String currentStrand = strand;
			for (int i = 0; i < inputs.size(); i++) {
				if (inputs.get(i).equals(GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND)
						|| inputs.get(i).equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND)) {
					if (!currentStrand.equals(inputs.get(i))) {
						nextState = startState;
						currentStrand = inputs.get(i);
					}
				} else {
					nextState = nextState.transition(inputs.get(i));
					if (nextState == null)
						return false;
				}
			}
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
			strand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
		}
		
		public void save() {
			saveStates.add(0, currentState);
			saveStrand = strand;
		}
		
		public void load() {
			currentState = saveStates.remove(0);
			strand = saveStrand;
		}
		
		public boolean inStartState() {
			return currentState == startState;
		}
	}
	
}
