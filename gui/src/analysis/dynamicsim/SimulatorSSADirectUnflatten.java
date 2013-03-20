package analysis.dynamicsim;

import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

public class SimulatorSSADirectUnflatten extends Simulator{
	private static Long initializationTime = new Long(0);
	public SimulatorSSADirectUnflatten(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			double stoichAmpValue, JFrame running, String[] interestingSpecies, String quantityType) 
	throws IOException, XMLStreamException {
		
		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, minTimeStep, randomSeed,
				progress, printInterval, initializationTime, stoichAmpValue, running, interestingSpecies, quantityType);
		
		System.out.println("test");
	}


	@Override
	protected void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void eraseComponentFurther(HashSet<String> reactionIDs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setupForNewRun(int newRun) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void simulate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateAfterDynamicChanges() {
		// TODO Auto-generated method stub
		
	}

}
