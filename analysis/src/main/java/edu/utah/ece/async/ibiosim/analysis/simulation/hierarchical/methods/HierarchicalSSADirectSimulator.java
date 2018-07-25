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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods;

import java.io.IOException;
import java.util.PriorityQueue;

import javax.xml.stream.XMLStreamException;

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.SimulationProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup.ModelSetup;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * Hierarchical SSA simulator.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HierarchicalSSADirectSimulator extends HierarchicalSimulation {
  private final boolean print;

  /**
   * Creates an instance of a SSA simulator.
   *
   * @param properties
   *          - the analysis properties.
   * @throws IOException
   *           - if there is a problem with the model file.
   * @throws XMLStreamException
   *           - if there is a problem parsing the SBML file.
   * @throws BioSimException
   *           - if an error occur in the initialization.
   */
  public HierarchicalSSADirectSimulator(AnalysisProperties properties) throws IOException, XMLStreamException, BioSimException {
    super(properties, SimType.HSSA);
    this.print = true;

  }

  /**
   * Creates an instance of an ODE simulator.
   *
   * @param properties
   *          - the analysis properties.
   * @param print
   *          - whether to save the output.
   * @throws IOException
   *           - if there is a problem with the model file.
   * @throws XMLStreamException
   *           - if there is a problem parsing the SBML file.
   * @throws BioSimException
   *           - if an error occur in the initialization.
   */
  public HierarchicalSSADirectSimulator(AnalysisProperties properties, boolean print) throws IOException, XMLStreamException, BioSimException {
    super(properties, SimType.HSSA);
    this.print = print;

  }

  /**
   * Initializes the simulator.
   *
   * @param runNumber
   *          - the run index.
   * @throws IOException
   *           - if there is a problem with the model file.
   * @throws XMLStreamException
   *           - if there is a problem parsing the SBML file.
   * @throws BioSimException
   *           - if an error occur in the initialization.
   */
  public void initialize(int runNumber) throws IOException, XMLStreamException, BioSimException {
    if (!isInitialized) {
      SimulationProperties simProperties = properties.getSimulationProperties();
      currProgress = 0;

      setCurrentTime(simProperties.getInitialTime());
      ModelSetup.setupModels(this, ModelType.HSSA);

      computeFixedPoint();
      for (HierarchicalModel model : this.getListOfHierarchicalModels()) {
        totalPropensity.getMath().addChild(model.getPropensity().getVariable());
      }
      totalPropensity.updateVariable(0);
      if (hasEvents) {
        triggeredEventList = new PriorityQueue<>(1);
        computeEvents();
      }

      setupForOutput(runNumber);
      isInitialized = true;
    }

  }

  @Override
  public void cancel() {
    this.cancel = true;
  }

  @Override
  public void setupForNewRun(int newRun) throws IOException {
    SimulationProperties simProperties = properties.getSimulationProperties();
    setCurrentTime(simProperties.getInitialTime());
    restoreInitialState();
    computeFixedPoint();
    setupForOutput(newRun);
  }

  @Override
  public void simulate() throws IOException, XMLStreamException, BioSimException {

    SimulationProperties simProperties = properties.getSimulationProperties();
    double r1 = 0, r2 = 0, totalPropensity = 0, delta_t = 0, nextReactionTime = 0, previousTime = 0, nextEventTime = 0, nextMaxTime = 0;
    double timeLimit = simProperties.getTimeLimit();
    double maxTimeStep = simProperties.getMaxTimeStep();

    if (!isInitialized) {
      this.initialize(1);
    }

    printTime = simProperties.getOutputStartTime();
    previousTime = 0;

    while (currentTime.getState().getValue() < timeLimit) {
      // if (!HierarchicalUtilities.evaluateConstraints(constraintList))
      // {
      // return;
      // }

      if (cancel) {
        break;
      }
      double currentTime = this.currentTime.getState().getValue();
      r1 = getRandom();
      r2 = getRandom();
      computePropensities();
      totalPropensity = getTotalPropensity();
      delta_t = Math.log(1 / r1) / totalPropensity;
      nextReactionTime = currentTime + delta_t;
      nextEventTime = getNextEventTime();
      nextMaxTime = currentTime + maxTimeStep;
      previousTime = currentTime;

      if (nextReactionTime < nextEventTime && nextReactionTime < nextMaxTime) {
        currentTime = nextReactionTime;
      } else if (nextEventTime <= nextMaxTime) {
        currentTime = nextEventTime;
      } else {
        currentTime = nextMaxTime;
      }

      if (currentTime > timeLimit) {
        break;
      }

      if (print) {
        printToFile();
      }

      setCurrentTime(currentTime);
      if (currentTime == nextReactionTime) {
        update(true, false, false, r2, previousTime);
      } else if (currentTime == nextEventTime) {
        update(false, false, true, r2, previousTime);
      } else {
        update(false, true, false, r2, previousTime);
      }
    }

    if (!cancel) {
      setCurrentTime(timeLimit);
      update(false, true, true, r2, previousTime);
      if (print) {
        printToFile();
      }
    }
    closeWriter();
  }

  @Override
  public void printStatisticsTSD() {

  }

  private void update(boolean reaction, boolean rateRule, boolean events, double r2, double previousTime) {
    if (reaction) {
      selectAndPerformReaction(r2);
    }
    if (rateRule) {
      fireRateRules(previousTime);
    }
    if (events) {
      computeEvents();
    }
    computeAssignmentRules();
  }

  private void computePropensities() {
    for (HierarchicalModel modelstate : this.getListOfHierarchicalModels()) {
      for (ReactionNode node : modelstate.getListOfReactions()) {
        node.computePropensity(modelstate.getIndex());
      }
      modelstate.getPropensity().updateVariable(modelstate.getIndex());
    }
    this.totalPropensity.updateVariable(0);
  }

  private void fireRateRules(double previousTime) {}

  private void selectAndPerformReaction(double r2) {
    double sum = 0;
    double threshold = getTotalPropensity() * r2;
    for (HierarchicalModel model : this.getListOfHierarchicalModels()) {
      for (ReactionNode node : model.getListOfReactions()) {
        sum += node.getValue(model.getIndex());

        if (sum >= threshold) {
          node.fireReaction(model.getIndex(), sum - threshold);
          return;
        }
      }
    }
  }

  private double getNextEventTime() {
    checkEvents();
    if (triggeredEventList != null && !triggeredEventList.isEmpty()) { return triggeredEventList.peek().getFireTime(); }
    return Double.POSITIVE_INFINITY;
  }
}
