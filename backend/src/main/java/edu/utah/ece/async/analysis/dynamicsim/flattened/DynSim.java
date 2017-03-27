/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package main.java.edu.utah.ece.async.analysis.dynamicsim.flattened;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;

import dataModels.biomodel.parser.BioModel;
import dataModels.util.dataparser.TSDParser;
import main.java.edu.utah.ece.async.analysis.dynamicsim.DynamicSimulation;
import main.java.edu.utah.ece.async.analysis.dynamicsim.DynamicSimulation.SimulationType;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class DynSim
{

	static double				timeLimit			= 5.0;
	static double				relativeError		= 1e-3;
	static double				absoluteError		= 1e-7;
	static int					numSteps;
	static double				maxTimeStep			= 1.0;
	static double				minTimeStep			= 0.0;
	static long					randomSeed			= 0;
	static double				printInterval		= 0.1;
	static int					runs				= 1;
	static double				stoichAmpValue		= 1.0;
	static boolean				genStats			= false;
	static String				selectedSimulator	= "";
	static ArrayList<String>	interestingSpecies	= new ArrayList<String>();
	static String				separator			= (File.separator.equals("\\")) ? "\\\\" : File.separator;
	// static String[] amountSpecies = new String[0];
	// static String[] concSpecies = new String[0];
	static String				quantityType		= "amount";

	/**
	 * entry point for simulator-only execution
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{

		boolean testSuite = true;
		// try
		// {
		// System.loadLibrary("sbmlj");
		// // For extra safety, check that the jar file is in the
		// // classpath.
		// Class.forName("org.sbml.jsbml.libsbml");
		// }
		// catch (UnsatisfiedLinkError e)
		// {
		// e.printStackTrace();
		// System.err.println("Error: could not link with the libSBML library."
		// + "  It is likely\nyour " + varname
		// + " environment variable does not include\nthe"
		// + " directory containing the libsbml library file.");
		// System.exit(1);
		// }
		// catch (ClassNotFoundException e)
		// {
		// System.err.println("Error: unable to load the file libsbmlj.jar."
		// + "  It is likely\nyour " + varname + " environment"
		// + " variable or CLASSPATH variable\ndoes not include"
		// + " the directory containing the libsbmlj.jar file.");
		// System.exit(1);
		// }
		// catch (SecurityException e)
		// {
		// System.err.println("Could not load the libSBML library files due to a"
		// + " security exception.");
		// System.exit(1);
		// }

		if (args.length == 0)
		{
			System.out.println("Not enough arguments");
			return;
		}

		String filename = "";
		String outputDirectory = "";
		String testcase = "";
		String settingsFile = "";

		String newFilename;
		JLabel progressLabel = null;
		JProgressBar progress = null;
		JFrame running = null;
		DynamicSimulation simulator = null;

		if (testSuite)
		{
			testcase = args[1];
			filename = args[0] + separator + testcase + separator + testcase + "-sbml-l3v1.xml";
			outputDirectory = args[2];
			settingsFile = args[0] + separator + testcase + separator + testcase + "-settings.txt";
			newFilename = outputDirectory + separator + testcase + "_flatten.xml";
			readSettings(settingsFile);
			printInterval = timeLimit / numSteps;
			simulator = new DynamicSimulation(SimulationType.RK);
		}
		else
		{

			filename = args[0];
			outputDirectory = args[1];
			settingsFile = args[2];
			newFilename = filename.replace(".xml", "_flatten.xml");
			readProperties(settingsFile);

			if (selectedSimulator.contains("SSA-CR"))
			{
				simulator = new DynamicSimulation(SimulationType.CR);
			}
			else if (selectedSimulator.contains("SSA-Direct"))
			{
				simulator = new DynamicSimulation(SimulationType.DIRECT);
			}
			else
			{
				simulator = new DynamicSimulation(SimulationType.RK);
			}
		}

		String[] intSpecies = new String[interestingSpecies.size()];

		int i = 0;
		for (String intSpec : interestingSpecies)
		{
			intSpecies[i] = intSpec;
			++i;
		}

		try
		{
			double t1 = System.currentTimeMillis();
			BioModel biomodel = new BioModel(outputDirectory);
			biomodel.load(filename);
			SBMLDocument flatten = biomodel.flattenModel(true);
			SBMLWriter.write(flatten, newFilename, ' ', (short) 2);
			double t2 = System.currentTimeMillis();

			System.out.println("Flattening time: " + (t2 - t1) / 1000);
			if (testSuite)
			{
				simulator.simulate(filename, outputDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, numSteps, relativeError, absoluteError, quantityType, genStats, null, null, 0, 0);

				TSDParser tsdp = new TSDParser(outputDirectory + "run-1.tsd", true);
				tsdp.outputCSV(outputDirectory + testcase + ".csv");
			}
			else
			{
				simulator.simulate(newFilename, outputDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, numSteps, relativeError, absoluteError, quantityType, genStats, null, null, 0,
						0);
			}

		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

	}

	private static void readProperties(String filename)
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

		String simMethod = properties.getProperty("reb2sac.simulation.method");
		String prefix = "monte.carlo.";

		if (simMethod.equals("ODE"))
		{
			prefix = "ode.";
		}

		if (properties.containsKey(prefix + "simulation.time.limit"))
		{
			timeLimit = Double.valueOf(properties.getProperty(prefix + "simulation.time.limit"));
		}
		if (properties.containsKey(prefix + "simulation.time.step"))
		{
			if (properties.getProperty(prefix + "simulation.time.step").equals("inf"))
			{
				maxTimeStep = Double.POSITIVE_INFINITY;
			}
			else
			{
				maxTimeStep = Double.valueOf(properties.getProperty(prefix + "simulation.time.step"));
			}
		}
		if (properties.containsKey(prefix + "simulation.print.interval"))
		{
			printInterval = Double.valueOf(properties.getProperty(prefix + "simulation.print.interval"));
		}
		if (properties.containsKey("monte.carlo.simulation.runs"))
		{
			runs = Integer.valueOf(properties.getProperty("monte.carlo.simulation.runs"));
		}
		if (properties.containsKey("reb2sac.generate.statistics"))
		{
			genStats = Boolean.valueOf(properties.getProperty("reb2sac.generate.statistics"));
		}
		if (properties.containsKey("monte.carlo.simulation.random.seed"))
		{
			randomSeed = Long.valueOf(properties.getProperty("monte.carlo.simulation.random.seed"));
		}
		if (properties.containsKey("simulation.printer.tracking.quantity"))
		{
			quantityType = properties.getProperty("simulation.printer.tracking.quantity");
		}
		if (properties.containsKey("monte.carlo.simulation.min.time.step"))
		{
			minTimeStep = Double.valueOf(properties.getProperty("monte.carlo.simulation.min.time.step"));
		}
		if (properties.containsKey("reb2sac.diffusion.stoichiometry.amplification.value"))
		{
			stoichAmpValue = Double.valueOf(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));
		}
		if (properties.containsKey("selected.simulator"))
		{
			selectedSimulator = properties.getProperty("selected.simulator");
		}
		// relativeError = Double.valueOf(properties.getProperty("relative"));
		if (properties.containsKey("ode.simulation.absolute.error"))
		{
			absoluteError = Double.valueOf(properties.getProperty("ode.simulation.absolute.error"));
		}
		if (properties.containsKey(prefix + "simulation.number.steps"))
		{
			numSteps = Integer.valueOf(properties.getProperty(prefix + "simulation.number.steps"));
		}

		int intSpecies = 1;

		while (properties.containsKey("reb2sac.interesting.species." + intSpecies))
		{

			interestingSpecies.add(properties.getProperty("reb2sac.interesting.species." + intSpecies));
			++intSpecies;
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

		timeLimit = Double.valueOf(properties.getProperty("duration")) - Double.valueOf(properties.getProperty("start"));
		relativeError = Double.valueOf(properties.getProperty("relative"));
		absoluteError = Double.valueOf(properties.getProperty("absolute"));
		numSteps = Integer.valueOf(properties.getProperty("steps"));

		for (String intSpecies : properties.getProperty("variables").split(", "))
		{
			interestingSpecies.add(intSpecies);
		}

		quantityType = properties.getProperty("simulation.printer.tracking.quantity");
	}
}
