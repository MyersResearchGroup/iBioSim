package backend.lpn.parser;


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import backend.biomodel.util.Utility;
import backend.verification.Verification;
import backend.verification.platu.lpn.DualHashMap;
import backend.verification.platu.stategraph.StateGraph;
import backend.verification.timed_state_exploration.zoneProject.InequalityVariable;
import frontend.main.Gui;
import frontend.main.Log;


public class LhpnFile {

	protected String separator;

	protected HashMap<String, Transition> transitions;

	protected HashMap<String, Place> places;
	
	//protected HashMap<String, Integer> placesIndices;

	protected HashMap<String, Variable> booleans;

	protected HashMap<String, Variable> continuous;

	protected HashMap<String, Variable> integers;

	protected ArrayList<Variable> variables;

	protected ArrayList<String> properties;

	protected Log log;
	
	protected String label;
	
	protected int tranIndex;
	
	protected int lpnIndex;

	/* 
	 * Cached value of the map that associates a variable name with its
	 * index. This field is initialized when a call to getVarIndexMap
	 * is made.
	 */
	protected DualHashMap<String, Integer> _varIndexMap;

	/*
	 * Cached value of the map that associates a continuous variable with
	 * its index. This field is initialized when a call to getContinuousIndexMap
	 * is made.
	 */
	DualHashMap<String, Integer> _continuousIndexMap;
	
	/* 
	 * Cached value of the array of all the places in this LPN. This field is 
	 * initialized when a call to getPlaceList is made.
	 */
	protected String[] placeList;
	
	/* 
	 * Cached value of all the transition in this LPN. This field is 
	 * initialized when a call to getAllTransitions is made.
	 */
	protected Transition[] allTransitions;
	
	/*
	 * The i-th array in this list stores THIS Lpn's variable indices of the shared variables 
	 * between this LPN and another LPN whose lpnIndex is i. 
	 */
	protected List<int[]> thisIndexList;
	
	/*
	 * The i-th array in this list stores THE i-th Lpn's variable indices of the
	 * shared variables between this LPN and the i-th LPN.
	 */
    protected List<int[]> otherIndexList;
    
    /*
     * The local state graph that corresponds to this LPN.  
     */
    protected StateGraph stateGraph;
    
    protected HashMap<String,String> implicitPlaceMap;
    
    private static int implicitPlaceCount=0;
	
	public LhpnFile(Log log) {
		separator = Gui.separator;
		this.log = log;
		transitions = new HashMap<String, Transition>();
		places = new HashMap<String, Place>();
		implicitPlaceMap = new HashMap<String,String>();
		booleans = new HashMap<String, Variable>();
		continuous = new HashMap<String, Variable>();
		integers = new HashMap<String, Variable>();
		variables = new ArrayList<Variable>();
		properties = new ArrayList<String>();
		lpnIndex = 0;
		tranIndex = 0;		
	}
	
	public LhpnFile() {
		separator = Gui.separator;
		transitions = new HashMap<String, Transition>();
		places = new HashMap<String, Place>();
		implicitPlaceMap = new HashMap<String,String>();
		booleans = new HashMap<String, Variable>();
		continuous = new HashMap<String, Variable>();
		integers = new HashMap<String, Variable>();
		variables = new ArrayList<Variable>();
		properties = new ArrayList<String>();
		lpnIndex = 0;
		tranIndex = 0;
		thisIndexList = new ArrayList<int[]>();
		otherIndexList = new ArrayList<int[]>();		
	}
	
