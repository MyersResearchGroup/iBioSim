package dynamicsim;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Stack;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.array.TDoubleArrayStack;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import javax.xml.stream.XMLStreamException;

import main.Gui;

import odk.lang.FastMath;

import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ASTNode.Type;


public class DynamicGillespie {
	
	//SBML model
	private Model model = null;
	
	//generates random numbers based on the xorshift method
	XORShiftRandom randomNumberGenerator = null;
	
	private HashMap<String, Reaction> reactionToSBMLReactionMap = null;
	
	//allows for access to a propensity from a reaction ID
	private TObjectDoubleHashMap<String> reactionToPropensityMap = null;
	
	//allows for access to reactant/product speciesID and stoichiometry from a reaction ID
	//note that species and stoichiometries need to be thought of as unique for each reaction
	private HashMap<String, HashSet<StringDoublePair> > reactionToSpeciesAndStoichiometrySetMap = null;
	
	//allows for access to reactant/modifier speciesID and stoichiometry from a reaction ID
	private HashMap<String, HashSet<StringDoublePair> > reactionToReactantStoichiometrySetMap = null;
	
	//allows for access to a kinetic formula tree from a reaction
	private HashMap<String, ASTNode> reactionToFormulaMap = null;
	
	//allows for access to a kinetic formula tree from a reaction
	//this is used for iterative evaluation
	private HashMap<String, ArrayDeque<ASTNode> > reactionToFormulaMap2 = null;
	
	//allows for access to a group number from a reaction ID
	private TObjectIntHashMap<String> reactionToGroupMap = null;
	
	//allows for access to a set of reactions that a species is in (as a reactant or modifier) from a species ID
	private HashMap<String, HashSet<String> > speciesToAffectedReactionSetMap = null;
	
	//a linked (ordered) set of all species IDs, to allow easy access to their values via the variableToValue map
	private LinkedHashSet<String> speciesIDSet = null;
	
	//allows for access to species and parameter values from a variable ID
	private TObjectDoubleHashMap<String> variableToValueMap = null;
	
	//allows for access to a group's min/max propensity from a group ID
	private TIntDoubleHashMap groupToMaxValueMap = new TIntDoubleHashMap(50);
	
	//allows for access to the minimum/maximum possible propensity in the group from a group ID
	private TIntDoubleHashMap groupToPropensityFloorMap = new TIntDoubleHashMap(50);
	private TIntDoubleHashMap groupToPropensityCeilingMap = new TIntDoubleHashMap(50);
	
	//allows for access to the reactionIDs in a group from a group ID
	private ArrayList<HashSet<String> > groupToReactionSetList = new ArrayList<HashSet<String> >(50);
	
	//allows for access to the group's total propensity from a group ID
	private TIntDoubleHashMap groupToTotalGroupPropensityMap = new TIntDoubleHashMap(50);
	
	//stores group numbers that are nonempty
	private TIntHashSet nonemptyGroupSet = new TIntHashSet(50);
	
	//stores events in order of fire time and priority
	private PriorityQueue<EventToFire> triggeredEventQueue = null;
	private HashSet<String> untriggeredEventSet = null;
	
	//hashmaps that allow for access to event information from the event's id
	private TObjectDoubleHashMap<String> eventToPriorityMap = null;
	private HashMap<String, ASTNode> eventToDelayMap = null;
	private HashMap<String, Boolean> eventToHasDelayMap = null;
	private HashMap<String, Boolean> eventToTriggerPersistenceMap = null;
	private HashMap<String, Boolean> eventToUseValuesFromTriggerTimeMap = null;
	private HashMap<String, ASTNode> eventToTriggerMap = null;
	private HashMap<String, HashSet<Object> > eventToAssignmentSetMap = null;
	
	//allows for access to the reactions whose propensity changes when an event fires
	private HashMap<String, HashSet<String> > eventToAffectedReactionSetMap = null;
	
	//allows for access to the set of events that a variable is in
	private HashMap<String, HashSet<String> > variableToEventSetMap = null;
	
	//compares two events based on fire time and priority
	private EventComparator eventComparator = new EventComparator();
	
	
	//number of groups including the empty groups and zero-propensity group
	private int numGroups = 0;
	
	//propensity variables
	private double totalPropensity = 0.0;
	private double minPropensity = Double.MAX_VALUE;
	private double maxPropensity = Double.MIN_VALUE;
	
	//file writing variables
	private FileWriter TSDWriter = null;
	private BufferedWriter bufferedTSDWriter = null;
	
	//boolean flags
	private boolean cancelFlag = false;;
	
	
	
	/**
	 * empty constructor
	 */
	public DynamicGillespie() {
	}
	
