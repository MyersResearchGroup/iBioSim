package analysis.dynamicsim;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.sbml.jsbml.SBMLDocument;

import biomodel.network.GeneticNetwork;
import biomodel.parser.BioModel;
import biomodel.parser.GCMParser;

import main.Gui;
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
		String varname;
		if (System.getProperty("mrj.version") != null)
			varname = "DYLD_LIBRARY_PATH"; // We're on a Mac.
		else
			varname = "LD_LIBRARY_PATH"; // We're not on a Mac.
		try {
			System.loadLibrary("sbmlj");
			// For extra safety, check that the jar file is in the
			// classpath.
			Class.forName("org.sbml.jsbml.libsbml");
		}
		catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			System.err.println("Error: could not link with the libSBML library." + "  It is likely\nyour " + varname
					+ " environment variable does not include\nthe" + " directory containing the libsbml library file.");
			System.exit(1);
		}
		catch (ClassNotFoundException e) {
			System.err.println("Error: unable to load the file libsbmlj.jar." + "  It is likely\nyour " + varname + " environment"
					+ " variable or CLASSPATH variable\ndoes not include" + " the directory containing the libsbmlj.jar file.");
			System.exit(1);
		}
		catch (SecurityException e) {
			System.err.println("Could not load the libSBML library files due to a" + " security exception.");
			System.exit(1);
		}
		
		if (args.length == 0) {
			//this is for the sbml test suite
			
			String caseNum = "00068";
			
			args = new String[3];
			
			//model
			args[0] = "/home/leandro/cases/semantic/" + caseNum + "/" + caseNum + "-sbml-l2v4.xml";
			
			//output dir
			args[1] = "/home/leandro/cases/semantic/" + caseNum + "/";
			//args[1] = "/home/leandro/cases/semantic/" + caseNum;
			
			//properties file
			args[2] = "/home/leandro/cases/semantic/" + caseNum + "/" + caseNum + "-settings.txt";
		}
		
		String filename = args[0];
		String outputDirectory = args[1];
		String settingsFile = args[2];
		JLabel progressLabel = new JLabel();
		JProgressBar progress = new JProgressBar();
		JFrame running = new JFrame();
		DynamicSimulation simulator = null;
		
		if (testSuite) {
			
			readSettings(settingsFile);			
			printInterval = timeLimit / numSteps;			
			simulator = new DynamicSimulation("rk");
		}
		else {
			
			readProperties(settingsFile);
			
			if (selectedSimulator.contains("SSA-CR"))
				simulator = new DynamicSimulation("cr");
			else if (selectedSimulator.contains("SSA-Direct"))
				simulator = new DynamicSimulation("direct");
			else
				simulator = new DynamicSimulation("rk");
		}
		
		String[] intSpecies = new String[interestingSpecies.size()];
		int i = 0;
		
		for (String intSpec : interestingSpecies) {
			intSpecies[i] = intSpec; ++i;
		}
		
		try {
		
		 	BioModel biomodel = new BioModel(outputDirectory);
		 	biomodel.load(filename);
			SBMLDocument sbml = biomodel.flattenModel(true);		
			GCMParser parser = new GCMParser(biomodel);
			GeneticNetwork network = parser.buildNetwork(sbml);
			sbml = network.getSBML();
			network.mergeSBML(filename, sbml);
			simulator.simulate(filename, outputDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep, 
					randomSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, 
					intSpecies, numSteps, relativeError, absoluteError, quantityType, genStats, null, null);
			
			TSDParser tsdp = new TSDParser(outputDirectory + "run-1.tsd", true);		
			tsdp.outputCSV(outputDirectory + "run-1.csv");
		
		}
		catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "Unable to create sbml file.",
					"Error Creating File", JOptionPane.ERROR_MESSAGE);
		}
		
		
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