	public void save(String filename) {
		try {
			String file = filename;
			PrintStream p = new PrintStream(new FileOutputStream(filename));
			StringBuffer buffer = new StringBuffer();
			HashMap<String, Integer> boolOrder = new HashMap<String, Integer>();
			int i = 0;
			boolean flag = false;
			for (String s : booleans.keySet()) {
				if (booleans.get(s) != null) {
					if (booleans.get(s).isInput()) {
						if (!flag) {
							buffer.append(".inputs ");
							flag = true;
						}
						buffer.append(s + " ");
						boolOrder.put(s, i);
						i++;
					}
				}
			}
			for (String s : continuous.keySet()) {
				if (continuous.get(s) != null) {
					if (continuous.get(s).isInput()) {
						if (!flag) {
							buffer.append(".inputs ");
							flag = true;
						}
						buffer.append(s + " ");
					}
				}
			}
			for (String s : integers.keySet()) {
				if (integers.get(s) != null) {
					if (integers.get(s).isInput()) {
						if (!flag) {
							buffer.append(".inputs ");
							flag = true;
						}
						buffer.append(s + " ");
					}
				}
			}
			if (flag) buffer.append("\n");
			flag = false;
			for (String s : booleans.keySet()) {
				if (booleans.get(s) != null) {
					if (!flag) {
						buffer.append(".outputs ");
						flag = true;
					}
					if (booleans.get(s).isOutput()) {
						buffer.append(s + " ");
						boolOrder.put(s, i);
						i++;
					}
				}
			}
			for (String s : continuous.keySet()) {
				if (continuous.get(s) != null) {
					if (continuous.get(s).isOutput()) {
						if (!flag) {
							buffer.append(".outputs ");
							flag = true;
						}
						buffer.append(s + " ");
					}
				}
			}
			for (String s : integers.keySet()) {
				if (integers.get(s) != null) {
					if (integers.get(s).isOutput()) {
						if (!flag) {
							buffer.append(".outputs ");
							flag = true;
						}
						buffer.append(s + " ");
					}
				}
			}
			if (flag) buffer.append("\n");
			flag = false;
			for (String s : booleans.keySet()) {
				if (booleans.get(s) != null) {
					if (!flag) {
						buffer.append(".internal ");
						flag = true;
					}
					if (booleans.get(s).isInternal()) {
						buffer.append(s + " ");
						boolOrder.put(s, i);
						i++;
					}
				}
			}
			for (String s : continuous.keySet()) {
				if (continuous.get(s) != null) {
					if (continuous.get(s).isInternal()) {
						if (!flag) {
							buffer.append(".internal ");
							flag = true;
						}
						buffer.append(s + " ");
					}
				}
			}
			for (String s : integers.keySet()) {
				if (integers.get(s) != null) {
					if (integers.get(s).isInternal()) {
						if (!flag) {
							buffer.append(".internal ");
							flag = true;
						}
						buffer.append(s + " ");
					}
				}
			}
			if (flag) buffer.append("\n");
			if (!transitions.isEmpty()) {
				buffer.append(".dummy ");
				for (String s : transitions.keySet()) {
					buffer.append(s + " ");
				}
				buffer.append("\n");
			}
			if (!continuous.isEmpty() || !integers.isEmpty()) {
				buffer.append("#@.variables ");
				for (String s : continuous.keySet()) {
					buffer.append(s + " ");
				}
				for (String s : integers.keySet()) {
					buffer.append(s + " ");
				}
				buffer.append("\n");
			}
			if (!transitions.isEmpty()) {
				flag = false;
				for (Transition t : transitions.values()) {
					if (t.isFail()) {
						if (!flag) {
							buffer.append("#@.failtrans ");
						}
						buffer.append(t.getLabel() + " ");
						flag = true;
					}
				}
				if (flag) {
					buffer.append("\n");
				}
				flag = false;
				for (Transition t : transitions.values()) {
					if (t.isPersistent()) {
						if (!flag) {
							buffer.append("#@.non_disabling ");
						}
						buffer.append(t.getLabel() + " ");
						flag = true;
					}
				}
				if (flag) {
					buffer.append("\n");
				}
			}
			if (!places.isEmpty()) {
				buffer.append("#|.places ");
				for (String s : places.keySet()) {
					buffer.append(s + " ");
				}
				buffer.append("\n");
			}
			if (!booleans.isEmpty()) {
				flag = false;
				for (i = 0; i < boolOrder.size(); i++) {
					for (String s : booleans.keySet()) {
						if (boolOrder.get(s).equals(i)) {
							if (!flag) {
								buffer.append("#@.init_state [");
								flag = true;
							}
							if (booleans.get(s).getInitValue().equals("true")) {
								buffer.append("1");
							} else if (booleans.get(s).getInitValue().equals(
									"false")) {
								buffer.append("0");
							} else {
								buffer.append("X");
							}
						}
					}
				}
				if (flag) {
					buffer.append("]\n");
				}
			}
			if (!transitions.isEmpty()) {
				buffer.append(".graph\n");
				for (Transition t : transitions.values()) {
					for (Place s : t.getPreset()) {
						buffer.append(s.getName() + " " + t.getLabel() + "\n");
					}
					for (Place s : t.getPostset()) {
						buffer.append(t.getLabel() + " " + s.getName() + "\n");
					}
				}
			}
			flag = false;
			if (!places.keySet().isEmpty()) {
				for (Place place : places.values()) {
					if (place.isMarked()) {
						if (!flag) {
							buffer.append(".marking {");
							flag = true;
						}
						buffer.append(place.getName() + " ");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			if (properties != null && !properties.isEmpty()) {
//				if (!properties.contains("none"))
//					properties.add(0, "none");
				for (String property : properties) {
					buffer.append("#@.property ");
					buffer.append(property + "\n");
				}
			}
			if (!continuous.isEmpty() || !integers.isEmpty()) {
				buffer.append("#@.init_vals {");
				for (Variable var : continuous.values()) {
					buffer.append("<" + var.getName() + "="
							+ var.getInitValue() + ">");
				}
				for (Variable var : integers.values()) {
					buffer.append("<" + var.getName() + "="
							+ var.getInitValue() + ">");
				}
				if (!continuous.isEmpty()) {
					buffer.append("}\n#@.init_rates {");
					for (Variable var : continuous.values()) {
						buffer.append("<" + var.getName() + "="
								+ var.getInitRate() + ">");
					}
				}
				buffer.append("}\n");
			}
			if (!transitions.isEmpty()) {
				flag = false;
				for (Transition t : transitions.values()) {
					if (t.getEnabling() != null && !t.getEnabling().equals("")) {
						if (!flag) {
							buffer.append("#@.enablings {");
							flag = true;
						}
						buffer.append("<" + t.getLabel() + "=["
								+ t.getEnabling() + "]>");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
				flag = false;
				for (Transition t : transitions.values()) {
					HashMap<String, String> contAssign = t.getContAssignments();
					if (!contAssign.isEmpty()) {
						if (!flag) {
							buffer.append("#@.assignments {");
							flag = true;
						}
						for (String var : contAssign.keySet()) {
							buffer.append("<" + t.getLabel() + "=[" + var + ":="
									+ contAssign.get(var) + "]>");
						}
					}
					HashMap<String, String> intAssign = t.getIntAssignments();
					if (!intAssign.isEmpty()) {
						if (!flag) {
							buffer.append("#@.assignments {");
							flag = true;
						}
						for (String var : intAssign.keySet()) {
							buffer.append("<" + t.getLabel() + "=[" + var + ":="
									+ intAssign.get(var) + "]>");
						}
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
				flag = false;
				for (Transition t : transitions.values()) {
					HashMap<String, String> rateAssign = t.getRateAssignments();
					for (String var : rateAssign.keySet()) {
						if (!var.equals("")) {
							if (!flag) {
								buffer.append("#@.rate_assignments {");
								flag = true;
							}
							buffer.append("<" + t.getLabel() + "=[" + var + ":="
									+ t.getRateAssignment(var) + "]>");
						}
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
				flag = false;
				for (Transition t : transitions.values()) {
					if (t.containsDelay()) {
						if (!flag) {
							buffer.append("#@.delay_assignments {");
							flag = true;
						}
						buffer.append("<" + t.getLabel() + "=[" + t.getDelay()
								+ "]>");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
				flag = false;
				for (Transition t : transitions.values()) {
					if (t.containsPriority()) {
						if (!flag) {
							buffer.append("#@.priority_assignments {");
							flag = true;
						}
						buffer.append("<" + t.getLabel() + "=["
								+ t.getPriority() + "]>");
					}
				}
				if (flag) {
					buffer.append("}\n");
				}
			}
			flag = false;
			for (Transition t : transitions.values()) {
				HashMap<String, String> boolAssign = t.getBoolAssignments();
				for (String var : boolAssign.keySet()) {
					if (!flag) {
						buffer.append("#@.boolean_assignments {");
						flag = true;
					}
					buffer.append("<" + t.getLabel() + "=[" + var + ":="
							+ boolAssign.get(var) + "]>");
				}
			}
			if (flag) {
				buffer.append("}\n");
			}
			buffer.append("#@.continuous ");
			for (String s : continuous.keySet()) {
				buffer.append(s + " ");
			}
			buffer.append("\n");
			if (buffer.toString().length() > 0) {
				buffer.append(".end\n");
			}
			p.print(buffer);
			p.close();
			if (log != null) {
				log.addText("Saving:\n" + file + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load(String filename) {
		StringBuffer data = new StringBuffer();
		label = filename.split(separator)[filename.split(separator).length - 1].replace(".lpn","");
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}

		parseProperty(data);
		parsePlaces(data);
		parseControlFlow(data);
		parseVars(data);
		parseIntegers(data);
		parseInOut(data);
		parseMarking(data);
		boolean error = parseEnabling(data);
		error = parseAssign(data, error);
		error = parseRateAssign(data, error);
		error = parseDelayAssign(data, error);
		error = parsePriorityAssign(data, error);
		error = parseBooleanAssign(data, error);
		error = parseTransitionRate(data, error);
		parseFailTransitions(data);
		parsePersistentTransitions(data);

		if (!error) {
			Utility
					.createErrorMessage("Invalid Expressions",
							"The input file contained invalid expressions.  See console for details.");
		}
	}
	
	public void printDot(String filename) {
		try {
			String file = filename;
			PrintStream p = new PrintStream(new FileOutputStream(filename));
			StringBuffer buffer = new StringBuffer();
			buffer.append("digraph G {\nsize=\"7.5,10\"\n");
			buffer.append("Inits [shape=plaintext,label=\"");
			for (Variable v : booleans.values()) {
				buffer.append(v.getName() + " = " + v.getInitValue() + "\\n");
			}
			for (Variable v : integers.values()) {
				buffer.append(v.getName() + " = " + v.getInitValue() + "\\n");
			}
			for (Variable v : continuous.values()) {
				buffer.append(v.getName() + " = " + v.getInitValue() + "\\n" + v.getName() + "' = " + v.getInitRate() + "\\n");
			}
			buffer.append("\"]\n");
			for (Transition t : transitions.values()) {
				buffer.append(t.getLabel() + " [shape=plaintext,label=\"" + t.getLabel());
				if (t.containsEnabling()) {
					if (t.isPersistent()) {
						buffer.append("\\n{" + t.getEnabling() + "}");
					}
					else {
						buffer.append("\\n{" + t.getEnabling() + "}");
					}
				}
				if (t.containsDelay()) {
					buffer.append("\\n[" + t.getDelay() + "]");
				}
				if (t.containsAssignment()) {
					buffer.append("\\n<");
					boolean flag = false;
					if (t.containsBooleanAssignment()) {
						HashMap<String, String> map = t.getBoolAssignments();
						for (String v : map.keySet()) {
							if (flag) {
								buffer.append(",");
							}
							else {
								flag = true;
							}
							buffer.append(v + ":="
									+ t.getBoolAssignment(v));
						}
					}
					if (t.containsContinuousAssignment()) {
						HashMap<String, String> map = t.getContAssignments();
						for (String v : map.keySet()) {
							if (flag) {
								buffer.append(",");
							}
							else {
								flag = true;
							}
							buffer.append(v + ":=" + t.getContAssignment(v));
						}
					}
					if (t.containsIntegerAssignment()) {
						HashMap<String, String> map = t.getIntAssignments();
						for (String v : map.keySet()) {
							if (flag) {
								buffer.append(",");
							}
							else {
								flag = true;
							}
							buffer.append(v + ":=" + t.getIntAssignment(v));
						}
					}
					if (t.containsRateAssignment()) {
						HashMap<String, String> map = t.getRateAssignments();
						for (String v : map.keySet()) {
							if (flag) {
								buffer.append(",");
							}
							else {
								flag = true;
							}
							buffer.append(v + "':=" + t.getRateAssignment(v));
						}
					}
					buffer.append(">");
				}
				buffer.append("\"");
				if (t.isFail()&&t.isPersistent()) {
					buffer.append(",fontcolor=purple");
				}
				else if (t.isFail()) {
					buffer.append(",fontcolor=red");
				}
				else if (t.isPersistent()) {
					buffer.append(",fontcolor=blue");
				}
				buffer.append("];\n");
			}
			for (Place place : places.values()) {
				buffer.append(place.getName() + " [label=\"" + place.getName() + "\"];\n" + place.getName()
						+ " [shape=circle,width=0.40,height=0.40]\n");
				if (place.isMarked()) {
					buffer
							.append(place.getName()
									+ " [height=.3,width=.3,peripheries=2,style=filled,color=black,fontcolor=white];\n");
				}
				Transition[] postset = place.getPostset();
				for (Transition t : postset) {
					buffer.append(place.getName() + " -> " + t.getLabel() + "\n");
				}
			}
			for (Transition t : transitions.values()) {
				Place[] postset = t.getPostset();
				for (Place place : postset) {
					buffer.append(t.getLabel() + " -> " + place.getName() + "\n");
				}
			}
			buffer.append("}\n");
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

	public void addTransition(String name) {
		Transition trans = new Transition(name, tranIndex++, this);
		transitions.put(name, trans);
	}

//	public void addTransition(String name, Properties prop) {
//		Transition trans = new Transition(name, variables, this);
//		for (String p : prop.getProperty("preset").split("\\s")) {
//			trans.addPreset(places.get(p));
//		}
//		for (String p : prop.getProperty("postset").split("\\s")) {
//			trans.addPostset(places.get(p));
//		}
//		transitions.put(name, trans);
//	}
	
	public void addTransition(Transition t) {
		transitions.put(t.getLabel(), t);
	}

	public String insertPlace(Boolean ic) {
		String name = "";
		do {
			name = "ip" + implicitPlaceCount++;
		} while (getPlace(name)!=null);
		Place place = new Place(name, ic);
		places.put(name, place);
		return name;
	}
	
	public void addPlace(String name, Boolean ic) {
		Place place = new Place(name, ic);
		places.put(name, place);
	}

	public void addEnabling(String transition, String enabling) {
		transitions.get(transition).addEnabling(enabling);
	}

	public void addProperty(String prop) {
		properties.add(prop);
	}
	
	public void removeProperty(String prop) {
		properties.remove(prop);
	}

	public void addMovement(String fromName, String toName) {
		if (isTransition(fromName)) {
			transitions.get(fromName).addPostset(places.get(toName));
			places.get(toName).addPreset(transitions.get(fromName));
		} else {
			transitions.get(toName).addPreset(places.get(fromName));
			places.get(fromName).addPostset(transitions.get(toName));
		}
	}

	public void addInput(String name, String ic) {
		Variable var = new Variable(name, "boolean", ic, Variable.INPUT);
		booleans.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}
	
	public void addInput(String name, String type, String ic) {
		Variable var = new Variable(name, type, ic, Variable.INPUT);
		if (type.equals("boolean"))
			booleans.put(name, var);
		else if (type.equals("integer"))
			integers.put(name, var);
		else if (type.equals("continuous"))
			continuous.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}

	public void addOutput(String name, String ic) {
		Variable var = new Variable(name, "boolean", ic, Variable.OUTPUT);
		booleans.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}
	
	public void addOutput(String name, String type, String ic) {
		Variable var = new Variable(name, type, ic, Variable.OUTPUT);
		if (type.equals("boolean"))
			booleans.put(name, var);
		else if (type.equals("integer"))
			integers.put(name, var);
		else if (type.equals("continuous"))
			continuous.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}
	
	public void addInternal(String name, String type, String ic) {
		Variable var = new Variable(name, type, ic, Variable.INTERNAL);
		if (type.equals("boolean"))
			booleans.put(name, var);
		else if (type.equals("integer"))
			integers.put(name, var);
		else if (type.equals("continuous"))
			continuous.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}
	
	
	public void addBoolean(String name, String ic) {
		Variable var = new Variable(name, "boolean", ic);
		booleans.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}

	public void addContinuous(String name) {
		// Check if the name is already present. If not, add the variable.
		
		if(!continuous.containsKey(name)){
			Variable var = new Variable(name, "continuous");
			continuous.put(name, var);
		}
		
//		Variable var = new Variable(name, "continuous");
//		continuous.put(name, var);
//		if (!variables.contains(var)) {
//			variables.add(var);
//		}
	}

	public void addContinuous(String name, Properties initCond) {
		Variable contVar = null;
		
		// Search for the Variable.
		for(Variable var : continuous.values()){
			if(var.getName().equals(name)){
				contVar = var;
			}
		}
		
		// If contVar contains null, then a previous existing Variable was not found.
		if(contVar != null){
			contVar.addInitCond(initCond);
		}
		else{
			continuous.put(name, new Variable(name, "continuous", initCond));
		}
			
	}


	public void addContinuousInput(String name, Properties initCond) {
		Variable contVar = null;
		
		// Search for the Variable.
		for(Variable var : continuous.values()){
			if(var.getName().equals(name)){
				contVar = var;
			}
		}
		
		// If contVar contains null, then a previous existing Variable was not found.
		if(contVar != null){
			contVar.addInitCond(initCond);
		}
		else{
			continuous.put(name, new Variable(name, "continuous", initCond, Variable.INPUT));
		}
			
	}
	
	public void addContinuousOutput(String name, Properties initCond) {
		Variable contVar = null;
		
		// Search for the Variable.
		for(Variable var : continuous.values()){
			if(var.getName().equals(name)){
				contVar = var;
			}
		}
		
		// If contVar contains null, then a previous existing Variable was not found.
		if(contVar != null){
			contVar.addInitCond(initCond);
		}
		else{
			continuous.put(name, new Variable(name, "continuous", initCond, Variable.OUTPUT));
		}
			
	}
	
	public void addContinuous(String name, String initVal, String initRate) {
		Properties initCond = new Properties();
		initCond.setProperty("value", initVal);
		initCond.setProperty("rate", initRate);
		Variable var = new Variable(name, "continuous", initCond);
		continuous.put(name, var);
//		if (!variables.contains(var)) {
//			variables.add(var);
//		}
	}

	public void addInteger(String name, String ic) {
		Variable var = new Variable(name, "integer", ic);
		integers.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}
	
	public void addIntegerInput(String name, String ic) {
		Variable var = new Variable(name, "integer", ic, Variable.INPUT);
		integers.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}
	
	public void addIntegerOutput(String name, String ic) {
		Variable var = new Variable(name, "integer", ic, Variable.OUTPUT);
		integers.put(name, var);
		if (!variables.contains(var)) {
			variables.add(var);
		}
	}

	public void addTransitionRate(String transition, String rate) {
		transitions.get(transition).addDelay("exponential(" + rate + ")");
	}

	public void addBoolAssign(String transition, String variable,
			String assignment) {
		if (!variables.contains(variable)) {
			addOutput(variable, "unknown");
		}
		transitions.get(transition).addIntAssign(variable, assignment);
	}

	public void addRateAssign(String transition, String variable, String rate) {
		transitions.get(transition).addRateAssign(variable, rate);
	}

	public void addIntAssign(String transition, String variable, String assign) {
		transitions.get(transition).addIntAssign(variable, assign);
	}

	public void changePlaceName(String oldName, String newName) {
		places.get(oldName).setName(newName);
		places.put(newName, places.get(oldName));
		places.remove(oldName);
	}

	public void changeTransitionName(String oldName, String newName) {
		transitions.get(oldName).setName(newName);
		transitions.put(newName, transitions.get(oldName));
		transitions.remove(oldName);
	}

	public void changeDelay(String t, String delay) {
		transitions.get(t).addDelay(delay);
	}

	public void changePriority(String t, String priority) {
		transitions.get(t).addDelay(priority);
	}

	public void changeInitialMarking(String p, boolean marking) {
		places.get(p).setMarking(marking);
	}

	public void changeVariableName(String oldName, String newName) {
		if (isContinuous(oldName)) {
			continuous.put(newName, continuous.get(oldName));
			continuous.remove(oldName);
		} else if (isBoolean(oldName)) {
			booleans.put(newName, booleans.get(oldName));
			booleans.remove(oldName);
		} else if (isInteger(oldName)) {
			integers.put(newName, integers.get(oldName));
			integers.remove(oldName);
		}
	}

	public void changeContInitCond(String var, Properties initCond) {
		continuous.get(var).addInitCond(initCond);
	}

	public void changeIntegerInitCond(String var, String initCond) {
		integers.get(var).addInitValue(initCond);
	}

	public String[] getAllIDs() {
		String[] ids = new String[transitions.size() + places.size()
				+ variables.size()];
		int i = 0;
		for (String t : transitions.keySet()) {
			ids[i++] = t;
		}
		for (String p : places.keySet()) {
			ids[i++] = p;
		}
		for (Variable v : variables) {
			ids[i++] = v.getName();
		}
		return ids;
	}

	public ArrayList<String> getProperties() {
		return properties;
	}

	public String[] getTransitionList() {
		String[] transitionList = new String[transitions.size()];
		int i = 0;
		for (String t : transitions.keySet()) {
			transitionList[i++] = t;
		}
		return transitionList;
	}
	
	/**
	 * Returns all transitions of this LPN. 
	 * @return
	 */
	public Transition[] getAllTransitions() {
		if (allTransitions == null) {
			allTransitions = new Transition[transitions.size()];
			for (String t: transitions.keySet()) {
				allTransitions[transitions.get(t).getIndex()] = transitions.get(t);
			}
			return allTransitions;
		}
		return allTransitions;
	}
	
	public Transition getTransition(int index) {
		return getAllTransitions()[index];
	}
	
	public ArrayList<String> getTransitionListArrayList() {
		ArrayList<String> transitionList = new ArrayList<String>(transitions.size());
		int i = 0;
		for (String t: transitions.keySet()) {
			transitionList.add(i++, t);
		}
		return transitionList;
	}

	public Transition getTransition(String transition) {
		return transitions.get(transition);
	}
	
	public boolean isRandomBoolAssignTree(String transition, String variable) {
		if (transitions.get(transition).getBoolAssignTree(variable) == null)
			return false;
		if (transitions.get(transition).getBoolAssignTree(variable).op.equals("exponential")
				|| transitions.get(transition).getBoolAssignTree(variable).op.equals("uniform")) {
			return true;
		}
		return false;
	}
	
	public boolean isRandomContAssignTree(String transition, String variable) {
		if (transitions.get(transition).getContAssignTree(variable) == null)
			return false;
		if (transitions.get(transition).getContAssignTree(variable).op.equals("exponential")
				|| transitions.get(transition).getContAssignTree(variable).op.equals("uniform")) {
			return true;
		}
		return false;
	}
	
	public boolean isRandomIntAssignTree(String transition, String variable) {
		if (transitions.get(transition).getIntAssignTree(variable) == null)
			return false;
		if (transitions.get(transition).getIntAssignTree(variable).op.equals("exponential")
				|| transitions.get(transition).getIntAssignTree(variable).op.equals("uniform")) {
			return true;
		}
		return false;
	}
	
	public boolean isExpTransitionRateTree(String transition) {
		if (transitions.get(transition).getDelayTree() == null)
			return false;
		if (transitions.get(transition).getDelayTree().op.equals("exponential")) {
			return true;
		}
		return false;
	}

	public ExprTree getTransitionRateTree(String transition) {
		if (transitions.get(transition).getDelayTree() == null)
			return null;
		if (transitions.get(transition).getDelayTree().op.equals("exponential")) {
			return transitions.get(transition).getDelayTree().r1;
		}
		return null;
	}
	
	public ExprTree getDelayTree(String transition) {
		if (transitions.get(transition).getDelayTree() == null) {
			return null;
		}
		return transitions.get(transition).getDelayTree();
	}

	public ExprTree getEnablingTree(String transition) {
		return transitions.get(transition).getEnablingTree();
	}

	public String getLabel() {
		return label;
	}

	public int getLpnIndex() {
		return lpnIndex;
	}

	public String[] getPlaceList() {
		placeList = new String[places.size()];
		int i = 0;
		for (String t : places.keySet()) {
			placeList[i++] = t;
		}
		return placeList;
	}
	
//	public ArrayList<String> getAllPlaces() {
//		if(placeList == null) {
//			int i = 0;
//			for (String t: places.keySet()) {
//				placeList.add(i++, t);
//			}
//			return placeList;
//		}
//		else
//			return placeList;
//
//	}
	
	public Place getPlace(String place) {
		return places.get(place);
	}

	public String[] getPreset(String name) {
		if (isTransition(name)) {
			String[] preset = new String[transitions.get(name).getPreset().length];
			int i = 0;
			for (Place p : transitions.get(name).getPreset()) {
				preset[i++] = p.getName();
			}
			return preset;
		} else if (places.containsKey(name)) {
			String[] preset = new String[places.get(name).getPreset().length];
			int i = 0;
			for (Transition t : places.get(name).getPreset()) {
				preset[i++] = t.getLabel();
			}
			return preset;
		} else {
			return null;
		}
	}

	public int[] getPresetIndex(String name) {
		if (isTransition(name)) {
			int[] preset = new int[transitions.get(name).getPreset().length];
			Place[] presetPlaces = transitions.get(name).getPreset();
			for (int i=0; i<presetPlaces.length; i++) {
				for (int placeIndex=0; placeIndex<this.getPlaceList().length; placeIndex++) {
					if (this.getPlaceList()[placeIndex] == presetPlaces[i].getName()) {
						preset[i] = placeIndex;
					}
				}
			}
			return preset;
		}
		else if (places.containsKey(name)) {
			int[] preset = new int[places.get(name).getPreset().length];
			int i = 0;
			for (Transition t : places.get(name).getPreset()) {
				preset[i++] = t.getIndex();
			}
			return preset;
		} else {
			return null;
		}
	}
	
	public String[] getPostset(String name) {
		if (isTransition(name)) {
			String[] postset = new String[transitions.get(name).getPostset().length];
			int i = 0;
			for (Place p : transitions.get(name).getPostset()) {
				postset[i++] = p.getName();
			}
			return postset;
		} else if (places.containsKey(name)) {
			String[] postset = new String[places.get(name).getPostset().length];
			int i = 0;
			for (Transition t : places.get(name).getPostset()) {
				postset[i++] = t.getLabel();
			}
			return postset;
		} else {
			return null;
		}
	}
	
	public int[] getPostsetIndex(String name) {
		if (isTransition(name)) {
			int[] postset = new int[transitions.get(name).getPostset().length];
			Place[] postPlaces = transitions.get(name).getPostset();
			for (int i=0; i<postPlaces.length; i++) {
				for (int placeIndex=0; placeIndex<this.getPlaceList().length; placeIndex++) {
					if (this.getPlaceList()[placeIndex] == postPlaces[i].getName()) {
						postset[i] = placeIndex;
					}
				}
			}
			return postset;
		}
		else if (places.containsKey(name)) {
			int[] postset = new int[places.get(name).getPostset().length];
			int i = 0;
			for (Transition t : places.get(name).getPostset()) {
				postset[i++] = t.getIndex();
			}
			return postset;
		} else {
			return null;
		}
	}
	
	public String[] getControlFlow() {
		ArrayList<String> movements = new ArrayList<String>();
		for (Transition t : transitions.values()) {
			for (Place p : t.getPostset()) {
				movements.add(t.getLabel() + " " + p.getName());
			}
			for (Place p : t.getPreset()) {
				movements.add(p.getName() + " " + t.getLabel());
			}
		}
		String[] array = new String[movements.size()];
		int i = 0;
		for (String s : movements) {
			array[i++] = s;
		}
		return array;
	}
	
	public boolean getInitialMarking(String place) {
		return places.get(place).isMarked();
	}

	public int[] getInitialMarkingsArray() {
		int[] initialMarkings = new int[this.getPlaceList().length];
		int i = 0;
		for (String place : this.getPlaceList()) {
			if(places.get(place).isMarked()) {
				initialMarkings[i] = 1;
			}
			else {
				initialMarkings[i] = 0;
			}
			i++;
		}
		return initialMarkings;
	}
	
	public String[] getVariables() {
		String[] vars = new String[variables.size()+continuous.keySet().size()];
		int i = 0;
		for (Variable v : variables) {
			vars[i++] = v.getName();
		}
		
		for(String contName : continuous.keySet()){
			vars[i++] = contName;
		}
		
		return vars;
	}
	
	public DualHashMap<String, Integer> getVarIndexMap() {
		
		if(_varIndexMap == null){

			int i = 0;
			HashMap<String, Integer> varIndexHashMap = new HashMap<String, Integer>();
			for (Variable v: variables) {
				varIndexHashMap.put(v.getName(), i);
				i++;
			}
			DualHashMap<String, Integer> varIndexMap = new DualHashMap<String, Integer>(varIndexHashMap, variables.size());
			
			_varIndexMap = varIndexMap;
			
			return varIndexMap;
		}
		return _varIndexMap;
	}
	
	/**
	 * Gives a map that associates the name of a continuous variable to its index.
	 * @return
	 * 		The map that associates the continuous variable and name.
	 */
	public DualHashMap<String, Integer> getContinuousIndexMap(){
		
		if(_continuousIndexMap == null){
			int i=allTransitions.length;
			HashMap<String, Integer> contVarIndexHashMap = new HashMap<String, Integer>();
			for(Variable v : continuous.values()){
				contVarIndexHashMap.put(v.getName(), i);
				i++;
			}
			DualHashMap<String, Integer> contIndexMap = 
					new DualHashMap<String, Integer> (contVarIndexHashMap, variables.size());
			
			_continuousIndexMap = contIndexMap;
			return contIndexMap;
		}
		return _continuousIndexMap;
	}
	
	public HashMap<String, String> getAllVarsWithValuesAsString(int[] varValueVector) {
		DualHashMap<String, Integer> varIndexMap = this.getVarIndexMap();
		HashMap<String, String> varToValueMap = new HashMap<String, String>();
		// varValue is a map between variable names and their values. 
		for (int i = 0; i < varValueVector.length; i++) {
			String var = varIndexMap.getKey(i);
			varToValueMap.put(var, varValueVector[i] + "");
		}	
		return varToValueMap;
	}
	
	public HashMap<String, Integer> getAllVarsWithValuesAsInt(int[] varValueVector) {
		DualHashMap<String, Integer> varIndexMap = this.getVarIndexMap();
		HashMap<String, Integer> varToValueMap = new HashMap<String, Integer>();
		// varValue is map between variable names and their values. 
		for (int i = 0; i < varValueVector.length; i++) {
			String var = varIndexMap.getKey(i);
			varToValueMap.put(var, varValueVector[i]);
		}	
		return varToValueMap;
	}
	
	public Variable getVariable(String name) {
		if (isBoolean(name)) {
			return booleans.get(name);
		} else if (isContinuous(name)) {
			return continuous.get(name);
		} else if (isInteger(name)) {
			return integers.get(name);
		}
		return null;
	}
	
	public Variable getVariable(int index){
		return variables.get(index);
	}
	
	public int getVariableIndex(String name){
		return getVarIndexMap().get(name);
	}
	
	public String getContVarName(int index){
		//int counter = 0;
		
		// The index of the continuous variable is determined by
		// the order it is returned by the 'continuous' fields
		// iterator.
//		for(String name : continuous.keySet()){
//			if(counter == index){
//				return name;
//			}
//			counter++;
//		}
		
		DualHashMap<String, Integer> variableMap = getContinuousIndexMap();
		
		//return null;
		return variableMap.getKey(index);
	}
	
	public Variable getContVar(int index){
		
		// Convert the index into a name.
		String name = getContVarName(index);
		
		return continuous.get(name);
	}
	
	public int getContVarIndex(String name){
		DualHashMap<String, Integer> contVarIndecies = getContinuousIndexMap();
		return contVarIndecies.getValue(name);
	}

	public HashMap<String, String> getBoolInputs() {
		HashMap<String, String> inputs = new HashMap<String, String>();
		for (Variable v : booleans.values()) {
			if (!v.isOutput()) {
				inputs.put(v.getName(), v.getInitValue());
			}
		}
		return inputs;
	}

	public HashMap<String, String> getBoolOutputs() {
		HashMap<String, String> outputs = new HashMap<String, String>();
		for (Variable v : booleans.values()) {
			if (v.isOutput()) {
				outputs.put(v.getName(), v.getInitValue());
			}
		}
		return outputs;
	}
	
	public HashMap<String, String> getAllInputs() {
		HashMap<String, String> inputs = new HashMap<String, String>();
		for (Variable v : booleans.values()) {
			if (v.isInput()) {
				inputs.put(v.getName(), v.getInitValue());
			}
		}
		for (Variable v : integers.values()) {
			if (v.isInput()) {
				inputs.put(v.getName(), v.getInitValue());
			}
		}
		for (Variable v : continuous.values()) {
			if (v.isInput()) {
				inputs.put(v.getName(), v.getInitValue());
			}
		}
		
		return inputs;
	}
	
	public HashMap<String, String> getAllInternals() {
		HashMap<String, String> internals = new HashMap<String, String>();
		for (Variable v : booleans.values()) {
			if (v.isInternal()) {
				internals.put(v.getName(), v.getInitValue());
			}
		}
		for (Variable v : integers.values()) {
			if (v.isInternal()) {
				internals.put(v.getName(), v.getInitValue());
			}
		}
		for (Variable v : continuous.values()) {
			if (v.isInternal()) {
				internals.put(v.getName(), v.getInitValue());
			}
		}	
		return internals;
	}

	public HashMap<String, String> getAllOutputs() {
		HashMap<String, String> outputs = new HashMap<String, String>();
		for (Variable v : booleans.values()) {
			if (v.isOutput()) {
				outputs.put(v.getName(), v.getInitValue());
			}
		}
		for (Variable v : integers.values()) {
			if (v.isOutput()) {
				outputs.put(v.getName(), v.getInitValue());
			}
		}
		for (Variable v : continuous.values()) {
			if (v.isOutput()) {
				outputs.put(v.getName(), v.getInitValue());
			}
		}		
		return outputs;
	}
	
	public HashMap<String, String> getBooleans() {
		HashMap<String, String> bools = new HashMap<String, String>();
		for (Variable v : booleans.values()) {
				bools.put(v.getName(), v.getInitValue());
		}
		return bools;
	}

	public HashMap<String, Properties> getContinuous() {
		HashMap<String, Properties> tempCont = new HashMap<String, Properties>();
		for (Variable v : continuous.values()) {
			Properties prop = new Properties();
			prop.setProperty("value", v.getInitValue());
			prop.setProperty("rate", v.getInitRate());
			tempCont.put(v.getName(), prop);
		}
		return tempCont;
	}

	public HashMap<String, String> getIntegers() {
		HashMap<String, String> tempInt = new HashMap<String, String>();
		for (Variable v : integers.values()) {
			tempInt.put(v.getName(), v.getInitValue());
		}
		return tempInt;
	}

	public String[] getBooleanVars() {
		String[] vars = new String[booleans.size()];
		int i = 0;
		for (String v : booleans.keySet()) {
			vars[i++] = v;
		}
		return vars;
	}

	public String[] getBooleanVars(String transition) {
		Set<String> set = transitions.get(transition).getBoolAssignments()
				.keySet();
		String[] array = new String[set.size()];
		int i = 0;
		for (String s : set) {
			array[i++] = s;
		}
		return array;
	}

	public String[] getContVars() {
		String[] vars = new String[continuous.size()];
		int i = 0;
		for (String v : continuous.keySet()) {
			vars[i++] = v;
		}
		return vars;
	}

	public String[] getContVars(String transition) {
		Set<String> set = transitions.get(transition).getContAssignments()
				.keySet();
		String[] array = new String[set.size()];
		int i = 0;
		for (String s : set) {
			array[i++] = s;
		}
		return array;
	}

	public String[] getRateVars(String transition) {
		Set<String> set = transitions.get(transition).getRateAssignments()
				.keySet();
		String[] array = new String[set.size()];
		int i = 0;
		for (String s : set) {
			array[i++] = s;
		}
		return array;
	}

	public String[] getIntVars() {
		String[] vars = new String[integers.size()];
		int i = 0;
		for (String v : integers.keySet()) {
			vars[i++] = v;
		}
		return vars;
	}

	public String[] getIntVars(String transition) {
		Set<String> set = transitions.get(transition).getIntAssignments()
				.keySet();
		String[] array = new String[set.size()];
		int i = 0;
		for (String s : set) {
			array[i++] = s;
		}
		return array;
	}

	public String getInitialVal(String var) {
		if (isBoolean(var)) {
			return booleans.get(var).getInitValue();
		} else if (isInteger(var)) {
			return integers.get(var).getInitValue();
		} else {
			return continuous.get(var).getInitValue();
		}
	}

	public String getInitialRate(String var) {
		if (isContinuous(var)) {
			return continuous.get(var).getInitRate();
		}
		return null;
	}
	
	/** 
	 * This method converts all variable values (boolean, continuous and integer) to int.
	 * @param var
	 * @return
	 */
	public int getInitVariableVector(String var) {
		if (isBoolean(var)) {
			if(booleans.get(var).getInitValue().equals("true"))
				return 1;
			return 0;
		}
		else if (isInteger(var)) {
			String initValue = integers.get(var).getInitValue();
			if (initValue.contains(",")) {
				initValue = initValue.split(",")[0].replace("[", "");
			}
			return Integer.parseInt(initValue);
		}
		else {
			// Continuous variable is not accepted here.
			// return (int) Double.parseDouble(continuous.get(var).getInitValue());
			System.out.println(var + " is neither boolean or integer variable. ");
    		new NullPointerException().printStackTrace();
    		System.exit(1);
    		return 0;
		}
	}

	public String getBoolAssign(String transition, String variable) {
		return transitions.get(transition).getBoolAssignment(variable);
	}

	public ExprTree getBoolAssignTree(String transition, String variable) {
		return transitions.get(transition).getBoolAssignTree(variable);
	}

	public String getContAssign(String transition, String variable) {
		return transitions.get(transition).getContAssignment(variable);
	}

	public ExprTree getContAssignTree(String transition, String variable) {
		return transitions.get(transition).getContAssignTree(variable);
	}

	public String getRateAssign(String transition, String variable) {
		return transitions.get(transition).getRateAssignment(variable);
	}

	public ExprTree getRateAssignTree(String transition, String variable) {
		return transitions.get(transition).getRateAssignTree(variable);
	}

	public String getIntAssign(String transition, String variable) {
		return transitions.get(transition).getIntAssignment(variable);
	}

	public ExprTree getIntAssignTree(String transition, String variable) {
		return transitions.get(transition).getIntAssignTree(variable);
	}

	public void removeTransition(String name) {
		if (!transitions.containsKey(name)) {
			return;
		}
		for (Place p : transitions.get(name).getPreset()) {
			removeMovement(p.getName(), name);
		}
		for (Place p : transitions.get(name).getPostset()) {
			removeMovement(name, p.getName());
		}
		transitions.remove(name);
	}

	public void removePlace(String name) {
		if (name != null && places.containsKey(name)) {
			for (Transition t : places.get(name).getPreset()) {
				removeMovement(t.getLabel(), name);
			}
			for (Transition t : places.get(name).getPostset()) {
				removeMovement(name, t.getLabel());
			}
			places.remove(name);
		}
	}

	public void renamePlace(String oldName, String newName) {
		if (oldName != null && places.containsKey(oldName)) {
			places.put(newName, places.get(oldName));
			places.get(newName).changeName(newName);
			places.remove(oldName);
		}
	}

	public void renameTransition(String oldName, String newName) {
		if (oldName != null && transitions.containsKey(oldName)) {
			transitions.put(newName, transitions.get(oldName));
			transitions.get(newName).changeName(newName);
			transitions.remove(oldName);
		}
	}

	public void removeMovement(String from, String to) {
		if (isTransition(from)) {
			transitions.get(from).removePostset(places.get(to));
			places.get(to).removePreset(transitions.get(from));
		} else {
			transitions.get(to).removePreset(places.get(from));
			places.get(from).removePostset(transitions.get(to));
		}
	}

	public void removeInput(String name) {
		if (name != null && booleans.containsKey(name)) {
			booleans.remove(name);
			variables.remove(booleans.get(name));
		}
	}

	public void removeBoolean(String name) {
		if (name != null && booleans.containsKey(name)) {
			booleans.remove(name);
			variables.remove(booleans.get(name));
		}
	}

	public void removeOutput(String name) {
		if (name != null && booleans.containsKey(name)) {
			booleans.remove(name);
			variables.remove(booleans.get(name));
		}
	}

	public void removeContinuous(String name) {
		if (name != null && continuous.containsKey(name)) {
			continuous.remove(name);
			variables.remove(continuous.get(name));
		}
	}

	public void removeInteger(String name) {
		if (name != null && integers.containsKey(name)) {
			integers.remove(name);
			variables.remove(integers.get(name));
		}
	}

	public boolean removeVar(String name) {
		for (Transition t : transitions.values()) {
			if (t.containsAssignment(name)) {
				return false;
			}
		}
		if (name != null && continuous.containsKey(name)) {
			removeContinuous(name);
		} else if (name != null && booleans.containsKey(name)) {
			removeBoolean(name);
		} else if (name != null && integers.containsKey(name)) {
			removeInteger(name);
		} else {
			for (Variable v : variables) {
				if (v.getName().equals(name)) {
					variables.remove(v);
					break;
				}
			}
		}
		return true;
	}

	public void removeAllAssignVar(String name) {
		for (Transition t : transitions.values()) {
			if (t.containsAssignment(name)) {
				t.removeAssignment(name);
			}
		}
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLpnIndex(int index) {
		this.lpnIndex = index;
	}

	public boolean isTransition(String name) {
		return transitions.containsKey(name);
	}

	public boolean isInput(String var) {
		if (isContinuous(var)) {
			return continuous.get(var).isInput();
		}
		else if (isInteger(var)) {
			return integers.get(var).isInput();
		}
		else if (isBoolean(var)) {
			return booleans.get(var).isInput();
		}
		return false;
	}

	public boolean isOutput(String var) {
		if (isContinuous(var)) {
			return continuous.get(var).isOutput();
		}
		else if (isInteger(var)) {
			return integers.get(var).isOutput();
		}
		else if (isBoolean(var)) {
			return booleans.get(var).isOutput();
		}
		return false;
	}

	public boolean isBoolean(String var) {
		return booleans.containsKey(var);
	}

	public boolean isContinuous(String var) {
		return continuous.containsKey(var);
	}

	public boolean isInteger(String var) {
		return integers.containsKey(var);
	}

	public boolean isMarked(String place) {
		return places.get(place).isMarked();
	}

	public boolean containsTransition(String name) {
		return transitions.containsKey(name);
	}

	public boolean containsMovement(String name) {
		if (places.containsKey(name)) {
			return places.get(name).isConnected();
		}
		return transitions.get(name).isConnected();
	}

	public boolean containsMovement(String from, String to) {
		if (isTransition(from)) {
			return transitions.get(from).containsPostset(to);
		}
		return places.get(from).containsPostset(to);
	}

	public Abstraction abstractLhpn(Verification pane) {
		Abstraction abstraction = new Abstraction(this, pane);
		return abstraction;
	}

	private void parseProperty(StringBuffer data) {
		Pattern pattern = Pattern.compile(PROPERTY);
		Matcher lineMatcher = pattern.matcher(data.toString());
		while (lineMatcher.find()) {
			properties.add(lineMatcher.group(1));
		}
	}
	
	private void parseInOut(StringBuffer data) {
		Properties varOrder = new Properties();
		Pattern inLinePattern = Pattern.compile(INPUT);
		Matcher inLineMatcher = inLinePattern.matcher(data.toString());
		Integer i = 0;
		Integer inLength = 0;
		Integer outLength = 0;
		if (inLineMatcher.find()) {
			Pattern inPattern = Pattern.compile(WORD);
			Matcher inMatcher = inPattern.matcher(inLineMatcher.group(1));
			while (inMatcher.find()) {
				String var = inMatcher.group();
				if (isContinuous(var)) {
					continuous.get(var).setPort("input");
				}
				else if (isInteger(var)) {
					integers.get(var).setPort("input");
				}
				else {
					varOrder.setProperty(i.toString(), var);
					i++;
					inLength++;
				}
			}
		}
		Pattern outPattern = Pattern.compile(OUTPUT);
		Matcher outLineMatcher = outPattern.matcher(data.toString());
		if (outLineMatcher.find()) {
			Pattern output = Pattern.compile(WORD);
			Matcher outMatcher = output.matcher(outLineMatcher.group(1));
			while (outMatcher.find()) {
				String var = outMatcher.group();
				if (isContinuous(var)) {
					continuous.get(var).setPort("output");
				}
				else if (isInteger(var)) {
					integers.get(var).setPort("output");
				}
				else {
					varOrder.setProperty(i.toString(), var);
					i++;
					outLength++;
				}
			}
		}
		Pattern internalPattern = Pattern.compile(INTERNAL);
		Matcher internalLineMatcher = internalPattern.matcher(data.toString());
		if (internalLineMatcher.find()) {
			Pattern internal = Pattern.compile(WORD);
			Matcher internalMatcher = internal.matcher(internalLineMatcher.group(1));
			while (internalMatcher.find()) {
				String var = internalMatcher.group();
				if (isContinuous(var)) {
					continuous.get(var).setPort("internal");
				}
				else if (isInteger(var)) {
					integers.get(var).setPort("internal");
				}
				else {
					varOrder.setProperty(i.toString(), var);
					i++;
				}
			}
		}
		Pattern initState = Pattern.compile(INIT_STATE);
		Matcher initMatcher = initState.matcher(data.toString());
		if (initMatcher.find()) {
			Pattern initDigit = Pattern.compile("[01X]+");
			Matcher digitMatcher = initDigit.matcher(initMatcher.group());
			digitMatcher.find();
			String[] initArray = new String[digitMatcher.group().length()];
			Pattern bit = Pattern.compile("[01X]");
			Matcher bitMatcher = bit.matcher(digitMatcher.group());
			i = 0;
			while (bitMatcher.find()) {
				initArray[i] = bitMatcher.group();
				i++;
			}
			for (i = 0; i < inLength; i++) {
				String name = varOrder.getProperty(i.toString());
				if (initArray[i].equals("1")) {
					addInput(name, "true");
				} else if (initArray[i].equals("0")) {
					addInput(name, "false");
				} else {
					addInput(name, "unknown");
				}
			}
			for (i = inLength; i < inLength + outLength; i++) {
				String name = varOrder.getProperty(i.toString());
				if (initArray[i].equals("1") && name != null) {
					addOutput(name, "true");
				} else if (initArray[i].equals("0") && name != null) {
					addOutput(name, "false");
				} else {
					addOutput(name, "unknown");
				}
			}
			for (i = inLength + outLength; i < initArray.length; i++) {
				String name = varOrder.getProperty(i.toString());
				if (initArray[i].equals("1") && name != null) {
					addBoolean(name, "true");
					booleans.get(name).setPort("internal");
				} else if (initArray[i].equals("0") && name != null) {
					addBoolean(name, "false");
					booleans.get(name).setPort("internal");
				} else {
					addBoolean(name, "unknown");
					booleans.get(name).setPort("internal");
				}
			}
		} else {
			if (varOrder.size() != 0) {
				System.out.println("WARNING: Boolean variables have not been initialized.");
				for (i = 0; i < varOrder.size(); i++) {
					if (i < inLength) {
						addInput(varOrder.getProperty(i.toString()), "unknown");
					} else {
						addOutput(varOrder.getProperty(i.toString()), "unknown");
					}
				}
			}
		}
		for (Variable var : continuous.values()) {
			if (var.getPort() == null) {
				var.setPort("internal");
			}
		}
		for (Variable var : integers.values()) {
			if (var.getPort() == null) {
				var.setPort("internal");
			}
		}
		for (Variable var : booleans.values()) {
			if (var.getPort() == null) {
				var.setPort("internal");
			}
		}
	}

	private void parseControlFlow(StringBuffer data) {
		allTransitions = null;
		Pattern pattern = Pattern.compile(TRANSITION);
		Matcher lineMatcher = pattern.matcher(data.toString());
		if (lineMatcher.find()) {
			lineMatcher.group(1);
			String name = lineMatcher.group(1).replaceAll("\\+/", "P");
			name = name.replaceAll("-/", "M");
			Pattern transPattern = Pattern.compile(WORD);
			Matcher transMatcher = transPattern.matcher(name);
			while (transMatcher.find()) {
				addTransition(transMatcher.group());
			}
		}
		Pattern placePattern = Pattern.compile(PLACE);
		Matcher placeMatcher = placePattern.matcher(data.toString());
		while (placeMatcher.find()) {
			String temp = placeMatcher.group(1);
			String[] tempPlace = temp.split("\\s");
			for (int i = 0; i < tempPlace.length; i++) {
				if (tempPlace[i].contains("+")||tempPlace[i].contains("-")) {
					boolean assignment = tempPlace[i].contains("+"); 
					String var = tempPlace[i].replaceAll("\\+(/\\d+)?", "").replaceAll("-(/\\d+)?", "");
					tempPlace[i] = tempPlace[i].replaceAll("\\+", "P").replaceAll("-", "M");
					tempPlace[i] = tempPlace[i].replace("/","");
					if (getTransition(tempPlace[i])==null) {
						addTransition(tempPlace[i]);
						Transition trans = getTransition(tempPlace[i]);
						trans.addBoolAssign(var, assignment?"TRUE":"FALSE");
					}
				}
			}
			if (isTransition(tempPlace[0]) && isTransition(tempPlace[1])) {
				for (int i = 1; i < tempPlace.length; i++) {
					String implicitPlace = implicitPlaceMap.get(tempPlace[0]+","+tempPlace[i]);
					if (implicitPlace==null) {
						implicitPlace = insertPlace(false);
						implicitPlaceMap.put(tempPlace[0]+","+tempPlace[i], implicitPlace);
					}
					addMovement(tempPlace[0],implicitPlace);
					addMovement(implicitPlace,tempPlace[i]);
				}
			} else {
				if (isTransition(tempPlace[0])) {
					if (!places.containsKey(tempPlace[1])) {
						addPlace(tempPlace[1], false);
					}
				} else {
					if (!places.containsKey(tempPlace[0])) {
						addPlace(tempPlace[0], false);
					}
				}
				addMovement(tempPlace[0], tempPlace[1]);
			}
		}
	}

	private void parseVars(StringBuffer data) {
		Properties initCond = new Properties();
		Properties initValue = new Properties();
		Properties initRate = new Properties();
		Pattern linePattern = Pattern.compile(CONTINUOUS);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern varPattern = Pattern.compile(WORD);
			Matcher varMatcher = varPattern.matcher(lineMatcher.group(1));
			while (varMatcher.find()) {
				addContinuous(varMatcher.group());
			}
			Pattern initLinePattern = Pattern.compile(VARS_INIT);
			Matcher initLineMatcher = initLinePattern.matcher(data.toString());
			if (initLineMatcher.find()) {
				Pattern initPattern = Pattern.compile(INIT_COND);
				Matcher initMatcher = initPattern.matcher(initLineMatcher
						.group(1));
				while (initMatcher.find()) {
					if (continuous.containsKey(initMatcher.group(1))) {
						initValue.put(initMatcher.group(1), initMatcher
								.group(2));
					}
				}
			}
			Pattern rateLinePattern = Pattern.compile(INIT_RATE);
			Matcher rateLineMatcher = rateLinePattern.matcher(data.toString());
			if (rateLineMatcher.find()) {
				Pattern ratePattern = Pattern.compile(INIT_COND);
				Matcher rateMatcher = ratePattern.matcher(rateLineMatcher
						.group(1));
				while (rateMatcher.find()) {
					initRate.put(rateMatcher.group(1), rateMatcher.group(2));
				}
			}
			for (String s : continuous.keySet()) {
				if (initValue.containsKey(s)) {
					initCond.put("value", initValue.get(s));
				} else {
					if (continuous.get(s).getInitValue() != null) // Added this condition for mergeLPN methods sake. SB
						initCond.put("value", continuous.get(s).getInitValue());
					else
						initCond.put("value", "[-inf,inf]");
				}
				if (initRate.containsKey(s)) {
					initCond.put("rate", initRate.get(s));
				} else {
					if (continuous.get(s).getInitRate() != null) // Added this condition for mergeLPN methods sake. SB
						initCond.put("rate", continuous.get(s).getInitRate());
					else
						initCond.put("rate", "[-inf,inf]");
				}
				addContinuous(s, initCond);
			}
		}
	}

	private void parseIntegers(StringBuffer data) {
		String initCond = "0";
		Properties initValue = new Properties();
		Pattern linePattern = Pattern.compile(VARIABLES);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern varPattern = Pattern.compile(WORD);
			Matcher varMatcher = varPattern.matcher(lineMatcher.group(1));
			while (varMatcher.find()) {
				if (!continuous.containsKey(varMatcher.group())) {
					addInteger(varMatcher.group(), initCond);
				}
			}
			Pattern initLinePattern = Pattern.compile(VARS_INIT);
			Matcher initLineMatcher = initLinePattern.matcher(data.toString());
			if (initLineMatcher.find()) {
				Pattern initPattern = Pattern.compile(INIT_COND);
				Matcher initMatcher = initPattern.matcher(initLineMatcher
						.group(1));
				while (initMatcher.find()) {
					if (integers.containsKey(initMatcher.group(1))) {
						initValue.put(initMatcher.group(1), initMatcher
								.group(2));
					}
				}
			}
			for (String s : integers.keySet()) {
				if (initValue.get(s) != null) {
					initCond = initValue.get(s).toString();
				} else {
					if (integers.get(s).getInitValue() != null) // Added this
																// condition for
																// mergeLPN
																// methods sake.
																// SB
						initCond = integers.get(s).getInitValue();
					else
						initCond = "[-inf,inf]";
				}
				addInteger(s, initCond);
			}
		}
	}

	private void parsePlaces(StringBuffer data) {
		placeList = null;
		Pattern linePattern = Pattern.compile(PLACES_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern markPattern = Pattern.compile(MARKING);
			Matcher markMatcher = markPattern.matcher(lineMatcher.group(1));
			while (markMatcher.find()) {
				String name = markMatcher.group();
				Place place = new Place(name);
				places.put(name, place);
			}
		}
	}

	private void parseMarking(StringBuffer data) {
		Pattern linePattern = Pattern.compile(MARKING_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern markPattern = Pattern.compile(MARKING);
			Matcher markMatcher = markPattern.matcher(lineMatcher.group(1));
			while (markMatcher.find()) {
				String marking = markMatcher.group();
				if (marking.startsWith("<")) {
					marking = marking.replace("<", "").replace(">","");
					marking = marking.replace("+", "P").replace("-", "M");
					marking = marking.replaceAll("/", "");
					marking = implicitPlaceMap.get(marking);
				}
				if (places.get(marking)==null) {
					System.out.println("Marking cannot be found: "+marking);
				} else {
					places.get(marking).setMarking(true);
				}
			}
		}
	}

	private boolean parseEnabling(StringBuffer data) {
		boolean error = true;
		Pattern linePattern = Pattern.compile(ENABLING_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern enabPattern = Pattern.compile(ENABLING);
			Matcher enabMatcher = enabPattern.matcher(lineMatcher.group(1));
			while (enabMatcher.find()) {
				if (transitions.get(enabMatcher.group(1)).addEnabling(
						enabMatcher.group(2)) == false) {
					error = false;
				}
			}
		}
		return error;
	}

	private boolean parseAssign(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(ASSIGNMENT_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		Pattern rangePattern = Pattern.compile(RANGE);
		if (lineMatcher.find()) {
			Pattern assignPattern = Pattern.compile(ASSIGNMENT);
			Matcher assignMatcher = assignPattern.matcher(lineMatcher.group(1)
					.replace("\\s", ""));
			Pattern varPattern = Pattern.compile(ASSIGN_VAR);
			Matcher varMatcher;
			while (assignMatcher.find()) {
				Transition transition = transitions.get(assignMatcher.group(1));
				varMatcher = varPattern.matcher(assignMatcher.group(2));
				if (varMatcher.find()) {
					String variable = varMatcher.group(1);
					String assignment = varMatcher.group(2);
					if (isInteger(variable)) {
						Matcher rangeMatcher = rangePattern.matcher(assignment);
						if (rangeMatcher.find()) {
							if (rangeMatcher.group(1).matches(INTEGER)
									&& rangeMatcher.group(2).matches(INTEGER)) {
								if (Integer.parseInt(rangeMatcher.group(1)) == Integer
										.parseInt(rangeMatcher.group(2))) {
									transition.addIntAssign(variable,
											rangeMatcher.group(1));
								}
							}
							if (transition.addIntAssign(variable, "uniform("
									+ rangeMatcher.group(1) + ","
									+ rangeMatcher.group(2) + ")") == false) {
								error = false;
							}
						} else {
							if (transition.addIntAssign(variable, assignment) == false) {
								error = false;
							}
						}
					} else {
						Matcher rangeMatcher = rangePattern.matcher(assignment);
						if (rangeMatcher.find()) {
							if (rangeMatcher.group(1).matches(INTEGER)
									&& rangeMatcher.group(2).matches(INTEGER)) {
								if (Integer.parseInt(rangeMatcher.group(1)) == Integer
										.parseInt(rangeMatcher.group(2))) {
									if (transition.addContAssign(variable,
											rangeMatcher.group(1)) == false) {
										error = false;
									}
								}
							}
							if (transition.addContAssign(variable, "uniform("
									+ rangeMatcher.group(1) + ","
									+ rangeMatcher.group(2) + ")") == false) {
								error = false;
							}
						} else if (transition.addContAssign(variable,
								assignment) == false) {
							error = false;
						}
					}
				}
			}
		}
		return error;
	}

	private boolean parseRateAssign(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(RATE_ASSIGNMENT_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		Pattern rangePattern = Pattern.compile(RANGE);
		if (lineMatcher.find()) {
			Pattern assignPattern = Pattern.compile(ASSIGNMENT);
			Matcher assignMatcher = assignPattern.matcher(lineMatcher.group(1)
					.replace("\\s", ""));
			Pattern varPattern = Pattern.compile(ASSIGN_VAR);
			Matcher varMatcher;
			while (assignMatcher.find()) {
				Transition transition = transitions.get(assignMatcher.group(1));
				varMatcher = varPattern.matcher(assignMatcher.group(2));
				while (varMatcher.find()) {
					String variable = varMatcher.group(1);
					String assignment = varMatcher.group(2);
					Matcher rangeMatcher = rangePattern.matcher(assignment);
					if (rangeMatcher.find()) {
						if (rangeMatcher.group(1).matches(INTEGER)
								&& rangeMatcher.group(2).matches(INTEGER)) {
							if (Integer.parseInt(rangeMatcher.group(1)) == Integer
									.parseInt(rangeMatcher.group(2))) {
								if (transition.addRateAssign(variable,
										rangeMatcher.group(1)) == false) {
									error = false;
								}
							}
						}
						if (transition.addRateAssign(variable, "uniform("
								+ rangeMatcher.group(1) + ","
								+ rangeMatcher.group(2) + ")") == false) {
							error = false;
						}
					} else if (transition.addRateAssign(variable, assignment) == false) {
						error = false;
					}
				}
			}
		}
		return error;
	}

	private boolean parseDelayAssign(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(DELAY_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern delayPattern = Pattern.compile(DELAY);
			Matcher delayMatcher = delayPattern.matcher(lineMatcher.group(1)
					.replace("\\s", ""));
			while (delayMatcher.find()) {
				Transition transition = transitions.get(delayMatcher.group(1));
				Pattern rangePattern = Pattern.compile(RANGE);
				Matcher rangeMatcher = rangePattern.matcher(delayMatcher
						.group(2));
				String delay;
				if (rangeMatcher.find()) {
					if (rangeMatcher.group(1).equals(rangeMatcher.group(2))) {
						delay = rangeMatcher.group(1);
					} else {
						delay = "uniform(" + rangeMatcher.group(1) + ","
								+ rangeMatcher.group(2) + ")";
					}
				} else {
					delay = delayMatcher.group(2);
				}
				if (transition.addDelay(delay) == false) {
					error = false;
				}
			}
		}
		/*
		for (Transition t : transitions.values()) {
			if (t.getDelay() == null) {
				t.addDelay("0");
			}
		}
		*/
		return error;
	}

	private boolean parsePriorityAssign(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(PRIORITY_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern priorityPattern = Pattern.compile(PRIORITY);
			Matcher priorityMatcher = priorityPattern.matcher(lineMatcher
					.group(1).replace("\\s", ""));
			while (priorityMatcher.find()) {
				Transition transition = transitions.get(priorityMatcher
						.group(1));
				String priority = priorityMatcher.group(2);
				if (transition.addPriority(priority) == false) {
					error = false;
				}
			}
		}
		return error;
	}

	private boolean parseBooleanAssign(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(BOOLEAN_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString().replace("\\s", ""));
		if (lineMatcher.find()) {
			Pattern transPattern = Pattern.compile(BOOLEAN_TRANS);
			Matcher transMatcher = transPattern.matcher(lineMatcher.group(1)
					.replace("\\s", ""));
			Pattern assignPattern = Pattern.compile(BOOLEAN_ASSIGN);
			while (transMatcher.find()) {
				Transition transition = transitions.get(transMatcher.group(1));
				Matcher assignMatcher = assignPattern.matcher(transMatcher
						.group(2));
				for (int i = 0; i < booleans.size(); i++) {
					while (assignMatcher.find()) {
						String variable = assignMatcher.group(1);
						String assignment = assignMatcher.group(2);
						if (transition.addBoolAssign(variable, assignment) == false) {
							error = false;
						}
					}
				}
			}
		}
		return error;
	}

	private boolean parseTransitionRate(StringBuffer data, boolean error) {
		Pattern linePattern = Pattern.compile(TRANS_RATE_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			Pattern delayPattern = Pattern.compile(ENABLING);
			Matcher delayMatcher = delayPattern.matcher(lineMatcher.group(1));
			while (delayMatcher.find()) {
				Transition transition = transitions.get(delayMatcher.group(1));
				if (transition.addDelay("exponential(" + delayMatcher.group(2)
						+ ")") == false) {
					error = false;
				}
			}
		}
		return error;
	}

	private void parseFailTransitions(StringBuffer data) {
		Pattern linePattern = Pattern.compile(FAIL_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			for (String s : lineMatcher.group(1).split("\\s")) {
				if (!s.equals("")) {
					transitions.get(s).setFail(true);
				}
			}
		}
	}
	
	private void parsePersistentTransitions(StringBuffer data) {
		Pattern linePattern = Pattern.compile(PERSISTENT_LINE);
		Matcher lineMatcher = linePattern.matcher(data.toString());
		if (lineMatcher.find()) {
			for (String s : lineMatcher.group(1).split("\\s")) {
				if (!s.equals("")) {
					transitions.get(s).setPersistent(true);
				}
			}
		}
	}
	
	/**
	 * This method extracts the boolean variables associated with inequalities 
	 * involved in the boolean assignments and the enabling conditions on
	 * transitions. 
	 */
	public void parseBooleanInequalities(){
		/*
		 *  The expression trees for the boolean assignment and the enabling 
		 *  conditions are contained in the Transitions.
		 */
		HashMap<InequalityVariable, InequalityVariable> inequalities =
				new HashMap<InequalityVariable, InequalityVariable>();
		for(Transition T : transitions.values()){
			// Extract the inequalities from the boolean expression.
			for(ExprTree E : T.getBoolAssignTrees().values()){
				parseBooleanInequalities(E, inequalities);
			}
			
			// Extract the inequalities from the enabling condition.
			parseBooleanInequalities(T.getEnablingTree(), inequalities, T);
		}
		
		// Add the inequalities to the booleans and variables.
		for(InequalityVariable iv : inequalities.keySet()){
			booleans.put("$" + iv.toString(), iv);
			variables.add(iv);
		}
	}
	
	/**
	 * Extracts the boolean variables associated with inequalities from a 
	 * single ExprTree. This method is not meant for use with enabling
	 * conditions since it will not register the transition with the
	 * inequality variable.
	 * @param ET
	 * 		The expression tree for which the inequalities are extracted.
	 */
	private void parseBooleanInequalities(ExprTree ET,
			HashMap<InequalityVariable, InequalityVariable> previousInequalities){
		parseBooleanInequalities(ET, previousInequalities, null);
	}
	
	/**
	 * Extracts the boolean variables associated with inequalities from a 
	 * single ExprTree.
	 * @param ET
	 * 		The expression tree for which the inequalities are extracted.
	 * @param T
	 * 		If this expression tree is from an enabling condition, then the Transition
	 * 		whose enabling condition that gave rise to this expression tree. Null
	 * 		otherwise.
	 */
	private void parseBooleanInequalities(ExprTree ET, 
			HashMap<InequalityVariable, InequalityVariable> previousInequalities, Transition T){
		/*
		 *  This method servers as a driver method for a recursive like search.
		 *  The basic idea is to explore the tree until an inequality is found,
		 *  create a new variable for the inequality including the subtree rooted
		 *  at that node, and replace the node with a boolean variable.
		 */
		
		// Create a list of operators to match.
		String[] operators = new String[]{"<", "<=", ">", ">=", "="};
		
		// Get the nodes containing inequalities.
		ArrayList<ExprTree> inequalities = new ArrayList<ExprTree>();
		findInequalityNodes(ET, operators, inequalities);
		
		parseBooleanInequalities(inequalities, previousInequalities, T);
		
	}
	
	/**
	 * Extracts boolean inequalities at the nodes of relational operators provided.
	 * @param inequalities
	 * 		An ExprTree array containing nodes whose roots are a relational
	 * 		operator.
	 * @param T
	 * 		If this expression tree is from an enabling condition, then the Transition
	 * 		whose enabling condition that gave rise to this expression tree. Null
	 * 		otherwise.
	 */
	private void
	parseBooleanInequalities(ArrayList<ExprTree> inequalities,
			HashMap<InequalityVariable, InequalityVariable> previousInequalities, Transition T){
		// For each node, create an InequalityVariable, add it to the set of variables,
		// and replace the current node of the ExprTree with the InequaltiyVariable.
		
//		HashMap<InequalityVariable, InequalityVariable> variableMap =
//				new HashMap<InequalityVariable, InequalityVariable>();
		
		for(ExprTree ET : inequalities){ // ET phone home.
			
			// Extract the expression for naming the new InequalityVariable.
			String booleanName = "$" + ET.toString();
			
			// Create the InequalityVariable.
			InequalityVariable newVariable = new InequalityVariable(booleanName, "false",
					ET.shallowclone(), this);
			
			// Check if the Variable is present already.
			Variable v = booleans.get(booleanName);
			
			if(v != null){
				// Check if it is an InequalityVariable.
				if(!(v instanceof InequalityVariable)){
					throw new IllegalStateException("Name collision. The extracted "
							+ "name for an InequalityVariable matches a name already "
							+ "given to a boolean variable.");
				}
				/*
				else{
//					InequalityVariable iv = (InequalityVariable) v;
					
//					
//					Not needed anymore since the list is not dynamically change
//					anymore.
//					
//					iv.increaseCount();
				}
				*/
			}
			else{
				// Register variable with the continuous variable.
				// This is taken care of by the constructor.
				// TODO : finish.

				// Add the variable
				//booleans.put(booleanName, newVariable);
				//variables.add(newVariable);
				// Check if we have seen this variable before.
				InequalityVariable seenBefore = previousInequalities.get(newVariable);
				if(seenBefore == null){
					// We have not seen this variable before, so add it to the
					// list.
					previousInequalities.put(newVariable, newVariable);
					if(T != null){
						// If there is a transition, register it.
						newVariable.addTransition(T);
					}
				}
				else if(T != null){
					// We've seen this variable before. So no need to add it. Just
					// need to register the transition. If the transition is null
					// there is nothing to do.
					seenBefore.addTransition(T);
				}
				
			}
			
			// Replace the node into a boolean value.
			// The the type.
			ET.isit = 'b';
			ET.logical = true;
			
			// Change the name.
			ET.variable = booleanName;
			
			// Change the op.
			ET.op = "";
			
			// Remove the branches.
			ET.r1 = null;
			ET.r2 = null;
		}
		
		//return variableMap;
	}
	
	/**
	 * Searches an expression tree and finds the nodes that contain an operator.
	 * @param ET
	 * 		The ExprTree to search.
	 * @param operators
	 * 		The operators to find.
	 * @param nodes
	 * 		The list to add the found nodes to.
	 */
	private void findInequalityNodes(ExprTree ET, String[] operators,
			ArrayList<ExprTree> nodes){
		
		// Check if ET node is null, if so return. (Base case 1 for recursion.)
		if(ET == null){
			return;
		}
		
		// Check if ET is a node we want. (Base case 2 for recursion.
		// Relations cannot be nested.)
		for(int i=0; i<operators.length; i++){
			
			// Extract the operators to check.
			String op = ET.getOp();
			String oplist = operators[i];
			
			// Avoid issue of leading and trailing spaces.
			op = op.trim();
			oplist = oplist.trim();
			
			if(oplist.equals(op)){
				// If the two are equal, then this node is an inequality.
				
				// Check that this inequality involves a continuous variable
				// and not a discrete variable which is handled by the 
				// untimed code currently.
				if(ET.containsExactlyCont()){
					// So add it to the list and return. After determining.
					nodes.add(ET);
				}
				return;
			}
		}
		
		// The node is not an inequality so search the children trees.
		findInequalityNodes(ET.getLeftChild(), operators, nodes);
		findInequalityNodes(ET.getRightChild(), operators, nodes);
	}
	
	public int getTotalNumberOfContVars(){
		return continuous.size();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((booleans == null) ? 0 : booleans.hashCode());
		result = prime * result
				+ ((continuous == null) ? 0 : continuous.hashCode());
		result = prime * result
				+ ((integers == null) ? 0 : integers.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + lpnIndex;
		result = prime * result
				+ ((variables == null) ? 0 : variables.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LhpnFile other = (LhpnFile) obj;
		if (booleans == null) {
			if (other.booleans != null)
				return false;
		} else if (!booleans.equals(other.booleans))
			return false;
		if (continuous == null) {
			if (other.continuous != null)
				return false;
		} else if (!continuous.equals(other.continuous))
			return false;
		if (integers == null) {
			if (other.integers != null)
				return false;
		} else if (!integers.equals(other.integers))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (lpnIndex != other.lpnIndex)
			return false;
		if (variables == null) {
			if (other.variables != null)
				return false;
		} else if (!variables.equals(other.variables))
			return false;
		return true;
	}
	

    /**
     * This method fills integer arrays varIndexArrayThisLpn and varIndexArrayOtherLpn. 
     * Both arrays stores indices of shared variables between this lpn and otherLpn.  
     * variable index from THIS lpn.
     * @param varIndexArrayThisLpn
     * @param varIndexArrayOtherLpn
     * @param otherLpn
     */
    public void genIndexLists(int[] varIndexArrayThisLpn, int[] varIndexArrayOtherLpn, LhpnFile otherLpn){
    	int arrayIndex = 0;
    	DualHashMap<String, Integer> otherVarIndexMap = otherLpn.getVarIndexMap();
    	String[] interfaceVars = otherLpn.getInterfaceVariables();
    	for(int i = 0; i < interfaceVars.length; i++){
			String var = interfaceVars[i];
			Integer thisIndex = this._varIndexMap.getValue(var);
			if(thisIndex != null){
				varIndexArrayThisLpn[arrayIndex] = thisIndex;
				varIndexArrayOtherLpn[arrayIndex] = otherVarIndexMap.getValue(var);
				arrayIndex++;
			}
		}
    }
	
	private String[] getInterfaceVariables() {
    		int size = getAllInputs().keySet().size() + getAllOutputs().keySet().size();
    		String[] interfaceVariables = new String[size];
    		HashSet<String> interfaceSet = new HashSet<String>();
    		int i = 0;
    		for(String input : getAllInputs().keySet()){
    			interfaceVariables[i++] = input;
    			interfaceSet.add(input);
    		}
    		for(String output : getAllOutputs().keySet()){
    			if(interfaceSet.contains(output)) 
    				continue;
    			interfaceVariables[i++] = output;
    		}	
    	return interfaceVariables;
	}
	
    public void setThisIndexList(List<int[]> indexList){
    	this.thisIndexList = indexList;
    }
    
    public void setOtherIndexList(List<int[]> indexList){
    	this.otherIndexList = indexList;
    }
    
    public List<int[]> getThisIndexList(){
    	return this.thisIndexList;
    }
    
    public List<int[]> getOtherIndexList(){
    	return this.otherIndexList;
    }
    
    public int[] getThisIndexArray(int i){
    	return this.thisIndexList.get(i);
    }
    
    public int[] getOtherIndexArray(int i){
    	return this.otherIndexList.get(i);
    }

	private static final String PROPERTY = "#@\\.property ([^@]*)\\n";

	private static final String INPUT = "\\.inputs([[\\s[^\\n]]\\w+]*?)\\n";

	private static final String OUTPUT = "\\.outputs([[\\s[^\\n]]\\w+]*?)\\n";
	
	private static final String INTERNAL = "\\.internal([[\\s[^\\n]]\\w+]*?)\\n";

	private static final String INIT_STATE = "#@\\.init_state \\[(\\w*)\\]";

	private static final String TRANSITION = "\\.dummy([^\\n]*?)\\n";

	private static final String WORD = "(\\S+)";

	private static final String INTEGER = "([-\\d]+)";

	private static final String PLACE = "\\n([\\w_\\+-/&&[^\\.#]]+[ ]+[\\w_\\+-/]+([ ]*[\\w_\\+-/]+)*)";
	//"\\n([\\w_\\+-/&&[^\\.#]]+ [\\w_\\+-/]+)";

	private static final String CONTINUOUS = "#@\\.continuous ([.[^\\n]]*)\\n";

	private static final String VARS_INIT = "#@\\.init_vals \\{([\\S[^\\}]]*?)\\}";

	private static final String INIT_RATE = "#@\\.init_rates \\{([\\S[^\\}]]*?)\\}";

	private static final String INIT_COND = "<(\\w+)=([\\S^>]*?)>";

	private static final String VARIABLES = "#@\\.variables ([.[^\\n]]*)\\n";

	private static final String PLACES_LINE = "#\\|\\.places ([.[^\\n]]*)\\n";

	private static final String MARKING = "(\\<\\w+[+-](/\\d+)*,\\w+[+-](/\\d+)*\\>)|(\\w+)"; 
	//"\\w+";

	private static final String MARKING_LINE = "\\.marking \\{(.*)\\}";

	private static final String ENABLING_LINE = "#@\\.enablings \\{([.[^\\}]]*?)\\}";

	private static final String ENABLING = "<([\\S[^=]]+?)=\\[([^\\]]+?)\\]>?";

	private static final String ASSIGNMENT_LINE = "#@\\.assignments \\{([.[^\\}]]*?)\\}";

	private static final String RATE_ASSIGNMENT_LINE = "#@\\.rate_assignments \\{([.[^\\}]]*?)\\}";

	private static final String ASSIGNMENT = "<([\\S[^=]]+?)=\\[(\\S+?)\\]>";

	private static final String ASSIGN_VAR = "([^:]+?):=(.+)";

	private static final String DELAY_LINE = "#@\\.delay_assignments \\{([\\S[^\\}]]*?)\\}";

	private static final String DELAY = "<([\\w_]+)=\\[(\\S+?)\\]>";

	private static final String RANGE = "\\[([\\w-]+?),([\\w-]+?)\\]";

	private static final String PRIORITY_LINE = "#@\\.priority_assignments \\{([\\S[^\\}]]*?)\\}";

	private static final String PRIORITY = "<([\\w_]+)=\\[(\\S+?)\\]>";

	private static final String TRANS_RATE_LINE = "#@\\.transition_rates \\{([\\S[^\\}]]*?)\\}";

	private static final String FAIL_LINE = "#@\\.failtrans ([.[^\\n]]*)\\n";
	
	private static final String PERSISTENT_LINE = "#@\\.non_disabling ([.[^\\n]]*)\\n";

	private static final String BOOLEAN_LINE = "#@\\.boolean_assignments \\{([\\S[^\\}]]*?)\\}";

	private static final String BOOLEAN_TRANS = "<(\\S+?)=\\[(\\S*?)\\]>";

	private static final String BOOLEAN_ASSIGN = "([^:]+?):=(.+)";
	
	@Override
	public String toString() {
		return "LhpnFile [label=" + label + ", lpnIndex=" + lpnIndex + "]";
	}

	public StateGraph getStateGraph() {		
		return this.stateGraph;
	}

	public void addStateGraph(StateGraph stateGraph) {
		if (this.stateGraph == null)
			this.stateGraph = stateGraph;
	}

	

}