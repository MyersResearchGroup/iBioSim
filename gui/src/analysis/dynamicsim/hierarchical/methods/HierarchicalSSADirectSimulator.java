package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import odk.lang.FastMath;

import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Rule;

import analysis.dynamicsim.hierarchical.HierarchicalArrayModels;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringPair;

//TODO: assignment rules need to verify if changing a hierarchical species because they
//		can trigger rules in other places.

public class HierarchicalSSADirectSimulator extends HierarchicalArrayModels
{

	private static Long		initializationTime	= new Long(0);
	private String			modelstateID;
	private final boolean	print;

	public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory,
			String outputDirectory, double timeLimit, double maxTimeStep, double minTimeStep,
			long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue,
			JFrame running, String[] interestingSpecies, String quantityType, String abstraction)
			throws IOException, XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep,
				progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType,
				abstraction);

		initialize(randomSeed, 1);
		modelstateID = "topmodel";
		this.print = true;
	}

	public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory,
			String outputDirectory, double timeLimit, double maxTimeStep, double minTimeStep,
			long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue,
			JFrame running, String[] interestingSpecies, String quantityType, String abstraction,
			boolean print) throws IOException, XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep,
				progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType,
				abstraction);

		initialize(randomSeed, 1);
		modelstateID = "topmodel";
		this.print = print;

	}

	@Override
	public void simulate()
	{

		double r1, r2, totalPropensity, delta_t, nextReactionTime, previousTime, printTime, nextEventTime;
		long initTime2;

		if (isSbmlHasErrorsFlag())
		{
			return;
		}

		initTime2 = System.nanoTime();

		initializationTime += System.nanoTime() - initTime2;

		printTime = getPrintInterval();

		nextEventTime = handleEvents();

		while (getCurrentTime() < getTimeLimit() && !isCancelFlag())
		{
			fireEvents();
			r1 = getRandomNumberGenerator().nextDouble();
			r2 = getRandomNumberGenerator().nextDouble();
			totalPropensity = getTotalPropensity();
			delta_t = FastMath.log(1 / r1) / totalPropensity;
			nextReactionTime = getCurrentTime() + delta_t;
			nextEventTime = handleEvents();
			previousTime = getCurrentTime();

			if (nextReactionTime < nextEventTime
					&& nextReactionTime < getCurrentTime() + getMaxTimeStep())
			{
				setCurrentTime(nextReactionTime);
			}
			else if (nextEventTime <= getCurrentTime() + getMaxTimeStep())
			{
				setCurrentTime(nextEventTime);
			}
			else
			{
				setCurrentTime(getCurrentTime() + getMaxTimeStep());
			}

			if (getCurrentTime() > getTimeLimit())
			{
				setCurrentTime(getTimeLimit());

				if (print)
				{
					print(printTime);
				}

				break;
			}

			if (print)
			{
				printTime = print(printTime);
			}

			if (getCurrentTime() == nextReactionTime)
			{
				printTime = print(printTime);
				update(true, true, true, r2, previousTime);
			}
			else
			{
				printTime = print(printTime);
				update(false, true, true, r2, previousTime);
			}

			// updateRules();
		}

		if (isCancelFlag() == false)
		{
			try
			{
				printToTSD(printTime);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				getBufferedTSDWriter().write(')');
				getBufferedTSDWriter().flush();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}

	/**
	 * performs every rate rule using the current time step
	 * 
	 * @param delta_t
	 * @return
	 */
	private HashSet<String> performRateRules(ModelState modelstate, double delta_t)
	{

		HashSet<String> affectedVariables = new HashSet<String>();

		for (Rule rule : modelstate.getRateRulesList())
		{

			RateRule rateRule = (RateRule) rule;
			String variable = rateRule.getVariable();

			// update the species count (but only if the species isn't constant)
			// (bound cond is fine)
			if (modelstate.getVariableToIsConstantMap().containsKey(variable)
					&& modelstate.getVariableToIsConstantMap().get(variable) == false)
			{

				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable)
						&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false)
				{

					double currVal = modelstate.getVariableToValue(getReplacements(), variable);
					double incr = delta_t
							* (evaluateExpressionRecursive(modelstate, rateRule.getMath(), false,
									getCurrentTime(), null, null) * modelstate.getVariableToValue(
									getReplacements(), modelstate.getSpeciesToCompartmentNameMap()
											.get(variable)));
					modelstate.setvariableToValueMap(getReplacements(), variable, currVal + incr);
				}
				else
				{
					double currVal = modelstate.getVariableToValue(getReplacements(), variable);
					double incr = delta_t
							* evaluateExpressionRecursive(modelstate, rateRule.getMath(), false,
									getCurrentTime(), null, null);

					modelstate.setvariableToValueMap(getReplacements(), variable, currVal + incr);
				}

				affectedVariables.add(variable);
			}

		}

		return affectedVariables;
	}

	/**
	 * sets up data structures local to the SSA-Direct method
	 * 
	 * @param isNoEventsFlag
	 *            ()
	 * @param noAssignmentRulesFlag
	 * @param isNoConstraintsFlag
	 *            ()
	 */
	private void initialize(long randomSeed, int runNumber) throws IOException
	{

		setCurrentTime(0.0);
		setupNonConstantSpeciesReferences(getTopmodel());
		setupSpecies(getTopmodel());
		setupParameters(getTopmodel());
		setupCompartments(getTopmodel());
		setupReactions(getTopmodel());
		setupEvents(getTopmodel());
		setupConstraints(getTopmodel());
		setupRules(getTopmodel());
		// setupInitialAssignments(getTopmodel());
		setupArraysValues(getTopmodel());
		setupArraysSBases(getTopmodel());

		setupForOutput(randomSeed, runNumber);

		for (ModelState model : getSubmodels().values())
		{
			setupNonConstantSpeciesReferences(model);
			setupSpecies(model);
			setupParameters(model);
			setupCompartments(model);
			setupReactions(model);
			setupConstraints(model);
			setupEvents(model);
			setupRules(model);
			setupInitialAssignments(model);
			setupForOutput(randomSeed, runNumber);
		}

		setupArrayedModels();

		setupVariableFromTSD();
	}

	private void setupVariableFromTSD() throws IOException
	{
		getBufferedTSDWriter().write("(" + "\"" + "time" + "\"");

		if (getInterestingSpecies().length > 0)
		{
			for (String s : getInterestingSpecies())
			{

				getBufferedTSDWriter().write(",\"" + s + "\"");

			}

			getBufferedTSDWriter().write("),\n");

			return;
		}

		for (String speciesID : getTopmodel().getSpeciesIDSet())
		{
			getBufferedTSDWriter().write(",\"" + speciesID + "\"");
		}

		for (String noConstantParam : getTopmodel().getVariablesToPrint())
		{
			getBufferedTSDWriter().write(",\"" + noConstantParam + "\"");
		}
		/*
		 * for (String compartment : getTopmodel().compartmentIDSet) {
		 * bufferedTSDWriter.write(", \"" + compartment + "\""); }
		 */
		for (ModelState model : getSubmodels().values())
		{
			for (String speciesID : model.getSpeciesIDSet())
			{
				if (!model.getIsHierarchical().contains(speciesID))
				{
					getBufferedTSDWriter().write(",\"" + model.getID() + "__" + speciesID + "\"");
				}
			}

			for (String noConstantParam : model.getVariablesToPrint())
			{
				if (!model.getIsHierarchical().contains(noConstantParam))
				{
					getBufferedTSDWriter().write(
							",\"" + model.getID() + "__" + noConstantParam + "\"");
				}
			}
		}

		getBufferedTSDWriter().write("),\n");

	}

	private HashSet<String> updatePropensities(HashSet<String> affectedReactionSet,
			ModelState modelstate)
	{

		HashSet<String> affectedSpecies = new HashSet<String>();

		for (String affectedReactionID : affectedReactionSet)
		{

			if (modelstate.isDeletedByMetaID(affectedReactionID))
			{
				continue;
			}

			HashSet<HierarchicalStringDoublePair> reactantStoichiometrySet = modelstate
					.getReactionToReactantStoichiometrySetMap().get(affectedReactionID);
			updatePropensity(modelstate, affectedReactionID, reactantStoichiometrySet,
					affectedSpecies);
		}

		return affectedSpecies;
	}

	private void updatePropensity(String modelstate, String species)
	{
		ModelState model = getModel(modelstate);
		HashSet<String> reactions = model.getSpeciesToAffectedReactionSetMap().get(species);
		updatePropensities(reactions, model);

	}

	private void perculateUp(ModelState modelstate, HashSet<String> affectedSpecies)
	{
		HashSet<String> updatedTopSpecies = new HashSet<String>();
		for (String species : affectedSpecies)
		{
			HashSet<HierarchicalStringPair> pairs = modelstate.getSpeciesToReplacement().get(
					species);
			if (pairs != null)
			{
				for (HierarchicalStringPair pair : pairs)
				{
					updatePropensity(pair.string1, pair.string2);
					updatedTopSpecies.add(pair.string2);
				}
			}
		}
		perculateDown(getTopmodel(), updatedTopSpecies);

	}

	private void perculateDown(ModelState modelstate, HashSet<String> affectedSpecies)
	{
		for (String species : affectedSpecies)
		{
			HashSet<HierarchicalStringPair> pairs = modelstate.getSpeciesToReplacement().get(
					species);
			if (pairs != null)
			{
				for (HierarchicalStringPair pair : pairs)
				{
					updatePropensity(pair.string1, pair.string2);
				}
			}
		}
	}

	private void updatePropensity(ModelState model, String affectedReactionID,
			HashSet<HierarchicalStringDoublePair> reactantStoichiometrySet,
			HashSet<String> affectedSpecies)
	{

		HashSet<HierarchicalStringDoublePair> reactionToSpecies = model
				.getReactionToSpeciesAndStoichiometrySetMap().get(affectedReactionID);

		if (model.getReactionToFormulaMap().get(affectedReactionID) == null)
		{
			model.getReactionToPropensityMap().put(affectedReactionID, 0.0);
			return;
		}

		boolean notEnoughMoleculesFlag = false;

		// check for enough molecules for the reaction to occur
		for (HierarchicalStringDoublePair speciesAndStoichiometry : reactantStoichiometrySet)
		{
			String speciesID = speciesAndStoichiometry.string;

			double stoichiometry = speciesAndStoichiometry.doub;

			// if there aren't enough molecules to satisfy the stoichiometry
			if (model.getVariableToValue(getReplacements(), speciesID) < stoichiometry)
			{
				notEnoughMoleculesFlag = true;
				break;
			}
		}

		for (HierarchicalStringDoublePair speciesPair : reactionToSpecies)
		{
			affectedSpecies.add(speciesPair.string);
		}

		double newPropensity = 0.0;
		if (notEnoughMoleculesFlag == false)
		{
			newPropensity = evaluateExpressionRecursive(model,
					model.getReactionToFormulaMap().get(affectedReactionID), false,
					getCurrentTime(), null, null);
		}

		double oldPropensity = model.getReactionToPropensityMap().get(affectedReactionID);
		model.setPropensity(model.getPropensity() + newPropensity - oldPropensity);
		model.updateReactionToPropensityMap(affectedReactionID, newPropensity);

	}

	/**
	 * randomly selects a reaction to perform
	 * 
	 * @param r2
	 *            random number
	 * @return the getID() of the selected reaction
	 */
	private String selectReaction(double r2)
	{

		double randomPropensity = r2 * (getTotalPropensity());
		double runningTotalReactionsPropensity = 0.0;
		String selectedReaction = "";

		// finds the reaction that the random propensity lies in
		// it keeps adding the next reaction's propensity to a running total
		// until the running total is greater than the random propensity

		for (String currentReaction : getTopmodel().getReactionToPropensityMap().keySet())
		{

			runningTotalReactionsPropensity += getTopmodel().getReactionToPropensityMap().get(
					currentReaction);

			if (randomPropensity < runningTotalReactionsPropensity)
			{
				selectedReaction = currentReaction;
				// keep track of submodel index
				modelstateID = "topmodel";
				return selectedReaction;
			}
		}

		for (ModelState models : getSubmodels().values())
		{

			for (String currentReaction : models.getReactionToPropensityMap().keySet())
			{
				runningTotalReactionsPropensity += models.getReactionToPropensityMap().get(
						currentReaction);

				if (randomPropensity < runningTotalReactionsPropensity)
				{
					selectedReaction = currentReaction;
					// keep track of submodel index
					modelstateID = models.getID();
					return selectedReaction;
				}
			}
		}

		return selectedReaction;
	}

	/**
	 * cancels the current run
	 */
	@Override
	public void cancel()
	{

		setCancelFlag(true);
	}

	/**
	 * clears data structures for new run
	 */
	@Override
	public void clear()
	{
		getTopmodel().clear();

		for (ModelState modelstate : getSubmodels().values())
		{
			modelstate.clear();
		}

		for (String key : getReplacements().keySet())
		{
			getReplacements().put(key, getInitReplacementState().get(key));
		}

	}

	private double handleEvents()
	{
		double nextEventTime = Double.POSITIVE_INFINITY;
		if (getTopmodel().isNoEventsFlag() == false)
		{
			handleEvents(getTopmodel());
			// step to the next event fire time if it comes before the next time
			// step
			if (!getTopmodel().getTriggeredEventQueue().isEmpty()
					&& getTopmodel().getTriggeredEventQueue().peek().getFireTime() <= nextEventTime)
			{
				if (getTopmodel().getTriggeredEventQueue().peek().getFireTime() < nextEventTime)
				{
					nextEventTime = getTopmodel().getTriggeredEventQueue().peek().getFireTime();
				}
			}
		}

		for (ModelState models : getSubmodels().values())
		{
			if (models.isNoEventsFlag() == false)
			{
				handleEvents(models);
				// step to the next event fire time if it comes before the next
				// time step
				if (!models.getTriggeredEventQueue().isEmpty()
						&& models.getTriggeredEventQueue().peek().getFireTime() <= nextEventTime)
				{
					if (models.getTriggeredEventQueue().peek().getFireTime() < nextEventTime)
					{
						nextEventTime = models.getTriggeredEventQueue().peek().getFireTime();
					}
				}
			}
		}
		return nextEventTime;
	}

	/**
	 * does minimized initalization process to prepare for a new run
	 */
	@Override
	public void setupForNewRun(int newRun)
	{
		setCurrentTime(0.0);
		try
		{
			setupNonConstantSpeciesReferences(getTopmodel());
			setupSpecies(getTopmodel());
			setupParameters(getTopmodel());
			setupReactions(getTopmodel());
			setupConstraints(getTopmodel());
			setupEvents(getTopmodel());
			setupInitialAssignments(getTopmodel());
			setupRules(getTopmodel());
			setupForOutput(0, newRun);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		for (ModelState model : getSubmodels().values())
		{
			try
			{
				setupNonConstantSpeciesReferences(model);
				setupSpecies(model);
				setupParameters(model);
				setupReactions(model);
				setupConstraints(model);
				setupEvents(model);
				setupInitialAssignments(model);
				setupRules(model);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}

		setConstraintFlag(true);

		try
		{
			setupVariableFromTSD();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private void update(boolean reaction, boolean assignRule, boolean rateRule, double r2,
			double previousTime)
	{
		if (reaction)
		{
			performReaction(r2);
		}

		if (assignRule)
		{
			performAssignmentRules(getTopmodel());

			for (ModelState modelstate : getSubmodels().values())
			{
				performAssignmentRules(modelstate);
			}
		}

		if (rateRule)
		{
			performRateRules(getTopmodel(), getCurrentTime() - previousTime);

			for (ModelState modelstate : getSubmodels().values())
			{
				performRateRules(modelstate, getCurrentTime() - previousTime);
			}
		}
	}

	private void performAssignmentRules(ModelState modelstate)
	{
		boolean changed = true;
		while (changed)
		{
			changed = false;
			for (AssignmentRule assignmentRule : modelstate.getAssignmentRulesList())
			{

				String variable = assignmentRule.getVariable();

				if (modelstate.getVariableToIsConstantMap().containsKey(variable)
						&& !modelstate.getVariableToIsConstantMap().get(variable)
						|| !modelstate.getVariableToIsConstantMap().containsKey(variable))
				{

					changed |= updateVariableValue(modelstate, variable, assignmentRule.getMath());

				}
			}
		}
	}

	private void performReaction(double r2)
	{
		String selectedReactionID = selectReaction(r2);
		if (!selectedReactionID.isEmpty())
		{
			if (modelstateID.equals("topmodel"))
			{
				if (!selectedReactionID.isEmpty())
				{
					performReaction(getTopmodel(), selectedReactionID,
							getTopmodel().isNoRuleFlag(), getTopmodel().isNoConstraintsFlag());
					HashSet<String> affectedReactionSet = getAffectedReactionSet(getTopmodel(),
							selectedReactionID, true);
					HashSet<String> affectedSpecies = updatePropensities(affectedReactionSet,
							getTopmodel());
					perculateDown(getTopmodel(), affectedSpecies);
				}
			}
			else
			{
				if (!selectedReactionID.isEmpty())
				{
					ModelState modelstate = getSubmodels().get(modelstateID);
					performReaction(modelstate, selectedReactionID, modelstate.isNoRuleFlag(),
							modelstate.isNoConstraintsFlag());
					HashSet<String> affectedReactionSet = getAffectedReactionSet(modelstate,
							selectedReactionID, true);
					HashSet<String> affectedSpecies = updatePropensities(affectedReactionSet,
							modelstate);
					perculateUp(modelstate, affectedSpecies);
				}
			}
		}
	}

	private void fireEvents()
	{
		if (getTopmodel().isNoEventsFlag() == false)
		{
			HashSet<String> affectedReactionSet = fireEvents(getTopmodel(), "reaction",
					getTopmodel().isNoRuleFlag(), getTopmodel().isNoConstraintsFlag());
			if (affectedReactionSet.size() > 0)
			{
				HashSet<String> affectedSpecies = updatePropensities(affectedReactionSet,
						getTopmodel());
				perculateDown(getTopmodel(), affectedSpecies);
			}
		}

		for (ModelState models : getSubmodels().values())
		{
			if (models.isNoEventsFlag() == false)
			{
				HashSet<String> affectedReactionSet = fireEvents(models, "reaction",
						models.isNoRuleFlag(), models.isNoConstraintsFlag());
				if (affectedReactionSet.size() > 0)
				{
					HashSet<String> affectedSpecies = updatePropensities(affectedReactionSet,
							models);
					perculateUp(models, affectedSpecies);

				}
			}
		}

	}

	private double print(double printTime)
	{
		while (getCurrentTime() > printTime && printTime < getTimeLimit())
		{

			try
			{
				printToTSD(printTime);
				getBufferedTSDWriter().write(",\n");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			printTime += getPrintInterval();
			getRunning().setTitle(
					"Progress (" + (int) ((getCurrentTime() / getTimeLimit()) * 100.0) + "%)");
			getProgress().setValue((int) ((getCurrentTime() / getTimeLimit()) * 100.0));

		}

		return printTime;
	}
}
