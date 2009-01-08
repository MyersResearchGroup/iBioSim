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

	private HashMap<String, Boolean> inputs;

	private HashMap<String, Boolean> outputs;

	private HashMap<String, String> enablings;

	private HashMap<String, Properties> controlFlow;

	private HashMap<String, Properties> variables;

	private HashMap<String, String> integers;

	private HashMap<String, Properties> rateAssignments;

	private HashMap<String, Properties> contAssignments;

	private HashMap<String, Properties> intAssignments;

	private HashMap<String, String> delays;

	private HashMap<String, Properties> booleanAssignments;

	private Log log;

	public LHPNFile(Log log) {
		this.log = log;
		places = new HashMap<String, Boolean>();
		inputs = new HashMap<String, Boolean>();
		outputs = new HashMap<String, Boolean>();
		enablings = new HashMap<String, String>();
		delays = new HashMap<String, String>();
		booleanAssignments = new HashMap<String, Properties>();
		controlFlow = new HashMap<String, Properties>();
		variables = new HashMap<String, Properties>();
		integers = new HashMap<String, String>();
		rateAssignments = new HashMap<String, Properties>();
		contAssignments = new HashMap<String, Properties>();
		intAssignments = new HashMap<String, Properties>();
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
					buffer.append(s + " ");
					boolOrder.put(s, i);
					i++;
				}
				buffer.append("\n");
			}
			if (!outputs.isEmpty()) {
				buffer.append(".outputs ");
				for (String s : outputs.keySet()) {
					buffer.append(s + " ");
					boolOrder.put(s, i);
					i++;
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
			if (!inputs.isEmpty() || !outputs.isEmpty()) {
				buffer.append("#@.init_state [");
				for (i = 0; i < boolOrder.size(); i++) {
					for (String s : inputs.keySet()) {
						if (boolOrder.get(s).equals(i)) {
							if (inputs.get(s)) {
								buffer.append("1");
							}
							else {
								buffer.append("0");
							}
						}
					}
					for (String s : outputs.keySet()) {
						if (boolOrder.get(s).equals(i)) {
							if (outputs.get(s)) {
								buffer.append("1");
							}
							else {
								buffer.append("0");
							}
						}
					}
				}
				buffer.append("]\n");
			}
			if (!controlFlow.isEmpty()) {
				buffer.append(".graph\n");
				for (String s : controlFlow.keySet()) {
					// log.addText(s);
					if (controlFlow.get(s) != null) {
						Properties prop = controlFlow.get(s);
						// log.addText(s + prop.getProperty("to"));
						if (prop.getProperty("to") != null) {
							String toString = prop.getProperty("to");
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
						if (prop.getProperty("from") != null) {
							String fromString = prop.getProperty("from");
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
				buffer.append("}\n");
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
				buffer.append("#@.enablings {");
				for (String s : enablings.keySet()) {
					if (s != null && !enablings.get(s).equals("")) {
						// log.addText("here " + enablings.get(s));
						buffer.append("<" + s + "=[" + enablings.get(s) + "]>");
					}
				}
				buffer.append("}\n");
			}
			if (!contAssignments.isEmpty() || !intAssignments.isEmpty()) {
				buffer.append("#@.assignments {");
				for (String s : contAssignments.keySet()) {
					Properties prop = contAssignments.get(s);
					buffer.append("<" + s + "=");
					for (Object key : prop.keySet()) {
						String t = (String) key;
						buffer.append("[" + t + ":=" + prop.getProperty(t) + "]");
					}
					buffer.append(">");
				}
				for (String s : intAssignments.keySet()) {
					Properties prop = intAssignments.get(s);
					buffer.append("<" + s + "=");
					for (Object key : prop.keySet()) {
						String t = (String) key;
						buffer.append("[" + t + ":=" + prop.getProperty(t) + "]");
					}
					buffer.append(">");
				}
				buffer.append("}\n");
			}
			if (!rateAssignments.isEmpty()) {
				buffer.append("#@.rate_assignments {");
				for (String s : rateAssignments.keySet()) {
					Properties prop = rateAssignments.get(s);
					buffer.append("<" + s + "=");
					for (Object key : prop.keySet()) {
						String t = (String) key;
						buffer.append("[" + t + ":=" + prop.getProperty(t) + "]");
					}
					buffer.append(">");
				}
				buffer.append("}\n");
			}
			if (!delays.isEmpty()) {
				buffer.append("#@.delay_assignments {");
				for (String s : delays.keySet()) {
					buffer.append("<" + s + "=" + delays.get(s) + ">");
				}
				buffer.append("}\n");
			}
			if (!booleanAssignments.isEmpty()) {
				buffer.append("#@.boolean_assignments {");
				for (String s : booleanAssignments.keySet()) {
					buffer.append("<" + s + "=");
					Properties prop = booleanAssignments.get(s);
					for (Object key : prop.keySet()) {
						String t = (String) key;
						if (isInput(t) || isOutput(t)) {
							buffer.append("[" + t + ":=" + prop.getProperty(t) + "]");
						}
					}
					buffer.append(">");
				}
				buffer.append("}\n");
			}
			if (!intAssignments.isEmpty()) {
				buffer.append("#@.int_assignments {");
				for (String s : intAssignments.keySet()) {
					Properties prop = intAssignments.get(s);
					buffer.append("<" + s + "=");
					for (Object key : prop.keySet()) {
						String t = (String) key;
						if (isInteger(t)) {
							buffer.append("[" + t + ":=" + prop.getProperty(t) + "]");
						}
					}
					buffer.append(">");
				}
				buffer.append("}\n");
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
			log.addText("Saving:\n" + file + "\n");
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void load(String filename) {
		places = new HashMap<String, Boolean>();
		inputs = new HashMap<String, Boolean>();
		outputs = new HashMap<String, Boolean>();
		enablings = new HashMap<String, String>();
		controlFlow = new HashMap<String, Properties>();
		variables = new HashMap<String, Properties>();
		contAssignments = new HashMap<String, Properties>();
		rateAssignments = new HashMap<String, Properties>();
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
			parseMarking(data);
			// System.out.println("check6");
			// log.addText("check5");
			parseEnabling(data);
			// System.out.println("check7");
			// log.addText("check6");
			parseContAssign(data);
			// System.out.println("check8");
			// log.addText("check7");
			parseRateAssign(data);
			// System.out.println("check9");
			// log.addText("check8");
			parseDelayAssign(data);
			parseIntAssign(data);
			// System.out.println("check0");
			// log.addText("check9");
			parseBooleanAssign(data);
			// System.out.println("check11");
			// log.addText("check0");
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to parse LHPN");
		}
	}

	public void addPlace(String name, Boolean ic) {
		places.put(name, ic);
	}

	public void removePlace(String name) {
		if (name != null && places.containsKey(name)) {
			places.remove(name);
		}
	}

	public void addInput(String name, Boolean ic) {
		inputs.put(name, ic);
	}

	public void removeInput(String name) {
		if (name != null && inputs.containsKey(name)) {
			inputs.remove(name);
		}
	}

	public void addOutput(String name, Boolean ic) {
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
	// flow.setProperty(name + "to", flow.getProperty(name + "to") + to + " ");
	// }
	// else if (controlFlow.containsKey(to)) {
	// name = to;
	// flow = controlFlow.get(to);
	// flow.setProperty(name + "from", flow.getProperty(name + "from") + to + "
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
	// String toString = flow.getProperty("to");
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
	// flow.setProperty(name + "to", "");
	// for (int i = 0; i < toArray.length - 1; i++) {
	// flow.setProperty("to", flow.getProperty("to") + toArray[i] + " ");
	// }
	// }
	// else if (controlFlow.containsKey(to)) {
	// name = to;
	// flow = controlFlow.get(to);
	// String fromString = flow.getProperty("from");
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
	// flow.setProperty("from", "");
	// for (int i = 0; i < fromArray.length - 1; i++) {
	// flow.setProperty("from", flow.getProperty("from") + fromArray[i] + " ");
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
			Properties prop = new Properties();
			String list = "";
			if (controlFlow.containsKey(fromName)) {
				if (controlFlow.get(fromName) != null) {
					prop = controlFlow.get(fromName);
					list = prop.getProperty("to");
					list = list + " " + toName;
				}
			}
			else {
				list = toName;
			}
			// log.addText(list);
			// System.out.println(prop == null);
			prop.setProperty("to", list);
			controlFlow.put(fromName, prop);
		}
		else {
			Properties prop = new Properties();
			String list = "";
			if (controlFlow.containsKey(toName)) {
				if (controlFlow.get(toName) != null) {
					prop = controlFlow.get(toName);
					list = prop.getProperty("from");
					list = list + " " + fromName;
				}
				else {
					list = fromName;
				}
			}
			else {
				list = fromName;
			}
			prop.setProperty("from", list);
			controlFlow.put(toName, prop);
		}
	}

	public void removeControlFlow(String fromName, String toName) {
		if (isTransition(fromName)) {
			Properties prop = controlFlow.get(fromName);
			String[] list = prop.getProperty("to").split("\\s");
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
			prop.put("to", toList);
			controlFlow.put(fromName, prop);
		}
		else {
			Properties prop = controlFlow.get(toName);
			String[] list = prop.getProperty("from").split("\\s");
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
			prop.put("from", list);
			controlFlow.put(toName, prop);
		}
	}

	public boolean containsFlow(String fromName, String toName) {
		if (isTransition(fromName)) {
			Properties prop = controlFlow.get(fromName);
			if (prop != null) {
				if (prop.getProperty("to") != null) {
					String[] list = prop.getProperty("to").split("\\s");
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
				if (prop.getProperty("from") != null) {
					String[] list = prop.getProperty("from").split("\\s");
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

	public void addTransition(String name, String delay, Properties rateAssign,
			Properties booleanAssign, String enabling) {
		addTransition(name);
		delays.put(name, delay);
		rateAssignments.put(name, rateAssign);
		booleanAssignments.put(name, booleanAssign);
		enablings.put(name, enabling);
	}

	public void removeTransition(String name) {
		controlFlow.remove(name);
		delays.remove(name);
		rateAssignments.remove(name);
		booleanAssignments.remove(name);
		enablings.remove(name);
	}

	public void addEnabling(String name, String cond) {
		enablings.put(name, cond);
	}

	private void removeEnabling(String name) {
		if (name != null && enablings.containsKey(name)) {
			enablings.remove(name);
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
		return flag;
	}

	public void addInteger(String name, String ic) {
		integers.put(name, ic);
	}

	public void addRateAssign(String transition, String name, String value) {
		Properties prop = new Properties();
		if (rateAssignments.get(transition) != null) {
			prop = rateAssignments.get(transition);
		}
		prop.setProperty(name, value);
		rateAssignments.put(transition, prop);
	}

	public void removeRateAssign(String transition, String name) {
		if (rateAssignments.containsKey(transition)) {
			Properties prop = rateAssignments.get(transition);
			if (name != null && prop.containsKey(name)) {
				prop.remove(name);
			}
			rateAssignments.put(transition, prop);
		}
	}

	public void addBoolAssign(String transition, String name, String value) {
		Properties prop = new Properties();
		if (booleanAssignments.get(transition) != null) {
			prop = booleanAssignments.get(transition);
		}
		prop.setProperty(name, value);
		booleanAssignments.put(transition, prop);
	}

	public void removeBoolAssign(String transition, String name) {
		Properties prop = booleanAssignments.get(transition);
		if (name != null && prop.containsKey(name)) {
			prop.remove(name);
		}
		booleanAssignments.put(transition, prop);
	}

	public void addContAssign(String transition, String name, String value) {
		Properties prop = new Properties();
		if (contAssignments.get(transition) != null) {
			prop = contAssignments.get(transition);
		}
		// System.out.println("here " + transition + name + value);
		prop.setProperty(name, value);
		contAssignments.put(transition, prop);
	}

	public void removeContAssign(String transition, String name) {
		if (contAssignments.containsKey(transition)) {
			Properties prop = contAssignments.get(transition);
			if (name != null && prop.containsKey(name)) {
				prop.remove(name);
			}
			contAssignments.put(transition, prop);
		}
	}

	public void addIntAssign(String transition, String name, String value) {
		Properties prop = new Properties();
		if (intAssignments.get(transition) != null) {
			prop = intAssignments.get(transition);
		}
		// System.out.println("here " + transition + name + value);
		prop.setProperty(name, value);
		intAssignments.put(transition, prop);
	}

	public void removeIntAssign(String transition, String name) {
		if (intAssignments.containsKey(transition)) {
			Properties prop = intAssignments.get(transition);
			if (name != null && prop.containsKey(name)) {
				prop.remove(name);
			}
			intAssignments.put(transition, prop);
		}
	}

	public void removeAllAssign(String transition) {
		Properties prop = new Properties();
		if (booleanAssignments.containsKey(transition)) {
			booleanAssignments.put(transition, prop);
		}
		if (contAssignments.containsKey(transition)) {
			contAssignments.put(transition, prop);
		}
		if (rateAssignments.containsKey(transition)) {
			rateAssignments.put(transition, prop);
		}
		if (intAssignments.containsKey(transition)) {
			intAssignments.put(transition, prop);
		}
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
		rateAssignments.put(newName, rateAssignments.get(oldName));
		rateAssignments.remove(oldName);
		booleanAssignments.put(newName, booleanAssignments.get(oldName));
		booleanAssignments.remove(oldName);
		enablings.put(newName, enablings.get(oldName));
		enablings.remove(oldName);
	}

	public void changeDelay(String transition, String delay) {
		if (delays.containsKey(transition)) {
			delays.remove(transition);
		}
		// log.addText(transition + delay);
		delays.put(transition, delay);
	}

	public void changeEnabling(String transition, String enabling) {
		if (enablings.containsKey(transition)) {
			enablings.remove(transition);
		}
		enablings.put(transition, enabling);
	}

	public HashMap<String, Properties> getVariables() {
		return variables;
	}

	public HashMap<String, String> getIntegers() {
		return integers;
	}

	public HashMap<String, Boolean> getInputs() {
		return inputs;
	}

	public HashMap<String, Boolean> getOutputs() {
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

	public String[] getControlFlow() {
		String retString = "";
		for (String s : controlFlow.keySet()) {
			// log.addText("key: " + s);
			Properties prop = controlFlow.get(s);
			String toString = prop.getProperty("to");
			// log.addText("toString " + toString);
			if (toString != null) {
				String[] toArray = toString.split("\\s");
				for (int i = 0; i < toArray.length; i++) {
					retString = retString + s + " " + toArray[i] + "\n";
				}
			}
			// log.addText("getfrom");
			String fromString = prop.getProperty("from");
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
			if (inputs.get(var)) {
				return "true";
			}
			else {
				return "false";
			}
		}
		else if (isOutput(var)) {
			if (outputs.get(var)) {
				return "true";
			}
			else {
				return "false";
			}
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
		}
		for (int j = 0; j < outArray.length; j++) {
			vars[i] = outArray[j].toString();
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
			return null;
		}
	}

	public Properties getContVars(String trans) {
		// log.addText(trans);
		Properties contVars = new Properties();
		contVars = contAssignments.get(trans);
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

	public String getContAssign(String transition, String var) {
		if (contAssignments.containsKey(transition) && var != null) {
			Properties prop = contAssignments.get(transition);
			if (prop.containsKey(var)) {
				return prop.getProperty(var);
			}
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

	public String getRateAssign(String transition, String var) {
		Properties prop = rateAssignments.get(transition);
		if (prop != null && var != null) {
			return prop.getProperty(var);
		}
		return "";
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

	/*
	 * private void parseTransitions(StringBuffer data) { Pattern pattern =
	 * Pattern.compile(TRANSITION); Matcher line_matcher =
	 * pattern.matcher(data.toString()); Pattern output = Pattern.compile(WORD);
	 * Matcher matcher = output.matcher(line_matcher.group()); while
	 * (line_matcher.find()) { String name = matcher.group();
	 * controlFlow.put(name, name); } }
	 */

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
				String temp = placeMatcher.group(1).replaceAll("\\+/", "P");
				temp = temp.replaceAll("-/", "M");
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
					if (tempProp.containsKey("to")) {
						tempString = tempProp.getProperty("to");
						tempString = tempString + " " + tempPlace[1];
					}
					else {
						tempString = tempPlace[1];
					}
					tempProp.setProperty("to", tempString);
					controlFlow.put(tempPlace[0], tempProp);
					places.put(tempPlace[1], false);
					// trans = tempPlace[0];
				}
				else if (controlFlow.containsKey(tempPlace[1])) {
					Properties tempProp = controlFlow.get(tempPlace[1]);
					// log.addText("check2c");
					String tempString;
					// Boolean temp = tempProp.containsKey("from");
					// log.addText("check2c1");
					// log.addText(temp.toString());
					if (tempProp.containsKey("from")) {
						// log.addText("check2a if");
						tempString = tempProp.getProperty("from");
						// log.addText("check2a if1");
						tempString = tempString + " " + tempPlace[0];
					}
					else {
						// log.addText("check2a else");
						tempString = tempPlace[0];
					}
					// log.addText("check2d");
					// log.addText("check2d1");
					tempProp.setProperty("from", tempString);
					// log.addText("check2e");
					controlFlow.put(tempPlace[1], tempProp);
					places.put(tempPlace[0], false);
					// trans = tempPlace[1];
				}
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
			//System.out.println("checkifin");
			Pattern inPattern = Pattern.compile(WORD);
			Matcher inMatcher = inPattern.matcher(inLineMatcher.group());
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
				// log.addText("out " + outMatcher.group());
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
			Pattern initDigit = Pattern.compile("\\d+");
			Matcher digitMatcher = initDigit.matcher(initMatcher.group());
			digitMatcher.find();
			// log.addText("check1e");
			String[] initArray = new String[digitMatcher.group().length()];
			Pattern bit = Pattern.compile("[01]");
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
					inputs.put(name, true);
				}
				else {
					inputs.put(name, false);
				}
			}
			// log.addText("check1f");
			for (i = inLength; i < initArray.length; i++) {
				String name = varOrder.getProperty(i.toString());
				if (initArray[i].equals("1") && name != null) {
					outputs.put(name, true);
				}
				else if (name != null) {
					outputs.put(name, false);
				}
				i++;
			}
		}
	}

	private void parseVars(StringBuffer data) {
		// log.addText("check3 start");
		//System.out.println("check3 start");
		Properties initCond = new Properties();
		Properties initValue = new Properties();
		Properties initRate = new Properties();
		// log.addText("check3a");
		//System.out.println("check3a");
		Pattern linePattern = Pattern.compile(CONTINUOUS);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			// log.addText("check3b");
			//System.out.println("check3b");
			Pattern varPattern = Pattern.compile(WORD);
			Matcher varMatcher = varPattern.matcher(lineMatcher.group(1));
			// log.addText("check3c");
			//System.out.println("check3c");
			while (varMatcher.find()) {
				variables.put(varMatcher.group(), initCond);
			}
			// log.addText("check3d " + VARS_INIT);
			//System.out.println("check3c");
			Pattern initLinePattern = Pattern.compile(VARS_INIT);
			// log.addText("check3d1");
			//System.out.println("check3d");
			Matcher initLineMatcher = initLinePattern.matcher(data.toString());
			// log.addText("check3d2");
			//System.out.println("check3e");
			initLineMatcher.find();
			// log.addText("check3e");
			//System.out.println("check3f");
			Pattern initPattern = Pattern.compile(INIT_COND);
			Matcher initMatcher = initPattern.matcher(initLineMatcher.group(1));
			// log.addText("check3f");
			//System.out.println("check3g");
			while (initMatcher.find()) {
				if (variables.containsKey(initMatcher.group(1))) {
					initValue.put(initMatcher.group(1), initMatcher.group(2));
				}
			}
			// log.addText("check3g");
			//System.out.println("check3h");
			Pattern rateLinePattern = Pattern.compile(INIT_RATE);
			Matcher rateLineMatcher = rateLinePattern.matcher(data.toString());
			if (rateLineMatcher.find()) {
				// log.addText("check3h");
				//System.out.println("check3i");
				Pattern ratePattern = Pattern.compile(INIT_COND);
				Matcher rateMatcher = ratePattern.matcher(rateLineMatcher.group(1));
				// log.addText("check3i");
				//System.out.println("check3j");
				while (rateMatcher.find()) {
					// log.addText(rateMatcher.group(1) + "value" +
					// rateMatcher.group(2));
					initRate.put(rateMatcher.group(1), rateMatcher.group(2));
				}
			}
			// log.addText("check3j");
			//System.out.println("check3k");
			for (String s : variables.keySet()) {
				// log.addText("check3for" + s);
				//System.out.println("check3for " + s);
				initCond.put("value", initValue.get(s));
				initCond.put("rate", initRate.get(s));
				// log.addText("check3for" + initCond.toString());
				//System.out.println("check3for " + initCond.toString());
				variables.put(s, initCond);
			}
		}
		// log.addText("check3end");
		//System.out.println("check3end");
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
				//log.addText("check3for" + s);
				if (initValue.get(s) != null) {
					initCond = initValue.get(s).toString();
				}
				// log.addText("check3for" + initCond);
				integers.put(s, initCond);
			}
		}
		// log.addText("check3end");
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
				// log.addText(enabMatcher.group(2) + enabMatcher.group(4));
			}
		}
	}

	private void parseContAssign(StringBuffer data) {
		// log.addText("check6start");
		Properties assignProp = new Properties();
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
				// log.addText("check6while1");
				varMatcher = varPattern.matcher(assignMatcher.group(2));
				if (!varMatcher.find()) {
					varMatcher = indetPattern.matcher(assignMatcher.group(2));
				}
				while (varMatcher.find()) {
					// log.addText("check6while2");
					assignProp.put(varMatcher.group(1), varMatcher.group(2));
				}
				contAssignments.put(assignMatcher.group(1), assignProp);
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
				if (rateAssignments.containsKey(assignMatcher.group(1))) {
					assignProp = rateAssignments.get(assignMatcher.group(1));
				}
				// log.addText("here " + assignMatcher.group(2));
				varMatcher = varPattern.matcher(assignMatcher.group(2));
				// log.addText(assignMatcher.group(2) + " " + indetPattern);
				while (varMatcher.find()) {
					if (!assignProp.containsKey(varMatcher.group(1))) {
						assignProp.put(varMatcher.group(1), varMatcher.group(2));
					}
					// log.addText("rate " + varMatcher.group(1) + ":=" +
					// varMatcher.group(2));
				}
				indetMatcher = indetPattern.matcher(assignMatcher.group(2));
				while (indetMatcher.find()) {
					assignProp.put(indetMatcher.group(1), indetMatcher.group(2));
					// log.addText("indet " + indetMatcher.group(1) + ":=" +
					// indetMatcher.group(2));
				}
				// log.addText("rates for " + assignMatcher.group(1));
				// for (Object o : assignProp.keySet()) {
				// log.addText((String)o + " " +
				// assignProp.getProperty((String)o));
				// }
				rateAssignments.put(assignMatcher.group(1), assignProp);
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
			while (transMatcher.find()) {
				Properties prop = new Properties();
				Matcher assignMatcher = assignPattern.matcher(transMatcher.group(2));
				while (assignMatcher.find()) {
					prop.put(assignMatcher.group(1), assignMatcher.group(2));
				}
				booleanAssignments.put(transMatcher.group(1), prop);
			}
		}
	}

	private void parseIntAssign(StringBuffer data) {
		// log.addText("check6start");
		Properties assignProp = new Properties();
		Pattern linePattern = Pattern.compile(INT_ASSIGNMENT_LINE);
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
				// log.addText("check6while1");
				varMatcher = varPattern.matcher(assignMatcher.group(2));
				if (!varMatcher.find()) {
					varMatcher = indetPattern.matcher(assignMatcher.group(2));
				}
				else {
					varMatcher = varPattern.matcher(assignMatcher.group(2));
				}
				// log.addText(varMatcher.toString());
				while (varMatcher.find()) {
					// log.addText("check6while2");
					assignProp.put(varMatcher.group(1), varMatcher.group(2));
				}
				// log.addText(assignMatcher.group(1) + assignProp.toString());
				intAssignments.put(assignMatcher.group(1), assignProp);
			}
		}
		// log.addText("check6end");
	}

	private static final String INPUT = "\\.inputs([[\\s[^\\n]]\\w+]*?)\\n";

	private static final String OUTPUT = "\\.outputs([[\\s[^\\n]]\\w+]*?)\\n";

	private static final String INIT_STATE = "#@\\.init_state \\[(\\d+)\\]";

	private static final String TRANSITION = "\\.dummy([^\\n]*?)\\n";

	private static final String PLACE = "\\n([^.#][\\w_\\+-/]+ [\\w_\\+-/]+)";

	private static final String CONTINUOUS = "#@\\.continuous ([.[^\\n]]+)\\n";

	private static final String VARIABLES = "#@\\.variables ([.[^\\n]]+)\\n";

	private static final String VARS_INIT = "#@\\.init_vals \\{([\\S[^\\}]]+?)\\}";

	private static final String INIT_RATE = "#@\\.init_rates \\{([\\S[^\\}]]+?)\\}";

	private static final String INIT_COND = "<(\\w+)=([\\S^>]+?)>";

	// private static final String INTS_INIT = "#@\\.init_ints
	// \\{([\\S[^\\}]]+?)\\}";

	private static final String MARKING_LINE = "\\.marking \\{(.+)\\}";

	private static final String MARKING = "\\w+";

	private static final String ENABLING_LINE = "#@\\.enablings \\{([.[^\\}]]+?)\\}";

	private static final String ENABLING = "(<([\\S[^=]]+?)=(\\[([^\\]]+?)\\])+?>)?";

	private static final String ASSIGNMENT_LINE = "#@\\.assignments \\{([.[^\\}]]+?)\\}";

	private static final String INT_ASSIGNMENT_LINE = "#@\\.int_assignments \\{([.[^\\}]]+?)\\}";

	private static final String RATE_ASSIGNMENT_LINE = "#@\\.rate_assignments \\{([.[^\\}]]+?)\\}";

	private static final String ASSIGNMENT = "<([\\S[^=]]+?)=\\[([^>]+?)\\]>";

	private static final String ASSIGN_VAR = "([\\S[^:]]+?):=([\\S]+)";

	private static final String INDET_ASSIGN_VAR = "([\\S[^:]]+?):=(\\[[-\\d]+,[-\\d]+\\])";

	private static final String DELAY_LINE = "#@\\.delay_assignments \\{([\\S[^\\}]]+?)\\}";

	private static final String DELAY = "<([\\w_]+)=(\\[\\d+,\\d+\\])>";

	private static final String BOOLEAN_LINE = "#@\\.boolean_assignments \\{([\\S[^\\}]]+?)\\}";

	private static final String BOOLEAN_TRANS = "<([\\w_]+?)=([\\S[^>]]+?)>";

	private static final String BOOLEAN_ASSIGN = "\\[([\\w_]+):=([\\S^\\]]+)\\]";

	private static final String WORD = "(\\S+)";

}