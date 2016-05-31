package lpn.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import main.Gui;
import verification.platu.main.Options;

public class Transition {

	private String label;

	private boolean fail = false;

	private boolean persistent = false;

	private String enabling;

	private ExprTree enablingTree;

	private String delay;

	private ExprTree delayTree;

	private String priority;

	private ExprTree priorityTree;

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
	
	private int index;
	
	/**
	 * List of LPNs that can be affected by firing this transition. 
	 */
	private List<LhpnFile> dstLpnList = new ArrayList<LhpnFile>();
	
	/**
	 * This field variable collects each product term of the transition's enabling condition. 
	 * It is initialized when buildConjunctsOfEnabling is called.
	 */
	private ArrayList<ExprTree> conjuncts; 

//	public Transition(String name, ArrayList<Variable> variables, LhpnFile lhpn) {
//		this.name = name;
//		this.lhpn = lhpn;
//		preset = new ArrayList<Place>();
//		postset = new ArrayList<Place>();
//		boolAssignments = new HashMap<String, String>();
//		boolAssignTrees = new HashMap<String, ExprTree>();
//		intAssignments = new HashMap<String, String>();
//		intAssignTrees = new HashMap<String, ExprTree>();
//		contAssignments = new HashMap<String, String>();
//		contAssignTrees = new HashMap<String, ExprTree>();
//		rateAssignments = new HashMap<String, String>();
//		rateAssignTrees = new HashMap<String, ExprTree>();
//	}
	
