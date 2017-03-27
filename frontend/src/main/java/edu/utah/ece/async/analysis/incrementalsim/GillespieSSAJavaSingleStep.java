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
package main.java.edu.utah.ece.async.analysis.incrementalsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;

import org.sbml.jsbml.*;

import main.java.edu.utah.ece.async.biomodel.util.SBMLutilities;
import main.java.edu.utah.ece.async.graph.Graph;
import main.java.edu.utah.ece.async.main.Gui;

import java.awt.BorderLayout;
import java.io.*;
import java.lang.Math;
import java.lang.Object;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamException;


/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class GillespieSSAJavaSingleStep {
	// SpeciesIndex maps from each species to a column index. 
	// ReactionsIndex maps from each reaction to a row index.
	// The state change vector is a 2-D array with reactions as rows and species as columns.
	// The amount of molecules for reactant j in Reaction i is indexed as SateChangeVetor[i][j]. 
	private HashMap<String, Integer> SpeciesToIndex = new HashMap<String, Integer>();
	private HashMap<Integer, String> IndexToSpecies = new HashMap<Integer, String>();
	private HashMap<String, Integer> ReactionsToIndex  = new HashMap<String, Integer>();
	private HashMap<Integer, String> IndexToReactions  = new HashMap<Integer, String>();
	private HashMap<String, Double> SpeciesList = new HashMap<String, Double>();
	private HashMap<String, Double> GlobalParamsList = new HashMap<String, Double>();
	private HashMap<String, ArrayList<Double>> EventAssignAtTriggerTime = new HashMap<String, ArrayList<Double>>();	
	
	private HashMap<String, Double> PropensityFunctionList = new HashMap<String, Double>();
	private HashMap<String, Boolean> EnoughMolecules = new HashMap<String, Boolean>();
	private String SpeciesID;
	private String GlobalParamID;
	
	private double SpeciesInitAmount;
	private double GlobalParamValue;
	
	private double PropensityFunctionValue = 0;
	private double PropensitySum=0.0;
	private double PropensityFunctionValueFW = 0;
	private double PropensityFunctionValueRV = 0;
	private double[][] StateChangeVector;
	private JTextField tNext;
	private JComboBox nextReactionsList;
	private JComboBox nextEventsList;
	private double tau=0.0;
	private int miu=0;
	private double time = 0;
	private double nextReactionTime = 0;
	private double nextTime = 0;
	private double nextEventTime = 0;
	private int NumIrreversible = 0;
	private int NumReversible = 0;
	private int NumReactions = 0;
	private FileOutputStream output;
	private PrintStream outTSD;
	private double runUntil = 0;
	private boolean[] prevTriggerValArray = null;
	private ListOf<Event> eventList = null;
	private PriorityQueue<EventQueueElement> eventQueue = null;
	private int initEventQueueCap = 10;
	private Model model = null;
	private int optValFireReaction=-1;
	private long randSeed;
	
	public GillespieSSAJavaSingleStep() {
	}
	public void PerformSim (String SBMLFileName,String outDir, double timeLimit, double timeStep, long rndSeed, Graph graph) throws FileNotFoundException{  
	 int optionValue = -1;
	 randSeed = rndSeed;
	 String outTSDName = outDir + "/run-1.tsd";
	 output = new FileOutputStream(outTSDName);
	 outTSD = new PrintStream(output);
	 SBMLDocument document = null;
	 try {
		 document = SBMLReader.read(new File(SBMLFileName));
	 } catch (XMLStreamException e1) {
		 JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file","Error Opening File", JOptionPane.ERROR_MESSAGE);
		 return;
	 } catch (IOException e1) {
		 JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file","Error Opening File", JOptionPane.ERROR_MESSAGE);
		 return;
	 }
	 model = document.getModel();
	 outTSD.print("(");
	 for (int i=0; i < model.getReactionCount(); i++){
		 Reaction reaction = model.getReaction(i);
		 if (reaction.getReversible())
			 NumReversible ++;
		 else
			 NumIrreversible ++;
	 }

	 
	 NumReactions = 2*NumReversible + NumIrreversible;
	 StateChangeVector=new double[NumReactions][model.getSpeciesCount()];
	 
	//---------Gillespie's SSA---------
	// 1. Initialization
	// Initialize time (t) and states (x) 
	 time = 0.0;
	 // get the species and the associated initial values
	 for (int i=0;i<model.getSpeciesCount();i++){
		 SpeciesID= model.getListOfSpecies().get(i).getId();
		 SpeciesInitAmount = model.getListOfSpecies().get(i).getInitialAmount();
		 SpeciesList.put(SpeciesID, SpeciesInitAmount);
		 SpeciesToIndex.put(SpeciesID,i);
		 IndexToSpecies.put(i,SpeciesID); 
	 }  	 
	 // get the global parameters
	 for (int i=0;i<model.getParameterCount();i++){
		 GlobalParamID= model.getListOfParameters().get(i).getId();
		 GlobalParamValue = model.getListOfParameters().get(i).getValue();
		 GlobalParamsList.put(GlobalParamID,GlobalParamValue);
	 }
	 
	 // print to the tsd file
	 if (SpeciesList.size() > 0 || GlobalParamsList.size() > 0) {
		 // print ID of species and (or) global parameters
		 outTSD.print("(\"time\",");
		 if (SpeciesList.size() > 0) {
			 for (int i=0;i<SpeciesList.size();i++){
				 if (i<SpeciesList.size()-1)
					 outTSD.print("\"" + SpeciesList.keySet().toArray()[i] + "\"" + ",");
				 else
					 outTSD.print("\"" + SpeciesList.keySet().toArray()[i] + "\"");
			 }
		 }
		 if (GlobalParamsList.size() > 0) {
			 if (SpeciesList.size() > 0)
				 outTSD.print(",");
			 for (int i=0;i<GlobalParamsList.size();i++){
				 if (i<GlobalParamsList.size()-1)
					 outTSD.print("\"" + GlobalParamsList.keySet().toArray()[i] + "\"" + ",");
				 else
					 outTSD.print("\"" + GlobalParamsList.keySet().toArray()[i] + "\"");
			 }
		 }
		 outTSD.print("),");
		 // print the initial values
		 printTSDVals();
	 }
	 
	 // initialize state change vector
	 int index = 0;
	 int l = 0;
	 while (index<NumReactions){
		 Reaction currentReaction = model.getListOfReactions().get(l);
		 
		 if (!currentReaction.getReversible()){
			 String currentReactionID = currentReaction.getId();
			 ReactionsToIndex.put(currentReactionID, index);
			 IndexToReactions.put(index, currentReactionID);
			 for (int j=0; j < currentReaction.getReactantCount(); j++){
				 String SpeciesAsReactant = currentReaction.getReactant(j).getSpecies();
				 StateChangeVector[index][SpeciesToIndex.get(SpeciesAsReactant)] = -currentReaction.getReactant(j).getStoichiometry();
			 }  
			 for (int j=0; j < currentReaction.getProductCount(); j++){
				 String SpeciesAsProduct = currentReaction.getProduct(j).getSpecies();
				 StateChangeVector[index][SpeciesToIndex.get(SpeciesAsProduct)] = currentReaction.getProduct(j).getStoichiometry();
			 }
			 index++;
		 }
		 else { // reversible reaction
			 String currentReactionID = currentReaction.getId();
			 ReactionsToIndex.put(currentReactionID, index);
			 ReactionsToIndex.put(currentReactionID + "_rev",  index+1);
			 IndexToReactions.put(index, currentReactionID);
			 IndexToReactions.put(index+1, currentReactionID+ "_rev");
			 for (int j=0; j < currentReaction.getReactantCount(); j++){
				 String SpeciesAsReactant = currentReaction.getReactant(j).getSpecies();
				 StateChangeVector[index][SpeciesToIndex.get(SpeciesAsReactant)] = -currentReaction.getReactant(j).getStoichiometry();
				 StateChangeVector[index+1][SpeciesToIndex.get(SpeciesAsReactant)] = currentReaction.getReactant(j).getStoichiometry();
			 }  
			 for (int j=0; j < currentReaction.getProductCount(); j++){
				 String SpeciesAsProduct = currentReaction.getProduct(j).getSpecies();
				 StateChangeVector[index][SpeciesToIndex.get(SpeciesAsProduct)] = currentReaction.getProduct(j).getStoichiometry();
				 StateChangeVector[index+1][SpeciesToIndex.get(SpeciesAsProduct)] = -currentReaction.getProduct(j).getStoichiometry();
			 }
			 index = index + 2;  
		 }
		 l++; 
	 }
	 
	 // TODO Initial assignments
	 // Initial evaluation of rules
	 evaluateRules(model);
	 
	 // TODO Initial evaluation of constraints
	 
	 
	  // Initialize events.
	 // if model does not include any events, both eventList and eventQueue are set to null
	 if (model.getListOfEvents().size() >0) {
		  eventList = model.getListOfEvents();
		  prevTriggerValArray = new boolean[eventList.size()];
		  for(int i=0; i< eventList.size();i++){
			  Event event = eventList.get(i); 
			  Trigger trigger = event.getTrigger();
			  if (trigger.getInitialValue()) {
				  prevTriggerValArray[i] = true;
			  }
			  else {
				  prevTriggerValArray[i] = false;
			  }
		  }
		  // Initialize event queue
		  EventQueueComparator comparator = new EventQueueComparator();
		  eventQueue = new PriorityQueue<EventQueueElement>(initEventQueueCap, comparator);
	 }
	 graph.editGraph();	  

	 double deltaTime = 0.01; // deltaTime is used to add to timeLimit, so that the while loop can terminate
	 while (time<=timeLimit) {
		  // 2. Update and fire events; evaluate propensity functions
		  if (timeStep == Double.MAX_VALUE) {
			  nextTime = timeLimit + deltaTime;
		  }
		  else {
			  nextTime = nextTime + timeStep;
		  }
		  evaluateRules(model);
		  String[] CustomParamsUpdateFireEvents = new String[2];
          CustomParamsUpdateFireEvents = UpdateFireEvents(graph);
          int optVal = Integer.parseInt(CustomParamsUpdateFireEvents[0]);
          boolean eventDelayNegative = Boolean.parseBoolean(CustomParamsUpdateFireEvents[1]);
          if (eventDelayNegative || optVal == 2 /*|| (time > 0 && optVal == -1)*/) 
        	  break;
          else 
        	  
		  if (nextEventTime < nextTime && nextEventTime>0) {
			  nextTime = nextEventTime;
		  }
		  
		  // TODO evaluate algebraic and rate rules, constraints
		  
		  // Evaluate propensity functions
		  InitializeEnoughMolecules();
		  PropensitySum = 0.0;
		  // get the reactions  
		  for (int i=0;i<model.getReactionCount();i++){
			  Reaction currentReaction = model.getListOfReactions().get(i);
			   
			  boolean ModifierNotEmpty = true;
			  if (currentReaction.getModifierCount()>0){
				  for (int k=0; k<currentReaction.getModifierCount();k++){
					  if (SpeciesList.get(currentReaction.getModifier(k).getSpecies())==0.0){
						  ModifierNotEmpty = false;
						  
					  }
				  }
			  }
			  String currentReactionID = currentReaction.getId();
			  // System.out.println("Reaction" + i + ": " + currentReactionID);
			  // get current kinetic law
			  KineticLaw currentKineticLaw = currentReaction.getKineticLaw();
			  // System.out.println("currentKineticLaw= " + currentKineticLaw.getFormula());
			  // get the abstract syntax tree of the current kinetic law
			  ASTNode currentAST=currentKineticLaw.getMath();
			  // Start evaluation from the top AST node: index 0 of ListOfNodes
			  ASTNode currentASTNode = currentAST.getListOfNodes().get(0);
			  // get the list of local parameters
			  ListOf<LocalParameter> currentListOfLocalParams = currentKineticLaw.getListOfLocalParameters();
			  HashMap<String, Double> LocalParamsList = new HashMap<String, Double>(); 
			  if (currentListOfLocalParams.size() > 0){  
				 for (int j=0; j<currentListOfLocalParams.size(); j++){
					 LocalParamsList.put(currentListOfLocalParams.get(j).getId(), currentListOfLocalParams.get(j).getValue());  
					
				 }
			  }
			  // calculate propensity function.
			  // For irreversible reaction, propensity function = kinetic law
			  if (!currentReaction.getReversible()){
				  // enzymatic reaction with no reactants
				  if (currentReaction.getReactantCount() == 0 && currentReaction.getProductCount()>0 && currentReaction.getModifierCount()>0){
					 boolean EnoughMoleculesCond = ModifierNotEmpty;
					 if(!EnoughMoleculesCond){
						 PropensityFunctionValue = 0;
						 EnoughMolecules.put(currentReactionID, false);
					 }
					 if (EnoughMolecules.get(currentReactionID)){
						 PropensityFunctionValue = Double.parseDouble(evaluatePropensityFunction(currentASTNode, LocalParamsList));
					 }
				  }
				  // other reactions
				  if (currentReaction.getReactantCount() > 0){ 
				    for (int j=0; j < currentReaction.getReactantCount(); j++){
					    // not enough reactant Molecules
					    boolean EnoughMoleculesCond = SpeciesList.get(currentReaction.getReactant(j).getSpecies()) >= currentReaction.getReactant(j).getStoichiometry();
					    if(!EnoughMoleculesCond){
						    PropensityFunctionValue = 0;
						    EnoughMolecules.put(currentReactionID, false);
						//  outTXT.println("EnoughMolecules: " + currentReactionID + " = " + EnoughMolecules.get(currentReactionID));
						    break;
					    }
				    }
				    if (EnoughMolecules.get(currentReactionID)){
				    	PropensityFunctionValue = Double.parseDouble(evaluatePropensityFunction(currentASTNode,LocalParamsList));
				    }
				  }
				    PropensitySum = PropensitySum + PropensityFunctionValue;
				    PropensityFunctionList.put(currentReactionID, PropensityFunctionValue);  
			  }
			  else {  // reversible reaction
				  // For reversible, the root node should be a minus operation
				  // Evaluate kinetic law for the forward reaction
				  // Check that there are enough Molecules for the reaction to happen     
				  for (int j=0; j < currentReaction.getReactantCount(); j++){
					  // not enough reactant Molecules
					  boolean EnoughMoleculesCondFW = SpeciesList.get(currentReaction.getReactant(j).getSpecies()) >= currentReaction.getReactant(j).getStoichiometry();
					  if(!EnoughMoleculesCondFW){
						  PropensityFunctionValueFW = 0;
						  EnoughMolecules.put(currentReactionID, false);      
						  break;
					  }
				  }
				  if (EnoughMolecules.get(currentReactionID)) {
				    PropensityFunctionValueFW = Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), LocalParamsList));
	
				  }
			      PropensitySum = PropensitySum + PropensityFunctionValueFW;
			      PropensityFunctionList.put(currentReactionID, PropensityFunctionValueFW);
			    
			      // Evaluate kinetic law for the reverse reaction
			      // Check that there are enough Molecules for the reaction to happen
			      for (int j=0; j < currentReaction.getProductCount(); j++){
			    	  // not enough reactant Molecules
			    	  boolean EnoughMoleculesCondRV = SpeciesList.get(currentReaction.getProduct(j).getSpecies()) >= currentReaction.getProduct(j).getStoichiometry();      
			    	  if(!EnoughMoleculesCondRV){
						  PropensityFunctionValueRV = 0;
						  EnoughMolecules.put(currentReactionID+"_rev", false);
						  break;
			    	  }
			      }
			      if (EnoughMolecules.get(currentReactionID+"_rev")){
			    	  PropensityFunctionValueRV = Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(),LocalParamsList));
			      }
			      PropensitySum = PropensitySum + PropensityFunctionValueRV;
			      PropensityFunctionList.put(currentReactionID+"_rev", PropensityFunctionValueRV);
			  } 
		 }
		 
		 if (PropensitySum == 0){
			 if (time > 0)
				 printTSDVals();
			 graph.refresh();
			 time = nextTime;
			 if (time <=0) {
				 JOptionPane.showMessageDialog(Gui.frame, "Sum of propensities is 0 and event queue is empty. Simulation time can not proceed.",
							"Error in next simulation time", JOptionPane.ERROR_MESSAGE);
				 break;
			 }
			 continue;
		 }
		 
		 // 3. Determine the time, tau, until the next reaction. 
		 // Detect if user specifies the time increment tau.
		 Random generator = new Random();
		 // Draw one uniform(0,1) random numbers r1. 
		 double r1 = generator.nextDouble();
		 // Determine randomly the time step, tau, until the next reaction. 
		 tau = (1/PropensitySum)*Math.log(1/r1);  
		 
		 // 4. Determine the next reaction: (miu is the row index of state change vector array)
		 double SumLeft = 0.0;
		 int count;
		 double r2 = generator.nextDouble();
		 for (count=0; SumLeft <= r2*PropensitySum; count++){
			 SumLeft = SumLeft + PropensityFunctionList.get(IndexToReactions.get(count));
			 if (SumLeft > r2*PropensitySum) break;
		 }
		 miu = count;
		 
		 // Pop up the interactive menu and asks the user to specify tau and miu
		 String[] CustomParamsReactionMenu=new String[3];
		 // optionValue: 0=step, 1=run, 2=terminate
		 boolean hasRunModeStarted = false;
		 boolean isRunMode = (optionValue == 1) && time < runUntil;
		 if (!isRunMode) {
			 CustomParamsReactionMenu = openReactionInteractiveMenu(time,tau,miu);
			 nextReactionTime = Double.parseDouble(CustomParamsReactionMenu[0]);
			 optionValue = Integer.parseInt(CustomParamsReactionMenu[2]);
			 if (optionValue != 2 && optionValue != -1) {  // 2 = terminate
				 while (nextReactionTime < time && optionValue!=2 && optionValue!=-1 ){
					 JOptionPane.showMessageDialog(Gui.frame, "The value of t_next needs to be greater than the current simulation time " + time + ".",
					"Error in next simulation time", JOptionPane.ERROR_MESSAGE);
					 CustomParamsReactionMenu = openReactionInteractiveMenu(time,tau,miu);
					 optionValue = Integer.parseInt(CustomParamsReactionMenu[2]);
					 nextReactionTime = Double.parseDouble(CustomParamsReactionMenu[0]);
				 }
				 while (nextReactionTime > nextTime && optionValue!=2 && optionValue!=-1 ) {
					 optValFireReaction = openReactionDialogue();
					 if (optValFireReaction == 0) {  // re-enter a new t_next
						 CustomParamsReactionMenu = openReactionInteractiveMenu(time,tau,miu);
						 optionValue = Integer.parseInt(CustomParamsReactionMenu[2]);
						 nextReactionTime = Double.parseDouble(CustomParamsReactionMenu[0]); 
					 }
					 else 
						 break; 
				 }
			 }
			 else {  // terminate
				 break;
			 }
			 
			 
			 if (optValFireReaction == 1) {
				 if (time > 0)
					 printTSDVals();
				 graph.refresh();
				 time = nextTime;
				 continue;
			 }
			 
			 if (optionValue == 0) {
				 tau = nextReactionTime - time;
				 miu = ReactionsToIndex.get(CustomParamsReactionMenu[1]);  
			 }
			 else if(optionValue == 1 && time >= runUntil){
				 runUntil = Double.parseDouble(CustomParamsReactionMenu[0]);
				 miu = ReactionsToIndex.get(CustomParamsReactionMenu[1]);
				 hasRunModeStarted = true;
				
			 }
			 else {  // optionValue == 2 (terminate)
				 break;
			 }
		 }
		 
		 // 5. Determine the new state: time = time + tau and x = x + v[miu]

		 if (nextTime <= time+tau)
			 time = nextTime;
		 else
			 time = time + tau;
		 // Determine the next reaction to fire, in row miu of StateChangeVector.
		 // Update the species amounts according to the state-change-vector in row miu. 
		 for (int i=0; i < model.getSpeciesCount(); i++){
			if (StateChangeVector[miu][i]!=0){
				String SpeciesToUpdate = IndexToSpecies.get(i);
				// System.out.println("SpeciesToUpdate = " + SpeciesToUpdate);
				if (EnoughMolecules.get(IndexToReactions.get(miu))){
					double SpeciesToUpdateAmount = SpeciesList.get(SpeciesToUpdate) + StateChangeVector[miu][i];
					SpeciesList.put(SpeciesToUpdate, SpeciesToUpdateAmount);
				} 
			 }
		 }	
		 printTSDVals();
		 if ((!isRunMode && !hasRunModeStarted) || (isRunMode && time >= runUntil)) {
			 graph.refresh();
		 }
	 }
	 outTSD.print(")");
	}

	static {
	   try {
		   System.loadLibrary("sbmlj");
	   }
	   catch (Exception e) {
	     System.err.println("Could not load libSBML library:" + e.getMessage());
	   }
	}
	
	private void printTSDVals() {
		 outTSD.print("(" + time + ", ");		 
		 if (SpeciesList.size() > 0) {
			 for (int i=0;i<SpeciesList.size();i++){
				 if (i<SpeciesList.size()-1)
					 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]) + ", ");
				 else
					 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]));
			 }
		 }
		 if (GlobalParamsList.size() > 0) {
			 if (SpeciesList.size() > 0)
				 outTSD.print(",");
			 for (int i=0;i<GlobalParamsList.size();i++){
				 if (i<GlobalParamsList.size()-1)
					 outTSD.print(GlobalParamsList.get(GlobalParamsList.keySet().toArray()[i]) + ", ");
				 else
					 outTSD.print(GlobalParamsList.get(GlobalParamsList.keySet().toArray()[i]));
			 }
		 }
		 outTSD.print("),");
	}
	
	
	private void evaluateRules(Model model) {
		ListOf<Rule> ruleList= null;
		 if (model.getListOfRules().size()>0) {
			 ruleList = model.getListOfRules();
			 for (int i=0; i<ruleList.size(); i++){
				 Rule currRule = ruleList.get(i);
				 if (currRule.isAssignment()) {
					 double assignment = Double.parseDouble(evaluateAST(currRule.getMath().getListOfNodes().get(0)));
					 String variable = SBMLutilities.getVariable(currRule);
					 if(SpeciesList.containsKey(variable)) {
						SpeciesList.put(variable, assignment);
					 }
					 else if(GlobalParamsList.containsKey(variable)){
						GlobalParamsList.put(variable, assignment);
					 }
				 }
				 else {
					 // TODO Initialize Algebraic and Rate rules
					  JOptionPane.showMessageDialog(Gui.frame, "The simulator does not currently support Algebraic and Rate rules.",
								"Error in rules", JOptionPane.ERROR_MESSAGE);
					  break;
				 }
			 }
		 }	
	}
	
	private String[] UpdateFireEvents(Graph graph) {
		  String[] CustomParamsEventFired = new String[2];
		  String[] CustomParamsUpdateFireEvents = new String[2];
		  String[] ReturnParamsFromUpdateEventQueue = new String[2];
		  boolean eventDelayNegative = false;
		  int optVal = -1;
		  if (model.getListOfEvents().size() >0) { 
			  do {
				  ReturnParamsFromUpdateEventQueue = updateEventQueue(eventList, eventQueue); // if (eventQueue is empty), then nextEventTime = -1
				  eventDelayNegative = Boolean.parseBoolean(ReturnParamsFromUpdateEventQueue[0]);
				  nextEventTime = Double.parseDouble(ReturnParamsFromUpdateEventQueue[1]);
				   if (!eventDelayNegative && eventQueue.size() > 0) {
					  CustomParamsEventFired = fireEvent(eventQueue, eventList, graph);
					  optVal = Integer.parseInt(CustomParamsEventFired[0]);
					  if (optVal == 2 || optVal == -1) { // 2=terminate -1=No interaction performed
						  break;
					  }
					  boolean eventFired = Boolean.parseBoolean(CustomParamsEventFired[1]); 
					  if (eventFired) {
						  evaluateRules(model);
						  // TODO evaluate algebraic and rate rules and constraints
						  ReturnParamsFromUpdateEventQueue = updateEventQueue(eventList, eventQueue); // if (eventQueue is empty), then nextEventTime = -1
						  eventDelayNegative = Boolean.parseBoolean(ReturnParamsFromUpdateEventQueue[0]);
						  nextEventTime = Double.parseDouble(ReturnParamsFromUpdateEventQueue[1]);
					  }  
				  }
				  if (eventDelayNegative) {
					  JOptionPane.showMessageDialog(Gui.frame, "Delay expression evaluates to a negative number.",
								"Error in piecewise function", JOptionPane.ERROR_MESSAGE);
					  eventDelayNegative = true;
					  break;
				  }
			  }
			  while (eventQueue.size() > 0 && time == eventQueue.peek().getScheduledTime());
		  }
		
		CustomParamsUpdateFireEvents[0] = optVal + "";
		CustomParamsUpdateFireEvents[1] = eventDelayNegative + "";
		return CustomParamsUpdateFireEvents;
	}
	
	public String[] updateEventQueue(ListOf<Event> eventList, PriorityQueue<EventQueueElement> eventQueue){
	 ArrayList<String> eventToRemoveArray = new ArrayList<String>();
	 boolean eventDelayNegative = false;
	 String[] ReturnParamsFromUpdateEventQueue = new String[2];
	 for(int i=0; i< eventList.size();i++){
		 Event currEvent = eventList.get(i); 
		 Trigger trigger = currEvent.getTrigger();
		 boolean currTriggerVal;
		 boolean prevTriggerVal = prevTriggerValArray[i];
		 double delayVal;
		 double priorityVal;
		 if (trigger == null) {
			 currTriggerVal = true;
		 }
		 else {
			 ASTNode triggerTopASTNode = trigger.getMath().getListOfNodes().get(0);
			 currTriggerVal = Boolean.parseBoolean(evaluateAST(triggerTopASTNode));
		 }
		 if (currEvent.getDelay() == null) {
			 delayVal = 0;
		 }
		 else {
			 ASTNode delayTopASTNode = currEvent.getDelay().getMath().getListOfNodes().get(0);
			 delayVal = Double.parseDouble(evaluateAST(delayTopASTNode));
		 }
		 if (currEvent.getPriority()== null) {
			  priorityVal = 0;
		 }
		 else {
			 ASTNode priorityTopASTNode = currEvent.getPriority().getMath().getListOfNodes().get(0);
			 priorityVal = Double.parseDouble(evaluateAST(priorityTopASTNode));
		 }
		 if (delayVal < 0) {
			 eventDelayNegative = true;
			 break;
		 }
		 
		 // update the event queue
		 if (!prevTriggerVal && currTriggerVal) {
			  if (currEvent.getUseValuesFromTriggerTime()) {
				  ListOf<EventAssignment> eventToFireAssignList = currEvent.getListOfEventAssignments();
				  ArrayList<Double> assignList = new ArrayList<Double>();
				  for (int j = 0; j < eventToFireAssignList.size(); j ++){
					  double assignVal = Double.parseDouble(evaluateAST(eventToFireAssignList.get(j).getMath().getListOfNodes().get(0)));
					  assignList.add(j, assignVal);
					  EventAssignAtTriggerTime.put(currEvent.getId(), assignList);
				  }
			  }
			  EventQueueElement currEventQueueElement = new EventQueueElement(time, currEvent.getId(), delayVal, priorityVal);
			  // add newly triggered event to the event queue
			  eventQueue.add(currEventQueueElement);
			  prevTriggerValArray[i] = currTriggerVal; 
		  }
		 
		 boolean eventQueueContainsEvent = false;
		 for (Iterator<EventQueueElement> eventQueueIterator = eventQueue.iterator();eventQueueIterator.hasNext();) {
		 	 EventQueueElement currEventQueueElement = eventQueueIterator.next();
		 	 if (currEvent.getId().equals(currEventQueueElement.getEventId())) {
		 		eventQueueContainsEvent = true;
		 	 }
		 }
		 
		 if (prevTriggerVal && !currTriggerVal ) { 
			  // non-persistent trigger
			  if (eventQueueContainsEvent && trigger != null && !trigger.getPersistent()) { 
				  // add event to eventToRemove
				  eventToRemoveArray.add(currEvent.getId());
			  }
			  prevTriggerValArray[i] = currTriggerVal;
		 }
	 }
	
	 // remove disabled non-persistent event from the queue
	 if (!eventToRemoveArray.isEmpty()) {
		 ArrayList<EventQueueElement> eventQueueElemToRemoveArray = new ArrayList<EventQueueElement>();
		 for (int j = 0; j < eventToRemoveArray.size(); j ++) {
			 String eventToRemove = eventToRemoveArray.get(j);
			 for (Iterator<EventQueueElement> eventQueueIterator = eventQueue.iterator();eventQueueIterator.hasNext();) {

				 EventQueueElement currEventQueueElement = eventQueueIterator.next();
			 	 if (eventToRemove.equals(currEventQueueElement.getEventId())) {
			 		 eventQueueElemToRemoveArray.add(currEventQueueElement);		 		 
			 	 }
			 }
		 } 
		 for (int j = 0; j < eventQueueElemToRemoveArray.size(); j++) {
			 eventQueue.remove(eventQueueElemToRemoveArray.get(j));
		 }
		 eventToRemoveArray.clear();
	 }
	 
	 ReturnParamsFromUpdateEventQueue[0] = eventDelayNegative + "";
	 if (!eventQueue.isEmpty()) {
		 ReturnParamsFromUpdateEventQueue[1] = eventQueue.peek().getScheduledTime() + "";
		 return ReturnParamsFromUpdateEventQueue;
	 }
	ReturnParamsFromUpdateEventQueue[1] = -1 + "";
	 return ReturnParamsFromUpdateEventQueue;
	 
	}
	
	private String[] fireEvent(PriorityQueue<EventQueueElement> eventQueue, ListOf<Event> eventList, Graph graph) {
		// Assume event assignments are evaluated at fire time
		boolean isEventFired = false;
		int optVal = -1;
		if (time == eventQueue.peek().getScheduledTime()) {
			isEventFired = true;
			ArrayList<EventQueueElement> eventReadyArray = new ArrayList<EventQueueElement>();
			EventQueueElement eventReady;
			// check if multiple events are ready to fire
			do {
				eventReady = eventQueue.poll();
				eventReadyArray.add(eventReady);
			}
			while(!eventQueue.isEmpty() && time == eventQueue.peek().getScheduledTime() && eventReady.getPriorityVal() == eventQueue.peek().getPriorityVal());
			// Pop up the interactive menu and asks the user to select event to fire
			String[] CustomParamsEventMenu=new String[2];
			CustomParamsEventMenu= openEventInteractiveMenu(eventReadyArray);
			optVal = Integer.parseInt(CustomParamsEventMenu[0]);
			int eventToFireIndex = Integer.parseInt(CustomParamsEventMenu[1]);
			if (optVal != 2 && optVal != -1) { // 2 = terminate  -1 = no action performed
				Event eventToFire = eventList.get(eventReadyArray.get(eventToFireIndex).getEventId());
				ListOf<EventAssignment> eventToFireAssignList = eventToFire.getListOfEventAssignments();
				if (!eventToFire.getUseValuesFromTriggerTime()) {
					for (int i = 0; i < eventToFireAssignList.size(); i ++){
						double assignment = Double.parseDouble(evaluateAST(eventToFireAssignList.get(i).getMath().getListOfNodes().get(0)));
						String varId = eventToFireAssignList.get(i).getVariable();
						if(SpeciesList.containsKey(varId)) {
							SpeciesList.put(varId, assignment);
						}
						else if(GlobalParamsList.containsKey(varId)){
							GlobalParamsList.put(varId, assignment);
						}
					}
				}
				else {  // UseValuesFromTriggerTime
					// get event assignment(s) of eventToFire, and update species and global parameters.
					ArrayList<Double> assignValList = EventAssignAtTriggerTime.get(eventToFire.getId());				
					for (int j=0; j < assignValList.size(); j++) {
						String varId = eventToFireAssignList.get(j).getVariable();
						Double varVal = assignValList.get(j);
						if(SpeciesList.containsKey(varId)) {
							SpeciesList.put(varId, varVal);
						}
						else if(GlobalParamsList.containsKey(varId)){
							GlobalParamsList.put(varId, varVal);
						}
					}
				}

				// If multiple events were popped off the queue and placed in eventsReadyArray, put the all events in eventsReadyArray (except eventToFire) back to the event queue.
				EventQueueElement eventFired = null;
				for (Iterator<EventQueueElement> eventReadyArrayIter = eventReadyArray.iterator();eventReadyArrayIter.hasNext();) {
					EventQueueElement currEventQueueElement = eventReadyArrayIter.next();
					if (eventToFire.getId().equals(currEventQueueElement.getEventId())) {
						eventFired = currEventQueueElement;
						break;
					}
				}
				if (eventFired != null)
					eventReadyArray.remove(eventFired);
				// add events that were not fired back to the event queue.
				for (int i = 0; i < eventReadyArray.size(); i++){
					eventQueue.add(eventReadyArray.get(i));
				}
				eventReadyArray.clear();	
				printTSDVals();
				graph.refresh();
			}			
		}
		String[] CustomParamsFireEvent = new String[2];
		CustomParamsFireEvent[0] = optVal + "";
		CustomParamsFireEvent[1] = isEventFired + "";
		return CustomParamsFireEvent;
		
	}
	
	public String evaluateAST(ASTNode currentASTNode) {
		String retStr = null;
		if(isLeafNode(currentASTNode)){
			retStr = evaluateLeafNode(currentASTNode);  
		}
		else{  // internal node with left and right children
			ASTNode.Type type_const = currentASTNode.getType();
			switch (type_const) {
				// arithmetic operators
			    case FUNCTION_ABS: retStr = Double.toString(Math.abs(Double.parseDouble(evaluateAST(currentASTNode.getChild(0))))); break;
			    case PLUS: retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) + Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case MINUS: retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) - Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case TIMES: retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) * Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case DIVIDE:retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) / Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case FUNCTION_POWER: retStr = Double.toString(Math.pow(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())), Double.parseDouble(evaluateAST(currentASTNode.getRightChild())))); break;
				// logical operators
				case LOGICAL_AND: retStr = Boolean.toString(Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild())) && Boolean.parseBoolean(evaluateAST(currentASTNode.getRightChild()))); break;
				case LOGICAL_OR:  retStr = Boolean.toString(Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild())) || Boolean.parseBoolean(evaluateAST(currentASTNode.getRightChild()))); break;
				case LOGICAL_NOT:  retStr = Boolean.toString(!Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild()))); break;
				case LOGICAL_XOR:  retStr = Boolean.toString(Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild())) ^ Boolean.parseBoolean(evaluateAST(currentASTNode.getRightChild()))); break;
				// relational operators
				// TODO EQ, NEQ can have boolean arguments
				case RELATIONAL_EQ:  
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) == Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case RELATIONAL_GEQ: 
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) >= Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case RELATIONAL_GT:  
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) > Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case RELATIONAL_LEQ: 
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) <= Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case RELATIONAL_LT:  
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) < Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case RELATIONAL_NEQ:  
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) != Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				// other operators
				case FUNCTION_PIECEWISE: {
					//Currently, the evaluator only accepts piecewise(1, arg1, 0), where arg1 is boolean
					if (currentASTNode.getChildCount() == 3) {
						if (Boolean.parseBoolean(evaluateAST(currentASTNode.getChild(1)))){
							retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getChild(0))));
						}
						else {
							retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getChild(2))));
						}		
					}
					else {
						JOptionPane.showMessageDialog(Gui.frame, "The piecewise function only accepts 3 children.",
									"Error in piecewise function", JOptionPane.ERROR_MESSAGE);
						break;
					}
					break;
				}