	/**
	 * simulates the sbml model
	 * 
	 * @param SBMLFileName
	 * @param outputDirectory
	 * @param timeLimit
	 * @param maxTimeStep
	 * @param randomSeed
	 */
	public void simulate(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval) {
		
		long timeBeforeSim = System.nanoTime();
		
		Boolean eventsFlag = false;
		
		//initialization will fail if the SBML model has errors
		try {
			if (!initialize(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, randomSeed, eventsFlag))
				return;
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (XMLStreamException e2) {
			e2.printStackTrace();
		}
		
		final boolean noEventsFlag = eventsFlag;
		
		System.err.println("initialization time: " + (System.nanoTime() - timeBeforeSim) / 1e9f);
		
		//SIMULATION LOOP
		//simulate until the time limit is reached
		
		long step1Time = 0;
		long step2Time = 0;
		long step3aTime = 0;
		long step3bTime = 0;
		long step4Time = 0;
		long step5Time = 0;
		long step6Time = 0;
		
		double currentTime = 0.0;
		double printTime = -0.1;
		
		while (currentTime <= timeLimit) {
			
			//if the user cancels the simulation
			if (cancelFlag == true) {
				
				JOptionPane.showMessageDialog(Gui.frame, "Simulation Canceled",
						"Canceled", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			//update progress bar			
			progress.setValue((int)((currentTime / timeLimit) * 100.0));
			
			//TSD PRINTING
			//print to TSD if the next print interval arrives
			//this obviously prints the previous timestep's data
			if (currentTime > printTime) {
				
				try {
					printToTSD();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				try {
					bufferedTSDWriter.write(",\n");
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				
				printTime += printInterval;
			}
			
			
			//EVENT HANDLING
			//trigger and/or fire events, etc.
			if (noEventsFlag == false)
				handleEvents(currentTime);			
			
			
			
			//STEP 1: generate random numbers
			
			//long step1Initial = System.nanoTime();
			
			double r1 = randomNumberGenerator.nextDouble();
			double r2 = randomNumberGenerator.nextDouble();
			double r3 = randomNumberGenerator.nextDouble();
			double r4 = randomNumberGenerator.nextDouble();
			
			//step1Time += System.nanoTime() - step1Initial;
			
			
			
			//STEP 2: calculate delta_t, the time till the next reaction execution
			
			//long step2Initial = System.nanoTime();
			 
			double delta_t = FastMath.log(1 / r1) / totalPropensity;
			
			//step2Time += System.nanoTime() - step2Initial;
			
			
			
			//STEP 3A: select a group
			
			//long step3aInitial = System.nanoTime();
			
			int selectedGroup = selectGroup(r2);
		
			//step3aTime += System.nanoTime() - step3aInitial;
			
			
			
			//STEP 3B: select a reaction within the group
			
			//long step3bInitial = System.nanoTime();
			
			String selectedReactionID = selectReaction(selectedGroup, r3, r4);
			
			//step3bTime += System.nanoTime() - step3bInitial;
			
			
			
			//STEP 4: perform selected reaction and update species counts
			
			//long step4Initial = System.nanoTime();
			
			performReaction(selectedReactionID);
			
			//step4Time += System.nanoTime() - step4Initial;
			
			
			
			//STEP 5: compute affected reactions' new propensities and update total propensity
			
			//long step5Initial = System.nanoTime();
			
			//create a set (precludes duplicates) of reactions that the selected reaction's species affect
			HashSet<String> affectedReactionSet = getAffectedReactionSet(selectedReactionID);
			
			boolean newMinPropensityFlag = updatePropensities(affectedReactionSet);
			
			//step5Time += System.nanoTime() - step5Initial;
			
			
			
			//STEP 6: re-assign affected reactions to appropriate groups
			
			//long step6Initial = System.nanoTime();
			
			//if there's a new minPropensity, then the group boundaries change
			//so re-calculate all groups
			if (newMinPropensityFlag == true)
				reassignAllReactionsToGroups();
			else
				updateGroups(affectedReactionSet);
			
			//step6Time += System.nanoTime() - step6Initial;
			
			
			
			//update time for next iteration: choose the smaller of delta_t and the given max timestep
			if (delta_t <= maxTimeStep)
				currentTime += delta_t;
			else
				currentTime += maxTimeStep;
			
		} //end simulation loop
		
		System.err.println("total time: " + String.valueOf((System.nanoTime() - timeBeforeSim) / 1e9f));
		System.err.println("total step 1 time: " + String.valueOf(step1Time / 1e9f));
		System.err.println("total step 2 time: " + String.valueOf(step2Time / 1e9f));
		System.err.println("total step 3a time: " + String.valueOf(step3aTime / 1e9f));
		System.err.println("total step 3b time: " + String.valueOf(step3bTime / 1e9f));
		System.err.println("total step 4 time: " + String.valueOf(step4Time / 1e9f));
		System.err.println("total step 5 time: " + String.valueOf(step5Time / 1e9f));
		System.err.println("total step 6 time: " + String.valueOf(step6Time / 1e9f));
		
		//print the final species counts
		try {
			printToTSD();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			bufferedTSDWriter.write(')');
			bufferedTSDWriter.flush();
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * loads the model and initializes the maps and variables and whatnot
	 * @throws IOException 
	 * @throws XMLStreamException 
	 * @throws XMLStreamException 
	 * @throws FileNotFoundException 
	 */
	private boolean initialize(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed, Boolean noEventsFlag) 
	throws IOException, XMLStreamException {
		
		randomNumberGenerator = new XORShiftRandom(randomSeed);
		
		TSDWriter = new FileWriter(outputDirectory + "run-1.tsd");		
		bufferedTSDWriter = new BufferedWriter(TSDWriter);
		bufferedTSDWriter.write('(');		
		
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = reader.readSBML(SBMLFileName);
		
		SBMLErrorLog errors = document.getListOfErrors();
		
		//if the sbml document has errors, tell the user and don't simulate
		if (document.getNumErrors() > 0) {
			
			String errorString = "";
			
			for (int i = 0; i < errors.getNumErrors(); i++) {
				errorString += errors.getError(i);
			}
			
			JOptionPane.showMessageDialog(Gui.frame, 
			"The SBML file contains " + document.getNumErrors() + " error(s):\n" + errorString,
			"SBML Error", JOptionPane.ERROR_MESSAGE);
			
			return false;
		}
		
		model = document.getModel();
		long numSpecies = model.getNumSpecies();
		long numParameters = model.getNumParameters();
		long numReactions = model.getNumReactions();
		long numEvents = model.getNumEvents();
		
		//set initial capacities for collections (1.5 is used to multiply numReactions due to reversible reactions)
		speciesToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numSpecies);
		speciesIDSet = new LinkedHashSet<String>((int) numSpecies);
		variableToValueMap = new TObjectDoubleHashMap<String>((int) numSpecies + (int) numParameters);
		
		reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (numReactions * 1.5));		
		reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));	
		reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));
		reactionToFormulaMap = new HashMap<String, ASTNode>((int) (numReactions * 1.5));
		//reactionToFormulaMap2 = new HashMap<String, ArrayDeque<ASTNode> >((int) (numReactions * 1.5));
		reactionToGroupMap = new TObjectIntHashMap<String>((int) (numReactions * 1.5));
		reactionToSBMLReactionMap = new HashMap<String, Reaction>((int) numReactions);
		
		triggeredEventQueue = new PriorityQueue<EventToFire>((int) numEvents, eventComparator);
		untriggeredEventSet = new HashSet<String>((int) numEvents);
		eventToPriorityMap = new TObjectDoubleHashMap<String>((int) numEvents);
		eventToDelayMap = new HashMap<String, ASTNode>((int) numEvents);
		eventToHasDelayMap = new HashMap<String, Boolean>((int) numEvents);
		eventToTriggerMap = new HashMap<String, ASTNode>((int) numEvents);
		eventToTriggerPersistenceMap = new HashMap<String, Boolean>((int) numEvents);
		eventToUseValuesFromTriggerTimeMap = new HashMap<String, Boolean>((int) numEvents);
		eventToAssignmentSetMap = new HashMap<String, HashSet<Object> >((int) numEvents);
		eventToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numEvents);
		variableToEventSetMap = new HashMap<String, HashSet<String> >((int) numEvents);
		
		bufferedTSDWriter.write('(');
		
		String commaSpace = "";
		
		//add values to hashmap for easy access to species amounts
		for (int i = 0; i < numSpecies; ++i) {
			
			String speciesID = model.getSpecies(i).getId();
			
			variableToValueMap.put(speciesID, model.getSpecies(i).getInitialAmount());
			speciesToAffectedReactionSetMap.put(speciesID, new HashSet<String>(20));
			speciesIDSet.add(speciesID);
			
			bufferedTSDWriter.write(commaSpace + "\"" + speciesID + "\"");
			commaSpace = ", ";
		}
		
		bufferedTSDWriter.write("),\n");
		
		//add values to hashmap for easy access to global parameter values
		//NOTE: the IDs for the parameters and species must be unique, so putting them in the
		//same hashmap is okay
		for (int i = 0; i < numParameters; ++i) {
			
			variableToValueMap.put(model.getParameter(i).getId(), model.getParameter(i).getValue());
		}
		
		if (numEvents == 0)
			noEventsFlag = true;
		else
			noEventsFlag = false;
		
		
		//STEP 0A: calculate initial propensities (including the total)		
		calculateInitialPropensities(numReactions);
		
		//STEP OB: create and populate initial groups		
		createAndPopulateInitialGroups();
		
		
		if (noEventsFlag == false) {
			
			//add event information to hashmaps for easy/fast access
			//this needs to happen after calculating initial propensities
			//so that the speciesToAffectedReactionSetMap is populated
			for (int i = 0; i < numEvents; ++i) {
				
				Event event = model.getEvent(i);
				String eventID = event.getId();
				
				if (event.isSetPriority())
					eventToPriorityMap.put(eventID, evaluateExpressionRecursive(event.getPriority().getMath()));
				
				if (event.isSetDelay()) {
					
					eventToDelayMap.put(eventID, event.getDelay().getMath());
					eventToHasDelayMap.put(eventID, true);
				}
				else
					eventToHasDelayMap.put(eventID, false);
				
				eventToTriggerMap.put(eventID, event.getTrigger().getMath());
				eventToTriggerPersistenceMap.put(eventID, event.getTrigger().getPersistent());
				eventToUseValuesFromTriggerTimeMap.put(eventID, event.isUseValuesFromTriggerTime());
				eventToAssignmentSetMap.put(eventID, new HashSet<Object>());
				eventToAffectedReactionSetMap.put(eventID, new HashSet<String>());
				
				untriggeredEventSet.add(eventID);
				
				for (EventAssignment assignment : event.getListOfEventAssignments()) {
					
					String variableID = assignment.getVariable();
					
					eventToAssignmentSetMap.get(eventID).add(assignment);					
					variableToEventSetMap.put(variableID, new HashSet<String>());
					variableToEventSetMap.get(variableID).add(eventID);
					
					//if the variable is a species, add the reactions it's in
					//to the event to affected reaction hashmap, which is used
					//for updating propensities after an event fires
					if (speciesToAffectedReactionSetMap.containsKey(variableID)) {
						
						eventToAffectedReactionSetMap.get(eventID).addAll(
								speciesToAffectedReactionSetMap.get(variableID));
					}					
				}
			}
		}
		
		
		return true;
	}
	
