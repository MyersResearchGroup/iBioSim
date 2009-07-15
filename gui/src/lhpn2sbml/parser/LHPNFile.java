package lhpn2sbml.parser;

//import gcm2sbml.util.GlobalConstants;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.swing.JOptionPane;
import biomodelsim.Log;

/**
 * This class describes an LHPN file
 * 
 * @author Kevin Jones
 * @organization University of Utah
 */

public class LHPNFile {

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

	// private HashMap<String, ExprTree[]> delayTrees;

	private HashMap<String, ExprTree> transitionRateTrees;

	private HashMap<String, String> transitionRates;

	private HashMap<String, Properties> booleanAssignments;

	private HashMap<String, HashMap<String, ExprTree[]>> booleanAssignmentTrees;

	private String property;

	private Log log;

	public LHPNFile(Log log) {
		this.log = log;
		places = new HashMap<String, Boolean>();
		inputs = new HashMap<String, String>();
		outputs = new HashMap<String, String>();
		enablings = new HashMap<String, String>();
		enablingTrees = new HashMap<String, ExprTree>();
		delays = new HashMap<String, String>();
		// delayTrees = new HashMap<String, ExprTree[]>();
		transitionRateTrees = new HashMap<String, ExprTree>();
		transitionRates = new HashMap<String, String>();
		booleanAssignments = new HashMap<String, Properties>();
		booleanAssignmentTrees = new HashMap<String, HashMap<String, ExprTree[]>>();
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
	}

