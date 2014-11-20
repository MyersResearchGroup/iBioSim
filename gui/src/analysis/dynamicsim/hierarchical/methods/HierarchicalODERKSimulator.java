package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import main.util.MutableBoolean;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.RateRule;

import analysis.dynamicsim.hierarchical.HierarchicalSimulationFunctions;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringPair;

public class HierarchicalODERKSimulator extends HierarchicalSimulationFunctions
{

	private int						numSteps;
	private double					relativeError;
	private double					absoluteError;
	private double					nextEventTime;
	private double					nextTriggerTime;
	private final DiffEquations[]	functions;

	public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory,
			String outputDirectory, double timeLimit, double maxTimeStep, long randomSeed,
			JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, int numSteps, double relError, double absError,
			String quantityType, String abstraction) throws IOException, XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, 0.0, progress,
				printInterval, stoichAmpValue, running, interestingSpecies, quantityType,
				abstraction);

		this.numSteps = numSteps;
		this.relativeError = relError;
		this.absoluteError = absError;
		this.functions = new DiffEquations[getNumSubmodels() + 1];

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

		int index = 0;
		setupNonConstantSpeciesReferences(getTopmodel());
		setupSpecies(getTopmodel());
		setupParameters(getTopmodel());

		setupConstraints(getTopmodel());
		setupRules(getTopmodel());
		setupInitialAssignments(getTopmodel());

		setupReactions(getTopmodel());
		setupEvents(getTopmodel());

		functions[index++] = new DiffEquations(new VariableState(getTopmodel()));
		setupForOutput(randomSeed, runNumber);

		for (ModelState model : getSubmodels().values())
		{
			setupNonConstantSpeciesReferences(model);
			setupSpecies(model);
			setupParameters(model);

			setupConstraints(model);
			setupRules(model);
			setupInitialAssignments(model);

			setupReactions(model);
			setupEvents(model);
			functions[index++] = new DiffEquations(new VariableState(model));
			setupForOutput(randomSeed, runNumber);
		}

		setupForOutput(randomSeed, runNumber);

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

	@Override
	public void simulate()
	{
		int currSteps = 0;

		if (isSbmlHasErrorsFlag())
		{
			return;
		}

		double nextEndTime = 0.0;

		// SIMULATION LOOP
		setCurrentTime(0);
		double printTime = 0;

		if (absoluteError == 0)
		{
			absoluteError = 1e-9;
		}

		if (relativeError == 0)
		{
			relativeError = 1e-6;
		}

		if (numSteps == 0)
		{
			numSteps = (int) (getTimeLimit() / getPrintInterval());
		}

		HighamHall54Integrator odecalc = new HighamHall54Integrator(getMinTimeStep(),
				getMaxTimeStep(), absoluteError, relativeError);
		odecalc.addEventHandler(new EventHandlerObject(), getMaxTimeStep(), 1e-18, 1000000);
		nextEventTime = handleEvents();

		for (DiffEquations eq : functions)
		{
			ModelState modelstate = eq.state.modelstate;
			fireEvent(eq, modelstate);

		}

		double initStepTime = 0;
		while (getCurrentTime() <= getTimeLimit() && !isCancelFlag() && !isConstraintFlag())
		{
			initStepTime = getCurrentTime();

			for (DiffEquations eq : functions)
			{
				// EVENT HANDLING
				// trigger and/or fire events, etc.

				// ModelState modelstate = eq.state.modelstate;

				// nextEventTime = handleEvents();

				setCurrentTime(initStepTime);

				nextEndTime = getCurrentTime() + getMaxTimeStep();

				if (nextEndTime > printTime)
				{
					nextEndTime = printTime;
				}

				// fireEvent(eq, modelstate);
				nextEventTime = Double.POSITIVE_INFINITY;
				nextTriggerTime = Double.POSITIVE_INFINITY;

				if (getCurrentTime() < nextEndTime
						&& Math.abs(getCurrentTime() - nextEndTime) > 1e-6)
				{
					odecalc.integrate(eq, getCurrentTime(), eq.state.values, nextEndTime,
							eq.state.values);
					// handleEvents();
					// fireEvent(eq, modelstate);

				}

				updateValuesArray(eq.state.modelstate);
			}

			updateValuesArray();
			setCurrentTime(nextEndTime);

			// TSD PRINTING
			// this prints the previous timestep's data
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
				// printTime += printInterval;
				getRunning().setTitle(
						"Progress (" + (int) ((getCurrentTime() / getTimeLimit()) * 100.0) + "%)");
			}

			// update progress bar
			getProgress().setValue((int) ((printTime / getTimeLimit()) * 100.0));

			// end simulation loop

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

	private void updateValuesArray()
	{
		int index = 0;

		// convert variableToValueMap into two arrays
		// and create a hashmap to find indices
		for (DiffEquations eq : functions)
		{
			for (String variable : eq.state.variables)
			{
				index = eq.state.variableToIndexMap.get(variable);
				eq.state.values[index] = eq.state.modelstate.getVariableToValue(getReplacements(),
						variable);
			}
		}
	}

	private void updateValuesArray(ModelState modelstate)
	{
		int index = 0;

		// convert variableToValueMap into two arrays
		// and create a hashmap to find indices
		for (DiffEquations eq : functions)
		{
			if (eq.state.modelstate.getID().equals(modelstate.getID()))
			{
				continue;
			}

			for (String variable : eq.state.modelstate.getIsHierarchical())
			{
				if (eq.state.variableToIndexMap.containsKey(variable))
				{
					index = eq.state.variableToIndexMap.get(variable);
					eq.state.values[index] = eq.state.modelstate.getVariableToValue(
							getReplacements(), variable);
				}
			}
		}
	}

	protected double handleEvents()
	{
		double nextEventTime = Double.POSITIVE_INFINITY;
		if (!getTopmodel().isNoEventsFlag())
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
			if (!models.isNoEventsFlag())
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

	private HashSet<String> fireEvent(DiffEquations eq, ModelState modelstate)
	{
		HashSet<String> vars = new HashSet<String>();
		if (!modelstate.isNoEventsFlag())
		{

			// if events fired, then the affected species counts need to be
			// updated in the values array
			int sizeBefore = modelstate.getTriggeredEventQueue().size();

			vars = fireEvents(modelstate, "variable", modelstate.isNoRuleFlag(),
					modelstate.isNoConstraintsFlag());

			int sizeAfter = modelstate.getTriggeredEventQueue().size();

			if (sizeAfter != sizeBefore)
			{
				for (String var : vars)
				{
					int index = eq.state.variableToIndexMap.get(var);
					if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(var)
							&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(var) == false)
					{
						eq.state.values[index] = modelstate.getVariableToValue(getReplacements(),
								var)
								* modelstate.getVariableToValue(getReplacements(), modelstate
										.getSpeciesToCompartmentNameMap().get(var));
					}
					else
					{
						eq.state.values[index] = modelstate.getVariableToValue(getReplacements(),
								var);
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

	private class EventHandlerObject implements EventHandler
	{
		// double t0, t1;
		public EventHandlerObject()
		{
		}

		@Override
		public Action eventOccurred(double t, double[] y, boolean increasing)
		{

			setCurrentTime(t);
			nextTriggerTime = t;
			nextEventTime = handleEvents();
			return EventHandler.Action.RESET_STATE;
		}

		@Override
		public double g(double t, double[] y)
		{

			// double t1 = currentTime;
			// double t2 = nextEventTime;

			for (DiffEquations eq : functions)
			{
				if (isEventTriggered(eq.state.modelstate, t, y, eq.state.variableToIndexMap))
				{
					// System.out.println("Time: " + t + " triggered " + y[1]);
					return -1;
				}
			}

			// System.out.println("Time: " + t + " NOT triggered " + y[1]);

			if (nextEventTime == t)
			{
				return -1;
			}
			else if (nextTriggerTime == t)
			{
				return -1;
			}

			return 1;
		}

		@Override
		public void init(double t0, double[] y, double t)
		{
			// this.t0 = t0;
			// this.t1 = t;
		}

		@Override
		public void resetState(double t, double[] y)
		{

			for (DiffEquations eq : functions)
			{

				ModelState modelstate = eq.state.modelstate;

				if (modelstate.isNoEventsFlag())
				{
					continue;
				}

				HashSet<String> variables = fireEvent(eq, modelstate);

				for (String var : variables)
				{
					int index = eq.state.variableToIndexMap.get(var);
					y[index] = eq.state.values[index];
				}

			}

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
		public void computeDerivatives(double t, double[] y, double[] currValueChanges)
				throws MaxCountExceededException, DimensionMismatchException
		{

			HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();

			// Needs to reevaluate some variables that is affected by a
			// nonconstant stoichiometry
			HashSet<String> revaluateVariables = new HashSet<String>();

			for (int i = 0; i < y.length; i++)
			{
				state.modelstate.setvariableToValueMap(getReplacements(),
						state.indexToVariableMap.get(i), y[i]);
			}

			// calculate the current variable values
			// based on the ODE system
			for (int i = 0; i < currValueChanges.length - 1; i++)
			{

				String currentVar = state.indexToVariableMap.get(i);

				if ((state.modelstate.getSpeciesIDSet().contains(currentVar) && state.modelstate
						.getSpeciesToIsBoundaryConditionMap().get(currentVar) == false)
						&& (state.modelstate.getVariableToValueMap().contains(currentVar))
						&& state.modelstate.getVariableToIsConstantMap().get(currentVar) == false)
				{

					currValueChanges[i] = evaluateExpressionRecursive(state.modelstate,
							state.dvariablesdtime[i], false, getCurrentTime(), null, null);
					// if (currValueChanges[i]!=0) {
					// System.out.println(indexToVariableMap.get(i) + "= " +
					// dvariablesdtime[i].toFormula() + "=" +
					// currValueChanges[i]);
					// }
				}
				else
				{
					currValueChanges[i] = 0;
				}

				if (state.modelstate.getVariableToIsInAssignmentRuleMap() != null
						&& state.modelstate.getVariableToIsInAssignmentRuleMap().containsKey(
								currentVar)
						&& state.modelstate.getVariableToValueMap().contains(currentVar)
						&& state.modelstate.getVariableToIsInAssignmentRuleMap().get(currentVar) == true)
				{
					affectedAssignmentRuleSet.addAll(state.modelstate
							.getVariableToAffectedAssignmentRuleSetMap().get(currentVar));
					revaluateVariables.add(currentVar);
				}
				// if (variableToIsInConstraintMap.get(speciesID) == true)
				// affectedConstraintSet.addAll(variableToAffectedConstraintSetMap.get(speciesID));
			}
			currValueChanges[currValueChanges.length - 1] = t;
			state.modelstate.setvariableToValueMap(getReplacements(), "time", t);
			// updatePropensities(performRateRules(getTopmodel(), currentTime),
			// "getTopmodel()");
			revaluateVariables.addAll(performRateRules(state.modelstate, state.variableToIndexMap,
					currValueChanges));

			// if assignment rules are performed, these changes need to be
			// reflected in the currValueChanges
			// that get passed back
			if (affectedAssignmentRuleSet.size() > 0)
			{

				HashSet<String> affectedVariables = performAssignmentRules(state.modelstate,
						affectedAssignmentRuleSet);

				for (String affectedVariable : affectedVariables)
				{

					int index = state.variableToIndexMap.get(affectedVariable);
					currValueChanges[index] = state.modelstate.getVariableToValue(
							getReplacements(), affectedVariable) - y[index];
					revaluateVariables.add(affectedVariable);

				}
				/*
				 * //for (String variable : revaluateVariables) { //int i =
				 * state.variableToIndexMap.get(variable); for (int i = 0; i <
				 * currValueChanges.length - 1; i++) {
				 * 
				 * String variable = state.indexToVariableMap.get(i);
				 * 
				 * if ((state.modelstate.speciesIDSet.contains(variable) &&
				 * state
				 * .modelstate.speciesToIsBoundaryConditionMap.get(variable) ==
				 * false) &&
				 * (state.modelstate.variableToValueMap.contains(variable)) &&
				 * state.modelstate.variableToIsConstantMap.get(variable) ==
				 * false) {
				 * 
				 * currValueChanges[i] =
				 * evaluateExpressionRecursive(state.modelstate,
				 * state.dvariablesdtime[i]); } }
				 */
			}

			setCurrentTime(t);

			// nextEventTime = handleEvents();

		}

		@Override
		public int getDimension()
		{

			return state.values.length;
		}

		/**
		 * performs every rate rule using the current time step
		 * 
		 * @param delta_t
		 * @return
		 */
		protected HashSet<String> performRateRules(ModelState modelstate,
				HashMap<String, Integer> variableToIndexMap, double[] currValueChanges)
		{

			HashSet<String> affectedVariables = new HashSet<String>();

			for (RateRule rateRule : modelstate.getRateRulesList())
			{

				String variable = rateRule.getVariable();
				ASTNode formula = inlineFormula(modelstate, rateRule.getMath());

				// update the species count (but only if the species isn't
				// constant) (bound cond is fine)
				if (modelstate.getVariableToIsConstantMap().containsKey(variable)
						&& modelstate.getVariableToIsConstantMap().get(variable) == false)
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

						// double oldValue =
						// modelstate.getVariableToValue(variable);
						double value = (evaluateExpressionRecursive(modelstate, formula, false,
								getCurrentTime(), null, null) * modelstate.getVariableToValue(
								getReplacements(),
								modelstate.getSpeciesToCompartmentNameMap().get(variable)));
						currValueChanges[index] = value;
						// modelstate.setvariableToValueMap(variable, oldValue +
						// value);

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
						// double oldValue =
						// modelstate.getVariableToValue(variable);
						double value = evaluateExpressionRecursive(modelstate, formula, false,
								getCurrentTime(), null, null);
						currValueChanges[index] = value;
						// modelstate.setvariableToValueMap(variable, oldValue +
						// value);
					}

					affectedVariables.add(variable);
				}
			}

			return affectedVariables;
		}

	}

	protected class VariableState
	{
		ModelState					modelstate;
		String[]					variables;
		double[]					values;
		ASTNode[]					dvariablesdtime;
		HashMap<String, Integer>	variableToIndexMap;
		HashMap<Integer, String>	indexToVariableMap;
		MutableBoolean				eventsFlag		= new MutableBoolean(false);
		MutableBoolean				rulesFlag		= new MutableBoolean(false);
		MutableBoolean				constraintsFlag	= new MutableBoolean(false);

		protected VariableState(ModelState modelstate)
		{
			this.modelstate = modelstate;
			int size = modelstate.getVariableToValueMap().size() + 1;
			variables = new String[size];
			values = new double[size];
			dvariablesdtime = new ASTNode[size];
			variableToIndexMap = new HashMap<String, Integer>(size);
			indexToVariableMap = new HashMap<Integer, String>(size);

			int index = 0;

			// convert variableToValueMap into two arrays
			// and create a hashmap to find indices
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
			// create system of ODEs for the change in variables
			for (String reaction : modelstate.getReactionToFormulaMap().keySet())
			{

				ASTNode formula = modelstate.getReactionToFormulaMap().get(reaction);
				// System.out.println("HERE: " + formula.toFormula());
				HashSet<HierarchicalStringDoublePair> reactantAndStoichiometrySet = modelstate
						.getReactionToReactantStoichiometrySetMap().get(reaction);
				HashSet<HierarchicalStringDoublePair> speciesAndStoichiometrySet = modelstate
						.getReactionToSpeciesAndStoichiometrySetMap().get(reaction);
				HashSet<HierarchicalStringPair> nonConstantStoichiometrySet = modelstate
						.getReactionToNonconstantStoichiometriesSetMap().get(reaction);
				// loop through reactants
				if (reactantAndStoichiometrySet != null)
				{
					for (HierarchicalStringDoublePair reactantAndStoichiometry : reactantAndStoichiometrySet)
					{

						String reactant = reactantAndStoichiometry.string;
						double stoichiometry = reactantAndStoichiometry.doub;
						int varIndex = variableToIndexMap.get(reactant);
						ASTNode stoichNode = new ASTNode();
						stoichNode.setValue(-1 * stoichiometry);
						dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex],
								ASTNode.times(formula, stoichNode));
					}
				}

				// loop through products
				if (speciesAndStoichiometrySet != null)
				{
					for (HierarchicalStringDoublePair speciesAndStoichiometry : speciesAndStoichiometrySet)
					{

						String species = speciesAndStoichiometry.string;
						double stoichiometry = speciesAndStoichiometry.doub;

						// if it's a product its stoichiometry will be positive
						// (and if it's a reactant it'll be negative)
						if (stoichiometry > 0)
						{

							int varIndex = variableToIndexMap.get(species);
							ASTNode stoichNode = new ASTNode();
							stoichNode.setValue(stoichiometry);

							// System.out.println("HERE2: " +
							// ASTNode.times(formula,stoichNode).toFormula());
							dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex],
									ASTNode.times(formula, stoichNode));

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
							ASTNode stoichNode = new ASTNode(stoichiometry.substring(1,
									stoichiometry.length()));
							dvariablesdtime[varIndex] = ASTNode.diff(dvariablesdtime[varIndex],
									ASTNode.times(formula, stoichNode));
						}
						else
						{
							ASTNode stoichNode = new ASTNode(stoichiometry);
							dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex],
									ASTNode.times(formula, stoichNode));
						}
					}
				}
			}

		}
	}

}
