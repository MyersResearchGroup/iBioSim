package dynamicsim;

import java.io.IOException;

import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

public class SimulatorSSADirect extends Simulator{

	public SimulatorSSADirect(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval) 
	throws IOException, XMLStreamException {
		
		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, randomSeed,
				progress, printInterval);
	}

	protected void simulate() {
		
		
	}

}