	/**
	 * calculates the initial propensities for each reaction in the model
	 * 
	 * @param numReactions the number of reactions in the model
	 */
	private void calculateInitialPropensities(long numReactions) {
		
		//loop through all reactions and calculate their propensities
		for (int i = 0; i < numReactions; ++i) {
			
			Reaction reaction = model.getReaction(i);
			String reactionID = reaction.getId();
			KineticLaw reactionKineticLaw = reaction.getKineticLaw();
			ASTNode reactionFormula = reactionKineticLaw.getMath();
			ListOf<LocalParameter> reactionParameters = reactionKineticLaw.getListOfLocalParameters();
			boolean notEnoughMoleculesFlagFd = false;
			boolean notEnoughMoleculesFlagRv = false;
			boolean notEnoughMoleculesFlag = false;
			
			reactionToSBMLReactionMap.put(reactionID, reaction);
			
			//put the local parameters into a hashmap for easy access
			//NOTE: these may overwrite some global parameters but that's fine,
			//because for each reaction the local parameters are the ones we want
			//and they're always defined
			for (int j = 0; j < reactionParameters.size(); ++j) {
				
				variableToValueMap.put(reactionParameters.get(j).getId(), reactionParameters.get(j).getValue());
			}
						
			//if it's a reversible reaction
			//split into a forward and reverse reaction (based on the minus sign in the middle)
			//and calculate both propensities
			if (reaction.getReversible()) {
				
				reactionToSpeciesAndStoichiometrySetMap.put(reactionID + "_fd", new HashSet<StringDoublePair>());
				reactionToSpeciesAndStoichiometrySetMap.put(reactionID + "_rv", new HashSet<StringDoublePair>());
				reactionToReactantStoichiometrySetMap.put(reactionID + "_fd", new HashSet<StringDoublePair>());
				reactionToReactantStoichiometrySetMap.put(reactionID + "_rv", new HashSet<StringDoublePair>());
				
				for (int a = 0; a < reaction.getNumReactants(); ++a) {
					
					SpeciesReference reactant = reaction.getReactant(a);
					String reactantID = reactant.getSpecies();
					double reactantStoichiometry = reactant.getStoichiometry();
					
					reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_fd").add(
							new StringDoublePair(reactantID, -reactantStoichiometry));
					reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_rv").add(
							new StringDoublePair(reactantID, reactantStoichiometry));
					reactionToReactantStoichiometrySetMap.get(reactionID + "_fd").add(
							new StringDoublePair(reactantID, reactantStoichiometry));
					
					//as a reactant, this species affects the reaction's propensity in the forward direction
					speciesToAffectedReactionSetMap.get(reactantID).add(reactionID + "_fd");
					
					//make sure there are enough molecules for this species
					//(in the reverse direction, molecules aren't subtracted, but added)
					if (variableToValueMap.get(reactantID) < reactantStoichiometry)
						notEnoughMoleculesFlagFd = true;
				}
				
				for (int a = 0; a < reaction.getNumProducts(); ++a) {
					
					SpeciesReference product = reaction.getProduct(a);
					String productID = product.getSpecies();
					double productStoichiometry = product.getStoichiometry();
					
					reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_fd").add(
							new StringDoublePair(productID, productStoichiometry));
					reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_rv").add(
							new StringDoublePair(productID, -productStoichiometry));
					reactionToReactantStoichiometrySetMap.get(reactionID + "_rv").add(
							new StringDoublePair(productID, productStoichiometry));
					
					//as a product, this species affects the reaction's propensity in the reverse direction
					speciesToAffectedReactionSetMap.get(productID).add(reactionID + "_rv");
					
