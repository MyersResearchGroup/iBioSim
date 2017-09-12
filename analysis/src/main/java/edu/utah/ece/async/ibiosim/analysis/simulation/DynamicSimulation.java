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

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.SimulationProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.SimulatorSSACR;
import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.SimulatorSSADirect;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalMixedSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalODERKSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalSSADirectSimulator;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class DynamicSimulation extends CoreObservable
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

  public void simulate(AnalysisProperties properties)
  {
    SimulationProperties simProperties = properties.getSimulationProperties();

    try
    {

      String SBMLFileName = properties.getFilename(), outputDirectory = properties.getDirectory(), rootDirectory = properties.getRoot(), quantityType = simProperties.getPrinter_track_quantity();
      double timeLimit = simProperties.getTimeLimit(), maxTimeStep = simProperties.getMaxTimeStep(), minTimeStep = simProperties.getMinTimeStep(), printInterval = simProperties.getPrintInterval(), stoichAmpValue = properties.getAdvancedProperties().getStoichAmp(),
          initialTime = simProperties.getInitialTime(), outputStartTime = simProperties.getOutputStartTime(), absError = simProperties.getAbsError(), relError = simProperties.getRelError();
      long randomSeed = simProperties.getRndSeed();
      String[] interestingSpecies = null;
      int runs = simProperties.getRun(), numSteps = simProperties.getNumSteps();

      switch (simulatorType)
      {
      case CR:
        simulator = new SimulatorSSACR(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, minTimeStep, randomSeed,  printInterval, stoichAmpValue, interestingSpecies, quantityType);
        break;
      case DIRECT:
        simulator = new SimulatorSSADirect(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, minTimeStep, randomSeed,  printInterval, stoichAmpValue, interestingSpecies, quantityType);
        break;
      case HIERARCHICAL_DIRECT:
        simulator = new HierarchicalSSADirectSimulator(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, randomSeed,  printInterval, stoichAmpValue,  interestingSpecies, quantityType, initialTime, outputStartTime);

        break;
      case HIERARCHICAL_RK:
        simulator = new HierarchicalODERKSimulator(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, randomSeed, printInterval, stoichAmpValue, interestingSpecies, numSteps, relError, absError, quantityType, initialTime,
          outputStartTime);
        break;
      case HIERARCHICAL_HYBRID:
        break;
      case HIERARCHICAL_MIXED:
        simulator = new HierarchicalMixedSimulator(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, randomSeed, printInterval, stoichAmpValue, interestingSpecies, quantityType, initialTime, outputStartTime);
        break;
      default:
        message.setLog("The simulation selection was invalid.");
        notifyObservers(message);
        return;
      }
      
      double val1 = System.currentTimeMillis();

      Runtime runtime = Runtime.getRuntime();
      double mb = 1024 * 1024;
      for (int run = 1; run <= runs; ++run)
      {

        if (cancelFlag == true)
        {
          break;
        }

        if (simulator != null)
        {
          simulator.simulate();
          if ((runs - run) >= 1)
          {
            simulator.setupForNewRun(run + 1);
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

          if (simulator != null)
          {
            simulator.printStatisticsTSD();
          }

        }
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