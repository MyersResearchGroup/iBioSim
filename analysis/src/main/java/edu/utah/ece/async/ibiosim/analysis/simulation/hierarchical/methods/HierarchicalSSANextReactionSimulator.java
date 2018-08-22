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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import javax.xml.stream.XMLStreamException;

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.SimulationProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.AbstractHierarchicalNode.Type;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.Evaluator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup.ModelSetup;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * Hierarchical SSA simulator using Gibson and Bruck method.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HierarchicalSSANextReactionSimulator extends HierarchicalSimulation {
  private final boolean print;
  private double totalPropensity;
  private Map<HierarchicalState, List<DependencyNode>> dependencyGraph;

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
  public HierarchicalSSANextReactionSimulator(AnalysisProperties properties) throws IOException, XMLStreamException, BioSimException {
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
  public HierarchicalSSANextReactionSimulator(AnalysisProperties properties, boolean print) throws IOException, XMLStreamException, BioSimException {
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

      if (hasEvents) {
        triggeredEventList = new PriorityQueue<>(1);
        computeEvents();
      }

      buildDependencyGraph();
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
    double r1 = 0, r2 = 0, delta_t = 0, nextReactionTime = 0, previousTime = 0, nextEventTime = 0, nextMaxTime = 0;
    double timeLimit = simProperties.getTimeLimit();
    double maxTimeStep = simProperties.getMaxTimeStep();

    if (!isInitialized) {
      this.initialize(1);
    }

    printTime = simProperties.getOutputStartTime();
    previousTime = 0;

    computePropensities();

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

  private void buildDependencyGraph() {
    dependencyGraph = new HashMap<>();
    for (HierarchicalModel model : this.getListOfHierarchicalModels()) {
      int index = model.getIndex();
      for (ReactionNode node : model.getListOfReactions()) {
        getReactionDependency(model, node, index);
      }
    }
  }

  private void getReactionDependency(HierarchicalModel model, ReactionNode node, int index) {
    List<HierarchicalNode> dependencies = getDependency(node);

    int[] arrayIndex = null;
    for (HierarchicalNode subNode : node) {
      if (node.getListOfDimensions() != null) {
        int n = node.getListOfDimensions().size();
        arrayIndex = new int[n];
        for (int i = 0; i < n; i++) {
          arrayIndex[i] = (int) node.getListOfDimensions().get(i).getValue(index);
        }
      }
      DependencyNode dependencyNode = new DependencyNode(model, node, arrayIndex);
      for (HierarchicalNode dependency : dependencies) {
        HierarchicalState state = null;
        if (dependency.isName()) {
          state = dependency.getRootState(index);
          if (!dependencyGraph.containsKey(state)) {
            dependencyGraph.put(state, new ArrayList<>());
          }
          dependencyGraph.get(state).add(dependencyNode);
        } else if (dependency.getType() == Type.FUNCTION_SELECTOR) {
          HierarchicalNode variable = dependency.getChild(0);
          state = variable.getState().getChild(index);
          for (int i = 1; i < dependency.getNumOfChild(); i++) {
            int selectorIndex = (int) Evaluator.evaluateExpressionRecursive(dependency.getChild(i), index);
            state = state.getChild(selectorIndex);
          }

        }

        if (state != null) {
          if (!dependencyGraph.containsKey(state)) {
            dependencyGraph.put(state, new ArrayList<>());
          }
          dependencyGraph.get(state).add(dependencyNode);
        }
      }
    }
  }

  private List<HierarchicalNode> getDependency(ReactionNode node) {

    List<HierarchicalNode> output = new ArrayList<>();
    LinkedList<HierarchicalNode> unprocessed = new LinkedList<>();
    unprocessed.add(node.getForwardRate());

    if (node.getReverseRate() != null) {
      unprocessed.add(node.getReverseRate());
    }

    while (!unprocessed.isEmpty()) {
      HierarchicalNode currentNode = unprocessed.remove();

      if (currentNode.isName()) {
        output.add(currentNode);
      } else if (currentNode.getType() == Type.FUNCTION_SELECTOR) {
        if (currentNode.getChild(0).isName()) {
          output.add(currentNode);
        }
      } else {
        if (currentNode.getNumOfChild() > 0) {
          for (int i = 0; i < currentNode.getNumOfChild(); i++) {
            unprocessed.add(currentNode.getChild(i));
          }
        }
      }

    }

    return output;
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
    totalPropensity = 0;
    for (HierarchicalModel modelstate : this.getListOfHierarchicalModels()) {
      modelstate.computePropensities(computeRateOfChange);
      totalPropensity += modelstate.getPropensity();
    }
  }

  private void fireRateRules(double previousTime) {}

  private void selectAndPerformReaction(double r2) {
    double sum = 0;
    double threshold = totalPropensity * r2;
    HierarchicalModel selectedModel = null;
    ReactionNode selectedReaction = null;
    for (HierarchicalModel model : this.getListOfHierarchicalModels()) {
      sum += model.getPropensity();
      if (sum >= threshold) {
        threshold = sum - threshold;
        selectedModel = model;
        break;
      }
    }
    if (selectedModel == null) { return; }

    int index = selectedModel.getIndex();
    sum = 0;
    for (ReactionNode node : selectedModel.getListOfReactions()) {
      sum += node.getState().getChild(index).getValue();
      if (sum >= threshold) {
        threshold = sum - threshold;
        sum = 0;
        selectedReaction = node;
        break;
      }
    }

    if (selectedReaction != null) {
      for (HierarchicalNode subNode : selectedReaction) {
        sum += selectedReaction.getValue(index);
        if (sum >= threshold) {
          performReaction(selectedModel, selectedReaction, sum - threshold, index);
          break;
        }
      }
    }
  }

  private void performReaction(HierarchicalModel model, ReactionNode reactionNode, double threshold, int index) {
    double oldValue = reactionNode.getValue(index);
    List<HierarchicalState> listOfUpdates = reactionNode.fireReactionAndUpdatePropensity(index, threshold);
    double newValue = reactionNode.getValue(index);

    double change = (newValue - oldValue);
    totalPropensity += change;
    model.updateModelPropensity(change);
    if (reactionNode.isArray()) {
      double totalReactionPropensity = reactionNode.getState().getChild(index).getValue();
      reactionNode.getState().getChild(index).setStateValue(totalReactionPropensity + change);
    }
    updateDependencies(listOfUpdates);
    return;
  }

  private void updateDependencies(List<HierarchicalState> listOfUpdates) {
    HashSet<DependencyNode> updatedNodes = new HashSet<>();
    for (HierarchicalState update : listOfUpdates) {

      if (!dependencyGraph.containsKey(update)) {
        continue;
      }

      List<DependencyNode> dependencies = dependencyGraph.get(update);

      for (DependencyNode dependency : dependencies) {
        if (!updatedNodes.contains(dependency)) {
          updatedNodes.add(dependency);
        } else {
          continue;
        }

        HierarchicalModel model = dependency.model;
        ReactionNode reactionNode = dependency.node;
        int index = model.getIndex();

        if (dependency.arrayIndex != null) {
          for (int i = 0; i < dependency.arrayIndex.length; i++) {
            reactionNode.getListOfDimensions().get(i).setValue(index, dependency.arrayIndex[i]);
          }
        }
        double oldValue = reactionNode.getValue(index);
        reactionNode.computePropensity(index, computeRateOfChange);
        double newValue = reactionNode.getValue(index);
        double change = (newValue - oldValue);
        totalPropensity += change;
        model.updateModelPropensity(change);
        if (reactionNode.isArray()) {
          double totalReactionPropensity = reactionNode.getState().getChild(index).getValue();
          reactionNode.getState().getChild(index).setStateValue(totalReactionPropensity + change);
        }
      }
    }
  }

  private double getNextEventTime() {
    checkEvents();
    if (triggeredEventList != null && !triggeredEventList.isEmpty()) { return triggeredEventList.peek().getFireTime(); }
    return Double.POSITIVE_INFINITY;
  }

  private class DependencyNode {
    ReactionNode node;
    int[] arrayIndex;
    HierarchicalModel model;

    public DependencyNode(HierarchicalModel model, ReactionNode node, int[] arrayIndex) {
      this.node = node;
      this.arrayIndex = arrayIndex;
      this.model = model;
    }
  }
}
