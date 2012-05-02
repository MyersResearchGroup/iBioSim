package analysis.dynamicsim;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class MainSim {

	/**
	 * entry point for simulator-only execution
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length == 0) {
			
			args = new String[2];
			args[0] = "/home/beauregard/myers_projects/sandbox/ngmd/ngmd.xml";
			args[1] = "/home/beauregard/Desktop/";
		}
			
		
		String filename = args[0];
		String outputDirectory = args[1];
		double timeLimit = 50.0;
		double maxTimeStep = 1.0;
		long randomSeed = 0;
		JProgressBar progress = new JProgressBar();
		double printInterval = 1.0;
		int runs = 1;
		JLabel progressLabel = new JLabel();
		double stoichAmpValue = 1.0;
		JFrame running = new JFrame();
		String[] interestingSpecies = new String[0];
		
		DynamicSimulation simulator = new DynamicSimulation("rk");
		
		simulator.simulate(filename, outputDirectory, timeLimit, 
				maxTimeStep, randomSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, 
				interestingSpecies);
	}
}
