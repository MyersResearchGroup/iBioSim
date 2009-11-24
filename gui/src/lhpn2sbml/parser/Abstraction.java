package lhpn2sbml.parser;

//import java.io.*;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
import java.util.*; //import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biomodelsim.Log;

import verification.Verification;
import verification.AbstPane;

public class Abstraction extends LHPNFile {

	private ArrayList<HashMap<String, Properties>> assignments = new ArrayList<HashMap<String, Properties>>();

	private HashMap<String, Integer> process_trans = new HashMap<String, Integer>();

	private HashMap<String, Integer> process_write = new HashMap<String, Integer>();

	private HashMap<String, Integer> process_read = new HashMap<String, Integer>();

	// private Verification verPane;

	private AbstPane abstPane;

	public Abstraction(Log log, Verification pane) {
		super(log);
		this.abstPane = pane.getAbstPane();
	}

	public void abstractSTG() {
		boolean change = true;
		assignments.add(booleanAssignments);
		assignments.add(contAssignments);
		assignments.add(intAssignments);
		assignments.add(rateAssignments);
		divideProcesses();
		if (abstPane.absListModel.contains(abstPane.xform12)) {
			abstractAssign();
		}
		ArrayList<String> removeEnab = new ArrayList<String>();
		for (String s : enablings.keySet()) {
			if (enablings.get(s) == null) {
				removeEnab.add(s);
			}
			else if (enablings.get(s).equals("") || enablings.get(s).trim().equals("~shutdown")
					|| enablings.get(s).equals("~fail")) {
				removeEnab.add(s);
			}
		}
		for (String s : removeEnab) {
			enablings.remove(s);
		}
		while (change) {
			change = false;
			// Transform 0 - Merge Parallel Places
			if (abstPane.absListModel.contains(abstPane.xform0) && abstPane.isSimplify()) {
				change = checkTrans0(change);
			}
			// Transform 1 - Remove a Place in a Self Loop
			if (abstPane.absListModel.contains(abstPane.xform1) && abstPane.isSimplify()) {
				change = checkTrans1(change);
			}
			// Transform 10 - Simplify Expressions
			if (abstPane.absListModel.contains(abstPane.xform10) && abstPane.isSimplify()) {
				simplifyExpr();
			}
			// Transform 11 - Remove Unused Variables
			if (abstPane.absListModel.contains(abstPane.xform11) && abstPane.isSimplify()) {
				change = removeVars(change);
			}
			// Transforms 5a, 6, 7 - Combine Transitions with the Same Preset
			// and/or Postset
			if ((abstPane.absListModel.contains(abstPane.xform5)
					|| abstPane.absListModel.contains(abstPane.xform6) || abstPane.absListModel
					.contains(abstPane.xform7))
					&& abstPane.isAbstract()) {
				change = checkTrans5(change);
			}
			// Transform 5b
			if (abstPane.absListModel.contains(abstPane.xform5) && abstPane.isAbstract()) {
				change = checkTrans5b(change);
			}
			// Transform 3 - Remove a Transition with a Single Place in the
			// Postset
			if (abstPane.absListModel.contains(abstPane.xform3) && abstPane.isAbstract()) {
				change = checkTrans3(change);
			}
			// Transform 4 - Remove a Transition with a Single Place in the
			// Preset
			if (abstPane.absListModel.contains(abstPane.xform4) && abstPane.isAbstract()) {
				change = checkTrans4(change);
			}
			if (abstPane.absListModel.contains(abstPane.xform14) && abstPane.isSimplify()) {
				if (removeDeadPlaces()) {
					change = true;
				}
			}
			// Transform 8 - Propogate local assignments
			if (abstPane.absListModel.contains(abstPane.xform8) && abstPane.isSimplify()) {
				change = checkTrans8(change);
			}
			if (abstPane.absListModel.contains(abstPane.xform9) && abstPane.isAbstract()) {
				// change = checkTrans9(change);
			}
			if (abstPane.absListModel.contains(abstPane.xform15) && abstPane.isSimplify()) {
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
		assignments.add(booleanAssignments);
		assignments.add(contAssignments);
		assignments.add(intAssignments);
		assignments.add(rateAssignments);
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
			if (controlFlow.get(s).containsKey("postset")) {
				String[] postset = controlFlow.get(s).getProperty("postset").split(" ");
				for (String t : postset) {
					if (controlPlaces.get(t).containsKey("postset")) {
						String[] postTrans = controlPlaces.get(t).getProperty("postset").split(" ");
						for (String u : postTrans) {
							boolean flag = true;
							if (contAssignments.containsKey(u) && contAssignments.containsKey(s)) {
								for (Object o : contAssignments.get(u).keySet()) {
									String key = o.toString();
									if (!contAssignments.get(s).keySet().contains(key)
											|| (contAssignmentTrees.get(u).get(key)[0].isit != 'c')) {
										flag = false;
									}
								}
							}
							else {
								flag = false;
							}
							if (intAssignments.containsKey(u) && intAssignments.containsKey(s)) {
								for (Object o : intAssignments.get(u).keySet()) {
									String key = o.toString();
									if (!intAssignments.get(s).keySet().contains(key)
											|| (intAssignmentTrees.get(u).get(key)[0].isit != 'i')) {
										flag = false;
									}
								}
							}
							else {
								flag = false;
							}
							if (booleanAssignments.containsKey(u)
									&& booleanAssignments.containsKey(s)) {
								for (Object o : booleanAssignmentTrees.get(u).keySet()) {
									String key = o.toString();
									if (!booleanAssignments.get(s).keySet().contains(key)
											|| (booleanAssignmentTrees.get(u).get(key)[0].isit != 't')) {
										flag = false;
									}
								}
							}
							else {
								flag = false;
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
									if (assignRange[0][1].equals("inf")
											|| assignRange[1][1].equals("inf")) {
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
										addContAssign(s, o.toString(), "[" + assign[0] + ","
												+ assign[1] + "]");
									}
								}
								for (Object o : intAssignments.get(u).keySet()) {
									String[] assign = {
											intAssignments.get(u).get(o.toString()).toString(),
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
									if (assignRange[0][1].equals("inf")
											|| assignRange[1][1].equals("inf")) {
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
										addIntAssign(s, o.toString(), "[" + assign[0] + ","
												+ assign[1] + "]");
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
										addBoolAssign(s, o.toString(), "[" + assign[0] + ","
												+ assign[1] + "]");
									}
								}
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
			if ((!controlPlaces.get(s).containsKey("preset") // If the place is
					// initially
					// unmarked and
					// has no preset
					|| controlPlaces.get(s).getProperty("preset") == null || controlPlaces.get(s)
					.getProperty("preset").equals(""))
					&& !places.get(s)) {
				if (controlPlaces.get(s).containsKey("postset")) {
					String[] postset = controlPlaces.get(s).getProperty("postset").split(" ");
					for (String t : postset) { // Remove each transition in the
						// postset
						removeControlFlow(s, t);
						if (controlFlow.get(t).containsKey("postset")) {
							String[] tempPostset = controlFlow.get(t).getProperty("postset").split(
									"\\s");
							for (String p : tempPostset) {
								removeControlFlow(t, p);
							}
						}
						removeTransition(t);
					}
				}
				removePlace.add(s);
				change = true;
				continue;
			}
			if (!controlPlaces.get(s).containsKey("preset")
					&& !controlPlaces.get(s).containsKey("postset")) {
				removePlace.add(s); // Remove unconnected places
			}
			if (!change && abstPane.absListModel.contains(abstPane.xform15)) {
				if (places.get(s))
					continue;
				if (hasMarkedPreset(s)) // If the place is recursively dead
					continue;
				if (controlPlaces.get(s).containsKey("postset")) {
					for (String t : controlPlaces.get(s).getProperty("postset").split(" ")) {
						removeControlFlow(s, t); // Remove all transitions in
						// its
						// postset
						if (controlFlow.get(t).containsKey("postset")) {
							String[] tempPostset = controlFlow.get(t).getProperty("postset").split(
									"\\s");
							for (String p : tempPostset) {
								removeControlFlow(t, p);
							}
						}
						removeTransition(t);
					}
				}
				removePlace.add(s);
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
			if (abstPane.absListModel.contains(abstPane.xform16) && expr.evaluateExp(initVars) == 1
					&& abstPane.isSimplify()) {
				boolean enabled = true;
				for (String trans : booleanAssignments.keySet()) {
					HashMap<String, String> assignments = new HashMap<String, String>();
					Properties prop = new Properties();
					prop.putAll(booleanAssignments);
					prop.putAll(contAssignments);
					prop.putAll(intAssignments);
					for (Object o : prop.keySet()) {
						assignments.put(o.toString(), prop.get(o).toString());
					}
					if (trans.equals(t) || expr.becomesFalse(assignments)) {
						enabled = false;
						break;
					}
				}
				if (enabled) {
					removeEnab.add(t);
				}
			}
			else if (abstPane.absListModel.contains(abstPane.xform15)
					&& expr.evaluateExp(initVars) == 0 && abstPane.isSimplify()) {
				boolean disabled = true;
				for (String trans : booleanAssignments.keySet()) {
					HashMap<String, String> assignments = new HashMap<String, String>();
					Properties prop = new Properties();
					prop.putAll(booleanAssignments);
					prop.putAll(contAssignments);
					prop.putAll(intAssignments);
					for (Object o : prop.keySet()) {
						assignments.put(o.toString(), prop.get(o).toString());
					}
					if (trans.equals(t) || expr.becomesTrue(assignments)) {
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
							else if (p.equals(place))
								return false;
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
		if (inputs.containsKey("fail") || outputs.containsKey("fail"))
			intVars.add("fail");
		else if (inputs.containsKey("shutdown") || outputs.containsKey("shutdown"))
			intVars.add("shutdown");
		for (String s : oldIntVars) {
			if (!intVars.contains(s))
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
			intVars = new ArrayList<String>();
			for (String s : tempIntVars) {
				intVars.add(s);
			}
			for (String var : intVars) {
				for (HashMap<String, ExprTree[]> h : contAssignmentTrees.values()) {
					if (h.containsKey(var)) {
						for (ExprTree e : h.get(var)) {
							tempIntVars.addAll(e.getVars());
						}
					}
				}
				for (HashMap<String, ExprTree[]> h : intAssignmentTrees.values()) {
					if (h.containsKey(var)) {
						for (ExprTree e : h.get(var)) {
							if (e != null) {
								for (String v : e.getVars()) {
									if (!tempIntVars.contains(v)) {
										tempIntVars.add(v);
									}
								}
							}
						}
					}
				}
				for (HashMap<String, ExprTree[]> h : booleanAssignmentTrees.values()) {
					if (h.containsKey(var)) {
						for (ExprTree e : h.get(var)) {
							tempIntVars.addAll(e.getVars());
						}
					}
				}
				for (HashMap<String, ExprTree[]> h : rateAssignmentTrees.values()) {
					if (h.containsKey(var)) {
						for (ExprTree e : h.get(var)) {
							tempIntVars.addAll(e.getVars());
						}
					}
				}
			}
		}
		while (!intVars.equals(tempIntVars));
		tempIntVars = intVars;
		do {
			intVars = new ArrayList<String>();
			for (String s : tempIntVars) {
				intVars.add(s);
			}
			// intVars = tempIntVars;
			for (String var : intVars) {
				ArrayList<String> process = new ArrayList<String>();
				for (String s : contAssignmentTrees.keySet()) {
					if (contAssignmentTrees.get(s).keySet().contains(var)) {
						process.add(s);
					}
				}
				for (String s : intAssignmentTrees.keySet()) {
					if (intAssignmentTrees.get(s).keySet().contains(var)) {
						process.add(s);
					}
				}
				for (String s : booleanAssignmentTrees.keySet()) {
					if (booleanAssignmentTrees.get(s).keySet().contains(var)) {
						process.add(s);
					}
				}
				for (String s : rateAssignmentTrees.keySet()) {
					if (rateAssignmentTrees.get(s).keySet().contains(var)) {
						process.add(s);
					}
				}
				ArrayList<String> tempProcess = new ArrayList<String>();
				for (String s : process) {
					tempProcess.add(s);
				}
				do {
					process = new ArrayList<String>();
					for (String s : tempProcess) {
						process.add(s);
					}
					// process = tempProcess;
					for (String s : process) {
						if (controlFlow.get(s).containsKey("postset")) {
							for (String t : controlFlow.get(s).getProperty("postset").split(" ")) {
								for (String u : controlPlaces.get(t).getProperty("postset").split(
										" ")) {
									if (!tempProcess.contains(u)) {
										tempProcess.add(u);
									}
								}
							}
						}
						if (controlFlow.get(s).containsKey("preset")) {
							for (String t : controlFlow.get(s).getProperty("preset").split(" ")) {
								if (controlPlaces.get(t).containsKey("preset")) {
									for (String u : controlPlaces.get(t).getProperty("preset")
											.split(" ")) {
										if (!tempProcess.contains(u)) {
											tempProcess.add(u);
										}
									}
								}
							}
						}
					}
				}
				while (!tempProcess.equals(process));
				for (String trans : process) {
					if (enablingTrees.containsKey(trans)) {
						ArrayList<String> tempVars = enablingTrees.get(trans).getVars();
						for (String s : tempVars) {
							if (!tempIntVars.contains(s))
								tempIntVars.add(s);
						}
					}
				}
			}
		}
		while (!intVars.equals(tempIntVars));
		return intVars;
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

	private boolean checkTrans0(boolean change) {
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
		return change;
	}

	private boolean checkTrans1(boolean change) {
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
		return change;
	}

	private boolean checkTrans5(boolean change) {
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
							if ((h.get(t) != null && !h.get(t).keySet().isEmpty())
									|| (h.get(s) != null && !h.get(s).keySet().isEmpty())) {
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
							&& controlFlow.get(t).containsKey("preset") && !assign
							&& abstPane.absListModel.contains(abstPane.xform6)) {
						String[] postset1 = controlFlow.get(s).getProperty("postset").split(" ");
						String[] postset2 = controlFlow.get(t).getProperty("postset").split(" ");
						if (postset1.length == 1 && postset2.length == 1
								&& controlPlaces.containsKey(postset1[0])
								&& controlPlaces.containsKey(postset2[0])) {
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
							&& controlFlow.get(t).containsKey("postset") && !assign
							&& abstPane.absListModel.contains(abstPane.xform7)) {
						String[] preset1 = controlFlow.get(s).getProperty("preset").split(" ");
						String[] preset2 = controlFlow.get(t).getProperty("preset").split(" ");
						if (preset1.length == 1 && preset2.length == 1) {
							if (comparePostset(controlPlaces.get(preset1[0]), controlPlaces
									.get(preset2[0]), s, t)
									&& !isFail(t)) {
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
		return change;
	}

	private boolean checkTrans5b(boolean change) {
		ArrayList<String[]> combine = new ArrayList<String[]>();
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
									if (!h.get(t).keySet().isEmpty()
											|| !h.get(s).keySet().isEmpty()) {
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
						&& controlFlow.get(s[1]).containsKey("postset") && !isFail(s[1])) {
					change = true;
					combineTransitions(s[0], s[1], true, true);
				}
			}
		}
		return change;
	}

	private boolean checkTrans3(boolean change) {
		// Remove a transition with a single place in the postset
		ArrayList<String> remove = new ArrayList<String>();
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
							preset = controlPlaces.get(postset[0]).getProperty("preset").split(" ");
							if ((preset.length == 1 || post) && !assign) {
								remove.add(s);
							}
						}
					}
				}
			}
		}
		for (String s : remove) {
			if (controlFlow.get(s).containsKey("preset")
					&& controlFlow.get(s).containsKey("postset")) {
				// String[] preset =
				// controlFlow.get(s).getProperty("preset").split(" ");
				String[] postset = controlFlow.get(s).getProperty("postset").split(" ");
				if (postset.length == 1 && !postset[0].equals("") && !isFail(s)) {
					if (removeTrans3(s))
						change = true;
				}
			}
		}
		return change;
	}

	private boolean checkTrans4(boolean change) {
		ArrayList<String> remove = new ArrayList<String>();
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
							if (controlPlaces.containsKey(p)) {
								if (controlPlaces.get(p).getProperty("preset").split(" ").length != 1) {
									pre = false;
								}
							}
						}
					}
					if (controlPlaces.containsKey(preset[0])) {
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
		}
		for (String s : remove) {
			if (controlFlow.get(s) != null) {
				// System.out.println("[4]Removing transition: " + s);
				if (controlFlow.get(s).getProperty("preset") != null
						&& controlFlow.get(s).getProperty("postset") != null
						&& !controlFlow.get(s).getProperty("postset").equals("") && !isFail(s)) {
					if (removeTrans4(s))
						change = true;
				}
			}
		}
		return change;
	}

	private boolean checkTrans8(boolean change) {
		// Propagate expressions of local variables to transition post sets
		ArrayList<String> initMarking = new ArrayList<String>();
		ArrayList<String> unvisited = new ArrayList<String>();
		unvisited.addAll(delays.keySet());
		for (String p : places.keySet()) {
			if (places.get(p)) {
				initMarking.add(p);
			}
		}
		for (int i = 0; i < 2; i++) {
			for (String p : initMarking) {
				if (controlPlaces.get(p).containsKey("postset")) {
					for (String t : controlPlaces.get(p).getProperty("postset").split("\\s")) {
						change = trans8Iteration(t, unvisited, change);
					}
				}
			}
		}
		return change;
	}

	private boolean checkTrans9(boolean change) {
		ArrayList<String[]> remove = new ArrayList<String[]>();
		for (HashMap<String, Properties> assign : assignments) {
			for (String t : assign.keySet()) {
				for (Object o : assign.get(t).keySet()) {
					String var = o.toString();
					if (readBeforeWrite(t, var)) {
						String[] temp = { t, var };
						remove.add(temp);
					}
				}
			}
			for (String[] temp : remove) {
				Properties prop = assign.get(temp[0]);
				if (prop != null) {
					prop.remove(temp[1]);
					assign.put(temp[0], prop);
				}
				else {
					assign.remove(temp[0]);
				}
			}
		}
		return change;
	}

	private void simplifyExpr() {
		for (String s : enablingTrees.keySet()) {
			if (s != null && enablingTrees.get(s) != null) {
				enablings.put(s, enablingTrees.get(s).toString());
			}
		}
		for (String s : booleanAssignments.keySet()) {
			Properties prop = booleanAssignments.get(s);
			HashMap<String, ExprTree[]> expr = booleanAssignmentTrees.get(s);
			for (Object o : prop.keySet()) {
				String t = o.toString();
				if (expr.containsKey(t)) {
					ExprTree[] e = expr.get(t);
					if (expr.get(t).length > 1) {
						if (expr.get(t)[1] != null) {
							prop
									.setProperty(t, "[" + e[0].toString() + "," + e[1].toString()
											+ "]");
						}
						else {
							prop.setProperty(t, e[0].toString());
						}
					}
					else {
						prop.setProperty(t, e[0].toString());
					}
				}
			}
			booleanAssignments.put(s, prop);
		}
		for (String s : contAssignments.keySet()) {
			Properties prop = contAssignments.get(s);
			HashMap<String, ExprTree[]> expr = contAssignmentTrees.get(s);
			for (Object o : prop.keySet()) {
				String t = o.toString();
				ExprTree[] e = expr.get(t);
				if (expr.get(t)[1] != null) {
					prop.setProperty(t, "[" + e[0].toString() + "," + e[1].toString() + "]");
				}
				else {
					prop.setProperty(t, e[0].toString());
				}
			}
			contAssignments.put(s, prop);
		}
		for (String s : rateAssignments.keySet()) {
			Properties prop = rateAssignments.get(s);
			HashMap<String, ExprTree[]> expr = rateAssignmentTrees.get(s);
			for (Object o : prop.keySet()) {
				String t = o.toString();
				ExprTree[] e = expr.get(t);
				if (expr.get(t)[1] != null) {
					prop.setProperty(t, "[" + e[0].toString() + "," + e[1].toString() + "]");
				}
				else {
					prop.setProperty(t, e[0].toString());
				}
			}
			rateAssignments.put(s, prop);
		}
		for (String s : intAssignments.keySet()) {
			Properties prop = intAssignments.get(s);
			HashMap<String, ExprTree[]> expr = intAssignmentTrees.get(s);
			for (Object o : prop.keySet()) {
				String t = o.toString();
				ExprTree[] e = expr.get(t);
				if (expr.get(t)[1] != null) {
					prop.setProperty(t, "[" + e[0].toString() + "," + e[1].toString() + "]");
				}
				else {
					prop.setProperty(t, e[0].toString());
				}
			}
			intAssignments.put(s, prop);
		}
	}

	private boolean removeVars(boolean change) {
		ArrayList<String> removeVars = new ArrayList<String>();
		for (String var : variables.keySet()) {
			boolean used = false;
			for (ExprTree e : enablingTrees.values()) {
				if (e != null) {
					if (e.containsVar(var)) {
						used = true;
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : booleanAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e.containsVar(var)) {
							used = true;
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : contAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e.containsVar(var)) {
							used = true;
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : intAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : rateAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			if (!used) {
				removeVars.add(var);
			}
		}
		for (String s : removeVars) {
			remove(s);
		}
		removeVars = new ArrayList<String>();
		for (String var : inputs.keySet()) {
			boolean used = false;
			for (ExprTree e : enablingTrees.values()) {
				if (e != null) {
					if (e != null) {
						if (e.containsVar(var)) {
							used = true;
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : booleanAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : contAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : intAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : rateAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			if (!used) {
				removeVars.add(var);
			}
		}
		for (String s : removeVars) {
			remove(s);
		}
		removeVars = new ArrayList<String>();
		for (String var : outputs.keySet()) {
			boolean used = false;
			for (ExprTree e : enablingTrees.values()) {
				if (e != null) {
					if (e.containsVar(var)) {
						used = true;
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : booleanAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : contAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : intAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : rateAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			if (!used) {
				removeVars.add(var);
			}
		}
		for (String s : removeVars) {
			remove(s);
		}
		removeVars = new ArrayList<String>();
		for (String var : integers.keySet()) {
			boolean used = false;
			for (ExprTree e : enablingTrees.values()) {
				if (e != null) {
					if (e.containsVar(var)) {
						used = true;
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : booleanAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e.containsVar(var)) {
							used = true;
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : contAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e.containsVar(var)) {
							used = true;
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : intAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			for (HashMap<String, ExprTree[]> map : rateAssignmentTrees.values()) {
				for (ExprTree[] expr : map.values()) {
					for (ExprTree e : expr) {
						if (e != null) {
							if (e.containsVar(var)) {
								used = true;
							}
						}
					}
				}
			}
			if (!used) {
				removeVars.add(var);
			}
		}
		for (String s : removeVars) {
			remove(s);
		}
		return change;
	}

	private boolean removeTrans3(String transition) {
		// Remove a transition with a single place in the postset
		String place = controlFlow.get(transition).getProperty("postset");
		String[] preset = controlFlow.get(transition).getProperty("preset").split("\\s");
		String[] postset = controlPlaces.get(place).getProperty("postset").split("\\s");
		String[] placePreset = controlPlaces.get(place).getProperty("preset").split("\\s");
		boolean marked = places.get(place);
		// Check to make sure that the place is not a self-loop
		if (preset.length == 1) {
			if (controlPlaces.get(preset[0]).containsKey("preset")) {
				String[] tempPreset = controlPlaces.get(preset[0]).getProperty("preset").split(" ");
				if (tempPreset.length == 1 && tempPreset[0].equals(transition)) {
					return false;
				}
			}
		}
		if (fail.contains(transition)) {
			return false;
		}
		// Update control flow
		removeControlFlow(transition, place);
		for (String p : preset) {
			if (marked)
				places.put(p, true);
			removeControlFlow(p, transition);
			for (String t : postset) {
				addControlFlow(p, t);
			}
		}
		for (String t : placePreset) {
			removeControlFlow(t, place);
			for (String p : preset) {
				if (!p.equals(place)) {
					addControlFlow(t, p);
				}
			}
		}
		for (String t : postset) {
			removeControlFlow(place, t);
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
			// String[] tempPostset =
			// controlPlaces.get(t).getProperty("postset").split(" ");
			String tempList = "";
			// for (int i = 0; i < tempPostset.length; i++) {
			// if (!tempPostset[i].equals(transition) &&
			// !tempPostset[i].equals("")) {
			// tempList = tempList + tempPostset[i] + " ";
			// // if (marked) {
			// // places.put(tempPostset[i], true);
			// // }
			// }
			// }
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

	private boolean removeTrans4(String transition) {
		// Remove a transition with a single place in the preset
		String place = controlFlow.get(transition).getProperty("preset");
		String[] preset = controlPlaces.get(place).getProperty("preset").split("\\s");
		String[] postset = controlFlow.get(transition).getProperty("postset").split("\\s");
		String[] transPostset = controlPlaces.get(place).getProperty("postset").split("\\s");
		// Check to make sure that the place is not a self-loop
		if (postset.length == 1) {
			String[] tempPostset = controlPlaces.get(postset[0]).getProperty("postset").split(" ");
			if (tempPostset.length == 1 && tempPostset[0].equals(transition)) {
				return false;
			}
		}
		if (fail.contains(transition)) {
			return false;
		}
		boolean marked = places.get(place);
		// Update the control Flow
		removeControlFlow(place, transition);
		for (String t : preset) {
			removeControlFlow(t, place);
			for (String p : postset) {
				addControlFlow(t, p);
			}
		}
		for (String t : transPostset) {
			removeControlFlow(place, t);
		}
		for (String p : postset) {
			removeControlFlow(transition, p);
			for (String t : transPostset) {
				if (!t.equals(transition)) {
					addControlFlow(p, t);
				}
			}
			if (marked) {
				places.put(p, true);
			}
		}
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
		removePlace(place);
		removeTransition(transition);
		return true;
	}

	private boolean removeTrans4(String transition, String[] preset, String[] postset) {
		// Remove a Transition with one Place in the Preset
		String place = preset[0];
		preset = controlPlaces.get(place).getProperty("preset").split(" ");
		// Check to make sure that the place is not a self-loop
		if (postset.length == 1) {
			String[] tempPostset = controlPlaces.get(postset[0]).getProperty("postset").split(" ");
			if (tempPostset.length == 1 && tempPostset[0].equals(transition)) {
				return false;
			}
		}
		boolean marked = places.get(place);
		String[] transPostset = controlPlaces.get(place).getProperty("postset").split(" ");
		// Combine control Flow
		for (String p : postset) {
			// Retain transitions in preset of postset, as in 4c
			String[] tempPreset = controlPlaces.get(p).getProperty("preset").split(" ");
			String tempList = "";
			for (int i = 0; i < tempPreset.length; i++) {
				if (!tempPreset[i].equals(transition)) {
					tempList = tempList + tempPreset[i] + " ";
				}
			}
			// Add transitions that were in the postset of the removed place, as
			// in 4b
			for (String t : transPostset) {
				if (!t.equals(transition)) {
					tempList = tempList + t + " ";
					Properties prop = controlFlow.get(t); // Including both
					// sides of the
					// movement
					prop.setProperty("postset", prop.getProperty("postset") + " " + t);
					controlPlaces.put(t, prop);
				}
			}
			// Add new transitions into preset
			for (String t : preset) {
				tempList = tempList + t + " ";
				Properties prop = controlFlow.get(t); // Including both sides of
				// the movement
				prop.setProperty("postset", prop.getProperty("postset") + " " + t);
				controlPlaces.put(t, prop);
			}
			Properties prop = controlPlaces.get(p);
			prop.setProperty("preset", tempList.trim());
			controlPlaces.put(p, prop);
			if (marked) {
				places.put(p, true);
			}
		}
		for (String t : preset) {
			String[] tempPostset = controlFlow.get(t).getProperty("postset").split(" ");
			String tempList = "";
			for (String p : tempPostset) {
				if (!p.equals(place)) {
					tempList = tempList + p + " ";
				}
			}
			for (String p : postset) {
				tempList = tempList + p + " ";
				Properties prop = controlPlaces.get(p);
				prop.setProperty("preset", prop.getProperty("preset") + " " + t);
				controlPlaces.put(p, prop);
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
		if (controlFlow.get(trans1) == null || controlFlow.get(trans2) == null
				|| fail.contains(trans2)) {
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

	private boolean trans8Iteration(String trans, ArrayList<String> unvisited, boolean change) {
		ArrayList<String[]> toChange = new ArrayList<String[]>();
		if (intAssignments.get(trans) != null) {
			for (Object o : intAssignments.get(trans).keySet()) {
				String var = o.toString();
				String[] add = { trans, var };
				toChange.add(add);
				// trans8(trans, var, change);
			}
		}
		for (String[] array : toChange) {
			trans8(array[0], array[1], change);
		}
		unvisited.remove(trans);
		if (controlFlow.get(trans).containsKey("postset")) {
			for (String p : controlFlow.get(trans).getProperty("postset").split("\\s")) {
				for (String t : controlPlaces.get(p).getProperty("postset").split("\\s")) {
					if (unvisited.contains(t)) {
						change = trans8Iteration(t, unvisited, change);
					}
				}
			}
		}
		return change;
	}

	private boolean trans8(String trans, String var, boolean change) {
		// Propagate expressions of local variables to transition post sets
		if (!(process_read.get(var).equals(process_trans.get(trans)) && process_write.get(var)
				.equals(process_trans.get(trans)))) {
			return change; // Return if the variable is not local
		}
		HashMap<String, HashMap<String, ExprTree[]>> typeAssign;
		// The assignments that will contain var
		if (isInteger(var)) {
			typeAssign = intAssignmentTrees;
		}
		else {
			return change;
		}
		if (typeAssign.containsKey(trans)) {
			ExprTree[] e = typeAssign.get(trans).get(var);
			if (e == null) {
				return change;
			}
			// for (ExprTree e1 : e) {
			for (String v : e[0].getVars()) {
				if (!process_read.get(v).equals(process_trans.get(trans))) {
					return change; // Return if the variables in support(e) are
					// not
					// locally written
				}
			}
			if (e.toString().equals("")) {
				return change;
			}
			// }
			if (controlFlow.get(trans).containsKey("postset")) {
				for (String p : controlFlow.get(trans).getProperty("postset").split("\\s")) {
					for (String tP : controlPlaces.get(p).getProperty("postset").split("\\s")) {
						for (String pP : controlFlow.get(tP).getProperty("preset").split("\\s")) {
							for (String tPP : controlPlaces.get(pP).getProperty("preset").split(
									"\\s")) {
								if (typeAssign.containsKey(tPP)) {
									ExprTree[] ePP = typeAssign.get(tPP).get(var);
									// for (ExprTree e1 : ePP) {
									if (!ePP[0].isEqual(e[0])) {
										return change; // All assignments to var
										// in
										// ..(t..)
										// must be equal
									}
									for (String v : ePP[0].getVars()) {
										if (isBoolean(v)) { // All variables in
											// support(e) cannot be
											// assigned
											if (booleanAssignments.containsKey(tPP)) {
												if (booleanAssignments.get(tPP).containsKey(v)) {
													return change;
												}
											}
										}
										else if (isInteger(v)) {
											if (intAssignments.containsKey(tPP)) {
												if (intAssignments.get(tPP).containsKey(v)) {
													return change;
												}
											}
										}
										else {
											if (contAssignments.containsKey(tPP)) {
												if (contAssignments.get(tPP).containsKey(v)) {
													return change;
												}
											}
										}
									}
								}
								else {
									return change;
								}
								// }
							}
						}
					}
				}
			}
			// Perform transform
			if (controlFlow.get(trans).containsKey("postset")) {
				for (String p : controlFlow.get(trans).getProperty("postset").split("\\s")) {
					for (String tP : controlPlaces.get(p).getProperty("postset").split("\\s")) {
						if (e.length > 1) {
							if (e[1] != null) {
								return change;
							}
						}
						replace(tP, var, e);
						for (String pP : controlFlow.get(tP).getProperty("preset").split("\\s")) {
							for (String tPP : controlPlaces.get(pP).getProperty("preset").split(
									"\\s")) {
								if (isBoolean(var)) {
									removeBoolAssign(tPP, var);
								}
								else if (isInteger(var)) {
									removeIntAssign(tPP, var);
								}
								else {
									removeContAssign(tPP, var);
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	private boolean readBeforeWrite(String trans, String var) {
		if (enablingTrees.containsKey(trans)) {
			if (enablingTrees.get(trans).getVars().contains(var)) {
				return false;
			}
		}
		if (booleanAssignmentTrees.containsKey(trans)) {
			for (String s : booleanAssignmentTrees.get(trans).keySet()) {
				if (s.equals(var)) {
					return true;
				}
				for (ExprTree e1 : booleanAssignmentTrees.get(trans).get(s)) {
					if (e1.getVars().contains(var)) {
						return false;
					}
				}
			}
		}
		if (contAssignmentTrees.containsKey(trans)) {
			for (String s : contAssignmentTrees.get(trans).keySet()) {
				if (s.equals(var)) {
					return true;
				}
				for (ExprTree e1 : contAssignmentTrees.get(trans).get(s)) {
					if (e1.getVars().contains(var)) {
						return false;
					}
				}
			}
		}
		if (intAssignmentTrees.containsKey(trans)) {
			for (String s : intAssignmentTrees.get(trans).keySet()) {
				if (s.equals(var)) {
					return true;
				}
				for (ExprTree e1 : intAssignmentTrees.get(trans).get(s)) {
					if (e1 != null) {
						if (e1.getVars().contains(var)) {
							return false;
						}
					}
				}
			}
		}
		if (rateAssignmentTrees.containsKey(trans)) {
			for (String s : rateAssignmentTrees.get(trans).keySet()) {
				if (s.equals(var)) {
					return true;
				}
				for (ExprTree e1 : rateAssignmentTrees.get(trans).get(s)) {
					if (e1.getVars().contains(var)) {
						return false;
					}
				}
			}
		}
		if (controlFlow.get(trans).containsKey("postset")) {
			for (String p : controlFlow.get(trans).getProperty("postset").split("\\s")) {
				if (controlPlaces.get(p).containsKey("postset")) {
					for (String t : controlPlaces.get(p).getProperty("postset").split("\\s")) {
						if (!readBeforeWrite(t, var)) {
							return false;
						}
					}
				}
			}
		}
		return true;
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

	public void addRates(HashMap<String, String> newRates) {
		for (String s : newRates.keySet()) {
			transitionRates.put(s, newRates.get(s));
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

	public void addFails(ArrayList<String> fails) {
		for (String s : fails) {
			fail.add(s);
		}
	}

	private boolean divideProcesses() {
		for (String t : delays.keySet()) { // Add all transitions to process
			// structure
			process_trans.put(t, 0);
		}
		for (String v : inputs.keySet()) { // / Add All variables to process
			// structure
			process_write.put(v, 0);
			process_read.put(v, 0);
		}
		for (String v : outputs.keySet()) {
			process_write.put(v, 0);
			process_read.put(v, 0);
		}
		for (String v : variables.keySet()) {
			process_write.put(v, 0);
			process_read.put(v, 0);
		}
		for (String v : integers.keySet()) {
			process_write.put(v, 0);
			process_read.put(v, 0);
		}
		Integer i = 1; // The total number of processes
		Integer process = 1; // The active process number
		while (process_trans.containsValue(0)) {
			String new_proc = ""; // Find a transition that is not part of a
			// process
			for (String t : process_trans.keySet()) {
				if (process_trans.get(t) == 0) {
					new_proc = t;
					break;
				}
			}
			boolean flag = false; // Make sure that it is not part of a process
			if (controlFlow.get(new_proc).containsKey("preset")) {
				for (String p : controlFlow.get(new_proc).getProperty("preset").split("\\s")) {
					if (!flag) // Check the preset to see if it is part of a
						// process
						if (controlPlaces.get(p).containsKey("preset")) {
							for (String t : controlPlaces.get(p).getProperty("preset").split("\\s")) {
								if (!flag)
									if (process_trans.get(t) != 0) {
										flag = true;
										process = process_trans.get(t);
										break;
									}
							}
						}
				}
			}
			if (!flag) // Check the postset to see if it is part of a process
				if (controlFlow.get(new_proc).containsKey("postset")) {
					for (String p : controlFlow.get(new_proc).getProperty("postset").split("\\s")) {
						if (!flag)
							if (controlPlaces.get(p).containsKey("postset")) {
								for (String t : controlPlaces.get(p).getProperty("postset").split(
										"\\s")) {
									if (!flag)
										if (process_trans.get(t) != 0) {
											flag = true;
											process = process_trans.get(t);
											break;
										}
								}
							}
					}
				}
			if (!flag) {
				i++; // Increment the process counter if it is not part of a
				// process
				process = i;
			}
			if (!addTransProcess(new_proc, process))
				return false;
		}
		assignVariableProcess();
		return true;
	}

	private void assignVariableProcess() {
		for (HashMap<String, Properties> h : assignments) { // For each
			// transition with
			// assignments
			for (String t : h.keySet()) {
				Properties prop = h.get(t);
				HashMap<String, HashMap<String, ExprTree[]>> map = null;
				if (h.equals(booleanAssignments)) {
					map = booleanAssignmentTrees;
				}
				else if (h.equals(contAssignments)) {
					map = contAssignmentTrees;
				}
				else if (h.equals(rateAssignments)) {
					map = rateAssignmentTrees;
				}
				else if (h.equals(intAssignments)) {
					map = intAssignmentTrees;
				}
				for (Object o : prop.keySet()) { // The variables assigned on
					// each transition
					String v = o.toString();
					if ((process_write.get(v) == 0)
							|| (process_write.get(v) == process_trans.get(t))) {
						process_write.put(v, process_trans.get(t)); // Mark a
						// variable
						// as
						// locally
						// written
						// to a
						// process
					}
					else {
						process_write.put(v, -1); // Mark a variable as globally
						// written
					}
				}
				HashMap<String, ExprTree[]> assign = map.get(t);
				for (ExprTree[] e : assign.values()) {
					for (String v : e[0].getVars()) {
						if ((process_read.get(v) == 0)
								|| (process_read.get(v) == process_trans.get(t))) {
							process_read.put(v, process_trans.get(t)); // Mark
							// a
							// variable
							// as
							// locally
							// read
						}
						else {
							process_read.put(v, -1); // Mark a variable as
							// globally read
						}
					}
				}
			}
		}
		for (String t : enablingTrees.keySet()) { // Check enabling conditions
			// for read variables
			ExprTree e = enablingTrees.get(t);
			if (e != null) {
				for (String v : e.getVars()) {
					if ((process_read.get(v) == 0) || (process_read.get(v) == process_trans.get(t))) {
						process_read.put(v, process_trans.get(t));
					}
					else {
						process_read.put(v, -1);
					}
				}
			}
		}
	}

	private boolean addTransProcess(String trans, Integer proc) {
		process_trans.put(trans, proc); // Add the current transition to the
		// process
		if (controlFlow.get(trans).containsKey("postset")) {
			for (String p : controlFlow.get(trans).getProperty("postset").split("\\s")) {
				if (controlPlaces.get(p).containsKey("postset")) {
					for (String t : controlPlaces.get(p).getProperty("postset").split("\\s")) {
						if (process_trans.get(t) == 0)
							addTransProcess(t, proc); // Add the postset of the
						// transition to the same
						// process recursively
						else if (process_trans.get(t) != proc) {
							System.out
									.println("Error: Multiple Process Labels Added to the Same Transition");
							return false;
						}
					}
				}
			}
		}
		if (controlFlow.get(trans).containsKey("preset")) {
			for (String p : controlFlow.get(trans).getProperty("preset").split("\\s")) {
				if (controlPlaces.get(p).containsKey("preset")) {
					for (String t : controlPlaces.get(p).getProperty("preset").split("\\s")) {
						if (process_trans.get(t) == 0)
							addTransProcess(t, proc); // Add the preset of the
						// transition to the same
						// process recursively
						else if (process_trans.get(t) != proc) {
							System.out
									.println("Error: Multiple Process Labels Added to the Same Transition");
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private boolean isBoolean(String var) {
		if (inputs.containsKey(var)) {
			return true;
		}
		else if (outputs.containsKey(var)) {
			return true;
		}
		return false;
	}

	private boolean replace(String trans, String var, ExprTree[] expr) {
		boolean flag = false;
		// if (expr[1] == null) {
		if (enablings.containsKey(trans)) {
			enablingTrees.get(trans).replace(var, expr[0]);
			enablings.put(trans, enablingTrees.get(trans).toString());
			flag = true;
		}
		if (intAssignmentTrees.containsKey(var)) {
			if (!intAssignmentTrees.get(trans).containsKey(var)) {
				addIntAssign(trans, var, expr[0].toString());
			}
		}
		else {
			addIntAssign(trans, var, expr[0].toString());
		}
		// }
		for (HashMap<String, Properties> assign : assignments) {
			HashMap<String, HashMap<String, ExprTree[]>> assignTree;
			if (assign.equals(booleanAssignments)) {
				assignTree = booleanAssignmentTrees;
			}
			else if (assign.equals(intAssignments)) {
				assignTree = intAssignmentTrees;
			}
			else if (assign.equals(contAssignments)) {
				assignTree = contAssignmentTrees;
			}
			else {
				assignTree = rateAssignmentTrees;
			}
			if (assignTree.containsKey(trans)) {
				for (String v : assignTree.get(trans).keySet()) {
					ExprTree[] e1 = assignTree.get(trans).get(v);
					// if (e1[1] == null && expr[1] != null) {
					// e1[1] = e1[0];
					// }
					// if (expr[1] == null) {
					e1[0].replace(var, expr[0]);
					if (e1.length > 1 && expr.length > 1) {
						if (e1[1] != null && expr[1] != null && !e1[1].toString().equals("")
								&& !expr[1].toString().equals("")) {
							e1[1].replace(var, expr[0]);
							if (assign.equals(booleanAssignments)) {
								addBoolAssign(trans, v, "[" + e1[0].toString() + ","
										+ e1[1].toString() + "]");
							}
							else if (assign.equals(intAssignments)) {
								addIntAssign(trans, v, "[" + e1[0].toString() + ","
										+ e1[1].toString() + "]");
							}
							else if (assign.equals(contAssignments)) {
								addContAssign(trans, v, "[" + e1[0].toString() + ","
										+ e1[1].toString() + "]");
							}
							else {
								addRateAssign(trans, v, "[" + e1[0].toString() + ","
										+ e1[1].toString() + "]");
							}
						}
						else {
							if (assign.equals(booleanAssignments)) {
								addBoolAssign(trans, v, e1[0].toString());
							}
							else if (assign.equals(intAssignments)) {
								addIntAssign(trans, v, e1[0].toString());
							}
							else if (assign.equals(contAssignments)) {
								addContAssign(trans, v, e1[0].toString());
							}
							else {
								addRateAssign(trans, v, e1[0].toString());
							}
						}
					}
					else {
						if (assign.equals(booleanAssignments)) {
							addBoolAssign(trans, v, e1[0].toString());
						}
						else if (assign.equals(intAssignments)) {
							addIntAssign(trans, v, e1[0].toString());
						}
						else if (assign.equals(contAssignments)) {
							addContAssign(trans, v, e1[0].toString());
						}
						else {
							addRateAssign(trans, v, e1[0].toString());
						}
					}
					// }
					// else {
					// e1[0].replace(var, expr[0]);
					// e1[1].replace(var, expr[1]);
					// if (assign.equals(booleanAssignments)) {
					// addBoolAssign(trans, var, "[" + expr[0].toString() + ","
					// + expr[1].toString() + "]");
					// }
					// else if (assign.equals(intAssignments)) {
					// addIntAssign(trans, var, "[" + expr[0].toString() + ","
					// + expr[1].toString() + "]");
					// }
					// else if (assign.equals(contAssignments)) {
					// addContAssign(trans, var, "[" + expr[0].toString() + ","
					// + expr[1].toString() + "]");
					// }
					// else {
					// addRateAssign(trans, var, "[" + expr[0].toString() + ","
					// + expr[1].toString() + "]");
					// }
					// }
				}
			}
		}
		return flag;
	}

	private void remove(String var) {
		for (HashMap<String, Properties> assign : assignments) {
			for (String t : assign.keySet()) {
				if (assign.get(t).containsKey(var)) {
					removeAssignment(t, var);
				}
			}
		}
		if (isContinuous(var))
			variables.remove(var);
		else if (isInteger(var))
			integers.remove(var);
		else if (isInput(var))
			inputs.remove(var);
		else if (isOutput(var))
			outputs.remove(var);
	}

	private static final String RANGE = "\\[(\\w+?),(\\w+?)\\]";
}