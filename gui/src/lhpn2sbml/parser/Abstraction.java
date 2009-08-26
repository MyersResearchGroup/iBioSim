package lhpn2sbml.parser;

//import java.io.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*; //import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biomodelsim.Log;

import verification.Verification;

public class Abstraction {

	private HashMap<String, Boolean> places;

	private HashMap<String, String> inputs;

	private HashMap<String, String> outputs;

	private HashMap<String, String> enablings;

	private HashMap<String, ExprTree> enablingTrees;

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

	private Verification verPane;

	public Abstraction(Log log, Verification pane) {
		this.log = log;
		this.verPane = pane;
		places = new HashMap<String, Boolean>();
		inputs = new HashMap<String, String>();
		outputs = new HashMap<String, String>();
		enablings = new HashMap<String, String>();
		enablingTrees = new HashMap<String, ExprTree>();
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
		boolean change = true;
		ArrayList<String> removeEnab = new ArrayList<String>();
		for (String s : enablings.keySet()) {
			if (enablings.get(s) == null) {
				removeEnab.add(s);
			}
			else if (enablings.get(s).equals("") || enablings.get(s).trim().equals("~shutdown") || enablings.get(s).equals("~fail")) {
				removeEnab.add(s);
			}
		}
		for (String s : removeEnab) {
			enablings.remove(s);
		}
		while (change) {
			change = false;
			// Transform 0 - Merge Parallel Places
			ArrayList<String[]> merge = new ArrayList<String[]>();
			for (String s : controlPlaces.keySet()) {
				for (String t : controlPlaces.keySet()) {
					if (!s.equals(t)) {
						Properties prop1 = controlPlaces.get(s);
						Properties prop2 = controlPlaces.get(t);
						boolean assign = false;
						for (HashMap<String, Properties> h : assignments) {
							if (h.get(t) == null || h.get(t).keySet().isEmpty()) {
								assign = true;
								break;
							}
						}
						// System.out.println(s + t);
						if (comparePreset(prop1, prop2) && comparePostset(prop1, prop2)
								&& (places.get(s).equals(places.get(t))) && !assign) {
							String[] temp = { s, t };
							merge.add(temp);
						}
					}
				}
			}
			for (String[] a : merge) {
				// System.out.println("Transform 0");
				change = true;
				combinePlaces(a[0], a[1]);
			}
			// Transform 1 - Remove a Place in a Self Loop
			ArrayList<String> remove = new ArrayList<String>();
			for (String s : controlPlaces.keySet()) {
				if (controlPlaces.get(s).getProperty("preset") != null
						&& controlPlaces.get(s).getProperty("postset") != null) {
					String[] preset = controlPlaces.get(s).getProperty("preset").split(" ");
					String[] postset = controlPlaces.get(s).getProperty("postset").split(" ");
					if (preset.length == 1 && postset.length == 1) {
						if (preset[0].equals(postset[0]) && !places.get(s)) {
							remove.add(s);
						}
						else {
							continue;
						}
					}
				}
			}
			for (String s : remove) {
				// System.out.println("Transform 1");
				change = true;
				removePlace(s);
			}
			// Transform 3 - Remove a Transition with a Single Place in the
			// Postset
			remove = new ArrayList<String>();
			for (String s : controlFlow.keySet()) {
				if (controlFlow.get(s).getProperty("postset") != null) {
					String[] postset = controlFlow.get(s).getProperty("postset").split(" ");
					if (postset.length == 1 && !postset[0].equals("")) {
						boolean assign = false;
						if (enablings.containsKey(s)) {
							assign = true;
							continue;
						}
						if (!assign) {
							for (HashMap<String, Properties> h : assignments) {
								// System.out.println(assignments);
								if (h.get(s) != null) {
									if (!h.get(s).keySet().isEmpty()) {
										assign = true;
										break;
									}
								}
							}
						}
						// log.addText(postset[0]);
						boolean post = true;
						String[] preset;
						if (controlFlow.get(s).containsKey("preset")) {
							preset = controlFlow.get(s).getProperty("preset").split(" ");
							for (String p : preset) {
								if (controlPlaces.get(p).getProperty("postset").split(" ").length != 1) {
									post = false;
								}
							}
						}
						if (controlPlaces.containsKey(postset[0])) {
							if (controlPlaces.get(postset[0]).containsKey("preset")) {
								preset = controlPlaces.get(postset[0]).getProperty("preset").split(
										" ");
								if ((preset.length == 1 || post) && !assign) {
									remove.add(s);
								}
							}
						}
					}
				}
			}
			for (String s : remove) {
				// if (s != null) {
				// System.out.println("[3]Removing transition: " + s);
				// }
				// System.out.println("s: " + s);
				// System.out.println("preset: " +
				// controlFlow.get(s).getProperty("preset"));
				// System.out.println("postset: " +
				// controlFlow.get(s).getProperty("postset"));
				if (controlFlow.get(s).containsKey("preset")
						&& controlFlow.get(s).containsKey("postset")) {
					// String[] preset =
					// controlFlow.get(s).getProperty("preset").split(" ");
					String[] postset = controlFlow.get(s).getProperty("postset").split(" ");
					if (postset.length == 1 && !postset[0].equals("")) {
						if (removeTrans3(s, controlFlow.get(s).getProperty("preset").split(" "),
								postset))
							change = true;
					}
				}
			}
			// Transform 4 - Remove a Transition with a Single Place in the
			// Preset
			remove = new ArrayList<String>();
			for (String s : controlFlow.keySet()) {
				if (controlFlow.get(s).getProperty("preset") != null) {
					String[] preset = controlFlow.get(s).getProperty("preset").split(" ");
					if (preset.length == 1 && !preset[0].equals("")) {
						boolean assign = false;
						if (enablings.containsKey(s)) {
							assign = true;
						}
						if (!assign) {
							for (HashMap<String, Properties> h : assignments) {
								if (h.get(s) != null) {
									if (!h.get(s).keySet().isEmpty()) {
										assign = true;
									}
								}
							}
						}
						// log.addText(preset[0]);
						boolean pre = true;
						if (controlFlow.get(s).containsKey("postset")
								&& !controlFlow.get(s).getProperty("postset").equals("")) {
							for (String p : controlFlow.get(s).getProperty("postset").split(" ")) {
								if (controlPlaces.get(p).getProperty("preset").split(" ").length != 1) {
									pre = false;
								}
							}
						}
						String[] postset = controlPlaces.get(preset[0]).getProperty("postset")
								.split(" ");
						if ((postset.length == 1 || pre) && places.containsKey(preset[0])) {
							if (!assign) {
								remove.add(s);
							}
						}
					}
				}
			}
			for (String s : remove) {
				if (controlFlow.get(s) != null) {
					// System.out.println("[4]Removing transition: " + s);
					if (controlFlow.get(s).getProperty("preset") != null
							&& controlFlow.get(s).getProperty("postset") != null
							&& !controlFlow.get(s).getProperty("postset").equals("")) {
						if (removeTrans4(s, controlFlow.get(s).getProperty("preset").split(" "),
								controlFlow.get(s).getProperty("postset").split(" ")))
							change = true;
					}
				}
			}
			// Transforms 5a, 6, 7 - Combine Transitions with the Same Preset
			// and/or Postset
			ArrayList<String[]> combine = new ArrayList<String[]>();
			HashMap<String, boolean[]> samesets = new HashMap<String, boolean[]>();
			for (String s : controlFlow.keySet()) {
				for (String t : controlFlow.keySet()) {
					if (!s.equals(t)) {
						boolean samePreset = comparePreset(controlFlow.get(s), controlFlow.get(t));
						boolean samePostset = comparePostset(controlFlow.get(s), controlFlow.get(t));
						boolean assign = false;
						if (enablings.containsKey(t)) {
							assign = true;
						}
						if (!assign) {
							for (HashMap<String, Properties> h : assignments) {
								if ((h.get(t) != null && !h.get(t).keySet().isEmpty()) || (h.get(s) != null && !h.get(s).keySet().isEmpty())) {
									assign = true;
								}
							}
						}
						if ((samePreset && samePostset) && !assign) {
							String[] array = { s, t };
							boolean[] same = { samePreset, samePostset };
							combine.add(array);
							samesets.put(s, same);
						}
						else if (samePreset && controlFlow.get(s).containsKey("postset")
								&& controlFlow.get(t).containsKey("postset")
								&& controlFlow.get(s).containsKey("preset")
								&& controlFlow.get(t).containsKey("preset") && !assign) {
							String[] postset1 = controlFlow.get(s).getProperty("postset")
									.split(" ");
							String[] postset2 = controlFlow.get(t).getProperty("postset")
									.split(" ");
							if (postset1.length == 1 && postset2.length == 1 && controlPlaces.containsKey(postset1[0]) && controlPlaces.containsKey(postset2[0])) {
								if (comparePreset(controlPlaces.get(postset1[0]), controlPlaces
										.get(postset2[0]), s, t)) {
									String[] array = { s, t };
									boolean[] same = { samePreset, samePostset };
									combine.add(array);
									samesets.put(s, same);
								}
							}
						}
						else if (samePostset && controlFlow.get(s).containsKey("preset")
								&& controlFlow.get(t).containsKey("preset")
								&& controlFlow.get(s).containsKey("postset")
								&& controlFlow.get(t).containsKey("postset") && !assign) {
							String[] preset1 = controlFlow.get(s).getProperty("preset").split(" ");
							String[] preset2 = controlFlow.get(t).getProperty("preset").split(" ");
							if (preset1.length == 1 && preset2.length == 1) {
								if (comparePostset(controlPlaces.get(preset1[0]), controlPlaces
										.get(preset2[0]), s, t)) {
									String[] array = { s, t };
									boolean[] same = { samePreset, samePostset };
									combine.add(array);
									samesets.put(s, same);
								}
							}
						}
					}
				}
			}
			for (String[] s : combine) {
				// System.out.println("[5]Removing transition: " + s[1] + s[0]);
				change = true;
				combineTransitions(s[0], s[1], samesets.get(s[0])[0], samesets.get(s[0])[1]);
			}
			// Transform 5b
			combine = new ArrayList<String[]>();
			for (String s : controlFlow.keySet()) {
				for (String t : controlFlow.keySet()) {
					boolean transform = true;
					if (!s.equals(t)) {
						if (controlFlow.get(s).getProperty("preset") != null
								&& controlFlow.get(t).getProperty("preset") != null) {
							String[] preset1 = controlFlow.get(s).getProperty("preset").split(" ");
							String[] preset2 = controlFlow.get(t).getProperty("preset").split(" ");
							boolean assign = false;
							if (enablings.containsKey(t)) {
								assign = true;
							}
							if (!assign) {
								for (HashMap<String, Properties> h : assignments) {
									if (h.get(t) != null && h.get(s) != null) {
										if (!h.get(t).keySet().isEmpty() || !h.get(s).keySet().isEmpty()) {
											assign = true;
										}
									}
								}
							}
							if (!assign) {
								for (String u : preset1) {
									if (transform) {
										for (String v : preset2) {
											if (!u.equals(v)) {
												Properties prop1 = controlPlaces.get(u);
												Properties prop2 = controlPlaces.get(v);
												if (prop1 != null && prop2 != null) {
													if (!comparePreset(prop1, prop2)) {
														transform = false;
														break;
													}
												}
											}
										}
									}
								}
								if (transform && controlFlow.get(s).containsKey("postset")
										&& controlFlow.get(t).containsKey("postset")) {
									String[] postset1 = controlFlow.get(s).getProperty("postset")
											.split(" ");
									String[] postset2 = controlFlow.get(t).getProperty("postset")
											.split(" ");
									for (String u : postset1) {
										for (String v : postset2) {
											if (!u.equals(v)) {
												Properties prop1 = controlPlaces.get(u);
												Properties prop2 = controlPlaces.get(v);
												if (!comparePostset(prop1, prop2)) {
													transform = false;
													break;
												}
											}
										}
									}
								}
							}
							if (transform && !assign) {
								String[] array = { s, t };
								combine.add(array);
								// combineTransitions(s, t, true, true);
							}
						}
					}
				}
			}
			for (String[] s : combine) {
				// System.out.println("[5b]Removing transition: " + s[1] +
				// s[0]);
				if (controlFlow.containsKey(s[0]) && controlFlow.containsKey(s[1])) {
					if (controlFlow.get(s[0]).containsKey("preset")
							&& controlFlow.get(s[0]).containsKey("postset")
							&& controlFlow.get(s[1]).containsKey("preset")
							&& controlFlow.get(s[1]).containsKey("postset")) {
						change = true;
						combineTransitions(s[0], s[1], true, true);
					}
				}
			}
			if (!verPane.isSimplify()) {
				if (removeDeadPlaces()) {
					change = true;
				}
				if (removeDeadTransitions()) {
					change = true;
				}
			}
		}
	}

