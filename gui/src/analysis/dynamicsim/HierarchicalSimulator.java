package analysis.dynamicsim;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import flanagan.math.Fmath;
import flanagan.math.PsRandom;


import org.sbml.jsbml.SBMLException;
import org.sbml.libsbml.CompModelPlugin;
import org.sbml.libsbml.CompSBMLDocumentPlugin;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.LocalParameter;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.RateRule;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.ListOfSpeciesReferences;
import org.sbml.libsbml.SimpleSpeciesReference;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.AssignmentRule;
import org.sbml.libsbml.Submodel;
import org.sbml.libsbml.XMLNode;
import org.sbml.libsbml.libsbml;
import org.sbml.libsbml.libsbmlConstants;
import org.sbml.libsbml.ListOfCompartments;
import org.sbml.libsbml.ListOfEvents;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLErrorLog;
import org.sbml.libsbml.SBMLReader;

import main.Gui;
import main.util.dataparser.DTSDParser;
import main.util.dataparser.DataParser;
import main.util.dataparser.TSDParser;

import odk.lang.FastMath;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import biomodel.parser.BioModel;

import analysis.dynamicsim.Simulator.EventToFire;
import analysis.dynamicsim.Simulator.StringDoublePair;
import analysis.dynamicsim.Simulator.StringStringPair;

public abstract class HierarchicalSimulator {
	
	//SBML Top model
	protected Model model = null;
	protected Model [] submodels;
	
	//generates random numbers based on the xorshift method
	protected XORShiftRandom randomNumberGenerator = null;
	
	//allows for access to a propensity from a reaction ID
	protected TObjectDoubleHashMap<String> reactionToPropensityMap = null;
	
	//allows for access to reactant/product speciesID and stoichiometry from a reaction ID
	//note that species and stoichiometries need to be thought of as unique for each reaction
	protected HashMap<String, HashSet<StringDoublePair> > reactionToSpeciesAndStoichiometrySetMap = null;
	
	protected HashMap<String, HashSet<StringStringPair> > reactionToNonconstantStoichiometriesSetMap = null;
	
	//allows for access to reactant/modifier speciesID and stoichiometry from a reaction ID
	protected HashMap<String, HashSet<StringDoublePair> > reactionToReactantStoichiometrySetMap = null;
	
	//allows for access to a kinetic formula tree from a reaction
	protected HashMap<String, ASTNode> reactionToFormulaMap = null;
	
	//allows for access to a set of reactions that a species is in (as a reactant or modifier) from a species ID
	protected HashMap<String, HashSet<String> > speciesToAffectedReactionSetMap = null;
	
	//allows for access to species booleans from a species ID
	protected HashMap<String, Boolean> speciesToIsBoundaryConditionMap = null;
	protected HashMap<String, Boolean> speciesToHasOnlySubstanceUnitsMap = null;
	protected HashMap<String, String> speciesToSubstanceUnitsMap = null;
	protected HashMap<String, String> speciesToConversionFactorMap = null;
	protected HashMap<String, String> speciesToCompartmentNameMap = null;
	protected TObjectDoubleHashMap<String> speciesToCompartmentSizeMap = null;
	
	//a linked (ordered) set of all species IDs, to allow easy access to their values via the variableToValue map
	protected LinkedHashSet<String> speciesIDSet = null;
	
	//allows for access to species and parameter values from a variable ID
	protected TObjectDoubleHashMap<String> variableToValueMap = null;
	
	//allows for access to the set of events that a variable is in
	protected HashMap<String, HashSet<String> > variableToEventSetMap = null;
	
	//allows for access to the set of assignment rules that a variable (rhs) in an assignment rule affects
	protected HashMap<String, HashSet<AssignmentRule> > variableToAffectedAssignmentRuleSetMap = null;
	
	//allows to access to whether or not a variable is in an assignment or rate rule rule (RHS)
	protected HashMap<String, Boolean> variableToIsInAssignmentRuleMap = null;
	protected HashMap<String, Boolean> variableToIsInRateRuleMap = null;
	
	//allows for access to the set of constraints that a variable affects
	protected HashMap<String, HashSet<ASTNode> > variableToAffectedConstraintSetMap = null;
	
	protected HashMap<String, Boolean> variableToIsInConstraintMap = null;
	protected HashMap<String, Boolean> variableToIsConstantMap = null;
	
	//allows access to a component's location on the grid from its ID
	protected LinkedHashMap<String, Point> componentToLocationMap = null;
	
	//allows access to a component's set of reactions from its ID
	protected HashMap<String, HashSet<String> > componentToReactionSetMap = null;
	
	//allows access to a component's set of variables (species and parameters) from its ID
	protected HashMap<String, HashSet<String> > componentToVariableSetMap = null;
	
	protected HashMap<String, HashSet<String> > componentToEventSetMap = null;
	
	protected HashSet<String> componentIDSet = new HashSet<String>();
	
	protected LinkedHashSet<String> compartmentIDSet = new LinkedHashSet<String>();
	protected LinkedHashSet<String> nonconstantParameterIDSet = new LinkedHashSet<String>();
	
	//initial model -- used for resetting when doing multiple dynamic runs
	protected ListOfSpecies initialSpecies = new ListOfSpecies(3,1);
	protected ListOfReactions initialReactions = new ListOfReactions(3,1);
	protected ListOfEvents initialEvents = new ListOfEvents(3,1);
	protected ListOfParameters initialParameters = new ListOfParameters(3,1);
	protected ListOfCompartments initialCompartments = new ListOfCompartments(3,1);
	
	//locations parameter annotations for placing compartments
	protected HashMap<String, String> submodelIDToLocationsMap = new HashMap<String, String>();
	
	protected ArrayList<String> interestingSpecies = new ArrayList<String>();
	
	protected HashSet<String> ibiosimFunctionDefinitions = new HashSet<String>();
	
	//propensity variables
	protected double totalPropensity = 0.0;
	protected double minPropensity = Double.MAX_VALUE / 10.0;
	protected double maxPropensity = Double.MIN_VALUE / 10.0;
	
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
	protected double minTimeStep;
	protected JProgressBar progress;
	protected double printInterval;
	protected int currentRun;
	protected String outputDirectory;
	protected String separator;
	
