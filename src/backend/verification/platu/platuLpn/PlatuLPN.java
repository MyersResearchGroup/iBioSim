package backend.verification.platu.platuLpn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import backend.verification.platu.expression.ArrayNode;
import backend.verification.platu.expression.VarNode;
import backend.verification.platu.project.Project;
import backend.verification.platu.stategraph.StateGraph;

public class PlatuLPN {
	public static int nextID=1;
    static private List<String> emptyStringList = new ArrayList<String>();
    static private List<int[]> emptyArrayList = new ArrayList<int[]>();
    static private String[] emptyStringArray = new String[0];
    static private int[] emptyIntArray = new int[0];

    public int ID=nextID++;
    
    protected Project prj;  // Pointer to the top-level ProjDef..
    
    protected String label;  // Label of this LPN.
    
    protected int index;
    
    /* Variables read by this LPN but defined by other LPNs*/
    protected VarSet inputs;
    
    /* Variables defined this LPN. Shared variables can be defined by this and other LPNs */
    protected VarSet outputs;
    
    /* Variables defined and read only by this LPN*/
    protected VarSet internals;
    
    protected HashMap<String, VarNode> varNodeMap;
    
    protected LpnTranList transitions;
    
    //protected Object initState;
    private int[] initMark;
    private HashMap<String, Integer> initVector;

    protected DualHashMap<String, Integer> varIndexMap = new DualHashMap<String, Integer>(); 
    
    /* Transitions that modify the input variables in 'inputs'*/
    protected List<LPNTran> inputTranSet = new ArrayList<LPNTran>();
    
    /* Transitions that modify the output variables in 'outputs'*/
    protected List<LPNTran> outputTranSet = new ArrayList<LPNTran>();
    
    protected StateGraph stateGraph;
    protected String[] interfaceVariables = emptyStringArray;
    protected int[] interfaceIndices = emptyIntArray;
    protected List<int[]> thisIndexList = emptyArrayList;
    protected List<int[]> otherIndexList = emptyArrayList;
    protected List<String> argumentList = emptyStringList;
    

    public StateGraph getStateGraph(){
    	return this.stateGraph;
    }
    
    @Override
    public String toString() {
        String ret = "";
        ret += prj + "\n";
        ret += label + "\n";
        ret += getInputs() + "\n";
        ret += getOutputs() + "\n";
        ret += getInternals() + "\n";
        ret += getTransitions() + "\n";
        ret += this.varIndexMap + "\n";
        ret += this.initVector + "\n";
        ret += this.varNodeMap + "\n";
        return ret;
    }

    public PlatuLPN(Project prj, String label, VarSet inputs, VarSet outputs,
            VarSet internals, LpnTranList transitions) {
        this.prj = prj;
        this.label = label;
        this.inputs = inputs;
        this.outputs = outputs;
        this.internals = internals;
        this.transitions = transitions;
        if (prj == null || label == null || inputs == null ||//
                outputs == null || internals == null
                || transitions == null) {
            new NullPointerException().printStackTrace();
        }

        // Adjust the visibility of lpn transitions.
        // TODO: (temp) Hack here, but no problem as LPN.java will go away.
        // TODO: cut below because transitionsTemp is null
        /*
        ArrayList<LPNTran> transitionsTemp = null;
		for (LPNTran curTran : transitionsTemp) { //transitions) {
            curTran.initialize(this, outputs);
        }
		*/
        // TODO: (temp) Hack here, but no problem as LPN.java will go away.
        this.stateGraph = null; //new StateGraph(this);
    }
    
    public PlatuLPN(Project prj, String label, VarSet inputs, VarSet outputs,
            VarSet internals, HashMap<String, VarNode> varNodeMap, LpnTranList transitions, 
            HashMap<String, Integer> initialVector, int[] initialMarkings) {
        this.prj = prj;
        this.label = label;
        this.inputs = inputs;
        this.outputs = outputs;
        this.internals = internals;
        this.transitions = transitions;
        this.initVector = initialVector;
        this.initMark = initialMarkings;
        this.varNodeMap = varNodeMap;
        
        if (prj == null || label == null || inputs == null ||//
                outputs == null || internals == null
                || transitions == null) {
            new NullPointerException().printStackTrace();
        }
        
        // Adjust the visibility of lpn transitions.
        // TODO: (temp) Hack here, but no problem as LPN.java will go away.
        // (temp) Hack here, but no problem as LPN.java will go away.
        // TODO: cut below because transitionsTemp is null
        /*
        ArrayList<LPNTran> transitionsTemp = null;
        for (LPNTran curTran : transitionsTemp) {
            curTran.initialize(this, outputs);
        }
        */
        // TODO: (temp) Hack here, but no problem as LPN.java will go away.
        this.stateGraph = null;  // new StateGraph(this);
    }

