package lhpn2sbml.parser;

//import gcm2sbml.util.GlobalConstants;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private HashMap<String, Properties> transitions;

	private HashMap<String, Properties> variables;

	private HashMap<String, Properties> rateAssignments;
	
	private HashMap<String, String> delays;
	
	private HashMap<String, HashMap<String, String>> booleanAssignments;

	public void save(String filename) {
		try {
			PrintStream p = new PrintStream(new FileOutputStream(filename));
			StringBuffer buffer = new StringBuffer(".outputs ");
			HashMap<String, Integer> boolOrder = new HashMap<String, Integer>();
			int i = 0;
			for (String s : inputs.keySet()) {
				buffer.append(s + " ");
				boolOrder.put(s, i);
				i++;
			}
			for (String s : outputs.keySet()) {
				buffer.append(s + " ");
				boolOrder.put(s, i);
				i++;
			}
			buffer.append("\n.dummy ");
			for (String s : transitions.keySet()) {
				buffer.append(s + " ");
			}
			buffer.append("\n#@.variables ");
			for (String s : variables.keySet()) {
				buffer.append(s + " ");
			}
			buffer.append("\n#@.init_state [");
			for (i=0; i<boolOrder.size(); i++) {
				for (String s : inputs.keySet()) {
					if (boolOrder.get(s).equals(i)) {
						buffer.append(inputs.get(s));
					}
				}
			}
			buffer.append("]\n.graph\n");
			for (String s : transitions.keySet()) {
				Properties prop = transitions.get(s);
				String toString = prop.getProperty(s + "to");
				String[] toArray = toString.split("\\s");
				for (i=0; i<toArray.length; i++) {
					buffer.append(toArray[i] + " " + transitions.get(s) + "\n");
				}
				String fromString = prop.getProperty(s + "to");
				String[] fromArray = fromString.split("\\s");
				for (i=0; i<fromArray.length; i++) {
					buffer.append(transitions.get(s) + " " + fromArray[i] + "\n");
				}
			}
			buffer.append(".marking {");
			for (String s : places.keySet()) {
				if (places.get(s).equals(true)) {
					buffer.append(places.get(s) + " ");
				}
			}
			buffer.append("}\n#@.init_vals {");
			for (String s : variables.keySet()) {
				Properties prop = variables.get(s);
				buffer.append("<" + s + "=" + prop.getProperty("value") + ">");
			}
			buffer.append("}\n#@.init_rates {");
			for (String s : variables.keySet()) {
				Properties prop = variables.get(s);
				buffer.append("<" + s + "=" + prop.getProperty("rate") + ">");
			}
			buffer.append("}\n.enablings {");
			for (String s : enablings.keySet()) {
				buffer.append("<" + s + "=[" + enablings.get(s) + "]>");
			}
			buffer.append("}\n#@.delay_assignments {");
			for (String s : delays.keySet()) {
				buffer.append("<" + s + "=" + delays.get(s) + ">");
			}
			buffer.append("}\n#@.boolean_assignments {");
			for (String s : booleanAssignments.keySet()) {
				buffer.append("<" + s + "=");
				HashMap<String, String> prop = booleanAssignments.get(s);
				for (String key : prop.keySet()) {
					buffer.append("[" + key + ":=" + prop.get(key) + "]");
				}
				buffer.append(">");
			}
			buffer.append("}\n#@.continuous ");
			for (String s : variables.keySet()) {
				buffer.append(s + " ");
			}
			buffer.append("\n.end\n");
			p.print(buffer);
			p.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void load(String filename) {
		places = new HashMap<String, Boolean>();
		inputs = new HashMap<String, Boolean>();
		outputs = new HashMap<String, Boolean>();
		enablings = new HashMap<String, String>();
		transitions = new HashMap<String, Properties>();
		variables = new HashMap<String, Properties>();
		rateAssignments = new HashMap<String, Properties>();
		StringBuffer data = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
			}
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}

		try {
			parseInOut(data);
			// parseTransitions(data);
			parseControlFlow(data);
			parseVars(data);
			parseMarking(data);
			parseEnabling(data);
			parseRateAssign(data);
			parseDelayAssign(data);
			parseBooleanAssign(data);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to parse LHPN");
		}
	}

	private void addPlace(String name, Boolean ic) {
		places.put(name, ic);
	}

	private void removePlace(String name) {
		if (name != null && places.containsKey(name)) {
			places.remove(name);
		}
	}
	
	private void addInput(String name, Boolean ic) {
		inputs.put(name, ic);
	}

	private void removeInput(String name) {
		if (name != null && inputs.containsKey(name)) {
			inputs.remove(name);
		}
	}

	private void addOutput(String name, Boolean ic) {
		outputs.put(name, ic);
	}

	private void removeOutput(String name) {
		if (name != null && outputs.containsKey(name)) {
			outputs.remove(name);
		}
	}

	private void addFlow(String from, String to) {
		String name = "";
		Properties flow = new Properties();
		if (transitions.containsKey(from)) {
			name = from;
			flow = transitions.get(from);
			flow.setProperty(name + "to", flow.getProperty(name + "to") + to + " ");
		}
		else if (transitions.containsKey(to)) {
			name = to;
			flow = transitions.get(to);
			flow.setProperty(name + "from", flow.getProperty(name + "from") + to + " ");
		}
		transitions.put(name, flow);
	}

	private void removeFlow(String from, String to) {
		String name = "";
		Properties flow = new Properties();
		if (transitions.containsKey(from)) {
			name = from;
			flow = transitions.get(from);
			String toString = flow.getProperty(name + "to");
			String[] toArray = toString.split("\\s");
			boolean flag = false;
			for (int i=0; i<toArray.length; i++) {
				if (flag) {
					toArray[i-1] = toArray[i];
				}
				if (toArray[i].equals(to)) {
					flag = true;
				}
			}
			flow.setProperty(name + "to", "");
			for (int i=0; i<toArray.length - 1; i++) {
				flow.setProperty(name + "to", flow.getProperty(name + "to") + toArray[i] + " ");
			}
		}
		else if (transitions.containsKey(to)) {
			name = to;
			flow = transitions.get(to);
			String fromString = flow.getProperty(name + "from");
			String[] fromArray = fromString.split("\\s");
			boolean flag = false;
			for (int i=0; i<fromArray.length; i++) {
				if (flag) {
					fromArray[i-1] = fromArray[i];
				}
				if (fromArray[i].equals(to)) {
					flag = true;
				}
			}
			flow.setProperty(name + "from", "");
			for (int i=0; i<fromArray.length - 1; i++) {
				flow.setProperty(name + "from", flow.getProperty(name + "from") + fromArray[i] + " ");
			}
		}
	}
	
	private void addTransition(String name) {
		transitions.put(name, null);
	}
	
	private void addTransition(String name, Properties prop) {
		transitions.put(name, prop);
	}
	
	private void removeTransition(String name) {
		transitions.remove(name);
	}

	private void addEnabling(String name, String cond) {
		enablings.put(name, cond);
	}

	private void removeEnabling(String name) {
		if (name != null && enablings.containsKey(name)) {
			enablings.remove(name);
		}
	}

	private void addVar(String name, Properties initCond) {
		variables.put(name, initCond);
	}

	private void removeVar(String name) {
		if (name != null && variables.containsKey(name)) {
			variables.remove(name);
		}
	}

	private void addRateAssign(String name, Properties assign) {
		rateAssignments.put(name, assign);
	}

	private void removeRateAssign(String name) {
		if (name != null && rateAssignments.containsKey(name)) {
			rateAssignments.remove(name);
		}
	}

	/*
	 * private void parseTransitions(StringBuffer data) { Pattern pattern =
	 * Pattern.compile(TRANSITION); Matcher line_matcher =
	 * pattern.matcher(data.toString()); Pattern output = Pattern.compile(WORD);
	 * Matcher matcher = output.matcher(line_matcher.group()); while
	 * (line_matcher.find()) { String name = matcher.group();
	 * transitions.put(name, name); } }
	 */

	private void parseControlFlow(StringBuffer data) {
		Pattern pattern = Pattern.compile(TRANSITION);
		Matcher lineMatcher = pattern.matcher(data.toString());
		while (lineMatcher.find()) {
			String name = lineMatcher.group(3);
			transitions.put(name, null);
		}
		Pattern placePattern = Pattern.compile(PLACE);
		Matcher placeMatcher = placePattern.matcher(data.toString());
		while (placeMatcher.find()) {
			String[] tempLine = placeMatcher.group().split("#");
			String[] tempPlace = tempLine[0].split("\\s");
			//String trans = "";
			if (transitions.containsKey(tempPlace[0])) {
				Properties tempProp = transitions.get(tempPlace[0]);
				tempProp.setProperty(tempPlace[0] + "to", tempProp.getProperty(tempPlace[0] + "to") + tempPlace[1] + " ");
				transitions.put(tempPlace[0], tempProp);
				places.put(tempPlace[1], false);
			//	trans = tempPlace[0];
			}
			else {
				Properties tempProp = transitions.get(tempPlace[1]);
				tempProp.setProperty(tempPlace[1] + "from", tempProp.getProperty(tempPlace[1] + "from") + tempPlace[0] + " ");
				transitions.put(tempPlace[1], tempProp);
				places.put(tempPlace[0], false);
			//	trans = tempPlace[1];
			}
			//if (tempLine[1].contains("#@")) {
			//	Pattern delayPattern = Pattern.compile(DELAY);
			//	Matcher delayMatcher = delayPattern.matcher(tempLine[1]);
			//	delays.put(trans, delayMatcher.group());
			//	transitions.put(tempPlace[0], null);
			//}
			// Pattern place = Pattern.compile(WORD);
			// Matcher placeMatch = place.matcher(matcher.group());
			// while (placeMatch.find()) {
			// String placeName = placeMatch.group();
			// if (!transitions.containsKey(placeName)) {
			// places.put(placeName, false);
			// }
			// }
		}
	}

	private void parseInOut(StringBuffer data) {
		Pattern inPattern = Pattern.compile(INPUT);
		Matcher inLineMatcher = inPattern.matcher(data.toString());
		Pattern outPattern = Pattern.compile(OUTPUT);
		Matcher outLineMatcher = outPattern.matcher(data.toString());
		Pattern output = Pattern.compile(WORD);
		Matcher matcher = output.matcher(outLineMatcher.group());
		Pattern initState = Pattern.compile(INIT_STATE);
		Matcher initMatcher = initState.matcher(data.toString());
		Pattern initDigit = Pattern.compile("\\d+");
		Matcher digitMatcher = initDigit.matcher(initMatcher.group());
		String[] initArray = new String[digitMatcher.group().length()];
		Pattern bit = Pattern.compile("[01]");
		Matcher bitMatcher = bit.matcher(digitMatcher.group());
		int i = 0;
		while (bitMatcher.find()) {
			initArray[i] = bitMatcher.group();
			i++;
		}
		while (inLineMatcher.find()) {
			String name = matcher.group();
			if (initArray[i].equals("1")) {
				inputs.put(name, true);
			}
			else {
				inputs.put(name, false);
			}
			i++;
		}
		while (outLineMatcher.find()) {
			String name = matcher.group();
			if (initArray[i].equals("1")) {
				outputs.put(name, true);
			}
			else {
				outputs.put(name, false);
			}
			i++;
		}
	}

	private void parseVars(StringBuffer data) {
		Properties initCond = new Properties();
		Properties initValue = new Properties();
		Properties initRate = new Properties();
		Pattern linePattern = Pattern.compile(VARIABLES);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		Pattern varPattern = Pattern.compile(WORD);
		Matcher varMatcher = varPattern.matcher(lineMatcher.group(1));
		while (varMatcher.find()) {
			variables.put(varMatcher.group(), initCond);
		}
		linePattern = Pattern.compile(VARS_INIT);
		lineMatcher = linePattern.matcher(data.toString());
		Pattern initPattern = Pattern.compile(INIT_COND);
		Matcher initMatcher = initPattern.matcher(lineMatcher.group(1));
		while (initMatcher.find()) {
			initValue.put(initMatcher.group(1), initMatcher.group(2));
		}
		linePattern = Pattern.compile(INIT_RATE);
		lineMatcher = linePattern.matcher(data.toString());
		Pattern ratePattern = Pattern.compile(INIT_COND);
		Matcher rateMatcher = ratePattern.matcher(lineMatcher.group(1));
		while (rateMatcher.find()) {
			initRate.put(rateMatcher.group(1), rateMatcher.group(2));
		}
		for (String s : variables.keySet()) {
			initCond.put("value", initValue.get(s));
			initCond.put("rate", initRate.get(s));
			variables.put(s, initCond);
		}
	}

	private void parseMarking(StringBuffer data) {
		Pattern linePattern = Pattern.compile(MARKING_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		Pattern markPattern = Pattern.compile(MARKING);
		Matcher markMatcher = markPattern.matcher(lineMatcher.group());
		while (markMatcher.find()) {
			places.put(markMatcher.group(), true);
		}
	}

	private void parseEnabling(StringBuffer data) {
		Pattern linePattern = Pattern.compile(ENABLING_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		Pattern enabPattern = Pattern.compile(ENABLING);
		Matcher enabMatcher = enabPattern.matcher(lineMatcher.group());
		while (enabMatcher.find()) {
			enablings.put(enabMatcher.group(1), enabMatcher.group(2));
		}
	}

	private void parseRateAssign(StringBuffer data) {
		Properties assignProp = new Properties();
		Pattern linePattern = Pattern.compile(ASSIGNMENT_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		Pattern assignPattern = Pattern.compile(ASSIGNMENT);
		Matcher assignMatcher = assignPattern.matcher(lineMatcher.group());
		Pattern varPattern = Pattern.compile(ASSIGN_VAR);
		Matcher varMatcher;
		while (assignMatcher.find()) {
			varMatcher = varPattern.matcher(assignMatcher.group(2));
			while (varMatcher.find()) {
				assignProp.put(varMatcher.group(1), varMatcher.group(2));
			}
			rateAssignments.put(assignMatcher.group(1), assignProp);
		}
	}
	
	private void parseDelayAssign(StringBuffer data) {
		Pattern linePattern = Pattern.compile(DELAY_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		Pattern delayPattern = Pattern.compile(DELAY);
		Matcher delayMatcher = delayPattern.matcher(lineMatcher.group(1));
		while (delayMatcher.find()) {
			delays.put(delayMatcher.group(1), delayMatcher.group(2));
		}
	}
	
	private void parseBooleanAssign(StringBuffer data) {
		Pattern linePattern = Pattern.compile(BOOLEAN_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		Pattern transPattern = Pattern.compile(BOOLEAN_TRANS);
		Matcher transMatcher = transPattern.matcher(lineMatcher.group());
		Pattern assignPattern = Pattern.compile(BOOLEAN_ASSIGN);
		while (transMatcher.find()) {
			HashMap<String, String> prop = new HashMap<String,String>();
			Matcher assignMatcher = assignPattern.matcher(transMatcher.group(2));
			while (assignMatcher.find()) {
				prop.put(assignMatcher.group(1), assignMatcher.group(2));
			}
			booleanAssignments.put(transMatcher.group(1), prop);
		}
	}

	private static final String INPUT = "\\.inputs([\\s[^\\n]](\\w+))*\\n";

	private static final String OUTPUT = "\\.outputs([\\s[^\\n]](\\w+))*\\n";

	private static final String INIT_STATE = "#@\\.init_state \\[\\d+\\]";

	private static final String TRANSITION = "\\.dummy(([\\s[^\\n]](\\S+))*)\\n";
	private static final String PLACE = "\\n([^\\.#][.[^\\n]]+)";
	
	private static final String VARIABLES = "#@\\.variables ([.[^\\n]]+)\\n";
	private static final String VARS_INIT = "#@\\.init_vals{([\\S[^}]]+)}";
	private static final String INIT_RATE = "#@\\.init_rates{([\\S[^}]]+)}";
	private static final String INIT_COND = "<(\\w+)=(\\d+)>";
	
	private static final String MARKING_LINE = "\\.marking {(.+)}";
	private static final String MARKING = "\\w+";
	
	private static final String ENABLING_LINE = "#@\\.enablings {([.[^}]]+)}";
	private static final String ENABLING = "<([\\S[^=]]+)=\\[([.[^\\]]]+)\\]>";
	
	private static final String ASSIGNMENT_LINE = "#@\\.rate_assignments {([.[^}]]+)}";
	private static final String ASSIGNMENT = "<([\\S[^=]]+)=([.[^>]]+)>";
	private static final String ASSIGN_VAR = "\\[([\\S[^:]]+):=([-\\d]+)\\]";

	private static final String DELAY_LINE = "#@\\.delay_assignments {([\\S+)[}]]+)}";
	private static final String DELAY = "(<([\\w_]+)=(\\[\\d+,\\d+\\])>";
	
	private static final String BOOLEAN_LINE = "#@\\.boolean_assignments {([\\S[^}]]+)}";
	private static final String BOOLEAN_TRANS = "<([\\w_]+)=<([\\S[^>]]+)>";
	private static final String BOOLEAN_ASSIGN = "\\[([\\w_]+):=(\\w+)\\]";
	
	private static final String WORD = "(\\S+)";

}