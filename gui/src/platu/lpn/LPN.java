package platu.lpn;

import java.util.*;
import java.util.Map.Entry;

import lmoore.TimedState;
import lmoore.zone.Zone;
import platu.logicAnalysis.Constraint;
import platu.lpn.VarType;
import platu.expression.ArrayElement;
import platu.expression.ArrayNode;
import platu.expression.VarNode;
import platu.project.Project;
import platu.stategraph.StateGraph;
import platu.stategraph.state.State;
import platu.IndexObjMap;

public class LPN {
	public static int nextID=1;
    static private List<String> emptyStringList = new ArrayList<String>();
    static private List<int[]> emptyArrayList = new ArrayList<int[]>();
    static private String[] emptyStringArray = new String[0];
    static private int[] emptyIntArray = new int[0];
    public static int[] counts = new int[10];
    public static boolean ENABLE_PRINT = false;

    public int ID=nextID++;
    protected Project prj;  // Pointer to the top-level ProjDef..
    protected String label;  // Label of this LPN.
    protected int index;
    protected VarSet inputs;
    protected VarSet outputs;
    protected VarSet internals;
    protected HashMap<String, VarNode> varNodeMap;
    protected LpnTranList transitions;
    protected Object initState;
    private int[] initMark;
    private HashMap<String, Integer> initVector;
    private Zone initZone;
    protected DualHashMap<String, Integer> varIndexMap = new DualHashMap<String, Integer>(); 
    
    protected List<LPNTran> inputTranSet = new ArrayList<LPNTran>();
    protected List<LPNTran> outputTranSet = new ArrayList<LPNTran>();
    protected List<Constraint> oldConstraintSet = new LinkedList<Constraint>();
    protected List<Constraint> newConstraintSet = new LinkedList<Constraint>();
    protected List<Constraint> frontierConstraintSet = new LinkedList<Constraint>();
    protected Set<Constraint> constraintSet = new HashSet<Constraint>();
    protected String[] interfaceVariables = emptyStringArray;
    protected int[] interfaceIndices = emptyIntArray;
    protected List<int[]> thisIndexList = emptyArrayList;
    protected List<int[]> otherIndexList = emptyArrayList;
    protected List<String> argumentList = emptyStringList;
    
	/*
     * The following members are dynamically updated and maintained during state space traversal
     */
    //protected HashMap<State, State> reachableSet;
    //protected HashMap<Integer, State> index2StateTbl;
    IndexObjMap stateCache;
    private HashMap<State, LpnTranList> enabledSetTbl;

    @Override
    public String toString() {
        String ret = "";
        ret += prj + "\n";
        ret += label + "\n";
        ret += getInputs() + "\n";
        ret += getOutputs() + "\n";
        ret += getInternals() + "\n";
        ret += getTransitions() + "\n";
//        ret += getInitStateTimed() + "\n";
        ret += this.varIndexMap + "\n";
        ret += this.initVector + "\n";
        ret += this.varNodeMap + "\n";
        return ret;
    }

    public LPN(Project prj, String label, VarSet inputs, VarSet outputs,
            VarSet internals, LpnTranList transitions, State initState) {
        this.prj = prj;
        this.label = label;
        this.inputs = inputs;
        this.outputs = outputs;
        this.internals = internals;
        this.transitions = transitions;
        this.initState = initState;
        if (prj == null || label == null || inputs == null ||//
                outputs == null || internals == null
                || transitions == null || initState == null) {
            new NullPointerException().printStackTrace();
        }
        if (ENABLE_PRINT) {
            System.out.println("new LPN()1: \t" + description());
        }

        // Adjust the visibility of lpn transitions.
        for (LPNTran curTran : transitions) {
            curTran.initialize(this, outputs);
        }

        //reachableSet = new HashMap<State, State>();
        //index2StateTbl = new HashMap<Integer, State>();
        enabledSetTbl = new HashMap<State, LpnTranList>();

        counts[0]++;
    }
    
