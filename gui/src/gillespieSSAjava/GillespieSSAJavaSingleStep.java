package gillespieSSAjava;

import graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.sbml.libsbml.*;

import biomodelsim.BioSim;
import java.awt.BorderLayout;
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
//  for (int i=0;i<SpeciesList.size();i++){
//  System.out.println(SpeciesList.keySet().toArray()[i] + " = " + SpeciesList.get(SpeciesList.keySet().toArray()[i]));
//  } 
 outTSD.print("(\"time\",");
 for (int i=0;i<SpeciesList.size();i++){
 if (i<SpeciesList.size()-1)
 outTSD.print("\"" + SpeciesList.keySet().toArray()[i] + "\"" + ",");
 else
 outTSD.print("\"" + SpeciesList.keySet().toArray()[i] + "\"),");
 }
 outTSD.print("(" + time + ", ");
 for (int i=0;i<SpeciesList.size();i++){
 if (i<SpeciesList.size()-1)
 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]) + ", ");
 else
 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]) + "),");
 }
 
 // get the global parameters
//  System.out.println("GlobalParameters:");
 if(model.getNumParameters() != 0){
 for (int i=0;i<model.getNumParameters();i++){
 GlobalParamID= model.getListOfParameters().get(i).getId();
 GlobalParamValue = model.getListOfParameters().get(i).getValue();
 GlobalParamsList.put(GlobalParamID,GlobalParamValue);
//  GlobalParamsChangeList.put(GlobalParamID, (double) 0);
 }
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
//  System.out.println("currentReaction = " + currentReaction.getId());
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
 else{ // reversible reaction
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
 
//  // Create the event queue
//  ListOfEvents eventList = model.getListOfEvents();
////  HashMap<String, ArrayList<ArrayList<Double>>>
//  for(int i=0; i< eventList.size();i++){
//  Event event = eventList.get(i); 
//  Trigger trigger = event.getTrigger();
//  ASTNode triggerTopASTNode = trigger.getMath().getListOfNodes().get(0);
//  boolean currTriggerVal = Boolean.parseBoolean(evaluateAST(triggerTopASTNode));
////  if (!prevTriggerVal && currTriggerVal) {
////  ASTNode currDelayTopASTNode = event.getDelay().getMath();
////  double delayVal = Double.parseDouble(evaluateAST(currDelayTopASTNode));
////  }
//  
//  
// }
 
 graph.editGraph();
//  // Create a table to hold the results
//  DefaultTableModel tableModel = new DefaultTableModel();
//  tableModel.addColumn("t");
//  tableModel.addColumn("tau");
//  tableModel.addColumn("Next Reaction");
//  SimResultsTable simResultsTbl = new SimResultsTable(tableModel);
//  JFrame tableFrame = new JFrame("Simulation Results");
//  simResultsTbl.showTable(tableFrame, simResultsTbl);
//        ########################################################
 while (time<=timeLimit) {
//  //####### fire/update events #######
//  if (timeStep == Double.MAX_VALUE) {
//  maxTime = Double.MAX_VALUE;
//  }
//  else {
//  maxTime = maxTime + timeStep;
//  }
//  nextEventTime = fireEvents(time, eventList);
//  // TODO
////  if (nextEventTime==-2.0) {
////  return FAILING;
////  }
//  if ((nextEventTime != -1) && (nextEventTime < maxTime)) {
//  maxTime = nextEventTime;
//  }
 //#################################
 // 2. Evaluate propensity functions
 InitializeEnoughMolecules();
 PropensitySum = 0.0;
     // get the reactions  
 for (int i=0;i<model.getNumReactions();i++){
 Reaction currentReaction = model.getListOfReactions().get(i);
//  outTXT.println("Reactions" + i + ": " + currentReaction.getId());  
     boolean ModifierNotEmpty = true;
     if (currentReaction.getNumModifiers()>0){
     for (int k=0; k<currentReaction.getNumModifiers();k++){
 if (SpeciesList.get(currentReaction.getModifier(k).getSpecies())==0.0){
     ModifierNotEmpty = false;
//      break;
     }
     }
     }
 String currentReactionID = currentReaction.getId();
//  System.out.println("Reaction" + i + ": " + currentReactionID);
 // get current kinetic law
 KineticLaw currentKineticLaw = currentReaction.getKineticLaw();
//  System.out.println("currentKineticLaw= " + currentKineticLaw.getFormula());
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
//  System.out.println("Local Param " + currentListOfLocalParams.get(j).getId()+ " = " + currentListOfLocalParams.get(j).getValue());
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
 PropensityFunctionValue = evaluatePropensityFunction(currentASTNode, LocalParamsList);
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
//     outTXT.println("EnoughMolecules: " + currentReactionID + " = " + EnoughMolecules.get(currentReactionID));
    break;
    }
    }
    if (EnoughMolecules.get(currentReactionID)){
    PropensityFunctionValue = evaluatePropensityFunction(currentASTNode,LocalParamsList);
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
//     System.out.println("FW current AST Node = " + currentASTNode.getType());
//     System.out.println("FW current left child = " + currentASTNode.getLeftChild().getType());
    PropensityFunctionValueFW = evaluatePropensityFunction(currentASTNode.getLeftChild(), LocalParamsList);
//     System.out.println("PropensityFunctionValueFW = " + PropensityFunctionValueFW);
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
    PropensityFunctionValueRV = evaluatePropensityFunction(currentASTNode.getRightChild(),LocalParamsList);
    }
    PropensitySum = PropensitySum + PropensityFunctionValueRV;
    PropensityFunctionList.put(currentReactionID+"_rev", PropensityFunctionValueRV);
} 
 }
 
 // TODO PropensitySum == 0
 if (PropensitySum == 0.0){
 
 }
 else {
 
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
 CustomParams = openInteractiveMenu(time,tau,miu);
 t_next = Double.parseDouble(CustomParams[0]);
 while (t_next < time){
 JOptionPane.showMessageDialog(BioSim.frame, "The value of t_next needs to be greater than current time",
"Error in next simulation time", JOptionPane.ERROR_MESSAGE);
 CustomParams = openInteractiveMenu(time,tau,miu);
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
 outTSD.print("(" + time + ", ");
 for (int i=0;i<SpeciesList.size();i++){
 if (i<SpeciesList.size()-1)
 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]) + ", ");
 else
 outTSD.print(SpeciesList.get(SpeciesList.keySet().toArray()[i]) + "),");
 }
 if ((!isRunMode && !hasRunModeStarted) || (isRunMode && time >= runUntil)) {
 graph.refresh();
 }
 
 }
 outTSD.print(")");
 }

 static 
 {
   try 
   {
     System.loadLibrary("sbmlj");
   }
   catch (Exception e)
   {
     System.err.println("Could not load libSBML library:" + e.getMessage());
   }
 }
 
