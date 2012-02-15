package lpn.parser;

import java.util.ArrayList;

public class Component extends LhpnFile{
	private ArrayList<Integer> processIDList;
	private ArrayList<Transition> compTrans;
	private ArrayList<Place> compPlaces; 
	private ArrayList<Variable> compInputs;
	private ArrayList<Variable> compOutputs;
	private ArrayList<Variable> compInternals;
	private int compid;
	
	public Component(LpnProcess process) {
		compTrans = new ArrayList<Transition>();
		compPlaces = new ArrayList<Place>();
		processIDList = new ArrayList<Integer>();
		compInputs = new ArrayList<Variable>();
		compOutputs = new ArrayList<Variable>();
		compInternals = new ArrayList<Variable>();
		
		processIDList.add(process.getProcessId());
		compTrans.addAll(process.getProcessTransitions());
		compPlaces.addAll(process.getProcessPlaces());
		compInputs.addAll(process.getProcessInput());
		compOutputs.addAll(process.getProcessOutput());
		compInternals.addAll(process.getProcessInternal());	
		compid = process.getProcessId();
	}

	public Component() {
		//compTrans = new ArrayList<Transition>();
		//compPlaces = new ArrayList<Place>();
		processIDList = new ArrayList<Integer>();
		compInputs = new ArrayList<Variable>();
		compOutputs = new ArrayList<Variable>();
		compInternals = new ArrayList<Variable>();
	}
	
	public Component getComponent() {
		return this;
	}

	public Integer getComponentId() {
		return compid;
	}
	
	public void setComponentId(Integer id) {
		this.compid = id;
	}

	public ArrayList<Integer> getProcessIDList() {
		return processIDList;
	}
	
	public void setProcessIDList(ArrayList processIDList) {
		this.processIDList = processIDList;
	}

	public ArrayList<Variable> getInternals() {
		return compInternals;
	}

	public ArrayList<Variable> getOutputs() {
		return compOutputs;
	}

	public ArrayList<Variable> getInputs() {
		return compInputs;
	}

	public LhpnFile buildLPN(LhpnFile lpnComp) {
		// Places
		for (int i=0; i< this.getComponentPlaces().size(); i++) {
			Place p = this.getComponentPlaces().get(i);
			lpnComp.addPlace(p.getName(), p.isMarked());
		}
		// Transitions
		for (int i=0; i< this.getCompTransitions().size(); i++) {
			Transition t = this.getCompTransitions().get(i);
			lpnComp.addTransition(t);
		}
		// Inputs
		for (int i=0; i< this.getCompInput().size(); i++) {
			Variable var = this.getCompInput().get(i);
			lpnComp.addInput(var.getName(), var.getType(), var.getInitValue());
		}
		// Outputs
		for (int i=0; i< this.getCompOutput().size(); i++) {
			Variable var = this.getCompOutput().get(i);
			lpnComp.addOutput(var.getName(), var.getType(), var.getInitValue());
		}
		// Internal
		for (int i=0; i< this.getCompInternal().size(); i++) {
			Variable var = this.getCompInternal().get(i);
			lpnComp.addInternal(var.getName(), var.getType(), var.getInitValue());
		}
		return lpnComp;		
	}

	private ArrayList<Variable> getCompInternal() {
		return compInternals;
	}

	private ArrayList<Variable> getCompOutput() {
		return compOutputs;
	}

	private ArrayList<Variable> getCompInput() {
		return compInputs;
	}

	private ArrayList<Transition> getCompTransitions() {
		return compTrans;
	}

	private ArrayList<Place> getComponentPlaces() {
		return compPlaces;
	}
	
	public int getNumVars() {
		// return the number of variables in this component
		return compInputs.size() + compInternals.size() + compOutputs.size();
	}
	

}
