/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.dataModels.lpn.LpnDecomposition;

import java.util.ArrayList;

import edu.utah.ece.async.dataModels.lpn.LPN;
import edu.utah.ece.async.dataModels.lpn.Place;
import edu.utah.ece.async.dataModels.lpn.Transition;
import edu.utah.ece.async.dataModels.lpn.Variable;
import edu.utah.ece.async.dataModels.verification.platu.main.Options;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Component extends LPN{
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
		compTrans = new ArrayList<Transition>();
		compPlaces = new ArrayList<Place>();
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
	
	public void setProcessIDList(ArrayList<Integer> processIDList) {
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

	public LPN buildLPN(LPN lpnComp) {
		// Places
		for (int i=0; i< this.getComponentPlaces().size(); i++) {
			Place p = this.getComponentPlaces().get(i);
			lpnComp.addPlace(p.getName(), p.isMarked());
		}
		// Transitions
		for (int i=0; i< this.getCompTransitions().size(); i++) {
			Transition t = this.getCompTransitions().get(i);
			t.setIndex(i);
			lpnComp.addTransition(t);
		}
		// Inputs
		for (int i=0; i< this.getInputs().size(); i++) {
			Variable var = this.getInputs().get(i);
			lpnComp.addInput(var.getName(), var.getType(), var.getInitValue());
		}
		// Outputs
		for (int i=0; i< this.getOutputs().size(); i++) {
			Variable var = this.getOutputs().get(i);
			lpnComp.addOutput(var.getName(), var.getType(), var.getInitValue());
		}
		// Internal
		for (int i=0; i< this.getInternals().size(); i++) {
			Variable var = this.getInternals().get(i);
			lpnComp.addInternal(var.getName(), var.getType(), var.getInitValue());
		}
		return lpnComp;		
	}

	public ArrayList<Transition> getCompTransitions() {
		return compTrans;
	}

	public ArrayList<Place> getComponentPlaces() {
		return compPlaces;
	}
	
	public int getNumVars() {
		// return the number of variables in this component
		if (Options.getDebugMode()) {
			System.out.println("+++++++ Vars in component " + this.getComponentId() + "+++++++");
			System.out.println("compInputs:");
			for (int i=0; i < compInputs.size(); i++) {
				System.out.println(compInputs.get(i).getName());
			}
			System.out.println("compOutputs:");
			for (int i=0; i < compOutputs.size(); i++) {
				System.out.println(compOutputs.get(i).getName());
			}
			System.out.println("compInternal:");
			for (int i=0; i < compInternals.size(); i++) {
				System.out.println(compInternals.get(i).getName());
			}
			System.out.println("++++++++++++++++++");
		}
		return compInputs.size() + compInternals.size() + compOutputs.size();
	}
	

}
