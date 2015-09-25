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

import org.sbml.jsbml.ASTNode;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalSetup;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringPair;
import analysis.dynamicsim.hierarchical.util.io.HierarchicalWriter;

//TODO: assignment rules need to verify if changing a hierarchical species because they
//		can trigger rules in other places.

public class HierarchicalSSADirectSimulator extends HierarchicalSetup
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
		setModels(null);
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
			HierarchicalWriter.setupVariableFromTSD(getBufferedTSDWriter(), getTopmodel(), getSubmodels(), getInterestingSpecies());
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

		nextEventTime = HierarchicalUtilities.handleEvents(getCurrentTime(), getReplacements(), getTopmodel(), getSubmodels());

		fireEvents();

		printTime = print(printTime);

		while (getCurrentTime() < getTimeLimit() && !isCancelFlag())
		{
			r1 = getRandomNumberGenerator().nextDouble();
			r2 = getRandomNumberGenerator().nextDouble();

			totalPropensity = getTotalPropensity();

			delta_t = computeNextTimeStep(r1, totalPropensity);

			nextReactionTime = getCurrentTime() + delta_t;
			nextEventTime = HierarchicalUtilities.handleEvents(getCurrentTime(), getReplacements(), getTopmodel(), getSubmodels());
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

	public double computeNextTimeStep(double r1, double totalPropensity)
	{
		return Math.log(1 / r1) / totalPropensity;
	}

	private boolean fireEvents()
	{
		boolean isFired = false;
		if (getTopmodel().isNoEventsFlag() == false)
		{
			Set<String> affectedReactionSet = HierarchicalUtilities.fireEvents(getTopmodel(), HierarchicalUtilities.Selector.REACTION, getTopmodel()
					.isNoRuleFlag(), getTopmodel().isNoConstraintsFlag(), getCurrentTime(), getReplacements());
			if (affectedReactionSet.size() > 0)
			{
				Set<String> affectedSpecies = HierarchicalUtilities.updatePropensities(affectedReactionSet, getTopmodel(), getCurrentTime(),
						getReplacements());
				perculateDown(getTopmodel(), affectedSpecies);
				isFired = true;
			}
		}

		for (ModelState models : getSubmodels().values())
		{
			if (models.isNoEventsFlag() == false)
			{
				Set<String> affectedReactionSet = HierarchicalUtilities.fireEvents(models, HierarchicalUtilities.Selector.REACTION,
						models.isNoRuleFlag(), models.isNoConstraintsFlag(), getCurrentTime(), getReplacements());
				if (affectedReactionSet.size() > 0)
				{
					Set<String> affectedSpecies = HierarchicalUtilities.updatePropensities(affectedReactionSet, models, getCurrentTime(),
							getReplacements());
					perculateUp(models, affectedSpecies);
					isFired = true;
				}
			}
		}

		return isFired;

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
		HierarchicalWriter.setupVariableFromTSD(getBufferedTSDWriter(), getTopmodel(), getSubmodels(), getInterestingSpecies());
	}

	private void initialize(ModelState model) throws IOException
	{
		setupNonConstantSpeciesReferences(model);
		setupParameters(model);
		setupCompartments(model);
		setupSpecies(model);
		setupReactions(model);
		setupEvents(model);
		setupConstraints(model);
		setupRules(model);
		setupInitialAssignments(model);
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
					ModelState model = getModelState(pair.string1);
					Set<String> reactions = model.getSpeciesToAffectedReactionSetMap().get(pair.string2);
					HierarchicalUtilities.updatePropensities(reactions, model, getCurrentTime(), getReplacements());
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
					ModelState model = getModelState(pair.string1);
					Set<String> reactions = model.getSpeciesToAffectedReactionSetMap().get(pair.string2);
					HierarchicalUtilities.updatePropensities(reactions, model, getCurrentTime(), getReplacements());
					updatedTopSpecies.add(pair.string2);
				}
			}
		}
		perculateDown(getTopmodel(), updatedTopSpecies);
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

	protected HashSet<String> getAffectedReactionSet(ModelState modelstate, String selectedReactionID, int[] dims, boolean noAssignmentRulesFlag)
	{

		HashSet<String> affectedReactionSet = new HashSet<String>(20);
		affectedReactionSet.add(selectedReactionID);
		HierarchicalUtilities.getVariableFromArray(selectedReactionID);

		for (HierarchicalStringDoublePair speciesAndStoichiometry : modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(selectedReactionID))
		{

			String speciesID = speciesAndStoichiometry.string;
			affectedReactionSet.addAll(modelstate.getSpeciesToAffectedReactionSetMap().get(speciesID));

			if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(speciesID))
			{

				for (String variable : modelstate.getVariableToAffectedAssignmentRuleSetMap().get(speciesID))
				{
					if (modelstate.getSpeciesToAffectedReactionSetMap().get(variable) != null)
					{
						affectedReactionSet.addAll(modelstate.getSpeciesToAffectedReactionSetMap().get(variable));
					}
				}
			}
		}

		return affectedReactionSet;
	}

	private void performReaction(ModelState modelstate, String selectedReactionID, final boolean noAssignmentRulesFlag,
			final boolean noConstraintsFlag, int[] dims)
	{

		HashSet<String> affectedAssignmentRuleSet = new HashSet<String>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		for (HierarchicalStringDoublePair speciesAndStoichiometry : modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(selectedReactionID))
		{
			if (dims == null)
			{
				performReaction(modelstate, selectedReactionID, speciesAndStoichiometry.string, speciesAndStoichiometry, noAssignmentRulesFlag,
						noConstraintsFlag, affectedAssignmentRuleSet, affectedConstraintSet);
			}
			else
			{
				List<String> listOfSpecies;

				if (speciesAndStoichiometry.doub < 0)
				{
					if (selectedReactionID.endsWith("_rv"))
					{
						listOfSpecies = HierarchicalUtilities.getIndexedSpeciesReference(modelstate, selectedReactionID, "product",
								speciesAndStoichiometry.string, dims, getCurrentTime(), getReplacements());
					}
					else
					{
						listOfSpecies = HierarchicalUtilities.getIndexedSpeciesReference(modelstate, selectedReactionID, "reactant",
								speciesAndStoichiometry.string, dims, getCurrentTime(), getReplacements());
					}

				}
				else
				{
					if (selectedReactionID.endsWith("_rv"))
					{
						listOfSpecies = HierarchicalUtilities.getIndexedSpeciesReference(modelstate, selectedReactionID, "reactant",
								speciesAndStoichiometry.string, dims, getCurrentTime(), getReplacements());
					}
					else
					{
						listOfSpecies = HierarchicalUtilities.getIndexedSpeciesReference(modelstate, selectedReactionID, "product",
								speciesAndStoichiometry.string, dims, getCurrentTime(), getReplacements());
					}
				}

				for (String speciesID : listOfSpecies)
				{
					performReaction(modelstate, selectedReactionID, speciesID, speciesAndStoichiometry, noAssignmentRulesFlag, noConstraintsFlag,
							affectedAssignmentRuleSet, affectedConstraintSet);
				}

				listOfSpecies = null;
			}
		}

		if (affectedAssignmentRuleSet.size() > 0)
		{
			HierarchicalUtilities.performAssignmentRules(modelstate, affectedAssignmentRuleSet, getReplacements(), getCurrentTime());
		}

		if (affectedConstraintSet.size() > 0)
		{
			setConstraintFlag(HierarchicalUtilities.testConstraints(modelstate, affectedConstraintSet, getCurrentTime(), getReplacements()));
		}
		affectedAssignmentRuleSet = null;
		affectedConstraintSet = null;
	}

	private void performReaction(ModelState modelstate, String selectedReactionID, String speciesID,
			HierarchicalStringDoublePair speciesAndStoichiometry, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag,
			HashSet<String> affectedAssignmentRuleSet, HashSet<ASTNode> affectedConstraintSet)
	{
		double stoichiometry = speciesAndStoichiometry.doub;
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
		String referencedSpecies = HierarchicalUtilities.getReferencedVariable(speciesID);

		if (!modelstate.getSpeciesToIsBoundaryConditionMap().get(referencedSpecies)
				&& !modelstate.getSpeciesToIsBoundaryConditionMap().get(referencedSpecies))
		{
			double val = modelstate.getVariableToValue(getReplacements(), speciesID) + stoichiometry;
			if (val >= 0)
			{
				modelstate.setVariableToValue(getReplacements(), speciesID, val);
			}
		}
		if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(speciesID) == true)
		{
			affectedAssignmentRuleSet.addAll(modelstate.getVariableToAffectedAssignmentRuleSetMap().get(speciesID));
		}

		if (noConstraintsFlag == false && modelstate.getVariableToIsInConstraintMap().get(speciesID) == true)
		{
			affectedConstraintSet.addAll(modelstate.getVariableToAffectedConstraintSetMap().get(speciesID));
		}
	}

	private void selectAndPerformReaction(double r2)
	{
		String selectedReactionID = selectReaction(r2);
		String reactionID = HierarchicalUtilities.getVariableFromArray(selectedReactionID);
		int[] dims = HierarchicalUtilities.getIndicesFromVariable(selectedReactionID);
		if (!selectedReactionID.isEmpty())
		{
			if (dims == null)
			{
				performReaction(selectedReactionID);
			}
			else
			{
				performReaction(reactionID, dims);
				dims = null;
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
				Set<String> affectedReactionSet = getAffectedReactionSet(getTopmodel(), selectedReactionID, null, true);
				Set<String> affectedSpecies = HierarchicalUtilities.updatePropensities(affectedReactionSet, getTopmodel(), getCurrentTime(),
						getReplacements());
				perculateDown(getTopmodel(), affectedSpecies);
			}
		}
		else
		{
			if (!selectedReactionID.isEmpty())
			{
				ModelState modelstate = getSubmodels().get(modelstateID);
				performReaction(modelstate, selectedReactionID, modelstate.isNoRuleFlag(), modelstate.isNoConstraintsFlag(), null);
				Set<String> affectedReactionSet = getAffectedReactionSet(modelstate, selectedReactionID, null, true);
				Set<String> affectedSpecies = HierarchicalUtilities.updatePropensities(affectedReactionSet, modelstate, getCurrentTime(),
						getReplacements());
				perculateUp(modelstate, affectedSpecies);
			}
		}
	}

	private void performReaction(String selectedReactionID, int[] dims)
	{

		String arrayedId = HierarchicalUtilities.getArrayedID(getTopmodel(), selectedReactionID, dims);

		performReaction(getTopmodel(), selectedReactionID, getTopmodel().isNoRuleFlag(), getTopmodel().isNoConstraintsFlag(), dims);

		Set<String> affectedReactionSet = getAffectedReactionSet(getTopmodel(), arrayedId, dims, true);

		HierarchicalUtilities.updatePropensities(affectedReactionSet, getTopmodel(), getCurrentTime(), getReplacements());
	}

	private double print(double printTime)
	{
		if (print)
		{
			while (getCurrentTime() >= printTime && printTime < getTimeLimit())
			{
				try
				{
					HierarchicalWriter.printToTSD(getBufferedTSDWriter(), getTopmodel(), getSubmodels(), getReplacements(), getInterestingSpecies(),
							getPrintConcentrationSpecies(), printTime);
					getBufferedTSDWriter().write(",\n");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				printTime += getPrintInterval();

				if (getRunning() != null)
				{
					int perc = HierarchicalUtilities.getPercentage(getTotalRuns(), currRun, getCurrentTime(), getTimeLimit());
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
	public String selectReaction(double r2)
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

	private void update(boolean reaction, boolean assignRule, boolean rateRule, boolean events, double r2, double previousTime)
	{
		if (reaction)
		{
			selectAndPerformReaction(r2);
		}

		if (events)
		{
			fireEvents();
		}

		if (assignRule)
		{
			HierarchicalUtilities.performAssignmentRules(getTopmodel(), getReplacements(), getCurrentTime());

			for (ModelState modelstate : getSubmodels().values())
			{
				HierarchicalUtilities.performAssignmentRules(modelstate, getReplacements(), getCurrentTime());
			}
		}

		if (rateRule)
		{
			HierarchicalUtilities.performEulerRateRules(getTopmodel(), getCurrentTime(), getCurrentTime() - previousTime, getReplacements());

			for (ModelState modelstate : getSubmodels().values())
			{
				HierarchicalUtilities.performEulerRateRules(modelstate, getCurrentTime(), getCurrentTime() - previousTime, getReplacements());
			}
		}
	}

	@Override
	public void printStatisticsTSD()
	{
		// TODO Auto-generated method stub

	}
}