    public LPN(Project prj, String label, VarSet inputs, VarSet outputs,
            VarSet internals, HashMap<String, VarNode> varNodeMap, LpnTranList transitions, 
            HashMap<String, Integer> initialVector, int[] initialMarkings, Zone initialZone) {
        this.prj = prj;
        this.label = label;
        this.inputs = inputs;
        this.outputs = outputs;
        this.internals = internals;
        this.transitions = transitions;
        this.initVector = initialVector;
        this.initMark = initialMarkings;
        this.initZone = initialZone;
        this.varNodeMap = varNodeMap;
        
        if (prj == null || label == null || inputs == null ||//
                outputs == null || internals == null
                || transitions == null) {
            new NullPointerException().printStackTrace();
        }
        
        if (ENABLE_PRINT) {
            System.out.println("new LPN()1: \t" + description());
        }

        // Adjust the visibility of lpn transitions.
        for (LPNTran curTran : transitions) {
            curTran.initialize(this, outputs);
        }

        //reachableSet = new HashMap<State, State>();
        //index2StateTbl = new HashMap<Integer, State>();
        this.stateCache = new IndexObjMap();
        enabledSetTbl = new HashMap<State, LpnTranList>();

        counts[0]++;
    }

    @Override
    public LPN clone() {
        // copy varNodeMap
        HashMap<String, VarNode> varNodeMap = new HashMap<String, VarNode>();
        List<ArrayNode> arrayNodes = new LinkedList<ArrayNode>();
        
        for(Entry<String, VarNode> e : this.varNodeMap.entrySet()){
        	VarNode var = e.getValue();
        	if(ArrayNode.class.isAssignableFrom(var.getClass())){
        		arrayNodes.add((ArrayNode)var);
        	}
        	else{
        		varNodeMap.put(e.getKey(), var.clone());
        	}
        }
        
        for(ArrayNode array : arrayNodes){
        	ArrayNode copyNode = (ArrayNode) array.copy(varNodeMap);
        	varNodeMap.put(copyNode.getName(), copyNode);
        }
        
        // copy transitions
        LpnTranList transitions = this.transitions.copy(varNodeMap);

        // copy varIndexMap
        DualHashMap<String, Integer> varIndexMap = this.varIndexMap.clone();
        
        // copy initVector
        HashMap<String, Integer> initVector = new HashMap<String, Integer>(this.initVector);
        
        // copy of zone
        LPN newLPN = new StateGraph(this.prj, this.label, this.inputs.clone(), this.outputs.clone(),
                this.internals.clone(), varNodeMap, transitions, initVector, this.initMark, this.initZone);
        
        transitions.setLPN(newLPN);
        newLPN.setVarIndexMap(varIndexMap);
        newLPN.argumentList = this.argumentList;

        return newLPN;
    }

    public void setGlobals(List<StateGraph> designUnitSet){
    	List<String> removeList = new ArrayList<String>();
    	List<String> addList = new ArrayList<String>();
    	for(String output : this.outputs){
    		VarNode var = varNodeMap.get(output);
    		if(var.getType() == VarType.GLOBAL){
    			if(ArrayNode.class.isAssignableFrom(var.getClass())){
    				removeList.add(output);
    				
    				for(VarNode element : ((ArrayNode)var).getVariableList()){
    					addList.add(element.getName());
    				}
    				
    				for(LPNTran lpnTran : this.transitions){
    	    			for(VarNode assignedVar : lpnTran.getAssignedVar()){
    		        		if(ArrayElement.class.isAssignableFrom(assignedVar.getClass()) && ((ArrayElement)assignedVar).getArray() == var){
    		        			this.addOutputTran(lpnTran);
    		        			lpnTran.setLocalFlag(false);
    		        			for(LPN dstLpn : designUnitSet){
    	    	    				if(dstLpn == this) continue;
    	    	    				
    		    	    			dstLpn.addInputTran(lpnTran);
    		    	    			lpnTran.addLpnList(dstLpn);  
    	    	    			}
    		        		}
    	    			}
    	        	}
    			}
    			else{
	    			for(LPNTran lpnTran : this.transitions){
	    	    		if(lpnTran.getAssignedVar().contains(var)){
	    	    			System.out.println(output);
	    	    			this.addOutputTran(lpnTran);
	    	    			lpnTran.setLocalFlag(false);
	    	    			
	    	    			for(LPN dstLpn : designUnitSet){
	    	    				if(dstLpn == this) continue;
	    	    				
		    	    			dstLpn.addInputTran(lpnTran);
		    	    			lpnTran.addLpnList(dstLpn);  
	    	    			}
	    	    		}
	    	    	}
    			}
    		}
    	}
    	
    	this.outputs.removeAll(removeList);
    	this.outputs.addAll(addList);
    }
    
