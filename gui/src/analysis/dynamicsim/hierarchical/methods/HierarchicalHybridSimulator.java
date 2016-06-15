package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

public final class HierarchicalHybridSimulator extends HierarchicalSSADirectSimulator
{

	public HierarchicalHybridSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction) throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction);
	}

	public void initialize()
	{

	}

	// @Override
	// public double computeNextTimeStep(double r1, double totalPropensity)
	// {
	//
	// return 0;
	// }
	//
	// @Override
	// public String selectReaction(double r2)
	// {
	// return "";
	// }

}
