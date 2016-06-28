package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.PriorityQueue;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalSimulation;
import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalEventComparator;
import analysis.dynamicsim.hierarchical.util.io.HierarchicalWriter;
import analysis.dynamicsim.hierarchical.util.math.EventNode;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;
import analysis.dynamicsim.hierarchical.util.setup.ModelSetup;

public class HierarchicalSSADirectSimulator extends HierarchicalSimulation
{

	private int				currRun;
	private final boolean	print;

	private long			randomSeed;

	public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction) throws IOException, XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, randomSeed, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction, SimType.HSSA);
		this.print = true;
		this.randomSeed = randomSeed;
	}

	public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction, boolean print) throws IOException, XMLStreamException
	{

		super(SBMLFileName, rootDirectory, outputDirectory, randomSeed, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction, SimType.HSSA);
		this.print = print;
		this.randomSeed = randomSeed;

	}

	public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, long randomSeed, double printInterval, boolean print) throws IOException, XMLStreamException
	{

		this(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, Double.POSITIVE_INFINITY, 0, randomSeed, null, printInterval, 1, null, null, null, null, print);

	}

	@Override
	public void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException
	{
		if (!isInitialized)
		{
			setCurrentTime(0);
			ModelSetup.setupModels(this, true);
			eventList = getEventList();
			variableList = getVariableList();
			assignmentList = getAssignmentRuleList();
			HierarchicalUtilities.computeFixedPoint(getInitAssignmentList(), getReactionList());
			if (!eventList.isEmpty())
			{
				triggeredEventList = new PriorityQueue<EventNode>(1, new HierarchicalEventComparator());
				HierarchicalUtilities.triggerAndFireEvents(eventList, triggeredEventList, currentTime.getValue());
			}

			initStateCopy = getArrayState(variableList);

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
		currRun = newRun;
		setCurrentTime(0.0);
		setConstraintFlag(true);
		setupForOutput(newRun);

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
		printTime = 0;
		previousTime = 0;
		nextEventTime = getNextEventTime();

		if (print)
		{
			printToFile();
		}

		while (currentTime.getValue() < getTimeLimit() && !isCancelFlag())
		{
			r1 = getRandomNumberGenerator().nextDouble();
			r2 = getRandomNumberGenerator().nextDouble();
			totalPropensity = getTotalPropensity();
			delta_t = computeNextTimeStep(r1, totalPropensity);
			nextReactionTime = currentTime.getValue() + delta_t;
			nextEventTime = getNextEventTime();
			nextMaxTime = currentTime.getValue() + getMaxTimeStep();
			previousTime = currentTime.getValue();

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

			if (currentTime.getValue() > getTimeLimit())
			{
				break;
			}

			if (currentTime.getValue() == nextReactionTime)
			{
				update(true, false, false, r2, previousTime);
			}
			else if (currentTime.getValue() == nextEventTime)
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
			HierarchicalUtilities.triggerAndFireEvents(eventList, triggeredEventList, currentTime.getValue());
		}
		HierarchicalUtilities.computeAssignmentRules(assignmentList);

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
		double totalRunningPropensity = getTopmodel().getPropensity();
		if (totalRunningPropensity >= threshold)
		{
			performReaction(getTopmodel(), threshold);
		}
		else
		{
			for (ModelState submodel : getSubmodels().values())
			{
				totalRunningPropensity += submodel.getPropensity();
				if (totalRunningPropensity >= threshold)
				{
					performReaction(submodel, threshold - (totalRunningPropensity - submodel.getPropensity()));
				}
			}
		}
	}

	private void performReaction(ModelState modelstate, double threshold)
	{
		double runningSum = 0;
		for (ReactionNode reactionNode : modelstate.getReactions())
		{
			runningSum += reactionNode.getValue();

			if (runningSum >= threshold)
			{
				reactionNode.fireReaction(runningSum - threshold);
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
		for (EventNode event : eventList)
		{
			event.isTriggeredAtTime(currentTime.getValue());
		}
		HierarchicalUtilities.triggerAndFireEvents(eventList, triggeredEventList, currentTime.getValue());

		if (triggeredEventList != null && !triggeredEventList.isEmpty())
		{
			return triggeredEventList.peek().getFireTime();
		}
		return Double.POSITIVE_INFINITY;
	}
}
