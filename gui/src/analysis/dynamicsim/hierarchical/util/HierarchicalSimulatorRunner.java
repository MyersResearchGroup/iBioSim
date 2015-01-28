package analysis.dynamicsim.hierarchical.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import main.Gui;
import main.util.dataparser.TSDParser;
import analysis.dynamicsim.DynamicSimulation;

public class HierarchicalSimulatorRunner
{

	static double				timeLimit			= 5.0;
	static double				relativeError		= 1e-6;
	static double				absoluteError		= 1e-9;
	static int					numSteps;
	static double				maxTimeStep			= Double.POSITIVE_INFINITY;
	static double				minTimeStep			= 0.0;
	static long					randomSeed			= 0;
	static double				printInterval		= 0;
	static int					runs				= 1;
	static double				stoichAmpValue		= 1.0;
	static boolean				genStats			= false;
	static String				selectedSimulator	= "";
	static ArrayList<String>	interestingSpecies	= new ArrayList<String>();
	static String				quantityType		= "amount";

	/**
	 * @param args
	 *            %d = args[0] = path to cases %n = args[1] = case id %o =
	 *            args[2] = output path
	 */
	public static void main(String[] args)
	{

		if (args.length < 1)
		{
			System.out.println("Missing arguments");
			return;
		}

		String separator = Gui.separator;

		String testcase = args[1];

		String[] casesNeedToChangeTimeStep = new String[] { "00028", "00080", "00128", "00173",
				"00194", "00196", "00197", "00198", "00200", "00201", "00269", "00274", "00400",
				"00460", "00276", "00278", "00279", "00870", "00872" };

		for (String s : casesNeedToChangeTimeStep)
		{
			if (s.equals(testcase))
			{
				maxTimeStep = 0.001;
				break;
			}
		}

		String filename = args[0] + separator + testcase + separator + testcase + "-sbml-l3v1.xml";
		String outputDirectory = args[2];

		String settingsFile = args[0] + separator + testcase + separator + testcase
				+ "-settings.txt";
		JLabel progressLabel = new JLabel();
		JProgressBar progress = new JProgressBar();
		JFrame running = new JFrame();
		DynamicSimulation simulator = null;

		readSettings(settingsFile);
		if (printInterval == 0)
		{
			printInterval = timeLimit / numSteps;
		}

		simulator = new DynamicSimulation("hierarchical-rk");

		String[] intSpecies = new String[interestingSpecies.size()];
		int i = 0;

		for (String intSpec : interestingSpecies)
		{
			intSpecies[i] = intSpec;
			++i;
		}

		try
		{

			simulator.simulate(filename, args[0] + separator + testcase, outputDirectory,
					timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval, runs,
					progressLabel, running, stoichAmpValue, intSpecies, numSteps, relativeError,
					absoluteError, quantityType, false, null, null, null);

			TSDParser tsdp = new TSDParser(outputDirectory + "run-1.tsd", true);
			tsdp.outputCSV(outputDirectory + testcase + ".csv");

		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "Unable to create sbml file.",
					"Error Creating File", JOptionPane.ERROR_MESSAGE);
		}

	}

	private static void readSettings(String filename)
	{

		File f = new File(filename);
		Properties properties = new Properties();
		FileInputStream in;

		try
		{
			in = new FileInputStream(f);
			properties.load(in);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		timeLimit = Double.valueOf(properties.getProperty("duration"))
				- Double.valueOf(properties.getProperty("start"));
		relativeError = Double.valueOf(properties.getProperty("relative"));
		absoluteError = Double.valueOf(properties.getProperty("absolute"));
		numSteps = Integer.valueOf(properties.getProperty("steps"));

		for (String intSpecies : properties.getProperty("variables").replaceAll(" ", "").split(","))
		{
			interestingSpecies.add(intSpecies);
		}

		quantityType = properties.getProperty("concentration");
	}

}