    public LPN instantiate(String label){
    	LPN newLpn = this.clone();
    	State currentState = newLpn.getInitStateUntimed();
    	newLpn.setLabel(label);
//    	StateGraph sg = (StateGraph) newLpn;
//    	sg.setLabel(label);
 
    	HashMap<String, VarNode> varNodeMap = newLpn.getVarNodeMap();
    	HashMap<String, Integer> initialVector = newLpn.getInitVector();
    	DualHashMap<String, Integer> varIndexMap = newLpn.getVarIndexMap();

    	List<String> outputs = new LinkedList<String>();
    	VarSet outputSet = newLpn.getOutputs();
    	for(String output : outputSet){
    		VarNode var = varNodeMap.get(output);
    		if(var.getType() == VarType.GLOBAL){
    			outputs.add(var.getName());
    			continue;
    		}
    		
    		String newName = label + "." + output;
    		outputs.add(newName);
    		
    		varNodeMap.remove(output);
    		var.setName(newName);
    		varNodeMap.put(newName, var);
    		
    		if(ArrayNode.class.isAssignableFrom(var.getClass())){
    			List<VarNode> varList = ((ArrayNode)var).getVariableList();
    			int size = varList.size();
    			for(int i = 0; i < size; i++){
    				VarNode elementNode = varList.get(i);
    				int val = initialVector.get(elementNode.getName());
    				
    				varNodeMap.remove(elementNode.getName());
    				varIndexMap.delete(elementNode.getName());
    				initialVector.remove(elementNode.getName());
    				
    				String elementName = label + "." + elementNode.getName();
    				elementNode.setName(elementName);
    				
    				varNodeMap.put(elementName, elementNode);
    				initialVector.put(elementName, val);
    				varIndexMap.insert(elementName, elementNode.getIndex(currentState));
    			}
    		}
    		else{
	    		int val = initialVector.get(output);
	    		initialVector.remove(output);
	    		initialVector.put(newName, val);
	    		
	    		varIndexMap.delete(output);
	    		varIndexMap.insert(newName, var.getIndex(currentState));
    		}
    	}
    	
    	outputSet.clear();
    	outputSet.addAll(outputs);

    	List<String> internals = new LinkedList<String>();
    	VarSet internalSet = newLpn.getInternals();
    	for(String internal : internalSet){
    		String newName = label + "." + internal;
    		internals.add(newName);
    		
    		VarNode var = varNodeMap.get(internal);
    		varNodeMap.remove(internal);
    		var.setName(newName);
    		varNodeMap.put(newName, var);
    		
    		if(ArrayNode.class.isAssignableFrom(var.getClass())){
    			List<VarNode> varList = ((ArrayNode)var).getVariableList();
    			int size = varList.size();
    			for(int i = 0; i < size; i++){
    				VarNode elementNode = varList.get(i);
    				int val = initialVector.get(elementNode.getName());
    				
    				varNodeMap.remove(elementNode.getName());
    				varIndexMap.delete(elementNode.getName());
    				initialVector.remove(elementNode.getName());
    				
    				String elementName = label + "." + elementNode.getName();
    				elementNode.setName(elementName);
    				
    				varNodeMap.put(elementName, elementNode);
    				initialVector.put(elementName, val);
    				varIndexMap.insert(elementName, elementNode.getIndex(currentState));
    			}
    		}
    		else{
	    		int val = initialVector.get(internal);
	    		initialVector.remove(internal);
	    		initialVector.put(newName, val);
	    		
	    		varIndexMap.delete(internal);
	    		varIndexMap.insert(newName, var.getIndex(currentState));
    		}
    	}
    	
    	internalSet.clear();
    	internalSet.addAll(internals);
    	
    	// varIndexMap + initialVector
    	return newLpn;
    }
    
