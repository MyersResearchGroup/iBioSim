package lhpn2sbml.parser;

import java.util.ArrayList;
import java.util.HashMap;

public class Transition {

	private String name;

	private boolean fail = false;

	private String enabling;

	private ExprTree enablingTree;

	private String delay;

	private ExprTree delayTree;

	private ArrayList<Place> preset;

	private ArrayList<Place> postset;

	private HashMap<String, String> boolAssignments;

	private HashMap<String, ExprTree> boolAssignTrees;

	private HashMap<String, String> intAssignments;

	private HashMap<String, ExprTree> intAssignTrees;

	private HashMap<String, String> contAssignments;

	private HashMap<String, ExprTree> contAssignTrees;

	private HashMap<String, String> rateAssignments;

	private HashMap<String, ExprTree> rateAssignTrees;
	
	private LhpnFile lhpn;

	public Transition(String name, ArrayList<Variable> variables, LhpnFile lhpn) {
		this.name = name;
		this.lhpn = lhpn;
		preset = new ArrayList<Place>();
		postset = new ArrayList<Place>();
		boolAssignments = new HashMap<String, String>();
		boolAssignTrees = new HashMap<String, ExprTree>();
		intAssignments = new HashMap<String, String>();
		intAssignTrees = new HashMap<String, ExprTree>();
		contAssignments = new HashMap<String, String>();
		contAssignTrees = new HashMap<String, ExprTree>();
		rateAssignments = new HashMap<String, String>();
		rateAssignTrees = new HashMap<String, ExprTree>();
	}

	public Transition() {
	}

	public void addPostset(Place place) {
		postset.add(place);
	}

	public void addPreset(Place place) {
		preset.add(place);
	}

	public boolean addEnabling(String newEnab) {
		if (newEnab == null) {
			return false;
		}
		if (newEnab.equals("")) {
			return true;
		}
		boolean retVal = false;
		enabling = newEnab;
		ExprTree expr = new ExprTree(lhpn);
		if (newEnab != null && !newEnab.equals("")) {
			expr.token = expr.intexpr_gettok(newEnab);
			retVal = expr.intexpr_L(newEnab);
			enablingTree = expr;
		}
		return retVal;
	}

	public boolean addIntAssign(String variable, String assignment) {
		boolean retVal;
		intAssignments.put(variable, assignment);
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(assignment);
		retVal = expr.intexpr_L(assignment);
		intAssignTrees.put(variable, expr);
		return retVal;
	}

	public boolean addContAssign(String variable, String assignment) {
		boolean retVal;
		contAssignments.put(variable, assignment);
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(assignment);
		retVal = expr.intexpr_L(assignment);
		contAssignTrees.put(variable, expr);
		return retVal;
	}
	
	public boolean addRateAssign(String variable, String assignment) {
		boolean retVal;
		rateAssignments.put(variable, assignment);
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(assignment);
		retVal = expr.intexpr_L(assignment);
		rateAssignTrees.put(variable, expr);
		return retVal;
	}
	
	public boolean addDelay(String delay) {
		if (delay.equals("")) {
			this.delay = null;
			delayTree = null;
			return true;
		}
		boolean retVal;
		this.delay = delay;
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(delay);
		retVal = expr.intexpr_L(delay);
		delayTree = expr;
		return retVal;
	}
	