	protected long numSpecies;
	protected long numParameters;
	protected long numReactions;
	protected long numEvents;
	protected long numRules;
	protected int numAssignmentRules;
	protected long numConstraints;
	protected int numInitialAssignments;
	protected int numRateRules;
	
	protected int minRow = Integer.MAX_VALUE;
	protected int minCol = Integer.MAX_VALUE;
	protected int maxRow = Integer.MIN_VALUE;
	protected int maxCol = Integer.MIN_VALUE;
	
	protected boolean stoichAmpBoolean = false;
	protected double stoichAmpGridValue = 1.0;
	
	protected boolean printConcentrations = false;
	
	protected int diffCount = 0;
	protected int totalCount = 0;
	protected int memCount = 0;
	
	protected JFrame running = new JFrame();
	
	final protected int SBML_LEVEL = 3;
	final protected int SBML_VERSION = 1;
	
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
	public HierarchicalSimulator(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			Long initializationTime, double stoichAmpValue, JFrame running, String[] interestingSpecies, 
			String quantityType) 
	throws IOException, XMLStreamException {
		
		long initTime1 = System.nanoTime();
		
		this.SBMLFileName = SBMLFileName;
		this.timeLimit = timeLimit;
		this.maxTimeStep = maxTimeStep;
		this.minTimeStep = minTimeStep;
		this.progress = progress;
		this.printInterval = printInterval;
		this.outputDirectory = outputDirectory;
		this.running = running;
		this.interestingSpecies.clear();

		for (int i = 0; i < interestingSpecies.length; ++i)
			this.interestingSpecies.add(interestingSpecies[i]);
		
		if (quantityType != null && quantityType.equals("concentration"))
			this.printConcentrations = true;
		
		if (stoichAmpValue <= 1.0)
			stoichAmpBoolean = false;
		else {
			stoichAmpBoolean = true;
			stoichAmpGridValue = stoichAmpValue;
		}
		
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = reader.readSBML(SBMLFileName);
		
		SBMLErrorLog errors = document.getErrorLog();
		
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
		numRules = model.getNumRules();
		numInitialAssignments = (int)model.getNumInitialAssignments() ;
		
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		
		CompModelPlugin sbmlCompModel = (CompModelPlugin)document.getModel().getPlugin("comp");
		CompSBMLDocumentPlugin sbmlComp = (CompSBMLDocumentPlugin)document.getPlugin("comp");
		for (long i = 0; i < sbmlCompModel.getNumSubmodels(); i++) {
			Submodel submodel = sbmlCompModel.getSubmodel(i);
			BioModel subBioModel = new BioModel(outputDirectory);		
			String extModelFile = sbmlComp.getExternalModelDefinition(submodel.getModelRef())
					.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			subBioModel.load(outputDirectory + "/" + extModelFile);
		}
		
		//set initial capacities for collections (1.5 is used to multiply numReactions due to reversible reactions)
		speciesToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numSpecies);
		speciesToIsBoundaryConditionMap = new HashMap<String, Boolean>((int) numSpecies);
		variableToIsConstantMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
		speciesToHasOnlySubstanceUnitsMap = new HashMap<String, Boolean>((int) numSpecies);
		speciesToSubstanceUnitsMap = new HashMap<String, String>((int) numSpecies);
		speciesToConversionFactorMap = new HashMap<String, String>((int) numSpecies);
		speciesToCompartmentNameMap = new HashMap<String, String>((int) numSpecies);
		speciesToCompartmentSizeMap = new TObjectDoubleHashMap<String>((int) numSpecies);
		speciesIDSet = new LinkedHashSet<String>((int) numSpecies);
		reactionToNonconstantStoichiometriesSetMap = new HashMap<String, HashSet<StringStringPair> >();
		variableToValueMap = new TObjectDoubleHashMap<String>((int) numSpecies + (int) numParameters);
		
		reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (numReactions * 1.5));		
		reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));	
		reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));
		reactionToFormulaMap = new HashMap<String, ASTNode>((int) (numReactions * 1.5));
		
		componentToLocationMap = new LinkedHashMap<String, Point>();
		componentToReactionSetMap = new HashMap<String, HashSet<String> >();
		componentToVariableSetMap = new HashMap<String, HashSet<String> >();
		componentToEventSetMap = new HashMap<String, HashSet<String> >();
		
		ibiosimFunctionDefinitions.add("uniform");
		ibiosimFunctionDefinitions.add("exponential");
		ibiosimFunctionDefinitions.add("gamma");
		ibiosimFunctionDefinitions.add("chisq");
		ibiosimFunctionDefinitions.add("lognormal");
		ibiosimFunctionDefinitions.add("laplace");
		ibiosimFunctionDefinitions.add("cauchy");
		ibiosimFunctionDefinitions.add("poisson");
		ibiosimFunctionDefinitions.add("binomial");
		ibiosimFunctionDefinitions.add("bernoulli");
		ibiosimFunctionDefinitions.add("normal");
		
		initializationTime = System.nanoTime() - initTime1;
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
	private void alterNode(ASTNode node, String oldString, String newString) {}

	/**
	 * replaceArgument() doesn't work when you're replacing a localParameter, so this
	 * does that -- finds the oldString within node and replaces it with the local parameter
	 * specified by newString
	 * 
	 * @param node
	 * @param reactionID
	 * @param oldString
	 * @param newString
	 */
	private void alterLocalParameter(ASTNode node, Reaction reaction, String oldString, String newString) {}
	
	/**
	 * alters the kinetic laws and stoichiometries of grid diffusion reactions
	 * in accordance with the stoichiometry amplification parameters specified by the user
	 * 
	 * @param model
	 */
	private static void applyStoichiometryAmplification(Model model, double stoichAmpValue) {
		
		if (stoichAmpValue <= 1.0)
			return;
		
		Reaction reaction;
		SpeciesReference reactant;
		long i, j, reactionSize, reactantSize;
		
		reactionSize = model.getListOfReactions().size();
		
		for (i = 0; i < reactionSize; i++) {
			reaction = model.getReaction(i);
			
			//stoichiometry amplification -- alter the stoichiometry
			if (reaction.getId().contains("_Diffusion_")) {
				
				reactantSize = reaction.getListOfReactants().size();
				
				//these are all forward reactions, so adjust the stoichiometries of the reactant
				for (j = 0; j < reactantSize; j++)
				{
					reactant = reaction.getReactant(j);
					reactant.setStoichiometry(stoichAmpValue);
				}
					
				//alter the kinetic laws by dividing by the stoichiometry amplification value
				//this will alter the propensities accordingly
				ASTNode divisionNode = new ASTNode();
				divisionNode.setValue(1.0 / stoichAmpValue);
				reaction.getKineticLaw().getMath().multiplyTimeBy(divisionNode);
			}
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
	 * copies parts of the model that may change during a dynamic run
	 */
	protected void createModelCopy() {}
	
	/**
	 * uses an existing component to create a new component and update data structures and the simulation state
	 * this is used for "birth" events in dynamic models
	 * 
	 * @param parentComponentID
	 * @param eventID the ID of the division event that just fired
	 */
	protected void duplicateComponent(String parentComponentID, String eventID, String symmetry) 
	{}	
	
	/**
	 * simulator-specific component erasure stuff
	 * @param reactionIDs
	 */
	protected abstract void eraseComponentFurther(HashSet<String> reactionIDs);
	
	/**
	 * erases all traces of a component (ie, its reactions and species and parameters, etc) and updates data structures
	 * and the simulation state
	 * used for "death" events in dynamic models
	 * 
	 * @param componentID
	 */
	protected HashSet<String> eraseComponent(String componentID) {
		
		HashSet<String> deadEvents = new HashSet<String>();

		return deadEvents;
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
			
			case libsbmlConstants.AST_CONSTANT_TRUE:
				return 1.0;
				
			case libsbmlConstants.AST_CONSTANT_FALSE:
				return 0.0;
				
			case libsbmlConstants.AST_LOGICAL_NOT:
				return getDoubleFromBoolean(!(getBooleanFromDouble(evaluateExpressionRecursive(node.getLeftChild()))));
				
			case libsbmlConstants.AST_LOGICAL_AND: {
				
				boolean andResult = true;
				
				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					andResult = andResult && getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter)));
				
				return getDoubleFromBoolean(andResult);
			}
				
			case libsbmlConstants.AST_LOGICAL_OR: {
				
				boolean orResult = false;
				
				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					orResult = orResult || getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter)));
				
				return getDoubleFromBoolean(orResult);				
			}
				
			case libsbmlConstants.AST_LOGICAL_XOR: {
				
				boolean xorResult = getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(0)));
				
				for (int childIter = 1; childIter < node.getNumChildren(); ++childIter)
					xorResult = xorResult ^ getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter)));
				
				return getDoubleFromBoolean(xorResult);
			}
			
			case libsbmlConstants.AST_RELATIONAL_EQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) == evaluateExpressionRecursive(node.getRightChild()));
				
			case libsbmlConstants.AST_RELATIONAL_NEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) != evaluateExpressionRecursive(node.getRightChild()));
				
			case libsbmlConstants.AST_RELATIONAL_GEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) >= evaluateExpressionRecursive(node.getRightChild()));
				
			case libsbmlConstants.AST_RELATIONAL_LEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) <= evaluateExpressionRecursive(node.getRightChild()));
				
			case libsbmlConstants.AST_RELATIONAL_GT:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) > evaluateExpressionRecursive(node.getRightChild()));
				
			case libsbmlConstants.AST_RELATIONAL_LT:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(node.getLeftChild()) < evaluateExpressionRecursive(node.getRightChild()));			
			}			
		}
		
		//if it's a mathematical constant
		else if (node.isConstant()) {
			
			switch (node.getType()) {
			
			case libsbmlConstants.AST_CONSTANT_E:
				return Math.E;
				
			case libsbmlConstants.AST_CONSTANT_PI:
				return Math.PI;
			}
		}
		
		//if it's a number
		else if (node.isNumber())
			return node.getReal();
		
		//if it's a user-defined variable
		//eg, a species name or global/local parameter
		else if (node.isName()) {
			
			String name = node.getName().replace("_negative_","-");
				
			if (node.getType() == libsbmlConstants.AST_NAME_TIME) {
				
				return currentTime;
			}
			//if it's a reaction id return the propensity
			else if (reactionToPropensityMap.keySet().contains(node.getName())) {
				return reactionToPropensityMap.get(node.getName());
			}
			else {
				
				double value;
				
				if (this.speciesToHasOnlySubstanceUnitsMap.containsKey(name) &&
						this.speciesToHasOnlySubstanceUnitsMap.get(name) == false) {
					
					value = (variableToValueMap.get(name) / variableToValueMap.get(speciesToCompartmentNameMap.get(name)));
				}
				else				
					value = variableToValueMap.get(name);
				return value;
			}
		}
		
		//operators/functions with two children
		else {
			
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();
			
			switch (node.getType()) {
			
			case libsbmlConstants.AST_PLUS: {
				
				double sum = 0.0;
				
				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					sum += evaluateExpressionRecursive(node.getChild(childIter));					
					
				return sum;
			}
				
			case libsbmlConstants.AST_MINUS: {
				
				double sum = evaluateExpressionRecursive(leftChild);
				
				for (int childIter = 1; childIter < node.getNumChildren(); ++childIter)
					sum -= evaluateExpressionRecursive(node.getChild(childIter));					
					
				return sum;
			}
				
			case libsbmlConstants.AST_TIMES: {
				
				double product = 1.0;
				
				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					product *= evaluateExpressionRecursive(node.getChild(childIter));
				
				return product;
			}
				
			case libsbmlConstants.AST_DIVIDE:
				return (evaluateExpressionRecursive(leftChild) / evaluateExpressionRecursive(rightChild));
				
			case libsbmlConstants.AST_FUNCTION_POWER:
				return (FastMath.pow(evaluateExpressionRecursive(leftChild), evaluateExpressionRecursive(rightChild)));
				
			case libsbmlConstants.AST_FUNCTION: {
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
					return 0;
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
					
				}
				else if (nodeName.equals("neighborQuantityLeftFull")) {
					
					int leftIndex = (int) evaluateExpressionRecursive(node.getChild(1));
					int rightIndex = (int) evaluateExpressionRecursive(node.getChild(2));
					String specName = node.getChild(0).getName().split("__")[node.getChild(0).getName().split("__").length - 1];
					String speciesName = "ROW" + leftIndex + "_COL" + (rightIndex - 1) + "__" + specName;
					
					if (variableToValueMap.containsKey(speciesName))
						return variableToValueMap.get(speciesName);
					else return 1;
				}
				else if (nodeName.equals("neighborQuantityRightFull")) {
					
					int leftIndex = (int) evaluateExpressionRecursive(node.getChild(1));
					int rightIndex = (int) evaluateExpressionRecursive(node.getChild(2));
					String specName = node.getChild(0).getName().split("__")[node.getChild(0).getName().split("__").length - 1];
					String speciesName = "ROW" + leftIndex + "_COL" + (rightIndex + 1) + "__" + specName;
					
					if (variableToValueMap.containsKey(speciesName)) {
						return variableToValueMap.get(speciesName);
					}
					else return 1;
				}
				else if (nodeName.equals("neighborQuantityAboveFull")) {
					
					int leftIndex = (int) evaluateExpressionRecursive(node.getChild(1));
					int rightIndex = (int) evaluateExpressionRecursive(node.getChild(2));
					String specName = node.getChild(0).getName().split("__")[node.getChild(0).getName().split("__").length - 1];
					String speciesName = "ROW" + (leftIndex - 1) + "_COL" + rightIndex + "__" + specName;
					
					if (variableToValueMap.containsKey(speciesName))
						return variableToValueMap.get(speciesName);
					else return 1;
				}
				else if (nodeName.equals("neighborQuantityBelowFull")) {
					
					int leftIndex = (int) evaluateExpressionRecursive(node.getChild(1));
					int rightIndex = (int) evaluateExpressionRecursive(node.getChild(2));
					String specName = node.getChild(0).getName().split("__")[node.getChild(0).getName().split("__").length - 1];
					String speciesName = "ROW" + (leftIndex + 1) + "_COL" + rightIndex + "__" + specName;
					
					if (variableToValueMap.containsKey(speciesName))
						return variableToValueMap.get(speciesName);
					else return 1;
				}
				else if (nodeName.equals("getCompartmentLocationX")) {
					
					return this.componentToLocationMap.get(node.getChild(0).getName().split("__")[0]).getX();
				}
				else if (nodeName.equals("getCompartmentLocationY")) {
					
					return this.componentToLocationMap.get(node.getChild(0).getName().split("__")[0]).getY();
				}
				
				break;
			}
			
			case libsbmlConstants.AST_FUNCTION_ABS:
				return FastMath.abs(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCOS:
				return FastMath.acos(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCSIN:
				return FastMath.asin(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCTAN:
				return FastMath.atan(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_CEILING:
				return FastMath.ceil(evaluateExpressionRecursive(node.getChild(0)));				
			
			case libsbmlConstants.AST_FUNCTION_COS:
				return FastMath.cos(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_COSH:
				return FastMath.cosh(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_EXP:
				return FastMath.exp(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_FLOOR:
				return FastMath.floor(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_LN:
				return FastMath.log(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_LOG:
				return FastMath.log10(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_SIN:
				return FastMath.sin(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_SINH:
				return FastMath.sinh(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_TAN:
				return FastMath.tan(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_TANH:		
				return FastMath.tanh(evaluateExpressionRecursive(node.getChild(0)));
				
			case libsbmlConstants.AST_FUNCTION_PIECEWISE: {
				
				//loop through child triples
				//if child 1 is true, return child 0, else return child 2				
				for (int childIter = 0; childIter < node.getNumChildren(); childIter += 3) {
					
					if ((childIter + 1) < node.getNumChildren() && 
							getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter + 1)))) {
						return evaluateExpressionRecursive(node.getChild(childIter));
					}
					else if ((childIter + 2) < node.getNumChildren()) {
						return evaluateExpressionRecursive(node.getChild(childIter + 2));
					}
				}
				
				return 0;
			}
			
			case libsbmlConstants.AST_FUNCTION_ROOT:
				return FastMath.pow(evaluateExpressionRecursive(node.getRightChild()), 
						1 / evaluateExpressionRecursive(node.getLeftChild()));
			
			case libsbmlConstants.AST_FUNCTION_SEC:
				return Fmath.sec(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_SECH:
				return Fmath.sech(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_FACTORIAL:
				return Fmath.factorial(evaluateExpressionRecursive(node.getChild(0)));
				
			case libsbmlConstants.AST_FUNCTION_COT:
				return Fmath.cot(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_COTH:
				return Fmath.coth(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_CSC:
				return Fmath.csc(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_CSCH:
				return Fmath.csch(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_DELAY:
				//NOT PLANNING TO SUPPORT THIS
				return 0;
				
			case libsbmlConstants.AST_FUNCTION_ARCTANH:
				return Fmath.atanh(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCSINH:
				return Fmath.asinh(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCOSH:
				return Fmath.acosh(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCOT:
				return Fmath.acot(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCOTH:
				return Fmath.acoth(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCSC:
				return Fmath.acsc(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCSCH:
				return Fmath.acsch(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCSEC:
				return Fmath.asec(evaluateExpressionRecursive(node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCSECH:
				return Fmath.asech(evaluateExpressionRecursive(node.getChild(0)));
				
			} //end switch
			
		}
		return 0.0;
	}

	/**
	 * adds species and reactions to the model that are implicit in arrays
	 * basically, it takes an arrayed model and flattens it
	 * 
	 * this also applies stoichiometry amplification at the end (if needed)
	 * i did it this way to avoid another read/write of the model, as i have to
	 * do it within the simulator in order to use jsbml instead of libsbml
	 */
	public static void expandArrays(String filename, double stoichAmpValue) {}
	

	/**
	 * recursively puts every astnode child into the arraylist passed in
	 * 
	 * @param node
	 * @param nodeChildrenList
	 */
	protected void getAllASTNodeChildren(ASTNode node, ArrayList<ASTNode> nodeChildrenList) {
		
		ASTNode child;
		long size = node.getNumChildren();
		
		for (int i = 0; i < size; i++) {
			child = node.getChild(i);
			if (child.getNumChildren() == 0)
				nodeChildrenList.add(child);
			else {
				nodeChildrenList.add(child);
				getAllASTNodeChildren(child, nodeChildrenList);
			}
		}			
	}
	
	/**
	 * returns a set of all the reactions that the recently performed reaction affects
	 * "affect" means that the species updates will change the affected reaction's propensity
	 * 
	 * @param selectedReactionID the reaction that was recently performed
	 * @return the set of all reactions that the performed reaction affects the propensity of
	 */
	protected HashSet<String> getAffectedReactionSet(String selectedReactionID, boolean noAssignmentRulesFlag) {
		
		HashSet<String> affectedReactionSet = new HashSet<String>(20);
		affectedReactionSet.add(selectedReactionID);
		
		//loop through the reaction's reactants and products
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
		else if (node.isFunction() && node.getName().equals(quarry))
			satisfyingNodes.add(node);
		else {
			ASTNode childNode;
			for (int i = 0; i < node.getNumChildren(); i++)
			{
				childNode = node.getChild(i);
				getSatisfyingNodes(childNode, quarry, satisfyingNodes);
			}
		}
	}
	
	/**
	 * recursively puts the nodes that have the same name as the quarry string passed in into the arraylist passed in
	 * so, the entire tree is searched through, which i don't think is possible with the jsbml methods
	 * the lax version uses contains instead of equals
	 * 
	 * @param node node to search through
	 * @param quarry string to search for
	 * @param satisfyingNodes list of nodes that satisfy the condition
	 */
	void getSatisfyingNodesLax(ASTNode node, String quarry, ArrayList<ASTNode> satisfyingNodes) {
		
		if (node.isName() && node.getName().contains(quarry))
			satisfyingNodes.add(node);
		else if (node.isFunction() && node.getName().contains(quarry))
			satisfyingNodes.add(node);
		else {
			ASTNode childNode;
			for (int i = 0; i < node.getNumChildren(); i++)
			{
				childNode = node.getChild(i);
				getSatisfyingNodesLax(childNode, quarry, satisfyingNodes);
			}
		}		
	}
	
	/**
	 * updates the event queue and fires events and so on
	 * @param currentTime the current time in the simulation
	 */
	protected void handleEvents(final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {}
	
	public void replaceArgument(ASTNode formula,String bvar, ASTNode arg) {}
	
	/**
	 * inlines a formula with function definitions
	 * 
	 * @param formula
	 * @return
	 */
	protected ASTNode inlineFormula(ASTNode formula) {
		
		if (formula.isFunction() == false ||
				(formula.getLeftChild() == null && formula.getRightChild() == null)) {
			
			for (int i = 0; i < formula.getNumChildren(); ++i)
				formula.replaceChild(i, inlineFormula(formula.getChild(i)));//.clone()));
		}
		
		if (formula.isFunction() && model.getFunctionDefinition(formula.getName()) != null) {
			
			if (ibiosimFunctionDefinitions.contains(formula.getName()))
				return formula;
			
			ASTNode inlinedFormula = model.getFunctionDefinition(formula.getName()).getBody().deepCopy();
			ASTNode oldFormula = formula.deepCopy();
			
			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			this.getAllASTNodeChildren(inlinedFormula, inlinedChildren);
			
			if (inlinedChildren.size() == 0)
				inlinedChildren.add(inlinedFormula);
			
			HashMap<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();
			
			for (int i = 0; i < model.getFunctionDefinition(formula.getName()).getNumArguments(); ++i) {
				inlinedChildToOldIndexMap.put(model.getFunctionDefinition(formula.getName()).getArgument(i).getName(), i);
			}
			
			for (int i = 0; i < inlinedChildren.size(); ++i) {
				
				ASTNode child = inlinedChildren.get(i);
				
				if ((child.getLeftChild() == null && child.getRightChild() == null) && child.isName()) {
					
					int index = inlinedChildToOldIndexMap.get(child.getName());
					replaceArgument(inlinedFormula,libsbml.formulaToString(child), oldFormula.getChild(index));
					
					if (inlinedFormula.getNumChildren() == 0)
						inlinedFormula = oldFormula.getChild(index);
				}
			}
			
			return inlinedFormula;
		}
		else {
			return formula;
		}
	}
	
	/**
	 * moves a component in a given direction
	 * moves components out of the way if needed
	 * creates grid reactions if the grid expands
	 * 
	 * @param parentComponentID
	 * @param direction
	 */
	protected void moveComponent(String parentComponentID, String childComponentID, Point parentLocation, Point childLocation,
			int direction, HashSet<String> reactionsToAdjust) {	}
	
	/**
	 * performs assignment rules that may have changed due to events or reactions firing
	 * 
	 * @param affectedAssignmentRuleSet the set of assignment rules that have been affected
	 */
	protected HashSet<String> performAssignmentRules(HashSet<AssignmentRule> affectedAssignmentRuleSet) {
		
		HashSet<String> affectedVariables = new HashSet<String>();
		
		for (AssignmentRule assignmentRule : affectedAssignmentRuleSet) {
			
			String variable = assignmentRule.getVariable();
			
			//update the species count (but only if the species isn't constant) (bound cond is fine)
			if (variableToIsConstantMap.containsKey(variable) && variableToIsConstantMap.get(variable) == false
					|| variableToIsConstantMap.containsKey(variable) == false) {
				
				if (speciesToHasOnlySubstanceUnitsMap.containsKey(variable) &&
						speciesToHasOnlySubstanceUnitsMap.get(variable) == false) {
					
					variableToValueMap.put(variable, 
							evaluateExpressionRecursive(assignmentRule.getMath()) * 
							variableToValueMap.get(speciesToCompartmentNameMap.get(variable)));
				}
				else {
					variableToValueMap.put(variable, evaluateExpressionRecursive(assignmentRule.getMath()));
				}
				
				affectedVariables.add(variable);
			}
		}
		
		return affectedVariables;
	}
	
	/**
	 * performs every rate rule using the current time step
	 * 
	 * @param delta_t
	 * @return
	 */
	protected HashSet<String> performRateRules(double delta_t) {
		
		HashSet<String> affectedVariables = new HashSet<String>();
		
		return affectedVariables;
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
			
			//this means the stoichiometry isn't constant, so look to the variableToValue map
			if (reactionToNonconstantStoichiometriesSetMap.containsKey(selectedReactionID)) {
				
				for (StringStringPair doubleID : reactionToNonconstantStoichiometriesSetMap.get(selectedReactionID)) {
					
					//string1 is the species ID; string2 is the speciesReference ID
					if (doubleID.string1.equals(speciesID)) {
						
						stoichiometry = variableToValueMap.get(doubleID.string2);
						
						//this is to get the plus/minus correct, as the variableToValueMap has
						//a stoichiometry without the reactant/product plus/minus data
						stoichiometry *= (int)(speciesAndStoichiometry.doub/Math.abs(speciesAndStoichiometry.doub));
						break;
					}
				}
			}
			
			//update the species count if the species isn't a boundary condition or constant
			//note that the stoichiometries are earlier modified with the correct +/- sign
			boolean cond1 = speciesToIsBoundaryConditionMap.get(speciesID);
			boolean cond2 = variableToIsConstantMap.get(speciesID);
			if (!cond1 && !cond2) {
				
				if (speciesToConversionFactorMap.containsKey(speciesID)) {
					variableToValueMap.adjustValue(speciesID, stoichiometry * 
							variableToValueMap.get(speciesToConversionFactorMap.get(speciesID)));
				}
				else {
					variableToValueMap.adjustValue(speciesID, stoichiometry);
				}
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
	 * recursively finds all variable nodes and prepends a string to the variable
	 * static version
	 * 
	 * @param node
	 * @param toPrepend
	 */
	private static void prependToVariableNodes(ASTNode node, String toPrepend, Model model) {
		
	}
	
	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException 
	 */
	protected void printToTSD(double printTime) throws IOException {
		
		String commaSpace = "";
		
		bufferedTSDWriter.write("(");
		
		commaSpace = "";
		
		//print the current time
		bufferedTSDWriter.write(printTime + ", ");
		
		//if there's an interesting species, only those get printed
		if (interestingSpecies.size() > 0) {
			
			for (String speciesID : interestingSpecies) {
				
				if (printConcentrations == true) {
					
					bufferedTSDWriter.write(commaSpace + 
							(variableToValueMap.get(speciesID) / variableToValueMap.get(speciesToCompartmentNameMap.get(speciesID))));
				}
				else
					bufferedTSDWriter.write(commaSpace + variableToValueMap.get(speciesID));
					
				commaSpace = ", ";
			}
			
			//always print component location values
			for (String componentID : componentToLocationMap.keySet()) {
				bufferedTSDWriter.write(commaSpace + (int) componentToLocationMap.get(componentID).getX());
				bufferedTSDWriter.write(commaSpace + (int) componentToLocationMap.get(componentID).getY());
			}
		}
		else {
			
			//loop through the speciesIDs and print their current value to the file
			for (String speciesID : speciesIDSet) {
				
				bufferedTSDWriter.write(commaSpace + variableToValueMap.get(speciesID));
				commaSpace = ", ";
			}
			
			//print component location values
			for (String componentID : componentToLocationMap.keySet()) {
				bufferedTSDWriter.write(commaSpace + (int) componentToLocationMap.get(componentID).getX());
				commaSpace = ", ";
				bufferedTSDWriter.write(commaSpace + (int) componentToLocationMap.get(componentID).getY());
			}
			
			//print compartment sizes
			for (String componentID : compartmentIDSet) {
				bufferedTSDWriter.write(commaSpace + variableToValueMap.get(componentID));
				commaSpace = ", ";
			}
			
			//print nonconstant parameter values
			for (String parameterID : nonconstantParameterIDSet) {
				bufferedTSDWriter.write(commaSpace + variableToValueMap.get(parameterID));
				commaSpace = ", ";
			}
		}
		
		bufferedTSDWriter.write(")");
		bufferedTSDWriter.flush();
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
		HashSet<String> speciesSet = new HashSet<String>();
		
		
		//store the TSD data for analysis
		for (int run = 1; run <= numRuns; ++run) {
			
			DTSDParser dtsdParser = null;
			TSDParser tsdParser = null;
			HashMap<String, ArrayList<Double> > runStatistics = null;

				tsdParser = new TSDParser(outputDirectory + "run-" + run + ".tsd", false);			
				allSpecies = tsdParser.getSpecies();				
				runStatistics = tsdParser.getHashMap();
			
			
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
	 * reverts the model back to its pre-dynamic state
	 * (during duplication events, the model is altered because of jsbml restrictions)
	 */
	protected void resetModel() {}
	
	/**
	 * non-static version of the method that flattens arrays into the sbml model
	 * this one doesn't print the model back out, though
	 */
	protected void setupArrays() {}
	
	/**
	 * creates hashmaps for representing and keeping track of a grid (of cells)
	 */
	protected void setupGrid() {}
	
	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	private void setupSingleSpecies(Species species, String speciesID) {
		if (speciesIDSet.contains(speciesID))
			return;
		if (species.isSetInitialAmount())
			variableToValueMap.put(speciesID, species.getInitialAmount());

		speciesToAffectedReactionSetMap.put(speciesID, new HashSet<String>(20));
		speciesToIsBoundaryConditionMap.put(speciesID, species.getBoundaryCondition());
		variableToIsConstantMap.put(speciesID, species.getConstant());
		speciesToHasOnlySubstanceUnitsMap.put(speciesID, species.getHasOnlySubstanceUnits());
		speciesIDSet.add(speciesID);
	}
	
	/**
	 * puts species-related information into data structures
	 * 
	 * @throws IOException
	 */
	protected void setupSpecies() throws IOException {
		
		//add values to hashmap for easy access to species amounts
		Species species;
		long size = model.getListOfSpecies().size();
		for (int i = 0; i < size; i++) 
		{
			species = model.getSpecies(i);
			setupSingleSpecies(species, species.getId());
		}
	}

	/**
	 * puts initial assignment-related information into data structures
	 */
	protected void setupInitialAssignments() 
	{
		HashSet<AssignmentRule> allAssignmentRules = new HashSet<AssignmentRule>();
		long size;
		
		performAssignmentRules(allAssignmentRules);
		
		HashSet<String> affectedVariables = new HashSet<String>();
		
		//calculate initial assignments a lot of times in case there are dependencies
		//running it the number of initial assignments times will avoid problems
		//and all of them will be fully calculated and determined
		for (int i = 0; i < numInitialAssignments; ++i) {
			size = model.getListOfInitialAssignments().size();
			for (long j = 0; j < size; j++) {
				InitialAssignment initialAssignment = model.getListOfInitialAssignments().get(j);
				
				String variable = initialAssignment.getSymbol().replace("_negative_","-");				
				initialAssignment.setMath(inlineFormula(initialAssignment.getMath()));
				
				if (speciesToHasOnlySubstanceUnitsMap.containsKey(variable) &&
						speciesToHasOnlySubstanceUnitsMap.get(variable) == false) {
					
					variableToValueMap.put(variable, 
							evaluateExpressionRecursive(initialAssignment.getMath()) * 
							variableToValueMap.get(speciesToCompartmentNameMap.get(variable)));
				}
				else {
					variableToValueMap.put(variable, evaluateExpressionRecursive(initialAssignment.getMath()));
				}			
				
				affectedVariables.add(variable);
			}
		}
		//perform assignment rules again for variable that may have changed due to the initial assignments
				//they aren't set up yet, so just perform them all
				performAssignmentRules(allAssignmentRules);
				
				//this is kind of weird, but apparently if an initial assignment changes a compartment size
				//i need to go back and update species amounts because they used the non-changed-by-assignment sizes
				size = model.getListOfSpecies().size();
				for (long j = 0; j < size; j++)
				{
						Species species = model.getListOfSpecies().get(j);
					
					if (species.isSetInitialConcentration()) {
						
						String speciesID = species.getId();
						
						//revert to the initial concentration value
						if (Double.isNaN(variableToValueMap.get(speciesID)) == false)
							variableToValueMap.put(speciesID, 
									variableToValueMap.get(speciesID) / model.getCompartment(species.getCompartment()).getSize());
						else
							variableToValueMap.put(speciesID, species.getInitialConcentration());
						
						//multiply by the new compartment size to get into amount
						variableToValueMap.put(speciesID, variableToValueMap.get(speciesID) * 
								variableToValueMap.get(speciesToCompartmentNameMap.get(speciesID)));
					}
				}
	}
	
	/**
	 * sets up the local parameters in a single kinetic law
	 * 
	 * @param kineticLaw
	 * @param reactionID
	 */
	private void setupLocalParameters(KineticLaw kineticLaw, Reaction reaction) {
		
		String reactionID = reaction.getId();
		reactionID = reactionID.replace("_negative_","-");
		
		for (int i = 0; i < kineticLaw.getNumParameters(); i++) {

			LocalParameter localParameter = kineticLaw.getLocalParameter(i);
			
			String parameterID = "";
			
			//the parameters don't get reset after each run, so don't re-do this prepending
			if (localParameter.getId().contains(reactionID + "_") == false)					
				parameterID = reactionID + "_" + localParameter.getId();
			else 
				parameterID = localParameter.getId();
							
			String oldParameterID = localParameter.getId();
			variableToValueMap.put(parameterID, localParameter.getValue());
						
			//alter the local parameter ID so that it goes to the local and not global value
			if (localParameter.getId() != parameterID) {
				localParameter.setId(parameterID);
				localParameter.setMetaId(parameterID);
			}
			
			//for some reason, changing the local parameter sometimes changes the kinetic law instances
			//of that parameter id (and sometimes doesn't), so those ones are fine and ignore them
			alterLocalParameter(kineticLaw.getMath(), reaction, oldParameterID, parameterID);
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
		
		if (parameter.getConstant() == false)
			nonconstantParameterIDSet.add(parameterID);
		
	}

	/**
	 * puts parameter-related information into data structures
	 */
	protected void setupParameters() {
		
		//add local parameters
		Reaction reaction;
		Parameter parameter;
		Compartment compartment;
		long size;
		
		
		size = model.getListOfReactions().size();
		for (int i = 0; i < size; i++) 
		{
			reaction = model.getReaction(i);
			KineticLaw kineticLaw = reaction.getKineticLaw();
			setupLocalParameters(kineticLaw, reaction);
		}
		
		//add values to hashmap for easy access to global parameter values
		//NOTE: the IDs for the parameters and species must be unique, so putting them in the
		//same hashmap is okay
		
		size = model.getListOfParameters().size();
		for (int i = 0; i < size; i++) 
		{
			parameter = model.getParameter(i);
			setupSingleParameter(parameter);
		}
		
		//add compartment sizes in
		size = model.getListOfCompartments().size();
		for (int i = 0; i < size; i++) {
			compartment = model.getCompartment(i);
			String compartmentID = compartment.getId();
			
			compartmentIDSet.add(compartmentID);
			variableToValueMap.put(compartmentID, compartment.getSize());
			
			if (Double.isNaN(compartment.getSize()))
				variableToValueMap.put(compartmentID, 1.0);
			
			variableToIsConstantMap.put(compartmentID, compartment.getConstant());
			
		}
	}
	
	/**
	 * puts rule-related information into data structures
	 */
	protected void setupRules(){}
	
	/**
	 * puts constraint-related information into data structures
	 */
	protected void setupConstraints() {}

	/**
	 * sets up a single event
	 * 
	 * @param event
	 */
	protected void setupSingleEvent(Event event) {}
	
	/**
	 * puts event-related information into data structures
	 */
	protected void setupEvents() {}
	
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
			
			TSDWriter = new FileWriter(outputDirectory + "run-" + currentRun + extension);
			bufferedTSDWriter = new BufferedWriter(TSDWriter);
			bufferedTSDWriter.write('(');
			
			if (currentRun > 1) {
			
				bufferedTSDWriter.write("(" + "\"" + "time" + "\"");
				
				//if there's an interesting species, only those get printed
				if (interestingSpecies.size() > 0) {
					
					for (String speciesID : interestingSpecies)
						bufferedTSDWriter.write(", \"" + speciesID + "\"");
					
										
						bufferedTSDWriter.write("),\n");
				}
				else {
				
					for (String speciesID : speciesIDSet) {
						
						bufferedTSDWriter.write(", \"" + speciesID + "\"");
					}
					
					//print compartment IDs (for sizes)
					for (String componentID : compartmentIDSet) {
						
						bufferedTSDWriter.write(", \"" + componentID + "\"");
					}
					
					//print nonconstant parameter IDs
					for (String parameterID : nonconstantParameterIDSet) {
						
						try {
							bufferedTSDWriter.write(", \"" + parameterID + "\"");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					bufferedTSDWriter.write("),\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			ListOfSpeciesReferences reactantsList, ListOfSpeciesReferences productsList, 
			ListOfSpeciesReferences modifiersList) {
	reactionID = reactionID.replace("_negative_","-");
		
		boolean notEnoughMoleculesFlag = false;
		long size;
		
		reactionToSpeciesAndStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());
		reactionToReactantStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());
		
		size = reactantsList.size();
		for (int i = 0; i < size; i++)
		{
					
			SpeciesReference reactant = (SpeciesReference)reactantsList.get(i);
			
			String reactantID = reactant.getSpecies().replace("_negative_","-");
				
			//stoichiometry amplification -- alter the stoichiometry
			if (reactionID.contains("_Diffusion_") && stoichAmpBoolean) 
				reactant.setStoichiometry(stoichAmpGridValue);
				
				double reactantStoichiometry;
				
				//if there was an initial assignment for the speciesref id
				if (variableToValueMap.containsKey(reactant.getId()))
					reactantStoichiometry = variableToValueMap.get(reactant.getId());
				else
					reactantStoichiometry = reactant.getStoichiometry();
				
				reactionToSpeciesAndStoichiometrySetMap.get(reactionID).add(
						new StringDoublePair(reactantID, -reactantStoichiometry));
				reactionToReactantStoichiometrySetMap.get(reactionID).add(
						new StringDoublePair(reactantID, reactantStoichiometry));
					
				//if there wasn't an initial assignment
				if (reactant.getConstant() == false &&
						variableToValueMap.containsKey(reactant.getId()) == false &&
						reactant.getId().length() > 0) {
					
					if (reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
						reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());
					
					reactionToNonconstantStoichiometriesSetMap.get(reactionID)
					.add(new StringStringPair(reactantID, reactant.getId()));
					variableToValueMap.put(reactant.getId(), reactantStoichiometry);
				}
				
				//as a reactant, this species affects the reaction's propensity
				speciesToAffectedReactionSetMap.get(reactantID).add(reactionID);
				
	
			}
			
			size = productsList.size();
			for (int i = 0; i < size; i ++) {
				SpeciesReference product = (SpeciesReference)productsList.get(i); 
				
				//stoichiometry amplification -- alter the stoichiometry
				if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
					product.setStoichiometry(stoichAmpGridValue);
				
				String productID = product.getSpecies().replace("_negative_","-");
				double productStoichiometry;
				
				//if there was an initial assignment for the speciesref id
				if (variableToValueMap.containsKey(product.getId()))
					productStoichiometry = variableToValueMap.get(product.getId());
				else
					productStoichiometry = product.getStoichiometry();
				
				reactionToSpeciesAndStoichiometrySetMap.get(reactionID).add(
						new StringDoublePair(productID, productStoichiometry));
				
				if (product.getConstant() == false && 
						variableToValueMap.containsKey(product.getId()) == false &&
						product.getId().length() > 0) {
					
					if (reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
						reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());
					
					reactionToNonconstantStoichiometriesSetMap.get(reactionID)
					.add(new StringStringPair(productID, product.getId()));
					variableToValueMap.put(product.getId(), productStoichiometry);
				}
				
				//don't need to check if there are enough, because products are added
			}
			size = productsList.size();
			for (int i = 0; i < size; i++)
			{
				ModifierSpeciesReference modifier = (ModifierSpeciesReference)modifiersList.get(i);
				
				String modifierID = modifier.getSpecies();
				modifierID = modifierID.replace("_negative_","-");
				
				//as a modifier, this species affects the reaction's propensity
				speciesToAffectedReactionSetMap.get(modifierID).add(reactionID);
			}
			
			reactionToFormulaMap.put(reactionID, inlineFormula(reactionFormula));
			
			double propensity;
			
	
				//calculate propensity
				propensity = evaluateExpressionRecursive(inlineFormula(reactionFormula));

				//stoichiometry amplification -- alter the propensity
				if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
					propensity *= (1.0 / stoichAmpGridValue);
				
				if (propensity < minPropensity && propensity > 0) 
					minPropensity = propensity;
				if (propensity > maxPropensity)
					maxPropensity = propensity;
				
				totalPropensity += propensity;
			
			
			reactionToPropensityMap.put(reactionID, propensity);
		}	
	
	/**
	 * calculates the initial propensities for each reaction in the model
	 * 
	 * @param numReactions the number of reactions in the model
	 */
	protected void setupReactions() {
		
		//loop through all reactions and calculate their propensities
		Reaction reaction;
		
		for (int i = 0;  i< model.getListOfReactions().size(); i++) {
			reaction = model.getReaction(i);
			String reactionID = reaction.getId();
			ASTNode reactionFormula = reaction.getKineticLaw().getMath();
						
			setupSingleReaction(reactionID, reactionFormula, reaction.getReversible(), 
					reaction.getListOfReactants(), reaction.getListOfProducts(), reaction.getListOfModifiers());
		}
	}
	
	/**
	 * abstract simulate method
	 * each simulator needs a simulate method
	 */
	protected abstract void simulate();
	
	/**
	 * returns an annotation with only array information remaining
	 * 
	 * @param annotation
	 * @return
	 */
	public static String stripAnnotation(String annotation) { return "";	}
	
	/**
	 * this evaluates a set of constraints that have been affected by an event or reaction firing
	 * and returns the OR'd boolean result
	 * 
	 * @param affectedConstraintSet the set of constraints affected
	 * @return the boolean result of the constraints' evaluation
	 */
	protected boolean testConstraints(HashSet<ASTNode> affectedConstraintSet) {
		
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
	private void updatePropensities(HashSet<String> reactionSet) {}
	
	
	//EVENT TO FIRE INNER CLASS
	/**
	 * easy way to store multiple data points for events that are firing
	 */
	protected class EventToFire {}
	
	
	//EVENT COMPARATOR INNER CLASS
	/**
	 * compares two events to see which one should be before the other in the priority queue
	 */
	protected class EventComparator implements Comparator<EventToFire> {

		/**
		 * compares two events based on their fire times and priorities
		 */
		public int compare(EventToFire event1, EventToFire event2) 
		{
			return 0;
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
	
	//STRING STRING PAIR INNER CLASS	
	/**
	 * class to combine a string and a string
	 */
	protected class StringStringPair {
		
		public String string1;
		public String string2;
		
		StringStringPair(String s1, String s2) {
			
			string1 = s1;
			string2 = s2;
		}
	}
}