    public void connect(String outputVar, LPN dstLpn, String inputVar){
    	// change outputVar to an output if not already
    		// move to output list
    		// set type
    	
    	VarNode outputVarNode = this.varNodeMap.get(outputVar);
    	if(outputVarNode == null){
    		System.err.println("connect: " + outputVar + " does not exist in " + this.getLabel());
    		System.exit(1);
    	}
		
    	// find associated LPNTrans
    		// put in outputTranList if not already
    		// put in dstLpn's inputTranList
    		// set as non local
    	for(LPNTran lpnTran : this.transitions){
    		if(lpnTran.getAssignedVar().contains(outputVarNode)){
    			this.addOutputTran(lpnTran);
    			dstLpn.addInputTran(lpnTran);
    			lpnTran.setLocalFlag(false);
    			lpnTran.addLpnList(dstLpn);
    		}
    	}
    	
    	// get input VarNode
    		// make sure it is an input
    	VarNode inputVarNode = dstLpn.varNodeMap.get(inputVar);
    	dstLpn.varNodeMap.remove(inputVar);
    	dstLpn.varNodeMap.put(outputVar, inputVarNode);
    	if(inputVarNode == null){
    		System.err.println("connect: " + inputVar + " does not exist in " + dstLpn.getLabel());
    		System.exit(1);
    	}
    	else if(!dstLpn.getInputs().contains(inputVar)){
    		System.err.println("connect: " + inputVar + " is not an input variable");
    		System.exit(1);
    	}
    	
    	// make sure types are compatible
    	if(ArrayNode.class.isAssignableFrom(inputVarNode.getClass())){
			if(!ArrayNode.class.isAssignableFrom(outputVarNode.getClass())){
				System.err.println("error: variable " + inputVarNode.getName() + " is an array");
				System.exit(1);
			}
			
			// make sure dimensions are the same
			List<Integer> inputDimensions = ((ArrayNode)inputVarNode).getDimensionList();
			List<Integer> outputDimensions = ((ArrayNode)outputVarNode).getDimensionList();
			
			if(inputDimensions.size() != outputDimensions.size()){
				System.err.println("error: incompatible dimensions");
				System.exit(1);
			}
			
			for(int i = 0; i < inputDimensions.size(); i++){
				if(inputDimensions.get(i) != outputDimensions.get(i)){
					System.err.println("error: incompatible dimensions");
					System.exit(1);
				}
			}
    	}
    	else if(ArrayNode.class.isAssignableFrom(outputVarNode.getClass())){
    		System.err.println("error: variable " + outputVarNode.getName() + " is an array");
			System.exit(1);
    	}
    	
    	if(ArrayNode.class.isAssignableFrom(outputVarNode.getClass())){
        	if(this.internals.contains(outputVar)){
        		this.internals.remove(outputVar);
        		outputVarNode.setType(VarType.OUTPUT);
//        		this.outputs.add(outputVar);
        	}
        	else if(!outputVarNode.getType().equals(VarType.OUTPUT)){
        		System.err.println("connect: " + outputVar + " is not an internal or output var in " + this.getLabel());
        		System.exit(1);
        	}

    		ArrayNode inputArray = (ArrayNode) inputVarNode;
    		ArrayNode outputArray = (ArrayNode) outputVarNode;
    		
    		VarSet inputs = dstLpn.getInputs();
    		inputs.remove(inputArray.getName());
    		
    		inputArray.setAlias(inputArray.getName());
	    	inputArray.setName(outputArray.getName());
	    	
	    	List<VarNode> inputVarList = inputArray.getVariableList();
	    	List<VarNode> outputVarList = outputArray.getVariableList();
	    	HashMap<String, VarNode> dstVarNodeMap = dstLpn.getVarNodeMap();
	    	for(int i = 0; i < inputVarList.size(); i++){
	    		inputVarNode = inputVarList.get(i);
	    		inputVarNode.setType(VarType.INPUT);
	    		dstVarNodeMap.remove(inputVarNode.getName());
	    		
	    		outputVarNode = outputVarList.get(i);
	    		outputVarNode.setType(VarType.OUTPUT);

	    		for(LPNTran lpnTran : this.transitions){
	    			for(VarNode assignedVar : lpnTran.getAssignedVar()){
		        		if(ArrayElement.class.isAssignableFrom(assignedVar.getClass()) && ((ArrayElement)assignedVar).getArray() == outputArray){
		        			this.addOutputTran(lpnTran);
		        			dstLpn.addInputTran(lpnTran);
		        			lpnTran.setLocalFlag(false);
		        			lpnTran.addLpnList(dstLpn);
		        		}
	    			}
	        	}
	    		
	    		// get output initial value
	    		// modify dstLpn's init vector
		    	int initialValue = this.getInitVector().get(outputVarNode.getName());
		    	HashMap<String, Integer> initialVector = dstLpn.getInitVector();
		    	initialVector.remove(inputVarNode.getName());
		    	initialVector.put(outputVarNode.getName(), initialValue);
		    	
		    	// Change input var name
		    		// find var in varNodeMap
		    			// change name to output var name
		    			// set alias - old name
		    		// change inputs list
		    		// change varIndexMap
		    	
//		    	inputs.remove(inputVar);
//		    	inputs.add(outputVar);
		    	DualHashMap<String, Integer> varIndexMap = dstLpn.getVarIndexMap();
		    	varIndexMap.delete(inputVarNode.getName());
		    	varIndexMap.insert(outputVarNode.getName(), inputVarNode.getIndex(this.getInitStateUntimed()));
		    	
		    	inputVarNode.setAlias(inputVarNode.getName());
		    	inputVarNode.setName(outputVarNode.getName());
		    	dstVarNodeMap.put(inputVarNode.getName(), inputVarNode);
		    	
		    	inputs.add(inputVarNode.getName());
		    	this.outputs.add(outputVarNode.getName());
//		    	System.out.println(inputVarNode.getName() + ", " + outputVarNode.getName() + ", " + outputVar);
	    	}
    	}
    	else{
        	if(this.internals.contains(outputVar)){
        		this.internals.remove(outputVar);
        		this.outputs.add(outputVar);
        		outputVarNode.setType(VarType.OUTPUT);
        	}
        	else if(!this.outputs.contains(outputVar)){
        		System.err.println("connect: " + outputVar + " is not an internal or output var in " + this.getLabel());
        		System.exit(1);
        	}
        	
	    	// get output initial value
	    		// modify dstLpn's init vector
	    	int initialValue = this.getInitVector().get(outputVarNode.getName());
	    	HashMap<String, Integer> initialVector = dstLpn.getInitVector();
	    	initialVector.remove(inputVar);
	    	initialVector.put(outputVar, initialValue);
	    	
	    	// Change input var name
	    		// find var in varNodeMap
	    			// change name to output var name
	    			// set alias - old name
	    		// change inputs list
	    		// change varIndexMap
	    	
	    	inputVarNode.setAlias(inputVarNode.getName());
	    	inputVarNode.setName(outputVar);
	    	VarSet inputs = dstLpn.getInputs();
	    	inputs.remove(inputVar);
	    	inputs.add(outputVar);
	    	DualHashMap<String, Integer> varIndexMap = dstLpn.getVarIndexMap();
	    	varIndexMap.delete(inputVar);
	    	varIndexMap.insert(outputVar, inputVarNode.getIndex(this.getInitStateUntimed()));
    	}
    }
    