//				// probability distribution functions
				case FUNCTION: {
					String currNodeName = currentASTNode.getName();
					if (currNodeName.equals("uniform")) {
						double lowerBound = Math.min(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())), Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
						double upperBound = Math.max(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())), Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
						Random uniformCont = new Random(randSeed);
						double uniformRandNumber = uniformCont.nextDouble();
						uniformRandNumber = (upperBound - lowerBound)*uniformRandNumber + lowerBound;
						retStr = Double.toString(uniformRandNumber);
					}
					if (currNodeName.equals("exponential")) {
						double lambda = Double.parseDouble(evaluateAST(currentASTNode.getChild(0)));
						Random uniformCont = new Random(randSeed);
						double uniformRandNumber = uniformCont.nextDouble();
						double expRandNumber = -Math.log(uniformRandNumber)/lambda;
						retStr = Double.toString(expRandNumber);
					}
					break;
				}
				default: {
					System.out.println("Unimplemented function");
				}
				// TODO MOD, BITNOT, BITOR, BITAND, BITXOR, idiv
			}
		 }
		return retStr;
	}
	 
	public static boolean isLeafNode(ASTNode node){
		boolean ret = false;
		ret = node.isConstant() || node.isInteger() || node.isReal() || node.isName() || node.isRational();
		return ret;
	}
	 
	public String evaluateLeafNode(ASTNode currentASTNode){
		double nodeVal=0;
		String nodeValStr = null;
	    if(currentASTNode.isInteger()){
	    	nodeVal = currentASTNode.getInteger();
	    	nodeValStr = Double.toString(nodeVal);
	    }
	    else if(currentASTNode.isReal()){
	    	nodeVal = currentASTNode.getReal();
	    	nodeValStr = Double.toString(nodeVal);
	    }
	    else if(currentASTNode.isName()){
	    	if (SpeciesToIndex.containsKey(currentASTNode.getName())){
	    		nodeVal = SpeciesList.get(currentASTNode.getName());
	    		nodeValStr = Double.toString(nodeVal);
	    	}
	    	else if (GlobalParamsList.containsKey(currentASTNode.getName())){
	    		nodeVal = GlobalParamsList.get(currentASTNode.getName());
	    		nodeValStr = Double.toString(nodeVal);
	    	}
	    	else if (currentASTNode.getName().equals("t") || currentASTNode.getName().equals("time")) {
	    		nodeVal = time;
	    		nodeValStr = Double.toString(nodeVal);
	    	}
	     }
	    else if(currentASTNode.isConstant()) {
	    	if (currentASTNode.getType() == ASTNode.Type.CONSTANT_E || currentASTNode.getType() == ASTNode.Type.CONSTANT_PI) {
	    		nodeVal = currentASTNode.getReal();
	    		nodeValStr = Double.toString(nodeVal);
	    	}
	    	else if (currentASTNode.getType() == ASTNode.Type.CONSTANT_TRUE) {
	    		nodeValStr = Boolean.toString(true);
	    	}
	    	else if (currentASTNode.getType() == ASTNode.Type.CONSTANT_FALSE) {
	    		nodeValStr = Boolean.toString(false);
	    	}
	    	else {
	    		JOptionPane.showMessageDialog(Gui.frame, "Leaf node type is unknown.",
						"Error in parsing AST", JOptionPane.ERROR_MESSAGE);
	    	}
	    }
	    return nodeValStr;
	    
	}
	 
	
	public void InitializeEnoughMolecules(){  
		for (int i = 0; i < ReactionsToIndex.size(); i++){
			EnoughMolecules.put((String)ReactionsToIndex.keySet().toArray()[i], true);
		}
	}
	 
	public String[] openReactionInteractiveMenu(double t, double tau, int miu) {
		String[] CustomParamsReactionMenu = new String[3];
		JPanel tNextPanel = new JPanel();
		JPanel nextReactionsListPanel = new JPanel();
		JPanel mainPanel = new JPanel(new BorderLayout());
		tNextPanel.add(new JLabel("t_next:"));
		tNext = new JTextField(10);
		double t_next_deft = t + tau; 
		tNext.setText("" + t_next_deft);
		tNextPanel.add(tNext);
		nextReactionsListPanel.add(new JLabel("Next reaction:"));
		// l = number of possible reactions that can fire at t_next
		int l = 0;
		for (int i=0; i<NumReactions; i++){
			// Check if a reaction has enough molecules to fire
			 if (EnoughMolecules.get(IndexToReactions.get(i))){
				 l++;
			 }
		}
		// create a drop-down list of next possible firing reactions
		String[] nextReactionsArray = new String[l];
		if (EnoughMolecules.get(IndexToReactions.get(miu))){
			nextReactionsArray[0] = IndexToReactions.get(miu);
		}
		int k = 1;
		for (int i=0; i<NumReactions; i++){
			// Check if a reaction has enough molecules to fire
			if (EnoughMolecules.get(IndexToReactions.get(i)) && (i!=miu)){
				nextReactionsArray[k] = IndexToReactions.get(i);
				k++;
			} 
		}
		nextReactionsList = new JComboBox(nextReactionsArray);
		nextReactionsListPanel.add(nextReactionsList);
		mainPanel.add(tNextPanel, "North");
		mainPanel.add(nextReactionsListPanel, "Center");
		Object[] options = {"Step", "Run", "Terminate"};
		int optionValue = -1;
		optionValue = JOptionPane.showOptionDialog(Gui.frame, mainPanel, "Next Simulation Time",
		JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		CustomParamsReactionMenu[0]= tNext.getText().trim();
		CustomParamsReactionMenu[1]=(String) nextReactionsList.getSelectedItem();
		CustomParamsReactionMenu[2]="" + optionValue;
		return CustomParamsReactionMenu;
	}
	 
	public String[] openEventInteractiveMenu(ArrayList<EventQueueElement> eventReadyArray) {
		String[] CustomParamsEventMenu=new String[2];
		JPanel nextEventsPanel = new JPanel();
		JPanel nextEventFireTimePanel = new JPanel();
		JPanel mainPanel = new JPanel(new BorderLayout());
		nextEventsPanel.add(new JLabel("Next event to fire:"));
		// create a drop-down list of next possible firing events
		int sizeEventReadyArray = eventReadyArray.size();
		String[] nextEventsArray = new String[sizeEventReadyArray];
		Random generator = new Random();
		int eventToFireIndex = generator.nextInt(sizeEventReadyArray);
		nextEventsArray[0] = eventReadyArray.get(eventToFireIndex).getEventId();
		EventQueueElement eventToFire = eventReadyArray.remove(eventToFireIndex);	
		for (int i=1; i<sizeEventReadyArray; i++) {
			nextEventsArray[i] = eventReadyArray.get(i-1).getEventId();
		}
		eventReadyArray.add(eventToFireIndex, eventToFire);
		nextEventsList = new JComboBox(nextEventsArray);
		nextEventsPanel.add(nextEventsList);
		mainPanel.add(nextEventsPanel, "Center");
		Object[] options = {"OK", "Cancel", "Terminate"};
		Object[] singleOpt = {"OK"};
		int optionValue;
		// optionValue: 0=OK, 1=Cancel, 2=Terminate
		optionValue = JOptionPane.showOptionDialog(Gui.frame, mainPanel, "Next Event Selection",
		JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		CustomParamsEventMenu[0] = optionValue + "";
		String eventSelected = (String)nextEventsList.getSelectedItem();
		if (eventSelected.equals(eventToFire.getEventId())) { // user selected event is the same as the randomly selected one.
			nextEventFireTimePanel.add(new JLabel("The selected event will fire at time " + eventToFire.getScheduledTime() + "."));
			JOptionPane.showOptionDialog(Gui.frame, nextEventFireTimePanel, "Next Event Selection",
					JOptionPane.YES_OPTION, JOptionPane.PLAIN_MESSAGE, null, singleOpt, singleOpt[0]);
			CustomParamsEventMenu[1] = "" + eventReadyArray.indexOf(eventToFire);
			return CustomParamsEventMenu;
		}
		int eventSelectedIndex = -1;
		double eventSelectedScheduledTime = -1;
		for (int i = 0; i<sizeEventReadyArray; i++) {
			if (eventReadyArray.get(i).getEventId() == eventSelected) {
				eventSelectedIndex = i;
				eventSelectedScheduledTime = eventReadyArray.get(i).getScheduledTime();
				break;
			}
		}
		nextEventFireTimePanel.add(new JLabel("The selected event will fire at some time " + eventSelectedScheduledTime + "."));
		JOptionPane.showOptionDialog(Gui.frame, nextEventFireTimePanel, "Next Event Selection",
				JOptionPane.YES_OPTION, JOptionPane.PLAIN_MESSAGE, null, singleOpt, singleOpt[0]);
		CustomParamsEventMenu[1] = "" + eventSelectedIndex;
		return CustomParamsEventMenu;
	}
	
	private int openReactionDialogue() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel tNextPanel = new JPanel();
		tNextPanel.add(new JLabel("<html>The next simulation time will stop at " + nextTime + " in stead of " + nextReactionTime + " . <br></br>Would you like to enter a new next reaction time? </html>"));

		mainPanel.add(tNextPanel, "Center");
		Object[] options = {"Enter a new value", "Proceed with the current value"};
		int optValue;
		optValue = JOptionPane.showOptionDialog(Gui.frame, mainPanel, "Fire the current reaction?",
		JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		return optValue;
	}
	
	public String evaluatePropensityFunction(ASTNode currentASTNode, HashMap<String, Double>ListOfLocalParameters) {
		String retStr = null;
		if(isLeafNode(currentASTNode)){
			retStr = evaluatePropensityLeafNode(currentASTNode, ListOfLocalParameters); 
		}
		else{  // internal node with left and right children
			ASTNode.Type type_const = currentASTNode.getType();
			switch (type_const) {
				// arithmetic operators
				case PLUS: retStr = Double.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) + Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case MINUS: retStr = Double.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) - Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case TIMES: retStr = Double.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) * Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case DIVIDE:retStr = Double.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) / Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case FUNCTION_POWER: retStr = Double.toString(Math.pow(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)), Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters)))); break;
				// logical operators
				case LOGICAL_AND: retStr = Boolean.toString(Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) && Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case LOGICAL_OR:  retStr = Boolean.toString(Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) || Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case LOGICAL_NOT:  retStr = Boolean.toString(!Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters))); break;
				case LOGICAL_XOR:  retStr = Boolean.toString(Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) ^ Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				// relational operators
				// TODO EQ, NEQ can have boolean arguments
				case RELATIONAL_EQ:  retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) == Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case RELATIONAL_GEQ: retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) >= Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case RELATIONAL_GT:  retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) > Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case RELATIONAL_LEQ:  retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) <= Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case RELATIONAL_LT:  retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) < Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case RELATIONAL_NEQ:  retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) > Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				// other operators
				case FUNCTION_PIECEWISE: {
					//Currently, the evaluator only accepts piecewise(1, arg1, 0), where arg1 is boolean
					if (currentASTNode.getChildCount() == 3) {
						if (Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getChild(1),ListOfLocalParameters))){
							retStr = Double.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getChild(0),ListOfLocalParameters)));
						}
						else {
							retStr = Double.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getChild(2),ListOfLocalParameters)));
						}		
					}
					else {
						JOptionPane.showMessageDialog(Gui.frame, "The piecewise function only accepts 3 children.",
									"Error in piecewise function", JOptionPane.ERROR_MESSAGE);
					}
					break;
				}
