package backend.analysis.dynamicsim.flattened;

import flanagan.math.Fmath;
import flanagan.math.PsRandom;
import frontend.main.Gui;
import frontend.main.util.dataparser.DTSDParser;
import frontend.main.util.dataparser.DataParser;
import frontend.main.util.dataparser.TSDParser;
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
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import odk.lang.FastMath;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Annotation;
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
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.text.parser.ParseException;

import backend.analysis.dynamicsim.ParentSimulator;
import backend.biomodel.annotation.AnnotationUtility;
import backend.biomodel.util.GlobalConstants;
import backend.biomodel.util.SBMLutilities;

public abstract class Simulator implements ParentSimulator
{

	// SBML model
	protected Model											model										= null;

	// generates random numbers based on the xorshift method
	protected XORShiftRandom								randomNumberGenerator						= null;

	// allows for access to a propensity from a reaction ID
	protected TObjectDoubleHashMap<String>					reactionToPropensityMap						= null;

	// allows for access to reactant/product speciesID and stoichiometry from a
	// reaction ID
	// note that species and stoichiometries need to be thought of as unique for
	// each reaction
	protected HashMap<String, HashSet<StringDoublePair>>	reactionToSpeciesAndStoichiometrySetMap		= null;

	protected HashMap<String, HashSet<StringStringPair>>	reactionToNonconstantStoichiometriesSetMap	= null;

	// allows for access to reactant/modifier speciesID and stoichiometry from a
	// reaction ID
	protected HashMap<String, HashSet<StringDoublePair>>	reactionToReactantStoichiometrySetMap		= null;

	// allows for access to a kinetic formula tree from a reaction
	protected HashMap<String, ASTNode>						reactionToFormulaMap						= null;

	// contains all rate rules
	protected HashSet<RateRule>								listOfRateRules;
	// contains all assignment rules
	protected HashSet<AssignmentRule>						listOfAssignmentRules;

	// allows for access to a set of reactions that a species is in (as a
	// reactant or modifier) from a species ID
	protected HashMap<String, HashSet<String>>				speciesToAffectedReactionSetMap				= null;

	// allows for access to species booleans from a species ID
	protected HashMap<String, Boolean>						speciesToIsBoundaryConditionMap				= null;
	protected HashMap<String, Boolean>						speciesToHasOnlySubstanceUnitsMap			= null;
	protected HashMap<String, String>						speciesToSubstanceUnitsMap					= null;
	protected HashMap<String, String>						speciesToConversionFactorMap				= null;
	protected HashMap<String, String>						speciesToCompartmentNameMap					= null;
	protected TObjectDoubleHashMap<String>					speciesToCompartmentSizeMap					= null;

	// a linked (ordered) set of all species IDs, to allow easy access to their
	// values via the variableToValue map
	protected LinkedHashSet<String>							speciesIDSet								= null;

	// allows for access to species and parameter values from a variable ID
	protected TObjectDoubleHashMap<String>					variableToValueMap							= null;

	// stores events in order of fire time and priority
	protected PriorityQueue<EventToFire>					triggeredEventQueue							= null;
	protected HashSet<String>								untriggeredEventSet							= null;

	// hashmaps that allow for access to event information from the event's id
	protected HashMap<String, ASTNode>						eventToPriorityMap							= null;
	protected HashMap<String, ASTNode>						eventToDelayMap								= null;
	protected HashMap<String, Boolean>						eventToHasDelayMap							= null;
	protected HashMap<String, Boolean>						eventToTriggerPersistenceMap				= null;
	protected HashMap<String, Boolean>						eventToUseValuesFromTriggerTimeMap			= null;
	protected HashMap<String, ASTNode>						eventToTriggerMap							= null;
	protected HashMap<String, Boolean>						eventToTriggerInitiallyTrueMap				= null;
	protected HashMap<String, Boolean>						eventToPreviousTriggerValueMap				= null;
	protected HashMap<String, HashSet<Object>>				eventToAssignmentSetMap						= null;

	// allows for access to the reactions whose propensity changes when an event
	// fires
	protected HashMap<String, HashSet<String>>				eventToAffectedReactionSetMap				= null;

	// allows for access to the set of events that a variable is in
	protected HashMap<String, HashSet<String>>				variableToEventSetMap						= null;

	// allows for access to the set of assignment rules that a variable (rhs) in
	// an assignment rule affects
	protected HashMap<String, HashSet<AssignmentRule>>		variableToAffectedAssignmentRuleSetMap		= null;

	// allows to access to whether or not a variable is in an assignment or rate
	// rule rule (RHS)
	protected HashMap<String, Boolean>						variableToIsInAssignmentRuleMap				= null;
	protected HashMap<String, Boolean>						variableToIsInRateRuleMap					= null;

	// allows for access to the set of constraints that a variable affects
	protected HashMap<String, HashSet<ASTNode>>				variableToAffectedConstraintSetMap			= null;

	protected HashMap<String, Boolean>						variableToIsInConstraintMap					= null;
	protected HashMap<String, Boolean>						variableToIsConstantMap						= null;

	// compares two events based on fire time and priority
	protected EventComparator								eventComparator								= new EventComparator();

	// allows access to a component's location on the grid from its ID
	protected LinkedHashMap<String, Point>					componentToLocationMap						= null;

	// allows access to a component's set of reactions from its ID
	protected HashMap<String, HashSet<String>>				componentToReactionSetMap					= null;

	// allows access to a component's set of variables (species and parameters)
	// from its ID
	protected HashMap<String, HashSet<String>>				componentToVariableSetMap					= null;

	protected HashMap<String, HashSet<String>>				componentToEventSetMap						= null;

	protected HashSet<String>								componentIDSet								= new HashSet<String>();

	protected LinkedHashSet<String>							compartmentIDSet							= new LinkedHashSet<String>();
	protected LinkedHashSet<String>							nonconstantParameterIDSet					= new LinkedHashSet<String>();

	// initial model -- used for resetting when doing multiple dynamic runs
	protected ListOf<Species>								initialSpecies								= new ListOf<Species>();
	protected ListOf<Reaction>								initialReactions							= new ListOf<Reaction>();
	protected ListOf<Event>									initialEvents								= new ListOf<Event>();
	protected ListOf<Parameter>								initialParameters							= new ListOf<Parameter>();
	protected ListOf<Compartment>							initialCompartments							= new ListOf<Compartment>();

	// locations parameter annotations for placing compartments
	protected HashMap<String, String>						submodelIDToLocationsMap					= new HashMap<String, String>();

	protected ArrayList<String>								interestingSpecies							= new ArrayList<String>();

	protected HashSet<String>								ibiosimFunctionDefinitions					= new HashSet<String>();

	// propensity variables
	protected double										totalPropensity								= 0.0;
	protected double										minPropensity								= Double.MAX_VALUE / 10.0;
	protected double										maxPropensity								= Double.MIN_VALUE / 10.0;

	// file writing variables
	protected FileWriter									TSDWriter									= null;
	protected BufferedWriter								bufferedTSDWriter							= null;

	// boolean flags
	protected boolean										cancelFlag									= false;
	protected boolean										constraintFailureFlag						= false;
	protected boolean										sbmlHasErrorsFlag							= false;

	protected double										currentTime;
	protected String										SBMLFileName;
	protected double										timeLimit;
	protected double										maxTimeStep;
	protected double										minTimeStep;
	protected JProgressBar									progress;
	protected double										printInterval;
	protected int											currentRun;
	protected String										outputDirectory;

	protected long											numSpecies;
	protected long											numParameters;
	protected long											numReactions;
	protected long											numEvents;
	protected long											numRules;
	protected int											numAssignmentRules;
	protected long											numConstraints;
	protected int											numInitialAssignments;
	protected int											numRateRules;

	protected int											minRow										= Integer.MAX_VALUE;
	protected int											minCol										= Integer.MAX_VALUE;
	protected int											maxRow										= Integer.MIN_VALUE;
	protected int											maxCol										= Integer.MIN_VALUE;

	protected boolean										stoichAmpBoolean							= false;
	protected double										stoichAmpGridValue							= 1.0;

	// true means the model is dynamic
	protected boolean										dynamicBoolean								= false;

	protected boolean										printConcentrations							= false;

	protected int											diffCount									= 0;
	protected int											totalCount									= 0;
	protected int											memCount									= 0;

	protected JFrame										running										= null;

