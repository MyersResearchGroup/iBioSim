package lpn.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Transition {

	private String name;

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
	
	private boolean sticky;

	private boolean local;
	
	// TODO: Sort LPNs that share variables in the assignedVarSet of this transition. 
	private List<LhpnFile> dstLpnList = new ArrayList<LhpnFile>();

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
		sticky = false;
	}
	
	public Transition(String name, int index, ArrayList<Variable> variables, LhpnFile lhpn, boolean local) {
		this.name = name;
		this.lhpn = lhpn;
		this.index = index;
		this.local = local;
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
		sticky = false;
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
		if (newEnab != null && !newEnab.equals("")) {
			expr.token = expr.intexpr_gettok(newEnab);
			retVal = expr.intexpr_L(newEnab);
			if (retVal) {
				enablingTree = expr;
				enabling = newEnab;
			}
		}
		return retVal;
	}

	public boolean addIntAssign(String variable, String assignment) {
		boolean retVal;
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(assignment);
		retVal = expr.intexpr_L(assignment);
		if (retVal) {
			intAssignTrees.put(variable, expr);
			intAssignments.put(variable, assignment);
		}
		return retVal;
	}

	public boolean addContAssign(String variable, String assignment) {
		boolean retVal;
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(assignment);
		retVal = expr.intexpr_L(assignment);
		if (retVal) {
			contAssignTrees.put(variable, expr);
			contAssignments.put(variable, assignment);
		}
		return retVal;
	}

	public boolean addRateAssign(String variable, String assignment) {
		boolean retVal;
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(assignment);
		retVal = expr.intexpr_L(assignment);
		if (retVal) {
			rateAssignTrees.put(variable, expr);
			rateAssignments.put(variable, assignment);
		}
		return retVal;
	}

	public boolean addDelay(String delay) {
		if (delay.equals("")) {
			this.delay = null;
			delayTree = null;
			return true;
		}
		boolean retVal;
		if (delay.matches("\\d+?,\\d+?")) {
			delay = "uniform(" + delay + ")";
		}
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(delay);
		retVal = expr.intexpr_L(delay);
		if (retVal) {
			delayTree = expr;
			this.delay = delay;
		}
		return retVal;
	}

	public boolean addPriority(String priority) {
		if (priority.equals("")) {
			this.priority = null;
			priorityTree = null;
			return true;
		}
		boolean retVal;
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(priority);
		retVal = expr.intexpr_L(priority);
		if (retVal) {
			priorityTree = expr;
			this.priority = priority;
		}
		return retVal;
	}

	public boolean addBoolAssign(String variable, String assignment) {
		boolean retVal;
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(assignment);
		retVal = expr.intexpr_L(assignment);
		if (retVal) {
			boolAssignTrees.put(variable, expr);
			boolAssignments.put(variable, assignment);
		}
		return retVal;
	}

	public void setName(String newName) {
		this.name = newName;
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

	public boolean hasConflictSet() {
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
	
	public String getName() {
		return name;
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
					conflictSet.add(t.getName());
				}
			}
		}
		return conflictSet;	
	}
	
	public Set<Integer> getConflictSetTransIndices() {
		Set<Integer> conflictSet = new HashSet<Integer>();
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
		return name;
	}

	public void changeName(String newName) {
		name = newName;
	}

	// TODO: (Check) Methods below are copied from LPNTran.
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

	public boolean local() {
		// Returns true if LPNTran only modifies non-output variables.
		boolean isLocal = true;
		for (Iterator<String> assignVarsIter = this.getAssignments().keySet().iterator(); assignVarsIter.hasNext();) {
			if (!this.getLpn().getAllInternals().keySet().contains(assignVarsIter.next())) {
				isLocal = false;
				break;
			}
		}
		return isLocal;
	}
	
	public List<LhpnFile> getDstLpnList(){
    	return this.dstLpnList;
    }
    
    public void addDstLpn(LhpnFile lpn){
    	this.dstLpnList.add(lpn);
    }
    
    public String getFullLabel() {
    	return this.lhpn.getLabel() + ":" + this.name;
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
//				if(this.sharePreSet(curTran) == false)
//				  return curTran;
				for (Iterator<String> confIter = this.getConflictSetTransNames().iterator(); confIter.hasNext();) { 
					String tran = confIter.next();
					if (curTran.getConflictSetTransNames().contains(tran))
						return curTran;
				}
			}
		}
		return null;
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}
	
	public boolean isSticky() {
		return this.sticky;
	}

	public void setDstLpnList(LhpnFile curLPN) {
		String[] allVars = curLPN.getVariables();
		boolean foundLPN = false;
		if (this.getAssignments() != null) {
			Set<String> assignedVars = this.getAssignments().keySet();  
			for (Iterator<String> assignedVarsIter = assignedVars.iterator(); assignedVarsIter.hasNext();) {
				String curVar = assignedVarsIter.next();
				for (int i=0; i<allVars.length; i++) {
					if (curVar.equals(allVars[i]) && !this.dstLpnList.contains(curLPN)) {
						this.dstLpnList.add(curLPN);
						foundLPN = true;
						break;
					}
				}
				if (foundLPN) 
					break;
			}
			
		}
		
		
	}

	/* Maybe copy method below is not needed.
	public Transition copy(HashMap<String, VarNode> variables){
    	return new Transition(this.name, this.index, this.preset, this.postset, this.enablingGuard.copy(variables), 
    			this.assignments.copy(variables), this.delayLB, this.delayUB, this.local);
    }
	*/
}