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
package backend.synthesis.genetic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SynthesisMatcher {
	private HashMap<Integer, HashSet<SynthesisGraph>> output = new HashMap<Integer, HashSet<SynthesisGraph>>();
	private HashMap<String, Integer> nextMove = new HashMap<String, Integer>();

	public SynthesisMatcher(Set<SynthesisGraph> graphLibrary) 
	{
		//NOTE: get each part within the library and walk that part. Add to alphabet list as parts are being walked
		Set<String> alphabet = new HashSet<String>();
		for (SynthesisGraph graph : graphLibrary) 
			for (String keyword : graph.getPaths())
				for (int i = 0; i < keyword.length(); i++) 
					alphabet.add(keyword.substring(i, i + 1)); //TODO: why are we looking at a substring of the path and not each index of that path?
		HashMap<String, Integer> goTo = constructGoTo(graphLibrary); //TODO: don't understand how this works and constructFailure()...
		constructFailure(alphabet, goTo);
		//		System.out.println(goTo);
		//		System.out.println(output);
		//		System.out.println(failure);
		//		System.out.println(nextMove);
	}

	public List<SynthesisGraph> match(String input) {
//		HashMap<Integer, HashSet<String>> matched = new HashMap<Integer, HashSet<String>>();
		List<SynthesisGraph> graphMatches = new LinkedList<SynthesisGraph>();
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

	public HashMap<String, Integer> constructGoTo(Set<SynthesisGraph> library) {
		HashMap<String, Integer> goTo = new HashMap<String, Integer>();
		int newState = 0;
		for (SynthesisGraph graph : library) {
			for (String keyword : graph.getPaths()) {
				//			String keyword = library.get(id);
				int state = 0;
				int j = 0;
				boolean fail = false;
				while (!fail && j < keyword.length()) 
				{
					//TODO: why do we want to get substring of a path?
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
					output.put(state, new HashSet<SynthesisGraph>());
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
							output.put(s, new LinkedHashSet<SynthesisGraph>());
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
