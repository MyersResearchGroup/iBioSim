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
package edu.utah.ece.async.verification.lpn.LpnDecomposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import edu.utah.ece.async.verification.lpn.LPN;
import edu.utah.ece.async.verification.lpn.Place;
import edu.utah.ece.async.verification.lpn.Transition;
import edu.utah.ece.async.verification.lpn.Variable;
import edu.utah.ece.async.verification.platu.main.Options;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class LpnComponentList extends LPN{
	private Integer maxNumVarsInOneComp;
	private HashMap<Integer, Component> compMap;  // <ComponentId, Component>
	private HashMap<Variable, ArrayList<Integer>> sharedCompVarsMap;
	
	public LpnComponentList(Integer maxNumProcInOneComp) {
		compMap = new HashMap<Integer, Component>();
		sharedCompVarsMap = new HashMap<Variable, ArrayList<Integer>>();
		this.maxNumVarsInOneComp = maxNumProcInOneComp;
	}

	public void buildComponents(HashMap<Integer, LpnProcess> processMap, String directory, String lpnFileName) {
		HashMap<Variable, ArrayList<LpnProcess>> sharedProcessVarsMap = new HashMap<Variable, ArrayList<LpnProcess>>();
		Object[] allProcesses = processMap.values().toArray();
		for (int i=0; i<allProcesses.length; i++) {
			LpnProcess curProcess = (LpnProcess) allProcesses[i];
			for (int j=i+1; j<allProcesses.length; j++) {
				LpnProcess nextProcess = (LpnProcess) allProcesses[j];
				for (int k=0; k < curProcess.getProcessInput().size(); k++) {
					Variable curInput = curProcess.getProcessInput().get(k);
					if (nextProcess.getProcessOutput().contains(curInput)) {
						if (sharedProcessVarsMap.get(curInput) == null || sharedProcessVarsMap.get(curInput).isEmpty()) {
							ArrayList<LpnProcess> procArray = new ArrayList<LpnProcess>(2);
							procArray.add(curProcess);
							procArray.add(nextProcess);
							sharedProcessVarsMap.put(curInput, procArray);
						}
						else {
							if (!sharedProcessVarsMap.get(curInput).contains(curProcess)) 
								sharedProcessVarsMap.get(curInput).add(curProcess);
							if (!sharedProcessVarsMap.get(curInput).contains(nextProcess))
								sharedProcessVarsMap.get(curInput).add(nextProcess);
						}
					}
				}
				for (int k=0; k<curProcess.getProcessOutput().size(); k++) {
					Variable curOutput = curProcess.getProcessOutput().get(k);
					if (nextProcess.getProcessInput().contains(curOutput)) {
						if (sharedProcessVarsMap.get(curOutput) == null || sharedProcessVarsMap.get(curOutput).isEmpty()) {
							ArrayList<LpnProcess> procArray = new ArrayList<LpnProcess>(2);
							procArray.add(curProcess);
							procArray.add(nextProcess);
							sharedProcessVarsMap.put(curOutput, procArray);
						}
						else {
							if (!sharedProcessVarsMap.get(curOutput).contains(curProcess))
								sharedProcessVarsMap.get(curOutput).add(curProcess);
							if (!sharedProcessVarsMap.get(curOutput).contains(nextProcess))
								sharedProcessVarsMap.get(curOutput).add(nextProcess);
						}
					}
				}
			}
		}
		if (Options.getDebugMode()) {
			printSharedProcessVarMap(sharedProcessVarsMap);
			printProcessMap(processMap);
		}
		// Use wrappers to convert processMap to compMap, and sharedProcessVarsMap to sharedCompMap
		for (Integer procID : processMap.keySet()) {
			Component comp = new Component(processMap.get(procID));
			compMap.put(comp.getComponentId(), comp);
		}
		for (Variable v : sharedProcessVarsMap.keySet()) {
			ArrayList<Integer> compIDList = new ArrayList<Integer>();
			for (LpnProcess proc : sharedProcessVarsMap.get(v)) {
				compIDList.add(proc.getProcessId());
			}
			sharedCompVarsMap.put(v, compIDList);
		}
		boolean quitCoalesing = false;
		int iter = 0;
		while (!quitCoalesing) {
			if (Options.getDebugMode())
				printNumProcesses();
			LpnComponentGraph componentGraph = new LpnComponentGraph(sharedCompVarsMap, compMap, maxNumVarsInOneComp);
			if (Options.getDebugMode()) {
				String graphFileName = lpnFileName + maxNumVarsInOneComp + "Vars" + "_compGraph" + iter + ".dot";
				componentGraph.outputDotFile(directory + separator + graphFileName);
			}
			Vertex vertexToCoalesce = componentGraph.selectVerticesToCoalesce();
			if (vertexToCoalesce != null) {
				Component comp1 = compMap.get(vertexToCoalesce.componentID);
				Component comp2 = compMap.get(vertexToCoalesce.getMostConnectedNeighbor().componentID);
				if (Options.getDebugMode()) {
					System.out.println("*****Coalescing results*******");
					System.out.println("vertices to coalesce: " + vertexToCoalesce.componentID + ", " + vertexToCoalesce.getMostConnectedNeighbor().componentID);
					System.out.println("best net gain = " + vertexToCoalesce.getBestNetGain());
					System.out.println("**********************");
				}
				coalesceComponents(comp1, comp2);
			}
			else {
				if (Options.getDebugMode())
					System.out.println("No net gain of coalescing components.");
				quitCoalesing = true;
			}
			iter++;
		}
	}
	
	private void printNumProcesses() {
		System.out.println("************ processes in each componenet ***********");
		for (Component c : compMap.values()) {
			System.out.println(c.getComponentId() + " = " + c.getProcessIDList());
		}
		System.out.println("*****************************************************");
	}

	private static void printProcessMap(HashMap<Integer, LpnProcess> processMap) {
		System.out.println("~~~~~~~~~~~~process map~~~~~~~~~~~~~~");
		for (Iterator<Integer> processMapIter = processMap.keySet().iterator(); processMapIter.hasNext();) {
			Integer procID = processMapIter.next();
			System.out.println("process " + procID + " = " + processMap.get(procID).getProcessId());
		}
	}
	
	private void printComponentMap() {
		System.out.println("~~~~~~~~~~~~component map~~~~~~~~~~~~~~");
		for (Iterator<Integer> processMapIter = compMap.keySet().iterator(); processMapIter.hasNext();) {
			Integer procID = processMapIter.next();
			System.out.println("component " + procID + " = " + compMap.get(procID).getComponentId());
		}
	}

	public void coalesceComponents(Component comp1, Component comp2) {
		// coalesce 2 components, each including one process
		Component coalescedComp = new Component();
		// Create the newly coalesced component's processes, inputs, outputs, internals, and component ID.
		coalescedComp.setProcessIDList(comp1.getProcessIDList());
		coalescedComp.getProcessIDList().addAll(comp2.getProcessIDList());
		Integer coalescedCompId; 
		do {
			Random rand = new Random();
			coalescedCompId = rand.nextInt(2000);
		} while (compMap.keySet().contains(coalescedCompId));
		coalescedComp.setComponentId(coalescedCompId);
		ArrayList<Variable> coalescedInternals = coalescedComp.getInternals();
		ArrayList<Variable> coalescedOutputs = coalescedComp.getOutputs();
		ArrayList<Variable> coalescedInputs = coalescedComp.getInputs();
		ArrayList<Variable> sharedVariables = getSharedVariables(comp1, comp2);
		ArrayList<Place> coalescedPlaces = coalescedComp.getComponentPlaces();
		ArrayList<Transition> coalescedTrans = coalescedComp.getCompTransitions();
		comp1.getInputs().removeAll(sharedVariables);
		comp2.getInputs().removeAll(sharedVariables);
		comp1.getOutputs().removeAll(sharedVariables);
		comp2.getOutputs().removeAll(sharedVariables);
		coalescedInternals.addAll(sharedVariables);
		coalescedInternals.addAll(comp1.getInternals());
		coalescedInternals.addAll(comp2.getInternals());
		coalescedInputs.addAll(comp1.getInputs());
		coalescedInputs.addAll(comp2.getInputs());
		coalescedOutputs.addAll(comp1.getOutputs());
		coalescedOutputs.addAll(comp2.getOutputs());
		coalescedPlaces.addAll(comp1.getComponentPlaces());
		coalescedPlaces.addAll(comp2.getComponentPlaces());
		coalescedTrans.addAll(comp1.getCompTransitions());
		coalescedTrans.addAll(comp2.getCompTransitions());
		
		// Update sharedCompVarsMap
		for (Variable sharedVar : sharedCompVarsMap.keySet()) {
			if (sharedCompVarsMap.get(sharedVar).contains(comp1.getComponentId())) {
				sharedCompVarsMap.get(sharedVar).remove(comp1.getComponentId());
				if (!sharedCompVarsMap.get(sharedVar).contains(coalescedComp.getComponentId()))
					sharedCompVarsMap.get(sharedVar).add(coalescedComp.getComponentId());
			}
				
			if (sharedCompVarsMap.get(sharedVar).contains(comp2.getComponentId())) {
				sharedCompVarsMap.get(sharedVar).remove(comp2.getComponentId());
				if (!sharedCompVarsMap.get(sharedVar).contains(coalescedComp.getComponentId()))
					sharedCompVarsMap.get(sharedVar).add(coalescedComp.getComponentId());
			}
		}
		for (Variable v : sharedVariables) {
			sharedCompVarsMap.remove(v);
		}
		
		if (Options.getDebugMode())
			printSharedCompVarMap();
		// Update the compMap: remove the merged components and add the resultant component
		compMap.remove(comp1.getComponentId());
		compMap.remove(comp2.getComponentId());
		compMap.put(coalescedComp.getComponentId(), coalescedComp);	
		if (Options.getDebugMode())
			printComponentMap();
	}

	private ArrayList<Variable> getSharedVariables(Component comp1,
			Component comp2) {
		ArrayList<Variable> sharedVars = new ArrayList<Variable>();
		for (Variable v : sharedCompVarsMap.keySet()) {
			ArrayList<Integer> compIDs = sharedCompVarsMap.get(v);
			if (compIDs.contains(comp1.getComponentId()) && compIDs.contains(comp2.getComponentId()))
				sharedVars.add(v);
		}
		return sharedVars;
	}

	private static void printSharedProcessVarMap(
			HashMap<Variable, ArrayList<LpnProcess>> sharedVarMap) {
		System.out.println("~~~~~~ shared variables map ~~~~~~~~~~~");
		for (Iterator<Variable> sharedVarMapIter = sharedVarMap.keySet().iterator(); sharedVarMapIter.hasNext();) {
			Variable curSharedVarMap = sharedVarMapIter.next();
			System.out.print(curSharedVarMap + "\t");
			ArrayList<LpnProcess> map = sharedVarMap.get(curSharedVarMap);
			for (int i=0; i<map.size(); i++) {
				System.out.print(map.get(i).getProcessId() + ", ");
			}
			System.out.print("\n");
		}
	}
	
	private void printSharedCompVarMap() {
		System.out.println("~~~~~~ shared variables map (components)~~~~~~~~~~~");
		for (Iterator<Variable> sharedVarMapIter = sharedCompVarsMap.keySet().iterator(); sharedVarMapIter.hasNext();) {
			Variable curSharedVarMap = sharedVarMapIter.next();
			System.out.print(curSharedVarMap + "\t");
			ArrayList<Integer> map = sharedCompVarsMap.get(curSharedVarMap);
			for (int i=0; i<map.size(); i++) {
				System.out.print(map.get(i) + ", ");
			}
			System.out.print("\n");
		}
	}

	public Component getComponent(Integer componentID) {
		return compMap.get(componentID);		
	}
	
	public HashMap<Integer, Component> getComponentMap() {
		return compMap;
	}
	
	public HashMap<Variable,ArrayList<Integer>> getSharedCompVarsMap() {
		return sharedCompVarsMap;
	}
}
