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

	private int										numSteps;
	private double									relativeError, absoluteError;
	private double									nextEventTime;
	private final boolean							print;
	private final DiffEquations						function;
	private final Map<String, Map<Double, Boolean>>	triggerValues;

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
		this.triggerValues = new HashMap<String, Map<Double, Boolean>>();
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
		this.triggerValues = new HashMap<String, Map<Double, Boolean>>();
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
			// this.function.state.addState(model);
		}
		setupForOutput(randomSeed, runNumber);
		setupVariableFromTSD();
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

		fireEvent();

		while (getCurrentTime() <= getTimeLimit() && !isCancelFlag() && !isConstraintFlag())
		{

			nextEndTime = getCurrentTime() + getMaxTimeStep();

			if (nextEndTime > printTime)
			{
				nextEndTime = printTime;
			}

			if (getCurrentTime() < nextEndTime && Math.abs(getCurrentTime() - nextEndTime) > absoluteError)
			{
				odecalc.integrate(function, getCurrentTime(), function.state.values, nextEndTime, function.state.values);
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

		for (String event : triggerValues.keySet())
		{
			clone.addAll(triggerValues.get(event).keySet());

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

			if (tau > 0)
			{
				modelstate.getEventToPreviousTriggerValueMap().put(event, triggerValues.get(event).get(tau));
			}

			clone.clear();

		}
	}

	private void updateStateAndFireReaction(double t, double[] y)
	{

		ModelState modelstate = function.state.modelstate;

		setCurrentTime(t);

		for (int i = 0; i < y.length; i++)
		{
			modelstate.setvariableToValueMap(getReplacements(), function.state.indexToVariableMap.get(i), y[i]);
		}

		nextEventTime = handleEvents();

		updateTriggerState(modelstate, t);

		Set<String> variables = fireEvent();

		for (String var : variables)
		{
			int index = function.state.variableToIndexMap.get(var);
			y[index] = function.state.values[index];

		}

		nextEventTime = handleEvents();

		for (String event : function.state.modelstate.getUntriggeredEventSet())
		{
			boolean state = isEventTriggered(function.state.modelstate, event, t, y, function.state.variableToIndexMap, false);

			triggerValues.get(event).clear();

			triggerValues.get(event).put(t, state);
		}
	}

	private HashSet<String> fireEvent()
	{
		HashSet<String> vars = new HashSet<String>();
		ModelState modelstate = function.state.modelstate;

		if (!modelstate.isNoEventsFlag())
		{
			int sizeBefore = modelstate.getTriggeredEventQueue().size();
			vars = fireEvents(modelstate, "variable", modelstate.isNoRuleFlag(), modelstate.isNoConstraintsFlag());
			int sizeAfter = modelstate.getTriggeredEventQueue().size();

			if (sizeAfter != sizeBefore)
			{
				for (String var : vars)
				{
					int index = function.state.variableToIndexMap.get(var);
					if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(var)
							&& !modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(var))
					{
						function.state.values[index] = modelstate.getVariableToValue(getReplacements(), var)
								* modelstate.getVariableToValue(getReplacements(), modelstate.getSpeciesToCompartmentNameMap().get(var));
						modelstate.setvariableToValueMap(getReplacements(), var, function.state.values[index]);
					}
					else
					{
						function.state.values[index] = modelstate.getVariableToValue(getReplacements(), var);
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
			boolean state;

			for (String event : function.state.modelstate.getUntriggeredEventSet())
			{
				state = isEventTriggered(function.state.modelstate, event, t, y, function.state.variableToIndexMap, false);

				if (!triggerValues.containsKey(event))
				{
					triggerValues.put(event, new HashMap<Double, Boolean>());
				}

				triggerValues.get(event).put(t, state);

				if (state)
				{
					if (triggerValues.get(event).keySet().size() > 0)
					{
						double key = -1;
						double maxTime = Double.MIN_VALUE;

						for (double time : triggerValues.get(event).keySet())
						{
							if (time < t && time > maxTime)
							{
								key = time;
							}
						}

						if (key > 0)
						{
							if (!triggerValues.get(event).get(key))
							{
								return -value;
							}
						}
					}
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

			HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
			HashSet<String> revaluateVariables = new HashSet<String>();

			for (int i = 0; i < y.length; i++)
			{
				state.modelstate.setvariableToValueMap(getReplacements(), state.indexToVariableMap.get(i), y[i]);
			}

			for (int i = 0; i < currValueChanges.length - 1; i++)
			{

				String currentVar = state.indexToVariableMap.get(i);

				if ((state.modelstate.getSpeciesIDSet().contains(currentVar) && state.modelstate.getSpeciesToIsBoundaryConditionMap().get(currentVar) == false)
						&& (state.modelstate.getVariableToValueMap().contains(currentVar))
						&& state.modelstate.getVariableToIsConstantMap().get(currentVar) == false)
				{
					currValueChanges[i] = evaluateExpressionRecursive(state.modelstate, state.dvariablesdtime[i], false, getCurrentTime(), null, null);
					revaluateVariables.add(currentVar);
				}
				else
				{
					currValueChanges[i] = 0;
				}

				if (state.modelstate.getVariableToIsInAssignmentRuleMap() != null
						&& state.modelstate.getVariableToIsInAssignmentRuleMap().containsKey(currentVar)
						&& state.modelstate.getVariableToValueMap().contains(currentVar)
						&& state.modelstate.getVariableToIsInAssignmentRuleMap().get(currentVar))
				{
					affectedAssignmentRuleSet.addAll(state.modelstate.getVariableToAffectedAssignmentRuleSetMap().get(currentVar));
					revaluateVariables.add(currentVar);
				}
			}
			currValueChanges[currValueChanges.length - 1] = t;
			state.modelstate.setvariableToValueMap(getReplacements(), "time", t);
			if (affectedAssignmentRuleSet.size() > 0)
			{
				HashSet<String> affectedVariables;
				boolean changed = true;

				while (changed)
				{
					affectedVariables = performAssignmentRules(state.modelstate, affectedAssignmentRuleSet);
					changed = false;
					for (String affectedVariable : affectedVariables)
					{
						int index = state.variableToIndexMap.get(affectedVariable);
						double oldValue = y[index];
						double newValue = state.modelstate.getVariableToValue(getReplacements(), affectedVariable);

						if (newValue != oldValue)
						{
							changed |= true;
							y[index] = newValue;
						}

					}

					for (String affectedVariable : revaluateVariables)
					{
						int index = state.variableToIndexMap.get(affectedVariable);
						double oldValue = currValueChanges[index];
						double newValue = evaluateExpressionRecursive(state.modelstate, state.dvariablesdtime[index], false, getCurrentTime(), null,
								null);

						if (newValue != oldValue)
						{
							changed |= true;
							currValueChanges[index] = newValue;
						}
					}
				}
			}
			performRateRules(state.modelstate, state.variableToIndexMap, currValueChanges);
			setCurrentTime(t);
		}

		@Override
		public int getDimension()
		{
			return state.values.length;
		}

		/**
		 * performs every rate rule using the current time step
		 */
		private HashSet<String> performRateRules(ModelState modelstate, HashMap<String, Integer> variableToIndexMap, double[] currValueChanges)
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

						double newValue = (evaluateExpressionRecursive(modelstate, formula, false, getCurrentTime(), null, null) * modelstate
								.getVariableToValue(getReplacements(), modelstate.getSpeciesToCompartmentNameMap().get(variable)));

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
						double newValue = evaluateExpressionRecursive(modelstate, formula, false, getCurrentTime(), null, null);
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
		ModelState					modelstate;
		String[]					variables;
		double[]					values;
		ASTNode[]					dvariablesdtime;
		HashMap<String, Integer>	variableToIndexMap;
		HashMap<Integer, String>	indexToVariableMap;

		public void addState(ModelState modelstate)
		{
			int size = modelstate.getVariableToValueMap().size() + 1;
			int index = 0;
			this.modelstate = modelstate;
			this.variables = new String[size];
			this.values = new double[size];
			this.dvariablesdtime = new ASTNode[size];
			this.variableToIndexMap = new HashMap<String, Integer>(size);
			this.indexToVariableMap = new HashMap<Integer, String>(size);

			for (String variable : modelstate.getVariableToValueMap().keySet())
			{
				variables[index] = variable;
				values[index] = modelstate.getVariableToValue(getReplacements(), variable);
				variableToIndexMap.put(variable, index);
				dvariablesdtime[index] = new ASTNode();
				dvariablesdtime[index].setValue(0);
				indexToVariableMap.put(index, variable);
				++index;
			}

			indexToVariableMap.put(index, "time");
			variableToIndexMap.put("time", index);
			variables[index] = "time";
			values[index] = 0;
			modelstate.getVariableToValueMap().put("time", 0);

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
						int varIndex = variableToIndexMap.get(reactant);
						ASTNode stoichNode = new ASTNode();
						stoichNode.setValue(-1 * stoichiometry);
						dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex], ASTNode.times(formula, stoichNode));
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
							int varIndex = variableToIndexMap.get(species);
							ASTNode stoichNode = new ASTNode();
							stoichNode.setValue(stoichiometry);
							dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex], ASTNode.times(formula, stoichNode));
						}
					}
				}

				if (nonConstantStoichiometrySet != null)
				{
					for (HierarchicalStringPair reactantAndStoichiometry : nonConstantStoichiometrySet)
					{
						String reactant = reactantAndStoichiometry.string1;
						String stoichiometry = reactantAndStoichiometry.string2;
						int varIndex = variableToIndexMap.get(reactant);
						if (stoichiometry.startsWith("-"))
						{
							ASTNode stoichNode = new ASTNode(stoichiometry.substring(1, stoichiometry.length()));
							dvariablesdtime[varIndex] = ASTNode.diff(dvariablesdtime[varIndex], ASTNode.times(formula, stoichNode));
						}
						else
						{
							ASTNode stoichNode = new ASTNode(stoichiometry);
							dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex], ASTNode.times(formula, stoichNode));
						}
					}
				}
			}
		}
	}
}
