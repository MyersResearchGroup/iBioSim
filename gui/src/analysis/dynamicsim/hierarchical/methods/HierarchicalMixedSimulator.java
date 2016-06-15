package analysis.dynamicsim.hierarchical.methods;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalSimulation;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalEventComparator;
import analysis.dynamicsim.hierarchical.util.io.HierarchicalWriter;
import analysis.dynamicsim.hierarchical.util.math.EventNode;
import analysis.dynamicsim.hierarchical.util.setup.ModelSetup;

public final class HierarchicalMixedSimulator extends HierarchicalSimulation
{

	private Map<String, HierarchicalSimulation>	fbaSims;
	private HierarchicalODERKSimulator			odeSim;
	private HierarchicalSimulation				ssaSim;

	public HierarchicalMixedSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction) throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction, SimType.MIXED);

		this.fbaSims = new HashMap<String, HierarchicalSimulation>();

		initialize(randomSeed, 1);
	}

	@Override
	public void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException
	{
		if (!isInitialized)
		{
			setCurrentTime(0);
			ModelSetup.setupModels(this, false);
			eventList = getEventList();
			variableList = getVariableList();

			assignmentList = getAssignmentRuleList();
			HierarchicalUtilities.computeFixedPoint(variableList, getReactionList());
			setRandomNumberGenerator(new Random(randomSeed));
			if (!eventList.isEmpty())
			{
				triggeredEventList = new PriorityQueue<EventNode>(new HierarchicalEventComparator());
				HierarchicalUtilities.triggerAndFireEvents(eventList, triggeredEventList, currentTime.getValue());
			}

			initStateCopy = getArrayState(variableList);

			setupForOutput(runNumber);
			HierarchicalWriter.setupVariableFromTSD(getBufferedTSDWriter(), getTopmodel(), getSubmodels(), getInterestingSpecies());
			isInitialized = true;
		}

	}

	@Override
	public void simulate()
	{

		// double updateTime = 0.1;
		// try
		// {
		// printValueToTSD(getCurrentTime());
		// }
		// catch (IOException e)
		// {
		// return;
		// }
		// for (HierarchicalSimulation sim : fbaSims.values())
		// {
		// sim.simulate();
		// }
		// HierarchicalUtilities.performAssignmentRules(getTopmodel(),
		// getReplacements(), getCurrentTime());
		//
		// odeSim.setTimeLimit(0);
		// while (getCurrentTime() < getTimeLimit())
		// {
		//
		// for (HierarchicalSimulation sim : fbaSims.values())
		// {
		// sim.simulate();
		// }
		//
		// odeSim.setTimeLimit(odeSim.getCurrentTime() + updateTime);
		// odeSim.simulate();
		// HierarchicalUtilities.performAssignmentRules(getTopmodel(),
		// getReplacements(), getCurrentTime());
		// odeSim.setCurrentTime(getCurrentTime() + updateTime);
		// setCurrentTime(getCurrentTime() + updateTime);
		// try
		// {
		// printValueToTSD(getCurrentTime());
		// }
		// catch (IOException e)
		// {
		// return;
		// }
		//
		// if (getProgress() != null)
		// {
		// getProgress().setValue((int) ((getCurrentTime() / getTimeLimit()) *
		// 100.0));
		// }
		// }
		//
		// try
		// {
		// getBufferedTSDWriter().write(')');
		// getBufferedTSDWriter().flush();
		// }
		// catch (IOException e)
		// {
		// }

	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void clear()
	{
	}

	@Override
	public void setupForNewRun(int newRun)
	{
	}

	private void setupForOutput(long randomSeed, int currentRun)
	{
		try
		{
			String extension = ".tsd";
			FileWriter tsdWriter = new FileWriter(getOutputDirectory() + "run-" + currentRun + extension);
			setBufferedTSDWriter(new BufferedWriter(tsdWriter));
			getBufferedTSDWriter().write('(');

		}
		catch (IOException e)
		{
		}
	}

	@Override
	public void printStatisticsTSD()
	{
		// TODO Auto-generated method stub

	}

	// // TODO: fix this
	// private void setupSubmodels(long randomSeed) throws XMLStreamException,
	// IOException
	// {
	// odeSim = new HierarchicalODERKSimulator(this, getTopmodel(), randomSeed);
	//
	// for (ModelState modelstate : getSubmodels().values())
	// {
	// if (modelstate.getModelType() == ModelState.ModelType.HFBA)
	// {
	// HierarchicalFBASimulator fbaSim = new HierarchicalFBASimulator(this);
	// fbaSim.setFBA(getModels().get(modelstate.getModel()));
	// fbaSims.put(modelstate.getID(), fbaSim);
	// odeSim.getSubmodels().remove(modelstate.getID());
	// }
	// }
	// }

}