//				// probability distribution functions
				case FUNCTION: {
					String currNodeName = currentASTNode.getName();
					if (currNodeName.equals("uniform")) {
						double lowerBound = Math.min(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)), Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters)));
						double upperBound = Math.max(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)), Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters)));
						Random uniformCont = new Random(randSeed);
						double uniformRandNumber = uniformCont.nextDouble();
						uniformRandNumber = (upperBound - lowerBound)*uniformRandNumber + lowerBound;
						retStr = Double.toString(uniformRandNumber);
					}
					if (currNodeName.equals("exponential")) {
						double lambda = Double.parseDouble(evaluatePropensityFunction(currentASTNode.getChild(0), ListOfLocalParameters));
						Random uniformCont = new Random(randSeed);
						double uniformRandNumber = uniformCont.nextDouble();
						double expRandNumber = -Math.log(uniformRandNumber)/lambda;
						retStr = Double.toString(expRandNumber);
					}
					break;
				}
				default: {
					System.out.println("Unimplemented function");
				}
				// TODO MOD, BITNOT, BITOR, BITAND, BITXOR, idiv
			}
		 }
		return retStr;
	}          
	 
	public String evaluatePropensityLeafNode(ASTNode currentASTNode, HashMap<String, Double> currentListOfLocalParams){		
		double node_val=0;
	    if(currentASTNode.isInteger()){
	    	node_val = currentASTNode.getInteger();
	    }
	    else if(currentASTNode.isReal()){
	    	node_val = currentASTNode.getReal();
	    }
	    else if(currentASTNode.isName()){
	    	if (SpeciesToIndex.containsKey(currentASTNode.getName())){
	    		node_val = SpeciesList.get(currentASTNode.getName());
	    	}
	    	else if (GlobalParamsList.containsKey(currentASTNode.getName()) & !currentListOfLocalParams.containsKey(currentASTNode.getName())){
	    		node_val = GlobalParamsList.get(currentASTNode.getName());
	    	}
	    	else if (currentListOfLocalParams.containsKey(currentASTNode.getName())){
	    		node_val =currentListOfLocalParams.get(currentASTNode.getName());
	    	}
	     }
	    return Double.toString(node_val);
	}
}
