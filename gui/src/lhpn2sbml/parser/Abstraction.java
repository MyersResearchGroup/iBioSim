package lhpn2sbml.parser;

//import java.io.*;
import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

public class Abstraction extends LHPNFile {
	
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
	
	public Abstraction () {
		super();
	}
	
	public void abstractSTG() {
		// Transform 0 - Merge Parallel Places
		for (String s : controlPlaces.keySet()) {
			for (String t : controlPlaces.keySet()) {
				if (!s.equals(t)) {
					Properties prop1 = controlPlaces.get(s);
					Properties prop2 = controlPlaces.get(t);
					if (comparePreset(prop1, prop2) && comparePostset(prop1, prop2) && (places.get(s).equals(places.get(t)))) {
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
	}
	
	private boolean comparePreset(Properties flow1, Properties flow2) {
		String[] set1 = flow1.get("preset").toString().split(" ");
		String[] set2 = flow2.get("preset").toString().split(" ");
		if (set1.length != set2.length) {
			return false;
		}
		boolean contains = false;
		for (int i=0; i<set1.length; i++) {
			contains = false;
			for (int j=0; j<set2.length; j++) {
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
		String[] set1 = flow1.get("postset").toString().split(" ");
		String[] set2 = flow2.get("postset").toString().split(" ");
		if (set1.length != set2.length) {
			return false;
		}
		boolean contains = false;
		for (int i=0; i<set1.length; i++) {
			contains = false;
			for (int j=0; j<set2.length; j++) {
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
	
	public void combinePlaces(String place1, String place2) {
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
			for (int i=1; i<array.length; i++) {
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
			for (int i=1; i<array.length; i++) {
				if (array[i].equals(place2)) {
					array[i] = newPlace;
				}
				setString = setString + " " + array[i];
			}
			prop.setProperty("postset", setString);
			controlFlow.put(s, prop);
		}
		controlPlaces.remove(place2);
		places.remove(place2);
	}
	
	public void addPlaces(HashMap<String, Boolean> newPlaces) {
		for (String s : newPlaces.keySet()) {
			places.put(s, newPlaces.get(s));
		}
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
	
	public void addContinuousAssignmentTrees(HashMap<String, HashMap<String, ExprTree[]>> newAssignment) {
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
}