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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import javax.xml.stream.XMLStreamException;

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.SimulationProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.AbstractSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.io.HierarchicalTSDWriter;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.io.HierarchicalWriter;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.AbstractHierarchicalNode.Type;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ConstraintNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.FunctionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.SpeciesConcentration;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.TriggeredEvent;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * This class contains the information to perform simulation.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class HierarchicalSimulation extends AbstractSimulator {

  /**
   * Simulation type.
   */
  public static enum SimType {
    FBA, HODE, HSSA, MIXED, NONE;
  }

  final private StateType atomicType;
  private int currentRun;
  final private StateType parentType;
  private final Random randomNumberGenerator;
  final private SimType type;
  private HierarchicalWriter writer;

  protected boolean cancel;
  protected final VariableNode currentTime;
  protected double currProgress, maxProgress;
  protected boolean hasEvents;
  protected boolean isInitialized;
  protected List<HierarchicalModel> modules;
  protected double printTime;
  protected HierarchicalModel topmodel;
  protected final AnalysisProperties properties;
  protected FunctionNode totalPropensity;
  protected PriorityQueue<TriggeredEvent> triggeredEventList;

  /**
   * Constructs a new simulation instance.
   *
   * @param properties
   *          - the analysis properties.
   * @param type
   *          - the simulation type.
   * @throws XMLStreamException
   *           - if an error occurs when parsing the SBML file.
   * @throws IOException
   *           - if an error occurs with handling input/output files.
   * @throws BioSimException
   *           - if something went wrong during simulation.
   */
  public HierarchicalSimulation(AnalysisProperties properties, SimType type) throws XMLStreamException, IOException, BioSimException {
    this.printTime = 0;

    this.parentType = StateType.SPARSE;
    this.type = type;

    if (type == SimType.HODE || type == SimType.MIXED) {
      this.atomicType = StateType.VECTOR;
    } else {
      this.atomicType = StateType.SCALAR;
    }

    this.properties = properties;
    SimulationProperties simProperties = properties.getSimulationProperties();
    this.maxProgress = simProperties.getTimeLimit() * simProperties.getRun();
    this.topmodel = new HierarchicalModel("topmodel", 0);
    this.currentTime = new VariableNode("_time", StateType.SCALAR);
    this.hasEvents = false;
    this.currentRun = 1;
    this.randomNumberGenerator = new Random(simProperties.getRndSeed());
    this.writer = new HierarchicalTSDWriter();

    this.totalPropensity = new FunctionNode(new VariableNode("propensity", StateType.SCALAR), new HierarchicalNode(Type.PLUS));

  }

  /**
   * Copy constructor.
   *
   * @param copy
   *          - the simulation that will get copied from.
   */
  public HierarchicalSimulation(HierarchicalSimulation copy) {
    this.properties = copy.properties;
    this.printTime = copy.printTime;
    this.type = copy.type;
    this.topmodel = copy.topmodel;
    this.currentTime = copy.currentTime;
    this.randomNumberGenerator = copy.randomNumberGenerator;
    this.hasEvents = copy.hasEvents;
    this.atomicType = copy.atomicType;
    this.parentType = copy.parentType;
    // this.totalPropensity = copy.totalPropensity;
  }

  /**
   * Adds a model to the simulation.
   *
   * @param hierarchicalModel
   *          - the model to be added.
   */
  public void addHierarchicalModel(HierarchicalModel hierarchicalModel) {
    if (modules == null) {
      modules = new ArrayList<>();
    }
    modules.add(hierarchicalModel);
  }

  /**
   * Adds a variable to the print list. The variable will have its value written out to the output of the simulation.
   *
   * @param id
   *          - the id of the variable.
   * @param node
   *          - the variable node.
   * @param index
   *          - the model index.
   * @param isConcentration
   *          - whether to print the value as a concentration.
   */
  public void addPrintVariable(String id, HierarchicalNode node, int index, boolean isConcentration) {
    writer.addVariable(id, node, index, isConcentration);
  }

  /**
   * Evaluates all constraints and check if any of them is violated.
   *
   * @return true if all constraints evaluate to true.
   */
  public boolean evaluateConstraints() {
    boolean hasSuccess = true;
    for (HierarchicalModel model : modules) {
      for (ConstraintNode constraintNode : model.getListOfConstraints()) {
        hasSuccess = hasSuccess && constraintNode.evaluateConstraint(model.getIndex());
      }
    }
    return hasSuccess;
  }

  /**
   * Gets the state type for variables.
   *
   * @return the state type.
   */
  public StateType getAtomicType() {
    return this.atomicType;
  }

  /**
   * Gets the state type for variables that are collections.
   *
   * @return the state type for collections.
   */
  public StateType getCollectionType() {
    return this.parentType;
  }

  /**
   * Gets the current simulation time.
   *
   * @return the current run.
   */
  public int getCurrentRun() {
    return currentRun;
  }

  /**
   * Gets the simulation time node.
   *
   * @return the current time node.
   */
  public VariableNode getCurrentTime() {
    return currentTime;
  }

  /**
   * Gets the list of hierarchical models in the simulation.
   *
   * @return the list of hierarchical models.
   */
  public List<HierarchicalModel> getListOfHierarchicalModels() {
    return modules;
  }

  /**
   * Gets the analysis properties.
   *
   * @return the analysis properties.
   */
  public AnalysisProperties getProperties() {
    return properties;
  }

  /**
   * Gets the simulation type.
   *
   * @return the simulation type.
   */
  public SimType getSimType() {
    return type;
  }

  /**
   * Gets the value of a top-level variable.
   *
   * @param variable
   *          - the id of the variable.
   *
   * @return the value of the variable if it exists. NaN otherwise.
   */
  public double getTopLevelValue(String variable) {
    if (topmodel != null) {
      HierarchicalNode variableNode = topmodel.getNode(variable);
      if (variableNode != null) { return variableNode.getValue(0); }
    }

    return Double.NaN;
  }

  /**
   * Checks if the simulation has any event.
   *
   * @return true if the simulation has at least one event.
   */
  public boolean hasEvents() {
    return this.hasEvents;
  }

  /**
   * Sets the current simulation run.
   *
   * @param currentRun
   *          - the current simulation run.
   */
  public void setCurrentRun(int currentRun) {
    this.currentRun = currentRun;
  }

  /**
   * Sets the current time;
   *
   * @param currentTime
   *          - the current simulation time.
   */
  public void setCurrentTime(double currentTime) {
    this.currentTime.setValue(0, currentTime);
  }

  /**
   * Sets the hasEvents flag.
   *
   * @param value
   *          - whether the simulation has events.
   */
  public void setHasEvents(boolean value) {
    this.hasEvents = value;
  }

  /**
   * Sets the list of hierarchical models.
   *
   * @param modules
   *          - the list of hierarchical models.
   */
  public void setListOfHierarchicalModels(List<HierarchicalModel> modules) {
    this.modules = modules;
  }

  /**
   * Sets the value of a top-level variable.
   *
   * @param variable
   *          - the id of the variable.
   * @param value
   *          - the new value of the variable.
   */
  public void setTopLevelValue(String variable, double value) {
    if (topmodel != null) {
      HierarchicalNode variableNode = topmodel.getNode(variable);
      if (variableNode != null) {
        variableNode.setValue(0, value);
      }
    }
  }

  /**
   * Sets the top-level model.
   *
   * @param topmodel
   *          - the new top-level model.
   */
  public void setTopmodel(HierarchicalModel topmodel) {
    this.topmodel = topmodel;
  }

  protected void checkEvents() {
    double time = currentTime.getState().getValue();
    for (HierarchicalModel model : modules) {
      int index = model.getIndex();
      for (EventNode event : model.getListOfEvents()) {
        if (!event.isDeleted(index) && event.isTriggeredAtTime(time, index)) {
          event.setMaxDisabledTime(index, Double.NEGATIVE_INFINITY);
          event.setMinEnabledTime(index, Double.POSITIVE_INFINITY);
          double fireTime = currentTime.getState().getValue() + event.evaluateFireTime(index);
          TriggeredEvent triggered = new TriggeredEvent(index, fireTime, event);
          triggered.setPriority(event.evaluatePriority(index));
          if (event.getState().getChild(index).isUseTriggerValue()) {
            double[] eventAssignments = event.computeEventAssignmentValues(index, currentTime.getState().getValue());

            if (eventAssignments != null) {
              triggered.setAssignmentValues(eventAssignments);
            }

          }
          triggeredEventList.add(triggered);
          if (!event.getState().getChild(index).isPersistent()) {
            event.addTriggeredEvent(index, triggered);
          }
        }

      }
    }
  }

  protected void closeWriter() throws IOException {
    if (writer != null) {
      writer.close();
    }
  }

  protected void computeAssignmentRules() {
    boolean changed = true;
    while (changed) {
      changed = false;
      for (HierarchicalModel modelstate : this.modules) {
        if (modelstate.getListOfAssignmentRules() != null) {
          for (FunctionNode node : modelstate.getListOfAssignmentRules()) {
            changed = changed | node.updateVariable(modelstate.getIndex());
          }
        }
      }
    }
  }

  protected void computeEvents() {
    boolean changed = true;
    double time = currentTime.getState().getValue();

    while (changed) {
      changed = false;

      checkEvents();

      if (triggeredEventList != null && !triggeredEventList.isEmpty()) {
        TriggeredEvent eventState = triggeredEventList.peek();
        if (eventState.getFireTime() <= time) {
          triggeredEventList.poll();
          eventState.fireEvent(time);
          reevaluatePriorities();
          changed = true;
        } else {
          break;
        }
      }
    }

  }

  /**
   * Calculate fixed-point of initial assignments
   *
   * @param modelstate
   * @param variables
   * @param math
   */
  protected void computeFixedPoint() {
    boolean changed = true;

    while (changed) {
      changed = false;
      resetRateValues();
      for (HierarchicalModel modelstate : this.modules) {
        int index = modelstate.getIndex();

        if (modelstate.getListOfInitialConcentrations() != null) {
          for (SpeciesConcentration concentration : modelstate.getListOfInitialConcentrations()) {

            if (!concentration.hasRule()) {
              changed = changed | concentration.computeValue();
            }
          }
        }

        if (modelstate.getListOfRateRules() != null) {
          for (FunctionNode rateRule : modelstate.getListOfRateRules()) {
            rateRule.updateRate(index);
          }
        }

        if (modelstate.getListOfReactions() != null) {
          for (ReactionNode node : modelstate.getListOfReactions()) {
            changed = changed | node.computePropensity(index);
          }

          modelstate.getPropensity().updateVariable(index);
        }

        if (modelstate.getListOfAssignmentRules() != null) {
          for (FunctionNode node : modelstate.getListOfAssignmentRules()) {
            changed = changed | node.updateVariable(index);
          }
        }

        if (modelstate.getListOfInitialAssignments() != null) {
          for (FunctionNode node : modelstate.getListOfInitialAssignments()) {
            changed = changed | node.updateVariable(index);
          }
        }
      }
    }
  }

  /**
   * @return the randomNumberGenerator
   */
  protected double getRandom() {
    return randomNumberGenerator.nextDouble();
  }

  protected double getRoundedDouble(double value) {
    if (Double.isFinite(value)) {
      BigDecimal bd = new BigDecimal(value);
      bd = bd.setScale(6, BigDecimal.ROUND_HALF_EVEN);
      value = bd.doubleValue();
      bd = null;
    }
    return value;
  }

  /**
   * Returns the total propensity of all model states.
   */
  protected double getTotalPropensity() {
    return totalPropensity != null ? totalPropensity.getVariable().getState().getValue() : 0;
  }

  protected void printToFile() {
    SimulationProperties simProperties = properties.getSimulationProperties();
    double timeLimit = simProperties.getTimeLimit();
    double time = currentTime.getState().getValue();

    while (time >= printTime && printTime <= timeLimit) {
      try {
        writer.print(printTime);
      }
      catch (IOException e) {
        e.printStackTrace();
      }

      currProgress += simProperties.getPrintInterval();
      printTime = getRoundedDouble(printTime + simProperties.getPrintInterval());

      message.setInteger((int) (Math.ceil(100 * currProgress / maxProgress)));
      parent.send(RequestType.REQUEST_PROGRESS, message);
    }
  }

  protected void reevaluatePriorities() {
    PriorityQueue<TriggeredEvent> tmp = new PriorityQueue<>(1);
    while (triggeredEventList != null && !triggeredEventList.isEmpty()) {
      TriggeredEvent eventState = triggeredEventList.poll();
      EventNode event = eventState.getParent();
      int index = eventState.getIndex();
      eventState.setPriority(event.evaluatePriority(index));
      tmp.add(eventState);
    }
    triggeredEventList = tmp;
  }

  protected void resetRateValues() {
    for (HierarchicalModel hierarchicalModel : modules) {
      int index = hierarchicalModel.getIndex();
      for (VariableNode node : hierarchicalModel.getListOfVariables()) {
        HierarchicalState state = node.getState().getChild(index);
        if (!state.hasRate()) {
          state.setRateValue(0);
        }
      }
    }
  }

  protected void restoreInitialState() {

    if (triggeredEventList != null) {
      triggeredEventList.clear();
    }

    for (HierarchicalModel hierarchicalModel : modules) {
      int index = hierarchicalModel.getIndex();
      for (VariableNode node : hierarchicalModel.getListOfVariables()) {
        node.getState().getChild(index).restoreInitialValue();
      }

      for (EventNode node : hierarchicalModel.getListOfEvents()) {
        node.resetEvents(index);
      }
    }
  }

  protected void setupForOutput(int currentRun) throws IOException {
    setCurrentRun(currentRun);
    String outputDirectory = properties.getOutDir();
    if (outputDirectory.equals(".")) {
      outputDirectory = properties.getDirectory();
    }
    writer.init(outputDirectory + File.separator + "run-" + currentRun + ".tsd");

  }

}