	PsRandom												prng										= new PsRandom();

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
	 */
	public Simulator(String SBMLFileName, String outputDirectory, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, Long initializationTime, double stoichAmpValue, JFrame running, String[] interestingSpecies, String quantityType)
	{

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
		{
			this.interestingSpecies.add(interestingSpecies[i]);
		}

		if (quantityType != null && quantityType.equals("concentration"))
		{
			this.printConcentrations = true;
		}

		if (stoichAmpValue <= 1.0)
		{
			stoichAmpBoolean = false;
		}
		else
		{
			stoichAmpBoolean = true;
			stoichAmpGridValue = stoichAmpValue;
		}

		SBMLDocument document = null;

		try
		{
			document = SBMLReader.read(new File(SBMLFileName));
		}
		catch (XMLStreamException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		SBMLErrorLog errors = document.getListOfErrors();

		// if the sbml document has errors, tell the user and don't simulate
		if (document.getErrorCount() > 0)
		{

			String errorString = "";

			for (int i = 0; i < errors.getErrorCount(); i++)
			{
				errorString += errors.getError(i);
			}

			JOptionPane.showMessageDialog(Gui.frame, "The SBML file contains " + document.getErrorCount() + " error(s):\n" + errorString, "SBML Error", JOptionPane.ERROR_MESSAGE);

			sbmlHasErrorsFlag = true;
		}

		model = document.getModel();

		numSpecies = model.getSpeciesCount();
		numParameters = model.getParameterCount();
		numReactions = model.getReactionCount();
		numEvents = model.getEventCount();
		numRules = model.getRuleCount();
		numConstraints = model.getConstraintCount();
		numInitialAssignments = model.getInitialAssignmentCount();

		// set initial capacities for collections (1.5 is used to multiply
		// numReactions due to reversible reactions)
		speciesToAffectedReactionSetMap = new HashMap<String, HashSet<String>>((int) numSpecies);
		speciesToIsBoundaryConditionMap = new HashMap<String, Boolean>((int) numSpecies);
		variableToIsConstantMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
		speciesToHasOnlySubstanceUnitsMap = new HashMap<String, Boolean>((int) numSpecies);
		speciesToSubstanceUnitsMap = new HashMap<String, String>((int) numSpecies);
		speciesToConversionFactorMap = new HashMap<String, String>((int) numSpecies);
		speciesToCompartmentNameMap = new HashMap<String, String>((int) numSpecies);
		speciesToCompartmentSizeMap = new TObjectDoubleHashMap<String>((int) numSpecies);
		speciesIDSet = new LinkedHashSet<String>((int) numSpecies);
		reactionToNonconstantStoichiometriesSetMap = new HashMap<String, HashSet<StringStringPair>>();
		variableToValueMap = new TObjectDoubleHashMap<String>((int) numSpecies + (int) numParameters);

		reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (numReactions * 1.5));
		reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair>>((int) (numReactions * 1.5));
		reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair>>((int) (numReactions * 1.5));
		reactionToFormulaMap = new HashMap<String, ASTNode>((int) (numReactions * 1.5));

		componentToLocationMap = new LinkedHashMap<String, Point>();
		componentToReactionSetMap = new HashMap<String, HashSet<String>>();
		componentToVariableSetMap = new HashMap<String, HashSet<String>>();
		componentToEventSetMap = new HashMap<String, HashSet<String>>();

		listOfRateRules = new HashSet<RateRule>();
		listOfAssignmentRules = new HashSet<AssignmentRule>();
		if (numEvents > 0)
		{

			triggeredEventQueue = new PriorityQueue<EventToFire>((int) numEvents, eventComparator);
			untriggeredEventSet = new HashSet<String>((int) numEvents);
			eventToPriorityMap = new HashMap<String, ASTNode>((int) numEvents);
			eventToDelayMap = new HashMap<String, ASTNode>((int) numEvents);
			eventToHasDelayMap = new HashMap<String, Boolean>((int) numEvents);
			eventToTriggerMap = new HashMap<String, ASTNode>((int) numEvents);
			eventToTriggerInitiallyTrueMap = new HashMap<String, Boolean>((int) numEvents);
			eventToTriggerPersistenceMap = new HashMap<String, Boolean>((int) numEvents);
			eventToUseValuesFromTriggerTimeMap = new HashMap<String, Boolean>((int) numEvents);
			eventToAssignmentSetMap = new HashMap<String, HashSet<Object>>((int) numEvents);
			eventToAffectedReactionSetMap = new HashMap<String, HashSet<String>>((int) numEvents);
			eventToPreviousTriggerValueMap = new HashMap<String, Boolean>((int) numEvents);
			variableToEventSetMap = new HashMap<String, HashSet<String>>((int) numEvents);
		}

		if (numRules > 0)
		{

			variableToAffectedAssignmentRuleSetMap = new HashMap<String, HashSet<AssignmentRule>>((int) numRules);
			variableToIsInAssignmentRuleMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
			variableToIsInRateRuleMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
		}

		if (numConstraints > 0)
		{

			variableToAffectedConstraintSetMap = new HashMap<String, HashSet<ASTNode>>((int) numConstraints);
			variableToIsInConstraintMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
		}

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
	 * replaces ROWX_COLY with ROWA_COLB in a kinetic law this has to be done
	 * without re-parsing the formula string because the row/col values can be
	 * negative, which gets parsed incorrectly
	 * 
	 * @param node
	 * @param oldString
	 * @param newString
	 */
	private void alterNode(ASTNode node, String oldString, String newString)
	{

		if (node.isName() && node.getName().contains(oldString))
		{
			node.setVariable(model.getSpecies(newString + "__" + node.getName().split("__")[1]));
		}
		else
		{
			for (ASTNode childNode : node.getChildren())
			{
				alterNode(childNode, oldString, newString);
			}
		}
	}

	/**
	 * replaceArgument() doesn't work when you're replacing a localParameter, so
	 * this does that -- finds the oldString within node and replaces it with
	 * the local parameter specified by newString
	 * 
	 * @param node
	 * @param reactionID
	 * @param oldString
	 * @param newString
	 */
	private void alterLocalParameter(ASTNode node, Reaction reaction, String oldString, String newString)
	{

		// String reactionID = reaction.getId();
		if (node.isName() && node.getName().equals(oldString))
		{
			node.setVariable(reaction.getKineticLaw().getLocalParameter(newString));
		}
		else
		{
			for (ASTNode childNode : node.getChildren())
			{
				alterLocalParameter(childNode, reaction, oldString, newString);
			}
		}
	}

	/**
	 * alters the kinetic laws and stoichiometries of grid diffusion reactions
	 * in accordance with the stoichiometry amplification parameters specified
	 * by the user
	 * 
	 * @param model
	 */
	private static void applyStoichiometryAmplification(Model model, double stoichAmpValue)
	{

		if (stoichAmpValue <= 1.0)
		{
			return;
		}

		for (Reaction reaction : model.getListOfReactions())
		{

			// stoichiometry amplification -- alter the stoichiometry
			if (reaction.getId().contains("_Diffusion_"))
			{

				// these are all forward reactions, so adjust the
				// stoichiometries of the reactant
				for (SpeciesReference reactant : reaction.getListOfReactants())
				{
					reactant.setStoichiometry(stoichAmpValue);
				}

				// alter the kinetic laws by dividing by the stoichiometry
				// amplification value
				// this will alter the propensities accordingly
				ASTNode divisionNode = new ASTNode();
				divisionNode.setValue(1.0 / stoichAmpValue);
				reaction.getKineticLaw().getMath().multiplyWith(divisionNode);
			}
		}
	}

	/**
	 * cancels the current run
	 */
	@Override
	public abstract void cancel();

	/**
	 * clears data structures for new run
	 */
	@Override
	public abstract void clear();

	/**
	 * copies parts of the model that may change during a dynamic run
	 */
	protected void createModelCopy()
	{

		initialSpecies = model.getListOfSpecies().clone();
		initialReactions = model.getListOfReactions().clone();
		initialEvents = model.getListOfEvents().clone();
		initialParameters = model.getListOfParameters().clone();
		initialCompartments = model.getListOfCompartments().clone();
	}

	/**
	 * uses an existing component to create a new component and update data
	 * structures and the simulation state this is used for "birth" events in
	 * dynamic models
	 * 
	 * @param parentComponentID
	 * @param eventID
	 *            the ID of the division event that just fired
	 */
	protected void duplicateComponent(String parentComponentID, String eventID, String symmetry)
	{

		// determine new component ID
		int componentNumber = componentToReactionSetMap.size() + 1;
		String childComponentID = "C" + String.valueOf(componentNumber);

		while (componentToReactionSetMap.keySet().contains(childComponentID) == true || componentIDSet.contains(childComponentID) == true)
		{

			++componentNumber;
			childComponentID = "C" + String.valueOf(componentNumber);
		}

		// the component ID is just the child name with no ancestry information
		// the compartment ID contains the ancestry information
		componentIDSet.add(childComponentID);

		// only use the immediate parent for the new ID
		childComponentID = childComponentID + "_of_" + parentComponentID.split("_of_")[0];

		Compartment childCompartment = model.createCompartment();
		String compartID = "";

		// duplicate compartment
		for (Compartment comp : model.getListOfCompartments())
		{

			if (comp.getId().contains(parentComponentID + "__"))
			{

				compartID = comp.getId();
				childCompartment.setSize(comp.getSize());
				childCompartment.setConstant(comp.getConstant());
			}
		}

		childCompartment.setId(childComponentID + "__" + compartID.split("__")[1]);
		compartmentIDSet.add(childCompartment.getId());
		variableToValueMap.put(childCompartment.getId(), childCompartment.getSize());
		variableToIsConstantMap.put(childCompartment.getId(), childCompartment.getConstant());

		// reactions that change and thus need their propensities re-evaluated
		HashSet<String> reactionsToAdjust = new HashSet<String>();

		// determine new component location
		// choose a random direction and place the component adjacent to the
		// parent
		// 0 = left, 1 = right, 2 = above, 3 = below
		int randomDirection = (int) (randomNumberGenerator.nextDouble() * 8.0);

		Point parentLocation = componentToLocationMap.get(parentComponentID);
		Point childLocation = (Point) parentLocation.clone();

		// MOVE COMPONENTS
		// this places the child and shuffles others around as needed
		moveComponent(parentComponentID, childComponentID, childLocation, randomDirection, reactionsToAdjust);

		// DUPLICATE VARIABLES and alter them to coincide with the new ID

		HashSet<String> childComponentVariableSet = new HashSet<String>();
		if (componentToVariableSetMap.containsKey(parentComponentID) == false)
		{
			componentToVariableSetMap.put(parentComponentID, new HashSet<String>());
		}

		for (String parentVariableID : componentToVariableSetMap.get(parentComponentID))
		{

			String childVariableID = parentVariableID.replace(parentComponentID, childComponentID);
			childComponentVariableSet.add(childVariableID);

			// this means it's a species
			if (speciesIDSet.contains(parentVariableID))
			{

				// if it's a promoter, double it first, as it's a dna-based
				// species
				if (model.getSpecies(parentVariableID).isSetSBOTerm() && model.getSpecies(parentVariableID).getSBOTerm() == GlobalConstants.SBO_PROMOTER_SPECIES)
				{
					variableToValueMap.put(parentVariableID, variableToValueMap.get(parentVariableID) * 2);
				}

				// duplicate species into the model
				Species newSpecies = model.getSpecies(parentVariableID).clone();
				newSpecies.setId(childVariableID);
				SBMLutilities.setMetaId(newSpecies, childVariableID);
				model.addSpecies(newSpecies);

				if (speciesToHasOnlySubstanceUnitsMap.get(parentVariableID) == false)
				{
					speciesToCompartmentSizeMap.put(childVariableID, speciesToCompartmentSizeMap.get(parentVariableID));
				}

				if (speciesToHasOnlySubstanceUnitsMap.get(parentVariableID) == false)
				{
					speciesToCompartmentNameMap.put(childVariableID, speciesToCompartmentNameMap.get(parentVariableID));
				}

				// the speciesToAffectedReactionSetMap gets filled-in later
				speciesToAffectedReactionSetMap.put(childVariableID, new HashSet<String>(20));
				speciesToIsBoundaryConditionMap.put(childVariableID, speciesToIsBoundaryConditionMap.get(parentVariableID));
				variableToIsConstantMap.put(childVariableID, variableToIsConstantMap.get(parentVariableID));
				speciesToHasOnlySubstanceUnitsMap.put(childVariableID, speciesToHasOnlySubstanceUnitsMap.get(parentVariableID));
				speciesIDSet.add(childVariableID);

				// divide the parent species amount by two by default
				// unless there is an event assignment or if the species is
				// constant/bc
				if (variableToIsConstantMap.get(parentVariableID) == true || speciesToIsBoundaryConditionMap.get(parentVariableID))
				{

					variableToValueMap.put(childVariableID, variableToValueMap.get(parentVariableID));
				}
				else
				{

					// go through the event assignments to find ones that affect
					// this particular species
					// this is used to determine, via species quantity
					// conservation, how much the daughter cell gets
					HashSet<Object> assignmentSetMap = this.eventToAssignmentSetMap.get(eventID);
					boolean setInAssignment = false;

					for (Object assignment : assignmentSetMap)
					{

						if (((EventAssignment) assignment).getVariable().equals(parentVariableID))
						{

							double totalAmount = variableToValueMap.get(parentVariableID);
							double afterEventAmount = evaluateExpressionRecursive(((EventAssignment) assignment).getMath());
							double childAmount = totalAmount - afterEventAmount;

							variableToValueMap.put(childVariableID, childAmount);
							variableToValueMap.put(parentVariableID, afterEventAmount);

							setInAssignment = true;
						}
					}

					if (setInAssignment == false)
					{

						variableToValueMap.put(childVariableID, (int) ((variableToValueMap.get(parentVariableID) / 2) + 1));
						variableToValueMap.put(parentVariableID, (int) ((variableToValueMap.get(parentVariableID) / 2) + 1));
					}
				}
			}
			// this means it's a parameter
			else
			{

				variableToValueMap.put(childVariableID, variableToValueMap.get(parentVariableID));
			}
		}

		componentToVariableSetMap.put(childComponentID, childComponentVariableSet);

		// DUPLICATE REACTIONS and alter them to coincide with the new ID

		HashSet<String> childReactionSet = new HashSet<String>();
		if (componentToReactionSetMap.containsKey(parentComponentID) == false)
		{
			componentToReactionSetMap.put(parentComponentID, new HashSet<String>());
		}

		for (String parentReactionID : componentToReactionSetMap.get(parentComponentID))
		{

			String parentReactionFormula = "";
			String childReactionID = parentReactionID.replace(parentComponentID, childComponentID);

			childReactionSet.add(childReactionID);

			parentReactionFormula = reactionToFormulaMap.get(parentReactionID).toFormula();

			ASTNode childFormulaNode = reactionToFormulaMap.get(parentReactionID).clone();
			String thing = "";

			try
			{

				String childReactionFormula = parentReactionFormula.replace(parentComponentID, childComponentID);

				if (parentReactionID.contains("MembraneDiffusion"))
				{

					String parentRowCol = "ROW" + (int) parentLocation.getX() + "_" + "COL" + (int) parentLocation.getY();
					String childRowCol = "ROW" + (int) childLocation.getX() + "_" + "COL" + (int) childLocation.getY();

					// formulas as ASTNodes can't have negative numbers in
					// species IDs
					parentRowCol = parentRowCol.replace("ROW-", "ROW_negative_");
					parentRowCol = parentRowCol.replace("COL-", "COL_negative_");
					childRowCol = childRowCol.replace("ROW-", "ROW_negative_");
					childRowCol = childRowCol.replace("COL-", "COL_negative_");

					if (childReactionFormula.contains("ROW") && childReactionFormula.contains("COL"))
					{
						alterNode(childFormulaNode, parentRowCol, childRowCol);
					}
					else
					{

						String childSpeciesID = childReactionFormula.split("\\*")[1];
						String underlyingSpeciesID = childSpeciesID.split("__")[childSpeciesID.split("__").length - 1];

						// we want the parent's kmdiff parameter, as the child's
						// doesn't exist
						childReactionFormula = childReactionFormula.replace(childSpeciesID + "__kmdiff", parentComponentID + "__" + underlyingSpeciesID + "__kmdiff");

						thing = childReactionFormula;

						childFormulaNode = ASTNode.parseFormula(childReactionFormula);
					}
				}
				else
				{
					childFormulaNode = ASTNode.parseFormula(childReactionFormula);
				}
			}
			catch (ParseException e)
			{

				System.err.println(thing);
				e.printStackTrace();
			}

			reactionToSpeciesAndStoichiometrySetMap.put(childReactionID, new HashSet<StringDoublePair>());
			reactionToReactantStoichiometrySetMap.put(childReactionID, new HashSet<StringDoublePair>());

			boolean notEnoughMoleculesFlag = false;
			if (reactionToSpeciesAndStoichiometrySetMap.containsKey(parentReactionID) == false)
			{
				reactionToSpeciesAndStoichiometrySetMap.put(parentReactionID, new HashSet<StringDoublePair>());
			}

			// add species/stoichiometry pairs for this new reaction
			for (StringDoublePair parentSpeciesAndStoichiometry : reactionToSpeciesAndStoichiometrySetMap.get(parentReactionID))
			{

				double childStoichiometry = parentSpeciesAndStoichiometry.doub;

				String childSpeciesID = "";

				if (parentSpeciesAndStoichiometry.string.contains("ROW") && parentSpeciesAndStoichiometry.string.contains("COL"))
				{

					String parentRowCol = "ROW" + (int) parentLocation.getX() + "_" + "COL" + (int) parentLocation.getY();
					String childRowCol = "ROW" + (int) childLocation.getX() + "_" + "COL" + (int) childLocation.getY();

					childSpeciesID = parentSpeciesAndStoichiometry.string.replace(parentRowCol, childRowCol);
				}
				else
				{
					childSpeciesID = parentSpeciesAndStoichiometry.string.replace(parentComponentID, childComponentID);
				}

				reactionToSpeciesAndStoichiometrySetMap.get(childReactionID).add(new StringDoublePair(childSpeciesID, childStoichiometry));

				if (speciesToAffectedReactionSetMap.containsKey(parentSpeciesAndStoichiometry.string))
				{
					speciesToAffectedReactionSetMap.get(childSpeciesID).add(childReactionID);
				}

				// make sure there are enough molecules for this species
				// (if the molecule is a reactant -- if it's a product then it's
				// being added)
				if (variableToValueMap.get(childSpeciesID) < childStoichiometry && reactionToReactantStoichiometrySetMap.get(parentReactionID).contains(childSpeciesID))
				{
					notEnoughMoleculesFlag = true;
				}
			}

			if (reactionToReactantStoichiometrySetMap.containsKey(parentReactionID) == false)
			{
				reactionToReactantStoichiometrySetMap.put(parentReactionID, new HashSet<StringDoublePair>());
			}

			// add reactant/stoichiometry pairs for this new reactions
			for (StringDoublePair parentReactantAndStoichiometry : reactionToReactantStoichiometrySetMap.get(parentReactionID))
			{

				double childStoichiometry = parentReactantAndStoichiometry.doub;
				String childSpeciesID = "";

				if (parentReactantAndStoichiometry.string.contains("ROW") && parentReactantAndStoichiometry.string.contains("COL"))
				{

					String parentRowCol = "ROW" + (int) parentLocation.getX() + "_" + "COL" + (int) parentLocation.getY();
					String childRowCol = "ROW" + (int) childLocation.getX() + "_" + "COL" + (int) childLocation.getY();

					childSpeciesID = parentReactantAndStoichiometry.string.replace(parentRowCol, childRowCol);
				}
				else
				{
					childSpeciesID = parentReactantAndStoichiometry.string.replace(parentComponentID, childComponentID);
				}

				reactionToReactantStoichiometrySetMap.get(childReactionID).add(new StringDoublePair(childSpeciesID, childStoichiometry));
			}

			reactionToFormulaMap.put(childReactionID, childFormulaNode);

			double propensity;

			if (notEnoughMoleculesFlag == true)
			{
				propensity = 0.0;
			}
			else
			{

				// calculate propensity
				propensity = evaluateExpressionRecursive(childFormulaNode);

				if (propensity < minPropensity && propensity > 0)
				{
					minPropensity = propensity;
				}
				if (propensity > maxPropensity)
				{
					maxPropensity = propensity;
				}

				totalPropensity += propensity;
			}

			reactionToPropensityMap.put(childReactionID, propensity);
		}

		componentToReactionSetMap.put(childComponentID, childReactionSet);

		reactionsToAdjust.addAll(componentToReactionSetMap.get(parentComponentID));

		// update propensities for the parent reactions, as their species values
		// may have changed
		updatePropensities(reactionsToAdjust);

		updateAfterDynamicChanges();

		// DUPLICATE EVENTS

		HashSet<String> childEventSet = new HashSet<String>();
		if (componentToEventSetMap.containsKey(parentComponentID) == false)
		{
			componentToEventSetMap.put(parentComponentID, new HashSet<String>());
		}

		for (String parentEventID : componentToEventSetMap.get(parentComponentID))
		{

			String childEventID = parentEventID.replace(parentComponentID, childComponentID);

			untriggeredEventSet.add(childEventID);
			childEventSet.add(childEventID);

			// hashmaps that allow for access to event information from the
			// event's id
			eventToPriorityMap.put(childEventID, eventToPriorityMap.get(parentEventID));
			eventToDelayMap.put(childEventID, eventToDelayMap.get(parentEventID).clone());
			eventToHasDelayMap.put(childEventID, eventToHasDelayMap.get(parentEventID));
			eventToTriggerPersistenceMap.put(childEventID, eventToTriggerPersistenceMap.get(parentEventID));
			eventToUseValuesFromTriggerTimeMap.put(childEventID, eventToUseValuesFromTriggerTimeMap.get(parentEventID));
			eventToTriggerMap.put(childEventID, eventToTriggerMap.get(parentEventID).clone());
			eventToTriggerInitiallyTrueMap.put(childEventID, eventToTriggerInitiallyTrueMap.get(parentEventID));
			eventToPreviousTriggerValueMap.put(childEventID, eventToTriggerInitiallyTrueMap.get(childEventID));

			// symmetric division creates two new things essentially: there are
			// two children, not a parent and child
			// untrigger the parent division event
			if (symmetry.equals("symmetric"))
			{

				// untrigger the event
				untriggeredEventSet.add(parentEventID);
				eventToPreviousTriggerValueMap.put(parentEventID, eventToTriggerInitiallyTrueMap.get(parentEventID));

				// remove the event from the queue
				// copy the triggered event queue -- except the parent event
				PriorityQueue<EventToFire> newTriggeredEventQueue = new PriorityQueue<EventToFire>(5, eventComparator);

				while (triggeredEventQueue.size() > 0)
				{

					EventToFire event = triggeredEventQueue.poll();
					EventToFire eventToAdd = new EventToFire(event.eventID, (HashSet<Object>) event.eventAssignmentSet.clone(), event.fireTime);

					if (parentEventID.equals(event.eventID) == false)
					{
						newTriggeredEventQueue.add(eventToAdd);
					}
				}

				triggeredEventQueue = newTriggeredEventQueue;
			}

			HashSet<Object> assignmentSetMap = new HashSet<Object>();

			// copy event assignments for the new child event
			for (Object assignment : eventToAssignmentSetMap.get(parentEventID))
			{

				String variableID = ((EventAssignment) assignment).getVariable();

				if (variableToEventSetMap.containsKey(variableID) == false)
				{
					variableToEventSetMap.put(variableID, new HashSet<String>());
				}

				variableToEventSetMap.get(variableID).add(childEventID);

				EventAssignment ea = ((EventAssignment) assignment).clone();
				ea.setVariable(((EventAssignment) assignment).getVariable().replace(parentComponentID, childComponentID));

				// ea.setMath(ASTNode.parseFormula(((EventAssignment)assignment).getMath().toFormula()
				// .replace(parentComponentID, childComponentID)));
				ea.setMath(SBMLutilities.myParseFormula(((EventAssignment) assignment).getMath().toFormula().replace(parentComponentID, childComponentID)));

				assignmentSetMap.add(ea);

				if (speciesToAffectedReactionSetMap.containsKey(variableID))
				{

					eventToAffectedReactionSetMap.put(childEventID, new HashSet<String>());
					eventToAffectedReactionSetMap.get(childEventID).addAll(speciesToAffectedReactionSetMap.get(variableID));
				}
			}

			eventToAssignmentSetMap.put(childEventID, assignmentSetMap);
		}

		componentToEventSetMap.put(childComponentID, childEventSet);

		// System.err.println("REACTIONS");
		//
		// for (String reactionID : reactionToPropensityMap.keySet()) {
		//
		// if (//reactionID.contains("_of_") ||
		// reactionID.contains("Diffusion")
		// ) {
		//
		// System.err.println(reactionID);
		// }
		// }
		//
		// System.err.println();
		// System.err.println("VARIABLES");
		//
		// for (String variableID : this.speciesIDSet) {
		// // if (//variableID.contains("_of_") ||
		// // variableID.contains("ROW")
		// // )
		// System.err.println(variableID + "          " +
		// variableToValueMap.get(variableID));
		// }
		//
		// for (String event : this.eventToAssignmentSetMap.keySet()) {
		//
		// System.err.println(event);
		// System.err.println(this.eventToAssignmentSetMap.get(event));
		// }
		//
		// System.err.println();
		// System.err.println("duplicate finished");
	}

	/**
	 * simulator-specific component erasure stuff
	 * 
	 * @param reactionIDs
	 */
	protected abstract void eraseComponentFurther(HashSet<String> reactionIDs);

	/**
	 * erases all traces of a component (ie, its reactions and species and
	 * parameters, etc) and updates data structures and the simulation state
	 * used for "death" events in dynamic models
	 * 
	 * @param componentID
	 */
	protected HashSet<String> eraseComponent(String componentID)
	{

		if (componentToReactionSetMap.get(componentID) != null)
		{
			for (String reactionID : componentToReactionSetMap.get(componentID))
			{

				reactionToPropensityMap.remove(reactionID);
				reactionToSpeciesAndStoichiometrySetMap.remove(reactionID);
				reactionToReactantStoichiometrySetMap.remove(reactionID);
				reactionToFormulaMap.remove(reactionID);
			}

			// simulator-specific data structure erasal
			eraseComponentFurther(componentToReactionSetMap.get(componentID));
		}

		for (String variableID : componentToVariableSetMap.get(componentID))
		{

			variableToEventSetMap.remove(variableID);

			if (variableToAffectedAssignmentRuleSetMap != null)
			{
				variableToAffectedAssignmentRuleSetMap.remove(variableID);
			}
			if (variableToIsInAssignmentRuleMap != null)
			{
				variableToIsInAssignmentRuleMap.remove(variableID);
			}
			if (variableToAffectedConstraintSetMap != null)
			{
				variableToAffectedConstraintSetMap.remove(variableID);
			}
			if (variableToIsInConstraintMap != null)
			{
				variableToIsInConstraintMap.remove(variableID);
			}

			variableToIsConstantMap.remove(variableID);
			variableToValueMap.remove(variableID);

			speciesToIsBoundaryConditionMap.remove(variableID);
			speciesToHasOnlySubstanceUnitsMap.remove(variableID);
			speciesToCompartmentNameMap.remove(variableID);
			speciesToCompartmentSizeMap.remove(variableID);
			speciesIDSet.remove(variableID);
		}

		String compartmentIDToRemove = "";

		for (String compartmentID : compartmentIDSet)
		{
			if (compartmentID.contains(componentID + "__"))
			{
				compartmentIDToRemove = compartmentID;
			}
		}

		compartmentIDSet.remove(compartmentIDToRemove);

		// for (String eventID : componentToEventSetMap.get(componentID)) {
		//
		// triggeredEventQueue.remove(eventID);
		// untriggeredEventSet.remove(eventID);
		// eventToPriorityMap.remove(eventID);
		// eventToDelayMap.remove(eventID);
		// eventToHasDelayMap.remove(eventID);
		// eventToTriggerPersistenceMap.remove(eventID);
		// eventToUseValuesFromTriggerTimeMap.remove(eventID);
		// eventToTriggerMap.remove(eventID);
		// eventToTriggerInitiallyTrueMap.remove(eventID);
		// eventToPreviousTriggerValueMap.remove(eventID);
		// eventToAssignmentSetMap.remove(eventID);
		// eventToAffectedReactionSetMap.remove(eventID);
		// }

		HashSet<String> deadEvents = new HashSet<String>();
		deadEvents.addAll(componentToEventSetMap.get(componentID));

		componentToLocationMap.remove(componentID);
		componentToReactionSetMap.remove(componentID);
		componentToVariableSetMap.remove(componentID);
		componentToEventSetMap.remove(componentID);

		updateAfterDynamicChanges();

		return deadEvents;
	}

	/**
	 * calculates an expression using a recursive algorithm
	 * 
	 * @param node
	 *            the AST with the formula
	 * @return the evaluated expression
	 */
	protected double evaluateExpressionRecursive(ASTNode node)
	{

		// these if/else-ifs before the else are leaf conditions

		// logical constant, logical operator, or relational operator
		if (node.isBoolean())
		{

			switch (node.getType())
			{

			case CONSTANT_TRUE:
				return 1.0;

			case CONSTANT_FALSE:
				return 0.0;

			case LOGICAL_NOT:
				return getDoubleFromBoolean(!(getBooleanFromDouble(evaluateExpressionRecursive(node.getLeftChild()))));

			case LOGICAL_AND:
			{

				boolean andResult = true;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
				{
					andResult = andResult && getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter)));
				}

				return getDoubleFromBoolean(andResult);
			}

			case LOGICAL_OR:
			{

				boolean orResult = false;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
				{
					orResult = orResult || getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter)));
				}

				return getDoubleFromBoolean(orResult);
			}

			case LOGICAL_XOR:
			{

				boolean xorResult = getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(0)));

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
				{
					xorResult = xorResult ^ getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter)));
				}

				return getDoubleFromBoolean(xorResult);
			}

			case RELATIONAL_EQ:
				return getDoubleFromBoolean(evaluateExpressionRecursive(node.getLeftChild()) == evaluateExpressionRecursive(node.getRightChild()));

			case RELATIONAL_NEQ:
				return getDoubleFromBoolean(evaluateExpressionRecursive(node.getLeftChild()) != evaluateExpressionRecursive(node.getRightChild()));

			case RELATIONAL_GEQ:
				return getDoubleFromBoolean(evaluateExpressionRecursive(node.getLeftChild()) >= evaluateExpressionRecursive(node.getRightChild()));

			case RELATIONAL_LEQ:
				return getDoubleFromBoolean(evaluateExpressionRecursive(node.getLeftChild()) <= evaluateExpressionRecursive(node.getRightChild()));

			case RELATIONAL_GT:
				return getDoubleFromBoolean(evaluateExpressionRecursive(node.getLeftChild()) > evaluateExpressionRecursive(node.getRightChild()));

			case RELATIONAL_LT:
				return getDoubleFromBoolean(evaluateExpressionRecursive(node.getLeftChild()) < evaluateExpressionRecursive(node.getRightChild()));
			default:
			}
		}

		// if it's a mathematical constant
		else if (node.isConstant())
		{

			switch (node.getType())
			{

			case CONSTANT_E:
				return Math.E;

			case CONSTANT_PI:
				return Math.PI;

			default:
			}
		}

		// if it's a number
		else if (node.isNumber())
		{
			return node.getReal();
		}
		else if (node.isName())
		{

			String name = node.getName().replace("_negative_", "-");

			if (node.getType().equals(org.sbml.jsbml.ASTNode.Type.NAME_TIME))
			{

				return currentTime;
			}
			// if it's a reaction id return the propensity
			else if (reactionToPropensityMap.keySet().contains(node.getName()))
			{
				return reactionToPropensityMap.get(node.getName());
			}
			else
			{

				double value;

				if (this.speciesToHasOnlySubstanceUnitsMap.containsKey(name) && this.speciesToHasOnlySubstanceUnitsMap.get(name) == false)
				{

					value = (variableToValueMap.get(name) / variableToValueMap.get(speciesToCompartmentNameMap.get(name)));
				}
				else
				{
					value = variableToValueMap.get(name);
				}
				return value;
			}
		}

		// operators/functions with two children
		else
		{

			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			switch (node.getType())
			{

			case PLUS:
			{

				double sum = 0.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
				{
					sum += evaluateExpressionRecursive(node.getChild(childIter));
				}

				return sum;
			}

			case MINUS:
			{

				double sum = evaluateExpressionRecursive(leftChild);

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
				{
					sum -= evaluateExpressionRecursive(node.getChild(childIter));
				}

				return sum;
			}

			case TIMES:
			{

				double product = 1.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
				{
					product *= evaluateExpressionRecursive(node.getChild(childIter));
				}

				return product;
			}

			case DIVIDE:
				return (evaluateExpressionRecursive(leftChild) / evaluateExpressionRecursive(rightChild));

			case FUNCTION_POWER:
				return (FastMath.pow(evaluateExpressionRecursive(leftChild), evaluateExpressionRecursive(rightChild)));

			case FUNCTION:
			{
				// use node name to determine function
				// i'm not sure what to do with completely user-defined
				// functions, though
				String nodeName = node.getName();

				// generates a uniform random number between the upper and lower
				// bound
				if (nodeName.equals("uniform"))
				{

					double leftChildValue = evaluateExpressionRecursive(node.getLeftChild());
					double rightChildValue = evaluateExpressionRecursive(node.getRightChild());
					double lowerBound = FastMath.min(leftChildValue, rightChildValue);
					double upperBound = FastMath.max(leftChildValue, rightChildValue);

					return prng.nextDouble(lowerBound, upperBound);
				}
				else if (nodeName.equals("exponential"))
				{

					return prng.nextExponential(evaluateExpressionRecursive(node.getLeftChild()), 1);
				}
				else if (nodeName.equals("gamma"))
				{

					return prng.nextGamma(1, evaluateExpressionRecursive(node.getLeftChild()), evaluateExpressionRecursive(node.getRightChild()));
				}
				else if (nodeName.equals("chisq"))
				{

					return prng.nextChiSquare((int) evaluateExpressionRecursive(node.getLeftChild()));
				}
				else if (nodeName.equals("lognormal"))
				{

					return prng.nextLogNormal(evaluateExpressionRecursive(node.getLeftChild()), evaluateExpressionRecursive(node.getRightChild()));
				}
				else if (nodeName.equals("laplace"))
				{

					// function doesn't exist in current libraries
					return 0;
				}
				else if (nodeName.equals("cauchy"))
				{

					return prng.nextLorentzian(0, evaluateExpressionRecursive(node.getLeftChild()));
				}
				else if (nodeName.equals("poisson"))
				{

					return prng.nextPoissonian(evaluateExpressionRecursive(node.getLeftChild()));
				}
				else if (nodeName.equals("binomial"))
				{

					return prng.nextBinomial(evaluateExpressionRecursive(node.getLeftChild()), (int) evaluateExpressionRecursive(node.getRightChild()));
				}
				else if (nodeName.equals("bernoulli"))
				{

					return prng.nextBinomial(evaluateExpressionRecursive(node.getLeftChild()), 1);
				}
				else if (nodeName.equals("normal"))
				{

					return prng.nextGaussian(evaluateExpressionRecursive(node.getLeftChild()), evaluateExpressionRecursive(node.getRightChild()));
				}
				else if (nodeName.equals("get2DArrayElement"))
				{

					// int leftIndex = node.getChild(1).getInteger();
					// int rightIndex = node.getChild(2).getInteger();
					// String speciesName = "ROW" + leftIndex + "_COL" +
					// rightIndex + "__" + node.getChild(0).getName();
					//
					// //check bounds
					// //if species exists, return its value/amount
					// if (variableToValueMap.containsKey(speciesName))
					// return variableToValueMap.get(speciesName);
				}
				else if (nodeName.equals("neighborQuantityLeftFull"))
				{

					int leftIndex = (int) evaluateExpressionRecursive(node.getChild(1));
					int rightIndex = (int) evaluateExpressionRecursive(node.getChild(2));
					String specName = node.getChild(0).getName().split("__")[node.getChild(0).getName().split("__").length - 1];
					String speciesName = "ROW" + leftIndex + "_COL" + (rightIndex - 1) + "__" + specName;

					if (variableToValueMap.containsKey(speciesName))
					{
						return variableToValueMap.get(speciesName);
					}
					return 1;
				}
				else if (nodeName.equals("neighborQuantityRightFull"))
				{

					int leftIndex = (int) evaluateExpressionRecursive(node.getChild(1));
					int rightIndex = (int) evaluateExpressionRecursive(node.getChild(2));
					String specName = node.getChild(0).getName().split("__")[node.getChild(0).getName().split("__").length - 1];
					String speciesName = "ROW" + leftIndex + "_COL" + (rightIndex + 1) + "__" + specName;

					if (variableToValueMap.containsKey(speciesName))
					{
						return variableToValueMap.get(speciesName);
					}
					return 1;
				}
				else if (nodeName.equals("neighborQuantityAboveFull"))
				{

					int leftIndex = (int) evaluateExpressionRecursive(node.getChild(1));
					int rightIndex = (int) evaluateExpressionRecursive(node.getChild(2));
					String specName = node.getChild(0).getName().split("__")[node.getChild(0).getName().split("__").length - 1];
					String speciesName = "ROW" + (leftIndex - 1) + "_COL" + rightIndex + "__" + specName;

					if (variableToValueMap.containsKey(speciesName))
					{
						return variableToValueMap.get(speciesName);
					}
					return 1;
				}
				else if (nodeName.equals("neighborQuantityBelowFull"))
				{

					int leftIndex = (int) evaluateExpressionRecursive(node.getChild(1));
					int rightIndex = (int) evaluateExpressionRecursive(node.getChild(2));
					String specName = node.getChild(0).getName().split("__")[node.getChild(0).getName().split("__").length - 1];
					String speciesName = "ROW" + (leftIndex + 1) + "_COL" + rightIndex + "__" + specName;

					if (variableToValueMap.containsKey(speciesName))
					{
						return variableToValueMap.get(speciesName);
					}
					return 1;
				}
				else if (nodeName.equals("getCompartmentLocationX"))
				{

					return this.componentToLocationMap.get(node.getChild(0).getName().split("__")[0]).getX();
				}
				else if (nodeName.equals("getCompartmentLocationY"))
				{

					return this.componentToLocationMap.get(node.getChild(0).getName().split("__")[0]).getY();
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
			{

				// loop through child triples
				// if child 1 is true, return child 0, else return child 2
				for (int childIter = 0; childIter < node.getChildCount(); childIter += 3)
				{

					if ((childIter + 1) < node.getChildCount() && getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter + 1))))
					{
						return evaluateExpressionRecursive(node.getChild(childIter));
					}
					else if ((childIter + 2) < node.getChildCount())
					{
						return evaluateExpressionRecursive(node.getChild(childIter + 2));
					}
				}

				return 0;
			}

			case FUNCTION_ROOT:
				return FastMath.pow(evaluateExpressionRecursive(node.getRightChild()), 1 / evaluateExpressionRecursive(node.getLeftChild()));

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
				// NOT PLANNING TO SUPPORT THIS
				return 0;

			case FUNCTION_ARCTANH:
				return Fmath.atanh(evaluateExpressionRecursive(node.getChild(0)));

			case FUNCTION_ARCSINH:
				return Fmath.asinh(evaluateExpressionRecursive(node.getChild(0)));

			case FUNCTION_ARCCOSH:
				return Fmath.acosh(evaluateExpressionRecursive(node.getChild(0)));

			case FUNCTION_ARCCOT:
				return Fmath.acot(evaluateExpressionRecursive(node.getChild(0)));

			case FUNCTION_ARCCOTH:
				return Fmath.acoth(evaluateExpressionRecursive(node.getChild(0)));

			case FUNCTION_ARCCSC:
				return Fmath.acsc(evaluateExpressionRecursive(node.getChild(0)));

			case FUNCTION_ARCCSCH:
				return Fmath.acsch(evaluateExpressionRecursive(node.getChild(0)));

			case FUNCTION_ARCSEC:
				return Fmath.asec(evaluateExpressionRecursive(node.getChild(0)));

			case FUNCTION_ARCSECH:
				return Fmath.asech(evaluateExpressionRecursive(node.getChild(0)));

			default:

			} // end switch

		}
		return 0.0;
	}

	/**
	 * adds species and reactions to the model that are implicit in arrays
	 * basically, it takes an arrayed model and flattens it
	 * 
	 * this also applies stoichiometry amplification at the end (if needed) i
	 * did it this way to avoid another read/write of the model, as i have to do
	 * it within the simulator in order to use jsbml instead of libsbml
	 */
	public static void expandArrays(String filename, double stoichAmpValue)
	{

		// open the sbml file for reading/writing
		SBMLDocument document = null;

		try
		{
			document = SBMLReader.read(new File(filename));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		Model model = document.getModel();

		boolean arraysExist = false;

		// ARRAYED SPECIES BUSINESS
		// create all new species that are implicit in the arrays and put them
		// into the model

		ArrayList<Species> speciesToAdd = new ArrayList<Species>();
		ArrayList<String> speciesToRemove = new ArrayList<String>();

		HashMap<String, Boolean> speciesToIsArrayedMap = new HashMap<String, Boolean>();
		HashMap<String, SpeciesDimensions> arrayedSpeciesToDimensionsMap = new HashMap<String, SpeciesDimensions>();

		try
		{
			for (Species species : model.getListOfSpecies())
			{

				String speciesID = species.getId();

				// check to see if the species is arrayed
				if (AnnotationUtility.parseSpeciesArrayAnnotation(species) != null)
				{

					arraysExist = true;

					speciesToIsArrayedMap.put(speciesID, true);
					speciesToRemove.add(speciesID);

					int numRowsLower = 0;
					int numColsLower = 0;
					int numRowsUpper = 0;
					int numColsUpper = 0;

					int[] values = AnnotationUtility.parseSpeciesArrayAnnotation(species);

					numRowsLower = values[0];
					numColsLower = values[1];
					numRowsUpper = values[2];
					numColsUpper = values[3];

					SpeciesDimensions speciesDimensions = new SpeciesDimensions(numRowsLower, numRowsUpper, numColsLower, numColsUpper);

					arrayedSpeciesToDimensionsMap.put(speciesID, speciesDimensions);

					// loop through all species in the array
					// prepend the row/col information to create a new ID
					for (int row = numRowsLower; row <= numRowsUpper; ++row)
					{
						for (int col = numColsLower; col <= numColsUpper; ++col)
						{

							speciesID = "ROW" + row + "_COL" + col + "__" + species.getId();

							Species newSpecies = new Species();
							newSpecies = species.clone();
							SBMLutilities.setMetaId(newSpecies, speciesID);
							newSpecies.setId(speciesID);
							newSpecies.setAnnotation(new Annotation());
							speciesToAdd.add(newSpecies);
						}
					}
				}
				else
				{
					speciesToIsArrayedMap.put(speciesID, false);
				}
			} // end species for loop

			// add new row/col species to the model
			for (Species species : speciesToAdd)
			{
				model.addSpecies(species);
			}

			// ARRAYED EVENTS BUSINESS

			ArrayList<String> eventsToRemove = new ArrayList<String>();
			ArrayList<Event> eventsToAdd = new ArrayList<Event>();

			for (Event event : model.getListOfEvents())
			{

				if (stripAnnotation(event.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim()).contains("array"))
				{

					arraysExist = true;

					eventsToRemove.add(event.getId());

					String annotationString = stripAnnotation(event.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim()).replace("<annotation>", "").replace("</annotation>", "").replace("\"", "");
					String[] splitAnnotation = annotationString.split("array:");
					ArrayList<String> eventCompartments = new ArrayList<String>();

					splitAnnotation[splitAnnotation.length - 2] = splitAnnotation[splitAnnotation.length - 2].split("xmlns:")[0];

					for (int i = 2; i < splitAnnotation.length; ++i)
					{

						String compartmentID = splitAnnotation[i].split("=")[0];
						eventCompartments.add(compartmentID);
					}

					// loop through all compartments and create an event for
					// each one
					for (String compartmentID : eventCompartments)
					{

						Event newEvent = new Event();
						newEvent.setVersion(event.getVersion());
						newEvent.setLevel(event.getLevel());
						newEvent.setId(compartmentID + "__" + event.getId());
						SBMLutilities.setMetaId(newEvent, compartmentID + "__" + event.getId());
						event.getTrigger().getMath().updateVariables();
						newEvent.setTrigger(event.getTrigger().clone());

						// //at this point, the formula has something like
						// neighborQuantity(Species1)
						// //this needs to become neighborQuantity(Species1,
						// CompartmentLocationX(Comp1),
						// CompartmentLocationY(Comp1))
						// if
						// (newEvent.getTrigger().getMath().toFormula().contains("neighborQuantity"))
						// {
						//
						// String triggerMath =
						// newEvent.getTrigger().getMath().toFormula();
						// ArrayList<ASTNode> nqNodes = new
						// ArrayList<ASTNode>();
						//
						// this.getSatisfyingNodesLax(newEvent.getTrigger().getMath(),
						// "neighborQuantity", nqNodes);
						//
						// //loop through all neighbor quantity nodes in the
						// trigger formula
						// for (ASTNode nqNode : nqNodes) {
						//
						// String direction = "";
						//
						// if (triggerMath.contains("QuantityLeft"))
						// direction = "Left";
						// else if (triggerMath.contains("QuantityRight"))
						// direction = "Right";
						// else if (triggerMath.contains("QuantityAbove"))
						// direction = "Above";
						// else
						// direction = "Below";
						//
						// String speciesID = nqNode.toFormula().split(
						// "neighborQuantity" +
						// direction)[1].replace("(","").replace(")","");
						//
						// try {
						// ASTNode newFormula = ASTNode.parseFormula(
						// "neighborQuantity" + direction + "Full(" +
						// compartmentID + "__" + speciesID +
						// ", getCompartmentLocationX(" + compartmentID +
						// "__Cell" +
						// "), getCompartmentLocationY(" + compartmentID +
						// "__Cell" + "))");
						//
						// for (int i = 0; i < ((ASTNode)
						// nqNode.getParent()).getChildCount(); ++i) {
						//
						// if (((ASTNode)
						// nqNode.getParent().getChildAt(i)).isFunction() &&
						// ((ASTNode)
						// nqNode.getParent().getChildAt(i)).getVariable().toString()
						// .contains("neighborQuantity" + direction)) {
						//
						// ((ASTNode) nqNode.getParent()).replaceChild(i,
						// newFormula);
						// break;
						// }
						// }
						// } catch (ParseException e) {
						// e.printStackTrace();
						// }
						// }
						// }

						if (event.isSetPriority())
						{
							newEvent.setPriority(event.getPriority().clone());
						}

						if (event.isSetDelay())
						{
							newEvent.setDelay(event.getDelay().clone());
						}

						newEvent.setUseValuesFromTriggerTime(event.getUseValuesFromTriggerTime());

						for (EventAssignment eventAssignment : event.getListOfEventAssignments())
						{

							EventAssignment ea = eventAssignment.clone();
							ea.setMath(eventAssignment.getMath().clone());
							ea.setVariable(eventAssignment.getVariable());
							newEvent.addEventAssignment(ea);
						}

						for (EventAssignment eventAssignment : newEvent.getListOfEventAssignments())
						{

							eventAssignment.setVariable(compartmentID + "__" + eventAssignment.getVariable());

							// prepends the compartment ID to all variables in
							// the event assignment
							prependToVariableNodes(eventAssignment.getMath(), compartmentID + "__", model);
						}

						eventsToAdd.add(newEvent);
					}
				}
			}

			for (Event eventToAdd : eventsToAdd)
			{
				model.addEvent(eventToAdd);
			}

			// ARRAYED REACTION BUSINESS
			// if a reaction has arrayed species
			// new reactions that are implicit are created and added to the
			// model

			ArrayList<Reaction> reactionsToAdd = new ArrayList<Reaction>();
			ArrayList<String> reactionsToRemove = new ArrayList<String>();

			for (Reaction reaction : model.getListOfReactions())
			{

				String reactionID = reaction.getId();

				ArrayList<Integer> membraneDiffusionRows = new ArrayList<Integer>();
				ArrayList<Integer> membraneDiffusionCols = new ArrayList<Integer>();
				ArrayList<String> membraneDiffusionCompartments = new ArrayList<String>();

				// MEMBRANE DIFFUSION REACTIONS
				// if it's a membrane diffusion reaction it'll have the
				// appropriate locations as an annotation
				// so parse them and store them in the above arraylists
				if (reactionID.contains("MembraneDiffusion"))
				{

					arraysExist = true;

					String[] splitAnnotation = AnnotationUtility.parseArrayAnnotation(reaction);
					for (int i = 1; i < splitAnnotation.length; i++)
					{
						String compartmentID = splitAnnotation[i].split("=")[0];
						String row = splitAnnotation[i].split(" ")[0].split("=")[1].split(",")[0].replace("(", "");
						String col = splitAnnotation[i].split(" ")[0].split("=")[1].split(",")[1].replace(")", "");

						membraneDiffusionRows.add(Integer.valueOf(row.trim()));
						membraneDiffusionCols.add(Integer.valueOf(col.trim()));
						membraneDiffusionCompartments.add(compartmentID);
					}

					int membraneDiffusionIndex = 0;

					reactionsToRemove.add(reaction.getId());
					reaction.setAnnotation(new Annotation());

					// loop through all appropriate row/col pairs and create a
					// membrane diffusion reaction for each one
					for (String compartmentID : membraneDiffusionCompartments)
					{

						int row = membraneDiffusionRows.get(membraneDiffusionIndex);
						int col = membraneDiffusionCols.get(membraneDiffusionIndex);

						// create a new reaction and set the ID
						Reaction newReaction = new Reaction();
						newReaction = reaction.clone();
						newReaction.setListOfReactants(new ListOf<SpeciesReference>());
						newReaction.setListOfProducts(new ListOf<SpeciesReference>());
						newReaction.setListOfModifiers(new ListOf<ModifierSpeciesReference>());
						newReaction.setId(compartmentID + "__" + reactionID);
						SBMLutilities.setMetaId(newReaction, compartmentID + "__" + reactionID);
						newReaction.setReversible(true);
						newReaction.setFast(false);
						newReaction.setCompartment(reaction.getCompartment());

						// alter the kinetic law to so that it has the correct
						// indexes as children for the
						// get2DArrayElement function
						// get the nodes to alter (that are arguments for the
						// get2DArrayElement method)
						ArrayList<ASTNode> get2DArrayElementNodes = new ArrayList<ASTNode>();

						getSatisfyingNodes(newReaction.getKineticLaw().getMath(), "get2DArrayElement", get2DArrayElementNodes);

						boolean reactantBool = false;

						// replace the get2darrayelement stuff with the proper
						// explicit species/parameters
						for (ASTNode node : get2DArrayElementNodes)
						{

							if (node.getLeftChild().getName().contains("kmdiff"))
							{

								String parameterName = node.getLeftChild().getName();

								// see if the species-specific one exists
								// if it doesn't, use the default
								// you'll need to parse the species name from
								// the reaction id, probably

								String speciesID = reactionID.replace("MembraneDiffusion_", "");

								if (model.getParameter(compartmentID + "__" + speciesID + "__" + parameterName) == null)
								{
									node.setVariable(model.getParameter(parameterName));
								}
								else
								{
									node.setVariable(model.getParameter(compartmentID + "__" + speciesID + "__" + parameterName));
								}

								for (int i = 0; i < node.getChildCount(); ++i)
								{
									node.removeChild(i);
								}

								for (int i = 0; i < node.getChildCount(); ++i)
								{
									node.removeChild(i);
								}
							}
							// this means it's a species, which we need to
							// prepend with the row/col prefix
							else
							{

								if (node.getChildCount() > 0 && model.getParameter(node.getLeftChild().getName()) == null)
								{

									// reactant
									if (reactantBool == true)
									{

										node.setVariable(model.getSpecies("ROW" + row + "_COL" + col + "__" + node.getLeftChild().getName()));
									}
									// product
									else
									{

										node.setVariable(model.getSpecies(compartmentID + "__" + node.getLeftChild().getName()));
										reactantBool = true;
									}
								}

								for (int i = 0; i < node.getChildCount(); ++i)
								{
									node.removeChild(i);
								}

								for (int i = 0; i < node.getChildCount(); ++i)
								{
									node.removeChild(i);
								}
							}
						}

						// loop through reactants
						for (SpeciesReference reactant : reaction.getListOfReactants())
						{

							// create a new reactant and add it to the new
							// reaction
							SpeciesReference newReactant = new SpeciesReference();
							newReactant = reactant.clone();
							newReactant.setSpecies(compartmentID + "__" + newReactant.getSpecies());
							newReactant.setAnnotation(new Annotation());
							newReaction.addReactant(newReactant);
						}

						// loop through products
						for (SpeciesReference product : reaction.getListOfProducts())
						{

							// create a new reactant and add it to the new
							// reaction
							SpeciesReference newProduct = new SpeciesReference();
							newProduct = product.clone();
							newProduct.setSpecies("ROW" + row + "_COL" + col + "__" + newProduct.getSpecies());
							newProduct.setAnnotation(new Annotation());
							newReaction.addProduct(newProduct);
						}

						boolean i = false, j = false;

						for (LocalParameter lp : newReaction.getKineticLaw().getListOfLocalParameters())
						{

							if (lp.getId().equals("i"))
							{
								i = true;
							}
							else if (lp.getId().equals("j"))
							{
								j = true;
							}
						}

						if (i)
						{
							newReaction.getKineticLaw().getListOfLocalParameters().remove("i");
						}

						if (j)
						{
							newReaction.getKineticLaw().getListOfLocalParameters().remove("j");
						}

						reactionsToAdd.add(newReaction);

						++membraneDiffusionIndex;
					}
				} // end if membrane diffusion

				// NON-MEMBRANE DIFFUSION REACTIONS
				// check to see if the (non-membrane-diffusion) reaction has
				// arrayed species
				// right now i'm only checking the first reactant species, due
				// to a bad assumption
				// about the homogeneity of the arrayed reaction (ie, if one
				// species is arrayed, they all are)
				else if (reaction.getReactantCount() > 0 && reaction.getReactant(0).getSpeciesInstance() != null && speciesToIsArrayedMap.get(reaction.getReactant(0).getSpeciesInstance().getId()) == true)
				{

					arraysExist = true;

					reactionsToRemove.add(reaction.getId());

					// get the reactant dimensions, which tells us how many new
					// reactions are going to be created
					SpeciesDimensions reactantDimensions = arrayedSpeciesToDimensionsMap.get(reaction.getReactant(0).getSpeciesInstance().getId());

					boolean abort = false;

					// loop through all of the new formerly-implicit reactants
					for (int row = reactantDimensions.numRowsLower; row <= reactantDimensions.numRowsUpper; ++row)
					{
						for (int col = reactantDimensions.numColsLower; col <= reactantDimensions.numColsUpper; ++col)
						{

							// create a new reaction and set the ID
							Reaction newReaction = new Reaction();
							newReaction = reaction.clone();
							newReaction.setListOfReactants(new ListOf<SpeciesReference>());
							newReaction.setListOfProducts(new ListOf<SpeciesReference>());
							newReaction.setListOfModifiers(new ListOf<ModifierSpeciesReference>());
							newReaction.setId("ROW" + row + "_COL" + col + "_" + reactionID);
							SBMLutilities.setMetaId(newReaction, "ROW" + row + "_COL" + col + "_" + reactionID);
							newReaction.setReversible(false);
							newReaction.setFast(false);
							newReaction.setCompartment(reaction.getCompartment());

							// get the nodes to alter
							ArrayList<ASTNode> get2DArrayElementNodes = new ArrayList<ASTNode>();

							// return the head node of the get2DArrayElement
							// function
							getSatisfyingNodes(newReaction.getKineticLaw().getMath(), "get2DArrayElement", get2DArrayElementNodes);

							// loop through all reactants
							for (SpeciesReference reactant : reaction.getListOfReactants())
							{

								reactant.unsetMetaId();

								// find offsets
								// the row offset is in the kinetic law via i
								// the col offset is in the kinetic law via j
								int rowOffset = 0;
								int colOffset = 0;

								ASTNode reactantHeadNode = null;

								// go through the get2DArrayElement nodes and
								// find the one corresponding to the reactant
								for (ASTNode headNode : get2DArrayElementNodes)
								{

									// make sure it's a reactant node
									if (headNode.getChildCount() > 0 && model.getParameter(headNode.getLeftChild().getName()) == null)
									{

										reactantHeadNode = headNode;
										break;
									}
								}
								if (reactantHeadNode == null)
								{
									// TODO: Is this possible?
									System.out.println("Could not find reactant");
									continue;
								}

								if (reactantHeadNode.getChild(1).getType().name().equals("PLUS"))
								{
									rowOffset = reactantHeadNode.getChild(1).getRightChild().getInteger();
								}
								else if (reactantHeadNode.getChild(1).getType().name().equals("MINUS"))
								{
									rowOffset = -1 * reactantHeadNode.getChild(1).getRightChild().getInteger();
								}

								if (reactantHeadNode.getChild(2).getType().name().equals("PLUS"))
								{
									colOffset = reactantHeadNode.getChild(2).getRightChild().getInteger();
								}
								else if (reactantHeadNode.getChild(2).getType().name().equals("MINUS"))
								{
									colOffset = -1 * reactantHeadNode.getChild(2).getRightChild().getInteger();
								}

								// create a new reactant and add it to the new
								// reaction
								SpeciesReference newReactant = new SpeciesReference();
								newReactant = reactant.clone();
								newReactant.setSpecies(model.getSpecies("ROW" + (row + rowOffset) + "_COL" + (col + colOffset) + "__" + newReactant.getSpecies()));
								newReactant.setAnnotation(new Annotation());
								newReaction.addReactant(newReactant);

								// put this reactant in place of the
								// get2DArrayElement function call
								for (int i = 0; i < reactantHeadNode.getChildCount(); ++i)
								{
									reactantHeadNode.removeChild(i);
								}

								for (int i = 0; i < reactantHeadNode.getChildCount(); ++i)
								{
									reactantHeadNode.removeChild(i);
								}

								reactantHeadNode.setVariable(model.getSpecies(newReactant.getSpecies()));
							}// end looping through reactants

							// loop through all modifiers
							for (ModifierSpeciesReference modifier : reaction.getListOfModifiers())
							{
								modifier.unsetMetaId();

							}

							// loop through all products
							for (SpeciesReference product : reaction.getListOfProducts())
							{

								product.unsetMetaId();
								// find offsets
								int rowOffset = 0;
								int colOffset = 0;

								ASTNode productHeadNode = null;

								// go through the get2DArrayElement nodes and
								// find the one corresponding to the product
								for (ASTNode headNode : get2DArrayElementNodes)
								{

									// make sure it's a product node
									// only the product has children, as the
									// reactant's children get deleted
									if (headNode.getChildCount() > 0 && model.getParameter(headNode.getLeftChild().getName()) == null)
									{

										productHeadNode = headNode;
										break;
									}
								}
								if (productHeadNode == null)
								{
									// TODO: Is this possible?
									System.out.println("Could not find product");
									return;
								}

								if (productHeadNode.getChild(1).getType().name().equals("PLUS"))
								{
									rowOffset = productHeadNode.getChild(1).getRightChild().getInteger();
								}
								else if (productHeadNode.getChild(1).getType().name().equals("MINUS"))
								{
									rowOffset = -1 * productHeadNode.getChild(1).getRightChild().getInteger();
								}

								if (productHeadNode.getChild(2).getType().name().equals("PLUS"))
								{
									colOffset = productHeadNode.getChild(2).getRightChild().getInteger();
								}
								else if (productHeadNode.getChild(2).getType().name().equals("MINUS"))
								{
									colOffset = -1 * productHeadNode.getChild(2).getRightChild().getInteger();
								}

								// don't create reactions with products that
								// don't exist
								if (row + rowOffset < reactantDimensions.numRowsLower || col + colOffset < reactantDimensions.numColsLower || row + rowOffset > reactantDimensions.numRowsUpper || col + colOffset > reactantDimensions.numColsUpper)
								{

									abort = true;
									break;
								}

								// create a new product and add it to the new
								// reaction
								SpeciesReference newProduct = new SpeciesReference();
								newProduct = product.clone();
								newProduct.setSpecies(model.getSpecies("ROW" + (row + rowOffset) + "_COL" + (col + colOffset) + "__" + newProduct.getSpecies()));
								newProduct.setAnnotation(new Annotation());
								newReaction.addProduct(newProduct);

								// put this reactant in place of the
								// get2DArrayElement function call
								for (int i = 0; i < productHeadNode.getChildCount(); ++i)
								{
									productHeadNode.removeChild(i);
								}

								// put this reactant in place of the
								// get2DArrayElement function call
								for (int i = 0; i < productHeadNode.getChildCount(); ++i)
								{
									productHeadNode.removeChild(i);
								}

								productHeadNode.setVariable(model.getSpecies(newProduct.getSpecies()));
							} // end looping through products

							if (abort == false)
							{

								boolean i = false, j = false;

								// checking for these local parameters using
								// getLocalParameters() doesn't seem to work
								for (LocalParameter lp : newReaction.getKineticLaw().getListOfLocalParameters())
								{
									lp.unsetMetaId();
									if (lp.getId().equals("i"))
									{
										i = true;
									}
									else if (lp.getId().equals("j"))
									{
										j = true;
									}
								}

								if (i)
								{
									newReaction.getKineticLaw().getListOfLocalParameters().remove("i");
								}

								if (j)
								{
									newReaction.getKineticLaw().getListOfLocalParameters().remove("j");
								}

								// this isn't a reversible reaction; only take
								// the left side
								if (newReaction.getId().contains(GlobalConstants.DEGRADATION) == false)
								{
									newReaction.getKineticLaw().setMath(newReaction.getKineticLaw().getMath().getLeftChild());
								}

								reactionsToAdd.add(newReaction);
							}
							else
							{
								abort = false;
							}
						}
					}

				}
			}// end looping through reactions

			// add in the new explicit array reactions
			for (Reaction reactionToAdd : reactionsToAdd)
			{

				SBMLutilities.setMetaId(reactionToAdd, reactionToAdd.getId());
				if (model.getReaction(reactionToAdd.getId()) != null)
				{
					model.removeReaction(reactionToAdd.getId());
				}
				model.addReaction(reactionToAdd);
			}

			ListOf<Reaction> allReactions = model.getListOfReactions();

			// remove original array reaction(s)
			for (String reactionToRemove : reactionsToRemove)
			{
				allReactions.remove(reactionToRemove);
			}

			model.setListOfReactions(allReactions);

			ListOf<Species> allSpecies = model.getListOfSpecies();

			// remove the original array species from the model
			for (String speciesID : speciesToRemove)
			{
				allSpecies.remove(speciesID);
			}

			model.setListOfSpecies(allSpecies);

			ListOf<Event> allEvents = model.getListOfEvents();

			// remove original array event(s)
			for (String eventID : eventsToRemove)
			{
				allEvents.remove(eventID);
			}

			model.setListOfEvents(allEvents);

			ArrayList<String> parametersToRemove = new ArrayList<String>();

			// get rid of the locations parameters
			for (Parameter parameter : model.getListOfParameters())
			{
				if (parameter.getId().contains("_locations"))
				{
					parametersToRemove.add(parameter.getId());
				}
			}

			for (String parameterID : parametersToRemove)
			{
				model.removeParameter(parameterID);
			}

			applyStoichiometryAmplification(model, stoichAmpValue);

			if (arraysExist)
			{

				SBMLWriter writer = new SBMLWriter();
				PrintStream p;

				try
				{
					p = new PrintStream(new FileOutputStream(filename), true, "UTF-8");
					p.print(writer.writeSBMLToString(model.getSBMLDocument()));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (XMLStreamException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * fires events
	 * 
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 */
	protected HashSet<String> fireEvents(final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag)
	{

		// temporary set of events to remove from the triggeredEventQueue
		HashSet<String> untriggeredEvents = new HashSet<String>();

		// loop through all triggered events
		// if they aren't persistent and the trigger is no longer true
		// remove from triggered queue and put into untriggered set
		for (EventToFire triggeredEvent : triggeredEventQueue)
		{

			String triggeredEventID = triggeredEvent.eventID;

			// if the trigger evaluates to false and the trigger isn't
			// persistent
			if (eventToTriggerPersistenceMap.get(triggeredEventID) == false && getBooleanFromDouble(evaluateExpressionRecursive(eventToTriggerMap.get(triggeredEventID))) == false)
			{

				untriggeredEvents.add(triggeredEventID);
				eventToPreviousTriggerValueMap.put(triggeredEventID, false);
			}

			if (eventToTriggerPersistenceMap.get(triggeredEventID) == true && getBooleanFromDouble(evaluateExpressionRecursive(eventToTriggerMap.get(triggeredEventID))) == false)
			{
				untriggeredEventSet.add(triggeredEventID);
			}
		}

		// copy the triggered event queue -- except the events that are now
		// untriggered
		// this is done because the remove function can't work with just a
		// string; it needs to match events
		// this also re-evaluates the priorities in case they have changed
		PriorityQueue<EventToFire> newTriggeredEventQueue = new PriorityQueue<EventToFire>(5, eventComparator);

		while (triggeredEventQueue.size() > 0)
		{

			EventToFire event = triggeredEventQueue.poll();
			EventToFire eventToAdd = new EventToFire(event.eventID, (HashSet<Object>) event.eventAssignmentSet.clone(), event.fireTime);

			if (untriggeredEvents.contains(event.eventID) == false)
			{
				newTriggeredEventQueue.add(eventToAdd);
			}
			else
			{
				untriggeredEventSet.add(event.eventID);
			}
		}

		triggeredEventQueue = newTriggeredEventQueue;

		// loop through untriggered events
		// if the trigger is no longer true
		// set the previous trigger value to false
		for (String untriggeredEventID : untriggeredEventSet)
		{

			if (eventToTriggerPersistenceMap.get(untriggeredEventID) == false && getBooleanFromDouble(evaluateExpressionRecursive(eventToTriggerMap.get(untriggeredEventID))) == false)
			{
				eventToPreviousTriggerValueMap.put(untriggeredEventID, false);
			}
		}

		// these are sets of things that need to be re-evaluated or tested due
		// to the event firing
		HashSet<String> affectedReactionSet = new HashSet<String>();
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		// set of fired events to add to the untriggered set
		// HashSet<String> firedEvents = new HashSet<String>();

		// set of events to be removed due to component erasure
		HashSet<String> deadEvents = new HashSet<String>();

		List<StringDoublePair> listOfAssignments = new ArrayList<StringDoublePair>();

		// fire all events whose fire time is less than the current time
		while (triggeredEventQueue.size() > 0 && triggeredEventQueue.peek().fireTime <= currentTime)
		{

			EventToFire eventToFire = triggeredEventQueue.poll();
			String eventToFireID = eventToFire.eventID;

			// System.err.println("firing " + eventToFireID);

			if (eventToAffectedReactionSetMap.get(eventToFireID) != null)
			{
				affectedReactionSet.addAll(eventToAffectedReactionSetMap.get(eventToFireID));
			}

			untriggeredEventSet.add(eventToFireID);

			// firedEvents.add(eventToFireID);
			eventToPreviousTriggerValueMap.put(eventToFireID, true);

			// handle dynamic events
			// the duplication method handles all event assignments
			// so those only get executed here if it's not a dynamic event
			if (eventToFire.eventID.contains("__AsymmetricDivision__"))
			{

				String compartmentID = eventToFire.eventID.split("__")[0];
				duplicateComponent(compartmentID, eventToFire.eventID, "asymmetric");
			}
			else if (eventToFire.eventID.contains("__SymmetricDivision__"))
			{

				String compartmentID = eventToFire.eventID.split("__")[0];
				duplicateComponent(compartmentID, eventToFire.eventID, "symmetric");
			}
			else if (eventToFire.eventID.contains("__Death__"))
			{

				String compartmentID = eventToFire.eventID.split("__")[0];
				deadEvents = eraseComponent(compartmentID);

				// firedEvents.removeAll(deadEvents);

				if (deadEvents.size() > 0)
				{

					for (String eventID : deadEvents)
					{

						untriggeredEventSet.remove(eventID);
						eventToPriorityMap.remove(eventID);
						eventToDelayMap.remove(eventID);
						eventToHasDelayMap.remove(eventID);
						eventToTriggerPersistenceMap.remove(eventID);
						eventToUseValuesFromTriggerTimeMap.remove(eventID);
						eventToTriggerMap.remove(eventID);
						eventToTriggerInitiallyTrueMap.remove(eventID);
						eventToPreviousTriggerValueMap.remove(eventID);
						eventToAssignmentSetMap.remove(eventID);
						eventToAffectedReactionSetMap.remove(eventID);
					}

					// copy the triggered event queue -- except the events that
					// are now dead/removed
					newTriggeredEventQueue = new PriorityQueue<EventToFire>(5, eventComparator);

					while (triggeredEventQueue.size() > 0)
					{

						EventToFire event = triggeredEventQueue.poll();
						EventToFire eventToAdd = new EventToFire(event.eventID, (HashSet<Object>) event.eventAssignmentSet.clone(), event.fireTime);

						if (deadEvents.contains(event.eventID) == false)
						{
							newTriggeredEventQueue.add(eventToAdd);
						}
					}

					triggeredEventQueue = newTriggeredEventQueue;
				}
			}
			else if (eventToFire.eventID.contains("__Move"))
			{

				int direction = (int) (randomNumberGenerator.nextDouble() * 4.0);

				if (eventToFire.eventID.contains("__MoveLeft__"))
				{
					direction = 0;
				}
				else if (eventToFire.eventID.contains("__MoveRight__"))
				{
					direction = 1;
				}
				else if (eventToFire.eventID.contains("__MoveAbove__"))
				{
					direction = 2;
				}
				else if (eventToFire.eventID.contains("__MoveBelow__"))
				{
					direction = 3;
				}

				// reactions that change and thus need their propensities
				// re-evaluated
				HashSet<String> reactionsToAdjust = new HashSet<String>();

				String compartmentID = eventToFire.eventID.split("__")[0];
				Point parentLocation = componentToLocationMap.get(compartmentID);
				Point childLocation = (Point) parentLocation.clone();
				moveComponent(compartmentID, "", childLocation, direction, reactionsToAdjust);
				updatePropensities(reactionsToAdjust);
				updateAfterDynamicChanges();
			}
			else
			{

				// TODO: split into two loops, one to evaluate, second to update
				// state
				// execute all assignments for this event
				for (Object eventAssignment : eventToFire.eventAssignmentSet)
				{

					String variable;
					double assignmentValue;

					if (eventToUseValuesFromTriggerTimeMap.get(eventToFireID) == true)
					{

						variable = ((StringDoublePair) eventAssignment).string;
						assignmentValue = ((StringDoublePair) eventAssignment).doub;
					}
					// assignment needs to be evaluated
					else
					{

						variable = ((EventAssignment) eventAssignment).getVariable();
						assignmentValue = evaluateExpressionRecursive(((EventAssignment) eventAssignment).getMath());
					}

					listOfAssignments.add(new StringDoublePair(variable, assignmentValue));
					// update the species, but only if it's not a constant
					// (bound. cond. is fine)
					// if (variableToIsConstantMap.get(variable) == false)
					// {
					//
					// if
					// (speciesToHasOnlySubstanceUnitsMap.containsKey(variable)
					// && speciesToHasOnlySubstanceUnitsMap.get(variable) ==
					// false)
					// {
					// variableToValueMap.put(variable, assignmentValue
					// * speciesToCompartmentSizeMap.get(variable));
					// }
					// else
					// {
					// variableToValueMap.put(variable, assignmentValue);
					// }
					// }

					// if this variable that was just updated is part of an
					// assignment rule (RHS)
					// then re-evaluate that assignment rule
					if (noAssignmentRulesFlag == false && variableToIsInAssignmentRuleMap.get(variable) == true)
					{
						affectedAssignmentRuleSet.addAll(variableToAffectedAssignmentRuleSetMap.get(variable));
					}

					if (noConstraintsFlag == false && variableToIsInConstraintMap.get(variable) == true)
					{
						affectedConstraintSet.addAll(variableToAffectedConstraintSetMap.get(variable));
					}
				} // end loop through assignments
			}

			// after an event fires, need to make sure the queue is updated
			untriggeredEvents.clear();

			// loop through all triggered events
			// if they aren't persistent and the trigger is no longer true
			// remove from triggered queue and put into untriggered set
			for (EventToFire triggeredEvent : triggeredEventQueue)
			{

				String triggeredEventID = triggeredEvent.eventID;

				// if the trigger evaluates to false and the trigger isn't
				// persistent
				if (eventToTriggerPersistenceMap.get(triggeredEventID) == false && getBooleanFromDouble(evaluateExpressionRecursive(eventToTriggerMap.get(triggeredEventID))) == false)
				{

					untriggeredEvents.add(triggeredEventID);
					eventToPreviousTriggerValueMap.put(triggeredEventID, false);
				}

				if (eventToTriggerPersistenceMap.get(triggeredEventID) == true && getBooleanFromDouble(evaluateExpressionRecursive(eventToTriggerMap.get(triggeredEventID))) == false)
				{
					untriggeredEventSet.add(triggeredEventID);
				}
			}

			// copy the triggered event queue -- except the events that are now
			// untriggered
			// this is done because the remove function can't work with just a
			// string; it needs to match events
			// this also re-evaluates the priorities in case they have changed
			newTriggeredEventQueue = new PriorityQueue<EventToFire>(5, eventComparator);

			while (triggeredEventQueue.size() > 0)
			{

				EventToFire event = triggeredEventQueue.poll();
				EventToFire eventToAdd = new EventToFire(event.eventID, (HashSet<Object>) event.eventAssignmentSet.clone(), event.fireTime);

				if (untriggeredEvents.contains(event.eventID) == false)
				{
					newTriggeredEventQueue.add(eventToAdd);
				}
				else
				{
					untriggeredEventSet.add(event.eventID);
				}
			}

			triggeredEventQueue = newTriggeredEventQueue;

			// some events might trigger after this
			handleEvents();
		}// end loop through event queue

		// add the fired events back into the untriggered set
		// this allows them to trigger/fire again later
		// untriggeredEventSet.addAll(firedEvents);

		for (int i = 0; i < listOfAssignments.size(); ++i)
		{
			String variable = listOfAssignments.get(i).string;
			double assignmentValue = listOfAssignments.get(i).doub;
			if (variableToIsConstantMap.get(variable) == false)
			{

				if (speciesToHasOnlySubstanceUnitsMap.containsKey(variable) && speciesToHasOnlySubstanceUnitsMap.get(variable) == false)
				{
					variableToValueMap.put(variable, assignmentValue * speciesToCompartmentSizeMap.get(variable));
				}
				else
				{
					variableToValueMap.put(variable, assignmentValue);
				}
			}
		}

		if (affectedAssignmentRuleSet.size() > 0)
		{
			performAssignmentRules(affectedAssignmentRuleSet);
		}

		if (affectedConstraintSet.size() > 0)
		{

			if (testConstraints(affectedConstraintSet) == false)
			{
				constraintFailureFlag = true;
			}
		}

		return affectedReactionSet;
	}

	/**
	 * recursively puts every astnode child into the arraylist passed in
	 * 
	 * @param node
	 * @param nodeChildrenList
	 */
	protected void getAllASTNodeChildren(ASTNode node, ArrayList<ASTNode> nodeChildrenList)
	{

		for (ASTNode child : node.getChildren())
		{

			if (child.getChildCount() == 0)
			{
				nodeChildrenList.add(child);
			}
			else
			{
				nodeChildrenList.add(child);
				getAllASTNodeChildren(child, nodeChildrenList);
			}
		}
	}

	/**
	 * returns a set of all the reactions that the recently performed reaction
	 * affects "affect" means that the species updates will change the affected
	 * reaction's propensity
	 * 
	 * @param selectedReactionID
	 *            the reaction that was recently performed
	 * @return the set of all reactions that the performed reaction affects the
	 *         propensity of
	 */
	protected HashSet<String> getAffectedReactionSet(String selectedReactionID, final boolean noAssignmentRulesFlag)
	{

		HashSet<String> affectedReactionSet = new HashSet<String>(20);
		affectedReactionSet.add(selectedReactionID);

		// loop through the reaction's reactants and products
		for (StringDoublePair speciesAndStoichiometry : reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID))
		{

			String speciesID = speciesAndStoichiometry.string;
			affectedReactionSet.addAll(speciesToAffectedReactionSetMap.get(speciesID));

			// if the species is involved in an assignment rule then it its
			// changing may affect a reaction's propensity
			if (noAssignmentRulesFlag == false && variableToIsInAssignmentRuleMap.get(speciesID))
			{

				// this assignment rule is going to be evaluated, so the rule's
				// variable's value will change
				for (AssignmentRule assignmentRule : variableToAffectedAssignmentRuleSetMap.get(speciesID))
				{
					if (speciesToAffectedReactionSetMap.get(assignmentRule.getVariable()) != null)
					{
						affectedReactionSet.addAll(speciesToAffectedReactionSetMap.get(assignmentRule.getVariable()));
					}
				}
			}
		}

		return affectedReactionSet;
	}

	/**
	 * kind of a hack to mingle doubles and booleans for the expression
	 * evaluator
	 * 
	 * @param value
	 *            the double to be translated to a boolean
	 * @return the translated boolean value
	 */
	protected static boolean getBooleanFromDouble(double value)
	{

		if (value == 0.0)
		{
			return false;
		}
		return true;
	}

	/**
	 * kind of a hack to mingle doubles and booleans for the expression
	 * evaluator
	 * 
	 * @param value
	 *            the boolean to be translated to a double
	 * @return the translated double value
	 */
	protected static double getDoubleFromBoolean(boolean value)
	{

		if (value == true)
		{
			return 1.0;
		}
		return 0.0;
	}

	/**
	 * recursively puts the nodes that have the same name as the quarry string
	 * passed in into the arraylist passed in so, the entire tree is searched
	 * through, which i don't think is possible with the jsbml methods
	 * 
	 * @param node
	 *            node to search through
	 * @param quarry
	 *            string to search for
	 * @param satisfyingNodes
	 *            list of nodes that satisfy the condition
	 */
	static void getSatisfyingNodes(ASTNode node, String quarry, ArrayList<ASTNode> satisfyingNodes)
	{

		if (node.isName() && node.getName().equals(quarry))
		{
			satisfyingNodes.add(node);
		}
		else if (node.isFunction() && node.getName().equals(quarry))
		{
			satisfyingNodes.add(node);
		}
		else
		{
			for (ASTNode childNode : node.getChildren())
			{
				getSatisfyingNodes(childNode, quarry, satisfyingNodes);
			}
		}
	}

	/**
	 * recursively puts the nodes that have the same name as the quarry string
	 * passed in into the arraylist passed in so, the entire tree is searched
	 * through, which i don't think is possible with the jsbml methods the lax
	 * version uses contains instead of equals
	 * 
	 * @param node
	 *            node to search through
	 * @param quarry
	 *            string to search for
	 * @param satisfyingNodes
	 *            list of nodes that satisfy the condition
	 */
	void getSatisfyingNodesLax(ASTNode node, String quarry, ArrayList<ASTNode> satisfyingNodes)
	{

		if (node.isName() && node.getName().contains(quarry))
		{
			satisfyingNodes.add(node);
		}
		else if (node.isFunction() && node.getName().contains(quarry))
		{
			satisfyingNodes.add(node);
		}
		else
		{
			for (ASTNode childNode : node.getChildren())
			{
				getSatisfyingNodesLax(childNode, quarry, satisfyingNodes);
			}
		}
	}

	/**
	 * updates the event queue and fires events and so on
	 * 
	 * @param currentTime
	 *            the current time in the simulation
	 */
	protected void handleEvents()
	{

		HashSet<String> triggeredEvents = new HashSet<String>();

		// loop through all untriggered events
		// if any trigger, evaluate the fire time(s) and add them to the queue
		for (String untriggeredEventID : untriggeredEventSet)
		{

			// System.err.println(untriggeredEventID);
			// System.err.println(eventToTriggerMap.get(untriggeredEventID));

			// if the trigger evaluates to true
			if (getBooleanFromDouble(evaluateExpressionRecursive(eventToTriggerMap.get(untriggeredEventID))) == true)
			{

				// skip the event if it's initially true and this is time == 0
				if (currentTime == 0.0 && eventToTriggerInitiallyTrueMap.get(untriggeredEventID) == true)
				{
					continue;
				}

				// switch from false to true must happen
				if (eventToPreviousTriggerValueMap.get(untriggeredEventID) == true)
				{
					continue;
				}

				triggeredEvents.add(untriggeredEventID);

				// if assignment is to be evaluated at trigger time, evaluate it
				// and replace the ASTNode assignment
				if (eventToUseValuesFromTriggerTimeMap.get(untriggeredEventID) == true)
				{

					// temporary hashset of evaluated assignments
					HashSet<Object> evaluatedAssignments = new HashSet<Object>();

					for (Object evAssignment : eventToAssignmentSetMap.get(untriggeredEventID))
					{

						EventAssignment eventAssignment = (EventAssignment) evAssignment;
						evaluatedAssignments.add(new StringDoublePair(eventAssignment.getVariable(), evaluateExpressionRecursive(eventAssignment.getMath())));
					}

					double fireTime = currentTime;

					if (eventToHasDelayMap.get(untriggeredEventID) == true)
					{
						fireTime += evaluateExpressionRecursive(eventToDelayMap.get(untriggeredEventID));
					}

					triggeredEventQueue.add(new EventToFire(untriggeredEventID, evaluatedAssignments, fireTime));
				}
				else
				{

					double fireTime = currentTime;

					if (eventToHasDelayMap.get(untriggeredEventID) == true)
					{
						fireTime += evaluateExpressionRecursive(eventToDelayMap.get(untriggeredEventID));
					}

					triggeredEventQueue.add(new EventToFire(untriggeredEventID, eventToAssignmentSetMap.get(untriggeredEventID), fireTime));
				}
			}
			else
			{

				eventToPreviousTriggerValueMap.put(untriggeredEventID, false);
			}
		}

		// remove recently triggered events from the untriggered set
		// when they're fired, they get put back into the untriggered set
		untriggeredEventSet.removeAll(triggeredEvents);
	}

	public void replaceArgument(ASTNode formula, String bvar, ASTNode arg)
	{
		int n = 0;
		for (int i = 0; i < formula.getChildCount(); i++)
		{
			ASTNode child = formula.getChild(i);
			if (child.isString() && child.getName().equals(bvar))
			{
				formula.replaceChild(n, arg.clone());
			}
			else if (child.getChildCount() > 0)
			{
				replaceArgument(child, bvar, arg);
			}
			n++;
		}
	}

	/**
	 * inlines a formula with function definitions
	 * 
	 * @param formula
	 * @return
	 */
	protected ASTNode inlineFormula(ASTNode formula)
	{

		if (formula.isFunction() == false || formula.isOperator()/* || formula.isLeaf() == false */)
		{

			for (int i = 0; i < formula.getChildCount(); ++i)
			{
				formula.replaceChild(i, inlineFormula(formula.getChild(i)));// .clone()));
			}
		}
		else if (formula.isFunction() && model.getFunctionDefinition(formula.getName()) != null)
		{

			if (ibiosimFunctionDefinitions.contains(formula.getName()))
			{
				return formula;
			}

			ASTNode inlinedFormula = model.getFunctionDefinition(formula.getName()).getBody().clone();
			ASTNode oldFormula = formula.clone();

			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			this.getAllASTNodeChildren(inlinedFormula, inlinedChildren);

			if (inlinedChildren.size() == 0)
			{
				inlinedChildren.add(inlinedFormula);
			}

			HashMap<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();

			for (int i = 0; i < model.getFunctionDefinition(formula.getName()).getArgumentCount(); ++i)
			{
				inlinedChildToOldIndexMap.put(model.getFunctionDefinition(formula.getName()).getArgument(i).getName(), i);
			}

			for (int i = 0; i < inlinedChildren.size(); ++i)
			{

				ASTNode child = inlinedChildren.get(i);

				if (child.isLeaf() && child.isName())
				{

					int index = inlinedChildToOldIndexMap.get(child.getName());
					replaceArgument(inlinedFormula, child.toFormula(), oldFormula.getChild(index));

					if (inlinedFormula.getChildCount() == 0)
					{
						inlinedFormula = oldFormula.getChild(index);
					}
				}
			}

			return inlinedFormula;
		}
		return formula;
	}

	/**
	 * moves a component in a given direction moves components out of the way if
	 * needed creates grid reactions if the grid expands
	 * 
	 * @param parentComponentID
	 * @param direction
	 */
	protected void moveComponent(String parentComponentID, String childComponentID, Point childLocation, int direction, HashSet<String> reactionsToAdjust)
	{

		HashSet<Integer> newRows = new HashSet<Integer>();
		HashSet<Integer> newCols = new HashSet<Integer>();

		// find the grid bounds
		for (Point location : componentToLocationMap.values())
		{

			if ((int) location.getX() < minRow)
			{
				minRow = (int) location.getX();
			}
			else if ((int) location.getX() > maxRow)
			{
				maxRow = (int) location.getX();
			}
			if ((int) location.getY() < minCol)
			{
				minCol = (int) location.getY();
			}
			else if ((int) location.getY() > maxCol)
			{
				maxCol = (int) location.getY();
			}
		}

		switch (direction)
		{

		case 0:
			childLocation.y -= 1;
			break;
		case 1:
			childLocation.y += 1;
			break;
		case 2:
			childLocation.x -= 1;
			break;
		case 3:
			childLocation.x += 1;
			break;

		case 4:
		{
			childLocation.x += 1;
			childLocation.y -= 1;
			break;
		}
		case 5:
		{
			childLocation.x -= 1;
			childLocation.y += 1;
			break;
		}
		case 6:
		{
			childLocation.x -= 1;
			childLocation.y -= 1;
			break;
		}
		case 7:
		{
			childLocation.x += 1;
			childLocation.y += 1;
			break;
		}
		}

		HashSet<Point> locationsToMove = new HashSet<Point>();

		// if this place is taken, make room by moving the cells in the way
		if (componentToLocationMap.containsValue(childLocation))
		{

			// empty location is the location that needs to become empty so the
			// child can go there
			Point emptyLocation = (Point) childLocation.clone();

			// find all of the locations that are in the way and put them in the
			// hashset
			// this is done my moving in the direction chosen until an empty
			// space is found
			while (componentToLocationMap.containsValue(emptyLocation) == true)
			{

				locationsToMove.add((Point) emptyLocation.clone());

				switch (direction)
				{

				case 0:
					emptyLocation.y -= 1;
					break;
				case 1:
					emptyLocation.y += 1;
					break;
				case 2:
					emptyLocation.x -= 1;
					break;
				case 3:
					emptyLocation.x += 1;
					break;

				case 4:
				{
					emptyLocation.x += 1;
					emptyLocation.y -= 1;
					break;
				}
				case 5:
				{
					emptyLocation.x -= 1;
					emptyLocation.y += 1;
					break;
				}
				case 6:
				{
					emptyLocation.x -= 1;
					emptyLocation.y -= 1;
					break;
				}
				case 7:
				{
					emptyLocation.x += 1;
					emptyLocation.y += 1;
					break;
				}
				}
			}

			LinkedHashMap<String, Point> componentToLocationMapCopy = (LinkedHashMap<String, Point>) componentToLocationMap.clone();

			// move the cells that are in the way
			for (Map.Entry<String, Point> componentAndLocation : componentToLocationMapCopy.entrySet())
			{

				String compID = componentAndLocation.getKey();

				if (locationsToMove.contains(componentAndLocation.getValue()))
				{

					switch (direction)
					{

					case 0:
						componentToLocationMap.get(compID).y -= 1;
						break;
					case 1:
						componentToLocationMap.get(compID).y += 1;
						break;
					case 2:
						componentToLocationMap.get(compID).x -= 1;
						break;
					case 3:
						componentToLocationMap.get(compID).x += 1;
						break;

					case 4:
					{
						componentToLocationMap.get(compID).x += 1;
						componentToLocationMap.get(compID).y -= 1;
						break;
					}
					case 5:
					{
						componentToLocationMap.get(compID).x -= 1;
						componentToLocationMap.get(compID).y += 1;
						break;
					}
					case 6:
					{
						componentToLocationMap.get(compID).x -= 1;
						componentToLocationMap.get(compID).y -= 1;
						break;
					}
					case 7:
					{
						componentToLocationMap.get(compID).x += 1;
						componentToLocationMap.get(compID).y += 1;
						break;
					}
					}

					// keep track of min row/col and max row/col so you know the
					// bounds of the grid
					if ((int) componentToLocationMap.get(compID).getX() < minRow)
					{
						minRow = (int) componentToLocationMap.get(compID).getX();
						newRows.add(minRow);
					}
					else if ((int) componentToLocationMap.get(compID).getX() > maxRow)
					{
						maxRow = (int) componentToLocationMap.get(compID).getX();
						newRows.add(maxRow);
					}
					if ((int) componentToLocationMap.get(compID).getY() < minCol)
					{
						minCol = (int) componentToLocationMap.get(compID).getY();
						newCols.add(minCol);
					}
					else if ((int) componentToLocationMap.get(compID).getY() > maxCol)
					{
						maxCol = (int) componentToLocationMap.get(compID).getY();
						newCols.add(maxCol);
					}
				}
			}
		}

		// now that an empty space has been created (if necessary), put in the
		// child component
		// or move the parent component (if the ID is "" then it's a pure move
		// not a duplication)
		if (childComponentID.equals(""))
		{
			componentToLocationMap.put(parentComponentID, childLocation);
		}
		else
		{
			componentToLocationMap.put(childComponentID, childLocation);
		}

		// keep track of min row/col and max row/col so you know the bounds of
		// the grid
		// this set of ifs is necessary because locationsToMove might be empty
		// (if nothing's in the way)
		if ((int) childLocation.getX() < minRow)
		{
			minRow = (int) childLocation.getX();
			newRows.add(minRow);
		}
		else if ((int) childLocation.getX() > maxRow)
		{
			maxRow = (int) childLocation.getX();
			newRows.add(maxRow);
		}
		if ((int) childLocation.getY() < minCol)
		{
			minCol = (int) childLocation.getY();
			newCols.add(minCol);
		}
		else if ((int) childLocation.getY() > maxCol)
		{
			maxCol = (int) childLocation.getY();
			newCols.add(maxCol);
		}

		HashSet<String> underlyingSpeciesIDs = new HashSet<String>();
		HashSet<String> newGridSpeciesIDs = new HashSet<String>();
		HashMap<String, String> newGridSpeciesIDToOldRowSubstring = new HashMap<String, String>();
		HashMap<String, String> newGridSpeciesIDToOldColSubstring = new HashMap<String, String>();
		HashMap<String, String> newGridSpeciesIDToNewRowSubstring = new HashMap<String, String>();
		HashMap<String, String> newGridSpeciesIDToNewColSubstring = new HashMap<String, String>();

		for (String speciesID : speciesIDSet)
		{

			// find the grid species
			if (speciesID.contains("ROW") && speciesID.contains("COL") && speciesID.contains("__"))
			{
				underlyingSpeciesIDs.add(speciesID.split("__")[1]);
			}
		}

		// if there are new rows or cols added to the grid
		// add new grid species
		if (newRows.size() > 0)
		{

			for (int newRow : newRows)
			{

				// create new grid species for this new row
				for (int col = minCol; col <= maxCol; ++col)
				{

					for (String underlyingSpeciesID : underlyingSpeciesIDs)
					{

						String nonnegRow = Integer.toString(newRow);
						String nonnegCol = Integer.toString(col);

						if (newRow < 0)
						{
							nonnegRow = nonnegRow.replace("-", "_negative_");
						}
						if (col < 0)
						{
							nonnegCol = nonnegCol.replace("-", "_negative_");
						}

						String newID = "ROW" + nonnegRow + "_COL" + nonnegCol + "__" + underlyingSpeciesID;
						String newIDWithNegatives = "ROW" + newRow + "_COL" + col + "__" + underlyingSpeciesID;

						if (model.getSpecies(newID) != null)
						{
							continue;
						}

						newGridSpeciesIDs.add(newIDWithNegatives);
						newGridSpeciesIDToOldRowSubstring.put(newIDWithNegatives, "ROW" + newRow);
						newGridSpeciesIDToOldColSubstring.put(newIDWithNegatives, "COL" + col);
						newGridSpeciesIDToNewRowSubstring.put(newIDWithNegatives, "ROW" + nonnegRow);
						newGridSpeciesIDToNewColSubstring.put(newIDWithNegatives, "COL" + nonnegCol);

						Species gridSpecies = null;

						// find a grid species to take values from
						for (Species species : model.getListOfSpecies())
						{

							if (species.getId().contains("__" + underlyingSpeciesID) && species.getId().contains("ROW") && species.getId().contains("COL"))
							{
								gridSpecies = species;
							}
						}
						if (gridSpecies == null)
						{
							// TODO: Is this possible?
							System.out.println("Could not find grid species");
							return;
						}

						Species newSpecies = gridSpecies.clone();
						newSpecies.setId(newID);
						SBMLutilities.setMetaId(newSpecies, newID);

						// add new grid species to the model (so that altering
						// the kinetic law through jsbml can work)
						model.addSpecies(newSpecies);

						// add a new species to the simulation data structures
						setupSingleSpecies(gridSpecies, newIDWithNegatives);
						variableToValueMap.put(newID.replace("_negative_", "-"), 0);
						speciesToAffectedReactionSetMap.put(newID.replace("_negative_", "-"), new HashSet<String>());
					}
				}
			}
		}

		if (newCols.size() > 0)
		{

			for (int newCol : newCols)
			{

				// create new grid species for this new col
				for (int row = minRow; row <= maxRow; ++row)
				{

					for (String underlyingSpeciesID : underlyingSpeciesIDs)
					{

						String nonnegRow = Integer.toString(row);
						String nonnegCol = Integer.toString(newCol);

						if (row < 0)
						{
							nonnegRow = nonnegRow.replace("-", "_negative_");
						}
						if (newCol < 0)
						{
							nonnegCol = nonnegCol.replace("-", "_negative_");
						}

						String newID = "ROW" + nonnegRow + "_COL" + nonnegCol + "__" + underlyingSpeciesID;
						String newIDWithNegatives = "ROW" + row + "_COL" + newCol + "__" + underlyingSpeciesID;
						newGridSpeciesIDs.add(newIDWithNegatives);
						newGridSpeciesIDToOldRowSubstring.put(newIDWithNegatives, "ROW" + row);
						newGridSpeciesIDToOldColSubstring.put(newIDWithNegatives, "COL" + newCol);
						newGridSpeciesIDToNewRowSubstring.put(newIDWithNegatives, "ROW" + nonnegRow);
						newGridSpeciesIDToNewColSubstring.put(newIDWithNegatives, "COL" + nonnegCol);

						if (model.getSpecies(newID) != null)
						{
							continue;
						}

						Species gridSpecies = null;

						// find a grid species to take values from
						for (Species species : model.getListOfSpecies())
						{

							if (species.getId().contains("__" + underlyingSpeciesID) && species.getId().contains("ROW") && species.getId().contains("COL"))
							{
								gridSpecies = species;
							}
						}
						if (gridSpecies == null)
						{
							// TODO: Is this possible?
							System.out.println("Could not find grid species");
							return;
						}

						Species newSpecies = gridSpecies.clone();
						newSpecies.setId(newID);
						SBMLutilities.setMetaId(newSpecies, newID);

						// add new grid species to the model (so that altering
						// the kinetic law through jsbml can work)
						model.addSpecies(newSpecies);

						// add a new species to the simulation data structures
						setupSingleSpecies(gridSpecies, newIDWithNegatives);
						variableToValueMap.put(newID.replace("_negative_", "-"), 0);
						speciesToAffectedReactionSetMap.put(newID.replace("_negative_", "-"), new HashSet<String>());
					}
				}
			}
		}

		// create new grid diffusion and degradation reactions for the new grid
		// species
		for (String speciesID : newGridSpeciesIDs)
		{

			String newGridSpeciesID = speciesID.replace(newGridSpeciesIDToOldRowSubstring.get(speciesID), newGridSpeciesIDToNewRowSubstring.get(speciesID)).replace(newGridSpeciesIDToOldColSubstring.get(speciesID), newGridSpeciesIDToNewColSubstring.get(speciesID));

			String[] splitID = speciesID.split("_");

			int row = Integer.valueOf(splitID[0].replace("ROW", ""));
			int col = Integer.valueOf(splitID[1].replace("COL", ""));

			ArrayList<Point> neighborLocations = new ArrayList<Point>();

			neighborLocations.add(new Point(row + 1, col)); // right
			neighborLocations.add(new Point(row, col + 1)); // below
			neighborLocations.add(new Point(row - 1, col)); // left
			neighborLocations.add(new Point(row, col - 1)); // above

			String underlyingSpeciesID = speciesID.split("__")[1];
			ASTNode newNode = new ASTNode();

			// find a grid diffusion reaction with this underlying species to
			// take values from
			for (Map.Entry<String, ASTNode> reactionAndFormula : reactionToFormulaMap.entrySet())
			{

				String reactionID = reactionAndFormula.getKey();

				if (reactionID.contains("Diffusion_" + underlyingSpeciesID + "_Below") || reactionID.contains("Diffusion_" + underlyingSpeciesID + "_Above") || reactionID.contains("Diffusion_" + underlyingSpeciesID + "_Left") || reactionID.contains("Diffusion_" + underlyingSpeciesID + "_Right"))
				{

					newNode = reactionAndFormula.getValue().clone();
				}
			}

			int directionIndex = 0;

			for (Point neighborLocation : neighborLocations)
			{

				int nRow = (int) neighborLocation.getX();
				int nCol = (int) neighborLocation.getY();
				String neighborID = "ROW" + nRow + "_COL" + nCol + "__" + underlyingSpeciesID;

				String fdString = "", rvString = "";

				switch (directionIndex)
				{

				case 0:
					fdString = "Below";
					rvString = "Above";
					break;
				case 1:
					fdString = "Right";
					rvString = "Left";
					break;
				case 2:
					fdString = "Above";
					rvString = "Below";
					break;
				case 3:
					fdString = "Left";
					rvString = "Right";
					break;
				}

				// make sure that the neighbor exists (ie, is a species
				// contained on the current grid size)
				if (speciesIDSet.contains(neighborID))
				{

					if (nRow < 0)
					{
						neighborID = neighborID.replace("ROW" + nRow, "ROW" + "_negative_" + (-1 * nRow));
					}
					if (nCol < 0)
					{
						neighborID = neighborID.replace("COL" + nCol, "COL" + "_negative_" + (-1 * nCol));
					}

					// create forward reaction (to the neighbor) if it doesn't
					// exist already
					if (reactionToPropensityMap.containsKey("ROW" + row + "_COL" + col + "_Diffusion_" + underlyingSpeciesID + "_" + fdString) == false)
					{

						// alter kinetic law for forward reaction
						newNode.getRightChild().setVariable(model.getSpecies(newGridSpeciesID));

						String newReactionID = "ROW" + row + "_COL" + col + "_Diffusion_" + underlyingSpeciesID + "_" + fdString;

						if (row < 0)
						{
							newReactionID = newReactionID.replace("ROW" + row, "ROW" + "_negative_" + (-1 * row));
						}
						if (col < 0)
						{
							newReactionID = newReactionID.replace("COL" + col, "COL" + "_negative_" + (-1 * col));
						}

						Reaction fdReaction = model.createReaction(newReactionID);
						KineticLaw fdKineticLaw = model.createKineticLaw();
						fdKineticLaw.setMath(newNode.clone());
						fdReaction.setKineticLaw(fdKineticLaw);
						if (model.getSpecies(newGridSpeciesID) != null)
						{
							SpeciesReference reactant = new SpeciesReference(model.getSpecies(newGridSpeciesID));
							reactant.setStoichiometry(1);
							fdReaction.addReactant(reactant);
						}
						if (model.getSpecies(neighborID) != null)
						{
							SpeciesReference product = new SpeciesReference(model.getSpecies(neighborID));
							product.setStoichiometry(1);
							fdReaction.addProduct(product);
						}
						setupLocalParameters(fdReaction.getKineticLaw(), fdReaction);

						setupSingleReaction(fdReaction.getId(), fdReaction.getKineticLaw().getMath(), false, fdReaction.getListOfReactants(), fdReaction.getListOfProducts(), fdReaction.getListOfModifiers());
					}

					// create the reverse reaction (from the neighbor) if it
					// doesn't already exist
					if (reactionToPropensityMap.containsKey("ROW" + nRow + "_COL" + nCol + "_Diffusion_" + underlyingSpeciesID + "_" + rvString) == false)
					{

						// alter kinetic law for reverse reaction
						newNode.getRightChild().setVariable(model.getSpecies(neighborID));

						String newReactionID = "ROW" + nRow + "_COL" + nCol + "_Diffusion_" + underlyingSpeciesID + "_" + rvString;

						if (nRow < 0)
						{
							newReactionID = newReactionID.replace("ROW" + nRow, "ROW" + "_negative_" + (-1 * nRow));
						}
						if (nCol < 0)
						{
							newReactionID = newReactionID.replace("COL" + nCol, "COL" + "_negative_" + (-1 * nCol));
						}

						// create reverse reaction
						Reaction rvReaction = model.createReaction(newReactionID);
						KineticLaw rvKineticLaw = model.createKineticLaw();
						rvKineticLaw.setMath(newNode.clone());
						rvReaction.setKineticLaw(rvKineticLaw);
						if (model.getSpecies(neighborID) != null)
						{
							SpeciesReference reactant = new SpeciesReference(model.getSpecies(neighborID));
							reactant.setStoichiometry(1);
							rvReaction.addReactant(reactant);
						}
						if (model.getSpecies(newGridSpeciesID) != null)
						{
							SpeciesReference product = new SpeciesReference(model.getSpecies(newGridSpeciesID));
							product.setStoichiometry(1);
							rvReaction.addProduct(product);
						}
						setupLocalParameters(rvReaction.getKineticLaw(), rvReaction);

						setupSingleReaction(rvReaction.getId(), rvReaction.getKineticLaw().getMath(), false, rvReaction.getListOfReactants(), rvReaction.getListOfProducts(), rvReaction.getListOfModifiers());
					}
				}

				++directionIndex;
			}

			ASTNode degradationNode = new ASTNode();

			// for (Map.Entry<String, ASTNode> reactionAndFormula :
			// reactionToFormulaMap.entrySet()) {
			//
			// String reactionID = reactionAndFormula.getKey();
			// }

			Boolean isDegradable = false;

			// create degradation reaction for each grid species
			// find a grid degradation reaction to copy from
			for (Map.Entry<String, ASTNode> reactionAndFormula : reactionToFormulaMap.entrySet())
			{

				String reactionID = reactionAndFormula.getKey();

				if (reactionID.contains(GlobalConstants.DEGRADATION + "_" + underlyingSpeciesID))
				{

					degradationNode = reactionAndFormula.getValue().clone();
					degradationNode.getRightChild().setVariable(model.getSpecies(newGridSpeciesID));
					isDegradable = true;
					break;
				}
			}

			if (isDegradable)
			{

				String newDegReactionID = "ROW" + row + "_COL" + col + "_" + GlobalConstants.DEGRADATION + "_" + underlyingSpeciesID;

				if (row < 0)
				{
					newDegReactionID = newDegReactionID.replace("ROW" + row, "ROW" + "_negative_" + (-1 * row));
				}
				if (col < 0)
				{
					newDegReactionID = newDegReactionID.replace("COL" + col, "COL" + "_negative_" + (-1 * col));
				}

				if (reactionToPropensityMap.containsKey(newDegReactionID) == false)
				{

					Reaction degReaction = model.createReaction(newDegReactionID);
					KineticLaw degKineticLaw = model.createKineticLaw();
					degKineticLaw.setMath(degradationNode.clone());
					degReaction.setKineticLaw(degKineticLaw);
					if (model.getSpecies(newGridSpeciesID) != null)
					{
						SpeciesReference reactant = new SpeciesReference(model.getSpecies(newGridSpeciesID));
						reactant.setStoichiometry(1);
						degReaction.addReactant(reactant);
					}
					setupLocalParameters(degReaction.getKineticLaw(), degReaction);
					setupSingleReaction(degReaction.getId(), degReaction.getKineticLaw().getMath(), false, degReaction.getListOfReactants(), degReaction.getListOfProducts(), degReaction.getListOfModifiers());
				}
			}
		}

		// MOVE MEMBRANE DIFFUSION REACTIONS FOR COMPONENTS THAT HAVE MOVED
		if (locationsToMove.size() > 0)
		{

			for (Point locationToMove : locationsToMove)
			{

				// adjust these locations to their new, moved location
				switch (direction)
				{

				case 0:
					locationToMove.y -= 1;
					break;
				case 1:
					locationToMove.y += 1;
					break;
				case 2:
					locationToMove.x -= 1;
					break;
				case 3:
					locationToMove.x += 1;
					break;

				case 4:
				{
					locationToMove.x += 1;
					locationToMove.y -= 1;
					break;
				}
				case 5:
				{
					locationToMove.x -= 1;
					locationToMove.y += 1;
					break;
				}
				case 6:
				{
					locationToMove.x -= 1;
					locationToMove.y -= 1;
					break;
				}
				case 7:
				{
					locationToMove.x += 1;
					locationToMove.y += 1;
					break;
				}
				}
			}

			// find the membrane diffusion reactions for these moved components
			// and alter it
			for (String compID : componentToLocationMap.keySet())
			{

				int compX = (int) componentToLocationMap.get(compID).getX();
				int compY = (int) componentToLocationMap.get(compID).getY();

				for (Point locationToMove : locationsToMove)
				{

					if (locationToMove.x == compX && locationToMove.y == compY)
					{

						if (componentToReactionSetMap.get(compID) == null)
						{
							continue;
						}

						for (String reactionID : componentToReactionSetMap.get(compID))
						{

							// only need to change the rv membrane diffusion
							// reaction
							if (reactionID.contains("MembraneDiffusion"))
							{

								ASTNode formulaNode = reactionToFormulaMap.get(reactionID);

								// the right child is the one to alter
								ASTNode speciesNode = formulaNode.getRightChild();

								Point oldLocation = (Point) locationToMove.clone();

								switch (direction)
								{

								case 0:
									oldLocation.y = locationToMove.y + 1;
									break;
								case 1:
									oldLocation.y = locationToMove.y - 1;
									break;
								case 2:
									oldLocation.x = locationToMove.x + 1;
									break;
								case 3:
									oldLocation.x = locationToMove.x - 1;
									break;

								case 4:
								{
									oldLocation.x = locationToMove.x - 1;
									oldLocation.y = locationToMove.y + 1;
									break;
								}
								case 5:
								{
									oldLocation.x = locationToMove.x + 1;
									oldLocation.y = locationToMove.y - 1;
									break;
								}
								case 6:
								{
									oldLocation.x = locationToMove.x + 1;
									oldLocation.y = locationToMove.y + 1;
									break;
								}
								case 7:
								{
									oldLocation.x = locationToMove.x - 1;
									oldLocation.y = locationToMove.y - 1;
									break;
								}
								}

								String oldRowCol = "ROW" + oldLocation.x + "_COL" + oldLocation.y;
								oldRowCol = oldRowCol.replace("ROW-", "ROW_negative_");
								oldRowCol = oldRowCol.replace("COL-", "COL_negative_");

								String newRowCol = "ROW" + locationToMove.x + "_COL" + locationToMove.y;
								newRowCol = newRowCol.replace("ROW-", "ROW_negative_");
								newRowCol = newRowCol.replace("COL-", "COL_negative_");

								// adjust kinetic law
								speciesNode.setVariable(model.getSpecies(speciesNode.getName().replace(oldRowCol, newRowCol)));

								newRowCol = newRowCol.replace("ROW_negative_", "ROW-");
								newRowCol = newRowCol.replace("COL_negative_", "COL-");
								oldRowCol = oldRowCol.replace("ROW_negative_", "ROW-");
								oldRowCol = oldRowCol.replace("COL_negative_", "COL-");

								// adjust reactants/products
								for (StringDoublePair speciesAndStoichiometry : reactionToSpeciesAndStoichiometrySetMap.get(reactionID))
								{

									speciesAndStoichiometry.string = speciesAndStoichiometry.string.replace(oldRowCol, newRowCol);
								}

								for (StringDoublePair reactantAndStoichiometry : reactionToReactantStoichiometrySetMap.get(reactionID))
								{

									reactantAndStoichiometry.string = reactantAndStoichiometry.string.replace(oldRowCol, newRowCol);
								}

								// remove the old, now out-dated species to
								// affected reaction pairs
								// put in the new up-to-date pairs
								for (String speciesID : speciesToAffectedReactionSetMap.keySet())
								{

									if (speciesID.contains(oldRowCol + "_"))
									{

										HashSet<String> reactionsToRemove = new HashSet<String>();

										for (String reaction : speciesToAffectedReactionSetMap.get(speciesID))
										{
											if (reaction.contains("MembraneDiffusion"))
											{
												reactionsToRemove.add(reaction);
											}
										}

										for (String reactionToRemove : reactionsToRemove)
										{
											speciesToAffectedReactionSetMap.get(speciesID).remove(reactionToRemove);
										}
									}

									if (speciesID.contains(newRowCol + "_"))
									{

										speciesToAffectedReactionSetMap.get(speciesID).add(reactionID);
									}
								}

								// adjust propensity
								reactionsToAdjust.add(reactionID);
							}
						}
					}
				}
			}
		}
	}

	protected void performAssignmentRules()
	{
		boolean changed = true;

		while (changed)
		{
			changed = false;

			for (AssignmentRule assignmentRule : listOfAssignmentRules)
			{
				String variable = assignmentRule.getVariable();

				// update the species count (but only if the species isn't
				// constant)
				// (bound cond is fine)
				if (variableToIsConstantMap.containsKey(variable) && variableToIsConstantMap.get(variable) == false || variableToIsConstantMap.containsKey(variable) == false)
				{

					if (speciesToHasOnlySubstanceUnitsMap.containsKey(variable) && speciesToHasOnlySubstanceUnitsMap.get(variable) == false)
					{

						double oldValue = variableToValueMap.get(variable);
						double newValue = evaluateExpressionRecursive(assignmentRule.getMath());

						if (oldValue != newValue)
						{
							variableToValueMap.put(variable, newValue * variableToValueMap.get(speciesToCompartmentNameMap.get(variable)));
							changed = true;
						}
					}
					else
					{

						double oldValue = variableToValueMap.get(variable);
						double newValue = evaluateExpressionRecursive(assignmentRule.getMath());
						if (oldValue != newValue)
						{
							variableToValueMap.put(variable, newValue);
							changed = true;
						}
					}

				}
			}
		}
	}

	/**
	 * performs assignment rules that may have changed due to events or
	 * reactions firing
	 * 
	 * @param affectedAssignmentRuleSet
	 *            the set of assignment rules that have been affected
	 */
	protected HashSet<String> performAssignmentRules(HashSet<AssignmentRule> affectedAssignmentRuleSet)
	{

		HashSet<String> affectedVariables = new HashSet<String>();

		for (AssignmentRule assignmentRule : affectedAssignmentRuleSet)
		{

			String variable = assignmentRule.getVariable();

			// update the species count (but only if the species isn't constant)
			// (bound cond is fine)
			if (variableToIsConstantMap.containsKey(variable) && variableToIsConstantMap.get(variable) == false || variableToIsConstantMap.containsKey(variable) == false)
			{

				if (speciesToHasOnlySubstanceUnitsMap.containsKey(variable) && speciesToHasOnlySubstanceUnitsMap.get(variable) == false)
				{

					variableToValueMap.put(variable, evaluateExpressionRecursive(assignmentRule.getMath()) * variableToValueMap.get(speciesToCompartmentNameMap.get(variable)));
				}
				else
				{
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
	protected HashSet<String> performRateRules(double delta_t)
	{

		HashSet<String> affectedVariables = new HashSet<String>();

		for (Rule rule : model.getListOfRules())
		{

			if (rule.isRate())
			{

				RateRule rateRule = (RateRule) rule;
				String variable = rateRule.getVariable();

				// update the species count (but only if the species isn't
				// constant) (bound cond is fine)
				if (variableToIsConstantMap.containsKey(variable) && variableToIsConstantMap.get(variable) == false)
				{

					if (speciesToHasOnlySubstanceUnitsMap.containsKey(variable) && speciesToHasOnlySubstanceUnitsMap.get(variable) == false)
					{

						variableToValueMap.adjustValue(variable, delta_t * (evaluateExpressionRecursive(rateRule.getMath()) * variableToValueMap.get(speciesToCompartmentNameMap.get(variable))));
					}
					else
					{
						variableToValueMap.adjustValue(variable, delta_t * evaluateExpressionRecursive(rateRule.getMath()));
					}

					affectedVariables.add(variable);
				}
			}
		}

		return affectedVariables;
	}

	/**
	 * updates reactant/product species counts based on their stoichiometries
	 * 
	 * @param selectedReactionID
	 *            the reaction to perform
	 */
	protected void performReaction(String selectedReactionID, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag)
	{

		// these are sets of things that need to be re-evaluated or tested due
		// to the reaction firing
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		// loop through the reaction's reactants and products and update their
		// amounts
		for (StringDoublePair speciesAndStoichiometry : reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID))
		{

			double stoichiometry = speciesAndStoichiometry.doub;
			String speciesID = speciesAndStoichiometry.string;

			// this means the stoichiometry isn't constant, so look to the
			// variableToValue map
			if (reactionToNonconstantStoichiometriesSetMap.containsKey(selectedReactionID))
			{

				for (StringStringPair doubleID : reactionToNonconstantStoichiometriesSetMap.get(selectedReactionID))
				{

					// string1 is the species ID; string2 is the
					// speciesReference ID
					if (doubleID.string1.equals(speciesID))
					{

						stoichiometry = variableToValueMap.get(doubleID.string2);

						// this is to get the plus/minus correct, as the
						// variableToValueMap has
						// a stoichiometry without the reactant/product
						// plus/minus data
						stoichiometry *= (int) (speciesAndStoichiometry.doub / Math.abs(speciesAndStoichiometry.doub));
						break;
					}
				}
			}

			// update the species count if the species isn't a boundary
			// condition or constant
			// note that the stoichiometries are earlier modified with the
			// correct +/- sign
			if (speciesToIsBoundaryConditionMap.get(speciesID) == false && variableToIsConstantMap.get(speciesID) == false)
			{

				if (speciesToConversionFactorMap.containsKey(speciesID))
				{
					variableToValueMap.adjustValue(speciesID, stoichiometry * variableToValueMap.get(speciesToConversionFactorMap.get(speciesID)));
				}
				else
				{
					variableToValueMap.adjustValue(speciesID, stoichiometry);
				}
			}

			// if this variable that was just updated is part of an assignment
			// rule (RHS)
			// then re-evaluate that assignment rule
			if (noAssignmentRulesFlag == false && variableToIsInAssignmentRuleMap.get(speciesID) == true)
			{
				affectedAssignmentRuleSet.addAll(variableToAffectedAssignmentRuleSetMap.get(speciesID));
			}

			if (noConstraintsFlag == false && variableToIsInConstraintMap.get(speciesID) == true)
			{
				affectedConstraintSet.addAll(variableToAffectedConstraintSetMap.get(speciesID));
			}
		}

		if (affectedAssignmentRuleSet.size() > 0)
		{
			performAssignmentRules(affectedAssignmentRuleSet);
		}

		if (affectedConstraintSet.size() > 0)
		{

			if (testConstraints(affectedConstraintSet) == false)
			{
				constraintFailureFlag = true;
			}
		}
	}

	/**
	 * recursively finds all variable nodes and prepends a string to the
	 * variable static version
	 * 
	 * @param node
	 * @param toPrepend
	 */
	private static void prependToVariableNodes(ASTNode node, String toPrepend, Model model)
	{

		if (node.isName())
		{

			// only prepend to species and parameters
			if (model.getSpecies(toPrepend + node.getName()) != null)
			{
				node.setVariable(model.getSpecies(toPrepend + node.getName()));
			}
			else if (model.getParameter(toPrepend + node.getName()) != null)
			{
				node.setVariable(model.getParameter(toPrepend + node.getName()));
			}
		}
		else
		{
			for (ASTNode childNode : node.getChildren())
			{
				prependToVariableNodes(childNode, toPrepend, model);
			}
		}
	}

	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException
	 */
	protected void printToTSD(double printTime) throws IOException
	{

		String commaSpace = "";

		// dynamic printing requires re-printing the species values each time
		// step
		if (dynamicBoolean == true)
		{

			bufferedTSDWriter.write("(\"time\"");

			commaSpace = ",";

			// if there's an interesting species, only those get printed
			if (interestingSpecies.size() > 0)
			{

				for (String speciesID : interestingSpecies)
				{
					bufferedTSDWriter.write(commaSpace + "\"" + speciesID + "\"");
				}

				// always print compartment location IDs
				for (String componentLocationID : componentToLocationMap.keySet())
				{

					String locationX = componentLocationID + "__locationX";
					String locationY = componentLocationID + "__locationY";

					bufferedTSDWriter.write(commaSpace + "\"" + locationX + "\", \"" + locationY + "\"");
				}
			}
			else
			{

				// print the species IDs
				for (String speciesID : speciesIDSet)
				{
					bufferedTSDWriter.write(commaSpace + "\"" + speciesID + "\"");
				}

				// print compartment location IDs
				for (String componentLocationID : componentToLocationMap.keySet())
				{

					String locationX = componentLocationID + "__locationX";
					String locationY = componentLocationID + "__locationY";

					bufferedTSDWriter.write(commaSpace + "\"" + locationX + "\", \"" + locationY + "\"");
				}

				// print compartment IDs (for sizes)
				for (String componentID : compartmentIDSet)
				{

					try
					{
						bufferedTSDWriter.write(", \"" + componentID + "\"");
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				// print nonconstant parameter IDs
				for (String parameterID : nonconstantParameterIDSet)
				{

					try
					{
						bufferedTSDWriter.write(", \"" + parameterID + "\"");
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}

			bufferedTSDWriter.write("),\n");
		}

		bufferedTSDWriter.write("(");

		commaSpace = "";

		// print the current time
		bufferedTSDWriter.write(printTime + ",");

		// if there's an interesting species, only those get printed
		if (interestingSpecies.size() > 0)
		{

			for (String speciesID : interestingSpecies)
			{

				if (printConcentrations == true)
				{

					bufferedTSDWriter.write(commaSpace + (variableToValueMap.get(speciesID) / variableToValueMap.get(speciesToCompartmentNameMap.get(speciesID))));
				}
				else
				{
					bufferedTSDWriter.write(commaSpace + variableToValueMap.get(speciesID));
				}

				commaSpace = ",";
			}

			// always print component location values
			for (String componentID : componentToLocationMap.keySet())
			{
				bufferedTSDWriter.write(commaSpace + (int) componentToLocationMap.get(componentID).getX());
				bufferedTSDWriter.write(commaSpace + (int) componentToLocationMap.get(componentID).getY());
			}
		}
		else
		{

			// loop through the speciesIDs and print their current value to the
			// file
			for (String speciesID : speciesIDSet)
			{

				bufferedTSDWriter.write(commaSpace + variableToValueMap.get(speciesID));
				commaSpace = ",";
			}

			// print component location values
			for (String componentID : componentToLocationMap.keySet())
			{
				bufferedTSDWriter.write(commaSpace + (int) componentToLocationMap.get(componentID).getX());
				commaSpace = ",";
				bufferedTSDWriter.write(commaSpace + (int) componentToLocationMap.get(componentID).getY());
			}

			// print compartment sizes
			for (String componentID : compartmentIDSet)
			{
				bufferedTSDWriter.write(commaSpace + variableToValueMap.get(componentID));
				commaSpace = ",";
			}

			// print nonconstant parameter values
			for (String parameterID : nonconstantParameterIDSet)
			{
				bufferedTSDWriter.write(commaSpace + variableToValueMap.get(parameterID));
				commaSpace = ",";
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
	@Override
	public void printStatisticsTSD()
	{

		// the last run is the number of runs
		int numRuns = currentRun;

		HashMap<String, ArrayList<DescriptiveStatistics>> speciesStatistics = new HashMap<String, ArrayList<DescriptiveStatistics>>();

		ArrayList<String> allSpecies = new ArrayList<String>();
		HashSet<String> speciesSet = new HashSet<String>();

		// if it's dynamic, we have to parse every run's file to get the full
		// list of species
		if (dynamicBoolean == true)
		{

			for (int run = 1; run <= numRuns; ++run)
			{

				DTSDParser dtsdParser = new DTSDParser(outputDirectory + "run-" + run + ".dtsd");
				speciesSet.addAll(dtsdParser.getSpecies());
			}

			speciesSet.remove("time");
			allSpecies.add("time");
			allSpecies.addAll(speciesSet);
		}

		// store the TSD data for analysis
		for (int run = 1; run <= numRuns; ++run)
		{

			DTSDParser dtsdParser = null;
			TSDParser tsdParser = null;
			HashMap<String, ArrayList<Double>> runStatistics = null;

			if (dynamicBoolean == true)
			{

				dtsdParser = new DTSDParser(outputDirectory + "run-" + run + ".dtsd");
				// allSpecies = dtsdParser.getSpecies();
				runStatistics = dtsdParser.getHashMap(allSpecies);
			}
			else
			{
				tsdParser = new TSDParser(outputDirectory + "run-" + run + ".tsd", false);
				allSpecies = tsdParser.getSpecies();
				runStatistics = tsdParser.getHashMap();
			}

			for (int speciesIndex = 0; speciesIndex < allSpecies.size(); ++speciesIndex)
			{

				String species = allSpecies.get(speciesIndex);

				for (int index = 0; index < runStatistics.get(species).size(); ++index)
				{

					Double speciesData = runStatistics.get(species).get(index);

					if (speciesStatistics.size() <= speciesIndex)
					{
						speciesStatistics.put(species, new ArrayList<DescriptiveStatistics>());
					}

					if (speciesStatistics.get(species).size() <= index)
					{
						speciesStatistics.get(species).add(new DescriptiveStatistics());
					}

					speciesStatistics.get(species).get(index).addValue(speciesData.doubleValue());
				}
			}
		}

		DataParser statsParser = new DataParser(null, null);
		ArrayList<ArrayList<Double>> meanTSDData = new ArrayList<ArrayList<Double>>();

		// calculate and print the mean tsd
		for (String species : allSpecies)
		{

			ArrayList<Double> speciesData = new ArrayList<Double>();

			for (int index = 0; index < speciesStatistics.get(species).size(); ++index)
			{
				speciesData.add(speciesStatistics.get(species).get(index).getMean());
			}

			meanTSDData.add(speciesData);
		}

		statsParser.setData(meanTSDData);
		statsParser.setSpecies(allSpecies);
		statsParser.outputTSD(outputDirectory + "mean.tsd");

		// calculate and print the standard deviation tsd
		ArrayList<ArrayList<Double>> standardDeviationTSDData = new ArrayList<ArrayList<Double>>();

		for (String species : allSpecies)
		{

			ArrayList<Double> speciesData = new ArrayList<Double>();

			if (species.equals("time"))
			{

				for (int index = 0; index < speciesStatistics.get(species).size(); ++index)
				{
					speciesData.add(speciesStatistics.get(species).get(index).getMean());
				}
			}
			else
			{

				for (int index = 0; index < speciesStatistics.get(species).size(); ++index)
				{
					speciesData.add(speciesStatistics.get(species).get(index).getStandardDeviation());
				}
			}

			standardDeviationTSDData.add(speciesData);
		}

		statsParser.setData(standardDeviationTSDData);
		statsParser.setSpecies(allSpecies);
		statsParser.outputTSD(outputDirectory + "standard_deviation.tsd");

		// calculate and print the variance tsd
		ArrayList<ArrayList<Double>> varianceTSDData = new ArrayList<ArrayList<Double>>();

		for (String species : allSpecies)
		{

			ArrayList<Double> speciesData = new ArrayList<Double>();

			if (species.equals("time"))
			{

				for (int index = 0; index < speciesStatistics.get(species).size(); ++index)
				{
					speciesData.add(speciesStatistics.get(species).get(index).getMean());
				}
			}
			else
			{

				for (int index = 0; index < speciesStatistics.get(species).size(); ++index)
				{
					speciesData.add(speciesStatistics.get(species).get(index).getVariance());
				}
			}

			varianceTSDData.add(speciesData);
		}

		statsParser.setData(varianceTSDData);
		statsParser.setSpecies(allSpecies);
		statsParser.outputTSD(outputDirectory + "variance.tsd");
	}

	/**
	 * reverts the model back to its pre-dynamic state (during duplication
	 * events, the model is altered because of jsbml restrictions)
	 */
	protected void resetModel()
	{

		model.setListOfSpecies(initialSpecies.clone());
		model.setListOfReactions(initialReactions.clone());
		model.setListOfEvents(initialEvents.clone());
		model.setListOfParameters(initialParameters.clone());
		model.setListOfCompartments(initialCompartments.clone());
	}

	/**
	 * non-static version of the method that flattens arrays into the sbml model
	 * this one doesn't print the model back out, though
	 */
	protected void setupArrays()
	{

		boolean arraysExist = false;

		// ARRAYED SPECIES BUSINESS
		// create all new species that are implicit in the arrays and put them
		// into the model

		ArrayList<Species> speciesToAdd = new ArrayList<Species>();
		ArrayList<String> speciesToRemove = new ArrayList<String>();

		HashMap<String, Boolean> speciesToIsArrayedMap = new HashMap<String, Boolean>();
		HashMap<String, SpeciesDimensions> arrayedSpeciesToDimensionsMap = new HashMap<String, SpeciesDimensions>();

		try
		{
			for (Species species : model.getListOfSpecies())
			{

				String speciesID = species.getId();

				// check to see if the species is arrayed

				if (AnnotationUtility.parseSpeciesArrayAnnotation(species) != null)
				{

					arraysExist = true;

					speciesToIsArrayedMap.put(speciesID, true);
					speciesToRemove.add(speciesID);

					int numRowsLower = 0;
					int numColsLower = 0;
					int numRowsUpper = 0;
					int numColsUpper = 0;

					// String[] annotationString = stripAnnotation(
					// species.getAnnotationString().replace("<annotation>", "")
					// .replace("</annotation>", "").trim()).split("=");
					int[] values = AnnotationUtility.parseSpeciesArrayAnnotation(species);

					numRowsLower = values[0];
					numColsLower = values[1];
					numRowsUpper = values[2];
					numColsUpper = values[3];

					SpeciesDimensions speciesDimensions = new SpeciesDimensions(numRowsLower, numRowsUpper, numColsLower, numColsUpper);

					arrayedSpeciesToDimensionsMap.put(speciesID, speciesDimensions);

					// loop through all species in the array
					// prepend the row/col information to create a new ID
					for (int row = numRowsLower; row <= numRowsUpper; ++row)
					{
						for (int col = numColsLower; col <= numColsUpper; ++col)
						{

							speciesID = "ROW" + row + "_COL" + col + "__" + species.getId();

							Species newSpecies = new Species();
							newSpecies = species.clone();
							SBMLutilities.setMetaId(newSpecies, speciesID);
							newSpecies.setId(speciesID);
							newSpecies.setAnnotation(new Annotation());
							speciesToAdd.add(newSpecies);
						}
					}
				}
				else
				{
					speciesToIsArrayedMap.put(speciesID, false);
				}
			} // end species for loop

			// add new row/col species to the model
			for (Species species : speciesToAdd)
			{
				model.addSpecies(species);
			}

			// ARRAYED EVENTS BUSINESS

			ArrayList<String> eventsToRemove = new ArrayList<String>();
			ArrayList<Event> eventsToAdd = new ArrayList<Event>();

			for (Event event : model.getListOfEvents())
			{

				if (stripAnnotation(event.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim()).contains("array"))
				{

					arraysExist = true;

					eventsToRemove.add(event.getId());

					String annotationString = stripAnnotation(event.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim()).replace("<annotation>", "").replace("</annotation>", "").replace("\"", "");
					String[] splitAnnotation = annotationString.split("array:");
					ArrayList<String> eventCompartments = new ArrayList<String>();

					splitAnnotation[splitAnnotation.length - 2] = splitAnnotation[splitAnnotation.length - 2].split("xmlns:")[0];

					for (int i = 2; i < splitAnnotation.length; ++i)
					{

						String compartmentID = splitAnnotation[i].split("=")[0];
						eventCompartments.add(compartmentID);
					}

					// loop through all compartments and create an event for
					// each one
					for (String compartmentID : eventCompartments)
					{

						Event newEvent = new Event();
						newEvent.setVersion(event.getVersion());
						newEvent.setLevel(event.getLevel());
						newEvent.setId(compartmentID + "__" + event.getId());
						SBMLutilities.setMetaId(newEvent, compartmentID + "__" + event.getId());
						event.getTrigger().getMath().updateVariables();
						newEvent.setTrigger(event.getTrigger().clone());

						// at this point, the formula has something like
						// neighborQuantity(Species1)
						// this needs to become neighborQuantity(Species1,
						// CompartmentLocationX(Comp1),
						// CompartmentLocationY(Comp1))
						if (newEvent.getTrigger().getMath().toFormula().contains("neighborQuantity"))
						{

							String triggerMath = newEvent.getTrigger().getMath().toFormula();
							ArrayList<ASTNode> nqNodes = new ArrayList<ASTNode>();

							this.getSatisfyingNodesLax(newEvent.getTrigger().getMath(), "neighborQuantity", nqNodes);

							// loop through all neighbor quantity nodes in the
							// trigger formula
							for (ASTNode nqNode : nqNodes)
							{

								String direction = "";

								if (triggerMath.contains("QuantityLeft"))
								{
									direction = "Left";
								}
								else if (triggerMath.contains("QuantityRight"))
								{
									direction = "Right";
								}
								else if (triggerMath.contains("QuantityAbove"))
								{
									direction = "Above";
								}
								else
								{
									direction = "Below";
								}

								String speciesID = nqNode.toFormula().split("neighborQuantity" + direction)[1].replace("(", "").replace(")", "");

								try
								{
									ASTNode newFormula = ASTNode.parseFormula("neighborQuantity" + direction + "Full(" + compartmentID + "__" + speciesID + ", getCompartmentLocationX(" + compartmentID + "__Cell" + "), getCompartmentLocationY(" + compartmentID + "__Cell" + "))");

									for (int i = 0; i < ((ASTNode) nqNode.getParent()).getChildCount(); ++i)
									{

										if (((ASTNode) nqNode.getParent().getChildAt(i)).isFunction() && ((ASTNode) nqNode.getParent().getChildAt(i)).getVariable().toString().contains("neighborQuantity" + direction))
										{

											((ASTNode) nqNode.getParent()).replaceChild(i, newFormula);
											break;
										}
									}
								}
								catch (ParseException e)
								{
									e.printStackTrace();
								}
							}
						}

						if (event.isSetPriority())
						{
							newEvent.setPriority(event.getPriority().clone());
						}

						if (event.isSetDelay())
						{
							newEvent.setDelay(event.getDelay().clone());
						}

						newEvent.setUseValuesFromTriggerTime(event.getUseValuesFromTriggerTime());

						for (EventAssignment eventAssignment : event.getListOfEventAssignments())
						{

							EventAssignment ea = eventAssignment.clone();
							ea.setMath(eventAssignment.getMath().clone());
							ea.setVariable(eventAssignment.getVariable());
							newEvent.addEventAssignment(ea);
						}

						for (EventAssignment eventAssignment : newEvent.getListOfEventAssignments())
						{

							eventAssignment.setVariable(compartmentID + "__" + eventAssignment.getVariable());

							// prepends the compartment ID to all variables in
							// the event assignment
							prependToVariableNodes(eventAssignment.getMath(), compartmentID + "__", model);
						}

						eventsToAdd.add(newEvent);
					}
				}
			}

			for (Event eventToAdd : eventsToAdd)
			{
				model.addEvent(eventToAdd);
			}

			// ARRAYED REACTION BUSINESS
			// if a reaction has arrayed species
			// new reactions that are implicit are created and added to the
			// model

			ArrayList<Reaction> reactionsToAdd = new ArrayList<Reaction>();
			ArrayList<String> reactionsToRemove = new ArrayList<String>();

			for (Reaction reaction : model.getListOfReactions())
			{

				String reactionID = reaction.getId();

				ArrayList<Integer> membraneDiffusionRows = new ArrayList<Integer>();
				ArrayList<Integer> membraneDiffusionCols = new ArrayList<Integer>();
				ArrayList<String> membraneDiffusionCompartments = new ArrayList<String>();

				// MEMBRANE DIFFUSION REACTIONS
				// if it's a membrane diffusion reaction it'll have the
				// appropriate locations as an annotation
				// so parse them and store them in the above arraylists
				if (reactionID.contains("MembraneDiffusion"))
				{

					arraysExist = true;

					String[] splitAnnotation = AnnotationUtility.parseArrayAnnotation(reaction);
					for (int i = 1; i < splitAnnotation.length; i++)
					{
						String compartmentID = splitAnnotation[i].split("=")[0];
						String row = splitAnnotation[i].split(" ")[0].split("=")[1].split(",")[0].replace("(", "");
						String col = splitAnnotation[i].split(" ")[0].split("=")[1].split(",")[1].replace(")", "");

						membraneDiffusionRows.add(Integer.valueOf(row.trim()));
						membraneDiffusionCols.add(Integer.valueOf(col.trim()));
						membraneDiffusionCompartments.add(compartmentID);
					}

					int membraneDiffusionIndex = 0;

					reactionsToRemove.add(reaction.getId());
					reaction.setAnnotation(new Annotation());

					// loop through all appropriate row/col pairs and create a
					// membrane diffusion reaction for each one
					for (String compartmentID : membraneDiffusionCompartments)
					{

						int row = membraneDiffusionRows.get(membraneDiffusionIndex);
						int col = membraneDiffusionCols.get(membraneDiffusionIndex);

						// create a new reaction and set the ID
						Reaction newReaction = new Reaction();
						newReaction = reaction.clone();
						newReaction.setListOfReactants(new ListOf<SpeciesReference>());
						newReaction.setListOfProducts(new ListOf<SpeciesReference>());
						newReaction.setListOfModifiers(new ListOf<ModifierSpeciesReference>());
						newReaction.setId(compartmentID + "__" + reactionID);
						SBMLutilities.setMetaId(newReaction, compartmentID + "__" + reactionID);
						newReaction.setReversible(true);
						newReaction.setFast(false);
						newReaction.setCompartment(reaction.getCompartment());

						// alter the kinetic law to so that it has the correct
						// indexes as children for the
						// get2DArrayElement function
						// get the nodes to alter (that are arguments for the
						// get2DArrayElement method)
						ArrayList<ASTNode> get2DArrayElementNodes = new ArrayList<ASTNode>();

						getSatisfyingNodes(newReaction.getKineticLaw().getMath(), "get2DArrayElement", get2DArrayElementNodes);

						boolean reactantBool = false;

						// replace the get2darrayelement stuff with the proper
						// explicit species/parameters
						for (ASTNode node : get2DArrayElementNodes)
						{

							if (node.getLeftChild().getName().contains("kmdiff"))
							{

								String parameterName = node.getLeftChild().getName();

								// see if the species-specific one exists
								// if it doesn't, use the default
								// you'll need to parse the species name from
								// the reaction id, probably

								String speciesID = reactionID.replace("MembraneDiffusion_", "");

								if (model.getParameter(compartmentID + "__" + speciesID + "__" + parameterName) == null)
								{
									node.setVariable(model.getParameter(parameterName));
								}
								else
								{
									node.setVariable(model.getParameter(compartmentID + "__" + speciesID + "__" + parameterName));
								}

								for (int i = 0; i < node.getChildCount(); ++i)
								{
									node.removeChild(i);
								}

								for (int i = 0; i < node.getChildCount(); ++i)
								{
									node.removeChild(i);
								}
							}
							// this means it's a species, which we need to
							// prepend with the row/col prefix
							else
							{

								if (node.getChildCount() > 0 && model.getParameter(node.getLeftChild().getName()) == null)
								{

									// reactant
									if (reactantBool == true)
									{

										node.setVariable(model.getSpecies("ROW" + row + "_COL" + col + "__" + node.getLeftChild().getName()));
									}
									// product
									else
									{

										node.setVariable(model.getSpecies(compartmentID + "__" + node.getLeftChild().getName()));
										reactantBool = true;
									}
								}

								for (int i = 0; i < node.getChildCount(); ++i)
								{
									node.removeChild(i);
								}

								for (int i = 0; i < node.getChildCount(); ++i)
								{
									node.removeChild(i);
								}
							}
						}

						// loop through reactants
						for (SpeciesReference reactant : reaction.getListOfReactants())
						{

							// create a new reactant and add it to the new
							// reaction
							SpeciesReference newReactant = new SpeciesReference();
							newReactant = reactant.clone();
							newReactant.setSpecies(compartmentID + "__" + newReactant.getSpecies());
							newReactant.setAnnotation(new Annotation());
							newReaction.addReactant(newReactant);
						}

						// loop through products
						for (SpeciesReference product : reaction.getListOfProducts())
						{

							// create a new reactant and add it to the new
							// reaction
							SpeciesReference newProduct = new SpeciesReference();
							newProduct = product.clone();
							newProduct.setSpecies("ROW" + row + "_COL" + col + "__" + newProduct.getSpecies());
							newProduct.setAnnotation(new Annotation());
							newReaction.addProduct(newProduct);
						}

						boolean i = false, j = false;

						for (LocalParameter lp : newReaction.getKineticLaw().getListOfLocalParameters())
						{

							if (lp.getId().equals("i"))
							{
								i = true;
							}
							else if (lp.getId().equals("j"))
							{
								j = true;
							}
						}

						if (i)
						{
							newReaction.getKineticLaw().getListOfLocalParameters().remove("i");
						}

						if (j)
						{
							newReaction.getKineticLaw().getListOfLocalParameters().remove("j");
						}

						reactionsToAdd.add(newReaction);

						++membraneDiffusionIndex;
					}
				} // end if membrane diffusion

				// NON-MEMBRANE DIFFUSION REACTIONS
				// check to see if the (non-membrane-diffusion) reaction has
				// arrayed species
				// right now i'm only checking the first reactant species, due
				// to a bad assumption
				// about the homogeneity of the arrayed reaction (ie, if one
				// species is arrayed, they all are)
				else if (reaction.getReactantCount() > 0 && speciesToIsArrayedMap.get(reaction.getReactant(0).getSpeciesInstance().getId()) == true)
				{

					arraysExist = true;

					reactionsToRemove.add(reaction.getId());

					// get the reactant dimensions, which tells us how many new
					// reactions are going to be created
					SpeciesDimensions reactantDimensions = arrayedSpeciesToDimensionsMap.get(reaction.getReactant(0).getSpeciesInstance().getId());

					boolean abort = false;

					// loop through all of the new formerly-implicit reactants
					for (int row = reactantDimensions.numRowsLower; row <= reactantDimensions.numRowsUpper; ++row)
					{
						for (int col = reactantDimensions.numColsLower; col <= reactantDimensions.numColsUpper; ++col)
						{

							// create a new reaction and set the ID
							Reaction newReaction = new Reaction();
							newReaction = reaction.clone();
							newReaction.setListOfReactants(new ListOf<SpeciesReference>());
							newReaction.setListOfProducts(new ListOf<SpeciesReference>());
							newReaction.setListOfModifiers(new ListOf<ModifierSpeciesReference>());
							newReaction.setId("ROW" + row + "_COL" + col + "_" + reactionID);
							SBMLutilities.setMetaId(newReaction, "ROW" + row + "_COL" + col + "_" + reactionID);
							newReaction.setReversible(false);
							newReaction.setFast(false);
							newReaction.setCompartment(reaction.getCompartment());

							// get the nodes to alter
							ArrayList<ASTNode> get2DArrayElementNodes = new ArrayList<ASTNode>();

							// return the head node of the get2DArrayElement
							// function
							getSatisfyingNodes(newReaction.getKineticLaw().getMath(), "get2DArrayElement", get2DArrayElementNodes);

							// loop through all reactants
							for (SpeciesReference reactant : reaction.getListOfReactants())
							{

								// find offsets
								// the row offset is in the kinetic law via i
								// the col offset is in the kinetic law via j
								int rowOffset = 0;
								int colOffset = 0;

								ASTNode reactantHeadNode = null;

								// go through the get2DArrayElement nodes and
								// find the one corresponding to the reactant
								for (ASTNode headNode : get2DArrayElementNodes)
								{

									// make sure it's a reactant node
									if (headNode.getChildCount() > 0 && model.getParameter(headNode.getLeftChild().getName()) == null)
									{

										reactantHeadNode = headNode;
										break;
									}
								}
								if (reactantHeadNode == null)
								{
									// TODO: Is this possible?
									System.out.println("Could not find reactant");
									return;
								}

								if (reactantHeadNode.getChild(1).getType().name().equals("PLUS"))
								{
									rowOffset = reactantHeadNode.getChild(1).getRightChild().getInteger();
								}
								else if (reactantHeadNode.getChild(1).getType().name().equals("MINUS"))
								{
									rowOffset = -1 * reactantHeadNode.getChild(1).getRightChild().getInteger();
								}

								if (reactantHeadNode.getChild(2).getType().name().equals("PLUS"))
								{
									colOffset = reactantHeadNode.getChild(2).getRightChild().getInteger();
								}
								else if (reactantHeadNode.getChild(2).getType().name().equals("MINUS"))
								{
									colOffset = -1 * reactantHeadNode.getChild(2).getRightChild().getInteger();
								}

								// create a new reactant and add it to the new
								// reaction
								SpeciesReference newReactant = new SpeciesReference();
								newReactant = reactant.clone();
								newReactant.setSpecies(model.getSpecies("ROW" + (row + rowOffset) + "_COL" + (col + colOffset) + "__" + newReactant.getSpecies()));
								newReactant.setAnnotation(new Annotation());
								newReaction.addReactant(newReactant);

								// put this reactant in place of the
								// get2DArrayElement function call
								for (int i = 0; i < reactantHeadNode.getChildCount(); ++i)
								{
									reactantHeadNode.removeChild(i);
								}

								for (int i = 0; i < reactantHeadNode.getChildCount(); ++i)
								{
									reactantHeadNode.removeChild(i);
								}

								reactantHeadNode.setVariable(model.getSpecies(newReactant.getSpecies()));
							}// end looping through reactants

							// loop through all modifiers
							// for (ModifierSpeciesReference modifier :
							// reaction.getListOfModifiers()) {

							// }

							// loop through all products
							for (SpeciesReference product : reaction.getListOfProducts())
							{

								// find offsets
								int rowOffset = 0;
								int colOffset = 0;

								ASTNode productHeadNode = null;

								// go through the get2DArrayElement nodes and
								// find the one corresponding to the product
								for (ASTNode headNode : get2DArrayElementNodes)
								{

									// make sure it's a product node
									// only the product has children, as the
									// reactant's children get deleted
									if (headNode.getChildCount() > 0 && model.getParameter(headNode.getLeftChild().getName()) == null)
									{

										productHeadNode = headNode;
										break;
									}
								}
								if (productHeadNode == null)
								{
									// TODO: Is this possible?
									System.out.println("Could not find product");
									return;
								}

								if (productHeadNode.getChild(1).getType().name().equals("PLUS"))
								{
									rowOffset = productHeadNode.getChild(1).getRightChild().getInteger();
								}
								else if (productHeadNode.getChild(1).getType().name().equals("MINUS"))
								{
									rowOffset = -1 * productHeadNode.getChild(1).getRightChild().getInteger();
								}

								if (productHeadNode.getChild(2).getType().name().equals("PLUS"))
								{
									colOffset = productHeadNode.getChild(2).getRightChild().getInteger();
								}
								else if (productHeadNode.getChild(2).getType().name().equals("MINUS"))
								{
									colOffset = -1 * productHeadNode.getChild(2).getRightChild().getInteger();
								}

								// don't create reactions with products that
								// don't exist
								if (row + rowOffset < reactantDimensions.numRowsLower || col + colOffset < reactantDimensions.numColsLower || row + rowOffset > reactantDimensions.numRowsUpper || col + colOffset > reactantDimensions.numColsUpper)
								{

									abort = true;
									break;
								}

								// create a new product and add it to the new
								// reaction
								SpeciesReference newProduct = new SpeciesReference();
								newProduct = product.clone();
								newProduct.setSpecies(model.getSpecies("ROW" + (row + rowOffset) + "_COL" + (col + colOffset) + "__" + newProduct.getSpecies()));
								newProduct.setAnnotation(new Annotation());
								newReaction.addProduct(newProduct);

								// put this reactant in place of the
								// get2DArrayElement function call
								for (int i = 0; i < productHeadNode.getChildCount(); ++i)
								{
									productHeadNode.removeChild(i);
								}

								// put this reactant in place of the
								// get2DArrayElement function call
								for (int i = 0; i < productHeadNode.getChildCount(); ++i)
								{
									productHeadNode.removeChild(i);
								}

								productHeadNode.setVariable(model.getSpecies(newProduct.getSpecies()));
							} // end looping through products

							if (abort == false)
							{

								boolean i = false, j = false;

								// checking for these local parameters using
								// getLocalParameters() doesn't seem to work
								for (LocalParameter lp : newReaction.getKineticLaw().getListOfLocalParameters())
								{

									if (lp.getId().equals("i"))
									{
										i = true;
									}
									else if (lp.getId().equals("j"))
									{
										j = true;
									}
								}

								if (i)
								{
									newReaction.getKineticLaw().getListOfLocalParameters().remove("i");
								}

								if (j)
								{
									newReaction.getKineticLaw().getListOfLocalParameters().remove("j");
								}

								// this isn't a reversible reaction; only take
								// the left side
								if (newReaction.getId().contains(GlobalConstants.DEGRADATION) == false)
								{
									newReaction.getKineticLaw().setMath(newReaction.getKineticLaw().getMath().getLeftChild());
								}

								reactionsToAdd.add(newReaction);
							}
							else
							{
								abort = false;
							}
						}
					}

				}
			}// end looping through reactions

			// add in the new explicit array reactions
			for (Reaction reactionToAdd : reactionsToAdd)
			{

				SBMLutilities.setMetaId(reactionToAdd, reactionToAdd.getId());
				if (model.getReaction(reactionToAdd.getId()) != null)
				{
					model.removeReaction(reactionToAdd.getId());
				}
				model.addReaction(reactionToAdd);
			}

			ListOf<Reaction> allReactions = model.getListOfReactions();

			// remove original array reaction(s)
			for (String reactionToRemove : reactionsToRemove)
			{
				allReactions.remove(reactionToRemove);
			}

			model.setListOfReactions(allReactions);

			ListOf<Species> allSpecies = model.getListOfSpecies();

			// remove the original array species from the model
			for (String speciesID : speciesToRemove)
			{
				allSpecies.remove(speciesID);
			}

			model.setListOfSpecies(allSpecies);

			ListOf<Event> allEvents = model.getListOfEvents();

			// remove original array event(s)
			for (String eventID : eventsToRemove)
			{
				allEvents.remove(eventID);
			}

			model.setListOfEvents(allEvents);

			ArrayList<String> parametersToRemove = new ArrayList<String>();

			// get rid of the locations parameters
			for (Parameter parameter : model.getListOfParameters())
			{
				if (parameter.getId().contains("_locations"))
				{
					if (parameter.getId().contains("_locations"))
					{
						this.submodelIDToLocationsMap.put(parameter.getId().replace("__locations", ""), stripAnnotation(parameter.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim()));
					}

					parametersToRemove.add(parameter.getId());
				}
			}

			for (String parameterID : parametersToRemove)
			{
				model.removeParameter(parameterID);
			}

		}
		catch (XMLStreamException e)
		{
			e.printStackTrace();
		}
		if (arraysExist)
		{

			SBMLWriter writer = new SBMLWriter();
			PrintStream p;

			try
			{
				p = new PrintStream(new FileOutputStream(SBMLFileName), true, "UTF-8");
				p.print(writer.writeSBMLToString(model.getSBMLDocument()));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	/**
	 * creates hashmaps for representing and keeping track of a grid (of cells)
	 */
	protected void setupGrid()
	{

		// go through all reaction IDs and group them by compartment ID
		for (String reactionID : reactionToPropensityMap.keySet())
		{

			if (reactionID.contains("__") == false)
			{
				continue;
			}

			// this will leave us with compartmentID or stuff_compartmentID
			String componentID = reactionID.split("__")[0];

			// if there's an underscore, remove everything before it to leave
			// the compartmentID
			String[] splitReactionID = componentID.split("_");
			componentID = splitReactionID[splitReactionID.length - 1];

			// the location of the component can be parsed from the membrane
			// diffusion reaction product
			if (reactionID.contains("MembraneDiffusion"))
			{

				String productID = "";

				for (StringDoublePair sdp : reactionToSpeciesAndStoichiometrySetMap.get(reactionID))
				{

					if (sdp.string.contains("ROW") && sdp.string.contains("COL"))
					{
						productID = sdp.string;
					}
				}

				String[] locationInfo = productID.split("__")[0].split("_");

				int row, col;

				if (locationInfo[0].length() <= 0)
				{
					row = 0;
					col = 0;
				}
				else
				{
					row = Integer.valueOf(locationInfo[0].replace("ROW", ""));
					col = Integer.valueOf(locationInfo[1].replace("COL", ""));
				}

				componentToLocationMap.put(componentID, new Point(row, col));
			}

			if (componentToReactionSetMap.containsKey(componentID) == false)
			{
				componentToReactionSetMap.put(componentID, new HashSet<String>());
			}

			componentToReactionSetMap.get(componentID).add(reactionID);
		}

		// go through the species and parameters and group them by compartment
		// ID
		for (String variableID : variableToValueMap.keySet())
		{

			if (variableID.contains("__") == false)
			{
				continue;
			}

			// this will leave us with compartmentID or stuff_compartmentID
			String componentID = variableID.split("__")[0];

			// if there's an underscore, remove everything before it to leave
			// the compartmentID
			String[] splitVariableID = componentID.split("_");
			componentID = splitVariableID[splitVariableID.length - 1];

			if (componentToVariableSetMap.containsKey(componentID) == false)
			{
				componentToVariableSetMap.put(componentID, new HashSet<String>());
			}

			componentToVariableSetMap.get(componentID).add(variableID);
		}

		// go through events and group them by compartment ID
		if (eventToDelayMap != null)
		{

			for (String eventID : eventToDelayMap.keySet())
			{

				if (eventID.contains("__") == false)
				{
					continue;
				}

				String componentID = eventID.split("__")[0];

				// if there's an underscore, remove everything before it to
				// leave the compartmentID
				String[] splitEventID = componentID.split("_");
				componentID = splitEventID[splitEventID.length - 1];

				componentIDSet.add(componentID);

				if (componentToEventSetMap.containsKey(componentID) == false)
				{
					componentToEventSetMap.put(componentID, new HashSet<String>());
				}

				componentToEventSetMap.get(componentID).add(eventID);
			}
		}

		// if the location information couldn't be parsed from membrane
		// diffusion reactions
		// (ie, if there aren't any membrane diffusion reactions)
		if (componentToLocationMap.size() < componentToVariableSetMap.size())
		{

			for (String annotationString : this.submodelIDToLocationsMap.values())
			{

				annotationString = annotationString.replace("<annotation>", "").replace("</annotation>", "").replace("\"", "");
				String[] splitAnnotation = annotationString.split("array:");

				splitAnnotation[splitAnnotation.length - 2] = splitAnnotation[splitAnnotation.length - 2].split("xmlns:")[0];

				for (int i = 2; i < splitAnnotation.length; ++i)
				{

					String compartmentID = splitAnnotation[i].split("=")[0];
					String row = splitAnnotation[i].split(" ")[0].split("=")[1].split(",")[0].replace("(", "");
					String col = splitAnnotation[i].split(" ")[0].split("=")[1].split(",")[1].replace(")", "");

					componentToLocationMap.put(compartmentID, new Point(Integer.valueOf(row.trim()), Integer.valueOf(col.trim())));
				}
			}
		}
	}

	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	private void setupSingleSpecies(Species species, String speciesID)
	{

		speciesID = speciesID.replace("_negative_", "-");

		if (speciesIDSet.contains(speciesID))
		{
			return;
		}

		if (species.isSetInitialAmount())
		{
			variableToValueMap.put(speciesID, species.getInitialAmount());
		}
		else if (species.isSetInitialConcentration())
		{

			variableToValueMap.put(speciesID, species.getInitialConcentration() * species.getCompartmentInstance().getSize());
		}

		if (species.getConversionFactor() != null)
		{
			speciesToConversionFactorMap.put(speciesID, species.getConversionFactor());
		}

		if (numRules > 0)
		{
			variableToIsInAssignmentRuleMap.put(speciesID, false);
		}

		if (numConstraints > 0)
		{
			variableToIsInConstraintMap.put(speciesID, false);
		}

		if (species.hasOnlySubstanceUnits() == false)
		{

			speciesToCompartmentSizeMap.put(speciesID, species.getCompartmentInstance().getSize());
			speciesToCompartmentNameMap.put(speciesID, species.getCompartment());

			if (Double.isNaN(species.getCompartmentInstance().getSize()))
			{
				speciesToCompartmentSizeMap.put(speciesID, 1.0);
			}
		}

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
	protected void setupSpecies() throws IOException
	{

		// add values to hashmap for easy access to species amounts
		for (Species species : model.getListOfSpecies())
		{

			setupSingleSpecies(species, species.getId());
		}
	}

	/**
	 * puts initial assignment-related information into data structures
	 */
	protected void setupInitialAssignments()
	{

		HashSet<AssignmentRule> allAssignmentRules = new HashSet<AssignmentRule>();

		// perform all assignment rules
		for (Rule rule : model.getListOfRules())
		{

			if (rule.isAssignment())
			{
				allAssignmentRules.add((AssignmentRule) rule);
			}
		}

		performAssignmentRules(allAssignmentRules);

		HashSet<String> affectedVariables = new HashSet<String>();

		// calculate initial assignments a lot of times in case there are
		// dependencies
		// running it the number of initial assignments times will avoid
		// problems
		// and all of them will be fully calculated and determined
		for (int i = 0; i < numInitialAssignments; ++i)
		{

			for (InitialAssignment initialAssignment : model.getListOfInitialAssignments())
			{

				String variable = initialAssignment.getVariable().replace("_negative_", "-");
				initialAssignment.setMath(inlineFormula(initialAssignment.getMath()));

				if (speciesToHasOnlySubstanceUnitsMap.containsKey(variable) && speciesToHasOnlySubstanceUnitsMap.get(variable) == false)
				{

					variableToValueMap.put(variable, evaluateExpressionRecursive(initialAssignment.getMath()) * variableToValueMap.get(speciesToCompartmentNameMap.get(variable)));
				}
				else
				{
					variableToValueMap.put(variable, evaluateExpressionRecursive(initialAssignment.getMath()));
				}

				affectedVariables.add(variable);
			}
		}

		// perform assignment rules again for variable that may have changed due
		// to the initial assignments
		// they aren't set up yet, so just perform them all
		performAssignmentRules(allAssignmentRules);

		// this is kind of weird, but apparently if an initial assignment
		// changes a compartment size
		// i need to go back and update species amounts because they used the
		// non-changed-by-assignment sizes
		for (Species species : model.getListOfSpecies())
		{

			if (species.isSetInitialConcentration())
			{

				String speciesID = species.getId();

				// revert to the initial concentration value
				if (Double.isNaN(variableToValueMap.get(speciesID)) == false)
				{
					variableToValueMap.put(speciesID, variableToValueMap.get(speciesID) / species.getCompartmentInstance().getSize());
				}
				else
				{
					variableToValueMap.put(speciesID, species.getInitialConcentration());
				}

				// multiply by the new compartment size to get into amount
				variableToValueMap.put(speciesID, variableToValueMap.get(speciesID) * variableToValueMap.get(speciesToCompartmentNameMap.get(speciesID)));
			}
		}
	}

	/**
	 * sets up the local parameters in a single kinetic law
	 * 
	 * @param kineticLaw
	 * @param reactionID
	 */
	private void setupLocalParameters(KineticLaw kineticLaw, Reaction reaction)
	{

		String reactionID = reaction.getId();
		reactionID = reactionID.replace("_negative_", "-");

		for (int i = 0; i < kineticLaw.getLocalParameterCount(); i++)
		{

			LocalParameter localParameter = kineticLaw.getLocalParameter(i);

			String parameterID = "";

			// the parameters don't get reset after each run, so don't re-do
			// this prepending
			if (localParameter.getId().contains(reactionID + "_") == false)
			{
				parameterID = reactionID + "_" + localParameter.getId();
			}
			else
			{
				parameterID = localParameter.getId();
				// System.out.println("Before: " + localParameter.getId() +
				// " after: " + parameterID);
			}

			String oldParameterID = localParameter.getId();
			variableToValueMap.put(parameterID, localParameter.getValue());

			// alter the local parameter ID so that it goes to the local and not
			// global value
			if (localParameter.getId() != parameterID)
			{
				localParameter.setId(parameterID);
				SBMLutilities.setMetaId(localParameter, parameterID);
			}
			// System.out.println("After: " + localParameter.getId());

			// for some reason, changing the local parameter sometimes changes
			// the kinetic law instances
			// of that parameter id (and sometimes doesn't), so those ones are
			// fine and ignore them
			// if (kineticLaw.getMath().toFormula().contains(parameterID) ==
			// false) {

			alterLocalParameter(kineticLaw.getMath(), reaction, oldParameterID, parameterID);
			// }
		}
		// System.out.println(kineticLaw.getMath().toFormula());
	}

	/**
	 * sets up a single (non-local) parameter
	 * 
	 * @param parameter
	 */
	private void setupSingleParameter(Parameter parameter)
	{

		String parameterID = parameter.getId();

		variableToValueMap.put(parameterID, parameter.getValue());
		variableToIsConstantMap.put(parameterID, parameter.getConstant());

		if (parameter.getConstant() == false)
		{
			nonconstantParameterIDSet.add(parameterID);
		}

		if (numRules > 0)
		{
			variableToIsInAssignmentRuleMap.put(parameterID, false);
		}

		if (numConstraints > 0)
		{
			variableToIsInConstraintMap.put(parameterID, false);
		}
	}

	/**
	 * puts parameter-related information into data structures
	 */
	protected void setupParameters()
	{

		// add local parameters
		for (Reaction reaction : model.getListOfReactions())
		{

			if (!reaction.isSetKineticLaw())
			{
				continue;
			}
			KineticLaw kineticLaw = reaction.getKineticLaw();
			setupLocalParameters(kineticLaw, reaction);
		}

		// add values to hashmap for easy access to global parameter values
		// NOTE: the IDs for the parameters and species must be unique, so
		// putting them in the
		// same hashmap is okay
		for (Parameter parameter : model.getListOfParameters())
		{

			setupSingleParameter(parameter);
		}

		// add compartment sizes in
		for (Compartment compartment : model.getListOfCompartments())
		{

			String compartmentID = compartment.getId();

			compartmentIDSet.add(compartmentID);
			variableToValueMap.put(compartmentID, compartment.getSize());

			if (Double.isNaN(compartment.getSize()))
			{
				variableToValueMap.put(compartmentID, 1.0);
			}

			variableToIsConstantMap.put(compartmentID, compartment.getConstant());

			if (numRules > 0)
			{
				variableToIsInAssignmentRuleMap.put(compartmentID, false);
			}

			if (numConstraints > 0)
			{
				variableToIsInConstraintMap.put(compartmentID, false);
			}
		}
	}

	/**
	 * puts rule-related information into data structures
	 */
	protected void setupRules()
	{

		numAssignmentRules = 0;
		numRateRules = 0;

		// NOTE: assignmentrules are performed in setupinitialassignments

		// loop through all assignment rules
		// store which variables (RHS) affect the rule variable (LHS)
		// so when those RHS variables change, we know to re-evaluate the rule
		// and change the value of the LHS variable
		for (Rule rule : model.getListOfRules())
		{

			if (rule.isAssignment())
			{

				// Rules don't have a getVariable method, so this needs to be
				// cast to an ExplicitRule
				rule.setMath(inlineFormula(rule.getMath()));
				AssignmentRule assignmentRule = (AssignmentRule) rule;

				// list of all children of the assignmentRule math
				ArrayList<ASTNode> formulaChildren = new ArrayList<ASTNode>();

				if (assignmentRule.getMath().getChildCount() == 0)
				{
					formulaChildren.add(assignmentRule.getMath());
				}
				else
				{
					getAllASTNodeChildren(assignmentRule.getMath(), formulaChildren);
				}

				for (ASTNode ruleNode : formulaChildren)
				{

					if (ruleNode.isName())
					{

						String nodeName = ruleNode.getName();

						variableToAffectedAssignmentRuleSetMap.put(nodeName, new HashSet<AssignmentRule>());
						variableToAffectedAssignmentRuleSetMap.get(nodeName).add(assignmentRule);
						variableToIsInAssignmentRuleMap.put(nodeName, true);
					}
				}

				++numAssignmentRules;
				listOfAssignmentRules.add(assignmentRule);
			}
			else if (rule.isRate())
			{

				// Rules don't have a getVariable method, so this needs to be
				// cast to an ExplicitRule
				rule.setMath(inlineFormula(rule.getMath()));
				RateRule rateRule = (RateRule) rule;

				listOfRateRules.add(rateRule);

				// list of all children of the assignmentRule math
				ArrayList<ASTNode> formulaChildren = new ArrayList<ASTNode>();

				if (rateRule.getMath().getChildCount() == 0)
				{
					formulaChildren.add(rateRule.getMath());
				}
				else
				{
					getAllASTNodeChildren(rateRule.getMath(), formulaChildren);
				}

				for (ASTNode ruleNode : formulaChildren)
				{

					if (ruleNode.isName())
					{

						String nodeName = ruleNode.getName();

						// not sure these are needed, as they only evaluate with
						// each time step
						// not when other variables update
						// variableToAffectedRateRuleSetMap.put(nodeName, new
						// HashSet<AssignmentRule>());
						// variableToAffectedRateRuleSetMap.get(nodeName).add(rateRule);
						variableToIsInRateRuleMap.put(nodeName, true);
					}
				}

				++numRateRules;
			}
		}

		// don't think this is necessary
		// //loop through the formulas and inline any user-defined functions
		// for (Rule rule : model.getListOfRules()) {
		//
		// if (rule.isAssignment() || rule.isRate()) {
		// rule.setMath(inlineFormula(rule.getMath()));
		// System.err.println(rule.getMath());
		// }
		// }
	}

	/**
	 * puts constraint-related information into data structures
	 */
	protected void setupConstraints()
	{

		// loop through all constraints to find out which variables affect which
		// constraints
		// this is stored in a hashmap, as is whether the variable is in a
		// constraint
		for (Constraint constraint : model.getListOfConstraints())
		{

			constraint.setMath(inlineFormula(constraint.getMath()));
			for (ASTNode constraintNode : constraint.getMath().getListOfNodes())
			{

				if (constraintNode.isName())
				{

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
	protected void setupSingleEvent(Event event)
	{

		String eventID = event.getId();

		// these events are what determines if a model is dynamic or not
		if (eventID.contains("Division__") || eventID.contains("Death__") || eventID.contains("__Move"))
		{
			dynamicBoolean = true;
		}

		if (event.isSetPriority())
		{
			eventToPriorityMap.put(eventID, inlineFormula(event.getPriority().getMath()));
		}

		if (event.isSetDelay())
		{

			eventToDelayMap.put(eventID, inlineFormula(event.getDelay().getMath()));
			eventToHasDelayMap.put(eventID, true);
		}
		else
		{
			eventToHasDelayMap.put(eventID, false);
		}

		if (event.getTrigger().getMath().toFormula().contains("neighborQuantity") == false)
		{
			event.getTrigger().setMath(inlineFormula(event.getTrigger().getMath()));
		}

		eventToTriggerMap.put(eventID, event.getTrigger().getMath());
		eventToTriggerInitiallyTrueMap.put(eventID, event.getTrigger().isInitialValue());
		eventToPreviousTriggerValueMap.put(eventID, event.getTrigger().isInitialValue());
		eventToTriggerPersistenceMap.put(eventID, event.getTrigger().getPersistent());
		eventToUseValuesFromTriggerTimeMap.put(eventID, event.isUseValuesFromTriggerTime());
		eventToAssignmentSetMap.put(eventID, new HashSet<Object>());
		eventToAffectedReactionSetMap.put(eventID, new HashSet<String>());

		untriggeredEventSet.add(eventID);

		for (EventAssignment assignment : event.getListOfEventAssignments())
		{

			String variableID = assignment.getVariable();

			assignment.setMath(inlineFormula(assignment.getMath()));

			eventToAssignmentSetMap.get(eventID).add(assignment);

			if (variableToEventSetMap.containsKey(variableID) == false)
			{
				variableToEventSetMap.put(variableID, new HashSet<String>());
			}

			variableToEventSetMap.get(variableID).add(eventID);

			// if the variable is a species, add the reactions it's in
			// to the event to affected reaction hashmap, which is used
			// for updating propensities after an event fires
			if (speciesToAffectedReactionSetMap.containsKey(variableID))
			{

				eventToAffectedReactionSetMap.get(eventID).addAll(speciesToAffectedReactionSetMap.get(variableID));
			}
		}
	}

	/**
	 * puts event-related information into data structures
	 */
	protected void setupEvents()
	{

		// add event information to hashmaps for easy/fast access
		// this needs to happen after calculating initial propensities
		// so that the speciesToAffectedReactionSetMap is populated
		for (Event event : model.getListOfEvents())
		{

			setupSingleEvent(event);
		}
	}

	/**
	 * does a minimized initialization process to prepare for a new run
	 */
	@Override
	public abstract void setupForNewRun(int newRun);

	/**
	 * opens output file and seeds rng for new run
	 * 
	 * @param randomSeed
	 * @param currentRun
	 * @throws IOException
	 */
	protected void setupForOutput(long randomSeed, int currentRun)
	{

		this.currentRun = currentRun;

		randomNumberGenerator = new XORShiftRandom(randomSeed);

		try
		{

			String extension = ".tsd";

			if (dynamicBoolean == true)
			{
				extension = ".dtsd";
			}

			TSDWriter = new FileWriter(outputDirectory + "run-" + currentRun + extension);
			bufferedTSDWriter = new BufferedWriter(TSDWriter);
			bufferedTSDWriter.write('(');

			if (currentRun > 1 && dynamicBoolean == false)
			{

				bufferedTSDWriter.write("(" + "\"" + "time" + "\"");

				// if there's an interesting species, only those get printed
				if (interestingSpecies.size() > 0)
				{

					for (String speciesID : interestingSpecies)
					{
						bufferedTSDWriter.write(", \"" + speciesID + "\"");
					}

					if (dynamicBoolean == false)
					{
						bufferedTSDWriter.write("),\n");
					}
				}
				else
				{

					for (String speciesID : speciesIDSet)
					{

						bufferedTSDWriter.write(", \"" + speciesID + "\"");
					}

					// print compartment IDs (for sizes)
					for (String componentID : compartmentIDSet)
					{

						bufferedTSDWriter.write(", \"" + componentID + "\"");
					}

					// print nonconstant parameter IDs
					for (String parameterID : nonconstantParameterIDSet)
					{

						try
						{
							bufferedTSDWriter.write(", \"" + parameterID + "\"");
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}

					bufferedTSDWriter.write("),\n");
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * calculates the initial propensity of a single reaction also does some
	 * initialization stuff
	 * 
	 * @param reactionID
	 * @param reactionFormula
	 * @param reversible
	 * @param reactantsList
	 * @param productsList
	 * @param modifiersList
	 */
	private void setupSingleReaction(String reactionID, ASTNode reactionFormula, boolean reversible, ListOf<SpeciesReference> reactantsList, ListOf<SpeciesReference> productsList, ListOf<ModifierSpeciesReference> modifiersList)
	{

		reactionID = reactionID.replace("_negative_", "-");

		boolean notEnoughMoleculesFlagFd = false;
		boolean notEnoughMoleculesFlagRv = false;
		boolean notEnoughMoleculesFlag = false;

		// if it's a reversible reaction
		// split into a forward and reverse reaction (based on the minus sign in
		// the middle)
		// and calculate both propensities
		if (reversible == true)
		{

			// distributes the left child across the parentheses
			if (reactionFormula.getType().equals(ASTNode.Type.TIMES))
			{

				ASTNode distributedNode = new ASTNode();

				reactionFormula = inlineFormula(reactionFormula);

				if (reactionFormula.getChildCount() >= 2 && reactionFormula.getChild(1).getType().equals(ASTNode.Type.PLUS))
				{
					distributedNode = ASTNode.sum(ASTNode.times(reactionFormula.getLeftChild(), reactionFormula.getRightChild().getLeftChild()), ASTNode.times(new ASTNode(-1), reactionFormula.getLeftChild(), reactionFormula.getRightChild().getRightChild()));
				}
				else if (reactionFormula.getChildCount() >= 2 && reactionFormula.getChild(1).getType().equals(ASTNode.Type.MINUS))
				{
					distributedNode = ASTNode.diff(ASTNode.times(reactionFormula.getLeftChild(), reactionFormula.getRightChild().getLeftChild()), ASTNode.times(reactionFormula.getLeftChild(), reactionFormula.getRightChild().getRightChild()));
				}

				reactionFormula = distributedNode;
			}

			reactionToSpeciesAndStoichiometrySetMap.put(reactionID + "_fd", new HashSet<StringDoublePair>());
			reactionToSpeciesAndStoichiometrySetMap.put(reactionID + "_rv", new HashSet<StringDoublePair>());
			reactionToReactantStoichiometrySetMap.put(reactionID + "_fd", new HashSet<StringDoublePair>());
			reactionToReactantStoichiometrySetMap.put(reactionID + "_rv", new HashSet<StringDoublePair>());

			for (SpeciesReference reactant : reactantsList)
			{

				String reactantID = reactant.getSpecies().replace("_negative_", "-");

				// stoichiometry amplification -- alter the stoichiometry
				if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
				{
					reactant.setStoichiometry(stoichAmpGridValue);
				}

				double reactantStoichiometry;

				// if there was an initial assignment for the reactant
				// this applies regardless of constancy of the reactant
				if (variableToValueMap.containsKey(reactant.getId()))
				{
					reactantStoichiometry = variableToValueMap.get(reactant.getId());
				}
				else
				{
					reactantStoichiometry = reactant.getStoichiometry();
				}

				reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_fd").add(new StringDoublePair(reactantID, -reactantStoichiometry));
				reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_rv").add(new StringDoublePair(reactantID, reactantStoichiometry));

				// not having a minus sign is intentional as this isn't used for
				// calculations
				reactionToReactantStoichiometrySetMap.get(reactionID + "_fd").add(new StringDoublePair(reactantID, reactantStoichiometry));

				// if there was not initial assignment for the reactant
				if (reactant.getConstant() == false && variableToValueMap.containsKey(reactant.getId()) == false && reactant.getId().length() > 0)
				{

					if (reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID + "_fd") == false)
					{
						reactionToNonconstantStoichiometriesSetMap.put(reactionID + "_fd", new HashSet<StringStringPair>());
					}
					if (reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID + "_rv") == false)
					{
						reactionToNonconstantStoichiometriesSetMap.put(reactionID + "_rv", new HashSet<StringStringPair>());
					}

					reactionToNonconstantStoichiometriesSetMap.get(reactionID + "_fd").add(new StringStringPair(reactantID + "_fd", reactant.getId()));
					reactionToNonconstantStoichiometriesSetMap.get(reactionID + "_rv").add(new StringStringPair(reactantID + "_rv", reactant.getId()));

					variableToValueMap.put(reactant.getId(), reactantStoichiometry);
				}

				// as a reactant, this species affects the reaction's propensity
				// in the forward direction
				speciesToAffectedReactionSetMap.get(reactantID).add(reactionID + "_fd");

				// make sure there are enough molecules for this species
				// (in the reverse direction, molecules aren't subtracted, but
				// added)
				if (variableToValueMap.get(reactantID) < reactantStoichiometry)
				{
					notEnoughMoleculesFlagFd = true;
				}
			}

			for (SpeciesReference product : productsList)
			{

				String productID = product.getSpecies().replace("_negative_", "-");

				// stoichiometry amplification -- alter the stoichiometry
				if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
				{
					product.setStoichiometry(stoichAmpGridValue);
				}

				double productStoichiometry;

				// if there was an initial assignment
				if (variableToValueMap.containsKey(product.getId()))
				{
					productStoichiometry = variableToValueMap.get(product.getId());
				}
				else
				{
					productStoichiometry = product.getStoichiometry();
				}

				reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_fd").add(new StringDoublePair(productID, productStoichiometry));
				reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_rv").add(new StringDoublePair(productID, -productStoichiometry));

				// not having a minus sign is intentional as this isn't used for
				// calculations
				reactionToReactantStoichiometrySetMap.get(reactionID + "_rv").add(new StringDoublePair(productID, productStoichiometry));

				// if there wasn't an initial assignment
				if (product.getConstant() == false && variableToValueMap.containsKey(product.getId()) == false && product.getId().length() > 0)
				{

					if (reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
					{
						reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());
					}

					reactionToNonconstantStoichiometriesSetMap.get(reactionID).add(new StringStringPair(productID, product.getId()));
					variableToValueMap.put(product.getId(), productStoichiometry);
				}

				// as a product, this species affects the reaction's propensity
				// in the reverse direction
				speciesToAffectedReactionSetMap.get(productID).add(reactionID + "_rv");

				// make sure there are enough molecules for this species
				// (in the forward direction, molecules aren't subtracted, but
				// added)
				if (variableToValueMap.get(productID) < productStoichiometry)
				{
					notEnoughMoleculesFlagRv = true;
				}
			}

			for (ModifierSpeciesReference modifier : modifiersList)
			{

				String modifierID = modifier.getSpecies();
				modifierID = modifierID.replace("_negative_", "-");

				String forwardString = "", reverseString = "";

				try
				{
					forwardString = ASTNode.formulaToString(reactionFormula.getLeftChild());
					reverseString = ASTNode.formulaToString(reactionFormula.getRightChild());
				}
				catch (SBMLException e)
				{
					e.printStackTrace();
				}

				// check the kinetic law to see which direction the modifier
				// affects the reaction's propensity
				if (forwardString.contains(modifierID))
				{
					speciesToAffectedReactionSetMap.get(modifierID).add(reactionID + "_fd");
				}

				if (reverseString.contains(modifierID))
				{
					speciesToAffectedReactionSetMap.get(modifierID).add(reactionID + "_rv");
				}
			}

			double propensity;

			reactionToFormulaMap.put(reactionID + "_rv", inlineFormula(reactionFormula.getRightChild()));
			reactionToFormulaMap.put(reactionID + "_fd", inlineFormula(reactionFormula.getLeftChild()));

			// calculate forward reaction propensity
			if (notEnoughMoleculesFlagFd == true)
			{
				propensity = 0.0;
			}
			else
			{
				// the left child is what's left of the minus sign
				propensity = evaluateExpressionRecursive(inlineFormula(reactionFormula.getLeftChild()));

				// stoichiometry amplification -- alter the propensity
				if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
				{
					propensity *= (1.0 / stoichAmpGridValue);
				}

				if ((propensity < minPropensity) && (propensity > 0))
				{
					minPropensity = propensity;
				}

				if (propensity > maxPropensity)
				{
					maxPropensity = propensity;
				}

				totalPropensity += propensity;
			}

			reactionToPropensityMap.put(reactionID + "_fd", propensity);

			// calculate reverse reaction propensity
			if (notEnoughMoleculesFlagRv == true)
			{
				propensity = 0.0;
			}
			else
			{
				// the right child is what's right of the minus sign
				propensity = evaluateExpressionRecursive(inlineFormula(reactionFormula.getRightChild()));

				// stoichiometry amplification -- alter the propensity
				if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
				{
					propensity *= (1.0 / stoichAmpGridValue);
				}

				if ((propensity < minPropensity) && (propensity > 0))
				{
					minPropensity = propensity;
				}

				if (propensity > maxPropensity)
				{
					maxPropensity = propensity;
				}

				totalPropensity += propensity;
			}

			reactionToPropensityMap.put(reactionID + "_rv", propensity);
		}
		// if it's not a reversible reaction
		else
		{
			reactionToSpeciesAndStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());
			reactionToReactantStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());

			for (SpeciesReference reactant : reactantsList)
			{

				String reactantID = reactant.getSpecies().replace("_negative_", "-");

				// stoichiometry amplification -- alter the stoichiometry
				if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
				{
					reactant.setStoichiometry(stoichAmpGridValue);
				}

				double reactantStoichiometry;

				// if there was an initial assignment for the speciesref id
				if (variableToValueMap.containsKey(reactant.getId()))
				{
					reactantStoichiometry = variableToValueMap.get(reactant.getId());
				}
				else
				{
					reactantStoichiometry = reactant.getStoichiometry();
				}

				reactionToSpeciesAndStoichiometrySetMap.get(reactionID).add(new StringDoublePair(reactantID, -reactantStoichiometry));
				reactionToReactantStoichiometrySetMap.get(reactionID).add(new StringDoublePair(reactantID, reactantStoichiometry));

				// if there wasn't an initial assignment
				if (reactant.getConstant() == false && variableToValueMap.containsKey(reactant.getId()) == false && reactant.getId().length() > 0)
				{

					if (reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
					{
						reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());
					}

					reactionToNonconstantStoichiometriesSetMap.get(reactionID).add(new StringStringPair(reactantID, reactant.getId()));
					variableToValueMap.put(reactant.getId(), reactantStoichiometry);
				}

				// as a reactant, this species affects the reaction's propensity
				speciesToAffectedReactionSetMap.get(reactantID).add(reactionID);

				// make sure there are enough molecules for this species
				if (variableToValueMap.get(reactantID) < reactantStoichiometry)
				{
					notEnoughMoleculesFlag = true;
				}
			}

			for (SpeciesReference product : productsList)
			{

				// stoichiometry amplification -- alter the stoichiometry
				if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
				{
					product.setStoichiometry(stoichAmpGridValue);
				}

				String productID = product.getSpecies().replace("_negative_", "-");
				double productStoichiometry;

				// if there was an initial assignment for the speciesref id
				if (variableToValueMap.containsKey(product.getId()))
				{
					productStoichiometry = variableToValueMap.get(product.getId());
				}
				else
				{
					productStoichiometry = product.getStoichiometry();
				}

				reactionToSpeciesAndStoichiometrySetMap.get(reactionID).add(new StringDoublePair(productID, productStoichiometry));

				if (product.getConstant() == false && variableToValueMap.containsKey(product.getId()) == false && product.getId().length() > 0)
				{

					if (reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
					{
						reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());
					}

					reactionToNonconstantStoichiometriesSetMap.get(reactionID).add(new StringStringPair(productID, product.getId()));
					variableToValueMap.put(product.getId(), productStoichiometry);
				}

				// don't need to check if there are enough, because products are
				// added
			}

			for (ModifierSpeciesReference modifier : modifiersList)
			{

				String modifierID = modifier.getSpecies();
				modifierID = modifierID.replace("_negative_", "-");

				// as a modifier, this species affects the reaction's propensity
				speciesToAffectedReactionSetMap.get(modifierID).add(reactionID);
			}

			reactionToFormulaMap.put(reactionID, inlineFormula(reactionFormula));

			double propensity;

			if (notEnoughMoleculesFlag == true)
			{
				propensity = 0.0;
			}
			else
			{

				// calculate propensity
				propensity = evaluateExpressionRecursive(inlineFormula(reactionFormula));

				// stoichiometry amplification -- alter the propensity
				if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
				{
					propensity *= (1.0 / stoichAmpGridValue);
				}

				if (propensity < minPropensity && propensity > 0)
				{
					minPropensity = propensity;
				}
				if (propensity > maxPropensity)
				{
					maxPropensity = propensity;
				}

				totalPropensity += propensity;
			}

			reactionToPropensityMap.put(reactionID, propensity);
		}
	}

	/**
	 * calculates the initial propensities for each reaction in the model
	 * 
	 * @param numReactions
	 *            the number of reactions in the model
	 */
	protected void setupReactions()
	{

		// loop through all reactions and calculate their propensities
		for (Reaction reaction : model.getListOfReactions())
		{
			if (!reaction.isSetKineticLaw())
			{
				continue;
			}

			String reactionID = reaction.getId();
			ASTNode reactionFormula = reaction.getKineticLaw().getMath();

			setupSingleReaction(reactionID, reactionFormula, reaction.getReversible(), reaction.getListOfReactants(), reaction.getListOfProducts(), reaction.getListOfModifiers());
		}
	}

	/**
	 * abstract simulate method each simulator needs a simulate method
	 */
	@Override
	public abstract void simulate();

	/**
	 * returns an annotation with only array information remaining
	 * 
	 * @param annotation
	 * @return
	 */
	public static String stripAnnotation(String annotation)
	{

		if (annotation.contains("array:array"))
		{

			// only leave the stuff in these two namespaces, which should appear
			// mutually exclusively
			annotation = annotation.replace("<annotation>", "").replace("</annotation>", "");

			for (int i = 0; i < annotation.length() - 12; ++i)
			{

				if (annotation.substring(i, i + 12).equals("<array:array"))
				{
					annotation = annotation.substring(i);
				}
			}

			for (int i = 0; i < annotation.length() - 2; ++i)
			{
				if (annotation.substring(i, i + 2).equals("/>"))
				{
					annotation = annotation.substring(0, i + 2);
					break;
				}
			}

			return "<annotation>" + annotation + "</annotation>";
		}
		else if (annotation.contains("ibiosim:ibiosim"))
		{

			// only leave the stuff in these two namespaces, which should appear
			// mutually exclusively
			annotation = annotation.replace("<annotation>", "").replace("</annotation>", "");

			for (int i = 0; i < annotation.length() - 16; ++i)
			{

				if (annotation.substring(i, i + 16).equals("<ibiosim:ibiosim"))
				{
					annotation = annotation.substring(i);
				}
			}

			for (int i = 0; i < annotation.length() - 2; ++i)
			{
				if (annotation.substring(i, i + 2).equals("/>"))
				{
					annotation = annotation.substring(0, i + 2);
					break;
				}
			}

			return "<annotation>" + annotation + "</annotation>";
		}
		else
		{
			return new String("");
		}
	}

	/**
	 * this evaluates a set of constraints that have been affected by an event
	 * or reaction firing and returns the OR'd boolean result
	 * 
	 * @param affectedConstraintSet
	 *            the set of constraints affected
	 * @return the boolean result of the constraints' evaluation
	 */
	protected boolean testConstraints(HashSet<ASTNode> affectedConstraintSet)
	{

		// check all of the affected constraints
		// if one evaluates to true, then the simulation halts
		for (ASTNode constraint : affectedConstraintSet)
		{

			if (getBooleanFromDouble(evaluateExpressionRecursive(constraint)) == true)
			{
				return true;
			}
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
	private void updatePropensities(HashSet<String> reactionSet)
	{

		for (String reactionID : reactionSet)
		{

			boolean notEnoughMoleculesFlag = false;

			HashSet<StringDoublePair> reactantStoichiometrySet = reactionToReactantStoichiometrySetMap.get(reactionID);

			// check for enough molecules for the reaction to occur
			for (StringDoublePair speciesAndStoichiometry : reactantStoichiometrySet)
			{

				String speciesID = speciesAndStoichiometry.string;
				double stoichiometry = speciesAndStoichiometry.doub;

				if (reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID))
				{

					for (StringStringPair doubleID : reactionToNonconstantStoichiometriesSetMap.get(reactionID))
					{

						// string1 is the species ID; string2 is the
						// speciesReference ID
						if (doubleID.string1.equals(speciesID))
						{
							stoichiometry = variableToValueMap.get(doubleID.string2);
							break;
						}
					}
				}

				// if there aren't enough molecules to satisfy the stoichiometry
				if (variableToValueMap.get(speciesID) < stoichiometry)
				{
					notEnoughMoleculesFlag = true;
					break;
				}
			}

			double newPropensity = 0.0;

			if (notEnoughMoleculesFlag == false)
			{
				newPropensity = evaluateExpressionRecursive(reactionToFormulaMap.get(reactionID));
			}

			// stoichiometry amplification -- alter the propensity
			if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
			{
				newPropensity *= (1.0 / stoichAmpGridValue);
			}

			if (newPropensity > 0.0 && newPropensity < minPropensity)
			{
				minPropensity = newPropensity;
			}

			if (newPropensity > maxPropensity)
			{
				maxPropensity = newPropensity;
			}

			double oldPropensity = reactionToPropensityMap.get(reactionID);

			// add the difference of new v. old propensity to the total
			// propensity
			totalPropensity += newPropensity - oldPropensity;

			reactionToPropensityMap.put(reactionID, newPropensity);
		}
	}

	// EVENT TO FIRE INNER CLASS
	/**
	 * easy way to store multiple data points for events that are firing
	 */
	protected class EventToFire
	{

		public String			eventID				= "";
		public HashSet<Object>	eventAssignmentSet	= null;
		public double			fireTime			= 0.0;

		public EventToFire(String eventID, HashSet<Object> eventAssignmentSet, double fireTime)
		{

			this.eventID = eventID;
			this.eventAssignmentSet = eventAssignmentSet;
			this.fireTime = fireTime;
		}
	}

	// EVENT COMPARATOR INNER CLASS
	/**
	 * compares two events to see which one should be before the other in the
	 * priority queue
	 */
	protected class EventComparator implements Comparator<EventToFire>
	{

		/**
		 * compares two events based on their fire times and priorities
		 */
		@Override
		public int compare(EventToFire event1, EventToFire event2)
		{

			if (event1.fireTime > event2.fireTime)
			{
				return 1;
			}
			else if (event1.fireTime < event2.fireTime)
			{
				return -1;
			}
			else
			{

				if (eventToPriorityMap.get(event1.eventID) == null)
				{
					if (eventToPriorityMap.get(event2.eventID) != null)
					{
						return -1;
					}
					if ((Math.random() * 100) > 50)
					{
						return -1;
					}
					return 1;
				}

				if (evaluateExpressionRecursive(eventToPriorityMap.get(event1.eventID)) > evaluateExpressionRecursive(eventToPriorityMap.get(event2.eventID)))
				{
					return -1;
				}
				else if (evaluateExpressionRecursive(eventToPriorityMap.get(event1.eventID)) < evaluateExpressionRecursive(eventToPriorityMap.get(event2.eventID)))
				{
					return 1;
				}
				else
				{
					if ((Math.random() * 100) > 50)
					{
						return -1;
					}
					return 1;
				}
			}
		}
	}

	// STRING DOUBLE PAIR INNER CLASS
	/**
	 * class to combine a string and a double
	 */
	protected class StringDoublePair
	{

		public String	string;
		public double	doub;

		StringDoublePair(String s, double d)
		{

			string = s;
			doub = d;
		}
	}

	// STRING STRING PAIR INNER CLASS
	/**
	 * class to combine a string and a string
	 */
	protected class StringStringPair
	{

		public String	string1;
		public String	string2;

		StringStringPair(String s1, String s2)
		{

			string1 = s1;
			string2 = s2;
		}
	}
}