	public Transition(String name, int index, LhpnFile lhpn) {
		this.label = name;
		this.lhpn = lhpn;
		this.index = index;
		//this.local = local;
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

	public void addPostset(Place place) {
		if (postset != null)
			postset.add(place);
		else {
			postset = new ArrayList<Place>();
			postset.add(place);
		}
	}

	public void addPreset(Place place) {
		if (preset != null)
			preset.add(place);
		else {
			preset = new ArrayList<Place>();
			preset.add(place);
		}
	}

	public boolean addEnabling(String newEnab) {
		boolean retVal = false;
		if (newEnab == null) {
			return false;
		}
		if (newEnab.equals("")) {
			enabling = "";
			enablingTree = new ExprTree();
			retVal = true;
		}
		ExprTree expr = new ExprTree(lhpn);
		if (!newEnab.equals("")) {
			try {
				expr.token = expr.intexpr_gettok(newEnab);
				expr.intexpr_L(newEnab);
				enablingTree = expr;
				enabling = newEnab;
				return true;
			} catch (IllegalArgumentException e) {
				JOptionPane.showMessageDialog(Gui.frame, String.format("Error parsing %s\n",newEnab)+e.getMessage(),
						"Parse Error in Property", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return retVal;
	}
	
	public void addEnablingWithoutLPN(ExprTree newEnab) {
		if (newEnab != null) {
			enablingTree = newEnab;
			enabling = newEnab.toString();
		}
	}

	public boolean addIntAssign(String variable, String assignment) {
		ExprTree expr = new ExprTree(lhpn);
		try {
			expr.token = expr.intexpr_gettok(assignment);
			expr.intexpr_L(assignment);
			intAssignTrees.put(variable, expr);
			intAssignments.put(variable, assignment);
			return true;
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(Gui.frame, String.format("Error parsing %s\n",assignment)+e.getMessage(),
					"Parse Error in Property", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	public void addIntAssign(String variable, ExprTree assignment) {
		if (intAssignTrees == null && intAssignments == null) {
			intAssignTrees = new HashMap<String, ExprTree>();
			intAssignments = new HashMap<String, String>();
			intAssignTrees.put(variable, assignment);
			intAssignments.put(variable, assignment.toString());
		}
		else if (intAssignTrees == null && intAssignments != null){
			intAssignTrees = new HashMap<String, ExprTree>();
			intAssignTrees.put(variable, assignment);
			intAssignments.put(variable, assignment.toString());
		}
		else if (intAssignments == null && intAssignTrees != null) {
			intAssignments = new HashMap<String, String>();
			intAssignTrees.put(variable, assignment);
			intAssignments.put(variable, assignment.toString());
		}
		else {
			intAssignTrees.put(variable, assignment);
			intAssignments.put(variable, assignment.toString());
		}
	}

	public boolean addContAssign(String variable, String assignment) {
		ExprTree expr = new ExprTree(lhpn);
		try {
			expr.token = expr.intexpr_gettok(assignment);
			expr.intexpr_L(assignment);
			contAssignTrees.put(variable, expr);
			contAssignments.put(variable, assignment);
			return true;
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(Gui.frame, String.format("Error parsing %s\n",assignment)+e.getMessage(),
					"Parse Error in Property", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public boolean addRateAssign(String variable, String assignment) {
		ExprTree expr = new ExprTree(lhpn);
		try {
			expr.token = expr.intexpr_gettok(assignment);
			expr.intexpr_L(assignment);
			rateAssignTrees.put(variable, expr);
			rateAssignments.put(variable, assignment);
			return true;
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(Gui.frame, String.format("Error parsing %s\n",assignment)+e.getMessage(),
					"Parse Error in Property", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public boolean addDelay(String delay) {
		if (delay.equals("")) {
			this.delay = null;
			delayTree = null;
			return true;
		}
		if (delay.matches("\\d+?,\\d+?")) {
			delay = "uniform(" + delay + ")";
		}
		ExprTree expr = new ExprTree(lhpn);
		try {
			expr.token = expr.intexpr_gettok(delay);
			expr.intexpr_L(delay);
			delayTree = expr;
			this.delay = delay;
			return true;
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(Gui.frame, String.format("Error parsing %s\n",delay)+e.getMessage(),
					"Parse Error in Property", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public boolean addPriority(String priority) {
		if (priority.equals("")) {
			this.priority = null;
			priorityTree = null;
			return true;
		}
		ExprTree expr = new ExprTree(lhpn);
		try {
			expr.token = expr.intexpr_gettok(priority);
			expr.intexpr_L(priority);
			priorityTree = expr;
			this.priority = priority;
			return true;
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(Gui.frame, String.format("Error parsing %s\n",priority)+e.getMessage(),
					"Parse Error in Property", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public boolean addBoolAssign(String variable, String assignment) {
		ExprTree expr = new ExprTree(lhpn);
		try {
			expr.token = expr.intexpr_gettok(assignment);
			expr.intexpr_L(assignment);
			boolAssignTrees.put(variable, expr);
			boolAssignments.put(variable, assignment);
			return true;
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(Gui.frame, String.format("Error parsing %s\n",assignment)+e.getMessage(),
					"Parse Error in Property", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public void setName(String newName) {
		this.label = newName;
	}

	public void setFail(boolean fail) {
		this.fail = fail;
	}
	
	public void setIndex(int idx) {
    	this.index = idx;
    }

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public boolean isFail() {
		return fail;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public boolean isConnected() {
		return (preset.size() > 0 || postset.size() > 0);
	}

	public boolean isInteresting(ArrayList<Transition> visited) {
		visited.add(this);
		if (boolAssignments.size() > 0 || intAssignments.size() > 0
				|| contAssignments.size() > 0 || rateAssignments.size() > 0
				|| fail) {
			return true;
		}
		for (Place p : postset) {
			for (Transition t : p.getPostset()) {
				if (visited.contains(t)) {
					continue;
				}
				if (t.isInteresting(visited)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check if this transition shares a preset place with at least one other transition.
	 * @return <code>true</code> if it does
	 * 
	 */
	public boolean hasConflict() {
		for (Place p : getPreset()) {
			for (Transition t : p.getPostset()) {
				if (!this.toString().equals(t.toString())) {
					return true;
				}
			}
		}
		return false;
	}

	public int getIndex() {
    	return this.index;
    }
	
	public String getLabel() {
		return label;
	}

	public String getDelay() {
		return delay;
	}

	public String getPriority() {
		return priority;
	}

	public ExprTree getDelayTree() {
		return delayTree;
	}

	public ExprTree getPriorityTree() {
		return priorityTree;
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

	public Transition[] getConflictSet() {
		ArrayList<Transition> conflictSet = new ArrayList<Transition>();
		for (Place p : getPreset()) {
			for (Transition t : p.getPostset()) {
				if (!this.toString().equals(t.toString())) {
					conflictSet.add(t);
				}
			}
		}
		Transition[] returnSet = new Transition[conflictSet.size()];
		int i = 0;
		for (Transition t : conflictSet) {
			returnSet[i] = t;
			i++;
		}
		return returnSet;
	}
	
	public Set<String> getConflictSetTransNames() {
		Set<String> conflictSet = new HashSet<String>();
		for (Place p : getPreset()) {
			for (Transition t : p.getPostset()) {
				if (!this.toString().equals(t.toString())) {
					conflictSet.add(t.getLabel());
				}
			}
		}
		return conflictSet;	
	}
	
	public HashSet<Integer> getConflictSetTransIndices() {
		HashSet<Integer> conflictSet = new HashSet<Integer>();
		for (Place p : getPreset()) {
			for (Transition t : p.getPostset()) {
				if (!this.toString().equals(t.toString())) {
					conflictSet.add(t.getIndex());
				}
			}
		}
		return conflictSet;	
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
				assignments.put(var + "_rate", rateAssignments.get(var));
			} else {
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
				assignments.put(var + "_rate", rateAssignTrees.get(var));
			} else {
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
		return ((delay != null) && !delay.equals(""));
	}

	public boolean containsEnabling() {
		return ((enabling != null) && !enabling.equals(""));
	}

	public boolean containsPriority() {
		return ((priority != null) && !priority.equals(""));
	}

	public boolean containsPreset(String name) {
		return preset.contains(name);
	}

	public boolean containsPostset(String name) {
		return postset.contains(name);
	}

	public boolean containsAssignment() {
		return (boolAssignments.size() > 0 || intAssignments.size() > 0
				|| contAssignments.size() > 0 || rateAssignments.size() > 0);
	}

	public boolean containsBooleanAssignment() {
		return boolAssignments.size() > 0;
	}

	public boolean containsIntegerAssignment() {
		return intAssignments.size() > 0;
	}

	public boolean containsContinuousAssignment() {
		return contAssignments.size() > 0;
	}

	public boolean containsRateAssignment() {
		return rateAssignments.size() > 0;
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

	public boolean simplifyExpr(boolean change) {
		if (enablingTree != null) {
			if (!enabling.equals(enablingTree.toString("LHPN"))) {
				change = true;
			}
			String newEnab = enablingTree.toString("LHPN");
			addEnabling(newEnab);
		}
		if (delayTree != null) {
			if (!delay.equals(delayTree.toString("LHPN"))) {
				change = true;
			}
			String newDelay = delayTree.toString("LHPN");
			addDelay(newDelay);
		}
		for (String var : boolAssignTrees.keySet()) {
			if (!boolAssignments.get(var).equals(
					boolAssignTrees.get(var).toString("boolean", "LHPN"))) {
				change = true;
			}
			boolAssignments.put(var, boolAssignTrees.get(var).toString(
					"boolean", "LHPN"));
		}
		for (String var : intAssignTrees.keySet()) {
			if (!intAssignments.get(var).equals(
					intAssignTrees.get(var).toString("integer", "LHPN"))) {
				change = true;
			}
			intAssignments.put(var, intAssignTrees.get(var).toString("integer",
					"LHPN"));
		}
		for (String var : contAssignTrees.keySet()) {
			if (!contAssignments.get(var).equals(
					contAssignTrees.get(var).toString("continuous", "LHPN"))) {
				change = true;
			}
			contAssignments.put(var, contAssignTrees.get(var).toString(
					"continuous", "LHPN"));
		}
		for (String var : rateAssignTrees.keySet()) {
			if (!rateAssignments.get(var).equals(
					rateAssignTrees.get(var).toString("continuous", "LHPN"))) {
				change = true;
			}
			rateAssignments.put(var, rateAssignTrees.get(var).toString(
					"continuous", "LHPN"));
		}
		return change;
	}

	public boolean minimizeUniforms(boolean change) {
		if (enablingTree != null) {
			if (!enabling.equals(enablingTree.minimizeUniforms().toString(
					"LHPN"))) {
				change = true;
			}
			enabling = enablingTree.minimizeUniforms().toString("LHPN");
		}
		if (delayTree != null) {
			if (!delay.equals(delayTree.minimizeUniforms().toString("LHPN"))) {
				change = true;
			}
			delay = delayTree.minimizeUniforms().toString("LHPN");
		}
		for (String var : boolAssignTrees.keySet()) {
			if (!boolAssignments.get(var).equals(
					boolAssignTrees.get(var).minimizeUniforms().toString(
							"boolean", "LHPN"))) {
				change = true;
			}
			boolAssignments.put(var, boolAssignTrees.get(var)
					.minimizeUniforms().toString("boolean", "LHPN"));
		}
		for (String var : intAssignTrees.keySet()) {
			if (!intAssignments.get(var).equals(
					intAssignTrees.get(var).minimizeUniforms().toString(
							"integer", "LHPN"))) {
				change = true;
			}
			intAssignments.put(var, intAssignTrees.get(var).minimizeUniforms()
					.toString("integer", "LHPN"));
		}
		for (String var : contAssignTrees.keySet()) {
			if (!contAssignments.get(var).equals(
					contAssignTrees.get(var).minimizeUniforms().toString(
							"continuous", "LHPN"))) {
				change = true;
			}
			contAssignments.put(var, contAssignTrees.get(var)
					.minimizeUniforms().toString("continuous", "LHPN"));
		}
		for (String var : rateAssignTrees.keySet()) {
			if (!rateAssignments.get(var).equals(
					rateAssignTrees.get(var).minimizeUniforms().toString(
							"continuous", "LHPN"))) {
				change = true;
			}
			rateAssignments.put(var, rateAssignTrees.get(var)
					.minimizeUniforms().toString("continuous", "LHPN"));
		}
		return change;
	}

	@Override
	public String toString() {
		return "Transition [label=" + label + ", lhpn=" + lhpn.getLabel() + ", tranIndex="
				+ index + "]";
	}

	public void changeName(String newName) {
		label = newName;
	}

    /**
     * @return LPN object containing this LPN transition.
     */
    public LhpnFile getLpn() {
        return lhpn;
    }

	/**
     * @param lpn - Associated LPN containing this LPN transition.
     */
	public void setLpn(LhpnFile lpn) {
        this.lhpn = lpn;
    }

	public boolean isLocal() {
		// Returns true if LPNTran only modifies internal variables.
		boolean isLocal = true;
		for (String assignVar : this.getAssignments().keySet()) {
			if (!this.getLpn().getAllInternals().keySet().contains(assignVar)) {
				isLocal = false;
				break;
			}
		}
		return isLocal;
	}
	
//	public void setLocal(boolean local) {
//		this.local = local;
//	}
	
	public List<LhpnFile> getDstLpnList(){
    	return this.dstLpnList;
    }
    
//    public void addDstLpn(LhpnFile lpn){
//    	this.dstLpnList.add(lpn);
//    }
    
    public String getFullLabel() {
    	return this.getLabel() + "(" + this.getLpn().getLabel() + ")";
    }

    /**
	 * Check if firing 'fired_transition' causes a disabling error.
	 * @param current_enabled_transitions
	 * @param next_enabled_transitions
	 * @return
	 */
	public Transition disablingError(final LinkedList<Transition> current_enabled_transitions,
			LinkedList<Transition> next_enabled_transitions) {
		if (current_enabled_transitions == null || current_enabled_transitions.size()==0)
			return null;
		for(Transition curTran : current_enabled_transitions) {
			if (curTran == this)
				continue;
			boolean disabled = true;
			if (next_enabled_transitions != null && next_enabled_transitions.size()!=0) {
				for(Transition nextTran : next_enabled_transitions) {
					if(curTran == nextTran) {
						disabled = false;
						break;
					}
				}
			}
			if (disabled == true) {
				Place[] preset1 = this.getPreset();
				Place[] preset2 = curTran.getPreset();
				Boolean share=false;
				for (int i=0; i < preset1.length && !share; i++) {
					for (int j=0; j < preset2.length && !share; j++) {
						if (preset1[i].getName().equals(preset2[j].getName())) share=true;
					}
				}
				if (!share) return curTran;
//				if(this.sharePreSet(curTran) == false)
//				  return curTran;
				/*
				for (Iterator<String> confIter = this.getConflictSetTransNames().iterator(); confIter.hasNext();) { 
					String tran = confIter.next();
					if (curTran.getConflictSetTransNames().contains(tran))
						return curTran;
				}
				*/
			}
		}
		return null;
	}

	/**
	 * If this transition's assignment can cause curTran's enabling condition to become FALSE, 
	 * or it appears at either the right or left hand side of curTran's assignment,
	 * then it is added to the dstLpnList for this transition.
	 * @param curLPN
	 */
	public void setDstLpnList(LhpnFile curLPN) {	
		for (Transition curTran : curLPN.getAllTransitions()) {
			ExprTree curTranEnablingTree = curTran.getEnablingTree();
			if (curTranEnablingTree != null
					&& (curTranEnablingTree.getChange(this.getAssignments())=='F'
					|| curTranEnablingTree.getChange(this.getAssignments())=='f'
					|| curTranEnablingTree.getChange(this.getAssignments())=='X')) {
				this.dstLpnList.add(curLPN);						
				return;
			}									
			for (String v : this.getAssignTrees().keySet()) {
				for (ExprTree curTranAssignTree : curTran.getAssignTrees().values()) {
					if (curTranAssignTree != null && curTranAssignTree.containsVar(v)) {
						this.dstLpnList.add(curLPN);						
						return;	
					}					
				}
			}			
			for (String v1 : this.getAssignTrees().keySet()) {
				for (String v2 : curTran.getAssignTrees().keySet()) {
					if (v1.equals(v2) && !this.getAssignTree(v1).equals(curTran.getAssignTree(v2))) {
						this.dstLpnList.add(curLPN);						
						return;						
					}					
				}
			}
			if (Options.getMarkovianModelFlag()) { // && !Options.getTranRatePorDef().toLowerCase().equals("none")) {
				ExprTree curTranDelayTree = curTran.getDelayTree();
				if (curTranDelayTree != null) {
					for (String var : this.getAssignments().keySet()) {
						if (curTranDelayTree.containsVar(var)) {
							this.dstLpnList.add(curLPN);						
							return;	
						}							
					}
				}
			}
		}
	}

	/**
	 * This method takes the enabling condition of a transition (in the product-of-sums), and break the conjunction into conjuncts.
	 * @param term
	 */
	public void buildConjunctsOfEnabling(ExprTree term) {
		if (conjuncts == null)
			conjuncts = new ArrayList<ExprTree>();
		if (term.getOp().equals("&&")){
			buildConjunctsOfEnabling(term.getLeftChild());
			buildConjunctsOfEnabling(term.getRightChild());
		}
		else {
			conjuncts.add(term);
		}
	}
	
	public ArrayList<ExprTree> getConjunctsOfEnabling() {
		return conjuncts;
	}
	/* Maybe copy method below is not needed.
	public Transition copy(HashMap<String, VarNode> variables){
    	return new Transition(this.name, this.index, this.preset, this.postset, this.enablingGuard.copy(variables), 
    			this.assignments.copy(variables), this.delayLB, this.delayUB, this.local);
    }
	*/

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (fail ? 1231 : 1237);
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((lhpn == null) ? 0 : lhpn.hashCode());
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
		Transition other = (Transition) obj;
		if (fail != other.fail)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (lhpn == null) {
			if (other.lhpn != null)
				return false;
		} else if (!lhpn.equals(other.lhpn))
			return false;
		return true;
	}
	
}