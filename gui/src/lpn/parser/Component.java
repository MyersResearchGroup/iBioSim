package lpn.parser;

import java.util.ArrayList;

public class Component extends LhpnFile{
	private ArrayList<LpnProcess> processes = new ArrayList<LpnProcess>();
	private ArrayList<Transition> compTrans = new ArrayList<Transition>();
	private ArrayList<Place> compPlaces = new ArrayList<Place>();
	private ArrayList<Variable> compInput = new ArrayList<Variable>();
	private ArrayList<Variable> compOutput = new ArrayList<Variable>();
	private ArrayList<Variable> compInternal = new ArrayList<Variable>();
	private int id;
	
	public Component(LpnProcess process) {
		processes.add(process);
		compTrans.addAll(process.getProcessTransitions());
		compPlaces.addAll(process.getProcessPlaces());
		compInput.addAll(process.getProcessInput());
		compOutput.addAll(process.getProcessOutput());
		compInternal.addAll(process.getProcessInternal());	
	}
	
	public Component getComponent() {
		return this;
	}

}
