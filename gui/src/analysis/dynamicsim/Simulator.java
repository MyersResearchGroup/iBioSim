package analysis.dynamicsim;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.PriorityQueue;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import flanagan.math.Fmath;
import flanagan.math.PsRandom;

import main.Gui;
import main.util.dataparser.DTSDParser;
import main.util.dataparser.DataParser;
import main.util.dataparser.TSDParser;

import odk.lang.FastMath;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.text.parser.ParseException;

public abstract class Simulator {
	
	//SBML model
	protected Model model = null;
	
	//generates random numbers based on the xorshift method
	protected XORShiftRandom randomNumberGenerator = null;
	
	//allows for access to a propensity from a reaction ID
	protected TObjectDoubleHashMap<String> reactionToPropensityMap = null;
	
	//allows for access to reactant/product speciesID and stoichiometry from a reaction ID
	//note that species and stoichiometries need to be thought of as unique for each reaction
	protected HashMap<String, HashSet<StringDoublePair> > reactionToSpeciesAndStoichiometrySetMap = null;
	
	//allows for access to reactant/modifier speciesID and stoichiometry from a reaction ID
	protected HashMap<String, HashSet<StringDoublePair> > reactionToReactantStoichiometrySetMap = null;
	
	//allows for access to a kinetic formula tree from a reaction
	protected HashMap<String, ASTNode> reactionToFormulaMap = null;
	
	//allows for access to a set of reactions that a species is in (as a reactant or modifier) from a species ID
	protected HashMap<String, HashSet<String> > speciesToAffectedReactionSetMap = null;
	
	//allows for access to species booleans from a species ID
	protected HashMap<String, Boolean> speciesToIsBoundaryConditionMap = null;
	protected HashMap<String, Boolean> speciesToHasOnlySubstanceUnitsMap = null;
	
	protected TObjectDoubleHashMap<String> speciesToCompartmentSizeMap = null;
	
	//a linked (ordered) set of all species IDs, to allow easy access to their values via the variableToValue map
	protected LinkedHashSet<String> speciesIDSet = null;
	
	//allows for access to species and parameter values from a variable ID
	protected TObjectDoubleHashMap<String> variableToValueMap = null;
	
	//stores events in order of fire time and priority
	protected PriorityQueue<EventToFire> triggeredEventQueue = null;
	protected HashSet<String> untriggeredEventSet = null;
	
	//hashmaps that allow for access to event information from the event's id
	protected TObjectDoubleHashMap<String> eventToPriorityMap = null;
	protected HashMap<String, ASTNode> eventToDelayMap = null;
	protected HashMap<String, Boolean> eventToHasDelayMap = null;
	protected HashMap<String, Boolean> eventToTriggerPersistenceMap = null;
	protected HashMap<String, Boolean> eventToUseValuesFromTriggerTimeMap = null;
	protected HashMap<String, ASTNode> eventToTriggerMap = null;
	protected HashMap<String, Boolean> eventToTriggerInitiallyTrueMap = null;
	protected HashMap<String, Boolean> eventToPreviousTriggerValueMap = null;
	protected HashMap<String, HashSet<Object> > eventToAssignmentSetMap = null;
	
	//allows for access to the reactions whose propensity changes when an event fires
	protected HashMap<String, HashSet<String> > eventToAffectedReactionSetMap = null;
	
	//allows for access to the set of events that a variable is in
	protected HashMap<String, HashSet<String> > variableToEventSetMap = null;
	
	//allows for access to the set of assignment rules that a variable (rhs) in an assignment rule affects
	protected HashMap<String, HashSet<AssignmentRule> > variableToAffectedAssignmentRuleSetMap = null;
	
	//allows to access to whether or not a variable is in an assignment rule (RHS)
	protected HashMap<String, Boolean> variableToIsInAssignmentRuleMap = null;
	
	//allows for access to the set of constraints that a variable affects
	protected HashMap<String, HashSet<ASTNode> > variableToAffectedConstraintSetMap = null;
	
	protected HashMap<String, Boolean> variableToIsInConstraintMap = null;
	protected HashMap<String, Boolean> variableToIsConstantMap = null;
	
	//compares two events based on fire time and priority
	protected EventComparator eventComparator = new EventComparator();
	
	//allows access to a component's location on the grid from its ID
	protected LinkedHashMap<String, Point> componentToLocationMap = null;
	
	//allows access to a component's set of reactions from its ID
	protected HashMap<String, HashSet<String> > componentToReactionSetMap = null;
	
	//allows access to a component's set of variables (species and parameters) from its ID
	protected HashMap<String, HashSet<String> > componentToVariableSetMap = null;
	
	protected HashMap<String, HashSet<String> > componentToEventSetMap = null;
	
	//propensity variables
	protected double totalPropensity = 0.0;
	protected double minPropensity = Double.MAX_VALUE;
	protected double maxPropensity = Double.MIN_VALUE;
	
	//file writing variables
	protected FileWriter TSDWriter = null;
	protected BufferedWriter bufferedTSDWriter = null;
	
	//boolean flags
	protected boolean cancelFlag = false;
	protected boolean constraintFailureFlag = false;
	protected boolean sbmlHasErrorsFlag = false;
	
	protected double currentTime;
	protected String SBMLFileName;
	protected double timeLimit;
	protected double maxTimeStep;
	protected JProgressBar progress;
	protected double printInterval;
	protected int currentRun;
	protected String outputDirectory;
	
	protected long numSpecies;
	protected long numParameters;
	protected long numReactions;
	protected long numEvents;
	protected long numRules;
	protected int numAssignmentRules;
	protected long numConstraints;
	protected int numInitialAssignments;
	
	protected int minRow = 0;
	protected int minCol = 0;
	protected int maxRow = 0;
	protected int maxCol = 0;
	
	//true means the model is dynamic
	protected boolean dynamicBoolean = false;

	PsRandom prng = new PsRandom();
	