// public double fireEvents(double time, ListOfEvents eventList){
// for(int i=0; i< eventList.size();i++){
// Event event = eventList.get(i); 
// Trigger trigger = event.getTrigger();
// ASTNode triggerTopASTNode = trigger.getMath().getListOfNodes().get(0);
// boolean currTriggerVal = Boolean.parseBoolean(evaluateAST(triggerTopASTNode));
//// if (!prevTriggerVal && currTriggerVal) {
//// ASTNode currDelayTopASTNode = event.getDelay().getMath();
//// double delayVal = Double.parseDouble(evaluateAST(currDelayTopASTNode));
//// }
//  
//  
// }
//  
// return 0;
// }
private String evaluateAST(ASTNode currentASTNode) {
String retStr = null;
if(isLeafNode(currentASTNode)){
  retStr = evaluateLeafNode(currentASTNode);  
}
else{  // internal node with left and right children
int type_const=currentASTNode.getType();
switch (type_const) {
// arithmetic operators
case libsbml.AST_PLUS: retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) + Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
case libsbml.AST_MINUS: retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) - Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
case libsbml.AST_TIMES: retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) * Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
case libsbml.AST_DIVIDE:retStr = Double.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) / Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
case libsbml.AST_FUNCTION_POWER: retStr = Double.toString(Math.pow(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())), Double.parseDouble(evaluateAST(currentASTNode.getRightChild()))));
// logical operators
case libsbml.AST_LOGICAL_AND: retStr = Boolean.toString(Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild())) && Boolean.parseBoolean(evaluateAST(currentASTNode.getRightChild())));
case libsbml.AST_LOGICAL_OR:  retStr = Boolean.toString(Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild())) || Boolean.parseBoolean(evaluateAST(currentASTNode.getRightChild())));
case libsbml.AST_LOGICAL_NOT:  retStr = Boolean.toString(!Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild())));
case libsbml.AST_LOGICAL_XOR:  retStr = Boolean.toString(Boolean.parseBoolean(evaluateAST(currentASTNode.getLeftChild())) ^ Boolean.parseBoolean(evaluateAST(currentASTNode.getRightChild())));
// relational operators
case libsbml.AST_RELATIONAL_EQ:  retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) == Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
case libsbml.AST_RELATIONAL_GEQ: retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) >= Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
case libsbml.AST_RELATIONAL_GT:  retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) > Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
case libsbml.AST_RELATIONAL_LEQ:  retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) <= Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
case libsbml.AST_RELATIONAL_LT:  retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) < Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
case libsbml.AST_RELATIONAL_NEQ:  retStr = Boolean.toString(Double.parseDouble(evaluateAST(currentASTNode.getLeftChild())) > Double.parseDouble(evaluateAST(currentASTNode.getRightChild())));
// TODO other operators
// MOD, BITNOT, BITOR, BITAND, BITXOR, idiv
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
     else if (GlobalParamsList.containsKey(currentASTNode.getName())){
     node_val = GlobalParamsList.get(currentASTNode.getName());
     }  
     }
     return Double.toString(node_val);
}
 

