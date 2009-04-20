package lhpn2sbml.parser;

//import java.io.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biomodelsim.Log;

public class Abstraction {

	private HashMap<String, Boolean> places;

	private HashMap<String, String> inputs;

	private HashMap<String, String> outputs;

	private HashMap<String, String> enablings;

	private HashMap<String, Properties> controlFlow;

	private HashMap<String, Properties> controlPlaces;

	private HashMap<String, Properties> variables;

	private HashMap<String, String> integers;

	private HashMap<String, Properties> rateAssignments;

	private HashMap<String, HashMap<String, ExprTree[]>> rateAssignmentTrees;

	private HashMap<String, Properties> contAssignments;

	private HashMap<String, HashMap<String, ExprTree[]>> contAssignmentTrees;

	private HashMap<String, Properties> intAssignments;

	private HashMap<String, HashMap<String, ExprTree[]>> intAssignmentTrees;

	private HashMap<String, String> delays;

	private HashMap<String, ExprTree[]> delayTrees;

	private HashMap<String, ExprTree> transitionRates;

	private HashMap<String, String> transitionRateStrings;

	private HashMap<String, Properties> booleanAssignments;

	private HashMap<String, HashMap<String, ExprTree[]>> booleanAssignmentTrees;

	private ArrayList<HashMap<String, Properties>> assignments = new ArrayList<HashMap<String, Properties>>();

	private String property;

	private Log log;

	public Abstraction(Log log) {
		this.log = log;
		places = new HashMap<String, Boolean>();
		inputs = new HashMap<String, String>();
		outputs = new HashMap<String, String>();
		enablings = new HashMap<String, String>();
		controlFlow = new HashMap<String, Properties>();
		controlPlaces = new HashMap<String, Properties>();
		variables = new HashMap<String, Properties>();
		integers = new HashMap<String, String>();
		rateAssignments = new HashMap<String, Properties>();
		rateAssignmentTrees = new HashMap<String, HashMap<String, ExprTree[]>>();
		contAssignments = new HashMap<String, Properties>();
		contAssignmentTrees = new HashMap<String, HashMap<String, ExprTree[]>>();
		intAssignments = new HashMap<String, Properties>();
		intAssignmentTrees = new HashMap<String, HashMap<String, ExprTree[]>>();
		delays = new HashMap<String, String>();
		delayTrees = new HashMap<String, ExprTree[]>();
		transitionRates = new HashMap<String, ExprTree>();
		transitionRateStrings = new HashMap<String, String>();
		booleanAssignments = new HashMap<String, Properties>();
		booleanAssignmentTrees = new HashMap<String, HashMap<String, ExprTree[]>>();
		assignments.add(booleanAssignments);
		assignments.add(contAssignments);
		assignments.add(intAssignments);
		assignments.add(rateAssignments);
	}

	public void abstractSTG() {
		// Transform 0 - Merge Parallel Places
		for (String s : controlPlaces.keySet()) {
			for (String t : controlPlaces.keySet()) {
				if (!s.equals(t)) {
					Properties prop1 = controlPlaces.get(s);
					Properties prop2 = controlPlaces.get(t);
					boolean assign = false;
					for (HashMap<String, Properties> h : assignments) {
						if (h.get(t) == null || h.get(t).keySet().isEmpty()) {
							assign = true;
						}
					}
					// System.out.println(s + t);
					if (comparePreset(prop1, prop2) && comparePostset(prop1, prop2)
							&& (places.get(s).equals(places.get(t))) && !assign) {
						combinePlaces(s, t);
					}
				}
			}
		}
		// Transform 1 - Remove a Place in a Self Loop
		for (String s : controlPlaces.keySet()) {
			String[] preset = controlPlaces.get(s).getProperty("preset").split(" ");
			String[] postset = controlPlaces.get(s).getProperty("postset").split(" ");
			if (preset.length == 1 && postset.length == 1) {
				if (preset[0].equals(postset[0])) {
					removePlace(s);
				}
				else {
					continue;
				}
			}
		}
		// Transform 3 - Remove a Transition with a Single Place in the Postset
		ArrayList<String> remove = new ArrayList<String>();
		for (String s : controlFlow.keySet()) {
			String[] postset = controlFlow.get(s).getProperty("postset").split(" ");
			if (postset.length == 1) {
				boolean assign = false;
				for (HashMap<String, Properties> h : assignments) {
					// System.out.println(assignments);
					if (h.get(s) != null) {
						if (!h.get(s).keySet().isEmpty()) {
							assign = true;
						}
					}
				}
				String[] preset = controlPlaces.get(postset[0]).getProperty("preset").split(" ");
				if (preset.length == 1 && !assign) {
					remove.add(s);
				}
			}
		}
		for (String s : remove) {
			removeTrans3(s, controlFlow.get(s).getProperty("preset").split(" "), controlFlow.get(s)
					.getProperty("postset").split(" "));
		}
		// Transform 4 - Remove a Transition with a Single Place in the Preset
		for (String s : controlFlow.keySet()) {
			String[] preset = controlFlow.get(s).getProperty("preset").split(" ");
			if (preset.length == 1) {
				boolean assign = false;
				for (HashMap<String, Properties> h : assignments) {
					if (h.get(s) != null) {
						if (!h.get(s).keySet().isEmpty()) {
							assign = true;
						}
					}
				}
				String[] postset = controlPlaces.get(preset[0]).getProperty("postset").split(" ");
				if (postset.length == 1 && !assign) {
					removeTrans4(s, preset, postset);
				}
			}
		}
		// Transforms 5a, 6, 7 - Combine Transitions with the Same Preset and/or
		// Postset
		for (String s : controlFlow.keySet()) {
			for (String t : controlFlow.keySet()) {
				boolean samePreset = comparePreset(controlFlow.get(s), controlFlow.get(t));
				boolean samePostset = comparePostset(controlFlow.get(s), controlFlow.get(t));
				boolean assign = false;
				for (HashMap<String, Properties> h : assignments) {
					if (h.get(t) == null || h.get(t).keySet().isEmpty()) {
						assign = true;
					}
				}
				if ((samePreset || samePostset) && !assign) {
					combineTransitions(s, t, samePreset, samePostset);
				}
			}
		}
		// Transform 5b
		for (String s : controlFlow.keySet()) {
			for (String t : controlFlow.keySet()) {
				boolean transform = true;
				String[] preset1 = controlFlow.get(s).getProperty("preset").split(" ");
				String[] preset2 = controlFlow.get(t).getProperty("preset").split(" ");
				boolean assign = false;
				for (HashMap<String, Properties> h : assignments) {
					if (h.get(t) == null || h.get(t).keySet().isEmpty()) {
						assign = true;
					}
				}
				if (!assign) {
					for (String u : preset1) {
						if (transform) {
							for (String v : preset2) {
								Properties prop1 = controlPlaces.get(u);
								Properties prop2 = controlPlaces.get(v);
								if (!comparePostset(prop1, prop2)) {
									transform = false;
									break;
								}
							}
						}
					}
					if (transform) {
						String[] postset1 = controlFlow.get(s).getProperty("postset").split(" ");
						String[] postset2 = controlFlow.get(t).getProperty("postset").split(" ");
						for (String u : postset1) {
							for (String v : postset2) {
								Properties prop1 = controlPlaces.get(u);
								Properties prop2 = controlPlaces.get(v);
								if (!comparePreset(prop1, prop2)) {
									transform = false;
									break;
								}
							}
						}
					}
				}
				if (transform && !assign) {
					combineTransitions(s, t, true, true);
				}
			}
		}
	}

