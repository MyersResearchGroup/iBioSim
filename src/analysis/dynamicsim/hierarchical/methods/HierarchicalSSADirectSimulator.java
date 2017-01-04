package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.PriorityQueue;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import analysis.dynamicsim.hierarchical.HierarchicalSimulation;
import analysis.dynamicsim.hierarchical.io.HierarchicalWriter;
import analysis.dynamicsim.hierarchical.math.EventNode;
import analysis.dynamicsim.hierarchical.math.ReactionNode;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalEventComparator;
import analysis.dynamicsim.hierarchical.util.setup.ModelSetup;

public class HierarchicalSSADirectSimulator extends HierarchicalSimulation
{
	private final boolean	print;

	private long			randomSeed;

	public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction, double initialTime, double outputStartTime) throws IOException, XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, randomSeed, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction, initialTime, outputStartTime, SimType.HSSA);
		this.print = true;
		this.randomSeed = randomSeed;
	}

	public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction, double initialTime, double outputStartTime, boolean print) throws IOException, XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, randomSeed, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction, initialTime, outputStartTime, SimType.HSSA);
		this.print = print;
		this.randomSeed = randomSeed;

	}

	public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, long randomSeed, double printInterval, double initialTime, double outputStartTime, boolean print) throws IOException, XMLStreamException
	{

		this(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, Double.POSITIVE_INFINITY, 0, randomSeed, null, printInterval, 1, null, null, null, null, 0, 0, print);

	}

	@Override
	public void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException
	{
		if (!isInitialized)
		{
			setCurrentTime(getInitialTime());
			ModelSetup.setupModels(this, ModelType.HSSA);
			eventList = getEventList();
			variableList = getVariableList();
			//constraintList = getConstraintList();
			computeFixedPoint();
			if (!eventList.isEmpty())
			{
				triggeredEventList = new PriorityQueue<EventNode>(1, new HierarchicalEventComparator());
				HierarchicalUtilities.triggerAndFireEvents(eventList, triggeredEventList, currentTime.getValue(0));
			}

			initStateCopy = getArrayState(variableList);
			setInitialPropensity();

			setupForOutput(runNumber);
			HierarchicalWriter.setupVariableFromTSD(getBufferedTSDWriter(), getTopmodel(), getSubmodels(), getInterestingSpecies());
			isInitialized = true;
		}

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

	}

	/**
	 * does minimized initialization process to prepare for a new run
	 */
	@Override
	public void setupForNewRun(int newRun)
	{
		setCurrentTime(getInitialTime());
		setConstraintFlag(true);
		setupForOutput(newRun);

		for (int i = initStateCopy.length - 1; i >= 0; i--)
		{
			variableList.get(i).setValue(i, initStateCopy[i]);
		}
		restoreInitialPropensity();

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

		double r1 = 0, r2 = 0, totalPropensity = 0, delta_t = 0, nextReactionTime = 0, previousTime = 0, nextEventTime = 0, nextMaxTime = 0;

		if (isSbmlHasErrorsFlag())
		{
			return;
		}

		if (!isInitialized)
		{
			try
			{
				this.initialize(randomSeed, 1);
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
		printTime = getOutputStartTime();
		previousTime = 0;
		nextEventTime = getNextEventTime();

		if (print)
		{
			printToFile();
		}

		while (currentTime.getValue(0) < getTimeLimit() && !isCancelFlag())
		{
//			if (!HierarchicalUtilities.evaluateConstraints(constraintList))
//			{
//				return;
//			}

			r1 = getRandomNumberGenerator().nextDouble();
			r2 = getRandomNumberGenerator().nextDouble();
			totalPropensity = getTotalPropensity();
			delta_t = computeNextTimeStep(r1, totalPropensity);
			nextReactionTime = currentTime.getValue(0) + delta_t;
			nextEventTime = getNextEventTime();
			nextMaxTime = currentTime.getValue(0) + getMaxTimeStep();
			previousTime = currentTime.getValue(0);

			if (nextReactionTime < nextEventTime && nextReactionTime < nextMaxTime)
			{
				setCurrentTime(nextReactionTime);
			}
			else if (nextEventTime <= nextMaxTime)
			{
				setCurrentTime(nextEventTime);
			}
			else
			{
				setCurrentTime(nextMaxTime);
			}

			if (currentTime.getValue(0) > getTimeLimit())
			{
				break;
			}

			if (currentTime.getValue(0) == nextReactionTime)
			{
				update(true, false, false, r2, previousTime);
			}
			else if (currentTime.getValue(0) == nextEventTime)
			{
				update(false, false, true, r2, previousTime);
			}
			else
			{
				update(false, true, false, r2, previousTime);
			}

			if (print)
			{
				printToFile();
			}
		}

		if (!isCancelFlag())
		{
			setCurrentTime(getTimeLimit());
			update(false, true, true, r2, previousTime);

			if (print)
			{
				printToFile();
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

	private void update(boolean reaction, boolean rateRule, boolean events, double r2, double previousTime)
	{
		if (reaction)
		{
			selectAndPerformReaction(r2);
		}

		if (rateRule)
		{
			fireRateRules(previousTime);
		}

		if (events)
		{
			HierarchicalUtilities.triggerAndFireEvents(eventList, triggeredEventList, currentTime.getValue(0));
		}
		computeAssignmentRules();

	}

	public double computeNextTimeStep(double r1, double totalPropensity)
	{
		return Math.log(1 / r1) / totalPropensity;
	}

	private void fireRateRules(double previousTime)
	{

	}

	private void selectAndPerformReaction(double r2)
	{
		double threshold = getTotalPropensity() * r2;
		double totalRunningPropensity = getTopmodel().getPropensity(0);
		if (totalRunningPropensity >= threshold)
		{
			performReaction(getTopmodel(), threshold);
		}
		else
		{
			for (HierarchicalModel submodel : getSubmodels().values())
			{
				totalRunningPropensity += submodel.getPropensity(0);
				if (totalRunningPropensity >= threshold)
				{
					performReaction(submodel, threshold - (totalRunningPropensity - submodel.getPropensity(submodel.getIndex())));
				}
			}
		}
	}

	private void performReaction(HierarchicalModel modelstate, double threshold)
	{
		double runningSum = 0;
		for (ReactionNode reactionNode : modelstate.getReactions())
		{
			runningSum += reactionNode.getValue(modelstate.getIndex());

			if (runningSum >= threshold)
			{
				reactionNode.fireReaction(modelstate.getIndex(), runningSum - threshold);
				return;
			}
		}
	}

	@Override
	public void printStatisticsTSD()
	{

	}

	public double getNextEventTime()
	{
		for (int i = eventList.size() - 1; i >= 0; i--)
		{
			EventNode event = eventList.get(i);
			event.isTriggeredAtTime(currentTime.getValue(0), event.getSubmodelIndex(i));
		}
		HierarchicalUtilities.triggerAndFireEvents(eventList, triggeredEventList, currentTime.getValue(0));

		if (triggeredEventList != null && !triggeredEventList.isEmpty())
		{
			return triggeredEventList.peek().getFireTime();
		}
		return Double.POSITIVE_INFINITY;
	}
}
