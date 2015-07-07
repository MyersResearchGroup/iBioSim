package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.sbml.jsbml.ASTNode;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalFunctions;
import analysis.dynamicsim.hierarchical.util.Evaluator;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringPair;

public final class HierarchicalODERKSimulator extends HierarchicalFunctions
{

	private final DiffEquations										function;
	private double[]												globalValues;
	private double													nextEventTime;
	private int														numSteps;
	private final boolean											print;
	private double													relativeError, absoluteError;
	private final Map<String, Map<String, Map<Double, Boolean>>>	triggerValues;

	public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit,
			double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, int numSteps, double relError, double absError, String quantityType, String abstraction) throws IOException,
			XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, 0.0, progress, printInterval, stoichAmpValue, running,
				interestingSpecies, quantityType, abstraction);

		this.numSteps = numSteps;
		this.relativeError = relError;
		this.absoluteError = absError;
		this.print = true;
		this.triggerValues = new HashMap<String, Map<String, Map<Double, Boolean>>>();
		this.function = new DiffEquations(new VariableState());
		this.globalValues = null;

		try
		{
			initialize(randomSeed, 1);
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
		}

	}

	public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit,
			double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, int numSteps, double relError, double absError, String quantityType, String abstraction, boolean print)
			throws IOException, XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, 0.0, progress, printInterval, stoichAmpValue, running,
				interestingSpecies, quantityType, abstraction);

		this.numSteps = numSteps;
		this.relativeError = relError;
		this.absoluteError = absError;
		this.print = print;
		this.triggerValues = new HashMap<String, Map<String, Map<Double, Boolean>>>();
		this.function = new DiffEquations(new VariableState());

		try
		{
			initialize(randomSeed, 1);
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
		}

	}

	@Override
	public void cancel()
	{
		setCancelFlag(true);

	}

	@Override
	public void clear()
	{

	}

	@Override
	public void printStatisticsTSD()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setupForNewRun(int newRun)
	{

	}

	@Override
	public void simulate()
	{
		int currSteps = 0;
		double nextEndTime = 0, printTime = 0;

		if (isSbmlHasErrorsFlag())
		{
			return;
		}

		setCurrentTime(0);

		if (absoluteError == 0)
		{
			absoluteError = 1e-12;
		}

		if (relativeError == 0)
		{
			relativeError = 1e-9;
		}

		if (numSteps == 0)
		{
			numSteps = (int) (getTimeLimit() / getPrintInterval());
		}

		nextEventTime = handleEvents();

		HighamHall54Integrator odecalc = new HighamHall54Integrator(getMinTimeStep(), getMaxTimeStep(), absoluteError, relativeError);
		TriggerEventHandlerObject trigger = new TriggerEventHandlerObject();
		TriggeredEventHandlerObject triggered = new TriggeredEventHandlerObject();

		odecalc.addEventHandler(trigger, getPrintInterval(), 1e-20, 10000);
		odecalc.addEventHandler(triggered, getPrintInterval(), 1e-20, 10000);
		globalValues = function.state.getValuesArray();

		fireEvent(getTopmodel());
		for (ModelState model : getSubmodels().values())
		{
			fireEvent(model);
		}

		while (getCurrentTime() <= getTimeLimit() && !isCancelFlag() && !isConstraintFlag())
		{

			nextEndTime = getCurrentTime() + getMaxTimeStep();

			if (nextEndTime > printTime)
			{
				nextEndTime = printTime;
			}

			if (getCurrentTime() < nextEndTime && Math.abs(getCurrentTime() - nextEndTime) > absoluteError)
			{
				odecalc.integrate(function, getCurrentTime(), globalValues, nextEndTime, globalValues);
			}
			else
			{
				setCurrentTime(nextEndTime);
			}

			if (print)
			{
				while (getCurrentTime() >= printTime && printTime <= getTimeLimit())
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

					currSteps++;
					printTime = (currSteps * getTimeLimit() / numSteps);

					if (getRunning() != null)
					{
						getRunning().setTitle("Progress (" + (int) ((getCurrentTime() / getTimeLimit()) * 100.0) + "%)");
					}
				}
			}

			if (getProgress() != null)
			{
				getProgress().setValue((int) ((printTime / getTimeLimit()) * 100.0));
			}
		}
		try
		{
			getBufferedTSDWriter().write(')');
			getBufferedTSDWriter().flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private HashSet<String> fireEvent(ModelState modelstate)
	{
		HashSet<String> vars = new HashSet<String>();

		String modelstateId = modelstate.getID();

		if (!modelstate.isNoEventsFlag())
		{
			int sizeBefore = modelstate.getTriggeredEventQueue().size();
			vars = fireEvents(modelstate, "variable", modelstate.isNoRuleFlag(), modelstate.isNoConstraintsFlag());
			int sizeAfter = modelstate.getTriggeredEventQueue().size();

			if (sizeAfter != sizeBefore)
			{
				for (String var : vars)
				{
					int index = function.state.variableToIndexMap.get(modelstateId).get(var);
					if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(var)
							&& !modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(var))
					{
						globalValues[index] = modelstate.getVariableToValue(getReplacements(), var)
								* modelstate.getVariableToValue(getReplacements(), modelstate.getSpeciesToCompartmentNameMap().get(var));
						modelstate.setVariableToValue(getReplacements(), var, globalValues[index]);
					}
					else
					{
						globalValues[index] = modelstate.getVariableToValue(getReplacements(), var);
					}
				}
			}
		}
		return vars;
	}

	private void initialize(long randomSeed, int runNumber) throws IOException
	{
		initializeModelStates();
		initializeODEStates();
		setupForOutput(runNumber);
		setupVariableFromTSD();
	}

	private void initializeModelStates() throws IOException
	{
		initialize(getTopmodel());
		for (ModelState model : getSubmodels().values())
		{
			initialize(model);
		}
	}

	private void initializeODEStates()
	{
		this.function.state.addState(getTopmodel());
		this.triggerValues.put(getTopmodel().getID(), new HashMap<String, Map<Double, Boolean>>());

		for (ModelState model : getSubmodels().values())
		{
			this.function.state.addState(model);
			this.triggerValues.put(model.getID(), new HashMap<String, Map<Double, Boolean>>());
		}
	}

	private void initialize(ModelState model) throws IOException
	{
		setupNonConstantSpeciesReferences(model);
		setupParameters(model);
		setupCompartments(model);
		setupSpecies(model);
		setupConstraints(model);
		setupRules(model);
		setupInitialAssignments(model);
		setupReactions(model);
		setupEvents(model);
	}

	private void updateStateAndFireReaction(double t, double[] y)
	{

		setCurrentTime(t);

		updateStateAndFireReaction(getTopmodel(), t, y);

		for (ModelState model : getSubmodels().values())
		{
			updateStateAndFireReaction(model, t, y);
		}
	}

	private void updateStateAndFireReaction(ModelState modelstate, double t, double[] y)
	{

		String modelstateId = modelstate.getID();

		for (int i : function.state.indexToVariableMap.get(modelstateId).keySet())
		{
			modelstate.setVariableToValue(getReplacements(), function.state.indexToVariableMap.get(modelstateId).get(i), y[i]);
		}

		fireEvents(modelstate, y, t);

	}

	private void fireEvents(ModelState modelstate, double[] y, double t)
	{
		String modelstateId = modelstate.getID();
		nextEventTime = handleEvents();

		updateTriggerState(modelstate, t);

		Set<String> variables = fireEvent(modelstate);

		for (String var : variables)
		{
			int index = function.state.variableToIndexMap.get(modelstateId).get(var);
			y[index] = globalValues[index];

		}

		nextEventTime = handleEvents();

		for (String event : modelstate.getUntriggeredEventSet())
		{
			if (triggerValues.get(modelstate.getID()).containsKey(event))
			{
				boolean state = Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(event), false, t, null, null,
						getReplacements()) > 0;

				triggerValues.get(modelstate.getID()).get(event).clear();

				triggerValues.get(modelstate.getID()).get(event).put(t, state);
			}
		}
	}

	private void updateTriggerState(ModelState modelstate, double t)
	{
		List<Double> clone = new ArrayList<Double>();

		for (String event : triggerValues.get(modelstate.getID()).keySet())
		{
			clone.addAll(triggerValues.get(modelstate.getID()).get(event).keySet());

			Collections.sort(clone);

			double tau = -1;

			for (int i = 0; i < clone.size(); ++i)
			{
				if (clone.get(i) >= t)
				{
					break;
				}

				tau = clone.get(i);
			}

			if (tau > 0 && !modelstate.isNoEventsFlag())
			{
				modelstate.getEventToPreviousTriggerValueMap().put(event, triggerValues.get(modelstate.getID()).get(event).get(tau));
			}
			clone.clear();
		}
	}

	private class DiffEquations implements FirstOrderDifferentialEquations
	{
		private VariableState	state;

		public DiffEquations(VariableState state)
		{
			this.state = state;
		}

		@Override
		public void computeDerivatives(double t, double[] y, double[] currValueChanges) throws MaxCountExceededException, DimensionMismatchException
		{

			setCurrentTime(t);

			computeDerivatives(getTopmodel(), t, y, currValueChanges);

			computeRateRules(getTopmodel(), t, currValueChanges);
			for (ModelState model : getSubmodels().values())
			{
				computeDerivatives(model, t, y, currValueChanges);

				computeRateRules(model, t, currValueChanges);
			}

			computeDependencies(currValueChanges);

		}

		@Override
		public int getDimension()
		{
			return state.getDimensions();
		}

		private void computeDerivatives(ModelState modelstate, double t, double[] y, double[] currValueChanges)
		{

			String modelstateId = modelstate.getID();
			HashSet<String> affectedAssignmentRuleSet = new HashSet<String>();
			HashSet<String> revaluateVariables = new HashSet<String>();

			for (int i : function.state.indexToVariableMap.get(modelstateId).keySet())
			{
				String variable = state.indexToVariableMap.get(modelstateId).get(i);
				modelstate.setVariableToValue(getReplacements(), variable, y[i]);
			}

			for (int i : function.state.indexToVariableMap.get(modelstateId).keySet())
			{
				String currentVar = state.indexToVariableMap.get(modelstateId).get(i);

				if (!modelstate.isConstant(currentVar))
				{
					if (modelstate.getSpeciesIDSet().contains(currentVar) && !modelstate.getSpeciesToIsBoundaryConditionMap().get(currentVar))
					{
						currValueChanges[i] = Evaluator.evaluateExpressionRecursive(modelstate,
								state.dvariablesdtime.get(modelstateId).get(currentVar), false, t, null, null, getReplacements());
						revaluateVariables.add(currentVar);
					}
				}

				if (modelstate.getVariableToIsInAssignmentRuleMap() != null
						&& modelstate.getVariableToIsInAssignmentRuleMap().containsKey(currentVar)
						&& modelstate.getVariableToValueMap().containsKey(currentVar)
						&& modelstate.getVariableToIsInAssignmentRuleMap().get(currentVar))
				{
					affectedAssignmentRuleSet.addAll(modelstate.getVariableToAffectedAssignmentRuleSetMap().get(currentVar));
					// revaluateVariables.add(currentVar);
				}
			}

			if (modelstate.isSetVariableToAffectedAssignmentRule() && modelstate.getVariableToAffectedAssignmentRuleSetMap().containsKey("_time"))
			{
				affectedAssignmentRuleSet.addAll(modelstate.getVariableToAffectedAssignmentRuleSetMap().get("_time"));
			}

			if (affectedAssignmentRuleSet.size() > 0)
			{
				computeAssignmentRules(modelstate, y, currValueChanges, affectedAssignmentRuleSet, revaluateVariables);
				affectedAssignmentRuleSet = null;
				revaluateVariables = null;
			}

		}

		private void computeAssignmentRules(ModelState modelstate, double[] y, double[] currValueChanges, HashSet<String> affectedAssignmentRuleSet,
				HashSet<String> revaluateVariables)
		{
			boolean changed = true;

			while (changed)
			{
				HashSet<String> affectedVariables = HierarchicalUtilities.performAssignmentRules(modelstate, affectedAssignmentRuleSet,
						getReplacements(), getCurrentTime());
				changed = false;
				for (String affectedVariable : affectedVariables)
				{
					int index = state.variableToIndexMap.get(modelstate.getID()).get(affectedVariable);

					double oldValue = y[index];
					double newValue = modelstate.getVariableToValue(getReplacements(), affectedVariable);

					if (Double.isNaN(newValue))
					{
						continue;
					}

					if (newValue != oldValue)
					{
						changed |= true;
						y[index] = newValue;
					}
				}

				for (String affectedVariable : revaluateVariables)
				{
					int index = state.variableToIndexMap.get(modelstate.getID()).get(affectedVariable);
					double oldValue = currValueChanges[index];
					double newValue = Evaluator.evaluateExpressionRecursive(modelstate,
							state.dvariablesdtime.get(modelstate.getID()).get(affectedVariable), false, getCurrentTime(), null, null,
							getReplacements());

					if (newValue != oldValue)
					{
						changed |= true;
						currValueChanges[index] = newValue;
					}
				}
			}
		}

		/**
		 * performs every rate rule using the current time step
		 */
		private void computeRateRules(ModelState modelstate, double t, double[] currValueChanges)
		{
			Map<String, Integer> variableToIndexMap = state.variableToIndexMap.get(modelstate.getID());

			for (String variable : modelstate.getRateRulesList().keySet())
			{
				ASTNode rateRule = modelstate.getRateRulesList().get(variable);
				ASTNode formula = HierarchicalUtilities.inlineFormula(modelstate, rateRule, getModels(), getIbiosimFunctionDefinitions());

				if (!modelstate.isConstant(variable))
				{

					if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable)
							&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false)
					{
						if (!variableToIndexMap.containsKey(variable))
						{
							continue;
						}

						int index = variableToIndexMap.get(variable);

						if (index > currValueChanges.length)
						{
							continue;
						}

						double newValue = Evaluator.evaluateExpressionRecursive(modelstate, formula, false, t, null, null, getReplacements())
								* modelstate.getVariableToValue(getReplacements(), modelstate.getSpeciesToCompartmentNameMap().get(variable));

						currValueChanges[index] = newValue;
					}
					else
					{
						if (!variableToIndexMap.containsKey(variable))
						{
							continue;
						}
						int index = variableToIndexMap.get(variable);
						if (index > currValueChanges.length)
						{
							continue;
						}
						double newValue = Evaluator.evaluateExpressionRecursive(modelstate, formula, false, t, null, null, getReplacements());
						if (modelstate.isHierarchical(variable))
						{
							String dependency = modelstate.getReplacementDependency().get(variable);

							for (String modelstateId : state.dependencyToDependents.get(dependency).keySet())
							{
								index = state.dependencyToDependents.get(dependency).get(modelstateId);
								currValueChanges[index] = 0;
							}
						}
						currValueChanges[index] = newValue;

					}
				}
			}

		}

		private void computeDependencies(double[] currValueChanges)
		{
			for (String dependency : state.dependencyToDependents.keySet())
			{
				double total = 0;

				for (String modelstate : state.dependencyToDependents.get(dependency).keySet())
				{
					int index = state.dependencyToDependents.get(dependency).get(modelstate);
					total = total + currValueChanges[index];
				}

				for (String modelstate : state.dependencyToDependents.get(dependency).keySet())
				{
					int index = state.dependencyToDependents.get(dependency).get(modelstate);
					currValueChanges[index] = total;
				}
			}
		}
	}

	private class TriggeredEventHandlerObject implements EventHandler
	{

		private double	value	= 1;

		@Override
		public Action eventOccurred(double t, double[] y, boolean increasing)
		{

			updateStateAndFireReaction(t, y);
			value = -value;

			return EventHandler.Action.STOP;
		}

		@Override
		public double g(double t, double[] y)
		{
			if (nextEventTime < t)
			{
				return -value;
			}
			else
			{
				return value;
			}
		}

		@Override
		public void init(double t0, double[] y, double t)
		{

		}

		@Override
		public void resetState(double t, double[] y)
		{

		}

	}

	private class TriggerEventHandlerObject implements EventHandler
	{
		private double	value	= 1;

		@Override
		public Action eventOccurred(double t, double[] y, boolean increasing)
		{
			value = -value;
			updateStateAndFireReaction(t, y);
			return EventHandler.Action.STOP;
		}

		@Override
		public double g(double t, double[] y)
		{

			if (checkTrigger(getTopmodel(), t, y))
			{
				return -value;
			}

			for (ModelState model : getSubmodels().values())
			{
				if (checkTrigger(model, t, y))
				{
					return -value;
				}
			}
			return value;
		}

		@Override
		public void init(double t0, double[] y, double t)
		{
		}

		@Override
		public void resetState(double t, double[] y)
		{

		}

		private boolean checkTrigger(ModelState modelstate, double t, double[] y)
		{
			boolean state;
			String modelstateId = modelstate.getID();

			for (String event : modelstate.getUntriggeredEventSet())
			{
				state = isEventTriggered(modelstate, event, t, y, function.state.variableToIndexMap.get(modelstateId));

				if (!triggerValues.get(modelstate.getID()).containsKey(event))
				{
					triggerValues.get(modelstate.getID()).put(event, new HashMap<Double, Boolean>());
				}

				triggerValues.get(modelstate.getID()).get(event).put(t, state);

				if (state)
				{
					if (triggerValues.get(modelstate.getID()).get(event).keySet().size() > 0)
					{
						double maxTime = Double.NEGATIVE_INFINITY;

						for (double time : triggerValues.get(modelstate.getID()).get(event).keySet())
						{
							if (time < t && time > maxTime)
							{
								maxTime = time;
							}
						}

						if (maxTime > 0)
						{
							if (!triggerValues.get(modelstate.getID()).get(event).get(maxTime))
							{
								return true;
							}
						}
					}
				}
			}

			return false;
		}

		private boolean isEventTriggered(ModelState modelstate, String event, double t, double[] y, Map<String, Integer> variableToIndexMap)
		{
			double givenState = Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(event), true, t, y,
					variableToIndexMap, getReplacements());

			if (givenState > 0)
			{
				return true;
			}

			return false;
		}
	}

	private class VariableState
	{
		private List<Double>						values;
		private List<String>						variables;
		private Map<String, ModelState>				idToModelState;
		private Map<String, Map<Integer, String>>	indexToVariableMap;
		private Map<String, Map<String, Integer>>	variableToIndexMap;
		private Map<String, Map<String, ASTNode>>	dvariablesdtime;
		private Map<String, Map<String, Integer>>	dependencyToDependents;

		public VariableState()
		{
			this.idToModelState = new HashMap<String, ModelState>();
			this.variableToIndexMap = new HashMap<String, Map<String, Integer>>();
			this.indexToVariableMap = new HashMap<String, Map<Integer, String>>();
			this.variables = new ArrayList<String>();
			this.values = new ArrayList<Double>();
			this.dvariablesdtime = new HashMap<String, Map<String, ASTNode>>();
			this.dependencyToDependents = new HashMap<String, Map<String, Integer>>();

		}

		public void addState(ModelState modelstate)
		{
			idToModelState.put(modelstate.getID(), modelstate);
			variableToIndexMap.put(modelstate.getID(), new HashMap<String, Integer>());
			indexToVariableMap.put(modelstate.getID(), new HashMap<Integer, String>());
			dvariablesdtime.put(modelstate.getID(), new HashMap<String, ASTNode>());

			for (String variable : modelstate.getVariableToValueMap().keySet())
			{
				addVariable(modelstate, variable);
			}

			for (String reaction : modelstate.getReactionToFormulaMap().keySet())
			{
				addReaction(modelstate, reaction);
			}
		}

		public int getDimensions()
		{
			return values.size();
		}

		public double[] getValuesArray()
		{
			double[] temp = new double[values.size()];
			for (int i = 0; i < temp.length; i++)
			{
				temp[i] = values.get(i);
			}
			return temp;
		}

		public String[] getVariablesArray()
		{
			return variables.toArray(new String[variables.size()]);
		}

		private void addVariable(ModelState modelstate, String variable)
		{
			int index = variables.size();

			if (modelstate.isHierarchical(variable))
			{
				String dependency = modelstate.getReplacementDependency().get(variable);

				if (!dependencyToDependents.containsKey(dependency))
				{
					dependencyToDependents.put(dependency, new HashMap<String, Integer>());
				}

				dependencyToDependents.get(dependency).put(modelstate.getID(), index);
			}

			variables.add(variable);
			values.add(modelstate.getVariableToValue(getReplacements(), variable));
			variableToIndexMap.get(modelstate.getID()).put(variable, index);
			indexToVariableMap.get(modelstate.getID()).put(index, variable);
			dvariablesdtime.get(modelstate.getID()).put(variable, new ASTNode(0));
		}

		private void addReaction(ModelState modelstate, String reaction)
		{
			String modelstateID = modelstate.getID();

			ASTNode formula = modelstate.getReactionToFormulaMap().get(reaction);
			Set<HierarchicalStringDoublePair> reactantAndStoichiometrySet = modelstate.getReactionToReactantStoichiometrySetMap().get(reaction);
			Set<HierarchicalStringDoublePair> speciesAndStoichiometrySet = modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reaction);
			Set<HierarchicalStringPair> nonConstantStoichiometrySet = modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reaction);

			if (reactantAndStoichiometrySet != null)
			{
				for (HierarchicalStringDoublePair reactantAndStoichiometry : reactantAndStoichiometrySet)
				{
					String reactant = reactantAndStoichiometry.string;
					double stoichiometry = reactantAndStoichiometry.doub;
					// int varIndex =
					// variableToIndexMap.get(modelstateID).get(reactant);
					ASTNode stoichNode = new ASTNode();
					stoichNode.setValue(-1 * stoichiometry);
					dvariablesdtime.get(modelstateID).put(reactant,
							ASTNode.sum(dvariablesdtime.get(modelstateID).get(reactant), ASTNode.times(formula, stoichNode)));
				}
			}

			if (speciesAndStoichiometrySet != null)
			{
				for (HierarchicalStringDoublePair speciesAndStoichiometry : speciesAndStoichiometrySet)
				{
					String species = speciesAndStoichiometry.string;
					double stoichiometry = speciesAndStoichiometry.doub;

					if (stoichiometry > 0)
					{
						// int varIndex =
						// variableToIndexMap.get(modelstate.getID()).get(species);
						ASTNode stoichNode = new ASTNode();
						stoichNode.setValue(stoichiometry);
						dvariablesdtime.get(modelstateID).put(species,
								ASTNode.sum(dvariablesdtime.get(modelstateID).get(species), ASTNode.times(formula, stoichNode)));
					}
				}
			}

			if (nonConstantStoichiometrySet != null)
			{
				for (HierarchicalStringPair reactantAndStoichiometry : nonConstantStoichiometrySet)
				{
					String reactant = reactantAndStoichiometry.string1;
					String stoichiometry = reactantAndStoichiometry.string2;
					// int varIndex =
					// variableToIndexMap.get(modelstate.getID()).get(reactant);
					if (stoichiometry.startsWith("-"))
					{
						ASTNode stoichNode = new ASTNode(stoichiometry.substring(1, stoichiometry.length()));
						dvariablesdtime.get(modelstateID).put(reactant,
								ASTNode.diff(dvariablesdtime.get(modelstateID).get(reactant), ASTNode.times(formula, stoichNode)));
					}
					else
					{
						ASTNode stoichNode = new ASTNode(stoichiometry);
						dvariablesdtime.get(modelstateID).put(reactant,
								ASTNode.sum(dvariablesdtime.get(modelstateID).get(reactant), ASTNode.times(formula, stoichNode)));
					}
				}
			}
		}
	}
}
