package analysis.dynamicsim;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import main.util.dataparser.TSDParser;

public class DynSim {
	 
	static double timeLimit = 5.0;
	static double relativeError = 1e-3;
	static double absoluteError = 1e-7;
	static int numSteps;
	static double maxTimeStep = 1.0;
	static double minTimeStep = 0.0;
	static long randomSeed = 0;
	static double printInterval = 0.1;
	static int runs = 1;
	static double stoichAmpValue = 1.0;
	static boolean genStats = false;
	static String selectedSimulator = "";
	static ArrayList<String> interestingSpecies = new ArrayList<String>();
//	static String[] amountSpecies = new String[0];
//	static String[] concSpecies = new String[0];
	static String quantityType = "amount";
	

	/**
	 * entry point for simulator-only execution
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		boolean testSuite = true;
		
		if (args.length == 0) {
			
			//this is for the sbml test suite
			
			String caseNum = "00834";
			
			args = new String[3];
			
			//model
			args[0] = "/home/beauregard/Desktop/cases/semantic/" + caseNum + "/" + caseNum + "-sbml-l3v1.xml";
			
			//output dir
			args[1] = "/home/beauregard/Desktop/cases/semantic/" + caseNum + "/";
			
			//properties file
			args[2] = "/home/beauregard/Desktop/cases/semantic/" + caseNum + "/" + caseNum + "-settings.properties";
		}
		
		String filename = args[0];
		String outputDirectory = args[1];
		String settingsFile = args[2];
		JLabel progressLabel = new JLabel();
		JProgressBar progress = new JProgressBar();
		JFrame running = new JFrame();
		DynamicSimulation simulator = null;
		
		if (testSuite == true) {
			
			readSettings(settingsFile);			
			printInterval = timeLimit / numSteps;			
			simulator = new DynamicSimulation("rk");
		}
		else {
			
			readProperties(settingsFile);
			
			if (selectedSimulator.contains("SSA-CR"))
				simulator = new DynamicSimulation("cr");
			else if (selectedSimulator.contains("Direct"))
				simulator = new DynamicSimulation("direct");
			else
				simulator = new DynamicSimulation("rk");
		}
		
		String[] intSpecies = new String[interestingSpecies.size()];
		int i = 0;
		
		for (String intSpec : interestingSpecies) {
			intSpecies[i] = intSpec; ++i;
		}
		
		simulator.simulate(filename, outputDirectory, timeLimit, maxTimeStep, minTimeStep, 
				randomSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, 
				intSpecies, numSteps, relativeError, absoluteError, quantityType, genStats);
		
		TSDParser tsdp = new TSDParser(outputDirectory + "run-1.tsd", true);		
		tsdp.outputCSV(outputDirectory + "run-1.csv");
	}
	
	private static void readProperties(String filename) {
		
		File f = new File(filename);
		Properties properties = new Properties();
		FileInputStream in;
		  
		try {
			in = new FileInputStream(f);
			properties.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String simMethod = properties.getProperty("reb2sac.simulation.method");
		String prefix = "monte.carlo.";
		
		if (simMethod.equals("ODE"))
			prefix = "ode.";
		
		if (properties.containsKey(prefix + "simulation.time.limit"))
		timeLimit = Double.valueOf(properties.getProperty(prefix + "simulation.time.limit"));
		if (properties.containsKey(prefix + "simulation.time.step"))
		maxTimeStep = Double.valueOf(properties.getProperty(prefix + "simulation.time.step"));
		if (properties.containsKey(prefix + "simulation.print.interval"))
		printInterval = Double.valueOf(properties.getProperty(prefix + "simulation.print.interval"));
		if (properties.containsKey("monte.carlo.simulation.runs"))
		runs = Integer.valueOf(properties.getProperty("monte.carlo.simulation.runs"));
		if (properties.containsKey("reb2sac.generate.statistics"))
		genStats = Boolean.valueOf(properties.getProperty("reb2sac.generate.statistics"));
		if (properties.containsKey("monte.carlo.simulation.random.seed"))
		randomSeed = Long.valueOf(properties.getProperty("monte.carlo.simulation.random.seed"));
		if (properties.containsKey("simulation.printer.tracking.quantity"))
		quantityType = properties.getProperty("simulation.printer.tracking.quantity");
		if (properties.containsKey("monte.carlo.simulation.min.time.step"))
		minTimeStep = Double.valueOf(properties.getProperty("monte.carlo.simulation.min.time.step"));
		if (properties.containsKey("reb2sac.diffusion.stoichiometry.amplification.value"))
		stoichAmpValue = Double.valueOf(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));
		if (properties.containsKey("selected.simulator"))
		selectedSimulator = properties.getProperty("selected.simulator");
		//relativeError = Double.valueOf(properties.getProperty("relative"));
		if (properties.containsKey("ode.simulation.absolute.error"))
		absoluteError = Double.valueOf(properties.getProperty("ode.simulation.absolute.error"));
		if (properties.containsKey(prefix + "simulation.number.steps"))
		numSteps = Integer.valueOf(properties.getProperty(prefix + "simulation.number.steps"));		
			
		int intSpecies = 1;
		
		while (properties.containsKey("reb2sac.interesting.species." + intSpecies)) {
			
			interestingSpecies.add(properties.getProperty("reb2sac.interesting.species." + intSpecies));
			++intSpecies;
		}
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
		
		for (String intSpecies : properties.getProperty("variables").split(", "))
			interestingSpecies.add(intSpecies);		
		
		quantityType = properties.getProperty("simulation.printer.tracking.quantity");
	}
}