    public HashMap<String, VarNode> getVarNodeMap(){
    	return this.varNodeMap;
    }
    
    public void insert(LPNTran tran) {
        tran.setLpn(this);
        this.getTransitions().add(tran);

        counts[3]++;
    }

//    public final VarVal insert(final VarVal prop) {
//        counts[4]++;
//        return prj.insert(prop);
//    }

//    // Create a new LPN by composing 'this' LPN with 'other'.
//    final public LPN compose(final LPN other, final String dest) {
//        counts[5]++;
//        if (this == null) {
//            new NullPointerException().printStackTrace();
//        }
//        if (other == null) {
//            new NullPointerException().printStackTrace();
//        }
//        if (dest == null) {
//            new NullPointerException().printStackTrace();
//        }
//        if (getTransitions().size() == 0) {
//            new Exception("transitions.size()==0").printStackTrace();
//        }
//
//        VarSet otherIn = new VarSet(other.getInputs());
//        VarSet otherOut = new VarSet(other.getOutputs());
//        VarSet otherIntr = new VarSet(other.getInternals());
//        VarSet thisIn = new VarSet(this.getInputs());
//        VarSet thisOut = new VarSet(this.getOutputs());
//        VarSet thisIntr = new VarSet(this.getInternals());
//        VarSet retIn = new VarSet();
//        VarSet retOut = new VarSet();
//        VarSet retIntr = new VarSet();
//        retIn.addAll(thisIn);
//        retIn.addAll(otherIn);
//        retOut.addAll(thisOut);
//        retOut.addAll(otherOut);
//        retIntr.addAll(thisIntr);
//        retIntr.addAll(otherIntr);
//        retIn.removeAll(retOut);
//        retIn.removeAll(retIntr);
//
//        TimedState retInitState = null;//this.getInitStateTimed().compose(other.getInitStateTimed());
//        //        HashSet<String> common = new HashSet<String>();
//        //        Iterator<String> it = thisIn.iterator();
//        //        String tmp;
//        //        while (it.hasNext()) {
//        //            tmp = it.next();
//        //            if (otherIn.contains(tmp)) {
//        //                common.add(tmp);
//        //            }
//        //        }
//        if (otherIn == null
//                || otherOut == null
//                || otherIntr == null
//                || thisIn == null
//                || thisOut == null
//                || thisIntr == null
//                || retIn == null
//                || retOut == null
//                || retIntr == null) {
//            if (ENABLE_PRINT) {
//                System.err.println(otherIn + "\n\t"
//                        + otherOut + "\n\t"
//                        + otherIntr + "\n\t"
//                        + thisIn + "\n\t"
//                        + thisOut + "\n\t"
//                        + thisIntr + "\n\t"
//                        + retIn + "\n\t"
//                        + retOut + "\n\t"
//                        + retIntr);
//            }
//            new NullPointerException().printStackTrace();
//        }
//        LPN ret = new LPN(prj, dest,
//                new VarSet(retIn),
//                new VarSet(retOut),
//                new VarSet(retIntr),
//                new LPNTranSet(),
//                retInitState);
//        ret.getTransitions().addAll(this.getTransitions());
//        ret.getTransitions().addAll(other.getTransitions());
//        return ret;
//    }