	public void abstractVars(String[] intVars) {
		ArrayList<String> interestingVars = getIntVars(intVars);
		ArrayList<String> vars = new ArrayList<String>();
		for (String s : variables.keySet()) {
			// boolean flag = false;
			// for (int j = 0; j < intVars.length; j++) {
			// if (s.equals(intVars[j])) {
			// flag = true;
			// }
			// }
			if (!interestingVars.contains(s)) {
				vars.add(s);
			}
		}
		for (String s : inputs.keySet()) {
			// boolean flag = false;
			// for (int j = 0; j < intVars.length; j++) {
			// if (s.equals(intVars[j])) {
			// flag = true;
			// }
			// }
			if (!interestingVars.contains(s)) {
				vars.add(s);
			}
		}
		for (String s : outputs.keySet()) {
			// boolean flag = false;
			// for (int j = 0; j < intVars.length; j++) {
			// if (s.equals(intVars[j])) {
			// flag = true;
			// }
			// }
			if (!interestingVars.contains(s)) {
				vars.add(s);
			}
		}
		for (String s : integers.keySet()) {
			// boolean flag = false;
			// for (int j = 0; j < intVars.length; j++) {
			// if (s.equals(intVars[j])) {
			// flag = true;
			// }
			// }
			if (!interestingVars.contains(s)) {
				vars.add(s);
			}
		}
		if (vars != null) {
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
				// for (String t : enablings.keySet()) {
				// if (t != null) {
				// if (enablings.get(t).contains(s)) {
				// enablings.put(s, enablings.get(t).replace(s, "MAYBE"));
				// }
				// }
				// }
				if (inputs.containsKey(s)) {
					inputs.put(s, "unknown");
				}
				else if (outputs.containsKey(s)) {
					outputs.put(s, "unknown");
				}
				else if (variables.containsKey(s)) {
					Properties prop = new Properties();
					prop.setProperty("value", "[-INF,INF]");
					prop.setProperty("rate", "[-INF,INF]");
					variables.put(s, prop);
				}
				else if (integers.containsKey(s)) {
					integers.put(s, "[-INF,INF]");
				}
				// for (String t : enablingTrees.keySet()) {
				// ExprTree expr = enablingTrees.get(t);
				// log.addText("here");
				// ExprTree here = expr;
				// }
			}
		}
	}

	public void abstractAssign() {
		for (String s : controlFlow.keySet()) {
			String[] postset = controlFlow.get(s).getProperty("postset").split(" ");
			for (String t : postset) {
				String[] postTrans = controlPlaces.get(t).getProperty("postset").split(" ");
				for (String u : postTrans) {
					boolean flag = true;
					for (Object o : contAssignments.get(u).keySet()) {
						String key = o.toString();
						if (!contAssignments.get(s).keySet().contains(key)
								|| (contAssignmentTrees.get(u).get(key)[0].isit != 'c')) {
							flag = false;
						}
					}
					for (Object o : intAssignments.get(u).keySet()) {
						String key = o.toString();
						if (!intAssignments.get(s).keySet().contains(key)
								|| (intAssignmentTrees.get(u).get(key)[0].isit != 'i')) {
							flag = false;
						}
					}
					for (Object o : booleanAssignments.get(u).keySet()) {
						String key = o.toString();
						if (!booleanAssignments.get(s).keySet().contains(key)
								|| (booleanAssignmentTrees.get(u).get(key)[0].isit != 't')) {
							flag = false;
						}
					}
					if (flag) {
						for (Object o : contAssignments.get(u).keySet()) {
							String[] assign = {
									contAssignments.get(u).get(o.toString()).toString(),
									contAssignments.get(u).get(o.toString()).toString() };
							String[][] assignRange = new String[2][2];
							Pattern pattern = Pattern.compile("\\[(\\S+?),(\\S+?)\\]");
							for (int i = 0; i < assign.length; i++) {
								Matcher matcher = pattern.matcher(assign[i]);
								if (matcher.find()) {
									assignRange[i][0] = matcher.group(1);
									assignRange[i][1] = matcher.group(2);
								}
								else {
									assignRange[i][0] = assign[i];
									assignRange[i][1] = assign[i];
								}
							}
							if (assignRange[0][0].equals("inf")) {
								assign[0] = assignRange[1][0];
							}
							else if (assignRange[1][0].equals("inf")) {
								assign[0] = assignRange[0][0];
							}
							else if (Float.parseFloat(assignRange[0][0]) < Float
									.parseFloat(assignRange[1][0])) {
								assign[0] = assignRange[0][0];
							}
							else {
								assign[0] = assignRange[1][0];
							}
							if (assignRange[0][1].equals("inf") || assignRange[1][1].equals("inf")) {
								assign[1] = "inf";
							}
							else if (Float.parseFloat(assignRange[0][1]) > Float
									.parseFloat(assignRange[1][1])) {
								assign[1] = assignRange[0][1];
							}
							else {
								assign[1] = assignRange[1][1];
							}
							if (assign[0].equals(assign[1])) {
								addContAssign(s, o.toString(), assign[0]);
							}
							else {
								addContAssign(s, o.toString(), "[" + assign[0] + "," + assign[1]
										+ "]");
							}
						}
						for (Object o : intAssignments.get(u).keySet()) {
							String[] assign = { intAssignments.get(u).get(o.toString()).toString(),
									intAssignments.get(u).get(o.toString()).toString() };
							String[][] assignRange = new String[2][2];
							Pattern pattern = Pattern.compile("\\[(\\S+?),(\\S+?)\\]");
							for (int i = 0; i < assign.length; i++) {
								Matcher matcher = pattern.matcher(assign[i]);
								if (matcher.find()) {
									assignRange[i][0] = matcher.group(1);
									assignRange[i][1] = matcher.group(2);
								}
								else {
									assignRange[i][0] = assign[i];
									assignRange[i][1] = assign[i];
								}
							}
							if (assignRange[0][0].equals("inf")) {
								assign[0] = assignRange[1][0];
							}
							else if (assignRange[1][0].equals("inf")) {
								assign[0] = assignRange[0][0];
							}
							else if (Integer.parseInt(assignRange[0][0]) < Integer
									.parseInt(assignRange[1][0])) {
								assign[0] = assignRange[0][0];
							}
							else {
								assign[0] = assignRange[1][0];
							}
							if (assignRange[0][1].equals("inf") || assignRange[1][1].equals("inf")) {
								assign[1] = "inf";
							}
							else if (Integer.parseInt(assignRange[0][1]) > Integer
									.parseInt(assignRange[1][1])) {
								assign[1] = assignRange[0][1];
							}
							else {
								assign[1] = assignRange[1][1];
							}
							if (assign[0].equals(assign[1])) {
								addIntAssign(s, o.toString(), assign[0]);
							}
							else {
								addIntAssign(s, o.toString(), "[" + assign[0] + "," + assign[1]
										+ "]");
							}
						}
						for (Object o : booleanAssignments.get(u).keySet()) {
							String[] assign = {
									booleanAssignments.get(u).get(o.toString()).toString(),
									booleanAssignments.get(u).get(o.toString()).toString() };
							String[][] assignRange = new String[2][2];
							Pattern pattern = Pattern.compile("\\[(\\S+?),(\\S+?)\\]");
							for (int i = 0; i < assign.length; i++) {
								Matcher matcher = pattern.matcher(assign[i]);
								if (matcher.find()) {
									assignRange[i][0] = matcher.group(1);
									assignRange[i][1] = matcher.group(2);
								}
								else {
									assignRange[i][0] = assign[i];
									assignRange[i][1] = assign[i];
								}
							}
							if (assignRange[0][0].equals("false")
									|| assignRange[1][0].equals("false")) {
								assign[0] = "false";
							}
							else {
								assign[0] = "true";
							}
							if (assignRange[0][1].equals("true")
									|| assignRange[1][1].equals("true")) {
								assign[1] = "true";
							}
							else {
								assign[1] = "false";
							}
							if (assign[0].equals(assign[1])) {
								addBoolAssign(s, o.toString(), assign[0]);
							}
							else {
								addBoolAssign(s, o.toString(), "[" + assign[0] + "," + assign[1]
										+ "]");
							}
						}
					}
				}
			}
		}
	}

	private boolean removeDeadPlaces() {
		boolean change = false;
		ArrayList<String> removePlace = new ArrayList<String>();
		for (String s : places.keySet()) {
			if ((!controlPlaces.get(s).containsKey("preset")
					|| controlPlaces.get(s).getProperty("preset") == null || controlPlaces.get(s)
					.getProperty("preset").equals(""))
					&& !places.get(s)) {
				String[] postset = controlPlaces.get(s).getProperty("postset").split(" ");
				for (String t : postset) {
					for (String v : controlPlaces.keySet()) {
						Properties prop = controlPlaces.get(v);
						if (prop.containsKey("preset")) {
							if (prop.getProperty("preset").contains(t)) {
								String[] preset = prop.getProperty("preset").split(" ");
								String temp = "";
								for (String u : preset) {
									if (!u.equals(t)) {
										temp = temp + u + " ";
									}
								}
								prop.setProperty("preset", temp);
							}
						}
						if (prop.containsKey("postset")) {
							if (prop.getProperty("postset").contains(t)) {
								String[] tempPostset = prop.getProperty("postset").split(" ");
								String temp = "";
								for (String u : tempPostset) {
									if (!u.equals(t)) {
										temp = temp + u + " ";
									}
								}
								prop.setProperty("postset", temp);
							}
						}
						controlPlaces.put(v, prop);
					}
					removeTransition(t);
				}
				removePlace.add(s);
				change = true;
				continue;
			}
			if (!controlPlaces.get(s).containsKey("preset")
					&& !controlPlaces.get(s).containsKey("postset")) {
				removePlace.add(s);
			}
			if (!change) {
				if (places.get(s))
					continue;
				if (hasMarkedPreset(s))
					continue;
				for (String t : controlPlaces.get(s).getProperty("postset").split(" ")) {
					for (String v : controlPlaces.keySet()) {
						Properties prop = controlPlaces.get(v);
						if (prop.getProperty("preset").contains(t)) {
							String[] preset = prop.getProperty("preset").split(" ");
							String temp = "";
							for (String u : preset) {
								if (!u.equals(t)) {
									temp = temp + u + " ";
								}
							}
							prop.setProperty("preset", temp);
						}
						if (prop.getProperty("postset").contains(t)) {
							String[] postset = prop.getProperty("postset").split(" ");
							String temp = "";
							for (String u : postset) {
								if (!u.equals(t)) {
									temp = temp + u + " ";
								}
							}
							prop.setProperty("postset", temp);
						}
						controlPlaces.put(v, prop);
					}
					removeTransition(t);
				}
				change = true;
			}
		}
		for (String s : removePlace) {
			removePlace(s);
		}
		return change;
	}

	private boolean removeDeadTransitions() {
		boolean change = false;
		HashMap<String, String> initVars = new HashMap<String, String>();
		for (String s : variables.keySet()) {
			initVars.put(s, variables.get(s).getProperty("value"));
		}
		initVars.putAll(integers);
		initVars.putAll(inputs);
		initVars.putAll(outputs);
		ArrayList<String> removeTrans = new ArrayList<String>();
		ArrayList<String> removeEnab = new ArrayList<String>();
		for (String t : enablingTrees.keySet()) {
			ExprTree expr = enablingTrees.get(t);
			if (expr == null) {
				removeEnab.add(t);
				continue;
			}
			if (expr.containsCont())
				continue;
			if (expr.evaluateExp(initVars) == 1) {
				boolean enabled = true;
				for (String trans : booleanAssignments.keySet()) {
					if (trans.equals(t) || expr.becomesFalse(booleanAssignments.get(trans))) {
						enabled = false;
						break;
					}
				}
				if (enabled) {
					removeEnab.add(t);
				}
			}
			else if (expr.evaluateExp(initVars) == 0) {
				boolean disabled = true;
				for (String trans : booleanAssignments.keySet()) {
					if (trans.equals(t) || expr.becomesTrue(booleanAssignments.get(trans))) {
						disabled = false;
						break;
					}
				}
				if (disabled) {
					for (String s : controlPlaces.keySet()) {
						Properties prop = controlPlaces.get(s);
						if (prop.getProperty("preset").contains(t)) {
							String[] preset = prop.getProperty("preset").split(" ");
							String temp = "";
							for (String u : preset) {
								if (!u.equals(t)) {
									temp = temp + u + " ";
								}
							}
							prop.setProperty("preset", temp);
						}
						if (prop.getProperty("postset").contains(t)) {
							String[] postset = prop.getProperty("postset").split(" ");
							String temp = "";
							for (String u : postset) {
								if (!u.equals(t)) {
									temp = temp + u + " ";
								}
							}
							prop.setProperty("postset", temp);
						}
						controlPlaces.put(s, prop);
					}
					removeTrans.add(t);
				}
			}
		}
		for (String t : removeEnab) {
			enablings.remove(t);
			enablingTrees.remove(t);
		}
		for (String t : removeTrans) {
			removeTransition(t);
		}
		return change;
	}

	private boolean hasMarkedPreset(String place) {
		if (controlPlaces.get(place).containsKey("preset")) {
			for (String t : controlPlaces.get(place).getProperty("preset").split(" ")) {
				if (controlFlow.containsKey(t)) {
				if (controlFlow.get(t).containsKey("preset")) {
					for (String p : controlFlow.get(t).getProperty("preset").split(" ")) {
						if (places.get(p))
							return true;
						else if (p.equals(place)) return false;
						else if (hasMarkedPreset(p))
							return true;
					}
				}
				}
			}
		}
		return false;
	}

	private ArrayList<String> getIntVars(String[] oldIntVars) {
		ArrayList<String> intVars = new ArrayList<String>();
		intVars.add("fail");
		for (String s : oldIntVars) {
			intVars.add(s);
		}
		HashMap<String, HashMap<String, ExprTree[]>> assignments = new HashMap<String, HashMap<String, ExprTree[]>>();
		for (String s : contAssignmentTrees.keySet()) {
			assignments.put(s, contAssignmentTrees.get(s));
		}
		for (String s : intAssignmentTrees.keySet()) {
			assignments.put(s, intAssignmentTrees.get(s));
		}
		for (String s : booleanAssignmentTrees.keySet()) {
			assignments.put(s, booleanAssignmentTrees.get(s));
		}
		for (String s : rateAssignmentTrees.keySet()) {
			assignments.put(s, rateAssignmentTrees.get(s));
		}
		ArrayList<String> tempIntVars = new ArrayList<String>();
		tempIntVars = intVars;
		do {
			intVars = tempIntVars;
			for (String var : intVars) {
				for (HashMap<String, ExprTree[]> h : contAssignmentTrees.values()) {
					for (String s : h.keySet()) {
						for (ExprTree e : h.get(s)) {
							if (e.containsVar(var)) {
								tempIntVars.add(var);
							}
						}
					}
				}
				for (HashMap<String, ExprTree[]> h : intAssignmentTrees.values()) {
					for (String s : h.keySet()) {
						for (ExprTree e : h.get(s)) {
							if (e.containsVar(var)) {
								tempIntVars.add(var);
							}
						}
					}
				}
				for (HashMap<String, ExprTree[]> h : booleanAssignmentTrees.values()) {
					for (String s : h.keySet()) {
						for (ExprTree e : h.get(s)) {
							if (e.containsVar(var)) {
								tempIntVars.add(var);
							}
						}
					}
				}
				for (HashMap<String, ExprTree[]> h : rateAssignmentTrees.values()) {
					for (String s : h.keySet()) {
						for (ExprTree e : h.get(s)) {
							if (e.containsVar(var)) {
								tempIntVars.add(var);
							}
						}
					}
				}
			}
		}
		while (!intVars.equals(tempIntVars));
		tempIntVars = intVars;
		do {
			intVars = tempIntVars;
			for (String var : intVars) {
				ArrayList<String> process = new ArrayList<String>();
				for (String s : contAssignmentTrees.keySet()) {
					if (contAssignmentTrees.get(s).keySet().contains(s)) {
						process.add(s);
					}
				}
				for (String s : intAssignmentTrees.keySet()) {
					if (intAssignmentTrees.get(s).keySet().contains(s)) {
						process.add(s);
					}
				}
				for (String s : booleanAssignmentTrees.keySet()) {
					if (booleanAssignmentTrees.get(s).keySet().contains(s)) {
						process.add(s);
					}
				}
				for (String s : rateAssignmentTrees.keySet()) {
					if (rateAssignmentTrees.get(s).keySet().contains(s)) {
						process.add(s);
					}
				}
				ArrayList<String> tempProcess = new ArrayList<String>();
				tempProcess = process;
				do {
					process = tempProcess;
					for (String s : process) {
						for (String t : controlFlow.get(s).getProperty("postset").split(" ")) {
							for (String u : controlPlaces.get(t).getProperty("postset").split(" ")) {
								if (!tempProcess.contains(u)) {
									tempProcess.add(u);
								}
							}
						}
					}
				}
				while (!tempProcess.equals(process));
				for (String trans : process) {
					ArrayList<String> tempVars = enablingTrees.get(trans).getVars();
					for (String s : tempVars) {
						if (!tempIntVars.contains(s))
							tempIntVars.add(s);
					}
				}
			}
		}
		while (!intVars.equals(tempIntVars));
		return intVars;
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
		if (flow1.getProperty("preset") != null && flow2.getProperty("preset") != null) {
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
		return false;
	}

	private boolean comparePreset(Properties flow1, Properties flow2, String trans1, String trans2) {
		if (flow1.getProperty("preset") != null && flow2.getProperty("preset") != null) {
			String[] set1 = flow1.get("preset").toString().split(" ");
			String[] set2 = flow2.get("preset").toString().split(" ");
			if (set1.length != set2.length) {
				return false;
			}
			boolean contains = false;
			for (int i = 0; i < set1.length; i++) {
				contains = false;
				for (int j = 0; j < set2.length; j++) {
					if (set1[i].equals(set2[j]) || set1[i].equals(trans1) || set1[i].equals(trans2)) {
						contains = true;
					}
				}
				if (!contains) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private boolean comparePostset(Properties flow1, Properties flow2) {
		if (flow1 == null || flow2 == null) {
			return false;
		}
		// System.out.println(flow1.get("postset"));
		// System.out.println(flow2.get("postset"));
		if (flow1.get("postset") != null && flow2.get("postset") != null) {
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
		return false;
	}

	private boolean comparePostset(Properties flow1, Properties flow2, String trans1, String trans2) {
		if (flow1 == null || flow2 == null) {
			return false;
		}
		// System.out.println(flow1.get("postset"));
		// System.out.println(flow2.get("postset"));
		if (flow1.get("postset") != null && flow2.get("postset") != null) {
			String[] set1 = flow1.get("postset").toString().split(" ");
			String[] set2 = flow2.get("postset").toString().split(" ");
			if (set1.length != set2.length) {
				return false;
			}
			boolean contains = false;
			for (int i = 0; i < set1.length; i++) {
				contains = false;
				for (int j = 0; j < set2.length; j++) {
					if (set1[i].equals(set2[j]) || set1[i].equals(trans1) || set1[i].equals(trans2)) {
						contains = true;
					}
				}
				if (!contains) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private void combinePlaces(String place1, String place2) {
		// System.out.println(place1 + place2);
		String newPlace = new String();
		newPlace = place1;
		for (String s : controlFlow.keySet()) {
			Properties prop = controlFlow.get(s);
			if (prop.containsKey("preset")) {
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
			}
			if (prop.containsKey("postset")) {
				String[] array = prop.getProperty("postset").split(" ");
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
				prop.setProperty("postset", setString);
			}
			controlFlow.put(s, prop);
		}
		String[] preset1;
		if (controlPlaces.get(place1).containsKey("preset")) {
			preset1 = controlPlaces.get(place1).getProperty("preset").split(" ");
		}
		else {
			preset1 = new String[0];
		}
		String[] preset2;
		if (controlPlaces.get(place2).containsKey("preset")) {
			preset2 = controlPlaces.get(place2).getProperty("preset").split(" ");
		}
		else {
			preset2 = new String[0];
		}
		String[] postset1;
		if (controlPlaces.get(place1).containsKey("preset")) {
			postset1 = controlPlaces.get(place1).getProperty("preset").split(" ");
		}
		else {
			postset1 = new String[0];
		}
		String[] postset2;
		if (controlPlaces.get(place2).containsKey("preset")) {
			postset2 = controlPlaces.get(place2).getProperty("preset").split(" ");
		}
		else {
			postset2 = new String[0];
		}
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

	private boolean removeTrans3(String transition, String[] preset, String[] postset) {
		String place = postset[0];
		boolean marked = false;
		if (places.containsKey(place)) {
			marked = places.get(place);
		}
		// preset = controlFlow.get(transition).getProperty("preset").split("
		// ");
		if (controlPlaces.get(place).containsKey("postset")) {
			postset = controlPlaces.get(postset[0]).getProperty("postset").split(" ");
			if ((postset.length == 1 && postset[0].equals(transition))) {
				return false;
			}
		}
		else if (preset[0].equals("")) {
			return false;
		}
		else {
			postset = new String[0];
		}
		String[] placePreset = controlPlaces.get(place).getProperty("preset").split(" ");
		// boolean marked = places.get(place);
		// Combine control Flow
		for (String t : preset) {
			//String[] tempPostset = controlPlaces.get(t).getProperty("postset").split(" ");
			String tempList = "";
//			for (int i = 0; i < tempPostset.length; i++) {
//				if (!tempPostset[i].equals(transition) && !tempPostset[i].equals("")) {
//					tempList = tempList + tempPostset[i] + " ";
//					// if (marked) {
//					// places.put(tempPostset[i], true);
//					// }
//				}
//			}
			for (String p : placePreset) {
				if (!p.equals(transition) && !p.equals("")) {
					tempList = tempList + p + " ";
				Properties prop = controlFlow.get(p);
				String placePostset = prop.getProperty("postset");
				if (!placePostset.equals("")) {
					prop.setProperty("postset", placePostset + " " + t);
				}
				else {
					prop.setProperty("postset", t);
				}
				}
			}
			Properties prop = new Properties();
			prop = controlPlaces.get(t);
			prop.setProperty("preset", tempList.trim());
			tempList = "";
			for (String u : postset) {
				tempList = tempList + u + " ";
			}
			prop.setProperty("postset", tempList.trim());
			controlPlaces.put(t, prop);
			if (marked) {
				places.put(t, true);
			}
		}
		for (String t : postset) {
			String[] tempPreset = controlFlow.get(t).getProperty("preset").split(" ");
			String tempList = "";
			for (int i = 0; i < tempPreset.length; i++) {
				if (!tempPreset[i].equals(place) && !tempPreset[i].equals("")) {
					tempList = tempList + tempPreset[i] + " ";
					// if (marked) {
					// places.put(tempPreset[i], true);
					// }
				}
			}
			for (int i = 0; i < preset.length; i++) {
				tempList = tempList + preset[i] + " ";
			}
			Properties prop = controlFlow.get(t);
			prop.setProperty("preset", tempList.trim());
			controlFlow.put(t, prop);
		}
		removePlace(place);
		// Add delays
		String[] oldDelay = new String[2];
		Pattern rangePattern = Pattern.compile(RANGE);
		if (delays.containsKey(transition)) {
			Matcher rangeMatcher = rangePattern.matcher(delays.get(transition));
			if (rangeMatcher.find()) {
				oldDelay[0] = rangeMatcher.group(1);
				oldDelay[1] = rangeMatcher.group(2);
			}
		}
		else {
			oldDelay[0] = "0";
			oldDelay[1] = "inf";
		}
		// ArrayList<String> list = new ArrayList<String>();
		// for (String t : preset) {
		// if (controlPlaces.get(t).containsKey("postset")) {
		// for (String u :
		// controlPlaces.get(t).getProperty("postset").split(" ")) {
		// list.add(u);
		// }
		// }
		// }
		// Object[] postTrans = list.toArray();
		for (String t : postset) {
			// String t = o.toString();
			if (delays.get(t) != null) {
				Matcher newMatcher = rangePattern.matcher(delays.get(t));
				if (newMatcher.find()) {
					String newDelay[] = { newMatcher.group(1), newMatcher.group(2) };
					for (int i = 0; i < newDelay.length; i++) {
						if (!oldDelay[i].equals("inf") && !newDelay[i].equals("inf")) {
							if (i != 0 || !marked) {
								newDelay[i] = String.valueOf(Integer.parseInt(newDelay[i])
										+ Integer.parseInt(oldDelay[i]));
							}
						}
						else {
							newDelay[i] = "inf";
						}
					}
					delays.put(t, "[" + newDelay[0] + "," + newDelay[1] + "]");
				}
			}
		}
		removeTransition(transition);
		return true;
		// save("/home/shang/kjones/eclipse/temp/temp.lpn");
	}

	private boolean removeTrans4(String transition, String[] preset, String[] postset) {
		String place = preset[0];
		// postset = controlFlow.get(transition).getProperty("postset").split("
		// ");
		if (controlPlaces.get(preset[0]).containsKey("preset")) {
			preset = controlPlaces.get(preset[0]).getProperty("preset").split(" ");
			if (postset.length == 1) {
				String[] tempPostset = controlPlaces.get(postset[0]).getProperty("postset").split(
						" ");
				if (tempPostset.length == 1 && tempPostset[0].equals(transition)) {
					return false;
				}
			}
		}
		else if (postset[0].equals("")) {
			return false;
		}
		else {
			preset = new String[0];
		}
		boolean marked = places.get(place);
		String[] transPostset = controlPlaces.get(place).getProperty("postset").split(" ");
		// Combine control Flow
		for (String t : postset) {
			if (controlPlaces.get(t) != null) {
				String[] tempPreset = controlPlaces.get(t).getProperty("preset").split(" ");
				String tempList = "";
				for (int i = 0; i < tempPreset.length; i++) {
					if (!tempPreset[i].equals(transition)) {
						tempList = tempList + tempPreset[i] + " ";
						// if (marked) {
						// places.put(tempPreset[i], true);
						// }
					}
				}
				for (int i = 0; i < preset.length; i++) {
					tempList = tempList + preset[i] + " ";
				}
				Properties prop = controlPlaces.get(t);
				prop.setProperty("preset", tempList.trim());
				controlPlaces.put(t, prop);
			}
			if (marked) {
				places.put(t, true);
			}
		}
		for (String t : preset) {
			if (controlFlow.get(t) != null) {
				String[] tempPostset = controlFlow.get(t).getProperty("postset").split(" ");
				String tempList = "";
				for (int i = 0; i < tempPostset.length; i++) {
					if (!tempPostset[i].equals(place)) {
						tempList = tempList + tempPostset[i] + " ";
					}
				}
				for (int i = 0; i < postset.length; i++) {
					tempList = tempList + postset[i] + " ";
				}
				Properties prop = controlFlow.get(t);
				if (!tempList.equals("")) {
					prop.setProperty("postset", tempList.trim());
				}
				else {
					prop.remove("postset");
				}
				controlFlow.put(t, prop);
			}
		}
		for (String s : transPostset) {
			String presetString = "";
			Properties tempProp = controlFlow.get(s);
			for (String t : postset) {
				if (controlPlaces.get(t) != null) {
					String[] tempPreset = controlPlaces.get(t).getProperty("preset").split(" ");
					String tempList = "";
					for (int i = 0; i < tempPreset.length; i++) {
						if (!tempPreset[i].equals(transition)) {
							tempList = tempList + s + " ";
						}
					}
					Properties prop = controlPlaces.get(t);
					if (!tempList.equals("")) {
						prop.setProperty("preset", tempList.trim());
					}
					else {
						prop.remove("preset");
					}
					controlPlaces.put(t, prop);
				}
				presetString = presetString + t + " ";
			}
			tempProp.setProperty("preset", presetString);
			controlFlow.put(s, tempProp);
		}
		places.remove(place);
		controlPlaces.remove(place);
		// Add delays
		String[] oldDelay = new String[2];
		Pattern rangePattern = Pattern.compile(RANGE);
		Matcher rangeMatcher = rangePattern.matcher(delays.get(transition));
		if (rangeMatcher.find()) {
			oldDelay[0] = rangeMatcher.group(1);
			oldDelay[1] = rangeMatcher.group(2);
		}
		HashMap<String, Boolean> postTrans = new HashMap<String, Boolean>();
		for (String t : postset) {
			if (controlPlaces.containsKey(t)) {
				if (controlPlaces.get(t).containsKey("postset")) {
					for (String u : controlPlaces.get(t).getProperty("postset").split(" ")) {
						postTrans.put(u, places.get(t));
					}
				}
			}
		}
		for (Object o : postTrans.keySet()) {
			String t = o.toString();
			if (delays.get(t) != null) {
				Matcher newMatcher = rangePattern.matcher(delays.get(t));
				if (newMatcher.find()) {
					String newDelay[] = { newMatcher.group(1), newMatcher.group(2) };
					for (int i = 0; i < newDelay.length; i++) {
						if (!oldDelay[i].equals("inf") && !newDelay[i].equals("inf")) {
							if (i != 0 || !postTrans.get(t)) {
								newDelay[i] = String.valueOf(Integer.parseInt(newDelay[i])
										+ Integer.parseInt(oldDelay[i]));
							}
						}
						else {
							newDelay[i] = "inf";
						}
					}
					delays.put(t, "[" + newDelay[0] + "," + newDelay[1] + "]");
				}
			}
		}
		removeTransition(transition);
		return true;
	}

	private void combineTransitions(String trans1, String trans2, boolean samePreset,
			boolean samePostset) {
		if (controlFlow.get(trans1) == null || controlFlow.get(trans2) == null) {
			return;
		}
		String[] delay = { delays.get(trans1), delays.get(trans2) };
		String[][] delayRange = new String[2][2];
		Pattern pattern = Pattern.compile("\\[(\\S+?),(\\S+?)\\]");
		for (int i = 0; i < delay.length; i++) {
			Matcher matcher = pattern.matcher(delay[i]);
			if (matcher.find()) {
				delayRange[i][0] = matcher.group(1);
				delayRange[i][1] = matcher.group(2);
			}
			else {
				delayRange[i][0] = delay[i];
				delayRange[i][1] = delay[i];
			}
		}
		if (delayRange[0][0].equals("inf")) {
			delay[0] = delayRange[1][0];
		}
		else if (delayRange[1][0].equals("inf")) {
			delay[0] = delayRange[0][0];
		}
		else if (Integer.parseInt(delayRange[0][0]) < Integer.parseInt(delayRange[1][0])) {
			delay[0] = delayRange[0][0];
		}
		else {
			delay[0] = delayRange[1][0];
		}
		if (delayRange[0][1].equals("inf") || delayRange[1][1].equals("inf")) {
			delay[1] = "inf";
		}
		else if (Integer.parseInt(delayRange[0][1]) > Integer.parseInt(delayRange[1][1])) {
			delay[1] = delayRange[0][1];
		}
		else {
			delay[1] = delayRange[1][1];
		}
		if (delay[0].equals(delay[1])) {
			delays.put(trans1, delay[0]);
		}
		else {
			delays.put(trans1, "[" + delay[0] + "," + delay[1] + "]");
		}
		// Combine Control Flow
		String[] preset1 = controlFlow.get(trans1).getProperty("preset").split(" ");
		String[] preset2 = controlFlow.get(trans2).getProperty("preset").split(" ");
		String[] postset1 = controlFlow.get(trans1).getProperty("postset").split(" ");
		String[] postset2 = controlFlow.get(trans2).getProperty("postset").split(" ");
		for (String s : controlPlaces.keySet()) {
			Properties prop = controlPlaces.get(s);
			if (prop.containsKey("preset")) {
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
			}
			if (prop.containsKey("postset")) {
				String[] array = prop.getProperty("postset").split(" ");
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
				prop.setProperty("postset", setString);
			}
			controlPlaces.put(s, prop);
		}
		Properties prop = new Properties();
		String preset = "";
		for (String s : preset1) {
			preset = preset + s + " ";
		}
		for (String s : preset2) {
			boolean flag = false;
			for (String t : preset1) {
				if (s.equals(t)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				preset = preset + s + " ";
			}
		}
		prop.setProperty("preset", preset.trim());
		String postset = "";
		for (String s : postset1) {
			postset = postset + s + " ";
		}
		for (String s : postset2) {
			boolean flag = false;
			for (String t : postset1) {
				if (s.equals(t)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				postset = postset + s + " ";
			}
		}
		prop.setProperty("postset", postset.trim());
		controlFlow.put(trans1, prop);
		removeTransition(trans2);
		if (!samePostset) {
			for (String s : postset2) {
				// boolean unique = true;
				// for (String t : postset1) {
				// if (t.equals(s)) {
				// unique = false;
				// }
				// }
				if (controlPlaces.containsKey(postset1[0])
						&& places.get(s) == places.get(postset1[0])) {
					combinePlaces(s, postset1[0]);
				}
			}
		}
		else if (!samePreset) {
			for (String s : preset2) {
				// boolean unique = true;
				// for (String t : preset1) {
				// if (t.equals(s)) {
				// unique = false;
				// }
				// }
				if (controlPlaces.containsKey(preset1[0])
						&& places.get(s) == places.get(preset1[0])) {
					combinePlaces(s, preset1[0]);
				}
			}
		}
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

	public void addEnablingTrees(HashMap<String, ExprTree> newEnablings) {
		for (String s : newEnablings.keySet()) {
			enablingTrees.put(s, newEnablings.get(s));
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
		// for (String s : newMovement.keySet()) {
		// Properties prop = new Properties();
		// Properties oldProp = newMovement.get(s);
		// for (Object o : oldProp.keySet()) {
		// String t = o.toString();
		// prop.setProperty(t, oldProp.getProperty(t));
		// }
		// controlPlaces.put(s, prop);
		// }
		controlPlaces = newMovement;
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
			if (prop.containsKey("preset")) {
				String[] array = prop.getProperty("preset").split(" ");
				String setString = new String();
				// int offset = 0;
				for (int i = 0; i < array.length; i++) {
					// if (array[i].equals(name)) {
					// offset += 1;
					// }
					if (!array[i].equals(name)) {
						setString = setString + array[i] + " ";
					}
				}
				prop.setProperty("preset", setString.trim());
			}
			if (prop.containsKey("postset")) {
				String[] array = prop.getProperty("postset").split(" ");
				String setString = new String();
				// offset = 0;
				for (int i = 0; i < array.length; i++) {
					// if (array[i].equals(name)) {
					// offset += 1;
					// }
					if (!array[i].equals(name)) {
						setString = setString + array[i] + " ";
					}
				}
				prop.setProperty("postset", setString.trim());
			}
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

	public String[] getBooleanVars() {
		Object[] inArray = inputs.keySet().toArray();
		Object[] outArray = outputs.keySet().toArray();
		String[] vars = new String[inArray.length + outArray.length];
		int i;
		for (i = 0; i < inArray.length; i++) {
			vars[i] = inArray[i].toString();
			// log.addText(vars[i]);
		}
		for (int j = 0; j < outArray.length; j++) {
			vars[i] = outArray[j].toString();
			// log.addText(vars[i]);
			i++;
		}
		return vars;
	}

	public String[] getContVars() {
		if (!variables.isEmpty()) {
			Object[] objArray = variables.keySet().toArray();
			String[] vars = new String[objArray.length];
			for (int i = 0; i < objArray.length; i++) {
				vars[i] = objArray[i].toString();
			}
			return vars;
		}
		else {
			return new String[0];
		}
	}

	public String[] getIntVars() {
		if (!integers.isEmpty()) {
			Object[] objArray = integers.keySet().toArray();
			String[] vars = new String[objArray.length];
			for (int i = 0; i < objArray.length; i++) {
				vars[i] = objArray[i].toString();
			}
			return vars;
		}
		else {
			return new String[0];
		}
	}

	public boolean addContAssign(String transition, String name, String value) {
		ExprTree[] expr = new ExprTree[2];
		expr[0] = new ExprTree(this);
		expr[1] = new ExprTree(this);
		Pattern pattern = Pattern.compile("\\[(\\w+?),(\\w+?)\\]");
		Matcher matcher = pattern.matcher(value);
		if (matcher.find()) {
			expr[0].token = expr[0].intexpr_gettok(matcher.group(1));
			if (!matcher.group(1).equals("")) {
				if (!expr[0].intexpr_L(matcher.group(1)))
					return false;
			}
			else {
				expr[0] = null;
			}
			expr[1].token = expr[1].intexpr_gettok(matcher.group(2));
			if (!matcher.group(2).equals("")) {
				if (!expr[1].intexpr_L(matcher.group(2)))
					return false;
			}
			else {
				expr[1] = null;
			}
		}
		else {
			expr[0].token = expr[0].intexpr_gettok(value);
			if (!value.equals("")) {
				if (!expr[0].intexpr_L(value))
					return false;
			}
			else {
				expr[0] = null;
				expr[1] = null;
			}
		}
		HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
		if (contAssignmentTrees.get(transition) != null) {
			map = contAssignmentTrees.get(transition);
		}
		map.put(name, expr);
		contAssignmentTrees.put(transition, map);
		Properties prop = new Properties();
		if (contAssignments.get(transition) != null) {
			prop = contAssignments.get(transition);
		}
		// System.out.println("here " + transition + name + value);
		prop.setProperty(name, value);
		// log.addText("lhpn " + prop.toString());
		contAssignments.put(transition, prop);
		return true;
	}

	public boolean addIntAssign(String transition, String name, String value) {
		ExprTree[] expr = new ExprTree[2];
		expr[0] = new ExprTree(this);
		expr[1] = new ExprTree(this);
		Pattern pattern = Pattern.compile("\\[(\\S+?),(\\S+?)\\]");
		Matcher matcher = pattern.matcher(value);
		if (matcher.find()) {
			expr[0].token = expr[0].intexpr_gettok(matcher.group(1));
			if (!matcher.group(1).equals("")) {
				if (!expr[0].intexpr_L(matcher.group(1)))
					return false;
			}
			else {
				expr[0] = null;
			}
			expr[1].token = expr[1].intexpr_gettok(matcher.group(2));
			if (!matcher.group(2).equals("")) {
				if (!expr[1].intexpr_L(matcher.group(2)))
					return false;
			}
			else {
				expr[1] = null;
			}
		}
		else {
			expr[0].token = expr[0].intexpr_gettok(value);
			if (!value.equals("")) {
				if (!expr[0].intexpr_L(value))
					return false;
			}
			else {
				expr[0] = null;
				expr[1] = null;
			}
		}
		HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
		if (intAssignmentTrees.get(transition) != null) {
			map = intAssignmentTrees.get(transition);
		}
		map.put(name, expr);
		intAssignmentTrees.put(transition, map);
		Properties prop = new Properties();
		if (intAssignments.get(transition) != null) {
			prop = intAssignments.get(transition);
		}
		// System.out.println("here " + transition + name + value);
		prop.setProperty(name, value);
		// log.addText("lhpn " + prop.toString());
		intAssignments.put(transition, prop);
		return true;
	}

	public boolean addBoolAssign(String transition, String name, String value) {
		boolean retval = false;
		Properties prop = new Properties();
		if (booleanAssignments.get(transition) != null) {
			prop = booleanAssignments.get(transition);
		}
		prop.setProperty(name, value);
		booleanAssignments.put(transition, prop);
		HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
		if (booleanAssignmentTrees.get(transition) != null) {
			map = booleanAssignmentTrees.get(transition);
		}
		ExprTree expr = new ExprTree(this);
		expr.token = expr.intexpr_gettok(value);
		if (!value.equals("")) {
			retval = expr.intexpr_L(value);
		}
		else {
			expr = null;
		}
		ExprTree[] array = { expr };
		map.put(name, array);
		booleanAssignmentTrees.put(transition, map);
		return retval;
	}

	public boolean isInput(String var) {
		return inputs.containsKey(var);
	}

	public boolean isOutput(String var) {
		return outputs.containsKey(var);
	}

	private static final String RANGE = "\\[(\\w+?),(\\w+?)\\]";
}