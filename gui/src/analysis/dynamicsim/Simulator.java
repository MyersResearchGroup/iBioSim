package analysis.dynamicsim;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import flanagan.math.Fmath;
import flanagan.math.PsRandom;

import main.Gui;
import main.util.dataparser.DataParser;
import main.util.dataparser.TSDParser;

import odk.lang.FastMath;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.CallableSBase;
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
	
	//allows for access to a SBML Reaction object from a reaction ID
	protected HashMap<String, Reaction> reactionToSBMLReactionMap = null;
	
	//allows for access to a propensity from a reaction ID
	protected TObjectDoubleHashMap<String> reactionToPropensityMap = null;
	
	//allows for access to reactant/product speciesID and stoichiometry from a reaction ID
	//note that species and stoichiometries need to be thought of as unique for each reaction
	protected HashMap<String, HashSet<StringDoublePair> > reactionToSpeciesAndStoichiometrySetMap = null;
	
	//allows for access to reactant/modifier speciesID and stoichiometry from a reaction ID
	protected HashMap<String, HashSet<StringDoublePair> > reactionToReactantStoichiometrySetMap = null;
	
	//allows for access to a kinetic formula tree from a reaction
	protected HashMap<String, ASTNode> reactionToFormulaMap = null;
	
	//allows for access to a kinetic formula tree from a reaction
	//this is used for iterative evaluation
	protected HashMap<String, ArrayDeque<ASTNode> > reactionToFormulaMap2 = null;
	
	//allows for access to a set of reactions that a species is in (as a reactant or modifier) from a species ID
	protected HashMap<String, HashSet<String> > speciesToAffectedReactionSetMap = null;
	
	//allows for access to species booleans from a species ID
	protected HashMap<String, Boolean> speciesToIsBoundaryConditionMap = null;
	protected HashMap<String, Boolean> speciesToIsConstantMap = null;
	protected HashMap<String, Boolean> speciesToHasOnlySubstanceUnitsMap = null;
	protected HashMap<String, Boolean> speciesToIsArrayedMap = null;
	protected TObjectDoubleHashMap<String> speciesToCompartmentSizeMap = null;
	
	//a linked (ordered) set of all species IDs, to allow easy access to their values via the variableToValue map
	protected LinkedHashSet<String> speciesIDSet = null;
	
	//allows access to upper/lower row/col bounds for a species array
	protected HashMap<String, SpeciesDimensions> arrayedSpeciesToDimensionsMap = null;
	
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
	
	//compares two events based on fire time and priority
	protected EventComparator eventComparator = new EventComparator();
	
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
		
		setupNewRun(randomSeed, 1);
		
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
		arrayedSpeciesToDimensionsMap = new HashMap<String, SpeciesDimensions>((int) numSpecies);
		speciesToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numSpecies);
		speciesToIsArrayedMap = new HashMap<String, Boolean >((int) numSpecies);
		speciesToIsBoundaryConditionMap = new HashMap<String, Boolean>((int) numSpecies);
		speciesToIsConstantMap = new HashMap<String, Boolean>((int) numSpecies);
		speciesToHasOnlySubstanceUnitsMap = new HashMap<String, Boolean>((int) numSpecies);
		speciesToCompartmentSizeMap = new TObjectDoubleHashMap<String>((int) numSpecies);
		speciesIDSet = new LinkedHashSet<String>((int) numSpecies);
		variableToValueMap = new TObjectDoubleHashMap<String>((int) numSpecies + (int) numParameters);
		
		reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (numReactions * 1.5));		
		reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));	
		reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));
		reactionToFormulaMap = new HashMap<String, ASTNode>((int) (numReactions * 1.5));
		//reactionToFormulaMap2 = new HashMap<String, ArrayDeque<ASTNode> >((int) (numReactions * 1.5));
		reactionToSBMLReactionMap = new HashMap<String, Reaction>((int) numReactions);
		
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
	 * cancels the simulation on the next iteration
	 * called from outside the class when the user closes the progress bar dialog
	 */
	public void cancel() {
		
		cancelFlag = true;
	}
	
	/**
	 * calculates the initial propensities for each reaction in the model
	 * 
	 * @param numReactions the number of reactions in the model
	 */
	protected void calculateInitialPropensities() {
		
		//loop through all reactions and calculate their propensities
		for (Reaction reaction : model.getListOfReactions()) {
			
			String reactionID = reaction.getId();
			KineticLaw reactionKineticLaw = reaction.getKineticLaw();
			ASTNode reactionFormula = reactionKineticLaw.getMath();
			boolean notEnoughMoleculesFlagFd = false;
			boolean notEnoughMoleculesFlagRv = false;
			boolean notEnoughMoleculesFlag = false;
			
			reactionToSBMLReactionMap.put(reactionID, reaction);
						
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
	 * clears data structures for new run
	 */
	protected abstract void clear();
	
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
				
				PsRandom prng = new PsRandom();
				
				return prng.nextDouble(lowerBound, upperBound);
			}
			else if (nodeName.equals("exponential")) {
				
				PsRandom prng = new PsRandom();
				
				return prng.nextExponential(evaluateExpressionRecursive(node.getLeftChild()), 1);
			}
			else if (nodeName.equals("gamma")) {
				
				PsRandom prng = new PsRandom();
				
				return prng.nextGamma(1, evaluateExpressionRecursive(node.getLeftChild()), 
						evaluateExpressionRecursive(node.getRightChild()));
			}
			else if (nodeName.equals("chisq")) {
				
				PsRandom prng = new PsRandom();
				
				return prng.nextChiSquare((int) evaluateExpressionRecursive(node.getLeftChild()));
			}
			else if (nodeName.equals("lognormal")) {
				
				PsRandom prng = new PsRandom();
				
				return prng.nextLogNormal(evaluateExpressionRecursive(node.getLeftChild()), 
						evaluateExpressionRecursive(node.getRightChild()));
			}
			else if (nodeName.equals("laplace")) {
				
				//function doesn't exist in current libraries
			}
			else if (nodeName.equals("cauchy")) {
				
				PsRandom prng = new PsRandom();
				
				return prng.nextLorentzian(0, evaluateExpressionRecursive(node.getLeftChild()));
			}
			else if (nodeName.equals("poisson")) {
				
				PsRandom prng = new PsRandom();
				
				return prng.nextPoissonian(evaluateExpressionRecursive(node.getLeftChild()));
			}
			else if (nodeName.equals("binomial")) {
				
				PsRandom prng = new PsRandom();
				
				return prng.nextBinomial(evaluateExpressionRecursive(node.getLeftChild()),
						(int) evaluateExpressionRecursive(node.getRightChild()));
			}
			else if (nodeName.equals("bernoulli")) {
				
				PsRandom prng = new PsRandom();
				
				return prng.nextBinomial(evaluateExpressionRecursive(node.getLeftChild()), 1);
			}
			else if (nodeName.equals("normal")) {
				
				PsRandom prng = new PsRandom();
				
				return prng.nextGaussian(evaluateExpressionRecursive(node.getLeftChild()),
						evaluateExpressionRecursive(node.getRightChild()));	
			}
			else if (nodeName.equals("get2DArrayElement")) {
				
				int leftIndex = node.getChild(1).getInteger();
				int rightIndex = node.getChild(2).getInteger();
				String speciesName = "ROW" + leftIndex + "_COL" + rightIndex + "__" + node.getChild(0).getName();
				
				//check bounds
				//if species exists, return its value/amount
				if (variableToValueMap.containsKey(speciesName))
					return variableToValueMap.get(speciesName);
			}
			else if (node.getType().equals(org.sbml.jsbml.ASTNode.Type.NAME_TIME)) {
				
				return currentTime;
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
				if (speciesToIsConstantMap.get(variable) == false) {
					
					if (speciesToHasOnlySubstanceUnitsMap.get(variable) == false)
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
			if (speciesToIsConstantMap.get(variable) == false) {
				
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
					speciesToIsConstantMap.get(speciesID) == false) {
				
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
		
		bufferedTSDWriter.write('(');
		
		String commaSpace = "";
		
		//print the current time
		bufferedTSDWriter.write(printTime + ", ");
		
		//loop through the speciesIDs and print their current value to the file
		for (String speciesID : speciesIDSet) {
			
			bufferedTSDWriter.write(commaSpace + (int) variableToValueMap.get(speciesID));
			commaSpace = ", ";
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
	 * adds species and reactions to the model that are implicit in arrays
	 */
	protected void setupArrays() {
				
		//ARRAYED SPECIES BUSINESS
		//create all new species that are implicit in the arrays and put them into the model		
		
		ArrayList<Species> speciesToAdd = new ArrayList<Species>();
		ArrayList<String> speciesToRemove = new ArrayList<String>();
		
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
				
				arrayedSpeciesToDimensionsMap.put(speciesID, 
						new SpeciesDimensions(numRowsLower, numRowsUpper, numColsLower, numColsUpper));
				
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
					newReaction.setId("ROW" + row + "_COL" + col  + "__" + reactionID);
					
					//alter the kinetic law to so that it has the correct indexes as children for the
					//get2DArrayElement function
					//get the nodes to alter (that are arguments for the get2DArrayElement method)
					ArrayList<ASTNode> get2DArrayElementNodes = new ArrayList<ASTNode>();
					
					getSatisfyingNodes(newReaction.getKineticLaw().getMath(), 
							"get2DArrayElement", get2DArrayElementNodes);
					
					//replace the get2darrayelement stuff with the proper explicit species/parameters
					for (ASTNode node : get2DArrayElementNodes) {
												
						if (node.getLeftChild().getName().contains("kmdiff")) {
							
							String parameterName = node.getLeftChild().getName();
							node.setVariable(model.getParameter(compartmentID + "__" + parameterName));							
							node.removeChild(0);
						}
						//this means it's a species, which we need to prepend with the row/col prefix
						else {
							
							//product
							if (node.getLeftChild().getName().contains("_p")) {
								
								node.setVariable(model.getSpecies(
										"ROW" + row + "_COL" + col + "__" + node.getLeftChild().getName().replace("_p","")));
							}
							//reactant
							else {
								
								node.setVariable(model.getSpecies(
										compartmentID + "__" + node.getLeftChild().getName().replace("_r","")));
							}								
								
							node.removeChild(0);
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
					
					reactionsToAdd.add(newReaction);
					++membraneDiffusionIndex;
				}
			}
			
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
					newReaction.setId("ROW" + row + "_COL" + col + "__" + reactionID);
					
					//get the nodes to alter
					ArrayList<ASTNode> get2DArrayElementNodes = new ArrayList<ASTNode>();
					
					getSatisfyingNodes(newReaction.getKineticLaw().getMath(), 
							"get2DArrayElement", get2DArrayElementNodes);
					
					//loop through all reactants
					for (SpeciesReference reactant : reaction.getListOfReactants()) {
						
						//find offsets
						int rowOffset = 0;
						int colOffset = 0;
						
						String[] annotationString = reactant.getAnnotationString().split("=");
												
						rowOffset = Integer.valueOf(((String[])(annotationString[1].split(" ")))[0].replace("\"",""));
						colOffset = Integer.valueOf(((String[])(annotationString[2].split(" ")))[0].replace("\"",""));
						
						//alter the kinetic law to so that it has the correct indexes as children for the
						//get2DArrayElement function
						ASTNode arrayChild1 = new ASTNode();
						arrayChild1.setValue(row + rowOffset);
						
						ASTNode arrayChild2 = new ASTNode();
						arrayChild2.setValue(col + colOffset);
						
						//adjust the node so that the get2DArrayElement function knows where to find the
						//relevant species amount
						for (ASTNode node : get2DArrayElementNodes) {
							
							//make sure that the node is a reactant and not a modifier
							//if it is a reactant, add the appropriate indexes as children								
							if (node.getLeftChild().getName().contains("_r")) {
								
								node.getLeftChild().setName(node.getLeftChild().getName().replace("_r",""));
								node.addChild(arrayChild1);
								node.addChild(arrayChild2);
							}
						}
						
						//create a new reactant and add it to the new reaction
						SpeciesReference newReactant = new SpeciesReference();
						newReactant = reactant.clone();
						newReactant.setSpecies("ROW" + row + "_COL" + col + "__" + newReactant.getSpecies());
						newReactant.setAnnotation(null);
						newReaction.addReactant(newReactant);
					}// end looping through reactants
					
					//loop through all modifiers
					for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
						
						
					}
					
					
					//loop through all products
					for (SpeciesReference product : reaction.getListOfProducts()) {
						
						//find offsets
						int rowOffset = 0;
						int colOffset = 0;
						
						String[] annotationString = product.getAnnotationString().split("=");
						rowOffset = Integer.valueOf(((String[])(annotationString[1].split(" ")))[0].replace("\"",""));
						colOffset = Integer.valueOf(((String[])(annotationString[2].split(" ")))[0].replace("\"",""));
						
						//don't create reactions with products that don't exist 
						if (row + rowOffset < reactantDimensions.numRowsLower || 
								col + colOffset < reactantDimensions.numColsLower ||
								row + rowOffset > reactantDimensions.numRowsUpper ||
								col + colOffset > reactantDimensions.numColsUpper) {
							
							abort = true;
							break;
						}
						
						//alter the kinetic law to so that it has the correct indexes as children for the
						//get2DArrayElement function
						ASTNode arrayChild1 = new ASTNode();
						arrayChild1.setValue(row + rowOffset);
						
						ASTNode arrayChild2 = new ASTNode();
						arrayChild2.setValue(col + colOffset);
						
						//adjust the node so that the get2DArrayElement function knows where to find the
						//relevant species amount
						for (ASTNode node : get2DArrayElementNodes) {
							
							//make sure that the node is a product and not a modifier
							//if it is a product, add the appropriate indexes as children
							if (node.getLeftChild().getName().contains("_p")) {
								
								node.getLeftChild().setName(node.getLeftChild().getName().replace("_p",""));
								node.addChild(arrayChild1);
								node.addChild(arrayChild2);
							}
						}
						
						//create a new product and add it to the new reaction
						SpeciesReference newProduct = new SpeciesReference();
						newProduct = product.clone();
						newProduct.setSpecies("ROW" + (row + rowOffset) + "_COL" + (col + colOffset) + "__" + newProduct.getSpecies());
						newProduct.setAnnotation(null);
						newReaction.addProduct(newProduct);
					} //end looping through products
					
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
			p = new PrintStream(new FileOutputStream("/home/beauregard/Desktop/output.xml"), true, "UTF-8");
			p.print(writer.writeSBMLToString(model.getSBMLDocument()));
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * recursively puts the nodes that have the same name as the quarry string passed in into the arraylist passed in
	 * so, the entire tree is searched through, which i don't think is possible with the jsbml methods
	 * 
	 * @param node node to search through
	 * @param quarry string to search for
	 * @param satisfyingNodes list of nodes that satisfy the condition
	 */
	protected void getSatisfyingNodes(ASTNode node, String quarry, ArrayList<ASTNode> satisfyingNodes) {
		
		if (node.isName() && node.getName().equals(quarry))
			satisfyingNodes.add(node);
		else {
			for (ASTNode childNode : node.getChildren())
				getSatisfyingNodes(childNode, quarry, satisfyingNodes);
		}		
	}
	
	/**
	 * puts species-related information into data structures
	 * 
	 * @throws IOException
	 */
	protected void setupSpecies() throws IOException {
		
		bufferedTSDWriter.write("(" + "\"" + "time" + "\"" + ", ");
		
		String commaSpace = "";
		
		//add values to hashmap for easy access to species amounts
		for (Species species : model.getListOfSpecies()) {
			
			String speciesID = species.getId();

			variableToValueMap.put(speciesID, species.getInitialAmount());
			
			if (numRules > 0)
				variableToIsInAssignmentRuleMap.put(speciesID, false);
			
			if (numConstraints > 0)
				variableToIsInConstraintMap.put(speciesID, false);
			
			if (species.hasOnlySubstanceUnits() == false)
				speciesToCompartmentSizeMap.put(speciesID, species.getCompartmentInstance().getSize());
			
			speciesToAffectedReactionSetMap.put(speciesID, new HashSet<String>(20));
			speciesToIsBoundaryConditionMap.put(speciesID, species.isBoundaryCondition());
			speciesToIsConstantMap.put(speciesID, species.isConstant());
			speciesToHasOnlySubstanceUnitsMap.put(speciesID, species.hasOnlySubstanceUnits());
			speciesIDSet.add(speciesID);
			
			bufferedTSDWriter.write(commaSpace + "\"" + speciesID + "\"");
			commaSpace = ", ";
		}
		
		bufferedTSDWriter.write("),\n");
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
	 * puts parameter-related information into data structures
	 */
	protected void setupParameters() {
		
		//add local parameters
		for (Reaction reaction : model.getListOfReactions()) {
			
			KineticLaw kineticLaw = reaction.getKineticLaw();
			
			for (LocalParameter localParameter : kineticLaw.getListOfLocalParameters()) {
				
				String parameterID = "";
				
				//the parameters don't get reset after each run, so don't re-do this prepending
				if (localParameter.getId().contains(reaction.getId() + "_") == false)					
					parameterID = reaction.getId() + "_" + localParameter.getId();
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
		
		//add values to hashmap for easy access to global parameter values
		//NOTE: the IDs for the parameters and species must be unique, so putting them in the
		//same hashmap is okay
		for (Parameter parameter : model.getListOfParameters()) {
			
			String parameterID = parameter.getId();
			
			variableToValueMap.put(parameterID, parameter.getValue());
			
			if (numRules > 0)
				variableToIsInAssignmentRuleMap.put(parameterID, false);
			
			if (numConstraints > 0)
				variableToIsInConstraintMap.put(parameterID, false);
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
	 * puts event-related informatino into data structures
	 */
	protected void setupEvents() {
		
		//add event information to hashmaps for easy/fast access
		//this needs to happen after calculating initial propensities
		//so that the speciesToAffectedReactionSetMap is populated
		for (Event event : model.getListOfEvents()) {
			
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
	protected void setupNewRun(long randomSeed, int currentRun) {
		
		this.currentRun = currentRun;
		
		randomNumberGenerator = new XORShiftRandom(randomSeed);
		
		try {
			TSDWriter = new FileWriter(outputDirectory + "run-" + currentRun + ".tsd");
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
	
	
	//SPECIES DIMENSIONS INNER CLASS
	protected class SpeciesDimensions {
		
		public int numRowsUpper;
		public int numColsUpper;
		public int numRowsLower;
		public int numColsLower;
		
		public SpeciesDimensions(int rLower, int rUpper, int cLower, int cUpper) {
			
			numRowsUpper = rUpper;
			numRowsLower = rLower;
			numColsUpper = cUpper;
			numColsLower = cLower;
		}
	}

}