    String blankString(int len) {
        String ret = "";
        for (int i = 0; i < len; i++) {
            ret += " ";
        }
        return ret;
    }

    public final String description() {
        String description = String.format("%-10s%-25s%-25s%-25s",
                getLabel() + ": ", "  Inputs: " + (getInputs().size()),
                "  Outputs: " + (getOutputs().size()), "  Internals: " + (getInternals().size()));
        return description;
    }

    public final String description2() {
        String description = String.format("%-10s%-25s%-25s%-25s",
                getLabel() + ": ", "  Inputs: " + (getInputs()),
                "  Outputs: " + (getOutputs()), "  Internals: " + (getInternals()));
        return description;
    }
    // end of function findSG(...)

    /**
    @return the instance_label
     */
    public String getLabel() {
        return label;
    }
    
    public void setIndex(int newIdx) {
    	this.index = newIdx;
    }
    
    public int getIndex() {
    	return this.index;
    }

    public Project getProj() {
        return this.prj;
    }

    /**
    @return the Inputs
     */
    public VarSet getInputs() {
        return inputs;
    }

    /**
    @return the Outputs
     */
    public VarSet getOutputs() {
        return outputs;
    }

    /**
    @return the Internals
     */
    public VarSet getInternals() {
        return internals;
    }

    /**
    @return the transitions
     */
    public LpnTranList getTransitions() {
        return transitions;
    }
    
    public LpnTranList getOutputTrans() {
        LpnTranList outputTranSet = new LpnTranList();

        for (LPNTran curTran : this.transitions) {
            HashSet<VarNode> assignedVars = curTran.getAssignedVar();
            for (VarNode var : assignedVars) {
                if (this.outputs.contains(var.getName()) == true) {
                    outputTranSet.add(curTran);
                    break;
                }
            }
        }

        return outputTranSet;
    }
    
    public void addInputTran(LPNTran inputTran){
    	this.inputTranSet.add(inputTran);
    }
    