	public LHPNFile() {
		places = new HashMap<String, Boolean>();
		inputs = new HashMap<String, String>();
		outputs = new HashMap<String, String>();
		enablings = new HashMap<String, String>();
		enablingTrees = new HashMap<String, ExprTree>();
		delays = new HashMap<String, String>();
		// delayTrees = new HashMap<String, ExprTree[]>();
		transitionRateTrees = new HashMap<String, ExprTree>();
		transitionRates = new HashMap<String, String>();
		booleanAssignments = new HashMap<String, Properties>();
		booleanAssignmentTrees = new HashMap<String, HashMap<String, ExprTree[]>>();
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
									if (toArray[i] != null && !toArray[i].equals("null")
											&& !toArray[i].equals("")) {
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
									if (fromArray[i] != null && !fromArray[i].equals("null")
											&& !fromArray[i].equals("")) {
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
						// buffer.append("<" + s + "=");
						for (Object key : prop.keySet()) {
							String t = (String) key;
							buffer.append("<" + s + "=[" + t + ":=" + prop.getProperty(t) + "]>");
						}
						// buffer.append(">");
					}
				}
				for (String s : intAssignments.keySet()) {
					Properties prop = intAssignments.get(s);
					if (!prop.isEmpty()) {
						if (!flag) {
							buffer.append("#@.assignments {");
							flag = true;
						}
						// buffer.append("<" + s + "=");
						for (Object key : prop.keySet()) {
							String t = (String) key;
							// log.addText("key " + t);
							buffer.append("<" + s + "=[" + t + ":=" + prop.getProperty(t) + "]>");
						}
						// buffer.append(">");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			if (!rateAssignments.isEmpty()) {
				flag = false;
				for (String s : rateAssignments.keySet()) {
					// boolean varFlag = false;
					Properties prop = rateAssignments.get(s);
					for (Object key : prop.keySet()) {
						String t = (String) key;
						if (!t.equals("")) {
							if (!flag) {
								buffer.append("#@.rate_assignments {");
								flag = true;
							}
							// if (!varFlag) {
							// buffer.append("<" + s + "=");
							// varFlag = true;
							// }
							buffer.append("<" + s + "=[" + t + ":=" + prop.getProperty(t) + "]>");
						}
					}
					// if (varFlag) {
					// buffer.append(">");
					// }
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
			if (!transitionRates.isEmpty()) {
				flag = false;
				for (String s : transitionRates.keySet()) {
					if (s != null && !transitionRates.get(s).equals("")) {
						if (!flag) {
							buffer.append("#@.transition_rates {");
							flag = true;
						}
						// log.addText("here " + enablings.get(s));
						buffer.append("<" + s + "=[" + transitionRates.get(s) + "]>");
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
						// boolean varFlag = false;
						Properties prop = booleanAssignments.get(s);
						for (Object key : prop.keySet()) {
							String t = (String) key;
							if (!t.equals("") && (isInput(t) || isOutput(t))) {
								if (!flag) {
									buffer.append("#@.boolean_assignments {");
									flag = true;
								}
								// if (!varFlag) {
								// buffer.append("<" + s + "=");
								// varFlag = true;
								// }
								buffer.append("<" + s + "=[" + t + ":=" + prop.getProperty(t)
										+ "]>");
							}
						}
						// if (varFlag) {
						// buffer.append(">");
						// }
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

	public void load(String filename) {
		places = new HashMap<String, Boolean>();
		inputs = new HashMap<String, String>();
		outputs = new HashMap<String, String>();
		enablings = new HashMap<String, String>();
		enablingTrees = new HashMap<String, ExprTree>();
		controlFlow = new HashMap<String, Properties>();
		controlPlaces = new HashMap<String, Properties>();
		variables = new HashMap<String, Properties>();
		contAssignments = new HashMap<String, Properties>();
		contAssignmentTrees = new HashMap<String, HashMap<String, ExprTree[]>>();
		rateAssignments = new HashMap<String, Properties>();
		rateAssignmentTrees = new HashMap<String, HashMap<String, ExprTree[]>>();
		StringBuffer data = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
				// System.out.println(str);
			}
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}

		try {
			parseProperty(data);
			// System.out.println("check1");
			// log.addText("check1");
			parseInOut(data);
			// System.out.println("check2");
			// log.addText("check2");
			// parseTransitions(data);
			parseControlFlow(data);
			// System.out.println("check3");
			// log.addText("check3");
			parseVars(data);
			// System.out.println("check4");
			// log.addText("check");
			parseIntegers(data);
			// parseInitialVals(data);
			// System.out.println("check5");
			// log.addText("check4");
			parsePlaces(data);
			parseMarking(data);
			// System.out.println("check6");
			// log.addText("check5");
			parseEnabling(data);
			// System.out.println("check7");
			// log.addText("check6");
			parseAssign(data);
			// System.out.println("check8");
			// log.addText("check7");
			parseRateAssign(data);
			// System.out.println("check9");
			// log.addText("check8");
			parseDelayAssign(data);
			// parseIntAssign(data);
			// System.out.println("check0");
			// log.addText("check9");
			parseBooleanAssign(data);
			parseTransitionRate(data);
			// System.out.println("check11");
			// log.addText("check0");
			// log.addText(intAssignments.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
			// throw new IllegalArgumentException("Unable to parse LHPN");
		}
	}

	public void printDot(String filename) {
		try {
			String file = filename;
			PrintStream p = new PrintStream(new FileOutputStream(filename));
			StringBuffer buffer = new StringBuffer();
			buffer.append("digraph G\nsize=\"7.5,10\"\n");
			for (String s : delays.keySet()) {
				buffer.append(s + " [shape=plaintext,label=\"" + s);
				if (enablings.containsKey(s)) {
					buffer.append("\\n{" + enablings.get(s) + "}");
				}
				if (delays.containsKey(s)) {
					buffer.append("\\n[" + delays.get(s) + "]");
				}
				if (contAssignments.containsKey(s) || intAssignments.containsKey(s)
						|| booleanAssignments.containsKey(s)) {
					buffer.append("\\n<");
					boolean flag = false;
					if (booleanAssignments.containsKey(s)) {
						Properties prop = booleanAssignments.get(s);
						for (Object t : prop.keySet()) {
							if (!flag) {
								buffer.append(",");
							}
							else {
								flag = true;
							}
							buffer.append(t.toString() + ":="
									+ booleanAssignments.get(t.toString()));
						}
					}
					if (contAssignments.containsKey(s)) {
						Properties prop = contAssignments.get(s);
						for (Object t : prop.keySet()) {
							if (!flag) {
								buffer.append(",");
							}
							else {
								flag = true;
							}
							buffer.append(t.toString() + ":=" + prop.getProperty(t.toString()));
						}
					}
					if (intAssignments.containsKey(s)) {
						Properties prop = intAssignments.get(s);
						for (Object t : prop.keySet()) {
							if (!flag) {
								buffer.append(",");
							}
							else {
								flag = true;
							}
							buffer.append(t.toString() + ":=" + prop.getProperty(t.toString()));
						}
					}
					buffer.append(">\"");
				}
				buffer.append("];\n");
			}
			for (String s : places.keySet()) {
				buffer.append(s + " [label=\"" + s + "\"];\n" + s
						+ " [shape=circle,width=0.40,height=0.40]\n");
				if (places.get(s)) {
					buffer
							.append(s
									+ " [height=.3,width=.3,peripheries=2,style=filled,color=black,fontcolor=white];\n");
				}
			}
			for (String s : controlFlow.keySet()) {
				String[] postset = controlFlow.get(s).getProperty("postset").split(" ");
				for (String t : postset) {
					buffer.append(s + " -> " + t + "\n");
				}
			}
			for (String s : controlPlaces.keySet()) {
				String[] postset = controlFlow.get(s).getProperty("postset").split(" ");
				for (String t : postset) {
					buffer.append(s + " -> " + t + "\n");
				}
			}
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

	public void addPlace(String name, Boolean ic) {
		places.put(name, ic);
	}

	public void removePlace(String name) {
		if (name != null && places.containsKey(name)) {
			places.remove(name);
			// controlPlaces.remove(name);
		}
		// for (String s : controlFlow.keySet()) {
		// Properties prop = controlFlow.get(s);
		// String[] array = prop.getProperty("preset").split(" ");
		// String setString = new String();
		// int offset = 0;
		// for (int i = 1; i < array.length; i++) {
		// if (arra[i].equals(name)) {
		// offset += 1;
		// }
		// setString = setString + " " + array[i - offset];
		// }
		// prop.setProperty("preset", setString);
		// array = prop.getProperty("postset").split(" ");
		// setString = new String();
		// offset = 0;
		// for (int i = 1; i < array.length; i++) {
		// if (array[i].equals(name)) {
		// offset += 1;
		// }
		// setString = setString + " " + array[i - offset];
		// }
		// prop.setProperty("postset", setString);
		// controlFlow.put(s, prop);
		// }
	}

	public void renamePlace(String oldName, String newName, Boolean ic) {
		if (oldName != null && places.containsKey(oldName)) {
			places.put(newName, ic);
			places.remove(oldName);
			controlPlaces.put(newName, controlPlaces.get(oldName));
			controlPlaces.remove(oldName);
		}
		for (String s : controlFlow.keySet()) {
			Properties prop = controlFlow.get(s);
			String[] array = prop.getProperty("preset").split(" ");
			String setString = new String();
			for (int i = 1; i < array.length; i++) {
				if (array[i].equals(oldName)) {
					array[i] = newName;
				}
				setString = setString + " " + array[i];
			}
			prop.setProperty("preset", setString);
			array = prop.getProperty("postset").split(" ");
			setString = new String();
			for (int i = 1; i < array.length; i++) {
				if (array[i].equals(oldName)) {
					array[i] = newName;
				}
				setString = setString + " " + array[i];
			}
			prop.setProperty("postset", setString);
			controlFlow.put(s, prop);
		}
	}

	public void addInput(String name, String ic) {
		inputs.put(name, ic);
	}

	public void removeInput(String name) {
		if (name != null && inputs.containsKey(name)) {
			inputs.remove(name);
		}
	}

	public void addOutput(String name, String ic) {
		outputs.put(name, ic);
	}

	public void removeOutput(String name) {
		if (name != null && outputs.containsKey(name)) {
			outputs.remove(name);
		}
	}

	// public void addFlow(String from, String to) {
	// String name = "";
	// Properties flow = new Properties();
	// if (controlFlow.containsKey(from)) {
	// name = from;
	// flow = controlFlow.get(from);
	// flow.setProperty(name + "postset", flow.getProperty(name + "postset") +
	// to + " ");
	// }
	// else if (controlFlow.containsKey(to)) {
	// name = to;
	// flow = controlFlow.get(to);
	// flow.setProperty(name + , flow.getProperty(name + ) + to + "
	// ");
	// }
	// controlFlow.put(name, flow);
	// }

	// public void removeFlow(String from, String to) {
	// String name = "";
	// Properties flow = new Properties();
	// if (controlFlow.containsKey(from)) {
	// name = from;
	// flow = controlFlow.get(from);
	// String toString = flow.getProperty("postset");
	// String[] toArray = toString.split("\\s");
	// boolean flag = false;
	// for (int i = 0; i < toArray.length; i++) {
	// if (flag) {
	// toArray[i - 1] = toArray[i];
	// }
	// if (toArray[i].equals(to)) {
	// flag = true;
	// }
	// }
	// flow.setProperty(name + "postset", "");
	// for (int i = 0; i < toArray.length - 1; i++) {
	// flow.setProperty("postset", flow.getProperty("postset") + toArray[i] + "
	// ");
	// }
	// }
	// else if (controlFlow.containsKey(to)) {
	// name = to;
	// flow = controlFlow.get(to);
	// String fromString = flow.getProperty();
	// String[] fromArray = fromString.split("\\s");
	// boolean flag = false;
	// for (int i = 0; i < fromArray.length; i++) {
	// if (flag) {
	// fromArray[i - 1] = fromArray[i];
	// }
	// if (fromArray[i].equals(to)) {
	// flag = true;
	// }
	// }
	// flow.setProperty("preset", "");
	// for (int i = 0; i < fromArray.length - 1; i++) {
	// flow.setProperty("preset", flow.getProperty("preset") + fromArray[i] + "
	// ");
	// }
	// }
	// }

	public void addTransition(String name) {
		controlFlow.put(name, null);
	}

	public void addTransition(String name, Properties prop) {
		controlFlow.put(name, prop);
	}

	public void addControlFlow(String fromName, String toName) {
		// log.addText(fromName+toName);
		if (isTransition(fromName)) {
			Properties propTrans = new Properties();
			Properties propPlace = new Properties();
			String list = "";
			String placeList = "";
			if (controlFlow.containsKey(fromName)) {
				if (controlFlow.get(fromName) != null) {
					propTrans = controlFlow.get(fromName);
					list = propTrans.getProperty("postset");
					if (list != null) {
					//if (!list.equals("null")) {
						list = list + " " + toName;
					}
					else {
						list = toName;
					}
					//}
				}
			}
			else {
				list = toName;
			}
			if (controlPlaces.containsKey(toName)) {
				if (controlPlaces.get(toName) != null) {
					propPlace = controlPlaces.get(toName);
					placeList = propPlace.getProperty("preset");
					if (placeList != null) {
					//if (!placeList.equals("null")) {
						placeList = placeList + " " + fromName;
					}
					else {
						placeList = fromName;
					}
					//}
				}
			}
			else {
				placeList = fromName;
			}
			// log.addText(list);
			// System.out.println(prop == null);
			propTrans.setProperty("postset", list);
			controlFlow.put(fromName, propTrans);
			propPlace.setProperty("preset", placeList);
			controlPlaces.put(toName, propPlace);
		}
		else {
			Properties propTrans = new Properties();
			Properties propPlace = new Properties();
			String list = "";
			String placeList = "";
			if (controlFlow.containsKey(toName)) {
				if (controlFlow.get(toName) != null) {
					propTrans = controlFlow.get(toName);
					list = propTrans.getProperty("preset");
					if (list != null) {
						list = list + " " + fromName;
					}
					else {
						list = fromName;
					}
				}
				else {
					list = fromName;
				}
			}
			else {
				list = fromName;
			}
			if (controlPlaces.containsKey(fromName)) {
				if (controlPlaces.get(fromName) != null) {
					propPlace = controlPlaces.get(fromName);
					placeList = propPlace.getProperty("postset");
					if (placeList != null) {
						placeList = placeList + " " + toName;
					}
					else {
						placeList = toName;
					}
				}
				else {
					placeList = toName;
				}
			}
			else {
				placeList = toName;
			}
			propTrans.setProperty("preset", list);
			controlFlow.put(toName, propTrans);
			propPlace.setProperty("postset", list);
			controlPlaces.put(fromName, propPlace);
		}
	}

	public void removeControlFlow(String fromName, String toName) {
		// System.out.println(fromName + toName);
		if (isTransition(fromName)) {
			Properties prop = new Properties();
			if (controlFlow.get(fromName) != null) {
				prop = controlFlow.get(fromName);
			}
			String[] list = prop.getProperty("postset").split("\\s");
			String[] toList = new String[list.length - 1];
			Boolean flag = false;
			for (int i = 0; i < list.length - 1; i++) {
				if (toName.equals(list[i])) {
					flag = true;
				}
				else {
					if (flag) {
						toList[i - 1] = list[i];
					}
					else {
						toList[i] = list[i];
					}
				}
			}
			if (toList.length > 0) {
				prop.put("postset", toList);
			}
			else {
				prop.remove("postset");
			}
			controlFlow.put(fromName, prop);
			if (controlPlaces.get(toName) != null) {
				prop = controlPlaces.get(toName);
			}
			list = prop.getProperty("preset").split("\\s");
			String[] fromList = new String[list.length - 1];
			flag = false;
			for (int i = 0; i < list.length - 1; i++) {
				if (toName.equals(list[i])) {
					flag = true;
				}
				else {
					if (flag) {
						fromList[i - 1] = list[i];
					}
					else {
						fromList[i] = list[i];
					}
				}
			}
			if (fromList.length > 0) {
				prop.put("preset", fromList);
			}
			else {
				prop.remove("preset");
			}
			controlPlaces.put(toName, prop);
		}
		else {
			Properties prop = new Properties();
			if (controlFlow.get(toName) != null) {
				prop = controlFlow.get(toName);
			}
			String[] list = prop.getProperty("preset").split("\\s");
			String[] fromList = new String[list.length - 1];
			Boolean flag = false;
			for (int i = 0; i < list.length - 1; i++) {
				if (toName.equals(list[i])) {
					flag = true;
				}
				else {
					if (flag) {
						fromList[i - 1] = list[i];
					}
					else {
						fromList[i] = list[i];
					}
				}
			}
			if (fromList.length > 0) {
				prop.put("preset", fromList);
			}
			else {
				prop.remove("preset");
			}
			controlPlaces.put(toName, prop);
			if (controlPlaces.get(fromName) != null) {
				prop = controlPlaces.get(fromName);
			}
			if (prop.getProperty("postset") != null) {
				list = prop.getProperty("postset").split("\\s");
				String[] toList = new String[list.length - 1];
				flag = false;
				for (int i = 0; i < list.length - 1; i++) {
					if (toName.equals(list[i])) {
						flag = true;
					}
					else {
						if (flag) {
							toList[i - 1] = list[i];
						}
						else {
							toList[i] = list[i];
						}
					}
				}
				if (toList.length > 0) {
					prop.put("postset", toList);
				}
				else {
					prop.remove("postset");
				}
				controlPlaces.put(fromName, prop);
			}
		}
	}

	public boolean containsFlow(String fromName, String toName) {
		if (isTransition(fromName)) {
			Properties prop = controlFlow.get(fromName);
			if (prop != null) {
				if (prop.getProperty("postset") != null) {
					String[] list = prop.getProperty("postset").split("\\s");
					for (int i = 0; i < list.length; i++) {
						if (list[i].equals(toName)) {
							return true;
						}
					}
				}
			}
			return false;
		}
		else {
			Properties prop = controlFlow.get(toName);
			if (prop != null) {
				if (prop.getProperty("preset") != null) {
					String[] list = prop.getProperty("preset").split("\\s");
					for (int i = 0; i < list.length; i++) {
						if (list[i].equals(fromName)) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	public boolean containsFlow(String place) {
		if (controlFlow.containsKey(place) && controlFlow.get(place) != null) {
			if (!controlFlow.get(place).isEmpty()) {
				return true;
			}
		}
		for (String s : controlFlow.keySet()) {
			if (controlFlow.get(s) != null) {
				Properties prop = controlFlow.get(s);
				if (prop.containsKey("postset")) {
					String[] toList = prop.get("postset").toString().split(" ");
					for (int i = 0; i < toList.length; i++) {
						if (toList[i].equals(place)) {
							return true;
						}
					}
				}
				if (prop.containsKey("preset")) {
					String[] fromList = prop.get("preset").toString().split(" ");
					for (int i = 0; i < fromList.length; i++) {
						if (fromList[i].equals(place)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public void addTransition(String name, String delay, String transitionRate,
			Properties rateAssign, Properties booleanAssign, String enabling) {
		addTransition(name);
		delays.put(name, delay);
		ExprTree[] array = new ExprTree[2];
		Pattern rangePattern = Pattern.compile(RANGE);
		Matcher rangeMatcher = rangePattern.matcher(delay);
		// array[0].token = array[0].intexpr_gettok(rangeMatcher.group(1));
		// if (!rangeMatcher.group(1).equals("")) {
		// array[0].intexpr_L(rangeMatcher.group(1));
		// }
		// array[1].token = array[1].intexpr_gettok(rangeMatcher.group(2));
		// if (!rangeMatcher.group(2).equals("")) {
		// array[1].intexpr_L(rangeMatcher.group(2));
		// }
		// delayTrees.put(name, array);
		ExprTree expr = new ExprTree(this);
		expr.token = expr.intexpr_gettok(transitionRate);
		if (!transitionRate.equals("")) {
			expr.intexpr_L(transitionRate);
		}
		else {
			expr = null;
		}
		transitionRateTrees.put(name, expr);
		transitionRates.put(name, transitionRate);
		rateAssignments.put(name, rateAssign);
		HashMap<String, ExprTree[]> assignments = new HashMap<String, ExprTree[]>();
		for (Object o : rateAssign.keySet()) {
			String s = o.toString();
			rangePattern = Pattern.compile(RANGE);
			rangeMatcher = rangePattern.matcher(delay);
			if (rangeMatcher.find()) {
				array = new ExprTree[2];
				array[0] = new ExprTree(this);
				array[1] = new ExprTree(this);
				array[0].token = array[0].intexpr_gettok(rangeMatcher.group(1));
				if (!rangeMatcher.group(1).equals("")) {
					array[0].intexpr_L(rangeMatcher.group(1));
				}
				array[1].token = array[1].intexpr_gettok(rangeMatcher.group(2));
				if (!rangeMatcher.group(2).equals("")) {
					array[1].intexpr_L(rangeMatcher.group(2));
				}
			}
			else {
				expr = new ExprTree(this);
				expr.token = expr.intexpr_gettok(transitionRate);
				if (!transitionRate.equals("")) {
					expr.intexpr_L(transitionRate);
				}
				array[0] = expr;
				array[1] = null;
				assignments.put(s, array);
			}
			assignments.put(s, array);
		}
		rateAssignmentTrees.put(name, assignments);
		booleanAssignments.put(name, booleanAssign);
		assignments = new HashMap<String, ExprTree[]>();
		for (Object o : rateAssign.keySet()) {
			String s = o.toString();
			rangePattern = Pattern.compile(RANGE);
			rangeMatcher = rangePattern.matcher(delay);
			if (rangeMatcher.find()) {
				array = new ExprTree[2];
				array[0] = new ExprTree(this);
				array[1] = new ExprTree(this);
				array[0].token = array[0].intexpr_gettok(rangeMatcher.group(1));
				if (!rangeMatcher.group(1).equals("")) {
					array[0].intexpr_L(rangeMatcher.group(1));
				}
				array[1].token = array[1].intexpr_gettok(rangeMatcher.group(2));
				if (!rangeMatcher.group(2).equals("")) {
					array[1].intexpr_L(rangeMatcher.group(2));
				}
			}
			else {
				expr = new ExprTree(this);
				expr.token = expr.intexpr_gettok(transitionRate);
				if (!transitionRate.equals("")) {
					expr.intexpr_L(transitionRate);
				}
				array[0] = expr;
				array[1] = null;
				assignments.put(s, array);
			}
			assignments.put(s, array);
		}
		booleanAssignmentTrees.put(name, assignments);
		enablings.put(name, enabling);
		expr = new ExprTree(this);
		expr.token = expr.intexpr_gettok(enabling);
		expr.intexpr_L(enabling);
		enablingTrees.put(name, expr);
		contAssignments.put(name, null);
		contAssignmentTrees.put(name, null);
		intAssignments.put(name, null);
		intAssignmentTrees.put(name, null);
	}

	public void removeTransition(String name) {
		controlFlow.remove(name);
		delays.remove(name);
		// delayTrees.remove(name);
		transitionRateTrees.remove(name);
		transitionRates.remove(name);
		rateAssignments.remove(name);
		rateAssignmentTrees.remove(name);
		booleanAssignments.remove(name);
		booleanAssignmentTrees.remove(name);
		enablings.remove(name);
		enablingTrees.remove(name);
		contAssignments.remove(name);
		contAssignmentTrees.remove(name);
		intAssignments.remove(name);
		intAssignmentTrees.remove(name);
	}

	public void addTransitionRate(String name, String transitionRate) {
		ExprTree expr = new ExprTree(this);
		expr.token = expr.intexpr_gettok(transitionRate);
		if (!transitionRate.equals("")) {
			expr.intexpr_L(transitionRate);
		}
		else {
			expr = null;
		}
		transitionRateTrees.put(name, expr);
		transitionRates.put(name, transitionRate);
	}

	public void addEnabling(String name, String cond) {
		enablings.put(name, cond);
		ExprTree expr = new ExprTree(this);
		expr.token = expr.intexpr_gettok(cond);
		if (!cond.equals("")) {
			expr.intexpr_L(cond);
			enablingTrees.put(name, expr);
		}
		else {
			enablingTrees.put(name, null);
		}
	}

	public void removeEnabling(String name) {
		if (name != null && enablings.containsKey(name)) {
			enablings.remove(name);
		}
		if (name != null && enablingTrees.containsKey(name)) {
			enablingTrees.remove(name);
		}
	}

	public void addVar(String name, Properties initCond) {
		variables.put(name, initCond);
	}

	public int removeVar(String name) {
		int flag = 0;
		for (String s : booleanAssignments.keySet()) {
			Properties prop = booleanAssignments.get(s);
			for (Object object : prop.keySet()) {
				String propName = object.toString();
				if (propName.equals(name)) {
					flag = 1;
				}
			}
		}
		for (String s : contAssignments.keySet()) {
			Properties prop = contAssignments.get(s);
			for (Object object : prop.keySet()) {
				String propName = object.toString();
				if (propName.equals(name)) {
					flag = 1;
				}
			}
		}
		for (String s : rateAssignments.keySet()) {
			Properties prop = rateAssignments.get(s);
			for (Object object : prop.keySet()) {
				String propName = object.toString();
				if (propName.equals(name)) {
					flag = 1;
				}
			}
		}
		for (String s : integers.keySet()) {
			Properties prop = intAssignments.get(s);
			if (prop != null) {
				for (Object object : prop.keySet()) {
					String propName = object.toString();
					if (propName.equals(name)) {
						flag = 1;
					}
				}
			}
		}
		if (flag == 0 && name != null && variables.containsKey(name)) {
			variables.remove(name);
		}
		else if (flag == 0 && name != null && inputs.containsKey(name)) {
			inputs.remove(name);
		}
		else if (flag == 0 && name != null && outputs.containsKey(name)) {
			outputs.remove(name);
		}
		else if (flag == 0 && name != null && integers.containsKey(name)) {
			integers.remove(name);
		}
		return flag;
	}

	public void addInteger(String name, String ic) {
		integers.put(name, ic);
	}

	public boolean addRateAssign(String transition, String name, String value) {
		ExprTree[] expr = new ExprTree[2];
		expr[0] = new ExprTree(this);
		expr[1] = new ExprTree(this);
		Pattern pattern = Pattern.compile("\\[(\\S+?),(\\S+?)\\]");
		Matcher matcher = pattern.matcher(value);
		if (matcher.find()) {
			expr[0].token = expr[0].intexpr_gettok(matcher.group(1));
			if (!matcher.group(1).equals("")) {
				if (!expr[0].intexpr_L(matcher.group(1))) return false;
			}
			else {
				expr[0] = null;
			}
			expr[1].token = expr[1].intexpr_gettok(matcher.group(2));
			if (!matcher.group(2).equals("")) {
				if (!expr[1].intexpr_L(matcher.group(2))) return false;
			}
			else {
				expr[1] = null;
			}
		}
		else {
			expr[0].token = expr[0].intexpr_gettok(value);
			if (!value.equals("")) {
				if (!expr[0].intexpr_L(value)) return false;
			}
			else {
				expr[0] = null;
				expr[1] = null;
			}
		}
		HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
		if (rateAssignmentTrees.get(transition) != null) {
			map = rateAssignmentTrees.get(transition);
		}
		map.put(name, expr);
		rateAssignmentTrees.put(transition, map);
		Properties prop = new Properties();
		if (rateAssignments.get(transition) != null) {
			prop = rateAssignments.get(transition);
		}
		// System.out.println("here " + transition + name + value);
		prop.setProperty(name, value);
		// log.addText("lhpn " + prop.toString());
		rateAssignments.put(transition, prop);
		return true;
	}

	public void removeRateAssign(String transition, String name) {
		if (rateAssignmentTrees.containsKey(transition)) {
			HashMap<String, ExprTree[]> map = rateAssignmentTrees.get(transition);
			if (name != null && map.containsKey(name)) {
				map.remove(name);
			}
			rateAssignmentTrees.put(transition, map);
		}
		if (rateAssignments.containsKey(transition)) {
			Properties prop = rateAssignments.get(transition);
			if (name != null && prop.containsKey(name)) {
				prop.remove(name);
			}
			rateAssignments.put(transition, prop);
		}
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

	public void removeBoolAssign(String transition, String name) {
		Properties prop = booleanAssignments.get(transition);
		if (name != null && prop.containsKey(name)) {
			prop.remove(name);
		}
		booleanAssignments.put(transition, prop);
		if (booleanAssignmentTrees.containsKey(transition)) {
			HashMap<String, ExprTree[]> map = booleanAssignmentTrees.get(transition);
			if (name != null && map.containsKey(name)) {
				map.remove(name);
			}
			booleanAssignmentTrees.put(transition, map);
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
				if (!expr[0].intexpr_L(matcher.group(1))) return false;
			}
			else {
				expr[0] = null;
			}
			expr[1].token = expr[1].intexpr_gettok(matcher.group(2));
			if (!matcher.group(2).equals("")) {
				if (!expr[1].intexpr_L(matcher.group(2))) return false;
			}
			else {
				expr[1] = null;
			}
		}
		else {
			expr[0].token = expr[0].intexpr_gettok(value);
			if (!value.equals("")) {
				if (!expr[0].intexpr_L(value)) return false;
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

	public void removeContAssign(String transition, String name) {
		if (contAssignments.containsKey(transition)) {
			Properties prop = contAssignments.get(transition);
			if (name != null && prop.containsKey(name)) {
				prop.remove(name);
			}
			// log.addText("lhpn " + prop.toString());
			contAssignments.put(transition, prop);
		}
		if (contAssignmentTrees.containsKey(transition)) {
			HashMap<String, ExprTree[]> map = contAssignmentTrees.get(transition);
			if (name != null && map.containsKey(name)) {
				map.remove(name);
			}
			contAssignmentTrees.put(transition, map);
		}
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
				if (!expr[0].intexpr_L(matcher.group(1))) return false;
			}
			else {
				expr[0] = null;
			}
			expr[1].token = expr[1].intexpr_gettok(matcher.group(2));
			if (!matcher.group(2).equals("")) {
				if (!expr[1].intexpr_L(matcher.group(2))) return false;
			}
			else {
				expr[1] = null;
			}
		}
		else {
			expr[0].token = expr[0].intexpr_gettok(value);
			if (!value.equals("")) {
				if (!expr[0].intexpr_L(value)) return false;
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

	public void removeIntAssign(String transition, String name) {
		if (intAssignments.containsKey(transition)) {
			Properties prop = intAssignments.get(transition);
			if (name != null && prop.containsKey(name)) {
				prop.remove(name);
			}
			intAssignments.put(transition, prop);
		}
		if (intAssignmentTrees.containsKey(transition)) {
			HashMap<String, ExprTree[]> map = intAssignmentTrees.get(transition);
			if (name != null && map.containsKey(name)) {
				map.remove(name);
			}
			intAssignmentTrees.put(transition, map);
		}
	}

	public void removeAllAssign(String transition) {
		if (booleanAssignments.containsKey(transition)) {
			Properties prop = new Properties();
			booleanAssignments.put(transition, prop);
		}
		if (booleanAssignmentTrees.containsKey(transition)) {
			HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
			booleanAssignmentTrees.put(transition, map);
		}
		if (contAssignments.containsKey(transition)) {
			Properties prop = new Properties();
			contAssignments.put(transition, prop);
		}
		if (contAssignmentTrees.containsKey(transition)) {
			HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
			contAssignmentTrees.put(transition, map);
		}
		if (rateAssignments.containsKey(transition)) {
			Properties prop = new Properties();
			rateAssignments.put(transition, prop);
		}
		if (rateAssignmentTrees.containsKey(transition)) {
			HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
			rateAssignmentTrees.put(transition, map);
		}
		if (intAssignments.containsKey(transition)) {
			Properties prop = new Properties();
			intAssignments.put(transition, prop);
		}
		if (intAssignmentTrees.containsKey(transition)) {
			HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
			intAssignmentTrees.put(transition, map);
		}
	}

	public void addProperty(String newProperty) {
		property = newProperty;
	}

	public void removeProperty() {
		property = "";
	}

	public String getProperty() {
		return property;
	}

	public void changeVariableName(String oldName, String newName) {
		if (isContinuous(oldName)) {
			variables.put(newName, variables.get(oldName));
			variables.remove(oldName);
		}
		else if (isInput(oldName)) {
			inputs.put(newName, inputs.get(oldName));
			inputs.remove(oldName);
		}
		else if (isInteger(oldName)) {
			integers.put(newName, integers.get(oldName));
			integers.remove(oldName);
		}
		else {
			outputs.put(newName, outputs.get(oldName));
			outputs.remove(oldName);
		}
	}

	public void changeTransitionName(String oldName, String newName) {
		controlFlow.put(newName, controlFlow.get(oldName));
		controlFlow.remove(oldName);
		delays.put(newName, delays.get(oldName));
		delays.remove(oldName);
		// delayTrees.put(newName, delayTrees.get(oldName));
		// delayTrees.remove(oldName);
		transitionRateTrees.put(newName, transitionRateTrees.get(oldName));
		transitionRateTrees.remove(oldName);
		transitionRates.put(newName, transitionRates.get(oldName));
		transitionRates.remove(oldName);
		rateAssignments.put(newName, rateAssignments.get(oldName));
		rateAssignments.remove(oldName);
		rateAssignmentTrees.put(newName, rateAssignmentTrees.get(oldName));
		rateAssignmentTrees.remove(oldName);
		contAssignments.put(newName, contAssignments.get(oldName));
		contAssignments.remove(oldName);
		contAssignmentTrees.put(newName, contAssignmentTrees.get(oldName));
		contAssignmentTrees.remove(oldName);
		intAssignments.put(newName, intAssignments.get(oldName));
		intAssignments.remove(oldName);
		intAssignmentTrees.put(newName, intAssignmentTrees.get(oldName));
		intAssignmentTrees.remove(oldName);
		booleanAssignments.put(newName, booleanAssignments.get(oldName));
		booleanAssignments.remove(oldName);
		booleanAssignmentTrees.put(newName, booleanAssignmentTrees.get(oldName));
		booleanAssignmentTrees.remove(oldName);
		enablings.put(newName, enablings.get(oldName));
		enablings.remove(oldName);
		enablingTrees.put(newName, enablingTrees.get(oldName));
		enablingTrees.remove(oldName);
	}

	public boolean changeDelay(String transition, String delay) {
		if (delays.containsKey(transition)) {
			delays.remove(transition);
		}
		// log.addText(transition + delay);
		delays.put(transition, delay);
		// if (delayTrees.containsKey(transition)) {
		// delayTrees.remove(transition);
		// }
		// ExprTree expr = new ExprTree(this);
		// expr.token = expr.intexpr_gettok(delay);
		// if (!delay.equals("")) {
		// if (!expr.intexpr_L(delay))
		// return false;
		// }
		// ExprTree[] array = { expr };
		// delayTrees.put(transition, array);
		return true;
	}

	public boolean changeTransitionRate(String transition, String rate) {
		ExprTree expr = new ExprTree(this);
		if (!rate.equals("")) {
			expr.token = expr.intexpr_gettok(rate);
			if (!(expr.intexpr_L(rate)))
				return false;
			if (transitionRates.containsKey(transition)) {
				transitionRates.remove(transition);
			}
			if (transitionRateTrees.containsKey(transition)) {
				transitionRateTrees.remove(transition);
			}
			transitionRateTrees.put(transition, expr);
			transitionRates.put(transition, rate);
		}
		else {
			transitionRates.remove(transition);
			transitionRateTrees.put(transition, null);
		}
		return true;
	}

	public boolean changeEnabling(String transition, String enabling) {
		ExprTree expr = new ExprTree(this);
		if (!enabling.equals("")) {
			expr.token = expr.intexpr_gettok(enabling);
			if (!expr.intexpr_L(enabling))
				return false;
			if (enablings.containsKey(transition)) {
				enablings.remove(transition);
			}
			if (enablingTrees.containsKey(transition)) {
				enablingTrees.remove(transition);
			}
			enablingTrees.put(transition, expr);
			enablings.put(transition, enabling);
		}
		else {
			enablings.remove(transition);
			enablingTrees.put(transition, null);
		}
		return true;
	}

	public void changeInitialMarking(String p,boolean ic){  // SB
		places.remove(p);
		places.put(p, ic);
	}
	
	public String[] getAllIDs() {
		String[] allVariables = new String[variables.size() + integers.size() + inputs.size()
				+ outputs.size() + delays.size() + places.size()];
		int i = 0;
		for (String s : variables.keySet()) {
			allVariables[i] = s;
			i++;
		}
		for (String s : integers.keySet()) {
			allVariables[i] = s;
			i++;
		}
		for (String s : inputs.keySet()) {
			allVariables[i] = s;
			i++;
		}
		for (String s : outputs.keySet()) {
			allVariables[i] = s;
			i++;
		}
		for (String s : delays.keySet()) {
			allVariables[i] = s;
			i++;
		}
		for (String s : places.keySet()) {
			allVariables[i] = s;
			i++;
		}
		return allVariables;
	}

	public String[] getAllVariables() {
		String[] allVariables = new String[variables.size() + integers.size() + inputs.size()
				+ outputs.size() + delays.size() + places.size()];
		int i = 0;
		for (String s : variables.keySet()) {
			allVariables[i] = s;
			i++;
		}
		for (String s : integers.keySet()) {
			allVariables[i] = s;
			i++;
		}
		for (String s : inputs.keySet()) {
			allVariables[i] = s;
			i++;
		}
		for (String s : outputs.keySet()) {
			allVariables[i] = s;
			i++;
		}
		return allVariables;
	}

	public HashMap<String, Properties> getVariables() {
		return variables;
	}

	public HashMap<String, String> getIntegers() {
		return integers;
	}

	public HashMap<String, String> getInputs() {
		return inputs;
	}

	public HashMap<String, String> getOutputs() {
		return outputs;
	}

	public HashMap<String, Boolean> getPlaces() {
		return places;
	}

	public HashMap<String, String> getDelays() {
		return delays;
	}

	public String getDelay(String var) {
		return delays.get(var);
	}

	// public ExprTree[] getDelayTree(String var) {
	// return delayTrees.get(var);
	// }

	public HashMap<String, ExprTree> getTransitionRates() {
		return transitionRateTrees;
	}

	public HashMap<String, String> getTransitionRateStrings() {
		return transitionRates;
	}

	public ExprTree getTransitionRateTree(String var) {
		return transitionRateTrees.get(var);
	}

	public String getTransitionRate(String var) {
		return transitionRates.get(var);
	}

	public String[] getControlFlow() {
		String retString = "";
		for (String s : controlFlow.keySet()) {
			// log.addText("key: " + s);
			Properties prop = controlFlow.get(s);
			String toString = prop.getProperty("postset");
			// log.addText("toString " + toString);
			if (toString != null) {
				String[] toArray = toString.split("\\s");
				for (int i = 0; i < toArray.length; i++) {
					retString = retString + s + " " + toArray[i] + "\n";
				}
			}
			// log.addText("getfrom");
			String fromString = prop.getProperty("preset");
			if (fromString != null) {
				// log.addText("fromString " + fromString);
				String[] fromArray = fromString.split("\\s");
				for (int i = 0; i < fromArray.length; i++) {
					retString = retString + fromArray[i] + " " + s + "\n";
				}
			}
		}
		return retString.split("\\n");
	}
/*
	public String[] getPreset(String node) {
		if (isTransition(node)) {
			if (controlFlow.get(node).containsKey("preset")) {
				return controlFlow.get(node).getProperty("preset").split(" ");
			}
			else {
				return new String[0];
			}
		}
		else {
			if (controlPlaces.get(node).containsKey("preset")) {
				return controlPlaces.get(node).getProperty("preset").split(" ");
			}
			else {
				return new String[0];
			}
		}
	}
*/
	public String[] getPreset(String node) {
		if (isTransition(node)) {
			if (controlFlow.get(node).containsKey("preset")) {
				return controlFlow.get(node).getProperty("preset").split(" ");
			}
			else {
				return new String[0];
			}
		}
		else if (controlPlaces.get(node).containsKey("preset")) {
				return controlPlaces.get(node).getProperty("preset").split(" ");
		}
		else {
			Pattern p = Pattern.compile(node);
			for (String st: getTransitionList()){
				Matcher matcher = p.matcher(st);
				if (matcher.find()){
					String[] pre_trans = new String[0];
					pre_trans[0] = st;
					return pre_trans;
				}
			}
			return new String[0];
			
		}
	}
	public String[] getPostset(String node) {
		if (isTransition(node)) {
			if (controlFlow.get(node).containsKey("postset")) {
				return controlFlow.get(node).getProperty("postset").split(" ");
			}
			else {
				return new String[0];
			}
		}
		else {
			if (controlPlaces.get(node).containsKey("postset")) {
				return controlPlaces.get(node).getProperty("postset").split(" ");
			}
			else {
				return new String[0];
			}
		}
	}

	public String getInitialVal(String var) {
		if (isContinuous(var)) {
			Properties prop = variables.get(var);
			return prop.getProperty("value");
		}
		else if (isInteger(var)) {
			String integer = integers.get(var);
			return integer;
		}
		else if (isInput(var)) {
			return inputs.get(var);
		}
		else if (isOutput(var)) {
			return outputs.get(var);
		}
		else {
			return "";
		}
	}

	public boolean getPlaceInitial(String var) {
		return places.get(var);
	}

	public String getInitialRate(String var) {
		Properties prop = variables.get(var);
		return prop.getProperty("rate");
	}

	public String getEnabling(String var) {
		return enablings.get(var);
	}

	public ExprTree getEnablingTree(String var) {
		return enablingTrees.get(var);
	}

	public String[] getBooleanVars(String trans) {
		if (booleanAssignments.containsKey(trans)) {
			Properties prop = booleanAssignments.get(trans);
			String[] assignArray = new String[prop.size()];
			int i = 0;
			for (Object s : prop.keySet()) {
				if (prop.get(s) != null) {
					if (isInput(s.toString()) || isOutput(s.toString())) {
						assignArray[i] = s.toString();
						i++;
					}
				}
			}
			return assignArray;
		}
		return null;
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

	public Properties getContVars(String trans) {
		Properties contVars = new Properties();
		contVars = contAssignments.get(trans);
		// log.addText("lhpn " + contVars.toString());
		return contVars;
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

	public Properties getIntVars(String trans) {
		// log.addText(trans);
		Properties intVars = new Properties();
		intVars = intAssignments.get(trans);
		if (intVars != null) {
			String[] tempArray = new String[intVars.keySet().size()];
			int i = 0;
			for (Object s : intVars.keySet()) {
				String t = (String) s;
				if (!isInteger(t)) {
					tempArray[i] = t;
					i++;
				}
			}
			for (i = 0; i < tempArray.length; i++) {
				if (tempArray[i] != null) {
					intVars.remove(tempArray[i]);
				}
				else {
					break;
				}
			}
			return intVars;
		}
		return null;
	}

	public String[] getContAssignVars(String trans) {
		// log.addText(trans);
		if (contAssignments.containsKey(trans)) {
			Properties prop = contAssignments.get(trans);
			String[] assignArray = new String[prop.size()];
			int i = 0;
			for (Object s : prop.keySet()) {
				if (isContinuous(s.toString())) {
					assignArray[i] = s.toString();
					i++;
				}
			}
			// Properties prop = booleanAssignments.get(var);
			// prop.setProperty("type", "boolean");
			return assignArray;
		}
		else {
			// log.addText("returning null");
			return null;
		}
	}

	public String[] getRateVars(String trans) {
		if (rateAssignments.containsKey(trans)) {
			Properties prop = rateAssignments.get(trans);
			String[] assignArray = new String[prop.size()];
			int i = 0;
			for (Object s : prop.keySet()) {
				if (isContinuous(s.toString())) {
					assignArray[i] = s.toString();
					i++;
				}
			}
			// Properties prop = booleanAssignments.get(var);
			// prop.setProperty("type", "boolean");
			return assignArray;
		}
		return null;
	}

	public String getBoolAssign(String trans, String var) {
		if (booleanAssignments.containsKey(trans)) {
			Properties prop = booleanAssignments.get(trans);
			if (prop != null && var != null) {
				if (prop.containsKey(var)) {
					return prop.getProperty(var);
				}
			}
		}
		return null;
	}

	public ExprTree[] getBoolAssignTree(String transition, String var) {
		HashMap<String, ExprTree[]> map = booleanAssignmentTrees.get(transition);
		if (map != null && var != null) {
			return map.get(var);
		}
		return null;
	}

	public String getContAssign(String transition, String var) {
		if (contAssignments.containsKey(transition) && var != null) {
			Properties prop = contAssignments.get(transition);
			// log.addText("lhpn " + prop.toString());
			if (prop.containsKey(var)) {
				return prop.getProperty(var);
			}
		}
		return null;
	}

	public ExprTree[] getContAssignTree(String transition, String var) {
		HashMap<String, ExprTree[]> map = contAssignmentTrees.get(transition);
		if (map != null && var != null) {
			return map.get(var);
		}
		return null;
	}

	public String getIntAssign(String transition, String var) {
		if (intAssignments.containsKey(transition) && var != null) {
			Properties prop = intAssignments.get(transition);
			if (prop.containsKey(var)) {
				return prop.getProperty(var);
			}
		}
		return null;
	}

	public ExprTree[] getIntAssignTree(String transition, String var) {
		HashMap<String, ExprTree[]> map = intAssignmentTrees.get(transition);
		if (map != null && var != null) {
			return map.get(var);
		}
		return null;
	}

	public String getRateAssign(String transition, String var) {
		Properties prop = rateAssignments.get(transition);
		if (prop != null && var != null) {
			return prop.getProperty(var);
		}
		return "";
	}

	public ExprTree[] getRateAssignTree(String transition, String var) {
		HashMap<String, ExprTree[]> map = rateAssignmentTrees.get(transition);
		if (map != null && var != null) {
			return map.get(var);
		}
		return null;
	}

	public String[] getPlaceList() {
		String[] placeList = new String[places.keySet().size()];
		int i = 0;
		for (String s : places.keySet()) {
			placeList[i] = s;
			i++;
		}
		return placeList;
	}

	public String[] getTransitionList() {
		String[] transitionList = new String[controlFlow.keySet().size()];
		int i = 0;
		for (String s : controlFlow.keySet()) {
			transitionList[i] = s;
			i++;
		}
		return transitionList;
	}

	public boolean isContinuous(String var) {
		return variables.containsKey(var);
	}

	public boolean isInput(String var) {
		return inputs.containsKey(var);
	}

	public boolean isOutput(String var) {
		return outputs.containsKey(var);
	}

	public boolean isInteger(String var) {
		return integers.containsKey(var);
	}

	public boolean isTransition(String var) {
		for (String s : controlFlow.keySet()) {
			if (var.equals(s)) {
				return true;
			}
		}
		return false;
	}

	public Abstraction abstractLhpn() {
		Abstraction abstraction = new Abstraction(log);
		abstraction.addPlaces(places);
		abstraction.addInputs(inputs);
		abstraction.addOutputs(outputs);
		abstraction.addEnablings(enablings);
		abstraction.addEnablingTrees(enablingTrees);
		abstraction.addDelays(delays);
		// abstraction.addDelayTrees(delayTrees);
		abstraction.addRateTrees(transitionRateTrees);
		abstraction.addRates(transitionRates);
		abstraction.addBooleanAssignments(booleanAssignments);
		abstraction.addBooleanAssignmentTrees(booleanAssignmentTrees);
		abstraction.addMovements(controlFlow);
		abstraction.addPlaceMovements(controlPlaces);
		abstraction.addVariables(variables);
		abstraction.addIntegers(integers);
		abstraction.addRateAssignments(rateAssignments);
		abstraction.addRateAssignmentTrees(rateAssignmentTrees);
		abstraction.addContinuousAssignments(contAssignments);
		abstraction.addContinuousAssignmentTrees(contAssignmentTrees);
		abstraction.addIntegerAssignments(intAssignments);
		abstraction.addIntegerAssignmentTrees(intAssignmentTrees);
		abstraction.addProperty(property);
		return abstraction;
	}

	/*
	 * private void parseTransitions(StringBuffer data) { Pattern pattern =
	 * Pattern.compile(TRANSITION); Matcher line_matcher =
	 * pattern.matcher(data.toString()); Pattern output = Pattern.compile(WORD);
	 * Matcher matcher = output.matcher(line_matcher.group()); while
	 * (line_matcher.find()) { String name = matcher.group();
	 * controlFlow.put(name, name); } }
	 */

	private void parseProperty(StringBuffer data) {
		Pattern pattern = Pattern.compile(PROPERTY);
		Matcher lineMatcher = pattern.matcher(data.toString());
		if (lineMatcher.find()) {
			property = lineMatcher.group(1);
		}
	}

	private void parseControlFlow(StringBuffer data) {
		// log.addText("check2start");
		Pattern pattern = Pattern.compile(TRANSITION);
		Matcher lineMatcher = pattern.matcher(data.toString());
		if (lineMatcher.find()) {
			lineMatcher.group(1);
			// log.addText("check2a-");
			String name = lineMatcher.group(1).replaceAll("\\+/", "P");
			name = name.replaceAll("-/", "M");
			// log.addText("check2a+");
			Pattern transPattern = Pattern.compile(WORD);
			Matcher transMatcher = transPattern.matcher(name);
			// log.addText("check2a");
			while (transMatcher.find()) {
				controlFlow.put(transMatcher.group(), new Properties());
			}
			Pattern placePattern = Pattern.compile(PLACE);
			Matcher placeMatcher = placePattern.matcher(data.toString());
			// log.addText("check2b");
			while (placeMatcher.find()) {
				// log.addText("check2 while");
				String temp = placeMatcher.group(1).replaceAll("\\+", "P");
				temp = temp.replaceAll("-", "M");
				// String[] tempLine = tempString.split("#");
				String[] tempPlace = temp.split("\\s");
				// String trans = "";
				if (controlFlow.containsKey(tempPlace[0])) {
					// log.addText("check2 if");
					Properties tempProp = new Properties();
					if (controlFlow.get(tempPlace[0]) != null) {
						tempProp = controlFlow.get(tempPlace[0]);
					}
					String tempString;
					if (tempProp.containsKey("postset")) {
						tempString = tempProp.getProperty("postset");
						tempString = tempString + " " + tempPlace[1];
					}
					else {
						tempString = tempPlace[1];
					}
					tempProp.setProperty("postset", tempString);
					controlFlow.put(tempPlace[0], tempProp);
					places.put(tempPlace[1], false);
					tempProp = new Properties();
					if (controlPlaces.get(tempPlace[1]) != null) {
						tempProp = controlPlaces.get(tempPlace[1]);
					}
					if (tempProp.containsKey("preset")) {
						tempString = tempProp.getProperty("preset");
						tempString = tempString + " " + tempPlace[0];
					}
					else {
						tempString = tempPlace[0];
					}
					tempProp.setProperty("preset", tempString);
					controlPlaces.put(tempPlace[1], tempProp);
					// trans = tempPlace[0];
				}
				else if (controlFlow.containsKey(tempPlace[1])) {
					Properties tempProp = controlFlow.get(tempPlace[1]);
					// log.addText("check2c");
					String tempString;
					// Boolean temp = tempProp.containsKey("preset");
					// log.addText("check2c1");
					// log.addText(temp.toString());
					if (tempProp.containsKey("preset")) {
						// log.addText("check2a if");
						tempString = tempProp.getProperty("preset");
						// log.addText("check2a if1");
						tempString = tempString + " " + tempPlace[0];
					}
					else {
						// log.addText("check2a else");
						tempString = tempPlace[0];
					}
					// log.addText("check2d");
					// log.addText("check2d1");
					tempProp.setProperty("preset", tempString);
					// log.addText("check2e");
					controlFlow.put(tempPlace[1], tempProp);
					places.put(tempPlace[0], false);
					tempProp = new Properties();
					if (controlPlaces.get(tempPlace[0]) != null) {
						tempProp = controlPlaces.get(tempPlace[0]);
					}
					if (tempProp.containsKey("postset")) {
						tempString = tempProp.getProperty("postset");
						tempString = tempString + " " + tempPlace[1];
					}
					else {
						tempString = tempPlace[1];
					}
					tempProp.setProperty("postset", tempString);
					controlPlaces.put(tempPlace[0], tempProp);
					// trans = tempPlace[1];
				}
				// if (controlPlaces.containsKey(tempPlace[0])) {
				// // log.addText("check2 if");
				// Properties tempProp = new Properties();
				// if (controlPlaces.get(tempPlace[0]) != null) {
				// tempProp = controlPlaces.get(tempPlace[0]);
				// }
				// String tempString;
				// if (tempProp.containsKey("postset")) {
				// tempString = tempProp.getProperty("postset");
				// tempString = tempString + " " + tempPlace[1];
				// }
				// else {
				// tempString = tempPlace[1];
				// }
				// tempProp.setProperty("postset", tempString);
				// controlPlaces.put(tempPlace[0], tempProp);
				// // trans = tempPlace[0];
				// }
				// else if (controlPlaces.containsKey(tempPlace[1])) {
				// Properties tempProp = controlPlaces.get(tempPlace[1]);
				// // log.addText("check2c");
				// String tempString;
				// // Boolean temp = tempProp.containsKey("preset");
				// // log.addText("check2c1");
				// // log.addText(temp.toString());
				// if (tempProp.containsKey("preset")) {
				// // log.addText("check2a if");
				// tempString = tempProp.getProperty("preset");
				// // log.addText("check2a if1");
				// tempString = tempString + " " + tempPlace[0];
				// }
				// else {
				// // log.addText("check2a else");
				// tempString = tempPlace[0];
				// }
				// // log.addText("check2d");
				// // log.addText("check2d1");
				// tempProp.setProperty("preset", tempString);
				// // log.addText("check2e");
				// controlPlaces.put(tempPlace[1], tempProp);
				// // trans = tempPlace[1];
				// }
			}
		}
		// for (String s : controlFlow.keySet()) {
		// log.addText(s + " " + controlFlow.get(s));
		// }
		// log.addText("check2end");
	}

	private void parseInOut(StringBuffer data) {
		// System.out.println("hello?");
		Properties varOrder = new Properties();
		// System.out.println("check1start");
		// log.addText("check1start");
		Pattern inLinePattern = Pattern.compile(INPUT);
		Matcher inLineMatcher = inLinePattern.matcher(data.toString());
		Integer i = 0;
		Integer inLength = 0;
		// System.out.println("check1a-");
		if (inLineMatcher.find()) {
			// System.out.println("checkifin");
			Pattern inPattern = Pattern.compile(WORD);
			Matcher inMatcher = inPattern.matcher(inLineMatcher.group(1));
			while (inMatcher.find()) {
				varOrder.setProperty(i.toString(), inMatcher.group());
				i++;
				inLength++;
			}
		}
		// System.out.println("check1a");
		Pattern outPattern = Pattern.compile(OUTPUT);
		Matcher outLineMatcher = outPattern.matcher(data.toString());
		if (outLineMatcher.find()) {
			// log.addText("outline " + outLineMatcher.group(1));
			// log.addText("check1b");
			Pattern output = Pattern.compile(WORD);
			Matcher outMatcher = output.matcher(outLineMatcher.group(1));
			while (outMatcher.find()) {
				varOrder.setProperty(i.toString(), outMatcher.group());
				i++;
			}
		}
		// System.out.println("check1e");
		// log.addText("check1c");
		Pattern initState = Pattern.compile(INIT_STATE);
		Matcher initMatcher = initState.matcher(data.toString());
		if (initMatcher.find()) {
			// log.addText("check1d");
			Pattern initDigit = Pattern.compile("[01X]+");
			Matcher digitMatcher = initDigit.matcher(initMatcher.group());
			digitMatcher.find();
			// log.addText("check1e");
			String[] initArray = new String[digitMatcher.group().length()];
			Pattern bit = Pattern.compile("[01X]");
			Matcher bitMatcher = bit.matcher(digitMatcher.group());
			// log.addText("check1f");
			i = 0;
			while (bitMatcher.find()) {
				initArray[i] = bitMatcher.group();
				i++;
			}
			for (i = 0; i < inLength; i++) {
				String name = varOrder.getProperty(i.toString());
				if (initArray[i].equals("1")) {
					inputs.put(name, "true");
				}
				else if (initArray[i].equals("0")) {
					inputs.put(name, "false");
				}
				else {
					inputs.put(name, "unknown");
				}
			}
			// log.addText("check1f");
			for (i = inLength; i < initArray.length; i++) {
				String name = varOrder.getProperty(i.toString());
				if (initArray[i].equals("1") && name != null) {
					outputs.put(name, "true");
				}
				else if (initArray[i].equals("0") && name != null) {
					outputs.put(name, "false");
				}
				else {
					outputs.put(name, "unknown");
				}
			}
		}
		else {
			if (varOrder.size() != 0) {
				System.out.println("WARNING: Boolean variables have not been initialized.");
				for (i = 0; i < varOrder.size(); i++) {
					if (i < inLength) {
						inputs.put(varOrder.getProperty(i.toString()), "unknown");
					}
					else {
						outputs.put(varOrder.getProperty(i.toString()), "unknown");
					}
				}
			}
		}
	}

	private void parseVars(StringBuffer data) {
		// log.addText("check3 start");
		// System.out.println("check3 start");
		Properties initCond = new Properties();
		Properties initValue = new Properties();
		Properties initRate = new Properties();
		// log.addText("check3a");
		// System.out.println("check3a");
		Pattern linePattern = Pattern.compile(CONTINUOUS);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			// log.addText("check3b");
			// System.out.println("check3b");
			Pattern varPattern = Pattern.compile(WORD);
			Matcher varMatcher = varPattern.matcher(lineMatcher.group(1));
			// log.addText("check3c");
			// System.out.println("check3c");
			while (varMatcher.find()) {
				variables.put(varMatcher.group(), initCond);
			}
			// log.addText("check3d " + VARS_INIT);
			// System.out.println("check3c");
			Pattern initLinePattern = Pattern.compile(VARS_INIT);
			// log.addText("check3d1");
			// System.out.println("check3d");
			Matcher initLineMatcher = initLinePattern.matcher(data.toString());
			// log.addText("check3d2");
			// System.out.println("check3e");
			initLineMatcher.find();
			// log.addText("check3e");
			// System.out.println("check3f");
			Pattern initPattern = Pattern.compile(INIT_COND);
			Matcher initMatcher = initPattern.matcher(initLineMatcher.group(1));
			// log.addText("check3f");
			// System.out.println("check3g");
			while (initMatcher.find()) {
				if (variables.containsKey(initMatcher.group(1))) {
					initValue.put(initMatcher.group(1), initMatcher.group(2));
				}
			}
			// log.addText("check3g");
			// System.out.println("check3h");
			Pattern rateLinePattern = Pattern.compile(INIT_RATE);
			Matcher rateLineMatcher = rateLinePattern.matcher(data.toString());
			if (rateLineMatcher.find()) {
				// log.addText("check3h");
				// System.out.println("check3i");
				Pattern ratePattern = Pattern.compile(INIT_COND);
				Matcher rateMatcher = ratePattern.matcher(rateLineMatcher.group(1));
				// log.addText("check3i");
				// System.out.println("check3j");
				while (rateMatcher.find()) {
					// log.addText(rateMatcher.group(1) + "value" +
					// rateMatcher.group(2));
					initRate.put(rateMatcher.group(1), rateMatcher.group(2));
				}
			}
			// log.addText("check3j");
			// System.out.println("check3k");
			for (String s : variables.keySet()) {
				// log.addText("check3for" + s);
				// System.out.println("check3for " + s);
				initCond.put("value", initValue.get(s));
				initCond.put("rate", initRate.get(s));
				// log.addText("check3for" + initCond.toString());
				// System.out.println("check3for " + initCond.toString());
				variables.put(s, initCond);
			}
		}
		// log.addText("check3end");
		// System.out.println("check3end");
	}

	private void parseIntegers(StringBuffer data) {
		// log.addText("check3 start");
		String initCond = "0";
		Properties initValue = new Properties();
		// log.addText("check3a");
		Pattern linePattern = Pattern.compile(VARIABLES);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		// log.addText("check3b");
		if (lineMatcher.find()) {
			Pattern varPattern = Pattern.compile(WORD);
			Matcher varMatcher = varPattern.matcher(lineMatcher.group(1));
			// log.addText("check3c");
			while (varMatcher.find()) {
				if (!variables.containsKey(varMatcher.group())) {
					integers.put(varMatcher.group(), initCond);
				}
			}
			// log.addText("check3d " + VARS_INIT);
			Pattern initLinePattern = Pattern.compile(VARS_INIT);
			// log.addText("check3d1");
			Matcher initLineMatcher = initLinePattern.matcher(data.toString());
			// log.addText("check3d2");
			if (initLineMatcher.find()) {
				// log.addText("check3e");
				Pattern initPattern = Pattern.compile(INIT_COND);
				Matcher initMatcher = initPattern.matcher(initLineMatcher.group(1));
				// log.addText("check3f");
				while (initMatcher.find()) {
					if (integers.containsKey(initMatcher.group(1))) {
						initValue.put(initMatcher.group(1), initMatcher.group(2));
					}
				}
			}
			// log.addText("check3g");
			// log.addText("check3i");
			// log.addText("check3j");
			for (String s : integers.keySet()) {
				// log.addText("check3for" + s);
				if (initValue.get(s) != null) {
					initCond = initValue.get(s).toString();
				}
				// log.addText("check3for" + initCond);
				integers.put(s, initCond);
			}
		}
		// log.addText("check3end");
	}

	private void parsePlaces(StringBuffer data) {
		Pattern linePattern = Pattern.compile(PLACES_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			// log.addText(lineMatcher.group());
			Pattern markPattern = Pattern.compile(MARKING);
			Matcher markMatcher = markPattern.matcher(lineMatcher.group(1));
			while (markMatcher.find()) {
				places.put(markMatcher.group(), false);
			}
		}
	}

	private void parseMarking(StringBuffer data) {
		// log.addText("check4start");
		Pattern linePattern = Pattern.compile(MARKING_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			// log.addText("check4a");
			Pattern markPattern = Pattern.compile(MARKING);
			Matcher markMatcher = markPattern.matcher(lineMatcher.group(1));
			// log.addText("check4b");
			while (markMatcher.find()) {
				// log.addText("check4loop");
				places.put(markMatcher.group(), true);
			}
		}
		// log.addText("check4end");
	}

	private void parseEnabling(StringBuffer data) {
		Pattern linePattern = Pattern.compile(ENABLING_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern enabPattern = Pattern.compile(ENABLING);
			Matcher enabMatcher = enabPattern.matcher(lineMatcher.group(1));
			while (enabMatcher.find()) {
				enablings.put(enabMatcher.group(2), enabMatcher.group(4));
				ExprTree expr = new ExprTree(this);
				if (enabMatcher.group(4) != null && !enabMatcher.group(4).equals("")) {
					expr.token = expr.intexpr_gettok(enabMatcher.group(4));
					expr.intexpr_L(enabMatcher.group(4));
					// log.addText(enabMatcher.group(4) + " " +
					// expr.toString());
					enablingTrees.put(enabMatcher.group(2), expr);
					// log.addText(expr.toString());
				}
				else {
					enablingTrees.put(enabMatcher.group(2), null);
				}
				// log.addText(enabMatcher.group(2) + enabMatcher.group(4));
			}
		}
	}

	private void parseAssign(StringBuffer data) {
		// log.addText("check6start");
		Pattern linePattern = Pattern.compile(ASSIGNMENT_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		// Boolean temp = lineMatcher.find();
		// log.addText(temp.toString());
		// log.addText("check6a");
		if (lineMatcher.find()) {
			Pattern assignPattern = Pattern.compile(ASSIGNMENT);
			// log.addText("check6a1.0");
			// log.addText("check6a1 " + lineMatcher.group());
			Matcher assignMatcher = assignPattern.matcher(lineMatcher.group(1));
			Pattern varPattern = Pattern.compile(ASSIGN_VAR);
			Pattern indetPattern = Pattern.compile(INDET_ASSIGN_VAR);
			Matcher varMatcher;
			// log.addText("check6ab");
			while (assignMatcher.find()) {
				Properties assignProp = new Properties();
				Properties intProp = new Properties();
				HashMap<String, ExprTree[]> assignMap = new HashMap<String, ExprTree[]>();
				HashMap<String, ExprTree[]> intMap = new HashMap<String, ExprTree[]>();
				// log.addText("check6while1");
				varMatcher = varPattern.matcher(assignMatcher.group(2));
				if (!varMatcher.find()) {
					varMatcher = indetPattern.matcher(assignMatcher.group(2));
				}
				else {
					// log.addText("check6 else");
					ExprTree[] expr = new ExprTree[2];
					Pattern rangePattern = Pattern.compile(RANGE);
					Matcher rangeMatcher = rangePattern.matcher(varMatcher.group(2));
					if (rangeMatcher.find()) {
						if (!rangeMatcher.group(1).equals("")) {
							expr[0].token = expr[0].intexpr_gettok(rangeMatcher.group(1));
							expr[0].intexpr_L(rangeMatcher.group(1));
						}
						else {
							expr[0] = null;
						}
						if (!rangeMatcher.group(1).equals("")) {
							expr[1].token = expr[1].intexpr_gettok(rangeMatcher.group(2));
							expr[1].intexpr_L(rangeMatcher.group(2));
						}
						else {
							expr[1] = null;
						}
						if (isInteger(varMatcher.group(1))) {
							if (intAssignments.containsKey(assignMatcher.group(1))) {
								intProp = intAssignments.get(assignMatcher.group(1));
							}
							intProp.put(varMatcher.group(1), varMatcher.group(2));
							intMap.put(varMatcher.group(1), expr);
						}
						else {
							if (contAssignments.containsKey(assignMatcher.group(1))) {
								assignProp = contAssignments.get(assignMatcher.group(1));
							}
							assignProp.put(varMatcher.group(1), varMatcher.group(2));
							assignMap.put(varMatcher.group(1), expr);
						}
						if (isInteger(varMatcher.group(1))) {
							if (intAssignments.containsKey(assignMatcher.group(1))) {
								intProp = intAssignments.get(assignMatcher.group(1));
							}
							intProp.put(varMatcher.group(1), varMatcher.group(2));
							intMap.put(varMatcher.group(1), expr);
						}
						else {
							if (contAssignments.containsKey(assignMatcher.group(1))) {
								assignProp = contAssignments.get(assignMatcher.group(1));
							}
							assignProp.put(varMatcher.group(1), varMatcher.group(2));
							assignMap.put(varMatcher.group(1), expr);
						}
					}
				}
				do {
					ExprTree expr = new ExprTree(this);
					expr.token = expr.intexpr_gettok(varMatcher.group(2));
					if (!varMatcher.group(2).equals("")) {
						expr.intexpr_L(varMatcher.group(2));
					}
					else {
						expr = null;
					}
					ExprTree[] array = { expr };
					// log.addText("check6while2");
					if (isInteger(varMatcher.group(1))) {
						if (intAssignments.containsKey(assignMatcher.group(1))) {
							intProp = intAssignments.get(assignMatcher.group(1));
						}
						intProp.put(varMatcher.group(1), varMatcher.group(2));
						intMap.put(varMatcher.group(1), array);
					}
					else {
						if (contAssignments.containsKey(assignMatcher.group(1))) {
							assignProp = contAssignments.get(assignMatcher.group(1));
						}
						assignProp.put(varMatcher.group(1), varMatcher.group(2));
						assignMap.put(varMatcher.group(1), array);
					}
				}
				while (varMatcher.find());
				if (intProp.size() > 0) {
					intAssignments.put(assignMatcher.group(1), intProp);
					intAssignmentTrees.put(assignMatcher.group(1), intMap);
				}
				if (assignProp.size() > 0) {
					contAssignments.put(assignMatcher.group(1), assignProp);
					contAssignmentTrees.put(assignMatcher.group(1), assignMap);
				}
			}
		}
		// log.addText("check6end");
	}

	private void parseRateAssign(StringBuffer data) {
		Pattern linePattern = Pattern.compile(RATE_ASSIGNMENT_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern assignPattern = Pattern.compile(ASSIGNMENT);
			Matcher assignMatcher = assignPattern.matcher(lineMatcher.group(1));
			Pattern varPattern = Pattern.compile(ASSIGN_VAR);
			Pattern indetPattern = Pattern.compile(INDET_ASSIGN_VAR);
			Matcher varMatcher;
			Matcher indetMatcher;
			while (assignMatcher.find()) {
				Properties assignProp = new Properties();
				HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
				if (rateAssignments.containsKey(assignMatcher.group(1))) {
					assignProp = rateAssignments.get(assignMatcher.group(1));
				}
				// log.addText("here " + assignMatcher.group(2));
				// String temp = assignMatcher.group(2);
				varMatcher = varPattern.matcher(assignMatcher.group(2));
				// log.addText(assignMatcher.group(2) + " " + indetPattern);
				boolean indet = false;
				indetMatcher = indetPattern.matcher(assignMatcher.group(2));
				while (indetMatcher.find()) {
					indet = true;
					ExprTree[] expr = new ExprTree[2];
					expr[0] = new ExprTree(this);
					expr[1] = new ExprTree(this);
					assignProp.put(indetMatcher.group(1), indetMatcher.group(2));
					Pattern rangePattern = Pattern.compile(RANGE);
					Matcher rangeMatcher = rangePattern.matcher(indetMatcher.group(2));
					if (rangeMatcher.find()) {
						expr[0].token = expr[0].intexpr_gettok(rangeMatcher.group(1));
						if (!rangeMatcher.group(1).equals("")) {
							expr[0].intexpr_L(rangeMatcher.group(1));
						}
						else {
							expr[0] = null;
						}
						expr[1].token = expr[1].intexpr_gettok(rangeMatcher.group(2));
						if (!rangeMatcher.group(2).equals("")) {
							expr[1].intexpr_L(rangeMatcher.group(2));
						}
						else {
							expr[1] = null;
						}
						map.put(varMatcher.group(1), expr);
					}
				}
				while (varMatcher.find() && !indet) {
					ExprTree expr = new ExprTree(this);
					expr.token = expr.intexpr_gettok(varMatcher.group(2));
					if (!varMatcher.group(2).equals("")) {
						expr.intexpr_L(varMatcher.group(2));
					}
					else {
						expr = null;
					}
					ExprTree[] array = { expr };
					map.put(varMatcher.group(1), array);
					if (!assignProp.containsKey(varMatcher.group(1))) {
						assignProp.put(varMatcher.group(1), varMatcher.group(2));
					}
					// log.addText("rate " + varMatcher.group(1) + ":=" +
					// varMatcher.group(2));
				}
				rateAssignments.put(assignMatcher.group(1), assignProp);
				rateAssignmentTrees.put(assignMatcher.group(1), map);
			}
		}
		// for (String s: rateAssignments.keySet()) {
		// log.addText(s + " " + rateAssignments.get(s));
		// }
	}

	private void parseDelayAssign(StringBuffer data) {
		// log.addText("check8start");
		Pattern linePattern = Pattern.compile(DELAY_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			// log.addText("check8a");
			Pattern delayPattern = Pattern.compile(DELAY);
			Matcher delayMatcher = delayPattern.matcher(lineMatcher.group(1));
			// log.addText("check8b");
			while (delayMatcher.find()) {
				// log.addText("check8while");
				delays.put(delayMatcher.group(1), delayMatcher.group(2));
				// ExprTree[] expr = new ExprTree[2];
				// Pattern rangePattern = Pattern.compile(RANGE);
				// Matcher rangeMatcher =
				// rangePattern.matcher(delayMatcher.group(2));
				// if (rangeMatcher.find()) {
				// expr[0].token =
				// expr[0].intexpr_gettok(rangeMatcher.group(1));
				// expr[0].intexpr_L(rangeMatcher.group(1));
				// expr[1].token =
				// expr[1].intexpr_gettok(rangeMatcher.group(2));
				// expr[1].intexpr_L(rangeMatcher.group(2));
				// delayTrees.put(delayMatcher.group(1), expr);
				// }
			}
		}
		// log.addText("check8end");
	}

	private void parseTransitionRate(StringBuffer data) {
		// log.addText("check8start");
		Pattern linePattern = Pattern.compile(TRANS_RATE_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			// log.addText("check8a");
			Pattern delayPattern = Pattern.compile(ENABLING);
			Matcher delayMatcher = delayPattern.matcher(lineMatcher.group(1));
			// log.addText("check8b");
			while (delayMatcher.find()) {
				// log.addText("check8while");
				ExprTree expr = new ExprTree(this);
				if (delayMatcher.group(4) != null && !delayMatcher.group(4).equals("")) {
					expr.token = expr.intexpr_gettok(delayMatcher.group(4));
					expr.intexpr_L(delayMatcher.group(4));
					transitionRateTrees.put(delayMatcher.group(2), expr);
					transitionRates.put(delayMatcher.group(2), delayMatcher.group(4));
				}
				else {
					transitionRateTrees.put(delayMatcher.group(2), null);
				}
			}
		}
		// log.addText("check8end");
	}

	private void parseBooleanAssign(StringBuffer data) {
		Pattern linePattern = Pattern.compile(BOOLEAN_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern transPattern = Pattern.compile(BOOLEAN_TRANS);
			Matcher transMatcher = transPattern.matcher(lineMatcher.group(1));
			Pattern assignPattern = Pattern.compile(BOOLEAN_ASSIGN);
			Pattern rangePattern = Pattern.compile(BOOLEAN_RANGE);
			while (transMatcher.find()) {
				Properties prop = new Properties();
				HashMap<String, ExprTree[]> map = new HashMap<String, ExprTree[]>();
				Matcher rangeMatcher = rangePattern.matcher(transMatcher.group(2));
				Matcher assignMatcher = assignPattern.matcher(transMatcher.group(2));
				for (int i = 0; i < inputs.size() + outputs.size(); i++) {
					while (rangeMatcher.find()) {
						// System.out.println(rangeMatcher.group(1) + " range "
						// + rangeMatcher.group(2));
						if (booleanAssignments.containsKey(transMatcher.group(1))) {
							prop = booleanAssignments.get(transMatcher.group(1));
						}
						prop.put(rangeMatcher.group(1), rangeMatcher.group(2));
						ExprTree[] expr = new ExprTree[2];
						expr[0] = new ExprTree(this);
						expr[1] = new ExprTree(this);
						Pattern newRangePattern = Pattern.compile(RANGE);
						Matcher newRangeMatcher = newRangePattern.matcher(rangeMatcher.group(2));
						newRangeMatcher.find();
						expr[0].token = expr[0].intexpr_gettok(newRangeMatcher.group(1));
						if (!newRangeMatcher.group(1).equals("")) {
							expr[0].intexpr_L(newRangeMatcher.group(1));
						}
						else {
							expr[0] = null;
						}
						expr[1].token = expr[1].intexpr_gettok(newRangeMatcher.group(2));
						if (!newRangeMatcher.group(2).equals("")) {
							expr[1].intexpr_L(newRangeMatcher.group(2));
						}
						else {
							expr[1] = null;
						}
						map.put(newRangeMatcher.group(1), expr);
					}
					while (assignMatcher.find()) {
						if (!prop.containsKey(assignMatcher.group(1))) {
							if (booleanAssignments.containsKey(transMatcher.group(1))) {
								prop = booleanAssignments.get(transMatcher.group(1));
							}
							// System.out.println(assignMatcher.group(1) + "
							// norange " + assignMatcher.group(2));
							prop.put(assignMatcher.group(1), assignMatcher.group(2));
						}
						ExprTree expr = new ExprTree(this);
						expr.token = expr.intexpr_gettok(assignMatcher.group(2));
						if (!assignMatcher.group(2).equals("")) {
							expr.intexpr_L(assignMatcher.group(2));
						}
						else {
							expr = null;
						}
						ExprTree[] array = { expr };
						map.put(assignMatcher.group(1), array);
					}
				}
				booleanAssignments.put(transMatcher.group(1), prop);
				booleanAssignmentTrees.put(transMatcher.group(1), map);
			}
		}
	}

	// private void parseIntAssign(StringBuffer data) {
	// // log.addText("check6start");
	// Properties assignProp = new Properties();
	// Pattern linePattern = Pattern.compile(ASSIGNMENT_LINE);
	// Matcher lineMatcher = linePattern.matcher(data.toString());
	// // Boolean temp = lineMatcher.find();
	// // log.addText(temp.toString());
	// // log.addText("check6a");
	// if (lineMatcher.find()) {
	// Pattern assignPattern = Pattern.compile(ASSIGNMENT);
	// // log.addText("check6a1.0");
	// // log.addText("check6a1 " + lineMatcher.group());
	// Matcher assignMatcher = assignPattern.matcher(lineMatcher.group(1));
	// Pattern varPattern = Pattern.compile(ASSIGN_VAR);
	// Pattern indetPattern = Pattern.compile(INDET_ASSIGN_VAR);
	// Matcher varMatcher;
	// // log.addText("check6ab");
	// while (assignMatcher.find()) {
	// // log.addText("check6while1");
	// varMatcher = varPattern.matcher(assignMatcher.group(2));
	// if (!varMatcher.find()) {
	// varMatcher = indetPattern.matcher(assignMatcher.group(2));
	// }
	// else {
	// varMatcher = varPattern.matcher(assignMatcher.group(2));
	// }
	// // log.addText(varMatcher.toString());
	// while (varMatcher.find()) {
	// // log.addText("check6while2");
	// assignProp.put(varMatcher.group(1), varMatcher.group(2));
	// }
	// // log.addText(assignMatcher.group(1) + assignProp.toString());
	// intAssignments.put(assignMatcher.group(1), assignProp);
	// }
	// }
	// // log.addText("check6end");
	// }

	private static final String PROPERTY = "#@\\.property (\\S+?)\\n";

	private static final String INPUT = "\\.inputs([[\\s[^\\n]]\\w+]*?)\\n";

	private static final String OUTPUT = "\\.outputs([[\\s[^\\n]]\\w+]*?)\\n";

	private static final String INIT_STATE = "#@\\.init_state \\[(\\w+)\\]";

	private static final String TRANSITION = "\\.dummy([^\\n]*?)\\n";

	private static final String PLACE = "\\n([\\w_\\+-/&&[^\\.#]]+ [\\w_\\+-/]+)";

	private static final String CONTINUOUS = "#@\\.continuous ([.[^\\n]]+)\\n";

	private static final String VARIABLES = "#@\\.variables ([.[^\\n]]+)\\n";

	private static final String VARS_INIT = "#@\\.init_vals \\{([\\S[^\\}]]+?)\\}";

	private static final String INIT_RATE = "#@\\.init_rates \\{([\\S[^\\}]]+?)\\}";

	private static final String INIT_COND = "<(\\w+)=([\\S^>]+?)>";

	// private static final String INTS_INIT = "#@\\.init_ints
	// \\{([\\S[^\\}]]+?)\\}";

	private static final String PLACES_LINE = "#\\|\\.places ([.[^\\n]]+)\\n";

	private static final String MARKING_LINE = "\\.marking \\{(.+)\\}";

	private static final String MARKING = "\\w+";

	private static final String ENABLING_LINE = "#@\\.enablings \\{([.[^\\}]]+?)\\}";

	private static final String ENABLING = "(<([\\S[^=]]+?)=(\\[([^\\]]+?)\\])+?>)?";

	private static final String ASSIGNMENT_LINE = "#@\\.assignments \\{([.[^\\}]]+?)\\}";

	// private static final String INT_ASSIGNMENT_LINE = "#@\\.int_assignments
	// \\{([.[^\\}]]+?)\\}";

	private static final String RATE_ASSIGNMENT_LINE = "#@\\.rate_assignments \\{([.[^\\}]]+?)\\}";

	private static final String ASSIGNMENT = "<([\\S[^=]]+?)=\\[([^>]+?)\\]>";

	private static final String ASSIGN_VAR = "([\\S[^:]]+?):=([\\S]+)";

	private static final String INDET_ASSIGN_VAR = "([\\S[^:]]+?):=(\\[[-\\d]+,[-\\d]+\\])";

	private static final String DELAY_LINE = "#@\\.delay_assignments \\{([\\S[^\\}]]+?)\\}";

	private static final String DELAY = "<([\\w_]+)=(\\[\\w+,\\w+\\])>";

	private static final String TRANS_RATE_LINE = "#@\\.transition_rates \\{([\\S[^\\}]]+?)\\}";

	private static final String BOOLEAN_LINE = "#@\\.boolean_assignments \\{([\\S[^\\}]]+?)\\}";

	private static final String BOOLEAN_TRANS = "<([\\w]+?)=([\\S[^>]]+?)>";

	private static final String BOOLEAN_ASSIGN = "\\[([\\w_]+):=([\\S^\\]]+?)\\]";

	private static final String BOOLEAN_RANGE = "\\[([\\w_]+):=(\\[[\\S^\\]]+?,[\\S^\\]]+?\\])\\]";

	private static final String WORD = "(\\S+)";

	private static final String RANGE = "\\[(\\w+?),(\\w+?)\\]";

}