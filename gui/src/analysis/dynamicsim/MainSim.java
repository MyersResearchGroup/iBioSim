package analysis.dynamicsim;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import main.util.dataparser.TSDParser;

public class MainSim {
	 
	static double timeLimit = 5.0;
	static double relativeError = 1e-3;
	static double absoluteError = 1e-7;
	static int numSteps;
	static String[] interestingSpecies = new String[0];
//	static String[] amountSpecies = new String[0];
//	static String[] concSpecies = new String[0];
	static String quantityType = "amount";
	

	/**
	 * entry point for simulator-only execution
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length == 0) {
			
			String caseNum = "00625";
			
			args = new String[3];
			args[0] = "/home/beauregard/Desktop/cases/semantic/" + caseNum + "/" + caseNum + "-sbml-l3v1.xml";
			args[1] = "/home/beauregard/Desktop/cases/semantic/" + caseNum + "/";
			args[2] = "/home/beauregard/Desktop/cases/semantic/" + caseNum + "/" + caseNum + "-settings.properties";
		}
		
		String filename = args[0];
		String outputDirectory = args[1];
		String settingsFile = args[2];
		double maxTimeStep = 1.0;
		long randomSeed = 0;
		JProgressBar progress = new JProgressBar();
		double printInterval = 0.1;
		int runs = 1;
		JLabel progressLabel = new JLabel();
		double stoichAmpValue = 1.0;
		JFrame running = new JFrame();
		
		readSettings(settingsFile);
		
		printInterval = timeLimit / numSteps;
		
		DynamicSimulation simulator = new DynamicSimulation("rk");
		
		simulator.simulate(filename, outputDirectory, timeLimit, 
				maxTimeStep, randomSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, 
				interestingSpecies, numSteps, relativeError, absoluteError, quantityType);
		
		TSDParser tsdp = new TSDParser(outputDirectory + "run-1.tsd", true);
		tsdp.outputCSV(outputDirectory + "run-1.csv");
	}	
	
	private static void readSettings(String filename) {
		
		File f = new File(filename);
		Properties properties = new Properties();
		FileInputStream in;
		  
		try {
			in = new FileInputStream(f);
			properties.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		timeLimit = Double.valueOf(properties.getProperty("duration")) - 
			Double.valueOf(properties.getProperty("start"));
		relativeError = Double.valueOf(properties.getProperty("relative"));
		absoluteError = Double.valueOf(properties.getProperty("absolute"));
		numSteps = Integer.valueOf(properties.getProperty("steps"));
		interestingSpecies = properties.getProperty("variables").split(", ");
		quantityType = properties.getProperty("simulation.printer.tracking.quantity");
	}
}