	public void abstractVars(String[] vars) {
		for (String s : vars) {
			for (String t : controlFlow.keySet()) {
				for (HashMap<String, Properties> v : assignments) {
					if (v.containsKey(t)) {
						if (v.get(t).containsKey(s)) {
							v.get(t).remove(s);
						}
					}
				}
			}
			for (String t : enablings.keySet()) {
				if (enablings.get(t).contains(s)) {
					enablings.put(s, enablings.get(t).replace(s, "X"));
				}
			}
		}
	}

	public void save(String filename) {
		try {
			String file = filename;
			PrintStream p = new PrintStream(new FileOutputStream(filename));
			StringBuffer buffer = new StringBuffer();
			HashMap<String, Integer> boolOrder = new HashMap<String, Integer>();
			int i = 0;
			if (!inputs.isEmpty()) {
				buffer.append(".inputs ");
				for (String s : inputs.keySet()) {
					if (inputs.get(s) != null) {
						buffer.append(s + " ");
						boolOrder.put(s, i);
						i++;
					}
				}
				buffer.append("\n");
			}
			if (!outputs.isEmpty()) {
				buffer.append(".outputs ");
				for (String s : outputs.keySet()) {
					if (outputs.get(s) != null) {
						buffer.append(s + " ");
						boolOrder.put(s, i);
						i++;
					}
				}
				buffer.append("\n");
			}
			if (!controlFlow.isEmpty()) {
				buffer.append(".dummy ");
				for (String s : controlFlow.keySet()) {
					buffer.append(s + " ");
				}
				buffer.append("\n");
			}
			if (!variables.isEmpty() || !integers.isEmpty()) {
				buffer.append("#@.variables ");
				for (String s : variables.keySet()) {
					buffer.append(s + " ");
				}
				for (String s : integers.keySet()) {
					buffer.append(s + " ");
				}
				buffer.append("\n");
			}
			if (!places.isEmpty()) {
				buffer.append("#|.places ");
				for (String s : places.keySet()) {
					buffer.append(s + " ");
				}
				buffer.append("\n");
			}
			if (!inputs.isEmpty() || !outputs.isEmpty()) {
				boolean flag = false;
				for (i = 0; i < boolOrder.size(); i++) {
					for (String s : inputs.keySet()) {
						if (boolOrder.get(s).equals(i)) {
							if (!flag) {
								buffer.append("#@.init_state [");
								flag = true;
							}
							if (inputs.get(s).equals("true")) {
								buffer.append("1");
							}
							else if (inputs.get(s).equals("false")) {
								buffer.append("0");
							}
							else {
								buffer.append("X");
							}
						}
					}
					for (String s : outputs.keySet()) {
						if (s != null && boolOrder.get(s) != null) {
							// log.addText(s);
							if (boolOrder.get(s).equals(i) && outputs.get(s) != null) {
								if (!flag) {
									buffer.append("#@.init_state [");
									flag = true;
								}
								if (outputs.get(s).equals("true")) {
									buffer.append("1");
								}
								else if (outputs.get(s).equals("false")) {
									buffer.append("0");
								}
								else {
									buffer.append("X");
								}
							}
						}
					}
				}
				if (flag) {
					buffer.append("]\n");
				}
			}
			if (!controlFlow.isEmpty()) {
				buffer.append(".graph\n");
				for (String s : controlFlow.keySet()) {
					// log.addText(s);
					if (controlFlow.get(s) != null) {
						Properties prop = controlFlow.get(s);
						// log.addText(s + prop.getProperty("postset"));
						if (prop.getProperty("postset") != null) {
							String toString = prop.getProperty("postset");
							if (toString != null) {
								// log.addText("to " + toString);
								String[] toArray = toString.split("\\s");
								for (i = 0; i < toArray.length; i++) {
									if (toArray[i] != null && !toArray[i].equals("null")) {
										buffer.append(s + " " + toArray[i] + "\n");
									}
								}
							}
						}
						if (prop.getProperty("preset") != null) {
							String fromString = prop.getProperty("preset");
							if (fromString != null) {
								// log.addText("from "+ fromString);
								String[] fromArray = fromString.split("\\s");
								for (i = 0; i < fromArray.length; i++) {
									if (fromArray[i] != null && !fromArray[i].equals("null")) {
										buffer.append(fromArray[i] + " " + s + "\n");
									}
								}
							}
						}
					}
				}
			}
			boolean flag = false;
			if (!places.keySet().isEmpty()) {
				for (String s : places.keySet()) {
					if (places.get(s).equals(true)) {
						if (!flag) {
							buffer.append(".marking {");
							flag = true;
						}
						buffer.append(s + " ");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			if (property != null && !property.equals("")) {
				buffer.append("#@.property " + property + "\n");
			}
			if (!variables.isEmpty() || !integers.isEmpty()) {
				buffer.append("#@.init_vals {");
				for (String s : variables.keySet()) {
					Properties prop = variables.get(s);
					buffer.append("<" + s + "=" + prop.getProperty("value") + ">");
				}
				for (String s : integers.keySet()) {
					String ic = integers.get(s);
					buffer.append("<" + s + "=" + ic + ">");
				}
				if (!variables.isEmpty()) {
					buffer.append("}\n#@.init_rates {");
					for (String s : variables.keySet()) {
						Properties prop = variables.get(s);
						buffer.append("<" + s + "=" + prop.getProperty("rate") + ">");
					}
				}
				buffer.append("}\n");
			}
			if (!enablings.isEmpty()) {
				flag = false;
				for (String s : enablings.keySet()) {
					if (s != null && !enablings.get(s).equals("")) {
						if (!flag) {
							buffer.append("#@.enablings {");
							flag = true;
						}
						// log.addText("here " + enablings.get(s));
						buffer.append("<" + s + "=[" + enablings.get(s) + "]>");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			if (!contAssignments.isEmpty() || !intAssignments.isEmpty()) {
				flag = false;
				for (String s : contAssignments.keySet()) {
					Properties prop = contAssignments.get(s);
					// log.addText(prop.toString());
					if (!prop.isEmpty()) {
						if (!flag) {
							buffer.append("#@.assignments {");
							flag = true;
						}
						buffer.append("<" + s + "=");
						for (Object key : prop.keySet()) {
							String t = (String) key;
							buffer.append("[" + t + ":=" + prop.getProperty(t) + "]");
						}
						buffer.append(">");
					}
				}
				for (String s : intAssignments.keySet()) {
					Properties prop = intAssignments.get(s);
					if (!prop.isEmpty()) {
						if (!flag) {
							buffer.append("#@.assignments {");
							flag = true;
						}
						buffer.append("<" + s + "=");
						for (Object key : prop.keySet()) {
							String t = (String) key;
							// log.addText("key " + t);
							buffer.append("[" + t + ":=" + prop.getProperty(t) + "]");
						}
						buffer.append(">");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			if (!rateAssignments.isEmpty()) {
				flag = false;
				for (String s : rateAssignments.keySet()) {
					boolean varFlag = false;
					Properties prop = rateAssignments.get(s);
					for (Object key : prop.keySet()) {
						String t = (String) key;
						if (!t.equals("")) {
							if (!flag) {
								buffer.append("#@.rate_assignments {");
								flag = true;
							}
							if (!varFlag) {
								buffer.append("<" + s + "=");
								varFlag = true;
							}
							buffer.append("[" + t + ":=" + prop.getProperty(t) + "]");
						}
					}
					if (varFlag) {
						buffer.append(">");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			if (!delays.isEmpty()) {
				flag = false;
				for (String s : delays.keySet()) {
					if (s != null && !delays.get(s).equals("")) {
						if (!flag) {
							buffer.append("#@.delay_assignments {");
							flag = true;
						}
						buffer.append("<" + s + "=" + delays.get(s) + ">");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			if (!transitionRateStrings.isEmpty()) {
				flag = false;
				for (String s : transitionRateStrings.keySet()) {
					if (s != null && !transitionRateStrings.get(s).equals("")) {
						if (!flag) {
							buffer.append("#@.transition_rates {");
							flag = true;
						}
						// log.addText("here " + enablings.get(s));
						buffer.append("<" + s + "=[" + transitionRateStrings.get(s) + "]>");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			if (!booleanAssignments.isEmpty()) {
				flag = false;
				for (String s : booleanAssignments.keySet()) {
					if (!s.equals("")) {
						boolean varFlag = false;
						Properties prop = booleanAssignments.get(s);
						for (Object key : prop.keySet()) {
							String t = (String) key;
							if (!t.equals("") && (isInput(t) || isOutput(t))) {
								if (!flag) {
									buffer.append("#@.boolean_assignments {");
									flag = true;
								}
								if (!varFlag) {
									buffer.append("<" + s + "=");
									varFlag = true;
								}
								buffer.append("[" + t + ":=" + prop.getProperty(t) + "]");
							}
						}
						if (varFlag) {
							buffer.append(">");
						}
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			if (!variables.isEmpty()) {
				buffer.append("#@.continuous ");
				for (String s : variables.keySet()) {
					buffer.append(s + " ");
				}
				buffer.append("\n");
			}
			if (buffer.toString().length() > 0) {
				buffer.append(".end\n");
			}
			// System.out.print(buffer);
			p.print(buffer);
			p.close();
			if (log != null) {
				log.addText("Saving:\n" + file + "\n");
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private boolean comparePreset(Properties flow1, Properties flow2) {
		String[] set1 = flow1.get("preset").toString().split(" ");
		String[] set2 = flow2.get("preset").toString().split(" ");
		if (set1.length != set2.length) {
			return false;
		}
		boolean contains = false;
		for (int i = 0; i < set1.length; i++) {
			contains = false;
			for (int j = 0; j < set2.length; j++) {
				if (set1[i].equals(set2[j])) {
					contains = true;
				}
			}
			if (!contains) {
				return false;
			}
		}
		return true;
	}

	private boolean comparePostset(Properties flow1, Properties flow2) {
		// System.out.println(flow1);
		// System.out.println(flow2);
		// System.out.println(flow1.get("postset"));
		// System.out.println(flow2.get("postset"));
		String[] set1 = flow1.get("postset").toString().split(" ");
		String[] set2 = flow2.get("postset").toString().split(" ");
		if (set1.length != set2.length) {
			return false;
		}
		boolean contains = false;
		for (int i = 0; i < set1.length; i++) {
			contains = false;
			for (int j = 0; j < set2.length; j++) {
				if (set1[i].equals(set2[j])) {
					contains = true;
				}
			}
			if (!contains) {
				return false;
			}
		}
		return true;
	}

	private void combinePlaces(String place1, String place2) {
		String newPlace = new String();
		newPlace = place1;
		for (String s : controlFlow.keySet()) {
			Properties prop = controlFlow.get(s);
			String[] array = prop.getProperty("preset").split(" ");
			String setString = new String();
			if (array[0].equals(place2)) {
				setString = newPlace;
			}
			else {
				setString = array[0];
			}
			for (int i = 1; i < array.length; i++) {
				if (array[i].equals(place2)) {
					array[i] = newPlace;
				}
				setString = setString + " " + array[i];
			}
			prop.setProperty("preset", setString);
			array = prop.getProperty("postset").split(" ");
			setString = new String();
			if (array[0].equals(place2)) {
				setString = newPlace;
			}
			else {
				setString = array[0];
			}
			for (int i = 1; i < array.length; i++) {
				if (array[i].equals(place2)) {
					array[i] = newPlace;
				}
				setString = setString + " " + array[i];
			}
			prop.setProperty("postset", setString);
			controlFlow.put(s, prop);
		}
		String[] preset1 = controlPlaces.get(place1).getProperty("preset").split(" ");
		String[] preset2 = controlPlaces.get(place2).getProperty("preset").split(" ");
		String[] postset1 = controlPlaces.get(place1).getProperty("postset").split(" ");
		String[] postset2 = controlPlaces.get(place2).getProperty("postset").split(" ");
		String preset = new String();
		String postset = new String();
		for (String s : preset1) {
			preset = preset + s + " ";
			for (int i = 0; i < preset2.length; i++) {
				if (s.equals(preset2[i])) {
					preset2[i] = "";
				}
			}
		}
		for (String s : preset2) {
			if (!s.equals("")) {
				preset = preset + s + " ";
			}
		}
		for (String s : postset1) {
			postset = postset + s + " ";
			for (int i = 0; i < postset2.length; i++) {
				if (s.equals(postset2[i])) {
					postset2[i] = "";
				}
			}
		}
		for (String s : postset2) {
			if (!s.equals("")) {
				postset = postset + s + " ";
			}
		}
		Properties prop = new Properties();
		prop.setProperty("preset", preset.trim());
		prop.setProperty("postset", postset.trim());
		controlPlaces.put(place1, prop);
		removePlace(place2);
	}

	private void removeTrans3(String transition, String[] preset, String[] postset) {
		String place = postset[0];
		preset = controlFlow.get(transition).getProperty("preset").split(" ");
		postset = controlPlaces.get(postset[0]).getProperty("postset").split(" ");
		// Combine control Flow
		for (String t : preset) {
			String[] tempPostset = controlPlaces.get(t).getProperty("postset").split(" ");
			String tempList = "";
			for (int i = 0; i < tempPostset.length; i++) {
				if (!tempPostset[i].equals(transition)) {
					tempList = tempList + tempPostset[i] + " ";
				}
			}
			for (int i = 0; i < postset.length; i++) {
				tempList = tempList + postset[i] + " ";
			}
			Properties prop = controlPlaces.get(t);
			prop.setProperty("postset", tempList.trim());
			controlPlaces.put(t, prop);
		}
		for (String t : postset) {
			String[] tempPreset = controlFlow.get(t).getProperty("preset").split(" ");
			String tempList = "";
			for (int i = 0; i < tempPreset.length; i++) {
				if (!tempPreset[i].equals(place)) {
					tempList = tempList + tempPreset[i] + " ";
				}
			}
			for (int i = 0; i < preset.length; i++) {
				tempList = tempList + preset[i] + " ";
			}
			Properties prop = controlFlow.get(t);
			prop.setProperty("preset", tempList.trim());
			controlFlow.put(t, prop);
		}
		// Add delays
		String[] oldDelay = new String[2];
		Pattern rangePattern = Pattern.compile(RANGE);
		Matcher rangeMatcher = rangePattern.matcher(delays.get(transition));
		if (rangeMatcher.find()) {
			oldDelay[0] = rangeMatcher.group(1);
			oldDelay[1] = rangeMatcher.group(2);
		}
		for (String t : postset) {
			Matcher newMatcher = rangePattern.matcher(delays.get(t));
			if (newMatcher.find()) {
				String newDelay[] = { newMatcher.group(1), newMatcher.group(2) };
				for (int i = 0; i < newDelay.length; i++) {
					if (!oldDelay[i].equals("inf") && !newDelay[i].equals("inf")) {
						newDelay[i] = String.valueOf(Integer.parseInt(newDelay[i])
								+ Integer.parseInt(oldDelay[i]));
					}
					else {
						newDelay[i] = "inf";
					}
				}
				delays.put(t, "[" + newDelay[0] + "," + newDelay[1] + "]");
			}
		}
		// Combine assignments
		Properties oldProp = contAssignments.get(transition);
		if (oldProp != null) {
			for (String t : postset) {
				Properties newProp = contAssignments.get(t);
				for (Object o : oldProp.keySet()) {
					if (!newProp.containsKey(o)) {
						newProp.setProperty(o.toString(), oldProp.getProperty(o.toString()));
					}
				}
				contAssignments.put(t, newProp);
			}
		}
		oldProp = booleanAssignments.get(transition);
		if (oldProp != null) {
			for (String t : postset) {
				Properties newProp = booleanAssignments.get(t);
				for (Object o : oldProp.keySet()) {
					if (!newProp.containsKey(o)) {
						newProp.setProperty(o.toString(), oldProp.getProperty(o.toString()));
					}
				}
				booleanAssignments.put(t, newProp);
			}
		}
		oldProp = intAssignments.get(transition);
		if (oldProp != null) {
			for (String t : postset) {
				Properties newProp = intAssignments.get(t);
				for (Object o : oldProp.keySet()) {
					if (!newProp.containsKey(o)) {
						newProp.setProperty(o.toString(), oldProp.getProperty(o.toString()));
					}
				}
				intAssignments.put(t, newProp);
			}
		}
		HashMap<String, ExprTree[]> oldMap = contAssignmentTrees.get(transition);
		if (oldMap != null) {
			for (String t : postset) {
				HashMap<String, ExprTree[]> newMap = contAssignmentTrees.get(t);
				for (Object o : oldMap.keySet()) {
					if (!newMap.containsKey(o)) {
						newMap.put(o.toString(), oldMap.get(o.toString()));
					}
				}
				contAssignmentTrees.put(t, newMap);
			}
		}
		oldMap = booleanAssignmentTrees.get(transition);
		if (oldMap != null) {
			for (String t : postset) {
				HashMap<String, ExprTree[]> newMap = booleanAssignmentTrees.get(t);
				for (Object o : oldMap.keySet()) {
					if (!newMap.containsKey(o)) {
						newMap.put(o.toString(), oldMap.get(o.toString()));
					}
				}
				booleanAssignmentTrees.put(t, newMap);
			}
		}
		oldMap = intAssignmentTrees.get(transition);
		if (oldMap != null) {
			for (String t : postset) {
				HashMap<String, ExprTree[]> newMap = intAssignmentTrees.get(t);
				for (Object o : oldMap.keySet()) {
					if (!newMap.containsKey(o)) {
						newMap.put(o.toString(), oldMap.get(o.toString()));
					}
				}
				intAssignmentTrees.put(t, newMap);
			}
		}
		removeTransition(transition);
	}

	private void removeTrans4(String transition, String[] preset, String[] postset) {
		String place = postset[0];
		postset = controlFlow.get(transition).getProperty("postset").split(" ");
		preset = controlPlaces.get(preset[0]).getProperty("preset").split(" ");
		// Combine control Flow
		for (String t : postset) {
			String[] tempPostset = controlFlow.get(t).getProperty("postset").split(" ");
			String tempList = "";
			for (int i = 0; i < tempPostset.length; i++) {
				if (!tempPostset[i].equals(transition)) {
					tempList = tempList + tempPostset[i] + " ";
				}
			}
			for (int i = 0; i < preset.length; i++) {
				tempList = tempList + preset[i] + " ";
			}
			Properties prop = controlFlow.get(t);
			prop.setProperty("preset", tempList.trim());
			controlFlow.put(t, prop);
		}
		for (String t : preset) {
			String[] tempPreset = controlPlaces.get(t).getProperty("preset").split(" ");
			String tempList = "";
			for (int i = 0; i < tempPreset.length; i++) {
				if (!tempPreset[i].equals(place)) {
					tempList = tempList + tempPreset[i] + " ";
				}
			}
			for (int i = 0; i < postset.length; i++) {
				tempList = tempList + postset[i] + " ";
			}
			Properties prop = controlFlow.get(t);
			prop.setProperty("postset", tempList.trim());
			controlFlow.put(t, prop);
		}
		// Add delays
		String[] oldDelay = new String[2];
		Pattern rangePattern = Pattern.compile(RANGE);
		Matcher rangeMatcher = rangePattern.matcher(delays.get(transition));
		if (rangeMatcher.find()) {
			oldDelay[0] = rangeMatcher.group(1);
			oldDelay[1] = rangeMatcher.group(2);
		}
		for (String t : postset) {
			Matcher newMatcher = rangePattern.matcher(delays.get(t));
			if (newMatcher.find()) {
				String newDelay[] = { newMatcher.group(1), newMatcher.group(2) };
				for (int i = 0; i < newDelay.length; i++) {
					newDelay[i] = String.valueOf(Integer.parseInt(newDelay[i])
							+ Integer.parseInt(oldDelay[i]));
				}
				delays.put(transition, "[" + newDelay[0] + "," + newDelay[1] + "]");
			}
		}
		// Combine assignments
		Properties oldProp = contAssignments.get(transition);
		for (String t : postset) {
			Properties newProp = contAssignments.get(t);
			for (Object o : oldProp.keySet()) {
				if (!newProp.containsKey(o)) {
					newProp.setProperty(o.toString(), oldProp.getProperty(o.toString()));
				}
			}
			contAssignments.put(t, newProp);
		}
		oldProp = booleanAssignments.get(transition);
		for (String t : postset) {
			Properties newProp = booleanAssignments.get(t);
			for (Object o : oldProp.keySet()) {
				if (!newProp.containsKey(o)) {
					newProp.setProperty(o.toString(), oldProp.getProperty(o.toString()));
				}
			}
			booleanAssignments.put(t, newProp);
		}
		oldProp = intAssignments.get(transition);
		for (String t : postset) {
			Properties newProp = intAssignments.get(t);
			for (Object o : oldProp.keySet()) {
				if (!newProp.containsKey(o)) {
					newProp.setProperty(o.toString(), oldProp.getProperty(o.toString()));
				}
			}
			intAssignments.put(t, newProp);
		}
		HashMap<String, ExprTree[]> oldMap = contAssignmentTrees.get(transition);
		for (String t : postset) {
			HashMap<String, ExprTree[]> newMap = contAssignmentTrees.get(t);
			for (Object o : oldMap.keySet()) {
				if (!newMap.containsKey(o)) {
					newMap.put(o.toString(), oldMap.get(o.toString()));
				}
			}
			contAssignmentTrees.put(t, newMap);
		}
		oldMap = booleanAssignmentTrees.get(transition);
		for (String t : postset) {
			HashMap<String, ExprTree[]> newMap = booleanAssignmentTrees.get(t);
			for (Object o : oldMap.keySet()) {
				if (!newMap.containsKey(o)) {
					newMap.put(o.toString(), oldMap.get(o.toString()));
				}
			}
			booleanAssignmentTrees.put(t, newMap);
		}
		oldMap = intAssignmentTrees.get(transition);
		for (String t : postset) {
			HashMap<String, ExprTree[]> newMap = intAssignmentTrees.get(t);
			for (Object o : oldMap.keySet()) {
				if (!newMap.containsKey(o)) {
					newMap.put(o.toString(), oldMap.get(o.toString()));
				}
			}
			intAssignmentTrees.put(t, newMap);
		}
		removeTransition(transition);
	}

	private void combineTransitions(String trans1, String trans2, boolean samePreset,
			boolean samePostset) {
		// Combine Control Flow
		String[] preset1 = controlFlow.get(trans1).getProperty("preset").split(" ");
		String[] preset2 = controlFlow.get(trans2).getProperty("preset").split(" ");
		String[] postset1 = controlFlow.get(trans1).getProperty("postset").split(" ");
		String[] postset2 = controlFlow.get(trans1).getProperty("postset").split(" ");
		if (!samePostset) {
			for (String s : postset2) {
				boolean unique = true;
				for (String t : postset1) {
					if (t.equals(s)) {
						unique = false;
					}
				}
				if (unique) {
					combinePlaces(s, postset1[0]);
				}
			}
		}
		else if (!samePreset) {
			for (String s : preset2) {
				boolean unique = true;
				for (String t : preset1) {
					if (t.equals(s)) {
						unique = false;
					}
				}
				if (unique) {
					combinePlaces(s, preset1[0]);
				}
			}
		}
		for (String s : controlPlaces.keySet()) {
			Properties prop = controlPlaces.get(s);
			String[] array = prop.getProperty("preset").split(" ");
			String setString = new String();
			if (array[0].equals(trans2)) {
				setString = trans1;
			}
			else {
				setString = array[0];
			}
			for (int i = 1; i < array.length; i++) {
				if (array[i].equals(trans2)) {
					array[i] = trans1;
				}
				setString = setString + " " + array[i];
			}
			prop.setProperty("preset", setString);
			array = prop.getProperty("postset").split(" ");
			setString = new String();
			if (array[0].equals(trans2)) {
				setString = trans1;
			}
			else {
				setString = array[0];
			}
			for (int i = 1; i < array.length; i++) {
				if (array[i].equals(trans2)) {
					array[i] = trans1;
				}
				setString = setString + " " + array[i];
			}
			prop.setProperty("postset", setString);
			controlPlaces.put(s, prop);
		}
		// Combine assignments
		Properties oldProp = contAssignments.get(trans2);
		Properties newProp = contAssignments.get(trans1);
		for (Object o : oldProp.keySet()) {
			if (!newProp.containsKey(o)) {
				newProp.setProperty(o.toString(), oldProp.getProperty(o.toString()));
			}
		}
		contAssignments.put(trans1, newProp);
		oldProp = booleanAssignments.get(trans2);
		newProp = booleanAssignments.get(trans1);
		for (Object o : oldProp.keySet()) {
			if (!newProp.containsKey(o)) {
				newProp.setProperty(o.toString(), oldProp.getProperty(o.toString()));
			}
		}
		booleanAssignments.put(trans1, newProp);
		oldProp = intAssignments.get(trans2);
		newProp = intAssignments.get(trans1);
		for (Object o : oldProp.keySet()) {
			if (!newProp.containsKey(o)) {
				newProp.setProperty(o.toString(), oldProp.getProperty(o.toString()));
			}
		}
		intAssignments.put(trans1, newProp);
		HashMap<String, ExprTree[]> oldMap = contAssignmentTrees.get(trans2);
		HashMap<String, ExprTree[]> newMap = contAssignmentTrees.get(trans1);
		for (Object o : oldMap.keySet()) {
			if (!newMap.containsKey(o)) {
				newMap.put(o.toString(), oldMap.get(o.toString()));
			}
		}
		contAssignmentTrees.put(trans1, newMap);
		oldMap = booleanAssignmentTrees.get(trans2);
		newMap = booleanAssignmentTrees.get(trans1);
		for (Object o : oldMap.keySet()) {
			if (!newMap.containsKey(o)) {
				newMap.put(o.toString(), oldMap.get(o.toString()));
			}
		}
		booleanAssignmentTrees.put(trans1, newMap);
		oldMap = intAssignmentTrees.get(trans2);
		newMap = intAssignmentTrees.get(trans1);
		for (Object o : oldMap.keySet()) {
			if (!newMap.containsKey(o)) {
				newMap.put(o.toString(), oldMap.get(o.toString()));
			}
		}
		intAssignmentTrees.put(trans1, newMap);
		removeTransition(trans2);
	}

	public void addPlaces(HashMap<String, Boolean> newPlaces) {
		places.putAll(newPlaces);
	}

	public void addInputs(HashMap<String, String> newInputs) {
		for (String s : newInputs.keySet()) {
			inputs.put(s, newInputs.get(s));
		}
	}

	public void addOutputs(HashMap<String, String> newOutputs) {
		for (String s : newOutputs.keySet()) {
			outputs.put(s, newOutputs.get(s));
		}
	}

	public void addEnablings(HashMap<String, String> newEnablings) {
		for (String s : newEnablings.keySet()) {
			enablings.put(s, newEnablings.get(s));
		}
	}

	public void addDelays(HashMap<String, String> newDelays) {
		for (String s : newDelays.keySet()) {
			delays.put(s, newDelays.get(s));
		}
	}

	public void addDelayTrees(HashMap<String, ExprTree[]> newRates) {
		for (String s : newRates.keySet()) {
			delayTrees.put(s, newRates.get(s));
		}
	}

	public void addRateTrees(HashMap<String, ExprTree> newRates) {
		for (String s : newRates.keySet()) {
			transitionRates.put(s, newRates.get(s));
		}
	}

	public void addRates(HashMap<String, String> newRates) {
		for (String s : newRates.keySet()) {
			transitionRateStrings.put(s, newRates.get(s));
		}
	}

	public void addBooleanAssignments(HashMap<String, Properties> newAssign) {
		for (String s : newAssign.keySet()) {
			Properties prop = new Properties();
			Properties oldProp = newAssign.get(s);
			for (Object o : oldProp.keySet()) {
				String t = o.toString();
				prop.setProperty(t, oldProp.getProperty(t));
			}
			booleanAssignments.put(s, prop);
		}
	}

	public void addBooleanAssignmentTrees(HashMap<String, HashMap<String, ExprTree[]>> newAssignment) {
		for (String s : newAssignment.keySet()) {
			HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
			HashMap<String, ExprTree[]> oldMap = newAssignment.get(s);
			for (Object o : oldMap.keySet()) {
				String t = o.toString();
				map.put(t, oldMap.get(t));
			}
			booleanAssignmentTrees.put(s, map);
		}
	}

	public void addMovements(HashMap<String, Properties> newMovement) {
		for (String s : newMovement.keySet()) {
			Properties prop = new Properties();
			Properties oldProp = newMovement.get(s);
			for (Object o : oldProp.keySet()) {
				String t = o.toString();
				prop.setProperty(t, oldProp.getProperty(t));
			}
			// System.out.println(s + prop.toString());
			controlFlow.put(s, prop);
		}
	}

	public void addPlaceMovements(HashMap<String, Properties> newMovement) {
		for (String s : newMovement.keySet()) {
			Properties prop = new Properties();
			Properties oldProp = newMovement.get(s);
			for (Object o : oldProp.keySet()) {
				String t = o.toString();
				prop.setProperty(t, oldProp.getProperty(t));
			}
			controlPlaces.put(s, prop);
		}
	}

	public void addVariables(HashMap<String, Properties> newVariable) {
		for (String s : newVariable.keySet()) {
			Properties prop = new Properties();
			Properties oldProp = newVariable.get(s);
			for (Object o : oldProp.keySet()) {
				String t = o.toString();
				prop.setProperty(t, oldProp.getProperty(t));
			}
			variables.put(s, prop);
		}
	}

	public void addIntegers(HashMap<String, String> newInteger) {
		for (String s : newInteger.keySet()) {
			integers.put(s, newInteger.get(s));
		}
	}

	public void addRateAssignments(HashMap<String, Properties> newAssignment) {
		for (String s : newAssignment.keySet()) {
			Properties prop = new Properties();
			Properties oldProp = newAssignment.get(s);
			for (Object o : oldProp.keySet()) {
				String t = o.toString();
				prop.setProperty(t, oldProp.getProperty(t));
			}
			rateAssignments.put(s, prop);
		}
	}

	public void addRateAssignmentTrees(HashMap<String, HashMap<String, ExprTree[]>> newAssignment) {
		for (String s : newAssignment.keySet()) {
			HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
			HashMap<String, ExprTree[]> oldMap = newAssignment.get(s);
			for (Object o : oldMap.keySet()) {
				String t = o.toString();
				map.put(t, oldMap.get(t));
			}
			rateAssignmentTrees.put(s, map);
		}
	}

	public void addContinuousAssignments(HashMap<String, Properties> newAssignment) {
		for (String s : newAssignment.keySet()) {
			Properties prop = new Properties();
			Properties oldProp = newAssignment.get(s);
			for (Object o : oldProp.keySet()) {
				String t = o.toString();
				prop.setProperty(t, oldProp.getProperty(t));
			}
			contAssignments.put(s, prop);
		}
	}

	public void addContinuousAssignmentTrees(
			HashMap<String, HashMap<String, ExprTree[]>> newAssignment) {
		for (String s : newAssignment.keySet()) {
			HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
			HashMap<String, ExprTree[]> oldMap = newAssignment.get(s);
			for (Object o : oldMap.keySet()) {
				String t = o.toString();
				map.put(t, oldMap.get(t));
			}
			contAssignmentTrees.put(s, map);
		}
	}

	public void addIntegerAssignments(HashMap<String, Properties> newAssignment) {
		for (String s : newAssignment.keySet()) {
			Properties prop = new Properties();
			Properties oldProp = newAssignment.get(s);
			for (Object o : oldProp.keySet()) {
				String t = o.toString();
				prop.setProperty(t, oldProp.getProperty(t));
			}
			intAssignments.put(s, prop);
		}
	}

	public void addIntegerAssignmentTrees(HashMap<String, HashMap<String, ExprTree[]>> newAssignment) {
		for (String s : newAssignment.keySet()) {
			HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
			HashMap<String, ExprTree[]> oldMap = newAssignment.get(s);
			for (Object o : oldMap.keySet()) {
				String t = o.toString();
				map.put(t, oldMap.get(t));
			}
			intAssignmentTrees.put(s, map);
		}
	}

	public void addProperty(String prop) {
		property = prop;
	}

	public void removePlace(String name) {
		if (name != null && places.containsKey(name)) {
			places.remove(name);
			controlPlaces.remove(name);
		}
		for (String s : controlFlow.keySet()) {
			Properties prop = controlFlow.get(s);
			String[] array = prop.getProperty("preset").split(" ");
			String setString = new String();
			int offset = 0;
			for (int i = 1; i < array.length; i++) {
				if (array[i].equals(name)) {
					offset += 1;
				}
				setString = setString + " " + array[i - offset];
			}
			prop.setProperty("preset", setString);
			array = prop.getProperty("postset").split(" ");
			setString = new String();
			offset = 0;
			for (int i = 1; i < array.length; i++) {
				if (array[i].equals(name)) {
					offset += 1;
				}
				setString = setString + " " + array[i - offset];
			}
			prop.setProperty("postset", setString);
			controlFlow.put(s, prop);
		}
	}

	public void removeTransition(String name) {
		controlFlow.remove(name);
		delays.remove(name);
		delayTrees.remove(name);
		transitionRates.remove(name);
		transitionRateStrings.remove(name);
		rateAssignments.remove(name);
		rateAssignmentTrees.remove(name);
		booleanAssignments.remove(name);
		booleanAssignmentTrees.remove(name);
		enablings.remove(name);
		contAssignments.remove(name);
		contAssignmentTrees.remove(name);
		intAssignments.remove(name);
		intAssignmentTrees.remove(name);
	}

	public boolean isInput(String var) {
		return inputs.containsKey(var);
	}

	public boolean isOutput(String var) {
		return outputs.containsKey(var);
	}

	private static final String RANGE = "\\[(\\w+?),(\\w+?)\\]";
}