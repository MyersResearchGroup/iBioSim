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
import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.SimulatorODERK;
import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.SimulatorSSACR;
import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.SimulatorSSADirect;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalMixedSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalODERKSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalSSADirectSimulator;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;

/**
 * Used to run a simulation method.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class DynamicSimulation extends CoreObservable {

  // simulator type
  private final SimulationType simulatorType;
  private final Message message;

  // the simulator object
  private AbstractSimulator simulator;
  private boolean cancelFlag;
  private boolean statisticsFlag;

  public static enum SimulationType {
    CR, DIRECT, RK, HIERARCHICAL_DIRECT, HIERARCHICAL_HYBRID, HIERARCHICAL_RK, HIERARCHICAL_MIXED;
  }

  /**
   * Constructs a dynamic simulation object.
   */
  public DynamicSimulation(SimulationType type) {
    simulatorType = type;
    message = new Message();
  }

  /**
   * Runs simulation using the given analysis properties.
   *
   * @param properties
   *          - specifies the configurations to setup simulation.
   * @param filename
   *          - the model to simulate.
   */
  public void simulate(AnalysisProperties properties, String filename) {
    SimulationProperties simProperties = properties.getSimulationProperties();

    try {
      String SBMLFileName = filename, outputDirectory = properties.getOutDir().equals(".") ? properties.getDirectory() : properties.getOutDir();
      properties.getRoot();
      String quantityType = simProperties.getPrinter_track_quantity();
      double timeLimit = simProperties.getTimeLimit(), maxTimeStep = simProperties.getMaxTimeStep(), minTimeStep = simProperties.getMinTimeStep(), printInterval = simProperties.getPrintInterval(), stoichAmpValue = properties.getAdvancedProperties().getStoichAmp();
      simProperties.getOutputStartTime();
      double absError = simProperties.getAbsError(), relError = simProperties.getRelError();
      simProperties.getInitialTime();
      long randomSeed = simProperties.getRndSeed();
      String[] interestingSpecies = simProperties.getIntSpecies().toArray(new String[simProperties.getIntSpecies().size()]);
      int runs = simProperties.getRun(), numSteps = simProperties.getNumSteps();
      if (numSteps == 0) {
        numSteps = (int) (timeLimit / printInterval);
      }
      switch (simulatorType) {
      case RK:
        simulator = new SimulatorODERK(SBMLFileName, outputDirectory, runs, timeLimit, maxTimeStep, randomSeed, printInterval, stoichAmpValue, interestingSpecies, numSteps, relError, absError, quantityType);
        simulator.addObservable(this);
        break;
      case CR:
        simulator = new SimulatorSSACR(SBMLFileName, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, randomSeed, printInterval, stoichAmpValue, interestingSpecies, quantityType);
        simulator.addObservable(this);
        break;
      case DIRECT:
        simulator = new SimulatorSSADirect(SBMLFileName, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, randomSeed, printInterval, stoichAmpValue, interestingSpecies, quantityType);
        simulator.addObservable(this);
        break;
      case HIERARCHICAL_DIRECT:
        simulator = new HierarchicalSSADirectSimulator(properties);
        simulator.addObservable(this);
        break;
      case HIERARCHICAL_RK:
        simulator = new HierarchicalODERKSimulator(properties);
        simulator.addObservable(this);
        break;
      case HIERARCHICAL_HYBRID:
        break;
      case HIERARCHICAL_MIXED:
        simulator = new HierarchicalMixedSimulator(properties);
        simulator.addObservable(this);
        break;
      default:
        message.setLog("The simulation selection was invalid.");
        notifyObservers(message);
        return;
      }

      for (int run = 1; run <= runs; ++run) {
        if (cancelFlag == true) {
          break;
        }
        if (simulator != null) {
          simulator.simulate();
          if ((runs - run) >= 1) {
            simulator.setupForNewRun(run + 1);
          }
        }
        if (cancelFlag == false && statisticsFlag == true) {
          if (simulator != null) {
            simulator.printStatisticsTSD();
          }

        }
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      return;
    }
    catch (XMLStreamException e) {
      e.printStackTrace();
      return;
    }
    catch (BioSimException e) {
      e.printStackTrace();
      return;
    }

  }

  /**
   * Cancels the simulation on the next iteration called from outside the
   * class when the user closes the progress bar dialog
   */
  public void cancel() {
    if (simulator != null) {
      simulator.cancel();
      cancelFlag = true;
      message.setCancel();
      notifyObservers(message);
    }
  }

  @Override
  public boolean send(RequestType type, Message message) {
    if (parent != null) { return parent.send(type, message); }
    return true;
  }

}