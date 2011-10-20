package dynamicsim;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import main.Gui;

import odk.lang.FastMath;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

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
	
	//a linked (ordered) set of all species IDs, to allow easy access to their values via the variableToValue map
	protected LinkedHashSet<String> speciesIDSet = null;
	
	//allows for access to species and parameter values from a variable ID
	protected TObjectDoubleHashMap<String> variableToValueMap = null;
	
	//stores events in order of fire time and priority
	protected PriorityQueue<EventToFire> triggeredEventQueue = null;
	protected HashSet<String> untriggeredEventSet = null;
	
	//hashmaps that allow for access to event information from the event's id
	private TObjectDoubleHashMap<String> eventToPriorityMap = null;
	protected HashMap<String, ASTNode> eventToDelayMap = null;
	protected HashMap<String, Boolean> eventToHasDelayMap = null;
	protected HashMap<String, Boolean> eventToTriggerPersistenceMap = null;
	protected HashMap<String, Boolean> eventToUseValuesFromTriggerTimeMap = null;
	protected HashMap<String, ASTNode> eventToTriggerMap = null;
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
	
	protected String SBMLFileName;
	protected double timeLimit;
	protected double maxTimeStep;
	protected JProgressBar progress;
	protected double printInterval;
	
	protected long numSpecies;
	protected long numParameters;
	protected long numReactions;
	protected long numEvents;
	protected long numRules;
	protected int numAssignmentRules;
	protected long numConstraints;
	protected int numInitialAssignments;
	
	
	
	
	public Simulator(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval) 
	throws IOException, XMLStreamException {
		
		this.SBMLFileName = SBMLFileName;
		this.timeLimit = timeLimit;
		this.maxTimeStep = maxTimeStep;
		this.progress = progress;
		this.printInterval = printInterval;
		
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
			eventToTriggerPersistenceMap = new HashMap<String, Boolean>((int) numEvents);
			eventToUseValuesFromTriggerTimeMap = new HashMap<String, Boolean>((int) numEvents);
			eventToAssignmentSetMap = new HashMap<String, HashSet<Object> >((int) numEvents);
			eventToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numEvents);		
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
	protected void calculateInitialPropensities(long numReactions) {
		
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
	protected HashSet<String> handleEvents(double currentTime, 
			final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {
		
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
			
			//update the species count
			variableToValueMap.adjustValue(assignmentRule.getVariable(), 
					evaluateExpressionRecursive(assignmentRule.getMath()));
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
			
			//update the species count
			//note that the stoichiometries are earlier modified with the correct +/- sign
			variableToValueMap.adjustValue(speciesID, stoichiometry);
			
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
	protected void printToTSD() throws IOException {
		
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
	 * puts species-related information into data structures
	 * 
	 * @throws IOException
	 */
	protected void setupSpecies() throws IOException {
		
		bufferedTSDWriter.write('(');
		
		String commaSpace = "";
		
		//add values to hashmap for easy access to species amounts
		for (Species species : model.getListOfSpecies()) {
			
			String speciesID = species.getId();
			
			variableToValueMap.put(speciesID, species.getInitialAmount());
			
			if (numRules > 0)
				variableToIsInAssignmentRuleMap.put(speciesID, false);
			
			if (numConstraints > 0)
				variableToIsInConstraintMap.put(speciesID, false);
			
			speciesToAffectedReactionSetMap.put(speciesID, new HashSet<String>(20));
			speciesIDSet.add(speciesID);
			
			bufferedTSDWriter.write(commaSpace + "\"" + speciesID + "\"");
			commaSpace = ", ";
		}
		
		bufferedTSDWriter.write("),\n");
	}

	/**
	 * puts initial assignment-related informatino into data structures
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
	}

	/**
	 * puts parameter-related informatino into data structures
	 */
	protected void setupParameters() {
		
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
	}
	
	/**
	 * puts rule-related informatino into data structures
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
	 * puts constraint-related informatino into data structures
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
	
	
	

}