	public boolean addBoolAssign(String variable, String assignment) {
		boolean retVal;
		boolAssignments.put(variable, assignment);
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(assignment);
		retVal = expr.intexpr_L(assignment);
		boolAssignTrees.put(variable, expr);
		return retVal;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public void setFail(boolean fail) {
		this.fail = fail;
	}
	
	public boolean isFail() {
		return fail;
	}
	
	public boolean isConnected() {
		return (preset.size() > 0 || postset.size() > 0);
	}
	
	public String getName() {
		return name;
	}
	
	public String getDelay() {
		return delay;
	}
	
	public ExprTree getDelayTree() {
		return delayTree;
	}
	
	public String getTransitionRate() {
		if (delayTree != null) {
			if (delayTree.op.equals("exponential")) {
				return delayTree.r1.toString();
			}	
		}
		return null;
	}
	
	public ExprTree getTransitionRateTree() {
		if (delayTree.op.equals("exponential")) {
			return delayTree.r1;
		}
		return null;
	}
	
	public Place[] getPreset() {
		Place[] array = new Place[preset.size()];
		int i = 0;
		for (Place p : preset) {
			array[i++] = p;
		}
		return array;
	}
	
	public Place[] getPostset() {
		Place[] array = new Place[postset.size()];
		int i = 0;
		for (Place p : postset) {
			array[i++] = p;
		}
		return array;
	}
	
	public String getEnabling() {
		return enabling;
	}
	
	public ExprTree getEnablingTree() {
		return enablingTree;
	}
	
	public HashMap<String, String> getAssignments() {
		HashMap<String, String> assignments = new HashMap<String, String>();
		assignments.putAll(boolAssignments);
		assignments.putAll(intAssignments);
		assignments.putAll(contAssignments);
		for (String var : rateAssignments.keySet()) {
			if (assignments.containsKey(var)) {
				assignments.put(var + " rate", rateAssignments.get(var));
			}
			else {
				assignments.put(var, rateAssignments.get(var));
			}
		}
		return assignments;
	}
	
	public HashMap<String, ExprTree> getAssignTrees() {
		HashMap<String, ExprTree> assignments = new HashMap<String, ExprTree>();
		assignments.putAll(boolAssignTrees);
		assignments.putAll(intAssignTrees);
		assignments.putAll(contAssignTrees);
		for (String var : rateAssignments.keySet()) {
			if (assignments.containsKey(var)) {
				assignments.put(var + " rate", rateAssignTrees.get(var));
			}
			else {
				assignments.put(var, rateAssignTrees.get(var));
			}
		}
		return assignments;
	}
	
	public ExprTree getAssignTree(String var) {
		if (boolAssignTrees.containsKey(var)) {
			return getBoolAssignTree(var);
		}
		if (intAssignTrees.containsKey(var)) {
			return getIntAssignTree(var);
		}
		if (contAssignTrees.containsKey(var)) {
			return getContAssignTree(var);
		}
		if (rateAssignTrees.containsKey(var)) {
			return getRateAssignTree(var);
		}
		if (var.split("\\s").length > 1) {
			return getRateAssignTree(var.split("\\s")[0]);
		}
		return null;
	}
	
	public HashMap<String, String> getContAssignments() {
		return contAssignments;
	}
	
	public HashMap<String, ExprTree> getContAssignTrees() {
		return contAssignTrees;
	}
	
	public String getContAssignment(String variable) {
		return contAssignments.get(variable);
	}
	
	public ExprTree getContAssignTree(String variable) {
		return contAssignTrees.get(variable);
	}
	
	public HashMap<String, String> getIntAssignments() {
		return intAssignments;
	}
	
	public HashMap<String, ExprTree> getIntAssignTrees() {
		return intAssignTrees;
	}
	
	public String getIntAssignment(String variable) {
		return intAssignments.get(variable);
	}
	
	public ExprTree getIntAssignTree(String variable) {
		return intAssignTrees.get(variable);
	}
	
	public HashMap<String, String> getRateAssignments() {
		return rateAssignments;
	}
	
	public HashMap<String, ExprTree> getRateAssignTrees() {
		return rateAssignTrees;
	}
	
	public String getRateAssignment(String variable) {
		return rateAssignments.get(variable);
	}
	
	public ExprTree getRateAssignTree(String variable) {
		return rateAssignTrees.get(variable);
	}
	
	public HashMap<String, String> getBoolAssignments() {
		return boolAssignments;
	}
	
	public HashMap<String, ExprTree> getBoolAssignTrees() {
		return boolAssignTrees;
	}
	
	public String getBoolAssignment(String variable) {
		return boolAssignments.get(variable);
	}
	
	public ExprTree getBoolAssignTree(String variable) {
		return boolAssignTrees.get(variable);
	}
	
	public void renamePlace(Place oldPlace, Place newPlace) {
		if (preset.contains(oldPlace)) {
			preset.add(newPlace);
			preset.remove(oldPlace);
		}
		if (postset.contains(oldPlace)) {
			postset.add(newPlace);
			postset.remove(oldPlace);
		}
	}
	
	public void removeEnabling() {
		enabling = null;
		enablingTree = null;
	}
	
	public void removePreset(Place place) {
		preset.remove(place);
	}
	
	public void removePostset(Place place) {
		postset.remove(place);
	}
	
	public void removeAllAssign() {
		boolAssignments.clear();
		contAssignments.clear();
		rateAssignments.clear();
		intAssignments.clear();
	}
	
	public void removeAssignment(String variable) {
		if (contAssignments.containsKey(variable)) {
			removeContAssign(variable);
		}
		if (rateAssignments.containsKey(variable)) {
			removeRateAssign(variable);
		}
		if (intAssignments.containsKey(variable)) {
			removeIntAssign(variable);
		}
		if (boolAssignments.containsKey(variable)) {
			removeBoolAssign(variable);
		}
		if (variable.split("\\s").length > 1) {
			removeRateAssign(variable.split("\\s")[0]);
		}
	}
	
	public void removeBoolAssign(String variable) {
		boolAssignments.remove(variable);
		boolAssignTrees.remove(variable);
	}
	
	public void removeContAssign(String variable) {
		contAssignments.remove(variable);
		contAssignTrees.remove(variable);
	}
	
	public void removeRateAssign(String variable) {
		rateAssignments.remove(variable);
		rateAssignTrees.remove(variable);
	}
	
	public void removeIntAssign(String variable) {
		intAssignments.remove(variable);
		intAssignTrees.remove(variable);
	}
	
	public boolean containsDelay() {
		return ((delay != null)&&!delay.equals(""));
	}
	
	public boolean containsPreset(String name) {
		return preset.contains(name);
	}
	
	public boolean containsPostset(String name) {
		return postset.contains(name);
	}
	
	public boolean containsAssignment(String var) {
		if (boolAssignments.containsKey(var)) {
			return true;
		}
		if (intAssignments.containsKey(var)) {
			return true;
		}
		if (contAssignments.containsKey(var)) {
			return true;
		}
		if (rateAssignments.containsKey(var)) {
			return true;
		}
		return false;
	}
	
	public void simplifyExpr() {
		if (enablingTree != null) {
			enabling = enablingTree.toString("LHPN");
		}
		for (String var : boolAssignTrees.keySet()) {
			boolAssignments.put(var, boolAssignTrees.get(var).toString("boolean", "LHPN"));
		}
		for (String var : intAssignTrees.keySet()) {
			intAssignments.put(var, intAssignTrees.get(var).toString("integer", "LHPN"));
		}
		for (String var : contAssignTrees.keySet()) {
			contAssignments.put(var, contAssignTrees.get(var).toString("continuous", "LHPN"));
		}
		for (String var : rateAssignTrees.keySet()) {
			rateAssignments.put(var, rateAssignTrees.get(var).toString("continuous", "LHPN"));
		}
	}
	
	public String toString() {
		return name;
	}
	
	public void changeName(String newName) {
		name = newName;
	}
	
}