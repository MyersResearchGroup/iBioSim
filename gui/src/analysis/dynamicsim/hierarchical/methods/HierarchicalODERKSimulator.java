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
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.RateRule;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalArrayModels;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringPair;

public class HierarchicalODERKSimulator extends HierarchicalArrayModels
{

	private int														numSteps;
	private double													relativeError, absoluteError;
	private double													nextEventTime;
	private final boolean											print;
	private final DiffEquations										function;
	private final Map<String, Map<String, Map<Double, Boolean>>>	triggerValues;
	private double[]												globalValues;

	public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, double maxTimeStep,
			long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies,
			int numSteps, double relError, double absError, String quantityType, String abstraction) throws IOException, XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, 0.0, progress, printInterval, stoichAmpValue, running,
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

	public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, double maxTimeStep,
			long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies,
			int numSteps, double relError, double absError, String quantityType, String abstraction, boolean print) throws IOException,
			XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, 0.0, progress, printInterval, stoichAmpValue, running,
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

	private void initialize(long randomSeed, int runNumber) throws IOException
	{

		setupNonConstantSpeciesReferences(getTopmodel());
		setupSpecies(getTopmodel());
		setupParameters(getTopmodel());
		setupCompartments(getTopmodel());
		setupConstraints(getTopmodel());
		setupRules(getTopmodel());
		setupInitialAssignments(getTopmodel());
		setupReactions(getTopmodel());
		setupEvents(getTopmodel());
		setupForOutput(randomSeed, runNumber);
		this.function.state.addState(getTopmodel());
		this.triggerValues.put(getTopmodel().getID(), new HashMap<String, Map<Double, Boolean>>());
		for (ModelState model : getSubmodels().values())
		{
			setupNonConstantSpeciesReferences(model);
			setupSpecies(model);
			setupParameters(model);
			setupCompartments(model);
			setupConstraints(model);
			setupRules(model);
			setupInitialAssignments(model);
			setupReactions(model);
			setupEvents(model);
			setupForOutput(randomSeed, runNumber);
			this.function.state.addState(model);
			this.triggerValues.put(model.getID(), new HashMap<String, Map<Double, Boolean>>());
		}
		setupForOutput(randomSeed, runNumber);
		setupVariableFromTSD();
		this.function.state.addTime();
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

	@Override
	public void cancel()
	{
		setCancelFlag(true);

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

	private void updateStateAndFireReaction(double t, double[] y)
	{
		updateStateAndFireReaction(getTopmodel(), t, y);

		for (ModelState model : getSubmodels().values())
		{
			updateStateAndFireReaction(model, t, y);
		}
	}

	private void updateStateAndFireReaction(ModelState modelstate, double t, double[] y)
	{

		String modelstateId = modelstate.getID();
		setCurrentTime(t);

		for (int i : function.state.indexToVariableMap.get(modelstateId).keySet())
		{
			modelstate.setvariableToValueMap(getReplacements(), function.state.indexToVariableMap.get(modelstateId).get(i), y[i]);
		}

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
			boolean state = isEventTriggered(modelstate, event, t, y, function.state.variableToIndexMap.get(modelstateId), false);

			triggerValues.get(modelstate.getID()).get(event).clear();

			triggerValues.get(modelstate.getID()).get(event).put(t, state);
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
						modelstate.setvariableToValueMap(getReplacements(), var, globalValues[index]);
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

	@Override
	public void clear()
	{

	}

	@Override
	public void setupForNewRun(int newRun)
	{

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

		private boolean checkTrigger(ModelState modelstate, double t, double[] y)
		{
			boolean state;
			String modelstateId = modelstate.getID();
			for (String event : modelstate.getUntriggeredEventSet())
			{
				state = isEventTriggered(modelstate, event, t, y, function.state.variableToIndexMap.get(modelstateId), false);

				if (!triggerValues.get(modelstate.getID()).containsKey(event))
				{
					triggerValues.get(modelstate.getID()).put(event, new HashMap<Double, Boolean>());
				}

				triggerValues.get(modelstate.getID()).get(event).put(t, state);

				if (state)
				{
					if (triggerValues.get(modelstate.getID()).get(event).keySet().size() > 0)
					{
						double key = -1;
						double maxTime = Double.MIN_VALUE;

						for (double time : triggerValues.get(modelstate.getID()).get(event).keySet())
						{
							if (time < t && time > maxTime)
							{
								key = time;
							}
						}

						if (key > 0)
						{
							if (!triggerValues.get(modelstate.getID()).get(event).get(key))
							{
								return true;
							}
						}
					}
				}
			}

			return false;
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

	private class DiffEquations implements FirstOrderDifferentialEquations
	{
		VariableState	state;

		public DiffEquations(VariableState state)
		{
			this.state = state;
		}

		@Override
		public void computeDerivatives(double t, double[] y, double[] currValueChanges) throws MaxCountExceededException, DimensionMismatchException
		{
			computeDerivatives(getTopmodel(), t, y, currValueChanges);
			for (ModelState model : getSubmodels().values())
			{
				computeDerivatives(model, t, y, currValueChanges);
			}
			setCurrentTime(t);
		}

		private void computeDerivatives(ModelState modelstate, double t, double[] y, double[] currValueChanges)
		{
			String modelstateId = modelstate.getID();
			HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
			HashSet<String> revaluateVariables = new HashSet<String>();

			for (int i : function.state.indexToVariableMap.get(modelstateId).keySet())
			{
				modelstate.setvariableToValueMap(getReplacements(), state.indexToVariableMap.get(modelstateId).get(i), y[i]);
			}

			for (int i = 0; i < currValueChanges.length - 1; i++)
			{

				String currentVar = state.indexToVariableMap.get(modelstateId).get(i);

				if ((modelstate.getSpeciesIDSet().contains(currentVar) && modelstate.getSpeciesToIsBoundaryConditionMap().get(currentVar) == false)
						&& (modelstate.getVariableToValueMap().contains(currentVar))
						&& modelstate.getVariableToIsConstantMap().get(currentVar) == false)
				{
					currValueChanges[i] = evaluateExpressionRecursive(modelstate, state.dvariablesdtime.get(i), false, t, null, null);
					revaluateVariables.add(currentVar);
				}
				else
				{
					currValueChanges[i] = 0;
				}

				if (modelstate.getVariableToIsInAssignmentRuleMap() != null
						&& modelstate.getVariableToIsInAssignmentRuleMap().containsKey(currentVar)
						&& modelstate.getVariableToValueMap().contains(currentVar) && modelstate.getVariableToIsInAssignmentRuleMap().get(currentVar))
				{
					affectedAssignmentRuleSet.addAll(modelstate.getVariableToAffectedAssignmentRuleSetMap().get(currentVar));
					revaluateVariables.add(currentVar);
				}
			}
			currValueChanges[currValueChanges.length - 1] = t;
			modelstate.setvariableToValueMap(getReplacements(), "_time", t);
			if (affectedAssignmentRuleSet.size() > 0)
			{
				HashSet<String> affectedVariables;
				boolean changed = true;

				while (changed)
				{
					affectedVariables = performAssignmentRules(modelstate, affectedAssignmentRuleSet);
					changed = false;
					for (String affectedVariable : affectedVariables)
					{
						int index = state.variableToIndexMap.get(modelstateId).get(affectedVariable);
						double oldValue = y[index];
						double newValue = modelstate.getVariableToValue(getReplacements(), affectedVariable);

						if (newValue != oldValue)
						{
							changed |= true;
							y[index] = newValue;
						}

					}

					for (String affectedVariable : revaluateVariables)
					{
						int index = state.variableToIndexMap.get(modelstateId).get(affectedVariable);
						double oldValue = currValueChanges[index];
						double newValue = evaluateExpressionRecursive(modelstate, state.dvariablesdtime.get(index), false, getCurrentTime(), null,
								null);

						if (newValue != oldValue)
						{
							changed |= true;
							currValueChanges[index] = newValue;
						}
					}
				}
			}
			performRateRules(modelstate, t, state.variableToIndexMap.get(modelstateId), currValueChanges);
		}

		@Override
		public int getDimension()
		{
			return state.getDimensions();
		}

		/**
		 * performs every rate rule using the current time step
		 */
		private HashSet<String> performRateRules(ModelState modelstate, double t, Map<String, Integer> variableToIndexMap, double[] currValueChanges)
		{

			HashSet<String> affectedVariables = new HashSet<String>();
			for (RateRule rateRule : modelstate.getRateRulesList())
			{
				String variable = rateRule.getVariable();
				ASTNode formula = inlineFormula(modelstate, rateRule.getMath());

				if (modelstate.getVariableToIsConstantMap().containsKey(variable) && modelstate.getVariableToIsConstantMap().get(variable) == false)
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

						double newValue = (evaluateExpressionRecursive(modelstate, formula, false, t, null, null) * modelstate.getVariableToValue(
								getReplacements(), modelstate.getSpeciesToCompartmentNameMap().get(variable)));

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
						double newValue = evaluateExpressionRecursive(modelstate, formula, false, t, null, null);
						currValueChanges[index] = newValue;
					}

					affectedVariables.add(variable);
				}
			}

			return affectedVariables;
		}
	}

	private class VariableState
	{
		private List<String>						variables;
		private List<Double>						values;
		private List<ASTNode>						dvariablesdtime;
		private Map<String, Map<String, Integer>>	variableToIndexMap;
		private Map<String, Map<Integer, String>>	indexToVariableMap;
		private Map<String, ModelState>				idToModelState;

		public VariableState()
		{
			this.idToModelState = new HashMap<String, ModelState>();
			this.variableToIndexMap = new HashMap<String, Map<String, Integer>>();
			this.indexToVariableMap = new HashMap<String, Map<Integer, String>>();
			this.variables = new ArrayList<String>();
			this.values = new ArrayList<Double>();
			this.dvariablesdtime = new ArrayList<ASTNode>();
		}

		public String[] getVariablesArray()
		{
			return variables.toArray(new String[variables.size()]);
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

		public ASTNode[] getRateArray()
		{
			return dvariablesdtime.toArray(new ASTNode[dvariablesdtime.size()]);
		}

		public int getDimensions()
		{
			return values.size();
		}

		public void addTime()
		{

			//
			// indexToVariableMap.put(index, "time");
			// variableToIndexMap.put("time", index);
			variables.add("time");
			values.add(0.0);
			// modelstate.getVariableToValueMap().put("time", 0);
		}

		public void addState(ModelState modelstate)
		{
			int index;
			idToModelState.put(modelstate.getID(), modelstate);
			variableToIndexMap.put(modelstate.getID(), new HashMap<String, Integer>());
			indexToVariableMap.put(modelstate.getID(), new HashMap<Integer, String>());

			for (String variable : modelstate.getVariableToValueMap().keySet())
			{
				index = variables.size();
				variables.add(variable);
				values.add(modelstate.getVariableToValue(getReplacements(), variable));
				variableToIndexMap.get(modelstate.getID()).put(variable, index);
				indexToVariableMap.get(modelstate.getID()).put(index, variable);
				dvariablesdtime.add(new ASTNode());
				dvariablesdtime.get(index).setValue(0);
			}

			for (String reaction : modelstate.getReactionToFormulaMap().keySet())
			{
				ASTNode formula = modelstate.getReactionToFormulaMap().get(reaction);
				HashSet<HierarchicalStringDoublePair> reactantAndStoichiometrySet = modelstate.getReactionToReactantStoichiometrySetMap().get(
						reaction);
				HashSet<HierarchicalStringDoublePair> speciesAndStoichiometrySet = modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(
						reaction);
				HashSet<HierarchicalStringPair> nonConstantStoichiometrySet = modelstate.getReactionToNonconstantStoichiometriesSetMap()
						.get(reaction);

				if (reactantAndStoichiometrySet != null)
				{
					for (HierarchicalStringDoublePair reactantAndStoichiometry : reactantAndStoichiometrySet)
					{
						String reactant = reactantAndStoichiometry.string;
						double stoichiometry = reactantAndStoichiometry.doub;
						int varIndex = variableToIndexMap.get(modelstate.getID()).get(reactant);
						ASTNode stoichNode = new ASTNode();
						stoichNode.setValue(-1 * stoichiometry);
						dvariablesdtime.set(varIndex, ASTNode.sum(dvariablesdtime.get(varIndex), ASTNode.times(formula, stoichNode)));
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
							int varIndex = variableToIndexMap.get(modelstate.getID()).get(species);
							ASTNode stoichNode = new ASTNode();
							stoichNode.setValue(stoichiometry);
							dvariablesdtime.set(varIndex, ASTNode.sum(dvariablesdtime.get(varIndex), ASTNode.times(formula, stoichNode)));
						}
					}
				}

				if (nonConstantStoichiometrySet != null)
				{
					for (HierarchicalStringPair reactantAndStoichiometry : nonConstantStoichiometrySet)
					{
						String reactant = reactantAndStoichiometry.string1;
						String stoichiometry = reactantAndStoichiometry.string2;
						int varIndex = variableToIndexMap.get(modelstate.getID()).get(reactant);
						if (stoichiometry.startsWith("-"))
						{
							ASTNode stoichNode = new ASTNode(stoichiometry.substring(1, stoichiometry.length()));
							dvariablesdtime.set(varIndex, ASTNode.diff(dvariablesdtime.get(varIndex), ASTNode.times(formula, stoichNode)));
						}
						else
						{
							ASTNode stoichNode = new ASTNode(stoichiometry);
							dvariablesdtime.set(varIndex, ASTNode.sum(dvariablesdtime.get(varIndex), ASTNode.times(formula, stoichNode)));
						}
					}
				}
			}
		}
	}
}
