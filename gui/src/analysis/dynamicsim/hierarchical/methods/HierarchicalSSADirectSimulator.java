package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import odk.lang.FastMath;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Rule;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalArrayModels;
import analysis.dynamicsim.hierarchical.util.ArraysObject;
import analysis.dynamicsim.hierarchical.util.Evaluator;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringPair;

//TODO: assignment rules need to verify if changing a hierarchical species because they
//		can trigger rules in other places.

public class HierarchicalSSADirectSimulator extends HierarchicalArrayModels
{

	private final Map<Double, List<Double>>	buffer;
	private int								currRun;
	private String							modelstateID;
	private final boolean					print;
	private final long						seed;

	public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit,
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue,
			JFrame running, String[] interestingSpecies, String quantityType, String abstraction) throws IOException, XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue,
				running, interestingSpecies, quantityType, abstraction);

		initialize(randomSeed, 1);
		modelstateID = "topmodel";
		seed = randomSeed;
		this.print = true;
		this.buffer = null;
	}

	public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit,
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue,
			JFrame running, String[] interestingSpecies, String quantityType, String abstraction, boolean print) throws IOException,
			XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue,
				running, interestingSpecies, quantityType, abstraction);

		initialize(randomSeed, 1);

		this.modelstateID = "topmodel";
		this.print = print;
		this.seed = randomSeed;
		this.buffer = new HashMap<Double, List<Double>>();

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

	/**
	 * does minimized initalization process to prepare for a new run
	 */
	@Override
	public void setupForNewRun(int newRun)
	{
		currRun = newRun;
		setCurrentTime(0.0);
		setConstraintFlag(true);
		setupForOutput(newRun);

		// TODO: calculate init values
		getTopmodel().restoreInitValues();
		setupPropensities(getTopmodel());

		for (ModelState model : getSubmodels().values())
		{
			model.restoreInitValues();
			setupPropensities(getTopmodel());
		}

		try
		{
			setupVariableFromTSD();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void simulate()
	{

		double r1 = 0, r2 = 0, totalPropensity, delta_t, nextReactionTime, previousTime, printTime, nextEventTime;

		if (isSbmlHasErrorsFlag())
		{
			return;
		}

		printTime = 0;

		previousTime = 0;

		nextEventTime = handleEvents();

		fireEvents();

		printTime = print(printTime);

		while (getCurrentTime() < getTimeLimit() && !isCancelFlag())
		{
			r1 = getRandomNumberGenerator().nextDouble();
			r2 = getRandomNumberGenerator().nextDouble();

			totalPropensity = getTotalPropensity();

			delta_t = FastMath.log(1 / r1) / totalPropensity;

			nextReactionTime = getCurrentTime() + delta_t;
			nextEventTime = handleEvents();
			previousTime = getCurrentTime();

			if (nextReactionTime < nextEventTime && nextReactionTime < getCurrentTime() + getMaxTimeStep())
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
				break;
			}

			if (getCurrentTime() == nextReactionTime)
			{
				update(true, true, true, false, r2, previousTime);

				printTime = print(printTime);
			}
			else if (getCurrentTime() == nextEventTime)
			{
				update(false, true, true, true, r2, previousTime);
				printTime = print(printTime);
			}
			else
			{
				update(false, true, true, false, r2, previousTime);
				printTime = print(printTime);
			}
		}

		if (!isCancelFlag())
		{
			setCurrentTime(getTimeLimit());
			update(false, true, true, true, r2, previousTime);

			print(printTime);

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

	private boolean fireEvents()
	{
		boolean isFired = false;
		if (getTopmodel().isNoEventsFlag() == false)
		{
			Set<String> affectedReactionSet = fireEvents(getTopmodel(), "reaction", getTopmodel().isNoRuleFlag(), getTopmodel().isNoConstraintsFlag());
			if (affectedReactionSet.size() > 0)
			{
				Set<String> affectedSpecies = updatePropensities(affectedReactionSet, getTopmodel());
				perculateDown(getTopmodel(), affectedSpecies);
				isFired = true;
			}
		}

		for (ModelState models : getSubmodels().values())
		{
			if (models.isNoEventsFlag() == false)
			{
				Set<String> affectedReactionSet = fireEvents(models, "reaction", models.isNoRuleFlag(), models.isNoConstraintsFlag());
				if (affectedReactionSet.size() > 0)
				{
					Set<String> affectedSpecies = updatePropensities(affectedReactionSet, models);
					perculateUp(models, affectedSpecies);
					isFired = true;
				}
			}
		}

		return isFired;

	}

	private int getPercentage()
	{

		if (getTotalRuns() == 1)
		{
			double timePerc = getCurrentTime() / getTimeLimit();
			return (int) (timePerc * 100);
		}
		else
		{
			double runPerc = 1.0 * currRun / getTotalRuns();
			return (int) (runPerc * 100);
		}
	}

	/**
	 * sets up data structures
	 */
	private void initialize(long randomSeed, int runNumber) throws IOException
	{
		currRun = runNumber;

		setCurrentTime(0.0);
		setRandomNumberGenerator(new Random(randomSeed));

		initialize(getTopmodel());

		for (ModelState model : getSubmodels().values())
		{
			initialize(model);
		}

		setupArrayedModels();
		setupForOutput(runNumber);
		setupVariableFromTSD();
	}

	private void initialize(ModelState model) throws IOException
	{
		setupNonConstantSpeciesReferences(model);
		setupSpecies(model);
		setupParameters(model);
		setupCompartments(model);
		setupArraysValues(model);
		setupReactions(model);
		setupEvents(model);
		setupConstraints(model);
		setupRules(model);
		setupInitialAssignments(model);
		setupArraysPropensities(model);
		model.setInitValues();
	}

	private void perculateDown(ModelState modelstate, Set<String> affectedSpecies)
	{
		for (String species : affectedSpecies)
		{
			Set<HierarchicalStringPair> pairs = modelstate.getSpeciesToReplacement().get(species);
			if (pairs != null)
			{
				for (HierarchicalStringPair pair : pairs)
				{
					updatePropensity(pair.string1, pair.string2);
				}
			}
		}
	}

	private void perculateUp(ModelState modelstate, Set<String> affectedSpecies)
	{
		Set<String> updatedTopSpecies = new HashSet<String>();
		for (String species : affectedSpecies)
		{
			Set<HierarchicalStringPair> pairs = modelstate.getSpeciesToReplacement().get(species);
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

	/**
	 * performs every rate rule using the current time step
	 * 
	 * @param delta_t
	 * @return
	 */
	private void performRateRules(ModelState modelstate, double delta_t)
	{
		for (Rule rule : modelstate.getRateRulesList())
		{
			RateRule rateRule = (RateRule) rule;
			String variable = rateRule.getVariable();

			if (modelstate.getVariableToIsConstantMap().containsKey(variable) && modelstate.getVariableToIsConstantMap().get(variable) == false)
			{
				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable)
						&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false)
				{
					double currVal = modelstate.getVariableToValue(getReplacements(), variable);
					double incr = delta_t
							* (Evaluator.evaluateExpressionRecursive(modelstate, rateRule.getMath(), false, getCurrentTime(), null, null,
									getReplacements()) * modelstate.getVariableToValue(getReplacements(), modelstate.getSpeciesToCompartmentNameMap()
									.get(variable)));
					modelstate.setVariableToValue(getReplacements(), variable, currVal + incr);
				}
				else
				{
					double currVal = modelstate.getVariableToValue(getReplacements(), variable);
					double incr = delta_t
							* Evaluator.evaluateExpressionRecursive(modelstate, rateRule.getMath(), false, getCurrentTime(), null, null,
									getReplacements());

					modelstate.setVariableToValue(getReplacements(), variable, currVal + incr);
				}
			}
		}
	}

	/**
	 * Returns the total propensity of all model states.
	 */
	protected double getTotalPropensity()
	{
		double totalPropensity = 0;

		totalPropensity += getTopmodel().getPropensity();

		for (ModelState model : getSubmodels().values())
		{
			totalPropensity += model.getPropensity();
		}

		return totalPropensity > 0 ? totalPropensity : 0;
	}

	protected HashSet<String> getAffectedReactionSet(ModelState modelstate, String selectedReactionID, boolean noAssignmentRulesFlag)
	{

		HashSet<String> affectedReactionSet = new HashSet<String>(20);
		affectedReactionSet.add(selectedReactionID);

		// loop through the reaction's reactants and products
		for (HierarchicalStringDoublePair speciesAndStoichiometry : modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(selectedReactionID))
		{

			String speciesID = speciesAndStoichiometry.string;
			affectedReactionSet.addAll(modelstate.getSpeciesToAffectedReactionSetMap().get(speciesID));

			// if the species is involved in an assignment rule then it its
			// changing may affect a reaction's propensity
			if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(speciesID))
			{

				// this assignment rule is going to be evaluated, so the rule's
				// variable's value will change
				for (AssignmentRule assignmentRule : modelstate.getVariableToAffectedAssignmentRuleSetMap().get(speciesID))
				{
					if (modelstate.getSpeciesToAffectedReactionSetMap().get(assignmentRule.getVariable()) != null)
					{
						affectedReactionSet.addAll(modelstate.getSpeciesToAffectedReactionSetMap().get(assignmentRule.getVariable()));
					}
				}
			}
		}

		return affectedReactionSet;
	}

	private void performReaction(ModelState modelstate, String selectedReactionID, final boolean noAssignmentRulesFlag,
			final boolean noConstraintsFlag, int[] dims)
	{

		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		for (HierarchicalStringDoublePair speciesAndStoichiometry : modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(selectedReactionID))
		{

			double stoichiometry = speciesAndStoichiometry.doub;

			String speciesID;

			if (dims == null)
			{
				speciesID = speciesAndStoichiometry.string;
			}
			else
			{
				speciesID = getIndexedSpeciesReference(modelstate, selectedReactionID, speciesAndStoichiometry.string, dims);
			}

			if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(selectedReactionID))
			{

				for (HierarchicalStringPair doubleID : modelstate.getReactionToNonconstantStoichiometriesSetMap().get(selectedReactionID))
				{
					if (doubleID.string1.equals(speciesID))
					{

						stoichiometry = modelstate.getVariableToValue(getReplacements(), doubleID.string2);

						stoichiometry *= (int) (speciesAndStoichiometry.doub / Math.abs(speciesAndStoichiometry.doub));
						break;
					}
				}
			}

			// update the species count if the species isn't a boundary
			// condition or constant
			// note that the stoichiometries are earlier modified with the
			// correct +/- sign
			boolean cond1 = modelstate.getSpeciesToIsBoundaryConditionMap().get(speciesID);
			boolean cond2 = modelstate.getVariableToIsConstantMap().get(speciesID);
			if (!cond1 && !cond2)
			{

				double val = modelstate.getVariableToValue(getReplacements(), speciesID) + stoichiometry;
				if (val >= 0)
				{
					modelstate.setVariableToValue(getReplacements(), speciesID, val);
				}

			}

			// if this variable that was just updated is part of an assignment
			// rule (RHS)
			// then re-evaluate that assignment rule
			if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(speciesID) == true)
			{
				affectedAssignmentRuleSet.addAll(modelstate.getVariableToAffectedAssignmentRuleSetMap().get(speciesID));
			}

			if (noConstraintsFlag == false && modelstate.getVariableToIsInConstraintMap().get(speciesID) == true)
			{
				affectedConstraintSet.addAll(modelstate.getVariableToAffectedConstraintSetMap().get(speciesID));
			}
		}

		if (affectedAssignmentRuleSet.size() > 0)
		{
			performAssignmentRules(modelstate, affectedAssignmentRuleSet);
		}

		if (affectedConstraintSet.size() > 0)
		{
			setConstraintFlag(testConstraints(modelstate, affectedConstraintSet));
		}
		affectedAssignmentRuleSet = null;
		affectedConstraintSet = null;
	}

	private void performReaction(double r2)
	{
		String selectedReactionID = selectReaction(r2);

		String[] dimensions = selectedReactionID.contains("[") ? selectedReactionID.replace("]", "").split("\\[") : null;

		if (!selectedReactionID.isEmpty())
		{
			if (dimensions == null)
			{
				performReaction(selectedReactionID);
			}
			else
			{
				performReaction(dimensions[0], dimensions);
			}
		}
	}

	private void performReaction(String selectedReactionID)
	{
		if (modelstateID.equals("topmodel"))
		{
			if (!selectedReactionID.isEmpty())
			{
				performReaction(getTopmodel(), selectedReactionID, getTopmodel().isNoRuleFlag(), getTopmodel().isNoConstraintsFlag(), null);
				Set<String> affectedReactionSet = getAffectedReactionSet(getTopmodel(), selectedReactionID, true);
				Set<String> affectedSpecies = updatePropensities(affectedReactionSet, getTopmodel());
				perculateDown(getTopmodel(), affectedSpecies);
			}
		}
		else
		{
			if (!selectedReactionID.isEmpty())
			{
				ModelState modelstate = getSubmodels().get(modelstateID);
				performReaction(modelstate, selectedReactionID, modelstate.isNoRuleFlag(), modelstate.isNoConstraintsFlag(), null);
				Set<String> affectedReactionSet = getAffectedReactionSet(modelstate, selectedReactionID, true);
				Set<String> affectedSpecies = updatePropensities(affectedReactionSet, modelstate);
				perculateUp(modelstate, affectedSpecies);
			}
		}
	}

	private void performReaction(String selectedReactionID, String[] dimensions)
	{

		int[] dims = new int[dimensions.length - 1];

		for (int i = 1; i < dimensions.length; i++)
		{
			dims[i - 1] = Integer.parseInt(dimensions[i]);
		}

		String arrayedId = getArrayedID(getTopmodel(), selectedReactionID, dims);

		performReaction(getTopmodel(), selectedReactionID, getTopmodel().isNoRuleFlag(), getTopmodel().isNoConstraintsFlag(), dims);

		Set<String> affectedReactionSet = getAffectedReactionSet(getTopmodel(), arrayedId, true);
		updatePropensities(affectedReactionSet, getTopmodel());
	}

	private double print(double printTime)
	{
		if (print)
		{
			while (getCurrentTime() >= printTime && printTime < getTimeLimit())
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

				if (getRunning() != null)
				{
					int perc = getPercentage();
					getRunning().setTitle("Progress (" + perc + "%)");
					getProgress().setValue(perc);
				}

			}
		}
		return printTime;
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

		for (String currentReaction : getTopmodel().getReactionToPropensityMap().keySet())
		{
			runningTotalReactionsPropensity += getTopmodel().getReactionToPropensityMap().get(currentReaction);

			if (randomPropensity < runningTotalReactionsPropensity)
			{
				selectedReaction = currentReaction;
				modelstateID = "topmodel";
				return selectedReaction;
			}
		}

		for (ModelState models : getSubmodels().values())
		{
			for (String currentReaction : models.getReactionToPropensityMap().keySet())
			{
				runningTotalReactionsPropensity += models.getReactionToPropensityMap().get(currentReaction);

				if (randomPropensity < runningTotalReactionsPropensity)
				{
					selectedReaction = currentReaction;
					modelstateID = models.getID();
					return selectedReaction;
				}
			}
		}

		return selectedReaction;
	}

	private void setupArraysPropensities(ModelState modelstate)
	{
		for (String reaction : modelstate.getReactionToFormulaMap().keySet())
		{
			setupArraysPropensity(modelstate, reaction);
		}
	}

	private void setupArraysPropensity(ModelState modelstate, String reactionId)
	{
		if (modelstate.isArrayed(reactionId))
		{
			int[] sizes = new int[modelstate.getDimensionCount(reactionId)];
			for (ArraysObject obj : modelstate.getDimensionObjects().get(reactionId))
			{
				sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(), obj.getSize()) - 1;
			}
			int[] indices = new int[sizes.length];
			setupArraysPropensity(modelstate, reactionId, sizes, indices);
			indices = null;
		}
	}

	private void setupArraysPropensity(ModelState modelstate, String reactionId, int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{

			String arrayedId = getArrayedID(modelstate, reactionId, indices);

			if (arrayedId != null)
			{
				setupPropensity(modelstate, reactionId, indices);
			}

			indices[0]++;
			for (int i = 0; i < indices.length - 1; i++)
			{
				if (indices[i] > sizes[i])
				{
					indices[i] = 0;
					indices[i + 1]++;
				}
			}
		}
	}

	private void setupPropensity(ModelState modelstate, String id, int[] indices)
	{
		String arrayedId = getArrayedID(modelstate, id, indices);

		Set<HierarchicalStringDoublePair> speciesSet = modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(id);

		if (modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(arrayedId) == null)
		{
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(arrayedId, new HashSet<HierarchicalStringDoublePair>());
		}

		for (HierarchicalStringDoublePair speciesAndStoichiometry : speciesSet)
		{
			String speciesID = getIndexedSpeciesReference(modelstate, id, speciesAndStoichiometry.string, indices);
			modelstate.getSpeciesToAffectedReactionSetMap().get(speciesID).add(arrayedId);

			modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(arrayedId)
					.add(new HierarchicalStringDoublePair(speciesID, speciesAndStoichiometry.doub));
		}

		updatePropensity(modelstate, id, indices);

	}

	private void update(boolean reaction, boolean assignRule, boolean rateRule, boolean events, double r2, double previousTime)
	{
		if (reaction)
		{
			performReaction(r2);
		}

		if (events)
		{
			fireEvents();
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

	private Set<String> updatePropensities(Set<String> affectedReactionSet, ModelState modelstate)
	{

		Set<String> affectedSpecies = new HashSet<String>();

		for (String affectedReactionID : affectedReactionSet)
		{

			if (modelstate.isDeletedByMetaID(affectedReactionID))
			{
				continue;
			}

			String[] dimensions = affectedReactionID.contains("[") ? affectedReactionID.replace("]", "").split("\\[") : null;

			if (modelstate.isArrayed(affectedReactionID))
			{
				continue;
			}
			else if (dimensions != null)
			{
				int[] dims = new int[dimensions.length - 1];

				for (int i = 1; i < dimensions.length; i++)
				{
					dims[i - 1] = Integer.parseInt(dimensions[i]);
				}

				updatePropensity(modelstate, dimensions[0], dims);
			}
			else
			{
				Set<HierarchicalStringDoublePair> reactantStoichiometrySet = modelstate.getReactionToReactantStoichiometrySetMap().get(
						affectedReactionID);
				updatePropensity(modelstate, affectedReactionID, reactantStoichiometrySet, affectedSpecies);
			}
		}

		return affectedSpecies;
	}

	private void updatePropensity(ModelState modelstate, String id, int[] indices)
	{
		String arrayedId = getArrayedID(modelstate, id, indices);

		Set<HierarchicalStringDoublePair> reactantStoichiometrySet = modelstate.getReactionToReactantStoichiometrySetMap().get(id);

		modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(id);

		if (modelstate.getReactionToFormulaMap().get(id) == null)
		{
			return;
		}

		boolean notEnoughMoleculesFlag = false;

		for (HierarchicalStringDoublePair speciesAndStoichiometry : reactantStoichiometrySet)
		{
			String speciesID = getIndexedSpeciesReference(modelstate, id, speciesAndStoichiometry.string, indices);
			double stoichiometry = speciesAndStoichiometry.doub;

			if (modelstate.getVariableToValue(getReplacements(), speciesID) < stoichiometry)
			{
				notEnoughMoleculesFlag = true;
				break;
			}
		}

		double newPropensity = 0.0;

		double oldPropensity = modelstate.getPropensity(arrayedId);

		Map<String, Integer> dimensionIdMap = new HashMap<String, Integer>();

		for (int i = 0; i < indices.length; i++)
		{
			dimensionIdMap.put("d" + i, indices[i]);
		}
		if (notEnoughMoleculesFlag == false)
		{
			newPropensity = Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getReactionToFormulaMap().get(id), false, getCurrentTime(),
					null, dimensionIdMap, getReplacements());
		}
		modelstate.setPropensity(modelstate.getPropensity() + newPropensity - oldPropensity);

		if (newPropensity > 0)
		{
			modelstate.updateReactionToPropensityMap(arrayedId, newPropensity);
		}
		else
		{
			modelstate.getReactionToPropensityMap().remove(arrayedId);
		}
	}

	private void updatePropensity(ModelState model, String affectedReactionID, Set<HierarchicalStringDoublePair> reactantStoichiometrySet,
			Set<String> affectedSpecies)
	{

		Set<HierarchicalStringDoublePair> reactionToSpecies = model.getReactionToSpeciesAndStoichiometrySetMap().get(affectedReactionID);

		if (model.getReactionToFormulaMap().get(affectedReactionID) == null)
		{
			model.getReactionToPropensityMap().put(affectedReactionID, 0.0);
			return;
		}

		boolean notEnoughMoleculesFlag = false;

		for (HierarchicalStringDoublePair speciesAndStoichiometry : reactantStoichiometrySet)
		{
			String speciesID = speciesAndStoichiometry.string;

			double stoichiometry = speciesAndStoichiometry.doub;

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
			newPropensity = Evaluator.evaluateExpressionRecursive(model, model.getReactionToFormulaMap().get(affectedReactionID), false,
					getCurrentTime(), null, null, getReplacements());
		}

		double oldPropensity = model.getReactionToPropensityMap().get(affectedReactionID);
		model.setPropensity(model.getPropensity() + newPropensity - oldPropensity);
		model.updateReactionToPropensityMap(affectedReactionID, newPropensity);

	}

	private void updatePropensity(String modelstate, String species)
	{
		ModelState model = getModel(modelstate);
		Set<String> reactions = model.getSpeciesToAffectedReactionSetMap().get(species);
		updatePropensities(reactions, model);

	}

	@Override
	public void printStatisticsTSD()
	{
		// TODO Auto-generated method stub

	}
}
