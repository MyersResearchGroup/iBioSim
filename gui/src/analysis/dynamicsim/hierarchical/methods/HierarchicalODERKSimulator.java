package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalSimulation;
import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalEventComparator;
import analysis.dynamicsim.hierarchical.util.io.HierarchicalWriter;
import analysis.dynamicsim.hierarchical.util.math.EventNode;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;
import analysis.dynamicsim.hierarchical.util.setup.ModelSetup;

public final class HierarchicalODERKSimulator extends HierarchicalSimulation
{
	private boolean						isSingleStep;
	private HighamHall54Integrator		odecalc;
	private double						relativeError, absoluteError;
	private DifferentialEquations		de;
	private double[]					state;
	private List<ReactionNode>			reactionList;
	private List<EventNode>				eventList;
	private PriorityQueue<EventNode>	triggeredEventList;

	public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory, double timeLimit) throws IOException, XMLStreamException
	{

		this(SBMLFileName, rootDirectory, rootDirectory, 0, timeLimit, Double.POSITIVE_INFINITY, 0, null, Double.POSITIVE_INFINITY, 0, null, null, 1, 1e-6, 1e-9, "amount", "none", 0, 0, false);
		isInitialized = false;
		isSingleStep = true;
		this.printTime = Double.POSITIVE_INFINITY;
	}

	public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies, int numSteps,
			double relError, double absError, String quantityType, String abstraction, double initialTime, double outputStartTime) throws IOException, XMLStreamException
	{

		this(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, interestingSpecies, numSteps, relError, absError, quantityType, abstraction, initialTime, outputStartTime, true);
		this.isInitialized = false;
		this.isSingleStep = false;

	}

	public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies, int numSteps,
			double relError, double absError, String quantityType, String abstraction, double initialTime, double outputStartTime, boolean print) throws IOException, XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, randomSeed, runs, timeLimit, maxTimeStep, 0.0, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction, initialTime, outputStartTime, SimType.HODE);
		this.relativeError = relError;
		this.absoluteError = absError;
		this.isSingleStep = false;
		this.absoluteError = absoluteError == 0 ? 1e-12 : absoluteError;
		this.relativeError = absoluteError == 0 ? 1e-9 : relativeError;

		this.printTime = outputStartTime;

		if (numSteps > 0)
		{
			setPrintInterval(timeLimit / numSteps);
		}

		odecalc = new HighamHall54Integrator(getMinTimeStep(), getMaxTimeStep(), absoluteError, relativeError);
		isInitialized = false;

	}

	public HierarchicalODERKSimulator(HierarchicalMixedSimulator sim, ModelState topmodel, Map<String, ModelState> submodels) throws IOException, XMLStreamException
	{
		super(sim);
		this.relativeError = 1e-6;
		this.absoluteError = 1e-9;
		this.odecalc = new HighamHall54Integrator(getMinTimeStep(), getMaxTimeStep(), absoluteError, relativeError);
		this.isInitialized = true;
		this.isSingleStep = true;
		this.printTime = Double.POSITIVE_INFINITY;

		this.eventList = getEventList();
		this.variableList = getVariableList();
		this.reactionList = getReactionList();

		de = new DifferentialEquations();
		if (!eventList.isEmpty())
		{
			HierarchicalEventHandler handler = new HierarchicalEventHandler();
			HierarchicalTriggeredEventHandler triggeredHandler = new HierarchicalTriggeredEventHandler();
			odecalc.addEventHandler(handler, getPrintInterval(), 1e-20, 10000);
			odecalc.addEventHandler(triggeredHandler, getPrintInterval(), 1e-20, 10000);
			triggeredEventList = new PriorityQueue<EventNode>(0, new HierarchicalEventComparator());

			HierarchicalUtilities.triggerAndFireEvents(eventList, triggeredEventList, currentTime.getValue());

		}
		state = getArrayState(variableList);

		initStateCopy = state.clone();
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
	public void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException
	{
		if (!isInitialized)
		{
			setCurrentTime(getInitialTime());
			ModelSetup.setupModels(this, false);
			eventList = getEventList();
			variableList = getVariableList();
			reactionList = getReactionList();
			constraintList = getConstraintList();

			de = new DifferentialEquations();
			HierarchicalUtilities.computeFixedPoint(getInitAssignmentList(), reactionList);

			if (!eventList.isEmpty())
			{
				HierarchicalEventHandler handler = new HierarchicalEventHandler();
				HierarchicalTriggeredEventHandler triggeredHandler = new HierarchicalTriggeredEventHandler();
				odecalc.addEventHandler(handler, getPrintInterval(), 1e-20, 10000);
				odecalc.addEventHandler(triggeredHandler, getPrintInterval(), 1e-20, 10000);
				triggeredEventList = new PriorityQueue<EventNode>(1, new HierarchicalEventComparator());

				HierarchicalUtilities.triggerAndFireEvents(eventList, triggeredEventList, currentTime.getValue());

			}
			state = getArrayState(variableList);

			initStateCopy = state.clone();
			if (!isSingleStep)
			{
				setupForOutput(runNumber);

				HierarchicalWriter.setupVariableFromTSD(getBufferedTSDWriter(), getTopmodel(), getSubmodels(), getInterestingSpecies());
			}
			isInitialized = true;
		}

	}

	@Override
	public void simulate()
	{
		if (!isInitialized)
		{
			try
			{
				initialize(0, 1);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (XMLStreamException e)
			{
				e.printStackTrace();
			}
		}

		double nextEndTime = 0;

		while (currentTime.getValue() < getTimeLimit() && !isCancelFlag())
		{

			if (!HierarchicalUtilities.evaluateConstraints(constraintList))
			{
				return;
			}

			nextEndTime = currentTime.getValue() + getMaxTimeStep();

			if (nextEndTime > printTime)
			{
				nextEndTime = printTime;
			}

			if (nextEndTime > getTimeLimit())
			{
				nextEndTime = getTimeLimit();
			}

			if (state.length > 0)
			{
				try
				{

					odecalc.integrate(de, currentTime.getValue(), state, nextEndTime, state);
					HierarchicalUtilities.computeAssignmentRules(state, variableList);

				}
				catch (Exception e)
				{
					setCurrentTime(nextEndTime);
				}
			}
			else
			{
				setCurrentTime(nextEndTime);
			}
			if (!isSingleStep)
			{
				printToFile();
			}
		}
		if (!isSingleStep)
		{
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
	}

	@Override
	public void setupForNewRun(int newRun)
	{
		if (isInitialized)
		{
			state = initStateCopy.clone();
		}
	}

	@Override
	public void printStatisticsTSD()
	{
		// TODO Auto-generated method stub

	}

	private void updateVariables(double[] y)
	{
		for (int i = 0; i < y.length; i++)
		{
			variableList.get(i).setValue(y[i]);
		}
	}

	public class HierarchicalEventHandler implements EventHandler
	{
		private double	value	= -1;

		@Override
		public void init(double t0, double[] y0, double t)
		{

		}

		@Override
		public double g(double t, double[] y)
		{
			double returnValue = -value;

			currentTime.setValue(t);
			for (int i = 0; i < y.length; i++)
			{
				variableList.get(i).setValue(y[i]);
			}

			for (EventNode event : eventList)
			{
				if (event.isTriggeredAtTime(t))
				{
					returnValue = value;
				}
			}
			return returnValue;
		}

		@Override
		public Action eventOccurred(double t, double[] y, boolean increasing)
		{
			value = -value;

			currentTime.setValue(t);

			for (int i = 0; i < y.length; i++)
			{
				variableList.get(i).setValue(y[i]);
			}

			HierarchicalUtilities.computeAssignmentRules(state, variableList);
			HierarchicalUtilities.triggerAndFireEvents(eventList, triggeredEventList, t);
			HierarchicalUtilities.computeAssignmentRules(y, variableList);

			return EventHandler.Action.STOP;
		}

		@Override
		public void resetState(double t, double[] y)
		{
			// TODO Auto-generated method stub

		}

	}

	public class HierarchicalTriggeredEventHandler implements EventHandler
	{
		private double	value	= -1;

		@Override
		public void init(double t0, double[] y0, double t)
		{

		}

		@Override
		public double g(double t, double[] y)
		{
			currentTime.setValue(t);
			if (!triggeredEventList.isEmpty())
			{
				if (triggeredEventList.peek().getFireTime() <= t)
				{
					return value;
				}
			}
			return -value;
		}

		@Override
		public Action eventOccurred(double t, double[] y, boolean increasing)
		{
			value = -value;

			currentTime.setValue(t);

			for (int i = 0; i < y.length; i++)
			{
				variableList.get(i).setValue(y[i]);
			}

			HierarchicalUtilities.computeAssignmentRules(state, variableList);
			HierarchicalUtilities.triggerAndFireEvents(eventList, triggeredEventList, t);
			HierarchicalUtilities.computeAssignmentRules(y, variableList);

			return EventHandler.Action.STOP;
		}

		@Override
		public void resetState(double t, double[] y)
		{
			// TODO Auto-generated method stub

		}

	}

	public class DifferentialEquations implements FirstOrderDifferentialEquations
	{

		@Override
		public int getDimension()
		{
			return variableList.size();
		}

		@Override
		public void computeDerivatives(double t, double[] y, double[] yDot) throws MaxCountExceededException, DimensionMismatchException
		{

			setCurrentTime(t);
			// Copy values
			updateVariables(y);
			HierarchicalUtilities.computeAssignmentRules(y, variableList);
			HierarchicalUtilities.computeReactionPropensities(reactionList);
			//
			for (int i = 0; i < yDot.length; i++)
			{
				yDot[i] = variableList.get(i).computeRateOfChange(t);
			}
		}
	}

}