    public void addOutputTran(LPNTran outputTran){
    	this.outputTranSet.add(outputTran);
    }
    
    public void addAllInputTrans(Collection<? extends LPNTran> inputTrans){
    	this.inputTranSet.addAll(inputTrans);
    }
    
    public void addAllOutputTrans(Collection<? extends LPNTran> outputTrans){
    	this.outputTranSet.addAll(outputTrans);
    }

    /**
    @return the InitState
     */
    public TimedState getInitStateTimed() {
    	// create initial vector
		int size = this.varIndexMap.size();
    	int[] initialVector = new int[size];
    	for(int i = 0; i < size; i++) {
    		String var = this.varIndexMap.getKey(i);
    		int val = this.initVector.get(var);
    		initialVector[i] = val;
    	}
    	
		this.initState = new TimedState(this.initMark, initialVector, this.initZone);
		TimedState s = (TimedState) this.initState;
		s.setLpn(this);
    	
    	return ((TimedState) this.initState);
    }

    /**
    @return the InitState
     */
    public State getInitStateUntimed() {	
    	// create initial vector
		int size = this.varIndexMap.size();
    	int[] initialVector = new int[size];
    	for(int i = 0; i < size; i++) {
    		String var = this.varIndexMap.getKey(i);
    		int val = this.initVector.get(var);
    		initialVector[i] = val;
    	}

		this.initState = new TimedState(this.initMark, initialVector, this.initZone);
		TimedState s = (TimedState) this.initState;
		s.setLpn(this);

    	return new State(this.initState);
    }
    