					//make sure there are enough molecules for this species
					//(in the forward direction, molecules aren't subtracted, but added)
					if (variableToValueMap.get(productID) < productStoichiometry)
						notEnoughMoleculesFlagRv = true;
				}
				
				for (int a = 0; a < reaction.getNumModifiers(); ++a) {
					
					String modifierID = reaction.getModifier(a).getSpecies();
					
					String forwardString = "", reverseString = "";
					
					try {
						forwardString = ASTNode.formulaToString(reactionFormula.getLeftChild());
						reverseString = ASTNode.formulaToString(reactionFormula.getRightChild());
					} 
					catch (SBMLException e) {
						e.printStackTrace();
					}
					
					//check the kinetic law to see which direction the modifier affects the reaction's propensity
					if (forwardString.contains(modifierID))
						speciesToAffectedReactionSetMap.get(modifierID).add(reactionID + "_fd");
				
					if (reverseString.contains(modifierID))
						speciesToAffectedReactionSetMap.get(modifierID).add(reactionID + "_rv");
				}				
				
				double propensity;
				
				reactionToFormulaMap.put(reactionID + "_rv", reactionFormula.getRightChild());
				//reactionToFormulaMap2.put(reactionID + "_rv", GetPrefixQueueFromASTNode(reactionFormula.getRightChild()));
				reactionToFormulaMap.put(reactionID + "_fd", reactionFormula.getLeftChild());
				//reactionToFormulaMap2.put(reactionID + "_fd", GetPrefixQueueFromASTNode(reactionFormula.getLeftChild()));
				
				//calculate forward reaction propensity
				if (notEnoughMoleculesFlagFd == true)
					propensity = 0.0;
				else {
					//the left child is what's left of the minus sign
					propensity = evaluateExpressionRecursive(reactionFormula.getLeftChild());
					//propensity = CalculatePropensityIterative(reactionID + "_fd");
					
					if (propensity < minPropensity && propensity > 0) 
						minPropensity = propensity;
					else if (propensity > maxPropensity) 
						maxPropensity = propensity;
					
					totalPropensity += propensity;
				}
				
				reactionToPropensityMap.put(reactionID + "_fd", propensity);
				
				//calculate reverse reaction propensity
				if (notEnoughMoleculesFlagRv == true)
					propensity = 0.0;
				else {
					//the right child is what's right of the minus sign
					propensity = evaluateExpressionRecursive(reactionFormula.getRightChild());
					//propensity = CalculatePropensityIterative(reactionID + "_rv");
					
					if (propensity < minPropensity && propensity > 0) 
						minPropensity = propensity;
					else if (propensity > maxPropensity) 
						maxPropensity = propensity;
					
					totalPropensity += propensity;
				}
				
				reactionToPropensityMap.put(reactionID + "_rv", propensity);
			}
			//if it's not a reversible reaction
			else {
				
				reactionToSpeciesAndStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());
				reactionToReactantStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());
				
				for (int a = 0; a < reaction.getNumReactants(); ++a) {
					
					SpeciesReference reactant = reaction.getReactant(a);
					String reactantID = reactant.getSpecies();
					double reactantStoichiometry = reactant.getStoichiometry();
					
					reactionToSpeciesAndStoichiometrySetMap.get(reactionID).add(
							new StringDoublePair(reactantID, -reactantStoichiometry));
					reactionToReactantStoichiometrySetMap.get(reactionID).add(
							new StringDoublePair(reactantID, reactantStoichiometry));
					
					//as a reactant, this species affects the reaction's propensity
					speciesToAffectedReactionSetMap.get(reactantID).add(reactionID);
					
					//make sure there are enough molecules for this species
					if (variableToValueMap.get(reactantID) < reactantStoichiometry)
						notEnoughMoleculesFlag = true;
				}
				
				for (int a = 0; a < reaction.getNumProducts(); ++a) {
					
					SpeciesReference product = reaction.getProduct(a);
					
					reactionToSpeciesAndStoichiometrySetMap.get(reactionID).add(
							new StringDoublePair(product.getSpecies(), product.getStoichiometry()));
					
					//don't need to check if there are enough, because products are added
				}
				
				for (int a = 0; a < reaction.getNumModifiers(); ++a) {
					
					String modifierID = reaction.getModifier(a).getSpecies();
					
					//as a modifier, this species affects the reaction's propensity
					speciesToAffectedReactionSetMap.get(modifierID).add(reactionID);
				}
				
				reactionToFormulaMap.put(reactionID, reactionFormula);				
				//reactionToFormulaMap2.put(reactionID, GetPrefixQueueFromASTNode(reactionFormula));
				
				double propensity;
				
				if (notEnoughMoleculesFlag == true)
					propensity = 0.0;
				else {
				
					//calculate propensity
					propensity = evaluateExpressionRecursive(reactionFormula);
					//propensity = CalculatePropensityIterative(reactionID);
					
					if (propensity < minPropensity && propensity > 0) 
						minPropensity = propensity;
					if (propensity > maxPropensity) 
						maxPropensity = propensity;
					
					totalPropensity += propensity;
				}
				
				reactionToPropensityMap.put(reactionID, propensity);
			}
		}
	}
	
	/**
	 * cancels the simulation on the next iteration
	 * called from outside the class when the user closes the progress bar dialog
	 */
	public void cancel() {
		
		cancelFlag = true;
	}
	
	/**
	 * creates the appropriate number of groups and associates reactions with groups
	 */
	private void createAndPopulateInitialGroups() {
		
		//create groups
		int currentGroup = 1;
		double groupPropensityCeiling = 2 * minPropensity;
		
		groupToPropensityFloorMap.put(1, minPropensity);
		
		while (groupPropensityCeiling < maxPropensity) {
			
			groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
			groupToPropensityFloorMap.put(currentGroup + 1, groupPropensityCeiling);
			groupToMaxValueMap.put(currentGroup, 0.0);
			
			groupPropensityCeiling *= 2;
			++currentGroup;
		}
		
		groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
		groupToMaxValueMap.put(currentGroup, 0.0);
		numGroups = currentGroup + 1;
		
		//start at 0 to make a group for zero propensities
		for (int groupNum = 0; groupNum < numGroups; ++groupNum) {

			groupToReactionSetList.add(new HashSet<String>(500));
			groupToTotalGroupPropensityMap.put(groupNum, 0.0);
		}		
		
		//assign reactions to groups
		for (String reaction : reactionToPropensityMap.keySet()) {
			
			double propensity = reactionToPropensityMap.get(reaction);			
			org.openmali.FastMath.FRExpResultf frexpResult = org.openmali.FastMath.frexp((float) (propensity / minPropensity));
			int group = frexpResult.exponent;
			
			//System.out.println(reaction + "   " + propensity + "   " + group);
			
			groupToTotalGroupPropensityMap.adjustValue(group, propensity);
			groupToReactionSetList.get(group).add(reaction);
			reactionToGroupMap.put(reaction, group);
			
			if (propensity > groupToMaxValueMap.get(group))
				groupToMaxValueMap.put(group, propensity);
		}
		
		//find out which (if any) groups are empty
		//this is done so that empty groups are never chosen during simulation
		for (int groupNum = 1; groupNum < numGroups; ++groupNum) {
			
			if (groupToReactionSetList.get(groupNum).isEmpty())
				continue;
			
			nonemptyGroupSet.add(groupNum);
		}
	}

	/**
	 * calculates an expression using a recursive algorithm
	 * 
	 * @param node the AST with the formula
	 * @return the evaluated expression
	 */
	private double evaluateExpressionRecursive(ASTNode node) {
		
		//these if/else-ifs before the else are leaf conditions
		
		//logical constant, logical operator, or relational operator
		if (node.isBoolean()) {
			
			switch (node.getType()) {
			
			case CONSTANT_TRUE:
				return 1.0;
				
			case CONSTANT_FALSE:
				return 0.0;
				
			case LOGICAL_NOT:
				return getDoubleFromBoolean(!(getBooleanFromDouble(evaluateExpressionRecursive(node.getLeftChild()))));
				
			case LOGICAL_AND: {
				
				boolean andResult = true;
				
				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					andResult = andResult && getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter)));
				
				return getDoubleFromBoolean(andResult);
			}
				
			case LOGICAL_OR: {
				
				boolean orResult = false;
				
				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					orResult = orResult || getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter)));
				
				return getDoubleFromBoolean(orResult);				
			}
				
			case LOGICAL_XOR: {
				
				boolean xorResult = getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(0)));
				
				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					xorResult = xorResult ^ getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter)));
				
				return getDoubleFromBoolean(xorResult);
			}
			
			case RELATIONAL_EQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) == evaluateExpressionRecursive(node.getRightChild()));
				
			case RELATIONAL_NEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) != evaluateExpressionRecursive(node.getRightChild()));
				
			case RELATIONAL_GEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) >= evaluateExpressionRecursive(node.getRightChild()));
				
			case RELATIONAL_LEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) <= evaluateExpressionRecursive(node.getRightChild()));
				
			case RELATIONAL_GT:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) > evaluateExpressionRecursive(node.getRightChild()));
				
			case RELATIONAL_LT:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) < evaluateExpressionRecursive(node.getRightChild()));			
			}			
		}
		
		//if it's a mathematical constant
		else if (node.isConstant()) {
			
			switch (node.getType()) {
			
			case CONSTANT_E:
				return Math.E;
				
			case CONSTANT_PI:
				return Math.PI;
			}
		}
		
		//if it's a number
		else if (node.isNumber())
			return node.getReal();
		
		//if it's a user-defined variable
		//eg, a species name or global/local parameter
		else if (node.isName()) {
				
			//use node name to determine function
			//i'm not sure what to do with completely user-defined functions, though
			String nodeName = node.getName();
			
			//generates a uniform random number between the upper and lower bound
			if (nodeName.equals("uniform")) {
				
				double leftChildValue = evaluateExpressionRecursive(node.getLeftChild());
				double rightChildValue = evaluateExpressionRecursive(node.getRightChild());
				double lowerBound = FastMath.min(leftChildValue, rightChildValue);
				double upperBound = FastMath.max(leftChildValue, rightChildValue);
				
				return ((upperBound - lowerBound) * randomNumberGenerator.nextDouble()) + lowerBound;
			}
			else
				return variableToValueMap.get(node.getName());
		}
		
		//operators/functions with two children
		else {
			
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();
			
			switch (node.getType()) {
			
			case PLUS: {
				
				double sum = 0.0;
				
				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					sum += evaluateExpressionRecursive(node.getChild(childIter));					
					
				return sum;
			}
				
			case MINUS:
				return (evaluateExpressionRecursive(leftChild) - evaluateExpressionRecursive(rightChild));
				
			case TIMES: {
				
				double product = 1.0;
				
				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					product *= evaluateExpressionRecursive(node.getChild(childIter));
				
				return product;
			}
				
			case DIVIDE:
				return (evaluateExpressionRecursive(leftChild) / evaluateExpressionRecursive(rightChild));
				
			case FUNCTION_POWER:
				return (FastMath.pow(evaluateExpressionRecursive(leftChild), evaluateExpressionRecursive(rightChild)));
				
			//user-defined function
			case FUNCTION: {
				//use node name to determine function
				//i'm not sure what to do with completely user-defined functions, though
				String nodeName = node.getName();
				
				System.err.println(nodeName);
				
				//generates a uniform random number between the upper and lower bound
				if (nodeName.equals("uniform")) {
					
					double leftChildValue = evaluateExpressionRecursive(node.getLeftChild());
					double rightChildValue = evaluateExpressionRecursive(node.getRightChild());
					double lowerBound = FastMath.min(leftChildValue, rightChildValue);
					double upperBound = FastMath.max(leftChildValue, rightChildValue);
					
					System.err.println("uniform" + leftChildValue + " " + rightChildValue);
					
					return ((upperBound - lowerBound) * randomNumberGenerator.nextDouble()) + lowerBound;
				}
				
				break;
			}
			
			case FUNCTION_ABS:
				return FastMath.abs(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_ARCCOS:
				return FastMath.acos(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_ARCSIN:
				return FastMath.asin(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_ARCTAN:
				return FastMath.atan(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_CEILING:
				return FastMath.ceil(evaluateExpressionRecursive(node.getChild(0)));				
			
			case FUNCTION_COS:
				return FastMath.cos(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_COSH:
				return FastMath.cosh(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_EXP:
				return FastMath.exp(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_FLOOR:
				return FastMath.floor(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_LN:
				return FastMath.log(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_LOG:
				return FastMath.log10(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_SIN:
				return FastMath.sin(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_SINH:
				return FastMath.sinh(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_TAN:
				return FastMath.tan(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_TANH:		
				return FastMath.tanh(evaluateExpressionRecursive(node.getChild(0)));
				
			case FUNCTION_PIECEWISE:
				//function doesn't exist in current libraries
			
			case FUNCTION_ROOT:
				//function doesn't exist in current libraries
			
			case FUNCTION_SEC:
				//function doesn't exist in current libraries
			
			case FUNCTION_SECH:
				//function doesn't exist in current libraries
			
			case FUNCTION_FACTORIAL:
				//function doesn't exist in current libraries
				
			case FUNCTION_COT:
				//function doesn't exist in current libraries
			
			case FUNCTION_COTH:
				//function doesn't exist in current libraries
			
			case FUNCTION_CSC:
				//function doesn't exist in current libraries
			
			case FUNCTION_CSCH:
				//function doesn't exist in current libraries
			
			case FUNCTION_DELAY:
				//function doesn't exist in current libraries				
				
			case FUNCTION_ARCTANH:
				//function doesn't exist in current libraries
			
			case FUNCTION_ARCSINH:
				//function doesn't exist in current libraries
			
			case FUNCTION_ARCCOSH:
				//function doesn't exist in current libraries
			
			case FUNCTION_ARCCOT:
				//function doesn't exist in current libraries
			
			case FUNCTION_ARCCOTH:
				//function doesn't exist in current libraries
			
			case FUNCTION_ARCCSC:
				//function doesn't exist in current libraries
			
			case FUNCTION_ARCCSCH:
				//function doesn't exist in current libraries
			
			case FUNCTION_ARCSEC:
				//function doesn't exist in current libraries
			
			case FUNCTION_ARCSECH:
				//function doesn't exist in current libraries
				
			} //end switch
			
		}
		
		return 0.0;
	}
	
	/**
	 * calculates a reaction's propensity using an iterative algorithm
	 * 
	 * @param node the AST with the kinetic formula
	 * @return the propensity
	 */
	private double calculatePropensityIterative(String reactionID) {
		
		ArrayDeque<ASTNode> expressionQueue = reactionToFormulaMap2.get(reactionID).clone();
		TDoubleArrayStack resultStack = new TDoubleArrayStack(20);
		
		while (!expressionQueue.isEmpty()) {
			
			ASTNode currentNode = expressionQueue.removeLast();
			ASTNode.Type nodeType = currentNode.getType();
			
			if (currentNode.isOperator()) {
				
				//evaluate using currentNode (an operator) and push
				switch(nodeType) {
				
				case PLUS: {
					
					double sum = 0.0;
					
					for (int childIter = 0; childIter < currentNode.getChildCount(); ++childIter) {
						
						sum += evaluateExpressionRecursive(currentNode.getChild(childIter));
					}
						
					resultStack.push(sum);
					
					break;
				}
					
				case MINUS: {
					
					double operand1 = resultStack.pop();
					double operand2 = resultStack.pop();
					resultStack.push(operand2 - operand1);
					
					break;
				}
					
				case TIMES: {
					
					double product = 1.0;
					
					for (int childIter = 0; childIter < currentNode.getChildCount(); ++childIter)
						product *= evaluateExpressionRecursive(currentNode.getChild(childIter));					
						
					resultStack.push(product);
					
					break;
				}
					
				case DIVIDE: {
					
					double operand1 = resultStack.pop();
					double operand2 = resultStack.pop();
					resultStack.push(operand2 / operand1);
					
					break;
				}
					
				case FUNCTION_POWER: {
					
					double operand1 = resultStack.pop();
					double operand2 = resultStack.pop();				
					resultStack.push(FastMath.pow(operand2, operand1));
					
					break;
				}
				}
			}
			else if (currentNode.isFunction()) {
				
				//you'll have to check all the functions, then pop once to get the argument
				//note, this does not deal with user-defined functions, i don't think
				//you can do a switch for all the functions
			}
			else {
				
				//evaluate (get real or function value, etc.) and push
				if (currentNode.isConstant()) {
					
					switch (currentNode.getType()) {
					
					case CONSTANT_E:
						resultStack.push(Math.E);
						break;
						
					case CONSTANT_PI:
						resultStack.push(Math.PI);
						break;
						
//					case CONSTANT_TRUE:
//						return;
//						
//					case CONSTANT_FALSE:
//						return;
					}
				}
				
				//if it's a number
				else if (currentNode.isNumber())
					resultStack.push(currentNode.getReal());
				
				//if it's a user-defined variable
				//eg, a species name or global/local parameter
				else if (currentNode.isName()) {
					
					resultStack.push(variableToValueMap.get(currentNode.getName()));
				}
			}			
		}		
		
		return resultStack.pop();
	}
	
	/**
	 * generates a preorder/prefix queue from an AST expression
	 * 
	 * @param root the root node
	 * @return the prefix queue
	 */
	private ArrayDeque<ASTNode> getPrefixQueueFromASTNode(ASTNode root) {
		
		ArrayDeque<ASTNode> expressionQueue = new ArrayDeque<ASTNode>();		
		Stack<ASTNode> nodeStack = new Stack<ASTNode>();
		nodeStack.push(root);
        ASTNode currentNode = null;
 
        while (!nodeStack.isEmpty()) {
	
        	currentNode = nodeStack.pop();
            expressionQueue.add(currentNode);
				
			if (currentNode.getChildCount() > 0) {
				
				for (int childIter = 0; childIter < currentNode.getChildCount(); ++childIter)
		            nodeStack.push(currentNode.getChild(childIter));
    		}
        }
		
		return expressionQueue;
	}
	
	/**
	 * returns a set of all the reactions that the recently performed reaction affects
	 * "affect" means that the species updates will change the affected reaction's propensity
	 * 
	 * @param selectedReactionID the reaction that was recently performed
	 * @return the set of all reactions that the performed reaction affects the propensity of
	 */
	private HashSet<String> getAffectedReactionSet(String selectedReactionID) {
		
		HashSet<String> affectedReactionSet = new HashSet<String>(20);
		affectedReactionSet.add(selectedReactionID);
		
		//loop through the reaction's reactants and products and update their amounts
		for (StringDoublePair speciesAndStoichiometry : reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID)) {
			
			String speciesID = speciesAndStoichiometry.string;
			affectedReactionSet.addAll(speciesToAffectedReactionSetMap.get(speciesID));
		}
		
		return affectedReactionSet;
	}
	
	/**
	 * kind of a hack to mingle doubles and booleans for the expression evaluator
	 * 
	 * @param value the double to be translated to a boolean
	 * @return the translated boolean value
	 */
	private boolean getBooleanFromDouble(double value) {
		
		if (value == 0.0) 
			return false;
		else 
			return true;
	}
	
	/**
	 * kind of a hack to mingle doubles and booleans for the expression evaluator
	 * 
	 * @param value the boolean to be translated to a double
	 * @return the translated double value
	 */
	private double getDoubleFromBoolean(boolean value) {
		
		if (value == true)
			return 1.0;
		else 
			return 0.0;
	}
	
	/**
	 * updates the event queue and fires events and so on
	 * @param currentTime the current time in the simulation
	 */
	private void handleEvents(double currentTime) {
		
		HashSet<String> triggeredEvents = new HashSet<String>();
		
		//loop through all untriggered events
		//if any trigger, evaluate the fire time(s) and add them to the queue
		for (String untriggeredEventID : untriggeredEventSet) {
			
			//if the trigger evaluates to true
			if (getBooleanFromDouble(evaluateExpressionRecursive(eventToTriggerMap.get(untriggeredEventID))) == true) {
				
				triggeredEvents.add(untriggeredEventID);
				
				//if assignment is to be evaluated at trigger time, evaluate it and replace the ASTNode assignment
				if (eventToUseValuesFromTriggerTimeMap.get(untriggeredEventID) == true)	{
					
					//temporary hashset of evaluated assignments
					HashSet<Object> evaluatedAssignments = new HashSet<Object>();
					
					for (Object evAssignment : eventToAssignmentSetMap.get(untriggeredEventID)) {
						
						EventAssignment eventAssignment = (EventAssignment) evAssignment;
						evaluatedAssignments.add(new StringDoublePair(
								eventAssignment.getVariable(), evaluateExpressionRecursive(eventAssignment.getMath())));
					}
					
					double fireTime = currentTime;
					
					if (eventToHasDelayMap.get(untriggeredEventID) == true)
						fireTime += evaluateExpressionRecursive(eventToDelayMap.get(untriggeredEventID));
							
					triggeredEventQueue.add(new EventToFire(
							untriggeredEventID, evaluatedAssignments, fireTime));
				}
				else {
					
					double fireTime = currentTime;
					
					if (eventToHasDelayMap.get(untriggeredEventID) == true)
						fireTime += evaluateExpressionRecursive(eventToDelayMap.get(untriggeredEventID));
				
					triggeredEventQueue.add(new EventToFire(
							untriggeredEventID, eventToAssignmentSetMap.get(untriggeredEventID), fireTime));
				}				
			}
		}
		
		//remove recently triggered events from the untriggered set
		//when they're fired, they get put back into the untriggered set
		untriggeredEventSet.removeAll(triggeredEvents);
		
		
		//temporary set of events to remove from the triggeredEventQueue
		HashSet<String> untriggeredEvents = new HashSet<String>();
		
		//loop through all triggered events
		//if they aren't persistent and the trigger is no longer true
		//remove from triggered queue and put into untriggered set
		for (EventToFire triggeredEvent : triggeredEventQueue) {
			
			String triggeredEventID = triggeredEvent.eventID;
			
			//if the trigger evaluates to false
			if (getBooleanFromDouble(evaluateExpressionRecursive(eventToTriggerMap.get(triggeredEventID))) == false)
				untriggeredEvents.add(triggeredEventID);
		}
		
		triggeredEventQueue.removeAll(untriggeredEvents);
		
		
		//temporary set of affected reactions whose propensity/groups needs to be recalculated
		HashSet<String> affectedReactionSet = new HashSet<String>();
		
		//set of fired events to add to the untriggered set
		HashSet<String> firedEvents = new HashSet<String>();
		
		//fire all events whose fire time is less than the current time				
		while (triggeredEventQueue.size() > 0 && triggeredEventQueue.peek().fireTime <= currentTime) {
			
			EventToFire eventToFire = triggeredEventQueue.poll();
			String eventToFireID = eventToFire.eventID;
			affectedReactionSet.addAll(eventToAffectedReactionSetMap.get(eventToFireID));
			
			firedEvents.add(eventToFireID);
			
			//execute all assignments for this event
			for (Object eventAssignment : eventToFire.eventAssignmentSet) {
				
				String variable;
				double assignmentValue;
				
				if (eventToUseValuesFromTriggerTimeMap.get(eventToFireID) == true)	{
				
					variable = ((StringDoublePair) eventAssignment).string;
					assignmentValue = ((StringDoublePair) eventAssignment).doub;
				}
				//assignment needs to be evaluated
				else {
					
					variable = ((EventAssignment) eventAssignment).getVariable();
					assignmentValue = evaluateExpressionRecursive(((EventAssignment) eventAssignment).getMath());
				}
				
				variableToValueMap.put(variable, assignmentValue);				
			}					
		}
		
		//add the fired events back into the untriggered set
		//this allows them to trigger/fire again later
		untriggeredEventSet.addAll(firedEvents);
		
		//recalculate propensties/groups for affected reactions
		if (affectedReactionSet.size() > 0) {
			
			updatePropensities(affectedReactionSet);
			updateGroups(affectedReactionSet);
		}
	}
	
	/**
	 * updates reactant/product species counts based on their stoichiometries
	 * 
	 * @param selectedReactionID the reaction to perform
	 */
	private void performReaction(String selectedReactionID) {
		
		//loop through the reaction's reactants and products and update their amounts
		for (StringDoublePair speciesAndStoichiometry : reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID)) {
			
			double stoichiometry = speciesAndStoichiometry.doub;
			String speciesID = speciesAndStoichiometry.string;
			
			//update the species count
			//note that the stoichiometries are earlier modified with the correct +/- sign
			variableToValueMap.adjustValue(speciesID, stoichiometry);	
		}
	}
	
	/**
	 * appends the current species states to the TSD file
	 * @throws IOException 
	 */
	private void printToTSD() throws IOException {
		
		bufferedTSDWriter.write('(');
		
		String commaSpace = "";
		
		//loop through the speciesIDs and print their current value to the file
		for (String speciesID : speciesIDSet) {
			
			bufferedTSDWriter.write(commaSpace + (int) variableToValueMap.get(speciesID));
			commaSpace = ", ";
		}		
		
		bufferedTSDWriter.write(")");
	}
	
	/**
	 * assigns all reactions to (possibly new) groups
	 * this is called when the minPropensity changes, which
	 * changes the groups' floor/ceiling propensity values
	 */
	private void reassignAllReactionsToGroups() {
		
		int currentGroup = 1;
		double groupPropensityCeiling = 2 * minPropensity;
		
		//re-calulate and store group propensity floors/ceilings
		groupToPropensityCeilingMap.clear();
		groupToPropensityFloorMap.clear();
		groupToPropensityFloorMap.put(1, minPropensity);
		
		while (groupPropensityCeiling < maxPropensity) {
			
			groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
			groupToPropensityFloorMap.put(currentGroup + 1, groupPropensityCeiling);
			
			groupPropensityCeiling *= 2;
			++currentGroup;
		}
		
		groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
		int newNumGroups = currentGroup + 1;
		
		//allocate memory if the number of groups expands
		if (newNumGroups > numGroups) {
			
			for (int groupNum = numGroups; groupNum < newNumGroups; ++groupNum)
				groupToReactionSetList.add(new HashSet<String>(500));
		}
		
		//clear the reaction set for each group
		//start at 1, as the zero propensity group isn't going to change
		for (int groupNum = 1; groupNum < newNumGroups; ++groupNum) {
			
			groupToReactionSetList.get(groupNum).clear();
			groupToMaxValueMap.put(groupNum, 0.0);
			groupToTotalGroupPropensityMap.put(groupNum, 0.0);
		}
		
		numGroups = newNumGroups;		
		
		//assign reactions to groups
		for (String reaction : reactionToPropensityMap.keySet()) {
			
			double propensity = reactionToPropensityMap.get(reaction);
			
			//the zero-propensity group doesn't need altering
			if (propensity == 0.0) continue;
			
			org.openmali.FastMath.FRExpResultf frexpResult = org.openmali.FastMath.frexp((float) (propensity / minPropensity));
			int group = frexpResult.exponent;
			
			groupToReactionSetList.get(group).add(reaction);
			reactionToGroupMap.put(reaction, group);
			groupToTotalGroupPropensityMap.adjustValue(group, propensity);
			
			if (propensity > groupToMaxValueMap.get(group))
				groupToMaxValueMap.put(group, propensity);
		}
		
		//find out which (if any) groups are empty
		//this is done so that empty groups are never chosen during simulation
		
		nonemptyGroupSet.clear();
		
		for (int groupNum = 1; groupNum < numGroups; ++groupNum) {
			
			if (groupToReactionSetList.get(groupNum).isEmpty())
				continue;
			
			nonemptyGroupSet.add(groupNum);
		}		
	}
	
	/**
	 * chooses a random number between 0 and the total propensity
	 * then it finds which nonempty group this number belongs to
	 * 
	 * @param r2 random number
	 * @return the group selected
	 */
	private int selectGroup(double r2) {
		
		double randomPropensity = r2 * (totalPropensity);
		double totalGroupsPropensity = 0.0;
		int selectedGroup = 1;
		
		for (; selectedGroup < numGroups; ++selectedGroup) {
			
			totalGroupsPropensity += groupToTotalGroupPropensityMap.get(selectedGroup);
			
			if (randomPropensity < totalGroupsPropensity && nonemptyGroupSet.contains(selectedGroup))
				break;
		}
		
		return selectedGroup;
	}
	
	/**
	 * from the selected group, a reaction is chosen randomly/uniformly
	 * a random number between 0 and the group's max propensity is then chosen
	 * if this number is not less than the chosen reaction's propensity,
	 * the reaction is rejected and the process is repeated until success occurs
	 * 
	 * @param selectedGroup the group to choose a reaction from
	 * @param r3
	 * @param r4
	 * @return the chosen reaction's ID
	 */
	private String selectReaction(int selectedGroup, double r3, double r4) {
		
		HashSet<String> reactionSet = groupToReactionSetList.get(selectedGroup);
		
		double randomIndex = FastMath.floor(r3 * reactionSet.size());
		int indexIter = 0;
		Iterator<String> reactionSetIterator = reactionSet.iterator();
		
		while (reactionSetIterator.hasNext() && indexIter < randomIndex) {
			
			reactionSetIterator.next();
			++indexIter;
		}
			
		String selectedReactionID = reactionSetIterator.next();	
		double reactionPropensity = reactionToPropensityMap.get(selectedReactionID);
		
		//this is choosing a value between 0 and the max propensity in the group
		double randomPropensity = r4 * groupToMaxValueMap.get(selectedGroup);
		
		//loop until there's no reaction rejection
		//if the random propensity is higher than the selected reaction's propensity, another random reaction is chosen
		while (randomPropensity > reactionPropensity) {
			
			r3 = randomNumberGenerator.nextDouble();
			r4 = randomNumberGenerator.nextDouble();
			
			randomIndex = (int) FastMath.floor(r3 * reactionSet.size());
			indexIter = 0;
			reactionSetIterator = reactionSet.iterator();
			
			while (reactionSetIterator.hasNext() && (indexIter < randomIndex)) {
				
				reactionSetIterator.next();
				++indexIter;
			}
				
			selectedReactionID = reactionSetIterator.next();
			reactionPropensity = reactionToPropensityMap.get(selectedReactionID);				
			randomPropensity = r4 * groupToMaxValueMap.get(selectedGroup);
		}		
		
		return selectedReactionID;
	}
	
	/**
	 * updates the groups of the reactions affected by the recently performed reaction
	 * ReassignAllReactionsToGroups() is called instead when all reactions need changing
	 * 
	 * @param affectedReactionSet the set of reactions affected by the recently performed reaction
	 */
	private void updateGroups(HashSet<String> affectedReactionSet) {
		
		//update the groups for all of the affected reactions
		//their propensities have changed and they may need to go into a different group
		for (String affectedReactionID : affectedReactionSet) {
			
			double newPropensity = reactionToPropensityMap.get(affectedReactionID);
			int oldGroup = reactionToGroupMap.get(affectedReactionID);								
			
			if (newPropensity == 0.0) {
				
				HashSet<String> oldReactionSet = groupToReactionSetList.get(oldGroup);
				
				//update group collections
				//zero propensities go into group 0
				oldReactionSet.remove(affectedReactionID);
				reactionToGroupMap.put(affectedReactionID, 0);
				groupToReactionSetList.get(0).add(affectedReactionID);
				
				if (oldReactionSet.size() == 0)
					nonemptyGroupSet.remove(oldGroup);	
			}
			//if the new propensity != 0.0 (ie, new group != 0)
			else {
				//if it's outside of the old group's boundaries
				if (newPropensity > groupToPropensityCeilingMap.get(oldGroup) ||
						newPropensity < groupToPropensityFloorMap.get(oldGroup)) {
						
					if (newPropensity > maxPropensity)
						maxPropensity = newPropensity;
					
					org.openmali.FastMath.FRExpResultf frexpResult = org.openmali.FastMath.frexp((float) (newPropensity / minPropensity));
					int group = frexpResult.exponent;
				
					//if the group is one that currently exists
					if (group < numGroups) {
						
						HashSet<String> newGroupReactionSet = groupToReactionSetList.get(group);
						HashSet<String> oldGroupReactionSet = groupToReactionSetList.get(oldGroup);
						
						//update group collections
						oldGroupReactionSet.remove(affectedReactionID);
						reactionToGroupMap.put(affectedReactionID, group);
						newGroupReactionSet.add(affectedReactionID);
						groupToTotalGroupPropensityMap.adjustValue(group, newPropensity);
						
						//if the group that the reaction was just added to is now nonempty
						if (newGroupReactionSet.size() == 1)
							nonemptyGroupSet.add(group);
						
						if (oldGroupReactionSet.size() == 0)
							nonemptyGroupSet.remove(oldGroup);
						
						if (newPropensity > groupToMaxValueMap.get(group))
							groupToMaxValueMap.put(group, newPropensity);
					}
					//this means the propensity goes into a group that doesn't currently exist
					else {
						
						//groupToReactionSetList is a list, so the group needs to be the index
						for (int iter = numGroups; iter <= group; ++iter) {
							
							if (iter >= groupToReactionSetList.size())
								groupToReactionSetList.add(new HashSet<String>(500));
							
							groupToTotalGroupPropensityMap.put(iter, 0.0);
						}
						
						numGroups = group + 1;
						
						HashSet<String> oldReactionSet = groupToReactionSetList.get(oldGroup);
						
						//update group collections
						groupToTotalGroupPropensityMap.adjustValue(group, newPropensity);
						groupToReactionSetList.get(oldGroup).remove(affectedReactionID);
						reactionToGroupMap.put(affectedReactionID, group);
						groupToReactionSetList.get(group).add(affectedReactionID);						
						nonemptyGroupSet.add(group);
						groupToMaxValueMap.put(group, newPropensity);
						
						if (oldReactionSet.size() == 0)
							nonemptyGroupSet.remove(oldGroup);
					}
				} 
				//if it's within the old group's boundaries (ie, group isn't changing)
				else {

					//maintain current group
					
					if (newPropensity > maxPropensity)
						maxPropensity = newPropensity;
					
					if (newPropensity > groupToMaxValueMap.get(oldGroup))
						groupToMaxValueMap.put(oldGroup, newPropensity);
					
					groupToTotalGroupPropensityMap.adjustValue(oldGroup, newPropensity);
				}
			}			
		}		
	}
	
	/**
	 * updates the propensities of the reactions affected by the recently performed reaction
	 * @param affectedReactionSet the set of reactions affected by the recently performed reaction
	 * @return whether or not there's a new minPropensity (if there is, all reaction's groups need to change)
	 */
	private boolean updatePropensities(HashSet<String> affectedReactionSet) {
		
		boolean newMinPropensityFlag = false;
		
		//loop through the affected reactions and update the propensities
		for (String affectedReactionID : affectedReactionSet) {
			
			boolean notEnoughMoleculesFlag = false;
			
			HashSet<StringDoublePair> reactantStoichiometrySet = 
				reactionToReactantStoichiometrySetMap.get(affectedReactionID);
			
			//check for enough molecules for the reaction to occur
			for (StringDoublePair speciesAndStoichiometry : reactantStoichiometrySet) {
				
				String speciesID = speciesAndStoichiometry.string;
				double stoichiometry = speciesAndStoichiometry.doub;
				
				//if there aren't enough molecules to satisfy the stoichiometry
				if (variableToValueMap.get(speciesID) < stoichiometry) {
					notEnoughMoleculesFlag = true;
					break;
				}
			}
			
			double newPropensity = 0.0;
			
			if (notEnoughMoleculesFlag == false) {
				
				newPropensity = evaluateExpressionRecursive(reactionToFormulaMap.get(affectedReactionID));
				//newPropensity = CalculatePropensityIterative(affectedReactionID);
			}
			
			if (newPropensity > 0.0 && newPropensity < minPropensity) {
				
				minPropensity = newPropensity;
				newMinPropensityFlag = true;
			}
			
			double oldPropensity = reactionToPropensityMap.get(affectedReactionID);
			int oldGroup = reactionToGroupMap.get(affectedReactionID);
			
			//remove the old propensity from the group's total
			//later on, the new propensity is added to the (possibly new) group's total
			groupToTotalGroupPropensityMap.adjustValue(oldGroup, -oldPropensity);
			
			//add the difference of new v. old propensity to the total propensity
			totalPropensity += newPropensity - oldPropensity;
			
			reactionToPropensityMap.put(affectedReactionID, newPropensity);	
		}
		
		return newMinPropensityFlag;
	}
	
	
	//EVENT TO FIRE INNER CLASS
	/**
	 * 
	 */
	private class EventToFire {
		
		public String eventID = "";
		public HashSet<Object> eventAssignmentSet = null;
		public double fireTime = 0.0;
		
		public EventToFire(String eventID, HashSet<Object> eventAssignmentSet, double fireTime) {
			
			this.eventID = eventID;
			this.eventAssignmentSet = eventAssignmentSet;
			this.fireTime = fireTime;			
		}
	}
	
	
	//EVENT COMPARATOR INNER CLASS
	/**
	 * 
	 */
	private class EventComparator implements Comparator<EventToFire> {

		/**
		 * compares two events based on their fire times and priorities
		 */
		public int compare(EventToFire event1, EventToFire event2) {
			
			if (event1.fireTime > event2.fireTime)
				return 1;
			else if (event1.fireTime < event2.fireTime)
				return -1;
			else {
				
				if (eventToPriorityMap.get(event1.eventID) > eventToPriorityMap.get(event2.eventID))
					return -1;
				else if (eventToPriorityMap.get(event1.eventID) < eventToPriorityMap.get(event2.eventID))
					return 1;
				else
					return 0;
			}
		}
	}
	
	
	//STRING DOUBLE PAIR INNER CLASS	
	/**
	 * class to combine a string and a double
	 */
	private class StringDoublePair {
		
		public String string;
		public double doub;
		
		StringDoublePair(String s, double d) {
			
			string = s;
			doub = d;
		}
	}
}


/*
IMPLEMENTATION NOTES:
	

if the top node of a reversible reaction isn't a minus sign, then give an error and exit

look at the util sbml formula functions to see what happens with strings
	--i'm not sure this is still relevant

add a gc call after reach run??
	
make sure reaction selection is happening properly

you don't currently take volumes into account
	--these affect propensity, i think


EVALUATION NOTES:

apparently minus can be non-binary?  (need to check this)
AND/XOR/OR are all (potentially) non-binary

for binary shit:
	--if you know the arguments need to be boolean, you can force them to be boolean
		by converting the double to a boolean (1.0 to true, 0.0 to false) and returning 1.0 or 0.0
		hmmmm . . . kind of hack-ish, but it's probably the fastest way to handle it


EVENT NOTES:


if trigger is initially true, then should it fire immediately?

*/