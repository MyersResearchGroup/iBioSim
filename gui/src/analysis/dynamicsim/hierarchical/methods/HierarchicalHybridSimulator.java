package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;

import analysis.dynamicsim.hierarchical.util.Evaluator;
import analysis.dynamicsim.hierarchical.util.ode.VariableState;

public final class HierarchicalHybridSimulator extends HierarchicalSSADirectSimulator
{

	private final DiffEquations		function;
	//private double[]				globalValues;
	//private double					nextEventTime;
	//private int						numSteps;
	private double					relativeError, absoluteError;
	private HighamHall54Integrator	odecalc;

	public HierarchicalHybridSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit,
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue,
			JFrame running, String[] interestingSpecies, String quantityType, String abstraction) throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval,
				stoichAmpValue, running, interestingSpecies, quantityType, abstraction);

		this.function = new DiffEquations(new VariableState());

	}

	public void initialize()
	{
		absoluteError = 1e-12;
		relativeError = 1e-9;
		odecalc = new HighamHall54Integrator(getMinTimeStep(), getMaxTimeStep(), absoluteError, relativeError);
		TriggeredEventHandlerObject triggered = new TriggeredEventHandlerObject();
		odecalc.addEventHandler(triggered, getPrintInterval(), 1e-20, 10000);

		this.function.state.addState(getTopmodel(), getReplacements());

		for (ModelState model : getSubmodels().values())
		{
			this.function.state.addState(model, getReplacements());
		}
	}

	@Override
	public double computeNextTimeStep(double r1, double totalPropensity)
	{

		return 0;
	}

	@Override
	public String selectReaction(double r2)
	{
		return "";
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

			if (Double.isNaN(t))
			{
				throw new MaxCountExceededException(-1);
			}

			setCurrentTime(t);

			computeDerivatives(getTopmodel(), t, y, currValueChanges);

			for (ModelState model : getSubmodels().values())
			{
				computeDerivatives(model, t, y, currValueChanges);
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

			for (int i : function.state.getIndexToVariableMap().get(modelstateId).keySet())
			{
				String variable = state.getIndexToVariableMap().get(modelstateId).get(i);
				modelstate.setVariableToValue(getReplacements(), variable, y[i]);
			}

			for (int i : function.state.getIndexToVariableMap().get(modelstateId).keySet())
			{
				String currentVar = state.getIndexToVariableMap().get(modelstateId).get(i);

				if (!modelstate.isConstant(currentVar))
				{
					if (modelstate.getSpeciesIDSet().contains(currentVar) && !modelstate.getSpeciesToIsBoundaryConditionMap().get(currentVar))
					{
						currValueChanges[i] = Evaluator.evaluateExpressionRecursive(modelstate,
								state.getDvariablesdtime().get(modelstateId).get(currentVar), false, t, null, null, getReplacements());
					}

				}
			}

		}

		private void computeDependencies(double[] currValueChanges)
		{
			for (String dependency : state.getDependencyToDependents().keySet())
			{
				double total = 0;

				for (String modelstate : state.getDependencyToDependents().get(dependency).keySet())
				{
					int index = state.getDependencyToDependents().get(dependency).get(modelstate);
					total = total + currValueChanges[index];
				}

				for (String modelstate : state.getDependencyToDependents().get(dependency).keySet())
				{
					int index = state.getDependencyToDependents().get(dependency).get(modelstate);
					currValueChanges[index] = total;
				}
			}
		}
	}

	private class TriggeredEventHandlerObject implements EventHandler
	{

		@Override
		public Action eventOccurred(double t, double[] y, boolean increasing)
		{

			return EventHandler.Action.STOP;
		}

		@Override
		public double g(double t, double[] y)
		{

			return 0;
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
}