    public HashMap<String, Integer> getInitVector(){
    	return this.initVector;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @param inputs the inputs to set
     */
    public void setInputs(VarSet inputs) {
        this.inputs = inputs;
    }

    /**
     * @param outputs the outputs to set
     */
    public void setOutputs(VarSet outputs) {
        this.outputs = outputs;
    }

    /**
     * @param internals the internals to set
     */
    public void setInternals(VarSet internals) {
        this.internals = internals;
    }

    /**
     * @param transitions the transitions to set
     */
    public void setTransitions(LpnTranList transitions) {
        this.transitions = transitions;
    }

    /**
     * @param initState the initState to set
     */
    public void setInitState(TimedState initState) {
        this.initState = initState;
    }

    public void setVarIndexMap(DualHashMap<String, Integer> m) {
        this.varIndexMap = m;
    }

    public DualHashMap<String, Integer> getVarIndexMap() {
        return this.varIndexMap;
    }

    /*
     * Return the enabled transitions in the state with index 'stateIdx'.
     */
    public LpnTranList getEnabled(int stateIdx) {
    	State curState = this.getState(stateIdx);
        return this.getEnabled(curState);
    }

    // Return the set of all LPN transitions that are enabled in 'state'.
    public LpnTranList getEnabled(State curState) {
    	if (curState == null) {
            throw new NullPointerException();
        }
    	
    	if(enabledSetTbl.containsKey(curState) == true){
            return (LpnTranList)enabledSetTbl.get(curState).clone();
        }
    	
        LpnTranList curEnabled = new LpnTranList();
        for (LPNTran tran : this.transitions) {
        	if (tran.isEnabled(curState)) {
        		if(tran.local()==true)
        			curEnabled.addLast(tran);
                else
                	curEnabled.addFirst(tran);
             } 
        }
        
        this.enabledSetTbl.put(curState, curEnabled);
        return curEnabled;
    }

    public int reachSize() {
    	return this.stateCache.size();
    }

    public State getState(int stateIdx) {
    	return (State)this.stateCache.get(stateIdx);
//    	State tmp = this.index2StateTbl.get(index);
//    	return tmp;
    }

    public State addState(State aState) {
    	return (State)this.stateCache.add(aState);
//    	State tmp = reachableSet.get(aState);
//    	if(tmp == null) {
//    		aState.setIndex(reachableSet.size());
//    		this.reachableSet.put(aState, aState);
//    		this.index2StateTbl.put(aState.getIndex(), aState);
//    		return aState;
//    	}
//    	return tmp;
    }
    
    /**
     * Adds state into the state graph.
     * @param st - state to be inserted
     * @return null if the state is new, otherwise the existing state
     */
    public State addReachable(State st) {
    	State s = this.addState(st); //reachableSet.get(st);
    	
    	if(s == st){
//	    	reachableSet.put(st, st);
//	    	st.setIndex(reachableSet.size());
//	    	
	    	StateGraph sg = (StateGraph) this;
	    	sg.addState(st);
	    	
	    	sg.lpnTransitionMap.put(st, new LinkedList<LPNTran>());
    	}
    	
    	return s;
    }
    
//    public Set<State> reachable(){
//    	return this.reachableSet.keySet();
//    }
    
    public void genIndexLists(int[] thisIndexList, int[] otherIndexList, StateGraph otherSG){
    	int arrayIndex = 0;
    	DualHashMap<String, Integer> otherVarIndexMap = otherSG.getVarIndexMap();
    	String[] interfaceVars = otherSG.getInterfaceVariables();

    	for(int i = 0; i < interfaceVars.length; i++){
			String var = interfaceVars[i];
			Integer thisIndex = this.varIndexMap.getValue(var);
			if(thisIndex != null){
				thisIndexList[arrayIndex] = thisIndex;
				otherIndexList[arrayIndex] = otherVarIndexMap.getValue(var);
				
				arrayIndex++;
			}
		}
    }
    
    public List<LPNTran> getInputTranSet(){
    	return this.inputTranSet;
    }
    
    public List<LPNTran> getOutputTranSet(){
    	return this.outputTranSet;
    }
    
    public int[] getThisIndexArray(int i){
    	return this.thisIndexList.get(i);
    }
    
    public int[] getOtherIndexArray(int i){
    	return this.otherIndexList.get(i);
    }
    
    public void setArgumentList(List<String> argList){
    	this.argumentList = argList;
    }
    
    public List<String> getArgumentList(){
    	return this.argumentList;
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
    
    public List<Constraint> getOldConstraintSet(){
    	return this.oldConstraintSet;
    }
    
    /**
	 * Adds constraint to the constraintSet.
	 * @param c - Constraint to be added.
	 * @return True if added, otherwise false.
	 */
    public boolean addConstraint(Constraint c){
    	if(this.constraintSet.add(c)){
    		this.frontierConstraintSet.add(c);
    		return true;
    	}
    	
    	return false;
    }
    
    /**
	 * Adds constraint to the constraintSet.  Synchronized version of addConstraint().
	 * @param c - Constraint to be added.
	 * @return True if added, otherwise false.
	 */
    public synchronized boolean synchronizedAddConstraint(Constraint c){
    	if(this.constraintSet.add(c)){
    		this.frontierConstraintSet.add(c);
    		return true;
    	}
    	
    	return false;
    }
    
    public List<Constraint> getNewConstraintSet(){
    	return this.newConstraintSet;
    }
    
    public void genConstraints(){
    	oldConstraintSet.addAll(newConstraintSet);
    	newConstraintSet.clear();
    	newConstraintSet.addAll(frontierConstraintSet);
    	frontierConstraintSet.clear();
    }
    
    public String[] getInterfaceVariables(){
    	if(interfaceVariables.length == 0){
    		int size = inputs.size() + outputs.size();
    		interfaceVariables = new String[size];
    		HashSet<String> interfaceSet = new HashSet<String>();
    		int i = 0;
    		for(String input : inputs){
    			interfaceVariables[i++] = input;
    			interfaceSet.add(input);
    		}
    		
    		for(String output : outputs){
    			if(interfaceSet.contains(output)) continue;
    			interfaceVariables[i++] = output;
    		}
    	}
    	
    	return interfaceVariables;
    }
    
    public int[] getInterfaceIndices(){
    	if(interfaceIndices.length == 0){
    		int size = inputs.size() + outputs.size();
    		interfaceIndices = new int[size];
    		
    		int i = 0;
    		for(String input : inputs){
    			interfaceIndices[i++] = varIndexMap.getValue(input);
    		}
    		
    		for(String output : outputs){
    			interfaceIndices[i++] = varIndexMap.getValue(output);
    		}
    	}
    	
    	return interfaceIndices;
    }
}
