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
package edu.utah.ece.async.backend.analysis.simulation.hierarchical.methods;

import java.io.IOException;
import java.util.PriorityQueue;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;

import edu.utah.ece.async.backend.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.io.HierarchicalWriter;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.states.EventState;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.util.HierarchicalUtilities;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.util.comp.HierarchicalEventComparator;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.util.setup.ModelSetup;
import edu.utah.ece.async.dataModels.util.exceptions.BioSimException;

/**
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim
 *         Contributors </a>
 * @version %I%
 */
public final class HierarchicalODERKSimulator extends HierarchicalSimulation {

  private boolean                isSingleStep;
  private HighamHall54Integrator odecalc;
  private double                 relativeError, absoluteError;
  private DifferentialEquations  de;
  private final VectorWrapper    vectorWrapper;


  public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory,
    double timeLimit) throws IOException, XMLStreamException, BioSimException {
    this(SBMLFileName, rootDirectory, rootDirectory, 0, timeLimit,
      Double.POSITIVE_INFINITY, 0, null, Double.POSITIVE_INFINITY, 0, null,
      null, 1, 1e-6, 1e-9, "amount", "none", 0, 0, false);
    isInitialized = false;
    isSingleStep = true;
    this.printTime = Double.POSITIVE_INFINITY;
  }


  public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory,
    String outputDirectory, int runs, double timeLimit, double maxTimeStep,
    long randomSeed, JProgressBar progress, double printInterval,
    double stoichAmpValue, JFrame running, String[] interestingSpecies,
    int numSteps, double relError, double absError, String quantityType,
    String abstraction, double initialTime, double outputStartTime)
    throws IOException, XMLStreamException, BioSimException {
    this(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit,
      maxTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running,
      interestingSpecies, numSteps, relError, absError, quantityType,
      abstraction, initialTime, outputStartTime, true);
    this.isInitialized = false;
    this.isSingleStep = false;
  }


  public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory,
    String outputDirectory, int runs, double timeLimit, double maxTimeStep,
    long randomSeed, JProgressBar progress, double printInterval,
    double stoichAmpValue, JFrame running, String[] interestingSpecies,
    int numSteps, double relError, double absError, String quantityType,
    String abstraction, double initialTime, double outputStartTime,
    boolean print) throws IOException, XMLStreamException, BioSimException {
    super(SBMLFileName, rootDirectory, outputDirectory, randomSeed, runs,
      timeLimit, maxTimeStep, 0.0, progress, printInterval, stoichAmpValue,
      running, interestingSpecies, quantityType, abstraction, initialTime,
      outputStartTime, SimType.HODE);
    this.relativeError = relError;
    this.absoluteError = absError;
    this.isSingleStep = false;
    this.absoluteError = absoluteError == 0 ? 1e-12 : absoluteError;
    this.relativeError = absoluteError == 0 ? 1e-9 : relativeError;
    this.printTime = outputStartTime;
    this.vectorWrapper = new VectorWrapper(initValues);
    if (numSteps > 0) {
      setPrintInterval(timeLimit / numSteps);
    }
    odecalc = new HighamHall54Integrator(getMinTimeStep(), getMaxTimeStep(),
      absoluteError, relativeError);
    isInitialized = false;
  }


  public HierarchicalODERKSimulator(HierarchicalMixedSimulator sim,
    HierarchicalModel topmodel) throws IOException, XMLStreamException {
    super(sim);
    this.relativeError = 1e-6;
    this.absoluteError = 1e-9;
    this.odecalc = new HighamHall54Integrator(getMinTimeStep(),
      getMaxTimeStep(), absoluteError, relativeError);
    this.isInitialized = true;
    this.isSingleStep = true;
    this.printTime = Double.POSITIVE_INFINITY;
    this.vectorWrapper = sim.getVectorWrapper();
    de = new DifferentialEquations();
    if (sim.hasEvents()) {
      HierarchicalEventHandler handler = new HierarchicalEventHandler();
      HierarchicalTriggeredEventHandler triggeredHandler =
        new HierarchicalTriggeredEventHandler();
      odecalc.addEventHandler(handler, getPrintInterval(), 1e-20, 10000);
      odecalc.addEventHandler(triggeredHandler, getPrintInterval(), 1e-20,
        10000);
      triggeredEventList =
        new PriorityQueue<EventState>(0, new HierarchicalEventComparator());
      computeEvents();
    }
  }


  @Override
  public void cancel() {
    setCancelFlag(true);
  }


  @Override
  public void clear() {
  }


  @Override
  public void initialize(long randomSeed, int runNumber)
    throws IOException, XMLStreamException {
    if (!isInitialized) {
      setCurrentTime(getInitialTime());
      ModelSetup.setupModels(this, ModelType.HODE, vectorWrapper);
      de = new DifferentialEquations();
      computeFixedPoint();
      if (hasEvents()) {
        HierarchicalEventHandler handler = new HierarchicalEventHandler();
        HierarchicalTriggeredEventHandler triggeredHandler =
          new HierarchicalTriggeredEventHandler();
        odecalc.addEventHandler(handler, getPrintInterval(), 1e-20, 10000);
        odecalc.addEventHandler(triggeredHandler, getPrintInterval(), 1e-20,
          10000);
        triggeredEventList =
          new PriorityQueue<EventState>(1, new HierarchicalEventComparator());
        computeEvents();
      }
      if (!isSingleStep) {
        setupForOutput(runNumber);
      }
      isInitialized = true;
    }
  }


  @Override
  public void simulate() {
    if (!isInitialized) {
      try {
        initialize(0, 1);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (XMLStreamException e) {
        e.printStackTrace();
      }
    }
    double nextEndTime = 0;
    while (currentTime.getValue() < getTimeLimit() && !isCancelFlag()) {
      // if (!HierarchicalUtilities.evaluateConstraints(constraintList))
      // {
      // return;
      // }
      nextEndTime = currentTime.getValue() + getMaxTimeStep();
      if (nextEndTime > printTime) {
        nextEndTime = printTime;
      }
      if (nextEndTime > getTimeLimit()) {
        nextEndTime = getTimeLimit();
      }
      if (vectorWrapper.getSize() > 0) {
        try {
          odecalc.integrate(de, currentTime.getValue(),
            vectorWrapper.getValues(), nextEndTime, vectorWrapper.getValues());
          computeAssignmentRules();
        } catch (NumberIsTooSmallException e) {
          setCurrentTime(nextEndTime);
        }
      } else {
        setCurrentTime(nextEndTime);
      }
      if (!isSingleStep) {
        printToFile();
      }
    }
    if (!isSingleStep) {
      print();
    }
  }


  @Override
  public void setupForNewRun(int newRun) {
  }


  @Override
  public void printStatisticsTSD() {
    // TODO Auto-generated method stub
  }

  public class HierarchicalEventHandler implements EventHandler {

    private double value = -1;


    @Override
    public void init(double t0, double[] y0, double t) {
    }


    @Override
    public double g(double t, double[] y) {
      double returnValue = -value;
      currentTime.setValue(t);
      vectorWrapper.setValues(y);
      for (HierarchicalModel modelstate : modules) {
        int index = modelstate.getIndex();
        for (EventNode event : modelstate.getEvents()) {
          if (!event.isEnabled(index)) {
            if (event.isTriggeredAtTime(t, index)) {
              returnValue = value;
            }
          }
        }
      }
      return returnValue;
    }


    @Override
    public Action eventOccurred(double t, double[] y, boolean increasing) {
      value = -value;
      currentTime.setValue(t);
      vectorWrapper.setValues(y);
      computeAssignmentRules();
      computeEvents();
      return EventHandler.Action.STOP;
    }


    @Override
    public void resetState(double t, double[] y) {
    }
  }

  public class HierarchicalTriggeredEventHandler implements EventHandler {

    private double value = -1;


    @Override
    public void init(double t0, double[] y0, double t) {
    }


    @Override
    public double g(double t, double[] y) {
      currentTime.setValue(0, t);
      if (!triggeredEventList.isEmpty()) {
        if (triggeredEventList.peek().getFireTime() <= t) {
          return value;
        }
      }
      return -value;
    }


    @Override
    public Action eventOccurred(double t, double[] y, boolean increasing) {
      value = -value;
      currentTime.setValue(t);
      vectorWrapper.setValues(y);
      computeAssignmentRules();
      computeEvents();
      return EventHandler.Action.STOP;
    }


    @Override
    public void resetState(double t, double[] y) {
      // TODO Auto-generated method stub
    }
  }

  public class DifferentialEquations
    implements FirstOrderDifferentialEquations {

    @Override
    public int getDimension() {
      return vectorWrapper.getSize();
    }


    public void computeReactionPropensities() {
      for (HierarchicalModel hierarchicalModel : modules) {
        hierarchicalModel.computePropensities();
      }
    }


    @Override
    public void computeDerivatives(double t, double[] y, double[] yDot)
      throws MaxCountExceededException, DimensionMismatchException {
      setCurrentTime(t);
      vectorWrapper.setValues(y);
      computeAssignmentRules();
      computeReactionPropensities();
      int i = 0;
      for (HierarchicalModel hierarchicalModel : modules) {
        int index = hierarchicalModel.getIndex();
        for (VariableNode node : hierarchicalModel.getListOfVariables()) {
          yDot[i++] = node.computeRateOfChange(index, t);
        }
      }
      vectorWrapper.setRates(yDot);
    }
  }
}
