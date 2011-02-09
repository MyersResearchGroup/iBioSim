package gillespieSSAjava;

import graph.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;

import main.Gui;

import org.sbml.libsbml.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.*;
import java.lang.Math;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

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
	private HashMap<String, HashMap<String, Double>> AtTriggerTimeList = new HashMap<String, HashMap<String, Double>>();	
	// static HashMap<String, Double> GlobalParamsChangeList = new HashMap<String, Double>();
	// static HashMap<String, Double> CompartmentList = new HashMap<String, Double>();
	private HashMap<String, Double> PropensityFunctionList = new HashMap<String, Double>();
	private HashMap<String, Boolean> EnoughMolecules = new HashMap<String, Boolean>();
	private String SpeciesID;
	private String GlobalParamID;
	// private String CompartmentID;
	private double SpeciesInitAmount;
	private double GlobalParamValue;
	// private double ComparmentSize;
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
	private double t_next = 0;
	private double maxTime = 0;
	private double nextEventTime = 0;
	private int NumIrreversible = 0;
	private int NumReversible = 0;
	private int NumReactions = 0;
	private FileOutputStream output;
	private PrintStream outTSD;
	private double runUntil = 0;
	private boolean[] prevTriggerValArray = null;
	boolean eventDelayNegative = false;
	
	public GillespieSSAJavaSingleStep() {
	}
	public void PerformSim (String SBMLFileName,String outDir, double timeLimit, double timeStep, Graph graph) throws FileNotFoundException{  
	 int optionValue = -1;
	 //  System.out.println("outDir = " + outDir);
	 String outTSDName = outDir + "/run-1.tsd";
	 output = new FileOutputStream(outTSDName);
	 outTSD = new PrintStream(output);
	 SBMLReader reader = new SBMLReader();
	 SBMLDocument document = reader.readSBML(SBMLFileName);
	 Model model = document.getModel();
	 outTSD.print("(");
	 for (int i=0; i < model.getNumReactions(); i++){
		 Reaction reaction = model.getReaction(i);
		 if (reaction.getReversible())
			 NumReversible ++;
		 else
			 NumIrreversible ++;
	 }
	 NumReactions = 2*NumReversible + NumIrreversible;
	 StateChangeVector=new double[(int) NumReactions][(int) model.getNumSpecies()];
	 
	//---------Gillespie's SSA---------
	// 1. Initialize time (t) and states (x) 
	 time = 0.0;
	 // get the species and the associated initial values
	 for (int i=0;i<model.getNumSpecies();i++){
		 SpeciesID= model.getListOfSpecies().get(i).getId();
		 SpeciesInitAmount = model.getListOfSpecies().get(i).getInitialAmount();
		 SpeciesList.put(SpeciesID, SpeciesInitAmount);
		 SpeciesToIndex.put(SpeciesID,i);
		 IndexToSpecies.put(i,SpeciesID); 
	 }  	 
	 // get the global parameters
	 for (int i=0;i<model.getNumParameters();i++){
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
			 for (int i=0;i<GlobalParamsList.size();i++){
				 if (i<GlobalParamsList.size()-1)
					 outTSD.print("\"" + GlobalParamsList.keySet().toArray()[i] + "\"" + ",");
				 else
					 outTSD.print("\"" + GlobalParamsList.keySet().toArray()[i] + "\"");
			 }
		 }
		 outTSD.print("),");
		 // print the initial values
		 outTSD.print("(" + time + ", ");
		 for (int i=0;i<SpeciesList.size();i++){
			 if (i<SpeciesList.size()-1)
				 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]) + ", ");
			 else
				 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]));
		 }
		 for (int i=0;i<GlobalParamsList.size();i++){
			 if (i<GlobalParamsList.size()-1)
				 outTSD.print(GlobalParamsList.get(GlobalParamsList.keySet().toArray()[i]) + ", ");
			 else
				 outTSD.print(GlobalParamsList.get(GlobalParamsList.keySet().toArray()[i]));
		 }
		 outTSD.print("),");
	 }

	 //Currently, we assume only one compartment in one SBML file
	 // get compartments and their sizes. 
	 //  if (model.getNumCompartments() !=0){
	 //  for (int i=0;i<model.getNumCompartments();i++){
	 //  CompartmentID = model.getListOfCompartments().get(i).getId();
	 //  ComparmentSize = model.getListOfCompartments().get(i).getSize();
	 ////  CompartmentList.put(CompartmentID, ComparmentSize);
	 ////  System.out.println("Compartment " + i + "=" + CompartmentID);
	 ////  System.out.println("CompartmentSize = " + ComparmentSize);
	 //  }
	 //  }
	 
	 // initialize state change vector
	 int index = 0;
	 int l = 0;
	 while (index<NumReactions){
		 Reaction currentReaction = model.getListOfReactions().get(l);
		 // System.out.println("currentReaction = " + currentReaction.getId());
		 // irreversible reaction
		 if (!currentReaction.getReversible()){
			 String currentReactionID = currentReaction.getId();
			 ReactionsToIndex.put(currentReactionID, index);
			 IndexToReactions.put(index, currentReactionID);
			 for (int j=0; j < currentReaction.getNumReactants(); j++){
				 String SpeciesAsReactant = currentReaction.getReactant(j).getSpecies();
				 StateChangeVector[index][SpeciesToIndex.get(SpeciesAsReactant)] = -currentReaction.getReactant(j).getStoichiometry();
			 }  
			 for (int j=0; j < currentReaction.getNumProducts(); j++){
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
			 for (int j=0; j < currentReaction.getNumReactants(); j++){
				 String SpeciesAsReactant = currentReaction.getReactant(j).getSpecies();
				 StateChangeVector[index][SpeciesToIndex.get(SpeciesAsReactant)] = -currentReaction.getReactant(j).getStoichiometry();
				 StateChangeVector[index+1][SpeciesToIndex.get(SpeciesAsReactant)] = currentReaction.getReactant(j).getStoichiometry();
			 }  
			 for (int j=0; j < currentReaction.getNumProducts(); j++){
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
	 ListOfEvents eventList = null;
	 PriorityQueue<EventQueueElement> eventQueue = null;
	 int initEventQueueCap = 20;
	 if (model.getListOfEvents().size() >0) {
		  eventList = model.getListOfEvents();
		  prevTriggerValArray = new boolean[(int) eventList.size()];
		  for(int i=0; i< eventList.size();i++){
			  Event event = eventList.get(i); 
			  Trigger trigger = event.getTrigger();
//			  if (event.getUseValuesFromTriggerTime()) {
//				  JOptionPane.showMessageDialog(Gui.frame, "The simulator does not currently support UseValuesFromTriggerTime.",
//							"Error in trigger", JOptionPane.ERROR_MESSAGE);
//				  break;
//			  }
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
	//  // Create a table to hold the results
	//  DefaultTableModel tableModel = new DefaultTableModel();
	//  tableModel.addColumn("t");
	//  tableModel.addColumn("tau");
	//  tableModel.addColumn("Next Reaction");
	//  SimResultsTable simResultsTbl = new SimResultsTable(tableModel);
	//  JFrame tableFrame = new JFrame("Simulation Results");
	//  simResultsTbl.showTable(tableFrame, simResultsTbl);
	//  ########################################################
	 double deltaTime = 0.01; // deltaTime is used to add to timeLimit, so that the while loop can terminate
	 while (time<=timeLimit) {
		  // 2. Update and fire events; evaluate propensity functions
		  if (timeStep == Double.MAX_VALUE) {
			  maxTime = timeLimit + deltaTime;
		  }
		  else {
			  maxTime = maxTime + timeStep;
		  }
		  evaluateRules(model);
		  if (model.getListOfEvents().size() >0) { 
			  do {
				  nextEventTime = updateEventQueue(eventList, eventQueue); // if (eventQueue is empty), then nextEventTime = -1
				  if (!eventDelayNegative && eventQueue.size() > 0) {
					  boolean eventFired = fireEvent(eventQueue, initEventQueueCap, eventList);
					  if (eventFired) {
						  evaluateRules(model);
						  // TODO evaluate algebraic and rate rules and constraints
						  nextEventTime = updateEventQueue(eventList, eventQueue);
					  }  
				  } 
			  }
			  while (eventQueue.size() > 0 && time == eventQueue.peek().getScheduledTime());
		  }
		  if (eventDelayNegative) {
			  JOptionPane.showMessageDialog(Gui.frame, "Delay expression evaluates to a negative number.",
						"Error in piecewise function", JOptionPane.ERROR_MESSAGE);
			  break;
		  }
		  if (nextEventTime < maxTime && nextEventTime>0) {
			  maxTime = nextEventTime;
		  }
		  
		  // TODO evaluate rules, constraints
		  
		  // Evaluate propensity functions
		  InitializeEnoughMolecules();
		  PropensitySum = 0.0;
		  // get the reactions  
		  for (int i=0;i<model.getNumReactions();i++){
			  Reaction currentReaction = model.getListOfReactions().get(i);
			  //outTXT.println("Reactions" + i + ": " + currentReaction.getId());  
			  boolean ModifierNotEmpty = true;
			  if (currentReaction.getNumModifiers()>0){
				  for (int k=0; k<currentReaction.getNumModifiers();k++){
					  if (SpeciesList.get(currentReaction.getModifier(k).getSpecies())==0.0){
						  ModifierNotEmpty = false;
						  //break;
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
			  ListOfLocalParameters currentListOfLocalParams = currentKineticLaw.getListOfLocalParameters();
			  HashMap<String, Double> LocalParamsList = new HashMap<String, Double>(); 
			  if (currentListOfLocalParams.size() > 0){  
				 for (int j=0; j<currentListOfLocalParams.size(); j++){
					 LocalParamsList.put(currentListOfLocalParams.get(j).getId(), currentListOfLocalParams.get(j).getValue());  
					//System.out.println("Local Param " + currentListOfLocalParams.get(j).getId()+ " = " + currentListOfLocalParams.get(j).getValue());
				 }
			  }
			  // calculate propensity function.
			  // For irreversible reaction, propensity function = kinetic law
			  if (!currentReaction.getReversible()){
				  // enzymatic reaction with no reactants
				  if (currentReaction.getNumReactants() == 0 && currentReaction.getNumProducts()>0 && currentReaction.getNumModifiers()>0){
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
				  if (currentReaction.getNumReactants() > 0){ 
				    for (int j=0; j < currentReaction.getNumReactants(); j++){
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
				  for (int j=0; j < currentReaction.getNumReactants(); j++){
					  // not enough reactant Molecules
					  boolean EnoughMoleculesCondFW = SpeciesList.get(currentReaction.getReactant(j).getSpecies()) >= currentReaction.getReactant(j).getStoichiometry();
					  if(!EnoughMoleculesCondFW){
						  PropensityFunctionValueFW = 0;
						  EnoughMolecules.put(currentReactionID, false);      
						  break;
					  }
				  }
				  if (EnoughMolecules.get(currentReactionID)) {
	//			  	System.out.println("FW current AST Node = " + currentASTNode.getType());
	//			    System.out.println("FW current left child = " + currentASTNode.getLeftChild().getType());
				    PropensityFunctionValueFW = Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), LocalParamsList));
	//			    System.out.println("PropensityFunctionValueFW = " + PropensityFunctionValueFW);
				  }
			      PropensitySum = PropensitySum + PropensityFunctionValueFW;
			      PropensityFunctionList.put(currentReactionID, PropensityFunctionValueFW);
			    
			      // Evaluate kinetic law for the reverse reaction
			      // Check that there are enough Molecules for the reaction to happen
			      for (int j=0; j < currentReaction.getNumProducts(); j++){
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
			 if (SpeciesList.size() > 0 || GlobalParamsList.size() > 0) {
				 outTSD.print("(" + time + ", ");
				 if (SpeciesList.size() > 0) {
					 for (int i=0;i<SpeciesList.size();i++){
						 if (i<SpeciesList.size()-1)
							 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]) + ", ");
						 else
							 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]));
					 }
				 }
				 if (GlobalParamsList.size()>0) {
					 for (int i=0; i < GlobalParamsList.size();i++){
						 if (i<GlobalParamsList.size()-1)
							 outTSD.print(GlobalParamsList.get(GlobalParamsList.keySet().toArray()[i]) + ", ");
						 else
							 outTSD.print(GlobalParamsList.get(GlobalParamsList.keySet().toArray()[i]));
					 }
				 }
				 outTSD.print("),");
			 }
			 graph.refresh();
			 time = maxTime;
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
		 // Determine randomly the time, tau, until the next reaction. 
		 tau = (1/PropensitySum)*Math.log(1/r1);  
		 
		 // 5. Determine the next reaction: (miu is the row index of state change vector array)
		 double SumLeft = 0.0;
		 int count;
		 double r2 = generator.nextDouble();
		 for (count=0; SumLeft <= r2*PropensitySum; count++){
			 SumLeft = SumLeft + PropensityFunctionList.get(IndexToReactions.get(count));
			 if (SumLeft > r2*PropensitySum) break;
		 }
		 miu = count;
		 
		 // Pop up the interactive menu and asks the user to specify tau and miu
		 String[] CustomParams=new String[3];
		 // optionValue: 0=step, 1=run, 2=terminate
		 boolean hasRunModeStarted = false;
		 boolean isRunMode = (optionValue == 1) && time < runUntil;
		 if (!isRunMode) {
			 CustomParams = openReactionInteractiveMenu(time,tau,miu);
			 t_next = Double.parseDouble(CustomParams[0]);
			 while (t_next < time){
				 JOptionPane.showMessageDialog(Gui.frame, "The value of t_next needs to be greater than current time",
				"Error in next simulation time", JOptionPane.ERROR_MESSAGE);
				 CustomParams = openReactionInteractiveMenu(time,tau,miu);
				 t_next = Double.parseDouble(CustomParams[0]);
			 }  
			 optionValue = Integer.parseInt(CustomParams[2]);
			 if (optionValue == 0) {
				 tau = t_next - time;
				 miu = ReactionsToIndex.get(CustomParams[1]);  
			 }
			 else if(optionValue == 1 && time >= runUntil){
				 runUntil = Double.parseDouble(CustomParams[0]);
				 miu = ReactionsToIndex.get(CustomParams[1]);
				 hasRunModeStarted = true;
				// ********************(obsolete) Create another pop-up window to specify the time to run ******************
				//  String[] CustomParamsRun = openRunMenu();
				//  int runUntil_optVal = Integer.parseInt(CustomParamsRun[1]);
				//  // runUntil_optVal: 0=Run, 1=Cancel
				//  if (runUntil_optVal == 0) {
				//  runUntil = t + Double.parseDouble(CustomParamsRun[0]);
				//  hasRunModeStarted = true;
				//  }
				//  else { // runUntil_optVal == 1 (Cancel)
				//  continue;
				//  }
				// ****************************************************************************************************
			 }
			 else {
				 break;
			 }
		 }
		 
		 // 6. Determine the new state: t = t + tau and x = x + v[miu]
		 double t_current = time;
		 if (maxTime <= time+tau)
			 time = maxTime;
		 else
			 time = time + tau;
		 // Determine the next reaction to fire, in row miu of StateChangeVector.
		 // Update the species amounts according to the state-change-vector in row miu. 
		 for (int i=0; i < model.getNumSpecies(); i++){
			if (StateChangeVector[miu][i]!=0){
				String SpeciesToUpdate = IndexToSpecies.get(i);
				// System.out.println("SpeciesToUpdate = " + SpeciesToUpdate);
				if (EnoughMolecules.get(IndexToReactions.get(miu))){
					double SpeciesToUpdateAmount = SpeciesList.get(SpeciesToUpdate) + StateChangeVector[miu][i];
					SpeciesList.put(SpeciesToUpdate, SpeciesToUpdateAmount);
				} 
			 }
		 }
		 //  System.out.println("t_current = " + t_current);
		 //  System.out.println("t = " + t);
		 //  System.out.println("t_next = " + t_next);
		 ////  System.out.println("tau = " + tau);
		 ////  System.out.println("miu = " + miu);
		 ////  System.out.println("Next reaction is " + IndexToReactions.get(miu));
		 //  System.out.println("*****************************************");
		 
		 //  // Print results to a table and display it. 
		 //  if ((!isRunMode && !hasRunModeStarted) || (isRunMode && t >= runUntil)){
		 //  tableModel.addRow(new Object[]{t_current, tau, IndexToReactions.get(miu)});
		 //  simResultsTbl.showTable(tableFrame, simResultsTbl);
		 //  }
		 if (SpeciesList.size() > 0 || GlobalParamsList.size() > 0) {
			 outTSD.print("(" + time + ", ");
			 if (SpeciesList.size() > 0) {
				 for (int i=0;i<SpeciesList.size();i++){
					 if (i<SpeciesList.size()-1)
						 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]) + ", ");
					 else
						 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]));
				 }
			 }
			 if (GlobalParamsList.size()>0) {
				 for (int i=0; i < GlobalParamsList.size();i++){
					 if (i<GlobalParamsList.size()-1)
						 outTSD.print(GlobalParamsList.get(GlobalParamsList.keySet().toArray()[i]) + ", ");
					 else
						 outTSD.print(GlobalParamsList.get(GlobalParamsList.keySet().toArray()[i]));
				 }
			 }
			 outTSD.print("),");
		 }
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
	
	private void evaluateRules(Model model) {
		ListOfRules ruleList= null;
		 if (model.getListOfRules().size()>0) {
			 ruleList = model.getListOfRules();
			 for (int i=0; i<ruleList.size(); i++){
				 Rule currRule = ruleList.get(i);
				 if (currRule.isAssignment()) {
					 double assignment = Double.parseDouble(evaluateAST(currRule.getMath().getListOfNodes().get(0)));
					 String variable = currRule.getVariable();
					 if(SpeciesList.containsKey(variable)) {
						SpeciesList.put(variable, assignment);
					 }
					 else if(GlobalParamsList.containsKey(variable)){
						GlobalParamsList.put(variable, assignment);
					 }
					 System.out.println("evaluateRules: variable = " + variable);
					 System.out.println("evaluateRules: assignment = " + assignment);
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
	
	public double updateEventQueue(ListOfEvents eventList, PriorityQueue<EventQueueElement> eventQueue){
	 ArrayList<String> eventToRemoveArray = new ArrayList<String>();
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
				  ListOfEventAssignments eventToFireAssignList = currEvent.getListOfEventAssignments();
				  for (int j = 0; j < eventToFireAssignList.size(); j ++){
					  double assignment = Double.parseDouble(evaluateAST(eventToFireAssignList.get(j).getMath().getListOfNodes().get(0)));
					  String variable = eventToFireAssignList.get(j).getVariable();
					  HashMap<String, Double> assignMap = new HashMap<String, Double>();
					  assignMap.put(variable, assignment);
					  AtTriggerTimeList.put(currEvent.getId(), assignMap);
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
			  if (eventQueueContainsEvent && !trigger.getPersistent()) { 
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
//				 System.out.println("eventQueueIterator.hasNext() = " + eventQueueIterator.hasNext());
//				 System.out.println("eventQueueIterator.next() = " + eventQueueIterator.next().getEventId());
				 EventQueueElement currEventQueueElement = eventQueueIterator.next();
			 	 if (eventToRemove.equals(currEventQueueElement.getEventId())) {
//			 		 eventQueue.remove(currEventQueueElement);
			 		 eventQueueElemToRemoveArray.add(currEventQueueElement);		 		 
			 	 }
			 }
		 } 
		 for (int j = 0; j < eventQueueElemToRemoveArray.size(); j++) {
			 eventQueue.remove(eventQueueElemToRemoveArray.get(j));
		 }
		 eventToRemoveArray.clear();
	 }
	 
	 if (!eventQueue.isEmpty()) {
		 return eventQueue.peek().getScheduledTime();
	 }
	 else {
		 return -1;
	 }
	 
	}
	
	private boolean fireEvent(PriorityQueue<EventQueueElement> eventQueue, int initEventQueueCap, ListOfEvents eventList) {
		// Assume event assignments are evaluated at fire time
		boolean isEventFired = false;
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
			int eventToFireIndex = openEventInteractiveMenu(eventReadyArray);
			Event eventToFire = eventList.get(eventReadyArray.get(eventToFireIndex).getEventId());
			if (!eventToFire.getUseValuesFromTriggerTime()) {
				ListOfEventAssignments eventToFireAssignList = eventToFire.getListOfEventAssignments();
				for (int i = 0; i < eventToFireAssignList.size(); i ++){
					double assignment = Double.parseDouble(evaluateAST(eventToFireAssignList.get(i).getMath().getListOfNodes().get(0)));
					String variable = eventToFireAssignList.get(i).getVariable();
					if(SpeciesList.containsKey(variable)) {
						SpeciesList.put(variable, assignment);
					}
					else if(GlobalParamsList.containsKey(variable)){
						GlobalParamsList.put(variable, assignment);
					}
				}
			}
			else {  // UseValuesFromTriggerTime
				HashMap<String, Double> varMap =  AtTriggerTimeList.get(eventToFire.getId());				
				for (Iterator<String> varMapIterator = varMap.keySet().iterator(); varMapIterator.hasNext();) {
					String currVarMapId = varMapIterator.next();
					Double currVarMapVal = varMap.get(currVarMapId);
					if(SpeciesList.containsKey(currVarMapId)) {
						SpeciesList.put(currVarMapId, currVarMapVal);
					}
					else if(GlobalParamsList.containsKey(currVarMapId)){
						GlobalParamsList.put(currVarMapId, currVarMapVal);
					}
				}
				
			}
			// TODO need to test multiple events
			// If multiple events were selected, put the all events in eventsReadyArray (except eventToFire) back to the event queue.
			for (Iterator<EventQueueElement> eventQueueIterator = eventQueue.iterator();eventQueueIterator.hasNext();) {
				EventQueueElement currEventQueueElement = eventQueueIterator.next();
				if (eventToFire.getId().equals(currEventQueueElement.getEventId())) {
					eventReadyArray.remove(currEventQueueElement);
				}
			}
			for (int i = 0; i < eventReadyArray.size() - 1; i++){
				eventQueue.add(eventReadyArray.get(i));
			}
			eventReadyArray.clear();
		}
		return isEventFired;
		
	}
	
	private String evaluateAST(ASTNode currentASTNode) {
		String retStr = null;
		if(isLeafNode(currentASTNode)){
			retStr = evaluateLeafNode(currentASTNode);  
		}
		else{  // internal node with left and right children
			int type_const=currentASTNode.getType();
			switch (type_const) {
				// arithmetic operators
				case libsbml.AST_PLUS: retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) + Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case libsbml.AST_MINUS: retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) - Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case libsbml.AST_TIMES: retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) * Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case libsbml.AST_DIVIDE:retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) / Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case libsbml.AST_FUNCTION_POWER: retStr = Double.toString(Math.pow(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())), Double.parseDouble(evaluateAST(currentASTNode.getRightChild())))); break;
				// logical operators
				case libsbml.AST_LOGICAL_AND: retStr = Boolean.toString(Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild())) && Boolean.parseBoolean(evaluateAST(currentASTNode.getRightChild()))); break;
				case libsbml.AST_LOGICAL_OR:  retStr = Boolean.toString(Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild())) || Boolean.parseBoolean(evaluateAST(currentASTNode.getRightChild()))); break;
				case libsbml.AST_LOGICAL_NOT:  retStr = Boolean.toString(!Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild()))); break;
				case libsbml.AST_LOGICAL_XOR:  retStr = Boolean.toString(Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild())) ^ Boolean.parseBoolean(evaluateAST(currentASTNode.getRightChild()))); break;
				// relational operators
				// TODO EQ, NEQ can have boolean arguments
				case libsbml.AST_RELATIONAL_EQ:  
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) == Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case libsbml.AST_RELATIONAL_GEQ: 
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) >= Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case libsbml.AST_RELATIONAL_GT:  
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) > Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case libsbml.AST_RELATIONAL_LEQ: 
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) <= Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case libsbml.AST_RELATIONAL_LT:  
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) < Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				case libsbml.AST_RELATIONAL_NEQ:  
					retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) != Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))); break;
				// other operators
				case libsbml.AST_FUNCTION_PIECEWISE: {
					//Currently, the evaluator only accepts piecewise(1, arg1, 0), where arg1 is boolean
					if (currentASTNode.getNumChildren() == 3) {
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
				// TODO MOD, BITNOT, BITOR, BITAND, BITXOR, idiv
			}
		 }
		return retStr;
	}
	 
	public boolean isLeafNode(ASTNode node){
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
	    	else if (currentASTNode.getName().equals("t")) {
	    		nodeVal = time;
	    		nodeValStr = Double.toString(nodeVal);
	    	}
	     }
	    else if(currentASTNode.isConstant()) {
	    	if (currentASTNode.getType() == libsbml.AST_CONSTANT_E || currentASTNode.getType() == libsbml.AST_CONSTANT_PI) {
	    		nodeVal = currentASTNode.getReal();
	    		nodeValStr = Double.toString(nodeVal);
	    	}
	    	else if (currentASTNode.getType() == libsbml.AST_CONSTANT_TRUE) {
	    		nodeValStr = Boolean.toString(true);
	    	}
	    	else if (currentASTNode.getType() == libsbml.AST_CONSTANT_FALSE) {
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
		String[] tNext_miu_optVal = new String[3];
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
		int optionValue;
		optionValue = JOptionPane.showOptionDialog(Gui.frame, mainPanel, "Next Simulation Time",
		JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		tNext_miu_optVal[0]= tNext.getText().trim();
		tNext_miu_optVal[1]=(String) nextReactionsList.getSelectedItem();
		tNext_miu_optVal[2]="" + optionValue;
		return tNext_miu_optVal;
	}
	 
	// public String[] openRunMenu(){
	// int optlVal;
	// String[] runTimeLimit_optVal = new String[2];
	// do {
	// JPanel runTimeLimitPanel = new JPanel();
	// runTimeLimitPanel.add(new JLabel("Time limit to run:"));
	// JTextField runTimeLimit = new JTextField(10);
	// runTimeLimitPanel.add(runTimeLimit);
	// Object[] options = {"Run", "Cancel"};
	// optlVal = JOptionPane.showOptionDialog(BioSim.frame, runTimeLimitPanel, "Specify Run Time Limit",
	// JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	// runTimeLimit_optVal[0] = runTimeLimit.getText().trim();
	// runTimeLimit_optVal[1] = "" + optlVal;
	//// System.out.println("runUntil_optVal[0] = " +  runUntil_optVal[0]);
	// if (optlVal == 0 && runTimeLimit_optVal[0].equals("")) {
	//  JOptionPane.showMessageDialog(BioSim.frame, "Please specify a time limit.",
	// "Error in Run", JOptionPane.ERROR_MESSAGE);  
	// }
	// if (optlVal == 1) {
	// break;
	// }
	// }
	// while (optlVal == 0 && runTimeLimit_optVal[0].equals(""));
	// return runTimeLimit_optVal; 
	// }
	 
	public int openEventInteractiveMenu(ArrayList<EventQueueElement> eventReadyArray) {
		JPanel nextEventsPanel = new JPanel();
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
		Object[] options = {"OK", "Cancel"};
//		int optionValue;
		// optionValue: 0=OK, 1=Cancel 
		JOptionPane.showOptionDialog(Gui.frame, mainPanel, "Next Event Selection",
		JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		String eventSelected = (String)nextEventsList.getSelectedItem();
		if (eventSelected.equals(eventToFire.getEventId()))
			return eventReadyArray.indexOf(eventToFire);
		else {
			int eventSelectedIndex = -1;
			for (int i = 0; i<sizeEventReadyArray; i++) {
				if (eventReadyArray.get(i).getEventId() == eventSelected) {
					 eventSelectedIndex = i;
					 break;
				}
			}
			return eventSelectedIndex;
		}
	}
	
	public String evaluatePropensityFunction(ASTNode currentASTNode, HashMap<String, Double>ListOfLocalParameters) {
		String retStr = null;
		if(isLeafNode(currentASTNode)){
			retStr = evaluatePropensityLeafNode(currentASTNode, ListOfLocalParameters); 
		}
		else{  // internal node with left and right children
			int type_const=currentASTNode.getType();
			switch (type_const) {
				// arithmetic operators
				case libsbml.AST_PLUS: retStr = Double.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) + Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case libsbml.AST_MINUS: retStr = Double.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) - Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case libsbml.AST_TIMES: retStr = Double.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) * Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case libsbml.AST_DIVIDE:retStr = Double.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) / Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case libsbml.AST_FUNCTION_POWER: retStr = Double.toString(Math.pow(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)), Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters)))); break;
				// logical operators
				case libsbml.AST_LOGICAL_AND: retStr = Boolean.toString(Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) && Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case libsbml.AST_LOGICAL_OR:  retStr = Boolean.toString(Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) || Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case libsbml.AST_LOGICAL_NOT:  retStr = Boolean.toString(!Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters))); break;
				case libsbml.AST_LOGICAL_XOR:  retStr = Boolean.toString(Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) ^ Boolean.parseBoolean(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				// relational operators
				// TODO EQ, NEQ can have boolean arguments
				case libsbml.AST_RELATIONAL_EQ:  retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) == Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case libsbml.AST_RELATIONAL_GEQ: retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) >= Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case libsbml.AST_RELATIONAL_GT:  retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) > Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case libsbml.AST_RELATIONAL_LEQ:  retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) <= Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case libsbml.AST_RELATIONAL_LT:  retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) < Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				case libsbml.AST_RELATIONAL_NEQ:  retStr = Boolean.toString(Double.parseDouble(evaluatePropensityFunction(currentASTNode.getLeftChild(), ListOfLocalParameters)) > Double.parseDouble(evaluatePropensityFunction(currentASTNode.getRightChild(), ListOfLocalParameters))); break;
				// other operators
				case libsbml.AST_FUNCTION_PIECEWISE: {
					//Currently, the evaluator only accepts piecewise(1, arg1, 0), where arg1 is boolean
					if (currentASTNode.getNumChildren() == 3) {
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

