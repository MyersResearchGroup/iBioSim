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
package edu.utah.ece.async.ibiosim.analysis.simulation;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Observable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.xml.stream.XMLStreamException;

import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.SimulatorSSACR;
import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.SimulatorSSADirect;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalMixedSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalODERKSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalSSADirectSimulator;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class DynamicSimulation extends Observable
{

	// simulator type
	private final SimulationType	simulatorType;
	private final Message message;
	
	// the simulator object
	private ParentSimulator			simulator;
	private boolean					cancelFlag;
	private boolean					statisticsFlag;
	
	
	public static enum SimulationType
	{
		CR, DIRECT, RK, HIERARCHICAL_DIRECT, HIERARCHICAL_HYBRID, HIERARCHICAL_RK, HIERARCHICAL_MIXED;
	}

	/**
	 * constructor; sets the simulator type
	 */
	public DynamicSimulation(SimulationType type)
	{
		simulatorType = type;
		message = new Message();
	}

	public void simulate(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, int runs, JLabel progressLabel, JFrame running, double stoichAmpValue,
			String[] interestingSpecies, int numSteps, double relError, double absError, String quantityType, Boolean genStats, JTabbedPane simTab, String abstraction,  double initialTime, double outputStartTime)
	{
		String progressText = "";

		if (progressLabel != null)
		{
			progressText = progressLabel.getText();
			statisticsFlag = genStats;
		}
		try
		{

			if (progressLabel != null)
			{
				progressLabel.setText("Generating Model . . .");
				running.setMinimumSize(new Dimension((progressLabel.getText().length() * 10) + 20, (int) running.getSize().getHeight()));
			}

			switch (simulatorType)
			{
			case CR:
				simulator = new SimulatorSSACR(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType);

				break;
			case DIRECT:
				simulator = new SimulatorSSADirect(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType);

				break;
			case HIERARCHICAL_DIRECT:
				simulator = new HierarchicalSSADirectSimulator(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction, initialTime, outputStartTime);

				break;
			case HIERARCHICAL_RK:
				simulator = new HierarchicalODERKSimulator(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, interestingSpecies, numSteps, relError, absError, quantityType, abstraction, initialTime,
						outputStartTime);
				break;
			case HIERARCHICAL_HYBRID:
				// simulator = new HierarchicalHybridSimulator(SBMLFileName,
				// rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep,
				// minTimeStep, randomSeed, progress, printInterval,
				// stoichAmpValue, running, interestingSpecies, quantityType,
				// abstraction);
				break;
			case HIERARCHICAL_MIXED:
				simulator = new HierarchicalMixedSimulator(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction, initialTime, outputStartTime);
				break;
			default:
		    message.setLog("The simulation selection was invalid.");
		    notifyObservers(message);
				return;

			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
		catch (XMLStreamException e)
		{
			e.printStackTrace();
			return;
		}
		catch (BioSimException e)
		{
			e.printStackTrace();
			return;
		}
		double val1 = System.currentTimeMillis();

		Runtime runtime = Runtime.getRuntime();
		double mb = 1024 * 1024;
		// int count = 0, total = 0;
		for (int run = 1; run <= runs; ++run)
		{

			if (cancelFlag == true)
			{
				break;
			}

			if (progressLabel != null && running != null)
			{
				progressLabel.setText(progressText.replace(" (" + (run - 1) + ")", "") + " (" + run + ")");
				running.setMinimumSize(new Dimension((progressLabel.getText().length() * 10) + 20, (int) running.getSize().getHeight()));
			}
			if (simulator != null)
			{
				simulator.simulate();
				// count += ((HierarchicalSSADirectSimulator)
				// simulator).getTopLevelValue("counter");
				// total += ((HierarchicalSSADirectSimulator)
				// simulator).getTopLevelValue("n")
				// * ((HierarchicalSSADirectSimulator)
				// simulator).getTopLevelValue("n");
				if ((runs - run) >= 1)
				{
					simulator.setupForNewRun(run + 1);
				}
			}
		}

		System.gc();
		double mem = (runtime.totalMemory() - runtime.freeMemory()) / mb;
		double val2 = System.currentTimeMillis();

		simulator = null;
		System.gc();
		System.runFinalization();


    message.setLog("Memory used: " + (mem) + "MB, Simulation Time: " + (val2 - val1) / 1000 + "secs");
    notifyObservers(message);

		if (cancelFlag == false && statisticsFlag == true)
		{
			if (progressLabel != null && running != null)
			{

				progressLabel.setText("Generating Statistics . . .");
				running.setMinimumSize(new Dimension(200, 100));
			}

			if (simulator != null)
			{
				simulator.printStatisticsTSD();
			}

		}
//		if (simTab != null)
//		{
//			for (int i = 0; i < simTab.getComponentCount(); i++)
//			{
//				if (simTab.getComponentAt(i).getName().equals("TSD Graph"))
//				{
//					if (simTab.getComponentAt(i) instanceof Graph)
//					{
//						((Graph) simTab.getComponentAt(i)).refresh();
//					}
//				}
//			}
//		}
	}

	/**
	 * cancels the simulation on the next iteration called from outside the
	 * class when the user closes the progress bar dialog
	 */
	public void cancel()
	{

		if (simulator != null)
		{

			simulator.cancel();

			
			cancelFlag = true;

      message.setCancel();
      notifyObservers(message);
		}
	}


}