public void InitializeEnoughMolecules(){  
for (int i = 0; i < ReactionsToIndex.size(); i++){
EnoughMolecules.put((String)ReactionsToIndex.keySet().toArray()[i], true);
}
}
 
public String[] openInteractiveMenu(double t, double tau, int miu) {
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
optionValue = JOptionPane.showOptionDialog(BioSim.frame, mainPanel, "Next Simulation Time",
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
 
public double evaluatePropensityFunction(ASTNode currentASTNode, HashMap<String, Double>ListOfLocalParameters) {
double ret = 0;
if(isLeafNode(currentASTNode)){
  ret = evaluatePropensityLeafNode(currentASTNode, ListOfLocalParameters);  
}
else{  // internal node with left and right children
int type_const=currentASTNode.getType();
if(type_const==libsbml.AST_PLUS){
ret = evaluatePropensityFunction(currentASTNode.getLeftChild(),ListOfLocalParameters) + evaluatePropensityFunction(currentASTNode.getRightChild(),ListOfLocalParameters);
}
if(type_const==libsbml.AST_MINUS){
ret = evaluatePropensityFunction(currentASTNode.getLeftChild(),ListOfLocalParameters) - evaluatePropensityFunction(currentASTNode.getRightChild(),ListOfLocalParameters);
}
if(type_const==libsbml.AST_TIMES){
ret = evaluatePropensityFunction(currentASTNode.getLeftChild(),ListOfLocalParameters) * evaluatePropensityFunction(currentASTNode.getRightChild(),ListOfLocalParameters);
}
if(type_const==libsbml.AST_DIVIDE){
ret = evaluatePropensityFunction(currentASTNode.getLeftChild(),ListOfLocalParameters) / evaluatePropensityFunction(currentASTNode.getRightChild(),ListOfLocalParameters);
}
if(type_const==libsbml.AST_FUNCTION_POWER){
ret = Math.pow(evaluatePropensityFunction(currentASTNode.getLeftChild(),ListOfLocalParameters), evaluatePropensityFunction(currentASTNode.getRightChild(),ListOfLocalParameters));
}
 }
return ret;
 }          
 
 public double evaluatePropensityLeafNode(ASTNode currentASTNode, HashMap<String, Double> currentListOfLocalParams){
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
      return node_val;
 }
}