    @Override
    public PlatuLPN clone() {
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
        // TODO: (temp) Hack here, but no problem as LPN.java will go away.
        LpnTranList transitions = null; //this.transitions.copy(varNodeMap);

        // copy varIndexMap
        DualHashMap<String, Integer> varIndexMap = this.varIndexMap.clone();
        
        // copy initVector
        HashMap<String, Integer> initVector = new HashMap<String, Integer>(this.initVector);
        
        // copy of zone
        PlatuLPN newLPN = new PlatuLPN(this.prj, this.label, this.inputs.clone(), this.outputs.clone(),
                this.internals.clone(), varNodeMap, transitions, initVector, this.initMark);
        
        // TODO: have to cut below because transitions is null
        // transitions.setLPN(newLPN);
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
    			    // TODO: (temp) Hack here, but no problem as LPN.java will go away.
    		        // TODO: cut below because transitionsTemp is null
    		        /*
    		        ArrayList<LPNTran> transitionsTemp = null;
    				for(LPNTran lpnTran : transitionsTemp) {//this.transitions){
    	    			for(VarNode assignedVar : lpnTran.getAssignedVar()){
    		        		if(ArrayElement.class.isAssignableFrom(assignedVar.getClass()) && ((ArrayElement)assignedVar).getArray() == var){
    		        			this.addOutputTran(lpnTran);
    		        			lpnTran.setLocalFlag(false);
    		        			for(StateGraph sg : designUnitSet){
    		        				// TODO: (temp) broken but okay depracting this class
    		        				*//*
    		        				LPN dstLpn = sg.getLpn();
    	    	    				if(dstLpn == this) continue;
    	    	    				
    		    	    			dstLpn.addInputTran(lpnTran);
    		    	    			lpnTran.addDstLpn(dstLpn);  
    		    	    			*//*
    	    	    			}
    		        		}
    	    			}
    	        	}
    				*/
    			}
    			else{
    			     // TODO: (temp) Hack here, but no problem as LPN.java will go away.
    		        // TODO: cut below because transitionsTemp is null
    		        /*
    		        ArrayList<LPNTran> transitionsTemp = null;
	    			for(LPNTran lpnTran : transitionsTemp) {//this.transitions){
	    	    		if(lpnTran.getAssignedVar().contains(var)){
	    	    			System.out.println(output);
	    	    			this.addOutputTran(lpnTran);
	    	    			lpnTran.setLocalFlag(false);
	    	    			
	    	    			for(StateGraph sg : designUnitSet){
		        				// TODO: (temp) broken but okay depracting this class
	    	    				*//*
	    	    				LPN dstLpn = sg.getLpn();
	    	    				if(dstLpn == this) continue;
	    	    				
		    	    			dstLpn.addInputTran(lpnTran);
		    	    			lpnTran.addDstLpn(dstLpn);  
		    	    			*//*
	    	    			}
	    	    		}
	    	    	}
    				*/
    			}
    		}
    	}
    	
    	this.outputs.removeAll(removeList);
    	this.outputs.addAll(addList);
    }
    
    public PlatuLPN instantiate(String label){
    	PlatuLPN newLpn = this.clone();
    	int[] currentVector = null;//newLpn.getInitState().getVector();
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
    				varIndexMap.insert(elementName, elementNode.getIndex(currentVector));
    			}
    		}
    		else{
	    		int val = initialVector.get(output);
	    		initialVector.remove(output);
	    		initialVector.put(newName, val);
	    		
	    		varIndexMap.delete(output);
	    		varIndexMap.insert(newName, var.getIndex(currentVector));
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
    				varIndexMap.insert(elementName, elementNode.getIndex(currentVector));
    			}
    		}
    		else{
	    		int val = initialVector.get(internal);
	    		initialVector.remove(internal);
	    		initialVector.put(newName, val);
	    		
	    		varIndexMap.delete(internal);
	    		varIndexMap.insert(newName, var.getIndex(currentVector));
    		}
    	}
    	
    	internalSet.clear();
    	internalSet.addAll(internals);
    	
    	// varIndexMap + initialVector
    	return newLpn;
    }
    
    public void connect(String outputVar, PlatuLPN dstLpn, String inputVar){
    	int[] initVector = null; // this.getInitState().getVector();
    	
    	// change outputVar to an output if not already
    		// move to output list
    		// set type
    	
    	VarNode outputVarNode = this.varNodeMap.get(outputVar);
    	if(outputVarNode == null){
    		System.err.println("connect: " + outputVar + " does not exist in " + this.getLabel());
    		return;
    	}
		
    	// find associated LPNTrans
    		// put in outputTranList if not already
    		// put in dstLpn's inputTranList
    		// set as non local
        // (temp) Hack here, but no problem as LPN.java will go away.
        // TODO: cut below because transitionsTemp is null
        /*
        ArrayList<LPNTran> transitionsTemp = null;
    	for(LPNTran lpnTran : transitionsTemp) {//this.transitions){
    		if(lpnTran.getAssignedVar().contains(outputVarNode)){
    			this.addOutputTran(lpnTran);
    			dstLpn.addInputTran(lpnTran);
    			lpnTran.setLocalFlag(false);
    			lpnTran.addDstLpn(dstLpn);
    		}
    	}
    	*/
    	// get input VarNode
    		// make sure it is an input
    	VarNode inputVarNode = dstLpn.varNodeMap.get(inputVar);
    	dstLpn.varNodeMap.remove(inputVar);
    	dstLpn.varNodeMap.put(outputVar, inputVarNode);
    	if(inputVarNode == null){
    		System.err.println("connect: " + inputVar + " does not exist in " + dstLpn.getLabel());
    		return;
    	}
    	else if(!dstLpn.getInputs().contains(inputVar)){
    		System.err.println("connect: " + inputVar + " is not an input variable");
    		return;
    	}
    	
    	// make sure types are compatible
    	if(ArrayNode.class.isAssignableFrom(inputVarNode.getClass())){
			if(!ArrayNode.class.isAssignableFrom(outputVarNode.getClass())){
				System.err.println("error: variable " + inputVarNode.getName() + " is an array");
				return;
			}
			
			// make sure dimensions are the same
			List<Integer> inputDimensions = ((ArrayNode)inputVarNode).getDimensionList();
			List<Integer> outputDimensions = ((ArrayNode)outputVarNode).getDimensionList();
			
			if(inputDimensions.size() != outputDimensions.size()){
				System.err.println("error: incompatible dimensions");
				return;
			}
			
			for(int i = 0; i < inputDimensions.size(); i++){
				if(inputDimensions.get(i) != outputDimensions.get(i)){
					System.err.println("error: incompatible dimensions");
					return;
				}
			}
    	}
    	else if(ArrayNode.class.isAssignableFrom(outputVarNode.getClass())){
    		System.err.println("error: variable " + outputVarNode.getName() + " is an array");
			return;
    	}
    	
    	if(ArrayNode.class.isAssignableFrom(outputVarNode.getClass())){
        	if(this.internals.contains(outputVar)){
        		this.internals.remove(outputVar);
        		outputVarNode.setType(VarType.OUTPUT);
//        		this.outputs.add(outputVar);
        	}
        	else if(!outputVarNode.getType().equals(VarType.OUTPUT)){
        		System.err.println("connect: " + outputVar + " is not an internal or output var in " + this.getLabel());
        		return;
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
	    		
	    	    // TODO: (temp) Hack here, but no problem as LPN.java will go away.
	            // TODO: cut below because transitionsTemp is null
	            /*
	    		for(LPNTran lpnTran : transitionsTemp) {//this.transitions){
	    			for(VarNode assignedVar : lpnTran.getAssignedVar()){
		        		if(ArrayElement.class.isAssignableFrom(assignedVar.getClass()) && ((ArrayElement)assignedVar).getArray() == outputArray){
		        			this.addOutputTran(lpnTran);
		        			dstLpn.addInputTran(lpnTran);
		        			lpnTran.setLocalFlag(false);
		        			lpnTran.addDstLpn(dstLpn);
		        		}
	    			}
	        	}
	             */	    		
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
		    	varIndexMap.insert(outputVarNode.getName(), inputVarNode.getIndex(initVector));
		    	
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
	    	varIndexMap.insert(outputVar, inputVarNode.getIndex(initVector));
    	}
    }
    
    public HashMap<String, VarNode> getVarNodeMap(){
    	return this.varNodeMap;
    }
    
    public void insert(LPNTran tran) {
        tran.setLpn(this);
        this.getTransitions().add(tran);
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

    static String blankString(int len) {
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
    
    public static LpnTranList getOutputTrans() {
        LpnTranList outputTranSet = new LpnTranList();
        // TODO: (temp) Hack here, but no problem as LPN.java will go away.
        // TODO: cut below because transitionsTemp is null
        /*
        ArrayList<LPNTran> transitionsTemp = null;
        for (LPNTran curTran : transitionsTemp) {// this.transitions) {
            HashSet<VarNode> assignedVars = curTran.getAssignedVar();
            for (VarNode var : assignedVars) {
                if (this.outputs.contains(var.getName()) == true) {
                    outputTranSet.add(curTran);
                    break;
                }
            }
        }
         */
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
    /*
    public State getInitState() {	
    	// create initial vector
		int size = this.varIndexMap.size();
    	int[] initialVector = new int[size];
    	for(int i = 0; i < size; i++) {
    		String var = this.varIndexMap.getKey(i);
    		int val = this.initVector.get(var);
    		initialVector[i] = val;
    	}
    	
		return new State(this, this.initMark, initialVector);
    }
    */
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

    public void setVarIndexMap(DualHashMap<String, Integer> m) {
        this.varIndexMap = m;
    }

    public DualHashMap<String, Integer> getVarIndexMap() {
        return this.varIndexMap;
    }
    
    public void genIndexLists(int[] thisIndexList, int[] otherIndexList, PlatuLPN otherLpn){
    	int arrayIndex = 0;
    	DualHashMap<String, Integer> otherVarIndexMap = otherLpn.getVarIndexMap();
    	String[] interfaceVars = otherLpn.getInterfaceVariables();

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
    
    public boolean isInput(LPNTran lpnTr) {
    	return this.inputTranSet.contains(lpnTr);
    }
    
    public boolean isOutput(LPNTran lpnTr) {
    	return this.outputTranSet.contains(lpnTr);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		PlatuLPN other = (PlatuLPN) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
}