	/**
	 * does lots of initialization
	 * 
	 * @param SBMLFileName
	 * @param outputDirectory
	 * @param timeLimit
	 * @param maxTimeStep
	 * @param randomSeed
	 * @param progress
	 * @param printInterval
	 * @param initializationTime
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public Simulator(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval, Long initializationTime) 
	throws IOException, XMLStreamException {
		
		long initTime1 = System.nanoTime();
		
		this.SBMLFileName = SBMLFileName;
		this.timeLimit = timeLimit;
		this.maxTimeStep = maxTimeStep;
		this.progress = progress;
		this.printInterval = printInterval;
		this.outputDirectory = outputDirectory;
		
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = null;
		
		document = reader.readSBML(SBMLFileName);
		
		//document.checkConsistency();
		
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
			
			sbmlHasErrorsFlag = true;
		}
		
		model = document.getModel();
		
		numSpecies = model.getNumSpecies();
		numParameters = model.getNumParameters();
		numReactions = model.getNumReactions();
		numEvents = model.getNumEvents();
		numRules = model.getNumRules();
		numConstraints = model.getNumConstraints();
		numInitialAssignments = model.getNumInitialAssignments();
		
		//set initial capacities for collections (1.5 is used to multiply numReactions due to reversible reactions)
		speciesToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numSpecies);
		speciesToIsBoundaryConditionMap = new HashMap<String, Boolean>((int) numSpecies);
		variableToIsConstantMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
		speciesToHasOnlySubstanceUnitsMap = new HashMap<String, Boolean>((int) numSpecies);
		speciesToCompartmentSizeMap = new TObjectDoubleHashMap<String>((int) numSpecies);
		speciesIDSet = new LinkedHashSet<String>((int) numSpecies);
		variableToValueMap = new TObjectDoubleHashMap<String>((int) numSpecies + (int) numParameters);
		
		reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (numReactions * 1.5));		
		reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));	
		reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));
		reactionToFormulaMap = new HashMap<String, ASTNode>((int) (numReactions * 1.5));
		
		componentToLocationMap = new LinkedHashMap<String, Point>();
		componentToReactionSetMap = new HashMap<String, HashSet<String> >();
		componentToVariableSetMap = new HashMap<String, HashSet<String> >();
		componentToEventSetMap = new HashMap<String, HashSet<String> >();
		
		if (numEvents > 0) {
			
			triggeredEventQueue = new PriorityQueue<EventToFire>((int) numEvents, eventComparator);
			untriggeredEventSet = new HashSet<String>((int) numEvents);
			eventToPriorityMap = new TObjectDoubleHashMap<String>((int) numEvents);
			eventToDelayMap = new HashMap<String, ASTNode>((int) numEvents);
			eventToHasDelayMap = new HashMap<String, Boolean>((int) numEvents);
			eventToTriggerMap = new HashMap<String, ASTNode>((int) numEvents);
			eventToTriggerInitiallyTrueMap = new HashMap<String, Boolean>((int) numEvents);
			eventToTriggerPersistenceMap = new HashMap<String, Boolean>((int) numEvents);
			eventToUseValuesFromTriggerTimeMap = new HashMap<String, Boolean>((int) numEvents);
			eventToAssignmentSetMap = new HashMap<String, HashSet<Object> >((int) numEvents);
			eventToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numEvents);
			eventToPreviousTriggerValueMap = new HashMap<String, Boolean>((int) numEvents);
			variableToEventSetMap = new HashMap<String, HashSet<String> >((int) numEvents);
		}
		
		if (numRules > 0) {
			
			variableToAffectedAssignmentRuleSetMap = new HashMap<String, HashSet<AssignmentRule> >((int) numRules);
			variableToIsInAssignmentRuleMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
		}
		
		if (numConstraints > 0) {
			
			variableToAffectedConstraintSetMap = new HashMap<String, HashSet<ASTNode> >((int) numConstraints);		
			variableToIsInConstraintMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
		}
		
		initializationTime = System.nanoTime() - initTime1;
	}
	
	/**
	 * calculates the initial propensity of a single reaction
	 * also does some initialization stuff
	 * 
	 * @param reactionID
	 * @param reactionFormula
	 * @param reversible
	 * @param reactantsList
	 * @param productsList
	 * @param modifiersList
	 */
	private void setupSingleReaction(String reactionID, ASTNode reactionFormula, boolean reversible, 
			ListOf<SpeciesReference> reactantsList, ListOf<SpeciesReference> productsList, 
			ListOf<ModifierSpeciesReference> modifiersList) {
		
		boolean notEnoughMoleculesFlagFd = false;
		boolean notEnoughMoleculesFlagRv = false;
		boolean notEnoughMoleculesFlag = false;
		
		//if it's a reversible reaction
		//split into a forward and reverse reaction (based on the minus sign in the middle)
		//and calculate both propensities
		if (reversible == true) {
			
			reactionToSpeciesAndStoichiometrySetMap.put(reactionID + "_fd", new HashSet<StringDoublePair>());
			reactionToSpeciesAndStoichiometrySetMap.put(reactionID + "_rv", new HashSet<StringDoublePair>());
			reactionToReactantStoichiometrySetMap.put(reactionID + "_fd", new HashSet<StringDoublePair>());
			reactionToReactantStoichiometrySetMap.put(reactionID + "_rv", new HashSet<StringDoublePair>());
			
			for (SpeciesReference reactant : reactantsList) {
				
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
			
			for (SpeciesReference product : productsList) {
				
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
			
			for (ModifierSpeciesReference modifier : modifiersList) {
				
				String modifierID = modifier.getSpecies();
				
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
			reactionToFormulaMap.put(reactionID + "_fd", reactionFormula.getLeftChild());
			
			//calculate forward reaction propensity
			if (notEnoughMoleculesFlagFd == true)
				propensity = 0.0;
			else {
				//the left child is what's left of the minus sign
				propensity = evaluateExpressionRecursive(reactionFormula.getLeftChild());
				
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
			
			for (SpeciesReference reactant : reactantsList) {
				
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
			
			for (SpeciesReference product : productsList) {
				
				reactionToSpeciesAndStoichiometrySetMap.get(reactionID).add(
						new StringDoublePair(product.getSpecies(), product.getStoichiometry()));
				
				//don't need to check if there are enough, because products are added
			}
			
			for (ModifierSpeciesReference modifier : modifiersList) {
				
				String modifierID = modifier.getSpecies();
				
				//as a modifier, this species affects the reaction's propensity
				speciesToAffectedReactionSetMap.get(modifierID).add(reactionID);
			}
			
			reactionToFormulaMap.put(reactionID, reactionFormula);
			
			double propensity;
			
			if (notEnoughMoleculesFlag == true)
				propensity = 0.0;
			else {
			
				//calculate propensity
				propensity = evaluateExpressionRecursive(reactionFormula);
				
				if (propensity < minPropensity && propensity > 0) 
					minPropensity = propensity;
				if (propensity > maxPropensity) 
					maxPropensity = propensity;
				
				totalPropensity += propensity;
			}
			
			reactionToPropensityMap.put(reactionID, propensity);
		}
	}	
	
	/**
	 * calculates the initial propensities for each reaction in the model
	 * 
	 * @param numReactions the number of reactions in the model
	 */
	protected void setupReactions() {
		
		//loop through all reactions and calculate their propensities
		for (Reaction reaction : model.getListOfReactions()) {
			
			String reactionID = reaction.getId();
			ASTNode reactionFormula = reaction.getKineticLaw().getMath();
						
			setupSingleReaction(reactionID, reactionFormula, reaction.getReversible(), 
					reaction.getListOfReactants(), reaction.getListOfProducts(), reaction.getListOfModifiers());
		}
	}

	/**
	 * replaces ROWX_COLY with ROWA_COLB in a kinetic law
	 * this has to be done without re-parsing the formula string because the row/col
	 * values can be negative, which gets parsed incorrectly
	 * 
	 * @param node
	 * @param oldString
	 * @param newString
	 */
	private void alterNode(ASTNode node, String oldString, String newString) {
		
		if (node.isName() && node.getName().contains(oldString)) {
			node.setVariable(model.getSpecies(newString + "__" + node.getName().split("__")[1]));
		}
		else {
			for (ASTNode childNode : node.getChildren())
				alterNode(childNode, oldString, newString);
		}
	}
	
	/**
	 * recursively finds all variable nodes and prepends a string to the variable
	 * 
	 * @param node
	 * @param toPrepend
	 */
	private void prependToVariableNodes(ASTNode node, String toPrepend) {
		
		if (node.isName()) {
			
			//only prepend to species and parameters
			if (model.getSpecies(toPrepend + node.getName()) != null)
				node.setVariable(model.getSpecies(toPrepend + node.getName()));
			else if (model.getParameter(toPrepend + node.getName()) != null)
				node.setVariable(model.getParameter(toPrepend + node.getName()));
		}
		else {
			for (ASTNode childNode : node.getChildren())
				prependToVariableNodes(childNode, toPrepend);
		}
	}
	
	/**
	 * recursively finds all variable nodes and prepends a string to the variable
	 * static version
	 * 
	 * @param node
	 * @param toPrepend
	 */
	private static void prependToVariableNodes(ASTNode node, String toPrepend, Model model) {
		
		if (node.isName()) {
			
			//only prepend to species and parameters
			if (model.getSpecies(toPrepend + node.getName()) != null)
				node.setVariable(model.getSpecies(toPrepend + node.getName()));
			else if (model.getParameter(toPrepend + node.getName()) != null)
				node.setVariable(model.getParameter(toPrepend + node.getName()));
		}
		else {
			for (ASTNode childNode : node.getChildren())
				prependToVariableNodes(childNode, toPrepend, model);
		}
	}
	
	/**
	 * cancels the current run
	 */
	protected abstract void cancel();
	
	/**
	 * clears data structures for new run
	 */
	protected abstract void clear();
	
	/**
	 * uses an existing component to create a new component and update data structures and the simulation state
	 * this is used for "birth" events in dynamic models
	 * 
	 * @param parentComponentID
	 * @param eventID the ID of the division event that just fired
	 */
	protected void duplicateComponent(String parentComponentID, String eventID) {
		
		//determine new component ID
		int componentNumber = componentToReactionSetMap.size() + 1;
		String childComponentID = "C" + String.valueOf(componentNumber);
		
		while (componentToReactionSetMap.keySet().contains(childComponentID) == true) {
			
			++componentNumber;
			childComponentID = "C" + String.valueOf(componentNumber);
		}
		
		childComponentID = childComponentID + "_of_" + parentComponentID;
		
		//determine new component location
		//choose a random direction and place the component adjacent to the parent
		//if that space is occupied, all components get moved over to make room
		Point parentLocation = componentToLocationMap.get(parentComponentID);
		Point childLocation = (Point) parentLocation.clone();
		
		HashSet<Integer> newRows = new HashSet<Integer>();
		HashSet<Integer> newCols = new HashSet<Integer>();
		
		//0 = left, 1 = right, 2 = above, 3 = below
		int randomDirection = (int) (randomNumberGenerator.nextDouble() * 4.0);
			
		switch (randomDirection) {
		
			case 0: childLocation.y -= 1; break;
			case 1: childLocation.y += 1; break;
			case 2: childLocation.x -= 1; break;
			case 3: childLocation.x += 1; break;
		}
		
		//if this place is taken, make room by moving the cells in the way
		if (componentToLocationMap.containsValue(childLocation)) {
			
			Point emptyLocation = (Point) childLocation.clone();
			HashSet<Point> locationsToMove = new HashSet<Point>();
			
			while (componentToLocationMap.containsValue(emptyLocation) == true) {
				
				locationsToMove.add((Point) emptyLocation.clone());
				
				switch (randomDirection) {
				
					case 0: emptyLocation.y -= 1; break;
					case 1: emptyLocation.y += 1; break;
					case 2: emptyLocation.x -= 1; break;
					case 3: emptyLocation.x += 1; break;		
				}
			}
			
			LinkedHashMap<String, Point> componentToLocationMapCopy = 
				(LinkedHashMap<String, Point>) componentToLocationMap.clone();
			
			//move the cells that are in the way
			for (Map.Entry<String, Point> componentAndLocation : componentToLocationMapCopy.entrySet()) {
				
				String compID = componentAndLocation.getKey();
				
				if (locationsToMove.contains(componentAndLocation.getValue())) {
					
					switch (randomDirection) {
					
						case 0: componentToLocationMap.get(compID).y -= 1; break;
						case 1: componentToLocationMap.get(compID).y += 1; break;
						case 2: componentToLocationMap.get(compID).x -= 1; break;
						case 3: componentToLocationMap.get(compID).x += 1; break;		
					}
										
					//keep track of min row/col and max row/col so you know the bounds of the grid
					if ((int) componentToLocationMap.get(compID).getX() < minRow) {
						minRow = (int) componentToLocationMap.get(compID).getX();
						newRows.add(minRow);
					}
					else if ((int) componentToLocationMap.get(compID).getX() > maxRow) {
						maxRow = (int) componentToLocationMap.get(compID).getX();
						newRows.add(maxRow);
					}
					if ((int) componentToLocationMap.get(compID).getY() < minCol) {
						minCol = (int) componentToLocationMap.get(compID).getY();
						newCols.add(minCol);
					}
					else if ((int) componentToLocationMap.get(compID).getY() > maxCol) {
						maxCol = (int) componentToLocationMap.get(compID).getY();
						newCols.add(maxCol);
					}
				}
			}
		}
		
		componentToLocationMap.put(childComponentID, childLocation);
		
		//keep track of min row/col and max row/col so you know the bounds of the grid
		if ((int) childLocation.getX() < minRow) {
			minRow = (int) childLocation.getX();
			newRows.add(minRow);
		}
		else if ((int) childLocation.getX() > maxRow) {
			maxRow = (int) childLocation.getX();
			newRows.add(maxRow);
		}
		if ((int) childLocation.getY() < minCol) {
			minCol = (int) childLocation.getY();
			newCols.add(minCol);
		}
		else if ((int) childLocation.getY() > maxCol) {
			maxCol = (int) childLocation.getY();
			newCols.add(maxCol);
		}
		
		HashSet<String> underlyingSpeciesIDs = new HashSet<String>();
		HashSet<String> newGridSpeciesIDs = new HashSet<String>();
		
		for (String speciesID : speciesIDSet) {
		
			//find the grid species
			if (speciesID.contains("ROW") && speciesID.contains("COL") && speciesID.contains("__")) {
				underlyingSpeciesIDs.add(speciesID.split("__")[1]);
			}
		}

		//if there are new rows or cols added to the grid
		//add new grid species reactions
		if (newRows.size() > 0) {
			
			for (int newRow : newRows) {
			
				//create new grid species for this new row
				for (int col = minCol; col <= maxCol; ++col) {
					
					for (String underlyingSpeciesID : underlyingSpeciesIDs) {
						
						String newID = "ROW" + newRow + "_COL" + col + "__" + underlyingSpeciesID;
						newGridSpeciesIDs.add(newID);
						
						Species gridSpecies = null;
						
						//find a grid species to take values from
						for (Species species : model.getListOfSpecies()) {
							
							if (species.getId().contains("__" + underlyingSpeciesID) && species.getId().contains("ROW") 
									&& species.getId().contains("COL"))
								gridSpecies = species;
						}
						
						Species newSpecies = gridSpecies.clone();
						newSpecies.setId(newID);
						
						//add new grid species to the model (so that altering the kinetic law through jsbml can work)
						model.addSpecies(newSpecies);
						
						//add a new species to the simulation data structures
						setupSingleSpecies(gridSpecies, newID);
						variableToValueMap.put(newID, 0);
					}
				}
			}
		}
		
		if (newCols.size() > 0) {
			
			for (int newCol : newCols) {
			
				//create new grid species for this new col
				for (int row = minRow; row <= maxRow; ++row) {
					
					for (String underlyingSpeciesID : underlyingSpeciesIDs) {
						
						String newID = "ROW" + row + "_COL" + newCol + "__" + underlyingSpeciesID;
						newGridSpeciesIDs.add(newID);
						
						Species gridSpecies = null;
						
						//find a grid species to take values from
						for (Species species : model.getListOfSpecies()) {
							
							if (species.getId().contains("__" + underlyingSpeciesID) && species.getId().contains("ROW") 
									&& species.getId().contains("COL"))
								gridSpecies = species;
						}
						
						Species newSpecies = gridSpecies.clone();
						newSpecies.setId(newID);
						
						//add new grid species to the model (so that altering the kinetic law through jsbml can work)
						model.addSpecies(newSpecies);
						
						//add a new species to the simulation data structures
						setupSingleSpecies(gridSpecies, newID);
						variableToValueMap.put(newID, 0);
					}
				}
			}
		}
		
		//create new grid diffusion and degradation reactions for the new grid species
		for (String speciesID : newGridSpeciesIDs) {
			
			String[] splitID = speciesID.split("_");
			
			int row = Integer.valueOf(splitID[0].replace("ROW",""));
			int col = Integer.valueOf(splitID[1].replace("COL",""));
			
			ArrayList<Point> neighborLocations = new ArrayList<Point>();
			
			neighborLocations.add(new Point(row+1,col)); //right
			neighborLocations.add(new Point(row,col+1)); //below
			neighborLocations.add(new Point(row-1,col)); //left
			neighborLocations.add(new Point(row,col-1)); //above
			
			String underlyingSpeciesID = speciesID.split("__")[1];
			ASTNode newNode = new ASTNode();
			
			//find a forward grid diffusion reaction with this underlying species to take values from
			for (Map.Entry<String, ASTNode> reactionAndFormula : reactionToFormulaMap.entrySet()) {
				
				String reactionID = reactionAndFormula.getKey();
				
				if (reactionID.contains("Diffusion_" + underlyingSpeciesID + "_Below") 
						|| reactionID.contains("Diffusion_" + underlyingSpeciesID + "_Above")
						|| reactionID.contains("Diffusion_" + underlyingSpeciesID + "_Left")
						|| reactionID.contains("Diffusion_" + underlyingSpeciesID + "_Right")
						&& reactionID.contains("_fd")) {
					
					newNode = reactionAndFormula.getValue().clone();
					newNode.getRightChild().setVariable(model.getSpecies(speciesID));
				}
			}
			
			int directionIndex = 0;
			
			for (Point neighborLocation : neighborLocations) {
				
				int nRow = (int) neighborLocation.getX();
				int nCol = (int) neighborLocation.getY();
				String neighborID = "ROW" + nRow + "_COL" + nCol + "__" + underlyingSpeciesID;
				
				String fdString = "", rvString = "";
				
				switch (directionIndex) {
				
				case 0: fdString = "Right"; rvString = "Left"; break;
				case 1: fdString = "Below"; rvString = "Above"; break;
				case 2: fdString = "Left"; rvString = "Right"; break;
				case 3: fdString = "Above"; rvString = "Below"; break;				
				}
				
				if (speciesIDSet.contains(neighborID)) {
					
					//create forward reaction if it doesn't exist already as a reverse reaction from a neighbor
					if (reactionToPropensityMap.containsKey("ROW" + nRow + "_COL" + nCol 
							+ "_Diffusion_" + underlyingSpeciesID + "_" + rvString + "_rv") == false) {
						
						Reaction fdReaction = model.createReaction("ROW" + row + "_COL" + col + "_Diffusion_" 
								+ underlyingSpeciesID + "_" + fdString + "_fd");
						KineticLaw fdKineticLaw = model.createKineticLaw();
						fdKineticLaw.setMath(newNode.clone());
						fdReaction.setKineticLaw(fdKineticLaw);
						fdReaction.addReactant(new SpeciesReference(model.getSpecies(speciesID)));
						fdReaction.addProduct(new SpeciesReference(model.getSpecies(neighborID)));
						
						setupLocalParameters(fdReaction.getKineticLaw(), fdReaction.getId());
						setupSingleReaction(fdReaction.getId(), fdReaction.getKineticLaw().getMath(), false,
								fdReaction.getListOfReactants(), fdReaction.getListOfProducts(), fdReaction.getListOfModifiers());
					}
					
					//create the reverse reaction if it doesn't already exist as the reverse reaction from a neighbor
					if (reactionToPropensityMap.containsKey("ROW" + nRow + "_COL" + nCol 
							+ "_Diffusion_" + underlyingSpeciesID + "_" + rvString + "_fd") == false) {
						
						//alter kinetic law for reverse reaction
						newNode.getRightChild().setVariable(model.getSpecies(neighborID));
						
						//create reverse reaction
						Reaction rvReaction = model.createReaction("ROW" + row + "_COL" + col + "_Diffusion_" 
								+ underlyingSpeciesID + "_" + fdString + "_rv");
						KineticLaw rvKineticLaw = model.createKineticLaw();
						rvKineticLaw.setMath(newNode.clone());
						rvReaction.setKineticLaw(rvKineticLaw);
						rvReaction.addProduct(new SpeciesReference(model.getSpecies(speciesID)));
						rvReaction.addReactant(new SpeciesReference(model.getSpecies(neighborID)));
	
						setupLocalParameters(rvReaction.getKineticLaw(), rvReaction.getId());
						setupSingleReaction(rvReaction.getId(), rvReaction.getKineticLaw().getMath(), false,
								rvReaction.getListOfReactants(), rvReaction.getListOfProducts(), rvReaction.getListOfModifiers());
					}
				}
				
				++directionIndex;
			}
			
			ASTNode degradationNode = new ASTNode();

			//create degradation reaction for each grid species
			//find a grid degradation reaction to copy from
			for (Map.Entry<String, ASTNode> reactionAndFormula : reactionToFormulaMap.entrySet()) {
				
				String reactionID = reactionAndFormula.getKey();
				
				if (reactionID.contains("Degradation_" + underlyingSpeciesID)) {
					
					degradationNode = reactionAndFormula.getValue().clone();
					degradationNode.getRightChild().setVariable(model.getSpecies(speciesID));
					break;
				}
			}
			
			Reaction degReaction = model.createReaction("ROW" + row + "_COL" + col + "Degradation_" 
					+ underlyingSpeciesID);
			KineticLaw degKineticLaw = model.createKineticLaw();
			degKineticLaw.setMath(degradationNode.clone());
			degReaction.setKineticLaw(degKineticLaw);
			degReaction.addReactant(new SpeciesReference(model.getSpecies(speciesID)));
			
			setupLocalParameters(degReaction.getKineticLaw(), degReaction.getId());
			setupSingleReaction(degReaction.getId(), degReaction.getKineticLaw().getMath(), false,
					degReaction.getListOfReactants(), degReaction.getListOfProducts(), degReaction.getListOfModifiers());
		}
		
		//DUPLICATE VARIABLES and alter them to coincide with the new ID
		
		HashSet<String> childComponentVariableSet = new HashSet<String>();
		
		for (String parentVariableID : componentToVariableSetMap.get(parentComponentID)) {
			
			String childVariableID = parentVariableID.replace(parentComponentID, childComponentID);
			childComponentVariableSet.add(childVariableID);
			
			//this means it's a species
			if (speciesIDSet.contains(parentVariableID)) {
				
				if (speciesToHasOnlySubstanceUnitsMap.get(parentVariableID) == false)
					speciesToCompartmentSizeMap.put(childVariableID, speciesToCompartmentSizeMap.get(parentVariableID));
				
				//the speciesToAffectedReactionSetMap gets filled-in later
				speciesToAffectedReactionSetMap.put(childVariableID, new HashSet<String>(20));
				speciesToIsBoundaryConditionMap.put(childVariableID, speciesToIsBoundaryConditionMap.get(parentVariableID));
				variableToIsConstantMap.put(childVariableID, variableToIsConstantMap.get(parentVariableID));
				speciesToHasOnlySubstanceUnitsMap.put(childVariableID, speciesToHasOnlySubstanceUnitsMap.get(parentVariableID));
				speciesIDSet.add(childVariableID);
				
				//divide the parent species amount by two by default
				//unless there is an event assignment or if the species is constant/bc			
				if (variableToIsConstantMap.get(parentVariableID) == true ||
						speciesToIsBoundaryConditionMap.get(parentVariableID)) {
					
					variableToValueMap.put(childVariableID, variableToValueMap.get(parentVariableID));
				}
				else {
					
					//go through the event assignments to find ones that affect this particular species
					//this is used to determine, via species quantity conservation, how much the daughter cell gets
					HashSet<Object> assignmentSetMap = this.eventToAssignmentSetMap.get(eventID);
					boolean setInAssignment = false;
					
					for (Object assignment : assignmentSetMap) {
						
						if (((EventAssignment) assignment).getVariable().equals(parentVariableID)) {
						
							double totalAmount = variableToValueMap.get(parentVariableID);
							double afterEventAmount = evaluateExpressionRecursive(((EventAssignment) assignment).getMath());
							double childAmount = totalAmount - afterEventAmount;
							
							variableToValueMap.put(childVariableID, childAmount);
							variableToValueMap.put(parentVariableID, afterEventAmount);
							
							setInAssignment = true;
						}
					}
				
					if (setInAssignment == false) {
						
						variableToValueMap.put(childVariableID, variableToValueMap.get(parentVariableID) / 2);
						variableToValueMap.put(parentVariableID, variableToValueMap.get(parentVariableID) / 2);
					}
				}
			}
			//this means it's a parameter
			else {
				
				variableToValueMap.put(childVariableID, variableToValueMap.get(parentVariableID));
			}
		}
		
		componentToVariableSetMap.put(childComponentID, childComponentVariableSet);	
		
		
		//DUPLICATE REACTIONS and alter them to coincide with the new ID
		
		HashSet<String> childReactionSet = new HashSet<String>();
		
		for (String parentReactionID : componentToReactionSetMap.get(parentComponentID)) {
			
			String parentReactionFormula = "";
			String childReactionID = parentReactionID.replace(parentComponentID, childComponentID);
			
			childReactionSet.add(childReactionID);
			
			try {
				parentReactionFormula = reactionToFormulaMap.get(parentReactionID).toFormula();
			} catch (SBMLException e) {
				e.printStackTrace();
			}
			
			ASTNode childFormulaNode = reactionToFormulaMap.get(parentReactionID).clone();
			
			try {
				
				String childReactionFormula = parentReactionFormula.replace(parentComponentID, childComponentID);
				
				if (parentReactionID.contains("MembraneDiffusion")) {
					
					String parentRowCol = "ROW" + (int) parentLocation.getX() + "_" + "COL" + (int) parentLocation.getY();
					String childRowCol = "ROW" + (int) childLocation.getX() + "_" + "COL" + (int) childLocation.getY();
					
					alterNode(childFormulaNode, parentRowCol, childRowCol);
				}
				else
					childFormulaNode = ASTNode.parseFormula(childReactionFormula);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			reactionToSpeciesAndStoichiometrySetMap.put(childReactionID, new HashSet<StringDoublePair>());
			reactionToReactantStoichiometrySetMap.put(childReactionID, new HashSet<StringDoublePair>());
			
			boolean notEnoughMoleculesFlag = false;
			
			//add species/stoichiometry pairs for this new reaction
			for (StringDoublePair parentSpeciesAndStoichiometry : reactionToSpeciesAndStoichiometrySetMap.get(parentReactionID)) {
				
				double childStoichiometry = parentSpeciesAndStoichiometry.doub;
				String childSpeciesID = parentSpeciesAndStoichiometry.string.replace(parentComponentID, childComponentID);
				
				reactionToSpeciesAndStoichiometrySetMap.get(childReactionID)
				.add(new StringDoublePair(childSpeciesID, childStoichiometry));
				
				if (speciesToAffectedReactionSetMap.containsKey(parentSpeciesAndStoichiometry.string))						
					speciesToAffectedReactionSetMap.get(childSpeciesID).add(childReactionID);
				
				//make sure there are enough molecules for this species
				//(if the molecule is a reactant -- if it's a product then it's being added)
				if (variableToValueMap.get(childSpeciesID) < childStoichiometry && 
						reactionToReactantStoichiometrySetMap.get(parentReactionID).contains(childSpeciesID))
					notEnoughMoleculesFlag = true;
			}
			
			//add reactant/stoichiometry pairs for this new reactions
			for (StringDoublePair parentReactantStoichiometry : reactionToReactantStoichiometrySetMap.get(parentReactionID)) {
				
				double childStoichiometry = parentReactantStoichiometry.doub;
				String childSpeciesID = parentReactantStoichiometry.string.replace(parentComponentID, childComponentID);	
				
				reactionToReactantStoichiometrySetMap.get(childReactionID).add(
						new StringDoublePair(childSpeciesID, childStoichiometry));
			}
			
			reactionToFormulaMap.put(childReactionID, childFormulaNode);
			
			double propensity;
			
			if (notEnoughMoleculesFlag == true)
				propensity = 0.0;
			else {
			
				//calculate propensity
				propensity = evaluateExpressionRecursive(childFormulaNode);
				
				if (propensity < minPropensity && propensity > 0) 
					minPropensity = propensity;
				if (propensity > maxPropensity) 
					maxPropensity = propensity;
				
				totalPropensity += propensity;
			}
			
			reactionToPropensityMap.put(childReactionID, propensity);	
		}
		
		componentToReactionSetMap.put(childComponentID, childReactionSet);
		
		//update propensities for the parent reactions, as their species values may have changed
		updatePropensities(componentToReactionSetMap.get(parentComponentID));
		
		updateAfterDynamicChanges();
		
		
		//DUPLICATE EVENTS
		
		HashSet<String> childEventSet = new HashSet<String>();
		
		for (String parentEventID : componentToEventSetMap.get(parentComponentID)) {
			
			String childEventID = parentEventID.replace(parentComponentID, childComponentID);
			
			untriggeredEventSet.add(childEventID);
			childEventSet.add(childEventID);
			
			//hashmaps that allow for access to event information from the event's id
			eventToPriorityMap.put(childEventID, eventToPriorityMap.get(parentEventID));
			eventToDelayMap.put(childEventID, eventToDelayMap.get(parentEventID).clone());
			eventToHasDelayMap.put(childEventID, eventToHasDelayMap.get(parentEventID));
			eventToTriggerPersistenceMap.put(childEventID, eventToTriggerPersistenceMap.get(parentEventID));
			eventToUseValuesFromTriggerTimeMap.put(childEventID, eventToUseValuesFromTriggerTimeMap.get(parentEventID));
			eventToTriggerMap.put(childEventID, eventToTriggerMap.get(parentEventID).clone());
			eventToTriggerInitiallyTrueMap.put(childEventID, eventToTriggerInitiallyTrueMap.get(parentEventID));
			eventToPreviousTriggerValueMap.put(childEventID, eventToTriggerInitiallyTrueMap.get(childEventID));
			
			//the parent event should be reset as well, as division creates two new things
			//not an old and a new
			untriggeredEventSet.add(parentEventID);
			eventToPreviousTriggerValueMap.put(parentEventID, eventToTriggerInitiallyTrueMap.get(parentEventID));
			
			HashSet<Object> assignmentSetMap = new HashSet<Object>();
			
			for (Object assignment : eventToAssignmentSetMap.get(parentEventID)) {
				
				String variableID = ((EventAssignment) assignment).getVariable();
				
				if (variableToEventSetMap.containsKey(variableID) == false)
					variableToEventSetMap.put(variableID, new HashSet<String>());
				
				variableToEventSetMap.get(variableID).add(childEventID);
				
				EventAssignment ea = ((EventAssignment)assignment).clone();				
				ea.setVariable(((EventAssignment)assignment).getVariable()
						.replace(parentComponentID, childComponentID));
				
				try {
					ea.setFormula(((EventAssignment)assignment).getFormula()
							.replace(parentComponentID, childComponentID));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				assignmentSetMap.add(ea);				
				
				if (speciesToAffectedReactionSetMap.containsKey(variableID)) {
					
					eventToAffectedReactionSetMap.put(childEventID, new HashSet<String>());
					eventToAffectedReactionSetMap.get(childEventID).addAll(
							speciesToAffectedReactionSetMap.get(variableID));
				}
			}
			
			eventToAssignmentSetMap.put(childEventID, assignmentSetMap);
		}
		
		componentToEventSetMap.put(childComponentID, childEventSet);
	}
	
	/**
	 * erases all traces of a component (ie, its reactions and species and parameters, etc) and updates data structures
	 * and the simulation state
	 * used for "death" events in dynamic models
	 * 
	 * @param componentID
	 */
	protected void eraseComponent(String componentID) {
		
		
	}
	
	/**
	 * calculates an expression using a recursive algorithm
	 * 
	 * @param node the AST with the formula
	 * @return the evaluated expression
	 */
	protected double evaluateExpressionRecursive(ASTNode node) {
		
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
				
				return prng.nextDouble(lowerBound, upperBound);
			}
			else if (nodeName.equals("exponential")) {
				
				return prng.nextExponential(evaluateExpressionRecursive(node.getLeftChild()), 1);
			}
			else if (nodeName.equals("gamma")) {
				
				return prng.nextGamma(1, evaluateExpressionRecursive(node.getLeftChild()), 
						evaluateExpressionRecursive(node.getRightChild()));
			}
			else if (nodeName.equals("chisq")) {
				
				return prng.nextChiSquare((int) evaluateExpressionRecursive(node.getLeftChild()));
			}
			else if (nodeName.equals("lognormal")) {
				
				return prng.nextLogNormal(evaluateExpressionRecursive(node.getLeftChild()), 
						evaluateExpressionRecursive(node.getRightChild()));
			}
			else if (nodeName.equals("laplace")) {
				
				//function doesn't exist in current libraries
			}
			else if (nodeName.equals("cauchy")) {
				
				return prng.nextLorentzian(0, evaluateExpressionRecursive(node.getLeftChild()));
			}
			else if (nodeName.equals("poisson")) {
				
				return prng.nextPoissonian(evaluateExpressionRecursive(node.getLeftChild()));
			}
			else if (nodeName.equals("binomial")) {
				
				return prng.nextBinomial(evaluateExpressionRecursive(node.getLeftChild()),
						(int) evaluateExpressionRecursive(node.getRightChild()));
			}
			else if (nodeName.equals("bernoulli")) {
				
				return prng.nextBinomial(evaluateExpressionRecursive(node.getLeftChild()), 1);
			}
			else if (nodeName.equals("normal")) {
				
				return prng.nextGaussian(evaluateExpressionRecursive(node.getLeftChild()),
						evaluateExpressionRecursive(node.getRightChild()));	
			}
			else if (nodeName.equals("get2DArrayElement")) {
				
//				int leftIndex = node.getChild(1).getInteger();
//				int rightIndex = node.getChild(2).getInteger();
//				String speciesName = "ROW" + leftIndex + "_COL" + rightIndex + "__" + node.getChild(0).getName();
//				
//				//check bounds
//				//if species exists, return its value/amount
//				if (variableToValueMap.containsKey(speciesName))
//					return variableToValueMap.get(speciesName);
			}
			else if (node.getType().equals(org.sbml.jsbml.ASTNode.Type.NAME_TIME)) {
				
				return currentTime;
			}
			else {
								
				return variableToValueMap.get(node.getName());
			}
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
				
			case MINUS: {
				
				double sum = evaluateExpressionRecursive(leftChild);
				
				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					sum -= evaluateExpressionRecursive(node.getChild(childIter));					
					
				return sum;
			}
				
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
				
				//String nodeName = node.getName();
				
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
				
			case FUNCTION_PIECEWISE: {
				
				//loop through child pairs
				//if child 1 is true, return child 2				
				for (int childIter = 0; childIter < node.getChildCount(); childIter += 2) {
					
					if (getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter))) &&
							(childIter + 1) < node.getChildCount())		
							return evaluateExpressionRecursive(node.getChild(childIter + 1));
				}
				
				return 0;
			}
			
			case FUNCTION_ROOT:
				return FastMath.pow(evaluateExpressionRecursive(node.getRightChild()), 
						1 / evaluateExpressionRecursive(node.getLeftChild()));
			
			case FUNCTION_SEC:
				return Fmath.sec(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_SECH:
				return Fmath.sech(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_FACTORIAL:
				return Fmath.factorial(evaluateExpressionRecursive(node.getChild(0)));
				
			case FUNCTION_COT:
				return Fmath.cot(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_COTH:
				return Fmath.coth(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_CSC:
				return Fmath.csc(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_CSCH:
				return Fmath.csch(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_DELAY:
				//NOT PLANNING TO SUPPORT THIS		
				
			case FUNCTION_ARCTANH:
				Fmath.atanh(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_ARCSINH:
				Fmath.asinh(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_ARCCOSH:
				Fmath.acosh(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_ARCCOT:
				Fmath.acot(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_ARCCOTH:
				Fmath.acoth(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_ARCCSC:
				Fmath.acsc(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_ARCCSCH:
				Fmath.acsch(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_ARCSEC:
				Fmath.asec(evaluateExpressionRecursive(node.getChild(0)));
			
			case FUNCTION_ARCSECH:
				Fmath.asech(evaluateExpressionRecursive(node.getChild(0)));
				
			} //end switch
			
		}
		
		return 0.0;
	}

	/**
	 * adds species and reactions to the model that are implicit in arrays
	 * basically, it takes an arrayed model and flattens it
	 */
	public static void expandArrays(String filename) {
		
		//open the sbml file for reading/writing
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = null;
		
		try {
			document = reader.readSBML(filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Model model = document.getModel();		
		
				
		//ARRAYED SPECIES BUSINESS
		//create all new species that are implicit in the arrays and put them into the model		
		
		ArrayList<Species> speciesToAdd = new ArrayList<Species>();
		ArrayList<String> speciesToRemove = new ArrayList<String>();
		
		HashMap<String, Boolean> speciesToIsArrayedMap = new HashMap<String, Boolean>();
		HashMap<String, SpeciesDimensions> arrayedSpeciesToDimensionsMap = 
			new HashMap<String, SpeciesDimensions>();
		
		for (Species species : model.getListOfSpecies()) {
			
			String speciesID = species.getId();
			
			//check to see if the species is arrayed			
			if (species.getAnnotationString().isEmpty() == false) {
				
				speciesToIsArrayedMap.put(speciesID, true);
				speciesToRemove.add(speciesID);
				
				int numRowsLower = 0;
				int numColsLower = 0;
				int numRowsUpper = 0;
				int numColsUpper = 0;
				
				String[] annotationString = species.getAnnotationString().split("=");
				
				numRowsLower = Integer.valueOf(((String[])(annotationString[1].split(" ")))[0].replace("\"",""));
				numRowsUpper = Integer.valueOf(((String[])(annotationString[2].split(" ")))[0].replace("\"",""));
				numColsLower = Integer.valueOf(((String[])(annotationString[3].split(" ")))[0].replace("\"",""));
				numColsUpper = Integer.valueOf(((String[])(annotationString[4].split(" ")))[0].replace("\"",""));
				
				SpeciesDimensions speciesDimensions = 
					new SpeciesDimensions(numRowsLower, numRowsUpper, numColsLower, numColsUpper);
				
				arrayedSpeciesToDimensionsMap.put(speciesID, speciesDimensions);
				
				//loop through all species in the array
				//prepend the row/col information to create a new ID
				for (int row = numRowsLower; row <= numRowsUpper; ++row) {
					for (int col = numColsLower; col <= numColsUpper; ++col) {
						
						speciesID = "ROW" + row + "_COL" + col + "__" + species.getId();
						
						Species newSpecies = new Species();
						newSpecies = species.clone();
						newSpecies.setMetaId(speciesID);
						newSpecies.setId(speciesID);
						newSpecies.setAnnotation(null);
						speciesToAdd.add(newSpecies);
					}
				}
			}
			else
				speciesToIsArrayedMap.put(speciesID, false);
		} //end species for loop
		
		//add new row/col species to the model
		for (Species species : speciesToAdd)
			model.addSpecies(species);
		
		//ARRAYED EVENTS BUSINESS
		
		ArrayList<String> eventsToRemove = new ArrayList<String>();
		ArrayList<Event> eventsToAdd = new ArrayList<Event>();
		
		for (Event event : model.getListOfEvents()) {
			
			if (event.getAnnotationString().contains("array")) {
				
				eventsToRemove.add(event.getId());
				
				String annotationString = event.getAnnotationString().replace("<annotation>","").
				replace("</annotation>","").replace("\"","");
				String[] splitAnnotation = annotationString.split("array:");
				ArrayList<String> eventCompartments = new ArrayList<String>();
				
				splitAnnotation[splitAnnotation.length - 2] = ((String[])splitAnnotation[splitAnnotation.length - 2].split("xmlns:"))[0];
				
				for (int i = 2; i < splitAnnotation.length - 1; ++i) {
					
					String compartmentID = ((String[])splitAnnotation[i].split("="))[0];
					eventCompartments.add(compartmentID);
				}
				
				//loop through all compartments and create an event for each one
				for (String compartmentID : eventCompartments) {
					
					Event newEvent = new Event();
					newEvent.setVersion(event.getVersion());
					newEvent.setLevel(event.getLevel());
					newEvent.setId(compartmentID + "__" + event.getId());
					newEvent.setTrigger(event.getTrigger().clone());
					
					if (event.isSetPriority())
						newEvent.setPriority(event.getPriority().clone());
					
					if (event.isSetDelay())
						newEvent.setDelay(event.getDelay().clone());
					
					newEvent.setUseValuesFromTriggerTime(event.getUseValuesFromTriggerTime());
					
					ArrayList<String> eventAssignmentsToRemove = new ArrayList<String>();
					
					for (EventAssignment eventAssignment : event.getListOfEventAssignments()) {
						
						EventAssignment ea = eventAssignment.clone();
						ea.setMath(eventAssignment.getMath().clone());
						ea.setVariable(eventAssignment.getVariable());
						newEvent.addEventAssignment(ea);
					}
					
					for (EventAssignment eventAssignment : newEvent.getListOfEventAssignments()) {
						
						if (eventAssignment.getVariable().contains("_size")) {
							
							eventAssignmentsToRemove.add(eventAssignment.getVariable());
							continue;
						}
							
						eventAssignment.setVariable(compartmentID + "__" + eventAssignment.getVariable());
						
						//prepends the compartment ID to all variables in the event assignment
						prependToVariableNodes(eventAssignment.getMath(), compartmentID + "__", model);
					}
					
					for (String eaToRemove : eventAssignmentsToRemove)
						newEvent.removeEventAssignment(eaToRemove);
					
					eventsToAdd.add(newEvent);
				}
			}
		}
		
		for (Event eventToAdd : eventsToAdd)
			model.addEvent(eventToAdd);
		
		
		//ARRAYED REACTION BUSINESS
		//if a reaction has arrayed species
		//new reactions that are implicit are created and added to the model		
		
		ArrayList<Reaction> reactionsToAdd = new ArrayList<Reaction>();
		ArrayList<String> reactionsToRemove = new ArrayList<String>();
		
		for (Reaction reaction : model.getListOfReactions()) {
			
			String reactionID = reaction.getId();
			
			//if reaction itself is arrayed
			if (reaction.getAnnotationString().isEmpty() == false) {				
			}
			
			ArrayList<Integer> membraneDiffusionRows = new ArrayList<Integer>();
			ArrayList<Integer> membraneDiffusionCols = new ArrayList<Integer>();
			ArrayList<String> membraneDiffusionCompartments = new ArrayList<String>();
			
			//MEMBRANE DIFFUSION REACTIONS
			//if it's a membrane diffusion reaction it'll have the appropriate locations as an annotation
			//so parse them and store them in the above arraylists
			if (reactionID.contains("MembraneDiffusion")) {
				
				String annotationString = reaction.getAnnotationString().replace("<annotation>","").
					replace("</annotation>","").replace("\"","");
				String[] splitAnnotation = annotationString.split("array:");
				
				splitAnnotation[splitAnnotation.length - 2] = ((String[])splitAnnotation[splitAnnotation.length - 2].split("xmlns:"))[0];
				
				for (int i = 2; i < splitAnnotation.length - 1; ++i) {
					
					String compartmentID = ((String[])splitAnnotation[i].split("="))[0];
					String row = ((String[])((String[])splitAnnotation[i].split("="))[1].split(","))[0].replace("(","");
					String col = ((String[])((String[])splitAnnotation[i].split("="))[1].split(","))[1].replace(")","");
					
					membraneDiffusionRows.add(Integer.valueOf(row.trim()));
					membraneDiffusionCols.add(Integer.valueOf(col.trim()));
					membraneDiffusionCompartments.add(compartmentID);
				}
				
				int membraneDiffusionIndex = 0;
				
				reactionsToRemove.add(reaction.getId());
				reaction.setAnnotation(null);
				
				//loop through all appropriate row/col pairs and create a membrane diffusion reaction for each one
				for (String compartmentID : membraneDiffusionCompartments) {
					
					int row = membraneDiffusionRows.get(membraneDiffusionIndex);
					int col = membraneDiffusionCols.get(membraneDiffusionIndex);
					
					//create a new reaction and set the ID
					Reaction newReaction = new Reaction();
					newReaction = reaction.clone();
					newReaction.setListOfReactants(new ListOf<SpeciesReference>());
					newReaction.setListOfProducts(new ListOf<SpeciesReference>());
					newReaction.setListOfModifiers(new ListOf<ModifierSpeciesReference>());
					newReaction.setId(compartmentID  + "__" + reactionID);
					newReaction.setReversible(true);
					newReaction.setFast(false);
					newReaction.setCompartment(reaction.getCompartment());
					
					//alter the kinetic law to so that it has the correct indexes as children for the
					//get2DArrayElement function
					//get the nodes to alter (that are arguments for the get2DArrayElement method)
					ArrayList<ASTNode> get2DArrayElementNodes = new ArrayList<ASTNode>();
					
					getSatisfyingNodes(newReaction.getKineticLaw().getMath(), 
							"get2DArrayElement", get2DArrayElementNodes);	
					
					boolean reactantBool = false;
					
					//replace the get2darrayelement stuff with the proper explicit species/parameters
					for (ASTNode node : get2DArrayElementNodes) {
												
						if (node.getLeftChild().getName().contains("kmdiff")) {
							
							String parameterName = node.getLeftChild().getName();
							
							//see if the species-specific one exists
							//if it doesn't, use the default
							//you'll need to parse the species name from the reaction id, probably
							
							String speciesID = reactionID.replace("MembraneDiffusion_","");
							
							if (model.getParameter(compartmentID + "__" + speciesID + "__" + parameterName) == null)
								node.setVariable(model.getParameter(parameterName));
							else							
								node.setVariable(model.getParameter(compartmentID + "__" + speciesID + "__" + parameterName));	
							
							for (int i = 0; i < node.getChildCount(); ++i)
								node.removeChild(i);
							
							for (int i = 0; i < node.getChildCount(); ++i)
								node.removeChild(i);
						}
						//this means it's a species, which we need to prepend with the row/col prefix
						else {
							
							if (node.getChildCount() > 0 &&
									model.getParameter(node.getLeftChild().getName()) == null) {
								
								//reactant
								if (reactantBool == true) {
									
									node.setVariable(model.getSpecies(
											"ROW" + row + "_COL" + col + "__" + node.getLeftChild().getName()));
								}
								//product
								else {
									
									node.setVariable(model.getSpecies(
											compartmentID + "__" + node.getLeftChild().getName()));
									reactantBool = true;
								}
							}						
								
							for (int i = 0; i < node.getChildCount(); ++i)
								node.removeChild(i);
							
							for (int i = 0; i < node.getChildCount(); ++i)
								node.removeChild(i);
						}
					}
					
					//loop through reactants
					for (SpeciesReference reactant : reaction.getListOfReactants()) {
						
						//create a new reactant and add it to the new reaction
						SpeciesReference newReactant = new SpeciesReference();
						newReactant = reactant.clone();
						newReactant.setSpecies(compartmentID + "__" + newReactant.getSpecies());
						newReactant.setAnnotation(null);
						newReaction.addReactant(newReactant);
					}
					
					//loop through products
					for (SpeciesReference product : reaction.getListOfProducts()) {
						
						//create a new reactant and add it to the new reaction
						SpeciesReference newProduct = new SpeciesReference();
						newProduct = product.clone();
						newProduct.setSpecies("ROW" + row + "_COL" + col + "__" + newProduct.getSpecies());
						newProduct.setAnnotation(null);
						newReaction.addProduct(newProduct);
					}
					
					if (newReaction.getKineticLaw().getLocalParameter("i") != null)
						newReaction.getKineticLaw().removeLocalParameter("i");
					
					if (newReaction.getKineticLaw().getLocalParameter("j") != null)
						newReaction.getKineticLaw().removeLocalParameter("j");
					
					reactionsToAdd.add(newReaction);
					++membraneDiffusionIndex;
				}
			}
			
			//NON-MEMBRANE DIFFUSION REACTIONS
			//check to see if the (non-membrane-diffusion) reaction has arrayed species
			//right now i'm only checking the first reactant species, due to a bad assumption
			//about the homogeneity of the arrayed reaction (ie, if one species is arrayed, they all are)
			else if (reaction.getNumReactants() > 0 &&
					speciesToIsArrayedMap.get(reaction.getReactant(0).getSpeciesInstance().getId()) == true) {
				
				reactionsToRemove.add(reaction.getId());
				
				//get the reactant dimensions, which tells us how many new reactions are going to be created
				SpeciesDimensions reactantDimensions = 
					arrayedSpeciesToDimensionsMap.get(reaction.getReactant(0).getSpeciesInstance().getId());
				
				boolean abort = false;
				
				//loop through all of the new formerly-implicit reactants
				for (int row = reactantDimensions.numRowsLower; row <= reactantDimensions.numRowsUpper; ++row) {
				for (int col = reactantDimensions.numColsLower; col <= reactantDimensions.numColsUpper; ++col) {	
					
					//create a new reaction and set the ID
					Reaction newReaction = new Reaction();
					newReaction = reaction.clone();
					newReaction.setListOfReactants(new ListOf<SpeciesReference>());
					newReaction.setListOfProducts(new ListOf<SpeciesReference>());
					newReaction.setListOfModifiers(new ListOf<ModifierSpeciesReference>());
					newReaction.setId("ROW" + row + "_COL" + col + "_" + reactionID);
					
					if (reactionID.contains("Degradation"))
						newReaction.setReversible(false);
					else
						newReaction.setReversible(true);
					
					newReaction.setFast(false);
					newReaction.setCompartment(reaction.getCompartment());
					
					//get the nodes to alter
					ArrayList<ASTNode> get2DArrayElementNodes = new ArrayList<ASTNode>();
					
					//return the head node of the get2DArrayElement function
					getSatisfyingNodes(newReaction.getKineticLaw().getMath(), 
							"get2DArrayElement", get2DArrayElementNodes);
					
					//loop through all reactants
					for (SpeciesReference reactant : reaction.getListOfReactants()) {
						
						//find offsets
						//the row offset is in the kinetic law via i
						//the col offset is in the kinetic law via j
						int rowOffset = 0;
						int colOffset = 0;
						
						ASTNode reactantHeadNode = null;
						
						//go through the get2DArrayElement nodes and find the one corresponding to the reactant
						for (ASTNode headNode : get2DArrayElementNodes) {
							
							//make sure it's a reactant node
							if (headNode.getChildCount() > 0 &&
									model.getParameter(headNode.getLeftChild().getName()) == null) {
								
								reactantHeadNode = headNode;
								break;
							}
						}
						
						if (reactantHeadNode.getChild(1).getType().name().equals("PLUS"))							
							rowOffset = reactantHeadNode.getChild(1).getRightChild().getInteger();
						else if (reactantHeadNode.getChild(1).getType().name().equals("MINUS"))							
							rowOffset = -1 * reactantHeadNode.getChild(1).getRightChild().getInteger();
						
						if (reactantHeadNode.getChild(2).getType().name().equals("PLUS"))							
							colOffset = reactantHeadNode.getChild(2).getRightChild().getInteger();
						else if (reactantHeadNode.getChild(2).getType().name().equals("MINUS"))							
							colOffset = -1 * reactantHeadNode.getChild(2).getRightChild().getInteger();
						
						//create a new reactant and add it to the new reaction
						SpeciesReference newReactant = new SpeciesReference();
						newReactant = reactant.clone();
						newReactant.setSpecies("ROW" + (row + rowOffset) + "_COL" + (col + colOffset) + "__" + newReactant.getSpecies());
						newReactant.setAnnotation(null);
						newReaction.addReactant(newReactant);
						
						//put this reactant in place of the get2DArrayElement function call
						for (int i = 0; i < reactantHeadNode.getChildCount(); ++i)
							reactantHeadNode.removeChild(i);
						
						for (int i = 0; i < reactantHeadNode.getChildCount(); ++i)
							reactantHeadNode.removeChild(i);
						
						reactantHeadNode.setVariable(newReactant.getSpeciesInstance());
					}// end looping through reactants
					
					//loop through all modifiers
					for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
						
						
					}					
					
					//loop through all products
					for (SpeciesReference product : reaction.getListOfProducts()) {
						
						//find offsets
						int rowOffset = 0;
						int colOffset = 0;
						
						ASTNode productHeadNode = null;
						
						//go through the get2DArrayElement nodes and find the one corresponding to the product
						for (ASTNode headNode : get2DArrayElementNodes) {
							
							//make sure it's a product node
							//only the product has children, as the reactant's children get deleted
							if (headNode.getChildCount() > 0 &&
									model.getParameter(headNode.getLeftChild().getName()) == null) {
								
								productHeadNode = headNode;
								break;
							}
						}
						
						if (productHeadNode.getChild(1).getType().name().equals("PLUS"))							
							rowOffset = productHeadNode.getChild(1).getRightChild().getInteger();
						else if (productHeadNode.getChild(1).getType().name().equals("MINUS"))							
							rowOffset = -1 * productHeadNode.getChild(1).getRightChild().getInteger();
						
						if (productHeadNode.getChild(2).getType().name().equals("PLUS"))							
							colOffset = productHeadNode.getChild(2).getRightChild().getInteger();
						else if (productHeadNode.getChild(2).getType().name().equals("MINUS"))							
							colOffset = -1 * productHeadNode.getChild(2).getRightChild().getInteger();
											
						//don't create reactions with products that don't exist 
						if (row + rowOffset < reactantDimensions.numRowsLower || 
								col + colOffset < reactantDimensions.numColsLower ||
								row + rowOffset > reactantDimensions.numRowsUpper ||
								col + colOffset > reactantDimensions.numColsUpper) {
							
							abort = true;
							break;
						}
						
						//create a new product and add it to the new reaction
						SpeciesReference newProduct = new SpeciesReference();
						newProduct = product.clone();
						newProduct.setSpecies("ROW" + (row + rowOffset) + "_COL" + (col + colOffset) + "__" + newProduct.getSpecies());
						newProduct.setAnnotation(null);
						newReaction.addProduct(newProduct);
						
						//put this reactant in place of the get2DArrayElement function call
						for (int i = 0; i < productHeadNode.getChildCount(); ++i)
							productHeadNode.removeChild(i);
						
						//put this reactant in place of the get2DArrayElement function call
						for (int i = 0; i < productHeadNode.getChildCount(); ++i)
							productHeadNode.removeChild(i);					
						
						productHeadNode.setVariable(newProduct.getSpeciesInstance());
					} //end looping through products
					
					if (newReaction.getKineticLaw().getLocalParameter("i") != null)
						newReaction.getKineticLaw().removeLocalParameter("i");
					
					if (newReaction.getKineticLaw().getLocalParameter("j") != null)
						newReaction.getKineticLaw().removeLocalParameter("j");
					
					if (abort == false)
						reactionsToAdd.add(newReaction);
					else
						abort = false;
				}
				}
				
			}
		}// end looping through reactions
		
		//add in the new explicit array reactions
		for (Reaction reactionToAdd : reactionsToAdd)
			model.addReaction(reactionToAdd);
		
		ListOf<Reaction> allReactions = model.getListOfReactions();
		
		//remove original array reaction(s)
		for (String reactionToRemove : reactionsToRemove)
			allReactions.remove(reactionToRemove);
		
		model.setListOfReactions(allReactions);
		
		ListOf<Species> allSpecies = model.getListOfSpecies();
		
		//remove the original array species from the model
		for (String speciesID : speciesToRemove)
			allSpecies.remove(speciesID);
		
		model.setListOfSpecies(allSpecies);
		
//		for (Reaction reaction : model.getListOfReactions()) {
//			System.out.println("id " + reaction.getId());
//			for (SpeciesReference reactant : reaction.getListOfReactants())
//				System.out.println(reactant.getSpecies());
//			for (SpeciesReference reactant : reaction.getListOfProducts())
//				System.out.println(reactant.getSpecies());
//		}
		
		SBMLWriter writer = new SBMLWriter();
		PrintStream p;
		
		try {
			p = new PrintStream(new FileOutputStream(filename), true, "UTF-8");
			p.print(writer.writeSBMLToString(model.getSBMLDocument()));
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * returns a set of all the reactions that the recently performed reaction affects
	 * "affect" means that the species updates will change the affected reaction's propensity
	 * 
	 * @param selectedReactionID the reaction that was recently performed
	 * @return the set of all reactions that the performed reaction affects the propensity of
	 */
	protected HashSet<String> getAffectedReactionSet(String selectedReactionID, final boolean noAssignmentRulesFlag) {
		
		HashSet<String> affectedReactionSet = new HashSet<String>(20);
		affectedReactionSet.add(selectedReactionID);
		
		//loop through the reaction's reactants and products
		for (StringDoublePair speciesAndStoichiometry : reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID)) {
			
			String speciesID = speciesAndStoichiometry.string;
			affectedReactionSet.addAll(speciesToAffectedReactionSetMap.get(speciesID));
			
			//if the species is involved in an assignment rule then it its changing may affect a reaction's propensity
			if (noAssignmentRulesFlag == false && variableToIsInAssignmentRuleMap.get(speciesID)) {
				
				//this assignment rule is going to be evaluated, so the rule's variable's value will change
				for (AssignmentRule assignmentRule : variableToAffectedAssignmentRuleSetMap.get(speciesID)) {
				
					affectedReactionSet.addAll(speciesToAffectedReactionSetMap
							.get(assignmentRule.getVariable()));
				}
			}
		}
		
		return affectedReactionSet;
	}
	
	/**
	 * kind of a hack to mingle doubles and booleans for the expression evaluator
	 * 
	 * @param value the double to be translated to a boolean
	 * @return the translated boolean value
	 */
	protected boolean getBooleanFromDouble(double value) {
		
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
	protected double getDoubleFromBoolean(boolean value) {
		
		if (value == true)
			return 1.0;
		else 
			return 0.0;
	}

	/**
	 * recursively puts the nodes that have the same name as the quarry string passed in into the arraylist passed in
	 * so, the entire tree is searched through, which i don't think is possible with the jsbml methods
	 * 
	 * @param node node to search through
	 * @param quarry string to search for
	 * @param satisfyingNodes list of nodes that satisfy the condition
	 */
	static void getSatisfyingNodes(ASTNode node, String quarry, ArrayList<ASTNode> satisfyingNodes) {
		
		if (node.isName() && node.getName().equals(quarry))
			satisfyingNodes.add(node);
		else {
			for (ASTNode childNode : node.getChildren())
				getSatisfyingNodes(childNode, quarry, satisfyingNodes);
		}		
	}
	
	/**
	 * updates the event queue and fires events and so on
	 * @param currentTime the current time in the simulation
	 */
	protected void handleEvents(final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {
		
		HashSet<String> triggeredEvents = new HashSet<String>();
		
		//loop through all untriggered events
		//if any trigger, evaluate the fire time(s) and add them to the queue
		for (String untriggeredEventID : untriggeredEventSet) {
			
			//if the trigger evaluates to true
			if (getBooleanFromDouble(evaluateExpressionRecursive(eventToTriggerMap.get(untriggeredEventID))) == true) {
				
				//skip the event if it's initially true and this is time == 0
				if (currentTime == 0.0 && eventToTriggerInitiallyTrueMap.get(untriggeredEventID) == true)
					continue;
				
				//switch from false to true must happen
				if (eventToPreviousTriggerValueMap.get(untriggeredEventID) == true)
					continue;
				
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
	}
		
	/**
	 * fires events
	 * 
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 */
	protected HashSet<String> fireEvents(final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {
		
		//temporary set of events to remove from the triggeredEventQueue
		HashSet<String> untriggeredEvents = new HashSet<String>();
		
		//loop through all triggered events
		//if they aren't persistent and the trigger is no longer true
		//remove from triggered queue and put into untriggered set
		for (EventToFire triggeredEvent : triggeredEventQueue) {
			
			String triggeredEventID = triggeredEvent.eventID;
			
			//if the trigger evaluates to false and the trigger isn't persistent
			if (eventToTriggerPersistenceMap.get(triggeredEventID) == false && 
					getBooleanFromDouble(evaluateExpressionRecursive(eventToTriggerMap.get(triggeredEventID))) == false) {
				
				untriggeredEvents.add(triggeredEventID);
				eventToPreviousTriggerValueMap.put(triggeredEventID, false);
			}
		}
		
		triggeredEventQueue.removeAll(untriggeredEvents);
		
		//loop through untriggered events
		//if the trigger is no longer true
		//set the previous trigger value to false
		for (String untriggeredEventID : untriggeredEventSet) {
			
			if (getBooleanFromDouble(evaluateExpressionRecursive(eventToTriggerMap.get(untriggeredEventID))) == false)
				eventToPreviousTriggerValueMap.put(untriggeredEventID, false);			
		}
		
		//these are sets of things that need to be re-evaluated or tested due to the event firing
		HashSet<String> affectedReactionSet = new HashSet<String>();
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();
		
		//set of fired events to add to the untriggered set
		HashSet<String> firedEvents = new HashSet<String>();
		
		//fire all events whose fire time is less than the current time	
		while (triggeredEventQueue.size() > 0 && triggeredEventQueue.peek().fireTime <= currentTime) {
			
			EventToFire eventToFire = triggeredEventQueue.poll();
			String eventToFireID = eventToFire.eventID;
			affectedReactionSet.addAll(eventToAffectedReactionSetMap.get(eventToFireID));
			
			firedEvents.add(eventToFireID);
			eventToPreviousTriggerValueMap.put(eventToFireID, true);
			
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
				
				//update the species, but only if it's not a constant
				if (variableToIsConstantMap.get(variable) == false) {
					
					if (eventToFire.eventID.contains("__Division__")) {
						
						String compartmentID = ((String[])eventToFire.eventID.split("__"))[0];
						duplicateComponent(compartmentID, eventToFire.eventID);
					}
						
					if (speciesToHasOnlySubstanceUnitsMap.get(variable) != null && 
							speciesToHasOnlySubstanceUnitsMap.get(variable) == false)
						variableToValueMap.put(variable, 
								(int)(assignmentValue / speciesToCompartmentSizeMap.get(variable)));				
					else				
						variableToValueMap.put(variable, assignmentValue);
				}
				
				//if this variable that was just updated is part of an assignment rule (RHS)
				//then re-evaluate that assignment rule
				if (noAssignmentRulesFlag == false && variableToIsInAssignmentRuleMap.get(variable) == true) 
					affectedAssignmentRuleSet.addAll(variableToAffectedAssignmentRuleSetMap.get(variable));
				
				if (noConstraintsFlag == false && variableToIsInConstraintMap.get(variable) == true)
					affectedConstraintSet.addAll(variableToAffectedConstraintSetMap.get(variable));
			}
		}
		
		//add the fired events back into the untriggered set
		//this allows them to trigger/fire again later
		untriggeredEventSet.addAll(firedEvents);
		
		if (affectedAssignmentRuleSet.size() > 0)
			performAssignmentRules(affectedAssignmentRuleSet);
		
		if (affectedConstraintSet.size() > 0) {
			
			if (testConstraints(affectedConstraintSet) == false)
				constraintFailureFlag = true;
		}
		
		return affectedReactionSet;
	}
	
	/**
	 * performs assignment rules that may have changed due to events or reactions firing
	 * 
	 * @param affectedAssignmentRuleSet the set of assignment rules that have been affected
	 */
	protected void performAssignmentRules(HashSet<AssignmentRule> affectedAssignmentRuleSet) {
		
		for (AssignmentRule assignmentRule : affectedAssignmentRuleSet) {
			
			String variable = assignmentRule.getVariable();
			
			//update the species count (but only if the species isn't constant)
			if (variableToIsConstantMap.get(variable) == false) {
				
				if (speciesToHasOnlySubstanceUnitsMap.get(variable) == false) {
					variableToValueMap.put(variable, 
							(int)(evaluateExpressionRecursive(assignmentRule.getMath()) / speciesToCompartmentSizeMap.get(variable)));
				}
				else
					variableToValueMap.put(variable, evaluateExpressionRecursive(assignmentRule.getMath()));
			}
		}
	}
	
	/**
	 * updates reactant/product species counts based on their stoichiometries
	 * 
	 * @param selectedReactionID the reaction to perform
	 */
	protected void performReaction(String selectedReactionID, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {
		
		//these are sets of things that need to be re-evaluated or tested due to the reaction firing
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();
		
		//loop through the reaction's reactants and products and update their amounts
		for (StringDoublePair speciesAndStoichiometry : reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID)) {
			
			double stoichiometry = speciesAndStoichiometry.doub;
			String speciesID = speciesAndStoichiometry.string;
			
			//update the species count if the species isn't a boundary condition or constant
			//note that the stoichiometries are earlier modified with the correct +/- sign
			if (speciesToIsBoundaryConditionMap.get(speciesID) == false &&
					variableToIsConstantMap.get(speciesID) == false) {
				
				if (speciesToHasOnlySubstanceUnitsMap.get(speciesID) == false)
					variableToValueMap.adjustValue(speciesID, 
							(int)(stoichiometry / speciesToCompartmentSizeMap.get(speciesID)));				
				else				
					variableToValueMap.adjustValue(speciesID, stoichiometry);
			}
			
			//if this variable that was just updated is part of an assignment rule (RHS)
			//then re-evaluate that assignment rule
			if (noAssignmentRulesFlag == false && variableToIsInAssignmentRuleMap.get(speciesID) == true)
				affectedAssignmentRuleSet.addAll(variableToAffectedAssignmentRuleSetMap.get(speciesID));
			
			if (noConstraintsFlag == false && variableToIsInConstraintMap.get(speciesID) == true)
				affectedConstraintSet.addAll(variableToAffectedConstraintSetMap.get(speciesID));
		}
		
		if (affectedAssignmentRuleSet.size() > 0)
			performAssignmentRules(affectedAssignmentRuleSet);
		
		if (affectedConstraintSet.size() > 0) {
			
			if (testConstraints(affectedConstraintSet) == false)
				constraintFailureFlag = true;
		}
	}
	
	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException 
	 */
	protected void printToTSD(double printTime) throws IOException {
		
		String commaSpace = "";
		
		//dynamic printing requires re-printing the species values each time step
		if (printTime > 0.0 && dynamicBoolean == true) {
		
			bufferedTSDWriter.write("(\"time\"");
			
			commaSpace = ", ";
			
			//print the species IDs
			for (String speciesID : speciesIDSet)
				bufferedTSDWriter.write(commaSpace + "\"" + speciesID + "\"");
			
			//print compartment location IDs
			for (String componentLocationID : componentToLocationMap.keySet()) {
				
				String locationX = componentLocationID + "__locationX";
				String locationY = componentLocationID + "__locationY";
				
				bufferedTSDWriter.write(commaSpace + "\"" + locationX + "\", \"" + locationY + "\"");
			}			
			
			bufferedTSDWriter.write("),\n");
		
		}
		
		bufferedTSDWriter.write("(");
		
		commaSpace = "";
		
		//print the current time
		bufferedTSDWriter.write(printTime + ", ");
		
		//loop through the speciesIDs and print their current value to the file
		for (String speciesID : speciesIDSet) {
			
			bufferedTSDWriter.write(commaSpace + (int) variableToValueMap.get(speciesID));
			commaSpace = ", ";
		}
		
		//print component location values
		for (String componentID : componentToLocationMap.keySet()) {
			bufferedTSDWriter.write(commaSpace + (int) componentToLocationMap.get(componentID).getX());
			bufferedTSDWriter.write(commaSpace + (int) componentToLocationMap.get(componentID).getY());
		}
		
		bufferedTSDWriter.write(")");
	}
	
	/**
	 * parses TSD files for all runs and prints mean and standard deviation TSDs
	 * 
	 * @throws IOException
	 */
	protected void printStatisticsTSD() throws IOException {
		
		//the last run is the number of runs
		int numRuns = currentRun;
		
		HashMap<String, ArrayList<DescriptiveStatistics> > speciesStatistics = 
			new HashMap<String, ArrayList<DescriptiveStatistics> >();
		
		ArrayList<String> allSpecies = new ArrayList<String>();
		
		//store the TSD data for analysis
		for (int run = 1; run <= numRuns; ++run) {
			
			DTSDParser dtsdParser = new DTSDParser(outputDirectory + "run-" + run + ".dtsd");
			
			TSDParser tsdParser = new TSDParser(outputDirectory + "run-" + run + ".tsd" , false);
			allSpecies = tsdParser.getSpecies();
			
			HashMap<String, ArrayList<Double> > runStatistics = tsdParser.getHashMap();
			
			for (int speciesIndex = 0; speciesIndex < allSpecies.size(); ++speciesIndex) {
			
				String species = allSpecies.get(speciesIndex);
				
				for (int index = 0; index < runStatistics.get(species).size(); ++index) {
					
					Double speciesData = runStatistics.get(species).get(index);
					
					if (speciesStatistics.size() <= speciesIndex)
						speciesStatistics.put(species, new ArrayList<DescriptiveStatistics>());
					
					if (speciesStatistics.get(species).size() <= index)
						speciesStatistics.get(species).add(new DescriptiveStatistics());
					
					speciesStatistics.get(species).get(index).addValue(speciesData.doubleValue());
				}
			}
		}
		
		DataParser statsParser = new DataParser(null, null);
		ArrayList<ArrayList<Double> > meanTSDData = new ArrayList<ArrayList<Double> >();		
		
		//calculate and print the mean tsd
		for (String species : allSpecies) {
			
			ArrayList<Double> speciesData = new ArrayList<Double>();
			
			for (int index = 0; index < speciesStatistics.get(species).size(); ++index)
				speciesData.add(speciesStatistics.get(species).get(index).getMean());
			
			meanTSDData.add(speciesData);
		}
		
		statsParser.setData(meanTSDData);
		statsParser.setSpecies(allSpecies);
		statsParser.outputTSD(outputDirectory + "mean.tsd");
		
		//calculate and print the standard deviation tsd
		ArrayList<ArrayList<Double> > standardDeviationTSDData = new ArrayList<ArrayList<Double> >();
		
		for (String species : allSpecies) {
			
			ArrayList<Double> speciesData = new ArrayList<Double>();
			
			if (species.equals("time")) {
				
				for (int index = 0; index < speciesStatistics.get(species).size(); ++index)
					speciesData.add(speciesStatistics.get(species).get(index).getMean());
			}
			else {
				
				for (int index = 0; index < speciesStatistics.get(species).size(); ++index)
					speciesData.add(speciesStatistics.get(species).get(index).getStandardDeviation());
			}
			
			standardDeviationTSDData.add(speciesData);
		}
		
		statsParser.setData(standardDeviationTSDData);
		statsParser.setSpecies(allSpecies);
		statsParser.outputTSD(outputDirectory + "standard_deviation.tsd");		
		
		
		//calculate and print the variance tsd
		ArrayList<ArrayList<Double> > varianceTSDData = new ArrayList<ArrayList<Double> >();
		
		for (String species : allSpecies) {
			
			ArrayList<Double> speciesData = new ArrayList<Double>();
			
			if (species.equals("time")) {
				
				for (int index = 0; index < speciesStatistics.get(species).size(); ++index)
					speciesData.add(speciesStatistics.get(species).get(index).getMean());
			}
			else {
				
				for (int index = 0; index < speciesStatistics.get(species).size(); ++index)
					speciesData.add(speciesStatistics.get(species).get(index).getVariance());
			}
			
			varianceTSDData.add(speciesData);
		}
		
		statsParser.setData(varianceTSDData);
		statsParser.setSpecies(allSpecies);
		statsParser.outputTSD(outputDirectory + "variance.tsd");
	}
	
	/**
	 * puts array stuff into data structures to be used during simulation
	 */
	protected void setupArrays() throws IOException {
				
		//ARRAYED SPECIES BUSINESS
		//create all new species that are implicit in the arrays and put them into the model	
		
		ArrayList<String> speciesToRemove = new ArrayList<String>();
		ArrayList<Species> speciesToAdd = new ArrayList<Species>();
		
		HashMap<String, Boolean> speciesToIsArrayedMap = new HashMap<String, Boolean>();
		HashMap<String, SpeciesDimensions> arrayedSpeciesToDimensionsMap = 
			new HashMap<String, SpeciesDimensions>();
		
		for (Species species : model.getListOfSpecies()) {
			
			String speciesID = species.getId();
			
			//check to see if the species is arrayed			
			if (species.getAnnotationString().isEmpty() == false) {
				
				speciesToIsArrayedMap.put(speciesID, true);
				speciesToRemove.add(speciesID);
				
				int numRowsLower = 0;
				int numColsLower = 0;
				int numRowsUpper = 0;
				int numColsUpper = 0;
				
				String[] annotationString = species.getAnnotationString().split("=");
				
				numRowsLower = Integer.valueOf(((String[])(annotationString[1].split(" ")))[0].replace("\"",""));
				numRowsUpper = Integer.valueOf(((String[])(annotationString[2].split(" ")))[0].replace("\"",""));
				numColsLower = Integer.valueOf(((String[])(annotationString[3].split(" ")))[0].replace("\"",""));
				numColsUpper = Integer.valueOf(((String[])(annotationString[4].split(" ")))[0].replace("\"",""));
				
				minRow = numRowsLower;
				maxRow = numRowsUpper;
				minCol = numColsLower;
				maxCol = numColsUpper;
				
				SpeciesDimensions speciesDimensions = 
					new SpeciesDimensions(numRowsLower, numRowsUpper, numColsLower, numColsUpper);
				
				arrayedSpeciesToDimensionsMap.put(speciesID, speciesDimensions);
				
				//loop through all species in the array
				//prepend the row/col information to create a new ID
				for (int row = numRowsLower; row <= numRowsUpper; ++row) {
					for (int col = numColsLower; col <= numColsUpper; ++col) {
						
						speciesID = "ROW" + row + "_COL" + col + "__" + species.getId();
						
						setupSingleSpecies(species, speciesID);
						
						//the species need to be added to the model so that JSBML can process
						//the speciesreferences in the kinetic laws of the new reactions						
						Species newSpecies = new Species();
						newSpecies = species.clone();
						newSpecies.setMetaId(speciesID);
						newSpecies.setId(speciesID);
						newSpecies.setAnnotation(null);
						
						speciesToAdd.add(newSpecies);
						//speciesToRemove.add(speciesID);
					}
				}
			}
			else
				speciesToIsArrayedMap.put(speciesID, false);
		} //end species for loop
		
		for (Species specToAdd : speciesToAdd)
			model.addSpecies(specToAdd);
		
		
		//ARRAYED EVENTS BUSINESS
		
		ArrayList<String> eventsToRemove = new ArrayList<String>();
		
		for (Event event : model.getListOfEvents()) {
			
			if (event.getAnnotationString().contains("array")) {
				
				eventsToRemove.add(event.getId());
				
				String annotationString = event.getAnnotationString().replace("<annotation>","").
				replace("</annotation>","").replace("\"","");
				String[] splitAnnotation = annotationString.split("array:");
				ArrayList<String> eventCompartments = new ArrayList<String>();
				
				splitAnnotation[splitAnnotation.length - 2] = ((String[])splitAnnotation[splitAnnotation.length - 2].split("xmlns:"))[0];
				
				for (int i = 2; i < splitAnnotation.length - 1; ++i) {
					
					String compartmentID = ((String[])splitAnnotation[i].split("="))[0];
					eventCompartments.add(compartmentID);
				}
				
				//loop through all compartments and create an event for each one
				for (String compartmentID : eventCompartments) {
					
					Event newEvent = new Event();
					newEvent.setVersion(event.getVersion());
					newEvent.setLevel(event.getLevel());
					newEvent.setId(compartmentID + "__" + event.getId());
					newEvent.setTrigger(event.getTrigger().clone());
					
					if (event.isSetPriority())
						newEvent.setPriority(event.getPriority().clone());
					
					if (event.isSetDelay())
						newEvent.setDelay(event.getDelay().clone());
					
					newEvent.setUseValuesFromTriggerTime(event.getUseValuesFromTriggerTime());
					
					ArrayList<String> eventAssignmentsToRemove = new ArrayList<String>();
					
					for (EventAssignment eventAssignment : event.getListOfEventAssignments()) {
						
						EventAssignment ea = eventAssignment.clone();
						ea.setMath(eventAssignment.getMath().clone());
						ea.setVariable(eventAssignment.getVariable());
						newEvent.addEventAssignment(ea);
					}
					
					for (EventAssignment eventAssignment : newEvent.getListOfEventAssignments()) {
						
						if (eventAssignment.getVariable().contains("_size")) {
							
							eventAssignmentsToRemove.add(eventAssignment.getVariable());
							continue;
						}
							
						eventAssignment.setVariable(compartmentID + "__" + eventAssignment.getVariable());
						
						//prepends the compartment ID to all variables in the event assignment
						prependToVariableNodes(eventAssignment.getMath(), compartmentID + "__");
					}
					
					for (String eaToRemove : eventAssignmentsToRemove)
						newEvent.removeEventAssignment(eaToRemove);
					
					setupSingleEvent(newEvent);
				}
			}
		}	
		
		
		//ARRAYED REACTION BUSINESS
		//if a reaction has arrayed species
		//new reactions that are implicit are created and added to the model	
		
		ArrayList<String> reactionsToRemove = new ArrayList<String>();
		
		for (Reaction reaction : model.getListOfReactions()) {
			
			String reactionID = reaction.getId();
			
			//if reaction itself is arrayed
			if (reaction.getAnnotationString().isEmpty() == false) {				
			}
			
			ArrayList<Integer> membraneDiffusionRows = new ArrayList<Integer>();
			ArrayList<Integer> membraneDiffusionCols = new ArrayList<Integer>();
			ArrayList<String> membraneDiffusionCompartments = new ArrayList<String>();
			
			//MEMBRANE DIFFUSION REACTIONS
			//if it's a membrane diffusion reaction it'll have the appropriate locations as an annotation
			//so parse them and store them in the above arraylists
			if (reactionID.contains("MembraneDiffusion")) {
				
				String annotationString = reaction.getAnnotationString().replace("<annotation>","").
					replace("</annotation>","").replace("\"","");
				String[] splitAnnotation = annotationString.split("array:");
				
				splitAnnotation[splitAnnotation.length - 2] = ((String[])splitAnnotation[splitAnnotation.length - 2].split("xmlns:"))[0];
				
				for (int i = 2; i < splitAnnotation.length - 1; ++i) {
					
					String compartmentID = ((String[])splitAnnotation[i].split("="))[0];
					String row = ((String[])((String[])splitAnnotation[i].split("="))[1].split(","))[0].replace("(","");
					String col = ((String[])((String[])splitAnnotation[i].split("="))[1].split(","))[1].replace(")","");
					
					membraneDiffusionRows.add(Integer.valueOf(row.trim()));
					membraneDiffusionCols.add(Integer.valueOf(col.trim()));
					membraneDiffusionCompartments.add(compartmentID);
				}
				
				int membraneDiffusionIndex = 0;
				
				reactionsToRemove.add(reaction.getId());
				reaction.setAnnotation(null);
				
				//loop through all appropriate row/col pairs and create a membrane diffusion reaction for each one
				for (String compartmentID : membraneDiffusionCompartments) {
					
					int row = membraneDiffusionRows.get(membraneDiffusionIndex);
					int col = membraneDiffusionCols.get(membraneDiffusionIndex);
					
					String newReactionID = compartmentID  + "__" + reactionID;
					KineticLaw newReactionKineticLaw = reaction.getKineticLaw().clone();
					ASTNode newReactionFormula = newReactionKineticLaw.getMath();
					boolean reversible = true;
					ListOf<SpeciesReference> reactantsList = new ListOf<SpeciesReference>();
					ListOf<SpeciesReference> productsList = new ListOf<SpeciesReference>();
					ListOf<ModifierSpeciesReference> modifiersList = new ListOf<ModifierSpeciesReference>();
					
					reactantsList.setVersion(reaction.getVersion());
					reactantsList.setLevel(reaction.getLevel());
					productsList.setVersion(reaction.getVersion());
					productsList.setLevel(reaction.getLevel());
					modifiersList.setVersion(reaction.getVersion());
					modifiersList.setLevel(reaction.getLevel());
					
					//alter the kinetic law to so that it has the correct indexes as children for the
					//get2DArrayElement function
					
					//get the nodes to alter (that are arguments for the get2DArrayElement method)
					ArrayList<ASTNode> get2DArrayElementNodes = new ArrayList<ASTNode>();
					
					getSatisfyingNodes(newReactionFormula, "get2DArrayElement", get2DArrayElementNodes);
					
					boolean reactantBool = false;
					
					//replace the get2darrayelement stuff with the proper explicit species/parameters
					for (ASTNode node : get2DArrayElementNodes) {
												
						if (node.getLeftChild().getName().contains("kmdiff")) {
							
							String parameterName = node.getLeftChild().getName();
							
							String speciesID = reactionID.replace("MembraneDiffusion_","");
							
							if (model.getParameter(compartmentID + "__" + speciesID + "__" + parameterName) == null)
								node.setVariable(model.getParameter(parameterName));
							else							
								node.setVariable(model.getParameter(compartmentID + "__" + speciesID + "__" + parameterName));	
							
							for (int i = 0; i < node.getChildCount(); ++i)
								node.removeChild(i);
							
							for (int i = 0; i < node.getChildCount(); ++i)
								node.removeChild(i);
						}
						//this means it's a species, which we need to prepend with the row/col prefix
						else {
							
							if (node.getChildCount() > 0 &&
									model.getParameter(node.getLeftChild().getName()) == null) {
								
								//reactant
								if (reactantBool == true) {
									
									node.setVariable(model.getSpecies(
											"ROW" + row + "_COL" + col + "__" + node.getLeftChild().getName()));
								}
								//product
								else {
									
									node.setVariable(model.getSpecies(
											compartmentID + "__" + node.getLeftChild().getName()));
									reactantBool = true;
								}
							}
								
							for (int i = 0; i < node.getChildCount(); ++i)
								node.removeChild(i);
							
							for (int i = 0; i < node.getChildCount(); ++i)
								node.removeChild(i);
						}
					}
					
					//loop through reactants
					for (SpeciesReference reactant : reaction.getListOfReactants()) {
						
						Species species = model.getSpecies(compartmentID + "__" + reactant.getSpecies());
						
						SpeciesReference sr = new SpeciesReference(species);								
						sr.setVersion(reaction.getVersion());
						sr.setLevel(reaction.getLevel());
						
						//this species hasn't been setup yet because it isn't a grid species
						setupSingleSpecies(species, species.getId());
						speciesToRemove.add(species.getId());
						
						//these parameters need to be setup because they're not local parameters
						setupSingleParameter(model.getParameter(compartmentID + "__" + reactant.getSpecies() + "__kmdiff_r"));
						setupSingleParameter(model.getParameter(compartmentID + "__" + reactant.getSpecies() + "__kmdiff_f"));
						
						reactantsList.add(sr);
					}
					
					//loop through products
					for (SpeciesReference product : reaction.getListOfProducts()) {
						
						SpeciesReference sr = new SpeciesReference(
								model.getSpecies("ROW" + row + "_COL" + col + "__" + product.getSpecies()));
						sr.setVersion(reaction.getVersion());
						sr.setLevel(reaction.getLevel());
						
						productsList.add(sr);
					}
					
					if (newReactionKineticLaw.getLocalParameter("i") != null)
						newReactionKineticLaw.removeLocalParameter("i");
					
					if (newReactionKineticLaw.getLocalParameter("j") != null)
						newReactionKineticLaw.removeLocalParameter("j");
					
					setupLocalParameters(newReactionKineticLaw, newReactionID);
					
					//initialize and calculate the propensity of this new membrane diffusion reaction
					setupSingleReaction(newReactionID, newReactionFormula, reversible, 
							reactantsList, productsList, modifiersList);
					
					++membraneDiffusionIndex;
				}
			}
			
			
			
			//NON-MEMBRANE DIFFUSION REACTIONS
			//check to see if the (non-membrane-diffusion) reaction has arrayed species
			//right now i'm only checking the first reactant species, due to a bad assumption
			//about the homogeneity of the arrayed reaction (ie, if one species is arrayed, they all are)
			else if (reaction.getNumReactants() > 0 &&
					speciesToIsArrayedMap.get(reaction.getReactant(0).getSpeciesInstance().getId()) == true) {
				
				reactionsToRemove.add(reaction.getId());
				
				//get the reactant dimensions, which tells us how many new reactions are going to be created
				SpeciesDimensions reactantDimensions = 
					arrayedSpeciesToDimensionsMap.get(reaction.getReactant(0).getSpeciesInstance().getId());
				
				boolean abort = false;
				
				//loop through all of the new formerly-implicit reactants
				for (int row = reactantDimensions.numRowsLower; row <= reactantDimensions.numRowsUpper; ++row) {
				for (int col = reactantDimensions.numColsLower; col <= reactantDimensions.numColsUpper; ++col) {
					
					String newReactionID = "ROW" + row + "_COL" + col  + "_" + reactionID;
					KineticLaw newReactionKineticLaw = reaction.getKineticLaw().clone();
					ASTNode newReactionFormula = newReactionKineticLaw.getMath().clone();
					boolean reversible = false;
					ListOf<SpeciesReference> reactantsList = new ListOf<SpeciesReference>();
					ListOf<SpeciesReference> productsList = new ListOf<SpeciesReference>();
					ListOf<ModifierSpeciesReference> modifiersList = new ListOf<ModifierSpeciesReference>();
					
					reactantsList.setVersion(reaction.getVersion());
					reactantsList.setLevel(reaction.getLevel());
					productsList.setVersion(reaction.getVersion());
					productsList.setLevel(reaction.getLevel());
					modifiersList.setVersion(reaction.getVersion());
					modifiersList.setLevel(reaction.getLevel());
					
					//get the nodes to alter
					ArrayList<ASTNode> get2DArrayElementNodes = new ArrayList<ASTNode>();
					
					//return the head node of the get2DArrayElement function
					getSatisfyingNodes(newReactionFormula, "get2DArrayElement", get2DArrayElementNodes);
					
					//loop through all reactants
					for (SpeciesReference reactant : reaction.getListOfReactants()) {
						
						//find offsets
						//the row offset is in the kinetic law via i
						//the col offset is in the kinetic law via j
						int rowOffset = 0;
						int colOffset = 0;
						
						ASTNode reactantHeadNode = null;
						
						//go through the get2DArrayElement nodes and find the one corresponding to the reactant
						for (ASTNode headNode : get2DArrayElementNodes) {
							
							//make sure it's a reactant node
							if (headNode.getChildCount() > 0 &&
									model.getParameter(headNode.getLeftChild().getName()) == null) {
								
								reactantHeadNode = headNode;
								break;
							}
						}
						
						if (reactantHeadNode.getChild(1).getType().name().equals("PLUS"))							
							rowOffset = reactantHeadNode.getChild(1).getRightChild().getInteger();
						else if (reactantHeadNode.getChild(1).getType().name().equals("MINUS"))							
							rowOffset = -1 * reactantHeadNode.getChild(1).getRightChild().getInteger();
						
						if (reactantHeadNode.getChild(2).getType().name().equals("PLUS"))				
							colOffset = reactantHeadNode.getChild(2).getRightChild().getInteger();
						else if (reactantHeadNode.getChild(2).getType().name().equals("MINUS"))				
							colOffset = -1 * reactantHeadNode.getChild(2).getRightChild().getInteger();
						
						SpeciesReference sr = new SpeciesReference(model.getSpecies(
								"ROW" + (row + rowOffset) + "_COL" + (col + colOffset) + "__" + reactant.getSpecies()));
						sr.setLevel(reaction.getLevel());
						sr.setVersion(reaction.getVersion());
						
						reactantsList.add(sr);
						
						//put this reactant in place of the get2DArrayElement function call
						for (int i = 0; i < reactantHeadNode.getChildCount(); ++i)
							reactantHeadNode.removeChild(i);
						
						for (int i = 0; i < reactantHeadNode.getChildCount(); ++i)
							reactantHeadNode.removeChild(i);
						
						reactantHeadNode.setVariable(model.getSpecies(
								"ROW" + (row + rowOffset) + "_COL" + (col + colOffset) + "__" + reactant.getSpecies()));
					}// end looping through reactants
					
					//loop through all modifiers
					for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
						
						
					}					
					
					//loop through all products
					for (SpeciesReference product : reaction.getListOfProducts()) {
						
						//find offsets
						int rowOffset = 0;
						int colOffset = 0;
						
						ASTNode productHeadNode = null;
						
						//go through the get2DArrayElement nodes and find the one corresponding to the product
						for (ASTNode headNode : get2DArrayElementNodes) {
							
							//make sure it's a product node
							//only the product has children, as the reactant's children get deleted
							if (headNode.getChildCount() > 0 &&
									model.getParameter(headNode.getLeftChild().getName()) == null) {
								
								productHeadNode = headNode;
								break;
							}
						}
						
						if (productHeadNode.getChild(1).getType().name().equals("PLUS"))							
							rowOffset = productHeadNode.getChild(1).getRightChild().getInteger();
						else if (productHeadNode.getChild(1).getType().name().equals("MINUS"))							
							rowOffset = -1 * productHeadNode.getChild(1).getRightChild().getInteger();
						
						if (productHeadNode.getChild(2).getType().name().equals("PLUS"))							
							colOffset = productHeadNode.getChild(2).getRightChild().getInteger();
						else if (productHeadNode.getChild(2).getType().name().equals("MINUS"))							
							colOffset = -1 * productHeadNode.getChild(2).getRightChild().getInteger();
											
						//don't create reactions with products that don't exist 
						if ((row + rowOffset) < reactantDimensions.numRowsLower || 
								(col + colOffset) < reactantDimensions.numColsLower ||
								(row + rowOffset) > reactantDimensions.numRowsUpper ||
								(col + colOffset) > reactantDimensions.numColsUpper) {
							
							abort = true;
							break;
						}
						
						SpeciesReference sr = new SpeciesReference(model.getSpecies("ROW" + (row + rowOffset) + "_COL" + (col + colOffset) 
								+ "__" + product.getSpecies()));
						sr.setLevel(reaction.getLevel());
						sr.setVersion(reaction.getVersion());
						
						productsList.add(sr);						
						
						//put this reactant in place of the get2DArrayElement function call
						for (int i = 0; i < productHeadNode.getChildCount(); ++i)
							productHeadNode.removeChild(i);
						
						//put this reactant in place of the get2DArrayElement function call
						for (int i = 0; i < productHeadNode.getChildCount(); ++i)
							productHeadNode.removeChild(i);
						
						productHeadNode.setVariable(model.getSpecies("ROW" + (row + rowOffset) + "_COL" + (col + colOffset) 
								+ "__" + product.getSpecies()));
					} //end looping through products
					
					if (abort == false) {
						
						if (newReactionKineticLaw.getLocalParameter("i") != null)
							newReactionKineticLaw.removeLocalParameter("i");
						
						if (newReactionKineticLaw.getLocalParameter("j") != null)
							newReactionKineticLaw.removeLocalParameter("j");
						
						//this isn't a reversible reaction; only take the left side
						if (newReactionID.contains("Degradation") == false) {
							newReactionFormula = newReactionFormula.getLeftChild();
							newReactionKineticLaw.setMath(newReactionFormula);
						}
						
						setupLocalParameters(newReactionKineticLaw, newReactionID);
						
						//initialize and calculate the propensity of this new reaction
						setupSingleReaction(newReactionID, newReactionFormula, reversible, 
								reactantsList, productsList, modifiersList);
					}
					else
						abort = false;
				}
				}
				
			}
		}// end looping through reactions
		
		//remove stuff so that it isn't used in subsequent setup
		
		ListOf<Reaction> allReactions = model.getListOfReactions();
		
		//remove original array reaction(s)
		for (String reactionToRemove : reactionsToRemove)
			allReactions.remove(reactionToRemove);
		
		model.setListOfReactions(allReactions);
		
		ListOf<Species> allSpecies = model.getListOfSpecies();
		
		//remove the original array species from the model
		for (String speciesID : speciesToRemove) {
			allSpecies.remove(speciesID);
		}
		
		model.setListOfSpecies(allSpecies);
		
		ListOf<Event> allEvents = model.getListOfEvents();
		
		//remove original array event(s)
		for (String eventToRemove : eventsToRemove) {
			allEvents.remove(eventToRemove);
		}
		
		model.setListOfEvents(allEvents);
	}
	
	/**
	 * creates hashmaps for representing and keeping track of a grid (of cells)
	 */
	protected void setupGrid() {
		
		//go through all reaction IDs and group them by compartment ID
		for (String reactionID : reactionToPropensityMap.keySet()) {
			
			if (reactionID.contains("__") == false)
				continue;
			
			//this will leave us with compartmentID or stuff_compartmentID
			String componentID = ((String[])reactionID.split("__"))[0];
			
			//if there's an underscore, remove everything before it to leave the compartmentID
			String[] splitReactionID = componentID.split("_");
			componentID = splitReactionID[splitReactionID.length - 1];
			
			//the location of the component can be parsed from the membrane diffusion reaction product
			if (reactionID.contains("MembraneDiffusion")) {
				
				String productID = "";
				
				for (StringDoublePair sdp : reactionToSpeciesAndStoichiometrySetMap.get(reactionID)) {
					
					if (sdp.string.contains("ROW") && sdp.string.contains("COL"))
						productID = sdp.string;
				}
				
				String[] locationInfo = 
					((String[])productID.split("__"))[0].split("_");
				
				int row = Integer.valueOf(locationInfo[0].replace("ROW",""));
				int col = Integer.valueOf(locationInfo[1].replace("COL",""));
				
				componentToLocationMap.put(componentID, new Point(row, col));
			}				
			
			if (componentToReactionSetMap.containsKey(componentID) == false)
				componentToReactionSetMap.put(componentID, new HashSet<String>());
			
			componentToReactionSetMap.get(componentID).add(reactionID);
		}
		
		
		//go through the species and parameters and group them by compartment ID
		for (String variableID : variableToValueMap.keySet()) {
			
			if (variableID.contains("__") == false)
				continue;
			
			//this will leave us with compartmentID or stuff_compartmentID
			String componentID = ((String[])variableID.split("__"))[0];
			
			//if there's an underscore, remove everything before it to leave the compartmentID
			String[] splitVariableID = componentID.split("_");
			componentID = splitVariableID[splitVariableID.length - 1];		
			
			if (componentToVariableSetMap.containsKey(componentID) == false)
				componentToVariableSetMap.put(componentID, new HashSet<String>());
		
			componentToVariableSetMap.get(componentID).add(variableID);
		}
		
		//go through events and group them by compartment ID
		if (eventToDelayMap != null) {
			
			for (String eventID : eventToDelayMap.keySet()) {
				
				if (eventID.contains("__") == false)
					continue;
				
				String componentID = ((String[])eventID.split("__"))[0];
				
				//if there's an underscore, remove everything before it to leave the compartmentID
				String[] splitEventID = componentID.split("_");
				componentID = splitEventID[splitEventID.length - 1];
				
				if (componentToEventSetMap.containsKey(componentID) == false)
					componentToEventSetMap.put(componentID, new HashSet<String>());
				
				componentToEventSetMap.get(componentID).add(eventID);			
			}
		}
	}
	
	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	private void setupSingleSpecies(Species species, String speciesID) {
		
		if (speciesIDSet.contains(speciesID))
			return;
		
		variableToValueMap.put(speciesID, species.getInitialAmount());
		
		if (numRules > 0)
			variableToIsInAssignmentRuleMap.put(speciesID, false);
		
		if (numConstraints > 0)
			variableToIsInConstraintMap.put(speciesID, false);
		
		if (species.hasOnlySubstanceUnits() == false)
			speciesToCompartmentSizeMap.put(speciesID, species.getCompartmentInstance().getSize());
		
		speciesToAffectedReactionSetMap.put(speciesID, new HashSet<String>(20));
		speciesToIsBoundaryConditionMap.put(speciesID, species.isBoundaryCondition());
		variableToIsConstantMap.put(speciesID, species.isConstant());
		speciesToHasOnlySubstanceUnitsMap.put(speciesID, species.hasOnlySubstanceUnits());
		speciesIDSet.add(speciesID);
	}
	
	/**
	 * puts species-related information into data structures
	 * 
	 * @throws IOException
	 */
	protected void setupSpecies() throws IOException {
		
		//add values to hashmap for easy access to species amounts
		for (Species species : model.getListOfSpecies()) {
			
			//skip the grid species as these have already been setup
			if (species.getId().contains("ROW") && species.getId().contains("COL") && species.getId().contains("__"))
				continue;

			setupSingleSpecies(species, species.getId());
		}
	}

	/**
	 * puts initial assignment-related information into data structures
	 */
	protected void setupInitialAssignments() {
		
		//calculate initial assignments a lot of times in case there are dependencies
		//running it the number of initial assignments times will avoid problems
		//and all of them will be fully calculated and determined
		for (int i = 0; i < numInitialAssignments; ++i) {
			
			for (InitialAssignment initialAssignment : model.getListOfInitialAssignments()) {
				
				variableToValueMap.put(initialAssignment.getVariable(), 
						evaluateExpressionRecursive(initialAssignment.getMath()));				
			}			
		}
		
		HashSet<AssignmentRule> allAssignmentRules = new HashSet<AssignmentRule>();
		
		//perform all assignment rules
		for (Rule rule : model.getListOfRules()) {
			
			if (rule.isAssignment())
				allAssignmentRules.add((AssignmentRule)rule);
		}
		
		performAssignmentRules(allAssignmentRules);
	}
	
	/**
	 * sets up the local parameters in a single kinetic law
	 * 
	 * @param kineticLaw
	 * @param reactionID
	 */
	private void setupLocalParameters(KineticLaw kineticLaw, String reactionID) {
		
		for (LocalParameter localParameter : kineticLaw.getListOfLocalParameters()) {
			
			String parameterID = "";
			
			//the parameters don't get reset after each run, so don't re-do this prepending
			if (localParameter.getId().contains(reactionID + "_") == false)					
				parameterID = reactionID + "_" + localParameter.getId();
			else 
				parameterID = localParameter.getId();
							
			String oldParameterID = localParameter.getId();
			variableToValueMap.put(parameterID, localParameter.getValue());
						
			//alter the local parameter ID so that it goes to the local and not global value
			localParameter.setId(parameterID);
			
			//for some reason, changing the local parameter sometimes changes the kinetic law instances
			//of that parameter id, so those ones are fine and ignore them
			if (kineticLaw.getFormula().contains(parameterID) == false) {
			
				try {
					kineticLaw.setFormula(kineticLaw.getFormula().replace(oldParameterID, parameterID));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * sets up a single (non-local) parameter
	 * 
	 * @param parameter
	 */
	private void setupSingleParameter(Parameter parameter) {
		
		String parameterID = parameter.getId();
		
		variableToValueMap.put(parameterID, parameter.getValue());
		variableToIsConstantMap.put(parameterID, parameter.getConstant());
		
		if (numRules > 0)
			variableToIsInAssignmentRuleMap.put(parameterID, false);
		
		if (numConstraints > 0)
			variableToIsInConstraintMap.put(parameterID, false);
	}

	/**
	 * puts parameter-related information into data structures
	 */
	protected void setupParameters() {
		
		//add local parameters
		for (Reaction reaction : model.getListOfReactions()) {
			
			KineticLaw kineticLaw = reaction.getKineticLaw();			
			setupLocalParameters(kineticLaw, reaction.getId());
		}
		
		//add values to hashmap for easy access to global parameter values
		//NOTE: the IDs for the parameters and species must be unique, so putting them in the
		//same hashmap is okay
		for (Parameter parameter : model.getListOfParameters()) {
			
			setupSingleParameter(parameter);
		}
		
		//add compartment sizes in
		for (Compartment compartment : model.getListOfCompartments()) {
			
			String compartmentID = compartment.getId();
			
			variableToValueMap.put(compartmentID, compartment.getSize());
			
			if (numRules > 0)
				variableToIsInAssignmentRuleMap.put(compartmentID, false);
			
			if (numConstraints > 0)
				variableToIsInConstraintMap.put(compartmentID, false);
		}
	}
	
	/**
	 * puts rule-related information into data structures
	 */
	protected void setupRules() {
		
		numAssignmentRules = 0;
		
		//loop through all assignment rules
		//store which variables (RHS) affect the rule variable (LHS)
		//so when those RHS variables change, we know to re-evaluate the rule
		//and change the value of the LHS variable
		for (Rule rule : model.getListOfRules()) {
			
			if (rule.isAssignment()) {
				
				//Rules don't have a getVariable method, so this needs to be cast to an ExplicitRule
				AssignmentRule assignmentRule = (AssignmentRule) rule;
				
				for (ASTNode ruleNode : assignmentRule.getMath().getListOfNodes()) {
					
					if (ruleNode.isName()) {
						
						String nodeName = ruleNode.getName();
						
						variableToAffectedAssignmentRuleSetMap.put(nodeName, new HashSet<AssignmentRule>());
						variableToAffectedAssignmentRuleSetMap.get(nodeName).add(assignmentRule);
						variableToIsInAssignmentRuleMap.put(nodeName, true);
					}
				}
				
				++numAssignmentRules;				
			}
		}
	}

	/**
	 * puts constraint-related information into data structures
	 */
	protected void setupConstraints() {
		
		//loop through all constraints to find out which variables affect which constraints
		//this is stored in a hashmap, as is whether the variable is in a constraint
		for (Constraint constraint : model.getListOfConstraints()) {
			
			for (ASTNode constraintNode : constraint.getMath().getListOfNodes()) {
				
				if (constraintNode.isName()) {
					
					String nodeName = constraintNode.getName();
					
					variableToAffectedConstraintSetMap.put(nodeName, new HashSet<ASTNode>());
					variableToAffectedConstraintSetMap.get(nodeName).add(constraint.getMath());
					variableToIsInConstraintMap.put(nodeName, true);
				}
			}
		}
	}

	/**
	 * sets up a single event
	 * 
	 * @param event
	 */
	protected void setupSingleEvent(Event event) {
		
		String eventID = event.getId();
		
		//these events are what determines if a model is dynamic or not
		if (eventID.contains("Division__") || eventID.contains("Death__"))
			dynamicBoolean = true;
		
		if (event.isSetPriority())
			eventToPriorityMap.put(eventID, evaluateExpressionRecursive(event.getPriority().getMath()));
		
		if (event.isSetDelay()) {
			
			eventToDelayMap.put(eventID, event.getDelay().getMath());
			eventToHasDelayMap.put(eventID, true);
		}
		else
			eventToHasDelayMap.put(eventID, false);
		
		eventToTriggerMap.put(eventID, event.getTrigger().getMath());
		eventToTriggerInitiallyTrueMap.put(eventID, event.getTrigger().isInitialValue());
		eventToPreviousTriggerValueMap.put(eventID, event.getTrigger().isInitialValue());
		eventToTriggerPersistenceMap.put(eventID, event.getTrigger().getPersistent());
		eventToUseValuesFromTriggerTimeMap.put(eventID, event.isUseValuesFromTriggerTime());
		eventToAssignmentSetMap.put(eventID, new HashSet<Object>());
		eventToAffectedReactionSetMap.put(eventID, new HashSet<String>());
		
		untriggeredEventSet.add(eventID);
		
		for (EventAssignment assignment : event.getListOfEventAssignments()) {
			
			String variableID = assignment.getVariable();
			
			eventToAssignmentSetMap.get(eventID).add(assignment);
			
			if (variableToEventSetMap.containsKey(variableID) == false)
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
	
	/**
	 * puts event-related information into data structures
	 */
	protected void setupEvents() {
		
		//add event information to hashmaps for easy/fast access
		//this needs to happen after calculating initial propensities
		//so that the speciesToAffectedReactionSetMap is populated
		for (Event event : model.getListOfEvents()) {
			
			setupSingleEvent(event);
		}
	}
	
	/**
	 * does a minimized initialization process to prepare for a new run
	 */
	protected abstract void setupForNewRun(int newRun);
	
	/**
	 * opens output file and seeds rng for new run
	 * 
	 * @param randomSeed
	 * @param currentRun
	 * @throws IOException
	 */
	protected void setupForOutput(long randomSeed, int currentRun) {
		
		this.currentRun = currentRun;
		
		randomNumberGenerator = new XORShiftRandom(randomSeed);
		
		try {
			
			String extension = ".tsd";
			
			if (dynamicBoolean == true)
				extension = ".dtsd";
			
			TSDWriter = new FileWriter(outputDirectory + "run-" + currentRun + extension);
			bufferedTSDWriter = new BufferedWriter(TSDWriter);
			bufferedTSDWriter.write('(');
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * abstract simulate method
	 * each simulator needs a simulate method
	 */
	protected abstract void simulate();
	
	/**
	 * this evaluates a set of constraints that have been affected by an event or reaction firing
	 * and returns the OR'd boolean result
	 * 
	 * @param affectedConstraintSet the set of constraints affected
	 * @return the boolean result of the constraints' evaluation
	 */
	protected boolean testConstraints(HashSet<ASTNode> affectedConstraintSet) {
		
		//check all of the affected constraints
		//if one evaluates to true, then the simulation halts
		for (ASTNode constraint : affectedConstraintSet) {
			
			if (getBooleanFromDouble(evaluateExpressionRecursive(constraint)) == true)
				return true;
		}
		
		return false;
	}
	
	/**
	 * is called after duplicateComponent or deleteComponent is called
	 */
	protected abstract void updateAfterDynamicChanges();
	
	/**
	 * updates the propensities of the reaction set passed in
	 * 
	 * @param reactionSet
	 */
	private void updatePropensities(HashSet<String> reactionSet) {
		
		for (String reactionID : reactionSet) {
			
			boolean notEnoughMoleculesFlag = false;
			
			HashSet<StringDoublePair> reactantStoichiometrySet = 
				reactionToReactantStoichiometrySetMap.get(reactionID);
			
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
			
			if (notEnoughMoleculesFlag == false)
				newPropensity = evaluateExpressionRecursive(reactionToFormulaMap.get(reactionID));
			
			if (newPropensity > 0.0 && newPropensity < minPropensity)
				minPropensity = newPropensity;
			
			if (newPropensity > maxPropensity)
				maxPropensity = newPropensity;
			
			double oldPropensity = reactionToPropensityMap.get(reactionID);
			
			//add the difference of new v. old propensity to the total propensity
			totalPropensity += newPropensity - oldPropensity;
			
			reactionToPropensityMap.put(reactionID, newPropensity);	
		}
		
		
	}
	
	//EVENT TO FIRE INNER CLASS
	/**
	 * easy way to store multiple data points for events that are firing
	 */
	protected class EventToFire {
		
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
	 * compares two events to see which one should be before the other in the priority queue
	 */
	protected class EventComparator implements Comparator<EventToFire> {

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
	protected class StringDoublePair {
		
		public String string;
		public double doub;
		
		StringDoublePair(String s, double d) {
			
			string = s;
			doub = d;
		}
	}
}
