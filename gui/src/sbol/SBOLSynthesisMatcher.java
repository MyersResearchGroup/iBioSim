package sbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SBOLSynthesisMatcher {
	private HashMap<Integer, HashSet<SBOLSynthesisGraph>> output = new HashMap<Integer, HashSet<SBOLSynthesisGraph>>();
	private HashMap<String, Integer> nextMove = new HashMap<String, Integer>();

	public SBOLSynthesisMatcher(Set<SBOLSynthesisGraph> graphLibrary) {
		Set<String> alphabet = new HashSet<String>();
		for (SBOLSynthesisGraph graph : graphLibrary) 
			for (String keyword : graph.getPaths())
				for (int i = 0; i < keyword.length(); i++) 
					alphabet.add(keyword.substring(i, i + 1));
		HashMap<String, Integer> goTo = constructGoTo(graphLibrary);
		constructFailure(alphabet, goTo);
		//		System.out.println(goTo);
		//		System.out.println(output);
		//		System.out.println(failure);
		//		System.out.println(nextMove);
	}

	public List<SBOLSynthesisGraph> match(String input) {
//		HashMap<Integer, HashSet<String>> matched = new HashMap<Integer, HashSet<String>>();
		List<SBOLSynthesisGraph> graphMatches = new LinkedList<SBOLSynthesisGraph>();
		//		System.out.println(input);
		int state = 0;
		for (int i = 0; i < input.length(); i++) {
			String a = input.substring(i, i + 1);
			if (nextMove.containsKey(state + a))
				state = nextMove.get(state + a);
			else
				state = 0;
			if (output.containsKey(state)) {
				graphMatches.addAll(output.get(state));
				//				System.out.println(i);
				//				System.out.println(output.get(state));
			}
		}
		return graphMatches;
	}

	public HashMap<String, Integer> constructGoTo(Set<SBOLSynthesisGraph> library) {
		HashMap<String, Integer> goTo = new HashMap<String, Integer>();
		int newState = 0;
		for (SBOLSynthesisGraph graph : library) {
			for (String keyword : graph.getPaths()) {
				//			String keyword = library.get(id);
				int state = 0;
				int j = 0;
				boolean fail = false;
				while (!fail && j < keyword.length()) {
					String a = keyword.substring(j, j + 1);
					if (goTo.containsKey(state + a)) {
						state = goTo.get(state + a);
						j++;
					} else
						fail = true;
				}
				for (int p = j; p < keyword.length(); p++) {
					newState++;
					goTo.put(state + keyword.substring(p, p + 1), newState);
					state = newState;
				}
				if (!output.containsKey(state))
					output.put(state, new HashSet<SBOLSynthesisGraph>());
				output.get(state).add(graph);
			}
		}
		return goTo;
	}

	public void constructFailure(Set<String> alphabet, HashMap<String, Integer> goTo) {
		HashMap<Integer, Integer> failure = new HashMap<Integer, Integer>();
		LinkedList<Integer> queue = new LinkedList<Integer>();
		for (String a : alphabet) {
			if (goTo.containsKey("0" + a)) {
				int s = goTo.get("0" + a);
				queue.add(s);
				failure.put(s, 0);
				nextMove.put("0" + a, s);
			}
		}	
		while (queue.size() > 0) {
			int r = queue.remove();
			for (String a : alphabet) {
				if (goTo.containsKey(r + a)) {
					int s = goTo.get(r + a);
					queue.add(s);
					int state = failure.get(r);
					while (state != 0 && !goTo.containsKey(state + a))
						state = failure.get(state);
					if (state == 0 && !goTo.containsKey(state + a))
						failure.put(s, 0);
					else
						failure.put(s, goTo.get(state + a));
					int fs = failure.get(s);
					if (output.containsKey(fs)) {
						if (!output.containsKey(s))
							output.put(s, new LinkedHashSet<SBOLSynthesisGraph>());
						output.get(s).addAll(output.get(fs));
					}
					nextMove.put(r + a, s);
				} else {
					int fs = failure.get(r);
					if (nextMove.containsKey(fs + a))
						nextMove.put(r + a, nextMove.get(fs + a));
				}
			}
		}
	}
}
